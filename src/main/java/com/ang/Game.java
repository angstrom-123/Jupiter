package com.ang;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Core.Moves.*;
import com.ang.Engine.Search;
import com.ang.Graphics.Renderer;
import com.ang.Util.GameInterface;

// TODO : implement checkmate

public class Game implements GameInterface {
    private int             squareSize;
    private double          renderScale;
    private int             selected;
    private boolean         playerCanMove;
    private MoveList        legalMoves;
    private int             playerCol;
    private int             engineCol;

    public BoardRecord   gameBoard;
    public Renderer         renderer;
    public Search           engineSearch;
    
    public Game(Search engineSearch) {
        this.engineSearch = engineSearch;
        this.engineCol = engineSearch.engineCol;
        this.playerCol = (engineCol == Piece.WHITE.val()) 
        ? Piece.BLACK.val()
        : Piece.WHITE.val();
    }

    public void init(int squareSize, double renderScale) {
        this.squareSize     = 45;
        this.renderScale    = 1.2;

        selected            = -1;
        legalMoves          = new MoveList(0);
        String startFEN     = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        // String startFEN     = "3r4/3r4/3k4/8/8/3K4/8/8";
        gameBoard           = new BoardRecord(startFEN);
        renderer            = new Renderer(squareSize, renderScale, this);

        renderer.drawBoard();
        renderer.drawAllSprites(gameBoard);

        if (engineCol == Piece.WHITE.val()) {
            playerCanMove = false;
            Move engineMove = engineSearch.generateMove(gameBoard);
            if (!Board.tryMove(gameBoard, engineMove)) {
                System.err.println("Engine could not make a valid move");
                return;  
            }
            renderer.drawBoard();
            renderer.drawAllSprites(gameBoard);
        }
        playerCanMove = true;
    }

    public void test(int time) {
        String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        BoardRecord testRecord = new BoardRecord(startFEN);
        Move[] moves = new Move[]{
            new Move(51,35),
            new Move(11,27),
            new Move(50,34),
            new Move(27,34),
            new Move(62,45),
            new Move(6, 21),
            new Move(54,46),
            new Move(12,20),
            new Move(61,54),
            new Move(1,16),
            new Move(57,40)};
        for (Move move : moves) {
            Board.tryMove(testRecord, move);
        }

        Search none = new Search(time, Piece.BLACK, false,  false,  false   );
        Search ab   = new Search(time, Piece.BLACK, true,   false,  false   );
        Search mo   = new Search(time, Piece.BLACK, false,  true,   false   );
        Search tt   = new Search(time, Piece.BLACK, false,  true,   true    );
        Search all  = new Search(time, Piece.BLACK);

        System.out.println("none");
        none.generateMove(testRecord);

        System.out.println("ab");
        ab.generateMove(testRecord);

        System.out.println("mo");
        mo.generateMove(testRecord);

        System.out.println("tt");
        tt.generateMove(testRecord);

        System.out.println("all");
        all.generateMove(testRecord);

        System.out.println();
    }

    @Override
    public void mouseClick(int x, int y) {
        double actualSquareSize = Math.round(squareSize * renderScale);
        int xCoord = (int) Math.floor((double) x / actualSquareSize);
        int yCoord = (int) Math.floor((double) y / actualSquareSize);
        int pressed = yCoord * 8 + xCoord;  
        if (((gameBoard.board[pressed] & 0b11000) == playerCol)
                && playerCanMove) {
            selected = pressed;
            renderer.drawBoard();
            renderer.highlightSquare(xCoord, yCoord);
            renderer.drawAllSprites(gameBoard);
            legalMoves = showMoves(xCoord, yCoord);
        } else if (selected > -1) {
            Move playerMove = findMove(new Move(selected, pressed), legalMoves);
            if (Board.tryMove(gameBoard, playerMove)) {
                Global.repTable.saveRepetition(gameBoard, playerCol);
                playerCanMove = false;
                selected = -1;

                renderer.drawBoard();
                renderer.drawAllSprites(gameBoard);
                // gameBoard.showPositions(); // debug
            } else {
                System.err.println("Player did not make a valid move");
                return;
            }
            
            Move engineMove = engineSearch.generateMove(gameBoard);
            if (Board.tryMove(gameBoard, engineMove)) {
                Global.repTable.saveRepetition(gameBoard, engineCol);
                playerCanMove = true;

                renderer.drawBoard();
                renderer.drawAllSprites(gameBoard);
                // gameBoard.showPositions(); // debug
            } else {
                System.err.println("Engine could not make a valid move");
                return;
            }

        }
    }

    private MoveList showMoves(int x, int y) {
        MoveList moves = PieceMover.moves(gameBoard, selected);   
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).flag == Flag.ONLY_ATTACK) {
                continue;
            }
            BoardRecord tempRec = gameBoard.copy();
            if (!Board.tryMove(tempRec, moves.at(i))) {
                continue;
            }
            int markX = moves.at(i).to % 8;
            int markY = (int) Math.floor(moves.at(i).to / 8);
            renderer.drawMarker(markX, markY);
        }
        return moves;
    }

    // need to convert user move to calculated move to get move flags
    private Move findMove(Move move, MoveList moves) {
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).equals(move)) {
                return moves.at(i);
            }
        }
        return Move.invalid();
    }
}
