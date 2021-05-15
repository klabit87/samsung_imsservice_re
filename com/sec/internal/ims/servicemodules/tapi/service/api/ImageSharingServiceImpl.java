package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.image.IImageSharing;
import com.gsma.services.rcs.sharing.image.IImageSharingListener;
import com.gsma.services.rcs.sharing.image.IImageSharingService;
import com.gsma.services.rcs.sharing.image.ImageSharingServiceConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.IImageShareEventListener;
import com.sec.internal.ims.servicemodules.csh.ImageShare;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImageSharingServiceImpl extends IImageSharingService.Stub implements IRegistrationStatusBroadcaster, IImageShareEventListener {
    private static final String LOG_TAG = ImageSharingServiceImpl.class.getSimpleName();
    private IImageShareModule ishModule = null;
    private RemoteCallbackList<IImageSharingListener> mImageSharingListeners = new RemoteCallbackList<>();
    private Object mIshListenerLock = new Object();
    private Hashtable<String, IImageSharing> mIshSessionsMap = new Hashtable<>();
    private Object mServiceListenerlock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public ImageSharingServiceImpl(IImageShareModule service) {
        this.ishModule = service;
        service.registerImageShareEventListener(this);
    }

    /* access modifiers changed from: package-private */
    public void addImageSharingSession(String sharedId, ImageSharingImpl session) {
        this.mIshSessionsMap.put(sharedId, session);
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        synchronized (this.mServiceListenerlock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (Exception e) {
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public void notifyImageSharingProgress(String sharedId, long currentSize) {
        synchronized (this.mIshListenerLock) {
            ImageSharingImpl ishSession = this.mIshSessionsMap.get(sharedId);
            if (ishSession == null) {
                Log.d(LOG_TAG, "notifyImageSharingProgress(): session is null");
                return;
            }
            ContactId contact = ishSession.getRemoteContact();
            String sharingId = ishSession.getSharingId();
            long totalSize = ishSession.getFileSize();
            int N = this.mImageSharingListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    this.mImageSharingListeners.getBroadcastItem(i).onProgressUpdate(contact, sharingId, currentSize, totalSize);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
            this.mImageSharingListeners.finishBroadcast();
        }
    }

    public boolean isServiceRegistered() {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        boolean isReg = manager.getRegistrationInfo()[0].hasService("is");
        String str = LOG_TAG;
        Log.d(str, "isServiceRegistered() = " + isReg);
        return isReg;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.mServiceListenerlock) {
            this.mServiceListeners.register(listener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener listener) {
        synchronized (this.mServiceListenerlock) {
            this.mServiceListeners.unregister(listener);
        }
    }

    public ImageSharingServiceConfiguration getConfiguration() {
        return new ImageSharingServiceConfiguration(this.ishModule.getMaxSize(), this.ishModule.getWarnSize());
    }

    public List<IBinder> getImageSharings() throws RemoteException {
        try {
            ArrayList<IBinder> result = new ArrayList<>(this.mIshSessionsMap.size());
            Enumeration<IImageSharing> e = this.mIshSessionsMap.elements();
            while (e.hasMoreElements()) {
                result.add(e.nextElement().asBinder());
            }
            return result;
        } catch (Exception e2) {
            throw new ServerApiException(e2.getMessage());
        }
    }

    public IImageSharing getImageSharing(String sharingId) throws RemoteException {
        return this.mIshSessionsMap.get(sharingId);
    }

    public int getServiceVersion() throws RemoteException {
        return 0;
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    public IImageSharing shareImage(ContactId contact, String fileUri) throws RemoteException {
        try {
            ImageShare session = this.ishModule.createShare(ImsUri.parse("tel:" + contact), fileUri).get();
            if (session != null) {
                ImageSharingImpl sessionImpl = new ImageSharingImpl(session);
                String str = LOG_TAG;
                Log.d(str, "shareImage: sharingId = " + sessionImpl.getSharingId());
                addImageSharingSession(sessionImpl.getSharingId(), sessionImpl);
                return sessionImpl;
            }
            throw new RemoteException("session is null");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void addEventListener(IImageSharingListener listener) throws RemoteException {
        synchronized (this.mIshListenerLock) {
            this.mImageSharingListeners.register(listener);
        }
    }

    public void removeEventListener(IImageSharingListener listener) throws RemoteException {
        synchronized (this.mIshListenerLock) {
            this.mImageSharingListeners.unregister(listener);
        }
    }

    public void deleteAllImageSharings() throws RemoteException {
        try {
            Enumeration<IImageSharing> e = this.mIshSessionsMap.elements();
            while (e.hasMoreElements()) {
                e.nextElement().abortSharing();
            }
            this.mIshSessionsMap.clear();
        } catch (Exception e2) {
            throw new ServerApiException(e2.getMessage());
        }
    }

    public void deleteImageSharings(ContactId contact) throws RemoteException {
        Iterator<String> it = (Iterator) this.mIshSessionsMap.keySet();
        while (it.hasNext()) {
            String sharingid = it.next();
            IImageSharing ish = this.mIshSessionsMap.get(sharingid);
            if (contact.equals(ish.getRemoteContact())) {
                ish.abortSharing();
                this.mIshSessionsMap.remove(sharingid);
            }
        }
    }

    public void deleteImageSharing(String sharingId) throws RemoteException {
        IImageSharing ish = this.mIshSessionsMap.get(sharingId);
        if (ish != null) {
            ish.abortSharing();
            this.mIshSessionsMap.remove(sharingId);
        }
    }

    public void onIshTransferProgressEvent(String shareId, long progress) {
        notifyImageSharingProgress(shareId, progress);
    }
}
