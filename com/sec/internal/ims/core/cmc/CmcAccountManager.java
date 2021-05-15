package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ModuleChannel;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CmcAccountManager implements ICmcAccountManager {
    private static final String CMC_ACCOUNT_SP = "cmcaccount";
    public static double CMC_CALLFORKING_VERSION = 2.0d;
    public static final String CMC_DEVICE_TYPE_PROP = "ro.cmc.device_type";
    private static final String CMC_PROFILE_NAME = "SamsungCMC";
    public static final String CMC_SATOKEN_DEFAULT = "default_token";
    private static final String CMC_SATOKEN_SP = "accesstoken";
    private static final String CMC_SAURL_DEFAULT = "us-auth2.samsungosp.comus-aut";
    private static final String CMC_SAURL_SP = "saurl";
    private static final String CMC_SERVICE_PACKAGE_NAME = "com.samsung.android.mdecservice";
    public static final String CMC_VERSION_PROP = "ro.cmc.version";
    private static final int EVENT_CMC_DEVICE_CHANGED = 5;
    private static final int EVENT_CMC_NW_PREF_CHANGED = 6;
    private static final int EVENT_SA_REQUEST = 1;
    private static final int EVENT_SA_UPDATE = 2;
    private static final int EVENT_START_CMC_REGISTRATION = 3;
    private static final int EVENT_STOP_CMC_REGISTRATION = 4;
    private static final String LOG_TAG = "CmcAccountManager";
    private static final String PD_PROFILE_NAME = "SamsungCMC_PD";
    private static final String SD_PROFILE_NAME = "SamsungCMC_SD";
    public static final String URN_PREFIX = "urn:duid:";
    private static boolean mIsCmcServiceInstalled = true;
    private CmcInfo mCmcInfo;
    private CmcSettingManagerWrapper mCmcSetting;
    private Context mContext;
    private String mDomain = "";
    private SimpleEventLog mEventLog;
    private final InternalHandler mHandler;
    private String mImpi = "";
    private boolean mIsCmcProfileAdded = false;
    private String mPassword = "";
    private int mPhoneCount;
    private int mPort = ModuleChannel.EVT_MODULE_CHANNEL_BASE;
    private Map<Integer, ImsProfile> mProfileMap = new HashMap();
    private String mProfileUpdateReason = "";
    private ICmcAccountManager.ProfileUpdateResult mProfileUpdatedResult = ICmcAccountManager.ProfileUpdateResult.FAILED;
    private List<String> mRegiEventNotifyHostInfo = new ArrayList();
    private IRegistrationManager mRm;
    private CmcSAManager mSaService;
    private String mSaToken = "";
    private String mSaUrl = "";

    public CmcAccountManager(Context context, Looper looper) {
        Log.i(LOG_TAG, "CmcAccountManager create");
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 100);
        this.mPhoneCount = SimUtil.getPhoneCount();
        this.mCmcInfo = new CmcInfo();
        CmcSettingManagerWrapper cmcSettingManagerWrapper = new CmcSettingManagerWrapper(this.mContext, this);
        this.mCmcSetting = cmcSettingManagerWrapper;
        cmcSettingManagerWrapper.init();
        boolean isSecondaryDevice = isSecondaryDevice();
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            List<ImsProfile> list = ImsProfileLoader.getProfileListWithMnoName(this.mContext, "MDMN", phoneId);
            if (!list.isEmpty()) {
                Iterator<ImsProfile> it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImsProfile p = it.next();
                    if (!p.getName().isEmpty() && CMC_PROFILE_NAME.equalsIgnoreCase(p.getName())) {
                        Log.i(LOG_TAG, "CMC profile found slot: " + phoneId);
                        if (isSecondaryDevice) {
                            Set<String> svc = new HashSet<>();
                            svc.add("mmtel");
                            p.setServiceSet(6, svc);
                            p.setServiceSet(5, svc);
                            p.setServiceSet(12, svc);
                            p.setServiceSet(14, svc);
                            p.setNetworkEnabled(6, true);
                            p.setNetworkEnabled(5, true);
                            p.setNetworkEnabled(12, true);
                            p.setNetworkEnabled(14, true);
                        }
                        this.mProfileMap.put(Integer.valueOf(phoneId), p);
                    }
                }
            } else {
                Log.i(LOG_TAG, "No pre-defined profile slot: " + phoneId);
            }
        }
        this.mSaService = new CmcSAManager(this.mContext, this);
        this.mHandler = new InternalHandler(looper);
        mIsCmcServiceInstalled = isCmcServiceInstalled();
        initCmcFromPref();
    }

    public void setRegistrationManager(IRegistrationManager rm) {
        this.mRm = rm;
    }

    public void startCmcRegistration() {
        this.mHandler.sendEmptyMessage(3);
    }

    private void stopCmcRegistration() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void notifyCmcDeviceChanged() {
        if (!this.mHandler.hasMessages(5)) {
            this.mHandler.sendEmptyMessage(5);
        }
    }

    public void notifyCmcNwPrefChanged() {
        if (this.mHandler.hasMessages(6)) {
            this.mHandler.removeMessages(6);
        }
        this.mHandler.sendEmptyMessageDelayed(6, 600);
    }

    /* access modifiers changed from: protected */
    public void onCmcDeviceChanged() {
        IRegistrationManager regiMgr = ImsRegistry.getRegistrationManager();
        if (regiMgr == null) {
            IMSLog.e(LOG_TAG, "onCmcDeviceChanged: RegistrationManagerBase is null");
            return;
        }
        int id = 0;
        while (id < this.mPhoneCount) {
            IRegisterTask cmcTask = getCmcRegisterTask(id);
            if (cmcTask == null || cmcTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                id++;
            } else {
                IMSLog.i(LOG_TAG, id, "onCmcDeviceChanged: deregistering");
                return;
            }
        }
        int phoneId = (this.mCmcInfo.mLineSlotIndex == -1 || isSecondaryDevice()) ? SimUtil.getDefaultPhoneId() : this.mCmcInfo.mLineSlotIndex;
        IRegisterTask task = getCmcRegisterTask(phoneId);
        if (task != null) {
            this.mProfileUpdatedResult = updateCmcProfile();
            if (!isCmcActivated() || (isSecondaryDevice() && !isSdHasCallForkingService())) {
                IMSLog.i(LOG_TAG, phoneId, "onCmcDeviceChanged: stopCmcRegistration: Activation[" + isCmcActivated() + "]");
                stopCmcRegistration();
            } else if (this.mProfileUpdatedResult == ICmcAccountManager.ProfileUpdateResult.NOT_UPDATED) {
                IMSLog.i(LOG_TAG, phoneId, "onCmcDeviceChanged: Not updated");
            } else {
                this.mRm.releaseThrottleByCmc(task);
                int lineSlotId = phoneId;
                if ("pd".equalsIgnoreCase(this.mCmcInfo.mDeviceType) && this.mProfileUpdatedResult == ICmcAccountManager.ProfileUpdateResult.UPDATED) {
                    lineSlotId = this.mCmcInfo.mLineSlotIndex;
                }
                boolean localDeregi = true;
                if (task.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                    task.setReason("CMC profile updated");
                    task.setDeregiReason(29);
                    if (TelephonyManagerWrapper.getInstance(this.mContext).getCallState(phoneId) != 0) {
                        IMSLog.i(LOG_TAG, phoneId, "onCmcDeviceChanged: call state not idle");
                        task.setHasPendingDeregister(true);
                    } else {
                        this.mEventLog.logAndAdd("onCmcDeviceChanged: deregister slot[" + phoneId + "]");
                        if (lineSlotId == phoneId) {
                            localDeregi = false;
                        }
                        regiMgr.deregister(task, localDeregi, false, "CMC profile updated");
                    }
                } else {
                    if (task.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                        this.mEventLog.logAndAdd("onCmcDeviceChanged: stopPdn slot[" + phoneId + "]");
                        regiMgr.stopPdnConnectivity(task.getPdnType(), task);
                        task.setState(RegistrationConstants.RegisterTaskState.IDLE);
                    }
                }
                IVolteServiceModule vm = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
                if (vm != null) {
                    Log.i(LOG_TAG, "onCmcDeviceChanged: update lineId and deviceId for p2p");
                    vm.getCmcServiceHelper().setP2pServiceInfo("urn:duid:" + this.mCmcInfo.mDeviceId, this.mCmcInfo.mLineId);
                }
                regiMgr.requestTryRegsiter(lineSlotId, 500);
            }
        } else {
            IMSLog.i(LOG_TAG, phoneId, "onCmcDeviceChanged: startCmcRegistration");
            startCmcRegistration();
        }
    }

    public void onSimRefresh(int phoneId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onSimRefresh(" + phoneId + ")");
        if (this.mIsCmcProfileAdded) {
            registerProfile(phoneId);
        }
    }

    /* access modifiers changed from: private */
    public void onStartCmcRegistration() {
        if (!mIsCmcServiceInstalled) {
            this.mEventLog.logAndAdd("onStartCmcRegistration: Cmc not installed");
        } else if (this.mIsCmcProfileAdded) {
            IMSLog.i(LOG_TAG, "onStartCmcRegistration: Cmc Profile is already added");
        } else if (!isCmcActivated()) {
            this.mEventLog.logAndAdd("onStartCmcRegistration: Cmc not activated");
        } else if (!isCmcCallSupported()) {
            this.mEventLog.logAndAdd("onStartCmcRegistration: Cmc Call forking not supported");
        } else {
            IVolteServiceModule vm = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
            if (vm != null && !vm.isRunning()) {
                Log.i(LOG_TAG, "Start VoLteService");
                vm.start();
            }
            if (isSecondaryDevice() && !isSdHasCallForkingService()) {
                IMSLog.i(LOG_TAG, "onStartCmcRegistration: SD CMC Call forking disabled");
            } else if ((this.mSaUrl.isEmpty() || this.mSaToken.isEmpty()) && !this.mHandler.hasMessages(1)) {
                this.mEventLog.logAndAdd("onStartCmcRegistration: request SA");
                InternalHandler internalHandler = this.mHandler;
                internalHandler.sendMessage(internalHandler.obtainMessage(1, true));
            } else {
                ICmcAccountManager.ProfileUpdateResult updateCmcProfile = updateCmcProfile();
                this.mProfileUpdatedResult = updateCmcProfile;
                if (updateCmcProfile != ICmcAccountManager.ProfileUpdateResult.FAILED) {
                    this.mEventLog.logAndAdd("onStartCmcRegistration: registerProfile");
                    for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                        registerProfile(phoneId);
                    }
                    this.mIsCmcProfileAdded = true;
                    if (vm != null) {
                        Log.i(LOG_TAG, "onStartCmcRegistration: update lineId and deviceId for p2p");
                        vm.getCmcServiceHelper().setP2pServiceInfo("urn:duid:" + this.mCmcInfo.mDeviceId, this.mCmcInfo.mLineId);
                        return;
                    }
                    return;
                }
                IMSLog.i(LOG_TAG, "onStartCmcRegistration: updateCmcProfile failed");
            }
        }
    }

    private void registerProfile(int phoneId) {
        if (isReadyRegisterP2p(phoneId)) {
            IMSLog.i(LOG_TAG, "registerProfile: ready to D2D register");
            ImsRegistry.getP2pCC().setDeviceIdInfo(this.mCmcInfo.mDeviceId, this.mCmcInfo.mLineOwnerDeviceId);
        } else if (getCmcRegisterTask(phoneId) != null) {
            IMSLog.i(LOG_TAG, "registerProfile: RegisterTask is already in the slot [" + phoneId + "]");
        } else {
            IMSLog.i(LOG_TAG, "registerProfile(" + phoneId + ")");
            ImsProfile profile = getProfile(phoneId);
            if (profile != null) {
                ImsRegistry.getRegistrationManager().registerProfile(profile, phoneId);
            }
        }
    }

    private boolean isReadyRegisterP2p(int phoneId) {
        ApplicationInfo targetInfo;
        Bundle bundle;
        try {
            PackageManager pm = this.mContext.getPackageManager();
            if (!(pm == null || (targetInfo = pm.getApplicationInfo("com.samsung.android.mdecservice", 128)) == null || (bundle = targetInfo.metaData) == null)) {
                return bundle.getBoolean("d2d_trial", false);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, e.toString());
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onStopCmcRegistration() {
        if (!this.mIsCmcProfileAdded) {
            IMSLog.i(LOG_TAG, "onStopCmcRegistration: no profile added");
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onStopCmcRegistration: deregisterProfile: activation[" + isCmcActivated() + "] isSD[" + isSecondaryDevice() + "] sdHasCallForking[" + isSdHasCallForkingService() + "]");
        for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
            ImsProfile profile = getProfile(phoneId);
            if (profile != null) {
                ImsRegistry.getRegistrationManager().deregisterProfile(profile.getId(), phoneId);
            }
        }
        this.mIsCmcProfileAdded = false;
    }

    public void setAccount(String newImpu) {
        for (ImsProfile profile : this.mProfileMap.values()) {
            profile.setImpi(this.mImpi);
            profile.setImpuList(newImpu);
            List<String> impuList = new ArrayList<>();
            impuList.add(newImpu);
            profile.setExtImpuList(impuList);
        }
    }

    public void setPcscfList(List<String> inputPcscfList) {
        if (this.mProfileMap.isEmpty()) {
            Log.e(LOG_TAG, "setPcscfList: mProfileMap is empty");
            return;
        }
        List<String> pcscfList = new ArrayList<>();
        StringBuilder strPcscfList = new StringBuilder();
        for (String inputPcscf : inputPcscfList) {
            if (inputPcscf.lastIndexOf(":") > 0) {
                this.mPort = Integer.valueOf(inputPcscf.substring(inputPcscf.lastIndexOf(":") + 1)).intValue();
                inputPcscf = inputPcscf.substring(0, inputPcscf.lastIndexOf(":"));
            } else {
                this.mPort = ModuleChannel.EVT_MODULE_CHANNEL_BASE;
            }
            strPcscfList.append("(pcscf = ");
            strPcscfList.append(inputPcscf);
            strPcscfList.append(" / mPort = ");
            strPcscfList.append(this.mPort);
            strPcscfList.append(")");
            pcscfList.add(inputPcscf);
        }
        Log.i(LOG_TAG, "pcscfList size[" + pcscfList.size() + "] : " + strPcscfList);
        for (ImsProfile profile : this.mProfileMap.values()) {
            profile.setPcscfList(pcscfList);
            profile.setSipPort(this.mPort);
        }
    }

    public void initProfile(String name, String newImpu, String duid, String accessToken, String priDeviceIdWithURN) {
        Log.i(LOG_TAG, "initProfile : build ImsProfile for MDMN");
        if (newImpu.contains("sip:")) {
            this.mImpi = newImpu.substring(newImpu.lastIndexOf(":") + 1);
        } else {
            this.mImpi = newImpu;
        }
        if (this.mImpi.indexOf("@") > 0) {
            String str = this.mImpi;
            this.mPassword = str.substring(0, str.indexOf("@"));
            String str2 = this.mImpi;
            this.mDomain = str2.substring(str2.lastIndexOf("@") + 1);
            Log.i(LOG_TAG, "password = " + this.mPassword + " / domain = " + this.mDomain);
        } else {
            this.mPassword = this.mImpi;
        }
        for (ImsProfile profile : this.mProfileMap.values()) {
            profile.setName(name);
            profile.setSipPort(this.mPort);
            profile.setPassword(this.mPassword);
            profile.setDomain(this.mDomain);
            profile.setVceConfigEnabled(true);
            profile.setDuid(duid);
            profile.setAccessToken(accessToken);
            profile.setPriDeviceIdWithURN(priDeviceIdWithURN);
            profile.setDisplayName(this.mCmcInfo.mDeviceId);
        }
        setAccount(newImpu);
    }

    public ImsProfile getProfile(int phoneId) {
        ImsProfile profile = this.mProfileMap.get(Integer.valueOf(phoneId));
        if (profile == null) {
            Log.e(LOG_TAG, "mProfile is null");
            return null;
        }
        Log.i(LOG_TAG, "mProfile = " + profile);
        return profile;
    }

    public String getCmcSaServerUrl() {
        return this.mCmcInfo.mSaServerUrl;
    }

    public String getCmcRelayType() {
        return getCmcRelayType(this.mCmcInfo.mIsSameWifiNetworkOnly);
    }

    private String getCmcRelayType(boolean isSameWifiNetworkOnly) {
        if (isSameWifiNetworkOnly) {
            return "priv-p2p";
        }
        return "";
    }

    private ICmcAccountManager.ProfileUpdateResult updateCmcProfile() {
        CmcInfo oldInfo = this.mCmcInfo;
        CmcInfo cmcInfo = getCmcInfo();
        this.mCmcInfo = cmcInfo;
        if (!isCmcInfoValid(cmcInfo)) {
            this.mEventLog.logAndAdd("updateCmcProfile: Invalid CmcInfo: " + this.mProfileUpdateReason);
            return ICmcAccountManager.ProfileUpdateResult.FAILED;
        } else if (isCmcInfoEqual(oldInfo, this.mCmcInfo)) {
            Log.i(LOG_TAG, "updateCmcProfile: Same CmcInfo");
            return ICmcAccountManager.ProfileUpdateResult.NOT_UPDATED;
        } else {
            String deviceIdWithURN = "urn:duid:" + this.mCmcInfo.mDeviceId;
            String priDeviceIdWithURN = "urn:duid:" + this.mCmcInfo.mLineOwnerDeviceId;
            String name = "pd".equalsIgnoreCase(this.mCmcInfo.mDeviceType) ? PD_PROFILE_NAME : SD_PROFILE_NAME;
            if (this.mCmcInfo.mPcscfAddrList != null && this.mCmcInfo.mPcscfAddrList.size() > 0) {
                setPcscfList(this.mCmcInfo.mPcscfAddrList);
            }
            initProfile(name, this.mCmcInfo.mLineImpu, deviceIdWithURN, this.mCmcInfo.mAccessToken, priDeviceIdWithURN);
            this.mEventLog.logAndAdd("updateCmcProfile: Update CmcInfo: Line[" + this.mCmcInfo.mLineSlotIndex + "] " + this.mProfileUpdateReason);
            return ICmcAccountManager.ProfileUpdateResult.UPDATED;
        }
    }

    public void updateCmcSaInfo(String token, String serverUrl) {
        this.mSaToken = token;
        this.mSaUrl = serverUrl;
        IMSLog.s(LOG_TAG, "updateCmcSaInfo: Url: " + this.mSaUrl + " token: " + this.mSaToken);
        this.mHandler.sendEmptyMessage(2);
    }

    private boolean isCmcCallSupported() {
        String version = this.mCmcSetting.getServiceVersion();
        if (!TextUtils.isEmpty(version) && Double.parseDouble(version) >= CMC_CALLFORKING_VERSION) {
            return true;
        }
        String versionProp = SemSystemProperties.get(CMC_VERSION_PROP, "");
        return (TextUtils.isEmpty(versionProp) || Double.parseDouble(versionProp) >= CMC_CALLFORKING_VERSION) ? true : true;
    }

    public boolean isCmcProfileAdded() {
        return this.mIsCmcProfileAdded;
    }

    private boolean isCmcInfoValid(CmcInfo cmcInfo) {
        String failReason = "";
        if (cmcInfo != null) {
            Iterator<String> it = CmcInfo.getInfoNameSet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String infoName = it.next();
                if (!cmcInfo.checkValidWithName(infoName)) {
                    failReason = infoName + " empty";
                    break;
                }
            }
        } else {
            failReason = "OwnDeviceInfo null";
        }
        if (!failReason.isEmpty()) {
            Log.i(LOG_TAG, "isCmcInfoValid: fail - " + failReason);
            this.mProfileUpdateReason = failReason;
            return false;
        }
        IMSLog.s(LOG_TAG, "isCmcInfoValid: true " + cmcInfo.toString());
        return true;
    }

    private boolean isCmcInfoEqual(CmcInfo src, CmcInfo target) {
        if (src == null) {
            return false;
        }
        String updateReason = "";
        Iterator<String> it = CmcInfo.getInfoNameSet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String infoName = it.next();
            if (!src.compareWithName(infoName, target)) {
                updateReason = infoName + " update";
                if (CmcInfo.isDumpPrintAvailable(infoName)) {
                    updateReason = updateReason + "[" + target.getValueWithName(infoName) + "]";
                }
            }
        }
        if (updateReason.isEmpty()) {
            return true;
        }
        if (this.mProfileUpdatedResult == ICmcAccountManager.ProfileUpdateResult.FAILED) {
            updateReason = "New valid CmcInfo ";
        }
        Log.i(LOG_TAG, "isCmcInfoEqual: false - " + updateReason);
        this.mProfileUpdateReason = updateReason;
        return false;
    }

    private CmcInfo getCmcInfo() {
        Log.i(LOG_TAG, "getCmcInfo");
        CmcInfo newCmcInfo = new CmcInfo();
        newCmcInfo.mOobe = this.mCmcSetting.getCmcSupported();
        newCmcInfo.mActivation = this.mCmcSetting.getOwnCmcActivation();
        newCmcInfo.mDeviceType = this.mCmcSetting.getDeviceType();
        newCmcInfo.mDeviceId = this.mCmcSetting.getDeviceId();
        newCmcInfo.mAccessToken = getAccessTokenFromCmcPref();
        newCmcInfo.mLineId = getLineId();
        newCmcInfo.mLineOwnerDeviceId = getPrimaryDeviceId();
        newCmcInfo.mLineImpu = getImpuFromLineId();
        newCmcInfo.mPcscfAddrList = getPcscfAddressList();
        newCmcInfo.mSaServerUrl = this.mSaUrl;
        newCmcInfo.mLineSlotIndex = getActiveSimSlot();
        newCmcInfo.mHasSd = hasSecondaryDevice();
        newCmcInfo.mNetworkPref = this.mCmcSetting.getPreferedNetwork();
        newCmcInfo.mCallforkingEnabled = isCallAllowedSdByPd(newCmcInfo.mDeviceId);
        newCmcInfo.mIsSameWifiNetworkOnly = this.mCmcSetting.isSameWifiNetworkOnly();
        return newCmcInfo;
    }

    private String getLineId() {
        String lineId = this.mCmcSetting.getLineId();
        IMSLog.s(LOG_TAG, "getLineId: " + lineId);
        return lineId;
    }

    private List<String> getPcscfAddressList() {
        List<String> pcscfList = this.mCmcSetting.getPcscfAddressList();
        IMSLog.s(LOG_TAG, "getPcscfAddressList: " + pcscfList);
        return pcscfList;
    }

    public boolean isCmcActivated() {
        return this.mCmcSetting.getOwnCmcActivation();
    }

    private boolean isSdHasCallForkingService() {
        String deviceId = this.mCmcSetting.getDeviceId();
        if (deviceId == null) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: deviceId is null");
            return false;
        } else if (!isCallAllowedSdByPd(deviceId)) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: isCallAllowedSdByPd false");
            return false;
        } else if (!getCmcCallActivation(deviceId)) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: Device CmcActivation false");
            return false;
        } else if (getCmcCallActivation(getPrimaryDeviceId())) {
            return true;
        } else {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: PD CmcActivation false");
            return false;
        }
    }

    private boolean isCallAllowedSdByPd(String deviceId) {
        return this.mCmcSetting.isCallAllowedSdByPd(deviceId);
    }

    public boolean getCmcCallActivation(String deviceId) {
        return this.mCmcSetting.getCmcCallActivation(deviceId);
    }

    private String getImpuFromLineId() {
        String impu = this.mCmcSetting.getLineImpu();
        IMSLog.s(LOG_TAG, "getImpuFromLineId: " + impu);
        return impu;
    }

    private String getPrimaryDeviceId() {
        String PrimaryDeviceId = "";
        List<String> deviceList = this.mCmcSetting.getDeviceIdList();
        if (deviceList != null && !deviceList.isEmpty()) {
            Iterator<String> it = deviceList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String dId = it.next();
                String Type = this.mCmcSetting.getDeviceTypeWithDeviceId(dId);
                if (!TextUtils.isEmpty(Type) && "pd".equalsIgnoreCase(Type)) {
                    PrimaryDeviceId = dId;
                    break;
                }
            }
        }
        IMSLog.s(LOG_TAG, "getPrimaryDeviceId: " + PrimaryDeviceId);
        return PrimaryDeviceId;
    }

    private int getActiveSimSlot() {
        return this.mCmcSetting.getActiveSimSlot();
    }

    public int getCurrentLineSlotIndex() {
        return this.mCmcInfo.mLineSlotIndex;
    }

    public String getCurrentLineOwnerDeviceId() {
        return this.mCmcInfo.mLineOwnerDeviceId;
    }

    public boolean hasSecondaryDevice() {
        String type = this.mCmcSetting.getDeviceType();
        List<String> deviceList = this.mCmcSetting.getDeviceIdList();
        if (!"pd".equalsIgnoreCase(type) || deviceList == null || deviceList.size() > 1) {
            return true;
        }
        Log.i(LOG_TAG, "hasSecondaryDevice : no SD with current PD");
        return false;
    }

    public boolean isWifiOnly() {
        return this.mCmcInfo.mNetworkPref == 1;
    }

    public void startSAService(boolean isLocal) {
        if (!this.mHandler.hasMessages(1)) {
            this.mEventLog.logAndAdd("startSAService: request SA");
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(1, Boolean.valueOf(isLocal)));
        }
    }

    /* access modifiers changed from: private */
    public void startSAServiceInternal(boolean isLocal) {
        this.mSaService.connectToSamsungAccountService(isLocal);
    }

    public void onChangedSamsungAccountInfo(String accessToken) {
        if (isCmcProfileAdded() && !TextUtils.isEmpty(accessToken) && !accessToken.equals(getAccessTokenFromCmcPref())) {
            IMSLog.i(LOG_TAG, "onChangedSamsungAccountInfo: startSAService");
            startSAService(true);
        }
    }

    public boolean isCmcEnabled() {
        String cmc_version = SemSystemProperties.get(CMC_VERSION_PROP, "");
        boolean isEnabled = false;
        if (!mIsCmcServiceInstalled) {
            IMSLog.i(LOG_TAG, "isCmcEnabled: Not installed ");
            return false;
        }
        if (cmc_version != null && !cmc_version.isEmpty()) {
            double version = Double.parseDouble(cmc_version);
            IMSLog.i(LOG_TAG, "isCmcEnabled: from cmc version: " + version);
            if (version >= 2.0d) {
                isEnabled = true;
            }
        }
        if (!isEnabled) {
            isEnabled = this.mCmcSetting.getOwnCmcActivation();
        }
        IMSLog.i(LOG_TAG, "isCmcEnabled: " + isEnabled);
        return isEnabled;
    }

    public boolean isSecondaryDevice() {
        CmcSettingManagerWrapper cmcSettingManagerWrapper = this.mCmcSetting;
        if (cmcSettingManagerWrapper == null) {
            IMSLog.e(LOG_TAG, "isSecondaryDevice : cmcsetting is null");
            return false;
        } else if ("sd".equalsIgnoreCase(cmcSettingManagerWrapper.getDeviceType())) {
            IMSLog.i(LOG_TAG, "isSecondaryDevice: by cmcsetting");
            return true;
        } else if (!"sd".equalsIgnoreCase(SemSystemProperties.get(CMC_DEVICE_TYPE_PROP, ""))) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, "isSecondaryDevice: by prop");
            return true;
        }
    }

    public boolean isCmcDeviceActivated() {
        return this.mCmcSetting.getOwnCmcActivation() && mIsCmcServiceInstalled;
    }

    public List<String> getRegiEventNotifyHostInfo() {
        return this.mRegiEventNotifyHostInfo;
    }

    public void setRegiEventNotifyHostInfo(List<String> regiEventNotifyHostInfo) {
        this.mRegiEventNotifyHostInfo = regiEventNotifyHostInfo;
    }

    /* access modifiers changed from: private */
    public void onSaUpdated() {
        IMSLog.i(LOG_TAG, "onSaUpdated: ");
        updateCmcPref();
        if (!this.mHandler.hasMessages(2)) {
            if (!this.mIsCmcProfileAdded) {
                startCmcRegistration();
                return;
            }
            IMSLog.i(LOG_TAG, "onSaUpdated: notifyCmcDeviceChanged with access token");
            notifyCmcDeviceChanged();
        }
    }

    private void initCmcFromPref() {
        SharedPreferences newSp = ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, CMC_ACCOUNT_SP, 0, false);
        this.mSaToken = newSp.getString(CMC_SATOKEN_SP, CMC_SATOKEN_DEFAULT);
        this.mSaUrl = newSp.getString(CMC_SAURL_SP, CMC_SAURL_DEFAULT);
        IMSLog.i(LOG_TAG, "initCmcFromPref: ");
    }

    private void updateCmcPref() {
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, CMC_ACCOUNT_SP, 0, false).edit();
        editor.putString(CMC_SATOKEN_SP, this.mSaToken);
        editor.putString(CMC_SAURL_SP, this.mSaUrl);
        editor.apply();
        IMSLog.s(LOG_TAG, "updateCmcPref: token: " + this.mSaToken + ", SaUrl: " + this.mSaUrl);
    }

    public String getAccessTokenFromCmcPref() {
        String accessToken = ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, CMC_ACCOUNT_SP, 0, false).getString(CMC_SATOKEN_SP, CMC_SATOKEN_DEFAULT);
        IMSLog.s(LOG_TAG, "getAccessTokenFromCmcPref: token: " + accessToken);
        return accessToken;
    }

    public IRegisterTask getCmcRegisterTask(int phoneId) {
        List<IRegisterTask> rtl = ImsRegistry.getRegistrationManager().getPendingRegistration(phoneId);
        if (rtl == null) {
            IMSLog.e(LOG_TAG, "getCmcRegisterTask: rtl is null");
            return null;
        }
        for (IRegisterTask task : rtl) {
            if (isCmcProfile(task.getProfile())) {
                return task;
            }
        }
        return null;
    }

    public ICmcAccountManager.ProfileUpdateResult getProfileUpdatedResult() {
        return this.mProfileUpdatedResult;
    }

    private boolean isCmcProfile(ImsProfile p) {
        if (p.getCmcType() != 0) {
            return true;
        }
        return false;
    }

    private boolean isCmcServiceInstalled() {
        try {
            this.mContext.getPackageManager().getApplicationInfo("com.samsung.android.mdecservice", 128);
            this.mEventLog.logAndAdd("isCmcServiceInstalled: true");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            this.mEventLog.logAndAdd("isCmcServiceInstalled: false");
            return false;
        }
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.i(CmcAccountManager.LOG_TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case 1:
                    CmcAccountManager.this.startSAServiceInternal(((Boolean) msg.obj).booleanValue());
                    return;
                case 2:
                    CmcAccountManager.this.onSaUpdated();
                    return;
                case 3:
                    CmcAccountManager.this.onStartCmcRegistration();
                    return;
                case 4:
                    CmcAccountManager.this.onStopCmcRegistration();
                    return;
                case 5:
                case 6:
                    CmcAccountManager.this.onCmcDeviceChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public void dump() {
        this.mEventLog.dump();
    }
}
