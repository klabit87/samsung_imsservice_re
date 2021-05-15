package com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.parser;

import android.util.Log;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.deviceconfig.DeviceConfig;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class DeviceMstoreConfigParser {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = DeviceMstoreConfigParser.class.getSimpleName();
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        public XmlPullParser createParser() {
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
                return parser;
            } catch (Exception e) {
                String access$000 = DeviceMstoreConfigParser.LOG_TAG;
                Log.d(access$000, "parserCreator(): " + e.getMessage());
                Log.d(DeviceMstoreConfigParser.LOG_TAG, "createParser failed");
                return null;
            }
        }
    };

    private DeviceMstoreConfigParser() {
    }

    static GsonXml createGsonXml(boolean namespaces) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(namespaces).setSameNameLists(true).create();
    }

    public static DeviceConfig parseDeviceConfig(String deviceConfigXml) {
        if (deviceConfigXml == null) {
            return null;
        }
        try {
            return (DeviceConfig) createGsonXml(false).fromXml(deviceConfigXml, DeviceConfig.class);
        } catch (JsonSyntaxException e) {
            Log.e(LOG_TAG, "parseDeviceConfig: malformed device config xml");
            return null;
        }
    }
}
