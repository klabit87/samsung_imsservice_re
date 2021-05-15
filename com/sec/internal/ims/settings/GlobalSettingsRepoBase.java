package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.CscFeatureTagCommon;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ImsCscFeature;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class GlobalSettingsRepoBase extends GlobalSettingsRepo {
    private final String LOG_TAG = GlobalSettingsRepoBase.class.getSimpleName();
    protected ImsAutoUpdate mAutoUpdate;
    protected boolean mCscChanged = false;
    protected SimpleEventLog mEventLog;
    protected ImsSimMobilityUpdate mMobilityUpdate;
    protected boolean mVersionUpdated = false;

    public GlobalSettingsRepoBase(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mEventLog = new SimpleEventLog(context, this.LOG_TAG, 300);
        this.mAutoUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId);
        this.mMobilityUpdate = ImsSimMobilityUpdate.getInstance(this.mContext);
    }

    public void update(ContentValues values) {
        synchronized (this.mLock) {
            save(values);
        }
    }

    /* access modifiers changed from: protected */
    public void save(JsonObject valuesObj) {
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
        for (Map.Entry<String, JsonElement> e : valuesObj.entrySet()) {
            String key = e.getKey();
            JsonElement value = e.getValue();
            String str = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("save : ");
            sb.append(key);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(value != null ? value.toString() : "null");
            Log.d(str, sb.toString());
            if (value != null) {
                if (value.isJsonArray()) {
                    editor.putString(key, value.toString());
                } else {
                    JsonPrimitive primitive = value.getAsJsonPrimitive();
                    if (!primitive.isBoolean()) {
                        editor.putString(key, value.getAsString());
                    } else if (primitive.getAsBoolean()) {
                        editor.putString(key, "1");
                    } else {
                        editor.putString(key, "0");
                    }
                }
            }
        }
        editor.apply();
    }

    /* access modifiers changed from: protected */
    public void save(ContentValues cv) {
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
        for (Map.Entry<String, Object> e : cv.valueSet()) {
            String str = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("   ");
            sb.append(e.getKey());
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(e.getValue() != null ? e.getValue().toString() : "null");
            Log.d(str, sb.toString());
            if (e.getValue() != null) {
                editor.putString(e.getKey(), e.getValue().toString());
            }
        }
        editor.apply();
    }

    private Map<String, Object> readSettings(String spname, String[] projection) {
        Map<String, Object> cv = new HashMap<>();
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, spname, 0, false);
        if (projection != null) {
            for (String key : projection) {
                if (!sp.contains(key)) {
                    Log.e(this.LOG_TAG, spname + " No matched key : " + key);
                    cv.put(key, (Object) null);
                }
                try {
                    cv.put(key, sp.getString(key, (String) null));
                } catch (ClassCastException e) {
                    try {
                        cv.put(key, Integer.valueOf(sp.getInt(key, 0)));
                    } catch (ClassCastException e2) {
                        e2.printStackTrace();
                        throw new IllegalArgumentException("Boolean type is not supported in globalSettings");
                    }
                }
            }
        } else {
            cv.putAll(sp.getAll());
        }
        return cv;
    }

    public Cursor query(String[] projection, String selection, String[] args) {
        if (!isLoaded()) {
            Log.e(this.LOG_TAG, "query: globalsettings not loaded. loading now.");
            load();
        }
        Map<String, Object> cv = new HashMap<>(readSettings(ImsSharedPrefHelper.GLOBAL_SETTINGS, projection));
        for (Map.Entry<String, Object> entry : readSettings(ImsSharedPrefHelper.GLOBAL_GC_SETTINGS, projection).entrySet()) {
            if (entry.getValue() != null) {
                if (TextUtils.equals(entry.getKey(), GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM) || TextUtils.equals(entry.getKey(), GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN)) {
                    Log.i(this.LOG_TAG, "query: Don't override block_regi_on_invalid_isim and voice_domain_pref_eutran value");
                } else {
                    cv.put(entry.getKey(), entry.getValue());
                }
            }
        }
        MatrixCursor cursor = new MatrixCursor((String[]) cv.keySet().toArray(new String[0]));
        cursor.addRow(cv.values());
        return cursor;
    }

    public void load() {
        synchronized (this.mLock) {
            if (!isLoaded()) {
                boolean hassim = false;
                String omcnwCode = OmcCode.getNWCode(this.mPhoneId);
                Mno mno = Mno.fromSalesCode(omcnwCode);
                String str = this.LOG_TAG;
                Log.d(str, "load: initial with OMCNW_CODE(" + omcnwCode + ") Mno=" + mno.getName());
                if (SimUtil.isSoftphoneEnabled()) {
                    hassim = true;
                }
                loadGlobalSettingsFromJson(hassim, mno.getName(), 0, new ContentValues());
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* access modifiers changed from: protected */
    public void loadGlobalGcSettings(boolean isGlobalGcEnabled) {
        Throwable th;
        JsonReader reader;
        Throwable th2;
        boolean z = isGlobalGcEnabled;
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "loadGlobalGcSettings isGlobalGcEnabled=" + z);
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_GC_SETTINGS, 0, false).edit();
        editor.clear();
        if (z) {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, " getResources : globalsettings.json");
            try {
                InputStream inputStream = this.mContext.getResources().openRawResource(R.raw.globalsettings);
                try {
                    reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
                    JsonParser parser = new JsonParser();
                    JsonElement element = parser.parse(reader);
                    reader.close();
                    reader.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    JsonArray globalSettingsArray = element.getAsJsonObject().getAsJsonArray(ImsAutoUpdate.TAG_GLOBALSETTING);
                    if (!JsonUtil.isValidJsonElement(globalSettingsArray)) {
                        IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: parse failed.");
                        return;
                    }
                    JsonElement gcElement = JsonNull.INSTANCE;
                    Iterator it = globalSettingsArray.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        JsonElement elem = (JsonElement) it.next();
                        JsonElement obj = elem.getAsJsonObject();
                        if (elem.getAsJsonObject().get("mnoname").getAsString().equalsIgnoreCase("GoogleGC_ALL")) {
                            gcElement = obj;
                            IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalGcSettings GoogleGC_ALL found");
                            break;
                        }
                    }
                    if (gcElement.isJsonNull()) {
                        IMSLog.i(this.LOG_TAG, this.mPhoneId, "loadGlobalGcSettings GoogleGC_ALL is not exist");
                        return;
                    }
                    ImsAutoUpdate imsAutoUpdate = this.mAutoUpdate;
                    if (imsAutoUpdate != null) {
                        gcElement = imsAutoUpdate.getUpdatedGlobalSetting(gcElement);
                    }
                    for (Map.Entry<String, JsonElement> e : gcElement.getAsJsonObject().entrySet()) {
                        String key = e.getKey();
                        JsonElement value = e.getValue();
                        String str2 = this.LOG_TAG;
                        StringBuilder sb = new StringBuilder();
                        JsonParser parser2 = parser;
                        sb.append("save : ");
                        sb.append(key);
                        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                        sb.append(value);
                        Log.d(str2, sb.toString());
                        if (value != null) {
                            if (value.isJsonArray()) {
                                editor.putString(key, value.toString());
                            } else {
                                JsonPrimitive primitive = value.getAsJsonPrimitive();
                                if (!primitive.isBoolean()) {
                                    editor.putString(key, value.getAsString());
                                } else if (primitive.getAsBoolean()) {
                                    editor.putString(key, "1");
                                } else {
                                    editor.putString(key, "0");
                                }
                            }
                        }
                        parser = parser2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                return;
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        }
        editor.apply();
        return;
        throw th2;
    }

    public void loadByDynamicConfig() {
        synchronized (this.mLock) {
            if (this.mMnoinfo != null) {
                Boolean hasSIM = this.mMnoinfo.getAsBoolean(ISimManager.KEY_HAS_SIM);
                if (hasSIM == null) {
                    hasSIM = false;
                }
                Boolean isGlobalGcEnabled = this.mMnoinfo.getAsBoolean(ISimManager.KEY_GLOBALGC_ENABLED);
                if (isGlobalGcEnabled == null) {
                    isGlobalGcEnabled = false;
                }
                String newMnoname = this.mMnoinfo.getAsString("mnoname");
                String newMvnoname = this.mMnoinfo.getAsString(ISimManager.KEY_MVNO_NAME);
                Integer imsSwitchType = this.mMnoinfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
                if (imsSwitchType == null) {
                    imsSwitchType = 0;
                }
                loadGlobalSettingsFromJson(hasSIM.booleanValue(), newMnoname, newMvnoname, imsSwitchType.intValue(), this.mMnoinfo);
                loadGlobalGcSettings(isGlobalGcEnabled.booleanValue());
            }
        }
    }

    public boolean isLoaded() {
        return ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "loaded", false);
    }

    public void loadGlobalSettingsFromJson(boolean hasSIM, String mnoname, int cscImsSettings, ContentValues mnoinfo) {
        loadGlobalSettingsFromJson(hasSIM, mnoname, "", cscImsSettings, mnoinfo);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:103:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0271 A[SYNTHETIC, Splitter:B:84:0x0271] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x027e A[SYNTHETIC, Splitter:B:89:0x027e] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadGlobalSettingsFromJson(boolean r27, java.lang.String r28, java.lang.String r29, int r30, android.content.ContentValues r31) {
        /*
            r26 = this;
            r1 = r26
            r2 = r27
            r3 = r28
            r4 = r29
            r5 = r30
            r6 = r31
            java.lang.String r0 = r1.LOG_TAG
            int r7 = r1.mPhoneId
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "loadGlobalSettings: mnoname="
            r8.append(r9)
            r8.append(r3)
            java.lang.String r9 = ",  mvnoname="
            r8.append(r9)
            r8.append(r4)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.d(r0, r7, r8)
            if (r3 == 0) goto L_0x0289
            boolean r0 = r28.isEmpty()
            if (r0 == 0) goto L_0x0036
            goto L_0x0289
        L_0x0036:
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.fromName(r28)
            r0 = 3
            if (r5 != r0) goto L_0x0042
            com.sec.internal.ims.settings.ImsSimMobilityUpdate r0 = r1.mMobilityUpdate
            r0.loadMobilityGlobalSettings()
        L_0x0042:
            r8 = 0
            android.content.Context r0 = r1.mContext     // Catch:{ IOException -> 0x026a }
            android.content.res.Resources r0 = r0.getResources()     // Catch:{ IOException -> 0x026a }
            r9 = 2131034112(0x7f050000, float:1.7678732E38)
            java.io.InputStream r0 = r0.openRawResource(r9)     // Catch:{ IOException -> 0x026a }
            r8 = r0
            com.google.gson.JsonParser r0 = new com.google.gson.JsonParser     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r0.<init>()     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r9 = r0
            com.google.gson.stream.JsonReader r0 = new com.google.gson.stream.JsonReader     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            java.io.BufferedReader r10 = new java.io.BufferedReader     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            java.io.InputStreamReader r11 = new java.io.InputStreamReader     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r11.<init>(r8)     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r10.<init>(r11)     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r0.<init>(r10)     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r10 = r0
            com.google.gson.JsonElement r0 = r9.parse(r10)     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            r11 = r0
            r10.close()     // Catch:{ IOException -> 0x0263, all -> 0x025e }
            if (r8 == 0) goto L_0x007b
            r8.close()     // Catch:{ IOException -> 0x0074 }
        L_0x0073:
            goto L_0x007b
        L_0x0074:
            r0 = move-exception
            r12 = r0
            r0 = r12
            r0.printStackTrace()
            goto L_0x0073
        L_0x007b:
            com.google.gson.JsonObject r0 = r11.getAsJsonObject()
            java.lang.String r12 = "defaultsetting"
            com.google.gson.JsonElement r12 = r0.get(r12)
            boolean r13 = r12.isJsonNull()
            if (r13 == 0) goto L_0x0095
            java.lang.String r13 = r1.LOG_TAG
            int r14 = r1.mPhoneId
            java.lang.String r15 = "load: No default setting."
            com.sec.internal.log.IMSLog.e(r13, r14, r15)
            return
        L_0x0095:
            java.lang.String r13 = "nohitsetting"
            com.google.gson.JsonElement r13 = r0.get(r13)
            com.sec.internal.ims.settings.ImsAutoUpdate r14 = r1.mAutoUpdate
            if (r14 == 0) goto L_0x00a9
            com.google.gson.JsonElement r13 = r14.applyNohitSettingUpdate(r13)
            com.sec.internal.ims.settings.ImsAutoUpdate r14 = r1.mAutoUpdate
            com.google.gson.JsonElement r12 = r14.applyDefaultSettingUpdate(r12)
        L_0x00a9:
            com.google.gson.JsonNull r14 = com.google.gson.JsonNull.INSTANCE
            com.sec.internal.constants.Mno r15 = com.sec.internal.constants.Mno.DEFAULT
            r16 = r8
            java.lang.String r8 = "mnoname"
            if (r7 == r15) goto L_0x01db
            java.lang.String r15 = "globalsetting"
            com.google.gson.JsonArray r15 = r0.getAsJsonArray(r15)
            boolean r17 = com.sec.internal.helper.JsonUtil.isValidJsonElement(r15)
            if (r17 != 0) goto L_0x00cd
            java.lang.String r8 = r1.LOG_TAG
            r17 = r0
            int r0 = r1.mPhoneId
            r18 = r9
            java.lang.String r9 = "load: parse failed."
            com.sec.internal.log.IMSLog.e(r8, r0, r9)
            return
        L_0x00cd:
            r17 = r0
            r18 = r9
            java.util.Iterator r0 = r15.iterator()
        L_0x00d5:
            boolean r9 = r0.hasNext()
            if (r9 == 0) goto L_0x01a0
            java.lang.Object r9 = r0.next()
            com.google.gson.JsonElement r9 = (com.google.gson.JsonElement) r9
            com.google.gson.JsonObject r19 = r9.getAsJsonObject()
            r20 = r0
            com.google.gson.JsonObject r0 = r9.getAsJsonObject()
            com.google.gson.JsonElement r0 = r0.get(r8)
            java.lang.String r0 = r0.getAsString()
            boolean r21 = android.text.TextUtils.isEmpty(r29)
            r22 = r9
            java.lang.String r9 = " found"
            if (r21 != 0) goto L_0x0169
            r21 = r10
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r10.append(r3)
            r23 = r11
            char r11 = com.sec.internal.constants.Mno.MVNO_DELIMITER
            r10.append(r11)
            r10.append(r4)
            java.lang.String r10 = r10.toString()
            boolean r11 = r0.equalsIgnoreCase(r10)
            if (r11 == 0) goto L_0x013f
            r14 = r19
            java.lang.String r11 = r1.LOG_TAG
            int r4 = r1.mPhoneId
            r24 = r10
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            r20 = r14
            java.lang.String r14 = "loadGlobalSettings - mvnoname on json:"
            r10.append(r14)
            r10.append(r0)
            r10.append(r9)
            java.lang.String r9 = r10.toString()
            com.sec.internal.log.IMSLog.d(r11, r4, r9)
            r14 = r20
            goto L_0x01a4
        L_0x013f:
            r24 = r10
            boolean r4 = r0.equalsIgnoreCase(r3)
            if (r4 == 0) goto L_0x0195
            r14 = r19
            java.lang.String r4 = r1.LOG_TAG
            int r10 = r1.mPhoneId
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r25 = r14
            java.lang.String r14 = "loadGlobalSettings - primary mnoname on json:"
            r11.append(r14)
            r11.append(r0)
            r11.append(r9)
            java.lang.String r9 = r11.toString()
            com.sec.internal.log.IMSLog.d(r4, r10, r9)
            r14 = r25
            goto L_0x0195
        L_0x0169:
            r21 = r10
            r23 = r11
            boolean r4 = r0.equalsIgnoreCase(r3)
            if (r4 == 0) goto L_0x0195
            r14 = r19
            java.lang.String r4 = r1.LOG_TAG
            int r10 = r1.mPhoneId
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            r20 = r14
            java.lang.String r14 = "loadGlobalSettings - mnoname on json:"
            r11.append(r14)
            r11.append(r0)
            r11.append(r9)
            java.lang.String r9 = r11.toString()
            com.sec.internal.log.IMSLog.d(r4, r10, r9)
            r14 = r20
            goto L_0x01a4
        L_0x0195:
            r4 = r29
            r0 = r20
            r10 = r21
            r11 = r23
            goto L_0x00d5
        L_0x01a0:
            r21 = r10
            r23 = r11
        L_0x01a4:
            boolean r0 = r14.isJsonNull()
            if (r0 == 0) goto L_0x01ce
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.GENERIC
            if (r7 != r0) goto L_0x01c3
            com.google.gson.JsonObject r0 = new com.google.gson.JsonObject
            r0.<init>()
            r0.addProperty(r8, r3)
            r4 = r0
            com.sec.internal.ims.settings.ImsAutoUpdate r9 = r1.mAutoUpdate
            if (r9 == 0) goto L_0x01c1
            com.google.gson.JsonElement r4 = r9.getUpdatedGlobalSetting(r4)
            r14 = r4
            goto L_0x01c2
        L_0x01c1:
            r14 = r4
        L_0x01c2:
            goto L_0x01d6
        L_0x01c3:
            java.lang.String r0 = r1.LOG_TAG
            int r4 = r1.mPhoneId
            java.lang.String r9 = "load: No matched setting load default setting"
            com.sec.internal.log.IMSLog.e(r0, r4, r9)
            r14 = r13
            goto L_0x01d6
        L_0x01ce:
            com.sec.internal.ims.settings.ImsAutoUpdate r0 = r1.mAutoUpdate
            if (r0 == 0) goto L_0x01d6
            com.google.gson.JsonElement r14 = r0.getUpdatedGlobalSetting(r14)
        L_0x01d6:
            com.google.gson.JsonElement r0 = com.sec.internal.helper.JsonUtil.merge(r12, r14)
            goto L_0x01e7
        L_0x01db:
            r17 = r0
            r18 = r9
            r21 = r10
            r23 = r11
            com.google.gson.JsonElement r0 = com.sec.internal.helper.JsonUtil.merge(r12, r13)
        L_0x01e7:
            com.google.gson.JsonObject r4 = r0.getAsJsonObject()
            int r9 = r1.mPhoneId
            java.lang.String r9 = com.sec.internal.helper.OmcCode.getNWCode(r9)
            java.lang.String r10 = "XAS"
            boolean r9 = r10.equals(r9)
            if (r9 == 0) goto L_0x01fc
            r1.overwriteXasGlobalSettings(r4)
        L_0x01fc:
            r1.save((com.google.gson.JsonObject) r4)
            com.sec.internal.constants.Mno r9 = com.sec.internal.constants.Mno.DEFAULT
            if (r7 == r9) goto L_0x0208
            if (r2 == 0) goto L_0x0208
            r1.setInitialSettings(r4, r6)
        L_0x0208:
            int r9 = r1.mPhoneId
            android.content.Context r10 = r1.mContext
            java.lang.String r11 = "globalsettings"
            r14 = 0
            android.content.SharedPreferences r9 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r9, r10, r11, r14, r14)
            android.content.SharedPreferences$Editor r9 = r9.edit()
            r10 = 1
            java.lang.String r11 = "loaded"
            r9.putBoolean(r11, r10)
            int r10 = r1.mPhoneId
            java.lang.String r10 = com.sec.internal.helper.OmcCode.getNWCode(r10)
            java.lang.String r11 = "nwcode"
            r9.putString(r11, r10)
            r9.putString(r8, r3)
            java.lang.String r8 = "cscimssettingtype"
            r9.putInt(r8, r5)
            java.lang.String r8 = "hassim"
            r9.putBoolean(r8, r2)
            java.lang.Boolean r8 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            boolean r8 = r8.booleanValue()
            java.lang.String r10 = "gcfmode"
            r9.putBoolean(r10, r8)
            java.lang.String r8 = r26.saveBuildInfo()
            java.lang.String r10 = "buildinfo"
            r9.putString(r10, r8)
            java.lang.String r8 = "imsi"
            java.lang.String r10 = r6.getAsString(r8)
            boolean r11 = android.text.TextUtils.isEmpty(r10)
            if (r11 != 0) goto L_0x025a
            r9.putString(r8, r10)
        L_0x025a:
            r9.apply()
            return
        L_0x025e:
            r0 = move-exception
            r16 = r8
            r4 = r0
            goto L_0x027c
        L_0x0263:
            r0 = move-exception
            r16 = r8
            goto L_0x026b
        L_0x0267:
            r0 = move-exception
            r4 = r0
            goto L_0x027c
        L_0x026a:
            r0 = move-exception
        L_0x026b:
            r4 = r0
            r4.printStackTrace()     // Catch:{ all -> 0x0267 }
            if (r8 == 0) goto L_0x027b
            r8.close()     // Catch:{ IOException -> 0x0275 }
            goto L_0x027b
        L_0x0275:
            r0 = move-exception
            r9 = r0
            r0 = r9
            r0.printStackTrace()
        L_0x027b:
            return
        L_0x027c:
            if (r8 == 0) goto L_0x0288
            r8.close()     // Catch:{ IOException -> 0x0282 }
            goto L_0x0288
        L_0x0282:
            r0 = move-exception
            r9 = r0
            r0 = r9
            r0.printStackTrace()
        L_0x0288:
            throw r4
        L_0x0289:
            java.lang.String r0 = r1.LOG_TAG
            int r4 = r1.mPhoneId
            java.lang.String r7 = "load: globalSettings is not identified."
            com.sec.internal.log.IMSLog.e(r0, r4, r7)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoBase.loadGlobalSettingsFromJson(boolean, java.lang.String, java.lang.String, int, android.content.ContentValues):void");
    }

    /* access modifiers changed from: protected */
    public void overwriteXasGlobalSettings(JsonObject target) {
        target.addProperty(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, DiagnosisConstants.RCSM_ORST_REGI);
        target.addProperty(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS");
        target.addProperty(GlobalSettingsConstants.SS.DOMAIN, "ps_only_psregied");
    }

    /* access modifiers changed from: protected */
    public void setInitialSettings(JsonObject mergedGlobalSettings, ContentValues mnoinfo) {
        int i = 0;
        if (ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId) == -1) {
            int voiceCallType = mergedGlobalSettings.get(GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED).getAsBoolean() ? 0 : 1;
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, voiceCallType, this.mPhoneId);
            this.mEventLog.logAndAdd(this.mPhoneId, "Set voicecall_type to [" + voiceCallType + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_VOICE_DB, this.mPhoneId + ",INITIAL:" + voiceCallType);
        }
        if (ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, this.mPhoneId) == -1) {
            if (!mergedGlobalSettings.get(GlobalSettingsConstants.Registration.VIDEO_DEFAULT_ENABLED).getAsBoolean()) {
                i = 1;
            }
            int videoCallType = i;
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, videoCallType, this.mPhoneId);
            this.mEventLog.logAndAdd(this.mPhoneId, "Set videocall_type to [" + videoCallType + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_VIDEO_DB, this.mPhoneId + ",INITIAL:" + videoCallType);
        }
        if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == -1) {
            int rcsUserSetting = mergedGlobalSettings.get(GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED).getAsInt();
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, rcsUserSetting, this.mPhoneId);
            this.mEventLog.logAndAdd(this.mPhoneId, "Set rcs_user_setting to [" + rcsUserSetting + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_RCS_DB, this.mPhoneId + ",SET INITIAL RCS SETTING:" + rcsUserSetting);
        }
    }

    /* access modifiers changed from: protected */
    public void logMnoInfo(ContentValues mnoinfo) {
        ContentValues cv = new ContentValues(mnoinfo);
        if (!TextUtils.isEmpty(cv.getAsString("imsi"))) {
            cv.remove("imsi");
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] updateMno: mnoInfo:" + cv);
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x02e9, code lost:
        r0 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateMno(android.content.ContentValues r33) {
        /*
            r32 = this;
            r14 = r32
            r15 = r33
            java.lang.Object r1 = r14.mLock
            monitor-enter(r1)
            java.lang.String r0 = "hassim"
            r13 = 0
            boolean r0 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r15, r0, r13)     // Catch:{ all -> 0x02eb }
            r12 = r0
            java.lang.String r0 = "mnoname"
            java.lang.String r0 = r15.getAsString(r0)     // Catch:{ all -> 0x02eb }
            r10 = r0
            java.lang.String r0 = "mvnoname"
            java.lang.String r2 = ""
            java.lang.String r11 = com.sec.internal.helper.CollectionUtils.getStringValue(r15, r0, r2)     // Catch:{ all -> 0x02eb }
            java.lang.String r0 = "imsSwitchType"
            int r0 = com.sec.internal.helper.CollectionUtils.getIntValue(r15, r0, r13)     // Catch:{ all -> 0x02eb }
            r9 = r0
            java.lang.String r0 = "imsi"
            java.lang.String r0 = r15.getAsString(r0)     // Catch:{ all -> 0x02eb }
            r8 = r0
            monitor-exit(r1)     // Catch:{ all -> 0x02eb }
            com.sec.internal.helper.SimpleEventLog r0 = r14.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r14.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] updateMno: hasSIM:"
            r1.append(r2)
            r1.append(r12)
            java.lang.String r2 = ", imsSwitchType:"
            r1.append(r2)
            r1.append(r9)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r32.logMnoInfo(r33)
            boolean r7 = r32.getPrevGcEnabled()
            java.lang.String r0 = "globalgcenabled"
            boolean r6 = com.sec.internal.helper.CollectionUtils.getBooleanValue(r15, r0, r13)
            if (r7 == r6) goto L_0x0067
            r1 = 1
            goto L_0x0068
        L_0x0067:
            r1 = r13
        L_0x0068:
            r5 = r1
            r14.setIsGcEnabledChange(r5)
            if (r5 == 0) goto L_0x00ab
            java.lang.String r1 = r14.LOG_TAG
            int r2 = r14.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "updateMno: prevGcEnabled: "
            r3.append(r4)
            r3.append(r7)
            java.lang.String r4 = ", newGcEnabled: "
            r3.append(r4)
            r3.append(r6)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.i(r1, r2, r3)
            r2 = 0
            r3 = -1
            r4 = 0
            r16 = -1
            r17 = 1
            int r18 = r32.readRcsDefaultEnabled()
            r1 = r32
            r19 = r5
            r5 = r16
            r13 = r6
            r6 = r17
            r17 = r7
            r7 = r18
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
            goto L_0x00b0
        L_0x00ab:
            r19 = r5
            r13 = r6
            r17 = r7
        L_0x00b0:
            r14.setPrevGcEnabled(r13)
            com.sec.internal.constants.Mno r18 = com.sec.internal.constants.Mno.fromName(r10)
            int r1 = r14.mPhoneId
            android.content.Context r2 = r14.mContext
            java.lang.String r3 = "globalsettings"
            r4 = 0
            android.content.SharedPreferences r7 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r1, r2, r3, r4, r4)
            java.lang.String r6 = r14.getPreviousImsi(r7)
            android.content.Context r1 = r14.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.RCS_USER_SETTING1
            java.lang.String r2 = r2.getName()
            int r3 = r14.mPhoneId
            int r5 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r1, r2, r3)
            boolean r1 = android.text.TextUtils.isEmpty(r8)
            if (r1 != 0) goto L_0x00e2
            boolean r1 = android.text.TextUtils.equals(r8, r6)
            if (r1 != 0) goto L_0x00e2
            r1 = 1
            goto L_0x00e3
        L_0x00e2:
            r1 = 0
        L_0x00e3:
            r20 = r1
            r14.mMnoinfo = r15
            int r4 = r32.readRcsDefaultEnabled()
            boolean r1 = r32.updateRequires(r33)
            if (r1 != 0) goto L_0x0153
            com.sec.internal.helper.SimpleEventLog r0 = r14.mEventLog
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "simSlot["
            r1.append(r2)
            int r2 = r14.mPhoneId
            r1.append(r2)
            java.lang.String r2 = "] updateMno: update not requires"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            r0.logAndAdd(r1)
            r14.initRcsUserSetting(r5, r4)
            if (r20 == 0) goto L_0x0151
            android.content.SharedPreferences$Editor r0 = r7.edit()
            java.lang.String r1 = "imsi"
            r0.putString(r1, r8)
            com.sec.internal.helper.SimpleEventLog r1 = r14.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "simSlot["
            r2.append(r3)
            int r3 = r14.mPhoneId
            r2.append(r3)
            java.lang.String r3 = "] imsi changed:"
            r2.append(r3)
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r6)
            r2.append(r3)
            java.lang.String r3 = " --> "
            r2.append(r3)
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r8)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            r0.apply()
        L_0x0151:
            r0 = 0
            return r0
        L_0x0153:
            com.sec.internal.helper.SimpleEventLog r1 = r14.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "simSlot["
            r2.append(r3)
            int r3 = r14.mPhoneId
            r2.append(r3)
            java.lang.String r3 = "] updateMno: update requires"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            java.lang.String r3 = r14.getPreviousMno(r7)
            int r2 = r32.readVolteDefaultEnabled()
            r32.reset()
            java.lang.String r1 = r14.LOG_TAG
            int r0 = r14.mPhoneId
            r21 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r22 = r4
            java.lang.String r4 = "updateMno: ["
            r2.append(r4)
            r2.append(r3)
            java.lang.String r4 = "] => ["
            r2.append(r4)
            r2.append(r10)
            java.lang.String r4 = "]"
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.d(r1, r0, r2)
            android.content.Context r0 = r14.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VOLTE_SLOT1
            java.lang.String r1 = r1.getName()
            int r2 = r14.mPhoneId
            int r23 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r0, r1, r2)
            android.content.Context r0 = r14.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r1 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VILTE_SLOT1
            java.lang.String r1 = r1.getName()
            int r2 = r14.mPhoneId
            int r24 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r0, r1, r2)
            r1 = r32
            r4 = r21
            r2 = r18
            r21 = r3
            r3 = r33
            r25 = r4
            r15 = r22
            r4 = r10
            r26 = r5
            r5 = r21
            r22 = r6
            r6 = r23
            r27 = r7
            r7 = r24
            r1.updateSystemSettings(r2, r3, r4, r5, r6, r7)
            java.lang.String r0 = "Bell_CA"
            boolean r0 = r0.equals(r10)
            if (r0 != 0) goto L_0x01fb
            java.lang.String r0 = "Telus_CA"
            boolean r0 = r0.equals(r10)
            if (r0 != 0) goto L_0x01fb
            java.lang.String r0 = "Koodo_CA"
            boolean r0 = r0.equals(r10)
            if (r0 == 0) goto L_0x01f9
            goto L_0x01fb
        L_0x01f9:
            r2 = 0
            goto L_0x0242
        L_0x01fb:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            r0.append(r10)
            java.lang.String r1 = "_CONF_UPDATE"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.content.Context r1 = r14.mContext
            r2 = 0
            android.content.SharedPreferences r1 = r1.getSharedPreferences(r0, r2)
            boolean r3 = r1.getBoolean(r10, r2)
            if (r3 != 0) goto L_0x0242
            java.lang.String r4 = r14.LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            r5.append(r10)
            java.lang.String r6 = ": volte_domestic_default_enabled - force Reset "
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r4, r5)
            android.content.Context r4 = r14.mContext
            r5 = -1
            int r6 = r14.mPhoneId
            com.sec.internal.constants.ims.ImsConstants.SystemSettings.setVoiceCallType(r4, r5, r6)
            android.content.SharedPreferences$Editor r4 = r1.edit()
            r5 = 1
            r4.putBoolean(r10, r5)
            r4.commit()
        L_0x0242:
            java.lang.Object r3 = r14.mLock
            monitor-enter(r3)
            r16 = r8
            r8 = r32
            r28 = r9
            r9 = r12
            r29 = r10
            r30 = r12
            r12 = r28
            r0 = r2
            r31 = r13
            r13 = r33
            r8.loadGlobalSettingsFromJson(r9, r10, r11, r12, r13)     // Catch:{ all -> 0x02e2 }
            monitor-exit(r3)     // Catch:{ all -> 0x02e2 }
            int r8 = r32.readRcsDefaultEnabled()
            int r9 = r32.readVolteDefaultEnabled()
            boolean r1 = r14.mVersionUpdated
            if (r1 == 0) goto L_0x02d9
            if (r15 == r8) goto L_0x0298
            com.sec.internal.helper.SimpleEventLog r1 = r14.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updateMno : rcs_default_enabled: ["
            r2.append(r3)
            r2.append(r15)
            java.lang.String r3 = "] => ["
            r2.append(r3)
            r2.append(r8)
            java.lang.String r3 = "]"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            r2 = 0
            r3 = -1
            r4 = 0
            r5 = -1
            r6 = 1
            r1 = r32
            r7 = r8
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
        L_0x0298:
            r10 = r25
            boolean r1 = r14.needResetVolteAsDefault(r10, r9)
            if (r1 == 0) goto L_0x02d4
            com.sec.internal.helper.SimpleEventLog r1 = r14.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updateMno : volte_domestic_default_enabled: ["
            r2.append(r3)
            r2.append(r10)
            java.lang.String r3 = "] => ["
            r2.append(r3)
            r2.append(r9)
            java.lang.String r3 = "]"
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r2)
            r2 = 1
            r1 = 1
            if (r9 != r1) goto L_0x02ca
            r3 = r0
            goto L_0x02cb
        L_0x02ca:
            r3 = 1
        L_0x02cb:
            r4 = 0
            r5 = -1
            r6 = 0
            r7 = -1
            r1 = r32
            r1.setSettingsFromSp(r2, r3, r4, r5, r6, r7)
        L_0x02d4:
            r14.mVersionUpdated = r0
            r2 = r26
            goto L_0x02e0
        L_0x02d9:
            r10 = r25
            r2 = r26
            r14.initRcsUserSetting(r2, r8)
        L_0x02e0:
            r0 = 1
            return r0
        L_0x02e2:
            r0 = move-exception
            r10 = r25
            r2 = r26
        L_0x02e7:
            monitor-exit(r3)     // Catch:{ all -> 0x02e9 }
            throw r0
        L_0x02e9:
            r0 = move-exception
            goto L_0x02e7
        L_0x02eb:
            r0 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x02eb }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoBase.updateMno(android.content.ContentValues):boolean");
    }

    /* access modifiers changed from: protected */
    public void setPrevGcEnabled(boolean isGcEnabled) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setPrevGcEnabled: " + isGcEnabled);
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        editor.putBoolean("prevGcEnabled", isGcEnabled);
        editor.apply();
    }

    /* access modifiers changed from: protected */
    public boolean getPrevGcEnabled() {
        return ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).getBoolean("prevGcEnabled", false);
    }

    /* access modifiers changed from: protected */
    public void setIsGcEnabledChange(boolean isGcEnabledChange) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setIsGcEnabledChange: " + isGcEnabledChange);
        SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        editor.putBoolean("isGcEnabledChange", isGcEnabledChange);
        editor.apply();
    }

    /* access modifiers changed from: protected */
    public void initRcsUserSetting(int spValue, int globalDefault) {
        int rcs_switch = globalDefault;
        int systemSetting = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -3, this.mPhoneId);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] initRcsUserSetting: system [" + systemSetting + "], sp [" + spValue + "], default [" + globalDefault + "]");
        if (spValue != -1) {
            rcs_switch = spValue;
        }
        IMSLog.c(LogClass.GLOBAL_INIT_RCS_DB, this.mPhoneId + "GLB:" + rcs_switch);
        if (systemSetting != rcs_switch) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, rcs_switch, this.mPhoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void updateSystemSettings(Mno mno, ContentValues mnoinfo, String newMnoname, String prevMnoname, int spValueVolte, int spValueVideo) {
        if (mno.isKor() && !TextUtils.equals(newMnoname, prevMnoname)) {
            spValueVolte = -1;
        }
        boolean isNeedToSetVoLTE = isNeedToBeSetVoLTE(mnoinfo);
        if (removeVolteMenuByCsc()) {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, "reset voice and vt call settings db because of VOICECLLCSC CONFIGOPSTYLEMOBILENETWORKSETTINGMENU Feature");
            isNeedToSetVoLTE = true;
            spValueVolte = -1;
        }
        setSettingsFromSp(isNeedToSetVoLTE, spValueVolte, isNeedToBeSetViLTE(mnoinfo), spValueVideo, false, -1);
    }

    public boolean isNeedToBeSetViLTE(ContentValues mnoinfo) {
        boolean isNeedToSetViLTE = false;
        String newMnoname = mnoinfo.getAsString("mnoname");
        String prevMnoname = getPreviousMno(ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false));
        if (needResetCallSettingBySim(this.mPhoneId)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] reset vt call settings db by simcard change");
            isNeedToSetViLTE = true;
        } else if (!TextUtils.equals(newMnoname, prevMnoname) && !TextUtils.isEmpty(prevMnoname)) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("simSlot[" + this.mPhoneId + "] reset video call settings db by simcard change");
            isNeedToSetViLTE = true;
        }
        Mno mno = Mno.fromName(newMnoname);
        int spValueVideo = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VILTE_SLOT1.getName(), this.mPhoneId);
        SimpleEventLog simpleEventLog3 = this.mEventLog;
        simpleEventLog3.logAndAdd("simSlot[" + this.mPhoneId + "] videocall_type_" + mno.getName() + ": [" + spValueVideo + "]");
        if (DeviceUtil.getGcfMode().booleanValue()) {
            return isNeedToSetViLTE;
        }
        SimpleEventLog simpleEventLog4 = this.mEventLog;
        simpleEventLog4.logAndAdd("simSlot[" + this.mPhoneId + "] NOT Temporal SIM swapped: Set Video DB - " + spValueVideo);
        if (spValueVideo != -1) {
            return true;
        }
        return isNeedToSetViLTE;
    }

    public boolean isNeedToBeSetVoLTE(ContentValues mnoinfo) {
        int spValueVolte = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), this.mPhoneId);
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        String newMnoname = mnoinfo.getAsString("mnoname");
        String prevMnoname = getPreviousMno(sp);
        if (spValueVolte != -1) {
            return true;
        }
        if (needResetCallSettingBySim(this.mPhoneId)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] reset voice call settings db by simcard change");
            return true;
        } else if (TextUtils.equals(newMnoname, prevMnoname) || TextUtils.isEmpty(prevMnoname)) {
            return false;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("simSlot[" + this.mPhoneId + "] reset voice call settings db by simcard change");
            return true;
        }
    }

    public void resetUserSettingAsDefault(boolean isNeedToResetVoice, boolean isNeedToResetVideo, boolean isNeedToResetRcs) {
        if (isNeedToResetVoice) {
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VOICE_DB, this.mPhoneId + ",SET:" + -1);
        }
        if (isNeedToResetVideo) {
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VIDEO_DB, this.mPhoneId + ",SET:" + -1);
        }
        if (isNeedToResetRcs) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_RCS_DB, this.mPhoneId + ",SET:" + -1);
        }
    }

    /* access modifiers changed from: protected */
    public void setSettingsFromSp(boolean isNeedToSetVoice, int spValueVoice, boolean isNeedToSetVideo, int spValueVideo, boolean isNeedToSetRcs, int spValueRcs) {
        if (isNeedToSetVoice) {
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, spValueVoice, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VOICE_DB, this.mPhoneId + ",SET:" + spValueVoice);
        }
        if (isNeedToSetVideo) {
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, spValueVideo, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VIDEO_DB, this.mPhoneId + ",SET:" + spValueVideo);
        }
        if (isNeedToSetRcs) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, spValueRcs, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_RCS_DB, this.mPhoneId + ",SET RCS DB:" + spValueRcs);
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRequires(ContentValues mnoinfo) {
        Boolean hasSim;
        String newMnoname;
        String newMvnoname;
        String newNwName;
        Integer imsSwitchType;
        synchronized (this.mLock) {
            hasSim = mnoinfo.getAsBoolean(ISimManager.KEY_HAS_SIM);
            if (hasSim == null) {
                hasSim = false;
            }
            newMnoname = mnoinfo.getAsString("mnoname");
            newMvnoname = mnoinfo.getAsString(ISimManager.KEY_MVNO_NAME);
            newNwName = mnoinfo.getAsString(ISimManager.KEY_NW_NAME);
            if (newNwName == null) {
                newNwName = "";
            }
            imsSwitchType = mnoinfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
            if (imsSwitchType == null) {
                imsSwitchType = 0;
            }
        }
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        if (isVersionUpdated()) {
            Log.d(this.LOG_TAG, "PDA or CSC version changed");
            this.mVersionUpdated = true;
            return true;
        } else if (CscParser.isCscChanged(this.mContext, this.mPhoneId)) {
            this.mEventLog.logAndAdd("update Requires: CSC Info Changed");
            this.mCscChanged = true;
            return true;
        } else if (!getPreviousNwCode(sp).equals(OmcCode.getNWCode(this.mPhoneId))) {
            Log.d(this.LOG_TAG, "update Requires: different omc_nw code");
            return true;
        } else if (!getPreviousMno(sp).equals(newMnoname)) {
            Log.d(this.LOG_TAG, "update Requires: different mnoname");
            return true;
        } else if (!getPreviousMvno(sp).equals(newMvnoname)) {
            Log.d(this.LOG_TAG, "update Requires: different MVNO name");
            return true;
        } else if (!getPreviousNwName(sp).equals(newNwName)) {
            Log.d(this.LOG_TAG, "update Requires: different network name");
            return true;
        } else if (getPreviousHasSim(sp) != hasSim.booleanValue()) {
            Log.d(this.LOG_TAG, "update Requires: hasSim Changed " + hasSim);
            return true;
        } else {
            int prevCscImsSetting = getPreviousCscImsSettingType(sp);
            if (prevCscImsSetting != imsSwitchType.intValue()) {
                Log.d(this.LOG_TAG, "update Requires: cscImsSettingType changed " + prevCscImsSetting + " => " + imsSwitchType);
                return true;
            }
            ImsAutoUpdate imsAutoUpdate = this.mAutoUpdate;
            if (imsAutoUpdate == null || !imsAutoUpdate.isUpdateNeeded()) {
                Log.d(this.LOG_TAG, "update not requires: same mno, same version");
                return false;
            }
            Log.d(this.LOG_TAG, "imsupdate changed.");
            return true;
        }
    }

    public void reset() {
        synchronized (this.mLock) {
            if (isLoaded()) {
                SharedPreferences.Editor editor = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
                editor.clear();
                editor.putBoolean("loaded", false);
                editor.apply();
            }
        }
    }

    public void unregisterIntentReceiver() {
    }

    /* access modifiers changed from: protected */
    public String getPreviousNwCode(SharedPreferences sp) {
        return sp.getString("nwcode", OmcCode.getNWCode(this.mPhoneId));
    }

    /* access modifiers changed from: protected */
    public boolean getPreviousHasSim(SharedPreferences sp) {
        return sp.getBoolean(ISimManager.KEY_HAS_SIM, false);
    }

    /* access modifiers changed from: protected */
    public int getPreviousCscImsSettingType(SharedPreferences sp) {
        return sp.getInt("cscimssettingtype", -1);
    }

    /* access modifiers changed from: protected */
    public boolean getPreviousGcfMode(SharedPreferences sp) {
        return sp.getBoolean("gcfmode", false);
    }

    /* access modifiers changed from: protected */
    public String getPreviousMno(SharedPreferences sp) {
        return sp.getString("mnoname", "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousMvno(SharedPreferences sp) {
        return sp.getString(ISimManager.KEY_MVNO_NAME, "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousNwName(SharedPreferences sp) {
        return sp.getString(ISimManager.KEY_NW_NAME, "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousImsi(SharedPreferences sp) {
        return sp.getString("imsi", "");
    }

    public String getPreviousMno() {
        return ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "mnoname", "");
    }

    public boolean getGlobalGcEnabled() {
        return ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, ISimManager.KEY_GLOBALGC_ENABLED, false);
    }

    /* access modifiers changed from: protected */
    public boolean isVersionUpdated() {
        String pdaVer = SemSystemProperties.get("ro.build.PDA", "");
        String cscVer = SemSystemProperties.get("ril.official_cscver", "");
        String prevVer = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "buildinfo", "");
        if (!prevVer.equals(pdaVer + "_" + cscVer)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public String saveBuildInfo() {
        String pdaVer = SemSystemProperties.get("ro.build.PDA", "");
        String cscVer = SemSystemProperties.get("ril.official_cscver", "");
        return pdaVer + "_" + cscVer;
    }

    public boolean removeVolteMenuByCsc() {
        String featureTag;
        if (DeviceUtil.isUnifiedSalesCodeInTSS()) {
            featureTag = SemCarrierFeature.getInstance().getString(this.mPhoneId, "CarrierFeature_VoiceCall_ConfigOpStyleMobileNetworkSettingMenu", "", false);
        } else {
            featureTag = ImsCscFeature.getInstance().getString(this.mPhoneId, CscFeatureTagCommon.TAG_CSCFEATURE_VOICECALL_CONFIGOPSTYLEMOBILENETWORKSETTINGMENU).toUpperCase(Locale.US);
        }
        if (!featureTag.contains("-VOLTECALL") || SimManagerFactory.isOutboundSim(this.mPhoneId)) {
            return false;
        }
        return true;
    }

    public void dump() {
        this.mEventLog.dump();
        SharedPreferences sp = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        if (sp != null && sp.getBoolean("loaded", false)) {
            Map<String, ?> settings = sp.getAll();
            settings.remove("imsi");
            IMSLog.increaseIndent(this.LOG_TAG);
            IMSLog.dump(this.LOG_TAG, this.mPhoneId, "\nLast values of GlobalSettings:");
            IMSLog.increaseIndent(this.LOG_TAG);
            for (Map.Entry<String, ?> entry : settings.entrySet()) {
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.dump(str, i, entry.getKey() + " = [" + entry.getValue() + "]");
            }
            IMSLog.decreaseIndent(this.LOG_TAG);
            IMSLog.decreaseIndent(this.LOG_TAG);
        }
    }

    /* access modifiers changed from: protected */
    public int readVolteDefaultEnabled() {
        return Integer.parseInt(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED, "-1"));
    }

    /* access modifiers changed from: protected */
    public int readRcsDefaultEnabled() {
        return Integer.parseInt(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED, "-1"));
    }
}
