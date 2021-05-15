package com.sec.internal.helper.picturetool;

import android.util.Pair;
import java.io.File;
import java.io.IOException;

public interface IVideoPreviewExtractor {

    public interface IVideoPreview {
        Pair<Integer, Integer> getDimensions();

        File getFile();

        long getSize();
    }

    IVideoPreview extract(File file, File file2) throws NullPointerException, IOException;
}
