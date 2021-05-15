package com.sec.internal.constants.ims.cmstore.omanetapi;

public enum SyncMsgType {
    DEFAULT(0),
    MESSAGE(1),
    FAX(2),
    VM(3),
    CALLLOG(4),
    VM_GREETINGS(5);
    
    private final int mId;

    private SyncMsgType(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public static SyncMsgType valueOf(int id) {
        for (SyncMsgType r : values()) {
            if (r.mId == id) {
                return r;
            }
        }
        return null;
    }
}
