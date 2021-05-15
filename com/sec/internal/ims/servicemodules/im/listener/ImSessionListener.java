package com.sec.internal.ims.servicemodules.im.listener;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ImSessionListener {
    void addToRevokingMessages(String str, String str2);

    void onAddParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason);

    void onAddParticipantsSucceeded(String str, List<ImsUri> list);

    void onBlockedMessageReceived(ImIncomingMessageEvent imIncomingMessageEvent);

    void onChangeGroupAliasFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupAliasSucceeded(String str, String str2);

    void onChangeGroupChatIconFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupChatIconSuccess(String str, String str2);

    void onChangeGroupChatLeaderFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason);

    void onChangeGroupChatLeaderSucceeded(String str, List<ImsUri> list);

    void onChangeGroupChatSubjectFailed(String str, String str2, ImErrorReason imErrorReason);

    void onChangeGroupChatSubjectSucceeded(String str, String str2);

    void onChatClosed(ImSession imSession, ImSessionClosedReason imSessionClosedReason);

    void onChatDeparted(ImSession imSession);

    void onChatEstablished(ImSession imSession);

    void onChatStatusUpdate(ImSession imSession, ImSession.SessionState sessionState);

    void onChatSubjectUpdated(String str, ImSubjectData imSubjectData);

    void onComposingReceived(ImSession imSession, ImsUri imsUri, String str, boolean z, int i);

    void onGroupChatIconDeleted(String str);

    void onGroupChatIconUpdated(String str, ImIconData imIconData);

    void onGroupChatLeaderChanged(ImSession imSession, String str);

    void onGroupChatLeaderInformed(ImSession imSession, String str);

    void onImErrorReport(ImError imError, int i);

    void onIncomingMessageProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession);

    void onIncomingSessionProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession, boolean z);

    void onMessageRevocationDone(ImConstants.RevocationStatus revocationStatus, Collection<MessageBase> collection, ImSession imSession);

    void onMessageRevokeTimerExpired(String str, Collection<String> collection, String str2);

    void onNotifyParticipantsAdded(ImSession imSession, Map<ImParticipant, Date> map);

    void onNotifyParticipantsJoined(ImSession imSession, Map<ImParticipant, Date> map);

    void onNotifyParticipantsKickedOut(ImSession imSession, Map<ImParticipant, Date> map);

    void onNotifyParticipantsLeft(ImSession imSession, Map<ImParticipant, Date> map);

    void onParticipantAliasUpdated(String str, ImParticipant imParticipant);

    void onParticipantsDeleted(ImSession imSession, Collection<ImParticipant> collection);

    void onParticipantsInserted(ImSession imSession, Collection<ImParticipant> collection);

    void onParticipantsUpdated(ImSession imSession, Collection<ImParticipant> collection);

    void onProcessingFileTransferChanged(ImSession imSession);

    void onRemoveParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason);

    void onRemoveParticipantsSucceeded(String str, List<ImsUri> list);

    void onRequestSendMessage(ImSession imSession, MessageBase messageBase);

    void removeFromRevokingMessages(Collection<String> collection);

    void setLegacyLatching(ImsUri imsUri, boolean z, String str);
}
