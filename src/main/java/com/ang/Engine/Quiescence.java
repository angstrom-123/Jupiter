package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.*;
import com.ang.Engine.Eval.*;
import com.ang.Engine.Transposition.*;

/**
 * Class for conducting a quiescence search (a search where heuristic cut-offs
 * reduce the amount of positions that need to be searched until, ideally, the 
 * final evaluated position has no winning tactical moves). This helps to avoid
 * the horizon effect of the main search
 */
public class Quiescence {
    private final static int maxDepth = 4; // limiting to speed up search

    /**
     * Runs the quiescence search
     * @param rec the position to quiesce in
     * @param alpha the lower bound for evaluation to accept
     * @param beta the upper bound for evaluation to accept
     * @param col the colour to search with
     * @param abDepth the depth reached with the previous (alpha/beta) search
     * @param depth the current depth of quiescence
     * @param endTime the time to end the search (System.currentTimeMillis() + searchTime)
     * @return
     */
    public static int quiesce(BoardRecord rec, int alpha, int beta, int col, 
            int abDepth, int depth, long endTime) {
        int standPat = Evaluation.evaluate(rec, col);
        int bestEval = standPat;
        if (depth == maxDepth) {
            return standPat;

        }
        if (standPat >= beta) {
            return standPat;

        }
        if (alpha < standPat) {
            alpha = standPat;

        }
        MoveList moves = Board.allMoves(rec, col);
        moves = Search.orderMoves(rec, col, moves);
        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) {
                return -Global.INFINITY;

            }
            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) {
                continue;

            }
            // heuristic cut-offs
            if ((standPat + Evaluation.pieceValue(rec, m.to) + 200 < alpha)
                    && (rec.minorPieceCount > 2) && (m.flag != MoveFlag.PROMOTE)) {
                continue;

            }
            if ((m.flag != MoveFlag.PROMOTE) 
                    && (Evaluation.see(rec, m.to, col) != SEEFlag.WINNING)) {
                continue;

            }
            if (rec.board[m.to] == Piece.NONE.val()) {
                continue;

            }
            BoardRecord tempRec = rec.copy();
            if (!Board.tryMove(rec, m)) {
                continue;

            }
            int eval = -quiesce(tempRec, -beta, -alpha, Piece.opposite(col).val(), 
                    abDepth, depth + 1, endTime);
            if (eval > bestEval) {
                bestEval = eval;

            }
            if (eval > alpha) {
                alpha = eval;
                // pv
                TableEntry te = new TableEntry(TTFlag.PV, m, eval, abDepth + depth, 0);
                Global.tTable.saveHash(te, rec, col);
            } else {
                // all
                TableEntry te = new TableEntry(TTFlag.ALL, m, eval, abDepth + depth, 0);
                Global.tTable.saveHash(te, rec, col);
            }
            if (eval >= beta) {
                // cut
                TableEntry te = new TableEntry(TTFlag.CUT, m, eval, abDepth + depth, 0);
                Global.tTable.saveHash(te, rec, col);
                return beta;

            }
        }
        return bestEval;

    }
}
