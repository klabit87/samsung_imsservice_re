package com.sun.mail.pop3;

import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.SocketFetcher;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.StringTokenizer;

class Protocol {
    private static final String CRLF = "\r\n";
    private static final int POP3_PORT = 110;
    private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private String apopChallenge = null;
    private boolean debug = false;
    private DataInputStream input;
    private PrintStream out;
    private PrintWriter output;
    private Socket socket;

    Protocol(String host, int port, boolean debug2, PrintStream out2, Properties props, String prefix, boolean isSSL) throws IOException {
        String str = host;
        boolean z = debug2;
        PrintStream printStream = out2;
        Properties properties = props;
        boolean z2 = isSSL;
        boolean z3 = false;
        this.debug = z;
        this.out = printStream;
        String apop = properties.getProperty(String.valueOf(prefix) + ".apop.enable");
        if (apop != null && apop.equalsIgnoreCase(CloudMessageProviderContract.JsonData.TRUE)) {
            z3 = true;
        }
        boolean enableAPOP = z3;
        int port2 = port;
        port2 = port2 == -1 ? 110 : port2;
        if (z) {
            try {
                printStream.println("DEBUG POP3: connecting to host \"" + str + "\", port " + port2 + ", isSSL " + z2);
            } catch (IOException e) {
                e = e;
                String str2 = prefix;
            }
        }
        try {
            this.socket = SocketFetcher.getSocket(str, port2, properties, prefix, z2);
            this.input = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
            this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "iso-8859-1")));
            Response r = simpleCommand((String) null);
            if (!r.ok) {
                try {
                    this.socket.close();
                } catch (Throwable th) {
                }
                throw new IOException("Connect failed");
            } else if (enableAPOP) {
                int challStart = r.data.indexOf(60);
                int challEnd = r.data.indexOf(62, challStart);
                if (!(challStart == -1 || challEnd == -1)) {
                    this.apopChallenge = r.data.substring(challStart, challEnd + 1);
                }
                if (z) {
                    printStream.println("DEBUG POP3: APOP challenge: " + this.apopChallenge);
                }
            }
        } catch (IOException e2) {
            e = e2;
            IOException ioe = e;
            try {
                this.socket.close();
            } catch (Throwable th2) {
            }
            throw ioe;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
        if (this.socket != null) {
            quit();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004c, code lost:
        return r1.data != null ? r1.data : "USER command failed";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        return r1.data != null ? r1.data : "login failed";
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String login(java.lang.String r5, java.lang.String r6) throws java.io.IOException {
        /*
            r4 = this;
            monitor-enter(r4)
            r0 = 0
            java.lang.String r1 = r4.apopChallenge     // Catch:{ all -> 0x0072 }
            if (r1 == 0) goto L_0x000b
            java.lang.String r1 = r4.getDigest(r6)     // Catch:{ all -> 0x0072 }
            r0 = r1
        L_0x000b:
            java.lang.String r1 = r4.apopChallenge     // Catch:{ all -> 0x0072 }
            if (r1 == 0) goto L_0x002c
            if (r0 == 0) goto L_0x002c
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0072 }
            java.lang.String r2 = "APOP "
            r1.<init>(r2)     // Catch:{ all -> 0x0072 }
            r1.append(r5)     // Catch:{ all -> 0x0072 }
            java.lang.String r2 = " "
            r1.append(r2)     // Catch:{ all -> 0x0072 }
            r1.append(r0)     // Catch:{ all -> 0x0072 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0072 }
            com.sun.mail.pop3.Response r1 = r4.simpleCommand(r1)     // Catch:{ all -> 0x0072 }
            goto L_0x0060
        L_0x002c:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0072 }
            java.lang.String r2 = "USER "
            r1.<init>(r2)     // Catch:{ all -> 0x0072 }
            r1.append(r5)     // Catch:{ all -> 0x0072 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0072 }
            com.sun.mail.pop3.Response r1 = r4.simpleCommand(r1)     // Catch:{ all -> 0x0072 }
            boolean r2 = r1.ok     // Catch:{ all -> 0x0072 }
            if (r2 != 0) goto L_0x004d
            java.lang.String r2 = r1.data     // Catch:{ all -> 0x0072 }
            if (r2 == 0) goto L_0x0049
            java.lang.String r2 = r1.data     // Catch:{ all -> 0x0072 }
            goto L_0x004b
        L_0x0049:
            java.lang.String r2 = "USER command failed"
        L_0x004b:
            monitor-exit(r4)
            return r2
        L_0x004d:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0072 }
            java.lang.String r3 = "PASS "
            r2.<init>(r3)     // Catch:{ all -> 0x0072 }
            r2.append(r6)     // Catch:{ all -> 0x0072 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0072 }
            com.sun.mail.pop3.Response r2 = r4.simpleCommand(r2)     // Catch:{ all -> 0x0072 }
            r1 = r2
        L_0x0060:
            boolean r2 = r1.ok     // Catch:{ all -> 0x0072 }
            if (r2 != 0) goto L_0x006f
            java.lang.String r2 = r1.data     // Catch:{ all -> 0x0072 }
            if (r2 == 0) goto L_0x006b
            java.lang.String r2 = r1.data     // Catch:{ all -> 0x0072 }
            goto L_0x006d
        L_0x006b:
            java.lang.String r2 = "login failed"
        L_0x006d:
            monitor-exit(r4)
            return r2
        L_0x006f:
            r2 = 0
            monitor-exit(r4)
            return r2
        L_0x0072:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.pop3.Protocol.login(java.lang.String, java.lang.String):java.lang.String");
    }

    private String getDigest(String password) {
        try {
            return toHex(MessageDigest.getInstance("MD5").digest((String.valueOf(this.apopChallenge) + password).getBytes("iso-8859-1")));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e2) {
            return null;
        }
    }

    private static String toHex(byte[] bytes) {
        char[] result = new char[(bytes.length * 2)];
        int i = 0;
        for (byte b : bytes) {
            int temp = b & 255;
            int i2 = i + 1;
            char[] cArr = digits;
            result[i] = cArr[temp >> 4];
            i = i2 + 1;
            result[i2] = cArr[temp & 15];
        }
        return new String(result);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean quit() throws IOException {
        boolean ok;
        try {
            ok = simpleCommand("QUIT").ok;
            this.socket.close();
            this.socket = null;
            this.input = null;
            this.output = null;
        } catch (Throwable th) {
            th = th;
            this.socket = null;
            this.input = null;
            this.output = null;
            throw th;
        }
        return ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized Status stat() throws IOException {
        Status s;
        Response r = simpleCommand(DiagnosisConstants.PSCI_KEY_CALL_STATE);
        s = new Status();
        if (r.ok && r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                s.total = Integer.parseInt(st.nextToken());
                s.size = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public synchronized int list(int msg) throws IOException {
        int size;
        Response r = simpleCommand("LIST " + msg);
        size = -1;
        if (r.ok && r.data != null) {
            try {
                StringTokenizer st = new StringTokenizer(r.data);
                st.nextToken();
                size = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
            }
        }
        return size;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream list() throws IOException {
        return multilineCommand("LIST", 128).bytes;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream retr(int msg, int size) throws IOException {
        return multilineCommand("RETR " + msg, size).bytes;
    }

    /* access modifiers changed from: package-private */
    public synchronized InputStream top(int msg, int n) throws IOException {
        return multilineCommand("TOP " + msg + " " + n, 0).bytes;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean dele(int msg) throws IOException {
        return simpleCommand("DELE " + msg).ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized String uidl(int msg) throws IOException {
        Response r = simpleCommand("UIDL " + msg);
        if (!r.ok) {
            return null;
        }
        int i = r.data.indexOf(32);
        if (i <= 0) {
            return null;
        }
        return r.data.substring(i + 1);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean uidl(String[] uids) throws IOException {
        Response r = multilineCommand("UIDL", uids.length * 15);
        if (!r.ok) {
            return false;
        }
        LineInputStream lis = new LineInputStream(r.bytes);
        while (true) {
            String readLine = lis.readLine();
            String line = readLine;
            if (readLine == null) {
                return true;
            }
            int i = line.indexOf(32);
            if (i >= 1) {
                if (i < line.length()) {
                    int n = Integer.parseInt(line.substring(0, i));
                    if (n > 0 && n <= uids.length) {
                        uids[n - 1] = line.substring(i + 1);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean noop() throws IOException {
        return simpleCommand("NOOP").ok;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean rset() throws IOException {
        return simpleCommand("RSET").ok;
    }

    private Response simpleCommand(String cmd) throws IOException {
        if (this.socket != null) {
            if (cmd != null) {
                if (this.debug) {
                    PrintStream printStream = this.out;
                    printStream.println("C: " + cmd);
                }
                this.output.print(String.valueOf(cmd) + CRLF);
                this.output.flush();
            }
            String line = this.input.readLine();
            if (line == null) {
                if (this.debug) {
                    this.out.println("S: EOF");
                }
                throw new EOFException("EOF on socket");
            }
            if (this.debug) {
                PrintStream printStream2 = this.out;
                printStream2.println("S: " + line);
            }
            Response r = new Response();
            if (line.startsWith("+OK")) {
                r.ok = true;
            } else if (line.startsWith("-ERR")) {
                r.ok = false;
            } else {
                throw new IOException("Unexpected response: " + line);
            }
            int indexOf = line.indexOf(32);
            int i = indexOf;
            if (indexOf >= 0) {
                r.data = line.substring(i + 1);
            }
            return r;
        }
        throw new IOException("Folder is closed");
    }

    private Response multilineCommand(String cmd, int size) throws IOException {
        int b;
        Response r = simpleCommand(cmd);
        if (!r.ok) {
            return r;
        }
        SharedByteArrayOutputStream buf = new SharedByteArrayOutputStream(size);
        int lastb = 10;
        while (true) {
            int read = this.input.read();
            b = read;
            if (read < 0) {
                break;
            }
            if (lastb == 10 && b == 46) {
                if (this.debug) {
                    this.out.write(b);
                }
                b = this.input.read();
                if (b == 13) {
                    if (this.debug) {
                        this.out.write(b);
                    }
                    b = this.input.read();
                    if (this.debug) {
                        this.out.write(b);
                    }
                }
            }
            buf.write(b);
            if (this.debug) {
                this.out.write(b);
            }
            lastb = b;
        }
        if (b >= 0) {
            r.bytes = buf.toStream();
            return r;
        }
        throw new EOFException("EOF on socket");
    }
}
