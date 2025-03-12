package com.ang.Graphics;

import com.ang.Core.BoardRecord;

/**
 * Interface into the UpdateWorker - function definitions can be found there
 */
public interface UpdateInterface {
    void drawBoard();
    void drawSquareNums();
    void drawAllSprites(BoardRecord rec);
    void drawMarker(int x, int y);
    void highlightSquare(int x, int y);
}
