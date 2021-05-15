package com.sec.internal.ims.servicemodules.im.translator;

import android.util.Log;
import com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;

public class TranslatorCollection {
    private static final String LOG_TAG = "TranslatorCollection";

    private TranslatorCollection() {
    }

    public static ImParticipant.Status translateEngineParticipantInfo(ImConferenceParticipantInfo info, ImParticipant participant) {
        if (info == null || info.mParticipantStatus == null) {
            return null;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus[info.mParticipantStatus.ordinal()];
        if (i == 1) {
            return ImParticipant.Status.ACCEPTED;
        }
        if (i != 2) {
            if (i != 3) {
                Log.i(LOG_TAG, "No translation for the following Engine's participant Status: " + info.mParticipantStatus);
                return null;
            }
            return (participant == null || participant.getStatus() != ImParticipant.Status.ACCEPTED) ? ImParticipant.Status.TO_INVITE : ImParticipant.Status.PENDING;
        } else if (info.mDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.DEPARTED || info.mUserElemState == ImConferenceParticipantInfo.ImConferenceUserElemState.DELETED) {
            return ImParticipant.Status.DECLINED;
        } else {
            if (info.mDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.FAILED) {
                if (info.mDisconnectionCause == ImError.REMOTE_PARTY_DECLINED) {
                    return ImParticipant.Status.DECLINED;
                }
                return ImParticipant.Status.FAILED;
            } else if (info.mDisconnectionReason == ImConferenceParticipantInfo.ImConferenceDisconnectionReason.BOOTED) {
                return ImParticipant.Status.GONE;
            } else {
                return ImParticipant.Status.FAILED;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.translator.TranslatorCollection$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus;

        static {
            int[] iArr = new int[ImConferenceParticipantInfo.ImConferenceParticipantStatus.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus = iArr;
            try {
                iArr[ImConferenceParticipantInfo.ImConferenceParticipantStatus.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus[ImConferenceParticipantInfo.ImConferenceParticipantStatus.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConferenceParticipantInfo$ImConferenceParticipantStatus[ImConferenceParticipantInfo.ImConferenceParticipantStatus.PENDING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }
}
