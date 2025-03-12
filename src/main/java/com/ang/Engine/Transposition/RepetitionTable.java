package com.ang.Engine.Transposition;

import java.util.HashMap;

import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;

/**
 * Class for the repetition table used to check for three-fold repetition draws
 */
public class RepetitionTable extends ZobristTable{
    private HashMap<Integer, Integer> history = new HashMap<Integer, Integer>();

    /**
     * Constructs a zobrist table with 768 elements
     */
    public RepetitionTable() {
        // 12 pieces * 64 squares
        super(768);  
    }

    /**
     * Zobrist Hashes only the positions of pieces in the position
     * @param rec BoardRecord representing the position to be hashed
     * @return the hash generated from the position
     */
    public int zobristHash(BoardRecord rec) {
        int h = 0;
        for (int i = 0; i < rec.board.length; i++) {
            int piece = rec.board[i];
            if (piece != Piece.NONE.val()) {
                h ^= zobristArray[indexOfPiece(piece, i)];
            }
        }
        return h; 

    }

    /**
     * Overload:
     * Saves a position to the repetition table
     * @param rec BoardRecord representing the position to be saved
     * @return the amount of times this position has been reached
     */
    public int saveRepetition(BoardRecord rec) {
        return saveRepetition(zobristHash(rec));

    }

    /**
     * Saves a position to the repetition table
     * @param hash the position hash to save the the repetition table
     * @return the amount of times this position has been entered into the table
     */
    public int saveRepetition(int hash) {
        int repCount = history.getOrDefault(hash, 0);
        if (repCount == 0) {
            size++;
            history.put(hash, 1);
        } else {
            history.remove(hash);
            repCount++;
            history.put(hash, repCount);
        }
        return repCount;

    }

    /**
     * Overload:
     * Checks how many times a position has been entered into the repetition table
     * @param rec the position to check for
     * @return the number of times it was entered into the transposition table
     */
    public int checkRepetitions(BoardRecord rec) {
        return checkRepetitions(zobristHash(rec));

    }

    /**
     * Checks how many times a position has been entered into the repetition table
     * @param hash the hash of the position to check for
     * @return the number of times it was entered into the transposition table
     */
    public int checkRepetitions(int hash) {
        return history.getOrDefault(hash, 0);

    }
}
