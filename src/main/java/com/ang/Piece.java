package com.ang;

public enum Piece {
    NONE    ( 0),
    PAWN    ( 1),
    KNIGHT  ( 2),
    BISHOP  ( 3),
    ROOK    ( 4),
    QUEEN   ( 5),
    KING    ( 6),
    WHITE   ( 8),
    BLACK   (16);

    private int val;

    private Piece (int val) {
        this.val = val;
    }

    public int val() {
        return val;
    }
}
