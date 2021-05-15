package org.xbill.DNS;

import java.io.IOException;

public class CAARecord extends Record {
    private static final long serialVersionUID = 8544304287274216443L;
    private int flags;
    private byte[] tag;
    private byte[] value;

    public static class Flags {
        public static final int IssuerCritical = 128;

        private Flags() {
        }
    }

    CAARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new CAARecord();
    }

    public CAARecord(Name name, int dclass, long ttl, int flags2, String tag2, String value2) {
        super(name, Type.CAA, dclass, ttl);
        this.flags = checkU8("flags", flags2);
        try {
            this.tag = byteArrayFromString(tag2);
            this.value = byteArrayFromString(value2);
        } catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.flags = in.readU8();
        this.tag = in.readCountedString();
        this.value = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.flags = st.getUInt8();
        try {
            this.tag = byteArrayFromString(st.getString());
            this.value = byteArrayFromString(st.getString());
        } catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.flags);
        sb.append(" ");
        sb.append(byteArrayToString(this.tag, false));
        sb.append(" ");
        sb.append(byteArrayToString(this.value, true));
        return sb.toString();
    }

    public int getFlags() {
        return this.flags;
    }

    public String getTag() {
        return byteArrayToString(this.tag, false);
    }

    public String getValue() {
        return byteArrayToString(this.value, false);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(this.flags);
        out.writeCountedString(this.tag);
        out.writeByteArray(this.value);
    }
}
