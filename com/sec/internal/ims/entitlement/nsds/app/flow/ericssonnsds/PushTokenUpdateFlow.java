package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.log.IMSLog;

public class PushTokenUpdateFlow extends NSDSAppFlowBase {
    private static final String LOG_TAG = PushTokenUpdateFlow.class.getSimpleName();
    private static final int UPDATE_PUSH_TOKEN = 1;

    public PushTokenUpdateFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    public void updatePushToken() {
        sendEmptyMessage(1);
    }

    private void handleResponsePushToken(Bundle bundleNSDSResponses) {
        if (bundleNSDSResponses != null) {
            ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundleNSDSResponses.getParcelable("managePushToken");
            if (responseManagePushToken != null && responseManagePushToken.responseCode == 1000) {
                IMSLog.i(LOG_TAG, "push token is udpated in entitlment successfully");
                return;
            }
            return;
        }
        IMSLog.e(LOG_TAG, "responseManagePushToken is NULL");
    }

    private void updatePushTokenForActiveLines() {
        String nativeMsisdn = this.mNSDSDatabaseHelper.getNativeMsisdn(this.mBaseFlowImpl.getDeviceId());
        IMnoNsdsStrategy mnoStrategy = getMnoNsdsStrategy();
        if (mnoStrategy != null) {
            for (String service : mnoStrategy.getServiceListForPushToken()) {
                updatePushTokenForLine(nativeMsisdn, service);
            }
            for (String activeMsisdn : this.mNSDSDatabaseHelper.getActiveMsisdns(this.mBaseFlowImpl.getDeviceId()).keySet()) {
                if (!activeMsisdn.equals(nativeMsisdn)) {
                    updatePushTokenForLine(activeMsisdn, "vowifi");
                }
            }
        }
    }

    private void updatePushTokenForLine(String msisdnFromNw, String serviceName) {
        String str = LOG_TAG;
        IMSLog.i(str, "updatePushTokenForLine: serviceName " + serviceName);
        String msisdn = msisdnFromNw == null ? NSDSHelper.getMSISDNFromSIM(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()) : msisdnFromNw;
        String pushToken = NSDSSharedPrefHelper.get(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        if (pushToken != null) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "updating push token for msisn:" + msisdn);
            new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA).updatePushToken(msisdn, serviceName, (String) null, 0, pushToken, false);
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 1) {
            updatePushTokenForActiveLines();
        } else if (i != 112) {
            String str = LOG_TAG;
            IMSLog.i(str, "Unknown flow request: " + msg.what);
        } else {
            handleResponsePushToken(msg.getData());
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
    }
}
