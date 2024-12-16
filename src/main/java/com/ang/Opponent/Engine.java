package com.ang.Opponent;

import com.ang.Board;
import com.ang.Global;
import com.ang.Piece;
import com.ang.Moves.*;
import com.ang.Util.BoardRecord;

// TODO : optimization:
//      - iterative deepening
//      - history heuristic
//      - killer moves
//      - quiescent search
//      - pawn position evaluation

public class Engine {  
    private int timeLimit;
    private int engineCol;
    private int playerCol;

    public Engine(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.engineCol = col;
        this.playerCol = Piece.WHITE.val();
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
            double bestEval = -Global.INFINITY;
    
            MoveList possibleMoves = Board.allMoves(rec, engineCol);
            for (int i = 0; i < possibleMoves.length() - 1; i++) {
                if (System.currentTimeMillis() >= endTime) {
                    doStop = true;
                    break;
                }
                if (possibleMoves.at(i).flag == Flag.ONLY_ATTACK) {
                    continue;
                }

                BoardRecord tempRec = rec.copy();
                Move tempMove = possibleMoves.at(i);
                
                if (Board.tryMove(tempRec, tempMove)) {
                    double eval = -alphaBeta(tempRec, playerCol,
                            -Global.INFINITY, Global.INFINITY, maxDepth);
    
                    if (eval > bestEval) {
                        bestEval = eval;
                        System.out.println("depth: "+maxDepth+" eval: "+eval+" from: "+tempMove.from+" to: "+tempMove.to);
                        bestMove = tempMove;
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
            double eval = evaluate(rec); // TODO : quiescence search here
            return eval;
        }

        MoveList possibleMoves = Board.allMoves(rec, moveCol);
        if (moveCol == Piece.WHITE.val()) { // max
            double max = -Global.INFINITY;
            for (int i = 0; i < possibleMoves.length() - 1; i++) {
                BoardRecord tempRec = rec.copy();
                Move tempMove = possibleMoves.at(i);
                if (Board.tryMove(tempRec, tempMove)) {
                    double eval = alphaBeta(tempRec, Piece.BLACK.val(),
                            alpha, beta, depth - 1);
                    max = Math.max(eval, max);
                    alpha = Math.max(alpha, max);
                    if (max >= beta) {
                        return eval;
                    }
                }
            }
            return max;
        } else {
            double min = Global.INFINITY;
            for (int i = 0; i < possibleMoves.length() - 1; i++) {
                BoardRecord tempRec = rec.copy();
                Move tempMove = possibleMoves.at(i);
                if (Board.tryMove(tempRec, tempMove)) {
                    double eval = alphaBeta(tempRec, Piece.WHITE.val(),
                            alpha, beta, depth - 1);
                    min = Math.min(eval, min);
                    beta = Math.min(beta, min);
                    if (min <= alpha) {
                        return eval;
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