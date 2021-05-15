package com.sec.internal.helper.httpclient;

import android.util.Log;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpResponseBuilder {
    private static final String TAG = HttpResponseBuilder.class.getSimpleName();

    public static HttpResponseParams buildResponse(Response response) {
        if (response == null) {
            Log.e(TAG, "buildResponse: okhttp response is null");
            return null;
        }
        HttpResponseParams result = new HttpResponseParams();
        Map<String, List<String>> resHeaders = new HashMap<>();
        for (String name : response.headers().names()) {
            resHeaders.put(name, response.headers(name));
        }
        result.setStatusCode(response.code());
        result.setHeaders(resHeaders);
        String dataString = null;
        try {
            result.setDataBinary(response.body().bytes());
            response.body().close();
            if (isGzipSupported(result)) {
                dataString = GzipCompressionUtil.decompress(result.getDataBinary());
            } else {
                dataString = new String(result.getDataBinary());
            }
        } catch (IOException e) {
            Log.e(TAG, "buildResponse: decompression failed, revoke");
        }
        result.setDataString(dataString);
        return result;
    }

    private static boolean isGzipSupported(HttpResponseParams requestParams) {
        List<String> encodings = getContentEncoding(requestParams);
        return encodings != null && containsIgnoreCase("gzip", encodings);
    }

    private static List<String> getContentEncoding(HttpResponseParams responseParam) {
        List<String> contentEncodingList = responseParam.getHeaders().get("Content-Encoding");
        if (contentEncodingList != null && !contentEncodingList.isEmpty()) {
            return contentEncodingList;
        }
        Log.d(TAG, "getContentEncoding: no content encoding, set to null");
        return null;
    }

    private static boolean containsIgnoreCase(String gzip, List<String> list) {
        for (String encoding : list) {
            if (encoding.equalsIgnoreCase(gzip)) {
                return true;
            }
        }
        return false;
    }
}
