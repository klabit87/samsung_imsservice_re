package org.xbill.DNS;

import java.io.IOException;
import java.util.BitSet;
import org.xbill.DNS.Tokenizer;

public class NXTRecord extends Record {
    private static final long serialVersionUID = -8851454400765507520L;
    private BitSet bitmap;
    private Name next;

    NXTRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NXTRecord();
    }

    public NXTRecord(Name name, int dclass, long ttl, Name next2, BitSet bitmap2) {
        super(name, 30, dclass, ttl);
        this.next = checkName("next", next2);
        this.bitmap = bitmap2;
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.next = new Name(in);
        this.bitmap = new BitSet();
        int bitmapLength = in.remaining();
        for (int i = 0; i < bitmapLength; i++) {
            int t = in.readU8();
            for (int j = 0; j < 8; j++) {
                if (((1 << (7 - j)) & t) != 0) {
                    this.bitmap.set((i * 8) + j);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        Tokenizer.Token t;
        this.next = st.getName(origin);
        this.bitmap = new BitSet();
        while (true) {
            t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            int typecode = Type.value(t.value, true);
            if (typecode <= 0 || typecode > 128) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("Invalid type: ");
                stringBuffer.append(t.value);
            } else {
                this.bitmap.set(typecode);
            }
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append("Invalid type: ");
        stringBuffer2.append(t.value);
        throw st.exception(stringBuffer2.toString());
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.next);
        int length = this.bitmap.length();
        for (short i = 0; i < length; i = (short) (i + 1)) {
            if (this.bitmap.get(i)) {
                sb.append(" ");
                sb.append(Type.string(i));
            }
        }
        return sb.toString();
    }

    public Name getNext() {
        return this.next;
    }

    public BitSet getBitmap() {
        return this.bitmap;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        this.next.toWire(out, (Compression) null, canonical);
        int length = this.bitmap.length();
        int t = 0;
        for (int i = 0; i < length; i++) {
            t |= this.bitmap.get(i) ? 1 << (7 - (i % 8)) : 0;
            if (i % 8 == 7 || i == length - 1) {
                out.writeU8(t);
                t = 0;
            }
        }
    }
}
