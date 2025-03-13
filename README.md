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
![Screenshot 2025-03-13 021620](https://github.com/user-attachments/assets/97d9264a-a084-4c1a-941f-c9af67e3982c)
![Screenshot 2025-03-13 021331](https://github.com/user-attachments/assets/f68b0f4c-8775-42da-9ca5-80977b74efe8)
