package com.ang;

import com.ang.Opponent.Engine;

public class Main {
    final static int SQUARE_SIZE = 45;
    final static double RENDER_SCALE = 1.2;
    
    public static void main(String[] args) {
        Engine e = new Engine(2000, Piece.BLACK);
        Game g = new Game(SQUARE_SIZE, RENDER_SCALE);
        g.init(e);
    }
}
