package com.sec.internal.ims.servicemodules.im.data;

public enum FtResumableOption {
    NOTRESUMABLE(0),
    MANUALLY_RESUMABLE_ONLY(1),
    MANUALLY_AUTOMATICALLY_RESUMABLE(2);
    
    private final int mId;

    private FtResumableOption(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }
}
