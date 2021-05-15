package com.sec.internal.ims.servicemodules.euc.cache;

import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;

public interface IEucCache {
    void clearAllForOwnIdentity(String str);

    IEucQuery get(EucMessageKey eucMessageKey);

    Iterable<IEucQuery> getAllByType(EucType eucType);

    boolean isEmpty();

    void put(IEucQuery iEucQuery);

    IEucQuery remove(EucMessageKey eucMessageKey);
}
