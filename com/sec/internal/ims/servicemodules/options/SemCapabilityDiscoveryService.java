package com.sec.internal.ims.servicemodules.options;

import android.os.IBinder;
import android.os.RemoteException;
import com.samsung.android.ims.options.SemCapabilities;
import com.samsung.android.ims.options.SemCapabilityServiceEventListener;
import com.samsung.android.ims.options.SemImsCapabilityService;
import com.samsung.android.ims.util.SemImsUri;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class SemCapabilityDiscoveryService extends SemImsCapabilityService.Stub {
    private Map<String, CapabilityServiceEventCallBack> mCapServiceEventCallbacks = new ConcurrentHashMap();
    private CapabilityDiscoveryService mCapabilityService = null;
    private Map<String, CapabilityServiceEventCallBack> mQueuedCapabilityListener = new HashMap();

    public void setServiceModule(CapabilityDiscoveryService capabilityService) {
        this.mCapabilityService = capabilityService;
        if (!this.mQueuedCapabilityListener.isEmpty()) {
            for (CapabilityServiceEventCallBack callback : this.mQueuedCapabilityListener.values()) {
                String token = callback.mToken;
                int phoneId = callback.mPhoneId;
                this.mCapServiceEventCallbacks.put(token, callback);
                this.mCapabilityService.registerListenerWithToken(callback, token, phoneId);
            }
            this.mQueuedCapabilityListener.clear();
        }
    }

    public SemCapabilities getOwnCapabilities(int phoneId) throws RemoteException {
        CapabilityDiscoveryService capabilityDiscoveryService = this.mCapabilityService;
        if (capabilityDiscoveryService != null) {
            return buildSemCapabilities(capabilityDiscoveryService.getOwnCapabilities(phoneId));
        }
        return null;
    }

    public SemCapabilities getCapabilities(String uri, int refreshType, int phoneId) throws RemoteException {
        if (this.mCapabilityService == null) {
            return null;
        }
        return buildSemCapabilities(this.mCapabilityService.getCapabilities(ImsUri.parse(uri), refreshType, phoneId));
    }

    public SemCapabilities getCapabilitiesByNumber(String number, int refreshType, boolean delay, int phoneId) throws RemoteException {
        Capabilities capabilities;
        Capabilities capabilities2 = this.mCapabilityService;
        if (capabilities2 == null) {
            return null;
        }
        if (delay) {
            capabilities = capabilities2.getCapabilitiesWithDelay(number, refreshType, phoneId);
        } else {
            capabilities = capabilities2.getCapabilitiesByNumber(number, refreshType, phoneId);
        }
        return buildSemCapabilities(capabilities);
    }

    public SemCapabilities[] getCapabilitiesByContactId(String contactId, int refreshType, int phoneId) throws RemoteException {
        CapabilityDiscoveryService capabilityDiscoveryService = this.mCapabilityService;
        if (capabilityDiscoveryService != null) {
            return buildSemCapabilitiesList(capabilityDiscoveryService.getCapabilitiesByContactId(contactId, refreshType, phoneId));
        }
        return null;
    }

    public String registerListener(SemCapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        String token;
        CapabilityServiceEventCallBack capServiceEventCallback = new CapabilityServiceEventCallBack(listener, phoneId);
        CapabilityDiscoveryService capabilityDiscoveryService = this.mCapabilityService;
        if (capabilityDiscoveryService != null) {
            token = capabilityDiscoveryService.registerListener(capServiceEventCallback, phoneId);
            if (token != null) {
                capServiceEventCallback.mToken = token;
                this.mCapServiceEventCallbacks.put(token, capServiceEventCallback);
            } else {
                capServiceEventCallback.reset();
                return token;
            }
        } else {
            token = CapabilityDiscoveryService.getRegisterToken(capServiceEventCallback);
            if (token != null) {
                capServiceEventCallback.mToken = token;
                this.mQueuedCapabilityListener.put(token, capServiceEventCallback);
            }
        }
        return token;
    }

    public void unregisterListener(String token, int phoneId) throws RemoteException {
        CapabilityServiceEventCallBack callback;
        if (token != null) {
            CapabilityDiscoveryService capabilityDiscoveryService = this.mCapabilityService;
            if (capabilityDiscoveryService != null) {
                capabilityDiscoveryService.unregisterListener(token, phoneId);
                CapabilityServiceEventCallBack callback2 = this.mCapServiceEventCallbacks.remove(token);
                if (callback2 != null) {
                    callback2.reset();
                }
            }
            if (!this.mQueuedCapabilityListener.isEmpty() && (callback = this.mQueuedCapabilityListener.remove(token)) != null) {
                callback.reset();
            }
        }
    }

    /* access modifiers changed from: private */
    public SemImsUri buildSemImsUri(ImsUri uri) {
        if (uri == null) {
            return null;
        }
        SemImsUri semImsUri = new SemImsUri();
        semImsUri.setUser(uri.getUser());
        semImsUri.setMsisdn(uri.getMsisdn());
        semImsUri.setUriType(uri.getUriType().name());
        semImsUri.setScheme(uri.getScheme());
        return semImsUri;
    }

    /* access modifiers changed from: private */
    public SemCapabilities buildSemCapabilities(Capabilities capa) {
        if (capa != null) {
            return SemCapabilities.getBuilder().setIsAvailable(capa.isAvailable()).setFeature(capa.getFeature()).setAvailableFeatures(capa.getAvailableFeatures()).setIsExpired(capa.getExpired()).setLegacyLatching(capa.getLegacyLatching()).setTimestamp(capa.getTimestamp()).setExtFeature(capa.getExtFeature()).setBotServiceId(capa.getBotServiceId()).build();
        }
        return null;
    }

    private SemCapabilities[] buildSemCapabilitiesList(Capabilities[] capaList) {
        List<SemCapabilities> semCapList = new ArrayList<>();
        if (capaList == null) {
            return null;
        }
        for (Capabilities capa : capaList) {
            semCapList.add(buildSemCapabilities(capa));
        }
        return (SemCapabilities[]) semCapList.toArray(new SemCapabilities[semCapList.size()]);
    }

    private class CapabilityServiceEventCallBack extends ICapabilityServiceEventListener.Stub implements IBinder.DeathRecipient {
        SemCapabilityServiceEventListener mListener;
        int mPhoneId;
        String mToken = null;

        public CapabilityServiceEventCallBack(SemCapabilityServiceEventListener listener, int phoneId) {
            this.mListener = listener;
            this.mPhoneId = phoneId;
            try {
                listener.asBinder().linkToDeath(this, 0);
            } catch (RemoteException e) {
            }
        }

        public void onOwnCapabilitiesChanged() {
            SemCapabilityServiceEventListener semCapabilityServiceEventListener = this.mListener;
            if (semCapabilityServiceEventListener != null) {
                try {
                    semCapabilityServiceEventListener.onOwnCapabilitiesChanged();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onCapabilitiesChanged(List<ImsUri> uris, Capabilities capa) {
            SemImsUri semUri = SemCapabilityDiscoveryService.this.buildSemImsUri(uris.get(0));
            SemCapabilities semCapa = SemCapabilityDiscoveryService.this.buildSemCapabilities(capa);
            SemCapabilityServiceEventListener semCapabilityServiceEventListener = this.mListener;
            if (semCapabilityServiceEventListener != null) {
                try {
                    semCapabilityServiceEventListener.onCapabilitiesChanged(semUri, semCapa);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onMultipleCapabilitiesChanged(List<ImsUri> list, List<Capabilities> list2) {
        }

        public void onCapabilityAndAvailabilityPublished(int errorCode) {
            SemCapabilityServiceEventListener semCapabilityServiceEventListener = this.mListener;
            if (semCapabilityServiceEventListener != null) {
                try {
                    semCapabilityServiceEventListener.onCapabilityAndAvailabilityPublished(errorCode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void binderDied() {
            try {
                SemCapabilityDiscoveryService.this.unregisterListener(this.mToken, this.mPhoneId);
            } catch (RemoteException e) {
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
