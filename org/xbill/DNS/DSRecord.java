package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class DSRecord extends Record {
    public static final int GOST3411_DIGEST_ID = 3;
    public static final int SHA1_DIGEST_ID = 1;
    public static final int SHA256_DIGEST_ID = 2;
    public static final int SHA384_DIGEST_ID = 4;
    private static final long serialVersionUID = -9001819329700081493L;
    private int alg;
    private byte[] digest;
    private int digestid;
    private int footprint;

    public static class Digest {
        public static final int GOST3411 = 3;
        public static final int SHA1 = 1;
        public static final int SHA256 = 2;
        public static final int SHA384 = 4;

        private Digest() {
        }
    }

    DSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new DSRecord();
    }

    public DSRecord(Name name, int dclass, long ttl, int footprint2, int alg2, int digestid2, byte[] digest2) {
        super(name, 43, dclass, ttl);
        this.footprint = checkU16("footprint", footprint2);
        this.alg = checkU8("alg", alg2);
        this.digestid = checkU8("digestid", digestid2);
        this.digest = digest2;
    }

    public DSRecord(Name name, int dclass, long ttl, int digestid2, DNSKEYRecord key) {
        this(name, dclass, ttl, key.getFootprint(), key.getAlgorithm(), digestid2, DNSSEC.generateDSDigest(key, digestid2));
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.footprint = in.readU16();
        this.alg = in.readU8();
        this.digestid = in.readU8();
        this.digest = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.footprint = st.getUInt16();
        this.alg = st.getUInt8();
        this.digestid = st.getUInt8();
        this.digest = st.getHex();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.footprint);
        sb.append(" ");
        sb.append(this.alg);
        sb.append(" ");
        sb.append(this.digestid);
        if (this.digest != null) {
            sb.append(" ");
            sb.append(base16.toString(this.digest));
        }
        return sb.toString();
    }

    public int getAlgorithm() {
        return this.alg;
    }

    public int getDigestID() {
        return this.digestid;
    }

    public byte[] getDigest() {
        return this.digest;
    }

    public int getFootprint() {
        return this.footprint;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.footprint);
        out.writeU8(this.alg);
        out.writeU8(this.digestid);
        byte[] bArr = this.digest;
        if (bArr != null) {
            out.writeByteArray(bArr);
        }
    }
}
