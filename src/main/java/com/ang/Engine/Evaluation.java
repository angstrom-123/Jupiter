package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;

/**
 * Class for estimating how good a given position is
 */
public class Evaluation {
    /**
     * Calculates the value of a piece on the board in a specific position
     * @param rec BoardRecord representing the position to be evaluated
     * @param pos index into @param rec where the piece to evaluate is
     * @return the evaluation for the relevant piece
     */
    public static int pieceValue(BoardRecord rec, int pos) {
        int value = 0;
        int pieceCol = rec.board[pos] & 0b11000;
        int heatmapIndex = (pieceCol == Piece.WHITE.val()) ? pos : 63 - pos;
        switch (rec.board[pos] & 0b111) {
        case 1:
            value = Piece.PAWN.staticEval() + Heatmap.pawnMap[heatmapIndex];
            break;

        case 2:
            value = Piece.KNIGHT.staticEval() + Heatmap.knightMap[heatmapIndex];
            break;

        case 3:
            value = Piece.BISHOP.staticEval() + Heatmap.bishopMap[heatmapIndex];
            break;

        case 4:
            value = Piece.ROOK.staticEval() + Heatmap.rookMap[heatmapIndex];
            break;

        case 5:
            value = Piece.QUEEN.staticEval() + Heatmap.queenMap[heatmapIndex];
            break;

        case 6:
            // king heatmap changes in endgame
            int[] heatmap = (rec.minorPieceCount < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = Piece.KING.staticEval() + heatmap[heatmapIndex];
            break;

        default:
            value = 0;
            break;

        }
        return (pieceCol == Piece.WHITE.val()) ? value : - value;

    }

    /**
     * Endgame evaluation that aids in pushing the opponents king to the edge
     * of the board where mate is easier to find (due high required
     * search depth to find a forcing endgame line)
     * @param rec BoardRecord representing the position to be evaluated
     * @param currentCol colour from who's perspective to evaluate from
     * @return the mopup evaluation for the position
     */
    public static int mopupEvaluation(BoardRecord rec, int currentCol) {
        int eval                = 0;
        int friendlyKingPos     = Board.findKing(rec, currentCol);
        int enemyKingPos        = Board.findKing(rec, Piece.opposite(currentCol));   
        int friendlyKingRank    = (int) Math.floor(friendlyKingPos / 8);
        int friendlyKingFile    = friendlyKingPos % 8;
        int enemyKingRank       = (int) Math.floor(enemyKingPos / 8);
        int enemyKingFile       = enemyKingPos % 8;
        // push enemy king to edge
        int enemyCentreDistRank = Math.max(3 - enemyKingRank, enemyKingRank - 4);
        int enemyCentreDistFile = Math.max(3 - enemyKingFile, enemyKingFile - 4);
        eval += (enemyCentreDistRank + enemyCentreDistFile);
        // bring friendly king closer to enemy king
        int kingSeperationRank = Math.abs(friendlyKingRank - enemyKingRank);
        int kingSeperationFile = Math.abs(friendlyKingFile - enemyKingFile);
        int kingSeperationSquared = (kingSeperationRank * kingSeperationRank) 
                + (kingSeperationFile * kingSeperationFile);
        eval += (128 - kingSeperationSquared);
        // distance rooks from enemy king
        for (int pos : BitBoard.setBits(rec.rooks)) {
            if (pos == -1) {
                break;

            }
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

    /**
     * Static exchange evaluation, calculates if an exchange is good or bad or equal
     * based on the static values of involved pieces from each side
     * @param rec BoardRecord representing the position to be evaluated
     * @param pos index into @param rec where the exchange should be
     * @param currentCol the colour to move first in the position
     * @return a flag representing the outcome (WINNING, EQUAL or LOSING)
     */
    public static SEEFlag see(BoardRecord rec, int pos, int currentCol) {
        if ((rec.board[pos] & 0b11000) == currentCol) {
            return SEEFlag.EQUAL;

        }
        IntList exclude = new IntList(10, -1);
        int eval = Piece.staticEval(rec.board[pos]);
        while (true) {
            int friendlyPos = Board.getLightestAttacker(rec, pos, currentCol, exclude);
            int enemyPos = Board.getLightestAttacker(rec, pos, Piece.opposite(currentCol).val(), exclude);
            if (friendlyPos == -1) {
                return (enemyPos == -1) ? SEEFlag.EQUAL : SEEFlag.LOSING;
            } else if (enemyPos == -1) {
                return SEEFlag.WINNING;

            }
            eval -= Piece.staticEval(rec.board[friendlyPos] & 0b11000);
            eval += Piece.staticEval(rec.board[enemyPos] & 0b11000);
            exclude.add(friendlyPos);
            exclude.add(enemyPos);
            if (eval > 0) {
                return SEEFlag.WINNING;

            }
            if (eval < 0) {
                return SEEFlag.LOSING;

            }
        }
    }

    /**
     * Calculates the values for each piece on the board
     * @param rec BoardRecord representing the position to be evaluated
     * @return the evaluation for the pieces
     */
    public static int pieceValueEval(BoardRecord rec) {
        int eval = 0;
        for (int pos : BitBoard.setBits(rec.allPieces)) {
            if (pos == -1) {
                break;

            }
            eval += pieceValue(rec, pos);
        }
        return eval;

    }

    /**
     * Calculates the overall evaluation of a position
     * @param rec BoardRecord representing the position to be evaluated
     * @param currentCol the colour to move in the position
     * @return the evaluation of the position
     */
    public static int evaluate(BoardRecord rec, int currentCol) {        
        // probe transposition table first
        int boardHash = Global.tTable.zobristHash(rec, currentCol);
        TableEntry te = Global.tTable.searchTable(boardHash);
        if ((te != null) && ((te.nodeType == TTFlag.PV) || (te.nodeType == TTFlag.CUT))) {
            return te.eval;

        }
        // main evaluation if no tt hit
        int eval = (currentCol == Piece.WHITE.val()) 
        ? pieceValueEval(rec) 
        : -pieceValueEval(rec);
        if (rec.minorPieceCount < 3) {
            eval += mopupEvaluation(rec, currentCol);
        }
        return eval;

    }
}
