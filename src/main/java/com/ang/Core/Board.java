package com.ang.Core;

import com.ang.Core.Moves.Flag;
import com.ang.Core.Moves.Move;
import com.ang.Core.Moves.MoveList;

// TODO : bugfix - engine can sometimes move player's pieces

// TODO : bugfix - player can sometimes move into attacked squares
//          - eg, rook blocks off a rank, king can step into it
//              - not tracking attacks correctly??

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

        int kingPos = -1;
        for (int pos : tempRec.kings) {
            if (pos == -1) {
                break;
            }
            if ((tempRec.board[pos] & 0b11000) == col) {
                kingPos = pos;
                break;
            }
        }
        if (kingPos == -1) { // king is taken in this position
            return false;
        }
        if (isInCheck(tempRec, kingPos)) {
            return false;
        }

        doMove(rec, move, legal);
        updateCRights(rec, move, rec.board[move.to]);

        return true;
    }

    public static MoveList pieceMoves(BoardRecord rec, int from) {
        switch (rec.board[from] & 0b111) {
        case 1:
            return pawnMoves(   rec, from);
        case 2:
            return knightMoves( rec, from);
        case 3:
            return bishopMoves( rec, from);
        case 4:
            return rookMoves(   rec, from);
        case 5:
            return queenMoves(  rec, from);
        case 6:
            return kingMoves(   rec, from);
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
                int shortR = move.from + 3;
                rec.board[shortR]       = Piece.NONE.val();
                rec.board[shortR - 2]   = Piece.ROOK.val() | col;
                rec.posArrReplace(Piece.ROOK.val(), Piece.NONE.val(),
                                  shortR, shortR - 2);

                int shortRightsIndex = (col == Piece.WHITE.val()) ? 0 : 2;
                rec.cRights[shortRightsIndex] = 0;
                rec.cRightsLocks[shortRightsIndex] = 1;

                rec.epPawnPos = -1;
                break;
            case Flag.CASTLE_LONG:
                int longR = move.from + -4;
                rec.board[longR]        = Piece.NONE.val();
                rec.board[longR + 3]    = Piece.ROOK.val() | col;
                rec.posArrReplace(Piece.ROOK.val(), Piece.NONE.val(), 
                                  longR, longR + 3);

                int longRightsIndex = (col == Piece.WHITE.val()) ? 1 : 3;
                rec.cRights[longRightsIndex] = 0;
                rec.cRightsLocks[longRightsIndex] = 1;

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
        for (int pos : rec.kings) {
            if (pos == -1) {
                continue;
            }
            if ((rec.board[pos] & 0b11000) == Piece.WHITE.val()) {
                if (isInCheck(rec, pos)) {
                    whiteCanShort   = false;
                    whiteCanLong    = false;
                } else if (pos != 60) {
                    whiteCanShort   = false;
                    whiteCanLong    = false;
                    lockWhiteShort  = true;
                    lockWhiteLong   = true;
                }
            } else {
                if (isInCheck(rec, pos)) {
                    blackCanShort   = false;
                    blackCanLong    = false;
                } else if (pos != 4) {
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
            } else if (isUnderAttack(rec, pos + blackOffset, 
                    Piece.BLACK.val())) {
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
                } else if (isUnderAttack(rec, pos + blackOffset, 
                        Piece.BLACK.val())) {
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
                    rec.attacksArrRemove(rec.board[move.to] & 0b11000, 
                                         ml.at(i).to);
                }
            }
        } 
        
        int moving  = rec.board[move.from];
        int taken   = rec.board[move.to];
        int col     = moving & 0b11000;

        rec.board[move.to] = rec.board[move.from];
        rec.board[move.from] = Piece.NONE.val();
        rec.movedArrRemove(move.from);
        rec.movedArrAdd(move.to);

        rec.posArrReplace(moving & 0b111, taken & 0b111, move.from, move.to);

        resolveFlags(rec, move, moving);

        // get sliding piece attacks after move (up to 13 from promotions)
        MoveList[] postBlackAttacks = new MoveList[13];
        MoveList[] postWhiteAttacks = new MoveList[13];
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
        if (!isSlidingPiece(rec, move.to)) {
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
    }

    private static boolean isUnderAttack(BoardRecord rec, int pos, int col) {        
        Piece pieceCol = (col == Piece.WHITE.val()) ? Piece.WHITE : Piece.BLACK;
        return isUnderAttack(rec, pos, pieceCol);
    }
    private static boolean isUnderAttack(BoardRecord rec, int pos, Piece col) {
        int[] attacks;
        switch (col) {
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
                    moves.add(new Move(from, from + offset, 
                              Flag.PROMOTE, false));
                } else {
                    moves.add(new Move(from, from + offset, false)); 
                }
            }
            // double push
            offset = -16 * dir;
            if (((Math.floor(from / 8) == 1) || (Math.floor(from / 8) == 6))
                    && (inBounds(from, offset))) {
                if (rec.board[from + offset] == Piece.NONE.val()) {
                    moves.add(new Move(from, from + offset, 
                              Flag.DOUBLE_PUSH, false)); 
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
        MoveList moves = new MoveList(10);
        int col = rec.board[from] & 0b11000;
        int[] offsets = new int[]{-9, -8, -7, -1, 1, 7, 8, 9};

        for (int move : offsets) {
            if (inBounds(from, move) && !isUnderAttack(rec, from + move, col)) { 
                if ((rec.board[from + move] & 0b11000) != col) {
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
        int opCol       = (col == 8)    ? 16 : 8;
        int start       = (orthogonal)  ?  0 : 4;
        int end         = (diagonal)    ?  8 : 4;

        for (int i = start; i < end; i++) {
            int step = 1;
            int stepPos = from;
            while (step < 8) {
                if (!inBounds(stepPos, offsets[i])) {
                    break;
                }
                if ((rec.board[stepPos + offsets[i]] & 0b11000) == col) {
                    moves.add(new Move(from, stepPos + offsets[i], 
                              Flag.ONLY_ATTACK));
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
