package com.sec.internal.ims.cmstore.omanetapi.gcm;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.State;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetActiveNotificationChannels;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageUpdateNotificationChannelLifeTime;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel;
import com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageUpdateSubscriptionChannel;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.ims.cmstore.utils.ReSyncParam;
import com.sec.internal.ims.cmstore.utils.SchedulerHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;

public class ChannelScheduler extends StateMachine implements IControllerCommonInterface, IAPICallFlowListener {
    private static final int STATE_EXPIRED = 2;
    private static final int STATE_GOING_EXPIRED = 1;
    private static final int STATE_NOT_EXPIRED = 0;
    public static final String TAG = ChannelScheduler.class.getSimpleName();
    private final int NO_RETRY_AFTER_VALUE = -1;
    State mChannelCheckingState = new ChannelCheckingState();
    State mChannelCreatedState = new ChannelCreatedState();
    State mChannelCreatingState = new ChannelCreatingState();
    State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public ICloudMessageManagerHelper mICloudMessageManagerHelper;
    /* access modifiers changed from: private */
    public INetAPIEventListener mINetAPIEventListener = null;
    State mLargePollingState = new LargePollingState();
    /* access modifiers changed from: private */
    public final String mLine = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
    State mLongPollingState = new LongPollingState();
    private ArrayList<OMANetAPIHandler.OnApiSucceedOnceListener> mOnApiSucceedOnceListenerList = new ArrayList<>();
    /* access modifiers changed from: private */
    public final ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    /* access modifiers changed from: private */
    public SchedulerHelper mSchedulerHelper = null;
    State mSubscribedState = new SubscribedState();
    State mSubscribingState = new SubscribingState();
    /* access modifiers changed from: private */
    public final IUIEventCallback mUIInterface;

    public ChannelScheduler(Looper looper, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(TAG, looper);
        this.mINetAPIEventListener = APIEventListener;
        this.mUIInterface = uicallback;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        ReSyncParam.update();
        this.mSchedulerHelper = SchedulerHelper.getInstance(getHandler());
        initStates();
    }

    /* access modifiers changed from: package-private */
    public OMASyncEventType InitEvent(Message msg) {
        OMASyncEventType event = OMASyncEventType.valueOf(msg.what);
        if (event == null) {
            return OMASyncEventType.DEFAULT;
        }
        return event;
    }

    private void initStates() {
        addState(this.mDefaultState);
        addState(this.mChannelCheckingState, this.mDefaultState);
        addState(this.mChannelCreatingState, this.mChannelCheckingState);
        addState(this.mChannelCreatedState, this.mChannelCreatingState);
        addState(this.mSubscribingState, this.mChannelCreatedState);
        addState(this.mSubscribedState, this.mSubscribingState);
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            addState(this.mLargePollingState, this.mSubscribedState);
        } else {
            addState(this.mLongPollingState, this.mSubscribedState);
        }
        setInitialState(this.mDefaultState);
        super.start();
    }

    /* access modifiers changed from: private */
    public void gotoHandlerEvent(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendMessage(event);
        }
    }

    private void gotoHandlerEventOnFailure(IHttpAPICommonInterface request) {
        boolean isRetryEnabled = this.mICloudMessageManagerHelper.isRetryEnabled();
        String str = TAG;
        Log.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + isRetryEnabled);
        if (isRetryEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
            sendMessage(OMASyncEventType.RESET_STATE.getId());
            NotificationListContainer.getInstance().clear();
            return;
        }
        sendMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void start() {
        sendMessage(OMASyncEventType.START.getId());
    }

    public void pause() {
        sendMessage(OMASyncEventType.PAUSE.getId());
    }

    public void resume() {
        sendMessage(OMASyncEventType.RESUME.getId());
    }

    public void stop() {
        sendMessage(OMASyncEventType.STOP.getId());
    }

    public void onGoToEvent(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendMessage(event);
        }
    }

    /* access modifiers changed from: private */
    public synchronized void onApiTreatAsSucceed(IHttpAPICommonInterface request) {
        this.mINetAPIEventListener.onOmaSuccess(request);
        if (this.mICloudMessageManagerHelper.isRetryEnabled() && ((this.mICloudMessageManagerHelper.getControllerOfLastFailedApi() == null || apiShouldMoveOn()) && this.mOnApiSucceedOnceListenerList.size() > 0)) {
            String str = TAG;
            Log.i(str, "mOnApiSucceedOnceListenerList.size() = " + this.mOnApiSucceedOnceListenerList.size());
            Iterator<OMANetAPIHandler.OnApiSucceedOnceListener> it = this.mOnApiSucceedOnceListenerList.iterator();
            while (it.hasNext()) {
                OMANetAPIHandler.OnApiSucceedOnceListener onApiSucceedOnceListener = it.next();
                if (onApiSucceedOnceListener != null) {
                    onApiSucceedOnceListener.onMoveOn();
                }
            }
            this.mOnApiSucceedOnceListenerList.clear();
        }
    }

    private boolean apiShouldMoveOn() {
        Class<? extends IHttpAPICommonInterface> lastFailedApi = this.mICloudMessageManagerHelper.getLastFailedApi();
        if (lastFailedApi == null) {
            return false;
        }
        String str = TAG;
        Log.i(str, "apiShouldMoveOn lastFailedApi:" + lastFailedApi);
        if (CloudMessageCreateLargeDataPolling.class.getSimpleName().equalsIgnoreCase(lastFailedApi.getSimpleName())) {
            return true;
        }
        return false;
    }

    public void onMoveOnToNext(IHttpAPICommonInterface request, Object param) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, param));
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request, String callFlow) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), request);
    }

    public void onSuccessfulCall(IHttpAPICommonInterface request) {
        gotoHandlerEvent(OMASyncEventType.MOVE_ON.getId(), new HttpResParamsWrapper(request, (Object) null));
    }

    public void onSuccessfulEvent(IHttpAPICommonInterface request, int event, Object param) {
        gotoHandlerEvent(OMASyncEventType.API_SUCCEED.getId(), request);
        gotoHandlerEvent(event, param);
    }

    public void onFailedCall(IHttpAPICommonInterface request, String errorCode) {
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
            this.mINetAPIEventListener.onFallbackToProvision(this, request, retryAfter);
        } else {
            sendMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    public void onFixedFlowWithMessage(Message msg) {
    }

    public void onFixedFlow(int event) {
    }

    public boolean update(int eventType) {
        if (eventType == OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId()) {
            removeMessages(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId());
            return true;
        }
        sendMessage(obtainMessage(eventType));
        return true;
    }

    public boolean updateDelay(int eventType, long delay) {
        String str = TAG;
        Log.i(str, "update with " + eventType + " delayed " + delay);
        if (hasMessages(eventType)) {
            removeMessages(eventType);
        }
        sendMessageDelayed(obtainMessage(eventType), delay);
        return true;
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    public boolean updateMessage(Message msg) {
        sendMessage(msg);
        return true;
    }

    /* access modifiers changed from: private */
    public void checkAndUpdateSubscriptionChannel() {
        ReSyncParam.update();
        if (CloudMessagePreferenceManager.getInstance().getOMASubscriptionTime() == 0) {
            sendMessage(OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (this.mSchedulerHelper.isSubscriptionChannelGoingExpired()) {
            sendMessage(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (!ATTGlobalVariables.isGcmReplacePolling()) {
            sendMessage(OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId());
        }
    }

    public synchronized void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
        if (listener == null) {
            Log.i(TAG, "listener == null, onOmaApiCredentialFailed, clear mOnApiSucceedOnceListenerList");
            this.mOnApiSucceedOnceListenerList.clear();
        } else {
            this.mOnApiSucceedOnceListenerList.add(listener);
        }
    }

    public void updateNotificationChannnelLifeTime() {
        String resUrl = CloudMessagePreferenceManager.getInstance().getOMAChannelResURL();
        String str = TAG;
        Log.i(str, "updateNotificationChannnelLifeTime resUrl: " + IMSLog.checker(resUrl));
        if (!TextUtils.isEmpty(resUrl)) {
            HttpController.getInstance().execute(new CloudMessageUpdateNotificationChannelLifeTime(this, this, resUrl.substring(resUrl.lastIndexOf(47) + 1), this.mICloudMessageManagerHelper));
        }
    }

    /* access modifiers changed from: private */
    public int isNotificationChannelGoingExpired() {
        long remainingTime = (CloudMessagePreferenceManager.getInstance().getOMAChannelCreateTime() + (CloudMessagePreferenceManager.getInstance().getOMAChannelLifeTime() * 1000)) - System.currentTimeMillis();
        String str = TAG;
        Log.i(str, "isNotificationChannelGoingExpired remainingTime:" + remainingTime);
        if (remainingTime <= 0) {
            return 2;
        }
        if (remainingTime < 900000) {
            return 1;
        }
        if (hasMessages(OMASyncEventType.UPDATE_NOTIFICATIONCHANNEL_LIFETIME.getId())) {
            return 0;
        }
        updateDelay(OMASyncEventType.UPDATE_NOTIFICATIONCHANNEL_LIFETIME.getId(), remainingTime - 900000);
        return 0;
    }

    private class DefaultState extends State {
        private DefaultState() {
        }

        public void enter() {
            ChannelScheduler.this.log("DefaultState, enter");
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v11, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v15, resolved type: java.lang.String} */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean processMessage(android.os.Message r13) {
            /*
                r12 = this;
                r0 = 1
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r1 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = r1.InitEvent(r13)
                int[] r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
                int r3 = r1.ordinal()
                r2 = r2[r3]
                switch(r2) {
                    case 1: goto L_0x01ee;
                    case 2: goto L_0x01e2;
                    case 3: goto L_0x01d6;
                    case 4: goto L_0x01d5;
                    case 5: goto L_0x0182;
                    case 6: goto L_0x015f;
                    case 7: goto L_0x0114;
                    case 8: goto L_0x00fa;
                    case 9: goto L_0x00eb;
                    case 10: goto L_0x00b7;
                    case 11: goto L_0x007a;
                    case 12: goto L_0x0067;
                    case 13: goto L_0x0052;
                    case 14: goto L_0x0043;
                    case 15: goto L_0x0025;
                    case 16: goto L_0x0015;
                    default: goto L_0x0012;
                }
            L_0x0012:
                r0 = 0
                goto L_0x01fa
            L_0x0015:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                r2.pause()
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.ims.cmstore.utils.SchedulerHelper r2 = r2.mSchedulerHelper
                r2.deleteNotificationSubscriptionResource()
                goto L_0x01fa
            L_0x0025:
                java.lang.Object r2 = r13.obj
                if (r2 == 0) goto L_0x01fa
                java.lang.Object r2 = r13.obj
                com.sec.internal.ims.cmstore.params.HttpResParamsWrapper r2 = (com.sec.internal.ims.cmstore.params.HttpResParamsWrapper) r2
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r4 = r2.mApi
                r3.onApiTreatAsSucceed(r4)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING
                int r4 = r4.getId()
                java.lang.Object r5 = r2.mBufDbParams
                r3.gotoHandlerEvent(r4, r5)
                goto L_0x01fa
            L_0x0043:
                java.lang.Object r2 = r13.obj
                if (r2 == 0) goto L_0x01fa
                java.lang.Object r2 = r13.obj
                com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r2
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                r3.onApiTreatAsSucceed(r2)
                goto L_0x01fa
            L_0x0052:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.IUIEventCallback r2 = r2.mUIInterface
                com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r3 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7
                int r3 = r3.getId()
                r4 = 0
                java.lang.String r5 = "pop_up"
                r2.notifyUIScreen(r3, r5, r4)
                goto L_0x01fa
            L_0x0067:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r2 = r2.mINetAPIEventListener
                java.lang.Object r3 = r13.obj
                java.lang.Integer r3 = (java.lang.Integer) r3
                int r3 = r3.intValue()
                r2.onPauseCMNNetApiWithResumeDelay(r3)
                goto L_0x01fa
            L_0x007a:
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                r2.<init>()
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.String r3 = r3.mLine
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setLine(r3)
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = r2.build()
                r3 = 0
                java.lang.Object r5 = r13.obj
                if (r5 == 0) goto L_0x00a1
                java.lang.Object r5 = r13.obj
                boolean r5 = r5 instanceof java.lang.Number
                if (r5 == 0) goto L_0x00a1
                java.lang.Object r5 = r13.obj
                java.lang.Number r5 = (java.lang.Number) r5
                long r3 = r5.longValue()
            L_0x00a1:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r5 = r5.mINetAPIEventListener
                r5.onOmaAuthenticationFailed(r2, r3)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r6 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE
                int r6 = r6.getId()
                r5.sendMessage((int) r6)
                goto L_0x01fa
            L_0x00b7:
                java.lang.Object r2 = r13.obj
                if (r2 == 0) goto L_0x01fa
                java.lang.Object r2 = r13.obj
                com.sec.internal.helper.httpclient.HttpRequestParams r2 = (com.sec.internal.helper.httpclient.HttpRequestParams) r2
                java.lang.String r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.TAG
                java.lang.StringBuilder r4 = new java.lang.StringBuilder
                r4.<init>()
                java.lang.String r5 = "ReExecute API "
                r4.append(r5)
                java.lang.Class r5 = r2.getClass()
                java.lang.String r5 = r5.getSimpleName()
                r4.append(r5)
                java.lang.String r5 = " after 302 by using new url"
                r4.append(r5)
                java.lang.String r4 = r4.toString()
                android.util.Log.i(r3, r4)
                com.sec.internal.helper.httpclient.HttpController r3 = com.sec.internal.helper.httpclient.HttpController.getInstance()
                r3.execute(r2)
                goto L_0x01fa
            L_0x00eb:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r2 = r2.mINetAPIEventListener
                java.lang.Object r3 = r13.obj
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = (com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB) r3
                r2.onCloudObjectNotificationUpdated(r3)
                goto L_0x01fa
            L_0x00fa:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r2 = r2.mINetAPIEventListener
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
                r3.<init>()
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r4 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r3 = r3.setActionType(r4)
                com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r3 = r3.build()
                r2.onCloudSyncStopped(r3)
                goto L_0x01fa
            L_0x0114:
                java.lang.String r2 = ""
                r3 = 0
                java.lang.Object r4 = r13.obj
                boolean r4 = r4 instanceof com.sec.internal.omanetapi.nc.data.ChannelDeleteData
                if (r4 == 0) goto L_0x012e
                java.lang.Object r4 = r13.obj
                com.sec.internal.omanetapi.nc.data.ChannelDeleteData r4 = (com.sec.internal.omanetapi.nc.data.ChannelDeleteData) r4
                java.lang.String r2 = r4.channelUrl
                boolean r3 = r4.isNeedRecreateChannel
                java.lang.String r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.TAG
                java.lang.String r6 = "need recreate channel"
                android.util.Log.d(r5, r6)
                r9 = r3
                goto L_0x0134
            L_0x012e:
                java.lang.Object r4 = r13.obj
                r2 = r4
                java.lang.String r2 = (java.lang.String) r2
                r9 = r3
            L_0x0134:
                boolean r3 = android.text.TextUtils.isEmpty(r2)
                if (r3 != 0) goto L_0x01fa
                com.sec.internal.helper.httpclient.HttpController r10 = com.sec.internal.helper.httpclient.HttpController.getInstance()
                com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel r11 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r5 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.String r3 = "/"
                int r3 = r2.lastIndexOf(r3)
                int r3 = r3 + 1
                java.lang.String r6 = r2.substring(r3)
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r3 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r8 = r3.mICloudMessageManagerHelper
                r3 = r11
                r4 = r5
                r7 = r9
                r3.<init>(r4, r5, r6, r7, r8)
                r10.execute(r11)
                goto L_0x01fa
            L_0x015f:
                com.sec.internal.helper.httpclient.HttpController r2 = com.sec.internal.helper.httpclient.HttpController.getInstance()
                com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription r3 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r4 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.Object r5 = r13.obj
                java.lang.String r5 = (java.lang.String) r5
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r6 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r6 = r6.mICloudMessageManagerHelper
                r3.<init>(r4, r5, r6)
                r2.execute(r3)
                com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
                java.lang.String r3 = ""
                r2.saveOMASubscriptionResUrl(r3)
                goto L_0x01fa
            L_0x0182:
                boolean r2 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.isGcmReplacePolling()
                if (r2 == 0) goto L_0x0194
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_ACTIVE_NOTIFICATIONCHANNEL
                int r3 = r3.getId()
                r2.sendMessage((int) r3)
                goto L_0x01cd
            L_0x0194:
                com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
                java.lang.String r2 = r2.getOMAChannelResURL()
                r3 = 0
                boolean r4 = android.text.TextUtils.isEmpty(r2)
                if (r4 != 0) goto L_0x01c2
                r4 = 47
                int r4 = r2.lastIndexOf(r4)
                int r4 = r4 + 1
                java.lang.String r3 = r2.substring(r4)
                com.sec.internal.helper.httpclient.HttpController r4 = com.sec.internal.helper.httpclient.HttpController.getInstance()
                com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo r5 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r6 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r7 = r6.mICloudMessageManagerHelper
                r5.<init>(r6, r6, r3, r7)
                r4.execute(r5)
                goto L_0x01cd
            L_0x01c2:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r4 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r5 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL
                int r5 = r5.getId()
                r4.sendMessage((int) r5)
            L_0x01cd:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.helper.State r3 = r2.mChannelCheckingState
                r2.transitionTo(r3)
                goto L_0x01fa
            L_0x01d5:
                goto L_0x01fa
            L_0x01d6:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE
                int r3 = r3.getId()
                r2.sendMessage((int) r3)
                goto L_0x01fa
            L_0x01e2:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL
                int r3 = r3.getId()
                r2.sendMessage((int) r3)
                goto L_0x01fa
            L_0x01ee:
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.RESET_STATE
                int r3 = r3.getId()
                r2.sendMessage((int) r3)
            L_0x01fa:
                if (r0 == 0) goto L_0x0212
                com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler r2 = com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.this
                java.lang.StringBuilder r3 = new java.lang.StringBuilder
                r3.<init>()
                java.lang.String r4 = "DefaultState, Handled : "
                r3.append(r4)
                r3.append(r1)
                java.lang.String r3 = r3.toString()
                r2.log(r3)
            L_0x0212:
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler.DefaultState.processMessage(android.os.Message):boolean");
        }

        public void exit() {
            ChannelScheduler.this.log("DefaultState, exit");
        }
    }

    private class ChannelCheckingState extends State {
        private ChannelCheckingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCheckingState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
            if (i != 5) {
                switch (i) {
                    case 17:
                        if (ChannelScheduler.this.hasMessages(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId())) {
                            ChannelScheduler.this.removeMessages(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId());
                        }
                        ChannelScheduler channelScheduler = ChannelScheduler.this;
                        channelScheduler.transitionTo(channelScheduler.mDefaultState);
                        break;
                    case 18:
                        HttpController instance = HttpController.getInstance();
                        ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                        instance.execute(new CloudMessageGetActiveNotificationChannels(channelScheduler2, channelScheduler2, channelScheduler2.mICloudMessageManagerHelper));
                        break;
                    case 19:
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            String gcmToken = CloudMessagePreferenceManager.getInstance().getGcmTokenFromVsim();
                            String str = ChannelScheduler.TAG;
                            Log.i(str, "Get GCM token from NSDSProvider, gcmToken=" + gcmToken);
                            if (TextUtils.isEmpty(gcmToken)) {
                                CloudMessagePreferenceManager.getInstance().getGcmTokenFromVsim();
                            } else if (msg.obj == null) {
                                HttpController instance2 = HttpController.getInstance();
                                ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                                instance2.execute(new CloudMessageCreateNotificationChannels(channelScheduler3, channelScheduler3, false, channelScheduler3.mICloudMessageManagerHelper));
                            } else {
                                HttpController instance3 = HttpController.getInstance();
                                ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                                instance3.execute(new CloudMessageCreateNotificationChannels(channelScheduler4, channelScheduler4, true, channelScheduler4.mICloudMessageManagerHelper));
                            }
                        } else {
                            HttpController instance4 = HttpController.getInstance();
                            ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                            instance4.execute(new CloudMessageCreateNotificationChannels(channelScheduler5, channelScheduler5, true, channelScheduler5.mICloudMessageManagerHelper));
                        }
                        ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                        channelScheduler6.transitionTo(channelScheduler6.mChannelCreatingState);
                        break;
                    default:
                        retVal = false;
                        break;
                }
            }
            if (retVal) {
                ChannelScheduler channelScheduler7 = ChannelScheduler.this;
                channelScheduler7.log("ChannelCheckingState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCheckingState, exit");
        }
    }

    private class ChannelCreatingState extends State {
        private ChannelCreatingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCreatingState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            if (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()] != 20) {
                retVal = false;
            } else {
                ChannelScheduler channelScheduler = ChannelScheduler.this;
                channelScheduler.transitionTo(channelScheduler.mChannelCreatedState);
            }
            if (retVal) {
                ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                channelScheduler2.log("ChannelCreatingState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCreatingState, exit");
        }
    }

    private class ChannelCreatedState extends State {
        private ChannelCreatedState() {
        }

        public void enter() {
            ChannelScheduler.this.log("ChannelCreatedState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
            if (i != 5) {
                switch (i) {
                    case 21:
                        ReSyncParam unused = ChannelScheduler.this.mReSyncParam;
                        ReSyncParam.update();
                        if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getNotifyURL())) {
                            HttpController instance = HttpController.getInstance();
                            ChannelScheduler channelScheduler = ChannelScheduler.this;
                            String notifyURL = channelScheduler.mReSyncParam.getNotifyURL();
                            String restartToken = ChannelScheduler.this.mReSyncParam.getRestartToken();
                            ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                            instance.execute(new CloudMessageCreateSubscriptionChannel(channelScheduler, notifyURL, restartToken, channelScheduler2, false, channelScheduler2.mICloudMessageManagerHelper));
                            ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                            channelScheduler3.transitionTo(channelScheduler3.mSubscribingState);
                            break;
                        }
                        break;
                    case 22:
                        ChannelScheduler.this.updateNotificationChannnelLifeTime();
                        break;
                    case 23:
                        ChannelScheduler.this.checkAndUpdateSubscriptionChannel();
                        break;
                    default:
                        retVal = false;
                        break;
                }
            } else {
                String resUrl = CloudMessagePreferenceManager.getInstance().getOMAChannelResURL();
                String str = ChannelScheduler.TAG;
                Log.i(str, "resUrl: " + resUrl);
                if (TextUtils.isEmpty(resUrl)) {
                    ChannelScheduler.this.sendMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId());
                } else if (ATTGlobalVariables.isGcmReplacePolling()) {
                    int state = ChannelScheduler.this.isNotificationChannelGoingExpired();
                    if (state == 1) {
                        String channelId = resUrl.substring(resUrl.lastIndexOf(47) + 1);
                        HttpController instance2 = HttpController.getInstance();
                        ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                        instance2.execute(new CloudMessageGetIndividualNotificationChannelInfo(channelScheduler4, channelScheduler4, channelId, channelScheduler4.mICloudMessageManagerHelper));
                    } else if (state == 2) {
                        ChannelScheduler.this.sendMessage(ChannelScheduler.this.obtainMessage(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) true));
                    }
                } else {
                    String channelId2 = resUrl.substring(resUrl.lastIndexOf(47) + 1);
                    HttpController instance3 = HttpController.getInstance();
                    ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                    instance3.execute(new CloudMessageGetIndividualNotificationChannelInfo(channelScheduler5, channelScheduler5, channelId2, channelScheduler5.mICloudMessageManagerHelper));
                }
            }
            if (retVal) {
                ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                channelScheduler6.log("ChannelCreatedState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("ChannelCreatedState, exit");
        }
    }

    private class SubscribingState extends State {
        private SubscribingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("SubscribingState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
            if (i == 24) {
                ChannelScheduler channelScheduler = ChannelScheduler.this;
                channelScheduler.transitionTo(channelScheduler.mSubscribedState);
            } else if (i != 25) {
                retVal = false;
            } else {
                HttpController instance = HttpController.getInstance();
                ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                String notifyURL = channelScheduler2.mReSyncParam.getNotifyURL();
                String restartToken = ChannelScheduler.this.mReSyncParam.getRestartToken();
                ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                instance.execute(new CloudMessageCreateSubscriptionChannel(channelScheduler2, notifyURL, restartToken, channelScheduler3, true, channelScheduler3.mICloudMessageManagerHelper));
            }
            if (retVal) {
                ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                channelScheduler4.log("SubscribingState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("SubscribingState, exit");
        }
    }

    private class SubscribedState extends State {
        private SubscribedState() {
        }

        public void enter() {
            ChannelScheduler.this.log("SubscribedState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            String str = ChannelScheduler.TAG;
            Log.i(str, "event:  " + event.getId());
            switch (event) {
                case CREATE_SUBSCRIPTION_FINISHED:
                    break;
                case UPDATE_SUBSCRIPTION_CHANNEL:
                    ReSyncParam unused = ChannelScheduler.this.mReSyncParam;
                    ReSyncParam.update();
                    if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                        HttpController instance = HttpController.getInstance();
                        ChannelScheduler channelScheduler = ChannelScheduler.this;
                        String restartToken = channelScheduler.mReSyncParam.getRestartToken();
                        String channelResURL = ChannelScheduler.this.mReSyncParam.getChannelResURL();
                        ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                        instance.execute(new CloudMessageUpdateSubscriptionChannel(channelScheduler, restartToken, channelResURL, channelScheduler2, channelScheduler2.mICloudMessageManagerHelper));
                        break;
                    }
                    break;
                case SEND_LONG_POLLING_REQUEST:
                    ChannelScheduler channelScheduler3 = ChannelScheduler.this;
                    HttpRequestParams polling = new CloudMessageCreateLongPolling(channelScheduler3, channelScheduler3.mReSyncParam.getChannelURL(), ChannelScheduler.this.mICloudMessageManagerHelper);
                    polling.setReadTimeout(ScheduleConstant.POLLING_TIME_OUT);
                    HttpController.getInstance().execute(polling);
                    ChannelScheduler channelScheduler4 = ChannelScheduler.this;
                    channelScheduler4.transitionTo(channelScheduler4.mLongPollingState);
                    break;
                case SEND_LARGE_DATA_POLLING_REQUEST:
                    String str2 = ChannelScheduler.TAG;
                    Log.i(str2, "large data polling " + msg.obj);
                    ChannelScheduler channelScheduler5 = ChannelScheduler.this;
                    HttpRequestParams largeDataPolling = new CloudMessageCreateLargeDataPolling(channelScheduler5, channelScheduler5, (String) msg.obj, ChannelScheduler.this.mICloudMessageManagerHelper);
                    largeDataPolling.setReadTimeout(ScheduleConstant.POLLING_TIME_OUT);
                    HttpController.getInstance().execute(largeDataPolling);
                    ChannelScheduler channelScheduler6 = ChannelScheduler.this;
                    channelScheduler6.transitionTo(channelScheduler6.mLargePollingState);
                    break;
                case UPDATE_SUBSCRIPTION_CHANNEL_DELAY:
                    if (!NotificationListContainer.getInstance().isEmpty()) {
                        if (!ChannelScheduler.this.hasMessages(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId())) {
                            ReSyncParam unused2 = ChannelScheduler.this.mReSyncParam;
                            ReSyncParam.update();
                            if (!TextUtils.isEmpty(ChannelScheduler.this.mReSyncParam.getChannelResURL())) {
                                HttpController instance2 = HttpController.getInstance();
                                ChannelScheduler channelScheduler7 = ChannelScheduler.this;
                                String restartToken2 = channelScheduler7.mReSyncParam.getRestartToken();
                                String channelResURL2 = ChannelScheduler.this.mReSyncParam.getChannelResURL();
                                ChannelScheduler channelScheduler8 = ChannelScheduler.this;
                                instance2.execute(new CloudMessageUpdateSubscriptionChannel(channelScheduler7, restartToken2, channelResURL2, channelScheduler8, channelScheduler8.mICloudMessageManagerHelper));
                                NotificationListContainer.getInstance().clear();
                                break;
                            }
                        } else {
                            ChannelScheduler.this.sendMessageDelayed(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), 60000);
                            break;
                        }
                    }
                    break;
                default:
                    retVal = false;
                    break;
            }
            if (retVal) {
                ChannelScheduler channelScheduler9 = ChannelScheduler.this;
                channelScheduler9.log("SubscribedState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("SubscribedState, exit");
        }
    }

    private class LongPollingState extends State {
        private LongPollingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("LongPollingState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            String str = ChannelScheduler.TAG;
            Log.i(str, "event:  " + event.getId());
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
            if (i != 27) {
                if (i != 30) {
                    retVal = false;
                } else {
                    ChannelScheduler channelScheduler = ChannelScheduler.this;
                    channelScheduler.transitionTo(channelScheduler.mSubscribedState);
                }
            }
            if (retVal) {
                ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                channelScheduler2.log("LongPollingState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("LongPollingState, exit");
        }
    }

    private class LargePollingState extends State {
        private LargePollingState() {
        }

        public void enter() {
            ChannelScheduler.this.log("LargePollingState, enter");
        }

        public boolean processMessage(Message msg) {
            boolean retVal = true;
            OMASyncEventType event = ChannelScheduler.this.InitEvent(msg);
            String str = ChannelScheduler.TAG;
            Log.i(str, "event:  " + event.getId());
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
            if (i == 28) {
                ChannelScheduler.this.deferMessage(msg);
            } else if (i != 31) {
                retVal = false;
            } else {
                ChannelScheduler channelScheduler = ChannelScheduler.this;
                channelScheduler.transitionTo(channelScheduler.mSubscribedState);
            }
            if (retVal) {
                ChannelScheduler channelScheduler2 = ChannelScheduler.this;
                channelScheduler2.log("LargePollingState, Handled : " + event);
            }
            return retVal;
        }

        public void exit() {
            ChannelScheduler.this.log("LargePollingState, exit");
        }
    }
}
