package com.sun.mail.imap;

import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;
import com.sun.mail.imap.protocol.ENVELOPE;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.RFC822DATA;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.IllegalWriteException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

public class IMAPMessage extends MimeMessage {
    private static String EnvelopeCmd = "ENVELOPE INTERNALDATE RFC822.SIZE";
    protected BODYSTRUCTURE bs;
    private String description;
    protected ENVELOPE envelope;
    private boolean headersLoaded = false;
    private Hashtable loadedHeaders;
    private boolean peek;
    private Date receivedDate;
    protected String sectionId;
    private int seqnum;
    /* access modifiers changed from: private */
    public int size = -1;
    private String subject;
    private String type;
    private long uid = -1;

    protected IMAPMessage(IMAPFolder folder, int msgnum, int seqnum2) {
        super((Folder) folder, msgnum);
        this.seqnum = seqnum2;
        this.flags = null;
    }

    protected IMAPMessage(Session session) {
        super(session);
    }

    /* access modifiers changed from: protected */
    public IMAPProtocol getProtocol() throws ProtocolException, FolderClosedException {
        ((IMAPFolder) this.folder).waitIfIdle();
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p;
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public boolean isREV1() throws FolderClosedException {
        IMAPProtocol p = ((IMAPFolder) this.folder).protocol;
        if (p != null) {
            return p.isREV1();
        }
        throw new FolderClosedException(this.folder);
    }

    /* access modifiers changed from: protected */
    public Object getMessageCacheLock() {
        return ((IMAPFolder) this.folder).messageCacheLock;
    }

    /* access modifiers changed from: protected */
    public int getSequenceNumber() {
        return this.seqnum;
    }

    /* access modifiers changed from: protected */
    public void setSequenceNumber(int seqnum2) {
        this.seqnum = seqnum2;
    }

    /* access modifiers changed from: protected */
    public void setMessageNumber(int msgnum) {
        super.setMessageNumber(msgnum);
    }

    /* access modifiers changed from: protected */
    public long getUID() {
        return this.uid;
    }

    /* access modifiers changed from: protected */
    public void setUID(long uid2) {
        this.uid = uid2;
    }

    /* access modifiers changed from: protected */
    public void setExpunged(boolean set) {
        super.setExpunged(set);
        this.seqnum = -1;
    }

    /* access modifiers changed from: protected */
    public void checkExpunged() throws MessageRemovedException {
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* access modifiers changed from: protected */
    public void forceCheckExpunged() throws MessageRemovedException, FolderClosedException {
        synchronized (getMessageCacheLock()) {
            try {
                getProtocol().noop();
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException e) {
            }
        }
        if (this.expunged) {
            throw new MessageRemovedException();
        }
    }

    /* access modifiers changed from: protected */
    public int getFetchBlockSize() {
        return ((IMAPStore) this.folder.getStore()).getFetchBlockSize();
    }

    public Address[] getFrom() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return aaclone(this.envelope.from);
    }

    public void setFrom(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addFrom(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address getSender() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.envelope.sender != null) {
            return this.envelope.sender[0];
        }
        return null;
    }

    public void setSender(Address address) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getRecipients(Message.RecipientType type2) throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (type2 == Message.RecipientType.TO) {
            return aaclone(this.envelope.to);
        }
        if (type2 == Message.RecipientType.CC) {
            return aaclone(this.envelope.cc);
        }
        if (type2 == Message.RecipientType.BCC) {
            return aaclone(this.envelope.bcc);
        }
        return super.getRecipients(type2);
    }

    public void setRecipients(Message.RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addRecipients(Message.RecipientType type2, Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Address[] getReplyTo() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return aaclone(this.envelope.replyTo);
    }

    public void setReplyTo(Address[] addresses) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getSubject() throws MessagingException {
        checkExpunged();
        String str = this.subject;
        if (str != null) {
            return str;
        }
        loadEnvelope();
        if (this.envelope.subject == null) {
            return null;
        }
        try {
            this.subject = MimeUtility.decodeText(this.envelope.subject);
        } catch (UnsupportedEncodingException e) {
            this.subject = this.envelope.subject;
        }
        return this.subject;
    }

    public void setSubject(String subject2, String charset) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getSentDate() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.envelope.date == null) {
            return null;
        }
        return new Date(this.envelope.date.getTime());
    }

    public void setSentDate(Date d) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Date getReceivedDate() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        if (this.receivedDate == null) {
            return null;
        }
        return new Date(this.receivedDate.getTime());
    }

    public int getSize() throws MessagingException {
        checkExpunged();
        if (this.size == -1) {
            loadEnvelope();
        }
        return this.size;
    }

    public int getLineCount() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.bs.lines;
    }

    public String[] getContentLanguage() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        if (this.bs.language != null) {
            return (String[]) this.bs.language.clone();
        }
        return null;
    }

    public void setContentLanguage(String[] languages) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getInReplyTo() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return this.envelope.inReplyTo;
    }

    public String getContentType() throws MessagingException {
        checkExpunged();
        if (this.type == null) {
            loadBODYSTRUCTURE();
            this.type = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams).toString();
        }
        return this.type;
    }

    public String getDisposition() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.bs.disposition;
    }

    public void setDisposition(String disposition) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getEncoding() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.bs.encoding;
    }

    public String getContentID() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.bs.id;
    }

    public void setContentID(String cid) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getContentMD5() throws MessagingException {
        checkExpunged();
        loadBODYSTRUCTURE();
        return this.bs.md5;
    }

    public void setContentMD5(String md5) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getDescription() throws MessagingException {
        checkExpunged();
        String str = this.description;
        if (str != null) {
            return str;
        }
        loadBODYSTRUCTURE();
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
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public String getMessageID() throws MessagingException {
        checkExpunged();
        loadEnvelope();
        return this.envelope.messageId;
    }

    public String getFileName() throws MessagingException {
        checkExpunged();
        String filename = null;
        loadBODYSTRUCTURE();
        if (this.bs.dParams != null) {
            filename = this.bs.dParams.get("filename");
        }
        if (filename != null || this.bs.cParams == null) {
            return filename;
        }
        return this.bs.cParams.get("name");
    }

    public void setFileName(String filename) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0072, code lost:
        if (r0 == null) goto L_0x0075;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0074, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        throw new javax.mail.MessagingException("No content");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.io.InputStream getContentStream() throws javax.mail.MessagingException {
        /*
            r8 = this;
            r0 = 0
            boolean r1 = r8.getPeek()
            java.lang.Object r2 = r8.getMessageCacheLock()
            monitor-enter(r2)
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r8.getProtocol()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            r8.checkExpunged()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            boolean r4 = r3.isREV1()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            if (r4 == 0) goto L_0x0033
            int r4 = r8.getFetchBlockSize()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            r5 = -1
            if (r4 == r5) goto L_0x0033
            com.sun.mail.imap.IMAPInputStream r4 = new com.sun.mail.imap.IMAPInputStream     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            java.lang.String r6 = "TEXT"
            java.lang.String r6 = r8.toSection(r6)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r7 = r8.bs     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            if (r7 == 0) goto L_0x002e
            com.sun.mail.imap.protocol.BODYSTRUCTURE r5 = r8.bs     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            int r5 = r5.size     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
        L_0x002e:
            r4.<init>(r8, r6, r5, r1)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            monitor-exit(r2)     // Catch:{ all -> 0x007d }
            return r4
        L_0x0033:
            boolean r4 = r3.isREV1()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            if (r4 == 0) goto L_0x0060
            if (r1 == 0) goto L_0x004a
            int r4 = r8.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            java.lang.String r5 = "TEXT"
            java.lang.String r5 = r8.toSection(r5)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            com.sun.mail.imap.protocol.BODY r4 = r3.peekBody(r4, r5)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            goto L_0x0058
        L_0x004a:
            int r4 = r8.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            java.lang.String r5 = "TEXT"
            java.lang.String r5 = r8.toSection(r5)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            com.sun.mail.imap.protocol.BODY r4 = r3.fetchBody(r4, r5)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
        L_0x0058:
            if (r4 == 0) goto L_0x0071
            java.io.ByteArrayInputStream r5 = r4.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            r0 = r5
            goto L_0x0071
        L_0x0060:
            int r4 = r8.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            java.lang.String r5 = "TEXT"
            com.sun.mail.imap.protocol.RFC822DATA r4 = r3.fetchRFC822(r4, r5)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            if (r4 == 0) goto L_0x0071
            java.io.ByteArrayInputStream r5 = r4.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x007f }
            r0 = r5
        L_0x0071:
            monitor-exit(r2)     // Catch:{ all -> 0x007d }
            if (r0 == 0) goto L_0x0075
            return r0
        L_0x0075:
            javax.mail.MessagingException r2 = new javax.mail.MessagingException
            java.lang.String r3 = "No content"
            r2.<init>(r3)
            throw r2
        L_0x007d:
            r3 = move-exception
            goto L_0x009a
        L_0x007f:
            r3 = move-exception
            r8.forceCheckExpunged()     // Catch:{ all -> 0x007d }
            javax.mail.MessagingException r4 = new javax.mail.MessagingException     // Catch:{ all -> 0x007d }
            java.lang.String r5 = r3.getMessage()     // Catch:{ all -> 0x007d }
            r4.<init>(r5, r3)     // Catch:{ all -> 0x007d }
            throw r4     // Catch:{ all -> 0x007d }
        L_0x008d:
            r3 = move-exception
            javax.mail.FolderClosedException r4 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x007d }
            javax.mail.Folder r5 = r8.folder     // Catch:{ all -> 0x007d }
            java.lang.String r6 = r3.getMessage()     // Catch:{ all -> 0x007d }
            r4.<init>(r5, r6)     // Catch:{ all -> 0x007d }
            throw r4     // Catch:{ all -> 0x007d }
        L_0x009a:
            monitor-exit(r2)     // Catch:{ all -> 0x007d }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.getContentStream():java.io.InputStream");
    }

    public synchronized DataHandler getDataHandler() throws MessagingException {
        String str;
        checkExpunged();
        if (this.dh == null) {
            loadBODYSTRUCTURE();
            if (this.type == null) {
                this.type = new ContentType(this.bs.type, this.bs.subtype, this.bs.cParams).toString();
            }
            if (this.bs.isMulti()) {
                this.dh = new DataHandler((DataSource) new IMAPMultipartDataSource(this, this.bs.bodies, this.sectionId, this));
            } else if (this.bs.isNested() && isREV1()) {
                BODYSTRUCTURE bodystructure = this.bs.bodies[0];
                ENVELOPE envelope2 = this.bs.envelope;
                if (this.sectionId == null) {
                    str = "1";
                } else {
                    str = String.valueOf(this.sectionId) + ".1";
                }
                this.dh = new DataHandler(new IMAPNestedMessage(this, bodystructure, envelope2, str), this.type);
            }
        }
        return super.getDataHandler();
    }

    public void setDataHandler(DataHandler content) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    public void writeTo(OutputStream os) throws IOException, MessagingException {
        BODY b;
        InputStream is = null;
        boolean pk = getPeek();
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                if (p.isREV1()) {
                    if (pk) {
                        b = p.peekBody(getSequenceNumber(), this.sectionId);
                    } else {
                        b = p.fetchBody(getSequenceNumber(), this.sectionId);
                    }
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    RFC822DATA rd = p.fetchRFC822(getSequenceNumber(), (String) null);
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is != null) {
            byte[] bytes = new byte[1024];
            while (true) {
                int read = is.read(bytes);
                int count = read;
                if (read != -1) {
                    os.write(bytes, 0, count);
                } else {
                    return;
                }
            }
        } else {
            throw new MessagingException("No content");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public String[] getHeader(String name) throws MessagingException {
        checkExpunged();
        if (isHeaderLoaded(name)) {
            return this.headers.getHeader(name);
        }
        InputStream is = null;
        synchronized (getMessageCacheLock()) {
            try {
                IMAPProtocol p = getProtocol();
                checkExpunged();
                if (p.isREV1()) {
                    int sequenceNumber = getSequenceNumber();
                    BODY b = p.peekBody(sequenceNumber, toSection("HEADER.FIELDS (" + name + ")"));
                    if (b != null) {
                        is = b.getByteArrayInputStream();
                    }
                } else {
                    int sequenceNumber2 = getSequenceNumber();
                    RFC822DATA rd = p.fetchRFC822(sequenceNumber2, "HEADER.LINES (" + name + ")");
                    if (rd != null) {
                        is = rd.getByteArrayInputStream();
                    }
                }
            } catch (ConnectionException cex) {
                throw new FolderClosedException(this.folder, cex.getMessage());
            } catch (ProtocolException pex) {
                forceCheckExpunged();
                throw new MessagingException(pex.getMessage(), pex);
            }
        }
        if (is == null) {
            return null;
        }
        if (this.headers == null) {
            this.headers = new InternetHeaders();
        }
        this.headers.load(is);
        setHeaderLoaded(name);
        return this.headers.getHeader(name);
    }

    public String getHeader(String name, String delimiter) throws MessagingException {
        checkExpunged();
        if (getHeader(name) == null) {
            return null;
        }
        return this.headers.getHeader(name, delimiter);
    }

    public void setHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void addHeader(String name, String value) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public void removeHeader(String name) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration getAllHeaders() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaders();
    }

    public Enumeration getMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaders(names);
    }

    public Enumeration getNonMatchingHeaders(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaders(names);
    }

    public void addHeaderLine(String line) throws MessagingException {
        throw new IllegalWriteException("IMAPMessage is read-only");
    }

    public Enumeration getAllHeaderLines() throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getAllHeaderLines();
    }

    public Enumeration getMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getMatchingHeaderLines(names);
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) throws MessagingException {
        checkExpunged();
        loadHeaders();
        return super.getNonMatchingHeaderLines(names);
    }

    public synchronized Flags getFlags() throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.getFlags();
    }

    public synchronized boolean isSet(Flags.Flag flag) throws MessagingException {
        checkExpunged();
        loadFlags();
        return super.isSet(flag);
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0033, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setFlags(javax.mail.Flags r6, boolean r7) throws javax.mail.MessagingException {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.Object r0 = r5.getMessageCacheLock()     // Catch:{ all -> 0x0035 }
            monitor-enter(r0)     // Catch:{ all -> 0x0035 }
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r5.getProtocol()     // Catch:{ ConnectionException -> 0x0024, ProtocolException -> 0x0019 }
            r5.checkExpunged()     // Catch:{ ConnectionException -> 0x0024, ProtocolException -> 0x0019 }
            int r2 = r5.getSequenceNumber()     // Catch:{ ConnectionException -> 0x0024, ProtocolException -> 0x0019 }
            r1.storeFlags((int) r2, (javax.mail.Flags) r6, (boolean) r7)     // Catch:{ ConnectionException -> 0x0024, ProtocolException -> 0x0019 }
            monitor-exit(r0)     // Catch:{ all -> 0x0017 }
            monitor-exit(r5)
            return
        L_0x0017:
            r1 = move-exception
            goto L_0x0031
        L_0x0019:
            r1 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0017 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x0017 }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x0017 }
            throw r2     // Catch:{ all -> 0x0017 }
        L_0x0024:
            r1 = move-exception
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0017 }
            javax.mail.Folder r3 = r5.folder     // Catch:{ all -> 0x0017 }
            java.lang.String r4 = r1.getMessage()     // Catch:{ all -> 0x0017 }
            r2.<init>(r3, r4)     // Catch:{ all -> 0x0017 }
            throw r2     // Catch:{ all -> 0x0017 }
        L_0x0031:
            monitor-exit(r0)     // Catch:{ all -> 0x0033 }
            throw r1     // Catch:{ all -> 0x0035 }
        L_0x0033:
            r1 = move-exception
            goto L_0x0031
        L_0x0035:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.setFlags(javax.mail.Flags, boolean):void");
    }

    public synchronized void setPeek(boolean peek2) {
        this.peek = peek2;
    }

    public synchronized boolean getPeek() {
        return this.peek;
    }

    public synchronized void invalidateHeaders() {
        this.headersLoaded = false;
        this.loadedHeaders = null;
        this.envelope = null;
        this.bs = null;
        this.receivedDate = null;
        this.size = -1;
        this.type = null;
        this.subject = null;
        this.description = null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00bf, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00c0, code lost:
        r17 = r3;
        r19 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00db, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00dc, code lost:
        r17 = r3;
        r19 = r5;
        r20 = r10;
        r21 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00e6, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00f0, code lost:
        throw new javax.mail.MessagingException(r0.getMessage(), r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0109, code lost:
        return;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:54:0x00bd, B:62:0x00cf, B:64:0x00d1] */
    /* JADX WARNING: Removed duplicated region for block: B:159:0x0263 A[Catch:{ all -> 0x0286, all -> 0x02a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0268 A[Catch:{ all -> 0x0286, all -> 0x02a4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00e6 A[ExcHandler: ProtocolException (r0v34 'pex' com.sun.mail.iap.ProtocolException A[CUSTOM_DECLARE]), Splitter:B:62:0x00cf] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x00f1 A[Catch:{ ConnectionException -> 0x00db, CommandFailedException -> 0x00f1, ProtocolException -> 0x00e6, all -> 0x00bf }, ExcHandler: CommandFailedException (e com.sun.mail.iap.CommandFailedException), Splitter:B:62:0x00cf] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void fetch(com.sun.mail.imap.IMAPFolder r25, javax.mail.Message[] r26, javax.mail.FetchProfile r27) throws javax.mail.MessagingException {
        /*
            r1 = r25
            r2 = r27
            java.lang.StringBuffer r0 = new java.lang.StringBuffer
            r0.<init>()
            r3 = r0
            r0 = 1
            r4 = 0
            javax.mail.FetchProfile$Item r5 = javax.mail.FetchProfile.Item.ENVELOPE
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x001a
            java.lang.String r5 = EnvelopeCmd
            r3.append(r5)
            r0 = 0
        L_0x001a:
            javax.mail.FetchProfile$Item r5 = javax.mail.FetchProfile.Item.FLAGS
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x002d
            if (r0 == 0) goto L_0x0027
            java.lang.String r5 = "FLAGS"
            goto L_0x0029
        L_0x0027:
            java.lang.String r5 = " FLAGS"
        L_0x0029:
            r3.append(r5)
            r0 = 0
        L_0x002d:
            javax.mail.FetchProfile$Item r5 = javax.mail.FetchProfile.Item.CONTENT_INFO
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x0040
            if (r0 == 0) goto L_0x003a
            java.lang.String r5 = "BODYSTRUCTURE"
            goto L_0x003c
        L_0x003a:
            java.lang.String r5 = " BODYSTRUCTURE"
        L_0x003c:
            r3.append(r5)
            r0 = 0
        L_0x0040:
            javax.mail.UIDFolder$FetchProfileItem r5 = javax.mail.UIDFolder.FetchProfileItem.UID
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x0053
            if (r0 == 0) goto L_0x004d
            java.lang.String r5 = "UID"
            goto L_0x004f
        L_0x004d:
            java.lang.String r5 = " UID"
        L_0x004f:
            r3.append(r5)
            r0 = 0
        L_0x0053:
            com.sun.mail.imap.IMAPFolder$FetchProfileItem r5 = com.sun.mail.imap.IMAPFolder.FetchProfileItem.HEADERS
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x007a
            r4 = 1
            com.sun.mail.imap.protocol.IMAPProtocol r5 = r1.protocol
            boolean r5 = r5.isREV1()
            if (r5 == 0) goto L_0x006f
            if (r0 == 0) goto L_0x0069
            java.lang.String r5 = "BODY.PEEK[HEADER]"
            goto L_0x006b
        L_0x0069:
            java.lang.String r5 = " BODY.PEEK[HEADER]"
        L_0x006b:
            r3.append(r5)
            goto L_0x0079
        L_0x006f:
            if (r0 == 0) goto L_0x0074
            java.lang.String r5 = "RFC822.HEADER"
            goto L_0x0076
        L_0x0074:
            java.lang.String r5 = " RFC822.HEADER"
        L_0x0076:
            r3.append(r5)
        L_0x0079:
            r0 = 0
        L_0x007a:
            com.sun.mail.imap.IMAPFolder$FetchProfileItem r5 = com.sun.mail.imap.IMAPFolder.FetchProfileItem.SIZE
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)
            if (r5 == 0) goto L_0x008f
            if (r0 == 0) goto L_0x0087
            java.lang.String r5 = "RFC822.SIZE"
            goto L_0x0089
        L_0x0087:
            java.lang.String r5 = " RFC822.SIZE"
        L_0x0089:
            r3.append(r5)
            r0 = 0
            r5 = r0
            goto L_0x0090
        L_0x008f:
            r5 = r0
        L_0x0090:
            r0 = 0
            r6 = r0
            java.lang.String[] r6 = (java.lang.String[]) r6
            if (r4 != 0) goto L_0x00ad
            java.lang.String[] r6 = r27.getHeaderNames()
            int r7 = r6.length
            if (r7 <= 0) goto L_0x00ad
            if (r5 != 0) goto L_0x00a4
            java.lang.String r7 = " "
            r3.append(r7)
        L_0x00a4:
            com.sun.mail.imap.protocol.IMAPProtocol r7 = r1.protocol
            java.lang.String r7 = craftHeaderCmd(r7, r6)
            r3.append(r7)
        L_0x00ad:
            com.sun.mail.imap.IMAPMessage$1FetchProfileCondition r7 = new com.sun.mail.imap.IMAPMessage$1FetchProfileCondition
            r7.<init>(r2)
            java.lang.Object r8 = r1.messageCacheLock
            monitor-enter(r8)
            r9 = r26
            com.sun.mail.imap.protocol.MessageSet[] r10 = com.sun.mail.imap.Utility.toMessageSet(r9, r7)     // Catch:{ all -> 0x029d }
            if (r10 != 0) goto L_0x00c6
            monitor-exit(r8)     // Catch:{ all -> 0x00bf }
            return
        L_0x00bf:
            r0 = move-exception
            r17 = r3
            r19 = r5
            goto L_0x02a2
        L_0x00c6:
            com.sun.mail.iap.Response[] r0 = (com.sun.mail.iap.Response[]) r0     // Catch:{ all -> 0x029d }
            r11 = r0
            java.util.Vector r0 = new java.util.Vector     // Catch:{ all -> 0x029d }
            r0.<init>()     // Catch:{ all -> 0x029d }
            r12 = r0
            com.sun.mail.imap.protocol.IMAPProtocol r0 = r1.protocol     // Catch:{ ConnectionException -> 0x028a, CommandFailedException -> 0x00f1, ProtocolException -> 0x00e6 }
            java.lang.String r13 = r3.toString()     // Catch:{ ConnectionException -> 0x00db, CommandFailedException -> 0x00f1, ProtocolException -> 0x00e6 }
            com.sun.mail.iap.Response[] r0 = r0.fetch((com.sun.mail.imap.protocol.MessageSet[]) r10, (java.lang.String) r13)     // Catch:{ ConnectionException -> 0x00db, CommandFailedException -> 0x00f1, ProtocolException -> 0x00e6 }
            r11 = r0
            goto L_0x00f2
        L_0x00db:
            r0 = move-exception
            r17 = r3
            r19 = r5
            r20 = r10
            r21 = r12
            goto L_0x0293
        L_0x00e6:
            r0 = move-exception
            javax.mail.MessagingException r13 = new javax.mail.MessagingException     // Catch:{ all -> 0x00bf }
            java.lang.String r14 = r0.getMessage()     // Catch:{ all -> 0x00bf }
            r13.<init>(r14, r0)     // Catch:{ all -> 0x00bf }
            throw r13     // Catch:{ all -> 0x00bf }
        L_0x00f1:
            r0 = move-exception
        L_0x00f2:
            if (r11 != 0) goto L_0x00f6
            monitor-exit(r8)     // Catch:{ all -> 0x00bf }
            return
        L_0x00f6:
            r0 = 0
        L_0x00f7:
            int r13 = r11.length     // Catch:{ all -> 0x029d }
            if (r0 < r13) goto L_0x010a
            int r0 = r12.size()     // Catch:{ all -> 0x00bf }
            if (r0 == 0) goto L_0x0108
            com.sun.mail.iap.Response[] r13 = new com.sun.mail.iap.Response[r0]     // Catch:{ all -> 0x00bf }
            r12.copyInto(r13)     // Catch:{ all -> 0x00bf }
            r1.handleResponses(r13)     // Catch:{ all -> 0x00bf }
        L_0x0108:
            monitor-exit(r8)     // Catch:{ all -> 0x00bf }
            return
        L_0x010a:
            r13 = r11[r0]     // Catch:{ all -> 0x029d }
            if (r13 != 0) goto L_0x0111
            r17 = r3
            goto L_0x0146
        L_0x0111:
            r13 = r11[r0]     // Catch:{ all -> 0x029d }
            boolean r13 = r13 instanceof com.sun.mail.imap.protocol.FetchResponse     // Catch:{ all -> 0x029d }
            if (r13 != 0) goto L_0x011f
            r13 = r11[r0]     // Catch:{ all -> 0x00bf }
            r12.addElement(r13)     // Catch:{ all -> 0x00bf }
            r17 = r3
            goto L_0x0146
        L_0x011f:
            r13 = r11[r0]     // Catch:{ all -> 0x029d }
            com.sun.mail.imap.protocol.FetchResponse r13 = (com.sun.mail.imap.protocol.FetchResponse) r13     // Catch:{ all -> 0x029d }
            int r14 = r13.getNumber()     // Catch:{ all -> 0x029d }
            com.sun.mail.imap.IMAPMessage r14 = r1.getMessageBySeqNumber(r14)     // Catch:{ all -> 0x029d }
            int r15 = r13.getItemCount()     // Catch:{ all -> 0x029d }
            r16 = 0
            r17 = 0
            r24 = r17
            r17 = r3
            r3 = r24
        L_0x0139:
            if (r3 < r15) goto L_0x014b
            if (r16 == 0) goto L_0x0146
            r12.addElement(r13)     // Catch:{ all -> 0x0141 }
            goto L_0x0146
        L_0x0141:
            r0 = move-exception
            r19 = r5
            goto L_0x02a2
        L_0x0146:
            int r0 = r0 + 1
            r3 = r17
            goto L_0x00f7
        L_0x014b:
            com.sun.mail.imap.protocol.Item r18 = r13.getItem((int) r3)     // Catch:{ all -> 0x0286 }
            r19 = r18
            r18 = r0
            r0 = r19
            r19 = r5
            boolean r5 = r0 instanceof javax.mail.Flags     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x017e
            javax.mail.FetchProfile$Item r5 = javax.mail.FetchProfile.Item.FLAGS     // Catch:{ all -> 0x02a4 }
            boolean r5 = r2.contains((javax.mail.FetchProfile.Item) r5)     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x0173
            if (r14 != 0) goto L_0x0166
            goto L_0x0173
        L_0x0166:
            r5 = r0
            javax.mail.Flags r5 = (javax.mail.Flags) r5     // Catch:{ all -> 0x02a4 }
            r14.flags = r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x0173:
            r5 = 1
            r16 = r5
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x017e:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.ENVELOPE     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x018f
            r5 = r0
            com.sun.mail.imap.protocol.ENVELOPE r5 = (com.sun.mail.imap.protocol.ENVELOPE) r5     // Catch:{ all -> 0x02a4 }
            r14.envelope = r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x018f:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.INTERNALDATE     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x01a4
            r5 = r0
            com.sun.mail.imap.protocol.INTERNALDATE r5 = (com.sun.mail.imap.protocol.INTERNALDATE) r5     // Catch:{ all -> 0x02a4 }
            java.util.Date r5 = r5.getDate()     // Catch:{ all -> 0x02a4 }
            r14.receivedDate = r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x01a4:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.RFC822SIZE     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x01b7
            r5 = r0
            com.sun.mail.imap.protocol.RFC822SIZE r5 = (com.sun.mail.imap.protocol.RFC822SIZE) r5     // Catch:{ all -> 0x02a4 }
            int r5 = r5.size     // Catch:{ all -> 0x02a4 }
            r14.size = r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x01b7:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.BODYSTRUCTURE     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x01c8
            r5 = r0
            com.sun.mail.imap.protocol.BODYSTRUCTURE r5 = (com.sun.mail.imap.protocol.BODYSTRUCTURE) r5     // Catch:{ all -> 0x02a4 }
            r14.bs = r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            r22 = r11
            r21 = r12
            goto L_0x026c
        L_0x01c8:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.UID     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x01f2
            r5 = r0
            com.sun.mail.imap.protocol.UID r5 = (com.sun.mail.imap.protocol.UID) r5     // Catch:{ all -> 0x02a4 }
            r20 = r10
            long r9 = r5.uid     // Catch:{ all -> 0x02a4 }
            r14.uid = r9     // Catch:{ all -> 0x02a4 }
            java.util.Hashtable r9 = r1.uidTable     // Catch:{ all -> 0x02a4 }
            if (r9 != 0) goto L_0x01e0
            java.util.Hashtable r9 = new java.util.Hashtable     // Catch:{ all -> 0x02a4 }
            r9.<init>()     // Catch:{ all -> 0x02a4 }
            r1.uidTable = r9     // Catch:{ all -> 0x02a4 }
        L_0x01e0:
            java.util.Hashtable r9 = r1.uidTable     // Catch:{ all -> 0x02a4 }
            java.lang.Long r10 = new java.lang.Long     // Catch:{ all -> 0x02a4 }
            r22 = r11
            r21 = r12
            long r11 = r5.uid     // Catch:{ all -> 0x02a4 }
            r10.<init>(r11)     // Catch:{ all -> 0x02a4 }
            r9.put(r10, r14)     // Catch:{ all -> 0x02a4 }
            goto L_0x026c
        L_0x01f2:
            r20 = r10
            r22 = r11
            r21 = r12
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.RFC822DATA     // Catch:{ all -> 0x02a4 }
            if (r5 != 0) goto L_0x0200
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.BODY     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x026c
        L_0x0200:
            boolean r5 = r0 instanceof com.sun.mail.imap.protocol.RFC822DATA     // Catch:{ all -> 0x02a4 }
            if (r5 == 0) goto L_0x020d
            r5 = r0
            com.sun.mail.imap.protocol.RFC822DATA r5 = (com.sun.mail.imap.protocol.RFC822DATA) r5     // Catch:{ all -> 0x02a4 }
            java.io.ByteArrayInputStream r5 = r5.getByteArrayInputStream()     // Catch:{ all -> 0x02a4 }
            goto L_0x0215
        L_0x020d:
            r5 = r0
            com.sun.mail.imap.protocol.BODY r5 = (com.sun.mail.imap.protocol.BODY) r5     // Catch:{ all -> 0x02a4 }
            java.io.ByteArrayInputStream r5 = r5.getByteArrayInputStream()     // Catch:{ all -> 0x02a4 }
        L_0x0215:
            javax.mail.internet.InternetHeaders r9 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x02a4 }
            r9.<init>()     // Catch:{ all -> 0x02a4 }
            r9.load(r5)     // Catch:{ all -> 0x02a4 }
            javax.mail.internet.InternetHeaders r10 = r14.headers     // Catch:{ all -> 0x02a4 }
            if (r10 == 0) goto L_0x025d
            if (r4 == 0) goto L_0x0226
            r23 = r0
            goto L_0x025f
        L_0x0226:
            java.util.Enumeration r10 = r9.getAllHeaders()     // Catch:{ all -> 0x02a4 }
        L_0x022b:
            boolean r11 = r10.hasMoreElements()     // Catch:{ all -> 0x02a4 }
            if (r11 != 0) goto L_0x0234
            r23 = r0
            goto L_0x0261
        L_0x0234:
            java.lang.Object r11 = r10.nextElement()     // Catch:{ all -> 0x02a4 }
            javax.mail.Header r11 = (javax.mail.Header) r11     // Catch:{ all -> 0x02a4 }
            java.lang.String r12 = r11.getName()     // Catch:{ all -> 0x02a4 }
            boolean r12 = r14.isHeaderLoaded(r12)     // Catch:{ all -> 0x02a4 }
            if (r12 != 0) goto L_0x0258
            javax.mail.internet.InternetHeaders r12 = r14.headers     // Catch:{ all -> 0x02a4 }
            r23 = r0
            java.lang.String r0 = r11.getName()     // Catch:{ all -> 0x02a4 }
            java.lang.String r2 = r11.getValue()     // Catch:{ all -> 0x02a4 }
            r12.addHeader(r0, r2)     // Catch:{ all -> 0x02a4 }
            r2 = r27
            r0 = r23
            goto L_0x022b
        L_0x0258:
            r23 = r0
            r2 = r27
            goto L_0x022b
        L_0x025d:
            r23 = r0
        L_0x025f:
            r14.headers = r9     // Catch:{ all -> 0x02a4 }
        L_0x0261:
            if (r4 == 0) goto L_0x0268
            r0 = 1
            r14.setHeadersLoaded(r0)     // Catch:{ all -> 0x02a4 }
            goto L_0x026c
        L_0x0268:
            r0 = 0
        L_0x0269:
            int r2 = r6.length     // Catch:{ all -> 0x02a4 }
            if (r0 < r2) goto L_0x027e
        L_0x026c:
            int r3 = r3 + 1
            r9 = r26
            r2 = r27
            r0 = r18
            r5 = r19
            r10 = r20
            r12 = r21
            r11 = r22
            goto L_0x0139
        L_0x027e:
            r2 = r6[r0]     // Catch:{ all -> 0x02a4 }
            r14.setHeaderLoaded(r2)     // Catch:{ all -> 0x02a4 }
            int r0 = r0 + 1
            goto L_0x0269
        L_0x0286:
            r0 = move-exception
            r19 = r5
            goto L_0x02a2
        L_0x028a:
            r0 = move-exception
            r17 = r3
            r19 = r5
            r20 = r10
            r21 = r12
        L_0x0293:
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x02a4 }
            java.lang.String r3 = r0.getMessage()     // Catch:{ all -> 0x02a4 }
            r2.<init>(r1, r3)     // Catch:{ all -> 0x02a4 }
            throw r2     // Catch:{ all -> 0x02a4 }
        L_0x029d:
            r0 = move-exception
            r17 = r3
            r19 = r5
        L_0x02a2:
            monitor-exit(r8)     // Catch:{ all -> 0x02a4 }
            throw r0
        L_0x02a4:
            r0 = move-exception
            goto L_0x02a2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.fetch(com.sun.mail.imap.IMAPFolder, javax.mail.Message[], javax.mail.FetchProfile):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0033, code lost:
        if (r10.envelope == null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0036, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003e, code lost:
        throw new javax.mail.MessagingException("Failed to load IMAP envelope");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x008d, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0091, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        forceCheckExpunged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x009e, code lost:
        throw new javax.mail.MessagingException(r2.getMessage(), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ac, code lost:
        r2 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0091 A[ExcHandler: ProtocolException (r2v4 'pex' com.sun.mail.iap.ProtocolException A[CUSTOM_DECLARE]), Splitter:B:10:0x000f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadEnvelope() throws javax.mail.MessagingException {
        /*
            r10 = this;
            monitor-enter(r10)
            com.sun.mail.imap.protocol.ENVELOPE r0 = r10.envelope     // Catch:{ all -> 0x00af }
            if (r0 == 0) goto L_0x0007
            monitor-exit(r10)
            return
        L_0x0007:
            r0 = 0
            com.sun.mail.iap.Response[] r0 = (com.sun.mail.iap.Response[]) r0     // Catch:{ all -> 0x00af }
            java.lang.Object r1 = r10.getMessageCacheLock()     // Catch:{ all -> 0x00af }
            monitor-enter(r1)     // Catch:{ all -> 0x00af }
            com.sun.mail.imap.protocol.IMAPProtocol r2 = r10.getProtocol()     // Catch:{ ConnectionException -> 0x009f, ProtocolException -> 0x0091 }
            r10.checkExpunged()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r3 = r10.getSequenceNumber()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            java.lang.String r4 = EnvelopeCmd     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            com.sun.mail.iap.Response[] r4 = r2.fetch((int) r3, (java.lang.String) r4)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r0 = r4
            r4 = 0
        L_0x0022:
            int r5 = r0.length     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r4 < r5) goto L_0x003f
            r2.notifyResponseHandlers(r0)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r4 = r0.length     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r4 = r4 + -1
            r4 = r0[r4]     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r2.handleResult(r4)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            monitor-exit(r1)     // Catch:{ all -> 0x008f }
            com.sun.mail.imap.protocol.ENVELOPE r1 = r10.envelope     // Catch:{ all -> 0x00af }
            if (r1 == 0) goto L_0x0037
            monitor-exit(r10)
            return
        L_0x0037:
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x00af }
            java.lang.String r2 = "Failed to load IMAP envelope"
            r1.<init>(r2)     // Catch:{ all -> 0x00af }
            throw r1     // Catch:{ all -> 0x00af }
        L_0x003f:
            r5 = r0[r4]     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r5 == 0) goto L_0x008a
            r5 = r0[r4]     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            boolean r5 = r5 instanceof com.sun.mail.imap.protocol.FetchResponse     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r5 == 0) goto L_0x008a
            r5 = r0[r4]     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            com.sun.mail.imap.protocol.FetchResponse r5 = (com.sun.mail.imap.protocol.FetchResponse) r5     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r5 = r5.getNumber()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r5 == r3) goto L_0x0054
            goto L_0x008a
        L_0x0054:
            r5 = r0[r4]     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            com.sun.mail.imap.protocol.FetchResponse r5 = (com.sun.mail.imap.protocol.FetchResponse) r5     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r6 = r5.getItemCount()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r7 = 0
        L_0x005d:
            if (r7 < r6) goto L_0x0060
            goto L_0x008a
        L_0x0060:
            com.sun.mail.imap.protocol.Item r8 = r5.getItem((int) r7)     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            boolean r9 = r8 instanceof com.sun.mail.imap.protocol.ENVELOPE     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r9 == 0) goto L_0x006e
            r9 = r8
            com.sun.mail.imap.protocol.ENVELOPE r9 = (com.sun.mail.imap.protocol.ENVELOPE) r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r10.envelope = r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            goto L_0x0087
        L_0x006e:
            boolean r9 = r8 instanceof com.sun.mail.imap.protocol.INTERNALDATE     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r9 == 0) goto L_0x007c
            r9 = r8
            com.sun.mail.imap.protocol.INTERNALDATE r9 = (com.sun.mail.imap.protocol.INTERNALDATE) r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            java.util.Date r9 = r9.getDate()     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r10.receivedDate = r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            goto L_0x0087
        L_0x007c:
            boolean r9 = r8 instanceof com.sun.mail.imap.protocol.RFC822SIZE     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            if (r9 == 0) goto L_0x0087
            r9 = r8
            com.sun.mail.imap.protocol.RFC822SIZE r9 = (com.sun.mail.imap.protocol.RFC822SIZE) r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            int r9 = r9.size     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
            r10.size = r9     // Catch:{ ConnectionException -> 0x008d, ProtocolException -> 0x0091 }
        L_0x0087:
            int r7 = r7 + 1
            goto L_0x005d
        L_0x008a:
            int r4 = r4 + 1
            goto L_0x0022
        L_0x008d:
            r2 = move-exception
            goto L_0x00a0
        L_0x008f:
            r2 = move-exception
            goto L_0x00ad
        L_0x0091:
            r2 = move-exception
            r10.forceCheckExpunged()     // Catch:{ all -> 0x00ac }
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x00ac }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x00ac }
            r3.<init>(r4, r2)     // Catch:{ all -> 0x00ac }
            throw r3     // Catch:{ all -> 0x00ac }
        L_0x009f:
            r2 = move-exception
        L_0x00a0:
            javax.mail.FolderClosedException r3 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x00ac }
            javax.mail.Folder r4 = r10.folder     // Catch:{ all -> 0x00ac }
            java.lang.String r5 = r2.getMessage()     // Catch:{ all -> 0x00ac }
            r3.<init>(r4, r5)     // Catch:{ all -> 0x00ac }
            throw r3     // Catch:{ all -> 0x00ac }
        L_0x00ac:
            r2 = move-exception
        L_0x00ad:
            monitor-exit(r1)     // Catch:{ all -> 0x008f }
            throw r2     // Catch:{ all -> 0x00af }
        L_0x00af:
            r0 = move-exception
            monitor-exit(r10)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.loadEnvelope():void");
    }

    private static String craftHeaderCmd(IMAPProtocol p, String[] hdrs) {
        StringBuffer sb;
        if (p.isREV1()) {
            sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
        } else {
            sb = new StringBuffer("RFC822.HEADER.LINES (");
        }
        for (int i = 0; i < hdrs.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hdrs[i]);
        }
        if (p.isREV1() != 0) {
            sb.append(")]");
        } else {
            sb.append(")");
        }
        return sb.toString();
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x004c, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadBODYSTRUCTURE() throws javax.mail.MessagingException {
        /*
            r5 = this;
            monitor-enter(r5)
            com.sun.mail.imap.protocol.BODYSTRUCTURE r0 = r5.bs     // Catch:{ all -> 0x004e }
            if (r0 == 0) goto L_0x0007
            monitor-exit(r5)
            return
        L_0x0007:
            java.lang.Object r0 = r5.getMessageCacheLock()     // Catch:{ all -> 0x004e }
            monitor-enter(r0)     // Catch:{ all -> 0x004e }
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r5.getProtocol()     // Catch:{ ConnectionException -> 0x003d, ProtocolException -> 0x002f }
            r5.checkExpunged()     // Catch:{ ConnectionException -> 0x003d, ProtocolException -> 0x002f }
            int r2 = r5.getSequenceNumber()     // Catch:{ ConnectionException -> 0x003d, ProtocolException -> 0x002f }
            com.sun.mail.imap.protocol.BODYSTRUCTURE r2 = r1.fetchBodyStructure(r2)     // Catch:{ ConnectionException -> 0x003d, ProtocolException -> 0x002f }
            r5.bs = r2     // Catch:{ ConnectionException -> 0x003d, ProtocolException -> 0x002f }
            if (r2 == 0) goto L_0x0022
            monitor-exit(r0)     // Catch:{ all -> 0x002d }
            monitor-exit(r5)
            return
        L_0x0022:
            r5.forceCheckExpunged()     // Catch:{ all -> 0x002d }
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x002d }
            java.lang.String r2 = "Unable to load BODYSTRUCTURE"
            r1.<init>(r2)     // Catch:{ all -> 0x002d }
            throw r1     // Catch:{ all -> 0x002d }
        L_0x002d:
            r1 = move-exception
            goto L_0x004a
        L_0x002f:
            r1 = move-exception
            r5.forceCheckExpunged()     // Catch:{ all -> 0x002d }
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x002d }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x002d }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x002d }
            throw r2     // Catch:{ all -> 0x002d }
        L_0x003d:
            r1 = move-exception
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x002d }
            javax.mail.Folder r3 = r5.folder     // Catch:{ all -> 0x002d }
            java.lang.String r4 = r1.getMessage()     // Catch:{ all -> 0x002d }
            r2.<init>(r3, r4)     // Catch:{ all -> 0x002d }
            throw r2     // Catch:{ all -> 0x002d }
        L_0x004a:
            monitor-exit(r0)     // Catch:{ all -> 0x004c }
            throw r1     // Catch:{ all -> 0x004e }
        L_0x004c:
            r1 = move-exception
            goto L_0x004a
        L_0x004e:
            r0 = move-exception
            monitor-exit(r5)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.loadBODYSTRUCTURE():void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0030, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0044, code lost:
        if (r0 == null) goto L_0x0052;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r6.headers = new javax.mail.internet.InternetHeaders(r0);
        r6.headersLoaded = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0051, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0059, code lost:
        throw new javax.mail.MessagingException("Cannot load header");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005a, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x005e, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        forceCheckExpunged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x006b, code lost:
        throw new javax.mail.MessagingException(r2.getMessage(), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x006c, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0078, code lost:
        throw new javax.mail.FolderClosedException(r6.folder, r2.getMessage());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0079, code lost:
        r2 = th;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x005e A[ExcHandler: ProtocolException (e com.sun.mail.iap.ProtocolException), Splitter:B:10:0x000d] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x006c A[Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030, all -> 0x0079 }, ExcHandler: ConnectionException (e com.sun.mail.iap.ConnectionException), Splitter:B:10:0x000d] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadHeaders() throws javax.mail.MessagingException {
        /*
            r6 = this;
            monitor-enter(r6)
            boolean r0 = r6.headersLoaded     // Catch:{ all -> 0x007c }
            if (r0 == 0) goto L_0x0007
            monitor-exit(r6)
            return
        L_0x0007:
            r0 = 0
            java.lang.Object r1 = r6.getMessageCacheLock()     // Catch:{ all -> 0x007c }
            monitor-enter(r1)     // Catch:{ all -> 0x007c }
            com.sun.mail.imap.protocol.IMAPProtocol r2 = r6.getProtocol()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x005e }
            r6.checkExpunged()     // Catch:{ ConnectionException -> 0x005a, ProtocolException -> 0x005e }
            boolean r3 = r2.isREV1()     // Catch:{ ConnectionException -> 0x005a, ProtocolException -> 0x005e }
            if (r3 == 0) goto L_0x0032
            int r3 = r6.getSequenceNumber()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030 }
            java.lang.String r4 = "HEADER"
            java.lang.String r4 = r6.toSection(r4)     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030 }
            com.sun.mail.imap.protocol.BODY r3 = r2.peekBody(r3, r4)     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030 }
            if (r3 == 0) goto L_0x0043
            java.io.ByteArrayInputStream r4 = r3.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030 }
            r0 = r4
            goto L_0x0043
        L_0x0030:
            r2 = move-exception
            goto L_0x005f
        L_0x0032:
            int r3 = r6.getSequenceNumber()     // Catch:{ ConnectionException -> 0x005a, ProtocolException -> 0x005e }
            java.lang.String r4 = "HEADER"
            com.sun.mail.imap.protocol.RFC822DATA r3 = r2.fetchRFC822(r3, r4)     // Catch:{ ConnectionException -> 0x005a, ProtocolException -> 0x005e }
            if (r3 == 0) goto L_0x0043
            java.io.ByteArrayInputStream r4 = r3.getByteArrayInputStream()     // Catch:{ ConnectionException -> 0x006c, ProtocolException -> 0x0030 }
            r0 = r4
        L_0x0043:
            monitor-exit(r1)     // Catch:{ all -> 0x005c }
            if (r0 == 0) goto L_0x0052
            javax.mail.internet.InternetHeaders r1 = new javax.mail.internet.InternetHeaders     // Catch:{ all -> 0x007c }
            r1.<init>(r0)     // Catch:{ all -> 0x007c }
            r6.headers = r1     // Catch:{ all -> 0x007c }
            r1 = 1
            r6.headersLoaded = r1     // Catch:{ all -> 0x007c }
            monitor-exit(r6)
            return
        L_0x0052:
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x007c }
            java.lang.String r2 = "Cannot load header"
            r1.<init>(r2)     // Catch:{ all -> 0x007c }
            throw r1     // Catch:{ all -> 0x007c }
        L_0x005a:
            r2 = move-exception
            goto L_0x006d
        L_0x005c:
            r2 = move-exception
            goto L_0x007a
        L_0x005e:
            r2 = move-exception
        L_0x005f:
            r6.forceCheckExpunged()     // Catch:{ all -> 0x0079 }
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x0079 }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x0079 }
            r3.<init>(r4, r2)     // Catch:{ all -> 0x0079 }
            throw r3     // Catch:{ all -> 0x0079 }
        L_0x006c:
            r2 = move-exception
        L_0x006d:
            javax.mail.FolderClosedException r3 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0079 }
            javax.mail.Folder r4 = r6.folder     // Catch:{ all -> 0x0079 }
            java.lang.String r5 = r2.getMessage()     // Catch:{ all -> 0x0079 }
            r3.<init>(r4, r5)     // Catch:{ all -> 0x0079 }
            throw r3     // Catch:{ all -> 0x0079 }
        L_0x0079:
            r2 = move-exception
        L_0x007a:
            monitor-exit(r1)     // Catch:{ all -> 0x005c }
            throw r2     // Catch:{ all -> 0x007c }
        L_0x007c:
            r0 = move-exception
            monitor-exit(r6)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.loadHeaders():void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003f, code lost:
        r1 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadFlags() throws javax.mail.MessagingException {
        /*
            r5 = this;
            monitor-enter(r5)
            javax.mail.Flags r0 = r5.flags     // Catch:{ all -> 0x0041 }
            if (r0 == 0) goto L_0x0007
            monitor-exit(r5)
            return
        L_0x0007:
            java.lang.Object r0 = r5.getMessageCacheLock()     // Catch:{ all -> 0x0041 }
            monitor-enter(r0)     // Catch:{ all -> 0x0041 }
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r5.getProtocol()     // Catch:{ ConnectionException -> 0x0030, ProtocolException -> 0x0022 }
            r5.checkExpunged()     // Catch:{ ConnectionException -> 0x0030, ProtocolException -> 0x0022 }
            int r2 = r5.getSequenceNumber()     // Catch:{ ConnectionException -> 0x0030, ProtocolException -> 0x0022 }
            javax.mail.Flags r2 = r1.fetchFlags(r2)     // Catch:{ ConnectionException -> 0x0030, ProtocolException -> 0x0022 }
            r5.flags = r2     // Catch:{ ConnectionException -> 0x0030, ProtocolException -> 0x0022 }
            monitor-exit(r0)     // Catch:{ all -> 0x0020 }
            monitor-exit(r5)
            return
        L_0x0020:
            r1 = move-exception
            goto L_0x003d
        L_0x0022:
            r1 = move-exception
            r5.forceCheckExpunged()     // Catch:{ all -> 0x0020 }
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0020 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x0020 }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x0020 }
            throw r2     // Catch:{ all -> 0x0020 }
        L_0x0030:
            r1 = move-exception
            javax.mail.FolderClosedException r2 = new javax.mail.FolderClosedException     // Catch:{ all -> 0x0020 }
            javax.mail.Folder r3 = r5.folder     // Catch:{ all -> 0x0020 }
            java.lang.String r4 = r1.getMessage()     // Catch:{ all -> 0x0020 }
            r2.<init>(r3, r4)     // Catch:{ all -> 0x0020 }
            throw r2     // Catch:{ all -> 0x0020 }
        L_0x003d:
            monitor-exit(r0)     // Catch:{ all -> 0x003f }
            throw r1     // Catch:{ all -> 0x0041 }
        L_0x003f:
            r1 = move-exception
            goto L_0x003d
        L_0x0041:
            r0 = move-exception
            monitor-exit(r5)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.loadFlags():void");
    }

    /* access modifiers changed from: private */
    public synchronized boolean areHeadersLoaded() {
        return this.headersLoaded;
    }

    private synchronized void setHeadersLoaded(boolean loaded) {
        this.headersLoaded = loaded;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001b, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isHeaderLoaded(java.lang.String r3) {
        /*
            r2 = this;
            monitor-enter(r2)
            boolean r0 = r2.headersLoaded     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0008
            r0 = 1
            monitor-exit(r2)
            return r0
        L_0x0008:
            java.util.Hashtable r0 = r2.loadedHeaders     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0019
            java.util.Hashtable r0 = r2.loadedHeaders     // Catch:{ all -> 0x001c }
            java.util.Locale r1 = java.util.Locale.ENGLISH     // Catch:{ all -> 0x001c }
            java.lang.String r1 = r3.toUpperCase(r1)     // Catch:{ all -> 0x001c }
            boolean r0 = r0.containsKey(r1)     // Catch:{ all -> 0x001c }
            goto L_0x001a
        L_0x0019:
            r0 = 0
        L_0x001a:
            monitor-exit(r2)
            return r0
        L_0x001c:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPMessage.isHeaderLoaded(java.lang.String):boolean");
    }

    private synchronized void setHeaderLoaded(String name) {
        if (this.loadedHeaders == null) {
            this.loadedHeaders = new Hashtable(1);
        }
        this.loadedHeaders.put(name.toUpperCase(Locale.ENGLISH), name);
    }

    private String toSection(String what) {
        String str = this.sectionId;
        if (str == null) {
            return what;
        }
        return String.valueOf(str) + "." + what;
    }

    private InternetAddress[] aaclone(InternetAddress[] aa) {
        if (aa == null) {
            return null;
        }
        return (InternetAddress[]) aa.clone();
    }

    /* access modifiers changed from: private */
    public Flags _getFlags() {
        return this.flags;
    }

    /* access modifiers changed from: private */
    public ENVELOPE _getEnvelope() {
        return this.envelope;
    }

    /* access modifiers changed from: private */
    public BODYSTRUCTURE _getBodyStructure() {
        return this.bs;
    }

    /* access modifiers changed from: package-private */
    public void _setFlags(Flags flags) {
        this.flags = flags;
    }

    /* access modifiers changed from: package-private */
    public Session _getSession() {
        return this.session;
    }
}
