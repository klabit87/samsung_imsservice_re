package com.sec.internal.ims.servicemodules.ss;

import android.content.Context;
import android.os.RemoteException;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.ims.ss.IUtService;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class UtService extends IUtService.Stub {
    private static final String LOG_TAG = UtService.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private Context mContext = null;
    private UtServiceModule mServiceModule = null;

    public UtService(ServiceModuleBase service) {
        UtServiceModule utServiceModule = (UtServiceModule) service;
        this.mServiceModule = utServiceModule;
        this.mContext = utServiceModule.getContext();
    }

    public void registerForUtEvent(int phoneId, IImsUtEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerForUtEvent(phoneId, listener);
    }

    public void deRegisterForUtEvent(int phoneId, IImsUtEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.deRegisterForUtEvent(phoneId, listener);
    }

    public int queryCallBarring(int phoneId, int cbType, int ssClass) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallBarring(phoneId, cbType, ssClass);
    }

    public int queryCallForward(int phoneId, int condition, String number) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallForward(phoneId, condition, number);
    }

    public int queryCallWaiting(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallWaiting(phoneId);
    }

    public int queryCLIR(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCLIR(phoneId);
    }

    public int queryCLIP(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCLIP(phoneId);
    }

    public int queryCOLR(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCOLR(phoneId);
    }

    public int queryCOLP(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCOLP(phoneId);
    }

    public int updateCallBarring(int phoneId, int cbType, int action, int serviceClass, String password, String[] barrList) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallBarring(phoneId, cbType, action, serviceClass, password, barrList);
    }

    public int updateCallForward(int phoneId, int action, int condition, String number, int serviceClass, int timeSeconds) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallForward(phoneId, action, condition, number, serviceClass, timeSeconds);
    }

    public int updateCallWaiting(int phoneId, boolean enable, int serviceClass) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallWaiting(phoneId, enable, serviceClass);
    }

    public int updateCLIR(int phoneId, int clirMode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCLIR(phoneId, clirMode);
    }

    public int updateCLIP(int phoneId, boolean enable) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCLIP(phoneId, enable);
    }

    public int updateCOLR(int phoneId, int presentation) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCOLR(phoneId, presentation);
    }

    public int updateCOLP(int phoneId, boolean enable) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCOLP(phoneId, enable);
    }

    public boolean isUtEnabled(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.isUtEnabled(phoneId);
    }
}
