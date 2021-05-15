package javax.mail.internet;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import javax.mail.Address;
import javax.mail.Session;

public class InternetAddress extends Address implements Cloneable {
    private static final String rfc822phrase = HeaderTokenizer.RFC822.replace(' ', 0).replace(9, 0);
    private static final long serialVersionUID = -7507595530758302903L;
    private static final String specialsNoDot = "()<>,;:\\\"[]@";
    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";
    protected String address;
    protected String encodedPersonal;
    protected String personal;

    public InternetAddress() {
    }

    public InternetAddress(String address2) throws AddressException {
        InternetAddress[] a = parse(address2, true);
        if (a.length == 1) {
            this.address = a[0].address;
            this.personal = a[0].personal;
            this.encodedPersonal = a[0].encodedPersonal;
            return;
        }
        throw new AddressException("Illegal address", address2);
    }

    public InternetAddress(String address2, boolean strict) throws AddressException {
        this(address2);
        if (strict) {
            checkAddress(this.address, true, true);
        }
    }

    public InternetAddress(String address2, String personal2) throws UnsupportedEncodingException {
        this(address2, personal2, (String) null);
    }

    public InternetAddress(String address2, String personal2, String charset) throws UnsupportedEncodingException {
        this.address = address2;
        setPersonal(personal2, charset);
    }

    public Object clone() {
        try {
            return (InternetAddress) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getType() {
        return "rfc822";
    }

    public void setAddress(String address2) {
        this.address = address2;
    }

    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name, charset, (String) null);
        } else {
            this.encodedPersonal = null;
        }
    }

    public void setPersonal(String name) throws UnsupportedEncodingException {
        this.personal = name;
        if (name != null) {
            this.encodedPersonal = MimeUtility.encodeWord(name);
        } else {
            this.encodedPersonal = null;
        }
    }

    public String getAddress() {
        return this.address;
    }

    public String getPersonal() {
        String str = this.personal;
        if (str != null) {
            return str;
        }
        String str2 = this.encodedPersonal;
        if (str2 == null) {
            return null;
        }
        try {
            String decodeText = MimeUtility.decodeText(str2);
            this.personal = decodeText;
            return decodeText;
        } catch (Exception e) {
            return this.encodedPersonal;
        }
    }

    public String toString() {
        String str;
        if (this.encodedPersonal == null && (str = this.personal) != null) {
            try {
                this.encodedPersonal = MimeUtility.encodeWord(str);
            } catch (UnsupportedEncodingException e) {
            }
        }
        String str2 = this.encodedPersonal;
        if (str2 != null) {
            return String.valueOf(quotePhrase(str2)) + " <" + this.address + ">";
        } else if (isGroup() || isSimple()) {
            return this.address;
        } else {
            return "<" + this.address + ">";
        }
    }

    public String toUnicodeString() {
        String p = getPersonal();
        if (p != null) {
            return String.valueOf(quotePhrase(p)) + " <" + this.address + ">";
        } else if (isGroup() || isSimple()) {
            return this.address;
        } else {
            return "<" + this.address + ">";
        }
    }

    private static String quotePhrase(String phrase) {
        int len = phrase.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; i++) {
            char c = phrase.charAt(i);
            if (c == '\"' || c == '\\') {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('\"');
                for (int j = 0; j < len; j++) {
                    char cc = phrase.charAt(j);
                    if (cc == '\"' || cc == '\\') {
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('\"');
                return sb.toString();
            }
            if ((c < ' ' && c != 13 && c != 10 && c != 9) || c >= 127 || rfc822phrase.indexOf(c) >= 0) {
                needQuoting = true;
            }
        }
        if (!needQuoting) {
            return phrase;
        }
        StringBuffer sb2 = new StringBuffer(len + 2);
        sb2.append('\"');
        sb2.append(phrase);
        sb2.append('\"');
        return sb2.toString();
    }

    private static String unquote(String s) {
        if (!s.startsWith("\"") || !s.endsWith("\"")) {
            return s;
        }
        String s2 = s.substring(1, s.length() - 1);
        if (s2.indexOf(92) < 0) {
            return s2;
        }
        StringBuffer sb = new StringBuffer(s2.length());
        int i = 0;
        while (i < s2.length()) {
            char c = s2.charAt(i);
            if (c == '\\' && i < s2.length() - 1) {
                i++;
                c = s2.charAt(i);
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    public boolean equals(Object a) {
        if (!(a instanceof InternetAddress)) {
            return false;
        }
        String s = ((InternetAddress) a).getAddress();
        String str = this.address;
        if (s == str) {
            return true;
        }
        if (str == null || !str.equalsIgnoreCase(s)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        String str = this.address;
        if (str == null) {
            return 0;
        }
        return str.toLowerCase(Locale.ENGLISH).hashCode();
    }

    public static String toString(Address[] addresses) {
        return toString(addresses, 0);
    }

    public static String toString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0) {
                sb.append(", ");
                used += 2;
            }
            String s = addresses[i].toString();
            if (used + lengthOfFirstSegment(s) > 76) {
                sb.append("\r\n\t");
                used = 8;
            }
            sb.append(s);
            used = lengthOfLastSegment(s, used);
        }
        return sb.toString();
    }

    private static int lengthOfFirstSegment(String s) {
        int indexOf = s.indexOf("\r\n");
        int pos = indexOf;
        if (indexOf != -1) {
            return pos;
        }
        return s.length();
    }

    private static int lengthOfLastSegment(String s, int used) {
        int lastIndexOf = s.lastIndexOf("\r\n");
        int pos = lastIndexOf;
        if (lastIndexOf != -1) {
            return (s.length() - pos) - 2;
        }
        return s.length() + used;
    }

    public static InternetAddress getLocalAddress(Session session) {
        InetAddress me;
        String user = null;
        String host = null;
        String address2 = null;
        if (session == null) {
            try {
                user = System.getProperty("user.name");
                host = InetAddress.getLocalHost().getHostName();
            } catch (SecurityException | UnknownHostException | AddressException e) {
                return null;
            }
        } else {
            address2 = session.getProperty("mail.from");
            if (address2 == null) {
                user = session.getProperty("mail.user");
                if (user == null || user.length() == 0) {
                    user = session.getProperty("user.name");
                }
                if (user == null || user.length() == 0) {
                    user = System.getProperty("user.name");
                }
                host = session.getProperty("mail.host");
                if ((host == null || host.length() == 0) && (me = InetAddress.getLocalHost()) != null) {
                    host = me.getHostName();
                }
            }
        }
        if (!(address2 != null || user == null || user.length() == 0 || host == null || host.length() == 0)) {
            address2 = String.valueOf(user) + "@" + host;
        }
        if (address2 != null) {
            return new InternetAddress(address2);
        }
        return null;
    }

    public static InternetAddress[] parse(String addresslist) throws AddressException {
        return parse(addresslist, true);
    }

    public static InternetAddress[] parse(String addresslist, boolean strict) throws AddressException {
        return parse(addresslist, strict, false);
    }

    public static InternetAddress[] parseHeader(String addresslist, boolean strict) throws AddressException {
        return parse(addresslist, strict, true);
    }

    /* JADX WARNING: Removed duplicated region for block: B:127:0x01a8  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x0251 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x01ab A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x00e1 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x0258 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x00f1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static javax.mail.internet.InternetAddress[] parse(java.lang.String r19, boolean r20, boolean r21) throws javax.mail.internet.AddressException {
        /*
            r0 = r19
            r1 = -1
            r2 = -1
            int r3 = r19.length()
            r4 = 0
            r5 = 0
            r6 = 0
            java.util.Vector r7 = new java.util.Vector
            r7.<init>()
            r8 = -1
            r9 = r8
            r10 = r8
            r11 = 0
        L_0x0014:
            r12 = 0
            if (r11 < r3) goto L_0x0078
            if (r10 < 0) goto L_0x006e
            if (r9 != r8) goto L_0x001c
            r9 = r11
        L_0x001c:
            java.lang.String r8 = r0.substring(r10, r9)
            java.lang.String r8 = r8.trim()
            if (r6 != 0) goto L_0x004b
            if (r20 != 0) goto L_0x004b
            if (r21 == 0) goto L_0x002b
            goto L_0x004b
        L_0x002b:
            java.util.StringTokenizer r13 = new java.util.StringTokenizer
            r13.<init>(r8)
        L_0x0031:
            boolean r14 = r13.hasMoreTokens()
            if (r14 != 0) goto L_0x0038
            goto L_0x006e
        L_0x0038:
            java.lang.String r14 = r13.nextToken()
            checkAddress(r14, r12, r12)
            javax.mail.internet.InternetAddress r15 = new javax.mail.internet.InternetAddress
            r15.<init>()
            r15.setAddress(r14)
            r7.addElement(r15)
            goto L_0x0031
        L_0x004b:
            if (r20 != 0) goto L_0x004f
            if (r21 != 0) goto L_0x0052
        L_0x004f:
            checkAddress(r8, r5, r12)
        L_0x0052:
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            r12.setAddress(r8)
            if (r1 < 0) goto L_0x006b
            java.lang.String r13 = r0.substring(r1, r2)
            java.lang.String r13 = r13.trim()
            java.lang.String r13 = unquote(r13)
            r12.encodedPersonal = r13
        L_0x006b:
            r7.addElement(r12)
        L_0x006e:
            int r8 = r7.size()
            javax.mail.internet.InternetAddress[] r8 = new javax.mail.internet.InternetAddress[r8]
            r7.copyInto(r8)
            return r8
        L_0x0078:
            char r13 = r0.charAt(r11)
            r14 = 9
            if (r13 == r14) goto L_0x0257
            r14 = 10
            if (r13 == r14) goto L_0x0257
            r14 = 13
            if (r13 == r14) goto L_0x0257
            r14 = 32
            if (r13 == r14) goto L_0x0257
            java.lang.String r14 = "Missing '\"'"
            r12 = 34
            if (r13 == r12) goto L_0x022f
            r15 = 44
            if (r13 == r15) goto L_0x01c1
            r15 = 62
            if (r13 == r15) goto L_0x01b9
            r15 = 91
            if (r13 == r15) goto L_0x0192
            r15 = 41
            r12 = 40
            if (r13 == r12) goto L_0x0154
            if (r13 == r15) goto L_0x014c
            switch(r13) {
                case 58: goto L_0x013b;
                case 59: goto L_0x010f;
                case 60: goto L_0x00ae;
                default: goto L_0x00a9;
            }
        L_0x00a9:
            if (r10 != r8) goto L_0x0258
            r10 = r11
            goto L_0x0258
        L_0x00ae:
            r6 = 1
            if (r5 != 0) goto L_0x0107
            if (r4 != 0) goto L_0x00be
            r1 = r10
            if (r1 < 0) goto L_0x00b7
            r2 = r11
        L_0x00b7:
            int r10 = r11 + 1
            r12 = r1
            r15 = r2
            r18 = r10
            goto L_0x00c2
        L_0x00be:
            r12 = r1
            r15 = r2
            r18 = r10
        L_0x00c2:
            r1 = 0
            int r11 = r11 + 1
        L_0x00c5:
            if (r11 < r3) goto L_0x00c8
            goto L_0x00df
        L_0x00c8:
            char r13 = r0.charAt(r11)
            r2 = 34
            if (r13 == r2) goto L_0x00fb
            r2 = 62
            if (r13 == r2) goto L_0x00dc
            r10 = 92
            if (r13 == r10) goto L_0x00d9
            goto L_0x0104
        L_0x00d9:
            int r11 = r11 + 1
            goto L_0x0104
        L_0x00dc:
            if (r1 == 0) goto L_0x00df
            goto L_0x0104
        L_0x00df:
            if (r11 < r3) goto L_0x00f1
            if (r1 == 0) goto L_0x00e9
            javax.mail.internet.AddressException r2 = new javax.mail.internet.AddressException
            r2.<init>(r14, r0, r11)
            throw r2
        L_0x00e9:
            javax.mail.internet.AddressException r2 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Missing '>'"
            r2.<init>(r8, r0, r11)
            throw r2
        L_0x00f1:
            r2 = 1
            r5 = r11
            r9 = r5
            r1 = r12
            r10 = r18
            r5 = r2
            r2 = r15
            goto L_0x0258
        L_0x00fb:
            r2 = 62
            if (r1 == 0) goto L_0x0101
            r10 = 0
            goto L_0x0102
        L_0x0101:
            r10 = 1
        L_0x0102:
            r1 = r10
        L_0x0104:
            r10 = 1
            int r11 = r11 + r10
            goto L_0x00c5
        L_0x0107:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Extra route-addr"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x010f:
            if (r10 != r8) goto L_0x0112
            r10 = r11
        L_0x0112:
            if (r4 == 0) goto L_0x0132
            r4 = 0
            if (r10 != r8) goto L_0x0118
            r10 = r11
        L_0x0118:
            javax.mail.internet.InternetAddress r12 = new javax.mail.internet.InternetAddress
            r12.<init>()
            int r9 = r11 + 1
            java.lang.String r14 = r0.substring(r10, r9)
            java.lang.String r14 = r14.trim()
            r12.setAddress(r14)
            r7.addElement(r12)
            r5 = 0
            r9 = r8
            r10 = r8
            goto L_0x0258
        L_0x0132:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Illegal semicolon, not in group"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x013b:
            r6 = 1
            if (r4 != 0) goto L_0x0144
            r4 = 1
            if (r10 != r8) goto L_0x0258
            r10 = r11
            goto L_0x0258
        L_0x0144:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Nested group"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x014c:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Missing '('"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x0154:
            r6 = 1
            if (r10 < 0) goto L_0x015a
            if (r9 != r8) goto L_0x015a
            r9 = r11
        L_0x015a:
            if (r1 != r8) goto L_0x015e
            int r1 = r11 + 1
        L_0x015e:
            int r11 = r11 + 1
            r14 = 1
        L_0x0161:
            if (r11 >= r3) goto L_0x0181
            if (r14 > 0) goto L_0x0166
            goto L_0x0181
        L_0x0166:
            char r13 = r0.charAt(r11)
            if (r13 == r12) goto L_0x0179
            if (r13 == r15) goto L_0x0176
            r12 = 92
            if (r13 == r12) goto L_0x0173
            goto L_0x017c
        L_0x0173:
            int r11 = r11 + 1
            goto L_0x017c
        L_0x0176:
            int r14 = r14 + -1
            goto L_0x017c
        L_0x0179:
            int r14 = r14 + 1
        L_0x017c:
            r12 = 1
            int r11 = r11 + r12
            r12 = 40
            goto L_0x0161
        L_0x0181:
            if (r14 > 0) goto L_0x018a
            int r11 = r11 + -1
            if (r2 != r8) goto L_0x0258
            r2 = r11
            goto L_0x0258
        L_0x018a:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Missing ')'"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x0192:
            r12 = 1
            int r11 = r11 + 1
        L_0x0195:
            if (r11 < r3) goto L_0x0198
            goto L_0x01a6
        L_0x0198:
            char r13 = r0.charAt(r11)
            r6 = 92
            if (r13 == r6) goto L_0x01b3
            r6 = 93
            if (r13 == r6) goto L_0x01a5
            goto L_0x01b6
        L_0x01a5:
        L_0x01a6:
            if (r11 >= r3) goto L_0x01ab
            r6 = r12
            goto L_0x0258
        L_0x01ab:
            javax.mail.internet.AddressException r6 = new javax.mail.internet.AddressException
            java.lang.String r8 = "Missing ']'"
            r6.<init>(r8, r0, r11)
            throw r6
        L_0x01b3:
            int r11 = r11 + 1
        L_0x01b6:
            r6 = 1
            int r11 = r11 + r6
            goto L_0x0195
        L_0x01b9:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            java.lang.String r12 = "Missing '<'"
            r8.<init>(r12, r0, r11)
            throw r8
        L_0x01c1:
            if (r10 != r8) goto L_0x01c9
            r5 = 0
            r6 = 0
            r9 = r8
            r10 = r8
            goto L_0x0258
        L_0x01c9:
            if (r4 == 0) goto L_0x01ce
            r5 = 0
            goto L_0x0258
        L_0x01ce:
            if (r9 != r8) goto L_0x01d1
            r9 = r11
        L_0x01d1:
            java.lang.String r12 = r0.substring(r10, r9)
            java.lang.String r12 = r12.trim()
            if (r6 != 0) goto L_0x0202
            if (r20 != 0) goto L_0x0202
            if (r21 == 0) goto L_0x01e0
            goto L_0x0202
        L_0x01e0:
            java.util.StringTokenizer r14 = new java.util.StringTokenizer
            r14.<init>(r12)
        L_0x01e6:
            boolean r15 = r14.hasMoreTokens()
            if (r15 != 0) goto L_0x01ed
            goto L_0x0229
        L_0x01ed:
            java.lang.String r15 = r14.nextToken()
            r8 = 0
            checkAddress(r15, r8, r8)
            javax.mail.internet.InternetAddress r8 = new javax.mail.internet.InternetAddress
            r8.<init>()
            r8.setAddress(r15)
            r7.addElement(r8)
            r8 = -1
            goto L_0x01e6
        L_0x0202:
            if (r20 != 0) goto L_0x0206
            if (r21 != 0) goto L_0x020a
        L_0x0206:
            r8 = 0
            checkAddress(r12, r5, r8)
        L_0x020a:
            javax.mail.internet.InternetAddress r8 = new javax.mail.internet.InternetAddress
            r8.<init>()
            r8.setAddress(r12)
            if (r1 < 0) goto L_0x0226
            java.lang.String r14 = r0.substring(r1, r2)
            java.lang.String r14 = r14.trim()
            java.lang.String r14 = unquote(r14)
            r8.encodedPersonal = r14
            r14 = -1
            r2 = r14
            r1 = r14
        L_0x0226:
            r7.addElement(r8)
        L_0x0229:
            r5 = 0
            r6 = 0
            r8 = -1
            r9 = r8
            r10 = r8
            goto L_0x0258
        L_0x022f:
            r6 = 1
            if (r10 != r8) goto L_0x0233
            r10 = r11
        L_0x0233:
            int r11 = r11 + 1
        L_0x0235:
            if (r11 < r3) goto L_0x0238
            goto L_0x024e
        L_0x0238:
            char r13 = r0.charAt(r11)
            r12 = 34
            if (r13 == r12) goto L_0x024d
            r15 = 92
            if (r13 == r15) goto L_0x0245
            goto L_0x0248
        L_0x0245:
            int r11 = r11 + 1
        L_0x0248:
            r16 = 1
            int r11 = r11 + 1
            goto L_0x0235
        L_0x024d:
        L_0x024e:
            if (r11 >= r3) goto L_0x0251
            goto L_0x0258
        L_0x0251:
            javax.mail.internet.AddressException r8 = new javax.mail.internet.AddressException
            r8.<init>(r14, r0, r11)
            throw r8
        L_0x0257:
        L_0x0258:
            r12 = 1
            int r11 = r11 + r12
            goto L_0x0014
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.mail.internet.InternetAddress.parse(java.lang.String, boolean, boolean):javax.mail.internet.InternetAddress[]");
    }

    public void validate() throws AddressException {
        checkAddress(getAddress(), true, true);
    }

    private static void checkAddress(String addr, boolean routeAddr, boolean validate) throws AddressException {
        String domain;
        String local;
        int start = 0;
        if (addr.indexOf(34) < 0) {
            if (routeAddr) {
                start = 0;
                while (true) {
                    int indexOfAny = indexOfAny(addr, ",:", start);
                    int i = indexOfAny;
                    if (indexOfAny < 0) {
                        break;
                    } else if (addr.charAt(start) != '@') {
                        throw new AddressException("Illegal route-addr", addr);
                    } else if (addr.charAt(i) == ':') {
                        start = i + 1;
                        break;
                    } else {
                        start = i + 1;
                    }
                }
            }
            int indexOf = addr.indexOf(64, start);
            int i2 = indexOf;
            if (indexOf >= 0) {
                if (i2 == start) {
                    throw new AddressException("Missing local name", addr);
                } else if (i2 != addr.length() - 1) {
                    local = addr.substring(start, i2);
                    domain = addr.substring(i2 + 1);
                } else {
                    throw new AddressException("Missing domain", addr);
                }
            } else if (!validate) {
                local = addr;
                domain = null;
            } else {
                throw new AddressException("Missing final '@domain'", addr);
            }
            if (indexOfAny(addr, " \t\n\r") >= 0) {
                throw new AddressException("Illegal whitespace in address", addr);
            } else if (indexOfAny(local, specialsNoDot) >= 0) {
                throw new AddressException("Illegal character in local name", addr);
            } else if (domain != null && domain.indexOf(91) < 0 && indexOfAny(domain, specialsNoDot) >= 0) {
                throw new AddressException("Illegal character in domain", addr);
            }
        }
    }

    private boolean isSimple() {
        String str = this.address;
        return str == null || indexOfAny(str, specialsNoDotNoAt) < 0;
    }

    public boolean isGroup() {
        String str = this.address;
        return str != null && str.endsWith(";") && this.address.indexOf(58) > 0;
    }

    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        int ix;
        String addr = getAddress();
        if (addr.endsWith(";") && (ix = addr.indexOf(58)) >= 0) {
            return parseHeader(addr.substring(ix + 1, addr.length() - 1), strict);
        }
        return null;
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
