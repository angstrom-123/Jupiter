package com.ang.Engine.Transposition;

import com.ang.Core.BoardRecord;
import com.ang.Core.Piece;
import com.ang.Engine.TableEntry;

import java.util.HashMap;

// TODO : add limit on table size, cull extremely outdated transpositions

public class TranspositionTable extends ZobristTable {
    public int size = 0;

    private HashMap<Integer, TableEntry> hashes = new HashMap<Integer, TableEntry>();

    public TranspositionTable() {
        // 12 pieces * 64 squares + 1 moveCol + 8 ep files + 4 castling rights
        super(781);
    }

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

    public void saveHash(TableEntry entry, BoardRecord rec, int moveCol) {
        saveHash(entry, zobristHash(rec, moveCol));
    }
    public void saveHash(TableEntry entry, int hash) {
        TableEntry oldEntry = searchTable(hash);
        if (oldEntry == null) {
            hashes.put(hash, entry);
            size++;
        } else if ((entry.nodeType.precedence() > oldEntry.nodeType.precedence())
                || (entry.depth > oldEntry.depth)) {
            hashes.remove(hash);
            hashes.put(hash, entry);
        }
    }

    public TableEntry searchTable(int hash) {
        return hashes.getOrDefault(hash, null);
    }
}
