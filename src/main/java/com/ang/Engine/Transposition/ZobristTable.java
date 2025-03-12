package com.ang.Engine.Transposition;

import com.ang.Global;
import com.ang.Core.Piece;

/**
 * Base class for using Zobrist Hashing to represent board positions
 */
public class ZobristTable {
    public int size = 0;

    protected int[] zobristArray;

    /**
     * Constructs a new zobrist table
     * @param size the amount of elements to initialize into the zobrist array
     */
    public ZobristTable(int size) {
        this.zobristArray = new int[size]; 
        for (int i = 0; i < size; i++) {
            zobristArray[i] = (int) Global.pseudoRandom();
        }
    }

    /**
     * Converts a piece and its position into an index into the zobrist table
     * @param piece integer representation of piece
     * @param pos index into a BoardRecord's board[] where the piece is
     * @return index of piece in zobrist table
     */
    protected int indexOfPiece(int piece, int pos) {
        int pieceIndex = (piece & 0b11000) == Piece.WHITE.val()
        ? (piece & 0b111) - 1
        : (piece & 0b111) + 5;

        return (pieceIndex * 64) + pos;
    }
}
