package org.xbill.DNS;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.xbill.DNS.utils.base16;

public class NSEC3PARAMRecord extends Record {
    private static final long serialVersionUID = -8689038598776316533L;
    private int flags;
    private int hashAlg;
    private int iterations;
    private byte[] salt;

    NSEC3PARAMRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSEC3PARAMRecord();
    }

    public NSEC3PARAMRecord(Name name, int dclass, long ttl, int hashAlg2, int flags2, int iterations2, byte[] salt2) {
        super(name, 51, dclass, ttl);
        this.hashAlg = checkU8("hashAlg", hashAlg2);
        this.flags = checkU8("flags", flags2);
        this.iterations = checkU16("iterations", iterations2);
        if (salt2 == null) {
            return;
        }
        if (salt2.length > 255) {
            throw new IllegalArgumentException("Invalid salt length");
        } else if (salt2.length > 0) {
            byte[] bArr = new byte[salt2.length];
            this.salt = bArr;
            System.arraycopy(salt2, 0, bArr, 0, salt2.length);
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.hashAlg = in.readU8();
        this.flags = in.readU8();
        this.iterations = in.readU16();
        int salt_length = in.readU8();
        if (salt_length > 0) {
            this.salt = in.readByteArray(salt_length);
        } else {
            this.salt = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(this.hashAlg);
        out.writeU8(this.flags);
        out.writeU16(this.iterations);
        byte[] bArr = this.salt;
        if (bArr != null) {
            out.writeU8(bArr.length);
            out.writeByteArray(this.salt);
            return;
        }
        out.writeU8(0);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.hashAlg = st.getUInt8();
        this.flags = st.getUInt8();
        this.iterations = st.getUInt16();
        if (st.getString().equals("-")) {
            this.salt = null;
            return;
        }
        st.unget();
        byte[] hexString = st.getHexString();
        this.salt = hexString;
        if (hexString.length > 255) {
            throw st.exception("salt value too long");
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.hashAlg);
        sb.append(' ');
        sb.append(this.flags);
        sb.append(' ');
        sb.append(this.iterations);
        sb.append(' ');
        byte[] bArr = this.salt;
        if (bArr == null) {
            sb.append('-');
        } else {
            sb.append(base16.toString(bArr));
        }
        return sb.toString();
    }

    public int getHashAlgorithm() {
        return this.hashAlg;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getIterations() {
        return this.iterations;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public byte[] hashName(Name name) throws NoSuchAlgorithmException {
        return NSEC3Record.hashName(name, this.hashAlg, this.iterations, this.salt);
    }
}
