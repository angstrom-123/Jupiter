package com.ang.Graphics;

import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Util.GameInterface;
import com.ang.Util.InputHandler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Class for drawing GUI
 */
public class Renderer extends JFrame {
    private final Colour    DARK_COL        = new Colour(112, 102, 119);
    private final Colour    LIGHT_COL       = new Colour(204, 183, 174);
    private final Colour    HIGHLIGHT_COL   = new Colour(255, 106,  60);
    private final Colour    HALFLIGHT_COL   = new Colour(171,  93,  68);
    private final Colour    FONT_COL        = new Colour( 50,  50,  60);
    private int             halflightSquare = -1;
    private int             squareSize;
    private double          scale;
    private int             size;
    private BufferedImage   img;
    private ImagePanel      imgPanel;
    private JFrame          frame;
    private GameInterface   gameInterface;
    private BufferedImage[] sprites;

    /**
     * Constructs a new Renderer and renders the starting position
     * @param squareSize pixel size of each tile on the board
     * @param scale multiplier to change rendered scale of squares
     * @param gameInterface interface with the input handler
     */
    public Renderer(int squareSize, double scale, GameInterface gameInterface) {
        this.squareSize     = squareSize;
        this.scale          = scale;
        this.gameInterface  = gameInterface;
        init();
        loadSprites();
        drawBoard();
    }

    /**
     * Initializes the game board and rendering components
     */
    public void init() {
        size = squareSize * 8;
        Dimension paneDimension = new Dimension((int) Math.round(size * scale),
                (int) Math.round(size * scale));
        img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        imgPanel = new ImagePanel(img);
        frame = new JFrame();
        frame.getContentPane().setPreferredSize(paneDimension);
        frame.getContentPane().add(imgPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                frame.dispose();
            }
        });
        imgPanel.addMouseListener(new InputHandler(gameInterface));
        frame.setFocusable(true);
        frame.requestFocusInWindow();
    }

    /**
     * Loads all sprites from their files into variables for faster access
     */
    public void loadSprites() {
        sprites = new BufferedImage[13];
        int end = 0;
        String[] paths = new String[]{
            "/PawnSprite.png",
            "/KnightSprite.png",
            "/BishopSprite.png",
            "/RookSprite.png",
            "/QueenSprite.png",
            "/KingSprite.png",
            "/StarSprite.png"
        };
        final Colour lightTint = new Colour(255, 255, 255);
        final Colour darkTint = new Colour(0, 0, 0);
        for (int i = 0; i < paths.length; i++) {
            BufferedImage s = new BufferedImage(squareSize, squareSize,
                    BufferedImage.TYPE_INT_ARGB);
            try {
                s = ImageIO.read(this
                        .getClass()
                        .getResource(paths[i]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (i == paths.length - 1) {
                try {
                    s = ImageIO.read(this
                            .getClass()
                            .getResource("/StarSprite.png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sprites[end] = s;
                return;
                
            }
            sprites[end] = tint(s, lightTint);
            sprites[end + 1] = tint(s, darkTint);
            end += 2;
        }
    }

    /**
     * Samples each pixel on a grayscale BufferedImage and tints them light or 
     * dark to serve as the white and black pieces
     * @param s BufferedImage to tint
     * @param tint colour to tint with
     * @return the new tinted BufferedImage
     */
    private BufferedImage tint(BufferedImage s, Colour tint) {
        BufferedImage out = new BufferedImage(squareSize, squareSize,
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < squareSize; y++) {
            for (int x = 0; x < squareSize; x++) {
                int samp    = s.getRGB(x, y);
                int alpha   = (samp >> 24)  & 0xff;
                int r       = (samp >> 16)  & 0xff;
                int g       = (samp >>  8)  & 0xff;
                int b       = (samp >>  0)  & 0xff;
                if ((r == 0) && (g == 0) && (b == 0) && (alpha == 0xff)) {
                    out.setRGB(x, y, samp);
                    continue;

                }
                if (alpha == 0) {
                    continue;

                }
                int rTinted = (int)Math.round((r + tint.r()) / 2);
                int gTinted = (int)Math.round((g + tint.g()) / 2);
                int bTinted = (int)Math.round((b + tint.b()) / 2);
                int pixelTinted = (alpha    << 24) 
                                | (rTinted  << 16) 
                                | (gTinted  <<  8) 
                                | (bTinted);
                out.setRGB(x, y, pixelTinted);
            }
        }
        return out;

    }

    /**
     * Draws all 64 light and dark squares on the chess board
     */
    public void drawBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (y * 8 + x == halflightSquare) {
                    drawSquare(x, y, HALFLIGHT_COL);
                } else {
                    drawSquare(x, y, ((x + y) % 2 == 0) ? LIGHT_COL : DARK_COL);
                }
            }
        }  
    }

    /**
     * Draws all rank and file labels on the board
     */
    public void drawSquareNums() {
        drawCharInSquare(0, 7, 34, 34, Characters.a);
        drawCharInSquare(1, 7, 34, 34, Characters.b);
        drawCharInSquare(2, 7, 34, 34, Characters.c);
        drawCharInSquare(3, 7, 34, 34, Characters.d);
        drawCharInSquare(4, 7, 34, 34, Characters.e);
        drawCharInSquare(5, 7, 34, 34, Characters.f);
        drawCharInSquare(6, 7, 34, 34, Characters.g);
        drawCharInSquare(7, 7, 34, 34, Characters.h);
        drawCharInSquare(0, 0, 1, 1, Characters.eight);
        drawCharInSquare(0, 1, 1, 1, Characters.seven);
        drawCharInSquare(0, 2, 1, 1, Characters.six);
        drawCharInSquare(0, 3, 1, 1, Characters.five);
        drawCharInSquare(0, 4, 1, 1, Characters.four);
        drawCharInSquare(0, 5, 1, 1, Characters.three);
        drawCharInSquare(0, 6, 1, 1, Characters.two);
        drawCharInSquare(0, 7, 1, 1, Characters.one);
    }

    /**
     * Draws all sprites in the position on the board
     * @param rec BoardRecord containing the position to be drawn
     */
    public void drawAllSprites(BoardRecord rec) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int p = rec.board[y * 8 + x];
                if (p != Piece.NONE.val()) {
                    drawSprite(x, y, p);
                }
            }
        }
    }

    /**
     * Draws an indicator in a given square
     * @param x logical x coordinate of square to draw in
     * @param y logical y coordinate of square to draw in
     */
    public void drawMarker(int x, int y) {
        BufferedImage s = sprites[12];
        drawSprite(x, y, s);
    }

    /**
     * Draws a coloured background for the square at given coordinates
     * @param x logical x coordinate of square to be highlighted
     * @param y logical y coordinate of square to be highlighted
     */
    public void highlightSquare(int x, int y) {
        drawSquare(x, y, HIGHLIGHT_COL);
    }

    public void halflightSquare(int x, int y) {
        drawSquare(x, y, HALFLIGHT_COL);
        halflightSquare = y * 8 + x;
    }

    public void clearHalflight() {
        halflightSquare = -1;
    }

    /**
     * Immediately updates the GUI
     */
    public void updateGUI() {
        imgPanel.paintImmediately(imgPanel.getBounds());
    }

    /**
     * Draws a character in a square on the board
     * @param x logical x coordinate of the square
     * @param y logical y coordinate of the square
     * @param xPad padding from left edge of square
     * @param yPad padding from top edge of square
     * @param character character to draw
     */
    private void drawCharInSquare(int x, int y, int xPad, int yPad, int[][] character) {
        int startX = x * squareSize;
        int startY = y * squareSize;
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 10; i++) {
                if (character[i][j] == 1) {
                    drawPixel(startX + xPad + j, startY + yPad + i, FONT_COL);
                }
            }
        }
    }

    /**
     * Draws a coloured square at the given coordinates
     * @param x logical x coordinate to draw at
     * @param y logical y coordinate to draw at
     * @param col colour of square to draw
     */
    private void drawSquare(int x, int y, Colour col) {
        int startX = x * squareSize;
        int startY = y * squareSize;
        for (int i = startX; i < startX + squareSize; i++) {
            for (int j = startY; j < startY + squareSize; j++) {
                drawPixel(i, j, col);
            }
        }
    }

    /**
     * Draws a sprite in a given square
     * @param x logical x coordinate of square to draw in
     * @param y logical y coordinate of square to draw in
     * @param piece integer corresponding to piece to be drawn 
     */
    private void drawSprite(int x, int y, int piece) {
        int index = 0;
        switch(piece & 0b111) {
        case 1: // pawn
            index = 0;
            break;

        case 2: // knight
            index = 2;
            break;

        case 3: // bishop
            index = 4;
            break;

        case 4: // rook
            index = 6;
            break;

        case 5: // queen
            index = 8;
            break;

        case 6: // king
            index = 10;
            break;

        default:
            break;

        }
        // if piece is black then index into sprites[] is incremented
        index = (piece & 0b11000) == 8 ? index : index + 1;
        BufferedImage s = sprites[index];
        drawSprite(x, y, s);
    }

    /**
     * Draws a sprite at a given square
     * @param x logical x coordinate of square to draw in
     * @param y logical y coordinate of square to draw in
     * @param s sprite to draw in square
     */
    private void drawSprite(int x, int y, BufferedImage s) {
        int startX = x * squareSize;
        int startY = y * squareSize;
        for (int j = 0; j < squareSize; j++) {
            for (int i = 0; i < squareSize; i++) {
                int samp    = s.getRGB(i, j);
                int alpha   = (samp >> 24)  & 0xff;
                int r       = (samp >> 16)  & 0xff;
                int g       = (samp >>  8)  & 0xff;
                int b       = (samp)        & 0xff;
                if (alpha == 0) {
                    continue;

                }
                Colour pixelCol = new Colour(r, g, b);
                drawPixel(startX + i, startY + j, pixelCol);
            }
        }
    }

    /**
     * Draws a coloured pixel at specified coordinates
     * @param x x coordinate to draw pixel at
     * @param y y coordinate to draw pixel at
     * @param col colour of pixel to be drawn
     */
    private void drawPixel(int x, int y, Colour col) {
        int r = (int) Math.round(col.r());
        int g = (int) Math.round(col.g());
        int b = (int) Math.round(col.b());
        int pixelCol = (r << 16) | (g << 8) | (b);
        img.setRGB(x, y, pixelCol);
    }
}
   