package com.sec.internal.ims.servicemodules.im.util;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class FtFallbackHttpUrlUtil {
    private static final String AMPERSAND = "&";
    private static final Pattern FALLBACK_PARAMS_PATTERN = Pattern.compile("\\?[tse]=.*&[tse]=.*&[tse]=.*");
    private static final String LOG_TAG = FtFallbackHttpUrlUtil.class.getSimpleName();
    private static final String QUERY = "?";

    public static boolean areFallbackParamsPresent(String fileDataUrl) throws NullPointerException {
        Preconditions.checkNotNull(fileDataUrl);
        return FALLBACK_PARAMS_PATTERN.matcher(fileDataUrl).find();
    }

    public static String addFtFallbackParams(String fileDataUrl, long fileSize, String fileContentType, String fileDataUntil) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull(fileDataUrl, "fileDataUrl");
        Preconditions.checkArgument(!FALLBACK_PARAMS_PATTERN.matcher(fileDataUrl).find(), "Invalid fileDataUrl format!");
        Preconditions.checkNotNull(fileContentType, "fileContentType");
        Preconditions.checkNotNull(fileDataUntil, "fileDataUntil");
        return fileDataUrl + QUERY + "t=" + encodeRFC3986(fileContentType) + AMPERSAND + "s=" + fileSize + AMPERSAND + "e=" + fileDataUntil.replace("-", "").replace(":", "");
    }

    public static String addDurationFtFallbackParam(String fileDataUrl, int playingLength) throws NullPointerException, IllegalArgumentException {
        Preconditions.checkNotNull(fileDataUrl);
        Preconditions.checkArgument(FALLBACK_PARAMS_PATTERN.matcher(fileDataUrl).find(), "Invalid fileDataUrl format!");
        return fileDataUrl + AMPERSAND + "d=" + playingLength;
    }

    private static String encodeRFC3986(String value) {
        try {
            return URLEncoder.encode(value, "utf-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, e.toString());
            e.printStackTrace();
            return value;
        }
    }
}
