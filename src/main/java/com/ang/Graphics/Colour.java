package com.ang.Graphics;

public class Colour {
    private double[] e = new double[3];

    public Colour(double r, double g, double b) {
        e[0] = r;
        e[1] = g;
        e[2] = b;
    }

    public Colour(int[] rgb) {
        e[0] = rgb[0];
        e[1] = rgb[1];
        e[2] = rgb[2];
    }

    public Colour(Colour c) {
        e[0] = c.r();
        e[1] = c.g();
        e[2] = c.b();
    }

    public double r() {
        return e[0];
    }

    public double g() {
        return e[1];
    }

    public double b() {
        return e[2];
    }

    public Colour multiply(double t) {
        return new Colour(e[0] * t, e[1] * t, e[2] * t);
    }
}
