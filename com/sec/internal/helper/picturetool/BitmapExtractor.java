package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BitmapExtractor {
    private static final String LOG_TAG = BitmapExtractor.class.getSimpleName();
    private static final Bitmap.Config PREFERRED_IMAGE_CONFIG = Bitmap.Config.RGB_565;

    public Bitmap extractFromImage(File image, int scale) throws NullPointerException, IllegalArgumentException, IOException {
        String str = LOG_TAG;
        Log.d(str, "extractBitmapFromImage(image=" + image + ", scale=" + scale + ")");
        FileInputStream dataStream = null;
        try {
            BitmapFactory.Options dataOptions = BitmapOptions.createData(scale, PREFERRED_IMAGE_CONFIG);
            dataStream = new FileInputStream(image);
            return BitmapFactory.decodeStream(dataStream, (Rect) null, dataOptions);
        } finally {
            closeStream(dataStream);
        }
    }

    public Bitmap extractFromVideo(File video) throws NullPointerException, IOException {
        String videoPath = video.getAbsolutePath();
        String str = LOG_TAG;
        Log.d(str, "extractFromVideo(videoPath=" + videoPath + ")");
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoPath, 1);
        if (thumb == null) {
            throwIOE("invalid input:%s", videoPath);
        }
        Log.d(LOG_TAG, "extractFromVideo:: Exit");
        return thumb;
    }

    private static void closeStream(Closeable cloaseable) throws IOException {
        if (cloaseable != null) {
            try {
                cloaseable.close();
            } catch (IOException e) {
                throw new IOException("Can't close stream: e=" + e);
            }
        }
    }

    private static void throwIOE(String format, Object... params) throws IOException {
        throw new IOException(String.format(format, params));
    }
}
