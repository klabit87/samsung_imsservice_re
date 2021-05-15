package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.Response3gppAuthentication;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.RetrieveAkaToken;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.IAkaTokenRetrievalFlow;
import java.util.ArrayList;

public class AkaTokenRetrievalFlow extends NSDSAppFlowBase implements IAkaTokenRetrievalFlow {
    public static final String ACTION_AKA_TOKEN_RETRIEVED = "com.samsung.nsds.action.AKA_TOKEN_RETRIEVED";
    private static final String LOG_TAG = AkaTokenRetrievalFlow.class.getSimpleName();
    private static final int RETRIEVE_AKA_TOKEN = 1;

    public AkaTokenRetrievalFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    /* access modifiers changed from: protected */
    public NSDSAppFlowBase.NSDSResponseStatus handleAkaTokenRetrievalResponse(Bundle bundleNSDSResponses) {
        Response3gppAuthentication response3gppAuthentication;
        int errorResponseCode = getHttpErrRespCode(bundleNSDSResponses);
        String str = LOG_TAG;
        Log.i(str, "handleAkaTokenRetrievalResponse: errorResponseCode: " + errorResponseCode);
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(errorResponseCode, NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH, -1);
        if (!(bundleNSDSResponses == null || errorResponseCode > 0 || (response3gppAuthentication = (Response3gppAuthentication) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH)) == null)) {
            nsdsResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.REQ_3GPP_AUTH;
            nsdsResponseStatus.responseCode = response3gppAuthentication.responseCode;
            if (nsdsResponseStatus.responseCode != 1000) {
                Log.e(LOG_TAG, "Aka Token Retrival failed:");
            }
        }
        return nsdsResponseStatus;
    }

    public void performAkaTokenRetrieval(int deviceEventType, int retryCount) {
        Log.i(LOG_TAG, "performAkaTokenRetrieval()");
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1), (Bundle) null);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nsdsBaseOperation, Bundle dataMap) {
        int msgId = -1;
        String str = LOG_TAG;
        Log.i(str, "queueOperation: " + nsdsBaseOperation);
        if (nsdsBaseOperation == 15) {
            msgId = 1;
        }
        if (msgId != -1) {
            Message message = obtainMessage(msgId);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    private void startAkaTokenretrieval() {
        Log.i(LOG_TAG, "startAkaTokenretrieval()");
        long retryInterval = 0;
        IMnoNsdsStrategy mnoVSimStrategy = getMnoNsdsStrategy();
        if (mnoVSimStrategy != null) {
            retryInterval = mnoVSimStrategy.getRetryInterval();
        }
        new RetrieveAkaToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").retrieveAkaToken((String) null, (String) null, false, retryInterval);
    }

    private int translateErrorCode(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        if (success || nsdsMethodName == null || nsdsErrorCode == -1) {
            return -1;
        }
        return NSDSErrorTranslator.translate(nsdsMethodName, operation, nsdsErrorCode);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "msg:" + msg.what);
        int i = msg.what;
        if (i == 1) {
            startAkaTokenretrieval();
        } else if (i == 118) {
            performNextOperationIf(15, handleAkaTokenRetrievalResponse(msg.getData()), (Bundle) null);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        Log.i(str, "notifyNSDSFlowResponse: success " + success);
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        translatedErroCodes.add(Integer.valueOf(translateErrorCode(success, nsdsMethodName, operation, nsdsErrorCode)));
        Intent intent = new Intent("com.samsung.nsds.action.AKA_TOKEN_RETRIEVED");
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }
}
