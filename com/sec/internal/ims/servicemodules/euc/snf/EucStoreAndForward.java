package com.sec.internal.ims.servicemodules.euc.snf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EucStoreAndForward extends Handler implements IEucStoreAndForward {
    private static final int EVENT_SEND_RESPONSE_RESPONSE = 1;
    private static final String LOG_TAG = EucStoreAndForward.class.getSimpleName();
    private IEucServiceInterface mEucService;
    private Set<String> mOwnIdentitiesInForwardState = new HashSet();
    /* access modifiers changed from: private */
    public List<IEucStoreAndForwardResponseData> storedResponses = new LinkedList();

    public EucStoreAndForward(IEucServiceInterface eucService, Looper looper) {
        super(looper);
        this.mEucService = eucService;
    }

    public IEucStoreAndForward.IResponseHandle sendResponse(IEucData euc, EucResponseData.Response response, IEucStoreAndForward.IResponseCallback callback) {
        AnonymousClass1 r0 = new IEucStoreAndForward.IResponseHandle() {
            public void invalidate() {
                Iterator<IEucStoreAndForwardResponseData> iterator = EucStoreAndForward.this.storedResponses.iterator();
                while (iterator.hasNext()) {
                    if (this == iterator.next().getResponseToWorkflowHandle()) {
                        iterator.remove();
                    }
                }
            }
        };
        this.storedResponses.add(createEUCStoreAndForwardResponseData(euc, response, (String) null, callback, r0));
        if (this.mOwnIdentitiesInForwardState.contains(euc.getOwnIdentity())) {
            this.mEucService.sendEucResponse(new EucResponseData(euc.getId(), euc.getType(), (String) null, euc.getRemoteUri(), euc.getOwnIdentity(), response, obtainMessage(1)));
        }
        return r0;
    }

    public IEucStoreAndForward.IResponseHandle sendResponse(IEucData euc, EucResponseData.Response response, String pin, IEucStoreAndForward.IResponseCallback callback) {
        AnonymousClass2 r0 = new IEucStoreAndForward.IResponseHandle() {
            public void invalidate() {
                Iterator<IEucStoreAndForwardResponseData> iterator = EucStoreAndForward.this.storedResponses.iterator();
                while (iterator.hasNext()) {
                    if (this == iterator.next().getResponseToWorkflowHandle()) {
                        iterator.remove();
                    }
                }
            }
        };
        this.storedResponses.add(createEUCStoreAndForwardResponseData(euc, response, pin, callback, r0));
        if (this.mOwnIdentitiesInForwardState.contains(euc.getOwnIdentity())) {
            this.mEucService.sendEucResponse(new EucResponseData(euc.getId(), euc.getType(), pin, euc.getRemoteUri(), euc.getOwnIdentity(), response, obtainMessage(1)));
        }
        return r0;
    }

    public void store(String ownIdentity) {
        this.mOwnIdentitiesInForwardState.remove(ownIdentity);
        String str = LOG_TAG;
        Log.i(str, "state for ownIdentity = " + IMSLog.checker(ownIdentity) + " set to STORE");
    }

    public void forward(String ownIdentity) {
        this.mOwnIdentitiesInForwardState.add(ownIdentity);
        String str = LOG_TAG;
        Log.i(str, "state for ownIdentity = " + IMSLog.checker(ownIdentity) + " set to FORWARD");
        for (IEucStoreAndForwardResponseData responseData : this.storedResponses) {
            IEucData eucData = responseData.getEUCData();
            if (eucData.getOwnIdentity().equals(ownIdentity)) {
                this.mEucService.sendEucResponse(new EucResponseData(eucData.getId(), eucData.getType(), responseData.getPIN(), eucData.getRemoteUri(), eucData.getOwnIdentity(), responseData.getResponse(), obtainMessage(1)));
            }
        }
    }

    private IEucStoreAndForwardResponseData createEUCStoreAndForwardResponseData(IEucData eucData, EucResponseData.Response response, String pin, IEucStoreAndForward.IResponseCallback callback, IEucStoreAndForward.IResponseHandle responseToWorkflowHandle) {
        final IEucData iEucData = eucData;
        final EucResponseData.Response response2 = response;
        final String str = pin;
        final IEucStoreAndForward.IResponseCallback iResponseCallback = callback;
        final IEucStoreAndForward.IResponseHandle iResponseHandle = responseToWorkflowHandle;
        return new IEucStoreAndForwardResponseData() {
            public IEucData getEUCData() {
                return iEucData;
            }

            public EucResponseData.Response getResponse() {
                return response2;
            }

            public String getPIN() {
                return str;
            }

            public IEucStoreAndForward.IResponseCallback getCallback() {
                return iResponseCallback;
            }

            public IEucStoreAndForward.IResponseHandle getResponseToWorkflowHandle() {
                return iResponseHandle;
            }
        };
    }

    public void handleMessage(Message msg) {
        if (1 == msg.what) {
            EucSendResponseStatus responseStatus = (EucSendResponseStatus) ((AsyncResult) msg.obj).result;
            IEucStoreAndForwardResponseData eucResponseToBeRemoved = null;
            for (IEucStoreAndForwardResponseData storedData : this.storedResponses) {
                if (storedData.getEUCData().getKey().equals(responseStatus.getKey()) && eucResponseToBeRemoved == null) {
                    eucResponseToBeRemoved = storedData;
                }
            }
            if (eucResponseToBeRemoved != null) {
                eucResponseToBeRemoved.getCallback().onStatus(responseStatus);
                this.storedResponses.remove(eucResponseToBeRemoved);
                return;
            }
            return;
        }
        Log.e(LOG_TAG, "handleMessage: Undefined message, ignoring!");
    }
}
