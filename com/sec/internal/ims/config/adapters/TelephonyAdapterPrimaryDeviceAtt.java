package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceBase;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;

public class TelephonyAdapterPrimaryDeviceAtt extends TelephonyAdapterPrimaryDeviceBase {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = TelephonyAdapterPrimaryDeviceAtt.class.getSimpleName();

    public TelephonyAdapterPrimaryDeviceAtt(Context context, Handler handler, int phoneId) {
        super(context, handler, phoneId);
        registerSmsReceiver();
        initState();
    }

    /* access modifiers changed from: protected */
    public void createSmsReceiver() {
        this.mSmsReceiver = new SmsReceiver();
    }

    /* access modifiers changed from: protected */
    public void sendSmsPushForConfigRequest(boolean isForceConfigRequest) {
        sendEmptyMessage(3);
        super.sendSmsPushForConfigRequest(isForceConfigRequest);
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message:" + msg.what);
        if (msg.what != 1) {
            super.handleMessage(msg);
        } else {
            handleReceivedDataSms(msg, false, true);
        }
    }

    public String getOtp() {
        sendMessageDelayed(obtainMessage(3), 1200000);
        return this.mState.getOtp();
    }

    protected class SmsReceiver extends TelephonyAdapterPrimaryDeviceBase.SmsReceiverBase {
        protected SmsReceiver() {
            super();
        }

        /* access modifiers changed from: protected */
        public void readMessageFromSMSIntent(Intent intent) {
            SmsMessage[] smss = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            IMSLog.i(TelephonyAdapterPrimaryDeviceAtt.LOG_TAG, TelephonyAdapterPrimaryDeviceAtt.this.mPhoneId, "readMessageFromSMSIntent: enter");
            if (smss != null && smss[0] != null) {
                SmsMessage sms = smss[0];
                String message = sms.getDisplayMessageBody();
                if (message == null) {
                    message = new String(sms.getUserData(), Charset.forName("UTF-16"));
                }
                TelephonyAdapterPrimaryDeviceAtt telephonyAdapterPrimaryDeviceAtt = TelephonyAdapterPrimaryDeviceAtt.this;
                telephonyAdapterPrimaryDeviceAtt.sendMessage(telephonyAdapterPrimaryDeviceAtt.obtainMessage(1, message));
            }
        }
    }
}
