package com.sec.internal.ims.cmstore.omanetapi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.cmstore.LineManager;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.BaseDeviceDataUpdateHandler;
import com.sec.internal.ims.cmstore.omanetapi.gcm.ChannelScheduler;
import com.sec.internal.ims.cmstore.omanetapi.polling.OMAPollingScheduler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamNetAPIStatusControl;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.INetAPIEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;

public class OMANetAPIHandler extends Handler implements IControllerCommonInterface, INetAPIEventListener {
    private static final int EVENT_APP_DATA_SYNC = 6;
    private static final int EVENT_DEVICE_DATA_UPDATE = 5;
    private static final int EVENT_INITSYNC_DATA_DOWNLOAD = 7;
    private static final int EVENT_INITSYNC_DATA_UPLOAD = 8;
    private static final int EVENT_NORMALSYNC_DATA_DOWNLOAD = 9;
    private static final int EVENT_PAUSE_CMN_NETAPI = 3;
    private static final int EVENT_PAUSE_CMN_NETAPI_WITH_CONTROLPARAM = 11;
    private static final int EVENT_RESETBOX_START_CMN_NETAPI = 10;
    private static final int EVENT_RESUME_CMN_NETAPI = 2;
    private static final int EVENT_RESUME_CMN_NETAPI_WITH_CONTROLPARAM = 12;
    private static final int EVENT_START_CMN_NETAPI = 1;
    private static final int EVENT_STOP_CMN_NETAPI = 4;
    private static final int EVENT_STOP_INITSYNC_AS_COMPLETE = 13;
    public static final String TAG = OMANetAPIHandler.class.getSimpleName();
    private final IControllerCommonInterface mChannelScheduler;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private final IWorkingStatusProvisionListener mIWorkingStatusProvisionListener;
    private boolean mIsFallbackProvisionInProcess = false;
    private boolean mIsRunning = true;
    private final LineManager mLineManager;
    private BroadcastReceiver mLowStorageReceiver;
    private final SyncHandlerFactory mSyncHandlerFactory;
    private final RegistrantList mUpdateFromCloudRegistrants = new RegistrantList();

    public interface OnApiSucceedOnceListener {
        void onMoveOn();
    }

    public void registerForUpdateFromCloud(Handler h, int what, Object obj) {
        this.mUpdateFromCloudRegistrants.add(new Registrant(h, what, obj));
    }

    public OMANetAPIHandler(Looper looper, Context context, IWorkingStatusProvisionListener controller, IUIEventCallback uicallback, LineManager linemanager, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper);
        this.mLineManager = linemanager;
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            this.mChannelScheduler = new ChannelScheduler(looper, this, uicallback, iCloudMessageManagerHelper);
        } else {
            this.mChannelScheduler = new OMAPollingScheduler(looper, this, uicallback, iCloudMessageManagerHelper);
        }
        this.mSyncHandlerFactory = new SyncHandlerFactory(looper, context, this, uicallback, this.mLineManager, iCloudMessageManagerHelper);
        this.mIWorkingStatusProvisionListener = controller;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mContext = context;
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = TAG;
        Log.i(str, "message: " + msg.what);
        logWorkingStatus();
        switch (msg.what) {
            case 1:
                this.mIsRunning = true;
                if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
                    this.mChannelScheduler.setOnApiSucceedOnceListener(new OnApiSucceedOnceListener() {
                        public void onMoveOn() {
                            Log.i(OMANetAPIHandler.TAG, "Move on: start sync");
                            if (!OMANetAPIHandler.this.mICloudMessageManagerHelper.isMultiLineSupported()) {
                                OMANetAPIHandler oMANetAPIHandler = OMANetAPIHandler.this;
                                oMANetAPIHandler.sendAppSync(new SyncParam(oMANetAPIHandler.mICloudMessageManagerHelper.getUserTelCtn(), SyncMsgType.DEFAULT));
                            }
                        }
                    });
                    this.mChannelScheduler.start();
                }
                if (this.mICloudMessageManagerHelper.isMultiLineSupported()) {
                    startAllSyncHandler();
                    return;
                }
                return;
            case 2:
                if (!this.mIsFallbackProvisionInProcess) {
                    Log.i(TAG, "Resume all handlers");
                    this.mIsRunning = true;
                    if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
                        this.mChannelScheduler.resume();
                    }
                    resumeAllSyncHandler();
                    return;
                }
                return;
            case 3:
                this.mIsRunning = false;
                if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
                    this.mChannelScheduler.pause();
                }
                pauseAllSyncHandler();
                return;
            case 4:
                this.mIsRunning = false;
                if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
                    this.mChannelScheduler.stop();
                }
                stopAllSyncHandler();
                return;
            case 5:
                BufferDBChangeParamList listNormalSyncUpdate = (BufferDBChangeParamList) msg.obj;
                if (listNormalSyncUpdate != null) {
                    sendDeviceUpdateToHandlers(listNormalSyncUpdate);
                    return;
                }
                return;
            case 7:
                BufferDBChangeParamList listInitSyncDownload = (BufferDBChangeParamList) msg.obj;
                if (listInitSyncDownload != null) {
                    sendDownloadToHandlers(listInitSyncDownload);
                    return;
                }
                return;
            case 8:
                BufferDBChangeParamList listInitSyncUpload = (BufferDBChangeParamList) msg.obj;
                if (listInitSyncUpload != null) {
                    sendUploadToHandlers(listInitSyncUpload);
                    return;
                }
                return;
            case 9:
                BufferDBChangeParamList listNormalSyncDownload = (BufferDBChangeParamList) msg.obj;
                if (listNormalSyncDownload != null) {
                    sendDownloadToDataChangeHandlers(listNormalSyncDownload);
                    return;
                }
                return;
            case 10:
                if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
                    this.mChannelScheduler.start();
                }
                if (!this.mICloudMessageManagerHelper.isMultiLineSupported()) {
                    sendAppSyncResetBox(new SyncParam(this.mICloudMessageManagerHelper.getUserTelCtn(), SyncMsgType.DEFAULT));
                    return;
                }
                return;
            case 11:
                ParamNetAPIStatusControl paramPause = (ParamNetAPIStatusControl) msg.obj;
                if (this.mICloudMessageManagerHelper.isPollingAllowed() && !paramPause.mIsMsgAppForeground) {
                    Log.d(TAG, "Pause polling");
                    this.mChannelScheduler.pause();
                }
                if (this.mICloudMessageManagerHelper.isTokenRequestedFromProvision()) {
                    if (!paramPause.mIsNetworkValid || !paramPause.mIsProvisionSuccess || paramPause.mIsUserDeleteAccount) {
                        pauseAllSyncHandler();
                        return;
                    } else {
                        Log.d(TAG, "Should only disable polling");
                        return;
                    }
                } else if (!paramPause.mIsNetworkValid) {
                    pauseAllSyncHandler();
                    return;
                } else {
                    return;
                }
            case 12:
                ParamNetAPIStatusControl paramResume = (ParamNetAPIStatusControl) msg.obj;
                this.mIsRunning = true;
                this.mIsFallbackProvisionInProcess = false;
                IControllerCommonInterface controller = this.mICloudMessageManagerHelper.getControllerOfLastFailedApi();
                if (controller == null) {
                    Log.i(TAG, "no failed API before, resume all handlers");
                    resumeHandlers(paramResume);
                    return;
                } else if ((controller instanceof OMAPollingScheduler) || (controller instanceof ChannelScheduler)) {
                    if (this.mICloudMessageManagerHelper.isPollingAllowed() && paramResume.mIsMsgAppForeground) {
                        resumeControllerOfLastFailedApi(controller, paramResume);
                        return;
                    }
                    return;
                } else if (paramResume.mIsNetworkValid) {
                    resumeControllerOfLastFailedApi(controller, paramResume);
                    return;
                } else {
                    return;
                }
            case 13:
                stopInitSyncAsComplete();
                return;
            default:
                return;
        }
    }

    private void resumeControllerOfLastFailedApi(IControllerCommonInterface controller, final ParamNetAPIStatusControl paramResume) {
        controller.setOnApiSucceedOnceListener(new OnApiSucceedOnceListener() {
            public void onMoveOn() {
                Log.i(OMANetAPIHandler.TAG, "Last failed API succeed, resume all handlers");
                OMANetAPIHandler.this.resumeHandlers(paramResume);
            }
        });
        controller.resume();
    }

    /* access modifiers changed from: private */
    public void resumeHandlers(ParamNetAPIStatusControl paramResume) {
        String str = TAG;
        Log.i(str, "resumeHandlers mIsMsgAppForeground: " + paramResume.mIsMsgAppForeground + " isPollingAllowed: " + this.mICloudMessageManagerHelper.isPollingAllowed() + " mIsNetworkValid: " + paramResume.mIsNetworkValid);
        if (this.mICloudMessageManagerHelper.isPollingAllowed() && paramResume.mIsMsgAppForeground) {
            this.mChannelScheduler.resume();
        }
        if (paramResume.mIsNetworkValid) {
            resumeAllSyncHandler();
        }
    }

    public void onLineSITRefreshed(String line) {
        String str = TAG;
        Log.i(str, "onLineSITRefreshed : " + IMSLog.checker(line));
        if (!TextUtils.isEmpty(line)) {
            resumeAllSyncHandlerByLine(line);
        }
    }

    private void resumeAllSyncHandlerByLine(String line) {
        for (BaseSyncHandler temp : this.mSyncHandlerFactory.getAllSyncHandlerInstancesByLine(line)) {
            temp.resume();
        }
        for (BaseDataChangeHandler temp2 : this.mSyncHandlerFactory.getAllDataChangeHandlerInstancesByLine(line)) {
            temp2.resume();
        }
        for (BaseDeviceDataUpdateHandler temp3 : this.mSyncHandlerFactory.getAllDeviceDataUpdateHandlerInstancesByLine(line)) {
            temp3.resume();
        }
    }

    private void startAllSyncHandler() {
        for (BaseSyncHandler temp : this.mSyncHandlerFactory.getAllSyncHandlerInstances()) {
            temp.start();
        }
        for (BaseDataChangeHandler temp2 : this.mSyncHandlerFactory.getAllDataChangeHandlerInstances()) {
            temp2.start();
        }
        for (BaseDeviceDataUpdateHandler temp3 : this.mSyncHandlerFactory.getAllDeviceDataUpdateHandlerInstances()) {
            temp3.start();
        }
    }

    private void resumeAllSyncHandler() {
        for (BaseSyncHandler temp : this.mSyncHandlerFactory.getAllSyncHandlerInstances()) {
            temp.resume();
        }
        for (BaseDataChangeHandler temp2 : this.mSyncHandlerFactory.getAllDataChangeHandlerInstances()) {
            temp2.resume();
        }
        for (BaseDeviceDataUpdateHandler temp3 : this.mSyncHandlerFactory.getAllDeviceDataUpdateHandlerInstances()) {
            temp3.resume();
        }
    }

    private void stopAllSyncHandler() {
        for (BaseSyncHandler temp : this.mSyncHandlerFactory.getAllSyncHandlerInstances()) {
            temp.stop();
        }
        this.mSyncHandlerFactory.clearAllSyncHandlerInstances();
        for (BaseDataChangeHandler temp2 : this.mSyncHandlerFactory.getAllDataChangeHandlerInstances()) {
            temp2.stop();
        }
        this.mSyncHandlerFactory.clearAllDataChangeHandlerInstances();
        for (BaseDeviceDataUpdateHandler temp3 : this.mSyncHandlerFactory.getAllDeviceDataUpdateHandlerInstances()) {
            temp3.stop();
        }
        this.mSyncHandlerFactory.clearAllDeviceDataUpdateHandlerInstances();
    }

    private void pauseAllSyncHandler() {
        for (BaseSyncHandler temp : this.mSyncHandlerFactory.getAllSyncHandlerInstances()) {
            temp.pause();
        }
        for (BaseDataChangeHandler temp2 : this.mSyncHandlerFactory.getAllDataChangeHandlerInstances()) {
            temp2.pause();
        }
        for (BaseDeviceDataUpdateHandler temp3 : this.mSyncHandlerFactory.getAllDeviceDataUpdateHandlerInstances()) {
            temp3.pause();
        }
    }

    private void sendDeviceUpdateToHandlers(BufferDBChangeParamList list) {
        String str = TAG;
        Log.i(str, "sendDeviceUpdateToHandlers: " + list);
        BufferDBChangeParamList msglist = new BufferDBChangeParamList();
        BufferDBChangeParamList vvmlist = new BufferDBChangeParamList();
        BufferDBChangeParamList faxlist = new BufferDBChangeParamList();
        BufferDBChangeParamList callloglist = new BufferDBChangeParamList();
        Iterator<BufferDBChangeParam> it = list.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            String str2 = param.mLine;
            int i = param.mDBIndex;
            if (!(i == 1 || i == 13)) {
                if (i != 21) {
                    if (!(i == 3 || i == 4)) {
                        switch (i) {
                            case 16:
                                callloglist.mChangelst.add(param);
                                break;
                            case 17:
                            case 18:
                                vvmlist.mChangelst.add(param);
                                break;
                        }
                    }
                } else {
                    faxlist.mChangelst.add(param);
                }
            }
            msglist.mChangelst.add(param);
        }
        if (msglist.mChangelst.size() > 0) {
            String line = msglist.mChangelst.get(0).mLine;
            String str3 = TAG;
            Log.i(str3, "sendDeviceUpdateToHandlers get handler : " + IMSLog.checker(line) + " type = msg");
            BaseDeviceDataUpdateHandler handler = this.mSyncHandlerFactory.getDeviceDataUpdateHandlerInstance(new SyncParam(line, SyncMsgType.MESSAGE));
            if (handler != null) {
                handler.appendToWorkingQueue(msglist);
                resumeSingleHandler(handler);
            }
        }
        if (vvmlist.mChangelst.size() > 0) {
            String line2 = vvmlist.mChangelst.get(0).mLine;
            String str4 = TAG;
            Log.i(str4, "sendDeviceUpdateToHandlers get handler : " + IMSLog.checker(line2) + " type = vvm");
            BaseDeviceDataUpdateHandler handler2 = this.mSyncHandlerFactory.getDeviceDataUpdateHandlerInstance(new SyncParam(line2, SyncMsgType.VM));
            if (handler2 != null) {
                handler2.appendToWorkingQueue(vvmlist);
                resumeSingleHandler(handler2);
            }
        }
        if (faxlist.mChangelst.size() > 0) {
            String line3 = faxlist.mChangelst.get(0).mLine;
            String str5 = TAG;
            Log.i(str5, "sendDeviceUpdateToHandlers get handler : " + IMSLog.checker(line3) + " type = fax");
            BaseDeviceDataUpdateHandler handler3 = this.mSyncHandlerFactory.getDeviceDataUpdateHandlerInstance(new SyncParam(line3, SyncMsgType.FAX));
            if (handler3 != null) {
                handler3.appendToWorkingQueue(faxlist);
                resumeSingleHandler(handler3);
            }
        }
        if (callloglist.mChangelst.size() > 0) {
            String line4 = callloglist.mChangelst.get(0).mLine;
            String str6 = TAG;
            Log.i(str6, "sendDeviceUpdateToHandlers get handler : " + IMSLog.checker(line4) + " type = call log");
            BaseDeviceDataUpdateHandler handler4 = this.mSyncHandlerFactory.getDeviceDataUpdateHandlerInstance(new SyncParam(line4, SyncMsgType.CALLLOG));
            if (handler4 != null) {
                handler4.appendToWorkingQueue(callloglist);
                resumeSingleHandler(handler4);
            }
        }
    }

    private void sendDownloadToDataChangeHandlers(BufferDBChangeParamList list) {
        String str = TAG;
        Log.i(str, "sendDownloadToDataChangeHandlers : " + list);
        BaseDataChangeHandler handler = null;
        Iterator<BufferDBChangeParam> it = list.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            String line = param.mLine;
            int i = param.mDBIndex;
            if (!(i == 1 || i == 13)) {
                if (i != 21) {
                    if (!(i == 3 || i == 4)) {
                        switch (i) {
                            case 16:
                                handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.CALLLOG));
                                if (handler == null) {
                                    break;
                                } else {
                                    handler.appendToWorkingQueue(param);
                                    break;
                                }
                            case 17:
                                handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.VM));
                                if (handler == null) {
                                    break;
                                } else {
                                    handler.appendToWorkingQueue(param);
                                    break;
                                }
                            case 18:
                                handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.VM_GREETINGS));
                                if (handler == null) {
                                    break;
                                } else {
                                    handler.appendToWorkingQueue(param);
                                    break;
                                }
                            default:
                                handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.MESSAGE));
                                if (handler == null) {
                                    break;
                                } else {
                                    handler.appendToWorkingQueue(param);
                                    break;
                                }
                        }
                    }
                } else {
                    handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.FAX));
                    if (handler != null) {
                        handler.appendToWorkingQueue(param);
                    }
                }
            }
            handler = this.mSyncHandlerFactory.getDataChangeHandlerInstance(new SyncParam(line, SyncMsgType.MESSAGE));
            if (handler != null) {
                handler.appendToWorkingQueue(param);
            }
        }
        resumeSingleHandler(handler);
    }

    private void sendDownloadToHandlers(BufferDBChangeParamList list) {
        sendToHandlerInternal(list, BaseSyncHandler.SyncOperation.DOWNLOAD);
    }

    private void sendToHandlerInternal(BufferDBChangeParamList list, BaseSyncHandler.SyncOperation operation) {
        SyncMsgType curType;
        String str = TAG;
        Log.i(str, "sendToHandlerInternal: " + list + ", operation: " + operation);
        BaseSyncHandler handler = null;
        BufferDBChangeParamList bulckChangeList = new BufferDBChangeParamList();
        Iterator<BufferDBChangeParam> it = list.mChangelst.iterator();
        while (it.hasNext()) {
            BufferDBChangeParam param = it.next();
            String line = param.mLine;
            int i = param.mDBIndex;
            if (i == 0) {
                notifyOperationsComplete((BaseSyncHandler) null, operation, param, (SyncMsgType) null);
                return;
            } else if (i == 1 || i == 3 || i == 4 || i == 13) {
                if (this.mICloudMessageManagerHelper.isEnableFolderIdInSearch()) {
                    curType = SyncMsgType.MESSAGE;
                } else {
                    curType = SyncMsgType.DEFAULT;
                }
                handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.MESSAGE));
                if (param.mRowId == 0) {
                    notifyOperationsComplete(handler, operation, param, curType);
                    handler = null;
                } else if (!this.mICloudMessageManagerHelper.isBulkCreationEnabled() || !BaseSyncHandler.SyncOperation.UPLOAD.equals(operation)) {
                    handler.appendToWorkingQueue(param, operation);
                } else {
                    bulckChangeList.mChangelst.add(param);
                }
            } else if (i != 21) {
                switch (i) {
                    case 16:
                        SyncMsgType curType2 = SyncMsgType.CALLLOG;
                        handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.CALLLOG));
                        if (param.mRowId != 0) {
                            handler.appendToWorkingQueue(param, operation);
                            break;
                        } else {
                            notifyOperationsComplete(handler, operation, param, curType2);
                            handler = null;
                            break;
                        }
                    case 17:
                        SyncMsgType curType3 = SyncMsgType.VM;
                        handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.VM));
                        if (param.mRowId != 0) {
                            handler.appendToWorkingQueue(param, operation);
                            break;
                        } else {
                            notifyOperationsComplete(handler, operation, param, curType3);
                            handler = null;
                            break;
                        }
                    case 18:
                        SyncMsgType curType4 = SyncMsgType.VM_GREETINGS;
                        handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.VM_GREETINGS));
                        if (param.mRowId != 0) {
                            handler.appendToWorkingQueue(param, operation);
                            break;
                        } else {
                            notifyOperationsComplete(handler, operation, param, curType4);
                            handler = null;
                            break;
                        }
                    default:
                        handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.MESSAGE));
                        handler.appendToWorkingQueue(param, operation);
                        break;
                }
            } else {
                SyncMsgType curType5 = SyncMsgType.FAX;
                handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.FAX));
                if (param.mRowId == 0) {
                    notifyOperationsComplete(handler, operation, param, curType5);
                    handler = null;
                } else {
                    handler.appendToWorkingQueue(param, operation);
                }
            }
        }
        if (isHandleAppendToWorkingQueue(handler, operation, bulckChangeList)) {
            handler.appendToWorkingQueue(bulckChangeList, BaseSyncHandler.SyncOperation.BULK_UPLOAD);
        }
        resumeSingleHandler(handler);
    }

    private boolean isHandleAppendToWorkingQueue(BaseSyncHandler handler, BaseSyncHandler.SyncOperation operation, BufferDBChangeParamList bulckChangeList) {
        return this.mICloudMessageManagerHelper.isBulkCreationEnabled() && handler != null && BaseSyncHandler.SyncOperation.UPLOAD.equals(operation) && !bulckChangeList.mChangelst.isEmpty();
    }

    private void resumeSingleHandler(Handler handler) {
        String str = TAG;
        Log.i(str, "resumeSingleHandler , isRunning: " + this.mIsRunning);
        if (handler == null) {
            return;
        }
        if (!this.mICloudMessageManagerHelper.shouldStopSendingAPIwhenNetworklost() || this.mIsRunning) {
            Message msg = Message.obtain();
            msg.what = OMASyncEventType.TRANSIT_TO_RESUME.getId();
            handler.sendMessage(msg);
        }
    }

    private void notifyOperationsComplete(BaseSyncHandler handler, BaseSyncHandler.SyncOperation operation, BufferDBChangeParam param, SyncMsgType curType) {
        String str = TAG;
        Log.i(str, "notifyOperationsComplete operation: " + operation);
        if (BaseSyncHandler.SyncOperation.DOWNLOAD.equals(operation)) {
            onMessageDownloadCompleted(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE).setSyncType(curType).setLine(param.mLine).build());
        } else if (BaseSyncHandler.SyncOperation.UPLOAD.equals(operation)) {
            onInitSyncCompleted(new ParamOMAresponseforBufDB.Builder().setLine(param.mLine).setSyncType(curType).setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE).setOMASyncEventType(OMASyncEventType.INITIAL_SYNC_COMPLETE).build());
            if (handler != null) {
                handler.setInitSyncComplete();
            }
        }
    }

    private void sendUploadToHandlers(BufferDBChangeParamList list) {
        sendToHandlerInternal(list, BaseSyncHandler.SyncOperation.UPLOAD);
    }

    public void sendAppSync(SyncParam param) {
        this.mSyncHandlerFactory.getSyncHandlerInstance(param).start();
    }

    public void sendAppSyncResetBox(SyncParam param) {
        BaseSyncHandler handler = this.mSyncHandlerFactory.getSyncHandlerInstance(param);
        handler.resetSearchParam();
        handler.start();
    }

    public void stopAppSync(SyncParam param) {
        this.mSyncHandlerFactory.getSyncHandlerInstance(param).stop();
    }

    public void sendUpdate(BufferDBChangeParamList param) {
        Message msg = obtainMessage(5);
        msg.obj = param;
        sendMessage(msg);
    }

    public void sendInitialSyncDownload(BufferDBChangeParamList param) {
        Message msg = obtainMessage(7);
        msg.obj = param;
        sendMessage(msg);
    }

    public void sendNormalSyncDownload(BufferDBChangeParamList param) {
        Message msg = obtainMessage(9);
        msg.obj = param;
        sendMessage(msg);
    }

    public void sendUpload(BufferDBChangeParamList param) {
        Message msg = obtainMessage(8);
        msg.obj = param;
        sendMessage(msg);
    }

    private void notifyBufferDB(ParamOMAresponseforBufDB param) {
        if (param == null) {
            Log.e(TAG, "notifyBufferDB ParamOMAresponseforBufDB is null");
        }
        this.mUpdateFromCloudRegistrants.notifyRegistrants(new AsyncResult((Object) null, param, (Throwable) null));
    }

    public void start() {
        sendEmptyMessage(1);
    }

    public void start_resetBox() {
        sendEmptyMessage(10);
    }

    public void pause() {
    }

    public void pausewithStatusParam(ParamNetAPIStatusControl param) {
        String str = TAG;
        Log.i(str, "pausewithStatusParam: " + param);
        Message msg = obtainMessage(11);
        msg.obj = param;
        sendMessage(msg);
    }

    public void resumewithStatusParam(ParamNetAPIStatusControl param) {
        String str = TAG;
        Log.i(str, "resumewithStatusParam: " + param);
        Message msg = obtainMessage(12);
        msg.obj = param;
        sendMessage(msg);
    }

    public void resume() {
    }

    public void stop() {
        sendEmptyMessage(4);
    }

    public void onInitialSyncStarted() {
        notifyBufferDB((ParamOMAresponseforBufDB) null);
        if (this.mICloudMessageManagerHelper.shouldStopInitSyncUponLowMemory()) {
            registerLowStorageReceiver();
        }
    }

    public void onDeviceFlagUpdateSchedulerStarted() {
        this.mIWorkingStatusProvisionListener.onDeviceFlagUpdateSchedulerStarted();
    }

    public void onInitSyncCompleted(ParamOMAresponseforBufDB param) {
        String str = TAG;
        Log.i(str, "onInitSyncCompleted getUserTbs: " + this.mICloudMessageManagerHelper.getUserTbs());
        if (this.mICloudMessageManagerHelper.getUserTbs()) {
            this.mIWorkingStatusProvisionListener.onInitialDBSyncCompleted();
        }
        notifyBufferDB(param);
        if (this.mICloudMessageManagerHelper.shouldStopInitSyncUponLowMemory()) {
            unregisterLowStorageReceiver();
        }
    }

    public void onInitSyncSummaryCompleted(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onCloudSyncStopped(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onSyncFailed(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
        if (this.mICloudMessageManagerHelper.shouldStopInitSyncUponLowMemory() && param.getOMASyncEventType() == OMASyncEventType.CANCEL_INITIAL_SYNC) {
            unregisterLowStorageReceiver();
        }
    }

    public void onMessageDownloadCompleted(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onMessageUploadCompleted(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onOneMessageDownloaded(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onOneMessageUploaded(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onCloudObjectNotificationUpdated(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onPartialSyncSummaryCompleted(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onNotificationObjectDownloaded(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onOmaAuthenticationFailed(ParamOMAresponseforBufDB param, long delayInMillis) {
        this.mIWorkingStatusProvisionListener.onOmaProvisionFailed(param, delayInMillis);
    }

    public void onOneDeviceFlagUpdated(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
    }

    public void onDeviceFlagUpdateCompleted(ParamOMAresponseforBufDB param) {
        notifyBufferDB(param);
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
        return false;
    }

    public void deleteNotificationSubscriptionResource() {
        if (this.mICloudMessageManagerHelper.isPollingAllowed()) {
            this.mChannelScheduler.update(OMASyncEventType.DELETE_NOTIFICATION_SUBSCRIPTION_RESOURCE.getId());
        }
    }

    public void onPauseCMNNetApi() {
        sendEmptyMessage(3);
    }

    public void onPauseCMNNetApiWithResumeDelay(int secs) {
        String str = TAG;
        Log.i(str, "pause all net API, resume all " + secs + " seconds later");
        removeMessages(2);
        sendEmptyMessage(3);
        sendEmptyMessageDelayed(2, ((long) secs) * 1000);
    }

    public void onFallbackToProvision(IControllerCommonInterface controller, IHttpAPICommonInterface request, int delaySecs) {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("check fallback to provision: ");
        sb.append(this.mIsFallbackProvisionInProcess ? "provision is in process, wait until next resume" : "");
        Log.i(str, sb.toString());
        onPauseCMNNetApi();
        if (!this.mIsFallbackProvisionInProcess) {
            this.mIsFallbackProvisionInProcess = true;
            this.mICloudMessageManagerHelper.onOmaApiCredentialFailed(controller, this, request, delaySecs);
        }
    }

    public void onOmaSuccess(IHttpAPICommonInterface request) {
        this.mICloudMessageManagerHelper.onOmaSuccess(request);
    }

    public void onOmaFailExceedMaxCount() {
        this.mIWorkingStatusProvisionListener.onOmaFailExceedMaxCount();
    }

    /* access modifiers changed from: protected */
    public void logWorkingStatus() {
        String str = TAG;
        Log.i(str, "logWorkingStatus: [mIsRunning: " + this.mIsRunning + " mIsFallbackProvisionInProcess: " + this.mIsFallbackProvisionInProcess + "]");
    }

    public void setOnApiSucceedOnceListener(OnApiSucceedOnceListener listener) {
    }

    public void updateSubscriptionChannel() {
        this.mChannelScheduler.updateDelay(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), 60000);
    }

    public void removeUpdateSubscriptionChannelEvent() {
        this.mChannelScheduler.update(OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId());
    }

    public void handleLargeDataPolling() {
        String channelUrl = this.mICloudMessageManagerHelper.getOMAChannelURL();
        String str = TAG;
        Log.d(str, "handleLargeDataPolling " + channelUrl);
        this.mChannelScheduler.updateMessage(obtainMessage(OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId(), channelUrl));
    }

    private void registerLowStorageReceiver() {
        Log.d(TAG, "registerLowStorageReceiver");
        if (this.mLowStorageReceiver == null) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.DEVICE_STORAGE_LOW");
            AnonymousClass3 r1 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    OMANetAPIHandler oMANetAPIHandler = OMANetAPIHandler.this;
                    oMANetAPIHandler.sendMessage(oMANetAPIHandler.obtainMessage(13));
                }
            };
            this.mLowStorageReceiver = r1;
            this.mContext.registerReceiver(r1, intentFilter);
        }
    }

    private void unregisterLowStorageReceiver() {
        BroadcastReceiver broadcastReceiver = this.mLowStorageReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mLowStorageReceiver = null;
        }
    }

    private void stopInitSyncAsComplete() {
        String line = this.mICloudMessageManagerHelper.getUserTelCtn();
        onInitSyncCompleted(new ParamOMAresponseforBufDB.Builder().setLine(line).setSyncType(SyncMsgType.DEFAULT).setActionType(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE).setOMASyncEventType(OMASyncEventType.INITIAL_SYNC_COMPLETE).build());
        BaseSyncHandler handler = this.mSyncHandlerFactory.getSyncHandlerInstance(new SyncParam(line, SyncMsgType.DEFAULT));
        if (handler != null) {
            handler.setInitSyncComplete();
        }
    }

    public void resetChannelState() {
        this.mChannelScheduler.update(OMASyncEventType.RESET_STATE.getId());
    }
}
