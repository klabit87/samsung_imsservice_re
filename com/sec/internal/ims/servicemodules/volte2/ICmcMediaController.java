package com.sec.internal.ims.servicemodules.volte2;

import com.samsung.android.ims.cmc.SemCmcRecordingInfo;

public interface ICmcMediaController {
    void connectToSve(int i);

    void disconnectToSve();

    void sendCmcRecordingEvent(int i, int i2, SemCmcRecordingInfo semCmcRecordingInfo);
}
