package com.sun.mail.smtp;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.AttributeNames;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sun.mail.util.ASCIIUtility;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.SocketFetcher;
import com.sun.mail.util.TraceInputStream;
import com.sun.mail.util.TraceOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.ParseException;
import org.xbill.DNS.Type;

public class SMTPTransport extends Transport {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final byte[] CRLF = {13, 10};
    private static final String UNKNOWN = "UNKNOWN";
    private static char[] hexchar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String[] ignoreList = {AttributeNames.bcc, "Content-Length"};
    private Address[] addresses;
    private SMTPOutputStream dataStream;
    private int defaultPort;
    private MessagingException exception;
    private Hashtable extMap;
    private Address[] invalidAddr;
    private boolean isSSL;
    private int lastReturnCode;
    private String lastServerResponse;
    private LineInputStream lineInputStream;
    private String localHostName;
    private DigestMD5 md5support;
    private MimeMessage message;
    private String name;
    private PrintStream out;
    private boolean quitWait;
    private boolean reportSuccess;
    private String saslRealm;
    private boolean sendPartiallyFailed;
    private BufferedInputStream serverInput;
    private OutputStream serverOutput;
    private Socket serverSocket;
    private boolean useRset;
    private boolean useStartTLS;
    private Address[] validSentAddr;
    private Address[] validUnsentAddr;

    public SMTPTransport(Session session, URLName urlname) {
        this(session, urlname, "smtp", 25, false);
    }

    protected SMTPTransport(Session session, URLName urlname, String name2, int defaultPort2, boolean isSSL2) {
        super(session, urlname);
        this.name = "smtp";
        this.defaultPort = 25;
        boolean z = false;
        this.isSSL = false;
        this.sendPartiallyFailed = false;
        this.quitWait = false;
        this.saslRealm = "UNKNOWN";
        name2 = urlname != null ? urlname.getProtocol() : name2;
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        this.out = session.getDebugOut();
        String s = session.getProperty("mail." + name2 + ".quitwait");
        this.quitWait = s == null || s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
        String s2 = session.getProperty("mail." + name2 + ".reportsuccess");
        this.reportSuccess = s2 != null && s2.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
        String s3 = session.getProperty("mail." + name2 + ".starttls.enable");
        this.useStartTLS = s3 != null && s3.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
        String s4 = session.getProperty("mail." + name2 + ".userset");
        if (s4 != null && s4.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            z = true;
        }
        this.useRset = z;
    }

    public synchronized String getLocalHost() {
        try {
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                Session session = this.session;
                this.localHostName = session.getProperty("mail." + this.name + ".localhost");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                Session session2 = this.session;
                this.localHostName = session2.getProperty("mail." + this.name + ".localaddress");
            }
            if (this.localHostName == null || this.localHostName.length() <= 0) {
                InetAddress localHost = InetAddress.getLocalHost();
                String hostName = localHost.getHostName();
                this.localHostName = hostName;
                if (hostName == null) {
                    this.localHostName = "[" + localHost.getHostAddress() + "]";
                }
            }
        } catch (UnknownHostException e) {
        }
        return this.localHostName;
    }

    public synchronized void setLocalHost(String localhost) {
        this.localHostName = localhost;
    }

    public synchronized void connect(Socket socket) throws MessagingException {
        this.serverSocket = socket;
        super.connect();
    }

    public synchronized String getSASLRealm() {
        if (this.saslRealm == "UNKNOWN") {
            Session session = this.session;
            String property = session.getProperty("mail." + this.name + ".sasl.realm");
            this.saslRealm = property;
            if (property == null) {
                Session session2 = this.session;
                this.saslRealm = session2.getProperty("mail." + this.name + ".saslrealm");
            }
        }
        return this.saslRealm;
    }

    public synchronized void setSASLRealm(String saslRealm2) {
        this.saslRealm = saslRealm2;
    }

    public synchronized boolean getReportSuccess() {
        return this.reportSuccess;
    }

    public synchronized void setReportSuccess(boolean reportSuccess2) {
        this.reportSuccess = reportSuccess2;
    }

    public synchronized boolean getStartTLS() {
        return this.useStartTLS;
    }

    public synchronized void setStartTLS(boolean useStartTLS2) {
        this.useStartTLS = useStartTLS2;
    }

    public synchronized boolean getUseRset() {
        return this.useRset;
    }

    public synchronized void setUseRset(boolean useRset2) {
        this.useRset = useRset2;
    }

    public synchronized String getLastServerResponse() {
        return this.lastServerResponse;
    }

    public synchronized int getLastReturnCode() {
        return this.lastReturnCode;
    }

    private synchronized DigestMD5 getMD5() {
        if (this.md5support == null) {
            this.md5support = new DigestMD5(this.debug ? this.out : null);
        }
        return this.md5support;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x01f0 A[Catch:{ all -> 0x020b }] */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0211  */
    /* JADX WARNING: Removed duplicated region for block: B:148:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:149:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean protocolConnect(java.lang.String r20, int r21, java.lang.String r22, java.lang.String r23) throws javax.mail.MessagingException {
        /*
            r19 = this;
            r1 = r19
            javax.mail.Session r0 = r1.session
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "mail."
            r2.<init>(r3)
            java.lang.String r4 = r1.name
            r2.append(r4)
            java.lang.String r4 = ".ehlo"
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            java.lang.String r8 = r0.getProperty(r2)
            r10 = 0
            if (r8 == 0) goto L_0x002a
            java.lang.String r0 = "false"
            boolean r0 = r8.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x002a
            r0 = r10
            goto L_0x002b
        L_0x002a:
            r0 = 1
        L_0x002b:
            r11 = r0
            javax.mail.Session r0 = r1.session
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>(r3)
            java.lang.String r4 = r1.name
            r2.append(r4)
            java.lang.String r4 = ".auth"
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            java.lang.String r12 = r0.getProperty(r2)
            if (r12 == 0) goto L_0x0052
            java.lang.String r0 = "true"
            boolean r0 = r12.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x0052
            r0 = 1
            goto L_0x0053
        L_0x0052:
            r0 = r10
        L_0x0053:
            r13 = r0
            boolean r0 = r1.debug
            if (r0 == 0) goto L_0x0073
            java.io.PrintStream r0 = r1.out
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r4 = "DEBUG SMTP: useEhlo "
            r2.<init>(r4)
            r2.append(r11)
            java.lang.String r4 = ", useAuth "
            r2.append(r4)
            r2.append(r13)
            java.lang.String r2 = r2.toString()
            r0.println(r2)
        L_0x0073:
            if (r13 == 0) goto L_0x007a
            if (r22 == 0) goto L_0x0079
            if (r23 != 0) goto L_0x007a
        L_0x0079:
            return r10
        L_0x007a:
            r0 = -1
            r2 = r21
            if (r2 != r0) goto L_0x00a4
            javax.mail.Session r0 = r1.session
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r3)
            java.lang.String r3 = r1.name
            r4.append(r3)
            java.lang.String r3 = ".port"
            r4.append(r3)
            java.lang.String r3 = r4.toString()
            java.lang.String r0 = r0.getProperty(r3)
            if (r0 == 0) goto L_0x00a0
            int r2 = java.lang.Integer.parseInt(r0)
            r14 = r2
            goto L_0x00a5
        L_0x00a0:
            int r2 = r1.defaultPort
            r14 = r2
            goto L_0x00a5
        L_0x00a4:
            r14 = r2
        L_0x00a5:
            if (r20 == 0) goto L_0x00b1
            int r0 = r20.length()
            if (r0 != 0) goto L_0x00ae
            goto L_0x00b1
        L_0x00ae:
            r15 = r20
            goto L_0x00b4
        L_0x00b1:
            java.lang.String r0 = "localhost"
            r15 = r0
        L_0x00b4:
            r0 = 0
            java.net.Socket r2 = r1.serverSocket
            if (r2 == 0) goto L_0x00bd
            r19.openServer()
            goto L_0x00c0
        L_0x00bd:
            r1.openServer(r15, r14)
        L_0x00c0:
            if (r11 == 0) goto L_0x00cd
            java.lang.String r2 = r19.getLocalHost()
            boolean r0 = r1.ehlo(r2)
            r16 = r0
            goto L_0x00cf
        L_0x00cd:
            r16 = r0
        L_0x00cf:
            if (r16 != 0) goto L_0x00d8
            java.lang.String r0 = r19.getLocalHost()
            r1.helo(r0)
        L_0x00d8:
            boolean r0 = r1.useStartTLS
            if (r0 == 0) goto L_0x00ee
            java.lang.String r0 = "STARTTLS"
            boolean r0 = r1.supportsExtension(r0)
            if (r0 == 0) goto L_0x00ee
            r19.startTLS()
            java.lang.String r0 = r19.getLocalHost()
            r1.ehlo(r0)
        L_0x00ee:
            if (r13 != 0) goto L_0x00f4
            if (r22 == 0) goto L_0x0278
            if (r23 == 0) goto L_0x0278
        L_0x00f4:
            java.lang.String r0 = "AUTH"
            boolean r0 = r1.supportsExtension(r0)
            java.lang.String r2 = "AUTH=LOGIN"
            if (r0 != 0) goto L_0x0104
            boolean r0 = r1.supportsExtension(r2)
            if (r0 == 0) goto L_0x0278
        L_0x0104:
            boolean r0 = r1.debug
            java.lang.String r3 = "LOGIN"
            if (r0 == 0) goto L_0x0124
            java.io.PrintStream r0 = r1.out
            java.lang.String r4 = "DEBUG SMTP: Attempt to authenticate"
            r0.println(r4)
            boolean r0 = r1.supportsAuthentication(r3)
            if (r0 != 0) goto L_0x0124
            boolean r0 = r1.supportsExtension(r2)
            if (r0 == 0) goto L_0x0124
            java.io.PrintStream r0 = r1.out
            java.lang.String r4 = "DEBUG SMTP: use AUTH=LOGIN hack"
            r0.println(r4)
        L_0x0124:
            boolean r0 = r1.supportsAuthentication(r3)
            r3 = 2147483647(0x7fffffff, float:NaN)
            r7 = 334(0x14e, float:4.68E-43)
            r6 = 235(0xeb, float:3.3E-43)
            if (r0 != 0) goto L_0x021b
            boolean r0 = r1.supportsExtension(r2)
            if (r0 == 0) goto L_0x013b
            r2 = r6
            r0 = r7
            goto L_0x021d
        L_0x013b:
            java.lang.String r0 = "PLAIN"
            boolean r0 = r1.supportsAuthentication(r0)
            if (r0 == 0) goto L_0x0185
            java.lang.String r0 = "AUTH PLAIN"
            int r2 = r1.simpleCommand((java.lang.String) r0)
            java.io.ByteArrayOutputStream r0 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r0.<init>()     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            com.sun.mail.util.BASE64EncoderStream r4 = new com.sun.mail.util.BASE64EncoderStream     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r4.<init>(r0, r3)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r3 = r4
            if (r2 != r7) goto L_0x0176
            r3.write(r10)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            byte[] r4 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r22)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r3.write(r4)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r3.write(r10)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            byte[] r4 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r23)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r3.write(r4)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r3.flush()     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            byte[] r4 = r0.toByteArray()     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            int r4 = r1.simpleCommand((byte[]) r4)     // Catch:{ IOException -> 0x0181, all -> 0x017c }
            r2 = r4
        L_0x0176:
            if (r2 == r6) goto L_0x0278
        L_0x0178:
            r19.closeConnection()
            return r10
        L_0x017c:
            r0 = move-exception
            if (r2 == r6) goto L_0x0180
            goto L_0x0178
        L_0x0180:
            throw r0
        L_0x0181:
            r0 = move-exception
            if (r2 == r6) goto L_0x0278
            goto L_0x0178
        L_0x0185:
            java.lang.String r0 = "DIGEST-MD5"
            boolean r0 = r1.supportsAuthentication(r0)
            if (r0 == 0) goto L_0x0278
            com.sun.mail.smtp.DigestMD5 r0 = r19.getMD5()
            r5 = r0
            if (r0 == 0) goto L_0x0219
            java.lang.String r0 = "AUTH DIGEST-MD5"
            int r4 = r1.simpleCommand((java.lang.String) r0)
            if (r4 != r7) goto L_0x0212
            java.lang.String r0 = r19.getSASLRealm()     // Catch:{ Exception -> 0x01e8, all -> 0x01e2 }
            java.lang.String r3 = r1.lastServerResponse     // Catch:{ Exception -> 0x01e8, all -> 0x01e2 }
            r2 = r5
            r17 = r3
            r3 = r15
            r18 = r4
            r4 = r22
            r9 = r5
            r5 = r23
            r6 = r0
            r0 = r7
            r7 = r17
            byte[] r2 = r2.authClient(r3, r4, r5, r6, r7)     // Catch:{ Exception -> 0x01de, all -> 0x01d8 }
            int r3 = r1.simpleCommand((byte[]) r2)     // Catch:{ Exception -> 0x01de, all -> 0x01d8 }
            r4 = r3
            if (r4 != r0) goto L_0x01d5
            java.lang.String r0 = r1.lastServerResponse     // Catch:{ Exception -> 0x01d3 }
            boolean r0 = r9.authServer(r0)     // Catch:{ Exception -> 0x01d3 }
            if (r0 != 0) goto L_0x01c9
            r4 = -1
            r2 = 235(0xeb, float:3.3E-43)
            goto L_0x0216
        L_0x01c9:
            byte[] r0 = new byte[r10]     // Catch:{ Exception -> 0x01d3 }
            int r0 = r1.simpleCommand((byte[]) r0)     // Catch:{ Exception -> 0x01d3 }
            r4 = r0
            r2 = 235(0xeb, float:3.3E-43)
            goto L_0x0216
        L_0x01d3:
            r0 = move-exception
            goto L_0x01ec
        L_0x01d5:
            r2 = 235(0xeb, float:3.3E-43)
            goto L_0x0216
        L_0x01d8:
            r0 = move-exception
            r4 = r18
            r2 = 235(0xeb, float:3.3E-43)
            goto L_0x020e
        L_0x01de:
            r0 = move-exception
            r4 = r18
            goto L_0x01ec
        L_0x01e2:
            r0 = move-exception
            r18 = r4
            r9 = r5
            r2 = r6
            goto L_0x020e
        L_0x01e8:
            r0 = move-exception
            r18 = r4
            r9 = r5
        L_0x01ec:
            boolean r2 = r1.debug     // Catch:{ all -> 0x020b }
            if (r2 == 0) goto L_0x0203
            java.io.PrintStream r2 = r1.out     // Catch:{ all -> 0x020b }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x020b }
            java.lang.String r5 = "DEBUG SMTP: DIGEST-MD5: "
            r3.<init>(r5)     // Catch:{ all -> 0x020b }
            r3.append(r0)     // Catch:{ all -> 0x020b }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x020b }
            r2.println(r3)     // Catch:{ all -> 0x020b }
        L_0x0203:
            r2 = 235(0xeb, float:3.3E-43)
            if (r4 == r2) goto L_0x0278
        L_0x0207:
            r19.closeConnection()
            return r10
        L_0x020b:
            r0 = move-exception
            r2 = 235(0xeb, float:3.3E-43)
        L_0x020e:
            if (r4 == r2) goto L_0x0211
            goto L_0x0207
        L_0x0211:
            throw r0
        L_0x0212:
            r18 = r4
            r9 = r5
            r2 = r6
        L_0x0216:
            if (r4 == r2) goto L_0x0278
            goto L_0x0207
        L_0x0219:
            r9 = r5
            goto L_0x0278
        L_0x021b:
            r2 = r6
            r0 = r7
        L_0x021d:
            java.lang.String r4 = "AUTH LOGIN"
            int r5 = r1.simpleCommand((java.lang.String) r4)
            r6 = 530(0x212, float:7.43E-43)
            if (r5 != r6) goto L_0x022e
            r19.startTLS()
            int r5 = r1.simpleCommand((java.lang.String) r4)
        L_0x022e:
            java.io.ByteArrayOutputStream r4 = new java.io.ByteArrayOutputStream     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r4.<init>()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            com.sun.mail.util.BASE64EncoderStream r6 = new com.sun.mail.util.BASE64EncoderStream     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r6.<init>(r4, r3)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r3 = r6
            if (r5 != r0) goto L_0x0251
            byte[] r6 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r22)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r3.write(r6)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r3.flush()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            byte[] r6 = r4.toByteArray()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            int r6 = r1.simpleCommand((byte[]) r6)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r5 = r6
            r4.reset()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
        L_0x0251:
            if (r5 != r0) goto L_0x0269
            byte[] r0 = com.sun.mail.util.ASCIIUtility.getBytes((java.lang.String) r23)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r3.write(r0)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r3.flush()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            byte[] r0 = r4.toByteArray()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            int r0 = r1.simpleCommand((byte[]) r0)     // Catch:{ IOException -> 0x0274, all -> 0x026f }
            r5 = r0
            r4.reset()     // Catch:{ IOException -> 0x0274, all -> 0x026f }
        L_0x0269:
            if (r5 == r2) goto L_0x0278
        L_0x026b:
            r19.closeConnection()
            return r10
        L_0x026f:
            r0 = move-exception
            if (r5 == r2) goto L_0x0273
            goto L_0x026b
        L_0x0273:
            throw r0
        L_0x0274:
            r0 = move-exception
            if (r5 == r2) goto L_0x0278
            goto L_0x026b
        L_0x0278:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.protocolConnect(java.lang.String, int, java.lang.String, java.lang.String):boolean");
    }

    /* Debug info: failed to restart local var, previous not found, register: 21 */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00f1, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0111, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0113, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0115, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0116, code lost:
        r13 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0119, code lost:
        if (r7.debug != false) goto L_0x011b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:?, code lost:
        r13.printStackTrace(r7.out);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:?, code lost:
        closeConnection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:?, code lost:
        notifyTransportListeners(2, r7.validSentAddr, r7.validUnsentAddr, r7.invalidAddr, r7.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x013b, code lost:
        throw new javax.mail.MessagingException("IOException while sending message", r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x013c, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x013f, code lost:
        if (r7.debug != false) goto L_0x0141;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0141, code lost:
        r0.printStackTrace(r7.out);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0146, code lost:
        notifyTransportListeners(2, r7.validSentAddr, r7.validUnsentAddr, r7.invalidAddr, r7.message);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0155, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0156, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:?, code lost:
        r7.invalidAddr = null;
        r7.validUnsentAddr = null;
        r7.validSentAddr = null;
        r7.addresses = null;
        r7.message = null;
        r7.exception = null;
        r7.sendPartiallyFailed = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0165, code lost:
        throw r0;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0115 A[ExcHandler: IOException (e java.io.IOException), Splitter:B:38:0x00a0] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x011b A[SYNTHETIC, Splitter:B:67:0x011b] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x013c A[Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1, all -> 0x0156 }, ExcHandler: MessagingException (e javax.mail.MessagingException), Splitter:B:38:0x00a0] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0141 A[Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1, all -> 0x0156 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void sendMessage(javax.mail.Message r22, javax.mail.Address[] r23) throws javax.mail.MessagingException, javax.mail.SendFailedException {
        /*
            r21 = this;
            r7 = r21
            r8 = r22
            r9 = r23
            monitor-enter(r21)
            r21.checkConnected()     // Catch:{ all -> 0x0189 }
            boolean r0 = r8 instanceof javax.mail.internet.MimeMessage     // Catch:{ all -> 0x0189 }
            if (r0 != 0) goto L_0x0021
            boolean r0 = r7.debug     // Catch:{ all -> 0x0189 }
            if (r0 == 0) goto L_0x0019
            java.io.PrintStream r0 = r7.out     // Catch:{ all -> 0x0189 }
            java.lang.String r1 = "DEBUG SMTP: Can only send RFC822 msgs"
            r0.println(r1)     // Catch:{ all -> 0x0189 }
        L_0x0019:
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ all -> 0x0189 }
            java.lang.String r1 = "SMTP can only send RFC822 messages"
            r0.<init>(r1)     // Catch:{ all -> 0x0189 }
            throw r0     // Catch:{ all -> 0x0189 }
        L_0x0021:
            r0 = 0
        L_0x0022:
            int r1 = r9.length     // Catch:{ all -> 0x0189 }
            if (r0 < r1) goto L_0x0166
            r0 = r8
            javax.mail.internet.MimeMessage r0 = (javax.mail.internet.MimeMessage) r0     // Catch:{ all -> 0x0189 }
            r7.message = r0     // Catch:{ all -> 0x0189 }
            r7.addresses = r9     // Catch:{ all -> 0x0189 }
            r7.validUnsentAddr = r9     // Catch:{ all -> 0x0189 }
            r21.expandGroups()     // Catch:{ all -> 0x0189 }
            r0 = 0
            boolean r1 = r8 instanceof com.sun.mail.smtp.SMTPMessage     // Catch:{ all -> 0x0189 }
            if (r1 == 0) goto L_0x003e
            r1 = r8
            com.sun.mail.smtp.SMTPMessage r1 = (com.sun.mail.smtp.SMTPMessage) r1     // Catch:{ all -> 0x0189 }
            boolean r1 = r1.getAllow8bitMIME()     // Catch:{ all -> 0x0189 }
            r0 = r1
        L_0x003e:
            r10 = 0
            if (r0 != 0) goto L_0x006e
            javax.mail.Session r1 = r7.session     // Catch:{ all -> 0x0189 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0189 }
            java.lang.String r3 = "mail."
            r2.<init>(r3)     // Catch:{ all -> 0x0189 }
            java.lang.String r3 = r7.name     // Catch:{ all -> 0x0189 }
            r2.append(r3)     // Catch:{ all -> 0x0189 }
            java.lang.String r3 = ".allow8bitmime"
            r2.append(r3)     // Catch:{ all -> 0x0189 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0189 }
            java.lang.String r1 = r1.getProperty(r2)     // Catch:{ all -> 0x0189 }
            if (r1 == 0) goto L_0x006a
            java.lang.String r2 = "true"
            boolean r2 = r1.equalsIgnoreCase(r2)     // Catch:{ all -> 0x0189 }
            if (r2 == 0) goto L_0x006a
            r2 = 1
            goto L_0x006b
        L_0x006a:
            r2 = r10
        L_0x006b:
            r0 = r2
            r11 = r0
            goto L_0x006f
        L_0x006e:
            r11 = r0
        L_0x006f:
            boolean r0 = r7.debug     // Catch:{ all -> 0x0189 }
            if (r0 == 0) goto L_0x0086
            java.io.PrintStream r0 = r7.out     // Catch:{ all -> 0x0189 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0189 }
            java.lang.String r2 = "DEBUG SMTP: use8bit "
            r1.<init>(r2)     // Catch:{ all -> 0x0189 }
            r1.append(r11)     // Catch:{ all -> 0x0189 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0189 }
            r0.println(r1)     // Catch:{ all -> 0x0189 }
        L_0x0086:
            if (r11 == 0) goto L_0x009f
            java.lang.String r0 = "8BITMIME"
            boolean r0 = r7.supportsExtension(r0)     // Catch:{ all -> 0x0189 }
            if (r0 == 0) goto L_0x009f
            javax.mail.internet.MimeMessage r0 = r7.message     // Catch:{ all -> 0x0189 }
            boolean r0 = r7.convertTo8Bit(r0)     // Catch:{ all -> 0x0189 }
            if (r0 == 0) goto L_0x009f
            javax.mail.internet.MimeMessage r0 = r7.message     // Catch:{ MessagingException -> 0x009e }
            r0.saveChanges()     // Catch:{ MessagingException -> 0x009e }
            goto L_0x009f
        L_0x009e:
            r0 = move-exception
        L_0x009f:
            r12 = 0
            r21.mailFrom()     // Catch:{ MessagingException -> 0x013c, IOException -> 0x0115 }
            r21.rcptTo()     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            javax.mail.internet.MimeMessage r0 = r7.message     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            java.io.OutputStream r1 = r21.data()     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            java.lang.String[] r2 = ignoreList     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            r0.writeTo(r1, r2)     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            r21.finishData()     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            boolean r0 = r7.sendPartiallyFailed     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            if (r0 == 0) goto L_0x00f3
            boolean r0 = r7.debug     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            if (r0 == 0) goto L_0x00c3
            java.io.PrintStream r0 = r7.out     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            java.lang.String r1 = "DEBUG SMTP: Sending partially failed because of invalid destination addresses"
            r0.println(r1)     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
        L_0x00c3:
            r2 = 3
            javax.mail.Address[] r3 = r7.validSentAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.Address[] r4 = r7.validUnsentAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.Address[] r5 = r7.invalidAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.internet.MimeMessage r6 = r7.message     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            r1 = r21
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            com.sun.mail.smtp.SMTPSendFailedException r0 = new com.sun.mail.smtp.SMTPSendFailedException     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            java.lang.String r14 = "."
            int r15 = r7.lastReturnCode     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            java.lang.String r1 = r7.lastServerResponse     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.MessagingException r2 = r7.exception     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.Address[] r3 = r7.validSentAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.Address[] r4 = r7.validUnsentAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            javax.mail.Address[] r5 = r7.invalidAddr     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            r13 = r0
            r16 = r1
            r17 = r2
            r18 = r3
            r19 = r4
            r20 = r5
            r13.<init>(r14, r15, r16, r17, r18, r19, r20)     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
            throw r0     // Catch:{ MessagingException -> 0x013c, IOException -> 0x00f1 }
        L_0x00f1:
            r0 = move-exception
            goto L_0x0116
        L_0x00f3:
            r2 = 1
            javax.mail.Address[] r3 = r7.validSentAddr     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            javax.mail.Address[] r4 = r7.validUnsentAddr     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            javax.mail.Address[] r5 = r7.invalidAddr     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            javax.mail.internet.MimeMessage r6 = r7.message     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            r1 = r21
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ MessagingException -> 0x0111, IOException -> 0x0115 }
            r7.invalidAddr = r12     // Catch:{ all -> 0x0189 }
            r7.validUnsentAddr = r12     // Catch:{ all -> 0x0189 }
            r7.validSentAddr = r12     // Catch:{ all -> 0x0189 }
            r7.addresses = r12     // Catch:{ all -> 0x0189 }
            r7.message = r12     // Catch:{ all -> 0x0189 }
            r7.exception = r12     // Catch:{ all -> 0x0189 }
            r7.sendPartiallyFailed = r10     // Catch:{ all -> 0x0189 }
            monitor-exit(r21)
            return
        L_0x0111:
            r0 = move-exception
            goto L_0x013d
        L_0x0113:
            r0 = move-exception
            goto L_0x0157
        L_0x0115:
            r0 = move-exception
        L_0x0116:
            r13 = r0
            boolean r0 = r7.debug     // Catch:{ all -> 0x0156 }
            if (r0 == 0) goto L_0x0120
            java.io.PrintStream r0 = r7.out     // Catch:{ all -> 0x0113 }
            r13.printStackTrace(r0)     // Catch:{ all -> 0x0113 }
        L_0x0120:
            r21.closeConnection()     // Catch:{ MessagingException -> 0x0124 }
            goto L_0x0125
        L_0x0124:
            r0 = move-exception
        L_0x0125:
            r2 = 2
            javax.mail.Address[] r3 = r7.validSentAddr     // Catch:{ all -> 0x0156 }
            javax.mail.Address[] r4 = r7.validUnsentAddr     // Catch:{ all -> 0x0156 }
            javax.mail.Address[] r5 = r7.invalidAddr     // Catch:{ all -> 0x0156 }
            javax.mail.internet.MimeMessage r6 = r7.message     // Catch:{ all -> 0x0156 }
            r1 = r21
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0156 }
            javax.mail.MessagingException r0 = new javax.mail.MessagingException     // Catch:{ all -> 0x0156 }
            java.lang.String r1 = "IOException while sending message"
            r0.<init>(r1, r13)     // Catch:{ all -> 0x0156 }
            throw r0     // Catch:{ all -> 0x0156 }
        L_0x013c:
            r0 = move-exception
        L_0x013d:
            boolean r1 = r7.debug     // Catch:{ all -> 0x0156 }
            if (r1 == 0) goto L_0x0146
            java.io.PrintStream r1 = r7.out     // Catch:{ all -> 0x0156 }
            r0.printStackTrace(r1)     // Catch:{ all -> 0x0156 }
        L_0x0146:
            r2 = 2
            javax.mail.Address[] r3 = r7.validSentAddr     // Catch:{ all -> 0x0156 }
            javax.mail.Address[] r4 = r7.validUnsentAddr     // Catch:{ all -> 0x0156 }
            javax.mail.Address[] r5 = r7.invalidAddr     // Catch:{ all -> 0x0156 }
            javax.mail.internet.MimeMessage r6 = r7.message     // Catch:{ all -> 0x0156 }
            r1 = r21
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0156 }
            throw r0     // Catch:{ all -> 0x0156 }
        L_0x0156:
            r0 = move-exception
        L_0x0157:
            r7.invalidAddr = r12     // Catch:{ all -> 0x0189 }
            r7.validUnsentAddr = r12     // Catch:{ all -> 0x0189 }
            r7.validSentAddr = r12     // Catch:{ all -> 0x0189 }
            r7.addresses = r12     // Catch:{ all -> 0x0189 }
            r7.message = r12     // Catch:{ all -> 0x0189 }
            r7.exception = r12     // Catch:{ all -> 0x0189 }
            r7.sendPartiallyFailed = r10     // Catch:{ all -> 0x0189 }
            throw r0     // Catch:{ all -> 0x0189 }
        L_0x0166:
            r1 = r9[r0]     // Catch:{ all -> 0x0189 }
            boolean r1 = r1 instanceof javax.mail.internet.InternetAddress     // Catch:{ all -> 0x0189 }
            if (r1 == 0) goto L_0x0170
            int r0 = r0 + 1
            goto L_0x0022
        L_0x0170:
            javax.mail.MessagingException r1 = new javax.mail.MessagingException     // Catch:{ all -> 0x0189 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0189 }
            r2.<init>()     // Catch:{ all -> 0x0189 }
            r3 = r9[r0]     // Catch:{ all -> 0x0189 }
            r2.append(r3)     // Catch:{ all -> 0x0189 }
            java.lang.String r3 = " is not an InternetAddress"
            r2.append(r3)     // Catch:{ all -> 0x0189 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0189 }
            r1.<init>(r2)     // Catch:{ all -> 0x0189 }
            throw r1     // Catch:{ all -> 0x0189 }
        L_0x0189:
            r0 = move-exception
            monitor-exit(r21)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.sendMessage(javax.mail.Message, javax.mail.Address[]):void");
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:19:0x0037=Splitter:B:19:0x0037, B:24:0x003d=Splitter:B:24:0x003d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close() throws javax.mail.MessagingException {
        /*
            r4 = this;
            monitor-enter(r4)
            boolean r0 = super.isConnected()     // Catch:{ all -> 0x0041 }
            if (r0 != 0) goto L_0x0009
            monitor-exit(r4)
            return
        L_0x0009:
            java.net.Socket r0 = r4.serverSocket     // Catch:{ all -> 0x003c }
            if (r0 == 0) goto L_0x0037
            java.lang.String r0 = "QUIT"
            r4.sendCommand((java.lang.String) r0)     // Catch:{ all -> 0x0035 }
            boolean r0 = r4.quitWait     // Catch:{ all -> 0x0035 }
            if (r0 == 0) goto L_0x0037
            int r0 = r4.readServerResponse()     // Catch:{ all -> 0x0035 }
            r1 = 221(0xdd, float:3.1E-43)
            if (r0 == r1) goto L_0x0037
            r1 = -1
            if (r0 == r1) goto L_0x0037
            java.io.PrintStream r1 = r4.out     // Catch:{ all -> 0x0035 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0035 }
            java.lang.String r3 = "DEBUG SMTP: QUIT failed with "
            r2.<init>(r3)     // Catch:{ all -> 0x0035 }
            r2.append(r0)     // Catch:{ all -> 0x0035 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0035 }
            r1.println(r2)     // Catch:{ all -> 0x0035 }
            goto L_0x0037
        L_0x0035:
            r0 = move-exception
            goto L_0x003d
        L_0x0037:
            r4.closeConnection()     // Catch:{ all -> 0x0041 }
            monitor-exit(r4)
            return
        L_0x003c:
            r0 = move-exception
        L_0x003d:
            r4.closeConnection()     // Catch:{ all -> 0x0041 }
            throw r0     // Catch:{ all -> 0x0041 }
        L_0x0041:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.close():void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    private void closeConnection() throws MessagingException {
        try {
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
        } catch (IOException ioex) {
            throw new MessagingException("Server Close Failed", ioex);
        } catch (Throwable th) {
            this.serverSocket = null;
            this.serverOutput = null;
            this.serverInput = null;
            this.lineInputStream = null;
            if (super.isConnected()) {
                super.close();
            }
            throw th;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 3 */
    public synchronized boolean isConnected() {
        if (!super.isConnected()) {
            return false;
        }
        try {
            if (this.useRset) {
                sendCommand("RSET");
            } else {
                sendCommand("NOOP");
            }
            int resp = readServerResponse();
            if (resp >= 0 && resp != 421) {
                return true;
            }
            try {
                closeConnection();
            } catch (MessagingException e) {
            }
        } catch (Exception e2) {
            try {
                closeConnection();
            } catch (MessagingException e3) {
            }
            return false;
        }
        return false;
    }

    private void expandGroups() {
        Vector groups = null;
        int i = 0;
        while (true) {
            Address[] addressArr = this.addresses;
            if (i >= addressArr.length) {
                break;
            }
            InternetAddress a = (InternetAddress) addressArr[i];
            if (a.isGroup()) {
                if (groups == null) {
                    Vector groups2 = new Vector();
                    for (int k = 0; k < i; k++) {
                        groups2.addElement(this.addresses[k]);
                    }
                    groups = groups2;
                }
                try {
                    InternetAddress[] ia = a.getGroup(true);
                    if (ia != null) {
                        for (InternetAddress addElement : ia) {
                            groups.addElement(addElement);
                        }
                    } else {
                        groups.addElement(a);
                    }
                } catch (ParseException e) {
                    groups.addElement(a);
                }
            } else if (groups != null) {
                groups.addElement(a);
            }
            i++;
        }
        if (groups != null) {
            InternetAddress[] newa = new InternetAddress[groups.size()];
            groups.copyInto(newa);
            this.addresses = newa;
        }
    }

    private boolean convertTo8Bit(MimePart part) {
        boolean changed = false;
        try {
            if (part.isMimeType("text/*")) {
                String enc = part.getEncoding();
                if (enc == null) {
                    return false;
                }
                if ((!enc.equalsIgnoreCase("quoted-printable") && !enc.equalsIgnoreCase(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64)) || !is8Bit(part.getInputStream())) {
                    return false;
                }
                part.setContent(part.getContent(), part.getContentType());
                part.setHeader(HttpController.HEADER_CONTENT_TRANSFER_ENCODING, "8bit");
                return true;
            } else if (!part.isMimeType("multipart/*")) {
                return false;
            } else {
                MimeMultipart mp = (MimeMultipart) part.getContent();
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {
                    if (convertTo8Bit((MimePart) mp.getBodyPart(i))) {
                        changed = true;
                    }
                }
                return changed;
            }
        } catch (IOException | MessagingException e) {
            return false;
        }
    }

    private boolean is8Bit(InputStream is) {
        int linelen = 0;
        boolean need8bit = false;
        while (true) {
            try {
                int read = is.read();
                int b = read;
                if (read < 0) {
                    if (this.debug && need8bit) {
                        this.out.println("DEBUG SMTP: found an 8bit part");
                    }
                    return need8bit;
                }
                int b2 = b & 255;
                if (b2 == 13 || b2 == 10) {
                    linelen = 0;
                } else if (b2 == 0 || (linelen = linelen + 1) > 998) {
                    return false;
                }
                if (b2 > 127) {
                    need8bit = true;
                }
            } catch (IOException e) {
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        try {
            closeConnection();
        } catch (MessagingException e) {
        }
    }

    /* access modifiers changed from: protected */
    public void helo(String domain) throws MessagingException {
        if (domain != null) {
            issueCommand("HELO " + domain, Type.TSIG);
            return;
        }
        issueCommand("HELO", Type.TSIG);
    }

    /* access modifiers changed from: protected */
    public boolean ehlo(String domain) throws MessagingException {
        String cmd;
        if (domain != null) {
            cmd = "EHLO " + domain;
        } else {
            cmd = "EHLO";
        }
        sendCommand(cmd);
        int resp = readServerResponse();
        if (resp == 250) {
            BufferedReader rd = new BufferedReader(new StringReader(this.lastServerResponse));
            this.extMap = new Hashtable();
            boolean first = true;
            while (true) {
                try {
                    String readLine = rd.readLine();
                    String line = readLine;
                    if (readLine == null) {
                        break;
                    } else if (first) {
                        first = false;
                    } else if (line.length() >= 5) {
                        String line2 = line.substring(4);
                        int i = line2.indexOf(32);
                        String arg = "";
                        if (i > 0) {
                            arg = line2.substring(i + 1);
                            line2 = line2.substring(0, i);
                        }
                        if (this.debug) {
                            this.out.println("DEBUG SMTP: Found extension \"" + line2 + "\", arg \"" + arg + "\"");
                        }
                        this.extMap.put(line2.toUpperCase(Locale.ENGLISH), arg);
                    }
                } catch (IOException e) {
                }
            }
        }
        if (resp == 250) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0052  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x016a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void mailFrom() throws javax.mail.MessagingException {
        /*
            r8 = this;
            r0 = 0
            javax.mail.internet.MimeMessage r1 = r8.message
            boolean r2 = r1 instanceof com.sun.mail.smtp.SMTPMessage
            if (r2 == 0) goto L_0x000d
            com.sun.mail.smtp.SMTPMessage r1 = (com.sun.mail.smtp.SMTPMessage) r1
            java.lang.String r0 = r1.getEnvelopeFrom()
        L_0x000d:
            java.lang.String r1 = "mail."
            if (r0 == 0) goto L_0x0017
            int r2 = r0.length()
            if (r2 > 0) goto L_0x0030
        L_0x0017:
            javax.mail.Session r2 = r8.session
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>(r1)
            java.lang.String r4 = r8.name
            r3.append(r4)
            java.lang.String r4 = ".from"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            java.lang.String r0 = r2.getProperty(r3)
        L_0x0030:
            if (r0 == 0) goto L_0x0038
            int r2 = r0.length()
            if (r2 > 0) goto L_0x0059
        L_0x0038:
            javax.mail.internet.MimeMessage r2 = r8.message
            if (r2 == 0) goto L_0x004a
            javax.mail.Address[] r2 = r2.getFrom()
            r3 = r2
            if (r2 == 0) goto L_0x004a
            int r2 = r3.length
            if (r2 <= 0) goto L_0x004a
            r2 = 0
            r2 = r3[r2]
            goto L_0x0050
        L_0x004a:
            javax.mail.Session r2 = r8.session
            javax.mail.internet.InternetAddress r2 = javax.mail.internet.InternetAddress.getLocalAddress(r2)
        L_0x0050:
            if (r2 == 0) goto L_0x016a
            r3 = r2
            javax.mail.internet.InternetAddress r3 = (javax.mail.internet.InternetAddress) r3
            java.lang.String r0 = r3.getAddress()
        L_0x0059:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "MAIL FROM:"
            r2.<init>(r3)
            java.lang.String r3 = r8.normalizeAddress(r0)
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "DSN"
            boolean r3 = r8.supportsExtension(r3)
            if (r3 == 0) goto L_0x00b2
            r3 = 0
            javax.mail.internet.MimeMessage r4 = r8.message
            boolean r5 = r4 instanceof com.sun.mail.smtp.SMTPMessage
            if (r5 == 0) goto L_0x0080
            com.sun.mail.smtp.SMTPMessage r4 = (com.sun.mail.smtp.SMTPMessage) r4
            java.lang.String r3 = r4.getDSNRet()
        L_0x0080:
            if (r3 != 0) goto L_0x009b
            javax.mail.Session r4 = r8.session
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r1)
            java.lang.String r6 = r8.name
            r5.append(r6)
            java.lang.String r6 = ".dsn.ret"
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.String r3 = r4.getProperty(r5)
        L_0x009b:
            if (r3 == 0) goto L_0x00b2
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            java.lang.String r5 = java.lang.String.valueOf(r2)
            r4.<init>(r5)
            java.lang.String r5 = " RET="
            r4.append(r5)
            r4.append(r3)
            java.lang.String r2 = r4.toString()
        L_0x00b2:
            java.lang.String r3 = "AUTH"
            boolean r3 = r8.supportsExtension(r3)
            if (r3 == 0) goto L_0x011f
            r3 = 0
            javax.mail.internet.MimeMessage r4 = r8.message
            boolean r5 = r4 instanceof com.sun.mail.smtp.SMTPMessage
            if (r5 == 0) goto L_0x00c7
            com.sun.mail.smtp.SMTPMessage r4 = (com.sun.mail.smtp.SMTPMessage) r4
            java.lang.String r3 = r4.getSubmitter()
        L_0x00c7:
            if (r3 != 0) goto L_0x00e2
            javax.mail.Session r4 = r8.session
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r1)
            java.lang.String r6 = r8.name
            r5.append(r6)
            java.lang.String r6 = ".submitter"
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            java.lang.String r3 = r4.getProperty(r5)
        L_0x00e2:
            if (r3 == 0) goto L_0x011f
            java.lang.String r4 = xtext(r3)     // Catch:{ IllegalArgumentException -> 0x00ff }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IllegalArgumentException -> 0x00ff }
            java.lang.String r6 = java.lang.String.valueOf(r2)     // Catch:{ IllegalArgumentException -> 0x00ff }
            r5.<init>(r6)     // Catch:{ IllegalArgumentException -> 0x00ff }
            java.lang.String r6 = " AUTH="
            r5.append(r6)     // Catch:{ IllegalArgumentException -> 0x00ff }
            r5.append(r4)     // Catch:{ IllegalArgumentException -> 0x00ff }
            java.lang.String r5 = r5.toString()     // Catch:{ IllegalArgumentException -> 0x00ff }
            r2 = r5
            goto L_0x011f
        L_0x00ff:
            r4 = move-exception
            boolean r5 = r8.debug
            if (r5 == 0) goto L_0x011f
            java.io.PrintStream r5 = r8.out
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            java.lang.String r7 = "DEBUG SMTP: ignoring invalid submitter: "
            r6.<init>(r7)
            r6.append(r3)
            java.lang.String r7 = ", Exception: "
            r6.append(r7)
            r6.append(r4)
            java.lang.String r6 = r6.toString()
            r5.println(r6)
        L_0x011f:
            r3 = 0
            javax.mail.internet.MimeMessage r4 = r8.message
            boolean r5 = r4 instanceof com.sun.mail.smtp.SMTPMessage
            if (r5 == 0) goto L_0x012c
            com.sun.mail.smtp.SMTPMessage r4 = (com.sun.mail.smtp.SMTPMessage) r4
            java.lang.String r3 = r4.getMailExtension()
        L_0x012c:
            if (r3 != 0) goto L_0x0147
            javax.mail.Session r4 = r8.session
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r1)
            java.lang.String r1 = r8.name
            r5.append(r1)
            java.lang.String r1 = ".mailextension"
            r5.append(r1)
            java.lang.String r1 = r5.toString()
            java.lang.String r3 = r4.getProperty(r1)
        L_0x0147:
            if (r3 == 0) goto L_0x0164
            int r1 = r3.length()
            if (r1 <= 0) goto L_0x0164
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            java.lang.String r4 = java.lang.String.valueOf(r2)
            r1.<init>(r4)
            java.lang.String r4 = " "
            r1.append(r4)
            r1.append(r3)
            java.lang.String r2 = r1.toString()
        L_0x0164:
            r1 = 250(0xfa, float:3.5E-43)
            r8.issueSendCommand(r2, r1)
            return
        L_0x016a:
            javax.mail.MessagingException r1 = new javax.mail.MessagingException
            java.lang.String r3 = "can't determine local email address"
            r1.<init>(r3)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.mailFrom():void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:148:0x0305, code lost:
        r17 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:150:0x030f, code lost:
        if (r11 != false) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:151:0x0311, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x0313, code lost:
        r2 = r17;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:153:0x0315, code lost:
        r9.addElement(r0);
        r1 = new com.sun.mail.smtp.SMTPAddressFailedException(r0, r6, r14, r7.lastServerResponse);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:154:0x031f, code lost:
        if (r15 != null) goto L_0x0324;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:155:0x0321, code lost:
        r15 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:156:0x0324, code lost:
        r15.setNextException(r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void rcptTo() throws javax.mail.MessagingException {
        /*
            r21 = this;
            r7 = r21
            java.util.Vector r0 = new java.util.Vector
            r0.<init>()
            r8 = r0
            java.util.Vector r0 = new java.util.Vector
            r0.<init>()
            r9 = r0
            java.util.Vector r0 = new java.util.Vector
            r0.<init>()
            r10 = r0
            r0 = -1
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 0
            r7.invalidAddr = r4
            r7.validUnsentAddr = r4
            r7.validSentAddr = r4
            r4 = 0
            javax.mail.internet.MimeMessage r5 = r7.message
            boolean r6 = r5 instanceof com.sun.mail.smtp.SMTPMessage
            if (r6 == 0) goto L_0x002c
            com.sun.mail.smtp.SMTPMessage r5 = (com.sun.mail.smtp.SMTPMessage) r5
            boolean r4 = r5.getSendPartial()
        L_0x002c:
            java.lang.String r5 = "mail."
            r6 = 1
            if (r4 != 0) goto L_0x005b
            javax.mail.Session r11 = r7.session
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>(r5)
            java.lang.String r13 = r7.name
            r12.append(r13)
            java.lang.String r13 = ".sendpartial"
            r12.append(r13)
            java.lang.String r12 = r12.toString()
            java.lang.String r11 = r11.getProperty(r12)
            if (r11 == 0) goto L_0x0057
            java.lang.String r12 = "true"
            boolean r12 = r11.equalsIgnoreCase(r12)
            if (r12 == 0) goto L_0x0057
            r12 = r6
            goto L_0x0058
        L_0x0057:
            r12 = 0
        L_0x0058:
            r4 = r12
            r11 = r4
            goto L_0x005c
        L_0x005b:
            r11 = r4
        L_0x005c:
            boolean r4 = r7.debug
            if (r4 == 0) goto L_0x0069
            if (r11 == 0) goto L_0x0069
            java.io.PrintStream r4 = r7.out
            java.lang.String r12 = "DEBUG SMTP: sendPartial set"
            r4.println(r12)
        L_0x0069:
            r4 = 0
            r12 = 0
            java.lang.String r13 = "DSN"
            boolean r13 = r7.supportsExtension(r13)
            if (r13 == 0) goto L_0x00a4
            javax.mail.internet.MimeMessage r13 = r7.message
            boolean r14 = r13 instanceof com.sun.mail.smtp.SMTPMessage
            if (r14 == 0) goto L_0x007f
            com.sun.mail.smtp.SMTPMessage r13 = (com.sun.mail.smtp.SMTPMessage) r13
            java.lang.String r12 = r13.getDSNNotify()
        L_0x007f:
            if (r12 != 0) goto L_0x009b
            javax.mail.Session r13 = r7.session
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>(r5)
            java.lang.String r5 = r7.name
            r14.append(r5)
            java.lang.String r5 = ".dsn.notify"
            r14.append(r5)
            java.lang.String r5 = r14.toString()
            java.lang.String r5 = r13.getProperty(r5)
            r12 = r5
        L_0x009b:
            if (r12 == 0) goto L_0x00a1
            r4 = 1
            r13 = r12
            r12 = r4
            goto L_0x00a6
        L_0x00a1:
            r13 = r12
            r12 = r4
            goto L_0x00a6
        L_0x00a4:
            r13 = r12
            r12 = r4
        L_0x00a6:
            r4 = 0
            r14 = r0
            r15 = r1
            r16 = r3
        L_0x00ab:
            javax.mail.Address[] r0 = r7.addresses
            int r1 = r0.length
            java.lang.String r5 = "RSET"
            r3 = 250(0xfa, float:3.5E-43)
            if (r4 < r1) goto L_0x0244
            if (r11 == 0) goto L_0x00c0
            int r0 = r8.size()
            if (r0 != 0) goto L_0x00c0
            r2 = 1
            r17 = r2
            goto L_0x00c2
        L_0x00c0:
            r17 = r2
        L_0x00c2:
            if (r17 == 0) goto L_0x010c
            int r0 = r10.size()
            javax.mail.Address[] r0 = new javax.mail.Address[r0]
            r7.invalidAddr = r0
            r10.copyInto(r0)
            int r0 = r8.size()
            int r1 = r9.size()
            int r0 = r0 + r1
            javax.mail.Address[] r0 = new javax.mail.Address[r0]
            r7.validUnsentAddr = r0
            r0 = 0
            r1 = 0
        L_0x00de:
            int r2 = r8.size()
            if (r1 < r2) goto L_0x00fc
            r1 = 0
        L_0x00e5:
            int r2 = r9.size()
            if (r1 < r2) goto L_0x00ec
            goto L_0x0149
        L_0x00ec:
            javax.mail.Address[] r2 = r7.validUnsentAddr
            int r4 = r0 + 1
            java.lang.Object r6 = r9.elementAt(r1)
            javax.mail.Address r6 = (javax.mail.Address) r6
            r2[r0] = r6
            int r1 = r1 + 1
            r0 = r4
            goto L_0x00e5
        L_0x00fc:
            javax.mail.Address[] r2 = r7.validUnsentAddr
            int r4 = r0 + 1
            java.lang.Object r6 = r8.elementAt(r1)
            javax.mail.Address r6 = (javax.mail.Address) r6
            r2[r0] = r6
            int r1 = r1 + 1
            r0 = r4
            goto L_0x00de
        L_0x010c:
            boolean r0 = r7.reportSuccess
            if (r0 != 0) goto L_0x0124
            if (r11 == 0) goto L_0x011f
            int r0 = r10.size()
            if (r0 > 0) goto L_0x0124
            int r0 = r9.size()
            if (r0 <= 0) goto L_0x011f
            goto L_0x0124
        L_0x011f:
            javax.mail.Address[] r0 = r7.addresses
            r7.validSentAddr = r0
            goto L_0x0149
        L_0x0124:
            r7.sendPartiallyFailed = r6
            r7.exception = r15
            int r0 = r10.size()
            javax.mail.Address[] r0 = new javax.mail.Address[r0]
            r7.invalidAddr = r0
            r10.copyInto(r0)
            int r0 = r9.size()
            javax.mail.Address[] r0 = new javax.mail.Address[r0]
            r7.validUnsentAddr = r0
            r9.copyInto(r0)
            int r0 = r8.size()
            javax.mail.Address[] r0 = new javax.mail.Address[r0]
            r7.validSentAddr = r0
            r8.copyInto(r0)
        L_0x0149:
            boolean r0 = r7.debug
            if (r0 == 0) goto L_0x01d6
            javax.mail.Address[] r0 = r7.validSentAddr
            java.lang.String r1 = "DEBUG SMTP:   "
            if (r0 == 0) goto L_0x017c
            int r0 = r0.length
            if (r0 <= 0) goto L_0x017c
            java.io.PrintStream r0 = r7.out
            java.lang.String r2 = "DEBUG SMTP: Verified Addresses"
            r0.println(r2)
            r0 = 0
        L_0x015e:
            javax.mail.Address[] r2 = r7.validSentAddr
            int r2 = r2.length
            if (r0 < r2) goto L_0x0164
            goto L_0x017c
        L_0x0164:
            java.io.PrintStream r2 = r7.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r1)
            javax.mail.Address[] r6 = r7.validSentAddr
            r6 = r6[r0]
            r4.append(r6)
            java.lang.String r4 = r4.toString()
            r2.println(r4)
            int r0 = r0 + 1
            goto L_0x015e
        L_0x017c:
            javax.mail.Address[] r0 = r7.validUnsentAddr
            if (r0 == 0) goto L_0x01a9
            int r0 = r0.length
            if (r0 <= 0) goto L_0x01a9
            java.io.PrintStream r0 = r7.out
            java.lang.String r2 = "DEBUG SMTP: Valid Unsent Addresses"
            r0.println(r2)
            r0 = 0
        L_0x018b:
            javax.mail.Address[] r2 = r7.validUnsentAddr
            int r2 = r2.length
            if (r0 < r2) goto L_0x0191
            goto L_0x01a9
        L_0x0191:
            java.io.PrintStream r2 = r7.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r1)
            javax.mail.Address[] r6 = r7.validUnsentAddr
            r6 = r6[r0]
            r4.append(r6)
            java.lang.String r4 = r4.toString()
            r2.println(r4)
            int r0 = r0 + 1
            goto L_0x018b
        L_0x01a9:
            javax.mail.Address[] r0 = r7.invalidAddr
            if (r0 == 0) goto L_0x01d6
            int r0 = r0.length
            if (r0 <= 0) goto L_0x01d6
            java.io.PrintStream r0 = r7.out
            java.lang.String r2 = "DEBUG SMTP: Invalid Addresses"
            r0.println(r2)
            r0 = 0
        L_0x01b8:
            javax.mail.Address[] r2 = r7.invalidAddr
            int r2 = r2.length
            if (r0 < r2) goto L_0x01be
            goto L_0x01d6
        L_0x01be:
            java.io.PrintStream r2 = r7.out
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>(r1)
            javax.mail.Address[] r6 = r7.invalidAddr
            r6 = r6[r0]
            r4.append(r6)
            java.lang.String r4 = r4.toString()
            r2.println(r4)
            int r0 = r0 + 1
            goto L_0x01b8
        L_0x01d6:
            if (r17 == 0) goto L_0x0243
            boolean r0 = r7.debug
            if (r0 == 0) goto L_0x01e3
            java.io.PrintStream r0 = r7.out
            java.lang.String r1 = "DEBUG SMTP: Sending failed because of invalid destination addresses"
            r0.println(r1)
        L_0x01e3:
            r2 = 2
            javax.mail.Address[] r0 = r7.validSentAddr
            javax.mail.Address[] r4 = r7.validUnsentAddr
            javax.mail.Address[] r6 = r7.invalidAddr
            javax.mail.internet.MimeMessage r1 = r7.message
            r18 = r1
            r1 = r21
            r3 = r0
            r0 = r5
            r5 = r6
            r6 = r18
            r1.notifyTransportListeners(r2, r3, r4, r5, r6)
            java.lang.String r6 = r7.lastServerResponse
            int r5 = r7.lastReturnCode
            java.net.Socket r1 = r7.serverSocket     // Catch:{ MessagingException -> 0x020f }
            if (r1 == 0) goto L_0x0205
            r1 = 250(0xfa, float:3.5E-43)
            r7.issueCommand(r0, r1)     // Catch:{ MessagingException -> 0x020f }
        L_0x0205:
            r7.lastServerResponse = r6
            r7.lastReturnCode = r5
            goto L_0x0222
        L_0x020a:
            r0 = move-exception
            r20 = r14
            r14 = r5
            goto L_0x023e
        L_0x020f:
            r0 = move-exception
            r1 = r0
            r21.close()     // Catch:{ MessagingException -> 0x0215 }
            goto L_0x0205
        L_0x0215:
            r0 = move-exception
            r2 = r0
            r0 = r2
            boolean r2 = r7.debug     // Catch:{ all -> 0x023a }
            if (r2 == 0) goto L_0x0205
            java.io.PrintStream r2 = r7.out     // Catch:{ all -> 0x020a }
            r0.printStackTrace(r2)     // Catch:{ all -> 0x020a }
            goto L_0x0205
        L_0x0222:
            javax.mail.SendFailedException r18 = new javax.mail.SendFailedException
            javax.mail.Address[] r3 = r7.validSentAddr
            javax.mail.Address[] r4 = r7.validUnsentAddr
            javax.mail.Address[] r2 = r7.invalidAddr
            java.lang.String r1 = "Invalid Addresses"
            r0 = r18
            r19 = r2
            r2 = r15
            r20 = r14
            r14 = r5
            r5 = r19
            r0.<init>(r1, r2, r3, r4, r5)
            throw r18
        L_0x023a:
            r0 = move-exception
            r20 = r14
            r14 = r5
        L_0x023e:
            r7.lastServerResponse = r6
            r7.lastReturnCode = r14
            throw r0
        L_0x0243:
            return
        L_0x0244:
            r1 = r3
            r3 = r5
            r20 = r14
            r5 = 0
            r0 = r0[r4]
            javax.mail.internet.InternetAddress r0 = (javax.mail.internet.InternetAddress) r0
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            java.lang.String r6 = "RCPT TO:"
            r14.<init>(r6)
            java.lang.String r6 = r0.getAddress()
            java.lang.String r6 = r7.normalizeAddress(r6)
            r14.append(r6)
            java.lang.String r6 = r14.toString()
            if (r12 == 0) goto L_0x027a
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            java.lang.String r1 = java.lang.String.valueOf(r6)
            r14.<init>(r1)
            java.lang.String r1 = " NOTIFY="
            r14.append(r1)
            r14.append(r13)
            java.lang.String r6 = r14.toString()
        L_0x027a:
            r7.sendCommand((java.lang.String) r6)
            int r14 = r21.readServerResponse()
            r1 = 250(0xfa, float:3.5E-43)
            if (r14 == r1) goto L_0x0341
            r1 = 251(0xfb, float:3.52E-43)
            if (r14 == r1) goto L_0x0341
            r1 = 501(0x1f5, float:7.02E-43)
            if (r14 == r1) goto L_0x030a
            r1 = 503(0x1f7, float:7.05E-43)
            if (r14 == r1) goto L_0x030a
            switch(r14) {
                case 450: goto L_0x0305;
                case 451: goto L_0x0305;
                case 452: goto L_0x0305;
                default: goto L_0x0294;
            }
        L_0x0294:
            switch(r14) {
                case 550: goto L_0x030a;
                case 551: goto L_0x030a;
                case 552: goto L_0x0305;
                case 553: goto L_0x030a;
                default: goto L_0x0297;
            }
        L_0x0297:
            r1 = 400(0x190, float:5.6E-43)
            if (r14 < r1) goto L_0x02a3
            r1 = 499(0x1f3, float:6.99E-43)
            if (r14 > r1) goto L_0x02a3
            r9.addElement(r0)
            goto L_0x02ae
        L_0x02a3:
            r1 = 500(0x1f4, float:7.0E-43)
            if (r14 < r1) goto L_0x02c3
            r1 = 599(0x257, float:8.4E-43)
            if (r14 > r1) goto L_0x02c3
            r10.addElement(r0)
        L_0x02ae:
            if (r11 != 0) goto L_0x02b1
            r2 = 1
        L_0x02b1:
            com.sun.mail.smtp.SMTPAddressFailedException r1 = new com.sun.mail.smtp.SMTPAddressFailedException
            java.lang.String r3 = r7.lastServerResponse
            r1.<init>(r0, r6, r14, r3)
            if (r15 != 0) goto L_0x02be
            r3 = r1
            r15 = r3
            goto L_0x0364
        L_0x02be:
            r15.setNextException(r1)
            goto L_0x0364
        L_0x02c3:
            boolean r1 = r7.debug
            if (r1 == 0) goto L_0x02e9
            java.io.PrintStream r1 = r7.out
            r17 = r2
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r18 = r5
            java.lang.String r5 = "DEBUG SMTP: got response code "
            r2.<init>(r5)
            r2.append(r14)
            java.lang.String r5 = ", with response: "
            r2.append(r5)
            java.lang.String r5 = r7.lastServerResponse
            r2.append(r5)
            java.lang.String r2 = r2.toString()
            r1.println(r2)
            goto L_0x02ed
        L_0x02e9:
            r17 = r2
            r18 = r5
        L_0x02ed:
            java.lang.String r1 = r7.lastServerResponse
            int r2 = r7.lastReturnCode
            java.net.Socket r5 = r7.serverSocket
            if (r5 == 0) goto L_0x02fa
            r5 = 250(0xfa, float:3.5E-43)
            r7.issueCommand(r3, r5)
        L_0x02fa:
            r7.lastServerResponse = r1
            r7.lastReturnCode = r2
            com.sun.mail.smtp.SMTPAddressFailedException r3 = new com.sun.mail.smtp.SMTPAddressFailedException
            r3.<init>(r0, r6, r14, r1)
            throw r3
        L_0x0305:
            r17 = r2
            r18 = r5
            goto L_0x030f
        L_0x030a:
            r17 = r2
            r18 = r5
            goto L_0x0328
        L_0x030f:
            if (r11 != 0) goto L_0x0313
            r2 = 1
            goto L_0x0315
        L_0x0313:
            r2 = r17
        L_0x0315:
            r9.addElement(r0)
            com.sun.mail.smtp.SMTPAddressFailedException r1 = new com.sun.mail.smtp.SMTPAddressFailedException
            java.lang.String r3 = r7.lastServerResponse
            r1.<init>(r0, r6, r14, r3)
            if (r15 != 0) goto L_0x0324
            r3 = r1
            r15 = r3
            goto L_0x0364
        L_0x0324:
            r15.setNextException(r1)
            goto L_0x0364
        L_0x0328:
            if (r11 != 0) goto L_0x032c
            r2 = 1
            goto L_0x032e
        L_0x032c:
            r2 = r17
        L_0x032e:
            r10.addElement(r0)
            com.sun.mail.smtp.SMTPAddressFailedException r1 = new com.sun.mail.smtp.SMTPAddressFailedException
            java.lang.String r3 = r7.lastServerResponse
            r1.<init>(r0, r6, r14, r3)
            if (r15 != 0) goto L_0x033d
            r3 = r1
            r15 = r3
            goto L_0x0364
        L_0x033d:
            r15.setNextException(r1)
            goto L_0x0364
        L_0x0341:
            r17 = r2
            r18 = r5
            r8.addElement(r0)
            boolean r1 = r7.reportSuccess
            if (r1 != 0) goto L_0x0351
            r2 = r17
            r1 = r18
            goto L_0x0364
        L_0x0351:
            com.sun.mail.smtp.SMTPAddressSucceededException r1 = new com.sun.mail.smtp.SMTPAddressSucceededException
            java.lang.String r2 = r7.lastServerResponse
            r1.<init>(r0, r6, r14, r2)
            if (r15 != 0) goto L_0x035f
            r2 = r1
            r15 = r2
            r2 = r17
            goto L_0x0364
        L_0x035f:
            r15.setNextException(r1)
            r2 = r17
        L_0x0364:
            int r4 = r4 + 1
            r16 = r1
            r6 = 1
            goto L_0x00ab
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.smtp.SMTPTransport.rcptTo():void");
    }

    /* access modifiers changed from: protected */
    public OutputStream data() throws MessagingException {
        issueSendCommand("DATA", 354);
        SMTPOutputStream sMTPOutputStream = new SMTPOutputStream(this.serverOutput);
        this.dataStream = sMTPOutputStream;
        return sMTPOutputStream;
    }

    /* access modifiers changed from: protected */
    public void finishData() throws IOException, MessagingException {
        this.dataStream.ensureAtBOL();
        issueSendCommand(".", Type.TSIG);
    }

    /* access modifiers changed from: protected */
    public void startTLS() throws MessagingException {
        issueCommand("STARTTLS", 220);
        try {
            Socket socket = this.serverSocket;
            Properties properties = this.session.getProperties();
            this.serverSocket = SocketFetcher.startTLS(socket, properties, "mail." + this.name);
            initStreams();
        } catch (IOException ioex) {
            closeConnection();
            throw new MessagingException("Could not convert socket to TLS", ioex);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private void openServer(String server, int port) throws MessagingException {
        if (this.debug) {
            PrintStream printStream = this.out;
            printStream.println("DEBUG SMTP: trying to connect to host \"" + server + "\", port " + port + ", isSSL " + this.isSSL);
        }
        try {
            Properties props = this.session.getProperties();
            Socket socket = SocketFetcher.getSocket(server, port, props, "mail." + this.name, this.isSSL);
            this.serverSocket = socket;
            port = socket.getPort();
            initStreams();
            int readServerResponse = readServerResponse();
            int r = readServerResponse;
            if (readServerResponse != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    PrintStream printStream2 = this.out;
                    printStream2.println("DEBUG SMTP: could not connect to host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port + ", response: " + r);
            } else if (this.debug) {
                PrintStream printStream3 = this.out;
                printStream3.println("DEBUG SMTP: connected to host \"" + server + "\", port: " + port + "\n");
            }
        } catch (UnknownHostException uhex) {
            throw new MessagingException("Unknown SMTP host: " + server, uhex);
        } catch (IOException ioe) {
            throw new MessagingException("Could not connect to SMTP host: " + server + ", port: " + port, ioe);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    private void openServer() throws MessagingException {
        int port = -1;
        String server = "UNKNOWN";
        try {
            port = this.serverSocket.getPort();
            server = this.serverSocket.getInetAddress().getHostName();
            if (this.debug) {
                PrintStream printStream = this.out;
                printStream.println("DEBUG SMTP: starting protocol to host \"" + server + "\", port " + port);
            }
            initStreams();
            int readServerResponse = readServerResponse();
            int r = readServerResponse;
            if (readServerResponse != 220) {
                this.serverSocket.close();
                this.serverSocket = null;
                this.serverOutput = null;
                this.serverInput = null;
                this.lineInputStream = null;
                if (this.debug) {
                    PrintStream printStream2 = this.out;
                    printStream2.println("DEBUG SMTP: got bad greeting from host \"" + server + "\", port: " + port + ", response: " + r + "\n");
                }
                throw new MessagingException("Got bad greeting from SMTP host: " + server + ", port: " + port + ", response: " + r);
            } else if (this.debug) {
                PrintStream printStream3 = this.out;
                printStream3.println("DEBUG SMTP: protocol started to host \"" + server + "\", port: " + port + "\n");
            }
        } catch (IOException ioe) {
            throw new MessagingException("Could not start protocol to SMTP host: " + server + ", port: " + port, ioe);
        }
    }

    private void initStreams() throws IOException {
        Properties props = this.session.getProperties();
        PrintStream out2 = this.session.getDebugOut();
        boolean debug = this.session.getDebug();
        String s = props.getProperty("mail.debug.quote");
        boolean quote = s != null && s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
        TraceInputStream traceInput = new TraceInputStream(this.serverSocket.getInputStream(), out2);
        traceInput.setTrace(debug);
        traceInput.setQuote(quote);
        TraceOutputStream traceOutput = new TraceOutputStream(this.serverSocket.getOutputStream(), out2);
        traceOutput.setTrace(debug);
        traceOutput.setQuote(quote);
        this.serverOutput = new BufferedOutputStream(traceOutput);
        this.serverInput = new BufferedInputStream(traceInput);
        this.lineInputStream = new LineInputStream(this.serverInput);
    }

    public synchronized void issueCommand(String cmd, int expect) throws MessagingException {
        sendCommand(cmd);
        if (readServerResponse() != expect) {
            throw new MessagingException(this.lastServerResponse);
        }
    }

    private void issueSendCommand(String cmd, int expect) throws MessagingException {
        sendCommand(cmd);
        int readServerResponse = readServerResponse();
        int ret = readServerResponse;
        if (readServerResponse != expect) {
            Address[] addressArr = this.validSentAddr;
            int vsl = addressArr == null ? 0 : addressArr.length;
            Address[] addressArr2 = this.validUnsentAddr;
            int vul = addressArr2 == null ? 0 : addressArr2.length;
            Address[] valid = new Address[(vsl + vul)];
            if (vsl > 0) {
                System.arraycopy(this.validSentAddr, 0, valid, 0, vsl);
            }
            if (vul > 0) {
                System.arraycopy(this.validUnsentAddr, 0, valid, vsl, vul);
            }
            this.validSentAddr = null;
            this.validUnsentAddr = valid;
            if (this.debug) {
                PrintStream printStream = this.out;
                printStream.println("DEBUG SMTP: got response code " + ret + ", with response: " + this.lastServerResponse);
            }
            String _lsr = this.lastServerResponse;
            int _lrc = this.lastReturnCode;
            if (this.serverSocket != null) {
                issueCommand("RSET", Type.TSIG);
            }
            this.lastServerResponse = _lsr;
            this.lastReturnCode = _lrc;
            throw new SMTPSendFailedException(cmd, ret, this.lastServerResponse, this.exception, this.validSentAddr, this.validUnsentAddr, this.invalidAddr);
        }
    }

    public synchronized int simpleCommand(String cmd) throws MessagingException {
        sendCommand(cmd);
        return readServerResponse();
    }

    /* access modifiers changed from: protected */
    public int simpleCommand(byte[] cmd) throws MessagingException {
        sendCommand(cmd);
        return readServerResponse();
    }

    /* access modifiers changed from: protected */
    public void sendCommand(String cmd) throws MessagingException {
        sendCommand(ASCIIUtility.getBytes(cmd));
    }

    private void sendCommand(byte[] cmdBytes) throws MessagingException {
        try {
            this.serverOutput.write(cmdBytes);
            this.serverOutput.write(CRLF);
            this.serverOutput.flush();
        } catch (IOException ex) {
            throw new MessagingException("Can't send command to SMTP host", ex);
        }
    }

    /* access modifiers changed from: protected */
    public int readServerResponse() throws MessagingException {
        String line;
        int returnCode;
        StringBuffer buf = new StringBuffer(100);
        do {
            try {
                line = this.lineInputStream.readLine();
                if (line == null) {
                    String serverResponse = buf.toString();
                    if (serverResponse.length() == 0) {
                        serverResponse = "[EOF]";
                    }
                    this.lastServerResponse = serverResponse;
                    this.lastReturnCode = -1;
                    if (this.debug) {
                        PrintStream printStream = this.out;
                        printStream.println("DEBUG SMTP: EOF: " + serverResponse);
                    }
                    return -1;
                }
                buf.append(line);
                buf.append("\n");
            } catch (IOException ioex) {
                if (this.debug) {
                    PrintStream printStream2 = this.out;
                    printStream2.println("DEBUG SMTP: exception reading response: " + ioex);
                }
                this.lastServerResponse = "";
                this.lastReturnCode = 0;
                throw new MessagingException("Exception reading response", ioex);
            }
        } while (isNotLastLine(line));
        String serverResponse2 = buf.toString();
        if (serverResponse2 == null || serverResponse2.length() < 3) {
            returnCode = -1;
        } else {
            try {
                returnCode = Integer.parseInt(serverResponse2.substring(0, 3));
            } catch (NumberFormatException e) {
                try {
                    close();
                } catch (MessagingException mex) {
                    if (this.debug) {
                        mex.printStackTrace(this.out);
                    }
                }
                returnCode = -1;
            } catch (StringIndexOutOfBoundsException e2) {
                try {
                    close();
                } catch (MessagingException mex2) {
                    if (this.debug) {
                        mex2.printStackTrace(this.out);
                    }
                }
                returnCode = -1;
            }
        }
        if (returnCode == -1 && this.debug) {
            PrintStream printStream3 = this.out;
            printStream3.println("DEBUG SMTP: bad server response: " + serverResponse2);
        }
        this.lastServerResponse = serverResponse2;
        this.lastReturnCode = returnCode;
        return returnCode;
    }

    /* access modifiers changed from: protected */
    public void checkConnected() {
        if (!super.isConnected()) {
            throw new IllegalStateException("Not connected");
        }
    }

    private boolean isNotLastLine(String line) {
        return line != null && line.length() >= 4 && line.charAt(3) == '-';
    }

    private String normalizeAddress(String addr) {
        if (addr.startsWith("<") || addr.endsWith(">")) {
            return addr;
        }
        return "<" + addr + ">";
    }

    public boolean supportsExtension(String ext) {
        Hashtable hashtable = this.extMap;
        return (hashtable == null || hashtable.get(ext.toUpperCase(Locale.ENGLISH)) == null) ? false : true;
    }

    public String getExtensionParameter(String ext) {
        Hashtable hashtable = this.extMap;
        if (hashtable == null) {
            return null;
        }
        return (String) hashtable.get(ext.toUpperCase(Locale.ENGLISH));
    }

    /* access modifiers changed from: protected */
    public boolean supportsAuthentication(String auth) {
        String a;
        Hashtable hashtable = this.extMap;
        if (hashtable == null || (a = (String) hashtable.get("AUTH")) == null) {
            return false;
        }
        StringTokenizer st = new StringTokenizer(a);
        while (st.hasMoreTokens()) {
            if (st.nextToken().equalsIgnoreCase(auth)) {
                return true;
            }
        }
        return false;
    }

    protected static String xtext(String s) {
        StringBuffer sb = null;
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c < 128) {
                if (c < '!' || c > '~' || c == '+' || c == '=') {
                    if (sb == null) {
                        sb = new StringBuffer(s.length() + 4);
                        sb.append(s.substring(0, i));
                    }
                    sb.append('+');
                    sb.append(hexchar[(c & 240) >> 4]);
                    sb.append(hexchar[c & 15]);
                } else if (sb != null) {
                    sb.append(c);
                }
                i++;
            } else {
                throw new IllegalArgumentException("Non-ASCII character in SMTP submitter: " + s);
            }
        }
        return sb != null ? sb.toString() : s;
    }
}
