package com.sec.internal.ims.servicemodules.options;

import android.os.RemoteException;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityService;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityDiscoveryService extends ICapabilityService.Stub {
    private Map<ICapabilityServiceEventListener, Integer> mQueuedCapabilityListener = new HashMap();
    private CapabilityDiscoveryModule mServiceModule = null;

    public void setServiceModule(ServiceModuleBase service) {
        this.mServiceModule = (CapabilityDiscoveryModule) service;
        if (!this.mQueuedCapabilityListener.isEmpty()) {
            for (Map.Entry<ICapabilityServiceEventListener, Integer> entry : this.mQueuedCapabilityListener.entrySet()) {
                this.mServiceModule.registerListener(entry.getKey(), entry.getValue().intValue());
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

    public void registerListener(ICapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.registerListener(listener, phoneId);
        } else {
            this.mQueuedCapabilityListener.put(listener, Integer.valueOf(phoneId));
        }
    }

    public void unregisterListener(ICapabilityServiceEventListener listener, int phoneId) throws RemoteException {
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mServiceModule;
        if (capabilityDiscoveryModule != null) {
            capabilityDiscoveryModule.unregisterListener(listener, phoneId);
        } else if (!this.mQueuedCapabilityListener.isEmpty()) {
            this.mQueuedCapabilityListener.remove(listener);
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
}
