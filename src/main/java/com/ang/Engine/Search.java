package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.*;

// TODO : compare move gen results to StockFish in random positions

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
    private int     positionsSearched;
    private int     maxDepth;
    private long    endTime;

    private PVLine pvLine = new PVLine();

    public Search(int searchTime, Piece col) {
        this(searchTime, col.val());
    }
    public Search(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.engineCol = col;
        this.playerCol = Piece.WHITE.val();
    }

    public Move generateMove(BoardRecord rec) {
        long actualStartTime = System.currentTimeMillis();
        this.positionsSearched = 0;

        endTime = System.currentTimeMillis() + timeLimit;
        Move lastPlyBestMove = Move.invalid();
        maxDepth = 1;
        while (true) {
            Global.quiesces = 0;
            Global.searches = 0;
            ttHits = 0;

            PVLine line = new PVLine();

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
                    System.out.println("searched " + this.positionsSearched + " in "
                            + ((System.currentTimeMillis() - actualStartTime) / 1000L) 
                            + " s");

                    System.out.println("principal variation");
                    for (int j = 0; j < pvLine.length; j++) {
                        System.out.println(pvLine.algebraics[j]);
                        System.out.println();
                    }
                    
                    return lastPlyBestMove;
                }

                Move m = moves.at(i);
                if (m.flag == MoveFlag.ONLY_ATTACK) {
                    continue;
                }

                BoardRecord tempRec = rec.copy();
                
                if (Board.tryMove(tempRec, m)) {
                    int eval;

                    switch (Board.endState(tempRec, engineCol)) {
                    case DRAW:
                        eval = contemptFactor;
                        break;
                    case CHECKMATE:
                        eval = -Global.INFINITY;
                        break;
                    default:
                        eval = alphaBetaNega(tempRec, 
                                -Global.INFINITY, Global.INFINITY, 
                                playerCol, 0, line);
                        break;
                    }
                    
                    if (engineCol == Piece.BLACK.val()) {
                        eval = -eval;
                    }
                    if (eval > bestEval) { // always pv
                        pvLine.moves[0] = m;
                        pvLine.algebraics[0] = AlgebraicNotator.moveToAlgeb(rec, m);
                        for (int j = 0; j < line.length; j++) {
                            pvLine.moves[j + 1] = line.moves[j];
                            pvLine.algebraics[j + 1] = line.algebraics[j];
                        }
                        pvLine.length = line.length + 1;

                        bestEval = eval;
                        bestMove = m;

                        System.out.println("depth: " + maxDepth + " eval: " 
                                + eval + " from: " + m.from+" to: " + m.to); 
                        // System.out.println("king pos eval "+endgameKingPosEval(tempRec, engineCol));
                        // System.out.println("tt hits: "+ttHits);
                    }
                }
            }

            if (bestMove.isInvalid()) {
                return lastPlyBestMove;
            }
            lastPlyBestMove = bestMove;
            maxDepth++;
        }
    }

    private int alphaBetaNega(BoardRecord rec, int alpha, int beta, 
            int col, int depth, PVLine pLine) {
        
        boolean foundMove = false;
        int bestEval = -Global.INFINITY;
        PVLine line = new PVLine();

        if (depth == maxDepth) {
            pLine.length = 0; // TODO : wtf is this for in doc?
            return quiesce(rec, alpha, beta, col, depth, line);
        }

        MoveList moves  = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                foundMove = true;
                break;
            }
            Move move = moves.at(i);
            if (move.flag == MoveFlag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, move)) {
                this.positionsSearched++;
                foundMove = true;
                int eval = -alphaBetaNega(tempRec, -beta, -alpha, 
                        Piece.opposite(col).val(), depth + 1, line);
                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                        // save pv node
                        pLine.moves[0] = move;
                        pLine.algebraics[0] = AlgebraicNotator.moveToAlgeb(rec, move);
                        for (int j = 0; j < line.length; j++) {
                            pLine.moves[j + 1] = line.moves[j];
                            pLine.algebraics[j + 1] = line.algebraics[j];
                        }
                        pLine.length = line.length + 1;
                        // save to transposition table
                        TableEntry te = new TableEntry(TTFlag.PV, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    } else { // fail low (upper bound)
                        // save to transposition table
                        TableEntry te = new TableEntry(TTFlag.ALL, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    }
                }
                if (eval >= beta) { // fail high (lower bound)
                    // save to transposition table
                    TableEntry te = new TableEntry(TTFlag.CUT, move, 
                            eval, depth, 0);
                    Global.tTable.saveHash(te, tempRec, col);
                    return beta;
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
            bestEval = evaluate(rec, col); // TODO : is this where engine cant find a move??
            return bestEval; 
        }
    }

    private int quiesce(BoardRecord rec, int alpha, int beta, int col, int depth, PVLine pLine) {
        int maxDepth = 5;
        
        int standPat = evaluate(rec, col);
        int bestEval = standPat;

        if (depth > maxDepth) return standPat;

        if (standPat >= beta) return standPat;
        
        if (alpha < standPat) alpha = standPat;

        BoardRecord outerRec = rec;
        PVLine line = new PVLine();
    
        MoveList moves = Board.allMoves(rec, col);
        moves = orderMoves(rec, moves, col);
        
        for (int i = 0; i < moves.length(); i++) {
            Move move = moves.at(i);
            if (move.flag == MoveFlag.ONLY_ATTACK) continue;
            
            // heuristic cut-offs

            if ((standPat + pieceValue(rec, move.to) + 200 < alpha)
                    && (rec.minorPieceCount() > 2) && (move.flag != MoveFlag.PROMOTE)) {
                continue;
            }

            if ((move.flag != MoveFlag.PROMOTE) 
                    && (see(rec, move.to, col) != SEEFlag.WINNING)) {
                continue;
            }

            if (rec.board[move.to] == Piece.NONE.val()) continue;

            BoardRecord tempRec = rec.copy(); 
            if (Board.tryMove(tempRec, move)) { 
                this.positionsSearched++;

                int eval = -quiesce(tempRec, -beta, -alpha, 
                        Piece.opposite(col).val(), depth + 1, line);

                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                        // save pv node
                        pLine.moves[0] = move;
                        pLine.algebraics[0] = AlgebraicNotator.moveToAlgeb(rec, move);
                        for (int j = 0; j < line.length; j++) {
                            pLine.moves[j + 1] = line.moves[j];
                            pLine.algebraics[j + 1] = line.algebraics[j];
                        }
                        pLine.length = line.length + 1;
                        // save to transposition table
                        TableEntry te = new TableEntry(TTFlag.PV, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    } else { // fail low (upper bound)
                        // save to transposition table
                        TableEntry te = new TableEntry(TTFlag.ALL, move, 
                                eval, depth, 0);
                        Global.tTable.saveHash(te, tempRec, col);
                    }
                }
                if (eval >= beta) { // fail high (lower bound)
                    // save to transposition table
                    TableEntry te = new TableEntry(TTFlag.CUT, move, 
                            eval, depth, 0);
                    Global.tTable.saveHash(te, tempRec, col);
                    outerRec = tempRec;
                    return beta; // TODO : test
                }
            }   
        }

        switch (Board.endState(outerRec, col)) {
        case CHECKMATE:
            int mateEval = (col == Piece.WHITE.val())
            ? -Global.INFINITY + depth * 100
            : Global.INFINITY - depth * 100;
            return mateEval;
        case DRAW:
            return (col == Piece.WHITE.val())
                ? contemptFactor
                : -contemptFactor;
        default:
            return bestEval;
        }
    }

    private int pieceValue(BoardRecord rec, int pos) {
        int value = 0;
        int pieceCol = rec.board[pos] & 0b11000;
        int heatmapIndex = (pieceCol == Piece.WHITE.val()) ? pos : 63 - pos;

        switch (rec.board[pos] & 0b111) {
        case 1:
            value = Piece.PAWN.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;
        case 2:
            value = Piece.KNIGHT.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;
        case 3:
            value = Piece.BISHOP.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;
        case 4:
            value = Piece.ROOK.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;
        case 5:
            value = Piece.QUEEN.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;
        case 6:
            // king heatmap changes in endgame
            int[] heatmap = (rec.minorPieceCount() < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = Piece.KING.staticEval() + heatmap[heatmapIndex];
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

    public SEEFlag see(BoardRecord rec, int pos, int currentCol) {
        if ((rec.board[pos] & 0b11000) == currentCol) return SEEFlag.EQUAL;

        IntList exclude = new IntList(10, -1);

        int eval = 0;
        eval += Piece.staticEval(rec.board[pos]);

        while (true) {
            int friendlyPos = Board.getLightestAttacker(rec, pos, currentCol, exclude);
            int enemyPos = Board.getLightestAttacker(rec, pos, Piece.opposite(currentCol).val(), exclude);
    
            if (friendlyPos == -1) {
                if (enemyPos == -1) return SEEFlag.EQUAL;
                else return SEEFlag.LOSING;
            } else {
                if (enemyPos == -1) return SEEFlag.WINNING;
            }

            eval -= Piece.staticEval(rec.board[friendlyPos] & 0b11000);
            eval += Piece.staticEval(rec.board[enemyPos] & 0b11000);

            exclude.add(friendlyPos);
            exclude.add(enemyPos);

            if (eval > 0) return SEEFlag.WINNING;
            if (eval < 0) return SEEFlag.LOSING;
        }
    }

    private int pieceValueEval(BoardRecord rec) {
        int eval = 0;
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            eval += pieceValue(rec, pos);
        }
        return eval;
    }

    private int evaluate(BoardRecord rec, int currentCol) {        
        // probe transposition table
        int boardHash = Global.tTable.zobristHash(rec, currentCol);
        TableEntry te = Global.tTable.searchTable(boardHash);
        if ((te != null) && (te.nodeType == TTFlag.PV)) {
            return te.eval;
        }
        
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