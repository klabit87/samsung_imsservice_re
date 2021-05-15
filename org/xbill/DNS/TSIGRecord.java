package org.xbill.DNS;

import java.io.IOException;
import java.util.Date;
import org.xbill.DNS.utils.base64;

public class TSIGRecord extends Record {
    private static final long serialVersionUID = -88820909016649306L;
    private Name alg;
    private int error;
    private int fudge;
    private int originalID;
    private byte[] other;
    private byte[] signature;
    private Date timeSigned;

    TSIGRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new TSIGRecord();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TSIGRecord(Name name, int dclass, long ttl, Name alg2, Date timeSigned2, int fudge2, byte[] signature2, int originalID2, int error2, byte[] other2) {
        super(name, Type.TSIG, dclass, ttl);
        Name name2 = alg2;
        this.alg = checkName("alg", alg2);
        this.timeSigned = timeSigned2;
        int i = fudge2;
        this.fudge = checkU16("fudge", fudge2);
        this.signature = signature2;
        this.originalID = checkU16("originalID", originalID2);
        this.error = checkU16("error", error2);
        this.other = other2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.alg = new Name(in);
        this.timeSigned = new Date(1000 * ((((long) in.readU16()) << 32) + in.readU32()));
        this.fudge = in.readU16();
        this.signature = in.readByteArray(in.readU16());
        this.originalID = in.readU16();
        this.error = in.readU16();
        int otherLen = in.readU16();
        if (otherLen > 0) {
            this.other = in.readByteArray(otherLen);
        } else {
            this.other = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        throw st.exception("no text format defined for TSIG");
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.alg);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(this.timeSigned.getTime() / 1000);
        sb.append(" ");
        sb.append(this.fudge);
        sb.append(" ");
        sb.append(this.signature.length);
        if (Options.check("multiline")) {
            sb.append("\n");
            sb.append(base64.formatString(this.signature, 64, "\t", false));
        } else {
            sb.append(" ");
            sb.append(base64.toString(this.signature));
        }
        sb.append(" ");
        sb.append(Rcode.TSIGstring(this.error));
        sb.append(" ");
        byte[] bArr = this.other;
        if (bArr == null) {
            sb.append(0);
        } else {
            sb.append(bArr.length);
            if (Options.check("multiline")) {
                sb.append("\n\n\n\t");
            } else {
                sb.append(" ");
            }
            if (this.error == 18) {
                byte[] bArr2 = this.other;
                if (bArr2.length != 6) {
                    sb.append("<invalid BADTIME other data>");
                } else {
                    sb.append("<server time: ");
                    sb.append(new Date(1000 * ((((long) (bArr2[0] & 255)) << 40) + (((long) (bArr2[1] & 255)) << 32) + ((long) ((bArr2[2] & 255) << 24)) + ((long) ((bArr2[3] & 255) << 16)) + ((long) ((bArr2[4] & 255) << 8)) + ((long) (bArr2[5] & 255)))));
                    sb.append(">");
                }
            } else {
                sb.append("<");
                sb.append(base64.toString(this.other));
                sb.append(">");
            }
        }
        if (Options.check("multiline")) {
            sb.append(" )");
        }
        return sb.toString();
    }

    public Name getAlgorithm() {
        return this.alg;
    }

    public Date getTimeSigned() {
        return this.timeSigned;
    }

    public int getFudge() {
        return this.fudge;
    }

    public byte[] getSignature() {
        return this.signature;
    }

    public int getOriginalID() {
        return this.originalID;
    }

    public int getError() {
        return this.error;
    }

    public byte[] getOther() {
        return this.other;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.alg.toWire(out, (Compression) null, canonical);
        long time = this.timeSigned.getTime() / 1000;
        out.writeU16((int) (time >> 32));
        out.writeU32(4294967295L & time);
        out.writeU16(this.fudge);
        out.writeU16(this.signature.length);
        out.writeByteArray(this.signature);
        out.writeU16(this.originalID);
        out.writeU16(this.error);
        byte[] bArr = this.other;
        if (bArr != null) {
            out.writeU16(bArr.length);
            out.writeByteArray(this.other);
            return;
        }
        out.writeU16(0);
    }
}
