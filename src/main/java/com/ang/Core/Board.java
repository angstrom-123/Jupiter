package com.ang.Core;

import com.ang.Core.Moves.*;

// TODO : bugfix - Piece attacks sometimes go negative (especially near king moves)
//          - King attacks not tracking correctly, eg rook next to king has 0
//              in this case when the king moves away the number goes negative

public class Board {
    // public
    public static boolean tryMove(BoardRecord rec, Move move) {
        if (move.isInvalid()) {
            System.err.println("Attempting invalid move");
            return false;
        }

        MoveList legal = PieceMover.moves(rec, move.from);
        if (!legal.containsMove(move)) {
            return false;
        }
        
        BoardRecord tempRec = rec.copy();
        makeMove(tempRec, move, legal);

        int col = rec.board[move.from] & 0b11000;
        int kingPos = findKing(tempRec, col);
        if ((kingPos == -1) || isUnderAttack(tempRec, kingPos, col)) { 
            return false;
        }

        makeMove(rec, move, legal);
        updateCRights(rec, move, rec.board[move.to]);
        return true;
    }

    public static MoveList allMoves(BoardRecord rec, int col) {
        MoveList out = new MoveList(200);
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            if ((rec.board[pos] & 0b11000) != col) {
                continue;
            }
            out.add(PieceMover.moves(rec, pos));
        }
        return out;
    }

    public static int findKing(BoardRecord rec, Piece col) {
        return findKing(rec, col.val());
    }
    public static int findKing(BoardRecord rec, int col) {
        for (int pos : rec.kings.elements) {
            if (pos == -1) {
                break;
            }
            if ((rec.board[pos] & 0b11000) == col) {
                return pos;
            }
        }
        return -1;
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
                MoveList takenMoves = PieceMover.moves(rec, rec.epPawnPos);
                for (int i = 0; i < takenMoves.length(); i++) {
                    Move m = takenMoves.at(i);
                    if (m.attack || (m.flag == Flag.ONLY_ATTACK)) {
                        rec.removeAttack(Piece.opposite(col), m.to);
                    }
                }
                rec.board[rec.epPawnPos] = Piece.NONE.val();
                rec.removePosition(Piece.PAWN, rec.epPawnPos);

                rec.epPawnPos = -1; 
                break;
            case Flag.CASTLE_SHORT:
                int shortR = move.from + 3;
                rec.board[shortR] = Piece.NONE.val();
                rec.board[shortR - 2] = Piece.ROOK.val() | col;
                rec.replacePosition(Piece.ROOK, Piece.NONE, shortR, shortR - 2);

                int shortRightsIndex = (col == Piece.WHITE.val()) ? 0 : 2;
                rec.cRights[shortRightsIndex] = 0;
                rec.cRightsLocks[shortRightsIndex] = 1;

                rec.epPawnPos = -1;
                break;
            case Flag.CASTLE_LONG:
                int longR = move.from + -4;
                rec.board[longR] = Piece.NONE.val();
                rec.board[longR + 3] = Piece.ROOK.val() | col;
                rec.replacePosition(Piece.ROOK, Piece.NONE, longR, longR + 3);

                int longRightsIndex = (col == Piece.WHITE.val()) ? 1 : 3;
                rec.cRights[longRightsIndex] = 0;
                rec.cRightsLocks[longRightsIndex] = 1;

                rec.epPawnPos = -1; 
                break;
            case Flag.PROMOTE:
                int dir = (col == Piece.WHITE.val()) ? 1 : -1;
                int[] oldAttacks = new int[]{-7 * dir, -9 * dir};
                for (int offset : oldAttacks) {
                    if (inBounds(move.from, offset)) {
                        rec.removeAttack(col, move.from + offset);
                    }
                }
                rec.board[move.to] = Piece.QUEEN.val() | col;
                rec.removePosition(piece, move.to);
                rec.addPosition(Piece.QUEEN, move.to);

                rec.epPawnPos = -1; 
                break;
            default:
                rec.epPawnPos = -1; 
                break;
            }
    }

    private static void updateCRights(BoardRecord rec, Move move, int piece) {
        boolean whiteCanShort   = true;
        boolean whiteCanLong    = true;
        boolean blackCanShort   = true;
        boolean blackCanLong    = true;
        boolean lockWhiteShort  = false;
        boolean lockWhiteLong   = false;
        boolean lockBlackShort  = false;
        boolean lockBlackLong   = false;

        // kings' castling paths
        int[]   whiteShortRoute = new int[]{61, 62};
        int[]   whiteLongRoute  = new int[]{58, 59};
        int     longEmptyPos    = 57;
        int     blackOffset     = -56;

        // king moved or in check
        for (int pos : rec.kings.elements) {
            if (pos == -1) {
                continue;
            }
            if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                if (isUnderAttack(rec, pos, Piece.WHITE.val())) {
                    whiteCanShort   = false;
                    whiteCanLong    = false;
                } 
                if (pos != 60) {
                    whiteCanShort   = false;
                    whiteCanLong    = false;
                    lockWhiteShort  = true;
                    lockWhiteLong   = true;
                }
            } else { 
                if (isUnderAttack(rec, pos, Piece.BLACK.val())) {
                    blackCanShort   = false;
                    blackCanLong    = false;
                } 
                if (pos != 4) {
                    blackCanShort   = false;
                    blackCanLong    = false;
                    lockBlackShort  = true;
                    lockBlackLong   = true;
                }
            }
        }

        // black king's rook moved
        if ((rec.board[7]) != (Piece.ROOK.val() | Piece.BLACK.val())) {
            blackCanShort   = false;
            lockBlackShort  = true;
        }
        // black queen's rook moved
        if ((rec.board[0]) != (Piece.ROOK.val() | Piece.BLACK.val())) {
            blackCanLong    = false;
            lockBlackLong   = true;
        }
        // white king's rook moved
        if ((rec.board[63]) != (Piece.ROOK.val() | Piece.WHITE.val())) {
            whiteCanShort   = false;
            lockWhiteShort  = true;
        }
        // white queen's rook moved
        if ((rec.board[56]) != (Piece.ROOK.val() | Piece.WHITE.val())) {
            whiteCanLong    = false;
            lockWhiteLong   = true;
        }

        // white short path blocked or checked
        for (int pos : whiteShortRoute) {
            if (rec.board[pos] != Piece.NONE.val()) {
                whiteCanShort = false;
                break;
            } else if (isUnderAttack(rec, pos, Piece.WHITE.val())) {
                whiteCanShort = false;
                break;
            }
        }
        // white long path blocked or checked
        if (rec.board[longEmptyPos] != Piece.NONE.val()) {
            whiteCanLong = false;
        } else {
            for (int pos : whiteLongRoute) {
                if (rec.board[pos] != Piece.NONE.val()) {
                    whiteCanLong = false;
                    break;
                } else if (isUnderAttack(rec, pos, Piece.WHITE.val())) {
                    whiteCanLong = false;
                    break;
                }
            }
        }
        // black short path blocked or checked
        for (int pos : whiteShortRoute) {
            if (rec.board[pos + blackOffset] != Piece.NONE.val()) {
                blackCanShort = false;
                break;
            } else if (isUnderAttack(rec, pos + blackOffset, Piece.BLACK.val())) {
                blackCanShort = false;
                break;
            }
        }
        // black long path blocked or checked
        if (rec.board[longEmptyPos + blackOffset] != Piece.NONE.val()) {
            blackCanLong = false;
        } else {
            for (int pos : whiteLongRoute) {
                if (rec.board[pos + blackOffset] != Piece.NONE.val()) {
                    blackCanLong = false;
                    break;
                } else if (isUnderAttack(rec, pos + blackOffset, Piece.BLACK.val())) {
                    blackCanLong = false;
                    break;
                }
            }
        }

        // update castling rights locks
        if (lockWhiteShort) {
            rec.cRightsLocks[0] = 1; 
            rec.cRights[0]      = 0;
        }
        if (lockWhiteLong) {
            rec.cRightsLocks[1] = 1;
            rec.cRights[1]      = 0; 
        }
        if (lockBlackShort) {
            rec.cRightsLocks[2] = 1; 
            rec.cRights[2]      = 0;
        }
        if (lockBlackLong) {
            rec.cRightsLocks[3] = 1; 
            rec.cRights[3]      = 0;
        }

        // update castling rights if unlocked
        if (rec.cRightsLocks[0] == 0) {
            rec.cRights[0] = (whiteCanShort) ? 1 : 0;
        }
        if (rec.cRightsLocks[1] == 0) {
            rec.cRights[1] = (whiteCanLong) ? 1 : 0;
        }
        if (rec.cRightsLocks[2] == 0) {
            rec.cRights[2] = (blackCanShort) ? 1 : 0;
        }
        if (rec.cRightsLocks[3] == 0) {
            rec.cRights[3] = (blackCanLong) ? 1 : 0;
        }
    }

    private static void makeMove(BoardRecord rec, Move move, MoveList legalMoves) {        
        MoveList preBlackAttacks = new MoveList(300);
        MoveList preWhiteAttacks = new MoveList(300);
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            // calculate attacks for sliding pieces before move
            if (isSlidingPiece(rec, pos)) {
                if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                    preWhiteAttacks.add(PieceMover.moves(rec, pos));
                } else {
                    preBlackAttacks.add(PieceMover.moves(rec, pos));
                }
            }
        }

        // recalculate attacks of taken piece, if sliding then already done
        if (!isSlidingPiece(rec, move.to)) {
            MoveList ml = PieceMover.moves(rec, move.to);
            for (int i = 0; i < ml.length(); i++) {
                if (ml.at(i).attack) {
                    rec.removeAttack(rec.board[move.to] & 0b11000, ml.at(i).to);
                }
            }
        } 
        
        int moving = rec.board[move.from];
        int taken = rec.board[move.to];
        int col = moving & 0b11000;

        rec.board[move.to] = rec.board[move.from];
        rec.board[move.from] = Piece.NONE.val();
        rec.replacePosition(moving, taken, move.from, move.to);
        
        resolveFlags(rec, move, moving);

        MoveList postBlackAttacks = new MoveList(300);
        MoveList postWhiteAttacks = new MoveList(300);
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            if (isSlidingPiece(rec, pos)) {
                if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                    postWhiteAttacks.add(PieceMover.moves(rec, pos));
                } else {
                    postBlackAttacks.add(PieceMover.moves(rec, pos));
                }
            }
        }

        // remove all sliding piece attacks from before move
        for (int i = 0; i < preWhiteAttacks.length(); i++) {
            Move m = preWhiteAttacks.at(i);
            if (m == null) {
                break;
            }
            rec.removeAttack(Piece.WHITE.val(), m.to);
        }
        for (int i = 0; i < preBlackAttacks.length(); i++) {
            Move m = preBlackAttacks.at(i);
            if (m == null) {
                break;
            }
            rec.removeAttack(Piece.BLACK.val(), m.to);
        }

        // re-add all sliding piece attacks after move
        for (int i = 0; i < postWhiteAttacks.length(); i++) {
            Move m = postWhiteAttacks.at(i);
            if (m == null) {
                break;
            }
            rec.addAttack(Piece.WHITE.val(), m.to);
        }
        for (int i = 0; i < postBlackAttacks.length(); i++) {
            Move m = postBlackAttacks.at(i);
            if (m == null) {
                break;
            }
            rec.addAttack(Piece.BLACK.val(), m.to);
        }

        // recalculate moving piece attacks, if it's sliding it's already done
        if (!isSlidingPiece(rec, move.to)) {
            // removing all attacks from before move
            for (int i = 0; i < legalMoves.length(); i++) {
                if (legalMoves.at(i).attack) {
                    rec.removeAttack(col, legalMoves.at(i).to);
                }
            }
            // calculate and add new attacks after move
            MoveList newMoves = PieceMover.moves(rec, move.to);
            for (int i = 0; i < newMoves.length(); i++) {
                if (newMoves.at(i).attack) {
                    rec.addAttack(col, newMoves.at(i).to); 
                }
            } 
        }        
    }

    public static boolean isUnderAttack(BoardRecord rec, int pos, Piece col) {
        return isUnderAttack(rec, pos, col.val());
    }
    public static boolean isUnderAttack(BoardRecord rec, int pos, int col) {        
        if (col == Piece.BLACK.val()) {
            return (rec.whiteAttacks[pos] > 0);
        } else if (col == Piece.WHITE.val()) {
            return (rec.blackAttacks[pos] > 0);
        }
        return false;
    }

    public static boolean inBounds(int from, int offset) {
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
}
