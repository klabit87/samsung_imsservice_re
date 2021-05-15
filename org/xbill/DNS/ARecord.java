package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ARecord extends Record {
    private static final long serialVersionUID = -2172609200849142323L;
    private int addr;

    ARecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new ARecord();
    }

    private static final int fromArray(byte[] array) {
        return ((array[0] & 255) << 24) | ((array[1] & 255) << 16) | ((array[2] & 255) << 8) | (array[3] & 255);
    }

    private static final byte[] toArray(int addr2) {
        return new byte[]{(byte) ((addr2 >>> 24) & 255), (byte) ((addr2 >>> 16) & 255), (byte) ((addr2 >>> 8) & 255), (byte) (addr2 & 255)};
    }

    public ARecord(Name name, int dclass, long ttl, InetAddress address) {
        super(name, 1, dclass, ttl);
        if (Address.familyOf(address) == 1) {
            this.addr = fromArray(address.getAddress());
            return;
        }
        throw new IllegalArgumentException("invalid IPv4 address");
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.addr = fromArray(in.readByteArray(4));
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.addr = fromArray(st.getAddressBytes(1));
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        return Address.toDottedQuad(toArray(this.addr));
    }

    public InetAddress getAddress() {
        try {
            if (this.name == null) {
                return InetAddress.getByAddress(toArray(this.addr));
            }
            return InetAddress.getByAddress(this.name.toString(), toArray(this.addr));
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU32(((long) this.addr) & 4294967295L);
    }
}
