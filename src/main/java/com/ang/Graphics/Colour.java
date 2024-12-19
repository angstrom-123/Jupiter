package com.ang.Graphics;

public class Colour {
    private double[] elements = new double[3];

    public Colour(double r, double g, double b) {
        elements[0] = r;
        elements[1] = g;
        elements[2] = b;
    }

    public Colour(int[] rgb) {
        elements[0] = rgb[0];
        elements[1] = rgb[1];
        elements[2] = rgb[2];
    }

    public Colour(Colour col) {
        elements[0] = col.r();
        elements[1] = col.g();
        elements[2] = col.b();
    }

    public double r() {
        return elements[0];
    }

    public double g() {
        return elements[1];
    }

    public double b() {
        return elements[2];
    }

    public Colour multiply(double t) {
        return new Colour(elements[0] * t, elements[1] * t, elements[2] * t);
    }
}
