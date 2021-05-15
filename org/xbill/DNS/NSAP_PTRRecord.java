package org.xbill.DNS;

import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;

public class NSAP_PTRRecord extends SingleNameBase {
    private static final long serialVersionUID = 2386284746382064904L;

    NSAP_PTRRecord() {
    }

    /* access modifiers changed from: package-private */
    public Record getObject() {
        return new NSAP_PTRRecord();
    }

    public NSAP_PTRRecord(Name name, int dclass, long ttl, Name target) {
        super(name, 23, dclass, ttl, target, SoftphoneNamespaces.SoftphoneCallHandling.TARGET);
    }

    public Name getTarget() {
        return getSingleName();
    }
}
