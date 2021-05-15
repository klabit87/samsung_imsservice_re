package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import java.io.IOException;

public class SRVRecord extends Record {
    private static final long serialVersionUID = -3886460132387522052L;
    private int port;
    private int priority;
    private Name target;
    private int weight;

    SRVRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new SRVRecord();
    }

    public SRVRecord(Name name, int dclass, long ttl, int priority2, int weight2, int port2, Name target2) {
        super(name, 33, dclass, ttl);
        this.priority = checkU16("priority", priority2);
        this.weight = checkU16("weight", weight2);
        this.port = checkU16("port", port2);
        this.target = checkName(SoftphoneNamespaces.SoftphoneCallHandling.TARGET, target2);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.priority = in.readU16();
        this.weight = in.readU16();
        this.port = in.readU16();
        this.target = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.priority = st.getUInt16();
        this.weight = st.getUInt16();
        this.port = st.getUInt16();
        this.target = st.getName(origin);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.priority);
        stringBuffer.append(" ");
        sb.append(stringBuffer.toString());
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(this.weight);
        stringBuffer2.append(" ");
        sb.append(stringBuffer2.toString());
        StringBuffer stringBuffer3 = new StringBuffer();
        stringBuffer3.append(this.port);
        stringBuffer3.append(" ");
        sb.append(stringBuffer3.toString());
        sb.append(this.target);
        return sb.toString();
    }

    public int getPriority() {
        return this.priority;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getPort() {
        return this.port;
    }

    public Name getTarget() {
        return this.target;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.priority);
        out.writeU16(this.weight);
        out.writeU16(this.port);
        this.target.toWire(out, (Compression) null, canonical);
    }

    public Name getAdditionalName() {
        return this.target;
    }
}
