package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_;

public final class ImParticipantStatus {
    public static final int PARTICIPANT_STATUS_CONNECTED = 0;
    public static final int PARTICIPANT_STATUS_DISCONNECTED = 1;
    public static final int PARTICIPANT_STATUS_PENDING = 2;
    public static final String[] names = {"PARTICIPANT_STATUS_CONNECTED", "PARTICIPANT_STATUS_DISCONNECTED", "PARTICIPANT_STATUS_PENDING"};

    private ImParticipantStatus() {
    }

    public static String name(int e) {
        return names[e];
    }
}
