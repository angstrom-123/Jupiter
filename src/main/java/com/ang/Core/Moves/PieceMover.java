package com.ang.Core.Moves;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;

/**
 * Class that handles finding possible moves for pieces
 */
public class PieceMover {
    /**
     * Finds the possible moves for a piece
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the moving piece
     * @return list of possible moves for the piece at @param from
     */
    public static MoveList moves(BoardRecord rec, int from) {    
        if (from == -1) {
            return new MoveList(0);

        }
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

    /**
     * Calculates possible moves for a pawn
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the pawn
     * @return list of possible moves for the pawn at @param from
     */
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
                            MoveFlag.PROMOTE, false));
                } else {
                    moves.add(new Move(from, from + offset, false)); 
                }
                // double push
                offset = -16 * dir;
                if (((Math.floor(from / 8) == 1) && (dir == -1)) // black
                        || ((Math.floor(from / 8) == 6) && (dir == 1))) { // white
                    if (rec.board[from + offset] == Piece.NONE.val()) {
                        moves.add(new Move(from, from + offset, MoveFlag.DOUBLE_PUSH, false)); 
                    }
                } 
            }
        }
        int[] offsets = new int[]{(-8 * dir) - 1, (-8 * dir) + 1};
        for (int off : offsets) {
            if (!Board.inBounds(from, off)) {
                continue;

            }
            if ((rec.board[from + off] & 0b11000) == Piece.opposite(rec.board[from] & 0b11000).val()) {
                // take
                if ((from + off < 8) || (from + off > 55)) {
                    moves.add(new Move(from, from + off, MoveFlag.PROMOTE));
                } else {
                    moves.add(new Move(from, from + off)); 
                }
            } else if (rec.board[from + off] == Piece.NONE.val()) {
                // en passant
                if (rec.epPawnPos == from + (off - (-8 * dir))) {
                    moves.add(new Move(from, from + off, MoveFlag.EN_PASSANT)); 
                } else {
                    moves.add(new Move(from, from + off, MoveFlag.ONLY_ATTACK));
                }
            } else { 
                // friendly piece at attack square - need to add attack manually
                moves.add(new Move(from, from + off, MoveFlag.ONLY_ATTACK));
            }
        }
        return moves;

    }

    /**
     * Calculates possible moves for a knight
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the knight
     * @return list of possible moves for the knight at @param from
     */
    private static MoveList knightMoves(BoardRecord rec, int from) {
        MoveList moves  = new MoveList(8);
        int col         = rec.board[from] & 0b11000;
        int[] offsets   = new int[]{-17, -15, -10, -6, 6, 10, 15, 17};
        for (int move : offsets) {
            if (Board.inBounds(from, move)) {
                if ((rec.board[from + move] & 0b11000) != col) {
                    moves.add(new Move(from, from + move));
                } else {
                    moves.add(new Move(from, from + move, MoveFlag.ONLY_ATTACK));
                }
            }
        }
        return moves;

    }

    /**
     * Calculates possible moves for a bishop
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the bishop
     * @return list of possible moves for the bishop at @param from
     */
    private static MoveList bishopMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, false, true);

    }

    /**
     * Calculates possible moves for a rook
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the rook
     * @return list of possible moves for the rook at @param from
     */
    private static MoveList rookMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, true, false);

    }

    /**
     * Calculates possible moves for a queen
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the queen
     * @return list of possible moves for the queen at @param from
     */
    private static MoveList queenMoves(BoardRecord rec, int from) {
        return slidingPieceMoves(rec, from, true, true);

    }

    /**
     * Calculates possible moves for a king
     * @param rec BoardRecord representing a position where moves should be found
     * @param from index into @param rec board[] of the king
     * @return list of possible moves for the king at @param from
     */
    private static MoveList kingMoves(BoardRecord rec, int from) {
        MoveList moves = new MoveList(10);
        int col = rec.board[from] & 0b11000;
        int[] offsets = new int[]{-9, -8, -7, -1, 1, 7, 8, 9};
        for (int move : offsets) {
            if (Board.inBounds(from, move)) {
                if (!Board.underAttack(rec, from + move, col) 
                        && ((rec.board[from + move] & 0b11000) != col)) {
                    moves.add(new Move(from, from + move));
                } else {
                    moves.add(new Move(from, from + move, MoveFlag.ONLY_ATTACK));
                }
            }
        }
        int canShort = (col == Piece.WHITE.val()) 
        ? rec.cRights[0]
        : rec.cRights[2];
        if (canShort == 1) {
            moves.add(new Move(from, from + 2, MoveFlag.CASTLE_SHORT, false));
        }
        int canLong = (col == Piece.WHITE.val()) 
        ? rec.cRights[1]
        : rec.cRights[3];
        if (canLong == 1) {
            moves.add(new Move(from, from - 2, MoveFlag.CASTLE_LONG, false));
        }
        return moves;

    }

    /**
     * Calculates moves for different sliding pieces
     * @param rec BoardRecord representing the position where moves should be found
     * @param from index into @param rec board[] of the sliding piece
     * @param orthogonal {@code true} if moves orthogonally, else {@code false}
     * @param diagonal {@code true} if moves diagonally, else {@code false}
     * @return list of possible moves for the sliding piece at @param from
     */
    private static MoveList slidingPieceMoves(BoardRecord rec, int from, 
            boolean orthogonal, boolean diagonal) {
        if (!orthogonal && !diagonal) { 
            System.err.println("Sliding piece must have a direction");
            return new MoveList(0); 

        }
        MoveList moves  = new MoveList(27);
        int[] offsets   = new int[]{-8, -1, 1, 8, -9, -7, 7, 9};
        int col         = rec.board[from] & 0b11000;
        int start       = (orthogonal) ? 0 : 4;
        int end         = (diagonal) ? 8 : 4;
        for (int i = start; i < end; i++) {
            int step = 1;
            int stepPos = from;
            while (step < 8) {
                if (!Board.inBounds(stepPos, offsets[i])) {
                    break;

                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == col) {
                    moves.add(new Move(from, stepPos + offsets[i], MoveFlag.ONLY_ATTACK));
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
