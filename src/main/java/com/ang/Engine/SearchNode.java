package com.ang.Engine;

public enum SearchNode {
    PV  (3),
    ALL (2),
    CUT (1);

    private int precedence;

    private SearchNode(int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
