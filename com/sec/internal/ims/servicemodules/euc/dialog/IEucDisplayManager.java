package com.sec.internal.ims.servicemodules.euc.dialog;

import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;

public interface IEucDisplayManager {

    public interface IDisplayCallback {
        void onSuccess(EucResponseData.Response response, String str);
    }

    void display(IEucQuery iEucQuery, String str, IDisplayCallback iDisplayCallback) throws IllegalStateException;

    void hide(EucMessageKey eucMessageKey) throws IllegalStateException;

    void hideAllForOwnIdentity(String str) throws IllegalStateException;

    void hideAllForType(EucType eucType) throws IllegalStateException;

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;
}
