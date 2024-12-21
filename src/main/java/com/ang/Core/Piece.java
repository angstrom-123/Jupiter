package com.ang.Core;

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

    public Piece opposite() {
        if (this == Piece.BLACK) {
            return Piece.WHITE;
        } else if (this == Piece.WHITE) {
            return Piece.BLACK;
        } else {
            return Piece.NONE;
        }
    }
}
