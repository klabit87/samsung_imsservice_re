package org.xbill.DNS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.xbill.DNS.utils.base16;

public class NSAPRecord extends Record {
    private static final long serialVersionUID = -1037209403185658593L;
    private byte[] address;

    NSAPRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSAPRecord();
    }

    private static final byte[] checkAndConvertAddress(String address2) {
        if (!address2.substring(0, 2).equalsIgnoreCase("0x")) {
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        boolean partial = false;
        int current = 0;
        for (int i = 2; i < address2.length(); i++) {
            char c = address2.charAt(i);
            if (c != '.') {
                int value = Character.digit(c, 16);
                if (value == -1) {
                    return null;
                }
                if (partial) {
                    current += value;
                    bytes.write(current);
                    partial = false;
                } else {
                    current = value << 4;
                    partial = true;
                }
            }
        }
        if (partial) {
            return null;
        }
        return bytes.toByteArray();
    }

    public NSAPRecord(Name name, int dclass, long ttl, String address2) {
        super(name, 22, dclass, ttl);
        byte[] checkAndConvertAddress = checkAndConvertAddress(address2);
        this.address = checkAndConvertAddress;
        if (checkAndConvertAddress == null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("invalid NSAP address ");
            stringBuffer.append(address2);
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.address = in.readByteArray();
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        String addr = st.getString();
        byte[] checkAndConvertAddress = checkAndConvertAddress(addr);
        this.address = checkAndConvertAddress;
        if (checkAndConvertAddress == null) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("invalid NSAP address ");
            stringBuffer.append(addr);
            throw st.exception(stringBuffer.toString());
        }
    }

    public String getAddress() {
        return byteArrayToString(this.address, false);
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.address);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("0x");
        stringBuffer.append(base16.toString(this.address));
        return stringBuffer.toString();
    }
}
