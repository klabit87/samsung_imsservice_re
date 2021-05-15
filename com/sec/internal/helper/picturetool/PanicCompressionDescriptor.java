package com.sec.internal.helper.picturetool;

import android.util.Log;
import android.util.Pair;

public class PanicCompressionDescriptor implements ICompressionDescriptor {
    private static String LOG_TAG = PanicCompressionDescriptor.class.getSimpleName();

    public Pair<Integer, Integer> next(long currentSize) {
        Log.e(LOG_TAG, "thow exception: conditions impossible to meet");
        return null;
    }
}
