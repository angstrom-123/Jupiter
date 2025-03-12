package com.ang;

import com.ang.Core.BitBoard;
import com.ang.Core.Piece;
import com.ang.Engine.Search;

/**
 * Main class
 */
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
        case 3:
            testBitBoard();
            break;
        }
    }

    /**
     * Initializes a chess game between a player and the engine
     * @param test boolean - run move gen test or just regular game
     */
    private static void initGame(boolean test) {
        Search s0 = new Search(2000, Piece.BLACK);
        Game g = new Game(s0);
        if (test) {
            g.test(4000);
        } else {
            g.init(SQUARE_SIZE, RENDER_SCALE);
        }
    }

    /**
     * Initializes a chess game between 2 engines
     */
    private static void initEngineGame() {
        Search s0 = new Search(1000, Piece.BLACK);
        Search s1 = new Search(1000, Piece.WHITE);
        EngineGame eg = new EngineGame(s0, s1);
        eg.init();
    }

    private static void testBitBoard() {
        long bb = 0;
        bb = BitBoard.activateBit(bb, 10);
        System.out.println(bb);
        bb = BitBoard.activateBit(bb, 15);
        System.out.println(bb);
        bb = BitBoard.activateBit(bb, 24);
        System.out.println(bb);
        bb = BitBoard.activateBit(bb, 63);
        System.out.println(bb);
        bb = BitBoard.activateBit(bb, 0);
        System.out.println(bb);
        BitBoard.displayBB(bb);
        printBinary(bb);
        for (int i : BitBoard.setBits(bb)) {
            if (i == -1) break;
            System.out.print(i + ", ");
        }
        System.out.println();
    }

    private static void printBinary(long num) {
        for (int i = 63; i > -1; i--) {
            if ((i + 1) % 8 == 0) {
                System.out.print("_");
            }
            long checkBit = 1L << i;
            if ((num & checkBit) == checkBit) {
                System.out.print("1");
            } else {
                System.out.print("0");
            }
        }
        System.out.println();
    }
}
