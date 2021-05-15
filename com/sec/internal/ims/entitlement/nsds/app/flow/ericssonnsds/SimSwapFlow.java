package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.interfaces.ims.entitlement.nsds.SimSwapCompletedListener;
import java.util.ArrayList;

public class SimSwapFlow extends NSDSAppFlowBase implements ISimSwapFlow {
    private static final String LOG_TAG = SimSwapFlow.class.getSimpleName();
    private SimSwapCompletedListener mSimSwapCompletedListener;

    public SimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    public void handleSimSwap(SimSwapCompletedListener simSwapCompletedListener) {
        this.mSimSwapCompletedListener = simSwapCompletedListener;
        Log.i(LOG_TAG, "handleSimSwap....");
        notifyNSDSFlowResponse(true, (String) null, -1, -1);
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        Log.i(str, "notifyNSDSFlowResponse: success " + success);
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        if (!(success || nsdsMethodName == null || nsdsErrorCode == -1)) {
            translatedErroCodes.add(Integer.valueOf(NSDSErrorTranslator.translate(nsdsMethodName, operation, nsdsErrorCode)));
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_SWAP_COMPLETED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        SimSwapCompletedListener simSwapCompletedListener = this.mSimSwapCompletedListener;
        if (simSwapCompletedListener != null) {
            simSwapCompletedListener.onSimSwapCompleted();
        }
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
    }
}
