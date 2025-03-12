package com.ang;

import com.ang.Core.Piece;

/**
 * Main class
 */
public class Main {
    private final static int    SQUARE_SIZE     = 45;
    private final static double RENDER_SCALE    = 1.2;
    
    /**
     * Initializes a game between a player
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Piece playerCol = parseArgs(args);
        Game g = new Game(playerCol);
        g.init(SQUARE_SIZE, RENDER_SCALE);
    }

    /**
     * Extracts possible flags from command line arguments specifying the
     * player's piece colour.
     * @param args the command line arguments passed in from main
     * @return black if flags matched black, else white
     */
    public static Piece parseArgs(String[] args) {
        if (args.length == 0) {
            return Piece.WHITE;

        }
        switch (args[0]) {
        case "-w": case "-W": case "-White": case "-white":
            return Piece.WHITE;

        case "-b": case "-B": case "-Black": case "-black":
            return Piece.BLACK;

        default:
            return Piece.WHITE;

        }
    }
}
