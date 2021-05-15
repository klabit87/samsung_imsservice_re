package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.location.Location;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IOneToOneChat;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChatImpl extends IOneToOneChat.Stub {
    private static final String LOG_TAG = ChatImpl.class.getSimpleName();
    private ContactId contact;
    private IGlsModule mGlsModule = null;
    private IImModule mImModule = null;
    private ImSession mSession = null;

    public ChatImpl(String contact2, ImSession session, IImModule module) {
        this.contact = new ContactId(contact2);
        this.mSession = session;
        this.mImModule = module;
        this.mGlsModule = ImsRegistry.getServiceModuleManager().getGlsModule();
    }

    public void openChat() throws ServerApiException {
    }

    public void resendMessage(String msgId) throws ServerApiException {
        Log.d(LOG_TAG, "start : resendMessage()");
        this.mImModule.resendMessage(Integer.valueOf(msgId).intValue());
    }

    public ImSession getCoreSession() {
        return this.mSession;
    }

    public ContactId getRemoteContact() throws ServerApiException {
        return this.contact;
    }

    /* Debug info: failed to restart local var, previous not found, register: 22 */
    public IChatMessage sendMessage(String message) throws ServerApiException {
        Log.d(LOG_TAG, "start : sendMessage()");
        try {
            if (this.mImModule.getImSession(this.mSession.getChatId()) == null) {
                ArrayList<ImsUri> uriList = new ArrayList<>();
                uriList.add(ImsUri.parse("tel:" + this.contact.toString()));
                this.mSession = this.mImModule.createChat(uriList, ChatServiceImpl.SUBJECT, "text/plain", -1, (String) null).get();
            }
            ImMessage imMessage = this.mImModule.sendMessage(this.mSession.getChatId(), message, EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), "text/plain", (String) null, -1, false, false, false, (String) null, (String) null, (String) null, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null).get();
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not make a message");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public IChatMessage sendGeoloc(Geoloc geoloc) throws ServerApiException {
        Log.d(LOG_TAG, "start : send Geolocation Message()");
        Location location = new Location("gps");
        location.setLatitude(geoloc.getLatitude());
        location.setLongitude(geoloc.getLongitude());
        location.setAccuracy(geoloc.getAccuracy());
        ImMessage imMessage = null;
        try {
            Future<ImMessage> imMessageFuture = this.mGlsModule.shareLocationInChat(this.mSession.getChatId(), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), location, geoloc.getLabel(), (String) null, (String) null, this.mSession.getRemoteUri(), false, (String) null);
            if (imMessageFuture != null) {
                imMessage = imMessageFuture.get();
            }
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not make a message");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void sendIsComposingEvent(boolean status) throws ServerApiException {
        Log.d(LOG_TAG, "start : sendIsComposingEvent()");
        this.mSession.sendComposing(status, 3);
    }

    public boolean isAllowedToSendMessage() throws ServerApiException {
        Capabilities capx = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
        return capx != null && capx.hasFeature(Capabilities.FEATURE_CHAT_CPM);
    }

    public void setComposingStatus(boolean ongoing) throws RemoteException {
        Log.d(LOG_TAG, "start : setComposingStatus()");
        ImSession imSession = this.mSession;
        if (imSession != null) {
            imSession.sendComposing(ongoing, 3);
        }
    }
}
