package com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds;

import android.content.Context;
import com.sec.ims.util.ImsUri;

public abstract class FcmMessage {
    protected transient String origMessage;

    public abstract void broadcastFcmMessage(Context context);

    public boolean shouldBroadcast(Context context) {
        return true;
    }

    public void setOrigMessage(String message) {
        this.origMessage = message;
    }

    /* access modifiers changed from: protected */
    public String deriveMsisdnFromRecipientUri(String recipientUri) {
        ImsUri imsUri = ImsUri.parse(recipientUri);
        if (imsUri != null) {
            return imsUri.getMsisdn();
        }
        return null;
    }
}
