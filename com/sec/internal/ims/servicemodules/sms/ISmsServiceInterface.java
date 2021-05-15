package com.sec.internal.ims.servicemodules.sms;

import android.os.Handler;

public interface ISmsServiceInterface {
    void registerForRrcConnectionEvent(Handler handler, int i, Object obj);

    void registerForSMSEvent(Handler handler, int i, Object obj);

    void sendMessage(String str, String str2, String str3, byte[] bArr, boolean z, String str4, int i, int i2);

    void sendSMSResponse(int i, String str, int i2);

    void setMsgAppInfoToSipUa(int i, String str);
}
