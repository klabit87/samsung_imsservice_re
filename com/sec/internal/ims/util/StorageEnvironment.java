package com.sec.internal.ims.util;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import java.io.File;

public class StorageEnvironment {
    private static String LOG_TAG = StorageEnvironment.class.getSimpleName();

    public static String generateStorePath(String contentName) {
        String fileName = new File(getDefaultStoreDirectory(1), contentName).getName();
        int extOffset = fileName.lastIndexOf(".");
        if (extOffset == -1) {
            extOffset = fileName.length();
        }
        String name = fileName.substring(0, extOffset);
        String ext = fileName.substring(extOffset);
        String newFileName = contentName;
        int count = 1;
        while (new File(getDefaultStoreDirectory(1), newFileName).exists()) {
            newFileName = name + "(" + count + ")" + ext;
            count++;
        }
        return new File(getDefaultStoreDirectory(1), newFileName).getAbsolutePath();
    }

    public static boolean isSdCardStateFine(long requestedStorage) {
        if (getSdCardFreeSpace(getExternalStorageDirectoryCreateIfNotExists(Environment.DIRECTORY_PICTURES)) > requestedStorage) {
            return true;
        }
        return false;
    }

    private static String getDefaultStoreDirectory(int shareType) {
        String envPath;
        String str = Environment.DIRECTORY_PICTURES;
        if (shareType == 1) {
            envPath = Environment.DIRECTORY_PICTURES;
        } else if (shareType != 2) {
            envPath = Environment.DIRECTORY_DOWNLOADS;
        } else {
            envPath = Environment.DIRECTORY_MOVIES;
        }
        return getExternalStorageDirectoryCreateIfNotExists(envPath);
    }

    private static String getExternalStorageDirectoryCreateIfNotExists(String envPath) {
        File path = Environment.getExternalStoragePublicDirectory(envPath);
        if (path.mkdirs() || path.isDirectory()) {
            return path.getPath();
        }
        String str = LOG_TAG;
        Log.d(str, "Environment " + envPath + " Error");
        return null;
    }

    private static long getSdCardFreeSpace(String path) {
        if (path == null) {
            Log.e(LOG_TAG, "path == null");
            return -1;
        } else if (new File(path).exists()) {
            return new StatFs(path).getAvailableBytes();
        } else {
            String str = LOG_TAG;
            Log.e(str, "path doesn't exist: '" + path + "'");
            return -1;
        }
    }
}
