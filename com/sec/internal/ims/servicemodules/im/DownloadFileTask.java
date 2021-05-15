package com.sec.internal.ims.servicemodules.im;

import android.net.Network;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.HttpAuthGenerator;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.OpenIdAuth;
import com.sec.internal.log.IMSLog;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

public class DownloadFileTask extends AsyncTask<DownloadRequest, Long, Long> {
    private static final long FT_SIZE_MARGIN = 10240;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = DownloadFileTask.class.getSimpleName();
    private static final int MAX_PROGRESS_COUNT = 50;
    /* access modifiers changed from: private */
    public long mDownloadProgressElapsed;
    private HttpRequest mHttpRequest = null;
    protected final IMnoStrategy mMnoStrategy;
    private int mPhoneId;
    /* access modifiers changed from: private */
    public DownloadRequest mRequest;
    /* access modifiers changed from: private */
    public long mTotal;
    /* access modifiers changed from: private */
    public long mTransferred;
    /* access modifiers changed from: private */
    public long mWritten;

    public interface DownloadTaskCallback {
        void onCanceled(CancelReason cancelReason, int i, int i2);

        void onCompleted(long j);

        void onProgressUpdate(long j);
    }

    public DownloadFileTask(int phoneId) {
        String str = LOG_TAG;
        Log.i(str, "phoneId: " + phoneId);
        this.mPhoneId = phoneId;
        this.mMnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    /* access modifiers changed from: protected */
    public Long doInBackground(DownloadRequest... params) {
        boolean isUploaded = false;
        Preconditions.checkNotNull(params[0].mCallback);
        Preconditions.checkNotNull(params[0].mUrl);
        Preconditions.checkNotNull(params[0].mFilePath);
        Preconditions.checkNotNull(params[0].mUserAgent);
        TrafficStats.setThreadStatsTag(Process.myTid());
        IMSLog.s(LOG_TAG, "doInBackground: " + params[0]);
        DownloadRequest downloadRequest = params[0];
        this.mRequest = downloadRequest;
        this.mTransferred = downloadRequest.mTransferredBytes;
        this.mTotal = this.mRequest.mTotalBytes;
        File file = new File(this.mRequest.mFilePath);
        if (file.length() != this.mTransferred) {
            Log.i(LOG_TAG, "Adjust mTransferred to " + file.length());
            long length = file.length();
            this.mTransferred = length;
            if (length >= this.mTotal) {
                Log.i(LOG_TAG, "Already the download was completed.");
                this.mRequest.mCallback.onCompleted(this.mTransferred);
                return Long.valueOf(this.mTransferred);
            }
        }
        this.mWritten = file.length();
        int bufferSize = (int) Math.min(512000, Math.max(this.mRequest.mTotalBytes / 50, 61440));
        BufferedOutputStream output = null;
        try {
            int response = sendGetRequest(bufferSize);
            if (response == 200 || (response == 206 && this.mTransferred > 0)) {
                output = new BufferedOutputStream(new FileOutputStream(this.mRequest.mFilePath, this.mTransferred > 0), bufferSize);
                this.mHttpRequest.progress(new HttpRequest.UploadProgress() {
                    public void onUpload(long uploaded, long total) {
                        String access$000 = DownloadFileTask.LOG_TAG;
                        Log.i(access$000, "onUpload: " + (DownloadFileTask.this.mWritten + uploaded) + " / " + DownloadFileTask.this.mTotal + " (" + (((DownloadFileTask.this.mWritten + uploaded) * 100) / DownloadFileTask.this.mTotal) + "%)");
                        if ((DownloadFileTask.this.mTransferred * 50) / DownloadFileTask.this.mTotal < (50 * uploaded) / DownloadFileTask.this.mTotal) {
                            long currentTime = SystemClock.elapsedRealtime();
                            String access$0002 = DownloadFileTask.LOG_TAG;
                            Log.i(access$0002, "onUpload: currentTime = " + currentTime + ", mDownloadProgressElapsed = " + DownloadFileTask.this.mDownloadProgressElapsed);
                            if (currentTime > DownloadFileTask.this.mDownloadProgressElapsed + 200 || DownloadFileTask.this.mWritten + uploaded > DownloadFileTask.this.mTotal + DownloadFileTask.FT_SIZE_MARGIN) {
                                DownloadFileTask.this.mRequest.mCallback.onProgressUpdate(DownloadFileTask.this.mWritten + uploaded);
                                long unused = DownloadFileTask.this.mDownloadProgressElapsed = currentTime;
                            }
                        }
                        long unused2 = DownloadFileTask.this.mTransferred = uploaded;
                    }

                    public boolean isCancelled() {
                        return DownloadFileTask.this.isCancelled();
                    }
                }).receive(output);
                if (!isCancelled()) {
                    if (this.mHttpRequest.ok()) {
                        Log.i(LOG_TAG, "Download success, handle response message.");
                        this.mRequest.mCallback.onCompleted(this.mTransferred + this.mWritten);
                    } else {
                        Log.e(LOG_TAG, "Download failed, " + this.mHttpRequest.message());
                        if (this.mWritten == this.mTotal || this.mTransferred + this.mWritten == this.mTotal) {
                            isUploaded = true;
                        }
                        if (this.mHttpRequest.code() != 206 || !isUploaded) {
                            cancelRequest(this.mHttpRequest);
                        } else {
                            this.mRequest.mCallback.onCompleted(this.mTransferred + this.mWritten);
                        }
                    }
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.mHttpRequest.disconnect();
                    return Long.valueOf(this.mTransferred);
                }
                throw new DownloadFileTaskException("Download Task Failed. isCancelled() is called.");
            }
            Log.i(LOG_TAG, "Download failed, response: " + response);
            cancelRequest(this.mHttpRequest);
            Long valueOf = Long.valueOf(this.mTransferred);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            this.mHttpRequest.disconnect();
            return valueOf;
        } catch (HttpRequest.HttpRequestException e3) {
            HttpRequest.HttpRequestException e4 = e3;
            if (isPermanentFailCause(e4)) {
                e4.printStackTrace();
                this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 30, -1);
            } else {
                this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
            }
            Log.e(LOG_TAG, e4.getCause() + " happened. Retry download Task.");
            Long valueOf2 = Long.valueOf(this.mTransferred);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            this.mHttpRequest.disconnect();
            return valueOf2;
        } catch (DownloadFileTaskException e6) {
            e6.printStackTrace();
            Long valueOf3 = Long.valueOf(this.mTransferred);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            this.mHttpRequest.disconnect();
            return valueOf3;
        } catch (FileNotFoundException e8) {
            e8.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e9) {
                    e9.printStackTrace();
                }
            }
            this.mHttpRequest.disconnect();
            return 0L;
        } catch (Throwable th) {
            Throwable th2 = th;
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e10) {
                    e10.printStackTrace();
                }
            }
            this.mHttpRequest.disconnect();
            throw th2;
        }
    }

    private boolean isPermanentFailCause(HttpRequest.HttpRequestException e) {
        return e.getCause() instanceof UnknownHostException;
    }

    private void cancelRequest(HttpRequest httpReq) {
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        IMnoStrategy.HttpStrategyResponse response = iMnoStrategy != null ? iMnoStrategy.handleFtHttpDownloadError(httpReq) : new IMnoStrategy.HttpStrategyResponse(CancelReason.ERROR, 3);
        this.mRequest.mCallback.onCanceled(response.getCancelReason(), response.getDelay(), httpReq.code());
    }

    public static class DownloadRequest {
        public DownloadTaskCallback mCallback;
        public String mFilePath;
        public Network mNetwork;
        public String mPassword;
        public long mTotalBytes;
        public long mTransferredBytes;
        public boolean mTrustAllCerts;
        public String mUrl;
        public String mUser;
        public String mUserAgent;

        public DownloadRequest(String url, long totalBytes, long transferredBytes, String filePath, String user, String password, String ua, Network network, boolean trustAllCerts, String dlUrl, Map<String, String> params, DownloadTaskCallback callback) {
            this.mTotalBytes = totalBytes;
            this.mTransferredBytes = transferredBytes;
            this.mFilePath = filePath;
            this.mUserAgent = ua;
            this.mCallback = callback;
            this.mUser = user;
            this.mPassword = password;
            this.mNetwork = network;
            this.mTrustAllCerts = trustAllCerts;
            this.mUrl = !TextUtils.isEmpty(dlUrl) ? dlUrl : url;
            if (params != null && params.size() > 0) {
                this.mUrl = FileTaskUtil.createRequestUrl(this.mUrl, params);
            }
        }

        public String toString() {
            return "DownloadRequest{mUrl=" + IMSLog.checker(this.mUrl) + ", mTotalBytes=" + this.mTotalBytes + ", mTransferredBytes=" + this.mTransferredBytes + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mUserAgent=" + this.mUserAgent + ", mCallback=" + this.mCallback + ", mUser=" + IMSLog.checker(this.mUser) + ", mPassword=" + IMSLog.checker(this.mPassword) + ", mNetwork=" + this.mNetwork + ", mTrustAllCerts=" + this.mTrustAllCerts + "}";
        }
    }

    public static class DownloadFileTaskException extends Exception {
        public DownloadFileTaskException(String message) {
            super(message);
        }
    }

    private int sendGetRequest(int bufferSize) {
        String akaResponse;
        String openIdResponseURL;
        this.mHttpRequest = null;
        try {
            int response = sendEmptyGetRequest(bufferSize);
            if (response == 200) {
                Log.i(LOG_TAG, "Receive 200 OK");
                if (!this.mHttpRequest.header("Content-Type").contains("application/vnd.gsma.eap-relay.v1.0+json")) {
                    return response;
                }
                this.mHttpRequest.disconnect();
                String body = this.mHttpRequest.body();
                if (body == null || (akaResponse = HttpAuthGenerator.getEAPAkaChallengeResponse(this.mPhoneId, body)) == null) {
                    String str = LOG_TAG;
                    Log.e(str, "EAP AKA authentication failed, code: " + this.mHttpRequest.code());
                    this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
                    return -1;
                }
                this.mHttpRequest = HttpRequest.post(this.mRequest.mUrl);
                setDefaultHeaders(bufferSize);
                this.mHttpRequest.contentType("application/vnd.gsma.eap-relay.v1.0+json");
                this.mHttpRequest.send((CharSequence) akaResponse);
                return this.mHttpRequest.code();
            } else if (response == 206) {
                Log.i(LOG_TAG, "Receive 206 Partial");
                return response;
            } else if (response == 302) {
                String location = this.mHttpRequest.header("Location");
                if (TextUtils.isEmpty(location) || (openIdResponseURL = OpenIdAuth.sendAuthRequest(new OpenIdAuth.OpenIdRequest(this.mPhoneId, location, this.mRequest.mNetwork, this.mRequest.mUserAgent, this.mRequest.mTrustAllCerts))) == null) {
                    Log.e(LOG_TAG, "sendGetRequest: OpenId Process failed!");
                    this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
                    return -1;
                }
                this.mHttpRequest.disconnect();
                this.mRequest.mUrl = openIdResponseURL;
                return sendEmptyGetRequest(bufferSize);
            } else if (response != 401) {
                String str2 = LOG_TAG;
                IMSLog.e(str2, "Receive HTTP response " + this.mHttpRequest.message() + " neither OK nor UNAUTHORIZED");
                cancelRequest(this.mHttpRequest);
                return response;
            } else {
                Log.i(LOG_TAG, "Receive 401 Unauthorized, attempt to generate response");
                this.mHttpRequest.disconnect();
                String challenge = this.mHttpRequest.wwwAuthenticate();
                String str3 = LOG_TAG;
                IMSLog.s(str3, "challenge: " + challenge);
                String authResponse = HttpAuthGenerator.getAuthorizationHeader(this.mPhoneId, this.mRequest.mUrl, challenge, "GET", this.mHttpRequest.getCipherSuite());
                this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl);
                setDefaultHeaders(bufferSize);
                this.mHttpRequest.authorization(authResponse);
                return this.mHttpRequest.code();
            }
        } catch (HttpRequest.HttpRequestException e) {
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            if (isPermanentFailCause(e)) {
                this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 30, -1);
            } else {
                this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
            }
            String str4 = LOG_TAG;
            Log.e(str4, e.getCause() + " happened. Retry download Task.");
            return -1;
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.ERROR, -1, -1);
            return -1;
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.DEVICE_UNREGISTERED, -1, -1);
            return -1;
        } catch (RuntimeException e4) {
            e4.printStackTrace();
            this.mRequest.mCallback.onCanceled(CancelReason.ERROR, 3, -1);
            return -1;
        }
    }

    private int sendEmptyGetRequest(int bufferSize) {
        this.mHttpRequest = HttpRequest.get(this.mRequest.mUrl);
        setDefaultHeaders(bufferSize);
        return this.mHttpRequest.code();
    }

    private void setDefaultHeaders(int bufferSize) {
        this.mHttpRequest.setParams(this.mRequest.mNetwork, false, 10000, FileTaskUtil.READ_DATA_TIMEOUT, this.mRequest.mUserAgent).bufferSize(bufferSize);
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            this.mHttpRequest.header("Accept", "application/vnd.gsma.eap-relay.v1.0+json");
        }
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA) && this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_USERIDENTITY_FOR_FTHTTP)) {
            String impu = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId).getImpuFromIsim(0);
            if (TextUtils.isEmpty(impu)) {
                impu = ImsUtil.getPublicId(this.mPhoneId);
            }
            HttpRequest httpRequest = this.mHttpRequest;
            httpRequest.header("X-3GPP-Intended-Identity", "\"" + impu + "\"");
        }
        long j = this.mTransferred;
        if (j > 0) {
            this.mHttpRequest.range(j, -1);
        }
        if (this.mRequest.mTrustAllCerts) {
            this.mHttpRequest.trustAllCerts().trustAllHosts();
        }
    }
}
