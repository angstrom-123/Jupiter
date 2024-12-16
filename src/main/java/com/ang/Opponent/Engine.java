package com.ang.Opponent;

import com.ang.Board;
import com.ang.Global;
import com.ang.Piece;
import com.ang.Moves.*;
import com.ang.Util.BoardRecord;

// TODO : optimization:
//      - Increase search depth when there are fewer pieces on the board
//      - iterative deepening
//      - history heuristic
//      - killer moves
//      - quiescent search
//      - pawn position evaluation

public class Engine {  
    private int timeLimit;
    private int col;
    private int opCol;

    public Engine(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.col = col;
        this.opCol = (col == Piece.WHITE.val()) 
        ? Piece.BLACK.val() 
        : Piece.WHITE.val();
    }

    public Engine(int searchTime, Piece col) {
        this(searchTime, col.val());
    }

    public Move generateMove(BoardRecord rec) {
        Move lastDepthBest = Move.invalid();
        int maxDepth = 1;

        boolean doStop = false;
        long endTime = System.currentTimeMillis() + timeLimit;
        while (true) {
            Move bestMove = Move.invalid();
    
            double bestEval = -Global.infinity;
    
            // MoveList possibleMoves = rec.possibleMoves(moveCol);
    
            // for (int i = 0; i < possibleMoves.length() - 1; i++) {
            //     if (System.currentTimeMillis() >= endTime) {
            //         doStop = true;
            //         break;
            //     }

            //     BoardRecord tempRec = rec.copy();
            //     Move tempMove = possibleMoves.at(i);
                
            //     if (tempRec.tryMove(tempMove)) {
            //         double eval = -alphaBeta(tempRec, PieceColour.opposite(moveCol),
            //                 -Global.infinity, Global.infinity, maxDepth);
    
            //         if (eval > bestEval) {
            //             bestEval = eval;
            //             bestMove = tempMove;
            //         }
            //     }
            // }


            // for (int pos : rec.allPieces) {
            //     if (pos == -1) {
            //         break;
            //     }
            //     if ((rec.board[pos] & 0b11000) != col) {
            //         continue;
            //     }
            MoveList moves = Board.allMoves(rec, col);
                
            for (int i = 0; i < moves.length(); i++) {
                if (System.currentTimeMillis() >= endTime) {
                    doStop = true;
                    break;
                }
    
                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }

                BoardRecord tempRec = rec.copy();
                if (Board.tryMove(tempRec, m)) {
                    double eval = -alphaBeta(tempRec, opCol,
                        -Global.infinity, Global.infinity, maxDepth);

                    if (eval > bestEval) {
                        bestEval = eval;
                        System.out.println("depth: "+maxDepth+" eval: "+eval+" from: "+m.from+" to: "+m.to);
                        bestMove = m;
                    }
                }
            }

            if (doStop) {
                break;
            }

            maxDepth++;

            // TODO: implement checkmate
            if (bestMove.isInvalid()) {
                System.out.println("couldn't find move");
            } else {
                lastDepthBest = bestMove;
            }
        }

        System.out.println("maximum search depth: "+maxDepth);
        System.out.println("final move - from: "+lastDepthBest.from+" to: "+lastDepthBest.to);
        return lastDepthBest;
    }

    public double alphaBeta(BoardRecord rec, int moveCol, 
            double alpha, double beta, int depth) {
        
        if (depth == 0) {
            double eval = evaluate(rec);
            return eval;
        }

        MoveList moves = Board.allMoves(rec, moveCol);
        // System.out.println(moves.length());
        if (moveCol == Piece.WHITE.val()) {
            double max = -Global.infinity;
            for (int i = 0; i < moves.length(); i++) {
                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }

                BoardRecord tempRec = rec.copy();

                if (Board.tryMove(tempRec, m)) {
                    double eval = alphaBeta(tempRec, Piece.BLACK.val(),
                            alpha, beta, depth - 1);
                    max = Math.max(eval, max);
                    alpha = Math.max(alpha, max);
                    if (max >= beta) {
                        break;
                    }
                }
            }
            return max;
        } else {
            double min = Global.infinity;

            for (int i = 0; i < moves.length(); i++) {
                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }

                BoardRecord tempRec = rec.copy();

                if (Board.tryMove(tempRec, m)) {
                    double eval = alphaBeta(tempRec, Piece.WHITE.val(),
                            alpha, beta, depth - 1);
                    min = Math.min(eval, min);
                    beta = Math.min(beta, min);
                    if (min <= alpha) {
                        break;
                    }
                }
            }
            return min;
        }
    }

    private double pieceValue(BoardRecord rec, int pos) {
        double value = 0.0;
        int pieceCol = rec.board[pos] & 0b11000;
        int heatmapIndex = (pieceCol == Piece.WHITE.val())
        ? pos
        : 63 - pos;

        switch (rec.board[pos] & 0b111) {
        case 1:
            value = 100.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 2:
            value = 320.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 3:
            value = 330.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 4:
            value = 500.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 5:
            value = 900.0 + Heatmap.pawnMap[heatmapIndex];
            break;
        case 6:
            double[] heatmap = (rec.minorPieceCount() < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = 20000.0 + heatmap[heatmapIndex];
            break;
        default:
            value = 0.0;
        }

        if (pieceCol == Piece.BLACK.val()) {
            value = -value;
        } 
        return value;
    }

    private double evaluate(BoardRecord rec) {
        double eval = 0.0;

        for (int pos : rec.allPieces) {
            if (pos == -1) {
                break;
            }
            eval += pieceValue(rec, pos);
        }

        return eval;
    }
}