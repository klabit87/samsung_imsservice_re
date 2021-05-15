package com.sec.internal.ims.servicemodules.euc.data;

import com.sec.internal.constants.ims.servicemodules.im.IEnumerationWithId;
import com.sec.internal.constants.ims.servicemodules.im.ReverseEnumMap;

public enum EucType implements IEnumerationWithId<EucType> {
    PERSISTENT(0),
    VOLATILE(1),
    NOTIFICATION(2),
    ACKNOWLEDGEMENT(3),
    EULA(4);
    
    private static final ReverseEnumMap<EucType> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(EucType.class);
    }

    private EucType(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public EucType getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
