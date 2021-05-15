package com.sec.internal.ims.servicemodules.gls;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.gls.GlsIntent;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import org.json.JSONException;
import org.json.JSONObject;

public class GlsTranslation implements IMessageEventListener, IFtEventListener {
    private static final String LOG_TAG = GlsTranslation.class.getSimpleName();
    private final Context mContext;
    private final GlsModule mGlsModule;

    public GlsTranslation(Context context, GlsModule glsModule) {
        this.mGlsModule = glsModule;
        this.mContext = context;
        glsModule.registerFtEventListener(ImConstants.Type.LOCATION, this);
        this.mGlsModule.registerMessageEventListener(ImConstants.Type.LOCATION, this);
    }

    public void handleIntent(Intent intent) {
        if (!intent.hasCategory(GlsIntent.CATEGORY_ACTION)) {
            return;
        }
        if (GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT.equals(intent.getAction()) || GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT_GC.equals(intent.getAction())) {
            requestShareLocationInChat(intent);
        } else if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL.equals(intent.getAction()) || GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL_GC.equals(intent.getAction())) {
            requestCreateInCallLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.ACCEPT_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestAcceptLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.REJECT_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestRejectLocationShare(intent);
        } else if (GlsIntent.Actions.RequestIntents.START_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            requestStartLocationShareInCall(intent);
        } else {
            String str = LOG_TAG;
            Log.v(str, "Unknown action: " + intent.getAction());
        }
    }

    private void requestShareLocationInChat(Intent intent) {
        String label;
        boolean isGroupChat;
        ImsUri ImsContactUri;
        int phoneId;
        Log.i(LOG_TAG, "requestShareLocationInChat()");
        Bundle extras = intent.getExtras();
        String cid = extras.getString("chat_id");
        String disposition = extras.getString("disposition_notification");
        Location location = (Location) extras.getParcelable(GlsIntent.Extras.EXTRA_LOCATION);
        String label2 = extras.getString("label");
        String location_type = Integer.toString(extras.getInt(GlsIntent.Extras.EXTRA_LOCATION_TYPE));
        if ("1".equals(location_type)) {
            label = null;
        } else {
            label = label2;
        }
        String requestMessageId = String.valueOf(extras.getLong("request_message_id"));
        String locationLink = extras.getString(GlsIntent.Extras.EXTRA_LOCATION_LINK);
        Uri contactUri = (Uri) extras.getParcelable("contactUri");
        String maapTrafficType = extras.getString("maap_traffic_type");
        String slotId = extras.getString("sim_slot_id");
        if (GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT.equals(intent.getAction())) {
            ImsContactUri = ImsUri.parse(contactUri.toString());
            isGroupChat = false;
        } else if (GlsIntent.Actions.RequestIntents.SHARE_LOCATION_IN_CHAT_GC.equals(intent.getAction())) {
            ImsContactUri = null;
            isGroupChat = true;
        } else {
            ImsContactUri = null;
            isGroupChat = false;
        }
        if (!TextUtils.isEmpty(slotId)) {
            try {
                phoneId = Integer.valueOf(slotId).intValue();
            } catch (NumberFormatException e) {
                Log.i(LOG_TAG, "Invalid slot id : " + slotId);
            }
            String str = slotId;
            String str2 = location_type;
            this.mGlsModule.shareLocationInChat(phoneId, cid, NotificationStatus.toSet(disposition), location, label, requestMessageId, locationLink, ImsContactUri, isGroupChat, maapTrafficType);
        }
        phoneId = -1;
        String str3 = slotId;
        String str22 = location_type;
        this.mGlsModule.shareLocationInChat(phoneId, cid, NotificationStatus.toSet(disposition), location, label, requestMessageId, locationLink, ImsContactUri, isGroupChat, maapTrafficType);
    }

    private void requestCreateInCallLocationShare(Intent intent) {
        boolean isGroupChat;
        ImsUri ImsContactUri;
        Log.i(LOG_TAG, "requestCreateInCallLocationShare()");
        Bundle extras = intent.getExtras();
        String chatId = extras.getString("chat_id");
        Uri contactUri = (Uri) extras.getParcelable("contactUri");
        String disposition = extras.getString("disposition_notification");
        Location location = (Location) extras.getParcelable(GlsIntent.Extras.EXTRA_LOCATION);
        String label = extras.getString("label");
        String requestMessageId = String.valueOf(extras.getLong("request_message_id"));
        boolean isPublicAccountMsg = extras.getBoolean("is_publicAccountMsg", false);
        if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL.equals(intent.getAction())) {
            ImsContactUri = ImsUri.parse(contactUri.toString());
            isGroupChat = false;
        } else if (GlsIntent.Actions.RequestIntents.CREATE_SHARE_LOCATION_INCALL_GC.equals(intent.getAction())) {
            ImsContactUri = ImsUri.parse("sip:anonymous@anonymous.invalid");
            isGroupChat = true;
        } else {
            ImsContactUri = null;
            isGroupChat = false;
        }
        this.mGlsModule.createInCallLocationShare(chatId, ImsContactUri, NotificationStatus.toSet(disposition), location, label, requestMessageId, isPublicAccountMsg, isGroupChat);
    }

    private void requestAcceptLocationShare(Intent intent) {
        this.mGlsModule.acceptLocationShare(intent.getExtras().getLong("sessionId"));
    }

    private void requestRejectLocationShare(Intent intent) {
        this.mGlsModule.rejectLocationShare(intent.getExtras().getLong("sessionId"));
    }

    public void requestStartLocationShareInCall(Intent intent) {
        this.mGlsModule.startLocationShareInCall(intent.getExtras().getLong("sessionId"));
    }

    public void onShareLocationInChatResponse(String cid, String requestAppId, int mid, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.SHARE_LOCATION_IN_CHAT_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("request_message_id", requestAppId == null ? -1 : Long.valueOf(requestAppId).longValue());
        intent.putExtra("chat_id", cid);
        intent.putExtra("sessionId", Long.valueOf((long) mid));
        broadcastIntent(intent, true);
    }

    public void onReceiveShareLocationInChatResponse(String cid, String requestAppId, int mid, boolean success, IMnoStrategy.StrategyResponse strategyResponse, IMnoStrategy mnoStrategy, Result result) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_SHARE_LOCATION_IN_CHAT_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("request_message_id", requestAppId == null ? -1 : Long.valueOf(requestAppId).longValue());
        intent.putExtra("chat_id", cid);
        intent.putExtra("sessionId", Long.valueOf((long) mid));
        if (strategyResponse != null) {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(strategyResponse.getStatusCode()));
            if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
                intent.putExtra(ImIntent.Extras.ERROR_REASON, ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString());
            }
        }
        if (!(mnoStrategy == null || result == null || !mnoStrategy.isDisplayWarnText() || result.getImError() == null)) {
            intent.putExtra(ImIntent.Extras.WARN_TEXT, result.getImError().toString());
        }
        broadcastIntent(intent, true);
    }

    public void onReceiveShareLocationInChatMsg(MessageBase msg, boolean isGroupChat) {
        Long midL = Long.valueOf((long) msg.getId());
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("chat_id", msg.getChatId());
        intent.putExtra("sessionId", midL);
        intent.putExtra("is_group_chat", isGroupChat);
        intent.putExtra("sessionDirection", msg.getDirection().getId());
        String suggestion = msg.getSuggestion();
        if (suggestion != null) {
            try {
                JSONObject jsonObj = new JSONObject(suggestion);
                jsonObj.remove("persistent");
                suggestion = jsonObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtra(ImIntent.Extras.SUGGESTION_TEXT, suggestion);
        }
        String maapTrafficType = msg.getMaapTrafficType();
        if (maapTrafficType != null) {
            intent.putExtra("maap_traffic_type", maapTrafficType);
        }
        if (msg.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        broadcastIntent(intent, true);
    }

    public void onCreateInCallLocationShareResponse(String cid, long sessionid, String requestAppId, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.CREATE_SHARE_LOCATION_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("request_message_id", requestAppId == null ? -1 : Long.valueOf(requestAppId).longValue());
        intent.putExtra("chat_id", cid);
        intent.putExtra("sessionId", Long.valueOf(sessionid));
        broadcastIntent(intent);
    }

    public void onAcceptLocationShareInCallResponse(long sessionid, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.ACCEPT_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        broadcastIntent(intent);
    }

    public void onRejectLocationShareInCallResponse(long sessionid, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.REJECT_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        broadcastIntent(intent);
    }

    public void onCancelLocationShareInCallResponse(long sessionid, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.CANCEL_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        broadcastIntent(intent);
    }

    public void onDeleteAllLocationShareResponse(boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.DELETE_ALL_LOCATION_SHARE_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        broadcastIntent(intent);
    }

    public void onStartLocationShareInCallResponse(long sessionid, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.START_SHARE_LOCATION_INCALL_RESPONSE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        broadcastIntent(intent);
    }

    public void onIncomingLoactionShareInCall(FtMessage transfer) {
        Intent intent = new Intent();
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.setAction(GlsIntent.Actions.ResponseIntents.INCOMING_LOCATION_SHARE_INCALL_INVITATION);
        intent.putExtra("sessionId", Long.valueOf((long) transfer.getId()));
        intent.putExtra("chat_id", transfer.getChatId());
        intent.putExtra("contactUri", transfer.getRemoteUri());
        if (transfer.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING) {
            intent.putExtra(ImIntent.Extras.IS_BOT, true);
        }
        broadcastIntent(intent);
    }

    public void onLocationShareInCallCompleted(long sessionid, ImDirection direction, boolean success) {
        Intent intent = new Intent();
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        intent.putExtra("sessionDirection", direction.getId());
        broadcastIntent(intent, true);
    }

    public void onLocationShareInCallCompleted(long sessionid, ImDirection direction, boolean success, MessageBase msg) {
        Intent intent = new Intent();
        if (direction == ImDirection.INCOMING) {
            intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_SHARE_MESSAGE);
        } else {
            intent.setAction(GlsIntent.Actions.ResponseIntents.SENT_LOCATION_SHARE_MESSAGE);
        }
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.putExtra("response_status", success);
        intent.putExtra("sessionId", sessionid);
        intent.putExtra("sessionDirection", direction.getId());
        if (msg != null) {
            intent.putExtra("request_message_id", msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
            intent.putExtra("chat_id", msg.getChatId());
            String maapTrafficType = msg.getMaapTrafficType();
            if (maapTrafficType != null) {
                intent.putExtra("maap_traffic_type", maapTrafficType);
            }
        }
        broadcastIntent(intent, true);
    }

    public void onImdnNotificationReceived(MessageBase msg, boolean isGroupChat) {
        Intent intent = new Intent();
        intent.addCategory(GlsIntent.CATEGORY_NOTIFICATION);
        intent.setAction(GlsIntent.Actions.ResponseIntents.RECEIVE_LOCATION_NOTIFICATION_STATUS);
        intent.putExtra("message_id", Long.valueOf((long) msg.getId()));
        intent.putExtra("message_notification_status", msg.getNotificationStatus().getId());
        intent.putExtra(ImIntent.Extras.MESSAGE_NOTIFICATION_STATUS_RECEIVED, msg.getLastNotificationType().getId());
        intent.putExtra("is_group_chat", isGroupChat);
        broadcastIntent(intent, true);
    }

    private void broadcastIntent(Intent intent) {
        broadcastIntent(intent, false);
    }

    private void broadcastIntent(Intent intent, boolean isForeground) {
        String str = LOG_TAG;
        Log.i(str, "broadcastIntent: " + intent.toString() + intent.getExtras());
        if (isForeground) {
            intent.addFlags(LogClass.SIM_EVENT);
        }
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void onFileTransferCreated(FtMessage msg) {
    }

    public void onFileTransferAttached(FtMessage msg) {
        Log.i(LOG_TAG, "onFileTransferAttached: call onOutgoingTransferAttached");
        this.mGlsModule.onOutgoingTransferAttached(msg);
    }

    public void onFileTransferReceived(FtMessage msg) {
        Log.i(LOG_TAG, "onFileTransferReceived: call onIncomingTransferUndecided");
        this.mGlsModule.onIncomingTransferUndecided(msg);
    }

    public void onTransferProgressReceived(FtMessage msg) {
    }

    public void onTransferStarted(FtMessage msg) {
    }

    public void onTransferCompleted(FtMessage msg) {
        Log.i(LOG_TAG, "onTransferCompleted: call onTransferCompleted");
        this.mGlsModule.onTransferCompleted(msg);
    }

    public void onTransferCanceled(FtMessage msg) {
        Log.i(LOG_TAG, "onTransferCanceled: call onTransferCanceled");
        this.mGlsModule.onTransferCanceled(msg);
    }

    public void onImdnNotificationReceived(FtMessage msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        onImdnNotificationReceived(msg, isGroupChat);
    }

    public void onMessageSendResponseTimeout(MessageBase msg) {
    }

    public void onMessageSendResponse(MessageBase msg) {
        onShareLocationInChatResponse(msg.getChatId(), msg.getRequestMessageId(), msg.getId(), true);
    }

    public void onMessageReceived(MessageBase msg, ImSession session) {
        this.mGlsModule.updateExtInfo(msg);
        onReceiveShareLocationInChatMsg(msg, session.isGroupChat());
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        onReceiveShareLocationInChatResponse(msg.getChatId(), msg.getRequestMessageId(), msg.getId(), true, (IMnoStrategy.StrategyResponse) null, msg.getRcsStrategy(), (Result) null);
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse reason, Result result) {
        onReceiveShareLocationInChatResponse(msg.getChatId(), msg.getRequestMessageId(), msg.getId(), false, reason, msg.getRcsStrategy(), result);
    }

    public void onMessageSendResponseFailed(String chatId, int messageNumber, int reasoncode, String requestMessageId) {
    }

    public void onImdnNotificationReceived(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        onImdnNotificationReceived(msg, isGroupChat);
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
    }

    public void onCancelRequestFailed(FtMessage msg) {
    }

    /* renamed from: com.sec.internal.ims.servicemodules.gls.GlsTranslation$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        static {
            int[] iArr = new int[IMnoStrategy.StatusCode.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = iArr;
            try {
                iArr[IMnoStrategy.StatusCode.DISPLAY_ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[IMnoStrategy.StatusCode.DISPLAY_ERROR_CFS.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private int getRequiredAction(IMnoStrategy.StatusCode state) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[state.ordinal()];
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 2;
        }
        if (i != 4) {
            return 0;
        }
        return 3;
    }
}
