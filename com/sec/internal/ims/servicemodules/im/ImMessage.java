package com.sec.internal.ims.servicemodules.im;

import android.net.Network;
import android.os.AsyncTask;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.im.DownloadFileTask;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

public class ImMessage extends MessageBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImMessage.class.getSimpleName();
    private final Set<ImsUri> mGroupCcListUri = new HashSet();
    /* access modifiers changed from: private */
    public final ImMessageListener mListener;

    protected ImMessage(Builder<?> builder) {
        super(builder);
        this.mListener = builder.mListener;
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public String getServiceTag() {
        return "IM";
    }

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageDone() : mid = " + this.mId + ", mStatus = " + this.mStatus + ", mBody = " + IMSLog.checker(this.mBody));
        if (result.getImError() != ImError.SUCCESS) {
            updateStatus(ImConstants.Status.FAILED);
            this.mListener.onMessageSendingFailed(this, strategyResponse, result);
        } else if (this.mStatus != ImConstants.Status.SENT) {
            setSentTimestamp(System.currentTimeMillis());
            updateStatus(ImConstants.Status.SENT);
            this.mListener.onMessageSendingSucceeded(this);
        }
    }

    public void onReceived() {
        if (this.mReliableMessage != null) {
            requestReliableImageDownload();
        } else {
            this.mListener.onMessageReceived(this);
        }
    }

    public void onSendMessageResponseTimeout() {
        this.mListener.onMessageSendResponseTimeout(this);
    }

    public Set<ImsUri> getGroupCcListUri() {
        return this.mGroupCcListUri;
    }

    public void setGroupCcListUri(Collection<ImsUri> ccList) {
        this.mGroupCcListUri.addAll(ccList);
    }

    private void requestReliableImageDownload() {
        String dir = getContext().getFilesDir().toString() + File.separatorChar + "rcsreliable_d";
        String url = getReliableMessage();
        String fileName = url.substring(url.lastIndexOf(47) + 1);
        final String path = dir + File.separatorChar + fileName;
        if (new File(path).exists()) {
            Log.e(LOG_TAG, "requestReliableImageDownload, file already exist.");
            setReliableMessage(path);
            this.mListener.onMessageReceived(this);
            return;
        }
        File folder = new File(dir);
        if (!folder.exists() && folder.mkdirs()) {
            Log.i(LOG_TAG, "requestReliableImageDownload, mkdirs");
        }
        Log.i(LOG_TAG, "requestReliableImageDownload, url: " + IMSLog.checker(url) + " path: " + path);
        DownloadFileTask downloadTask = new DownloadFileTask(this.mConfig.getPhoneId());
        Executor executor = AsyncTask.SERIAL_EXECUTOR;
        String ftHttpCsUser = this.mConfig.getFtHttpCsUser();
        String ftHttpCsPwd = this.mConfig.getFtHttpCsPwd();
        String userAgent = this.mConfig.getUserAgent();
        Network network = this.mNetwork;
        boolean isFtHttpTrustAllCerts = this.mConfig.isFtHttpTrustAllCerts();
        AnonymousClass1 r26 = new DownloadFileTask.DownloadTaskCallback() {
            public void onProgressUpdate(long transferred) {
            }

            public void onCompleted(long transferred) {
                Log.i(ImMessage.LOG_TAG, "requestReliableImageDownload, onCompleted");
                this.setReliableMessage(path);
                ImMessage.this.mListener.onMessageReceived(this);
            }

            public void onCanceled(CancelReason reason, int retryTime, int errorCode) {
                Log.i(ImMessage.LOG_TAG, "requestReliableImageDownload, onCanceled");
                ImMessage.this.mListener.onMessageReceived(this);
            }
        };
        DownloadFileTask downloadTask2 = downloadTask;
        String str = userAgent;
        String str2 = path;
        String str3 = fileName;
        boolean z = isFtHttpTrustAllCerts;
        String str4 = url;
        DownloadFileTask.DownloadRequest[] downloadRequestArr = new DownloadFileTask.DownloadRequest[1];
        downloadRequestArr[0] = new DownloadFileTask.DownloadRequest(url, 1, 0, path, ftHttpCsUser, ftHttpCsPwd, str, network, z, (String) null, (Map<String, String>) null, r26);
        downloadTask2.executeOnExecutor(executor, downloadRequestArr);
    }

    public void setSlmSvcMsg(boolean isSlmSvcMsg) {
        this.mIsSlmSvcMsg = isSlmSvcMsg;
        if (isSlmSvcMsg) {
            setMessagingTech(getBody().length() > this.mConfig.getPagerModeLimit() ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE);
        } else {
            setMessagingTech(ImConstants.MessagingTech.NORMAL);
        }
    }

    public String toString() {
        return "ImMessage [" + super.toString() + "]";
    }

    public static abstract class Builder<T extends Builder<T>> extends MessageBase.Builder<T> {
        /* access modifiers changed from: private */
        public ImMessageListener mListener;

        public T listener(ImMessageListener listener) {
            this.mListener = listener;
            return (Builder) self();
        }

        public ImMessage build() {
            Preconditions.checkNotNull(this.mListener);
            return new ImMessage(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        private Builder2() {
        }

        /* access modifiers changed from: protected */
        public Builder2 self() {
            return this;
        }
    }
}
