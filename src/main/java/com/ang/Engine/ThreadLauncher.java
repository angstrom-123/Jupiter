package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.*;
import com.ang.Engine.Transposition.TranspositionTable;
import com.ang.Util.StringManip;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;

public class ThreadLauncher implements Runnable, ThreadListener {
    private final int               MAX_DEPTH           = 8;
    private final int               PERMUTATION_COUNT   = 2;

    // TODO: because using max time now,
    //      - change how handling worker[]
    //          - especially when collecting / analysing results

    private SearchResult[]          searchResults;
    private Worker[]                workers;
    private int                     workersFinished;
    private int                     workersLaunched;
    private BlockingQueue<Runnable> taskQueue;
    private ExecutorService         execService;
    private boolean                 searchDone; // TODO : switch to max time
    private ThreadListener          listener;
    private int                     col;
    private BoardRecord             rec;
    private long                    searchTime;
    private long                    endTime;

    public ThreadLauncher(ThreadListener listener) {
        this.listener = listener;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setBoardRecord(BoardRecord rec) {
        this.rec = rec;
    }

    public void setSearchTime(long ms) {
        this.searchTime = ms;
    }

    @Override
    public void run() {
        endTime = System.currentTimeMillis() + searchTime;
        System.out.println("start time " + System.currentTimeMillis() + " end " + endTime);
        init();
        AspirationWindow aspWin = searchRoot(rec, col);
        reSearch(rec, col, aspWin.alpha, aspWin.beta);
        while (!searchDone) {
            try {
                Thread.sleep(50);
                if ((System.currentTimeMillis() >= endTime)
                        && (workersFinished != workersLaunched)) {
                    for (int i = 0; i < workers.length; i++) {
                        Worker w = workers[i];
                        if (w == null) continue;

                        w.doFinish();
                        workers[i] = null;
                    }
                    searchDone = true;
                    execService.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Thread await interrupted");
            }
        }
        if (Global.tTable.size > 500000) {
            System.out.println("Clearing Transposition Table");
            Global.tTable = new TranspositionTable();
        }
        Move bestMove = analyseSearchResults();
        System.out.println("==============================");
        System.out.println(StringManip.centre(
                "Final Engine Move: " + bestMove.from + " to " + bestMove.to
                , 30));
        System.out.println("==============================");
        listener.searchComplete(bestMove);
    }

    private AspirationWindow searchRoot(BoardRecord rec, int col) {
        int bestEval = -Global.INFINITY;

        // depth 1 search
        MoveList moves = Board.allMoves(rec, col);
        moves = Search2.orderMoves(rec, col, moves);

        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) continue;

            int eval; 
            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, m)) {
                eval = Evaluation.evaluate(rec, Piece.opposite(col).val());
                if (eval > bestEval) {
                    bestEval = eval;
                }
            }
        }
        AspirationWindow aspWin = new AspirationWindow();
        aspWin.alpha = - Math.abs(bestEval) - Piece.PAWN.val() * 30;
        aspWin.beta = Math.abs(bestEval) + Piece.PAWN.val() * 30;
        return aspWin;

    }

    private void reSearch(BoardRecord rec, int col, int alpha, int beta) {
        for (int i = 0; i < (MAX_DEPTH - 1) * PERMUTATION_COUNT; i++) {
            int index = getFreeIndex();
            if (index == -1) {
                System.err.println("Thread cap reached");
                return;

            }
            Worker w = new Worker(index, this);
            w.setAspirationWindow(alpha, beta);
            w.setCol(col);
            w.setRoot(rec);
            w.setEndTime(endTime);
            System.out.println();
            System.out.println("New Search");
            if (i > (MAX_DEPTH - 2)) {
                System.out.println("Worker to depth: " + (i - MAX_DEPTH + 3));
                w.setDepth(i - MAX_DEPTH + 2 + 1);
                w.altMoveOrdering(true);
            } else {
                System.out.println("Worker to depth: " + (i + 2));
                w.setDepth(i + 2);
                w.altMoveOrdering(false);
            }
            addTask((Runnable) w, index);
        }
    }

    @Override
    public void workerComplete(Worker w) {
        workersFinished++;
        searchResults[w.id] = w.result;
        workers[w.id] = null;
        if ((workersFinished == workersLaunched)) {
            execService.shutdown();
            searchDone = true;
        }
    }

    @Override 
    public void searchComplete(Move m) {
        return;
    }

    private void init() {
        searchResults   = new SearchResult[(MAX_DEPTH - 1) * PERMUTATION_COUNT];
        execService     = Executors.newFixedThreadPool((MAX_DEPTH - 1) * PERMUTATION_COUNT);
        taskQueue       = new LinkedBlockingQueue<>();
        searchDone      = false;
        workers         = new Worker[(MAX_DEPTH - 1) * PERMUTATION_COUNT];
        workersFinished = 0;
        workersLaunched = 0;
    }

    private int getFreeIndex() {
        for (int i = 0; i < ((MAX_DEPTH - 1) * PERMUTATION_COUNT); i++) {
            if (workers[i] == null) return i;

        }
        return -1;

    }

    private void addTask(Runnable task, int index) {
        System.out.println("Starting Thread");
        if (execService.isShutdown()) {
            System.err.println("Cannot add more tasks, Executor shut down");
            return;

        }
        taskQueue.offer(task);
        execService.execute(task);
        workers[index] = (Worker) task;
        workersLaunched++;
    }

    private Move analyseSearchResults() {
        int[] moveScores = new int[workersLaunched];
        MoveList bestMoves = new MoveList(workersLaunched);
        for (int i = 0; i < searchResults.length; i++) {
            SearchResult sr = searchResults[i];
            if (sr == null) continue;
            
            if (sr.move.isInvalid()) continue;

            if (moveScores.length == 0) {
                bestMoves.add(sr.move);
                moveScores[0] = 1;
            } else {
                boolean found = false;
                for (int j = 0; j < bestMoves.length(); j++) {
                    if (bestMoves.at(j).equals(sr.move)) {
                        moveScores[j] += weightedMoveScore(sr);
                        found = true;
                        break;

                    }
                }
                if (!found) {
                    bestMoves.add(sr.move);
                    moveScores[bestMoves.length() - 1] = weightedMoveScore(sr);
                }
            }
        }

        int max = 0;
        Move bestMove = Move.invalid();
        for (int i = 0; i < moveScores.length; i++) {
            if (moveScores[i] > max) {
                max = moveScores[i];
                bestMove = bestMoves.at(i);
            }
        }

        for (int i = 0; i < moveScores.length; i++) {
            if (bestMoves.at(i) == null) break;

            System.out.println("Considering: " + bestMoves.at(i).from + " to " 
                    + bestMoves.at(i).to);
            System.out.println("    - Score: " + moveScores[i]);
        }

        return bestMove;
    }

    private int weightedMoveScore(SearchResult sr) {
        final int BASE_SCORE = 100;
        int weightedScore = BASE_SCORE;
        weightedScore *= (1 + (sr.depth / 10));
        weightedScore += (sr.eval / 50);
        return (int) Math.round(weightedScore);

    }
}
