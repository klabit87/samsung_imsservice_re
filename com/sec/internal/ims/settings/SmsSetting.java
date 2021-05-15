package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class SmsSetting {
    private static final String LOG_TAG = SmsSetting.class.getSimpleName();
    private Context mContext;
    private SimpleEventLog mLog;
    private String mMnoName = "";
    private String mMvnoName = "";
    private int mPhoneId;
    private ContentValues mValues;

    interface Properties {
        public static final String DEFAULT_SETTING = "default_setting";
        public static final String MNO_NAME = "mnoname";
        public static final String SMS_SETTINGS = "sms_settings";
    }

    public SmsSetting(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mLog = new SimpleEventLog(this.mContext, LOG_TAG, 500);
        this.mValues = new ContentValues();
        init();
    }

    public boolean updateMno(ContentValues mnoInfo, boolean force) {
        String mnoName = CollectionUtils.getStringValue(mnoInfo, "mnoname", this.mMnoName);
        String mvnoName = CollectionUtils.getStringValue(mnoInfo, ISimManager.KEY_MVNO_NAME, this.mMvnoName);
        if (this.mMnoName.equalsIgnoreCase(mnoName) && this.mMvnoName.equalsIgnoreCase(mvnoName) && !force) {
            return false;
        }
        SimpleEventLog simpleEventLog = this.mLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "updateMno " + this.mMnoName + " -> " + mnoName + " force : " + force);
        this.mMnoName = mnoName;
        this.mMvnoName = mvnoName;
        return init();
    }

    /* Debug info: failed to restart local var, previous not found, register: 16 */
    public boolean init() {
        JsonReader reader;
        JsonElement jsonElement;
        JsonElement matchedSmsSettingElement;
        if (TextUtils.isEmpty(this.mMnoName)) {
            ISimManager simManager = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
            if (simManager != null) {
                String[] fullMnoName = simManager.getSimMnoName().split(String.valueOf(Mno.MVNO_DELIMITER), 2);
                this.mMnoName = fullMnoName[0];
                this.mLog.logAndAdd(this.mPhoneId, "initialize with SIM " + Arrays.toString(fullMnoName));
                if (fullMnoName.length > 1) {
                    this.mMvnoName = fullMnoName[1];
                }
            } else {
                this.mLog.logAndAdd(this.mPhoneId, "initialize without SIM");
                this.mMnoName = SimUtil.getMno(this.mPhoneId).getName();
                this.mMvnoName = "";
            }
        }
        this.mValues.clear();
        ImsAutoUpdate autoUpdate = ImsAutoUpdate.getInstance(this.mContext, this.mPhoneId);
        try {
            reader = new JsonReader(new BufferedReader(new InputStreamReader(this.mContext.getResources().openRawResource(R.raw.smssettings))));
            JsonElement wholeElement = new JsonParser().parse(reader);
            reader.close();
            JsonObject wholeObject = wholeElement.getAsJsonObject();
            JsonElement defaultSmsSettingElement = wholeObject.get(Properties.DEFAULT_SETTING);
            if (defaultSmsSettingElement.isJsonNull()) {
                this.mLog.logAndAdd(this.mPhoneId, "default_setting is not exist");
                return false;
            }
            JsonElement defaultSmsSettingElement2 = autoUpdate.getUpdatedSmsSetting(defaultSmsSettingElement, Properties.DEFAULT_SETTING);
            JsonArray smsSettingsArray = wholeObject.getAsJsonArray(Properties.SMS_SETTINGS);
            if (!JsonUtil.isValidJsonElement(smsSettingsArray)) {
                this.mLog.logAndAdd(this.mPhoneId, "sms_settings is not valid");
                return false;
            }
            String expectedMnoName = this.mMnoName;
            JsonElement matchedSmsSettingElement2 = JsonNull.INSTANCE;
            if (!TextUtils.isEmpty(this.mMvnoName)) {
                expectedMnoName = expectedMnoName + Mno.MVNO_DELIMITER + this.mMvnoName;
            }
            Iterator it = smsSettingsArray.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                JsonElement settingObject = ((JsonElement) it.next()).getAsJsonObject();
                String mnoName = settingObject.get("mnoname").getAsString();
                if (expectedMnoName.equalsIgnoreCase(mnoName)) {
                    IMSLog.d(LOG_TAG, this.mPhoneId, "find exact sms setting by " + expectedMnoName);
                    matchedSmsSettingElement2 = settingObject;
                    break;
                } else if (this.mMnoName.equalsIgnoreCase(mnoName)) {
                    IMSLog.d(LOG_TAG, this.mPhoneId, "find sms setting expected " + expectedMnoName + " without MVNO");
                    matchedSmsSettingElement2 = settingObject;
                }
            }
            JsonElement matchedSmsSettingElement3 = JsonUtil.merge(defaultSmsSettingElement2, matchedSmsSettingElement2);
            if (!JsonUtil.isValidJsonElement(matchedSmsSettingElement3)) {
                this.mLog.logAndAdd(this.mPhoneId, "Not defined sms setting for " + expectedMnoName);
                matchedSmsSettingElement = defaultSmsSettingElement2;
            } else {
                matchedSmsSettingElement = autoUpdate.getUpdatedSmsSetting(matchedSmsSettingElement3, "mnoname");
            }
            for (Map.Entry<String, JsonElement> e : matchedSmsSettingElement.getAsJsonObject().entrySet()) {
                this.mValues.put(e.getKey(), ((JsonElement) Optional.ofNullable(e.getValue()).orElse(JsonNull.INSTANCE)).toString());
            }
            return true;
        } catch (JsonParseException | IOException e2) {
            e2.printStackTrace();
            this.mLog.logAndAdd(this.mPhoneId, "smssettings.json parse fail " + e2.getMessage());
            return false;
        } catch (Throwable th) {
            jsonElement.addSuppressed(th);
        }
        throw jsonElement;
    }

    public Cursor getAsCursor() {
        MatrixCursor cursor = new MatrixCursor((String[]) this.mValues.keySet().toArray(new String[0]));
        cursor.addRow(this.mValues.getValues().values());
        return cursor;
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of SmsSetting:");
        this.mLog.dump();
        IMSLog.increaseIndent(LOG_TAG);
        IMSLog.dump(LOG_TAG, "Last value of SmsSetting:");
        this.mValues.keySet().forEach(new Consumer() {
            public final void accept(Object obj) {
                SmsSetting.this.lambda$dump$0$SmsSetting((String) obj);
            }
        });
        IMSLog.decreaseIndent(LOG_TAG);
    }

    public /* synthetic */ void lambda$dump$0$SmsSetting(String key) {
        IMSLog.increaseIndent(LOG_TAG);
        String str = LOG_TAG;
        IMSLog.dump(str, key + ": " + this.mValues.getAsString(key));
        IMSLog.decreaseIndent(LOG_TAG);
    }
}
