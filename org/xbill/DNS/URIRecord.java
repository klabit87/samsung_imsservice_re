package org.xbill.DNS;

import java.io.IOException;

public class URIRecord extends Record {
    private static final long serialVersionUID = 7955422413971804232L;
    private int priority;
    private byte[] target;
    private int weight;

    URIRecord() {
        this.target = new byte[0];
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new URIRecord();
    }

    public URIRecord(Name name, int dclass, long ttl, int priority2, int weight2, String target2) {
        super(name, 256, dclass, ttl);
        this.priority = checkU16("priority", priority2);
        this.weight = checkU16("weight", weight2);
        try {
            this.target = byteArrayFromString(target2);
        } catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.priority = in.readU16();
        this.weight = in.readU16();
        this.target = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.priority = st.getUInt16();
        this.weight = st.getUInt16();
        try {
            this.target = byteArrayFromString(st.getString());
        } catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.priority);
        stringBuffer.append(" ");
        sb.append(stringBuffer.toString());
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(this.weight);
        stringBuffer2.append(" ");
        sb.append(stringBuffer2.toString());
        sb.append(byteArrayToString(this.target, true));
        return sb.toString();
    }

    public int getPriority() {
        return this.priority;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getTarget() {
        return byteArrayToString(this.target, false);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.priority);
        out.writeU16(this.weight);
        out.writeByteArray(this.target);
    }
}
