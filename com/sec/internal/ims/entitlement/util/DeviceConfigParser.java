package com.sec.internal.ims.entitlement.util;

import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.data.DeviceConfiguration;
import com.sec.internal.log.IMSLog;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public final class DeviceConfigParser {
    private static final String LOG_TAG = DeviceConfigParser.class.getSimpleName();
    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        public XmlPullParser createParser() {
            try {
                return (XmlPullParser) Class.forName("android.util.Xml").getMethod("newPullParser", new Class[0]).invoke((Object) null, new Object[0]);
            } catch (Exception e) {
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
                    return parser;
                } catch (Exception e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
    };

    private DeviceConfigParser() {
    }

    static GsonXml createGsonXml(boolean namespaces) {
        return new GsonXmlBuilder().setXmlParserCreator(PARSER_CREATOR).setTreatNamespaces(namespaces).setSameNameLists(true).create();
    }

    public static DeviceConfiguration parseDeviceConfig(String deviceConfigXml) {
        String str = LOG_TAG;
        IMSLog.s(str, "deviceConfigXml: " + deviceConfigXml);
        if (deviceConfigXml == null) {
            return null;
        }
        try {
            return (DeviceConfiguration) createGsonXml(false).fromXml(deviceConfigXml, DeviceConfiguration.class);
        } catch (JsonSyntaxException e) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "parseDeviceConfig: malformed device config xml" + e.getMessage());
            return null;
        }
    }
}
