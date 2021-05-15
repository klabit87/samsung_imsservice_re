package com.sec.internal.constants.ims.servicemodules.im;

public enum SlmMode implements IEnumerationWithId<SlmMode> {
    UNKOWN(0),
    PAGER(1),
    LARGE_MESSAGE(2);
    
    private static final ReverseEnumMap<SlmMode> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(SlmMode.class);
    }

    private SlmMode(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public SlmMode getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }

    public static SlmMode fromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
