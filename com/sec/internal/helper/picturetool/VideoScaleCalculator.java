package com.sec.internal.helper.picturetool;

import android.util.Pair;

public class VideoScaleCalculator {
    public static Pair<Integer, Integer> calculate(int videoWidth, int videoHeight, int thumbnailWidth, int thumbnailHeight) throws IllegalArgumentException {
        double scale = Math.max(Math.max(((double) videoWidth) / ((double) thumbnailWidth), ((double) videoHeight) / ((double) thumbnailHeight)), 1.0d);
        return Pair.create(Integer.valueOf(Math.max((int) (((double) videoWidth) / scale), 1)), Integer.valueOf(Math.max((int) (((double) videoHeight) / scale), 1)));
    }
}
