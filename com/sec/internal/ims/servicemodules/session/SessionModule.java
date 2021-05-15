package com.sec.internal.ims.servicemodules.session;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.diagnosis.RcsHqmAgent;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.ImSessionBuilder;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.options.Intents;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsPhoneStateManager;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionModule extends ServiceModuleBase implements ISessionModule, ImSessionListener, ImMessageListener, IGetter {
    private static final int EVENT_CLOSE_SESSION = 7;
    private static final int EVENT_CONFIGURED = 8;
    private static final int EVENT_INCOMING_MESSAGE = 4;
    private static final int EVENT_INCOMING_SESSION = 1;
    private static final int EVENT_REJECT_SESSION = 6;
    private static final int EVENT_SEND_MESSAGE_FAILED = 5;
    private static final int EVENT_SESSION_CLOSED = 3;
    private static final int EVENT_SESSION_ESTABLISHED = 2;
    public static final String INTENT_FILTER_MESSAGE = "com.gsma.services.rcs.extension.action.NEW_MESSAGING_SESSION";
    public static final String INTENT_FILTER_STREAM = "com.gsma.services.rcs.extension.action.NEW_STREAMING_SESSION";
    /* access modifiers changed from: private */
    public static final String LOG_TAG;
    public static final String MIMETYPE_ALL = "com.gsma.services.rcs/*";
    public static final String MIMETYPE_PREFIX = "com.gsma.services.rcs/";
    public static final String NAME;
    private static final String SERVICE_ID_CALL_COMPOSER = "gsma.callcomposer";
    private static final String SERVICE_ID_POST_CALL = "gsma.callunanswered";
    private static final String SERVICE_ID_SHARED_MAP = "gsma.sharedmap";
    private static final String SERVICE_ID_SHARED_SKETCH = "gsma.sharedsketch";
    private static long mInactivityTimeout = 0;
    private static final String[] sRequiredServices = {"ec"};
    /* access modifiers changed from: private */
    public int callState = 0;
    private boolean canRegisterExt = false;
    private boolean isEnableFailedMedia = false;
    private boolean isWaitingForCloseTagSendingComplete = false;
    private int[] mCallComposerTimerIdle = {MNO.EVR_ESN, MNO.EVR_ESN};
    private List<ImsUri> mCallList = new ArrayList();
    private boolean[] mComposerAuth = {false, false};
    private ImConfig mConfig;
    private final Context mContext;
    private final List<String> mIariTypes = new ArrayList();
    private final IImServiceInterface mImService;
    private final List<IMessagingSessionListener> mListeners = new ArrayList();
    private final Map<String, ImSession.SessionState> mMessagingSessionStates = new HashMap();
    private final Map<String, ImSession> mMessagingSessions = new ConcurrentHashMap();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            String access$000 = SessionModule.LOG_TAG;
            Log.d(access$000, "call state is changed." + state);
            int unused = SessionModule.this.callState = state;
        }
    };
    private final ImsPhoneStateManager mPhoneStateManager;
    private boolean[] mPostCallAuth = {false, false};
    private final Set<String> mRegisteredServices = new ArraySet();
    private int mRegistrationId = -1;
    private final List<String> mServiceIDsFromMetaData = new ArrayList();
    private boolean[] mSharedMapAuth = {false, false};
    private boolean[] mSharedSketchAuth = {false, false};
    private UriGenerator mUriGenerator;

    static {
        Class<SessionModule> cls = SessionModule.class;
        LOG_TAG = cls.getSimpleName();
        NAME = cls.getSimpleName();
    }

    public SessionModule(Looper looper, Context context, IImServiceInterface imsServiceInterface) {
        super(looper);
        this.mContext = context;
        ImsPhoneStateManager imsPhoneStateManager = new ImsPhoneStateManager(this.mContext, 32);
        this.mPhoneStateManager = imsPhoneStateManager;
        imsPhoneStateManager.registerListener(this.mPhoneStateListener);
        this.mUriGenerator = UriGeneratorFactory.getInstance().get();
        this.mConfig = ImsRegistry.getServiceModuleManager().getImModule().getImConfig();
        this.mImService = imsServiceInterface;
        log("SessionModule");
    }

    public int getMaxMsrpLengthForExtensions() {
        return RcsConfigurationHelper.readIntParam(this.mContext, ConfigConstants.ConfigTable.OTHER_EXTENSIONS_MAX_MSRP_SIZE, 0).intValue();
    }

    public long getInactivityTimeout() {
        return mInactivityTimeout;
    }

    public static void setInactivityTimeout(long inactivityTimeout) {
        String str = NAME;
        Log.d(str, "set InactivityTimeout=: " + inactivityTimeout);
        mInactivityTimeout = inactivityTimeout;
    }

    public void onMessageSendResponse(ImMessage msg) {
    }

    public void onMessageReceived(ImMessage msg) {
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
        Log.d(LOG_TAG, "onMessageSendingSucceeded");
        sendRCSMInfoToHQM(msg, "0", (String) null);
        ImSession session = this.mMessagingSessions.get(msg.getChatId());
        if (session == null) {
            Log.e(LOG_TAG, "onMessageSendingSucceeded: Session not found.");
            return;
        }
        for (IMessagingSessionListener listener : this.mListeners) {
            listener.onMessagesFlushed(session);
        }
        if (this.isWaitingForCloseTagSendingComplete) {
            Log.d(LOG_TAG, "onMessageSendingSucceeded : EVENT_CLOSE_SESSION");
            removeMessages(7);
            sendMessage(obtainMessage(7, session.getChatId()));
        }
    }

    public void onMessageSendResponseTimeout(ImMessage msg) {
    }

    /* renamed from: com.sec.internal.ims.servicemodules.session.SessionModule$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type;

        static {
            int[] iArr = new int[Result.Type.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type = iArr;
            try {
                iArr[Result.Type.SIP_ERROR.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.MSRP_ERROR.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.DEVICE_UNREGISTERED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.SESSION_RELEASE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.NETWORK_ERROR.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.DEDICATED_BEARER_ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.REMOTE_PARTY_CANCELED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.NONE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.SUCCESS.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.ENGINE_ERROR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.UNKNOWN_ERROR.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.SESSION_RSRC_UNAVAILABLE.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[Result.Type.SIP_PROVISIONAL.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        if (result != null && result.getType() != Result.Type.NONE) {
            int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[result.getType().ordinal()];
            IMnoStrategy.StatusCode statusCode = null;
            if (i == 1) {
                String valueOf = result.getSipResponse() != null ? String.valueOf(result.getSipResponse().getId()) : null;
                Result.Type type = result.getType();
                if (strategyResponse != null) {
                    statusCode = strategyResponse.getStatusCode();
                }
                sendRCSMInfoToHQM(msg, "1", valueOf, type, statusCode);
            } else if (i == 2) {
                String valueOf2 = result.getMsrpResponse() != null ? String.valueOf(result.getMsrpResponse().getId()) : null;
                Result.Type type2 = result.getType();
                if (strategyResponse != null) {
                    statusCode = strategyResponse.getStatusCode();
                }
                sendRCSMInfoToHQM(msg, "2", valueOf2, type2, statusCode);
            } else if (i != 3) {
                String type3 = result.getType().toString();
                Result.Type type4 = result.getType();
                if (strategyResponse != null) {
                    statusCode = strategyResponse.getStatusCode();
                }
                sendRCSMInfoToHQM(msg, DiagnosisConstants.RCSM_ORST_ITER, type3, type4, statusCode);
            } else {
                String type5 = result.getType().toString();
                Result.Type type6 = result.getType();
                if (strategyResponse != null) {
                    statusCode = strategyResponse.getStatusCode();
                }
                sendRCSMInfoToHQM(msg, DiagnosisConstants.RCSM_ORST_REGI, type5, type6, statusCode);
            }
        }
    }

    public void onChatEstablished(ImSession chat) {
    }

    public void onIncomingMessageProcessed(ImIncomingMessageEvent msgEvent, ImSession session) {
    }

    public void onChatStatusUpdate(ImSession chat, ImSession.SessionState state) {
        ImSession.SessionState oldState = this.mMessagingSessionStates.get(chat.getChatId());
        if (state == ImSession.SessionState.CLOSED) {
            if (this.isEnableFailedMedia && oldState == ImSession.SessionState.ESTABLISHED) {
                state = ImSession.SessionState.FAILED_MEDIA;
                Log.e(LOG_TAG, "onChatStatusUpdate: State is FAILED MEDIA");
            }
            this.mMessagingSessions.remove(chat.getChatId());
        }
        for (IMessagingSessionListener listener : this.mListeners) {
            String str = LOG_TAG;
            Log.e(str, "onChatStatusUpdate: isEnableFailedMedia = " + this.isEnableFailedMedia);
            listener.onStateChanged(chat, state);
        }
        if (state == ImSession.SessionState.CLOSED) {
            this.mMessagingSessionStates.remove(chat.getChatId());
        } else if (state != ImSession.SessionState.INITIAL) {
            this.mMessagingSessionStates.put(chat.getChatId(), state);
        }
    }

    public void onChatClosed(ImSession chat, ImSessionClosedReason reason) {
    }

    public void onChatDeparted(ImSession chat) {
    }

    public void onComposingReceived(ImSession chat, ImsUri eventUri, String userAlias, boolean isComposing, int interval) {
    }

    public void onAddParticipantsSucceeded(String chatId, List<ImsUri> list) {
    }

    public void onAddParticipantsFailed(String chatId, List<ImsUri> list, ImErrorReason reason) {
    }

    public void onRemoveParticipantsSucceeded(String chatId, List<ImsUri> list) {
    }

    public void onRemoveParticipantsFailed(String chatId, List<ImsUri> list, ImErrorReason reason) {
    }

    public void onChangeGroupChatLeaderSucceeded(String chatId, List<ImsUri> list) {
    }

    public void onChangeGroupChatLeaderFailed(String chatId, List<ImsUri> list, ImErrorReason reason) {
    }

    public void onChangeGroupChatSubjectSucceeded(String chatId, String subject) {
    }

    public void onChangeGroupChatSubjectFailed(String chatId, String subject, ImErrorReason reason) {
    }

    public void onChangeGroupChatIconSuccess(String chatId, String icon_path) {
    }

    public void onChangeGroupChatIconFailed(String chatId, String icon_path, ImErrorReason reason) {
    }

    public void onChangeGroupAliasSucceeded(String chatId, String alias) {
    }

    public void onChangeGroupAliasFailed(String chatId, String alias, ImErrorReason reason) {
    }

    public void onParticipantsInserted(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onParticipantsUpdated(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onParticipantsDeleted(ImSession session, Collection<ImParticipant> collection) {
    }

    public void onNotifyParticipantsAdded(ImSession session, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsJoined(ImSession session, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsLeft(ImSession session, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsKickedOut(ImSession session, Map<ImParticipant, Date> map) {
    }

    public void onGroupChatLeaderChanged(ImSession session, String participants) {
    }

    public void onGroupChatLeaderInformed(ImSession session, String participants) {
    }

    public void onIncomingSessionProcessed(ImIncomingMessageEvent msgevent, ImSession session, boolean accepted) {
    }

    public void onImErrorReport(ImError imError, int phoneId) {
    }

    public void onProcessingFileTransferChanged(ImSession session) {
    }

    public void onChatSubjectUpdated(String chatId, ImSubjectData subjectData) {
    }

    public void onGroupChatIconUpdated(String chatId, ImIconData iconData) {
    }

    public void onGroupChatIconDeleted(String chatId) {
    }

    public void onParticipantAliasUpdated(String chatId, ImParticipant participant) {
    }

    public void onRequestSendMessage(ImSession session, MessageBase message) {
    }

    public void onBlockedMessageReceived(ImIncomingMessageEvent event) {
    }

    public void setLegacyLatching(ImsUri uri, boolean b, String imsi) {
    }

    public ImsUri normalizeUri(ImsUri uri) {
        return null;
    }

    public ImSession getMessagingSession(String sessionId) {
        return this.mMessagingSessions.get(sessionId);
    }

    public ImSession getMessagingSession(String serviceId, ImsUri uri) {
        Preconditions.checkNotNull(serviceId);
        Preconditions.checkNotNull(uri);
        for (ImSession s : this.mMessagingSessions.values()) {
            if (TextUtils.equals(s.getServiceId(), serviceId) && uri.equals(s.getRemoteUri())) {
                return s;
            }
        }
        return null;
    }

    public void sendMultimediaMessage(String chatId, byte[] contents, String contentType) {
        ImSession session = this.mMessagingSessions.get(chatId);
        if (session == null) {
            Log.e(LOG_TAG, "sendMultimediaMessage: Session not found.");
        } else {
            session.sendImMessage(createOutgoingMessage(chatId, session.getRemoteUri(), contents, contentType));
        }
    }

    public void abortSession(String chatId) {
        this.isWaitingForCloseTagSendingComplete = true;
        sendMessageDelayed(obtainMessage(7, chatId), 1000);
    }

    public void closeSession(String chatId) {
        String str = LOG_TAG;
        Log.d(str, "closeSession: " + chatId);
        ImSession session = this.mMessagingSessions.get(chatId);
        if (session == null) {
            Log.e(LOG_TAG, "closeSession: Session not found.");
        } else {
            session.closeSession();
        }
    }

    private ImMessage createOutgoingMessage(String chatId, ImsUri remoteUri, byte[] contents, String contentType) {
        return ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this)).listener(this).config(this.mConfig)).uriGenerator(this.mUriGenerator)).imsService(this.mImService)).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).chatId(chatId)).remoteUri(remoteUri)).body(new String(contents, Charset.defaultCharset()))).imdnId(StringIdGenerator.generateImdn())).dispNotification(new HashSet(Arrays.asList(new NotificationStatus[]{NotificationStatus.NONE})))).contentType(contentType)).direction(ImDirection.OUTGOING)).status(ImConstants.Status.TO_SEND)).type(ImConstants.Type.TEXT)).insertedTimestamp(System.currentTimeMillis())).mnoStrategy(RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()))).build();
    }

    public ImSession initiateMessagingSession(String serviceId, ImsUri remoteUri, String[] acceptTypes, String[] acceptWrappedTypes) {
        String si = adjustServiceId(serviceId);
        ImSession session = createOutgoingSession(SimManagerFactory.getSimManager().getImsi(), si, remoteUri, adjustAcceptTypes(si, acceptTypes), adjustAcceptWrappedTypes(si, acceptWrappedTypes));
        session.startSession();
        this.isEnableFailedMedia = false;
        return session;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String adjustServiceId(java.lang.String r5) {
        /*
            r4 = this;
            int r0 = r5.hashCode()
            r1 = 3
            r2 = 2
            r3 = 1
            switch(r0) {
                case 136638338: goto L_0x0029;
                case 1028711913: goto L_0x001f;
                case 1482410284: goto L_0x0015;
                case 1945740287: goto L_0x000b;
                default: goto L_0x000a;
            }
        L_0x000a:
            goto L_0x0033
        L_0x000b:
            java.lang.String r0 = "gsma.sharedsketch"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r2
            goto L_0x0034
        L_0x0015:
            java.lang.String r0 = "gsma.callcomposer"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = 0
            goto L_0x0034
        L_0x001f:
            java.lang.String r0 = "gsma.sharedmap"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r3
            goto L_0x0034
        L_0x0029:
            java.lang.String r0 = "gsma.callunanswered"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r1
            goto L_0x0034
        L_0x0033:
            r0 = -1
        L_0x0034:
            if (r0 == 0) goto L_0x0046
            if (r0 == r3) goto L_0x0043
            if (r0 == r2) goto L_0x0040
            if (r0 == r1) goto L_0x003d
            return r5
        L_0x003d:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\""
            return r0
        L_0x0040:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\""
            return r0
        L_0x0043:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""
            return r0
        L_0x0046:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\""
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.session.SessionModule.adjustServiceId(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String adjustServiceId2(java.lang.String r5) {
        /*
            r4 = this;
            int r0 = r5.hashCode()
            r1 = 3
            r2 = 2
            r3 = 1
            switch(r0) {
                case -1756044211: goto L_0x0029;
                case -749354161: goto L_0x001f;
                case -365814102: goto L_0x0015;
                case 1060594880: goto L_0x000b;
                default: goto L_0x000a;
            }
        L_0x000a:
            goto L_0x0033
        L_0x000b:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\""
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = 0
            goto L_0x0034
        L_0x0015:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\""
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r1
            goto L_0x0034
        L_0x001f:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r3
            goto L_0x0034
        L_0x0029:
            java.lang.String r0 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\""
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x000a
            r0 = r2
            goto L_0x0034
        L_0x0033:
            r0 = -1
        L_0x0034:
            if (r0 == 0) goto L_0x0046
            if (r0 == r3) goto L_0x0043
            if (r0 == r2) goto L_0x0040
            if (r0 == r1) goto L_0x003d
            return r5
        L_0x003d:
            java.lang.String r0 = "gsma.callunanswered"
            return r0
        L_0x0040:
            java.lang.String r0 = "gsma.sharedsketch"
            return r0
        L_0x0043:
            java.lang.String r0 = "gsma.sharedmap"
            return r0
        L_0x0046:
            java.lang.String r0 = "gsma.callcomposer"
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.session.SessionModule.adjustServiceId2(java.lang.String):java.lang.String");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.List<java.lang.String> adjustAcceptTypes(java.lang.String r7, java.lang.String[] r8) {
        /*
            r6 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            int r1 = r8.length
            r0.<init>(r1)
            java.util.Collections.addAll(r0, r8)
            int r1 = r7.hashCode()
            r2 = 3
            r3 = 2
            r4 = 1
            switch(r1) {
                case -1756044211: goto L_0x0032;
                case -749354161: goto L_0x0028;
                case -365814102: goto L_0x001e;
                case 1060594880: goto L_0x0014;
                default: goto L_0x0013;
            }
        L_0x0013:
            goto L_0x003c
        L_0x0014:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\""
            boolean r1 = r7.equals(r1)
            if (r1 == 0) goto L_0x0013
            r1 = 0
            goto L_0x003d
        L_0x001e:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\""
            boolean r1 = r7.equals(r1)
            if (r1 == 0) goto L_0x0013
            r1 = r4
            goto L_0x003d
        L_0x0028:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""
            boolean r1 = r7.equals(r1)
            if (r1 == 0) goto L_0x0013
            r1 = r3
            goto L_0x003d
        L_0x0032:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\""
            boolean r1 = r7.equals(r1)
            if (r1 == 0) goto L_0x0013
            r1 = r2
            goto L_0x003d
        L_0x003c:
            r1 = -1
        L_0x003d:
            java.lang.String r5 = "application/vnd.gsma.encall+xml"
            if (r1 == 0) goto L_0x005d
            if (r1 == r4) goto L_0x0054
            if (r1 == r3) goto L_0x004e
            if (r1 == r2) goto L_0x0048
            goto L_0x0066
        L_0x0048:
            java.lang.String r1 = "application/vnd.gsma.sharedsketch+xml"
            r0.add(r1)
            goto L_0x0066
        L_0x004e:
            java.lang.String r1 = "application/vnd.gsma.sharedmap+xml"
            r0.add(r1)
            goto L_0x0066
        L_0x0054:
            r0.add(r5)
            java.lang.String r1 = "application/vnd.gsma.rcs-ft-http+xml"
            r0.add(r1)
            goto L_0x0066
        L_0x005d:
            r0.add(r5)
            java.lang.String r1 = "message/cpim"
            r0.add(r1)
        L_0x0066:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.session.SessionModule.adjustAcceptTypes(java.lang.String, java.lang.String[]):java.util.List");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.List<java.lang.String> adjustAcceptWrappedTypes(java.lang.String r3, java.lang.String[] r4) {
        /*
            r2 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            int r1 = r4.length
            r0.<init>(r1)
            java.util.Collections.addAll(r0, r4)
            int r1 = r3.hashCode()
            switch(r1) {
                case -1756044211: goto L_0x002f;
                case -749354161: goto L_0x0025;
                case -365814102: goto L_0x001b;
                case 1060594880: goto L_0x0011;
                default: goto L_0x0010;
            }
        L_0x0010:
            goto L_0x0039
        L_0x0011:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\""
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0010
            r1 = 0
            goto L_0x003a
        L_0x001b:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\""
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0010
            r1 = 3
            goto L_0x003a
        L_0x0025:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\""
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0010
            r1 = 1
            goto L_0x003a
        L_0x002f:
            java.lang.String r1 = "+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\""
            boolean r1 = r3.equals(r1)
            if (r1 == 0) goto L_0x0010
            r1 = 2
            goto L_0x003a
        L_0x0039:
            r1 = -1
        L_0x003a:
            if (r1 == 0) goto L_0x003d
            goto L_0x0048
        L_0x003d:
            java.lang.String r1 = "message/imdn+xml"
            r0.add(r1)
            java.lang.String r1 = "application/vnd.gsma.rcs-ft-http+xml"
            r0.add(r1)
        L_0x0048:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.session.SessionModule.adjustAcceptWrappedTypes(java.lang.String, java.lang.String[]):java.util.List");
    }

    private ImSession createOutgoingSession(String imsi, String serviceId, ImsUri remoteUri, List<String> acceptTypes, List<String> acceptWrappedTypes) {
        Set<ImsUri> participants = new HashSet<>();
        participants.add(remoteUri);
        ImSession session = new ImSessionBuilder().looper(getLooper()).listener(this).config(this.mConfig).imsService(this.mImService).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mUriGenerator).chatId(StringIdGenerator.generateChatId(participants, imsi, true, ChatMode.OFF.getId())).participantsUri(participants).direction(ImDirection.OUTGOING).ownSimIMSI(imsi).getter(this).serviceId(serviceId).acceptTypes(acceptTypes).acceptWrappedTypes(acceptWrappedTypes).build();
        this.mMessagingSessions.put(session.getChatId(), session);
        return session;
    }

    private ImSession createIncomingImSession(ImIncomingSessionEvent event) {
        List<ImsUri> mInitiator = new ArrayList<>();
        mInitiator.add(event.mInitiator);
        ImSession session = new ImSessionBuilder().looper(getLooper()).listener(this).config(this.mConfig).imsService(this.mImService).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mUriGenerator).chatId(StringIdGenerator.generateChatId(new HashSet(event.mRecipients), event.mOwnImsi, true, ChatMode.OFF.getId())).participantsUri(mInitiator).sdpContentType(event.mSdpContentType).direction(ImDirection.INCOMING).rawHandle(event.mRawHandle).sessionType(event.mSessionType).ownSimIMSI(event.mOwnImsi).getter(this).serviceId(event.mServiceId).build();
        this.mMessagingSessions.put(session.getChatId(), session);
        return session;
    }

    public void registerMessagingSessionListener(IMessagingSessionListener listener) {
        this.mListeners.add(listener);
    }

    private void buildServiceConfig(String serviceID) {
    }

    public void deRegisterApp() {
    }

    public String[] getServicesRequiring() {
        return sRequiredServices;
    }

    public void handleIntent(Intent intent) {
        log("handleIntent" + intent);
    }

    public void init() {
        super.init();
        log("SessionModule init");
        updateAppInfo();
    }

    public boolean isServiceRegistered() {
        return getImsRegistration() != null;
    }

    public void log(String info) {
        Log.d(NAME, info);
    }

    public boolean needDeRegister(String packageName) {
        log("needDeRegister " + packageName);
        Hashtable<String, AppInfo> oldApps = AppInfo.ALL;
        updateAppInfo();
        if (this.canRegisterExt || !isServiceRegistered() || !oldApps.containsKey(packageName) || AppInfo.ALL.containsKey(packageName)) {
            return false;
        }
        return true;
    }

    public boolean needRegister(String packageName) {
        log("needRegister " + packageName);
        Hashtable<String, AppInfo> oldApps = AppInfo.ALL;
        updateAppInfo();
        if (!this.canRegisterExt || isServiceRegistered() || oldApps.containsKey(packageName) || !AppInfo.ALL.containsKey(packageName)) {
            return false;
        }
        return true;
    }

    public void onDeregistered(ImsRegistration regiInfo, int errorCode) {
        super.onDeregistered(regiInfo, errorCode);
        log("onDeregistered " + regiInfo.toString() + "\n errorcode=" + errorCode);
        this.mRegistrationId = -1;
        this.isEnableFailedMedia = true;
    }

    public void onConfigured(int phoneId) {
        super.onConfigured(phoneId);
        sendMessage(obtainMessage(8, Integer.valueOf(phoneId)));
    }

    public void onDeregistering(ImsRegistration reg) {
        super.onDeregistering(reg);
        this.isEnableFailedMedia = true;
        log("onDeregistering " + reg.toString());
        if (SimManagerFactory.getSimManager().getSimMno() == Mno.RJIL) {
            for (String chatId : this.mMessagingSessions.keySet()) {
                closeSession(chatId);
            }
        }
    }

    public void onRegistered(ImsRegistration regiInfo) {
        ICapabilityDiscoveryModule discoveryModule;
        if (regiInfo == null) {
            Log.d(LOG_TAG, "regiInfo is null");
            return;
        }
        super.onRegistered(regiInfo);
        int phoneId = regiInfo.getPhoneId();
        log("onRegistered " + regiInfo.toString());
        if (regiInfo.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(regiInfo);
        }
        this.isEnableFailedMedia = false;
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(regiInfo.getPreferredImpu().getUri());
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            Iterator it = regiInfo.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr addr = (NameAddr) it.next();
                if (addr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    this.mUriGenerator = UriGeneratorFactory.getInstance().get(addr.getUri());
                    break;
                }
            }
        }
        if (regiInfo.hasService("options") && regiInfo.hasService("ec") && !regiInfo.hasService("vs") && (discoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule()) != null) {
            discoveryModule.exchangeCapabilitiesForVSHOnRegi(false, phoneId);
        }
    }

    public void onSimChanged(int phoneId) {
        super.onSimChanged(phoneId);
    }

    public void registerApp() {
        log("registerApp");
        if (getImsRegistration() != null) {
            for (String serviceID : this.mIariTypes) {
                buildServiceConfig(serviceID);
            }
            log("register ext done");
        }
    }

    public void onServiceSwitched(int phoneId, ContentValues switchStatus) {
        String str = LOG_TAG;
        Log.d(str, "onServiceSwitched: " + phoneId);
        updateFeatures(phoneId);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            log("SessionModule start");
            this.mImService.registerForImIncomingSession(this, 1, (Object) null);
            this.mImService.registerForImSessionEstablished(this, 2, (Object) null);
            this.mImService.registerForImSessionClosed(this, 3, (Object) null);
            this.mImService.registerForImIncomingMessage(this, 4, (Object) null);
            this.mImService.registerForMessageFailed(this, 5, (Object) null);
        }
    }

    public void stop() {
        super.stop();
        this.mRegisteredServices.clear();
        this.mImService.unregisterForImIncomingSession(this);
        this.mImService.unregisterForImSessionEstablished(this);
        this.mImService.unregisterForImSessionClosed(this);
        this.mImService.unregisterForImIncomingMessage(this);
        this.mImService.unregisterForMessageFailed(this);
        log("SessionModule stop");
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                onIncomingSessionReceived((ImIncomingSessionEvent) ((AsyncResult) msg.obj).result);
                return;
            case 2:
                onSessionEstablished((ImSessionEstablishedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 3:
                onSessionClosed((ImSessionClosedEvent) ((AsyncResult) msg.obj).result);
                return;
            case 4:
                onIncomingMessageReceived((ImIncomingMessageEvent) ((AsyncResult) msg.obj).result);
                return;
            case 6:
                onRejectSession((ImSession) msg.obj);
                return;
            case 7:
                this.isWaitingForCloseTagSendingComplete = false;
                closeSession((String) msg.obj);
                return;
            case 8:
                updateConfig(((Integer) msg.obj).intValue());
                return;
            default:
                return;
        }
    }

    private void updateConfig(int phoneId) {
        updateFeatures(phoneId);
        updateAppInfo();
        ImsProfile profile = ImsRegistry.getRegistrationManager().getImsProfile(phoneId, ImsProfile.PROFILE_TYPE.CHAT);
        if (((Boolean) Optional.ofNullable(profile).map($$Lambda$SessionModule$HkaIUyOEbyiL7wLWW591fX5KmM.INSTANCE).orElse(true)).booleanValue()) {
            Log.e(LOG_TAG, "profile is null, return !!!");
            return;
        }
        String rcsProfile = ConfigUtil.getRcsProfileWithFeature(this.mContext, phoneId, profile);
        String str = LOG_TAG;
        Log.d(str, "rcsProfile = " + rcsProfile);
        if (ImsRegistry.getServiceModuleManager().getImModule() != null) {
            this.mConfig = ImsRegistry.getServiceModuleManager().getImModule().getImConfig();
        }
    }

    static /* synthetic */ Boolean lambda$updateConfig$0(ImsProfile p) {
        return Boolean.valueOf(!p.hasService("im") && !p.hasService("slm"));
    }

    private void onIncomingSessionReceived(ImIncomingSessionEvent event) {
        String str = LOG_TAG;
        Log.d(str, "onIncomingSessionReceived: " + event);
        this.isEnableFailedMedia = false;
        if (this.mRegisteredServices.contains(event.mServiceId)) {
            ImSession session = createIncomingImSession(event);
            String si = adjustServiceId2(event.mServiceId);
            boolean activeCall = getActiveCall(event.mInitiator);
            String str2 = LOG_TAG;
            Log.d(str2, "getActiveCall result = " + activeCall);
            if (activeCall || (!si.equals(SERVICE_ID_SHARED_MAP) && !si.equals(SERVICE_ID_SHARED_SKETCH))) {
                for (IMessagingSessionListener listener : this.mListeners) {
                    listener.onIncomingSessionInvited(session, MIMETYPE_PREFIX + si);
                }
                session.processIncomingSession(event);
                return;
            }
            String str3 = LOG_TAG;
            Log.d(str3, "Call State :" + this.callState + "ServiceID: " + si);
            session.processIncomingSession(event);
            sendMessage(obtainMessage(6, session));
        }
    }

    private void onSessionEstablished(ImSessionEstablishedEvent event) {
        String str = LOG_TAG;
        Log.d(str, "onSessionEstablished: " + event);
        ImSession session = this.mMessagingSessions.get(event.mChatId);
        if (session == null) {
            Log.e(LOG_TAG, "onSessionEstablished: Session not found.");
        } else {
            session.receiveSessionEstablished(event);
        }
    }

    private void onSessionClosed(ImSessionClosedEvent event) {
        ImSession session;
        String str = LOG_TAG;
        Log.d(str, "onSessionClosed: " + event);
        if (event.mChatId == null) {
            session = getImSessionByRawHandle(event.mRawHandle);
        } else {
            session = this.mMessagingSessions.get(event.mChatId);
        }
        if (session == null) {
            Log.e(LOG_TAG, "onSessionClosed: Session not found.");
            return;
        }
        ImError error = event.mResult.getImError();
        if (error == ImError.NETWORK_ERROR || error == ImError.DEVICE_UNREGISTERED || error == ImError.DEDICATED_BEARER_ERROR) {
            String str2 = LOG_TAG;
            Log.e(str2, "onSessionClosed: Session closed by " + error);
            this.isEnableFailedMedia = true;
        }
        session.receiveSessionClosed(event);
    }

    public ImSession getImSessionByRawHandle(Object rawHandle) {
        for (ImSession s : this.mMessagingSessions.values()) {
            if (s.hasImSessionInfo(rawHandle)) {
                return s;
            }
        }
        return null;
    }

    private void onIncomingMessageReceived(ImIncomingMessageEvent event) {
        String str = LOG_TAG;
        Log.d(str, "onIncomingMessageReceived: " + event);
        if (event.mChatId == null) {
            Log.e(LOG_TAG, "onIncomingMessageReceived: mChatId is null.");
            return;
        }
        ImSession session = this.mMessagingSessions.get(event.mChatId);
        if (session == null) {
            Log.e(LOG_TAG, "onIncomingMessageReceived: Session not found.");
            return;
        }
        for (IMessagingSessionListener listener : this.mListeners) {
            listener.onMessageReceived(session, event.mBody.getBytes(Charset.defaultCharset()), event.mContentType);
        }
        int phoneId = SimManagerFactory.getPhoneId(session.getOwnImsi());
        if (phoneId == -1) {
            phoneId = SimUtil.getDefaultPhoneId();
        }
        storeDRCSInfoToImsLogAgent(phoneId, ImDirection.INCOMING, (String) null, (Result.Type) null, (IMnoStrategy.StatusCode) null);
    }

    private void onRejectSession(ImSession session) {
        Log.d(LOG_TAG, "onRejectSession");
        session.rejectSession();
    }

    public void updateAppInfo() {
        this.canRegisterExt = false;
        AppInfo.ALL.clear();
        this.mIariTypes.clear();
        updateAppInfo(INTENT_FILTER_MESSAGE);
        updateAppInfo(INTENT_FILTER_STREAM);
        if (!AppInfo.ALL.isEmpty()) {
            this.canRegisterExt = true;
        }
    }

    public void updateAppInfo(String type) {
        AppInfo app;
        Intent in = new Intent();
        in.setType(MIMETYPE_ALL);
        in.addCategory(Intents.INTENT_CATEGORY);
        in.addCategory("android.intent.category.LAUNCHER");
        in.setAction(type);
        List<ResolveInfo> lsr = this.mContext.getPackageManager().queryBroadcastReceivers(in, 64);
        if (lsr != null) {
            for (ResolveInfo ri : lsr) {
                String applicationName = ri.activityInfo.packageName;
                log("new app name = " + applicationName);
                synchronized (AppInfo.ALL) {
                    if (AppInfo.ALL.containsKey(applicationName)) {
                        app = AppInfo.ALL.get(applicationName);
                    } else {
                        app = new AppInfo(applicationName);
                    }
                    if (ri.filter != null) {
                        int count = ri.filter.countDataTypes();
                        List<String> list = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            String dataType = ri.filter.getDataType(i);
                            String extType = dataType.substring(dataType.lastIndexOf("/") + 1);
                            list.add(extType);
                            if (!this.mIariTypes.contains(extType)) {
                                this.mIariTypes.add(extType);
                            }
                        }
                        if (list.size() > 0) {
                            app.addType(type, list);
                        }
                    }
                }
            }
        }
    }

    public boolean isServiceActivated(String serviceId) {
        log("isServiceActivated,serviceId= " + serviceId);
        int defaultPhoneId = SimUtil.getDefaultPhoneId();
        if (!serviceId.startsWith("gsma")) {
            int activeNetwork = ImsRegistry.getRegistrationManager().getCurrentNetworkByPhoneId(defaultPhoneId);
            if (activeNetwork == 1 || activeNetwork == 2) {
                log("isServiceActivated: current network is 2G, return ");
                return false;
            }
            if (Settings.System.getInt(this.mContext.getContentResolver(), "easy_mode_switch", 1) == 0) {
                log("Easymode on, return ");
                return false;
            }
            for (String sid : this.mServiceIDsFromMetaData) {
                if (sid.equalsIgnoreCase(serviceId)) {
                    return true;
                }
            }
        } else {
            String icsiServiceId = serviceId.substring(5);
            if ("callunanswered".equalsIgnoreCase(icsiServiceId)) {
                return this.mPostCallAuth[defaultPhoneId];
            }
            if ("callcomposer".equalsIgnoreCase(icsiServiceId)) {
                return this.mComposerAuth[defaultPhoneId];
            }
            if ("sharedmap".equalsIgnoreCase(icsiServiceId)) {
                return this.mSharedMapAuth[defaultPhoneId];
            }
            if ("sharedsketch".equalsIgnoreCase(icsiServiceId)) {
                return this.mSharedSketchAuth[defaultPhoneId];
            }
            for (String sid2 : this.mServiceIDsFromMetaData) {
                if (sid2.equalsIgnoreCase(serviceId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendInstantMultimediaMessage(String serviceId, ImsUri uri, byte[] content, String contentType) {
        log("sendInstantMultimediaMessage,serviceId= " + serviceId + ",contact=" + uri + ",contentType=" + contentType);
        ImSession session = getMessagingSession(serviceId, uri);
        if (session != null) {
            session.sendImMessage(createOutgoingMessage(session.getChatId(), uri, content, contentType));
        }
    }

    public static class AppInfo {
        public static Hashtable<String, AppInfo> ALL = new Hashtable<>();
        private Hashtable<String, List<String>> mExtTable = new Hashtable<>();

        AppInfo(String appName) {
            ALL.put(appName, this);
        }

        public void addType(String type, List<String> list) {
            if (!this.mExtTable.containsKey(type)) {
                this.mExtTable.put(type, list);
            }
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }

    public void onMessageRevokeTimerExpired(String chatId, Collection<String> collection, String imsi) {
    }

    public void addToRevokingMessages(String imdnMessageId, String chatId) {
    }

    public void removeFromRevokingMessages(Collection<String> collection) {
    }

    public void onMessageRevocationDone(ImConstants.RevocationStatus status, Collection<MessageBase> collection, ImSession session) {
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService("connectivity");
        Network[] netInfo = cm.getAllNetworks();
        int length = netInfo.length;
        int i = 0;
        while (i < length) {
            NetworkInfo ni = cm.getNetworkInfo(netInfo[i]);
            if (ni == null || ni.getType() != 1 || !ni.isConnected()) {
                i++;
            } else {
                Log.d(LOG_TAG, "isWifiConnected: " + ni);
                return true;
            }
        }
        return false;
    }

    public IMnoStrategy getRcsStrategy() {
        return RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
    }

    public IMnoStrategy getRcsStrategy(int phoneId) {
        return RcsPolicyManager.getRcsStrategy(phoneId);
    }

    public MessageBase getMessage(int id) {
        return ImCache.getInstance().getMessage(id);
    }

    public MessageBase getMessage(String imdnId, ImDirection direction) {
        return ImCache.getInstance().getMessage(imdnId, direction);
    }

    public List<MessageBase> getMessages(Collection<String> ids) {
        return ImCache.getInstance().getMessages(ids);
    }

    public MessageBase getPendingMessage(int id) {
        return ImCache.getInstance().getPendingMessage(id);
    }

    public List<MessageBase> getAllPendingMessages(String chatId) {
        return ImCache.getInstance().getAllPendingMessages(chatId);
    }

    public List<String> getMessageIdsForDisplayAggregation(String chatId, ImDirection direction, Long timestamp) {
        return ImCache.getInstance().getMessageIdsForDisplayAggregation(chatId, direction, timestamp);
    }

    public String onRequestIncomingFtTransferPath() {
        File fileDir = this.mContext.getExternalFilesDir((String) null);
        if (fileDir != null) {
            return fileDir.getAbsolutePath();
        }
        return null;
    }

    public Network getNetwork(int phoneId) {
        ImsRegistration imsRegistration = getImsRegistration(phoneId);
        if (imsRegistration == null || this.mConfig.isFtHttpOverDefaultPdn()) {
            return null;
        }
        return imsRegistration.getNetwork();
    }

    public Set<ImsUri> getOwnUris(int phoneId) {
        Set<ImsUri> ownUris = new HashSet<>();
        ImsRegistration imsRegistration = getImsRegistration();
        if (imsRegistration != null) {
            for (NameAddr addr : imsRegistration.getImpuList()) {
                ownUris.add(this.mUriGenerator.normalize(addr.getUri()));
            }
        }
        return ownUris;
    }

    public void onCallStateChanged(int phoneId, List<ICall> calls) {
        int nConnectedCalls = 0;
        this.mCallList.clear();
        for (ICall call : calls) {
            if (call.isConnected()) {
                nConnectedCalls++;
                ImsUri uri = this.mUriGenerator.getNormalizedUri(call.getNumber(), true);
                if (uri != null && !this.mCallList.contains(uri)) {
                    this.mCallList.add(uri);
                }
            }
        }
        String str = LOG_TAG;
        Log.d(str, "nConnecteCalls = " + nConnectedCalls);
        if (nConnectedCalls > 1) {
            this.mCallList.clear();
        }
    }

    private boolean getActiveCall(ImsUri uri) {
        for (ImsUri callUri : this.mCallList) {
            if (callUri != null && callUri.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    private synchronized void updateFeatures(int phoneId) {
        Log.d(LOG_TAG, "updateFeatures: phoneId = " + phoneId);
        boolean z = true;
        if (!(DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) == 1)) {
            Log.d(LOG_TAG, "updateFeatures: RCS is disabled, return");
            this.mEnabledFeatures[phoneId] = 0;
            return;
        }
        this.mCallComposerTimerIdle[phoneId] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, phoneId), Integer.valueOf(MNO.EVR_ESN)).intValue();
        log("updateFeatures: mCallComposerTimerIdle=" + this.mCallComposerTimerIdle[phoneId]);
        int val = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, phoneId), 0).intValue();
        boolean[] zArr = this.mComposerAuth;
        if (val != 1) {
            if (val != 3) {
                z = false;
            }
        }
        zArr[phoneId] = z;
        log("updateFeatures: Composer enable :" + this.mComposerAuth[phoneId]);
        this.mSharedMapAuth[phoneId] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, phoneId), false).booleanValue();
        log("updateFeatures: SharedMapAuth enable " + this.mSharedMapAuth[phoneId]);
        this.mSharedSketchAuth[phoneId] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, phoneId), false).booleanValue();
        log("updateFeatures: SharedSketchAuth enable " + this.mSharedSketchAuth[phoneId]);
        this.mPostCallAuth[phoneId] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, phoneId), false).booleanValue();
        log("updateFeatures: PostCallAuth enable " + this.mPostCallAuth[phoneId]);
        this.mEnabledFeatures[phoneId] = 0;
        if (this.mComposerAuth[phoneId]) {
            long[] jArr = this.mEnabledFeatures;
            jArr[phoneId] = jArr[phoneId] | Capabilities.FEATURE_ENRICHED_CALL_COMPOSER;
            this.mRegisteredServices.add("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callcomposer\"");
        }
        if (this.mSharedMapAuth[phoneId]) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[phoneId] = jArr2[phoneId] | Capabilities.FEATURE_ENRICHED_SHARED_MAP;
            this.mRegisteredServices.add("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedmap\"");
        }
        if (this.mSharedSketchAuth[phoneId]) {
            long[] jArr3 = this.mEnabledFeatures;
            jArr3[phoneId] = jArr3[phoneId] | Capabilities.FEATURE_ENRICHED_SHARED_SKETCH;
            this.mRegisteredServices.add("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.sharedsketch\"");
        }
        if (this.mPostCallAuth[phoneId]) {
            long[] jArr4 = this.mEnabledFeatures;
            jArr4[phoneId] = jArr4[phoneId] | Capabilities.FEATURE_ENRICHED_POST_CALL;
            this.mRegisteredServices.add("+g.3gpp.icsi-ref=\"urn%3Aurn-7%3A3gpp-service.ims.icsi.gsma.callunanswered\"");
        }
        log("updateFeatures: mEnabledFeatures=" + this.mEnabledFeatures[phoneId]);
    }

    private void storeDRCSInfoToImsLogAgent(int phoneId, ImDirection imDirection, String orst, Result.Type resultType, IMnoStrategy.StatusCode statusCode) {
        ContentValues values = new ContentValues();
        values.put(DiagnosisConstants.KEY_SEND_MODE, 1);
        values.put(DiagnosisConstants.KEY_OVERWRITE_MODE, 1);
        if (imDirection == ImDirection.OUTGOING) {
            char c = 65535;
            switch (orst.hashCode()) {
                case 48:
                    if (orst.equals("0")) {
                        c = 0;
                        break;
                    }
                    break;
                case 49:
                    if (orst.equals("1")) {
                        c = 1;
                        break;
                    }
                    break;
                case 50:
                    if (orst.equals("2")) {
                        c = 2;
                        break;
                    }
                    break;
                case 51:
                    if (orst.equals(DiagnosisConstants.RCSM_ORST_REGI)) {
                        c = 4;
                        break;
                    }
                    break;
                case 52:
                    if (orst.equals(DiagnosisConstants.RCSM_ORST_HTTP)) {
                        c = 3;
                        break;
                    }
                    break;
                case 53:
                    if (orst.equals(DiagnosisConstants.RCSM_ORST_ITER)) {
                        c = 5;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_SUCCESS, 1);
                values.put(DiagnosisConstants.DRCS_KEY_RCS_EC_MO_SUCCESS, 1);
            } else if (c == 1 || c == 2 || c == 3) {
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL, 1);
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_NETWORK, 1);
            } else if (c == 4) {
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL, 1);
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_TERMINAL, 1);
            } else if (c == 5) {
                values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL, 1);
                int i = AnonymousClass2.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$result$Result$Type[(resultType != null ? resultType : Result.Type.UNKNOWN_ERROR).ordinal()];
                if (i == 1 || i == 2 || i == 4 || i == 5 || i == 6 || i == 7) {
                    values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_NETWORK, 1);
                } else {
                    values.put(DiagnosisConstants.DRCS_KEY_RCS_MO_FAIL_TERMINAL, 1);
                }
            }
            if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
                values.put(DiagnosisConstants.DRCS_KEY_SMS_FALLBACK, 1);
            }
        } else if (imDirection == ImDirection.INCOMING) {
            values.put(DiagnosisConstants.DRCS_KEY_RCS_MT, 1);
        }
        ImsLogAgentUtil.storeLogToAgent(phoneId, this.mContext, DiagnosisConstants.FEATURE_DRCS, values);
    }

    private boolean sendRCSMInfoToHQM(MessageBase msg, String orst, String cause) {
        return sendRCSMInfoToHQM(msg, orst, cause, (Result.Type) null, (IMnoStrategy.StatusCode) null);
    }

    private boolean sendRCSMInfoToHQM(MessageBase msg, String orst, String cause, Result.Type resultType, IMnoStrategy.StatusCode statusCode) {
        int phoneId;
        String regiRat;
        String str;
        String str2 = orst;
        String str3 = cause;
        int phoneId2 = SimManagerFactory.getPhoneId(msg.getOwnIMSI());
        char c = 65535;
        if (phoneId2 == -1) {
            phoneId = SimUtil.getDefaultPhoneId();
        } else {
            phoneId = phoneId2;
        }
        Map<String, String> rcsmKeys = new LinkedHashMap<>();
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ORST, str2);
        if (msg.getDirection() == ImDirection.OUTGOING) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MDIR, "0");
        } else if (msg.getDirection() == ImDirection.INCOMING) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MDIR, "1");
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MTYP, DiagnosisConstants.RCSM_MTYP_EC);
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MCID, msg.getChatId());
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MIID, msg.getImdnId());
        try {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSIZ, String.valueOf(msg.getBody().getBytes("UTF-8").length));
        } catch (UnsupportedEncodingException e) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSIZ, "-1");
        } catch (NullPointerException e2) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSIZ, "0");
        }
        try {
            regiRat = String.valueOf(getImsRegistration(phoneId).getCurrentRat());
        } catch (NullPointerException e3) {
            Log.e(LOG_TAG, "sendRCSMInfoToHQM: NullPointerException e = " + e3);
            regiRat = "-1";
        }
        if (isWifiConnected()) {
            str = regiRat + DiagnosisConstants.RCSM_MRAT_WIFI_POSTFIX;
        } else {
            str = regiRat;
        }
        rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MRAT, str);
        switch (orst.hashCode()) {
            case 48:
                if (str2.equals("0")) {
                    c = 0;
                    break;
                }
                break;
            case 49:
                if (str2.equals("1")) {
                    c = 1;
                    break;
                }
                break;
            case 50:
                if (str2.equals("2")) {
                    c = 2;
                    break;
                }
                break;
            case 51:
                if (str2.equals(DiagnosisConstants.RCSM_ORST_REGI)) {
                    c = 3;
                    break;
                }
                break;
            case 52:
                if (str2.equals(DiagnosisConstants.RCSM_ORST_HTTP)) {
                    c = 4;
                    break;
                }
                break;
            case 53:
                if (str2.equals(DiagnosisConstants.RCSM_ORST_ITER)) {
                    c = 5;
                    break;
                }
                break;
        }
        if (c != 0) {
            if (c == 1) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_SIPR, str3);
            } else if (c == 2) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_MSRP, str3);
            } else if (c == 3) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ITER, str3);
            } else if (c == 4) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_HTTP, str3);
            } else if (c != 5) {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ITER, str3);
            } else {
                rcsmKeys.put(DiagnosisConstants.RCSM_KEY_ITER, str3);
            }
        }
        if (statusCode != null) {
            rcsmKeys.put(DiagnosisConstants.RCSM_KEY_SRSC, statusCode.toString());
        }
        storeDRCSInfoToImsLogAgent(phoneId, msg.getDirection(), orst, resultType, statusCode);
        return RcsHqmAgent.sendRCSInfoToHQM(this.mContext, DiagnosisConstants.FEATURE_RCSM, phoneId, rcsmKeys);
    }
}
