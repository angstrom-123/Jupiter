package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.Move;
import com.ang.Core.Moves.MoveFlag;
import com.ang.Core.Moves.MoveList;

/**
 * Class for a recursive search using alpha beta pruning
 */
public class Search {
    /**
     * Starts a search in a given position
     * @param rec the position to search in
     * @param alpha the lower bound of evaluation to accept
     * @param beta the upper bound of evaluation to accept
     * @param col the colour to search for
     * @param maxDepth the depth to search to
     * @param altMoveOrdering use standard or random move ordering
     * @param endTime the time to end the search (System.currentTimeMillis() + searchTime)
     * @return SearchResult containing best move, evaluation, and depth
     */
    public static SearchResult search(BoardRecord rec, int alpha, int beta, int col, 
            int maxDepth, boolean altMoveOrdering, long endTime) {
        int bestEval = -Global.INFINITY;
        Move bestMove = Move.invalid();
        MoveList moves = Board.allMoves(rec, col);
        if (!altMoveOrdering) {
            moves = orderMoves(rec, col, moves);
        } else {
            moves.randomize();
        }
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                return new SearchResult(Move.invalid(), -Global.INFINITY, 0);

            }

            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) {
                continue;

            }
            BoardRecord tempRec = rec.copy();
            if (!Board.tryMove(tempRec, m)) {
                continue;

            }
            int eval = evalSearch(tempRec, -beta, -alpha, Piece.opposite(col).val(), 
                    0, maxDepth, altMoveOrdering, endTime);
            if (col == Piece.BLACK.val()) {
                eval = -eval;
            }
            if (eval > bestEval) {
                bestEval = eval;
                bestMove = m;
            }
        }
        // if the move is invalid, attempts to re-search with a null window, if
        // the result is still invalid after this, the engine cannot find a move
        if (bestMove.isInvalid()) {
            if ((Math.abs(alpha) == Global.INFINITY) || (Math.abs(beta) == Global.INFINITY)) {
                System.out.println("No move with null window");
                return new SearchResult(Move.invalid(), -Global.INFINITY, 0);

            }
            System.out.println("Null window");
            bestMove = search(rec, -Global.INFINITY, Global.INFINITY, col, maxDepth, 
                    altMoveOrdering, endTime).move;
            if (bestMove.isInvalid()) {
                System.out.println("Engine could not find valid move");
            }
        }
        System.out.println("Search to depth " + maxDepth + ": ");
        System.out.println("    - Move: " + bestMove.from + " to " + bestMove.to);
        System.out.println("    - Eval: " + bestEval);
        return new SearchResult(bestMove, bestEval, maxDepth);

    }

    /**
     * Main search down a path started in search()
     * @param rec the position to search in
     * @param alpha the lower bound for evaluation to accept
     * @param beta the upper bound for evaluation to accept
     * @param col the colour to search with
     * @param depth the current depth of the search
     * @param maxDepth the depth to search to
     * @param altMoveOrdering use standard or random move ordering
     * @param endTime the time to end search (System.currentTimeMillis() + searchTime)
     * @return the evaluation of the position found down this path
     */
    private static int evalSearch(BoardRecord rec, int alpha, int beta, int col, 
            int depth, int maxDepth, boolean altMoveOrdering, long endTime) {
        if (depth == maxDepth) {
            return Quiescence.quiesce(rec, alpha, beta, col, maxDepth, 0, endTime);

        }
        int bestEval = -Global.INFINITY;
        MoveList moves = Board.allMoves(rec, col);
        if (altMoveOrdering) {
            moves.randomize();
        } else {
            moves = orderMoves(rec, col, moves);
        }
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                return -Global.INFINITY;

            }
            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) {
                continue;

            }
            BoardRecord tempRec = rec.copy();
            if (!Board.tryMove(tempRec, m)) {
                continue;

            }
            int eval = -evalSearch(tempRec, -beta, -alpha, Piece.opposite(col).val(), 
                    depth + 1, maxDepth, altMoveOrdering, endTime);
            if (eval > bestEval) {
                bestEval = eval;
                if (eval > alpha) {
                    alpha = eval;
                    // pv
                    TableEntry te = new TableEntry(TTFlag.PV, m, eval, depth, 0);
                    Global.tTable.saveHash(te, rec, col);
                } else {
                    // all
                    TableEntry te = new TableEntry(TTFlag.ALL, m, eval, depth, 0);
                    Global.tTable.saveHash(te, rec, col);
                }
            }
            if (eval >= beta) {
                // cut
                TableEntry te = new TableEntry(TTFlag.CUT, m, eval, depth, 0);
                Global.tTable.saveHash(te, rec, col);
                return beta;

            }
        }
        return bestEval;
        
    }

    public static MoveList orderMoves(BoardRecord rec, int col, MoveList moves) {
        moves.attacksToFront();
        TableEntry te = Global.tTable.searchTable(Global.tTable.zobristHash(rec, col));
        if (te != null) {
            moves.sendToFront(te.bestMove);
        }
        return moves;

    }
}
