package com.sec.internal.interfaces.ims.servicemodules.options;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import java.util.List;

public interface ICapabilityEventListener {
    void onCapabilityAndAvailabilityPublished(int i, int i2);

    void onCapabilityUpdate(List<ImsUri> list, long j, CapabilityConstants.CapExResult capExResult, String str, int i);

    void onCapabilityUpdate(List<ImsUri> list, CapabilityConstants.CapExResult capExResult, String str, OptionsEvent optionsEvent);

    void onMediaReady(boolean z, boolean z2, int i);

    void onPollingRequested(boolean z, int i);
}
