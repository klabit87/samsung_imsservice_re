package org.xbill.DNS;

import java.io.IOException;

public class NAPTRRecord extends Record {
    private static final long serialVersionUID = 5191232392044947002L;
    private byte[] flags;
    private int order;
    private int preference;
    private byte[] regexp;
    private Name replacement;
    private byte[] service;

    NAPTRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NAPTRRecord();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NAPTRRecord(Name name, int dclass, long ttl, int order2, int preference2, String flags2, String service2, String regexp2, Name replacement2) {
        super(name, 35, dclass, ttl);
        int i = order2;
        this.order = checkU16("order", order2);
        int i2 = preference2;
        this.preference = checkU16("preference", preference2);
        try {
            this.flags = byteArrayFromString(flags2);
            this.service = byteArrayFromString(service2);
            this.regexp = byteArrayFromString(regexp2);
            this.replacement = checkName("replacement", replacement2);
        } catch (TextParseException e) {
            Name name2 = replacement2;
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.order = in.readU16();
        this.preference = in.readU16();
        this.flags = in.readCountedString();
        this.service = in.readCountedString();
        this.regexp = in.readCountedString();
        this.replacement = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.order = st.getUInt16();
        this.preference = st.getUInt16();
        try {
            this.flags = byteArrayFromString(st.getString());
            this.service = byteArrayFromString(st.getString());
            this.regexp = byteArrayFromString(st.getString());
            this.replacement = st.getName(origin);
        } catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.order);
        sb.append(" ");
        sb.append(this.preference);
        sb.append(" ");
        sb.append(byteArrayToString(this.flags, true));
        sb.append(" ");
        sb.append(byteArrayToString(this.service, true));
        sb.append(" ");
        sb.append(byteArrayToString(this.regexp, true));
        sb.append(" ");
        sb.append(this.replacement);
        return sb.toString();
    }

    public int getOrder() {
        return this.order;
    }

    public int getPreference() {
        return this.preference;
    }

    public String getFlags() {
        return byteArrayToString(this.flags, false);
    }

    public String getService() {
        return byteArrayToString(this.service, false);
    }

    public String getRegexp() {
        return byteArrayToString(this.regexp, false);
    }

    public Name getReplacement() {
        return this.replacement;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.order);
        out.writeU16(this.preference);
        out.writeCountedString(this.flags);
        out.writeCountedString(this.service);
        out.writeCountedString(this.regexp);
        this.replacement.toWire(out, (Compression) null, canonical);
    }

    public Name getAdditionalName() {
        return this.replacement;
    }
}
