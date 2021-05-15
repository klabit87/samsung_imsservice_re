package com.sec.internal.ims.cmstore.ambs.provision;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;

public class AmbsPhoneStateListener {
    /* access modifiers changed from: private */
    public static final String TAG = AmbsPhoneStateListener.class.getSimpleName();
    /* access modifiers changed from: private */
    public static boolean mZcodeRequested = false;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public IControllerCommonInterface mIControllerCommonInterface;
    /* access modifiers changed from: private */
    public boolean mIsPhoneServiceReady = false;
    private PhoneStateListener mServiceStateListener = null;
    private TelephonyManager mTelephonyManager;

    AmbsPhoneStateListener(TelephonyManager telephonyManager, IControllerCommonInterface controllerInterface, Context context) {
        this.mTelephonyManager = telephonyManager;
        this.mIControllerCommonInterface = controllerInterface;
        this.mContext = context;
    }

    public void startListen() {
        createPhoneServiceListener();
        resumeListen();
    }

    public void stopListen() {
        PhoneStateListener phoneStateListener = this.mServiceStateListener;
        if (phoneStateListener != null) {
            this.mTelephonyManager.listen(phoneStateListener, 0);
        } else {
            Log.d(TAG, "Phone state listener was not initial, maybe provison started form the latest failed api. No need to close it.");
        }
    }

    public void resumeListen() {
        mZcodeRequested = false;
        this.mTelephonyManager.listen(this.mServiceStateListener, 1);
    }

    private void createPhoneServiceListener() {
        this.mServiceStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState serviceState) {
                String access$000 = AmbsPhoneStateListener.TAG;
                Log.v(access$000, "onServiceStateChanged " + serviceState.getState());
                boolean unused = AmbsPhoneStateListener.this.mIsPhoneServiceReady = serviceState.getState() == 0 || Util.isWifiCallingEnabled(AmbsPhoneStateListener.this.mContext);
                if (AmbsPhoneStateListener.this.mIsPhoneServiceReady && !AmbsPhoneStateListener.mZcodeRequested) {
                    AmbsPhoneStateListener.this.mIControllerCommonInterface.update(EnumProvision.ProvisionEventType.REQ_AUTH_ZCODE.getId());
                    boolean unused2 = AmbsPhoneStateListener.mZcodeRequested = true;
                }
            }
        };
    }
}
