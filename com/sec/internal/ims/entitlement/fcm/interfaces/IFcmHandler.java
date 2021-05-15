package com.sec.internal.ims.entitlement.fcm.interfaces;

import android.content.Context;
import java.util.Map;

public interface IFcmHandler {
    void onMessageReceived(Context context, String str, Map map);

    void registerFcmEventListener(IFcmEventListener iFcmEventListener);

    void unRegisterFcmEventListener(IFcmEventListener iFcmEventListener);
}
