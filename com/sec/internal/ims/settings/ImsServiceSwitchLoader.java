package com.sec.internal.ims.settings;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class ImsServiceSwitchLoader {
    private static final String LOG_TAG = "ImsServiceSwitchLoader";
    protected static final String SP_KEY_MNONAME = "mnoname";

    protected static JsonElement getImsSwitchFromJson(Context context, String mnoname, int phoneId) {
        InputStream inputStream;
        InputStream inputStream2 = null;
        try {
            if (DeviceUtil.isTablet()) {
                IMSLog.d(LOG_TAG, phoneId, " getResources : imsswitch_tablet.json");
                inputStream = context.getResources().openRawResource(R.raw.imsswitch_tablet);
            } else if (DeviceUtil.isUSOpenDevice()) {
                IMSLog.d(LOG_TAG, phoneId, " getResources : imsswitch_open.json");
                inputStream = context.getResources().openRawResource(R.raw.imsswitch_open);
            } else {
                IMSLog.d(LOG_TAG, phoneId, " getResources : imsswitch.json");
                inputStream = context.getResources().openRawResource(R.raw.imsswitch);
            }
            JsonParser parser = new JsonParser();
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            JsonElement ret = parser.parse(reader);
            reader.close();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            JsonNull jsonNull = JsonNull.INSTANCE;
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e12) {
                    e12.printStackTrace();
                }
            }
            return jsonNull;
        } catch (Throwable parser2) {
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e13) {
                    e13.printStackTrace();
                }
            }
            throw parser2;
        }
    }

    public static JsonElement getMatchedJsonElement(JsonObject object, String mnoname, String mvnoname, int phoneId) {
        JsonElement matchElement = JsonNull.INSTANCE;
        JsonArray imsswitchArray = object.getAsJsonArray("imsswitch");
        if (!JsonUtil.isValidJsonElement(imsswitchArray)) {
            IMSLog.e(LOG_TAG, phoneId, "load: parse failed.");
            return matchElement;
        }
        try {
            Iterator it = imsswitchArray.iterator();
            while (it.hasNext()) {
                JsonElement elem = (JsonElement) it.next();
                JsonElement obj = elem.getAsJsonObject();
                String name = elem.getAsJsonObject().get("mnoname").getAsString();
                if (!TextUtils.isEmpty(mvnoname)) {
                    if (name.equalsIgnoreCase(mnoname + Mno.MVNO_DELIMITER + mvnoname)) {
                        JsonElement matchElement2 = obj;
                        IMSLog.d(LOG_TAG, phoneId, "loadImsSwitchFromJson - mvnoname on json:" + name + " found");
                        return matchElement2;
                    } else if (name.equalsIgnoreCase(mnoname)) {
                        matchElement = obj;
                        IMSLog.d(LOG_TAG, phoneId, "loadImsSwitchFromJson - primary mnoname on json:" + name + " found");
                    }
                } else if (name.equalsIgnoreCase(mnoname)) {
                    JsonElement matchElement3 = obj;
                    IMSLog.d(LOG_TAG, phoneId, "loadImsSwitchFromJson - mnoname on json:" + name + " found");
                    return matchElement3;
                }
            }
            return matchElement;
        } catch (Exception e) {
            return matchElement;
        }
    }

    public static boolean hasImsSwitchAtJson(Context ctx, String mnoname, String mvnoname, int phoneId) {
        JsonElement element = getImsSwitchFromJson(ctx, mnoname, phoneId);
        if (element.isJsonNull()) {
            return false;
        }
        return !getMatchedJsonElement(element.getAsJsonObject(), mnoname, mvnoname, phoneId).isJsonNull();
    }
}
