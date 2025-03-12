package com.ang.Engine.LazySMP;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.Move;
import com.ang.Engine.*;

/**
 * Thread worker that performs a single search to a specific depth in LazySMP
 */
public class Worker implements Runnable {
    public int              id;
    public SearchResult     result;

    private int             alpha;
    private int             beta;
    private int             maxDepth;
    private int             col;
    private long            endTime;
    private boolean         altMoveOrdering;
    private BoardRecord     rootRec;
    private ThreadListener  listener;

    /**
     * Constructs a new worker
     * @param id the index of the worker into the workers[] in the threadLauncher
     * @param listener interface to call when the search is finished
     */
    public Worker(int id, ThreadListener listener) {
        this.id = id;
        this.listener = listener;
    }

    /**
     * Sets the aspiration window for the search
     * @param alpha the lower bound for evaluation to accept
     * @param beta the upper bound for evaluation to accept
     */
    public void setAspirationWindow(int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Sets the depth of the search
     * @param maxDepth the depth to which to attempt to search
     */
    public void setDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Sets the colour to move in the position to search
     * @param col colour to move 
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * Sets the position to search in
     * @param rootRec the position to search
     */
    public void setRoot(BoardRecord rootRec) {
        this.rootRec = rootRec;
    }

    /**
     * Sets the time to terminate the search
     * @param ms end time from System.currentTimeMillis() + searchTime
     */
    public void setEndTime(long ms) {
        this.endTime = ms;
    }

    /**
     * Sets move ordering to be standard or random
     * @param b should the move ordering be randomized
     */
    public void altMoveOrdering(boolean b) {
        this.altMoveOrdering = b;
    }

    /**
     * Attempts to terminate the thread before the search is finished
     */
    public void doFinish() {
        result = new SearchResult(Move.invalid(), -Global.INFINITY, 0);
        listener.workerComplete(this);
    }

    /**
     * Overrides Runnable run method to begin a search
     */
    @Override
    public void run() {
        BoardRecord tempRec = rootRec.copy();
        result = Search.search(tempRec, alpha, beta, col, maxDepth, 
                altMoveOrdering, endTime);
        listener.workerComplete(this);
    }
}
