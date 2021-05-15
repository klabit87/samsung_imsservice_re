package com.sec.internal.ims.servicemodules.sms;

import android.os.RemoteException;
import com.sec.ims.sms.ISmsService;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import org.xbill.DNS.Type;

public class SmsService extends ISmsService.Stub {
    private final SmsServiceModule mServiceModule;

    public SmsService(ServiceModuleBase service) {
        this.mServiceModule = (SmsServiceModule) service;
    }

    public void registerForSMSStateChange(int phoneId, ISmsServiceEventListener listener) throws RemoteException {
        this.mServiceModule.registerForSMSStateChange(phoneId, listener);
    }

    public void deRegisterForSMSStateChange(int phoneId, ISmsServiceEventListener listener) throws RemoteException {
        this.mServiceModule.deRegisterForSMSStateChange(phoneId, listener);
    }

    public void sendSMSOverIMS(int phoneId, byte[] pdu, String destAddr, String contentType, int msgId) throws RemoteException {
        this.mServiceModule.sendSMSOverIMS(phoneId, pdu, destAddr, contentType, msgId, false);
    }

    public void sendSMSResponse(boolean isSuccess, int responseCode) throws RemoteException {
        this.mServiceModule.sendSMSResponse(isSuccess, responseCode);
    }

    public void sendDeliverReport(int phoneId, byte[] data) throws RemoteException {
        this.mServiceModule.sendDeliverReport(phoneId, data);
    }

    public void sendRPSMMA(int phoneId, String smscAddr) throws RemoteException {
        this.mServiceModule.sendSMSOverIMS(phoneId, (byte[]) null, smscAddr, GsmSmsUtil.CONTENT_TYPE_3GPP, Type.CAA, true);
    }

    public boolean getSmsFallback(int phoneId) throws RemoteException {
        return this.mServiceModule.getSmsFallback(phoneId);
    }
}
