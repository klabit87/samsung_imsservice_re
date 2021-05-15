package org.xbill.DNS;

import java.io.IOException;

class EmptyRecord extends Record {
    private static final long serialVersionUID = 3601852050646429582L;

    EmptyRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new EmptyRecord();
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return "";
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
    }
}
