package com.sec.internal.omanetapi.nms;

import android.util.Log;
import com.sec.internal.omanetapi.nms.data.GroupState;
import com.sec.internal.omanetapi.nms.data.Part;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlParser {
    public static final String LOG_TAG = XmlParser.class.getSimpleName();
    public static final String TAG_GROUP_STATE = "groupstate";
    public static final String TAG_GROUP_STATE_ATTR_CONTRIBUTIONID = "contributionid";
    public static final String TAG_GROUP_STATE_ATTR_GROUP_TYPE = "group-type";
    public static final String TAG_GROUP_STATE_ATTR_LASTFOCUSSESSIONID = "lastfocussessionid";
    public static final String TAG_GROUP_STATE_ATTR_TIMESTAMP = "timestamp";
    public static final String TAG_PARTICIPANT = "participant";
    public static final String TAG_PARTICIPANT_ATTR_COMMADDR = "comm-addr";
    public static final String TAG_PARTICIPANT_ATTR_NAME = "name";
    public static final String TAG_PARTICIPANT_ATTR_ROLE = "role";

    public static GroupState parseGroupState(String xml) {
        GroupState rt = new GroupState();
        try {
            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            xpp.setInput(new StringReader(xml));
            for (int eventType = xpp.getEventType(); eventType != 1; eventType = xpp.next()) {
                if (eventType == 0) {
                    Log.i(LOG_TAG, "Start document");
                } else if (eventType == 2) {
                    String tagName = xpp.getName().toLowerCase(Locale.US);
                    String str = LOG_TAG;
                    Log.i(str, "start tagName:" + tagName);
                    int attrCount = xpp.getAttributeCount();
                    if (TAG_GROUP_STATE.equals(tagName)) {
                        for (int i = 0; i < attrCount; i++) {
                            String attrName = xpp.getAttributeName(i).toLowerCase(Locale.US);
                            String attrValue = xpp.getAttributeValue(i).toLowerCase(Locale.US);
                            if ("timestamp".equals(attrName)) {
                                rt.timestamp = attrValue;
                            } else if (TAG_GROUP_STATE_ATTR_LASTFOCUSSESSIONID.equals(attrName)) {
                                rt.lastfocussessionid = attrValue;
                            } else if (TAG_GROUP_STATE_ATTR_GROUP_TYPE.equals(attrName)) {
                                rt.group_type = attrValue;
                            } else if (TAG_GROUP_STATE_ATTR_CONTRIBUTIONID.equals(attrName)) {
                                rt.contributionid = attrValue;
                            } else {
                                String str2 = LOG_TAG;
                                Log.e(str2, "Unknown attrName:" + attrName);
                            }
                        }
                    } else if ("participant".equals(tagName)) {
                        Part pt = new Part();
                        for (int i2 = 0; i2 < attrCount; i2++) {
                            String attrName2 = xpp.getAttributeName(i2).toLowerCase(Locale.US);
                            String attrValue2 = xpp.getAttributeValue(i2).toLowerCase(Locale.US);
                            PrintStream printStream = System.out;
                            printStream.println("attrname>" + attrName2 + ";attrValue>" + attrValue2);
                            if ("name".equals(attrName2)) {
                                pt.name = attrValue2;
                            } else if (TAG_PARTICIPANT_ATTR_COMMADDR.equals(attrName2)) {
                                pt.comm_addr = attrValue2;
                            } else if (TAG_PARTICIPANT_ATTR_ROLE.equals(attrName2)) {
                                pt.role = attrValue2;
                            } else {
                                String str3 = LOG_TAG;
                                Log.e(str3, "Unknown attrName:" + attrName2);
                            }
                        }
                        rt.participantList.add(pt);
                    } else {
                        String str4 = LOG_TAG;
                        Log.e(str4, "Unknown tagName:" + tagName);
                    }
                } else if (eventType == 3) {
                    String tagName2 = xpp.getName().toLowerCase(Locale.US);
                    String str5 = LOG_TAG;
                    Log.i(str5, "end tagName:" + tagName2);
                } else if (eventType == 4) {
                    Log.e(LOG_TAG, "unhandled element");
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return rt;
    }
}
