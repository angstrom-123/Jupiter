package com.ang.Core;

public enum Piece {
    NONE    ( 0, 0),
    PAWN    ( 1, 100),
    KNIGHT  ( 2, 320),
    BISHOP  ( 3, 330),
    ROOK    ( 4, 500),
    QUEEN   ( 5, 900),
    KING    ( 6, 20000),
    WHITE   ( 8, 0),
    BLACK   (16, 0);

    private int val;
    private int staticEval;

    private Piece (int val, int staticEval) {
        this.val = val;
        this.staticEval = staticEval;
    }

    public int val() {
        return val;
    }

    public int staticEval() {
        return staticEval;
    }

    public Piece opposite() {
        if (this == Piece.BLACK) {
            return Piece.WHITE;
        } else if (this == Piece.WHITE) {
            return Piece.BLACK;
        } else {
            return Piece.NONE;
        }
    }

    public static int staticEval(int piece) {
        switch (piece) {
        case 1:
            return Piece.PAWN.staticEval();
        case 2:
            return Piece.KNIGHT.staticEval();
        case 3:
            return Piece.BISHOP.staticEval();
        case 4:
            return Piece.ROOK.staticEval();
        case 5:
            return Piece.QUEEN.staticEval();
        case 6:
            return Piece.KING.staticEval();
        default:
            return 0;
        }
    }

    public static Piece opposite(int col) {
        if (col == Piece.WHITE.val()) {
            return Piece.BLACK;
        }
        return Piece.WHITE;
    }
}
