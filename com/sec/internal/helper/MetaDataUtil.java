package com.sec.internal.helper;

import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MetaDataUtil {
    private static String checkMetaInfo(File file, String mimeType) {
        String metaInfo = mimeType;
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!mimeType.equalsIgnoreCase("video/mp4") || TextUtils.isEmpty(extension)) {
            return metaInfo;
        }
        if ("3gp".equalsIgnoreCase(extension) || SmsMessage.FORMAT_3GPP.equalsIgnoreCase(extension) || "3g2".equalsIgnoreCase(extension)) {
            return "video/3gpp";
        }
        return metaInfo;
    }

    public static String getContentType(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            retriever.setDataSource(fis.getFD());
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e4) {
            e4.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (RuntimeException e5) {
            e5.printStackTrace();
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e6) {
                    e6.printStackTrace();
                }
            }
            throw th;
        }
        return checkMetaInfo(file, retriever.extractMetadata(12));
    }
}
