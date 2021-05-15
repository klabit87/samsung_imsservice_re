package org.xbill.DNS;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.Tokenizer;
import org.xbill.DNS.utils.base16;

public class APLRecord extends Record {
    private static final long serialVersionUID = -1348173791712935864L;
    private List elements;

    public static class Element {
        public final Object address;
        public final int family;
        public final boolean negative;
        public final int prefixLength;

        private Element(int family2, boolean negative2, Object address2, int prefixLength2) {
            this.family = family2;
            this.negative = negative2;
            this.address = address2;
            this.prefixLength = prefixLength2;
            if (!APLRecord.validatePrefixLength(family2, prefixLength2)) {
                throw new IllegalArgumentException("invalid prefix length");
            }
        }

        public Element(boolean negative2, InetAddress address2, int prefixLength2) {
            this(Address.familyOf(address2), negative2, address2, prefixLength2);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            if (this.negative) {
                sb.append("!");
            }
            sb.append(this.family);
            sb.append(":");
            int i = this.family;
            if (i == 1 || i == 2) {
                sb.append(((InetAddress) this.address).getHostAddress());
            } else {
                sb.append(base16.toString((byte[]) this.address));
            }
            sb.append("/");
            sb.append(this.prefixLength);
            return sb.toString();
        }

        public boolean equals(Object arg) {
            if (arg == null || !(arg instanceof Element)) {
                return false;
            }
            Element elt = (Element) arg;
            if (this.family == elt.family && this.negative == elt.negative && this.prefixLength == elt.prefixLength && this.address.equals(elt.address)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.address.hashCode() + this.prefixLength + (this.negative ? 1 : 0);
        }
    }

    APLRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new APLRecord();
    }

    /* access modifiers changed from: private */
    public static boolean validatePrefixLength(int family, int prefixLength) {
        if (prefixLength < 0 || prefixLength >= 256) {
            return false;
        }
        if ((family != 1 || prefixLength <= 32) && (family != 2 || prefixLength <= 128)) {
            return true;
        }
        return false;
    }

    public APLRecord(Name name, int dclass, long ttl, List elements2) {
        super(name, 42, dclass, ttl);
        this.elements = new ArrayList(elements2.size());
        for (Object o : elements2) {
            if (o instanceof Element) {
                Element element = (Element) o;
                if (element.family == 1 || element.family == 2) {
                    this.elements.add(element);
                } else {
                    throw new IllegalArgumentException("unknown family");
                }
            } else {
                throw new IllegalArgumentException("illegal element");
            }
        }
    }

    private static byte[] parseAddress(byte[] in, int length) throws WireParseException {
        if (in.length > length) {
            throw new WireParseException("invalid address length");
        } else if (in.length == length) {
            return in;
        } else {
            byte[] out = new byte[length];
            System.arraycopy(in, 0, out, 0, in.length);
            return out;
        }
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        Element element;
        this.elements = new ArrayList(1);
        while (in.remaining() != 0) {
            int family = in.readU16();
            int prefix = in.readU8();
            int length = in.readU8();
            boolean negative = (length & 128) != 0;
            byte[] data = in.readByteArray(length & -129);
            if (validatePrefixLength(family, prefix)) {
                if (family == 1 || family == 2) {
                    element = new Element(negative, InetAddress.getByAddress(parseAddress(data, Address.addressLength(family))), prefix);
                } else {
                    element = new Element(family, negative, data, prefix);
                }
                this.elements.add(element);
            } else {
                throw new WireParseException("invalid prefix length");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        int start;
        boolean negative;
        APLRecord aPLRecord = this;
        Tokenizer tokenizer = st;
        int i = 1;
        aPLRecord.elements = new ArrayList(1);
        while (true) {
            Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            String s = t.value;
            if (s.startsWith("!")) {
                start = 1;
                negative = true;
            } else {
                start = 0;
                negative = false;
            }
            int colon = s.indexOf(58, start);
            if (colon >= 0) {
                int slash = s.indexOf(47, colon);
                if (slash >= 0) {
                    String familyString = s.substring(start, colon);
                    String addressString = s.substring(colon + 1, slash);
                    String prefixString = s.substring(slash + 1);
                    try {
                        int family = Integer.parseInt(familyString);
                        if (family == i || family == 2) {
                            try {
                                int prefix = Integer.parseInt(prefixString);
                                if (validatePrefixLength(family, prefix)) {
                                    byte[] bytes = Address.toByteArray(addressString, family);
                                    if (bytes != null) {
                                        Tokenizer.Token token = t;
                                        aPLRecord.elements.add(new Element(negative, InetAddress.getByAddress(bytes), prefix));
                                        i = 1;
                                        aPLRecord = this;
                                    } else {
                                        StringBuffer stringBuffer = new StringBuffer();
                                        stringBuffer.append("invalid IP address ");
                                        stringBuffer.append(addressString);
                                        throw tokenizer.exception(stringBuffer.toString());
                                    }
                                } else {
                                    throw tokenizer.exception("invalid prefix length");
                                }
                            } catch (NumberFormatException e) {
                                Tokenizer.Token token2 = t;
                                NumberFormatException numberFormatException = e;
                                throw tokenizer.exception("invalid prefix length");
                            }
                        } else {
                            throw tokenizer.exception("unknown family");
                        }
                    } catch (NumberFormatException e2) {
                        Tokenizer.Token token3 = t;
                        NumberFormatException numberFormatException2 = e2;
                        throw tokenizer.exception("invalid family");
                    }
                } else {
                    throw tokenizer.exception("invalid address prefix element");
                }
            } else {
                throw tokenizer.exception("invalid address prefix element");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = this.elements.iterator();
        while (it.hasNext()) {
            sb.append((Element) it.next());
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public List getElements() {
        return this.elements;
    }

    private static int addressLength(byte[] addr) {
        for (int i = addr.length - 1; i >= 0; i--) {
            if (addr[i] != 0) {
                return i + 1;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        byte[] data;
        int length;
        for (Element element : this.elements) {
            if (element.family == 1 || element.family == 2) {
                byte[] data2 = ((InetAddress) element.address).getAddress();
                length = addressLength(data2);
                data = data2;
            } else {
                data = (byte[]) element.address;
                length = data.length;
            }
            int wlength = length;
            if (element.negative) {
                wlength |= 128;
            }
            out.writeU16(element.family);
            out.writeU8(element.prefixLength);
            out.writeU8(wlength);
            out.writeByteArray(data, 0, length);
        }
    }
}
