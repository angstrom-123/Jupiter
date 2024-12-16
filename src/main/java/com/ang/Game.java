package com.ang;

import com.ang.Graphics.Renderer;
import com.ang.Moves.*;
import com.ang.Opponent.Engine;
import com.ang.Util.BoardRecord;

public class Game implements GameInterface {
    int squareSize;
    double renderScale;

    private int selected;
    private MoveList legalMoves;
    private int colToMove;

    public BoardRecord mainRec;
    public Renderer renderer;
    public Engine engine;
    
    public Game(int squareSize, double renderScale) {
        this.squareSize = 45;
        this.renderScale = 1.2;
    }

    public void init(Engine engine) {
        this.engine = engine;

        selected = -1;
        legalMoves = new MoveList(0);
        colToMove = Piece.WHITE.val();

        String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        mainRec = new BoardRecord(startFEN);
        renderer = new Renderer(squareSize, renderScale, this);

        renderer.drawBoard();
        renderer.drawAllSprites(mainRec);
    }

    @Override
    public void mouseClick(int x, int y) {
        double actualSquareSize = Math.round(squareSize * renderScale);
        int xCoord = (int)Math.floor((double)x / actualSquareSize);
        int yCoord = (int)Math.floor((double)y / actualSquareSize);
        
        int pressed = yCoord * 8 + xCoord;   
        if ((mainRec.board[pressed] & 0b11000) == colToMove) {
            selected = pressed;
            legalMoves = Board.pieceMoves(mainRec, selected);   
            renderer.drawBoard();
            renderer.highlightSquare(xCoord, yCoord);
            renderer.drawAllSprites(mainRec);
            for (int i = 0; i < legalMoves.length(); i++) {
                if (legalMoves.at(i).flag == Flag.ONLY_ATTACK) {
                    continue;
                }
                if (!Board.tryMove(mainRec.copy(), legalMoves.at(i))) {
                    continue;
                }
                int markX = legalMoves.at(i).to % 8;
                int markY = (int) Math.floor(legalMoves.at(i).to / 8);
                renderer.drawMarker(markX, markY);
            }
        } else if (selected > -1) {
            Move playerMove = findMove(new Move(selected, pressed), legalMoves);
            boolean moved = Board.tryMove(mainRec, playerMove);
            if (moved) {
                selected = -1;
                colToMove = colToMove == Piece.WHITE.val() 
                ? Piece.BLACK.val() 
                : Piece.WHITE.val();

                mainRec.showPositions();
                Move engineMove = engine.generateMove(mainRec);
                if (Board.tryMove(mainRec, engineMove)) {
                    colToMove = colToMove == Piece.WHITE.val() 
                    ? Piece.BLACK.val() 
                    : Piece.WHITE.val();
                } else {
                    System.err.println("Engine could not make a valid move");
                }
                mainRec.showPositions();
                renderer.drawBoard();
                renderer.drawAllSprites(mainRec);
            }
        }
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
