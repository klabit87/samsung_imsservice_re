package com.sec.internal.google.cmc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PublishDialog;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.google.DataTypeConvertor;
import com.sec.internal.google.GoogleImsService;
import com.sec.internal.google.ImsCallSessionImpl;
import com.sec.internal.google.ServiceProfile;
import com.sec.internal.google.cmc.CmcConnectivityController;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmcImsServiceUtil {
    private static final String LOG_TAG = CmcImsServiceUtil.class.getSimpleName();
    private final int RESERVED_P2P_CALLID = 999;
    private boolean mCmcReady = true;
    private int mCmcRegHandle = -1;
    CmcConnectivityController mConnectivityController = null;
    private Context mContext;
    GoogleImsService mGoogleImsAdaptor = null;
    ServiceProfile mServiceProfile = null;
    IVolteServiceModule mVolteServiceModule = null;
    private Map<Integer, IImsCallSession> mp2pSecSessionMap = new ConcurrentHashMap();

    public CmcImsServiceUtil(Context context, GoogleImsService googleImsService, CmcConnectivityController cc, IVolteServiceModule volteServiceModule) {
        this.mContext = context;
        this.mGoogleImsAdaptor = googleImsService;
        this.mConnectivityController = cc;
        this.mVolteServiceModule = volteServiceModule;
    }

    public void acquireP2pNetwork() {
        if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice) {
            Log.d(LOG_TAG, "MO call by PD. startCmcP2pConnection!");
            this.mConnectivityController.getNsdManager().acquireNetwork(new NsdNetworkCapabilities.Builder().addCapability(0).build());
        }
    }

    public void setServiceProfile(ServiceProfile service) {
        this.mServiceProfile = service;
    }

    private int getCmcRegHandle(ServiceProfile service, int cmcType) {
        IRegistrationGovernor governor;
        if (service.getServiceClass() == 1) {
            ImsRegistration[] registrationList = ImsRegistry.getRegistrationManager().getRegistrationInfo();
            int length = registrationList.length;
            int i = 0;
            while (i < length) {
                ImsRegistration reg = registrationList[i];
                if (reg == null || reg.getPhoneId() != service.getPhoneId() || !reg.hasVolteService() || reg.getImsProfile() == null || reg.getImsProfile().getCmcType() != cmcType) {
                    i++;
                } else if (!isP2pPrimaryType(cmcType) || ((governor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(reg.getHandle())) != null && governor.getP2pListSize(cmcType) != 0)) {
                    return reg.getHandle();
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    private boolean setBoundSessionInfo(int phoneId, ImsCallProfile profile, CallProfile callProfile) {
        Log.d(LOG_TAG, "setBoundSessionInfo()");
        Bundle oemExtras = profile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
        if (!this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(phoneId)) {
            return false;
        }
        if (oemExtras == null) {
            return true;
        }
        if (oemExtras.containsKey("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID")) {
            int boundSessionId = oemExtras.getInt("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID");
            String str = LOG_TAG;
            Log.d(str, "setBoundSessionInfo(), boundSessionId: " + boundSessionId);
            if (boundSessionId > 0) {
                callProfile.setCmcBoundSessionId(boundSessionId);
            }
        }
        if (!oemExtras.containsKey("com.samsung.telephony.extra.CMC_DIAL_FROM")) {
            return true;
        }
        String dialFrom = oemExtras.getString("com.samsung.telephony.extra.CMC_DIAL_FROM");
        if (TextUtils.isEmpty(dialFrom)) {
            return true;
        }
        callProfile.setLetteringText(dialFrom);
        return true;
    }

    public int prepareCallSession(int cmcType, ImsCallProfile profile, CallProfile callProfile, ServiceProfile service) throws RemoteException, UnsupportedOperationException {
        this.mCmcRegHandle = -1;
        this.mCmcReady = true;
        this.mp2pSecSessionMap.clear();
        try {
            boolean isCmcRegExist = setBoundSessionInfo(service.getPhoneId(), profile, callProfile);
            String str = LOG_TAG;
            Log.d(str, "isCmcRegExist: " + isCmcRegExist);
            if (!isCmcRegExist) {
                this.mCmcReady = false;
            } else if (cmcType == 1) {
                int cmcLineSlotIndex = ImsRegistry.getRegistrationManager().getCmcLineSlotIndex();
                int phoneId = service.getPhoneId();
                int cmcRegHandle = getCmcRegHandle(service, cmcType);
                this.mCmcRegHandle = cmcRegHandle;
                if (cmcRegHandle == -1) {
                    Log.e(LOG_TAG, "CMC PD is not registered.");
                    this.mCmcReady = false;
                } else if (cmcLineSlotIndex != phoneId) {
                    Log.e(LOG_TAG, "phoneId and cmcLineSlotIndex are not matched");
                    this.mCmcReady = false;
                } else {
                    Log.d(LOG_TAG, "create session on CMC-PD");
                }
                String str2 = LOG_TAG;
                Log.d(str2, "cmcLineSlotIndex: " + cmcLineSlotIndex + ", phoneId: " + phoneId);
                if (cmcLineSlotIndex == phoneId) {
                    int p2pType = 5;
                    if (this.mConnectivityController.isEnabledWifiDirectFeature()) {
                        p2pType = 7;
                    }
                    for (int type = 3; type <= p2pType; type += 2) {
                        int cmcRegHandle2 = getCmcRegHandle(service, type);
                        int p2pRegHandle = cmcRegHandle2;
                        if (cmcRegHandle2 != -1) {
                            callProfile.setCmcType(type);
                            this.mp2pSecSessionMap.put(Integer.valueOf(p2pRegHandle), this.mVolteServiceModule.createSession(callProfile, p2pRegHandle));
                        }
                    }
                }
            } else {
                if (profile.getCallExtraBoolean("CallPull")) {
                    cmcType = 2;
                } else if (!TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable()) {
                    cmcType = 2;
                }
                int cmcRegHandle3 = getCmcRegHandle(service, 2);
                this.mCmcRegHandle = cmcRegHandle3;
                if (cmcRegHandle3 != -1) {
                    Log.d(LOG_TAG, "create session on CMC SD");
                } else {
                    this.mCmcReady = false;
                }
                int cmcRegHandle4 = getCmcRegHandle(service, 4);
                int p2pRegHandle2 = cmcRegHandle4;
                if (cmcRegHandle4 != -1) {
                    Log.d(LOG_TAG, "create session on WIFI-AP SD");
                    callProfile.setCmcType(4);
                    this.mp2pSecSessionMap.put(Integer.valueOf(p2pRegHandle2), this.mVolteServiceModule.createSession(callProfile, p2pRegHandle2));
                } else if (this.mConnectivityController.isEnabledWifiDirectFeature()) {
                    int cmcRegHandle5 = getCmcRegHandle(service, 8);
                    int p2pRegHandle3 = cmcRegHandle5;
                    if (cmcRegHandle5 != -1) {
                        Log.d(LOG_TAG, "create session on WIFI-DIRECT SD");
                        callProfile.setCmcType(8);
                        this.mp2pSecSessionMap.put(Integer.valueOf(p2pRegHandle3), this.mVolteServiceModule.createSession(callProfile, p2pRegHandle3));
                    }
                }
            }
            callProfile.setCmcType(cmcType);
        } catch (RemoteException e) {
        }
        return cmcType;
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    public com.android.ims.internal.IImsCallSession createCallSession(int cmcType, int serviceId, ImsCallProfile profile, CallProfile callProfile, ServiceProfile service) throws RemoteException, UnsupportedOperationException {
        CmcCallSessionManager p2pSessionManager;
        int i = cmcType;
        ImsCallProfile imsCallProfile = profile;
        CallProfile callProfile2 = callProfile;
        String str = LOG_TAG;
        Log.d(str, "createCallSession(), cmcType: " + i);
        IImsCallSession secCallSession = null;
        try {
            String str2 = LOG_TAG;
            Log.d(str2, "mCmcRegHandle: " + this.mCmcRegHandle + ", mCmcReady: " + this.mCmcReady);
            String str3 = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("mp2pSecSessionMap size: ");
            sb.append(this.mp2pSecSessionMap.size());
            Log.d(str3, sb.toString());
            for (Map.Entry<Integer, IImsCallSession> e : this.mp2pSecSessionMap.entrySet()) {
                e.getValue().setEpdgState(TextUtils.equals(imsCallProfile.getCallExtra("CallRadioTech"), String.valueOf(18)));
            }
            boolean existP2pSession = this.mp2pSecSessionMap.size() > 0;
            if (this.mCmcReady) {
                secCallSession = this.mVolteServiceModule.createSession(callProfile2, this.mCmcRegHandle);
            }
            if (!this.mConnectivityController.isEnabledWifiDirectFeature() || this.mCmcReady || existP2pSession || !(i == 1 || i == 2 || i == 7 || i == 8)) {
                if (secCallSession == null) {
                    if (!existP2pSession) {
                        throw new UnsupportedOperationException();
                    }
                }
                CmcCallSessionManager p2pSessionManager2 = null;
                if (!this.mCmcReady || secCallSession == null) {
                    boolean setMainSession = false;
                    for (Map.Entry<Integer, IImsCallSession> e2 : this.mp2pSecSessionMap.entrySet()) {
                        if (!setMainSession) {
                            p2pSessionManager2 = new CmcCallSessionManager(e2.getValue(), this.mVolteServiceModule, this.mConnectivityController.getNsdManager(), this.mConnectivityController.isEnabledWifiDirectFeature());
                            setMainSession = true;
                        } else {
                            p2pSessionManager2.addP2pSession(e2.getValue());
                        }
                    }
                    p2pSessionManager = p2pSessionManager2;
                } else {
                    secCallSession.setEpdgState(TextUtils.equals(imsCallProfile.getCallExtra("CallRadioTech"), String.valueOf(18)));
                    p2pSessionManager = new CmcCallSessionManager(secCallSession, this.mVolteServiceModule, this.mConnectivityController.getNsdManager(), this.mConnectivityController.isEnabledWifiDirectFeature());
                    if (existP2pSession) {
                        for (Map.Entry<Integer, IImsCallSession> e3 : this.mp2pSecSessionMap.entrySet()) {
                            p2pSessionManager.addP2pSession(e3.getValue());
                        }
                    }
                }
                Log.d(LOG_TAG, "createCallSession, create imsCallSessionImpl for [CMC+D2D volte call]");
                ImsCallSessionImpl session = new CmcImsCallSessionImpl(imsCallProfile, p2pSessionManager, (IImsCallSessionListener) null, this.mGoogleImsAdaptor);
                this.mGoogleImsAdaptor.setCallSession(session.getCallIdInt(), session);
                this.mConnectivityController.setReservedId(session.getCallIdInt());
                return session;
            }
            if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice) {
                Log.d(LOG_TAG, "[P2P] create fake p2pSessionManager in PD");
            } else if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.SDevice) {
                Log.d(LOG_TAG, "[P2P] create fake sessionManager in SD ");
                Log.d(LOG_TAG, "there is no cmc, startCmcP2pConnection!");
                this.mConnectivityController.getNsdManager().acquireNetwork(new NsdNetworkCapabilities.Builder().addCapability(0).build());
            } else {
                Log.e(LOG_TAG, "[P2P] error: please check your P2pSwitchEnabled");
                throw new UnsupportedOperationException();
            }
            this.mConnectivityController.setReservedId(999);
            Log.d(LOG_TAG, "createCallSession, create imsCallSessionImpl for [D2D volte call]");
            CmcCallSessionManager p2pSessionManager3 = new CmcCallSessionManager((IImsCallSession) null, this.mVolteServiceModule, this.mConnectivityController.getNsdManager(), this.mConnectivityController.isEnabledWifiDirectFeature());
            ImsCallSessionImpl session2 = new CmcImsCallSessionImpl(imsCallProfile, p2pSessionManager3, (IImsCallSessionListener) null, this.mGoogleImsAdaptor);
            p2pSessionManager3.setReservedCallProfile(callProfile2);
            this.mGoogleImsAdaptor.setCallSession(999, session2);
            return session2;
        } catch (RemoteException e4) {
            throw new UnsupportedOperationException();
        }
    }

    public void createCmcCallSession() throws RemoteException {
        Log.d(LOG_TAG, "createCmcCallSession()");
        this.mConnectivityController.setNeedSubSession(false);
        int reservedId = this.mConnectivityController.getReservedId();
        String str = LOG_TAG;
        Log.d(str, "reservedId: " + reservedId);
        if (reservedId == -1) {
            Log.e(LOG_TAG, "sub(wifi-direct) session is already created, just return");
            return;
        }
        ImsCallSessionImpl sessionImpl = this.mGoogleImsAdaptor.getCallSession(reservedId);
        this.mConnectivityController.setReservedId(-1);
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            IImsCallSession foreSession = iVolteServiceModule.getForegroundSession();
            if (foreSession != null) {
                String str2 = LOG_TAG;
                Log.e(str2, "foreSession.getCmcType(): " + foreSession.getCmcType());
                Log.e(LOG_TAG, "pdcall is already connected. don't create subcallsession, just return");
                return;
            } else if (this.mVolteServiceModule.getExtMoCall()) {
                Log.e(LOG_TAG, "the call is MOcall. don't create subcallsession, just return");
                return;
            }
        }
        if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.None) {
            Log.e(LOG_TAG, "Not support p2p");
        } else if (sessionImpl == null) {
            Log.e(LOG_TAG, "sessionImpl is null");
        } else {
            ImsCallProfile profile = sessionImpl.getCallProfile();
            CallProfile p2pCallProfile = DataTypeConvertor.convertToSecCallProfile(this.mConnectivityController.getPhoneId(), sessionImpl.getCallProfile(), false);
            Bundle oemExtras = profile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            if (oemExtras != null) {
                if (oemExtras.containsKey("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID")) {
                    int boundSessionId = oemExtras.getInt("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID");
                    String str3 = LOG_TAG;
                    Log.e(str3, "boundSessionId: " + boundSessionId);
                    if (boundSessionId > 0) {
                        p2pCallProfile.setCmcBoundSessionId(boundSessionId);
                    }
                }
                if (oemExtras.containsKey("com.samsung.telephony.extra.CMC_DIAL_FROM")) {
                    String dialFrom = oemExtras.getString("com.samsung.telephony.extra.CMC_DIAL_FROM");
                    if (!TextUtils.isEmpty(dialFrom)) {
                        p2pCallProfile.setLetteringText(dialFrom);
                    }
                }
            }
            ServiceProfile service = this.mServiceProfile;
            Map<Integer, IImsCallSession> p2pSecSessionMap = new ConcurrentHashMap<>();
            if (this.mVolteServiceModule != null) {
                if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice) {
                    int cmcRegHandle = getCmcRegHandle(service, 7);
                    int p2pRegHandle = cmcRegHandle;
                    if (cmcRegHandle != -1) {
                        Log.d(LOG_TAG, "create session on WIFI-DIRECT PD");
                        p2pSecSessionMap.put(Integer.valueOf(p2pRegHandle), this.mVolteServiceModule.createSession(p2pCallProfile, p2pRegHandle));
                    }
                } else if (this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.SDevice) {
                    int cmcRegHandle2 = getCmcRegHandle(service, 8);
                    int p2pRegHandle2 = cmcRegHandle2;
                    if (cmcRegHandle2 != -1) {
                        Log.d(LOG_TAG, "create session on WIFI-DIRECT SD");
                        p2pSecSessionMap.put(Integer.valueOf(p2pRegHandle2), this.mVolteServiceModule.createSession(p2pCallProfile, p2pRegHandle2));
                    }
                }
            }
            String str4 = LOG_TAG;
            Log.d(str4, "subSecSessionMap size: " + p2pSecSessionMap.size());
            for (Map.Entry<Integer, IImsCallSession> e : p2pSecSessionMap.entrySet()) {
                e.getValue().setEpdgState(TextUtils.equals(profile.getCallExtra("CallRadioTech"), String.valueOf(18)));
            }
            if (p2pSecSessionMap.size() > 0) {
                boolean existMainSession = false;
                int cmcType = 5;
                if (this.mConnectivityController.isEnabledWifiDirectFeature()) {
                    cmcType = 7;
                }
                int type = 1;
                while (true) {
                    if (type > cmcType) {
                        break;
                    } else if (getCmcRegHandle(service, type) != -1) {
                        existMainSession = true;
                        break;
                    } else {
                        type += 2;
                    }
                }
                String str5 = LOG_TAG;
                Log.d(str5, "existMainSession: " + existMainSession);
                boolean setMainSession = false;
                CmcCallSessionManager p2pSessionManager = sessionImpl.getCmcCallSessionManager();
                for (Map.Entry<Integer, IImsCallSession> e2 : p2pSecSessionMap.entrySet()) {
                    if (setMainSession) {
                        p2pSessionManager.addP2pSession(e2.getValue());
                    } else if (!existMainSession) {
                        p2pSessionManager.setMainSession(e2.getValue());
                        setMainSession = true;
                    } else {
                        p2pSessionManager.addP2pSession(e2.getValue());
                    }
                }
                String str6 = LOG_TAG;
                Log.i(str6, "mSubSessionList size: " + p2pSessionManager.getP2pSessionSize());
                sessionImpl.initP2pImpl();
                this.mGoogleImsAdaptor.setCallSession(sessionImpl.getCallIdInt(), sessionImpl);
                p2pSessionManager.startP2pSessions(setMainSession);
            }
        }
    }

    public void getPendingCallSession(int phoneId, ImsCallProfile profile, IImsCallSession session) throws RemoteException {
        ImsCallProfile imsCallProfile = profile;
        Log.i(LOG_TAG, "getPendingCallSession()");
        if (this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(phoneId)) {
            Bundle extras = new Bundle();
            int cmcType = session.getCmcType();
            int sessionId = session.getSessionId();
            if (isCmcPrimaryType(cmcType)) {
                cmcType = 1;
            } else if (isCmcSecondaryType(cmcType)) {
                cmcType = 2;
            }
            String str = LOG_TAG;
            Log.i(str, "getPendingCallSession(), SEM_EXTRA_CMC_TYPE: (" + session.getCmcType() + " -> " + cmcType + ")");
            extras.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
            extras.putInt("com.samsung.telephony.extra.CMC_TYPE", cmcType);
            if (isCmcPrimaryType(cmcType)) {
                int cmcLineSlotIndex = ImsRegistry.getRegistrationManager().getCmcLineSlotIndex();
                extras.putInt("com.samsung.telephony.extra.CMC_PHONE_ID", cmcLineSlotIndex == -1 ? 0 : cmcLineSlotIndex);
            }
            imsCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", extras);
        } else if (this.mConnectivityController.isEnabledWifiDirectFeature() && this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice) {
            Bundle extras2 = new Bundle();
            int sessionId2 = session.getSessionId();
            String str2 = LOG_TAG;
            Log.i(str2, "getPendingCallSession(), SEM_EXTRA_CMC_TYPE: (" + session.getCmcType() + " -> " + 1 + ")");
            extras2.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId2);
            extras2.putInt("com.samsung.telephony.extra.CMC_TYPE", 1);
            if (1 == 7) {
                int cmcLineSlotIndex2 = ImsRegistry.getRegistrationManager().getCmcLineSlotIndex();
                extras2.putInt("com.samsung.telephony.extra.CMC_PHONE_ID", cmcLineSlotIndex2 == -1 ? 0 : cmcLineSlotIndex2);
            }
            imsCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", extras2);
        }
    }

    public void sendPublishDialog(int phoneId, PublishDialog publishDialog) throws RemoteException {
        int cmcType = 5;
        if (this.mConnectivityController.isEnabledWifiDirectFeature()) {
            cmcType = 7;
        }
        for (int type = 1; type <= cmcType; type += 2) {
            this.mVolteServiceModule.getCmcServiceHelper().sendPublishDialog(phoneId, publishDialog, type);
        }
    }

    public void onIncomingCall(int phoneId, Intent fillIn, ServiceProfile service, IImsCallSession secCallSession) {
        try {
            if (this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(phoneId)) {
                CallProfile cp = secCallSession.getCallProfile();
                int cmcType = secCallSession.getCmcType();
                int sessionId = secCallSession.getSessionId();
                if (isCmcPrimaryType(cmcType)) {
                    cmcType = 1;
                } else if (isCmcSecondaryType(cmcType)) {
                    cmcType = 2;
                }
                String str = LOG_TAG;
                Log.i(str, "onIncomingCall(), SEM_EXTRA_CMC_TYPE: (" + secCallSession.getCmcType() + " -> " + cmcType + ")");
                fillIn.putExtra("com.samsung.telephony.extra.CMC_TYPE", cmcType);
                fillIn.putExtra("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
                if (isCmcPrimaryType(cmcType)) {
                    fillIn.putExtra("com.samsung.telephony.extra.CMC_DIAL_TO", cp.getDialingNumber());
                    if (!TextUtils.isEmpty(cp.getReplaceSipCallId())) {
                        fillIn.putExtra("com.samsung.telephony.extra.CMC_REPLACE_CALL_ID", cp.getReplaceSipCallId());
                        fillIn.putExtra("com.samsung.telephony.extra.CMC_DEVICE_ID_BY_SD", cp.getCmcDeviceId());
                    } else if (!TextUtils.isEmpty(cp.getCmcDeviceId())) {
                        fillIn.putExtra("com.samsung.telephony.extra.CMC_DEVICE_ID", cp.getCmcDeviceId());
                    }
                    int cmcLineSlotIndex = ImsRegistry.getRegistrationManager().getCmcLineSlotIndex();
                    fillIn.putExtra("com.samsung.telephony.extra.CMC_PHONE_ID", cmcLineSlotIndex == -1 ? 0 : cmcLineSlotIndex);
                }
            } else if (this.mConnectivityController.isEnabledWifiDirectFeature() && this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice) {
                String str2 = LOG_TAG;
                Log.i(str2, "onIncomingCall(), SEM_EXTRA_CMC_TYPE: (" + secCallSession.getCmcType() + " -> " + 1 + ")");
                fillIn.putExtra("com.samsung.telephony.extra.CMC_TYPE", 1);
                fillIn.putExtra("com.samsung.telephony.extra.CMC_SESSION_ID", secCallSession.getSessionId());
            }
            if (this.mConnectivityController.isEnabledWifiDirectFeature() && this.mConnectivityController.getDeviceType() == CmcConnectivityController.DeviceType.PDevice && getCmcRegHandle(service, 7) == -1) {
                Log.e(LOG_TAG, "onIncomingCall: need wifi-direct connection, startCmcP2pConnection!");
                this.mConnectivityController.getNsdManager().acquireNetwork(new NsdNetworkCapabilities.Builder().addCapability(1).build());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isCmcPrimaryType(int cmcType) {
        if (cmcType == 1 || cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public boolean isP2pPrimaryType(int cmcType) {
        if (cmcType == 3 || cmcType == 5 || cmcType == 7) {
            return true;
        }
        return false;
    }

    public boolean isCmcSecondaryType(int cmcType) {
        if (cmcType == 2 || cmcType == 4 || cmcType == 8) {
            return true;
        }
        return false;
    }
}
