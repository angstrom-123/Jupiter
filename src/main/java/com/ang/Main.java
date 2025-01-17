package com.ang;

import com.ang.Core.Piece;
import com.ang.Engine.Search;

public class Main {
    private final static int    SQUARE_SIZE     = 45;
    private final static double RENDER_SCALE    = 1.2;
    
    public static void main(String[] args) {
        switch (0) {
        case 0: // player v engine
            initGame(false);
            break;
        case 1: // testing move gen
            initGame(true);
            break;
        case 2: // engine v engine
            initEngineGame();        
            break;
        }
    }

    private static void initGame(boolean test) {
        Search s0 = new Search(2000, Piece.BLACK);
        Game g = new Game(s0);
        if (test) {
            g.test(4000);
        } else {
            g.init(SQUARE_SIZE, RENDER_SCALE);
        }
    }

    private static void initEngineGame() {
        Search s0 = new Search(1000, Piece.BLACK);
        Search s1 = new Search(1000, Piece.WHITE);
        EngineGame eg = new EngineGame(s0, s1);
        eg.init();
    }
}
