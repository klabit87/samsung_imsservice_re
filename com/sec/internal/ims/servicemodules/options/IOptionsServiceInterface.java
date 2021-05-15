package com.sec.internal.ims.servicemodules.options;

import android.os.Handler;
import android.os.Message;
import com.sec.ims.util.ImsUri;

public interface IOptionsServiceInterface {
    void registerForCmcOptionsEvent(Handler handler, int i, Object obj);

    void registerForOptionsEvent(Handler handler, int i, Object obj);

    void registerForP2pOptionsEvent(Handler handler, int i, Object obj);

    void requestCapabilityExchange(ImsUri imsUri, long j, int i, String str);

    void requestSendCmcCheckMsg(int i, int i2, String str);

    void sendCapexResponse(ImsUri imsUri, long j, String str, int i, Message message, int i2, String str2);

    void setOwnCapabilities(long j, int i);

    int updateCmcExtCallCount(int i, int i2);
}
