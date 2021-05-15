package org.xbill.DNS;

public class AFSDBRecord extends U16NameBase {
    private static final long serialVersionUID = 3034379930729102437L;

    AFSDBRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new AFSDBRecord();
    }

    public AFSDBRecord(Name name, int dclass, long ttl, int subtype, Name host) {
        super(name, 18, dclass, ttl, subtype, "subtype", host, "host");
    }

    public int getSubtype() {
        return getU16Field();
    }

    public Name getHost() {
        return getNameField();
    }
}
