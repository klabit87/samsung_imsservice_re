package com.sec.internal.ims.servicemodules.im.util;

import android.net.Uri;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class FileTaskUtil {
    public static final int CONNECTION_TIMEOUT = 10000;
    private static final String LOG_TAG = FileTaskUtil.class.getSimpleName();
    public static final int MAX_CHUNK_SIZE = 512000;
    public static final int MAX_PROGRESS_COUNT = 50;
    public static final int MIN_CHUNK_SIZE = 61440;
    public static final long MIN_ELAPSED_TIME = 200;
    public static final int READ_DATA_TIMEOUT = 120000;
    public static final String UTF_8 = "UTF-8";

    public static String createRequestUrl(String url, Map<String, String> params) {
        Log.i(LOG_TAG, "createRequestUrl:");
        Uri.Builder urlBuilder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                urlBuilder.appendQueryParameter(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "Unsupported encoding, add key as it is");
                urlBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return urlBuilder.build().toString();
    }
}
