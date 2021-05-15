package com.sun.mail.imap.protocol;

import java.io.PrintStream;
import java.util.Properties;

public class IMAPSaslAuthenticator implements SaslAuthenticator {
    /* access modifiers changed from: private */
    public boolean debug;
    private String host;
    private String name;
    /* access modifiers changed from: private */
    public PrintStream out;
    private IMAPProtocol pr;
    private Properties props;

    public IMAPSaslAuthenticator(IMAPProtocol pr2, String name2, Properties props2, boolean debug2, PrintStream out2, String host2) {
        this.pr = pr2;
        this.name = name2;
        this.props = props2;
        this.debug = debug2;
        this.out = out2;
        this.host = host2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:114:0x024a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x024b, code lost:
        r19 = r7;
        r21 = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0251, code lost:
        if (r1.debug != false) goto L_0x0253;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0253, code lost:
        r1.out.println("IMAP SASL DEBUG: AUTHENTICATE Exception: " + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x0267, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ff, code lost:
        return false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x023a A[Catch:{ Exception -> 0x024a }] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x027a A[Catch:{ Exception -> 0x024a }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean authenticate(java.lang.String[] r23, java.lang.String r24, java.lang.String r25, java.lang.String r26, java.lang.String r27) throws com.sun.mail.iap.ProtocolException {
        /*
            r22 = this;
            r1 = r22
            r8 = r23
            com.sun.mail.imap.protocol.IMAPProtocol r9 = r1.pr
            monitor-enter(r9)
            java.util.Vector r0 = new java.util.Vector     // Catch:{ all -> 0x0290 }
            r0.<init>()     // Catch:{ all -> 0x0290 }
            r10 = r0
            r11 = 0
            r12 = 0
            r13 = 0
            boolean r0 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r0 == 0) goto L_0x003d
            java.io.PrintStream r0 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.String r2 = "IMAP SASL DEBUG: Mechanisms:"
            r0.print(r2)     // Catch:{ all -> 0x0290 }
            r0 = 0
        L_0x001c:
            int r2 = r8.length     // Catch:{ all -> 0x0290 }
            if (r0 < r2) goto L_0x0025
            java.io.PrintStream r0 = r1.out     // Catch:{ all -> 0x0290 }
            r0.println()     // Catch:{ all -> 0x0290 }
            goto L_0x003d
        L_0x0025:
            java.io.PrintStream r2 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = " "
            r3.<init>(r4)     // Catch:{ all -> 0x0290 }
            r4 = r8[r0]     // Catch:{ all -> 0x0290 }
            r3.append(r4)     // Catch:{ all -> 0x0290 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0290 }
            r2.print(r3)     // Catch:{ all -> 0x0290 }
            int r0 = r0 + 1
            goto L_0x001c
        L_0x003d:
            r14 = r24
            r15 = r26
            r6 = r27
            com.sun.mail.imap.protocol.IMAPSaslAuthenticator$1 r7 = new com.sun.mail.imap.protocol.IMAPSaslAuthenticator$1     // Catch:{ all -> 0x0290 }
            r7.<init>(r15, r6, r14)     // Catch:{ all -> 0x0290 }
            r5 = 0
            java.lang.String r4 = r1.name     // Catch:{ SaslException -> 0x026f }
            java.lang.String r0 = r1.host     // Catch:{ SaslException -> 0x026f }
            java.util.Properties r3 = r1.props     // Catch:{ SaslException -> 0x026f }
            r2 = r23
            r16 = r3
            r3 = r25
            r8 = r5
            r5 = r0
            r17 = r6
            r6 = r16
            javax.security.sasl.SaslClient r0 = javax.security.sasl.Sasl.createSaslClient(r2, r3, r4, r5, r6, r7)     // Catch:{ SaslException -> 0x0269 }
            r2 = r0
            if (r2 != 0) goto L_0x006f
            boolean r0 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r0 == 0) goto L_0x006d
            java.io.PrintStream r0 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.String r3 = "IMAP SASL DEBUG: No SASL support"
            r0.println(r3)     // Catch:{ all -> 0x0290 }
        L_0x006d:
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            return r8
        L_0x006f:
            boolean r0 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r0 == 0) goto L_0x008a
            java.io.PrintStream r0 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = "IMAP SASL DEBUG: SASL client "
            r3.<init>(r4)     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = r2.getMechanismName()     // Catch:{ all -> 0x0290 }
            r3.append(r4)     // Catch:{ all -> 0x0290 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0290 }
            r0.println(r3)     // Catch:{ all -> 0x0290 }
        L_0x008a:
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r1.pr     // Catch:{ Exception -> 0x024a }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x024a }
            java.lang.String r4 = "AUTHENTICATE "
            r3.<init>(r4)     // Catch:{ Exception -> 0x024a }
            java.lang.String r4 = r2.getMechanismName()     // Catch:{ Exception -> 0x024a }
            r3.append(r4)     // Catch:{ Exception -> 0x024a }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x024a }
            r4 = 0
            java.lang.String r0 = r0.writeCommand(r3, r4)     // Catch:{ Exception -> 0x024a }
            r3 = r0
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r1.pr     // Catch:{ all -> 0x0290 }
            java.io.OutputStream r0 = r0.getIMAPOutputStream()     // Catch:{ all -> 0x0290 }
            r5 = r0
            java.io.ByteArrayOutputStream r0 = new java.io.ByteArrayOutputStream     // Catch:{ all -> 0x0290 }
            r0.<init>()     // Catch:{ all -> 0x0290 }
            r6 = r0
            r0 = 2
            byte[] r0 = new byte[r0]     // Catch:{ all -> 0x0290 }
            r11 = 13
            r0[r8] = r11     // Catch:{ all -> 0x0290 }
            r11 = 10
            r16 = 1
            r0[r16] = r11     // Catch:{ all -> 0x0290 }
            r11 = r0
            java.lang.String r0 = r2.getMechanismName()     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = "XGWTRUSTEDAPP"
            boolean r0 = r0.equals(r4)     // Catch:{ all -> 0x0290 }
            r4 = r0
        L_0x00cc:
            if (r13 == 0) goto L_0x011d
            boolean r0 = r2.isComplete()     // Catch:{ all -> 0x0290 }
            if (r0 == 0) goto L_0x0103
            java.lang.String r0 = "javax.security.sasl.qop"
            java.lang.Object r0 = r2.getNegotiatedProperty(r0)     // Catch:{ all -> 0x0290 }
            java.lang.String r0 = (java.lang.String) r0     // Catch:{ all -> 0x0290 }
            if (r0 == 0) goto L_0x0101
            java.lang.String r8 = "auth-int"
            boolean r8 = r0.equalsIgnoreCase(r8)     // Catch:{ all -> 0x0290 }
            if (r8 != 0) goto L_0x00ee
            java.lang.String r8 = "auth-conf"
            boolean r8 = r0.equalsIgnoreCase(r8)     // Catch:{ all -> 0x0290 }
            if (r8 == 0) goto L_0x0103
        L_0x00ee:
            boolean r8 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r8 == 0) goto L_0x00fc
            java.io.PrintStream r8 = r1.out     // Catch:{ all -> 0x0290 }
            r18 = r0
            java.lang.String r0 = "IMAP SASL DEBUG: Mechanism requires integrity or confidentiality"
            r8.println(r0)     // Catch:{ all -> 0x0290 }
            goto L_0x00fe
        L_0x00fc:
            r18 = r0
        L_0x00fe:
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            r8 = 0
            return r8
        L_0x0101:
            r18 = r0
        L_0x0103:
            int r0 = r10.size()     // Catch:{ all -> 0x0290 }
            com.sun.mail.iap.Response[] r0 = new com.sun.mail.iap.Response[r0]     // Catch:{ all -> 0x0290 }
            r10.copyInto(r0)     // Catch:{ all -> 0x0290 }
            com.sun.mail.imap.protocol.IMAPProtocol r8 = r1.pr     // Catch:{ all -> 0x0290 }
            r8.notifyResponseHandlers(r0)     // Catch:{ all -> 0x0290 }
            com.sun.mail.imap.protocol.IMAPProtocol r8 = r1.pr     // Catch:{ all -> 0x0290 }
            r8.handleResult(r12)     // Catch:{ all -> 0x0290 }
            com.sun.mail.imap.protocol.IMAPProtocol r8 = r1.pr     // Catch:{ all -> 0x0290 }
            r8.setCapabilities(r12)     // Catch:{ all -> 0x0290 }
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            return r16
        L_0x011d:
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r1.pr     // Catch:{ Exception -> 0x022f }
            com.sun.mail.iap.Response r0 = r0.readResponse()     // Catch:{ Exception -> 0x022f }
            r12 = r0
            boolean r0 = r12.isContinuation()     // Catch:{ Exception -> 0x022f }
            if (r0 == 0) goto L_0x01f5
            r8 = 0
            r0 = r8
            byte[] r0 = (byte[]) r0     // Catch:{ Exception -> 0x022f }
            boolean r18 = r2.isComplete()     // Catch:{ Exception -> 0x022f }
            if (r18 != 0) goto L_0x0191
            com.sun.mail.iap.ByteArray r18 = r12.readByteArray()     // Catch:{ Exception -> 0x022f }
            byte[] r18 = r18.getNewBytes()     // Catch:{ Exception -> 0x022f }
            r0 = r18
            int r8 = r0.length     // Catch:{ Exception -> 0x022f }
            if (r8 <= 0) goto L_0x0150
            byte[] r8 = com.sun.mail.util.BASE64DecoderStream.decode(r0)     // Catch:{ Exception -> 0x0147 }
            r0 = r8
            goto L_0x0150
        L_0x0147:
            r0 = move-exception
            r19 = r7
            r20 = r13
            r21 = r14
            goto L_0x0236
        L_0x0150:
            boolean r8 = r1.debug     // Catch:{ Exception -> 0x022f }
            if (r8 == 0) goto L_0x0185
            java.io.PrintStream r8 = r1.out     // Catch:{ Exception -> 0x022f }
            r19 = r7
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x017e }
            r20 = r13
            java.lang.String r13 = "IMAP SASL DEBUG: challenge: "
            r7.<init>(r13)     // Catch:{ Exception -> 0x0179 }
            int r13 = r0.length     // Catch:{ Exception -> 0x0179 }
            r21 = r14
            r14 = 0
            java.lang.String r13 = com.sun.mail.util.ASCIIUtility.toString(r0, r14, r13)     // Catch:{ Exception -> 0x022d }
            r7.append(r13)     // Catch:{ Exception -> 0x022d }
            java.lang.String r13 = " :"
            r7.append(r13)     // Catch:{ Exception -> 0x022d }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x022d }
            r8.println(r7)     // Catch:{ Exception -> 0x022d }
            goto L_0x018b
        L_0x0179:
            r0 = move-exception
            r21 = r14
            goto L_0x0236
        L_0x017e:
            r0 = move-exception
            r20 = r13
            r21 = r14
            goto L_0x0236
        L_0x0185:
            r19 = r7
            r20 = r13
            r21 = r14
        L_0x018b:
            byte[] r7 = r2.evaluateChallenge(r0)     // Catch:{ Exception -> 0x022d }
            r0 = r7
            goto L_0x0197
        L_0x0191:
            r19 = r7
            r20 = r13
            r21 = r14
        L_0x0197:
            if (r0 != 0) goto L_0x01af
            boolean r7 = r1.debug     // Catch:{ Exception -> 0x022d }
            if (r7 == 0) goto L_0x01a4
            java.io.PrintStream r7 = r1.out     // Catch:{ Exception -> 0x022d }
            java.lang.String r8 = "IMAP SASL DEBUG: no response"
            r7.println(r8)     // Catch:{ Exception -> 0x022d }
        L_0x01a4:
            r5.write(r11)     // Catch:{ Exception -> 0x022d }
            r5.flush()     // Catch:{ Exception -> 0x022d }
            r6.reset()     // Catch:{ Exception -> 0x022d }
            goto L_0x0224
        L_0x01af:
            boolean r7 = r1.debug     // Catch:{ Exception -> 0x022d }
            if (r7 == 0) goto L_0x01d1
            java.io.PrintStream r7 = r1.out     // Catch:{ Exception -> 0x022d }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x022d }
            java.lang.String r13 = "IMAP SASL DEBUG: response: "
            r8.<init>(r13)     // Catch:{ Exception -> 0x022d }
            int r13 = r0.length     // Catch:{ Exception -> 0x022d }
            r14 = 0
            java.lang.String r13 = com.sun.mail.util.ASCIIUtility.toString(r0, r14, r13)     // Catch:{ Exception -> 0x022d }
            r8.append(r13)     // Catch:{ Exception -> 0x022d }
            java.lang.String r13 = " :"
            r8.append(r13)     // Catch:{ Exception -> 0x022d }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x022d }
            r7.println(r8)     // Catch:{ Exception -> 0x022d }
        L_0x01d1:
            byte[] r7 = com.sun.mail.util.BASE64EncoderStream.encode(r0)     // Catch:{ Exception -> 0x022d }
            r0 = r7
            if (r4 == 0) goto L_0x01e1
            java.lang.String r7 = "XGWTRUSTEDAPP "
            byte[] r7 = r7.getBytes()     // Catch:{ Exception -> 0x022d }
            r6.write(r7)     // Catch:{ Exception -> 0x022d }
        L_0x01e1:
            r6.write(r0)     // Catch:{ Exception -> 0x022d }
            r6.write(r11)     // Catch:{ Exception -> 0x022d }
            byte[] r7 = r6.toByteArray()     // Catch:{ Exception -> 0x022d }
            r5.write(r7)     // Catch:{ Exception -> 0x022d }
            r5.flush()     // Catch:{ Exception -> 0x022d }
            r6.reset()     // Catch:{ Exception -> 0x022d }
            goto L_0x0224
        L_0x01f5:
            r19 = r7
            r20 = r13
            r21 = r14
            boolean r0 = r12.isTagged()     // Catch:{ Exception -> 0x022d }
            if (r0 == 0) goto L_0x0213
            java.lang.String r0 = r12.getTag()     // Catch:{ Exception -> 0x022d }
            boolean r0 = r0.equals(r3)     // Catch:{ Exception -> 0x022d }
            if (r0 == 0) goto L_0x0213
            r13 = 1
            r7 = r19
            r14 = r21
            r8 = 0
            goto L_0x00cc
        L_0x0213:
            boolean r0 = r12.isBYE()     // Catch:{ Exception -> 0x022d }
            if (r0 == 0) goto L_0x0221
            r13 = 1
            r7 = r19
            r14 = r21
            r8 = 0
            goto L_0x00cc
        L_0x0221:
            r10.addElement(r12)     // Catch:{ Exception -> 0x022d }
        L_0x0224:
            r7 = r19
            r13 = r20
            r14 = r21
            r8 = 0
            goto L_0x00cc
        L_0x022d:
            r0 = move-exception
            goto L_0x0236
        L_0x022f:
            r0 = move-exception
            r19 = r7
            r20 = r13
            r21 = r14
        L_0x0236:
            boolean r7 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r7 == 0) goto L_0x023d
            r0.printStackTrace()     // Catch:{ all -> 0x0290 }
        L_0x023d:
            com.sun.mail.iap.Response r7 = com.sun.mail.iap.Response.byeResponse(r0)     // Catch:{ all -> 0x0290 }
            r12 = r7
            r13 = 1
            r7 = r19
            r14 = r21
            r8 = 0
            goto L_0x00cc
        L_0x024a:
            r0 = move-exception
            r19 = r7
            r21 = r14
            boolean r3 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r3 == 0) goto L_0x0266
            java.io.PrintStream r3 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0290 }
            java.lang.String r5 = "IMAP SASL DEBUG: AUTHENTICATE Exception: "
            r4.<init>(r5)     // Catch:{ all -> 0x0290 }
            r4.append(r0)     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0290 }
            r3.println(r4)     // Catch:{ all -> 0x0290 }
        L_0x0266:
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            r3 = 0
            return r3
        L_0x0269:
            r0 = move-exception
            r19 = r7
            r21 = r14
            goto L_0x0276
        L_0x026f:
            r0 = move-exception
            r17 = r6
            r19 = r7
            r21 = r14
        L_0x0276:
            boolean r2 = r1.debug     // Catch:{ all -> 0x0290 }
            if (r2 == 0) goto L_0x028d
            java.io.PrintStream r2 = r1.out     // Catch:{ all -> 0x0290 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0290 }
            java.lang.String r4 = "IMAP SASL DEBUG: Failed to create SASL client: "
            r3.<init>(r4)     // Catch:{ all -> 0x0290 }
            r3.append(r0)     // Catch:{ all -> 0x0290 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0290 }
            r2.println(r3)     // Catch:{ all -> 0x0290 }
        L_0x028d:
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            r2 = 0
            return r2
        L_0x0290:
            r0 = move-exception
            monitor-exit(r9)     // Catch:{ all -> 0x0290 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPSaslAuthenticator.authenticate(java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean");
    }
}
