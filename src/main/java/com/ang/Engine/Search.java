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
            double bestEval = -Global.INFINITY;
            Move bestMove = Move.invalid();         
            MoveList moves = Board.allMoves(rec, engineCol);
            moves = orderMoves(rec, moves, engineCol);
            for (int i = 0; i < moves.length(); i++) {
                if (System.currentTimeMillis() >= endTime) {
                    System.out.println("maximum complete depth: " 
                            + (maxDepth - 1));
                    System.out.println("final move from: " 
                            + lastPlyBestMove.from + " to: " + lastPlyBestMove.to);

                    return lastPlyBestMove;
                }

                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }
                BoardRecord tempRec = rec.copy();
                
                if (Board.tryMove(tempRec, m)) {
                    double eval;

                    // repetitions
                    if (Global.repTable.checkRepetitions(tempRec, Piece.NONE.val()) >= 3) {
                        eval = 0.0; // draw by repetition
                    } else {
                        eval = alphaBetaNega(tempRec, 
                                -Global.INFINITY, Global.INFINITY, 
                                playerCol, 0);
                    }
                    
                    if (engineCol == Piece.BLACK.val()) {
                        eval = -eval;
                    }
                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = m;

                        System.out.println("depth: " + maxDepth + " eval: " 
                                + eval + " from: " + m.from+" to: " + m.to); 
                        System.out.println("tt hits: "+ttHits);
                        System.out.println("quiesces: " + Global.quiesces 
                            + " searches: " + Global.searches);
                    }
                }
            }

            if (bestMove.isInvalid()) { // TODO : checkmate / stalemate
                return bestMove;
            }
            lastPlyBestMove = bestMove;
            maxDepth++;
        }
    }

    private double alphaBetaNega(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        Global.searches++;

        boolean foundMove = false;
        double bestEval = -Global.INFINITY;

        if (depth == maxDepth) {
            return quiesce(rec, alpha, beta, col, depth);
        }

        MoveList moves  = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                break;
            }
            foundMove = false;
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, move)) {
                foundMove = true;
                double eval = -alphaBetaNega(tempRec, 
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

        if (!foundMove) {
            int kingPos = Board.findKing(rec, col);
            if (kingPos == -1) {
                return 0.0;
            }
            if (Board.isUnderAttack(rec, kingPos, col)) {
                return Global.INFINITY - depth * 10E300;
            }
            return 0.0;
        } 
        return bestEval;
    }

    private double quiesce(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        Global.quiesces++;
        double standPat = evaluate(rec, col);
        double bestEval = standPat;

        if (standPat >= beta) { 
            return standPat;
        }
        if (alpha < standPat) {
            alpha = standPat;
        }

        MoveList moves = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                return standPat;
            }
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }
            
            // heuristic cut-offs
            
            int attackDelta = rec.whiteAttacks[move.to] - rec.blackAttacks[move.to];
            if ((col == Piece.WHITE.val()) && (attackDelta < 0) 
                    && !Board.isPromotion(rec, move)) {
                continue;
            } else if ((col == Piece.BLACK.val()) && (attackDelta > 0)) {
                continue;
            }
            if ((standPat + pieceValue(rec, move.to) + 200 < alpha)
                    && (rec.minorPieceCount() > 2) && !Board.isPromotion(rec, move)) {
                continue;
            }

            BoardRecord tempRec = rec.copy(); // TODO : test that testing checks is working
            if (Board.tryMove(tempRec, move)) { 
                if ((rec.board[move.to] == Piece.NONE.val())
                        && (!Board.isUnderAttack(rec, Board.findKing(rec, col), col))) {
                    continue;
                }

                double eval = -quiesce(tempRec, -beta, -alpha, 
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

        return bestEval;
    }

    private double pieceValue(BoardRecord rec, int pos) {
        double value = 0.0;
        int pieceCol = rec.board[pos] & 0b11000;
        int heatmapIndex = (pieceCol == Piece.WHITE.val()) ? pos : 63 - pos;

        switch (rec.board[pos] & 0b111) {
        case 1:
            value = 100.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 2:
            value = 320.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 3:
            value = 330.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 4:
            value = 500.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 5:
            value = 900.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 6:
            // king heatmap changes in endgame
            double[] heatmap = (rec.minorPieceCount() < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = 20000.0 + heatmap[heatmapIndex];
            break;
        default:
            value = 0.0;
        }
        return (pieceCol == Piece.WHITE.val()) ? value : - value;
    }

    // TODO : tune this
    private double endgameKingPosEval(BoardRecord rec, int currentCol) {
        double eval = 0.0;
        int whiteKingPos = Board.findKing(rec, Piece.WHITE);
        int blackKingPos = Board.findKing(rec, Piece.BLACK);

        int whiteRank = (int) Math.floor(whiteKingPos / 8);
        int whiteFile = whiteKingPos % 8;
        int blackRank = (int) Math.floor(blackKingPos / 8);
        int blackFile = blackKingPos % 8;

        if (currentCol == Piece.BLACK.val()) {
            int whiteCentreDistRank = Math.max(3 - whiteRank, whiteRank - 4);
            int whiteCentreDistFile = Math.max(3 - whiteFile, whiteFile - 4);
            int whiteCentreDist = whiteCentreDistRank + whiteCentreDistFile;
            eval += whiteCentreDist;
        } else {
            int blackCentreDistRank = Math.max(3 - blackRank, blackRank - 4);
            int blackCentreDistFile = Math.max(3 - blackFile, blackFile - 4); 
            int blackCentreDist = blackCentreDistRank + blackCentreDistFile;
            eval += blackCentreDist;
        }

        int rankSeperation = Math.abs(whiteRank - blackRank);
        int fileSeperation = Math.abs(whiteFile - blackFile);
        int kingSeperationSquared = (rankSeperation * rankSeperation) 
                                  + (fileSeperation * fileSeperation);
        eval += (128 - kingSeperationSquared) / 80;

        return eval * 50;
    }

    private double pieceValueEval(BoardRecord rec) {
        double eval = 0.0;
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            eval += pieceValue(rec, pos);
        }
        return eval;
    }

    private double evaluate(BoardRecord rec, int currentCol) {
        double eval = 0.0;
        eval += (currentCol == Piece.WHITE.val()) 
        ? pieceValueEval(rec) 
        : -pieceValueEval(rec);
        if (rec.minorPieceCount() < 3) {
            eval += endgameKingPosEval(rec, currentCol);
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