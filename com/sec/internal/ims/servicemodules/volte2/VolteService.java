package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IVolteService;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import java.util.HashMap;
import java.util.Map;

public class VolteService extends IVolteService.Stub {
    private static final String LOG_TAG = VolteService.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private Context mContext = null;
    private VolteServiceModule mServiceModule = null;

    public VolteService(ServiceModuleBase service) {
        VolteServiceModule volteServiceModule = (VolteServiceModule) service;
        this.mServiceModule = volteServiceModule;
        this.mContext = volteServiceModule.getContext();
    }

    public CallProfile createCallProfile(int serviceType, int callType) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createCallProfile(serviceType, callType);
    }

    public ImsCallSession createSession(CallProfile profile) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createSession(profile);
    }

    public ImsCallSession createSessionWithRegId(CallProfile profile, int regId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createSession(profile, regId);
    }

    public ImsCallSession getPendingSession(String callId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getPendingSession(callId);
    }

    public ImsCallSession getSession(int callId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getSessionByCallId(callId);
    }

    public void registerForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerForVolteServiceEvent(phoneId, listener);
    }

    public void deRegisterForVolteServiceEvent(int phoneId, IVolteServiceEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.deRegisterForVolteServiceEvent(phoneId, listener);
    }

    public void registerForCallStateEvent(IImsCallEventListener listener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.registerForCallStateEvent(listener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void deregisterForCallStateEvent(IImsCallEventListener listener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.deregisterForCallStateEvent(listener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void registerForCallStateEventForSlot(int phoneId, IImsCallEventListener listener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.registerForCallStateEvent(phoneId, listener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void deregisterForCallStateEventForSlot(int phoneId, IImsCallEventListener listener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.deregisterForCallStateEvent(phoneId, listener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void setTtyMode(int mode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.setTtyMode(mode);
    }

    public int updateSSACInfo(int voiceFactor, int voiceTime, int videoFactor, int videoTime) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateSSACInfo(voiceFactor, voiceTime, videoFactor, videoTime);
    }

    public void enableCallWaitingRule(boolean enableRule) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.enableCallWaitingRule(enableRule);
    }

    public void notifyProgressIncomingCall(int sessionId, Map headers) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (headers instanceof HashMap) {
            this.mServiceModule.notifyProgressIncomingCall(sessionId, (HashMap) headers);
        }
    }

    public int[] getCallCount() throws RemoteException {
        if (isPermissionGranted()) {
            return this.mServiceModule.getCallCount();
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public int getRttMode() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getRttMode();
    }

    public void setAutomaticMode(int phoneId, boolean mode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.setAutomaticMode(phoneId, mode);
    }

    public void sendRttSessionModifyResponse(int callId, boolean accept) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.sendRttSessionModifyResponse(callId, accept);
    }

    public void sendRttSessionModifyRequest(int callId, boolean mode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.sendRttSessionModifyRequest(callId, mode);
    }

    public void registerRttEventListener(int phoneId, IRttEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerRttEventListener(phoneId, listener);
    }

    public void unregisterRttEventListener(int phoneId, IRttEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.unregisterRttEventListener(phoneId, listener);
    }

    public int getParticipantIdForMerge(int phoneId, int hostId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getParticipantIdForMerge(phoneId, hostId);
    }

    public IImsCallSession getSessionByCallId(int callId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getSessionByCallId(callId);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener listener, boolean broadcast, int phoneId) throws RemoteException {
        ImsRegistry.registerImsRegistrationListener(listener, broadcast, phoneId);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener listener) throws RemoteException {
        ImsRegistry.unregisterImsRegistrationListener(listener);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) throws RemoteException {
        return ImsRegistry.getRegistrationInfoByPhoneId(phoneId);
    }

    public int getNetworkType(int handle) throws RemoteException {
        return ImsRegistry.getNetworkType(handle);
    }

    public String updateEccUrn(int phoneId, String dialingNumber) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateEccUrn(phoneId, dialingNumber);
    }

    private boolean isPermissionGranted() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0;
    }

    public void changeAudioPath(int phoneId, int direction) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.updateAudioInterface(phoneId, direction);
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.startLocalRingBackTone(streamType, volume, toneType);
    }

    public int stopLocalRingBackTone() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.stopLocalRingBackTone();
    }

    public String getTrn(String srcMsisdn, String dstMsisdn) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getTrn(srcMsisdn, dstMsisdn);
    }

    public ImsCallInfo[] getImsCallInfos(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getImsCallInfos(phoneId);
    }
}
