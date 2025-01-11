package com.ang;

import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Core.Moves.*;
import com.ang.Engine.EndState;
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

    public BoardRecord   gameRec;
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
        // String startFEN     = "3r4/8/3k4/8/8/3K4/8/8";
        // String startFEN     = "8/8/3k4/8/2KB4/4r3/8/8"; 
        gameRec             = new BoardRecord(startFEN);
        renderer            = new Renderer(squareSize, renderScale, this);

        renderer.drawBoard();
        renderer.drawAllSprites(gameRec);

        if (engineCol == Piece.WHITE.val()) {
            playerCanMove = false;
            Move engineMove = engineSearch.generateMove(gameRec);
            if (!Board.tryMove(gameRec, engineMove)) {
                System.err.println("Engine could not make a valid move");
                return;  
            }
            renderer.drawBoard();
            renderer.drawAllSprites(gameRec);
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

        Search s0  = new Search(time, Piece.BLACK);
        System.out.println("test: ");
        s0.generateMove(testRecord);
        System.out.println();
    }

    @Override
    public void mouseClick(int x, int y) {
        double actualSquareSize = Math.round(squareSize * renderScale);
        int xCoord = (int) Math.floor((double) x / actualSquareSize);
        int yCoord = (int) Math.floor((double) y / actualSquareSize);
        int pressed = yCoord * 8 + xCoord;  
        if (((gameRec.board[pressed] & 0b11000) == playerCol)
                && playerCanMove) {
            selected = pressed;
            renderer.drawBoard();
            renderer.highlightSquare(xCoord, yCoord);
            renderer.drawAllSprites(gameRec);
            legalMoves = showMoves(xCoord, yCoord);
        } else if (selected > -1) {
            Move playerMove = findMove(new Move(selected, pressed), legalMoves);
            boolean playerTook = (gameRec.board[playerMove.to] != Piece.NONE.val());
            if (Board.tryMove(gameRec, playerMove)) {
                gameRec.showAttacks();
                
                if (((gameRec.board[playerMove.to] & 0b111) != Piece.PAWN.val())
                        && !playerTook) {
                    Global.fiftyMoveCounter++;
                }
                if (Board.endState(gameRec, playerCol) == EndState.DRAW) {
                    System.out.println("draw");
                    renderer.drawBoard();
                    renderer.drawAllSprites(gameRec);
                    return;
                }
                Global.repTable.saveRepetition(gameRec.copy());
                playerCanMove = false;
                selected = -1;

                renderer.drawBoard();
                renderer.drawAllSprites(gameRec);
                // gameBoard.showPositions(); // debug

            } else {
                System.err.println("Player did not make a valid move");
                return;
            }
            
            Move engineMove = engineSearch.generateMove(gameRec);
            if (engineMove.isInvalid()) {
                System.err.println("Engine move invalid");
                return;
            }
            boolean engineTook = (gameRec.board[engineMove.to] != Piece.NONE.val());
            if (Board.tryMove(gameRec, engineMove)) {
                gameRec.showAttacks();
                
                if (((gameRec.board[engineMove.to] & 0b111) != Piece.PAWN.val())
                        && !engineTook) {
                    Global.fiftyMoveCounter++;
                }
                if (Board.endState(gameRec, engineCol) == EndState.DRAW) {
                    renderer.drawBoard();
                    renderer.drawAllSprites(gameRec);
                    System.out.println("draw");
                    return;
                }
                Global.repTable.saveRepetition(gameRec.copy());
                playerCanMove = true;

                renderer.drawBoard();
                renderer.drawAllSprites(gameRec);
                // gameBoard.showPositions(); // debug
            } else {
                System.err.println("Engine could not make a valid move");
                return;
            }
        }
    }

    private MoveList showMoves(int x, int y) {
        MoveList moves = PieceMover.moves(gameRec, selected);   
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).flag == Flag.ONLY_ATTACK) {
                continue;
            }
            BoardRecord tempRec = gameRec.copy();
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
