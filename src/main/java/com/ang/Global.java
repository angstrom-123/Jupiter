package com.ang;

import com.ang.Engine.Transposition.RepetitionTable;
import com.ang.Engine.Transposition.TranspositionTable;

/**
 * Class storing globally accessible variables, constants, and functions
 */
public class Global {
    public final static int INFINITY = Integer.MAX_VALUE;
    public final static int LARGE_NUM = Integer.MAX_VALUE / 2;

    public static TranspositionTable tTable = new TranspositionTable();
    public static RepetitionTable repTable = new RepetitionTable();
    public static int fiftyMoveCounter = 0;
    public static int ttHits = 0;
    public static int ttColisions = 0;

    /**
     * @return a pseudo-random long 
     */
    public static long pseudoRandom() {
        long max = (long) Math.pow(2, 64);
        long min = 0L;
        return (long) (Math.random() * (max - min) + min); 
    }
}
