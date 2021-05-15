package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImSessionStartingState extends ImSessionStateBase {
    private static final String LOG_TAG = "StartingState";

    ImSessionStartingState(int phoneId, ImSession imSession) {
        super(phoneId, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState enter. " + this.mImSession.getChatId());
        this.mImSession.mListener.onChatStatusUpdate(this.mImSession, ImSession.SessionState.STARTING);
        this.mImSession.mClosedReason = ImSessionClosedReason.NONE;
        if (!this.mImSession.isVoluntaryDeparture() && !this.mImSession.isAutoRejoinSession()) {
            this.mImSession.getChatData().updateState(ChatData.State.INACTIVE);
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processMessagingEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 3001) {
            onSendMessage((MessageBase) msg.obj);
            return true;
        } else if (i != 3010) {
            return false;
        } else {
            onSendDeliveredNotification((MessageBase) msg.obj);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processGroupChatManagementEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (!(i == 2001 || i == 2008 || i == 2010 || i == 2012 || i == 2014)) {
            if (i == 2005) {
                this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) msg.obj);
                return true;
            } else if (i != 2006) {
                return false;
            }
        }
        msg.arg1 = 1;
        this.mImSession.deferMessage(msg);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message msg) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processSessionConnectionEvent: " + msg.what + " ChatId: " + this.mImSession.getChatId());
        int i = msg.what;
        if (i == 1002) {
            onStartSessionDone(msg);
            return true;
        } else if (i == 1007) {
            onAcceptSessionDone(msg);
            return true;
        } else if (i == 1016) {
            onStartSessionProvisionalResponse((StartImSessionResult) ((AsyncResult) msg.obj).result);
            return true;
        } else if (i == 1004) {
            this.mImSession.onEstablishmentTimeOut(msg.obj);
            return true;
        } else if (i == 1005) {
            onProcessIncomingSession((ImIncomingSessionEvent) msg.obj);
            return true;
        } else if (i == 1012) {
            return onCloseAllSession(msg);
        } else {
            if (i != 1013) {
                return false;
            }
            this.mImSession.mClosedState.onCloseSessionDone(msg);
            return true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x00be  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00e4  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0180  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0183  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x01e6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStartSession(com.sec.internal.ims.servicemodules.im.MessageBase r44, com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.StartingReason r45, boolean r46) {
        /*
            r43 = this;
            r0 = r43
            r1 = r44
            r2 = r45
            r15 = r46
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r4 = "onStartSession"
            r3.logi(r4)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.Set r14 = r3.getParticipantsUri()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r3 = r3.isGroupChat()
            r13 = 1
            if (r3 != 0) goto L_0x002c
            int r3 = r14.size()
            if (r3 <= r13) goto L_0x002c
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r4 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT
            r3.updateChatType(r4)
        L_0x002c:
            int r3 = r14.size()
            r0.dumpOnStartSession(r3, r2, r15)
            java.util.List r12 = r0.generateReceivers(r14)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r11 = r0.preCheckToStartSession(r1, r14, r12)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r3 = r11.getStatusCode()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r4 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            if (r3 == r4) goto L_0x0047
            r0.handleStartSessionFailure(r1, r11)
            return
        L_0x0047:
            r3 = 0
            if (r1 == 0) goto L_0x0068
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.String r5 = r44.getBody()
            boolean r4 = r4.isFirstMessageInStart(r5)
            if (r4 == 0) goto L_0x0061
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r4.setSessionTimeoutThreshold(r1)
            com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams r3 = r43.createFirstMessageParams(r44)
            r10 = r3
            goto L_0x0069
        L_0x0061:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.util.List<com.sec.internal.ims.servicemodules.im.MessageBase> r4 = r4.mCurrentMessages
            r4.add(r1)
        L_0x0068:
            r10 = r3
        L_0x0069:
            r43.generateSessionIds()
            java.lang.String r9 = com.sec.internal.ims.util.StringIdGenerator.generateUuid()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onStartSession, sendMessage IM. temporary sessionKey : "
            r4.append(r5)
            r4.append(r9)
            java.lang.String r5 = ", msgParams : "
            r4.append(r5)
            r4.append(r10)
            java.lang.String r4 = r4.toString()
            r3.logi(r4)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r4 = 1002(0x3ea, float:1.404E-42)
            android.os.Message r28 = r3.obtainMessage((int) r4, (java.lang.Object) r1)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r4 = 1017(0x3f9, float:1.425E-42)
            android.os.Message r29 = r3.obtainMessage((int) r4, (java.lang.Object) r9)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r4 = 1016(0x3f8, float:1.424E-42)
            android.os.Message r30 = r3.obtainMessage(r4)
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo r3 = new com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r18 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.INITIAL
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r19 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            boolean r4 = r4.isRejoinable()
            if (r4 == 0) goto L_0x00be
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            com.sec.ims.util.ImsUri r4 = r4.getSessionUri()
            r20 = r4
            goto L_0x00c0
        L_0x00be:
            r20 = 0
        L_0x00c0:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.String r21 = r4.getContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.String r22 = r4.getConversationId()
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.String r23 = r4.getInReplyToContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.String r24 = r4.getSdpContentType()
            r16 = r3
            r17 = r9
            r16.<init>(r17, r18, r19, r20, r21, r22, r23, r24)
            r7 = r3
            r7.mIsTryToLeave = r15
            if (r15 == 0) goto L_0x00ec
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.ims.servicemodules.im.ImSessionClosedState r3 = r3.mClosedState
            com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason.VOLUNTARILY
            r3.mStopReason = r4
        L_0x00ec:
            r7.mStartingReason = r2
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.addImSessionInfo(r7)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.updateSessionInfo(r7)
            r43.checkIconUpdateRequired()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r3 = r3.getSdpContentType()
            if (r3 != 0) goto L_0x0112
            if (r1 == 0) goto L_0x0112
            java.lang.String r4 = r44.getContentType()
            if (r4 == 0) goto L_0x0112
            java.lang.String r3 = r44.getContentType()
            r31 = r3
            goto L_0x0114
        L_0x0112:
            r31 = r3
        L_0x0114:
            com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams r32 = new com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r4 = r3.getChatId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r5 = r3.getSubject()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r16 = r3.getContributionId()
            r17 = 0
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r18 = r3.getUserAlias()
            com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams$ServiceType r19 = com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams.ServiceType.NORMAL
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r20 = r3.isGroupChat()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r21 = r3.getConversationId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r22 = r3.getInReplyToContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r23 = r3.isRejoinable()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = r3.getChatType()
            boolean r24 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.isClosedGroupChat(r3)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r25 = r3.getServiceId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.List<java.lang.String> r6 = r3.mAcceptTypes
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.List<java.lang.String> r3 = r3.mAcceptWrappedTypes
            com.sec.internal.ims.servicemodules.im.ImSession r8 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatData r8 = r8.getChatData()
            java.lang.String r33 = r8.getOwnIMSI()
            com.sec.internal.ims.servicemodules.im.ImSession r8 = r0.mImSession
            boolean r8 = r8.isGroupChat()
            if (r8 != 0) goto L_0x0183
            com.sec.internal.ims.servicemodules.im.ImSession r8 = r0.mImSession
            java.util.Set r8 = r8.getParticipantsUri()
            boolean r8 = com.sec.internal.ims.util.ChatbotUriUtil.hasChatbotUri(r8)
            if (r8 == 0) goto L_0x0183
            r34 = r13
            goto L_0x0186
        L_0x0183:
            r8 = 0
            r34 = r8
        L_0x0186:
            com.sec.internal.ims.servicemodules.im.ImSession r8 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r27 = r8.getChatMode()
            r35 = r3
            r3 = r32
            r36 = r6
            r6 = r12
            r37 = r7
            r7 = r16
            r8 = r17
            r38 = r9
            r9 = r18
            r39 = r10
            r10 = r19
            r40 = r11
            r11 = r20
            r41 = r12
            r12 = r31
            r13 = r28
            r42 = r14
            r14 = r30
            r15 = r29
            r16 = r39
            r17 = r21
            r18 = r22
            r19 = r23
            r20 = r24
            r21 = r46
            r22 = r25
            r23 = r36
            r24 = r35
            r25 = r33
            r26 = r34
            r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27)
            if (r1 == 0) goto L_0x01dd
            java.lang.String r4 = r1.mContentType
            if (r4 == 0) goto L_0x01dd
            java.lang.String r4 = r1.mContentType
            java.lang.String r5 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r4 = r4.contains(r5)
            if (r4 == 0) goto L_0x01dd
            r4 = 1
            r3.mIsGeolocationPush = r4
        L_0x01dd:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r4 = r4.mImsService
            r4.startImSession(r3)
            if (r39 == 0) goto L_0x01eb
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r4.onMessageSending(r1)
        L_0x01eb:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            boolean r4 = r4.isRejoinable()
            if (r4 != 0) goto L_0x0200
            if (r46 != 0) goto L_0x0200
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$StartingReason r4 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.StartingReason.RESTARTING
            if (r2 == r4) goto L_0x0200
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r5 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED
            r4.updateParticipantsStatus(r5)
        L_0x0200:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r5 = 0
            r4.mClosedEvent = r5
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r4.transitionToProperState()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionStartingState.onStartSession(com.sec.internal.ims.servicemodules.im.MessageBase, com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$StartingReason, boolean):void");
    }

    private Set<ImsUri> getParticipantsNetworkPreferredUri(Set<ImsUri> list) {
        ICapabilityDiscoveryModule discoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        Set<ImsUri> listToUse = new HashSet<>();
        for (ImsUri uri : list) {
            if (discoveryModule == null || discoveryModule.getCapabilitiesCache() == null) {
                listToUse.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri));
            } else {
                Capabilities caps = discoveryModule.getCapabilitiesCache().get(uri);
                if (caps == null) {
                    listToUse.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri));
                } else {
                    String domain = null;
                    Iterator<ImsUri> it = caps.getPAssertedId().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        ImsUri remoteUri = it.next();
                        if (remoteUri.getUriType() == ImsUri.UriType.SIP_URI) {
                            domain = remoteUri.getHost();
                            break;
                        }
                    }
                    listToUse.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, uri, domain));
                }
            }
        }
        return listToUse;
    }

    public void onStartSessionDone(Message msg) {
        AsyncResult ar = (AsyncResult) msg.obj;
        StartImSessionResult startResult = (StartImSessionResult) ar.result;
        ImError error = startResult.mResult.getImError();
        this.mImSession.mRetryTimer = startResult.mRetryTimer;
        List<String> dumps = new ArrayList<>();
        dumps.add(String.valueOf(error.ordinal()));
        dumps.add(startResult.toCriticalLog());
        ImsUtil.listToDumpFormat(LogClass.IM_START_SESSION_DONE, this.mPhoneId, this.mImSession.getChatId(), dumps);
        ImSession imSession = this.mImSession;
        imSession.logi("onStartSessionDone : " + startResult);
        ImSessionInfo info = this.mImSession.getImSessionInfo(startResult.mRawHandle);
        if (info == null) {
            ImSession imSession2 = this.mImSession;
            imSession2.loge("onStartSessionDone unknown rawHandle : " + startResult.mRawHandle);
        } else if (error == ImError.SUCCESS) {
            onStartSessionDoneSuccess(startResult, info);
        } else {
            onStartSessionDoneFailure(startResult, info, error, (MessageBase) ar.userObj);
        }
    }

    private boolean shouldRestartSessionWithNewID(ImError error) {
        if (!this.mImSession.isGroupChat()) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()];
        if ((i == 1 || i == 2) && this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
            return true;
        }
        return false;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionStartingState$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        static {
            int[] iArr = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr;
            try {
                iArr[ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.FORBIDDEN_VERSION_NOT_SUPPORTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private void restartSession(boolean isInviteForBye, boolean withNewID) {
        this.mImSession.setSessionUri((ImsUri) null);
        ImSessionInfo.StartingReason reason = ImSessionInfo.StartingReason.RESTARTING;
        if (withNewID) {
            if (this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
            } else {
                String conversationId = StringIdGenerator.generateConversationId();
                this.mImSession.setConversationId(conversationId);
                this.mImSession.setContributionId(this.mImSession.isGroupChat() ? conversationId : StringIdGenerator.generateContributionId());
            }
            reason = ImSessionInfo.StartingReason.RESTARTING_WITH_NEW_ID;
        }
        Set<ImParticipant> removedParticipants = new HashSet<>();
        for (ImsUri uri : this.mImSession.mGetter.getOwnUris(SimUtil.getSimSlotPriority())) {
            ImParticipant p = this.mImSession.getParticipant(uri);
            if (p != null) {
                p.setStatus(ImParticipant.Status.DECLINED);
                removedParticipants.add(p);
            }
        }
        if (!removedParticipants.isEmpty()) {
            Log.e(LOG_TAG, "restartSession: remove own uris from participants list");
            this.mImSession.mListener.onParticipantsDeleted(this.mImSession, removedParticipants);
        }
        onStartSession((MessageBase) null, reason, isInviteForBye);
    }

    private void onSendMessage(MessageBase message) {
        ImSessionInfo info = this.mImSession.getLatestActiveImSessionInfo();
        if (!this.mImSession.isFirstMessageInStart(message.getBody()) || !this.mImSession.mCurrentMessages.isEmpty() || (info != null && (info.mState != ImSessionInfo.ImSessionState.STARTING || info.mLastProvisionalResponse == null))) {
            this.mImSession.logi("Starting Session, send message after session establishment");
            this.mImSession.mCurrentMessages.add(message);
        } else if (!this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_INVITE_AFTER_18X)) {
            onStartSession(message, ImSessionInfo.StartingReason.NORMAL, false);
        } else if (info == null || !(info.mLastProvisionalResponse == ImError.RINGING || info.mLastProvisionalResponse == ImError.CALL_IS_BEING_FORWARDED || info.mLastProvisionalResponse == ImError.SESSION_PROGRESS)) {
            this.mImSession.mCurrentMessages.add(message);
        } else {
            onStartSession(message, ImSessionInfo.StartingReason.NORMAL, false);
        }
    }

    private void onSendDeliveredNotification(MessageBase message) {
        ImSessionInfo info = this.mImSession.getImSessionInfoByMessageId(message.getId());
        Object rawHandle = null;
        if (info != null && info.isSnFSession() && info.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
            rawHandle = info.mRawHandle;
        }
        message.sendDeliveredNotification(rawHandle, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
    }

    private void onProcessIncomingSession(ImIncomingSessionEvent event) {
        ImSession imSession = this.mImSession;
        imSession.logi("race-condition : mRawHandle=" + event.mRawHandle);
        IMSLog.c(LogClass.IM_INCOMING_SESSION_ERR, " race : " + event.mRawHandle);
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.logi("Explicit departure is in progress. Reject the incoming invite");
            this.mImSession.leaveSessionWithReject(event.mRawHandle);
            return;
        }
        if (!this.mImSession.isGroupChat()) {
            if (this.mImSession.getDirection() == ImDirection.OUTGOING) {
                this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), event.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
                this.mImSession.onIncomingSessionProcessed(event.mReceivedMessage, false);
                return;
            }
            this.mImSession.mClosedState.handleCloseSession(this.mImSession.getRawHandle(), ImSessionStopReason.INVOLUNTARILY);
        }
        ImSessionInfo info = this.mImSession.addImSessionInfo(event, ImSessionInfo.ImSessionState.ACCEPTING);
        this.mImSession.updateSessionInfo(info);
        this.mImSession.handleAcceptSession(info);
        this.mImSession.onIncomingSessionProcessed(event.mReceivedMessage, false);
        this.mImSession.transitionToProperState();
        this.mImSession.releaseWakeLock(event.mRawHandle);
    }

    private boolean onCloseAllSession(Message msg) {
        if (!this.mImSession.isVoluntaryDeparture()) {
            return false;
        }
        this.mImSession.logi("Voluntary departure in StartingState. DeferMessage.");
        this.mImSession.deferMessage(msg);
        return true;
    }

    private void onStartSessionProvisionalResponse(StartImSessionResult startResult) {
        ImSessionInfo info = this.mImSession.getImSessionInfo(startResult.mRawHandle);
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_PROVISIONAL_RESPONSE : response=" + startResult);
        ImError error = startResult.mResult.getImError();
        if (info != null) {
            if (!this.mImSession.mCurrentMessages.isEmpty() && this.mImSession.isFirstMessageInStart(this.mImSession.mCurrentMessages.get(0).getBody()) && info.equals(this.mImSession.getLatestActiveImSessionInfo()) && (info.mLastProvisionalResponse == null || info.mLastProvisionalResponse == ImError.TRYING)) {
                if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_INVITE_AFTER_18X)) {
                    if (error == ImError.RINGING || error == ImError.CALL_IS_BEING_FORWARDED || error == ImError.SESSION_PROGRESS) {
                        onStartSession(this.mImSession.mCurrentMessages.remove(0), ImSessionInfo.StartingReason.NORMAL, false);
                    }
                } else if (info.mLastProvisionalResponse == null) {
                    onStartSession(this.mImSession.mCurrentMessages.remove(0), ImSessionInfo.StartingReason.NORMAL, false);
                }
            }
            info.mLastProvisionalResponse = error;
        }
    }

    private void dumpOnStartSession(int participantsSize, ImSessionInfo.StartingReason startingReason, boolean isInviteForBye) {
        List<String> dumps = new ArrayList<>();
        dumps.add(String.valueOf(participantsSize));
        String str = "1";
        dumps.add(this.mImSession.isGroupChat() ? str : "0");
        dumps.add(String.valueOf(startingReason.ordinal()));
        if (!isInviteForBye) {
            str = "0";
        }
        dumps.add(str);
        ImsUtil.listToDumpFormat(LogClass.IM_START_SESSION, this.mPhoneId, this.mImSession.getChatId(), dumps);
    }

    private IMnoStrategy.StrategyResponse preCheckToStartSession(MessageBase imMsg, Set<ImsUri> participants, List<ImsUri> receivers) {
        IMnoStrategy.StrategyResponse strategyResponse;
        if (this.mImSession.isGroupChat() && !this.mImSession.mConfig.getGroupChatEnabled()) {
            strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).handleImFailure(ImError.GROUPCHAT_DISABLED, this.mImSession.getChatType());
        } else if (this.mImSession.getChatType() != ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT || !this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.ALLOW_ONLY_OPENGROUPCHAT)) {
            strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).checkCapability(participants, Capabilities.FEATURE_IM_SERVICE, this.mImSession.getChatType(), this.mImSession.isBroadcastMsg(imMsg));
        } else if (this.mImSession.mConfig.getSlmAuth() == ImConstants.SlmAuth.ENABLED) {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        } else {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imMsg != null && imMsg.getType() == ImConstants.Type.LOCATION && strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            this.mImSession.logi("onStartSession : GLS fallback to legacy");
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (receivers.isEmpty()) {
            this.mImSession.loge("onStartSession : Invalid receiver");
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (!this.mImSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(this.mImSession.getParticipantsUri()) && (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY)) {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
        }
        ImSession imSession = this.mImSession;
        imSession.logi("onStartSession: statusCode=" + strategyResponse.getStatusCode());
        return strategyResponse;
    }

    private void handleStartSessionFailure(MessageBase imMsg, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            if (imMsg == null) {
                this.mImSession.transitionToProperState();
            } else if (imMsg instanceof FtHttpOutgoingMessage) {
                this.mImSession.handleUploadedFileFallback((FtHttpOutgoingMessage) imMsg);
            } else {
                this.mImSession.logi("onStartSession, sendMessage SLM");
                this.mImSession.sendMessage(this.mImSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) imMsg));
            }
        } else if (imMsg != null) {
            this.mImSession.logi("onStartSession, display error or sendMessage error");
            imMsg.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), strategyResponse);
        } else {
            this.mImSession.transitionToProperState();
        }
    }

    private void generateSessionIds() {
        if (this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            if (this.mImSession.getDirection() == ImDirection.INCOMING) {
                this.mImSession.setInReplyToContributionId(this.mImSession.getContributionId());
                this.mImSession.setDirection(ImDirection.OUTGOING);
            }
            if (!this.mImSession.isGroupChat()) {
                this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
            }
            if (TextUtils.isEmpty(this.mImSession.getConversationId())) {
                this.mImSession.setConversationId(StringIdGenerator.generateConversationId());
            }
            if (TextUtils.isEmpty(this.mImSession.getContributionId())) {
                this.mImSession.setContributionId(this.mImSession.isGroupChat() ? this.mImSession.getConversationId() : StringIdGenerator.generateContributionId());
            }
        } else if (TextUtils.isEmpty(this.mImSession.getContributionId())) {
            this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
        }
    }

    private List<ImsUri> generateReceivers(Set<ImsUri> participants) {
        List<ImsUri> receivers = new ArrayList<>();
        if (this.mImSession.isRejoinable()) {
            receivers.add(this.mImSession.getSessionUri());
        } else {
            receivers.addAll(getParticipantsNetworkPreferredUri(participants));
        }
        if (this.mImSession.mNewContactValueUri != null) {
            receivers.clear();
            receivers.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mImSession.mNewContactValueUri.getMsisdn(), (String) null));
            this.mImSession.mNewContactValueUri = null;
        }
        if (this.mImSession.mSwapUriType) {
            Set<ImsUri> networkParticipants = this.mImSession.mUriGenerator.swapUriType(receivers);
            receivers.clear();
            receivers.addAll(networkParticipants);
            this.mImSession.mSwapUriType = false;
        }
        return receivers;
    }

    private void checkIconUpdateRequired() {
        this.mImSession.getChatData().setIconUpdatedRequiredOnSessionEstablished(this.mImSession.isGroupChat() && this.mImSession.getDirection() == ImDirection.OUTGOING && !this.mImSession.isRejoinable());
    }

    private void onStartSessionDoneSuccess(StartImSessionResult startResult, ImSessionInfo info) {
        info.mState = ImSessionInfo.ImSessionState.STARTED;
        info.mSessionUri = startResult.mSessionUri;
        if (this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
            this.mImSession.updateSessionInfo(info);
        }
        this.mImSession.setNetworkFallbackMech(startResult.mIsMsgFallbackSupported, startResult.mIsMsgRevokeSupported);
        if (!this.mImSession.isMsgRevocationSupported() && !this.mImSession.getNeedToRevokeMessages().isEmpty()) {
            Map<String, Integer> needToRevokeMessages = this.mImSession.getNeedToRevokeMessages();
            for (String imdnId : needToRevokeMessages.keySet()) {
                MessageBase message = this.mImSession.mGetter.getMessage(imdnId, ImDirection.OUTGOING);
                if (message != null) {
                    message.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                } else {
                    ImSession imSession = this.mImSession;
                    imSession.loge("message from mGetter is null. imdnId : " + imdnId);
                }
            }
            this.mImSession.removeMsgFromListForRevoke((Collection<String>) needToRevokeMessages.keySet());
        }
        this.mImSession.updateIsChatbotRole(startResult.mIsChatbotRole);
        startSessionEstablishmentTimer(info.mRawHandle);
        if (this.mImSession.mConfig.getUserAliasEnabled() && !this.mImSession.isGroupChat()) {
            this.mImSession.updateParticipantAlias(startResult.mRemoteUserDisplayName, this.mImSession.getParticipants().iterator().next());
        }
    }

    private void onStartSessionDoneFailure(StartImSessionResult startResult, ImSessionInfo info, ImError error, MessageBase imMsg) {
        this.mImSession.getHandler().removeMessages(1004, info.mRawHandle);
        this.mImSession.removeImSessionInfo(info);
        if (this.mImSession.hasActiveImSessionInfo()) {
            this.mImSession.logi("onStartSessionDone : race condition, waiting events of another session");
        } else if (info.mStartingReason == ImSessionInfo.StartingReason.AUTOMATIC_REJOINING) {
            this.mImSession.loge("onStartSessionDone : automatic rejoining was unsuccessful. Ignore the startResult");
            if (this.mImSession.getRcsStrategy(this.mPhoneId).needStopAutoRejoin(error)) {
                if (info.mIsTryToLeave) {
                    this.mImSession.setSessionUri((ImsUri) null);
                } else {
                    this.mImSession.getChatData().updateState(ChatData.State.CLOSED_BY_USER);
                }
            }
            this.mImSession.transitionToProperState();
        } else if (this.mImSession.isRejoinable() && RcsPolicyManager.getRcsStrategy(this.mPhoneId).shouldRestartSession(error)) {
            this.mImSession.loge("onStartSessionDone : Rejoining groupchat was unsuccessful. Restart groupchat");
            restartSession(info.mIsTryToLeave, false);
        } else if (!shouldRestartSessionWithNewID(error) || info.mIsTryToLeave) {
            if (!this.mImSession.mCurrentMessages.isEmpty()) {
                if (imMsg == null || !this.mImSession.isFirstMessageInStart(imMsg.getBody()) || error != ImError.BUSY_HERE) {
                    this.mImSession.failCurrentMessages(startResult.mRawHandle, startResult.mResult, startResult.mAllowedMethods);
                } else {
                    this.mImSession.logi("onStartSessionDone : handle 486 response as SUCCESS for the message in INVITE.");
                    MessageBase nextMsg = null;
                    if (this.mImSession.isFirstMessageInStart(this.mImSession.mCurrentMessages.get(0).getBody())) {
                        nextMsg = this.mImSession.mCurrentMessages.remove(0);
                    }
                    onStartSession(nextMsg, ImSessionInfo.StartingReason.NORMAL, false);
                }
            }
            if (error == ImError.FORBIDDEN_MAX_GROUP_NUMBER) {
                this.mImSession.mClosedReason = ImSessionClosedReason.MAX_GROUP_NUMBER_REACHED;
            } else if (error == ImError.GONE && this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
                this.mImSession.mClosedReason = ImSessionClosedReason.GROUP_CHAT_DISMISSED;
            } else if (error != ImError.FORBIDDEN_RESTART_GC_CLOSED || !this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_CHAT_CLOSE_BY_SERVER)) {
                this.mImSession.mClosedReason = RcsPolicyManager.getRcsStrategy(this.mPhoneId).handleSessionFailure(error);
            } else {
                this.mImSession.logi("onStartSessionDone : Chat Close by Server ");
                if (info.mIsTryToLeave) {
                    this.mImSession.mClosedReason = ImSessionClosedReason.CLOSED_BY_LOCAL;
                    this.mImSession.mListener.onChatDeparted(this.mImSession);
                } else {
                    this.mImSession.mClosedReason = ImSessionClosedReason.LEFT_BY_SERVER;
                }
            }
            if (this.mImSession.getRcsStrategy(this.mPhoneId).isNeedToReportToRegiGvn(error)) {
                this.mImSession.mListener.onImErrorReport(error, this.mPhoneId);
            }
            this.mImSession.mClosedEvent = new ImSessionClosedEvent(startResult.mRawHandle, this.mImSession.getChatId(), startResult.mResult);
            this.mImSession.transitionToProperState();
        } else {
            this.mImSession.loge("onStartSessionDone : User is not authorized to rejoin the group. start new chat");
            restartSession(false, true);
        }
    }

    /* access modifiers changed from: protected */
    public void onAcceptSessionDone(Message msg) {
        StartImSessionResult acceptResult = (StartImSessionResult) ((AsyncResult) msg.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("onAcceptSessionDone : " + acceptResult);
        if (acceptResult.mResult.getImError() == ImError.SUCCESS) {
            startSessionEstablishmentTimer(acceptResult.mRawHandle);
        } else {
            this.mImSession.removeImSessionInfo(acceptResult.mRawHandle);
            if (!this.mImSession.hasActiveImSessionInfo()) {
                this.mImSession.failCurrentMessages(acceptResult, acceptResult.mResult);
            }
            this.mImSession.transitionToProperState();
        }
        this.mImSession.releaseWakeLock(acceptResult.mRawHandle);
    }

    /* access modifiers changed from: protected */
    public void startSessionEstablishmentTimer(Object rawHandle) {
        if (RcsPolicyManager.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) > 0 && this.mImSession.getChatType() != ChatData.ChatType.REGULAR_GROUP_CHAT) {
            ImSession imSession = this.mImSession;
            imSession.logi("Stack response timer starts" + toString());
            this.mImSession.getHandler().removeMessages(1004, rawHandle);
            this.mImSession.sendMessageDelayed(this.mImSession.obtainMessage(1004, rawHandle), ((long) RcsPolicyManager.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
        }
    }

    private SendMessageParams createFirstMessageParams(MessageBase imMsg) {
        ImSession imSession = this.mImSession;
        imSession.logi("initializing SendMessageParams: " + this.mImSession.mConfig.isFirstMsgInvite());
        Set<NotificationStatus> updatedNotification = imMsg.getDispositionNotification();
        if ((ImsProfile.isRcsUpProfile(this.mImSession.mConfig.getRcsProfile()) && this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) || this.mImSession.isMsgFallbackSupported()) {
            updatedNotification.add(NotificationStatus.INTERWORKING_SMS);
        }
        String body = imMsg.getBody();
        String userAlias = this.mImSession.getUserAlias();
        String contentType = imMsg.getContentType();
        String imdnId = imMsg.getImdnId();
        Date date = r10;
        Date date2 = new Date();
        return new SendMessageParams((Object) null, body, userAlias, contentType, imdnId, date, updatedNotification, imMsg.getDeviceName(), imMsg.getReliableMessage(), this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.EXTRA_FT_FOR_NS), imMsg.getXmsMessage(), (Set<ImsUri>) null, this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) imMsg), imMsg.getMaapTrafficType(), imMsg.getReferenceId(), imMsg.getReferenceType(), imMsg.getReferenceValue());
    }
}
