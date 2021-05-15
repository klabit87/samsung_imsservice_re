package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class PngContext extends SupportedContextAdapter {
    private static final String LOG_TAG = PngContext.class.getSimpleName();

    public Bitmap.CompressFormat getDestinationFormat() {
        return Bitmap.CompressFormat.PNG;
    }

    public String toString() {
        return PngContext.class.getSimpleName();
    }

    public void processSpecificData(File originalFile, File destinationFile) throws IOException {
        Log.d(LOG_TAG, "processSpecificData: Exit");
    }
}
