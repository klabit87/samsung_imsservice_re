package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaMessagingSession;
import com.gsma.services.rcs.extension.IMultimediaMessagingSessionListener;
import com.gsma.services.rcs.extension.IMultimediaSessionService;
import com.gsma.services.rcs.extension.IMultimediaSessionServiceConfiguration;
import com.gsma.services.rcs.extension.IMultimediaStreamingSession;
import com.gsma.services.rcs.extension.IMultimediaStreamingSessionListener;
import com.gsma.services.rcs.extension.MultimediaSession;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.options.Intents;
import com.sec.internal.ims.servicemodules.session.IMessagingSessionListener;
import com.sec.internal.ims.servicemodules.session.SessionModule;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.MultimediaMessagingSessionEventBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.MultimediaStreamingSessionEventBroadcaster;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultimediaSessionServiceImpl extends IMultimediaSessionService.Stub implements IMessagingSessionListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = MultimediaSessionServiceImpl.class.getSimpleName();
    private final Object lock = new Object();
    private final Context mContext;
    private final Map<String, IMultimediaMessagingSession> mMultimediaMessagingCache = new HashMap();
    private final MultimediaMessagingSessionEventBroadcaster mMultimediaMessagingSessionEventBroadcaster = new MultimediaMessagingSessionEventBroadcaster();
    private final Map<String, IMultimediaStreamingSession> mMultimediaStreamingCache = new HashMap();
    private final MultimediaStreamingSessionEventBroadcaster mMultimediaStreamingSessionEventBroadcaster = new MultimediaStreamingSessionEventBroadcaster();
    private final Map<String, Boolean> mSessionEstablishCache = new HashMap();
    private final ISessionModule mSessionModule;
    private UriGenerator mUriGenerator;
    private RemoteCallbackList<IRcsServiceRegistrationListener> serviceListeners = new RemoteCallbackList<>();

    public MultimediaSessionServiceImpl(ISessionModule service) {
        this.mSessionModule = service;
        this.mContext = ImsRegistry.getContext();
        this.mSessionModule.registerMessagingSessionListener(this);
        this.mUriGenerator = UriGeneratorFactory.getInstance().get();
    }

    public void addEventListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.lock) {
            this.serviceListeners.register(listener);
        }
    }

    public void addEventListener2(IMultimediaMessagingSessionListener listener) {
        synchronized (this.lock) {
            this.mMultimediaMessagingSessionEventBroadcaster.addMultimediaMessagingEventListener(listener);
        }
    }

    public void addEventListener3(IMultimediaStreamingSessionListener listener) {
        synchronized (this.lock) {
            this.mMultimediaStreamingSessionEventBroadcaster.addMultimediaStreamingEventListener(listener);
        }
    }

    private void addMultimediaMessaging(MultimediaMessagingSessionImpl multimediaMessaging) {
        this.mMultimediaMessagingCache.put(multimediaMessaging.getSessionId(), multimediaMessaging);
        this.mSessionEstablishCache.put(multimediaMessaging.getSessionId(), Boolean.FALSE);
    }

    public IMultimediaSessionServiceConfiguration getConfiguration() {
        return MultimediaSessionServiceConfigurationImpl.getInstance(this.mSessionModule);
    }

    public IMultimediaMessagingSession getMessagingSession(String sessionId) throws ServerApiException {
        IMultimediaMessagingSession result = this.mMultimediaMessagingCache.get(sessionId);
        if (result != null) {
            return result;
        }
        ImSession session = this.mSessionModule.getMessagingSession(sessionId);
        if (session == null) {
            Log.e(LOG_TAG, "Session not exists.");
            return null;
        }
        MultimediaMessagingSessionImpl impl = new MultimediaMessagingSessionImpl(this.mSessionModule, session);
        addMultimediaMessaging(impl);
        return impl;
    }

    public List<IBinder> getMessagingSessions(String serviceId) throws ServerApiException {
        try {
            List<IBinder> multimediaMessagingSessions = new ArrayList<>();
            for (IMultimediaMessagingSession session : this.mMultimediaMessagingCache.values()) {
                if (session.getServiceId().contains(serviceId)) {
                    multimediaMessagingSessions.add(session.asBinder());
                }
            }
            return multimediaMessagingSessions;
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public IMultimediaStreamingSession getStreamingSession(String sessionId) throws ServerApiException {
        throw new ServerApiException("Unsupported operation");
    }

    public List<IBinder> getStreamingSessions(String serviceId) throws ServerApiException {
        try {
            List<IBinder> multimediaStreamingSessions = new ArrayList<>();
            for (IMultimediaStreamingSession multimediaStreamingSession : this.mMultimediaStreamingCache.values()) {
                if (multimediaStreamingSession.getServiceId().contains(serviceId)) {
                    multimediaStreamingSessions.add(multimediaStreamingSession.asBinder());
                }
            }
            return multimediaStreamingSessions;
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public IMultimediaMessagingSession initiateMessagingSession(String serviceId, ContactId contact, String[] acceptType, String[] acceptWrappedType) throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "initiateMessagingSession: " + serviceId + " ContactId = " + IMSLog.checker(contact));
        ImsRegistration imsRegInfo = this.mSessionModule.getImsRegistration();
        if (imsRegInfo == null) {
            return null;
        }
        UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(imsRegInfo.getPreferredImpu().getUri());
        this.mUriGenerator = uriGenerator;
        ImsUri mUri = uriGenerator.getNormalizedUri(contact.toString(), true);
        if (mUri == null || TextUtils.isEmpty(serviceId)) {
            return null;
        }
        MultimediaMessagingSessionImpl impl = new MultimediaMessagingSessionImpl(this.mSessionModule, this.mSessionModule.initiateMessagingSession(serviceId, mUri, acceptType, acceptWrappedType));
        addMultimediaMessaging(impl);
        return impl;
    }

    public IMultimediaStreamingSession initiateStreamingSession(String serviceId, ContactId contact) throws ServerApiException {
        throw new ServerApiException("Unsupported operation");
    }

    public boolean isServiceRegistered() {
        return this.mSessionModule.isServiceRegistered();
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.lock) {
            int N = this.serviceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.serviceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    this.serviceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.serviceListeners.finishBroadcast();
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.lock) {
            this.serviceListeners.unregister(listener);
        }
    }

    public void removeEventListener2(IMultimediaMessagingSessionListener listener) {
        synchronized (this.lock) {
            this.mMultimediaMessagingSessionEventBroadcaster.removeMultimediaMessagingEventListener(listener);
        }
    }

    public void removeEventListener3(IMultimediaStreamingSessionListener listener) {
        synchronized (this.lock) {
            this.mMultimediaStreamingSessionEventBroadcaster.removeMultimediaStreamingEventListener(listener);
        }
    }

    public void setInactivityTimeout(long timeout) throws ServerApiException {
        try {
            SessionModule.setInactivityTimeout(timeout);
        } catch (Exception e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public void sendInstantMultimediaMessage(String serviceId, ContactId contact, byte[] content, String contentType) throws ServerApiException {
        String str = LOG_TAG;
        Log.d(str, "sendInstantMultimediaMessage,serviceId=" + serviceId + "contactId=" + IMSLog.checker(contact));
        if (contact != null) {
            try {
                this.mSessionModule.sendInstantMultimediaMessage(serviceId, UriUtil.parseNumber(contact.toString()), content, contentType);
            } catch (Exception e) {
                throw new ServerApiException(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeMultimediaMessaging(String sessionId) {
        this.mMultimediaMessagingCache.remove(sessionId);
        this.mSessionEstablishCache.remove(sessionId);
    }

    public void onIncomingSessionInvited(ImSession session, String mimeType) {
        String str = LOG_TAG;
        Log.d(str, "onIncomingSessionInvited: " + session.getChatId());
        Intent intent = new Intent(SessionModule.INTENT_FILTER_MESSAGE);
        intent.addCategory(Intents.INTENT_CATEGORY);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(16777216);
        intent.addFlags(LogClass.SIM_EVENT);
        intent.setType(mimeType);
        intent.putExtra("sessionId", session.getChatId());
        this.mContext.sendBroadcast(intent);
    }

    public void onStateChanged(ImSession session, ImSession.SessionState state) {
        String str = LOG_TAG;
        Log.d(str, "onStateChanged: id=" + session.getChatId() + ", state=" + state);
        ImsUri remoteUri = session.getRemoteUri();
        ImSessionClosedEvent event = session.getImSessionClosedEvent();
        boolean isTimerForSessionExpired = session.isTimerExpired();
        MultimediaSession.ReasonCode reasonCode = MultimediaSession.ReasonCode.UNSPECIFIED;
        if (remoteUri != null) {
            if (state == ImSession.SessionState.ESTABLISHED) {
                this.mSessionEstablishCache.put(session.getChatId(), Boolean.TRUE);
            }
            if (event != null) {
                reasonCode = translateError(event.mResult.getImError(), state);
            }
            if (state == ImSession.SessionState.CLOSED && isTimerForSessionExpired) {
                reasonCode = MultimediaSession.ReasonCode.ABORTED_BY_INACTIVITY;
            }
            if (state == ImSession.SessionState.CLOSED || state == ImSession.SessionState.FAILED_MEDIA) {
                removeMultimediaMessaging(session.getChatId());
            }
            this.mMultimediaMessagingSessionEventBroadcaster.broadcastStateChanged(new ContactId(remoteUri.getMsisdn()), session.getChatId(), translateState(state, session.getDirection()), reasonCode);
        }
    }

    public void onMessageReceived(ImSession session, byte[] content, String contentType) {
        ContactId contactId = null;
        ImsUri uri = session.getRemoteUri();
        if (uri != null) {
            contactId = new ContactId(uri.getMsisdn());
        }
        this.mMultimediaMessagingSessionEventBroadcaster.broadcastMessageReceived(contactId, session.getChatId(), content, contentType);
    }

    public void onMessagesFlushed(ImSession session) {
        String str = LOG_TAG;
        Log.d(str, "onMessagesFlushed: " + session.getChatId());
        this.mMultimediaMessagingSessionEventBroadcaster.broadcastMessagesFlushed(new ContactId(session.getRemoteUri().getMsisdn()), session.getChatId());
    }

    private static MultimediaSession.ReasonCode translateError(ImError error, ImSession.SessionState state) {
        if (state == ImSession.SessionState.FAILED_MEDIA) {
            return MultimediaSession.ReasonCode.FAILED_MEDIA;
        }
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[error.ordinal()]) {
            case 1:
                return MultimediaSession.ReasonCode.REJECT_REASON_BUSY;
            case 2:
                return MultimediaSession.ReasonCode.REJECT_REASON_DECLINE;
            case 3:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_UNAVAILABLE;
            case 4:
                return MultimediaSession.ReasonCode.REJECT_REASON_BAD_REQUEST;
            case 5:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQ_TERMINATED;
            case 6:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVICE_UNAVAILABLE;
            case 7:
                return MultimediaSession.ReasonCode.REJECT_REASON_USER_CALL_BLOCK;
            case 8:
                return MultimediaSession.ReasonCode.REJECTED_BY_TIMEOUT;
            case 9:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_NOT_ACCEPTABLE;
            case 10:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQUEST_PENDING;
            case 11:
                return MultimediaSession.ReasonCode.REJECT_REASON_REMOTE_USER_INVALID;
            case 12:
                return MultimediaSession.ReasonCode.REJECT_REASON_NOT_IMPLEMENTED;
            case 13:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVER_TIMEOUT;
            default:
                return MultimediaSession.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaSessionServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState;

        static {
            int[] iArr = new int[ImSession.SessionState.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState = iArr;
            try {
                iArr[ImSession.SessionState.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.STARTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.ESTABLISHED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.CLOSING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.CLOSED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[ImSession.SessionState.FAILED_MEDIA.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            int[] iArr2 = new int[ImError.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = iArr2;
            try {
                iArr2[ImError.BUSY_HERE.ordinal()] = 1;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.REMOTE_PARTY_DECLINED.ordinal()] = 2;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.REMOTE_TEMPORARILY_UNAVAILABLE.ordinal()] = 3;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.INVALID_REQUEST.ordinal()] = 4;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.CONNECTION_RELEASED.ordinal()] = 5;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SERVICE_UNAVAILABLE.ordinal()] = 6;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.FORBIDDEN_NO_WARNING_HEADER.ordinal()] = 7;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SESSION_TIMED_OUT.ordinal()] = 8;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NOT_ACCEPTABLE_HERE.ordinal()] = 9;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.REQUEST_PENDING.ordinal()] = 10;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.REMOTE_USER_INVALID.ordinal()] = 11;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.NOT_IMPLEMENTED.ordinal()] = 12;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[ImError.SERVER_TIMEOUT.ordinal()] = 13;
            } catch (NoSuchFieldError e19) {
            }
        }
    }

    private static MultimediaSession.State translateState(ImSession.SessionState state, ImDirection direction) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[state.ordinal()]) {
            case 1:
                if (direction == ImDirection.OUTGOING) {
                    return MultimediaSession.State.INITIATING;
                }
                return MultimediaSession.State.INVITED;
            case 2:
                if (direction == ImDirection.OUTGOING) {
                    return MultimediaSession.State.RINGING;
                }
                return MultimediaSession.State.ACCEPTING;
            case 3:
                return MultimediaSession.State.STARTED;
            case 4:
                return MultimediaSession.State.ABORTED;
            case 5:
                return MultimediaSession.State.ABORTED;
            case 6:
                return MultimediaSession.State.ABORTED;
            default:
                return MultimediaSession.State.FAILED;
        }
    }
}
