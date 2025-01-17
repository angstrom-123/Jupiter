package com.ang;

import com.ang.Engine.Transposition.RepetitionTable;
import com.ang.Engine.Transposition.TranspositionTable;

public class Global {
    public final static int INFINITY = Integer.MAX_VALUE;
    public final static int LARGE_NUM = Integer.MAX_VALUE / 2;

    public static TranspositionTable tTable = new TranspositionTable();
    public static RepetitionTable repTable = new RepetitionTable();
    public static int fiftyMoveCounter = 0;
    // public static int maxRepetitions = 0;

    public static long quiesces = 0;
    public static long searches = 0;

    public static long pseudoRandom() {
        long max = (long) Math.pow(2, 64);
        long min = 0L;
        // TODO : implement a beter prng
        return (long) (Math.random() * (max - min) + min); 
    }
}
