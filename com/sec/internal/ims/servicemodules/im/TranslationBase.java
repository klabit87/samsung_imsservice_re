package com.sec.internal.ims.servicemodules.im;

import android.content.Intent;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import org.json.JSONException;
import org.json.JSONObject;

public class TranslationBase {
    private static final String LOG_TAG = TranslationBase.class.getSimpleName();

    /* renamed from: com.sec.internal.ims.servicemodules.im.TranslationBase$1  reason: invalid class name */
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

    /* access modifiers changed from: protected */
    public int getRequiredAction(IMnoStrategy.StatusCode state) {
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

    /* access modifiers changed from: protected */
    public void putMaapExtras(MessageBase msg, Intent intent) {
        String suggestion = msg.getSuggestion();
        if (suggestion != null) {
            try {
                JSONObject jsonObj = new JSONObject(suggestion);
                jsonObj.remove("persistent");
                suggestion = jsonObj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "no suggestions ");
            }
            String str = LOG_TAG;
            Log.i(str, "suggestion = " + IMSLog.checker(suggestion));
            intent.putExtra(ImIntent.Extras.SUGGESTION_TEXT, suggestion);
        }
        String maapTrafficType = msg.getMaapTrafficType();
        if (maapTrafficType != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "maapTrafficType = [" + maapTrafficType + "]");
            intent.putExtra("maap_traffic_type", maapTrafficType);
        }
    }

    public Intent createMessageSendingFailedIntent(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Intent intent = new Intent(ImIntent.Action.RECEIVE_SEND_MESSAGE_RESPONSE);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_id", (long) msg.getId());
        intent.putExtra("response_status", false);
        intent.putExtra(ImIntent.Extras.ERROR_REASON, msg.getRcsStrategy() != null ? msg.getRcsStrategy().getErrorReasonForStrategyResponse(msg, strategyResponse) : null);
        if (!(msg.getRcsStrategy() == null || result == null)) {
            if (msg.getRcsStrategy().isDisplayBotError() && result.getSipResponse() != null) {
                intent.putExtra(ImIntent.Extras.SIP_ERROR, result.getSipResponse().getId());
            }
            if (msg.getRcsStrategy().isDisplayBotError() && result.getMsrpResponse() != null) {
                intent.putExtra(ImIntent.Extras.SIP_ERROR, result.getMsrpResponse().getId());
            }
            if (msg.getRcsStrategy().isDisplayWarnText() && result.getImError() != null) {
                intent.putExtra(ImIntent.Extras.WARN_TEXT, result.getImError().toString());
            }
        }
        intent.putExtra("request_message_id", msg.getRequestMessageId() == null ? -1 : Long.valueOf(msg.getRequestMessageId()).longValue());
        intent.putExtra("is_broadcast_msg", msg.isBroadcastMsg());
        if (strategyResponse != null) {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(strategyResponse.getStatusCode()));
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, strategyResponse.getErrorNotificationId().ordinal());
        } else {
            intent.putExtra(ImIntent.Extras.REQUIRED_ACTION, getRequiredAction(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            intent.putExtra(ImIntent.Extras.ERROR_NOTIFICATION_ID, IMnoStrategy.ErrorNotificationId.NONE.ordinal());
        }
        return intent;
    }

    public Intent createImdnNotificationReceivedIntent(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
        Intent intent = new Intent(ImIntent.Action.RECEIVE_MESSAGE_NOTIFICATION_STATUS);
        intent.addCategory(ImIntent.CATEGORY_ACTION);
        intent.putExtra("message_id", Long.valueOf((long) msg.getId()));
        intent.putExtra("message_notification_status", msg.getNotificationStatus().getId());
        intent.putExtra(ImIntent.Extras.MESSAGE_NOTIFICATION_STATUS_RECEIVED, msg.getLastNotificationType().getId());
        intent.putExtra("is_group_chat", isGroupChat);
        return intent;
    }
}
