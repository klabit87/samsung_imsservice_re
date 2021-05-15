package com.sun.mail.imap.protocol;

import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.helper.httpclient.HttpController;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.Literal;
import com.sun.mail.iap.LiteralException;
import com.sun.mail.iap.ParsingException;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.Rights;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.BASE64EncoderStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.mail.Flags;
import javax.mail.Quota;
import javax.mail.internet.MimeUtility;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPProtocol extends Protocol {
    private static final byte[] CRLF = {13, 10};
    private static final byte[] DONE = {68, 79, 78, 69, 13, 10};
    private boolean authenticated;
    private List authmechs = null;
    private ByteArray ba;
    private Map capabilities = null;
    private boolean connected = false;
    private String idleTag;
    private String name;
    private boolean rev1 = false;
    private SaslAuthenticator saslAuthenticator;
    private String[] searchCharsets;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public IMAPProtocol(String name2, String host, int port, boolean debug, PrintStream out, Properties props, boolean isSSL) throws IOException, ProtocolException {
        super(host, port, debug, out, props, "mail." + name2, isSSL);
        String str = name2;
        try {
            this.name = str;
            if (0 == 0) {
                capability();
            }
            if (hasCapability("IMAP4rev1")) {
                this.rev1 = true;
            }
            String[] strArr = new String[2];
            this.searchCharsets = strArr;
            strArr[0] = "UTF-8";
            strArr[1] = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
            this.connected = true;
            if (1 == 0) {
                disconnect();
            }
        } catch (Throwable th) {
            if (!this.connected) {
                disconnect();
            }
            throw th;
        }
    }

    public void capability() throws ProtocolException {
        Response[] r = command("CAPABILITY", (Argument) null);
        if (r[r.length - 1].isOK()) {
            this.capabilities = new HashMap(10);
            this.authmechs = new ArrayList(5);
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("CAPABILITY")) {
                        parseCapabilities(ir);
                    }
                }
            }
            return;
        }
        throw new ProtocolException(r[r.length - 1].toString());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x001a A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x001b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCapabilities(com.sun.mail.iap.Response r5) {
        /*
            r4 = this;
        L_0x0000:
            byte r0 = r5.readByte()
            r1 = r0
            if (r0 <= 0) goto L_0x000b
            r0 = 91
            if (r1 != r0) goto L_0x0000
        L_0x000b:
            if (r1 != 0) goto L_0x000e
            return
        L_0x000e:
            java.lang.String r0 = r5.readAtom()
            java.lang.String r2 = "CAPABILITY"
            boolean r2 = r0.equalsIgnoreCase(r2)
            if (r2 != 0) goto L_0x001b
            return
        L_0x001b:
            java.util.HashMap r2 = new java.util.HashMap
            r3 = 10
            r2.<init>(r3)
            r4.capabilities = r2
            java.util.ArrayList r2 = new java.util.ArrayList
            r3 = 5
            r2.<init>(r3)
            r4.authmechs = r2
            r4.parseCapabilities(r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPProtocol.setCapabilities(com.sun.mail.iap.Response):void");
    }

    /* access modifiers changed from: protected */
    public void parseCapabilities(Response r) {
        while (true) {
            String readAtom = r.readAtom(']');
            String s = readAtom;
            if (readAtom != null) {
                if (s.length() != 0) {
                    this.capabilities.put(s.toUpperCase(Locale.ENGLISH), s);
                    if (s.regionMatches(true, 0, "AUTH=", 0, 5)) {
                        this.authmechs.add(s.substring(5));
                        if (this.debug) {
                            PrintStream printStream = this.out;
                            printStream.println("IMAP DEBUG: AUTH: " + s.substring(5));
                        }
                    }
                } else if (r.peekByte() != 93) {
                    r.skipToken();
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processGreeting(Response r) throws ProtocolException {
        super.processGreeting(r);
        if (r.isOK()) {
            setCapabilities(r);
        } else if (((IMAPResponse) r).keyEquals("PREAUTH")) {
            this.authenticated = true;
            setCapabilities(r);
        } else {
            throw new ConnectionException(this, r);
        }
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public boolean isREV1() {
        return this.rev1;
    }

    /* access modifiers changed from: protected */
    public boolean supportsNonSyncLiterals() {
        return hasCapability("LITERAL+");
    }

    public Response readResponse() throws IOException, ProtocolException {
        return IMAPResponse.readResponse(this);
    }

    public boolean hasCapability(String c) {
        return this.capabilities.containsKey(c.toUpperCase(Locale.ENGLISH));
    }

    public Map getCapabilities() {
        return this.capabilities;
    }

    public void disconnect() {
        super.disconnect();
        this.authenticated = false;
    }

    public void noop() throws ProtocolException {
        if (this.debug) {
            this.out.println("IMAP DEBUG: IMAPProtocol noop");
        }
        simpleCommand("NOOP", (Argument) null);
    }

    public void logout() throws ProtocolException {
        Response[] r = command("LOGOUT", (Argument) null);
        this.authenticated = false;
        notifyResponseHandlers(r);
        disconnect();
    }

    public void login(String u, String p) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);
        args.writeString(p);
        Response[] r = command("LOGIN", args);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        setCapabilities(r[r.length - 1]);
        this.authenticated = true;
    }

    public synchronized void authlogin(String u, String p) throws ProtocolException {
        Response r;
        String s;
        Vector v = new Vector();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        try {
            tag = writeCommand("AUTHENTICATE LOGIN", (Argument) null);
        } catch (Exception ex) {
            r2 = Response.byeResponse(ex);
            done = true;
        }
        OutputStream os = getOutputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
        boolean first = true;
        while (!done) {
            try {
                r = readResponse();
                if (r.isContinuation()) {
                    if (first) {
                        s = u;
                        first = false;
                    } else {
                        s = p;
                    }
                    b64os.write(ASCIIUtility.getBytes(s));
                    b64os.flush();
                    bos.write(CRLF);
                    os.write(bos.toByteArray());
                    os.flush();
                    bos.reset();
                } else if (r.isTagged() && r.getTag().equals(tag)) {
                    done = true;
                } else if (r.isBYE()) {
                    done = true;
                } else {
                    v.addElement(r);
                }
            } catch (Exception ioex) {
                r = Response.byeResponse(ioex);
                done = true;
            }
        }
        Response[] responses = new Response[v.size()];
        v.copyInto(responses);
        notifyResponseHandlers(responses);
        handleResult(r);
        setCapabilities(r);
        this.authenticated = true;
    }

    public synchronized void authplain(String authzid, String u, String p) throws ProtocolException {
        Response r;
        Vector v = new Vector();
        String tag = null;
        Response r2 = null;
        boolean done = false;
        try {
            tag = writeCommand("AUTHENTICATE PLAIN", (Argument) null);
        } catch (Exception ex) {
            r2 = Response.byeResponse(ex);
            done = true;
        }
        OutputStream os = getOutputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream b64os = new BASE64EncoderStream(bos, Integer.MAX_VALUE);
        while (!done) {
            try {
                r = readResponse();
                if (r.isContinuation()) {
                    b64os.write(ASCIIUtility.getBytes(String.valueOf(authzid) + "\u0000" + u + "\u0000" + p));
                    b64os.flush();
                    bos.write(CRLF);
                    os.write(bos.toByteArray());
                    os.flush();
                    bos.reset();
                } else if (r.isTagged() && r.getTag().equals(tag)) {
                    done = true;
                } else if (r.isBYE()) {
                    done = true;
                } else {
                    v.addElement(r);
                }
            } catch (Exception ioex) {
                r = Response.byeResponse(ioex);
                done = true;
            }
        }
        Response[] responses = new Response[v.size()];
        v.copyInto(responses);
        notifyResponseHandlers(responses);
        handleResult(r);
        setCapabilities(r);
        this.authenticated = true;
    }

    public void sasllogin(String[] allowed, String realm, String authzid, String u, String p) throws ProtocolException {
        List v;
        if (this.saslAuthenticator == null) {
            try {
                Constructor c = Class.forName("com.sun.mail.imap.protocol.IMAPSaslAuthenticator").getConstructor(new Class[]{IMAPProtocol.class, String.class, Properties.class, Boolean.TYPE, PrintStream.class, String.class});
                Object[] objArr = new Object[6];
                objArr[0] = this;
                objArr[1] = this.name;
                objArr[2] = this.props;
                objArr[3] = this.debug ? Boolean.TRUE : Boolean.FALSE;
                objArr[4] = this.out;
                objArr[5] = this.host;
                this.saslAuthenticator = (SaslAuthenticator) c.newInstance(objArr);
            } catch (Exception ex) {
                if (this.debug) {
                    PrintStream printStream = this.out;
                    printStream.println("IMAP DEBUG: Can't load SASL authenticator: " + ex);
                    return;
                }
                return;
            }
        }
        if (allowed == null || allowed.length <= 0) {
            v = this.authmechs;
        } else {
            v = new ArrayList(allowed.length);
            for (int i = 0; i < allowed.length; i++) {
                if (this.authmechs.contains(allowed[i])) {
                    v.add(allowed[i]);
                }
            }
        }
        if (this.saslAuthenticator.authenticate((String[]) v.toArray(new String[v.size()]), realm, authzid, u, p)) {
            this.authenticated = true;
        }
    }

    /* access modifiers changed from: package-private */
    public OutputStream getIMAPOutputStream() {
        return getOutputStream();
    }

    public void proxyauth(String u) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(u);
        simpleCommand("PROXYAUTH", args);
    }

    public void startTLS() throws ProtocolException {
        try {
            super.startTLS("STARTTLS");
        } catch (ProtocolException pex) {
            throw pex;
        } catch (Exception ex) {
            notifyResponseHandlers(new Response[]{Response.byeResponse(ex)});
            disconnect();
        }
    }

    public MailboxInfo select(String mbox) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeString(mbox2);
        Response[] r = command("SELECT", args);
        MailboxInfo minfo = new MailboxInfo(r);
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            if (response.toString().indexOf("READ-ONLY") != -1) {
                minfo.mode = 1;
            } else {
                minfo.mode = 2;
            }
        }
        handleResult(response);
        return minfo;
    }

    public MailboxInfo examine(String mbox) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeString(mbox2);
        Response[] r = command("EXAMINE", args);
        MailboxInfo minfo = new MailboxInfo(r);
        minfo.mode = 1;
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return minfo;
    }

    public Status status(String mbox, String[] items) throws ProtocolException {
        if (isREV1() || hasCapability("IMAP4SUNVERSION")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            Argument itemArgs = new Argument();
            if (items == null) {
                items = Status.standardItems;
            }
            for (String writeAtom : items) {
                itemArgs.writeAtom(writeAtom);
            }
            args.writeArgument(itemArgs);
            Response[] r = command("STATUS", args);
            Status status = null;
            Response response = r[r.length - 1];
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("STATUS")) {
                            if (status == null) {
                                status = new Status(ir);
                            } else {
                                Status.add(status, new Status(ir));
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            return status;
        }
        throw new BadCommandException("STATUS not supported");
    }

    public void create(String mbox) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeString(mbox2);
        simpleCommand("CREATE", args);
    }

    public void delete(String mbox) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeString(mbox2);
        simpleCommand(HttpController.METHOD_DELETE, args);
    }

    public void rename(String o, String n) throws ProtocolException {
        String o2 = BASE64MailboxEncoder.encode(o);
        String n2 = BASE64MailboxEncoder.encode(n);
        Argument args = new Argument();
        args.writeString(o2);
        args.writeString(n2);
        simpleCommand("RENAME", args);
    }

    public void subscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(BASE64MailboxEncoder.encode(mbox));
        simpleCommand("SUBSCRIBE", args);
    }

    public void unsubscribe(String mbox) throws ProtocolException {
        Argument args = new Argument();
        args.writeString(BASE64MailboxEncoder.encode(mbox));
        simpleCommand("UNSUBSCRIBE", args);
    }

    public ListInfo[] list(String ref, String pattern) throws ProtocolException {
        return doList("LIST", ref, pattern);
    }

    public ListInfo[] lsub(String ref, String pattern) throws ProtocolException {
        return doList("LSUB", ref, pattern);
    }

    private ListInfo[] doList(String cmd, String ref, String pat) throws ProtocolException {
        String ref2 = BASE64MailboxEncoder.encode(ref);
        String pat2 = BASE64MailboxEncoder.encode(pat);
        Argument args = new Argument();
        args.writeString(ref2);
        args.writeString(pat2);
        Response[] r = command(cmd, args);
        ListInfo[] linfo = null;
        Response response = r[r.length - 1];
        if (response.isOK()) {
            Vector v = new Vector(1);
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals(cmd)) {
                        v.addElement(new ListInfo(ir));
                        r[i] = null;
                    }
                }
            }
            if (v.size() > 0) {
                linfo = new ListInfo[v.size()];
                v.copyInto(linfo);
            }
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return linfo;
    }

    public void append(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        appenduid(mbox, f, d, data, false);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data) throws ProtocolException {
        return appenduid(mbox, f, d, data, true);
    }

    public AppendUID appenduid(String mbox, Flags f, Date d, Literal data, boolean uid) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeString(mbox2);
        if (f != null) {
            if (f.contains(Flags.Flag.RECENT)) {
                f = new Flags(f);
                f.remove(Flags.Flag.RECENT);
            }
            args.writeAtom(createFlagList(f));
        }
        if (d != null) {
            args.writeString(INTERNALDATE.format(d));
        }
        args.writeBytes(data);
        Response[] r = command("APPEND", args);
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        if (uid) {
            return getAppendUID(r[r.length - 1]);
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0022 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0023  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sun.mail.imap.AppendUID getAppendUID(com.sun.mail.iap.Response r8) {
        /*
            r7 = this;
            boolean r0 = r8.isOK()
            r1 = 0
            if (r0 != 0) goto L_0x0008
            return r1
        L_0x0008:
            byte r0 = r8.readByte()
            r2 = r0
            if (r0 <= 0) goto L_0x0013
            r0 = 91
            if (r2 != r0) goto L_0x0008
        L_0x0013:
            if (r2 != 0) goto L_0x0016
            return r1
        L_0x0016:
            java.lang.String r0 = r8.readAtom()
            java.lang.String r3 = "APPENDUID"
            boolean r3 = r0.equalsIgnoreCase(r3)
            if (r3 != 0) goto L_0x0023
            return r1
        L_0x0023:
            long r3 = r8.readLong()
            long r5 = r8.readLong()
            com.sun.mail.imap.AppendUID r1 = new com.sun.mail.imap.AppendUID
            r1.<init>(r3, r5)
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.protocol.IMAPProtocol.getAppendUID(com.sun.mail.iap.Response):com.sun.mail.imap.AppendUID");
    }

    public void check() throws ProtocolException {
        simpleCommand("CHECK", (Argument) null);
    }

    public void close() throws ProtocolException {
        simpleCommand("CLOSE", (Argument) null);
    }

    public void expunge() throws ProtocolException {
        simpleCommand("EXPUNGE", (Argument) null);
    }

    public void uidexpunge(UIDSet[] set) throws ProtocolException {
        if (hasCapability("UIDPLUS")) {
            simpleCommand("UID EXPUNGE " + UIDSet.toString(set), (Argument) null);
            return;
        }
        throw new BadCommandException("UID EXPUNGE not supported");
    }

    public BODYSTRUCTURE fetchBodyStructure(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "BODYSTRUCTURE");
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODYSTRUCTURE) FetchResponse.getItem(r, msgno, BODYSTRUCTURE.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public BODY peekBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, true);
    }

    public BODY fetchBody(int msgno, String section) throws ProtocolException {
        return fetchBody(msgno, section, false);
    }

    /* access modifiers changed from: protected */
    public BODY fetchBody(int msgno, String section, boolean peek) throws ProtocolException {
        Response[] r;
        String str = "]";
        if (peek) {
            StringBuilder sb = new StringBuilder("BODY.PEEK[");
            if (section != null) {
                str = String.valueOf(section) + str;
            }
            sb.append(str);
            r = fetch(msgno, sb.toString());
        } else {
            StringBuilder sb2 = new StringBuilder("BODY[");
            if (section != null) {
                str = String.valueOf(section) + str;
            }
            sb2.append(str);
            r = fetch(msgno, sb2.toString());
        }
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODY) FetchResponse.getItem(r, msgno, BODY.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public BODY peekBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, (ByteArray) null);
    }

    public BODY fetchBody(int msgno, String section, int start, int size) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, (ByteArray) null);
    }

    public BODY peekBody(int msgno, String section, int start, int size, ByteArray ba2) throws ProtocolException {
        return fetchBody(msgno, section, start, size, true, ba2);
    }

    public BODY fetchBody(int msgno, String section, int start, int size, ByteArray ba2) throws ProtocolException {
        return fetchBody(msgno, section, start, size, false, ba2);
    }

    /* access modifiers changed from: protected */
    public BODY fetchBody(int msgno, String section, int start, int size, boolean peek, ByteArray ba2) throws ProtocolException {
        this.ba = ba2;
        StringBuilder sb = new StringBuilder(String.valueOf(peek ? "BODY.PEEK[" : "BODY["));
        String str = "]<";
        if (section != null) {
            str = String.valueOf(section) + str;
        }
        sb.append(str);
        sb.append(String.valueOf(start));
        sb.append(".");
        sb.append(String.valueOf(size));
        sb.append(">");
        Response[] r = fetch(msgno, sb.toString());
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (BODY) FetchResponse.getItem(r, msgno, BODY.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    /* access modifiers changed from: protected */
    public ByteArray getResponseBuffer() {
        ByteArray ret = this.ba;
        this.ba = null;
        return ret;
    }

    public RFC822DATA fetchRFC822(int msgno, String what) throws ProtocolException {
        String str;
        if (what == null) {
            str = "RFC822";
        } else {
            str = "RFC822." + what;
        }
        Response[] r = fetch(msgno, str);
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (RFC822DATA) FetchResponse.getItem(r, msgno, RFC822DATA.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public Flags fetchFlags(int msgno) throws ProtocolException {
        Flags flags = null;
        Response[] r = fetch(msgno, "FLAGS");
        int i = 0;
        int len = r.length;
        while (true) {
            if (i >= len) {
                break;
            }
            if (r[i] != null && (r[i] instanceof FetchResponse) && ((FetchResponse) r[i]).getNumber() == msgno) {
                Flags flags2 = (Flags) ((FetchResponse) r[i]).getItem(Flags.class);
                flags = flags2;
                if (flags2 != null) {
                    r[i] = null;
                    break;
                }
            }
            i++;
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return flags;
    }

    public UID fetchUID(int msgno) throws ProtocolException {
        Response[] r = fetch(msgno, "UID");
        notifyResponseHandlers(r);
        Response response = r[r.length - 1];
        if (response.isOK()) {
            return (UID) FetchResponse.getItem(r, msgno, UID.class);
        }
        if (response.isNO()) {
            return null;
        }
        handleResult(response);
        return null;
    }

    public UID fetchSequenceNumber(long uid) throws ProtocolException {
        UID u = null;
        Response[] r = fetch(String.valueOf(uid), "UID", true);
        int len = r.length;
        for (int i = 0; i < len; i++) {
            if (r[i] != null && (r[i] instanceof FetchResponse)) {
                UID uid2 = (UID) ((FetchResponse) r[i]).getItem(UID.class);
                u = uid2;
                if (uid2 == null) {
                    continue;
                } else if (u.uid == uid) {
                    break;
                } else {
                    u = null;
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        return u;
    }

    public UID[] fetchSequenceNumbers(long start, long end) throws ProtocolException {
        String str;
        StringBuilder sb = new StringBuilder(String.valueOf(String.valueOf(start)));
        sb.append(":");
        if (end == -1) {
            str = "*";
        } else {
            str = String.valueOf(end);
        }
        sb.append(str);
        Response[] r = fetch(sb.toString(), "UID", true);
        Vector v = new Vector();
        int len = r.length;
        for (int i = 0; i < len; i++) {
            if (r[i] != null && (r[i] instanceof FetchResponse)) {
                UID uid = (UID) ((FetchResponse) r[i]).getItem(UID.class);
                UID u = uid;
                if (uid != null) {
                    v.addElement(u);
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        UID[] ua = new UID[v.size()];
        v.copyInto(ua);
        return ua;
    }

    public UID[] fetchSequenceNumbers(long[] uids) throws ProtocolException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < uids.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(uids[i]));
        }
        Response[] r = fetch(sb.toString(), "UID", true);
        Vector v = new Vector();
        int len = r.length;
        for (int i2 = 0; i2 < len; i2++) {
            if (r[i2] != null && (r[i2] instanceof FetchResponse)) {
                UID uid = (UID) ((FetchResponse) r[i2]).getItem(UID.class);
                UID u = uid;
                if (uid != null) {
                    v.addElement(u);
                }
            }
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
        UID[] ua = new UID[v.size()];
        v.copyInto(ua);
        return ua;
    }

    public Response[] fetch(MessageSet[] msgsets, String what) throws ProtocolException {
        return fetch(MessageSet.toString(msgsets), what, false);
    }

    public Response[] fetch(int start, int end, String what) throws ProtocolException {
        return fetch(String.valueOf(String.valueOf(start)) + ":" + String.valueOf(end), what, false);
    }

    public Response[] fetch(int msg, String what) throws ProtocolException {
        return fetch(String.valueOf(msg), what, false);
    }

    private Response[] fetch(String msgSequence, String what, boolean uid) throws ProtocolException {
        if (uid) {
            return command("UID FETCH " + msgSequence + " (" + what + ")", (Argument) null);
        }
        return command("FETCH " + msgSequence + " (" + what + ")", (Argument) null);
    }

    public void copy(MessageSet[] msgsets, String mbox) throws ProtocolException {
        copy(MessageSet.toString(msgsets), mbox);
    }

    public void copy(int start, int end, String mbox) throws ProtocolException {
        copy(String.valueOf(String.valueOf(start)) + ":" + String.valueOf(end), mbox);
    }

    private void copy(String msgSequence, String mbox) throws ProtocolException {
        String mbox2 = BASE64MailboxEncoder.encode(mbox);
        Argument args = new Argument();
        args.writeAtom(msgSequence);
        args.writeString(mbox2);
        simpleCommand("COPY", args);
    }

    public void storeFlags(MessageSet[] msgsets, Flags flags, boolean set) throws ProtocolException {
        storeFlags(MessageSet.toString(msgsets), flags, set);
    }

    public void storeFlags(int start, int end, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(String.valueOf(start)) + ":" + String.valueOf(end), flags, set);
    }

    public void storeFlags(int msg, Flags flags, boolean set) throws ProtocolException {
        storeFlags(String.valueOf(msg), flags, set);
    }

    private void storeFlags(String msgset, Flags flags, boolean set) throws ProtocolException {
        Response[] r;
        if (set) {
            r = command("STORE " + msgset + " +FLAGS " + createFlagList(flags), (Argument) null);
        } else {
            r = command("STORE " + msgset + " -FLAGS " + createFlagList(flags), (Argument) null);
        }
        notifyResponseHandlers(r);
        handleResult(r[r.length - 1]);
    }

    private String createFlagList(Flags flags) {
        String s;
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        Flags.Flag[] sf = flags.getSystemFlags();
        boolean first = true;
        for (Flags.Flag f : sf) {
            if (f == Flags.Flag.ANSWERED) {
                s = FlagNames.Answered;
            } else if (f == Flags.Flag.DELETED) {
                s = FlagNames.Deleted;
            } else if (f == Flags.Flag.DRAFT) {
                s = FlagNames.Draft;
            } else if (f == Flags.Flag.FLAGGED) {
                s = FlagNames.Flagged;
            } else if (f == Flags.Flag.RECENT) {
                s = FlagNames.Recent;
            } else if (f == Flags.Flag.SEEN) {
                s = FlagNames.Seen;
            }
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(s);
        }
        String[] uf = flags.getUserFlags();
        for (String append : uf) {
            if (first) {
                first = false;
            } else {
                sb.append(' ');
            }
            sb.append(append);
        }
        sb.append(")");
        return sb.toString();
    }

    public int[] search(MessageSet[] msgsets, SearchTerm term) throws ProtocolException, SearchException {
        return search(MessageSet.toString(msgsets), term);
    }

    public int[] search(SearchTerm term) throws ProtocolException, SearchException {
        return search("ALL", term);
    }

    private int[] search(String msgSequence, SearchTerm term) throws ProtocolException, SearchException {
        if (SearchSequence.isAscii(term)) {
            try {
                return issueSearch(msgSequence, term, (String) null);
            } catch (IOException e) {
            }
        }
        int i = 0;
        while (true) {
            String[] strArr = this.searchCharsets;
            if (i < strArr.length) {
                if (strArr[i] != null) {
                    try {
                        return issueSearch(msgSequence, term, strArr[i]);
                    } catch (CommandFailedException e2) {
                        this.searchCharsets[i] = null;
                    } catch (IOException e3) {
                    } catch (ProtocolException pex) {
                        throw pex;
                    } catch (SearchException sex) {
                        throw sex;
                    }
                }
                i++;
            } else {
                throw new SearchException("Search failed");
            }
        }
    }

    private int[] issueSearch(String msgSequence, SearchTerm term, String charset) throws ProtocolException, SearchException, IOException {
        String str;
        Response[] r;
        String str2 = charset;
        if (str2 == null) {
            str = null;
        } else {
            str = MimeUtility.javaCharset(charset);
        }
        Argument args = SearchSequence.generateSequence(term, str);
        args.writeAtom(msgSequence);
        if (str2 == null) {
            r = command("SEARCH", args);
        } else {
            r = command("SEARCH CHARSET " + str2, args);
        }
        Response response = r[r.length - 1];
        int[] matches = null;
        if (response.isOK()) {
            Vector v = new Vector();
            int len = r.length;
            for (int i = 0; i < len; i++) {
                if (r[i] instanceof IMAPResponse) {
                    IMAPResponse ir = (IMAPResponse) r[i];
                    if (ir.keyEquals("SEARCH")) {
                        while (true) {
                            int readNumber = ir.readNumber();
                            int num = readNumber;
                            if (readNumber == -1) {
                                break;
                            }
                            v.addElement(new Integer(num));
                        }
                        r[i] = null;
                    }
                }
            }
            int vsize = v.size();
            int[] matches2 = new int[vsize];
            for (int i2 = 0; i2 < vsize; i2++) {
                matches2[i2] = ((Integer) v.elementAt(i2)).intValue();
            }
            matches = matches2;
        }
        notifyResponseHandlers(r);
        handleResult(response);
        return matches;
    }

    public Namespaces namespace() throws ProtocolException {
        if (hasCapability("NAMESPACE")) {
            Response[] r = command("NAMESPACE", (Argument) null);
            Namespaces namespace = null;
            Response response = r[r.length - 1];
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("NAMESPACE")) {
                            if (namespace == null) {
                                namespace = new Namespaces(ir);
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            return namespace;
        }
        throw new BadCommandException("NAMESPACE not supported");
    }

    public Quota[] getQuotaRoot(String mbox) throws ProtocolException {
        if (hasCapability("QUOTA")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            Response[] r = command("GETQUOTAROOT", args);
            Response response = r[r.length - 1];
            Hashtable tab = new Hashtable();
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("QUOTAROOT")) {
                            ir.readAtomString();
                            while (true) {
                                String readAtomString = ir.readAtomString();
                                String root = readAtomString;
                                if (readAtomString == null) {
                                    break;
                                }
                                tab.put(root, new Quota(root));
                            }
                            r[i] = null;
                        } else if (ir.keyEquals("QUOTA")) {
                            Quota quota = parseQuota(ir);
                            Quota q = (Quota) tab.get(quota.quotaRoot);
                            if (q != null) {
                                Quota.Resource[] resourceArr = q.resources;
                            }
                            tab.put(quota.quotaRoot, quota);
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            Quota[] qa = new Quota[tab.size()];
            Enumeration e = tab.elements();
            int i2 = 0;
            while (e.hasMoreElements()) {
                qa[i2] = (Quota) e.nextElement();
                i2++;
            }
            return qa;
        }
        throw new BadCommandException("GETQUOTAROOT not supported");
    }

    public Quota[] getQuota(String root) throws ProtocolException {
        if (hasCapability("QUOTA")) {
            Argument args = new Argument();
            args.writeString(root);
            Response[] r = command("GETQUOTA", args);
            Vector v = new Vector();
            Response response = r[r.length - 1];
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("QUOTA")) {
                            v.addElement(parseQuota(ir));
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            Quota[] qa = new Quota[v.size()];
            v.copyInto(qa);
            return qa;
        }
        throw new BadCommandException("QUOTA not supported");
    }

    public void setQuota(Quota quota) throws ProtocolException {
        if (hasCapability("QUOTA")) {
            Argument args = new Argument();
            args.writeString(quota.quotaRoot);
            Argument qargs = new Argument();
            if (quota.resources != null) {
                for (int i = 0; i < quota.resources.length; i++) {
                    qargs.writeAtom(quota.resources[i].name);
                    qargs.writeNumber(quota.resources[i].limit);
                }
            }
            args.writeArgument(qargs);
            Response[] r = command("SETQUOTA", args);
            Response response = r[r.length - 1];
            notifyResponseHandlers(r);
            handleResult(response);
            return;
        }
        throw new BadCommandException("QUOTA not supported");
    }

    private Quota parseQuota(Response r) throws ParsingException {
        Quota q = new Quota(r.readAtomString());
        r.skipSpaces();
        if (r.readByte() == 40) {
            Vector v = new Vector();
            while (r.peekByte() != 41) {
                String name2 = r.readAtom();
                if (name2 != null) {
                    v.addElement(new Quota.Resource(name2, r.readLong(), r.readLong()));
                }
            }
            r.readByte();
            q.resources = new Quota.Resource[v.size()];
            v.copyInto(q.resources);
            return q;
        }
        throw new ParsingException("parse error in QUOTA");
    }

    public void setACL(String mbox, char modifier, ACL acl) throws ProtocolException {
        if (hasCapability("ACL")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            args.writeString(acl.getName());
            String rights = acl.getRights().toString();
            if (modifier == '+' || modifier == '-') {
                rights = String.valueOf(modifier) + rights;
            }
            args.writeString(rights);
            Response[] r = command("SETACL", args);
            Response response = r[r.length - 1];
            notifyResponseHandlers(r);
            handleResult(response);
            return;
        }
        throw new BadCommandException("ACL not supported");
    }

    public void deleteACL(String mbox, String user) throws ProtocolException {
        if (hasCapability("ACL")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            args.writeString(user);
            Response[] r = command("DELETEACL", args);
            Response response = r[r.length - 1];
            notifyResponseHandlers(r);
            handleResult(response);
            return;
        }
        throw new BadCommandException("ACL not supported");
    }

    public ACL[] getACL(String mbox) throws ProtocolException {
        String rights;
        if (hasCapability("ACL")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            Response[] r = command("GETACL", args);
            Response response = r[r.length - 1];
            Vector v = new Vector();
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("ACL")) {
                            ir.readAtomString();
                            while (true) {
                                String readAtomString = ir.readAtomString();
                                String name2 = readAtomString;
                                if (!(readAtomString == null || (rights = ir.readAtomString()) == null)) {
                                    v.addElement(new ACL(name2, new Rights(rights)));
                                }
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            ACL[] aa = new ACL[v.size()];
            v.copyInto(aa);
            return aa;
        }
        throw new BadCommandException("ACL not supported");
    }

    public Rights[] listRights(String mbox, String user) throws ProtocolException {
        if (hasCapability("ACL")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            args.writeString(user);
            Response[] r = command("LISTRIGHTS", args);
            Response response = r[r.length - 1];
            Vector v = new Vector();
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("LISTRIGHTS")) {
                            ir.readAtomString();
                            ir.readAtomString();
                            while (true) {
                                String readAtomString = ir.readAtomString();
                                String rights = readAtomString;
                                if (readAtomString == null) {
                                    break;
                                }
                                v.addElement(new Rights(rights));
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            Rights[] ra = new Rights[v.size()];
            v.copyInto(ra);
            return ra;
        }
        throw new BadCommandException("ACL not supported");
    }

    public Rights myRights(String mbox) throws ProtocolException {
        if (hasCapability("ACL")) {
            String mbox2 = BASE64MailboxEncoder.encode(mbox);
            Argument args = new Argument();
            args.writeString(mbox2);
            Response[] r = command("MYRIGHTS", args);
            Response response = r[r.length - 1];
            Rights rights = null;
            if (response.isOK()) {
                int len = r.length;
                for (int i = 0; i < len; i++) {
                    if (r[i] instanceof IMAPResponse) {
                        IMAPResponse ir = (IMAPResponse) r[i];
                        if (ir.keyEquals("MYRIGHTS")) {
                            ir.readAtomString();
                            String rs = ir.readAtomString();
                            if (rights == null) {
                                rights = new Rights(rs);
                            }
                            r[i] = null;
                        }
                    }
                }
            }
            notifyResponseHandlers(r);
            handleResult(response);
            return rights;
        }
        throw new BadCommandException("ACL not supported");
    }

    public synchronized void idleStart() throws ProtocolException {
        Response r;
        if (hasCapability("IDLE")) {
            try {
                this.idleTag = writeCommand("IDLE", (Argument) null);
                r = readResponse();
            } catch (LiteralException lex) {
                r = lex.getResponse();
            } catch (Exception ex) {
                r = Response.byeResponse(ex);
            }
            if (!r.isContinuation()) {
                handleResult(r);
            }
        } else {
            throw new BadCommandException("IDLE not supported");
        }
    }

    public synchronized Response readIdleResponse() {
        Response r;
        if (this.idleTag == null) {
            return null;
        }
        try {
            r = readResponse();
        } catch (IOException ioex) {
            r = Response.byeResponse(ioex);
        } catch (ProtocolException pex) {
            r = Response.byeResponse(pex);
        }
        return r;
    }

    public boolean processIdleResponse(Response r) throws ProtocolException {
        boolean done = false;
        notifyResponseHandlers(new Response[]{r});
        if (r.isBYE()) {
            done = true;
        }
        if (r.isTagged() && r.getTag().equals(this.idleTag)) {
            done = true;
        }
        if (done) {
            this.idleTag = null;
        }
        handleResult(r);
        return !done;
    }

    public void idleAbort() throws ProtocolException {
        OutputStream os = getOutputStream();
        try {
            os.write(DONE);
            os.flush();
        } catch (IOException e) {
        }
    }
}
