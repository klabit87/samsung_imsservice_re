package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.RemoteException;

public class ServerApiException extends RemoteException {
    static final long serialVersionUID = 1;

    public ServerApiException(String error) {
        setStackTrace(new Exception(error).getStackTrace());
    }
}
