package com.sec.internal.ims.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.sec.ims.extensions.ReflectionUtils;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class ThumbnailUtil {
    private static final int HIGH_QUALITY = 100;
    private static final String LOG_TAG = ThumbnailUtil.class.getSimpleName();
    private static final int MAX_BYTE_COUNT = 5120;
    private static final int MAX_BYTE_COUNT_HIGH = 51200;
    private static final int MAX_THUMBNAIL_SIZE = 512;
    private static final int QUALITY = 95;

    public static Bitmap getThumbnailBitmap(String filePath, String contentType) {
        Bitmap thumbBitmap;
        Matrix matrix;
        String str = filePath;
        String str2 = LOG_TAG;
        Log.d(str2, "filePath: " + str);
        Bitmap thumbBitmap2 = null;
        String type = contentType != null ? contentType : getFileType(filePath);
        if (type != null) {
            if (type.startsWith(TMOConstants.CallLogTypes.VIDEO)) {
                thumbBitmap2 = ThumbnailUtils.createVideoThumbnail(str, 1);
            } else if (type.startsWith(CallConstants.ComposerData.IMAGE)) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(str, opts);
                int width = opts.outWidth;
                int height = opts.outHeight;
                int inSampleSize = 1;
                String str3 = LOG_TAG;
                Log.d(str3, "width: " + width + ", height: " + height);
                if (height > 512 || width > 512) {
                    while (height / inSampleSize > 512 && width / inSampleSize > 512) {
                        inSampleSize *= 2;
                    }
                }
                String str4 = LOG_TAG;
                Log.d(str4, "inSampleSize: " + inSampleSize);
                if (inSampleSize > 1) {
                    opts.inSampleSize = inSampleSize;
                }
                opts.inJustDecodeBounds = false;
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap thumbBitmap3 = BitmapFactory.decodeFile(str, opts);
                if (!(thumbBitmap3 == null || (matrix = readPictureDegree(filePath)) == null)) {
                    try {
                        thumbBitmap2 = Bitmap.createBitmap(thumbBitmap3, 0, 0, thumbBitmap3.getWidth(), thumbBitmap3.getHeight(), matrix, true);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    } catch (NullPointerException e2) {
                        e2.printStackTrace();
                    }
                }
                thumbBitmap2 = thumbBitmap3;
            }
        }
        if (thumbBitmap2 == null) {
            return null;
        }
        if (thumbBitmap2.getByteCount() > MAX_BYTE_COUNT) {
            int scale = Math.max((int) Math.sqrt(((double) thumbBitmap2.getByteCount()) / 5120.0d), 1);
            int width2 = thumbBitmap2.getWidth() / scale;
            int height2 = thumbBitmap2.getHeight() / scale;
            String str5 = LOG_TAG;
            Log.d(str5, "Width: " + width2 + ", height: " + height2);
            thumbBitmap = Bitmap.createScaledBitmap(thumbBitmap2, width2, height2, false);
        } else {
            thumbBitmap = thumbBitmap2;
        }
        if (thumbBitmap.getConfig() == Bitmap.Config.RGB_565) {
            return thumbBitmap;
        }
        try {
            return thumbBitmap.copy(Bitmap.Config.RGB_565, false);
        } catch (OutOfMemoryError e3) {
            e3.printStackTrace();
            return thumbBitmap;
        }
    }

    public static byte[] getThumbnailByteArray(String filePath, String contentType) {
        Bitmap thumbBitmap = getThumbnailBitmap(filePath, contentType);
        if (thumbBitmap == null) {
            return null;
        }
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 95, ostream);
        byte[] byteArray = ostream.toByteArray();
        try {
            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    public static Matrix readPictureDegree(String path) {
        Matrix matrix = new Matrix();
        try {
            switch (new ExifInterface(path).getAttributeInt("Orientation", 1)) {
                case 2:
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case 3:
                    matrix.postRotate(180.0f);
                    return matrix;
                case 4:
                    matrix.postScale(1.0f, -1.0f);
                    return matrix;
                case 5:
                    matrix.preRotate(90.0f);
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case 6:
                    matrix.postRotate(90.0f);
                    return matrix;
                case 7:
                    matrix.preRotate(-90.0f);
                    matrix.postScale(-1.0f, 1.0f);
                    return matrix;
                case 8:
                    matrix.postRotate(270.0f);
                    return matrix;
                default:
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return matrix;
        }
    }

    public static byte[] getHighResolutionVideoThumbnailByteArray(String filePath, String contentType) {
        String str = LOG_TAG;
        Log.d(str, "filePath HR : " + filePath);
        Bitmap thumbBitmap = null;
        String type = contentType != null ? contentType : getFileType(filePath);
        if (!TextUtils.isEmpty(type)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(filePath);
                if (TextUtils.equals(retriever.extractMetadata(17), "yes") || type.startsWith(TMOConstants.CallLogTypes.VIDEO)) {
                    try {
                        thumbBitmap = retriever.getFrameAtTime(-1);
                        try {
                            retriever.release();
                        } catch (RuntimeException e) {
                        }
                    } catch (RuntimeException e2) {
                        retriever.release();
                    } catch (Throwable th) {
                        try {
                            retriever.release();
                        } catch (RuntimeException e3) {
                        }
                        throw th;
                    }
                    if (thumbBitmap == null) {
                        thumbBitmap = ThumbnailUtils.createVideoThumbnail(filePath, 1);
                    }
                    if (thumbBitmap != null && thumbBitmap.getByteCount() > MAX_BYTE_COUNT_HIGH) {
                        int scale = Math.max((int) Math.sqrt(((double) thumbBitmap.getByteCount()) / 51200.0d), 1);
                        int width = thumbBitmap.getWidth() / scale;
                        int height = thumbBitmap.getHeight() / scale;
                        String str2 = LOG_TAG;
                        Log.d(str2, "Width: " + width + ", height: " + height);
                        thumbBitmap = Bitmap.createScaledBitmap(thumbBitmap, width, height, false);
                    }
                    if (thumbBitmap != null) {
                        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                        byte[] byteArray = ostream.toByteArray();
                        try {
                            ostream.close();
                        } catch (IOException e4) {
                            e4.printStackTrace();
                        }
                        return byteArray;
                    }
                } else {
                    Log.d(LOG_TAG, "getHighResolutionThumbnailByteArray not for Video");
                }
            } catch (IllegalArgumentException e5) {
                Log.e(LOG_TAG, "getHighResolutionVideoThumbnailByteArray() failure");
                return null;
            }
        }
        return null;
    }

    private static String getFileType(String filePath) {
        int ext_index = filePath.lastIndexOf(".");
        String str = LOG_TAG;
        Log.d(str, "filePath: " + filePath);
        if (ext_index == -1) {
            return null;
        }
        String extension = filePath.substring(ext_index + 1);
        String str2 = LOG_TAG;
        Log.d(str2, "extension: " + extension);
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ENGLISH));
        if (type == null) {
            type = getMimeTypeForFile(filePath);
        }
        String str3 = LOG_TAG;
        Log.d(str3, "type: " + type);
        return type;
    }

    private static String getMimeTypeForFile(String filePath) {
        try {
            return (String) ReflectionUtils.invoke2(Class.forName("android.media.MediaFile").getMethod("getMimeTypeForFile", new Class[]{String.class}), (Object) null, new Object[]{filePath});
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
