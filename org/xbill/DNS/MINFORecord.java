package org.xbill.DNS;

import java.io.IOException;

public class MINFORecord extends Record {
    private static final long serialVersionUID = -3962147172340353796L;
    private Name errorAddress;
    private Name responsibleAddress;

    MINFORecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MINFORecord();
    }

    public MINFORecord(Name name, int dclass, long ttl, Name responsibleAddress2, Name errorAddress2) {
        super(name, 14, dclass, ttl);
        this.responsibleAddress = checkName("responsibleAddress", responsibleAddress2);
        this.errorAddress = checkName("errorAddress", errorAddress2);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.responsibleAddress = new Name(in);
        this.errorAddress = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.responsibleAddress = st.getName(origin);
        this.errorAddress = st.getName(origin);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.responsibleAddress);
        sb.append(" ");
        sb.append(this.errorAddress);
        return sb.toString();
    }

    public Name getResponsibleAddress() {
        return this.responsibleAddress;
    }

    public Name getErrorAddress() {
        return this.errorAddress;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.responsibleAddress.toWire(out, (Compression) null, canonical);
        this.errorAddress.toWire(out, (Compression) null, canonical);
    }
}
