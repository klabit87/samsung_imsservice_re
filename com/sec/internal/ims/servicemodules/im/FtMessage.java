package com.sec.internal.ims.servicemodules.im;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.response.FileResizeResponse;
import com.sec.internal.ims.servicemodules.im.listener.FtMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.servicemodules.im.util.TidGenerator;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParserException;

public abstract class FtMessage extends MessageBase {
    public static final int DEFAULT_PLAYING_LENGTH = 0;
    protected static final long DEFAULT_TRANSFER_TIMEOUT = 300000;
    protected static final long DEFAULT_WAKE_LOCK_TIMEOUT = 10000;
    protected static final int EVENT_ACCEPT_TRANSFER = 4;
    protected static final int EVENT_ACCEPT_TRANSFER_DONE = 5;
    protected static final int EVENT_ATTACH_FILE = 1;
    protected static final int EVENT_ATTACH_FILE_ON_CREATE_THUMBNAIL = 19;
    protected static final int EVENT_ATTACH_SLM_FILE = 16;
    protected static final int EVENT_AUTOACCEPT_RESUMING = 15;
    protected static final int EVENT_AUTO_RESUME_FILE_TIMER_TIMEOUT = 21;
    protected static final int EVENT_CANCEL_TRANSFER = 8;
    protected static final int EVENT_CANCEL_TRANSFER_DONE = 9;
    protected static final int EVENT_DELAY_CANCEL_TRANSFER = 52;
    protected static final int EVENT_HANDLE_FILE_RESIZE_RESPONSE = 20;
    protected static final int EVENT_QUEUED_FILE = 14;
    protected static final int EVENT_RECEIVE_TRANSFER = 10;
    protected static final int EVENT_REJECT_TRANSFER = 6;
    protected static final int EVENT_REJECT_TRANSFER_DONE = 7;
    protected static final int EVENT_RETRY_SEND_FILE = 18;
    protected static final int EVENT_SEND_DELIVERED_NOTIFICATION_DONE = 13;
    protected static final int EVENT_SEND_FILE = 11;
    protected static final int EVENT_SEND_FILE_DONE = 2;
    protected static final int EVENT_SEND_FILE_REQUEST_TIMEOUT = 17;
    protected static final int EVENT_SEND_FILE_SESSION_HANDLE = 22;
    protected static final int EVENT_SEND_SLM_FILE_DONE = 12;
    protected static final int EVENT_SET_UP_NETWORK_FAILURE_FOR_FT = 51;
    protected static final int EVENT_SET_UP_NETWORK_SUCCESS_FOR_FT = 50;
    protected static final int EVENT_TRANSFER_PROGRESS = 3;
    protected static final int EVENT_TRANSFER_TIMER_TIMEOUT = 23;
    private static final int FTHTTP_POOL_SIZE = 3;
    private static final int KEEP_ALIVE = 1;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FtMessage.class.getSimpleName();
    private static final int MAXIMUM_POOL_SIZE = 3;
    protected static final int MAX_FILE_NAME_LEN = 128;
    protected static final int MAX_RETRY_COUNT = 3;
    protected static final int SET_UP_NETWORK_TIMEOUT = 15000;
    private static final MappingTranslator<ImError, CancelReason> sCancelReasonTranslator = new MappingTranslator.Builder().map(ImError.REMOTE_PARTY_CANCELED, CancelReason.CANCELED_BY_REMOTE).map(ImError.REMOTE_PARTY_REJECTED, CancelReason.REJECTED_BY_REMOTE).map(ImError.REMOTE_PARTY_DECLINED, CancelReason.REJECTED_BY_REMOTE).map(ImError.SESSION_TIMED_OUT, CancelReason.TIME_OUT).map(ImError.ENGINE_ERROR, CancelReason.UNKNOWN).map(ImError.NETWORK_ERROR, CancelReason.ERROR).map(ImError.NORMAL_RELEASE, CancelReason.ERROR).map(ImError.SERVICE_UNAVAILABLE, CancelReason.ERROR).map(ImError.CONNECTION_RELEASED, CancelReason.CONNECTION_RELEASED).map(ImError.FORBIDDEN_NO_WARNING_HEADER, CancelReason.NOT_AUTHORIZED).map(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE).map(ImError.REMOTE_USER_INVALID, CancelReason.REMOTE_USER_INVALID).map(ImError.NO_RESPONSE, CancelReason.NO_RESPONSE).map(ImError.CANCELED_BY_LOCAL, CancelReason.CANCELED_BY_USER).map(ImError.REMOTE_PARTY_CLOSED, CancelReason.CANCELED_BY_REMOTE).map(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, CancelReason.FORBIDDEN_NO_RETRY_FALLBACK).map(ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, CancelReason.FORBIDDEN_NO_RETRY_FALLBACK).map(ImError.CONTENT_REACHED_DOWNSIZE, CancelReason.CONTENT_REACHED_DOWNSIZE).map(ImError.INVALID_REQUEST, CancelReason.INVALID_REQUEST).map(ImError.DEVICE_UNREGISTERED, CancelReason.DEVICE_UNREGISTERED).map(ImError.MSRP_REQUEST_UNINTELLIGIBLE, CancelReason.ERROR).map(ImError.MSRP_TRANSACTION_TIMED_OUT, CancelReason.ERROR).map(ImError.MSRP_ACTION_NOT_ALLOWED, CancelReason.MSRP_SESSION_ERROR_NO_RESUME).map(ImError.MSRP_UNKNOWN_CONTENT_TYPE, CancelReason.MSRP_SESSION_ERROR_NO_RESUME).map(ImError.MSRP_SESSION_DOES_NOT_EXIST, CancelReason.ERROR).map(ImError.MSRP_SESSION_ON_OTHER_CONNECTION, CancelReason.MSRP_SESSION_ERROR_NO_RESUME).map(ImError.MSRP_PARAMETERS_OUT_OF_BOUND, CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE).map(ImError.MSRP_UNKNOWN_METHOD, CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE).map(ImError.METHOD_NOT_ALLOWED, CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE).map(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, CancelReason.MSRP_SESSION_ERROR_NO_RESUME).map(ImError.DEDICATED_BEARER_ERROR, CancelReason.ERROR).buildTranslator();
    protected static final Executor sFtHttpThreadPool = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + this.mCount.getAndIncrement());
        }
    };
    protected static final TidGenerator sTidGenerator = new TidGenerator();
    protected long FT_SIZE_MARGIN = 10240;
    protected long MAX_SIZE_DOWNLOAD_THUMBNAIL = 1048576;
    protected long MAX_SIZE_THUMBNAIL = 9216;
    protected CancelReason mCancelReason;
    protected String mDeaccentedFilePath;
    protected IMnoStrategy.ErrorNotificationId mErrorNotificationId = IMnoStrategy.ErrorNotificationId.NONE;
    protected String mFileBrandedUrl;
    protected String mFileDataUrl;
    protected FileDisposition mFileDisposition;
    protected String mFileExpire;
    protected String mFileFingerPrint;
    protected String mFileName;
    protected String mFilePath;
    protected long mFileSize;
    protected String mFileTransferId;
    protected Message mFtCompleteCallback;
    protected final String mInReplyToContributionId;
    protected boolean mIsAutoDownload;
    protected boolean mIsBootup;
    protected boolean mIsConferenceUriChanged;
    protected boolean mIsGroupChat;
    protected boolean mIsNetworkConnected;
    protected boolean mIsNetworkRequested;
    protected boolean mIsResizable;
    protected boolean mIsResuming;
    protected boolean mIsWifiUsed;
    protected FtMessageListener mListener;
    private ConnectivityManager.NetworkCallback mNetworkStateCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            Log.i(FtMessage.LOG_TAG, "ConnectivityManager.NetworkCallback: onAvailable");
            FtMessage.this.onConnectionChanged(network, true);
        }

        public void onLost(Network network) {
            Log.i(FtMessage.LOG_TAG, "ConnectivityManager.NetworkCallback: onLost");
            FtMessage.this.onConnectionChanged(network, false);
        }
    };
    protected int mPlayingLength = 0;
    protected Object mRawHandle;
    protected FtRejectReason mRejectReason;
    protected int mResumableOptionCode;
    protected int mRetryCount;
    protected int mStateId;
    protected FtStateMachine mStateMachine;
    protected String mThumbnailContentType;
    protected String mThumbnailPath;
    protected int mTimeDuration;
    protected long mTransferredBytes;
    protected final PowerManager.WakeLock mWakeLock;

    /* access modifiers changed from: protected */
    public abstract FtStateMachine createFtStateMachine(String str, Looper looper);

    public abstract int getTransferMech();

    protected FtMessage(Builder<?> builder) {
        super(builder);
        Preconditions.checkNotNull(builder.mListener);
        this.mListener = builder.mListener;
        this.mFilePath = builder.mFilePath;
        this.mFileName = builder.mFileName;
        this.mDeaccentedFilePath = deAccent(builder.mFilePath, true);
        this.mFileSize = builder.mFileSize;
        this.mFileDisposition = builder.mFileDisposition;
        this.mPlayingLength = builder.mPlayingLength;
        this.mThumbnailPath = builder.mThumbnailPath;
        this.mTimeDuration = builder.mTimeDuration;
        this.mTransferredBytes = builder.mTransferredBytes;
        this.mInReplyToContributionId = builder.mInReplyToContributionId;
        this.mFileTransferId = builder.mFileTransferId;
        this.mResumableOptionCode = builder.mResumableOptionCode;
        this.mCancelReason = CancelReason.valueOf(builder.mCancelReason);
        this.mIsResizable = builder.mIsResizable;
        this.mIsGroupChat = builder.mIsGroupChat;
        PowerManager.WakeLock newWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, FtMessage.class.getSimpleName());
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        initStateMachine(builder.mLooper, builder.mCurrentStateMachineState);
        this.mFileFingerPrint = "";
    }

    protected static CancelReason translateToCancelReason(ImError reason) {
        if (sCancelReasonTranslator.isTranslationDefined(reason)) {
            return sCancelReasonTranslator.translate(reason);
        }
        return CancelReason.UNKNOWN;
    }

    protected static boolean checkAvailableStorage(String dir, long reqSize) {
        try {
            long sdAvailSize = new StatFs(dir).getAvailableBytes();
            String str = LOG_TAG;
            Log.i(str, "checkAvailableStorage: reqSize=" + reqSize + " available=" + sdAvailSize);
            if (reqSize <= sdAvailSize) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ImConstants.Type getType(String contentType) {
        if (MIMEContentType.LOCATION_PUSH.equals(contentType)) {
            return ImConstants.Type.LOCATION;
        }
        return ImConstants.Type.MULTIMEDIA;
    }

    public static String deAccent(String input, boolean specialToUnderscore) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        String result = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(Normalizer.normalize(input, Normalizer.Form.NFD)).replaceAll("");
        if (specialToUnderscore) {
            return Normalizer.normalize(result, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "_").replaceAll("`", "_").replaceAll("'", "_");
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void initStateMachine(Looper looper, int transferState) {
        String name;
        if (TextUtils.isEmpty(this.mImdnId) || this.mImdnId.length() < 4) {
            name = "";
        } else {
            name = TextUtils.substring(this.mImdnId, 0, 4);
        }
        this.mStateMachine = createFtStateMachine(name, looper);
        if (transferState == 4 || transferState == 3 || transferState == 2) {
            this.mIsBootup = true;
        }
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.initState(ftStateMachine.getState(Integer.valueOf(transferState)));
        this.mStateId = this.mStateMachine.getStateId();
    }

    public void attachFile(boolean checkCapability) {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(1, (int) checkCapability));
    }

    public void attachSlmFile() {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(16));
    }

    public void setFtCompleteCallback(Message callback) {
        this.mFtCompleteCallback = callback;
    }

    public void sendFile() {
        if (this.mIsResuming) {
            FtStateMachine ftStateMachine = this.mStateMachine;
            ftStateMachine.sendMessage(ftStateMachine.obtainMessage(1, 1));
            return;
        }
        FtStateMachine ftStateMachine2 = this.mStateMachine;
        ftStateMachine2.sendMessage(ftStateMachine2.obtainMessage(11));
    }

    public void acceptTransfer() {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(4));
    }

    public void rejectTransfer() {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(6));
    }

    public void cancelTransfer(CancelReason reason) {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(8, (Object) reason));
    }

    public void handleFileResizeResponse(boolean isSuccess, String resizedFilePath) {
        FileResizeResponse resizeResponse = new FileResizeResponse(isSuccess, resizedFilePath);
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(20, (Object) resizeResponse));
    }

    public void receiveTransfer(Message callback, FtIncomingSessionEvent event, boolean resume) {
        this.mIsResuming = resume;
        this.mFtCompleteCallback = callback;
        if (resume && this.mStatus == ImConstants.Status.FAILED) {
            updateStatus(ImConstants.Status.UNREAD);
        }
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(10, (Object) event));
    }

    public void handleTransferProgress(FtTransferProgressEvent event) {
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(3, (Object) event));
    }

    public void removeAutoResumeFileTimer() {
        this.mStateMachine.getHandler().removeMessages(21);
    }

    public void startFileTransferTimer() {
    }

    public String getServiceTag() {
        return "FT";
    }

    public Message getFtCallback() {
        return this.mFtCompleteCallback;
    }

    public String getFilePath() {
        return this.mFilePath;
    }

    public long getFileSize() {
        return this.mFileSize;
    }

    public String getFileExpire() {
        return this.mFileExpire;
    }

    public String getFileDataUrl() {
        return this.mFileDataUrl;
    }

    public String getFileBrandedUrl() {
        return this.mFileBrandedUrl;
    }

    public FileDisposition getFileDisposition() {
        return this.mFileDisposition;
    }

    public int getPlayingLength() {
        return this.mPlayingLength;
    }

    public String getBody() {
        return this.mBody;
    }

    public void updateBody(String body) {
        this.mBody = body;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public String getStateName() {
        return this.mStateMachine.getState();
    }

    public int getStateId() {
        return this.mStateMachine.getStateId();
    }

    public FtRejectReason getRejectReason() {
        return this.mRejectReason;
    }

    public CancelReason getCancelReason() {
        return this.mCancelReason;
    }

    public int getReasonId() {
        CancelReason cancelReason = this.mCancelReason;
        if (cancelReason == null) {
            return CancelReason.UNKNOWN.getId();
        }
        return cancelReason.getId();
    }

    public long getTransferredBytes() {
        return this.mTransferredBytes;
    }

    public void setTransferredBytes(int transferredBytes) {
        this.mTransferredBytes = (long) transferredBytes;
    }

    public String getThumbnailPath() {
        return this.mThumbnailPath;
    }

    public int getTimeDuration() {
        return this.mTimeDuration;
    }

    public int getResumableOptionCode() {
        return this.mResumableOptionCode;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public String getFileTransferId() {
        return this.mFileTransferId;
    }

    public void setConversationId(String conversationId) {
        this.mConversationId = conversationId;
    }

    public void setContributionId(String contributionId) {
        this.mContributionId = contributionId;
    }

    public int getRetryCount() {
        return this.mRetryCount;
    }

    public void setRetryCount(int retryCount) {
        this.mRetryCount = retryCount;
    }

    public void updateQueued() {
        Log.i(LOG_TAG, "updateQueued");
        FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(14));
    }

    public void updateState() {
        int stateId = getStateId();
        if (this.mStateId != stateId) {
            this.mStateId = stateId;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateResumeableOptionCode(int resumeable) {
        if (this.mResumableOptionCode != resumeable) {
            this.mResumableOptionCode = resumeable;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateTransferredBytes(long bytes) {
        this.mTransferredBytes = bytes;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public boolean isResuming() {
        return this.mIsResuming;
    }

    public void setIsResuming(boolean isResuming) {
        this.mIsResuming = isResuming;
    }

    public boolean isAutoResumable() {
        return this.mConfig.getEnableFtAutoResumable();
    }

    public boolean getIsResizable() {
        return this.mIsResizable;
    }

    public void setIsResizable(boolean isResizable) {
        this.mIsResizable = isResizable;
    }

    public void setIsGroupChat(boolean isGroupChat) {
        this.mIsGroupChat = isGroupChat;
    }

    /* access modifiers changed from: protected */
    public boolean isExternalStorageAvailable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    /* access modifiers changed from: protected */
    public boolean renameFile() {
        File oldFile = new File(this.mFilePath);
        String str = LOG_TAG;
        Log.i(str, "temporary file path: " + this.mFilePath);
        String dir = this.mListener.onRequestIncomingFtTransferPath();
        File folder = new File(dir);
        if (!folder.exists() && !folder.mkdirs()) {
            Log.e(LOG_TAG, "Fail to create folder");
        }
        this.mFilePath = FilePathGenerator.generateUniqueFilePath(dir, this.mFileName, 128);
        String str2 = LOG_TAG;
        Log.i(str2, "new file path: " + this.mFilePath);
        if (oldFile.renameTo(new File(this.mFilePath))) {
            Log.i(LOG_TAG, "file rename success");
            return true;
        }
        Log.e(LOG_TAG, "file rename failure");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean deleteFile() {
        if (this.mFilePath == null) {
            return false;
        }
        File file = new File(this.mFilePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void removeThumbnail() {
        if (this.mThumbnailPath != null && this.mConfig.isFtThumb() && getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.REMOVE_FT_THUMBNAIL)) {
            new File(this.mThumbnailPath).delete();
            this.mThumbnailPath = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean deleteThumbnail() {
        if (this.mThumbnailPath == null) {
            return false;
        }
        File file = new File(this.mThumbnailPath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void invokeFtQueueCallBack() {
        Message message = this.mFtCompleteCallback;
        if (message != null) {
            message.obj = this;
            this.mFtCompleteCallback.sendToTarget();
        } else {
            Log.i(LOG_TAG, "mFtCompleteCallback is not set");
        }
        this.mFtCompleteCallback = null;
    }

    /* access modifiers changed from: protected */
    public boolean checkValidPeriod() {
        try {
            String fileExpire = getFileExpire();
            if (fileExpire == null) {
                FtHttpFileInfo fileInfo = FtHttpXmlParser.parse(this.mBody);
                if (fileInfo != null) {
                    if (fileInfo.getDataUntil() != null) {
                        fileExpire = fileInfo.getDataUntil();
                    }
                }
                Log.e(LOG_TAG, "Failed to parse FtHttpFileInfo or fileExpire is null");
                return true;
            }
            Date expiredDate = Iso8601.parse(fileExpire);
            Date currentDate = new Date(System.currentTimeMillis());
            String str = LOG_TAG;
            Log.i(str, "checkValidPeriod: expiredDate=" + expiredDate + " currentDate=" + currentDate);
            return expiredDate.after(currentDate);
        } catch (IOException | NullPointerException | ParseException | XmlPullParserException e) {
            e.printStackTrace();
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkAvailableRetry() {
        Network network = getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.EXTRA_FT_FOR_NS) ? null : this.mNetwork;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService("connectivity");
        NetworkInfo ni = network != null ? cm.getNetworkInfo(network) : cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    public void conferenceUriChanged() {
        this.mIsConferenceUriChanged = true;
    }

    public boolean isConferenceUriChanged() {
        return this.mIsConferenceUriChanged;
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock() {
        String str = LOG_TAG;
        Log.i(str, "acquireWakeLock: " + getId());
        this.mWakeLock.acquire(10000);
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock() {
        if (this.mWakeLock.isHeld()) {
            String str = LOG_TAG;
            Log.i(str, "releaseWakeLock: " + getId());
            this.mWakeLock.release();
        }
    }

    /* access modifiers changed from: protected */
    public String getFtHttpUserAgent() {
        return getRcsStrategy().getFtHttpUserAgent(this.mConfig);
    }

    public IMnoStrategy.ErrorNotificationId getErrorNotificationId() {
        return this.mErrorNotificationId;
    }

    /* access modifiers changed from: protected */
    public boolean sendSlmFile(Message sendFileCallback) {
        Set<ImsUri> participants = getRcsStrategy().getNetworkPreferredUri(this.mUriGenerator, this.mListener.onRequestParticipantUris(this.mChatId));
        if (!getRcsStrategy().checkSlmFileType(this.mContentType)) {
            String str = LOG_TAG;
            Log.i(str, "can't send slm. contentType = " + this.mContentType);
            this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
            return false;
        }
        this.mSlmService.sendFtSlmMessage(new SendSlmFileTransferParams(this.mId, participants, (String) null, (String) null, this.mFileName, this.mFilePath, this.mFileSize, this.mContentType, this.mContentType, this.mContributionId, this.mConversationId, this.mInReplyToContributionId, this.mImdnId, this.mDispNotification, sendFileCallback, isBroadcastMsg(), this.mDeviceName, this.mReliableMessage, this.mExtraFt, this.mSimIMSI));
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean validateFileResizeResponse(FileResizeResponse resizeResponse) {
        if (!this.mIsSlmSvcMsg) {
            Log.e(LOG_TAG, "validateFileResizeResponse called for non SLM msg, return");
            return false;
        } else if (!resizeResponse.isResizeSuccessful) {
            String str = LOG_TAG;
            Log.e(str, "validateFileResizeResponse File resizing failed id:" + this.mId);
            return false;
        } else if (resizeResponse.resizedFilePath == null) {
            String str2 = LOG_TAG;
            Log.e(str2, "validateFileResizeResponse no resized filepath, id:" + this.mId);
            return false;
        } else {
            File file = new File(resizeResponse.resizedFilePath);
            if (file.exists() && file.length() <= this.mConfig.getSlmMaxMsgSize()) {
                return true;
            }
            String str3 = LOG_TAG;
            Log.e(str3, "validateFileResizeResponse File resizing failed id:" + this.mId + ", length:" + file.length());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void sendCancelFtSession(CancelReason reason) {
        FtRejectReason rejectReason;
        this.mCancelReason = reason;
        if (AnonymousClass3.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[reason.ordinal()] != 1) {
            rejectReason = FtRejectReason.DECLINE;
        } else {
            rejectReason = FtRejectReason.SESSION_TIMEOUT;
        }
        RejectFtSessionParams rejectParams = new RejectFtSessionParams(this.mRawHandle, this.mStateMachine.obtainMessage(9), rejectReason, this.mFileTransferId);
        if (this.mIsSlmSvcMsg) {
            this.mSlmService.cancelFtSlmMessage(rejectParams);
        } else {
            this.mImsService.cancelFtSession(rejectParams);
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.FtMessage$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;

        static {
            int[] iArr = new int[CancelReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = iArr;
            try {
                iArr[CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean needToAcquireNetworkForFT() {
        return !getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN) && getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY) != 4 && !this.mIsNetworkConnected && !isWifiConnected();
    }

    /* access modifiers changed from: protected */
    public void acquireNetworkForFT(int networkCapability) {
        Log.i(LOG_TAG, "acquireNetworkForFT");
        this.mIsNetworkRequested = true;
        try {
            NetworkRequest mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(networkCapability).build();
            ConnectivityManager mConnectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
            mConnectivityManager.registerNetworkCallback(mNetworkRequest, this.mNetworkStateCallback);
            mConnectivityManager.requestNetwork(mNetworkRequest, this.mNetworkStateCallback);
            this.mStateMachine.sendMessageDelayed(51, 15000);
        } catch (Exception e) {
            e.printStackTrace();
            this.mStateMachine.sendMessage(51);
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkAcquiredForFT() {
        Log.i(LOG_TAG, "releaseNetworkAcquiredForFT");
        ((ConnectivityManager) getContext().getSystemService("connectivity")).unregisterNetworkCallback(this.mNetworkStateCallback);
        setNetwork((Network) null);
        this.mIsNetworkRequested = false;
        this.mIsNetworkConnected = false;
    }

    /* access modifiers changed from: private */
    public void onConnectionChanged(Network network, boolean isAvailable) {
        String str = LOG_TAG;
        Log.i(str, "onConnectionChanged: network = " + network + ", available = " + isAvailable);
        if (isAvailable) {
            if (this.mIsNetworkRequested && !this.mIsNetworkConnected) {
                if (network != null) {
                    setNetwork(network);
                    this.mIsNetworkConnected = true;
                    this.mStateMachine.sendMessage(50);
                    return;
                }
                this.mStateMachine.sendMessage(51);
            }
        } else if (this.mIsNetworkRequested) {
            setNetwork((Network) null);
            this.mIsNetworkConnected = false;
        }
    }

    public void listToDumpFormat(int MainSub, int phoneId, List<String> list) {
        int i;
        try {
            list.add(0, Integer.toString(phoneId));
            String str = this.mChatId;
            String str2 = MessageContextValues.none;
            list.add(1, str != null ? this.mChatId.substring(0, 4) : str2);
            if (this.mImdnId != null) {
                str2 = this.mImdnId.substring(0, 4);
            }
            list.add(2, str2);
            list.add(3, this.mDirection == ImDirection.INCOMING ? "MT" : "MO");
            list.add(4, Long.toString(this.mFileSize));
            String fileExt = "";
            if (this.mFileName != null && (i = this.mFileName.lastIndexOf(46)) > -1) {
                fileExt = this.mFileName.substring(i + 1, i + 4);
            }
            list.add(5, fileExt);
            IMSLog.c(MainSub, String.join(",", list));
        } catch (Exception e) {
            Log.e(LOG_TAG, "listToDumpFormat has an exception");
            e.printStackTrace();
        }
    }

    public String toString() {
        return "FtMessage [mFileName=" + IMSLog.checker(this.mFileName) + ", State=" + getStateName() + ", mTransferredBytes=" + this.mTransferredBytes + ", mFileSize=" + this.mFileSize + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mDeaccentedFilePath=" + IMSLog.checker(this.mDeaccentedFilePath) + ", mThumbnailPath=" + this.mThumbnailPath + ", mIsGroupChat=" + this.mIsGroupChat + ", mTimeDuration=" + this.mTimeDuration + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mRejectReason=" + this.mRejectReason + ", mCancelReason=" + this.mCancelReason + ", mIsResuming=" + this.mIsResuming + ", mDeviceName=" + this.mDeviceName + ", mWakeLock=" + this.mWakeLock + ", mExtInfo=" + this.mExtInfo + ", mFileFingerPrint=" + this.mFileFingerPrint + ", mFileDisposition=" + this.mFileDisposition + ", " + super.toString() + "]";
    }

    public static abstract class Builder<T extends Builder<T>> extends MessageBase.Builder<T> {
        /* access modifiers changed from: private */
        public int mCancelReason;
        /* access modifiers changed from: private */
        public int mCurrentStateMachineState;
        /* access modifiers changed from: private */
        public FileDisposition mFileDisposition;
        /* access modifiers changed from: private */
        public String mFileName;
        /* access modifiers changed from: private */
        public String mFilePath;
        /* access modifiers changed from: private */
        public long mFileSize;
        /* access modifiers changed from: private */
        public String mFileTransferId;
        /* access modifiers changed from: private */
        public String mInReplyToContributionId;
        /* access modifiers changed from: private */
        public boolean mIsGroupChat;
        /* access modifiers changed from: private */
        public boolean mIsResizable;
        /* access modifiers changed from: private */
        public FtMessageListener mListener;
        /* access modifiers changed from: private */
        public Looper mLooper;
        /* access modifiers changed from: private */
        public int mPlayingLength;
        /* access modifiers changed from: private */
        public int mResumableOptionCode;
        /* access modifiers changed from: private */
        public String mThumbnailPath;
        /* access modifiers changed from: private */
        public int mTimeDuration;
        /* access modifiers changed from: private */
        public long mTransferredBytes;

        public T looper(Looper looper) {
            this.mLooper = looper;
            return (Builder) self();
        }

        public T listener(FtMessageListener listener) {
            this.mListener = listener;
            return (Builder) self();
        }

        public T filePath(String filePath) {
            this.mFilePath = filePath;
            return (Builder) self();
        }

        public T fileName(String fileName) {
            this.mFileName = fileName;
            return (Builder) self();
        }

        public T fileSize(long fileSize) {
            this.mFileSize = fileSize;
            return (Builder) self();
        }

        public T thumbnailPath(String thumbnailPath) {
            this.mThumbnailPath = thumbnailPath;
            return (Builder) self();
        }

        public T timeDuration(int timeDuration) {
            this.mTimeDuration = timeDuration;
            return (Builder) self();
        }

        public T transferredBytes(long transferredBytes) {
            this.mTransferredBytes = transferredBytes;
            return (Builder) self();
        }

        public T fileTransferId(String fileTransferId) {
            this.mFileTransferId = fileTransferId;
            return (Builder) self();
        }

        public T inReplyToConversationId(String inReplyToConversationId) {
            this.mInReplyToContributionId = inReplyToConversationId;
            return (Builder) self();
        }

        public T setState(int currentState) {
            this.mCurrentStateMachineState = currentState;
            return (Builder) self();
        }

        public T setCancelReason(int cancelReason) {
            this.mCancelReason = cancelReason;
            return (Builder) self();
        }

        public T setResumableOptions(int resumableOptionCode) {
            this.mResumableOptionCode = resumableOptionCode;
            return (Builder) self();
        }

        public T isResizable(boolean isResizable) {
            this.mIsResizable = isResizable;
            return (Builder) self();
        }

        public T isGroupChat(boolean isGroupChat) {
            this.mIsGroupChat = isGroupChat;
            return (Builder) self();
        }

        public T setFileDisposition(FileDisposition fileDisposition) {
            this.mFileDisposition = fileDisposition;
            return (Builder) self();
        }

        public T setPlayingLength(int playingLength) {
            this.mPlayingLength = playingLength;
            return (Builder) self();
        }

        public FtMessage build() {
            return ((Builder) self()).build();
        }
    }

    protected abstract class FtStateMachine extends StateMachine {
        /* access modifiers changed from: protected */
        public abstract State getState(Integer num);

        /* access modifiers changed from: protected */
        public abstract int getStateId();

        /* access modifiers changed from: protected */
        public abstract void initState(State state);

        protected FtStateMachine(String name, Looper looper) {
            super(name, looper);
        }

        public String getState() {
            if (getCurrentState() == null) {
                return null;
            }
            return getCurrentState().getName();
        }
    }
}
