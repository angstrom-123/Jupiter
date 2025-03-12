package com.ang.Core;

/**
 * Enum for the different pieces and colours in chess
 */
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

    /**
     * Constructs a piece with an integer representation (used in BoardRecord) and
     * a static evaluation (used in evaluation functions in Search)
     * @param val integer representation of piece
     * @param staticEval base (static) evaluation for the piece
     */
    private Piece (int val, int staticEval) {
        this.val = val;
        this.staticEval = staticEval;
    }

    /**
     * @return the integer representation of the piece
     */
    public int val() {
        return val;

    }

    /**
     * @return the static evaluation of the piece
     */
    public int staticEval() {
        return staticEval;

    }

    /**
     * @return opposite colour of instance (none if not BLACK, or WHITE)
     */
    public Piece opposite() {
        if (this == Piece.BLACK) {
            return Piece.WHITE;

        } else if (this == Piece.WHITE) {
            return Piece.BLACK;

        } else {
            return Piece.NONE;

        }
    }

    /**
     * Finds the static evaluation of an integer representation of a piece
     * @param piece integer representation of piece
     * @return its static evaluation
     */
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

    /**
     * Finds the opposite colour of an integer representation of a colour
     * @param col integer representation of a colour to find opposite of
     * @return opposite colour
     */
    public static Piece opposite(int col) {
        if (col == Piece.WHITE.val()) {
            return Piece.BLACK;

        }
        return Piece.WHITE;
        
    }
}
