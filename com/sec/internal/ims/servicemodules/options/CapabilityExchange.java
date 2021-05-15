package com.sec.internal.ims.servicemodules.options;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapabilityExchange {
    private static final String LOG_TAG = "CapabilityExchange";
    private static final int POLL_LIMIT = 1000;
    private static final int POLL_REMOVE_LIMIT = 100;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private SimpleEventLog mEventLog;
    private int room = 0;

    CapabilityExchange(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, SimpleEventLog eventLog) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mEventLog = eventLog;
    }

    /* access modifiers changed from: package-private */
    public void poll(Context mContext, boolean isPeriodic, int phoneId, Map<Integer, ImsRegistration> mImsRegInfoList, List<Date> mPollingHistory) {
        boolean retry;
        boolean z = isPeriodic;
        int i = phoneId;
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(i, "poll: isPeriodic = " + z + ", " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size() + " contacts");
        IMSLog.c(LogClass.CDM_POLL, i + "," + z + "," + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size());
        this.mCapabilityDiscovery.removeMessages(1);
        stopThrottledRetryTimer(mContext);
        if (!stopPoll(mImsRegInfoList, i)) {
            setThrottleContactSync(i);
            if (z && this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).isEmpty()) {
                fillPollingList(i, false);
            } else if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).isEmpty()) {
                IMSLog.i(LOG_TAG, i, "poll: no uris to request");
                return;
            }
            Date current = new Date();
            long pollingRatePeriod = this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingRatePeriod();
            trimPollingHistory(current, pollingRatePeriod, phoneId, mPollingHistory);
            this.room = this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingRate() - mPollingHistory.size();
            IMSLog.i(LOG_TAG, i, "poll: room: " + this.room + ", " + mPollingHistory.size() + " request sent in " + pollingRatePeriod + " seconds.");
            if (this.mCapabilityDiscovery.getCapabilityControl(i) == this.mCapabilityDiscovery.getPresenceModule()) {
                List<Date> list = mPollingHistory;
                retry = requestCapabilityForPresence(i, z, current);
            } else {
                retry = requestCapabilityForOptions(i, current, mPollingHistory);
            }
            if (retry) {
                throttledRetryTimer(mContext, phoneId, this.room, pollingRatePeriod, mPollingHistory, isPeriodic);
                return;
            }
            this.mCapabilityDiscovery.setforcePollingGuard(false);
            if (z) {
                delayPoll(i, current);
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(16, i, 0, (Object) null), 10000);
            }
        }
    }

    private void delayPoll(int phoneId, Date current) {
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        long delay = ((long) this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingPeriod()) * 1000;
        if (mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_RAND_DELAY_PERIODIC_POLL)) {
            delay = this.mCapabilityUtil.getRandomizedDelayForPeriodicPolling(phoneId, delay);
        }
        if (delay > 0) {
            this.mCapabilityDiscovery.startPollingTimer(delay);
            this.mCapabilityDiscovery.savePollTimestamp(current.getTime());
        }
    }

    private void throttledRetryTimer(Context mContext, int phoneId, int room2, long pollingRatePeriod, List<Date> mPollingHistory, boolean isPeriodic) {
        long mDelay;
        if (this.mCapabilityDiscovery.getCapabilityControl(phoneId) == this.mCapabilityDiscovery.getPresenceModule()) {
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollListSubExpiry() == 0) {
                mDelay = 30000;
            } else {
                mDelay = RcsPolicyManager.getRcsStrategy(phoneId).getThrottledDelay((long) this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollListSubExpiry()) * 1000;
            }
            if (room2 == 0 && pollingRatePeriod != 0) {
                mDelay = (1000 * pollingRatePeriod) - (((long) mPollingHistory.size()) * mDelay);
            }
        } else {
            mDelay = pollingRatePeriod * 1000;
        }
        startThrottledRetryTimer(mContext, isPeriodic, mDelay);
    }

    private boolean stopPoll(Map<Integer, ImsRegistration> mImsRegInfoList, int phoneId) {
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (!this.mCapabilityUtil.checkModuleReady(phoneId) || ((mnoStrategy != null && !mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED)) || (this.mCapabilityDiscovery.getCapabilityControl(phoneId) != null && !this.mCapabilityDiscovery.getCapabilityControl(phoneId).isReadyToRequest(phoneId)))) {
            IMSLog.i(LOG_TAG, phoneId, "poll: cancel poll request");
            this.mCapabilityDiscovery.setforcePollingGuard(false);
            return true;
        } else if (!mImsRegInfoList.isEmpty() && mImsRegInfoList.containsKey(Integer.valueOf(phoneId))) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, phoneId, "poll: not registered.");
            this.mCapabilityDiscovery.setforcePollingGuard(false);
            return true;
        }
    }

    private void setThrottleContactSync(int phoneId) {
        if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size() >= 1000) {
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(true);
        } else if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size() <= 100) {
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false);
        }
    }

    private void trimPollingHistory(Date current, long pollingRatePeriod, int phoneId, List<Date> mPollingHistory) {
        Iterator<Date> it = mPollingHistory.iterator();
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null) {
            while (it.hasNext()) {
                if (current.getTime() - it.next().getTime() > 1000 * pollingRatePeriod) {
                    it.remove();
                }
            }
        }
    }

    private boolean requestCapabilityForPresence(int phoneId, boolean isPeriodic, Date current) {
        CapabilityConstants.RequestType requestType;
        CapabilityConstants.RequestType requestType2;
        IMSLog.s(LOG_TAG, phoneId, "requestCapabilityForPresence:");
        int subscribed = 0;
        if (this.room > 0 || !isPeriodic) {
            List<ImsUri> urisToRequest = this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId));
            synchronized (urisToRequest) {
                if (urisToRequest.size() == 1) {
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    ImsUri imsUri = urisToRequest.get(0);
                    if (isPeriodic) {
                        requestType2 = CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC;
                    } else {
                        requestType2 = CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE;
                    }
                    if (capabilityDiscoveryModule.requestCapabilityExchange(imsUri, requestType2, false, phoneId)) {
                        subscribed = 1;
                        urisToRequest.clear();
                    }
                } else {
                    CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                    if (isPeriodic) {
                        requestType = CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC;
                    } else {
                        requestType = CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE;
                    }
                    subscribed = capabilityDiscoveryModule2.requestCapabilityExchange(urisToRequest, requestType, phoneId);
                }
            }
            this.mCapabilityDiscovery.putUrisToRequestList(phoneId, urisToRequest);
            if (subscribed > 1) {
                this.mCapabilityDiscovery.setLastListSubscribeStamp(current.getTime());
            }
            if (isPeriodic && subscribed > 0) {
                this.mCapabilityDiscovery.addPollingHistory(current);
            }
        }
        if (this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size() <= 0) {
            return false;
        }
        IMSLog.i(LOG_TAG, phoneId, "poll: remained mUrisToRequest size: " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size());
        return true;
    }

    private boolean requestCapabilityForOptions(int phoneId, Date current, List<Date> mPollingHistory) {
        IMSLog.s(LOG_TAG, phoneId, "requestCapabilityForOptions:");
        boolean retry = false;
        List<ImsUri> urisToRequest = this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId));
        synchronized (urisToRequest) {
            Iterator<ImsUri> it = urisToRequest.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ImsUri uri = it.next();
                if (this.room == 0) {
                    IMSLog.i(LOG_TAG, phoneId, "poll: room is 0");
                    break;
                }
                if (this.mCapabilityDiscovery.requestCapabilityExchange(uri, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, false, phoneId)) {
                    it.remove();
                    this.mCapabilityDiscovery.addPollingHistory(current);
                    this.room--;
                } else {
                    retry = true;
                }
                if (mPollingHistory.size() >= this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingRate()) {
                    break;
                }
            }
            if (urisToRequest.size() > 0) {
                IMSLog.i(LOG_TAG, phoneId, "poll: remained mUrisToRequest size: " + urisToRequest.size());
                retry = true;
            }
        }
        this.mCapabilityDiscovery.putUrisToRequestList(phoneId, urisToRequest);
        return retry;
    }

    /* access modifiers changed from: package-private */
    public boolean requestCapabilityExchange(ImsUri uri, CapabilityConstants.RequestType type, boolean isAlwaysForce, int phoneId, Capabilities ownCap, IRegistrationManager mRegMan, Map<Integer, ImsRegistration> mImsRegInfoList, String mCallNumber, int mNetworkType) {
        String extFeature;
        ImsUri imsUri = uri;
        int i = phoneId;
        IRegistrationManager iRegistrationManager = mRegMan;
        Map<Integer, ImsRegistration> map = mImsRegInfoList;
        int i2 = mNetworkType;
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) ownCap.getExtFeature())) {
            extFeature = String.join(",", ownCap.getExtFeature());
        } else {
            extFeature = "";
        }
        if (imsUri == null || this.mCapabilityDiscovery.getCapabilityControl(i) == null) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: uri or mControl is null");
            return false;
        }
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(phoneId);
        if (mnoStrategy == null) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: mnoStrategy is null.");
            return false;
        } else if (!this.mCapabilityUtil.isAllowedPrefixesUri(imsUri, i) && !ChatbotUriUtil.hasChatbotRoleSession(uri)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: isAllowedPrefixesUri and hasChatbotRoleSession are false.");
            return true;
        } else if (!mnoStrategy.isCapabilityValidUri(imsUri)) {
            IMSLog.e(LOG_TAG, i, "requestCapabilityExchange: isPresenceValidUri is false.");
            return true;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(imsUri, i)) {
            return true;
        } else {
            if (ChatbotUriUtil.hasUriBotPlatform(uri) && !TextUtils.isEmpty(imsUri.getParam("user"))) {
                IMSLog.i(LOG_TAG, i, "remove user=phone param for chatbot serviceId");
                uri.removeUserParam();
            }
            if (iRegistrationManager == null || mImsRegInfoList.isEmpty()) {
            } else {
                ImsRegistration imsRegistration = map.get(Integer.valueOf(phoneId));
                ImsRegistration regi = imsRegistration;
                if (imsRegistration == null) {
                    IMnoStrategy iMnoStrategy = mnoStrategy;
                } else if (iRegistrationManager.isSuspended(regi.getHandle())) {
                    IMSLog.i(LOG_TAG, i, "both phoneId 0 and phoneId 1 was suspended, cannot exchange capabilities.");
                    return false;
                } else {
                    IMSLog.s(LOG_TAG, i, "requestCapabilityExchange: uri = " + imsUri);
                    IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: " + uri.toStringLimit() + ", requesttype: " + type + ", isAlwaysForce: " + isAlwaysForce);
                    Set<String> services = iRegistrationManager.getServiceForNetwork(map.get(Integer.valueOf(phoneId)).getImsProfile(), i2, false, i);
                    Set<String> set = services;
                    IMnoStrategy iMnoStrategy2 = mnoStrategy;
                    return this.mCapabilityDiscovery.getCapabilityControl(i).requestCapabilityExchange(uri, (ICapabilityExchangeControl.ICapabilityExchangeCallback) null, type, isAlwaysForce, this.mCapabilityUtil.filterInCallFeatures(this.mCapabilityUtil.filterFeaturesWithService(ownCap.getFeature(), services, i2), imsUri, mCallNumber), phoneId, extFeature);
                }
            }
            IMSLog.i(LOG_TAG, i, "requestCapabilityExchange: mRegMan or ImsRegistration is null");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public int requestCapabilityExchange(List<ImsUri> uris, CapabilityConstants.RequestType requestType, int phoneId) {
        if (uris == null || uris.size() == 0 || this.mCapabilityDiscovery.getCapabilityControl(phoneId) == null) {
            return 0;
        }
        IMSLog.i(LOG_TAG, phoneId, "requestCapabilityExchange: " + uris.size() + " contacts");
        List<ImsUri> notAllowedUris = new ArrayList<>();
        for (ImsUri uri : uris) {
            if (!this.mCapabilityUtil.isAllowedPrefixesUri(uri, phoneId) && !ChatbotUriUtil.hasChatbotRoleSession(uri)) {
                notAllowedUris.add(uri);
            }
        }
        if (notAllowedUris.size() > 0) {
            IMSLog.s(LOG_TAG, phoneId, "requestCapabilityExchange: remove notAllowedUris = " + notAllowedUris);
            uris.removeAll(notAllowedUris);
            IMSLog.i(LOG_TAG, phoneId, "requestCapabilityExchange: " + uris.size() + " contacts after removed notAllowedUris");
            if (uris.size() == 0) {
                return 0;
            }
        }
        return this.mCapabilityDiscovery.getCapabilityControl(phoneId).requestCapabilityExchange(uris, requestType, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void requestInitialCapabilitiesQuery(int phoneId, boolean mInitialQuery, long mLastPollTimestamp) {
        IMSLog.i(LOG_TAG, phoneId, "requestInitialCapabilitiesQuery:");
        this.mCapabilityDiscovery.removeMessages(3, Integer.valueOf(phoneId));
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isDisableInitialScan()) {
            Log.i(LOG_TAG, "requestInitialCapabilitiesQuery: disable initial scan");
            if (!RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED) && mInitialQuery) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(16, phoneId, 0, (Object) null), 10000);
            }
        } else if (!this.mCapabilityDiscovery.getPhonebook().isReady()) {
            IMSLog.i(LOG_TAG, phoneId, "requestInitialCapabilitiesQuery: contact is not ready. retry in 1 second.");
            CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(3, Integer.valueOf(phoneId)), 1000);
        } else if (this.mCapabilityDiscovery.getCapabilityControl(phoneId) == null || !this.mCapabilityDiscovery.getCapabilityControl(phoneId).isReadyToRequest(phoneId) || this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null) {
            Log.i(LOG_TAG, "requestInitialCapabilitiesQuery: not ready. retry in 1 second.");
            CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule3.sendMessageDelayed(capabilityDiscoveryModule3.obtainMessage(3, Integer.valueOf(phoneId)), 1000);
        } else {
            Date current = new Date();
            IMSLog.i(LOG_TAG, phoneId, "requestInitialCapabilitiesQuery: current " + current.getTime() + " mLastPollTimestamp " + mLastPollTimestamp + " mPollingPeriod " + this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingPeriod());
            if (this.mCapabilityDiscovery.isPollingInProgress(current, phoneId)) {
                Log.i(LOG_TAG, "requestInitialCapabilitiesQuery: Polling already in progress");
            } else if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingPeriod() <= 0 || this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollingRate() <= 0) {
                if (!this.mCapabilityDiscovery.getCapabilityConfig(phoneId).usePresence() || !RcsPolicyManager.getRcsStrategy(phoneId).boolSetting(RcsPolicySettings.RcsPolicy.POLL_ALLOWED)) {
                    this.mCapabilityDiscovery.onContactChanged(mInitialQuery);
                    if (mInitialQuery) {
                        this.mCapabilityDiscovery.setInitialQuery(false);
                        return;
                    }
                    return;
                }
                this.mCapabilityDiscovery.onContactChanged(true);
            } else if (mLastPollTimestamp == 0) {
                Log.i(LOG_TAG, "requestInitialCapabilitiesQuery: Polling has not been performed yet, start polling");
                CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(1, true));
            } else {
                this.mCapabilityDiscovery.startPoll(current, phoneId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void forcePoll(boolean isPeriodic, int phoneId) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd(phoneId, "forcePoll forcePollingGuard = " + this.mCapabilityDiscovery.getForcePollingGuard());
        if (!this.mCapabilityDiscovery.getForcePollingGuard()) {
            this.mCapabilityDiscovery.setforcePollingGuard(true);
            new Thread(new Runnable(phoneId, isPeriodic) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    CapabilityExchange.this.lambda$forcePoll$0$CapabilityExchange(this.f$1, this.f$2);
                }
            }).start();
        }
    }

    public /* synthetic */ void lambda$forcePoll$0$CapabilityExchange(int phoneId, boolean isPeriodic) {
        fillPollingList(phoneId, true);
        this.mCapabilityDiscovery.poll(isPeriodic, phoneId);
    }

    private void fillPollingList(int phoneId, boolean addAll) {
        if (this.mCapabilityDiscovery.getCapabilitiesCache(phoneId) == null) {
            IMSLog.e(LOG_TAG, phoneId, "fillPollingList: CapabilitiesCache is null");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "fillPollingList");
        try {
            for (Capabilities capex : this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).getCapabilities()) {
                if (capex.getContactId() == null) {
                    IMSLog.s(LOG_TAG, phoneId, "skip, there is no contactId in capex for uri: " + capex.getUri());
                } else if (addAll || RcsPolicyManager.getRcsStrategy(phoneId).needPoll(capex, (long) this.mCapabilityUtil.getCapInfoExpiry(capex, phoneId))) {
                    this.mCapabilityDiscovery.updatePollList(capex.getUri(), true, phoneId);
                }
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void exchangeCapabilities(Map<Integer, ImsRegistration> mImsRegInfoList, IRegistrationManager mRegMan, String number, long myFeatures, int phoneId, String extFeature, String mCallNumber) {
        Map<Integer, ImsRegistration> map = mImsRegInfoList;
        IRegistrationManager iRegistrationManager = mRegMan;
        ImsUri uri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(number, true);
        if (uri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return;
        }
        Log.i(LOG_TAG, "exchangeCapabilities: myFeatures = " + Capabilities.dumpFeature(myFeatures));
        if (iRegistrationManager == null) {
            long j = myFeatures;
        } else if (!map.containsKey(Integer.valueOf(phoneId))) {
            long j2 = myFeatures;
        } else if (iRegistrationManager.isSuspended(map.get(Integer.valueOf(phoneId)).getHandle())) {
            Log.i(LOG_TAG, "cannot exchange capabilities. currently in suspend");
            return;
        } else {
            ImsUri imsUri = uri;
            this.mCapabilityDiscovery.getOptionsModule().requestCapabilityExchange(imsUri, (ICapabilityExchangeControl.ICapabilityExchangeCallback) null, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, true, this.mCapabilityUtil.filterInCallFeatures(myFeatures, uri, mCallNumber), phoneId, extFeature);
            return;
        }
        Log.i(LOG_TAG, "exchangeCapabilities: mRegMan or mImsRegInfo is null");
    }

    private void startThrottledRetryTimer(Context mContext, boolean isPeriodic, long millis) {
        stopThrottledRetryTimer(mContext);
        Log.i(LOG_TAG, "startThrottledRetryTimer: isPeriodic = " + isPeriodic + ", millis " + millis);
        if (millis < 60000) {
            PreciseAlarmManager.getInstance(mContext).sendMessageDelayed(getClass().getSimpleName(), this.mCapabilityDiscovery.obtainMessage(1, Boolean.valueOf(isPeriodic)), millis);
            return;
        }
        Intent intent = new Intent("com.sec.internal.ims.servicemodules.options.sub_throttled_timeout");
        intent.putExtra("IS_PERIODIC", isPeriodic);
        this.mCapabilityDiscovery.setThrottledIntent(PendingIntent.getBroadcast(mContext, 0, intent, 134217728));
        AlarmTimer.start(mContext, this.mCapabilityDiscovery.getThrottledIntent(), millis);
    }

    private void stopThrottledRetryTimer(Context mContext) {
        Log.i(LOG_TAG, "stopThrottledRetryTimer");
        PreciseAlarmManager.getInstance(mContext).removeMessage(this.mCapabilityDiscovery.obtainMessage(1));
        if (this.mCapabilityDiscovery.getThrottledIntent() != null) {
            AlarmTimer.stop(mContext, this.mCapabilityDiscovery.getThrottledIntent());
            this.mCapabilityDiscovery.setThrottledIntent((PendingIntent) null);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0073, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updatePollList(com.sec.ims.util.ImsUri r6, boolean r7, int r8) {
        /*
            r5 = this;
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r8)
            r1 = 0
            if (r0 != 0) goto L_0x0010
            java.lang.String r2 = "CapabilityExchange"
            java.lang.String r3 = "updatePollList: mnoStrategy is null."
            android.util.Log.e(r2, r3)
            return r1
        L_0x0010:
            boolean r2 = r0.isCapabilityValidUri(r6)
            if (r2 != 0) goto L_0x001f
            java.lang.String r2 = "CapabilityExchange"
            java.lang.String r3 = "updatePollList: isCapabilityValidUri is false."
            android.util.Log.e(r2, r3)
            return r1
        L_0x001f:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "updatePollList: uri = "
            r2.append(r3)
            r2.append(r6)
            java.lang.String r3 = ", needAdd = "
            r2.append(r3)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "CapabilityExchange"
            com.sec.internal.log.IMSLog.s(r3, r8, r2)
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r2 = r5.mCapabilityDiscovery
            java.util.Map r2 = r2.getUrisToRequest()
            java.lang.Integer r3 = java.lang.Integer.valueOf(r8)
            java.lang.Object r2 = r2.get(r3)
            java.util.List r2 = (java.util.List) r2
            monitor-enter(r2)
            r3 = 1
            if (r7 == 0) goto L_0x0062
            boolean r4 = r2.contains(r6)     // Catch:{ all -> 0x0074 }
            if (r4 != 0) goto L_0x0072
            r2.add(r6)     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r1 = r5.mCapabilityDiscovery     // Catch:{ all -> 0x0074 }
            r1.putUrisToRequestList(r8, r2)     // Catch:{ all -> 0x0074 }
            monitor-exit(r2)     // Catch:{ all -> 0x0074 }
            return r3
        L_0x0062:
            boolean r4 = r2.contains(r6)     // Catch:{ all -> 0x0074 }
            if (r4 == 0) goto L_0x0072
            r2.remove(r6)     // Catch:{ all -> 0x0074 }
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r1 = r5.mCapabilityDiscovery     // Catch:{ all -> 0x0074 }
            r1.putUrisToRequestList(r8, r2)     // Catch:{ all -> 0x0074 }
            monitor-exit(r2)     // Catch:{ all -> 0x0074 }
            return r3
        L_0x0072:
            monitor-exit(r2)     // Catch:{ all -> 0x0074 }
            return r1
        L_0x0074:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0074 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityExchange.updatePollList(com.sec.ims.util.ImsUri, boolean, int):boolean");
    }
}
