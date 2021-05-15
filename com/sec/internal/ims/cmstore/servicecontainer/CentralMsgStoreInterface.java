package com.sec.internal.ims.cmstore.servicecontainer;

import android.os.RemoteException;
import android.util.Log;
import com.sec.ims.ICentralMsgStoreService;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.JanskyIntentTranslation;
import com.sec.internal.ims.cmstore.NetAPIWorkingStatusController;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class CentralMsgStoreInterface {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CentralMsgStoreInterface.class.getSimpleName();
    private ICentralMsgStoreService.Stub mBinder = null;
    /* access modifiers changed from: private */
    public CloudMessageBufferSchedulingHandler mCloudMessageScheduler;
    /* access modifiers changed from: private */
    public ICloudMessageManagerHelper mICloudMessageManagerHelper;
    private JanskyIntentTranslation mJanskyIntentTranslation;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusController mNetAPIWorkingController = null;

    /* access modifiers changed from: private */
    public void logInvalidAppType() {
        Log.e(LOG_TAG, "invalid apptype ");
    }

    public CentralMsgStoreInterface(CloudMessageBufferSchedulingHandler scheduler, NetAPIWorkingStatusController netAPIcontroller, JanskyIntentTranslation janskytranslation, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        this.mCloudMessageScheduler = scheduler;
        this.mNetAPIWorkingController = netAPIcontroller;
        this.mJanskyIntentTranslation = janskytranslation;
        this.mICloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mBinder = new ICentralMsgStoreService.Stub() {
            public void receivedMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "receivedMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "receivedMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.receivedMessageJson(jsonSummary);
                }
            }

            public void sentMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "sentMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "sentMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.sentMessageJson(jsonSummary);
                }
            }

            public void readMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "readMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "readMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.readMessageJson(appType, jsonSummary);
                }
            }

            public void unReadMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "unReadMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "unReadMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.unReadMessageJson(jsonSummary);
                }
            }

            public void deleteMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "deleteMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "deleteMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteMessageJson(jsonSummary);
                }
            }

            public void uploadMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "uploadMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "uploadMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.uploadMessageJson(jsonSummary);
                }
            }

            public void downloadMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$000, "downloadMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "downloadMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.downloadMessageJson(jsonSummary);
                }
            }

            public void wipeOutMessage(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "wipeOutMessage " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "wipeOutMessage : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.wipeOutMessageJson(jsonSummary);
                }
            }

            public void onUserEnterApp(String appType) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "onUserEnterApp " + appType);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                    return;
                }
                CentralMsgStoreInterface.this.mNetAPIWorkingController.setMsgAppForegroundStatus(true);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.onReturnAppFetchingFailedMsg(appType);
            }

            public void onUserLeaveApp(String appType) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "onUserLeaveApp " + appType);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.setMsgAppForegroundStatus(false);
                }
            }

            public boolean onUIButtonProceed(String appType, int screenName, String message) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "onUIButtonProceed " + appType + " screenName: " + screenName);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("onUIButtonProceed , message: ");
                sb.append(message);
                Log.d(access$0002, sb.toString());
                if (CentralMsgStoreInterface.this.isValidAppType(appType) && CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    return CentralMsgStoreInterface.this.mNetAPIWorkingController.onUIButtonProceed(screenName, message);
                }
                CentralMsgStoreInterface.this.logInvalidAppType();
                return false;
            }

            public void onBufferDBReadResult(String appType, String messageType, String bufferRowId, String appMessageId, int syncAction, boolean isSuccess) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "onBufferDBReadResult: " + appType + " msgType: " + messageType + " bufferRowID: " + bufferRowId + " appMessageId: " + appMessageId + " syncAction: " + syncAction + " isSuccess: " + isSuccess);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    Log.d(CentralMsgStoreInterface.LOG_TAG, "ignore");
                } else if (!CentralMsgStoreInterface.this.mICloudMessageManagerHelper.getIsInitSyncIndicatorRequired() || Integer.valueOf(bufferRowId).intValue() >= 0) {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.onBufferDBReadResult(messageType, bufferRowId, appMessageId, syncAction, isSuccess);
                } else {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.hideIndicator();
                }
            }

            public void onBufferDBReadResultBatch(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "onBufferDBReadResultBatch " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "onBufferDBReadResultBatch : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.bufferDbReadBatchMessageJson(jsonSummary);
                }
            }

            public void registerCallback(String appType, ICentralMsgStoreService instance) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$000, "registerCallback " + appType);
            }

            public void stopSync(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "stopSync " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "stopSync : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.stopSync(appType, jsonSummary);
                }
            }

            public void startFullSync(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "startFullSync " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "startFullSync : " + jsonSummary);
                if (!CentralMsgStoreInterface.this.isValidAppType(appType) || !CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                } else {
                    CentralMsgStoreInterface.this.mCloudMessageScheduler.startFullSync(appType, jsonSummary);
                }
            }

            public void deleteOldLegacyMessage(String appType, String threadId) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "deleteOldLegacyMessage " + appType + " thread:" + threadId);
            }

            public void resumeSync(String appType) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "resumeSync " + appType);
            }

            public void restartService(String appType) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "restartService " + appType);
                if (CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                    CentralMsgStoreInterface.this.mNetAPIWorkingController.onRestartService();
                } else {
                    CentralMsgStoreInterface.this.logInvalidAppType();
                }
            }

            public void notifyUIScreen(String appType, int screenName, String style, int para1) throws RemoteException {
            }

            public void notifyCloudMessageUpdate(String appType, String messageType, String rowIDs) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "notifyCloudMessageUpdate, apptype: " + appType + " msgType: " + messageType + " rowIDs: " + rowIDs);
            }

            public void createSession(String appType, String chatId) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "createSession " + appType + " chatId: " + chatId);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.createSession(chatId);
            }

            public void createParticipant(String appType, String chatId) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$000, "createParticipant " + appType + " chatId: " + chatId);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.createParticipant(chatId);
            }

            public void deleteSession(String appType, String chatId) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "deleteSession " + appType + " chatId: " + chatId);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteSession(chatId);
            }

            public void deleteParticipant(String appType, String chatId) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "deleteParticipant " + appType + " chatId: " + chatId);
                CentralMsgStoreInterface.this.mCloudMessageScheduler.deleteParticipant(chatId);
            }

            public void onRCSDBReady(String status) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$000, "onRCSDBReady: " + status);
                try {
                    JSONObject JsonRoot = new JSONObject(status);
                    String eventType = JsonRoot.getString(CloudMessageProviderContract.JsonParamTags.CMS_PROFILE_EVENT);
                    String simStatus = JsonRoot.getString(CloudMessageProviderContract.JsonParamTags.SIM_STATUS);
                    String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                    Log.i(access$0002, "eventType =" + eventType + ", simStatus =" + simStatus);
                    if (CloudMessageProviderContract.SimStatusValue.SIM_REMOVED.equals(simStatus)) {
                        CentralMsgStoreInterface.this.mNetAPIWorkingController.setCmsProfileEnabled(false);
                        return;
                    }
                    if (CloudMessageProviderContract.SimStatusValue.SIM_READY.equals(simStatus) && CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_DISABLE.equals(eventType) && CentralMsgStoreInterface.this.mNetAPIWorkingController.isSimChanged()) {
                        CentralMsgStoreInterface.this.mICloudMessageManagerHelper.clearAll();
                    }
                    if (CloudMessageProviderContract.CmsEventTypeValue.CMS_PROFILE_ENABLE.equals(eventType)) {
                        if (!CentralMsgStoreInterface.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                            CentralMsgStoreInterface.this.mNetAPIWorkingController.setCmsProfileEnabled(true);
                            CentralMsgStoreInterface.this.mCloudMessageScheduler.onRCSDbReady();
                        }
                        if (CentralMsgStoreInterface.this.mICloudMessageManagerHelper.needToHandleSimSwap() && CentralMsgStoreInterface.this.mNetAPIWorkingController.isSimChanged()) {
                            CentralMsgStoreInterface.this.mNetAPIWorkingController.onRestartService();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void manualSync(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "manualSync: " + appType);
                String access$0002 = CentralMsgStoreInterface.LOG_TAG;
                Log.d(access$0002, "manualSync jsonSummary: " + jsonSummary);
                CentralMsgStoreInterface.this.mNetAPIWorkingController.setImpuFromImsRegistration(jsonSummary);
            }

            public void enableAutoSync(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "enableAutoSync: " + appType);
            }

            public void disableAutoSync(String appType, String jsonSummary) throws RemoteException {
                String access$000 = CentralMsgStoreInterface.LOG_TAG;
                Log.i(access$000, "disableAutoSync: " + appType);
            }
        };
    }

    public void notifyUIScreen(int screenName, String message, int param) {
        String str = LOG_TAG;
        Log.i(str, "notifyUIScreen, screenName: " + screenName);
        String str2 = LOG_TAG;
        Log.d(str2, "notifyUIScreen, message: " + message + " param: " + param);
        this.mJanskyIntentTranslation.onNotifyMessageAppUI(screenName, message, param);
    }

    public void notifyAppInitialSyncStatus(String appType, String messageType, String line, CloudMessageBufferDBConstants.InitialSyncStatusFlag SyncStatus) {
        String str = LOG_TAG;
        Log.i(str, "notifyAppInitialSyncStatus, apptype: " + appType + " msgType: " + messageType + " SyncStatus: " + SyncStatus);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyMessageAppInitialSyncStatus(line, messageType, SyncStatus);
        }
    }

    public void notifyCloudMessageUpdate(String appType, String messageType, String rowIDs) {
        String str = LOG_TAG;
        Log.i(str, "notifyCloudMessageUpdate, apptype: " + appType + " msgType: " + messageType + " rowIDs: " + rowIDs);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyMessageApp(messageType, rowIDs);
        } else if ("VVMDATA".equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyVVMApp(messageType, rowIDs);
        } else if ("CALLLOGDATA".equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyContactApp(messageType, rowIDs);
        }
    }

    public void notifyAppCloudDeleteFail(String appType, String msgType, String rowIDs) {
        String str = LOG_TAG;
        Log.i(str, "notifyAppCloudDeleteFail, type: " + appType + " msgtype: " + msgType + " bufferId: " + rowIDs);
        if (CloudMessageProviderContract.ApplicationTypes.MSGDATA.equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyMessageAppCloudDeleteFailure(msgType, rowIDs);
        } else if ("VVMDATA".equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyVVMAppCloudDeleteFailure(msgType, rowIDs);
        } else if ("CALLLOGDATA".equals(appType)) {
            this.mJanskyIntentTranslation.onNotifyContactAppCloudDeleteFailure(msgType, rowIDs);
        }
    }

    /* access modifiers changed from: private */
    public boolean isValidAppType(String appType) {
        if (!CloudMessageProviderContract.ApplicationTypes.MSGDATA.equalsIgnoreCase(appType) && !"VVMDATA".equalsIgnoreCase(appType) && !"CALLLOGDATA".equalsIgnoreCase(appType) && !CloudMessageProviderContract.ApplicationTypes.RCSDATA.equalsIgnoreCase(appType)) {
            return false;
        }
        return true;
    }

    public ICentralMsgStoreService.Stub getBinder() {
        return this.mBinder;
    }
}
