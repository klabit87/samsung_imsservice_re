package org.xbill.DNS;

import java.io.IOException;

public class NULLRecord extends Record {
    private static final long serialVersionUID = -5796493183235216538L;
    private byte[] data;

    NULLRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NULLRecord();
    }

    public NULLRecord(Name name, int dclass, long ttl, byte[] data2) {
        super(name, 10, dclass, ttl);
        if (data2.length <= 65535) {
            this.data = data2;
            return;
        }
        throw new IllegalArgumentException("data must be <65536 bytes");
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.data = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        throw st.exception("no defined text format for NULL records");
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return unknownToString(this.data);
    }

    public byte[] getData() {
        return this.data;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.data);
    }
}
