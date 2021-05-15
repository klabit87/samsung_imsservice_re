package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.DNSSEC;

public class DNSKEYRecord extends KEYBase {
    private static final long serialVersionUID = -8679800040426675002L;

    public static class Protocol {
        public static final int DNSSEC = 3;

        private Protocol() {
        }
    }

    public static class Flags {
        public static final int REVOKE = 128;
        public static final int SEP_KEY = 1;
        public static final int ZONE_KEY = 256;

        private Flags() {
        }
    }

    DNSKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DNSKEYRecord();
    }

    public DNSKEYRecord(Name name, int dclass, long ttl, int flags, int proto, int alg, byte[] key) {
        super(name, 48, dclass, ttl, flags, proto, alg, key);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public DNSKEYRecord(org.xbill.DNS.Name r13, int r14, long r15, int r17, int r18, int r19, java.security.PublicKey r20) throws org.xbill.DNS.DNSSEC.DNSSECException {
        /*
            r12 = this;
            r0 = r20
            r11 = r19
            byte[] r10 = org.xbill.DNS.DNSSEC.fromPublicKey(r0, r11)
            r3 = 48
            r1 = r12
            r2 = r13
            r4 = r14
            r5 = r15
            r7 = r17
            r8 = r18
            r9 = r19
            r1.<init>(r2, r3, r4, r5, r7, r8, r9, r10)
            r1.publicKey = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: org.xbill.DNS.DNSKEYRecord.<init>(org.xbill.DNS.Name, int, long, int, int, int, java.security.PublicKey):void");
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.flags = st.getUInt16();
        this.proto = st.getUInt8();
        String algString = st.getString();
        this.alg = DNSSEC.Algorithm.value(algString);
        if (this.alg >= 0) {
            this.key = st.getBase64();
            return;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Invalid algorithm: ");
        stringBuffer.append(algString);
        throw st.exception(stringBuffer.toString());
    }
}
