package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.SimSwapNSDSConfigHelper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import java.util.ArrayList;

public class AttSimSwapFlow extends NSDSAppFlowBase implements ISimSwapFlow {
    private static final String LOG_TAG = AttSimSwapFlow.class.getSimpleName();
    private static final int REMOVE_PUSH_TOKEN = 0;
    private SimSwapCompletedListener mSimSwapCompletedListener;

    public AttSimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
        this.mDeviceEventType = 3;
    }

    public void handleSimSwap(SimSwapCompletedListener simSwapCompletedListener) {
        this.mSimSwapCompletedListener = simSwapCompletedListener;
        Log.i(LOG_TAG, "handleSimSwap....");
        String deviceUid = this.mBaseFlowImpl.getDeviceId();
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy == null || !mnoVSimStrategy.isNsdsUIAppSwitchOn(deviceUid)) {
            notifyNSDSFlowResponse(true, (String) null, -1, -1);
        } else {
            sendEmptyMessage(0);
        }
    }

    private void removePushToken() {
        Log.i(LOG_TAG, "removePushToken()");
        String akaToken = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_AKA_TOKEN);
        String imsiEap = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        String pushToken = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        String msisdnFromNw = SimSwapNSDSConfigHelper.getConfigValue(this.mContext, SimSwapNSDSConfigHelper.KEY_NATIVE_MSISDN);
        new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", (String) null, (String) null).removeVoWiFiPushToken(msisdnFromNw == null ? NSDSHelper.getMSISDNFromSIM(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()) : msisdnFromNw, (String) null, pushToken, NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, akaToken, imsiEap, false, 30000);
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRemovePushTokenResponse(Bundle bundleNSDSResponses) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(1000, (String) null, -1);
        if (bundleNSDSResponses == null) {
            return nsdsResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundleNSDSResponses.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            Log.i(str, "responseManagePushToken : messageId:" + responseManagePushToken.messageId + "responseCode:" + responseManagePushToken.responseCode);
            if (responseManagePushToken.responseCode != 1000) {
                Log.i(LOG_TAG, "responseManagePushToken failed");
                nsdsResponseStatus.failedOperation = 1;
            }
        } else {
            Log.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        return nsdsResponseStatus;
    }

    private void resetDeviceStatus() {
        String deviceUid = this.mBaseFlowImpl.getDeviceId();
        ISimManager sm = this.mBaseFlowImpl.getSimManager();
        NSDSSharedPrefHelper.save(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, sm.getSimSlotIndex(), NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        NSDSSharedPrefHelper.removeAkaToken(this.mContext, sm.getImsi());
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER);
        NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceUid);
        this.mNSDSDatabaseHelper.resetE911AidInfoForNativeLine(deviceUid);
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        Log.i(str, "notifyNSDSFlowResponse: success " + success);
        resetDeviceStatus();
        ArrayList<Integer> translatedErrorCodes = new ArrayList<>();
        translatedErrorCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE));
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, true);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErrorCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        SimSwapCompletedListener simSwapCompletedListener = this.mSimSwapCompletedListener;
        if (simSwapCompletedListener != null) {
            simSwapCompletedListener.onSimSwapCompleted();
        }
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "msg:" + msg.what);
        int i = msg.what;
        if (i == 0) {
            removePushToken();
        } else if (i == 113) {
            performNextOperationIf(5, handleRemovePushTokenResponse(msg.getData()), msg.getData());
        }
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
    }
}
