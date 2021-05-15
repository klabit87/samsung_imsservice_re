package org.xbill.DNS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import org.xbill.DNS.Tokenizer;
import org.xbill.DNS.utils.base16;

public abstract class Record implements Cloneable, Comparable, Serializable {
    private static final DecimalFormat byteFormat;
    private static final long serialVersionUID = 2694906050116005466L;
    protected int dclass;
    protected Name name;
    protected long ttl;
    protected int type;

    /* access modifiers changed from: package-private */
    public abstract Record getObject();

    /* access modifiers changed from: package-private */
    public abstract void rdataFromString(Tokenizer tokenizer, Name name2) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void rrFromWire(DNSInput dNSInput) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract String rrToString();

    /* access modifiers changed from: package-private */
    public abstract void rrToWire(DNSOutput dNSOutput, Compression compression, boolean z);

    static {
        DecimalFormat decimalFormat = new DecimalFormat();
        byteFormat = decimalFormat;
        decimalFormat.setMinimumIntegerDigits(3);
    }

    protected Record() {
    }

    Record(Name name2, int type2, int dclass2, long ttl2) {
        if (name2.isAbsolute()) {
            Type.check(type2);
            DClass.check(dclass2);
            TTL.check(ttl2);
            this.name = name2;
            this.type = type2;
            this.dclass = dclass2;
            this.ttl = ttl2;
            return;
        }
        throw new RelativeNameException(name2);
    }

    private static final Record getEmptyRecord(Name name2, int type2, int dclass2, long ttl2, boolean hasData) {
        Record rec;
        if (hasData) {
            Record proto = Type.getProto(type2);
            if (proto != null) {
                rec = proto.getObject();
            } else {
                rec = new UNKRecord();
            }
        } else {
            rec = new EmptyRecord();
        }
        rec.name = name2;
        rec.type = type2;
        rec.dclass = dclass2;
        rec.ttl = ttl2;
        return rec;
    }

    private static Record newRecord(Name name2, int type2, int dclass2, long ttl2, int length, DNSInput in) throws IOException {
        Record rec = getEmptyRecord(name2, type2, dclass2, ttl2, in != null);
        if (in != null) {
            if (in.remaining() >= length) {
                in.setActive(length);
                rec.rrFromWire(in);
                if (in.remaining() <= 0) {
                    in.clearActive();
                } else {
                    throw new WireParseException("invalid record length");
                }
            } else {
                throw new WireParseException("truncated record");
            }
        }
        return rec;
    }

    public static Record newRecord(Name name2, int type2, int dclass2, long ttl2, int length, byte[] data) {
        DNSInput in;
        if (name2.isAbsolute()) {
            Type.check(type2);
            DClass.check(dclass2);
            TTL.check(ttl2);
            if (data != null) {
                in = new DNSInput(data);
            } else {
                in = null;
            }
            try {
                return newRecord(name2, type2, dclass2, ttl2, length, in);
            } catch (IOException e) {
                return null;
            }
        } else {
            throw new RelativeNameException(name2);
        }
    }

    public static Record newRecord(Name name2, int type2, int dclass2, long ttl2, byte[] data) {
        return newRecord(name2, type2, dclass2, ttl2, data.length, data);
    }

    public static Record newRecord(Name name2, int type2, int dclass2, long ttl2) {
        if (name2.isAbsolute()) {
            Type.check(type2);
            DClass.check(dclass2);
            TTL.check(ttl2);
            return getEmptyRecord(name2, type2, dclass2, ttl2, false);
        }
        throw new RelativeNameException(name2);
    }

    public static Record newRecord(Name name2, int type2, int dclass2) {
        return newRecord(name2, type2, dclass2, 0);
    }

    static Record fromWire(DNSInput in, int section, boolean isUpdate) throws IOException {
        Name name2 = new Name(in);
        int type2 = in.readU16();
        int dclass2 = in.readU16();
        if (section == 0) {
            return newRecord(name2, type2, dclass2);
        }
        long ttl2 = in.readU32();
        int length = in.readU16();
        if (length == 0 && isUpdate && (section == 1 || section == 2)) {
            return newRecord(name2, type2, dclass2, ttl2);
        }
        return newRecord(name2, type2, dclass2, ttl2, length, in);
    }

    static Record fromWire(DNSInput in, int section) throws IOException {
        return fromWire(in, section, false);
    }

    public static Record fromWire(byte[] b, int section) throws IOException {
        return fromWire(new DNSInput(b), section, false);
    }

    /* access modifiers changed from: package-private */
    public void toWire(DNSOutput out, int section, Compression c) {
        this.name.toWire(out, c);
        out.writeU16(this.type);
        out.writeU16(this.dclass);
        if (section != 0) {
            out.writeU32(this.ttl);
            int lengthPosition = out.current();
            out.writeU16(0);
            rrToWire(out, c, false);
            out.writeU16At((out.current() - lengthPosition) - 2, lengthPosition);
        }
    }

    public byte[] toWire(int section) {
        DNSOutput out = new DNSOutput();
        toWire(out, section, (Compression) null);
        return out.toByteArray();
    }

    private void toWireCanonical(DNSOutput out, boolean noTTL) {
        this.name.toWireCanonical(out);
        out.writeU16(this.type);
        out.writeU16(this.dclass);
        if (noTTL) {
            out.writeU32(0);
        } else {
            out.writeU32(this.ttl);
        }
        int lengthPosition = out.current();
        out.writeU16(0);
        rrToWire(out, (Compression) null, true);
        out.writeU16At((out.current() - lengthPosition) - 2, lengthPosition);
    }

    private byte[] toWireCanonical(boolean noTTL) {
        DNSOutput out = new DNSOutput();
        toWireCanonical(out, noTTL);
        return out.toByteArray();
    }

    public byte[] toWireCanonical() {
        return toWireCanonical(false);
    }

    public byte[] rdataToWireCanonical() {
        DNSOutput out = new DNSOutput();
        rrToWire(out, (Compression) null, true);
        return out.toByteArray();
    }

    public String rdataToString() {
        return rrToString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name);
        if (sb.length() < 8) {
            sb.append("\t");
        }
        if (sb.length() < 16) {
            sb.append("\t");
        }
        sb.append("\t");
        if (Options.check("BINDTTL")) {
            sb.append(TTL.format(this.ttl));
        } else {
            sb.append(this.ttl);
        }
        sb.append("\t");
        if (this.dclass != 1 || !Options.check("noPrintIN")) {
            sb.append(DClass.string(this.dclass));
            sb.append("\t");
        }
        sb.append(Type.string(this.type));
        String rdata = rrToString();
        if (!rdata.equals("")) {
            sb.append("\t");
            sb.append(rdata);
        }
        return sb.toString();
    }

    protected static byte[] byteArrayFromString(String s) throws TextParseException {
        byte[] array = s.getBytes();
        boolean escaped = false;
        boolean hasEscapes = false;
        int i = 0;
        while (true) {
            if (i >= array.length) {
                break;
            } else if (array[i] == 92) {
                hasEscapes = true;
                break;
            } else {
                i++;
            }
        }
        if (hasEscapes) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int digits = 0;
            int intval = 0;
            for (int i2 = 0; i2 < array.length; i2++) {
                byte b = array[i2];
                if (escaped) {
                    if (b >= 48 && b <= 57 && digits < 3) {
                        digits++;
                        intval = (intval * 10) + (b - 48);
                        if (intval > 255) {
                            throw new TextParseException("bad escape");
                        } else if (digits >= 3) {
                            b = (byte) intval;
                        }
                    } else if (digits > 0 && digits < 3) {
                        throw new TextParseException("bad escape");
                    }
                    os.write(b);
                    escaped = false;
                } else if (array[i2] == 92) {
                    escaped = true;
                    digits = 0;
                    intval = 0;
                } else {
                    os.write(array[i2]);
                }
            }
            if (digits > 0 && digits < 3) {
                throw new TextParseException("bad escape");
            } else if (os.toByteArray().length <= 255) {
                return os.toByteArray();
            } else {
                throw new TextParseException("text string too long");
            }
        } else if (array.length <= 255) {
            return array;
        } else {
            throw new TextParseException("text string too long");
        }
    }

    protected static String byteArrayToString(byte[] array, boolean quote) {
        StringBuffer sb = new StringBuffer();
        if (quote) {
            sb.append('\"');
        }
        for (byte b : array) {
            int b2 = b & 255;
            if (b2 < 32 || b2 >= 127) {
                sb.append('\\');
                sb.append(byteFormat.format((long) b2));
            } else if (b2 == 34 || b2 == 92) {
                sb.append('\\');
                sb.append((char) b2);
            } else {
                sb.append((char) b2);
            }
        }
        if (quote) {
            sb.append('\"');
        }
        return sb.toString();
    }

    protected static String unknownToString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        sb.append("\\# ");
        sb.append(data.length);
        sb.append(" ");
        sb.append(base16.toString(data));
        return sb.toString();
    }

    public static Record fromString(Name name2, int type2, int dclass2, long ttl2, Tokenizer st, Name origin) throws IOException {
        byte[] data;
        Tokenizer tokenizer = st;
        if (name2.isAbsolute()) {
            Type.check(type2);
            DClass.check(dclass2);
            TTL.check(ttl2);
            Tokenizer.Token t = st.get();
            if (t.type != 3 || !t.value.equals("\\#")) {
                st.unget();
                Record rec = getEmptyRecord(name2, type2, dclass2, ttl2, true);
                rec.rdataFromString(tokenizer, origin);
                Tokenizer.Token t2 = st.get();
                if (t2.type == 1 || t2.type == 0) {
                    return rec;
                }
                throw tokenizer.exception("unexpected tokens at end of record");
            }
            int length = st.getUInt16();
            byte[] data2 = st.getHex();
            if (data2 == null) {
                data = new byte[0];
            } else {
                data = data2;
            }
            if (length == data.length) {
                return newRecord(name2, type2, dclass2, ttl2, length, new DNSInput(data));
            }
            throw tokenizer.exception("invalid unknown RR encoding: length mismatch");
        }
        Name name3 = origin;
        Name name4 = name2;
        throw new RelativeNameException(name2);
    }

    public static Record fromString(Name name2, int type2, int dclass2, long ttl2, String s, Name origin) throws IOException {
        return fromString(name2, type2, dclass2, ttl2, new Tokenizer(s), origin);
    }

    public Name getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public int getRRsetType() {
        int i = this.type;
        if (i == 46) {
            return ((RRSIGRecord) this).getTypeCovered();
        }
        return i;
    }

    public int getDClass() {
        return this.dclass;
    }

    public long getTTL() {
        return this.ttl;
    }

    public boolean sameRRset(Record rec) {
        return getRRsetType() == rec.getRRsetType() && this.dclass == rec.dclass && this.name.equals(rec.name);
    }

    public boolean equals(Object arg) {
        if (arg == null || !(arg instanceof Record)) {
            return false;
        }
        Record r = (Record) arg;
        if (this.type == r.type && this.dclass == r.dclass && this.name.equals(r.name)) {
            return Arrays.equals(rdataToWireCanonical(), r.rdataToWireCanonical());
        }
        return false;
    }

    public int hashCode() {
        int code = 0;
        for (byte b : toWireCanonical(true)) {
            code += (code << 3) + (b & 255);
        }
        return code;
    }

    /* access modifiers changed from: package-private */
    public Record cloneRecord() {
        try {
            return (Record) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

    public Record withName(Name name2) {
        if (name2.isAbsolute()) {
            Record rec = cloneRecord();
            rec.name = name2;
            return rec;
        }
        throw new RelativeNameException(name2);
    }

    /* access modifiers changed from: package-private */
    public Record withDClass(int dclass2, long ttl2) {
        Record rec = cloneRecord();
        rec.dclass = dclass2;
        rec.ttl = ttl2;
        return rec;
    }

    /* access modifiers changed from: package-private */
    public void setTTL(long ttl2) {
        this.ttl = ttl2;
    }

    /* JADX WARNING: type inference failed for: r8v0, types: [java.lang.Object] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int compareTo(java.lang.Object r8) {
        /*
            r7 = this;
            r0 = r8
            org.xbill.DNS.Record r0 = (org.xbill.DNS.Record) r0
            if (r7 != r0) goto L_0x0007
            r1 = 0
            return r1
        L_0x0007:
            org.xbill.DNS.Name r1 = r7.name
            org.xbill.DNS.Name r2 = r0.name
            int r1 = r1.compareTo(r2)
            if (r1 == 0) goto L_0x0012
            return r1
        L_0x0012:
            int r2 = r7.dclass
            int r3 = r0.dclass
            int r2 = r2 - r3
            if (r2 == 0) goto L_0x001a
            return r2
        L_0x001a:
            int r1 = r7.type
            int r3 = r0.type
            int r1 = r1 - r3
            if (r1 == 0) goto L_0x0022
            return r1
        L_0x0022:
            byte[] r2 = r7.rdataToWireCanonical()
            byte[] r3 = r0.rdataToWireCanonical()
            r4 = 0
        L_0x002b:
            int r5 = r2.length
            if (r4 >= r5) goto L_0x0041
            int r5 = r3.length
            if (r4 >= r5) goto L_0x0041
            byte r5 = r2[r4]
            r5 = r5 & 255(0xff, float:3.57E-43)
            byte r6 = r3[r4]
            r6 = r6 & 255(0xff, float:3.57E-43)
            int r1 = r5 - r6
            if (r1 == 0) goto L_0x003e
            return r1
        L_0x003e:
            int r4 = r4 + 1
            goto L_0x002b
        L_0x0041:
            int r4 = r2.length
            int r5 = r3.length
            int r4 = r4 - r5
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.Record.compareTo(java.lang.Object):int");
    }

    public Name getAdditionalName() {
        return null;
    }

    static int checkU8(String field, int val) {
        if (val >= 0 && val <= 255) {
            return val;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"");
        stringBuffer.append(field);
        stringBuffer.append("\" ");
        stringBuffer.append(val);
        stringBuffer.append(" must be an unsigned 8 bit value");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    static int checkU16(String field, int val) {
        if (val >= 0 && val <= 65535) {
            return val;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"");
        stringBuffer.append(field);
        stringBuffer.append("\" ");
        stringBuffer.append(val);
        stringBuffer.append(" must be an unsigned 16 bit value");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    static long checkU32(String field, long val) {
        if (val >= 0 && val <= 4294967295L) {
            return val;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"");
        stringBuffer.append(field);
        stringBuffer.append("\" ");
        stringBuffer.append(val);
        stringBuffer.append(" must be an unsigned 32 bit value");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    static Name checkName(String field, Name name2) {
        if (name2.isAbsolute()) {
            return name2;
        }
        throw new RelativeNameException(name2);
    }

    static byte[] checkByteArrayLength(String field, byte[] array, int maxLength) {
        if (array.length <= 65535) {
            byte[] out = new byte[array.length];
            System.arraycopy(array, 0, out, 0, array.length);
            return out;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"");
        stringBuffer.append(field);
        stringBuffer.append("\" array must have no more than ");
        stringBuffer.append(maxLength);
        stringBuffer.append(" elements");
        throw new IllegalArgumentException(stringBuffer.toString());
    }
}
