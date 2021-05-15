package javax.mail;

import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sun.mail.util.LineInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.mail.Provider;

public final class Session {
    private static Session defaultSession = null;
    /* access modifiers changed from: private */
    public final Properties addressMap = new Properties();
    private final Hashtable authTable = new Hashtable();
    private final Authenticator authenticator;
    private boolean debug = false;
    private PrintStream out;
    private final Properties props;
    private final Vector providers = new Vector();
    private final Hashtable providersByClassName = new Hashtable();
    private final Hashtable providersByProtocol = new Hashtable();

    private Session(Properties props2, Authenticator authenticator2) {
        Class cl;
        this.props = props2;
        this.authenticator = authenticator2;
        if (Boolean.valueOf(props2.getProperty("mail.debug")).booleanValue()) {
            this.debug = true;
        }
        if (this.debug) {
            pr("DEBUG: JavaMail version 1.4.1");
        }
        if (authenticator2 != null) {
            cl = authenticator2.getClass();
        } else {
            cl = getClass();
        }
        loadProviders(cl);
        loadAddressMap(cl);
    }

    public static Session getInstance(Properties props2, Authenticator authenticator2) {
        return new Session(props2, authenticator2);
    }

    public static Session getInstance(Properties props2) {
        return new Session(props2, (Authenticator) null);
    }

    public static synchronized Session getDefaultInstance(Properties props2, Authenticator authenticator2) {
        Session session;
        synchronized (Session.class) {
            if (defaultSession == null) {
                defaultSession = new Session(props2, authenticator2);
            } else if (defaultSession.authenticator != authenticator2) {
                if (defaultSession.authenticator == null || authenticator2 == null || defaultSession.authenticator.getClass().getClassLoader() != authenticator2.getClass().getClassLoader()) {
                    throw new SecurityException("Access to default session denied");
                }
            }
            session = defaultSession;
        }
        return session;
    }

    public static Session getDefaultInstance(Properties props2) {
        return getDefaultInstance(props2, (Authenticator) null);
    }

    public synchronized void setDebug(boolean debug2) {
        this.debug = debug2;
        if (debug2) {
            pr("DEBUG: setDebug: JavaMail version 1.4.1");
        }
    }

    public synchronized boolean getDebug() {
        return this.debug;
    }

    public synchronized void setDebugOut(PrintStream out2) {
        this.out = out2;
    }

    public synchronized PrintStream getDebugOut() {
        if (this.out == null) {
            return System.out;
        }
        return this.out;
    }

    public synchronized Provider[] getProviders() {
        Provider[] _providers;
        _providers = new Provider[this.providers.size()];
        this.providers.copyInto(_providers);
        return _providers;
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0074, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized javax.mail.Provider getProvider(java.lang.String r6) throws javax.mail.NoSuchProviderException {
        /*
            r5 = this;
            monitor-enter(r5)
            if (r6 == 0) goto L_0x0089
            int r0 = r6.length()     // Catch:{ all -> 0x0091 }
            if (r0 <= 0) goto L_0x0089
            r0 = 0
            java.util.Properties r1 = r5.props     // Catch:{ all -> 0x0091 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = "mail."
            r2.<init>(r3)     // Catch:{ all -> 0x0091 }
            r2.append(r6)     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = ".class"
            r2.append(r3)     // Catch:{ all -> 0x0091 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0091 }
            java.lang.String r1 = r1.getProperty(r2)     // Catch:{ all -> 0x0091 }
            if (r1 == 0) goto L_0x004b
            boolean r2 = r5.debug     // Catch:{ all -> 0x0091 }
            if (r2 == 0) goto L_0x0042
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = "DEBUG: mail."
            r2.<init>(r3)     // Catch:{ all -> 0x0091 }
            r2.append(r6)     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = ".class property exists and points to "
            r2.append(r3)     // Catch:{ all -> 0x0091 }
            r2.append(r1)     // Catch:{ all -> 0x0091 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0091 }
            r5.pr(r2)     // Catch:{ all -> 0x0091 }
        L_0x0042:
            java.util.Hashtable r2 = r5.providersByClassName     // Catch:{ all -> 0x0091 }
            java.lang.Object r2 = r2.get(r1)     // Catch:{ all -> 0x0091 }
            javax.mail.Provider r2 = (javax.mail.Provider) r2     // Catch:{ all -> 0x0091 }
            r0 = r2
        L_0x004b:
            if (r0 == 0) goto L_0x004f
            monitor-exit(r5)
            return r0
        L_0x004f:
            java.util.Hashtable r2 = r5.providersByProtocol     // Catch:{ all -> 0x0091 }
            java.lang.Object r2 = r2.get(r6)     // Catch:{ all -> 0x0091 }
            javax.mail.Provider r2 = (javax.mail.Provider) r2     // Catch:{ all -> 0x0091 }
            r0 = r2
            if (r0 == 0) goto L_0x0075
            boolean r2 = r5.debug     // Catch:{ all -> 0x0091 }
            if (r2 == 0) goto L_0x0073
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = "DEBUG: getProvider() returning "
            r2.<init>(r3)     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = r0.toString()     // Catch:{ all -> 0x0091 }
            r2.append(r3)     // Catch:{ all -> 0x0091 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0091 }
            r5.pr(r2)     // Catch:{ all -> 0x0091 }
        L_0x0073:
            monitor-exit(r5)
            return r0
        L_0x0075:
            javax.mail.NoSuchProviderException r2 = new javax.mail.NoSuchProviderException     // Catch:{ all -> 0x0091 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0091 }
            java.lang.String r4 = "No provider for "
            r3.<init>(r4)     // Catch:{ all -> 0x0091 }
            r3.append(r6)     // Catch:{ all -> 0x0091 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0091 }
            r2.<init>(r3)     // Catch:{ all -> 0x0091 }
            throw r2     // Catch:{ all -> 0x0091 }
        L_0x0089:
            javax.mail.NoSuchProviderException r0 = new javax.mail.NoSuchProviderException     // Catch:{ all -> 0x0091 }
            java.lang.String r1 = "Invalid protocol: null"
            r0.<init>(r1)     // Catch:{ all -> 0x0091 }
            throw r0     // Catch:{ all -> 0x0091 }
        L_0x0091:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.Session.getProvider(java.lang.String):javax.mail.Provider");
    }

    public synchronized void setProvider(Provider provider) throws NoSuchProviderException {
        if (provider != null) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
            Properties properties = this.props;
            properties.put("mail." + provider.getProtocol() + ".class", provider.getClassName());
        } else {
            throw new NoSuchProviderException("Can't set null provider");
        }
    }

    public Store getStore() throws NoSuchProviderException {
        return getStore(getProperty("mail.store.protocol"));
    }

    public Store getStore(String protocol) throws NoSuchProviderException {
        return getStore(new URLName(protocol, (String) null, -1, (String) null, (String) null, (String) null));
    }

    public Store getStore(URLName url) throws NoSuchProviderException {
        return getStore(getProvider(url.getProtocol()), url);
    }

    public Store getStore(Provider provider) throws NoSuchProviderException {
        return getStore(provider, (URLName) null);
    }

    private Store getStore(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.STORE) {
            throw new NoSuchProviderException("invalid provider");
        }
        try {
            return (Store) getService(provider, url);
        } catch (ClassCastException e) {
            throw new NoSuchProviderException("incorrect class");
        }
    }

    public Folder getFolder(URLName url) throws MessagingException {
        Store store = getStore(url);
        store.connect();
        return store.getFolder(url);
    }

    public Transport getTransport() throws NoSuchProviderException {
        return getTransport(getProperty("mail.transport.protocol"));
    }

    public Transport getTransport(String protocol) throws NoSuchProviderException {
        return getTransport(new URLName(protocol, (String) null, -1, (String) null, (String) null, (String) null));
    }

    public Transport getTransport(URLName url) throws NoSuchProviderException {
        return getTransport(getProvider(url.getProtocol()), url);
    }

    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        return getTransport(provider, (URLName) null);
    }

    public Transport getTransport(Address address) throws NoSuchProviderException {
        String transportProtocol = (String) this.addressMap.get(address.getType());
        if (transportProtocol != null) {
            return getTransport(transportProtocol);
        }
        throw new NoSuchProviderException("No provider for Address type: " + address.getType());
    }

    private Transport getTransport(Provider provider, URLName url) throws NoSuchProviderException {
        if (provider == null || provider.getType() != Provider.Type.TRANSPORT) {
            throw new NoSuchProviderException("invalid provider");
        }
        try {
            return (Transport) getService(provider, url);
        } catch (ClassCastException e) {
            throw new NoSuchProviderException("incorrect class");
        }
    }

    private Object getService(Provider provider, URLName url) throws NoSuchProviderException {
        ClassLoader cl;
        if (provider != null) {
            if (url == null) {
                url = new URLName(provider.getProtocol(), (String) null, -1, (String) null, (String) null, (String) null);
            }
            Authenticator authenticator2 = this.authenticator;
            if (authenticator2 != null) {
                cl = authenticator2.getClass().getClassLoader();
            } else {
                cl = getClass().getClassLoader();
            }
            Class serviceClass = null;
            try {
                ClassLoader ccl = getContextClassLoader();
                if (ccl != null) {
                    try {
                        serviceClass = ccl.loadClass(provider.getClassName());
                    } catch (ClassNotFoundException e) {
                    }
                }
                if (serviceClass == null) {
                    serviceClass = cl.loadClass(provider.getClassName());
                }
            } catch (Exception e2) {
                try {
                    serviceClass = Class.forName(provider.getClassName());
                } catch (Exception ex) {
                    if (this.debug) {
                        ex.printStackTrace(getDebugOut());
                    }
                    throw new NoSuchProviderException(provider.getProtocol());
                }
            }
            try {
                return serviceClass.getConstructor(new Class[]{Session.class, URLName.class}).newInstance(new Object[]{this, url});
            } catch (Exception ex2) {
                if (this.debug) {
                    ex2.printStackTrace(getDebugOut());
                }
                throw new NoSuchProviderException(provider.getProtocol());
            }
        } else {
            throw new NoSuchProviderException("null");
        }
    }

    public void setPasswordAuthentication(URLName url, PasswordAuthentication pw) {
        if (pw == null) {
            this.authTable.remove(url);
        } else {
            this.authTable.put(url, pw);
        }
    }

    public PasswordAuthentication getPasswordAuthentication(URLName url) {
        return (PasswordAuthentication) this.authTable.get(url);
    }

    public PasswordAuthentication requestPasswordAuthentication(InetAddress addr, int port, String protocol, String prompt, String defaultUserName) {
        Authenticator authenticator2 = this.authenticator;
        if (authenticator2 != null) {
            return authenticator2.requestPasswordAuthentication(addr, port, protocol, prompt, defaultUserName);
        }
        return null;
    }

    public Properties getProperties() {
        return this.props;
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    private void loadProviders(Class cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.loadProvidersFromStream(is);
            }
        };
        try {
            loadFile(String.valueOf(System.getProperty("java.home")) + File.separator + "lib" + File.separator + "javamail.providers", loader);
        } catch (SecurityException sex) {
            if (this.debug) {
                pr("DEBUG: can't get java.home: " + sex);
            }
        }
        loadAllResources("META-INF/javamail.providers", cl, loader);
        loadResource("/META-INF/javamail.default.providers", cl, loader);
        if (this.providers.size() == 0) {
            if (this.debug) {
                pr("DEBUG: failed to load any providers, using defaults");
            }
            addProvider(new Provider(Provider.Type.STORE, "imap", "com.sun.mail.imap.IMAPStore", "Sun Microsystems, Inc.", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "imaps", "com.sun.mail.imap.IMAPSSLStore", "Sun Microsystems, Inc.", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "pop3", "com.sun.mail.pop3.POP3Store", "Sun Microsystems, Inc.", Version.version));
            addProvider(new Provider(Provider.Type.STORE, "pop3s", "com.sun.mail.pop3.POP3SSLStore", "Sun Microsystems, Inc.", Version.version));
            addProvider(new Provider(Provider.Type.TRANSPORT, "smtp", "com.sun.mail.smtp.SMTPTransport", "Sun Microsystems, Inc.", Version.version));
            addProvider(new Provider(Provider.Type.TRANSPORT, "smtps", "com.sun.mail.smtp.SMTPSSLTransport", "Sun Microsystems, Inc.", Version.version));
        }
        if (this.debug) {
            pr("DEBUG: Tables of loaded providers");
            pr("DEBUG: Providers Listed By Class Name: " + this.providersByClassName.toString());
            pr("DEBUG: Providers Listed By Protocol: " + this.providersByProtocol.toString());
        }
    }

    /* access modifiers changed from: private */
    public void loadProvidersFromStream(InputStream is) throws IOException {
        InputStream inputStream = is;
        if (inputStream != null) {
            LineInputStream lis = new LineInputStream(inputStream);
            while (true) {
                String readLine = lis.readLine();
                String currLine = readLine;
                if (readLine != null) {
                    if (!currLine.startsWith("#")) {
                        Provider.Type type = null;
                        String protocol = null;
                        String className = null;
                        String vendor2 = null;
                        String version = null;
                        StringTokenizer tuples = new StringTokenizer(currLine, ";");
                        while (tuples.hasMoreTokens()) {
                            String currTuple = tuples.nextToken().trim();
                            int sep = currTuple.indexOf(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                            if (currTuple.startsWith("protocol=")) {
                                protocol = currTuple.substring(sep + 1);
                            } else if (currTuple.startsWith("type=")) {
                                String strType = currTuple.substring(sep + 1);
                                if (strType.equalsIgnoreCase("store")) {
                                    type = Provider.Type.STORE;
                                } else if (strType.equalsIgnoreCase("transport")) {
                                    type = Provider.Type.TRANSPORT;
                                }
                            } else if (currTuple.startsWith("class=")) {
                                className = currTuple.substring(sep + 1);
                            } else if (currTuple.startsWith("vendor=")) {
                                vendor2 = currTuple.substring(sep + 1);
                            } else if (currTuple.startsWith("version=")) {
                                version = currTuple.substring(sep + 1);
                            }
                        }
                        if (type != null && protocol != null && className != null && protocol.length() > 0 && className.length() > 0) {
                            addProvider(new Provider(type, protocol, className, vendor2, version));
                        } else if (this.debug) {
                            pr("DEBUG: Bad provider entry: " + currLine);
                        }
                    }
                } else {
                    return;
                }
            }
        }
    }

    public synchronized void addProvider(Provider provider) {
        this.providers.addElement(provider);
        this.providersByClassName.put(provider.getClassName(), provider);
        if (!this.providersByProtocol.containsKey(provider.getProtocol())) {
            this.providersByProtocol.put(provider.getProtocol(), provider);
        }
    }

    private void loadAddressMap(Class cl) {
        StreamLoader loader = new StreamLoader() {
            public void load(InputStream is) throws IOException {
                Session.this.addressMap.load(is);
            }
        };
        loadResource("/META-INF/javamail.default.address.map", cl, loader);
        loadAllResources("META-INF/javamail.address.map", cl, loader);
        try {
            loadFile(String.valueOf(System.getProperty("java.home")) + File.separator + "lib" + File.separator + "javamail.address.map", loader);
        } catch (SecurityException sex) {
            if (this.debug) {
                pr("DEBUG: can't get java.home: " + sex);
            }
        }
        if (this.addressMap.isEmpty()) {
            if (this.debug) {
                pr("DEBUG: failed to load address map, using defaults");
            }
            this.addressMap.put("rfc822", "smtp");
        }
    }

    public synchronized void setProtocolForAddress(String addresstype, String protocol) {
        if (protocol == null) {
            this.addressMap.remove(addresstype);
        } else {
            this.addressMap.put(addresstype, protocol);
        }
    }

    private void loadFile(String name, StreamLoader loader) {
        InputStream clis = null;
        try {
            InputStream clis2 = new BufferedInputStream(new FileInputStream(name));
            loader.load(clis2);
            if (this.debug) {
                pr("DEBUG: successfully loaded file: " + name);
            }
            try {
                clis2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            if (this.debug) {
                pr("DEBUG: not loading file: " + name);
                pr("DEBUG: " + e2);
            }
            if (clis != null) {
                clis.close();
            }
        } catch (SecurityException sex) {
            if (this.debug) {
                pr("DEBUG: not loading file: " + name);
                pr("DEBUG: " + sex);
            }
            if (clis != null) {
                clis.close();
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    private void loadResource(String name, Class cl, StreamLoader loader) {
        InputStream clis = null;
        try {
            InputStream clis2 = getResourceAsStream(cl, name);
            if (clis2 != null) {
                loader.load(clis2);
                if (this.debug) {
                    pr("DEBUG: successfully loaded resource: " + name);
                }
            } else if (this.debug) {
                pr("DEBUG: not loading resource: " + name);
            }
            if (clis2 != null) {
                try {
                    clis2.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            if (this.debug) {
                pr("DEBUG: " + e2);
            }
            if (clis != null) {
                clis.close();
            }
        } catch (SecurityException sex) {
            if (this.debug) {
                pr("DEBUG: " + sex);
            }
            if (clis != null) {
                clis.close();
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private void loadAllResources(String name, Class cl, StreamLoader loader) {
        URL[] urls;
        InputStream clis;
        boolean anyLoaded = false;
        try {
            ClassLoader cld = getContextClassLoader();
            if (cld == null) {
                cld = cl.getClassLoader();
            }
            if (cld != null) {
                urls = getResources(cld, name);
            } else {
                urls = getSystemResources(name);
            }
            if (urls != null) {
                for (URL url : urls) {
                    clis = null;
                    if (this.debug) {
                        pr("DEBUG: URL " + url);
                    }
                    try {
                        InputStream clis2 = openStream(url);
                        if (clis2 != null) {
                            loader.load(clis2);
                            anyLoaded = true;
                            if (this.debug) {
                                pr("DEBUG: successfully loaded resource: " + url);
                            }
                        } else if (this.debug) {
                            pr("DEBUG: not loading resource: " + url);
                        }
                        if (clis2 != null) {
                            try {
                                clis2.close();
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException ioex) {
                        if (this.debug) {
                            pr("DEBUG: " + ioex);
                        }
                        if (clis != null) {
                            clis.close();
                        }
                    } catch (SecurityException sex) {
                        if (this.debug) {
                            pr("DEBUG: " + sex);
                        }
                        if (clis != null) {
                            clis.close();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (this.debug) {
                pr("DEBUG: " + ex);
            }
        } catch (Throwable th) {
            if (clis != null) {
                try {
                    clis.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        if (!anyLoaded) {
            if (this.debug) {
                pr("DEBUG: !anyLoaded");
            }
            loadResource("/" + name, cl, loader);
        }
    }

    private void pr(String str) {
        getDebugOut().println(str);
    }

    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Thread.currentThread().getContextClassLoader();
                } catch (SecurityException e) {
                    return null;
                }
            }
        });
    }

    private static InputStream getResourceAsStream(final Class c, final String name) throws IOException {
        try {
            return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return c.getResourceAsStream(name);
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((IOException) e.getException());
        }
    }

    private static URL[] getResources(final ClassLoader cl, final String name) {
        return (URL[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                URL[] ret = null;
                try {
                    Vector v = new Vector();
                    Enumeration e = cl.getResources(name);
                    while (true) {
                        if (e == null) {
                            break;
                        } else if (!e.hasMoreElements()) {
                            break;
                        } else {
                            URL url = e.nextElement();
                            if (url != null) {
                                v.addElement(url);
                            }
                        }
                    }
                    if (v.size() <= 0) {
                        return ret;
                    }
                    URL[] ret2 = new URL[v.size()];
                    v.copyInto(ret2);
                    return ret2;
                } catch (IOException | SecurityException e2) {
                    return ret;
                }
            }
        });
    }

    private static URL[] getSystemResources(final String name) {
        return (URL[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                URL[] ret = null;
                try {
                    Vector v = new Vector();
                    Enumeration e = ClassLoader.getSystemResources(name);
                    while (true) {
                        if (e == null) {
                            break;
                        } else if (!e.hasMoreElements()) {
                            break;
                        } else {
                            URL url = e.nextElement();
                            if (url != null) {
                                v.addElement(url);
                            }
                        }
                    }
                    if (v.size() <= 0) {
                        return ret;
                    }
                    URL[] ret2 = new URL[v.size()];
                    v.copyInto(ret2);
                    return ret2;
                } catch (IOException | SecurityException e2) {
                    return ret;
                }
            }
        });
    }

    private static InputStream openStream(final URL url) throws IOException {
        try {
            return (InputStream) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IOException {
                    return url.openStream();
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((IOException) e.getException());
        }
    }
}
