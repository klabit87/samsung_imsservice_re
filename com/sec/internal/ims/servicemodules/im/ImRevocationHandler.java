package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.MessageRevokeResponse;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImRevocationHandler extends Handler {
    private static final String LOG_TAG = ImRevocationHandler.class.getSimpleName();
    private final ImCache mCache;
    private final Context mContext;
    private final ImModule mImModule;
    private final ImSessionProcessor mImSessionProcessor;
    private final PhoneIdKeyMap<Boolean> mIsReconnectGuardTimersRunning;
    private final Map<String, String> mRevokingMessages = new HashMap();

    public ImRevocationHandler(Context context, ImModule imModule, ImCache imCache, ImSessionProcessor imSessionProcessor) {
        this.mContext = context;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImSessionProcessor = imSessionProcessor;
        this.mIsReconnectGuardTimersRunning = new PhoneIdKeyMap<>(SimManagerFactory.getAllSimManagers().size(), false);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == 26) {
            handleEventReconnectGuardTimerExpired(((Integer) msg.obj).intValue());
        }
    }

    /* access modifiers changed from: protected */
    public void setLegacyLatching(ImsUri uri, boolean b, String imsi) {
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        if (this.mImModule.getImConfig(phoneId).getLegacyLatching() && ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().setLegacyLatching(uri, b, phoneId)) {
            String str = LOG_TAG;
            Log.i(str, "setLegacyLatching: Uri = " + IMSLog.checker(uri) + ", bool = " + b);
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevokeTimerExpired(String chatId, Collection<String> imdnIds, String imsi) {
        int phoneId = this.mImModule.getPhoneIdByIMSI(imsi);
        ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_TIMEOUT, phoneId, chatId);
        if (!this.mImModule.isRegistered(phoneId) || isReconnectGuardTimersRunning(phoneId)) {
            Log.e(LOG_TAG, "onMessageRevokeTimerExpired: Deregi state or ReconnectGuardTimerRunning");
            return;
        }
        for (IChatEventListener listener : this.mImSessionProcessor.getChatEventListeners()) {
            listener.onMessageRevokeTimerExpired(chatId, imdnIds);
        }
    }

    /* access modifiers changed from: protected */
    public void requestMessageRevocation(String chatId, List<String> imdnIds, boolean userSelectResult, int userSelectType) {
        post(new Runnable(chatId, userSelectResult, userSelectType, imdnIds) {
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ List f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                ImRevocationHandler.this.lambda$requestMessageRevocation$0$ImRevocationHandler(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$requestMessageRevocation$0$ImRevocationHandler(String chatId, boolean userSelectResult, int userSelectType, List imdnIds) {
        ImSession session = this.mCache.getImSession(chatId);
        if (session == null) {
            Log.e(LOG_TAG, "requestMessageRevocation(): Session not found in the cache.");
            return;
        }
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getOwnImsi());
        if (this.mImModule.isRegistered(phoneId)) {
            List<String> dumps = new ArrayList<>();
            dumps.add(userSelectResult ? "1" : "0");
            dumps.add(String.valueOf(userSelectType));
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_REQ, phoneId, chatId, dumps);
            if (imdnIds != null) {
                session.messageRevocationRequest(imdnIds, userSelectResult, userSelectType);
            } else {
                session.messageRevocationRequestAll(userSelectResult, userSelectType);
            }
        } else {
            Log.e(LOG_TAG, "requestMessageRevocation(): Deregi state");
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevocationDone(ImConstants.RevocationStatus status, Collection<MessageBase> messages, ImSession session) {
        String str = LOG_TAG;
        Log.i(str, "onMessageRevocationDone() : Status : " + status);
        Collection<String> imdnIds = new ArrayList<>();
        for (MessageBase msg : messages) {
            msg.updateRevocationStatus(status);
            imdnIds.add(msg.getImdnId());
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus[status.ordinal()];
        if (i == 1 || i == 2) {
            IMnoStrategy.StrategyResponse strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS);
            for (MessageBase msg2 : messages) {
                if (msg2 instanceof ImMessage) {
                    for (IMessageEventListener listener : this.mImSessionProcessor.getMessageEventListener(msg2.getType())) {
                        listener.onMessageSendingFailed(msg2, strategyResponse, (Result) null);
                    }
                } else if (msg2 instanceof FtMessage) {
                    for (IFtEventListener listener2 : this.mImSessionProcessor.getFtEventListener(msg2.getType())) {
                        listener2.onMessageSendingFailed(msg2, strategyResponse, (Result) null);
                    }
                }
            }
        }
        for (MessageBase msg3 : messages) {
            this.mCache.removeFromPendingList(msg3.getId());
        }
        session.removeMsgFromListForRevoke(imdnIds);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImRevocationHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus;

        static {
            int[] iArr = new int[ImConstants.RevocationStatus.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus = iArr;
            try {
                iArr[ImConstants.RevocationStatus.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus[ImConstants.RevocationStatus.SUCCESS.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus[ImConstants.RevocationStatus.FAILED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addToRevokingMessages(String imdnId, String chatId) {
        this.mRevokingMessages.put(imdnId, chatId);
    }

    /* access modifiers changed from: protected */
    public void removeFromRevokingMessages(Collection<String> imdnIds) {
        this.mRevokingMessages.keySet().removeAll(imdnIds);
    }

    /* access modifiers changed from: protected */
    public void onSendMessageRevokeRequestDone(MessageRevokeResponse response) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageRevokeRequestDone(): " + response);
        ImSession session = this.mCache.getImSession(this.mRevokingMessages.get(response.mImdnId));
        if (session == null) {
            Log.e(LOG_TAG, "onSendMessageRevokeRequestDone(): Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSendMessageRevokeRequestDone: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId=" + response.mImdnId);
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        if (this.mImModule.getImConfig(phoneId).isCfsTrigger()) {
            List<String> dumps = new ArrayList<>();
            dumps.add(ImsUtil.hideInfo(response.mImdnId, 4));
            dumps.add(response.mResult ? "1" : "0");
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_REQ_RES, phoneId, session.getChatId(), dumps);
            MessageBase message = this.mCache.getMessage(session.getNeedToRevokeMessages().get(response.mImdnId).intValue());
            if (response.mResult) {
                message.updateRevocationStatus(ImConstants.RevocationStatus.SENT);
                session.startMsgRevokeOperationTimer(message.getImdnId());
                return;
            }
            Collection<MessageBase> messages = new ArrayList<>();
            messages.add(message);
            onMessageRevocationDone(ImConstants.RevocationStatus.NONE, messages, session);
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevokeResponseReceived(MessageRevokeResponse response) {
        String str = LOG_TAG;
        Log.i(str, "onMessageRevokeResponseReceived(): " + response);
        ImSession session = this.mCache.getImSession(this.mRevokingMessages.remove(response.mImdnId));
        if (session == null) {
            Log.e(LOG_TAG, "onSendMessageRevokeRequestDone(): Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onMessageRevokeResponseReceived: chatId=" + session.getChatId() + ", convId=" + session.getConversationId() + ", contId=" + session.getContributionId() + ", imdnId=" + response.mImdnId + ", result=" + response.mResult);
        int phoneId = this.mImModule.getPhoneIdByIMSI(session.getChatData().getOwnIMSI());
        List<String> dumps = new ArrayList<>();
        String str2 = "1";
        dumps.add(this.mImModule.getImConfig(phoneId).isCfsTrigger() ? str2 : "0");
        if (!this.mImModule.getImConfig(phoneId).isCfsTrigger()) {
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_RES, phoneId, session.getChatId(), dumps);
            return;
        }
        dumps.add(ImsUtil.hideInfo(response.mImdnId, 4));
        if (!response.mResult) {
            str2 = "0";
        }
        dumps.add(str2);
        ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_RES, phoneId, session.getChatId(), dumps);
        MessageBase message = this.mCache.getMessage(session.getNeedToRevokeMessages().get(response.mImdnId).intValue());
        if (message == null) {
            Log.e(LOG_TAG, "onSendMessageRevokeRequestDone(): message not found.");
            return;
        }
        session.stopMsgRevokeOperationTimer(message.getImdnId());
        Collection<MessageBase> messages = new ArrayList<>();
        messages.add(message);
        if (response.mResult) {
            onMessageRevocationDone(ImConstants.RevocationStatus.SUCCESS, messages, session);
        } else {
            onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, messages, session);
        }
    }

    /* access modifiers changed from: protected */
    public void stopReconnectGuardTimer(int phoneId) {
        if (this.mIsReconnectGuardTimersRunning.get(phoneId).booleanValue()) {
            this.mIsReconnectGuardTimersRunning.remove(phoneId);
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(obtainMessage(26, Integer.valueOf(phoneId)));
        }
        this.mRevokingMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void handleEventReconnectGuardTimerExpired(int phoneId) {
        Log.i(LOG_TAG, "handleEventReconnectGuardTimerExpired()");
        if (this.mIsReconnectGuardTimersRunning.get(phoneId).booleanValue()) {
            this.mIsReconnectGuardTimersRunning.put(phoneId, false);
            for (ImSession session : this.mCache.getAllImSessions()) {
                if (!session.getNeedToRevokeMessages().isEmpty()) {
                    session.reconnectGuardTimerExpired();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startReconnectGuardTiemer(int phoneId) {
        int reconnectGuardTimer = this.mImModule.getImConfig(phoneId).getReconnectGuardTimer();
        for (ImSession session : this.mCache.getAllImSessions()) {
            if (!this.mIsReconnectGuardTimersRunning.get(phoneId).booleanValue() && reconnectGuardTimer >= 0 && !session.getNeedToRevokeMessages().isEmpty()) {
                String str = LOG_TAG;
                IMSLog.s(str, "mIsReconnectGuardTimersRunning:" + this.mIsReconnectGuardTimersRunning.get(phoneId) + " reconnectGuardTimer:" + reconnectGuardTimer + " list : " + session.getNeedToRevokeMessages().size());
                this.mIsReconnectGuardTimersRunning.put(phoneId, true);
                PreciseAlarmManager.getInstance(this.mContext).sendMessageDelayed(getClass().getSimpleName(), obtainMessage(26, Integer.valueOf(phoneId)), ((long) reconnectGuardTimer) * 1000);
                session.handleSendingStateRevokeMessages();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReconnectGuardTimersRunning(int phoneID) {
        return this.mIsReconnectGuardTimersRunning.get(phoneID).booleanValue();
    }
}
