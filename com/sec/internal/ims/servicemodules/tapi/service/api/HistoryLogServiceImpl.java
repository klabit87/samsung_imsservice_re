package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.net.Uri;
import android.os.RemoteException;
import com.gsma.services.rcs.history.IHistoryService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HistoryLogServiceImpl extends IHistoryService.Stub {
    private Map<Integer, HistoryLogMember> mExternalProviderMap = new HashMap();
    private Set<Integer> mInternalProviderIds;

    public Map<Integer, HistoryLogMember> getExternalProviderMap() {
        return this.mExternalProviderMap;
    }

    public long createUniqueId(int providerId) throws RemoteException {
        return ((long) this.mExternalProviderMap.get(Integer.valueOf(providerId)).getProviderId()) + System.currentTimeMillis();
    }

    public void registerExtraHistoryLogMember(int providerId, Uri providerUri, Uri database, String table, Map columnMapping) throws RemoteException {
        if (this.mExternalProviderMap.containsKey(Integer.valueOf(providerId))) {
            throw new IllegalArgumentException("Cannot register external database for already registered provider id " + providerId + "!");
        } else if (getInternalMemberIds().contains(Integer.valueOf(providerId))) {
            throw new IllegalArgumentException("Cannot register internal database for provider id " + providerId + "!");
        } else if (providerUri != null) {
            this.mExternalProviderMap.put(Integer.valueOf(providerId), new HistoryLogMember(providerId, providerUri.toString(), table, columnMapping));
        } else {
            throw new IllegalArgumentException("providerUri cannot be null");
        }
    }

    public void unRegisterExtraHistoryLogMember(int providerId) throws RemoteException {
        this.mExternalProviderMap.remove(Integer.valueOf(providerId));
    }

    public Set<Integer> getInternalMemberIds() {
        if (this.mInternalProviderIds == null) {
            HashSet hashSet = new HashSet();
            this.mInternalProviderIds = hashSet;
            hashSet.add(1);
            this.mInternalProviderIds.add(2);
            this.mInternalProviderIds.add(3);
            this.mInternalProviderIds.add(4);
            this.mInternalProviderIds.add(5);
        }
        return this.mInternalProviderIds;
    }
}
