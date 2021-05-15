package com.sec.internal.interfaces.ims.servicemodules.session;

import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.session.IMessagingSessionListener;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;

public interface ISessionModule extends IServiceModule {
    void abortSession(String str);

    void deRegisterApp();

    ImsRegistration getImsRegistration();

    long getInactivityTimeout();

    int getMaxMsrpLengthForExtensions();

    ImSession getMessagingSession(String str);

    ImSession initiateMessagingSession(String str, ImsUri imsUri, String[] strArr, String[] strArr2);

    boolean isServiceActivated(String str);

    boolean isServiceRegistered();

    boolean needDeRegister(String str);

    boolean needRegister(String str);

    void registerApp();

    void registerMessagingSessionListener(IMessagingSessionListener iMessagingSessionListener);

    void sendInstantMultimediaMessage(String str, ImsUri imsUri, byte[] bArr, String str2);

    void sendMultimediaMessage(String str, byte[] bArr, String str2);
}
