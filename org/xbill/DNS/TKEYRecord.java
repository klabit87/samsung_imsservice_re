package org.xbill.DNS;

import com.sec.internal.helper.httpclient.HttpController;
import java.io.IOException;
import java.util.Date;
import org.xbill.DNS.utils.base64;

public class TKEYRecord extends Record {
    public static final int DELETE = 5;
    public static final int DIFFIEHELLMAN = 2;
    public static final int GSSAPI = 3;
    public static final int RESOLVERASSIGNED = 4;
    public static final int SERVERASSIGNED = 1;
    private static final long serialVersionUID = 8828458121926391756L;
    private Name alg;
    private int error;
    private byte[] key;
    private int mode;
    private byte[] other;
    private Date timeExpire;
    private Date timeInception;

    TKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new TKEYRecord();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TKEYRecord(Name name, int dclass, long ttl, Name alg2, Date timeInception2, Date timeExpire2, int mode2, int error2, byte[] key2, byte[] other2) {
        super(name, Type.TKEY, dclass, ttl);
        Name name2 = alg2;
        this.alg = checkName("alg", alg2);
        this.timeInception = timeInception2;
        this.timeExpire = timeExpire2;
        this.mode = checkU16("mode", mode2);
        this.error = checkU16("error", error2);
        this.key = key2;
        this.other = other2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.alg = new Name(in);
        this.timeInception = new Date(in.readU32() * 1000);
        this.timeExpire = new Date(in.readU32() * 1000);
        this.mode = in.readU16();
        this.error = in.readU16();
        int keylen = in.readU16();
        if (keylen > 0) {
            this.key = in.readByteArray(keylen);
        } else {
            this.key = null;
        }
        int otherlen = in.readU16();
        if (otherlen > 0) {
            this.other = in.readByteArray(otherlen);
        } else {
            this.other = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        throw st.exception("no text format defined for TKEY");
    }

    /* access modifiers changed from: protected */
    public String modeString() {
        int i = this.mode;
        if (i == 1) {
            return "SERVERASSIGNED";
        }
        if (i == 2) {
            return "DIFFIEHELLMAN";
        }
        if (i == 3) {
            return "GSSAPI";
        }
        if (i == 4) {
            return "RESOLVERASSIGNED";
        }
        if (i != 5) {
            return Integer.toString(i);
        }
        return HttpController.METHOD_DELETE;
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.alg);
        sb.append(" ");
        if (Options.check("multiline")) {
            sb.append("(\n\t");
        }
        sb.append(FormattedTime.format(this.timeInception));
        sb.append(" ");
        sb.append(FormattedTime.format(this.timeExpire));
        sb.append(" ");
        sb.append(modeString());
        sb.append(" ");
        sb.append(Rcode.TSIGstring(this.error));
        if (Options.check("multiline")) {
            sb.append("\n");
            byte[] bArr = this.key;
            if (bArr != null) {
                sb.append(base64.formatString(bArr, 64, "\t", false));
                sb.append("\n");
            }
            byte[] bArr2 = this.other;
            if (bArr2 != null) {
                sb.append(base64.formatString(bArr2, 64, "\t", false));
            }
            sb.append(" )");
        } else {
            sb.append(" ");
            byte[] bArr3 = this.key;
            if (bArr3 != null) {
                sb.append(base64.toString(bArr3));
                sb.append(" ");
            }
            byte[] bArr4 = this.other;
            if (bArr4 != null) {
                sb.append(base64.toString(bArr4));
            }
        }
        return sb.toString();
    }

    public Name getAlgorithm() {
        return this.alg;
    }

    public Date getTimeInception() {
        return this.timeInception;
    }

    public Date getTimeExpire() {
        return this.timeExpire;
    }

    public int getMode() {
        return this.mode;
    }

    public int getError() {
        return this.error;
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] getOther() {
        return this.other;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.alg.toWire(out, (Compression) null, canonical);
        out.writeU32(this.timeInception.getTime() / 1000);
        out.writeU32(this.timeExpire.getTime() / 1000);
        out.writeU16(this.mode);
        out.writeU16(this.error);
        byte[] bArr = this.key;
        if (bArr != null) {
            out.writeU16(bArr.length);
            out.writeByteArray(this.key);
        } else {
            out.writeU16(0);
        }
        byte[] bArr2 = this.other;
        if (bArr2 != null) {
            out.writeU16(bArr2.length);
            out.writeByteArray(this.other);
            return;
        }
        out.writeU16(0);
    }
}
