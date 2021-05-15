package com.sec.internal.google;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.ServiceState;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.ImsConferenceState;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsStreamMediaProfile;
import android.telephony.ims.aidl.IImsRegistration;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.telephony.ims.aidl.IImsSmsListener;
import android.telephony.ims.feature.ImsFeature;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsService;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.ISecImsMmTelEventListener;
import com.android.internal.telephony.PublishDialog;
import com.samsung.android.cmcnsd.CmcNsdManager;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.google.cmc.CmcCallSessionManager;
import com.sec.internal.google.cmc.CmcConnectivityController;
import com.sec.internal.google.cmc.CmcImsCallSessionImpl;
import com.sec.internal.google.cmc.CmcImsServiceUtil;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.google.IGoogleImsService;
import com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class GoogleImsService extends IImsService.Stub implements IGoogleImsService {
    private static final String IMS_CALL_PERMISSION = "android.permission.ACCESS_IMS_CALL_SERVICE";
    private static final String IMS_SERVICE = "ims6";
    private static final String LOG_TAG = "GoogleImsService";
    static GoogleImsService mInstance = null;
    static Map<Integer, ImsMultiEndPointImpl> mMultEndPoints = new ConcurrentHashMap();
    static int mServiceIdRef = 0;
    static Map<Integer, ServiceProfile> mServiceList = new ConcurrentHashMap();
    static ImsSmsImpl[] mSmsImpl = {null, null};
    static Map<Integer, IImsSmsListener> mSmsListenerList = new ConcurrentHashMap();
    Map<Integer, ImsCallSessionImpl> mCallSessionList = new ConcurrentHashMap();
    ImsFeature.Capabilities[] mCapabilities;
    private CmcImsServiceUtil mCmcImsServiceUtil = null;
    private ImsCallSessionImpl mConferenceHost = null;
    Map<Integer, IImsConfig> mConfigs = new ConcurrentHashMap();
    private CmcConnectivityController mConnectivityController = null;
    Context mContext;
    private Map<Integer, Bundle> mImsConferenceState = new HashMap();
    Map<Integer, ImsEcbmImpl> mImsEcbmList = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public ImsNotifier mImsNotifier = new ImsNotifier(this);
    private boolean mIsInitialMerge = false;
    Map<Integer, Uri[]> mOwnUris = new ConcurrentHashMap();
    int mPhoneCount;
    Map<Integer, IImsRegistration> mRegistrations = new ConcurrentHashMap();
    Map<Integer, ISecImsMmTelEventListener> mSecMmtelListener = new ConcurrentHashMap();
    IServiceModuleManager mServiceModuleManager;
    String mServiceUrn = "";
    IImsSmsListener mSmsListener = null;
    Map<Integer, IImsUt> mUtList = new ConcurrentHashMap();
    IVolteServiceModule mVolteServiceModule;

    public void setConnectivityController(CmcConnectivityController cController) {
        this.mConnectivityController = cController;
        this.mCmcImsServiceUtil = new CmcImsServiceUtil(this.mContext, this, this.mConnectivityController, this.mVolteServiceModule);
        this.mConnectivityController.setPhoneId(SimUtil.getDefaultPhoneId());
    }

    public boolean isEnabledWifiDirectFeature() {
        return this.mConnectivityController.isEnabledWifiDirectFeature();
    }

    public CmcConnectivityController.DeviceType getDeviceType() {
        return this.mConnectivityController.getDeviceType();
    }

    public CmcNsdManager getNsdManager() {
        return this.mConnectivityController.getNsdManager();
    }

    public CmcImsServiceUtil getCmcImsServiceUtil() {
        return this.mCmcImsServiceUtil;
    }

    public void preparePushCall(DialogEvent de) throws RemoteException {
        Log.i(LOG_TAG, "preparePushCall(), size: " + this.mCallSessionList.size());
        if (de == null && this.mCallSessionList.size() > 0) {
            Log.i(LOG_TAG, "Push for [PD]");
            for (Map.Entry<Integer, ImsCallSessionImpl> e : this.mCallSessionList.entrySet()) {
                ImsCallSessionImpl sessionImpl = e.getValue();
                if (sessionImpl.mSession != null && sessionImpl.isP2pPrimaryType(sessionImpl.mSession.getCmcType())) {
                    sessionImpl.mListener.callSessionResumeFailed(new ImsReasonInfo(6007, 6007));
                    return;
                }
            }
        } else if (de != null) {
            Log.i(LOG_TAG, "Push for [SD]");
            mMultEndPoints.get(Integer.valueOf(de.getPhoneId())).setP2pPushDialogInfo(de, getCmcTypeFromRegHandle(de.getRegId()));
        }
    }

    /* JADX WARNING: type inference failed for: r5v0, types: [com.sec.internal.google.GoogleImsService, android.os.IBinder] */
    private GoogleImsService(Context context, IServiceModuleManager smm) {
        this.mContext = context;
        this.mServiceModuleManager = smm;
        this.mVolteServiceModule = smm.getVolteServiceModule();
        if (ServiceManager.getService(IMS_SERVICE) == null) {
            ServiceManager.addService(IMS_SERVICE, this);
        }
        int phoneCount = TelephonyManagerWrapper.getInstance(this.mContext).getPhoneCount();
        this.mPhoneCount = phoneCount;
        this.mCapabilities = new ImsFeature.Capabilities[phoneCount];
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCapabilities[i] = new ImsFeature.Capabilities();
            this.mOwnUris.put(Integer.valueOf(i), new Uri[0]);
        }
    }

    public static synchronized GoogleImsService getInstance(Context context, IServiceModuleManager smm) {
        GoogleImsService googleImsService;
        synchronized (GoogleImsService.class) {
            if (mInstance == null) {
                mInstance = new GoogleImsService(context, smm);
            }
            googleImsService = mInstance;
        }
        return googleImsService;
    }

    public static int getRegistrationTech(int networkType) {
        if (networkType == 13 || networkType == 20) {
            return 0;
        }
        if (TelephonyManagerExt.getNetworkClass(networkType) == 2) {
            return 2;
        }
        if (networkType == 18) {
            return 1;
        }
        return -1;
    }

    private int getIncreasedServiceId() {
        int i = mServiceIdRef + 1;
        mServiceIdRef = i;
        if (i >= 254) {
            mServiceIdRef = 0;
        }
        return mServiceIdRef;
    }

    public int open(int phoneId, int serviceClass, PendingIntent incomingCallIntent, IImsRegistrationListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "open");
        if (incomingCallIntent == null || listener == null) {
            throw null;
        }
        if (this.mVolteServiceModule == null) {
            IVolteServiceModule volteServiceModule = this.mServiceModuleManager.getVolteServiceModule();
            this.mVolteServiceModule = volteServiceModule;
            if (volteServiceModule == null) {
                throw new RemoteException("Not ready to open");
            }
        }
        int serviceId = ((Integer) mServiceList.entrySet().stream().filter(new Predicate(serviceClass, phoneId) {
            public final /* synthetic */ int f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return GoogleImsService.lambda$open$0(this.f$0, this.f$1, (Map.Entry) obj);
            }
        }).findFirst().map($$Lambda$htemI6hNv3kq1UVGrXpRlPIVXRU.INSTANCE).orElse(Integer.valueOf(getIncreasedServiceId()))).intValue();
        mServiceList.put(Integer.valueOf(serviceId), new ServiceProfile(phoneId, serviceClass, listener));
        listener.registrationDisconnected(DataTypeConvertor.convertToGoogleImsReason(1000));
        if (serviceClass == 1) {
            listener.registrationFeatureCapabilityChanged(serviceClass, convertCapaToFeature(this.mCapabilities[phoneId]), (int[]) null);
            ImsRegistration[] registrationList = ImsRegistry.getRegistrationManager().getRegistrationInfo();
            if (!CollectionUtils.isNullOrEmpty((Object[]) registrationList)) {
                for (ImsRegistration reg : registrationList) {
                    if (reg.getPhoneId() == phoneId && reg.hasVolteService()) {
                        listener.registrationConnectedWithRadioTech(getRegistrationTech(reg.getCurrentRat()));
                        if (isOwnUrisChanged(phoneId, reg)) {
                            listener.registrationAssociatedUriChanged(this.mOwnUris.get(Integer.valueOf(phoneId)));
                        }
                    }
                }
            }
        }
        return serviceId;
    }

    static /* synthetic */ boolean lambda$open$0(int serviceClass, int phoneId, Map.Entry value) {
        return value.getValue() != null && ((ServiceProfile) value.getValue()).getServiceClass() == serviceClass && ((ServiceProfile) value.getValue()).getPhoneId() == phoneId;
    }

    public void close(int serviceId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "close");
        mServiceList.remove(Integer.valueOf(serviceId));
    }

    public boolean isConnected(int serviceId, int serviceType, int callType) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "isConnected");
        if (mServiceList.containsKey(Integer.valueOf(serviceId))) {
            return true;
        }
        throw new RemoteException();
    }

    public boolean isOpened(int serviceId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "isOpened");
        return mServiceList.containsKey(Integer.valueOf(serviceId));
    }

    public void setRegistrationListener(int serviceId, IImsRegistrationListener listener) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setRegistrationListener");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (service != null) {
            service.setRegistrationListener(listener);
            mServiceList.put(Integer.valueOf(serviceId), service);
        }
    }

    public IImsRegistration getRegistration(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getRegistration");
        if (this.mRegistrations.containsKey(Integer.valueOf(phoneId))) {
            return this.mRegistrations.get(Integer.valueOf(phoneId));
        }
        IImsRegistration regi = new ImsRegistrationImpl(phoneId);
        this.mRegistrations.put(Integer.valueOf(phoneId), regi);
        return regi;
    }

    public void addRegistrationListener(int phoneId, int serviceClass, IImsRegistrationListener listener) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "addRegistrationListener");
    }

    public ImsCallProfile createCallProfile(int serviceId, int serviceType, int callType) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "createCallProfile");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (!isConnected(serviceId, serviceType, callType) || service == null) {
            throw new RemoteException();
        }
        ImsCallProfile profile = new ImsCallProfile(serviceType, callType);
        ImsRegistration[] registrations = ImsRegistry.getRegistrationInfoByPhoneId(service.getPhoneId());
        if (registrations != null) {
            int length = registrations.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                ImsRegistration regi = registrations[i];
                if (regi != null && regi.getImsProfile() != null && regi.hasVolteService() && (serviceType == 2 || !regi.getImsProfile().hasEmergencySupport())) {
                    Mno mno = Mno.fromName(regi.getImsProfile().getMnoName());
                    profile.setCallExtraBoolean("SupportHeldHostMerge", mno == Mno.VZW || mno == Mno.USCC);
                    if (serviceType != 2 && regi.getImsProfile().getCmcType() == 0) {
                        profile.setCallExtra("CallRadioTech", String.valueOf(ServiceState.networkTypeToRilRadioTechnology(regi.getCurrentRat())));
                        break;
                    }
                }
                i++;
            }
        }
        return profile;
    }

    public IImsCallSession createCallSession(int serviceId, ImsCallProfile profile, IImsCallSessionListener listener) throws RemoteException, UnsupportedOperationException {
        IVolteServiceModule iVolteServiceModule;
        ImsUri normalizedUri;
        ImsCallProfile imsCallProfile = profile;
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "createCallSession");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (!isOpened(serviceId) || (iVolteServiceModule = this.mVolteServiceModule) == null || service == null) {
            throw new RemoteException();
        }
        try {
            CallProfile callProfile = DataTypeConvertor.convertToSecCallProfile(service.getPhoneId(), imsCallProfile, iVolteServiceModule.getTtyMode(service.getPhoneId()) != Extensions.TelecomManager.TTY_MODE_OFF);
            int cmcType = 0;
            this.mCmcImsServiceUtil.setServiceProfile(service);
            Bundle oemExtras = imsCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            if (oemExtras != null) {
                String janskyMsisdn = oemExtras.getString("com.samsung.telephony.extra.CALL_START_WITH_JANSKY_MSISDN");
                if (!TextUtils.isEmpty(janskyMsisdn) && (normalizedUri = this.mVolteServiceModule.getNormalizedUri(service.getPhoneId(), janskyMsisdn)) != null) {
                    callProfile.setLineMsisdn(UriUtil.getMsisdnNumber(normalizedUri));
                    callProfile.setOriginatingUri(normalizedUri);
                }
                if (oemExtras.containsKey("com.samsung.telephony.extra.CMC_TYPE")) {
                    cmcType = oemExtras.getInt("com.samsung.telephony.extra.CMC_TYPE");
                }
            }
            int cmcType2 = this.mCmcImsServiceUtil.prepareCallSession(cmcType, imsCallProfile, callProfile, service);
            if (cmcType2 > 0) {
                return this.mCmcImsServiceUtil.createCallSession(cmcType2, serviceId, profile, callProfile, service);
            }
            ImsRegistration[] registrations = ImsRegistry.getRegistrationInfoByPhoneId(service.getPhoneId());
            if (registrations != null) {
                for (ImsRegistration regi : registrations) {
                    if (!(regi == null || regi.getImsProfile() == null || !regi.hasVolteService())) {
                        if (cmcType2 == regi.getImsProfile().getCmcType()) {
                            if (callProfile.getUrn() == "urn:service:unspecified") {
                                if (this.mServiceUrn.isEmpty()) {
                                    callProfile.setUrn(ImsCallUtil.ECC_SERVICE_URN_DEFAULT);
                                } else {
                                    callProfile.setUrn(this.mServiceUrn);
                                    this.mServiceUrn = "";
                                }
                            }
                        }
                    }
                }
            }
            int volteRegHandle = getVolteRegHandle(service);
            if (ImsRegistry.getCmcAccountManager().isSecondaryDevice() && volteRegHandle == -1 && TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable() && cmcType2 == 0 && !ImsUtil.isCdmalessEnabled(service.getPhoneId()) && !ImsCallUtil.isE911Call(callProfile.getCallType())) {
                callProfile.setForceCSFB(true);
            }
            com.sec.ims.volte2.IImsCallSession secCallSession = this.mVolteServiceModule.createSession(callProfile);
            secCallSession.setEpdgState(TextUtils.equals(imsCallProfile.getCallExtra("CallRadioTech"), String.valueOf(18)));
            ImsCallSessionImpl session = new ImsCallSessionImpl(imsCallProfile, secCallSession, (android.telephony.ims.aidl.IImsCallSessionListener) null, this);
            this.mCallSessionList.put(Integer.valueOf(session.getCallIdInt()), session);
            if (isEnabledWifiDirectFeature()) {
                this.mCmcImsServiceUtil.acquireP2pNetwork();
            }
            callProfile.setCmcType(cmcType2);
            if (SimUtil.isSoftphoneEnabled() && callProfile.getCallType() == 7) {
                callProfile.setCallType(13);
                callProfile.setUrn((String) null);
            }
            return session;
        } catch (RemoteException e) {
            throw new UnsupportedOperationException();
        }
    }

    public IImsCallSession getPendingCallSession(int serviceId, String callId) throws RemoteException {
        int oir;
        int i;
        ImsCallSessionImpl imsCallSessionImpl;
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getPendingCallSession");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (!isOpened(serviceId) || this.mVolteServiceModule == null || service == null) {
            String str = callId;
            ServiceProfile serviceProfile = service;
            throw new RemoteException();
        }
        int phoneId = service.getPhoneId();
        com.sec.ims.volte2.IImsCallSession session = this.mVolteServiceModule.getPendingSession(callId);
        CallConstants.STATE sessionState = CallConstants.STATE.values()[session.getCallStateOrdinal()];
        if (sessionState == CallConstants.STATE.EndingCall || sessionState == CallConstants.STATE.EndedCall) {
            int i2 = phoneId;
            throw new RemoteException();
        }
        CallProfile cp = session.getCallProfile();
        ImsCallProfile profile = new ImsCallProfile(1, DataTypeConvertor.convertToGoogleCallType(cp.getCallType()), prepareComposerDataBundle(cp.getComposerData()), new ImsStreamMediaProfile());
        ImsRegistration registration = session.getRegistration();
        if (registration != null) {
            int currentRat = registration.getCurrentRat();
            if (cp.getRadioTech() != 0) {
                currentRat = cp.getRadioTech();
            }
            if (this.mCmcImsServiceUtil.isCmcSecondaryType(session.getCmcType())) {
                profile.setCallExtra("CallRadioTech", String.valueOf(14));
            } else {
                profile.setCallExtra("CallRadioTech", String.valueOf(currentRat));
            }
            session.setEpdgStateNoNotify(currentRat == 18);
        }
        profile.setCallExtra("oi", cp.getDialingNumber());
        profile.mMediaProfile = DataTypeConvertor.convertToGoogleMediaProfile(cp.getMediaProfile());
        String number = cp.getDialingNumber();
        String Pletteting = cp.getLetteringText();
        if (TextUtils.isEmpty(number)) {
            number = NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
        }
        if (Mno.fromName(registration.getImsProfile().getMnoName()) == Mno.DOCOMO) {
            oir = getOirExtraFromDialingNumberForDcm(Pletteting);
        } else {
            oir = getOirExtraFromDialingNumber(number);
        }
        profile.setCallExtraInt("oir", oir);
        profile.setCallExtraInt("cnap", oir);
        profile.setCallExtra("cna", cp.getLetteringText());
        profile.setCallExtra("com.samsung.telephony.extra.PHOTO_RING_AVAILABLE", cp.getPhotoRing());
        profile.setCallExtraBoolean("com.samsung.telephony.extra.IS_TWO_PHONE_MODE", !TextUtils.isEmpty(cp.getNumberPlus()));
        profile.setCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE", "1".equals(cp.getIsFocus()));
        profile.setCallExtra("com.samsung.telephony.extra.DUAL_NUMBER", cp.getNumberPlus());
        profile.setCallExtra("com.samsung.telephony.extra.ALERT_INFO", cp.getAlertInfo());
        profile.setCallExtra("com.samsung.telephony.extra.LINE_MSISDN", cp.getLineMsisdn());
        profile.mMediaProfile.setRttMode(cp.getMediaProfile().getRttMode());
        if (cp.getHistoryInfo() != null) {
            profile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_REDIRECT_NUMBER", cp.getHistoryInfo());
            if ("anonymous".equalsIgnoreCase(cp.getHistoryInfo())) {
                profile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "1");
            } else {
                profile.setCallExtra("com.samsung.telephony.extra.CALL_FORWARDING_PRESENTATION", "0");
            }
        }
        if (cp.getVerstat() != null) {
            profile.setCallExtra("com.samsung.telephony.extra.ims.VERSTAT", cp.getVerstat());
            if (cp.getVerstat().equals("TN-Validation-Passed")) {
                profile.setCallerNumberVerificationStatus(1);
                i = 0;
            } else if (cp.getVerstat().equals("TN-Validation-Failed")) {
                profile.setCallerNumberVerificationStatus(2);
                i = 0;
            } else {
                i = 0;
                profile.setCallerNumberVerificationStatus(0);
            }
        } else {
            i = 0;
            profile.setCallerNumberVerificationStatus(0);
        }
        if (cp.getHDIcon() == 1) {
            profile.mRestrictCause = i;
        } else {
            profile.mRestrictCause = 3;
        }
        this.mCmcImsServiceUtil.getPendingCallSession(phoneId, profile, session);
        if (session.getCmcType() > 0) {
            Log.d(LOG_TAG, "getPendingCallSession, create imsCallSessionImpl for [CMC+D2D volte call]");
            ServiceProfile serviceProfile2 = service;
            int i3 = phoneId;
            imsCallSessionImpl = new CmcImsCallSessionImpl(profile, new CmcCallSessionManager(session, this.mVolteServiceModule, getNsdManager(), isEnabledWifiDirectFeature()), (android.telephony.ims.aidl.IImsCallSessionListener) null, this);
        } else {
            int i4 = phoneId;
            Log.d(LOG_TAG, "getPendingCallSession, create imsCallSessionImpl for [NORMAL volte call]");
            imsCallSessionImpl = new ImsCallSessionImpl(profile, session, (android.telephony.ims.aidl.IImsCallSessionListener) null, this);
        }
        this.mCallSessionList.put(Integer.valueOf(imsCallSessionImpl.getCallIdInt()), imsCallSessionImpl);
        return imsCallSessionImpl;
    }

    public IImsUt getUtInterface(int serviceId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getUtInterface");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (service == null) {
            return null;
        }
        int phoneId = service.getPhoneId();
        if (this.mUtList.containsKey(Integer.valueOf(phoneId))) {
            return this.mUtList.get(Integer.valueOf(phoneId));
        }
        IImsUt ut = new ImsUtImpl(phoneId);
        this.mUtList.put(Integer.valueOf(phoneId), ut);
        return ut;
    }

    public IImsConfig getConfigInterface(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getConfigInterface");
        if (this.mConfigs.containsKey(Integer.valueOf(phoneId))) {
            return this.mConfigs.get(Integer.valueOf(phoneId));
        }
        IImsConfig config = new ImsConfigImpl(phoneId);
        this.mConfigs.put(Integer.valueOf(phoneId), config);
        return config;
    }

    public void turnOnIms(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "turnOnIms");
    }

    public void turnOffIms(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "turnOffIms");
    }

    public ImsEcbmImpl getEcbmInterface(int serviceId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getEcbmInterface");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (service == null) {
            return null;
        }
        return getEcbmInterfaceForPhoneId(service.getPhoneId());
    }

    private ImsEcbmImpl getEcbmInterfaceForPhoneId(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getEcbmInterfaceForPhoneId");
        if (this.mImsEcbmList.containsKey(Integer.valueOf(phoneId))) {
            return this.mImsEcbmList.get(Integer.valueOf(phoneId));
        }
        ImsEcbmImpl ecbm = new ImsEcbmImpl();
        this.mImsEcbmList.put(Integer.valueOf(phoneId), ecbm);
        return ecbm;
    }

    public void setUiTTYMode(int serviceId, int uiTtyMode, Message onComplete) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setUiTTYMode");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (service != null) {
            int phoneId = service.getPhoneId();
            IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
            if (iVolteServiceModule != null) {
                iVolteServiceModule.setUiTTYMode(phoneId, uiTtyMode, onComplete);
                return;
            }
            throw new RemoteException();
        }
    }

    public void setTtyMode(int phoneId, int mode) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setTtyMode");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.setTtyMode(phoneId, mode);
            return;
        }
        throw new RemoteException();
    }

    public IImsMultiEndpoint getMultiEndpointInterface(int serviceId) {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getMultiEndpointInterface");
        ServiceProfile service = mServiceList.get(Integer.valueOf(serviceId));
        if (service == null) {
            return null;
        }
        int phoneId = service.getPhoneId();
        if (mMultEndPoints.containsKey(Integer.valueOf(phoneId))) {
            return mMultEndPoints.get(Integer.valueOf(phoneId));
        }
        ImsMultiEndPointImpl multiEndpoint = new ImsMultiEndPointImpl(service.getPhoneId());
        mMultEndPoints.put(Integer.valueOf(phoneId), multiEndpoint);
        return multiEndpoint;
    }

    public void changeAudioPath(int phoneId, int direction) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "changeAudioPath");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            iVolteServiceModule.updateAudioInterface(phoneId, direction);
            return;
        }
        throw new RemoteException();
    }

    public int startLocalRingBackTone(int streamType, int volume, int toneType) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "startLocalRingBackTone");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.startLocalRingBackTone(streamType, volume, toneType);
        }
        throw new RemoteException();
    }

    public int stopLocalRingBackTone() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "stopLocalRingBackTone");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.stopLocalRingBackTone();
        }
        throw new RemoteException();
    }

    public String getTrn(String srcMsisdn, String dstMsisdn) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getTrn");
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            return iVolteServiceModule.getTrn(srcMsisdn, dstMsisdn);
        }
        throw new RemoteException();
    }

    public void sendPublishDialog(int phoneId, PublishDialog publishDialog) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendPublishDialog");
        if (this.mVolteServiceModule != null) {
            this.mCmcImsServiceUtil.sendPublishDialog(phoneId, publishDialog);
            return;
        }
        throw new RemoteException();
    }

    public void triggerAutoConfigurationForApp(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "triggerAutoConfigurationForApp");
        ImsRegistry.triggerAutoConfigurationForApp(phoneId);
    }

    public void setSecImsMmTelEventListener(int phoneId, ISecImsMmTelEventListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setSecImsMmTelEventListener");
        this.mSecMmtelListener.put(Integer.valueOf(phoneId), listener);
    }

    public void sendSms(int phoneId, int token, int messageRef, String format, String smsc, boolean isRetry, byte[] pdu) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendSms");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].sendSms(phoneId, token, messageRef, format, smsc, pdu);
            return;
        }
        throw new RemoteException();
    }

    public void setRetryCount(int phoneId, int token, int retryCount) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendSms");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].setRetryCount(phoneId, token, retryCount);
            return;
        }
        throw new RemoteException();
    }

    public void acknowledgeSms(int phoneId, int token, int messageRef, int result) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSms");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].acknowledgeSms(phoneId, token, messageRef, result);
            return;
        }
        throw new RemoteException();
    }

    public void acknowledgeSmsReport(int phoneId, int token, int messageRef, int result) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSmsReport");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].acknowledgeSmsReport(phoneId, token, messageRef, result);
            return;
        }
        throw new RemoteException();
    }

    public void acknowledgeSmsWithPdu(int phoneId, int token, int msgRef, byte[] data) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "acknowledgeSmsWithPdu");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].acknowledgeSmsWithPdu(phoneId, token, msgRef, data);
            return;
        }
        throw new RemoteException();
    }

    public void setSmsListener(int phoneId, IImsSmsListener listener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "setSmsListener");
        if (mSmsListenerList.containsKey(Integer.valueOf(phoneId))) {
            if (mSmsListenerList.get(Integer.valueOf(phoneId)) != listener) {
                mSmsListenerList.replace(Integer.valueOf(phoneId), listener);
            }
            this.mSmsListener = listener;
            return;
        }
        this.mSmsListener = listener;
        mSmsListenerList.put(Integer.valueOf(phoneId), this.mSmsListener);
    }

    public void onSmsReady(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "onSmsReady");
        mSmsImpl[phoneId] = new ImsSmsImpl(this.mContext, phoneId, this.mSmsListener);
        mSmsImpl[phoneId].updateTPMR(phoneId);
    }

    public void sendRpSmma(int phoneId, String smsc) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "sendRpSmma");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            imsSmsImplArr[phoneId].sendRpSmma(phoneId, smsc);
            return;
        }
        throw new RemoteException();
    }

    public String getSmsFormat(int phoneId) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(IMS_CALL_PERMISSION, "getSmsFormat");
        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
        if (imsSmsImplArr[phoneId] != null) {
            return imsSmsImplArr[phoneId].getSmsFormat(phoneId);
        }
        throw new RemoteException();
    }

    public ImsCallSessionImpl getConferenceHost() {
        return this.mConferenceHost;
    }

    public void setConferenceHost(ImsCallSessionImpl hostSession) {
        this.mConferenceHost = hostSession;
        if (hostSession == null) {
            this.mImsConferenceState.clear();
        }
    }

    public boolean hasConferenceHost() {
        return this.mConferenceHost != null;
    }

    public boolean isInitialMerge() {
        return this.mIsInitialMerge;
    }

    public void setInitialMerge(boolean initialMerge) {
        this.mIsInitialMerge = initialMerge;
    }

    public ImsCallSessionImpl getCallSession(int callId) {
        return this.mCallSessionList.get(Integer.valueOf(callId));
    }

    public void setCallSession(int callId, ImsCallSessionImpl sessionImpl) {
        this.mCallSessionList.put(Integer.valueOf(callId), sessionImpl);
    }

    public void setServiceUrn(String mServiceUrn2) {
        this.mServiceUrn = mServiceUrn2;
    }

    public void putConferenceState(int callId, String user, String endPoint, String status, ImsCallProfile callprofile) {
        Bundle confState = new Bundle();
        confState.putString("user", user);
        confState.putString("endpoint", endPoint);
        confState.putString("status", status);
        confState.putInt("callId", callId);
        confState.putString("cna", callprofile.getCallExtra("cna"));
        confState.putInt("cnap", callprofile.getCallExtraInt("cnap"));
        confState.putInt("oir", callprofile.getCallExtraInt("oir"));
        confState.putInt("audioQuality", callprofile.getMediaProfile().getAudioQuality());
        confState.putBoolean("com.samsung.telephony.extra.MT_CONFERENCE", callprofile.getCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE"));
        confState.putString("com.samsung.telephony.extra.ims.VERSTAT", callprofile.getCallExtra("com.samsung.telephony.extra.ims.VERSTAT"));
        this.mImsConferenceState.put(Integer.valueOf(callId), confState);
    }

    public void putConferenceStateList(int userId, int callId, String user, String endPoint, String status, int sipError, ImsCallProfile callprofile) {
        Bundle confState = new Bundle();
        confState.putString("user", user);
        confState.putString("endpoint", endPoint);
        confState.putString("status", status);
        confState.putInt("callId", callId);
        confState.putInt("sipError", sipError);
        confState.putString("uriType", "tel");
        confState.putString("cna", callprofile.getCallExtra("cna"));
        confState.putInt("cnap", callprofile.getCallExtraInt("cnap"));
        confState.putInt("oir", callprofile.getCallExtraInt("oir"));
        confState.putInt("audioQuality", callprofile.getMediaProfile().getAudioQuality());
        confState.putBoolean("com.samsung.telephony.extra.MT_CONFERENCE", callprofile.getCallExtraBoolean("com.samsung.telephony.extra.MT_CONFERENCE"));
        confState.putString("com.samsung.telephony.extra.ims.VERSTAT", callprofile.getCallExtra("com.samsung.telephony.extra.ims.VERSTAT"));
        if ("disconnected".equals(status)) {
            confState.putInt("disconnectCause", 2);
        }
        this.mImsConferenceState.put(Integer.valueOf(userId), confState);
    }

    public void removeConferenceState(int callId) {
        this.mImsConferenceState.remove(Integer.valueOf(callId));
    }

    public ImsConferenceState getImsConferenceState() {
        ImsConferenceState imsConfState = new ImsConferenceState();
        for (Map.Entry<Integer, Bundle> conference : this.mImsConferenceState.entrySet()) {
            imsConfState.mParticipants.put(conference.getKey().toString(), conference.getValue());
        }
        return imsConfState;
    }

    public void updateSecConferenceInfo(ImsCallProfile callProfile) {
        Bundle newSecConferenceInfo = new Bundle();
        Bundle oldSecConferenceInfo = callProfile.mCallExtras.getBundle("secConferenceInfo");
        for (Map.Entry<Integer, Bundle> conference : this.mImsConferenceState.entrySet()) {
            Integer callId = conference.getKey();
            Bundle newValue = conference.getValue();
            Bundle oldValue = null;
            if (oldSecConferenceInfo != null) {
                Iterator it = oldSecConferenceInfo.keySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    String oldCallId = (String) it.next();
                    if (TextUtils.equals(oldCallId, callId.toString())) {
                        oldValue = oldSecConferenceInfo.getBundle(oldCallId);
                        break;
                    }
                }
            }
            if (oldValue != null) {
                oldValue.putAll(newValue);
                newSecConferenceInfo.putBundle(callId.toString(), oldValue);
            } else {
                ImsCallSessionImpl session = getCallSession(callId.intValue());
                int origCallId = newValue.getInt("callId");
                if (origCallId > 0) {
                    session = getCallSession(origCallId);
                }
                if (session != null) {
                    newValue.putString("cna", session.getCallProfile().getCallExtra("cna"));
                    CallProfile cp = null;
                    try {
                        com.sec.ims.volte2.IImsCallSession s = this.mVolteServiceModule.getSessionByCallId(callId.intValue());
                        if (s != null) {
                            cp = s.getCallProfile();
                        }
                    } catch (RemoteException e) {
                    }
                    if (cp != null && !TextUtils.isEmpty(cp.getVerstat())) {
                        newValue.putString("com.samsung.telephony.extra.ims.VERSTAT", cp.getVerstat());
                    }
                    newSecConferenceInfo.putBundle(callId.toString(), newValue);
                }
            }
        }
        callProfile.mCallExtras.putBundle("secConferenceInfo", newSecConferenceInfo);
    }

    public int getParticipantId(String user) {
        try {
            int callId = Integer.parseInt(user);
            if (this.mImsConferenceState.containsKey(Integer.valueOf(callId))) {
                return callId;
            }
        } catch (IllegalArgumentException e) {
        }
        for (Map.Entry<Integer, Bundle> participant : this.mImsConferenceState.entrySet()) {
            if (user.equals(participant.getValue().getString("user"))) {
                return participant.getKey().intValue();
            }
        }
        return -1;
    }

    public void updateParticipant(int callId, String status) {
        updateParticipant(callId, (String) null, (String) null, status, -1);
    }

    public void updateParticipant(int callId, String status, int disconnectCause) {
        updateParticipant(callId, (String) null, (String) null, status, disconnectCause);
    }

    public void updateParticipant(int callId, String user, String endPoint, String status, int disconnectCause) {
        Bundle confState = this.mImsConferenceState.get(Integer.valueOf(callId));
        if (confState != null) {
            if (!TextUtils.isEmpty(user)) {
                confState.putString("user", user);
            }
            if (!TextUtils.isEmpty(endPoint)) {
                confState.putString("endpoint", endPoint);
            }
            if (!TextUtils.isEmpty(status)) {
                confState.putString("status", status);
            }
            if (disconnectCause != -1) {
                confState.putInt("android.telephony.ims.extra.CALL_DISCONNECT_CAUSE", disconnectCause);
            }
            this.mImsConferenceState.replace(Integer.valueOf(callId), confState);
        }
    }

    public void enterEmergencyCallbackMode(int phoneId) throws RemoteException {
        getEcbmInterfaceForPhoneId(phoneId).enterEmergencyCallbackMode();
    }

    public int getOirExtraFromDialingNumber(String number) {
        if (NSDSNamespaces.NSDSSimAuthType.UNKNOWN.equalsIgnoreCase(number)) {
            return 3;
        }
        if ("RESTRICTED".equalsIgnoreCase(number) || number.toLowerCase(Locale.US).contains("anonymous")) {
            return 1;
        }
        if ("Coin line/payphone".equalsIgnoreCase(number)) {
            return 4;
        }
        if ("Interaction with other service".equalsIgnoreCase(number) || "Unavailable".equalsIgnoreCase(number)) {
            return 3;
        }
        return 2;
    }

    public int getOirExtraFromDialingNumberForDcm(String Pletteting) {
        if (!TextUtils.isEmpty(Pletteting) && Pletteting.startsWith("Anonymous")) {
            return 1;
        }
        if (!TextUtils.isEmpty(Pletteting) && Pletteting.startsWith("Coin line/payphone")) {
            return 4;
        }
        if (TextUtils.isEmpty(Pletteting) || Pletteting.length() <= 0) {
            return 2;
        }
        return 3;
    }

    public void updateCapabilities(int phoneId, ImsFeature.Capabilities capabilities) {
        if (!this.mCapabilities[phoneId].equals(capabilities)) {
            this.mCapabilities[phoneId] = capabilities.copy();
            this.mImsNotifier.notifyFeatureCapableChanged(phoneId);
        }
    }

    public void updateCapabilities(int phoneId, int[] capabilities, boolean[] capables) {
        Log.i(LOG_TAG, "updateCapabilities, phoneId: " + phoneId);
        boolean capabilityUpdated = false;
        for (int capability = 0; capability < capabilities.length; capability++) {
            if (capables[capability] != this.mCapabilities[phoneId].isCapable(capabilities[capability])) {
                if (capables[capability]) {
                    this.mCapabilities[phoneId].addCapabilities(capabilities[capability]);
                    if (capabilities[capability] == 8) {
                        ImsSmsImpl[] imsSmsImplArr = mSmsImpl;
                        if (imsSmsImplArr[phoneId] != null) {
                            imsSmsImplArr[phoneId].updateTPMR(phoneId);
                        }
                    }
                } else {
                    this.mCapabilities[phoneId].removeCapabilities(capabilities[capability]);
                }
                capabilityUpdated = true;
            }
        }
        if (capabilityUpdated) {
            this.mImsNotifier.notifyFeatureCapableChanged(phoneId);
        }
    }

    public void onCallClosed(int callIdInt) {
        this.mCallSessionList.remove(Integer.valueOf(callIdInt));
    }

    /* access modifiers changed from: package-private */
    public int[] convertCapaToFeature(ImsFeature.Capabilities capabilities) {
        int[] features = {-1, -1, -1, -1, -1, -1, -1, -1, -1};
        if (capabilities.isCapable(1)) {
            features[0] = 0;
            features[2] = 2;
        }
        if (capabilities.isCapable(2)) {
            features[1] = 1;
            features[3] = 3;
        }
        if (capabilities.isCapable(4)) {
            features[4] = 4;
            features[5] = 5;
        }
        if (capabilities.isCapable(8)) {
            features[6] = 6;
            features[7] = 7;
        }
        return features;
    }

    public int getVolteRegHandle(ServiceProfile service) {
        if (service.getServiceClass() != 1) {
            return -1;
        }
        for (ImsRegistration reg : ImsRegistry.getRegistrationManager().getRegistrationInfo()) {
            if (reg != null && reg.getPhoneId() == service.getPhoneId() && reg.hasVolteService() && reg.getImsProfile() != null && reg.getImsProfile().getCmcType() == 0) {
                return reg.getHandle();
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int getCmcTypeFromRegHandle(int regHandle) {
        for (ImsRegistration reg : ImsRegistry.getRegistrationManager().getRegistrationInfo()) {
            if (reg != null && regHandle == reg.getHandle() && reg.getImsProfile() != null) {
                return reg.getImsProfile().getCmcType();
            }
        }
        return -1;
    }

    private Uri[] extractOwnUrisFromReg(ImsRegistration reg) {
        if (reg == null || CollectionUtils.isNullOrEmpty((Collection<?>) reg.getImpuList())) {
            return new Uri[0];
        }
        return (Uri[]) reg.getImpuList().stream().filter($$Lambda$GoogleImsService$j4oR8wOS3QH8MgHgpOwnrA0XLGA.INSTANCE).map($$Lambda$GoogleImsService$GyhyRv54YAwXDpQDtEf5Wlrh0.INSTANCE).toArray($$Lambda$GoogleImsService$xMi4NpfdpNYkU0AZXK7IS7B9uw.INSTANCE);
    }

    static /* synthetic */ Uri[] lambda$extractOwnUrisFromReg$3(int x$0) {
        return new Uri[x$0];
    }

    /* access modifiers changed from: package-private */
    public boolean isOwnUrisChanged(int phoneId, ImsRegistration reg) {
        Uri[] latestUris = extractOwnUrisFromReg(reg);
        if (Arrays.deepEquals((Object[]) this.mOwnUris.get(Integer.valueOf(phoneId)), latestUris)) {
            return false;
        }
        this.mOwnUris.put(Integer.valueOf(phoneId), latestUris);
        return true;
    }

    public ImsNotifier getImsNotifier() {
        return this.mImsNotifier;
    }

    public Bundle prepareComposerDataBundle(Bundle cBundle) {
        Bundle callExtras = new Bundle();
        if (cBundle != null && !cBundle.isEmpty()) {
            if (cBundle.containsKey(CallConstants.ComposerData.IMPORTANCE)) {
                callExtras.putBoolean(ImsConstants.Intents.EXTRA_CALL_IMPORTANCE, cBundle.getBoolean(CallConstants.ComposerData.IMPORTANCE));
            }
            if (cBundle.containsKey(CallConstants.ComposerData.IMAGE)) {
                callExtras.putString(ImsConstants.Intents.EXTRA_CALL_IMAGE, cBundle.getString(CallConstants.ComposerData.IMAGE));
            }
            if (cBundle.containsKey("subject")) {
                callExtras.putString(ImsConstants.Intents.EXTRA_CALL_SUBJECT, cBundle.getString("subject"));
            }
            if (cBundle.containsKey(CallConstants.ComposerData.LONGITUDE)) {
                callExtras.putString(ImsConstants.Intents.EXTRA_CALL_LONGITUDE, cBundle.getString(CallConstants.ComposerData.LONGITUDE));
            }
            if (cBundle.containsKey(CallConstants.ComposerData.LATITUDE)) {
                callExtras.putString(ImsConstants.Intents.EXTRA_CALL_LATITUDE, cBundle.getString(CallConstants.ComposerData.LATITUDE));
            }
            if (cBundle.containsKey(CallConstants.ComposerData.RADIUS)) {
                callExtras.putString(ImsConstants.Intents.EXTRA_CALL_RADIUS, cBundle.getString(CallConstants.ComposerData.RADIUS));
            }
        }
        return callExtras;
    }

    private class ImsRegistrationImpl extends IImsRegistration.Stub {
        private int mPhoneId = 0;

        public ImsRegistrationImpl(int phoneId) {
            this.mPhoneId = phoneId;
        }

        public int getRegistrationTechnology() {
            ImsRegistration[] registrationList = ImsRegistry.getRegistrationManager().getRegistrationInfo();
            int regiTech = -1;
            if (!CollectionUtils.isNullOrEmpty((Object[]) registrationList)) {
                for (ImsRegistration regi : registrationList) {
                    if (regi.getPhoneId() == this.mPhoneId && regi.hasVolteService()) {
                        Mno mno = Mno.fromName(regi.getImsProfile().getMnoName());
                        if (!regi.getImsProfile().hasEmergencySupport()) {
                            if (!mno.isKor() || regi.getRegiRat() == 18) {
                                regiTech = GoogleImsService.getRegistrationTech(regi.getCurrentRat());
                            } else {
                                regiTech = 0;
                            }
                        }
                    }
                }
            }
            return regiTech;
        }

        public void addRegistrationCallback(IImsRegistrationCallback callback) {
            GoogleImsService.this.mImsNotifier.addCallback(this.mPhoneId, callback);
        }

        public void removeRegistrationCallback(IImsRegistrationCallback callback) {
            GoogleImsService.this.mImsNotifier.removeCallback(this.mPhoneId, callback);
        }
    }
}
