package com.sec.internal.ims.settings;

import android.content.Context;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Iterator;

public class ImsAutoUpdate {
    public static final String GLOBALSETTINGS_UPDATE = "globalsettings_update";
    public static final String IMSPROFILE_UPDATE = "imsprofile_update";
    private static final String IMSSWITCH_UPDATE = "imsswitch_update";
    private static final String IMSUPDATE_JSON_FILE = "imsupdate.json";
    private static final String LOG_TAG = ImsAutoUpdate.class.getSimpleName();
    private static final String MNOMAPUPDATE_JSON_FILE = "mnomapupdate.json";
    private static final String MNOMAP_UPDATE = "mnomap_update";
    private static final String PROVIDERSETTINGS_UPDATE = "providersettings_update";
    private static final String RCSRPOLICY_UPDATE = "rcspolicy_update";
    public static final int RESOURCE_CARRIER_FEATURE = 4;
    public static final int RESOURCE_DOWNLOAD = 0;
    public static final int RESOURCE_IMSUPDATE = 1;
    public static final int RESOURCE_MNOMAPUPDATE = 2;
    public static final int RESOURCE_MOBILITY_GLOBAL = 3;
    private static final String SMS_SETTINGS_UPDATE = "sms_settings_update";
    public static final String TAG_DEFAULT_RCS_POLICY = "default_rcs_policy";
    public static final String TAG_DEFAULT_UP_POLICY = "default_up_policy";
    public static final String TAG_GC_BLOCK_MCC_LIST = "gc_block_mcc_list";
    public static final String TAG_GLOBALSETTING = "globalsetting";
    public static final String TAG_GLOBALSETTINGS_DEFAULT = "defaultsetting";
    public static final String TAG_GLOBALSETTINGS_NOHIT = "nohitsetting";
    public static final String TAG_IMSSWITCH = "imsswitch";
    public static final String TAG_MNOMAP_ADD = "add_mnomap";
    public static final String TAG_MNOMAP_REMOVE = "remove_mnomap";
    public static final String TAG_POLICY_NAME = "policy_name";
    public static final String TAG_PROFILE = "profile";
    public static final String TAG_RCS_POLICY = "rcs_policy";
    public static final String TAG_REPLACE_GC_BLOCK_MCC_LIST = "replace_gc_block_mcc_list";
    private static final String UPDATE_FILE_PATH_CSC = "/system/csc";
    private static final HashMap<Integer, ImsAutoUpdate> sInstances = new HashMap<>();
    private JsonElement mCarrierUpdate = JsonNull.INSTANCE;
    private Context mContext;
    private String mCurrentHash = "";
    private boolean mHashChanged = false;
    private HashManager mHashManager;
    private ImsSimMobilityUpdate mImsMobilityUpdate;
    private boolean mLoaded = false;
    public JsonElement mMnomapUpdate = JsonNull.INSTANCE;
    private String mNote = "";
    private int mPhoneId;
    private handleSmkConfig mSmkConfig;
    private JsonElement mUpdate = JsonNull.INSTANCE;
    private boolean mUpdatedGlobalSettings = false;
    private boolean mUpdatedImsProfile = false;
    private boolean mUpdatedImsSwitch = false;

    protected ImsAutoUpdate(Context ctx, int phoneId) {
        this.mHashManager = HashManager.getInstance(ctx, phoneId);
        this.mImsMobilityUpdate = ImsSimMobilityUpdate.getInstance(ctx);
        handleSmkConfig handlesmkconfig = new handleSmkConfig(ctx);
        this.mSmkConfig = handlesmkconfig;
        handlesmkconfig.load();
        this.mContext = ctx;
        this.mPhoneId = phoneId;
    }

    public static ImsAutoUpdate getInstance(Context ctx, int phoneId) {
        synchronized (sInstances) {
            if (sInstances.containsKey(Integer.valueOf(phoneId))) {
                ImsAutoUpdate imsAutoUpdate = sInstances.get(Integer.valueOf(phoneId));
                return imsAutoUpdate;
            }
            sInstances.put(Integer.valueOf(phoneId), new ImsAutoUpdate(ctx, phoneId));
            sInstances.get(Integer.valueOf(phoneId)).copyMnoMapUpdateToInternal();
            sInstances.get(Integer.valueOf(phoneId)).checkLoaded();
            ImsAutoUpdate imsAutoUpdate2 = sInstances.get(Integer.valueOf(phoneId));
            return imsAutoUpdate2;
        }
    }

    public boolean checkLoaded() {
        if (!this.mLoaded) {
            this.mLoaded = (loadImsAutoUpdate() && !this.mUpdate.isJsonNull()) || getSmkConfig() != null;
        }
        return this.mLoaded;
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    private boolean loadImsAutoUpdate() {
        JsonReader reader;
        boolean result = false;
        String updateFilePath = getUpdateFilePath();
        String str = LOG_TAG;
        Log.d(str, "Use imsupdate file on " + updateFilePath);
        File file = new File(updateFilePath);
        if (!file.exists() || file.length() <= 0) {
            Log.e(LOG_TAG, "imsupdate.json not found.");
            this.mCurrentHash = "";
            result = false;
        } else {
            try {
                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                try {
                    reader = new JsonReader(new BufferedReader(new InputStreamReader(fis)));
                    JsonElement parse = new JsonParser().parse(reader);
                    this.mUpdate = parse;
                    if (!parse.isJsonNull() && this.mUpdate.isJsonObject()) {
                        result = true;
                        JsonElement note = this.mUpdate.getAsJsonObject().get("note");
                        if (note != null && !note.isJsonNull()) {
                            this.mNote = note.getAsString();
                            String str2 = LOG_TAG;
                            Log.d(str2, "imsupdate is ready : " + this.mNote);
                        }
                    }
                    byte[] content = new byte[((int) file.length())];
                    fis.getChannel().position(0);
                    int cnt = fis.read(content);
                    if (cnt <= 0) {
                        String str3 = LOG_TAG;
                        Log.e(str3, "Failed to read imsupdate.json! Got [" + cnt + "]");
                    }
                    try {
                        this.mCurrentHash = this.mHashManager.getHash(content);
                    } catch (Exception e) {
                    }
                    reader.close();
                    fis.close();
                } catch (Throwable reader2) {
                    fis.close();
                    throw reader2;
                }
            } catch (JsonParseException | IOException e2) {
                e2.printStackTrace();
                Log.e(LOG_TAG, "imsupdate.json parsing fail.");
                result = false;
            } catch (Throwable th) {
                reader2.addSuppressed(th);
            }
        }
        this.mHashChanged = this.mHashManager.isHashChanged(HashManager.HASH_IMSUPDATE, this.mCurrentHash);
        String str4 = LOG_TAG;
        Log.d(str4, "loadImsAutoUpdate: hash changed [" + this.mHashChanged + "]");
        return result;
        throw th;
    }

    public boolean loadCarrierFeature(int phoneId) {
        int carrierId = SemCarrierFeature.getInstance().getCarrierId(phoneId, false);
        int forceUseProperty = SemSystemProperties.getInt(ImsConstants.SystemProperties.CARRIERFEATURE_FORCE_USE, -1);
        String str = LOG_TAG;
        Log.d(str, "loadCarrierFeature phoneId : " + phoneId + " carrierId : " + carrierId + " forceProp : " + forceUseProperty);
        if (carrierId == -1 && forceUseProperty == -1) {
            return false;
        }
        try {
            JsonParser parser = new JsonParser();
            String updateList = SemCarrierFeature.getInstance().getString(phoneId, "CarrierFeature_IMS_ImsUpdate", "", false);
            if (TextUtils.isEmpty(updateList)) {
                Log.e(LOG_TAG, "carrierfeature was not found.");
                return false;
            }
            JsonElement je = parser.parse(updateList);
            if (!JsonUtil.isValidJsonElement(je)) {
                return false;
            }
            String str2 = LOG_TAG;
            Log.d(str2, "Successfully get carrier feature : " + je.toString());
            this.mCarrierUpdate = je;
            return true;
        } catch (Exception e) {
            String str3 = LOG_TAG;
            Log.e(str3, "Problem on Carrier feature : " + e);
            return false;
        }
    }

    public File getMnoMapUpdateFile(boolean internal) {
        if (internal) {
            return new File(this.mContext.getFilesDir(), MNOMAPUPDATE_JSON_FILE);
        }
        return new File(getMnomapUpdateFilePath());
    }

    public void copyMnoMapUpdateToInternal() {
        File fromCsc = getMnoMapUpdateFile(false);
        if (fromCsc == null) {
            Log.d(LOG_TAG, "There are no mnomapupdate.json on CSC");
        } else {
            FileUtils.copyFile(fromCsc, getMnoMapUpdateFile(true));
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public boolean loadMnomapAutoUpdate() {
        JsonReader reader;
        boolean result = false;
        File file = getMnoMapUpdateFile(true);
        if (!file.exists() || file.length() <= 0) {
            Log.e(LOG_TAG, "mnomapupdate.json not found.");
            return false;
        }
        try {
            reader = new JsonReader(new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath()))));
            JsonElement parse = new JsonParser().parse(reader);
            this.mMnomapUpdate = parse;
            if (!parse.isJsonNull() && this.mMnomapUpdate.isJsonObject()) {
                result = true;
                JsonElement note = this.mMnomapUpdate.getAsJsonObject().get("note");
                if (note != null && !note.isJsonNull()) {
                    this.mNote = note.getAsString();
                    String str = LOG_TAG;
                    Log.d(str, "mnomapupdate is ready : " + this.mNote);
                }
            }
            reader.close();
            return result;
        } catch (JsonParseException | IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "mnomapupdate.json parsing fail.");
            return false;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void saveHash() {
        this.mHashManager.saveHash(HashManager.HASH_IMSUPDATE, this.mCurrentHash);
        this.mHashManager.saveMemo(HashManager.HASH_IMSUPDATE, this.mNote);
        this.mHashChanged = false;
    }

    /* access modifiers changed from: protected */
    public String getUpdateFilePath() {
        if (!OmcCode.isOmcModel()) {
            return "/system/csc/imsupdate.json";
        }
        String omcPath = OmcCode.getNWPath(this.mPhoneId);
        if (omcPath.contains(UPDATE_FILE_PATH_CSC)) {
            return "/system/csc/imsupdate.json";
        }
        String path = omcPath + "/" + IMSUPDATE_JSON_FILE;
        if (new File(path).exists()) {
            return path;
        }
        return OmcCode.getEtcPath() + "/" + IMSUPDATE_JSON_FILE;
    }

    public String getMnomapUpdateFilePath() {
        if (!OmcCode.isOmcModel()) {
            return "/system/csc/mnomapupdate.json";
        }
        String omcPath = OmcCode.getNWPath(this.mPhoneId);
        if (omcPath.contains(UPDATE_FILE_PATH_CSC)) {
            return "/system/csc/mnomapupdate.json";
        }
        String path = omcPath + "/" + MNOMAPUPDATE_JSON_FILE;
        if (new File(path).exists()) {
            return path;
        }
        return OmcCode.getEtcPath() + "/" + MNOMAPUPDATE_JSON_FILE;
    }

    public boolean isUpdateNeeded() {
        checkLoaded();
        return this.mHashChanged || this.mSmkConfig.hasNewSmkConfig();
    }

    public JsonElement selectResource(int source) {
        if (source == 0 && this.mSmkConfig != null) {
            return getSmkConfig() == null ? JsonNull.INSTANCE : getSmkConfig();
        }
        if (source == 1) {
            return this.mUpdate;
        }
        if (source == 2) {
            return this.mMnomapUpdate;
        }
        if (source == 3) {
            return this.mImsMobilityUpdate.getMobilityGlobalSettings();
        }
        if (source == 4) {
            return this.mCarrierUpdate;
        }
        return JsonNull.INSTANCE;
    }

    private String sourceToString(int source) {
        if (source == 0) {
            return "SMK";
        }
        if (source == 1) {
            return "IMSUPDATE";
        }
        if (source == 2) {
            return "MNOUPDATE";
        }
        if (source == 3) {
            return "MOBILITY_GLOBAL";
        }
        return "UNKNOWN update source " + source;
    }

    public JsonElement getImsProfileUpdate(int source, String tag) {
        JsonElement jsonSource = selectResource(source);
        try {
            if (jsonSource.getAsJsonObject().has(IMSPROFILE_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(IMSPROFILE_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(tag)) {
                    return update.get(tag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getImsProfileUpdate :  source : " + sourceToString(source) + " tag :  " + tag + " message : " + e.getMessage());
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getImsSwitches(int source, String tag) {
        JsonElement jsonSource = selectResource(source);
        try {
            if (jsonSource.getAsJsonObject().has(IMSSWITCH_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(IMSSWITCH_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(tag)) {
                    return update.get(tag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getImsSwitches :  source : " + sourceToString(source) + " tag :  " + tag + " message : " + e.getMessage());
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getGlobalSettings(int source, String tag) {
        JsonElement jsonSource = selectResource(source);
        try {
            if (jsonSource.getAsJsonObject().has(GLOBALSETTINGS_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(GLOBALSETTINGS_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(tag)) {
                    return update.get(tag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getGlobalSettings :  source : " + sourceToString(source) + " tag :  " + tag + " message : " + e.getMessage());
        }
        return JsonNull.INSTANCE;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0012, code lost:
        r1 = r0.getAsJsonArray();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getGlobalSettingsSpecificParam(int r7, java.lang.String r8, java.lang.String r9) {
        /*
            r6 = this;
            java.lang.String r0 = "globalsetting"
            com.google.gson.JsonElement r0 = r6.getGlobalSettings(r7, r0)
            boolean r1 = r0.isJsonNull()
            if (r1 != 0) goto L_0x004a
            boolean r1 = r0.isJsonArray()
            if (r1 == 0) goto L_0x004a
            com.google.gson.JsonArray r1 = r0.getAsJsonArray()
            int r2 = getIndexWithMnoname(r1, r8)
            r3 = -1
            if (r2 == r3) goto L_0x004a
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Found Globalsetting for : "
            r4.append(r5)
            r4.append(r8)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r3, r4)
            com.google.gson.JsonElement r3 = r1.get(r2)
            com.google.gson.JsonObject r3 = r3.getAsJsonObject()
            boolean r4 = r3.has(r9)
            if (r4 == 0) goto L_0x004a
            com.google.gson.JsonElement r4 = r3.get(r9)
            java.lang.String r4 = r4.getAsString()
            return r4
        L_0x004a:
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsAutoUpdate.getGlobalSettingsSpecificParam(int, java.lang.String, java.lang.String):java.lang.String");
    }

    public JsonElement getMnomap(int source, String tag) {
        JsonElement jsonSource = selectResource(source);
        try {
            if (jsonSource.getAsJsonObject().has(MNOMAP_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(MNOMAP_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(tag)) {
                    if ("[]".equals(update.get(tag).toString())) {
                        return JsonNull.INSTANCE;
                    }
                    return update.get(tag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getMnomap :  source : " + sourceToString(source) + " tag :  " + tag + " message : " + e.getMessage());
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement getProviderSettings(int source, String tag) {
        JsonElement jsonSource = selectResource(source);
        try {
            if (jsonSource.getAsJsonObject().has(PROVIDERSETTINGS_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(PROVIDERSETTINGS_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(tag)) {
                    if ("[]".equals(update.get(tag).toString())) {
                        return JsonNull.INSTANCE;
                    }
                    return update.get(tag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "Failed to getProviderSettings :  source : " + sourceToString(source) + " tag :  " + tag + " message : " + e.getMessage());
        }
        return JsonNull.INSTANCE;
    }

    public JsonElement applyImsProfileUpdate(JsonArray baseProfiles) {
        JsonElement profiles;
        JsonNull jsonNull = JsonNull.INSTANCE;
        if (isForceSMKUpdate().booleanValue()) {
            Log.d(LOG_TAG, "SMK ForceMode - ImsProfile");
            profiles = applyImsProfileUpdate(applyImsProfileUpdate(applyImsProfileUpdate(baseProfiles, 1).getAsJsonArray(), 4).getAsJsonArray(), 0);
        } else {
            profiles = applyImsProfileUpdate(applyImsProfileUpdate(applyImsProfileUpdate(baseProfiles, 0).getAsJsonArray(), 1).getAsJsonArray(), 4);
        }
        this.mUpdatedImsProfile = true;
        if ((this.mUpdatedGlobalSettings || this.mUpdatedImsSwitch) && this.mHashChanged) {
            saveHash();
        }
        return profiles;
    }

    public JsonElement applyImsProfileUpdate(JsonArray baseProfiles, int source) {
        JsonArray orgProfiles = (JsonArray) JsonUtil.deepCopy(baseProfiles, JsonArray.class);
        JsonElement update = getImsProfileUpdate(source, "profile");
        if (!baseProfiles.isJsonNull() && !update.isJsonNull() && update.isJsonArray()) {
            Iterator it = update.getAsJsonArray().iterator();
            while (it.hasNext()) {
                JsonElement p = (JsonElement) it.next();
                JsonObject prof = p.getAsJsonObject();
                if (prof.has("name") && prof.has("mnoname")) {
                    String name = prof.get("name").getAsString();
                    String mnoname = prof.get("mnoname").getAsString();
                    int index = getIndexWithNames(orgProfiles, name, mnoname);
                    if (index == -1) {
                        orgProfiles.add(p);
                        String str = LOG_TAG;
                        Log.d(str, "add imsprofile for " + mnoname + " " + name);
                    } else {
                        JsonElement tmp = prof.remove("mnoname");
                        JsonElement updatedProf = JsonUtil.merge(orgProfiles.get(index), p);
                        if (!updatedProf.isJsonNull()) {
                            orgProfiles.set(index, updatedProf);
                        }
                        if (tmp != null && !tmp.isJsonNull()) {
                            prof.add("mnoname", tmp);
                        }
                    }
                }
            }
        }
        return orgProfiles;
    }

    public JsonElement getUpdatedGlobalSetting(JsonElement globalElement) {
        JsonElement globalsetting;
        if (isForceSMKUpdate().booleanValue()) {
            Log.d(LOG_TAG, "SMK ForceMode - GlobalSettings");
            globalsetting = applyGlobalSettingUpdate(applyGlobalSettingUpdate(applyGlobalSettingUpdate(applyGlobalSettingUpdate(globalElement, 3), 1), 4), 0);
        } else {
            globalsetting = applyGlobalSettingUpdate(applyGlobalSettingUpdate(applyGlobalSettingUpdate(applyGlobalSettingUpdate(globalElement, 3), 0), 1), 4);
        }
        this.mUpdatedGlobalSettings = true;
        if (this.mUpdatedImsProfile && this.mHashChanged) {
            saveHash();
        }
        return globalsetting;
    }

    public JsonElement applyGlobalSettingUpdate(JsonElement globalElement, int source) {
        int index;
        if (!JsonUtil.isValidJsonElement(globalElement)) {
            Log.d(LOG_TAG, "Not a valid GlobalElement.");
            return globalElement;
        }
        JsonElement updatedProf = globalElement;
        String orgMnoName = globalElement.getAsJsonObject().get("mnoname").getAsString();
        JsonElement updateDiff = getGlobalSettings(source, TAG_GLOBALSETTING);
        if (JsonUtil.isValidJsonElement(updateDiff) && (index = getIndexWithMnoname(updateDiff.getAsJsonArray(), orgMnoName)) != -1) {
            String str = LOG_TAG;
            Log.d(str, "Found Globalsetting for : " + orgMnoName);
            updatedProf = JsonUtil.merge(globalElement, updateDiff.getAsJsonArray().get(index));
        }
        return updatedProf != JsonNull.INSTANCE ? updatedProf : globalElement;
    }

    public JsonElement getUpdatedImsSwitch(JsonElement imsswitchElement) {
        JsonElement imsswitch = applyImsSwitchUpdate(applyImsSwitchUpdate(imsswitchElement, 1), 4);
        this.mUpdatedImsSwitch = true;
        if (this.mUpdatedImsProfile && this.mHashChanged) {
            saveHash();
        }
        return imsswitch;
    }

    public JsonElement applyImsSwitchUpdate(JsonElement imsswitchElement, int source) {
        int index;
        if (!JsonUtil.isValidJsonElement(imsswitchElement)) {
            Log.d(LOG_TAG, "Not a valid imsswitchElement.");
            return imsswitchElement;
        }
        JsonElement updatedProf = imsswitchElement;
        String orgMnoName = imsswitchElement.getAsJsonObject().get("mnoname").getAsString();
        JsonElement updateDiff = getImsSwitches(source, "imsswitch");
        if (JsonUtil.isValidJsonElement(updateDiff) && (index = getIndexWithMnoname(updateDiff.getAsJsonArray(), orgMnoName)) != -1) {
            String str = LOG_TAG;
            Log.d(str, "Found ImsSwitch for : " + orgMnoName);
            updatedProf = JsonUtil.merge(imsswitchElement, updateDiff.getAsJsonArray().get(index));
        }
        return updatedProf != JsonNull.INSTANCE ? updatedProf : imsswitchElement;
    }

    public JsonElement applyNohitSettingUpdate(JsonElement nohitElement) {
        JsonElement smkUpdatedNohit = getGlobalSettings(0, TAG_GLOBALSETTINGS_NOHIT);
        JsonElement autoUpdatedNohit = getGlobalSettings(1, TAG_GLOBALSETTINGS_NOHIT);
        JsonElement carrierFeatureNohit = getGlobalSettings(4, TAG_GLOBALSETTINGS_NOHIT);
        if (isForceSMKUpdate().booleanValue()) {
            Log.d(LOG_TAG, "SMK ForceMode - Nohit");
            if (JsonUtil.isValidJsonElement(autoUpdatedNohit)) {
                JsonElement mergedNohit = JsonUtil.merge(nohitElement, autoUpdatedNohit);
                if (JsonUtil.isValidJsonElement(mergedNohit)) {
                    nohitElement = mergedNohit;
                }
            }
            if (JsonUtil.isValidJsonElement(carrierFeatureNohit)) {
                JsonElement mergedNohit2 = JsonUtil.merge(nohitElement, carrierFeatureNohit);
                if (JsonUtil.isValidJsonElement(mergedNohit2)) {
                    nohitElement = mergedNohit2;
                }
            }
            if (!JsonUtil.isValidJsonElement(smkUpdatedNohit)) {
                return nohitElement;
            }
            JsonElement mergedNohit3 = JsonUtil.merge(nohitElement, smkUpdatedNohit);
            if (JsonUtil.isValidJsonElement(mergedNohit3)) {
                return mergedNohit3;
            }
            return nohitElement;
        }
        if (JsonUtil.isValidJsonElement(smkUpdatedNohit)) {
            JsonElement mergedNohit4 = JsonUtil.merge(nohitElement, smkUpdatedNohit);
            if (JsonUtil.isValidJsonElement(mergedNohit4)) {
                nohitElement = mergedNohit4;
            }
        }
        if (JsonUtil.isValidJsonElement(autoUpdatedNohit)) {
            JsonElement mergedNohit5 = JsonUtil.merge(nohitElement, autoUpdatedNohit);
            if (JsonUtil.isValidJsonElement(mergedNohit5)) {
                nohitElement = mergedNohit5;
            }
        }
        if (!JsonUtil.isValidJsonElement(carrierFeatureNohit)) {
            return nohitElement;
        }
        JsonElement mergedNohit6 = JsonUtil.merge(nohitElement, carrierFeatureNohit);
        if (JsonUtil.isValidJsonElement(mergedNohit6)) {
            return mergedNohit6;
        }
        return nohitElement;
    }

    public JsonElement applyDefaultSettingUpdate(JsonElement defaultElement) {
        JsonElement smkUpdatedDefault = getGlobalSettings(0, TAG_GLOBALSETTINGS_DEFAULT);
        JsonElement autoUpdatedDefault = getGlobalSettings(1, TAG_GLOBALSETTINGS_DEFAULT);
        JsonElement carrierFeatureDefault = getGlobalSettings(4, TAG_GLOBALSETTINGS_DEFAULT);
        if (isForceSMKUpdate().booleanValue()) {
            Log.d(LOG_TAG, "SMK ForceMode - default");
            if (JsonUtil.isValidJsonElement(autoUpdatedDefault)) {
                JsonElement mergedDefault = JsonUtil.merge(defaultElement, autoUpdatedDefault);
                if (JsonUtil.isValidJsonElement(mergedDefault)) {
                    defaultElement = mergedDefault;
                }
            }
            if (JsonUtil.isValidJsonElement(carrierFeatureDefault)) {
                JsonElement mergedDefault2 = JsonUtil.merge(defaultElement, carrierFeatureDefault);
                if (JsonUtil.isValidJsonElement(mergedDefault2)) {
                    defaultElement = mergedDefault2;
                }
            }
            if (!JsonUtil.isValidJsonElement(smkUpdatedDefault)) {
                return defaultElement;
            }
            JsonElement mergedDefault3 = JsonUtil.merge(defaultElement, smkUpdatedDefault);
            if (JsonUtil.isValidJsonElement(mergedDefault3)) {
                return mergedDefault3;
            }
            return defaultElement;
        }
        if (JsonUtil.isValidJsonElement(smkUpdatedDefault)) {
            JsonElement mergedDefault4 = JsonUtil.merge(defaultElement, smkUpdatedDefault);
            if (JsonUtil.isValidJsonElement(mergedDefault4)) {
                defaultElement = mergedDefault4;
            }
        }
        if (JsonUtil.isValidJsonElement(autoUpdatedDefault)) {
            JsonElement mergedDefault5 = JsonUtil.merge(defaultElement, autoUpdatedDefault);
            if (JsonUtil.isValidJsonElement(mergedDefault5)) {
                defaultElement = mergedDefault5;
            }
        }
        if (!JsonUtil.isValidJsonElement(carrierFeatureDefault)) {
            return defaultElement;
        }
        JsonElement mergedDefault6 = JsonUtil.merge(defaultElement, carrierFeatureDefault);
        if (JsonUtil.isValidJsonElement(mergedDefault6)) {
            return mergedDefault6;
        }
        return defaultElement;
    }

    public JsonElement getRcsDefaultPolicyUpdate(JsonElement defaultPolicy, boolean isUp) {
        return applyRcsDefaultPolicyUpdate(applyRcsDefaultPolicyUpdate(applyRcsDefaultPolicyUpdate(defaultPolicy, 0, isUp), 1, isUp), 4, isUp);
    }

    private JsonElement applyRcsDefaultPolicyUpdate(JsonElement orgDefaultPolicy, int source, boolean isUp) {
        String defaultPolicyTag = isUp ? TAG_DEFAULT_UP_POLICY : TAG_DEFAULT_RCS_POLICY;
        JsonElement jsonSource = selectResource(source);
        JsonElement updateDefaultPolicy = JsonNull.INSTANCE;
        try {
            if (jsonSource.getAsJsonObject().has(RCSRPOLICY_UPDATE)) {
                JsonObject update = jsonSource.getAsJsonObject().getAsJsonObject(RCSRPOLICY_UPDATE);
                if (JsonUtil.isValidJsonElement(update) && update.has(defaultPolicyTag)) {
                    updateDefaultPolicy = update.get(defaultPolicyTag);
                }
            }
        } catch (IllegalStateException e) {
            String str = LOG_TAG;
            Log.e(str, "exception on applyDefaultRcsPolicyUpdate :" + defaultPolicyTag + " source " + sourceToString(source));
        }
        if (!JsonUtil.isValidJsonElement(updateDefaultPolicy)) {
            String str2 = LOG_TAG;
            Log.e(str2, "applyDefaultRcsPolicyUpdate :" + defaultPolicyTag + " source " + sourceToString(source) + " not valid");
            return orgDefaultPolicy;
        }
        JsonElement merged = JsonUtil.merge(orgDefaultPolicy, updateDefaultPolicy);
        if (JsonUtil.isValidJsonElement(merged)) {
            return merged;
        }
        String str3 = LOG_TAG;
        Log.e(str3, "applyDefaultRcsPolicyUpdate :" + defaultPolicyTag + " source " + sourceToString(source) + " merge result not valid");
        return orgDefaultPolicy;
    }

    public JsonElement getRcsPolicyUpdate(JsonElement policy, String policyName) {
        if (!TextUtils.isEmpty(policyName) && !policy.isJsonNull()) {
            return applyRcsPolicySettingUpdate(applyRcsPolicySettingUpdate(applyRcsPolicySettingUpdate(policy, 0, policyName), 1, policyName), 4, policyName);
        }
        Log.e(LOG_TAG, "policyName is not valid or policy is JsonNull");
        return policy;
    }

    private JsonElement applyRcsPolicySettingUpdate(JsonElement orgPolicy, int source, String orgPolicyName) {
        JsonElement jsonSource = selectResource(source);
        JsonElement updatePolicy = JsonNull.INSTANCE;
        try {
            if (jsonSource.getAsJsonObject().has(RCSRPOLICY_UPDATE)) {
                JsonObject updateListobj = jsonSource.getAsJsonObject().getAsJsonObject(RCSRPOLICY_UPDATE);
                if (JsonUtil.isValidJsonElement(updateListobj) && updateListobj.has(TAG_RCS_POLICY)) {
                    Iterator it = updateListobj.getAsJsonArray(TAG_RCS_POLICY).iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        JsonElement elm = (JsonElement) it.next();
                        JsonObject obj = elm.getAsJsonObject();
                        if (obj.has(TAG_POLICY_NAME) && TextUtils.equals(obj.get(TAG_POLICY_NAME).getAsString(), orgPolicyName)) {
                            updatePolicy = elm;
                            break;
                        }
                    }
                }
            }
            if (!JsonUtil.isValidJsonElement(updatePolicy)) {
                String str = LOG_TAG;
                Log.e(str, "applyRcsPolicySettingUpdate : " + orgPolicyName + " source " + sourceToString(source) + " not valid");
                return orgPolicy;
            }
            JsonElement merged = JsonUtil.merge(orgPolicy, updatePolicy);
            if (JsonUtil.isValidJsonElement(merged)) {
                return merged;
            }
            String str2 = LOG_TAG;
            Log.e(str2, "applyRcsPolicySettingUpdate : " + orgPolicyName + " source " + sourceToString(source) + " merge result not valid");
            return orgPolicy;
        } catch (IllegalStateException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "exception on applyRcsPolicySettingUpdate : " + e);
            return orgPolicy;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0058, code lost:
        r8 = r7.getAsJsonArray(com.sec.internal.ims.settings.SmsSetting.Properties.SMS_SETTINGS);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.google.gson.JsonElement getUpdatedSmsSetting(com.google.gson.JsonElement r12, java.lang.String r13) {
        /*
            r11 = this;
            r0 = 2
            int[] r0 = new int[r0]
            r0 = {0, 1} // fill-array
            java.lang.Boolean r1 = r11.isForceSMKUpdate()
            boolean r1 = r1.booleanValue()
            r2 = 0
            if (r1 == 0) goto L_0x0016
            r1 = 1
            r0[r2] = r1
            r0[r1] = r2
        L_0x0016:
            com.google.gson.JsonNull r1 = com.google.gson.JsonNull.INSTANCE
            com.google.gson.JsonObject r3 = r12.getAsJsonObject()
            java.lang.String r4 = "mnoname"
            com.google.gson.JsonElement r3 = r3.get(r4)
            java.lang.String r3 = r3.getAsString()
            int r5 = r0.length
        L_0x0027:
            if (r2 >= r5) goto L_0x0092
            r6 = r0[r2]
            com.google.gson.JsonElement r7 = r11.selectResource(r6)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            com.google.gson.JsonObject r7 = r7.getAsJsonObject()     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            java.lang.String r8 = "sms_settings_update"
            com.google.gson.JsonObject r7 = r7.getAsJsonObject(r8)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            boolean r8 = com.sec.internal.helper.JsonUtil.isValidJsonElement(r7)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            if (r8 == 0) goto L_0x006f
            java.lang.String r8 = "default_setting"
            boolean r8 = r8.equalsIgnoreCase(r13)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            if (r8 == 0) goto L_0x0052
            com.google.gson.JsonElement r8 = r7.get(r13)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            com.google.gson.JsonElement r8 = com.sec.internal.helper.JsonUtil.merge(r1, r8)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            r1 = r8
            goto L_0x006f
        L_0x0052:
            boolean r8 = r4.equalsIgnoreCase(r13)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            if (r8 == 0) goto L_0x006f
            java.lang.String r8 = "sms_settings"
            com.google.gson.JsonArray r8 = r7.getAsJsonArray(r8)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            int r9 = getIndexWithMnoname(r8, r3)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            r10 = -1
            if (r9 == r10) goto L_0x006f
            com.google.gson.JsonElement r10 = r8.get(r9)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            com.google.gson.JsonElement r10 = com.sec.internal.helper.JsonUtil.merge(r1, r10)     // Catch:{ ArrayIndexOutOfBoundsException | IllegalStateException -> 0x0070 }
            r1 = r10
        L_0x006f:
            goto L_0x008f
        L_0x0070:
            r7 = move-exception
            java.lang.String r8 = LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = "failed to find updated sms setting from "
            r9.append(r10)
            r9.append(r6)
            java.lang.String r10 = " for "
            r9.append(r10)
            r9.append(r13)
            java.lang.String r9 = r9.toString()
            android.util.Log.d(r8, r9)
        L_0x008f:
            int r2 = r2 + 1
            goto L_0x0027
        L_0x0092:
            com.google.gson.JsonElement r2 = com.sec.internal.helper.JsonUtil.merge(r12, r1)
            boolean r4 = com.sec.internal.helper.JsonUtil.isValidJsonElement(r2)
            if (r4 == 0) goto L_0x009d
            return r2
        L_0x009d:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.ImsAutoUpdate.getUpdatedSmsSetting(com.google.gson.JsonElement, java.lang.String):com.google.gson.JsonElement");
    }

    public static int getIndexWithMnoname(JsonArray array, String mnoname) {
        if (!array.isJsonNull() && array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                try {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    JsonElement mnonameVal = obj.get("mnoname");
                    if (mnonameVal != null && !obj.isJsonNull() && mnonameVal.getAsString().equalsIgnoreCase(mnoname)) {
                        return i;
                    }
                } catch (ClassCastException | IllegalStateException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = LOG_TAG;
        Log.e(str, "no matched element with mnoname " + mnoname);
        return -1;
    }

    private static int getIndexWithNames(JsonArray array, String name, String mnoname) {
        if (!array.isJsonNull() && array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                try {
                    JsonObject obj = array.get(i).getAsJsonObject();
                    JsonElement nameVal = obj.get("name");
                    JsonElement mnonameVal = obj.get("mnoname");
                    if (nameVal != null && mnonameVal != null && !obj.isJsonNull() && nameVal.getAsString().equalsIgnoreCase(name) && mnonameVal.getAsString().equalsIgnoreCase(mnoname)) {
                        return i;
                    }
                } catch (ClassCastException | IllegalStateException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        String str = LOG_TAG;
        Log.e(str, "no matched element with name " + name + "and mnoname " + mnoname);
        return -1;
    }

    public void updateSmkConfig(String config) {
        if (!TextUtils.isEmpty(config)) {
            this.mSmkConfig.saveSmkConfig(new JsonParser().parse(config).getAsJsonObject());
        }
    }

    public void clearSmkConfig() {
        this.mSmkConfig.clearSmkConfig();
    }

    public JsonObject getSmkConfig() {
        return this.mSmkConfig.getSmkConfig();
    }

    public Boolean isForceSMKUpdate() {
        JsonObject smkObject = getSmkConfig();
        if ((smkObject != null || this.mSmkConfig.hasNewSmkConfig()) && smkObject.has("update_method") && smkObject.get("update_method").getAsInt() == 0) {
            return true;
        }
        return false;
    }

    public static class handleSmkConfig {
        private static final String LOG_TAG = "handleSmkConfig";
        private JsonObject mCachedSmkConfig;
        private Context mContext;
        private final File mDownloadedSmkConfig = new File(this.mContext.getFilesDir(), "smkconfig.json");
        private boolean mHasNewSmkConfig = false;

        public handleSmkConfig(Context context) {
            this.mContext = context;
        }

        public void load() {
            try {
                if (this.mDownloadedSmkConfig.exists()) {
                    this.mCachedSmkConfig = new JsonParser().parse(new String(Files.readAllBytes(this.mDownloadedSmkConfig.toPath()))).getAsJsonObject();
                }
            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }
        }

        public void saveSmkConfig(JsonObject config) {
            Log.d(LOG_TAG, "Save downloaded Smk Config");
            try {
                if (this.mDownloadedSmkConfig.exists()) {
                    this.mDownloadedSmkConfig.delete();
                }
                this.mDownloadedSmkConfig.createNewFile();
                Files.write(this.mDownloadedSmkConfig.toPath(), config.toString().getBytes(), new OpenOption[0]);
                Log.d(LOG_TAG, "Store downloaded Smk Config complete");
                this.mCachedSmkConfig = config;
                this.mHasNewSmkConfig = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public JsonObject getSmkConfig() {
            return this.mCachedSmkConfig;
        }

        public void clearSmkConfig() {
            Log.d(LOG_TAG, "Clear Smk Config");
            if (this.mCachedSmkConfig != null) {
                try {
                    if (this.mDownloadedSmkConfig.exists()) {
                        this.mDownloadedSmkConfig.delete();
                        disableSmkConfig();
                        Log.d(LOG_TAG, "Clear Smk Config Successfully");
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, "has problem for delete Smk Config");
                }
                this.mCachedSmkConfig = null;
            }
        }

        public void disableSmkConfig() {
            this.mHasNewSmkConfig = false;
        }

        public boolean hasNewSmkConfig() {
            return this.mHasNewSmkConfig;
        }
    }
}
