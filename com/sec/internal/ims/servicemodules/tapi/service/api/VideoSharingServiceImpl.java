package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.video.IVideoPlayer;
import com.gsma.services.rcs.sharing.video.IVideoSharing;
import com.gsma.services.rcs.sharing.video.IVideoSharingListener;
import com.gsma.services.rcs.sharing.video.IVideoSharingService;
import com.gsma.services.rcs.sharing.video.VideoSharingServiceConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.VideoShare;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VideoSharingServiceImpl extends IVideoSharingService.Stub implements IRegistrationStatusBroadcaster {
    private static String LOG_TAG = VideoSharingServiceImpl.class.getName();
    private static Hashtable<String, IVideoSharing> videoSharingSessions = new Hashtable<>();
    private static IVideoShareModule vshModule = null;
    private RemoteCallbackList<IVideoSharingListener> eventListeners = new RemoteCallbackList<>();
    private Object lock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> serviceListeners = new RemoteCallbackList<>();

    public VideoSharingServiceImpl(IVideoShareModule service) {
        vshModule = service;
    }

    public static IVideoShareModule getModule() {
        return vshModule;
    }

    protected static void addVideoSharingSession(String sessionId, VideoSharingImpl session) {
        String str = LOG_TAG;
        Log.d(str, "Add a vsh session (size=" + videoSharingSessions.size() + ") : sessionid = " + sessionId);
        videoSharingSessions.put(sessionId, session);
    }

    protected static void removeVideoSharingSession(String sessionId) {
        String str = LOG_TAG;
        Log.d(str, "remove a vsh session (size=" + videoSharingSessions.size() + ") : sessionid = " + sessionId);
        videoSharingSessions.remove(sessionId);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        boolean isReg = manager.getRegistrationInfo()[0].hasService("vs");
        String str = LOG_TAG;
        Log.d(str, "isServiceRegistered() = " + isReg);
        return isReg;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.lock) {
            this.serviceListeners.register(listener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.lock) {
            this.serviceListeners.unregister(listener);
        }
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.lock) {
            int N = this.serviceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.serviceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "Can't notify listener", e);
                    }
                } else {
                    this.serviceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.serviceListeners.finishBroadcast();
        }
    }

    public VideoSharingServiceConfiguration getConfiguration() throws ServerApiException {
        return new VideoSharingServiceConfiguration((long) vshModule.getMaxDurationTime());
    }

    public IVideoSharing getVideoSharing(String sharingId) throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "Get video sharing session " + sharingId);
        return videoSharingSessions.get(sharingId);
    }

    public void deleteVideoSharing(String sharingId) throws ServerApiException {
        String str = LOG_TAG;
        Log.i(str, "delete video sharing session " + sharingId);
        List<String> sharingIds = new ArrayList<>();
        String contacturi = null;
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            try {
                contacturi = videoSharingSessions.get(sharingId).getRemoteContact();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            ContactId contact = new ContactId(contacturi);
            sharingIds.add(sharingId);
            videoSharingSessions.remove(sharingId);
            handleVideoSessionDeleted(contact, sharingIds);
        }
    }

    public VideoSharingImpl getVideoSharingByID(String sharingId) {
        String str = LOG_TAG;
        Log.i(str, "Get video sharing session " + sharingId + "; videoSharingSessions = " + videoSharingSessions.size());
        return videoSharingSessions.get(sharingId);
    }

    public List<IBinder> getVideoSharings() throws ServerApiException {
        ArrayList<IBinder> result = new ArrayList<>(videoSharingSessions.size());
        Enumeration<IVideoSharing> e = videoSharingSessions.elements();
        while (e.hasMoreElements()) {
            result.add(e.nextElement().asBinder());
        }
        return result;
    }

    public void deleteVideoSharings() throws ServerApiException {
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            videoSharingSessions.clear();
        }
    }

    public void addEventListener(IVideoSharingListener listener) throws ServerApiException {
        synchronized (this.lock) {
            this.eventListeners.register(listener);
        }
    }

    public void removeEventListener(IVideoSharingListener listener) throws ServerApiException {
        synchronized (this.lock) {
            this.eventListeners.unregister(listener);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    public IVideoSharing shareVideo(ContactId contact, IVideoPlayer player) throws ServerApiException {
        if (contact == null) {
            Log.e(LOG_TAG, "Cannot initiate a live video session, contact is null");
            return null;
        }
        String str = LOG_TAG;
        Log.i(str, "Initiate a live video session with contact " + contact.toString());
        if (player != null) {
            try {
                VideoShare session = vshModule.createShare(ImsUri.parse(contact.toString()), VshIntents.LIVE_VIDEO_CONTENTPATH).get();
                if (session != null) {
                    VideoSharingImpl sessionApi = new VideoSharingImpl(session, player);
                    addVideoSharingSession(String.valueOf(session.getContent().shareId), sessionApi);
                    return sessionApi;
                }
                throw new ServerApiException("session is null");
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (ExecutionException e2) {
                e2.printStackTrace();
                return null;
            }
        } else {
            throw new ServerApiException("Missing video player");
        }
    }

    public void deleteVideoSharingsByContactId(ContactId contact) throws RemoteException {
        if (contact == null) {
            Log.e(LOG_TAG, "Cannot delete video sharing session, contact is null");
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "delete video sharing session " + contact.toString());
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            videoSharingSessions.remove(contact.toString());
        }
    }

    public void handleVideoSessionDeleted(ContactId contact, List<String> sharingIds) {
        String str = LOG_TAG;
        Log.d(str, "handleVideoSessionDeleted: contactid = " + contact + " ,sharingIds = " + sharingIds);
        synchronized (this.lock) {
            int N = this.eventListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    this.eventListeners.getBroadcastItem(i).onDeleted(contact, sharingIds);
                } catch (RemoteException e) {
                    Log.i(LOG_TAG, "Can't notify listener", e);
                }
            }
            this.eventListeners.finishBroadcast();
        }
    }
}
