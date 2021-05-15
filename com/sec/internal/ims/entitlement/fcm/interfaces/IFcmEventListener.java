package com.sec.internal.ims.entitlement.fcm.interfaces;

import android.content.Context;
import java.util.Map;

public interface IFcmEventListener {
    void onMessageReceived(Context context, String str, Map map);
}
