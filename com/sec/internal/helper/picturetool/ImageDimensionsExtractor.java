package com.sec.internal.helper.picturetool;

import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageDimensionsExtractor {
    private static final String LOG_TAG = ImageDimensionsExtractor.class.getSimpleName();

    public Pair<Integer, Integer> extract(File input) {
        String str = LOG_TAG;
        Log.d(str, "getImageDimensions:" + input.getAbsolutePath());
        FileInputStream controlStream = null;
        Pair<Integer, Integer> dimensions = null;
        try {
            BitmapFactory.Options controlOptions = new BitmapFactory.Options();
            controlOptions.inJustDecodeBounds = true;
            controlStream = new FileInputStream(input);
            BitmapFactory.decodeStream(controlStream, (Rect) null, controlOptions);
            dimensions = Pair.create(Integer.valueOf(controlOptions.outWidth), Integer.valueOf(controlOptions.outHeight));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            closeStream((Closeable) null);
            throw th;
        }
        closeStream(controlStream);
        return dimensions;
    }

    private static void closeStream(Closeable cloaseable) {
        if (cloaseable != null) {
            try {
                cloaseable.close();
            } catch (IOException e) {
                String str = LOG_TAG;
                Log.d(str, "closeStream: e=" + e);
            }
        }
    }
}
