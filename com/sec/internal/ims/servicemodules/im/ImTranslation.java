package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.im.ImParticipantData;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IImCacheActionListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImTranslation extends TranslationBase implements IChatEventListener, IMessageEventListener, IImCacheActionListener {
    private static final String INNER_RELIABLE_URI = "content://com.samsung.rcs.im/getreliableimage/";
    private static final String LOG_TAG = ImTranslation.class.getSimpleName();
    private final Context mContext;
    private final ExecutorService mFileExecutor = Executors.newSingleThreadExecutor();
    private final ImModule mImModule;
    private final ImProcessor mImProcessor;
    private final ImSessionProcessor mImSessionProcessor;

    public ImTranslation(Context context, ImModule imModule, ImSessionProcessor imSessionProcessor, ImProcessor imProcessor) {
        Log.i(LOG_TAG, "Create ImTranslation.");
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerChatEventListener(this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.TEXT_PUBLICACCOUNT, this);
        this.mImSessionProcessor = imSessionProcessor;
        this.mImProcessor = imProcessor;
    }

    private static ArrayList<ImsUri> convertToUriList(Collection<String> list) {
        ArrayList<ImsUri> uriList = new ArrayList<>();
        for (String uriString : list) {
            if (uriString != null) {
                uriList.add(ImsUri.parse(uriString));
            }
        }
        return uriList;
    }

    private static ArrayList<String> convertToStringList(Collection<ImsUri> list) {
        ArrayList<String> strList = new ArrayList<>();
        for (ImsUri uri : list) {
            if (uri != null) {
                strList.add(uri.toString());
            }
        }
        return strList;
    }

    private void requestCreateChat(Intent intent) {
        Bundle extras = intent.getExtras();
        List<String> stringArrayList = extras.getStringArrayList(ImIntent.Extras.PARTICIPANTS_LIST);
        String string = extras.getString("subject");
        String string2 = extras.getString("content_type");
        int i = extras.getInt(ImIntent.Extras.REQUEST_THREAD_ID, -1);
        String valueOf = String.valueOf(extras.getLong("request_message_id"));
        boolean z = extras.getBoolean("is_broadcast_msg", false);
        boolean z2 = extras.getBoolean(ImIntent.Extras.IS_CLOSED_GROUP_CHAT, false);
        String string3 = extras.getString(ImIntent.Extras.GROUPCHAT_ICON_PATH);
        String string4 = extras.getString("sim_slot_id");
        Bundle bundle = extras;
        $$Lambda$ImTranslation$PtxO7kT2hhRhNK_IiEiSE_KZzGQ r35 = r0;
        ExecutorService executorService = this.mFileExecutor;
        $$Lambda$ImTranslation$PtxO7kT2hhRhNK_IiEiSE_KZzGQ r0 = new Runnable(this, string4, stringArrayList, i, valueOf, string, string2, z, z2, string3, extras, extras.getBoolean(ImIntent.Extras.IS_TOKEN_USED, false), extras.getBoolean(ImIntent.Extras.IS_TOKEN_LINK, false), extras.getString("conversation_id"), extras.getString("contribution_id"), extras.getString("session_uri")) {
            public final /* synthetic */ ImTranslation f$0;
            public final /* synthetic */ String f$1;
            public final /* synthetic */ Bundle f$10;
            public final /* synthetic */ boolean f$11;
            public final /* synthetic */ boolean f$12;
            public final /* synthetic */ String f$13;
            public final /* synthetic */ String f$14;
            public final /* synthetic */ String f$15;
            public final /* synthetic */ List f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ String f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$0 = r3;
                this.f$1 = r4;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
                this.f$5 = r8;
                this.f$6 = r9;
                this.f$7 = r10;
                this.f$8 = r11;
                this.f$9 = r12;
                this.f$10 = r13;
                this.f$11 = r14;
                this.f$12 = r15;
                this.f$13 = r16;
                this.f$14 = r17;
                this.f$15 = r18;
            }

            public final void run() {
                this.f$0.lambda$requestCreateChat$0$ImTranslation(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15);
            }
        };
        executorService.execute(r35);
    }

    public /* synthetic */ void lambda$requestCreateChat$0$ImTranslation(String slotId, List list, int threadId, String requestMessageId, String subject, String sdpContentType, boolean isBroadcastMsg, boolean isClosedGroupChat, String iconPath, Bundle extras, boolean isTokenUsed, boolean isTokenLink, String conversationId, String contributionId, String sessionUri) {
        int phoneId = 0;
        if (!TextUtils.isEmpty(slotId)) {
            try {
                phoneId = Integer.valueOf(slotId).intValue();
                String str = slotId;
            } catch (NumberFormatException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Invalid slot id : " + slotId);
            }
        } else {
            String str3 = slotId;
        }
        String str4 = LOG_TAG;
        Log.i(str4, "requestCreateChat() phoneId = " + phoneId);
        if (list == null) {
            onCreateChatFailed(phoneId, threadId, ImErrorReason.INVALID, requestMessageId);
            return;
        }
        int i = threadId;
        String str5 = requestMessageId;
        this.mImSessionProcessor.createChat(phoneId, convertToUriList(list), subject, sdpContentType, threadId, requestMessageId, isBroadcastMsg, isClosedGroupChat, FileUtils.copyFileFromUri(this.mContext, iconPath, extras.getString(ImIntent.Extras.GROUPCHAT_ICON_NAME)), isTokenUsed, isTokenLink, conversationId, contributionId, TextUtils.isEmpty(sessionUri) ? null : ImsUri.parse(sessionUri));
    }

    private void requestAddParticipantsToChat(Intent intent) {
        Log.i(LOG_TAG, "requestAddParticipantsToChat()");
        Bundle extras = intent.getExtras();
        String chatId = extras.getString("chat_id");
        List<String> list = getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST);
        if (list == null || list.isEmpty()) {
            onAddParticipantsFailed(chatId, (Collection<ImsUri>) null, ImErrorReason.INVALID);
        } else {
            this.mImModule.addParticipants(chatId, convertToUriList(list));
        }
    }

    private void requestRemoveGroupChatParticipants(Intent intent) {
        Log.i(LOG_TAG, "requestRemoveGroupChatParticipants()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.removeParticipants(extras.getString("chat_id"), convertToUriList(getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST)));
    }

    private void requestChangeGroupChatLeader(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatLeader()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupChatLeader(extras.getString("chat_id"), convertToUriList(getArrayList(extras, ImIntent.Extras.PARTICIPANTS_LIST)));
    }

    private void requestChangeGroupChatSubject(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatSubject()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupChatSubject(extras.getString("chat_id"), extras.getString("subject"));
    }

    private void requestChangeGroupChatIcon(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupChatIcon()");
        Bundle extras = intent.getExtras();
        this.mFileExecutor.execute(new Runnable(extras.getString("chat_id"), extras.getString(ImIntent.Extras.GROUPCHAT_ICON_PATH), extras) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ Bundle f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                ImTranslation.this.lambda$requestChangeGroupChatIcon$1$ImTranslation(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$requestChangeGroupChatIcon$1$ImTranslation(String chatId, String iconPath, Bundle extras) {
        this.mImSessionProcessor.changeGroupChatIcon(chatId, FileUtils.copyFileFromUri(this.mContext, iconPath, extras.getString(ImIntent.Extras.GROUPCHAT_ICON_NAME)));
    }

    private void requestDeleteGroupChatIcon(Intent intent) {
        Log.i(LOG_TAG, "requestDeleteGroupChatIcon()");
        this.mImSessionProcessor.changeGroupChatIcon(intent.getExtras().getString("chat_id"), (String) null);
    }

    private void requestChangeGroupAlias(Intent intent) {
        Log.i(LOG_TAG, "requestChangeGroupAlias()");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.changeGroupAlias(extras.getString("chat_id"), extras.getString(ImIntent.Extras.USER_ALIAS));
    }

    private void requestReportMessage(Intent intent) {
        Log.i(LOG_TAG, "requestReportMessage");
        this.mImProcessor.reportMessages(new ArrayList(getArrayList(intent.getExtras(), ImIntent.Extras.MESSAGES_LIST)));
    }

    private void requestDeliveryTimeout(Intent intent) {
        String chatId = intent.getExtras().getString("chat_id");
        String str = LOG_TAG;
        Log.i(str, "requestDeliveryTimeout() chatId:" + chatId);
        this.mImSessionProcessor.receiveDeliveryTimeout(chatId);
    }

    public void onMessageReportResponse(Long messageId, boolean isSuccess) {
        String str = LOG_TAG;
        Log.i(str, "onMessageReportResponse, messageId=" + messageId + ", res=" + messageId);
        Intent intent = new Intent(ImIntent.Action.REPORT_MESSAGES_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_id", messageId);
        intent.putExtra("response_status", isSuccess);
        broadcastIntent(intent);
    }

    private void requestIgnoreIncomingMsgSet(Intent intent) {
        Log.i(LOG_TAG, "requestIgnoreIncomingMsgSet");
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.ignoreIncomingMsgSet(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.IS_IGNORE_INCOMING_MSG, false));
    }

    public void onIgnoreIncomingMsgSetResponse(String chatId, boolean isSuccess) {
        Log.i(LOG_TAG, "onIgnoreIncomingMsgSetResponse");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.IGNORE_INCOMING_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("response_status", isSuccess);
        broadcastIntent(intent);
    }

    private void requestSendMessage(Intent intent) {
        Log.i(LOG_TAG, "requestSendMessage()");
        Bundle extras = intent.getExtras();
        String cid = extras.getString("chat_id");
        String body = extras.getString(ImIntent.Extras.MESSAGE_BODY);
        String disposition = extras.getString("disposition_notification");
        String contentType = extras.getString("content_type", "text/plain");
        String requestMessageId = String.valueOf(extras.getLong("request_message_id"));
        int messageNumber = extras.getInt(ImIntent.Extras.MESSAGE_NUMBER);
        boolean isBroadcastMsg = extras.getBoolean("is_broadcast_msg", false);
        boolean isPublicAccountMsg = extras.getBoolean("is_publicAccountMsg", false);
        if (isPublicAccountMsg) {
            PublicAccountUri.setPublicAccountDomain(extras.getString("publicAccount_Send_Domain"));
        }
        String deviceName = extras.getString("device_name");
        String reliableMessage = extras.getString("reliable_message");
        String xmsMessage = extras.getString(ImIntent.Extras.LENGTH_TYPE);
        List<String> ccList = getArrayList(extras, ImIntent.Extras.GROUP_CCUSER_LIST);
        boolean isTemporary = extras.getBoolean("is_temporary", false);
        String maapTrafficType = extras.getString("maap_traffic_type");
        String referenceMessageId = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_ID);
        String referenceMessageType = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_TYPE);
        String referenceMessageValue = extras.getString(ImIntent.Extras.REFERENCE_MESSAGE_VALUE);
        if (maapTrafficType != null) {
            String str = LOG_TAG;
            Log.i(str, "requestSendMessage, maapTrafficType = [" + maapTrafficType + "]");
        }
        String maapTrafficType2 = maapTrafficType;
        this.mImModule.sendMessage(cid, body, NotificationStatus.toSet(disposition), contentType, requestMessageId, messageNumber, isBroadcastMsg, isPublicAccountMsg, false, deviceName, reliableMessage, xmsMessage, convertToUriList(ccList), isTemporary, maapTrafficType2, referenceMessageId, referenceMessageType, referenceMessageValue);
    }

    private void requestReadMessage(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.readMessages(extras.getString("chat_id"), getArrayList(extras, ImIntent.Extras.MESSAGES_LIST), extras.getBoolean(ImIntent.Extras.UPDATE_ONLY_MSTORE, false));
    }

    private void requestSendComposingNotification(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.sendComposingNotification(extras.getString("chat_id"), extras.getInt(ImIntent.Extras.INTERVAL), extras.getBoolean(ImIntent.Extras.IS_TYPING));
    }

    public void requestComposingActiveUris(Intent intent) {
        Log.i(LOG_TAG, "requestComposingActiveUris()");
        this.mImSessionProcessor.getComposingActiveUris(intent.getExtras().getString("chat_id"));
    }

    public void requestGetLastSentMessages(Intent intent) {
        List<String> list = getArrayList(intent.getExtras(), ImIntent.Extras.REQUEST_MESSAGES_LIST);
        String str = LOG_TAG;
        Log.i(str, "requestGetLastSentMessages(): REQUEST_MESSAGES_LIST size:" + list.size() + ", " + IMSLog.checker(intent.toString()));
        this.mImProcessor.getLastSentMessagesStatus(list);
    }

    public void notifyComposingActiveUris(String chatId, Set<ImsUri> composingActiveUris) {
        Log.i(LOG_TAG, "notifyComposingActiveUris()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.GET_IS_COMPOSING_ACTIVE_URIS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", chatId);
        List<ImsUri> listComposingUri = new ArrayList<>();
        if (composingActiveUris != null) {
            listComposingUri = new ArrayList<>(composingActiveUris);
        }
        intent.putStringArrayListExtra(ImIntent.Extras.COMPOSING_URI_LIST, new ArrayList(convertToStringList(listComposingUri)));
        broadcastIntent(intent, true);
    }

    public void notifyLastSentMessagesStatus(List<Bundle> messages) {
        Log.i(LOG_TAG, "notifyLastSentMessagesStatus()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.GET_LAST_MESSAGES_SENT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle extras = new Bundle();
        extras.putParcelableArrayList(ImIntent.Extras.LAST_SENT_MESSAGES_STATUS, messages != null ? new ArrayList(messages) : null);
        intent.putExtras(extras);
        broadcastIntent(intent, true);
    }

    public void onRequestChatbotAnonymizeResponse(String uri, boolean isSuccess, String commandId, int retryAfter) {
        Log.i(LOG_TAG, "onChatbotAnonymizeNotificationReceived()");
        Intent intent = new Intent(ImIntent.Action.CHATBOT_ANONYMIZE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle extras = new Bundle();
        extras.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI, uri);
        extras.putBoolean("response_status", isSuccess);
        extras.putString(ImIntent.Extras.CHATBOT_COMMAND_ID, commandId);
        if (!isSuccess && retryAfter != -1) {
            extras.putInt(ImIntent.Extras.RETRY_AFTER, retryAfter);
        }
        intent.putExtras(extras);
        broadcastIntent(intent);
    }

    public void onRequestChatbotAnonymizeNotiReceived(String uri, String result, String commandId) {
        Log.i(LOG_TAG, "onChatbotAnonymizeNotificationReceived()");
        Intent intent = new Intent(ImIntent.Action.CHATBOT_ANONYMIZE_NOTIFICATION);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle extras = new Bundle();
        extras.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI, uri);
        extras.putString(ImIntent.Extras.CHATBOT_ANONYMIZE_RESULT, result);
        extras.putString(ImIntent.Extras.CHATBOT_COMMAND_ID, commandId);
        intent.putExtras(extras);
        broadcastIntent(intent);
    }

    public void onReportChatbotAsSpamRespReceived(String uri, boolean isSuccess, String requestId) {
        Log.i(LOG_TAG, "onReportChatbotAsSpamRespReceived()");
        Intent intent = new Intent(ImIntent.Action.REPORT_CHATBOT_AS_SPAM_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        Bundle extras = new Bundle();
        extras.putString(ImIntent.Extras.CHATBOT_URI, uri);
        extras.putBoolean("response_status", isSuccess);
        extras.putString(ImIntent.Extras.CHATBOT_REQUEST_ID, requestId);
        intent.putExtras(extras);
        broadcastIntent(intent);
    }

    private void requestDeleteAllChats() {
        this.mImSessionProcessor.deleteAllChats();
    }

    private void requestAcceptChat(Intent intent) {
        Bundle extras = intent.getExtras();
        String cid = extras.getString("chat_id");
        Boolean isAccept = Boolean.valueOf(extras.getBoolean(ImIntent.Extras.IS_ACCEPT));
        this.mImSessionProcessor.acceptChat(cid, isAccept.booleanValue(), extras.getInt("reason", 0));
    }

    private void requestOpenChat(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.openChat(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.INVITATION_UI, false));
    }

    private void requestCloseChat(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.closeChat(getArrayList(extras, ImIntent.Extras.CHATS_LIST), true, extras.getBoolean(ImIntent.Extras.IS_DISMISS_GROUPCHAT, false));
    }

    private void requestAnswerGcChats(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.answerGcSession(extras.getString("chat_id"), extras.getBoolean(ImIntent.Extras.BOOLEAN_ANSWER));
    }

    private void requestDeleteChats(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImSessionProcessor.deleteChats(getArrayList(extras, ImIntent.Extras.CHATS_LIST), extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false));
    }

    private void requestDeleteAllMessages(Intent intent) {
        Bundle extras = intent.getExtras();
        this.mImProcessor.deleteAllMessages(getArrayList(extras, ImIntent.Extras.CHATS_LIST), extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false));
    }

    private void requestDeleteMessages(Intent intent) {
        Bundle extras = intent.getExtras();
        boolean isLocalWipe = extras.getBoolean(ImIntent.Extras.IS_LOCAL_WIPEOUT, false);
        this.mImModule.deleteMessagesByImdnId((Map) extras.getSerializable(ImIntent.Extras.MESSAGES_IMDN_DIR_MAP), isLocalWipe);
    }

    private void requestMessageRevocation(Intent intent) {
        Bundle extras = intent.getExtras();
        String cid = extras.getString("chat_id");
        String str = LOG_TAG;
        Log.i(str, "requestMessageRevocation(): chatId = " + cid);
        boolean userSelectResult = extras.getBoolean(ImIntent.Extras.USER_SELECT_RESULT);
        int userSelectType = extras.getInt(ImIntent.Extras.USER_SELECT_MESSAGE_TYPE, 3);
        String imdnId = extras.getString(ImIntent.Extras.MESSAGE_IMDN_ID);
        this.mImSessionProcessor.getImRevocationHandler().requestMessageRevocation(cid, imdnId != null ? new ArrayList(Collections.singletonList(imdnId)) : null, userSelectResult, userSelectType);
    }

    private void requestChatbotAnonymize(Intent intent) {
        Bundle extras = intent.getExtras();
        String chatbotUri = extras.getString(ImIntent.Extras.CHATBOT_ANONYMIZE_URI);
        String action = extras.getString(ImIntent.Extras.CHATBOT_ANONYMIZE_ACTION);
        String commandId = extras.getString(ImIntent.Extras.CHATBOT_COMMAND_ID);
        String slotId = extras.getString("sim_slot_id");
        int phoneId = 0;
        if (!TextUtils.isEmpty(slotId)) {
            try {
                phoneId = Integer.valueOf(slotId).intValue();
            } catch (NumberFormatException e) {
                String str = LOG_TAG;
                Log.e(str, "Invalid slot id : " + slotId);
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "requestChatbotAnonymize() phoneId = " + phoneId + ", uri = " + IMSLog.checker(chatbotUri) + ", action = " + action);
        this.mImModule.requestChatbotAnonymize(phoneId, ImsUri.parse(chatbotUri), action, commandId);
    }

    private void reportChatbotAsSpam(Intent intent) {
        int phoneId;
        Bundle extras = intent.getExtras();
        String chatbotUri = extras.getString(ImIntent.Extras.CHATBOT_URI);
        String request_id = extras.getString(ImIntent.Extras.CHATBOT_REQUEST_ID);
        String slotId = extras.getString("sim_slot_id");
        String spamType = extras.getString(ImIntent.Extras.CHATBOT_SPAM_TYPE);
        String freeText = extras.getString(ImIntent.Extras.CHATBOT_FREE_TEXT);
        List<String> msgIds = getArrayList(extras, ImIntent.Extras.MESSAGES_ID_LIST);
        if (!TextUtils.isEmpty(slotId)) {
            try {
                phoneId = Integer.valueOf(slotId).intValue();
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Invalid slot id : " + slotId);
            }
            Log.i(LOG_TAG, "reportChatbotAsSpam() phoneId = " + phoneId + ", uri = " + IMSLog.checker(chatbotUri));
            this.mImModule.reportChatbotAsSpam(phoneId, request_id, ImsUri.parse(chatbotUri), msgIds, spamType, freeText);
        }
        phoneId = 0;
        Log.i(LOG_TAG, "reportChatbotAsSpam() phoneId = " + phoneId + ", uri = " + IMSLog.checker(chatbotUri));
        this.mImModule.reportChatbotAsSpam(phoneId, request_id, ImsUri.parse(chatbotUri), msgIds, spamType, freeText);
    }

    private List<String> getArrayList(Bundle extras, String key) {
        String[] array;
        Preconditions.checkNotNull(extras, "extras is null");
        Preconditions.checkNotNull(key, "key is null");
        List<String> list = extras.getStringArrayList(key);
        if (list == null && (array = extras.getStringArray(key)) != null) {
            list = Arrays.asList(array);
        }
        return list != null ? list : Collections.emptyList();
    }

    public void onCreateChatSucceeded(ImSession chat) {
        String str = LOG_TAG;
        Log.i(str, "onCreateChatSucceeded(), notify, chat : " + chat.getChatId());
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CREATE_CHAT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chat.getChatId());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, new ArrayList(chat.getParticipantsString()));
        intent.putExtra(ImIntent.Extras.REQUEST_THREAD_ID, chat.getThreadId());
        intent.putExtra("subject", chat.getSubject());
        intent.putExtra("request_message_id", chat.getRequestMessageId() == null ? -1 : Long.valueOf(chat.getRequestMessageId()).longValue());
        intent.putExtra("sim_slot_id", this.mImModule.getPhoneIdByIMSI(chat.getOwnImsi()));
        String str2 = null;
        intent.putExtra("conversation_id", chat.isGroupChat() ? chat.getChatData().getConversationId() : null);
        if (chat.isGroupChat()) {
            str2 = chat.getChatData().getContributionId();
        }
        intent.putExtra("contribution_id", str2);
        broadcastIntent(intent, true);
    }

    public void onCreateChatFailed(int phoneId, int threadId, ImErrorReason reason, String requestMessageId) {
        String str = LOG_TAG;
        Log.i(str, "onCreateChatFailed(), notifyError : " + reason);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CREATE_CHAT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putExtra(ImIntent.Extras.REQUEST_THREAD_ID, threadId);
        intent.putExtra("request_message_id", requestMessageId == null ? -1 : Long.valueOf(requestMessageId).longValue());
        intent.putExtra("sim_slot_id", String.valueOf(phoneId));
        broadcastIntent(intent, true);
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        Log.i(LOG_TAG, "onMessageSendingSucceeded");
        Preconditions.checkNotNull(msg, "message is null");
        Long messageId = Long.valueOf((long) msg.getId());
        Intent intent = new Intent(ImIntent.Action.RECEIVE_SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_id", messageId);
        intent.putExtra("response_status", true);
        intent.putExtra("request_message_id", msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra("is_broadcast_msg", msg.isBroadcastMsg());
        if (msg.getReferenceId() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_ID, msg.getReferenceId());
        }
        if (msg.getReferenceType() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_TYPE, msg.getReferenceType());
        }
        if (msg.getReferenceValue() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_VALUE, msg.getReferenceValue());
        }
        broadcastIntent(intent, true);
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Log.i(LOG_TAG, "onMessageSendingFailed()");
        Preconditions.checkNotNull(msg, "message is null");
        broadcastIntent(createMessageSendingFailedIntent(msg, strategyResponse, result), true);
    }

    public void onComposingNotificationReceived(String chatId, boolean isGroupChat, ImsUri uri, String userAlias, boolean isTyping, int interval) {
        Log.i(LOG_TAG, "onComposingNotificationReceived");
        Intent intent = new Intent(ImIntent.Action.RECEIVE_TYPING_NOTIFICATION);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("participant", uri == null ? "" : uri.toString());
        intent.putExtra(ImIntent.Extras.INTERVAL, interval);
        intent.putExtra(ImIntent.Extras.IS_TYPING, isTyping);
        if (!TextUtils.isEmpty(userAlias)) {
            intent.putExtra(ImIntent.Extras.USER_ALIAS, userAlias);
        }
        broadcastIntent(intent, true);
    }

    public void onDeviceOutOfMemory() {
        Log.i(LOG_TAG, "onDeviceOutOfMemory()");
        Intent intent = new Intent(ImIntent.Action.OUT_OF_MEMORY_ERROR);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        broadcastIntent(intent);
    }

    public void onChatUpdateState(String chatId, ImDirection direction, ImSession.SessionState state) {
    }

    public void onChatEstablished(String chatId, ImDirection direction, ImsUri sessionUri, List<String> acceptTypes, List<String> acceptWrappedTypes) {
        Log.i(LOG_TAG, "onChatEstablished()");
        Preconditions.checkNotNull(chatId);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_ESTABLISHED);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("session_uri", sessionUri == null ? null : sessionUri.toString());
        ArrayList<String> supportedContentList = new ArrayList<>();
        if (acceptTypes != null && !acceptTypes.isEmpty()) {
            supportedContentList.addAll(acceptTypes);
        }
        if (acceptWrappedTypes != null && !acceptWrappedTypes.isEmpty()) {
            supportedContentList.addAll(acceptWrappedTypes);
        }
        if (!supportedContentList.isEmpty()) {
            intent.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, supportedContentList);
        }
        broadcastIntent(intent, true);
    }

    public void onChatClosed(String chatId, ImDirection direction, ImSessionClosedReason reason) {
        Log.i(LOG_TAG, "onChatClosed()");
        Preconditions.checkNotNull(chatId);
        Preconditions.checkNotNull(reason);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_CLOSED);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.CHAT_STATUS, reason.name());
        broadcastIntent(intent, true);
    }

    public void onChatSubjectUpdated(String chatId, ImSubjectData subjectData) {
        Log.i(LOG_TAG, "onChatSubjectUpdated()");
        Preconditions.checkNotNull(chatId);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_CHAT_SUBJECT_UPDATED);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("subject", subjectData.getSubject());
        String str = null;
        intent.putExtra("subject_participant", subjectData.getParticipant() != null ? subjectData.getParticipant().toString() : null);
        if (subjectData.getTimestamp() != null) {
            str = subjectData.getTimestamp().toString();
        }
        intent.putExtra("subject_timestamp", str);
        broadcastIntent(intent, true);
    }

    public void onGroupChatIconUpdated(String chatId, ImIconData iconData) {
        Log.i(LOG_TAG, "onGroupChatIconUpdated()");
        Preconditions.checkNotNull(chatId);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_ICON_UPDATED);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_PATH, FileUtils.getUriForFileAsString(this.mContext, iconData.getIconLocation()));
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_PARTICIPANT, iconData.getParticipant());
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_TIMESTAMP, iconData.getTimestamp());
        broadcastIntent(intent, true);
    }

    public void onGroupChatIconDeleted(String chatId) {
        Log.i(LOG_TAG, "onGroupChatIconDeleted()");
        Preconditions.checkNotNull(chatId);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_ICON_DELETED);
        intent.putExtra("chat_id", chatId);
        broadcastIntent(intent, true);
    }

    public void onParticipantAliasUpdated(String chatId, ImParticipant participant) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantAliasUpdated: " + IMSLog.numberChecker(participant.getUri()));
        Preconditions.checkNotNull(chatId);
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_ALIAS_UPDATED);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("participant", participant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) participant.getId()));
        intent.putExtra(ImIntent.Extras.USER_ALIAS, participant.getUserAlias());
        broadcastIntent(intent, true);
    }

    public void onChatInvitationReceived(ImSession session) {
        Log.i(LOG_TAG, "onChatInvitationReceived()");
        Preconditions.checkNotNull(session);
        ArrayList<String> supportedContentList = new ArrayList<>();
        if (session.mRemoteAcceptTypes != null && !session.mRemoteAcceptTypes.isEmpty()) {
            supportedContentList.addAll(session.mRemoteAcceptTypes);
        }
        if (session.mRemoteAcceptWrappedTypes != null && !session.mRemoteAcceptWrappedTypes.isEmpty()) {
            supportedContentList.addAll(session.mRemoteAcceptWrappedTypes);
        }
        String str = null;
        if (session.needToUseGroupChatInvitationUI()) {
            Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_SESSION);
            intent.putExtra("chat_id", session.getChatId());
            intent.putExtra("subject", session.getSubject());
            intent.putExtra("content_type", session.getSdpContentType());
            intent.putExtra("conversation_id", session.isGroupChat() ? session.getChatData().getConversationId() : null);
            intent.putExtra("contribution_id", session.isGroupChat() ? session.getChatData().getContributionId() : null);
            if (session.getChatData().getSessionUri() != null) {
                str = session.getChatData().getSessionUri().toString();
            }
            intent.putExtra("session_uri", str);
            if (session.getInitiator() != null) {
                intent.putExtra("remote_uri", session.getInitiator().toString());
            }
            if (!supportedContentList.isEmpty()) {
                intent.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, supportedContentList);
            }
            broadcastIntent(intent, true);
            return;
        }
        Intent intent2 = new Intent(ImIntent.Action.RECEIVE_CHAT_INVITATION);
        intent2.addCategory(ImIntent.CATEGORY_ACTION);
        intent2.putExtra("chat_id", session.getChatId());
        intent2.putExtra("subject", session.getSubject());
        intent2.putExtra("content_type", session.getSdpContentType());
        intent2.putExtra(ImIntent.Extras.USER_ALIAS, session.getInitiatorAlias());
        intent2.putExtra(ImIntent.Extras.IS_TOKEN_USED, session.getIsTokenUsed());
        intent2.putExtra("conversation_id", session.isGroupChat() ? session.getChatData().getConversationId() : null);
        intent2.putExtra("contribution_id", session.isGroupChat() ? session.getChatData().getContributionId() : null);
        if (session.getChatData().getSessionUri() != null) {
            str = session.getChatData().getSessionUri().toString();
        }
        intent2.putExtra("session_uri", str);
        intent2.putExtra(ImIntent.Extras.IS_CLOSED_GROUP_CHAT, ChatData.ChatType.isClosedGroupChat(session.getChatType()));
        if (session.isChatbotManualAcceptUsed()) {
            intent2.putExtra(ImIntent.Extras.IS_BOT, true);
            if (session.getInitiator() != null) {
                String str2 = LOG_TAG;
                Log.i(str2, "session.getInitiator=" + IMSLog.numberChecker(session.getInitiator()));
                intent2.putExtra(ImIntent.Extras.SERVICE_ID, session.getInitiator().toString());
            }
        } else {
            intent2.putExtra(ImIntent.Extras.IS_BOT, false);
        }
        if (session.isGroupChat() && session.getInitiator() != null) {
            intent2.putExtra("remote_uri", session.getInitiator().toString());
        }
        if (!supportedContentList.isEmpty()) {
            intent2.putStringArrayListExtra(ImIntent.Extras.SUPPORTED_CONTENT_LIST, supportedContentList);
        }
        broadcastIntent(intent2, true);
    }

    public void onAddParticipantsSucceeded(String chatId, Collection<ImsUri> participants) {
        Log.i(LOG_TAG, "onAddParticipantsSucceeded()");
        Preconditions.checkNotNull(participants);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.ADD_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        broadcastIntent(intent, true);
    }

    public void onAddParticipantsFailed(String chatId, Collection<ImsUri> participants, ImErrorReason reason) {
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.ADD_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        if (participants != null) {
            intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        }
        broadcastIntent(intent, true);
    }

    public void onRemoveParticipantsSucceeded(String chatId, Collection<ImsUri> participants) {
        Log.i(LOG_TAG, "onRemoveParticipantsSucceeded()");
        Preconditions.checkNotNull(participants);
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.REMOVE_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        broadcastIntent(intent, true);
    }

    public void onRemoveParticipantsFailed(String chatId, Collection<ImsUri> participants, ImErrorReason reason) {
        Log.i(LOG_TAG, "onRemoveParticipantsFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.REMOVE_PARTICIPANTS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        broadcastIntent(intent, true);
    }

    public void onParticipantsAdded(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onParticipantsJoined(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onParticipantsLeft(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onChangeGroupChatLeaderSucceeded(String chatId, List<ImsUri> participants) {
        Log.i(LOG_TAG, "onChangeGroupChatLeaderSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUPCHAT_LEADER_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        broadcastIntent(intent);
    }

    public void onChangeGroupChatLeaderFailed(String chatId, List<ImsUri> participants, ImErrorReason reason) {
        Log.i(LOG_TAG, "onChangeGroupChatLeaderFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUPCHAT_LEADER_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, convertToStringList(participants));
        broadcastIntent(intent);
    }

    public void onChangeGroupChatSubjectSucceeded(String chatId, String subject) {
        Log.i(LOG_TAG, "onChangeGroupChatSubjectSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_CHAT_SUBJECT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("subject", subject);
        broadcastIntent(intent);
    }

    public void onChangeGroupChatSubjectFailed(String chatId, String subject, ImErrorReason reason) {
        Log.i(LOG_TAG, "onChangeGroupChatSubjectFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_CHAT_SUBJECT_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putExtra("subject", subject);
        broadcastIntent(intent);
    }

    public void onChangeGroupChatIconSuccess(String chatId, String icon_path) {
        Log.i(LOG_TAG, "onChangeGroupChatIconSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_GROUPCHAT_ICON_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_PATH, FileUtils.getUriForFileAsString(this.mContext, icon_path));
        broadcastIntent(intent);
    }

    public void onChangeGroupChatIconFailed(String chatId, String icon_path, ImErrorReason reason) {
        Log.i(LOG_TAG, "onChangeGroupChatIconFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SET_GROUPCHAT_ICON_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putExtra(ImIntent.Extras.GROUPCHAT_ICON_PATH, FileUtils.getUriForFileAsString(this.mContext, icon_path));
        broadcastIntent(intent);
    }

    public void onChangeGroupAliasSucceeded(String chatId, String alias) {
        Log.i(LOG_TAG, "onChangeGroupAliasSucceeded()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUP_ALIAS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("chat_id", chatId);
        intent.putExtra(ImIntent.Extras.USER_ALIAS, alias);
        broadcastIntent(intent);
    }

    public void onChangeGroupAliasFailed(String chatId, String alias, ImErrorReason reason) {
        Log.i(LOG_TAG, "onChangeGroupAliasFailed()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.CHANGE_GROUP_ALIAS_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reason.toString());
        intent.putExtra(ImIntent.Extras.USER_ALIAS, alias);
        broadcastIntent(intent);
    }

    public void onMessageInserted(MessageBase msg) {
        Log.i(LOG_TAG, "onMessageInserted()");
        Preconditions.checkNotNull(msg, "msg is null");
        Intent intent = new Intent(ImIntent.Action.RECEIVE_MESSAGE_INSERTED);
        Long messageId = Long.valueOf((long) msg.getId());
        String chatId = msg.getChatId();
        String reliableMessage = msg.getReliableMessage();
        ImConstants.Type type = msg.getType();
        if (type != ImConstants.Type.MULTIMEDIA && type != ImConstants.Type.TEXT && type != ImConstants.Type.LOCATION && type != ImConstants.Type.SYSTEM) {
            intent.addCategory(ImIntent.CATEGORY_ACTION);
            intent.putExtra("message_id", messageId);
            intent.putExtra("chat_id", chatId);
            intent.putExtra("message_type", type.getId());
            intent.putExtra(ImIntent.Extras.MESSAGE_DIRECTION, msg.getDirection().getId());
            intent.putExtra(ImIntent.Extras.MESSAGE_SERVICE, msg.getServiceTag());
            if (reliableMessage != null) {
                intent.putExtra("reliable_message", INNER_RELIABLE_URI + reliableMessage.substring(reliableMessage.lastIndexOf(47) + 1));
            }
            broadcastIntent(intent, true);
        }
    }

    public void onMessageSendResponseTimeout(MessageBase msg) {
        Log.i(LOG_TAG, "onMessageSendResponseTimeout()");
        Preconditions.checkNotNull(msg, "msg is null");
        Intent intent = new Intent(ImIntent.Action.SEND_MESSAGE_RESPONSE_TAKETOOLONG);
        Long messageId = Long.valueOf((long) msg.getId());
        String chatId = msg.getChatId();
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_id", messageId);
        intent.putExtra("chat_id", chatId);
        broadcastIntent(intent);
    }

    public void onMessageSendResponse(MessageBase msg) {
        Log.i(LOG_TAG, "onMessageSendResponse()");
        Intent intent = new Intent();
        intent.setAction(ImIntent.Action.SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", true);
        intent.putExtra("request_message_id", msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra("message_id", Long.valueOf((long) msg.getId()));
        String str = LOG_TAG;
        IMSLog.s(str, "onMessageSendResponse: " + intent + intent.getExtras());
        broadcastIntent(intent, true);
    }

    public void onMessageSendResponseFailed(String chatId, int messageNumber, int reasonCode, String requestMessageId) {
        String str = LOG_TAG;
        Log.i(str, "onMessageSendResponseFailed(): reasonCode = " + reasonCode + " requestMessageId = " + requestMessageId);
        Intent intent = new Intent(ImIntent.Action.SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.MESSAGE_NUMBER, messageNumber);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, reasonCode);
        intent.putExtra("chat_id", chatId);
        intent.putExtra("request_message_id", requestMessageId == null ? -1 : Long.valueOf(requestMessageId).longValue());
        intent.putExtra("message_id", -1);
        broadcastIntent(intent, true);
    }

    public void onMessageReceived(MessageBase msg, ImSession session) {
        Log.i(LOG_TAG, "onMessageReceived()");
        Preconditions.checkNotNull(msg, "message is null");
        String cid = msg.getChatId();
        String reliableMessage = msg.getReliableMessage();
        Long mid = Long.valueOf((long) msg.getId());
        Intent intent = new Intent(ImIntent.Action.RECEIVE_NEW_MESSAGE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("chat_id", cid);
        intent.putExtra("message_id", mid);
        intent.putExtra(ImIntent.Extras.IS_TOKEN_USED, session.getIsTokenUsed());
        intent.putExtra("message_type", msg.getType().getId());
        intent.putExtra("is_group_chat", session.isGroupChat());
        if (msg.mDeviceName != null) {
            intent.putExtra("device_name", msg.mDeviceName);
        }
        if (msg.isRoutingMsg()) {
            intent.putExtra(ImIntent.Extras.IS_ROUTING_MSG, msg.isRoutingMsg());
            if (!(msg.getRoutingType() == null || msg.getRoutingType() == RoutingType.NONE)) {
                intent.putExtra(ImIntent.Extras.ROUTING_MSG_TYPE, msg.getRoutingType().getId());
            }
        }
        if (reliableMessage != null) {
            intent.putExtra("reliable_message", INNER_RELIABLE_URI + reliableMessage.substring(reliableMessage.lastIndexOf(47) + 1));
        }
        intent.putExtra(ImIntent.Extras.MESSAGE_DIRECTION, msg.mDirection.getId());
        if (session.isGroupChat() && (msg instanceof ImMessage) && !((ImMessage) msg).getGroupCcListUri().isEmpty()) {
            intent.putStringArrayListExtra(ImIntent.Extras.GROUP_CCUSER_LIST, convertToStringList(((ImMessage) msg).getGroupCcListUri()));
        }
        if (msg.getDirection() == ImDirection.INCOMING) {
            intent.putExtra("from", msg.getRemoteUri() == null ? "" : msg.getRemoteUri().toString());
        }
        if (msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        putMaapExtras(msg, intent);
        if (msg.getReferenceId() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_ID, msg.getReferenceId());
        }
        if (msg.getReferenceType() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_TYPE, msg.getReferenceType());
        }
        if (msg.getReferenceValue() != null) {
            intent.putExtra(ImIntent.Extras.REFERENCE_MESSAGE_VALUE, msg.getReferenceValue());
        }
        String rcsTrafficType = msg.getRcsTrafficType();
        if (rcsTrafficType != null) {
            String str = LOG_TAG;
            Log.i(str, "rcsTrafficType = [" + rcsTrafficType + "]");
            intent.putExtra(ImIntent.Extras.RCS_TRAFFIC_TYPE, rcsTrafficType);
        }
        broadcastIntent(intent, true);
    }

    public void onImdnNotificationReceived(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        Log.i(LOG_TAG, "onMessageSentNotificationReceived()");
        broadcastIntent(createImdnNotificationReceivedIntent(msg, remoteUri, status, isGroupChat), true);
    }

    public void onMessageRevokeTimerExpired(String chatId, Collection<String> imdnIds) {
        String str = LOG_TAG;
        Log.i(str, "onMessageRevokeTimerExpired(): chatId = " + chatId + " imdnIds = " + imdnIds);
        Intent intent = new Intent(ImIntent.Action.MESSAGE_REVOKE_TIMER_EXPIRED);
        intent.putExtra("chat_id", chatId);
        intent.putStringArrayListExtra(ImIntent.Extras.MESSAGES_IMDN_ID_LIST, new ArrayList(imdnIds));
        broadcastIntent(intent, true);
    }

    public void onParticipantInserted(ImParticipant participant) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantInserted: " + IMSLog.numberChecker(participant.getUri()));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_INSERTED);
        intent.putExtra("chat_id", participant.getChatId());
        intent.putExtra("participant", participant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) participant.getId()));
        intent.putExtra(ImIntent.Extras.PARTICIPANT_STATUS, participant.getStatus().getId());
        intent.putExtra(ImIntent.Extras.USER_ALIAS, participant.getUserAlias());
        broadcastIntent(intent, true);
    }

    public void onParticipantInserted(ArrayList<ImParticipantData> participantsData) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantInserted: " + IMSLog.checker(participantsData));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANTS_INSERTED);
        intent.putParcelableArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, participantsData);
        broadcastIntent(intent, true);
    }

    public void onParticipantDeleted(ImParticipant participant) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantDeleted: " + IMSLog.numberChecker(participant.getUri()));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANT_DELETED);
        intent.putExtra("chat_id", participant.getChatId());
        intent.putExtra("participant", participant.getUri().toString());
        intent.putExtra(ImIntent.Extras.PARTICIPANT_ID, Long.valueOf((long) participant.getId()));
        intent.putExtra(ImIntent.Extras.PARTICIPANT_STATUS, participant.getStatus().getId());
        intent.putExtra(ImIntent.Extras.USER_ALIAS, participant.getUserAlias());
        broadcastIntent(intent, true);
    }

    public void onParticipantDeleted(ArrayList<ImParticipantData> participantsData) {
        String str = LOG_TAG;
        Log.i(str, "onParticipantDeleted: " + IMSLog.checker(participantsData));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_PARTICIPANTS_DELETED);
        intent.putParcelableArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, participantsData);
        broadcastIntent(intent, true);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleIntent(android.content.Intent r5) {
        /*
            r4 = this;
            java.lang.String r0 = r5.getAction()
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Received intent: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r1, r2)
            int r1 = r0.hashCode()
            switch(r1) {
                case -1913821034: goto L_0x014b;
                case -1675693016: goto L_0x0140;
                case -1519516883: goto L_0x0135;
                case -1476850583: goto L_0x012a;
                case -1241657933: goto L_0x011f;
                case -1144800568: goto L_0x0115;
                case -1120324265: goto L_0x010a;
                case -801423832: goto L_0x0100;
                case -653426186: goto L_0x00f5;
                case -589383736: goto L_0x00ea;
                case -479667282: goto L_0x00de;
                case -478765228: goto L_0x00d2;
                case -418937103: goto L_0x00c7;
                case -385468532: goto L_0x00bb;
                case -325378863: goto L_0x00af;
                case 375322443: goto L_0x00a3;
                case 421496980: goto L_0x0098;
                case 520146251: goto L_0x008c;
                case 703339109: goto L_0x0080;
                case 724573882: goto L_0x0075;
                case 906375040: goto L_0x0069;
                case 965765382: goto L_0x005e;
                case 1017807128: goto L_0x0052;
                case 1039119331: goto L_0x0046;
                case 1664331893: goto L_0x003a;
                case 1896987889: goto L_0x002f;
                case 2052413361: goto L_0x0023;
                default: goto L_0x0021;
            }
        L_0x0021:
            goto L_0x0155
        L_0x0023:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.REMOVE_PARTICIPANTS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 14
            goto L_0x0156
        L_0x002f:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELETE_CHATS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 7
            goto L_0x0156
        L_0x003a:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.GET_LAST_MESSAGES_SENT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 12
            goto L_0x0156
        L_0x0046:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.OPEN_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 9
            goto L_0x0156
        L_0x0052:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.MESSAGE_REVOKE_REQUEST"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 23
            goto L_0x0156
        L_0x005e:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.SEND_TYPING_NOTIFICATION"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 3
            goto L_0x0156
        L_0x0069:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELIVERY_TIMEOUT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 21
            goto L_0x0156
        L_0x0075:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.SEND_MESSAGE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 2
            goto L_0x0156
        L_0x0080:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.ACCEPT_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 24
            goto L_0x0156
        L_0x008c:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELETE_GROUPCHAT_ICON"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 18
            goto L_0x0156
        L_0x0098:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELETE_ALL_MESSAGES"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 6
            goto L_0x0156
        L_0x00a3:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.CHANGE_GROUP_ALIAS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 19
            goto L_0x0156
        L_0x00af:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.IGNORE_INCOMING_MESSAGE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 22
            goto L_0x0156
        L_0x00bb:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.SET_CHAT_SUBJECT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 16
            goto L_0x0156
        L_0x00c7:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.CREATE_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 0
            goto L_0x0156
        L_0x00d2:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.ANSWER_GC_CHAT_INVITATION"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 13
            goto L_0x0156
        L_0x00de:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.GET_IS_COMPOSING_ACTIVE_URIS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 11
            goto L_0x0156
        L_0x00ea:
            java.lang.String r1 = "com.samsung.rcs.framework.chatbot.action.CHATBOT_ANONYMIZE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 25
            goto L_0x0156
        L_0x00f5:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.CHANGE_GROUPCHAT_LEADER"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 15
            goto L_0x0156
        L_0x0100:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.ADD_PARTICIPANTS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 1
            goto L_0x0156
        L_0x010a:
            java.lang.String r1 = "com.samsung.rcs.framework.chatbot.action.REPORT_CHATBOT_AS_SPAM"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 26
            goto L_0x0156
        L_0x0115:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.READ_MESSAGE"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 4
            goto L_0x0156
        L_0x011f:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELETE_ALL_CHATS"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 8
            goto L_0x0156
        L_0x012a:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.CLOSE_CHAT"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 10
            goto L_0x0156
        L_0x0135:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.REPORT_MESSAGES"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 20
            goto L_0x0156
        L_0x0140:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.SET_GROUPCHAT_ICON"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 17
            goto L_0x0156
        L_0x014b:
            java.lang.String r1 = "com.samsung.rcs.framework.instantmessaging.action.DELETE_MESSAGES"
            boolean r1 = r0.equals(r1)
            if (r1 == 0) goto L_0x0021
            r1 = 5
            goto L_0x0156
        L_0x0155:
            r1 = -1
        L_0x0156:
            switch(r1) {
                case 0: goto L_0x01de;
                case 1: goto L_0x01da;
                case 2: goto L_0x01d6;
                case 3: goto L_0x01d2;
                case 4: goto L_0x01ce;
                case 5: goto L_0x01ca;
                case 6: goto L_0x01c6;
                case 7: goto L_0x01c2;
                case 8: goto L_0x01be;
                case 9: goto L_0x01ba;
                case 10: goto L_0x01b6;
                case 11: goto L_0x01b2;
                case 12: goto L_0x01ae;
                case 13: goto L_0x01aa;
                case 14: goto L_0x01a6;
                case 15: goto L_0x01a2;
                case 16: goto L_0x019e;
                case 17: goto L_0x019a;
                case 18: goto L_0x0196;
                case 19: goto L_0x0192;
                case 20: goto L_0x018e;
                case 21: goto L_0x018a;
                case 22: goto L_0x0185;
                case 23: goto L_0x0180;
                case 24: goto L_0x017b;
                case 25: goto L_0x0176;
                case 26: goto L_0x0171;
                default: goto L_0x0159;
            }
        L_0x0159:
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Unexpected intent received. acition="
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.e(r1, r2)
            goto L_0x01e2
        L_0x0171:
            r4.reportChatbotAsSpam(r5)
            goto L_0x01e2
        L_0x0176:
            r4.requestChatbotAnonymize(r5)
            goto L_0x01e2
        L_0x017b:
            r4.requestAcceptChat(r5)
            goto L_0x01e2
        L_0x0180:
            r4.requestMessageRevocation(r5)
            goto L_0x01e2
        L_0x0185:
            r4.requestIgnoreIncomingMsgSet(r5)
            goto L_0x01e2
        L_0x018a:
            r4.requestDeliveryTimeout(r5)
            goto L_0x01e2
        L_0x018e:
            r4.requestReportMessage(r5)
            goto L_0x01e2
        L_0x0192:
            r4.requestChangeGroupAlias(r5)
            goto L_0x01e2
        L_0x0196:
            r4.requestDeleteGroupChatIcon(r5)
            goto L_0x01e2
        L_0x019a:
            r4.requestChangeGroupChatIcon(r5)
            goto L_0x01e2
        L_0x019e:
            r4.requestChangeGroupChatSubject(r5)
            goto L_0x01e2
        L_0x01a2:
            r4.requestChangeGroupChatLeader(r5)
            goto L_0x01e2
        L_0x01a6:
            r4.requestRemoveGroupChatParticipants(r5)
            goto L_0x01e2
        L_0x01aa:
            r4.requestAnswerGcChats(r5)
            goto L_0x01e2
        L_0x01ae:
            r4.requestGetLastSentMessages(r5)
            goto L_0x01e2
        L_0x01b2:
            r4.requestComposingActiveUris(r5)
            goto L_0x01e2
        L_0x01b6:
            r4.requestCloseChat(r5)
            goto L_0x01e2
        L_0x01ba:
            r4.requestOpenChat(r5)
            goto L_0x01e2
        L_0x01be:
            r4.requestDeleteAllChats()
            goto L_0x01e2
        L_0x01c2:
            r4.requestDeleteChats(r5)
            goto L_0x01e2
        L_0x01c6:
            r4.requestDeleteAllMessages(r5)
            goto L_0x01e2
        L_0x01ca:
            r4.requestDeleteMessages(r5)
            goto L_0x01e2
        L_0x01ce:
            r4.requestReadMessage(r5)
            goto L_0x01e2
        L_0x01d2:
            r4.requestSendComposingNotification(r5)
            goto L_0x01e2
        L_0x01d6:
            r4.requestSendMessage(r5)
            goto L_0x01e2
        L_0x01da:
            r4.requestAddParticipantsToChat(r5)
            goto L_0x01e2
        L_0x01de:
            r4.requestCreateChat(r5)
        L_0x01e2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImTranslation.handleIntent(android.content.Intent):void");
    }

    private void broadcastIntent(Intent intent) {
        broadcastIntent(intent, false);
    }

    private void broadcastIntent(Intent intent, boolean isForeground) {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        if (isForeground) {
            intent.addFlags(LogClass.SIM_EVENT);
        }
        if (this.mImModule.getRcsStrategy() == null || !this.mImModule.getRcsStrategy().isBMode(true)) {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.OWNER);
        }
    }

    public void updateMessage(MessageBase message, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            onMessageInserted(message);
        }
    }

    public void updateMessage(Collection<MessageBase> messages, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            for (MessageBase message : messages) {
                onMessageInserted(message);
            }
        }
    }

    public void updateParticipant(ImParticipant participant, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED) {
            onParticipantInserted(participant);
        } else if (action == ImCacheAction.DELETED) {
            onParticipantDeleted(participant);
        }
    }

    public void updateParticipant(Collection<ImParticipant> participants, ImCacheAction action) {
        if (action == ImCacheAction.INSERTED || action == ImCacheAction.DELETED) {
            ArrayList<ImParticipantData> list = new ArrayList<>();
            for (ImParticipant p : participants) {
                list.add(new ImParticipantData(p.getChatId(), p.getUri().toString(), p.getId(), p.getStatus().getId(), p.getUserAlias()));
            }
            if (action == ImCacheAction.INSERTED) {
                onParticipantInserted(list);
            } else {
                onParticipantDeleted(list);
            }
        }
    }

    public void onGroupChatLeaderUpdated(String chatId, String leaderParticipant) {
        String str = LOG_TAG;
        Log.i(str, "onGroupChatLeaderUpdated: " + IMSLog.numberChecker(leaderParticipant));
        Intent intent = new Intent(ImIntent.Action.RECEIVE_GROUPCHAT_LEADER_CHANGED);
        intent.putExtra("chat_id", chatId);
        ArrayList<String> leaderList = new ArrayList<>();
        leaderList.add(leaderParticipant);
        intent.putStringArrayListExtra(ImIntent.Extras.PARTICIPANTS_LIST, leaderList);
        broadcastIntent(intent, true);
    }
}
