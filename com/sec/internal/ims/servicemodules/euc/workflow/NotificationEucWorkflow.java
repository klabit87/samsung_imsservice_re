package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import java.util.Collections;
import java.util.List;

public class NotificationEucWorkflow extends BaseEucWorkflow implements IEucWorkflow {
    private static final String LOG_TAG = NotificationEucWorkflow.class.getSimpleName();
    private final IEucFactory mEucFactory;

    public /* bridge */ /* synthetic */ void discard(String str) {
        super.discard(str);
    }

    public /* bridge */ /* synthetic */ void start() {
        super.start();
    }

    public /* bridge */ /* synthetic */ void stop() {
        super.stop();
    }

    public NotificationEucWorkflow(IEucPersistence eucrPersistence, IEucDisplayManager displayManager, IEucStoreAndForward storeAndForward, IEucFactory factory) {
        super(eucrPersistence, displayManager, storeAndForward);
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(factory);
    }

    public void load(String ownIdentity) {
        this.mOwnIdentities.add(ownIdentity);
        List<EucType> types = Collections.singletonList(EucType.NOTIFICATION);
        try {
            Iterable<IEucQuery> queries = this.mEucFactory.combine(this.mEucPersistence.getAllEucs(EucState.NONE, EucType.NOTIFICATION, ownIdentity), this.mEucPersistence.getDialogsByTypes(EucState.NONE, types, this.mLanguageCode, ownIdentity));
            loadToCache(queries);
            displayQueries(queries, this.mLanguageCode);
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to obtain EUCs from persistence: " + e);
        }
    }

    public void handleIncomingEuc(IEucQuery eucQuery) {
        this.mCache.put(eucQuery);
        try {
            this.mEucPersistence.insertEuc(eucQuery.getEucData());
            this.mEucPersistence.insertDialogs(eucQuery);
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to store EUC with key=" + eucQuery.getEucData().getKey() + " in persistence: " + e);
        }
        this.mDisplayManager.display(eucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(eucQuery));
    }

    public void changeLanguage(String lang) {
        this.mLanguageCode = lang;
        changeLanguage(lang, EucType.NOTIFICATION);
    }

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return null;
    }
}
