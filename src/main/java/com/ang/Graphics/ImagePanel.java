package com.ang.Graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Class for an image that can be drawn to 
 */
public class ImagePanel extends JPanel {
    private BufferedImage image;

    /**
     * Constructs a new ImagePanel
     * @param image the BufferedImage for the screen
     */
    public ImagePanel(BufferedImage image) {
        this.image = image;
    }

    /**
     * Overrides the paint component allowing individual pixels to be drawn
     * @param g graphics component of the BufferedImage
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }        
    }
}