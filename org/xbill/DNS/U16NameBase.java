package org.xbill.DNS;

import java.io.IOException;

abstract class U16NameBase extends Record {
    private static final long serialVersionUID = -8315884183112502995L;
    protected Name nameField;
    protected int u16Field;

    protected U16NameBase() {
    }

    protected U16NameBase(Name name, int type, int dclass, long ttl) {
        super(name, type, dclass, ttl);
    }

    protected U16NameBase(Name name, int type, int dclass, long ttl, int u16Field2, String u16Description, Name nameField2, String nameDescription) {
        super(name, type, dclass, ttl);
        this.u16Field = checkU16(u16Description, u16Field2);
        this.nameField = checkName(nameDescription, nameField2);
    }

    /* access modifiers changed from: package-private */
    public void rrFromWire(DNSInput in) throws IOException {
        this.u16Field = in.readU16();
        this.nameField = new Name(in);
    }

    /* access modifiers changed from: package-private */
    public void rdataFromString(Tokenizer st, Name origin) throws IOException {
        this.u16Field = st.getUInt16();
        this.nameField = st.getName(origin);
    }

    /* access modifiers changed from: package-private */
    public String rrToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.u16Field);
        sb.append(" ");
        sb.append(this.nameField);
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public int getU16Field() {
        return this.u16Field;
    }

    /* access modifiers changed from: protected */
    public Name getNameField() {
        return this.nameField;
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.u16Field);
        this.nameField.toWire(out, (Compression) null, canonical);
    }
}
