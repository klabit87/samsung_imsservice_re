package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;

public class NSRecord extends SingleCompressedNameBase {
    private static final long serialVersionUID = 487170758138268838L;

    NSRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSRecord();
    }

    public NSRecord(Name name, int dclass, long ttl, Name target) {
        super(name, 2, dclass, ttl, target, SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
    }

    public Name getTarget() {
        return getSingleName();
    }

    public Name getAdditionalName() {
        return getSingleName();
    }
}
