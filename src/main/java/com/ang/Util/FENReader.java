package com.ang.Util;

import com.ang.Piece;

public class FENReader {
    public static int[] readFEN(String fen) {
        int[] board = new int[64];

        int index = 0;

        char[] chars = fen.toCharArray();
        for (char c : chars) {
            switch (c) {
            case '1':
                break;
            case '2':
                index += 1;
                break;
            case '3':
                index += 2;
                break;
            case '4':
                index += 3;
                break;
            case '5':
                index += 4;
                break;
            case '6':
                index += 5;
                break;
            case '7':
                index += 6;
                break;
            case '8':
                index += 7;
                break;
            case 'p':
                board[index] = Piece.PAWN.val() | Piece.BLACK.val();
                break;
            case 'P':
                board[index] = Piece.PAWN.val() | Piece.WHITE.val();
                break;
            case 'n':
                board[index] = Piece.KNIGHT.val() | Piece.BLACK.val();
                break;
            case 'N':
                board[index] = Piece.KNIGHT.val() | Piece.WHITE.val();
                break;    
            case 'b':
                board[index] = Piece.BISHOP.val() | Piece.BLACK.val();
                break;
            case 'B':
                board[index] = Piece.BISHOP.val() | Piece.WHITE.val();
                break;
            case 'r':
                board[index] = Piece.ROOK.val() | Piece.BLACK.val();
                break;
            case 'R':
                board[index] = Piece.ROOK.val() | Piece.WHITE.val();
                break;
            case 'q':
                board[index] = Piece.QUEEN.val() | Piece.BLACK.val();
                break;
            case 'Q':
                board[index] = Piece.QUEEN.val() | Piece.WHITE.val();
                break;
            case 'k':
                board[index] = Piece.KING.val() | Piece.BLACK.val();
                break;
            case 'K':
                board[index] = Piece.KING.val() | Piece.WHITE.val();
                break;
            default:
                break;
            }
            if (c != '/') {
                index++;
            }
        }

        return board;
    }
}
