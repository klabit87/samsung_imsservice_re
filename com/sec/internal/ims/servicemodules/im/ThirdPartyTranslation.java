package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.im.interfaces.ImIntent;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Locale;

public class ThirdPartyTranslation implements IMessageEventListener, IFtEventListener {
    private static final String LOG_TAG = ThirdPartyTranslation.class.getSimpleName();
    private final Context mContext;
    private final ImModule mImModule;

    public ThirdPartyTranslation(Context context, ImModule imModule) {
        Log.i(LOG_TAG, "Create ThirdPartyTranslation.");
        this.mContext = context;
        this.mImModule = imModule;
        imModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.TEXT_PUBLICACCOUNT, this);
        this.mImModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA, this);
        this.mImModule.registerFtEventListener(ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT, this);
    }

    public void broadcastIntent(Intent intent) throws NullPointerException {
        String str = LOG_TAG;
        IMSLog.s(str, "broadcastIntent: " + intent + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void onFileTransferCreated(FtMessage msg) {
    }

    public void onFileTransferAttached(FtMessage msg) {
    }

    public void onFileTransferReceived(FtMessage msg) {
    }

    public void onTransferProgressReceived(FtMessage msg) {
    }

    public void onTransferStarted(FtMessage msg) {
    }

    public void onTransferCompleted(FtMessage msg) {
        if (this.mImModule.notifyRCSMessages()) {
            ImSession session = this.mImModule.getImSession(msg.getChatId());
            ArrayList<String> participants = new ArrayList<>();
            if (session != null) {
                participants.addAll(session.getParticipantsString());
            }
            if (participants.isEmpty()) {
                Log.i(LOG_TAG, "onTransferCompleted: no participants for this chat");
            }
            Intent rcsMsgIntent = new Intent(ImIntent.Action.RCS_MESSAGE);
            rcsMsgIntent.addCategory("com.gsma.services.rcs.category.ACTION");
            String str = "";
            if (msg.getDirection() == ImDirection.INCOMING) {
                rcsMsgIntent.putExtra("from", msg.getRemoteUri() == null ? str : msg.getRemoteUri().toString());
            } else {
                rcsMsgIntent.putExtra("recipients", participants);
            }
            rcsMsgIntent.putExtra("direction", msg.getDirection().getId());
            broadcastIntent(rcsMsgIntent);
            if (msg.getDirection() == ImDirection.INCOMING) {
                Intent rcsIntent = new Intent(ImIntent.Action.RECEIVE_RCS_MESSAGE);
                rcsIntent.addCategory("com.gsma.services.rcs.category.ACTION");
                String upperCase = "from".toUpperCase(Locale.US);
                if (msg.getRemoteUri() != null) {
                    str = msg.mRemoteUri.toString();
                }
                rcsIntent.putExtra(upperCase, str);
                broadcastIntent(rcsIntent);
            }
        }
    }

    public void onTransferCanceled(FtMessage msg) {
    }

    public void onImdnNotificationReceived(FtMessage msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
    }

    public void onMessageSendResponse(MessageBase msg) {
        if (this.mImModule.notifyRCSMessages()) {
            ImSession session = this.mImModule.getImSession(msg.getChatId());
            ArrayList<String> participants = new ArrayList<>();
            if (session != null) {
                participants.addAll(session.getParticipantsString());
            }
            if (participants.isEmpty()) {
                Log.i(LOG_TAG, "onMessageSendResponse: no participants for this chat");
            }
            Intent rcsIntent = new Intent(ImIntent.Action.RCS_MESSAGE);
            rcsIntent.addCategory("com.gsma.services.rcs.category.ACTION");
            rcsIntent.putStringArrayListExtra("recipients", participants);
            rcsIntent.putExtra("direction", ImDirection.OUTGOING.getId());
            broadcastIntent(rcsIntent);
        }
    }

    public void onCancelRequestFailed(FtMessage msg) {
    }

    public void onMessageSendResponseTimeout(MessageBase msg) {
    }

    public void onMessageSendResponseFailed(String chatId, int messageNumber, int reasonCode, String requestMessageId) {
    }

    public void onMessageReceived(MessageBase msg, ImSession session) {
        if (this.mImModule.notifyRCSMessages()) {
            Intent rcsMsgIntent = new Intent(ImIntent.Action.RCS_MESSAGE);
            rcsMsgIntent.addCategory("com.gsma.services.rcs.category.ACTION");
            if (msg.getDirection() == ImDirection.INCOMING) {
                rcsMsgIntent.putExtra("from", msg.getRemoteUri().toString());
            }
            rcsMsgIntent.putExtra("direction", msg.getDirection().getId());
            broadcastIntent(rcsMsgIntent);
            if (msg.getDirection() == ImDirection.INCOMING) {
                Intent rcsIntent = new Intent(ImIntent.Action.RECEIVE_RCS_MESSAGE);
                rcsIntent.addCategory("com.gsma.services.rcs.category.ACTION");
                rcsIntent.putExtra("from".toUpperCase(Locale.US), msg.getRemoteUri().toString());
                broadcastIntent(rcsIntent);
            }
        }
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
    }

    public void onImdnNotificationReceived(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
    }
}
