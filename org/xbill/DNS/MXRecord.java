package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;

public class MXRecord extends U16NameBase {
    private static final long serialVersionUID = 2914841027584208546L;

    MXRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new MXRecord();
    }

    public MXRecord(Name name, int dclass, long ttl, int priority, Name target) {
        super(name, 15, dclass, ttl, priority, "priority", target, SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
    }

    public Name getTarget() {
        return getNameField();
    }

    public int getPriority() {
        return getU16Field();
    }

    /* access modifiers changed from: package-private */
    public void rrToWire(DNSOutput out, Compression c, boolean canonical) {
        out.writeU16(this.u16Field);
        this.nameField.toWire(out, c, canonical);
    }

    public Name getAdditionalName() {
        return getNameField();
    }
}
