package org.xbill.DNS;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientSubnetOption extends EDNSOption {
    private static final long serialVersionUID = -3868158449890266347L;
    private InetAddress address;
    private int family;
    private int scopeNetmask;
    private int sourceNetmask;

    ClientSubnetOption() {
        super(8);
    }

    private static int checkMaskLength(String field, int family2, int val) {
        int max = Address.addressLength(family2) * 8;
        if (val >= 0 && val <= max) {
            return val;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\"");
        stringBuffer.append(field);
        stringBuffer.append("\" ");
        stringBuffer.append(val);
        stringBuffer.append(" must be in the range [0..");
        stringBuffer.append(max);
        stringBuffer.append("]");
        throw new IllegalArgumentException(stringBuffer.toString());
    }

    public ClientSubnetOption(int sourceNetmask2, int scopeNetmask2, InetAddress address2) {
        super(8);
        int familyOf = Address.familyOf(address2);
        this.family = familyOf;
        this.sourceNetmask = checkMaskLength("source netmask", familyOf, sourceNetmask2);
        this.scopeNetmask = checkMaskLength("scope netmask", this.family, scopeNetmask2);
        InetAddress truncate = Address.truncate(address2, sourceNetmask2);
        this.address = truncate;
        if (!address2.equals(truncate)) {
            throw new IllegalArgumentException("source netmask is not valid for address");
        }
    }

    public ClientSubnetOption(int sourceNetmask2, InetAddress address2) {
        this(sourceNetmask2, 0, address2);
    }

    public int getFamily() {
        return this.family;
    }

    public int getSourceNetmask() {
        return this.sourceNetmask;
    }

    public int getScopeNetmask() {
        return this.scopeNetmask;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    /* access modifiers changed from: package-private */
    public void optionFromWire(DNSInput in) throws WireParseException {
        int readU16 = in.readU16();
        this.family = readU16;
        if (readU16 == 1 || readU16 == 2) {
            int readU8 = in.readU8();
            this.sourceNetmask = readU8;
            if (readU8 <= Address.addressLength(this.family) * 8) {
                int readU82 = in.readU8();
                this.scopeNetmask = readU82;
                if (readU82 <= Address.addressLength(this.family) * 8) {
                    byte[] addr = in.readByteArray();
                    if (addr.length == (this.sourceNetmask + 7) / 8) {
                        byte[] fulladdr = new byte[Address.addressLength(this.family)];
                        System.arraycopy(addr, 0, fulladdr, 0, addr.length);
                        try {
                            InetAddress byAddress = InetAddress.getByAddress(fulladdr);
                            this.address = byAddress;
                            if (!Address.truncate(byAddress, this.sourceNetmask).equals(this.address)) {
                                throw new WireParseException("invalid padding");
                            }
                        } catch (UnknownHostException e) {
                            throw new WireParseException("invalid address", e);
                        }
                    } else {
                        throw new WireParseException("invalid address");
                    }
                } else {
                    throw new WireParseException("invalid scope netmask");
                }
            } else {
                throw new WireParseException("invalid source netmask");
            }
        } else {
            throw new WireParseException("unknown address family");
        }
    }

    /* access modifiers changed from: package-private */
    public void optionToWire(DNSOutput out) {
        out.writeU16(this.family);
        out.writeU8(this.sourceNetmask);
        out.writeU8(this.scopeNetmask);
        out.writeByteArray(this.address.getAddress(), 0, (this.sourceNetmask + 7) / 8);
    }

    /* access modifiers changed from: package-private */
    public String optionToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.address.getHostAddress());
        sb.append("/");
        sb.append(this.sourceNetmask);
        sb.append(", scope netmask ");
        sb.append(this.scopeNetmask);
        return sb.toString();
    }
}
