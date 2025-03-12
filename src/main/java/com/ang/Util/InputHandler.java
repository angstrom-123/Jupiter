package com.ang.Util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Class for handling user inputs
 */
public class InputHandler implements MouseListener {
    private GameInterface gameInterface;

    /**
     * Constructs an input handler for the gui
     * @param gi an interface for interacting with the maing Game.java file
     */
    public InputHandler(GameInterface gi) {
        gameInterface = gi;
    }

    /**
     * Calls the interface with the coordinates of the mouse click
     * @param e the event of clicking the mouse on the window
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        gameInterface.mouseClick(e.getX(), e.getY());
    }

    // overriding extra functionality that isn't used

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
