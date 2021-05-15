package com.sec.internal.ims.core.sim;

import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/* compiled from: MnoMap */
class CscNetParser {
    private static final String CUSTOMER_CSC_FILE_NAME = "/customer.xml";
    private static final String CUSTOMER_CSC_FILE_PATH = "/system/csc";
    private static final String LOG_TAG = "CscNetParser";
    private static final String NETWORK_INFO_PATH = "CustomerData.GeneralInfo.NetworkInfo";
    private static final String PERSIST_OMCNW_PATH = "persist.sys.omcnw_path";
    private static final String PERSIST_OMCNW_PATH2 = "persist.sys.omcnw_path2";
    private static final String PERSIST_OMC_PATH = "persist.sys.omc_path";
    private FileInputStream mFileInputStream;
    public ArrayList<CscNetwork> mNetworkInfoList = new ArrayList<>();
    private int mPhoneId = 0;

    public CscNetParser(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public ArrayList<CscNetwork> getCscNetwokrInfo() {
        parseNetworkInfo();
        return this.mNetworkInfoList;
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    private void parseNetworkInfo() {
        /*
            r24 = this;
            r1 = r24
            java.lang.String r2 = ""
            org.xmlpull.v1.XmlPullParser r3 = r24.getCscCustomerParser()
            java.lang.String r4 = "CscNetParser"
            if (r3 != 0) goto L_0x0015
            java.lang.String r0 = "XmlPullParser is null"
            android.util.Log.e(r4, r0)
            r24.closeFileInputStream()
            return
        L_0x0015:
            java.lang.String r0 = "CustomerData.GeneralInfo.NetworkInfo"
            boolean r0 = com.sec.internal.helper.XmlUtils.search(r3, r0)
            if (r0 != 0) goto L_0x0026
            java.lang.String r0 = "can not find CustomerData.GeneralInfo.NetworkInfo"
            android.util.Log.e(r4, r0)
            r24.closeFileInputStream()
            return
        L_0x0026:
            r0 = r2
            r5 = r2
            r6 = r2
            r7 = r2
            r8 = 0
            r9 = r2
            r10 = r2
            r11 = r2
            r12 = r5
            r13 = r6
            r14 = r7
            r15 = r8
            r5 = r0
            r23 = r11
            r11 = r10
            r10 = r23
        L_0x0038:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r8 = r0
            r6 = 1
            if (r0 == r6) goto L_0x0281
            r0 = 2
            if (r8 != r0) goto L_0x00d9
            java.lang.String r0 = r3.getName()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r5 = r0
            java.lang.String r0 = "NetworkName"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6 = 4
            if (r0 == 0) goto L_0x005e
        L_0x0051:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x0058
            goto L_0x0051
        L_0x0058:
            java.lang.String r0 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r13 = r0
            goto L_0x0038
        L_0x005e:
            java.lang.String r0 = "MCCMNC"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x0073
        L_0x0066:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x006d
            goto L_0x0066
        L_0x006d:
            java.lang.String r0 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r12 = r0
            goto L_0x0038
        L_0x0073:
            java.lang.String r0 = "SPCode"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x008c
        L_0x007b:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x0082
            goto L_0x007b
        L_0x0082:
            java.lang.String r0 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r0 = r0.toUpperCase()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r14 = r0
            goto L_0x0038
        L_0x008c:
            java.lang.String r0 = "CodeType"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x00a9
        L_0x0094:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x009b
            goto L_0x0094
        L_0x009b:
            java.lang.String r0 = "HEX"
            java.lang.String r6 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            boolean r0 = r0.equalsIgnoreCase(r6)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x0038
            r15 = 1
            goto L_0x0038
        L_0x00a9:
            java.lang.String r0 = "SubsetCode"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x00c3
        L_0x00b1:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x00b8
            goto L_0x00b1
        L_0x00b8:
            java.lang.String r0 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r0 = r0.toUpperCase()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r9 = r0
            goto L_0x0038
        L_0x00c3:
            java.lang.String r0 = "Spname"
            boolean r0 = r0.equalsIgnoreCase(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x0038
        L_0x00cb:
            int r0 = r3.next()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == r6) goto L_0x00d2
            goto L_0x00cb
        L_0x00d2:
            java.lang.String r0 = r3.getText()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r10 = r0
            goto L_0x0038
        L_0x00d9:
            r0 = 3
            if (r8 != r0) goto L_0x026d
            java.lang.String r0 = "NetworkInfo"
            java.lang.String r6 = r3.getName()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            boolean r0 = r0.equalsIgnoreCase(r6)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x0256
            if (r15 != 0) goto L_0x0101
            int r0 = java.lang.Integer.parseInt(r9)     // Catch:{ NumberFormatException -> 0x00f8 }
            java.lang.String r6 = java.lang.Integer.toHexString(r0)     // Catch:{ NumberFormatException -> 0x00f8 }
            java.lang.String r6 = r6.toUpperCase()     // Catch:{ NumberFormatException -> 0x00f8 }
            r0 = r6
            goto L_0x0102
        L_0x00f8:
            r0 = move-exception
            java.lang.String r6 = "invalid NetworkInfo have CodeType, but no gid1"
            android.util.Log.i(r4, r6)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6 = r2
            r0 = r6
            goto L_0x0102
        L_0x0101:
            r0 = r9
        L_0x0102:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.<init>()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = "mccmnc: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = ", networkName: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r13)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = ", subset: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r14)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = ", gid1: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r0)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = ", gid2: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r11)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r7 = ", spname: "
            r6.append(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.append(r10)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.String r6 = r6.toString()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            android.util.Log.i(r4, r6)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            boolean r6 = r13.isEmpty()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x0241
            boolean r6 = r12.isEmpty()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 == 0) goto L_0x0154
            r16 = r4
            r18 = r5
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x0154:
            java.lang.String r6 = "00101"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x0236
            java.lang.String r6 = "001001"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x0236
            java.lang.String r6 = "001010"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x022b
            java.lang.String r6 = "00101f"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x022b
            java.lang.String r6 = "99999"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 != 0) goto L_0x0220
            java.lang.String r6 = "45001"
            boolean r6 = r6.equals(r12)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 == 0) goto L_0x018e
            r16 = r4
            r18 = r5
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x018e:
            java.lang.String r6 = "GCF"
            boolean r6 = r6.equalsIgnoreCase(r13)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r6 == 0) goto L_0x01a0
            r16 = r4
            r18 = r5
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x01a0:
            r6 = 0
            java.util.ArrayList<com.sec.internal.ims.core.sim.CscNetwork> r7 = r1.mNetworkInfoList     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            int r7 = r7.size()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r9 = r7
            r7 = 0
            r23 = r7
            r7 = r6
            r6 = r23
        L_0x01ae:
            if (r7 >= r9) goto L_0x01ca
            r16 = r4
            java.util.ArrayList<com.sec.internal.ims.core.sim.CscNetwork> r4 = r1.mNetworkInfoList     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.lang.Object r4 = r4.get(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            com.sec.internal.ims.core.sim.CscNetwork r4 = (com.sec.internal.ims.core.sim.CscNetwork) r4     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6 = r4
            java.lang.String r4 = r6.mNetworkName     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            boolean r4 = r13.equals(r4)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r4 == 0) goto L_0x01c5
            r4 = r6
            goto L_0x01cd
        L_0x01c5:
            int r7 = r7 + 1
            r4 = r16
            goto L_0x01ae
        L_0x01ca:
            r16 = r4
            r4 = r6
        L_0x01cd:
            if (r7 != r9) goto L_0x01f3
            com.sec.internal.ims.core.sim.CscNetwork r6 = new com.sec.internal.ims.core.sim.CscNetwork     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.<init>(r13)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r17 = r6
            r18 = r5
            r5 = r7
            r7 = r12
            r19 = r8
            r8 = r14
            r20 = r9
            r9 = r0
            r21 = r10
            r10 = r11
            r22 = r11
            r11 = r21
            r6.addIdentifier(r7, r8, r9, r10, r11)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.util.ArrayList<com.sec.internal.ims.core.sim.CscNetwork> r6 = r1.mNetworkInfoList     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r7 = r17
            r6.add(r7)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            goto L_0x0213
        L_0x01f3:
            r18 = r5
            r5 = r7
            r19 = r8
            r20 = r9
            r21 = r10
            r22 = r11
            r6 = r4
            r7 = r12
            r8 = r14
            r9 = r0
            r10 = r22
            r11 = r21
            r6.addIdentifier(r7, r8, r9, r10, r11)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.util.ArrayList<com.sec.internal.ims.core.sim.CscNetwork> r6 = r1.mNetworkInfoList     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.remove(r5)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            java.util.ArrayList<com.sec.internal.ims.core.sim.CscNetwork> r6 = r1.mNetworkInfoList     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            r6.add(r4)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
        L_0x0213:
            r10 = r2
            r11 = r2
            r9 = r2
            r14 = r2
            r12 = r2
            r13 = r2
            r15 = 0
            r4 = r16
            r5 = r18
            goto L_0x0038
        L_0x0220:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x022b:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x0236:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
            goto L_0x024b
        L_0x0241:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
        L_0x024b:
            r9 = r0
            r4 = r16
            r5 = r18
            r10 = r21
            r11 = r22
            goto L_0x0038
        L_0x0256:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
            java.lang.String r0 = "GeneralInfo"
            java.lang.String r4 = r3.getName()     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            boolean r0 = r0.equalsIgnoreCase(r4)     // Catch:{ XmlPullParserException -> 0x028f, IOException -> 0x028a }
            if (r0 == 0) goto L_0x0277
            goto L_0x0293
        L_0x026d:
            r16 = r4
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
        L_0x0277:
            r4 = r16
            r5 = r18
            r10 = r21
            r11 = r22
            goto L_0x0038
        L_0x0281:
            r18 = r5
            r19 = r8
            r21 = r10
            r22 = r11
            goto L_0x0293
        L_0x028a:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0298 }
            goto L_0x0293
        L_0x028f:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ all -> 0x0298 }
        L_0x0293:
            r24.closeFileInputStream()
            return
        L_0x0298:
            r0 = move-exception
            r24.closeFileInputStream()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.CscNetParser.parseNetworkInfo():void");
    }

    private XmlPullParser getCscCustomerParser() {
        String omcnwPath;
        String customerPath;
        IMSLog.i(LOG_TAG, this.mPhoneId, "getCscCustomerParser:");
        String omcPath = SemSystemProperties.get(PERSIST_OMC_PATH);
        double omcVer = OmcCode.getOmcVersion();
        Log.i(LOG_TAG, "OMC version : " + omcVer);
        if (omcVer < 5.0d) {
            omcnwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        } else if (this.mPhoneId == 1) {
            omcnwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH2, omcPath);
        } else {
            omcnwPath = SemSystemProperties.get(PERSIST_OMCNW_PATH, omcPath);
        }
        if (SimUtil.getSimMno(this.mPhoneId) == Mno.DEFAULT) {
            return null;
        }
        if (!TextUtils.isEmpty(omcnwPath)) {
            customerPath = omcnwPath + CUSTOMER_CSC_FILE_NAME;
        } else {
            customerPath = "/system/csc/customer.xml";
        }
        Log.i(LOG_TAG, "customerPath = " + customerPath);
        File customer = new File(customerPath);
        if (!customer.exists()) {
            Log.e(LOG_TAG, "customer.xml file does not exists");
            return null;
        }
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            FileInputStream fileInputStream = new FileInputStream(customer);
            this.mFileInputStream = fileInputStream;
            xpp.setInput(fileInputStream, (String) null);
            return xpp;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            closeFileInputStream();
            return null;
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
            closeFileInputStream();
            return null;
        }
    }

    private void closeFileInputStream() {
        FileInputStream fileInputStream = this.mFileInputStream;
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable th) {
                this.mFileInputStream = null;
                throw th;
            }
            this.mFileInputStream = null;
        }
    }
}
