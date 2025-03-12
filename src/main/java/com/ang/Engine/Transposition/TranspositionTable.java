package com.ang.Engine.Transposition;

import com.ang.Global;
import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Engine.TableEntry;

import java.util.HashMap;

/**
 * Class for a transposition table used for looking up previously searched positions
 */
public class TranspositionTable extends ZobristTable {
    public int size = 0;

    private HashMap<Integer, TableEntry> hashes = new HashMap<Integer, TableEntry>();

    /**
     * Constructs a zobrist table with 781 elements
     */
    public TranspositionTable() {
        // 12 pieces * 64 squares + 1 moveCol + 8 ep files + 4 castling rights
        super(781);
    }

    /**
     * Converts a board position into a zobrist hash
     * @param rec the BoardRecord representing the position to convert
     * @param moveCol the colour to move in the position
     * @return zobrist hash of the position
     */
    public int zobristHash(BoardRecord rec, int moveCol) {
        int h = 0;
        // piece positions
        for (int i = 0; i < rec.board.length; i++) {
            int piece = rec.board[i];
            if (piece != Piece.NONE.val()) {
                h ^= zobristArray[indexOfPiece(piece, i)];
            }
        }
        // colour to move
        if (moveCol == Piece.WHITE.val()) {
            h ^= zobristArray[64 * 12];
        }
        // en passsant pawn file
        int epPawnFile = rec.epPawnPos % 8;
        h ^= zobristArray[64 * 12 + 1 + epPawnFile];
        // castling rights are boolean (0 or 1)
        // in order white short, white long, black short, black long
        for (int i = 0; i < rec.cRights.length; i++) {
            if (rec.cRights[i] == 1) {
                h ^= zobristArray[64 * 12 + 1 + 8 + i];
            }
        }
        return h;

    }

    /**
     * Overload: 
     * Saves a zobrist hash and the corresponding TableEntry to the transposition table
     * @param entry TableEntry corresponding to the hashed position
     * @param rec BoardRecord to be hashed
     * @param moveCol colour to move in the position to be hashed
     */
    public void saveHash(TableEntry entry, BoardRecord rec, int moveCol) {
        saveHash(entry, zobristHash(rec, moveCol));
    }

    /**
     * Saves a zobrist hash and the corresponding TableEntry to the transposition table
     * @param entry TableEntry corresponding to the hashed position
     * @param hash the position hash to enter into the transposition table
     */
    public void saveHash(TableEntry entry, int hash) {
        TableEntry oldEntry = searchTable(hash);
        if (oldEntry == null) {
            hashes.put(hash, entry);
            size++;
        } else if ((entry.nodeType.precedence() > oldEntry.nodeType.precedence())
                || (entry.depth > oldEntry.depth)) {
            Global.ttColisions++;
            hashes.remove(hash);
            hashes.put(hash, entry);
        }
    }

    /**
     * Searches the transposition table for a given hash
     * @param hash the hash to search for
     * @return the hash's TableEntry if found, else null
     */
    public TableEntry searchTable(int hash) {
        TableEntry te = hashes.getOrDefault(hash, null);
        if (te != null) {
            Global.ttHits++;
        }
        return te;

    }
}
