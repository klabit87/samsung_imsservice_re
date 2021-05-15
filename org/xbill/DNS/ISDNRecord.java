package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.Tokenizer;

public class ISDNRecord extends Record {
    private static final long serialVersionUID = -8730801385178968798L;
    private byte[] address;
    private byte[] subAddress;

    ISDNRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new ISDNRecord();
    }

    public ISDNRecord(Name name, int dclass, long ttl, String address2, String subAddress2) {
        super(name, 20, dclass, ttl);
        try {
            this.address = byteArrayFromString(address2);
            if (subAddress2 != null) {
                this.subAddress = byteArrayFromString(subAddress2);
            }
        } catch (TextParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.address = in.readCountedString();
        if (in.remaining() > 0) {
            this.subAddress = in.readCountedString();
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        try {
            this.address = byteArrayFromString(st.getString());
            Tokenizer.Token t = st.get();
            if (t.isString()) {
                this.subAddress = byteArrayFromString(t.value);
            } else {
                st.unget();
            }
        } catch (TextParseException e) {
            throw st.exception(e.getMessage());
        }
    }

    public String getAddress() {
        return byteArrayToString(this.address, false);
    }

    public String getSubAddress() {
        byte[] bArr = this.subAddress;
        if (bArr == null) {
            return null;
        }
        return byteArrayToString(bArr, false);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeCountedString(this.address);
        byte[] bArr = this.subAddress;
        if (bArr != null) {
            out.writeCountedString(bArr);
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(byteArrayToString(this.address, true));
        if (this.subAddress != null) {
            sb.append(" ");
            sb.append(byteArrayToString(this.subAddress, true));
        }
        return sb.toString();
    }
}
