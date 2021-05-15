package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public abstract class SupportedContextAdapter implements IContentTypeContext {
    private static final String LOG_TAG = SupportedContextAdapter.class.getSimpleName();

    public File getFinalFilePath(File directory, String fileName) throws NullPointerException, IOException {
        return UniqueFilePathResolver.getUniqueFile(fileName, directory);
    }

    public void validateExtension() throws IOException {
        Log.v(LOG_TAG, "validateExtension:: Exit");
    }
}
