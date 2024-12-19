package com.ang.Opponent;

public enum Node {
    PV  (3),
    ALL (2),
    CUT (1);

    private int precedence;

    private Node(int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
