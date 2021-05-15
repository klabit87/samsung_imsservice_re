package org.xbill.DNS;

import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class TLSARecord extends Record {
    private static final long serialVersionUID = 356494267028580169L;
    private byte[] certificateAssociationData;
    private int certificateUsage;
    private int matchingType;
    private int selector;

    public static class CertificateUsage {
        public static final int CA_CONSTRAINT = 0;
        public static final int DOMAIN_ISSUED_CERTIFICATE = 3;
        public static final int SERVICE_CERTIFICATE_CONSTRAINT = 1;
        public static final int TRUST_ANCHOR_ASSERTION = 2;

        private CertificateUsage() {
        }
    }

    public static class Selector {
        public static final int FULL_CERTIFICATE = 0;
        public static final int SUBJECT_PUBLIC_KEY_INFO = 1;

        private Selector() {
        }
    }

    public static class MatchingType {
        public static final int EXACT = 0;
        public static final int SHA256 = 1;
        public static final int SHA512 = 2;

        private MatchingType() {
        }
    }

    TLSARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new TLSARecord();
    }

    public TLSARecord(Name name, int dclass, long ttl, int certificateUsage2, int selector2, int matchingType2, byte[] certificateAssociationData2) {
        super(name, 52, dclass, ttl);
        this.certificateUsage = checkU8("certificateUsage", certificateUsage2);
        this.selector = checkU8("selector", selector2);
        this.matchingType = checkU8("matchingType", matchingType2);
        this.certificateAssociationData = checkByteArrayLength("certificateAssociationData", certificateAssociationData2, Message.MAXLENGTH);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.certificateUsage = in.readU8();
        this.selector = in.readU8();
        this.matchingType = in.readU8();
        this.certificateAssociationData = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.certificateUsage = st.getUInt8();
        this.selector = st.getUInt8();
        this.matchingType = st.getUInt8();
        this.certificateAssociationData = st.getHex();
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.certificateUsage);
        sb.append(" ");
        sb.append(this.selector);
        sb.append(" ");
        sb.append(this.matchingType);
        sb.append(" ");
        sb.append(base16.toString(this.certificateAssociationData));
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(this.certificateUsage);
        out.writeU8(this.selector);
        out.writeU8(this.matchingType);
        out.writeByteArray(this.certificateAssociationData);
    }

    public int getCertificateUsage() {
        return this.certificateUsage;
    }

    public int getSelector() {
        return this.selector;
    }

    public int getMatchingType() {
        return this.matchingType;
    }

    public final byte[] getCertificateAssociationData() {
        return this.certificateAssociationData;
    }
}
