package com.sec.internal.ims.servicemodules.volte2.util;

import android.util.Log;
import com.sec.ims.DialogEvent;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import java.text.ParseException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class DialogXmlParser {
    private static final int CMC_TYPE_NONE = 0;
    private static final int CMC_TYPE_PRIMARY = 1;
    private static final int CMC_TYPE_SECONDARY = 2;
    private static final int CMC_WIFI_HS_TYPE_PRIMARY = 5;
    private static final int CMC_WIFI_HS_TYPE_SECONDARY = 6;
    private static final int CMC_WIFI_P2P_TYPE_PRIMARY = 7;
    private static final int CMC_WIFI_P2P_TYPE_SECONDARY = 8;
    private static final int CMC_WIFI_TYPE_PRIMARY = 3;
    private static final int CMC_WIFI_TYPE_SECONDARY = 4;
    private static final String LOG_TAG = DialogXmlParser.class.getSimpleName();
    private static DialogXmlParser sInstance = null;
    private XPath mXPath;
    private XPathExpression mXPathCallId;
    private XPathExpression mXPathCode;
    private XPathExpression mXPathDialog;
    private XPathExpression mXPathDialogInfo;
    private XPathExpression mXPathDirection;
    private XPathExpression mXPathEntity;
    private XPathExpression mXPathEvent;
    private XPathExpression mXPathExclusive;
    private XPathExpression mXPathId;
    private XPathExpression mXPathLocalDisplay;
    private XPathExpression mXPathLocalDisplayName;
    private XPathExpression mXPathLocalIdentity;
    private XPathExpression mXPathLocalTag;
    private XPathExpression mXPathLocalTarget;
    private XPathExpression mXPathLocalUri;
    private XPathExpression mXPathMediaAttributes;
    private XPathExpression mXPathMediaDirection;
    private XPathExpression mXPathMediaPortZero;
    private XPathExpression mXPathMediaType;
    private XPathExpression mXPathRemoteDisplay;
    private XPathExpression mXPathRemoteDisplayName;
    private XPathExpression mXPathRemoteIdentity;
    private XPathExpression mXPathRemoteTag;
    private XPathExpression mXPathSessionDesc;
    private XPathExpression mXPathSipInstance;
    private XPathExpression mXPathSipRendering;
    private XPathExpression mXPathState;

    public static DialogXmlParser getInstance() {
        if (sInstance == null) {
            sInstance = new DialogXmlParser();
        }
        return sInstance;
    }

    private void init() {
        XPath newXPath = XPathFactory.newInstance().newXPath();
        this.mXPath = newXPath;
        newXPath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if ("dins".equals(prefix)) {
                    return "urn:ietf:params:xml:ns:dialog-info";
                }
                if ("sa".equals(prefix)) {
                    return "urn:ietf:params:xml:ns:sa-dialog-info";
                }
                return "";
            }

            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }

            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        });
        try {
            this.mXPathDialogInfo = this.mXPath.compile("/dins:dialog-info");
            this.mXPathEntity = this.mXPath.compile("@entity");
            this.mXPathDialog = this.mXPath.compile("dins:dialog");
            this.mXPathId = this.mXPath.compile("@id");
            this.mXPathCallId = this.mXPath.compile("@call-id");
            this.mXPathLocalTag = this.mXPath.compile("@local-tag");
            this.mXPathRemoteTag = this.mXPath.compile("@remote-tag");
            this.mXPathDirection = this.mXPath.compile("@direction");
            this.mXPathExclusive = this.mXPath.compile("sa:exclusive");
            this.mXPathState = this.mXPath.compile("dins:state");
            this.mXPathEvent = this.mXPath.compile("dins:state/@event");
            this.mXPathCode = this.mXPath.compile("dins:state/@code");
            this.mXPathLocalIdentity = this.mXPath.compile("dins:local/dins:identity");
            this.mXPathLocalDisplayName = this.mXPath.compile("dins:local/dins:identity/@display-name");
            this.mXPathLocalDisplay = this.mXPath.compile("dins:local/dins:identity/@display");
            this.mXPathLocalUri = this.mXPath.compile("dins:local/dins:target/@uri");
            this.mXPathLocalTarget = this.mXPath.compile("dins:local/dins:target");
            this.mXPathSessionDesc = this.mXPath.compile("dins:session-description");
            this.mXPathSipInstance = this.mXPath.compile("dins:local/dins:target/dins:param[@pname='+sip.instance']/@pval");
            this.mXPathSipRendering = this.mXPath.compile("dins:local/dins:target/dins:param[@pname='+sip.rendering']/@pval");
            this.mXPathMediaAttributes = this.mXPath.compile("dins:local/dins:mediaAttributes");
            this.mXPathMediaType = this.mXPath.compile("dins:mediaType");
            this.mXPathMediaDirection = this.mXPath.compile("dins:mediaDirection");
            this.mXPathMediaPortZero = this.mXPath.compile("dins:port0");
            this.mXPathRemoteIdentity = this.mXPath.compile("dins:remote/dins:identity");
            this.mXPathRemoteDisplayName = this.mXPath.compile("dins:remote/dins:identity/@display-name");
            this.mXPathRemoteDisplay = this.mXPath.compile("dins:remote/dins:identity/@display");
        } catch (XPathExpressionException e) {
            Log.e(LOG_TAG, "XPath compile failed!", e);
        }
    }

    private DialogXmlParser() {
        init();
    }

    private int convertDialogDirection(String direction) throws ParseException {
        if ("initiator".equals(direction)) {
            return 0;
        }
        if (CloudMessageProviderContract.VVMMessageColumns.RECIPIENT.equals(direction)) {
            return 1;
        }
        throw new ParseException("invalid direction: " + direction, 0);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int convertDialogState(int r8, java.lang.String r9, java.lang.String r10, java.lang.String r11) throws java.text.ParseException {
        /*
            r7 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "convertDialogState(): "
            r1.append(r2)
            r1.append(r9)
            java.lang.String r2 = " / "
            r1.append(r2)
            r1.append(r10)
            r1.append(r2)
            r1.append(r11)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            boolean r0 = android.text.TextUtils.isEmpty(r11)
            r1 = 0
            if (r0 == 0) goto L_0x002d
            r0 = r1
            goto L_0x0031
        L_0x002d:
            int r0 = java.lang.Integer.parseInt(r11)
        L_0x0031:
            int r2 = r9.hashCode()
            r3 = -1
            r4 = 3
            r5 = 2
            r6 = 1
            switch(r2) {
                case -1308815837: goto L_0x005c;
                case -864995257: goto L_0x0051;
                case -804109473: goto L_0x0047;
                case 96278371: goto L_0x003d;
                default: goto L_0x003c;
            }
        L_0x003c:
            goto L_0x0067
        L_0x003d:
            java.lang.String r2 = "early"
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x003c
            r2 = r6
            goto L_0x0068
        L_0x0047:
            java.lang.String r2 = "confirmed"
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x003c
            r2 = r5
            goto L_0x0068
        L_0x0051:
            java.lang.String r2 = "trying"
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x003c
            r2 = r1
            goto L_0x0068
        L_0x005c:
            java.lang.String r2 = "terminated"
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x003c
            r2 = r4
            goto L_0x0068
        L_0x0067:
            r2 = r3
        L_0x0068:
            if (r2 == 0) goto L_0x0088
            if (r2 == r6) goto L_0x0081
            if (r2 == r5) goto L_0x0080
            if (r2 == r4) goto L_0x0071
            goto L_0x008c
        L_0x0071:
            java.lang.String r1 = "rejected"
            boolean r1 = r1.equals(r10)
            if (r1 == 0) goto L_0x007f
            r1 = 486(0x1e6, float:6.81E-43)
            if (r0 != r1) goto L_0x007f
            return r4
        L_0x007f:
            return r5
        L_0x0080:
            return r6
        L_0x0081:
            if (r8 != r6) goto L_0x008c
            r2 = 180(0xb4, float:2.52E-43)
            if (r0 != r2) goto L_0x008c
            return r1
        L_0x0088:
            if (r8 != 0) goto L_0x008c
            r1 = 4
            return r1
        L_0x008c:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "convertDialogState(): ignoring"
            android.util.Log.i(r1, r2)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser.convertDialogState(int, java.lang.String, java.lang.String, java.lang.String):int");
    }

    private String getDeviceIdFromSipInstanceId(String sipInstanceId) throws ParseException {
        Matcher m = Pattern.compile("urn:gsma:imei:([0-9-]+)").matcher(sipInstanceId);
        if (m.matches()) {
            return m.group(1).replaceAll("[^0-9]", "");
        }
        throw new ParseException("invalid instance id: " + sipInstanceId, 0);
    }

    private int convertDialogCallType(String mediaType) throws ParseException {
        if (TMOConstants.CallLogTypes.VIDEO.equalsIgnoreCase(mediaType)) {
            return 2;
        }
        return 1;
    }

    private int convertDialogMediaDirection(String mediaDirection) throws ParseException {
        if ("sendrecv".equalsIgnoreCase(mediaDirection)) {
            return 4;
        }
        if ("recvonly".equalsIgnoreCase(mediaDirection)) {
            return 3;
        }
        if ("sendonly".equalsIgnoreCase(mediaDirection)) {
            return 2;
        }
        if ("inactive".equalsIgnoreCase(mediaDirection)) {
            return 1;
        }
        return 0;
    }

    private int convertDialogCallState(String sipRendering) throws ParseException {
        if ("no".equalsIgnoreCase(sipRendering)) {
            return 2;
        }
        return 1;
    }

    public DialogEvent parseDialogInfoXml(String dialogInfoXml) throws XPathExpressionException {
        return parseDialogInfoXml(dialogInfoXml, 0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x0266 A[SYNTHETIC, Splitter:B:107:0x0266] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x027b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.ims.DialogEvent parseDialogInfoXml(java.lang.String r55, int r56) throws javax.xml.xpath.XPathExpressionException {
        /*
            r54 = this;
            r1 = r54
            r2 = r56
            org.xml.sax.InputSource r0 = new org.xml.sax.InputSource
            java.io.StringReader r3 = new java.io.StringReader
            r4 = r55
            r3.<init>(r4)
            r0.<init>(r3)
            r3 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathDialogInfo
            javax.xml.namespace.QName r5 = javax.xml.xpath.XPathConstants.NODE
            java.lang.Object r0 = r0.evaluate(r3, r5)
            r5 = r0
            org.w3c.dom.Node r5 = (org.w3c.dom.Node) r5
            javax.xml.xpath.XPathExpression r0 = r1.mXPathEntity
            java.lang.String r6 = r0.evaluate(r5)
            com.sec.ims.util.ImsUri r7 = com.sec.ims.util.ImsUri.parse(r6)
            java.lang.String r0 = r7.getMsisdn()
            if (r0 != 0) goto L_0x0032
            java.lang.String r0 = r7.getUser()
            r8 = r0
            goto L_0x0033
        L_0x0032:
            r8 = r0
        L_0x0033:
            javax.xml.xpath.XPathExpression r0 = r1.mXPathDialog
            javax.xml.namespace.QName r9 = javax.xml.xpath.XPathConstants.NODESET
            java.lang.Object r0 = r0.evaluate(r5, r9)
            r9 = r0
            org.w3c.dom.NodeList r9 = (org.w3c.dom.NodeList) r9
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r10 = r0
            r0 = 0
            r11 = r0
        L_0x0046:
            int r0 = r9.getLength()
            if (r11 >= r0) goto L_0x0311
            org.w3c.dom.Node r12 = r9.item(r11)
            r13 = 0
            r14 = 0
            r15 = 0
            r16 = 0
            r17 = 0
            r18 = 0
            java.lang.String r19 = ""
            java.lang.String r20 = ""
            java.lang.String r21 = ""
            java.lang.String r22 = ""
            java.lang.String r23 = ""
            java.lang.String r24 = ""
            java.lang.String r25 = ""
            java.lang.String r26 = ""
            java.lang.String r27 = ""
            java.lang.String r28 = ""
            java.lang.String r29 = ""
            r30 = 0
            r31 = 0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathDirection     // Catch:{ ParseException -> 0x02b3 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02b3 }
            int r0 = r1.convertDialogDirection(r0)     // Catch:{ ParseException -> 0x02b3 }
            r14 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathState     // Catch:{ ParseException -> 0x02b3 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02b3 }
            r32 = r3
            javax.xml.xpath.XPathExpression r3 = r1.mXPathEvent     // Catch:{ ParseException -> 0x02ac }
            java.lang.String r3 = r3.evaluate(r12)     // Catch:{ ParseException -> 0x02ac }
            javax.xml.xpath.XPathExpression r4 = r1.mXPathCode     // Catch:{ ParseException -> 0x02ac }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02ac }
            int r0 = r1.convertDialogState(r14, r0, r3, r4)     // Catch:{ ParseException -> 0x02ac }
            r13 = r0
            if (r13 >= 0) goto L_0x009d
            r34 = r5
            goto L_0x0305
        L_0x009d:
            javax.xml.xpath.XPathExpression r0 = r1.mXPathMediaAttributes     // Catch:{ ParseException -> 0x02ac }
            javax.xml.namespace.QName r3 = javax.xml.xpath.XPathConstants.NODESET     // Catch:{ ParseException -> 0x02ac }
            java.lang.Object r0 = r0.evaluate(r12, r3)     // Catch:{ ParseException -> 0x02ac }
            org.w3c.dom.NodeList r0 = (org.w3c.dom.NodeList) r0     // Catch:{ ParseException -> 0x02ac }
            int r3 = r0.getLength()     // Catch:{ ParseException -> 0x02ac }
            if (r3 <= 0) goto L_0x0124
            r3 = 0
        L_0x00ae:
            int r4 = r0.getLength()     // Catch:{ ParseException -> 0x02ac }
            if (r3 >= r4) goto L_0x011f
            org.w3c.dom.Node r4 = r0.item(r3)     // Catch:{ ParseException -> 0x02ac }
            r33 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathMediaType     // Catch:{ ParseException -> 0x02ac }
            java.lang.String r0 = r0.evaluate(r4)     // Catch:{ ParseException -> 0x02ac }
            int r0 = r1.convertDialogCallType(r0)     // Catch:{ ParseException -> 0x02ac }
            if (r15 != 0) goto L_0x00ca
            r15 = r0
            r34 = r5
            goto L_0x00d3
        L_0x00ca:
            r34 = r5
            r5 = 2
            if (r15 == r5) goto L_0x00d3
            if (r0 != r5) goto L_0x00d3
            r5 = r0
            r15 = r5
        L_0x00d3:
            r5 = 1
            if (r0 == r5) goto L_0x010a
            r5 = 2
            if (r0 == r5) goto L_0x00e6
            javax.xml.xpath.XPathExpression r5 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r5 = r5.evaluate(r4)     // Catch:{ ParseException -> 0x02a7 }
            int r5 = r1.convertDialogMediaDirection(r5)     // Catch:{ ParseException -> 0x02a7 }
            r17 = r5
            goto L_0x0118
        L_0x00e6:
            javax.xml.xpath.XPathExpression r5 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r5 = r5.evaluate(r4)     // Catch:{ ParseException -> 0x02a7 }
            int r5 = r1.convertDialogMediaDirection(r5)     // Catch:{ ParseException -> 0x02a7 }
            r18 = r5
            javax.xml.xpath.XPathExpression r5 = r1.mXPathMediaPortZero     // Catch:{ ParseException -> 0x02a7 }
            r35 = r0
            javax.xml.namespace.QName r0 = javax.xml.xpath.XPathConstants.NODESET     // Catch:{ ParseException -> 0x02a7 }
            java.lang.Object r0 = r5.evaluate(r4, r0)     // Catch:{ ParseException -> 0x02a7 }
            org.w3c.dom.NodeList r0 = (org.w3c.dom.NodeList) r0     // Catch:{ ParseException -> 0x02a7 }
            int r0 = r0.getLength()     // Catch:{ ParseException -> 0x02a7 }
            if (r0 <= 0) goto L_0x0106
            r0 = 1
            goto L_0x0107
        L_0x0106:
            r0 = 0
        L_0x0107:
            r31 = r0
            goto L_0x0118
        L_0x010a:
            r35 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathMediaDirection     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r0 = r0.evaluate(r4)     // Catch:{ ParseException -> 0x02a7 }
            int r0 = r1.convertDialogMediaDirection(r0)     // Catch:{ ParseException -> 0x02a7 }
            r17 = r0
        L_0x0118:
            int r3 = r3 + 1
            r0 = r33
            r5 = r34
            goto L_0x00ae
        L_0x011f:
            r33 = r0
            r34 = r5
            goto L_0x0128
        L_0x0124:
            r33 = r0
            r34 = r5
        L_0x0128:
            javax.xml.xpath.XPathExpression r0 = r1.mXPathId     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r19 = r0
            r0 = 8
            r3 = 4
            r4 = 2
            if (r2 == r4) goto L_0x013a
            if (r2 == r3) goto L_0x013a
            if (r2 != r0) goto L_0x0156
        L_0x013a:
            int r4 = r19.length()     // Catch:{ ParseException -> 0x02a7 }
            if (r4 <= 0) goto L_0x0156
            r4 = 31
            r5 = 1
            int r35 = r5 * 31
            int r36 = r19.hashCode()     // Catch:{ ParseException -> 0x02a7 }
            r37 = 2147483647(0x7fffffff, float:NaN)
            r36 = r36 & r37
            int r35 = r35 + r36
            java.lang.String r5 = java.lang.Integer.toString(r35)     // Catch:{ ParseException -> 0x02a7 }
            r19 = r5
        L_0x0156:
            javax.xml.xpath.XPathExpression r4 = r1.mXPathCallId     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r21 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathLocalTag     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r22 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathRemoteTag     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r23 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathLocalIdentity     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r24 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathRemoteIdentity     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r25 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathLocalDisplay     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            r26 = r4
            javax.xml.xpath.XPathExpression r4 = r1.mXPathSessionDesc     // Catch:{ ParseException -> 0x02a7 }
            java.lang.String r4 = r4.evaluate(r12)     // Catch:{ ParseException -> 0x02a7 }
            boolean r5 = android.text.TextUtils.isEmpty(r26)     // Catch:{ ParseException -> 0x02a0 }
            r0 = 1
            if (r5 != r0) goto L_0x019b
            javax.xml.xpath.XPathExpression r0 = r1.mXPathLocalDisplayName     // Catch:{ ParseException -> 0x02a0 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02a0 }
            r26 = r0
        L_0x019b:
            javax.xml.xpath.XPathExpression r0 = r1.mXPathRemoteDisplay     // Catch:{ ParseException -> 0x02a0 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02a0 }
            r27 = r0
            boolean r0 = android.text.TextUtils.isEmpty(r27)     // Catch:{ ParseException -> 0x02a0 }
            r5 = 1
            if (r0 != r5) goto L_0x01b2
            javax.xml.xpath.XPathExpression r0 = r1.mXPathRemoteDisplayName     // Catch:{ ParseException -> 0x02a0 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02a0 }
            r27 = r0
        L_0x01b2:
            javax.xml.xpath.XPathExpression r0 = r1.mXPathSipRendering     // Catch:{ ParseException -> 0x02a0 }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x02a0 }
            r5 = r0
            int r0 = r1.convertDialogCallState(r5)     // Catch:{ ParseException -> 0x0297 }
            r16 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathExclusive     // Catch:{ ParseException -> 0x028c }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x028c }
            boolean r0 = java.lang.Boolean.parseBoolean(r0)     // Catch:{ ParseException -> 0x028c }
            r30 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mXPathSipInstance     // Catch:{ ParseException -> 0x028c }
            java.lang.String r0 = r0.evaluate(r12)     // Catch:{ ParseException -> 0x028c }
            boolean r28 = android.text.TextUtils.isEmpty(r0)     // Catch:{ ParseException -> 0x028c }
            if (r28 != 0) goto L_0x01e0
            java.lang.String r28 = r1.getDeviceIdFromSipInstanceId(r0)     // Catch:{ ParseException -> 0x0297 }
            r20 = r28
            r3 = r17
            goto L_0x01f6
        L_0x01e0:
            javax.xml.xpath.XPathExpression r3 = r1.mXPathLocalTarget     // Catch:{ ParseException -> 0x028c }
            java.lang.String r3 = r3.evaluate(r12)     // Catch:{ ParseException -> 0x028c }
            if (r17 != 0) goto L_0x01ea
            r17 = 4
        L_0x01ea:
            if (r15 != 0) goto L_0x01f2
            r15 = 1
            r20 = r3
            r3 = r17
            goto L_0x01f6
        L_0x01f2:
            r20 = r3
            r3 = r17
        L_0x01f6:
            r28 = r0
            r0 = 2
            if (r2 == r0) goto L_0x0202
            r0 = 4
            if (r2 == r0) goto L_0x0202
            r0 = 8
            if (r2 != r0) goto L_0x025c
        L_0x0202:
            int r0 = r19.length()     // Catch:{ ParseException -> 0x027f }
            if (r0 <= 0) goto L_0x025c
            java.lang.String r0 = "*31#"
            boolean r0 = r4.startsWith(r0)     // Catch:{ ParseException -> 0x0250 }
            if (r0 != 0) goto L_0x0218
            java.lang.String r0 = "#31#"
            boolean r0 = r4.startsWith(r0)     // Catch:{ ParseException -> 0x0250 }
            if (r0 == 0) goto L_0x0228
        L_0x0218:
            r0 = 4
            java.lang.String r17 = r4.substring(r0)     // Catch:{ ParseException -> 0x0250 }
            r29 = r17
            java.lang.String r0 = LOG_TAG     // Catch:{ ParseException -> 0x0246 }
            java.lang.String r4 = "Remove CLIR prefix"
            android.util.Log.i(r0, r4)     // Catch:{ ParseException -> 0x0246 }
            r4 = r29
        L_0x0228:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x0250 }
            r0.<init>()     // Catch:{ ParseException -> 0x0250 }
            java.lang.String r1 = "sip:"
            r0.append(r1)     // Catch:{ ParseException -> 0x0250 }
            r0.append(r4)     // Catch:{ ParseException -> 0x0250 }
            java.lang.String r0 = r0.toString()     // Catch:{ ParseException -> 0x0250 }
            r25 = r0
            java.lang.String r0 = LOG_TAG     // Catch:{ ParseException -> 0x0250 }
            java.lang.String r1 = "[CMC] Displayname on pulling UI : use session-description value."
            android.util.Log.i(r0, r1)     // Catch:{ ParseException -> 0x0250 }
            r29 = r4
            goto L_0x025e
        L_0x0246:
            r0 = move-exception
            r1 = r0
            r17 = r3
            r28 = r5
            r0 = r19
            goto L_0x02bb
        L_0x0250:
            r0 = move-exception
            r1 = r0
            r17 = r3
            r29 = r4
            r28 = r5
            r0 = r19
            goto L_0x02bb
        L_0x025c:
            r29 = r4
        L_0x025e:
            r1 = r16
            r0 = 2
            if (r1 == r0) goto L_0x027b
            r0 = 4
            if (r3 == r0) goto L_0x027b
            java.lang.String r0 = LOG_TAG     // Catch:{ ParseException -> 0x0270 }
            java.lang.String r4 = "HELD call check by Audio Direction"
            android.util.Log.i(r0, r4)     // Catch:{ ParseException -> 0x0270 }
            r16 = 2
            goto L_0x027d
        L_0x0270:
            r0 = move-exception
            r16 = r1
            r17 = r3
            r28 = r5
            r1 = r0
            r0 = r19
            goto L_0x02bb
        L_0x027b:
            r16 = r1
        L_0x027d:
            goto L_0x02d7
        L_0x027f:
            r0 = move-exception
            r1 = r16
            r17 = r3
            r29 = r4
            r28 = r5
            r1 = r0
            r0 = r19
            goto L_0x02bb
        L_0x028c:
            r0 = move-exception
            r1 = r16
            r29 = r4
            r28 = r5
            r1 = r0
            r0 = r19
            goto L_0x02bb
        L_0x0297:
            r0 = move-exception
            r1 = r0
            r29 = r4
            r28 = r5
            r0 = r19
            goto L_0x02bb
        L_0x02a0:
            r0 = move-exception
            r1 = r0
            r29 = r4
            r0 = r19
            goto L_0x02bb
        L_0x02a7:
            r0 = move-exception
            r1 = r0
            r0 = r19
            goto L_0x02bb
        L_0x02ac:
            r0 = move-exception
            r34 = r5
            r1 = r0
            r0 = r19
            goto L_0x02bb
        L_0x02b3:
            r0 = move-exception
            r32 = r3
            r34 = r5
            r1 = r0
            r0 = r19
        L_0x02bb:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "Parse error for dialog id "
            r4.append(r5)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            android.util.Log.e(r3, r4, r1)
            r19 = r0
            r3 = r17
            r5 = r28
        L_0x02d7:
            com.sec.ims.Dialog r0 = new com.sec.ims.Dialog
            r35 = r0
            r36 = r19
            r37 = r20
            r38 = r21
            r39 = r22
            r40 = r23
            r41 = r24
            r42 = r25
            r43 = r26
            r44 = r27
            r45 = r29
            r46 = r13
            r47 = r14
            r48 = r15
            r49 = r16
            r50 = r3
            r51 = r18
            r52 = r30
            r53 = r31
            r35.<init>(r36, r37, r38, r39, r40, r41, r42, r43, r44, r45, r46, r47, r48, r49, r50, r51, r52, r53)
            r10.add(r0)
        L_0x0305:
            int r11 = r11 + 1
            r1 = r54
            r4 = r55
            r3 = r32
            r5 = r34
            goto L_0x0046
        L_0x0311:
            r32 = r3
            r34 = r5
            com.sec.ims.DialogEvent r0 = new com.sec.ims.DialogEvent
            r0.<init>(r8, r10)
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "parsed dialog xml: "
            r3.append(r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            r4.append(r0)
            java.lang.String r5 = ""
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            java.lang.String r4 = com.sec.internal.log.IMSLog.checker(r4)
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.i(r1, r3)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.util.DialogXmlParser.parseDialogInfoXml(java.lang.String, int):com.sec.ims.DialogEvent");
    }
}
