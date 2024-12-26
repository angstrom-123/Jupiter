package com.ang.Core.Moves;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;

public class PieceMover {
    public static MoveList moves(BoardRecord rec, int from) {
        switch (rec.board[from] & 0b111) {
        case 1:
            return pawnMoves(rec, from);
        case 2:
            return knightMoves(rec, from);
        case 3:
            return bishopMoves(rec, from);
        case 4:
            return rookMoves(rec, from);
        case 5:
            return queenMoves(rec, from);
        case 6:
            return kingMoves(rec, from);
        default:
            return new MoveList(0);
        }
    }

    private static MoveList pawnMoves(BoardRecord rec, int from) {
        MoveList moves = new MoveList(6);
        int offset;
        int dir = (rec.board[from] & 0b11000) == Piece.WHITE.val() ? 1 : -1;
        
        offset = -8 * dir;
        if (Board.inBounds(from, offset)) {
            // single push
            if (rec.board[from + offset] == Piece.NONE.val()) { 
                if ((from + offset < 8) || (from + offset > 55)) {
                    moves.add(new Move(from, from + offset, 
                              Flag.PROMOTE, false));
                } else {
                    moves.add(new Move(from, from + offset, false)); 
                }
            }
            // double push
            offset = -16 * dir;
            if (((Math.floor(from / 8) == 1) && (dir == -1)) // black
                    || ((Math.floor(from / 8) == 6) && (dir == 1))) { // white
                if (rec.board[from + offset] == Piece.NONE.val()) {
                    moves.add(new Move(from, from + offset, Flag.DOUBLE_PUSH, false)); 
                }
            } 
        }
        
        int[] offsets = new int[]{(-8 * dir) - 1, (-8 * dir) + 1};
        for (int off : offsets) {
            if (!Board.inBounds(from, off)) {
                continue;
            }
            if ((rec.board[from + off] & 0b11000) 
                    == Piece.opposite(rec.board[from] & 0b11000).val()) {
                // take
                if ((from + off < 8) || (from + off > 55)) {
                    moves.add(new Move(from, from + off, Flag.PROMOTE));
                } else {
                    moves.add(new Move(from, from + off)); 
                }
            } else if (rec.board[from + off] == Piece.NONE.val()) {
                // en passant
                if (rec.epPawnPos == from + (off - (-8 * dir))) {
                    moves.add(new Move(from, from + off, Flag.EN_PASSANT)); 
                } else {
                    moves.add(new Move(from, from + off, Flag.ONLY_ATTACK));
                }
            } else { 
                // friendly piece at attack square - need to add attack manually
                moves.add(new Move(from, from + off, Flag.ONLY_ATTACK));
            }
        }
        return moves;
    }

    private static MoveList knightMoves(BoardRecord rec, int from) {
        MoveList moves  = new MoveList(8);
        int col         = rec.board[from] & 0b11000;
        int[] offsets   = new int[]{-17, -15, -10, -6, 6, 10, 15, 17};

        for (int move : offsets) {
            if (Board.inBounds(from, move)) {
                if ((rec.board[from + move] & 0b11000) != col) {
                    moves.add(new Move(from, from + move));
                } else {
                    moves.add(new Move(from, from + move, Flag.ONLY_ATTACK));
                }
            }
        }
        
        return moves;
    }

    private static MoveList bishopMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, false, true);
    }

    private static MoveList rookMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, true, false);
    }

    private static MoveList queenMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, true, true);
    }

    private static MoveList kingMoves(BoardRecord rec, int from) {
        MoveList moves = new MoveList(10);
        int col = rec.board[from] & 0b11000;
        int[] offsets = new int[]{-9, -8, -7, -1, 1, 7, 8, 9};

        for (int move : offsets) {
            if (Board.inBounds(from, move)) {
                if (!Board.isUnderAttack(rec, from + move, col) 
                        && ((rec.board[from + move] & 0b11000) != col)) {
                    moves.add(new Move(from, from + move));
                } else {
                    moves.add(new Move(from, from + move, Flag.ONLY_ATTACK));
                }
            }
        }

        int canShort = (col == Piece.WHITE.val()) 
        ? rec.cRights[0]
        : rec.cRights[2];
        if (canShort == 1) {
            moves.add(new Move(from, from + 2, Flag.CASTLE_SHORT, false));
        }

        int canLong = (col == Piece.WHITE.val()) 
        ? rec.cRights[1]
        : rec.cRights[3];
        if (canLong == 1) {
            moves.add(new Move(from, from - 2, Flag.CASTLE_LONG, false));
        }

        return moves;
    }

    private static MoveList slidingPieceMoves(BoardRecord rec, int from, 
            boolean orthogonal, boolean diagonal) {
        if (!orthogonal && !diagonal) { 
            System.err.println("Sliding piece must have a direction");
            return new MoveList(0); 
        }

        MoveList moves  = new MoveList(27);
        int[] offsets   = new int[]{-8, -1, 1, 8, -9, -7, 7, 9};
        int col         = rec.board[from] & 0b11000;
        int start       = (orthogonal)  ?  0 : 4;
        int end         = (diagonal)    ?  8 : 4;

        for (int i = start; i < end; i++) {
            int step = 1;
            int stepPos = from;
            while (step < 8) {
                if (!Board.inBounds(stepPos, offsets[i])) {
                    break;
                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == col) {
                    moves.add(new Move(from, stepPos + offsets[i], 
                              Flag.ONLY_ATTACK));
                    break;
                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == Piece.opposite(col).val()) {
                    moves.add(new Move(from, stepPos + offsets[i]));
                    break;
                }
                
                moves.add(new Move(from, stepPos + offsets[i]));
                stepPos += offsets[i];
                step++;
            }
        }

        return moves;
    }
}
