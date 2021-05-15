package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.DeviceDeactivation;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceDeactivation;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class SIMDeviceDeactivationFlow extends NSDSAppFlowBase implements ISIMDeviceDeactivation {
    private static final int DEACTIVATE_DEVICE = 0;
    private static final String LOG_TAG = SIMDeviceDeactivationFlow.class.getSimpleName();
    private static int mDeactivateCause = 0;

    public SIMDeviceDeactivationFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    public void deactivateDevice(int deactivationCause) {
        if (!NSDSSharedPrefHelper.isDeviceActivated(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.e(LOG_TAG, "requestSimDeviceDeactivation: not activated");
            notifyNSDSFlowResponse(true, (String) null, -1, -1);
            return;
        }
        Message msg = new Message();
        msg.what = 0;
        msg.arg1 = deactivationCause;
        sendMessage(msg);
    }

    private void handleResponseDeactivation(Bundle bundleNSDSResponses) {
        int nsdsErrorCode = -1;
        boolean flowSuccess = false;
        if (bundleNSDSResponses == null) {
            IMSLog.i(LOG_TAG, "handleRefreshDeviceResponse. response is null");
        } else {
            ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            if (responseManageConnectivity != null) {
                if (responseManageConnectivity.responseCode == 1000) {
                    String str = LOG_TAG;
                    IMSLog.i(str, "ResponseManageConnectivity content : messageId: " + responseManageConnectivity.messageId + ", responseCode: " + responseManageConnectivity.responseCode);
                    flowSuccess = true;
                } else {
                    nsdsErrorCode = responseManageConnectivity.responseCode;
                }
            }
        }
        notifyNSDSFlowResponse(flowSuccess, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 2, nsdsErrorCode);
    }

    private void performDeactivation(int deactivationCause) {
        mDeactivateCause = deactivationCause;
        String str = LOG_TAG;
        IMSLog.i(str, "deactivateDevice: deactivationCause" + deactivationCause);
        DeviceDeactivation deviceDeactivation = new DeviceDeactivation(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA);
        if (mDeactivateCause == 1) {
            deviceDeactivation.deactivateDevice();
        }
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "handleMessage: " + msg.what);
        int i = msg.what;
        if (i == 0) {
            performDeactivation(msg.arg1);
        } else if (i == 111) {
            handleResponseDeactivation(msg.getData());
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: success " + success + " errorcode " + nsdsErrorCode);
        this.mNSDSDatabaseHelper.resetDeviceStatus(this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getImsi(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        if (success) {
            if (nsdsErrorCode == -1 && mDeactivateCause == 1) {
                translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_DEACTIVATION_SUCCESS_FOR_INVALID_FINGERPRINT));
            }
        } else if (!(nsdsMethodName == null || nsdsErrorCode == -1)) {
            translatedErroCodes.add(Integer.valueOf(NSDSErrorTranslator.translate(nsdsMethodName, operation, nsdsErrorCode)));
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_DEACTIVATION_CAUSE, mDeactivateCause);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
    }
}
