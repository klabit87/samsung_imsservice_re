package com.sec.internal.ims.servicemodules.im;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.MIMEContentType;
import java.util.Locale;

public class ImMultipart {
    private static final String LOG_TAG = "ImMultipart";
    public static final String SHARED_CLIENT_DATA_CONTENT_TYPE = "application/vnd.gsma.botsharedclientdata.v1.0+json";
    public static final String SUGGESTION_RESPONSE_CONTENT_TYPE = "application/vnd.gsma.botsuggestion.response.v1.0+json";
    private String mBody;
    private String mContentType;
    private String mSuggestion;

    public ImMultipart(String content, String contentType) {
        String boundary;
        String[] parts;
        int endIndex;
        int startIndex;
        int index;
        int startIndex2;
        int endIndex2;
        String[] parts2;
        String str = content;
        String str2 = contentType;
        int index2 = str2.indexOf("boundary=");
        if (index2 == -1) {
        } else if (index2 + 9 > contentType.length()) {
            int i = index2;
        } else {
            int startIndex3 = index2 + 9;
            int endIndex3 = str2.indexOf(59, startIndex3);
            if (endIndex3 == -1) {
                boundary = str2.substring(startIndex3).replace("\"", "");
            } else {
                boundary = str2.substring(startIndex3, endIndex3).replace("\"", "");
            }
            String[] parts3 = str.split("\r?\n?--" + boundary + "(--)?\r?\n?");
            int length = parts3.length;
            int i2 = 0;
            while (i2 < length) {
                String part = parts3[i2];
                if (TextUtils.isEmpty(part)) {
                    index = index2;
                    startIndex = startIndex3;
                    endIndex = endIndex3;
                    parts = parts3;
                } else {
                    if (part.startsWith("\n")) {
                        index = index2;
                        startIndex2 = startIndex3;
                        endIndex2 = endIndex3;
                        parts2 = parts3;
                    } else if (part.startsWith("\r\n")) {
                        index = index2;
                        startIndex2 = startIndex3;
                        endIndex2 = endIndex3;
                        parts2 = parts3;
                    } else {
                        String type = null;
                        String[] headerBody = part.split("\r?\n\r?\n", 2);
                        index = index2;
                        if (headerBody.length == 2) {
                            String[] split = headerBody[0].split("\r?\n");
                            int length2 = split.length;
                            startIndex = startIndex3;
                            int startIndex4 = 0;
                            while (true) {
                                if (startIndex4 >= length2) {
                                    endIndex = endIndex3;
                                    parts = parts3;
                                    break;
                                }
                                endIndex = endIndex3;
                                String header = split[startIndex4];
                                String[] strArr = split;
                                parts = parts3;
                                String[] nameValue = header.split(": |:", 2);
                                String str3 = header;
                                if (nameValue.length == 2 && "content-type".equalsIgnoreCase(nameValue[0])) {
                                    type = nameValue[1].trim();
                                    break;
                                }
                                startIndex4++;
                                endIndex3 = endIndex;
                                split = strArr;
                                parts3 = parts;
                            }
                            if (type != null) {
                                if (type.contains(MIMEContentType.BOT_SUGGESTION)) {
                                    this.mSuggestion = headerBody[1];
                                } else {
                                    this.mBody = headerBody[1];
                                    this.mContentType = type;
                                }
                            }
                        } else {
                            startIndex = startIndex3;
                            endIndex = endIndex3;
                            parts = parts3;
                        }
                        if (type == null) {
                            this.mBody = part;
                            this.mContentType = "text/plain";
                        }
                    }
                    int index3 = part.indexOf("\n");
                    if (part.length() > index3 + 1) {
                        this.mBody = part.substring(index3 + 1);
                        this.mContentType = "text/plain";
                    }
                }
                i2++;
                index2 = index;
                startIndex3 = startIndex;
                endIndex3 = endIndex;
                parts3 = parts;
            }
            int i3 = startIndex3;
            Log.i(LOG_TAG, "boundary = " + boundary + ", ContentType = " + this.mContentType);
            return;
        }
        Log.e(LOG_TAG, "no boundary");
        this.mBody = str;
        this.mContentType = str2;
    }

    public static boolean isMultipart(String contentType) {
        if (contentType != null) {
            return contentType.toLowerCase(Locale.US).contains("multipart/mixed".toLowerCase(Locale.US));
        }
        return false;
    }

    public String getBody() {
        return this.mBody;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getSuggestion() {
        return this.mSuggestion;
    }
}
