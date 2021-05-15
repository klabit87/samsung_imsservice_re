package com.sec.internal.ims.cmstore.omanetapi.synchandler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkCreation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateAllObjects;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageObjectsOpSearch;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamBulkCreation;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import com.sec.internal.omanetapi.nms.data.Response;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseSyncHandler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    public static final String TAG = BaseSyncHandler.class.getSimpleName();
    private final int NO_RETRY_AFTER_VALUE = -1;
    protected final BufferDBTranslation mBufferDBTranslation;
    protected ParamBulkCreation mBulkCreation = null;
    protected final Queue<BufferDBChangeParam> mBulkUploadQueue = new LinkedList();
    protected OMASyncEventType mEventType;
    protected ICloudMessageManagerHelper mICloudMessageManagerHelper;
    protected final INetAPIEventListener mINetAPIEventListener;
    protected boolean mIsFTThumbnailDownload = false;
    protected boolean mIsHandlerRunning = false;
    protected boolean mIsSearchFinished = false;
    protected final String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected String mSearchCursor;
    protected final SyncMsgType mSyncMsgType;
    protected final IUIEventCallback mUIInterface;
    protected final Queue<BufferDBChangeParam> mWorkingDownloadQueue = new LinkedList();
    protected final Queue<BufferDBChangeParam> mWorkingUploadQueue = new LinkedList();

    public enum SyncOperation {
        DOWNLOAD,
        UPLOAD,
        BULK_UPLOAD
    }

    /* access modifiers changed from: protected */
    public abstract void makeBulkUploadparameter();

    /* access modifiers changed from: protected */
    public abstract HttpRequestParams peekBulkUploadQueue();

    /* access modifiers changed from: protected */
    public abstract HttpRequestParams peekDownloadQueue();

    /* access modifiers changed from: protected */
    public abstract HttpRequestParams peekUploadQueue();

    /* access modifiers changed from: protected */
    public abstract void setBulkUploadQueue(BufferDBChangeParamList bufferDBChangeParamList);

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam, SyncOperation syncOperation);

    BaseSyncHandler(Looper looper, Context context, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, String line, SyncMsgType msgtype, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mINetAPIEventListener = APIEventListener;
        this.mBufferDBTranslation = new BufferDBTranslation(context, iCloudMessageManagerHelper);
        this.mUIInterface = uicallback;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mLine = line;
        if (iCloudMessageManagerHelper.isEnableFolderIdInSearch()) {
            this.mSyncMsgType = msgtype;
        } else {
            this.mSyncMsgType = SyncMsgType.DEFAULT;
        }
        this.mSearchCursor = this.mBufferDBTranslation.getSearchCursorByLine(this.mLine, this.mSyncMsgType);
        this.mEventType = this.mBufferDBTranslation.getInitialSyncStatusByLine(this.mLine, this.mSyncMsgType);
        if (OMASyncEventType.INITIAL_SYNC_COMPLETE.equals(this.mEventType) || OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.equals(this.mEventType)) {
            this.mIsSearchFinished = true;
        }
    }

    public void resetSearchParam() {
        this.mSearchCursor = this.mBufferDBTranslation.getSearchCursorByLine(this.mLine, this.mSyncMsgType);
        this.mEventType = this.mBufferDBTranslation.getInitialSyncStatusByLine(this.mLine, this.mSyncMsgType);
        this.mWorkingDownloadQueue.clear();
        this.mWorkingUploadQueue.clear();
        String str = TAG;
        Log.d(str, "resetSearchParam, cursor: " + this.mSearchCursor + " event: " + this.mEventType);
    }

    public void setInitSyncComplete() {
        this.mIsHandlerRunning = false;
        this.mIsSearchFinished = true;
        this.mWorkingDownloadQueue.clear();
        this.mWorkingUploadQueue.clear();
        if (this.mICloudMessageManagerHelper.shouldClearCursorUponInitSyncDone()) {
            this.mSearchCursor = "";
        }
        this.mEventType = null;
        logWorkingStatus();
    }

    public void appendToWorkingQueue(BufferDBChangeParamList paramlist, SyncOperation operation) {
        String str = TAG;
        Log.d(str, "appendToWorkingQueue: " + operation);
        if (SyncOperation.BULK_UPLOAD.equals(operation)) {
            Message msg = new Message();
            msg.obj = paramlist;
            msg.what = OMASyncEventType.ADD_TO_QUEUE_BULKUPLOAD.getId();
            sendMessage(msg);
        }
    }

    public void appendToWorkingQueue(BufferDBChangeParam param, SyncOperation operation) {
        BufferDBChangeParamList list = new BufferDBChangeParamList();
        list.mChangelst.add(param);
        if (SyncOperation.DOWNLOAD.equals(operation)) {
            Message msg = new Message();
            msg.obj = list;
            msg.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
            sendMessage(msg);
        } else if (SyncOperation.UPLOAD.equals(operation)) {
            Message msg2 = new Message();
            msg2.obj = list;
            msg2.what = OMASyncEventType.ADD_TO_UPLOADWORKINGQUEUE.getId();
            sendMessage(msg2);
        }
    }

    public void handleMessage(Message msg) {
        int whatEvent;
        super.handleMessage(msg);
        OMASyncEventType event = OMASyncEventType.valueOf(msg.what);
        Log.i(TAG, "message: " + event);
        logWorkingStatus();
        if (event == null) {
            event = OMASyncEventType.DEFAULT;
        }
        boolean z = false;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                this.mINetAPIEventListener.onInitialSyncStarted();
                HttpController.getInstance().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mICloudMessageManagerHelper));
                this.mIsSearchFinished = false;
                this.mUIInterface.showInitsyncIndicator(true);
                return;
            case 2:
                if (this.mIsHandlerRunning) {
                    this.mINetAPIEventListener.onPartialSyncSummaryCompleted((ParamOMAresponseforBufDB) msg.obj);
                    HttpController.getInstance().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mICloudMessageManagerHelper));
                    this.mUIInterface.showInitsyncIndicator(true);
                }
                this.mIsSearchFinished = false;
                return;
            case 3:
                HttpController.getInstance().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, true, this.mICloudMessageManagerHelper));
                this.mIsSearchFinished = false;
                this.mUIInterface.showInitsyncIndicator(true);
                return;
            case 4:
                this.mIsHandlerRunning = false;
                this.mINetAPIEventListener.onSyncFailed(new ParamOMAresponseforBufDB.Builder().setOMASyncEventType(OMASyncEventType.PAUSE_INITIAL_SYNC).setLine(this.mLine).setSyncType(this.mSyncMsgType).setActionType(ParamOMAresponseforBufDB.ActionType.SYNC_FAILED).build());
                return;
            case 5:
                if (!this.mIsHandlerRunning) {
                    this.mIsHandlerRunning = true;
                    if (!this.mIsSearchFinished) {
                        HttpController.getInstance().execute(new CloudMessageObjectsOpSearch(this, this.mSearchCursor, this.mLine, this.mSyncMsgType, false, this.mICloudMessageManagerHelper));
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mWorkingDownloadQueue.size() > 0) {
                        checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mWorkingUploadQueue.size() > 0) {
                        checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mBulkCreation != null) {
                        retryBulkUploadRequest();
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else if (this.mBulkUploadQueue.size() > 0) {
                        checkNextBulkUploadWorkingQueue();
                        this.mUIInterface.showInitsyncIndicator(true);
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case 6:
                this.mIsHandlerRunning = false;
                return;
            case 7:
                this.mIsHandlerRunning = false;
                this.mWorkingDownloadQueue.clear();
                this.mWorkingUploadQueue.clear();
                if (this.mICloudMessageManagerHelper.shouldClearCursorUponInitSyncDone()) {
                    this.mSearchCursor = "";
                }
                this.mEventType = null;
                ParamOMAresponseforBufDB paramCancelInitSync = new ParamOMAresponseforBufDB.Builder().setOMASyncEventType(OMASyncEventType.CANCEL_INITIAL_SYNC).setLine(this.mLine).setSyncType(this.mSyncMsgType).setActionType(ParamOMAresponseforBufDB.ActionType.SYNC_FAILED).build();
                this.mUIInterface.showInitsyncIndicator(false);
                this.mINetAPIEventListener.onSyncFailed(paramCancelInitSync);
                return;
            case 8:
                this.mINetAPIEventListener.onInitSyncCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE).setOMASyncEventType(event).setLine(this.mLine).setSyncType(this.mSyncMsgType).build());
                sendEmptyMessage(OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE.getId());
                return;
            case 9:
                this.mIsSearchFinished = true;
                this.mINetAPIEventListener.onInitSyncSummaryCompleted((ParamOMAresponseforBufDB) msg.obj);
                return;
            case 10:
                Log.i(TAG, "empty queue: " + this.mWorkingUploadQueue.isEmpty());
                if (this.mWorkingUploadQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
                    return;
                } else {
                    checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                    return;
                }
            case 11:
                this.mINetAPIEventListener.onOneMessageUploaded((ParamOMAresponseforBufDB) msg.obj);
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                return;
            case 12:
                this.mINetAPIEventListener.onOneMessageUploaded((ParamOMAresponseforBufDB) msg.obj);
                this.mBulkCreation = null;
                checkIndividualResponseCodeUpload((ParamOMAresponseforBufDB) msg.obj);
                checkNextBulkUploadWorkingQueue();
                return;
            case 13:
                this.mBulkCreation = null;
                fallbackOneMessageUplaod((ParamOMAresponseforBufDB) msg.obj);
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                return;
            case 14:
                this.mINetAPIEventListener.onMessageUploadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_UPLOAD_COMPLETE).setLine(this.mLine).build());
                if (!this.mICloudMessageManagerHelper.isBulkCreationEnabled() || this.mBulkUploadQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.INITIAL_SYNC_COMPLETE.getId());
                    return;
                } else {
                    gotoHandlerEvent(OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED.getId(), (Object) null);
                    return;
                }
            case 15:
                Log.i(TAG, "empty queue: " + this.mWorkingUploadQueue.isEmpty());
                if (this.mWorkingDownloadQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
                    return;
                } else {
                    checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                    return;
                }
            case 16:
                if (msg.obj != null) {
                    this.mINetAPIEventListener.onOneMessageDownloaded((ParamOMAresponseforBufDB) msg.obj);
                    if (!this.mIsFTThumbnailDownload) {
                        this.mWorkingDownloadQueue.poll();
                    }
                    checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                    return;
                }
                return;
            case 17:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE).setLine(this.mLine).setSyncType(this.mSyncMsgType).build());
                return;
            case 18:
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 19:
                ParamOMAresponseforBufDB paramCredExpired = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                long delay = 0;
                if (msg.obj != null && (msg.obj instanceof Number)) {
                    delay = ((Number) msg.obj).longValue();
                }
                this.mINetAPIEventListener.onOmaAuthenticationFailed(paramCredExpired, delay);
                return;
            case 20:
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
                return;
            case 22:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) msg.obj).intValue());
                return;
            case 23:
                if (this.mWorkingDownloadQueue.size() == 0) {
                    z = true;
                }
                boolean isQueueEmpty = z;
                setWorkingQueue((BufferDBChangeParamList) msg.obj, SyncOperation.DOWNLOAD);
                if (this.mIsHandlerRunning && isQueueEmpty) {
                    checkNextMsgFromDownloadWorkingQueue(SyncOperation.DOWNLOAD);
                    this.mUIInterface.showInitsyncIndicator(true);
                    return;
                }
                return;
            case 24:
                if (this.mWorkingUploadQueue.size() == 0) {
                    z = true;
                }
                boolean isuploadQueueEmpty = z;
                setWorkingQueue((BufferDBChangeParamList) msg.obj, SyncOperation.UPLOAD);
                if (this.mIsHandlerRunning && isuploadQueueEmpty) {
                    checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
                    this.mUIInterface.showInitsyncIndicator(true);
                    return;
                }
                return;
            case 25:
                if (this.mBulkUploadQueue.size() == 0) {
                    z = true;
                }
                boolean isBulkUploadQueueEmpty = z;
                setBulkUploadQueue((BufferDBChangeParamList) msg.obj);
                if (this.mIsHandlerRunning && isBulkUploadQueueEmpty) {
                    checkNextBulkUploadWorkingQueue();
                    this.mUIInterface.showInitsyncIndicator(true);
                    return;
                }
                return;
            case 26:
                if (msg.obj != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) msg.obj);
                    return;
                }
                return;
            case 27:
                if (msg.obj != null) {
                    HttpResParamsWrapper paramsWrapper = (HttpResParamsWrapper) msg.obj;
                    onApiTreatAsSucceed(paramsWrapper.mApi);
                    if (paramsWrapper.mApi instanceof CloudMessageCreateAllObjects) {
                        whatEvent = OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.getId();
                    } else if (paramsWrapper.mApi instanceof CloudMessageObjectsOpSearch) {
                        whatEvent = OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.getId();
                    } else if (paramsWrapper.mApi instanceof CloudMessageBulkCreation) {
                        ParamOMAresponseforBufDB param = (ParamOMAresponseforBufDB) paramsWrapper.mBufDbParams;
                        if (param == null || ParamOMAresponseforBufDB.ActionType.FALLBACK_MESSAGES_UPLOADED != param.getActionType()) {
                            whatEvent = OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED.getId();
                        } else {
                            whatEvent = OMASyncEventType.FALLBACK_ONE_UPLOAD.getId();
                        }
                    } else {
                        whatEvent = OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.getId();
                    }
                    gotoHandlerEvent(whatEvent, paramsWrapper.mBufDbParams);
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        static {
            int[] iArr = new int[OMASyncEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = iArr;
            try {
                iArr[OMASyncEventType.START_INITIAL_SYNC.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.REQUEST_OPSEARCH_AFTER_PSF_REMOVED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.PAUSE_INITIAL_SYNC.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.TRANSIT_TO_RESUME.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.TRANSIT_TO_PAUSE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CANCEL_INITIAL_SYNC.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.INITIAL_SYNC_COMPLETE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_START_UPLOAD.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_ONE_UPLOAD_COMPLETED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_BULK_UPLOAD_COMPLETED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.FALLBACK_ONE_UPLOAD.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_END_UPLOAD.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_START_DOWNLOAD.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_ONE_DOWNLOAD_COMPLETED.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECT_END_DOWNLOAD.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MSTORE_REDIRECT.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREDENTIAL_EXPIRED.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SYNC_ERR.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SELF_RETRY.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_WORKINGQUEUE.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_UPLOADWORKINGQUEUE.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_QUEUE_BULKUPLOAD.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.API_SUCCEED.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MOVE_ON.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
        }
    }

    private void gotoHandlerEvent(int event, Object param) {
        if (param != null) {
            if (param instanceof ParamOMAresponseforBufDB) {
                ParamOMAresponseforBufDB paramResult = (ParamOMAresponseforBufDB) param;
                if (this.mICloudMessageManagerHelper.shouldClearCursorUponInitSyncDone()) {
                    this.mSearchCursor = paramResult.getSearchCursor();
                } else if (!TextUtils.isEmpty(paramResult.getSearchCursor())) {
                    this.mSearchCursor = paramResult.getSearchCursor();
                }
                this.mEventType = paramResult.getOMASyncEventType();
                String str = TAG;
                Log.i(str, "update cursor: [" + this.mSearchCursor + "], and event type: [" + this.mEventType + "]");
            }
            sendMessage(obtainMessage(event, param));
            return;
        }
        sendEmptyMessage(event);
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface request) {
        String str = TAG;
        Log.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + this.mICloudMessageManagerHelper.isRetryEnabled());
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
        } else {
            sendEmptyMessage(OMASyncEventType.PAUSE_INITIAL_SYNC.getId());
        }
    }

    public void start() {
        start(this.mLine);
    }

    /* access modifiers changed from: protected */
    public void start(String line) {
        String str = TAG;
        Log.i(str, "start: " + IMSLog.checker(line) + " mEventType: " + this.mEventType);
        if (this.mEventType == null) {
            sendEmptyMessage(OMASyncEventType.START_INITIAL_SYNC.getId());
            return;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[this.mEventType.ordinal()];
        if (i == 2) {
            sendEmptyMessage(OMASyncEventType.INITIAL_SYNC_CONTINUE_SEARCH.getId());
        } else if (i == 8) {
            sendEmptyMessage(OMASyncEventType.ONE_LINE_INIT_SYNC_COMPLETE.getId());
        } else if (i != 9) {
            sendEmptyMessage(OMASyncEventType.START_INITIAL_SYNC.getId());
        }
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        sendEmptyMessage(OMASyncEventType.CANCEL_INITIAL_SYNC.getId());
    }

    public boolean update(int eventType) {
        String str = TAG;
        Log.i(str, "update with " + eventType);
        return sendEmptyMessage(eventType);
    }

    public boolean updateDelay(int eventType, long delay) {
        String str = TAG;
        Log.i(str, "update with " + eventType + " delayed " + delay);
        return sendEmptyMessageDelayed(eventType, delay);
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    public boolean updateMessage(Message msg) {
        return sendMessage(msg);
    }

    private void onApiTreatAsSucceed(IHttpAPICommonInterface request) {
        OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener;
        this.mINetAPIEventListener.onOmaSuccess(request);
        if (this.mICloudMessageManagerHelper.isRetryEnabled() && this.mICloudMessageManagerHelper.getControllerOfLastFailedApi() == null && (onApiSucceedOnceListener = this.mOnApiSucceedOnceListener) != null) {
            onApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, param));
    }

    public void onGoToEvent(int event, Object param) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), request);
        gotoHandlerEvent(event, param);
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
        gotoHandlerEventOnFailure(request);
    }

    public void onFailedCall(IHttpAPICommonInterface request, BufferDBChangeParam newParam) {
        gotoHandlerEventOnFailure(request);
    }

    public void onFailedCall(IHttpAPICommonInterface request) {
        gotoHandlerEventOnFailure(request);
    }

    public void onFailedEvent(int event, Object param) {
        gotoHandlerEvent(event, param);
    }

    public void onOverRequest(IHttpAPICommonInterface request, String errorCode, int retryAfter) {
        String str = TAG;
        Log.i(str, request.getClass().getSimpleName() + " 429, retry after isRetryEnabled: " + this.mICloudMessageManagerHelper.isRetryEnabled());
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, retryAfter);
            return;
        }
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, (Object) null));
    }

    public void onFixedFlow(int event) {
        String str = TAG;
        Log.i(str, "onFixedFlow event is " + event);
        sendEmptyMessage(event);
    }

    public void onFixedFlowWithMessage(Message msg) {
        if (msg == null || msg.obj == null) {
            Log.e(TAG, "onFixedFlowWithMessage message is null");
        } else if (!(msg.obj instanceof ParamOMAresponseforBufDB)) {
            Log.e(TAG, "onFixedFlowWithMessage message not ParamOMAresponseforBufDB");
        } else {
            String str = TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + ((ParamOMAresponseforBufDB) msg.obj).getActionType());
            ParamOMAresponseforBufDB param = (ParamOMAresponseforBufDB) msg.obj;
            if (this.mICloudMessageManagerHelper.shouldClearCursorUponInitSyncDone()) {
                this.mSearchCursor = param.getSearchCursor();
            } else if (!TextUtils.isEmpty(param.getSearchCursor())) {
                this.mSearchCursor = param.getSearchCursor();
            }
            this.mEventType = param.getOMASyncEventType();
            sendMessage(msg);
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromDownloadWorkingQueue(SyncOperation operation) {
        String str = TAG;
        Log.i(str, "checkNextMsgFromDownloadWorkingQueue: " + operation);
        if (!this.mWorkingDownloadQueue.isEmpty()) {
            HttpRequestParams httpparam = peekDownloadQueue();
            if (httpparam == null) {
                this.mWorkingDownloadQueue.poll();
                checkNextMsgFromDownloadWorkingQueue(operation);
                return;
            }
            if (httpparam instanceof BaseNMSRequest) {
                ((BaseNMSRequest) httpparam).updateToken();
                ((BaseNMSRequest) httpparam).replaceUrlPrefix();
            }
            String str2 = TAG;
            Log.i(str2, "url : " + IMSLog.checker(httpparam.getUrl()) + " ; method: " + httpparam.getMethod());
            if (TextUtils.isEmpty(httpparam.getUrl()) || httpparam.getMethod() == null) {
                this.mWorkingDownloadQueue.poll();
                checkNextMsgFromDownloadWorkingQueue(operation);
                return;
            }
            HttpController.getInstance().execute(httpparam);
        } else if (SyncOperation.DOWNLOAD.equals(operation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
        } else if (SyncOperation.UPLOAD.equals(operation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromUploadWorkingQueue(SyncOperation operation) {
        String str = TAG;
        Log.i(str, "checkNextMsgFromUploadWorkingQueue: " + operation);
        if (!this.mWorkingUploadQueue.isEmpty()) {
            HttpRequestParams httpparam = peekUploadQueue();
            if (httpparam == null) {
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(operation);
                return;
            }
            if (httpparam instanceof BaseNMSRequest) {
                ((BaseNMSRequest) httpparam).updateToken();
                ((BaseNMSRequest) httpparam).replaceUrlPrefix();
            }
            String str2 = TAG;
            Log.i(str2, "url : " + IMSLog.checker(httpparam.getUrl()) + " ; method: " + httpparam.getMethod());
            if (TextUtils.isEmpty(httpparam.getUrl()) || httpparam.getMethod() == null) {
                this.mWorkingUploadQueue.poll();
                checkNextMsgFromUploadWorkingQueue(operation);
                return;
            }
            HttpController.getInstance().execute(httpparam);
        } else if (SyncOperation.DOWNLOAD.equals(operation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_DOWNLOAD.getId());
        } else if (SyncOperation.UPLOAD.equals(operation)) {
            sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
        }
    }

    /* access modifiers changed from: protected */
    public void checkNextBulkUploadWorkingQueue() {
        String str = TAG;
        Log.i(str, "checkNextBulkUploadWorkingQueue: mBulkUploadQueue is empty: " + this.mBulkUploadQueue.isEmpty());
        if (!this.mBulkUploadQueue.isEmpty()) {
            makeBulkUploadparameter();
            retryBulkUploadRequest();
            return;
        }
        sendEmptyMessage(OMASyncEventType.OBJECT_END_UPLOAD.getId());
    }

    /* access modifiers changed from: protected */
    public void retryBulkUploadRequest() {
        HttpRequestParams httpparam = peekBulkUploadQueue();
        if (httpparam == null) {
            checkNextBulkUploadWorkingQueue();
            return;
        }
        if (httpparam instanceof BaseNMSRequest) {
            ((BaseNMSRequest) httpparam).updateToken();
            ((BaseNMSRequest) httpparam).replaceUrlPrefix();
        }
        String str = TAG;
        Log.i(str, "retryBulkUploadRequest url : " + httpparam.getUrl() + " ; method: " + httpparam.getMethod());
        if (TextUtils.isEmpty(httpparam.getUrl()) || httpparam.getMethod() == null) {
            checkNextBulkUploadWorkingQueue();
        } else {
            HttpController.getInstance().execute(httpparam);
        }
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParamList paramlist, SyncOperation operation) {
        Iterator<BufferDBChangeParam> it = paramlist.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            if (param != null) {
                setWorkingQueue(param, operation);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = TAG;
        Log.d(str, "mLine: " + IMSLog.checker(this.mLine) + "logWorkingStatus: [mSyncMsgType: " + this.mSyncMsgType + " mIsHandlerRunning: " + this.mIsHandlerRunning + " mEventType: " + this.mEventType + " mIsSearchFinished: " + this.mIsSearchFinished + " mWorkingDownloadQueue size: " + this.mWorkingDownloadQueue.size() + " mWorkingUploadQueue size: " + this.mWorkingUploadQueue.size() + " mBulkUploadQueue size: " + this.mBulkUploadQueue.size() + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
        this.mOnApiSucceedOnceListener = listener;
    }

    private void fallbackOneMessageUplaod(ParamOMAresponseforBufDB param) {
        if (param == null || param.getBufferDBChangeParamList() == null || param.getBufferDBChangeParamList().mChangelst == null) {
            Log.d(TAG, "DBchange list is empty: do nothting ");
        } else {
            setWorkingQueue(param.getBufferDBChangeParamList(), SyncOperation.UPLOAD);
        }
    }

    private void checkIndividualResponseCodeUpload(ParamOMAresponseforBufDB param) {
        Log.i(TAG, "checkIndividualResponseCodeUpload: ");
        if (param != null && param.getBufferDBChangeParamList() != null && param.getBufferDBChangeParamList().mChangelst != null) {
            int fallbackNum = 0;
            for (int i = 0; i < param.getBulkResponseList().response.length; i++) {
                Response rsp = param.getBulkResponseList().response[i];
                if ((rsp.code == 403 || rsp.code == 503) && !this.mICloudMessageManagerHelper.bulkOpTreatSuccessIndividualResponse(rsp.code)) {
                    setWorkingQueue(param.getBufferDBChangeParamList().mChangelst.get(i), SyncOperation.UPLOAD);
                    fallbackNum++;
                }
            }
            if (fallbackNum > 0) {
                checkNextMsgFromUploadWorkingQueue(SyncOperation.UPLOAD);
            }
        }
    }
}
