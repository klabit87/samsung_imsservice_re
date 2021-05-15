package com.sec.internal.ims.settings;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.ims.extensions.SemEmergencyConstantsExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ImsServiceSwitchEur extends ImsServiceSwitchBase {
    private static final String LOG_TAG = "ImsServiceSwitchEur";
    private BroadcastReceiver mDefaultSmsPackageChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(intent.getAction())) {
                boolean isSmsDefault = ImsServiceSwitchEur.this.isDefaultMessageAppInUse();
                Log.i("ImsServiceSwitchEur[" + ImsServiceSwitchEur.this.mPhoneId + "]", "onChange: RCS DefaultSmsObserver. [" + isSmsDefault + "]");
                SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(ImsServiceSwitchEur.this.mPhoneId, ImsServiceSwitchEur.this.mContext, "imsswitch", 0, false).edit();
                if (isSmsDefault) {
                    ImsServiceSwitchEur.this.mDefaultSms = true;
                    editor.putBoolean("defaultsms", ImsServiceSwitchEur.this.mDefaultSms);
                } else {
                    ImsServiceSwitchEur.this.mDefaultSms = false;
                    editor.putBoolean("defaultsms", ImsServiceSwitchEur.this.mDefaultSms);
                }
                editor.apply();
            }
        }
    };
    private EmergencyEventBroadcastReceiver mEmEventReceiver = null;
    private boolean mEmergencyEnabled = false;
    private boolean mUpsmEnabled = false;

    public ImsServiceSwitchEur(Context context, int phoneId) {
        super(context, phoneId);
        registerDefaultSmsPackageChangeReceiver();
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
        if (!sp.contains("defaultsms") || sp.getBoolean("defaultsms", false)) {
            this.mDefaultSms = true;
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("defaultsms", this.mDefaultSms);
            editor.apply();
        } else {
            this.mDefaultSms = false;
        }
        setEmEventReceiver();
    }

    public void migrationSharedPreferences(SharedPreferences newSp) {
        SharedPreferences.Editor editor = newSp.edit();
        for (Map.Entry<String, ?> entry : ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "imsswitch", 0, false).getAll().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                editor.putInt(entry.getKey(), ((Integer) value).intValue());
            } else if (value instanceof Boolean) {
                editor.putBoolean(entry.getKey(), ((Boolean) value).booleanValue());
            } else {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.logAndAdd("Wrong type: " + entry.getKey());
            }
        }
        editor.apply();
    }

    public void registerDefaultSmsPackageChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        this.mContext.registerReceiver(this.mDefaultSmsPackageChangeReceiver, filter);
    }

    public boolean isRcsEnabled() {
        if (this.isLoded && getDefaultMessageApp() != null) {
            boolean isSmsDefault = isDefaultMessageAppInUse();
            SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
            this.mDefaultSms = isSmsDefault;
            editor.putBoolean("defaultsms", this.mDefaultSms);
            editor.apply();
            this.isLoded = false;
        }
        Log.i("ImsServiceSwitchEur[" + this.mPhoneId + "]", " isRcsEnabled: " + this.mRcsEnabled + " mDefaultSms " + this.mDefaultSms + " mUpsmEnabled " + this.mUpsmEnabled + " mEmergencyEnabled " + this.mEmergencyEnabled);
        if (!this.mRcsEnabled || this.mUpsmEnabled || !this.mDefaultSms || this.mEmergencyEnabled) {
            return false;
        }
        return true;
    }

    private void setEmEventReceiver() {
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "setEmEventReceiver. ");
        if (this.mEmEventReceiver != null) {
            Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "mEmEventReceiver is not null. ");
            return;
        }
        registerEmEventReceiver();
        SemEmergencyManager emergencyManager = SemEmergencyManager.getInstance(this.mContext);
        if (emergencyManager != null && SemEmergencyManager.isEmergencyMode(this.mContext)) {
            if (SystemUtil.checkUltraPowerSavingMode(emergencyManager) || emergencyManager.checkModeType(16)) {
                Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "emergency mode is already set, so send upsm event.");
                onEmergencyModeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void onEmergencyModeChanged() {
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "onEmergencyModeChanged.");
        SemEmergencyManager emergencyManager = SemEmergencyManager.getInstance(this.mContext);
        Log.i("ImsServiceSwitchEur[" + this.mPhoneId + "]", "Emergency is " + SemEmergencyManager.isEmergencyMode(this.mContext));
        Log.i("ImsServiceSwitchEur[" + this.mPhoneId + "]", "UPSM is " + SystemUtil.checkUltraPowerSavingMode(emergencyManager));
        if (!SemEmergencyManager.isEmergencyMode(this.mContext) || !emergencyManager.checkModeType(16)) {
            if (!SemEmergencyManager.isEmergencyMode(this.mContext) || !SystemUtil.checkUltraPowerSavingMode(emergencyManager)) {
                if (this.mEmergencyEnabled) {
                    Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "Emergency mode is disabled.");
                    this.mEmergencyEnabled = false;
                    persist();
                    if (this.mRcsEnabled) {
                        forceNotifyToApp(this.mPhoneId);
                    }
                } else if (this.mUpsmEnabled) {
                    Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "UPSM is disabled.");
                    this.mUpsmEnabled = false;
                    persist();
                    if (this.mRcsEnabled) {
                        forceNotifyToApp(this.mPhoneId);
                    }
                }
            } else if (this.mUpsmEnabled) {
                Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "UPSM is already enabled, so skip.");
            } else {
                this.mUpsmEnabled = true;
                if (this.mRcsEnabled) {
                    Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "UPSM is enabled.");
                    persist();
                    return;
                }
                Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "UPSM is enabled: rcs off, so no change.");
            }
        } else if (this.mEmergencyEnabled) {
            Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "Emergency mode is already enabled, so skip.");
        } else {
            this.mEmergencyEnabled = true;
            if (this.mRcsEnabled) {
                Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "Emergency mode is enabled.");
                persist();
                return;
            }
            Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "Emergency mode is enabled: rcs off, so no change.");
        }
    }

    private void forceNotifyToApp(int phoneId) {
        IRegistrationManager rm = ImsServiceStub.makeImsService(this.mContext).getRegistrationManager();
        if (rm != null) {
            rm.forceNotifyToApp(phoneId);
        }
    }

    private class EmergencyEventBroadcastReceiver extends BroadcastReceiver {
        private EmergencyEventBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            Log.i("ImsServiceSwitchEur[" + ImsServiceSwitchEur.this.mPhoneId + "]", "Received EmEvent: " + intent.getAction() + " extra: " + intent.getExtras());
            ImsServiceSwitchEur.this.onEmergencyModeChanged();
        }
    }

    public boolean isEnabled(String service) {
        if (this.mVolteServiceSwitch.containsKey(service)) {
            if ("ss".equals(service)) {
                if ((this.mSsEnabled || this.mVoLteEnabled) && ((Boolean) this.mVolteServiceSwitch.get(service)).booleanValue()) {
                    return true;
                }
                return false;
            } else if (!this.mVoLteEnabled || !((Boolean) this.mVolteServiceSwitch.get(service)).booleanValue()) {
                return false;
            } else {
                return true;
            }
        } else if (!this.mRcsServiceSwitch.containsKey(service)) {
            return false;
        } else {
            if (!isRcsEnabled() || !((Boolean) this.mRcsServiceSwitch.get(service)).booleanValue()) {
                return false;
            }
            return true;
        }
    }

    private void registerEmEventReceiver() {
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "registerEmEventReceiver. ");
        this.mEmEventReceiver = new EmergencyEventBroadcastReceiver();
        IntentFilter emIntentFilter = new IntentFilter();
        emIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_STATE_CHANGED");
        emIntentFilter.addAction(SemEmergencyConstantsExt.EMERGENCY_CHECK_ABNORMAL_STATE);
        emIntentFilter.addAction("com.samsung.intent.action.EMERGENCY_START_SERVICE_BY_ORDER");
        this.mContext.registerReceiver(this.mEmEventReceiver, emIntentFilter);
    }

    public void unregisterObserver() {
        unregisterEventListener();
        unregisterEmEventReceiver();
    }

    private void unregisterEventListener() {
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "unregisterEventListener. ");
        try {
            this.mContext.unregisterReceiver(this.mDefaultSmsPackageChangeReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "DefaultSmsPackageChangeReceiver is not registered!");
        }
    }

    private void unregisterEmEventReceiver() {
        if (this.mEmEventReceiver == null) {
            Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "mEmEventReceiver is null. ");
            return;
        }
        try {
            this.mContext.unregisterReceiver(this.mEmEventReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "EmEventReceiver is not registered!");
        } catch (Throwable th) {
            this.mEmEventReceiver = null;
            throw th;
        }
        this.mEmEventReceiver = null;
    }

    /* access modifiers changed from: protected */
    public void initVolteServiceSwitch(boolean cscVolteEnabled, boolean cscLteVideoCallEnabled, boolean cscRcsEnabled) {
        this.mVolteServiceSwitch.put("mmtel", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("mmtel-video", Boolean.valueOf(cscLteVideoCallEnabled));
        this.mVolteServiceSwitch.put("smsip", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("ss", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("cdpn", Boolean.valueOf(cscVolteEnabled));
        if (this.mVolteServiceSwitch.values().contains(true)) {
            this.mVoLteEnabled = true;
            this.mSsEnabled = true;
            return;
        }
        this.mVoLteEnabled = false;
        this.mSsEnabled = false;
    }

    /* access modifiers changed from: protected */
    public void initServiceSwitch(String operator, List<String> list, boolean isLabSimCard, boolean isSimLoaded, String mnoname) {
        boolean cscVolteEnabled = true;
        boolean cscLteVideoCallEnabled = true;
        boolean cscRcsEnabled = !DeviceUtil.isTablet();
        ContentValues imsswitch = new ContentValues();
        for (String service : getImsServiceSwitchTable()) {
            if (service.equals(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS) || service.equals(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE)) {
                imsswitch.put(service, Boolean.valueOf(cscRcsEnabled));
            } else {
                imsswitch.put(service, true);
            }
        }
        if (!DeviceUtil.getGcfMode().booleanValue() && !"GCF".equalsIgnoreCase(OmcCode.get()) && !TextUtils.isEmpty(operator) && !isLabSimCard && isSimLoaded && !"45001".equals(operator) && !SimUtil.isSoftphoneEnabled() && ((imsswitch = loadImsSwitchFromJson(mnoname, "")) == null || imsswitch.size() == 0)) {
            cscLteVideoCallEnabled = false;
            cscVolteEnabled = false;
            this.mEventLog.logAndAdd(this.mPhoneId, "init: No ImsSettings in Json for [" + operator + "]. Switch off.");
            cscRcsEnabled = false;
            this.mRcsEnabled = false;
        }
        saveImsSwitch(imsswitch);
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "init: cscVolteEnabled=" + cscVolteEnabled + "operator: " + operator);
        initVolteServiceSwitch(cscVolteEnabled, cscLteVideoCallEnabled, cscRcsEnabled);
        Log.d("ImsServiceSwitchEur[" + this.mPhoneId + "]", "init: rcse=" + cscRcsEnabled);
        initRcsServiceSwitch(cscRcsEnabled);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",INIT SW:");
        String str = "1_";
        sb.append(cscVolteEnabled ? str : "0_");
        sb.append(cscLteVideoCallEnabled ? str : "0_");
        if (!cscRcsEnabled) {
            str = "0_";
        }
        sb.append(str);
        IMSLog.c(LogClass.SWITCH_INIT_DONE, sb.toString());
    }

    /* access modifiers changed from: protected */
    public void load() {
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
        String[] volteServices = ImsProfile.getVoLteServiceList();
        String[] rcsServices = ImsProfile.getRcsServiceList();
        for (String service : volteServices) {
            this.mVolteServiceSwitch.put(service, Boolean.valueOf(sp.getBoolean(service, false)));
        }
        for (String service2 : rcsServices) {
            this.mRcsServiceSwitch.put(service2, Boolean.valueOf(sp.getBoolean(service2, false)));
        }
        this.mVoLteEnabled = sp.getBoolean("volte", false);
        this.mSsEnabled = sp.getBoolean("ss", false);
        if (DeviceUtil.isTablet()) {
            this.mRcsEnabled = false;
        } else {
            this.mRcsEnabled = sp.getBoolean(DeviceConfigManager.RCS, false);
        }
        if (!sp.contains(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
            Log.d(LOG_TAG, "load: new switch chatbot-communication being set to " + this.mRcsEnabled);
            this.mRcsServiceSwitch.put(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, Boolean.valueOf(this.mRcsEnabled));
            persist();
        }
        dumpServiceSwitch();
        IMSLog.c(LogClass.SWITCH_LOAD, this.mPhoneId + ",LOAD:" + getSwitchDump());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007f, code lost:
        if (r9 != false) goto L_0x0084;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateServiceSwitchInternal(android.content.ContentValues r21) {
        /*
            r20 = this;
            r0 = r20
            r1 = r21
            java.lang.String r2 = "enableIms"
            java.lang.String r3 = "enableServiceRcs"
            java.lang.String r4 = "enableServiceRcschat"
            java.lang.String r5 = "enableServiceVolte"
            java.lang.String r6 = "enableServiceVowifi"
            java.lang.String r7 = "enableServiceSmsip"
            java.lang.String r8 = "globalgcenabled"
            r9 = 0
            boolean r8 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r8, r9)
            r10 = 1
            java.lang.Boolean r11 = java.lang.Boolean.valueOf(r10)
            if (r8 == 0) goto L_0x0035
            android.content.ContentValues r12 = new android.content.ContentValues
            r12.<init>()
            java.lang.String r13 = "enableIms"
            r12.put(r13, r11)
            java.lang.String r13 = "enableServiceRcs"
            r12.put(r13, r11)
            java.lang.String r13 = "enableServiceRcschat"
            r12.put(r13, r11)
            r1.putAll(r12)
        L_0x0035:
            boolean r12 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r2, r9)
            boolean r13 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r3, r9)
            if (r12 == 0) goto L_0x0049
            if (r13 == 0) goto L_0x0049
            boolean r14 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r4, r9)
            if (r14 == 0) goto L_0x0049
            r14 = r10
            goto L_0x004a
        L_0x0049:
            r14 = r9
        L_0x004a:
            if (r12 == 0) goto L_0x0054
            boolean r15 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r5, r9)
            if (r15 == 0) goto L_0x0054
            r15 = r10
            goto L_0x0055
        L_0x0054:
            r15 = r9
        L_0x0055:
            if (r12 == 0) goto L_0x0060
            boolean r16 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r6, r9)
            if (r16 == 0) goto L_0x0060
            r16 = r10
            goto L_0x0062
        L_0x0060:
            r16 = r9
        L_0x0062:
            r17 = r16
            if (r12 == 0) goto L_0x006f
            boolean r16 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r1, r7, r9)
            if (r16 == 0) goto L_0x006f
            r16 = r10
            goto L_0x0071
        L_0x006f:
            r16 = r9
        L_0x0071:
            r18 = r16
            if (r12 == 0) goto L_0x00b2
            r20.parseImsSwitch(r21)
            r20.enableCpdnForJansky()
            if (r15 != 0) goto L_0x0082
            r9 = r17
            if (r9 == 0) goto L_0x0093
            goto L_0x0084
        L_0x0082:
            r9 = r17
        L_0x0084:
            java.util.Map r10 = r0.mServiceMap
            java.lang.String r1 = "mmtel"
            r10.put(r1, r11)
            java.util.Map r1 = r0.mServiceMap
            java.lang.String r10 = "ss"
            r1.put(r10, r11)
        L_0x0093:
            int r1 = r0.mPhoneId
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "updateServiceSwitch: isEnableRcs : "
            r10.append(r11)
            r10.append(r13)
            java.lang.String r10 = r10.toString()
            java.lang.String r11 = "ImsServiceSwitchEur"
            com.sec.internal.log.IMSLog.i(r11, r1, r10)
            if (r13 == 0) goto L_0x00b4
            r0.enableRcsSwitch(r14)
            goto L_0x00b4
        L_0x00b2:
            r9 = r17
        L_0x00b4:
            com.sec.internal.helper.SimpleEventLog r1 = r0.mEventLog
            int r10 = r0.mPhoneId
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r19 = r2
            java.lang.String r2 = "updateServiceSwitch: Ims["
            r11.append(r2)
            r11.append(r12)
            java.lang.String r2 = "] Rcs["
            r11.append(r2)
            r11.append(r13)
            java.lang.String r2 = "] RcsChat["
            r11.append(r2)
            r11.append(r14)
            java.lang.String r2 = "] Volte["
            r11.append(r2)
            r11.append(r15)
            java.lang.String r2 = "] Vowifi["
            r11.append(r2)
            r11.append(r9)
            java.lang.String r2 = "] Smsip["
            r11.append(r2)
            r2 = r18
            r11.append(r2)
            r18 = r3
            java.lang.String r3 = "] "
            r11.append(r3)
            java.lang.String r3 = r11.toString()
            r1.logAndAdd(r10, r3)
            if (r15 != 0) goto L_0x0109
            if (r9 != 0) goto L_0x0109
            if (r2 == 0) goto L_0x0107
            goto L_0x0109
        L_0x0107:
            r1 = 0
            goto L_0x010a
        L_0x0109:
            r1 = 1
        L_0x010a:
            r0.mVoLteEnabled = r1
            r0.mRcsEnabled = r13
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsServiceSwitchEur.updateServiceSwitchInternal(android.content.ContentValues):void");
    }

    /* access modifiers changed from: protected */
    public ContentValues updateRcsSwitchForEur(ContentValues imsSwitch) {
        String salesCode = OmcCode.get();
        String nwCode = OmcCode.getNWCode(this.mPhoneId);
        boolean z = false;
        String[] carrier_list = GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getStringArray(GlobalSettingsConstants.RCS.RCS_CARRIER_LIST, new String[0]);
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        IMSLog.i(LOG_TAG, this.mPhoneId, "salesCode = " + salesCode + " nwCode = " + nwCode);
        if (!Arrays.asList(carrier_list).contains(salesCode)) {
            if (!salesCode.equals(nwCode)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "not support sim mobility");
                editor.putBoolean(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false);
                editor.putBoolean(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false);
                imsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false);
                imsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false);
            } else {
                boolean imsSwitchFromJson = CollectionUtils.getBooleanValue(imsSwitch, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false);
                if (GlobalSettingsManager.getInstance(this.mContext, this.mPhoneId).getBoolean(GlobalSettingsConstants.RCS.RCS_OPEN_SWITCH_FOR_EUR, true) && imsSwitchFromJson) {
                    z = true;
                }
                boolean isEnableRcs = z;
                IMSLog.i(LOG_TAG, this.mPhoneId, "imsSwitchFromJson [" + imsSwitchFromJson + "], RCS_OPEN_SWITCH_FOR_EUR [" + isEnableRcs + "]");
                editor.putBoolean(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, isEnableRcs);
                editor.putBoolean(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, isEnableRcs);
                imsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, Boolean.valueOf(isEnableRcs));
                imsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, Boolean.valueOf(isEnableRcs));
            }
        }
        editor.apply();
        return imsSwitch;
    }

    /* access modifiers changed from: protected */
    public ContentValues loadImsSwitchFromJson(String mnoname, String mvnoname) {
        int i = this.mPhoneId;
        IMSLog.d(LOG_TAG, i, "loadImsSwitchFromJson: mnoname=" + mnoname + ",  mvnoname=" + mvnoname);
        ContentValues imsSwitch = new ContentValues();
        if (TextUtils.isEmpty(mnoname)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: loadImsSwitchFromJson is not identified.");
            return imsSwitch;
        }
        JsonElement element = ImsServiceSwitchLoader.getImsSwitchFromJson(this.mContext, mnoname, this.mPhoneId);
        if (element.isJsonNull()) {
            return imsSwitch;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement defaultElement = object.get("defaultswitch");
        if (defaultElement.isJsonNull()) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: No default setting.");
            return imsSwitch;
        }
        JsonElement matchElement = ImsServiceSwitchLoader.getMatchedJsonElement(object, mnoname, mvnoname, this.mPhoneId);
        if (matchElement.isJsonNull()) {
            JsonElement matchObj = new JsonObject();
            matchObj.addProperty("mnoname", mnoname);
            matchElement = matchObj;
        }
        if (DeviceUtil.isTablet() && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 30) {
            matchElement = applyTabletPolicy(defaultElement, matchElement);
        }
        if (this.mAutoUpdate != null) {
            matchElement = this.mAutoUpdate.getUpdatedImsSwitch(matchElement);
        }
        JsonElement matchElement2 = JsonUtil.merge(defaultElement, matchElement);
        if (!JsonUtil.isValidJsonElement(matchElement2)) {
            return imsSwitch;
        }
        for (Map.Entry<String, JsonElement> e : matchElement2.getAsJsonObject().entrySet()) {
            imsSwitch.put(e.getKey(), e.getValue().getAsString());
        }
        if (imsSwitch.size() > 0) {
            return updateRcsSwitchForEur(imsSwitch);
        }
        return imsSwitch;
    }

    private JsonElement applyTabletPolicy(JsonElement defaultSwitchElement, JsonElement matchedSwitchElement) {
        if (!JsonUtil.isValidJsonElement(matchedSwitchElement)) {
            Log.d(LOG_TAG, "Not a valid matchedSwitchElement.");
            return matchedSwitchElement;
        }
        if (!Mno.fromName(matchedSwitchElement.getAsJsonObject().get("mnoname").getAsString()).isOneOf(Mno.RJIL, Mno.VODAFONE_INDIA, Mno.AIRTEL, Mno.IDEA_INDIA, Mno.TELSTRA, Mno.OPTUS, Mno.VODAFONE_AUSTRALIA, Mno.TWO_DEGREE, Mno.VODAFONE_NEWZEALAND, Mno.SPARK, Mno.CHT, Mno.APT, Mno.TSTAR, Mno.FET, Mno.TWM, Mno.DLOG, Mno.MOBITEL_LK, Mno.OOREDOO_MV, Mno.ROBI)) {
            return JsonUtil.merge(matchedSwitchElement, defaultSwitchElement);
        }
        Log.i(LOG_TAG, "support ImsService in Tablet");
        return matchedSwitchElement;
    }
}
