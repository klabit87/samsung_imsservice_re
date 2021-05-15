package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import java.io.IOException;

public class SOARecord extends Record {
    private static final long serialVersionUID = 1049740098229303931L;
    private Name admin;
    private long expire;
    private Name host;
    private long minimum;
    private long refresh;
    private long retry;
    private long serial;

    SOARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SOARecord();
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public SOARecord(Name name, int dclass, long ttl, Name host2, Name admin2, long serial2, long refresh2, long retry2, long expire2, long minimum2) {
        super(name, 6, dclass, ttl);
        this.host = checkName("host", host2);
        this.admin = checkName("admin", admin2);
        this.serial = checkU32("serial", serial2);
        this.refresh = checkU32("refresh", refresh2);
        this.retry = checkU32(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY, retry2);
        this.expire = checkU32("expire", expire2);
        this.minimum = checkU32("minimum", minimum2);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.host = new Name(in);
        this.admin = new Name(in);
        this.serial = in.readU32();
        this.refresh = in.readU32();
        this.retry = in.readU32();
        this.expire = in.readU32();
        this.minimum = in.readU32();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.host = st.getName(origin);
        this.admin = st.getName(origin);
        this.serial = st.getUInt32();
        this.refresh = st.getTTLLike();
        this.retry = st.getTTLLike();
        this.expire = st.getTTLLike();
        this.minimum = st.getTTLLike();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.host);
        sb.append(" ");
        sb.append(this.admin);
        if (Options.check("multiline")) {
            sb.append(" (\n\t\t\t\t\t");
            sb.append(this.serial);
            sb.append("\t; serial\n\t\t\t\t\t");
            sb.append(this.refresh);
            sb.append("\t; refresh\n\t\t\t\t\t");
            sb.append(this.retry);
            sb.append("\t; retry\n\t\t\t\t\t");
            sb.append(this.expire);
            sb.append("\t; expire\n\t\t\t\t\t");
            sb.append(this.minimum);
            sb.append(" )\t; minimum");
        } else {
            sb.append(" ");
            sb.append(this.serial);
            sb.append(" ");
            sb.append(this.refresh);
            sb.append(" ");
            sb.append(this.retry);
            sb.append(" ");
            sb.append(this.expire);
            sb.append(" ");
            sb.append(this.minimum);
        }
        return sb.toString();
    }

    public Name getHost() {
        return this.host;
    }

    public Name getAdmin() {
        return this.admin;
    }

    public long getSerial() {
        return this.serial;
    }

    public long getRefresh() {
        return this.refresh;
    }

    public long getRetry() {
        return this.retry;
    }

    public long getExpire() {
        return this.expire;
    }

    public long getMinimum() {
        return this.minimum;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.host.toWire(out, c, canonical);
        this.admin.toWire(out, c, canonical);
        out.writeU32(this.serial);
        out.writeU32(this.refresh);
        out.writeU32(this.retry);
        out.writeU32(this.expire);
        out.writeU32(this.minimum);
    }
}
