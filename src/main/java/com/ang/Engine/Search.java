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

/**
 * Class for the search function of the engine
 */
public class Search {  
    public int      engineCol;

    private int     contemptFactor = 0;
    private int     timeLimit;
    private int     playerCol;
    private int     positionsSearched;
    private int     maxDepth;
    private long    endTime;

    private PVLine pvLine = new PVLine();

    /**
     * Constructs search
     * @param searchTime max time to search for a move
     * @param col colour that the engine plays as
     */
    public Search(int searchTime, Piece col) {
        this(searchTime, col.val());
    }
    public Search(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.engineCol = col;
        this.playerCol = Piece.WHITE.val();
    }

    /**
     * Attempts to fund the best move in a given position
     * @param rec BoardRecord representing the position
     * @return the best move that the engine finds
     */
    public Move generateMove(BoardRecord rec) {
        long actualStartTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis() + timeLimit;

        this.positionsSearched = 0;

        Move lastPlyBestMove = Move.invalid();
        maxDepth = 1;
        while (true) {
            PVLine line = new PVLine();

            int bestEval = -Global.INFINITY;

            Move bestMove = Move.invalid();         
            MoveList moves = Board.allMoves(rec, engineCol);
            moves = orderMoves(rec, moves, engineCol);
            for (int i = 0; i < moves.length(); i++) {
                if ((System.currentTimeMillis() >= endTime)
                        && (maxDepth > 2)) {
                    System.out.println("maximum complete depth: " 
                            + (maxDepth - 1));
                    System.out.println("final move from: " 
                            + lastPlyBestMove.from + " to: " + lastPlyBestMove.to 
                            + " with eval " + bestEval);
                    System.out.println("white " + Evaluation.evaluate(rec, Piece.WHITE.val())
                            + " black " + Evaluation.evaluate(rec, Piece.BLACK.val()));
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

    /**
     * NegaMax tree search using Alpha/Beta Pruning
     * @param rec BoardRecord representing the position to search from
     * @param alpha evaluation lower bound
     * @param beta evaluation upper bound
     * @param col colour to move in this position
     * @param depth current search depth
     * @param pLine principal line that is being explored
     * @return
     */
    private int alphaBetaNega(BoardRecord rec, int alpha, int beta, 
            int col, int depth, PVLine pLine) {
        
        boolean foundMove = false;
        int bestEval = -Global.INFINITY;
        PVLine line = new PVLine();

        if (depth == maxDepth) {
            pLine.length = 0;
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
            if (foundMove) {
                return bestEval;
            }
            // return evaluate(rec, col);
            mateEval = (col == Piece.WHITE.val())
            ? -Global.INFINITY + depth * 100
            : Global.INFINITY - depth * 100;   
            return mateEval;
        }
    }

    /**
     * Quiescence search ran after main search - attempts to only evaluate positions
     * where there are no winning moves (quiet / quiescent positions) to minimize
     * horizon effect (from searching too few nodes).
     * @param rec BoardRecord representing the position to search from
     * @param alpha evaluation lower bound
     * @param beta evaluation upper bound
     * @param col colour to move in this position
     * @param depth current quiescence depth
     * @param pLine principal line that is being explored
     * @return the evaluation of the position that is considered quiescent
     */
    private int quiesce(BoardRecord rec, int alpha, int beta, int col, int depth, PVLine pLine) {
        int maxDepth = 5;
        
        int standPat = Evaluation.evaluate(rec, col);
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

            if ((standPat + Evaluation.pieceValue(rec, move.to) + 200 < alpha)
                    && (rec.minorPieceCount > 2) && (move.flag != MoveFlag.PROMOTE)) {
                continue;
            }

            if ((move.flag != MoveFlag.PROMOTE) 
                    && (Evaluation.see(rec, move.to, col) != SEEFlag.WINNING)) {
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
                    return beta;
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

    /**
     * Changes the order in which moves are searched based on the results of 
     * previous searches that were recorder in the transposition table
     * @param rec BoardRecord representing the position where a move is being
     * generated
     * @param moves the MoveList that should be reordered
     * @param moveCol the colour who's turn it is to move
     * @return the new reordered MoveList
     */
    private MoveList orderMoves(BoardRecord rec, MoveList moves, int moveCol) {
        moves.attacksToFront();

        TableEntry te = Global.tTable.searchTable(
                Global.tTable.zobristHash(rec, moveCol));
        if (te != null) { // tt hit
            moves.sendToFront(te.bestMove);
        }
        return moves;
    }
}