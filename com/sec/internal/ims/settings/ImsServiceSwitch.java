package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImsServiceSwitch {
    protected static final String IMS_FEATURE_RCS = "rcs";
    protected static final String IMS_FEATURE_VOLTE = "volte";
    protected static final String IMS_FEATURE_VT = "videocall";
    protected static final String IMS_SETTINGS_UPDATED = "android.intent.action.IMS_SETTINGS_UPDATED";
    private static final String LOG_TAG = "ImsServiceSwitch";
    protected static final String SP_KEY_MNONAME = "mnoname";
    private static final List<String> mImsServiceSwitchTable = new ArrayList();
    private static final HashMap<String, String> mImsVolteSwitchTable = new HashMap<>();
    protected boolean isLoded = true;
    protected Context mContext;
    protected boolean mDefaultSms = true;
    protected SimpleEventLog mEventLog;
    protected int mPhoneId = 0;
    protected boolean mRcsEnabled = false;
    protected Map<String, Boolean> mRcsServiceSwitch = new ConcurrentHashMap();
    protected boolean mSsEnabled = false;
    protected boolean mVoLteEnabled = false;
    protected Map<String, Boolean> mVolteServiceSwitch = new ConcurrentHashMap();
    protected ImsServiceSwitch sInstance = null;

    public static class ImsSwitch {

        public static class DeviceManagement {
            public static final String ENABLE_IMS = "enableIms";
            public static final String ENABLE_VOWIFI = "enableServiceVowifi";
        }

        public static class RCS {
            public static final String ENABLE_RCS = "enableServiceRcs";
            public static final String ENABLE_RCS_CHAT_SERVICE = "enableServiceRcschat";
        }

        public static class VoLTE {
            public static final String ENABLE_SMS_IP = "enableServiceSmsip";
            public static final String ENABLE_VIDEO_CALL = "enableServiceVilte";
            public static final String ENABLE_VOLTE = "enableServiceVolte";
        }
    }

    public ImsServiceSwitch() {
    }

    public ImsServiceSwitch(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        makeInstance(SimUtil.getMno(phoneId), phoneId);
    }

    private void makeInstance(Mno mno, int phoneId) {
        IMSLog.d(LOG_TAG, phoneId, "makeInstance: " + mno);
        if (mno.isUSA()) {
            this.sInstance = new ImsServiceSwitchUsa(this.mContext, phoneId);
        } else if (ConfigUtil.isRcsEur(mno) || mno.isOce()) {
            this.sInstance = new ImsServiceSwitchEur(this.mContext, phoneId);
        } else if (mno.isKor()) {
            this.sInstance = new ImsServiceSwitchKor(this.mContext, phoneId);
        } else {
            this.sInstance = new ImsServiceSwitchBase(this.mContext, phoneId);
        }
    }

    public void updateServiceSwitch(ContentValues mnoinfo) {
        makeInstance(Mno.fromName(mnoinfo.getAsString("mnoname")), this.mPhoneId);
        this.sInstance.updateServiceSwitch(mnoinfo);
        this.sInstance.dumpServiceSwitch();
        IMSLog.c(LogClass.SWITCH_UPDATE_DONE, this.mPhoneId + ",UPDATE:" + this.sInstance.getSwitchDump());
    }

    public boolean isRcsEnabled() {
        return this.sInstance.isRcsEnabled();
    }

    public boolean isRcsSwitchEnabled() {
        return this.sInstance.isRcsSwitchEnabled();
    }

    public boolean isEnabled(String service) {
        return this.sInstance.isEnabled(service);
    }

    public boolean isImsEnabled() {
        return this.sInstance.isImsEnabled();
    }

    public boolean isVoLteEnabled() {
        return this.sInstance.isVoLteEnabled();
    }

    public int getVoiceCallType(String simMno) {
        return this.sInstance.getVoiceCallType(simMno);
    }

    public int getVideoCallType(String simMno) {
        return this.sInstance.getVideoCallType(simMno);
    }

    public int getRcsUserSetting() {
        return this.sInstance.getRcsUserSetting();
    }

    public boolean isDefaultMessageAppInUse() {
        return this.sInstance.isDefaultMessageAppInUse();
    }

    public void enable(String service, boolean enable) {
        this.sInstance.enable(service, enable);
    }

    public void enableVoLte(boolean enable) {
        this.sInstance.enableVoLte(enable);
    }

    public void enableRcs(boolean enable) {
        this.sInstance.enableRcs(enable);
        IMSLog.c(LogClass.SWITCH_ENABLE_RCS, this.mPhoneId + ",RCS SW:" + enable);
    }

    public void dump() {
        this.sInstance.dump();
    }

    public void setVoiceCallType(String simMno, int voiceCallType) {
        this.sInstance.setVoiceCallType(simMno, voiceCallType);
    }

    public void setVideoCallType(String simMno, int videoCallType) {
        this.sInstance.setVideoCallType(simMno, videoCallType);
    }

    public void setRcsUserSetting(int rcsUserSetting) {
        this.sInstance.setRcsUserSetting(rcsUserSetting);
    }

    public void doInit() {
        this.sInstance.doInit();
    }

    /* access modifiers changed from: protected */
    public void dumpServiceSwitch() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "dumpServiceSwitch: volte [" + this.mVoLteEnabled + "] rcs [" + this.mRcsEnabled + "]");
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i2 = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("dumpServiceSwitch: ");
        sb.append((String) Stream.concat(this.mVolteServiceSwitch.entrySet().stream(), this.mRcsServiceSwitch.entrySet().stream()).map($$Lambda$ImsServiceSwitch$IBXxg7mTeSy6TId_FqbCiR5LOgU.INSTANCE).collect(Collectors.joining(", ")));
        simpleEventLog2.logAndAdd(i2, sb.toString());
    }

    static /* synthetic */ String lambda$dumpServiceSwitch$0(Map.Entry e) {
        return ((String) e.getKey()) + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + e.getValue();
    }

    /* access modifiers changed from: protected */
    public String getSwitchDump() {
        StringBuilder dump = new StringBuilder();
        String str = "1";
        dump.append(this.mVoLteEnabled ? str : "0");
        dump.append((String) Arrays.stream(ImsProfile.getVoLteServiceList()).map(new Function() {
            public final Object apply(Object obj) {
                return ImsServiceSwitch.this.lambda$getSwitchDump$1$ImsServiceSwitch((String) obj);
            }
        }).collect(Collectors.joining("", "_", ",")));
        if (!this.mRcsEnabled) {
            str = "0";
        }
        dump.append(str);
        dump.append((String) Arrays.stream(ImsProfile.getRcsServiceList()).map(new Function() {
            public final Object apply(Object obj) {
                return ImsServiceSwitch.this.lambda$getSwitchDump$2$ImsServiceSwitch((String) obj);
            }
        }).collect(Collectors.joining("", "_", "")));
        return dump.toString();
    }

    public /* synthetic */ String lambda$getSwitchDump$1$ImsServiceSwitch(String svc) {
        return ((Boolean) Optional.ofNullable(this.mVolteServiceSwitch.get(svc)).orElse(false)).booleanValue() ? "1" : "0";
    }

    public /* synthetic */ String lambda$getSwitchDump$2$ImsServiceSwitch(String svc) {
        return ((Boolean) Optional.ofNullable(this.mRcsServiceSwitch.get(svc)).orElse(false)).booleanValue() ? "1" : "0";
    }

    /* access modifiers changed from: protected */
    public void saveImsSwitch(ContentValues cv) {
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        for (String value : getImsServiceSwitchTable()) {
            editor.putBoolean(value, CollectionUtils.getBooleanValue(cv, value, false));
        }
        editor.apply();
    }

    static {
        mImsVolteSwitchTable.put(ImsSwitch.VoLTE.ENABLE_VOLTE, "mmtel");
        mImsVolteSwitchTable.put(ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, "mmtel-video");
        mImsVolteSwitchTable.put(ImsSwitch.VoLTE.ENABLE_SMS_IP, "smsip");
        mImsServiceSwitchTable.add(ImsSwitch.DeviceManagement.ENABLE_IMS);
        mImsServiceSwitchTable.add(ImsSwitch.DeviceManagement.ENABLE_VOWIFI);
        mImsServiceSwitchTable.add(ImsSwitch.VoLTE.ENABLE_SMS_IP);
        mImsServiceSwitchTable.add(ImsSwitch.VoLTE.ENABLE_VIDEO_CALL);
        mImsServiceSwitchTable.add(ImsSwitch.VoLTE.ENABLE_VOLTE);
        mImsServiceSwitchTable.add(ImsSwitch.RCS.ENABLE_RCS);
        mImsServiceSwitchTable.add(ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE);
    }

    public static HashMap<String, String> getVolteServiceSwitchTable() {
        return mImsVolteSwitchTable;
    }

    public static List<String> getImsServiceSwitchTable() {
        return mImsServiceSwitchTable;
    }

    public static ContentValues getSimMobilityImsSwitchSetting() {
        ContentValues simMobilitySettings = new ContentValues();
        simMobilitySettings.put(ImsSwitch.DeviceManagement.ENABLE_IMS, true);
        for (String cscField : getVolteServiceSwitchTable().keySet()) {
            simMobilitySettings.put(cscField, true);
        }
        simMobilitySettings.put(ImsSwitch.DeviceManagement.ENABLE_VOWIFI, true);
        return simMobilitySettings;
    }

    public static ContentValues getXasImsSwitchSetting() {
        ContentValues xasSettings = new ContentValues();
        xasSettings.put(ImsSwitch.DeviceManagement.ENABLE_IMS, true);
        xasSettings.put(ImsSwitch.VoLTE.ENABLE_VOLTE, true);
        xasSettings.put(ImsSwitch.DeviceManagement.ENABLE_VOWIFI, true);
        xasSettings.put(ImsSwitch.VoLTE.ENABLE_SMS_IP, true);
        xasSettings.put(ImsSwitch.RCS.ENABLE_RCS, true);
        xasSettings.put(ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, true);
        return xasSettings;
    }

    public void unregisterObserver() {
        this.sInstance.unregisterObserver();
    }

    public boolean isImsSwitchEnabled(String switchval) {
        return ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).getBoolean(switchval, false);
    }
}
