package com.ang.Engine;

import com.ang.Core.Moves.Move;

/**
 * Class for holding the result of a search conducted by a worker
 */
public class SearchResult {
    public final Move move;
    public final int eval;
    public final int depth;

    /**
     * Constructs a new search result
     * @param move the best move found by the search
     * @param eval the evaluation of the move
     * @param depth the depth to which the worker searched
     */
    public SearchResult(Move move, int eval, int depth) {
        this.move = move;
        this.eval = eval;
        this.depth = depth;
    }
}
