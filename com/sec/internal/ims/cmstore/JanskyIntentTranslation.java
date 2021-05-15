package com.sec.internal.ims.cmstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.cmstore.CloudMessageIntent;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.GCMPushNotification;

public class JanskyIntentTranslation {
    private static final String LOG_TAG = JanskyIntentTranslation.class.getSimpleName();
    private final CloudMessageService mCloudMsgService;
    private final Context mContext;

    public JanskyIntentTranslation(Context context, CloudMessageService cldMsgService) {
        Log.i(LOG_TAG, "Create JanskyServiceTranslation.");
        this.mContext = context;
        this.mCloudMsgService = cldMsgService;
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        Log.i(str, "Received intent: " + action);
        if (TextUtils.equals(action, "com.samsung.nsds.action.LINES_ACTIVATED")) {
            onLineActivated(intent);
        } else if (TextUtils.equals(action, "com.samsung.nsds.action.LINES_DEACTIVATED")) {
            onDeLineActivated(intent);
        } else if (TextUtils.equals(action, NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED)) {
            onSimDeviceActivated(intent);
        } else if (TextUtils.equals(action, NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED)) {
            onSimDeviceDeActivated(intent);
        } else if (TextUtils.equals(action, NSDSNamespaces.NSDSActions.RECEIVED_PUSH_NOTIFICATION)) {
            onReceiveGCMPushNotification(intent);
        } else if (TextUtils.equals(action, NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION)) {
            onReceiveNativeChannelNotification(intent);
        } else if (TextUtils.equals(action, "com.samsung.nsds.action.SIT_REFRESHED")) {
            onSITRefreshed(intent);
        }
    }

    private void onLineActivated(Intent intent) {
        Bundle extras = intent.getExtras();
        String msisdn = extras.getString("line_msisdn");
        boolean isSuccess = extras.getBoolean(NSDSNamespaces.NSDSExtras.REQUEST_STATUS);
        String str = LOG_TAG;
        IMSLog.s(str, "onLineActivated: " + IMSLog.checker(msisdn) + " issuccess:" + isSuccess);
        if (isSuccess && msisdn != null) {
            this.mCloudMsgService.onMStoreEnabled(msisdn);
        }
    }

    private void onSimDeviceActivated(Intent intent) {
        IMSLog.s(LOG_TAG, "onSimDeviceActivated");
        if (Boolean.valueOf(intent.getExtras().getBoolean(NSDSNamespaces.NSDSExtras.REQUEST_STATUS)).booleanValue()) {
            String msisdn = CloudMessagePreferenceManager.getInstance().getUserCtn();
            if (msisdn == null || msisdn.length() <= 5) {
                msisdn = CloudMessageStrategyManager.getStrategy().getNativeLine();
                CloudMessagePreferenceManager.getInstance().saveUserCtn(msisdn, false);
            }
            if (msisdn != null && msisdn.length() > 5) {
                this.mCloudMsgService.onMStoreEnabled(msisdn);
            }
        }
    }

    private void onSimDeviceDeActivated(Intent intent) {
        IMSLog.s(LOG_TAG, "onSimDeviceDeActivated");
        if (Boolean.valueOf(intent.getExtras().getBoolean(NSDSNamespaces.NSDSExtras.REQUEST_STATUS)).booleanValue()) {
            String msisdn = CloudMessagePreferenceManager.getInstance().getUserCtn();
            if (msisdn == null || msisdn.length() <= 5) {
                IMSLog.s(LOG_TAG, "onSimDeviceDeActivated: do nothing, no userctn");
            } else {
                this.mCloudMsgService.onMStoreDisabled(msisdn);
            }
        }
    }

    private void onDeLineActivated(Intent intent) {
        Bundle extras = intent.getExtras();
        String msisdn = extras.getString("line_msisdn");
        boolean isSuccess = extras.getBoolean(NSDSNamespaces.NSDSExtras.REQUEST_STATUS);
        String str = LOG_TAG;
        IMSLog.s(str, "onDeLineActivated: " + IMSLog.checker(msisdn) + " issuccess:" + isSuccess);
        if (isSuccess) {
            this.mCloudMsgService.onMStoreDisabled(msisdn);
        }
    }

    private void onReceiveGCMPushNotification(Intent intent) {
        String pushMessage = intent.getExtras().getString(NSDSNamespaces.NSDSExtras.ORIG_PUSH_MESSAGE);
        String str = LOG_TAG;
        IMSLog.s(str, "onReceiveGCMPushNotification: " + pushMessage);
        try {
            GCMPushNotification pushnotification = (GCMPushNotification) new Gson().fromJson(pushMessage, GCMPushNotification.class);
            pushnotification.mOrigNotification = pushMessage;
            if (isValidPushNotification(pushnotification)) {
                this.mCloudMsgService.onGcmReceived(pushnotification);
            } else {
                Log.e(LOG_TAG, "invalid push notifiction: ");
            }
        } catch (Exception e) {
            String str2 = LOG_TAG;
            Log.e(str2, "onReceiveGCMPushNotification: " + e.getMessage());
        }
    }

    private void onReceiveNativeChannelNotification(Intent intent) {
        String pushMessage = intent.getExtras().getString(NSDSNamespaces.NSDSExtras.ORIG_PUSH_MESSAGE);
        String str = LOG_TAG;
        IMSLog.s(str, "onReceiveNativeChannelNotification, pushMessage: " + pushMessage);
        if (TextUtils.isEmpty(CloudMessagePreferenceManager.getInstance().getOMASubscriptionResUrl())) {
            Log.e(LOG_TAG, "Not subscribed, it should not receive gcm notifications here");
            return;
        }
        try {
            OMAApiResponseParam response = (OMAApiResponseParam) new Gson().fromJson(pushMessage, OMAApiResponseParam.class);
            if (response != null) {
                if (response.notificationList != null) {
                    NotificationList[] notificationList = response.notificationList;
                    boolean setDelayedUpdateSubscription = false;
                    if (notificationList != null) {
                        NotificationList notiList = notificationList[0];
                        if (notiList.largePollingNotification != null) {
                            String channelURL = notiList.largePollingNotification.channelURL;
                            String str2 = LOG_TAG;
                            Log.i(str2, "largePollingNotification " + channelURL);
                            CloudMessagePreferenceManager.getInstance().saveOMAChannelURL(channelURL);
                            this.mCloudMsgService.handleLargeDataPolling();
                            return;
                        } else if (MailBoxHelper.isMailBoxReset(pushMessage)) {
                            Log.i(LOG_TAG, "MailBoxReset true");
                            this.mCloudMsgService.onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET).build());
                            return;
                        } else if (notiList.nmsEventList != null) {
                            if (Util.isMatchedSubscriptionID(notiList)) {
                                long savedindex = CloudMessagePreferenceManager.getInstance().getOMASubscriptionIndex();
                                long curindex = notiList.nmsEventList.index.longValue();
                                String str3 = LOG_TAG;
                                Log.d(str3, "notification curindex=" + curindex + " savedindex=" + savedindex);
                                if (curindex > savedindex + 1) {
                                    if (NotificationListContainer.getInstance().isEmpty()) {
                                        setDelayedUpdateSubscription = true;
                                    }
                                    NotificationListContainer.getInstance().insertContainer(Long.valueOf(curindex), notificationList);
                                } else if (curindex == savedindex + 1) {
                                    CloudMessagePreferenceManager.getInstance().saveOMASubscriptionRestartToken(notiList.nmsEventList.restartToken);
                                    CloudMessagePreferenceManager.getInstance().saveOMASubscriptionIndex(curindex);
                                    this.mCloudMsgService.onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationList).build());
                                    long savedindex2 = CloudMessagePreferenceManager.getInstance().getOMASubscriptionIndex();
                                    while (true) {
                                        if (NotificationListContainer.getInstance().isEmpty() || NotificationListContainer.getInstance().peekFirstIndex() != savedindex2 + 1) {
                                            break;
                                        }
                                        NotificationList[] notificationList2 = NotificationListContainer.getInstance().popFirstEntry().getValue();
                                        NotificationList notiList2 = notificationList2[0];
                                        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionRestartToken(notiList2.nmsEventList.restartToken);
                                        CloudMessagePreferenceManager.getInstance().saveOMASubscriptionIndex(notiList2.nmsEventList.index.longValue());
                                        this.mCloudMsgService.onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationList2).build());
                                        savedindex2 = CloudMessagePreferenceManager.getInstance().getOMASubscriptionIndex();
                                        if (NotificationListContainer.getInstance().isEmpty()) {
                                            Log.i(LOG_TAG, "NotificationListContainer is empty, all the disordered notifications have been proceeded, remove UPDATE_SUBSCRIPTION_CHANNEL_DELAY");
                                            this.mCloudMsgService.removeUpdateSubscriptionChannelEvent();
                                            break;
                                        }
                                    }
                                }
                            } else {
                                Log.e(LOG_TAG, "no link subscription url matched, drop this notification");
                                return;
                            }
                        }
                    }
                    if (setDelayedUpdateSubscription) {
                        this.mCloudMsgService.updateSubscriptionChannel();
                    }
                    return;
                }
            }
            Log.e(LOG_TAG, "response or notificationList is null, polling failed");
        } catch (Exception e) {
            String str4 = LOG_TAG;
            Log.e(str4, "onReceiveNativeChannelNotification: " + e.getMessage());
        }
    }

    private boolean isValidPushNotification(GCMPushNotification pushnotification) {
        String dataType = CloudMessageStrategyManager.getStrategy().makeParamNotificationType(pushnotification.pnsType, pushnotification.pnsSubtype).getDataType();
        String str = LOG_TAG;
        IMSLog.s(str, "judge PushNotification, dataType = " + dataType);
        try {
            if (pushnotification.recipients == null || pushnotification.nmsEventList.nmsEvent == null) {
                return false;
            }
            if (pushnotification.nmsEventList.nmsEvent[0].changedObject == null && pushnotification.nmsEventList.nmsEvent[0].deletedObject == null && pushnotification.nmsEventList.nmsEvent[0].notifyObject == null) {
                return false;
            }
            if (pushnotification.nmsEventList.nmsEvent[0].changedObject != null) {
                if (pushnotification.nmsEventList.nmsEvent[0].changedObject.resourceURL == null) {
                    return false;
                }
                if (pushnotification.nmsEventList.nmsEvent[0].changedObject.extendedMessage.recipients == null && !dataType.equals("GSO")) {
                    return false;
                }
            }
            if (pushnotification.nmsEventList.nmsEvent[0].deletedObject == null || pushnotification.nmsEventList.nmsEvent[0].deletedObject.resourceURL != null) {
                return true;
            }
            return false;
        } catch (NullPointerException e1) {
            String str2 = LOG_TAG;
            Log.e(str2, "NullPointerException: " + e1.getMessage());
            return false;
        }
    }

    public void onNotifyMessageApp(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        String str = LOG_TAG;
        Log.i(str, "onNotifyMessageApp : " + msgType);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onNotifyMessageApp, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    /* renamed from: com.sec.internal.ims.cmstore.JanskyIntentTranslation$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.InitialSyncStatusFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.InitialSyncStatusFlag.START.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag[CloudMessageBufferDBConstants.InitialSyncStatusFlag.FINISHED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag[CloudMessageBufferDBConstants.InitialSyncStatusFlag.FAIL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public void onNotifyMessageAppInitialSyncStatus(String line, String messageType, CloudMessageBufferDBConstants.InitialSyncStatusFlag flag) {
        Intent intent = null;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$InitialSyncStatusFlag[flag.ordinal()];
        if (i == 1) {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNSTART);
        } else if (i == 2) {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNCEND);
        } else if (i == 3) {
            intent = new Intent(CloudMessageIntent.Action.MSGINTENT_INITSYNCFAIL);
        }
        if (intent != null) {
            intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
            intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, messageType);
            intent.putExtra("linenum", line);
            String str = LOG_TAG;
            IMSLog.s(str, "onNotifyMessageAppInitialSyncStatus, broadcastIntent: " + intent.toString() + intent.getExtras());
            sendBroadcastToMsgApp(this.mContext, intent);
        }
    }

    public void onNotifyMessageAppCloudDeleteFailure(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGDELETEFAILURE);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        String str = LOG_TAG;
        Log.i(str, "onNotifyMessageAppCloudDeleteFailure : " + msgType);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onNotifyMessageAppCloudDeleteFailure, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    public void onNotifyMessageAppUI(int screenName, String message, int param) {
        Intent intent = new Intent(CloudMessageIntent.Action.MSGUIINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.SCREENNAME, screenName);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.STYLE, message);
        intent.putExtra(CloudMessageIntent.ExtrasAMBSUI.PARAM, param);
        String str = LOG_TAG;
        Log.i(str, "onNotifyMessageAppUI : " + screenName);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onNotifyMessageAppUI, broadcastIntent: " + intent.toString() + intent.getExtras());
        sendBroadcastToMsgApp(this.mContext, intent);
    }

    public void onNotifyVVMApp(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.VVMINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        broadcastIntent(intent);
    }

    public void onNotifyVVMAppCloudDeleteFailure(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.VVMDATADELETEFAILURE);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        broadcastIntent(intent);
    }

    public void onNotifyContactApp(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.CALLLOGINTENT);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        broadcastIntent(intent);
    }

    public void onNotifyContactAppCloudDeleteFailure(String msgType, String jsonString) {
        Intent intent = new Intent(CloudMessageIntent.Action.CALLLOGDATADELETEFAILURE);
        intent.addCategory(CloudMessageIntent.CATEGORY_ACTION);
        intent.putExtra(CloudMessageIntent.Extras.MSGTYPE, msgType);
        intent.putExtra(CloudMessageIntent.Extras.ROWIDS, jsonString);
        broadcastIntent(intent);
    }

    private void onSITRefreshed(Intent intent) {
        Bundle extras = intent.getExtras();
        boolean status = extras.getBoolean(NSDSNamespaces.NSDSExtras.REQUEST_STATUS);
        String msisdn = extras.getString("line_msisdn");
        String str = LOG_TAG;
        Log.i(str, "status : " + status);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onSITRefreshed, msisdn : " + IMSLog.checker(msisdn));
        if (status) {
            this.mCloudMsgService.onJanskySITTokenUpdated(msisdn);
        }
    }

    public void broadcastIntent(Intent intent) throws NullPointerException {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent.toString() + intent.getExtras());
        intent.addFlags(16777216);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public static void sendBroadcastToMsgApp(Context context, Intent intent) {
        intent.addFlags(16777216);
        context.sendBroadcast(intent, CloudMessageIntent.Permission.MSGAPP);
    }
}
