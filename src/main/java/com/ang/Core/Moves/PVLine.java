package com.ang.Core.Moves;

public class PVLine {
    private final int maxLength = 30;

    public int length = 0;
    public Move[] moves = new Move[maxLength];
    public String[] algebraics = new String[maxLength];
}
