package com.ang;

import com.ang.Graphics.Renderer;
import com.ang.Moves.*;
import com.ang.Opponent.Engine;
import com.ang.Util.BoardRecord;

public class Game implements GameInterface {
    private int         squareSize;
    private double      renderScale;

    private int         selected;
    private MoveList    legalMoves;
    private int         colToMove;

    public BoardRecord  mainRec;
    public Renderer     renderer;
    public Engine       engine;
    
    public Game(int squareSize, double renderScale) {
        this.squareSize = 45;
        this.renderScale = 1.2;
    }

    public void init(Engine engine) {
        this.engine     = engine;

        selected        = -1;
        colToMove       = Piece.WHITE.val();
        // colToMove       = Piece.BLACK.val();
        legalMoves      = new MoveList(0);
        String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        mainRec         = new BoardRecord(startFEN);
        renderer        = new Renderer(squareSize, renderScale, this);

        renderer.drawBoard();
        renderer.drawAllSprites(mainRec);
    }

    public void test() {
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

        Engine none = new Engine(4000, Piece.BLACK, false,  false,  false   );
        Engine ab   = new Engine(4000, Piece.BLACK, true,   false,  false   );
        Engine mo   = new Engine(4000, Piece.BLACK, false,  true,   false   );
        Engine tt   = new Engine(4000, Piece.BLACK, false,  true,   true    );
        Engine all  = new Engine(4000, Piece.BLACK);

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
        if ((mainRec.board[pressed] & 0b11000) == colToMove) {
            selected = pressed;
            renderer.drawBoard();
            renderer.highlightSquare(xCoord, yCoord);
            renderer.drawAllSprites(mainRec);
            legalMoves = showMoves(xCoord, yCoord);
        } else if (selected > -1) {
            Move playerMove = findMove(new Move(selected, pressed), legalMoves);
            if (!Board.tryMove(mainRec, playerMove)) {
                System.err.println("Player did not make a valid move");
                return;
            }

            renderer.drawBoard();
            renderer.drawAllSprites(mainRec);

            selected = -1;
            colToMove = colToMove == Piece.WHITE.val() 
            ? Piece.BLACK.val() 
            : Piece.WHITE.val();

            // mainRec.showPositions(); // debug

            Move engineMove = engine.generateMove(mainRec);
            if (!Board.tryMove(mainRec, engineMove)) {
                System.err.println("Engine could not make a valid move");
                return;  
            }

            renderer.drawBoard();
            renderer.drawAllSprites(mainRec);

            colToMove = colToMove == Piece.WHITE.val() 
                ? Piece.BLACK.val() 
                : Piece.WHITE.val();

            // mainRec.showPositions(); // debug
        }
    }

    private MoveList showMoves(int x, int y) {
        MoveList moves = Board.pieceMoves(mainRec, selected);   
        for (int i = 0; i < moves.length(); i++) {
            if (moves.at(i).flag == Flag.ONLY_ATTACK) {
                continue;
            }
            BoardRecord tempRec = mainRec.copy();
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
