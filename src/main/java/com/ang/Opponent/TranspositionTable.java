package com.ang.Opponent;

import com.ang.Global;
import com.ang.Piece;
import com.ang.Moves.Move;
import com.ang.Util.BoardRecord;

import java.util.HashMap;

public class TranspositionTable {
    private HashMap<Integer, int[]> hashes = new HashMap<Integer, int[]>();
    private int[] zobristTable;

    public void init() {
        // 12 pieces * 64 squares + 1 moveCol + 8 ep files + 4 castling rights
        this.zobristTable = new int[781]; 
        for (int i = 0; i < zobristTable.length; i++) {
            zobristTable[i] = (int) Global.pseudoRandom();
        }
    }

    private int indexOfPiece(int piece) {
        boolean isBlack = ((piece & 0b11000) == Piece.BLACK.val());
        int pieceVal = (isBlack) ? piece * 2 : piece;
        return (pieceVal & 0b111) * 64; 
    }

    public int zobristHash(BoardRecord rec, int moveCol) {
        int h = 0;
        
        // piece positions
        for (int i = 0; i < rec.board.length; i++) {
            int piece = rec.board[i];
            if (piece == Piece.NONE.val()) {
                continue;
            }
            h ^= zobristTable[indexOfPiece(piece)];
        }

        // colour to move
        if (moveCol == Piece.WHITE.val()) {
            h ^= zobristTable[64 * 12];
        }

        // en passsant pawn file
        int epPawnFile = rec.epPawnPos % 8;
        h ^= zobristTable[64 * 12 + 1 + epPawnFile];

        // castling rights are boolean (0 or 1)
        // in order white short, white long, black short, black long
        for (int i = 0; i < rec.cRights.length; i++) {
            if (rec.cRights[i] == 1) {
                h ^= zobristTable[64 * 12 + 1 + 8 + i];
            }
        }

        return h;
    }

    public void saveHash(int h, Move bestMove) {
        hashes.put(h, new int[]{bestMove.from, bestMove.to});
    }

    public Move searchTable(int h) {
        int[] fromTo = hashes.getOrDefault(h, new int[]{-1, -1});
        if ((fromTo[0] == -1) || (fromTo[1] == -1)) {
            return Move.invalid();
        }
        return new Move(fromTo[0], fromTo[1]);
    }
}
