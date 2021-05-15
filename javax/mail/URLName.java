package javax.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Locale;

public class URLName {
    static final int caseDiff = 32;
    private static boolean doEncode;
    static BitSet dontNeedEncoding = new BitSet(256);
    private String file;
    protected String fullURL;
    private int hashCode;
    private String host;
    private InetAddress hostAddress;
    private boolean hostAddressKnown;
    private String password;
    private int port;
    private String protocol;
    private String ref;
    private String username;

    static {
        boolean z = true;
        doEncode = true;
        try {
            if (Boolean.getBoolean("mail.URLName.dontencode")) {
                z = false;
            }
            doEncode = z;
        } catch (Exception e) {
        }
        for (int i = 97; i <= 122; i++) {
            dontNeedEncoding.set(i);
        }
        for (int i2 = 65; i2 <= 90; i2++) {
            dontNeedEncoding.set(i2);
        }
        for (int i3 = 48; i3 <= 57; i3++) {
            dontNeedEncoding.set(i3);
        }
        dontNeedEncoding.set(32);
        dontNeedEncoding.set(45);
        dontNeedEncoding.set(95);
        dontNeedEncoding.set(46);
        dontNeedEncoding.set(42);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0040  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0039  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public URLName(java.lang.String r5, java.lang.String r6, int r7, java.lang.String r8, java.lang.String r9, java.lang.String r10) {
        /*
            r4 = this;
            r4.<init>()
            r0 = 0
            r4.hostAddressKnown = r0
            r1 = -1
            r4.port = r1
            r4.hashCode = r0
            r4.protocol = r5
            r4.host = r6
            r4.port = r7
            if (r8 == 0) goto L_0x002b
            r2 = 35
            int r2 = r8.indexOf(r2)
            r3 = r2
            if (r2 == r1) goto L_0x002b
            java.lang.String r0 = r8.substring(r0, r3)
            r4.file = r0
            int r0 = r3 + 1
            java.lang.String r0 = r8.substring(r0)
            r4.ref = r0
            goto L_0x0030
        L_0x002b:
            r4.file = r8
            r0 = 0
            r4.ref = r0
        L_0x0030:
            boolean r0 = doEncode
            if (r0 == 0) goto L_0x0039
            java.lang.String r0 = encode(r9)
            goto L_0x003a
        L_0x0039:
            r0 = r9
        L_0x003a:
            r4.username = r0
            boolean r0 = doEncode
            if (r0 == 0) goto L_0x0045
            java.lang.String r0 = encode(r10)
            goto L_0x0046
        L_0x0045:
            r0 = r10
        L_0x0046:
            r4.password = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.URLName.<init>(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.lang.String):void");
    }

    public URLName(URL url) {
        this(url.toString());
    }

    public URLName(String url) {
        this.hostAddressKnown = false;
        this.port = -1;
        this.hashCode = 0;
        parseString(url);
    }

    public String toString() {
        if (this.fullURL == null) {
            StringBuffer tempURL = new StringBuffer();
            String str = this.protocol;
            if (str != null) {
                tempURL.append(str);
                tempURL.append(":");
            }
            if (!(this.username == null && this.host == null)) {
                tempURL.append("//");
                String str2 = this.username;
                if (str2 != null) {
                    tempURL.append(str2);
                    if (this.password != null) {
                        tempURL.append(":");
                        tempURL.append(this.password);
                    }
                    tempURL.append("@");
                }
                String str3 = this.host;
                if (str3 != null) {
                    tempURL.append(str3);
                }
                if (this.port != -1) {
                    tempURL.append(":");
                    tempURL.append(Integer.toString(this.port));
                }
                if (this.file != null) {
                    tempURL.append("/");
                }
            }
            String str4 = this.file;
            if (str4 != null) {
                tempURL.append(str4);
            }
            if (this.ref != null) {
                tempURL.append("#");
                tempURL.append(this.ref);
            }
            this.fullURL = tempURL.toString();
        }
        return this.fullURL;
    }

    /* access modifiers changed from: protected */
    public void parseString(String url) {
        String fullhost;
        int portindex;
        this.password = null;
        this.username = null;
        this.host = null;
        this.ref = null;
        this.file = null;
        this.protocol = null;
        this.port = -1;
        int len = url.length();
        int protocolEnd = url.indexOf(58);
        if (protocolEnd != -1) {
            this.protocol = url.substring(0, protocolEnd);
        }
        if (url.regionMatches(protocolEnd + 1, "//", 0, 2)) {
            int fileStart = url.indexOf(47, protocolEnd + 3);
            if (fileStart != -1) {
                fullhost = url.substring(protocolEnd + 3, fileStart);
                if (fileStart + 1 < len) {
                    this.file = url.substring(fileStart + 1);
                } else {
                    this.file = "";
                }
            } else {
                fullhost = url.substring(protocolEnd + 3);
            }
            int i = fullhost.indexOf(64);
            if (i != -1) {
                String fulluserpass = fullhost.substring(0, i);
                fullhost = fullhost.substring(i + 1);
                int passindex = fulluserpass.indexOf(58);
                if (passindex != -1) {
                    this.username = fulluserpass.substring(0, passindex);
                    this.password = fulluserpass.substring(passindex + 1);
                } else {
                    this.username = fulluserpass;
                }
            }
            if (fullhost.length() <= 0 || fullhost.charAt(0) != '[') {
                portindex = fullhost.indexOf(58);
            } else {
                portindex = fullhost.indexOf(58, fullhost.indexOf(93));
            }
            if (portindex != -1) {
                String portstring = fullhost.substring(portindex + 1);
                if (portstring.length() > 0) {
                    try {
                        this.port = Integer.parseInt(portstring);
                    } catch (NumberFormatException e) {
                        this.port = -1;
                    }
                }
                this.host = fullhost.substring(0, portindex);
            } else {
                this.host = fullhost;
            }
        } else if (protocolEnd + 1 < len) {
            this.file = url.substring(protocolEnd + 1);
        }
        String str = this.file;
        if (str != null) {
            int indexOf = str.indexOf(35);
            int refStart = indexOf;
            if (indexOf != -1) {
                this.ref = this.file.substring(refStart + 1);
                this.file = this.file.substring(0, refStart);
            }
        }
    }

    public int getPort() {
        return this.port;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getFile() {
        return this.file;
    }

    public String getRef() {
        return this.ref;
    }

    public String getHost() {
        return this.host;
    }

    public String getUsername() {
        return doEncode ? decode(this.username) : this.username;
    }

    public String getPassword() {
        return doEncode ? decode(this.password) : this.password;
    }

    public URL getURL() throws MalformedURLException {
        return new URL(getProtocol(), getHost(), getPort(), getFile());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0006, code lost:
        r0 = (javax.mail.URLName) r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(java.lang.Object r9) {
        /*
            r8 = this;
            boolean r0 = r9 instanceof javax.mail.URLName
            r1 = 0
            if (r0 != 0) goto L_0x0006
            return r1
        L_0x0006:
            r0 = r9
            javax.mail.URLName r0 = (javax.mail.URLName) r0
            java.lang.String r2 = r0.protocol
            if (r2 == 0) goto L_0x006b
            java.lang.String r3 = r8.protocol
            boolean r2 = r2.equals(r3)
            if (r2 != 0) goto L_0x0016
            goto L_0x006b
        L_0x0016:
            java.net.InetAddress r2 = r8.getHostAddress()
            java.net.InetAddress r3 = r0.getHostAddress()
            if (r2 == 0) goto L_0x0029
            if (r3 == 0) goto L_0x0029
            boolean r4 = r2.equals(r3)
            if (r4 != 0) goto L_0x003f
            return r1
        L_0x0029:
            java.lang.String r4 = r8.host
            if (r4 == 0) goto L_0x0038
            java.lang.String r5 = r0.host
            if (r5 == 0) goto L_0x0038
            boolean r4 = r4.equalsIgnoreCase(r5)
            if (r4 != 0) goto L_0x003f
            return r1
        L_0x0038:
            java.lang.String r4 = r8.host
            java.lang.String r5 = r0.host
            if (r4 == r5) goto L_0x003f
            return r1
        L_0x003f:
            java.lang.String r4 = r8.username
            java.lang.String r5 = r0.username
            if (r4 == r5) goto L_0x004e
            if (r4 == 0) goto L_0x004d
            boolean r4 = r4.equals(r5)
            if (r4 != 0) goto L_0x004e
        L_0x004d:
            return r1
        L_0x004e:
            java.lang.String r4 = r8.file
            java.lang.String r5 = ""
            if (r4 != 0) goto L_0x0055
            r4 = r5
        L_0x0055:
            java.lang.String r6 = r0.file
            if (r6 != 0) goto L_0x005a
            goto L_0x005b
        L_0x005a:
            r5 = r6
        L_0x005b:
            boolean r6 = r4.equals(r5)
            if (r6 != 0) goto L_0x0062
            return r1
        L_0x0062:
            int r6 = r8.port
            int r7 = r0.port
            if (r6 == r7) goto L_0x0069
            return r1
        L_0x0069:
            r1 = 1
            return r1
        L_0x006b:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.URLName.equals(java.lang.Object):boolean");
    }

    public int hashCode() {
        int i = this.hashCode;
        if (i != 0) {
            return i;
        }
        String str = this.protocol;
        if (str != null) {
            this.hashCode = i + str.hashCode();
        }
        InetAddress addr = getHostAddress();
        if (addr != null) {
            this.hashCode += addr.hashCode();
        } else {
            String str2 = this.host;
            if (str2 != null) {
                this.hashCode += str2.toLowerCase(Locale.ENGLISH).hashCode();
            }
        }
        String str3 = this.username;
        if (str3 != null) {
            this.hashCode += str3.hashCode();
        }
        String str4 = this.file;
        if (str4 != null) {
            this.hashCode += str4.hashCode();
        }
        int i2 = this.hashCode + this.port;
        this.hashCode = i2;
        return i2;
    }

    /* Debug info: failed to restart local var, previous not found, register: 2 */
    private synchronized InetAddress getHostAddress() {
        if (this.hostAddressKnown) {
            return this.hostAddress;
        } else if (this.host == null) {
            return null;
        } else {
            try {
                this.hostAddress = InetAddress.getByName(this.host);
            } catch (UnknownHostException e) {
                this.hostAddress = null;
            }
            this.hostAddressKnown = true;
            return this.hostAddress;
        }
    }

    static String encode(String s) {
        if (s == null) {
            return null;
        }
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (c == 32 || !dontNeedEncoding.get(c)) {
                return _encode(s);
            }
        }
        return s;
    }

    private static String _encode(String s) {
        StringBuffer out = new StringBuffer(s.length());
        ByteArrayOutputStream buf = new ByteArrayOutputStream(10);
        OutputStreamWriter writer = new OutputStreamWriter(buf);
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (dontNeedEncoding.get(c)) {
                if (c == 32) {
                    c = 43;
                }
                out.append((char) c);
            } else {
                try {
                    writer.write(c);
                    writer.flush();
                    byte[] ba = buf.toByteArray();
                    for (int j = 0; j < ba.length; j++) {
                        out.append('%');
                        char ch = Character.forDigit((ba[j] >> 4) & 15, 16);
                        if (Character.isLetter(ch)) {
                            ch = (char) (ch - ' ');
                        }
                        out.append(ch);
                        char ch2 = Character.forDigit(ba[j] & 15, 16);
                        if (Character.isLetter(ch2)) {
                            ch2 = (char) (ch2 - ' ');
                        }
                        out.append(ch2);
                    }
                    buf.reset();
                } catch (IOException e) {
                    buf.reset();
                }
            }
        }
        return out.toString();
    }

    static String decode(String s) {
        if (s == null) {
            return null;
        }
        if (indexOfAny(s, "+%") == -1) {
            return s;
        }
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '%') {
                try {
                    sb.append((char) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    i += 2;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
            } else if (c != '+') {
                sb.append(c);
            } else {
                sb.append(' ');
            }
            i++;
        }
        String result = sb.toString();
        try {
            return new String(result.getBytes("8859_1"));
        } catch (UnsupportedEncodingException e2) {
            return result;
        }
    }

    private static int indexOfAny(String s, String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }
}
