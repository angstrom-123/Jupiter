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

    public static Piece opposite(int col) {
        if (col == Piece.WHITE.val()) {
            return Piece.BLACK;
        }
        return Piece.WHITE;
    }
}
