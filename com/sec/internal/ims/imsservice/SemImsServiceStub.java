package com.sec.internal.ims.imsservice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.ims.ISemEpdgListener;
import com.samsung.android.ims.SemAutoConfigListener;
import com.samsung.android.ims.SemImsDmConfigListener;
import com.samsung.android.ims.SemImsRegiListener;
import com.samsung.android.ims.SemImsRegistration;
import com.samsung.android.ims.SemImsRegistrationError;
import com.samsung.android.ims.SemImsService;
import com.samsung.android.ims.SemSimMobStatusListener;
import com.samsung.android.ims.cmc.ISemCmcRecordingListener;
import com.samsung.android.ims.cmc.SemCmcRecordingInfo;
import com.samsung.android.ims.ft.SemImsFtListener;
import com.samsung.android.ims.settings.SemImsProfile;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.ims.IEpdgListener;
import com.sec.ims.IImsDmConfigListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ISimMobilityStatusListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SemImsServiceStub extends SemImsService.Stub {
    public static final Uri AUTO_CONFIGURATION_VERS_URI = Uri.parse(ConfigConstants.CONFIG_URI);
    private static final String IMS_SEAPI_SERVICE = "ImsBase";
    private static final String LOG_TAG = SemImsServiceStub.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    public static final String RCS_AUTOCONFIG_URI = "com.samsung.rcs.autoconfigurationprovider";
    private static SemImsServiceStub sInstance = null;
    private Map<Integer, RemoteCallbackList<SemAutoConfigListener>> mAutoCofigListeners = new ConcurrentHashMap();
    private Map<Integer, Map<SemAutoConfigListener, AutoConfigCallBack>> mAutoConfigCallbacks = new ConcurrentHashMap();
    private Context mContext;
    private final HandlerThread mCoreThread;
    private ImsDmConfigCallBack mDmConfigCallbacks;
    /* access modifiers changed from: private */
    public RemoteCallbackList<SemImsDmConfigListener> mDmConfigListeners = new RemoteCallbackList<>();
    /* access modifiers changed from: private */
    public boolean[] mEpdgAvailable = new boolean[SimUtil.getPhoneCount()];
    private EpdgListenerCallback mEpdgHandoverCallback;
    /* access modifiers changed from: private */
    public RemoteCallbackList<ISemEpdgListener> mEpdgListeners = new RemoteCallbackList<>();
    private ImsOngoingFtEventCallBack mOngoingFtEventCallback;
    /* access modifiers changed from: private */
    public RemoteCallbackList<SemImsFtListener> mOngoingFtEventListeners = new RemoteCallbackList<>();
    private int mRcsConfigVers = 0;
    private Map<Integer, Map<SemImsRegiListener, ImsRegistrationCallBack>> mRegCallbacks = new ConcurrentHashMap();
    private Map<Integer, RemoteCallbackList<SemImsRegiListener>> mRegListeners = new ConcurrentHashMap();
    private final Map<Integer, RemoteCallbackList<IImsRegistrationCallback>> mRegistrationCallbacks = new ConcurrentHashMap();
    private Map<Integer, Map<SemSimMobStatusListener, SimMobilityStatusCallBack>> mSimMobilityStatusCallbacks = new ConcurrentHashMap();
    private Map<Integer, RemoteCallbackList<SemSimMobStatusListener>> mSimMobilityStatusListeners = new ConcurrentHashMap();

    /* JADX WARNING: type inference failed for: r2v0, types: [com.sec.internal.ims.imsservice.SemImsServiceStub, java.lang.Object, android.os.IBinder] */
    private SemImsServiceStub(Context context) {
        this.mContext = context;
        this.mCoreThread = new HandlerThread(getClass().getSimpleName());
        ServiceManager.addService(IMS_SEAPI_SERVICE, this);
        Log.d(LOG_TAG, "SemImsServiceStub added");
    }

    public static synchronized SemImsServiceStub makeSemImsService(Context context) {
        synchronized (SemImsServiceStub.class) {
            if (sInstance != null) {
                Log.d(LOG_TAG, "Already created.");
                SemImsServiceStub semImsServiceStub = sInstance;
                return semImsServiceStub;
            }
            Log.d(LOG_TAG, "Creating SemImsService");
            SemImsServiceStub semImsServiceStub2 = new SemImsServiceStub(context);
            sInstance = semImsServiceStub2;
            semImsServiceStub2.init();
            Log.d(LOG_TAG, "Done.");
            SemImsServiceStub semImsServiceStub3 = sInstance;
            return semImsServiceStub3;
        }
    }

    private void init() {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mCoreThread.start();
    }

    public static SemImsServiceStub getInstance() {
        while (getInstanceInternal() == null) {
            Log.e(LOG_TAG, "instance is null...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getInstanceInternal();
    }

    private static synchronized SemImsServiceStub getInstanceInternal() {
        SemImsServiceStub semImsServiceStub;
        synchronized (SemImsServiceStub.class) {
            semImsServiceStub = sInstance;
        }
        return semImsServiceStub;
    }

    public Binder getBinder() {
        return ImsServiceStub.getInstance().getSemBinder();
    }

    public boolean isSimMobilityActivated(int phoneId) {
        return ImsServiceStub.getInstance().isSimMobilityActivated(phoneId);
    }

    public boolean isServiceAvailable(String service, int rat, int phoneId) throws RemoteException {
        return ImsServiceStub.getInstance().isServiceAvailable(service, rat, phoneId);
    }

    public String getRcsProfileType(int phoneId) throws RemoteException {
        return ImsServiceStub.getInstance().getRcsProfileType(phoneId);
    }

    public ContentValues getConfigValues(String[] fields, int phoneId) {
        return ImsServiceStub.getInstance().getConfigValues(fields, phoneId);
    }

    public boolean isForbiddenByPhoneId(int phoneId) {
        return ImsServiceStub.getInstance().isForbiddenByPhoneId(phoneId);
    }

    public SemImsRegistration[] getRegistrationInfoByPhoneId(int phoneId) {
        List<SemImsRegistration> regiList = new ArrayList<>();
        ImsRegistration[] reg = ImsServiceStub.getInstance().getRegistrationInfoByPhoneId(phoneId);
        if (reg != null) {
            for (ImsRegistration registration : reg) {
                if (registration.getPhoneId() == phoneId) {
                    regiList.add(buildSemImsRegistration(registration));
                }
            }
        }
        return (SemImsRegistration[]) regiList.toArray(new SemImsRegistration[0]);
    }

    public SemImsProfile[] getCurrentProfileForSlot(int phoneId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        List<SemImsProfile> proList = new ArrayList<>();
        ImsProfile[] profile = ImsServiceStub.getInstance().getCurrentProfileForSlot(phoneId);
        if (profile != null) {
            for (ImsProfile imsprofile : profile) {
                proList.add(buildSemImsProfile(imsprofile));
            }
        }
        return (SemImsProfile[]) proList.toArray(new SemImsProfile[0]);
    }

    public SemImsRegistration getRegistrationInfoByServiceType(String serviceType, int phoneId) throws RemoteException {
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getRegistrationInfoByServiceType: phoneId " + phoneId + " serviceType " + serviceType);
        return buildSemImsRegistration(ImsServiceStub.getInstance().getRegistrationInfoByServiceType(serviceType, phoneId));
    }

    public void registerImsRegistrationListenerForSlot(SemImsRegiListener listener, int phoneId) throws RemoteException {
        IMSLog.d(LOG_TAG, phoneId, "SemRegisterImsOngoingFtListener");
        if (listener != null) {
            if (!this.mRegCallbacks.containsKey(Integer.valueOf(phoneId)) || !this.mRegCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                ImsRegistrationCallBack regCallback = new ImsRegistrationCallBack(listener);
                if (!this.mRegCallbacks.containsKey(Integer.valueOf(phoneId))) {
                    this.mRegCallbacks.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
                }
                this.mRegCallbacks.get(Integer.valueOf(phoneId)).put(listener, regCallback);
                ImsServiceStub.getInstance().registerImsRegistrationListener(regCallback, false, phoneId);
                if (!this.mRegListeners.containsKey(Integer.valueOf(phoneId))) {
                    this.mRegListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
                }
                this.mRegListeners.get(Integer.valueOf(phoneId)).register(listener);
                ImsRegistration[] registrations = ImsServiceStub.getInstance().getRegistrationInfoByPhoneId(phoneId);
                if (registrations != null) {
                    for (ImsRegistration registration : registrations) {
                        if (registration.hasVolteService() && !registration.getImsProfile().hasEmergencySupport()) {
                            try {
                                listener.onRegistered(buildSemImsRegistration(registration));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return;
                }
                return;
            }
            IMSLog.d(LOG_TAG, phoneId, "registerImsRegistrationListenerForSlot : listener has already registered");
        }
    }

    public void unregisterImsRegistrationListenerForSlot(SemImsRegiListener listener, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "SemUnregisterImsRegistrationListenerForSlot");
        if (listener == null) {
            String str = LOG_TAG;
            IMSLog.d(str, phoneId, "unregisterImsRegistrationListenerForSlot: listener " + listener);
            return;
        }
        if (this.mRegCallbacks.containsKey(Integer.valueOf(phoneId)) && this.mRegCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
            ImsServiceStub.getInstance().unregisterImsRegistrationListenerForSlot((ImsRegistrationCallBack) this.mRegCallbacks.get(Integer.valueOf(phoneId)).get(listener), phoneId);
            this.mRegCallbacks.get(Integer.valueOf(phoneId)).remove(listener);
        }
        if (this.mRegListeners.containsKey(Integer.valueOf(phoneId))) {
            this.mRegListeners.get(Integer.valueOf(phoneId)).unregister(listener);
        }
    }

    public void registerImsOngoingFtEventListener(SemImsFtListener listener) throws RemoteException {
        IMSLog.d(LOG_TAG, "SemRegisterImsOngoingFtListener");
        this.mOngoingFtEventCallback = new ImsOngoingFtEventCallBack();
        ImsServiceStub.getInstance().registerImsOngoingFtListener(this.mOngoingFtEventCallback);
        if (listener != null) {
            IMSLog.d(LOG_TAG, "mOngingFtEventListers register");
            this.mOngoingFtEventListeners.register(listener);
        }
    }

    public void unregisterImsOngoingFtEventListener(SemImsFtListener listener) {
        IMSLog.d(LOG_TAG, "SemUnregisterImsOngoingFtListener");
        if (listener != null) {
            this.mOngoingFtEventListeners.unregister(listener);
        }
        if (this.mOngoingFtEventCallback != null && this.mOngoingFtEventListeners.getRegisteredCallbackCount() == 0) {
            ImsServiceStub.getInstance().unregisterImsOngoingFtListener(this.mOngoingFtEventCallback);
        }
    }

    public void registerSimMobilityStatusListener(SemSimMobStatusListener listener, int phoneId) throws RemoteException {
        IMSLog.d(LOG_TAG, phoneId, "SemRegisterSimMobilityStatusListener");
        if (listener != null) {
            if (!this.mSimMobilityStatusCallbacks.containsKey(Integer.valueOf(phoneId)) || !this.mSimMobilityStatusCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                SimMobilityStatusCallBack simMobilityStatusCallBack = new SimMobilityStatusCallBack(listener);
                if (!this.mSimMobilityStatusCallbacks.containsKey(Integer.valueOf(phoneId))) {
                    this.mSimMobilityStatusCallbacks.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
                }
                this.mSimMobilityStatusCallbacks.get(Integer.valueOf(phoneId)).put(listener, simMobilityStatusCallBack);
                ImsServiceStub.getInstance().registerSimMobilityStatusListenerByPhoneId(simMobilityStatusCallBack, phoneId);
                if (!this.mSimMobilityStatusListeners.containsKey(Integer.valueOf(phoneId))) {
                    this.mSimMobilityStatusListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
                }
                this.mSimMobilityStatusListeners.get(Integer.valueOf(phoneId)).register(listener);
                return;
            }
            IMSLog.d(LOG_TAG, phoneId, "registerSimMobilityStatusListener : listener has already registered");
        }
    }

    public void unregisterSimMobilityStatusListener(SemSimMobStatusListener listener, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "SemUnregisterSimMobilityStatusListener");
        if (listener != null) {
            if (this.mSimMobilityStatusCallbacks.containsKey(Integer.valueOf(phoneId)) && this.mSimMobilityStatusCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                ImsServiceStub.getInstance().unregisterSimMobilityStatusListenerByPhoneId((SimMobilityStatusCallBack) this.mSimMobilityStatusCallbacks.get(Integer.valueOf(phoneId)).get(listener), phoneId);
                this.mSimMobilityStatusCallbacks.get(Integer.valueOf(phoneId)).remove(listener);
            }
            if (this.mSimMobilityStatusListeners.containsKey(Integer.valueOf(phoneId))) {
                this.mSimMobilityStatusListeners.get(Integer.valueOf(phoneId)).unregister(listener);
            }
        }
    }

    public void registerAutoConfigurationListener(SemAutoConfigListener listener, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "registerAutoConfigurationListener");
        if (listener != null) {
            if (!this.mAutoConfigCallbacks.containsKey(Integer.valueOf(phoneId)) || !this.mAutoConfigCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                AutoConfigCallBack autoConfigCallback = new AutoConfigCallBack(listener);
                if (!this.mAutoConfigCallbacks.containsKey(Integer.valueOf(phoneId))) {
                    this.mAutoConfigCallbacks.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
                }
                this.mAutoConfigCallbacks.get(Integer.valueOf(phoneId)).put(listener, autoConfigCallback);
                ImsServiceStub.getInstance().registerAutoConfigurationListener(autoConfigCallback, phoneId);
                if (!this.mAutoCofigListeners.containsKey(Integer.valueOf(phoneId))) {
                    this.mAutoCofigListeners.put(Integer.valueOf(phoneId), new RemoteCallbackList());
                }
                this.mAutoCofigListeners.get(Integer.valueOf(phoneId)).register(listener);
                return;
            }
            IMSLog.d(LOG_TAG, phoneId, "registerAutoConfigurationListener : listener has already registered");
        }
    }

    public void unregisterAutoConfigurationListener(SemAutoConfigListener listener, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "unregisterAutoConfigurationListener");
        if (listener != null) {
            if (this.mAutoConfigCallbacks.containsKey(Integer.valueOf(phoneId)) && this.mAutoConfigCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                ImsServiceStub.getInstance().unregisterAutoConfigurationListener((AutoConfigCallBack) this.mAutoConfigCallbacks.get(Integer.valueOf(phoneId)).get(listener), phoneId);
                this.mAutoConfigCallbacks.get(Integer.valueOf(phoneId)).remove(listener);
            }
            if (this.mAutoCofigListeners.containsKey(Integer.valueOf(phoneId))) {
                this.mAutoCofigListeners.get(Integer.valueOf(phoneId)).unregister(listener);
            }
        }
    }

    public boolean isRcsEnabled(boolean needAutoConfigCheck, int phoneId) {
        int version = 0;
        boolean rcsEnabled = false;
        boolean autoconfCompleted = false;
        try {
            rcsEnabled = Settings.System.getInt(this.mContext.getContentResolver(), "rcs_user_setting") == 1;
        } catch (Settings.SettingNotFoundException e) {
            Log.d(LOG_TAG + "[" + phoneId + "]", "isRcsEnabled: rcs_user_setting is not exist.");
        }
        if (!needAutoConfigCheck) {
            return rcsEnabled;
        }
        try {
            if (getRcsAutoconfigVers(phoneId)) {
                version = this.mRcsConfigVers;
            }
            if (getRcsAutoConfigCompl(phoneId) != null) {
                autoconfCompleted = CloudMessageProviderContract.JsonData.TRUE.equals(getRcsAutoConfigCompl(phoneId));
            }
            Log.d(LOG_TAG + "[" + phoneId + "]", "isRcsEnabled: version " + version + " autoConfigComplete " + autoconfCompleted);
        } catch (IllegalStateException e2) {
            Log.d(LOG_TAG + "[" + phoneId + "]", "isRcsEnabled: AutoConfiguration is not completed.");
        }
        if (!rcsEnabled) {
            return false;
        }
        if (!autoconfCompleted || version > 0) {
            return true;
        }
        return false;
    }

    private String getRcsAutoConfigCompl(int phoneId) {
        Uri versUri = AUTO_CONFIGURATION_VERS_URI;
        String autoConfigCompl = null;
        Uri.Builder buildUpon = Uri.parse(versUri + ConfigConstants.PATH.INFO_COMPLETED.replaceAll("#simslot\\d", "")).buildUpon();
        Uri uri = buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + Integer.toString(phoneId)).build();
        Cursor c = null;
        try {
            if (this.mContext != null) {
                c = this.mContext.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            }
            if (c != null && c.moveToFirst()) {
                autoConfigCompl = c.getString(0);
            }
            return autoConfigCompl;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private boolean getRcsAutoconfigVers(int phoneId) {
        String configurationValue = null;
        Uri versUri = AUTO_CONFIGURATION_VERS_URI;
        Uri.Builder buildUpon = Uri.parse(versUri + "parameter/version".replaceAll("#simslot\\d", "")).buildUpon();
        Uri uri = buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + Integer.toString(phoneId)).build();
        Cursor c = null;
        try {
            if (this.mContext != null) {
                c = this.mContext.getContentResolver().query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            }
            if (c != null && c.moveToFirst()) {
                configurationValue = c.getString(0);
            }
            if (configurationValue != null) {
                this.mRcsConfigVers = Integer.parseInt(configurationValue);
                if (c != null) {
                    c.close();
                }
                return true;
            }
            if (c != null) {
                c.close();
            }
            return false;
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Error while parsing integer in getIntValue() - NumberFormatException");
            if (c != null) {
                c.close();
            }
            return false;
        } catch (Throwable th) {
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    public void enableRcsByPhoneId(boolean enable, int phoneId) {
        ImsServiceStub.getInstance().enableRcsByPhoneId(enable, phoneId);
    }

    public boolean isVoLteAvailable(int phoneId) throws RemoteException {
        return ImsServiceStub.getInstance().hasVoLteSimByPhoneId(phoneId);
    }

    public void sendTryRegisterByPhoneId(int phoneId) {
        ImsServiceStub.getInstance().sendTryRegisterByPhoneId(phoneId);
    }

    public boolean getBooleanConfig(String service, int phoneId) {
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "getBooleanConfig : " + service);
        String[] dmFields = new String[1];
        if ("mmtel".equals(service)) {
            if (SimUtil.getSimMno(phoneId) == Mno.USCC) {
                dmFields[0] = "81";
            } else {
                dmFields[0] = "93";
            }
        } else if (!"mmtel-video".equals(service)) {
            return false;
        } else {
            dmFields[0] = "94";
        }
        ContentValues dmValue = ImsServiceStub.getInstance().getConfigValues(dmFields, phoneId);
        if (dmValue != null) {
            String result = (String) dmValue.get(dmFields[0]);
            if (!TextUtils.isEmpty(result)) {
                if ("81".equals(dmFields[0])) {
                    return DiagnosisConstants.RCSM_ORST_REGI.equals(result);
                }
                return "1".equals(result);
            }
        } else {
            IMSLog.d(LOG_TAG, phoneId, "can not read DM values");
        }
        return false;
    }

    public void setRttMode(int phoneId, int mode) {
        ImsServiceStub.getInstance().setRttMode(phoneId, mode);
    }

    public void sendVerificationCode(String value, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "sendVerificationCode");
        ImsServiceStub.getInstance().sendVerificationCode(value, phoneId);
    }

    public void sendMsisdnNumber(String value, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "sendMsisdnNumber");
        ImsServiceStub.getInstance().sendMsisdnNumber(value, phoneId);
    }

    public void registerDmValueListener(SemImsDmConfigListener listener) throws RemoteException {
        ImsDmConfigCallBack dmConfigCallback = new ImsDmConfigCallBack();
        this.mDmConfigCallbacks = dmConfigCallback;
        ImsServiceStub.getInstance().registerDmValueListener(dmConfigCallback);
        if (listener != null) {
            Log.d(LOG_TAG, "mDmConfigListeners register");
            this.mDmConfigListeners.register(listener);
        }
    }

    public void unregisterDmValueListener(SemImsDmConfigListener listener) {
        if (listener != null) {
            this.mDmConfigListeners.unregister(listener);
        }
        if (this.mDmConfigCallbacks != null) {
            ImsServiceStub.getInstance().unregisterDmValueListener(this.mDmConfigCallbacks);
        }
    }

    public boolean supportEmergencyCallOnCmc(int phoneId) {
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "supportEmergencyCallOnCmc phoneId : " + phoneId);
        return false;
    }

    public void sendSemCmcRecordingEvent(SemCmcRecordingInfo info, int event, int phoneId) {
        String str = LOG_TAG;
        IMSLog.d(str, phoneId, "sendSemCmcRecordingEvent : " + event);
        ImsServiceStub.getInstance().sendCmcRecordingEvent(phoneId, event, info);
    }

    public void registerSemCmcRecordingListener(ISemCmcRecordingListener listener, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "registerSemCmcRecordingListener");
        ImsServiceStub.getInstance().registerCmcRecordingListener(phoneId, listener);
    }

    public void registerEpdgListener(ISemEpdgListener listener) {
        if (this.mEpdgHandoverCallback == null) {
            this.mEpdgHandoverCallback = new EpdgListenerCallback();
            ImsServiceStub.getInstance().registerEpdgListener(this.mEpdgHandoverCallback);
        }
        this.mEpdgListeners.register(listener);
        boolean isWifiConnected = ImsServiceStub.getInstance().getPdnController().isWifiConnected();
        for (int phoneId = 0; phoneId < SimUtil.getPhoneCount(); phoneId++) {
            try {
                String str = LOG_TAG;
                Log.d(str, "register epdg listnern onepdg available : " + this.mEpdgAvailable[phoneId] + " wifi connected " + isWifiConnected);
                listener.onEpdgAvailable(phoneId, this.mEpdgAvailable[phoneId], isWifiConnected ? 1 : 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unRegisterEpdgListener(ISemEpdgListener listener) {
        if (this.mEpdgHandoverCallback != null) {
            this.mEpdgListeners.unregister(listener);
            if (this.mEpdgListeners.getRegisteredCallbackCount() <= 0) {
                ImsServiceStub.getInstance().unRegisterEpdgListener(this.mEpdgHandoverCallback);
                this.mEpdgHandoverCallback = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public SemImsRegistration buildSemImsRegistration(ImsRegistration reg) {
        if (reg == null) {
            return null;
        }
        SemImsRegistration.Builder registrationBuilder = SemImsRegistration.getBuilder();
        registrationBuilder.setRegiRat(reg.getCurrentRat());
        registrationBuilder.setPdnType(reg.getNetworkType());
        registrationBuilder.setPhoneId(reg.getPhoneId());
        registrationBuilder.setServices(reg.getServices());
        registrationBuilder.setPAssociatedUri2nd(reg.getPAssociatedUri2nd());
        Optional.ofNullable(reg.getRegisteredImpu()).ifPresent(new Consumer(registrationBuilder) {
            public final /* synthetic */ SemImsRegistration.Builder f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                this.f$0.setRegisteredPublicUserId(((ImsUri) obj).toString());
            }
        });
        String ownNumber = (String) Optional.ofNullable(reg.getOwnNumber()).orElse("");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(reg.getPhoneId());
        if (sm != null) {
            String imsi = sm.getImsi();
            if (!TextUtils.isEmpty(imsi) && ownNumber.contains(imsi)) {
                ownNumber = "";
            }
        }
        registrationBuilder.setOwnNumber(ownNumber);
        return new SemImsRegistration(registrationBuilder);
    }

    private SemImsProfile buildSemImsProfile(ImsProfile profile) {
        if (profile != null) {
            return new SemImsProfile(profile.toJson());
        }
        return null;
    }

    /* access modifiers changed from: private */
    public SemImsRegistrationError buildSemImsRegistrationError(ImsRegistrationError registrationError) {
        return new SemImsRegistrationError(registrationError.getSipErrorCode(), registrationError.getSipErrorReason(), registrationError.getDetailedDeregiReason(), registrationError.getDeregistrationReason());
    }

    private class ImsDmConfigCallBack extends IImsDmConfigListener.Stub {
        private ImsDmConfigCallBack() {
        }

        public void onChangeDmValue(String uri, boolean state) {
            RemoteCallbackList<SemImsDmConfigListener> dmConfigListener = SemImsServiceStub.this.mDmConfigListeners;
            if (dmConfigListener != null) {
                int i = dmConfigListener.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        dmConfigListener.getBroadcastItem(i).onChangeDmValue(uri, state);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                dmConfigListener.finishBroadcast();
            }
        }
    }

    private class AutoConfigCallBack extends IAutoConfigurationListener.Stub {
        SemAutoConfigListener mListener;

        public AutoConfigCallBack(SemAutoConfigListener listener) {
            this.mListener = listener;
        }

        public void onVerificationCodeNeeded() {
            try {
                this.mListener.onVerificationCodeNeeded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onMsisdnNumberNeeded() {
            try {
                this.mListener.onMsisdnNumberNeeded();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onAutoConfigurationCompleted(boolean result) {
            try {
                this.mListener.onAutoConfigurationCompleted(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class ImsRegistrationCallBack extends IImsRegistrationListener.Stub {
        SemImsRegiListener mListener;

        public ImsRegistrationCallBack(SemImsRegiListener listener) {
            this.mListener = listener;
        }

        public void onRegistered(ImsRegistration reg) {
            if ((reg.hasVolteService() && !reg.getImsProfile().hasEmergencySupport()) || reg.hasRcsService()) {
                try {
                    this.mListener.onRegistered(SemImsServiceStub.this.buildSemImsRegistration(reg));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError registrationError) {
            if ((reg.hasVolteService() && !reg.getImsProfile().hasEmergencySupport()) || reg.hasRcsService()) {
                try {
                    this.mListener.onDeregistered(SemImsServiceStub.this.buildSemImsRegistration(reg), SemImsServiceStub.this.buildSemImsRegistrationError(registrationError));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ImsOngoingFtEventCallBack extends IImsOngoingFtEventListener.Stub {
        private ImsOngoingFtEventCallBack() {
        }

        public void onFtStateChanged(boolean event) {
            RemoteCallbackList<SemImsFtListener> ftListener = SemImsServiceStub.this.mOngoingFtEventListeners;
            if (ftListener != null) {
                int i = ftListener.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        ftListener.getBroadcastItem(i).onFtStateChanged(event);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                ftListener.finishBroadcast();
            }
        }
    }

    private class SimMobilityStatusCallBack extends ISimMobilityStatusListener.Stub {
        SemSimMobStatusListener mListener;

        public SimMobilityStatusCallBack(SemSimMobStatusListener listener) {
            this.mListener = listener;
        }

        public void onSimMobilityStateChanged(boolean event) {
            try {
                this.mListener.onSimMobilityStateChanged(event);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private class EpdgListenerCallback extends IEpdgListener.Stub {
        private EpdgListenerCallback() {
        }

        public void onEpdgAvailable(int phoneId, int isAvailable, int wifiState) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                int i = SemImsServiceStub.this.mEpdgListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    boolean availability = true;
                    if (isAvailable != 1) {
                        availability = false;
                    }
                    try {
                        SemImsServiceStub.this.mEpdgAvailable[phoneId] = availability;
                        SemImsServiceStub.this.mEpdgListeners.getBroadcastItem(i).onEpdgAvailable(phoneId, availability, wifiState);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                SemImsServiceStub.this.mEpdgListeners.finishBroadcast();
            }
        }

        public void onEpdgHandoverResult(int phoneId, int isL2WHandover, int result, String apnType) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                int i = SemImsServiceStub.this.mEpdgListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        SemImsServiceStub.this.mEpdgListeners.getBroadcastItem(i).onHandoverResult(phoneId, isL2WHandover, result, apnType);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                SemImsServiceStub.this.mEpdgListeners.finishBroadcast();
            }
        }

        public void onEpdgDeregister(int phoneId) {
        }

        public void onEpdgIpsecConnection(int phoneId, String apnType, int ikeError, int throttleCount) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                int i = SemImsServiceStub.this.mEpdgListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        SemImsServiceStub.this.mEpdgListeners.getBroadcastItem(i).onIpsecConnection(phoneId, apnType, ikeError, throttleCount);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                SemImsServiceStub.this.mEpdgListeners.finishBroadcast();
            }
        }

        public void onEpdgIpsecDisconnection(int phoneId, String apnType) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                int i = SemImsServiceStub.this.mEpdgListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        SemImsServiceStub.this.mEpdgListeners.getBroadcastItem(i).onIpsecDisconnection(phoneId, apnType);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                SemImsServiceStub.this.mEpdgListeners.finishBroadcast();
            }
        }

        public void onEpdgRegister(int phoneId, boolean cdmaAvailability) {
        }

        public void onEpdgShowPopup(int phoneId, int popupType) {
            if (SemImsServiceStub.this.mEpdgListeners != null) {
                int i = SemImsServiceStub.this.mEpdgListeners.beginBroadcast();
                while (i > 0) {
                    i--;
                    try {
                        SemImsServiceStub.this.mEpdgListeners.getBroadcastItem(i).onEpdgShowPopup(phoneId, popupType);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                SemImsServiceStub.this.mEpdgListeners.finishBroadcast();
            }
        }

        public void onEpdgReleaseCall(int phoneId) {
        }
    }
}
