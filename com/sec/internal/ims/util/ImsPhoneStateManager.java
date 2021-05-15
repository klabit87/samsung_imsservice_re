package com.sec.internal.ims.util;

import android.content.Context;
import android.content.IntentFilter;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.PreciseDataConnectionState;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImsPhoneStateManager {
    private static String INTENT_ACTION_DEFAULT_DATA_SUB_CHANGED = ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED;
    private static String INTENT_ACTION_SUBINFO_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    /* access modifiers changed from: private */
    public static String LOG_TAG = ImsPhoneStateManager.class.getSimpleName();
    private Context mContext;
    private int mListenEvent = 0;
    /* access modifiers changed from: private */
    public Map<Integer, PhoneStateListener> mListener;
    private List<PhoneStateListenerInternal> mPhoneStateListenerInternal;
    private SubscriptionManager mSubManager;
    private TelephonyManager mTelephonyManager;

    public ImsPhoneStateManager(Context context, int event) {
        this.mContext = context;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY);
        this.mSubManager = SubscriptionManager.from(context);
        this.mListenEvent = event;
        this.mListener = new HashMap();
        this.mPhoneStateListenerInternal = new ArrayList();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_SUBINFO_UPDATED);
        intentFilter.addAction(INTENT_ACTION_DEFAULT_DATA_SUB_CHANGED);
    }

    public void registerListener(PhoneStateListener listener) {
        registerListener(listener, SubscriptionManager.getDefaultDataSubscriptionId(), Extensions.SubscriptionManager.getDefaultDataPhoneId(this.mSubManager));
    }

    public void registerListener(PhoneStateListener listener, int subId, int phoneId) {
        Log.d(LOG_TAG + "[" + phoneId + "](" + subId + ")", "registerListener:");
        this.mListener.put(Integer.valueOf(phoneId), listener);
        registerPhoneStateListenerInternal(subId, phoneId);
    }

    public void unRegisterListener(int phoneId) {
        unRegisterPhoneStateListenerInternal(phoneId);
        this.mListener.remove(Integer.valueOf(phoneId));
    }

    public boolean hasListener(int phoneId) {
        return !CollectionUtils.isNullOrEmpty((Map<?, ?>) this.mListener) && this.mListener.containsKey(Integer.valueOf(phoneId));
    }

    private void registerPhoneStateListenerInternal(int subId, int phoneId) {
        PhoneStateListenerInternal psli = new PhoneStateListenerInternal(subId, phoneId);
        this.mPhoneStateListenerInternal.add(psli);
        if (this.mTelephonyManager.createForSubscriptionId(subId) != null) {
            IMSLog.d(LOG_TAG, phoneId, "registerPhoneStateListenerInternal:");
            this.mTelephonyManager.createForSubscriptionId(subId).listen(psli, this.mListenEvent);
        }
    }

    private void unRegisterPhoneStateListenerInternal(int phoneId) {
        if (getPhoneStateListenerInternal(phoneId) == null) {
            String str = LOG_TAG;
            Log.d(str, "unRegisterPhoneStateListenerInternal, phoneStateListenerInternal[" + phoneId + "] is not exist. return..");
            return;
        }
        PhoneStateListenerInternal psli = getPhoneStateListenerInternal(phoneId);
        if (this.mTelephonyManager.createForSubscriptionId(psli.getSubId()) != null) {
            IMSLog.d(LOG_TAG, phoneId, "registerPhoneStateListenerInternal:");
            this.mTelephonyManager.createForSubscriptionId(psli.getSubId()).listen(psli, 0);
        }
        this.mPhoneStateListenerInternal.remove(psli);
    }

    private PhoneStateListenerInternal getPhoneStateListenerInternal(int phoneId) {
        for (PhoneStateListenerInternal psli : this.mPhoneStateListenerInternal) {
            if (psli.getSimSlot() == phoneId) {
                return psli;
            }
        }
        return null;
    }

    private class PhoneStateListenerInternal extends PhoneStateListener {
        int mSimSlot;
        int mSubId;

        public PhoneStateListenerInternal(int subId, int phoneId) {
            this.mSimSlot = phoneId;
            this.mSubId = subId;
        }

        public int getSimSlot() {
            return this.mSimSlot;
        }

        public int getSubId() {
            return this.mSubId;
        }

        private boolean isImsSlot() {
            return true;
        }

        public void onCallForwardingIndicatorChanged(boolean cfi) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onCallForwardingIndicatorChanged(cfi);
            }
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onCallStateChanged(state, incomingNumber);
            }
        }

        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onCellInfoChanged(cellInfo);
            }
        }

        public void onCellLocationChanged(CellLocation location) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onCellLocationChanged(location);
            }
        }

        public void onDataActivity(int direction) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onDataActivity(direction);
            }
        }

        public void onDataConnectionStateChanged(int state, int networkType) {
            IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onDataConnectionStateChanged(s, n) E");
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onDataConnectionStateChanged(s, n) X");
                listener.onDataConnectionStateChanged(state, networkType);
            }
        }

        public void onDataConnectionStateChanged(int state) {
            IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onDataConnectionStateChanged(s) E");
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onDataConnectionStateChanged(s) X");
                listener.onDataConnectionStateChanged(state);
            }
        }

        public void onMessageWaitingIndicatorChanged(boolean mwi) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onMessageWaitingIndicatorChanged(mwi);
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onServiceStateChanged E");
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                IMSLog.d(ImsPhoneStateManager.LOG_TAG, this.mSimSlot, "onServiceStateChanged X");
                listener.onServiceStateChanged(serviceState);
            }
        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onSignalStrengthsChanged(signalStrength);
            }
        }

        public void onSrvccStateChanged(int srvccState) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onSrvccStateChanged(srvccState);
            }
        }

        public void onPreciseDataConnectionStateChanged(PreciseDataConnectionState state) {
            PhoneStateListener listener = (PhoneStateListener) ImsPhoneStateManager.this.mListener.get(Integer.valueOf(this.mSimSlot));
            if (isImsSlot() && listener != null) {
                listener.onPreciseDataConnectionStateChanged(state);
            }
        }
    }
}
