package com.sec.internal.ims.servicemodules.euc.data;

import com.sec.ims.util.ImsUri;

public interface IEucData {
    boolean getExternal();

    String getId();

    EucMessageKey getKey();

    String getOwnIdentity();

    boolean getPin();

    ImsUri getRemoteUri();

    EucState getState();

    Long getTimeOut();

    long getTimestamp();

    EucType getType();

    String getUserPin();
}
