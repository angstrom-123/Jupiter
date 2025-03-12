package com.ang;

import com.ang.Core.*;
import com.ang.Core.Moves.*;
import com.ang.Engine.*;
import com.ang.Graphics.Renderer;
import com.ang.Util.GameInterface;
import com.ang.Engine.ThreadLauncher;

/**
 * Class handling the player's interaction with the game and the game loop
 */
public class Game implements GameInterface, ThreadListener {
    private final long      ENGINE_SEARCH_TIME = 5000;
    private int             squareSize;
    private double          renderScale;
    private int             selected;
    private boolean         playerCanMove;
    private MoveList        legalMoves;
    private int             playerCol;
    private int             engineCol;

    public BoardRecord      gameRec;
    public Renderer         renderer;
    public Search           engineSearch;

    public Game(Search engineSearch) {
        this.engineSearch   = engineSearch;
        this.engineCol      = engineSearch.engineCol;
        this.playerCol      = (engineCol == Piece.WHITE.val()) 
        ? Piece.BLACK.val()
        : Piece.WHITE.val();
    }

    /**
     * Initialises the game to be played between an engine and player
     * @param squareSize defines the pixel size of board squares
     * @param renderScale scaling factor used when rendering squares
     */
    public void init(int squareSize, double renderScale) {
        this.squareSize     = squareSize;
        this.renderScale    = renderScale;
        String startFEN     = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

        selected            = -1;
        legalMoves          = new MoveList(0);
        gameRec             = new BoardRecord(startFEN);
        renderer            = new Renderer(squareSize, renderScale, this);

        renderer.drawBoard();
        renderer.drawAllSprites(gameRec);
        renderer.drawSquareNums();
        renderer.updateGUI();

        if (engineCol == Piece.WHITE.val()) {
            playerCanMove = false;
            Move engineMove = engineSearch.generateMove(gameRec);
            if (!Board.tryMove(gameRec, engineMove)) {
                System.err.println("Engine could not make a valid move");
                return;  
            }
            renderer.drawBoard();
            renderer.drawAllSprites(gameRec);
            renderer.drawSquareNums();
            renderer.updateGUI();
        }
        playerCanMove = true;
    }

    /**
     * tests how deep the engine can search in a given position
     * @param time time in ms that the engine is allowed to search for
     */
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

    /**
     * Handles all logic for player interaction with the gui
     * @param x x coordinate of clicked position
     * @param y y coordinate of clicked position
     */
    @Override
    public void mouseClick(int x, int y) {
        double actualSquareSize = Math.round(squareSize * renderScale);
        int xCoord = (int) Math.floor((double) x / actualSquareSize);
        int yCoord = (int) Math.floor((double) y / actualSquareSize);
        int pressed = yCoord * 8 + xCoord;  
        if (selectValid(xCoord, yCoord, pressed)) {
            selected = pressed;
            renderer.drawBoard();
            renderer.highlightSquare(xCoord, yCoord);
            renderer.drawAllSprites(gameRec);
            renderer.drawSquareNums();
            renderer.updateGUI();
            legalMoves = showMoves(xCoord, yCoord);
        } else if (moveValid(xCoord, yCoord, pressed) && (selected > -1)) {
            makePlayerMove(selected, pressed);

            renderer.drawBoard();
            renderer.drawAllSprites(gameRec);
            renderer.drawSquareNums();
            renderer.updateGUI();

            launchSearch();
        }
    }

    private void makePlayerMove(int selected, int pressed) {
        Move playerMove = findMove(new Move(selected, pressed), legalMoves);
        boolean playerTook = (gameRec.board[playerMove.to] != Piece.NONE.val());
        if (Board.tryMove(gameRec, playerMove)) {                
            updateState(playerMove, playerCol, playerTook);
            playerCanMove = false;
            selected = -1;
        } else {
            System.err.println("Player move invalid");
            return;
        }
    }

    @Override
    public void searchComplete(Move engineMove) {
        if (engineMove.isInvalid()) {
            System.err.println("Engine search returned invalid move");
            return;

        }
        boolean engineTook = (gameRec.board[engineMove.to] != Piece.NONE.val());
        if (Board.tryMove(gameRec, engineMove)) {                
            updateState(engineMove, engineCol, engineTook);
            playerCanMove = true;
            renderer.drawBoard();
            renderer.drawAllSprites(gameRec);
            renderer.drawSquareNums();
            renderer.updateGUI();
        } else {
            System.err.println("Engine could not make a valid move");
            return;

        }
    }

    @Override
    public void workerComplete(Worker w) {
        return;
    }

    private void launchSearch() {
        ThreadLauncher th = new ThreadLauncher(this);
        th.setBoardRecord(gameRec);
        th.setCol(engineCol);
        th.setSearchTime(ENGINE_SEARCH_TIME);
        th.run();
        playerCanMove = true;
    }

    private boolean moveValid(int xCoord, int yCoord, int pos) {
        if ((pos > 63) || (pos < 0)) return false;

        if ((gameRec.board[pos] & 0b11000) == playerCol) return false;

        if (!playerCanMove) return false;

        for (int i = 0; i < legalMoves.length(); i++) {
            Move m = legalMoves.at(i);
            if (m == null) return false;

            if (m.flag == MoveFlag.ONLY_ATTACK) continue;

            if (pos == m.to) return true;

        }

        return false;

    }

    private boolean selectValid(int xCoord, int yCoord, int pos) {
        if ((pos > 63) || (pos < 0)) return false;

        if ((gameRec.board[pos] & 0b11000) != playerCol) return false;

        if (!playerCanMove) return false;

        return true;

    }

    /**
     * displays graphically all possible moves for a friendly piece that was 
     * clicked by the player
     * @param x logical x coordinate of square clicked
     * @param y logical y coordinate of square clicked
     * @return
     */
    private MoveList showMoves(int x, int y) {
        MoveList moves = PieceMover.moves(gameRec, selected);   
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).flag == MoveFlag.ONLY_ATTACK) continue;
            
            BoardRecord tempRec = gameRec.copy();
            if (!Board.tryMove(tempRec, moves.at(i))) continue;
            
            int markX = moves.at(i).to % 8;
            int markY = (int) Math.floor(moves.at(i).to / 8);
            renderer.drawMarker(markX, markY);
        }
        renderer.updateGUI();
        return moves;

    }

    private void updateState(Move m, int col, boolean took) {
        if (((gameRec.board[m.to] & 0b111) != Piece.PAWN.val()) && !took) Global.fiftyMoveCounter++;
        if (Board.endState(gameRec, col) == GameFlag.DRAW) {
            renderer.drawBoard();
            renderer.drawAllSprites(gameRec);
            renderer.drawSquareNums();
            System.out.println("draw");
        }
        Global.repTable.saveRepetition(gameRec.copy());
    }

    /**
     * When the player creates a move, they specify the from and to squares.
     * This must be converted to the corresponding move from the movelist for the 
     * clicked piece to get any required move flags such as promotion & en passant.
     * @param move the player's (flagless) move
     * @param moves the movelist of the clicked piece used to find the player's move
     * @return the move found in the movelist or invalid move if not found
     */
    private Move findMove(Move move, MoveList moves) {
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).equals(move)) {
                return moves.at(i);

            }
        }
        return Move.invalid();

    }
}
