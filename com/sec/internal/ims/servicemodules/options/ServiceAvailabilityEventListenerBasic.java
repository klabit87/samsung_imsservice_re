package com.sec.internal.ims.servicemodules.options;

import com.android.internal.util.Preconditions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import java.util.Date;

public class ServiceAvailabilityEventListenerBasic implements IServiceAvailabilityEventListener {
    public void onServiceAvailabilityUpdate(String ownIMSI, ImsUri uri, Date timestamp) {
        Preconditions.checkNotNull(ownIMSI);
    }
}
