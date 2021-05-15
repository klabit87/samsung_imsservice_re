package com.sec.internal.ims.servicemodules.euc.persistence;

import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import java.util.List;

public class EucPersistenceNotifier implements IEucPersistence {
    private final IEucPersistence mEucrPersistence;
    private final UserConsentPersistenceNotifier mUserConsentPersistenceNotifier;

    public EucPersistenceNotifier(EucPersistence eucPersistence, UserConsentPersistenceNotifier userConsentPersistenceNotifier) {
        this.mEucrPersistence = (IEucPersistence) Preconditions.checkNotNull(eucPersistence);
        this.mUserConsentPersistenceNotifier = (UserConsentPersistenceNotifier) Preconditions.checkNotNull(userConsentPersistenceNotifier);
    }

    public void updateEuc(EucMessageKey key, EucState state, String pin) throws EucPersistenceException {
        this.mEucrPersistence.updateEuc(key, state, pin);
        this.mUserConsentPersistenceNotifier.notifyListener(key.getOwnIdentity());
    }

    public void insertEuc(IEucData eucData) throws EucPersistenceException {
        this.mEucrPersistence.insertEuc(eucData);
    }

    public void insertDialogs(IEucQuery query) throws EucPersistenceException {
        this.mEucrPersistence.insertDialogs(query);
        this.mUserConsentPersistenceNotifier.notifyListener(query.getEucData().getOwnIdentity());
    }

    public void insertAutoconfUserConsent(AutoconfUserConsentData userConsentData) throws EucPersistenceException {
        this.mEucrPersistence.insertAutoconfUserConsent(userConsentData);
        this.mUserConsentPersistenceNotifier.notifyListener(userConsentData.getOwnIdentity());
    }

    public List<IDialogData> getDialogs(List<String> eucIds, EucType type, String lang, List<String> ownIdentities) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getDialogs(eucIds, type, lang, ownIdentities);
    }

    public List<IDialogData> getDialogsByTypes(EucState state, List<EucType> types, String lang, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getDialogsByTypes(state, types, lang, ownIdentity);
    }

    public List<IEucData> getAllEucs(EucState state, EucType type, String ownIdentity) throws EucPersistenceException {
        return this.mEucrPersistence.getAllEucs(state, type, ownIdentity);
    }

    public List<IEucData> getAllEucs(List<EucState> states, EucType type, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getAllEucs(states, type, ownIdentity);
    }

    public List<IEucData> getAllEucs(EucState state, List<EucType> types, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getAllEucs(state, types, ownIdentity);
    }

    public List<IEucData> getAllEucs(List<EucState> states, List<EucType> types, String ownIdentity) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getAllEucs(states, types, ownIdentity);
    }

    public IEucData getEucByKey(EucMessageKey key) throws EucPersistenceException {
        return this.mEucrPersistence.getEucByKey(key);
    }

    public IEucData getVolatileEucByMostRecentTimeout(List<String> identities) throws EucPersistenceException, IllegalArgumentException {
        return this.mEucrPersistence.getVolatileEucByMostRecentTimeout(identities);
    }

    public void open() throws IllegalStateException, EucPersistenceException {
        this.mEucrPersistence.open();
    }

    public void close() throws IllegalStateException {
        this.mEucrPersistence.close();
    }
}
