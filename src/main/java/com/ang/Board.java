package com.ang;

import com.ang.Moves.Flag;
import com.ang.Moves.Move;
import com.ang.Moves.MoveList;
import com.ang.Util.BoardRecord;

public class Board {
    // public
    public static boolean tryMove(BoardRecord rec, Move move) {
        if (move.isInvalid()) {
            System.err.println("Attempting invalid move");
            return false;
        }

        int col = (rec.board[move.from] & 0b11000) == Piece.WHITE.val()
        ? Piece.WHITE.val()
        : Piece.BLACK.val();

        MoveList legal = pieceMoves(rec, move.from);
        boolean legalMove = false;
        for (int i = 0; i < legal.length(); i++) {
            Move m = legal.at(i);
            if ((m.flag != Flag.ONLY_ATTACK) && (m.equals(move))) {
                legalMove = true;
            }
        }

        if (!legalMove) {
            return false;
        }        
        
        BoardRecord tempRec = rec.copy();
        doMove(tempRec, move, legal);

        // in check?
        int kingPos = -1;
        for (int pos : tempRec.kings) {
            if (pos == -1) {
                break;
            }
            if ((tempRec.board[pos] & 0b11000) == col) {
                kingPos = pos;
            }
        }
        if (kingPos == -1) { // king taken in this pos?
            return false;
        }
        if (isInCheck(tempRec, kingPos)) {
            return false;
        }

        doMove(rec, move, legal);

        return true;
    }

    public static MoveList pieceMoves(BoardRecord rec, int from) {
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

    public static MoveList allMoves(BoardRecord rec, int col) {
        MoveList out = new MoveList(200);
        for (int pos : rec.allPieces) {
            if (pos == -1) {
                break;
            }
            if ((rec.board[pos] & 0b11000) != col) {
                continue;
            }
            out.add(pieceMoves(rec, pos));
        }
        return out;
    }

    // private
    private static boolean isSlidingPiece(BoardRecord rec, int pos) {
        switch (rec.board[pos] & 0b111) {
            case 3: case 4: case 5: // bishop, rook, queen
                return true;
            default:
                return false;
        }
    }

    private static void resolveFlags(BoardRecord rec, Move move, int piece) {
        int col = piece & 0b11000;
        switch (move.flag) {
            case Flag.DOUBLE_PUSH:
                rec.epPawnPos = move.to;
                break;
            case Flag.EN_PASSANT:
                rec.board[rec.epPawnPos] = Piece.NONE.val();
                rec.posArrRemove(Piece.PAWN.val(), rec.epPawnPos);
                rec.epPawnPos = -1; 
                break;
            case Flag.CASTLE_SHORT:
                int shortRook = move.from + 3;
                rec.board[shortRook] = Piece.NONE.val();
                rec.board[shortRook - 2] = Piece.ROOK.val() | col;
                rec.posArrReplace(Piece.ROOK.val(), Piece.NONE.val(), shortRook, shortRook - 2);
                rec.epPawnPos = -1; 
                break;
            case Flag.CASTLE_LONG:
                int longRook = move.from + -4;
                rec.board[longRook] = Piece.NONE.val();
                rec.board[longRook + 3] = Piece.ROOK.val() | col;
                rec.posArrReplace(Piece.ROOK.val(), Piece.NONE.val(), longRook, longRook + 3);
                rec.epPawnPos = -1; 
                break;
            case Flag.PROMOTE:
                rec.board[move.to] = Piece.QUEEN.val() | col;
                rec.posArrRemove(piece, move.to);
                rec.posArrAdd(Piece.QUEEN.val(), move.to);
                rec.attacksArrRemove(col, move.to);
                rec.epPawnPos = -1; 
                break;
            default:
                rec.epPawnPos = -1; 
                break;
            }
    }

    private static void doMove(BoardRecord rec, Move move, MoveList legalMoves) {        
        // get sliding piece attacks before move (up to 13 from promotions)
        MoveList[] preBlackAttacks = new MoveList[13];
        MoveList[] preWhiteAttacks = new MoveList[13];
        int preBlackEnd = 0;
        int preWhiteEnd = 0;
        for (int pos : rec.allPieces) {
            if (pos == -1) {
                break;
            }
            // calculate attacks for sliding pieces before move
            if (isSlidingPiece(rec, pos)) {
                if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                    preWhiteAttacks[preWhiteEnd++] = pieceMoves(rec, pos);
                } else {
                    preBlackAttacks[preBlackEnd++] = pieceMoves(rec, pos);
                }
            }
        }

        // recalculate attacks of taken piece, if sliding then already done
        if (!isSlidingPiece(rec, move.to)) {
            MoveList ml = pieceMoves(rec, move.to);
            for (int i = 0; i < ml.length(); i++) {
                if (ml.at(i).attack) {
                    rec.attacksArrRemove(rec.board[move.to] & 0b11000, ml.at(i).to);
                }
            }
        } 
        
        int moving = rec.board[move.from];
        int taken = rec.board[move.to];
        int col = moving & 0b11000;

        rec.board[move.to] = rec.board[move.from];
        rec.board[move.from] = Piece.NONE.val();
        rec.movedArrRemove(move.from); // does nothing if the piece has not moved yet
        rec.movedArrAdd(move.to);

        rec.posArrReplace(moving & 0b111, taken & 0b111, move.from, move.to);

        resolveFlags(rec, move, moving);

        // get sliding piece attacks before move
        MoveList[] postBlackAttacks = new MoveList[13]; // promotions add more
        MoveList[] postWhiteAttacks = new MoveList[13]; // promotions add more
        int postBlackEnd = 0;
        int postWhiteEnd = 0;
        for (int pos : rec.allPieces) {
            if (pos == -1) {
                break;
            }
            if (isSlidingPiece(rec, pos)) {
                if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                    postWhiteAttacks[postWhiteEnd++] = pieceMoves(rec, pos);
                } else {
                    postBlackAttacks[postBlackEnd++] = pieceMoves(rec, pos);
                }
            }
        }

        // remove all sliding piece attacks from before move
        for (MoveList ml : preWhiteAttacks) {
            if (ml == null) {
                break;
            }
            for (int i = 0; i < ml.length(); i++) {
                rec.attacksArrRemove(Piece.WHITE.val(), ml.at(i).to);
            }
        }
        for (MoveList ml : preBlackAttacks) {
            if (ml == null) {
                break;
            }
            for (int i = 0; i < ml.length(); i++) {
                rec.attacksArrRemove(Piece.BLACK.val(), ml.at(i).to);
            }
        }

        // re-add all sliding piece attacks after move
        for (MoveList ml : postWhiteAttacks) {
            if (ml == null) {
                break;
            }
            for (int i = 0; i < ml.length(); i++) {
                rec.attacksArrAdd(Piece.WHITE.val(), ml.at(i).to);
            }
        }
        for (MoveList ml : postBlackAttacks) {
            if (ml == null) {
                break;
            }
            for (int i = 0; i < ml.length(); i++) {
                rec.attacksArrAdd(Piece.BLACK.val(), ml.at(i).to);
            }
        }

        // recalculate moving piece attacks, if it is sliding it's already done
        if (isSlidingPiece(rec, move.to)) {
            return;
        }

        // removing all attacks from before move
        for (int i = 0; i < legalMoves.length(); i++) {
            if (legalMoves.at(i).attack) {
                rec.attacksArrRemove(col, legalMoves.at(i).to);
            }
        }

        // calculate and add new attacks after move
        MoveList newMoves = pieceMoves(rec, move.to);
        for (int i = 0; i < newMoves.length(); i++) {

            if (newMoves.at(i).attack) {
                rec.attacksArrAdd(col, newMoves.at(i).to); 
            }
        } 
    }

    private static boolean isUnderAttack(BoardRecord rec, int pos, int col) {        
        Piece pieceCol = (col == Piece.WHITE.val()) ? Piece.WHITE : Piece.BLACK;
        int[] attacks;
        switch (pieceCol) {
            case BLACK:
                attacks = rec.whiteAttacks;
                break;
            case WHITE:
                attacks = rec.blackAttacks;
                break;
            default:
                return false;
        }

        if (attacks[pos] > 0) {
            return true;
        }
        return false;
    }

    private static boolean isInCheck(BoardRecord rec, int pos) {
        return isUnderAttack(rec, pos, rec.board[pos] & 0b11000);
        // return false;
    }

    private static boolean hasMoved(BoardRecord rec, int pos) {
        for (int p : rec.movedPieces) {
            if (p == -1) {
                return false;
            }
            if (p == pos) {
                return true;
            }
        }
        return false;
    }

    private static boolean inBounds(int from, int offset) {
        if ((from + offset > 63) || (from + offset) < 0) {
            return false;
        }

        int posX = from % 8;
        int posY = (int)Math.floor(from / 8);

        int offsetX = (from + offset) % 8;
        int offsetY = (int)Math.floor((from + offset) / 8);

        int deltaX = offsetX - posX;
        int deltaY = offsetY - posY;

        // if the end coord is outside of a 5x5 grid centred on start, OOB
        return !((Math.abs(deltaX) > 2) || (Math.abs(deltaY) > 2));
    }

    private static MoveList pawnMoves(BoardRecord rec, int from) {
        MoveList moves = new MoveList(6);
        int opCol;
        int offset;
        int dir;

        if ((rec.board[from] & 0b11000) == Piece.WHITE.val()) {
            opCol = Piece.BLACK.val();
            dir = 1;
        } else {
            opCol = Piece.WHITE.val();
            dir = -1;
        }
        
        offset = -8 * dir;
        if (rec.board[from + offset] == Piece.NONE.val()) {
            // single push
            if (inBounds(from, offset)) { 
                if ((from + offset < 8) || (from + offset > 55)) {
                    moves.add(new Move(from, from + offset, Flag.PROMOTE, false));
                } else {
                    moves.add(new Move(from, from + offset, false)); 
                }
            }

            if (!hasMoved(rec, from)) {
                offset = -16 * dir;
                if (rec.board[from + offset] == Piece.NONE.val()) {
                    // double push
                    moves.add(new Move(from, from + offset, Flag.DOUBLE_PUSH, false)); 
                }
            } 
        }
        
        int[] offsets = new int[]{(-8 * dir) - 1, (-8 * dir) + 1};
        for (int off : offsets) {
            if (!inBounds(from, off)) {
                continue;
            }
            if ((rec.board[from + off] & 0b11000) == opCol) {
                // take
                if ((from + off < 8) || (from + off > 55)) {
                    moves.add(new Move(from, from + off, Flag.PROMOTE));
                } else {
                    moves.add(new Move(from, from + off)); 
                }
            } else if (rec.board[from + off] == Piece.NONE.val()) {
                if (rec.epPawnPos == from + (off - (-8 * dir))) {
                    moves.add(new Move(from, from + off, Flag.EN_PASSANT)); 
                } else {
                    moves.add(new Move(from, from + off, Flag.ONLY_ATTACK));
                }
            } else { // friendly piece at attack square
                moves.add(new Move(from, from + off, Flag.ONLY_ATTACK));
            }
        }
        return moves;
    }

    private static MoveList knightMoves(BoardRecord rec, int from) {
        MoveList moves = new MoveList(8);
        int col = rec.board[from] & 0b11000;
        int[] offsets = new int[]{-17, -15, -10, -6, 6, 10, 15, 17};

        for (int move : offsets) {
            if (inBounds(from, move)) {
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
        MoveList moves = new MoveList(8);
        int col = rec.board[from] & 0b11000;
        int[] offsets = new int[]{-9, -8, -7, -1, 1, 7, 8, 9};

        for (int move : offsets) {
            if (inBounds(from, move)) { 
                if ((rec.board[from + move] & 0b11000) != col) {
                    moves.add(new Move(from, from + move));
                } else {
                    moves.add(new Move(from, from + move, Flag.ONLY_ATTACK));
                }
            }
        }

        if (hasMoved(rec, from)) {
            return moves;
        }

        if (isInCheck(rec, from)) {
            return moves;
        }  

        // castle short
        int[] shortOffsets = new int[]{1, 2};
        boolean canShort = true;
        for (int offset : shortOffsets) {
            if (rec.board[from + offset] != Piece.NONE.val()) {
                canShort = false;
                break;
            }
            if (isUnderAttack(rec, from + offset, col)) {
                canShort = false;
                break;
            }
        }
        if (canShort) {
            int rookPos = (col == Piece.WHITE.val()) ? 63 : 7;
            if (!hasMoved(rec, rookPos)) {
                moves.add(new Move(from, from + 2, Flag.CASTLE_SHORT, false));
            }
        }

        // castle long
        int[] longOffsets = new int[]{-1, -2, -3};
        boolean canLong = true;
        for (int offset : longOffsets) {
            if (rec.board[from + offset] != Piece.NONE.val()) {
                canLong = false;
                break;
            }
            if (isUnderAttack(rec, from + offset, col)) {
                canLong = false;
                break;
            }
        }
        if (canLong) {
            int rookPos = (col == Piece.WHITE.val()) ? 56 : 0;
            if (!hasMoved(rec, rookPos)) {
                moves.add(new Move(from, from - 2, Flag.CASTLE_LONG, false));
            }
        }

        return moves;
    }

    private static MoveList slidingPieceMoves(BoardRecord rec, int from, 
            boolean orthogonal, boolean diagonal) {
        if ((!orthogonal) && (!diagonal)) { 
            System.err.println("Sliding piece must have a direction");
            return new MoveList(0); 
        }

        MoveList moves = new MoveList(27);

        int[] offsets = new int[]{-8, -1, 1, 8, -9, -7, 7, 9};
        int col = rec.board[from] & 0b11000;
        int opCol = (col == 8) ? 16 : 8;

        int start = (orthogonal) ? 0 : 4;
        int end = (diagonal) ? 8 : 4;
        for (int i = start; i < end; i++) {
            int step = 1;
            int stepPos = from;
            while (step < 8) {
                if (!inBounds(stepPos, offsets[i])) {
                    break;
                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == col) {
                    moves.add(new Move(from, stepPos + offsets[i], Flag.ONLY_ATTACK));
                    break;
                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == opCol) {
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
