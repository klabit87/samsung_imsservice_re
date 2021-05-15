package com.sec.internal.ims.servicemodules.openapi;

import android.content.Intent;
import android.os.Looper;
import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IVolteService;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IImsStatusServiceModule;

public class ImsStatusServiceModule extends ServiceModuleBase implements IImsStatusServiceModule {
    private static final String LOG_TAG = "ImsStatusServiceModule";
    private IVolteService mVolteService;

    public ImsStatusServiceModule(Looper looper, IVolteService volteService) {
        super(looper);
        this.mVolteService = volteService;
    }

    public String[] getServicesRequiring() {
        return new String[]{"mmtel"};
    }

    public void start() {
        super.start();
    }

    public void onRegistered(ImsRegistration regiInfo) {
        super.onRegistered(regiInfo);
    }

    public void onDeregistering(ImsRegistration reg) {
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
    }

    public void handleIntent(Intent intent) {
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        if (ImsRegistry.isReady()) {
            ImsRegistry.registerImsRegistrationListener(listener);
        }
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        if (ImsRegistry.isReady()) {
            ImsRegistry.unregisterImsRegistrationListener(listener);
        }
    }

    public void registerImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mVolteService.registerForCallStateEvent(listener);
    }

    public void unregisterImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mVolteService.deregisterForCallStateEvent(listener);
    }

    public int[] getCallCount() throws RemoteException {
        if (ImsRegistry.isReady()) {
            return ImsRegistry.getCallCount(-1);
        }
        return null;
    }
}
