package com.sec.internal.ims.servicemodules.csh.event;

public enum VshViewType {
    LOCAL(1),
    REMOTE(2);
    
    private final int mType;

    private VshViewType(int i) {
        this.mType = i;
    }
}
