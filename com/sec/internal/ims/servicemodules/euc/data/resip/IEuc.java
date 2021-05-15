package com.sec.internal.ims.servicemodules.euc.data.resip;

import com.sec.ims.util.ImsUri;
import java.util.Map;

public interface IEuc<T> {
    T getDefaultData();

    String getEucId();

    ImsUri getFromHeader();

    Map<String, T> getLanguageMapping();

    String getOwnIdentity();

    long getTimestamp();
}
