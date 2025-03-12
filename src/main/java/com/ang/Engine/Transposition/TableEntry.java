package com.ang.Engine.Transposition;

import com.ang.Core.Moves.Move;

/**
 * Class for storing information about a position in the transposition table
 */
public class TableEntry {
    public TTFlag   nodeType;
    public Move     bestMove;
    public int      eval;
    public int      depth;
    public int      age;

    /**
     * Creates a new entry to be saved to the transposition table
     * @param nodeType type of node to be saved (PV, ALL, CUT)
     * @param bestMove the best move in the position that is saved
     * @param eval the evaluation at the position
     * @param depth the depth to which the engine searched to find the best move
     * @param age the age of the node (currently unused - default to 0)
     */
    public TableEntry(TTFlag nodeType, Move bestMove, int eval, int depth, int age) {
        this.nodeType   = nodeType;
        this.bestMove   = bestMove;
        this.eval       = eval;
        this.depth      = depth;
        this.age        = age;
    }
}
