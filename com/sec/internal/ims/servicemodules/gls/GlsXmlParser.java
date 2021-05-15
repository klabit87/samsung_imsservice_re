package com.sec.internal.ims.servicemodules.gls;

import android.location.Location;
import com.sec.internal.constants.ims.servicemodules.gls.LocationType;
import com.sec.internal.log.IMSLog;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class GlsXmlParser {
    private static final String LOG_TAG = GlsXmlParser.class.getSimpleName();
    private XPathExpression mDatePath;
    private DocumentBuilder mDocumentBuilder;
    private XPathExpression mEntityPath;
    private XPathExpression mIdPath;
    private XPathExpression mLabelPath;
    private XPathExpression mLocationPath;
    private XPathExpression mPointLocationPath;
    private XPathExpression mRadiusPath;
    private XPathExpression mValidityDatePath;
    private XPathExpression mValidityTimezonePath;
    private XPath mXpath;

    public GlsXmlParser() {
        try {
            this.mDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mXpath = XPathFactory.newInstance().newXPath();
        this.mEntityPath = createXpathLazy("rcsenvelope/@entity");
        this.mIdPath = createXpathLazy("rcsenvelope/rcspushlocation/@id");
        this.mLabelPath = createXpathLazy("rcsenvelope/rcspushlocation/@label");
        this.mLocationPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Circle/pos");
        this.mRadiusPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Circle/radius");
        this.mPointLocationPath = createXpathLazy("rcsenvelope/rcspushlocation/geopriv/location-info/Point/pos");
        this.mDatePath = createXpathLazy("rcsenvelope/rcspushlocation/timestamp");
        this.mValidityDatePath = createXpathLazy("rcsenvelope/rcspushlocation/time-offset/@until");
        this.mValidityTimezonePath = createXpathLazy("rcsenvelope/rcspushlocation/time-offset");
    }

    /* Debug info: failed to restart local var, previous not found, register: 30 */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0099 A[Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }] */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x013d A[Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.gls.GlsData parse(java.lang.String r31) throws java.lang.Exception {
        /*
            r30 = this;
            r1 = r30
            java.io.ByteArrayInputStream r0 = new java.io.ByteArrayInputStream     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r2 = "utf-8"
            r3 = r31
            byte[] r2 = r3.getBytes(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r0.<init>(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r2 = r0
            javax.xml.parsers.DocumentBuilder r0 = r1.mDocumentBuilder     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            org.w3c.dom.Document r0 = r0.parse(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r4 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mEntityPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r5 = r0
            java.lang.String r0 = "entity"
            verifyNotEmpty(r5, r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            com.sec.ims.util.ImsUri r8 = com.sec.ims.util.ImsUri.parse(r5)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            javax.xml.xpath.XPathExpression r0 = r1.mIdPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r14 = r0
            java.lang.String r0 = "id"
            verifyNotEmpty(r14, r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            javax.xml.xpath.XPathExpression r0 = r1.mLabelPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r15 = r0
            if (r15 == 0) goto L_0x0041
            com.sec.internal.constants.ims.servicemodules.gls.LocationType r0 = com.sec.internal.constants.ims.servicemodules.gls.LocationType.OTHER_LOCATION     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r10 = r0
            goto L_0x0044
        L_0x0041:
            com.sec.internal.constants.ims.servicemodules.gls.LocationType r0 = com.sec.internal.constants.ims.servicemodules.gls.LocationType.OWN_LOCATION     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r10 = r0
        L_0x0044:
            r6 = 0
            javax.xml.xpath.XPathExpression r0 = r1.mPointLocationPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r13 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mLocationPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r12 = r0
            r0 = 0
            java.lang.String r9 = " "
            if (r13 == 0) goto L_0x0066
            boolean r11 = r13.isEmpty()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            if (r11 != 0) goto L_0x0066
            java.lang.String[] r9 = r13.split(r9)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r0 = r9
            goto L_0x0093
        L_0x0066:
            if (r12 == 0) goto L_0x008b
            boolean r11 = r12.isEmpty()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            if (r11 != 0) goto L_0x008b
            java.lang.String[] r9 = r12.split(r9)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r0 = r9
            javax.xml.xpath.XPathExpression r9 = r1.mRadiusPath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r9 = extractString(r9, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r11 = "radiusStr"
            verifyNotEmpty(r9, r11)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.Double r11 = java.lang.Double.valueOf(r9)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            double r16 = r11.doubleValue()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r6 = r16
            r9 = r0
            goto L_0x0093
        L_0x008b:
            java.lang.String r9 = LOG_TAG     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r11 = "Other type location, error!"
            android.util.Log.i(r9, r11)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r9 = r0
        L_0x0093:
            if (r9 == 0) goto L_0x013d
            int r0 = r9.length     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r11 = 2
            if (r0 != r11) goto L_0x013d
            r0 = 0
            r0 = r9[r0]     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.Double r0 = java.lang.Double.valueOf(r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            double r16 = r0.doubleValue()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r18 = r16
            r0 = 1
            r0 = r9[r0]     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.Double r0 = java.lang.Double.valueOf(r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            double r16 = r0.doubleValue()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r20 = r16
            android.location.Location r0 = new android.location.Location     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r11 = "passive"
            r0.<init>(r11)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r11 = r0
            r16 = r2
            r2 = r18
            r11.setLatitude(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r17 = r2
            r2 = r20
            r11.setLongitude(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            float r0 = (float) r6     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r11.setAccuracy(r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            javax.xml.xpath.XPathExpression r0 = r1.mDatePath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r19 = r0
            java.lang.String r0 = "dateString"
            r20 = r2
            r2 = r19
            verifyNotEmpty(r2, r0)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r3 = r11
            r11 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mValidityDatePath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r19 = r0
            javax.xml.xpath.XPathExpression r0 = r1.mValidityTimezonePath     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r0 = extractString(r0, r4)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r22 = r0
            r23 = 0
            r24 = 0
            if (r19 != 0) goto L_0x0101
            r0 = 0
            r1 = r23
            r23 = r2
            goto L_0x0129
        L_0x0101:
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r19)     // Catch:{ NumberFormatException -> 0x0116 }
            r23 = r0
            java.lang.Integer r0 = java.lang.Integer.valueOf(r22)     // Catch:{ NumberFormatException -> 0x0116 }
            int r0 = r0.intValue()     // Catch:{ NumberFormatException -> 0x0116 }
            r24 = r0
            r0 = r23
            r1 = r24
            goto L_0x011e
        L_0x0116:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r0 = r23
            r1 = r24
        L_0x011e:
            r23 = r2
            com.sec.internal.ims.servicemodules.gls.GlsValidityTime r2 = new com.sec.internal.ims.servicemodules.gls.GlsValidityTime     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r2.<init>(r0, r1)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r24 = r1
            r1 = r0
            r0 = r2
        L_0x0129:
            com.sec.internal.ims.servicemodules.gls.GlsData r2 = new com.sec.internal.ims.servicemodules.gls.GlsData     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r25 = r6
            r6 = r2
            r7 = r14
            r27 = r9
            r9 = r3
            r28 = r1
            r1 = r12
            r12 = r15
            r29 = r13
            r13 = r0
            r6.<init>(r7, r8, r9, r10, r11, r12, r13)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            return r2
        L_0x013d:
            r16 = r2
            r25 = r6
            r27 = r9
            r1 = r12
            r29 = r13
            java.lang.Exception r0 = new java.lang.Exception     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r2.<init>()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r3 = "Could not parse location string: "
            r2.append(r3)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r2.append(r1)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            java.lang.String r2 = r2.toString()     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            r0.<init>(r2)     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
            throw r0     // Catch:{ IOException -> 0x0167, SAXException -> 0x0165, XPathExpressionException -> 0x0163, URISyntaxException -> 0x0161, ParseException -> 0x015f, Exception -> 0x015d }
        L_0x015d:
            r0 = move-exception
            throw r0
        L_0x015f:
            r0 = move-exception
            throw r0
        L_0x0161:
            r0 = move-exception
            throw r0
        L_0x0163:
            r0 = move-exception
            throw r0
        L_0x0165:
            r0 = move-exception
            throw r0
        L_0x0167:
            r0 = move-exception
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.gls.GlsXmlParser.parse(java.lang.String):com.sec.internal.ims.servicemodules.gls.GlsData");
    }

    public String getGeolocString(String xml) throws Exception {
        GlsData data = parse(xml);
        GlsValidityTime validityDate = data.getValidityDate();
        Location location = data.getLocation();
        String label = data.getLabel();
        return location.getLatitude() + "-" + location.getLongitude() + "-" + location.getAccuracy() + "-" + validityDate.getValidityDate().getTime() + "-" + label;
    }

    public String getGlsExtInfo(String body) {
        long validitytime;
        String str = LOG_TAG;
        IMSLog.s(str, "body=" + body);
        try {
            GlsData data = parse(body);
            GlsValidityTime validityDate = data.getValidityDate();
            Location location = data.getLocation();
            LocationType type = data.getLocationType();
            String label = type == LocationType.OWN_LOCATION ? "" : data.getLabel();
            if (validityDate != null) {
                if (validityDate.getValidityDate() != null) {
                    validitytime = validityDate.getValidityDate().getTime();
                    return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + validitytime + "," + label + "," + type.toString();
                }
            }
            validitytime = data.getDate().getTime();
            return location.getLatitude() + "," + location.getLongitude() + "," + location.getAccuracy() + "," + validitytime + "," + label + "," + type.toString();
        } catch (Exception e) {
            IMSLog.s(LOG_TAG, e.toString());
            return null;
        }
    }

    private XPathExpression createXpathLazy(String xpath) {
        try {
            return this.mXpath.compile(xpath);
        } catch (XPathExpressionException e) {
            throw new Error(e);
        }
    }

    private static String extractString(XPathExpression expressionLazy, Document context) throws XPathExpressionException {
        Node node = (Node) expressionLazy.evaluate(context, XPathConstants.NODE);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }

    private static void verifyNotEmpty(String toCheck, String fieldName) throws Exception {
        if (toCheck == null || toCheck.isEmpty()) {
            throw new Exception(fieldName + " is empty!");
        }
    }
}
