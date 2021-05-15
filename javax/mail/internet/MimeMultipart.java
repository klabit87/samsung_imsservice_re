package javax.mail.internet;

import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessageAware;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.MultipartDataSource;

public class MimeMultipart extends Multipart {
    private static boolean bmparse;
    private static boolean ignoreMissingBoundaryParameter;
    private static boolean ignoreMissingEndBoundary;
    private boolean complete;
    protected DataSource ds;
    protected boolean parsed;
    private String preamble;

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002c A[Catch:{ SecurityException -> 0x0044 }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002e A[Catch:{ SecurityException -> 0x0044 }] */
    static {
        /*
            r0 = 1
            ignoreMissingEndBoundary = r0
            ignoreMissingBoundaryParameter = r0
            bmparse = r0
            java.lang.String r1 = "mail.mime.multipart.ignoremissingendboundary"
            java.lang.String r1 = java.lang.System.getProperty(r1)     // Catch:{ SecurityException -> 0x0044 }
            r2 = 0
            java.lang.String r3 = "false"
            if (r1 == 0) goto L_0x001a
            boolean r4 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x0044 }
            if (r4 == 0) goto L_0x001a
            r4 = r2
            goto L_0x001b
        L_0x001a:
            r4 = r0
        L_0x001b:
            ignoreMissingEndBoundary = r4     // Catch:{ SecurityException -> 0x0044 }
            java.lang.String r4 = "mail.mime.multipart.ignoremissingboundaryparameter"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x0044 }
            r1 = r4
            if (r1 == 0) goto L_0x002e
            boolean r4 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x0044 }
            if (r4 == 0) goto L_0x002e
            r4 = r2
            goto L_0x002f
        L_0x002e:
            r4 = r0
        L_0x002f:
            ignoreMissingBoundaryParameter = r4     // Catch:{ SecurityException -> 0x0044 }
            java.lang.String r4 = "mail.mime.multipart.bmparse"
            java.lang.String r4 = java.lang.System.getProperty(r4)     // Catch:{ SecurityException -> 0x0044 }
            r1 = r4
            if (r1 == 0) goto L_0x0041
            boolean r3 = r1.equalsIgnoreCase(r3)     // Catch:{ SecurityException -> 0x0044 }
            if (r3 == 0) goto L_0x0041
            r0 = r2
        L_0x0041:
            bmparse = r0     // Catch:{ SecurityException -> 0x0044 }
            goto L_0x0045
        L_0x0044:
            r0 = move-exception
        L_0x0045:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.<clinit>():void");
    }

    public MimeMultipart() {
        this("mixed");
    }

    public MimeMultipart(String subtype) {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        String boundary = UniqueValue.getUniqueBoundaryValue();
        ContentType cType = new ContentType("multipart", subtype, (ParameterList) null);
        cType.setParameter("boundary", boundary);
        this.contentType = cType.toString();
    }

    public MimeMultipart(DataSource ds2) throws MessagingException {
        this.ds = null;
        this.parsed = true;
        this.complete = true;
        this.preamble = null;
        if (ds2 instanceof MessageAware) {
            setParent(((MessageAware) ds2).getMessageContext().getPart());
        }
        if (ds2 instanceof MultipartDataSource) {
            setMultipartDataSource((MultipartDataSource) ds2);
            return;
        }
        this.parsed = false;
        this.ds = ds2;
        this.contentType = ds2.getContentType();
    }

    public synchronized void setSubType(String subtype) throws MessagingException {
        ContentType cType = new ContentType(this.contentType);
        cType.setSubType(subtype);
        this.contentType = cType.toString();
    }

    public synchronized int getCount() throws MessagingException {
        parse();
        return super.getCount();
    }

    public synchronized BodyPart getBodyPart(int index) throws MessagingException {
        parse();
        return super.getBodyPart(index);
    }

    public synchronized BodyPart getBodyPart(String CID) throws MessagingException {
        parse();
        int count = getCount();
        for (int i = 0; i < count; i++) {
            MimeBodyPart part = (MimeBodyPart) getBodyPart(i);
            String s = part.getContentID();
            if (s != null && s.equals(CID)) {
                return part;
            }
        }
        return null;
    }

    public boolean removeBodyPart(BodyPart part) throws MessagingException {
        parse();
        return super.removeBodyPart(part);
    }

    public void removeBodyPart(int index) throws MessagingException {
        parse();
        super.removeBodyPart(index);
    }

    public synchronized void addBodyPart(BodyPart part) throws MessagingException {
        parse();
        super.addBodyPart(part);
    }

    public synchronized void addBodyPart(BodyPart part, int index) throws MessagingException {
        parse();
        super.addBodyPart(part, index);
    }

    public synchronized boolean isComplete() throws MessagingException {
        parse();
        return this.complete;
    }

    public synchronized String getPreamble() throws MessagingException {
        parse();
        return this.preamble;
    }

    public synchronized void setPreamble(String preamble2) throws MessagingException {
        this.preamble = preamble2;
    }

    /* access modifiers changed from: protected */
    public void updateHeaders() throws MessagingException {
        for (int i = 0; i < this.parts.size(); i++) {
            ((MimeBodyPart) this.parts.elementAt(i)).updateHeaders();
        }
    }

    public synchronized void writeTo(OutputStream os) throws IOException, MessagingException {
        parse();
        String boundary = "--" + new ContentType(this.contentType).getParameter("boundary");
        LineOutputStream los = new LineOutputStream(os);
        if (this.preamble != null) {
            byte[] pb = ASCIIUtility.getBytes(this.preamble);
            los.write(pb);
            if (!(pb.length <= 0 || pb[pb.length - 1] == 13 || pb[pb.length - 1] == 10)) {
                los.writeln();
            }
        }
        for (int i = 0; i < this.parts.size(); i++) {
            los.writeln(boundary);
            ((MimeBodyPart) this.parts.elementAt(i)).writeTo(os);
            los.writeln();
        }
        los.writeln(String.valueOf(boundary) + "--");
    }

    /* Debug info: failed to restart local var, previous not found, register: 33 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0110, code lost:
        r19 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x0114, code lost:
        r20 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x011b, code lost:
        throw new javax.mail.MessagingException("missing multipart end boundary");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x011c, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x011d, code lost:
        r23 = r8;
        r24 = r9;
        r4 = r17;
        r6 = r20;
        r8 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0128, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0129, code lost:
        r23 = r8;
        r24 = r9;
        r4 = r17;
        r6 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x00fe, code lost:
        if (ignoreMissingEndBoundary == false) goto L_0x0110;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0100, code lost:
        r19 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:?, code lost:
        r1.complete = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void parse() throws javax.mail.MessagingException {
        /*
            r33 = this;
            r1 = r33
            monitor-enter(r33)
            boolean r0 = r1.parsed     // Catch:{ all -> 0x042e }
            if (r0 == 0) goto L_0x0009
            monitor-exit(r33)
            return
        L_0x0009:
            boolean r0 = bmparse     // Catch:{ all -> 0x042e }
            if (r0 == 0) goto L_0x0012
            r33.parsebm()     // Catch:{ all -> 0x042e }
            monitor-exit(r33)
            return
        L_0x0012:
            r2 = 0
            r3 = 0
            r4 = 0
            r6 = 0
            javax.activation.DataSource r0 = r1.ds     // Catch:{ Exception -> 0x0423 }
            java.io.InputStream r0 = r0.getInputStream()     // Catch:{ Exception -> 0x0423 }
            r2 = r0
            boolean r0 = r2 instanceof java.io.ByteArrayInputStream     // Catch:{ Exception -> 0x0423 }
            if (r0 != 0) goto L_0x0037
            boolean r0 = r2 instanceof java.io.BufferedInputStream     // Catch:{ Exception -> 0x0032 }
            if (r0 != 0) goto L_0x0037
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ Exception -> 0x0032 }
            if (r0 != 0) goto L_0x0037
            java.io.BufferedInputStream r0 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x0032 }
            r0.<init>(r2)     // Catch:{ Exception -> 0x0032 }
            r2 = r0
            goto L_0x0037
        L_0x0032:
            r0 = move-exception
            r17 = r4
            goto L_0x0426
        L_0x0037:
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ all -> 0x042e }
            if (r0 == 0) goto L_0x003f
            r0 = r2
            javax.mail.internet.SharedInputStream r0 = (javax.mail.internet.SharedInputStream) r0     // Catch:{ all -> 0x042e }
            r3 = r0
        L_0x003f:
            javax.mail.internet.ContentType r0 = new javax.mail.internet.ContentType     // Catch:{ all -> 0x042e }
            java.lang.String r8 = r1.contentType     // Catch:{ all -> 0x042e }
            r0.<init>(r8)     // Catch:{ all -> 0x042e }
            r8 = r0
            r0 = 0
            java.lang.String r9 = "boundary"
            java.lang.String r9 = r8.getParameter(r9)     // Catch:{ all -> 0x042e }
            if (r9 == 0) goto L_0x0060
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x042e }
            java.lang.String r11 = "--"
            r10.<init>(r11)     // Catch:{ all -> 0x042e }
            r10.append(r9)     // Catch:{ all -> 0x042e }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x042e }
            r0 = r10
            goto L_0x0065
        L_0x0060:
            boolean r10 = ignoreMissingBoundaryParameter     // Catch:{ all -> 0x042e }
            if (r10 == 0) goto L_0x0415
            r10 = r0
        L_0x0065:
            com.sun.mail.util.LineInputStream r0 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x03fe, all -> 0x03f5 }
            r0.<init>(r2)     // Catch:{ IOException -> 0x03fe, all -> 0x03f5 }
            r11 = r0
            r0 = 0
            r12 = 0
            r13 = r12
            r12 = r0
        L_0x006f:
            java.lang.String r0 = r11.readLine()     // Catch:{ IOException -> 0x03fe, all -> 0x03f5 }
            r14 = r0
            r15 = 1
            if (r0 != 0) goto L_0x007a
            r17 = r4
            goto L_0x00bb
        L_0x007a:
            int r0 = r14.length()     // Catch:{ IOException -> 0x03fe, all -> 0x03f5 }
            int r0 = r0 - r15
            r15 = r0
        L_0x0080:
            if (r15 >= 0) goto L_0x0085
            r17 = r4
            goto L_0x0094
        L_0x0085:
            char r0 = r14.charAt(r15)     // Catch:{ IOException -> 0x03fe, all -> 0x03f5 }
            r17 = r4
            r4 = 32
            if (r0 == r4) goto L_0x03d8
            r4 = 9
            if (r0 == r4) goto L_0x03cd
        L_0x0094:
            int r0 = r15 + 1
            r4 = 0
            java.lang.String r0 = r14.substring(r4, r0)     // Catch:{ IOException -> 0x03c5, all -> 0x03bc }
            r14 = r0
            if (r10 == 0) goto L_0x00b1
            boolean r0 = r14.equals(r10)     // Catch:{ IOException -> 0x00d0, all -> 0x00c6 }
            if (r0 == 0) goto L_0x00a5
            goto L_0x00bb
        L_0x00a5:
            r23 = r8
            r24 = r9
            r25 = r11
            r26 = r12
            r27 = r13
            goto L_0x0369
        L_0x00b1:
            java.lang.String r0 = "--"
            boolean r0 = r14.startsWith(r0)     // Catch:{ IOException -> 0x03c5, all -> 0x03bc }
            if (r0 == 0) goto L_0x035f
            r10 = r14
        L_0x00bb:
            if (r14 == 0) goto L_0x033c
            if (r12 == 0) goto L_0x00d9
            java.lang.String r0 = r12.toString()     // Catch:{ IOException -> 0x00d0, all -> 0x00c6 }
            r1.preamble = r0     // Catch:{ IOException -> 0x00d0, all -> 0x00c6 }
            goto L_0x00d9
        L_0x00c6:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r4 = r17
            r8 = r0
            goto L_0x040f
        L_0x00d0:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r4 = r17
            goto L_0x0405
        L_0x00d9:
            byte[] r0 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r10)     // Catch:{ IOException -> 0x0331, all -> 0x0325 }
            int r4 = r0.length     // Catch:{ IOException -> 0x0331, all -> 0x0325 }
            r5 = 0
        L_0x00e0:
            if (r5 == 0) goto L_0x00e3
            goto L_0x0106
        L_0x00e3:
            r15 = 0
            if (r3 == 0) goto L_0x0151
            long r19 = r3.getPosition()     // Catch:{ IOException -> 0x0146, all -> 0x013a }
            r17 = r19
        L_0x00ec:
            java.lang.String r19 = r11.readLine()     // Catch:{ IOException -> 0x0146, all -> 0x013a }
            r14 = r19
            if (r19 == 0) goto L_0x00fa
            int r19 = r14.length()     // Catch:{ IOException -> 0x00d0, all -> 0x00c6 }
            if (r19 > 0) goto L_0x00ec
        L_0x00fa:
            if (r14 != 0) goto L_0x0133
            boolean r16 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0146, all -> 0x013a }
            if (r16 == 0) goto L_0x0110
            r19 = r5
            r5 = 0
            r1.complete = r5     // Catch:{ IOException -> 0x00d0, all -> 0x00c6 }
        L_0x0106:
            r2.close()     // Catch:{ IOException -> 0x010a }
            goto L_0x010b
        L_0x010a:
            r0 = move-exception
        L_0x010b:
            r4 = 1
            r1.parsed = r4     // Catch:{ all -> 0x042e }
            monitor-exit(r33)
            return
        L_0x0110:
            r19 = r5
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0146, all -> 0x013a }
            r20 = r6
            java.lang.String r6 = "missing multipart end boundary"
            r5.<init>(r6)     // Catch:{ IOException -> 0x0128, all -> 0x011c }
            throw r5     // Catch:{ IOException -> 0x0128, all -> 0x011c }
        L_0x011c:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r4 = r17
            r6 = r20
            r8 = r0
            goto L_0x040f
        L_0x0128:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r4 = r17
            r6 = r20
            goto L_0x0405
        L_0x0133:
            r19 = r5
            r20 = r6
            r5 = r17
            goto L_0x015c
        L_0x013a:
            r0 = move-exception
            r20 = r6
            r23 = r8
            r24 = r9
            r4 = r17
            r8 = r0
            goto L_0x040f
        L_0x0146:
            r0 = move-exception
            r20 = r6
            r23 = r8
            r24 = r9
            r4 = r17
            goto L_0x0405
        L_0x0151:
            r19 = r5
            r20 = r6
            javax.mail.internet.InternetHeaders r5 = r1.createInternetHeaders(r2)     // Catch:{ IOException -> 0x0318, all -> 0x030a }
            r15 = r5
            r5 = r17
        L_0x015c:
            boolean r7 = r2.markSupported()     // Catch:{ IOException -> 0x02fe, all -> 0x02f1 }
            if (r7 == 0) goto L_0x02c8
            r7 = 0
            if (r3 != 0) goto L_0x0184
            java.io.ByteArrayOutputStream r17 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x017a, all -> 0x016f }
            r17.<init>()     // Catch:{ IOException -> 0x017a, all -> 0x016f }
            r7 = r17
            r17 = r20
            goto L_0x0188
        L_0x016f:
            r0 = move-exception
            r4 = r5
            r23 = r8
            r24 = r9
            r6 = r20
            r8 = r0
            goto L_0x040f
        L_0x017a:
            r0 = move-exception
            r4 = r5
            r23 = r8
            r24 = r9
            r6 = r20
            goto L_0x0405
        L_0x0184:
            long r17 = r3.getPosition()     // Catch:{ IOException -> 0x02fe, all -> 0x02f1 }
        L_0x0188:
            r20 = 1
            r21 = -1
            r22 = -1
            r23 = r8
            r24 = r9
            r25 = r11
            r8 = r17
            r11 = r22
            r22 = r10
            r10 = r21
        L_0x019c:
            r17 = r14
            if (r20 == 0) goto L_0x0223
            int r14 = r4 + 4
            int r14 = r14 + 1000
            r2.mark(r14)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r14 = 0
        L_0x01a8:
            if (r14 < r4) goto L_0x01af
            r26 = r12
            r27 = r13
            goto L_0x01be
        L_0x01af:
            r26 = r12
            int r12 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r27 = r13
            byte r13 = r0[r14]     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r13 = r13 & 255(0xff, float:3.57E-43)
            if (r12 == r13) goto L_0x021a
        L_0x01be:
            if (r14 != r4) goto L_0x0205
            int r12 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r13 = 45
            if (r12 != r13) goto L_0x01d9
            int r13 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r28 = r0
            r0 = 45
            if (r13 != r0) goto L_0x01db
            r13 = 1
            r1.complete = r13     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r0 = 1
            r13 = 0
            goto L_0x0239
        L_0x01d9:
            r28 = r0
        L_0x01db:
            r13 = 32
            if (r12 == r13) goto L_0x01ff
            r0 = 9
            if (r12 == r0) goto L_0x01ff
            r0 = 10
            if (r12 != r0) goto L_0x01e8
            goto L_0x01fb
        L_0x01e8:
            r0 = 13
            if (r12 != r0) goto L_0x0207
            r13 = 1
            r2.mark(r13)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            int r0 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r13 = 10
            if (r0 == r13) goto L_0x01fb
            r2.reset()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
        L_0x01fb:
            r0 = r19
            r13 = 0
            goto L_0x0239
        L_0x01ff:
            int r0 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r12 = r0
            goto L_0x01db
        L_0x0205:
            r28 = r0
        L_0x0207:
            r2.reset()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            if (r7 == 0) goto L_0x0229
            r0 = -1
            if (r10 == r0) goto L_0x0229
            r7.write(r10)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            if (r11 == r0) goto L_0x0217
            r7.write(r11)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
        L_0x0217:
            r11 = r0
            r10 = r0
            goto L_0x0229
        L_0x021a:
            r28 = r0
            int r14 = r14 + 1
            r12 = r26
            r13 = r27
            goto L_0x01a8
        L_0x0223:
            r28 = r0
            r26 = r12
            r27 = r13
        L_0x0229:
            int r0 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r12 = r0
            if (r0 >= 0) goto L_0x026d
            boolean r0 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            if (r0 == 0) goto L_0x0265
            r13 = 0
            r1.complete = r13     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r0 = 1
        L_0x0239:
            if (r3 == 0) goto L_0x0244
            java.io.InputStream r12 = r3.newStream(r5, r8)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            javax.mail.internet.MimeBodyPart r12 = r1.createMimeBodyPart(r12)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            goto L_0x024c
        L_0x0244:
            byte[] r12 = r7.toByteArray()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            javax.mail.internet.MimeBodyPart r12 = r1.createMimeBodyPart(r15, r12)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
        L_0x024c:
            super.addBodyPart(r12)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r14 = r17
            r10 = r22
            r11 = r25
            r12 = r26
            r13 = r27
            r17 = r5
            r6 = r8
            r8 = r23
            r9 = r24
            r5 = r0
            r0 = r28
            goto L_0x00e0
        L_0x0265:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            java.lang.String r13 = "missing multipart end boundary"
            r0.<init>(r13)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            throw r0     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
        L_0x026d:
            r13 = 0
            r0 = 13
            if (r12 == r0) goto L_0x027f
            r0 = 10
            if (r12 != r0) goto L_0x0277
            goto L_0x027f
        L_0x0277:
            r20 = 0
            if (r7 == 0) goto L_0x02af
            r7.write(r12)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            goto L_0x02af
        L_0x027f:
            r20 = 1
            if (r3 == 0) goto L_0x028d
            long r29 = r3.getPosition()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r31 = 1
            long r29 = r29 - r31
            r8 = r29
        L_0x028d:
            r10 = r12
            r0 = 13
            if (r12 != r0) goto L_0x02ae
            r14 = 1
            r2.mark(r14)     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            int r0 = r2.read()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            r12 = r0
            r13 = 10
            if (r0 != r13) goto L_0x02aa
            r11 = r12
            r14 = r17
            r12 = r26
            r13 = r27
            r0 = r28
            goto L_0x019c
        L_0x02aa:
            r2.reset()     // Catch:{ IOException -> 0x02c1, all -> 0x02b9 }
            goto L_0x02af
        L_0x02ae:
            r14 = 1
        L_0x02af:
            r14 = r17
            r12 = r26
            r13 = r27
            r0 = r28
            goto L_0x019c
        L_0x02b9:
            r0 = move-exception
            r4 = r5
            r6 = r8
            r10 = r22
            r8 = r0
            goto L_0x040f
        L_0x02c1:
            r0 = move-exception
            r4 = r5
            r6 = r8
            r10 = r22
            goto L_0x0405
        L_0x02c8:
            r28 = r0
            r23 = r8
            r24 = r9
            r22 = r10
            r25 = r11
            r26 = r12
            r27 = r13
            r17 = r14
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x02e9, all -> 0x02e0 }
            java.lang.String r7 = "Stream doesn't support mark"
            r0.<init>(r7)     // Catch:{ IOException -> 0x02e9, all -> 0x02e0 }
            throw r0     // Catch:{ IOException -> 0x02e9, all -> 0x02e0 }
        L_0x02e0:
            r0 = move-exception
            r8 = r0
            r4 = r5
            r6 = r20
            r10 = r22
            goto L_0x040f
        L_0x02e9:
            r0 = move-exception
            r4 = r5
            r6 = r20
            r10 = r22
            goto L_0x0405
        L_0x02f1:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r8 = r0
            r4 = r5
            r6 = r20
            goto L_0x040f
        L_0x02fe:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r4 = r5
            r6 = r20
            goto L_0x0405
        L_0x030a:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r8 = r0
            r4 = r17
            r6 = r20
            goto L_0x040f
        L_0x0318:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r4 = r17
            r6 = r20
            goto L_0x0405
        L_0x0325:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r8 = r0
            r4 = r17
            goto L_0x040f
        L_0x0331:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r22 = r10
            r4 = r17
            goto L_0x0405
        L_0x033c:
            r23 = r8
            r24 = r9
            r22 = r10
            r25 = r11
            r26 = r12
            r27 = r13
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0358, all -> 0x0350 }
            java.lang.String r4 = "Missing start boundary"
            r0.<init>(r4)     // Catch:{ IOException -> 0x0358, all -> 0x0350 }
            throw r0     // Catch:{ IOException -> 0x0358, all -> 0x0350 }
        L_0x0350:
            r0 = move-exception
            r8 = r0
            r4 = r17
            r10 = r22
            goto L_0x040f
        L_0x0358:
            r0 = move-exception
            r4 = r17
            r10 = r22
            goto L_0x0405
        L_0x035f:
            r23 = r8
            r24 = r9
            r25 = r11
            r26 = r12
            r27 = r13
        L_0x0369:
            int r0 = r14.length()     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            if (r0 <= 0) goto L_0x03a4
            if (r27 != 0) goto L_0x0381
            java.lang.String r0 = "line.separator"
            java.lang.String r4 = "\n"
            java.lang.String r0 = java.lang.System.getProperty(r0, r4)     // Catch:{ SecurityException -> 0x037c }
            r13 = r0
            goto L_0x0383
        L_0x037c:
            r0 = move-exception
            java.lang.String r4 = "\n"
            r13 = r4
            goto L_0x0383
        L_0x0381:
            r13 = r27
        L_0x0383:
            if (r26 != 0) goto L_0x0392
            java.lang.StringBuffer r0 = new java.lang.StringBuffer     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            int r4 = r14.length()     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            int r4 = r4 + 2
            r0.<init>(r4)     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            r12 = r0
            goto L_0x0394
        L_0x0392:
            r12 = r26
        L_0x0394:
            r12.append(r14)     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            r12.append(r13)     // Catch:{ IOException -> 0x03b8, all -> 0x03b2 }
            r4 = r17
            r8 = r23
            r9 = r24
            r11 = r25
            goto L_0x006f
        L_0x03a4:
            r4 = r17
            r8 = r23
            r9 = r24
            r11 = r25
            r12 = r26
            r13 = r27
            goto L_0x006f
        L_0x03b2:
            r0 = move-exception
            r8 = r0
            r4 = r17
            goto L_0x040f
        L_0x03b8:
            r0 = move-exception
            r4 = r17
            goto L_0x0405
        L_0x03bc:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r8 = r0
            r4 = r17
            goto L_0x040f
        L_0x03c5:
            r0 = move-exception
            r23 = r8
            r24 = r9
            r4 = r17
            goto L_0x0405
        L_0x03cd:
            r23 = r8
            r24 = r9
            r25 = r11
            r26 = r12
            r27 = r13
            goto L_0x03e4
        L_0x03d8:
            r23 = r8
            r24 = r9
            r25 = r11
            r26 = r12
            r27 = r13
            r4 = 9
        L_0x03e4:
            r5 = 1
            int r15 = r15 + -1
            r4 = r17
            r8 = r23
            r9 = r24
            r11 = r25
            r12 = r26
            r13 = r27
            goto L_0x0080
        L_0x03f5:
            r0 = move-exception
            r17 = r4
            r23 = r8
            r24 = r9
            r8 = r0
            goto L_0x040f
        L_0x03fe:
            r0 = move-exception
            r17 = r4
            r23 = r8
            r24 = r9
        L_0x0405:
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x040d }
            java.lang.String r9 = "IO Error"
            r8.<init>(r9, r0)     // Catch:{ all -> 0x040d }
            throw r8     // Catch:{ all -> 0x040d }
        L_0x040d:
            r0 = move-exception
            r8 = r0
        L_0x040f:
            r2.close()     // Catch:{ IOException -> 0x0413 }
            goto L_0x0414
        L_0x0413:
            r0 = move-exception
        L_0x0414:
            throw r8     // Catch:{ all -> 0x042e }
        L_0x0415:
            r17 = r4
            r23 = r8
            r24 = r9
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x042e }
            java.lang.String r5 = "Missing boundary parameter"
            r4.<init>(r5)     // Catch:{ all -> 0x042e }
            throw r4     // Catch:{ all -> 0x042e }
        L_0x0423:
            r0 = move-exception
            r17 = r4
        L_0x0426:
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x042e }
            java.lang.String r5 = "No inputstream from datasource"
            r4.<init>(r5, r0)     // Catch:{ all -> 0x042e }
            throw r4     // Catch:{ all -> 0x042e }
        L_0x042e:
            r0 = move-exception
            monitor-exit(r33)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.parse():void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 37 */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x0139, code lost:
        r19 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0144, code lost:
        throw new javax.mail.MessagingException("missing multipart end boundary");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x01cc, code lost:
        if (ignoreMissingEndBoundary == false) goto L_0x01ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:141:0x01ce, code lost:
        if (r3 == null) goto L_0x01d6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:142:0x01d0, code lost:
        r17 = r3.getPosition();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x01d6, code lost:
        r29 = r13;
        r1.complete = false;
        r35 = r4;
        r31 = r5;
        r21 = r12;
        r19 = true;
        r30 = r15;
        r4 = r17;
        r12 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x01ec, code lost:
        r29 = r13;
        r30 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:145:0x01f7, code lost:
        throw new javax.mail.MessagingException("missing multipart end boundary");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x02b3, code lost:
        r35 = r4;
        r21 = r5;
        r12 = r15;
        r4 = r32;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x0481, code lost:
        if (r4 > 0) goto L_0x0484;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:290:0x0484, code lost:
        r4 = r4 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:?, code lost:
        r30[r4] = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x050f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:0x0510, code lost:
        r8 = r0;
        r4 = r17;
        r6 = r20;
        r10 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x0519, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:307:0x051a, code lost:
        r4 = r17;
        r6 = r20;
        r10 = r24;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0127, code lost:
        if (ignoreMissingEndBoundary == false) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0129, code lost:
        r19 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:?, code lost:
        r1.complete = false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x024e A[Catch:{ IOException -> 0x0201, all -> 0x01f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x025a A[Catch:{ IOException -> 0x0201, all -> 0x01f8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0268  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0275 A[Catch:{ IOException -> 0x0342, all -> 0x0339 }] */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x028d A[Catch:{ IOException -> 0x0342, all -> 0x0339 }] */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x029e A[Catch:{ IOException -> 0x0342, all -> 0x0339 }] */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x0326  */
    /* JADX WARNING: Removed duplicated region for block: B:377:0x029d A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:385:0x0299 A[EDGE_INSN: B:385:0x0299->B:196:0x0299 ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void parsebm() throws javax.mail.MessagingException {
        /*
            r37 = this;
            r1 = r37
            monitor-enter(r37)
            boolean r0 = r1.parsed     // Catch:{ all -> 0x0600 }
            if (r0 == 0) goto L_0x0009
            monitor-exit(r37)
            return
        L_0x0009:
            r2 = 0
            r3 = 0
            r4 = 0
            r6 = 0
            javax.activation.DataSource r0 = r1.ds     // Catch:{ Exception -> 0x05f3 }
            java.io.InputStream r0 = r0.getInputStream()     // Catch:{ Exception -> 0x05f3 }
            r2 = r0
            boolean r0 = r2 instanceof java.io.ByteArrayInputStream     // Catch:{ Exception -> 0x05f3 }
            if (r0 != 0) goto L_0x0030
            boolean r0 = r2 instanceof java.io.BufferedInputStream     // Catch:{ Exception -> 0x0029 }
            if (r0 != 0) goto L_0x0030
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ Exception -> 0x0029 }
            if (r0 != 0) goto L_0x0030
            java.io.BufferedInputStream r0 = new java.io.BufferedInputStream     // Catch:{ Exception -> 0x0029 }
            r0.<init>(r2)     // Catch:{ Exception -> 0x0029 }
            r2 = r0
            goto L_0x0030
        L_0x0029:
            r0 = move-exception
            r17 = r4
            r20 = r6
            goto L_0x05f8
        L_0x0030:
            boolean r0 = r2 instanceof javax.mail.internet.SharedInputStream     // Catch:{ all -> 0x0600 }
            if (r0 == 0) goto L_0x0038
            r0 = r2
            javax.mail.internet.SharedInputStream r0 = (javax.mail.internet.SharedInputStream) r0     // Catch:{ all -> 0x0600 }
            r3 = r0
        L_0x0038:
            javax.mail.internet.ContentType r0 = new javax.mail.internet.ContentType     // Catch:{ all -> 0x0600 }
            java.lang.String r8 = r1.contentType     // Catch:{ all -> 0x0600 }
            r0.<init>(r8)     // Catch:{ all -> 0x0600 }
            r8 = r0
            r0 = 0
            java.lang.String r9 = "boundary"
            java.lang.String r9 = r8.getParameter(r9)     // Catch:{ all -> 0x0600 }
            if (r9 == 0) goto L_0x0059
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x0600 }
            java.lang.String r11 = "--"
            r10.<init>(r11)     // Catch:{ all -> 0x0600 }
            r10.append(r9)     // Catch:{ all -> 0x0600 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x0600 }
            r0 = r10
            goto L_0x005e
        L_0x0059:
            boolean r10 = ignoreMissingBoundaryParameter     // Catch:{ all -> 0x0600 }
            if (r10 == 0) goto L_0x05e3
            r10 = r0
        L_0x005e:
            com.sun.mail.util.LineInputStream r0 = new com.sun.mail.util.LineInputStream     // Catch:{ IOException -> 0x05ca, all -> 0x05bf }
            r0.<init>(r2)     // Catch:{ IOException -> 0x05ca, all -> 0x05bf }
            r11 = r0
            r0 = 0
            r12 = 0
            r13 = r12
            r12 = r0
        L_0x0068:
            java.lang.String r0 = r11.readLine()     // Catch:{ IOException -> 0x05ca, all -> 0x05bf }
            r14 = r0
            r15 = 1
            if (r0 != 0) goto L_0x0073
            r17 = r4
            goto L_0x00b6
        L_0x0073:
            int r0 = r14.length()     // Catch:{ IOException -> 0x05ca, all -> 0x05bf }
            int r0 = r0 - r15
            r15 = r0
        L_0x0079:
            if (r15 >= 0) goto L_0x007e
            r17 = r4
            goto L_0x008d
        L_0x007e:
            char r0 = r14.charAt(r15)     // Catch:{ IOException -> 0x05ca, all -> 0x05bf }
            r17 = r4
            r4 = 32
            if (r0 == r4) goto L_0x059e
            r4 = 9
            if (r0 == r4) goto L_0x059e
        L_0x008d:
            int r0 = r15 + 1
            r4 = 0
            java.lang.String r0 = r14.substring(r4, r0)     // Catch:{ IOException -> 0x0594, all -> 0x0589 }
            r14 = r0
            if (r10 == 0) goto L_0x00ac
            boolean r0 = r14.equals(r10)     // Catch:{ IOException -> 0x00cb, all -> 0x00c1 }
            if (r0 == 0) goto L_0x009e
            goto L_0x00b6
        L_0x009e:
            r20 = r6
            r22 = r8
            r23 = r9
            r25 = r11
            r27 = r12
            r29 = r13
            goto L_0x052e
        L_0x00ac:
            java.lang.String r0 = "--"
            boolean r0 = r14.startsWith(r0)     // Catch:{ IOException -> 0x0594, all -> 0x0589 }
            if (r0 == 0) goto L_0x0522
            r10 = r14
        L_0x00b6:
            if (r14 == 0) goto L_0x04f9
            if (r12 == 0) goto L_0x00d4
            java.lang.String r0 = r12.toString()     // Catch:{ IOException -> 0x00cb, all -> 0x00c1 }
            r1.preamble = r0     // Catch:{ IOException -> 0x00cb, all -> 0x00c1 }
            goto L_0x00d4
        L_0x00c1:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            r8 = r0
            goto L_0x05dd
        L_0x00cb:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            goto L_0x05d3
        L_0x00d4:
            byte[] r0 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r10)     // Catch:{ IOException -> 0x04ec, all -> 0x04de }
            int r4 = r0.length     // Catch:{ IOException -> 0x04ec, all -> 0x04de }
            r5 = 256(0x100, float:3.59E-43)
            int[] r5 = new int[r5]     // Catch:{ IOException -> 0x04ec, all -> 0x04de }
            r15 = 0
        L_0x00de:
            if (r15 < r4) goto L_0x04ae
            int[] r15 = new int[r4]     // Catch:{ IOException -> 0x04ec, all -> 0x04de }
            r19 = r4
            r20 = r6
            r6 = r19
        L_0x00e8:
            if (r6 > 0) goto L_0x0468
            int r6 = r4 + -1
            r7 = 1
            r15[r6] = r7     // Catch:{ IOException -> 0x045b, all -> 0x044d }
            r6 = 0
        L_0x00f1:
            if (r6 == 0) goto L_0x00f4
            goto L_0x012f
        L_0x00f4:
            r7 = 0
            if (r3 == 0) goto L_0x0167
            long r22 = r3.getPosition()     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
            r17 = r22
        L_0x00fd:
            java.lang.String r19 = r11.readLine()     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
            r14 = r19
            if (r19 == 0) goto L_0x0123
            int r19 = r14.length()     // Catch:{ IOException -> 0x0118, all -> 0x010c }
            if (r19 > 0) goto L_0x00fd
            goto L_0x0123
        L_0x010c:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            r6 = r20
            r8 = r0
            goto L_0x05dd
        L_0x0118:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            r6 = r20
            goto L_0x05d3
        L_0x0123:
            if (r14 != 0) goto L_0x0145
            boolean r16 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
            if (r16 == 0) goto L_0x0139
            r19 = r6
            r6 = 0
            r1.complete = r6     // Catch:{ IOException -> 0x0118, all -> 0x010c }
        L_0x012f:
            r2.close()     // Catch:{ IOException -> 0x0133 }
            goto L_0x0134
        L_0x0133:
            r0 = move-exception
        L_0x0134:
            r4 = 1
            r1.parsed = r4     // Catch:{ all -> 0x0600 }
            monitor-exit(r37)
            return
        L_0x0139:
            r19 = r6
            javax.mail.MessagingException r6 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
            r22 = r7
            java.lang.String r7 = "missing multipart end boundary"
            r6.<init>(r7)     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
            throw r6     // Catch:{ IOException -> 0x015c, all -> 0x0150 }
        L_0x0145:
            r19 = r6
            r22 = r7
            r23 = r9
            r22 = r8
            r8 = r17
            goto L_0x0176
        L_0x0150:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            r6 = r20
            r8 = r0
            goto L_0x05dd
        L_0x015c:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r4 = r17
            r6 = r20
            goto L_0x05d3
        L_0x0167:
            r19 = r6
            r22 = r7
            javax.mail.internet.InternetHeaders r6 = r1.createInternetHeaders(r2)     // Catch:{ IOException -> 0x045b, all -> 0x044d }
            r7 = r6
            r22 = r8
            r23 = r9
            r8 = r17
        L_0x0176:
            boolean r6 = r2.markSupported()     // Catch:{ IOException -> 0x0442, all -> 0x0436 }
            if (r6 == 0) goto L_0x0408
            r6 = 0
            if (r3 != 0) goto L_0x0196
            java.io.ByteArrayOutputStream r17 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0190, all -> 0x0189 }
            r17.<init>()     // Catch:{ IOException -> 0x0190, all -> 0x0189 }
            r6 = r17
            r17 = r20
            goto L_0x019a
        L_0x0189:
            r0 = move-exception
            r4 = r8
            r6 = r20
            r8 = r0
            goto L_0x05dd
        L_0x0190:
            r0 = move-exception
            r4 = r8
            r6 = r20
            goto L_0x05d3
        L_0x0196:
            long r17 = r3.getPosition()     // Catch:{ IOException -> 0x0442, all -> 0x0436 }
        L_0x019a:
            r24 = r10
            byte[] r10 = new byte[r4]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r20 = r10
            byte[] r10 = new byte[r4]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r21 = 0
            r25 = 0
            r26 = 1
            r36 = r11
            r11 = r10
            r10 = r20
            r20 = r26
            r26 = r14
            r14 = r25
            r25 = r36
        L_0x01b5:
            r27 = r12
            int r12 = r4 + 4
            int r12 = r12 + 1000
            r2.mark(r12)     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r12 = 0
            r28 = r12
            r12 = 0
            int r29 = readFully(r2, r10, r12, r4)     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r12 = r29
            if (r12 >= r4) goto L_0x0209
            boolean r21 = ignoreMissingEndBoundary     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            if (r21 == 0) goto L_0x01ec
            if (r3 == 0) goto L_0x01d6
            long r29 = r3.getPosition()     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r17 = r29
        L_0x01d6:
            r29 = r13
            r13 = 0
            r1.complete = r13     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r13 = 1
            r35 = r4
            r31 = r5
            r21 = r12
            r19 = r13
            r30 = r15
            r4 = r17
            r12 = r28
            goto L_0x02ba
        L_0x01ec:
            r29 = r13
            javax.mail.MessagingException r13 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r30 = r15
            java.lang.String r15 = "missing multipart end boundary"
            r13.<init>(r15)     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            throw r13     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
        L_0x01f8:
            r0 = move-exception
            r4 = r8
            r6 = r17
            r10 = r24
            r8 = r0
            goto L_0x05dd
        L_0x0201:
            r0 = move-exception
            r4 = r8
            r6 = r17
            r10 = r24
            goto L_0x05d3
        L_0x0209:
            r29 = r13
            r30 = r15
            int r13 = r4 + -1
        L_0x020f:
            if (r13 >= 0) goto L_0x0214
            r31 = r5
            goto L_0x021d
        L_0x0214:
            byte r15 = r10[r13]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r31 = r5
            byte r5 = r0[r13]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            if (r15 == r5) goto L_0x03db
        L_0x021d:
            if (r13 >= 0) goto L_0x034a
            r5 = 0
            if (r20 != 0) goto L_0x0248
            int r28 = r14 + -1
            byte r28 = r11[r28]     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r32 = r28
            r15 = r32
            r32 = r5
            r5 = 13
            if (r15 == r5) goto L_0x0235
            r5 = 10
            if (r15 != r5) goto L_0x024a
            goto L_0x0237
        L_0x0235:
            r5 = 10
        L_0x0237:
            r32 = 1
            if (r15 != r5) goto L_0x024a
            r5 = 2
            if (r14 < r5) goto L_0x024a
            int r5 = r14 + -2
            byte r5 = r11[r5]     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r15 = 13
            if (r5 != r15) goto L_0x024a
            r15 = 2
            goto L_0x024c
        L_0x0248:
            r32 = r5
        L_0x024a:
            r15 = r32
        L_0x024c:
            if (r20 != 0) goto L_0x0258
            if (r15 <= 0) goto L_0x0251
            goto L_0x0258
        L_0x0251:
            r35 = r4
            r34 = r13
            r13 = r12
            goto L_0x031e
        L_0x0258:
            if (r3 == 0) goto L_0x0268
            long r32 = r3.getPosition()     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            r5 = r12
            r34 = r13
            long r12 = (long) r4
            long r32 = r32 - r12
            long r12 = (long) r15
            long r32 = r32 - r12
            goto L_0x026d
        L_0x0268:
            r5 = r12
            r34 = r13
            r32 = r17
        L_0x026d:
            int r12 = r2.read()     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            r13 = 45
            if (r12 != r13) goto L_0x028d
            int r13 = r2.read()     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            r18 = r12
            r12 = 45
            if (r13 != r12) goto L_0x028f
            r12 = 1
            r1.complete = r12     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            r12 = 1
            r35 = r4
            r21 = r5
            r19 = r12
            r12 = r15
            r4 = r32
            goto L_0x02ba
        L_0x028d:
            r18 = r12
        L_0x028f:
            r12 = r18
        L_0x0291:
            r13 = 32
            if (r12 == r13) goto L_0x0326
            r13 = 9
            if (r12 == r13) goto L_0x0326
            r13 = 10
            if (r12 != r13) goto L_0x029e
            goto L_0x02b3
        L_0x029e:
            r13 = 13
            if (r12 != r13) goto L_0x0317
            r13 = 1
            r2.mark(r13)     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            int r13 = r2.read()     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            r17 = r12
            r12 = 10
            if (r13 == r12) goto L_0x02b3
            r2.reset()     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
        L_0x02b3:
            r35 = r4
            r21 = r5
            r12 = r15
            r4 = r32
        L_0x02ba:
            if (r3 == 0) goto L_0x02d7
            java.io.InputStream r13 = r3.newStream(r8, r4)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            javax.mail.internet.MimeBodyPart r13 = r1.createMimeBodyPart(r13)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            r15 = r13
            r13 = r21
            goto L_0x02f8
        L_0x02c8:
            r0 = move-exception
            r6 = r4
            r4 = r8
            r10 = r24
            r8 = r0
            goto L_0x05dd
        L_0x02d0:
            r0 = move-exception
            r6 = r4
            r4 = r8
            r10 = r24
            goto L_0x05d3
        L_0x02d7:
            int r13 = r14 - r12
            if (r13 <= 0) goto L_0x02e1
            int r13 = r14 - r12
            r15 = 0
            r6.write(r11, r15, r13)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
        L_0x02e1:
            boolean r13 = r1.complete     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            if (r13 != 0) goto L_0x02ee
            if (r21 <= 0) goto L_0x02ee
            r13 = r21
            r15 = 0
            r6.write(r10, r15, r13)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            goto L_0x02f0
        L_0x02ee:
            r13 = r21
        L_0x02f0:
            byte[] r15 = r6.toByteArray()     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            javax.mail.internet.MimeBodyPart r15 = r1.createMimeBodyPart(r7, r15)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
        L_0x02f8:
            super.addBodyPart(r15)     // Catch:{ IOException -> 0x02d0, all -> 0x02c8 }
            r20 = r4
            r17 = r8
            r6 = r19
            r8 = r22
            r9 = r23
            r10 = r24
            r11 = r25
            r14 = r26
            r12 = r27
            r13 = r29
            r15 = r30
            r5 = r31
            r4 = r35
            goto L_0x00f1
        L_0x0317:
            r35 = r4
            r13 = r5
            r17 = r12
            r17 = r32
        L_0x031e:
            r4 = 0
            r12 = r15
            r36 = r13
            r13 = r4
            r4 = r36
            goto L_0x0351
        L_0x0326:
            r35 = r4
            r4 = r5
            r17 = r12
            r12 = 10
            r13 = 13
            int r5 = r2.read()     // Catch:{ IOException -> 0x0342, all -> 0x0339 }
            r12 = r5
            r5 = r4
            r4 = r35
            goto L_0x0291
        L_0x0339:
            r0 = move-exception
            r4 = r8
            r10 = r24
            r6 = r32
            r8 = r0
            goto L_0x05dd
        L_0x0342:
            r0 = move-exception
            r4 = r8
            r10 = r24
            r6 = r32
            goto L_0x05d3
        L_0x034a:
            r35 = r4
            r4 = r12
            r34 = r13
            r12 = r28
        L_0x0351:
            int r5 = r13 + 1
            byte r15 = r10[r13]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r15 = r15 & 127(0x7f, float:1.78E-43)
            r15 = r31[r15]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            int r5 = r5 - r15
            r15 = r30[r13]     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            int r5 = java.lang.Math.max(r5, r15)     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r15 = 2
            if (r5 >= r15) goto L_0x039b
            if (r3 != 0) goto L_0x0371
            r15 = 1
            if (r14 <= r15) goto L_0x0371
            int r15 = r14 + -1
            r21 = r4
            r4 = 0
            r6.write(r11, r4, r15)     // Catch:{ IOException -> 0x0201, all -> 0x01f8 }
            goto L_0x0373
        L_0x0371:
            r21 = r4
        L_0x0373:
            r2.reset()     // Catch:{ IOException -> 0x03fd, all -> 0x03f1 }
            r4 = r7
            r32 = r8
            r7 = 1
            r1.skipFully(r2, r7)     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r7 = 1
            if (r14 < r7) goto L_0x0391
            int r7 = r14 + -1
            byte r7 = r11[r7]     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r8 = 0
            r11[r8] = r7     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            byte r7 = r10[r8]     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r8 = 1
            r11[r8] = r7     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r7 = 2
            r14 = r7
            r7 = 0
            goto L_0x03b7
        L_0x0391:
            r8 = r7
            r7 = 0
            byte r9 = r10[r7]     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r11[r7] = r9     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r7 = 1
            r14 = r7
            r7 = 0
            goto L_0x03b7
        L_0x039b:
            r21 = r4
            r4 = r7
            r32 = r8
            r8 = 1
            if (r14 <= 0) goto L_0x03aa
            if (r3 != 0) goto L_0x03aa
            r7 = 0
            r6.write(r11, r7, r14)     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            goto L_0x03ab
        L_0x03aa:
            r7 = 0
        L_0x03ab:
            r9 = r5
            r2.reset()     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            long r14 = (long) r9     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r1.skipFully(r2, r14)     // Catch:{ IOException -> 0x03d2, all -> 0x03c8 }
            r14 = r10
            r10 = r11
            r11 = r14
            r14 = r9
        L_0x03b7:
            r20 = 0
            r7 = r4
            r12 = r27
            r13 = r29
            r15 = r30
            r5 = r31
            r8 = r32
            r4 = r35
            goto L_0x01b5
        L_0x03c8:
            r0 = move-exception
            r8 = r0
            r6 = r17
            r10 = r24
            r4 = r32
            goto L_0x05dd
        L_0x03d2:
            r0 = move-exception
            r6 = r17
            r10 = r24
            r4 = r32
            goto L_0x05d3
        L_0x03db:
            r35 = r4
            r4 = r7
            r32 = r8
            r21 = r12
            r34 = r13
            r7 = 0
            r8 = 1
            int r13 = r34 + -1
            r7 = r4
            r5 = r31
            r8 = r32
            r4 = r35
            goto L_0x020f
        L_0x03f1:
            r0 = move-exception
            r32 = r8
            r8 = r0
            r6 = r17
            r10 = r24
            r4 = r32
            goto L_0x05dd
        L_0x03fd:
            r0 = move-exception
            r32 = r8
            r6 = r17
            r10 = r24
            r4 = r32
            goto L_0x05d3
        L_0x0408:
            r35 = r4
            r31 = r5
            r4 = r7
            r32 = r8
            r24 = r10
            r25 = r11
            r27 = r12
            r29 = r13
            r26 = r14
            r30 = r15
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x042d, all -> 0x0423 }
            java.lang.String r6 = "Stream doesn't support mark"
            r5.<init>(r6)     // Catch:{ IOException -> 0x042d, all -> 0x0423 }
            throw r5     // Catch:{ IOException -> 0x042d, all -> 0x0423 }
        L_0x0423:
            r0 = move-exception
            r8 = r0
            r6 = r20
            r10 = r24
            r4 = r32
            goto L_0x05dd
        L_0x042d:
            r0 = move-exception
            r6 = r20
            r10 = r24
            r4 = r32
            goto L_0x05d3
        L_0x0436:
            r0 = move-exception
            r32 = r8
            r24 = r10
            r8 = r0
            r6 = r20
            r4 = r32
            goto L_0x05dd
        L_0x0442:
            r0 = move-exception
            r32 = r8
            r24 = r10
            r6 = r20
            r4 = r32
            goto L_0x05d3
        L_0x044d:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r24 = r10
            r8 = r0
            r4 = r17
            r6 = r20
            goto L_0x05dd
        L_0x045b:
            r0 = move-exception
            r22 = r8
            r23 = r9
            r24 = r10
            r4 = r17
            r6 = r20
            goto L_0x05d3
        L_0x0468:
            r35 = r4
            r31 = r5
            r22 = r8
            r23 = r9
            r24 = r10
            r25 = r11
            r27 = r12
            r29 = r13
            r30 = r15
            r7 = 0
            r8 = 1
            int r4 = r35 + -1
        L_0x047e:
            if (r4 >= r6) goto L_0x0489
        L_0x0481:
            if (r4 > 0) goto L_0x0484
            goto L_0x0498
        L_0x0484:
            int r4 = r4 + -1
            r30[r4] = r6     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            goto L_0x0481
        L_0x0489:
            byte r5 = r0[r4]     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            int r9 = r4 - r6
            byte r9 = r0[r9]     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            if (r5 != r9) goto L_0x0498
            int r5 = r4 + -1
            r30[r5] = r6     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            int r4 = r4 + -1
            goto L_0x047e
        L_0x0498:
            int r6 = r6 + -1
            r8 = r22
            r9 = r23
            r10 = r24
            r11 = r25
            r12 = r27
            r13 = r29
            r15 = r30
            r5 = r31
            r4 = r35
            goto L_0x00e8
        L_0x04ae:
            r35 = r4
            r31 = r5
            r20 = r6
            r22 = r8
            r23 = r9
            r24 = r10
            r25 = r11
            r27 = r12
            r29 = r13
            r7 = 0
            r8 = 1
            byte r4 = r0[r15]     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            int r5 = r15 + 1
            r31[r4] = r5     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            int r15 = r15 + 1
            r6 = r20
            r8 = r22
            r9 = r23
            r10 = r24
            r11 = r25
            r12 = r27
            r13 = r29
            r5 = r31
            r4 = r35
            goto L_0x00de
        L_0x04de:
            r0 = move-exception
            r20 = r6
            r22 = r8
            r23 = r9
            r24 = r10
            r8 = r0
            r4 = r17
            goto L_0x05dd
        L_0x04ec:
            r0 = move-exception
            r20 = r6
            r22 = r8
            r23 = r9
            r24 = r10
            r4 = r17
            goto L_0x05d3
        L_0x04f9:
            r20 = r6
            r22 = r8
            r23 = r9
            r24 = r10
            r25 = r11
            r27 = r12
            r29 = r13
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            java.lang.String r4 = "Missing start boundary"
            r0.<init>(r4)     // Catch:{ IOException -> 0x0519, all -> 0x050f }
            throw r0     // Catch:{ IOException -> 0x0519, all -> 0x050f }
        L_0x050f:
            r0 = move-exception
            r8 = r0
            r4 = r17
            r6 = r20
            r10 = r24
            goto L_0x05dd
        L_0x0519:
            r0 = move-exception
            r4 = r17
            r6 = r20
            r10 = r24
            goto L_0x05d3
        L_0x0522:
            r20 = r6
            r22 = r8
            r23 = r9
            r25 = r11
            r27 = r12
            r29 = r13
        L_0x052e:
            int r0 = r14.length()     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            if (r0 <= 0) goto L_0x056b
            if (r29 != 0) goto L_0x0546
            java.lang.String r0 = "line.separator"
            java.lang.String r4 = "\n"
            java.lang.String r0 = java.lang.System.getProperty(r0, r4)     // Catch:{ SecurityException -> 0x0541 }
            r13 = r0
            goto L_0x0548
        L_0x0541:
            r0 = move-exception
            java.lang.String r4 = "\n"
            r13 = r4
            goto L_0x0548
        L_0x0546:
            r13 = r29
        L_0x0548:
            if (r27 != 0) goto L_0x0557
            java.lang.StringBuffer r0 = new java.lang.StringBuffer     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            int r4 = r14.length()     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            r5 = 2
            int r4 = r4 + r5
            r0.<init>(r4)     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            r12 = r0
            goto L_0x0559
        L_0x0557:
            r12 = r27
        L_0x0559:
            r12.append(r14)     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            r12.append(r13)     // Catch:{ IOException -> 0x0583, all -> 0x057b }
            r4 = r17
            r6 = r20
            r8 = r22
            r9 = r23
            r11 = r25
            goto L_0x0068
        L_0x056b:
            r4 = r17
            r6 = r20
            r8 = r22
            r9 = r23
            r11 = r25
            r12 = r27
            r13 = r29
            goto L_0x0068
        L_0x057b:
            r0 = move-exception
            r8 = r0
            r4 = r17
            r6 = r20
            goto L_0x05dd
        L_0x0583:
            r0 = move-exception
            r4 = r17
            r6 = r20
            goto L_0x05d3
        L_0x0589:
            r0 = move-exception
            r20 = r6
            r22 = r8
            r23 = r9
            r8 = r0
            r4 = r17
            goto L_0x05dd
        L_0x0594:
            r0 = move-exception
            r20 = r6
            r22 = r8
            r23 = r9
            r4 = r17
            goto L_0x05d3
        L_0x059e:
            r20 = r6
            r22 = r8
            r23 = r9
            r25 = r11
            r27 = r12
            r29 = r13
            r5 = 2
            r7 = 0
            r8 = 1
            int r15 = r15 + -1
            r4 = r17
            r6 = r20
            r8 = r22
            r9 = r23
            r11 = r25
            r12 = r27
            r13 = r29
            goto L_0x0079
        L_0x05bf:
            r0 = move-exception
            r17 = r4
            r20 = r6
            r22 = r8
            r23 = r9
            r8 = r0
            goto L_0x05dd
        L_0x05ca:
            r0 = move-exception
            r17 = r4
            r20 = r6
            r22 = r8
            r23 = r9
        L_0x05d3:
            javax.mail.MessagingException r8 = new javax.mail.MessagingException     // Catch:{ all -> 0x05db }
            java.lang.String r9 = "IO Error"
            r8.<init>(r9, r0)     // Catch:{ all -> 0x05db }
            throw r8     // Catch:{ all -> 0x05db }
        L_0x05db:
            r0 = move-exception
            r8 = r0
        L_0x05dd:
            r2.close()     // Catch:{ IOException -> 0x05e1 }
            goto L_0x05e2
        L_0x05e1:
            r0 = move-exception
        L_0x05e2:
            throw r8     // Catch:{ all -> 0x0600 }
        L_0x05e3:
            r17 = r4
            r20 = r6
            r22 = r8
            r23 = r9
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x0600 }
            java.lang.String r5 = "Missing boundary parameter"
            r4.<init>(r5)     // Catch:{ all -> 0x0600 }
            throw r4     // Catch:{ all -> 0x0600 }
        L_0x05f3:
            r0 = move-exception
            r17 = r4
            r20 = r6
        L_0x05f8:
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x0600 }
            java.lang.String r5 = "No inputstream from datasource"
            r4.<init>(r5, r0)     // Catch:{ all -> 0x0600 }
            throw r4     // Catch:{ all -> 0x0600 }
        L_0x0600:
            r0 = move-exception
            monitor-exit(r37)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.MimeMultipart.parsebm():void");
    }

    private static int readFully(InputStream in, byte[] buf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int total = 0;
        while (len > 0) {
            int bsize = in.read(buf, off, len);
            if (bsize <= 0) {
                break;
            }
            off += bsize;
            total += bsize;
            len -= bsize;
        }
        if (total > 0) {
            return total;
        }
        return -1;
    }

    private void skipFully(InputStream in, long offset) throws IOException {
        while (offset > 0) {
            long cur = in.skip(offset);
            if (cur > 0) {
                offset -= cur;
            } else {
                throw new EOFException("can't skip");
            }
        }
    }

    /* access modifiers changed from: protected */
    public InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
        return new InternetHeaders(is);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InternetHeaders headers, byte[] content) throws MessagingException {
        return new MimeBodyPart(headers, content);
    }

    /* access modifiers changed from: protected */
    public MimeBodyPart createMimeBodyPart(InputStream is) throws MessagingException {
        return new MimeBodyPart(is);
    }
}
