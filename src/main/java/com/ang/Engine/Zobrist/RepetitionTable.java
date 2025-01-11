package com.ang.Engine.Zobrist;

import java.util.HashMap;

import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;

public class RepetitionTable extends ZobristTable{
    private HashMap<Integer, Integer> history = new HashMap<Integer, Integer>();

    public RepetitionTable() {
        // 12 pieces * 64 squares
        super(768);  
    }

    public int zobristHash(BoardRecord rec) {
        int h = 0;
        
        for (int i = 0; i < rec.board.length; i++) {
            int piece = rec.board[i];
            if (piece != Piece.NONE.val()) {
                h ^= zobristArray[indexOfPiece(piece, i)];
            }
        }
        return h; 
    }

    public int saveRepetition(BoardRecord rec) {
        return saveRepetition(zobristHash(rec));
    }
    public int saveRepetition(int hash) {
        int repCount = history.getOrDefault(hash, 0);
        if (repCount == 0) {
            size++;
            history.put(hash, 1);
        } else {
            history.remove(hash);
            repCount++;
            history.put(hash, repCount);
        }
        return repCount;
    }

    public int checkRepetitions(BoardRecord rec) {
        return checkRepetitions(zobristHash(rec));
    }
    public int checkRepetitions(int hash) {
        return history.getOrDefault(hash, 0);
    }
}
