package com.sun.mail.pop3;

import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class POP3Store extends Store {
    private int defaultPort;
    boolean disableTop;
    boolean forgetTopHeaders;
    private String host;
    private boolean isSSL;
    Constructor messageConstructor;
    private String name;
    private String passwd;
    private Protocol port;
    private int portNum;
    private POP3Folder portOwner;
    boolean rsetBeforeQuit;
    private String user;

    public POP3Store(Session session, URLName url) {
        this(session, url, "pop3", 110, false);
    }

    public POP3Store(Session session, URLName url, String name2, int defaultPort2, boolean isSSL2) {
        super(session, url);
        Class messageClass;
        this.name = "pop3";
        this.defaultPort = 110;
        this.isSSL = false;
        this.port = null;
        this.portOwner = null;
        this.host = null;
        this.portNum = -1;
        this.user = null;
        this.passwd = null;
        this.rsetBeforeQuit = false;
        this.disableTop = false;
        this.forgetTopHeaders = false;
        this.messageConstructor = null;
        name2 = url != null ? url.getProtocol() : name2;
        this.name = name2;
        this.defaultPort = defaultPort2;
        this.isSSL = isSSL2;
        String s = session.getProperty("mail." + name2 + ".rsetbeforequit");
        if (s != null && s.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            this.rsetBeforeQuit = true;
        }
        String s2 = session.getProperty("mail." + name2 + ".disabletop");
        if (s2 != null && s2.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            this.disableTop = true;
        }
        String s3 = session.getProperty("mail." + name2 + ".forgettopheaders");
        if (s3 != null && s3.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            this.forgetTopHeaders = true;
        }
        String s4 = session.getProperty("mail." + name2 + ".message.class");
        if (s4 != null) {
            if (session.getDebug()) {
                PrintStream debugOut = session.getDebugOut();
                debugOut.println("DEBUG: POP3 message class: " + s4);
            }
            try {
                try {
                    messageClass = getClass().getClassLoader().loadClass(s4);
                } catch (ClassNotFoundException e) {
                    messageClass = Class.forName(s4);
                }
                this.messageConstructor = messageClass.getConstructor(new Class[]{Folder.class, Integer.TYPE});
            } catch (Exception ex) {
                if (session.getDebug()) {
                    PrintStream debugOut2 = session.getDebugOut();
                    debugOut2.println("DEBUG: failed to load POP3 message class: " + ex);
                }
            }
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 4 */
    /* access modifiers changed from: protected */
    public synchronized boolean protocolConnect(String host2, int portNum2, String user2, String passwd2) throws MessagingException {
        if (host2 == null || passwd2 == null || user2 == null) {
            return false;
        }
        if (portNum2 == -1) {
            try {
                Session session = this.session;
                String portstring = session.getProperty("mail." + this.name + ".port");
                if (portstring != null) {
                    portNum2 = Integer.parseInt(portstring);
                }
            } catch (EOFException eex) {
                throw new AuthenticationFailedException(eex.getMessage());
            } catch (IOException ioex) {
                throw new MessagingException("Connect failed", ioex);
            } catch (Throwable th) {
                throw th;
            }
        }
        if (portNum2 == -1) {
            portNum2 = this.defaultPort;
        }
        this.host = host2;
        this.portNum = portNum2;
        this.user = user2;
        this.passwd = passwd2;
        this.port = getPort((POP3Folder) null);
        return true;
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:698)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:24:0x002a=Splitter:B:24:0x002a, B:14:0x001c=Splitter:B:14:0x001c} */
    public synchronized boolean isConnected() {
        /*
            r3 = this;
            monitor-enter(r3)
            boolean r0 = super.isConnected()     // Catch:{ all -> 0x0031 }
            r1 = 0
            if (r0 != 0) goto L_0x000a
            monitor-exit(r3)
            return r1
        L_0x000a:
            monitor-enter(r3)     // Catch:{ all -> 0x0031 }
            com.sun.mail.pop3.Protocol r0 = r3.port     // Catch:{ IOException -> 0x0022 }
            if (r0 != 0) goto L_0x0017
            r0 = 0
            com.sun.mail.pop3.Protocol r0 = r3.getPort(r0)     // Catch:{ IOException -> 0x0022 }
            r3.port = r0     // Catch:{ IOException -> 0x0022 }
            goto L_0x001c
        L_0x0017:
            com.sun.mail.pop3.Protocol r0 = r3.port     // Catch:{ IOException -> 0x0022 }
            r0.noop()     // Catch:{ IOException -> 0x0022 }
        L_0x001c:
            monitor-exit(r3)     // Catch:{ all -> 0x0020 }
            r0 = 1
            monitor-exit(r3)
            return r0
        L_0x0020:
            r0 = move-exception
            goto L_0x002d
        L_0x0022:
            r0 = move-exception
            super.close()     // Catch:{ MessagingException -> 0x0029, all -> 0x0027 }
            goto L_0x002a
        L_0x0027:
            r2 = move-exception
            goto L_0x002a
        L_0x0029:
            r2 = move-exception
        L_0x002a:
            monitor-exit(r3)     // Catch:{ all -> 0x0020 }
            monitor-exit(r3)
            return r1
        L_0x002d:
            monitor-exit(r3)     // Catch:{ all -> 0x002f }
            throw r0     // Catch:{ all -> 0x0031 }
        L_0x002f:
            r0 = move-exception
            goto L_0x002d
        L_0x0031:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Store.isConnected():boolean");
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005b, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sun.mail.pop3.Protocol getPort(com.sun.mail.pop3.POP3Folder r10) throws java.io.IOException {
        /*
            r9 = this;
            monitor-enter(r9)
            com.sun.mail.pop3.Protocol r0 = r9.port     // Catch:{ all -> 0x0069 }
            if (r0 == 0) goto L_0x000f
            com.sun.mail.pop3.POP3Folder r0 = r9.portOwner     // Catch:{ all -> 0x0069 }
            if (r0 != 0) goto L_0x000f
            r9.portOwner = r10     // Catch:{ all -> 0x0069 }
            com.sun.mail.pop3.Protocol r0 = r9.port     // Catch:{ all -> 0x0069 }
            monitor-exit(r9)
            return r0
        L_0x000f:
            com.sun.mail.pop3.Protocol r8 = new com.sun.mail.pop3.Protocol     // Catch:{ all -> 0x0069 }
            java.lang.String r1 = r9.host     // Catch:{ all -> 0x0069 }
            int r2 = r9.portNum     // Catch:{ all -> 0x0069 }
            javax.mail.Session r0 = r9.session     // Catch:{ all -> 0x0069 }
            boolean r3 = r0.getDebug()     // Catch:{ all -> 0x0069 }
            javax.mail.Session r0 = r9.session     // Catch:{ all -> 0x0069 }
            java.io.PrintStream r4 = r0.getDebugOut()     // Catch:{ all -> 0x0069 }
            javax.mail.Session r0 = r9.session     // Catch:{ all -> 0x0069 }
            java.util.Properties r5 = r0.getProperties()     // Catch:{ all -> 0x0069 }
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ all -> 0x0069 }
            java.lang.String r6 = "mail."
            r0.<init>(r6)     // Catch:{ all -> 0x0069 }
            java.lang.String r6 = r9.name     // Catch:{ all -> 0x0069 }
            r0.append(r6)     // Catch:{ all -> 0x0069 }
            java.lang.String r6 = r0.toString()     // Catch:{ all -> 0x0069 }
            boolean r7 = r9.isSSL     // Catch:{ all -> 0x0069 }
            r0 = r8
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)     // Catch:{ all -> 0x0069 }
            r0 = r8
            r1 = 0
            java.lang.String r2 = r9.user     // Catch:{ all -> 0x0069 }
            java.lang.String r3 = r9.passwd     // Catch:{ all -> 0x0069 }
            java.lang.String r2 = r0.login(r2, r3)     // Catch:{ all -> 0x0069 }
            r1 = r2
            if (r2 != 0) goto L_0x005c
            com.sun.mail.pop3.Protocol r2 = r9.port     // Catch:{ all -> 0x0069 }
            if (r2 != 0) goto L_0x0054
            if (r10 == 0) goto L_0x0054
            r9.port = r0     // Catch:{ all -> 0x0069 }
            r9.portOwner = r10     // Catch:{ all -> 0x0069 }
        L_0x0054:
            com.sun.mail.pop3.POP3Folder r2 = r9.portOwner     // Catch:{ all -> 0x0069 }
            if (r2 != 0) goto L_0x005a
            r9.portOwner = r10     // Catch:{ all -> 0x0069 }
        L_0x005a:
            monitor-exit(r9)
            return r0
        L_0x005c:
            r0.quit()     // Catch:{ IOException -> 0x0062, all -> 0x0060 }
            goto L_0x0063
        L_0x0060:
            r2 = move-exception
            goto L_0x0063
        L_0x0062:
            r2 = move-exception
        L_0x0063:
            java.io.EOFException r2 = new java.io.EOFException     // Catch:{ all -> 0x0069 }
            r2.<init>(r1)     // Catch:{ all -> 0x0069 }
            throw r2     // Catch:{ all -> 0x0069 }
        L_0x0069:
            r10 = move-exception
            monitor-exit(r9)
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.POP3Store.getPort(com.sun.mail.pop3.POP3Folder):com.sun.mail.pop3.Protocol");
    }

    /* access modifiers changed from: package-private */
    public synchronized void closePort(POP3Folder owner) {
        if (this.portOwner == owner) {
            this.port = null;
            this.portOwner = null;
        }
    }

    public synchronized void close() throws MessagingException {
        try {
            if (this.port != null) {
                try {
                    this.port.quit();
                } catch (IOException e) {
                } catch (Throwable th) {
                    th = th;
                    this.port = null;
                    super.close();
                    throw th;
                }
            }
            this.port = null;
            super.close();
        } catch (IOException e2) {
            this.port = null;
            super.close();
        } catch (Throwable th2) {
            throw th2;
        }
    }

    public Folder getDefaultFolder() throws MessagingException {
        checkConnected();
        return new DefaultFolder(this);
    }

    public Folder getFolder(String name2) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, name2);
    }

    public Folder getFolder(URLName url) throws MessagingException {
        checkConnected();
        return new POP3Folder(this, url.getFile());
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        if (this.port != null) {
            close();
        }
    }

    private void checkConnected() throws MessagingException {
        if (!super.isConnected()) {
            throw new MessagingException("Not connected");
        }
    }
}
