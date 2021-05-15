package com.sec.internal.helper;

import android.text.TextUtils;
import android.util.Xml;
import java.io.IOException;
import java.util.StringTokenizer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlUtils {
    public static XmlPullParser newPullParser() {
        try {
            return Xml.newPullParser();
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

    public static void skipCurrentTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int next = parser.next();
            int type = next;
            if (next == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x000e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final void beginDocument(org.xmlpull.v1.XmlPullParser r4, java.lang.String r5) throws org.xmlpull.v1.XmlPullParserException, java.io.IOException {
        /*
        L_0x0000:
            int r0 = r4.next()
            r1 = r0
            r2 = 2
            if (r0 == r2) goto L_0x000c
            r0 = 1
            if (r1 == r0) goto L_0x000c
            goto L_0x0000
        L_0x000c:
            if (r1 != r2) goto L_0x003c
            java.lang.String r0 = r4.getName()
            boolean r0 = r0.equals(r5)
            if (r0 == 0) goto L_0x0019
            return
        L_0x0019:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Unexpected start tag: found "
            r2.append(r3)
            java.lang.String r3 = r4.getName()
            r2.append(r3)
            java.lang.String r3 = ", expected "
            r2.append(r3)
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            r0.<init>(r2)
            throw r0
        L_0x003c:
            org.xmlpull.v1.XmlPullParserException r0 = new org.xmlpull.v1.XmlPullParserException
            java.lang.String r2 = "No start tag found"
            r0.<init>(r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.XmlUtils.beginDocument(org.xmlpull.v1.XmlPullParser, java.lang.String):void");
    }

    public static boolean search(XmlPullParser xpp, String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(path, ".");
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        try {
            beginDocument(xpp, tokenizer.nextToken());
            xpp.nextTag();
            while (tokenizer.hasMoreTokens() && searchTag(xpp, tokenizer.nextToken())) {
                if (!tokenizer.hasMoreTokens()) {
                    return true;
                }
                xpp.nextTag();
            }
        } catch (XmlPullParserException e) {
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return false;
    }

    public static boolean searchTag(XmlPullParser xpp, String name) {
        while (true) {
            try {
                int eventType = xpp.getEventType();
                int event = eventType;
                if (eventType == 1) {
                    return false;
                }
                if (event != 2) {
                    xpp.nextTag();
                } else if (name.equalsIgnoreCase(xpp.getName())) {
                    return true;
                } else {
                    skipCurrentTag(xpp);
                }
            } catch (XmlPullParserException e) {
                return false;
            } catch (IOException e2) {
                e2.printStackTrace();
                return false;
            }
        }
    }
}
