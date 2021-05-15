package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;

public class PTRRecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = -8321636610425434192L;

    PTRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new PTRRecord();
    }

    public PTRRecord(Name name, int dclass, long ttl, Name target) {
        super(name, 12, dclass, ttl, target, SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
    }

    public Name getTarget() {
        return getSingleName();
    }
}
