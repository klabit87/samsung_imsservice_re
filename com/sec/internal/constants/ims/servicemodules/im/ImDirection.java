package com.sec.internal.constants.ims.servicemodules.im;

public enum ImDirection implements IEnumerationWithId<ImDirection> {
    INCOMING(0),
    OUTGOING(1),
    IRRELEVANT(2);
    
    private static final ReverseEnumMap<ImDirection> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(ImDirection.class);
    }

    private ImDirection(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public ImDirection getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }

    public static ImDirection fromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
