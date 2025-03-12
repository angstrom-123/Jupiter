package com.ang.Core;

import com.ang.Global;
import com.ang.Core.Moves.*;

/**
 * Class containing functions to do with the virtual board and the pieces on it
 */
public class Board {    
    /**
     * Attempts to make a move on the board
     * @param rec BoardRecord representing the position where the move should be made
     * @param move the move to be attempted
     * @return {@code true} if the move is successful, else {@code false}
     */
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

    /**
     * Returns the evaluation of checkmate for a given col
     * @param col the colour that would be in checkmate
     * @return the checkmate evaluation
     */
    public static int mateEval(int col) {
        return (col == Piece.WHITE.val()) ? -Global.INFINITY : Global.INFINITY;

    }

    /**
     * Finds all moves possible for a given colour in a position. Not all moves
     * are legal, but they are possible in theory
     * @param rec the BoardRecord representing the position where to find moves
     * @param col the colour to move in the position
     * @return a MoveList of possible moves in the position for the given colour
     */
    public static MoveList allMoves(BoardRecord rec, int col) {
        MoveList out = new MoveList(200);
        for (int i : BitBoard.setBits(rec.allPieces)) {
            if (i == -1) {
                break;

            }
            if ((rec.board[i] & 0b11000) == col) {
                out.add(PieceMover.moves(rec, i));
            }
        }
        return out;

    }

    /**
     * Overload:
     * Finds the king in the position
     * @param rec BoardRecord representing the position where the king should be found
     * @param col the colour of the king to be found
     * @return an index into a BoardRecord's board[] where the given king is, -1
     * if the king can't be found
     */
    public static int findKing(BoardRecord rec, Piece col) {
        return findKing(rec, col.val());

    }

    /**
     * Finds the king in the position
     * @param rec BoardRecord representing the position where the king should be found
     * @param col integer representation of the colour of the king to be found
     * @return an index into a BoardRecord's board[] where the given king is, -1
     * if the king can't be found
     */
    public static int findKing(BoardRecord rec, int col) {
        for (int i : BitBoard.setBits(rec.kings)) {
            if (i == -1) {
                break;

            }
            if ((rec.board[i] & 0b11000) == col) {
                return i;

            }
        }
        return -1;

    }

    /**
     * Overload:
     * checks if a square is under attack
     * @param rec BoardRecord representing the position where to check if under attack
     * @param pos index into the BoardRecord's board[] of the square to check
     * @param col the colour of the piece in the tested square
     * @return {@code true} if the square is under attack by the opponent, else {@code false}
     */
    public static boolean underAttack(BoardRecord rec, int pos, Piece col) {
        return underAttack(rec, pos, col.val());

    }

    /**
    * checks if a square is under attack
    * @param rec BoardRecord representing the position where to check if under attack
    * @param pos index into the BoardRecord's board[] of the square to check
    * @param col integer representation of the colour of the piece in the tested square
    * @return {@code true} if the square is under attack by the opponent, else {@code false}
    */
    public static boolean underAttack(BoardRecord rec, int pos, int col) {        
        if (col == Piece.BLACK.val()) {
            return (BitBoard.bitActive(rec.whiteAttacks, pos));

        } else if (col == Piece.WHITE.val()) {
            return (BitBoard.bitActive(rec.blackAttacks, pos));

        }
        return false;

    }

    /**
     * Checks if a move results in a piece going out of bounds
     * @param from index into a BoardRecord's board[] that the move is from
     * @param offset offset into a BoardRecord's board[] representing a move
     * @return {@code true} if the move goes out of bounds, else {@code false}
     */
    public static boolean inBounds(int from, int offset) {
        if ((from + offset > 63) || (from + offset) < 0) {
            return false;

        }
        int posX = from % 8;
        int posY = (int) Math.floor(from / 8);
        int offsetX = (from + offset) % 8;
        int offsetY = (int) Math.floor((from + offset) / 8);
        int deltaX = offsetX - posX;
        int deltaY = offsetY - posY;
        // if the end coord is outside of a 5x5 grid centred on start, OOB
        return !((Math.abs(deltaX) > 2) || (Math.abs(deltaY) > 2));

    }

    /**
     * Checks if a move is a promotion of a pawn
     * @param rec BoardRecord representing the position where to check for promotion
     * @param m the move to check
     * @return {@code true} if the move promotes a pawn, else {@code false}
     */
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

    /**
     * Checks the current status of the game
     * @param rec BoardRecord representing the position to check
     * @param col the colour to move in the position
     * @return {@code DRAW} if stalemate, {@code CHECKMATE} if checkmate, else {@code NONE}
     */
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

    /**
     * Checks if a piece can x-ray a square based on its movement direction
     * @param piece the piece to check
     * @param from index into a BoardRecord's board[] that the piece is on
     * @param to index into a BoardRecord's board[] of the target square
     * @return {@code true} if it can x-ray, else {@code false}
     */
    public static boolean canXray(int piece, int from, int to) {
        int diff = Math.abs(from - to);
        boolean inSameRow = (from % 8) == (to % 8);
        boolean inSameCol = Math.floor(from / 8) == Math.floor(to / 8);
        switch (piece) {
        case 1: case 2: case 6: // pawn, knight, king
            return false;

        case 3: // bishop
            if ((diff % 7 == 0) || (diff % 9 == 0)) {
                return true;    

            }
            return false;

        case 4: // rook
            if (inSameRow || inSameCol) {
                return true;

            }
            return false;

        case 5: // queen
            if (inSameRow || inSameCol || (diff % 7 == 0) || (diff % 9 == 0)) {
                return true;

            }
            return false;

        default:
            return false;

        }
    }

    /**
     * Checks if a piece does hit a square after certain pieces are removed
     * @param rec BoardRecord representing the position to check
     * @param piece the piece to check
     * @param from index into the @param rec board[] that the piece is on
     * @param to index into the @param rec board[] of the target square
     * @param exclude list of indices into the @param rec board[] to treat as
     * if they were taken in the position (to ignore)
     * @return {@code true} if removing pieces in @param exclude allows @param piece
     * to attack @param to, else {@code false} 
     */
    public static boolean doesXray(BoardRecord rec, int piece, int from, int to, IntList exclude) {
        int rowDelta = (int) (Math.floor(from / 8) - Math.floor(to / 8));
        int colDelta = (from % 8) - (to % 8);
        if (!canXray(piece, from, to)) {
            return false;

        }
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
                if (!Board.inBounds(from + (offset * i), offset)) {
                    break; 

                }
                int targetSquare = from + (offset * (i + 1));
                int targetCol = rec.board[targetSquare] & 0b11000;
                int pieceCol = rec.board[from] & 0b11000;
                if (!exclude.contains(targetSquare)) {
                    if (targetCol == pieceCol) {
                        return false;

                    }
                    if (targetCol == Piece.opposite(pieceCol).val()) {
                        if (targetSquare == to) {
                            return true;

                        }
                        return false;

                    } 
                }
                if (targetSquare == to) {
                    return true;

                }
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
                if (!Board.inBounds(from + (offset * i), offset)) {
                    return false; 

                }
                int targetSquare = from + (offset * (i + 1));
                int targetCol = rec.board[targetSquare] & 0b11000;
                int pieceCol = rec.board[from] & 0b11000;
                if (!exclude.contains(targetSquare)) {
                    if (targetCol == pieceCol) {
                        break;

                    }
                    if (targetCol == Piece.opposite(pieceCol).val()) {
                        if (targetSquare == to) {
                            return true;

                        }
                        return false;

                    } 
                }
                if (targetSquare == to) {
                    return true;

                }
            }
            return false;

        default:
            return false;

        }
    }

    /**
     * Finds the lowest value (static evaluation) attacker of a square after certain
     * pieces are removed
     * @param rec BoardRecord representing the position to find the attacker
     * @param pos index into @param rec board[] that the resulting attacker should hit
     * @param col colour to move in the position
     * @param exclude list of indices into @param rec to treat as if they were
     * removed (to ignore)
     * @return index into @param rec board[] of the lightest attacker, -1 if not found
     */
    public static int getLightestAttacker(BoardRecord rec, int pos, int col, IntList exclude) {
        int minVal = Global.INFINITY;
        int minPos = -1;
        MoveList moves = Board.allMoves(rec, col);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (!m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                continue;

            }
            if (exclude.contains(m.from)) {
                continue;

            }
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

    /**
     * Overload:
     * Checks if a player has insufficient material to force a win
     * @param rec BoardRecord representing the position to check
     * @param col the colour to move in the position
     * @return {@code true} if the player playing @param col has insufficient 
     * material, else {@code false}
     */
    public static boolean insufficientMaterial(BoardRecord rec, int col) {
        if (rec.pawnCount + rec.rookCount + rec.queenCount == 0) {
            return true;

        }
        for (int i : BitBoard.setBits(rec.pawns)) {
            if (i == -1) {
                break;

            }
            if ((rec.board[i] & 0b11000) == col) {
                return false;

            }
        }
        for (int i : BitBoard.setBits(rec.rooks)) {
            if (i == -1) {
                break;

            }
            if ((rec.board[i] & 0b11000) == col) {
                return false;

            }
        }
        for (int i : BitBoard.setBits(rec.queens)) {
            if (i == -1) {
                break;

            }
            if ((rec.board[i] & 0b11000) == col) {
                return false;

            }
        }
        return true;

    }

    /**
     * Checks if there is insufficient material for either player to force a win
     * @param rec BoardRecord representing the position to check
     * @return {@code true} if there is insufficient material. else {@code false}
     */
    public static boolean insufficientMaterial(BoardRecord rec) {
        return (rec.pawnCount + rec.rookCount + rec.queenCount == 0);

    }

    /**
     * Checks which piece is in a given square
     * @param rec BoardRecord representing the position to check
     * @param pos index into @param rec board[] to search
     * @return the piece in the square
     */
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

    /**
     * Checks if a piece is a long range sliding piece
     * @param rec BoardRecord representing the position to check
     * @param pos index into @param rec board[] containing the piece to check
     * @return {@code true} if the piece slides, else {@code false}
     */
    private static boolean isSlidingPiece(BoardRecord rec, int pos) {
        switch (rec.board[pos] & 0b111) {
            case 3: case 4: case 5: // bishop, rook, queen
                return true;

            default:
                return false;

        }
    }

    /**
     * Updates the information in @param rec to deal with potential flags of a move
     * that was made
     * @param rec BoardRecord representing the position where @param move was made
     * @param move the move that was made
     * @param piece the piece that moved
     */
    private static void resolveFlags(BoardRecord rec, Move move, int piece) {
        int col = piece & 0b11000;
        switch (move.flag) {
        case MoveFlag.DOUBLE_PUSH:
            rec.epPawnPos = move.to;
            break;

        case MoveFlag.EN_PASSANT:
            if (rec.epPawnPos == -1) {
                break;

            }
            MoveList takenMoves = PieceMover.moves(rec, rec.epPawnPos);
            for (int i = 0; i < takenMoves.length(); i++) {
                Move m = takenMoves.at(i);
                if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                    rec.removeAttack(rec.board[rec.epPawnPos], m.to);
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

    /**
     * Checks if a given position is a draw from 50-move rule, repetition, or 
     * insufficient material
     * @param rec BoardRecord representing the position to check
     * @return {@code true} if it is a draw, else {@code false}
     */
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

    /**
     * Updates the information in @param rec to update castling rights
     * @param rec BoardRecord representing the position where @param move was made
     * @param move the move that was made
     * @param piece the piece that moved
     */
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
        for (int pos : BitBoard.setBits(rec.kings)) {
            if (pos == -1) {
                break;

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

    /**
     * Updates @param rec to simulate a move being made
     * @param rec BoardRecord representing the position where @param move was made
     * @param move the move that was made
     * @param legalMoves the list of legal moves for the moving piece
     */
    private static void makeMove(BoardRecord rec, Move move, MoveList legalMoves) {        
        int moving = rec.board[move.from];
        int taken = rec.board[move.to];
        // remove piece attacks prior to move for recalculation
        for (int pos : BitBoard.setBits(rec.allPieces)) {
            if (pos == -1) {
                break;

            }
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
        for (int pos : BitBoard.setBits(rec.allPieces)) {
            if (pos == -1) {
                break;

            }
            // recalculate piece attacks
            MoveList moves = PieceMover.moves(rec, pos);
            for (int i = 0; i < moves.length(); i++) {
                Move m = moves.at(i);
                if (m.attack || (m.flag == MoveFlag.ONLY_ATTACK)) {
                    rec.addAttack(rec.board[pos], m.to);
                }
            }
        }
    }
}
