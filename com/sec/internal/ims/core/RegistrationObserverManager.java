package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.List;

public class RegistrationObserverManager {
    private static final int EVENT_AIRPLANE_MODE = 3;
    private static final int EVENT_CARRIER_FEATURE_UPDATED = 32;
    private static final int EVENT_DATA_ROAMING = 2;
    private static final int EVENT_DOWNLOAD_CONFIG = 21;
    private static final int EVENT_IMS_DM_CONFIG = 9;
    private static final int EVENT_IMS_GLOBAL_SETTING = 6;
    private static final int EVENT_IMS_PROFILE_SETTING = 7;
    private static final int EVENT_LOCATION_MODE = 14;
    private static final int EVENT_LTE_DATA_NETWORK_MODE = 12;
    private static final int EVENT_LTE_ROAMING = 15;
    private static final int EVENT_MNOMAP_UPDATED = 29;
    private static final int EVENT_MOBILE_DATA = 4;
    private static final int EVENT_MOBILE_DATA_PRESSED = 5;
    private static final int EVENT_RCS_USER_SETTING_SLOT1 = 30;
    private static final int EVENT_RCS_USER_SETTING_SLOT2 = 31;
    private static final int EVENT_RESET_DOWNLOAD_CONFIG = 22;
    private static final int EVENT_SIM_MOBILITY = 23;
    private static final int EVENT_VILTE_SLOT1 = 1;
    private static final int EVENT_VILTE_SLOT2 = 18;
    private static final int EVENT_VOLTE_ROAMING = 11;
    private static final int EVENT_VOLTE_SLOT1 = 0;
    private static final int EVENT_VOLTE_SLOT2 = 17;
    private static final int EVENT_VOWIFI_SLOT1 = 10;
    private static final int EVENT_VOWIFI_SLOT2 = 19;
    private static final String LOG_TAG = "RegiObsMgr";
    private static final String SILENT_LOG_CHANGED_ACTION = "com.sec.android.app.servicemodeapp.SILENT_LOG_CHANGED";
    /* access modifiers changed from: private */
    public static UriMatcher sUriMatcher;
    private ChatbotAgreementObserver mChatbotAgreementObserver = null;
    private CompleteSetupWizardObserver mCompleteSetupWizardObserver = null;
    protected Context mContext;
    protected Handler mHandler = null;
    protected HandlerThread mHandlerThread = null;
    private ContentObserver mImsServiceSwitchObserver = null;
    private RegContentObserver mRegContentObserver;
    protected RegistrationManagerBase mRegMan;
    protected IRegistrationHandlerNotifiable mRegManHandler;
    private BroadcastReceiver mSilentLogReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean onoff = intent.getBooleanExtra("onoff", false);
            Log.i(RegistrationObserverManager.LOG_TAG, "silentLog is changed " + onoff);
            Debug.setSilentLogEnabled();
            RegistrationObserverManager.this.mRegMan.setSilentLogEnabled(onoff);
        }
    };
    protected List<ISimManager> mSimManagers;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        ImsConstants.SystemSettings.addUri(uriMatcher, ImsConstants.SystemSettings.VOLTE_SLOT1, 0);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.VOLTE_SLOT2, 17);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.VILTE_SLOT1, 1);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.VILTE_SLOT2, 18);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.DATA_ROAMING, 2);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.AIRPLANE_MODE, 3);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.MOBILE_DATA, 4);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.MOBILE_DATA_PRESSED, 5);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.LOCATION_MODE, 14);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.VOLTE_ROAMING, 11);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.LTE_DATA_MODE, 12);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.LTE_DATA_ROAMING, 15);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_GLOBAL, 6);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_PROFILES, 7);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_SIM_MOBILITY, 23);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_NV_STORAGE, 9);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_DM_CONFIG, 9);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_ENABLE1, 10);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_ENABLE2, 19);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_PREFERRED1, 10);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_PREFERRED2, 19);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_WHEN_ROAMING1, 10);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.WIFI_CALL_WHEN_ROAMING2, 19);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_DOWNLOAD_CONFIG, 21);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.IMS_RESET_DOWNLOAD_CONFIG, 22);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.MNOMAP_UPDATED, 29);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.CARRIER_FEATURE_UPDATED, 32);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.RCS_USER_SETTING1, 30);
        ImsConstants.SystemSettings.addUri(sUriMatcher, ImsConstants.SystemSettings.RCS_USER_SETTING2, 31);
    }

    RegistrationObserverManager(Context ctx, List<ISimManager> simManagers) {
        this.mContext = ctx;
        this.mSimManagers = simManagers;
    }

    RegistrationObserverManager(Context context, RegistrationManagerBase regMan, List<ISimManager> simManagers, RegistrationManagerHandler regManHandler) {
        this.mContext = context;
        this.mRegMan = regMan;
        this.mSimManagers = simManagers;
        this.mRegManHandler = regManHandler;
    }

    public void init() {
        HandlerThread handlerThread = new HandlerThread("RegistrationObserverManager");
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mRegContentObserver = new RegContentObserver(this.mHandler);
        this.mImsServiceSwitchObserver = new ImsServiceSwitchObserver(this.mHandler);
        this.mChatbotAgreementObserver = new ChatbotAgreementObserver(this.mHandler);
        this.mCompleteSetupWizardObserver = new CompleteSetupWizardObserver(this.mHandler);
        registerSilentLogIntentReceiver();
        registerObservers();
    }

    /* access modifiers changed from: private */
    public void onRcsUserSettingChanged(int phoneId) {
        int rcsUserSetting = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 1, phoneId);
        Log.i(LOG_TAG, "onRcsUserSettingChanged rcsUserSetting:" + rcsUserSetting);
        this.mRegManHandler.notifyRcsUserSettingChanged(rcsUserSetting, phoneId);
    }

    /* access modifiers changed from: private */
    public void onVolteSettingChanged(int phoneId) {
        int voiceType = ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, phoneId);
        if (voiceType == -1) {
            IMSLog.i(LOG_TAG, phoneId, "onVolteSettingChanged : unknown");
            return;
        }
        ISimManager simManager = this.mSimManagers.get(phoneId);
        if (simManager != null) {
            boolean isVoLteOn = voiceType == 0;
            StringBuilder sb = new StringBuilder();
            sb.append("onVolteSettingChanged : ");
            sb.append(isVoLteOn ? "VOLTE" : "CS");
            IMSLog.i(LOG_TAG, phoneId, sb.toString());
            this.mRegManHandler.notifyVolteSettingChanged(isVoLteOn, false, phoneId);
            Mno mno = simManager.getSimMno();
            if (mno == Mno.TELEFONICA_UK || mno == Mno.TELEFONICA_UK_LAB) {
                String imsi = simManager.getImsi();
                if (!TextUtils.isEmpty(imsi)) {
                    String prefName = ImsSharedPrefHelper.USER_CONFIG;
                    String lastVolteSwitch = "last_volte_switch_" + imsi;
                    if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 28) {
                        prefName = "imsswitch";
                        lastVolteSwitch = "last_volte_switch_" + imsi.substring(5);
                    }
                    if (SlotBasedConfig.getInstance(phoneId).getEntitlementNsds()) {
                        ImsSharedPrefHelper.save(phoneId, this.mContext, prefName, lastVolteSwitch, voiceType);
                        IMSLog.i(LOG_TAG, phoneId, "set lastVoLTESwitch: " + voiceType);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onVilteSettingChanged(int phoneId) {
        int videoType = ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, phoneId);
        if (videoType == -1) {
            IMSLog.i(LOG_TAG, phoneId, "onVilteSettingChanged : unknown");
            return;
        }
        boolean isVideoCallOn = videoType == 0;
        IMSLog.i(LOG_TAG, phoneId, "onVilteSettingChanged : " + isVideoCallOn);
        this.mRegManHandler.notifyVolteSettingChanged(isVideoCallOn, true, phoneId);
    }

    /* access modifiers changed from: private */
    public void onVolteRoamingSettingChanged(int phoneId) {
        ISimManager sm = this.mSimManagers.get(phoneId);
        if (sm != null) {
            Mno mno = sm.getSimMno();
            int voiceType = ImsConstants.SystemSettings.VOLTE_ROAMING.get(this.mContext, ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED);
            StringBuilder sb = new StringBuilder();
            sb.append("onVolteRoamingSettingChanged: now [");
            sb.append(voiceType == ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED ? "VOLTE" : "CS");
            sb.append("]");
            Log.i(LOG_TAG, sb.toString());
            boolean z = true;
            if (mno == Mno.KDDI) {
                IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable = this.mRegManHandler;
                if (voiceType != ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED) {
                    z = false;
                }
                iRegistrationHandlerNotifiable.notifyVolteSettingChanged(z, false, phoneId);
                return;
            }
            IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable2 = this.mRegManHandler;
            if (voiceType != ImsConstants.SystemSettings.VOLTE_ROAMING_ENABLED) {
                z = false;
            }
            iRegistrationHandlerNotifiable2.notifyVolteRoamingSettingChanged(z, phoneId);
        }
    }

    /* access modifiers changed from: private */
    public void onDataRoamingSettingChanged(int phoneId) {
        int roamingDataOn = ImsConstants.SystemSettings.DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.LTE_DATA_ROAMING_DISABLED);
        StringBuilder sb = new StringBuilder();
        sb.append("onDataRoamingSettingChanged: now [");
        boolean z = true;
        if (roamingDataOn != 1) {
            z = false;
        }
        sb.append(z);
        sb.append("]");
        Log.i(LOG_TAG, sb.toString());
        this.mRegManHandler.notifyRoamingDataSettigChanged(roamingDataOn, phoneId);
    }

    /* access modifiers changed from: private */
    public void onAirplaneModeSettingChanged() {
        boolean z = false;
        int airPlaneModeOn = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        StringBuilder sb = new StringBuilder();
        sb.append("onAirplaneModeSettingChanged: now [");
        if (airPlaneModeOn == 1) {
            z = true;
        }
        sb.append(z);
        sb.append("]");
        Log.i(LOG_TAG, sb.toString());
        this.mRegManHandler.notifyAirplaneModeChanged(airPlaneModeOn);
    }

    /* access modifiers changed from: private */
    public void onMobileDataSettingChanged(int phoneId) {
        boolean z = true;
        int mobileDataOn = Settings.Global.getInt(this.mContext.getContentResolver(), Extensions.Settings.Global.MOBILE_DATA, 1);
        StringBuilder sb = new StringBuilder();
        sb.append("onMobileDataSettingChanged: now [");
        if (mobileDataOn != 1) {
            z = false;
        }
        sb.append(z);
        sb.append("]");
        Log.i(LOG_TAG, sb.toString());
        this.mRegManHandler.notifyMobileDataSettingeChanged(mobileDataOn, phoneId);
    }

    /* access modifiers changed from: private */
    public void onMobileDataPressedSettingChanged(int phoneId) {
        boolean z = true;
        int mobileDataPressed = Settings.Global.getInt(this.mContext.getContentResolver(), ImsConstants.SystemSettings.MOBILE_DATA_PRESSED.getName(), 1);
        StringBuilder sb = new StringBuilder();
        sb.append("onMobileDataPressedSettingChanged: now [");
        if (mobileDataPressed != 1) {
            z = false;
        }
        sb.append(z);
        sb.append("]");
        Log.i(LOG_TAG, sb.toString());
        this.mRegManHandler.notifyMobileDataPressedSettingeChanged(mobileDataPressed, phoneId);
    }

    /* access modifiers changed from: private */
    public void onImsSettingsChanged(Uri uri, int phoneId) {
        Log.i(LOG_TAG, "onImsSettingsChanged, phoneId: " + phoneId);
        this.mRegManHandler.notifyImsSettingChanged(uri, phoneId);
    }

    /* access modifiers changed from: private */
    public void onMnoMapUpdated(Uri uri, int phoneId) {
        Log.i(LOG_TAG, "onMnoMapUpdated, phoneId: " + phoneId);
        this.mRegManHandler.notifyMnoMapUpdated(uri, phoneId);
    }

    /* access modifiers changed from: private */
    public void onImsDmConfigChanged(Uri uri, int phoneId) {
        Log.i(LOG_TAG, "onImsDmConfigChanged, phoneId: " + phoneId);
        this.mRegManHandler.notifyConfigChanged(uri, phoneId);
    }

    /* access modifiers changed from: private */
    public void onVoWiFiSettingsChanged(int phoneId) {
        Log.i(LOG_TAG, "onVoWiFiSettingsChanged:");
        this.mRegManHandler.notifyVowifiSettingChanged(phoneId, 0);
    }

    /* access modifiers changed from: private */
    public void onLteDataNetworkModeSettingChanged(int phoneId) {
        Log.i(LOG_TAG, "onLteDataNetworkModeSettingChaged:");
        this.mRegManHandler.notifyLteDataNetworkModeSettingChanged(Settings.Secure.getInt(this.mContext.getContentResolver(), ImsConstants.SystemSettings.LTE_DATA_MODE.getName(), ImsConstants.SystemSettings.LTE_DATA_NETWORK_MODE_ENABLED) == ImsConstants.SystemSettings.LTE_DATA_NETWORK_MODE_ENABLED, phoneId);
    }

    /* access modifiers changed from: private */
    public void onLocationModeChanged(int phoneId) {
        Log.i(LOG_TAG, "onLocationModeChanged:");
        ISimManager simManager = this.mSimManagers.get(phoneId);
        if (simManager != null && simManager.getSimMno() == Mno.SPRINT) {
            int locationMode = Settings.Secure.getInt(this.mContext.getContentResolver(), "location_mode", 0);
            Log.i(LOG_TAG, "onLocationModeChanged: locationMode = " + locationMode);
            if (locationMode != 0) {
                this.mRegManHandler.notifyLocationModeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onLteRoamingSettingChanged() {
        int roamingLteOn = ImsConstants.SystemSettings.LTE_DATA_ROAMING.get(this.mContext, ImsConstants.SystemSettings.LTE_DATA_ROAMING_DISABLED);
        StringBuilder sb = new StringBuilder();
        sb.append("onLteRoamingSettingChanged: now [");
        boolean z = false;
        sb.append(roamingLteOn == 1);
        sb.append("]");
        Log.i(LOG_TAG, sb.toString());
        IRegistrationHandlerNotifiable iRegistrationHandlerNotifiable = this.mRegManHandler;
        if (roamingLteOn == 1) {
            z = true;
        }
        iRegistrationHandlerNotifiable.notifyRoamingLteSettigChanged(z);
    }

    private void registerObservers(Uri uri, boolean notifyForDescendants, ContentObserver observer) {
        Uri validUri = Uri.parse(uri.toString().replaceFirst("/\\*$", ""));
        if (!uri.equals(validUri)) {
            Log.i(LOG_TAG, "registerObservers: validateUri [" + uri + "] -> [" + validUri + "]");
            notifyForDescendants = true;
        }
        try {
            this.mContext.getContentResolver().registerContentObserver(validUri, notifyForDescendants, observer);
        } catch (SQLiteFullException | SecurityException e) {
            SimpleEventLog eventLog = this.mRegMan.getEventLog();
            eventLog.logAndAdd("registerObservers() : " + validUri + " failed!");
            e.printStackTrace();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x010c A[EDGE_INSN: B:35:0x010c->B:12:0x010c ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x00ee  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void registerObservers() throws android.database.sqlite.SQLiteFullException {
        /*
            r6 = this;
            java.lang.String r0 = "RegiObsMgr"
            java.lang.String r1 = "registerObservers:"
            android.util.Log.i(r0, r1)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r1 = r6.mSimManagers
            java.util.Iterator r1 = r1.iterator()
        L_0x0013:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x0027
            java.lang.Object r2 = r1.next()
            com.sec.internal.interfaces.ims.core.ISimManager r2 = (com.sec.internal.interfaces.ims.core.ISimManager) r2
            com.sec.internal.constants.Mno r3 = r2.getSimMno()
            r0.add(r3)
            goto L_0x0013
        L_0x0027:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_SLOT1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r3 = 0
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_SLOT2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.RCS_USER_SETTING1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.RCS_USER_SETTING2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VILTE_SLOT1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VILTE_SLOT2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.DATA_ROAMING
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.AIRPLANE_MODE
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.MOBILE_DATA
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.MOBILE_DATA_PRESSED
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_SWITCHES
            android.net.Uri r1 = r1.getUri()
            android.database.ContentObserver r2 = r6.mImsServiceSwitchObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_SETTING
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_GLOBAL
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r4 = 1
            r6.registerObservers(r1, r4, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_PROFILES
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r4, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_SIM_MOBILITY
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r4, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_NV_STORAGE
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r4, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_DM_CONFIG
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r4, r2)
            java.util.Iterator r1 = r0.iterator()
        L_0x00e8:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x010c
            java.lang.Object r2 = r1.next()
            com.sec.internal.constants.Mno r2 = (com.sec.internal.constants.Mno) r2
            boolean r5 = r2.isKor()
            if (r5 != 0) goto L_0x0100
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.KDDI
            if (r2 != r5) goto L_0x00ff
            goto L_0x0100
        L_0x00ff:
            goto L_0x00e8
        L_0x0100:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_ROAMING
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r5 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r5)
        L_0x010c:
            java.util.Iterator r1 = r0.iterator()
        L_0x0110:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x012d
            java.lang.Object r2 = r1.next()
            com.sec.internal.constants.Mno r2 = (com.sec.internal.constants.Mno) r2
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.SPRINT
            if (r2 != r5) goto L_0x012c
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.LOCATION_MODE
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r5 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r5)
            goto L_0x012d
        L_0x012c:
            goto L_0x0110
        L_0x012d:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.LTE_DATA_MODE
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_ENABLE1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_ENABLE2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_WHEN_ROAMING1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_WHEN_ROAMING2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_PREFERRED1
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.WIFI_CALL_PREFERRED2
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.PREFFERED_VOICE_CALL
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r4, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.PREFFERED_NETWORK_MODE
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            java.util.Iterator r1 = r0.iterator()
        L_0x0194:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x01b3
            java.lang.Object r2 = r1.next()
            com.sec.internal.constants.Mno r2 = (com.sec.internal.constants.Mno) r2
            boolean r4 = r2.isKor()
            if (r4 == 0) goto L_0x01b2
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.LTE_DATA_ROAMING
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r4 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r4)
            goto L_0x01b3
        L_0x01b2:
            goto L_0x0194
        L_0x01b3:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_DOWNLOAD_CONFIG
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.IMS_RESET_DOWNLOAD_CONFIG
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.CARRIER_FEATURE_UPDATED
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.MNOMAP_UPDATED
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$RegContentObserver r2 = r6.mRegContentObserver
            r6.registerObservers(r1, r3, r2)
            java.util.Iterator r1 = r0.iterator()
        L_0x01e3:
            boolean r2 = r1.hasNext()
            if (r2 == 0) goto L_0x01fe
            java.lang.Object r2 = r1.next()
            com.sec.internal.constants.Mno r2 = (com.sec.internal.constants.Mno) r2
            boolean r4 = r2.isKor()
            if (r4 == 0) goto L_0x01fd
            android.net.Uri r1 = com.sec.internal.constants.ims.ImsConstants.Uris.MMS_PREFERENCE_PROVIDER_KEY_URI
            com.sec.internal.ims.core.RegistrationObserverManager$ChatbotAgreementObserver r4 = r6.mChatbotAgreementObserver
            r6.registerObservers(r1, r3, r4)
            goto L_0x01fe
        L_0x01fd:
            goto L_0x01e3
        L_0x01fe:
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.SETUP_WIZARD
            android.net.Uri r1 = r1.getUri()
            com.sec.internal.ims.core.RegistrationObserverManager$CompleteSetupWizardObserver r2 = r6.mCompleteSetupWizardObserver
            r6.registerObservers(r1, r3, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationObserverManager.registerObservers():void");
    }

    private void registerSilentLogIntentReceiver() {
        Log.i(LOG_TAG, "registerSilentLogIntentReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SILENT_LOG_CHANGED_ACTION);
        this.mContext.registerReceiver(this.mSilentLogReceiver, filter);
    }

    class ImsServiceSwitchObserver extends ContentObserver {
        public ImsServiceSwitchObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            Log.i(RegistrationObserverManager.LOG_TAG, "ImsServiceSwitch updated.");
            if (uri != null) {
                RegistrationObserverManager.this.mRegManHandler.notifyImsSettingUpdated(UriUtil.getSimSlotFromUri(uri));
            }
        }
    }

    class RegContentObserver extends ContentObserver {
        public RegContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            int match = RegistrationObserverManager.sUriMatcher.match(uri);
            Log.e(RegistrationObserverManager.LOG_TAG, "onChange: " + uri + " => match [" + match + "]");
            int phoneId = SimUtil.getDefaultPhoneId();
            if (uri.getFragment() != null && uri.getFragment().contains(ImsConstants.Uris.FRAGMENT_SIM_SLOT)) {
                phoneId = Character.getNumericValue(uri.getFragment().charAt(7));
                Log.i(RegistrationObserverManager.LOG_TAG, "query : Exist simslot on uri: " + phoneId);
            }
            switch (match) {
                case 0:
                case 17:
                    RegistrationObserverManager.this.onVolteSettingChanged(match == 0 ? ImsConstants.Phone.SLOT_1 : ImsConstants.Phone.SLOT_2);
                    return;
                case 1:
                case 18:
                    RegistrationObserverManager.this.onVilteSettingChanged(match == 1 ? ImsConstants.Phone.SLOT_1 : ImsConstants.Phone.SLOT_2);
                    return;
                case 2:
                    RegistrationObserverManager.this.onDataRoamingSettingChanged(phoneId);
                    return;
                case 3:
                    RegistrationObserverManager.this.onAirplaneModeSettingChanged();
                    return;
                case 4:
                    RegistrationObserverManager.this.onMobileDataSettingChanged(phoneId);
                    return;
                case 5:
                    RegistrationObserverManager.this.onMobileDataPressedSettingChanged(phoneId);
                    return;
                case 6:
                case 7:
                case 21:
                case 22:
                case 23:
                case 32:
                    RegistrationObserverManager.this.onImsSettingsChanged(uri, phoneId);
                    return;
                case 9:
                    RegistrationObserverManager.this.onImsDmConfigChanged(uri, phoneId);
                    return;
                case 10:
                case 19:
                    RegistrationObserverManager.this.onVoWiFiSettingsChanged(match == 10 ? ImsConstants.Phone.SLOT_1 : ImsConstants.Phone.SLOT_2);
                    return;
                case 11:
                    RegistrationObserverManager.this.onVolteRoamingSettingChanged(phoneId);
                    return;
                case 12:
                    RegistrationObserverManager.this.onLteDataNetworkModeSettingChanged(phoneId);
                    return;
                case 14:
                    RegistrationObserverManager.this.onLocationModeChanged(phoneId);
                    return;
                case 15:
                    RegistrationObserverManager.this.onLteRoamingSettingChanged();
                    return;
                case 29:
                    RegistrationObserverManager.this.onMnoMapUpdated(uri, phoneId);
                    return;
                case 30:
                case 31:
                    RegistrationObserverManager.this.onRcsUserSettingChanged(match == 30 ? ImsConstants.Phone.SLOT_1 : ImsConstants.Phone.SLOT_2);
                    return;
                default:
                    return;
            }
        }
    }

    class ChatbotAgreementObserver extends ContentObserver {
        public ChatbotAgreementObserver(Handler handler) {
            super(handler);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:2:0x000c, code lost:
            r0 = com.sec.internal.helper.UriUtil.getSimSlotFromUri(r6);
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onChange(boolean r5, android.net.Uri r6) {
            /*
                r4 = this;
                super.onChange(r5)
                java.lang.String r0 = "RegiObsMgr"
                java.lang.String r1 = "ChatbotAgreementObserver onChange"
                android.util.Log.i(r0, r1)
                if (r6 == 0) goto L_0x002e
                int r0 = com.sec.internal.helper.UriUtil.getSimSlotFromUri(r6)
                com.sec.internal.ims.core.RegistrationObserverManager r1 = com.sec.internal.ims.core.RegistrationObserverManager.this
                java.util.List<com.sec.internal.interfaces.ims.core.ISimManager> r1 = r1.mSimManagers
                java.lang.Object r1 = r1.get(r0)
                com.sec.internal.interfaces.ims.core.ISimManager r1 = (com.sec.internal.interfaces.ims.core.ISimManager) r1
                if (r1 != 0) goto L_0x001d
                return
            L_0x001d:
                com.sec.internal.constants.Mno r2 = r1.getSimMno()
                boolean r3 = r2.isKor()
                if (r3 == 0) goto L_0x002e
                com.sec.internal.ims.core.RegistrationObserverManager r3 = com.sec.internal.ims.core.RegistrationObserverManager.this
                com.sec.internal.interfaces.ims.core.IRegistrationHandlerNotifiable r3 = r3.mRegManHandler
                r3.notifyChatbotAgreementChanged(r0)
            L_0x002e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.RegistrationObserverManager.ChatbotAgreementObserver.onChange(boolean, android.net.Uri):void");
        }
    }

    class CompleteSetupWizardObserver extends ContentObserver {
        public CompleteSetupWizardObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            boolean z = false;
            if (Settings.Secure.getInt(RegistrationObserverManager.this.mContext.getContentResolver(), "user_setup_complete", 0) == 1) {
                z = true;
            }
            boolean isSetupWizardCompleted = z;
            Log.i(RegistrationObserverManager.LOG_TAG, "CompleteSetupWizard updated : " + isSetupWizardCompleted);
            if (uri != null && isSetupWizardCompleted) {
                RegistrationObserverManager.this.mRegManHandler.notifySetupWizardCompleted();
            }
        }
    }
}
