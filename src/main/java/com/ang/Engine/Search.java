package com.ang.Engine;

import com.ang.Global;
import com.ang.Core.Board;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Core.Moves.*;

// TODO : bugfix - engine can sometimes move player's pieces
//          - (queen takes pawn, engine moves my queen);

// TODO : compare move gen results to StockFish in random positions
//      - need to allow engine to play as white and black
//      - check that arbitrary board states are loaded and usable correctly

// TODO : optimization:
//      - history heuristic
//      - killer moves
//      - pawn position evaluation
//      - futility pruning

// TODO : fixes
//      - bot doesn't find mate well in endgames, usually stalemate
public class Search {  
    public int      engineCol;

    private int     timeLimit;
    private int     playerCol;

    private boolean useAlphaBeta        = true;
    private boolean useMoveOrdering     = true;
    private boolean useTTable           = true;

    private TranspositionTable tTable   = new TranspositionTable();

    public Search(int searchTime, Piece col, boolean ab, boolean mo, boolean tt) {
        this(searchTime, col.val());
        this.useAlphaBeta       = ab;
        this.useMoveOrdering    = mo;
        this.useTTable          = tt;
    }
    public Search(int searchTime, Piece col) {
        this(searchTime, col.val());
    }
    public Search(int searchTime, int col) {
        this.timeLimit = searchTime;
        this.engineCol = col;
        this.playerCol = Piece.WHITE.val();
    }

    public Move generateMove(BoardRecord rec) {
        Move    lastPlyBestMove = Move.invalid();
        int     maxDepth        = 1;
        long    endTime         = System.currentTimeMillis() + timeLimit;
        while (true) {
            double bestEval = -Global.INFINITY;
            Move bestMove = Move.invalid();         
            MoveList moves = Board.allMoves(rec, engineCol);
            if (useMoveOrdering) {
                moves = orderMoves(rec, moves, engineCol);
            }
            for (int i = 0; i < moves.length(); i++) {
                if (System.currentTimeMillis() >= endTime) {
                    System.out.println("maximum complete depth: " 
                            + (maxDepth - 1));
                    System.out.println("final move from: " 
                            + lastPlyBestMove.from + " to: " + lastPlyBestMove.to);

                    return lastPlyBestMove;
                }

                Move m = moves.at(i);
                if (m.flag == Flag.ONLY_ATTACK) {
                    continue;
                }
                BoardRecord tempRec = rec.copy();
                
                if (Board.tryMove(tempRec, m)) {
                    double eval;
                    if (useAlphaBeta) {
                        eval = alphaBetaNega(tempRec, 
                                -Global.INFINITY, Global.INFINITY, 
                                playerCol, maxDepth);
                    } else {
                        eval = negaMax(tempRec, playerCol, maxDepth);
                    }
                    
                    if (engineCol == Piece.BLACK.val()) {
                        eval = -eval;
                    }
                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = m;

                        // System.out.println("depth: " + maxDepth + " eval: " 
                                // + eval + " from: " + m.from+" to: " + m.to);
                       
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

    private double negaMax(BoardRecord rec, int col, int depth) {
        if (depth == 0) {
            return (col == Piece.WHITE.val()) 
            ? evaluate(rec) 
            : -evaluate(rec);
        }

        int opCol = (col == Piece.WHITE.val()) 
        ? Piece.BLACK.val() 
        : Piece.WHITE.val();
        double bestEval = -Global.INFINITY;

        MoveList moves  = Board.allMoves(rec, col);
        if (useMoveOrdering) {
            moves = orderMoves(rec, moves, col);
        }
        for (int i = 0; i < moves.length(); i++) {
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, move)) {
                double eval = -negaMax(tempRec, opCol, depth - 1);
                if (eval > bestEval) {
                    bestEval = eval;
                    if (useTTable) {
                        // save to transposition table
                        TableEntry te = new TableEntry( 
                                SearchNode.PV,
                                move,
                                eval,
                                depth,
                                0        );
                        tTable.saveHash(te, tTable.zobristHash(tempRec, col));
                    }
                }
                if (useTTable) {
                    // save to transposition table
                    TableEntry te = new TableEntry( 
                        SearchNode.CUT,
                        move,
                        eval,
                        depth,
                        0        );
                    tTable.saveHash(te, tTable.zobristHash(tempRec, col));
                }
            }
        }
        return bestEval;
    }

    private double alphaBetaNega(BoardRecord rec, double alpha, double beta, 
            int col, int depth) {
        if (depth == 0) {
            return quiesce(rec, alpha, beta, col);
        }

        int opCol = (col == Piece.WHITE.val()) 
        ? Piece.BLACK.val() 
        : Piece.WHITE.val();
        double bestEval = -Global.INFINITY;

        MoveList moves  = Board.allMoves(rec, col);
        if (useMoveOrdering) {
            moves = orderMoves(rec, moves, col);
        }
        for (int i = 0; i < moves.length(); i++) {
            Move move = moves.at(i);
            if (move.flag == Flag.ONLY_ATTACK) {
                continue;
            }

            BoardRecord tempRec = rec.copy();
            if (Board.tryMove(tempRec, move)) {
                double eval = -alphaBetaNega(tempRec, 
                        -beta, -alpha, opCol, depth - 1);
                if (eval > bestEval) {
                    bestEval = eval;
                    if (eval > alpha) {
                        alpha = eval;
                        if (useTTable) {
                            // save to transposition table
                            TableEntry te = new TableEntry( 
                                    SearchNode.ALL,
                                    move,
                                    eval,
                                    depth,
                                    0        );
                            tTable.saveHash(te, tTable.zobristHash(tempRec, col));
                        }
                    } else {
                        if (useTTable) {
                            // save to transposition table
                            TableEntry te = new TableEntry( 
                                    SearchNode.PV,
                                    move,
                                    eval,
                                    depth,
                                    0        );
                            tTable.saveHash(te, tTable.zobristHash(tempRec, col));
                        }
                    }
                }
                if (useTTable) {
                    // save to transposition table
                    TableEntry te = new TableEntry( 
                        SearchNode.CUT,
                        move,
                        eval,
                        depth,
                        0        );
                    tTable.saveHash(te, tTable.zobristHash(tempRec, col));
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

        MoveList moves  = Board.allMoves(rec, col);
        moves           = orderMoves(rec, moves, col);
        
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
        double value        = 0.0;
        int pieceCol        = rec.board[pos] & 0b11000;
        int heatmapIndex    = (pieceCol == Piece.WHITE.val()) ? pos : 63 - pos;

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
            // king heatmap changes in endgame
            double[] heatmap = (rec.minorPieceCount() < 3)
            ? Heatmap.kingEndMap
            : Heatmap.kingStartMap;
            value = 20000.0 + heatmap[heatmapIndex];
            break;
        default:
            value = 0.0;
        }

        return (pieceCol == Piece.WHITE.val()) ? value : - value;
    }

    private double evaluate(BoardRecord rec) {
        double eval = 0.0;
        for (int pos : rec.allPieces.elements) {
            if (pos == -1) {
                break;
            }
            eval += pieceValue(rec, pos);
        }

        return eval;
    }

    private MoveList orderMoves(BoardRecord rec, MoveList moves, int moveCol) {
        moves.attacksToFront();

        if (useTTable) {
            TableEntry te = tTable.searchTable(tTable.zobristHash(rec, moveCol));
            if (te != null) { // tt hit
                moves.sendToFront(te.bestMove);
            }
        }
        
        return moves;
    }
}