package com.sec.internal.ims.cmstore.cloudmessagebuffer;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.cmstore.helper.SyncParam;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceSessionPartcptsUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValueList;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.strategy.DefaultCloudMessageStrategy;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageBufferEvent;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.GCMPushNotification;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;
import java.util.Iterator;

public class CloudMessageBufferSchedulingHandler extends CloudMessageBufferDBHelper implements ICloudMessageBufferEvent {
    private static final String TAG = CloudMessageBufferSchedulingHandler.class.getSimpleName();
    private ICloudMessageManagerHelper mICloudMessageManagerHelper;

    public CloudMessageBufferSchedulingHandler(Looper looper, Context context, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(looper, context, deviceDataListener, callback);
        Log.d(TAG, "onCreate");
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        registerRegistrants();
    }

    private void registerRegistrants() {
        Log.d(TAG, "registerRegistrants()");
        this.mDeviceDataChangeListener.registerForUpdateFromCloud(this, 3, (Object) null);
        this.mDeviceDataChangeListener.registerForUpdateOfWorkingStatus(this, 4, (Object) null);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        String str = TAG;
        Log.i(str, "message: " + msg.what);
        switch (msg.what) {
            case 1:
                startInitialSyncDBCopyTask();
                return;
            case 3:
                onUpdateFromCloud((ParamOMAresponseforBufDB) ((AsyncResult) msg.obj).result);
                return;
            case 4:
                onWorkingStatusChanged((IWorkingStatusProvisionListener.WorkingStatus) ((AsyncResult) msg.obj).result);
                return;
            case 6:
                onUpdateFromDeviceLegacy();
                return;
            case 7:
                DeviceSessionPartcptsUpdateParam updateParam = (DeviceSessionPartcptsUpdateParam) msg.obj;
                if (updateParam.mTableindex == 2) {
                    this.mRcsScheduler.onUpdateFromDeviceSessionPartcpts(updateParam);
                    return;
                } else if (updateParam.mTableindex == 10) {
                    this.mRcsScheduler.onUpdateFromDeviceSession(updateParam);
                    return;
                } else {
                    return;
                }
            case 8:
                onUpdateFromDeviceMsgAppFetch((DeviceMsgAppFetchUpdateParam) msg.obj);
                return;
            case 11:
                handleRCSDbReady();
                return;
            case 12:
                onLineActivated((String) msg.obj);
                return;
            case 13:
                onLineDeactivated((String) msg.obj);
                return;
            case 14:
                onServiceRestarted();
                return;
            case 15:
                handleReceivedMessageJson((String) msg.obj);
                return;
            case 16:
                handleSentMessageJson((String) msg.obj);
                return;
            case 17:
                handleReadMessageJson((String) msg.obj);
                return;
            case 18:
                handleUnReadMessageJson((String) msg.obj);
                return;
            case 19:
                handleDeleteMessageJson((String) msg.obj);
                return;
            case 20:
                handleUploadMessageJson((String) msg.obj);
                return;
            case 21:
                handleDownloadMessageJson((String) msg.obj);
                return;
            case 22:
                handleWipeOutMessageJson((String) msg.obj);
                return;
            case 24:
                handleBufferDbReadMessageJson((String) msg.obj);
                return;
            case 25:
                onBufferDBReadBatch((String) msg.obj);
                return;
            case 26:
                handleReceivedGcm((GCMPushNotification) msg.obj);
                return;
            case 27:
                handleStartFullSync((ParamAppJsonValueList) msg.obj);
                return;
            case 28:
                handleStopSync((ParamAppJsonValueList) msg.obj);
                return;
            case 29:
                onUpdateFromDeviceMsgAppFetchFailed((DeviceMsgAppFetchUpdateParam) msg.obj);
                return;
            case 30:
                appFetchingFailedMsg(String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.FetchingFail.getId()));
                return;
            case 31:
                fetchingPendingMsg();
                return;
            default:
                return;
        }
    }

    private void onServiceRestarted() {
        this.mProvisionSuccess = false;
        setBufferDBLoaded(false);
    }

    private void handleStartFullSync(ParamAppJsonValueList list) {
        String str = TAG;
        IMSLog.s(str, "handleStartFullSync: " + list);
        if (list != null) {
            Iterator<ParamAppJsonValue> it = list.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue value = it.next();
                String line = value.mLine;
                String appType = value.mAppType;
                String dataType = value.mDataType;
                if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(appType) && CloudMessageProviderContract.DataTypes.MSGAPP_ALL.equalsIgnoreCase(dataType)) {
                    this.mMultiLnScheduler.insertNewLine(line, SyncMsgType.MESSAGE);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(line, SyncMsgType.MESSAGE));
                } else if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(appType) && "FAX".equalsIgnoreCase(dataType)) {
                    this.mMultiLnScheduler.insertNewLine(line, SyncMsgType.FAX);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(line, SyncMsgType.FAX));
                } else if ("CALLLOGDATA".equalsIgnoreCase(appType)) {
                    this.mMultiLnScheduler.insertNewLine(line, SyncMsgType.CALLLOG);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(line, SyncMsgType.CALLLOG));
                } else if ("VVMDATA".equalsIgnoreCase(appType) && "VVMDATA".equalsIgnoreCase(dataType)) {
                    this.mMultiLnScheduler.insertNewLine(line, SyncMsgType.VM);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(line, SyncMsgType.VM));
                } else if ("VVMDATA".equalsIgnoreCase(appType) && CloudMessageProviderContract.DataTypes.VVMGREETING.equalsIgnoreCase(dataType)) {
                    this.mVVMScheduler.wipeOutData(18, line);
                    this.mDeviceDataChangeListener.sendAppSync(new SyncParam(line, SyncMsgType.VM_GREETINGS));
                }
            }
        }
    }

    private void handleStopSync(ParamAppJsonValueList list) {
        String str = TAG;
        IMSLog.s(str, "handleStopSync: " + list);
        if (list != null) {
            Iterator<ParamAppJsonValue> it = list.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue value = it.next();
                String line = value.mLine;
                String type = value.mAppType;
                if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(type) || CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(type)) {
                    this.mMultiLnScheduler.deleteLine(line, SyncMsgType.MESSAGE);
                    this.mMultiLnScheduler.deleteLine(line, SyncMsgType.FAX);
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(line, SyncMsgType.MESSAGE));
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(line, SyncMsgType.FAX));
                } else if ("CALLLOGDATA".equalsIgnoreCase(type)) {
                    this.mMultiLnScheduler.deleteLine(line, SyncMsgType.CALLLOG);
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(line, SyncMsgType.CALLLOG));
                } else if ("VVMDATA".equalsIgnoreCase(type)) {
                    this.mMultiLnScheduler.deleteLine(line, SyncMsgType.VM);
                    this.mDeviceDataChangeListener.stopAppSync(new SyncParam(line, SyncMsgType.VM));
                }
            }
        }
    }

    private void handleReceivedGcm(GCMPushNotification pushnotification) {
        if (pushnotification != null) {
            String line = null;
            try {
                line = Util.getTelUri(pushnotification.recipients[0].uri);
                String str = TAG;
                IMSLog.s(str, "pushnotification: " + IMSLog.checker(line));
            } catch (NullPointerException e) {
                String str2 = TAG;
                Log.e(str2, "nullpointer: " + e.getMessage());
            }
            if (line != null) {
                if (!TMOConstants.TmoGcmPnsVariables.NOTIFY.equalsIgnoreCase(pushnotification.pnsType) || !TMOConstants.TmoGcmPnsVariables.FULL_SYNC.equalsIgnoreCase(pushnotification.pnsSubtype)) {
                    DefaultCloudMessageStrategy.NmsNotificationType dataContract = CloudMessageStrategyManager.getStrategy().makeParamNotificationType(pushnotification.pnsType, pushnotification.pnsSubtype);
                    if (dataContract != null) {
                        String dataType = dataContract.getDataType();
                        ParamNmsNotificationList paramNmsNotificationList = new ParamNmsNotificationList(dataType, dataContract.getContractType(), line, pushnotification);
                        if (dataType.equals("SMS")) {
                            this.mSmsScheduler.onNotificationReceived(paramNmsNotificationList);
                        } else if (dataType.equals("MMS")) {
                            this.mMmsScheduler.onNotificationReceived(paramNmsNotificationList);
                        } else if (dataType.equals("FAX")) {
                            this.mFaxScheduler.onNotificationReceived(paramNmsNotificationList);
                        } else if (dataType.equals("CALLLOGDATA")) {
                            this.mCallLogScheduler.onNotificationReceived(paramNmsNotificationList);
                        } else if (dataType.equals(CloudMessageProviderContract.DataTypes.CHAT) || dataType.equals("FT") || dataType.equals("IMDN")) {
                            this.mRcsScheduler.onNotificationReceived(paramNmsNotificationList);
                        } else if (dataType.equals("GSO")) {
                            this.mRcsScheduler.onNotificationGSOReceived(paramNmsNotificationList);
                        } else {
                            this.mVVMScheduler.onNotificationReceived(paramNmsNotificationList);
                        }
                    }
                } else {
                    handleNotifyObject(pushnotification, line);
                }
            }
        }
    }

    private void handleNotifyObject(GCMPushNotification notification, String line) {
        if (notification.nmsEventList != null && notification.nmsEventList.nmsEvent != null) {
            for (NmsEvent event : notification.nmsEventList.nmsEvent) {
                if (!(event.notifyObject == null || event.notifyObject.extendedMessage == null || event.notifyObject.extendedMessage.folderURL == null)) {
                    String folderId = event.notifyObject.extendedMessage.folderURL.toString();
                    Log.d(TAG, "handleNotifyObject: notifyObject, folderID:" + folderId);
                    if (folderId.contains(TMOVariables.TmoMessageFolderId.mVVMailGreeting)) {
                        this.mVVMScheduler.onNotifyObjectReceived(line);
                    }
                }
            }
        }
    }

    private void onWorkingStatusChanged(IWorkingStatusProvisionListener.WorkingStatus status) {
        String str = TAG;
        Log.i(str, "onWorkingStatusChanged: " + status);
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[status.ordinal()];
        if (i == 1) {
            handleProvisionSuccess();
        } else if (i == 3) {
            onSendCloudUnSyncedUpdate();
        } else if (i == 5) {
            handleDftMsgAppChangedToNative();
        } else if (i == 6) {
            restartService();
        } else if (i == 7) {
            cleanAllBufferDB();
        } else if (i == 8) {
            onMailBoxReset();
        }
    }

    private void handleDftMsgAppChangedToNative() {
        int initSyncStatus = this.mMultiLnScheduler.getLineInitSyncStatus(CloudMessagePreferenceManager.getInstance().getUserTelCtn(), SyncMsgType.DEFAULT);
        String str = TAG;
        Log.d(str, "handleDftMsgAppChangedToNative initSyncStatus: " + initSyncStatus);
        if (initSyncStatus == OMASyncEventType.INITIAL_SYNC_COMPLETE.getId()) {
            this.mIsGoforwardSync = true;
            startGoForwardSyncDbCopyTask();
        }
    }

    private void handleProvisionSuccess() {
        this.mProvisionSuccess = true;
        String linenum = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
        int initSyncStatus = this.mMultiLnScheduler.getLineInitSyncStatus(linenum, SyncMsgType.DEFAULT);
        OMASyncEventType event = OMASyncEventType.valueOf(initSyncStatus);
        String str = TAG;
        Log.i(str, "check initial sync status: " + initSyncStatus + "event: " + event + " linenum:" + IMSLog.checker(linenum));
        if (event == null) {
            event = OMASyncEventType.DEFAULT;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[event.ordinal()];
        if (i == 1) {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
            onSendCloudUnSyncedUpdate();
            onSendDeviceUnSyncedUpdate();
            this.mBufferDBloaded = CloudMessagePreferenceManager.getInstance().getBufferDbLoaded();
        } else if (i == 2) {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
            onSendUnDownloadedMessage(linenum, SyncMsgType.DEFAULT, false);
            this.mBufferDBloaded = CloudMessagePreferenceManager.getInstance().getBufferDbLoaded();
        } else if (i == 3 || i == 4) {
            startInitialDBCopy();
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
        } else {
            this.mDeviceDataChangeListener.onInitialDBCopyDone();
        }
    }

    private void handleRCSDbReady() {
        this.mRCSDbReady = true;
        startInitialDBCopy();
    }

    private void onUpdateFromDeviceLegacy() {
    }

    private void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para) {
        int i = para.mTableindex;
        if (!(i == 1 || i == 14)) {
            if (i == 3) {
                this.mSmsScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
                return;
            } else if (i == 4) {
                this.mMmsScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
                return;
            } else if (!(i == 11 || i == 12)) {
                switch (i) {
                    case 16:
                        this.mCallLogScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
                        return;
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        this.mVVMScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
                        return;
                    case 21:
                        this.mFaxScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
                        return;
                    default:
                        return;
                }
            }
        }
        this.mRcsScheduler.onUpdateFromDeviceMsgAppFetch(para, this.mIsGoforwardSync);
    }

    private void onUpdateFromDeviceMsgAppFetchFailed(DeviceMsgAppFetchUpdateParam para) {
        String str = TAG;
        Log.d(str, "onUpdateFromDeviceMsgAppFetchFailed " + para);
        int i = para.mTableindex;
        if (!(i == 1 || i == 14)) {
            if (i == 3) {
                this.mSmsScheduler.onUpdateFromDeviceMsgAppFetchFailed(para);
                return;
            } else if (i == 4) {
                this.mMmsScheduler.onUpdateFromDeviceMsgAppFetchFailed(para);
                return;
            } else if (!(i == 11 || i == 12)) {
                return;
            }
        }
        this.mRcsScheduler.onUpdateFromDeviceMsgAppFetchFailed(para);
    }

    private void handleSearchObject(ParamOMAresponseforBufDB para, boolean isGoforwardSync) {
        this.mMultiLnScheduler.updateLineInitsyncStatus(para.getLine(), para.getSyncMsgType(), para.getSearchCursor(), para.getOMASyncEventType().getId());
        if (SyncMsgType.DEFAULT.equals(para.getSyncMsgType()) || SyncMsgType.MESSAGE.equals(para.getSyncMsgType())) {
            this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, para.getLine(), CloudMessageBufferDBConstants.InitialSyncStatusFlag.START);
        } else if (SyncMsgType.FAX.equals(para.getSyncMsgType())) {
            this.mFaxScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", para.getLine(), CloudMessageBufferDBConstants.InitialSyncStatusFlag.START);
        }
        ObjectList olst = para.getObjectList();
        if (!(olst == null || olst.object == null)) {
            for (Object objt : olst.object) {
                ParamOMAObject param = new ParamOMAObject(objt, false, -1, this.mICloudMessageManagerHelper);
                if (param.mObjectType != -1) {
                    Log.d(TAG, "param.mObjectType: " + param.mObjectType);
                    int i = param.mObjectType;
                    if (i == 3) {
                        this.mSmsScheduler.handleObjectSMSCloudSearch(param);
                    } else if (i == 4) {
                        this.mMmsScheduler.handleObjectMMSCloudSearch(param);
                    } else if (i == 21) {
                        this.mFaxScheduler.handleObjectFaxMessageCloudSearch(param, isGoforwardSync);
                    } else if (i != 34) {
                        switch (i) {
                            case 11:
                            case 12:
                            case 14:
                                if (CloudMessageStrategyManager.getStrategy().shouldSkipMessage(param)) {
                                    break;
                                } else {
                                    this.mRcsScheduler.handleObjectRCSMessageCloudSearch(param, isGoforwardSync);
                                    break;
                                }
                            case 13:
                                this.mRcsScheduler.handleObjectRCSIMDNCloudSearch(param);
                                break;
                            default:
                                switch (i) {
                                    case 16:
                                        this.mCallLogScheduler.handleObjectCallLogMessageCloudSearch(param);
                                        break;
                                    case 17:
                                        this.mVVMScheduler.handleObjectVvmMessageCloudSearch(param, isGoforwardSync);
                                        break;
                                    case 18:
                                        this.mVVMScheduler.handleObjectVvmGreetingCloudSearch(param);
                                        break;
                                }
                        }
                    } else {
                        this.mRcsScheduler.handleCloudNotifyGSOChangedObj(param, objt);
                    }
                }
            }
        }
        if (para.getActionType().equals(ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE)) {
            onSendUnDownloadedMessage(para.getLine(), para.getSyncMsgType(), false);
        }
    }

    private void handleDownloadedPayload(ParamOMAresponseforBufDB para, boolean isInitialSync) {
        if (para.getBufferDBChangeParam() != null) {
            int i = para.getBufferDBChangeParam().mDBIndex;
            if (i == 1) {
                this.mRcsScheduler.onRcsPayloadDownloaded(para, false);
            } else if (i == 6) {
                this.mMmsScheduler.onMmsPayloadDownloaded(para, false);
            }
        }
    }

    private void handleDownloadedAllPayloads(ParamOMAresponseforBufDB para, boolean isInitialSync) {
        if (para.getBufferDBChangeParam() != null) {
            int i = para.getBufferDBChangeParam().mDBIndex;
            if (i == 1) {
                this.mRcsScheduler.onRcsAllPayloadsDownloaded(para, false);
            } else if (i == 21) {
                this.mFaxScheduler.onFaxAllPayloadDownloaded(para, false);
            } else if (i == 17) {
                this.mVVMScheduler.onVvmAllPayloadDownloaded(para, false);
            } else if (i == 18) {
                this.mVVMScheduler.onGreetingAllPayloadDownloaded(para, false);
            }
        }
    }

    private void onUpdateFromCloud(ParamOMAresponseforBufDB para) {
        String str = TAG;
        Log.i(str, "onUpdateFromCloud: " + para + " mIsGoforwardSync:" + this.mIsGoforwardSync);
        if (para != null && para.getActionType() != null) {
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[para.getActionType().ordinal()]) {
                case 1:
                    onInitialSyncComplete(true, para.getLine(), para.getSyncMsgType(), para);
                    return;
                case 2:
                case 3:
                    handleSearchObject(para, false);
                    return;
                case 5:
                    onInitialSyncComplete(false, para.getLine(), para.getSyncMsgType(), para);
                    return;
                case 7:
                    handleDownloadedPayload(para, true);
                    return;
                case 8:
                    handleDownloadedAllPayloads(para, true);
                    return;
                case 9:
                    notifyNetAPIUploadMessages(para.getLine(), para.getSyncMsgType(), false);
                    return;
                case 10:
                    onCloudUploadSuccess(para);
                    return;
                case 11:
                    this.mBufferDBChangeNetAPI.mChangelst.clear();
                    return;
                case 12:
                    onCloudNormalSyncObjectDownload(para, false);
                    return;
                case 13:
                    handleDownloadedPayload(para, false);
                    return;
                case 14:
                    handleDownloadedAllPayloads(para, false);
                    return;
                case 16:
                    onMailBoxReset();
                    return;
                case 18:
                case 19:
                    onCloudUpdateFlagSuccess(para);
                    return;
                case 20:
                    onCloudNotificationReceivedUnknownType(para);
                    return;
                case 21:
                    onDownloadFailure(para);
                    return;
                case 22:
                    onUpLoadFailureHandling(para);
                    return;
                case 23:
                    onCloudDeleteObjectFailed(para);
                    return;
                case 25:
                    this.mVVMScheduler.handleVvmProfileDownloaded(para);
                    return;
                case 26:
                    onBulkFlagUpdateComplete(para);
                    return;
                case 27:
                    onBulkCreationComplete(para);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus;

        static {
            int[] iArr = new int[ParamOMAresponseforBufDB.ActionType.values().length];
            $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType = iArr;
            try {
                iArr[ParamOMAresponseforBufDB.ActionType.INIT_SYNC_COMPLETE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.INIT_SYNC_SUMMARY_COMPLETE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.INIT_SYNC_PARTIAL_SYNC_SUMMARY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.MATCH_DB.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.SYNC_FAILED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_DOWNLOAD.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.ONE_PAYLOAD_DOWNLOAD.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.ALL_PAYLOAD_DOWNLOAD.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.MESSAGE_DOWNLOAD_COMPLETE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.MESSAGE_UPLOAD_COMPLETE.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECT_DOWNLOADED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.NOTIFICATION_PAYLOAD_DOWNLOADED.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.NOTIFICATION_ALL_PAYLOAD_DOWNLOADED.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECTS_DOWNLOAD_COMPLETE.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.CLOUD_OBJECT_UPDATE.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_FLAG_UPDATED.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND.ordinal()] = 21;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.VVM_FAX_ERROR_WITH_NO_RETRY.ordinal()] = 22;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_DELETE_UPDATE_FAILED.ordinal()] = 23;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_READ_UPDATE_FAILED.ordinal()] = 24;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.VVM_PROFILE_DOWNLOADED.ordinal()] = 25;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_BULK_UPDATE_COMPLETE.ordinal()] = 26;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$cmstore$params$ParamOMAresponseforBufDB$ActionType[ParamOMAresponseforBufDB.ActionType.BULK_MESSAGES_UPLOADED.ordinal()] = 27;
            } catch (NoSuchFieldError e27) {
            }
            int[] iArr2 = new int[OMASyncEventType.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType = iArr2;
            try {
                iArr2[OMASyncEventType.INITIAL_SYNC_COMPLETE.ordinal()] = 1;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.INITIAL_SYNC_SUMMARY_COMPLETE.ordinal()] = 2;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.START_INITIAL_SYNC.ordinal()] = 3;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$omanetapi$OMASyncEventType[OMASyncEventType.DEFAULT.ordinal()] = 4;
            } catch (NoSuchFieldError e31) {
            }
            int[] iArr3 = new int[IWorkingStatusProvisionListener.WorkingStatus.values().length];
            $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus = iArr3;
            try {
                iArr3[IWorkingStatusProvisionListener.WorkingStatus.PROVISION_SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.OMA_PROVISION_FAILED.ordinal()] = 2;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.SEND_TOCLOUD_UNSYNC.ordinal()] = 3;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.NET_WORK_STATUS_CHANGED.ordinal()] = 4;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.DEFAULT_MSGAPP_CHGTO_NATIVE.ordinal()] = 5;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.RESTART_SERVICE.ordinal()] = 6;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.BUFFERDB_CLEAN.ordinal()] = 7;
            } catch (NoSuchFieldError e38) {
            }
            try {
                $SwitchMap$com$sec$internal$interfaces$ims$cmstore$IWorkingStatusProvisionListener$WorkingStatus[IWorkingStatusProvisionListener.WorkingStatus.MAILBOX_MIGRATION_RESET.ordinal()] = 8;
            } catch (NoSuchFieldError e39) {
            }
        }
    }

    private void onCloudDeleteObjectFailed(ParamOMAresponseforBufDB para) {
        if (para != null && para.getBufferDBChangeParam() != null && CloudMessageStrategyManager.getStrategy().isNotifyAppOnUpdateCloudFail()) {
            int i = para.getBufferDBChangeParam().mDBIndex;
            if (i == 1) {
                this.mRcsScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            } else if (i == 21) {
                this.mFaxScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            } else if (i == 3) {
                this.mSmsScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            } else if (i == 4) {
                this.mMmsScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            } else if (i == 16) {
                this.mCallLogScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            } else if (i == 17) {
                this.mVVMScheduler.notifyMsgAppDeleteFail(para.getBufferDBChangeParam().mDBIndex, para.getBufferDBChangeParam().mRowId, para.getBufferDBChangeParam().mLine);
            }
        }
    }

    private void onBulkFlagUpdateComplete(ParamOMAresponseforBufDB para) {
        if (para.getBulkResponseList() == null || para.getBulkResponseList().response == null) {
            Log.e(TAG, "onBulkFlagUpdateComplete: invalid return results");
            return;
        }
        for (int i = 0; i < para.getBulkResponseList().response.length; i++) {
            if (para.getBulkResponseList().response[i].success != null && para.getBulkResponseList().response[i].success.resourceURL != null) {
                handleBulkOpSingleUrlSuccess(para.getBulkResponseList().response[i].success.resourceURL.toString());
            } else if (!(para.getBulkResponseList().response[i].failure == null || !CloudMessageStrategyManager.getStrategy().bulkOpTreatSuccessIndividualResponse(para.getBulkResponseList().response[i].code) || para.getBulkResponseList().response[i].failure == null || para.getBulkResponseList().response[i].failure.serviceException == null || para.getBulkResponseList().response[i].failure.serviceException.variables == null)) {
                handleBulkOpSingleUrlSuccess(para.getBulkResponseList().response[i].failure.serviceException.variables[0]);
            }
        }
    }

    private void onUpLoadFailureHandling(ParamOMAresponseforBufDB para) {
        if (para.getBufferDBChangeParam() != null) {
            if (para.getBufferDBChangeParam().mDBIndex == 21) {
                this.mFaxScheduler.handleSendFaxMessageResponse(para, false);
            } else if (para.getBufferDBChangeParam().mDBIndex == 18 || para.getBufferDBChangeParam().mDBIndex == 19 || para.getBufferDBChangeParam().mDBIndex == 20) {
                this.mVVMScheduler.handleUpdateVVMResponse(para, false);
            }
        }
    }

    private void onDownloadFailure(ParamOMAresponseforBufDB para) {
        if (para.getBufferDBChangeParam() != null) {
            if (para.getBufferDBChangeParam().mDBIndex == 17 || para.getBufferDBChangeParam().mDBIndex == 18) {
                this.mVVMScheduler.handleDownLoadMessageResponse(para, false);
            } else if (para.getBufferDBChangeParam().mDBIndex == 1) {
                this.mRcsScheduler.handleDownLoadMessageResponse(para, false);
            }
        }
    }

    private void onMailBoxReset() {
        cleanAllBufferDB();
        startInitialSyncDBCopyTask();
        this.mDeviceDataChangeListener.onMailBoxResetBufferDbDone();
    }

    private void onCloudNormalSyncObjectDownload(ParamOMAresponseforBufDB para, boolean isGoforwardSync) {
        if (para.getBufferDBChangeParam() != null) {
            ParamOMAObject paramOMAObj = new ParamOMAObject(para.getObject(), para.getBufferDBChangeParam().mIsGoforwardSync, para.getBufferDBChangeParam().mDBIndex, this.mICloudMessageManagerHelper);
            if (paramOMAObj.mObjectType != -1 || para.getBufferDBChangeParam().mDBIndex == 7) {
                this.mSummaryQuery.insertSummaryDbUsingObjectIfNonExist(paramOMAObj, paramOMAObj.mObjectType);
                int i = paramOMAObj.mObjectType;
                if (i != 1) {
                    if (i == 21) {
                        this.mFaxScheduler.handleNormalSyncDownloadedFaxMessage(paramOMAObj);
                        return;
                    } else if (i == 34) {
                        this.mRcsScheduler.handleCloudNotifyGSOChangedObj(paramOMAObj, para.getObject());
                        return;
                    } else if (i == 3) {
                        this.mSmsScheduler.handleNormalSyncObjectSmsDownload(paramOMAObj);
                        return;
                    } else if (i != 4) {
                        switch (i) {
                            case 11:
                            case 12:
                            case 14:
                                break;
                            case 13:
                                this.mRcsScheduler.handleNormalSyncObjectRcsImdnDownload(paramOMAObj);
                                return;
                            default:
                                switch (i) {
                                    case 16:
                                        this.mCallLogScheduler.handleNormalSyncDownloadedCallLog(paramOMAObj);
                                        return;
                                    case 17:
                                        this.mVVMScheduler.handleNormalSyncDownloadedVVMMessage(paramOMAObj);
                                        return;
                                    case 18:
                                        this.mVVMScheduler.handleNormalSyncDownloadedVVMGreeting(paramOMAObj);
                                        return;
                                    default:
                                        return;
                                }
                        }
                    } else {
                        this.mMmsScheduler.handleNormalSyncObjectMmsDownload(paramOMAObj, isGoforwardSync);
                        return;
                    }
                }
                this.mRcsScheduler.handleNormalSyncObjectRcsMessageDownload(paramOMAObj, isGoforwardSync);
            }
        }
    }

    private void onCloudNotificationReceivedUnknownType(ParamOMAresponseforBufDB para) {
        String str;
        NotificationList[] list = para.getNotificationList();
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (list == null) {
            this.mIsGoforwardSync = false;
            return;
        }
        boolean shouldSkipDeletedObjt = CloudMessageStrategyManager.getStrategy().isGoForwardSyncSupported() && this.mIsGoforwardSync;
        for (NotificationList notificationList : list) {
            NmsEventList nmsEvents = notificationList.nmsEventList;
            if (!(nmsEvents == null || nmsEvents.nmsEvent == null)) {
                for (NmsEvent event : nmsEvents.nmsEvent) {
                    String str2 = TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("onCloudNotificationReceivedUnknownType, ChangedObj:");
                    String str3 = null;
                    sb.append(event.changedObject == null ? null : "not null");
                    sb.append(" DeletedObj:");
                    if (event.deletedObject == null) {
                        str = null;
                    } else {
                        str = "not null";
                    }
                    sb.append(str);
                    sb.append(" ExpiredObj:");
                    if (event.expiredObject != null) {
                        str3 = "not null";
                    }
                    sb.append(str3);
                    sb.append(" shouldSkipDeletedObjt:");
                    sb.append(shouldSkipDeletedObjt);
                    sb.append(" mIsGoforwardSync:");
                    sb.append(this.mIsGoforwardSync);
                    Log.i(str2, sb.toString());
                    if (event.changedObject != null) {
                        handleCloudNotifyChangedObj(event.changedObject, downloadlist, this.mIsGoforwardSync);
                    }
                    if (event.deletedObject != null && !shouldSkipDeletedObjt) {
                        handleCloudNotifyDeletedObj(event.deletedObject, false);
                    }
                    if (event.expiredObject != null) {
                        handleExpiredObject(event.expiredObject);
                    }
                }
            }
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
        }
        if (this.mIsGoforwardSync) {
            onSendCloudUnSyncedUpdate();
            this.mIsGoforwardSync = false;
        }
    }

    private void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para) {
        if (para.getBufferDBChangeParam() != null) {
            int i = para.getBufferDBChangeParam().mDBIndex;
            if (!(i == 1 || i == 14)) {
                if (i == 21) {
                    this.mFaxScheduler.onCloudUpdateFlagSuccess(para, false);
                    return;
                } else if (i == 3) {
                    this.mSmsScheduler.onCloudUpdateFlagSuccess(para, false);
                    return;
                } else if (i == 4) {
                    this.mMmsScheduler.onCloudUpdateFlagSuccess(para, false);
                    return;
                } else if (!(i == 11 || i == 12)) {
                    if (i == 16) {
                        this.mCallLogScheduler.onCloudUpdateFlagSuccess(para, false);
                        return;
                    } else if (i == 17) {
                        this.mVVMScheduler.onCloudUpdateFlagSuccess(para, false);
                        return;
                    } else {
                        return;
                    }
                }
            }
            this.mRcsScheduler.onCloudUpdateFlagSuccess(para, false);
        }
    }

    private void onCloudUploadSuccess(ParamOMAresponseforBufDB para) {
        if (para.getBufferDBChangeParam() != null) {
            if (para.getReference() != null) {
                int i = para.getBufferDBChangeParam().mDBIndex;
                if (!(i == 1 || i == 14)) {
                    if (i == 3) {
                        this.mSmsScheduler.onCloudUploadSuccess(para, false);
                        return;
                    } else if (i == 4) {
                        this.mMmsScheduler.onCloudUploadSuccess(para, false);
                        return;
                    } else if (!(i == 11 || i == 12)) {
                        switch (i) {
                            case 18:
                            case 19:
                            case 20:
                                this.mVVMScheduler.handleUpdateVVMResponse(para, true);
                                return;
                            case 21:
                                this.mFaxScheduler.handleSendFaxMessageResponse(para, true);
                                return;
                            default:
                                return;
                        }
                    }
                }
                this.mRcsScheduler.onCloudUploadSuccess(para, false);
                return;
            }
            switch (para.getBufferDBChangeParam().mDBIndex) {
                case 18:
                case 19:
                case 20:
                    this.mVVMScheduler.handleUpdateVVMResponse(para, true);
                    return;
                case 21:
                    this.mFaxScheduler.handleSendFaxMessageResponse(para, false);
                    return;
                default:
                    return;
            }
        }
    }

    private void onInitialSyncComplete(boolean isSuccess, String line, SyncMsgType syncType, ParamOMAresponseforBufDB para) {
        if (line != null) {
            if (isSuccess) {
                this.mMultiLnScheduler.updateLineInitsyncStatus(line, para.getSyncMsgType(), para.getSearchCursor(), para.getOMASyncEventType().getId());
            }
            if (SyncMsgType.DEFAULT.equals(para.getSyncMsgType()) || SyncMsgType.MESSAGE.equals(para.getSyncMsgType())) {
                if (isSuccess) {
                    this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, line, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED);
                } else {
                    this.mSmsScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.MSGAPP_ALL, line, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL);
                }
            } else if (SyncMsgType.FAX.equals(para.getSyncMsgType())) {
                if (isSuccess) {
                    this.mFaxScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", line, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED);
                } else {
                    this.mFaxScheduler.notifyInitialSyncStatus(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "FAX", line, CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL);
                }
            }
            onHandlePendingNmsEvent();
            if (CloudMessageStrategyManager.getStrategy().getIsInitSyncIndicatorRequired()) {
                Log.i(TAG, "Send a to init sync termial flag(RowId = -1) to messaging app");
                this.mSmsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "SMS", -1);
            }
        }
    }

    private void startInitialDBCopy() {
        this.mBufferDBloaded = CloudMessagePreferenceManager.getInstance().getBufferDbLoaded();
        String str = TAG;
        Log.d(str, "startInitialDBCopy(), mProvisionSuccess: " + this.mProvisionSuccess + ", mRCSDbReady: " + this.mRCSDbReady + ", mBufferDBloaded: " + this.mBufferDBloaded);
        if (this.mRCSDbReady && !this.mBufferDBloaded && this.mProvisionSuccess) {
            onLineActivated(CloudMessagePreferenceManager.getInstance().getUserTelCtn());
            sendMessage(obtainMessage(1, (Object) null));
        }
    }

    private void onLineActivated(String teluri) {
    }

    private void onLineDeactivated(String teluri) {
    }

    private void restartService() {
        sendMessage(obtainMessage(14, (Object) null));
    }

    public void onRCSDbReady() {
        Log.d(TAG, "onRCSDbReady()");
        sendMessage(obtainMessage(11, (Object) null));
    }

    private void onBufferDBReadBatch(String json) {
    }

    public void onBufferDBReadResult(String type, String bufferRowIDs, String telephonyRowId, int syncAction, boolean isSuccess) {
        String telephonyRowId2;
        DeviceMsgAppFetchUpdateParam para;
        String str = type;
        boolean z = isSuccess;
        String str2 = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onBufferDBReadResult ");
        sb.append(str);
        sb.append(" ");
        sb.append(bufferRowIDs);
        sb.append(" ");
        String str3 = telephonyRowId;
        sb.append(str3);
        sb.append(" ");
        sb.append(z);
        Log.d(str2, sb.toString());
        CloudMessageBufferDBConstants.ActionStatusFlag msgResponse = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(syncAction);
        if (msgResponse == null) {
            String str4 = TAG;
            Log.e(str4, "illegal syncAction: " + syncAction);
        } else {
            int i = syncAction;
        }
        if (!z) {
            telephonyRowId2 = "0";
        } else {
            telephonyRowId2 = str3;
        }
        try {
            if ("MMS".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(4, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if ("SMS".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(3, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if ("FT".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(1, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), true);
            } else if (CloudMessageProviderContract.DataTypes.CHAT.equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(1, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if ("FAX".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(21, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if ("CALLLOGDATA".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(16, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if ("VVMDATA".equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(17, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else if (CloudMessageProviderContract.DataTypes.VVMGREETING.equalsIgnoreCase(str)) {
                para = new DeviceMsgAppFetchUpdateParam(18, msgResponse, (long) Integer.valueOf(bufferRowIDs).intValue(), (long) Integer.valueOf(telephonyRowId2).intValue(), false);
            } else {
                String str5 = TAG;
                Log.d(str5, "onBufferDBReadResult wrong input type: " + str);
                return;
            }
            if (z) {
                sendMessage(obtainMessage(8, para));
            } else {
                sendMessage(obtainMessage(29, para));
            }
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void createSession(String chatId) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(10, CloudMessageBufferDBConstants.ActionStatusFlag.Insert, chatId)));
    }

    public void createParticipant(String chatId) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(2, CloudMessageBufferDBConstants.ActionStatusFlag.Insert, chatId)));
    }

    public void deleteSession(String chatId) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(10, CloudMessageBufferDBConstants.ActionStatusFlag.Delete, chatId)));
    }

    public void deleteParticipant(String chatId) {
        sendMessage(obtainMessage(7, new DeviceSessionPartcptsUpdateParam(2, CloudMessageBufferDBConstants.ActionStatusFlag.Delete, chatId)));
    }

    public void activateLine(String msisdn) {
        ImsUri uri = Util.getNormalizedTelUri(msisdn);
        if (uri != null) {
            sendMessage(obtainMessage(12, uri.toString()));
        }
    }

    public void deActivateLine(String msisdn) {
        ImsUri uri = Util.getNormalizedTelUri(msisdn);
        if (uri != null) {
            sendMessage(obtainMessage(13, uri.toString()));
        }
    }

    public void onReturnAppFetchingFailedMsg(String syncDirection) {
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(syncDirection)) {
            sendMessage(obtainMessage(30, (Object) null));
        }
    }

    private boolean isDelayProcessGcm(GCMPushNotification pushnotification) {
        if ("SMS".equalsIgnoreCase(pushnotification.pnsType) && "MOMT".equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if ("MMS".equalsIgnoreCase(pushnotification.pnsType) && "MOMT".equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if (TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pushnotification.pnsType) && TMOConstants.TmoGcmPnsVariables.CHAT.equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if (TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pushnotification.pnsType) && TMOConstants.TmoGcmPnsVariables.FILE_TRANSFER.equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if (TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pushnotification.pnsType) && TMOConstants.TmoGcmPnsVariables.LMM.equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if (TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pushnotification.pnsType) && TMOConstants.TmoGcmPnsVariables.CHAT.equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return true;
        }
        if (!TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pushnotification.pnsType) || !"IMDN".equalsIgnoreCase(pushnotification.pnsSubtype)) {
            return false;
        }
        return true;
    }

    private boolean isMsgPushNotification(GCMPushNotification pushnotification) {
        return "SMS".equalsIgnoreCase(pushnotification.pnsType) || "MMS".equalsIgnoreCase(pushnotification.pnsType) || TMOConstants.TmoGcmPnsVariables.RCS_SESSION.equalsIgnoreCase(pushnotification.pnsType) || TMOConstants.TmoGcmPnsVariables.RCS_PAGE.equalsIgnoreCase(pushnotification.pnsType) || TMOConstants.TmoGcmPnsVariables.FAX.equalsIgnoreCase(pushnotification.pnsType);
    }

    public void onGcmReceived(GCMPushNotification pushnotification) {
        if (!this.mDeviceDataChangeListener.isNativeMsgAppDefault() && isMsgPushNotification(pushnotification)) {
            Log.d(TAG, "onGcmReceived: msg app not default application - Ignore msg push notification");
        } else if (isDelayProcessGcm(pushnotification)) {
            sendMessageDelayed(obtainMessage(26, pushnotification), TMOVariables.TmoMessageSyncPeriod.PUSH_SYNC_DELAY);
        } else {
            sendMessage(obtainMessage(26, pushnotification));
        }
    }

    public void onNativeChannelReceived(ParamOMAresponseforBufDB para) {
        if (!this.mDeviceDataChangeListener.isNativeMsgAppDefault()) {
            Log.d(TAG, "onNativeChannelReceived: msg app not default application - Ignore native channel notification");
        } else {
            sendMessage(obtainMessage(3, new AsyncResult((Object) null, para, (Throwable) null)));
        }
    }

    public void receivedMessageJson(String json) {
        sendMessage(obtainMessage(15, json));
    }

    public void sentMessageJson(String json) {
        sendMessage(obtainMessage(16, json));
    }

    public void readMessageJson(String appType, String json) {
        if (CloudMessageStrategyManager.getStrategy().shouldEnableNetAPIPutFlag(appType)) {
            sendMessage(obtainMessage(17, json));
        }
    }

    public void unReadMessageJson(String json) {
        sendMessage(obtainMessage(18, json));
    }

    public void deleteMessageJson(String json) {
        sendMessage(obtainMessage(19, json));
    }

    public void uploadMessageJson(String json) {
        sendMessage(obtainMessage(20, json));
    }

    public void downloadMessageJson(String json) {
        sendMessage(obtainMessage(21, json));
    }

    public void wipeOutMessageJson(String json) {
        sendMessage(obtainMessage(22, json));
    }

    public void bufferDbReadBatchMessageJson(String json) {
        sendMessage(obtainMessage(25, json));
    }

    public void startFullSync(String appType, String json) {
        if (CloudMessageStrategyManager.getStrategy().isAppTriggerMessageSearch()) {
            ParamAppJsonValueList list = decodeJson(appType, json, CloudMessageBufferDBConstants.MsgOperationFlag.StartFullSync);
            if (list == null) {
                Log.e(TAG, "error parsing startfullsync json value");
            } else {
                sendMessage(obtainMessage(27, list));
            }
        }
    }

    public void stopSync(String appType, String json) {
        if (CloudMessageStrategyManager.getStrategy().isAppTriggerMessageSearch()) {
            ParamAppJsonValueList list = decodeJson(appType, json, CloudMessageBufferDBConstants.MsgOperationFlag.StopSync);
            if (list == null) {
                Log.e(TAG, "error parsing startfullsync json value");
            } else {
                sendMessage(obtainMessage(28, list));
            }
        }
    }

    public void resyncPendingMsg() {
        sendEmptyMessage(31);
    }

    private void onBulkCreationComplete(ParamOMAresponseforBufDB para) {
        if (para == null || para.getBufferDBChangeParamList() == null || para.getBufferDBChangeParamList().mChangelst == null) {
            Log.d(TAG, "DBchange list is empty: do nothting ");
        } else if (para.getBulkResponseList() != null && para.getBulkResponseList().response != null) {
            BufferDBChangeParamList list = para.getBufferDBChangeParamList();
            int listSize = para.getBulkResponseList().response.length;
            if (listSize > list.mChangelst.size()) {
                listSize = list.mChangelst.size();
            }
            for (int i = 0; i < listSize; i++) {
                if (!(para.getBulkResponseList().response[i].success == null || para.getBulkResponseList().response[i].success.resourceURL == null)) {
                    handleBulkOpSingleUrlSuccess(para.getBulkResponseList().response[i].success.resourceURL.toString());
                    Reference reference = new Reference();
                    reference.resourceURL = para.getBulkResponseList().response[i].success.resourceURL;
                    reference.path = "";
                    onCloudUploadSuccess(new ParamOMAresponseforBufDB.Builder().setReference(reference).setBufferDBChangeParam(list.mChangelst.get(i)).build());
                }
            }
        }
    }
}
