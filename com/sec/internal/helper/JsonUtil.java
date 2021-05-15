package com.sec.internal.helper;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.Map;

public class JsonUtil {
    private static final String LOG_TAG = JsonUtil.class.getSimpleName();

    public static boolean isValidJsonElement(JsonElement element) {
        if (element == null || element.isJsonNull() || element.toString().equals("{}") || element.toString().equals("[]")) {
            return false;
        }
        return true;
    }

    public static JsonElement merge(JsonElement dst, JsonElement src) {
        JsonElement result = JsonNull.INSTANCE;
        if (dst.isJsonObject() && src.isJsonObject()) {
            JsonObject dstObj = dst.getAsJsonObject();
            JsonObject srcObj = src.getAsJsonObject();
            JsonElement mergedObj = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : dstObj.entrySet()) {
                if (srcObj.has(entry.getKey())) {
                    JsonElement e = merge(entry.getValue(), srcObj.get(entry.getKey()));
                    if (e == JsonNull.INSTANCE) {
                        String str = LOG_TAG;
                        Log.e(str, "merge failed. key: " + entry.getKey() + " value: " + entry.getValue() + " + " + srcObj.get(entry.getKey()));
                    } else {
                        mergedObj.add(entry.getKey(), e);
                    }
                } else {
                    mergedObj.add(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, JsonElement> entry2 : srcObj.entrySet()) {
                if (!mergedObj.has(entry2.getKey())) {
                    mergedObj.add(entry2.getKey(), entry2.getValue());
                }
            }
            return mergedObj;
        } else if (dst.isJsonNull() || ((dst.isJsonPrimitive() && src.isJsonPrimitive()) || (dst.isJsonArray() && src.isJsonArray()))) {
            return src;
        } else {
            Log.e(LOG_TAG, "merge: type mismatch.");
            return result;
        }
    }

    public static <T> T deepCopy(T object, Class<T> type) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(gson.toJson(object, type), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
