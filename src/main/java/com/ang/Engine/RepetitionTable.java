package com.ang.Engine;

import java.util.HashMap;

import com.ang.Core.BoardRecord;

public class RepetitionTable extends TranspositionTable {
    private HashMap<Integer, Integer> history = new HashMap<Integer, Integer>();

    public RepetitionTable() {
        super();
    }

    public int saveRepetition(BoardRecord rec, int moveCol) {
        return saveRepetition(super.zobristHash(rec, moveCol));
    }
    public int saveRepetition(int hash) {
        int repetitions = history.getOrDefault(hash, 0);
        repetitions++;
        if (repetitions == 1) {
            history.put(hash, repetitions);
        } else {
            history.remove(hash);
            repetitions++;
            history.put(hash, repetitions);
        }
        return repetitions;
    }

    public int checkRepetitions(BoardRecord rec, int moveCol) {
        return checkRepetitions(super.zobristHash(rec, moveCol));
    }
    public int checkRepetitions(int hash) {
        return history.getOrDefault(hash, 0);
    }
}
