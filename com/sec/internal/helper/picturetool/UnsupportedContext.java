package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class UnsupportedContext implements IContentTypeContext {
    private static final String LOG_TAG = UnsupportedContext.class.getSimpleName();

    public Bitmap.CompressFormat getDestinationFormat() {
        throw new RuntimeException("BAD ACCESS");
    }

    public void validateExtension() throws IOException {
        Log.d(LOG_TAG, "unsupported image format");
        throw new IOException();
    }

    public File getFinalFilePath(File directory, String fileName) throws IOException {
        throw new RuntimeException("BAD ACCESS");
    }

    public String toString() {
        return UnsupportedContext.class.getSimpleName();
    }

    public void processSpecificData(File originalFile, File destinationFile) throws IOException {
        throw new RuntimeException("BAD ACCESS");
    }
}
