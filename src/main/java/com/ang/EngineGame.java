package com.ang;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Moves.Move;
import com.ang.Engine.Search;

public class EngineGame {
    private Search whiteSearch;
    private Search blackSearch;
    private BoardRecord gameBoard;

    public EngineGame(Search whiteSearch, Search blackSearch) {
        this.whiteSearch = whiteSearch;
        this.blackSearch = blackSearch;
    }

    public void init() {
        String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        gameBoard = new BoardRecord(startFEN);

        gameLoop();
    }

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
