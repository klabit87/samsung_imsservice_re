package com.sec.internal.interfaces.google;

import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.volte2.data.ImsCallInfo;

public interface IImsNotifier {
    void notifyImsRegistration(ImsRegistration imsRegistration, boolean z, ImsRegistrationError imsRegistrationError);

    void onCdpnInfo(int i, String str, int i2);

    void onDialogEvent(DialogEvent dialogEvent);

    void onIncomingCall(int i, int i2);

    void onIncomingPreAlerting(ImsCallInfo imsCallInfo, String str);

    void onP2pPushCallEvent(DialogEvent dialogEvent);

    void onP2pRegCompleteEvent();
}
