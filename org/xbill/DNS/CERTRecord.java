package org.xbill.DNS;

import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import java.io.IOException;
import org.xbill.DNS.DNSSEC;
import org.xbill.DNS.utils.base64;

public class CERTRecord extends Record {
    public static final int OID = 254;
    public static final int PGP = 3;
    public static final int PKIX = 1;
    public static final int SPKI = 2;
    public static final int URI = 253;
    private static final long serialVersionUID = 4763014646517016835L;
    private int alg;
    private byte[] cert;
    private int certType;
    private int keyTag;

    public static class CertificateType {
        public static final int ACPKIX = 7;
        public static final int IACPKIX = 8;
        public static final int IPGP = 6;
        public static final int IPKIX = 4;
        public static final int ISPKI = 5;
        public static final int OID = 254;
        public static final int PGP = 3;
        public static final int PKIX = 1;
        public static final int SPKI = 2;
        public static final int URI = 253;
        private static Mnemonic types;

        private CertificateType() {
        }

        static {
            Mnemonic mnemonic = new Mnemonic("Certificate type", 2);
            types = mnemonic;
            mnemonic.setMaximum(Message.MAXLENGTH);
            types.setNumericAllowed(true);
            types.add(1, "PKIX");
            types.add(2, "SPKI");
            types.add(3, "PGP");
            types.add(1, "IPKIX");
            types.add(2, "ISPKI");
            types.add(3, "IPGP");
            types.add(3, "ACPKIX");
            types.add(3, "IACPKIX");
            types.add(253, Constants.SIG_PROPERTY_URI_NAME);
            types.add(254, "OID");
        }

        public static String string(int type) {
            return types.getText(type);
        }

        public static int value(String s) {
            return types.getValue(s);
        }
    }

    CERTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new CERTRecord();
    }

    public CERTRecord(Name name, int dclass, long ttl, int certType2, int keyTag2, int alg2, byte[] cert2) {
        super(name, 37, dclass, ttl);
        this.certType = checkU16("certType", certType2);
        this.keyTag = checkU16("keyTag", keyTag2);
        this.alg = checkU8("alg", alg2);
        this.cert = cert2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.certType = in.readU16();
        this.keyTag = in.readU16();
        this.alg = in.readU8();
        this.cert = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        String certTypeString = st.getString();
        int value = CertificateType.value(certTypeString);
        this.certType = value;
        if (value >= 0) {
            this.keyTag = st.getUInt16();
            String algString = st.getString();
            int value2 = DNSSEC.Algorithm.value(algString);
            this.alg = value2;
            if (value2 >= 0) {
                this.cert = st.getBase64();
                return;
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("Invalid algorithm: ");
            stringBuffer.append(algString);
            throw st.exception(stringBuffer.toString());
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append("Invalid certificate type: ");
        stringBuffer2.append(certTypeString);
        throw st.exception(stringBuffer2.toString());
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.certType);
        sb.append(" ");
        sb.append(this.keyTag);
        sb.append(" ");
        sb.append(this.alg);
        if (this.cert != null) {
            if (Options.check("multiline")) {
                sb.append(" (\n");
                sb.append(base64.formatString(this.cert, 64, "\t", true));
            } else {
                sb.append(" ");
                sb.append(base64.toString(this.cert));
            }
        }
        return sb.toString();
    }

    public int getCertType() {
        return this.certType;
    }

    public int getKeyTag() {
        return this.keyTag;
    }

    public int getAlgorithm() {
        return this.alg;
    }

    public byte[] getCert() {
        return this.cert;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.certType);
        out.writeU16(this.keyTag);
        out.writeU8(this.alg);
        out.writeByteArray(this.cert);
    }
}
