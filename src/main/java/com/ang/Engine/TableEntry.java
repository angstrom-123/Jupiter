package com.ang.Engine;

import com.ang.Core.Moves.Move;

public class TableEntry {
    public TTFlag   nodeType;
    public Move         bestMove;
    public int          eval;
    public int          depth;
    public int          age;

    public TableEntry() {};

    public TableEntry(TTFlag nodeType, Move bestMove, int eval, int depth, int age) {
        this.nodeType   = nodeType;
        this.bestMove   = bestMove;
        this.eval       = eval;
        this.depth      = depth;
        this.age        = age;
    }
}
