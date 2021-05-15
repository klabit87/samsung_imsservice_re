package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Generator {
    private long current;
    public final int dclass;
    public long end;
    public final String namePattern;
    public final Name origin;
    public final String rdataPattern;
    public long start;
    public long step;
    public final long ttl;
    public final int type;

    public static boolean supportedType(int type2) {
        Type.check(type2);
        return type2 == 12 || type2 == 5 || type2 == 39 || type2 == 1 || type2 == 28 || type2 == 2;
    }

    public Generator(long start2, long end2, long step2, String namePattern2, int type2, int dclass2, long ttl2, String rdataPattern2, Name origin2) {
        long j = start2;
        long j2 = end2;
        long j3 = step2;
        if (j < 0 || j2 < 0 || j > j2 || j3 <= 0) {
            String str = namePattern2;
            int i = type2;
            int i2 = dclass2;
            long j4 = ttl2;
            String str2 = rdataPattern2;
            Name name = origin2;
            throw new IllegalArgumentException("invalid range specification");
        } else if (supportedType(type2)) {
            DClass.check(dclass2);
            this.start = j;
            this.end = j2;
            this.step = j3;
            this.namePattern = namePattern2;
            this.type = type2;
            this.dclass = dclass2;
            this.ttl = ttl2;
            this.rdataPattern = rdataPattern2;
            this.origin = origin2;
            this.current = j;
        } else {
            String str3 = namePattern2;
            int i3 = type2;
            int i4 = dclass2;
            long j5 = ttl2;
            String str4 = rdataPattern2;
            Name name2 = origin2;
            throw new IllegalArgumentException("unsupported type");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00d9, code lost:
        throw new org.xbill.DNS.TextParseException("invalid width");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String substitute(java.lang.String r21, long r22) throws java.io.IOException {
        /*
            r20 = this;
            r0 = 0
            byte[] r1 = r21.getBytes()
            java.lang.StringBuffer r2 = new java.lang.StringBuffer
            r2.<init>()
            r3 = 0
        L_0x000b:
            int r4 = r1.length
            if (r3 >= r4) goto L_0x01a0
            byte r4 = r1[r3]
            r4 = r4 & 255(0xff, float:3.57E-43)
            char r4 = (char) r4
            if (r0 == 0) goto L_0x001d
            r2.append(r4)
            r0 = 0
            r17 = r1
            goto L_0x019a
        L_0x001d:
            r5 = 92
            if (r4 != r5) goto L_0x0033
            int r5 = r3 + 1
            int r6 = r1.length
            if (r5 == r6) goto L_0x002b
            r0 = 1
            r17 = r1
            goto L_0x019a
        L_0x002b:
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            java.lang.String r6 = "invalid escape character"
            r5.<init>(r6)
            throw r5
        L_0x0033:
            r5 = 36
            if (r4 != r5) goto L_0x0193
            r6 = 0
            r7 = 0
            r9 = 0
            r11 = 10
            r13 = 0
            int r14 = r3 + 1
            int r15 = r1.length
            if (r14 >= r15) goto L_0x0058
            int r14 = r3 + 1
            byte r14 = r1[r14]
            if (r14 != r5) goto L_0x0058
            int r3 = r3 + 1
            byte r5 = r1[r3]
            r5 = r5 & 255(0xff, float:3.57E-43)
            char r4 = (char) r5
            r2.append(r4)
            r17 = r1
            goto L_0x019a
        L_0x0058:
            int r5 = r3 + 1
            int r14 = r1.length
            if (r5 >= r14) goto L_0x0133
            int r5 = r3 + 1
            byte r5 = r1[r5]
            r14 = 123(0x7b, float:1.72E-43)
            if (r5 != r14) goto L_0x0133
            int r3 = r3 + 1
            int r5 = r3 + 1
            int r14 = r1.length
            if (r5 >= r14) goto L_0x0077
            int r5 = r3 + 1
            byte r5 = r1[r5]
            r14 = 45
            if (r5 != r14) goto L_0x0077
            r6 = 1
            int r3 = r3 + 1
        L_0x0077:
            int r5 = r3 + 1
            int r14 = r1.length
            r16 = 10
            r15 = 44
            if (r5 >= r14) goto L_0x00a6
            int r3 = r3 + 1
            byte r5 = r1[r3]
            r5 = r5 & 255(0xff, float:3.57E-43)
            char r4 = (char) r5
            if (r4 == r15) goto L_0x00a6
            r5 = 125(0x7d, float:1.75E-43)
            if (r4 != r5) goto L_0x008e
            goto L_0x00a6
        L_0x008e:
            r5 = 48
            if (r4 < r5) goto L_0x009e
            r5 = 57
            if (r4 > r5) goto L_0x009e
            int r5 = r4 + -48
            char r4 = (char) r5
            long r7 = r7 * r16
            long r14 = (long) r4
            long r7 = r7 + r14
            goto L_0x0077
        L_0x009e:
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            java.lang.String r14 = "invalid offset"
            r5.<init>(r14)
            throw r5
        L_0x00a6:
            if (r6 == 0) goto L_0x00a9
            long r7 = -r7
        L_0x00a9:
            if (r4 != r15) goto L_0x00de
        L_0x00ab:
            int r5 = r3 + 1
            int r14 = r1.length
            if (r5 >= r14) goto L_0x00dc
            int r3 = r3 + 1
            byte r5 = r1[r3]
            r5 = r5 & 255(0xff, float:3.57E-43)
            char r4 = (char) r5
            if (r4 == r15) goto L_0x00da
            r5 = 125(0x7d, float:1.75E-43)
            if (r4 != r5) goto L_0x00bf
            r14 = r6
            goto L_0x00df
        L_0x00bf:
            r5 = 48
            if (r4 < r5) goto L_0x00d1
            r5 = 57
            if (r4 > r5) goto L_0x00d1
            int r14 = r4 + -48
            char r4 = (char) r14
            long r9 = r9 * r16
            r14 = r6
            long r5 = (long) r4
            long r9 = r9 + r5
            r6 = r14
            goto L_0x00ab
        L_0x00d1:
            r14 = r6
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            java.lang.String r6 = "invalid width"
            r5.<init>(r6)
            throw r5
        L_0x00da:
            r14 = r6
            goto L_0x00df
        L_0x00dc:
            r14 = r6
            goto L_0x00df
        L_0x00de:
            r14 = r6
        L_0x00df:
            if (r4 != r15) goto L_0x011a
            int r5 = r3 + 1
            int r6 = r1.length
            java.lang.String r15 = "invalid base"
            if (r5 == r6) goto L_0x0114
            int r3 = r3 + 1
            byte r5 = r1[r3]
            r5 = r5 & 255(0xff, float:3.57E-43)
            char r4 = (char) r5
            r5 = 111(0x6f, float:1.56E-43)
            if (r4 != r5) goto L_0x00f7
            r5 = 8
            r11 = r5
            goto L_0x011a
        L_0x00f7:
            r5 = 120(0x78, float:1.68E-43)
            if (r4 != r5) goto L_0x00ff
            r5 = 16
            r11 = r5
            goto L_0x011a
        L_0x00ff:
            r5 = 88
            if (r4 != r5) goto L_0x0109
            r5 = 16
            r11 = 1
            r13 = r11
            r11 = r5
            goto L_0x011a
        L_0x0109:
            r5 = 100
            if (r4 != r5) goto L_0x010e
            goto L_0x011a
        L_0x010e:
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            r5.<init>(r15)
            throw r5
        L_0x0114:
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            r5.<init>(r15)
            throw r5
        L_0x011a:
            int r5 = r3 + 1
            int r6 = r1.length
            if (r5 == r6) goto L_0x012b
            int r5 = r3 + 1
            byte r5 = r1[r5]
            r6 = 125(0x7d, float:1.75E-43)
            if (r5 != r6) goto L_0x012b
            int r3 = r3 + 1
            r6 = r14
            goto L_0x0133
        L_0x012b:
            org.xbill.DNS.TextParseException r5 = new org.xbill.DNS.TextParseException
            java.lang.String r6 = "invalid modifiers"
            r5.<init>(r6)
            throw r5
        L_0x0133:
            long r14 = r22 + r7
            r16 = 0
            int r5 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1))
            if (r5 < 0) goto L_0x0187
            r18 = 8
            int r5 = (r11 > r18 ? 1 : (r11 == r18 ? 0 : -1))
            if (r5 != 0) goto L_0x0146
            java.lang.String r5 = java.lang.Long.toOctalString(r14)
            goto L_0x0155
        L_0x0146:
            r18 = 16
            int r5 = (r11 > r18 ? 1 : (r11 == r18 ? 0 : -1))
            if (r5 != 0) goto L_0x0151
            java.lang.String r5 = java.lang.Long.toHexString(r14)
            goto L_0x0155
        L_0x0151:
            java.lang.String r5 = java.lang.Long.toString(r14)
        L_0x0155:
            if (r13 == 0) goto L_0x015b
            java.lang.String r5 = r5.toUpperCase()
        L_0x015b:
            int r16 = (r9 > r16 ? 1 : (r9 == r16 ? 0 : -1))
            if (r16 == 0) goto L_0x017d
            r16 = r0
            int r0 = r5.length()
            r17 = r1
            long r0 = (long) r0
            int r0 = (r9 > r0 ? 1 : (r9 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0181
            int r0 = (int) r9
            int r1 = r5.length()
            int r0 = r0 - r1
        L_0x0172:
            int r1 = r0 + -1
            if (r0 <= 0) goto L_0x0181
            r0 = 48
            r2.append(r0)
            r0 = r1
            goto L_0x0172
        L_0x017d:
            r16 = r0
            r17 = r1
        L_0x0181:
            r2.append(r5)
            r0 = r16
            goto L_0x019a
        L_0x0187:
            r16 = r0
            r17 = r1
            org.xbill.DNS.TextParseException r0 = new org.xbill.DNS.TextParseException
            java.lang.String r1 = "invalid offset expansion"
            r0.<init>(r1)
            throw r0
        L_0x0193:
            r16 = r0
            r17 = r1
            r2.append(r4)
        L_0x019a:
            int r3 = r3 + 1
            r1 = r17
            goto L_0x000b
        L_0x01a0:
            r16 = r0
            java.lang.String r0 = r2.toString()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Generator.substitute(java.lang.String, long):java.lang.String");
    }

    public Record nextRecord() throws IOException {
        long j = this.current;
        if (j > this.end) {
            return null;
        }
        Name name = Name.fromString(substitute(this.namePattern, j), this.origin);
        String rdata = substitute(this.rdataPattern, this.current);
        this.current += this.step;
        return Record.fromString(name, this.type, this.dclass, this.ttl, rdata, this.origin);
    }

    public Record[] expand() throws IOException {
        List list = new ArrayList();
        long i = this.start;
        while (i < this.end) {
            Name name = Name.fromString(substitute(this.namePattern, this.current), this.origin);
            list.add(Record.fromString(name, this.type, this.dclass, this.ttl, substitute(this.rdataPattern, this.current), this.origin));
            i += this.step;
        }
        return (Record[]) list.toArray(new Record[list.size()]);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("$GENERATE ");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.start);
        stringBuffer.append("-");
        stringBuffer.append(this.end);
        sb.append(stringBuffer.toString());
        if (this.step > 1) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append("/");
            stringBuffer2.append(this.step);
            sb.append(stringBuffer2.toString());
        }
        sb.append(" ");
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(this.namePattern);
        stringBuffer3.append(" ");
        sb.append(stringBuffer3.toString());
        StringBuffer stringBuffer4 = new StringBuffer();
        stringBuffer4.append(this.ttl);
        stringBuffer4.append(" ");
        sb.append(stringBuffer4.toString());
        if (this.dclass != 1 || !Options.check("noPrintIN")) {
            StringBuffer stringBuffer5 = new StringBuffer();
            stringBuffer5.append(DClass.string(this.dclass));
            stringBuffer5.append(" ");
            sb.append(stringBuffer5.toString());
        }
        StringBuffer stringBuffer6 = new StringBuffer();
        stringBuffer6.append(Type.string(this.type));
        stringBuffer6.append(" ");
        sb.append(stringBuffer6.toString());
        StringBuffer stringBuffer7 = new StringBuffer();
        stringBuffer7.append(this.rdataPattern);
        stringBuffer7.append(" ");
        sb.append(stringBuffer7.toString());
        return sb.toString();
    }
}
