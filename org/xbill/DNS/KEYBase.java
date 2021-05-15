package org.xbill.DNS;

import java.io.IOException;
import java.security.PublicKey;
import org.xbill.DNS.DNSSEC;
import org.xbill.DNS.utils.base64;

abstract class KEYBase extends Record {
    private static final long serialVersionUID = 3469321722693285454L;
    protected int alg;
    protected int flags;
    protected int footprint = -1;
    protected byte[] key;
    protected int proto;
    protected PublicKey publicKey = null;

    protected KEYBase() {
    }

    public KEYBase(Name name, int type, int dclass, long ttl, int flags2, int proto2, int alg2, byte[] key2) {
        super(name, type, dclass, ttl);
        this.flags = checkU16("flags", flags2);
        this.proto = checkU8("proto", proto2);
        this.alg = checkU8("alg", alg2);
        this.key = key2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.flags = in.readU16();
        this.proto = in.readU8();
        this.alg = in.readU8();
        if (in.remaining() > 0) {
            this.key = in.readByteArray();
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.flags);
        sb.append(" ");
        sb.append(this.proto);
        sb.append(" ");
        sb.append(this.alg);
        if (this.key != null) {
            if (Options.check("multiline")) {
                sb.append(" (\n");
                sb.append(base64.formatString(this.key, 64, "\t", true));
                sb.append(" ; key_tag = ");
                sb.append(getFootprint());
            } else {
                sb.append(" ");
                sb.append(base64.toString(this.key));
            }
        }
        return sb.toString();
    }

    public int getFlags() {
        return this.flags;
    }

    public int getProtocol() {
        return this.proto;
    }

    public int getAlgorithm() {
        return this.alg;
    }

    public byte[] getKey() {
        return this.key;
    }

    public int getFootprint() {
        int foot;
        int i = this.footprint;
        if (i >= 0) {
            return i;
        }
        int foot2 = 0;
        DNSOutput out = new DNSOutput();
        rrToWire(out, (Compression) null, false);
        byte[] rdata = out.toByteArray();
        if (this.alg == 1) {
            foot = ((rdata[rdata.length - 3] & 255) << 8) + (rdata[rdata.length - 2] & 255);
        } else {
            int i2 = 0;
            while (i2 < rdata.length - 1) {
                foot2 += ((rdata[i2] & 255) << 8) + (rdata[i2 + 1] & 255);
                i2 += 2;
            }
            if (i2 < rdata.length) {
                foot2 += (rdata[i2] & 255) << 8;
            }
            foot = foot2 + ((foot2 >> 16) & Message.MAXLENGTH);
        }
        int foot3 = foot & Message.MAXLENGTH;
        this.footprint = foot3;
        return foot3;
    }

    public PublicKey getPublicKey() throws DNSSEC.DNSSECException {
        PublicKey publicKey2 = this.publicKey;
        if (publicKey2 != null) {
            return publicKey2;
        }
        PublicKey publicKey3 = DNSSEC.toPublicKey(this);
        this.publicKey = publicKey3;
        return publicKey3;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.flags);
        out.writeU8(this.proto);
        out.writeU8(this.alg);
        byte[] bArr = this.key;
        if (bArr != null) {
            out.writeByteArray(bArr);
        }
    }
}
