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
        whiteAttacks    = new int[64];
        blackAttacks    = new int[64];

        initLists();
    }

    private void initLists() {
        for (int i = 0; i < board.length; i++) {
            int piece = board[i];
            boolean isBlack = (piece & 0b11000) == Piece.BLACK.val();
            if (piece == Piece.NONE.val()) { continue; }

            allPieces.add(i);
            MoveList moves;
            for (int j = 0; j < (moves = Board.pieceMoves(this, i)).length(); j++) {
                if (moves.at(j).attack) {
                    if (isBlack) {
                        blackAttacks[moves.at(j).to]++;
                    } else {
                        whiteAttacks[moves.at(j).to]++;
                    }
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
        tempRec.whiteAttacks    = this.whiteAttacks.clone();
        tempRec.blackAttacks    = this.blackAttacks.clone();

        return tempRec;
    }

    public int minorPieceCount() {
        return (knights.length() + bishops.length());
    }

    public void removeAttack(Piece col, int pos) {
        removeAttack(col.val(), pos);
    }

    public void removeAttack(int col, int pos) {
        if (col == Piece.WHITE.val()) {
            whiteAttacks[pos]--;
        } else {
            blackAttacks[pos]--;
        }
    }

    public void addAttack(Piece col, int pos) {
        addAttack(col.val(), pos);
    }

    public void addAttack(int col, int pos) {
        if (col == Piece.WHITE.val()) {
            whiteAttacks[pos]++;
        } else {
            blackAttacks[pos]++;
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
                    System.out.print(' ');
                    break;
                case 1:
                    System.out.print((isBlack) ? 'p' : 'P');
                    break;
                case 2:
                    System.out.print((isBlack) ? 'n' : 'N');
                    break;
                case 3:
                    System.out.print((isBlack) ? 'b' : 'B');
                    break;
                case 4:
                    System.out.print((isBlack) ? 'r' : 'R');
                    break;
                case 5:
                    System.out.print((isBlack) ? 'q' : 'Q');
                    break;
                case 6:
                    System.out.print((isBlack) ? 'k' : 'K');
                    break;
                default:
                    break;
            }
        }
        System.out.println();
    }

    public void showPositions() {
        char[] outArr = new char[64];
        for (int i = 0; i < 64; i++) {
            outArr[i] = ' ';
        }
        for (int pos : pawns.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'p';
        }
        for (int pos : knights.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'n';
        }
        for (int pos : bishops.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'b';
        }
        for (int pos : rooks.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'r';
        }
        for (int pos : queens.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'q';
        }
        for (int pos : kings.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'k';
        }
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(outArr[i]);
        }
        System.out.println();

        for (int i = 0; i < 64; i++) {
            outArr[i] = ' ';
        }
        for (int pos : allPieces.elements) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'x';
        }
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(outArr[i]);
        }
        System.out.println();

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

        System.out.println();
        System.out.println("ep pawn "+epPawnPos);
    }
}
