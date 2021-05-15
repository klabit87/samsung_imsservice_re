package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class DefaultContext extends JpgContext {
    private static final String LOG_TAG = DefaultContext.class.getSimpleName();

    public DefaultContext(ExifProcessor exifProcessor) {
        super(exifProcessor);
    }

    public File getFinalFilePath(File directory, String fileName) throws IOException {
        return super.getFinalFilePath(directory, changeExtToJpg(fileName));
    }

    public String toString() {
        return DefaultContext.class.getSimpleName();
    }

    private String changeExtToJpg(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf(".")) + ".jpg";
    }

    public void processSpecificData(File originalFile, File destinationFile) throws IOException {
        Log.d(LOG_TAG, "processSpecificData: Exit");
    }
}
