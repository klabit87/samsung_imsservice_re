package com.sec.internal.ims.servicemodules.options;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class OptionsRequestController extends Handler {
    private static final int EVENT_OPTIONS_EVENT = 3;
    private static final int EVENT_PROCESS_QUEUE = 1;
    private static final int EVENT_PUSH_REQUEST = 2;
    private static final int EVENT_PUSH_RESPONSE = 5;
    private static final int EVENT_SEND_CAPEX_RESPONSE_COMPLETE = 6;
    private static final int EVENT_SET_OWN_CAPABILITIES = 4;
    private static final String LOG_TAG = "OptionsReqController";
    private static final int MAX_OPTIONS_REQ = 15;
    private static final int OPTIONS_PROCESS_TIMEOUT = 30;
    private IOptionsEventListener mListener = null;
    private int mProcessingRequests = 0;
    private PhoneIdKeyMap<Integer> mRegistrationId = new PhoneIdKeyMap<>(SimUtil.getPhoneCount(), -1);
    private final CopyOnWriteArrayList<OptionsRequest> mRequestQueue = new CopyOnWriteArrayList<>();
    IOptionsServiceInterface mService = ImsRegistry.getHandlerFactory().getOptionsHandler();

    interface IOptionsEventListener {
        void onCapabilityUpdate(OptionsEvent optionsEvent);
    }

    public OptionsRequestController(Looper looper) {
        super(looper);
    }

    public void init() {
        this.mService.registerForOptionsEvent(this, 3, (Object) null);
    }

    public void registerOptionsEvent(IOptionsEventListener listener) {
        this.mListener = listener;
    }

    public void setImsRegistration(ImsRegistration reg) {
        Optional.ofNullable(reg).ifPresent(new Consumer(reg) {
            public final /* synthetic */ ImsRegistration f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                OptionsRequestController.this.lambda$setImsRegistration$0$OptionsRequestController(this.f$1, (ImsRegistration) obj);
            }
        });
    }

    public /* synthetic */ void lambda$setImsRegistration$0$OptionsRequestController(ImsRegistration reg, ImsRegistration r) {
        int phoneId = reg.getPhoneId();
        this.mRegistrationId.put(phoneId, Integer.valueOf(IRegistrationManager.getRegistrationInfoId(reg.getImsProfile().getId(), phoneId)));
        Log.i(LOG_TAG, "setImsRegistration: " + reg);
    }

    public void setImsDeRegistration(ImsRegistration reg) {
        if (reg != null) {
            int phoneId = reg.getPhoneId();
            Log.i(LOG_TAG, "setImsDeRegistration: clearing requests queue for phoneId: " + phoneId);
            Iterator<OptionsRequest> it = this.mRequestQueue.iterator();
            while (it.hasNext()) {
                OptionsRequest req = it.next();
                if (req.getPhoneId() == phoneId) {
                    this.mRequestQueue.remove(req);
                }
            }
            this.mRegistrationId.put(phoneId, -1);
            return;
        }
        this.mRequestQueue.clear();
        for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
            this.mRegistrationId.put(i, -1);
        }
    }

    public void setOwnCapabilities(long features, int phoneId) {
        sendMessage(obtainMessage(4, phoneId, 0, Long.valueOf(features)));
    }

    public boolean requestCapabilityExchange(ImsUri uri, long myFeatures, int phoneId, String extFeature) {
        IMSLog.s(LOG_TAG, phoneId, "requestCapabilityExchange: uri " + uri.toString() + Constants.IARI_ELT + extFeature);
        Bundle b = new Bundle();
        b.putParcelable(Constants.SIG_PROPERTY_URI_NAME, uri);
        b.putLong("FEATURES", myFeatures);
        b.putString("EXTFEATURE", extFeature);
        sendMessage(obtainMessage(2, phoneId, 0, b));
        return true;
    }

    public boolean sendCapexResponse(ImsUri uri, long myFeatures, String txId, int lastSeen, int phoneId, String extFeature) {
        IMSLog.s(LOG_TAG, "sendCapexResponse: uri " + uri.toString());
        Bundle b = new Bundle();
        b.putString("TXID", txId);
        b.putLong("FEATURES", myFeatures);
        b.putParcelable(Constants.SIG_PROPERTY_URI_NAME, uri);
        b.putInt("LASTSEEN", lastSeen);
        b.putString("EXTFEATURE", extFeature);
        sendMessage(obtainMessage(5, phoneId, 0, b));
        return true;
    }

    private void onRequestCapabilityExchange(ImsUri uri, long myFeatures, String extFeature, int phoneId) {
        OptionsRequest req = findOptionsRequest(uri, phoneId);
        if (req != null) {
            long diff = new Date().getTime() - req.getTimestamp().getTime();
            if (diff > 30000) {
                Log.i(LOG_TAG, "onRequestCapabilityExchange: options timeout diff = " + diff + " ms, set failed");
                failedRequest(req);
            } else {
                Log.i(LOG_TAG, "onRequestCapabilityExchange: myFeatures :" + myFeatures + "req.getMyFeatures()" + req.getMyFeatures());
                if (myFeatures == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || req.getMyFeatures() == myFeatures) {
                    return;
                }
            }
        }
        this.mRequestQueue.add(new OptionsRequest(uri, myFeatures, phoneId, extFeature));
        sendEmptyMessage(1);
    }

    private void OnSendCapexResponse(ImsUri uri, long myFeatures, String txID, int lastSeen, int phoneId, String extFeature) {
        OptionsRequest optionsReq = new OptionsRequest(uri, myFeatures, phoneId, extFeature);
        optionsReq.setIncoming(true);
        IMSLog.s(LOG_TAG, "OnSendCapexResponse: txID " + txID);
        optionsReq.setTxId(txID);
        optionsReq.setLastSeen(lastSeen);
        optionsReq.setExtFeature(extFeature);
        this.mRequestQueue.add(optionsReq);
        sendEmptyMessage(1);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x004a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void process() {
        /*
            r20 = this;
            r0 = r20
            java.lang.String r1 = "OptionsReqController"
            java.lang.String r2 = "process requestQueue."
            android.util.Log.i(r1, r2)
            java.util.concurrent.CopyOnWriteArrayList<com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest> r1 = r0.mRequestQueue
            int r1 = r1.size()
            if (r1 != 0) goto L_0x0013
            return
        L_0x0013:
            java.util.concurrent.CopyOnWriteArrayList<com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest> r1 = r0.mRequestQueue
            java.util.Iterator r1 = r1.iterator()
        L_0x0019:
            java.lang.Object r2 = r1.next()
            com.sec.internal.ims.servicemodules.options.OptionsRequestController$OptionsRequest r2 = (com.sec.internal.ims.servicemodules.options.OptionsRequestController.OptionsRequest) r2
            int r3 = r2.getState()
            r4 = 1
            if (r3 == r4) goto L_0x0074
            int r3 = r2.getState()
            r5 = 2
            if (r3 != r5) goto L_0x002e
            goto L_0x0074
        L_0x002e:
            boolean r3 = r2.isIncoming()
            if (r3 != 0) goto L_0x004a
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r5 = r0.mService
            com.sec.ims.util.ImsUri r6 = r2.getUri()
            long r7 = r2.getMyFeatures()
            int r9 = r2.getPhoneId()
            java.lang.String r10 = r2.getExtFeature()
            r5.requestCapabilityExchange(r6, r7, r9, r10)
            goto L_0x006c
        L_0x004a:
            com.sec.internal.ims.servicemodules.options.IOptionsServiceInterface r11 = r0.mService
            com.sec.ims.util.ImsUri r12 = r2.getUri()
            long r13 = r2.getMyFeatures()
            java.lang.String r15 = r2.getTxId()
            int r16 = r2.getLastSeen()
            r3 = 6
            android.os.Message r17 = r0.obtainMessage(r3, r2)
            int r18 = r2.getPhoneId()
            java.lang.String r19 = r2.getExtFeature()
            r11.sendCapexResponse(r12, r13, r15, r16, r17, r18, r19)
        L_0x006c:
            r2.setState(r4)
            int r3 = r0.mProcessingRequests
            int r3 = r3 + r4
            r0.mProcessingRequests = r3
        L_0x0074:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0080
            int r2 = r0.mProcessingRequests
            r3 = 15
            if (r2 < r3) goto L_0x0019
        L_0x0080:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.OptionsRequestController.process():void");
    }

    private OptionsRequest findOptionsRequest(ImsUri uri, int phoneId) {
        return findRequest(uri, -1, phoneId);
    }

    private OptionsRequest findRequest(ImsUri uri, int state, int phoneId) {
        Iterator<OptionsRequest> it = this.mRequestQueue.iterator();
        while (it.hasNext()) {
            OptionsRequest req = it.next();
            if (req != null && req.getPhoneId() == phoneId && UriUtil.equals(req.getUri(), uri)) {
                if (state < 0 || req.getState() == state) {
                    return req;
                }
            }
        }
        return null;
    }

    private void failedRequest(OptionsRequest req) {
        IMSLog.s(LOG_TAG, "failedRequest: uri " + req.getUri());
        this.mRequestQueue.remove(req);
        req.setState(3);
        this.mProcessingRequests = this.mProcessingRequests + -1;
    }

    private void completeRequest(OptionsRequest req) {
        IMSLog.s(LOG_TAG, "completeRequest: uri " + req.getUri());
        this.mRequestQueue.remove(req);
        req.setState(2);
        this.mProcessingRequests = this.mProcessingRequests + -1;
    }

    private void onOptionsEvent(AsyncResult ret) {
        IRegistrationGovernor governor;
        OptionsEvent event = (OptionsEvent) ret.result;
        int phoneId = event.getPhoneId();
        if (this.mRegistrationId.get(phoneId).intValue() == -1) {
            Log.i(LOG_TAG, "onOptionsEvent: registration is null. fail.");
            return;
        }
        try {
            int mHandle = ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId.get(phoneId).intValue()).getHandle();
            ImsUri uri = event.getUri();
            IMSLog.s(LOG_TAG, "onOptionsEvent: event " + event);
            IMSLog.s(LOG_TAG, "onOptionsEvent: mRegistrationId " + this.mRegistrationId);
            IMSLog.s(LOG_TAG, "onOptionsEvent: getHandle() " + mHandle);
            IMSLog.s(LOG_TAG, "onOptionsEvent: event.getSessionId() " + event.getSessionId());
            if (event.isResponse()) {
                if (event.getReason() == OptionsEvent.OptionsFailureReason.FORBIDDEN_403 && (governor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(mHandle)) != null) {
                    Log.i(LOG_TAG, "403 forbidden response w/o warning header");
                    governor.onSipError("options", new SipError(403, "Forbidden"));
                }
                OptionsRequest req = findRequest(uri, 1, phoneId);
                if (req != null) {
                    completeRequest(req);
                }
                if (uri != null && uri.getUriType() == ImsUri.UriType.TEL_URI && (event.getFeatures() & Capabilities.FEATURE_CHATBOT_ROLE) == Capabilities.FEATURE_CHATBOT_ROLE) {
                    Iterator<ImsUri> it = event.getPAssertedIdSet().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ImsUri pAssertedIdentity = it.next();
                        if (pAssertedIdentity.getUriType() == ImsUri.UriType.SIP_URI) {
                            BotServiceIdTranslator.getInstance().register(uri.getMsisdn(), pAssertedIdentity.toString());
                            break;
                        }
                    }
                }
            } else if (!UriUtil.hasMsisdnNumber(uri) && (event.getFeatures() & Capabilities.FEATURE_CHATBOT_ROLE) != Capabilities.FEATURE_CHATBOT_ROLE) {
                return;
            } else {
                if (mHandle != event.getSessionId()) {
                    IMSLog.s(LOG_TAG, "onOptionsEvent: event.getSessionId()!= event.getSessionId()");
                    return;
                }
            }
            IOptionsEventListener iOptionsEventListener = this.mListener;
            if (iOptionsEventListener != null) {
                iOptionsEventListener.onCapabilityUpdate(event);
            }
            process();
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "onOptionsEvent: getRegistrationInfo is Null" + e.getMessage() + " mRegistrationId: " + this.mRegistrationId);
        }
    }

    private void handleSendCapexResponseComplete(AsyncResult ret) {
        OptionsRequest req = (OptionsRequest) ret.userObj;
        if (req != null) {
            Log.i(LOG_TAG, "onOptionsEvent: handleSendCapexResponseComplete." + req.getTxId() + req.getState() + req.getTimestamp());
            completeRequest(req);
        }
    }

    private void handleSetOwnCapabilities(long features, int phoneId) {
        this.mService.setOwnCapabilities(features, phoneId);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                process();
                return;
            case 2:
                Bundle b = (Bundle) msg.obj;
                onRequestCapabilityExchange((ImsUri) b.get(Constants.SIG_PROPERTY_URI_NAME), ((Long) b.get("FEATURES")).longValue(), (String) b.get("EXTFEATURE"), msg.arg1);
                return;
            case 3:
                onOptionsEvent((AsyncResult) msg.obj);
                return;
            case 4:
                handleSetOwnCapabilities(((Long) msg.obj).longValue(), msg.arg1);
                return;
            case 5:
                Bundle b1 = (Bundle) msg.obj;
                OnSendCapexResponse((ImsUri) b1.get(Constants.SIG_PROPERTY_URI_NAME), ((Long) b1.get("FEATURES")).longValue(), (String) b1.get("TXID"), ((Integer) b1.get("LASTSEEN")).intValue(), msg.arg1, (String) b1.get("EXTFEATURE"));
                return;
            case 6:
                handleSendCapexResponseComplete((AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private static class OptionsRequest {
        static final int DONE = 2;
        static final int FAILED = 3;
        static final int INIT = 0;
        static final int REQUESTED = 1;
        private boolean isIncoming = false;
        private int lastSeen;
        private String mExtFeature;
        private final long mMyFeatures;
        private int mPhoneId;
        private int mState;
        private Date mTimestamp;
        private final ImsUri mUri;
        private String txId = null;

        OptionsRequest(ImsUri uri, long myFeatures, int phoneId, String extFeature) {
            this.mUri = uri;
            this.mMyFeatures = myFeatures;
            this.mState = 0;
            this.mTimestamp = new Date();
            this.mPhoneId = phoneId;
            this.lastSeen = -1;
            this.mExtFeature = extFeature;
        }

        /* access modifiers changed from: package-private */
        public void setState(int state) {
            this.mState = state;
        }

        /* access modifiers changed from: package-private */
        public int getState() {
            return this.mState;
        }

        /* access modifiers changed from: package-private */
        public ImsUri getUri() {
            return this.mUri;
        }

        /* access modifiers changed from: package-private */
        public int getPhoneId() {
            return this.mPhoneId;
        }

        /* access modifiers changed from: package-private */
        public long getMyFeatures() {
            return this.mMyFeatures;
        }

        /* access modifiers changed from: package-private */
        public Date getTimestamp() {
            return this.mTimestamp;
        }

        /* access modifiers changed from: package-private */
        public boolean isIncoming() {
            return this.isIncoming;
        }

        /* access modifiers changed from: package-private */
        public String getExtFeature() {
            return this.mExtFeature;
        }

        /* access modifiers changed from: package-private */
        public void setIncoming(boolean incoming) {
            this.isIncoming = incoming;
        }

        /* access modifiers changed from: package-private */
        public String getTxId() {
            return this.txId;
        }

        /* access modifiers changed from: package-private */
        public void setTxId(String txId2) {
            this.txId = txId2;
        }

        /* access modifiers changed from: package-private */
        public int getLastSeen() {
            return this.lastSeen;
        }

        /* access modifiers changed from: package-private */
        public void setLastSeen(int lastSeen2) {
            this.lastSeen = lastSeen2;
        }

        /* access modifiers changed from: package-private */
        public void setExtFeature(String extFeature) {
            this.mExtFeature = extFeature;
        }
    }
}
