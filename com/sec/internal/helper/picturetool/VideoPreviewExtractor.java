package com.sec.internal.helper.picturetool;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.picturetool.IVideoPreviewExtractor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VideoPreviewExtractor implements IVideoPreviewExtractor {
    private static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    private static final String EXTENSION = "jpg";
    private static final String LOG_TAG = VideoPreviewExtractor.class.getSimpleName();
    private static final Pair<Integer, Integer> MINI_DIMENSIONS = Pair.create(512, 384);
    private static final int QUALITY = 75;
    private BitmapExtractor mBitmapExtractor;

    public VideoPreviewExtractor(BitmapExtractor bitmapExtractor) {
        this.mBitmapExtractor = bitmapExtractor;
    }

    public IVideoPreviewExtractor.IVideoPreview extract(File originalFile, File destinationDirectory) throws IOException {
        String videoPath = originalFile.getAbsolutePath();
        String destPreviewName = extractFileNameCore(originalFile.getName()) + "." + EXTENSION;
        Log.d(LOG_TAG, "extract: destPreviewName=" + destPreviewName);
        Bitmap defaultBitmap = extractDefaultBitmap(originalFile);
        final Pair<Integer, Integer> previewDimensions = calculatePreviewDimensions(extractVideoDimensions(videoPath));
        final File file = UniqueFilePathResolver.getUniqueFile(destPreviewName, destinationDirectory);
        if (MINI_DIMENSIONS.equals(previewDimensions)) {
            saveBitmapToFile(defaultBitmap, file);
        } else {
            saveBitmapToFile(defaultBitmap, file, previewDimensions);
        }
        return new IVideoPreviewExtractor.IVideoPreview() {
            public File getFile() {
                return file;
            }

            public long getSize() {
                return file.length();
            }

            public Pair<Integer, Integer> getDimensions() {
                return previewDimensions;
            }
        };
    }

    private String extractFileNameCore(String videoPath) throws IOException {
        int lastDot = videoPath.lastIndexOf(".");
        if (lastDot < 0) {
            throwIOE("lack of extension:%s", videoPath);
        }
        return videoPath.substring(0, lastDot);
    }

    private Pair<Integer, Integer> extractVideoDimensions(String videoPath) {
        MediaMetadataRetriever videoMeta = new MediaMetadataRetriever();
        videoMeta.setDataSource(videoPath);
        return Pair.create(Integer.valueOf(Integer.parseInt(videoMeta.extractMetadata(18))), Integer.valueOf(Integer.parseInt(videoMeta.extractMetadata(19))));
    }

    private Pair<Integer, Integer> calculatePreviewDimensions(Pair<Integer, Integer> videoDimensions) {
        return VideoScaleCalculator.calculate(((Integer) videoDimensions.first).intValue(), ((Integer) videoDimensions.second).intValue(), ((Integer) MINI_DIMENSIONS.first).intValue(), ((Integer) MINI_DIMENSIONS.second).intValue());
    }

    private Bitmap extractDefaultBitmap(File video) throws IOException {
        return this.mBitmapExtractor.extractFromVideo(video);
    }

    private static void throwIOE(String format, Object... params) throws IOException {
        throw new IOException(String.format(format, params));
    }

    private static void closeStream(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                Log.w(LOG_TAG, "error closing stream", e);
            }
        }
    }

    private static void saveBitmapToFile(Bitmap sourceBitmap, File destFilePath, Pair<Integer, Integer> destDimensions) throws IOException {
        int height;
        int width;
        OutputStream dataStream = null;
        try {
            if (sourceBitmap.getWidth() > sourceBitmap.getHeight()) {
                width = ((Integer) destDimensions.first).intValue();
                height = ((Integer) destDimensions.second).intValue();
            } else {
                width = ((Integer) destDimensions.second).intValue();
                height = ((Integer) destDimensions.first).intValue();
            }
            dataStream = new FileOutputStream(destFilePath);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, false);
            boolean result = scaledBitmap.compress(COMPRESS_FORMAT, 75, dataStream);
            if (!scaledBitmap.sameAs(sourceBitmap)) {
                scaledBitmap.recycle();
            }
            dataStream.flush();
            if (!result) {
                throwIOE("failure while compressing:%s,%d", destFilePath, 75);
            }
        } finally {
            closeStream(dataStream);
        }
    }

    private void saveBitmapToFile(Bitmap sourceBitmap, File destFilePath) throws IOException {
        OutputStream dataStream = null;
        try {
            dataStream = new FileOutputStream(destFilePath);
            boolean result = sourceBitmap.compress(COMPRESS_FORMAT, 75, dataStream);
            dataStream.flush();
            if (!result) {
                throwIOE("failure while compressing:%s,%d", destFilePath, 75);
            }
        } finally {
            closeStream(dataStream);
        }
    }
}
