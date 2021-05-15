package org.xbill.DNS;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.xbill.DNS.utils.base16;
import org.xbill.DNS.utils.base32;

public class NSEC3Record extends Record {
    public static final int SHA1_DIGEST_ID = 1;
    private static final base32 b32 = new base32(base32.Alphabet.BASE32HEX, false, false);
    private static final long serialVersionUID = -7123504635968932855L;
    private int flags;
    private int hashAlg;
    private int iterations;
    private byte[] next;
    private byte[] salt;
    private TypeBitmap types;

    public static class Flags {
        public static final int OPT_OUT = 1;

        private Flags() {
        }
    }

    public static class Digest {
        public static final int SHA1 = 1;

        private Digest() {
        }
    }

    NSEC3Record() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSEC3Record();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NSEC3Record(Name name, int dclass, long ttl, int hashAlg2, int flags2, int iterations2, byte[] salt2, byte[] next2, int[] types2) {
        super(name, 50, dclass, ttl);
        byte[] bArr = salt2;
        byte[] bArr2 = next2;
        int i = hashAlg2;
        this.hashAlg = checkU8("hashAlg", hashAlg2);
        this.flags = checkU8("flags", flags2);
        this.iterations = checkU16("iterations", iterations2);
        if (bArr != null) {
            if (bArr.length > 255) {
                throw new IllegalArgumentException("Invalid salt");
            } else if (bArr.length > 0) {
                byte[] bArr3 = new byte[bArr.length];
                this.salt = bArr3;
                System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
            }
        }
        if (bArr2.length <= 255) {
            byte[] bArr4 = new byte[bArr2.length];
            this.next = bArr4;
            System.arraycopy(bArr2, 0, bArr4, 0, bArr2.length);
            this.types = new TypeBitmap(types2);
            return;
        }
        int[] iArr = types2;
        throw new IllegalArgumentException("Invalid next hash");
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
        this.next = in.readByteArray(in.readU8());
        this.types = new TypeBitmap(in);
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
        } else {
            out.writeU8(0);
        }
        out.writeU8(this.next.length);
        out.writeByteArray(this.next);
        this.types.toWire(out);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.hashAlg = st.getUInt8();
        this.flags = st.getUInt8();
        this.iterations = st.getUInt16();
        if (st.getString().equals("-")) {
            this.salt = null;
        } else {
            st.unget();
            byte[] hexString = st.getHexString();
            this.salt = hexString;
            if (hexString.length > 255) {
                throw st.exception("salt value too long");
            }
        }
        this.next = st.getBase32String(b32);
        this.types = new TypeBitmap(st);
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
        sb.append(' ');
        sb.append(b32.toString(this.next));
        if (!this.types.empty()) {
            sb.append(' ');
            sb.append(this.types.toString());
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

    public byte[] getNext() {
        return this.next;
    }

    public int[] getTypes() {
        return this.types.toArray();
    }

    public boolean hasType(int type) {
        return this.types.contains(type);
    }

    static byte[] hashName(Name name, int hashAlg2, int iterations2, byte[] salt2) throws NoSuchAlgorithmException {
        if (hashAlg2 == 1) {
            MessageDigest digest = MessageDigest.getInstance("sha-1");
            byte[] hash = null;
            for (int i = 0; i <= iterations2; i++) {
                digest.reset();
                if (i == 0) {
                    digest.update(name.toWireCanonical());
                } else {
                    digest.update(hash);
                }
                if (salt2 != null) {
                    digest.update(salt2);
                }
                hash = digest.digest();
            }
            return hash;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Unknown NSEC3 algorithmidentifier: ");
        stringBuffer.append(hashAlg2);
        throw new NoSuchAlgorithmException(stringBuffer.toString());
    }

    public byte[] hashName(Name name) throws NoSuchAlgorithmException {
        return hashName(name, this.hashAlg, this.iterations, this.salt);
    }
}
