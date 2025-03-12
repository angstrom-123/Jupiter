package com.ang.Core;

public class BitBoard {
    /**
     * Checks if a bitboard contains data
     * @param bb bitboard to check
     * @return {@code true} if it is empty, else {@code false}
     */
    public static boolean isEmpty(long bb) {
        return (bb == 0);

    }

    /**
     * Finds the index of a specific bit in the bitboard
     * @param bitPos the value represented by the bit
     * @return the index of the bit
     */
    public static int indexOfBit(long bitPos) {
        if (isEmpty(bitPos)) {
            System.err.println("Failed to find index in bitboard as it is empty");
            return -1;
        }
        if (bitPos == Long.MIN_VALUE) {
            return 63;

        }
        return (int) (Math.log(bitPos) / Math.log(2)); // log2(bbInt)

    }

    /**
     * Flips a given bit to a 1
     * @param bb the bitboard where the bit should be activated
     * @param index the index of the bit to activate
     * @return the updated bitboard
     */
    public static long activateBit(long bb, int index) {
        long bitPos;
        if (index < 63) {
            bitPos = (long) Math.pow(2, index);
        } else {
            bitPos = Long.MIN_VALUE;
        }
        bb |= bitPos;
        return bb;

    }

    /**
     * Flips a given bit to a 0
     * @param bb the bitboard where the bit should be deactivated
     * @param index the index of the bit to deactivate
     * @return the updated bitboard
     */
    public static long deactivateBit(long bb, int index) {
        long bitPos;
        if (index < 63) {
            bitPos = (long) Math.pow(2, index);
        } else {
            bitPos = Long.MIN_VALUE;
        }
        bb &= ~bitPos;
        return bb;

    }

    /**
     * Checks if a given bit is active
     * @param bb the bitboard where to check for the bit
     * @param index the index of the bit to check
     * @return {@code true} if the bit is a 1, else {@code false}
     */
    public static boolean bitActive(long bb, int index) {
        long bitPos;
        if (index < 63) {
            bitPos = (long) Math.pow(2, index);
        } else {
            bitPos = Long.MIN_VALUE;
        }
        return (bb & bitPos) == bitPos;

    }

    /**
     * Finds the indices of all active bits in the bitboard
     * @param bb the bitboard to search through
     * @return an array of indices of active bits
     */
    public static int[] setBits(long bb) {
        int[] out = new int[64];
        int end = 0;
        for (int i = 0; i < out.length; i++) {
            long checkBit = (1L << i);
            if ((bb & checkBit) == checkBit) {
                out[end++] = i;
            }
        }
        if (end < out.length) {
            out[end] = -1;
        }
        return out;

    }

    /**
     * Displays the bitboard as an 8x8 grid
     * @param bb the bitboard to display
     */
    public static void displayBB(long bb) {
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) {
                System.out.println();
            }
            if (bitActive(bb, i)) {
                System.out.print("##");
            } else {
                System.out.print("[]");
            }
        }
        System.out.println();
    }
}
