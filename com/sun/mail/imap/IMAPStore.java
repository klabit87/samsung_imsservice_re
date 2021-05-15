package com.sun.mail.imap;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.Namespaces;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.QuotaAwareStore;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class IMAPStore extends Store implements QuotaAwareStore, ResponseHandler {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int RESPONSE = 1000;
    private int appendBufferSize;
    private String authorizationID;
    private int blksize;
    private volatile boolean connected;
    private int defaultPort;
    private boolean disableAuthLogin;
    private boolean disableAuthPlain;
    private boolean enableImapEvents;
    private boolean enableSASL;
    private boolean enableStartTLS;
    private boolean forcePasswordRefresh;
    private String host;
    private boolean isSSL;
    private int minIdleTime;
    private String name;
    private Namespaces namespaces;
    private PrintStream out;
    private String password;
    private ConnectionPool pool;
    private int port;
    private String proxyAuthUser;
    private String[] saslMechanisms;
    private String saslRealm;
    private int statusCacheTimeout;
    private String user;

    static class ConnectionPool {
        private static final int ABORTING = 2;
        private static final int IDLE = 1;
        private static final int RUNNING = 0;
        /* access modifiers changed from: private */
        public Vector authenticatedConnections = new Vector();
        /* access modifiers changed from: private */
        public long clientTimeoutInterval = 45000;
        /* access modifiers changed from: private */
        public boolean debug = false;
        /* access modifiers changed from: private */
        public Vector folders;
        /* access modifiers changed from: private */
        public IMAPProtocol idleProtocol;
        /* access modifiers changed from: private */
        public int idleState = 0;
        /* access modifiers changed from: private */
        public long lastTimePruned;
        /* access modifiers changed from: private */
        public int poolSize = 1;
        /* access modifiers changed from: private */
        public long pruningInterval = 60000;
        /* access modifiers changed from: private */
        public boolean separateStoreConnection = false;
        /* access modifiers changed from: private */
        public long serverTimeoutInterval = 1800000;
        /* access modifiers changed from: private */
        public boolean storeConnectionInUse = false;

        ConnectionPool() {
        }
    }

    public IMAPStore(Session session, URLName url) {
        this(session, url, "imap", 143, false);
    }

    protected IMAPStore(Session session, URLName url, String name2, int defaultPort2, boolean isSSL2) {
        super(session, url);
        this.name = "imap";
        this.defaultPort = 143;
        this.isSSL = false;
        this.port = -1;
        this.blksize = 16384;
        this.statusCacheTimeout = 1000;
        this.appendBufferSize = -1;
        this.minIdleTime = 10;
        this.disableAuthLogin = false;
        this.disableAuthPlain = false;
        this.enableStartTLS = false;
        this.enableSASL = false;
        this.forcePasswordRefresh = false;
        this.enableImapEvents = false;
        this.connected = false;
        this.pool = new ConnectionPool();
        name2 = url != null ? url.getProtocol() : name2;
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        this.pool.lastTimePruned = System.currentTimeMillis();
        this.debug = session.getDebug();
        PrintStream debugOut = session.getDebugOut();
        this.out = debugOut;
        if (debugOut == null) {
            this.out = System.out;
        }
        String s = session.getProperty("mail." + name2 + ".connectionpool.debug");
        if (s != null && s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            this.pool.debug = true;
        }
        String s2 = session.getProperty("mail." + name2 + ".partialfetch");
        if (s2 == null || !s2.equalsIgnoreCase(ConfigConstants.VALUE.INFO_COMPLETED)) {
            String property = session.getProperty("mail." + name2 + ".fetchsize");
            String s3 = property;
            if (property != null) {
                this.blksize = Integer.parseInt(s3);
            }
            if (this.debug) {
                PrintStream printStream = this.out;
                printStream.println("DEBUG: mail.imap.fetchsize: " + this.blksize);
            }
        } else {
            this.blksize = -1;
            if (this.debug) {
                this.out.println("DEBUG: mail.imap.partialfetch: false");
            }
        }
        String s4 = session.getProperty("mail." + name2 + ".statuscachetimeout");
        if (s4 != null) {
            this.statusCacheTimeout = Integer.parseInt(s4);
            if (this.debug) {
                PrintStream printStream2 = this.out;
                printStream2.println("DEBUG: mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
            }
        }
        String s5 = session.getProperty("mail." + name2 + ".appendbuffersize");
        if (s5 != null) {
            this.appendBufferSize = Integer.parseInt(s5);
            if (this.debug) {
                PrintStream printStream3 = this.out;
                printStream3.println("DEBUG: mail.imap.appendbuffersize: " + this.appendBufferSize);
            }
        }
        String s6 = session.getProperty("mail." + name2 + ".minidletime");
        if (s6 != null) {
            this.minIdleTime = Integer.parseInt(s6);
            if (this.debug) {
                PrintStream printStream4 = this.out;
                printStream4.println("DEBUG: mail.imap.minidletime: " + this.minIdleTime);
            }
        }
        String s7 = session.getProperty("mail." + name2 + ".connectionpoolsize");
        if (s7 != null) {
            try {
                int size = Integer.parseInt(s7);
                if (size > 0) {
                    this.pool.poolSize = size;
                }
            } catch (NumberFormatException e) {
            }
            if (this.pool.debug) {
                PrintStream printStream5 = this.out;
                printStream5.println("DEBUG: mail.imap.connectionpoolsize: " + this.pool.poolSize);
            }
        }
        String s8 = session.getProperty("mail." + name2 + ".connectionpooltimeout");
        if (s8 != null) {
            try {
                int connectionPoolTimeout = Integer.parseInt(s8);
                if (connectionPoolTimeout > 0) {
                    this.pool.clientTimeoutInterval = (long) connectionPoolTimeout;
                }
            } catch (NumberFormatException e2) {
            }
            if (this.pool.debug) {
                PrintStream printStream6 = this.out;
                printStream6.println("DEBUG: mail.imap.connectionpooltimeout: " + this.pool.clientTimeoutInterval);
            }
        }
        String s9 = session.getProperty("mail." + name2 + ".servertimeout");
        if (s9 != null) {
            try {
                int serverTimeout = Integer.parseInt(s9);
                if (serverTimeout > 0) {
                    this.pool.serverTimeoutInterval = (long) serverTimeout;
                }
            } catch (NumberFormatException e3) {
            }
            if (this.pool.debug) {
                PrintStream printStream7 = this.out;
                printStream7.println("DEBUG: mail.imap.servertimeout: " + this.pool.serverTimeoutInterval);
            }
        }
        String s10 = session.getProperty("mail." + name2 + ".separatestoreconnection");
        if (s10 != null && s10.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.pool.debug) {
                this.out.println("DEBUG: dedicate a store connection");
            }
            this.pool.separateStoreConnection = true;
        }
        String s11 = session.getProperty("mail." + name2 + ".proxyauth.user");
        if (s11 != null) {
            this.proxyAuthUser = s11;
            if (this.debug) {
                PrintStream printStream8 = this.out;
                printStream8.println("DEBUG: mail.imap.proxyauth.user: " + this.proxyAuthUser);
            }
        }
        String s12 = session.getProperty("mail." + name2 + ".auth.login.disable");
        if (s12 != null && s12.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=LOGIN");
            }
            this.disableAuthLogin = true;
        }
        String s13 = session.getProperty("mail." + name2 + ".auth.plain.disable");
        if (s13 != null && s13.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: disable AUTH=PLAIN");
            }
            this.disableAuthPlain = true;
        }
        String s14 = session.getProperty("mail." + name2 + ".starttls.enable");
        if (s14 != null && s14.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: enable STARTTLS");
            }
            this.enableStartTLS = true;
        }
        String s15 = session.getProperty("mail." + name2 + ".sasl.enable");
        if (s15 != null && s15.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: enable SASL");
            }
            this.enableSASL = true;
        }
        if (this.enableSASL) {
            String s16 = session.getProperty("mail." + name2 + ".sasl.mechanisms");
            if (s16 != null && s16.length() > 0) {
                if (this.debug) {
                    PrintStream printStream9 = this.out;
                    printStream9.println("DEBUG: SASL mechanisms allowed: " + s16);
                }
                Vector v = new Vector(5);
                StringTokenizer st = new StringTokenizer(s16, " ,");
                while (st.hasMoreTokens()) {
                    String m = st.nextToken();
                    if (m.length() > 0) {
                        v.addElement(m);
                    }
                }
                String[] strArr = new String[v.size()];
                this.saslMechanisms = strArr;
                v.copyInto(strArr);
            }
        }
        String s17 = session.getProperty("mail." + name2 + ".sasl.authorizationid");
        if (s17 != null) {
            this.authorizationID = s17;
            if (this.debug) {
                PrintStream printStream10 = this.out;
                printStream10.println("DEBUG: mail.imap.sasl.authorizationid: " + this.authorizationID);
            }
        }
        String s18 = session.getProperty("mail." + name2 + ".sasl.realm");
        if (s18 != null) {
            this.saslRealm = s18;
            if (this.debug) {
                PrintStream printStream11 = this.out;
                printStream11.println("DEBUG: mail.imap.sasl.realm: " + this.saslRealm);
            }
        }
        String s19 = session.getProperty("mail." + name2 + ".forcepasswordrefresh");
        if (s19 != null && s19.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: enable forcePasswordRefresh");
            }
            this.forcePasswordRefresh = true;
        }
        String s20 = session.getProperty("mail." + name2 + ".enableimapevents");
        if (s20 != null && s20.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            if (this.debug) {
                this.out.println("DEBUG: enable IMAP events");
            }
            this.enableImapEvents = true;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized boolean protocolConnect(String host2, int pport, String user2, String password2) throws MessagingException {
        boolean poolEmpty;
        IMAPProtocol protocol = null;
        if (host2 == null || password2 == null || user2 == null) {
            if (this.debug) {
                PrintStream printStream = this.out;
                StringBuilder sb = new StringBuilder("DEBUG: protocolConnect returning false, host=");
                sb.append(host2);
                sb.append(", user=");
                sb.append(user2);
                sb.append(", password=");
                sb.append(password2 != null ? "<non-null>" : "<null>");
                printStream.println(sb.toString());
            }
            return false;
        }
        if (pport != -1) {
            this.port = pport;
        } else {
            Session session = this.session;
            String portstring = session.getProperty("mail." + this.name + ".port");
            if (portstring != null) {
                this.port = Integer.parseInt(portstring);
            }
        }
        if (this.port == -1) {
            this.port = this.defaultPort;
        }
        try {
            synchronized (this.pool) {
                poolEmpty = this.pool.authenticatedConnections.isEmpty();
            }
            if (poolEmpty) {
                protocol = new IMAPProtocol(this.name, host2, this.port, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), this.isSSL);
                if (this.debug) {
                    PrintStream printStream2 = this.out;
                    printStream2.println("DEBUG: protocolConnect login, host=" + host2 + ", user=" + user2 + ", password=<non-null>");
                }
                login(protocol, user2, password2);
                protocol.addResponseHandler(this);
                this.host = host2;
                this.user = user2;
                this.password = password2;
                synchronized (this.pool) {
                    this.pool.authenticatedConnections.addElement(protocol);
                }
            }
            this.connected = true;
            return true;
        } catch (CommandFailedException cex) {
            if (protocol != null) {
                protocol.disconnect();
            }
            throw new AuthenticationFailedException(cex.getResponse().getRest());
        } catch (ProtocolException pex) {
            throw new MessagingException(pex.getMessage(), pex);
        } catch (IOException ioex) {
            throw new MessagingException(ioex.getMessage(), ioex);
        }
    }

    private void login(IMAPProtocol p, String u, String pw) throws ProtocolException {
        String authzid;
        if (this.enableStartTLS && p.hasCapability("STARTTLS")) {
            p.startTLS();
            p.capability();
        }
        if (!p.isAuthenticated()) {
            p.getCapabilities().put("__PRELOGIN__", "");
            if (this.authorizationID != null) {
                authzid = this.authorizationID;
            } else if (this.proxyAuthUser != null) {
                authzid = this.proxyAuthUser;
            } else {
                authzid = u;
            }
            if (this.enableSASL) {
                p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
            }
            if (!p.isAuthenticated()) {
                if (p.hasCapability("AUTH=PLAIN") && !this.disableAuthPlain) {
                    p.authplain(authzid, u, pw);
                } else if ((p.hasCapability("AUTH-LOGIN") || p.hasCapability("AUTH=LOGIN")) && !this.disableAuthLogin) {
                    p.authlogin(u, pw);
                } else if (!p.hasCapability("LOGINDISABLED")) {
                    p.login(u, pw);
                } else {
                    throw new ProtocolException("No login methods supported!");
                }
            }
            String str = this.proxyAuthUser;
            if (str != null) {
                p.proxyauth(str);
            }
            if (p.hasCapability("__PRELOGIN__")) {
                try {
                    p.capability();
                } catch (ConnectionException cex) {
                    throw cex;
                } catch (ProtocolException e) {
                }
            }
        }
    }

    public synchronized void setUsername(String user2) {
        this.user = user2;
    }

    public synchronized void setPassword(String password2) {
        this.password = password2;
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0101 A[Catch:{ ProtocolException -> 0x0080, all -> 0x0088 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sun.mail.imap.protocol.IMAPProtocol getProtocol(com.sun.mail.imap.IMAPFolder r12) throws javax.mail.MessagingException {
        /*
            r11 = this;
            r0 = 0
        L_0x0002:
            if (r0 == 0) goto L_0x0005
            return r0
        L_0x0005:
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r11.pool
            monitor-enter(r1)
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0127 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0127 }
            if (r2 != 0) goto L_0x0092
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0127 }
            int r2 = r2.size()     // Catch:{ all -> 0x0127 }
            r3 = 1
            if (r2 != r3) goto L_0x0032
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            boolean r2 = r2.separateStoreConnection     // Catch:{ all -> 0x0127 }
            if (r2 != 0) goto L_0x0092
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            boolean r2 = r2.storeConnectionInUse     // Catch:{ all -> 0x0127 }
            if (r2 == 0) goto L_0x0032
            goto L_0x0092
        L_0x0032:
            boolean r2 = r11.debug     // Catch:{ all -> 0x0127 }
            if (r2 == 0) goto L_0x0053
            java.io.PrintStream r2 = r11.out     // Catch:{ all -> 0x0127 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0127 }
            java.lang.String r4 = "DEBUG: connection available -- size: "
            r3.<init>(r4)     // Catch:{ all -> 0x0127 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r4 = r4.authenticatedConnections     // Catch:{ all -> 0x0127 }
            int r4 = r4.size()     // Catch:{ all -> 0x0127 }
            r3.append(r4)     // Catch:{ all -> 0x0127 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0127 }
            r2.println(r3)     // Catch:{ all -> 0x0127 }
        L_0x0053:
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0127 }
            java.lang.Object r2 = r2.lastElement()     // Catch:{ all -> 0x0127 }
            com.sun.mail.imap.protocol.IMAPProtocol r2 = (com.sun.mail.imap.protocol.IMAPProtocol) r2     // Catch:{ all -> 0x0127 }
            r0 = r2
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0127 }
            r2.removeElement(r0)     // Catch:{ all -> 0x0127 }
            long r2 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0127 }
            long r4 = r0.getTimestamp()     // Catch:{ all -> 0x0127 }
            long r2 = r2 - r4
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r11.pool     // Catch:{ all -> 0x0127 }
            long r4 = r4.serverTimeoutInterval     // Catch:{ all -> 0x0127 }
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r4 <= 0) goto L_0x008d
            r0.noop()     // Catch:{ ProtocolException -> 0x0080 }
            goto L_0x008d
        L_0x0080:
            r4 = move-exception
            r0.removeResponseHandler(r11)     // Catch:{ all -> 0x0088 }
            r0.disconnect()     // Catch:{ all -> 0x0088 }
            goto L_0x0089
        L_0x0088:
            r5 = move-exception
        L_0x0089:
            r0 = 0
            monitor-exit(r1)     // Catch:{ all -> 0x0127 }
            goto L_0x0002
        L_0x008d:
            r0.removeResponseHandler(r11)     // Catch:{ all -> 0x0127 }
            goto L_0x00fc
        L_0x0092:
            boolean r2 = r11.debug     // Catch:{ all -> 0x0127 }
            if (r2 == 0) goto L_0x009d
            java.io.PrintStream r2 = r11.out     // Catch:{ all -> 0x0127 }
            java.lang.String r3 = "DEBUG: no connections in the pool, creating a new one"
            r2.println(r3)     // Catch:{ all -> 0x0127 }
        L_0x009d:
            boolean r2 = r11.forcePasswordRefresh     // Catch:{ Exception -> 0x00f1 }
            if (r2 == 0) goto L_0x00c8
            java.lang.String r2 = r11.host     // Catch:{ UnknownHostException -> 0x00a8 }
            java.net.InetAddress r2 = java.net.InetAddress.getByName(r2)     // Catch:{ UnknownHostException -> 0x00a8 }
            goto L_0x00ab
        L_0x00a8:
            r2 = move-exception
            r3 = 0
            r2 = r3
        L_0x00ab:
            javax.mail.Session r3 = r11.session     // Catch:{ Exception -> 0x00f1 }
            int r5 = r11.port     // Catch:{ Exception -> 0x00f1 }
            java.lang.String r6 = r11.name     // Catch:{ Exception -> 0x00f1 }
            r7 = 0
            java.lang.String r8 = r11.user     // Catch:{ Exception -> 0x00f1 }
            r4 = r2
            javax.mail.PasswordAuthentication r3 = r3.requestPasswordAuthentication(r4, r5, r6, r7, r8)     // Catch:{ Exception -> 0x00f1 }
            if (r3 == 0) goto L_0x00c8
            java.lang.String r4 = r3.getUserName()     // Catch:{ Exception -> 0x00f1 }
            r11.user = r4     // Catch:{ Exception -> 0x00f1 }
            java.lang.String r4 = r3.getPassword()     // Catch:{ Exception -> 0x00f1 }
            r11.password = r4     // Catch:{ Exception -> 0x00f1 }
        L_0x00c8:
            com.sun.mail.imap.protocol.IMAPProtocol r2 = new com.sun.mail.imap.protocol.IMAPProtocol     // Catch:{ Exception -> 0x00f1 }
            java.lang.String r4 = r11.name     // Catch:{ Exception -> 0x00f1 }
            java.lang.String r5 = r11.host     // Catch:{ Exception -> 0x00f1 }
            int r6 = r11.port     // Catch:{ Exception -> 0x00f1 }
            javax.mail.Session r3 = r11.session     // Catch:{ Exception -> 0x00f1 }
            boolean r7 = r3.getDebug()     // Catch:{ Exception -> 0x00f1 }
            javax.mail.Session r3 = r11.session     // Catch:{ Exception -> 0x00f1 }
            java.io.PrintStream r8 = r3.getDebugOut()     // Catch:{ Exception -> 0x00f1 }
            javax.mail.Session r3 = r11.session     // Catch:{ Exception -> 0x00f1 }
            java.util.Properties r9 = r3.getProperties()     // Catch:{ Exception -> 0x00f1 }
            boolean r10 = r11.isSSL     // Catch:{ Exception -> 0x00f1 }
            r3 = r2
            r3.<init>(r4, r5, r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x00f1 }
            r0 = r2
            java.lang.String r2 = r11.user     // Catch:{ Exception -> 0x00f1 }
            java.lang.String r3 = r11.password     // Catch:{ Exception -> 0x00f1 }
            r11.login(r0, r2, r3)     // Catch:{ Exception -> 0x00f1 }
            goto L_0x00fa
        L_0x00f1:
            r2 = move-exception
            if (r0 == 0) goto L_0x00f9
            r0.disconnect()     // Catch:{ Exception -> 0x00f8 }
            goto L_0x00f9
        L_0x00f8:
            r3 = move-exception
        L_0x00f9:
            r0 = 0
        L_0x00fa:
            if (r0 == 0) goto L_0x011f
        L_0x00fc:
            r11.timeoutConnections()     // Catch:{ all -> 0x0127 }
            if (r12 == 0) goto L_0x011c
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.folders     // Catch:{ all -> 0x0127 }
            if (r2 != 0) goto L_0x0113
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r3 = new java.util.Vector     // Catch:{ all -> 0x0127 }
            r3.<init>()     // Catch:{ all -> 0x0127 }
            r2.folders = r3     // Catch:{ all -> 0x0127 }
        L_0x0113:
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r11.pool     // Catch:{ all -> 0x0127 }
            java.util.Vector r2 = r2.folders     // Catch:{ all -> 0x0127 }
            r2.addElement(r12)     // Catch:{ all -> 0x0127 }
        L_0x011c:
            monitor-exit(r1)     // Catch:{ all -> 0x0127 }
            goto L_0x0002
        L_0x011f:
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0127 }
            java.lang.String r3 = "connection failure"
            r2.<init>(r3)     // Catch:{ all -> 0x0127 }
            throw r2     // Catch:{ all -> 0x0127 }
        L_0x0127:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0127 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getProtocol(com.sun.mail.imap.IMAPFolder):com.sun.mail.imap.protocol.IMAPProtocol");
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    /* access modifiers changed from: package-private */
    public IMAPProtocol getStoreProtocol() throws ProtocolException {
        IMAPProtocol p = null;
        while (p == null) {
            synchronized (this.pool) {
                waitIfIdle();
                if (this.pool.authenticatedConnections.isEmpty()) {
                    if (this.pool.debug) {
                        this.out.println("DEBUG: getStoreProtocol() - no connections in the pool, creating a new one");
                    }
                    try {
                        p = new IMAPProtocol(this.name, this.host, this.port, this.session.getDebug(), this.session.getDebugOut(), this.session.getProperties(), this.isSSL);
                        login(p, this.user, this.password);
                    } catch (Exception e) {
                        if (p != null) {
                            try {
                                p.logout();
                            } catch (Exception e2) {
                            }
                        }
                        p = null;
                    }
                    if (p != null) {
                        p.addResponseHandler(this);
                        this.pool.authenticatedConnections.addElement(p);
                    } else {
                        throw new ConnectionException("failed to create new store connection");
                    }
                } else {
                    if (this.pool.debug) {
                        PrintStream printStream = this.out;
                        printStream.println("DEBUG: getStoreProtocol() - connection available -- size: " + this.pool.authenticatedConnections.size());
                    }
                    p = (IMAPProtocol) this.pool.authenticatedConnections.firstElement();
                }
                if (this.pool.storeConnectionInUse) {
                    p = null;
                    try {
                        this.pool.wait();
                    } catch (InterruptedException e3) {
                    }
                } else {
                    this.pool.storeConnectionInUse = true;
                    if (this.pool.debug) {
                        this.out.println("DEBUG: getStoreProtocol() -- storeConnectionInUse");
                    }
                }
                timeoutConnections();
            }
        }
        return p;
    }

    /* access modifiers changed from: package-private */
    public boolean allowReadOnlySelect() {
        Session session = this.session;
        String s = session.getProperty("mail." + this.name + ".allowreadonlyselect");
        return s != null && s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSeparateStoreConnection() {
        return this.pool.separateStoreConnection;
    }

    /* access modifiers changed from: package-private */
    public boolean getConnectionPoolDebug() {
        return this.pool.debug;
    }

    /* access modifiers changed from: package-private */
    public boolean isConnectionPoolFull() {
        boolean z;
        synchronized (this.pool) {
            if (this.pool.debug) {
                PrintStream printStream = this.out;
                printStream.println("DEBUG: current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
            }
            z = this.pool.authenticatedConnections.size() >= this.pool.poolSize;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void releaseProtocol(IMAPFolder folder, IMAPProtocol protocol) {
        synchronized (this.pool) {
            if (protocol != null) {
                if (!isConnectionPoolFull()) {
                    protocol.addResponseHandler(this);
                    this.pool.authenticatedConnections.addElement(protocol);
                    if (this.debug) {
                        PrintStream printStream = this.out;
                        printStream.println("DEBUG: added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
                    }
                } else {
                    if (this.debug) {
                        this.out.println("DEBUG: pool is full, not adding an Authenticated connection");
                    }
                    try {
                        protocol.logout();
                    } catch (ProtocolException e) {
                    }
                }
            }
            if (this.pool.folders != null) {
                this.pool.folders.removeElement(folder);
            }
            timeoutConnections();
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseStoreProtocol(IMAPProtocol protocol) {
        if (protocol != null) {
            synchronized (this.pool) {
                this.pool.storeConnectionInUse = false;
                this.pool.notifyAll();
                if (this.pool.debug) {
                    this.out.println("DEBUG: releaseStoreProtocol()");
                }
                timeoutConnections();
            }
        }
    }

    private void emptyConnectionPool(boolean force) {
        synchronized (this.pool) {
            for (int index = this.pool.authenticatedConnections.size() - 1; index >= 0; index--) {
                try {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    p.removeResponseHandler(this);
                    if (force) {
                        p.disconnect();
                    } else {
                        p.logout();
                    }
                } catch (ProtocolException e) {
                }
            }
            this.pool.authenticatedConnections.removeAllElements();
        }
        if (this.pool.debug) {
            this.out.println("DEBUG: removed all authenticated connections");
        }
    }

    private void timeoutConnections() {
        synchronized (this.pool) {
            if (System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval && this.pool.authenticatedConnections.size() > 1) {
                if (this.pool.debug) {
                    this.out.println("DEBUG: checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
                    this.out.println("DEBUG: clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
                }
                for (int index = this.pool.authenticatedConnections.size() - 1; index > 0; index--) {
                    IMAPProtocol p = (IMAPProtocol) this.pool.authenticatedConnections.elementAt(index);
                    if (this.pool.debug) {
                        this.out.println("DEBUG: protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
                    }
                    if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval) {
                        if (this.pool.debug) {
                            this.out.println("DEBUG: authenticated connection timed out");
                            this.out.println("DEBUG: logging out the connection");
                        }
                        p.removeResponseHandler(this);
                        this.pool.authenticatedConnections.removeElementAt(index);
                        try {
                            p.logout();
                        } catch (ProtocolException e) {
                        }
                    }
                }
                this.pool.lastTimePruned = System.currentTimeMillis();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getFetchBlockSize() {
        return this.blksize;
    }

    /* access modifiers changed from: package-private */
    public Session getSession() {
        return this.session;
    }

    /* access modifiers changed from: package-private */
    public int getStatusCacheTimeout() {
        return this.statusCacheTimeout;
    }

    /* access modifiers changed from: package-private */
    public int getAppendBufferSize() {
        return this.appendBufferSize;
    }

    /* access modifiers changed from: package-private */
    public int getMinIdleTime() {
        return this.minIdleTime;
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    public synchronized boolean hasCapability(String capability) throws MessagingException {
        boolean hasCapability;
        try {
            IMAPProtocol p = getStoreProtocol();
            hasCapability = p.hasCapability(capability);
            releaseStoreProtocol(p);
        } catch (ProtocolException pex) {
            if (0 == 0) {
                cleanup();
            }
            throw new MessagingException(pex.getMessage(), pex);
        } catch (Throwable th) {
            pex = th;
            releaseStoreProtocol((IMAPProtocol) null);
            throw pex;
        }
        return hasCapability;
    }

    public synchronized boolean isConnected() {
        if (!this.connected) {
            super.setConnected(false);
            return false;
        }
        IMAPProtocol p = null;
        try {
            p = getStoreProtocol();
            p.noop();
        } catch (ProtocolException e) {
            if (p == null) {
                try {
                    cleanup();
                } catch (Throwable th) {
                    releaseStoreProtocol(p);
                    throw th;
                }
            }
        }
        releaseStoreProtocol(p);
        return super.isConnected();
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0052, code lost:
        r3 = th;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void close() throws javax.mail.MessagingException {
        /*
            r4 = this;
            monitor-enter(r4)
            boolean r0 = super.isConnected()     // Catch:{ all -> 0x006b }
            if (r0 != 0) goto L_0x0009
            monitor-exit(r4)
            return
        L_0x0009:
            r0 = 0
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r4.pool     // Catch:{ ProtocolException -> 0x0059 }
            monitor-enter(r1)     // Catch:{ ProtocolException -> 0x0059 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r4.pool     // Catch:{ all -> 0x0054 }
            java.util.Vector r2 = r2.authenticatedConnections     // Catch:{ all -> 0x0054 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0054 }
            monitor-exit(r1)     // Catch:{ all -> 0x0054 }
            if (r2 == 0) goto L_0x0035
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r4.pool     // Catch:{ ProtocolException -> 0x0033, all -> 0x0031 }
            boolean r1 = r1.debug     // Catch:{ ProtocolException -> 0x0033, all -> 0x0031 }
            if (r1 == 0) goto L_0x0029
            java.io.PrintStream r1 = r4.out     // Catch:{ ProtocolException -> 0x0059 }
            java.lang.String r3 = "DEBUG: close() - no connections "
            r1.println(r3)     // Catch:{ ProtocolException -> 0x0059 }
        L_0x0029:
            r4.cleanup()     // Catch:{ ProtocolException -> 0x0033, all -> 0x0031 }
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x006b }
            monitor-exit(r4)
            return
        L_0x0031:
            r1 = move-exception
            goto L_0x0067
        L_0x0033:
            r1 = move-exception
            goto L_0x005a
        L_0x0035:
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r4.getStoreProtocol()     // Catch:{ ProtocolException -> 0x0059 }
            r0 = r1
            com.sun.mail.imap.IMAPStore$ConnectionPool r1 = r4.pool     // Catch:{ ProtocolException -> 0x0059 }
            monitor-enter(r1)     // Catch:{ ProtocolException -> 0x0059 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r4.pool     // Catch:{ all -> 0x004f }
            java.util.Vector r3 = r3.authenticatedConnections     // Catch:{ all -> 0x004f }
            r3.removeElement(r0)     // Catch:{ all -> 0x004f }
            monitor-exit(r1)     // Catch:{ all -> 0x004f }
            r0.logout()     // Catch:{ ProtocolException -> 0x0033, all -> 0x0031 }
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x006b }
            monitor-exit(r4)
            return
        L_0x004f:
            r3 = move-exception
        L_0x0050:
            monitor-exit(r1)     // Catch:{ all -> 0x0052 }
            throw r3     // Catch:{ ProtocolException -> 0x0059 }
        L_0x0052:
            r3 = move-exception
            goto L_0x0050
        L_0x0054:
            r2 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0054 }
            throw r2     // Catch:{ ProtocolException -> 0x0059 }
        L_0x0057:
            r1 = move-exception
            goto L_0x0067
        L_0x0059:
            r1 = move-exception
        L_0x005a:
            r4.cleanup()     // Catch:{ all -> 0x0057 }
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0057 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x0057 }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x0057 }
            throw r2     // Catch:{ all -> 0x0057 }
        L_0x0067:
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x006b }
            throw r1     // Catch:{ all -> 0x006b }
        L_0x006b:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.close():void");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private void cleanup() {
        cleanup(false);
    }

    private void cleanup(boolean force) {
        boolean done;
        if (this.debug) {
            PrintStream printStream = this.out;
            printStream.println("DEBUG: IMAPStore cleanup, force " + force);
        }
        Vector foldersCopy = null;
        while (true) {
            synchronized (this.pool) {
                if (this.pool.folders != null) {
                    done = false;
                    foldersCopy = this.pool.folders;
                    this.pool.folders = null;
                } else {
                    done = true;
                }
            }
            if (done) {
                synchronized (this.pool) {
                    emptyConnectionPool(force);
                }
                this.connected = false;
                notifyConnectionListeners(3);
                if (this.debug) {
                    this.out.println("DEBUG: IMAPStore cleanup done");
                    return;
                }
                return;
            }
            int fsize = foldersCopy.size();
            for (int i = 0; i < fsize; i++) {
                IMAPFolder f = (IMAPFolder) foldersCopy.elementAt(i);
                if (force) {
                    try {
                        if (this.debug) {
                            this.out.println("DEBUG: force folder to close");
                        }
                        f.forceClose();
                    } catch (IllegalStateException | MessagingException e) {
                    }
                } else {
                    if (this.debug) {
                        this.out.println("DEBUG: close folder");
                    }
                    f.close(false);
                }
            }
        }
        while (true) {
        }
    }

    public synchronized Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public synchronized Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return new IMAPFolder(name2, 65535, this);
    }

    public synchronized Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return new IMAPFolder(url.getFile(), 65535, this);
    }

    public Folder[] getPersonalNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.personal == null) {
            return super.getPersonalNamespaces();
        }
        return namespaceToFolders(ns.personal, (String) null);
    }

    public Folder[] getUserNamespaces(String user2) throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.otherUsers == null) {
            return super.getUserNamespaces(user2);
        }
        return namespaceToFolders(ns.otherUsers, user2);
    }

    public Folder[] getSharedNamespaces() throws MessagingException {
        Namespaces ns = getNamespaces();
        if (ns == null || ns.shared == null) {
            return super.getSharedNamespaces();
        }
        return namespaceToFolders(ns.shared, (String) null);
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002b, code lost:
        throw new javax.mail.MessagingException(r1.getMessage(), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0037, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        releaseStoreProtocol(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003b, code lost:
        if (r0 == null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003d, code lost:
        cleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0040, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0042, code lost:
        releaseStoreProtocol(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0045, code lost:
        if (r0 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0047, code lost:
        cleanup();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x001f A[ExcHandler: all (th java.lang.Throwable), PHI: r0 
      PHI: (r0v6 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r0v1 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0009, B:7:0x000e, B:8:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0009] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0021 A[ExcHandler: ProtocolException (r1v7 'pex' com.sun.mail.iap.ProtocolException A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v5 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r0v1 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0009, B:7:0x000e, B:8:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0009] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x003d A[Catch:{ BadCommandException -> 0x0041, ConnectionException -> 0x001d, ProtocolException -> 0x0021, all -> 0x001f, all -> 0x0037 }] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0041 A[Catch:{ BadCommandException -> 0x0041, ConnectionException -> 0x001d, ProtocolException -> 0x0021, all -> 0x001f, all -> 0x0037 }, ExcHandler: BadCommandException (e com.sun.mail.iap.BadCommandException), PHI: r0 
      PHI: (r0v2 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r0v1 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v7 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0009, B:7:0x000e, B:8:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0009] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized com.sun.mail.imap.protocol.Namespaces getNamespaces() throws javax.mail.MessagingException {
        /*
            r4 = this;
            monitor-enter(r4)
            r4.checkConnected()     // Catch:{ all -> 0x004e }
            r0 = 0
            com.sun.mail.imap.protocol.Namespaces r1 = r4.namespaces     // Catch:{ all -> 0x004e }
            if (r1 != 0) goto L_0x004a
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r4.getStoreProtocol()     // Catch:{ BadCommandException -> 0x0041, ConnectionException -> 0x002c, ProtocolException -> 0x0021, all -> 0x001f }
            r0 = r1
            com.sun.mail.imap.protocol.Namespaces r1 = r0.namespace()     // Catch:{ BadCommandException -> 0x0041, ConnectionException -> 0x001d, ProtocolException -> 0x0021, all -> 0x001f }
            r4.namespaces = r1     // Catch:{ BadCommandException -> 0x0041, ConnectionException -> 0x001d, ProtocolException -> 0x0021, all -> 0x001f }
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x004e }
            if (r0 != 0) goto L_0x004a
            r4.cleanup()     // Catch:{ all -> 0x004e }
            goto L_0x004a
        L_0x001d:
            r1 = move-exception
            goto L_0x002d
        L_0x001f:
            r1 = move-exception
            goto L_0x0038
        L_0x0021:
            r1 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x0037 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x0037 }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x0037 }
            throw r2     // Catch:{ all -> 0x0037 }
        L_0x002c:
            r1 = move-exception
        L_0x002d:
            javax.mail.StoreClosedException r2 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0037 }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x0037 }
            r2.<init>(r4, r3)     // Catch:{ all -> 0x0037 }
            throw r2     // Catch:{ all -> 0x0037 }
        L_0x0037:
            r1 = move-exception
        L_0x0038:
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x004e }
            if (r0 != 0) goto L_0x0040
            r4.cleanup()     // Catch:{ all -> 0x004e }
        L_0x0040:
            throw r1     // Catch:{ all -> 0x004e }
        L_0x0041:
            r1 = move-exception
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x004e }
            if (r0 != 0) goto L_0x004a
            r4.cleanup()     // Catch:{ all -> 0x004e }
        L_0x004a:
            com.sun.mail.imap.protocol.Namespaces r1 = r4.namespaces     // Catch:{ all -> 0x004e }
            monitor-exit(r4)
            return r1
        L_0x004e:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getNamespaces():com.sun.mail.imap.protocol.Namespaces");
    }

    private Folder[] namespaceToFolders(Namespaces.Namespace[] ns, String user2) {
        Folder[] fa = new Folder[ns.length];
        for (int i = 0; i < fa.length; i++) {
            String name2 = ns[i].prefix;
            boolean z = false;
            if (user2 == null) {
                int len = name2.length();
                if (len > 0 && name2.charAt(len - 1) == ns[i].delimiter) {
                    name2 = name2.substring(0, len - 1);
                }
            } else {
                name2 = String.valueOf(name2) + user2;
            }
            char c = ns[i].delimiter;
            if (user2 == null) {
                z = true;
            }
            fa[i] = new IMAPFolder(name2, c, this, z);
        }
        return fa;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001c, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001e, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0020, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0022, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002c, code lost:
        throw new javax.mail.MessagingException(r2.getMessage(), r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0041, code lost:
        r2 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        releaseStoreProtocol(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0045, code lost:
        if (r1 == null) goto L_0x0047;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0047, code lost:
        cleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004a, code lost:
        throw r2;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0020 A[ExcHandler: all (th java.lang.Throwable), PHI: r1 
      PHI: (r1v4 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r1v0 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r1v5 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0008, B:7:0x000d] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0022 A[ExcHandler: ProtocolException (r2v6 'pex' com.sun.mail.iap.ProtocolException A[CUSTOM_DECLARE]), PHI: r1 
      PHI: (r1v3 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r1v0 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r1v5 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0008, B:7:0x000d] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0008] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0047  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.mail.Quota[] getQuota(java.lang.String r6) throws javax.mail.MessagingException {
        /*
            r5 = this;
            monitor-enter(r5)
            r5.checkConnected()     // Catch:{ all -> 0x004b }
            r0 = 0
            javax.mail.Quota[] r0 = (javax.mail.Quota[]) r0     // Catch:{ all -> 0x004b }
            r1 = 0
            com.sun.mail.imap.protocol.IMAPProtocol r2 = r5.getStoreProtocol()     // Catch:{ BadCommandException -> 0x0038, ConnectionException -> 0x002d, ProtocolException -> 0x0022, all -> 0x0020 }
            r1 = r2
            javax.mail.Quota[] r2 = r1.getQuotaRoot(r6)     // Catch:{ BadCommandException -> 0x001e, ConnectionException -> 0x001c, ProtocolException -> 0x0022, all -> 0x0020 }
            r0 = r2
            r5.releaseStoreProtocol(r1)     // Catch:{ all -> 0x004b }
            if (r1 != 0) goto L_0x001a
            r5.cleanup()     // Catch:{ all -> 0x004b }
        L_0x001a:
            monitor-exit(r5)
            return r0
        L_0x001c:
            r2 = move-exception
            goto L_0x002e
        L_0x001e:
            r2 = move-exception
            goto L_0x0039
        L_0x0020:
            r2 = move-exception
            goto L_0x0042
        L_0x0022:
            r2 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x0041 }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x0041 }
            r3.<init>(r4, r2)     // Catch:{ all -> 0x0041 }
            throw r3     // Catch:{ all -> 0x0041 }
        L_0x002d:
            r2 = move-exception
        L_0x002e:
            javax.mail.StoreClosedException r3 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x0041 }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x0041 }
            r3.<init>(r5, r4)     // Catch:{ all -> 0x0041 }
            throw r3     // Catch:{ all -> 0x0041 }
        L_0x0038:
            r2 = move-exception
        L_0x0039:
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x0041 }
            java.lang.String r4 = "QUOTA not supported"
            r3.<init>(r4, r2)     // Catch:{ all -> 0x0041 }
            throw r3     // Catch:{ all -> 0x0041 }
        L_0x0041:
            r2 = move-exception
        L_0x0042:
            r5.releaseStoreProtocol(r1)     // Catch:{ all -> 0x004b }
            if (r1 != 0) goto L_0x004a
            r5.cleanup()     // Catch:{ all -> 0x004b }
        L_0x004a:
            throw r2     // Catch:{ all -> 0x004b }
        L_0x004b:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.getQuota(java.lang.String):javax.mail.Quota[]");
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0017, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0019, code lost:
        r1 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x001b, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x001d, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0027, code lost:
        throw new javax.mail.MessagingException(r1.getMessage(), r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003c, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        releaseStoreProtocol(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0040, code lost:
        if (r0 == null) goto L_0x0042;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0042, code lost:
        cleanup();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0045, code lost:
        throw r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x001b A[ExcHandler: all (th java.lang.Throwable), PHI: r0 
      PHI: (r0v4 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r0v0 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v5 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v5 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0005, B:7:0x000a, B:8:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0005] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x001d A[ExcHandler: ProtocolException (r1v6 'pex' com.sun.mail.iap.ProtocolException A[CUSTOM_DECLARE]), PHI: r0 
      PHI: (r0v3 'p' com.sun.mail.imap.protocol.IMAPProtocol) = (r0v0 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v5 'p' com.sun.mail.imap.protocol.IMAPProtocol), (r0v5 'p' com.sun.mail.imap.protocol.IMAPProtocol) binds: [B:4:0x0005, B:7:0x000a, B:8:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:4:0x0005] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0042  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setQuota(javax.mail.Quota r5) throws javax.mail.MessagingException {
        /*
            r4 = this;
            monitor-enter(r4)
            r4.checkConnected()     // Catch:{ all -> 0x0046 }
            r0 = 0
            com.sun.mail.imap.protocol.IMAPProtocol r1 = r4.getStoreProtocol()     // Catch:{ BadCommandException -> 0x0033, ConnectionException -> 0x0028, ProtocolException -> 0x001d, all -> 0x001b }
            r0 = r1
            r0.setQuota(r5)     // Catch:{ BadCommandException -> 0x0019, ConnectionException -> 0x0017, ProtocolException -> 0x001d, all -> 0x001b }
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x0046 }
            if (r0 != 0) goto L_0x0015
            r4.cleanup()     // Catch:{ all -> 0x0046 }
        L_0x0015:
            monitor-exit(r4)
            return
        L_0x0017:
            r1 = move-exception
            goto L_0x0029
        L_0x0019:
            r1 = move-exception
            goto L_0x0034
        L_0x001b:
            r1 = move-exception
            goto L_0x003d
        L_0x001d:
            r1 = move-exception
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x003c }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x003c }
            r2.<init>(r3, r1)     // Catch:{ all -> 0x003c }
            throw r2     // Catch:{ all -> 0x003c }
        L_0x0028:
            r1 = move-exception
        L_0x0029:
            javax.mail.StoreClosedException r2 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x003c }
            java.lang.String r3 = r1.getMessage()     // Catch:{ all -> 0x003c }
            r2.<init>(r4, r3)     // Catch:{ all -> 0x003c }
            throw r2     // Catch:{ all -> 0x003c }
        L_0x0033:
            r1 = move-exception
        L_0x0034:
            javax.mail.MessagingException r2 = new javax.mail.MessagingException     // Catch:{ all -> 0x003c }
            java.lang.String r3 = "QUOTA not supported"
            r2.<init>(r3, r1)     // Catch:{ all -> 0x003c }
            throw r2     // Catch:{ all -> 0x003c }
        L_0x003c:
            r1 = move-exception
        L_0x003d:
            r4.releaseStoreProtocol(r0)     // Catch:{ all -> 0x0046 }
            if (r0 != 0) goto L_0x0045
            r4.cleanup()     // Catch:{ all -> 0x0046 }
        L_0x0045:
            throw r1     // Catch:{ all -> 0x0046 }
        L_0x0046:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.setQuota(javax.mail.Quota):void");
    }

    private void checkConnected() {
        if (!this.connected) {
            super.setConnected(false);
            throw new IllegalStateException("Not connected");
        }
    }

    public void handleResponse(Response r) {
        if (r.isOK() || r.isNO() || r.isBAD() || r.isBYE()) {
            handleResponseCode(r);
        }
        if (r.isBYE()) {
            if (this.debug) {
                this.out.println("DEBUG: IMAPStore connection dead");
            }
            if (this.connected) {
                cleanup(r.isSynthetic());
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 7 */
    /* JADX WARNING: Code restructure failed: missing block: B:132:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:133:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r2 = r0.readIdleResponse();
        r3 = r7.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
        if (r2 == null) goto L_0x004c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0034, code lost:
        if (r0.processIdleResponse(r2) != false) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0037, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x003a, code lost:
        if (r7.enableImapEvents == false) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0040, code lost:
        if (r2.isUnTagged() == false) goto L_0x0027;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0042, code lost:
        notifyStoreListeners(1000, r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$20(r7.pool, 0);
        r7.pool.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0057, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:?, code lost:
        r2 = getMinIdleTime();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x005c, code lost:
        if (r2 <= 0) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        java.lang.Thread.sleep((long) r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0084, code lost:
        r3 = r7.pool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0086, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        com.sun.mail.imap.IMAPStore.ConnectionPool.access$18(r7.pool, (com.sun.mail.imap.protocol.IMAPProtocol) null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x008c, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x008d, code lost:
        releaseStoreProtocol(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0090, code lost:
        if (r0 != null) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0092, code lost:
        cleanup();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void idle() throws javax.mail.MessagingException {
        /*
            r7 = this;
            r0 = 0
            monitor-enter(r7)
            r7.checkConnected()     // Catch:{ all -> 0x00d5 }
            monitor-exit(r7)     // Catch:{ all -> 0x00d5 }
            r1 = 0
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r7.pool     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            monitor-enter(r2)     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            com.sun.mail.imap.protocol.IMAPProtocol r3 = r7.getStoreProtocol()     // Catch:{ all -> 0x0099 }
            r0 = r3
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ all -> 0x0099 }
            int r3 = r3.idleState     // Catch:{ all -> 0x0099 }
            if (r3 != 0) goto L_0x007c
            r0.idleStart()     // Catch:{ all -> 0x0099 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ all -> 0x0099 }
            r4 = 1
            r3.idleState = r4     // Catch:{ all -> 0x0099 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ all -> 0x0099 }
            r3.idleProtocol = r0     // Catch:{ all -> 0x0099 }
            monitor-exit(r2)     // Catch:{ all -> 0x0099 }
        L_0x0027:
            com.sun.mail.iap.Response r2 = r0.readIdleResponse()     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            monitor-enter(r3)     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            if (r2 == 0) goto L_0x004c
            boolean r4 = r0.processIdleResponse(r2)     // Catch:{ all -> 0x0079 }
            if (r4 != 0) goto L_0x0037
            goto L_0x004c
        L_0x0037:
            monitor-exit(r3)     // Catch:{ all -> 0x0079 }
            boolean r3 = r7.enableImapEvents     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            if (r3 == 0) goto L_0x0027
            boolean r3 = r2.isUnTagged()     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            if (r3 == 0) goto L_0x0027
            r3 = 1000(0x3e8, float:1.401E-42)
            java.lang.String r4 = r2.toString()     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            r7.notifyStoreListeners(r3, r4)     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            goto L_0x0027
        L_0x004c:
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r7.pool     // Catch:{ all -> 0x0079 }
            r5 = 0
            r4.idleState = r5     // Catch:{ all -> 0x0079 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r7.pool     // Catch:{ all -> 0x0079 }
            r4.notifyAll()     // Catch:{ all -> 0x0079 }
            monitor-exit(r3)     // Catch:{ all -> 0x0079 }
            int r2 = r7.getMinIdleTime()     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
            if (r2 <= 0) goto L_0x0064
            long r3 = (long) r2
            java.lang.Thread.sleep(r3)     // Catch:{ InterruptedException -> 0x0063 }
            goto L_0x0064
        L_0x0063:
            r3 = move-exception
        L_0x0064:
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r7.pool
            monitor-enter(r2)
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ all -> 0x0076 }
            r3.idleProtocol = r1     // Catch:{ all -> 0x0076 }
            monitor-exit(r2)     // Catch:{ all -> 0x0076 }
            r7.releaseStoreProtocol(r0)
            if (r0 != 0) goto L_0x0075
            r7.cleanup()
        L_0x0075:
            return
        L_0x0076:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0076 }
            throw r1
        L_0x0079:
            r4 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0079 }
            throw r4     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
        L_0x007c:
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool     // Catch:{ InterruptedException -> 0x0082 }
            r3.wait()     // Catch:{ InterruptedException -> 0x0082 }
            goto L_0x0083
        L_0x0082:
            r3 = move-exception
        L_0x0083:
            monitor-exit(r2)     // Catch:{ all -> 0x0099 }
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool
            monitor-enter(r3)
            com.sun.mail.imap.IMAPStore$ConnectionPool r2 = r7.pool     // Catch:{ all -> 0x0096 }
            r2.idleProtocol = r1     // Catch:{ all -> 0x0096 }
            monitor-exit(r3)     // Catch:{ all -> 0x0096 }
            r7.releaseStoreProtocol(r0)
            if (r0 != 0) goto L_0x0095
            r7.cleanup()
        L_0x0095:
            return
        L_0x0096:
            r1 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x0096 }
            throw r1
        L_0x0099:
            r3 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0099 }
            throw r3     // Catch:{ BadCommandException -> 0x00b7, ConnectionException -> 0x00ac, ProtocolException -> 0x00a1 }
        L_0x009c:
            r2 = move-exception
            r6 = r2
            r2 = r0
            r0 = r6
            goto L_0x00c0
        L_0x00a1:
            r2 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x009c }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x009c }
            r3.<init>(r4, r2)     // Catch:{ all -> 0x009c }
            throw r3     // Catch:{ all -> 0x009c }
        L_0x00ac:
            r2 = move-exception
            javax.mail.StoreClosedException r3 = new javax.mail.StoreClosedException     // Catch:{ all -> 0x009c }
            java.lang.String r4 = r2.getMessage()     // Catch:{ all -> 0x009c }
            r3.<init>(r7, r4)     // Catch:{ all -> 0x009c }
            throw r3     // Catch:{ all -> 0x009c }
        L_0x00b7:
            r2 = move-exception
            javax.mail.MessagingException r3 = new javax.mail.MessagingException     // Catch:{ all -> 0x009c }
            java.lang.String r4 = "IDLE not supported"
            r3.<init>(r4, r2)     // Catch:{ all -> 0x009c }
            throw r3     // Catch:{ all -> 0x009c }
        L_0x00c0:
            com.sun.mail.imap.IMAPStore$ConnectionPool r3 = r7.pool
            monitor-enter(r3)
            com.sun.mail.imap.IMAPStore$ConnectionPool r4 = r7.pool     // Catch:{ all -> 0x00d2 }
            r4.idleProtocol = r1     // Catch:{ all -> 0x00d2 }
            monitor-exit(r3)     // Catch:{ all -> 0x00d2 }
            r7.releaseStoreProtocol(r2)
            if (r2 != 0) goto L_0x00d1
            r7.cleanup()
        L_0x00d1:
            throw r0
        L_0x00d2:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x00d2 }
            throw r0
        L_0x00d5:
            r1 = move-exception
            monitor-exit(r7)     // Catch:{ all -> 0x00d5 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.imap.IMAPStore.idle():void");
    }

    private void waitIfIdle() throws ProtocolException {
        while (this.pool.idleState != 0) {
            if (this.pool.idleState == 1) {
                this.pool.idleProtocol.idleAbort();
                this.pool.idleState = 2;
            }
            try {
                this.pool.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleResponseCode(Response r) {
        String s = r.getRest();
        boolean isAlert = false;
        if (s.startsWith("[")) {
            int i = s.indexOf(93);
            if (i > 0 && s.substring(0, i + 1).equalsIgnoreCase("[ALERT]")) {
                isAlert = true;
            }
            s = s.substring(i + 1).trim();
        }
        if (isAlert) {
            notifyStoreListeners(1, s);
        } else if (r.isUnTagged() && s.length() > 0) {
            notifyStoreListeners(2, s);
        }
    }
}
