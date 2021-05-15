package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ConfCallChanged_;

public final class ConfEvent {
    public static final int CONF_PARTICIPANT_ADDED = 0;
    public static final int CONF_PARTICIPANT_REMOVED = 1;
    public static final int CONF_PARTICIPANT_UPDATED = 2;
    public static final String[] names = {"CONF_PARTICIPANT_ADDED", "CONF_PARTICIPANT_REMOVED", "CONF_PARTICIPANT_UPDATED"};

    private ConfEvent() {
    }

    public static String name(int e) {
        return names[e];
    }
}
