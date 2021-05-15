package com.sec.internal.interfaces.ims.servicemodules.ss;

import com.sec.ims.ss.IImsUtEventListener;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;

public interface IUtServiceModule extends IServiceModule {
    void enableUt(int i, boolean z);

    boolean isUssdEnabled(int i);

    boolean isUtEnabled(int i);

    int queryCLIP(int i);

    int queryCLIR(int i);

    int queryCOLP(int i);

    int queryCOLR(int i);

    int queryCallBarring(int i, int i2, int i3);

    int queryCallForward(int i, int i2, String str);

    int queryCallWaiting(int i);

    void registerForUtEvent(int i, IImsUtEventListener iImsUtEventListener);

    int updateCLIP(int i, boolean z);

    int updateCLIR(int i, int i2);

    int updateCOLP(int i, boolean z);

    int updateCOLR(int i, int i2);

    int updateCallBarring(int i, int i2, int i3, int i4, String str, String[] strArr);

    int updateCallForward(int i, int i2, int i3, String str, int i4, int i5);

    int updateCallWaiting(int i, boolean z, int i2);
}
