package com.ang.Util;

import com.ang.Board;
import com.ang.Piece;
import com.ang.Moves.MoveList;

public class BoardRecord {
    public int[] board;

    public int epPawnPos;

    public int[] pawns;
    public int[] knights;
    public int[] bishops;
    public int[] rooks;
    public int[] queens;
    public int[] kings;
    public int[] allPieces;
    public int[] movedPieces;
    public int[] whiteAttacks;
    public int[] blackAttacks;

    private int pawnsEnd;
    private int knightsEnd;
    private int bishopsEnd;
    private int rooksEnd;
    private int queensEnd;
    private int kingsEnd;
    private int allPiecesEnd;
    private int movedPiecesEnd;

    public BoardRecord() {};

    public BoardRecord(String FENString) {
        board = FENReader.readFEN(FENString);
        
        epPawnPos = -1;

        pawns = new int[16];
        knights = new int[4];
        bishops = new int[4];
        rooks = new int[4];
        queens = new int[18]; // pawns can promote
        kings = new int[2];
        allPieces = new int[32];
        movedPieces = new int[32];
        whiteAttacks = new int[64];
        blackAttacks = new int[64];

        pawnsEnd = 0;
        knightsEnd = 0;
        bishopsEnd = 0;
        rooksEnd = 0;
        queensEnd = 0;
        kingsEnd = 0;
        allPiecesEnd = 0;
        movedPiecesEnd = 0;

        for (int i = 0; i < board.length; i++) {
            if (i < queens.length) {
                queens[i] = -1;
            }

            int piece = board[i] & 0b00111;

            MoveList moves = Board.pieceMoves(this, i);
            for (int j = 0; j < moves.length(); j++) {
                if (moves.at(j).attack) {
                    attacksArrAdd(board[i] & 0b11000, moves.at(j).to);
                }
            }
            
            if (piece != Piece.NONE.val()) {
                allPieces[allPiecesEnd] = i;
                movedPieces[allPiecesEnd] = - 1;
                allPiecesEnd++; 
            }
            switch (piece) {
            case 1:
                pawns[pawnsEnd++] = i;
                break;
            case 2:
                knights[knightsEnd++] = i;
                break;
            case 3:
                bishops[bishopsEnd++] = i;
                break;
            case 4:
                rooks[rooksEnd++] = i;
                break;
            case 5:
                queens[queensEnd++] = i;
                break;
            case 6:
                kings[kingsEnd++] = i;
                break;
            default:
                break;
            }
        }

        showPositions();
    }

    public BoardRecord copy() {
        BoardRecord temp = new BoardRecord();

        temp.board = this.board.clone();

        temp.epPawnPos = this.epPawnPos;

        temp.pawns = this.pawns.clone();
        temp.knights = this.knights.clone();
        temp.bishops = this.bishops.clone();
        temp.rooks = this.rooks.clone();
        temp.queens = this.queens.clone();
        temp.kings = this.kings.clone();
        temp.allPieces = this.allPieces.clone();
        temp.movedPieces = this.movedPieces.clone();
        temp.whiteAttacks = this.whiteAttacks.clone();
        temp.blackAttacks = this.blackAttacks.clone();

        temp.pawnsEnd = this.pawnsEnd;
        temp.knightsEnd = this.knightsEnd;
        temp.bishopsEnd = this.bishopsEnd;
        temp.rooksEnd = this.rooksEnd;
        temp.queensEnd = this.queensEnd;
        temp.kingsEnd = this.kingsEnd;
        temp.allPiecesEnd = this.allPiecesEnd;
        temp.movedPiecesEnd = this.movedPiecesEnd;

        return temp;
    }

    public int minorPieceCount() {
        return knightsEnd + bishopsEnd;
    }

    public void attacksArrAdd(int col, int pos) {
        Piece colour = (col == Piece.WHITE.val()) ? Piece.WHITE : Piece.BLACK;
        switch (colour) {
        case WHITE:
            whiteAttacks[pos]++;
            break;
        case BLACK:
            blackAttacks[pos]++;
            break;
        default:
            System.err.println("Bad colour at attack array add");
            break;
        }
    }

    public void attacksArrRemove(int col, int pos) {
        Piece colour = (col == Piece.WHITE.val()) ? Piece.WHITE : Piece.BLACK;
        switch (colour) {
        case WHITE:
            whiteAttacks[pos]--;
            break;
        case BLACK:
            blackAttacks[pos]--;
            break;
        default:
            System.err.println("Bad colour at attack array remove");
            break;
        }
    }

    public void movedArrAdd(int pos) {
        movedPieces[movedPiecesEnd++] = pos;
    }

    public void movedArrRemove(int pos) {
        for (int i = 0; i < movedPiecesEnd - 1; i++) {
            if (movedPieces[i] == pos) {
                movedPiecesEnd--;
                movedPieces[i] = movedPieces[movedPiecesEnd];
                movedPieces[movedPiecesEnd] = -1;
            }
        }
    }

    public void posArrAdd(int piece, int pos) {
        int[] movingArr;
        int movingArrEnd;
        switch (piece & 0b111) {
        case 1:
            movingArr = pawns;
            movingArrEnd = pawnsEnd++;
            break;
        case 2:
            movingArr = knights;
            movingArrEnd = knightsEnd++;
            break;
        case 3:
            movingArr = bishops;
            movingArrEnd = bishopsEnd++;
            break;
        case 4:
            movingArr = rooks;
            movingArrEnd = rooksEnd++;
            break;
        case 5:
            movingArr = queens;
            movingArrEnd = queensEnd++;
            break;
        case 6:
            movingArr = kings;
            movingArrEnd = kingsEnd++;
            break;
        default:
            return;
        }

        movingArr[movingArrEnd] = pos;
        allPieces[allPiecesEnd++] = pos;
    }

    public void posArrReplace(int moving, int taken, int from, int to) {
        posArrRemove(moving, from);
        posArrAdd(moving, to);
        
        posArrRemove(taken, to);
    }
    
    public void posArrRemove(int piece, int pos) {
        int[] movingArr;
        int movingArrEnd;
        switch (piece & 0b111) {
        case 1:
            movingArr = pawns;
            movingArrEnd = --pawnsEnd;
            break;
        case 2:
            movingArr = knights;
            movingArrEnd = --knightsEnd;
            break;
        case 3:
            movingArr = bishops;
            movingArrEnd = --bishopsEnd;
            break;
        case 4:
            movingArr = rooks;
            movingArrEnd = --rooksEnd;
            break;
        case 5:
            movingArr = queens;
            movingArrEnd = --queensEnd;
            break;
        case 6:
            movingArr = kings;
            movingArrEnd = --kingsEnd;
            break;
        default:
            return;
        }

        for (int i = 0; i < movingArrEnd + 1; i++) {
            if (movingArr[i] == pos) {
                movingArr[i] = movingArr[movingArrEnd];
                movingArr[movingArrEnd] = -1;
                break;
            }
        }
        for (int i = 0; i < allPiecesEnd; i++) {
            if (allPieces[i] == pos) {
                allPiecesEnd--;
                allPieces[i] = allPieces[allPiecesEnd];
                allPieces[allPiecesEnd] = -1;
                break;
            }
        }
    }
 
    public void showPositions() {
        char[] outArr = new char[64];
        for (int i = 0; i < 64; i++) {
            outArr[i] = ' ';
        }
        for (int pos : pawns) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'p';
        }
        for (int pos : knights) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'n';
        }
        for (int pos : bishops) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'b';
        }
        for (int pos : rooks) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'r';
        }
        for (int pos : queens) {
            if (pos == -1) {
                break;
            }
            outArr[pos] = 'q';
        }
        for (int pos : kings) {
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

        // for (int i = 0; i < 64; i++) {
        //     outArr[i] = ' ';
        // }
        // for (int i = 0; i < allPiecesEnd; i++) {
        //     outArr[allPieces[i]] = 'x';
        // }
        // for (int i = 0; i < 64; i++) {
        //     if (i % 8 == 0) {
        //         System.out.println();
        //     }
        //     System.out.print(outArr[i]);
        // }
        // System.out.println();

        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(whiteAttacks[i]);
        }
        System.out.println();

        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            System.out.print(blackAttacks[i]);
        }
        System.out.println();

    }
}
