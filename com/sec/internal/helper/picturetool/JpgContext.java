package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class JpgContext extends SupportedContextAdapter {
    private static final String LOG_TAG = JpgContext.class.getSimpleName();
    private ExifProcessor mExifProcessor;

    public JpgContext(ExifProcessor exifProcessor) {
        this.mExifProcessor = exifProcessor;
    }

    public Bitmap.CompressFormat getDestinationFormat() {
        return Bitmap.CompressFormat.JPEG;
    }

    public String toString() {
        return JpgContext.class.getSimpleName();
    }

    public void processSpecificData(File originalFile, File destinationFile) throws IOException {
        Log.d(LOG_TAG, "processSpecificData: Enter");
        try {
            this.mExifProcessor.process(originalFile, destinationFile);
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException from ExifProcessor but use destinationFile");
        }
        Log.d(LOG_TAG, "processSpecificData: Exit");
    }
}
