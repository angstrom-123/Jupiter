package com.ang.Engine.LazySMP;

import com.ang.Core.Moves.Move;

/**
 * Interface for the multithreaded search
 */
public interface ThreadListener {
    void workerComplete(Worker w);
    void searchComplete(Move m);
}
