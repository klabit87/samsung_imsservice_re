package com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.log.IMSLog;

public class AndsfMessage extends FcmMessage {
    private static final String LOG_TAG = AndsfMessage.class.getSimpleName();
    public String messageType;

    public AndsfMessage(String messageType2) {
        this.messageType = messageType2;
    }

    public boolean shouldBroadcast(Context context) {
        return true;
    }

    public void broadcastFcmMessage(Context context) {
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION);
        intent.putExtra(NSDSNamespaces.NSDSExtras.MESSAGE_TYPE, this.messageType);
        intent.putExtra(NSDSNamespaces.NSDSExtras.NOTIFCATION_TITLE, getNotificationTitle());
        intent.putExtra(NSDSNamespaces.NSDSExtras.NOTIFCATION_CONTENT, getNotificationContent());
        String str = LOG_TAG;
        IMSLog.s(str, "push notification broadcastIntent: " + intent.toString() + intent.getExtras());
        IntentUtil.sendBroadcast(context, intent, ContextExt.CURRENT_OR_SELF);
    }

    private String getNotificationTitle() {
        return "PNS: ";
    }

    private String getNotificationContent() {
        return "original message: " + this.origMessage;
    }
}
