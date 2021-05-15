package org.xbill.DNS;

import java.io.IOException;

public class X25Record extends Record {
    private static final long serialVersionUID = 4267576252335579764L;
    private byte[] address;

    X25Record() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new X25Record();
    }

    private static final byte[] checkAndConvertAddress(String address2) {
        int length = address2.length();
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            char c = address2.charAt(i);
            if (!Character.isDigit(c)) {
                return null;
            }
            out[i] = (byte) c;
        }
        return out;
    }

    public X25Record(Name name, int dclass, long ttl, String address2) {
        super(name, 19, dclass, ttl);
        byte[] checkAndConvertAddress = checkAndConvertAddress(address2);
        this.address = checkAndConvertAddress;
        if (checkAndConvertAddress == null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("invalid PSDN address ");
            stringBuffer.append(address2);
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.address = in.readCountedString();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        String addr = st.getString();
        byte[] checkAndConvertAddress = checkAndConvertAddress(addr);
        this.address = checkAndConvertAddress;
        if (checkAndConvertAddress == null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("invalid PSDN address ");
            stringBuffer.append(addr);
            throw st.exception(stringBuffer.toString());
        }
    }

    public String getAddress() {
        return byteArrayToString(this.address, false);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeCountedString(this.address);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return byteArrayToString(this.address, true);
    }
}
