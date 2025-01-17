package com.ang.Engine.Transposition;

import com.ang.Global;
import com.ang.Core.Piece;

public class ZobristTable {
    public int size = 0;

    protected int[] zobristArray;

    public ZobristTable(int size) {
        this.zobristArray = new int[size]; 
        for (int i = 0; i < size; i++) {
            zobristArray[i] = (int) Global.pseudoRandom();
        }
    }

    protected int indexOfPiece(int piece, int pos) {
        int pieceIndex = (piece & 0b11000) == Piece.WHITE.val()
        ? (piece & 0b111) - 1
        : (piece & 0b111) + 5;

        return (pieceIndex * 64) + pos;
    }
}
