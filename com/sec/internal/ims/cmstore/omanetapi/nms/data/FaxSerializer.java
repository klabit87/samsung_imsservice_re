package com.sec.internal.ims.cmstore.omanetapi.nms.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.omanetapi.nms.data.Attribute;
import com.sec.internal.omanetapi.nms.data.Object;
import java.lang.reflect.Type;

public class FaxSerializer implements JsonSerializer<Object> {
    public JsonElement serialize(Object object, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        JsonObject obj = new JsonObject();
        obj.addProperty("parentFolderPath", object.parentFolderPath);
        JsonObject attribute = new JsonObject();
        JsonArray attributeArr = new JsonArray();
        for (Attribute attr : object.attributes.attribute) {
            JsonObject attrJson = new JsonObject();
            attrJson.addProperty("name", attr.name);
            for (String addProperty : attr.value) {
                attrJson.addProperty(ImsConstants.Intents.EXTRA_UPDATED_VALUE, addProperty);
            }
            attributeArr.add(attrJson);
        }
        attribute.add("attribute", attributeArr);
        obj.add("attributes", attribute);
        jsonObject.add("object", obj);
        return jsonObject;
    }
}
