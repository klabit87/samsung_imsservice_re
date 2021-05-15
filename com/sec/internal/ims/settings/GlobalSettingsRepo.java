package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.SemSystemProperties;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.CscFeatureTagCommon;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ImsCscFeature;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.Locale;
import java.util.Observable;
import java.util.Optional;

public class GlobalSettingsRepo extends Observable {
    protected static final String SP_KEY_BUILDINFO = "buildinfo";
    protected static final String SP_KEY_CSC_IMSSETTING_TYPE = "cscimssettingtype";
    protected static final String SP_KEY_GCFMODE = "gcfmode";
    protected static final String SP_KEY_GLOBAL_GC_ENABLED = "globalgcenabled";
    protected static final String SP_KEY_HAS_SIM = "hassim";
    protected static final String SP_KEY_IMSI = "imsi";
    protected static final String SP_KEY_LOADED = "loaded";
    protected static final String SP_KEY_MNONAME = "mnoname";
    protected static final String SP_KEY_MVNONAME = "mvnoname";
    protected static final String SP_KEY_NWCODE = "nwcode";
    protected static final String SP_KEY_NWNAME = "NetworkName";
    protected Context mContext;
    protected final Object mLock = new Object();
    protected ContentValues mMnoinfo = null;
    protected int mPhoneId = 0;
    public GlobalSettingsRepo sInstance = null;

    protected GlobalSettingsRepo() {
    }

    public GlobalSettingsRepo(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        makeInstance(SimUtil.getMno(phoneId), phoneId);
    }

    private void makeInstance(Mno mno, int phoneId) {
        unregisterIntentReceiver();
        if (mno.isKor() || mno.isChn() || mno.isHkMo() || mno.isTw()) {
            this.sInstance = new GlobalSettingsRepoKorChnx(this.mContext, phoneId);
        } else if (mno.isUSA()) {
            this.sInstance = new GlobalSettingsRepoUsa(this.mContext, phoneId);
        } else {
            this.sInstance = new GlobalSettingsRepoBase(this.mContext, phoneId);
        }
    }

    public boolean updateMno(ContentValues mnoinfo) {
        Mno mno = Mno.fromName(mnoinfo.getAsString("mnoname"));
        if (mnoinfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE).intValue() == 2 && !mno.isChn()) {
            mno = Mno.DEFAULT;
        }
        mnoinfo.put("mnoname", mno.getName());
        makeInstance(mno, this.mPhoneId);
        boolean updated = this.sInstance.updateMno(mnoinfo);
        boolean isGlobalGcEnabled = ((Boolean) Optional.ofNullable(mnoinfo.getAsBoolean("globalgcenabled")).orElse(false)).booleanValue();
        if (getGlobalGcEnabled() != isGlobalGcEnabled) {
            this.sInstance.loadGlobalGcSettings(isGlobalGcEnabled);
            updated = true;
        }
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
        editor.putString("mnoname", (String) Optional.ofNullable(mnoinfo.getAsString("mnoname")).orElse(""));
        editor.putString("mvnoname", (String) Optional.ofNullable(mnoinfo.getAsString("mvnoname")).orElse(""));
        editor.putString("NetworkName", (String) Optional.ofNullable(mnoinfo.getAsString("NetworkName")).orElse(""));
        editor.putBoolean("globalgcenabled", isGlobalGcEnabled);
        editor.apply();
        return updated;
    }

    /* access modifiers changed from: protected */
    public void loadGlobalGcSettings(boolean isGlobalGcEnabled) {
    }

    /* access modifiers changed from: protected */
    public boolean needResetCallSettingBySim(int phoneId) {
        String autoconfigType = ImsCscFeature.getInstance().getString(phoneId, CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_AUTOCONFIGURATIONTYPE).toUpperCase(Locale.US);
        if (autoconfigType.contains("SIMBASED_OMC") || autoconfigType.contains("SIMBASED_SSKU")) {
            return true;
        }
        String omcProperty = SemSystemProperties.get("ro.simbased.changetype", "");
        if (omcProperty.contains("OMC") || omcProperty.contains("SED")) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean needResetVolteAsDefault(int prevVolteDefaultEnabled, int newVolteDefaultEnabled) {
        return false;
    }

    public Cursor query(String[] projection, String selection, String[] args) {
        return this.sInstance.query(projection, selection, args);
    }

    public String getPreviousMno() {
        return this.sInstance.getPreviousMno();
    }

    public boolean getGlobalGcEnabled() {
        return this.sInstance.getGlobalGcEnabled();
    }

    public void update(ContentValues values) {
        this.sInstance.update(values);
    }

    public void reset() {
        this.sInstance.reset();
    }

    public void load() {
        this.sInstance.load();
    }

    public void loadByDynamicConfig() {
        this.sInstance.loadByDynamicConfig();
    }

    public void unregisterIntentReceiver() {
        GlobalSettingsRepo globalSettingsRepo = this.sInstance;
        if (globalSettingsRepo != null) {
            globalSettingsRepo.unregisterIntentReceiver();
        }
    }

    public void resetUserSettingAsDefault(boolean isNeedToResetVoice, boolean isNeedToResetVideo, boolean isNeedToResetRcs) {
        this.sInstance.resetUserSettingAsDefault(isNeedToResetVoice, isNeedToResetVideo, isNeedToResetRcs);
    }

    public boolean isNeedToBeSetViLTE(ContentValues mnoinfo) {
        return this.sInstance.isNeedToBeSetViLTE(mnoinfo);
    }

    public boolean isNeedToBeSetVoLTE(ContentValues mnoinfo) {
        return this.sInstance.isNeedToBeSetVoLTE(mnoinfo);
    }

    public void dump() {
        this.sInstance.dump();
    }

    /* access modifiers changed from: protected */
    public void updateSystemSettings(Mno mno, ContentValues mnoinfo, String newMnoname, String prevMnoname, int spValueVolte, int spValueVideo) {
    }
}
