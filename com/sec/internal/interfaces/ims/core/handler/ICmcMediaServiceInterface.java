package com.sec.internal.interfaces.ims.core.handler;

import android.os.Handler;
import com.sec.internal.constants.ims.servicemodules.volte2.IMSMediaEvent;

public interface ICmcMediaServiceInterface {
    void registerForCmcMediaEvent(Handler handler, int i, Object obj);

    void sendConnectToSve(int i);

    void sendDisonnectToSve();

    void sendMediaEvent(int i, int i2, int i3, int i4);

    void sendRtpStatsToStack(IMSMediaEvent.AudioRtpStats audioRtpStats);

    int sreCreateRelayChannel(int i, int i2);

    int sreHoldRelayChannel(int i);

    int sreResumeRelayChannel(int i);

    int sreStartRecordingChannel(int i, int i2, int i3);

    int sreStartRelayChannel(int i, int i2);

    boolean startCmcRecord(int i, int i2, int i3, int i4, long j, int i5, String str, int i6, int i7, int i8, int i9, int i10, long j2, String str2);

    boolean stopCmcRecord(int i, int i2);

    void unregisterForCmcMediaEvent(Handler handler);
}
