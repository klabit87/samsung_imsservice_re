package com.sec.internal.helper;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.translate.FileExtensionTranslator;
import java.io.File;
import java.io.IOException;

public class FilePathGenerator {
    protected static final int FILE_RENAME_HASHVALUE_LEN = 5;
    private static final String LOG_TAG = FilePathGenerator.class.getSimpleName();
    protected static final String sReceivedFilesDir = (File.separatorChar + "Samsung Messages");
    protected static final String sThumbnailDir = (File.separatorChar + ".thumbnail");

    public static String generateUniqueFilePath(String directory, String fileFullName, int maxFileNameLength) {
        if (TextUtils.isEmpty(fileFullName)) {
            return null;
        }
        int lastDot = fileFullName.lastIndexOf(".");
        if (lastDot < 0) {
            int counter = 1;
            StringBuilder stringBuilder = new StringBuilder();
            String fileNameNew = fileFullName;
            if (fileNameNew.length() < maxFileNameLength) {
                while (new File(directory, fileNameNew).exists()) {
                    stringBuilder.setLength(0);
                    stringBuilder.append(fileFullName);
                    stringBuilder.append('(');
                    stringBuilder.append(counter);
                    stringBuilder.append(")");
                    fileNameNew = stringBuilder.toString();
                    counter++;
                }
            } else {
                fileNameNew = fileNameNew.substring(0, maxFileNameLength - 5) + StringGenerator.generateString(5, 5);
            }
            String generatedPath = new File(directory, fileNameNew).getPath();
            Log.d(LOG_TAG, "generateUniqueFilePath: " + generatedPath);
            return generatedPath;
        }
        String name = fileFullName.substring(0, lastDot);
        String extension = fileFullName.substring(lastDot + 1);
        Log.d(LOG_TAG, "extractFileNameCoreAndExtension" + fileFullName + " returned value:" + name + " " + extension);
        int counter2 = 1;
        StringBuilder stringBuilder2 = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(".");
        sb.append(extension);
        String fileNameNew2 = sb.toString();
        if (name.length() < maxFileNameLength) {
            while (new File(directory, fileNameNew2).exists()) {
                stringBuilder2.setLength(0);
                stringBuilder2.append(name);
                stringBuilder2.append('(');
                stringBuilder2.append(counter2);
                stringBuilder2.append(')');
                stringBuilder2.append('.');
                stringBuilder2.append(extension);
                fileNameNew2 = stringBuilder2.toString();
                counter2++;
            }
        } else {
            fileNameNew2 = name.substring(0, maxFileNameLength - 5) + StringGenerator.generateString(5, 5) + "." + extension;
        }
        String generatedPath2 = new File(directory, fileNameNew2).getPath();
        Log.d(LOG_TAG, "generateUniqueFilePath: " + generatedPath2);
        return generatedPath2;
    }

    public static String generateUniqueThumbnailPath(String fileName, String contentType, String directory, int maxFileNameLength) {
        String name;
        String dir = getIncomingFileDestinationDir(directory) + sThumbnailDir;
        File folder = new File(dir);
        if (!folder.exists() && folder.mkdirs()) {
            Log.d(LOG_TAG, "create Unique Thumbnail folder success ");
        }
        if (FileExtensionTranslator.isTranslationDefined(contentType)) {
            name = fileName + "." + FileExtensionTranslator.translate(contentType);
        } else {
            name = fileName + ".jpg";
        }
        return generateUniqueFilePath(dir, name + ".tmp" + StringGenerator.generateString(3, 3), maxFileNameLength);
    }

    public static String getIncomingFileDestinationDir(String directory) {
        String defaultDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        if (TextUtils.isEmpty(directory)) {
            return defaultDirectory;
        }
        String dir = directory;
        File folder = new File(dir);
        if (!folder.exists() && !folder.mkdir()) {
            Log.e(LOG_TAG, "can not create dir. Use default download directory.");
            dir = defaultDirectory;
        }
        if (folder.exists()) {
            File nomedia = new File(dir + File.separatorChar + ".nomedia");
            if (!nomedia.exists()) {
                try {
                    if (nomedia.createNewFile()) {
                        String str = LOG_TAG;
                        Log.d(str, "makeDirectoryToCopyImage, created .nomedia file in: " + dir);
                    } else {
                        String str2 = LOG_TAG;
                        Log.e(str2, "makeDirectoryToCopyImage, created failed in: " + dir);
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "makeDirectoryToCopyImage, cannot create .nomedia file");
                    e.printStackTrace();
                }
            }
        }
        return dir;
    }

    public static String renameThumbnail(String thumbnailPath, String thumbnailContentType, String fileName, int maxFileNameLength) {
        boolean renameSuccess = true;
        if (thumbnailPath == null) {
            Log.d(LOG_TAG, "mThumbnailPath is null");
            return null;
        }
        File oldFile = new File(thumbnailPath);
        Log.d(LOG_TAG, "temporary thumbnail path: " + thumbnailPath);
        String dir = oldFile.getParent();
        String extension = ".jpg";
        if (FileExtensionTranslator.isTranslationDefined(thumbnailContentType)) {
            extension = "." + FileExtensionTranslator.translate(thumbnailContentType);
        }
        String thumbnailPath2 = generateUniqueFilePath(dir, fileName + extension, maxFileNameLength);
        Log.d(LOG_TAG, "new file path: " + thumbnailPath2);
        if (oldFile.renameTo(new File(thumbnailPath2))) {
            Log.d(LOG_TAG, "Thumbnail rename success");
        } else {
            Log.d(LOG_TAG, "Thumbnail rename failure");
            renameSuccess = false;
        }
        if (renameSuccess) {
            return thumbnailPath2;
        }
        return null;
    }

    public static String getFileDownloadPath(Context context, boolean isStack) {
        if (context != null) {
            File fileDir = context.getExternalFilesDir((String) null);
            if (fileDir != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(fileDir.getAbsolutePath());
                sb.append(isStack ? "" : sReceivedFilesDir);
                return sb.toString();
            }
            Log.e(LOG_TAG, "Failed to get external files directory.");
        }
        return null;
    }
}
