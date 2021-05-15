package com.sec.internal.ims.cmstore.omanetapi.polling;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.ims.cmstore.omanetapi.OMANetAPIHandler;
import com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLargeDataPolling;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.HttpResParamsWrapper;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
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

public class OMAPollingScheduler extends Handler implements IControllerCommonInterface, IAPICallFlowListener {
    public static final String TAG = OMAPollingScheduler.class.getSimpleName();
    private final int NO_RETRY_AFTER_VALUE = -1;
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private INetAPIEventListener mINetAPIEventListener = null;
    private boolean mIsCreateSubscriptionRunning = false;
    private boolean mIsOnePollingRunning = false;
    private boolean mIsPollingNonStopRunning = false;
    private boolean mIsPollingStarted = false;
    private boolean mIsSchedulerRunning = false;
    private final String mLine = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
    private ArrayList<OMANetAPIHandler.OnApiSucceedOnceListener> mOnApiSucceedOnceListenerList = new ArrayList<>();
    private final ReSyncParam mReSyncParam = ReSyncParam.getInstance();
    private SchedulerHelper mSchedulerHelper = null;
    private final IUIEventCallback mUIInterface;

    public OMAPollingScheduler(Looper looper, INetAPIEventListener APIEventListener, IUIEventCallback uicallback, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mINetAPIEventListener = APIEventListener;
        this.mUIInterface = uicallback;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        ReSyncParam.update();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v29, resolved type: java.lang.String} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r14) {
        /*
            r13 = this;
            com.sec.internal.ims.cmstore.utils.SchedulerHelper r0 = r13.mSchedulerHelper
            if (r0 != 0) goto L_0x000a
            com.sec.internal.ims.cmstore.utils.SchedulerHelper r0 = com.sec.internal.ims.cmstore.utils.SchedulerHelper.getInstance(r13)
            r13.mSchedulerHelper = r0
        L_0x000a:
            super.handleMessage(r14)
            int r0 = r14.what
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.valueOf((int) r0)
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "message: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            r13.logWorkingStatus()
            if (r0 != 0) goto L_0x0030
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT
        L_0x0030:
            int[] r1 = com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler.AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType
            int r2 = r0.ordinal()
            r1 = r1[r2]
            java.lang.String r2 = ""
            r3 = 1
            r4 = 0
            switch(r1) {
                case 1: goto L_0x0301;
                case 2: goto L_0x02e0;
                case 3: goto L_0x02db;
                case 4: goto L_0x02d6;
                case 5: goto L_0x028c;
                case 6: goto L_0x027c;
                case 7: goto L_0x0271;
                case 8: goto L_0x0220;
                case 9: goto L_0x01d8;
                case 10: goto L_0x01bd;
                case 11: goto L_0x016a;
                case 12: goto L_0x013c;
                case 13: goto L_0x0126;
                case 14: goto L_0x011b;
                case 15: goto L_0x00cb;
                case 16: goto L_0x0099;
                case 17: goto L_0x008a;
                case 18: goto L_0x007a;
                case 19: goto L_0x0076;
                case 20: goto L_0x0072;
                case 21: goto L_0x0065;
                case 22: goto L_0x004b;
                case 23: goto L_0x0041;
                default: goto L_0x003f;
            }
        L_0x003f:
            goto L_0x0312
        L_0x0041:
            r13.pause()
            com.sec.internal.ims.cmstore.utils.SchedulerHelper r1 = r13.mSchedulerHelper
            r1.deleteNotificationSubscriptionResource()
            goto L_0x0312
        L_0x004b:
            java.lang.Object r1 = r14.obj
            if (r1 == 0) goto L_0x0312
            java.lang.Object r1 = r14.obj
            com.sec.internal.ims.cmstore.params.HttpResParamsWrapper r1 = (com.sec.internal.ims.cmstore.params.HttpResParamsWrapper) r1
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = r1.mApi
            r13.onApiTreatAsSucceed(r2)
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r2 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING
            int r2 = r2.getId()
            java.lang.Object r3 = r1.mBufDbParams
            r13.gotoHandlerEvent(r2, r3)
            goto L_0x0312
        L_0x0065:
            java.lang.Object r1 = r14.obj
            if (r1 == 0) goto L_0x0312
            java.lang.Object r1 = r14.obj
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r1 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r1
            r13.onApiTreatAsSucceed(r1)
            goto L_0x0312
        L_0x0072:
            r13.mIsCreateSubscriptionRunning = r4
            goto L_0x0312
        L_0x0076:
            r13.mIsOnePollingRunning = r4
            goto L_0x0312
        L_0x007a:
            com.sec.internal.interfaces.ims.cmstore.IUIEventCallback r1 = r13.mUIInterface
            com.sec.internal.constants.ims.cmstore.ATTConstants$AttAmbsUIScreenNames r2 = com.sec.internal.constants.ims.cmstore.ATTConstants.AttAmbsUIScreenNames.SteadyStateError_ErrMsg7
            int r2 = r2.getId()
            java.lang.String r3 = "pop_up"
            r1.notifyUIScreen(r2, r3, r4)
            goto L_0x0312
        L_0x008a:
            com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r1 = r13.mINetAPIEventListener
            java.lang.Object r2 = r14.obj
            java.lang.Integer r2 = (java.lang.Integer) r2
            int r2 = r2.intValue()
            r1.onPauseCMNNetApiWithResumeDelay(r2)
            goto L_0x0312
        L_0x0099:
            r13.mIsOnePollingRunning = r4
            r13.mIsSchedulerRunning = r4
            r13.mIsPollingNonStopRunning = r4
            r13.mIsCreateSubscriptionRunning = r4
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
            r1.<init>()
            java.lang.String r2 = r13.mLine
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r1 = r1.setLine(r2)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r1 = r1.build()
            r2 = 0
            java.lang.Object r4 = r14.obj
            if (r4 == 0) goto L_0x00c4
            java.lang.Object r4 = r14.obj
            boolean r4 = r4 instanceof java.lang.Number
            if (r4 == 0) goto L_0x00c4
            java.lang.Object r4 = r14.obj
            java.lang.Number r4 = (java.lang.Number) r4
            long r2 = r4.longValue()
        L_0x00c4:
            com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r4 = r13.mINetAPIEventListener
            r4.onOmaAuthenticationFailed(r1, r2)
            goto L_0x0312
        L_0x00cb:
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "MSTORE_REDIRECT mIsSchedulerRunning: "
            r2.append(r3)
            boolean r3 = r13.mIsSchedulerRunning
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            boolean r1 = r13.mIsSchedulerRunning
            if (r1 == 0) goto L_0x0312
            java.lang.Object r1 = r14.obj
            if (r1 == 0) goto L_0x0312
            java.lang.Object r1 = r14.obj
            com.sec.internal.helper.httpclient.HttpRequestParams r1 = (com.sec.internal.helper.httpclient.HttpRequestParams) r1
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "ReExecute API "
            r3.append(r4)
            java.lang.Class r4 = r1.getClass()
            java.lang.String r4 = r4.getSimpleName()
            r3.append(r4)
            java.lang.String r4 = " after 302 by using new url"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r2, r3)
            com.sec.internal.helper.httpclient.HttpController r2 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            r2.execute(r1)
            goto L_0x0312
        L_0x011b:
            com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r1 = r13.mINetAPIEventListener
            java.lang.Object r2 = r14.obj
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = (com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB) r2
            r1.onCloudObjectNotificationUpdated(r2)
            goto L_0x0312
        L_0x0126:
            com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener r1 = r13.mINetAPIEventListener
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
            r2.<init>()
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = r2.build()
            r1.onCloudSyncStopped(r2)
            goto L_0x0312
        L_0x013c:
            com.sec.internal.ims.cmstore.utils.ReSyncParam.update()
            com.sec.internal.ims.cmstore.utils.ReSyncParam r1 = r13.mReSyncParam
            java.lang.String r1 = r1.getChannelResURL()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0312
            com.sec.internal.helper.httpclient.HttpController r1 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageUpdateSubscriptionChannel r8 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageUpdateSubscriptionChannel
            com.sec.internal.ims.cmstore.utils.ReSyncParam r2 = r13.mReSyncParam
            java.lang.String r4 = r2.getRestartToken()
            com.sec.internal.ims.cmstore.utils.ReSyncParam r2 = r13.mReSyncParam
            java.lang.String r5 = r2.getChannelResURL()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r7 = r13.mICloudMessageManagerHelper
            r2 = r8
            r3 = r13
            r6 = r13
            r2.<init>(r3, r4, r5, r6, r7)
            r1.execute(r8)
            goto L_0x0312
        L_0x016a:
            java.lang.String r1 = ""
            r4 = 0
            java.lang.Object r5 = r14.obj
            boolean r5 = r5 instanceof com.sec.internal.omanetapi.nc.data.ChannelDeleteData
            if (r5 == 0) goto L_0x017d
            java.lang.Object r5 = r14.obj
            com.sec.internal.omanetapi.nc.data.ChannelDeleteData r5 = (com.sec.internal.omanetapi.nc.data.ChannelDeleteData) r5
            java.lang.String r1 = r5.channelUrl
            boolean r4 = r5.isNeedRecreateChannel
            r10 = r4
            goto L_0x0183
        L_0x017d:
            java.lang.Object r5 = r14.obj
            r1 = r5
            java.lang.String r1 = (java.lang.String) r1
            r10 = r4
        L_0x0183:
            boolean r4 = android.text.TextUtils.isEmpty(r1)
            if (r4 != 0) goto L_0x0312
            com.sec.internal.helper.httpclient.HttpController r11 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel r12 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageDeleteIndividualChannel
            java.lang.String r4 = "/"
            int r4 = r1.lastIndexOf(r4)
            int r4 = r4 + r3
            java.lang.String r7 = r1.substring(r4)
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r9 = r13.mICloudMessageManagerHelper
            r4 = r12
            r5 = r13
            r6 = r13
            r8 = r10
            r4.<init>(r5, r6, r7, r8, r9)
            r11.execute(r12)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            r3.saveOMAChannelResURL(r2)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            r3.saveOMACallBackURL(r2)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            r3.saveOMAChannelURL(r2)
            goto L_0x0312
        L_0x01bd:
            com.sec.internal.helper.httpclient.HttpController r1 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription r3 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageDeleteIndividualSubscription
            java.lang.Object r4 = r14.obj
            java.lang.String r4 = (java.lang.String) r4
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r5 = r13.mICloudMessageManagerHelper
            r3.<init>(r13, r4, r5)
            r1.execute(r3)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r1 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            r1.saveOMASubscriptionResUrl(r2)
            goto L_0x0312
        L_0x01d8:
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r5 = "SEND_LONG_POLLING_REQUEST mIsSchedulerRunning: "
            r2.append(r5)
            boolean r5 = r13.mIsSchedulerRunning
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            boolean r1 = r13.mIsSchedulerRunning
            if (r1 == 0) goto L_0x0218
            boolean r1 = r13.mIsOnePollingRunning
            if (r1 != 0) goto L_0x0312
            r13.mIsOnePollingRunning = r3
            r13.mIsPollingNonStopRunning = r3
            com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling r1 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateLongPolling
            com.sec.internal.ims.cmstore.utils.ReSyncParam r2 = r13.mReSyncParam
            java.lang.String r2 = r2.getChannelURL()
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r3 = r13.mICloudMessageManagerHelper
            r1.<init>(r13, r2, r3)
            r2 = 360000(0x57e40, double:1.778636E-318)
            r1.setReadTimeout(r2)
            com.sec.internal.helper.httpclient.HttpController r2 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            r2.execute(r1)
            goto L_0x0312
        L_0x0218:
            r13.mIsOnePollingRunning = r4
            r13.mIsPollingNonStopRunning = r4
            r13.mIsCreateSubscriptionRunning = r4
            goto L_0x0312
        L_0x0220:
            boolean r1 = r13.mIsCreateSubscriptionRunning
            if (r1 != 0) goto L_0x0312
            com.sec.internal.ims.cmstore.utils.ReSyncParam.update()
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "getNotifyURL: "
            r2.append(r4)
            com.sec.internal.ims.cmstore.utils.ReSyncParam r4 = r13.mReSyncParam
            java.lang.String r4 = r4.getNotifyURL()
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.cmstore.utils.ReSyncParam r1 = r13.mReSyncParam
            java.lang.String r1 = r1.getNotifyURL()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 != 0) goto L_0x0312
            r13.mIsCreateSubscriptionRunning = r3
            com.sec.internal.helper.httpclient.HttpController r1 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel r9 = new com.sec.internal.ims.cmstore.omanetapi.nms.CloudMessageCreateSubscriptionChannel
            com.sec.internal.ims.cmstore.utils.ReSyncParam r2 = r13.mReSyncParam
            java.lang.String r4 = r2.getNotifyURL()
            com.sec.internal.ims.cmstore.utils.ReSyncParam r2 = r13.mReSyncParam
            java.lang.String r5 = r2.getRestartToken()
            r7 = 0
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r8 = r13.mICloudMessageManagerHelper
            r2 = r9
            r3 = r13
            r6 = r13
            r2.<init>(r3, r4, r5, r6, r7, r8)
            r1.execute(r9)
            goto L_0x0312
        L_0x0271:
            boolean r1 = r13.shouldSendPollRequest()
            if (r1 == 0) goto L_0x0312
            r13.checkAndUpdateSubscriptionChannel()
            goto L_0x0312
        L_0x027c:
            com.sec.internal.helper.httpclient.HttpController r1 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels r2 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageCreateNotificationChannels
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r4 = r13.mICloudMessageManagerHelper
            r2.<init>(r13, r13, r3, r4)
            r1.execute(r2)
            goto L_0x0312
        L_0x028c:
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r1 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
            java.lang.String r1 = r1.getOMAChannelResURL()
            r2 = 0
            java.lang.String r4 = TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "resUrl: "
            r5.append(r6)
            r5.append(r1)
            java.lang.String r5 = r5.toString()
            android.util.Log.i(r4, r5)
            boolean r4 = android.text.TextUtils.isEmpty(r1)
            if (r4 != 0) goto L_0x02cc
            r4 = 47
            int r4 = r1.lastIndexOf(r4)
            int r4 = r4 + r3
            java.lang.String r2 = r1.substring(r4)
            com.sec.internal.helper.httpclient.HttpController r3 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo r4 = new com.sec.internal.ims.cmstore.omanetapi.nc.CloudMessageGetIndividualNotificationChannelInfo
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r5 = r13.mICloudMessageManagerHelper
            r4.<init>(r13, r13, r2, r5)
            r3.execute(r4)
            goto L_0x0312
        L_0x02cc:
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CREATE_NOTIFICATION_CHANNEL
            int r3 = r3.getId()
            r13.sendEmptyMessage(r3)
            goto L_0x0312
        L_0x02d6:
            r13.mIsSchedulerRunning = r4
            r13.mIsPollingNonStopRunning = r4
            goto L_0x0312
        L_0x02db:
            r13.mIsSchedulerRunning = r4
            r13.mIsPollingStarted = r4
            goto L_0x0312
        L_0x02e0:
            boolean r1 = r13.mIsSchedulerRunning
            if (r1 != 0) goto L_0x02f9
            r13.mIsSchedulerRunning = r3
            boolean r1 = r13.shouldSendPollRequest()
            if (r1 == 0) goto L_0x02f6
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL
            int r1 = r1.getId()
            r13.sendEmptyMessage(r1)
            goto L_0x0312
        L_0x02f6:
            r13.mIsPollingNonStopRunning = r3
            goto L_0x0312
        L_0x02f9:
            java.lang.String r1 = TAG
            java.lang.String r2 = "already running"
            android.util.Log.i(r1, r2)
            goto L_0x0312
        L_0x0301:
            r13.mIsPollingStarted = r3
            boolean r1 = r13.shouldSendPollRequest()
            if (r1 == 0) goto L_0x0312
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r1 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.CHECK_NOTIFICATION_CHANNEL
            int r1 = r1.getId()
            r13.sendEmptyMessage(r1)
        L_0x0312:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler.handleMessage(android.os.Message):void");
    }

    /* renamed from: com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;

        static {
            int[] iArr = new int[OMASyncEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = iArr;
            try {
                iArr[OMASyncEventType.START.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.RESUME.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.STOP.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.PAUSE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CHECK_NOTIFICATION_CHANNEL.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SEND_LONG_POLLING_REQUEST.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MAILBOX_RESET.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CLOUD_UPDATE.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MSTORE_REDIRECT.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREDENTIAL_EXPIRED.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SELF_RETRY.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.SYNC_ERR.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.ONE_POLLING_FINISHED.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.API_SUCCEED.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.MOVE_ON.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DELETE_NOTIFICATION_SUBSCRIPTION_RESOURCE.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
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
        boolean isRetryEnabled = this.mICloudMessageManagerHelper.isRetryEnabled();
        String str = TAG;
        Log.i(str, "gotoHandlerEventOnFailure isRetryEnabled: " + isRetryEnabled);
        if (isRetryEnabled) {
            this.mINetAPIEventListener.onFallbackToProvision(this, request, -1);
            this.mIsOnePollingRunning = false;
            this.mIsSchedulerRunning = false;
            this.mIsPollingNonStopRunning = false;
            this.mIsCreateSubscriptionRunning = false;
            return;
        }
        sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
    }

    public void start() {
        sendEmptyMessage(OMASyncEventType.START.getId());
    }

    public void pause() {
        sendEmptyMessage(OMASyncEventType.PAUSE.getId());
    }

    public void resume() {
        sendEmptyMessage(OMASyncEventType.RESUME.getId());
    }

    public void stop() {
        sendEmptyMessage(OMASyncEventType.STOP.getId());
    }

    public void onGoToEvent(int event, Object param) {
        if (param != null) {
            sendMessage(obtainMessage(event, param));
        } else {
            sendEmptyMessage(event);
        }
    }

    private synchronized void onApiTreatAsSucceed(IHttpAPICommonInterface request) {
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
            sendEmptyMessage(OMASyncEventType.DOWNLOAD_RETRIVED.getId());
        }
    }

    public void onFixedFlowWithMessage(Message msg) {
    }

    public void onFixedFlow(int event) {
    }

    public boolean update(int eventType) {
        sendMessage(obtainMessage(eventType));
        return true;
    }

    public boolean updateDelay(int eventType, long delay) {
        return true;
    }

    public boolean updateDelayRetry(int eventType, long delay) {
        return false;
    }

    public boolean updateMessage(Message msg) {
        return sendMessage(msg);
    }

    private void checkAndUpdateSubscriptionChannel() {
        ReSyncParam.update();
        if (CloudMessagePreferenceManager.getInstance().getOMASubscriptionTime() == 0) {
            sendEmptyMessage(OMASyncEventType.CREATE_SUBSCRIPTION_CHANNEL.getId());
        } else if (this.mSchedulerHelper.isSubscriptionChannelGoingExpired() || !this.mIsPollingNonStopRunning) {
            sendEmptyMessage(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
        } else {
            sendEmptyMessage(OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId());
        }
    }

    private boolean shouldSendPollRequest() {
        return this.mIsPollingStarted && !this.mIsOnePollingRunning && this.mIsSchedulerRunning;
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = TAG;
        Log.i(str, "mLine: " + IMSLog.checker(this.mLine) + " logWorkingStatus: [mIsPollingStarted: " + this.mIsPollingStarted + " mIsSchedulerRunning: " + this.mIsSchedulerRunning + " mIsPollingRunning: " + this.mIsOnePollingRunning + "]");
    }

    public synchronized void setOnApiSucceedOnceListener(OMANetAPIHandler.OnApiSucceedOnceListener listener) {
        if (listener == null) {
            Log.i(TAG, "listener == null, onOmaApiCredentialFailed, clear mOnApiSucceedOnceListenerList");
            this.mOnApiSucceedOnceListenerList.clear();
        } else {
            this.mOnApiSucceedOnceListenerList.add(listener);
        }
    }
}
