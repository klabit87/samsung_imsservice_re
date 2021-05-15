package com.sec.internal.helper.picturetool;

public class ReadScaleCalculator {
    public static int calculate(long size, long maxSize) throws IllegalArgumentException {
        return Math.max((int) Math.sqrt(((double) size) / ((double) maxSize)), 1);
    }

    public static int calculate(long size, int width, int height, long maxSize, int maxWidth, int maxHeight) {
        return Math.max(calculate(size, maxSize), Math.max(Math.max(width / maxWidth, 1), Math.max(height / maxHeight, 1)));
    }
}
