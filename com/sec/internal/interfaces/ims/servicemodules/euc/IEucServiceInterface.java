package com.sec.internal.interfaces.ims.servicemodules.euc;

import android.os.Handler;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;

public interface IEucServiceInterface {
    void registerForAckMessage(Handler handler, int i, Object obj);

    void registerForNotificationMessage(Handler handler, int i, Object obj);

    void registerForPersistentMessage(Handler handler, int i, Object obj);

    void registerForSystemMessage(Handler handler, int i, Object obj);

    void registerForVolatileMessage(Handler handler, int i, Object obj);

    void sendEucResponse(EucResponseData eucResponseData);

    void unregisterForAckMessage(Handler handler);

    void unregisterForNotificationMessage(Handler handler);

    void unregisterForPersistentMessage(Handler handler);

    void unregisterForSystemMessage(Handler handler);

    void unregisterForVolatileMessage(Handler handler);
}
