package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.ImsCscFeature;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImsServiceSwitchBase extends ImsServiceSwitch {
    private final String LOG_TAG = ImsServiceSwitchBase.class.getSimpleName();
    protected ImsAutoUpdate mAutoUpdate;
    protected Map<String, Boolean> mServiceMap = new HashMap();

    public ImsServiceSwitchBase(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "created");
        Context context2 = this.mContext;
        StringBuilder sb = new StringBuilder();
        sb.append(this.LOG_TAG);
        sb.append(phoneId);
        this.mEventLog = new SimpleEventLog(context2, sb.toString(), 200);
        this.mAutoUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId);
        initSwitchPref(isCscFeatureChanged());
        migrationCountryCode();
        dumpServiceSwitch();
    }

    public boolean isCscFeatureChanged() {
        String str;
        SharedPreferences spFeature = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_FEATURE, 0, false);
        if (!spFeature.contains("volte")) {
            this.mEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] isCscFeatureChanged: Making new shared preference.");
            updateCscFeature(spFeature, 1, 1, 1);
            return true;
        }
        int spVolte = spFeature.getInt("volte", -1);
        int spRcs = spFeature.getInt(DeviceConfigManager.RCS, -1);
        int spVideo = spFeature.getInt("videocall", -1);
        String str2 = this.LOG_TAG + "[" + this.mPhoneId + "]";
        StringBuilder sb = new StringBuilder();
        sb.append("VoLTE: SP[");
        String str3 = "ON";
        sb.append(spVolte == 1 ? str3 : "OFF");
        sb.append("] Video: SP[");
        if (spVideo == 1) {
            str = str3;
        } else {
            str = "OFF";
        }
        sb.append(str);
        sb.append("] RCS: SP[");
        if (spRcs != 1) {
            str3 = "OFF";
        }
        sb.append(str3);
        sb.append("] ");
        IMSLog.i(str2, sb.toString());
        if (spVolte == 1 && spRcs == 1 && 1 == spVideo) {
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "isCscFeatureChanged : false");
            return false;
        }
        updateCscFeature(spFeature, 1, 1, 1);
        return true;
    }

    /* access modifiers changed from: protected */
    public void updateCscFeature(SharedPreferences sp, int volte, int rcs, int videoCall) {
        SharedPreferences.Editor editor = sp.edit();
        if (editor != null) {
            editor.putInt("volte", volte);
            editor.putInt(DeviceConfigManager.RCS, rcs);
            editor.putInt("videocall", videoCall);
            editor.apply();
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] updateCscFeature: VoLTE [" + volte + "], Video [" + videoCall + "], Rcs [" + rcs + "]");
        }
    }

    /* access modifiers changed from: protected */
    public void initSwitchPref(boolean updated) {
        SharedPreferences newSp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
        if (updated) {
            if (!newSp.contains("volte")) {
                migrationSharedPreferences(newSp);
            }
            init();
        } else if (newSp.contains("volte")) {
            load();
        } else {
            init();
        }
    }

    public void migrationSharedPreferences(SharedPreferences newSp) {
        SharedPreferences.Editor editor = newSp.edit();
        for (Map.Entry<String, ?> entry : ImsSharedPrefHelper.getSharedPref(-1, this.mContext, "imsswitch", 0, false).getAll().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                editor.putInt(entry.getKey(), ((Integer) value).intValue());
            } else if (!(value instanceof Boolean)) {
                SimpleEventLog simpleEventLog = this.mEventLog;
                simpleEventLog.logAndAdd("Wrong type: " + entry.getKey());
            } else if ("ipme".equals(entry.getKey())) {
                editor.putInt(getIpmeSpKeyName(SimUtil.getMno().getName()), ((Boolean) value).booleanValue() ? 1 : 0);
            } else {
                editor.putBoolean(entry.getKey(), ((Boolean) value).booleanValue());
            }
        }
        editor.apply();
    }

    private void migrationCountryCode() {
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
        if (sp.contains("countryCodeMigration")) {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, "migrationCountryCode: already done before.");
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        for (Map.Entry<String, ?> entry : sp.getAll().entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Integer) {
                String oldKey = entry.getKey();
                String newKey = Mno.convertContryCode(oldKey);
                if (!oldKey.equals(newKey)) {
                    editor.putInt(newKey, ((Integer) value).intValue());
                    editor.remove(oldKey);
                    SimpleEventLog simpleEventLog = this.mEventLog;
                    simpleEventLog.logAndAdd("migrationCountryCode: [" + newKey + ": " + value + "]");
                }
            }
        }
        editor.putBoolean("countryCodeMigration", true);
        editor.apply();
    }

    public void init() {
        String mnoname;
        String operator;
        List<String> networkNames;
        boolean isSimLoaded;
        boolean isLabSimCard;
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init:");
        List<String> networkNames2 = new ArrayList<>();
        String operator2 = SemSystemProperties.get(Mno.MOCK_MNO_PROPERTY, "");
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        ITelephonyManager tm = TelephonyManagerWrapper.getInstance(this.mContext);
        int simState = tm.getSimState(this.mPhoneId);
        if (!TextUtils.isEmpty(operator2) || simState == 0 || simState == 1) {
            isSimLoaded = false;
            networkNames = networkNames2;
            operator = operator2;
            mnoname = "";
            isLabSimCard = false;
        } else if (sm == null) {
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: Not SIM ready yet.");
            isSimLoaded = false;
            networkNames = networkNames2;
            operator = tm.getSimOperator();
            mnoname = "";
            isLabSimCard = false;
        } else {
            String mnoname2 = sm.getSimMnoName();
            String operator3 = sm.getSimOperator();
            boolean isLabSimCard2 = sm.isLabSimCard();
            isSimLoaded = sm.isSimLoaded();
            networkNames = sm.getNetworkNames();
            operator = operator3;
            mnoname = mnoname2;
            isLabSimCard = isLabSimCard2;
        }
        initServiceSwitch(operator, networkNames, isLabSimCard, isSimLoaded, mnoname);
        persist();
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
        this.mRcsEnabled = sp.getBoolean(DeviceConfigManager.RCS, false);
        this.mSsEnabled = sp.getBoolean("ss", false);
        if (!sp.contains(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
            Log.d(this.LOG_TAG, "load: new switch chatbot-communication being set to " + this.mRcsEnabled);
            this.mRcsServiceSwitch.put(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, Boolean.valueOf(this.mRcsEnabled));
            persist();
        }
        dumpServiceSwitch();
        IMSLog.c(LogClass.SWITCH_LOAD, this.mPhoneId + ",LOAD:" + getSwitchDump());
    }

    /* access modifiers changed from: protected */
    public void persist() {
        Log.d(this.LOG_TAG, "persist.");
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        for (Map.Entry<String, Boolean> e : this.mVolteServiceSwitch.entrySet()) {
            editor.putBoolean(e.getKey(), e.getValue().booleanValue());
        }
        for (Map.Entry<String, Boolean> e2 : this.mRcsServiceSwitch.entrySet()) {
            editor.putBoolean(e2.getKey(), e2.getValue().booleanValue());
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "load: volte [" + this.mVoLteEnabled + "], rcs [" + this.mRcsEnabled + "]");
        editor.putBoolean("volte", this.mVoLteEnabled);
        editor.putBoolean(DeviceConfigManager.RCS, this.mRcsEnabled);
        editor.putBoolean("ss", this.mSsEnabled);
        editor.apply();
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch", this.mPhoneId), (ContentObserver) null);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch/mmtel", this.mPhoneId), (ContentObserver) null);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch/mmtel-video", this.mPhoneId), (ContentObserver) null);
    }

    public String getIpmeSpKeyName(String simMno) {
        return "ipme_status_" + simMno;
    }

    public void updateServiceSwitch(ContentValues mnoinfo) {
        IMSLog.d(this.LOG_TAG, this.mPhoneId, "updateServiceSwitch:");
        boolean hasSIM = mnoinfo.getAsBoolean(ISimManager.KEY_HAS_SIM).booleanValue();
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        boolean isLabSimCard = sm != null && sm.isLabSimCard();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "updateServiceSwitch: isLabSimCard [" + isLabSimCard + "]");
        Integer imsSwitchType = mnoinfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
        if (imsSwitchType == null) {
            imsSwitchType = 0;
        }
        if (!hasSIM || !(!isLabSimCard || imsSwitchType.intValue() == 4 || imsSwitchType.intValue() == 3)) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.IMS_SETTINGS_UPDATED"));
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "No operator code for settings. Update UI!");
            return;
        }
        String str2 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "updateMno: hasSIM:" + hasSIM + ", imsSwitchType:" + imsSwitchType + ", mnoinfo:" + mnoinfo);
        if (imsSwitchType.intValue() == 3 || imsSwitchType.intValue() == 4 || imsSwitchType.intValue() == 5) {
            if (imsSwitchType.intValue() == 4) {
                ContentValues imsSwitch = loadImsSwitchFromJson(mnoinfo.getAsString("mnoname"), mnoinfo.getAsString(ISimManager.KEY_MVNO_NAME));
                if (imsSwitch.size() > 0) {
                    mnoinfo.putAll(imsSwitch);
                } else {
                    for (String var : getImsServiceSwitchTable()) {
                        mnoinfo.put(var, false);
                    }
                }
            }
            turnOffAllSwitch();
            updateServiceSwitchInternal(mnoinfo);
            saveImsSwitch(mnoinfo);
            enable(this.mServiceMap);
            return;
        }
        IMSLog.e(this.LOG_TAG, this.mPhoneId, "can not find a matched ims switch type");
        init();
    }

    public void enable(String service, boolean enable) {
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "enable: " + service + " : " + enable);
        if (this.mVolteServiceSwitch.containsKey(service)) {
            this.mVolteServiceSwitch.put(service, Boolean.valueOf(enable));
        }
        if (this.mRcsServiceSwitch.containsKey(service)) {
            this.mRcsServiceSwitch.put(service, Boolean.valueOf(enable));
        }
        persist();
    }

    public void enable(Map<String, Boolean> serviceMap) {
        for (Map.Entry<String, Boolean> e : serviceMap.entrySet()) {
            String key = e.getKey();
            Boolean value = Boolean.valueOf(e.getValue().booleanValue());
            if (this.mVolteServiceSwitch.containsKey(key)) {
                this.mVolteServiceSwitch.put(key, value);
            }
            if (this.mRcsServiceSwitch.containsKey(key)) {
                this.mRcsServiceSwitch.put(key, value);
            }
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mPhoneId;
            simpleEventLog.logAndAdd(i, "enable: " + key + " : " + value);
        }
        persist();
    }

    public String toString() {
        return "Simslot[" + this.mPhoneId + "] ImsServiceSwitch mRcsEnabled [" + this.mRcsEnabled + "], mVoLteEnabled [" + this.mVoLteEnabled + "], mVolteServiceSwitch [" + this.mVolteServiceSwitch + "], mRcsServiceSwitch [" + this.mRcsServiceSwitch + "]";
    }

    public boolean isImsEnabled() {
        return isVoLteEnabled() || isRcsEnabled();
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
            if (!this.mRcsEnabled || !((Boolean) this.mRcsServiceSwitch.get(service)).booleanValue()) {
                return false;
            }
            return true;
        }
    }

    public boolean isVoLteEnabled() {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "isVoLteEnabled: " + this.mVoLteEnabled);
        return this.mVoLteEnabled;
    }

    public void enableVoLte(boolean enable) {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "enableVoLte: " + enable);
        this.mVoLteEnabled = enable;
        persist();
    }

    public boolean isRcsEnabled() {
        return this.mRcsEnabled;
    }

    public boolean isRcsSwitchEnabled() {
        return this.mRcsEnabled;
    }

    public void enableRcs(boolean enable) {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "enableRcs: " + enable);
        this.mRcsEnabled = enable;
        persist();
    }

    public boolean isDefaultMessageAppInUse() {
        boolean result;
        String smsApplication = null;
        try {
            smsApplication = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "Failed to getDefaultSmsPackage: ", e);
        }
        if (smsApplication == null) {
            smsApplication = Settings.Secure.getString(this.mContext.getContentResolver(), "sms_default_application");
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "smsApplication is null check from Settings : " + smsApplication);
        }
        if (smsApplication == null) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "smsApplication is null");
            result = false;
        } else {
            result = TextUtils.equals(smsApplication, PackageUtils.getMsgAppPkgName(this.mContext));
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "isDefaultMessageAppInUse : Result [" + result + "] Name [" + smsApplication + "] ");
        return result;
    }

    public void doInit() {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "doInit from ImsSettings");
        init();
    }

    public int getVideoCallType(String simMno) {
        String spKey = getVideoSpKeyName(simMno);
        int spVal = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", spKey, -1);
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "getVideoCallType: " + spKey + " = [" + spVal + "]");
        return spVal;
    }

    public void setVideoCallType(String simMno, int videoCallType) {
        String spKey = getVideoSpKeyName(simMno);
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", spKey, videoCallType);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] setVideoCallType: " + spKey + " = [" + videoCallType + "]");
    }

    private String getVideoSpKeyName(String simMno) {
        return ImsConstants.SystemSettings.VILTE_SLOT1.getName() + "_" + simMno;
    }

    public int getVoiceCallType(String simMno) {
        String spKey = getVoLteSpKeyName(simMno);
        int spVal = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", spKey, -1);
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "getVoiceCallType: " + spKey + " = [" + spVal + "]");
        return spVal;
    }

    public void setVoiceCallType(String simMno, int voiceCallType) {
        String spKey = getVoLteSpKeyName(simMno);
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", spKey, voiceCallType);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] setVoiceCallType: " + spKey + " = [" + voiceCallType + "]");
    }

    private String getVoLteSpKeyName(String simMno) {
        return ImsConstants.SystemSettings.VOLTE_SLOT1.getName() + "_" + simMno;
    }

    public int getRcsUserSetting() {
        String spKey = getRcsUserSettingSpKeyName();
        int spVal = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", spKey, -1);
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "getRcsUserSetting: " + spKey + " = [" + spVal + "]");
        return spVal;
    }

    public void setRcsUserSetting(int rcsUserSetting) {
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", getRcsUserSettingSpKeyName(), rcsUserSetting);
    }

    private String getRcsUserSettingSpKeyName() {
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        String imsi = "";
        if (sm != null) {
            imsi = sm.getImsi();
        }
        return ImsConstants.SystemSettings.RCS_USER_SETTING1.getName() + "_" + imsi;
    }

    public void dump() {
        this.mEventLog.dump();
        IMSLog.increaseIndent(this.LOG_TAG);
        String str = this.LOG_TAG;
        IMSLog.dump(str, "Last state of " + this.LOG_TAG + "<" + this.mPhoneId + ">:");
        IMSLog.increaseIndent(this.LOG_TAG);
        String str2 = this.LOG_TAG;
        IMSLog.dump(str2, "mVoLteEnabled [" + this.mVoLteEnabled + "], mRcsEnabled [" + this.mRcsEnabled + "]");
        for (Map.Entry<String, Boolean> e : this.mVolteServiceSwitch.entrySet()) {
            String str3 = this.LOG_TAG;
            IMSLog.dump(str3, "<" + this.mPhoneId + "> " + e.getKey() + " = " + e.getValue());
        }
        for (Map.Entry<String, Boolean> e2 : this.mRcsServiceSwitch.entrySet()) {
            String str4 = this.LOG_TAG;
            IMSLog.dump(str4, "<" + this.mPhoneId + "> " + e2.getKey() + " = " + e2.getValue());
        }
        IMSLog.decreaseIndent(this.LOG_TAG);
        IMSLog.decreaseIndent(this.LOG_TAG);
    }

    public String getDefaultMessageApp() {
        String smsApplication = null;
        try {
            smsApplication = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "Failed to getDefaultSmsPackage: ", e);
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "getDefaultMessageApp : [" + smsApplication + "] ");
        return smsApplication;
    }

    public void unregisterObserver() {
    }

    /* access modifiers changed from: protected */
    public void initVolteServiceSwitch(boolean cscVolteEnabled, boolean cscLteVideoCallEnabled, boolean cscRcsEnabled) {
        this.mVolteServiceSwitch.put("mmtel", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("mmtel-video", Boolean.valueOf(cscLteVideoCallEnabled));
        this.mVolteServiceSwitch.put("smsip", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("ss", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("cdpn", Boolean.valueOf(cscVolteEnabled));
        this.mVolteServiceSwitch.put("mmtel-call-composer", Boolean.valueOf(cscVolteEnabled && cscRcsEnabled));
        if (this.mVolteServiceSwitch.values().contains(true)) {
            this.mVoLteEnabled = true;
            this.mSsEnabled = true;
            return;
        }
        this.mVoLteEnabled = false;
        this.mSsEnabled = false;
    }

    /* access modifiers changed from: protected */
    public void initRcsServiceSwitch(boolean cscRcsEnabled) {
        for (String service : ImsProfile.getRcsServiceList()) {
            if (!TextUtils.equals(service, "plug-in")) {
                this.mRcsServiceSwitch.put(service, Boolean.valueOf(cscRcsEnabled));
            }
        }
        if (this.mRcsServiceSwitch.values().contains(true)) {
            this.mRcsEnabled = true;
        }
    }

    /* access modifiers changed from: protected */
    public void initServiceSwitch(String operator, List<String> list, boolean isLabSimCard, boolean isSimLoaded, String mnoname) {
        boolean cscVolteEnabled = true;
        boolean cscLteVideoCallEnabled = true;
        boolean cscRcsEnabled = true;
        ContentValues imsswitch = new ContentValues();
        for (String service : getImsServiceSwitchTable()) {
            imsswitch.put(service, true);
        }
        if (!DeviceUtil.getGcfMode().booleanValue() && !"GCF".equalsIgnoreCase(OmcCode.get()) && !TextUtils.isEmpty(operator) && !isLabSimCard && isSimLoaded && !"45001".equals(operator) && !SimUtil.isSoftphoneEnabled() && ((imsswitch = loadImsSwitchFromJson(mnoname, "")) == null || imsswitch.size() == 0)) {
            cscLteVideoCallEnabled = false;
            cscVolteEnabled = false;
            this.mEventLog.logAndAdd(this.mPhoneId, "init: No ImsSettings in Json for [" + operator + "]. Switch off.");
            cscRcsEnabled = false;
            this.mRcsEnabled = false;
        }
        saveImsSwitch(imsswitch);
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: cscVolteEnabled=" + cscVolteEnabled + "operator: " + operator);
        initVolteServiceSwitch(cscVolteEnabled, cscLteVideoCallEnabled, cscRcsEnabled);
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: rcse=" + cscRcsEnabled);
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
    public void turnOffAllSwitch() {
        this.mServiceMap.clear();
        for (String serviceSwitch : ImsProfile.getVoLteServiceList()) {
            this.mServiceMap.put(serviceSwitch, false);
        }
        for (String serviceSwitch2 : ImsProfile.getRcsServiceList()) {
            this.mServiceMap.put(serviceSwitch2, false);
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning all the switches off.");
    }

    /* access modifiers changed from: protected */
    public void parseImsSwitch(ContentValues mnoinfo) {
        for (Map.Entry<String, String> e : getVolteServiceSwitchTable().entrySet()) {
            String cscField = e.getKey();
            String serviceSwitch = e.getValue();
            String switchValue = mnoinfo.getAsString(cscField);
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mPhoneId;
            simpleEventLog.logAndAdd(i, "CSC(Json) field: " + cscField + "[" + switchValue + "] -> Switching " + serviceSwitch);
            if ("TRUE".equalsIgnoreCase(switchValue)) {
                this.mServiceMap.put(serviceSwitch, true);
                IMSLog.c(LogClass.SWITCH_UPDATE_ON, this.mPhoneId + ",ON:" + serviceSwitch);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void enableCpdnForJansky() {
        if (ImsCscFeature.getInstance().getString(this.mPhoneId, CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGMDMNTYPE).toUpperCase().contains("Jansky".toUpperCase())) {
            this.mServiceMap.put("cdpn", true);
        }
    }

    /* access modifiers changed from: protected */
    public void enableRcsSwitch(boolean isEnableRcsChat) {
        for (String serviceSwitch : ImsProfile.getRcsServiceList()) {
            this.mServiceMap.put(serviceSwitch, true);
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning on all the RCS services.");
        if (!isEnableRcsChat) {
            this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning off RCS Chat Service");
            for (String serviceSwitch2 : ImsProfile.getChatServiceList()) {
                this.mServiceMap.put(serviceSwitch2, false);
            }
            IMSLog.c(LogClass.SWITCH_UPDATE_OFF_CHAT, this.mPhoneId + ",OFF CHAT SW");
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceSwitchInternal(ContentValues mnoinfo) {
        ContentValues contentValues = mnoinfo;
        if (CollectionUtils.getBooleanValue(contentValues, ISimManager.KEY_GLOBALGC_ENABLED, false)) {
            ContentValues gcSettings = new ContentValues();
            gcSettings.put(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, true);
            gcSettings.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, true);
            gcSettings.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, true);
            contentValues.putAll(gcSettings);
        }
        boolean isEnableIms = CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false);
        boolean isEnableRcs = CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false);
        boolean isEnableRcsChat = isEnableIms && isEnableRcs && CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false);
        boolean isEnableVolte = isEnableIms && (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false) || CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false) || CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false));
        if (isEnableIms) {
            parseImsSwitch(mnoinfo);
            enableCpdnForJansky();
            if (isEnableVolte) {
                this.mServiceMap.put("ss", true);
            }
            if (isEnableRcs) {
                enableRcsSwitch(isEnableRcsChat);
                int i = this.mPhoneId;
                Context context = this.mContext;
                Object obj = ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS;
                Object obj2 = ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS;
                SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(i, context, "imsswitch", 0, false);
                if (sp.contains(ImsConstants.SystemSettings.RCS_USER_SETTING1.getName()) && (Mno.SPRINT == Mno.fromName(contentValues.getAsString("mnoname")) || Mno.CMCC == Mno.fromName(contentValues.getAsString("mnoname")))) {
                    isEnableRcs = sp.getBoolean(ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), true);
                }
                if (isEnableRcs && isEnableVolte) {
                    this.mServiceMap.put("mmtel-call-composer", true);
                }
            } else {
                String fieldIms = ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS;
                Object obj3 = ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS;
            }
        } else {
            String fieldIms2 = ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS;
            Object obj4 = ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog.logAndAdd(i2, "updateServiceSwitch: ims [" + isEnableIms + "] volte [" + isEnableVolte + "] rcs [" + isEnableRcs + "]");
        this.mVoLteEnabled = isEnableVolte;
        this.mRcsEnabled = isEnableRcs;
    }

    /* access modifiers changed from: protected */
    public ContentValues loadImsSwitchFromJson(String mnoname, String mvnoname) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "loadImsSwitchFromJson: mnoname=" + mnoname + ",  mvnoname=" + mvnoname);
        ContentValues imsSwitch = new ContentValues();
        if (TextUtils.isEmpty(mnoname)) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: loadImsSwitchFromJson is not identified.");
            return imsSwitch;
        }
        JsonElement element = ImsServiceSwitchLoader.getImsSwitchFromJson(this.mContext, mnoname, this.mPhoneId);
        if (element.isJsonNull()) {
            return imsSwitch;
        }
        JsonObject object = element.getAsJsonObject();
        JsonElement defaultElement = object.get("defaultswitch");
        if (defaultElement.isJsonNull()) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: No default setting.");
            return imsSwitch;
        }
        JsonElement matchElement = ImsServiceSwitchLoader.getMatchedJsonElement(object, mnoname, mvnoname, this.mPhoneId);
        if (matchElement.isJsonNull()) {
            JsonElement matchObj = new JsonObject();
            matchObj.addProperty("mnoname", mnoname);
            matchElement = matchObj;
        }
        JsonObject matchObj2 = this.mAutoUpdate;
        if (matchObj2 != null) {
            matchElement = matchObj2.getUpdatedImsSwitch(matchElement);
        }
        JsonElement matchElement2 = JsonUtil.merge(defaultElement, matchElement);
        if (!JsonUtil.isValidJsonElement(matchElement2)) {
            return imsSwitch;
        }
        for (Map.Entry<String, JsonElement> e : matchElement2.getAsJsonObject().entrySet()) {
            imsSwitch.put(e.getKey(), e.getValue().getAsString());
        }
        return imsSwitch;
    }
}
