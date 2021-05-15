package com.sec.internal.interfaces.ims.servicemodules;

import android.os.Handler;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.ims.servicemodules.tapi.service.api.interfaces.ITapiServiceManager;
import com.sec.internal.interfaces.ims.cmstore.ICmsModule;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucModule;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IImsStatusServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.openapi.IOpenApiServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IOptionsModule;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.interfaces.ims.servicemodules.sms.ISmsServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.List;
import java.util.Set;

public interface IServiceModuleManager extends ISequentialInitializable {
    void cleanUpModules();

    ICapabilityDiscoveryModule getCapabilityDiscoveryModule();

    ICmsModule getCmsModule();

    IEucModule getEucModule();

    IGlsModule getGlsModule();

    IImModule getImModule();

    IImageShareModule getImageShareModule();

    IImsStatusServiceModule getImsStatusServiceModule();

    IOpenApiServiceModule getOpenApiServiceModule();

    IOptionsModule getOptionsModule();

    IPresenceModule getPresenceModule();

    Handler getServiceModuleHandler(String str);

    ISessionModule getSessionModule();

    ISmsServiceModule getSmsServiceModule();

    ITapiServiceManager getTapiServiceManager();

    IUtServiceModule getUtServiceModule();

    IVideoShareModule getVideoShareModule();

    IVolteServiceModule getVolteServiceModule();

    boolean isLooperExist();

    void notifyAutoConfigDone(int i);

    void notifyConfigured(boolean z, int i);

    void notifyDeregistering(ImsRegistration imsRegistration);

    void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, int i);

    void notifyImsSwitchUpdateToApp();

    void notifyNetworkChanged(NetworkEvent networkEvent, int i);

    void notifyOmadmVolteConfigDone(int i);

    void notifyRcsDeregistering(Set<String> set, ImsRegistration imsRegistration);

    void notifyReRegistering(int i, Set<String> set);

    void notifySimChange(int i);

    void onImsSwitchUpdated(int i);

    void serviceStartDeterminer(IRegistrationManager iRegistrationManager, List<ImsProfile> list, int i);

    void updateCapabilities(int i);
}
