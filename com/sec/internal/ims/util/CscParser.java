package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class CscParser {
    private static final String COUNTRY_ISO_PATH = "CustomerData.GeneralInfo.CountryISO";
    private static final String CSC_EDITION_PATH = "CustomerData.GeneralInfo.CSCEdition";
    private static final String CSC_SW_CONFIG_FILE_PATH = "/system/SW_Configuration.xml";
    private static final String CUSTOMER_CSC_FILE_NAME = "/customer.xml";
    private static final String CUSTOMER_CSC_FILE_PATH = "/system/csc";
    private static final String IMSSETTING_STR_VERSION_PILOT = "PILOT";
    private static final String IMSSETTING_VERSION_PATH = "CustomerData.Settings.IMSSetting.Setting.Version";
    private static final String IMSSETTING_VERSION_PILOT = "1.0";
    private static final String IMS_PATH = "CustomerData.Settings.IMSSetting.NbSetting";
    private static final String KEY_CSC_EDITION = "csc.key.edition";
    private static final String KEY_CSC_SALES_CODE = "csc.key.salescode";
    private static final String KEY_CSC_VERSION = "csc.key.version";
    private static final String KEY_OMC_VERSION = "omc.key.version";
    private static final String LOG_TAG = "CscParser";
    private static final String NETWORK_INFO_PATH = "CustomerData.GeneralInfo.NetworkInfo";
    private static final String OMC_INFO_FILE_NAME = "/omc.info";
    private static final String OMC_INFO_VERSION = "version";
    private static final String PERSIST_OMCNW_PATH = "persist.sys.omcnw_path";
    private static final String PERSIST_OMCNW_PATH2 = "persist.sys.omcnw_path2";
    private static final String PERSIST_OMC_PATH = "persist.sys.omc_path";
    private static final String SALES_CODE_PATH = "CustomerData.GeneralInfo.SalesCode";
    private static final String SW_CONFIG_CSCNAME = "CSCName";
    private static final String SW_CONFIG_CSCVERSION = "CSCVersion";
    private static boolean[] sCscChangeChecked = {false, false};

    private static FileInputStream getCscFile(int simslot) {
        String omcNwPath;
        String customerPath;
        String omcPath = SemSystemProperties.get(PERSIST_OMC_PATH);
        if (OmcCode.getOmcVersion() < 5.0d || !"dsds".equals(SemSystemProperties.get("persist.radio.multisim.config"))) {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        } else if (simslot == 1) {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH2, omcPath);
        } else {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        }
        if (!TextUtils.isEmpty(omcNwPath)) {
            customerPath = omcNwPath + CUSTOMER_CSC_FILE_NAME;
        } else {
            customerPath = "/system/csc/customer.xml";
        }
        try {
            return new FileInputStream(new File(customerPath));
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, e.getClass().getSimpleName() + "!! " + e.getMessage());
            return null;
        }
    }

    private static XmlPullParser getCscCustomerParser(FileInputStream stream) {
        if (stream == null) {
            Log.d(LOG_TAG, "no csc file");
            return null;
        }
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(stream, (String) null);
            return xpp;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            closeFileInputStream(stream);
            return null;
        }
    }

    private static void closeFileInputStream(FileInputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static String getCscEdition(int simslot) {
        return getFieldFromCsc(simslot, CSC_EDITION_PATH);
    }

    static String getCscVersion(String oldVersion) {
        String swConfigPath;
        if (OmcCode.isOmcModel()) {
            swConfigPath = SemSystemProperties.get("persist.sys.omc_root", "/odm/omc") + "/SW_Configuration.xml";
        } else {
            swConfigPath = CSC_SW_CONFIG_FILE_PATH;
        }
        File swConfigFile = new File(swConfigPath);
        if (!swConfigFile.exists() || !swConfigFile.canRead()) {
            Log.e(LOG_TAG, "Can't read CSC Version");
            return oldVersion;
        }
        FileInputStream swConfigStream = null;
        String readName = null;
        String readVersion = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            FileInputStream swConfigStream2 = new FileInputStream(swConfigFile);
            xpp.setInput(swConfigStream2, (String) null);
            String name = null;
            while (true) {
                int eventType = xpp.getEventType();
                int event = eventType;
                if (eventType != 1) {
                    if (event == 2) {
                        name = xpp.getName();
                    } else if (event == 4) {
                        String text = xpp.getText();
                        if (!TextUtils.isEmpty(text)) {
                            if (TextUtils.isEmpty(readName) && SW_CONFIG_CSCNAME.equals(name)) {
                                readName = text.trim();
                            } else if (TextUtils.isEmpty(readVersion) && SW_CONFIG_CSCVERSION.equals(name)) {
                                readVersion = text.trim();
                            }
                        }
                        name = "";
                    }
                    if (!TextUtils.isEmpty(readName) && !TextUtils.isEmpty(readVersion)) {
                        Log.d(LOG_TAG, "Found Name and Version");
                        break;
                    }
                    xpp.next();
                }
            }
            try {
                swConfigStream2.close();
            } catch (IOException e) {
            }
        } catch (IOException | XmlPullParserException e2) {
            e2.printStackTrace();
            if (swConfigStream != null) {
                swConfigStream.close();
            }
        } catch (Throwable th) {
            if (swConfigStream != null) {
                try {
                    swConfigStream.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (TextUtils.isEmpty(readName) || TextUtils.isEmpty(readVersion)) {
            return oldVersion;
        }
        return readName + readVersion;
    }

    static String getOmcInfoVersion(String oldVersion, int simslot) {
        String omcNwPath;
        if (!OmcCode.isOmcModel()) {
            return oldVersion;
        }
        String omcPath = SemSystemProperties.get(PERSIST_OMC_PATH);
        if (OmcCode.getOmcVersion() < 5.0d || !"dsds".equals(SemSystemProperties.get("persist.radio.multisim.config"))) {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        } else if (simslot == 1) {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH2, omcPath);
        } else {
            omcNwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        }
        File omcinfoFile = new File(omcNwPath + OMC_INFO_FILE_NAME);
        if (!omcinfoFile.exists() || !omcinfoFile.canRead()) {
            Log.e(LOG_TAG, "Can't read OMC Version");
            return oldVersion;
        }
        FileInputStream omcInfoStream = null;
        String readVersion = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            FileInputStream omcInfoStream2 = new FileInputStream(omcinfoFile);
            xpp.setInput(omcInfoStream2, (String) null);
            String name = null;
            while (true) {
                int eventType = xpp.getEventType();
                int event = eventType;
                if (eventType != 1) {
                    if (event == 2) {
                        name = xpp.getName();
                    } else if (event == 4) {
                        String text = xpp.getText();
                        if (!TextUtils.isEmpty(text) && TextUtils.isEmpty(readVersion) && "version".equals(name)) {
                            readVersion = text.trim();
                        }
                        name = "";
                    }
                    if (!TextUtils.isEmpty(readVersion)) {
                        Log.d(LOG_TAG, "Found OMC Version");
                        break;
                    }
                    xpp.next();
                }
            }
            try {
                omcInfoStream2.close();
            } catch (IOException e) {
            }
        } catch (IOException | XmlPullParserException e2) {
            e2.printStackTrace();
            if (omcInfoStream != null) {
                omcInfoStream.close();
            }
        } catch (Throwable th) {
            if (omcInfoStream != null) {
                try {
                    omcInfoStream.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        if (TextUtils.isEmpty(readVersion)) {
            return oldVersion;
        }
        return readVersion;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:33:0x010c, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isCscChanged(android.content.Context r14, int r15) {
        /*
            java.lang.Class<com.sec.internal.ims.util.CscParser> r0 = com.sec.internal.ims.util.CscParser.class
            monitor-enter(r0)
            r1 = 0
            if (r14 == 0) goto L_0x010b
            if (r15 < 0) goto L_0x010b
            boolean[] r2 = sCscChangeChecked     // Catch:{ all -> 0x0108 }
            int r2 = r2.length     // Catch:{ all -> 0x0108 }
            if (r15 < r2) goto L_0x000f
            goto L_0x010b
        L_0x000f:
            boolean[] r2 = sCscChangeChecked     // Catch:{ all -> 0x0108 }
            boolean r2 = r2[r15]     // Catch:{ all -> 0x0108 }
            if (r2 == 0) goto L_0x0017
            monitor-exit(r0)
            return r1
        L_0x0017:
            java.lang.String r2 = "CSC_INFO_PREF"
            android.content.SharedPreferences r2 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r15, r14, r2, r1, r1)     // Catch:{ all -> 0x0108 }
            java.lang.String r3 = "csc.key.edition"
            java.lang.String r4 = "unknown"
            java.lang.String r3 = r2.getString(r3, r4)     // Catch:{ all -> 0x0108 }
            java.lang.String r4 = "csc.key.version"
            java.lang.String r5 = "unknown"
            java.lang.String r4 = r2.getString(r4, r5)     // Catch:{ all -> 0x0108 }
            java.lang.String r5 = "csc.key.salescode"
            java.lang.String r6 = "unknown"
            java.lang.String r5 = r2.getString(r5, r6)     // Catch:{ all -> 0x0108 }
            java.lang.String r6 = "omc.key.version"
            java.lang.String r7 = "unknown"
            java.lang.String r6 = r2.getString(r6, r7)     // Catch:{ all -> 0x0108 }
            java.lang.String r7 = getCscEdition(r15)     // Catch:{ all -> 0x0108 }
            java.lang.String r8 = getCscVersion(r4)     // Catch:{ all -> 0x0108 }
            java.lang.String r9 = getCscSalesCode(r15)     // Catch:{ all -> 0x0108 }
            java.lang.String r10 = getOmcInfoVersion(r6, r15)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "CscParser"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x0108 }
            r12.<init>()     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = "old edition : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r3)     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = " new edition : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r7)     // Catch:{ all -> 0x0108 }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x0108 }
            android.util.Log.d(r11, r12)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "CscParser"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x0108 }
            r12.<init>()     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = "old csc version : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r4)     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = " new csc version : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r8)     // Catch:{ all -> 0x0108 }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x0108 }
            android.util.Log.d(r11, r12)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "CscParser"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x0108 }
            r12.<init>()     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = "old salescode : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r5)     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = " new salescode : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r9)     // Catch:{ all -> 0x0108 }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x0108 }
            android.util.Log.d(r11, r12)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "CscParser"
            java.lang.StringBuilder r12 = new java.lang.StringBuilder     // Catch:{ all -> 0x0108 }
            r12.<init>()     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = "old omc version : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r6)     // Catch:{ all -> 0x0108 }
            java.lang.String r13 = " new omc version : "
            r12.append(r13)     // Catch:{ all -> 0x0108 }
            r12.append(r10)     // Catch:{ all -> 0x0108 }
            java.lang.String r12 = r12.toString()     // Catch:{ all -> 0x0108 }
            android.util.Log.d(r11, r12)     // Catch:{ all -> 0x0108 }
            boolean[] r11 = sCscChangeChecked     // Catch:{ all -> 0x0108 }
            r12 = 1
            r11[r15] = r12     // Catch:{ all -> 0x0108 }
            boolean r11 = android.text.TextUtils.equals(r3, r7)     // Catch:{ all -> 0x0108 }
            if (r11 == 0) goto L_0x00e8
            boolean r11 = android.text.TextUtils.equals(r4, r8)     // Catch:{ all -> 0x0108 }
            if (r11 == 0) goto L_0x00e8
            boolean r11 = android.text.TextUtils.equals(r5, r9)     // Catch:{ all -> 0x0108 }
            if (r11 == 0) goto L_0x00e8
            boolean r11 = android.text.TextUtils.equals(r6, r10)     // Catch:{ all -> 0x0108 }
            if (r11 == 0) goto L_0x00e8
            monitor-exit(r0)
            return r1
        L_0x00e8:
            android.content.SharedPreferences$Editor r1 = r2.edit()     // Catch:{ all -> 0x0108 }
            r1.clear()     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "csc.key.edition"
            r1.putString(r11, r7)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "csc.key.version"
            r1.putString(r11, r8)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "csc.key.salescode"
            r1.putString(r11, r9)     // Catch:{ all -> 0x0108 }
            java.lang.String r11 = "omc.key.version"
            r1.putString(r11, r10)     // Catch:{ all -> 0x0108 }
            r1.apply()     // Catch:{ all -> 0x0108 }
            monitor-exit(r0)
            return r12
        L_0x0108:
            r14 = move-exception
            monitor-exit(r0)
            throw r14
        L_0x010b:
            monitor-exit(r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.isCscChanged(android.content.Context, int):boolean");
    }

    private static String getFieldFromCsc(int simSlot, String fieldName) {
        FileInputStream stream = getCscFile(simSlot);
        XmlPullParser xpp = getCscCustomerParser(stream);
        if (xpp == null) {
            Log.e(LOG_TAG, "XmlPullParser is null");
            closeFileInputStream(stream);
            return null;
        } else if (!XmlUtils.search(xpp, fieldName)) {
            Log.e(LOG_TAG, "can not find " + fieldName);
            closeFileInputStream(stream);
            return null;
        } else {
            while (xpp.next() != 4) {
                try {
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    closeFileInputStream(stream);
                }
            }
            return xpp.getText();
        }
    }

    static String getCscSalesCode(int simslot) {
        return getFieldFromCsc(simslot, SALES_CODE_PATH);
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    public static java.util.List<java.lang.String> getNetworkNames(java.lang.String r22, java.lang.String r23, java.lang.String r24, java.lang.String r25, java.lang.String r26, int r27, boolean r28) {
        /*
            r1 = r23
            java.lang.String r2 = ""
            java.io.FileInputStream r3 = getCscFile(r27)
            org.xmlpull.v1.XmlPullParser r4 = getCscCustomerParser(r3)
            r0 = 0
            java.lang.String r5 = "CscParser"
            if (r4 != 0) goto L_0x001a
            java.lang.String r2 = "XmlPullParser is null"
            android.util.Log.e(r5, r2)
            closeFileInputStream(r3)
            return r0
        L_0x001a:
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            java.util.ArrayList r7 = new java.util.ArrayList
            r7.<init>()
            java.lang.String r8 = "CustomerData.GeneralInfo.NetworkInfo"
            boolean r8 = com.sec.internal.helper.XmlUtils.search(r4, r8)
            if (r8 != 0) goto L_0x0035
            java.lang.String r2 = "can not find CustomerData.GeneralInfo.NetworkInfo"
            android.util.Log.e(r5, r2)
            closeFileInputStream(r3)
            return r0
        L_0x0035:
            if (r1 == 0) goto L_0x004c
            int r0 = r23.length()
            int r5 = r22.length()
            if (r0 > r5) goto L_0x0042
            goto L_0x004c
        L_0x0042:
            int r0 = r22.length()
            java.lang.String r0 = r1.substring(r0)
            r5 = r0
            goto L_0x004f
        L_0x004c:
            java.lang.String r0 = ""
            r5 = r0
        L_0x004f:
            r0 = 0
            r8 = 0
            r9 = 0
            r10 = 1
            r11 = r2
            r12 = 0
            r13 = r2
            r14 = r2
            r15 = r2
            r16 = 0
            r17 = r13
            r18 = r14
            r19 = r15
            r20 = r16
            r13 = r8
            r14 = r9
            r15 = r11
            r16 = r12
            r8 = r24
            r9 = r25
            r11 = r26
            r12 = r0
        L_0x006e:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            r24 = r0
            if (r0 == r10) goto L_0x039e
            r0 = 2
            r10 = r24
            if (r10 != r0) goto L_0x0158
            java.lang.String r0 = r4.getName()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r12 = r0
            java.lang.String r0 = "MCCMNC"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r1 = 4
            if (r0 == 0) goto L_0x0099
        L_0x0089:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x0090
            goto L_0x0089
        L_0x0090:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r13 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x0099:
            java.lang.String r0 = "SPCode"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x00b5
        L_0x00a1:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x00a8
            goto L_0x00a1
        L_0x00a8:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            java.lang.String r0 = r0.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r15 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x00b5:
            java.lang.String r0 = "CodeType"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x00d6
        L_0x00bd:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x00c4
            goto L_0x00bd
        L_0x00c4:
            java.lang.String r0 = "HEX"
            java.lang.String r1 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x0145
            r16 = 1
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x00d6:
            java.lang.String r0 = "SubsetCode"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x00f4
        L_0x00de:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x00e5
            goto L_0x00de
        L_0x00e5:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            java.lang.String r0 = r0.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r17 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x00f4:
            java.lang.String r0 = "Gid2"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x0112
        L_0x00fc:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x0103
            goto L_0x00fc
        L_0x0103:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            java.lang.String r0 = r0.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r18 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x0112:
            java.lang.String r0 = "Spname"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x012c
        L_0x011a:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x0121
            goto L_0x011a
        L_0x0121:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r19 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x012c:
            java.lang.String r0 = "NetworkName"
            boolean r0 = r0.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == 0) goto L_0x0145
        L_0x0134:
            int r0 = r4.next()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r0 == r1) goto L_0x013b
            goto L_0x0134
        L_0x013b:
            java.lang.String r0 = r4.getText()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r14 = r0
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x0145:
            r1 = r23
            r10 = 1
            goto L_0x006e
        L_0x014a:
            r0 = move-exception
            r13 = r27
        L_0x014d:
            r21 = r5
            goto L_0x03c6
        L_0x0151:
            r0 = move-exception
            r13 = r27
        L_0x0154:
            r21 = r5
            goto L_0x03b2
        L_0x0158:
            r0 = 3
            if (r10 != r0) goto L_0x0389
            java.lang.String r0 = "NetworkInfo"
            java.lang.String r1 = r4.getName()     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r0 == 0) goto L_0x0370
            if (r16 != 0) goto L_0x018d
            int r0 = java.lang.Integer.parseInt(r17)     // Catch:{ NumberFormatException -> 0x0176, IOException | XmlPullParserException -> 0x0151 }
            java.lang.String r1 = java.lang.Integer.toHexString(r0)     // Catch:{ NumberFormatException -> 0x0176, IOException | XmlPullParserException -> 0x0151 }
            java.lang.String r1 = r1.toUpperCase()     // Catch:{ NumberFormatException -> 0x0176, IOException | XmlPullParserException -> 0x0151 }
            goto L_0x0178
        L_0x0176:
            r0 = move-exception
            r1 = r2
        L_0x0178:
            int r0 = java.lang.Integer.parseInt(r18)     // Catch:{ NumberFormatException -> 0x0187, IOException | XmlPullParserException -> 0x0151 }
            java.lang.String r17 = java.lang.Integer.toHexString(r0)     // Catch:{ NumberFormatException -> 0x0187, IOException | XmlPullParserException -> 0x0151 }
            java.lang.String r17 = r17.toUpperCase()     // Catch:{ NumberFormatException -> 0x0187, IOException | XmlPullParserException -> 0x0151 }
            r0 = r17
            goto L_0x0191
        L_0x0187:
            r0 = move-exception
            r17 = r2
            r0 = r17
            goto L_0x0191
        L_0x018d:
            r1 = r17
            r0 = r18
        L_0x0191:
            boolean r17 = android.text.TextUtils.isEmpty(r14)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r17 != 0) goto L_0x0357
            boolean r17 = android.text.TextUtils.isEmpty(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r17 == 0) goto L_0x01a7
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0361
        L_0x01a7:
            r24 = r10
            java.lang.String r10 = "00101"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 != 0) goto L_0x034e
            java.lang.String r10 = "001001"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 != 0) goto L_0x034e
            java.lang.String r10 = "001010"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 != 0) goto L_0x0345
            java.lang.String r10 = "00101f"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 != 0) goto L_0x0345
            java.lang.String r10 = "99999"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 != 0) goto L_0x033c
            java.lang.String r10 = "45001"
            boolean r10 = r10.equals(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r10 == 0) goto L_0x01e3
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0361
        L_0x01e3:
            r10 = r22
            boolean r17 = r10.equalsIgnoreCase(r13)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r17 == 0) goto L_0x031f
            if (r28 == 0) goto L_0x01fa
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0327
        L_0x01fa:
            boolean r17 = android.text.TextUtils.isEmpty(r15)     // Catch:{ IOException | XmlPullParserException -> 0x03ad, all -> 0x03a7 }
            if (r17 != 0) goto L_0x024a
            boolean r17 = android.text.TextUtils.isEmpty(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            if (r17 != 0) goto L_0x024a
            if (r20 != 0) goto L_0x0225
            com.sec.internal.constants.Mno$Country r17 = com.sec.internal.constants.Mno.Country.CANADA     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            java.lang.String r10 = r17.getCountryIso()     // Catch:{ IOException | XmlPullParserException -> 0x0151, all -> 0x014a }
            r25 = r12
            java.lang.String r12 = "CustomerData.GeneralInfo.CountryISO"
            r26 = r13
            r13 = r27
            java.lang.String r12 = getFieldFromCsc(r13, r12)     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            boolean r10 = r10.equalsIgnoreCase(r12)     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            java.lang.Boolean r10 = java.lang.Boolean.valueOf(r10)     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            r20 = r10
            goto L_0x022b
        L_0x0225:
            r25 = r12
            r26 = r13
            r13 = r27
        L_0x022b:
            boolean r10 = r20.booleanValue()     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            if (r10 == 0) goto L_0x0233
            r10 = 1
            goto L_0x0234
        L_0x0233:
            r10 = 0
        L_0x0234:
            boolean r10 = r5.startsWith(r15, r10)     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            if (r10 == 0) goto L_0x0250
            r6.clear()     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0247, all -> 0x0244 }
            r21 = r5
            goto L_0x03b5
        L_0x0244:
            r0 = move-exception
            goto L_0x014d
        L_0x0247:
            r0 = move-exception
            goto L_0x0154
        L_0x024a:
            r25 = r12
            r26 = r13
            r13 = r27
        L_0x0250:
            boolean r10 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            java.lang.String r12 = "^0+(?!$)"
            if (r10 != 0) goto L_0x028f
            boolean r10 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            if (r10 != 0) goto L_0x028f
            java.lang.String r10 = r1.replaceFirst(r12, r2)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            r1 = r10
            java.lang.String r10 = r8.replaceFirst(r12, r2)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            r8 = r10
            boolean r10 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            if (r10 != 0) goto L_0x028c
            boolean r10 = android.text.TextUtils.isEmpty(r8)     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            if (r10 != 0) goto L_0x028c
            java.lang.String r10 = r8.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x031c, all -> 0x0319 }
            r21 = r5
            java.lang.String r5 = r1.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            boolean r5 = r10.startsWith(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x0291
            r6.clear()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            goto L_0x03b5
        L_0x028c:
            r21 = r5
            goto L_0x0291
        L_0x028f:
            r21 = r5
        L_0x0291:
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02c9
            boolean r5 = android.text.TextUtils.isEmpty(r9)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02c9
            java.lang.String r5 = r0.replaceFirst(r12, r2)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r0 = r5
            java.lang.String r5 = r9.replaceFirst(r12, r2)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r9 = r5
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02c9
            boolean r5 = android.text.TextUtils.isEmpty(r9)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02c9
            java.lang.String r5 = r9.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            java.lang.String r10 = r0.toUpperCase()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            boolean r5 = r5.startsWith(r10)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x02c9
            r6.clear()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            goto L_0x03b5
        L_0x02c9:
            boolean r5 = android.text.TextUtils.isEmpty(r19)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02fa
            boolean r5 = android.text.TextUtils.isEmpty(r11)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 != 0) goto L_0x02fa
            java.lang.String r5 = r19.trim()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            java.lang.String r10 = r11.trim()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r11 = r10
            boolean r10 = android.text.TextUtils.isEmpty(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r10 != 0) goto L_0x02f8
            boolean r10 = android.text.TextUtils.isEmpty(r11)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r10 != 0) goto L_0x02f8
            boolean r10 = r11.equalsIgnoreCase(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r10 == 0) goto L_0x02f8
            r6.clear()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            goto L_0x03b5
        L_0x02f8:
            r19 = r5
        L_0x02fa:
            boolean r5 = android.text.TextUtils.isEmpty(r15)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x0327
            boolean r5 = android.text.TextUtils.isEmpty(r1)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x0327
            boolean r5 = android.text.TextUtils.isEmpty(r0)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x0327
            r6.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            boolean r5 = android.text.TextUtils.isEmpty(r19)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r5 == 0) goto L_0x0327
            r7.add(r14)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            goto L_0x0327
        L_0x0319:
            r0 = move-exception
            goto L_0x03aa
        L_0x031c:
            r0 = move-exception
            goto L_0x03b0
        L_0x031f:
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
        L_0x0327:
            r19 = r2
            r18 = r2
            r17 = r2
            r15 = r2
            r0 = r2
            r14 = r2
            r16 = 0
            r1 = r23
            r12 = r25
            r13 = r0
            r5 = r21
            r10 = 1
            goto L_0x006e
        L_0x033c:
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0361
        L_0x0345:
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0361
        L_0x034e:
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x0361
        L_0x0357:
            r21 = r5
            r24 = r10
            r25 = r12
            r26 = r13
            r13 = r27
        L_0x0361:
            r12 = r25
            r13 = r26
            r18 = r0
            r17 = r1
            r5 = r21
            r10 = 1
            r1 = r23
            goto L_0x006e
        L_0x0370:
            r21 = r5
            r24 = r10
            r25 = r12
            r26 = r13
            r13 = r27
            java.lang.String r0 = "GeneralInfo"
            java.lang.String r1 = r4.getName()     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ IOException | XmlPullParserException -> 0x0387 }
            if (r0 == 0) goto L_0x0393
            goto L_0x03b5
        L_0x0387:
            r0 = move-exception
            goto L_0x03b2
        L_0x0389:
            r21 = r5
            r24 = r10
            r25 = r12
            r26 = r13
            r13 = r27
        L_0x0393:
            r1 = r23
            r12 = r25
            r13 = r26
            r5 = r21
            r10 = 1
            goto L_0x006e
        L_0x039e:
            r21 = r5
            r25 = r12
            r26 = r13
            r13 = r27
            goto L_0x03b5
        L_0x03a7:
            r0 = move-exception
            r13 = r27
        L_0x03aa:
            r21 = r5
            goto L_0x03c6
        L_0x03ad:
            r0 = move-exception
            r13 = r27
        L_0x03b0:
            r21 = r5
        L_0x03b2:
            r0.printStackTrace()     // Catch:{ all -> 0x03c5 }
        L_0x03b5:
            closeFileInputStream(r3)
            if (r28 == 0) goto L_0x03bc
            return r6
        L_0x03bc:
            int r0 = r6.size()
            r1 = 1
            if (r0 <= r1) goto L_0x03c4
            return r7
        L_0x03c4:
            return r6
        L_0x03c5:
            r0 = move-exception
        L_0x03c6:
            closeFileInputStream(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getNetworkNames(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, boolean):java.util.List");
    }

    static synchronized String getCscImsSettingVersion(int simslot) {
        synchronized (CscParser.class) {
            FileInputStream stream = getCscFile(simslot);
            XmlPullParser xpp = getCscCustomerParser(stream);
            String versionInCsc = null;
            if (xpp == null) {
                Log.e(LOG_TAG, "XmlPullParser is null");
                closeFileInputStream(stream);
                return null;
            } else if (!XmlUtils.search(xpp, IMSSETTING_VERSION_PATH)) {
                Log.e(LOG_TAG, "can not find imsSettings");
                closeFileInputStream(stream);
                return null;
            } else {
                while (xpp.next() != 4) {
                    try {
                    } catch (IOException | XmlPullParserException e) {
                        try {
                            Log.e(LOG_TAG, "getCscImsSettingVersion : " + e.getMessage());
                        } catch (Throwable th) {
                            closeFileInputStream(stream);
                            throw th;
                        }
                    }
                }
                versionInCsc = xpp.getText();
                Log.e(LOG_TAG, "getCscImsSettingVersion : " + versionInCsc);
                closeFileInputStream(stream);
                return versionInCsc;
            }
        }
    }

    public static synchronized ContentValues getCscImsSetting(String operator, int simslot) {
        ContentValues cscImsSetting;
        synchronized (CscParser.class) {
            cscImsSetting = getCscImsSetting(getNetworkNames(operator, "", "", "", "", simslot, true), simslot);
        }
        return cscImsSetting;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        closeFileInputStream(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00b0, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized android.content.ContentValues getCscImsSetting(java.util.List<java.lang.String> r9, int r10) {
        /*
            java.lang.Class<com.sec.internal.ims.util.CscParser> r0 = com.sec.internal.ims.util.CscParser.class
            monitor-enter(r0)
            r1 = 0
            if (r9 == 0) goto L_0x00af
            int r2 = r9.size()     // Catch:{ all -> 0x00ac }
            if (r2 != 0) goto L_0x000e
            goto L_0x00af
        L_0x000e:
            java.io.FileInputStream r2 = getCscFile(r10)     // Catch:{ all -> 0x00ac }
            org.xmlpull.v1.XmlPullParser r3 = getCscCustomerParser(r2)     // Catch:{ all -> 0x00ac }
            if (r3 != 0) goto L_0x0024
            java.lang.String r4 = "CscParser"
            java.lang.String r5 = "XmlPullParser is null"
            android.util.Log.e(r4, r5)     // Catch:{ all -> 0x00ac }
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
            monitor-exit(r0)
            return r1
        L_0x0024:
            java.lang.String r4 = "CustomerData.Settings.IMSSetting.NbSetting"
            boolean r4 = com.sec.internal.helper.XmlUtils.search(r3, r4)     // Catch:{ all -> 0x00ac }
            if (r4 != 0) goto L_0x0038
            java.lang.String r4 = "CscParser"
            java.lang.String r5 = "can not find CustomerData.Settings.IMSSetting.NbSetting"
            android.util.Log.e(r4, r5)     // Catch:{ all -> 0x00ac }
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
            monitor-exit(r0)
            return r1
        L_0x0038:
            r4 = 0
        L_0x0039:
            int r5 = r3.getEventType()     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            r6 = r5
            r7 = 1
            if (r5 == r7) goto L_0x0081
            r5 = 2
            if (r6 != r5) goto L_0x007d
            java.lang.String r5 = "Setting"
            java.lang.String r7 = r3.getName()     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            boolean r5 = r5.equalsIgnoreCase(r7)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            if (r5 == 0) goto L_0x007d
            android.content.ContentValues r5 = getSetting(r3)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            r4 = r5
            java.lang.String r5 = "NetworkName"
            java.lang.String r5 = r4.getAsString(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            boolean r5 = r9.contains(r5)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            if (r5 == 0) goto L_0x007d
            java.lang.String r5 = "CscParser"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            r7.<init>()     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            java.lang.String r8 = "csc ims setting: "
            r7.append(r8)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            r7.append(r4)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            java.lang.String r7 = r7.toString()     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            android.util.Log.d(r5, r7)     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
            monitor-exit(r0)
            return r4
        L_0x007d:
            r3.next()     // Catch:{ IOException | XmlPullParserException -> 0x0087 }
            goto L_0x0039
        L_0x0081:
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
            goto L_0x00a6
        L_0x0085:
            r1 = move-exception
            goto L_0x00a8
        L_0x0087:
            r5 = move-exception
            java.lang.String r6 = "CscParser"
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ all -> 0x0085 }
            r7.<init>()     // Catch:{ all -> 0x0085 }
            java.lang.String r8 = "getCscImsSetting: "
            r7.append(r8)     // Catch:{ all -> 0x0085 }
            java.lang.String r8 = r5.getMessage()     // Catch:{ all -> 0x0085 }
            r7.append(r8)     // Catch:{ all -> 0x0085 }
            java.lang.String r7 = r7.toString()     // Catch:{ all -> 0x0085 }
            android.util.Log.e(r6, r7)     // Catch:{ all -> 0x0085 }
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
        L_0x00a6:
            monitor-exit(r0)
            return r1
        L_0x00a8:
            closeFileInputStream(r2)     // Catch:{ all -> 0x00ac }
            throw r1     // Catch:{ all -> 0x00ac }
        L_0x00ac:
            r9 = move-exception
            monitor-exit(r0)
            throw r9
        L_0x00af:
            monitor-exit(r0)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.CscParser.getCscImsSetting(java.util.List, int):android.content.ContentValues");
    }

    private static ContentValues getSetting(XmlPullParser xpp) {
        ContentValues setting = new ContentValues();
        String name = null;
        while (true) {
            try {
                int eventType = xpp.getEventType();
                int event = eventType;
                if (eventType == 1) {
                    break;
                }
                if (event == 2) {
                    name = xpp.getName();
                } else if (event == 3) {
                    if ("Setting".equalsIgnoreCase(xpp.getName())) {
                        break;
                    }
                } else if (event == 4) {
                    String text = xpp.getText();
                    if (!TextUtils.isEmpty(text) && text.trim().length() > 0) {
                        setting.put(name, text);
                    }
                }
                xpp.next();
            } catch (IOException | XmlPullParserException e) {
                Log.e(LOG_TAG, "getSetting: " + e.getMessage());
            }
        }
        return setting;
    }

    public static boolean isPilotSetting(int simslot) {
        String version = getCscImsSettingVersion(simslot);
        if (TextUtils.isEmpty(version)) {
            return false;
        }
        if ("1.0".equals(version) || IMSSETTING_STR_VERSION_PILOT.equalsIgnoreCase(version)) {
            return true;
        }
        return false;
    }
}
