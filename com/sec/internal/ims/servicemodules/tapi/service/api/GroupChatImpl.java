package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.location.Location;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.chat.GroupChat;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IGroupChat;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GroupChatImpl extends IGroupChat.Stub {
    private static final String LOG_TAG = GroupChatImpl.class.getSimpleName();
    private GroupChat.State mGroupChatState = GroupChat.State.STARTED;
    private IImModule mImModule = null;
    private GroupChat.ReasonCode mReasonCode = GroupChat.ReasonCode.UNSPECIFIED;
    private ImSession mSession = null;
    private boolean mSessionLeaved = false;

    public GroupChatImpl(ImSession session) {
        this.mSession = session;
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
    }

    public GroupChatImpl(String chatId) {
        this.mSession = ImCache.getInstance().getImSession(chatId);
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GroupChatImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status;

        static {
            int[] iArr = new int[ImParticipant.Status.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status = iArr;
            try {
                iArr[ImParticipant.Status.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.INVITED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.ACCEPTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.PENDING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.DECLINED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.GONE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.TIMEOUT.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[ImParticipant.Status.TO_INVITE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    public GroupChat.ParticipantStatus convertStatus(ImParticipant.Status imStatus) {
        GroupChat.ParticipantStatus participantStatus = GroupChat.ParticipantStatus.DISCONNECTED;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[imStatus.ordinal()]) {
            case 1:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 2:
                return GroupChat.ParticipantStatus.INVITED;
            case 3:
            case 4:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 5:
                return GroupChat.ParticipantStatus.DECLINED;
            case 6:
                return GroupChat.ParticipantStatus.DEPARTED;
            case 7:
                return GroupChat.ParticipantStatus.TIMEOUT;
            case 8:
                return GroupChat.ParticipantStatus.INVITING;
            default:
                return GroupChat.ParticipantStatus.DISCONNECTED;
        }
    }

    public long getTimestamp() {
        return 0;
    }

    public String getChatId() {
        return this.mSession.getChatId();
    }

    public String getSubject() {
        return this.mSession.getSubject();
    }

    public Map getParticipants() {
        Map<String, Integer> participants = new HashMap<>();
        for (ImParticipant p : this.mSession.getParticipants()) {
            participants.put(p.getUri().toString(), Integer.valueOf(convertStatus(p.getStatus()).ordinal()));
        }
        return participants;
    }

    public int getDirection() {
        return this.mSession.getDirection().getId();
    }

    public GroupChat.State getState() {
        return this.mGroupChatState;
    }

    public GroupChat.ReasonCode getReasonCode() {
        return this.mReasonCode;
    }

    public void openChat() throws RemoteException {
    }

    public boolean canSendMessage() throws RemoteException {
        return !this.mSessionLeaved;
    }

    public IChatMessage sendMessage(String text) {
        Log.d(LOG_TAG, "start : sendMessage()");
        try {
            IImModule iImModule = this.mImModule;
            String chatId = this.mSession.getChatId();
            EnumSet of = EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED);
            ImMessage imMessage = iImModule.sendMessage(chatId, text, of, "text/plain", System.currentTimeMillis() + "", -1, false, false, false, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null).get();
            if (imMessage == null) {
                return null;
            }
            return new ChatMessageImpl(String.valueOf(imMessage.getId()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    public IChatMessage sendGeoloc(Geoloc geoloc) throws RemoteException {
        Location location = new Location("gps");
        location.setLatitude(geoloc.getLatitude());
        location.setLongitude(geoloc.getLongitude());
        location.setAccuracy(geoloc.getAccuracy());
        ImMessage imMessage = null;
        try {
            Future<ImMessage> imMessageFuture = ImsRegistry.getServiceModuleManager().getGlsModule().shareLocationInChat(this.mSession.getChatId(), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), location, geoloc.getLabel(), (String) null, (String) null, (ImsUri) null, true, (String) null);
            if (imMessageFuture != null) {
                imMessage = imMessageFuture.get();
            }
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not get imMessage with messageId ");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void sendIsComposingEvent(boolean status) {
        Log.d(LOG_TAG, "start : sendIsComposingEvent()");
        this.mSession.sendComposing(status, 1);
    }

    public boolean canAddParticipants() throws RemoteException {
        Capabilities capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
        if (capx != null && !this.mSessionLeaved && capx.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canAddListParticipants(java.util.List<com.gsma.services.rcs.contact.ContactId> r9) throws android.os.RemoteException {
        /*
            r8 = this;
            int r0 = com.sec.internal.helper.SimUtil.getDefaultPhoneId()
            boolean r1 = r8.mSessionLeaved
            r2 = 0
            if (r1 == 0) goto L_0x000a
            return r2
        L_0x000a:
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r1 = com.sec.internal.ims.registry.ImsRegistry.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r1 = r1.getCapabilityDiscoveryModule()
            java.util.Iterator r3 = r9.iterator()
        L_0x0016:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x003a
            java.lang.Object r4 = r3.next()
            com.gsma.services.rcs.contact.ContactId r4 = (com.gsma.services.rcs.contact.ContactId) r4
            java.lang.String r5 = r4.toString()
            int r6 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT
            long r6 = (long) r6
            com.sec.ims.options.Capabilities r5 = r1.getCapabilities((java.lang.String) r5, (long) r6, (int) r0)
            if (r5 == 0) goto L_0x0039
            int r6 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT
            boolean r6 = r5.hasFeature(r6)
            if (r6 != 0) goto L_0x0038
            goto L_0x0039
        L_0x0038:
            goto L_0x0016
        L_0x0039:
            return r2
        L_0x003a:
            r2 = 1
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.GroupChatImpl.canAddListParticipants(java.util.List):boolean");
    }

    public void addParticipants(List<ContactId> participants) {
        Log.d(LOG_TAG, "start : addParticipants()");
        ArrayList<ImsUri> uriList = new ArrayList<>();
        for (ContactId contact : participants) {
            if (contact != null) {
                uriList.add(ImsUri.parse("tel:" + contact.toString()));
            }
        }
        this.mImModule.addParticipants(this.mSession.getChatId(), uriList);
    }

    public int getMaxParticipants() {
        return this.mSession.getMaxParticipantsCount();
    }

    public void leave() throws RemoteException {
        Log.d(LOG_TAG, "start : leave()");
        this.mImModule.closeChat(this.mSession.getChatId());
        this.mSessionLeaved = true;
    }

    public boolean isAllowedToLeave() throws RemoteException {
        return !this.mSessionLeaved;
    }

    public String getRemoteContact() throws RemoteException {
        for (ImParticipant participant : this.mSession.getParticipants()) {
            if (participant.getType() == ImParticipant.Type.CHAIRMAN) {
                return participant.getUri().toString();
            }
        }
        return null;
    }

    public void setComposingStatus(boolean ongoing) throws RemoteException {
        String str = LOG_TAG;
        Log.d(str, "start : setComposingStatus() ongoing=" + ongoing);
        ImSession imSession = this.mSession;
        if (imSession != null) {
            imSession.sendComposing(ongoing, 3);
        }
    }
}
