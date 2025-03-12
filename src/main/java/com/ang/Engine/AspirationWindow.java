package com.ang.Engine;

public class AspirationWindow {
    public int alpha;
    public int beta;

    /**
     * Constructs a blank aspiration window
     */
    public AspirationWindow() {}

    /**
     * Constructs an aspiration window with lower and uper bound
     * @param alpha lower bound for evaluation
     * @param beta upper bound for evaluation
     */
    public AspirationWindow(int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;
    }
}
