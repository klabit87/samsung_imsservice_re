package com.sec.internal.ims.servicemodules.euc.persistence;

import com.sec.internal.ims.servicemodules.euc.data.AutoconfUserConsentData;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import java.util.List;

public interface IEucPersistence {
    void close() throws IllegalStateException;

    List<IEucData> getAllEucs(EucState eucState, EucType eucType, String str) throws EucPersistenceException;

    List<IEucData> getAllEucs(EucState eucState, List<EucType> list, String str) throws EucPersistenceException, IllegalArgumentException;

    List<IEucData> getAllEucs(List<EucState> list, EucType eucType, String str) throws EucPersistenceException, IllegalArgumentException;

    List<IEucData> getAllEucs(List<EucState> list, List<EucType> list2, String str) throws EucPersistenceException, IllegalArgumentException;

    List<IDialogData> getDialogs(List<String> list, EucType eucType, String str, List<String> list2) throws EucPersistenceException, IllegalArgumentException;

    List<IDialogData> getDialogsByTypes(EucState eucState, List<EucType> list, String str, String str2) throws EucPersistenceException, IllegalArgumentException;

    IEucData getEucByKey(EucMessageKey eucMessageKey) throws EucPersistenceException;

    IEucData getVolatileEucByMostRecentTimeout(List<String> list) throws EucPersistenceException, IllegalArgumentException;

    void insertAutoconfUserConsent(AutoconfUserConsentData autoconfUserConsentData) throws EucPersistenceException;

    void insertDialogs(IEucQuery iEucQuery) throws EucPersistenceException;

    void insertEuc(IEucData iEucData) throws EucPersistenceException;

    void open() throws IllegalStateException, EucPersistenceException;

    void updateEuc(EucMessageKey eucMessageKey, EucState eucState, String str) throws EucPersistenceException;
}
