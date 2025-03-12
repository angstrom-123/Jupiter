package com.ang;

import com.ang.Core.*;
import com.ang.Core.Moves.*;
import com.ang.Engine.LazySMP.ThreadLauncher;
import com.ang.Engine.LazySMP.ThreadListener;
import com.ang.Engine.LazySMP.Worker;
import com.ang.Graphics.Renderer;
import com.ang.Util.GameInterface;

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

    public Game(Piece playerCol) {
        this.playerCol = playerCol.val();
        this.engineCol = Piece.opposite(playerCol.val()).val();
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
        playerCanMove = true;
        if (engineCol == Piece.WHITE.val()) {
            playerCanMove = false;
            launchSearch();
        }
    }

    /**
     * Handles all logic for player interaction with the gui. 
     *  - Checks if player clicks are valid
     *  - Makes player move when appropriate
     *  - Launches an engine search when appropriate
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

    /**
     * Applies a player's move to the board
     * @param selected index of the square containing the piece to move
     * @param pressed index of the square where the piece should move to
     */
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

    /**
     * Applies the engine's move to the board when the search is complete
     * @param engineMove the move found by the engine, returned from the search
     */
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

    /**
     * Override to implement ThreadListener
     */
    @Override
    public void workerComplete(Worker w) {
        return;
    }

    /**
     * Initialises an engine search in the current position
     */
    private void launchSearch() {
        ThreadLauncher th = new ThreadLauncher(this);
        th.setBoardRecord(gameRec);
        th.setCol(engineCol);
        th.setSearchTime(ENGINE_SEARCH_TIME);
        th.run();
        playerCanMove = true;
    }

    /**
     * Checks if a square pressed would be a valid move for the player
     * @param xCoord the x coordinate of the square pressed
     * @param yCoord the y coordinate of the square pressed
     * @param pos the index of the square pressed
     * @return {@code true} if the click is valid, else {@code false}
     */
    private boolean moveValid(int xCoord, int yCoord, int pos) {
        if ((pos > 63) || (pos < 0)) {
            return false;

        }
        if ((gameRec.board[pos] & 0b11000) == playerCol) {
            return false;

        }
        if (!playerCanMove) {
            return false;

        }
        for (int i = 0; i < legalMoves.length(); i++) {
            Move m = legalMoves.at(i);
            if (m == null) {
                return false;

            }
            if (m.flag != MoveFlag.ONLY_ATTACK) {
                if (pos == m.to) {
                    return true;

                }
            }
        }
        return false;

    }

    /**
     * Checks if a square pressed would be valid to select for the player
     * @param xCoord the x coordinate of the square pressed
     * @param yCoord the y coordinate of the square pressed
     * @param pos the index of the square pressed
     * @return {@code true} if the select is valud, else {@code false}
     */
    private boolean selectValid(int xCoord, int yCoord, int pos) {
        if ((pos > 63) || (pos < 0)) {
            return false;

        }
        if ((gameRec.board[pos] & 0b11000) != playerCol) {
            return false;

        }
        if (!playerCanMove) {
            return false;

        }
        return true;

    }

    /**
     * displays graphically all possible moves for a friendly piece that was 
     * clicked by the player
     * @param x logical x coordinate of square clicked
     * @param y logical y coordinate of square clicked
     * @return the possible moves for the selected piece
     */
    private MoveList showMoves(int x, int y) {
        MoveList moves = PieceMover.moves(gameRec, selected);   
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).flag == MoveFlag.ONLY_ATTACK) {
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
        renderer.updateGUI();
        return moves;

    }

    /**
     * Updates the state of the game (50-move counter, repetitions, etc)
     * @param m the move most recently made
     * @param col the colour that most recently moved
     * @param took did the move capture another piece
     */
    private void updateState(Move m, int col, boolean took) {
        if (((gameRec.board[m.to] & 0b111) != Piece.PAWN.val()) && !took) {
            Global.fiftyMoveCounter++;
        }
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
