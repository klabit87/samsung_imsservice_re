package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.interfaces.ims.core.ISimEventListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;

public class TelephonyAdapterPrimaryDeviceRjil extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceRjil.class.getSimpleName();
    protected SimEventListener mSimEventListener;

    public TelephonyAdapterPrimaryDeviceRjil(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        this.mSmsReceiver = new SmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void registerSimEventListener() {
        this.mSimEventListener = new SimEventListener();
        if (this.mSimManager != null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "register SIM event listener");
            this.mSimManager.registerForSimRefresh(this, 101, (Object) null);
            this.mSimManager.registerForSimRemoved(this, 101, (Object) null);
            this.mSimManager.registerSimCardEventListener(this.mSimEventListener);
            this.mSubId = this.mSimManager.getSubscriptionId();
        }
        if (this.mTelephony.getPhoneCount() > 1 && SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            SimManagerFactory.registerForDDSChange(this, 100, (Object) null);
        }
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 1) {
            handleReceivedDataSms(msg, false, false);
        } else if (i2 == 3) {
            handleOtpTimeout(false);
        } else if (i2 == 100) {
            ISimManager sm = SimManagerFactory.getSimManager();
            if (sm != null) {
                String str2 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str2, i3, "EVENT_DDS_CHANGED sim state = " + sm.getSimState());
                if (sm.getSimState() == 5 && !(this.mState instanceof ReadyState)) {
                    this.mState = new ReadyState();
                }
            }
        } else if (i2 == 101) {
            int simState = this.mTelephony.getSimState(this.mPhoneId);
            String str3 = LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.i(str3, i4, "EVENT_SIM_REMOVED  SIM state" + simState);
            if (1 == simState && !(this.mState instanceof AbsentState)) {
                this.mState = new AbsentState();
            }
        }
    }

    protected class SimEventListener implements ISimEventListener {
        protected SimEventListener() {
        }

        public void onReady(int subId, boolean absent) {
            int simState = TelephonyAdapterPrimaryDeviceRjil.this.mTelephony.getSimState(TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId);
            String access$000 = TelephonyAdapterPrimaryDeviceRjil.LOG_TAG;
            int i = TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId;
            IMSLog.i(access$000, i, "onSimStateChanged: " + simState + "absent" + absent);
            if (5 == simState) {
                if (!(TelephonyAdapterPrimaryDeviceRjil.this.mState instanceof ReadyState)) {
                    TelephonyAdapterPrimaryDeviceRjil.this.mState = new ReadyState();
                }
            } else if (!(TelephonyAdapterPrimaryDeviceRjil.this.mState instanceof TelephonyAdapterPrimaryDeviceBase.IdleState)) {
                TelephonyAdapterPrimaryDeviceRjil.this.mState = new TelephonyAdapterPrimaryDeviceBase.IdleState();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void getState(String state) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getState: change to " + state);
        if (TelephonyAdapterState.READY_STATE.equals(state)) {
            this.mState = new ReadyState();
        } else if (TelephonyAdapterState.ABSENT_STATE.equals(state)) {
            this.mState = new AbsentState();
        } else {
            super.getState(state);
        }
    }

    protected class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        public ReadyState() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "TelephonyAdapter:ready state");
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceRjil.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceRjil.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceRjil.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    TelephonyAdapterPrimaryDeviceRjil.this.mOtp = null;
                    TelephonyAdapterPrimaryDeviceRjil.this.mOtpReceivedTime = 0;
                    e.printStackTrace();
                }
                TelephonyAdapterPrimaryDeviceRjil.this.removeMessages(3);
                String access$000 = TelephonyAdapterPrimaryDeviceRjil.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId;
                IMSLog.i(access$000, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceRjil.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceRjil.this.mOtp;
        }

        public void registerUneregisterForOTP(boolean val) {
            if (val) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "registerUneregisterForOTP");
                if (TelephonyAdapterPrimaryDeviceRjil.this.mModuleHandler != null) {
                    TelephonyAdapterPrimaryDeviceRjil.this.mContext.registerReceiver(TelephonyAdapterPrimaryDeviceRjil.this.mSmsReceiver, TelephonyAdapterPrimaryDeviceRjil.this.mSmsReceiver.getIntentFilter());
                } else {
                    IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "disable SMS receiver");
                }
            } else if (TelephonyAdapterPrimaryDeviceRjil.this.mModuleHandler != null && TelephonyAdapterPrimaryDeviceRjil.this.mSmsReceiver != null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "unregister mSmsReceiver");
                TelephonyAdapterPrimaryDeviceRjil.this.mContext.unregisterReceiver(TelephonyAdapterPrimaryDeviceRjil.this.mSmsReceiver);
                TelephonyAdapterPrimaryDeviceRjil.this.mOtp = null;
            }
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        ImsProfile mImsProfile = null;

        public AbsentState() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "TelephonyAdapter:Absent state");
            List<ImsProfile> profile = ImsProfileLoaderInternal.getProfileList(TelephonyAdapterPrimaryDeviceRjil.this.mContext, SimManagerFactory.getSimManager().getSimSlotIndex());
            if (profile == null || profile.size() <= 0 || profile.get(0) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "AbsentState : no ImsProfile loaded");
            } else {
                this.mImsProfile = profile.get(0);
            }
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceRjil.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceRjil.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceRjil.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String access$000 = TelephonyAdapterPrimaryDeviceRjil.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId;
                IMSLog.i(access$000, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceRjil.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceRjil.this.mOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return null;
        }

        public String getDeviceId(int soltId) {
            return null;
        }
    }

    protected class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        protected SmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceRjil.LOG_TAG, TelephonyAdapterPrimaryDeviceRjil.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceRjil telephonyAdapterPrimaryDeviceRjil = TelephonyAdapterPrimaryDeviceRjil.this;
                telephonyAdapterPrimaryDeviceRjil.sendMessage(telephonyAdapterPrimaryDeviceRjil.obtainMessage(1, message));
            }
        }
    }

    public void cleanup() {
        if (!(this.mSimManager == null || this.mSimEventListener == null)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregister mSimEventListener");
            this.mSimManager.deRegisterSimCardEventListener(this.mSimEventListener);
            this.mSimManager.deregisterForSimRefresh(this);
            this.mSimManager.deregisterForSimRemoved(this);
            this.mSimEventListener = null;
        }
        this.mState.cleanup();
    }

    public void registerUneregisterForOTP(boolean val) {
        this.mState.registerUneregisterForOTP(val);
    }
}
