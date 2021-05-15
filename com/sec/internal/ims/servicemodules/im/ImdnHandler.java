package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImdnHandler {
    private static final String LOG_TAG = ImdnHandler.class.getSimpleName();
    private ImCache mCache;
    private Context mContext;
    private FtProcessor mFtProcessor;
    private ImModule mImModule;
    private ImProcessor mImProcessor;
    private ImSessionProcessor mImSessionProcessor;

    public ImdnHandler(Context context, ImModule imModule, ImCache imCache, ImProcessor imProcessor, FtProcessor ftProcessor, ImSessionProcessor imSessionProcessor) {
        this.mContext = context;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImProcessor = imProcessor;
        this.mFtProcessor = ftProcessor;
        this.mImSessionProcessor = imSessionProcessor;
    }

    /* access modifiers changed from: protected */
    public void readMessages(String cid, List<String> list, boolean updateOnlyMStore) {
        ICapabilityDiscoveryModule discoveryModule;
        Log.i(LOG_TAG, "readMessage: cid " + cid + " index : " + list);
        int phoneId = this.mImModule.getPhoneIdByChatId(cid);
        this.mCache.readMessagesforCloudSync(phoneId, list);
        if (updateOnlyMStore) {
            for (String idStr : list) {
                updateDbForReadMessage(this.mCache.getMessage(Integer.valueOf(idStr).intValue()));
            }
            return;
        }
        ImSession session = this.mCache.getImSession(cid);
        if (session == null) {
            Log.e(LOG_TAG, "readMessage: Session not found in the cache.");
            return;
        }
        String str = "sendDisplayedNotification: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnIds=";
        List<MessageBase> messesages = this.mCache.getMessages(list);
        for (MessageBase m : messesages) {
            str = str + m.getImdnId() + ", ";
        }
        this.mImModule.getImDump().addEventLogs(str);
        if (this.mImModule.isRegistered(phoneId)) {
            if (this.mImModule.getRcsStrategy().needToCapabilityCheckForImdn(session.isGroupChat()) && (discoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule()) != null) {
                Capabilities capx = discoveryModule.getCapabilities(session.getParticipantsUri().iterator().next(), CapabilityRefreshType.ONLY_IF_NOT_FRESH, phoneId);
                if (capx == null) {
                    Log.i(LOG_TAG, "readMessage: cap is null");
                } else if (capx.hasFeature(Capabilities.FEATURE_NON_RCS_USER) && !session.isEstablishedState()) {
                    for (MessageBase m2 : messesages) {
                        m2.updateDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                        m2.onSendDisplayedNotificationDone();
                    }
                    return;
                }
            }
            session.readMessages(list);
            return;
        }
        Log.i(LOG_TAG, "readMessage: not registered, mark status as displayed.");
        this.mCache.updateDesiredNotificationStatusAsDisplay(list);
    }

    private void updateDbForReadMessage(MessageBase msg) {
        if (msg == null) {
            return;
        }
        if ((msg instanceof FtHttpIncomingMessage) || msg.getStatus() != ImConstants.Status.FAILED) {
            msg.updateStatus(ImConstants.Status.READ);
            msg.updateDisplayedTimestamp(System.currentTimeMillis());
            msg.updateDesiredNotificationStatus(NotificationStatus.DISPLAYED);
            msg.updateNotificationStatus(NotificationStatus.DISPLAYED);
            return;
        }
        String str = LOG_TAG;
        Log.e(str, "Do not update message with status FAILED: messageId" + msg.getId());
    }

    /* access modifiers changed from: protected */
    public void sendComposingNotification(String cid, int interval, boolean isTyping) {
        String str = LOG_TAG;
        Log.i(str, "sendComposingNotification: chatId=" + cid + " typing=" + isTyping + " interval=" + interval);
        ImSession session = this.mCache.getImSession(cid);
        if (session == null) {
            Log.e(LOG_TAG, "Session not found in the cache.");
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        if (!this.mImModule.isRegistered(phoneId)) {
            Log.e(LOG_TAG, "sendComposingNotification: not registered");
            return;
        }
        if (!session.isAutoAccept() && this.mImModule.getImConfig(phoneId).getImSessionStart() != ImConstants.ImSessionStart.WHEN_PRESSES_SEND_BUTTON) {
            session.acceptSession(false);
        }
        session.sendComposing(isTyping, interval);
    }

    /* access modifiers changed from: protected */
    public void onImdnNotificationReceived(ImdnNotificationEvent event) {
        ImdnNotificationEvent imdnNotificationEvent = event;
        String str = LOG_TAG;
        Log.i(str, "onImdnNotificationReceived: " + imdnNotificationEvent);
        MessageBase msg = this.mCache.getMessage(imdnNotificationEvent.mImdnId, ImDirection.OUTGOING);
        if (msg == null) {
            Log.e(LOG_TAG, "onImdnNotificationReceived: Couldn't find the im message.");
            return;
        }
        ImModule imModule = this.mImModule;
        imdnNotificationEvent.mRemoteUri = imModule.normalizeUri(imModule.getPhoneIdByIMSI(msg.getOwnIMSI()), imdnNotificationEvent.mRemoteUri);
        NotificationStatus status = this.mCache.getNotificationStatus(imdnNotificationEvent.mImdnId, imdnNotificationEvent.mRemoteUri);
        if (!isValidImdnNotification(status, imdnNotificationEvent.mStatus)) {
            String str2 = LOG_TAG;
            Log.i(str2, "onImdnNotificationReceived: ignore. current status=" + status);
            return;
        }
        ImSession session = this.mCache.getImSession(msg.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "onImdnNotificationReceived: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onImdnNotificationReceived: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId=" + imdnNotificationEvent.mImdnId + ", status=" + imdnNotificationEvent.mStatus);
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        boolean isGroupChat = session.isGroupChat();
        for (MessageBase m : getMessagesForReceivedImdn(!session.isGroupChat() && RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_AGGREGATION_DISPLAYED_IMDN), imdnNotificationEvent.mStatus, session.getChatId(), msg)) {
            m.onImdnNotificationReceived(imdnNotificationEvent);
            if (session.getNeedToRevokeMessages().containsKey(m.getImdnId())) {
                m.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                session.removeMsgFromListForRevoke(m.getImdnId());
                this.mCache.removeFromPendingList(m.getId());
            }
            if (m instanceof ImMessage) {
                for (IMessageEventListener listener : this.mImProcessor.getMessageEventListener(m.getType())) {
                    listener.onImdnNotificationReceived(m, imdnNotificationEvent.mRemoteUri, m.getLastNotificationType(), isGroupChat);
                }
            } else if (m instanceof FtMessage) {
                for (IFtEventListener listener2 : this.mFtProcessor.getFtEventListener(m.getType())) {
                    listener2.onImdnNotificationReceived((FtMessage) m, imdnNotificationEvent.mRemoteUri, m.getLastNotificationType(), isGroupChat);
                }
            }
        }
        if (!isGroupChat) {
            this.mImSessionProcessor.setLegacyLatching(session.getRemoteUri(), false, session.getChatData().getOwnIMSI());
        }
        if ((imdnNotificationEvent.mStatus == NotificationStatus.DELIVERED || imdnNotificationEvent.mStatus == NotificationStatus.DISPLAYED) && !isGroupChat) {
            this.mImModule.updateServiceAvailability(session.getChatData().getOwnIMSI(), imdnNotificationEvent.mRemoteUri, imdnNotificationEvent.mCpimDate);
        }
    }

    private boolean isValidImdnNotification(NotificationStatus currentStatus, NotificationStatus receivedStatus) {
        if (currentStatus == null || currentStatus == NotificationStatus.DISPLAYED) {
            return false;
        }
        if (currentStatus == NotificationStatus.DELIVERED && receivedStatus == NotificationStatus.DELIVERED) {
            return false;
        }
        return true;
    }

    private List<MessageBase> getMessagesForReceivedImdn(boolean isAggregationUsed, NotificationStatus status, String chatId, MessageBase msg) {
        List<MessageBase> messages = new ArrayList<>();
        if (!isAggregationUsed || status != NotificationStatus.DISPLAYED) {
            messages.add(msg);
        } else {
            List<String> ids = this.mCache.getMessageIdsForDisplayAggregation(chatId, ImDirection.OUTGOING, Long.valueOf(msg.getDeliveredTimestamp()));
            ids.remove(String.valueOf(msg.getId()));
            if (!ids.isEmpty()) {
                messages.addAll(this.mCache.getMessages(ids));
            }
            messages.add(msg);
            if (messages.size() > 1) {
                messages.sort($$Lambda$ImdnHandler$fRENq26WtFZXuhB2I24m9IDwPa0.INSTANCE);
            }
        }
        return messages;
    }

    static /* synthetic */ int lambda$getMessagesForReceivedImdn$0(MessageBase m1, MessageBase m2) {
        return m1.getId() - m2.getId() < 0 ? -1 : 1;
    }

    /* access modifiers changed from: protected */
    public void onComposingNotificationReceived(ImComposingEvent event) {
        String alias;
        String str = LOG_TAG;
        Log.i(str, "onComposingNotificationReceived: " + event);
        ImSession session = this.mCache.getImSession(event.mChatId);
        if (session == null) {
            Log.e(LOG_TAG, "onComposingNotificationReceived: Session not found.");
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        ImsUri remoteUri = this.mImModule.normalizeUri(ImsUri.parse(event.mUri));
        boolean isGroupChat = session.isGroupChat();
        if (isGroupChat || !RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_MSG) || !BlockedNumberUtil.isBlockedNumber(this.mContext, remoteUri.getMsisdn())) {
            session.receiveComposingNotification(event);
            if (!isGroupChat) {
                this.mImSessionProcessor.setLegacyLatching(session.getRemoteUri(), false, session.getChatData().getOwnIMSI());
            }
            if (this.mImModule.getImConfig(phoneId).getUserAliasEnabled()) {
                alias = event.mUserAlias;
            } else {
                alias = "";
            }
            for (IChatEventListener onComposingNotificationReceived : this.mImSessionProcessor.mChatEventListeners) {
                onComposingNotificationReceived.onComposingNotificationReceived(event.mChatId, session.isGroupChat(), remoteUri, alias, event.mIsComposing, event.mInterval);
            }
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "Incoming Composing Noti from blocked number (" + remoteUri.getMsisdn());
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent event) {
        String str = LOG_TAG;
        Log.i(str, "onSendImdnFailed: " + event);
        MessageBase msg = this.mCache.getMessage(event.mImdnId, ImDirection.INCOMING);
        if (msg != null) {
            ImSession session = this.mCache.getImSession(event.mChatId);
            if (session != null) {
                session.onSendImdnFailed(event, msg);
            } else {
                Log.e(LOG_TAG, "onSendImdnFailed: Session not found.");
            }
        } else {
            Log.e(LOG_TAG, "onSendImdnFailed: Message not found.");
        }
    }
}
