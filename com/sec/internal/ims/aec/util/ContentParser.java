package com.sec.internal.ims.aec.util;

import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.AECLog;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ContentParser {
    private static final Map<String, List<String>> LIST_TAG_NAME = new TreeMap<String, List<String>>() {
        {
            put("application", (Object) null);
        }
    };
    private static final String LOG_TAG = ContentParser.class.getSimpleName();
    private static final String PATH_DIVIDER = "/";
    private static final String PATH_ROOT = "root";
    private static final String TAG_CHARACTERISTIC = "characteristic";
    private static final int TAG_CHARACTERISTIC_ATTR_COUNT = 1;
    private static final String TAG_CHARACTERISTIC_ATTR_TYPE = "type";
    private static final String TAG_PARAM = "param";
    private static final String TAG_PARM = "parm";
    private static final int TAG_PARM_ATTR_COUNT = 2;
    private static final String TAG_PARM_ATTR_NAME = "name";
    private static final String TAG_PARM_ATTR_VALUE = "value";
    private static final String TAG_WAPPROVISIONINGDOC = "wap-provisioningdoc";
    private static final int TAG_WAPPROVISIONINGDOC_ATTR_COUNT = 1;

    public static synchronized boolean isJSONValid(List<String> type) {
        synchronized (ContentParser.class) {
            if (type == null) {
                return false;
            }
            boolean contains = type.contains("application/vnd.gsma.eap-relay.v1.0+json");
            return contains;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0011, code lost:
        return false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isJSONValid(java.lang.String r3) {
        /*
            java.lang.Class<com.sec.internal.ims.aec.util.ContentParser> r0 = com.sec.internal.ims.aec.util.ContentParser.class
            monitor-enter(r0)
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ JSONException -> 0x000e, all -> 0x000b }
            r1.<init>(r3)     // Catch:{ JSONException -> 0x000e, all -> 0x000b }
            r1 = 1
            monitor-exit(r0)
            return r1
        L_0x000b:
            r3 = move-exception
            monitor-exit(r0)
            throw r3
        L_0x000e:
            r1 = move-exception
            r2 = 0
            monitor-exit(r0)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.ContentParser.isJSONValid(java.lang.String):boolean");
    }

    public static synchronized Map<String, String> parseJson(String body) throws Exception {
        Map<String, String> parsedJson;
        synchronized (ContentParser.class) {
            parsedJson = new TreeMap<>();
            try {
                if (!TextUtils.isEmpty(body)) {
                    JSONObject jsonObj = new JSONObject(body);
                    Iterator<String> keysItr = jsonObj.keys();
                    while (keysItr.hasNext()) {
                        String key = keysItr.next();
                        parsedJson.put("root/" + key, (String) jsonObj.get(key));
                    }
                }
            } catch (Exception e) {
                throw new Exception("parseJson: " + e.getMessage());
            }
        }
        return parsedJson;
    }

    public static synchronized Map<String, String> parseXml(String xml) throws Exception {
        Map<String, String> convertedMap;
        XmlPullParserFactory factory;
        XmlPullParserFactory factory2;
        synchronized (ContentParser.class) {
            Map<String, String> parsedXml = new TreeMap<>();
            try {
                XmlPullParserFactory factory3 = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory3.newPullParser();
                xpp.setInput(new StringReader(replaceXMLCharacters(xml)));
                int eventType = xpp.getEventType();
                List<String> paths = new ArrayList<>();
                List<String> unknownTags = new ArrayList<>();
                Map<String, Integer> listTagCount = new TreeMap<>();
                paths.add(PATH_ROOT);
                while (eventType != 1) {
                    if (eventType == 2) {
                        int attrCount = xpp.getAttributeCount();
                        String tagName = xpp.getName().toLowerCase(Locale.US);
                        if (tagName.equals(TAG_WAPPROVISIONINGDOC)) {
                            if (attrCount == 1) {
                                factory = factory3;
                            }
                        }
                        if (!tagName.equals(TAG_CHARACTERISTIC) || attrCount != 1) {
                            if (!tagName.equals(TAG_PARM)) {
                                if (!tagName.equals("param")) {
                                    factory2 = factory3;
                                    unknownTags.add(tagName);
                                }
                            }
                            if (attrCount == 2) {
                                String name = xpp.getAttributeName(0).toLowerCase(Locale.US);
                                String value = xpp.getAttributeValue(0).toLowerCase(Locale.US);
                                String name1 = xpp.getAttributeName(1).toLowerCase(Locale.US);
                                String value1 = xpp.getAttributeValue(1);
                                if (!name.equals("name") || !name1.equals("value")) {
                                    factory = factory3;
                                } else {
                                    paths.add(value);
                                    String pathFull = convertList(paths);
                                    if (checkListTag(paths, value)) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(value);
                                        factory = factory3;
                                        sb.append(PATH_DIVIDER);
                                        sb.append(increaseListTagCount(listTagCount, pathFull));
                                        paths.set(paths.size() - 1, sb.toString());
                                        pathFull = convertList(paths);
                                    } else {
                                        factory = factory3;
                                    }
                                    parsedXml.put(pathFull, value1);
                                }
                            } else {
                                factory2 = factory3;
                                unknownTags.add(tagName);
                            }
                        } else {
                            String name2 = xpp.getAttributeName(0).toLowerCase(Locale.US);
                            String value2 = xpp.getAttributeValue(0).toLowerCase(Locale.US);
                            if (name2.equals("type")) {
                                paths.add(value2);
                                if (checkListTag(paths, value2)) {
                                    paths.set(paths.size() - 1, value2 + PATH_DIVIDER + increaseListTagCount(listTagCount, convertList(paths)));
                                }
                            }
                            factory = factory3;
                        }
                    } else if (eventType != 3) {
                        factory = factory3;
                    } else {
                        String tagName2 = xpp.getName().toLowerCase(Locale.US);
                        if (unknownTags.contains(tagName2)) {
                            unknownTags.remove(tagName2);
                            factory = factory3;
                        } else {
                            paths.remove(paths.size() - 1);
                            factory = factory3;
                        }
                    }
                    eventType = xpp.next();
                    factory3 = factory;
                }
                convertedMap = convertedMap(parsedXml);
            } catch (Exception e) {
                throw new Exception("parseXml: " + e.getMessage());
            }
        }
        return convertedMap;
    }

    public static synchronized void debugPrint(int phoneId, Map<String, String> parsedDate) {
        synchronized (ContentParser.class) {
            if (!parsedDate.isEmpty()) {
                for (Map.Entry<String, String> entry : parsedDate.entrySet()) {
                    String str = LOG_TAG;
                    AECLog.s(str, entry.getKey() + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + entry.getValue(), phoneId);
                }
            }
        }
    }

    private static String convertList(List<String> datas) {
        StringBuilder result = new StringBuilder();
        for (String data : datas) {
            result.append(data);
            result.append(PATH_DIVIDER);
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private static boolean checkListTag(List<String> currentPaths, String currentTag) {
        if (!LIST_TAG_NAME.containsKey(currentTag)) {
            return false;
        }
        String pathFull = convertList(currentPaths);
        List<String> conditions = LIST_TAG_NAME.get(currentTag);
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

    private static int increaseListTagCount(Map<String, Integer> listTagCount, String key) {
        int value;
        if (listTagCount.containsKey(key)) {
            value = listTagCount.get(key).intValue() + 1;
        } else {
            value = 0;
        }
        listTagCount.put(key, Integer.valueOf(value));
        return value;
    }

    private static String replaceXMLCharacters(String xml) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new StringReader(xml));
        while (true) {
            try {
                String readLine = br.readLine();
                String line = readLine;
                if (readLine != null) {
                    sb.append(line.replaceAll("&", "&amp;"));
                } else {
                    br.close();
                    return sb.toString();
                }
            } catch (Exception e) {
                throw new Exception("replaceXMLCharacters: " + e.getMessage());
            }
        }
    }

    private static String convertServiceId(String applicationId) {
        if (applicationId.equalsIgnoreCase(AECNamespace.ApplicationId.APP_ID_VOLTE)) {
            return "volte";
        }
        if (applicationId.equalsIgnoreCase(AECNamespace.ApplicationId.APP_ID_VOWIFI)) {
            return "vowifi";
        }
        if (applicationId.equalsIgnoreCase(AECNamespace.ApplicationId.APP_ID_SMSOIP)) {
            return AECNamespace.ServiceId.SERVICE_ID_SMSOIP;
        }
        return applicationId;
    }

    private static Map<String, String> convertedMap(Map<String, String> parsedXml) {
        Map<String, String> convertedMap = new TreeMap<>();
        String serviceId_0 = convertServiceId(parsedXml.getOrDefault(AECNamespace.Path.APPLICATION_0_APPID, ""));
        String serviceId_1 = convertServiceId(parsedXml.getOrDefault(AECNamespace.Path.APPLICATION_1_APPID, ""));
        String serviceId_2 = convertServiceId(parsedXml.getOrDefault(AECNamespace.Path.APPLICATION_2_APPID, ""));
        for (String key : parsedXml.keySet()) {
            String value = parsedXml.get(key);
            if (key.contains(AECNamespace.Path.APPLICATION_0) && !TextUtils.isEmpty(serviceId_0)) {
                key = key.replace("0", serviceId_0.toLowerCase(Locale.ROOT));
            } else if (key.contains(AECNamespace.Path.APPLICATION_1) && !TextUtils.isEmpty(serviceId_1)) {
                key = key.replace("1", serviceId_1.toLowerCase(Locale.ROOT));
            } else if (key.contains(AECNamespace.Path.APPLICATION_2) && !TextUtils.isEmpty(serviceId_2)) {
                key = key.replace("2", serviceId_2.toLowerCase(Locale.ROOT));
            }
            convertedMap.put(key, value);
        }
        return convertedMap;
    }
}
