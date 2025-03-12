package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.Move;

public class Worker implements Runnable {
    public int id;
    public SearchResult result;

    private int alpha;
    private int beta;
    private int maxDepth;
    private int col;
    private long endTime;
    private boolean altMoveOrdering;
    private BoardRecord rootRec;
    private ThreadListener listener;

    public Worker(int id, ThreadListener listener) {
        this.id = id;
        this.listener = listener;
    }

    public void setAspirationWindow(int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public void setDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRoot(BoardRecord rootRec) {
        this.rootRec = rootRec;
    }

    public void setEndTime(long ms) {
        this.endTime = ms;
    }

    public void altMoveOrdering(boolean b) {
        this.altMoveOrdering = b;
    }

    public void doFinish() {
        result = new SearchResult(Move.invalid(), -Global.INFINITY, 0);
        listener.workerComplete(this);
    }

    @Override
    public void run() {
        BoardRecord tempRec = rootRec.copy();
        result = Search2.search(tempRec, alpha, beta, col, 0, maxDepth, 
                altMoveOrdering, endTime);
        listener.workerComplete(this);
    }
}
