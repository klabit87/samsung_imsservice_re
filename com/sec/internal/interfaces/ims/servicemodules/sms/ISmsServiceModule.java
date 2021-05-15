package com.sec.internal.interfaces.ims.servicemodules.sms;

import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;

public interface ISmsServiceModule extends IServiceModule {
    boolean getSmsFallback(int i);

    void handleEventDefaultAppChanged();

    boolean isVolteSupported(int i);

    void registerForSMSStateChange(int i, ISmsServiceEventListener iSmsServiceEventListener);

    void sendDeliverReport(int i, byte[] bArr);

    void sendSMSOverIMS(int i, byte[] bArr, String str, String str2, int i2, boolean z);

    void setDelayedDeregisterTimerRunning(int i, boolean z);
}
