package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_;

public final class participantstatus {
    public static final int PARTICIPANT_ACTIVE = 2;
    public static final int PARTICIPANT_ALERTING = 5;
    public static final int PARTICIPANT_INVITING = 1;
    public static final int PARTICIPANT_NON_ACTIVE = 4;
    public static final int PARTICIPANT_ONHOLD = 6;
    public static final int PARTICIPANT_REMOVING = 3;
    public static final int PARTICIPANT_STATUS_NONE = 0;
    public static final String[] names = {"PARTICIPANT_STATUS_NONE", "PARTICIPANT_INVITING", "PARTICIPANT_ACTIVE", "PARTICIPANT_REMOVING", "PARTICIPANT_NON_ACTIVE", "PARTICIPANT_ALERTING", "PARTICIPANT_ONHOLD"};

    private participantstatus() {
    }

    public static String name(int e) {
        return names[e];
    }
}
