package com.sec.internal.ims.servicemodules.im.util;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileDurationUtil {
    private static final String LOG_TAG = FileDurationUtil.class.getSimpleName();

    public static int getFileDurationTime(String filePath) {
        Log.i(LOG_TAG, "getFileDurationTime");
        String str = LOG_TAG;
        IMSLog.s(str, "getFileDurationTime for filePath=" + filePath);
        long duration = 0;
        if (filePath == null) {
            return -1;
        }
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            return -1;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(targetFile);
            retriever.setDataSource(fis.getFD());
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException | IllegalArgumentException e2) {
            e2.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            throw th;
        }
        try {
            duration = Long.parseLong(retriever.extractMetadata(9));
            String str2 = LOG_TAG;
            Log.i(str2, "getFileDurationTime, time = " + duration);
        } catch (NumberFormatException e4) {
            e4.printStackTrace();
        }
        return (int) duration;
    }
}
