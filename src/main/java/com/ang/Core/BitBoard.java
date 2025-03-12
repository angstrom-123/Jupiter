package com.ang.Core;

public class BitBoard {
    // long as unsigned
    // Long.compareUnsigned(a, b);
    // Long.divideUnsigned(a, b);

    public static boolean isEmpty(long bb) {
        return (bb == 0);
    }

    public static int indexOfBit(long bitPos) { // TODO : test if working unsigned
        if (bitPos == Long.MIN_VALUE) return 63;
        return (int) (Math.log(bitPos) / Math.log(2)); // log2(bbInt)
    }

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

    public static long deactivateBit(long bb, int index) {
        long bitPos;
        if (index < 63) {
            bitPos = (long) Math.pow(2, index);
        } else {
            bitPos = Long.MIN_VALUE;
        }
        bb &= ~bitPos; // TODO : check that this is actualy NAND
        return bb;
    }

    public static boolean bitActive(long bb, int index) {
        long bitPos;
        if (index < 63) {
            bitPos = (long) Math.pow(2, index);
        } else {
            bitPos = Long.MIN_VALUE;
        }
        return (bb & bitPos) == bitPos;
    }

    public static int[] setBits(long bb) {
        int[] out = new int[64];
        int end = 0;
        for (int i = 0; i < out.length; i++) {
            long checkBit = (1L << i);
            if ((bb & checkBit) == checkBit) out[end++] = i;
        }
        out[end] = -1;
        return out;
    }

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
