package com.sec.internal.interfaces.ims.servicemodules.options;

import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.options.CapabilitiesCache;
import com.sec.internal.ims.servicemodules.options.ContactCache;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.List;

public interface ICapabilityDiscoveryModule extends IServiceModule {
    void changeParalysed(boolean z, int i);

    void clearCapabilitiesCache(int i);

    void exchangeCapabilitiesForVSH(int i, boolean z);

    void exchangeCapabilitiesForVSHOnRegi(boolean z, int i);

    Capabilities getCapabilities(ImsUri imsUri, long j, int i);

    Capabilities getCapabilities(ImsUri imsUri, CapabilityRefreshType capabilityRefreshType, int i);

    Capabilities getCapabilities(String str, long j, int i);

    Capabilities[] getCapabilities(List<ImsUri> list, CapabilityRefreshType capabilityRefreshType, long j, int i);

    Capabilities[] getCapabilitiesByContactId(String str, CapabilityRefreshType capabilityRefreshType, int i);

    CapabilitiesCache getCapabilitiesCache();

    CapabilitiesCache getCapabilitiesCache(int i);

    ImsUri getNetworkPreferredUri(ImsUri imsUri);

    Capabilities getOwnCapabilities();

    Capabilities getOwnCapabilitiesBase(int i);

    ContactCache getPhonebook();

    boolean hasVideoOwnCapability(int i);

    boolean isConfigured(int i);

    void onPackageUpdated(String str);

    boolean setLegacyLatching(ImsUri imsUri, boolean z, int i);

    void updateOwnCapabilities(int i);
}
