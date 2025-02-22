package com.ang.Core;

import com.ang.Global;
import com.ang.Core.Moves.*;
import com.ang.Engine.GameFlag;

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
        if ((kingPos == -1) || underAttack(tempRec, kingPos, col)) { 
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

    public static boolean underAttack(BoardRecord rec, int pos, Piece col) {
        return underAttack(rec, pos, col.val());
    }
    public static boolean underAttack(BoardRecord rec, int pos, int col) {        
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

    public static boolean isPromotion(BoardRecord rec, Move m) {
        int piece = rec.board[m.from] & 0b111;
        if (piece != Piece.PAWN.val()) {
            return false;
        }
        if ((m.to > 55) || (m.to < 8)) {
            return true;
        }
        return false;
    }

    public static GameFlag endState(BoardRecord rec, int col) {
        if (isDraw(rec)) {
            return GameFlag.DRAW;
        }
        
        int kingPos = findKing(rec, col);
        if (kingPos == -1) {
            return GameFlag.CHECKMATE;
        }
        
        MoveList moves = allMoves(rec, col);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) {
                continue;
            }
            BoardRecord tempRec = rec.copy();
            if (tryMove(tempRec, m)) {
                return GameFlag.NONE;
            }
        }

        if (!underAttack(rec, kingPos, col)) {
            return GameFlag.DRAW;
        }

        return GameFlag.CHECKMATE;
    }

    public static boolean canXray(int piece, int from, int to) {
        int diff = Math.abs(from - to);
        boolean inSameRow = (from % 8) == (to % 8);
        boolean inSameCol = Math.floor(from / 8) == Math.floor(to / 8);

        switch (piece) {
        case 1: case 2: case 6: // pawn, knight, king
            return false;
        case 3: // bishop
            if ((diff % 7 == 0) || (diff % 9 == 0)) return true;    
            return false;
        case 4: // rook
            if (inSameRow || inSameCol) return true;
            return false;
        case 5: // queen
            if (inSameRow || inSameCol || (diff % 7 == 0) || (diff % 9 == 0)) return true;
            return false;
        default:
            return false;
        }
    }

    public static boolean doesXray(BoardRecord rec, int piece, int from, int to, IntList exclude) {
        int rowDelta = (int) (Math.floor(from / 8) - Math.floor(to / 8));
        int colDelta = (from % 8) - (to % 8);
        
        if (!canXray(piece, from, to)) return false;

        int offset;
        switch (piece) {
        case 1: case 2: case 6: // pawn, knight, king
            return false;
        case 3:
            return false;
        case 4:
            if (rowDelta == 0) {
                offset = (colDelta < 0) ? -8 : 8;
            } else if (colDelta == 0) {
                offset = (rowDelta < 0) ? -1 : 1;
            } else {
                return false;
            }

            for (int i = 0; i < 7; i++) {
                if (!Board.inBounds(from + (offset * i), offset)) break; 

                int targetSquare = from + (offset * (i + 1));
                int targetCol = rec.board[targetSquare] & 0b11000;
                int pieceCol = rec.board[from] & 0b11000;
                if (!exclude.contains(targetSquare)) {
                    if (targetCol == pieceCol) return false;
                    if (targetCol == Piece.opposite(pieceCol).val()) {
                        if (targetSquare == to) return true;
                        return false;
                    } 
                }
                if (targetSquare == to) return true;
            }
            return false;
        case 5: // queen
            int diff = Math.abs(to - from);
            if ((diff % 7 == 0) || (diff % 9 == 0)) { // diagonal
                if (colDelta < 0) {
                    offset = (rowDelta < 0) ? -9 : 7;
                } else {
                    offset = (rowDelta < 0) ? -7 : 9;
                }
            } else if (rowDelta == 0) {
                offset = (colDelta < 0) ? -1 : 1;
            } else if (colDelta == 0) {
                offset = (rowDelta < 0) ? -8 : 8;
            } else {
                return false;
            }

            for (int i = 0; i < 7; i++) {
                if (!Board.inBounds(from + (offset * i), offset)) return false; 

                int targetSquare = from + (offset * (i + 1));
                int targetCol = rec.board[targetSquare] & 0b11000;
                int pieceCol = rec.board[from] & 0b11000;
                if (!exclude.contains(targetSquare)) {
                    if (targetCol == pieceCol) break;
                    if (targetCol == Piece.opposite(pieceCol).val()) {
                        if (targetSquare == to) return true;
                        return false;
                    } 
                }
                if (targetSquare == to) return true;
            }
            return false;
        default:
            break;
        }

        return false;
    }

    public static int getLightestAttacker(BoardRecord rec, int pos, int col, IntList exclude) {
        int minVal = Global.INFINITY;
        int minPos = -1;

        MoveList moves = Board.allMoves(rec, col);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);

            if (!m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) continue;
            if (exclude.contains(m.from)) continue;

            int piece = rec.board[m.from] & 0b11000; 
            if ((m.to == pos) || (Board.doesXray(rec, piece, m.from, pos, exclude))) {
                int se = Piece.staticEval(piece);
                if (se < minVal) {
                    minVal = se;
                    minPos = m.from;
                }
            }
        }

        return minPos;
    }

    public static boolean heavyCanCapture(BoardRecord rec, int col) {
        for (int i = 0; i < rec.allPieces.length(); i++) {
            int pos = rec.allPieces.at(i);
            if ((rec.board[pos] & 0b11000) == col) continue;

            if (col == Piece.WHITE.val()) {
                if (rec.whiteAttacksR[pos] > 0) return true;
                if (rec.whiteAttacksQ[pos] > 0) return true;
            } else {
                if (rec.blackAttacksR[pos] > 0) return true;
                if (rec.blackAttacksQ[pos] > 0) return true;
            }
        }

        return false;
    }

    public static boolean insufficientMaterial(BoardRecord rec, int col) {
        for (int i = 0; i < rec.rooks.length(); i++) {
            if ((rec.board[rec.rooks.at(i)] & 0b11000) == col) return false;
        }
        for (int i = 0; i < rec.queens.length(); i++) {
            if ((rec.board[rec.queens.at(i)] & 0b11000) == col) return false;
        }
        for (int i = 0; i < rec.pawns.length(); i++) {
            if ((rec.board[rec.pawns.at(i)] & 0b11000) == col) return false;
        }

        return true;
    }
    public static boolean insufficientMaterial(BoardRecord rec) {
        return (rec.rooks.length() == 0) 
                && (rec.pawns.length() == 0) 
                && (rec.queens.length() == 0);
    }

    public static Piece pieceInSquare(BoardRecord rec, int pos) {
        switch (rec.board[pos] & 0b111) {
        case 1:
            return Piece.PAWN;
        case 2:
            return Piece.KNIGHT;
        case 3:
            return Piece.BISHOP;
        case 4:
            return Piece.ROOK;
        case 5:
            return Piece.QUEEN;
        case 6:
            return Piece.KING;
        default:
            return Piece.NONE;
        }
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
            case MoveFlag.DOUBLE_PUSH:
                rec.epPawnPos = move.to;
                break;
            case MoveFlag.EN_PASSANT:
                MoveList takenMoves = PieceMover.moves(rec, rec.epPawnPos);
                for (int i = 0; i < takenMoves.length(); i++) {
                    Move m = takenMoves.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.removeAttack(rec.board[rec.epPawnPos], m.to);
                        // rec.removeAttack(Piece.opposite(col), m.to);
                    }
                }
                rec.board[rec.epPawnPos] = Piece.NONE.val();
                rec.removePosition(Piece.PAWN, rec.epPawnPos);

                rec.epPawnPos = -1; 
                break;
            case MoveFlag.CASTLE_SHORT:
                int shortR = move.from + 3;
                rec.board[shortR] = Piece.NONE.val();
                rec.board[shortR - 2] = Piece.ROOK.val() | col;
                rec.replacePosition(Piece.ROOK, Piece.NONE, shortR, shortR - 2);

                int shortRightsIndex = (col == Piece.WHITE.val()) ? 0 : 2;
                rec.cRights[shortRightsIndex] = 0;
                rec.cRightsLocks[shortRightsIndex] = 1;

                rec.epPawnPos = -1;
                break;
            case MoveFlag.CASTLE_LONG:
                int longR = move.from + -4;
                rec.board[longR] = Piece.NONE.val();
                rec.board[longR + 3] = Piece.ROOK.val() | col;
                rec.replacePosition(Piece.ROOK, Piece.NONE, longR, longR + 3);

                int longRightsIndex = (col == Piece.WHITE.val()) ? 1 : 3;
                rec.cRights[longRightsIndex] = 0;
                rec.cRightsLocks[longRightsIndex] = 1;

                rec.epPawnPos = -1; 
                break;
            case MoveFlag.PROMOTE:
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

    private static boolean isDraw(BoardRecord rec) {
        if (Global.fiftyMoveCounter >= 75) {
            return true;
        }
        if (Global.repTable.checkRepetitions(rec) >= 2) {
            return true;
        }
        if (Board.insufficientMaterial(rec)) {
            return true;
        }
        return false;
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
                if (underAttack(rec, pos, Piece.WHITE.val())) {
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
                if (underAttack(rec, pos, Piece.BLACK.val())) {
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
            } else if (underAttack(rec, pos, Piece.WHITE.val())) {
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
                } else if (underAttack(rec, pos, Piece.WHITE.val())) {
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
            } else if (underAttack(rec, pos + blackOffset, Piece.BLACK.val())) {
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
                } else if (underAttack(rec, pos + blackOffset, Piece.BLACK.val())) {
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
        int moving = rec.board[move.from];
        int taken = rec.board[move.to];
    
        // remove piece attacks prior to move for recalculation
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) break;

            if (pos == move.from) { // remove attacks of moving piece
                for (int i = 0; i < legalMoves.length(); i++) {
                    Move m = legalMoves.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.removeAttack(moving, m.to);
                    }
                }
            } else if (pos == move.to) { // remove attacks of taken piece
                MoveList takenMoves = PieceMover.moves(rec, pos);
                for (int i = 0; i < takenMoves.length(); i++) {
                    Move m = takenMoves.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.removeAttack(taken, m.to);
                    }
                }   
            } else if (isSlidingPiece(rec, pos)) { // remove attacks of sliding pieces
                MoveList slidingMoves = PieceMover.moves(rec, pos);
                for (int i = 0; i < slidingMoves.length(); i++) {
                    Move m = slidingMoves.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.removeAttack(rec.board[pos], m.to);
                    }
                }
            }
        }   
    
        rec.board[move.to] = rec.board[move.from];
        rec.board[move.from] = Piece.NONE.val();
        rec.replacePosition(moving, taken, move.from, move.to);
        
        resolveFlags(rec, move, moving);

        // recalculate piece attacks after move
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) break;

            if (pos == move.to) { // recalc moving piece attacks
                MoveList newMovingAttacks = PieceMover.moves(rec, pos);
                for (int i = 0; i < newMovingAttacks.length(); i++) {
                    Move m = newMovingAttacks.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.addAttack(moving, m.to);
                    }
                }
            } else if (isSlidingPiece(rec, pos)) { // recalc sliding piece attacks 
                MoveList newSlidingMoves = PieceMover.moves(rec, pos);
                for (int i = 0; i < newSlidingMoves.length(); i++) {
                    Move m = newSlidingMoves.at(i);
                    if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                        rec.addAttack(rec.board[pos], m.to);
                    }
                }
            }
        }
    }
}
