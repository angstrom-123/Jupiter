package com.ang.Engine;

import com.ang.Core.Moves.Move;

public interface ThreadListener {
    void workerComplete(Worker w);
    void searchComplete(Move m);
}
