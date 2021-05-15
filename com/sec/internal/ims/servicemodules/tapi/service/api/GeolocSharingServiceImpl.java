package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingListener;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GeolocSharingEventBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GeolocSharingServiceImpl extends IGeolocSharingService.Stub implements IMessageEventListener, IFtEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = GeolocSharingServiceImpl.class.getSimpleName();
    private GeolocSharingEventBroadcaster mGeolocSharingEventBroadcaster = null;
    private IGlsModule mGlsModule = null;
    private final Hashtable<String, IGeolocSharing> mGshSessions = new Hashtable<>();
    private final Object mLock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public GeolocSharingServiceImpl(Context ctx, IGlsModule service) {
        this.mGlsModule = service;
        this.mGeolocSharingEventBroadcaster = new GeolocSharingEventBroadcaster(ctx);
        this.mGlsModule.registerMessageEventListener(ImConstants.Type.LOCATION, this);
        this.mGlsModule.registerFtEventListener(ImConstants.Type.LOCATION, this);
    }

    private void addGeolocSharingSession(GeolocSharingImpl session) {
        try {
            this.mGshSessions.put(session.getSharingId(), session);
        } catch (ServerApiException e) {
            e.printStackTrace();
        }
    }

    private void removeGeolocSharingSession(String sessionId) {
        this.mGshSessions.remove(sessionId);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        for (ImsRegistration reg : manager.getRegistrationInfo()) {
            if (reg.hasService("gls")) {
                return true;
            }
        }
        return false;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(listener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(listener);
        }
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.mLock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public IGeolocSharing shareGeoloc(ContactId contact, Geoloc geoloc) throws ServerApiException {
        ImsUri uri = ImsUri.parse("tel:" + contact.toString());
        Location location = new Location("gps");
        if (geoloc == null) {
            return null;
        }
        location.setLatitude(geoloc.getLatitude());
        location.setLongitude(geoloc.getLongitude());
        location.setAccuracy(geoloc.getAccuracy());
        IGlsModule iGlsModule = this.mGlsModule;
        if (iGlsModule == null) {
            Log.e(LOG_TAG, "GLS module is not created");
            return null;
        }
        Future<FtMessage> future = iGlsModule.createInCallLocationShare((String) null, uri, EnumSet.of(NotificationStatus.DELIVERED), location, geoloc.getLabel(), (String) null, false, false);
        if (future == null) {
            Log.e(LOG_TAG, "sharing geolocation  failed, return null!");
            return null;
        }
        try {
            FtMessage msg = future.get();
            if (msg == null) {
                Log.e(LOG_TAG, "sharing geolocation  failed, return null!");
                return null;
            }
            GeolocSharingImpl sessionApi = new GeolocSharingImpl(msg, this.mGlsModule);
            addGeolocSharingSession(sessionApi);
            return sessionApi;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public List<IBinder> getGeolocSharings() throws ServerApiException {
        Log.d(LOG_TAG, "Get geoloc sharing sessions");
        ArrayList<IBinder> result = new ArrayList<>(this.mGshSessions.size());
        Enumeration<IGeolocSharing> e = this.mGshSessions.elements();
        while (e.hasMoreElements()) {
            result.add(e.nextElement().asBinder());
        }
        return result;
    }

    public IGeolocSharing getGeolocSharing(String sharingId) throws ServerApiException {
        return this.mGshSessions.get(sharingId);
    }

    public void addEventListener(IGeolocSharingListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.addEventListener(listener);
        }
    }

    public void removeEventListener(IGeolocSharingListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.removeEventListener(listener);
        }
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public void deleteAllGeolocSharings() throws ServerApiException {
        Map<String, Set<String>> msgs = getGeoMessage("content_type ='application/vnd.gsma.rcspushlocation+xml'");
        if (msgs == null) {
            Log.e(LOG_TAG, "deleteAllGeolocSharings: Message not found.");
            return;
        }
        List<String> listMsgs = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
            listMsgs.addAll(entry.getValue());
            ContactId contact = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId(entry.getKey())));
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastDeleted(contact, new ArrayList(entry.getValue()));
            }
        }
        this.mGlsModule.deleteGeolocSharings(listMsgs);
        for (String id : listMsgs) {
            removeGeolocSharingSession(id);
        }
    }

    public void deleteGeolocSharingsByContactId(ContactId contact) throws ServerApiException {
        if (contact != null) {
            Set<ImsUri> uris = new HashSet<>();
            uris.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contact.toString())));
            ImSession session = ImCache.getInstance().getImSessionByParticipants(uris, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
            if (session == null) {
                Log.e(LOG_TAG, "deleteGeolocSharingsByContactId: No session for geoloc");
                return;
            }
            Map<String, Set<String>> msgs = getGeoMessage("is_filetransfer = 1 and chat_id = '" + session.getChatId() + "' and " + "content_type" + " ='" + MIMEContentType.LOCATION_PUSH + "'");
            if (msgs == null) {
                Log.e(LOG_TAG, "deleteGeolocSharingsByContactId: Message not found.");
                return;
            }
            List<String> listMsgs = new ArrayList<>();
            for (Map.Entry<String, Set<String>> entry : msgs.entrySet()) {
                listMsgs.addAll(entry.getValue());
                synchronized (this.mLock) {
                    this.mGeolocSharingEventBroadcaster.broadcastDeleted(contact, new ArrayList(entry.getValue()));
                }
            }
            this.mGlsModule.deleteGeolocSharings(listMsgs);
            for (String id : listMsgs) {
                removeGeolocSharingSession(id);
            }
        }
    }

    public void deleteGeolocSharingBySharingId(String sharingId) throws ServerApiException {
        List<String> list = new ArrayList<>();
        list.add(sharingId);
        this.mGlsModule.deleteGeolocSharings(list);
        GeolocSharingImpl geoSharing = getGeolocSharing(sharingId);
        if (geoSharing == null) {
            String str = LOG_TAG;
            Log.e(str, "deleteGeolocSharingBySharingId, id:" + sharingId + ", GeolocSharingImpl not found.");
            return;
        }
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.broadcastDeleted(geoSharing.getRemoteContact(), list);
        }
        removeGeolocSharingSession(sharingId);
    }

    private String getImSessionByChatId(String chatId) {
        ImSession imSession = ImCache.getInstance().getImSession(chatId);
        if (imSession == null) {
            return null;
        }
        return imSession.getParticipantsString().get(0);
    }

    private Map<String, Set<String>> getGeoMessage(String selection) {
        ImCache cache = ImCache.getInstance();
        Map<String, Set<String>> msgs = new TreeMap<>();
        Cursor cursorDb = cache.queryMessages(new String[]{"_id", "chat_id"}, selection, (String[]) null, (String) null);
        if (cursorDb != null) {
            try {
                if (cursorDb.getCount() == 0) {
                    if (cursorDb != null) {
                        cursorDb.close();
                    }
                    return null;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        while (cursorDb != null) {
            if (!cursorDb.moveToNext()) {
                break;
            }
            String chatIdString = cursorDb.getString(cursorDb.getColumnIndexOrThrow("chat_id"));
            if (cache.getImSession(chatIdString) != null) {
                String idString = String.valueOf(cursorDb.getInt(cursorDb.getColumnIndexOrThrow("_id")));
                Set<String> setMsgs = msgs.get(chatIdString);
                if (setMsgs == null) {
                    Set<String> setMsgs2 = new HashSet<>();
                    setMsgs2.add(idString);
                    msgs.put(chatIdString, setMsgs2);
                } else {
                    setMsgs.add(idString);
                }
            }
        }
        if (cursorDb != null) {
            cursorDb.close();
        }
        return msgs;
        throw th;
    }

    private ContactId getContactId(FtMessage msg) {
        return new ContactId(msg.getRemoteUri().getMsisdn());
    }

    private String getSharingId(FtMessage msg) {
        return String.valueOf(msg.getId());
    }

    public static GeolocSharing.ReasonCode translateToReasonCode(CancelReason code) {
        String str = LOG_TAG;
        Log.d(str, "translateToReasonCode(), CancelReason: " + code);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[code.ordinal()]) {
            case 1:
            case 2:
                return GeolocSharing.ReasonCode.ABORTED_BY_SYSTEM;
            case 3:
                return GeolocSharing.ReasonCode.ABORTED_BY_USER;
            case 4:
                return GeolocSharing.ReasonCode.ABORTED_BY_REMOTE;
            case 5:
                return GeolocSharing.ReasonCode.REJECTED_BY_REMOTE;
            case 6:
                return GeolocSharing.ReasonCode.FAILED_SHARING;
            default:
                return GeolocSharing.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;

        static {
            int[] iArr = new int[CancelReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = iArr;
            try {
                iArr[CancelReason.TIME_OUT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_SYSTEM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_USER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_REMOTE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REJECTED_BY_REMOTE.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.DEVICE_UNREGISTERED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NOT_AUTHORIZED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_BLOCKED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.ERROR.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.VALIDITY_EXPIRED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.INVALID_REQUEST.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_USER_INVALID.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NO_RESPONSE.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.FORBIDDEN_NO_RETRY_FALLBACK.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONTENT_REACHED_DOWNSIZE.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.LOCALLY_ABORTED.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONNECTION_RELEASED.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.UNKNOWN.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
        }
    }

    private void notifyStateChanged(FtMessage msg, GeolocSharing.State state, GeolocSharing.ReasonCode reason) {
        String str = LOG_TAG;
        Log.d(str, "notifyStateChanged state=" + state + ", reason=" + reason);
        if (msg.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingStateChanged(getContactId(msg), getSharingId(msg), state, reason);
            }
        }
    }

    public void handleGeolocSharingProgress(FtMessage msg) {
        String str = LOG_TAG;
        Log.d(str, "handleSharingProgress id:" + msg.getId() + "  progress:" + ((msg.getTransferredBytes() * 100) / msg.getFileSize()) + "%.");
        if (msg.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingprogress(getContactId(msg), getSharingId(msg), msg.getTransferredBytes(), msg.getFileSize());
            }
        }
    }

    public void onFileTransferCreated(FtMessage msg) {
        if (this.mGshSessions.containsKey(String.valueOf(msg.getId()))) {
            this.mGlsModule.startLocationShareInCall((long) msg.getId());
            notifyStateChanged(msg, GeolocSharing.State.INITIATING, GeolocSharing.ReasonCode.UNSPECIFIED);
        }
    }

    public void onFileTransferAttached(FtMessage msg) {
    }

    public void onFileTransferReceived(FtMessage msg) {
        addGeolocSharingSession(new GeolocSharingImpl(msg, this.mGlsModule));
        notifyStateChanged(msg, GeolocSharing.State.INVITED, GeolocSharing.ReasonCode.UNSPECIFIED);
        this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingInvitation(getSharingId(msg));
    }

    public void onTransferProgressReceived(FtMessage msg) {
        handleGeolocSharingProgress(msg);
        if (msg.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingprogress(getContactId(msg), getSharingId(msg), msg.getTransferredBytes(), msg.getFileSize());
            }
        }
    }

    public void onTransferStarted(FtMessage msg) {
        notifyStateChanged(msg, GeolocSharing.State.STARTED, GeolocSharing.ReasonCode.UNSPECIFIED);
    }

    public void onTransferCompleted(FtMessage msg) {
        notifyStateChanged(msg, GeolocSharing.State.TRANSFERRED, GeolocSharing.ReasonCode.UNSPECIFIED);
    }

    public void onTransferCanceled(FtMessage msg) {
        CancelReason cancelReason = msg.getCancelReason();
        GeolocSharing.ReasonCode reasonCode = GeolocSharing.ReasonCode.UNSPECIFIED;
        if (cancelReason != null) {
            reasonCode = translateToReasonCode(cancelReason);
        }
        notifyStateChanged(msg, GeolocSharing.State.ABORTED, reasonCode);
    }

    public void onImdnNotificationReceived(FtMessage msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
    }

    public void onFileResizingNeeded(FtMessage msg, long resizeLimit) {
    }

    public void onCancelRequestFailed(FtMessage msg) {
    }

    public void onMessageSendResponseTimeout(MessageBase msg) {
    }

    public void onMessageSendResponse(MessageBase msg) {
    }

    public void onMessageReceived(MessageBase msg, ImSession session) {
    }

    public void onMessageSendingSucceeded(MessageBase msg) {
    }

    public void onMessageSendingFailed(MessageBase msg, IMnoStrategy.StrategyResponse reason, Result result) {
    }

    public void onMessageSendResponseFailed(String chatId, int messageNumber, int reasoncode, String requestMessageId) {
    }

    public void onImdnNotificationReceived(MessageBase msg, ImsUri remoteUri, NotificationStatus status, boolean isGroupChat) {
    }
}
