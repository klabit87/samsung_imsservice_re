package com.sun.mail.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SocketFetcher {
    private SocketFetcher() {
    }

    /* JADX WARNING: type inference failed for: r7v10, types: [java.lang.Throwable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d9 A[Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0142  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x018e  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0194  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x01a8 A[SYNTHETIC, Splitter:B:76:0x01a8] */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01b1  */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.net.Socket getSocket(java.lang.String r23, int r24, java.util.Properties r25, java.lang.String r26, boolean r27) throws java.io.IOException {
        /*
            if (r26 != 0) goto L_0x0007
            java.lang.String r0 = "socket"
            r1 = r0
            goto L_0x0009
        L_0x0007:
            r1 = r26
        L_0x0009:
            if (r25 != 0) goto L_0x0012
            java.util.Properties r0 = new java.util.Properties
            r0.<init>()
            r2 = r0
            goto L_0x0014
        L_0x0012:
            r2 = r25
        L_0x0014:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r3 = java.lang.String.valueOf(r1)
            r0.<init>(r3)
            java.lang.String r3 = ".connectiontimeout"
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            r3 = 0
            java.lang.String r4 = r2.getProperty(r0, r3)
            r5 = -1
            if (r4 == 0) goto L_0x0035
            int r0 = java.lang.Integer.parseInt(r4)     // Catch:{ NumberFormatException -> 0x0034 }
            r5 = r0
            goto L_0x0035
        L_0x0034:
            r0 = move-exception
        L_0x0035:
            r13 = 0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r6 = java.lang.String.valueOf(r1)
            r0.<init>(r6)
            java.lang.String r6 = ".timeout"
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r14 = r2.getProperty(r0, r3)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r6 = java.lang.String.valueOf(r1)
            r0.<init>(r6)
            java.lang.String r6 = ".localaddress"
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r15 = r2.getProperty(r0, r3)
            r0 = 0
            if (r15 == 0) goto L_0x006c
            java.net.InetAddress r0 = java.net.InetAddress.getByName(r15)
            r16 = r0
            goto L_0x006e
        L_0x006c:
            r16 = r0
        L_0x006e:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r6 = java.lang.String.valueOf(r1)
            r0.<init>(r6)
            java.lang.String r6 = ".localport"
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r17 = r2.getProperty(r0, r3)
            r6 = 0
            if (r17 == 0) goto L_0x0090
            int r0 = java.lang.Integer.parseInt(r17)     // Catch:{ NumberFormatException -> 0x008f }
            r6 = r0
            r18 = r6
            goto L_0x0092
        L_0x008f:
            r0 = move-exception
        L_0x0090:
            r18 = r6
        L_0x0092:
            r0 = 0
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            java.lang.String r7 = java.lang.String.valueOf(r1)
            r6.<init>(r7)
            java.lang.String r7 = ".socketFactory.fallback"
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            java.lang.String r6 = r2.getProperty(r6, r3)
            r12 = r6
            if (r12 == 0) goto L_0x00b6
            java.lang.String r6 = "false"
            boolean r6 = r12.equalsIgnoreCase(r6)
            if (r6 == 0) goto L_0x00b6
            r6 = 0
            goto L_0x00b7
        L_0x00b6:
            r6 = 1
        L_0x00b7:
            r19 = r6
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r6 = java.lang.String.valueOf(r1)
            r0.<init>(r6)
            java.lang.String r6 = ".socketFactory.class"
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = r2.getProperty(r0, r3)
            r11 = r0
            r6 = -1
            javax.net.SocketFactory r0 = getSocketFactory(r11)     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            r20 = r0
            if (r20 == 0) goto L_0x0134
            java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            java.lang.String r7 = java.lang.String.valueOf(r1)     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            r0.<init>(r7)     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            java.lang.String r7 = ".socketFactory.port"
            r0.append(r7)     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            java.lang.String r0 = r0.toString()     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            java.lang.String r0 = r2.getProperty(r0, r3)     // Catch:{ SocketTimeoutException -> 0x01b8, Exception -> 0x013c }
            r3 = r0
            if (r3 == 0) goto L_0x0104
            int r0 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x0103, SocketTimeoutException -> 0x00fd, Exception -> 0x00f8 }
            r6 = r0
            goto L_0x0104
        L_0x00f8:
            r0 = move-exception
            r3 = r11
            r22 = r12
            goto L_0x0140
        L_0x00fd:
            r0 = move-exception
            r3 = r11
            r22 = r12
            goto L_0x01bc
        L_0x0103:
            r0 = move-exception
        L_0x0104:
            r0 = -1
            if (r6 != r0) goto L_0x010c
            r0 = r24
            r21 = r0
            goto L_0x010e
        L_0x010c:
            r21 = r6
        L_0x010e:
            r6 = r16
            r7 = r18
            r8 = r23
            r9 = r21
            r10 = r5
            r25 = r3
            r3 = r11
            r11 = r20
            r22 = r12
            r12 = r27
            java.net.Socket r0 = createSocket(r6, r7, r8, r9, r10, r11, r12)     // Catch:{ SocketTimeoutException -> 0x012f, Exception -> 0x012b }
            r13 = r0
            r12 = r23
            goto L_0x0192
        L_0x012b:
            r0 = move-exception
            r6 = r21
            goto L_0x0140
        L_0x012f:
            r0 = move-exception
            r6 = r21
            goto L_0x01bc
        L_0x0134:
            r3 = r11
            r22 = r12
            r12 = r23
            r21 = r6
            goto L_0x0192
        L_0x013c:
            r0 = move-exception
            r3 = r11
            r22 = r12
        L_0x0140:
            if (r19 != 0) goto L_0x018e
            boolean r7 = r0 instanceof java.lang.reflect.InvocationTargetException
            if (r7 == 0) goto L_0x0155
            r7 = r0
            java.lang.reflect.InvocationTargetException r7 = (java.lang.reflect.InvocationTargetException) r7
            java.lang.Throwable r7 = r7.getTargetException()
            boolean r8 = r7 instanceof java.lang.Exception
            if (r8 == 0) goto L_0x0155
            r0 = r7
            java.lang.Exception r0 = (java.lang.Exception) r0
        L_0x0155:
            boolean r7 = r0 instanceof java.io.IOException
            if (r7 == 0) goto L_0x015d
            r7 = r0
            java.io.IOException r7 = (java.io.IOException) r7
            throw r7
        L_0x015d:
            java.io.IOException r7 = new java.io.IOException
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            java.lang.String r9 = "Couldn't connect using \""
            r8.<init>(r9)
            r8.append(r3)
            java.lang.String r9 = "\" socket factory to host, port: "
            r8.append(r9)
            r12 = r23
            r8.append(r12)
            java.lang.String r9 = ", "
            r8.append(r9)
            r8.append(r6)
            java.lang.String r9 = "; Exception: "
            r8.append(r9)
            r8.append(r0)
            java.lang.String r8 = r8.toString()
            r7.<init>(r8)
            r7.initCause(r0)
            throw r7
        L_0x018e:
            r12 = r23
            r21 = r6
        L_0x0192:
            if (r13 != 0) goto L_0x01a5
            r11 = 0
            r6 = r16
            r7 = r18
            r8 = r23
            r9 = r24
            r10 = r5
            r12 = r27
            java.net.Socket r13 = createSocket(r6, r7, r8, r9, r10, r11, r12)
        L_0x01a5:
            r6 = -1
            if (r14 == 0) goto L_0x01af
            int r0 = java.lang.Integer.parseInt(r14)     // Catch:{ NumberFormatException -> 0x01ae }
            r6 = r0
            goto L_0x01af
        L_0x01ae:
            r0 = move-exception
        L_0x01af:
            if (r6 < 0) goto L_0x01b4
            r13.setSoTimeout(r6)
        L_0x01b4:
            configureSSLSocket(r13, r2, r1)
            return r13
        L_0x01b8:
            r0 = move-exception
            r3 = r11
            r22 = r12
        L_0x01bc:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.SocketFetcher.getSocket(java.lang.String, int, java.util.Properties, java.lang.String, boolean):java.net.Socket");
    }

    public static Socket getSocket(String host, int port, Properties props, String prefix) throws IOException {
        return getSocket(host, port, props, prefix, false);
    }

    private static Socket createSocket(InetAddress localaddr, int localport, String host, int port, int cto, SocketFactory sf, boolean useSSL) throws IOException {
        Socket socket;
        if (sf != null) {
            socket = sf.createSocket();
        } else if (useSSL) {
            socket = SSLSocketFactory.getDefault().createSocket();
        } else {
            socket = new Socket();
        }
        if (localaddr != null) {
            socket.bind(new InetSocketAddress(localaddr, localport));
        }
        if (cto >= 0) {
            socket.connect(new InetSocketAddress(host, port), cto);
        } else {
            socket.connect(new InetSocketAddress(host, port));
        }
        return socket;
    }

    private static SocketFactory getSocketFactory(String sfClass) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (sfClass == null || sfClass.length() == 0) {
            return null;
        }
        ClassLoader cl = getContextClassLoader();
        Class clsSockFact = null;
        if (cl != null) {
            try {
                clsSockFact = cl.loadClass(sfClass);
            } catch (ClassNotFoundException e) {
            }
        }
        if (clsSockFact == null) {
            clsSockFact = Class.forName(sfClass);
        }
        return (SocketFactory) clsSockFact.getMethod("getDefault", new Class[0]).invoke(new Object(), new Object[0]);
    }

    public static Socket startTLS(Socket socket) throws IOException {
        return startTLS(socket, new Properties(), "socket");
    }

    /* JADX WARNING: type inference failed for: r4v7, types: [java.lang.Throwable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.net.Socket startTLS(java.net.Socket r7, java.util.Properties r8, java.lang.String r9) throws java.io.IOException {
        /*
            java.net.InetAddress r0 = r7.getInetAddress()
            java.lang.String r1 = r0.getHostName()
            int r2 = r7.getPort()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0042 }
            java.lang.String r4 = java.lang.String.valueOf(r9)     // Catch:{ Exception -> 0x0042 }
            r3.<init>(r4)     // Catch:{ Exception -> 0x0042 }
            java.lang.String r4 = ".socketFactory.class"
            r3.append(r4)     // Catch:{ Exception -> 0x0042 }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0042 }
            r4 = 0
            java.lang.String r3 = r8.getProperty(r3, r4)     // Catch:{ Exception -> 0x0042 }
            javax.net.SocketFactory r4 = getSocketFactory(r3)     // Catch:{ Exception -> 0x0042 }
            if (r4 == 0) goto L_0x0032
            boolean r5 = r4 instanceof javax.net.ssl.SSLSocketFactory     // Catch:{ Exception -> 0x0042 }
            if (r5 == 0) goto L_0x0032
            r5 = r4
            javax.net.ssl.SSLSocketFactory r5 = (javax.net.ssl.SSLSocketFactory) r5     // Catch:{ Exception -> 0x0042 }
            goto L_0x0038
        L_0x0032:
            javax.net.SocketFactory r5 = javax.net.ssl.SSLSocketFactory.getDefault()     // Catch:{ Exception -> 0x0042 }
            javax.net.ssl.SSLSocketFactory r5 = (javax.net.ssl.SSLSocketFactory) r5     // Catch:{ Exception -> 0x0042 }
        L_0x0038:
            r6 = 1
            java.net.Socket r6 = r5.createSocket(r7, r1, r2, r6)     // Catch:{ Exception -> 0x0042 }
            r7 = r6
            configureSSLSocket(r7, r8, r9)     // Catch:{ Exception -> 0x0042 }
            return r7
        L_0x0042:
            r3 = move-exception
            boolean r4 = r3 instanceof java.lang.reflect.InvocationTargetException
            if (r4 == 0) goto L_0x0056
            r4 = r3
            java.lang.reflect.InvocationTargetException r4 = (java.lang.reflect.InvocationTargetException) r4
            java.lang.Throwable r4 = r4.getTargetException()
            boolean r5 = r4 instanceof java.lang.Exception
            if (r5 == 0) goto L_0x0056
            r3 = r4
            java.lang.Exception r3 = (java.lang.Exception) r3
        L_0x0056:
            boolean r4 = r3 instanceof java.io.IOException
            if (r4 == 0) goto L_0x005e
            r4 = r3
            java.io.IOException r4 = (java.io.IOException) r4
            throw r4
        L_0x005e:
            java.io.IOException r4 = new java.io.IOException
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            java.lang.String r6 = "Exception in startTLS: host "
            r5.<init>(r6)
            r5.append(r1)
            java.lang.String r6 = ", port "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = "; Exception: "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r5 = r5.toString()
            r4.<init>(r5)
            r4.initCause(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.SocketFetcher.startTLS(java.net.Socket, java.util.Properties, java.lang.String):java.net.Socket");
    }

    private static void configureSSLSocket(Socket socket, Properties props, String prefix) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslsocket = (SSLSocket) socket;
            String protocols = props.getProperty(String.valueOf(prefix) + ".ssl.protocols", (String) null);
            if (protocols != null) {
                sslsocket.setEnabledProtocols(stringArray(protocols));
            } else {
                sslsocket.setEnabledProtocols(new String[]{"TLSv1"});
            }
            String ciphers = props.getProperty(String.valueOf(prefix) + ".ssl.ciphersuites", (String) null);
            if (ciphers != null) {
                sslsocket.setEnabledCipherSuites(stringArray(ciphers));
            }
        }
    }

    private static String[] stringArray(String s) {
        StringTokenizer st = new StringTokenizer(s);
        List tokens = new ArrayList();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return (String[]) tokens.toArray(new String[tokens.size()]);
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
}
