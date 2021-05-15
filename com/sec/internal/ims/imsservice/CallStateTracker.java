package com.sec.internal.ims.imsservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.ServiceModuleManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallStateTracker implements ISequentialInitializable {
    public static final int CALL_CONNECTED = 2;
    public static final int CALL_DISCONECTED = 1;
    public static final int CALL_RESUMED = 4;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CallStateTracker.class.getSimpleName();
    /* access modifiers changed from: private */
    public final Map<Integer, Map<String, Call>> mCallLists = new HashMap();
    private final BroadcastReceiver mCallStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Map<String, Integer> callcounts;
            Map<String, Call> calls;
            String telNo;
            String normalizedNumber;
            Intent intent2 = intent;
            Log.i(CallStateTracker.LOG_TAG, "Received intent: " + intent.getAction() + " extra: " + intent.getExtras());
            int callEvent = intent2.getIntExtra(ImsConstants.Intents.EXTRA_CALL_EVENT, -1);
            String telNo2 = intent2.getStringExtra(ImsConstants.Intents.EXTRA_TEL_NUMBER);
            int phoneId = intent2.getIntExtra(ImsConstants.Intents.EXTRA_PHONE_ID, -1);
            boolean isIncoming = intent2.getBooleanExtra(ImsConstants.Intents.EXTRA_IS_INCOMING, false);
            boolean isCmcConnected = intent2.getBooleanExtra(ImsConstants.Intents.EXTRA_IS_CMC_CONNECTED, false);
            boolean isCmcCall = intent2.getBooleanExtra(ImsConstants.Intents.EXTRA_IS_CMC_CALL, false);
            Log.i(CallStateTracker.LOG_TAG, "Received call event: " + callEvent + ", phoneId: " + phoneId + ", isCmcConnected: " + isCmcConnected + ", isCmcCall: " + isCmcCall);
            if (!SimUtil.isValidSimSlot(phoneId)) {
                Log.d(CallStateTracker.LOG_TAG, "Invalid phoneId - Ignore");
                return;
            }
            Map<String, Call> calls2 = new ArrayMap<>();
            Map<String, Integer> callcounts2 = new ArrayMap<>();
            if (CallStateTracker.this.mCallLists.containsKey(Integer.valueOf(phoneId))) {
                calls = (Map) CallStateTracker.this.mCallLists.get(Integer.valueOf(phoneId));
                callcounts = (Map) CallStateTracker.this.mCountLists.get(Integer.valueOf(phoneId));
            } else {
                calls = calls2;
                callcounts = callcounts2;
            }
            if (telNo2 != null) {
                String telNo3 = telNo2.trim();
                Log.i(CallStateTracker.LOG_TAG, "Tel Number length " + telNo3.length());
                if (telNo3.isEmpty()) {
                    telNo = null;
                } else {
                    telNo = telNo3;
                }
            } else {
                telNo = telNo2;
            }
            UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(phoneId);
            ImsUri uri = uriGenerator.getNormalizedUri(telNo, true);
            if (uri != null) {
                normalizedNumber = uri.getMsisdn();
            } else if (!isCmcCall || telNo == null) {
                normalizedNumber = null;
            } else {
                normalizedNumber = telNo;
            }
            if (normalizedNumber == null) {
                UriGenerator uriGenerator2 = uriGenerator;
                String str = telNo;
            } else if (callEvent == 1) {
                int callstate = ((TelephonyManager) CallStateTracker.this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).getCallStateForSlot(phoneId);
                int count = callcounts.getOrDefault(normalizedNumber, 0).intValue() - 1;
                if (count < 1 || callstate == 0) {
                    calls.remove(normalizedNumber);
                    callcounts.remove(normalizedNumber);
                } else {
                    callcounts.put(normalizedNumber, Integer.valueOf(count));
                }
                String str2 = normalizedNumber;
                UriGenerator uriGenerator3 = uriGenerator;
                String str3 = telNo;
            } else {
                Call call = r4;
                UriGenerator uriGenerator4 = uriGenerator;
                String str4 = telNo;
                Call call2 = new Call(callEvent, normalizedNumber, isIncoming, isCmcConnected, isCmcCall);
                String normalizedNumber2 = normalizedNumber;
                calls.put(normalizedNumber2, call);
                if (callEvent == 2) {
                    callcounts.put(normalizedNumber2, Integer.valueOf(callcounts.getOrDefault(normalizedNumber2, 0).intValue() + 1));
                }
            }
            CallStateTracker.this.mCallLists.put(Integer.valueOf(phoneId), calls);
            CallStateTracker.this.mCountLists.put(Integer.valueOf(phoneId), callcounts);
            List<ICall> callList = new ArrayList<>(calls.values());
            for (Listener listener : CallStateTracker.this.mListeners) {
                listener.onCallStateChanged(callList, phoneId);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Map<Integer, Map<String, Integer>> mCountLists = new HashMap();
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public final List<Listener> mListeners = new ArrayList();
    /* access modifiers changed from: private */
    public final ServiceModuleManager mServiceModuleManager;

    public static abstract class Listener {
        /* access modifiers changed from: protected */
        public abstract void onCallStateChanged(List<ICall> list, int i);
    }

    static class Call implements ICall {
        public final boolean mIsCmcCall;
        public final boolean mIsCmcConnected;
        public final boolean mIsIncoming;
        public final String mNumber;
        public final int mState;

        Call(int state, String number, boolean isIncoming, boolean isCmcConnected, boolean isCmcCall) {
            this.mState = state;
            this.mNumber = number;
            this.mIsIncoming = isIncoming;
            this.mIsCmcConnected = isCmcConnected;
            this.mIsCmcCall = isCmcCall;
        }

        public String toString() {
            return "Call{mState=" + this.mState + ", mNumber='" + this.mNumber + '\'' + ", mIsIncoming=" + this.mIsIncoming + ", mIsCmcConnected=" + this.mIsCmcConnected + ", mIsCmcCall=" + this.mIsCmcCall + '}';
        }

        public boolean isConnected() {
            int i = this.mState;
            return i == 2 || i == 4;
        }

        public boolean isCmcConnected() {
            return this.mIsCmcConnected;
        }

        public boolean isCmcCall() {
            return this.mIsCmcCall;
        }

        public String getNumber() {
            return this.mNumber;
        }
    }

    public CallStateTracker(Context context, Handler handler, ServiceModuleManager smm) {
        this.mContext = context;
        this.mHandler = handler;
        this.mServiceModuleManager = smm;
    }

    public void initSequentially() {
        IntentFilter callStateIntentFilter = new IntentFilter();
        callStateIntentFilter.addAction(ImsConstants.Intents.ACTION_CALL_STATE_CHANGED);
        this.mContext.registerReceiver(this.mCallStateReceiver, callStateIntentFilter, (String) null, this.mHandler);
        register(new Listener() {
            /* access modifiers changed from: protected */
            public void onCallStateChanged(List<ICall> calls, int phoneId) {
                CallStateTracker.this.mServiceModuleManager.notifyCallStateChanged(calls, phoneId);
            }
        });
    }

    public void register(Listener listener) {
        this.mListeners.add(listener);
    }

    public void unregister(Listener listener) {
        this.mListeners.remove(listener);
    }
}
