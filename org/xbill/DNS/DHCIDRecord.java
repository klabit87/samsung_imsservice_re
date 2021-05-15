package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base64;

public class DHCIDRecord extends Record {
    private static final long serialVersionUID = -8214820200808997707L;
    private byte[] data;

    DHCIDRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DHCIDRecord();
    }

    public DHCIDRecord(Name name, int dclass, long ttl, byte[] data2) {
        super(name, 49, dclass, ttl);
        this.data = data2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.data = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.data = st.getBase64();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.data);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return base64.toString(this.data);
    }

    public byte[] getData() {
        return this.data;
    }
}
