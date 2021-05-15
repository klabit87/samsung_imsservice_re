package com.sec.internal.constants.ims.servicemodules.im.params;

public class GroupChatListParams {
    private final boolean increaseMode;
    private final String mOwnImsi;
    private final int version;

    public GroupChatListParams(int version2, boolean increaseMode2, String ownImsi) {
        this.version = version2;
        this.increaseMode = increaseMode2;
        this.mOwnImsi = ownImsi;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean getIncreaseMode() {
        return this.increaseMode;
    }

    public String getOwnImsi() {
        return this.mOwnImsi;
    }
}
