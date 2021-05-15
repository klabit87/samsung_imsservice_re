package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.Contact;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapabilityUpdate {
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityUpdate";
    private static final long MAX_LAST_SEEN = 43200;
    private static final int MAX_RETRY_SYNC_CONTACT_COUNT = 10;
    private static final int MINUTE_DENOMINATION = 60000;
    private static final int RETRY_SYNC_CONTACT_DELAY = 30000;
    protected Handler mBackgroundHandler;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private SimpleEventLog mEventLog;
    IRegistrationManager mRegMan;

    CapabilityUpdate(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, IRegistrationManager rm, SimpleEventLog eventLog) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = rm;
        this.mEventLog = eventLog;
        HandlerThread thread = new HandlerThread(LOG_TAG, 10);
        thread.start();
        this.mBackgroundHandler = new Handler(thread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void updateOwnCapabilities(Context context, Map<Integer, ImsRegistration> mImsRegInfoList, int phoneId, boolean mIsConfiguredOnCapability, int mNetworkType) {
        long features = 0;
        for (ServiceModuleBase module : ImsRegistry.getAllServiceModules()) {
            if (module != this.mCapabilityDiscovery) {
                features |= module.getSupportFeature(phoneId);
            }
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isLastSeenActive() && DmConfigHelper.getImsSwitchValue(context, "lastseen", phoneId) == 1) {
            features |= Capabilities.FEATURE_LAST_SEEN_ACTIVE;
        }
        Mno mno = SimUtil.getSimMno(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "updateOwnCapabilities: mIsConfiguredOnCapability is " + mIsConfiguredOnCapability + ", features from all module is " + Long.toHexString(features));
        if (this.mRegMan != null && mImsRegInfoList.containsKey(Integer.valueOf(phoneId)) && mIsConfiguredOnCapability && ConfigUtil.isRcsEur(mno)) {
            if (mno.isRjil()) {
                mNetworkType = this.mRegMan.getCurrentNetworkByPhoneId(phoneId);
                this.mCapabilityDiscovery.setNetworkType(mNetworkType);
            }
            features = this.mCapabilityUtil.filterFeaturesWithService(features, this.mRegMan.getServiceForNetwork(mImsRegInfoList.get(Integer.valueOf(phoneId)).getImsProfile(), mNetworkType, false, phoneId), mNetworkType);
            if (RcsUtils.DualRcs.isDualRcsReg() && phoneId != SimUtil.getDefaultPhoneId()) {
                features = this.mCapabilityUtil.filterEnrichedCallFeatures(features);
            }
        }
        IMSLog.s(LOG_TAG, phoneId, "updateOwnCapabilities: filtered features is " + Long.toHexString(features));
        Capabilities ownCap = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId));
        ownCap.setFeatures(features);
        ownCap.setAvailableFeatures(features);
        this.mCapabilityDiscovery.putOwnList(phoneId, ownCap);
        this.mCapabilityDiscovery.setIsConfigured(true, phoneId);
        this.mCapabilityDiscovery.setIsConfiguredOnCapability(true, phoneId);
    }

    private void processContactChanged(boolean initial, int phoneId, boolean isOfflineAddedContact, long mLastListSubscribeStamp) {
        this.mBackgroundHandler.post(new Runnable(initial, phoneId, isOfflineAddedContact, mLastListSubscribeStamp) {
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ boolean f$3;
            public final /* synthetic */ long f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                CapabilityUpdate.this.lambda$processContactChanged$0$CapabilityUpdate(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$processContactChanged$0$CapabilityUpdate(boolean initial, int phoneId, boolean isOfflineAddedContact, long mLastListSubscribeStamp) {
        boolean isPollRequired;
        ImsUri teluri;
        int i = phoneId;
        Map<String, Contact> contacts = this.mCapabilityDiscovery.getPhonebook().getContacts();
        boolean isPollRequired2 = initial;
        this.mEventLog.logAndAdd(i, "processContactChanged: " + contacts.size() + " contacts.");
        IMSLog.c(LogClass.CDM_CON_CHANGE, i + "," + contacts.size());
        Date current = new Date();
        for (Contact contact : contacts.values()) {
            for (Contact.ContactNumber cn : contact.getContactNumberList()) {
                if (cn.getNumber() != null && cn.getNumber().startsWith("*")) {
                    teluri = null;
                } else if (cn.getNormalizedNumber() != null) {
                    teluri = ImsUri.parse("tel:" + cn.getNormalizedNumber());
                } else {
                    teluri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(cn.getNumber(), true);
                }
                if (!this.mCapabilityUtil.blockOptionsToOwnUri(teluri, i) && teluri != null && !this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).contains(teluri)) {
                    Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(teluri);
                    if (capex == null) {
                        Capabilities capex2 = new Capabilities(teluri, UriUtil.getMsisdnNumber(teluri), contact.getId(), -1, contact.getName());
                        capex2.resetFeatures();
                        capex2.setPhoneId(i);
                        this.mCapabilityDiscovery.getCapabilitiesCache(i).add(capex2);
                        Capabilities capabilities = capex2;
                    } else if (capex.getContactId() == null) {
                        this.mCapabilityDiscovery.getCapabilitiesCache(i).updateContactInfo(teluri, UriUtil.getMsisdnNumber(teluri), contact.getId(), contact.getName());
                        this.mCapabilityDiscovery.getCapabilitiesCache(i).persistCachedUri(teluri);
                    } else if (!capex.getContactId().equals(contact.getId())) {
                        this.mCapabilityDiscovery.getCapabilitiesCache(i).updateContactInfo(teluri, UriUtil.getMsisdnNumber(teluri), contact.getId(), contact.getName());
                    }
                    if (this.mCapabilityDiscovery.updatePollList(teluri, true, i)) {
                        isPollRequired2 = true;
                    }
                }
            }
        }
        this.mEventLog.logAndAdd(i, "processContactChanged: updatePollList done, " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(phoneId)).size() + " contacts added");
        this.mCapabilityUtil.handleRemovedNumbers(i);
        if (this.mCapabilityDiscovery.getUrisToRequest().values().isEmpty() || isPollRequired2 || !isOfflineAddedContact) {
            isPollRequired = isPollRequired2;
        } else {
            IMSLog.i(LOG_TAG, i, "processContactChanged: added an contact when RCS offline. need to poll");
            this.mCapabilityDiscovery.setIsOfflineAddedContact(false);
            isPollRequired = true;
        }
        if (!needPollOnContactChanged(isPollRequired, current, phoneId, mLastListSubscribeStamp)) {
            IMSLog.i(LOG_TAG, i, "processContactChanged: no need to poll now");
        }
    }

    /* access modifiers changed from: package-private */
    public void setOwnCapabilities(Context context, int phoneId, boolean notifyToRm, Map<Integer, ImsRegistration> mImsRegInfoList, int mNetworkType, boolean mIsInCall, String mCallNumber) {
        long features;
        IMSLog.i(LOG_TAG, phoneId, "setOwnCapabilities:");
        Capabilities ownCap = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId));
        Set<String> filteredServices = this.mCapabilityUtil.filterServicesWithReg(mImsRegInfoList, this.mRegMan, mNetworkType, phoneId);
        if (filteredServices != null) {
            long features2 = this.mCapabilityUtil.filterFeaturesWithService(ownCap.getFeature(), filteredServices, mNetworkType);
            if (!RcsUtils.DualRcs.isDualRcsReg() || phoneId == SimUtil.getDefaultPhoneId()) {
                features = this.mCapabilityUtil.filterFeaturesWithCallState(features2, mIsInCall, mCallNumber);
            } else {
                features = this.mCapabilityUtil.filterEnrichedCallFeatures(features2);
            }
            this.mCapabilityDiscovery.setHasVideoOwnCapability(CapabilityUtil.hasFeature(features, (long) Capabilities.FEATURE_MMTEL_VIDEO), phoneId);
            IMSLog.i(LOG_TAG, phoneId, "setOwnCapabilities: mHasVideoOwn = " + this.mCapabilityDiscovery.hasVideoOwnCapability(phoneId));
            IMSLog.c(LogClass.CDM_SET_OWNCAPA, phoneId + ",SETOWN:" + features);
            this.mCapabilityDiscovery.getOptionsModule().setOwnCapabilities(features, phoneId);
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).usePresence()) {
                this.mCapabilityDiscovery.getPresenceModule().setOwnCapabilities(features, phoneId);
            }
        }
        if (ownCap.getUri() != null) {
            if (this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).get(ownCap.getUri()) == null) {
                IMSLog.i(LOG_TAG, phoneId, "setOwnCapabilities: Add ownCap to CapabilitiesCache");
                try {
                    this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).add(ownCap.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            } else {
                IMSLog.i(LOG_TAG, phoneId, "setOwnCapabilities: updateOwnCapabilities");
                this.mCapabilityDiscovery.updateOwnCapabilities(phoneId);
            }
        }
        if (notifyToRm) {
            this.mRegMan.setOwnCapabilities(phoneId, ownCap);
        }
        this.mCapabilityDiscovery.notifyOwnCapabilitiesChanged(phoneId);
    }

    private boolean needPollOnContactChanged(boolean isPollRequired, Date current, int phoneId, long mLastListSubscribeStamp) {
        if (!isPollRequired) {
            Log.i(LOG_TAG, "needPollOnContactChanged: isPollRequired is false.");
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false);
            return false;
        } else if (this.mCapabilityDiscovery.getUrisToRequest().values().isEmpty()) {
            Log.i(LOG_TAG, "needPollOnContactChanged: No URI to request.");
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false);
            return false;
        } else {
            Mno mMno = SimUtil.getSimMno(phoneId);
            if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) != null && this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isDisableInitialScan() && (mMno == Mno.RJIL || mMno == Mno.CMCC || mMno == Mno.VODAFONE_INDIA || mMno == Mno.IDEA_INDIA)) {
                Log.i(LOG_TAG, "needPollOnContactChanged: Address book scan disabled.");
                return false;
            } else if (this.mCapabilityDiscovery.isPollingInProgress(current, phoneId)) {
                if (this.mCapabilityDiscovery.getThrottledIntent() == null) {
                    Log.i(LOG_TAG, "needPollOnContactChanged: posting delayed poll event");
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(1, false), this.mCapabilityUtil.getDelayTimeToPoll(mLastListSubscribeStamp, phoneId));
                    return true;
                }
                Log.i(LOG_TAG, "needPollOnContactChanged: polling already in progress");
                return false;
            } else if (this.mCapabilityDiscovery.getCapabilityControl(phoneId) == null || !this.mCapabilityDiscovery.getCapabilityControl(phoneId).isReadyToRequest(phoneId) || !this.mCapabilityDiscovery.isRunning()) {
                Log.i(LOG_TAG, "needPollOnContactChanged: new contact was added but RCS not work");
                this.mCapabilityDiscovery.setIsOfflineAddedContact(true);
                return false;
            } else {
                Log.i(LOG_TAG, "needPollOnContactChanged: posting poll event");
                this.mCapabilityDiscovery.removeMessages(1);
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(1, false), this.mCapabilityUtil.getDelayTimeToPoll(mLastListSubscribeStamp, phoneId));
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPollingInProgress(Date current, int phoneId, List<Date> mPollingHistory) {
        if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null) {
            Log.e(LOG_TAG, "isPollingInProgress: mConfig for phoneId: " + phoneId + " is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isPollingPeriodUpdated()) {
            this.mCapabilityDiscovery.getCapabilityConfig(phoneId).resetPollingPeriodUpdated();
            Log.i(LOG_TAG, "isPollingPeriodUpdated: " + this.mCapabilityDiscovery.getCapabilityConfig(phoneId).isPollingPeriodUpdated());
            return false;
        } else if (this.mCapabilityDiscovery.getThrottledIntent() != null) {
            Log.i(LOG_TAG, "isPollingInProgress: subscribe throttle in progress");
            return true;
        } else {
            for (Date pollDate : mPollingHistory) {
                if (current.getTime() - pollDate.getTime() < ((long) this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getPollListSubExpiry()) * 1000) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onUpdateCapabilities(List<ImsUri> uris, long availFeatures, CapabilityConstants.CapExResult result, String pidf, int lastSeen, List<ImsUri> paidList, int phoneId, boolean isTokenUsed, String extFeature, String mCallNumber) {
        List<ImsUri> list = uris;
        CapabilityConstants.CapExResult capExResult = result;
        int i = phoneId;
        if (list == null) {
            IMSLog.i(LOG_TAG, i, "onUpdateCapabilities: uris null, return");
            return;
        }
        List<String> numberList = new ArrayList<>();
        for (ImsUri uri : uris) {
            if (uri != null) {
                numberList.add(uri.toStringLimit());
            }
        }
        long availFeatures2 = this.mCapabilityUtil.filterInCallFeatures(availFeatures, list.get(0), mCallNumber);
        IMSLog.s(LOG_TAG, i, "onUpdateCapabilities: uriList " + list);
        IMSLog.i(LOG_TAG, i, "onUpdateCapabilities: " + numberList + " result " + capExResult + " features " + Capabilities.dumpFeature(availFeatures2));
        this.mCapabilityDiscovery.setLastCapExResult(capExResult, i);
        if (this.mCapabilityUtil.checkModuleReady(i)) {
            processUpdateCapabilities(uris, availFeatures2, result, pidf, lastSeen, paidList, phoneId, isTokenUsed, extFeature);
        }
    }

    private void processUpdateCapabilities(List<ImsUri> uris, long availFeatures, CapabilityConstants.CapExResult result, String pidf, int lastSeen, List<ImsUri> paidList, int phoneId, boolean isTokenUsed, String extFeature) {
        this.mBackgroundHandler.post(new Runnable(phoneId, uris, availFeatures, result, pidf, lastSeen, paidList, isTokenUsed, extFeature) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ List f$2;
            public final /* synthetic */ long f$3;
            public final /* synthetic */ CapabilityConstants.CapExResult f$4;
            public final /* synthetic */ String f$5;
            public final /* synthetic */ int f$6;
            public final /* synthetic */ List f$7;
            public final /* synthetic */ boolean f$8;
            public final /* synthetic */ String f$9;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r6;
                this.f$5 = r7;
                this.f$6 = r8;
                this.f$7 = r9;
                this.f$8 = r10;
                this.f$9 = r11;
            }

            public final void run() {
                CapabilityUpdate.this.lambda$processUpdateCapabilities$1$CapabilityUpdate(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9);
            }
        });
    }

    public /* synthetic */ void lambda$processUpdateCapabilities$1$CapabilityUpdate(int phoneId, List uris, long availFeatures, CapabilityConstants.CapExResult result, String pidf, int lastSeen, List paidList, boolean isTokenUsed, String extFeature) {
        int i = phoneId;
        long features = availFeatures;
        CapabilityConstants.CapExResult capExResult = result;
        boolean isSupportExpCapInfoExpiry = this.mCapabilityDiscovery.getCapabilityConfig(i).getIsSupportExpCapInfoExpiry();
        List<ImsUri> expiredList = new ArrayList<>();
        List<ImsUri> normalizedUris = new ArrayList<>();
        Iterator it = uris.iterator();
        while (it.hasNext()) {
            normalizedUris.add(this.mCapabilityDiscovery.getUriGenerator().normalize((ImsUri) it.next()));
        }
        ImsUri uri = LOG_TAG;
        IMSLog.s(uri, i, "processUpdateCapabilities,run, normalizedUris " + normalizedUris);
        boolean hasCapChanged = false;
        int expCapInfoExpiry = 0;
        for (ImsUri uri2 : normalizedUris) {
            Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(uri2);
            long updatedAvailFeatures = RcsPolicyManager.getRcsStrategy(phoneId).updateAvailableFeatures(capex, features, capExResult);
            long features2 = RcsPolicyManager.getRcsStrategy(phoneId).updateFeatures(capex, updatedAvailFeatures, capExResult);
            long updatedAvailFeatures2 = updatedAvailFeatures;
            List<ImsUri> normalizedUris2 = normalizedUris;
            Capabilities capex2 = capex;
            ImsUri imsUri = uri;
            ImsUri uri3 = uri2;
            if (!RcsPolicyManager.getRcsStrategy(phoneId).needCapabilitiesUpdate(result, capex, features2, this.mCapabilityDiscovery.getCapabilityConfig(i).getCapCacheExpiry())) {
                expiredList.add(uri3);
                features = availFeatures;
                normalizedUris = normalizedUris2;
                uri = imsUri;
            } else {
                if (capex2 != null && isSupportExpCapInfoExpiry) {
                    expCapInfoExpiry = this.mCapabilityUtil.updateExpCapInfoExpiry(capex2, features2, i);
                }
                boolean hasCapChanged2 = hasCapChanged | this.mCapabilityDiscovery.getCapabilitiesCache(i).update(uri3, features2, updatedAvailFeatures2, capExResult == CapabilityConstants.CapExResult.POLLING_SUCCESS, pidf, (long) lastSeen, new Date(), paidList, isTokenUsed, extFeature, expCapInfoExpiry);
                StringBuilder sb = new StringBuilder();
                sb.append("processUpdateCapabilities: ");
                sb.append(uri3 != null ? uri3.toStringLimit() : null);
                sb.append(" is updated, features: ");
                sb.append(Long.toHexString(features2));
                sb.append(", hasCapChanged: ");
                sb.append(hasCapChanged2);
                ImsUri imsUri2 = imsUri;
                IMSLog.i(imsUri2, i, sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append(i);
                sb2.append(",");
                sb2.append(uri3 != null ? uri3.toStringLimit() : "xx");
                sb2.append(",");
                sb2.append(Long.toHexString(features2));
                sb2.append(",");
                long j = features2;
                sb2.append(availFeatures);
                IMSLog.c(LogClass.CDM_UPD_CAPA, sb2.toString());
                this.mCapabilityUtil.sendGateMessage(uri3, updatedAvailFeatures2, i);
                features = availFeatures;
                hasCapChanged = hasCapChanged2;
                uri = imsUri2;
                normalizedUris = normalizedUris2;
            }
        }
        int i2 = lastSeen;
        List<ImsUri> normalizedUris3 = normalizedUris;
        normalizedUris3.removeAll(expiredList);
        if (normalizedUris3.size() > 0) {
            Capabilities capex3 = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(normalizedUris3.get(0));
            if (capex3 != null) {
                if (CapabilityUtil.hasFeature(capex3.getFeature(), Capabilities.FEATURE_CHATBOT_ROLE)) {
                    capex3.setBotServiceId(BotServiceIdTranslator.getInstance().translate(((ImsUri) uris.get(0)).getMsisdn()));
                } else {
                    List list = uris;
                }
                this.mCapabilityDiscovery.notifyCapabilitiesChanged(normalizedUris3, capex3, i);
                return;
            }
            List list2 = uris;
            return;
        }
        List list3 = uris;
    }

    /* access modifiers changed from: package-private */
    public void _syncContact(Mno mno) {
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) == null) {
            Log.e(LOG_TAG, "_syncContact: MnoStrategy is null");
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(10, mno), 1000);
            return;
        }
        if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            this.mCapabilityDiscovery.setUriGenerator(UriGeneratorFactory.getInstance().get());
            this.mCapabilityDiscovery.getPhonebook().setUriGenerator(this.mCapabilityDiscovery.getUriGenerator());
        }
        Log.i(LOG_TAG, "_syncContact: initial startContactSync");
        this.mCapabilityDiscovery.getPhonebook().setMno(mno);
        if (this.mCapabilityDiscovery.getPhonebook().getContactProviderStatus() >= 0) {
            this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
            return;
        }
        Log.i(LOG_TAG, "_syncContact: contactProvider is not yet ready");
        IMSLog.c(LogClass.CDM_SYNC_CONT, "N,CP:NOTREADY");
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int phoneId) {
        this.mCapabilityDiscovery.updateOwnCapabilities(phoneId);
        this.mRegMan.setOwnCapabilities(phoneId, this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId)));
        IMSLog.i(LOG_TAG, phoneId, "onOwnCapabilitiesChanged: " + Capabilities.dumpFeature(this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId)).getFeature()));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x00a8  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0125  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01c1  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x01e0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prepareResponse(android.content.Context r22, java.util.List<com.sec.ims.util.ImsUri> r23, long r24, java.lang.String r26, int r27, java.lang.String r28, java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r29, int r30, java.lang.String r31) {
        /*
            r21 = this;
            r0 = r21
            r1 = r23
            r2 = r24
            r12 = r27
            r13 = r29
            r14 = r30
            java.lang.String r4 = "CapabilityUpdate"
            java.lang.String r5 = "prepareResponse"
            com.sec.internal.log.IMSLog.i(r4, r12, r5)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r0.mRegMan
            if (r5 == 0) goto L_0x01e5
            java.lang.Integer r5 = java.lang.Integer.valueOf(r27)
            boolean r5 = r13.containsKey(r5)
            if (r5 != 0) goto L_0x0024
            goto L_0x01e5
        L_0x0024:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r5 = r0.mRegMan
            java.lang.Integer r6 = java.lang.Integer.valueOf(r27)
            java.lang.Object r6 = r13.get(r6)
            com.sec.ims.ImsRegistration r6 = (com.sec.ims.ImsRegistration) r6
            com.sec.ims.settings.ImsProfile r6 = r6.getImsProfile()
            r7 = 0
            java.util.Set r15 = r5.getServiceForNetwork(r6, r14, r7, r12)
            int r5 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r5 = (long) r5
            java.lang.String r8 = ""
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r9 = r0.mCapabilityDiscovery
            java.util.Map r9 = r9.getOwnList()
            java.lang.Integer r10 = java.lang.Integer.valueOf(r27)
            java.lang.Object r9 = r9.get(r10)
            com.sec.ims.options.Capabilities r9 = (com.sec.ims.options.Capabilities) r9
            java.util.List r11 = r9.getExtFeature()
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.lang.String) r28)
            if (r9 != 0) goto L_0x007f
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r11)
            if (r9 != 0) goto L_0x007f
            java.util.ArrayList r9 = new java.util.ArrayList
            java.lang.String r10 = ","
            r7 = r28
            java.lang.String[] r17 = r7.split(r10)
            java.util.List r7 = java.util.Arrays.asList(r17)
            r9.<init>(r7)
            r7 = r9
            r7.retainAll(r11)
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r7)
            if (r9 != 0) goto L_0x007f
            java.lang.String r8 = java.lang.String.join(r10, r7)
            r10 = r8
            goto L_0x0080
        L_0x007f:
            r10 = r8
        L_0x0080:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Common extfeature: "
            r7.append(r8)
            r7.append(r10)
            java.lang.String r7 = r7.toString()
            android.util.Log.i(r4, r7)
            r7 = -1
            r8 = 0
            java.lang.Object r9 = r1.get(r8)
            com.sec.ims.util.ImsUri r9 = (com.sec.ims.util.ImsUri) r9
            java.lang.String r8 = r9.getMsisdn()
            r9 = r22
            boolean r8 = com.sec.internal.helper.BlockedNumberUtil.isBlockedNumber(r9, r8)
            if (r8 != 0) goto L_0x0125
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r8 = r0.mCapabilityUtil
            r17 = r7
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r7 = r0.mCapabilityDiscovery
            java.util.Map r7 = r7.getOwnList()
            java.lang.Integer r9 = java.lang.Integer.valueOf(r27)
            java.lang.Object r7 = r7.get(r9)
            com.sec.ims.options.Capabilities r7 = (com.sec.ims.options.Capabilities) r7
            r18 = r10
            long r9 = r7.getFeature()
            long r5 = r8.filterFeaturesWithService(r9, r15, r14)
            int r7 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r7 = (long) r7
            int r7 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
            if (r7 == 0) goto L_0x00de
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r7 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.CapabilityConfig r7 = r7.getCapabilityConfig(r12)
            java.lang.String r7 = r7.getRcsProfile()
            boolean r7 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r7)
            if (r7 != 0) goto L_0x00de
            long r5 = r5 & r2
        L_0x00de:
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r7 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.CapabilityConfig r7 = r7.getCapabilityConfig(r12)
            if (r7 == 0) goto L_0x010e
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r7 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.CapabilityConfig r7 = r7.getCapabilityConfig(r12)
            boolean r7 = r7.isLastSeenActive()
            if (r7 == 0) goto L_0x010e
            java.lang.String r7 = "lastseen"
            boolean r7 = r0.isServiceRegistered(r7, r13)
            if (r7 == 0) goto L_0x010e
            long r7 = com.sec.ims.options.Capabilities.FEATURE_LAST_SEEN_ACTIVE
            boolean r7 = com.sec.internal.ims.servicemodules.options.CapabilityUtil.hasFeature(r2, r7)
            if (r7 == 0) goto L_0x010e
            java.lang.String r7 = "setting last seen active"
            com.sec.internal.log.IMSLog.s(r4, r7)
            int r4 = r0.getLastSeen(r12)
            r7 = r4
            goto L_0x0110
        L_0x010e:
            r7 = r17
        L_0x0110:
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r4 = r0.mCapabilityUtil
            r8 = 0
            java.lang.Object r9 = r1.get(r8)
            com.sec.ims.util.ImsUri r9 = (com.sec.ims.util.ImsUri) r9
            r10 = r31
            long r5 = r4.filterInCallFeatures(r5, r9, r10)
            r19 = r5
            r17 = r7
            goto L_0x01bb
        L_0x0125:
            r17 = r7
            r18 = r10
            r10 = r31
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r7 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r27)
            java.lang.String r8 = "block_msg"
            boolean r7 = r7.boolSetting(r8)
            if (r7 == 0) goto L_0x0192
            java.util.HashSet r7 = new java.util.HashSet
            java.lang.String[] r8 = com.sec.ims.settings.ImsProfile.getChatServiceList()
            java.util.List r8 = java.util.Arrays.asList(r8)
            r7.<init>(r8)
            r15.retainAll(r7)
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r8 = r0.mCapabilityUtil
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r9 = r0.mCapabilityDiscovery
            java.util.Map r9 = r9.getOwnList()
            r19 = r7
            java.lang.Integer r7 = java.lang.Integer.valueOf(r27)
            java.lang.Object r7 = r9.get(r7)
            com.sec.ims.options.Capabilities r7 = (com.sec.ims.options.Capabilities) r7
            long r9 = r7.getFeature()
            long r5 = r8.filterFeaturesWithService(r9, r15, r14)
            int r7 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r7 = (long) r7
            int r7 = (r2 > r7 ? 1 : (r2 == r7 ? 0 : -1))
            if (r7 == 0) goto L_0x017b
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r7 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.CapabilityConfig r7 = r7.getCapabilityConfig(r12)
            java.lang.String r7 = r7.getRcsProfile()
            boolean r7 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r7)
            if (r7 != 0) goto L_0x017b
            long r5 = r5 & r2
        L_0x017b:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Number is blocked respond with Chat tag : "
            r7.append(r8)
            r7.append(r5)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.s(r4, r7)
            r19 = r5
            goto L_0x01bb
        L_0x0192:
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "Number is blocked respond with empty tags : "
            r7.append(r8)
            r7.append(r5)
            java.lang.String r8 = " "
            r7.append(r8)
            r8 = 0
            java.lang.Object r9 = r1.get(r8)
            com.sec.ims.util.ImsUri r9 = (com.sec.ims.util.ImsUri) r9
            java.lang.String r8 = r9.getMsisdn()
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            com.sec.internal.log.IMSLog.s(r4, r7)
            r19 = r5
        L_0x01bb:
            boolean r4 = r23.isEmpty()
            if (r4 != 0) goto L_0x01e0
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r4 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.OptionsModule r4 = r4.getOptionsModule()
            r5 = 0
            java.lang.Object r5 = r1.get(r5)
            com.sec.ims.util.ImsUri r5 = (com.sec.ims.util.ImsUri) r5
            r6 = r19
            r8 = r26
            r9 = r17
            r16 = r18
            r10 = r27
            r18 = r11
            r11 = r16
            r4.sendCapexResponse(r5, r6, r8, r9, r10, r11)
            goto L_0x01e4
        L_0x01e0:
            r16 = r18
            r18 = r11
        L_0x01e4:
            return
        L_0x01e5:
            java.lang.String r5 = "prepareResponse: mRegMan or mImsRegInfo is null"
            com.sec.internal.log.IMSLog.i(r4, r12, r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityUpdate.prepareResponse(android.content.Context, java.util.List, long, java.lang.String, int, java.lang.String, java.util.Map, int, java.lang.String):void");
    }

    private boolean isServiceRegistered(String service, Map<Integer, ImsRegistration> mImsRegInfoList) {
        if (!mImsRegInfoList.containsKey(Integer.valueOf(this.mCapabilityDiscovery.getDefaultPhoneId())) || service == null) {
            return false;
        }
        ImsRegistration imsRegistration = mImsRegInfoList.get(Integer.valueOf(this.mCapabilityDiscovery.getDefaultPhoneId()));
        Log.i(LOG_TAG, "isServiceRegistered: " + service + " : " + imsRegistration.getServices());
        return imsRegistration.hasService(service);
    }

    private int getLastSeen(int phoneId) {
        long time;
        long storedLastSeen = this.mCapabilityDiscovery.getUserLastActive(phoneId);
        if (storedLastSeen <= 0) {
            time = storedLastSeen;
        } else {
            long time2 = System.currentTimeMillis();
            Log.i(LOG_TAG, "last active timestamp " + new Date(storedLastSeen).toString() + "Current Time Stamp " + new Date(time2).toString());
            long lastSeen = (long) ((int) ((time2 - storedLastSeen) / 60000));
            long lastSeen2 = MAX_LAST_SEEN;
            if (lastSeen < MAX_LAST_SEEN) {
                lastSeen2 = lastSeen;
            }
            time = lastSeen2;
        }
        Log.i(LOG_TAG, " last seen value " + time);
        return (int) time;
    }

    /* access modifiers changed from: package-private */
    public void onRetrySyncContact(int mRetrySyncContactCount) {
        Log.i(LOG_TAG, "onRetrySyncContact");
        IMSLog.c(LogClass.CDM_SYNC_CONT_RETRY, "N," + mRetrySyncContactCount);
        this.mCapabilityDiscovery.removeMessages(13);
        if (mRetrySyncContactCount == 10) {
            Log.i(LOG_TAG, "onRetrySyncContact: max retry count exceed");
        } else if (this.mCapabilityDiscovery.getPhonebook().getContactProviderStatus() >= 0) {
            this.mCapabilityDiscovery.setRetrySyncContactCount(0);
            this.mCapabilityDiscovery.syncContact();
        } else {
            int mRetrySyncContactCount2 = mRetrySyncContactCount + 1;
            this.mCapabilityDiscovery.setRetrySyncContactCount(mRetrySyncContactCount2);
            Log.i(LOG_TAG, "onRetrySyncContact: contactProvider is not yet ready, retrycount = " + mRetrySyncContactCount2);
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(13), 30000);
        }
    }

    /* access modifiers changed from: package-private */
    public void onContactChanged(boolean initial, int phoneId, boolean isOfflineAddedContact, long mLastListSubscribeStamp) {
        if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            Log.i(LOG_TAG, "onContactChanged: mUriGenerator is null");
            return;
        }
        IMSLog.i(LOG_TAG, phoneId, "onContactChanged: initial = " + initial);
        processContactChanged(initial, phoneId, isOfflineAddedContact, mLastListSubscribeStamp);
    }

    /* access modifiers changed from: package-private */
    public boolean setLegacyLatching(Context mContext, ImsUri uri, boolean isLatching, int phoneId) {
        StringBuilder sb = new StringBuilder();
        sb.append("setLegacyLatching: ");
        sb.append(uri != null ? uri.toStringLimit() : null);
        sb.append(" isLatching = ");
        sb.append(isLatching);
        IMSLog.i(LOG_TAG, phoneId, sb.toString());
        Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).get(uri);
        if (capex == null || capex.getLegacyLatching() == isLatching) {
            return false;
        }
        IMSLog.d(LOG_TAG, phoneId, "setLegacyLatching: Latching is changed to " + isLatching);
        capex.setLegacyLatching(isLatching);
        capex.setExpCapInfoExpiry(CapabilityUtil.exponentialCapInfoExpiry[0]);
        this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).persistCachedUri(uri);
        List<ImsUri> uris = new ArrayList<>();
        uris.add(uri);
        this.mCapabilityDiscovery.notifyCapabilitiesChanged(uris, capex, phoneId);
        this.mCapabilityUtil.sendRCSLInfoToHQM(mContext, isLatching, phoneId);
        IMSLog.c(LogClass.CDM_SET_LATCHING, phoneId + "," + isLatching);
        return true;
    }
}
