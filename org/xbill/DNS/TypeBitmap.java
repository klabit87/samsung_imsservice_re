package org.xbill.DNS;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;
import org.xbill.DNS.Tokenizer;

final class TypeBitmap implements Serializable {
    private static final long serialVersionUID = -125354057735389003L;
    private TreeSet types;

    private TypeBitmap() {
        this.types = new TreeSet();
    }

    public TypeBitmap(int[] array) {
        this();
        for (int i = 0; i < array.length; i++) {
            Type.check(array[i]);
            this.types.add(new Integer(array[i]));
        }
    }

    public TypeBitmap(DNSInput in) throws WireParseException {
        this();
        while (in.remaining() > 0) {
            if (in.remaining() >= 2) {
                int mapbase = in.readU8();
                if (mapbase >= -1) {
                    int maplength = in.readU8();
                    if (maplength <= in.remaining()) {
                        for (int i = 0; i < maplength; i++) {
                            int current = in.readU8();
                            if (current != 0) {
                                for (int j = 0; j < 8; j++) {
                                    if (((1 << (7 - j)) & current) != 0) {
                                        this.types.add(Mnemonic.toInteger((mapbase * 256) + (i * 8) + j));
                                    }
                                }
                            }
                        }
                    } else {
                        throw new WireParseException("invalid bitmap");
                    }
                } else {
                    throw new WireParseException("invalid ordering");
                }
            } else {
                throw new WireParseException("invalid bitmap descriptor");
            }
        }
    }

    public TypeBitmap(Tokenizer st) throws IOException {
        this();
        while (true) {
            Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            int typecode = Type.value(t.value);
            if (typecode >= 0) {
                this.types.add(Mnemonic.toInteger(typecode));
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid type: ");
                stringBuffer.append(t.value);
                throw st.exception(stringBuffer.toString());
            }
        }
    }

    public int[] toArray() {
        int[] array = new int[this.types.size()];
        int n = 0;
        Iterator it = this.types.iterator();
        while (it.hasNext()) {
            array[n] = ((Integer) it.next()).intValue();
            n++;
        }
        return array;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = this.types.iterator();
        while (it.hasNext()) {
            sb.append(Type.string(((Integer) it.next()).intValue()));
            if (it.hasNext()) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private static void mapToWire(DNSOutput out, TreeSet map, int mapbase) {
        int arraylength = ((((Integer) map.last()).intValue() & 255) / 8) + 1;
        int[] array = new int[arraylength];
        out.writeU8(mapbase);
        out.writeU8(arraylength);
        Iterator it = map.iterator();
        while (it.hasNext()) {
            int typecode = ((Integer) it.next()).intValue();
            int i = (typecode & 255) / 8;
            array[i] = array[i] | (1 << (7 - (typecode % 8)));
        }
        for (int j = 0; j < arraylength; j++) {
            out.writeU8(array[j]);
        }
    }

    public void toWire(DNSOutput out) {
        if (this.types.size() != 0) {
            int mapbase = -1;
            TreeSet map = new TreeSet();
            Iterator it = this.types.iterator();
            while (it.hasNext()) {
                int t = ((Integer) it.next()).intValue();
                int base = t >> 8;
                if (base != mapbase) {
                    if (map.size() > 0) {
                        mapToWire(out, map, mapbase);
                        map.clear();
                    }
                    mapbase = base;
                }
                map.add(new Integer(t));
            }
            mapToWire(out, map, mapbase);
        }
    }

    public boolean empty() {
        return this.types.isEmpty();
    }

    public boolean contains(int typecode) {
        return this.types.contains(Mnemonic.toInteger(typecode));
    }
}
