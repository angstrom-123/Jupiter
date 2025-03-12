package com.ang.Util;

import com.ang.Core.*;
import com.ang.Core.Moves.*;

/**
 * Class for converting a move from internal representation to algebraic notation
 */
public class AlgebraicNotator {
    /**
     * Converts a move to algebraic notation
     * @param rec BoardRecord representing the position where the move was made
     * @param move the move to convert
     * @return algebraic notation for the specified move
     */
    public static String moveToAlgeb(BoardRecord rec, Move move) {
        String out = "";
        int moving = rec.board[move.from];
        int taken = rec.board[move.to];
        if ((moving & 0b111) == Piece.PAWN.val()) {
            if (taken == Piece.NONE.val()) {
                out += indexToCoords(move.to);
            } else {
                char movingFile = indexToCoords(move.from).toCharArray()[0];
                out += (char) movingFile + 'x' + indexToCoords(move.to);
            }
        } else {
            out += intToPiece(moving);
            if (taken != Piece.NONE.val()) {
                out += 'x';
            }
            out += indexToCoords(move.to);
        }
        return out;

    }

    /**
     * Converts an index into algebraic notation coordinates
     * @param index the index into a boardRecord's board[] to convert
     * @return the algebraic notation coordinates of the index
     */
    private static String indexToCoords(int index) {
        String out = "";
        int x = index % 8;
        int y = (int) Math.floor(index / 8);
        char[] ranks = new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};
        char[] files = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        return out + files[x] + ranks[7 - y];

    }

    /**
     * Converts integer representation of a piece into algebraic notation
     * @param piece int value of piece to convert
     * @return alebraic notation symbol for the piece or '.' if not found
     */
    private static char intToPiece(int piece) {
        boolean isBlack = (piece & 0b11000) == Piece.BLACK.val();
        switch (piece & 0b111) {
        case 1:
            return isBlack ? 'p' : 'P'; 

        case 2:
            return isBlack ? 'n' : 'N'; 

        case 3:
            return isBlack ? 'b' : 'B'; 

        case 4:
            return isBlack ? 'r' : 'R'; 

        case 5:
            return isBlack ? 'q' : 'Q'; 

        case 6:
            return isBlack ? 'k' : 'K'; 

        default:
            return '.';
            
        }
    }
}   
