package com.sec.internal.google;

import android.os.RemoteException;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsEcbmListener;

public class ImsEcbmImpl extends IImsEcbm.Stub {
    private IImsEcbmListener miImsEcbmListener;

    public void setListener(IImsEcbmListener listener) throws RemoteException {
        this.miImsEcbmListener = listener;
    }

    public void exitEmergencyCallbackMode() throws RemoteException {
        this.miImsEcbmListener.exitedECBM();
    }

    public void enterEmergencyCallbackMode() throws RemoteException {
        this.miImsEcbmListener.enteredECBM();
    }
}
