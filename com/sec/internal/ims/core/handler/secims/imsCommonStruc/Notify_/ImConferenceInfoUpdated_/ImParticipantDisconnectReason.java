package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConferenceInfoUpdated_;

public final class ImParticipantDisconnectReason {
    public static final int PARTICIPANT_DISCONNECT_BOOTED = 1;
    public static final int PARTICIPANT_DISCONNECT_BUSY = 3;
    public static final int PARTICIPANT_DISCONNECT_FAILED = 2;
    public static final int PARTICIPANT_DISCONNECT_REASON = 0;
    public static final String[] names = {"PARTICIPANT_DISCONNECT_REASON", "PARTICIPANT_DISCONNECT_BOOTED", "PARTICIPANT_DISCONNECT_FAILED", "PARTICIPANT_DISCONNECT_BUSY"};

    private ImParticipantDisconnectReason() {
    }

    public static String name(int e) {
        return names[e];
    }
}
