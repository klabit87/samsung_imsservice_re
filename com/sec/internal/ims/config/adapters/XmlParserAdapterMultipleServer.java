package com.sec.internal.ims.config.adapters;

import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.xmlpull.v1.XmlPullParser;

public class XmlParserAdapterMultipleServer extends XmlParserAdapter {
    private static final String LOG_TAG = XmlParserAdapterMultipleServer.class.getSimpleName();
    private final String ATTR_VALUE_ACCESS_CONTROL_APPID = "app-id";
    private final String ATTR_VALUE_APPID = "appid";
    private final String ATTR_VALUE_APPLICATION = "application";
    protected boolean isParsingApplicationCharacterisitic = false;
    protected int position = -1;
    protected Map<String, String> tempMap = null;

    public XmlParserAdapterMultipleServer() {
        Log.i(LOG_TAG, "Init XmlParserAdapterMultipleServer");
        LIST_TAG_NAME.put("server", (Object) null);
        LIST_TAG_NAME.put("app-id", (Object) null);
        LIST_TAG_NAME.put("node", (Object) null);
    }

    public int getCharacteristicListTagCount(List<String> paths, Map<String, Integer> listTagCount, String attrValue) {
        int count = super.getCharacteristicListTagCount(paths, listTagCount, attrValue);
        if (attrValue.equals("application")) {
            this.position = count;
            this.isParsingApplicationCharacterisitic = true;
        }
        return count;
    }

    public void parseParamTag(XmlPullParser xpp, List<String> paths, Map<String, Integer> listTagCount, String tagName, Map<String, String> parsedXml) {
        XmlPullParser xmlPullParser = xpp;
        List<String> list = paths;
        Map<String, String> map = parsedXml;
        if (isParamTag(xmlPullParser, tagName)) {
            if (xmlPullParser.getAttributeValue(0).toLowerCase(Locale.US).equals("appid")) {
                this.isParsingApplicationCharacterisitic = false;
                String count = ConfigConstants.APPID_MAP.get(xmlPullParser.getAttributeValue(1));
                String str = LOG_TAG;
                Log.i(str, "position: " + this.position + " appid: " + xmlPullParser.getAttributeValue(1) + "replacement: " + count);
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("application");
                stringBuffer.append("/");
                StringBuffer temp = stringBuffer.append(count);
                int index = 0;
                Iterator<String> it = paths.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    } else if (it.next().matches("application/([0-9])")) {
                        list.set(index, temp.toString());
                        break;
                    } else {
                        index++;
                    }
                }
                for (Map.Entry<String, String> entry : this.tempMap.entrySet()) {
                    String key = entry.getKey();
                    if (key.contains("application/" + this.position)) {
                        key = key.replace("application/" + this.position, temp.toString());
                    }
                    map.put(key, entry.getValue());
                }
                this.tempMap.clear();
            }
            String pathFull = parseParamListTag(list, listTagCount, xmlPullParser.getAttributeValue(0).toLowerCase(Locale.US));
            if (!this.isParsingApplicationCharacterisitic) {
                map.put(pathFull, xmlPullParser.getAttributeValue(1));
            } else {
                this.tempMap.put(pathFull, xmlPullParser.getAttributeValue(1));
            }
        } else {
            Map<String, Integer> map2 = listTagCount;
        }
    }

    public void parseEndTag(XmlPullParser xpp, List<String> paths, List<String> unknownTags, Map<String, Integer> listTagCount) {
        super.parseEndTag(xpp, paths, unknownTags, listTagCount);
        listTagCount.remove("app-id");
    }

    public Map<String, String> parse(String xml) {
        this.tempMap = new TreeMap();
        this.isParsingApplicationCharacterisitic = false;
        this.position = -1;
        return super.parse(xml);
    }
}
