package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.SIMDeviceActivation;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.IntentScheduler;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class SIMDeviceImplicitActivation extends NSDSAppFlowBase implements ISIMDeviceImplicitActivation {
    private static final String ACTION_WAIT_GCM_TOKEN = "com.sec.vsim.ericssonnsds.WAIT_GCM_TOKEN";
    private static final long GCM_TOKEN_WAIT_TIME = 10000;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = SIMDeviceImplicitActivation.class.getSimpleName();
    private static final int START_SIM_ACTIVATION = 1;
    private BroadcastReceiver mGcmTokenIntentReceiver = null;
    private SharedPreferences.OnSharedPreferenceChangeListener mNSDSSharedPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.contains(NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER)) {
                boolean isTokenSent = NSDSSharedPrefHelper.isGcmTokenSentToServer(SIMDeviceImplicitActivation.this.mContext, SIMDeviceImplicitActivation.this.mBaseFlowImpl.getDeviceId());
                String access$000 = SIMDeviceImplicitActivation.LOG_TAG;
                IMSLog.i(access$000, "PREF_SENT_TOKEN_TO_SERVER: " + isTokenSent);
                SIMDeviceImplicitActivation.this.resumeSimDeviceActivation();
            }
        }
    };
    protected LineDetail mNativeLineDetail = new LineDetail();

    /* access modifiers changed from: private */
    public void resumeSimDeviceActivation() {
        IMSLog.i(LOG_TAG, "resumeSimDeviceActivation()");
        stopGcmTokenWaitTimer();
        unregisterNsdsSharePrefChangeListener();
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
        sendEmptyMessage(1);
    }

    private void registerNsdsSharePrefChangeListener() {
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp != null) {
            sp.registerOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
        }
    }

    private void unregisterNsdsSharePrefChangeListener() {
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(this.mContext, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp != null) {
            sp.unregisterOnSharedPreferenceChangeListener(this.mNSDSSharedPrefChangeListener);
        }
    }

    private void startGcmTokenWaitTimer() {
        IMSLog.i(LOG_TAG, "startGcmTokenWaitTimer()");
        stopGcmTokenWaitTimer();
        this.mGcmTokenIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String access$000 = SIMDeviceImplicitActivation.LOG_TAG;
                IMSLog.i(access$000, "onReceive: timer expired for " + intent.getAction());
                if (SIMDeviceImplicitActivation.ACTION_WAIT_GCM_TOKEN.equals(intent.getAction())) {
                    SIMDeviceImplicitActivation.this.resumeSimDeviceActivation();
                }
            }
        };
        this.mContext.registerReceiver(this.mGcmTokenIntentReceiver, new IntentFilter(ACTION_WAIT_GCM_TOKEN));
        IntentScheduler.scheduleTimer(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), ACTION_WAIT_GCM_TOKEN, 10000);
    }

    private void stopGcmTokenWaitTimer() {
        if (this.mGcmTokenIntentReceiver != null) {
            IMSLog.i(LOG_TAG, "stopGcmTokenWaitTimer()");
            IntentScheduler.stopTimer(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), ACTION_WAIT_GCM_TOKEN);
            this.mContext.unregisterReceiver(this.mGcmTokenIntentReceiver);
            this.mGcmTokenIntentReceiver = null;
        }
    }

    public SIMDeviceImplicitActivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    /* access modifiers changed from: protected */
    public NSDSAppFlowBase.NSDSResponseStatus handleSimDeviceActivationResponse(Bundle bundleNSDSResponses) {
        int errorResponseCode = getHttpErrRespCode(bundleNSDSResponses);
        String str = LOG_TAG;
        IMSLog.i(str, "handleSimDeviceActivationResponse: errorResponseCode: " + errorResponseCode);
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(errorResponseCode, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, -1);
        if (bundleNSDSResponses == null || errorResponseCode > 0) {
            return nsdsResponseStatus;
        }
        ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
        ResponseManagePushToken responsePushToken = (ResponseManagePushToken) bundleNSDSResponses.getParcelable("managePushToken");
        ResponseGetMSISDN responseGetMsisdn = (ResponseGetMSISDN) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN);
        handleResponsePushToken(responsePushToken);
        handleResponseGetMsisdn(responseGetMsisdn);
        if (responseManageConnectivity == null || responseManageConnectivity.responseCode != 1000 || responsePushToken == null || responsePushToken.responseCode != 1000 || responseGetMsisdn == null || responseGetMsisdn.responseCode != 1000) {
            if (responseGetMsisdn != null && responseGetMsisdn.responseCode != 1000) {
                nsdsResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
                nsdsResponseStatus.responseCode = responseGetMsisdn.responseCode;
            } else if (responseManageConnectivity != null && responseManageConnectivity.responseCode != 1000) {
                nsdsResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY;
                nsdsResponseStatus.failedOperation = 0;
                nsdsResponseStatus.responseCode = responseManageConnectivity.responseCode;
            } else if (!(responsePushToken == null || responsePushToken.responseCode == 1000)) {
                nsdsResponseStatus.methodName = "managePushToken";
                nsdsResponseStatus.responseCode = responsePushToken.responseCode;
            }
            IMSLog.e(LOG_TAG, "SIMDevice activation failed:");
        } else {
            nsdsResponseStatus.responseCode = responseManageConnectivity.responseCode;
        }
        if (this.mNativeLineDetail.lineId <= 0) {
            nsdsResponseStatus.responseCode = -1;
            IMSLog.e(LOG_TAG, "handleSimDeviceActivationResponse: native line insert/update failed");
        }
        return nsdsResponseStatus;
    }

    /* access modifiers changed from: protected */
    public void handleResponseGetMsisdn(ResponseGetMSISDN responseGetMsisdn) {
        if (responseGetMsisdn != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "responseGetMsisdn : messageId:" + responseGetMsisdn.messageId + "responseCode:" + responseGetMsisdn.responseCode + "msisdn:" + responseGetMsisdn.msisdn + "service_fingerprint:" + responseGetMsisdn.serviceFingerprint);
            if (responseGetMsisdn.responseCode == 1000 && responseGetMsisdn.msisdn != null && responseGetMsisdn.serviceFingerprint != null) {
                this.mNativeLineDetail.lineId = this.mNSDSDatabaseHelper.insertOrUpdateNativeLine(0, this.mBaseFlowImpl.getDeviceId(), responseGetMsisdn);
                this.mNativeLineDetail.msisdn = responseGetMsisdn.msisdn;
                this.mNativeLineDetail.serviceFingerPrint = responseGetMsisdn.serviceFingerprint;
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, "ResponseGetMSISDN is NULL");
    }

    /* access modifiers changed from: protected */
    public void handleResponsePushToken(ResponseManagePushToken responsePushToken) {
        if (responsePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responsePushToken : messageId:" + responsePushToken.messageId + "responseCode:" + responsePushToken.responseCode);
            return;
        }
        IMSLog.e(LOG_TAG, "ResponseManagePushToken is NULL");
    }

    public void performSimDeviceImplicitActivation(int deviceEventType, int retryCount) {
        String str = LOG_TAG;
        IMSLog.i(str, "performSimDeviceImplicitActivation: eventType-" + deviceEventType);
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1), (Bundle) null);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nsdsBaseOperation, Bundle dataMap) {
        int msgId = -1;
        if (nsdsBaseOperation != 1) {
            IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
        } else {
            msgId = 1;
        }
        if (msgId != -1) {
            Message message = obtainMessage(msgId);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    private void startSimDeviceActivation() {
        IMSLog.i(LOG_TAG, "startSimDeviceActivation:");
        if (NSDSSharedPrefHelper.isDeviceInActivationProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "startSimDeviceActivation: activation in progress. do not do any thing");
            return;
        }
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.ACTIVATION_IN_PROGRESS);
        if (!NSDSSharedPrefHelper.isGcmTokenSentToServer(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(LOG_TAG, "startSimDeviceActivation: GCM token not yet created");
            registerNsdsSharePrefChangeListener();
            startGcmTokenWaitTimer();
            return;
        }
        long retryInterval = 0;
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy != null) {
            retryInterval = mnoVSimStrategy.getRetryInterval();
        }
        new SIMDeviceActivation(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").activateSIMDevice((String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), false, retryInterval);
    }

    /* access modifiers changed from: protected */
    public void updateDeviceState(boolean success) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateDeviceState: flow " + success);
        if (success) {
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.ACTIVATED);
            return;
        }
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
    }

    private int translateErrorCode(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        if (success || nsdsMethodName == null || nsdsErrorCode == -1) {
            return -1;
        }
        return NSDSErrorTranslator.translate(nsdsMethodName, operation, nsdsErrorCode);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "msg:" + msg.what);
        int i = msg.what;
        if (i == 1) {
            startSimDeviceActivation();
        } else if (i == 103) {
            performNextOperationIf(1, handleSimDeviceActivationResponse(msg.getData()), (Bundle) null);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: success " + success);
        updateDeviceState(success);
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        translatedErroCodes.add(Integer.valueOf(translateErrorCode(success, nsdsMethodName, operation, nsdsErrorCode)));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putExtra("retry_count", this.mRetryCount);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, nsdsErrorCode);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        notifyCallbackForNsdsEvent(0);
    }
}
