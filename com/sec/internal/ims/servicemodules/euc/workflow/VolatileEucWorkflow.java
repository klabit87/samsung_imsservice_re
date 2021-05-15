package com.sec.internal.ims.servicemodules.euc.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.data.EucMessageKey;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.EucState;
import com.sec.internal.ims.servicemodules.euc.data.EucType;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.data.IEucQuery;
import com.sec.internal.ims.servicemodules.euc.dialog.IEucDisplayManager;
import com.sec.internal.ims.servicemodules.euc.persistence.EucPersistenceException;
import com.sec.internal.ims.servicemodules.euc.persistence.IEucPersistence;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucFactory;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VolatileEucWorkflow extends BaseEucWorkflow implements IEucWorkflow {
    private static final String INTENT_EUCR_VOLATILE_TIMEOUT = "com.sec.internal.ims.servicemodules.euc.workflow.action.VOLATILE_TIMEOUT";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = VolatileEucWorkflow.class.getSimpleName();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String access$000 = VolatileEucWorkflow.LOG_TAG;
            Log.d(access$000, "onReceive: EUCR Volatile intent: " + intent.getAction());
            if (VolatileEucWorkflow.INTENT_EUCR_VOLATILE_TIMEOUT.equals(intent.getAction())) {
                Log.i(VolatileEucWorkflow.LOG_TAG, "onReceive: EUCR Volatile message timeout.");
                onEucrVolatileTimeout();
            }
        }

        private void onEucrVolatileTimeout() {
            VolatileEucWorkflow volatileEucWorkflow = VolatileEucWorkflow.this;
            volatileEucWorkflow.timeoutMessage((IEucData) volatileEucWorkflow.mCurrentAlarm.second);
            VolatileEucWorkflow.this.unscheduleCurrentAlarmTimerIntent();
            VolatileEucWorkflow.this.scheduleNextAlarmTimerIntent((IEucData) null);
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public Pair<PendingIntent, IEucData> mCurrentAlarm = null;
    private final IEucFactory mEucFactory;
    private final Handler mHandler;

    public /* bridge */ /* synthetic */ void discard(String str) {
        super.discard(str);
    }

    public VolatileEucWorkflow(Context context, Handler handler, IEucPersistence eucrPersistence, IEucDisplayManager displayManager, IEucStoreAndForward storeAndForward, IEucFactory factory) {
        super(eucrPersistence, displayManager, storeAndForward);
        this.mContext = context;
        this.mHandler = handler;
        this.mEucFactory = (IEucFactory) Preconditions.checkNotNull(factory);
    }

    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_EUCR_VOLATILE_TIMEOUT);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, (String) null, this.mHandler);
        Log.d(LOG_TAG, "Receiver registered.");
    }

    public void stop() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        Log.d(LOG_TAG, "Receiver unregistered.");
    }

    public void load(String ownIdentity) {
        this.mOwnIdentities.add(ownIdentity);
        List<EucType> types = Collections.singletonList(EucType.VOLATILE);
        try {
            for (IEucData message : this.mEucPersistence.getAllEucs(Arrays.asList(new EucState[]{EucState.ACCEPTED_NOT_SENT, EucState.REJECTED_NOT_SENT}), EucType.VOLATILE, ownIdentity)) {
                if (isMessageTimedOut(message).booleanValue()) {
                    timeoutMessage(message);
                } else if (message.getState() == EucState.ACCEPTED_NOT_SENT) {
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
            Iterable<IEucQuery> queries = this.mEucFactory.combine(this.mEucPersistence.getAllEucs(EucState.NONE, EucType.VOLATILE, ownIdentity), this.mEucPersistence.getDialogsByTypes(EucState.NONE, types, this.mLanguageCode, ownIdentity));
            for (IEucQuery message2 : queries) {
                IEucData messageData = message2.getEucData();
                if (isMessageTimedOut(messageData).booleanValue()) {
                    timeoutMessage(messageData);
                }
            }
            scheduleNextAlarmTimerIntent((IEucData) null);
            loadToCache(queries);
            displayQueries(queries, this.mLanguageCode);
        } catch (EucPersistenceException e2) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to obtain EUCs from persistence: " + e2);
        }
    }

    public void handleIncomingEuc(IEucQuery eucQuery) {
        IEucData eucData = eucQuery.getEucData();
        String str = LOG_TAG;
        Log.d(str, "handleIncomingEuc with id=" + eucData.getKey());
        try {
            this.mEucPersistence.insertEuc(eucData);
            this.mEucPersistence.insertDialogs(eucQuery);
        } catch (EucPersistenceException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "Unable to store EUC with key=" + eucData.getKey() + " in persistence: " + e);
        }
        if (eucData.getTimeOut().longValue() > System.currentTimeMillis()) {
            this.mCache.put(eucQuery);
            scheduleNextAlarmTimerIntent(eucData);
            this.mDisplayManager.display(eucQuery, this.mLanguageCode, createDisplayManagerRequestCallback(eucQuery));
        }
    }

    public void changeLanguage(String lang) {
        this.mLanguageCode = lang;
        changeLanguage(lang, EucType.VOLATILE);
    }

    public IEucStoreAndForward.IResponseCallback createSendResponseCallback() {
        return new IEucStoreAndForward.IResponseCallback() {
            /* Debug info: failed to restart local var, previous not found, register: 13 */
            public void onStatus(EucSendResponseStatus status) {
                EucSendResponseStatus.Status responseStatus = status.getStatus();
                String eucId = status.getId();
                String ownIdentity = status.getOwnIdentity();
                EucMessageKey eucMessageKey = new EucMessageKey(eucId, ownIdentity, EucType.VOLATILE, status.getRemoteUri());
                try {
                    IEucData euc = VolatileEucWorkflow.this.mEucPersistence.getEucByKey(eucMessageKey);
                    if (euc != null) {
                        int i = AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucSendResponseStatus$Status[responseStatus.ordinal()];
                        if (i == 1) {
                            EucState eucState = euc.getState();
                            int i2 = AnonymousClass3.$SwitchMap$com$sec$internal$ims$servicemodules$euc$data$EucState[eucState.ordinal()];
                            if (i2 == 1) {
                                VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.ACCEPTED, (String) null);
                                reschedule(eucMessageKey);
                            } else if (i2 == 2) {
                                VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.REJECTED, (String) null);
                                reschedule(eucMessageKey);
                            } else {
                                String access$000 = VolatileEucWorkflow.LOG_TAG;
                                Log.e(access$000, "Wrong state: " + eucState.getId() + " for EUCR with id=" + eucId);
                                String access$0002 = VolatileEucWorkflow.LOG_TAG;
                                IMSLog.s(access$0002, "Wrong state: " + eucState.getId() + " for EUCR with key=" + eucMessageKey);
                                throw new IllegalStateException("Illegal volatile EUC state!");
                            }
                        } else if (i == 2) {
                            Log.e(VolatileEucWorkflow.LOG_TAG, "Network error. Message will not be send");
                            VolatileEucWorkflow.this.mEucPersistence.updateEuc(eucMessageKey, EucState.FAILED, (String) null);
                            reschedule(eucMessageKey);
                        } else if (i == 3) {
                            String access$0003 = VolatileEucWorkflow.LOG_TAG;
                            Log.e(access$0003, "Internal error. Msg will be send on a new regi for identity: " + IMSLog.checker(ownIdentity));
                        }
                        return;
                    }
                    String access$0004 = VolatileEucWorkflow.LOG_TAG;
                    Log.e(access$0004, "EUCR with id=" + eucId + " was not found!");
                    String access$0005 = VolatileEucWorkflow.LOG_TAG;
                    IMSLog.s(access$0005, "EUCR with key=" + eucMessageKey + " was not found!");
                } catch (EucPersistenceException e) {
                    String access$0006 = VolatileEucWorkflow.LOG_TAG;
                    Log.e(access$0006, "Unable to change EUCs state in persistence for EUCR with id=" + eucId);
                    String access$0007 = VolatileEucWorkflow.LOG_TAG;
                    IMSLog.s(access$0007, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
                }
            }

            private void reschedule(EucMessageKey eucMessageKey) {
                if (VolatileEucWorkflow.this.mCurrentAlarm != null && eucMessageKey.equals(((IEucData) VolatileEucWorkflow.this.mCurrentAlarm.second).getKey())) {
                    VolatileEucWorkflow.this.unscheduleCurrentAlarmTimerIntent();
                    VolatileEucWorkflow.this.scheduleNextAlarmTimerIntent((IEucData) null);
                }
            }
        };
    }

    /* renamed from: com.sec.internal.ims.servicemodules.euc.workflow.VolatileEucWorkflow$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
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

    /* access modifiers changed from: private */
    public void timeoutMessage(IEucData eucMessage) {
        EucMessageKey eucMessageKey = eucMessage.getKey();
        String eucId = eucMessage.getId();
        String str = LOG_TAG;
        Log.i(str, "Timeout message with id=" + eucId);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "Timeout message with key=" + eucMessageKey);
        if (!this.mHandleMap.containsKey(eucMessageKey)) {
            this.mDisplayManager.hide(eucMessageKey);
        } else {
            ((IEucStoreAndForward.IResponseHandle) this.mHandleMap.get(eucMessageKey)).invalidate();
        }
        try {
            this.mEucPersistence.updateEuc(eucMessageKey, EucState.TIMED_OUT, (String) null);
        } catch (EucPersistenceException e) {
            String str3 = LOG_TAG;
            Log.e(str3, "Unable to change EUCs state in persistence for EUCR with id=" + eucId);
            String str4 = LOG_TAG;
            IMSLog.s(str4, "Unable to change EUCs state in persistence for EUCR with key=" + eucMessageKey);
        } catch (Throwable th) {
            this.mCache.remove(eucMessageKey);
            throw th;
        }
        this.mCache.remove(eucMessageKey);
    }

    private Boolean isMessageTimedOut(IEucData message) {
        return Boolean.valueOf(getRemainingTimeout(message) < 0);
    }

    private long getRemainingTimeout(IEucData message) {
        return message.getTimeOut().longValue() - System.currentTimeMillis();
    }

    /* access modifiers changed from: private */
    public void scheduleNextAlarmTimerIntent(IEucData eucr) {
        IEucData eucrForAlarm = eucr;
        if (eucrForAlarm == null) {
            try {
                eucrForAlarm = this.mEucPersistence.getVolatileEucByMostRecentTimeout(this.mOwnIdentities);
            } catch (EucPersistenceException e) {
                String str = LOG_TAG;
                Log.e(str, "Unable to obtain EUCs from persistence: " + e);
            }
        }
        if (eucrForAlarm != null) {
            Pair<PendingIntent, IEucData> pair = this.mCurrentAlarm;
            if (pair != null) {
                if (getRemainingTimeout((IEucData) pair.second) > getRemainingTimeout(eucrForAlarm)) {
                    unscheduleCurrentAlarmTimerIntent();
                } else {
                    return;
                }
            }
            Pair<PendingIntent, IEucData> create = Pair.create(PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_EUCR_VOLATILE_TIMEOUT), 134217728), eucrForAlarm);
            this.mCurrentAlarm = create;
            AlarmTimer.start(this.mContext, (PendingIntent) create.first, getRemainingTimeout(eucrForAlarm));
        }
    }

    /* access modifiers changed from: private */
    public void unscheduleCurrentAlarmTimerIntent() {
        AlarmTimer.stop(this.mContext, (PendingIntent) this.mCurrentAlarm.first);
        this.mCurrentAlarm = null;
    }
}
