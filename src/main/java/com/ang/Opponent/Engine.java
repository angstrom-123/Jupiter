package com.ang.Opponent;

import com.ang.Board;
import com.ang.Global;
import com.ang.Piece;
import com.ang.Moves.*;
import com.ang.Util.BoardRecord;

// TODO : optimization:
//      - history heuristic
//      - killer moves
//      - quiescent search
//      - pawn position evaluation

// TODO : fixes
//      - bot doesn't find mate well in endgames, usually stalemate

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
        Move lastPlyBestMove = Move.invalid();
        int maxDepth = 1;

        long endTime = System.currentTimeMillis() + timeLimit;
        while (true) {
            Move bestMove = Move.invalid();
            double bestEval = -Global.INFINITY;
            MoveList moves = Board.allMoves(rec, engineCol);
            for (int i = 0; i < moves.length(); i++) {
                if (System.currentTimeMillis() >= endTime) {
                    System.out.println("maximum complete depth: " 
                            + (maxDepth - 1));
                    System.out.println("final move from: " 
                            + bestMove.from + " to: " + bestMove.to);
                    return lastPlyBestMove;
                }

                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }
                BoardRecord tempRec = rec.copy();
                
                if (Board.tryMove(tempRec, m)) {
                    double eval = alphaBetaNega(tempRec, 
                            -Global.INFINITY, Global.INFINITY, 
                            playerCol, maxDepth);
                    if (engineCol == Piece.BLACK.val()) {
                        eval = -eval;
                    }
                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = m;
                        System.out.println("depth: " + maxDepth + " eval: " 
                                + eval + " from: " + m.from+" to: " + m.to);
                    }
                }
            }

            if (bestMove.isInvalid()) {
                return bestMove;
            }
            lastPlyBestMove = bestMove;
            maxDepth++;
        }
    }

    private double alphaBetaNega(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        if (depth == 0) {
            return quiesce(rec, alpha, beta, col);
            // return (col == Piece.WHITE.val()) ? evaluate(rec) : -evaluate(rec);
        }

        int opCol = col == Piece.WHITE.val()
        ? Piece.BLACK.val()
        : Piece.WHITE.val();
        double bestEval = -Global.INFINITY;
        MoveList moves = Board.allMoves(rec, col);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (m.flag == Flag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, m)) {
                double eval = -alphaBetaNega(tempRec, 
                        -beta, -alpha, opCol, depth - 1);
                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                    }
                }
                if (alpha >= beta) {
                    break;
                }
            }
        }
        return bestEval;
    }

    private double quiesce(BoardRecord rec, double alpha, double beta, int col) {
        double standPat = (col == Piece.WHITE.val()) 
        ? evaluate(rec) 
        : -evaluate(rec);

        if (standPat > beta) { 
            return beta;
        }
        if (alpha < standPat) {
            alpha = standPat;
        }

        int opCol = col == Piece.WHITE.val()
        ? Piece.BLACK.val()
        : Piece.WHITE.val();
        MoveList moves = Board.allMoves(rec, col);
        for (int i = 0; i < moves.length(); i++) {
            Move m = moves.at(i);
            if (m.flag == Flag.ONLY_ATTACK) {
                continue;
            }
            if (rec.board[m.to] == Piece.NONE.val()) { // only consider captures
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, m)) {
                double eval = -quiesce(tempRec, -beta, -alpha, opCol);
                if (eval >= beta) {
                    return beta;
                }

                // do not delta prune in endgame
                if ((pieceValue(rec, m.to) + 200 > alpha)
                        && (rec.minorPieceCount() > 3)) {
                    return alpha;
                }

                if (eval > alpha) {
                    alpha = eval;
                }
            }
        }
        return alpha;
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