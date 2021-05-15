package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NSDSAppFlowBase extends Handler {
    private static final String LOG_TAG = NSDSAppFlowBase.class.getSimpleName();
    protected static final List<Messenger> sEvtMessengers = new ArrayList();
    protected BaseFlowImpl mBaseFlowImpl;
    protected final Context mContext;
    protected ArrayList<Message> mDeferredMessages = new ArrayList<>();
    protected int mDeviceEventType;
    protected String mDeviceGroup = null;
    protected String mImeiForUA = null;
    protected NSDSDatabaseHelper mNSDSDatabaseHelper;
    protected int mRetryCount = 0;
    protected int mSlotId = 0;
    protected String mUserAgent = null;

    /* access modifiers changed from: protected */
    public abstract void notifyNSDSFlowResponse(boolean z, String str, int i, int i2);

    /* access modifiers changed from: protected */
    public abstract void queueOperation(int i, Bundle bundle);

    public NSDSAppFlowBase(Looper looper, Context context) {
        super(looper);
        this.mContext = context;
    }

    public NSDSAppFlowBase(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mNSDSDatabaseHelper = databaseHelper;
        init();
    }

    private void init() {
        this.mSlotId = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        String str = null;
        this.mUserAgent = mnoNsdsStrategy == null ? null : mnoNsdsStrategy.getUserAgent();
        if (mnoNsdsStrategy != null) {
            str = mnoNsdsStrategy.getDeviceGroup(this.mSlotId);
        }
        this.mDeviceGroup = str;
    }

    /* access modifiers changed from: protected */
    public <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoNsdsStrategy() {
        return MnoNsdsStrategyCreator.getInstance(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()).getMnoStrategy();
    }

    /* access modifiers changed from: protected */
    public final void deferMessage(Message msg) {
        String str = LOG_TAG;
        IMSLog.i(str, "deferMessage: msg=" + msg.what);
        this.mDeferredMessages.add(Message.obtain(msg));
    }

    /* access modifiers changed from: protected */
    public int getHttpErrRespCode(Bundle bundleNSDSResponses) {
        int httpErrRespCode = -1;
        if (bundleNSDSResponses != null) {
            httpErrRespCode = bundleNSDSResponses.getInt(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_CODE, -1);
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getHttpErrRespCode: " + httpErrRespCode);
        return httpErrRespCode;
    }

    /* access modifiers changed from: protected */
    public String getHttpErrRespReason(Bundle bundleNSDSResponses) {
        String httpErrRespReason = null;
        if (bundleNSDSResponses != null) {
            httpErrRespReason = bundleNSDSResponses.getString(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_REASON, (String) null);
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getHttpErrRespReason: " + httpErrRespReason);
        return httpErrRespReason;
    }

    /* access modifiers changed from: protected */
    public void moveDeferredMessageAtFrontOfQueue() {
        for (int i = this.mDeferredMessages.size() - 1; i >= 0; i += -1) {
            Message curMsg = this.mDeferredMessages.get(i);
            IMSLog.i(LOG_TAG, "moveDeferredMessageAtFrontOfQueue: what = " + curMsg.what);
            sendMessageAtFrontOfQueue(curMsg);
        }
        this.mDeferredMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void clearDeferredMessage() {
        IMSLog.i(LOG_TAG, "clearDeferredMessage()");
        this.mDeferredMessages.clear();
    }

    public static void registerEventMessenger(Messenger evtMessenger) {
        synchronized (sEvtMessengers) {
            String str = LOG_TAG;
            IMSLog.i(str, "registerEventMessenger: " + sEvtMessengers.size());
            if (evtMessenger != null) {
                sEvtMessengers.add(evtMessenger);
            }
        }
    }

    public static void unregisterEventMessenger(Messenger evtMessenger) {
        synchronized (sEvtMessengers) {
            String str = LOG_TAG;
            IMSLog.i(str, "unregisterEventMessenger: " + sEvtMessengers.size());
            if (evtMessenger != null) {
                sEvtMessengers.remove(evtMessenger);
            }
        }
    }

    protected class NSDSResponseStatus {
        public int failedOperation;
        public String methodName;
        public int responseCode;

        public NSDSResponseStatus(int pResponseCode, String pMethodName, int pFailedOperation) {
            this.responseCode = pResponseCode;
            this.methodName = pMethodName;
            this.failedOperation = pFailedOperation;
        }
    }

    /* access modifiers changed from: protected */
    public void performNextOperationIf(int prevNsdsBaseOperation, NSDSResponseStatus nsdsResponseStatus, Bundle dataMap) {
        boolean status = false;
        if (getMnoNsdsStrategy() != null) {
            int nextOperation = getMnoNsdsStrategy().getNextOperation(this.mDeviceEventType, prevNsdsBaseOperation, nsdsResponseStatus.responseCode, dataMap);
            String str = LOG_TAG;
            IMSLog.i(str, "performNextOperationIf: nextOperation " + nextOperation);
            if (nextOperation == -1) {
                if (nsdsResponseStatus.responseCode == 1000) {
                    status = true;
                }
                notifyNSDSFlowResponse(status, nsdsResponseStatus.methodName, nsdsResponseStatus.failedOperation, nsdsResponseStatus.responseCode);
                return;
            }
            queueOperation(nextOperation, dataMap);
            return;
        }
        notifyNSDSFlowResponse(false, (String) null, -1, -1);
    }

    /* access modifiers changed from: protected */
    public void notifyCallbackForNsdsEvent(int eventType) {
        synchronized (sEvtMessengers) {
            IMSLog.i(LOG_TAG, "notifyCallbackForNsdsEvent: eventType=" + eventType + ":" + sEvtMessengers.size());
            for (int i = sEvtMessengers.size() - 1; i >= 0; i--) {
                try {
                    sEvtMessengers.get(i).send(obtainMessage(eventType));
                } catch (RemoteException e) {
                    IMSLog.s(LOG_TAG, "notifyCallbackForNsdsEvent: dead messenger, removed " + e.getMessage());
                    sEvtMessengers.remove(i);
                }
            }
        }
    }
}
