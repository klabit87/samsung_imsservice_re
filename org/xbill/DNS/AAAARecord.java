package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AAAARecord extends Record {
    private static final long serialVersionUID = -4588601512069748050L;
    private byte[] address;

    AAAARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new AAAARecord();
    }

    public AAAARecord(Name name, int dclass, long ttl, InetAddress address2) {
        super(name, 28, dclass, ttl);
        if (Address.familyOf(address2) == 2) {
            this.address = address2.getAddress();
            return;
        }
        throw new IllegalArgumentException("invalid IPv6 address");
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.address = in.readByteArray(16);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.address = st.getAddressBytes(2);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        try {
            InetAddress addr = InetAddress.getByAddress((String) null, this.address);
            if (addr.getAddress().length != 4) {
                return addr.getHostAddress();
            }
            StringBuffer sb = new StringBuffer("0:0:0:0:0:ffff:");
            byte[] bArr = this.address;
            int high = ((bArr[12] & 255) << 8) + (bArr[13] & 255);
            int low = ((bArr[14] & 255) << 8) + (bArr[15] & 255);
            sb.append(Integer.toHexString(high));
            sb.append(':');
            sb.append(Integer.toHexString(low));
            return sb.toString();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public InetAddress getAddress() {
        try {
            if (this.name == null) {
                return InetAddress.getByAddress(this.address);
            }
            return InetAddress.getByAddress(this.name.toString(), this.address);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeByteArray(this.address);
    }
}
