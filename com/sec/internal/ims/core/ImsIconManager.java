package com.sec.internal.ims.core;

import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ServiceStateWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;

public class ImsIconManager {
    private static final String CMC_SD_ICON = "stat_sys_phone_call_skt";
    public static final String DEFAULT_VOLTE_REGI_ICON_ID = "stat_notify_volte_service_avaliable";
    private static final String DUAL_IMS_NO_CTC_VOLTE_ICON_NAME = "stat_sys_phone_no_volte_chn_hd";
    private static final String INTENT_ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
    private static final String INTENT_ACTION_SILENT_REDIAL = "android.intent.action.PHONE_NEED_SILENT_REDIAL";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsIconManager.class.getSimpleName();
    private static final int NOTIFICATION_BUILDER__ID = -26247;
    private static final String NO_CTC_VOLTE_ICON_NAME = "stat_sys_phone_no_volte_chn_ctc";
    private static final String PRIMARY_CHANNEL = "imsicon_channel";
    private static final String RCS_ICON_DESCRIPTION = "RCS";
    private static final String RCS_ICON_NAME = "stat_notify_rcs_service_avaliable";
    private static final String RCS_ICON_NAME_CMCC = "stat_notify_rcs";
    private static final String[] RCS_ICON_NAME_DUAL = {"stat_notify_rcs_service_avaliable_1", "stat_notify_rcs_service_avaliable_2", "stat_notify_rcs_service_avaliable_dual"};
    private static final String RCS_ICON_SLOT = "com.samsung.rcs";
    private static final String VOLTE_ICON_SLOT_HEAD = "ims_volte";
    private static String mRegiIndicatorID = "";
    private static boolean[] mShowVoWIFILabel = {false, false, false};
    private static String[] mVowifiOperatorLabel = {"", ""};
    private static int[] mVowifiOperatorLabelOngoing = {0, 0};
    private static String[] mWifiSubTextOnLockScreen = {"", ""};
    private String VOLTE_ICON_SLOT = "";
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    protected boolean mCurrentInRoaming;
    protected int mCurrentNetworkType;
    /* access modifiers changed from: private */
    public int mCurrentPhoneState;
    protected int mCurrentServiceState;
    protected int mCurrentVoiceRatType;
    /* access modifiers changed from: private */
    public int mDisplayDensity = -1;
    /* access modifiers changed from: private */
    public boolean mForceRefreshIcon = false;
    private final BroadcastReceiver mIconBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DisplayMetrics metrics;
            int currDensity;
            String action = intent.getAction();
            String access$100 = ImsIconManager.LOG_TAG;
            int access$200 = ImsIconManager.this.mPhoneId;
            IMSLog.i(access$100, access$200, "Received intent: " + action + " extra: " + intent.getExtras());
            if (action.equals(ImsIconManager.INTENT_ACTION_SILENT_REDIAL)) {
                ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(ImsIconManager.this.mPhoneId);
                if (ImsIconManager.this.mMno != Mno.SKT) {
                    ImsIconManager imsIconManager = ImsIconManager.this;
                    if (!imsIconManager.needHideIconWhenCSCall(imsIconManager.mMno)) {
                        return;
                    }
                }
                if (sm != null && sm.isSimAvailable()) {
                    IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "Silent Redial Enabled");
                    if (SimUtil.getPhoneCount() > 1) {
                        if (ImsIconManager.this.mPhoneId == intent.getIntExtra("SLOTID", -1)) {
                            boolean unused = ImsIconManager.this.mIsSilentRedialInProgress = true;
                            ImsIconManager.this.updateRegistrationIcon(false);
                            return;
                        }
                        return;
                    }
                    boolean unused2 = ImsIconManager.this.mIsSilentRedialInProgress = true;
                    ImsIconManager.this.updateRegistrationIcon(false);
                }
            } else if (action.equals(ImsIconManager.INTENT_ACTION_CONFIGURATION_CHANGED) && (metrics = ImsIconManager.this.mContext.getResources().getDisplayMetrics()) != null && ImsIconManager.this.mDisplayDensity != (currDensity = metrics.densityDpi)) {
                IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "config is changed. update icon");
                boolean unused3 = ImsIconManager.this.mForceRefreshIcon = true;
                ImsIconManager.this.updateRegistrationIcon(false);
                int unused4 = ImsIconManager.this.mDisplayDensity = currDensity;
                boolean unused5 = ImsIconManager.this.mForceRefreshIcon = false;
            }
        }
    };
    private boolean mIsDuringEmergencyCall;
    private boolean mIsFirstVoLTEIconShown = false;
    /* access modifiers changed from: private */
    public boolean mIsSilentRedialInProgress;
    private IconVisibility mLastRcsVisiblity = IconVisibility.HIDE;
    private int mLastVoLTEResourceId = -1;
    private IconVisibility mLastVoLTEVisiblity = IconVisibility.UNKNOWN;
    /* access modifiers changed from: private */
    public Mno mMno;
    private NotificationManager mNotificationManager;
    private String mOmcCode;
    private final String mPackageName;
    /* access modifiers changed from: private */
    public final PdnController mPdnController;
    /* access modifiers changed from: private */
    public int mPhoneId = 0;
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0044, code lost:
            if (com.sec.internal.ims.core.ImsIconManager.access$400(r1, com.sec.internal.ims.core.ImsIconManager.access$300(r1)) != false) goto L_0x0046;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onCallStateChanged(int r5, java.lang.String r6) {
            /*
                r4 = this;
                com.sec.internal.ims.core.ImsIconManager r0 = com.sec.internal.ims.core.ImsIconManager.this
                int r0 = r0.mCurrentPhoneState
                if (r5 != r0) goto L_0x0009
                return
            L_0x0009:
                java.lang.String r0 = com.sec.internal.ims.core.ImsIconManager.LOG_TAG
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                int r1 = r1.mPhoneId
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "call state is changed to ["
                r2.append(r3)
                r2.append(r5)
                java.lang.String r3 = "]"
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                com.sec.internal.log.IMSLog.i(r0, r1, r2)
                com.sec.internal.ims.core.ImsIconManager r0 = com.sec.internal.ims.core.ImsIconManager.this
                int unused = r0.mCurrentPhoneState = r5
                r0 = 0
                if (r5 != 0) goto L_0x0050
                boolean r1 = com.sec.internal.helper.OmcCode.isKOROmcCode()
                if (r1 != 0) goto L_0x0046
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                com.sec.internal.constants.Mno r2 = r1.mMno
                boolean r1 = r1.needHideIconWhenCSCall(r2)
                if (r1 == 0) goto L_0x0050
            L_0x0046:
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                boolean unused = r1.mIsSilentRedialInProgress = r0
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                r1.updateRegistrationIcon(r0)
            L_0x0050:
                if (r5 != 0) goto L_0x005f
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                boolean r1 = r1.getDuringEmergencyCall()
                if (r1 == 0) goto L_0x005f
                com.sec.internal.ims.core.ImsIconManager r1 = com.sec.internal.ims.core.ImsIconManager.this
                r1.setDuringEmergencyCall(r0)
            L_0x005f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.ImsIconManager.AnonymousClass1.onCallStateChanged(int, java.lang.String):void");
        }

        private boolean isImsIconSupportedNW(int networkType) {
            return networkType == 13 || networkType == 20 || networkType == 18;
        }

        private boolean isNWTypeChangedUpdateRequires(int oldNetworkType, int currentNetworkType) {
            return isImsIconSupportedNW(oldNetworkType) != isImsIconSupportedNW(currentNetworkType);
        }

        private boolean isUpdateRequires(ServiceState serviceState) {
            ServiceStateWrapper ss = new ServiceStateWrapper(serviceState);
            int oldNetworkType = ImsIconManager.this.mCurrentNetworkType;
            int oldServiceState = ImsIconManager.this.mCurrentServiceState;
            int oldVoiceRatType = ImsIconManager.this.mCurrentVoiceRatType;
            boolean oldRoamingState = ImsIconManager.this.mCurrentInRoaming;
            ImsIconManager.this.setCurrentNetworkType(ss.getDataNetworkType());
            ImsIconManager.this.setCurrentServiceState(ss.getDataRegState());
            ImsIconManager.this.setCurrentVoiceRatType(ss.getVoiceNetworkType());
            ImsIconManager.this.setCurrentRoamingState(ss.getVoiceRoaming());
            boolean isRoamingStateChanged = ImsIconManager.this.mMno.isOneOf(Mno.CTC, Mno.CTCMO) && oldRoamingState != ImsIconManager.this.mCurrentInRoaming;
            boolean isServiceStateChanged = (oldServiceState != 0 && ss.getDataRegState() == 0) || (oldServiceState == 0 && ss.getDataRegState() != 0);
            boolean isVoiceRatTypeChanged = ImsIconManager.this.mMno.isOneOf(Mno.CTC, Mno.CTCMO) && oldVoiceRatType != ImsIconManager.this.mCurrentVoiceRatType;
            if (isServiceStateChanged || isNWTypeChangedUpdateRequires(oldNetworkType, ImsIconManager.this.mCurrentNetworkType) || isRoamingStateChanged || isVoiceRatTypeChanged) {
                return true;
            }
            return false;
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            String access$100 = ImsIconManager.LOG_TAG;
            int access$200 = ImsIconManager.this.mPhoneId;
            IMSLog.i(access$100, access$200, "onServiceStateChanged(" + serviceState + ")");
            boolean updateRequires = isUpdateRequires(serviceState);
            if ((ImsIconManager.this.mMno.isChn() || ImsIconManager.this.mMno.isHkMo() || ImsIconManager.this.mMno.isTw() || ConfigUtil.isRcsEur(ImsIconManager.this.mMno) || ImsIconManager.this.mMno.isOce() || ImsIconManager.this.mMno.isLatin() || ImsIconManager.this.mMno.isATTMexico()) && updateRequires) {
                boolean isSuspend = ImsIconManager.this.mPdnController.isSuspended(ImsIconManager.this.mConnectivityManager.getNetworkInfo(ImsIconManager.this.mCurrentNetworkType));
                IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "updateRegistrationIcon on RAT change");
                ImsIconManager.this.updateRegistrationIcon(isSuspend);
            }
            if (OmcCode.isKOROmcCode()) {
                ImsIconManager.this.updateRegistrationIcon(false);
            }
        }
    };
    private final ImsPhoneStateManager mPhoneStateManager;
    private final IRegistrationManager mRegistrationManager;
    private final ITelephonyManager mTelephonyManager;
    private boolean mUseDualVolteIcon = false;
    private final ContentObserver mVolteNotiObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChanged) {
            IMSLog.i(ImsIconManager.LOG_TAG, ImsIconManager.this.mPhoneId, "call settins is changed. update icon");
            ImsIconManager.this.updateRegistrationIcon(false);
        }
    };

    public enum Icon {
        VOLTE,
        VOWIFI
    }

    public enum IconVisibility {
        UNKNOWN,
        SHOW,
        HIDE
    }

    public ImsIconManager(Context context, IRegistrationManager regMgr, PdnController pdnController, Mno mno, int phoneId) {
        this.mContext = context;
        this.mPackageName = context.getPackageName();
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(this.mContext);
        this.mRegistrationManager = regMgr;
        this.mPdnController = pdnController;
        this.mOmcCode = OmcCode.get();
        this.mUseDualVolteIcon = showDualVolteIcon();
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mPhoneId = phoneId;
        this.mPhoneStateManager = new ImsPhoneStateManager(this.mContext, 33);
        if (OmcCode.isSKTOmcCode()) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("volte_noti_settings"), true, this.mVolteNotiObserver);
        }
        IntentFilter iconIntentFilter = new IntentFilter();
        iconIntentFilter.addAction(INTENT_ACTION_SILENT_REDIAL);
        iconIntentFilter.addAction(INTENT_ACTION_CONFIGURATION_CHANGED);
        this.mContext.registerReceiver(this.mIconBroadcastReceiver, iconIntentFilter);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        initConfiguration(mno, phoneId);
    }

    public void initConfiguration(Mno mno, int phoneId) {
        this.mMno = mno;
        this.mPhoneId = phoneId;
        StringBuilder sb = new StringBuilder();
        sb.append(VOLTE_ICON_SLOT_HEAD);
        sb.append(this.mPhoneId == 0 ? "" : "2");
        this.VOLTE_ICON_SLOT = sb.toString();
        this.mIsSilentRedialInProgress = false;
        this.mIsDuringEmergencyCall = false;
        if (this.mPhoneStateManager.hasListener(phoneId)) {
            unRegisterPhoneStateListener();
        }
        registerPhoneStateListener();
        clearIcon(phoneId);
    }

    private boolean isServiceAvailable(String service) {
        int currentNetwork;
        if ("ATT".equals(this.mOmcCode) || "APP".equals(this.mOmcCode)) {
            if (SimUtil.isSoftphoneEnabled() != 0 || (currentNetwork = this.mRegistrationManager.getCurrentNetworkByPhoneId(this.mPhoneId)) == 13 || currentNetwork == 18) {
                return true;
            }
            if ("mmtel".equals(service) || "mmtel-video".equals(service)) {
                return false;
            }
            return true;
        } else if (this.mMno != Mno.BOG && this.mMno != Mno.ORANGE && this.mMno != Mno.ORANGE_POLAND && this.mMno != Mno.DIGI && this.mMno != Mno.TELECOM_ITALY && this.mMno != Mno.VODAFONE && !this.mMno.isTmobile() && this.mMno != Mno.TELEKOM_ALBANIA && this.mMno != Mno.VODAFONE_NEWZEALAND && this.mMno != Mno.WINDTRE) {
            return true;
        } else {
            int currentNetwork2 = this.mRegistrationManager.getCurrentNetworkByPhoneId(this.mPhoneId);
            if (currentNetwork2 != 13 && (currentNetwork2 != 18 || !this.mPdnController.isEpdgConnected(this.mPhoneId))) {
                return false;
            }
            if ("mmtel".equals(service) || "mmtel-video".equals(service)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean needHideIconWhenCSCall(Mno mno) {
        return mno.isHkMo() || mno.isTw() || mno.isLatin() || mno == Mno.VODAFONE_AUSTRALIA || mno == Mno.PTR || mno == Mno.VODAFONE_NETHERLAND || mno == Mno.MTS_RUSSIA || mno == Mno.ETISALAT_UAE;
    }

    private boolean needShowNoCTCVoLTEIcon() {
        int i;
        boolean result = false;
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (sm != null && sm.getRilSimOperator().contains("CTC") && (this.mUseDualVolteIcon || this.mPhoneId == SimUtil.getDefaultPhoneId())) {
            int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId);
            int airPlaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
            if (voiceType == 0 && (((i = this.mCurrentNetworkType) == 13 || i == 20) && airPlaneModeOn == 0 && sm.isSimLoaded() && this.mTelephonyManager.getCurrentPhoneTypeForSlot(this.mPhoneId) != 2 && this.mCurrentVoiceRatType != 7 && !this.mCurrentInRoaming)) {
                result = true;
            }
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "needShowNoCTCVoLTEIcon: " + result);
        return result;
    }

    class IconVisiblities {
        boolean mShowRcsIcon;
        boolean mShowVoWiFiIcon;
        boolean mShowVolteIcon;

        IconVisiblities() {
        }

        /* access modifiers changed from: package-private */
        public void setShowVolteIcon(boolean show) {
            this.mShowVolteIcon = show;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowVolteIcon() {
            return this.mShowVolteIcon;
        }

        /* access modifiers changed from: package-private */
        public void setShowVoWiFiIcon(boolean show) {
            this.mShowVoWiFiIcon = show;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowVowiFiIcon() {
            return this.mShowVoWiFiIcon;
        }

        /* access modifiers changed from: package-private */
        public void setShowRcsIcon(boolean show) {
            this.mShowRcsIcon = show;
        }

        /* access modifiers changed from: package-private */
        public boolean isShowRcsIcon() {
            return this.mShowRcsIcon;
        }
    }

    class RegistrationStatus {
        boolean mCmcRegistered;
        boolean mRcsRegistered;
        boolean mVolteRegistered;
        boolean mVowifiRegistered;

        RegistrationStatus() {
        }

        /* access modifiers changed from: package-private */
        public void setVolteRegistered(boolean registered) {
            this.mVolteRegistered = registered;
        }

        /* access modifiers changed from: package-private */
        public boolean isVolteRegistered() {
            return this.mVolteRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setRcsRegistered(boolean registered) {
            this.mRcsRegistered = registered;
        }

        /* access modifiers changed from: package-private */
        public boolean isRcsRegistered() {
            return this.mRcsRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setVowifiRegistered(boolean registered) {
            this.mVowifiRegistered = registered;
        }

        /* access modifiers changed from: package-private */
        public boolean isVowifiRegistered() {
            return this.mVowifiRegistered;
        }

        /* access modifiers changed from: package-private */
        public void setCmcRegistered(boolean registered) {
            this.mCmcRegistered = registered;
        }

        /* access modifiers changed from: package-private */
        public boolean isCmcRegistered() {
            return this.mCmcRegistered;
        }

        /* access modifiers changed from: package-private */
        public boolean isAllRegistered() {
            return (isVolteRegistered() || isVowifiRegistered()) && isRcsRegistered();
        }
    }

    public void updateRegistrationIcon(boolean isSuspend) {
        boolean isCmcOnlyReg = false;
        int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, 0, this.mPhoneId);
        IconVisiblities visiblities = updateShowIconSettings(voiceType);
        ImsRegistration[] regArray = this.mRegistrationManager.getRegistrationInfoByPhoneId(this.mPhoneId);
        RegistrationStatus registrationStatus = updateRegistrationStatus(regArray, isSuspend, voiceType);
        IMSLog.i(LOG_TAG, this.mPhoneId, "updateRegistrationIcon: VoLTE [show: " + visiblities.isShowVolteIcon() + ", regi: " + registrationStatus.isVolteRegistered() + "] VoWiFi [show: " + visiblities.isShowVowiFiIcon() + ", regi: " + registrationStatus.isVowifiRegistered() + "] RCS [show: " + visiblities.isShowRcsIcon() + ", regi: " + registrationStatus.isRcsRegistered() + "] (SUSPENDED: " + isSuspend + ")");
        if (registrationStatus.isCmcRegistered() && regArray != null && regArray.length == 1) {
            isCmcOnlyReg = true;
        }
        updateVolteIcon(visiblities, registrationStatus, isCmcOnlyReg);
        updateRcsIcon(visiblities, registrationStatus, isSuspend);
        updateVoWifiLabel(visiblities, registrationStatus);
    }

    private IconVisiblities updateShowIconSettings(int voiceType) {
        int i;
        IconVisiblities visiblities = new IconVisiblities();
        visiblities.setShowVolteIcon(true);
        visiblities.setShowVoWiFiIcon(ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SHOW_VOWIFI_REGI_ICON, false));
        visiblities.setShowRcsIcon(true);
        boolean showVolteRegIcon = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.Registration.SHOW_VOLTE_REGI_ICON, false);
        int removeIconNoSvc = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.Registration.REMOVE_ICON_NOSVC, 0);
        if (!Build.IS_DEBUGGABLE) {
            if (!showVolteRegIcon) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Volte/RCS RegistrationIcon: turned off.");
                visiblities.setShowVolteIcon(false);
            }
            if (this.mMno != Mno.CMCC) {
                visiblities.setShowRcsIcon(false);
            }
        }
        if (needHideIconWhenCSCall(this.mMno) && visiblities.isShowVowiFiIcon() && this.mIsSilentRedialInProgress) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "VoWIFI Special Req.: Hide vowifi icon when CSFB");
            visiblities.setShowVoWiFiIcon(false);
        }
        if ("DCM".equals(this.mOmcCode) && this.mPdnController.getVopsIndication(this.mPhoneId) == VoPsIndication.NOT_SUPPORTED) {
            visiblities.setShowVolteIcon(false);
        }
        if (this.mMno.isKor()) {
            if (OmcCode.isKOROmcCode()) {
                visiblities.setShowVolteIcon(checkKORVolteIcon());
            } else if (voiceType != 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "KOR requirement");
                visiblities.setShowVolteIcon(false);
            }
        } else if (removeIconNoSvc == 1 && !(this.mCurrentServiceState == 0 && ((i = this.mCurrentNetworkType) == 13 || i == 20 || i == 18))) {
            visiblities.setShowVolteIcon(false);
        }
        return visiblities;
    }

    private RegistrationStatus updateRegistrationStatus(ImsRegistration[] regArray, boolean isSuspend, int voiceType) {
        RegistrationStatus registrationStatus = new RegistrationStatus();
        boolean z = true;
        if (OmcCode.isKOROmcCode() && this.mMno == Mno.KT && this.mTelephonyManager.getServiceState() == 0) {
            registrationStatus.setVolteRegistered(true);
        }
        if (regArray == null) {
            return registrationStatus;
        }
        for (ImsRegistration reg : regArray) {
            if (isVoImsRegistered(reg)) {
                boolean isVoWiFiConnected = isVoWiFiConnected(reg);
                registrationStatus.setVolteRegistered(!isVoWiFiConnected);
                registrationStatus.setVowifiRegistered(isVoWiFiConnected);
            }
            if (reg.getImsProfile().getCmcType() == 2 || reg.getImsProfile().getCmcType() == 4 || reg.getImsProfile().getCmcType() == 8) {
                registrationStatus.setCmcRegistered(true);
            }
            if (reg.hasRcsService() && !isSuspend && ((this.mMno != Mno.CMCC || this.mCurrentServiceState == 0) && (this.mMno != Mno.CMCC || !isOtherSimInCallStatus()))) {
                registrationStatus.setRcsRegistered(true);
            }
            if (registrationStatus.isAllRegistered()) {
                break;
            }
        }
        if (getDuringEmergencyCall() && registrationStatus.isVowifiRegistered()) {
            if (this.mMno == Mno.APT) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "APT special requirement");
                boolean volteTurnon = voiceType == 0;
                registrationStatus.setVolteRegistered(volteTurnon);
                if (volteTurnon) {
                    z = false;
                }
                registrationStatus.setVowifiRegistered(z);
            } else if (this.mMno == Mno.VODAFONE_AUSTRALIA) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "Vodafone AUS special requirement");
                registrationStatus.setVowifiRegistered(false);
            }
        }
        return registrationStatus;
    }

    private boolean isVoImsRegistered(ImsRegistration reg) {
        return hasVolteService(reg) && !reg.getImsProfile().hasEmergencySupport() && reg.getImsProfile().getCmcType() == 0 && (isServiceAvailable("mmtel") || isServiceAvailable("mmtel-video"));
    }

    private boolean isVoWiFiConnected(ImsRegistration reg) {
        int currentNetwork = this.mRegistrationManager.getCurrentNetwork(reg.getHandle());
        int currentRegiRat = reg.getRegiRat();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getRegiRat [" + currentRegiRat + "]");
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "getCurrentNetwork [" + currentNetwork + "]");
        if (currentNetwork != 18 || !this.mPdnController.isEpdgConnected(this.mPhoneId)) {
            return false;
        }
        if (this.mMno != Mno.CHT || currentRegiRat == 18) {
            return true;
        }
        return false;
    }

    private void updateVolteIcon(IconVisiblities visibilities, RegistrationStatus registrationStatus, boolean isCmcRegOnly) {
        String iconNametoSet = null;
        String description = null;
        if (this.mUseDualVolteIcon) {
            if (registrationStatus.isVowifiRegistered() && visibilities.isShowVowiFiIcon()) {
                iconNametoSet = getDualIMSIconName(Icon.VOWIFI);
                description = this.mContext.getResources().getString(R.string.DREAM_VOWIFI_T_DEX_OPT_ABB);
            } else if (registrationStatus.isVolteRegistered() && visibilities.isShowVolteIcon()) {
                iconNametoSet = getDualIMSIconName(Icon.VOLTE);
                description = this.mContext.getResources().getString(R.string.DREAM_VOLTE_T_DEX_OPT_ABB);
            } else if (needShowNoCTCVoLTEIcon()) {
                registrationStatus.setVolteRegistered(true);
                visibilities.setShowVolteIcon(true);
                iconNametoSet = DUAL_IMS_NO_CTC_VOLTE_ICON_NAME + Integer.toString(this.mPhoneId + 1);
            }
        } else if (registrationStatus.isVolteRegistered() && visibilities.isShowVolteIcon()) {
            iconNametoSet = getVolteIconName();
            description = this.mContext.getResources().getString(R.string.DREAM_VOLTE_T_DEX_OPT_ABB);
        } else if (registrationStatus.isVowifiRegistered() && visibilities.isShowVowiFiIcon()) {
            iconNametoSet = getVowifiIconName();
            description = this.mContext.getResources().getString(R.string.DREAM_VOWIFI_T_DEX_OPT_ABB);
        } else if (needShowNoCTCVoLTEIcon()) {
            registrationStatus.setVolteRegistered(true);
            visibilities.setShowVolteIcon(true);
            iconNametoSet = NO_CTC_VOLTE_ICON_NAME;
        }
        if (description == null) {
            description = "";
        }
        if (Build.IS_DEBUGGABLE && isCmcRegOnly) {
            iconNametoSet = CMC_SD_ICON;
            registrationStatus.setVolteRegistered(true);
        }
        if (!TextUtils.isEmpty(iconNametoSet)) {
            setIconSlot(this.VOLTE_ICON_SLOT, iconNametoSet, description);
        }
        setIconVisibility(this.VOLTE_ICON_SLOT, getVolteIconVisibility(visibilities, registrationStatus));
    }

    private IconVisibility getVolteIconVisibility(IconVisiblities visibility, RegistrationStatus registrationStatus) {
        return ((!visibility.isShowVolteIcon() || ((!OmcCode.isKOROmcCode() || !this.mMno.isKor()) && !registrationStatus.isVolteRegistered())) && (!visibility.isShowVowiFiIcon() || !registrationStatus.isVowifiRegistered())) ? IconVisibility.HIDE : IconVisibility.SHOW;
    }

    private IconVisibility getRcsIconVisibility(IconVisiblities visibility, RegistrationStatus registrationStatus) {
        return (!visibility.isShowRcsIcon() || !registrationStatus.isRcsRegistered()) ? IconVisibility.HIDE : IconVisibility.SHOW;
    }

    private void updateRcsIcon(IconVisiblities visibilities, RegistrationStatus registrationStatus, boolean isSuspend) {
        int i = 1;
        boolean isRcsIconVisible = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.SHOW_REGI_ICON, true);
        IMSLog.i(LOG_TAG, this.mPhoneId, "isRcsIconVisible: " + isRcsIconVisible);
        if (!isRcsIconVisible) {
            visibilities.setShowRcsIcon(false);
        }
        IconVisibility visibility = getRcsIconVisibility(visibilities, registrationStatus);
        if (this.mMno == Mno.CMCC) {
            if (visibility == IconVisibility.SHOW) {
                setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_CMCC, (String) null);
            }
        } else if (Build.IS_DEBUGGABLE) {
            int mSimState = this.mTelephonyManager.getSimState(this.mPhoneId);
            if (mSimState == 0 || mSimState == 1) {
                RcsUtils.DualRcs.refreshDualRcsReg(this.mContext);
            }
            if (this.mPhoneId != 0) {
                i = 0;
            }
            int counterPhoneId = i;
            if (!this.mMno.isEur() || !RcsUtils.DualRcs.isDualRcsSettings()) {
                if (visibility == IconVisibility.SHOW) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME, (String) null);
                } else if (this.mPhoneId != SimUtil.getDefaultPhoneId() && isRcsRegistered(counterPhoneId, isSuspend)) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME, (String) null);
                    visibility = IconVisibility.SHOW;
                }
            } else if (visibility == IconVisibility.SHOW) {
                if (isRcsRegistered(counterPhoneId, isSuspend)) {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[2], (String) null);
                } else {
                    setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[this.mPhoneId], (String) null);
                }
            } else if (isRcsRegistered(counterPhoneId, isSuspend)) {
                setIconSlot(RCS_ICON_SLOT, RCS_ICON_NAME_DUAL[counterPhoneId], (String) null);
                visibility = IconVisibility.SHOW;
            }
        }
        setIconVisibility(RCS_ICON_SLOT, visibility);
    }

    private void updateVoWifiLabel(IconVisiblities visiblities, RegistrationStatus registrationStatus) {
        String voWiFiOperatorLabel = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
        if (visiblities.isShowVowiFiIcon() && !TextUtils.isEmpty(voWiFiOperatorLabel)) {
            fillWifiLabel();
            boolean isSameLabel = checkSameVoWIFILabel();
            int otherSlotIndex = SimUtil.getOppositeSimSlot(this.mPhoneId);
            if (!isSameLabel) {
                showWifiRegistrationSateQuickPanel(this.mPhoneId, registrationStatus.isVowifiRegistered());
            } else if (isVoWIFIRegistered(otherSlotIndex) || registrationStatus.isVowifiRegistered()) {
                showWifiRegistrationSateQuickPanel(-1, true);
            } else {
                showWifiRegistrationSateQuickPanel(-1, false);
            }
        }
    }

    private boolean hasVolteService(ImsRegistration reg) {
        if (this.mMno == Mno.SPRINT) {
            return reg.hasService("mmtel") || reg.hasService("mmtel-video");
        }
        return reg.hasVolteService();
    }

    private void fillWifiLabel() {
        mRegiIndicatorID = "stat_notify_wfc_warning";
        int[] iArr = mVowifiOperatorLabelOngoing;
        int i = this.mPhoneId;
        iArr[i] = ImsRegistry.getInt(i, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL_ONGOING, 0);
        String[] strArr = mWifiSubTextOnLockScreen;
        int i2 = this.mPhoneId;
        strArr[i2] = ImsRegistry.getString(i2, GlobalSettingsConstants.Registration.VOWIFI_SUBTEXT_ON_LOCKSCREEN, "");
        String[] strArr2 = mVowifiOperatorLabel;
        int i3 = this.mPhoneId;
        strArr2[i3] = ImsRegistry.getString(i3, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
        int otherSlotIndex = SimUtil.getOppositeSimSlot(this.mPhoneId);
        mVowifiOperatorLabelOngoing[otherSlotIndex] = ImsRegistry.getInt(otherSlotIndex, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL_ONGOING, 0);
        mWifiSubTextOnLockScreen[otherSlotIndex] = ImsRegistry.getString(otherSlotIndex, GlobalSettingsConstants.Registration.VOWIFI_SUBTEXT_ON_LOCKSCREEN, "");
        mVowifiOperatorLabel[otherSlotIndex] = ImsRegistry.getString(otherSlotIndex, GlobalSettingsConstants.Registration.VOWIFI_OPERATOR_LABEL, "");
    }

    private boolean checkSameVoWIFILabel() {
        int otherSlotIndex = SimUtil.getOppositeSimSlot(this.mPhoneId);
        int[] iArr = mVowifiOperatorLabelOngoing;
        int i = this.mPhoneId;
        if (iArr[i] == iArr[otherSlotIndex]) {
            String[] strArr = mWifiSubTextOnLockScreen;
            if (TextUtils.equals(strArr[i], strArr[otherSlotIndex])) {
                String[] strArr2 = mVowifiOperatorLabel;
                if (TextUtils.equals(strArr2[this.mPhoneId], strArr2[otherSlotIndex])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isVoWIFIRegistered(int phoneId) {
        ImsRegistration[] regArray = this.mRegistrationManager.getRegistrationInfoByPhoneId(phoneId);
        if (regArray != null) {
            int length = regArray.length;
            int i = 0;
            while (i < length) {
                ImsRegistration reg = regArray[i];
                if (!reg.hasVolteService() || reg.getImsProfile().hasEmergencySupport() || ((!isServiceAvailable("mmtel") && !isServiceAvailable("mmtel-video")) || this.mRegistrationManager.getCurrentNetworkByPhoneId(phoneId) != 18 || !this.mPdnController.isEpdgConnected(phoneId))) {
                    i++;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "isVoWIFIRegistered");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRcsRegistered(int phoneId, boolean isSuspend) {
        ImsRegistration[] regArray = this.mRegistrationManager.getRegistrationInfoByPhoneId(phoneId);
        if (regArray != null) {
            for (ImsRegistration reg : regArray) {
                if (reg.hasRcsService() && !isSuspend && ((this.mMno != Mno.CMCC || this.mCurrentServiceState == 0) && (this.mMno != Mno.CMCC || !isOtherSimInCallStatus()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0101, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void showWifiRegistrationSateQuickPanel(int r10, boolean r11) {
        /*
            r9 = this;
            monitor-enter(r9)
            boolean[] r0 = mShowVoWIFILabel     // Catch:{ all -> 0x0102 }
            int r1 = r10 + 1
            boolean r0 = r0[r1]     // Catch:{ all -> 0x0102 }
            if (r0 != r11) goto L_0x0030
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0102 }
            int r1 = r9.mPhoneId     // Catch:{ all -> 0x0102 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0102 }
            r2.<init>()     // Catch:{ all -> 0x0102 }
            java.lang.String r3 = "no need to update mShowVoWIFILabel["
            r2.append(r3)     // Catch:{ all -> 0x0102 }
            r2.append(r10)     // Catch:{ all -> 0x0102 }
            java.lang.String r3 = "]  aready ["
            r2.append(r3)     // Catch:{ all -> 0x0102 }
            r2.append(r11)     // Catch:{ all -> 0x0102 }
            java.lang.String r3 = "]"
            r2.append(r3)     // Catch:{ all -> 0x0102 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0102 }
            com.sec.internal.log.IMSLog.i(r0, r1, r2)     // Catch:{ all -> 0x0102 }
            monitor-exit(r9)
            return
        L_0x0030:
            r0 = -1
            if (r10 != r0) goto L_0x0036
            java.lang.String r1 = "imsicon_channel_both"
            goto L_0x003d
        L_0x0036:
            if (r10 != 0) goto L_0x003b
            java.lang.String r1 = "imsicon_channel_0"
            goto L_0x003d
        L_0x003b:
            java.lang.String r1 = "imsicon_channel_1"
        L_0x003d:
            r2 = -26247(0xffffffffffff9979, float:NaN)
            r3 = 0
            r4 = 1
            if (r10 == r0) goto L_0x0050
            boolean[] r0 = mShowVoWIFILabel     // Catch:{ all -> 0x0102 }
            boolean r0 = r0[r3]     // Catch:{ all -> 0x0102 }
            if (r0 != r4) goto L_0x0050
            android.app.NotificationManager r0 = r9.mNotificationManager     // Catch:{ all -> 0x0102 }
            java.lang.String r5 = "imsicon_channel_both"
            r0.cancel(r5, r2)     // Catch:{ all -> 0x0102 }
        L_0x0050:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0102 }
            int r5 = r9.mPhoneId     // Catch:{ all -> 0x0102 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0102 }
            r6.<init>()     // Catch:{ all -> 0x0102 }
            java.lang.String r7 = "show notification VoWiFi tag["
            r6.append(r7)     // Catch:{ all -> 0x0102 }
            r6.append(r10)     // Catch:{ all -> 0x0102 }
            java.lang.String r7 = "] in quick panel ["
            r6.append(r7)     // Catch:{ all -> 0x0102 }
            r6.append(r11)     // Catch:{ all -> 0x0102 }
            java.lang.String r7 = "]"
            r6.append(r7)     // Catch:{ all -> 0x0102 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0102 }
            com.sec.internal.log.IMSLog.i(r0, r5, r6)     // Catch:{ all -> 0x0102 }
            boolean[] r0 = mShowVoWIFILabel     // Catch:{ all -> 0x0102 }
            int r5 = r10 + 1
            r0[r5] = r11     // Catch:{ all -> 0x0102 }
            if (r10 > 0) goto L_0x0080
            r0 = 0
            goto L_0x0081
        L_0x0080:
            r0 = 1
        L_0x0081:
            android.app.NotificationChannel r5 = new android.app.NotificationChannel     // Catch:{ all -> 0x0102 }
            java.lang.String r6 = "imsicon_channel"
            r7 = 2
            r5.<init>(r6, r1, r7)     // Catch:{ all -> 0x0102 }
            r5.setLockscreenVisibility(r3)     // Catch:{ all -> 0x0102 }
            android.app.NotificationManager r6 = r9.mNotificationManager     // Catch:{ all -> 0x0102 }
            r6.createNotificationChannel(r5)     // Catch:{ all -> 0x0102 }
            if (r11 == 0) goto L_0x00fb
            android.app.Notification$Builder r6 = new android.app.Notification$Builder     // Catch:{ all -> 0x0102 }
            android.content.Context r7 = r9.mContext     // Catch:{ all -> 0x0102 }
            java.lang.String r8 = "imsicon_channel"
            r6.<init>(r7, r8)     // Catch:{ all -> 0x0102 }
            java.lang.String r7 = "drawable"
            java.lang.String r8 = mRegiIndicatorID     // Catch:{ all -> 0x0102 }
            int r7 = r9.getResourceIdByName(r7, r8)     // Catch:{ all -> 0x0102 }
            r6.setSmallIcon(r7)     // Catch:{ all -> 0x0102 }
            java.lang.String[] r7 = mVowifiOperatorLabel     // Catch:{ all -> 0x0102 }
            r7 = r7[r0]     // Catch:{ all -> 0x0102 }
            r6.setContentTitle(r7)     // Catch:{ all -> 0x0102 }
            r7 = 0
            android.app.Notification$Builder r7 = r6.setWhen(r7)     // Catch:{ all -> 0x0102 }
            r7.setShowWhen(r3)     // Catch:{ all -> 0x0102 }
            r6.setAutoCancel(r3)     // Catch:{ all -> 0x0102 }
            java.lang.String[] r3 = mWifiSubTextOnLockScreen     // Catch:{ all -> 0x0102 }
            r3 = r3[r0]     // Catch:{ all -> 0x0102 }
            boolean r3 = android.text.TextUtils.isEmpty(r3)     // Catch:{ all -> 0x0102 }
            if (r3 != 0) goto L_0x00e8
            java.lang.String r3 = "string"
            java.lang.String[] r7 = mWifiSubTextOnLockScreen     // Catch:{ all -> 0x0102 }
            r7 = r7[r0]     // Catch:{ all -> 0x0102 }
            int r3 = r9.getResourceIdByName(r3, r7)     // Catch:{ all -> 0x0102 }
            android.content.Context r7 = r9.mContext     // Catch:{ all -> 0x0102 }
            android.content.res.Resources r7 = r7.getResources()     // Catch:{ all -> 0x0102 }
            java.lang.String r7 = r7.getString(r3)     // Catch:{ all -> 0x0102 }
            r6.setContentText(r7)     // Catch:{ all -> 0x0102 }
            android.app.Notification$BigTextStyle r8 = new android.app.Notification$BigTextStyle     // Catch:{ all -> 0x0102 }
            r8.<init>()     // Catch:{ all -> 0x0102 }
            android.app.Notification$BigTextStyle r8 = r8.bigText(r7)     // Catch:{ all -> 0x0102 }
            r6.setStyle(r8)     // Catch:{ all -> 0x0102 }
        L_0x00e8:
            int[] r3 = mVowifiOperatorLabelOngoing     // Catch:{ all -> 0x0102 }
            r3 = r3[r0]     // Catch:{ all -> 0x0102 }
            if (r3 != r4) goto L_0x00f1
            r6.setOngoing(r4)     // Catch:{ all -> 0x0102 }
        L_0x00f1:
            android.app.Notification r3 = r6.build()     // Catch:{ all -> 0x0102 }
            android.app.NotificationManager r4 = r9.mNotificationManager     // Catch:{ all -> 0x0102 }
            r4.notify(r1, r2, r3)     // Catch:{ all -> 0x0102 }
            goto L_0x0100
        L_0x00fb:
            android.app.NotificationManager r3 = r9.mNotificationManager     // Catch:{ all -> 0x0102 }
            r3.cancel(r1, r2)     // Catch:{ all -> 0x0102 }
        L_0x0100:
            monitor-exit(r9)
            return
        L_0x0102:
            r10 = move-exception
            monitor-exit(r9)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.ImsIconManager.showWifiRegistrationSateQuickPanel(int, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void setIconSlot(String slotName, String resourceName, String description) {
        int resourceId = getResourceIdByName("drawable", resourceName);
        boolean isNeedToUpdate = false;
        if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(slotName)) {
            if (this.mLastVoLTEResourceId != resourceId) {
                this.mLastVoLTEResourceId = resourceId;
                isNeedToUpdate = true;
            }
        } else if (RCS_ICON_SLOT.equalsIgnoreCase(slotName)) {
            description = RCS_ICON_DESCRIPTION;
            isNeedToUpdate = true;
        } else {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str, i, "Wrong slot name: " + slotName);
            return;
        }
        if (isNeedToUpdate || this.mForceRefreshIcon) {
            try {
                ((StatusBarManager) this.mContext.getSystemService("statusbar")).setIcon(slotName, resourceId, 0, description);
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "setIconSlot: " + resourceName + " (id: " + resourceId + ")");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setIconVisibility(String slotName, IconVisibility visible) {
        boolean isNeedToUpdate = false;
        if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(slotName)) {
            if (this.mLastVoLTEVisiblity != visible) {
                this.mLastVoLTEVisiblity = visible;
                isNeedToUpdate = true;
            }
        } else if (RCS_ICON_SLOT.equalsIgnoreCase(slotName)) {
            if (this.mLastRcsVisiblity != visible) {
                this.mLastRcsVisiblity = visible;
                isNeedToUpdate = true;
            }
            if (!isNeedToUpdate && visible != IconVisibility.SHOW && this.mForceRefreshIcon) {
                IMSLog.e(LOG_TAG, this.mPhoneId, "RCS not registered on this SIM. Skip refresh.");
                return;
            }
        } else {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.e(str, i, "Wrong slot name: " + slotName);
            return;
        }
        if (isNeedToUpdate || this.mForceRefreshIcon) {
            try {
                ((StatusBarManager) this.mContext.getSystemService("statusbar")).setIconVisibility(slotName, visible == IconVisibility.SHOW);
                if (this.VOLTE_ICON_SLOT.equalsIgnoreCase(slotName)) {
                    if (!this.mIsFirstVoLTEIconShown && visible == IconVisibility.SHOW) {
                        this.mIsFirstVoLTEIconShown = true;
                        String str2 = LOG_TAG;
                        int i2 = this.mPhoneId;
                        IMSLog.e(str2, i2, "!@Boot: " + "setIconVisibility: " + slotName + ": [" + visible + "]");
                    }
                }
                String str3 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str3, i3, "setIconVisibility: " + slotName + ": [" + visible + "]");
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getResourceIdByName(String type, String name) {
        return this.mContext.getResources().getIdentifier(name, type, this.mPackageName);
    }

    private boolean checkKORVolteIcon() {
        boolean isVolteFeatureEnabled = false;
        for (ImsRegistration reg : this.mRegistrationManager.getRegistrationList().values()) {
            if (reg.hasService("mmtel") && reg.getImsProfile().getCmcType() == 0) {
                int i = this.mCurrentNetworkType;
                if (i == 13 || i == 20) {
                    isVolteFeatureEnabled = true;
                }
            }
        }
        int mSimState = this.mTelephonyManager.getSimState();
        if (mSimState == 0 || mSimState == 1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "SIM state is unknown or absent");
            return false;
        } else if (this.mCurrentNetworkType == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : network is unknown.");
            return false;
        } else if (!"oversea".equals(SemSystemProperties.get(ImsConstants.SystemProperties.CURRENT_PLMN))) {
            return checkKORVolteIconOperatorSpecifics(isVolteFeatureEnabled);
        } else {
            String str = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "checkKORVolteIcon : on roaming. Volte featuremask = " + isVolteFeatureEnabled);
            return isVolteFeatureEnabled;
        }
    }

    private boolean checkKORVolteIconOperatorSpecifics(boolean isVolteFeatureEnabled) {
        if (OmcCode.isKTTOmcCode() && this.mMno == Mno.KT) {
            int voicecallType = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, -1);
            if (voicecallType == -1) {
                if (Extensions.UserHandle.myUserId() != 0) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : Settings not found, return VOLTE_PREFERRED");
                    voicecallType = 0;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : Settings not found");
                    voicecallType = -1;
                }
            }
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "checkKORVolteIcon : KT device and KT sim, ServiceState = " + this.mTelephonyManager.getDataServiceState() + ", voicecall_type = " + voicecallType);
            if (this.mTelephonyManager.getServiceState() == 0 && (voicecallType == 0 || voicecallType == 2)) {
                return true;
            }
            return false;
        } else if (this.mMno == Mno.LGU && !OmcCode.isKorOpenOmcCode()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : SIM card is LGT and device is not KOR open, return false");
            return false;
        } else if (!this.mMno.isKor()) {
            return false;
        } else {
            boolean isHide = false;
            int volteNoti = 1;
            if (this.mMno == Mno.SKT) {
                if (this.mIsSilentRedialInProgress) {
                    isHide = true;
                }
                if (OmcCode.isSKTOmcCode()) {
                    volteNoti = 0;
                    try {
                        volteNoti = Settings.System.getInt(this.mContext.getContentResolver(), "volte_noti_settings");
                    } catch (Settings.SettingNotFoundException e) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "checkKORVolteIcon : volte_noti_settings is not exists");
                    }
                }
            }
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "checkKORVolteIcon : volte_noti_settings = " + volteNoti + ", isVolteFeatureEnabled = " + isVolteFeatureEnabled + ", isHide = " + isHide + ", ServiceState = " + this.mTelephonyManager.getServiceState());
            if (volteNoti != 1 || !isVolteFeatureEnabled || isHide || this.mTelephonyManager.getServiceState() != 0) {
                return false;
            }
            return true;
        }
    }

    private String getVowifiIconName() {
        return ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOWIFI_ICON, "");
    }

    private String getVolteIconName() {
        if (!OmcCode.isKOROmcCode() || !this.mMno.isKor()) {
            String volteIconId = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.VOLTE_ICON, "");
            if (!TextUtils.isEmpty(volteIconId)) {
                return volteIconId;
            }
            return DEFAULT_VOLTE_REGI_ICON_ID;
        } else if (OmcCode.isKorOpenOmcCode()) {
            return "stat_sys_phone_call";
        } else {
            if (OmcCode.isSKTOmcCode()) {
                return CMC_SD_ICON;
            }
            if (OmcCode.isKTTOmcCode()) {
                return "stat_sys_phone_call_kt";
            }
            return "stat_sys_phone_call_lgt";
        }
    }

    private String getDualIMSIconName(Icon select) {
        if (!this.mUseDualVolteIcon) {
            return DEFAULT_VOLTE_REGI_ICON_ID;
        }
        String projection = "";
        int i = AnonymousClass4.$SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon[select.ordinal()];
        if (i == 1) {
            projection = GlobalSettingsConstants.Registration.VOLTE_ICON + Integer.toString(this.mPhoneId + 1);
        } else if (i != 2) {
            Log.i(LOG_TAG, "Wrong select");
        } else {
            projection = GlobalSettingsConstants.Registration.VOWIFI_ICON + Integer.toString(this.mPhoneId + 1);
        }
        String volteIconId = "";
        if (!projection.isEmpty()) {
            volteIconId = ImsRegistry.getString(this.mPhoneId, projection, "");
        }
        return volteIconId;
    }

    /* renamed from: com.sec.internal.ims.core.ImsIconManager$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon;

        static {
            int[] iArr = new int[Icon.values().length];
            $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon = iArr;
            try {
                iArr[Icon.VOLTE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$core$ImsIconManager$Icon[Icon.VOWIFI.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public void registerPhoneStateListener() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerPhoneStateListener:");
        int subId = SimUtil.getSubId(this.mPhoneId);
        if (subId >= 0) {
            if (!SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS()) || this.mPhoneId == SimUtil.getDefaultPhoneId()) {
                this.mPhoneStateManager.registerListener(this.mPhoneStateListener, subId, this.mPhoneId);
                return;
            }
            Log.i(LOG_TAG + "[" + this.mPhoneId + "]", "do not register to non-DDS PhoneStateListener");
        }
    }

    public void unRegisterPhoneStateListener() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "unRegisterPhoneStateListener:");
        this.mPhoneStateManager.unRegisterListener(this.mPhoneId);
    }

    public void setCurrentNetworkType(int networkType) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setCurrentNetworkType:" + networkType);
        this.mCurrentNetworkType = networkType;
    }

    public void setCurrentServiceState(int serviceState) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setCurrentServiceState:" + serviceState);
        this.mCurrentServiceState = serviceState;
    }

    public void setCurrentVoiceRatType(int VoiceRatType) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setCurrentVoiceRatType:" + VoiceRatType);
        this.mCurrentVoiceRatType = VoiceRatType;
    }

    public void setCurrentRoamingState(boolean InRoaming) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setCurrentRoamingState:" + InRoaming);
        this.mCurrentInRoaming = InRoaming;
    }

    private void clearIcon(int phoneId) {
        if (!needShowRcsIcon(phoneId)) {
            setIconVisibility(RCS_ICON_SLOT, IconVisibility.HIDE);
        }
        if (!needShowNoCTCVoLTEIcon()) {
            setIconVisibility(this.VOLTE_ICON_SLOT, IconVisibility.HIDE);
        }
    }

    private boolean isOtherSimInCallStatus() {
        int otherStatus = this.mRegistrationManager.getTelephonyCallStatus(this.mPhoneId == 0 ? 1 : 0);
        if (otherStatus == 2 || otherStatus == 1) {
            return true;
        }
        return false;
    }

    public void updateIconWithDDSChange() {
        if (SimConstants.DSDS_SI_DDS.equals(SimUtil.getConfigDualIMS())) {
            this.mLastVoLTEResourceId = -1;
            ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (sm != null && sm.getRilSimOperator().contains("CTC") && this.mPhoneId != SimUtil.getDefaultPhoneId()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "updateIconWithDDSChange");
                updateRegistrationIcon(false);
            }
        }
    }

    private boolean showDualVolteIcon() {
        boolean supportDualVolte = SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS());
        boolean configESimSlotSwitch = TextUtils.equals("tsds2", SemSystemProperties.get("persist.ril.esim.slotswitch", ""));
        boolean supportESimFloatingFeature = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_COMMON_SUPPORT_EMBEDDED_SIM", false);
        boolean supportESimCscFeature = SemCscFeature.getInstance().getBoolean("CscFeature_RIL_SupportEsim", false);
        String str = LOG_TAG;
        Log.i(str, "supportDualVolte:" + supportDualVolte + ",configESimSlotSwitch:" + configESimSlotSwitch + "ESim Features - floating:" + supportESimFloatingFeature + ", csc:" + supportESimCscFeature);
        if (!supportDualVolte) {
            return false;
        }
        if (!supportESimFloatingFeature || configESimSlotSwitch || supportESimCscFeature) {
            return true;
        }
        return false;
    }

    public void setDuringEmergencyCall(boolean isDuringEmergencyCall) {
        if (this.mMno.isOneOf(Mno.VODAFONE_AUSTRALIA, Mno.APT)) {
            this.mIsDuringEmergencyCall = isDuringEmergencyCall;
            updateRegistrationIcon(false);
        }
    }

    public boolean getDuringEmergencyCall() {
        return this.mIsDuringEmergencyCall;
    }

    private boolean needShowRcsIcon(int phoneId) {
        if (phoneId != SimUtil.getDefaultPhoneId()) {
            if (isRcsRegistered(phoneId == 0 ? 1 : 0, this.mPdnController.isSuspended(this.mConnectivityManager.getNetworkInfo(this.mCurrentNetworkType)))) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "needShowRcsIcon: true");
                return true;
            }
        }
        return false;
    }
}
