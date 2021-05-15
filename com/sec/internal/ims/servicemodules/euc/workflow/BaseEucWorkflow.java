package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.cache.EucCache;
import com.sec.internal.ims.servicemodules.euc.cache.IEucCache;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.locale.DeviceLocale;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

abstract class BaseEucWorkflow implements IEucWorkflow {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = BaseEucWorkflow.class.getSimpleName();
    protected final IEucCache mCache = new EucCache();
    final IEucDisplayManager mDisplayManager;
    final IEucPersistence mEucPersistence;
    Map<EucMessageKey, IEucStoreAndForward.IResponseHandle> mHandleMap = new HashMap();
    String mLanguageCode = DeviceLocale.DEFAULT_LANG_VALUE;
    List<String> mOwnIdentities = new ArrayList();
    private final IEucStoreAndForward mStoreAndForward;

    /* access modifiers changed from: package-private */
    public abstract IEucStoreAndForward.IResponseCallback createSendResponseCallback();

    BaseEucWorkflow(IEucPersistence eucPersistence, IEucDisplayManager displayManager, IEucStoreAndForward storeAndForward) {
        this.mEucPersistence = (IEucPersistence) Preconditions.checkNotNull(eucPersistence);
        this.mDisplayManager = (IEucDisplayManager) Preconditions.checkNotNull(displayManager);
        this.mStoreAndForward = storeAndForward;
    }

    /* access modifiers changed from: package-private */
    public void loadToCache(Iterable<IEucQuery> eucQueries) {
        for (IEucQuery query : eucQueries) {
            this.mCache.put(query);
        }
    }

    /* access modifiers changed from: package-private */
    public void changeLanguage(String lang, EucType type) {
        String str;
        List<String> idList = new ArrayList<>();
        for (IEucQuery euc : this.mCache.getAllByType(type)) {
            if (!euc.hasDialog(lang)) {
                idList.add(euc.getEucData().getId());
            }
        }
        if (!idList.isEmpty()) {
            try {
                List<IDialogData> dialogList = this.mEucPersistence.getDialogs(idList, type, lang, this.mOwnIdentities);
                Preconditions.checkState(!dialogList.isEmpty(), "No dialogs found for given EUCRs, it should not happen!");
                for (IDialogData dialog : dialogList) {
                    EucMessageKey eucMessageKey = dialog.getKey();
                    IEucQuery query = this.mCache.get(eucMessageKey);
                    Preconditions.checkState(query != null, "No query in cache for id=" + eucMessageKey.getEucId() + ". Should not happen!");
                    query.addDialogData(dialog);
                }
            } catch (EucPersistenceException e) {
                Log.e(LOG_TAG, "Unable to obtain dialogs data for type=" + type + " language=" + lang + " from persistence: " + e);
            } catch (IllegalArgumentException e2) {
                String str2 = LOG_TAG;
                if (idList.isEmpty()) {
                    str = "idList";
                } else {
                    str = "mOwnIdentities list is empty - wrong argument in query to persistence: " + e2;
                }
                Log.e(str2, str);
            }
            replaceDisplay(type, lang);
        }
    }

    /* access modifiers changed from: package-private */
    public void displayQueries(Iterable<IEucQuery> eucQueries, String lang) {
        for (IEucQuery query : eucQueries) {
            IDialogData dialogData = query.getDialogData(lang);
            if (!dialogData.getSubject().isEmpty() || !dialogData.getText().isEmpty()) {
                this.mDisplayManager.display(query, lang, createDisplayManagerRequestCallback(query));
            }
        }
    }

    private void replaceDisplay(EucType type, String language) {
        this.mDisplayManager.hideAllForType(type);
        displayQueries(this.mCache.getAllByType(type), language);
    }

    /* access modifiers changed from: package-private */
    public void sendResponse(IEucData eucData, EucResponseData.Response response, String pin) {
        IEucStoreAndForward.IResponseHandle handle;
        if (pin == null) {
            handle = this.mStoreAndForward.sendResponse(eucData, response, createSendResponseCallback());
        } else {
            handle = this.mStoreAndForward.sendResponse(eucData, response, pin, createSendResponseCallback());
        }
        if (handle != null) {
            this.mHandleMap.put(new EucMessageKey(eucData.getId(), eucData.getOwnIdentity(), eucData.getType(), eucData.getRemoteUri()), handle);
        } else {
            Log.e(LOG_TAG, "Handle is null");
        }
    }

    /* access modifiers changed from: package-private */
    public IEucDisplayManager.IDisplayCallback createDisplayManagerRequestCallback(IEucQuery euc) {
        IEucData eucData = euc.getEucData();
        String eucId = eucData.getId();
        EucType eucType = eucData.getType();
        final EucMessageKey eucMessageKey = new EucMessageKey(eucId, eucData.getOwnIdentity(), eucType, eucData.getRemoteUri());
        final EucType eucType2 = eucType;
        final IEucData iEucData = eucData;
        final String str = eucId;
        return new IEucDisplayManager.IDisplayCallback() {
            public void onSuccess(EucResponseData.Response response, String pin) {
                EucResponseData.Response userResponse;
                EucState newEucState;
                try {
                    int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[eucType2.ordinal()];
                    if (i == 1 || i == 2) {
                        if (response.equals(EucResponseData.Response.ACCEPT)) {
                            newEucState = EucState.ACCEPTED_NOT_SENT;
                            userResponse = EucResponseData.Response.ACCEPT;
                        } else {
                            newEucState = EucState.REJECTED_NOT_SENT;
                            userResponse = EucResponseData.Response.DECLINE;
                        }
                        BaseEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, newEucState, pin);
                        BaseEucWorkflow.this.sendResponse(iEucData, userResponse, pin);
                        BaseEucWorkflow.this.mCache.remove(eucMessageKey);
                    } else if (i == 3 || i == 4) {
                        Preconditions.checkState(response.equals(EucResponseData.Response.ACCEPT), "Only ok button expected for notification or acknowledgment!");
                        BaseEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.ACCEPTED, pin);
                        BaseEucWorkflow.this.mCache.remove(eucMessageKey);
                    } else {
                        if (i == 5) {
                            Log.e(BaseEucWorkflow.LOG_TAG, "EULA is not handled here!");
                        }
                        BaseEucWorkflow.this.mCache.remove(eucMessageKey);
                    }
                } catch (EucPersistenceException e) {
                    String access$000 = BaseEucWorkflow.LOG_TAG;
                    Log.e(access$000, "Unable to change EUCs state in persistence for EUCR with id=" + str);
                    String access$0002 = BaseEucWorkflow.LOG_TAG;
                    IMSLog.s(access$0002, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
                } catch (Throwable th) {
                    BaseEucWorkflow.this.mCache.remove(eucMessageKey);
                    throw th;
                }
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.BaseEucWorkflow$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType;

        static {
            int[] iArr = new int[EucType.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType = iArr;
            try {
                iArr[EucType.PERSISTENT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.VOLATILE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.ACKNOWLEDGEMENT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.NOTIFICATION.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucType[EucType.EULA.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public void discard(String ownIdentity) {
        Iterator<Map.Entry<EucMessageKey, IEucStoreAndForward.IResponseHandle>> it = this.mHandleMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<EucMessageKey, IEucStoreAndForward.IResponseHandle> entry = it.next();
            if (ownIdentity.equals(entry.getKey().getOwnIdentity())) {
                entry.getValue().invalidate();
                it.remove();
            }
        }
        this.mDisplayManager.hideAllForOwnIdentity(ownIdentity);
        this.mCache.clearAllForOwnIdentity(ownIdentity);
        this.mOwnIdentities.remove(ownIdentity);
    }

    public void start() {
    }

    public void stop() {
    }
}
