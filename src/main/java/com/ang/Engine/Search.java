package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Core.Moves.*;

// TODO : compare move gen results to StockFish in random positions
//      - need to allow engine to play as white and black

// TODO : optimization:
//      - history heuristic
//      - killer moves
//      - pawn position evaluation
//      - futility pruning
//      - multithreading

// TODO : fixes
//      - test the transposition table / node types
//      - bot doesn't find mate well in endgames, usually stalemate - Fixed? test!
public class Search {  
    public int      engineCol;

    private int     contemptFactor = 0;
    private int     timeLimit;
    private int     playerCol;
    private int     ttHits;
    private int     maxDepth;
    private long    endTime;

    public Search(int searchTime, Piece col) {
        this(searchTime, col.val());
    }
    public Search(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.engineCol = col;
        this.playerCol = Piece.WHITE.val();
    }

    public Move generateMove(BoardRecord rec) {
        endTime = System.currentTimeMillis() + timeLimit;
        Move lastPlyBestMove = Move.invalid();
        maxDepth = 1;
        while (true) {
            Global.quiesces = 0;
            Global.searches = 0;
            ttHits = 0;

            int bestEval = -Global.INFINITY;

            Move bestMove = Move.invalid();         
            MoveList moves = Board.allMoves(rec, engineCol);
            moves = orderMoves(rec, moves, engineCol);
            for (int i = 0; i < moves.length(); i++) {
                if (System.currentTimeMillis() >= endTime) {
                    System.out.println("maximum complete depth: " 
                            + (maxDepth - 1));
                    System.out.println("final move from: " 
                            + lastPlyBestMove.from + " to: " + lastPlyBestMove.to 
                            + " with eval " + bestEval);
                    System.out.println("white " + evaluate(rec, Piece.WHITE.val())
                            + " black " + evaluate(rec, Piece.BLACK.val()));
                    return lastPlyBestMove;
                }

                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }
                BoardRecord tempRec = rec.copy();
                
                if (Board.tryMove(tempRec, m)) {
                    int eval;

                    switch (Board.endState(tempRec, engineCol)) {
                    case DRAW:
                        int drawEval = (engineCol == Piece.WHITE.val())
                        ? contemptFactor
                        : -contemptFactor;
                        eval = drawEval;
                        break;
                    case CHECKMATE:
                        int mateEval = (engineCol == Piece.WHITE.val())
                        ? -Global.INFINITY
                        : Global.INFINITY;
                        eval = mateEval;
                        break;
                    default:
                        eval = alphaBetaNega(tempRec, 
                                -Global.INFINITY, Global.INFINITY, 
                                playerCol, 0);
                        break;
                    }
                    
                    if (engineCol == Piece.BLACK.val()) {
                        eval = -eval;
                    }
                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = m;

                        System.out.println("depth: " + maxDepth + " eval: " 
                                + eval + " from: " + m.from+" to: " + m.to); 
                        // System.out.println("king pos eval "+endgameKingPosEval(tempRec, engineCol));
                        // System.out.println("tt hits: "+ttHits);
                        // System.out.println("quiesces: " + Global.quiesces 
                        //     + " searches: " + Global.searches);
                    }
                }
            }

            if (bestMove.isInvalid()) { // TODO : checkmate / stalemate
                return lastPlyBestMove;
            }
            lastPlyBestMove = bestMove;
            maxDepth++;
        }
    }

    private int alphaBetaNega(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        Global.searches++;

        boolean foundMove = false;
        int bestEval = -Global.INFINITY;
        if (depth == maxDepth) {
            return quiesce(rec, alpha, beta, col, depth);
        }

        MoveList moves  = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                foundMove = true;
                break;
            }
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, move)) {
                foundMove = true;
                int eval = -alphaBetaNega(tempRec, 
                        -beta, -alpha, Piece.opposite(col).val(), depth + 1);
                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                        // save to transposition table
                        TableEntry te = new TableEntry(SearchNode.ALL, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    } else { 
                        // save to transposition table
                        TableEntry te = new TableEntry(SearchNode.PV, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    }
                }
                if (eval >= beta) {
                    // save to transposition table
                    TableEntry te = new TableEntry(SearchNode.CUT, move, 
                            eval, depth, 0);
                    Global.tTable.saveHash(te, tempRec, col);
                    break;
                }
            }
        }
        if (foundMove) {
            return bestEval;
        }

        switch (Board.endState(rec, col)) {
        case CHECKMATE:
            int mateEval = (col == Piece.WHITE.val())
            ? -Global.INFINITY + depth * 100
            : Global.INFINITY - depth * 100;   
            return mateEval;
        case DRAW:
            int drawEval = (col == Piece.WHITE.val())
            ? contemptFactor
            : -contemptFactor;
            return drawEval;
        default:
            bestEval = evaluate(rec, col);
            System.out.println("none state "+bestEval);
            rec.printBoard();
            return bestEval; // why getting through?? TODO : investigate
        }
    }
    // TODO : fix : quiescence doesnt see positions where enemy attacks the engine
    //          engine will hang pieces if they are
    private int quiesce(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        Global.quiesces++;
        int standPat = evaluate(rec, col);
        int bestEval = standPat;

        if (standPat >= beta) { 
            return standPat;
        }
        if (alpha < standPat) {
            alpha = standPat;
        }

        MoveList moves = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        
        for (int i = 0; i < moves.length(); i++) {
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }
            
            // heuristic cut-offs
            
            if ((standPat + pieceValues(rec, move.to) + 200 < alpha)
                    && (rec.minorPieceCount() > 2) && !Board.isPromotion(rec, move)) {
                continue;
            }

            BoardRecord tempRec = rec.copy(); 
            if (Board.tryMove(tempRec, move)) { 
                int opCol = Piece.opposite(col).val();
                int enemyKingPos = Board.findKing(tempRec, opCol);
                if (enemyKingPos == -1) {
                    continue;
                }
                if ((rec.board[move.to] == Piece.NONE.val())
                        && (!Board.underAttack(tempRec, enemyKingPos, opCol))) {
                    continue;
                }

                int eval = -quiesce(tempRec, -beta, -alpha, 
                        Piece.opposite(col).val(), depth + 1);

                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                        // save to transposition table
                        TableEntry te = new TableEntry(SearchNode.ALL, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    } else { 
                        // save to transposition table
                        TableEntry te = new TableEntry(SearchNode.PV, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    }
                }
                if (eval >= beta) {
                    // save to transposition table
                    TableEntry te = new TableEntry(SearchNode.CUT, move, 
                            eval, depth, 0);
                    Global.tTable.saveHash(te, tempRec, col);
                    break;
                }
            }   
        }

        switch (Board.endState(rec, col)) {
        case CHECKMATE:
            int mateEval = (col == Piece.WHITE.val())
            ? -Global.INFINITY + depth * 100
            : Global.INFINITY - depth * 100;
            return mateEval;
        case DRAW:
            int drawEval = (col == Piece.WHITE.val())
            ? contemptFactor
            : -contemptFactor;
            // System.out.println("quiescence draw state "+drawEval);
            // rec.printBoard();
            return drawEval;
        default:
            return evaluate(rec, col);
            // return evaluate(rec, col);
            // return bestEval;
        }
    }

    private int pieceValues(BoardRecord rec, int pos) {
        int value = 0;
        int pieceCol = rec.board[pos] & 0b11000;
        int heatmapIndex = (pieceCol == Piece.WHITE.val()) ? pos : 63 - pos;

        switch (rec.board[pos] & 0b111) {
        case 1:
            value = 100 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 2:
            value = 320 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 3:
            value = 330 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 4:
            value = 500 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 5:
            value = 900 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 6:
            // king heatmap changes in endgame
            int[] heatmap = (rec.minorPieceCount() < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = 20000 + heatmap[heatmapIndex];
            break;
        default:
            value = 0;
        }
        return (pieceCol == Piece.WHITE.val()) ? value : - value;
    }

    private int mopupEvaluation(BoardRecord rec, int currentCol) {
        int eval = 0;

        int friendlyKingPos = Board.findKing(rec, currentCol);
        int enemyKingPos = Board.findKing(rec, Piece.opposite(currentCol));   

        int friendlyKingRank = (int) Math.floor(friendlyKingPos / 8);
        int friendlyKingFile = friendlyKingPos % 8;
        int enemyKingRank = (int) Math.floor(enemyKingPos / 8);
        int enemyKingFile = enemyKingPos % 8;

        // push enemy king to edge
        int enemyCentreDistRank = Math.max(3 - enemyKingRank, enemyKingRank - 4);
        int enemyCentreDistFile = Math.max(3 - enemyKingFile, enemyKingFile - 4);
        eval += (enemyCentreDistRank + enemyCentreDistFile);

        // bring king closer to enemy king
        int kingSeperationRank = Math.abs(friendlyKingRank - enemyKingRank);
        int kingSeperationFile = Math.abs(friendlyKingFile - enemyKingFile);
        int kingSeperationSquared = (kingSeperationRank * kingSeperationRank) 
                                  + (kingSeperationFile * kingSeperationFile);
        eval += (128 - kingSeperationSquared);

        // distance rooks from enemy king
        for (int i = 0; i < rec.rooks.length(); i++) {
            int pos = rec.rooks.at(i);
            if ((rec.board[pos] & 0b11000) == currentCol) {
                int rookRank = (int) Math.floor(pos / 8);
                int rookFile = pos % 8;
                int kingRookSeperationRank = Math.abs(rookRank - enemyKingRank);
                int kingRookSeperationFile = Math.abs(rookFile - enemyKingFile);

                int kingRookSeperationSquared = (kingRookSeperationRank * kingRookSeperationRank)
                                              + (kingRookSeperationFile * kingRookSeperationFile);

                eval += kingRookSeperationSquared;
            }
        }

        return eval;
    }

    private int pieceValueEval(BoardRecord rec) {
        int eval = 0;
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            eval += pieceValues(rec, pos);
        }
        return eval;
    }

    private int evaluate(BoardRecord rec, int currentCol) {        
        int eval = 0;
        eval += (currentCol == Piece.WHITE.val()) 
        ? pieceValueEval(rec) 
        : -pieceValueEval(rec);
        if (rec.minorPieceCount() < 3) {
             eval += mopupEvaluation(rec, currentCol);
        }
        return eval;
    }

    private MoveList orderMoves(BoardRecord rec, MoveList moves, int moveCol) {
        moves.attacksToFront();

        TableEntry te = Global.tTable.searchTable(
                Global.tTable.zobristHash(rec, moveCol));
        if (te != null) { // tt hit
            ttHits++;
            moves.sendToFront(te.bestMove);
        }
        return moves;
    }
}