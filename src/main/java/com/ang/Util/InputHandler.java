package com.ang.Util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InputHandler implements MouseListener {
    private GameInterface gameInterface;

    public InputHandler(GameInterface gi) {
        gameInterface = gi;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gameInterface.mouseClick(e.getX(), e.getY());
    }

    @Override 
    public void mouseEntered(MouseEvent e) {
    }

    @Override 
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
