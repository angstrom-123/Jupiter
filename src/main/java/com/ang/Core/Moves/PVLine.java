package com.ang.Core.Moves;

/**
 * Class holding the principal variation found in a search
 */
public class PVLine {
    private final int maxLength = 1000;

    public int length = 0;
    public Move[] moves = new Move[maxLength];
    public String[] algebraics = new String[maxLength];
}
