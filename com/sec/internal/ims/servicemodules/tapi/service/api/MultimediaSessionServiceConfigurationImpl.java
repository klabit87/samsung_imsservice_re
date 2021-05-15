package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;
import com.gsma.services.rcs.extension.IMultimediaSessionServiceConfiguration;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;

public class MultimediaSessionServiceConfigurationImpl extends IMultimediaSessionServiceConfiguration.Stub {
    private static final String LOG_TAG = MultimediaSessionServiceConfigurationImpl.class.getSimpleName();
    private static MultimediaSessionServiceConfigurationImpl multimediaSessionServiceConfigurationImpl = null;
    private ISessionModule mSessionModule;

    private MultimediaSessionServiceConfigurationImpl(ISessionModule sessionModule) {
        this.mSessionModule = sessionModule;
    }

    public static MultimediaSessionServiceConfigurationImpl getInstance(ISessionModule sessionModule) {
        if (multimediaSessionServiceConfigurationImpl == null) {
            multimediaSessionServiceConfigurationImpl = new MultimediaSessionServiceConfigurationImpl(sessionModule);
        }
        return multimediaSessionServiceConfigurationImpl;
    }

    public boolean isServiceActivated(String serviceId) throws RemoteException {
        ISessionModule iSessionModule = this.mSessionModule;
        if (iSessionModule != null) {
            return iSessionModule.isServiceActivated(serviceId);
        }
        return false;
    }

    public long getInactivityTimeout() throws RemoteException {
        return this.mSessionModule.getInactivityTimeout();
    }

    public long getMessagingSessionInactivityTimeout(String serviceId) throws RemoteException {
        return this.mSessionModule.getInactivityTimeout();
    }

    public int getMessageMaxLength() throws RemoteException {
        return this.mSessionModule.getMaxMsrpLengthForExtensions();
    }
}
