package com.sec.internal.ims.servicemodules.options;

import android.os.RemoteException;
import android.util.Log;
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
import java.util.concurrent.ConcurrentHashMap;

public class SemCapabilityDiscoveryService extends SemImsCapabilityService.Stub {
    private Map<Integer, Map<SemCapabilityServiceEventListener, CapabilityServiceEventCallBack>> mCapServiceEventCallbacks = new ConcurrentHashMap();
    private CapabilityDiscoveryService mCapabilityService = null;
    private Map<SemCapabilityServiceEventListener, Integer> mQueuedCapabilityListener = new HashMap();

    public void setServiceModule(CapabilityDiscoveryService capabilityService) {
        this.mCapabilityService = capabilityService;
        if (!this.mQueuedCapabilityListener.isEmpty()) {
            try {
                for (Map.Entry<SemCapabilityServiceEventListener, Integer> entry : this.mQueuedCapabilityListener.entrySet()) {
                    registerListener(entry.getKey(), entry.getValue().intValue());
                }
                this.mQueuedCapabilityListener.clear();
            } catch (RemoteException e) {
                Log.d("SemCapabilityDiscoveryService", "registerListener failed. RemoteException: " + e);
            }
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

    public void registerListener(SemCapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        if (this.mCapabilityService == null) {
            this.mQueuedCapabilityListener.put(listener, Integer.valueOf(phoneId));
        } else if (!this.mCapServiceEventCallbacks.containsKey(Integer.valueOf(phoneId)) || !this.mCapServiceEventCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
            CapabilityServiceEventCallBack capServiceEventCallback = new CapabilityServiceEventCallBack(listener);
            if (!this.mCapServiceEventCallbacks.containsKey(Integer.valueOf(phoneId))) {
                this.mCapServiceEventCallbacks.put(Integer.valueOf(phoneId), new ConcurrentHashMap());
            }
            this.mCapServiceEventCallbacks.get(Integer.valueOf(phoneId)).put(listener, capServiceEventCallback);
            this.mCapabilityService.registerListener(capServiceEventCallback, phoneId);
        } else {
            Log.d("SemCapabilityDiscoveryService", "registerListener : listener has already registered");
        }
    }

    public void unregisterListener(SemCapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        if (this.mCapabilityService != null) {
            if (this.mCapServiceEventCallbacks.containsKey(Integer.valueOf(phoneId)) && this.mCapServiceEventCallbacks.get(Integer.valueOf(phoneId)).containsKey(listener)) {
                this.mCapabilityService.unregisterListener((CapabilityServiceEventCallBack) this.mCapServiceEventCallbacks.get(Integer.valueOf(phoneId)).get(listener), phoneId);
                this.mCapServiceEventCallbacks.get(Integer.valueOf(phoneId)).remove(listener);
            }
        } else if (!this.mQueuedCapabilityListener.isEmpty()) {
            this.mQueuedCapabilityListener.remove(listener);
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

    private class CapabilityServiceEventCallBack extends ICapabilityServiceEventListener.Stub {
        SemCapabilityServiceEventListener mListener;

        public CapabilityServiceEventCallBack(SemCapabilityServiceEventListener listener) {
            this.mListener = listener;
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
    }
}
