package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.ims.servicemodules.im.ImSession;
import java.util.Collection;
import java.util.List;

public interface IChatEventListener {
    void onAddParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason);

    void onAddParticipantsSucceeded(String str, Collection<ImsUri> collection);

    void onChangeGroupAliasFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupAliasSucceeded(String str, String str2);

    void onChangeGroupChatIconFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupChatIconSuccess(String str, String str2);

    void onChangeGroupChatLeaderFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason);

    void onChangeGroupChatLeaderSucceeded(String str, List<ImsUri> list);

    void onChangeGroupChatSubjectFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupChatSubjectSucceeded(String str, String str2);

    void onChatClosed(String str, ImDirection imDirection, ImSessionClosedReason imSessionClosedReason);

    void onChatEstablished(String str, ImDirection imDirection, ImsUri imsUri, List<String> list, List<String> list2);

    void onChatInvitationReceived(ImSession imSession);

    void onChatSubjectUpdated(String str, ImSubjectData imSubjectData);

    void onChatUpdateState(String str, ImDirection imDirection, ImSession.SessionState sessionState);

    void onComposingNotificationReceived(String str, boolean z, ImsUri imsUri, String str2, boolean z2, int i);

    void onCreateChatFailed(int i, int i2, ImErrorReason imErrorReason, String str);

    void onCreateChatSucceeded(ImSession imSession);

    void onGroupChatIconDeleted(String str);

    void onGroupChatIconUpdated(String str, ImIconData imIconData);

    void onGroupChatLeaderUpdated(String str, String str2);

    void onMessageRevokeTimerExpired(String str, Collection<String> collection);

    void onParticipantAliasUpdated(String str, ImParticipant imParticipant);

    void onParticipantsAdded(ImSession imSession, Collection<ImParticipant> collection);

    void onParticipantsJoined(ImSession imSession, Collection<ImParticipant> collection);

    void onParticipantsLeft(ImSession imSession, Collection<ImParticipant> collection);

    void onRemoveParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason);

    void onRemoveParticipantsSucceeded(String str, Collection<ImsUri> collection);
}
