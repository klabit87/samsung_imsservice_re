package com.sec.internal.interfaces.google;

import android.telephony.ims.feature.ImsFeature;

public interface IGoogleImsService {
    void updateCapabilities(int i, ImsFeature.Capabilities capabilities);

    void updateCapabilities(int i, int[] iArr, boolean[] zArr);
}
