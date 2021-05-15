package com.sec.internal.constants.ims.servicemodules.im;

public enum RoutingType implements IEnumerationWithId<RoutingType> {
    NONE(0),
    SENT(1),
    RECEIVED(2);
    
    private static final ReverseEnumMap<RoutingType> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(RoutingType.class);
    }

    private RoutingType(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public RoutingType getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
