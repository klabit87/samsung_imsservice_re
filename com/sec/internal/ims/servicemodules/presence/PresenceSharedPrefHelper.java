package com.sec.internal.ims.servicemodules.presence;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class PresenceSharedPrefHelper {
    private static final String LOG_TAG = "PresenceSharedPrefHelper";
    private final Context mContext;
    private final PresenceModule mPresence;

    PresenceSharedPrefHelper(Context context, PresenceModule presenceModule) {
        this.mContext = context;
        this.mPresence = presenceModule;
    }

    private SharedPreferences getPresenceSharedPreferences(int phoneId) {
        Context context = this.mContext;
        return context.getSharedPreferences("presence_" + phoneId, 0);
    }

    private void save(String key, String value, int phoneId) {
        SharedPreferences.Editor editor = getPresenceSharedPreferences(phoneId).edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void save(String key, Long value, int phoneId) {
        SharedPreferences.Editor editor = getPresenceSharedPreferences(phoneId).edit();
        editor.putLong(key, value.longValue());
        editor.apply();
    }

    private String load(String key, String defaultValue, int phoneId) {
        return getPresenceSharedPreferences(phoneId).getString(key, defaultValue);
    }

    private long load(String key, long defaultValue, int phoneId) {
        return getPresenceSharedPreferences(phoneId).getLong(key, defaultValue);
    }

    /* access modifiers changed from: package-private */
    public void saveRandomTupleId(long feature, String tupleId, int phoneId) {
        save("tupleId_" + feature, tupleId, phoneId);
    }

    /* access modifiers changed from: package-private */
    public String loadRandomTupleId(long feature, int phoneId) {
        return load("tupleId_" + feature, (String) null, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void saveBadEventTimestamp(long timestamp, int phoneId) {
        save("BadEventTimestamp", Long.valueOf(timestamp), phoneId);
        this.mPresence.getPresenceModuleInfo(phoneId).mLastBadEventTimestamp = timestamp;
    }

    /* access modifiers changed from: package-private */
    public long loadBadEventTimestamp(int phoneId) {
        long lastBadEventTimestamp = load("BadEventTimestamp", 0, phoneId);
        if (lastBadEventTimestamp <= new Date().getTime()) {
            return lastBadEventTimestamp;
        }
        IMSLog.s(LOG_TAG, phoneId, "loadBadEventTimestamp: abnormal case, clear lastBadEventTimestamp " + lastBadEventTimestamp + " to 0");
        saveBadEventTimestamp(0, phoneId);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void savePublishTimestamp(long timestamp, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "savePublishTimestamp: publish_timeout = " + timestamp);
        save("publish_timeout", Long.valueOf(timestamp), phoneId);
        this.mPresence.getPresenceModuleInfo(phoneId).mLastPublishTimestamp = timestamp;
    }

    /* access modifiers changed from: package-private */
    public long loadPublishTimestamp(int phoneId) {
        long lastPublishTimestamp = load("publish_timeout", 0, phoneId);
        if (lastPublishTimestamp <= new Date().getTime()) {
            return lastPublishTimestamp;
        }
        IMSLog.s(LOG_TAG, phoneId, "loadPublishTimestamp: abnormal case, clear lastPublishTimestamp " + lastPublishTimestamp + " to 0");
        savePublishTimestamp(0, phoneId);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void savePublishETag(String eTag, long expireTimer, int phoneId) {
        SharedPreferences.Editor editor = getPresenceSharedPreferences(phoneId).edit();
        editor.putString("publish_etag", eTag);
        editor.putLong("publish_expire_timer", expireTimer);
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public String getPublishETag(int phoneId) {
        return load("publish_etag", (String) null, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void resetPublishEtag(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "resetPublishEtag");
        SharedPreferences.Editor editor = getPresenceSharedPreferences(phoneId).edit();
        editor.remove("publish_etag");
        editor.remove("publish_expire_timer");
        editor.apply();
    }

    /* access modifiers changed from: package-private */
    public boolean checkIfValidEtag(int phoneId) {
        SharedPreferences pref = getPresenceSharedPreferences(phoneId);
        String imsi = pref.getString("imsi", (String) null);
        String newImsi = this.mPresence.getPresenceModuleInfo(phoneId).mSimCardManager.getImsi();
        long expireTimer = pref.getLong("publish_expire_timer", 0);
        long publishTimer = loadPublishTimestamp(phoneId);
        if (this.mPresence.getPresenceModuleInfo(phoneId).mMno.isKor()) {
            if (this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp > 0) {
                publishTimer = this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp;
                this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp = 0;
            }
        } else if (this.mPresence.getPresenceModuleInfo(phoneId).mMno == Mno.ATT && publishTimer == 0 && this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp > 0) {
            publishTimer = this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp;
            this.mPresence.getPresenceModuleInfo(phoneId).mBackupPublishTimestamp = 0;
        }
        Date currentTime = new Date();
        IMSLog.i(LOG_TAG, phoneId, "checkIfValidEtag: currentTime=" + currentTime.getTime() + " publishTimer=" + publishTimer + " expireTimer=" + expireTimer + " currentTime-publishTimer=" + ((currentTime.getTime() - publishTimer) / 1000));
        return (currentTime.getTime() - publishTimer) / 1000 < expireTimer && !TextUtils.isEmpty(newImsi) && newImsi.equals(imsi);
    }

    /* access modifiers changed from: package-private */
    public void checkAndClearPresencePreferences(String phoneIdImsi, int phoneId) {
        SharedPreferences pref = getPresenceSharedPreferences(phoneId);
        String imsi = pref.getString("imsi", (String) null);
        if (imsi == null || !imsi.equals(phoneIdImsi)) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("publish_etag", "");
            editor.putLong("publish_expire_timer", 0);
            editor.putString("imsi", phoneIdImsi);
            editor.putLong("publish_timeout", 0);
            editor.putLong("BadEventTimestamp", 0);
            editor.apply();
        }
    }
}
