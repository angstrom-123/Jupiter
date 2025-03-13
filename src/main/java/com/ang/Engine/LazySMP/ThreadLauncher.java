package com.ang.Engine.LazySMP;

import com.ang.Global;
import com.ang.Core.*;
import com.ang.Core.Moves.*;
import com.ang.Engine.*;
import com.ang.Engine.Eval.*;
import com.ang.Util.StringManip;
import com.ang.Util.AlgebraicNotator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Class for launching a search in a position
 */
public class ThreadLauncher implements Runnable, ThreadListener {
    private final int               PERM_COUNT      = 2; // amount of repeat workers at same depth
    private int                     maxDepth        = 8;
    private SearchResult[]          searchResults   = new SearchResult[(maxDepth - 1) * PERM_COUNT];
    private ExecutorService         execService     = Executors.newFixedThreadPool((maxDepth - 1) * PERM_COUNT);
    private BlockingQueue<Runnable> taskQueue       = new LinkedBlockingQueue<>();
    private boolean                 searchDone      = false;
    private Worker[]                workers         = new Worker[(maxDepth - 1) * PERM_COUNT];
    private int                     workersFinished = 0;
    private int                     workersLaunched = 0;
    private ThreadListener          listener;
    private int                     col;
    private BoardRecord             rec;
    private long                    searchTime;
    private long                    endTime;
    
    /**
     * Constructs a new thread launcher
     * @param listener interface to call when the search is finished
     */
    public ThreadLauncher(ThreadListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the colour to search for
     * @param col the colour to search with
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * Sets the position to search for a move in
     * @param rec the position to search
     */
    public void setBoardRecord(BoardRecord rec) {
        this.rec = rec;
    }

    /**
     * Sets the amount of time to search for
     * @param ms the amount of ms to search for
     */
    public void setSearchTime(long ms) {
        this.searchTime = ms;
    }

    /**
     * Overrides Runnable run to launch search workers. Awaits time limit reached
     */
    @Override
    public void run() {
        maxDepth = calculateMaxDepth();
        System.out.println("\nLaunching workers\n");
        endTime = System.currentTimeMillis() + searchTime;
        AspirationWindow aspWin = searchRoot(rec, col);
        reSearch(rec, col, aspWin.alpha, aspWin.beta);
        while (!searchDone) {
            try {
                if (System.currentTimeMillis() < endTime) {
                    Thread.sleep(50);
                } else if (workersFinished != workersLaunched) {
                    killAllWorkers();
                }
            } catch (InterruptedException e) {
                System.err.println("Thread await interrupted");
            }
        }
        System.out.println("==============================\n");
        Move bestMove = analyseSearchResults();
        System.out.println("==============================\n"
                + StringManip.centre(AlgebraicNotator.moveToAlgeb(rec, bestMove), 30) + "\n"
                + "==============================");
        listener.searchComplete(bestMove);
    }

    /**
     * Performs an initial depth 1 search to estimate an aspiration window
     * @param rec the position to search in
     * @param col the colour to search with
     * @return the asipration window found from the search
     */
    private AspirationWindow searchRoot(BoardRecord rec, int col) {
        int bestEval = -Global.INFINITY;
        MoveList moves = Board.allMoves(rec, col);
        moves = Search.orderMoves(rec, col, moves);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (m.flag == MoveFlag.ONLY_ATTACK) {
                continue;

            }
            int eval; 
            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, m)) {
                eval = Evaluation.evaluate(rec, Piece.opposite(col).val());
                if (eval > bestEval) {
                    bestEval = eval;
                }
            }
        }
        // Window width has to be very wide as in near-equal positions, most 
        // moves will be cut. This is the easiest way to avoid this.
        AspirationWindow aspWin = new AspirationWindow();
        aspWin.alpha = - Math.abs(bestEval) - Piece.PAWN.val() * 30;
        aspWin.beta = Math.abs(bestEval) + Piece.PAWN.val() * 30;
        return aspWin;

    }

    /**
     * Performs the main search
     * @param rec the position to search in
     * @param col the colour to search with
     * @param alpha the lower bound for evaluation to accept
     * @param beta the upper bound for evaluation to accept
     */
    private void reSearch(BoardRecord rec, int col, int alpha, int beta) {
        for (int i = 0; i < (maxDepth - 1) * PERM_COUNT; i++) {
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
            if (i > (maxDepth - 2)) {
                w.setDepth(i - maxDepth + 2 + 1);
                w.altMoveOrdering(true);
            } else {
                w.setDepth(i + 2);
                w.altMoveOrdering(false);
            }
            addTask((Runnable) w, index);
        }
    }

    /**
     * Called when a worker completes their search, saves their result. Shuts
     * down the search when all workers are finished
     */
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

    /**
     * Overriding to implement ThreadListener, handled in main game loop
     */
    @Override 
    public void searchComplete(Move m) {
        return;

    }

    /**
     * Finds an index into workers[] containing null, so a new worker can be put in 
     * @return the free index, or -1 if there are no free indices
     */
    private int getFreeIndex() {
        for (int i = 0; i < ((maxDepth - 1) * PERM_COUNT); i++) {
            if (workers[i] == null) {
                return i;

            }
        }
        return -1;

    }

    /**
     * Offers a worker to the executor service
     * @param task the worker to add
     * @param index the index of the worker in workers[]
     */
    private void addTask(Runnable task, int index) {
        if (execService.isShutdown()) {
            System.err.println("Cannot add more tasks, Executor is shut down");
            return;

        }
        taskQueue.offer(task);
        execService.execute(task);
        workers[index] = (Worker) task;
        workersLaunched++;
    }

    /**
     * Attempts to stop all active workers immediately.
     */
    private void killAllWorkers() {
        for (int i = 0; i < workers.length; i++) {
            Worker w = workers[i];
            if (w == null) {
                continue;

            }
            w.doFinish();
            workers[i] = null;
        }
        workersFinished = workersLaunched;
        searchDone = true;
        execService.shutdownNow();
    }

    /**
     * Implements thread voting. Finds favourite move of all threads.
     * @return the favourite move
     */
    private Move analyseSearchResults() {
        int[] moveScores = new int[workersLaunched];
        MoveList bestMoves = new MoveList(workersLaunched);
        for (int i = 0; i < searchResults.length; i++) {
            SearchResult sr = searchResults[i];
            if (sr == null) {
                continue;

            }
            if (sr.move.isInvalid()) {
                continue;

            }
            if (moveScores.length == 0) {
                bestMoves.add(sr.move);
                moveScores[0] = weightedMoveScore(sr);
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
            if (bestMoves.at(i) == null) {
                break;

            }
            System.out.println("Considering:\n" 
                    + "    - Move: " + AlgebraicNotator.moveToAlgeb(rec, bestMoves.at(i)) + "\n" 
                    + "    - Score: " + moveScores[i] + "\n");
        }
        return bestMove;

    }

    /**
     * Calculates the weighed score of a search result
     * @param sr the search result to calculate
     * @return weighted score for the search result
     */
    private int weightedMoveScore(SearchResult sr) {
        final int BASE_SCORE = 100;
        int weightedScore = BASE_SCORE;
        weightedScore += Math.round((0.1 + (double) sr.depth / 10) * weightedScore);
        weightedScore += (sr.eval / 40);
        if ((rec.minorPieceCount > 6) && (rec.board[sr.move.from] & 0b111) == Piece.ROOK.val()) {
            weightedScore -= 25;
        }
        return weightedScore;

    }

    /**
     * Calculates a variable search depth based on amount of minor pieces
     * @return calculated maximum depth
     */
    private int calculateMaxDepth() {
        final int BASE_DEPTH = 4;
        if (rec.minorPieceCount > 6) {
            return BASE_DEPTH;
        } else if (rec.minorPieceCount > 4) {
            return BASE_DEPTH + 1;
        } else if (rec.minorPieceCount > 2) {
            return BASE_DEPTH + 2;
        } else {
            return BASE_DEPTH + 3;
        }
    }
}
