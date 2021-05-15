package org.xbill.DNS;

import java.io.IOException;

public class PXRecord extends Record {
    private static final long serialVersionUID = 1811540008806660667L;
    private Name map822;
    private Name mapX400;
    private int preference;

    PXRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new PXRecord();
    }

    public PXRecord(Name name, int dclass, long ttl, int preference2, Name map8222, Name mapX4002) {
        super(name, 26, dclass, ttl);
        this.preference = checkU16("preference", preference2);
        this.map822 = checkName("map822", map8222);
        this.mapX400 = checkName("mapX400", mapX4002);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.preference = in.readU16();
        this.map822 = new Name(in);
        this.mapX400 = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.preference = st.getUInt16();
        this.map822 = st.getName(origin);
        this.mapX400 = st.getName(origin);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.preference);
        sb.append(" ");
        sb.append(this.map822);
        sb.append(" ");
        sb.append(this.mapX400);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.preference);
        this.map822.toWire(out, (Compression) null, canonical);
        this.mapX400.toWire(out, (Compression) null, canonical);
    }

    public int getPreference() {
        return this.preference;
    }

    public Name getMap822() {
        return this.map822;
    }

    public Name getMapX400() {
        return this.mapX400;
    }
}
