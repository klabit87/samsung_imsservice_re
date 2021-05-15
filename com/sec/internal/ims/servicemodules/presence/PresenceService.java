package com.sec.internal.ims.servicemodules.presence;

import android.os.RemoteException;
import com.sec.ims.presence.IPresenceService;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule;

public class PresenceService extends IPresenceService.Stub {
    PresenceModule mPresence = null;

    public PresenceService(CapabilityDiscoveryModule capex) {
        this.mPresence = capex.getPresenceModule();
    }

    public PresenceInfo getOwnPresenceInfo() throws RemoteException {
        return this.mPresence.getOwnPresenceInfo(SimUtil.getDefaultPhoneId());
    }

    public PresenceInfo getPresenceInfo(ImsUri uri) throws RemoteException {
        return this.mPresence.getPresenceInfo(uri, SimUtil.getDefaultPhoneId());
    }

    public PresenceInfo getPresenceInfoByContactId(String contactId) throws RemoteException {
        return this.mPresence.getPresenceInfoByContactId(contactId, SimUtil.getDefaultPhoneId());
    }
}
