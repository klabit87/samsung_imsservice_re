package com.sun.mail.imap;

import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.IllegalWriteException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeUtility;

public class IMAPBodyPart extends MimeBodyPart {
    private BODYSTRUCTURE bs;
    private String description;
    private boolean headersLoaded = false;
    private IMAPMessage message;
    private String sectionId;
    private String type;

    protected IMAPBodyPart(BODYSTRUCTURE bs2, String sid, IMAPMessage message2) {
        this.bs = bs2;
        this.sectionId = sid;
        this.message = message2;
        this.type = new ContentType(bs2.type, bs2.subtype, bs2.cParams).toString();
    }

    /* access modifiers changed from: protected */
    public void updateHeaders() {
    }

    public int getSize() throws MessagingException {
        return this.bs.size;
    }

    public int getLineCount() throws MessagingException {
        return this.bs.lines;
    }

    public String getContentType() throws MessagingException {
        return this.type;
    }

    public String getDisposition() throws MessagingException {
        return this.bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getEncoding() throws MessagingException {
        return this.bs.encoding;
    }

    public String getContentID() throws MessagingException {
        return this.bs.id;
    }

    public String getContentMD5() throws MessagingException {
        return this.bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getDescription() throws MessagingException {
        String str = this.description;
        if (str != null) {
            return str;
        }
        if (this.bs.description == null) {
            return null;
        }
        try {
            this.description = MimeUtility.decodeText(this.bs.description);
        } catch (UnsupportedEncodingException e) {
            this.description = this.bs.description;
        }
        return this.description;
    }

    public void setDescription(String description2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String getFileName() throws MessagingException {
        String filename = null;
        if (this.bs.dParams != null) {
            filename = this.bs.dParams.get("filename");
        }
        if (filename != null || this.bs.cParams == null) {
            return filename;
        }
        return this.bs.cParams.get("name");
    }

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0054, code lost:
        if (r0 == null) goto L_0x0057;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0056, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005e, code lost:
        throw new javax.mail.MessagingException("No content");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r8 = this;
            r0 = 0
            com.sun.mail.imap.IMAPMessage r1 = r8.message
            boolean r1 = r1.getPeek()
            com.sun.mail.imap.IMAPMessage r2 = r8.message
            java.lang.Object r2 = r2.getMessageCacheLock()
            monitor-enter(r2)
            com.sun.mail.imap.IMAPMessage r3 = r8.message     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r3.getProtocol()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.IMAPMessage r4 = r8.message     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            r4.checkExpunged()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            boolean r4 = r3.isREV1()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            if (r4 == 0) goto L_0x0037
            com.sun.mail.imap.IMAPMessage r4 = r8.message     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            int r4 = r4.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            r5 = -1
            if (r4 == r5) goto L_0x0037
            com.sun.mail.imap.IMAPInputStream r4 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.IMAPMessage r5 = r8.message     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            java.lang.String r6 = r8.sectionId     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r7 = r8.bs     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            int r7 = r7.size     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            r4.<init>(r5, r6, r7, r1)     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            monitor-exit(r2)     // Catch:{ all -> 0x005f }
            return r4
        L_0x0037:
            com.sun.mail.imap.IMAPMessage r4 = r8.message     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            int r4 = r4.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            if (r1 == 0) goto L_0x0046
            java.lang.String r5 = r8.sectionId     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.protocol.BODY r5 = r3.peekBody(r4, r5)     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            goto L_0x004c
        L_0x0046:
            java.lang.String r5 = r8.sectionId     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            com.sun.mail.imap.protocol.BODY r5 = r3.fetchBody(r4, r5)     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
        L_0x004c:
            if (r5 == 0) goto L_0x0053
            java.io.ByteArrayInputStream r6 = r5.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0061 }
            r0 = r6
        L_0x0053:
            monitor-exit(r2)     // Catch:{ all -> 0x005f }
            if (r0 == 0) goto L_0x0057
            return r0
        L_0x0057:
            javax.mail.MessagingException r2 = new javax.mail.MessagingException
            java.lang.String r3 = "No content"
            r2.<init>(r3)
            throw r2
        L_0x005f:
            r3 = move-exception
            goto L_0x007d
        L_0x0061:
            r3 = move-exception
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x005f }
            java.lang.String r5 = r3.getMessage()     // Catch:{ all -> 0x005f }
            r4.<init>(r5, r3)     // Catch:{ all -> 0x005f }
            throw r4     // Catch:{ all -> 0x005f }
        L_0x006c:
            r3 = move-exception
            javax.mail.FolderClosedException r4 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x005f }
            com.sun.mail.imap.IMAPMessage r5 = r8.message     // Catch:{ all -> 0x005f }
            javax.mail.Folder r5 = r5.getFolder()     // Catch:{ all -> 0x005f }
            java.lang.String r6 = r3.getMessage()     // Catch:{ all -> 0x005f }
            r4.<init>(r5, r6)     // Catch:{ all -> 0x005f }
            throw r4     // Catch:{ all -> 0x005f }
        L_0x007d:
            monitor-exit(r2)     // Catch:{ all -> 0x005f }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPBodyPart.getContentStream():java.io.InputStream");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        if (this.dh == null) {
            if (this.bs.isMulti()) {
                this.dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.bs.bodies, this.sectionId, this.message));
            } else if (this.bs.isNested() && this.message.isREV1()) {
                this.dh = new DataHandler(new IMAPNestedMessage(this.message, this.bs.bodies[0], this.bs.envelope, this.sectionId), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Object o, String type2) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void setContent(Multipart mp) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public String[] getHeader(String name) throws MessagingException {
        loadHeaders();
        return super.getHeader(name);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration getAllHeaders() throws MessagingException {
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPBodyPart is read-only");
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0066, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00b9, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00c3, code lost:
        throw new javax.mail.MessagingException(r1.getMessage(), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c4, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00d5, code lost:
        r1 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00b9 A[ExcHandler: ProtocolException (e com.sun.mail.iap.ProtocolException), Splitter:B:12:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00c4 A[Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066, all -> 0x00d5 }, ExcHandler: ConnectionException (e com.sun.mail.iap.ConnectionException), Splitter:B:12:0x0019] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadHeaders() throws javax.mail.MessagingException {
        /*
            r7 = this;
            monitor-enter(r7)
            boolean r0 = r7.headersLoaded     // Catch:{ all -> 0x00d8 }
            if (r0 == 0) goto L_0x0007
            monitor-exit(r7)
            return
        L_0x0007:
            javax.mail.internet.InternetHeaders r0 = r7.headers     // Catch:{ all -> 0x00d8 }
            if (r0 != 0) goto L_0x0012
            javax.mail.internet.InternetHeaders r0 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x00d8 }
            r0.<init>()     // Catch:{ all -> 0x00d8 }
            r7.headers = r0     // Catch:{ all -> 0x00d8 }
        L_0x0012:
            com.sun.mail.imap.IMAPMessage r0 = r7.message     // Catch:{ all -> 0x00d8 }
            java.lang.Object r0 = r0.getMessageCacheLock()     // Catch:{ all -> 0x00d8 }
            monitor-enter(r0)     // Catch:{ all -> 0x00d8 }
            com.sun.mail.imap.IMAPMessage r1 = r7.message     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x00b9 }
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r1.getProtocol()     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            com.sun.mail.imap.IMAPMessage r2 = r7.message     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            r2.checkExpunged()     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            boolean r2 = r1.isREV1()     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            if (r2 == 0) goto L_0x0068
            com.sun.mail.imap.IMAPMessage r2 = r7.message     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            int r2 = r2.getSequenceNumber()     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = r7.sectionId     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = java.lang.String.valueOf(r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            r3.<init>(r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = ".MIME"
            r3.append(r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r3 = r3.toString()     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            com.sun.mail.imap.protocol.BODY r3 = r1.peekBody(r2, r3)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            if (r3 == 0) goto L_0x005e
            java.io.ByteArrayInputStream r4 = r3.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            if (r4 == 0) goto L_0x0056
            javax.mail.internet.InternetHeaders r5 = r7.headers     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            r5.load(r4)     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            goto L_0x00af
        L_0x0056:
            javax.mail.MessagingException r5 = new javax.mail.MessagingException     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r6 = "Failed to fetch headers"
            r5.<init>(r6)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            throw r5     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
        L_0x005e:
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r5 = "Failed to fetch headers"
            r4.<init>(r5)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            throw r4     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
        L_0x0066:
            r1 = move-exception
            goto L_0x00ba
        L_0x0068:
            javax.mail.internet.InternetHeaders r2 = r7.headers     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r3 = "Content-Type"
            java.lang.String r4 = r7.type     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            r2.addHeader(r3, r4)     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            javax.mail.internet.InternetHeaders r2 = r7.headers     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r3 = "Content-Transfer-Encoding"
            com.sun.mail.imap.protocol.BODYSTRUCTURE r4 = r7.bs     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r4 = r4.encoding     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            r2.addHeader(r3, r4)     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r2 = r7.bs     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r2 = r2.description     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            if (r2 == 0) goto L_0x008d
            javax.mail.internet.InternetHeaders r2 = r7.headers     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r3 = "Content-Description"
            com.sun.mail.imap.protocol.BODYSTRUCTURE r4 = r7.bs     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = r4.description     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            r2.addHeader(r3, r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
        L_0x008d:
            com.sun.mail.imap.protocol.BODYSTRUCTURE r2 = r7.bs     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r2 = r2.id     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            if (r2 == 0) goto L_0x009e
            javax.mail.internet.InternetHeaders r2 = r7.headers     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r3 = "Content-ID"
            com.sun.mail.imap.protocol.BODYSTRUCTURE r4 = r7.bs     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = r4.id     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            r2.addHeader(r3, r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
        L_0x009e:
            com.sun.mail.imap.protocol.BODYSTRUCTURE r2 = r7.bs     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            java.lang.String r2 = r2.md5     // Catch:{ ConnectionException -> 0x00b5, ProtocolException -> 0x00b9 }
            if (r2 == 0) goto L_0x00af
            javax.mail.internet.InternetHeaders r2 = r7.headers     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r3 = "Content-MD5"
            com.sun.mail.imap.protocol.BODYSTRUCTURE r4 = r7.bs     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            java.lang.String r4 = r4.md5     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
            r2.addHeader(r3, r4)     // Catch:{ ConnectionException -> 0x00c4, ProtocolException -> 0x0066 }
        L_0x00af:
            monitor-exit(r0)     // Catch:{ all -> 0x00b7 }
            r0 = 1
            r7.headersLoaded = r0     // Catch:{ all -> 0x00d8 }
            monitor-exit(r7)
            return
        L_0x00b5:
            r1 = move-exception
            goto L_0x00c5
        L_0x00b7:
            r1 = move-exception
            goto L_0x00d6
        L_0x00b9:
            r1 = move-exception
        L_0x00ba:
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x00d5 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x00d5 }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x00d5 }
            throw r2     // Catch:{ all -> 0x00d5 }
        L_0x00c4:
            r1 = move-exception
        L_0x00c5:
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x00d5 }
            com.sun.mail.imap.IMAPMessage r3 = r7.message     // Catch:{ all -> 0x00d5 }
            javax.mail.Folder r3 = r3.getFolder()     // Catch:{ all -> 0x00d5 }
            java.lang.String r4 = r1.getMessage()     // Catch:{ all -> 0x00d5 }
            r2.<init>(r3, r4)     // Catch:{ all -> 0x00d5 }
            throw r2     // Catch:{ all -> 0x00d5 }
        L_0x00d5:
            r1 = move-exception
        L_0x00d6:
            monitor-exit(r0)     // Catch:{ all -> 0x00b7 }
            throw r1     // Catch:{ all -> 0x00d8 }
        L_0x00d8:
            r0 = move-exception
            monitor-exit(r7)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPBodyPart.loadHeaders():void");
    }
}
