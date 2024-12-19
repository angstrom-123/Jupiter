package com.ang.Graphics;

import com.ang.GameInterface;
import com.ang.Piece;
import com.ang.Util.BoardRecord;
import com.ang.Util.InputHandler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Dimension;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Renderer extends JFrame {
    final Colour            DARK_COL       = new Colour(112, 102, 119);
    final Colour            LIGHT_COL      = new Colour(204, 183, 174);
    final Colour            HIGHLIGHT_COL  = new Colour(255, 106,  60);
    
    private int             squareSize;
    private double          scale;
    private int             size;

    private BufferedImage   img;
    private ImagePanel      imgPanel;
    private JFrame          frame;
    private GameInterface   gameInterface;

    private BufferedImage[] sprites;

    public Renderer(int squareSize, double scale, GameInterface gameInterface) {
        this.squareSize     = squareSize;
        this.scale          = scale;
        this.gameInterface  = gameInterface;

        init();
        loadSprites();
        drawBoard();
    }

    public void init() {
        size = squareSize * 8;
        Dimension paneDimension = new Dimension((int) Math.round(size * scale),
                (int) Math.round(size * scale));

        img         = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        imgPanel    = new ImagePanel(img);
        frame       = new JFrame();
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
        Colour lightTint    = new Colour(255, 255, 255);
        Colour darkTint     = new Colour(  0,   0,   0);

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

            sprites[end]        = tint(s, lightTint);
            sprites[end + 1]    = tint(s, darkTint);
            end += 2;
        }
    }

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

    public void drawBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if ((y + x) % 2 == 0) {
                    drawSquare(x, y, LIGHT_COL);
                } else {
                    drawSquare(x, y, DARK_COL);
                }
            }
        }  
        frame.repaint();
    }

    private void drawSquare(int x, int y, Colour col) {
        int startX = x * squareSize;
        int startY = y * squareSize;
        for (int i = startX; i < startX + squareSize; i++) {
            for (int j = startY; j < startY + squareSize; j++) {
                drawPixel(i, j, col);
            }
        }
    }

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

    public void drawSprite(int x, int y, int piece) {
        int index = 0;
        switch(piece & 0b111) {
        case 1:
            index = 0;
            break;
        case 2:
            index = 2;
            break;
        case 3:
            index = 4;
            break;
        case 4:
            index = 6;
            break;
        case 5:
            index = 8;
            break;
        case 6:
            index = 10;
            break;
        default:
            break;
        }

        index = (piece & 0b11000) == 8 ? index : index + 1;
        BufferedImage s = sprites[index];

        drawSprite(x, y, s);
    }

    public void drawMarker(int x, int y) {
        BufferedImage s = sprites[12];

        drawSprite(x, y, s);
    }

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

        frame.repaint();
    }

    private void drawPixel(int x, int y, Colour col) {
        int r = (int) Math.round(col.r());
        int g = (int) Math.round(col.g());
        int b = (int) Math.round(col.b());
        int pixelCol = (r << 16) | (g << 8) | (b);

        img.setRGB(x, y, pixelCol);
    }

    public void highlightSquare(int x, int y) {
        drawSquare(x, y, HIGHLIGHT_COL);

        frame.repaint();
    }

}
   