package com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.data.OperationEnum;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkDeletion;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageBulkUpdate;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualObject;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessagePutObjectFlag;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import com.sec.internal.omanetapi.nms.data.BulkDelete;
import com.sec.internal.omanetapi.nms.data.BulkUpdate;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.ObjectReferenceList;
import com.sec.internal.omanetapi.nms.data.Reference;
import com.sec.internal.omanetapi.nms.data.Response;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class BaseDeviceDataUpdateHandler extends Handler implements IAPICallFlowListener, IControllerCommonInterface {
    public static final String TAG = BaseDeviceDataUpdateHandler.class.getSimpleName();
    private final int NO_RETRY_AFTER_VALUE = -1;
    protected final BufferDBTranslation mBufferDBTranslation;
    private final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final INetAPIEventListener mINetAPIEventListener;
    protected boolean mIsHandlerRunning = false;
    protected String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected SyncMsgType mSyncMsgType;
    protected final Queue<HttpRequestParams> mWorkingQueue = new LinkedList();

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam);

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParamList bufferDBChangeParamList);

    public BaseDeviceDataUpdateHandler(Looper looper, Context context, INetAPIEventListener APIEventListener, String line, SyncMsgType type, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mINetAPIEventListener = APIEventListener;
        this.mBufferDBTranslation = new BufferDBTranslation(context, iCloudMessageManagerHelper);
        this.mLine = line;
        this.mSyncMsgType = type;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        OMASyncEventType event = OMASyncEventType.valueOf(msg.what);
        Log.i(TAG, "message :: " + event);
        logWorkingStatus();
        if (event == null) {
            event = OMASyncEventType.DEFAULT;
        }
        boolean z = true;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                this.mINetAPIEventListener.onDeviceFlagUpdateSchedulerStarted();
                checkNextMsgFromWorkingQueue();
                return;
            case 2:
                if (!this.mIsHandlerRunning) {
                    this.mIsHandlerRunning = true;
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 3:
                this.mIsHandlerRunning = false;
                return;
            case 4:
                this.mIsHandlerRunning = false;
                this.mWorkingQueue.clear();
                return;
            case 6:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) msg.obj);
                this.mWorkingQueue.poll();
                if (this.mIsHandlerRunning) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                    return;
                }
                return;
            case 7:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) msg.obj);
                this.mWorkingQueue.poll();
                sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                return;
            case 8:
                if (this.mWorkingQueue.isEmpty()) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_COMPLETED.getId());
                    return;
                } else {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
            case 9:
                this.mINetAPIEventListener.onDeviceFlagUpdateCompleted((ParamOMAresponseforBufDB) msg.obj);
                sendEmptyMessage(OMASyncEventType.ONE_LINE_FLAG_SYNC_COMPLETE.getId());
                return;
            case 11:
                this.mIsHandlerRunning = false;
                ParamOMAresponseforBufDB paramCredExpired = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                long delay = 0;
                if (msg.obj != null && (msg.obj instanceof Number)) {
                    delay = ((Number) msg.obj).longValue();
                }
                this.mINetAPIEventListener.onOmaAuthenticationFailed(paramCredExpired, delay);
                return;
            case 12:
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 13:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                boolean isQueueEmpty = z;
                setWorkingQueue((BufferDBChangeParamList) msg.obj);
                if (this.mIsHandlerRunning && isQueueEmpty) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 14:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                boolean isQueueEmpty2 = z;
                this.mWorkingQueue.offer((CloudMessageBulkDeletion) msg.obj);
                if (this.mIsHandlerRunning && isQueueEmpty2) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 15:
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                boolean isQueueEmpty3 = z;
                this.mWorkingQueue.offer((CloudMessageBulkUpdate) msg.obj);
                if (this.mIsHandlerRunning && isQueueEmpty3) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 16:
                this.mWorkingQueue.poll();
                if (this.mWorkingQueue.size() != 0) {
                    z = false;
                }
                boolean isQueueEmpty4 = z;
                fallbackOneMessageUpdate(msg.obj);
                if (this.mIsHandlerRunning && isQueueEmpty4) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 17:
                this.mINetAPIEventListener.onOneDeviceFlagUpdated((ParamOMAresponseforBufDB) msg.obj);
                this.mWorkingQueue.poll();
                handleSuccessBulkOpResponse(msg.obj);
                if (this.mIsHandlerRunning) {
                    sendEmptyMessage(OMASyncEventType.UPDATE_NEXT.getId());
                    return;
                }
                return;
            case 18:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) msg.obj).intValue());
                return;
            case 19:
                if (msg.obj != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) msg.obj);
                    return;
                }
                return;
            case 20:
                if (msg.obj != null) {
                    HttpResParamsWrapper paramsWrapper = (HttpResParamsWrapper) msg.obj;
                    onApiTreatAsSucceed(paramsWrapper.mApi);
                    sendMessage(obtainMessage(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId(), paramsWrapper.mBufDbParams));
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        static {
            int[] iArr = new int[OMASyncEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = iArr;
            try {
                iArr[OMASyncEventType.TRANSIT_TO_START.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.TRANSIT_TO_RESUME.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.TRANSIT_TO_PAUSE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.TRANSIT_TO_STOP.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.PUT_OBJECT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPDATE_ONE_SUCCESSFUL.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPDATE_FAILED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPDATE_NEXT.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPDATE_COMPLETED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ONE_LINE_FLAG_SYNC_COMPLETE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREDENTIAL_EXPIRED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MSTORE_REDIRECT.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_WORKINGQUEUE.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_QUEUE_BULKDELETE.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_QUEUE_BULKUPDATE.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.FALLBACK_ONE_UPDATE_OR_DELETE.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.BULK_UPDATE_OR_DELETE_SUCCESSFUL.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SELF_RETRY.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.API_SUCCEED.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MOVE_ON.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
        }
    }

    public void appendToWorkingQueue(BufferDBChangeParamList paramlist) {
        Message msg = new Message();
        msg.obj = paramlist;
        msg.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
        sendMessage(msg);
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
        this.mINetAPIEventListener.onOmaSuccess(request);
        if (this.mICloudMessageManagerHelper.isRetryEnabled() && this.mICloudMessageManagerHelper.getControllerOfLastFailedApi() == null && this.mOnApiSucceedOnceListener != null) {
            Log.i(TAG, "API in BaseDeviceDataUpdateHandler succeed, ready to move on");
            this.mOnApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
        sendMessage(obtainMessage(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, param)));
    }

    public void onGoToEvent(int event, Object param) {
    }

    private void handleFailedBulkDeleteResponse(IHttpAPICommonInterface request) {
        if (request != null && (request instanceof CloudMessageBulkDeletion)) {
            CloudMessageBulkDeletion bulkrequest = (CloudMessageBulkDeletion) request;
            boolean shouldRetry = !this.mICloudMessageManagerHelper.bulkOpTreatSuccessRequestResponse(bulkrequest.getResponseCode());
            Log.i(TAG, "shouldRetry: " + shouldRetry + " getRetryCount: " + bulkrequest.getRetryCount());
            if (shouldRetry && bulkrequest.getRetryCount() < 1) {
                bulkrequest.increaseRetryCount();
                sendMessage(obtainMessage(OMASyncEventType.ADD_TO_QUEUE_BULKDELETE.getId(), bulkrequest));
            }
        }
    }

    private void handleFailedBulkUpdateResponse(IHttpAPICommonInterface request) {
        if (request instanceof CloudMessageBulkUpdate) {
            CloudMessageBulkUpdate bulkrequest = (CloudMessageBulkUpdate) request;
            boolean shouldRetry = !this.mICloudMessageManagerHelper.bulkOpTreatSuccessRequestResponse(bulkrequest.getResponseCode());
            Log.i(TAG, "handleSuccessBulkOpResponse shouldRetry: " + shouldRetry + " getRetryCount: " + bulkrequest.getRetryCount());
            if (shouldRetry && bulkrequest.getRetryCount() < 1) {
                bulkrequest.increaseRetryCount();
                sendMessage(obtainMessage(OMASyncEventType.ADD_TO_QUEUE_BULKUPDATE.getId(), bulkrequest));
            }
        }
    }

    private void handleSuccessBulkOpResponse(Object param) {
        if (param != null && (param instanceof ParamOMAresponseforBufDB)) {
            ParamOMAresponseforBufDB bulkOpResult = (ParamOMAresponseforBufDB) param;
            if (bulkOpResult.getBulkResponseList() != null) {
                for (int i = 0; i < bulkOpResult.getBulkResponseList().response.length; i++) {
                    Response rsp = bulkOpResult.getBulkResponseList().response[i];
                    if (rsp.code == 403 && !this.mICloudMessageManagerHelper.bulkOpTreatSuccessIndividualResponse(rsp.code)) {
                        setWorkingQueue(bulkOpResult.getBufferDBChangeParamList().mChangelst.get(i));
                    }
                }
            }
        }
    }

    private void gotoHandlerEventOnSuccess(IHttpAPICommonInterface request, int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendEmptyMessage(event);
        }
    }

    private void gotoHandlerEventOnFailure(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendEmptyMessage(event);
        }
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
        sendMessage(obtainMessage(OMASyncEventType.API_SUCCEED.getId(), request));
        gotoHandlerEventOnSuccess(request, event, param);
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
        onFailedCall(request);
    }

    public void onFailedCall(IHttpAPICommonInterface request, BufferDBChangeParam newParam) {
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
            return;
        }
        ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine);
        if (request instanceof CloudMessageDeleteIndividualObject) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED).setBufferDBChangeParam(newParam);
        } else if (request instanceof CloudMessagePutObjectFlag) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED).setBufferDBChangeParam(newParam);
        } else if (request instanceof CloudMessageBulkDeletion) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
            handleFailedBulkDeleteResponse(request);
        } else if (request instanceof CloudMessageBulkUpdate) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
            handleFailedBulkUpdateResponse(request);
        }
        sendMessage(obtainMessage(OMASyncEventType.UPDATE_FAILED.getId(), builder.build()));
    }

    public void onFailedEvent(int event, Object param) {
        gotoHandlerEventOnFailure(event, param);
    }

    public void onFailedCall(IHttpAPICommonInterface request) {
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
            return;
        }
        ParamOMAresponseforBufDB.Builder builder = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine);
        if (request instanceof CloudMessageDeleteIndividualObject) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED);
        } else if (request instanceof CloudMessagePutObjectFlag) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED);
        } else if (request instanceof CloudMessageBulkDeletion) {
            builder.setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE);
            handleFailedBulkDeleteResponse(request);
        }
        sendMessage(obtainMessage(OMASyncEventType.UPDATE_FAILED.getId(), builder.build()));
    }

    public void onOverRequest(IHttpAPICommonInterface request, String errorCode, int retryAfter) {
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            Log.i(TAG, "onOverRequest, go to session gen API if necessary");
            this.mINetAPIEventListener.onFallbackToProvision(this, request, retryAfter);
            return;
        }
        sendEmptyMessage(OMASyncEventType.UPDATE_ONE_SUCCESSFUL.getId());
    }

    public void onFixedFlow(int event) {
    }

    public void onFixedFlowWithMessage(Message msg) {
        if (!(msg == null || msg.obj == null)) {
            String str = TAG;
            Log.i(str, "onFixedFlowWithMessage message is " + ((ParamOMAresponseforBufDB) msg.obj).getActionType());
        }
        sendMessage(msg);
    }

    public void start() {
        this.mIsHandlerRunning = true;
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_START.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        this.mIsHandlerRunning = false;
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_STOP.getId());
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromWorkingQueue() {
        if (!this.mWorkingQueue.isEmpty()) {
            boolean shouldSendRequest = true;
            HttpRequestParams httpparam = this.mWorkingQueue.peek();
            if (httpparam == null) {
                this.mWorkingQueue.poll();
                Log.e(TAG, " Should not be Null. Skip the current and plz check enqueue");
                checkNextMsgFromWorkingQueue();
                return;
            }
            if (httpparam instanceof BaseNMSRequest) {
                shouldSendRequest = ((BaseNMSRequest) httpparam).updateToken();
                if (this.mICloudMessageManagerHelper.isEnableATTHeader()) {
                    ((BaseNMSRequest) httpparam).updateServerRoot(this.mICloudMessageManagerHelper.getNmsHost());
                }
            }
            if (shouldSendRequest) {
                HttpController.getInstance().execute(httpparam);
                return;
            }
            String str = TAG;
            Log.d(str, "Url: " + IMSLog.checker(httpparam.getUrl()));
            this.mWorkingQueue.poll();
            checkNextMsgFromWorkingQueue();
            return;
        }
        sendEmptyMessage(OMASyncEventType.UPDATE_COMPLETED.getId());
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = TAG;
        Log.i(str, "mLine: " + IMSLog.checker(this.mLine) + " logWorkingStatus: [mSyncMsgType: " + this.mSyncMsgType + " mIsHandlerRunning: " + this.mIsHandlerRunning + " mWorkingQueue size: " + this.mWorkingQueue.size() + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
        this.mOnApiSucceedOnceListener = listener;
    }

    /* access modifiers changed from: protected */
    public BulkUpdate createNewBulkUpdateParam(List<Reference> referenceList, String[] flags, OperationEnum operation) {
        BulkUpdate bulkupdate = new BulkUpdate();
        bulkupdate.operation = operation;
        bulkupdate.flags = new FlagList();
        bulkupdate.flags.flag = flags;
        bulkupdate.objects = new ObjectReferenceList();
        bulkupdate.objects.objectReference = (Reference[]) referenceList.toArray(new Reference[referenceList.size()]);
        return bulkupdate;
    }

    /* access modifiers changed from: protected */
    public BulkDelete createNewBulkDeleteParam(List<Reference> referenceList) {
        BulkDelete bulkdelete = new BulkDelete();
        bulkdelete.objects = new ObjectReferenceList();
        bulkdelete.objects.objectReference = (Reference[]) referenceList.toArray(new Reference[referenceList.size()]);
        return bulkdelete;
    }

    private void fallbackOneMessageUpdate(Object param) {
        if (param != null && (param instanceof ParamOMAresponseforBufDB)) {
            ParamOMAresponseforBufDB bulkOpResult = (ParamOMAresponseforBufDB) param;
            if (bulkOpResult.getBufferDBChangeParamList() != null && bulkOpResult.getBufferDBChangeParamList().mChangelst != null) {
                Iterator<BufferDBChangeParam> it = bulkOpResult.getBufferDBChangeParamList().mChangelst.iterator();
                while (it.hasNext()) {
                    setWorkingQueue(it.next());
                }
            }
        }
    }
}
