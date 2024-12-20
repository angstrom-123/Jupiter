package com.ang;

import com.ang.Core.Piece;
import com.ang.Engine.Search;

public class Main {
    private final static int    SEARCH_MS       = 1000;
    private final static Piece  ENGINE_COLOUR   = Piece.BLACK;
    private final static int    SQUARE_SIZE     = 45;
    private final static double RENDER_SCALE    = 1.2;
    
    public static void main(String[] args) {
        final Search s = new Search(SEARCH_MS, ENGINE_COLOUR);
        final Game g = new Game(SQUARE_SIZE, RENDER_SCALE);

        g.init(s);
        // g.test();
    }
}
