package com.sec.internal.ims.config.adapters;

import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlParserAdapter implements IXmlParserAdapter {
    protected static final Map<String, List<String>> LIST_TAG_NAME = new TreeMap<String, List<String>>() {
        {
            put("application", (Object) null);
            put("conrefs", (Object) null);
            put("icsi_list", (Object) null);
            put("lbo_p-cscf_address", (Object) null);
            put("phonecontext_list", (Object) null);
            put(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY, Arrays.asList(new String[]{".*application/[0-9]+/phonecontext_list/[0-9]+.*"}));
            put("public_user_identity_list", (Object) null);
        }
    };
    private static final String LOG_TAG = XmlParserAdapter.class.getSimpleName();
    protected static final String PATH_DEVIDER = "/";
    protected static final String PATH_ROOT = "root";
    protected static final String TAG_CHARACTERISTIC = "characteristic";
    protected static final int TAG_CHARACTERISTIC_ATTR_COUNT = 1;
    protected static final String TAG_CHARACTERISTIC_ATTR_TYPE = "type";
    protected static final String TAG_PARAM = "param";
    protected static final String TAG_PARM = "parm";
    protected static final int TAG_PARM_ATTR_COUNT = 2;
    protected static final String TAG_PARM_ATTR_NAME = "name";
    protected static final String TAG_PARM_ATTR_VALUE = "value";
    protected static final String TAG_WAPPROVISIONINGDOC = "wap-provisioningdoc";
    protected static final int TAG_WAPPROVISIONINGDOC_ATTR_COUNT = 1;
    protected static final String TAG_WAPPROVISIONINGDOC_ATTR_VERISON = "version";

    public XmlParserAdapter() {
        Log.i(LOG_TAG, "Init XmlParser");
    }

    public void parseWapProvisioningDocTag(XmlPullParser xpp, String tagName) {
        String attrName = xpp.getAttributeName(0).toLowerCase(Locale.US);
        String attrValue = xpp.getAttributeValue(0).toLowerCase(Locale.US);
        if (attrName.equals("version")) {
            String str = LOG_TAG;
            Log.i(str, tagName + " " + attrName + ":" + attrValue);
            return;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "unknown '" + tagName + "' attr name:" + attrName);
    }

    public int getCharacteristicListTagCount(List<String> paths, Map<String, Integer> listTagCount, String attrValue) {
        int count = increaseListTagCount(listTagCount, convertList(paths, PATH_DEVIDER));
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(attrValue);
        stringBuffer.append(PATH_DEVIDER);
        paths.set(paths.size() - 1, stringBuffer.append(count).toString());
        return count;
    }

    public void parseCharacteristicTag(XmlPullParser xpp, List<String> paths, Map<String, Integer> listTagCount, String tagName) {
        String attrName = xpp.getAttributeName(0).toLowerCase(Locale.US);
        String attrValue = xpp.getAttributeValue(0).toLowerCase(Locale.US);
        if (attrName.equals("type")) {
            paths.add(attrValue);
            if (checkListTag(paths, attrValue, LIST_TAG_NAME)) {
                getCharacteristicListTagCount(paths, listTagCount, attrValue);
                return;
            }
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "unknown '" + tagName + "' attr name:" + attrName);
    }

    public boolean isParamTag(XmlPullParser xpp, String tagName) {
        String attrName0 = xpp.getAttributeName(0).toLowerCase(Locale.US);
        String attrName1 = xpp.getAttributeName(1).toLowerCase(Locale.US);
        if (attrName0.equals("name") && attrName1.equals("value")) {
            return true;
        }
        String str = LOG_TAG;
        Log.i(str, "unknown '" + tagName + "' attr name:" + attrName0 + "," + attrName1);
        return false;
    }

    public String parseParamListTag(List<String> paths, Map<String, Integer> listTagCount, String attrValue0) {
        paths.add(attrValue0);
        String pathFull = convertList(paths, PATH_DEVIDER);
        if (!checkListTag(paths, attrValue0, LIST_TAG_NAME)) {
            return pathFull;
        }
        int count = increaseListTagCount(listTagCount, pathFull);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(attrValue0);
        stringBuffer.append(PATH_DEVIDER);
        paths.set(paths.size() - 1, stringBuffer.append(count).toString());
        return convertList(paths, PATH_DEVIDER);
    }

    public void parseParamTag(XmlPullParser xpp, List<String> paths, Map<String, Integer> listTagCount, String tagName, Map<String, String> parsedXml) {
        if (isParamTag(xpp, tagName)) {
            parsedXml.put(parseParamListTag(paths, listTagCount, xpp.getAttributeValue(0).toLowerCase(Locale.US)), xpp.getAttributeValue(1));
        }
    }

    public void parseStartTag(XmlPullParser xpp, List<String> paths, List<String> unknownTags, Map<String, Integer> listTagCount, Map<String, String> parsedXml) {
        String tagName = xpp.getName().toLowerCase(Locale.US);
        int attrCount = xpp.getAttributeCount();
        if (tagName.equals(TAG_WAPPROVISIONINGDOC) && attrCount == 1) {
            parseWapProvisioningDocTag(xpp, tagName);
        } else if (tagName.equals(TAG_CHARACTERISTIC) && attrCount == 1) {
            parseCharacteristicTag(xpp, paths, listTagCount, tagName);
        } else if ((tagName.equals(TAG_PARM) || tagName.equals("param")) && attrCount == 2) {
            parseParamTag(xpp, paths, listTagCount, tagName, parsedXml);
        } else {
            String str = LOG_TAG;
            Log.i(str, "unknown tag or count:" + tagName + "," + attrCount);
            unknownTags.add(tagName);
            String str2 = LOG_TAG;
            Log.i(str2, "unknownTags size:" + unknownTags.size());
        }
    }

    public void parseEndTag(XmlPullParser xpp, List<String> paths, List<String> unknownTags, Map<String, Integer> map) {
        String tagName = xpp.getName().toLowerCase(Locale.US);
        if (unknownTags.contains(tagName)) {
            String str = LOG_TAG;
            Log.i(str, "size of Unknown Tags " + unknownTags.size());
            unknownTags.remove(tagName);
            return;
        }
        paths.remove(paths.size() - 1);
    }

    public Map<String, String> parse(String xml) {
        TreeMap treeMap = new TreeMap();
        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            xpp.setInput(new StringReader(xml));
            int eventType = xpp.getEventType();
            List<String> paths = new ArrayList<>();
            List<String> unknownTags = new ArrayList<>();
            Map<String, Integer> listTagCount = new TreeMap<>();
            paths.add(PATH_ROOT);
            Log.i(LOG_TAG, "Start document");
            for (int eventType2 = eventType; eventType2 != 1; eventType2 = xpp.next()) {
                if (eventType2 == 2) {
                    parseStartTag(xpp, paths, unknownTags, listTagCount, treeMap);
                } else if (eventType2 == 3) {
                    parseEndTag(xpp, paths, unknownTags, listTagCount);
                }
            }
            Log.i(LOG_TAG, "End document");
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        Log.i(LOG_TAG, "+++ parsed data");
        for (Map.Entry<String, String> entry : treeMap.entrySet()) {
            displayParsedData(entry.getKey(), entry.getValue());
        }
        Log.i(LOG_TAG, "--- parsed data");
        return treeMap;
    }

    /* access modifiers changed from: protected */
    public void displayParsedData(String key, String value) {
        String str = LOG_TAG;
        IMSLog.s(str, "path:" + key + ",value:" + value);
    }

    /* access modifiers changed from: protected */
    public String convertList(List<String> datas, String devider) {
        StringBuffer result = new StringBuffer();
        for (String data : datas) {
            result.append(data);
            result.append(devider);
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    /* access modifiers changed from: protected */
    public boolean checkListTag(List<String> currentPaths, String currentTag, Map<String, List<String>> listTag) {
        if (!listTag.keySet().contains(currentTag)) {
            return false;
        }
        String pathFull = convertList(currentPaths, PATH_DEVIDER);
        List<String> conditions = listTag.get(currentTag);
        if (conditions == null) {
            return true;
        }
        for (String condition : conditions) {
            if (pathFull.matches(condition)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int increaseListTagCount(Map<String, Integer> listTagCount, String key) {
        int value;
        if (!listTagCount.containsKey(key)) {
            value = 0;
        } else {
            value = listTagCount.get(key).intValue() + 1;
        }
        listTagCount.put(key, Integer.valueOf(value));
        return value;
    }
}
