package org.xbill.DNS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xbill.DNS.Tokenizer;

abstract class TXTBase extends Record {
    private static final long serialVersionUID = -4319510507246305931L;
    protected List strings;

    protected TXTBase() {
    }

    protected TXTBase(Name name, int type, int dclass, long ttl) {
        super(name, type, dclass, ttl);
    }

    protected TXTBase(Name name, int type, int dclass, long ttl, List strings2) {
        super(name, type, dclass, ttl);
        if (strings2 != null) {
            this.strings = new ArrayList(strings2.size());
            Iterator it = strings2.iterator();
            while (it.hasNext()) {
                try {
                    this.strings.add(byteArrayFromString((String) it.next()));
                } catch (TextParseException e) {
                    throw new IllegalArgumentException(e.getMessage());
                }
            }
            return;
        }
        throw new IllegalArgumentException("strings must not be null");
    }

    protected TXTBase(Name name, int type, int dclass, long ttl, String string) {
        this(name, type, dclass, ttl, Collections.singletonList(string));
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.strings = new ArrayList(2);
        while (in.remaining() > 0) {
            this.strings.add(in.readCountedString());
        }
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.strings = new ArrayList(2);
        while (true) {
            Tokenizer.Token t = st.get();
            if (!t.isString()) {
                st.unget();
                return;
            }
            try {
                this.strings.add(byteArrayFromString(t.value));
            } catch (TextParseException e) {
                throw st.exception(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        Iterator it = this.strings.iterator();
        while (it.hasNext()) {
            sb.append(byteArrayToString((byte[]) it.next(), true));
            if (it.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public List getStrings() {
        List list = new ArrayList(this.strings.size());
        for (int i = 0; i < this.strings.size(); i++) {
            list.add(byteArrayToString((byte[]) this.strings.get(i), false));
        }
        return list;
    }

    public List getStringsAsByteArrays() {
        return this.strings;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        for (byte[] b : this.strings) {
            out.writeCountedString(b);
        }
    }
}
