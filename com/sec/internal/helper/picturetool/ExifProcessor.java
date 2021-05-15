package com.sec.internal.helper.picturetool;

import android.media.ExifInterface;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class ExifProcessor {
    private static final String LOG_TAG = ExifProcessor.class.getSimpleName();

    public void process(File originalFile, File destinationFile) throws IOException {
        Log.d(LOG_TAG, "process: Enter");
        int sourceOrientation = new ExifInterface(originalFile.getAbsolutePath()).getAttributeInt("Orientation", 0);
        ExifInterface destinationExifInterface = new ExifInterface(destinationFile.getAbsolutePath());
        destinationExifInterface.setAttribute("Orientation", Integer.toString(sourceOrientation));
        destinationExifInterface.saveAttributes();
        Log.d(LOG_TAG, "process: Exit");
    }
}
