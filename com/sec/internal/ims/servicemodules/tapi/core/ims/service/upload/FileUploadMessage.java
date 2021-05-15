package com.sec.internal.ims.servicemodules.tapi.core.ims.service.upload;

import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.gsma.services.rcs.upload.FileUpload;
import com.gsma.services.rcs.upload.FileUploadInfo;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.UploadFileTask;
import com.sec.internal.ims.servicemodules.im.UploadResumeFileTask;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.servicemodules.im.util.TidGenerator;
import java.io.IOException;
import java.text.ParseException;
import org.xmlpull.v1.XmlPullParserException;

public class FileUploadMessage {
    private static final int EVENT_CANCEL_UPLOAD = 2;
    private static final int EVENT_RETRY_UPLOAD = 7;
    private static final int EVENT_UPLOAD_CANCELED = 6;
    private static final int EVENT_UPLOAD_COMPLETED = 5;
    private static final int EVENT_UPLOAD_FILE = 1;
    private static final int EVENT_UPLOAD_PROGRESS = 3;
    private static final int EVENT_UPLOAD_STARTED = 4;
    private static final String LOG_TAG = FileUploadMessage.class.getSimpleName();
    private static final int MAX_RETRY_COUNT = 3;
    private boolean bFileIconRequired;
    /* access modifiers changed from: private */
    public boolean bRetryEvent = false;
    private UploadFileTask.UploadTaskCallback mCallback = new UploadFileTask.UploadTaskCallback() {
        public void onStarted() {
            FileUploadMessage.this.mHandler.sendMessage(FileUploadMessage.this.mHandler.obtainMessage(4));
        }

        public void onProgressUpdate(long transferred) {
            FileUploadMessage.this.mHandler.sendMessage(FileUploadMessage.this.mHandler.obtainMessage(3, Long.valueOf(transferred)));
        }

        public void onCompleted(String result) {
            FileUploadMessage.this.mHandler.sendMessage(FileUploadMessage.this.mHandler.obtainMessage(5, result));
        }

        public void onCanceled(CancelReason reason, int retryTime, int errorCode, boolean fullUploadNeeded) {
            if (fullUploadNeeded) {
                long unused = FileUploadMessage.this.mUploadBytes = 0;
            }
            FileUploadMessage.this.mHandler.sendMessage(FileUploadMessage.this.mHandler.obtainMessage(6, retryTime, errorCode));
        }

        public void onFinished() {
            if (!FileUploadMessage.this.isUploadCompleted() && !FileUploadMessage.this.bRetryEvent) {
                FileUploadMessage.this.mHandler.sendMessage(FileUploadMessage.this.mHandler.obtainMessage(6, -1, 0));
            }
        }
    };
    private String mFilePath;
    private long mFileSize;
    private String mFileUploadId;
    private FileUploadInfo mFileUploadInfo = null;
    private Uri mFileUri;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final ImConfig mImConfig;
    private IFileUploadTaskListener mListener;
    private final int mPhoneId;
    /* access modifiers changed from: private */
    public int mRetryCnt = 0;
    private FileUpload.State mState = FileUpload.State.INITIATING;
    private final TidGenerator mTidGenerator = new TidGenerator();
    /* access modifiers changed from: private */
    public long mUploadBytes = 0;
    private UploadFileTask mUploadTask = null;

    static /* synthetic */ int access$508(FileUploadMessage x0) {
        int i = x0.mRetryCnt;
        x0.mRetryCnt = i + 1;
        return i;
    }

    public FileUploadMessage(int phoneId, ImConfig imConfig, Looper looper, Uri fileUri, String filePath, String fileName, long fileSize, boolean useFileIcon) {
        this.mPhoneId = phoneId;
        this.mImConfig = imConfig;
        this.mFileUploadId = this.mTidGenerator.generate().toString();
        this.mFileUri = fileUri;
        this.mFilePath = filePath;
        this.mFileSize = fileSize;
        this.bFileIconRequired = useFileIcon;
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!FileUploadMessage.this.isUploadCompleted()) {
                    switch (msg.what) {
                        case 1:
                            FileUploadMessage.this.tryUpload();
                            return;
                        case 2:
                            FileUploadMessage.this.tryAbort();
                            return;
                        case 3:
                            FileUploadMessage.this.handleProgressUpadate(((Long) msg.obj).longValue());
                            return;
                        case 4:
                            if (!FileUploadMessage.this.isUploadStated()) {
                                FileUploadMessage.this.handleTransferStarted();
                                return;
                            }
                            return;
                        case 5:
                            String result = (String) msg.obj;
                            if (result != null) {
                                FileUploadMessage.this.handleTransferCompleted(result);
                                return;
                            }
                            return;
                        case 6:
                            removeMessages(6);
                            FileUploadMessage.this.handleTransferCanceled(msg.arg1, msg.arg2);
                            return;
                        case 7:
                            boolean unused = FileUploadMessage.this.bRetryEvent = false;
                            if (FileUploadMessage.this.mRetryCnt < 3) {
                                FileUploadMessage.access$508(FileUploadMessage.this);
                                FileUploadMessage.this.tryUpload();
                                return;
                            }
                            FileUploadMessage.this.handleTransferCanceled(-1, 0);
                            return;
                        default:
                            return;
                    }
                }
            }
        };
    }

    public void addListener(IFileUploadTaskListener listener) {
        this.mListener = listener;
    }

    public void startUploadTask() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(1));
    }

    public void abortUploadTask() {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(2));
    }

    /* access modifiers changed from: private */
    public void tryUpload() {
        UploadFileTask uploadFileTask = this.mUploadTask;
        if (uploadFileTask == null || uploadFileTask.getStatus() == AsyncTask.Status.FINISHED) {
            UploadFileTask.UploadRequest uploadRequest = new UploadFileTask.UploadRequest(this.mImConfig.getFtHttpCsUri().toString(), this.mFileSize, this.mFilePath, this.bFileIconRequired, this.mFileUploadId, this.mImConfig.getFtHttpCsUser(), this.mImConfig.getFtHttpCsPwd(), RcsPolicyManager.getRcsStrategy(this.mPhoneId).getFtHttpUserAgent(this.mImConfig), (Network) null, this.mImConfig.isFtHttpTrustAllCerts(), this.mCallback, (String) null);
            if (this.mUploadBytes > 0) {
                this.mUploadTask = new UploadResumeFileTask(this.mImConfig.getPhoneId());
            } else {
                this.mUploadTask = new UploadFileTask(this.mImConfig.getPhoneId());
            }
            this.mUploadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new UploadFileTask.UploadRequest[]{uploadRequest});
            FileUpload.State state = FileUpload.State.INITIATING;
            this.mState = state;
            IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
            if (iFileUploadTaskListener != null) {
                iFileUploadTaskListener.onUploadStateChanged(this.mFileUploadId, state, false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void tryAbort() {
        String str = LOG_TAG;
        Log.d(str, "Abort uploading: " + this.mFileUploadId);
        UploadFileTask uploadFileTask = this.mUploadTask;
        if (uploadFileTask != null) {
            uploadFileTask.cancel(true);
            this.mUploadTask = null;
        }
        if (this.mFileUploadInfo == null) {
            handleTransferAborted();
        }
    }

    private boolean parseResult(String msgBody) {
        try {
            FtHttpFileInfo fileInfo = FtHttpXmlParser.parse(msgBody);
            if (fileInfo == null) {
                return false;
            }
            if (fileInfo.isThumbnailExist()) {
                this.mFileUploadInfo = new FileUploadInfo(Uri.parse(fileInfo.getDataUrl().toString()), convertTimeToLong(fileInfo.getDataUntil()), fileInfo.getFileName(), fileInfo.getFileSize(), fileInfo.getContentType(), Uri.parse(fileInfo.getThumbnailDataUrl().toString()), convertTimeToLong(fileInfo.getThumbnailDataUntil()), fileInfo.getThumbnailFileSize(), fileInfo.getThumbnailContentType(), 0, 0);
                return true;
            }
            this.mFileUploadInfo = new FileUploadInfo(Uri.parse(fileInfo.getDataUrl().toString()), convertTimeToLong(fileInfo.getDataUntil()), fileInfo.getFileName(), fileInfo.getFileSize(), fileInfo.getContentType(), Uri.EMPTY, 0, 0, "", 0, 0);
            return true;
        } catch (XmlPullParserException e) {
            String str = LOG_TAG;
            Log.e(str, "Can't parse upload result: " + msgBody);
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, e2.toString() + ": " + e2.getMessage());
            e2.printStackTrace();
            return false;
        }
    }

    private long convertTimeToLong(String time) {
        try {
            return Iso8601.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public void handleProgressUpadate(long transferred) {
        this.mUploadBytes = transferred;
        IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
        if (iFileUploadTaskListener != null) {
            iFileUploadTaskListener.onUploadProgress(this.mFileUploadId, transferred, this.mFileSize);
        }
    }

    /* access modifiers changed from: private */
    public void handleTransferStarted() {
        FileUpload.State state = FileUpload.State.STARTED;
        this.mState = state;
        IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
        if (iFileUploadTaskListener != null) {
            iFileUploadTaskListener.onUploadStateChanged(this.mFileUploadId, state, false);
        }
    }

    /* access modifiers changed from: private */
    public void handleTransferCompleted(String body) {
        if (parseResult(body)) {
            this.mState = FileUpload.State.TRANSFERRED;
        } else {
            this.mState = FileUpload.State.FAILED;
        }
        IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
        if (iFileUploadTaskListener != null) {
            iFileUploadTaskListener.onUploadStateChanged(this.mFileUploadId, this.mState, true);
            if (FileUpload.State.TRANSFERRED == this.mState) {
                this.mListener.onUploadComplete(this.mFileUploadId, this.mFileUploadInfo);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleTransferCanceled(int retryTime, int errorCode) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Handle file upload state: ");
        sb.append(this.mFileUploadId);
        sb.append(", CANCELED, retry: ");
        sb.append(retryTime >= 0);
        Log.d(str, sb.toString());
        if (retryTime < 0) {
            FileUpload.State state = FileUpload.State.FAILED;
            this.mState = state;
            IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
            if (iFileUploadTaskListener != null) {
                iFileUploadTaskListener.onUploadStateChanged(this.mFileUploadId, state, true);
                return;
            }
            return;
        }
        this.bRetryEvent = true;
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(7, Integer.valueOf(errorCode)), ((long) retryTime) * 1000);
    }

    private void handleTransferAborted() {
        FileUpload.State state = FileUpload.State.ABORTED;
        this.mState = state;
        IFileUploadTaskListener iFileUploadTaskListener = this.mListener;
        if (iFileUploadTaskListener != null) {
            iFileUploadTaskListener.onUploadStateChanged(this.mFileUploadId, state, true);
        }
    }

    /* access modifiers changed from: private */
    public boolean isUploadStated() {
        return this.mState != FileUpload.State.INITIATING;
    }

    /* access modifiers changed from: private */
    public boolean isUploadCompleted() {
        return (this.mState == FileUpload.State.INITIATING || this.mState == FileUpload.State.STARTED) ? false : true;
    }

    public Uri getFileUri() {
        return this.mFileUri;
    }

    public String getFileUploadId() {
        return this.mFileUploadId;
    }

    public FileUpload.State getState() {
        return this.mState;
    }

    public FileUploadInfo getFileUploadInfo() {
        return this.mFileUploadInfo;
    }
}
