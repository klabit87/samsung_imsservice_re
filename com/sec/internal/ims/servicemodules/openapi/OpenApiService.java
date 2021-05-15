package com.sec.internal.ims.servicemodules.openapi;

import android.os.RemoteException;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.openapi.IOpenApiService;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class OpenApiService extends IOpenApiService.Stub {
    OpenApiServiceModule mModule;

    public OpenApiService(ServiceModuleBase service) {
        this.mModule = (OpenApiServiceModule) service;
    }

    public void setFeatureTags(String[] featureTags) {
        this.mModule.setFeatureTags(featureTags);
    }

    public void registerIncomingSipMessageListener(ISipDialogListener listener) {
        this.mModule.registerIncomingSipMessageListener(listener);
    }

    public void unregisterIncomingSipMessageListener(ISipDialogListener listener) {
        this.mModule.unregisterIncomingSipMessageListener(listener);
    }

    public void registerImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mModule.registerImsCallEventListener(listener);
    }

    public void unregisterImsCallEventListener(IImsCallEventListener listener) throws RemoteException {
        this.mModule.unregisterImsCallEventListener(listener);
    }

    public void registerDialogEventListener(IDialogEventListener listener) throws RemoteException {
        this.mModule.registerDialogEventListener(listener);
    }

    public void unregisterDialogEventListener(IDialogEventListener listener) throws RemoteException {
        this.mModule.unregisterDialogEventListener(listener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mModule.registerImsRegistrationListener(listener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        this.mModule.unregisterImsRegistrationListener(listener);
    }

    public boolean sendSip(ImsUri impu, String sipMessage, ISipDialogListener listener) {
        return this.mModule.sendSip(sipMessage, listener);
    }

    public void setupMediaPath(String[] remoteIP) {
        this.mModule.setupMediaPath(remoteIP);
    }
}
