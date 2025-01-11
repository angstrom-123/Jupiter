package com.ang.Core;

import com.ang.Util.FENReader;
import com.ang.Core.Moves.*;

public class BoardRecord {
    public int[]    board;

    public int      epPawnPos;

    public int[]    cRights;
    public int[]    cRightsLocks;

    public IntList  pawns;
    public IntList  knights;
    public IntList  bishops;
    public IntList  rooks;
    public IntList  queens;
    public IntList  kings;
    public IntList  allPieces;

    public int[]    whiteAttacksP;
    public int[]    whiteAttacksN;
    public int[]    whiteAttacksB;
    public int[]    whiteAttacksR;
    public int[]    whiteAttacksQ;
    public int[]    whiteAttacksK;
    public int[]    blackAttacksP;
    public int[]    blackAttacksN;
    public int[]    blackAttacksB;
    public int[]    blackAttacksR;
    public int[]    blackAttacksQ;
    public int[]    blackAttacksK;
    public int[]    whiteAttacks;
    public int[]    blackAttacks;

    public BoardRecord() {}

    public BoardRecord(String startFEN) {
        board           = FENReader.readFEN(startFEN);

        epPawnPos       = -1;

        cRights         = new int[]{0, 0, 0, 0};
        cRightsLocks    = new int[]{0, 0, 0, 0};

        pawns           = new IntList(16, -1);
        knights         = new IntList(20, -1);
        bishops         = new IntList(20, -1);
        rooks           = new IntList(20, -1);
        queens          = new IntList(18, -1);
        kings           = new IntList( 2, -1);
        allPieces       = new IntList(64, -1);

        whiteAttacksP   = new int[64];
        whiteAttacksN   = new int[64];
        whiteAttacksB   = new int[64];
        whiteAttacksR   = new int[64];
        whiteAttacksQ   = new int[64];
        whiteAttacksK   = new int[64];
        blackAttacksP   = new int[64];
        blackAttacksN   = new int[64];
        blackAttacksB   = new int[64];
        blackAttacksR   = new int[64];
        blackAttacksQ   = new int[64];
        blackAttacksK   = new int[64];
        whiteAttacks    = new int[64];
        blackAttacks    = new int[64];

        initLists();
    }

    private void initLists() {
        for (int i = 0; i < board.length; i++) {
            int piece = board[i];
            // boolean isBlack = (piece & 0b11000) == Piece.BLACK.val();
            if (piece == Piece.NONE.val()) { continue; }

            allPieces.add(i);
            MoveList moves;
            for (int j = 0; j < (moves = PieceMover.moves(this, i)).length(); j++) {
                Move m = moves.at(j);
                if (m.attack) {
                    addAttack(piece, m.to);
                }
            }

            switch (piece & 0b111) {
            case 1:
                pawns.add(i);
                break;
            case 2:
                knights.add(i);
                break;
            case 3:
                bishops.add(i);
                break;
            case 4:
                rooks.add(i);
                break;
            case 5:
                queens.add(i);
                break;
            case 6:
                kings.add(i);
                break;
            default:
                break;
            }
        }

        showPositions();
    }

    public BoardRecord copy() {
        BoardRecord tempRec = new BoardRecord();
        
        tempRec.board           = this.board.clone();
        tempRec.epPawnPos       = this.epPawnPos;
        tempRec.cRights         = this.cRights.clone();
        tempRec.cRightsLocks    = this.cRightsLocks.clone();
        tempRec.pawns           = this.pawns.copy();
        tempRec.knights         = this.knights.copy();
        tempRec.bishops         = this.bishops.copy();
        tempRec.rooks           = this.rooks.copy();
        tempRec.queens          = this.queens.copy();
        tempRec.kings           = this.kings.copy();
        tempRec.allPieces       = this.allPieces.copy();
        tempRec.whiteAttacksP   = this.whiteAttacksP.clone();
        tempRec.whiteAttacksN   = this.whiteAttacksN.clone();
        tempRec.whiteAttacksB   = this.whiteAttacksB.clone();
        tempRec.whiteAttacksR   = this.whiteAttacksR.clone();
        tempRec.whiteAttacksQ   = this.whiteAttacksQ.clone();
        tempRec.whiteAttacksK   = this.whiteAttacksK.clone();
        tempRec.blackAttacksP   = this.blackAttacksP.clone();
        tempRec.blackAttacksN   = this.blackAttacksN.clone();
        tempRec.blackAttacksB   = this.blackAttacksB.clone();
        tempRec.blackAttacksR   = this.blackAttacksR.clone();
        tempRec.blackAttacksQ   = this.blackAttacksQ.clone();
        tempRec.blackAttacksK   = this.blackAttacksK.clone();

        tempRec.whiteAttacks    = this.whiteAttacks.clone();
        tempRec.blackAttacks    = this.blackAttacks.clone();

        return tempRec;
    }

    public int minorPieceCount() {
        return (knights.length() + bishops.length());
    }

    public void removeAttack(Piece piece, int pos) {
        removeAttack(piece.val(), pos);
    }
    public void removeAttack(int piece, int pos) {
        changeAttack(piece, pos, -1);
    }

    public void addAttack(Piece piece, int pos) {
        addAttack(piece.val(), pos);
    }
    public void addAttack(int piece, int pos) {
        changeAttack(piece, pos, 1);
    }

    // TODO : visualize all attacks for all pieces to test new add / remove funcs

    private void changeAttack(int piece, int pos, int delta) {
        boolean isBlack = (piece & 0b11000) == Piece.BLACK.val();
        int type = piece & 0b111;
        
        switch (type) {
        case 1:
            if (isBlack) {
                blackAttacksP[pos] += delta;
            } else {
                whiteAttacksP[pos] += delta;
            }
            break;
        case 2:
            if (isBlack) {
                blackAttacksN[pos] += delta;
            } else {
                whiteAttacksN[pos] += delta;
            }
            break;
        case 3:
            if (isBlack) {
                blackAttacksB[pos] += delta;
            } else {
                whiteAttacksB[pos] += delta;
            }
            break;
        case 4:
            if (isBlack) {
                blackAttacksR[pos] += delta;
            } else {
                whiteAttacksR[pos] += delta;
            }
            break;
        case 5:
            if (isBlack) {
                blackAttacksQ[pos] += delta;
            } else {
                whiteAttacksQ[pos] += delta;
            }
            break;
        case 6:
            if (isBlack) {
                blackAttacksK[pos] += delta;
            } else {
                whiteAttacksK[pos] += delta;
            }
            break;
        default:
            return;
        }
        if (isBlack) {
            blackAttacks[pos] += delta;
        } else {
            whiteAttacks[pos] += delta;
        }
    }

    public void removePosition(Piece piece, int pos) {
        removePosition(piece.val(), pos);
    }
    public void removePosition(int piece, int pos) {
        if (piece > Piece.WHITE.val()) {
            piece &= 0b111;
        }
        allPieces.rem(pos);
        switch (piece) {
        case 0:
            break;
        case 1:
            pawns.rem(pos);
            break;
        case 2:
            knights.rem(pos);
            break;
        case 3:
            bishops.rem(pos);
            break;
        case 4:
            rooks.rem(pos);
            break;
        case 5:
            queens.rem(pos);
            break;
        case 6:
            kings.rem(pos);
            break;
        default:
            System.err.println("Attempting to remove piece "+piece);
            System.err.println("Could not remove piece position - piece invalid");
            break;
        } 
    }

    public void addPosition(Piece piece, int pos) {
        addPosition(piece.val() & 0b111, pos);
    }

    public void addPosition(int piece, int pos) {
        if (piece > Piece.WHITE.val()) {
            piece &= 0b111;
        }
        allPieces.add(pos);
        switch (piece) {
        case 0:
            break;
        case 1:
            pawns.add(pos);
            break;
        case 2:
            knights.add(pos);
            break;
        case 3:
            bishops.add(pos);
            break;
        case 4:
            rooks.add(pos);
            break;
        case 5:
            queens.add(pos);
            break;
        case 6:
            kings.add(pos);
            break;
        default:
            System.err.println("Attempting to save piece "+piece);
            System.err.println("Could not add piece position - piece invalid");
            break;
        }   
    }

    public void replacePosition(Piece moving, Piece taken, int from, int to) {
        replacePosition(moving.val() & 0b111, taken.val() & 0b111, from, to);
    }

    public void replacePosition(int moving, int taken, int from, int to) {
        removePosition(moving, from);
        addPosition(moving, to);
        if (taken != Piece.NONE.val()) {
            removePosition(taken, to);
        }
    }

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

    public void showPositions() {
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(whiteAttacks[i]+" ");
        }
        System.out.println();

        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(blackAttacks[i]+" ");
        }
        System.out.println();

        printBoard();

        System.out.println();
        // System.out.println("ep pawn "+epPawnPos);
    }

    public void showAttacks() {
        System.out.println();
        System.out.println();
        System.out.println("Pawns");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksP[i] + " ");
        }
        System.out.println();
        System.out.println("Black");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksP[i] + " ");
        }

        System.out.println();
        System.out.println();
        System.out.println("Knights");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksN[i] + " ");
        }
        System.out.println();
        System.out.println("Black");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksN[i] + " ");
        }

        System.out.println();
        System.out.println();
        System.out.println("Bishops");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksB[i] + " ");
        }
        System.out.println("Black");
        System.out.println();
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksB[i] + " ");
        }

        System.out.println();
        System.out.println();
        System.out.println("Rooks");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksR[i] + " ");
        }
        System.out.println("Black");
        System.out.println();
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksR[i] + " ");
        }

        System.out.println();
        System.out.println();
        System.out.println("Queens");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksQ[i] + " ");
        }
        System.out.println("Black");
        System.out.println();
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksQ[i] + " ");
        }

        System.out.println();
        System.out.println();
        System.out.println("Kings");
        System.out.println("White");
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.whiteAttacksK[i] + " ");
        }
        System.out.println("Black");
        System.out.println();
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(this.blackAttacksK[i] + " ");
        }
        System.out.println();
    }
}
