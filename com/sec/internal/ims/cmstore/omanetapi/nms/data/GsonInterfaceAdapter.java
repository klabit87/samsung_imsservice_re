package com.sec.internal.ims.cmstore.omanetapi.nms.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sec.internal.omanetapi.nc.data.LongPollingData;
import java.lang.reflect.Type;

public class GsonInterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {
    private final Type mType;

    public GsonInterfaceAdapter(Class<?> className) {
        this.mType = className;
    }

    public final JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
        JsonObject member = context.serialize(object).getAsJsonObject();
        if (this.mType.equals(LongPollingData.class)) {
            member.addProperty("type", object.getClass().getSimpleName());
        }
        return member;
    }

    public final T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(elem, this.mType);
    }
}
