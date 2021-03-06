package org.xbill.DNS;

import java.io.IOException;
import java.util.Date;
import org.xbill.DNS.DNSSEC;
import org.xbill.DNS.utils.base64;

abstract class SIGBase extends Record {
    private static final long serialVersionUID = -3738444391533812369L;
    protected int alg;
    protected int covered;
    protected Date expire;
    protected int footprint;
    protected int labels;
    protected long origttl;
    protected byte[] signature;
    protected Name signer;
    protected Date timeSigned;

    protected SIGBase() {
    }

    public SIGBase(Name name, int type, int dclass, long ttl, int covered2, int alg2, long origttl2, Date expire2, Date timeSigned2, int footprint2, Name signer2, byte[] signature2) {
        super(name, type, dclass, ttl);
        Type.check(covered2);
        TTL.check(origttl2);
        this.covered = covered2;
        this.alg = checkU8("alg", alg2);
        this.labels = name.labels() - 1;
        if (name.isWild()) {
            this.labels--;
        }
        this.origttl = origttl2;
        this.expire = expire2;
        this.timeSigned = timeSigned2;
        this.footprint = checkU16("footprint", footprint2);
        this.signer = checkName("signer", signer2);
        this.signature = signature2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.covered = in.readU16();
        this.alg = in.readU8();
        this.labels = in.readU8();
        this.origttl = in.readU32();
        this.expire = new Date(in.readU32() * 1000);
        this.timeSigned = new Date(in.readU32() * 1000);
        this.footprint = in.readU16();
        this.signer = new Name(in);
        this.signature = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        String typeString = st.getString();
        int value = Type.value(typeString);
        this.covered = value;
        if (value >= 0) {
            String algString = st.getString();
            int value2 = DNSSEC.Algorithm.value(algString);
            this.alg = value2;
            if (value2 >= 0) {
                this.labels = st.getUInt8();
                this.origttl = st.getTTL();
                this.expire = FormattedTime.parse(st.getString());
                this.timeSigned = FormattedTime.parse(st.getString());
                this.footprint = st.getUInt16();
                this.signer = st.getName(origin);
                this.signature = st.getBase64();
                return;
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Invalid algorithm: ");
            stringBuffer.append(algString);
            throw st.exception(stringBuffer.toString());
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append("Invalid type: ");
        stringBuffer2.append(typeString);
        throw st.exception(stringBuffer2.toString());
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(Type.string(this.covered));
        sb.append(" ");
        sb.append(this.alg);
        sb.append(" ");
        sb.append(this.labels);
        sb.append(" ");
        sb.append(this.origttl);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(FormattedTime.format(this.expire));
        sb.append(" ");
        sb.append(FormattedTime.format(this.timeSigned));
        sb.append(" ");
        sb.append(this.footprint);
        sb.append(" ");
        sb.append(this.signer);
        if (Options.check("multiline")) {
            sb.append("\n");
            sb.append(base64.formatString(this.signature, 64, "\t", true));
        } else {
            sb.append(" ");
            sb.append(base64.toString(this.signature));
        }
        return sb.toString();
    }

    public int getTypeCovered() {
        return this.covered;
    }

    public int getAlgorithm() {
        return this.alg;
    }

    public int getLabels() {
        return this.labels;
    }

    public long getOrigTTL() {
        return this.origttl;
    }

    public Date getExpire() {
        return this.expire;
    }

    public Date getTimeSigned() {
        return this.timeSigned;
    }

    public int getFootprint() {
        return this.footprint;
    }

    public Name getSigner() {
        return this.signer;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    /* access modifiers changed from: package-private */
    public void setSignature(byte[] signature2) {
        this.signature = signature2;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.covered);
        out.writeU8(this.alg);
        out.writeU8(this.labels);
        out.writeU32(this.origttl);
        out.writeU32(this.expire.getTime() / 1000);
        out.writeU32(this.timeSigned.getTime() / 1000);
        out.writeU16(this.footprint);
        this.signer.toWire(out, (Compression) null, canonical);
        out.writeByteArray(this.signature);
    }
}
