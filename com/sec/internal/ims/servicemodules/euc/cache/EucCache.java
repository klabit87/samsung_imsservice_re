package com.sec.internal.ims.servicemodules.euc.cache;

import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EucCache implements IEucCache {
    private Map<EucMessageKey, IEucQuery> mEucrMap = new HashMap();

    public void put(IEucQuery euc) {
        IEucData eucData = euc.getEucData();
        this.mEucrMap.put(new EucMessageKey(eucData.getId(), eucData.getOwnIdentity(), eucData.getType(), eucData.getRemoteUri()), euc);
    }

    public IEucQuery get(EucMessageKey eucMessageKey) {
        return this.mEucrMap.get(eucMessageKey);
    }

    public Iterable<IEucQuery> getAllByType(EucType type) {
        List<IEucQuery> eucQueries = new ArrayList<>();
        for (Map.Entry<EucMessageKey, IEucQuery> entry : this.mEucrMap.entrySet()) {
            if (type == entry.getKey().getEucType()) {
                eucQueries.add(entry.getValue());
            }
        }
        return eucQueries;
    }

    public IEucQuery remove(EucMessageKey eucMessageKey) {
        return this.mEucrMap.remove(eucMessageKey);
    }

    public void clearAllForOwnIdentity(String ownIdentity) {
        Iterator<Map.Entry<EucMessageKey, IEucQuery>> it = this.mEucrMap.entrySet().iterator();
        while (it.hasNext()) {
            if (ownIdentity.equals(it.next().getKey().getOwnIdentity())) {
                it.remove();
            }
        }
    }

    public boolean isEmpty() {
        return this.mEucrMap.isEmpty();
    }
}
