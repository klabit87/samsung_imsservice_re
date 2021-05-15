package com.sec.internal.ims.servicemodules.im;

import android.net.Network;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.motricity.verizon.ssoengine.SSOContentProviderConstants;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.HttpAuthGenerator;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.OpenIdAuth;
import com.sec.internal.ims.util.ThumbnailUtil;
import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class UploadFileTask extends AsyncTask<UploadRequest, Long, Long> {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = UploadFileTask.class.getSimpleName();
    protected String mContentType;
    protected String mEncodedFileName;
    protected File mFile;
    protected HttpRequest mHttpRequest;
    protected final IMnoStrategy mMnoStrategy;
    protected int mPhoneId;
    protected UploadRequest mRequest;
    protected long mTotal;
    protected long mTransferred;
    protected long mUploadProgressElapsed;
    protected String thumbFileName;
    protected byte[] thumbnailData;
    protected String thumbnailType;

    public interface UploadTaskCallback {
        void onCanceled(CancelReason cancelReason, int i, int i2, boolean z);

        void onCompleted(String str);

        void onFinished();

        void onProgressUpdate(long j);

        void onStarted();
    }

    public UploadFileTask(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + phoneId);
        this.mPhoneId = phoneId;
        this.mMnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
    }

    /* access modifiers changed from: protected */
    public void onCancelled(Long result) {
        UploadRequest uploadRequest = this.mRequest;
        if (!(uploadRequest == null || uploadRequest.mCallback == null)) {
            this.mRequest.mCallback.onFinished();
        }
        String str = LOG_TAG;
        Log.i(str, "Task cancelled. " + result);
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Long result) {
        UploadRequest uploadRequest = this.mRequest;
        if (!(uploadRequest == null || uploadRequest.mCallback == null)) {
            this.mRequest.mCallback.onFinished();
        }
        String str = LOG_TAG;
        Log.i(str, "Task finished. " + result);
    }

    /* access modifiers changed from: protected */
    public Long doInBackground(UploadRequest... params) {
        if (this.mMnoStrategy == null) {
            Log.e(LOG_TAG, "mMnoStrategy is null");
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return Long.valueOf(this.mTransferred);
        }
        Preconditions.checkNotNull(params[0].mCallback);
        Preconditions.checkNotNull(params[0].mUrl);
        Preconditions.checkNotNull(params[0].mTid);
        Preconditions.checkNotNull(params[0].mFilePath);
        TrafficStats.setThreadStatsTag(Process.myTid());
        String str = LOG_TAG;
        IMSLog.i(str, "doInBackground: " + params[0]);
        UploadRequest uploadRequest = params[0];
        this.mRequest = uploadRequest;
        this.mTotal = uploadRequest.mTotalBytes;
        this.mHttpRequest = null;
        if (!sendFirstPostRequest()) {
            return Long.valueOf(this.mTransferred);
        }
        if (this.mHttpRequest == null) {
            Log.e(LOG_TAG, "mHttpRequest is null");
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return Long.valueOf(this.mTransferred);
        }
        int bufferSize = (int) Math.min(512000, Math.max(this.mRequest.mTotalBytes / 50, 61440));
        try {
            setDefaultHeaders();
            this.mHttpRequest.bufferSize(bufferSize);
            generateFileInfo();
            if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_FTHTTP_CONTENTLENGTH)) {
                long contentLength = getRequestContentLength(this.mHttpRequest, this.thumbnailData, this.thumbnailType, this.thumbFileName, this.mContentType, this.mEncodedFileName);
                String str2 = LOG_TAG;
                Log.i(str2, "Http request length:" + contentLength);
                this.mHttpRequest.contentLength(Long.toString(contentLength));
            } else {
                this.mHttpRequest.chunk(0);
            }
            this.mHttpRequest.part(SSOContentProviderConstants.ResultFields.TID, (String) null, "text/plain", this.mRequest.mTid);
            if (this.thumbnailData != null) {
                this.mHttpRequest.part("Thumbnail", this.thumbFileName, this.thumbnailType, (InputStream) new ByteArrayInputStream(this.thumbnailData));
            }
            this.mRequest.mCallback.onStarted();
            this.mHttpRequest.progress(new HttpRequest.UploadProgress() {
                public void onUpload(long uploaded, long total) {
                    String access$000 = UploadFileTask.LOG_TAG;
                    Log.i(access$000, "onUpload: " + uploaded + " / " + UploadFileTask.this.mTotal + " (" + ((100 * uploaded) / UploadFileTask.this.mTotal) + "%)");
                    if ((UploadFileTask.this.mTransferred * 50) / UploadFileTask.this.mTotal < (50 * uploaded) / UploadFileTask.this.mTotal) {
                        long currentTime = SystemClock.elapsedRealtime();
                        String access$0002 = UploadFileTask.LOG_TAG;
                        Log.i(access$0002, "onUpload: currentTime = " + currentTime + ", mUploadProgressElapsed = " + UploadFileTask.this.mUploadProgressElapsed);
                        if (currentTime > UploadFileTask.this.mUploadProgressElapsed + 200) {
                            UploadFileTask.this.mRequest.mCallback.onProgressUpdate(uploaded);
                            UploadFileTask.this.mUploadProgressElapsed = currentTime;
                        }
                    }
                    UploadFileTask.this.mTransferred = uploaded;
                }

                public boolean isCancelled() {
                    return UploadFileTask.this.isCancelled();
                }
            }).part("File", this.mEncodedFileName, this.mContentType, this.mFile).progress((HttpRequest.UploadProgress) null);
            if (isCancelled()) {
                Long valueOf = Long.valueOf(this.mTransferred);
                this.mHttpRequest.disconnect();
                return valueOf;
            }
            onUploadFileDone();
            this.mHttpRequest.disconnect();
            return Long.valueOf(this.mTransferred);
        } catch (HttpRequest.HttpRequestException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (isPermanentFailCause(e)) {
                cancelRequest(CancelReason.ERROR, 30, -1, false);
            } else {
                String str3 = LOG_TAG;
                Log.e(str3, e.getCause() + " happened. Retry Upload Task.");
                cancelRequest(CancelReason.ERROR, 3, -1, false);
            }
        } catch (IllegalStateException e2) {
            if (e2.getCause() != null) {
                e2.getCause().printStackTrace();
            }
            cancelRequest(CancelReason.ERROR, 3, -1, false);
        } catch (Throwable th) {
            this.mHttpRequest.disconnect();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPermanentFailCause(HttpRequest.HttpRequestException e) {
        return e.getCause() instanceof UnknownHostException;
    }

    /* access modifiers changed from: protected */
    public void cancelRequest(HttpRequest httpReq, boolean fullUploadNeeded) {
        int delay;
        CancelReason reason = CancelReason.ERROR;
        int code = httpReq.code();
        if (code != 403) {
            delay = code != 410 ? code != 500 ? code != 503 ? 3 : RetryTimerUtil.getRetryAfter(httpReq.header(HttpRequest.HEADER_RETRY_AFTER)) : 5 : 1;
        } else {
            delay = 3;
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy != null) {
                iMnoStrategy.handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.OUTGOING, false);
            }
        }
        cancelRequest(reason, delay, httpReq.code(), fullUploadNeeded);
    }

    /* access modifiers changed from: protected */
    public void cancelRequest(CancelReason reason, int retryTime, int errorCode, boolean fullUploadNeeded) {
        if (!isCancelled()) {
            this.mRequest.mCallback.onCanceled(reason, retryTime, errorCode, fullUploadNeeded);
        }
    }

    private long getRequestContentLength(HttpRequest httpReq, byte[] thumbnailData2, String thumbnailType2, String thumbFileName2, String contentType, String encodedFileName) {
        HttpRequest httpRequest = httpReq;
        byte[] bArr = thumbnailData2;
        String str = encodedFileName;
        File file = new File(this.mRequest.mFilePath);
        long contentLength = 0 + httpRequest.getPartHeaderLength(SSOContentProviderConstants.ResultFields.TID, (String) null, "text/plain", true) + ((long) this.mRequest.mTid.length());
        if (bArr != null) {
            contentLength += httpRequest.getPartHeaderLength("Thumbnail", thumbFileName2, thumbnailType2, false) + ((long) bArr.length);
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy != null && iMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CONTENTLENGTH_IN_BYTE)) {
                contentLength += (long) (str.getBytes(Charset.defaultCharset()).length - encodedFileName.length());
            }
        } else {
            String str2 = thumbnailType2;
            String str3 = thumbFileName2;
        }
        long contentLength2 = contentLength + httpRequest.getPartHeaderLength("File", str, contentType, false) + file.length();
        IMnoStrategy iMnoStrategy2 = this.mMnoStrategy;
        if (iMnoStrategy2 != null && iMnoStrategy2.boolSetting(RcsPolicySettings.RcsPolicy.CONTENTLENGTH_IN_BYTE)) {
            contentLength2 += (long) (encodedFileName.getBytes().length - encodedFileName.length());
        }
        return contentLength2 + ((long) ("\r\n" + "--" + HttpRequest.BOUNDARY + "--" + "\r\n").length());
    }

    private String getTrimmedFileName(String fileName, int limitSize) {
        Log.i(LOG_TAG, "getTrimmedFileName() fileName=" + IMSLog.checker(fileName) + ", limitSize= " + limitSize);
        try {
            if (TextUtils.isEmpty(fileName)) {
                return null;
            }
            int encodedNameLength = URLEncoder.encode(fileName, "UTF-8").getBytes().length;
            int extOffset = fileName.lastIndexOf(".");
            if (extOffset == -1) {
                extOffset = fileName.length();
            }
            String name = fileName.substring(0, extOffset);
            String ext = fileName.substring(extOffset);
            int totalTrimmedSize = 0;
            int idx = name.length();
            while (idx > 0 && encodedNameLength - totalTrimmedSize > limitSize) {
                totalTrimmedSize += URLEncoder.encode(name.substring(idx - 1, idx), "UTF-8").getBytes().length;
                idx--;
            }
            String name2 = name.substring(0, idx);
            Log.i(LOG_TAG, "Trimmed fileName=" + IMSLog.checker(name2) + ext);
            return name2 + ext;
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(LOG_TAG, "Exception: " + e);
            return fileName;
        }
    }

    private boolean sendFirstPostRequest() {
        try {
            sendEmptyPostRequest();
            setDefaultHeaders();
            int code = this.mHttpRequest.code();
            this.mHttpRequest.disconnect();
            if (!handleFirstRequestResponse(code)) {
                return false;
            }
            return true;
        } catch (HttpRequest.HttpRequestException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (isPermanentFailCause(e)) {
                cancelRequest(CancelReason.ERROR, 30, -1, false);
            } else {
                String str = LOG_TAG;
                Log.e(str, e.getCause() + " happened. Retry Upload Task.");
                cancelRequest(CancelReason.ERROR, 3, -1, false);
            }
            return false;
        } catch (Exception e2) {
            e2.printStackTrace();
            cancelRequest(CancelReason.ERROR, -1, -1, false);
            return false;
        }
    }

    private void sendEmptyPostRequest() {
        String emptyPostUrl = this.mRequest.mUrl;
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        if (iMnoStrategy != null && iMnoStrategy.isHTTPUsedForEmptyFtHttpPOST()) {
            emptyPostUrl = emptyPostUrl.replaceFirst("https://", "http://");
        }
        this.mHttpRequest = HttpRequest.post(emptyPostUrl);
    }

    private boolean handleFirstRequestResponse(int code) {
        if (code == 200 || code == 204) {
            String location = LOG_TAG;
            Log.i(location, "Receive: " + code);
            this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl);
            return true;
        } else if (code == 302) {
            String location2 = this.mHttpRequest.header("Location");
            if (!TextUtils.isEmpty(location2)) {
                String openIdResponseURL = OpenIdAuth.sendAuthRequest(new OpenIdAuth.OpenIdRequest(this.mPhoneId, location2, this.mRequest.mNetwork, this.mRequest.mUserAgent, this.mRequest.mTrustAllCerts));
                if (openIdResponseURL != null) {
                    this.mHttpRequest.disconnect();
                    this.mRequest.mUrl = openIdResponseURL;
                    this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl);
                    return true;
                }
            }
            Log.e(LOG_TAG, "handleFirstRequestResponse: OpenId process failed!");
            cancelRequest(this.mHttpRequest, false);
            return false;
        } else if (code != 401) {
            String str = LOG_TAG;
            Log.e(str, "Receive " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
            cancelRequest(this.mHttpRequest, false);
            return false;
        } else {
            String response = getAuthorizationHeader(this.mPhoneId, this.mHttpRequest, this.mRequest.mUrl, "POST");
            if (TextUtils.isEmpty(response)) {
                Log.e(LOG_TAG, "handleFirstRequestResponse: Authorization response is null!");
                cancelRequest(this.mHttpRequest, false);
            }
            this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl).useNetwork(this.mRequest.mNetwork).authorization(response);
            return true;
        }
    }

    private void generateFileInfo() {
        this.mFile = new File(this.mRequest.mFilePath);
        if (this.mRequest.mContentType != null) {
            this.mContentType = this.mRequest.mContentType;
        } else {
            this.mContentType = ImCache.getContentType(this.mFile);
        }
        this.thumbFileName = "";
        this.thumbnailType = "image/jpeg";
        this.thumbnailData = null;
        if (this.mRequest.bFileIcon) {
            generateThumbnailData(this.mRequest.mFilePath, this.mContentType);
        }
        this.mEncodedFileName = this.mFile.getName();
        try {
            if (!this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_ENCODING_FILENAME_BY_SERVER)) {
                int fnLengthlimit = this.mMnoStrategy.intSetting(RcsPolicySettings.RcsPolicy.FILE_NAME_LENGTH_LIMIT_IN_SERVER);
                String fileName = this.mFile.getName();
                if (fnLengthlimit > 0) {
                    if (URLEncoder.encode(fileName, "UTF-8").getBytes().length > fnLengthlimit) {
                        fileName = getTrimmedFileName(fileName, fnLengthlimit);
                    }
                }
                this.mEncodedFileName = URLEncoder.encode(fileName, "UTF-8");
            }
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            Log.e(LOG_TAG, "UnsupportedEncodingException or IllegalArgumentException");
        }
        if (TextUtils.isEmpty(this.thumbFileName)) {
            IMSLog.s(LOG_TAG, "mEncodedFileName : " + this.mEncodedFileName);
            String[] fileName2 = this.mEncodedFileName.split("\\.");
            if (fileName2.length > 0) {
                this.thumbFileName = fileName2[0];
            } else {
                this.thumbFileName = "thumb";
            }
            if ("image/jpeg".equals(this.thumbnailType)) {
                this.thumbFileName += ".jpg";
            } else if ("image/bmp".equals(this.thumbnailType)) {
                this.thumbFileName += ".bmp";
            }
        }
    }

    private void generateThumbnailData(String filePath, String contentType) {
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_HIGHRESOLUTIONVIDEO_THUMBNAIL) && contentType != null && contentType.startsWith(TMOConstants.CallLogTypes.VIDEO)) {
            this.thumbnailData = ThumbnailUtil.getHighResolutionVideoThumbnailByteArray(filePath, contentType);
        }
        if (this.thumbnailData == null) {
            this.thumbnailData = ThumbnailUtil.getThumbnailByteArray(filePath, contentType);
        }
        if (this.thumbnailData == null) {
            String csc = OmcCode.get();
            if ("DTM".equals(csc) || "DTR".equals(csc)) {
                this.thumbnailData = new byte[]{66, 77, 66, 0, 0, 0, 0, 0, 0, 0, 62, 0, 0, 0, 40, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, -64, 0, 0, 0};
                this.thumbnailType = "image/bmp";
                this.thumbFileName = "dummy.txt.txt.bmp";
            }
        }
    }

    private void onUploadFileDone() {
        Log.i(LOG_TAG, "Upload File done. Read http response.");
        if (this.mHttpRequest.ok()) {
            Log.i(LOG_TAG, "Upload success, handle response message.");
            this.mRequest.mCallback.onCompleted(this.mHttpRequest.body());
            return;
        }
        String str = LOG_TAG;
        Log.e(str, "Upload failed, " + this.mHttpRequest.code() + " " + this.mHttpRequest.message());
        if (this.mHttpRequest.code() == 500) {
            Log.e(LOG_TAG, "Retry uploading with deaccented mFile name.");
            cancelRequest(CancelReason.ERROR, 3, 500, false);
            return;
        }
        cancelRequest(this.mHttpRequest, false);
    }

    /* access modifiers changed from: protected */
    public String getAuthorizationHeader(int phoneId, HttpRequest httpRequest, String requestUrl, String method) {
        Log.i(LOG_TAG, "Receive 401 Unauthorized, attempt to generate response");
        String challenge = httpRequest.wwwAuthenticate();
        String str = LOG_TAG;
        IMSLog.s(str, "challenge: " + challenge);
        if (challenge == null) {
            Log.i(LOG_TAG, "Got 401 and challenge is NULL!");
            return "";
        } else if (challenge.trim().equals("SIT")) {
            Log.i(LOG_TAG, "Got 401 for SIT. Skip GBA");
            return "";
        } else {
            String response = HttpAuthGenerator.getAuthorizationHeader(phoneId, requestUrl, challenge, method, httpRequest.getCipherSuite());
            String str2 = LOG_TAG;
            IMSLog.s(str2, "response: " + response);
            return response;
        }
    }

    /* access modifiers changed from: protected */
    public void setDefaultHeaders() {
        this.mHttpRequest.useNetwork(this.mRequest.mNetwork).useCaches(false).connectTimeout(10000).readTimeout(FileTaskUtil.READ_DATA_TIMEOUT).userAgent(this.mRequest.mUserAgent);
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA) && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_USERIDENTITY_FOR_FTHTTP)) {
            String impu = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId).getImpuFromIsim(0);
            if (TextUtils.isEmpty(impu)) {
                impu = ImsUtil.getPublicId(this.mPhoneId);
            }
            HttpRequest httpRequest = this.mHttpRequest;
            httpRequest.header("X-3GPP-Intended-Identity", "\"" + impu + "\"");
        }
        if (this.mRequest.mTrustAllCerts) {
            this.mHttpRequest.trustAllCerts().trustAllHosts();
        }
    }

    public static class UploadRequest {
        public boolean bFileIcon;
        public UploadTaskCallback mCallback;
        public String mContentType;
        public String mFilePath;
        public Network mNetwork;
        public String mPassword;
        public String mTid;
        public long mTotalBytes;
        public boolean mTrustAllCerts;
        public String mUrl;
        public String mUser;
        public String mUserAgent;

        public UploadRequest(String url, long totalBytes, String filePath, boolean useFileIcon, String tid, String user, String password, String ua, Network network, boolean trustAllCert, UploadTaskCallback callback, String contentType) {
            this.mUrl = url;
            this.mTotalBytes = totalBytes;
            this.mFilePath = filePath;
            this.mTid = tid;
            this.mUser = user;
            this.mPassword = password;
            this.mUserAgent = ua;
            this.mCallback = callback;
            this.bFileIcon = useFileIcon;
            this.mNetwork = network;
            this.mTrustAllCerts = trustAllCert;
            this.mContentType = contentType;
        }

        public String toString() {
            return "UploadRequest{mUrl=" + IMSLog.checker(this.mUrl) + ", mTotalBytes=" + this.mTotalBytes + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", bFileIcon=" + this.bFileIcon + ", mTid=" + this.mTid + ", mUser=" + IMSLog.checker(this.mUser) + ", mPassword=" + IMSLog.checker(this.mPassword) + ", mUserAgent=" + this.mUserAgent + ", mNetwork=" + this.mNetwork + ", mTrustAllCerts=" + this.mTrustAllCerts + ", mCallback=" + this.mCallback + ", mContentType=" + this.mContentType + "}";
        }
    }
}
