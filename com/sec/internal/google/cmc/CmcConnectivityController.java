package com.sec.internal.google.cmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.cmcnsd.CmcNsdManager;
import com.samsung.android.cmcnsd.network.NsdNetwork;
import com.samsung.android.cmcnsd.network.NsdNetworkCallback;
import com.samsung.android.cmcnsd.network.NsdNetworkCapabilities;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmcConnectivityController extends Handler implements ICmcConnectivityController {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final int EVENT_TRY_NSD_BIND = 1001;
    private static final String IMS_PCSCF_IP = "ims_pcscf_ip";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CmcConnectivityController.class.getSimpleName();
    private static final String NSD_FIRST_TRIIGER = "sys.ims.nsd.first_trigger";
    private static final String SERVER_DOMAIN = "p2p.samsungims.com";
    private static final String SIP_DOMAIN = "samsungims.com";
    public static final String URN_PREFIX = "urn:duid:";
    private static final String WD_HOST_PCSCF_IP = "192.168.49.1";
    private static final String WD_PDN_NAME = "p2p-wlan";
    private static final String WD_PROFILE_NAME_PD = "SamsungCMC_WIFI_P2P_PD";
    private static final String WD_PROFILE_NAME_SD = "SamsungCMC_WIFI_P2P_SD";
    private static final String WIFI_HS_PCSCF_PREF = "mobile_hotspot_pcscf";
    private static final String WIFI_HS_PDN_NAME = "swlan";
    private static final String WIFI_HS_PROFILE_NAME_PD = "SamsungCMC_WIFI_HS_PD";
    private static final String WIFI_PDN_NAME = "wlan";
    private static final String WIFI_PROFILE_NAME_PD = "SamsungCMC_WIFI_PD";
    private static final String WIFI_PROFILE_NAME_SD = "SamsungCMC_WIFI_SD";
    private CmcNsdManager.BindStatusListener mBindStatusListener = new CmcNsdManager.BindStatusListener() {
        public void onBound() {
            Log.e(CmcConnectivityController.LOG_TAG, "onBound()");
            CmcConnectivityController.this.mNsdManager.registerNetworkCallback(new NsdNetworkCapabilities.Builder().addTransport(0).addCapability(0).addCapability(1).build(), CmcConnectivityController.this.mNsdNetworkCallbackListener);
        }

        public void onUnbound() {
            Log.e(CmcConnectivityController.LOG_TAG, "onUnbound()");
            CmcConnectivityController.this.mNsdManager.unregisterNetworkCallback(CmcConnectivityController.this.mNsdNetworkCallbackListener);
            CmcConnectivityController.this.onShutDownNsd(true);
        }
    };
    private final Context mContext;
    private DeviceType mDeviceType = DeviceType.None;
    private String mFirstTrigger = "";
    private String mHotspotAuthToken = "";
    /* access modifiers changed from: private */
    public String mHotspotHostPcscfIp = "";
    private boolean mHotspotIsConnect = false;
    private String mHotspotLocalIp = "";
    private ImsProfile mHotspotProfile = null;
    /* access modifiers changed from: private */
    public boolean mHotspotRegistered = false;
    /* access modifiers changed from: private */
    public IpServiceManager mIpServiceManager = null;
    private final boolean mIsEnableWifiDirectFeature = false;
    /* access modifiers changed from: private */
    public boolean mIsNeedSubSession = false;
    /* access modifiers changed from: private */
    public final CmcNsdManager mNsdManager;
    /* access modifiers changed from: private */
    public NsdNetworkCallback mNsdNetworkCallbackListener = new NsdNetworkCallback() {
        public void onAvailable(NsdNetworkCapabilities capabilities) {
            String access$000 = CmcConnectivityController.LOG_TAG;
            Log.i(access$000, "onAvailable: cap=" + capabilities);
        }

        public void onConnected(NsdNetwork network) {
            String access$000 = CmcConnectivityController.LOG_TAG;
            Log.i(access$000, "onConnected: net=" + network);
            if (network.hasTransport(0)) {
                CmcConnectivityController.this.onWifiConnectionChanged(network.isConnected(), network.getHostAddress(), network.getAuthenticationToken(), network.getInterfaceNameList());
            } else if (network.hasTransport(1)) {
                CmcConnectivityController.this.onWifiDirectConnectionChanged(network.isConnected(), network.getAuthenticationToken());
            }
        }

        public void onDisconnected(NsdNetwork network) {
            String access$000 = CmcConnectivityController.LOG_TAG;
            Log.i(access$000, "onDisconnected: net=" + network);
            if (network.hasTransport(0)) {
                CmcConnectivityController.this.onWifiConnectionChanged(network.isConnected(), network.getHostAddress(), network.getAuthenticationToken(), network.getInterfaceNameList());
            } else if (network.hasTransport(1)) {
                CmcConnectivityController.this.onWifiDirectConnectionChanged(network.isConnected(), network.getAuthenticationToken());
            }
        }
    };
    private String mOwnDeviceId = "";
    private String mOwnDuid = "";
    /* access modifiers changed from: private */
    public int mPhoneCount = 0;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private String mPrimaryDuid = "";
    private final IImsRegistrationListener mRegisterP2pListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) {
            int cmcType = reg.getImsProfile().getCmcType();
            String access$000 = CmcConnectivityController.LOG_TAG;
            Log.i(access$000, "onRegistered, cmcType: " + cmcType);
            if (cmcType == 2) {
                String access$0002 = CmcConnectivityController.LOG_TAG;
                IMSLog.i(access$0002, "cmc is registered, mWifiRegistered: " + CmcConnectivityController.this.mWifiRegistered);
                if (CmcConnectivityController.this.mWifiRegistered && CmcConnectivityController.this.mWifiProfile != null) {
                    for (int phoneId = 0; phoneId < CmcConnectivityController.this.mPhoneCount; phoneId++) {
                        CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), phoneId, true);
                    }
                }
            } else if (cmcType == 3) {
                boolean unused = CmcConnectivityController.this.mWifiRegistered = true;
            } else if (cmcType == 4) {
                boolean unused2 = CmcConnectivityController.this.mWifiRegistered = true;
                if (CmcConnectivityController.this.mRm.isCmcRegistered(CmcConnectivityController.this.mPhoneId) > 0) {
                    IMSLog.i(CmcConnectivityController.LOG_TAG, "There is already cmc registration. deregister");
                    for (int phoneId2 = 0; phoneId2 < CmcConnectivityController.this.mPhoneCount; phoneId2++) {
                        CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), phoneId2, true);
                    }
                }
                boolean access$1100 = CmcConnectivityController.this.mWifiDirectRegistered;
            } else if (cmcType == 5) {
                boolean unused3 = CmcConnectivityController.this.mHotspotRegistered = true;
                String prevHotspotHostPcscfIp = CmcConnectivityController.this.getHSPref();
                if (!TextUtils.isEmpty(prevHotspotHostPcscfIp)) {
                    CmcConnectivityController.this.mIpServiceManager.ipRuleRemove("local_network", prevHotspotHostPcscfIp);
                }
                CmcConnectivityController.this.mIpServiceManager.ipRuleAdd("local_network", CmcConnectivityController.this.mHotspotHostPcscfIp);
                CmcConnectivityController cmcConnectivityController = CmcConnectivityController.this;
                cmcConnectivityController.setHSPref(cmcConnectivityController.mHotspotHostPcscfIp);
            } else if (cmcType == 7 || cmcType == 8) {
                boolean unused4 = CmcConnectivityController.this.mWifiDirectRegistered = true;
                if (cmcType == 7 && CmcConnectivityController.this.mIsNeedSubSession) {
                    CmcConnectivityController.this.mIpServiceManager.ipRuleAdd("local_network", CmcConnectivityController.WD_HOST_PCSCF_IP);
                }
            }
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errInfo) {
            int cmcType = reg.getImsProfile().getCmcType();
            String access$000 = CmcConnectivityController.LOG_TAG;
            Log.i(access$000, "onDeregistered(), cmcType: " + cmcType + ", SipErrorCode: " + errInfo.getSipErrorCode());
            String access$0002 = CmcConnectivityController.LOG_TAG;
            Log.i(access$0002, "mWifiDirectIsConnect: " + CmcConnectivityController.this.mWifiDirectIsConnect + ", mWifiIsConnect: " + CmcConnectivityController.this.mWifiIsConnect);
            if (cmcType == 2) {
                CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_SD);
            } else if (cmcType == 3) {
                boolean unused = CmcConnectivityController.this.mWifiRegistered = false;
                CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_PD);
            } else if (cmcType == 4) {
                boolean unused2 = CmcConnectivityController.this.mWifiRegistered = false;
                if (CmcConnectivityController.this.mWifiProfile != null) {
                    for (int phoneId = 0; phoneId < CmcConnectivityController.this.mPhoneCount; phoneId++) {
                        CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), phoneId, false);
                    }
                }
            } else if (cmcType == 7) {
                int unused3 = CmcConnectivityController.this.mServiceId = 0;
                int unused4 = CmcConnectivityController.this.mReservedId = -1;
                boolean unused5 = CmcConnectivityController.this.mIsNeedSubSession = false;
            } else if (cmcType == 8) {
                int unused6 = CmcConnectivityController.this.mServiceId = 0;
                int unused7 = CmcConnectivityController.this.mReservedId = -1;
                boolean unused8 = CmcConnectivityController.this.mIsNeedSubSession = false;
                Log.i(CmcConnectivityController.LOG_TAG, "wifi-direct disconnect, releaseP2pNetwork!");
                CmcConnectivityController.this.mNsdManager.releaseNetwork();
                CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_SD);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mReservedId = -1;
    /* access modifiers changed from: private */
    public IRegistrationManager mRm;
    /* access modifiers changed from: private */
    public int mServiceId = 0;
    private ITelephonyManager mTelephonyManager = null;
    private String mWifiAuthToken = "";
    private String mWifiDirectAuthToken = "";
    /* access modifiers changed from: private */
    public boolean mWifiDirectIsConnect = false;
    private ImsProfile mWifiDirectProfile = null;
    /* access modifiers changed from: private */
    public boolean mWifiDirectRegistered = false;
    private String mWifiHostPcscfIp = "";
    /* access modifiers changed from: private */
    public boolean mWifiIsConnect = false;
    private String mWifiLocalIp = "";
    /* access modifiers changed from: private */
    public ImsProfile mWifiProfile = null;
    /* access modifiers changed from: private */
    public boolean mWifiRegistered = false;

    public enum ConnectType {
        WifiDirect,
        Wifi,
        Wifi_HS,
        Internet
    }

    public enum DeviceType {
        PDevice,
        SDevice,
        None
    }

    public boolean isEnabledWifiDirectFeature() {
        return false;
    }

    public void changeWifiDirectConnection(boolean status) {
        String str = LOG_TAG;
        Log.i(str, "changeWifiDirectConnection, status: " + status);
        this.mNsdManager.releaseNetwork();
    }

    public CmcNsdManager getNsdManager() {
        return this.mNsdManager;
    }

    public void tryNsdBind() {
        String str = SemSystemProperties.get(NSD_FIRST_TRIIGER, CloudMessageProviderContract.JsonData.TRUE);
        this.mFirstTrigger = str;
        if (CloudMessageProviderContract.JsonData.TRUE.equals(str)) {
            SemSystemProperties.set(NSD_FIRST_TRIIGER, ConfigConstants.VALUE.INFO_COMPLETED);
            sendEmptyMessageDelayed(1001, 10000);
            return;
        }
        immediateNsdBind();
    }

    public void immediateNsdBind() {
        if (!this.mNsdManager.isBound()) {
            Log.i(LOG_TAG, "immediateNsdBind, called bind..");
            this.mNsdManager.bind();
        }
    }

    public void setDeviceIdInfo(String deviceId, String primaryDeviceId) {
        this.mOwnDeviceId = deviceId;
        this.mOwnDuid = "urn:duid:" + deviceId;
        this.mPrimaryDuid = "urn:duid:" + primaryDeviceId;
        String str = LOG_TAG;
        Log.i(str, "ownDuid: " + this.mOwnDuid + ", primaryDuid: " + this.mPrimaryDuid);
    }

    public CmcConnectivityController(Looper looper, IRegistrationManager rm) {
        super(looper);
        Context context = ImsServiceStub.getInstance().getContext();
        this.mContext = context;
        this.mRm = rm;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mIpServiceManager = new IpServiceManager(this.mContext);
        String type = Settings.Global.getString(this.mContext.getContentResolver(), "cmc_device_type");
        if ("pd".equals(type)) {
            this.mDeviceType = DeviceType.PDevice;
        } else if ("sd".equals(type)) {
            this.mDeviceType = DeviceType.SDevice;
        } else {
            this.mDeviceType = DeviceType.None;
        }
        if (this.mDeviceType != DeviceType.None) {
            registerP2pListener();
        }
        this.mPhoneCount = SimUtil.getPhoneCount();
        String str = LOG_TAG;
        IMSLog.i(str, "mPhoneCount: " + this.mPhoneCount);
        CmcNsdManager cmcNsdManager = new CmcNsdManager(this.mContext);
        this.mNsdManager = cmcNsdManager;
        cmcNsdManager.registerServiceConnectionListener(this.mBindStatusListener);
    }

    public void setPhoneId(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public DeviceType getDeviceType() {
        return this.mDeviceType;
    }

    public void setNeedSubSession(boolean isNeed) {
        this.mIsNeedSubSession = isNeed;
    }

    public int getReservedId() {
        return this.mReservedId;
    }

    public void setReservedId(int id) {
        this.mReservedId = id;
    }

    /* access modifiers changed from: private */
    public void onShutDownNsd(boolean retyBind) {
        this.mIsNeedSubSession = false;
        this.mServiceId = 0;
        this.mReservedId = -1;
        onWifiDirectConnectionChanged(false, "");
        onWifiConnectionChanged(false, "", "", (ArrayList<String>) null);
        if (retyBind) {
            sendEmptyMessage(1001);
        }
    }

    /* access modifiers changed from: private */
    public void onWifiDirectConnectionChanged(boolean isConnected, String authToken) {
        if (this.mDeviceType != DeviceType.None) {
            IMSLog.i(LOG_TAG, "onWifiDirectConnectionChanged()");
            if (isConnected) {
                String str = LOG_TAG;
                IMSLog.i(str, "mWifiDirectIsConnect: " + this.mWifiDirectIsConnect + ", mWifiDirectRegistered: " + this.mWifiDirectRegistered);
                if (this.mWifiDirectRegistered) {
                    IMSLog.i(LOG_TAG, "already wifi direct is registered, maybe it'll be connected for 3rd SD");
                    return;
                }
                this.mIsNeedSubSession = true;
                this.mWifiDirectIsConnect = true;
                this.mWifiDirectAuthToken = authToken;
                if (this.mDeviceType == DeviceType.PDevice) {
                    imsRegister(ConnectType.WifiDirect, WD_PDN_NAME, WD_PROFILE_NAME_PD);
                    this.mIpServiceManager.ipRuleRemove("local_network", WD_HOST_PCSCF_IP);
                    if (this.mRm.isCmcRegistered(this.mPhoneId) > 0 || this.mWifiRegistered) {
                        String ipAddress = getIpAddress(ConnectType.Wifi);
                        this.mWifiLocalIp = ipAddress;
                        this.mIpServiceManager.ipRuleAdd("wlan0", ipAddress);
                    }
                } else if (this.mDeviceType == DeviceType.SDevice && isReadyToWifiDirectRegister()) {
                    imsRegister(ConnectType.WifiDirect, WD_PDN_NAME, WD_PROFILE_NAME_SD);
                }
            } else if (this.mWifiDirectIsConnect) {
                IMSLog.i(LOG_TAG, "the Wifi-Direct are all disconnected");
                if (this.mDeviceType == DeviceType.PDevice) {
                    this.mIpServiceManager.ipRuleRemove("local_network", WD_HOST_PCSCF_IP);
                    if (!TextUtils.isEmpty(this.mWifiLocalIp)) {
                        this.mIpServiceManager.ipRuleRemove("wlan0", this.mWifiLocalIp);
                    }
                }
                this.mWifiDirectIsConnect = false;
                this.mWifiDirectAuthToken = "";
                if (!this.mWifiDirectRegistered || this.mWifiDirectProfile == null) {
                    this.mServiceId = 0;
                    this.mReservedId = -1;
                    this.mIsNeedSubSession = false;
                } else {
                    for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                        this.mRm.deregisterProfile(this.mWifiDirectProfile.getId(), phoneId, false);
                    }
                }
                this.mWifiDirectRegistered = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public void onWifiConnectionChanged(boolean isConnected, String hostAddress, String authToken, ArrayList<String> infList) {
        if (this.mDeviceType != DeviceType.None) {
            IMSLog.i(LOG_TAG, "onWifiConnectionChanged()");
            if (this.mDeviceType != DeviceType.PDevice) {
                wifiConnectionChanged(isConnected, hostAddress, authToken);
            } else if (infList == null || infList.isEmpty()) {
                IMSLog.i(LOG_TAG, "there are no network interface, all disconnect");
                hotspotConnectionChanged(false, hostAddress, authToken);
                wifiConnectionChanged(false, hostAddress, authToken);
            } else if (infList.size() > 1) {
                IMSLog.i(LOG_TAG, "wifi register by priority (WIFI > MOBILE-HOTSPOT)");
                hotspotConnectionChanged(false, hostAddress, authToken);
                wifiConnectionChanged(isConnected, hostAddress, authToken);
            } else {
                String intf = infList.get(0);
                String str = LOG_TAG;
                IMSLog.i(str, "tryRegister intf: " + intf);
                if ("wlan0".equals(intf)) {
                    hotspotConnectionChanged(false, hostAddress, authToken);
                    wifiConnectionChanged(isConnected, hostAddress, authToken);
                    return;
                }
                wifiConnectionChanged(false, hostAddress, authToken);
                hotspotConnectionChanged(isConnected, hostAddress, authToken);
            }
        }
    }

    private void imsRegister(ConnectType type, String pdnName, String profileName) {
        for (ImsProfile p : ImsProfileLoader.getProfileListWithMnoName(this.mContext, "MDMN", 0)) {
            if ("SamsungCMC_P2P".equals(p.getName())) {
                p.setDuid(this.mOwnDuid);
                p.setPdn(pdnName);
                p.setName(profileName);
                p.setDomain(SERVER_DOMAIN);
                p.setPriDeviceIdWithURN(this.mPrimaryDuid);
                p.setDisplayName(this.mOwnDeviceId);
                p.setImpuList("sip:D2D@samsungims.com");
                p.setImpi("D2D@samsungims.com");
                p.setSslType(4);
                p.setNetworkEnabled(13, false);
                p.setNetworkEnabled(3, false);
                p.setNetworkEnabled(10, false);
                p.setNetworkEnabled(15, false);
                p.setNetworkEnabled(8, false);
                p.setNetworkEnabled(9, false);
                if (type == ConnectType.WifiDirect) {
                    ArrayList<String> pcscflist = new ArrayList<>();
                    pcscflist.add(WD_HOST_PCSCF_IP);
                    p.setPcscfList(pcscflist);
                    p.setAccessToken(this.mWifiDirectAuthToken);
                    p.setId(p.getId() + 10000);
                    this.mWifiDirectProfile = p;
                    for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                        this.mRm.registerProfile(this.mWifiDirectProfile, phoneId);
                    }
                    return;
                }
                p.setVceConfigEnabled(true);
                ArrayList<String> pcscflist2 = new ArrayList<>();
                if (type == ConnectType.Wifi) {
                    pcscflist2.add(this.mWifiHostPcscfIp);
                    p.setPcscfList(pcscflist2);
                    p.setAccessToken(this.mWifiAuthToken);
                    this.mWifiProfile = p;
                    for (int phoneId2 = 0; phoneId2 < this.mPhoneCount; phoneId2++) {
                        this.mRm.registerProfile(this.mWifiProfile, phoneId2);
                    }
                    return;
                } else if (type == ConnectType.Wifi_HS) {
                    pcscflist2.add(this.mHotspotHostPcscfIp);
                    p.setPcscfList(pcscflist2);
                    p.setAccessToken(this.mHotspotAuthToken);
                    Set<String> svc = new HashSet<>();
                    svc.add("mmtel");
                    p.setServiceSet(13, svc);
                    p.setNetworkEnabled(13, true);
                    this.mHotspotProfile = p;
                    for (int phoneId3 = 0; phoneId3 < this.mPhoneCount; phoneId3++) {
                        this.mRm.registerProfile(this.mHotspotProfile, phoneId3);
                    }
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private void wifiConnectionChanged(boolean isConnected, String hostAddress, String authToken) {
        if (isConnected) {
            this.mWifiIsConnect = true;
            this.mWifiHostPcscfIp = hostAddress;
            this.mWifiAuthToken = authToken;
            if (this.mDeviceType == DeviceType.PDevice) {
                this.mWifiHostPcscfIp = getIpAddress(ConnectType.Wifi);
                if (isReadyToWifiPDRegister()) {
                    imsRegister(ConnectType.Wifi, WIFI_PDN_NAME, WIFI_PROFILE_NAME_PD);
                }
            } else if (isReadyToWifiSDRegister()) {
                imsRegister(ConnectType.Wifi, WIFI_PDN_NAME, WIFI_PROFILE_NAME_SD);
            }
        } else if (this.mWifiIsConnect) {
            IMSLog.i(LOG_TAG, "the Wifi are all disconnected");
            this.mWifiIsConnect = false;
            this.mWifiLocalIp = "";
            this.mWifiHostPcscfIp = "";
            this.mWifiAuthToken = "";
            if (this.mWifiRegistered && this.mWifiProfile != null) {
                for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                    this.mRm.deregisterProfile(this.mWifiProfile.getId(), phoneId, false);
                }
            }
            this.mWifiRegistered = false;
        }
    }

    private void hotspotConnectionChanged(boolean isConnected, String hostAddress, String authToken) {
        if (isConnected) {
            this.mHotspotIsConnect = true;
            this.mHotspotAuthToken = authToken;
            this.mHotspotHostPcscfIp = getIpAddress(ConnectType.Wifi_HS);
            if (isReadyToHotspotRegister()) {
                imsRegister(ConnectType.Wifi_HS, WIFI_HS_PDN_NAME, WIFI_HS_PROFILE_NAME_PD);
            }
        } else if (this.mHotspotIsConnect) {
            IMSLog.i(LOG_TAG, "the Hotspot are all disconnected");
            if (!TextUtils.isEmpty(this.mHotspotHostPcscfIp)) {
                this.mIpServiceManager.ipRuleRemove("local_network", this.mHotspotHostPcscfIp);
            }
            this.mHotspotIsConnect = false;
            this.mHotspotLocalIp = "";
            this.mHotspotHostPcscfIp = "";
            this.mHotspotAuthToken = "";
            if (this.mHotspotRegistered && this.mHotspotProfile != null) {
                for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                    this.mRm.deregisterProfile(this.mHotspotProfile.getId(), phoneId, false);
                }
            }
            this.mHotspotRegistered = false;
        }
    }

    private boolean isReadyToWifiRegister() {
        String str = LOG_TAG;
        IMSLog.i(str, "mWifiIsConnect: " + this.mWifiIsConnect + ", mWifiRegistered: " + this.mWifiRegistered);
        if (this.mWifiIsConnect && !this.mWifiRegistered && !TextUtils.isEmpty(this.mWifiHostPcscfIp)) {
            return true;
        }
        return false;
    }

    private boolean isReadyToWifiPDRegister() {
        if (!isReadyToWifiRegister()) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "mHotspotRegistered: " + this.mHotspotRegistered);
        if (!this.mHotspotRegistered) {
            return true;
        }
        IMSLog.i(LOG_TAG, "There is already [mobile-hotspot] registration. don't wifi registration");
        return false;
    }

    private boolean isReadyToWifiSDRegister() {
        if (!isReadyToWifiRegister()) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "mWifiDirectRegistered: " + this.mWifiDirectRegistered);
        if (this.mWifiDirectRegistered && isExistCalls()) {
            IMSLog.i(LOG_TAG, "There is already wifi-direct(+calls) registration. don't wifi registration");
            return false;
        } else if (this.mRm.isCmcRegistered(this.mPhoneId) <= 0) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, "There is already cmc registration. don't wifi registration");
            return false;
        }
    }

    private boolean isReadyToWifiDirectRegister() {
        if (this.mRm.isCmcRegistered(this.mPhoneId) > 0) {
            IMSLog.i(LOG_TAG, "there is already a [cmc] registration. releaseP2pNetwork!");
            this.mNsdManager.releaseNetwork();
            return false;
        } else if (!this.mWifiRegistered || this.mWifiProfile == null) {
            return true;
        } else {
            if (isExistCalls()) {
                Log.e(LOG_TAG, "there are calls with [wifi], releaseP2pNetwork!");
                this.mNsdManager.releaseNetwork();
                return false;
            }
            Log.i(LOG_TAG, "invalid wifi registration state, deregister [case A]");
            for (int phoneId = 0; phoneId < this.mPhoneCount; phoneId++) {
                this.mRm.deregisterProfile(this.mWifiProfile.getId(), phoneId, false);
            }
            return true;
        }
    }

    private boolean isReadyToHotspotRegister() {
        String str = LOG_TAG;
        IMSLog.i(str, "mHotspotIsConnect: " + this.mHotspotIsConnect + ", mHotspotRegistered: " + this.mHotspotRegistered);
        if (!this.mHotspotIsConnect || this.mHotspotRegistered || TextUtils.isEmpty(this.mHotspotHostPcscfIp)) {
            return false;
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "mWifiRegistered: " + this.mWifiRegistered);
        if (!this.mWifiRegistered) {
            return true;
        }
        IMSLog.i(LOG_TAG, "There is already [wifi] registration. don't mobile-hotspot registration");
        return false;
    }

    /* access modifiers changed from: private */
    public void retryWifiRegister(String profileName) {
        String str = LOG_TAG;
        IMSLog.i(str, "retryWifiRegister: " + profileName);
        if (this.mDeviceType == DeviceType.PDevice) {
            if (isReadyToWifiPDRegister()) {
                imsRegister(ConnectType.Wifi, WIFI_PDN_NAME, profileName);
            }
        } else if (isReadyToWifiSDRegister()) {
            imsRegister(ConnectType.Wifi, WIFI_PDN_NAME, profileName);
        }
    }

    private boolean isExistCalls() {
        IVolteServiceModule mVolteServiceModule = ImsServiceStub.getInstance().getServiceModuleManager().getVolteServiceModule();
        if (mVolteServiceModule != null) {
            return mVolteServiceModule.hasActiveCall(this.mPhoneId);
        }
        return false;
    }

    /* access modifiers changed from: private */
    public String getHSPref() {
        return ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, IMS_PCSCF_IP, 0, false).getString(WIFI_HS_PCSCF_PREF, "");
    }

    /* access modifiers changed from: private */
    public void setHSPref(String pcscf) {
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, IMS_PCSCF_IP, 0, false).edit();
        editor.putString(WIFI_HS_PCSCF_PREF, pcscf);
        editor.apply();
        String str = LOG_TAG;
        IMSLog.i(str, "setHSPref: " + pcscf);
    }

    public void handleMessage(Message msg) {
        if (msg.what == 1001) {
            Log.i(LOG_TAG, "EVENT_TRY_NSD_BIND");
            immediateNsdBind();
        }
    }

    private void registerP2pListener() {
        Log.i(LOG_TAG, "registerP2pListener");
        try {
            this.mRm.registerP2pListener(this.mRegisterP2pListener);
        } catch (Exception e) {
            Log.e(LOG_TAG, "registerP2pListener failed");
        }
    }

    private void unregisterImsRegistrationListener() {
        Log.i(LOG_TAG, "unregisterImsRegistrationListener");
    }

    private String getIpAddress(ConnectType type) {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (type == ConnectType.WifiDirect) {
                    if (!intf.getName().contains("p2p")) {
                    }
                } else if (type == ConnectType.Wifi_HS) {
                    if (!intf.getName().contains(WIFI_HS_PDN_NAME)) {
                    }
                } else if (type == ConnectType.Wifi) {
                    if (!intf.getName().contains(WIFI_PDN_NAME)) {
                    }
                } else if (type == ConnectType.Internet && !intf.getName().contains("rmnet0")) {
                }
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && NetworkUtil.isIPv4Address(addr.getHostAddress())) {
                        return addr.getHostAddress().toString();
                    }
                }
                continue;
            }
        } catch (Exception e) {
            IMSLog.i(LOG_TAG, "error in parsing");
        }
        IMSLog.i(LOG_TAG, "returning empty ip address");
        return "";
    }
}
