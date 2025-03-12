package com.ang;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Moves.Move;
import com.ang.Engine.Search;

/**
 * Class handling a game played between 2 copies of my engine
 */
public class EngineGame {
    private Search whiteSearch;
    private Search blackSearch;
    private BoardRecord gameBoard;

    /**
     * Constructs a game between 2 engines
     * @param whiteSearch the engine (move search) to play as white
     * @param blackSearch the engine to play as black
     */
    public EngineGame(Search whiteSearch, Search blackSearch) {
        this.whiteSearch = whiteSearch;
        this.blackSearch = blackSearch;
    }

    /**
     * Initializes and starts the game
     */
    public void init() {
        String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        gameBoard = new BoardRecord(startFEN);

        gameLoop();
    }

    /**
     * Allows each engine to make a move until the game concludes
     */
    private void gameLoop() {
        boolean endGame = false;
        while (!endGame) {
            Move whiteMove = whiteSearch.generateMove(gameBoard);
            if (!Board.tryMove(gameBoard, whiteMove)) {
                endGame = true;
                System.err.println("Engine could not make a move as white");
                return;
            }
            gameBoard.printBoard();

            Move blackMove = blackSearch.generateMove(gameBoard);
            if (!Board.tryMove(gameBoard, blackMove)) {
                endGame = true;
                System.err.println("Engine could not make a move as black");
                return;
            }
            gameBoard.printBoard();
        }
    }
}
