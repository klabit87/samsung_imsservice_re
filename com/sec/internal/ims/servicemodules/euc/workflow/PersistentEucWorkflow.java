package com.sec.internal.ims.servicemodules.euc.workflow;

import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IDialogData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;

public class PersistentEucWorkflow extends BaseEucWorkflow implements IEucWorkflow {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PersistentEucWorkflow.class.getSimpleName();
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

    public PersistentEucWorkflow(IEucPersistence eucrPersistence, IEucDisplayManager displayManager, IEucFactory factory, IEucStoreAndForward storeAndForward) {
        super(eucrPersistence, displayManager, storeAndForward);
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(factory);
    }

    public void load(String ownIdentity) {
        this.mOwnIdentities.add(ownIdentity);
        List<EucType> types = Arrays.asList(new EucType[]{EucType.PERSISTENT, EucType.ACKNOWLEDGEMENT});
        try {
            for (IEucData message : this.mEucPersistence.getAllEucs(Arrays.asList(new EucState[]{EucState.ACCEPTED_NOT_SENT, EucState.REJECTED_NOT_SENT, EucState.NONE}), types, ownIdentity)) {
                if (message.getState() == EucState.ACCEPTED_NOT_SENT) {
                    sendResponse(message, EucResponseData.Response.ACCEPT, message.getUserPin());
                } else if (message.getState() == EucState.REJECTED_NOT_SENT) {
                    sendResponse(message, EucResponseData.Response.DECLINE, message.getUserPin());
                }
            }
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to obtain EUCs from persistence: " + e);
        }
        try {
            Iterable<IEucQuery> queries = this.mEucFactory.combine(this.mEucPersistence.getAllEucs(EucState.NONE, types, ownIdentity), this.mEucPersistence.getDialogsByTypes(EucState.NONE, types, this.mLanguageCode, ownIdentity));
            loadToCache(queries);
            displayQueries(queries, this.mLanguageCode);
        } catch (EucPersistenceException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to obtain EUCs from persistence: " + e2);
        }
    }

    public void handleIncomingEuc(IEucQuery eucQuery) {
        this.mCache.put(eucQuery);
        IEucData eucData = eucQuery.getEucData();
        String eucId = eucData.getId();
        EucMessageKey persistentMessageKey = new EucMessageKey(eucId, eucData.getOwnIdentity(), EucType.PERSISTENT, eucData.getRemoteUri());
        try {
            if (eucData.getType() == EucType.ACKNOWLEDGEMENT && this.mCache.get(persistentMessageKey) != null) {
                this.mDisplayManager.hide(persistentMessageKey);
                this.mCache.remove(persistentMessageKey);
                this.mEucPersistence.updateEuc(persistentMessageKey, EucState.DISMISSED, (String) null);
            }
        } catch (EucPersistenceException e) {
            String str = LOG_TAG;
            Log.e(str, "Unable to update EUC with id=" + eucId + " in persistence: " + e);
            String str2 = LOG_TAG;
            IMSLog.s(str2, "Unable to update EUC with key=" + persistentMessageKey + " in persistence: " + e);
        }
        try {
            this.mEucPersistence.insertEuc(eucData);
            this.mEucPersistence.insertDialogs(eucQuery);
        } catch (EucPersistenceException e2) {
            String str3 = LOG_TAG;
            Log.e(str3, "Unable to insert EUC with id=" + eucId + " in persistence: " + e2);
            String str4 = LOG_TAG;
            IMSLog.s(str4, "Unable to insert EUC with key=" + persistentMessageKey + " in persistence: " + e2);
        }
        IDialogData dialogData = eucQuery.getDialogData(this.mLanguageCode);
        if (!dialogData.getSubject().isEmpty() || !dialogData.getText().isEmpty()) {
            this.mDisplayManager.display(eucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(eucQuery));
        }
    }

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return new IEucStoreAndForward.IResponseCallback() {
            /* Debug info: failed to restart local var, previous not found, register: 12 */
            public void onStatus(EucSendResponseStatus status) {
                EucSendResponseStatus.Status responseStatus = status.getStatus();
                String eucId = status.getId();
                String ownIdentity = status.getOwnIdentity();
                EucMessageKey eucMessageKey = new EucMessageKey(eucId, ownIdentity, EucType.PERSISTENT, status.getRemoteUri());
                if (PersistentEucWorkflow.this.mHandleMap.containsKey(eucMessageKey)) {
                    ((IEucStoreAndForward.IResponseHandle) PersistentEucWorkflow.this.mHandleMap.get(eucMessageKey)).invalidate();
                    PersistentEucWorkflow.this.mHandleMap.remove(eucMessageKey);
                }
                try {
                    IEucData euc = PersistentEucWorkflow.this.mEucPersistence.getEucByKey(eucMessageKey);
                    if (euc != null) {
                        int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[responseStatus.ordinal()];
                        if (i == 1) {
                            EucState eucState = euc.getState();
                            int i2 = AnonymousClass2.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState[eucState.ordinal()];
                            if (i2 == 1) {
                                PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.ACCEPTED, (String) null);
                            } else if (i2 == 2) {
                                PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.REJECTED, (String) null);
                            } else {
                                String access$000 = PersistentEucWorkflow.LOG_TAG;
                                Log.e(access$000, "Wrong state: " + eucState.getId() + " for EUCR with id=" + eucId);
                                String access$0002 = PersistentEucWorkflow.LOG_TAG;
                                IMSLog.s(access$0002, "Wrong state: " + eucState.getId() + " for EUCR with key=" + eucMessageKey);
                                throw new IllegalStateException("Illegal persistent EUC state!");
                            }
                        } else if (i == 2) {
                            Log.e(PersistentEucWorkflow.LOG_TAG, "Network error. Message will not be send");
                            PersistentEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.FAILED, (String) null);
                        } else if (i == 3) {
                            String access$0003 = PersistentEucWorkflow.LOG_TAG;
                            Log.e(access$0003, "Internal error. Msg will be send on a new regi for identity: " + IMSLog.checker(ownIdentity));
                        }
                        return;
                    }
                    String access$0004 = PersistentEucWorkflow.LOG_TAG;
                    Log.e(access$0004, "EUCR with id=" + eucId + " was not found!");
                    String access$0005 = PersistentEucWorkflow.LOG_TAG;
                    IMSLog.s(access$0005, "EUCR with key=" + eucMessageKey + " was not found!");
                } catch (EucPersistenceException e) {
                    String access$0006 = PersistentEucWorkflow.LOG_TAG;
                    Log.e(access$0006, "Unable to change EUCs state in persistence for EUCR with id=" + eucId);
                    String access$0007 = PersistentEucWorkflow.LOG_TAG;
                    IMSLog.s(access$0007, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
                }
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.PersistentEucWorkflow$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState;

        static {
            int[] iArr = new int[EucSendResponseStatus.Status.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status = iArr;
            try {
                iArr[EucSendResponseStatus.Status.SUCCESS.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[EucSendResponseStatus.Status.FAILURE_NETWORK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[EucSendResponseStatus.Status.FAILURE_INTERNAL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            int[] iArr2 = new int[EucState.values().length];
            $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState = iArr2;
            try {
                iArr2[EucState.ACCEPTED_NOT_SENT.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState[EucState.REJECTED_NOT_SENT.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public void changeLanguage(String lang) {
        this.mLanguageCode = lang;
        changeLanguage(lang, EucType.PERSISTENT);
        changeLanguage(lang, EucType.ACKNOWLEDGEMENT);
    }
}
