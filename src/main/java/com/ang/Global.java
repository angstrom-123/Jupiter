package com.ang;

import com.ang.Engine.Zobrist.RepetitionTable;
import com.ang.Engine.Zobrist.TranspositionTable;

public class Global {
    public final static double INFINITY = Double.MAX_VALUE;

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
