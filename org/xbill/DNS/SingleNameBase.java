package org.xbill.DNS;

import java.io.IOException;

abstract class SingleNameBase extends Record {
    private static final long serialVersionUID = -18595042501413L;
    protected Name singleName;

    protected SingleNameBase() {
    }

    protected SingleNameBase(Name name, int type, int dclass, long ttl) {
        super(name, type, dclass, ttl);
    }

    protected SingleNameBase(Name name, int type, int dclass, long ttl, Name singleName2, String description) {
        super(name, type, dclass, ttl);
        this.singleName = checkName(description, singleName2);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.singleName = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.singleName = st.getName(origin);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return this.singleName.toString();
    }

    /* access modifiers changed from: protected */
    public Name getSingleName() {
        return this.singleName;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.singleName.toWire(out, (Compression) null, canonical);
    }
}
