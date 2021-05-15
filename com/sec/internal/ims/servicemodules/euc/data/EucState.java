package com.sec.internal.ims.servicemodules.euc.data;

import com.sec.internal.constants.ims.servicemodules.im.IEnumerationWithId;
import com.sec.internal.constants.ims.servicemodules.im.ReverseEnumMap;

public enum EucState implements IEnumerationWithId<EucState> {
    ACCEPTED(0),
    REJECTED(1),
    ACCEPTED_NOT_SENT(2),
    REJECTED_NOT_SENT(3),
    DISMISSED(4),
    TIMED_OUT(5),
    FAILED(6),
    NONE(7);
    
    private static final ReverseEnumMap<EucState> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(EucState.class);
    }

    private EucState(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public EucState getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
