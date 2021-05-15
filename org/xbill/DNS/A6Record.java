package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class A6Record extends Record {
    private static final long serialVersionUID = -8815026887337346789L;
    private Name prefix;
    private int prefixBits;
    private InetAddress suffix;

    A6Record() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new A6Record();
    }

    public A6Record(Name name, int dclass, long ttl, int prefixBits2, InetAddress suffix2, Name prefix2) {
        super(name, 38, dclass, ttl);
        this.prefixBits = checkU8("prefixBits", prefixBits2);
        if (suffix2 == null || Address.familyOf(suffix2) == 2) {
            this.suffix = suffix2;
            if (prefix2 != null) {
                this.prefix = checkName("prefix", prefix2);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("invalid IPv6 address");
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        int readU8 = in.readU8();
        this.prefixBits = readU8;
        int suffixbytes = ((128 - readU8) + 7) / 8;
        if (readU8 < 128) {
            byte[] bytes = new byte[16];
            in.readByteArray(bytes, 16 - suffixbytes, suffixbytes);
            this.suffix = InetAddress.getByAddress(bytes);
        }
        if (this.prefixBits > 0) {
            this.prefix = new Name(in);
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        int uInt8 = st.getUInt8();
        this.prefixBits = uInt8;
        if (uInt8 <= 128) {
            if (uInt8 < 128) {
                String s = st.getString();
                try {
                    this.suffix = Address.getByAddress(s, 2);
                } catch (UnknownHostException e) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("invalid IPv6 address: ");
                    stringBuffer.append(s);
                    throw st.exception(stringBuffer.toString());
                }
            }
            if (this.prefixBits > 0) {
                this.prefix = st.getName(origin);
                return;
            }
            return;
        }
        throw st.exception("prefix bits must be [0..128]");
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.prefixBits);
        if (this.suffix != null) {
            sb.append(" ");
            sb.append(this.suffix.getHostAddress());
        }
        if (this.prefix != null) {
            sb.append(" ");
            sb.append(this.prefix);
        }
        return sb.toString();
    }

    public int getPrefixBits() {
        return this.prefixBits;
    }

    public InetAddress getSuffix() {
        return this.suffix;
    }

    public Name getPrefix() {
        return this.prefix;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(this.prefixBits);
        InetAddress inetAddress = this.suffix;
        if (inetAddress != null) {
            int suffixbytes = ((128 - this.prefixBits) + 7) / 8;
            out.writeByteArray(inetAddress.getAddress(), 16 - suffixbytes, suffixbytes);
        }
        Name name = this.prefix;
        if (name != null) {
            name.toWire(out, (Compression) null, canonical);
        }
    }
}
