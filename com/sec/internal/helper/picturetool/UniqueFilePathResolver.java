package com.sec.internal.helper.picturetool;

import android.util.Log;
import java.io.File;
import java.io.IOException;

public class UniqueFilePathResolver {
    public static File getUniqueFile(String name, File destDir) {
        String fileName = new File(destDir.getAbsoluteFile(), name).getName();
        int extOffset = fileName.lastIndexOf(".");
        if (extOffset == -1) {
            extOffset = fileName.length();
        }
        String n = fileName.substring(0, extOffset);
        String ext = fileName.substring(extOffset);
        String newFileName = name;
        int count = 1;
        while (new File(destDir.getAbsoluteFile(), newFileName).exists()) {
            newFileName = n + "(" + count + ")" + ext;
            count++;
        }
        File f = new File(destDir.getAbsoluteFile(), newFileName);
        Log.d("UniqueFilePathResolver", "file path=" + f.getAbsolutePath());
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}
