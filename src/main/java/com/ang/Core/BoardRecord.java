package com.ang.Core;

import com.ang.Util.FENReader;
import com.ang.Core.Moves.*;

/**
 * Class for representing the state of a chess position
 */
public class BoardRecord {
    public int[]    board;
    public int      epPawnPos;
    public int      minorPieceCount;
    public int      pawnCount;
    public int      rookCount;
    public int      queenCount;
    public int[]    cRights;
    public int[]    cRightsLocks;
    public long     pawns;
    public long     knights;
    public long     bishops;
    public long     rooks;
    public long     queens;
    public long     kings;
    public long     allPieces;
    public long     whiteAttacks;
    public long     blackAttacks;

    /**
     * Constructs a blank BoardRecord
     */
    public BoardRecord() {}

    /**
     * Constructs from a starting FEN position
     * @param startFEN starting position in FEN
     */
    public BoardRecord(String startFEN) {
        board           = FENReader.readFEN(startFEN);
        epPawnPos       = -1;
        minorPieceCount = 0;
        pawnCount       = 0;
        rookCount       = 0;
        queenCount      = 0;
        cRights         = new int[]{0, 0, 0, 0};
        cRightsLocks    = new int[]{0, 0, 0, 0};
        pawns           = 0;
        knights         = 0;
        bishops         = 0;
        rooks           = 0;
        queens          = 0;
        kings           = 0;
        allPieces       = 0;
        whiteAttacks    = 0;
        blackAttacks    = 0;
        initLists();
    }

    /**
     * Initializes all lists of attacks, occupied squares, and piece positions
     */
    private void initLists() {
        for (int i = 0; i < board.length; i++) {
            int piece = board[i];
            if (piece == Piece.NONE.val()) {
                continue;

            }
            MoveList moves = PieceMover.moves(this, i);
            for (int j = 0; j < moves.length(); j++) {
                Move m = moves.at(j);
                if (m.attack) {
                    addAttack(piece, m.to);
                }
            }
            addPosition(piece, i);
        }
    }

    /**
     * Creates an unlinked copy of the BoardRecord
     * @return a copy of the BoardRecord
     */
    public BoardRecord copy() {
        BoardRecord tempRec     = new BoardRecord();
        tempRec.board           = this.board.clone();
        tempRec.epPawnPos       = this.epPawnPos;
        tempRec.minorPieceCount = this.minorPieceCount;
        tempRec.pawnCount       = this.pawnCount;
        tempRec.rookCount       = this.rookCount;
        tempRec.queenCount      = this.queenCount;
        tempRec.cRights         = this.cRights.clone();
        tempRec.cRightsLocks    = this.cRightsLocks.clone();
        tempRec.pawns           = this.pawns;
        tempRec.knights         = this.knights;
        tempRec.bishops         = this.bishops;
        tempRec.rooks           = this.rooks;
        tempRec.queens          = this.queens;
        tempRec.kings           = this.kings;
        tempRec.allPieces       = this.allPieces;
        tempRec.whiteAttacks    = this.whiteAttacks;
        tempRec.blackAttacks    = this.blackAttacks;
        return tempRec;

    }

    /**
     * Overload:
     * Removes an attack from the attacks lists
     * @param piece piece that has the attack to be removed
     * @param pos target square of attack
     */
    public void removeAttack(Piece piece, int pos) {
        removeAttack(piece.val(), pos);
    }

    /**
     * Removes an attack from the attacks list
     * @param piece integer representation of piece that has the attack to be removed
     * @param pos target square of attack
     */
    public void removeAttack(int piece, int pos) {
        if ((piece & 0b11000) == Piece.BLACK.val()) {
            blackAttacks = BitBoard.deactivateBit(blackAttacks, pos);
        } else {
            whiteAttacks = BitBoard.deactivateBit(whiteAttacks, pos);
        } 
    }

    /**
     * Overload:
     * Adds an attack to the attacks lists
     * @param piece piece that has the attack to be added
     * @param pos target square of attack
     */
    public void addAttack(Piece piece, int pos) {
        addAttack(piece.val(), pos);
    }

    /**
     * Adds an attack to the attacks lists
     * @param piece integer representation of piece that has the attack to be added
     * @param pos target square of attack
     */
    public void addAttack(int piece, int pos) {
        if ((piece & 0b11000) == Piece.BLACK.val()) {
            blackAttacks = BitBoard.activateBit(blackAttacks, pos);
        } else {
            whiteAttacks = BitBoard.activateBit(whiteAttacks, pos);
        } 
    }

    /**
     * Overload:
     * Removes a position from the positions lists
     * @param piece piece whose position will be removed
     * @param pos position to be removed
     */
    public void removePosition(Piece piece, int pos) {
        removePosition(piece.val(), pos);
    }

    /**
     * Removes a position from the positions lists
     * @param piece integer representation of piece whose position will be removed
     * @param pos position to be removed
     */
    public void removePosition(int piece, int pos) {
        if (piece > Piece.WHITE.val()) {
            piece &= 0b111;
        }
        BitBoard.deactivateBit(allPieces, pos);
        switch (piece) {
        case 0:
            break;

        case 1:
            pawns = BitBoard.deactivateBit(pawns, pos);
            pawnCount--;
            break;

        case 2:
            knights = BitBoard.deactivateBit(knights, pos);
            minorPieceCount--;
            break;

        case 3:
            bishops = BitBoard.deactivateBit(bishops, pos);
            minorPieceCount--;
            break;

        case 4:
            rooks = BitBoard.deactivateBit(rooks, pos);
            rookCount--;
            break;

        case 5:
            queens = BitBoard.deactivateBit(queens, pos);
            queenCount--;
            break;

        case 6:
            kings = BitBoard.deactivateBit(kings, pos);
            break;

        default:
            System.err.println("Attempting to remove piece " + piece);
            System.err.println("Could not remove piece position - piece invalid");
            break;

        } 
    }

    /**
     * Overload:
     * Adds a position to the positions lists
     * @param piece piece whose position will be added
     * @param pos position to be added
     */
    public void addPosition(Piece piece, int pos) {
        addPosition(piece.val() & 0b111, pos);
    }

    /**
     * Adds a position to the positions lists
     * @param piece integer representation of piece whose position will be added
     * @param pos position to be added
     */
    public void addPosition(int piece, int pos) {
        if (piece > Piece.WHITE.val()) {
            piece &= 0b111;
        }
        allPieces = BitBoard.activateBit(allPieces, pos);
        switch (piece) {
        case 0:
            break;

        case 1:
            pawns = BitBoard.activateBit(pawns, pos);
            pawnCount++;
            break;

        case 2:
            knights = BitBoard.activateBit(knights, pos);
            minorPieceCount++;
            break;

        case 3:
            bishops = BitBoard.activateBit(bishops, pos);
            minorPieceCount++;
            break;

        case 4:
            rooks = BitBoard.activateBit(rooks, pos);
            rookCount++;
            break;

        case 5:
            queens = BitBoard.activateBit(queens, pos);
            queenCount++;
            break;

        case 6:
            kings = BitBoard.activateBit(kings, pos);
            break;

        default:
            System.err.println("Attempting to save piece " + piece);
            System.err.println("Could not add piece position - piece invalid");
            break;

        }   
    }

    /**
     * Overload:
     * Replaces the position of one piece with the position of another piece
     * @param moving piece that will replace a position
     * @param taken piece whose position will be replaced
     * @param from initial position of moving piece
     * @param to initial position of taken piece
     */
    public void replacePosition(Piece moving, Piece taken, int from, int to) {
        replacePosition(moving.val() & 0b111, taken.val() & 0b111, from, to);
    }
    
    /**
     * Replaces the position of one piece with the position of another piece
     * @param moving integer representation of piece that will replace a position
     * @param taken integer representation of piece whose position will be replaced
     * @param from initial position of moving piece
     * @param to initial position of taken piece
     */
    public void replacePosition(int moving, int taken, int from, int to) {
        removePosition(moving, from);
        addPosition(moving, to);
        if (taken != Piece.NONE.val()) {
            removePosition(taken, to);
        }
    }

    /**
     * Prints the current board state to the terminal (for debugging)
     */
    public void printBoard() {
        for (int i = 0; i < board.length; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            boolean isBlack = (board[i] & 0b11000) == Piece.BLACK.val();
            switch (board[i] & 0b111) {
            case 0:
                System.out.print(". ");
                break;

            case 1:
                System.out.print((isBlack) ? "p " : "P ");
                break;

            case 2:
                System.out.print((isBlack) ? "n " : "N ");
                break;

            case 3:
                System.out.print((isBlack) ? "b " : "B ");
                break;

            case 4:
                System.out.print((isBlack) ? "r " : "R ");
                break;

            case 5:
                System.out.print((isBlack) ? "q " : "Q ");
                break;

            case 6:
                System.out.print((isBlack) ? "k " : "K ");
                break;

            default:
                break;

            }
        }
        System.out.println();
    }

    /**
     * Prints information about the position to the terminal (for debugging)
     */
    public void showPositions() {
        BitBoard.displayBB(allPieces);
    }

    /**
     * Prints all of the attacks for black and white to the terminal (for debugging)
     */
    public void showAttacks() {
        System.out.println("White");
        BitBoard.displayBB(whiteAttacks);
        System.out.println(whiteAttacks);
        System.out.println();
        System.out.println("Black");
        BitBoard.displayBB(blackAttacks);
        System.out.println(blackAttacks);
    }
}
