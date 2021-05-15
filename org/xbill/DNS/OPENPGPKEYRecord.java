package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base64;

public class OPENPGPKEYRecord extends Record {
    private static final long serialVersionUID = -1277262990243423062L;
    private byte[] cert;

    OPENPGPKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new OPENPGPKEYRecord();
    }

    public OPENPGPKEYRecord(Name name, int dclass, long ttl, byte[] cert2) {
        super(name, 61, dclass, ttl);
        this.cert = cert2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.cert = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.cert = st.getBase64();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        if (this.cert != null) {
            if (Options.check("multiline")) {
                sb.append("(\n");
                sb.append(base64.formatString(this.cert, 64, "\t", true));
            } else {
                sb.append(base64.toString(this.cert));
            }
        }
        return sb.toString();
    }

    public byte[] getCert() {
        return this.cert;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.cert);
    }
}
