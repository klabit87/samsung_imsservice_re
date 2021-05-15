package com.sec.internal.ims.servicemodules.presence;

import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;

public class PresenceCacheController {
    private static final String LOG_TAG = "PresenceCacheController";
    private final PresenceModule mPresence;

    PresenceCacheController(PresenceModule presenceModule) {
        this.mPresence = presenceModule;
    }

    /* access modifiers changed from: package-private */
    public PresenceInfo getPresenceInfo(ImsUri uri, int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "getPresenceInfo: uri " + uri);
        return this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.get(uri);
    }

    /* access modifiers changed from: package-private */
    public PresenceInfo getPresenceInfoByContactId(String contactId, List<String> normalizedNumberList, int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "getPresenceInfoByContactId: contactId " + contactId);
        if (normalizedNumberList == null || normalizedNumberList.isEmpty()) {
            return null;
        }
        return this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.get(ImsUri.parse("tel:" + normalizedNumberList.get(0)));
    }

    /* access modifiers changed from: package-private */
    public void removePresenceCache(List<ImsUri> uris, int phoneId) {
        this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.remove(uris);
        PresenceSubscriptionController.removeSubscription(uris);
    }

    /* access modifiers changed from: package-private */
    public void clearPresenceInfo(int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "clearPresenceInfo");
        this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.clear();
    }

    /* access modifiers changed from: package-private */
    public void updatePresenceDatabase(List<ImsUri> uris, PresenceInfo info, ICapabilityDiscoveryModule capabilityDiscoveryModule, UriGenerator uriGenerator, int phoneId) {
        Capabilities cap;
        String contactId;
        if (this.mPresence.checkModuleReady(phoneId) && uris != null && uris.size() != 0) {
            IMSLog.s(LOG_TAG, phoneId, "updatePresenceDatabase: uris " + uris);
            PresenceInfo newInfo = info;
            for (ImsUri uri : uris) {
                ImsUri telUri = uriGenerator.normalize(uri);
                if (!info.isFetchSuccess()) {
                    newInfo = getPresenceInfo(telUri, phoneId);
                    if (newInfo == null) {
                        newInfo = new PresenceInfo(info.getSubscriptionId(), phoneId);
                    } else if (uris.size() > 1) {
                    }
                }
                if (!(capabilityDiscoveryModule.getCapabilitiesCache(phoneId) == null || (cap = capabilityDiscoveryModule.getCapabilitiesCache(phoneId).get(telUri)) == null || (contactId = cap.getContactId()) == null)) {
                    newInfo.setContactId(contactId);
                }
                PresenceInfo cacheInfo = this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.get(telUri);
                if (cacheInfo != null) {
                    newInfo.setId(cacheInfo.getId());
                }
                newInfo.setTelUri(telUri.toString());
                newInfo.setUri(uri.toString());
                newInfo.addService(info.getServiceList());
                this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.update(telUri, newInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void loadPresenceStorage(List<ImsUri> teluris, int phoneId) {
        Map<ImsUri, PresenceInfo> piMap = this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.get(teluris);
        if (piMap == null || piMap.size() <= 0) {
            for (ImsUri teluri : teluris) {
                PresenceInfo pi = new PresenceInfo(phoneId);
                pi.setTelUri(teluri.toString());
                this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.add(pi);
            }
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "loadPresenceStorage: found " + piMap.size() + " presenceInfo from DB");
        for (ImsUri teluri2 : teluris) {
            PresenceInfo pi2 = piMap.get(teluri2);
            if (pi2 == null) {
                pi2 = new PresenceInfo(phoneId);
                pi2.setTelUri(teluri2.toString());
            }
            this.mPresence.getPresenceModuleInfo(phoneId).mPresenceCache.add(pi2);
        }
    }
}
