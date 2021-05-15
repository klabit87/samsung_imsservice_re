package com.sec.internal.ims.servicemodules.options;

import android.os.IBinder;
import android.os.RemoteException;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityService;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class CapabilityDiscoveryService extends ICapabilityService.Stub {
    private static int mRegisterToken = 0;
    /* access modifiers changed from: private */
    public Map<String, CallBack> mCapabilityListenerMap = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public Map<String, CallBack> mQueuedCapabilityListener = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public CapabilityDiscoveryModule mServiceModule = null;

    protected static synchronized String getRegisterToken(ICapabilityServiceEventListener listener) {
        String str;
        synchronized (CapabilityDiscoveryService.class) {
            if (mRegisterToken == Integer.MAX_VALUE) {
                mRegisterToken = 100;
            }
            mRegisterToken++;
            str = listener.hashCode() + "$" + mRegisterToken;
        }
        return str;
    }

    public void setServiceModule(ServiceModuleBase service) {
        this.mServiceModule = (CapabilityDiscoveryModule) service;
        if (!this.mQueuedCapabilityListener.isEmpty()) {
            for (CallBack callback : this.mQueuedCapabilityListener.values()) {
                this.mCapabilityListenerMap.put(callback.mToken, callback);
                this.mServiceModule.registerListener(callback.mListener, callback.mPhoneId);
            }
            this.mQueuedCapabilityListener.clear();
        }
    }

    public Capabilities getOwnCapabilities(int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getOwnCapabilities(phoneId);
        }
        return null;
    }

    public Capabilities getCapabilities(ImsUri uri, int refreshType, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilities(uri, CapabilityRefreshType.values()[refreshType], phoneId);
        }
        return null;
    }

    public Capabilities getCapabilitiesByNumber(String number, int refreshType, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilities(number, CapabilityRefreshType.values()[refreshType], false, phoneId);
        }
        return null;
    }

    public Capabilities getCapabilitiesWithDelay(String number, int refreshType, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilities(number, CapabilityRefreshType.values()[refreshType], true, phoneId);
        }
        return null;
    }

    public Capabilities getCapabilitiesWithFeature(String number, int feature, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilities(number, (long) feature, phoneId);
        }
        return null;
    }

    public Capabilities[] getCapabilitiesWithFeatureByUriList(List<ImsUri> uris, int refreshType, int feature, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule == null) {
            return null;
        }
        return capabilityDiscoveryModule.getCapabilities(uris, CapabilityRefreshType.values()[refreshType], (long) feature, phoneId);
    }

    public Capabilities getCapabilitiesById(int id, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilities(id, phoneId);
        }
        return null;
    }

    public Capabilities[] getCapabilitiesByContactId(String contactId, int refreshType, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getCapabilitiesByContactId(contactId, CapabilityRefreshType.values()[refreshType], phoneId);
        }
        return null;
    }

    public Capabilities[] getAllCapabilities(int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.getAllCapabilities(phoneId);
        }
        return null;
    }

    public String registerListener(ICapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        if (listener == null) {
            return null;
        }
        String token = getRegisterToken(listener);
        CallBack callback = new CallBack(listener, phoneId, token);
        if (this.mServiceModule != null) {
            this.mCapabilityListenerMap.put(token, callback);
            this.mServiceModule.registerListener(listener, phoneId);
        } else {
            this.mQueuedCapabilityListener.put(token, callback);
        }
        return token;
    }

    public void registerListenerWithToken(ICapabilityServiceEventListener listener, String token, int phoneId) {
        if (listener != null && token != null) {
            CallBack callback = new CallBack(listener, phoneId, token);
            if (this.mServiceModule != null) {
                this.mCapabilityListenerMap.put(token, callback);
                this.mServiceModule.registerListener(listener, phoneId);
                return;
            }
            this.mQueuedCapabilityListener.put(token, callback);
        }
    }

    public void unregisterListener(String token, int phoneId) throws RemoteException {
        ICapabilityServiceEventListener listener;
        CapabilityDiscoveryModule capabilityDiscoveryModule;
        if (token != null && (listener = removeCallback(token)) != null && (capabilityDiscoveryModule = this.mServiceModule) != null) {
            capabilityDiscoveryModule.unregisterListener(listener, phoneId);
        }
    }

    public void addFakeCapabilityInfo(List<ImsUri> uris, boolean feature, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.addFakeCapabilityInfo(uris, feature, phoneId);
        }
    }

    public boolean isOwnInfoPublished() throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            return capabilityDiscoveryModule.isOwnInfoPublished();
        }
        return false;
    }

    public void registerService(String serviceId, String version) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.registerService(serviceId, version);
        }
    }

    public void deRegisterService(List<String> serviceIdList) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.deRegisterService(serviceIdList);
        }
    }

    public void setUserActivity(boolean isActive, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.setUserActive(isActive, phoneId);
        }
    }

    private ICapabilityServiceEventListener removeCallback(String token) {
        CallBack callback = null;
        if (this.mServiceModule != null) {
            callback = this.mCapabilityListenerMap.remove(token);
        }
        if (!this.mQueuedCapabilityListener.isEmpty()) {
            CallBack tempCallback = this.mQueuedCapabilityListener.remove(token);
            if (callback == null && tempCallback != null) {
                callback = tempCallback;
            }
        }
        if (callback == null) {
            return null;
        }
        ICapabilityServiceEventListener listener = callback.mListener;
        callback.reset();
        return listener;
    }

    private final class CallBack implements IBinder.DeathRecipient {
        final ICapabilityServiceEventListener mListener;
        final int mPhoneId;
        final String mToken;

        CallBack(ICapabilityServiceEventListener listener, int phoneId, String token) {
            this.mListener = listener;
            this.mPhoneId = phoneId;
            this.mToken = token;
            try {
                listener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public void binderDied() {
            reset();
            if (!CapabilityDiscoveryService.this.mQueuedCapabilityListener.isEmpty()) {
                CapabilityDiscoveryService.this.mQueuedCapabilityListener.remove(this.mToken);
            }
            if (CapabilityDiscoveryService.this.mServiceModule != null) {
                CapabilityDiscoveryService.this.mCapabilityListenerMap.remove(this.mToken);
            }
        }

        /* access modifiers changed from: protected */
        public void reset() {
            try {
                this.mListener.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
            }
        }
    }
}
