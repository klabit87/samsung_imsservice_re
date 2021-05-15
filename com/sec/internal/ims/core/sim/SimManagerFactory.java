package com.sec.internal.ims.core.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimManagerFactory {
    private static final String LOG_TAG = "SimManagerFactory";
    public static final int PHONE_ID_NON_EXISTING = -1;
    private static Context mContext;
    private static boolean mCreated = false;
    /* access modifiers changed from: private */
    public static RegistrantList mDDSChangeRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public static int mDefaultSimSubId = 0;
    /* access modifiers changed from: private */
    public static boolean mIsMultiSimSupported = false;
    private static Looper mLooper;
    private static final BroadcastReceiver mSimFactoryIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED.equals(intent.getAction())) {
                int phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SimManagerFactory.mSubMan);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                Log.i(SimManagerFactory.LOG_TAG, "DDS change intent received: subId=" + subId + ", slot=" + phoneId);
                int currentDefaultPhoneId = SimUtil.getDefaultPhoneId();
                if (!SimUtil.isValidSimSlot(phoneId) || subId < 0 || currentDefaultPhoneId == phoneId || SimManagerFactory.mDefaultSimSubId == subId) {
                    Log.i(SimManagerFactory.LOG_TAG, "Current default subId=" + SimManagerFactory.mDefaultSimSubId + " slot=" + currentDefaultPhoneId);
                    return;
                }
                SimUtil.setDefaultPhoneId(phoneId);
                int unused = SimManagerFactory.mDefaultSimSubId = subId;
                if (SimManagerFactory.mIsMultiSimSupported) {
                    SimManagerFactory.mDDSChangeRegistrants.notifyRegistrants();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public static RegistrantList mSubIdChangeRegistrants = new RegistrantList();
    /* access modifiers changed from: private */
    public static SubscriptionManager mSubMan;
    /* access modifiers changed from: private */
    public static volatile List<SimManager> sSimManagerList = new CopyOnWriteArrayList();

    public static void createInstance(Looper looper, Context context) {
        if (!mCreated) {
            mContext = context;
            mLooper = looper;
            SubscriptionManager from = SubscriptionManager.from(context);
            mSubMan = from;
            SimUtil.setSubMgr(from);
            int phoneCount = TelephonyManagerWrapper.getInstance(mContext).getPhoneCount();
            SimUtil.setPhoneCount(phoneCount);
            Log.i(LOG_TAG, "maxSimCount=" + phoneCount);
            mIsMultiSimSupported = phoneCount > 1;
            mDefaultSimSubId = SubscriptionManager.getDefaultDataSubscriptionId();
            Log.i(LOG_TAG, "Current default subId=" + mDefaultSimSubId);
            Log.i(LOG_TAG, "getConfigDualIMS = " + SimUtil.getConfigDualIMS());
            for (int i = 0; i < phoneCount; i++) {
                sSimManagerList.add(new SimManager(mLooper, mContext, i, mSubMan.getActiveSubscriptionInfoForSimSlotIndex(i), TelephonyManagerWrapper.getInstance(context)));
            }
            mCreated = true;
        }
    }

    public static void initInstances() {
        for (SimManager sm : sSimManagerList) {
            sm.initializeSimState();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ImsConstants.Intents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        mContext.registerReceiver(mSimFactoryIntentReceiver, filter);
        mSubMan.addOnSubscriptionsChangedListener(new SubscriptionManager.OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                boolean doNotify = false;
                List<SubscriptionInfo> subInfoList = SimManagerFactory.mSubMan.getActiveSubscriptionInfoList();
                if (subInfoList == null) {
                    Log.e(SimManagerFactory.LOG_TAG, "subInfoList is null");
                    return;
                }
                for (SubscriptionInfo subInfo : subInfoList) {
                    Log.i(SimManagerFactory.LOG_TAG, "onSubscriptionsChanged: subInfo=" + subInfo);
                    for (ISimManager manager : SimManagerFactory.sSimManagerList) {
                        if (manager.getSimSlotIndex() == subInfo.getSimSlotIndex()) {
                            if (manager.getSubscriptionId() != subInfo.getSubscriptionId()) {
                                doNotify = true;
                                manager.setSubscriptionInfo(subInfo);
                            } else {
                                Log.i(SimManagerFactory.LOG_TAG, "Do not notify: SubId is not changed.");
                            }
                        }
                    }
                    if (doNotify) {
                        SimManagerFactory.mSubIdChangeRegistrants.notifyResult(subInfo);
                    }
                }
                int phoneId = Extensions.SubscriptionManager.getDefaultDataPhoneId(SimManagerFactory.mSubMan);
                int subId = SubscriptionManager.getDefaultDataSubscriptionId();
                int currentDefaultPhoneId = SimUtil.getDefaultPhoneId();
                if (SimUtil.isValidSimSlot(phoneId) && subId >= 0) {
                    if (currentDefaultPhoneId != phoneId || SimManagerFactory.mDefaultSimSubId != subId) {
                        Log.i(SimManagerFactory.LOG_TAG, "Data subsciption changed: subId=" + subId + ", slot=" + phoneId);
                        SimUtil.setDefaultPhoneId(phoneId);
                        int unused = SimManagerFactory.mDefaultSimSubId = subId;
                        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
                        if (sm == null) {
                            Log.i(SimManagerFactory.LOG_TAG, "SimManagerMainInstance is not exist. Do not notify.");
                            return;
                        }
                        sm.notifyDDSChanged(phoneId);
                        if (SimManagerFactory.mIsMultiSimSupported) {
                            SimManagerFactory.mDDSChangeRegistrants.notifyRegistrants();
                        }
                    }
                }
            }
        });
    }

    public static List<? extends ISimManager> getAllSimManagers() {
        return sSimManagerList;
    }

    public static synchronized ISimManager getSimManager() {
        synchronized (SimManagerFactory.class) {
            for (ISimManager sm : sSimManagerList) {
                if (sm.getSimSlotIndex() == SimUtil.getDefaultPhoneId()) {
                    return sm;
                }
            }
            if (!sSimManagerList.isEmpty()) {
                Log.e(LOG_TAG, "Not matched. Return slot 0's.");
                ISimManager iSimManager = sSimManagerList.get(0);
                return iSimManager;
            }
            Log.e(LOG_TAG, "SimManager is not yet initiated!");
            return null;
        }
    }

    public static synchronized ISimManager getSimManagerFromSimSlot(int simSlot) {
        synchronized (SimManagerFactory.class) {
            for (ISimManager sm : sSimManagerList) {
                if (sm.getSimSlotIndex() == simSlot) {
                    return sm;
                }
            }
            IMSLog.i(LOG_TAG, simSlot, "getSimManagerFromSimSlot, No matched ISimManager. Return null..");
            return null;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized com.sec.internal.interfaces.ims.core.ISimManager getSimManagerFromSubId(int r4) {
        /*
            java.lang.Class<com.sec.internal.ims.core.sim.SimManagerFactory> r0 = com.sec.internal.ims.core.sim.SimManagerFactory.class
            monitor-enter(r0)
            java.util.List<com.sec.internal.ims.core.sim.SimManager> r1 = sSimManagerList     // Catch:{ all -> 0x0030 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x0030 }
        L_0x0009:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x0030 }
            if (r2 == 0) goto L_0x0026
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x0030 }
            com.sec.internal.interfaces.ims.core.ISimManager r2 = (com.sec.internal.interfaces.ims.core.ISimManager) r2     // Catch:{ all -> 0x0030 }
            int r3 = r2.getSubscriptionId()     // Catch:{ all -> 0x0030 }
            if (r3 != r4) goto L_0x0025
            if (r4 < 0) goto L_0x0023
            boolean r3 = r2.hasNoSim()     // Catch:{ all -> 0x0030 }
            if (r3 != 0) goto L_0x0025
        L_0x0023:
            monitor-exit(r0)
            return r2
        L_0x0025:
            goto L_0x0009
        L_0x0026:
            java.lang.String r1 = "SimManagerFactory"
            java.lang.String r2 = "getSimManagerFromSubId, No matched ISimManager. Return null.."
            com.sec.internal.log.IMSLog.i(r1, r4, r2)     // Catch:{ all -> 0x0030 }
            r1 = 0
            monitor-exit(r0)
            return r1
        L_0x0030:
            r4 = move-exception
            monitor-exit(r0)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.sim.SimManagerFactory.getSimManagerFromSubId(int):com.sec.internal.interfaces.ims.core.ISimManager");
    }

    public static void registerForDDSChange(Handler h, int what, Object obj) {
        mDDSChangeRegistrants.add(new Registrant(h, what, obj));
    }

    public static void registerForSubIdChange(Handler h, int what, Object obj) {
        mSubIdChangeRegistrants.add(new Registrant(h, what, obj));
    }

    public static void dump() {
        IMSLog.dump(LOG_TAG, "Dump of SimManagerFactory:");
        for (SimManager sm : sSimManagerList) {
            sm.dump();
        }
    }

    public static void notifySubscriptionIdChanged(SubscriptionInfo subInfo) {
        mSubIdChangeRegistrants.notifyResult(subInfo);
    }

    public static int getPhoneId(String ownIMSI) {
        for (ISimManager simManager : sSimManagerList) {
            if (simManager.isSimLoaded() && simManager.getImsi().equals(ownIMSI)) {
                return simManager.getSimSlotIndex();
            }
        }
        return -1;
    }

    public static String getImsiFromPhoneId(int phoneId) {
        ISimManager sm = getSimManagerFromSimSlot(phoneId);
        if (sm == null || !sm.isSimLoaded()) {
            return null;
        }
        return sm.getImsi();
    }

    public static boolean isOutboundSim(int phoneId) {
        ISimManager sm = getSimManagerFromSimSlot(phoneId);
        if (sm != null) {
            return sm.isOutBoundSIM();
        }
        Log.i(LOG_TAG, "isOutboundSim, sm is null");
        return false;
    }

    public static int getSlotId(int subId) {
        if (subId < 0) {
            IMSLog.e(LOG_TAG, subId, "subId is wrong");
            return -1;
        }
        ISimManager sm = getSimManagerFromSubId(subId);
        if (sm != null) {
            return sm.getSimSlotIndex();
        }
        Log.e(LOG_TAG, "Simmanager is not created yet");
        return -1;
    }
}
