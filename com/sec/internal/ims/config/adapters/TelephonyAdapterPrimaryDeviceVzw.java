package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.sms.SmsMessage;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;

public class TelephonyAdapterPrimaryDeviceVzw extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceVzw.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceVzw(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerPortSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createPortSmsReceiver() {
        this.mPortSmsReceiver = new PortSmsReceiver();
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.d(str, "message:" + msg.what);
        if (msg.what != 0) {
            super.handleMessage(msg);
            return;
        }
        Log.d(LOG_TAG, "receive port sms");
        if (msg.obj == null) {
            Log.d(LOG_TAG, "invalid sms configuration request: null");
        } else if (((String) msg.obj).contains(SMS_CONFIGURATION_REQUEST)) {
            Log.d(LOG_TAG, "force configuration request");
            IMSLog.c(LogClass.TAPDV_RECEIVED_PORTSMS, this.mPhoneId + ",REVPO");
            this.mModuleHandler.sendMessage(obtainMessage(21, Integer.valueOf(this.mPhoneId)));
        } else {
            String str2 = LOG_TAG;
            Log.d(str2, "invalid sms configuration request: " + ((String) msg.obj));
        }
    }

    public String getOtp() {
        return this.mState.getOtp();
    }

    /* access modifiers changed from: protected */
    public void getState(String state) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "getState: change to " + state);
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
            return null;
        }
    }

    private class AbsentState extends TelephonyAdapterPrimaryDeviceBase.AbsentState {
        private AbsentState() {
            super();
        }

        public String getOtp() {
            Log.e(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, "getOtp method can't run in absentState");
            return null;
        }

        public String getIdentityByPhoneId(int phoneId) {
            Log.e(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, "getIdentityByPhoneId method can't run in absentState");
            return null;
        }

        public String getSubscriberId(int subscriptionId) {
            Log.e(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, "getSubscriberId method can't run in absentState");
            return null;
        }

        public String getMsisdn(int subscriptionId) {
            Log.e(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, "getMsisdn method can't run in absentState");
            return null;
        }

        public String getDeviceId(int slotId) {
            Log.e(TelephonyAdapterPrimaryDeviceVzw.LOG_TAG, "getDeviceId method can't run in absentState");
            return null;
        }
    }

    private class PortSmsReceiver extends TelephonyAdapterPrimaryDeviceBase.PortSmsReceiverBase {
        private PortSmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            String format = intent.getStringExtra("format");
            String access$300 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
            Log.d(access$300, "readMessageFromSMSIntent: format: " + format);
            if (format.equals(SmsMessage.FORMAT_3GPP2)) {
                try {
                    Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
                    if (messages != null && messages[0] != null) {
                        String message = new String((byte[]) messages[0], Charset.forName("UTF-8"));
                        String access$3002 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                        Log.d(access$3002, "readMessageFromSMSIntent, message : " + message);
                        IMSLog.c(LogClass.TAPDV_MSG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId + ",MSG:" + message);
                        TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw = TelephonyAdapterPrimaryDeviceVzw.this;
                        telephonyAdapterPrimaryDeviceVzw.sendMessage(telephonyAdapterPrimaryDeviceVzw.obtainMessage(0, message));
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            } else {
                android.telephony.SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                if (smss != null && smss[0] != null) {
                    android.telephony.SmsMessage sms = smss[0];
                    String message2 = sms.getDisplayMessageBody();
                    if (message2 == null) {
                        message2 = new String(sms.getUserData(), Charset.forName("UTF-8"));
                    }
                    String access$3003 = TelephonyAdapterPrimaryDeviceVzw.LOG_TAG;
                    Log.d(access$3003, "readMessageFromSMSIntent, message : " + message2);
                    IMSLog.c(LogClass.TAPDV_MSG, TelephonyAdapterPrimaryDeviceVzw.this.mPhoneId + ",MSG:" + message2);
                    TelephonyAdapterPrimaryDeviceVzw telephonyAdapterPrimaryDeviceVzw2 = TelephonyAdapterPrimaryDeviceVzw.this;
                    telephonyAdapterPrimaryDeviceVzw2.sendMessage(telephonyAdapterPrimaryDeviceVzw2.obtainMessage(0, message2));
                }
            }
        }
    }
}
