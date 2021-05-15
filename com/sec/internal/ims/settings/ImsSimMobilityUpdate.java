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
import com.sec.ims.settings.ImsProfile;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class ImsSimMobilityUpdate {
    private static final String OMC_DATA_FILE = "omc_data.json";
    private static final String OMC_PATH_PRISM = "/prism/etc/";
    private static final String TAG = ImsSimMobilityUpdate.class.getSimpleName();
    private static ImsSimMobilityUpdate mInstance = null;
    private Context mContext;
    private JsonElement mMobilityGlobalSettings = JsonNull.INSTANCE;

    protected ImsSimMobilityUpdate(Context ctx) {
        this.mContext = ctx;
    }

    public static ImsSimMobilityUpdate getInstance(Context ctx) {
        if (mInstance == null) {
            synchronized (ImsSimMobilityUpdate.class) {
                if (mInstance == null) {
                    mInstance = new ImsSimMobilityUpdate(ctx);
                }
            }
        }
        return mInstance;
    }

    private ImsProfile makeUpdatedProfile(ImsProfile orgProfile, JsonObject configJson) {
        JsonElement orgProfileElement = JsonNull.INSTANCE;
        try {
            orgProfileElement = new JsonParser().parse(orgProfile.toJson());
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "profile cannot parse result");
            e.printStackTrace();
        }
        JsonElement copyOrgProfile = (JsonElement) JsonUtil.deepCopy(orgProfileElement, JsonElement.class);
        if (configJson == null) {
            Log.d(TAG, "object profiles is null.");
            return orgProfile;
        }
        JsonArray update = configJson.getAsJsonArray("profile");
        if (update == null) {
            Log.d(TAG, "updates is null.");
            return orgProfile;
        }
        JsonElement updatedProf = JsonNull.INSTANCE;
        if (!orgProfileElement.isJsonNull() && !update.isJsonNull()) {
            Iterator it = update.iterator();
            while (it.hasNext()) {
                JsonElement p = (JsonElement) it.next();
                JsonObject prof = p.getAsJsonObject();
                if (prof.has("name") && prof.has("mnoname")) {
                    String name = prof.get("name").getAsString();
                    String mnoname = prof.get("mnoname").getAsString();
                    if (checkProfileWithNames(copyOrgProfile, name, mnoname)) {
                        String str = TAG;
                        Log.d(str, "sim mobility imsprofile update : " + mnoname);
                        updatedProf = JsonUtil.merge(copyOrgProfile, p);
                    }
                }
            }
        }
        if (!updatedProf.isJsonNull()) {
            return new ImsProfile(updatedProf.toString());
        }
        Log.d(TAG, "updatedProf is null");
        return orgProfile;
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public boolean loadMobilityGlobalSettings() {
        JsonReader reader;
        JsonElement mobilityGlobalElement = null;
        IMSLog.d(TAG, "loadMobilityGlobalSettings");
        try {
            reader = new JsonReader(new BufferedReader(new InputStreamReader(this.mContext.getResources().openRawResource(R.raw.mobilityupdate))));
            mobilityGlobalElement = new JsonParser().parse(reader);
            reader.close();
        } catch (JsonParseException | IOException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        if (mobilityGlobalElement == null) {
            return false;
        }
        if (!mobilityGlobalElement.getAsJsonObject().has(ImsAutoUpdate.GLOBALSETTINGS_UPDATE)) {
            return true;
        }
        this.mMobilityGlobalSettings = mobilityGlobalElement;
        return true;
        throw th;
    }

    public JsonElement getMobilityGlobalSettings() {
        return this.mMobilityGlobalSettings;
    }

    public boolean isSimMobilityAllowedByCarrier(ImsProfile profile) {
        String OMC_CODE = OmcCode.get();
        String allowList = profile.getAsString("simmobility_allowlist");
        String blockList = profile.getAsString("simmobility_blocklist");
        if (OMC_CODE.isEmpty()) {
            return true;
        }
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.SIMMOBILITY_ENABLE, -1) == 1) {
            IMSLog.d(TAG, "SimMobility Feature is Enabled");
            return true;
        } else if (blockList.contains("*") || blockList.contains(OMC_CODE)) {
            IMSLog.d(TAG, "No mobility condition");
            return false;
        } else if (!allowList.contains("*") && !allowList.contains(OMC_CODE)) {
            return false;
        } else {
            IMSLog.d(TAG, "SimMobility enabled by allowlist");
            return true;
        }
    }

    public ImsProfile applySimMobilityProfileUpdate(ImsProfile profile, int phoneId) {
        boolean isSimMobility = isSimMobilityAvailable(profile, phoneId);
        profile.setSimMobility(isSimMobility);
        SlotBasedConfig.getInstance(phoneId).activeSimMobility(isSimMobility);
        if (!isSimMobility) {
            String str = TAG;
            Log.d(str, "Not support SimMobility for " + profile.getName());
            return profile;
        }
        ImsAutoUpdate imsAutoUpdate = ImsAutoUpdate.getInstance(this.mContext, phoneId);
        JsonObject downloadConfigObject = imsAutoUpdate.getSmkConfig();
        if (!(downloadConfigObject == null || downloadConfigObject.getAsJsonObject("mobilityprofile_update") == null)) {
            Log.d(TAG, "has download mobilityprofile_update");
            downloadConfigObject = downloadConfigObject.getAsJsonObject("mobilityprofile_update");
        }
        InputStream mobilityInputStream = this.mContext.getResources().openRawResource(R.raw.mobilityupdate);
        JsonElement mobilityProfileElement = null;
        if (mobilityInputStream != null) {
            JsonParser mobilityParser = new JsonParser();
            JsonReader mobilityReader = new JsonReader(new BufferedReader(new InputStreamReader(mobilityInputStream)));
            try {
                mobilityProfileElement = mobilityParser.parse(mobilityReader).getAsJsonObject().getAsJsonObject(ImsAutoUpdate.IMSPROFILE_UPDATE);
                mobilityReader.close();
            } catch (Exception e) {
                Log.e(TAG, "mobilityupdate cannot parse result");
            }
        } else if (downloadConfigObject == null) {
            Log.e(TAG, "mobilityupdate / downloadmobilityprofile were not found");
            return profile;
        }
        ImsProfile updatedProfile = mergeProfiles(mobilityProfileElement.getAsJsonObject(), profile);
        JsonObject imsUpdate = null;
        try {
            imsUpdate = imsAutoUpdate.selectResource(1).getAsJsonObject().getAsJsonObject(ImsAutoUpdate.IMSPROFILE_UPDATE);
        } catch (IllegalStateException | NullPointerException e2) {
            Log.e(TAG, "imsupdate cannot parse result");
        }
        if (!imsAutoUpdate.isForceSMKUpdate().booleanValue()) {
            return mergeProfiles(imsUpdate, mergeProfiles(downloadConfigObject, updatedProfile));
        }
        Log.d(TAG, "SMK ForceMode - SimMobilityProfile");
        return mergeProfiles(downloadConfigObject, mergeProfiles(imsUpdate, updatedProfile));
    }

    private ImsProfile mergeProfiles(JsonObject diffJson, ImsProfile profile) {
        JsonObject object;
        if (!(diffJson == null || (object = diffJson.getAsJsonObject()) == null)) {
            try {
                return makeUpdatedProfile(profile, object);
            } catch (Exception e) {
                String str = TAG;
                Log.e(str, "Updating mobility profile failed.return original profile " + e.toString());
            }
        }
        return profile;
    }

    private static boolean checkProfileWithNames(JsonElement element, String name, String mnoname) {
        try {
            if (element.isJsonNull()) {
                return false;
            }
            JsonObject obj = element.getAsJsonObject();
            JsonElement nameVal = obj.get("name");
            JsonElement mnonameVal = obj.get("mnoname");
            if (nameVal == null || mnonameVal == null || obj.isJsonNull() || !TextUtils.equals(nameVal.getAsString(), name) || !TextUtils.equals(mnonameVal.getAsString(), mnoname)) {
                return false;
            }
            return true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSimMobilityAvailable(ImsProfile profile, int phoneId) {
        String reason;
        if (!SimUtil.isSimMobilityFeatureEnabled()) {
            reason = "SIM Mobility Feature disabled; ";
        } else if (!SimManagerFactory.isOutboundSim(phoneId)) {
            reason = "Not outbound Sim - SimMobility should be disabled; ";
        } else if (Mno.fromName(profile.getMnoName()).isAus() && profile.hasEmergencySupport()) {
            reason = "Au operator return original emergency profile :" + profile.getMnoName();
        } else if (!DeviceUtil.isTablet() || TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable()) {
            return isSimMobilityAllowedByCarrier(profile);
        } else {
            reason = "Disable non voice capable tablet in R OS";
        }
        IMSLog.d(TAG, phoneId, reason);
        return false;
    }
}
