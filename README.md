# Jupiter
Simple chess engine written in Java. (Work in progress)

## Features
- Alpha-Beta pruning optimization
- Transposition table position saving
- Move ordering optimizations
- Iterative deepening
- Quiescence search

## Running
Requires an installation of JDK-21.

By default, engine thinks for 1 second before attempting to make a move.

## Known Bugs
This project is a work-in-progress so these bugs may or may not be fixed.
- Transposition table size is uncapped
- Engine can sometimes move the player's pieces
- Player's king can sometimes be moved into illegal squares

## TODO
- Implement checkmates, draws
- Display player's moves immediately in GUI
- Fix windows DPI scaling problems with sprites
- Improve engine's endgame performance
- Optimize: history heuristic, killer moves, pawn position evaluation, futility pruning
- Implement multithreaded move search
- Ensure that search is stopped after time limit is reached

## Screenshots
![Screenshot 2024-12-20 014551](https://github.com/user-attachments/assets/49d0751e-ca2f-4777-a1f0-f6982b322109)
![Screenshot 2024-12-20 014826](https://github.com/user-attachments/assets/64708562-02ed-48bd-a771-c923e1336437)
