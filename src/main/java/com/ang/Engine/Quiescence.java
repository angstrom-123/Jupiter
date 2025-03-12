package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Core.Moves.Move;
import com.ang.Core.Moves.MoveFlag;
import com.ang.Core.Moves.MoveList;

public class Quiescence {
    private final static int maxDepth = 4;

    public static int quiesce(BoardRecord rec, int alpha, int beta, int col, 
            int abDepth, int depth, long endTime) {
        int standPat = Evaluation.evaluate(rec, col);
        int bestEval = standPat;

        if (depth == maxDepth) return standPat;

        if (standPat >= beta) return standPat;

        if (alpha < standPat) alpha = standPat;

        MoveList moves = Board.allMoves(rec, col);
        moves = Search2.orderMoves(rec, col, moves);

        for (int i = 0; i < moves.length(); i++) {
            if (System.currentTimeMillis() >= endTime) return -Global.INFINITY;

            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) continue;

            // heuristic cut-offs
            if ((standPat + Evaluation.pieceValue(rec, m.to) + 200 < alpha)
                    && (rec.minorPieceCount > 2) && (m.flag != MoveFlag.PROMOTE)) {
                continue;

            }
            if ((m.flag != MoveFlag.PROMOTE) 
                    && (Evaluation.see(rec, m.to, col) != SEEFlag.WINNING)) {
                continue;

            }
            if (rec.board[m.to] == Piece.NONE.val()) continue;

            BoardRecord tempRec = rec.copy();
            if (!Board.tryMove(rec, m)) continue;

            int eval = -quiesce(tempRec, -beta, -alpha, Piece.opposite(col).val(), 
                    abDepth, depth + 1, endTime);
            if (eval > bestEval) bestEval = eval;
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
