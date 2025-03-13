# Jupiter
Simple chess engine with accompanying GUI written in Java.

## Features
- Alpha-Beta pruning with Quiescence Search
- Transposition table position lookup
- Move ordering optimizations
- Iterative deepening
- LazySMP multithreading with thread voting

## Running
Requires an installation of JDK-21.

To run the engine, run "java -jar Jupiter-x.x.x.jar". Add "-b" or "-w" to 
choose to play as black or white, the default is white.The engine thinks 
for 5 seconds before making a move. 

## TODO
- Update algebraic notation to support castling and en passant
- Fix windows DPI scaling problems with sprites
- Optimize: killer moves, pawn position evaluation
- Update screenshots

## Screenshots
![Screenshot 2024-12-20 014551](https://github.com/user-attachments/assets/49d0751e-ca2f-4777-a1f0-f6982b322109)
![Screenshot 2024-12-20 014826](https://github.com/user-attachments/assets/64708562-02ed-48bd-a771-c923e1336437)
