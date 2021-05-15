package com.sec.internal.helper.httpclient;

import com.sec.internal.helper.header.AuthenticationHeaders;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class HttpPostBody {
    public static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
    public static final String CONTENT_DISPOSITION_FORM_DATA = "form-data";
    public static final String CONTENT_DISPOSITION_ICON = "icon";
    public static final String CONTENT_TRANSFER_ENCODING_BASE64 = "base64";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_DEFAULT = "application/octet-stream";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/";
    public static final String CONTENT_TYPE_MULTIPART_FORMDATA = "multipart/form-data";
    public static final String CONTENT_TYPE_MULTIPART_MIXED = "multipart/mixed";
    public static final String CONTENT_TYPE_MULTIPART_RELATED = "multipart/related";
    public static final String CONTENT_TYPE_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String TAG = HttpPostBody.class.getSimpleName();
    private String mBody = null;
    private long mBodySize = 0;
    private String mContentDisposition = null;
    private String mContentId = null;
    private String mContentTransferEncoding = null;
    private String mContentType = null;
    private byte[] mData = null;
    private File mFile = null;
    private String mFileIcon = null;
    private JSONObject mJSONBody = null;
    private List<HttpPostBody> mMultiparts = null;

    public HttpPostBody(String body) {
        this.mBody = body;
        this.mBodySize = (long) getFieldSize(body);
    }

    public HttpPostBody(JSONObject jsonBody) {
        this.mJSONBody = jsonBody;
        if (jsonBody != null) {
            this.mBody = jsonBody.toString();
        }
        this.mBodySize = (long) getFieldSize(this.mBody);
    }

    public HttpPostBody(File file) {
        this.mFile = file;
        if (file != null) {
            this.mBodySize = file.length();
        }
    }

    public HttpPostBody(byte[] data) {
        this.mData = data;
        if (data != null) {
            this.mBodySize = (long) data.length;
        }
    }

    public HttpPostBody(List<HttpPostBody> multipart) {
        this.mMultiparts = multipart;
        if (multipart != null) {
            for (HttpPostBody body : multipart) {
                this.mBodySize += body.getBodySize();
            }
        }
    }

    public HttpPostBody(Map<String, String> postParameters) {
        String convertPrams = convertPrams(postParameters);
        this.mBody = convertPrams;
        this.mBodySize = (long) getFieldSize(convertPrams);
    }

    public HttpPostBody(String contentDisposition, String contentType, String body) {
        this.mContentDisposition = contentDisposition;
        long fieldSize = (long) getFieldSize(contentDisposition);
        this.mBodySize = fieldSize;
        this.mContentType = contentType;
        long fieldSize2 = fieldSize + ((long) getFieldSize(contentType));
        this.mBodySize = fieldSize2;
        this.mBody = body;
        this.mBodySize = fieldSize2 + ((long) getFieldSize(body));
    }

    public HttpPostBody(String contentDisposition, String contentType, byte[] data) {
        this.mContentDisposition = contentDisposition;
        long fieldSize = (long) getFieldSize(contentDisposition);
        this.mBodySize = fieldSize;
        this.mContentType = contentType;
        long fieldSize2 = fieldSize + ((long) getFieldSize(contentType));
        this.mBodySize = fieldSize2;
        this.mData = data;
        if (data != null) {
            this.mBodySize = fieldSize2 + ((long) data.length);
        }
    }

    public HttpPostBody(String contentDisposition, String contentType, byte[] data, String fileIcon, String contentId) {
        this.mContentDisposition = contentDisposition;
        long fieldSize = (long) getFieldSize(contentDisposition);
        this.mBodySize = fieldSize;
        this.mContentType = contentType;
        long fieldSize2 = fieldSize + ((long) getFieldSize(contentType));
        this.mBodySize = fieldSize2;
        this.mData = data;
        if (data != null) {
            this.mBodySize = fieldSize2 + ((long) data.length);
        }
        this.mFileIcon = fileIcon;
        long fieldSize3 = this.mBodySize + ((long) getFieldSize(fileIcon));
        this.mBodySize = fieldSize3;
        this.mContentId = contentId;
        this.mBodySize = fieldSize3 + ((long) getFieldSize(contentId));
    }

    public HttpPostBody(String contentDisposition, String contentType, File file) {
        this.mContentDisposition = contentDisposition;
        long fieldSize = (long) getFieldSize(contentDisposition);
        this.mBodySize = fieldSize;
        this.mContentType = contentType;
        long fieldSize2 = fieldSize + ((long) getFieldSize(contentType));
        this.mBodySize = fieldSize2;
        this.mFile = file;
        if (file != null) {
            this.mBodySize = fieldSize2 + file.length();
        }
    }

    public HttpPostBody(String contentDisposition, String contentType, List<HttpPostBody> multipart) {
        this.mContentDisposition = contentDisposition;
        long fieldSize = (long) getFieldSize(contentDisposition);
        this.mBodySize = fieldSize;
        this.mContentType = contentType;
        this.mBodySize = fieldSize + ((long) getFieldSize(contentType));
        this.mMultiparts = multipart;
        for (HttpPostBody body : multipart) {
            this.mBodySize += body.getBodySize();
        }
    }

    public long getBodySize() {
        return this.mBodySize;
    }

    public String getContentDisposition() {
        return this.mContentDisposition;
    }

    public String getContentTransferEncoding() {
        return this.mContentTransferEncoding;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public String getFileIcon() {
        return this.mFileIcon;
    }

    public String getContentId() {
        return this.mContentId;
    }

    public String getBody() {
        return this.mBody;
    }

    public JSONObject getJSONBody() {
        return this.mJSONBody;
    }

    public File getFile() {
        return this.mFile;
    }

    public byte[] getData() {
        return this.mData;
    }

    public List<HttpPostBody> getMultiparts() {
        return this.mMultiparts;
    }

    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.mContentTransferEncoding = contentTransferEncoding;
    }

    private String convertPrams(Map<String, String> parameters) {
        try {
            return convertPrams(parameters, Charset.defaultCharset());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String convertPrams(Map<String, String> parameters, Charset charSet) throws UnsupportedEncodingException {
        boolean isFirstEntry = true;
        StringBuffer result = new StringBuffer();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (isFirstEntry) {
                isFirstEntry = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), charSet.name()));
            result.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            result.append(URLEncoder.encode(entry.getValue(), charSet.name()));
        }
        return result.toString();
    }

    public String toString() {
        try {
            return toString(0);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return "";
        }
    }

    private String toString(int depth) {
        String indent = makeIndent(depth);
        String indent2 = indent + "    ";
        StringBuffer multiparts = new StringBuffer();
        List<HttpPostBody> list = this.mMultiparts;
        if (list == null) {
            multiparts.append("null");
        } else {
            for (HttpPostBody body : list) {
                multiparts.append(body.toString(depth + 1));
            }
        }
        if (this.mBody != null) {
            return "\r\n" + indent + "HttpPostBody(depth" + depth + ")[\r\n" + indent2 + "mContentDisposition: " + this.mContentDisposition + "\r\n" + indent2 + "mContentTransferEncoding: " + this.mContentTransferEncoding + "\r\n" + indent2 + "mContentType: " + this.mContentType + "\r\n" + indent2 + "mBody: " + this.mBody + "\r\n" + indent2 + "mFile: " + this.mFile + "\r\n" + indent2 + "mMultiparts: " + multiparts + "\r\n" + indent + "]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n");
        sb.append(indent);
        sb.append("HttpPostBody(depth");
        sb.append(depth);
        sb.append(")[\r\n");
        sb.append(indent2);
        sb.append("mContentDisposition: ");
        sb.append(this.mContentDisposition);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mContentType: ");
        sb.append(this.mContentType);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mFileIcon: ");
        sb.append(this.mFileIcon);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mContentId: ");
        sb.append(this.mContentId);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mContentTransferEncoding: ");
        sb.append(this.mContentTransferEncoding);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mData: length is ");
        byte[] bArr = this.mData;
        sb.append(bArr != null ? bArr.length : 0);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mData: ");
        byte[] bArr2 = this.mData;
        sb.append((bArr2 == null || bArr2.length >= 8192) ? this.mData : new String(this.mData));
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mFile: ");
        sb.append(this.mFile);
        sb.append("\r\n");
        sb.append(indent2);
        sb.append("mMultiparts: ");
        sb.append(multiparts);
        sb.append("\r\n");
        sb.append(indent);
        sb.append("]");
        return sb.toString();
    }

    private String makeIndent(int depth) {
        StringBuffer indent = new StringBuffer();
        for (int i = 0; i < depth * 2; i++) {
            indent.append("    ");
        }
        return indent.toString();
    }

    private int getFieldSize(String field) {
        if (field != null) {
            return field.length();
        }
        return 0;
    }
}
