package org.xbill.DNS;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.xbill.DNS.utils.base64;

public class IPSECKEYRecord extends Record {
    private static final long serialVersionUID = 3050449702765909687L;
    private int algorithmType;
    private Object gateway;
    private int gatewayType;
    private byte[] key;
    private int precedence;

    public static class Algorithm {
        public static final int DSA = 1;
        public static final int RSA = 2;

        private Algorithm() {
        }
    }

    public static class Gateway {
        public static final int IPv4 = 1;
        public static final int IPv6 = 2;
        public static final int Name = 3;
        public static final int None = 0;

        private Gateway() {
        }
    }

    IPSECKEYRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new IPSECKEYRecord();
    }

    public IPSECKEYRecord(Name name, int dclass, long ttl, int precedence2, int gatewayType2, int algorithmType2, Object gateway2, byte[] key2) {
        super(name, 45, dclass, ttl);
        this.precedence = checkU8("precedence", precedence2);
        this.gatewayType = checkU8("gatewayType", gatewayType2);
        this.algorithmType = checkU8("algorithmType", algorithmType2);
        if (gatewayType2 == 0) {
            this.gateway = null;
        } else if (gatewayType2 != 1) {
            if (gatewayType2 != 2) {
                if (gatewayType2 != 3) {
                    throw new IllegalArgumentException("\"gatewayType\" must be between 0 and 3");
                } else if (gateway2 instanceof Name) {
                    this.gateway = checkName("gateway", (Name) gateway2);
                } else {
                    throw new IllegalArgumentException("\"gateway\" must be a DNS name");
                }
            } else if (gateway2 instanceof Inet6Address) {
                this.gateway = gateway2;
            } else {
                throw new IllegalArgumentException("\"gateway\" must be an IPv6 address");
            }
        } else if (gateway2 instanceof InetAddress) {
            this.gateway = gateway2;
        } else {
            throw new IllegalArgumentException("\"gateway\" must be an IPv4 address");
        }
        this.key = key2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.precedence = in.readU8();
        this.gatewayType = in.readU8();
        this.algorithmType = in.readU8();
        int i = this.gatewayType;
        if (i == 0) {
            this.gateway = null;
        } else if (i == 1) {
            this.gateway = InetAddress.getByAddress(in.readByteArray(4));
        } else if (i == 2) {
            this.gateway = InetAddress.getByAddress(in.readByteArray(16));
        } else if (i == 3) {
            this.gateway = new Name(in);
        } else {
            throw new WireParseException("invalid gateway type");
        }
        if (in.remaining() > 0) {
            this.key = in.readByteArray();
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.precedence = st.getUInt8();
        this.gatewayType = st.getUInt8();
        this.algorithmType = st.getUInt8();
        int i = this.gatewayType;
        if (i != 0) {
            if (i == 1) {
                this.gateway = st.getAddress(1);
            } else if (i == 2) {
                this.gateway = st.getAddress(2);
            } else if (i == 3) {
                this.gateway = st.getName(origin);
            } else {
                throw new WireParseException("invalid gateway type");
            }
        } else if (st.getString().equals(".")) {
            this.gateway = null;
        } else {
            throw new TextParseException("invalid gateway format");
        }
        this.key = st.getBase64(false);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.precedence);
        sb.append(" ");
        sb.append(this.gatewayType);
        sb.append(" ");
        sb.append(this.algorithmType);
        sb.append(" ");
        int i = this.gatewayType;
        if (i == 0) {
            sb.append(".");
        } else if (i == 1 || i == 2) {
            sb.append(((InetAddress) this.gateway).getHostAddress());
        } else if (i == 3) {
            sb.append(this.gateway);
        }
        if (this.key != null) {
            sb.append(" ");
            sb.append(base64.toString(this.key));
        }
        return sb.toString();
    }

    public int getPrecedence() {
        return this.precedence;
    }

    public int getGatewayType() {
        return this.gatewayType;
    }

    public int getAlgorithmType() {
        return this.algorithmType;
    }

    public Object getGateway() {
        return this.gateway;
    }

    public byte[] getKey() {
        return this.key;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU8(this.precedence);
        out.writeU8(this.gatewayType);
        out.writeU8(this.algorithmType);
        int i = this.gatewayType;
        if (i == 1 || i == 2) {
            out.writeByteArray(((InetAddress) this.gateway).getAddress());
        } else if (i == 3) {
            ((Name) this.gateway).toWire(out, (Compression) null, canonical);
        }
        byte[] bArr = this.key;
        if (bArr != null) {
            out.writeByteArray(bArr);
        }
    }
}
