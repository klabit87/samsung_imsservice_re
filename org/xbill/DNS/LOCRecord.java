package org.xbill.DNS;

import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.xbill.DNS.Tokenizer;

public class LOCRecord extends Record {
    private static final long serialVersionUID = 9058224788126750409L;
    private static NumberFormat w2;
    private static NumberFormat w3;
    private long altitude;
    private long hPrecision;
    private long latitude;
    private long longitude;
    private long size;
    private long vPrecision;

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        w2 = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(2);
        DecimalFormat decimalFormat2 = new DecimalFormat();
        w3 = decimalFormat2;
        decimalFormat2.setMinimumIntegerDigits(3);
    }

    LOCRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new LOCRecord();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public LOCRecord(Name name, int dclass, long ttl, double latitude2, double longitude2, double altitude2, double size2, double hPrecision2, double vPrecision2) {
        super(name, 29, dclass, ttl);
        this.latitude = (long) ((latitude2 * 3600.0d * 1000.0d) + 2.147483648E9d);
        this.longitude = (long) ((3600.0d * longitude2 * 1000.0d) + 2.147483648E9d);
        this.altitude = (long) ((altitude2 + 100000.0d) * 100.0d);
        this.size = (long) (size2 * 100.0d);
        this.hPrecision = (long) (hPrecision2 * 100.0d);
        this.vPrecision = (long) (vPrecision2 * 100.0d);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        if (in.readU8() == 0) {
            this.size = parseLOCformat(in.readU8());
            this.hPrecision = parseLOCformat(in.readU8());
            this.vPrecision = parseLOCformat(in.readU8());
            this.latitude = in.readU32();
            this.longitude = in.readU32();
            this.altitude = in.readU32();
            return;
        }
        throw new WireParseException("Invalid LOC version");
    }

    private double parseFixedPoint(String s) {
        if (s.matches("^-?\\d+$")) {
            return (double) Integer.parseInt(s);
        }
        if (s.matches("^-?\\d+\\.\\d*$")) {
            String[] parts = s.split("\\.");
            double value = (double) Integer.parseInt(parts[0]);
            double fraction = (double) Integer.parseInt(parts[1]);
            if (value < 0.0d) {
                fraction *= -1.0d;
            }
            return (fraction / Math.pow(10.0d, (double) parts[1].length())) + value;
        }
        throw new NumberFormatException();
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00df  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long parsePosition(org.xbill.DNS.Tokenizer r18, java.lang.String r19) throws java.io.IOException {
        /*
            r17 = this;
            r1 = r18
            r2 = r19
            java.lang.String r0 = "latitude"
            boolean r3 = r2.equals(r0)
            r0 = 0
            r4 = 0
            r5 = 0
            int r7 = r18.getUInt16()
            java.lang.String r8 = "Invalid LOC "
            r0 = 180(0xb4, float:2.52E-43)
            if (r7 > r0) goto L_0x00f3
            r0 = 90
            if (r7 <= r0) goto L_0x0023
            if (r3 != 0) goto L_0x001f
            goto L_0x0023
        L_0x001f:
            r10 = r17
            goto L_0x00f5
        L_0x0023:
            java.lang.String r9 = r18.getString()
            int r0 = java.lang.Integer.parseInt(r9)     // Catch:{ NumberFormatException -> 0x0085 }
            r4 = r0
            if (r4 < 0) goto L_0x0068
            r0 = 59
            if (r4 > r0) goto L_0x0068
            java.lang.String r0 = r18.getString()     // Catch:{ NumberFormatException -> 0x0085 }
            r9 = r0
            r10 = r17
            double r11 = r10.parseFixedPoint(r9)     // Catch:{ NumberFormatException -> 0x0083 }
            r5 = r11
            r11 = 0
            int r0 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r0 < 0) goto L_0x004f
            r11 = 4633641066610819072(0x404e000000000000, double:60.0)
            int r0 = (r5 > r11 ? 1 : (r5 == r11 ? 0 : -1))
            if (r0 >= 0) goto L_0x004f
            java.lang.String r0 = r18.getString()     // Catch:{ NumberFormatException -> 0x0083 }
            goto L_0x0089
        L_0x004f:
            java.lang.StringBuffer r0 = new java.lang.StringBuffer     // Catch:{ NumberFormatException -> 0x0083 }
            r0.<init>()     // Catch:{ NumberFormatException -> 0x0083 }
            r0.append(r8)     // Catch:{ NumberFormatException -> 0x0083 }
            r0.append(r2)     // Catch:{ NumberFormatException -> 0x0083 }
            java.lang.String r11 = " seconds"
            r0.append(r11)     // Catch:{ NumberFormatException -> 0x0083 }
            java.lang.String r0 = r0.toString()     // Catch:{ NumberFormatException -> 0x0083 }
            org.xbill.DNS.TextParseException r0 = r1.exception(r0)     // Catch:{ NumberFormatException -> 0x0083 }
            throw r0     // Catch:{ NumberFormatException -> 0x0083 }
        L_0x0068:
            r10 = r17
            java.lang.StringBuffer r0 = new java.lang.StringBuffer     // Catch:{ NumberFormatException -> 0x0083 }
            r0.<init>()     // Catch:{ NumberFormatException -> 0x0083 }
            r0.append(r8)     // Catch:{ NumberFormatException -> 0x0083 }
            r0.append(r2)     // Catch:{ NumberFormatException -> 0x0083 }
            java.lang.String r11 = " minutes"
            r0.append(r11)     // Catch:{ NumberFormatException -> 0x0083 }
            java.lang.String r0 = r0.toString()     // Catch:{ NumberFormatException -> 0x0083 }
            org.xbill.DNS.TextParseException r0 = r1.exception(r0)     // Catch:{ NumberFormatException -> 0x0083 }
            throw r0     // Catch:{ NumberFormatException -> 0x0083 }
        L_0x0083:
            r0 = move-exception
            goto L_0x0088
        L_0x0085:
            r0 = move-exception
            r10 = r17
        L_0x0088:
            r0 = r9
        L_0x0089:
            int r9 = r0.length()
            r11 = 1
            if (r9 != r11) goto L_0x00df
            long r13 = (long) r4
            long r11 = (long) r7
            r15 = 60
            long r11 = r11 * r15
            long r13 = r13 + r11
            long r13 = r13 * r15
            double r11 = (double) r13
            double r11 = r11 + r5
            r13 = 4652007308841189376(0x408f400000000000, double:1000.0)
            double r11 = r11 * r13
            long r11 = (long) r11
            r9 = 0
            char r9 = r0.charAt(r9)
            char r9 = java.lang.Character.toUpperCase(r9)
            if (r3 == 0) goto L_0x00af
            r13 = 83
            if (r9 == r13) goto L_0x00b5
        L_0x00af:
            if (r3 != 0) goto L_0x00b7
            r13 = 87
            if (r9 != r13) goto L_0x00b7
        L_0x00b5:
            long r11 = -r11
            goto L_0x00d8
        L_0x00b7:
            if (r3 == 0) goto L_0x00bd
            r13 = 78
            if (r9 != r13) goto L_0x00c4
        L_0x00bd:
            if (r3 != 0) goto L_0x00d8
            r13 = 69
            if (r9 != r13) goto L_0x00c4
            goto L_0x00d8
        L_0x00c4:
            java.lang.StringBuffer r13 = new java.lang.StringBuffer
            r13.<init>()
            r13.append(r8)
            r13.append(r2)
            java.lang.String r8 = r13.toString()
            org.xbill.DNS.TextParseException r8 = r1.exception(r8)
            throw r8
        L_0x00d8:
            r13 = 2147483648(0x80000000, double:1.0609978955E-314)
            long r11 = r11 + r13
            return r11
        L_0x00df:
            java.lang.StringBuffer r9 = new java.lang.StringBuffer
            r9.<init>()
            r9.append(r8)
            r9.append(r2)
            java.lang.String r8 = r9.toString()
            org.xbill.DNS.TextParseException r8 = r1.exception(r8)
            throw r8
        L_0x00f3:
            r10 = r17
        L_0x00f5:
            java.lang.StringBuffer r0 = new java.lang.StringBuffer
            r0.<init>()
            r0.append(r8)
            r0.append(r2)
            java.lang.String r8 = " degrees"
            r0.append(r8)
            java.lang.String r0 = r0.toString()
            org.xbill.DNS.TextParseException r0 = r1.exception(r0)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.LOCRecord.parsePosition(org.xbill.DNS.Tokenizer, java.lang.String):long");
    }

    /* Debug info: failed to restart local var, previous not found, register: 11 */
    private long parseDouble(Tokenizer st, String type, boolean required, long min, long max, long defaultValue) throws IOException {
        String s;
        Tokenizer tokenizer = st;
        String str = type;
        Tokenizer.Token token = st.get();
        if (!token.isEOL()) {
            String s2 = token.value;
            if (s2.length() <= 1 || s2.charAt(s2.length() - 1) != 'm') {
                s = s2;
            } else {
                s = s2.substring(0, s2.length() - 1);
            }
            try {
                long value = (long) (parseFixedPoint(s) * 100.0d);
                if (value >= min && value <= max) {
                    return value;
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid LOC ");
                stringBuffer.append(type);
                throw st.exception(stringBuffer.toString());
            } catch (NumberFormatException e) {
                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append("Invalid LOC ");
                stringBuffer2.append(type);
                throw st.exception(stringBuffer2.toString());
            }
        } else if (!required) {
            st.unget();
            return defaultValue;
        } else {
            StringBuffer stringBuffer3 = new StringBuffer();
            stringBuffer3.append("Invalid LOC ");
            stringBuffer3.append(type);
            throw st.exception(stringBuffer3.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.latitude = parsePosition(st, CallConstants.ComposerData.LATITUDE);
        this.longitude = parsePosition(st, CallConstants.ComposerData.LONGITUDE);
        this.altitude = parseDouble(st, "altitude", true, -10000000, 4284967295L, 0) + 10000000;
        Tokenizer tokenizer = st;
        this.size = parseDouble(tokenizer, "size", false, 0, 9000000000L, 100);
        this.hPrecision = parseDouble(tokenizer, "horizontal precision", false, 0, 9000000000L, 1000000);
        this.vPrecision = parseDouble(tokenizer, "vertical precision", false, 0, 9000000000L, 1000);
    }

    private void renderFixedPoint(StringBuffer sb, NumberFormat formatter, long value, long divisor) {
        sb.append(value / divisor);
        long value2 = value % divisor;
        if (value2 != 0) {
            sb.append(".");
            sb.append(formatter.format(value2));
        }
    }

    private String positionToString(long value, char pos, char neg) {
        char direction;
        StringBuffer sb = new StringBuffer();
        long temp = value - 2147483648L;
        if (temp < 0) {
            temp = -temp;
            direction = neg;
        } else {
            direction = pos;
        }
        sb.append(temp / 3600000);
        long temp2 = temp % 3600000;
        sb.append(" ");
        sb.append(temp2 / 60000);
        long temp3 = temp2 % 60000;
        sb.append(" ");
        renderFixedPoint(sb, w3, temp3, 1000);
        sb.append(" ");
        sb.append(direction);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(positionToString(this.latitude, 'N', 'S'));
        sb.append(" ");
        sb.append(positionToString(this.longitude, 'E', 'W'));
        sb.append(" ");
        StringBuffer stringBuffer = sb;
        renderFixedPoint(stringBuffer, w2, this.altitude - 10000000, 100);
        sb.append("m ");
        renderFixedPoint(stringBuffer, w2, this.size, 100);
        sb.append("m ");
        renderFixedPoint(stringBuffer, w2, this.hPrecision, 100);
        sb.append("m ");
        renderFixedPoint(stringBuffer, w2, this.vPrecision, 100);
        sb.append("m");
        return sb.toString();
    }

    public double getLatitude() {
        return ((double) (this.latitude - 2147483648L)) / 3600000.0d;
    }

    public double getLongitude() {
        return ((double) (this.longitude - 2147483648L)) / 3600000.0d;
    }

    public double getAltitude() {
        return ((double) (this.altitude - 10000000)) / 100.0d;
    }

    public double getSize() {
        return ((double) this.size) / 100.0d;
    }

    public double getHPrecision() {
        return ((double) this.hPrecision) / 100.0d;
    }

    public double getVPrecision() {
        return ((double) this.vPrecision) / 100.0d;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(0);
        out.writeU8(toLOCformat(this.size));
        out.writeU8(toLOCformat(this.hPrecision));
        out.writeU8(toLOCformat(this.vPrecision));
        out.writeU32(this.latitude);
        out.writeU32(this.longitude);
        out.writeU32(this.altitude);
    }

    private static long parseLOCformat(int b) throws WireParseException {
        long out = (long) (b >> 4);
        int exp = b & 15;
        if (out > 9 || exp > 9) {
            throw new WireParseException("Invalid LOC Encoding");
        }
        while (true) {
            int exp2 = exp - 1;
            if (exp <= 0) {
                return out;
            }
            out *= 10;
            exp = exp2;
        }
    }

    private int toLOCformat(long l) {
        byte exp = 0;
        while (l > 9) {
            exp = (byte) (exp + 1);
            l /= 10;
        }
        return (int) ((l << 4) + ((long) exp));
    }
}
