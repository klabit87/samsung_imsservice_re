package com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.BaseNMSRequest;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public abstract class BaseDataChangeHandler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    public static final String TAG = BaseDataChangeHandler.class.getSimpleName();
    private final int NO_RETRY_AFTER_VALUE = -1;
    protected final BufferDBTranslation mBufferDBTranslation;
    protected ICloudMessageManagerHelper mICloudMessageManagerHelper = null;
    private INetAPIEventListener mINetAPIEventListener = null;
    protected boolean mIsHandlerRunning = false;
    protected final String mLine;
    private OMANetAPIHandler.OnApiSucceedOnceListener mOnApiSucceedOnceListener = null;
    protected final SyncMsgType mSyncMsgType;
    private final IUIEventCallback mUIInterface;
    protected final Queue<HttpRequestParams> mWorkingQueue = new LinkedList();

    /* access modifiers changed from: protected */
    public abstract void setWorkingQueue(BufferDBChangeParam bufferDBChangeParam);

    public BaseDataChangeHandler(Looper looper, Context context, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, String line, SyncMsgType type, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mINetAPIEventListener = APIEventListener;
        this.mBufferDBTranslation = new BufferDBTranslation(context, iCloudMessageManagerHelper);
        this.mLine = line;
        this.mUIInterface = uicallback;
        this.mSyncMsgType = type;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        OMASyncEventType event = OMASyncEventType.valueOf(msg.what);
        Log.i(TAG, "message: " + event);
        logWorkingStatus();
        if (event == null) {
            event = OMASyncEventType.DEFAULT;
        }
        boolean z = true;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()]) {
            case 1:
                this.mIsHandlerRunning = true;
                BufferDBChangeParamList downloadlist = (BufferDBChangeParamList) msg.obj;
                if (downloadlist == null || downloadlist.mChangelst == null || downloadlist.mChangelst.size() <= 0) {
                    sendEmptyMessage(OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE.getId());
                    return;
                }
                Log.i(TAG, "mWorkingQueue empty: " + this.mWorkingQueue.isEmpty());
                if (this.mWorkingQueue.isEmpty()) {
                    setWorkingQueue(downloadlist);
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                setWorkingQueue(downloadlist);
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
            case 5:
                this.mINetAPIEventListener.onNotificationObjectDownloaded((ParamOMAresponseforBufDB) msg.obj);
                this.mWorkingQueue.poll();
                if (this.mIsHandlerRunning) {
                    checkNextMsgFromWorkingQueue();
                    return;
                }
                return;
            case 6:
                this.mINetAPIEventListener.onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE).build());
                sendEmptyMessage(OMASyncEventType.NORMAL_SYNC_COMPLETE.getId());
                return;
            case 7:
                sendEmptyMessage(OMASyncEventType.ONE_LINE_NORMAL_SYNC_COMPLETE.getId());
                return;
            case 8:
                this.mIsHandlerRunning = false;
                ParamOMAresponseforBufDB paramCredExpired = new ParamOMAresponseforBufDB.Builder().setLine(this.mLine).build();
                long delay = 0;
                if (msg.obj != null && (msg.obj instanceof Number)) {
                    delay = ((Number) msg.obj).longValue();
                }
                this.mINetAPIEventListener.onOmaAuthenticationFailed(paramCredExpired, delay);
                return;
            case 10:
                this.mWorkingQueue.clear();
                return;
            case 11:
                this.mINetAPIEventListener.onPauseCMNNetApiWithResumeDelay(((Integer) msg.obj).intValue());
                return;
            case 12:
                this.mUIInterface.notifyUIScreen(ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7.getId(), IUIEventCallback.POP_UP, 0);
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
                if (this.mIsHandlerRunning) {
                    pause();
                    resume();
                    return;
                }
                return;
            case 15:
                if (msg.obj != null) {
                    onApiTreatAsSucceed((IHttpAPICommonInterface) msg.obj);
                    return;
                }
                return;
            case 16:
                if (msg.obj != null) {
                    HttpResParamsWrapper paramsWrapper = (HttpResParamsWrapper) msg.obj;
                    onApiTreatAsSucceed(paramsWrapper.mApi);
                    gotoHandlerEvent(OMASyncEventType.DOWNLOAD_RETRIVED.getId(), paramsWrapper.mBufDbParams);
                    return;
                }
                return;
            default:
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler$1  reason: invalid class name */
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
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DOWNLOAD_RETRIVED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.NORMAL_SYNC_COMPLETE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREDENTIAL_EXPIRED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ONE_LINE_NORMAL_SYNC_COMPLETE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CANCEL_DOWNLOADING.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SELF_RETRY.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SYNC_ERR.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ADD_TO_WORKINGQUEUE.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MSTORE_REDIRECT.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.API_SUCCEED.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MOVE_ON.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
        }
    }

    private void gotoHandlerEvent(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendEmptyMessage(event);
        }
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface request) {
        String str = TAG;
        Log.i(str, "gotoHandlerEventOnFailure: isRetryEnabled: " + this.mICloudMessageManagerHelper.isRetryEnabled());
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
        } else {
            sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    public void appendToWorkingQueue(BufferDBChangeParam param) {
        BufferDBChangeParamList list = new BufferDBChangeParamList();
        list.mChangelst.add(param);
        Message msg = new Message();
        msg.obj = list;
        msg.what = OMASyncEventType.ADD_TO_WORKINGQUEUE.getId();
        sendMessage(msg);
    }

    public void start() {
        sendEmptyMessage(OMASyncEventType.OBJECT_AND_PAYLOAD_DOWNLOAD.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_RESUME.getId());
    }

    public void stop() {
        pause();
        sendEmptyMessage(OMASyncEventType.TRANSIT_TO_STOP.getId());
    }

    public void onGoToEvent(int event, Object param) {
    }

    private void onApiTreatAsSucceed(IHttpAPICommonInterface request) {
        this.mINetAPIEventListener.onOmaSuccess(request);
        if (this.mICloudMessageManagerHelper.isRetryEnabled() && this.mICloudMessageManagerHelper.getControllerOfLastFailedApi() == null && this.mOnApiSucceedOnceListener != null) {
            Log.i(TAG, "API in BaseDataChangeHandler succeed, ready to move on");
            this.mOnApiSucceedOnceListener.onMoveOn();
            this.mOnApiSucceedOnceListener = null;
        }
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, param));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
        Log.d(TAG, "not used in this handler");
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
        if (this.mICloudMessageManagerHelper.isRetryEnabled()) {
            Log.i(TAG, "onOverRequest, go to session gen API if necessary");
            this.mINetAPIEventListener.onFallbackToProvision(this, request, retryAfter);
            return;
        }
        sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void onFixedFlowWithMessage(Message msg) {
        if (!(msg == null || msg.obj == null)) {
            String str = TAG;
            Log.i(str, "onFixedFlowWithMessage action is " + ((ParamOMAresponseforBufDB) msg.obj).getActionType() + " event is " + msg.what);
        }
        sendMessage(msg);
    }

    public void onFixedFlow(int event) {
        String str = TAG;
        Log.i(str, "onFixedFlow event is " + event);
        sendEmptyMessage(event);
    }

    public boolean update(int eventType) {
        return sendEmptyMessage(eventType);
    }

    public boolean updateDelay(int eventType, long delay) {
        return sendEmptyMessageDelayed(eventType, delay);
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    public boolean updateMessage(Message msg) {
        return sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void checkNextMsgFromWorkingQueue() {
        if (!this.mWorkingQueue.isEmpty()) {
            boolean shouldSendRequest = true;
            HttpRequestParams httpparam = this.mWorkingQueue.peek();
            if (httpparam == null) {
                this.mWorkingQueue.poll();
                checkNextMsgFromWorkingQueue();
                return;
            }
            if (httpparam instanceof BaseNMSRequest) {
                shouldSendRequest = ((BaseNMSRequest) httpparam).updateToken();
                if (!shouldSendRequest) {
                    String str = TAG;
                    Log.e(str, "updateToken is null, again using mLine: " + this.mLine);
                    shouldSendRequest = ((BaseNMSRequest) httpparam).updateToken(this.mLine);
                }
                if (this.mICloudMessageManagerHelper.isEnableATTHeader()) {
                    ((BaseNMSRequest) httpparam).updateServerRoot(this.mICloudMessageManagerHelper.getNmsHost());
                }
            }
            if (shouldSendRequest) {
                HttpController.getInstance().execute(httpparam);
                return;
            }
            String str2 = TAG;
            Log.d(str2, "Url: " + IMSLog.checker(httpparam.getUrl()));
            this.mWorkingQueue.poll();
            checkNextMsgFromWorkingQueue();
            return;
        }
        sendEmptyMessage(OMASyncEventType.OBJECTS_AND_PAYLOAD_DOWNLOAD_COMPLETE.getId());
    }

    /* access modifiers changed from: protected */
    public void setWorkingQueue(BufferDBChangeParamList paramlist) {
        String str = TAG;
        Log.d(str, "setWorkingQueue: " + paramlist);
        Iterator<BufferDBChangeParam> it = paramlist.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            if (param != null) {
                setWorkingQueue(param);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = TAG;
        Log.d(str, "logWorkingStatus: [mLine: " + IMSLog.checker(this.mLine) + "mSyncMsgType: " + this.mSyncMsgType + " mIsHandlerRunning: " + this.mIsHandlerRunning + " mWorkingQueue size: " + this.mWorkingQueue.size() + "]");
    }

    public void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
        this.mOnApiSucceedOnceListener = listener;
    }
}
