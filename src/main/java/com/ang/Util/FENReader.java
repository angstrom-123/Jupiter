package com.ang.Util;

import com.ang.Core.Piece;

/**
 * Class for reading FEN strings
 */
public class FENReader {
    /**
     * Reads a Forsyth-Edwards Notation (FEN) string into a chess position
     * @param fen the string of FEN to be read
     * @return int[64] representing the chess board
     */
    public static int[] readFEN(String fen) {
        int[] board = new int[64];
        int end = 0;

        final char[] chars = fen.toCharArray();
        for (char c : chars) {
            switch (c) {
            case '1':
                break;
            case '2':
                end += 1;
                break;
            case '3':
                end += 2;
                break;
            case '4':
                end += 3;
                break;
            case '5':
                end += 4;
                break;
            case '6':
                end += 5;
                break;
            case '7':
                end += 6;
                break;
            case '8':
                end += 7;
                break;
            case 'p':
                board[end] = Piece.PAWN.val() | Piece.BLACK.val();
                break;
            case 'P':
                board[end] = Piece.PAWN.val() | Piece.WHITE.val();
                break;
            case 'n':
                board[end] = Piece.KNIGHT.val() | Piece.BLACK.val();
                break;
            case 'N':
                board[end] = Piece.KNIGHT.val() | Piece.WHITE.val();
                break;    
            case 'b':
                board[end] = Piece.BISHOP.val() | Piece.BLACK.val();
                break;
            case 'B':
                board[end] = Piece.BISHOP.val() | Piece.WHITE.val();
                break;
            case 'r':
                board[end] = Piece.ROOK.val() | Piece.BLACK.val();
                break;
            case 'R':
                board[end] = Piece.ROOK.val() | Piece.WHITE.val();
                break;
            case 'q':
                board[end] = Piece.QUEEN.val() | Piece.BLACK.val();
                break;
            case 'Q':
                board[end] = Piece.QUEEN.val() | Piece.WHITE.val();
                break;
            case 'k':
                board[end] = Piece.KING.val() | Piece.BLACK.val();
                break;
            case 'K':
                board[end] = Piece.KING.val() | Piece.WHITE.val();
                break;
            default:
                break;
            }
            if (c != '/') {
                end++;
            }
        }

        return board;
    }
}
