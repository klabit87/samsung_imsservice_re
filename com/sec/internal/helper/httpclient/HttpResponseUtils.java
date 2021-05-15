package com.sec.internal.helper.httpclient;

import com.sec.internal.log.IMSLog;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class HttpResponseUtils {
    public static <T> T parseXmlResponse(HttpResponseParams httpResponse, Class<T> genericType, boolean namespaces) {
        String xml = httpResponse.getDataString();
        if (xml == null) {
            return null;
        }
        try {
            return new GsonXmlBuilder().setXmlParserCreator(new XmlParserCreator() {
                public XmlPullParser createParser() {
                    try {
                        return XmlPullParserFactory.newInstance().newPullParser();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).setTreatNamespaces(namespaces).create().fromXml(xml, genericType);
        } catch (Exception e) {
            IMSLog.e("parseXmlResponse()", "cannot parse result");
            e.printStackTrace();
            return null;
        }
    }
}
