package com.ang.Engine;

import com.ang.Core.Moves.Move;

public class TableEntry {
    public SearchNode   nodeType;
    public Move         bestMove;
    public double       eval;
    public int          depth;
    public int          age;

    public TableEntry() {};

    public TableEntry(SearchNode nodeType, Move bestMove, double eval, int depth, int age) {
        this.nodeType   = nodeType;
        this.bestMove   = bestMove;
        this.eval       = eval;
        this.depth      = depth;
        this.age        = age;
    }
}
