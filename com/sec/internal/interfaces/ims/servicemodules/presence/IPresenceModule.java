package com.sec.internal.interfaces.ims.servicemodules.presence;

import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.List;

public interface IPresenceModule extends IServiceModule {
    PresenceInfo getOwnPresenceInfo(int i);

    boolean getParalysed(int i);

    PresenceInfo getPresenceInfo(ImsUri imsUri, int i);

    PresenceInfo getPresenceInfoByContactId(String str, int i);

    void removePresenceCache(List<ImsUri> list, int i);

    void setParalysed(boolean z, int i);

    void unpublish(int i);
}
