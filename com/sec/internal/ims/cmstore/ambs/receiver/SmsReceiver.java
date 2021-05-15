package com.sec.internal.ims.cmstore.ambs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.sec.internal.ims.cmstore.RetryStackAdapterHelper;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqZCode;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;

public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = SmsReceiver.class.getSimpleName();
    IAPICallFlowListener mListener;

    public SmsReceiver(IAPICallFlowListener callListener) {
        this.mListener = callListener;
    }

    public void onReceive(Context context, Intent intent) {
        Log.v(this.TAG, ">>>>>>>onReceive start");
        Bundle bundle = intent.getExtras();
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") && bundle != null) {
            StringBuilder body = new StringBuilder();
            StringBuilder number = new StringBuilder();
            Object[] _pdus = (Object[]) bundle.get("pdus");
            if (_pdus == null) {
                Log.d(this.TAG, "invalid pdus");
                return;
            }
            SmsMessage[] message = new SmsMessage[_pdus.length];
            for (int i = 0; i < _pdus.length; i++) {
                message[i] = SmsMessage.createFromPdu((byte[]) _pdus[i]);
            }
            for (SmsMessage currentMessage : message) {
                body.append(currentMessage.getDisplayMessageBody());
                number.append(currentMessage.getDisplayOriginatingAddress());
            }
            checkAndHandleZCode(body.toString(), number.toString());
        }
        Log.v(this.TAG, ">>>>>>>onReceive end");
    }

    private void checkAndHandleZCode(String smsBody, String phoneNumber) {
        if (ReqZCode.isSmsZCode(smsBody, phoneNumber)) {
            ReqZCode.handleSmsZCode(smsBody, this.mListener, new RetryStackAdapterHelper());
        }
    }
}
