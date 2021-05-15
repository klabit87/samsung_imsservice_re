package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;

public class TelephonyAdapterPrimaryDeviceCmcc extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceCmcc.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceCmcc(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiver();
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        int i2 = msg.what;
        if (i2 == 1) {
            handleReceivedDataSms(msg, false, false);
        } else if (i2 != 3) {
            super.handleMessage(msg);
        } else {
            handleOtpTimeout(false);
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

    private class ReadyState extends TelephonyAdapterPrimaryDeviceBase.ReadyState {
        private ReadyState() {
            super();
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceCmcc.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceCmcc.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceCmcc.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TelephonyAdapterPrimaryDeviceCmcc.this.removeMessages(3);
                String access$200 = TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId;
                IMSLog.i(access$200, i, "receive OTP: " + IMSLog.checker(TelephonyAdapterPrimaryDeviceCmcc.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceCmcc.this.mOtp;
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        ImsProfile mImsProfile = null;

        public AbsentState() {
            super();
            IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "TelephonyAdapter:Absent state");
            List<ImsProfile> profile = ImsProfileLoaderInternal.getProfileList(TelephonyAdapterPrimaryDeviceCmcc.this.mContext, SimUtil.getSimSlotPriority());
            if (profile == null || profile.size() <= 0 || profile.get(0) == null) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "AbsentState : no ImsProfile loaded");
            } else {
                this.mImsProfile = profile.get(0);
            }
        }

        public String getOtp() {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (TelephonyAdapterPrimaryDeviceCmcc.this.mOtp == null || currentTime >= TelephonyAdapterPrimaryDeviceCmcc.this.mOtpReceivedTime + 3000) {
                IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "OTP don't exist. wait OTP");
                try {
                    TelephonyAdapterPrimaryDeviceCmcc.this.mSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String access$200 = TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG;
                int i = TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId;
                IMSLog.i(access$200, i, "receive OTP:" + IMSLog.checker(TelephonyAdapterPrimaryDeviceCmcc.this.mOtp));
            } else {
                IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "OTP exist. send immediately");
            }
            return TelephonyAdapterPrimaryDeviceCmcc.this.mOtp;
        }

        public String getIdentityByPhoneId(int phoneId) {
            return null;
        }

        public String getDeviceId(int slotId) {
            return null;
        }
    }

    private class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        private SmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceCmcc.LOG_TAG, TelephonyAdapterPrimaryDeviceCmcc.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceCmcc telephonyAdapterPrimaryDeviceCmcc = TelephonyAdapterPrimaryDeviceCmcc.this;
                telephonyAdapterPrimaryDeviceCmcc.sendMessage(telephonyAdapterPrimaryDeviceCmcc.obtainMessage(1, message));
            }
        }
    }
}
