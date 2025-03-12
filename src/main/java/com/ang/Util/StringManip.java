package com.ang.Util;

/**
 * Allows for simple string manipulation
 */
public class StringManip {
    /**
     * Centres a string within a given width
     * @param s the string to centre
     * @param width the width to take up
     * @return the centred string, or the string itself if @param width is too small
     */
    public static String centre(String s, int width) {
        int sLen = s.length();
        int diff = width - sLen;
        if (diff < 0) {
            return s;

        }
        int leftPad = (int) Math.ceil(diff / 2);
        int rightPad = (int) Math.floor(diff / 2);
        String out = "";
        for (int i = 0; i < leftPad; i++) out += " ";
        out += s;
        for (int i = 0; i < rightPad; i++) out += " ";
        return out;

    }
}
