package com.sec.internal.helper;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.os.Debug;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class FileUtils {
    private static final boolean DBG = (!Debug.isProductShip());
    private static final String FILE_PROVIDER_AUTHORITY = "com.sec.internal.ims.rcs.fileprovider";
    private static final String LOG_TAG = FileUtils.class.getSimpleName();
    private static final int MAX_FILE_NAME_LENGTH = 128;

    public static boolean copyFile(File sourceFile, File destFile) {
        try {
            copyFileOrThrow(sourceFile, destFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void copyFileOrThrow(File sourceFile, File destFile) throws IOException {
        FileChannel destination;
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = new FileInputStream(sourceFile).getChannel();
        try {
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            if (destination != null) {
                destination.close();
            }
            if (source != null) {
                source.close();
                return;
            }
            return;
        } catch (Throwable th) {
            if (source != null) {
                try {
                    source.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
        throw th;
    }

    public static String copyFileFromUri(Context context, String fileUri, String fileName) {
        InputStream is;
        if (TextUtils.isEmpty(fileUri)) {
            return null;
        }
        if (!fileUri.startsWith("content")) {
            return fileUri;
        }
        if (DBG) {
            String str = LOG_TAG;
            Log.i(str, "File from TP : " + fileUri);
        }
        File cacheDir = context.getCacheDir();
        if (cacheDir == null) {
            Log.e(LOG_TAG, "Unable to get Cache Dir!");
            return null;
        }
        String internalFilePath = FilePathGenerator.generateUniqueFilePath(cacheDir.getAbsolutePath(), fileName, 128);
        if (internalFilePath == null) {
            Log.e(LOG_TAG, "Create internal path failed!!!");
            return null;
        }
        File out = new File(internalFilePath);
        try {
            is = context.getContentResolver().openInputStream(Uri.parse(fileUri));
            if (is == null) {
                Log.e(LOG_TAG, "TP URI open failed!!!!");
                if (is != null) {
                    is.close();
                }
                return null;
            }
            String str2 = LOG_TAG;
            Log.i(str2, fileUri + " ==> " + out.getAbsolutePath());
            long start = System.currentTimeMillis();
            Files.copy(is, out.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            if (DBG) {
                long end = System.currentTimeMillis();
                String str3 = LOG_TAG;
                Log.i(str3, "Copy duration : " + (end - start));
            }
            String absolutePath = out.getAbsolutePath();
            if (is != null) {
                is.close();
            }
            return absolutePath;
        } catch (IOException e) {
            String str4 = LOG_TAG;
            Log.e(str4, "File get from TP failed by " + e);
            return null;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static String getUriForFileAsString(Context context, String path) {
        return (String) Optional.ofNullable(getUriForFile(context, path)).map($$Lambda$cYL0L0IM52LyojJwo43i0vo5gn4.INSTANCE).orElse(path);
    }

    public static Uri getUriForFile(Context context, String path) {
        if (path == null) {
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            Uri contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
            context.grantUriPermission(ImsConstants.Packages.PACKAGE_SEC_MSG, contentUri, 1);
            if (DBG) {
                String str = LOG_TAG;
                Log.i(str, "grantPermission : uri=" + contentUri);
            }
            return contentUri;
        } catch (Exception e) {
            String str2 = LOG_TAG;
            Log.e(str2, "grantPermission failed. " + e);
            return null;
        }
    }

    public static boolean removeFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
