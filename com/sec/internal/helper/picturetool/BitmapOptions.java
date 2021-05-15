package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapOptions {
    public static BitmapFactory.Options createData(int sampleSize, Bitmap.Config preferredConfig) throws IllegalArgumentException, NullPointerException {
        BitmapFactory.Options dataOptions = new BitmapFactory.Options();
        dataOptions.inSampleSize = sampleSize;
        dataOptions.inPreferredConfig = preferredConfig;
        dataOptions.inPurgeable = true;
        dataOptions.inInputShareable = true;
        return dataOptions;
    }
}
