package com.ang.Graphics;

/**
 * Class for representing a colour
 */
public class Colour {
    private int[] elements = new int[3];

    /**
     * Constructs colour from individual components
     * @param r red component
     * @param g green component
     * @param b blue component
     */
    public Colour(int r, int g, int b) {
        elements[0] = r;
        elements[1] = g;
        elements[2] = b;
    }

    /**
     * Constructs colour from int[] representing components
     * @param rgb int[3] with r, g, b values
     */
    public Colour(int[] rgb) {
        elements[0] = rgb[0];
        elements[1] = rgb[1];
        elements[2] = rgb[2];
    }

    /**
     * Constructs colour as a copy of another colour
     * @param col colour to be copied
     */
    public Colour(Colour col) {
        elements[0] = col.r();
        elements[1] = col.g();
        elements[2] = col.b();
    }

    /**
     * @return red component
     */
    public int r() {
        return elements[0];
    }

    /**
     * @return green component
     */
    public int g() {
        return elements[1];
    }

    /**
     * @return blue component
     */
    public int b() {
        return elements[2];
    }

    /**
     * Multiplies the colour by a constant
     * @param t constant to multiply by
     * @return new colour with multiplier applied
     */
    public Colour multiply(double t) {
        int r = (int) Math.round(elements[0] * t);
        int g = (int) Math.round(elements[1] * t);
        int b = (int) Math.round(elements[2] * t);

        return new Colour(r, g, b);
    }
}
