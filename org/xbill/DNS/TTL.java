package org.xbill.DNS;

public final class TTL {
    public static final long MAX_VALUE = 2147483647L;

    private TTL() {
    }

    static void check(long i) {
        if (i < 0 || i > MAX_VALUE) {
            throw new InvalidTTLException(i);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x0077 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0074 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long parse(java.lang.String r14, boolean r15) {
        /*
            if (r14 == 0) goto L_0x009b
            int r0 = r14.length()
            if (r0 == 0) goto L_0x009b
            r0 = 0
            char r0 = r14.charAt(r0)
            boolean r0 = java.lang.Character.isDigit(r0)
            if (r0 == 0) goto L_0x009b
            r0 = 0
            r2 = 0
            r4 = 0
        L_0x0018:
            int r5 = r14.length()
            r6 = 4294967295(0xffffffff, double:2.1219957905E-314)
            if (r4 >= r5) goto L_0x007d
            char r5 = r14.charAt(r4)
            r8 = r0
            boolean r10 = java.lang.Character.isDigit(r5)
            if (r10 == 0) goto L_0x0043
            r6 = 10
            long r6 = r6 * r0
            int r10 = java.lang.Character.getNumericValue(r5)
            long r10 = (long) r10
            long r6 = r6 + r10
            int r0 = (r6 > r8 ? 1 : (r6 == r8 ? 0 : -1))
            if (r0 < 0) goto L_0x003d
            r0 = r6
            goto L_0x0074
        L_0x003d:
            java.lang.NumberFormatException r0 = new java.lang.NumberFormatException
            r0.<init>()
            throw r0
        L_0x0043:
            char r10 = java.lang.Character.toUpperCase(r5)
            r11 = 68
            r12 = 60
            if (r10 == r11) goto L_0x0067
            r11 = 72
            if (r10 == r11) goto L_0x006a
            r11 = 77
            if (r10 == r11) goto L_0x006b
            r11 = 83
            if (r10 == r11) goto L_0x006c
            r11 = 87
            if (r10 != r11) goto L_0x0061
            r10 = 7
            long r0 = r0 * r10
            goto L_0x0067
        L_0x0061:
            java.lang.NumberFormatException r6 = new java.lang.NumberFormatException
            r6.<init>()
            throw r6
        L_0x0067:
            r10 = 24
            long r0 = r0 * r10
        L_0x006a:
            long r0 = r0 * r12
        L_0x006b:
            long r0 = r0 * r12
        L_0x006c:
            long r2 = r2 + r0
            r0 = 0
            int r6 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r6 > 0) goto L_0x0077
        L_0x0074:
            int r4 = r4 + 1
            goto L_0x0018
        L_0x0077:
            java.lang.NumberFormatException r6 = new java.lang.NumberFormatException
            r6.<init>()
            throw r6
        L_0x007d:
            r4 = 0
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r4 != 0) goto L_0x0084
            r2 = r0
        L_0x0084:
            int r4 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r4 > 0) goto L_0x0095
            r4 = 2147483647(0x7fffffff, double:1.060997895E-314)
            int r4 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r4 <= 0) goto L_0x0094
            if (r15 == 0) goto L_0x0094
            r2 = 2147483647(0x7fffffff, double:1.060997895E-314)
        L_0x0094:
            return r2
        L_0x0095:
            java.lang.NumberFormatException r4 = new java.lang.NumberFormatException
            r4.<init>()
            throw r4
        L_0x009b:
            java.lang.NumberFormatException r0 = new java.lang.NumberFormatException
            r0.<init>()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.TTL.parse(java.lang.String, boolean):long");
    }

    public static long parseTTL(String s) {
        return parse(s, true);
    }

    public static String format(long ttl) {
        check(ttl);
        StringBuffer sb = new StringBuffer();
        long secs = ttl % 60;
        long ttl2 = ttl / 60;
        long mins = ttl2 % 60;
        long ttl3 = ttl2 / 60;
        long hours = ttl3 % 24;
        long ttl4 = ttl3 / 24;
        long days = ttl4 % 7;
        long weeks = ttl4 / 7;
        if (weeks > 0) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(weeks);
            stringBuffer.append("W");
            sb.append(stringBuffer.toString());
        }
        if (days > 0) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(days);
            stringBuffer2.append("D");
            sb.append(stringBuffer2.toString());
        }
        if (hours > 0) {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append(hours);
            stringBuffer3.append("H");
            sb.append(stringBuffer3.toString());
        }
        if (mins > 0) {
            StringBuffer stringBuffer4 = new StringBuffer();
            stringBuffer4.append(mins);
            stringBuffer4.append("M");
            sb.append(stringBuffer4.toString());
        }
        if (secs > 0 || (weeks == 0 && days == 0 && hours == 0 && mins == 0)) {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append(secs);
            stringBuffer5.append("S");
            sb.append(stringBuffer5.toString());
        }
        return sb.toString();
    }
}
