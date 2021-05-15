package com.sec.internal.ims.cmstore.ambs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;

public class DataSMSReceiver extends BroadcastReceiver {
    static final String TAG = "DataSMSReceiver";
    static final String TAG_ACTION = "action";
    static final String TAG_SID = "serviceId";
    static final String VAL_ACTION = "OptIn";
    static final String VAL_SID = "msgstoreoem";
    protected final IAPICallFlowListener mListener;

    public DataSMSReceiver(IAPICallFlowListener callListener) {
        this.mListener = callListener;
    }

    public void onReceive(Context context, Intent intent) {
        byte[] data;
        if (intent.getAction() != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            StringBuilder dataMessageBuilder = new StringBuilder();
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) {
                Log.d(TAG, "invalid pdus");
                return;
            }
            SmsMessage[] msgs = new SmsMessage[pdus.length];
            int i = 0;
            while (i < msgs.length) {
                if (pdus[i] != null) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    if (msgs[i] != null && (data = msgs[i].getUserData()) != null) {
                        for (byte b : data) {
                            dataMessageBuilder.append(Character.toString((char) b));
                        }
                        i++;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
            Log.v(TAG, " messages = " + dataMessageBuilder.toString());
            String[] resp = parse(dataMessageBuilder.toString());
            if (resp != null && VAL_SID.equals(resp[0]) && VAL_ACTION.equals(resp[1])) {
                Log.d(TAG, "binary SMS received to provision!");
                RequestAccount.handleExternalUserOptIn(this.mListener);
            }
        }
    }

    public String[] parse(String input) {
        String strBegin;
        int end;
        String str = input;
        int begin = str.indexOf(TAG_SID);
        if (begin < 0 || (end = strBegin.indexOf(VAL_ACTION)) < 0) {
            return null;
        }
        String[] res = new String[2];
        for (String value : (strBegin = str.substring(begin)).substring(0, VAL_ACTION.length() + end).split(":")) {
            String[] val = value.split(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            if (TAG_SID.equals(val[0])) {
                res[0] = val[1];
            }
            if (TAG_ACTION.equals(val[0])) {
                res[1] = val[1];
            }
        }
        return res;
    }
}
