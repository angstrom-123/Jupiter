package com.ang.Engine;

public enum TTFlag {
    PV  (2),
    ALL (1),
    CUT (3);

    private int precedence;

    private TTFlag(int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
