package org.xbill.DNS;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Address {
    public static final int IPv4 = 1;
    public static final int IPv6 = 2;

    private Address() {
    }

    private static byte[] parseV4(String s) {
        byte[] values = new byte[4];
        int length = s.length();
        int currentValue = 0;
        int currentValue2 = 0;
        int numDigits = 0;
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                if (c != '.' || currentValue == 3 || numDigits == 0) {
                    return null;
                }
                values[currentValue] = (byte) currentValue2;
                numDigits = 0;
                currentValue2 = 0;
                currentValue++;
            } else if (numDigits == 3) {
                return null;
            } else {
                if (numDigits > 0 && currentValue2 == 0) {
                    return null;
                }
                numDigits++;
                currentValue2 = (currentValue2 * 10) + (c - '0');
                if (currentValue2 > 255) {
                    return null;
                }
            }
        }
        if (currentValue != 3 || numDigits == 0) {
            return null;
        }
        values[currentValue] = (byte) currentValue2;
        return values;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00c5, code lost:
        if (r10 >= 16) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00c7, code lost:
        if (r0 >= 0) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00c9, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00ca, code lost:
        if (r0 < 0) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00cc, code lost:
        r1 = 16 - r10;
        java.lang.System.arraycopy(r2, r0, r2, r0 + r1, r10 - r0);
        r8 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00d9, code lost:
        if (r8 >= (r0 + r1)) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00db, code lost:
        r2[r8] = 0;
        r8 = r8 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x00e0, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] parseV6(java.lang.String r14) {
        /*
            r0 = -1
            r1 = 16
            byte[] r2 = new byte[r1]
            java.lang.String r3 = ":"
            r4 = -1
            java.lang.String[] r3 = r14.split(r3, r4)
            r4 = 0
            int r5 = r3.length
            r6 = 1
            int r5 = r5 - r6
            r7 = 0
            r8 = r3[r7]
            int r8 = r8.length()
            r9 = 0
            if (r8 != 0) goto L_0x002a
            int r8 = r5 - r4
            if (r8 <= 0) goto L_0x0029
            r8 = r3[r6]
            int r8 = r8.length()
            if (r8 != 0) goto L_0x0029
            int r4 = r4 + 1
            goto L_0x002a
        L_0x0029:
            return r9
        L_0x002a:
            r8 = r3[r5]
            int r8 = r8.length()
            if (r8 != 0) goto L_0x0044
            int r8 = r5 - r4
            if (r8 <= 0) goto L_0x0043
            int r8 = r5 + -1
            r8 = r3[r8]
            int r8 = r8.length()
            if (r8 != 0) goto L_0x0043
            int r5 = r5 + -1
            goto L_0x0044
        L_0x0043:
            return r9
        L_0x0044:
            int r8 = r5 - r4
            int r8 = r8 + r6
            r10 = 8
            if (r8 <= r10) goto L_0x004c
            return r9
        L_0x004c:
            r8 = r4
            r10 = 0
        L_0x004e:
            if (r8 > r5) goto L_0x00c5
            r11 = r3[r8]
            int r11 = r11.length()
            if (r11 != 0) goto L_0x005d
            if (r0 < 0) goto L_0x005b
            return r9
        L_0x005b:
            r0 = r10
            goto L_0x00bc
        L_0x005d:
            r11 = r3[r8]
            r12 = 46
            int r11 = r11.indexOf(r12)
            if (r11 < 0) goto L_0x0086
            if (r8 >= r5) goto L_0x006a
            return r9
        L_0x006a:
            r11 = 6
            if (r8 <= r11) goto L_0x006e
            return r9
        L_0x006e:
            r11 = r3[r8]
            byte[] r6 = toByteArray(r11, r6)
            if (r6 != 0) goto L_0x0077
            return r9
        L_0x0077:
            r11 = 0
        L_0x0078:
            r12 = 4
            if (r11 >= r12) goto L_0x0085
            int r12 = r10 + 1
            byte r13 = r6[r11]
            r2[r10] = r13
            int r11 = r11 + 1
            r10 = r12
            goto L_0x0078
        L_0x0085:
            goto L_0x00c5
        L_0x0086:
            r11 = 0
        L_0x0087:
            r12 = r3[r8]     // Catch:{ NumberFormatException -> 0x00c3 }
            int r12 = r12.length()     // Catch:{ NumberFormatException -> 0x00c3 }
            if (r11 >= r12) goto L_0x009f
            r12 = r3[r8]     // Catch:{ NumberFormatException -> 0x00c3 }
            char r12 = r12.charAt(r11)     // Catch:{ NumberFormatException -> 0x00c3 }
            int r13 = java.lang.Character.digit(r12, r1)     // Catch:{ NumberFormatException -> 0x00c3 }
            if (r13 >= 0) goto L_0x009c
            return r9
        L_0x009c:
            int r11 = r11 + 1
            goto L_0x0087
        L_0x009f:
            r11 = r3[r8]     // Catch:{ NumberFormatException -> 0x00c3 }
            int r11 = java.lang.Integer.parseInt(r11, r1)     // Catch:{ NumberFormatException -> 0x00c3 }
            r12 = 65535(0xffff, float:9.1834E-41)
            if (r11 > r12) goto L_0x00c2
            if (r11 >= 0) goto L_0x00ad
            goto L_0x00c2
        L_0x00ad:
            int r12 = r10 + 1
            int r13 = r11 >>> 8
            byte r13 = (byte) r13
            r2[r10] = r13     // Catch:{ NumberFormatException -> 0x00bf }
            int r10 = r12 + 1
            r13 = r11 & 255(0xff, float:3.57E-43)
            byte r13 = (byte) r13
            r2[r12] = r13     // Catch:{ NumberFormatException -> 0x00c3 }
        L_0x00bc:
            int r8 = r8 + 1
            goto L_0x004e
        L_0x00bf:
            r1 = move-exception
            r10 = r12
            goto L_0x00c4
        L_0x00c2:
            return r9
        L_0x00c3:
            r1 = move-exception
        L_0x00c4:
            return r9
        L_0x00c5:
            if (r10 >= r1) goto L_0x00ca
            if (r0 >= 0) goto L_0x00ca
            return r9
        L_0x00ca:
            if (r0 < 0) goto L_0x00e0
            int r1 = 16 - r10
            int r6 = r0 + r1
            int r9 = r10 - r0
            java.lang.System.arraycopy(r2, r0, r2, r6, r9)
            r6 = r0
            r8 = r6
        L_0x00d7:
            int r6 = r0 + r1
            if (r8 >= r6) goto L_0x00e0
            r2[r8] = r7
            int r8 = r8 + 1
            goto L_0x00d7
        L_0x00e0:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Address.parseV6(java.lang.String):byte[]");
    }

    public static int[] toArray(String s, int family) {
        byte[] byteArray = toByteArray(s, family);
        if (byteArray == null) {
            return null;
        }
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = byteArray[i] & 255;
        }
        return intArray;
    }

    public static int[] toArray(String s) {
        return toArray(s, 1);
    }

    public static byte[] toByteArray(String s, int family) {
        if (family == 1) {
            return parseV4(s);
        }
        if (family == 2) {
            return parseV6(s);
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static boolean isDottedQuad(String s) {
        if (toByteArray(s, 1) != null) {
            return true;
        }
        return false;
    }

    public static String toDottedQuad(byte[] addr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(addr[0] & 255);
        stringBuffer.append(".");
        stringBuffer.append(addr[1] & 255);
        stringBuffer.append(".");
        stringBuffer.append(addr[2] & 255);
        stringBuffer.append(".");
        stringBuffer.append(addr[3] & 255);
        return stringBuffer.toString();
    }

    public static String toDottedQuad(int[] addr) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(addr[0]);
        stringBuffer.append(".");
        stringBuffer.append(addr[1]);
        stringBuffer.append(".");
        stringBuffer.append(addr[2]);
        stringBuffer.append(".");
        stringBuffer.append(addr[3]);
        return stringBuffer.toString();
    }

    private static Record[] lookupHostName(String name, boolean all) throws UnknownHostException {
        Record[] aaaa;
        Record[] aaaa2;
        try {
            Lookup lookup = new Lookup(name, 1);
            Record[] a = lookup.run();
            if (a == null) {
                if (lookup.getResult() == 4 && (aaaa2 = new Lookup(name, 28).run()) != null) {
                    return aaaa2;
                }
                throw new UnknownHostException("unknown host");
            } else if (!all || (aaaa = new Lookup(name, 28).run()) == null) {
                return a;
            } else {
                Record[] merged = new Record[(a.length + aaaa.length)];
                System.arraycopy(a, 0, merged, 0, a.length);
                System.arraycopy(aaaa, 0, merged, a.length, aaaa.length);
                return merged;
            }
        } catch (TextParseException e) {
            throw new UnknownHostException("invalid name");
        }
    }

    private static InetAddress addrFromRecord(String name, Record r) throws UnknownHostException {
        InetAddress addr;
        if (r instanceof ARecord) {
            addr = ((ARecord) r).getAddress();
        } else {
            addr = ((AAAARecord) r).getAddress();
        }
        return InetAddress.getByAddress(name, addr.getAddress());
    }

    public static InetAddress getByName(String name) throws UnknownHostException {
        try {
            return getByAddress(name);
        } catch (UnknownHostException e) {
            return addrFromRecord(name, lookupHostName(name, false)[0]);
        }
    }

    public static InetAddress[] getAllByName(String name) throws UnknownHostException {
        try {
            return new InetAddress[]{getByAddress(name)};
        } catch (UnknownHostException e) {
            Record[] records = lookupHostName(name, true);
            InetAddress[] addrs = new InetAddress[records.length];
            for (int i = 0; i < records.length; i++) {
                addrs[i] = addrFromRecord(name, records[i]);
            }
            return addrs;
        }
    }

    public static InetAddress getByAddress(String addr) throws UnknownHostException {
        byte[] bytes = toByteArray(addr, 1);
        if (bytes != null) {
            return InetAddress.getByAddress(addr, bytes);
        }
        byte[] bytes2 = toByteArray(addr, 2);
        if (bytes2 != null) {
            return InetAddress.getByAddress(addr, bytes2);
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Invalid address: ");
        stringBuffer.append(addr);
        throw new UnknownHostException(stringBuffer.toString());
    }

    public static InetAddress getByAddress(String addr, int family) throws UnknownHostException {
        if (family == 1 || family == 2) {
            byte[] bytes = toByteArray(addr, family);
            if (bytes != null) {
                return InetAddress.getByAddress(addr, bytes);
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Invalid address: ");
            stringBuffer.append(addr);
            throw new UnknownHostException(stringBuffer.toString());
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static String getHostName(InetAddress addr) throws UnknownHostException {
        Record[] records = new Lookup(ReverseMap.fromAddress(addr), 12).run();
        if (records != null) {
            return ((PTRRecord) records[0]).getTarget().toString();
        }
        throw new UnknownHostException("unknown address");
    }

    public static int familyOf(InetAddress address) {
        if (address instanceof Inet4Address) {
            return 1;
        }
        if (address instanceof Inet6Address) {
            return 2;
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static int addressLength(int family) {
        if (family == 1) {
            return 4;
        }
        if (family == 2) {
            return 16;
        }
        throw new IllegalArgumentException("unknown address family");
    }

    public static InetAddress truncate(InetAddress address, int maskLength) {
        int maxMaskLength = addressLength(familyOf(address)) * 8;
        if (maskLength < 0 || maskLength > maxMaskLength) {
            throw new IllegalArgumentException("invalid mask length");
        } else if (maskLength == maxMaskLength) {
            return address;
        } else {
            byte[] bytes = address.getAddress();
            for (int i = (maskLength / 8) + 1; i < bytes.length; i++) {
                bytes[i] = 0;
            }
            int bitmask = 0;
            for (int i2 = 0; i2 < maskLength % 8; i2++) {
                bitmask |= 1 << (7 - i2);
            }
            int i3 = maskLength / 8;
            bytes[i3] = (byte) (bytes[i3] & bitmask);
            try {
                return InetAddress.getByAddress(bytes);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("invalid address");
            }
        }
    }
}
