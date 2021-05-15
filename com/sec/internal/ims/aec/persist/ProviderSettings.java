package com.sec.internal.ims.aec.persist;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderSettings {
    private static final String MNO_NAME = "mnoname";
    private static final String PROVIDER_SETTINGS = "ProviderSettings";

    private static JsonElement getResource(InputStream inputStream) {
        JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
        JsonElement jsonElement = new JsonParser().parse(jsonReader);
        try {
            jsonReader.close();
            inputStream.close();
            if (jsonElement.getAsJsonObject().has(PROVIDER_SETTINGS)) {
                return jsonElement.getAsJsonObject().get(PROVIDER_SETTINGS);
            }
            return JsonNull.INSTANCE;
        } catch (IOException e) {
            return JsonNull.INSTANCE;
        }
    }

    private static Map<String, String> getSettingMap(String mno, JsonElement jsonElement) {
        Map<String, String> settingMap = new ConcurrentHashMap<>();
        if (jsonElement != JsonNull.INSTANCE) {
            Iterator it = jsonElement.getAsJsonArray().iterator();
            while (it.hasNext()) {
                JsonObject obj = ((JsonElement) it.next()).getAsJsonObject();
                if (mno.equalsIgnoreCase(obj.get("mnoname").getAsString())) {
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                        settingMap.put(entry.getKey(), obj.get(entry.getKey()).getAsString());
                    }
                }
            }
        }
        return settingMap;
    }

    private static JsonElement getImsUpdate(Context context, int phoneId) {
        return ImsAutoUpdate.getInstance(context, phoneId).getProviderSettings(1, PROVIDER_SETTINGS);
    }

    private static Map<String, String> mergeSettingMap(Map<String, String> resource, Map<String, String> imsUpdate) {
        if (imsUpdate != null && !imsUpdate.isEmpty()) {
            for (Map.Entry<String, String> entry : imsUpdate.entrySet()) {
                resource.put(entry.getKey(), entry.getValue());
            }
        }
        return resource;
    }

    public static Map<String, String> getSettingMap(Context context, int phoneId, String mno) {
        return mergeSettingMap(getSettingMap(mno, getResource(context.getResources().openRawResource(R.raw.providersettings))), getSettingMap(mno, getImsUpdate(context, phoneId)));
    }
}
