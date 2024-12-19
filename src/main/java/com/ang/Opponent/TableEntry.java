package com.ang.Opponent;

import com.ang.Moves.Move;

public class TableEntry {
    public Node     nodeType;
    public Move     bestMove;
    public double   eval;
    public int      depth;
    public int      age;
    
    public TableEntry() {};

    public TableEntry(Node nodeType, Move bestMove, double eval, int depth, int age) {
        this.nodeType   = nodeType;
        this.bestMove   = bestMove;
        this.eval       = eval;
        this.depth      = depth;
        this.age        = age;
    }
}
