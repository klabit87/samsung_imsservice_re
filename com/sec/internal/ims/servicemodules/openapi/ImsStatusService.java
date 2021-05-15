package com.sec.internal.ims.servicemodules.openapi;

import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.openapi.IImsStatusService;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class ImsStatusService extends IImsStatusService.Stub {
    ImsStatusServiceModule mModule;

    public ImsStatusService(ServiceModuleBase service) {
        this.mModule = (ImsStatusServiceModule) service;
    }

    public void registerImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mModule.registerImsCallEventListener(listener);
    }

    public void unregisterImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mModule.unregisterImsCallEventListener(listener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mModule.registerImsRegistrationListener(listener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mModule.unregisterImsRegistrationListener(listener);
    }

    public int[] getCallCount() throws RemoteException {
        return this.mModule.getCallCount();
    }
}
