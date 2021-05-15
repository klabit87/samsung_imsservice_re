package com.sec.internal.constants.ims.servicemodules.im;

public enum ChatMode implements IEnumerationWithId<ChatMode> {
    OFF(0),
    ON(1),
    LINK(2);
    
    private static final ReverseEnumMap<ChatMode> map = null;
    private final int id;

    static {
        map = new ReverseEnumMap<>(ChatMode.class);
    }

    private ChatMode(int id2) {
        this.id = id2;
    }

    public int getId() {
        return this.id;
    }

    public ChatMode getFromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }

    public static ChatMode fromId(int id2) {
        return map.get(Integer.valueOf(id2));
    }
}
