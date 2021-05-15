package com.sec.internal.ims.entitlement.util;

import android.util.Base64;
import android.util.Log;

public class Base64Decoder {
    private static final String LOG_TAG = Base64Decoder.class.getSimpleName();

    public static String decode(String input) {
        if (input == null) {
            return null;
        }
        try {
            return new String(Base64.decode(input, 2));
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }
}
