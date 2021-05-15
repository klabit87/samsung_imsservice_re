package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class GcReqType {
    public static final int ADD_PARTICIPANTS = 0;
    public static final int CHANGE_CHAT_ICON = 5;
    public static final int CHANGE_CHAT_SUBJECT = 4;
    public static final int CHANGE_LEADER = 2;
    public static final int CHANGE_USER_ALIAS = 3;
    public static final int REMOVE_PARTICIPANTS = 1;
    public static final String[] names = {"ADD_PARTICIPANTS", "REMOVE_PARTICIPANTS", "CHANGE_LEADER", "CHANGE_USER_ALIAS", "CHANGE_CHAT_SUBJECT", "CHANGE_CHAT_ICON"};

    private GcReqType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
