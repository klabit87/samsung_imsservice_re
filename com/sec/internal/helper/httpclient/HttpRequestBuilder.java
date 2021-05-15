package com.sec.internal.helper.httpclient;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequestBuilder {
    private static final String TAG = HttpRequestBuilder.class.getSimpleName();

    public static Request buildRequest(HttpRequestParams requestParams) {
        Request.Builder reqBuilder;
        HttpQueryParams queryParams = requestParams.getQueryParams();
        HttpUrl parsedUrl = HttpUrl.parse(requestParams.getUrl());
        if (queryParams == null || parsedUrl == null) {
            reqBuilder = new Request.Builder().url(requestParams.getUrl());
        } else {
            try {
                HttpUrl.Builder builder = parsedUrl.newBuilder();
                Map<String, String> parameters = queryParams.getParams();
                if (queryParams.isEncoded()) {
                    for (Map.Entry<String, String> entry : parameters.entrySet()) {
                        builder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
                    }
                } else {
                    for (Map.Entry<String, String> entry2 : parameters.entrySet()) {
                        builder.addQueryParameter(entry2.getKey(), entry2.getValue());
                    }
                }
                reqBuilder = new Request.Builder().url(builder.build());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "URL is wrong");
                return null;
            }
        }
        buildRequestHeader(requestParams, reqBuilder);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[requestParams.getMethod().ordinal()];
        if (i == 1) {
            return reqBuilder.build();
        }
        if (i == 2) {
            return buildDeleteRequest(requestParams, reqBuilder);
        }
        if (i == 3 || i == 4) {
            return buildPostOrPutRequest(requestParams, reqBuilder);
        }
        return null;
    }

    /* renamed from: com.sec.internal.helper.httpclient.HttpRequestBuilder$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method;

        static {
            int[] iArr = new int[HttpRequestParams.Method.values().length];
            $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method = iArr;
            try {
                iArr[HttpRequestParams.Method.GET.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[HttpRequestParams.Method.DELETE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[HttpRequestParams.Method.POST.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$HttpRequestParams$Method[HttpRequestParams.Method.PUT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private static void buildRequestHeader(HttpRequestParams requestParams, Request.Builder reqBuilder) {
        Map<String, String> reqHeaders = requestParams.getHeaders();
        if (reqHeaders != null) {
            for (Map.Entry<String, String> entry : reqHeaders.entrySet()) {
                if (!(entry == null || entry.getKey() == null || entry.getValue() == null)) {
                    reqBuilder.header(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static Request buildDeleteRequest(HttpRequestParams requestParams, Request.Builder reqBuilder) {
        if (requestParams.getPostBody() == null) {
            Log.d(TAG, "buildDeleteRequest: delete all");
            reqBuilder.delete();
            return reqBuilder.build();
        }
        reqBuilder.delete(buildRequestBody(requestParams));
        return reqBuilder.build();
    }

    private static Request buildPostOrPutRequest(HttpRequestParams requestParams, Request.Builder reqBuilder) {
        RequestBody requestBody;
        String contentType = getContentType(requestParams);
        String str = TAG;
        Log.d(str, "buildPostOrPutRequest: " + contentType);
        if (requestParams.getPostBody() == null) {
            reqBuilder.method(requestParams.getMethod().name(), RequestBody.create(MediaType.parse(contentType), ""));
        } else if (isContentMatching(contentType, HttpPostBody.CONTENT_TYPE_MULTIPART)) {
            String boundary = null;
            int startIndexOfBoundary = contentType.indexOf("boundary=");
            int endIndexOfBoundary = "boundary=".length() + startIndexOfBoundary;
            if (startIndexOfBoundary != -1) {
                boundary = contentType.substring(endIndexOfBoundary).trim();
                String str2 = TAG;
                Log.d(str2, "boundary:" + boundary);
            }
            MultipartBuilder multBuilder = new MultipartBuilder();
            if (boundary != null) {
                multBuilder = new MultipartBuilder(boundary);
            }
            if (setMultipartType(multBuilder, contentType)) {
                buildMultipart(multBuilder, requestParams.getPostBody().getMultiparts());
                reqBuilder.method(requestParams.getMethod().name(), multBuilder.build());
            }
        } else if (isContentMatching(contentType, "application/x-www-form-urlencoded")) {
            if (requestParams.getPostBody().getJSONBody() != null) {
                requestBody = buildFormEncodingBody(requestParams);
            } else {
                requestBody = buildRequestBody(requestParams);
            }
            reqBuilder.method(requestParams.getMethod().name(), requestBody);
        } else {
            MediaType mediaType = MediaType.parse(contentType);
            if (requestParams.getPostBody().getFile() != null) {
                reqBuilder.method(requestParams.getMethod().name(), RequestBody.create(mediaType, requestParams.getPostBody().getFile()));
            } else if (requestParams.getPostBody().getBody() != null) {
                reqBuilder.method(requestParams.getMethod().name(), buildRequestBody(requestParams));
            } else {
                reqBuilder.method(requestParams.getMethod().name(), RequestBody.create(mediaType, requestParams.getPostBody().getData()));
            }
        }
        return reqBuilder.build();
    }

    private static RequestBody buildFormEncodingBody(HttpRequestParams requestParams) {
        FormEncodingBuilder formEncodingbuilder = new FormEncodingBuilder();
        JSONObject body = requestParams.getPostBody().getJSONBody();
        String key = null;
        Iterator<String> keys = body.keys();
        while (keys.hasNext()) {
            try {
                key = keys.next();
                formEncodingbuilder.add(key, body.getString(key));
            } catch (JSONException e) {
                String str = TAG;
                Log.e(str, "buildFormEncodingBody: failed to load value " + key);
                e.printStackTrace();
            }
        }
        return formEncodingbuilder.build();
    }

    private static void buildMultipart(MultipartBuilder multBuilder, List<HttpPostBody> list) {
        if (list == null || list.size() <= 0) {
            Log.e(TAG, "buildMultipart: list is empty");
            return;
        }
        for (HttpPostBody body : list) {
            if (body.getMultiparts() == null || body.getMultiparts().size() <= 0) {
                Map<String, String> header = new HashMap<>();
                header.put(HttpController.HEADER_CONTENT_DISPOSITION, body.getContentDisposition());
                if (!TextUtils.isEmpty(body.getContentTransferEncoding())) {
                    header.put(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, body.getContentTransferEncoding());
                }
                if (!TextUtils.isEmpty(body.getFileIcon())) {
                    header.put(HttpController.HEADER_FILE_ICON, body.getFileIcon());
                }
                if (!TextUtils.isEmpty(body.getContentId())) {
                    header.put(HttpController.HEADER_CONTENT_ID, body.getContentId());
                }
                if (body.getFile() != null) {
                    multBuilder.addPart(Headers.of(header), RequestBody.create(MediaType.parse(body.getContentType()), body.getFile()));
                } else if (body.getBody() != null) {
                    multBuilder.addPart(Headers.of(header), RequestBody.create(MediaType.parse(body.getContentType()), body.getBody()));
                } else if (body.getData() != null) {
                    multBuilder.addPart(Headers.of(header), RequestBody.create(MediaType.parse(body.getContentType()), body.getData()));
                } else {
                    Log.e(TAG, "buildMultipart: body, file and data are null.");
                }
            } else {
                MultipartBuilder innerMBuilder = new MultipartBuilder();
                if (setMultipartType(innerMBuilder, body.getContentType())) {
                    buildMultipart(innerMBuilder, body.getMultiparts());
                    multBuilder.addPart(Headers.of(HttpController.HEADER_CONTENT_DISPOSITION, body.getContentDisposition()), innerMBuilder.build());
                }
            }
        }
    }

    private static RequestBody buildRequestBody(HttpRequestParams requestParams) {
        MediaType mediaType = MediaType.parse(getContentType(requestParams));
        String body = requestParams.getPostBody().getBody();
        byte[] data = requestParams.getPostBody().getData();
        if (isGzipSupported(requestParams)) {
            if (data != null) {
                try {
                    return RequestBody.create(mediaType, GzipCompressionUtil.compress(data));
                } catch (IOException e) {
                    Log.e(TAG, "buildRequestBody: body compression failed");
                    return null;
                }
            } else if (body != null) {
                return RequestBody.create(mediaType, GzipCompressionUtil.compress(body));
            } else {
                Log.e(TAG, "buildRequestBody: body construction failed");
                return null;
            }
        } else if (data != null) {
            return RequestBody.create(mediaType, data);
        } else {
            if (body != null) {
                return RequestBody.create(mediaType, body);
            }
            Log.e(TAG, "buildRequestBody: body compression failed");
            return null;
        }
    }

    private static boolean setMultipartType(MultipartBuilder mBuilder, String contentType) {
        if (contentType.contains(HttpPostBody.CONTENT_TYPE_MULTIPART_FORMDATA)) {
            mBuilder.type(MultipartBuilder.FORM);
            return true;
        } else if (contentType.contains("multipart/mixed")) {
            mBuilder.type(MultipartBuilder.MIXED);
            return true;
        } else {
            Log.e(TAG, "setMultipartType: wrong content-type, should be multipart.");
            return false;
        }
    }

    private static String getContentType(HttpRequestParams requestParams) {
        String contentType = requestParams.getHeaders().get("Content-Type");
        if (!TextUtils.isEmpty(contentType)) {
            return contentType;
        }
        Log.d(TAG, "getContentType: no content type, set to default");
        return HttpPostBody.CONTENT_TYPE_DEFAULT;
    }

    private static String getContentEncoding(HttpRequestParams requestParams) {
        String contentEncoding = requestParams.getHeaders().get("Content-Encoding");
        if (!TextUtils.isEmpty(contentEncoding)) {
            return contentEncoding;
        }
        Log.d(TAG, "getContentEncoding: no content encoding, set to null");
        return null;
    }

    private static boolean isContentMatching(String orgType, String targetType) {
        return Pattern.compile(Pattern.quote(targetType), 2).matcher(orgType).find();
    }

    private static boolean isGzipSupported(HttpRequestParams requestParams) {
        String encoding = getContentEncoding(requestParams);
        return encoding != null && encoding.equalsIgnoreCase("gzip");
    }
}
