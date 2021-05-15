package com.sec.internal.interfaces.ims.servicemodules.im;

import android.net.Uri;
import android.os.Handler;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AddParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupAliasParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatLeaderParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatSubjectParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChatbotAnonymizeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ImSendComposingParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RemoveParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ReportChatbotAsSpamParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageRevokeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;

public interface IImServiceInterface {
    void acceptFtSession(AcceptFtSessionParams acceptFtSessionParams);

    void acceptImSession(AcceptImSessionParams acceptImSessionParams);

    void addImParticipants(AddParticipantsParams addParticipantsParams);

    void cancelFtSession(RejectFtSessionParams rejectFtSessionParams);

    void changeGroupAlias(ChangeGroupAliasParams changeGroupAliasParams);

    void changeGroupChatIcon(ChangeGroupChatIconParams changeGroupChatIconParams);

    void changeGroupChatLeader(ChangeGroupChatLeaderParams changeGroupChatLeaderParams);

    void changeGroupChatSubject(ChangeGroupChatSubjectParams changeGroupChatSubjectParams);

    void extendToGroupChat(StartImSessionParams startImSessionParams);

    void registerForChatbotAnonymizeNotify(Handler handler, int i, Object obj);

    void registerForChatbotAnonymizeResp(Handler handler, int i, Object obj);

    void registerForChatbotAsSpamNotify(Handler handler, int i, Object obj);

    void registerForComposingNotification(Handler handler, int i, Object obj);

    void registerForConferenceInfoUpdate(Handler handler, int i, Object obj);

    void registerForGroupChatInfoUpdate(Handler handler, int i, Object obj);

    void registerForGroupChatListUpdate(Handler handler, int i, Object obj);

    void registerForImIncomingFileTransfer(Handler handler, int i, Object obj);

    void registerForImIncomingMessage(Handler handler, int i, Object obj);

    void registerForImIncomingSession(Handler handler, int i, Object obj);

    void registerForImSessionClosed(Handler handler, int i, Object obj);

    void registerForImSessionEstablished(Handler handler, int i, Object obj);

    void registerForImdnFailed(Handler handler, int i, Object obj);

    void registerForImdnNotification(Handler handler, int i, Object obj);

    void registerForImdnResponse(Handler handler, int i, Object obj);

    void registerForMessageFailed(Handler handler, int i, Object obj);

    void registerForMessageRevokeResponse(Handler handler, int i, Object obj);

    void registerForSendMessageRevokeDone(Handler handler, int i, Object obj);

    void registerForTransferProgress(Handler handler, int i, Object obj);

    void rejectFtSession(RejectFtSessionParams rejectFtSessionParams);

    void rejectImSession(RejectImSessionParams rejectImSessionParams);

    void removeImParticipants(RemoveParticipantsParams removeParticipantsParams);

    void reportChatbotAsSpam(ReportChatbotAsSpamParams reportChatbotAsSpamParams);

    void requestChatbotAnonymize(ChatbotAnonymizeParams chatbotAnonymizeParams);

    void sendComposingNotification(ImSendComposingParams imSendComposingParams);

    void sendDeliveredNotification(SendImdnParams sendImdnParams);

    void sendDisplayedNotification(SendImdnParams sendImdnParams);

    void sendFtDeliveredNotification(SendImdnParams sendImdnParams);

    void sendFtDisplayedNotification(SendImdnParams sendImdnParams);

    void sendFtSession(SendFtSessionParams sendFtSessionParams);

    void sendImMessage(SendMessageParams sendMessageParams);

    void sendMessageRevokeRequest(SendMessageRevokeParams sendMessageRevokeParams);

    void setFtMessageId(Object obj, int i);

    void setMoreInfoToSipUserAgent(String str, int i);

    void startImSession(StartImSessionParams startImSessionParams);

    void stopImSession(StopImSessionParams stopImSessionParams);

    void subscribeGroupChatInfo(Uri uri, String str);

    void subscribeGroupChatList(int i, boolean z, String str);

    void unRegisterForGroupChatInfoUpdate(Handler handler);

    void unRegisterForGroupChatListUpdate(Handler handler);

    void unregisterAllFileTransferProgress();

    void unregisterForChatbotAnonymizeNotify(Handler handler);

    void unregisterForChatbotAnonymizeResp(Handler handler);

    void unregisterForChatbotAsSpamNotify(Handler handler);

    void unregisterForComposingNotification(Handler handler);

    void unregisterForConferenceInfoUpdate(Handler handler);

    void unregisterForImIncomingFileTransfer(Handler handler);

    void unregisterForImIncomingMessage(Handler handler);

    void unregisterForImIncomingSession(Handler handler);

    void unregisterForImSessionClosed(Handler handler);

    void unregisterForImSessionEstablished(Handler handler);

    void unregisterForImdnFailed(Handler handler);

    void unregisterForImdnNotification(Handler handler);

    void unregisterForImdnResponse(Handler handler);

    void unregisterForMessageFailed(Handler handler);

    void unregisterForMessageRevokeResponse(Handler handler);

    void unregisterForSendMessageRevokeDone(Handler handler);

    void unregisterForTransferProgress(Handler handler);
}
