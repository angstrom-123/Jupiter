package com.ang;

public class Global {
    public final static double INFINITY = Double.MAX_VALUE;

    public static long pseudoRandom() {
        long max = (long) Math.pow(2, 64);
        long min = 0L;
        // TODO : implement a beter prng
        return (long) Math.random() * (max - min) + min; 
    }
}
