package com.sec.internal.ims.aec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.log.AECLog;

public class SmsNotification extends BroadcastReceiver {
    private static final String DATA_AUTHORITY = "localhost";
    private static final String DATA_SCHEME = "sms";
    private static final String DEST_PORT = "8095";
    private static final String LOG_TAG = SmsNotification.class.getSimpleName();
    private static final String TS43_SMS_PUSH_MESSAGE = "aescfg";
    private final Context mContext;
    private final Handler mModuleHandler;

    public SmsNotification(Context context, Handler handler) {
        this.mContext = context;
        this.mModuleHandler = handler;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
        intentFilter.addDataScheme(DATA_SCHEME);
        intentFilter.addDataAuthority(DATA_AUTHORITY, DEST_PORT);
        return intentFilter;
    }

    public void onReceive(Context context, Intent intent) {
        sendSmsNotification(intent);
    }

    private void sendSmsNotification(Intent intent) {
        try {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null && messages[0] != null) {
                SmsMessage sms = messages[0];
                int phoneId = SubscriptionManager.from(this.mContext).getActiveSubscriptionInfo(intent.getIntExtra(PhoneConstants.SUBSCRIPTION_KEY, -1)).getSimSlotIndex();
                String body = sms.getDisplayMessageBody();
                String str = LOG_TAG;
                AECLog.i(str, "sendSmsNotification: " + body, phoneId);
                if (TextUtils.isEmpty(body)) {
                    AECLog.i(LOG_TAG, "sendSmsNotification: discard empty notification", phoneId);
                } else if (body.contains(TS43_SMS_PUSH_MESSAGE)) {
                    Message message = this.mModuleHandler.obtainMessage();
                    message.what = 8;
                    message.arg1 = phoneId;
                    message.obj = body.substring(body.indexOf(",") + 1);
                    this.mModuleHandler.sendMessage(message);
                } else {
                    AECLog.i(LOG_TAG, "sendSmsNotification: discard invalid notification", phoneId);
                }
            }
        } catch (SecurityException e) {
            String str2 = LOG_TAG;
            AECLog.e(str2, "sendSmsNotification: " + e.toString());
        }
    }
}
