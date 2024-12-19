package com.ang.Opponent;

import com.ang.Global;
import com.ang.Piece;
import com.ang.Util.BoardRecord;

import java.util.HashMap;

public class TranspositionTable {
    public int size = 0;

    private HashMap<Integer, TableEntry> hashes = new HashMap<Integer, TableEntry>();
    private int[] zobristTable;

    public TranspositionTable() {
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
            if (piece != Piece.NONE.val()) {
                h ^= zobristTable[indexOfPiece(piece)];
            }
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

    // TODO : fix this
    public void saveHash(TableEntry entry, int hash) {
        TableEntry oldEntry = searchTable(hash);
        if (oldEntry == null) {
            hashes.put(hash, entry);
            size++;
        } else {
            handleCollision(entry, oldEntry, hash);  
        }     
    }

    public TableEntry searchTable(int hash) {
        return hashes.getOrDefault(hash, null);
    }

    public void handleCollision(TableEntry oldEntry, TableEntry newEntry, int hash) {
        if ((newEntry.nodeType.precedence() > oldEntry.nodeType.precedence())
                || (newEntry.depth > oldEntry.depth)) {
            hashes.remove(hash);
            hashes.put(hash, newEntry);
        }
    }
}
