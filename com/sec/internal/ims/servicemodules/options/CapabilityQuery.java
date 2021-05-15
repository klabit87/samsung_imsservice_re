package com.sec.internal.ims.servicemodules.options;

import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CapabilityQuery {
    private static final String LOG_TAG = "CapabilityQuery";
    private static final long SHORT_NUMBER_DELAY = 2000;
    private static final int SHORT_NUMBER_LENGTH = 8;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityExchange mCapabilityExchange;
    private CapabilityUtil mCapabilityUtil;

    CapabilityQuery(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, CapabilityExchange capabilityExchange) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mCapabilityExchange = capabilityExchange;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(int id, int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "getCapabilities: Capex list id " + id);
        if (!this.mCapabilityUtil.checkModuleReady(phoneId)) {
            return null;
        }
        Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).get(id);
        if (capex == null || !capex.isExpired((long) this.mCapabilityUtil.getCapInfoExpiry(capex, phoneId))) {
            IMSLog.i(LOG_TAG, phoneId, "getCapabilities: No need to refresh. capex [" + ((String) Optional.ofNullable(capex).map($$Lambda$ucXTz5hqdB7L09u5aavOB3EqZFc.INSTANCE).orElse("null")) + "]");
        } else {
            IMSLog.i(LOG_TAG, phoneId, "getCapabilities: " + capex.getUri().toStringLimit() + " is expired. refresh it.");
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, 0, phoneId, capex.getUri()));
        }
        return capex;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(String number, CapabilityRefreshType refreshType, boolean lazyQuery, int phoneId, String rcsProfile) {
        String str = number;
        CapabilityRefreshType capabilityRefreshType = refreshType;
        boolean z = lazyQuery;
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "getCapabilities: refreshType " + capabilityRefreshType + ", lazyQuery: " + z);
        StringBuilder sb = new StringBuilder();
        sb.append("getCapabilities: number ");
        sb.append(str);
        IMSLog.s(LOG_TAG, i, sb.toString());
        this.mCapabilityDiscovery.removeMessages(8);
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            return null;
        }
        if (!RcsPolicyManager.getRcsStrategy(phoneId).checkCapDiscoveryOption()) {
            Capabilities dummyCapex = new Capabilities();
            dummyCapex.addFeature((long) (Capabilities.FEATURE_MMTEL_VIDEO | Capabilities.FEATURE_PRESENCE_DISCOVERY));
            dummyCapex.setAvailiable(true);
            return dummyCapex;
        }
        String number2 = RcsPolicyManager.getRcsStrategy(phoneId).checkNeedParsing(str);
        ImsUri uri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(number2, true);
        if (uri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return null;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(uri, i)) {
            return null;
        } else {
            if (capabilityRefreshType != CapabilityRefreshType.DISABLED) {
                IMSLog.c(LogClass.CDM_GET_CAPA, i + ",GETCAPA," + refreshType.ordinal() + "," + z + "," + uri.toStringLimit());
            }
            if (ImsProfile.isRcsUpProfile(rcsProfile) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
                return copyToOwnCapabilities(uri, number2);
            }
            Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(uri);
            needCapabilityRefresh(capex, refreshType, uri, -1, number2.length() <= 8, lazyQuery, phoneId);
            return capex;
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(String number, long features, int phoneId, String rcsProfile) {
        String str = number;
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "getCapabilities: feature " + Capabilities.dumpFeature(features));
        IMSLog.s(LOG_TAG, i, "getCapabilities: number " + number);
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            return null;
        }
        ImsUri uri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(number, true);
        if (uri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return null;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(uri, i)) {
            return null;
        } else {
            if (ImsProfile.isRcsUpProfile(rcsProfile) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
                return copyToOwnCapabilities(uri, number);
            }
            Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(uri);
            needCapabilityRefresh(capex, CapabilityRefreshType.ONLY_IF_NOT_FRESH, uri, features, false, false, phoneId);
            return capex;
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(ImsUri uri, long features, int phoneId, String rcsProfile) {
        ImsUri uri2;
        ImsUri uri3 = uri;
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "getCapabilities: feature " + Capabilities.dumpFeature(features));
        IMSLog.s(LOG_TAG, i, "getCapabilities: uri " + uri);
        if (!this.mCapabilityUtil.checkModuleReady(i) || uri3 == null) {
            Log.i(LOG_TAG, "getCapabilities: failed");
            return null;
        }
        if (uri.getUriType() == ImsUri.UriType.SIP_URI) {
            uri2 = this.mCapabilityDiscovery.getUriGenerator().normalize(uri);
        } else {
            uri2 = uri3;
        }
        if (this.mCapabilityUtil.blockOptionsToOwnUri(uri2, i)) {
            return null;
        }
        if (ImsProfile.isRcsUpProfile(rcsProfile) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
            return copyToOwnCapabilities(uri2, uri2.getMsisdn());
        }
        Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(uri2);
        needCapabilityRefresh(capex, CapabilityRefreshType.ONLY_IF_NOT_FRESH, uri2, features, false, false, phoneId);
        return capex;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getCapabilities(List<ImsUri> uris, CapabilityRefreshType refreshType, long features, int phoneId, String rcsProfile) {
        int i;
        List<Capabilities> caplist;
        IMnoStrategy strategy;
        List<ImsUri> urilist;
        ImsUri uri;
        int i2 = phoneId;
        IMSLog.i(LOG_TAG, i2, "getCapabilities: refreshType " + refreshType + ", feature " + Capabilities.dumpFeature(features));
        if (uris == null) {
            Log.i(LOG_TAG, "getCapabilities: uris is null.");
            return null;
        }
        IMSLog.s(LOG_TAG, i2, "getCapabilities: uris " + uris.toString());
        if (!this.mCapabilityUtil.checkModuleReady(i2)) {
            return null;
        }
        List<ImsUri> urilist2 = new ArrayList<>();
        List<Capabilities> caplist2 = new ArrayList<>();
        CapabilityConfig capaConfig = this.mCapabilityDiscovery.getCapabilityConfig(i2);
        if (!ImsProfile.isRcsUpProfile(rcsProfile) || capaConfig.getDefaultDisc() != 2) {
            IMnoStrategy strategy2 = RcsPolicyManager.getRcsStrategy(phoneId);
            for (ImsUri uri2 : uris) {
                if (uri2 == null) {
                    ImsUri imsUri = uri2;
                    IMnoStrategy iMnoStrategy = strategy2;
                    List<ImsUri> list = urilist2;
                    List<Capabilities> list2 = caplist2;
                    CapabilityRefreshType capabilityRefreshType = refreshType;
                } else if (!this.mCapabilityUtil.blockOptionsToOwnUri(uri2, i2)) {
                    Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i2).get(uri2);
                    if (capex != null) {
                        caplist2.add(capex);
                    }
                    if (capex == null || !capex.isFeatureAvailable(features)) {
                        uri = uri2;
                        strategy = strategy2;
                        urilist = urilist2;
                        caplist = caplist2;
                    } else {
                        IMnoStrategy strategy3 = strategy2;
                        Capabilities capex2 = capex;
                        uri = uri2;
                        strategy = strategy3;
                        urilist = urilist2;
                        caplist = caplist2;
                        if (!strategy3.needRefresh(capex, refreshType, (long) this.mCapabilityUtil.getCapInfoExpiry(capex, i2), (long) capaConfig.getServiceAvailabilityInfoExpiry(), capaConfig.getCapCacheExpiry(), capaConfig.getMsgcapvalidity())) {
                            IMSLog.i(LOG_TAG, i2, "getCapabilities: No need to refresh. " + capex2.toString());
                            urilist2 = urilist;
                            strategy2 = strategy;
                            caplist2 = caplist;
                            CapabilityRefreshType capabilityRefreshType2 = refreshType;
                        }
                    }
                    IMSLog.i(LOG_TAG, i2, "getCapabilities: " + uri.toStringLimit() + " is expired. refresh it");
                    if (strategy.boolSetting(RcsPolicySettings.RcsPolicy.ALLOW_LIST_CAPEX)) {
                        urilist.add(uri);
                    } else {
                        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, refreshType.ordinal(), i2, uri));
                    }
                    urilist2 = urilist;
                    strategy2 = strategy;
                    caplist2 = caplist;
                    CapabilityRefreshType capabilityRefreshType22 = refreshType;
                }
            }
            IMnoStrategy strategy4 = strategy2;
            List<ImsUri> urilist3 = urilist2;
            List<Capabilities> caplist3 = caplist2;
            if (urilist3.size() > 1) {
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                i = 0;
                capabilityDiscoveryModule2.sendMessage(capabilityDiscoveryModule2.obtainMessage(33, i2, 0, urilist3));
            } else {
                i = 0;
                if (urilist3.size() == 1) {
                    CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(6, refreshType.ordinal(), i2, urilist3.get(0)));
                }
            }
            if (caplist3.size() != 0) {
                if (!strategy4.boolSetting(RcsPolicySettings.RcsPolicy.CAPA_SKIP_NOTIFY_FORCE_REFRESH_SYNC)) {
                    CapabilityRefreshType capabilityRefreshType3 = refreshType;
                } else if (refreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
                    ArrayList arrayList = caplist3;
                    return null;
                }
                return (Capabilities[]) caplist3.toArray(new Capabilities[i]);
            }
            CapabilityRefreshType capabilityRefreshType4 = refreshType;
            List<Capabilities> list3 = caplist3;
            return null;
        }
        for (ImsUri uri3 : uris) {
            caplist2.add(copyToOwnCapabilities(uri3, uri3.getMsisdn()));
        }
        return (Capabilities[]) caplist2.toArray(new Capabilities[0]);
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(ImsUri uri, CapabilityRefreshType refreshType, int phoneId, String rcsProfile) {
        ImsUri uri2;
        ImsUri uri3 = uri;
        int i = phoneId;
        IMSLog.i(LOG_TAG, i, "getCapabilities: refreshType " + refreshType);
        IMSLog.s(LOG_TAG, i, "getCapabilities: uri " + uri);
        if (!this.mCapabilityUtil.checkModuleReady(i) || uri3 == null) {
            Log.i(LOG_TAG, "getCapabilities: failed");
            return null;
        }
        if (uri.getUriType() == ImsUri.UriType.SIP_URI) {
            uri2 = this.mCapabilityDiscovery.getUriGenerator().normalize(uri);
        } else {
            uri2 = uri3;
        }
        if (uri2 == null || this.mCapabilityUtil.blockOptionsToOwnUri(uri2, i)) {
            return null;
        }
        if (ImsProfile.isRcsUpProfile(rcsProfile) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
            return copyToOwnCapabilities(uri2, uri2.getMsisdn());
        }
        Capabilities capex = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(uri2);
        needCapabilityRefresh(capex, refreshType, uri2, -1, false, false, phoneId);
        return capex;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getCapabilitiesByContactId(String contactId, CapabilityRefreshType refreshType, int phoneId, String rcsProfile) {
        IMSLog.i(LOG_TAG, phoneId, "getCapabilitiesByContactId: contactId " + contactId + ", refreshType " + refreshType);
        if (!this.mCapabilityUtil.checkModuleReady(phoneId)) {
            return null;
        }
        if ("FORCE_CAPA_POLLING".equals(contactId)) {
            this.mCapabilityExchange.forcePoll(true, phoneId);
            return null;
        }
        ArrayList arrayList = new ArrayList();
        List<String> normalizedNumberList = this.mCapabilityDiscovery.getPhonebook().getNumberlistByContactId(contactId);
        if (normalizedNumberList != null) {
            for (String nornumber : normalizedNumberList) {
                ImsUri teluri = ImsUri.parse("tel:" + nornumber);
                IMSLog.s(LOG_TAG, phoneId, "getCapabilitiesByContactId: contactId " + contactId + ", teluri " + teluri);
                arrayList.add(teluri);
            }
        }
        return getCapabilities((List<ImsUri>) arrayList, refreshType, (long) Capabilities.FEATURE_OFFLINE_RCS_USER, phoneId, rcsProfile);
    }

    private Capabilities copyToOwnCapabilities(ImsUri uri, String number) {
        IMSLog.s(LOG_TAG, "copyToOwnCapabilities: CAPABILITY DISCOVERY MECHANISM is off. Copy to OwnCapabilities");
        Capabilities own = this.mCapabilityDiscovery.getOwnCapabilities();
        if (own != null) {
            long ownFeatures = own.getFeature();
            own.setUri(uri);
            own.setAvailableFeatures(ownFeatures);
            own.setNumber(number);
        }
        return own;
    }

    private void needCapabilityRefresh(Capabilities capex, CapabilityRefreshType refreshType, ImsUri uri, long features, boolean isShortNumber, boolean lazyQuery, int phoneId) {
        Capabilities capabilities;
        CapabilityRefreshType capabilityRefreshType = refreshType;
        ImsUri imsUri = uri;
        int i = phoneId;
        long j = features;
        if (RcsPolicyManager.getRcsStrategy(phoneId).needRefresh(capex, refreshType, (long) this.mCapabilityUtil.getCapInfoExpiry(capex, i), (long) this.mCapabilityDiscovery.getCapabilityConfig(i).getServiceAvailabilityInfoExpiry(), this.mCapabilityDiscovery.getCapabilityConfig(i).getCapCacheExpiry(), this.mCapabilityDiscovery.getCapabilityConfig(i).getMsgcapvalidity())) {
            IMSLog.i(LOG_TAG, i, "needCapabilityRefresh: true, missing capabilities for " + uri.toStringLimit());
            if (!lazyQuery) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, refreshType.ordinal(), i, imsUri));
                Capabilities capabilities2 = capex;
                CapabilityRefreshType capabilityRefreshType2 = refreshType;
                long j2 = j;
            } else if (isShortNumber) {
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(8, refreshType.ordinal(), i, imsUri), 2000);
                Capabilities capabilities3 = capex;
                CapabilityRefreshType capabilityRefreshType3 = refreshType;
                long j3 = j;
            } else {
                CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(8, refreshType.ordinal(), i, imsUri));
                Capabilities capabilities4 = capex;
                CapabilityRefreshType capabilityRefreshType4 = refreshType;
                long j4 = j;
            }
        } else {
            if (j >= 0) {
                capabilities = capex;
                long j5 = j;
                if (capabilities == null || !capex.isAvailable() || !capabilities.isFeatureAvailable(j5)) {
                    IMSLog.i(LOG_TAG, i, "needCapabilityRefresh: true, missing features for " + uri.toStringLimit());
                    CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(6, refreshType.ordinal(), i, imsUri));
                    CapabilityRefreshType capabilityRefreshType5 = refreshType;
                    return;
                }
            } else {
                capabilities = capex;
                long j6 = j;
            }
            if (capabilities != null) {
                IMSLog.i(LOG_TAG, i, "needCapabilityRefresh: false, capex is " + capex.toString());
                if (refreshType != CapabilityRefreshType.DISABLED) {
                    IMSLog.c(LogClass.CDM_NO_REFRESH, i + ",NOREF," + capex.getFeature() + "," + capex.getAvailableFeatures());
                    return;
                }
                return;
            }
            CapabilityRefreshType capabilityRefreshType6 = refreshType;
            IMSLog.i(LOG_TAG, i, "needCapabilityRefresh: false, capex is null for " + uri.toStringLimit());
            if (capabilityRefreshType6 != CapabilityRefreshType.DISABLED) {
                IMSLog.c(LogClass.CDM_NO_REFRESH, i + ",NOREF,NOCAP");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getOwnCapabilitiesBase(int phoneId, Capabilities ownCap) {
        IMSLog.i(LOG_TAG, phoneId, "getOwnCapabilitiesBase:");
        Capabilities own = null;
        if (!this.mCapabilityUtil.checkModuleReady(phoneId)) {
            try {
                own = ownCap.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (own != null) {
                IMSLog.i(LOG_TAG, phoneId, "getOwnCapabilitiesBase: module is not ready, " + Capabilities.dumpFeature(own.getFeature()));
            }
            return own;
        }
        this.mCapabilityDiscovery.updateOwnCapabilities(phoneId);
        try {
            own = ownCap.clone();
        } catch (CloneNotSupportedException e2) {
            e2.printStackTrace();
        }
        if (own != null) {
            IMSLog.i(LOG_TAG, phoneId, "getOwnCapabilitiesBase: " + Capabilities.dumpFeature(own.getFeature()));
        }
        return own;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getOwnCapabilities(int phoneId, int mAvailablePhoneId, Map<Integer, ImsRegistration> mImsRegInfoList, IRegistrationManager mRegMan, int mNetworkType, boolean mIsInCall, String mCallNumber, Capabilities ownCap) {
        if (!this.mCapabilityUtil.checkModuleReady(phoneId)) {
            return null;
        }
        if (RcsUtils.DualRcs.isDualRcsReg() || mAvailablePhoneId == phoneId) {
            this.mCapabilityDiscovery.updateOwnCapabilities(phoneId);
            Capabilities own = new Capabilities();
            try {
                own = ownCap.clone();
                Set<String> filteredServices = this.mCapabilityUtil.filterServicesWithReg(mImsRegInfoList, mRegMan, mNetworkType, phoneId);
                if (filteredServices != null) {
                    long features = this.mCapabilityUtil.filterFeaturesWithCallState(this.mCapabilityUtil.filterFeaturesWithService(own.getFeature(), filteredServices, mNetworkType), mIsInCall, mCallNumber);
                    own.setFeatures(features);
                    own.setAvailableFeatures(features);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            IMSLog.i(LOG_TAG, phoneId, "getOwnCapabilities: feature=" + Long.toHexString(own.getFeature()) + ", detail=" + Capabilities.dumpFeature(own.getFeature()));
            return own;
        }
        IMSLog.s(LOG_TAG, phoneId, "getOwnCapabilities: mAvailablePhoneId = ! phoneId");
        return null;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getAllCapabilities(int phoneId) {
        IMSLog.s(LOG_TAG, phoneId, "getAllCapabilities:");
        if (this.mCapabilityDiscovery.isRunning()) {
            return (Capabilities[]) this.mCapabilityDiscovery.getCapabilitiesCache(phoneId).getRcsUserCapabilities().toArray(new Capabilities[0]);
        }
        Log.i(LOG_TAG, "getAllCapabilities: CapabilityDiscoveryModule is disabled");
        return null;
    }
}
