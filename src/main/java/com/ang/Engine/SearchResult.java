package com.ang.Engine;

import com.ang.Core.Moves.Move;

public class SearchResult {
    public final Move move;
    public final int eval;
    public final int depth;

    public SearchResult(Move move, int eval, int depth) {
        this.move = move;
        this.eval = eval;
        this.depth = depth;
    }

    public boolean isInvalid() {
        return (move.isInvalid()) || (depth == -1);
    }

    public static SearchResult invalid() {
        return new SearchResult(Move.invalid(), -1, -1);
    }
}
