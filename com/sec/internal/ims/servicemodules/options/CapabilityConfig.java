package com.sec.internal.ims.servicemodules.options;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CapabilityConfig {
    private static final String LOG_TAG = "CapabilityConfig";
    private boolean isVzwCapabilitypolicy = false;
    private Set<Pattern> mAllowedPrefixes = ConcurrentHashMap.newKeySet();
    private int mCapCacheExpiry = 7776000;
    private boolean mCapDiscCommonStack = false;
    private int mCapInfoExpiry = 60;
    private Context mContext;
    private boolean mDefaultDisableInitialScan = false;
    private int mDefaultDisc = 0;
    private boolean mDisableInitialScan = false;
    private boolean mForceDisableInitialScan = false;
    protected boolean mIsAvailable = false;
    protected boolean mIsLocalConfigUsed = false;
    private boolean mIsPollingPeriodUpdated = false;
    private boolean mIsRcsUpProfile = false;
    private boolean mIsSupportExpCapInfoExpiry = false;
    protected boolean mLastSeenActive = false;
    private Mno mMno = Mno.DEFAULT;
    private int mMsgcapvalidity = 30;
    private int mNonRCScapInfoExpiry = 60;
    private int mPhoneId = 0;
    private int mPollListSubExpiry = 3;
    private int mPollingPeriod = 0;
    private int mPollingRate = 10;
    private long mPollingRatePeriod = 10;
    private String mRcsProfile = "";
    private int mServiceAvailabilityInfoExpiry = 60;
    protected int mServiceType = 0;

    public static class Builder {
        int capCacheExpiry = 0;
        int capInfoExpiry = 0;
        int defaultDisc = 0;
        boolean isAvailable = false;
        boolean isLastseenAvailable = false;
        int pollingPeriod = 120;
        int pollingRate = 10;
        long pollingRatePeriod = 10;
    }

    public CapabilityConfig(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
    }

    public void load() {
        int i;
        IRegistrationManager rm = ImsRegistry.getRegistrationManager();
        if (rm == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: registrationManager is null");
            return;
        }
        this.mServiceType = 0;
        ImsProfile profile = rm.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.RCS);
        boolean z = true;
        if (profile != null) {
            if (profile.hasService("presence")) {
                this.mServiceType = 2;
            } else if (profile.hasService("options")) {
                this.mServiceType = 1;
            }
        }
        if (this.mServiceType == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: mServiceType is zero");
            return;
        }
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(this.mContext, "root/*", this.mPhoneId);
        if (configData == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: configData is not found");
            return;
        }
        String rcsProfileWithFeature = ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, profile);
        this.mRcsProfile = rcsProfileWithFeature;
        this.mIsRcsUpProfile = ImsProfile.isRcsUpProfile(rcsProfileWithFeature);
        IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(this.mPhoneId);
        this.mIsLocalConfigUsed = mnoStrategy != null && mnoStrategy.isLocalConfigUsed();
        this.mMno = SimUtil.getSimMno(this.mPhoneId);
        int version = configData.readInt("version", 0).intValue();
        this.mDefaultDisc = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue();
        notifyDefaultDiscChange();
        this.mIsAvailable = version > 0 && (this.mDefaultDisc != 2 || this.mIsRcsUpProfile);
        this.mCapDiscCommonStack = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_CAP_DISC_COMMON_STACK, false).booleanValue();
        boolean z2 = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_DEFAULT_DISABLE_INITIAL_SCAN, false);
        this.mDefaultDisableInitialScan = z2;
        this.mDisableInitialScan = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN, Boolean.valueOf(z2)).booleanValue();
        boolean z3 = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_FORCE_DISABLE_INITIAL_SCAN, false);
        this.mForceDisableInitialScan = z3;
        if (!z3) {
            z3 = this.mDisableInitialScan;
        }
        this.mDisableInitialScan = z3;
        if (this.mMno == Mno.TELSTRA) {
            this.mPollingRate = 5;
        } else {
            this.mPollingRate = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE, 10).intValue();
        }
        this.mPollingRatePeriod = (long) configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE_PERIOD, 10).intValue();
        StringBuilder logbuffer = new StringBuilder();
        if (this.mPollingRate == 0 && this.mPollingRatePeriod == 0) {
            this.mPollingRate = 10;
            this.mPollingRatePeriod = 3;
            logbuffer.append("load: change mPollingRate to ");
            logbuffer.append(this.mPollingRate);
            logbuffer.append(" and change mPollingRatePeriod to ");
            logbuffer.append(this.mPollingRatePeriod);
        }
        String rcsAs = ConfigUtil.getAcsServerType(this.mContext, this.mPhoneId);
        if (this.mMno != Mno.VZW || ImsConstants.RCS_AS.JIBE.equals(rcsAs)) {
            z = false;
        }
        this.isVzwCapabilitypolicy = z;
        int defaultCapPollInterval = !z ? 0 : 625000;
        updatePollingPeriod(defaultCapPollInterval, profile.getCapPollInterval(), configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD, Integer.valueOf(defaultCapPollInterval)).intValue());
        int defaultCapInfoExpiry = !this.mIsRcsUpProfile ? 60 : 2592000;
        if (this.isVzwCapabilitypolicy && !this.mIsLocalConfigUsed) {
            defaultCapInfoExpiry = 604800;
        }
        updateCapInfoExpiry(defaultCapInfoExpiry, profile.getAvailCacheExpiry(), configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY, Integer.valueOf(defaultCapInfoExpiry)).intValue());
        this.mNonRCScapInfoExpiry = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, Integer.valueOf(this.mCapInfoExpiry)).intValue();
        List<String> allowedPrefixes = configData.readListString(ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES);
        updateCapDiscoveryAllowedPrefixes(allowedPrefixes);
        if (!this.mIsRcsUpProfile) {
            this.mServiceAvailabilityInfoExpiry = 60;
        } else {
            this.mServiceAvailabilityInfoExpiry = configData.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, 60).intValue();
        }
        if (!this.mIsRcsUpProfile || (this.isVzwCapabilitypolicy && this.mIsLocalConfigUsed)) {
            IRegistrationManager iRegistrationManager = rm;
            i = 30;
            this.mMsgcapvalidity = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, 30).intValue();
        } else {
            this.mMsgcapvalidity = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, Integer.valueOf(this.mServiceAvailabilityInfoExpiry)).intValue();
            IRegistrationManager iRegistrationManager2 = rm;
            i = 30;
        }
        this.mLastSeenActive = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, false).booleanValue();
        if (!this.isVzwCapabilitypolicy) {
            i = 0;
        }
        int defaultPollListSubExpiry = i;
        int i2 = defaultPollListSubExpiry;
        List<String> list = allowedPrefixes;
        this.mPollListSubExpiry = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP, Integer.valueOf(defaultPollListSubExpiry), this.mPhoneId).intValue();
        if (mnoStrategy != null) {
            this.mCapCacheExpiry = !this.isVzwCapabilitypolicy ? this.mCapInfoExpiry : 7776000;
            if (mnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_CAPCACHE_EXPIRY)) {
                ImsProfile imsProfile = profile;
                this.mCapCacheExpiry += DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP, 0, this.mPhoneId).intValue();
            } else {
                this.mCapCacheExpiry = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP, 0, this.mPhoneId).intValue();
            }
            mnoStrategy.updateCapDiscoveryOption();
        }
        this.mIsSupportExpCapInfoExpiry = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_SUPPORT_EXPONENTIAL_CAPINFOEXPIRY, false);
        logbuffer.append(" load: mServiceType: ");
        logbuffer.append(this.mServiceType);
        logbuffer.append(" mRcsProfile: ");
        logbuffer.append(this.mRcsProfile);
        logbuffer.append(" mIsRcsUpProfile: ");
        logbuffer.append(this.mIsRcsUpProfile);
        logbuffer.append(" mIsLocalConfigUsed: ");
        logbuffer.append(this.mIsLocalConfigUsed);
        logbuffer.append(" rcsVersion: ");
        logbuffer.append(version);
        logbuffer.append(" mDefaultDisc: ");
        logbuffer.append(this.mDefaultDisc);
        logbuffer.append(" mIsAvailable: ");
        logbuffer.append(this.mIsAvailable);
        logbuffer.append(" mCapDiscCommonStack: ");
        logbuffer.append(this.mCapDiscCommonStack);
        logbuffer.append(" mDisableInitialScan: ");
        logbuffer.append(this.mDisableInitialScan);
        logbuffer.append(" mDefaultDisableInitialScan: ");
        logbuffer.append(this.mDefaultDisableInitialScan);
        logbuffer.append(" mForceDisableInitialScan: ");
        logbuffer.append(this.mForceDisableInitialScan);
        logbuffer.append(" mPollingRate: ");
        logbuffer.append(this.mPollingRate);
        logbuffer.append(" mPollingRatePeriod: ");
        logbuffer.append(this.mPollingRatePeriod);
        logbuffer.append(" mNonRCScapInfoExpiry: ");
        logbuffer.append(this.mNonRCScapInfoExpiry);
        logbuffer.append(" mMsgcapvalidity: ");
        logbuffer.append(this.mMsgcapvalidity);
        logbuffer.append(" mServiceAvailabilityInfoExpiry: ");
        logbuffer.append(this.mServiceAvailabilityInfoExpiry);
        logbuffer.append(" mLastSeenActive: ");
        logbuffer.append(this.mLastSeenActive);
        logbuffer.append(" mPollListSubExpiry: ");
        logbuffer.append(this.mPollListSubExpiry);
        logbuffer.append(" mCapCacheExpiry: ");
        logbuffer.append(this.mCapCacheExpiry);
        logbuffer.append(" mIsSupportExpCapInfoExpiry: ");
        logbuffer.append(this.mIsSupportExpCapInfoExpiry);
        IMSLog.i(LOG_TAG, this.mPhoneId, logbuffer.toString());
    }

    public int getCapInfoExpiry() {
        return this.mCapInfoExpiry;
    }

    public int getNonRCScapInfoExpiry() {
        return this.mNonRCScapInfoExpiry;
    }

    public long getCapCacheExpiry() {
        return (long) this.mCapCacheExpiry;
    }

    public boolean isDisableInitialScan() {
        return this.mDisableInitialScan;
    }

    public int getPollingPeriod() {
        return this.mPollingPeriod;
    }

    public int getPollListSubExpiry() {
        return this.mPollListSubExpiry;
    }

    public int getPollingRate() {
        return this.mPollingRate;
    }

    public long getPollingRatePeriod() {
        return this.mPollingRatePeriod;
    }

    public long getMsgcapvalidity() {
        return (long) this.mMsgcapvalidity;
    }

    public boolean isPollingPeriodUpdated() {
        return this.mIsPollingPeriodUpdated;
    }

    public void resetPollingPeriodUpdated() {
        this.mIsPollingPeriodUpdated = false;
    }

    public boolean isLastSeenActive() {
        return this.mLastSeenActive;
    }

    public boolean usePresence() {
        return this.mDefaultDisc == 1;
    }

    public void setUsePresence(boolean usePresence) {
        this.mDefaultDisc = usePresence;
    }

    public boolean isAvailable() {
        return this.mIsAvailable;
    }

    public Set<Pattern> getCapAllowedPrefixes() {
        return this.mAllowedPrefixes;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public int getServiceAvailabilityInfoExpiry() {
        return this.mServiceAvailabilityInfoExpiry;
    }

    public boolean getIsSupportExpCapInfoExpiry() {
        return this.mIsSupportExpCapInfoExpiry;
    }

    public int getDefaultDisc() {
        return this.mDefaultDisc;
    }

    /* access modifiers changed from: package-private */
    public int getDefaultDisc(int phoneId) {
        return RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, phoneId), 2).intValue();
    }

    private void notifyDefaultDiscChange() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.notifyChange(Uri.parse(ConfigConstants.CONTENT_URI + "root/application/1/capdiscovery/defaultdisc"), (ContentObserver) null);
        contentResolver.notifyChange(Uri.parse(ConfigConstants.CONTENT_URI + "parameter/defaultdisc"), (ContentObserver) null);
    }

    private void updatePollingPeriod(int CapPollInterval, int profileCapPollInterval, int pollingPeriodFromConfigDB) {
        StringBuilder logbuffer = new StringBuilder("updatePollingPeriod() ");
        boolean z = true;
        if (this.mIsLocalConfigUsed) {
            int capPollIntervalFromDM = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_POLL_INTERVAL, Integer.valueOf(CapPollInterval), this.mPhoneId).intValue();
            logbuffer.append(", capPollIntervalFromDM: ");
            logbuffer.append(capPollIntervalFromDM);
            if (capPollIntervalFromDM > 0) {
                if (this.mPollingPeriod == capPollIntervalFromDM) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = capPollIntervalFromDM;
            } else {
                if (this.mPollingPeriod == CapPollInterval) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = CapPollInterval;
            }
        } else if (this.mServiceType != 2 || profileCapPollInterval <= 0 || this.isVzwCapabilitypolicy) {
            logbuffer.append(", pollingPeriodFromConfigDB: ");
            logbuffer.append(pollingPeriodFromConfigDB);
            if (pollingPeriodFromConfigDB >= 0) {
                if (this.mPollingPeriod == pollingPeriodFromConfigDB) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = pollingPeriodFromConfigDB;
            } else {
                if (this.mPollingPeriod == CapPollInterval) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = CapPollInterval;
            }
        } else {
            logbuffer.append(", capPollIntervalFromProfile: ");
            logbuffer.append(profileCapPollInterval);
            if (this.mPollingPeriod == profileCapPollInterval) {
                z = false;
            }
            this.mIsPollingPeriodUpdated = z;
            this.mPollingPeriod = profileCapPollInterval;
        }
        logbuffer.append(", mPollingPeriod: ");
        logbuffer.append(this.mPollingPeriod);
        logbuffer.append(", mIsPollingPeriodUpdated: ");
        logbuffer.append(this.mIsPollingPeriodUpdated);
        IMSLog.i(LOG_TAG, this.mPhoneId, logbuffer.toString());
    }

    private void updateCapInfoExpiry(int defaultCapInfoExpiry, int profileCapInfoExpiry, int capInfoExpiryFromConfigDB) {
        StringBuilder logbuffer = new StringBuilder("updateCapInfoExpiry() ");
        if (this.mIsLocalConfigUsed) {
            int availCacheExpFromDM = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_AVAIL_CACHE_EXP, 60, this.mPhoneId).intValue();
            logbuffer.append(", availCacheExpFromDM: ");
            logbuffer.append(availCacheExpFromDM);
            if (availCacheExpFromDM > 0) {
                this.mCapInfoExpiry = availCacheExpFromDM;
            } else {
                this.mCapInfoExpiry = 60;
            }
        } else if (this.mServiceType != 2 || profileCapInfoExpiry <= 0 || this.isVzwCapabilitypolicy) {
            logbuffer.append(", capInfoExpiryFromConfigDB: ");
            logbuffer.append(capInfoExpiryFromConfigDB);
            if (capInfoExpiryFromConfigDB > 0) {
                logbuffer.append(", use capInfoExpiryFromConfigDB: ");
                this.mCapInfoExpiry = capInfoExpiryFromConfigDB;
            } else if (capInfoExpiryFromConfigDB == 0 && ConfigUtil.isRcsEur(this.mPhoneId)) {
                int capInfoExpiryFromConfigDB2 = defaultCapInfoExpiry;
                logbuffer.append(", change capInfoExpiryFromConfigDB to ");
                logbuffer.append(capInfoExpiryFromConfigDB2);
                logbuffer.append(" for eur");
                this.mCapInfoExpiry = capInfoExpiryFromConfigDB2;
            } else if (capInfoExpiryFromConfigDB != 0 || !this.mIsRcsUpProfile || this.isVzwCapabilitypolicy) {
                logbuffer.append(", use defaultCapInfoExpiry");
                this.mCapInfoExpiry = defaultCapInfoExpiry;
            } else {
                this.mCapInfoExpiry = capInfoExpiryFromConfigDB;
            }
        } else {
            logbuffer.append(", profileCapInfoExpiry: ");
            logbuffer.append(profileCapInfoExpiry);
            this.mCapInfoExpiry = profileCapInfoExpiry;
        }
        logbuffer.append(", mCapInfoExpiry: ");
        logbuffer.append(this.mCapInfoExpiry);
        IMSLog.i(LOG_TAG, this.mPhoneId, logbuffer.toString());
    }

    private void updateCapDiscoveryAllowedPrefixes(List<String> allowedPrefixes) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateCapDiscoveryAllowedPrefixes: allowedPrefixes = " + allowedPrefixes);
        for (String prefix : allowedPrefixes) {
            Pattern prefixRegex = null;
            if (prefix.startsWith("!")) {
                try {
                    prefixRegex = Pattern.compile(prefix.substring(1));
                } catch (PatternSyntaxException e) {
                    int i2 = this.mPhoneId;
                    IMSLog.e(LOG_TAG, i2, "updateCapDiscoveryAllowedPrefixes: patternSyntaxException on prefix: " + prefix.substring(1));
                }
            } else {
                prefixRegex = Pattern.compile("^(" + prefix.replaceAll("\\+", "\\\\+") + ")");
            }
            if (prefixRegex != null) {
                this.mAllowedPrefixes.add(prefixRegex);
            }
        }
    }

    public CapabilityConfig(Builder builder) {
        this.mCapInfoExpiry = builder.capInfoExpiry;
        this.mCapCacheExpiry = builder.capCacheExpiry;
        this.mPollingPeriod = builder.pollingPeriod;
        this.mPollingRate = builder.pollingRate;
        this.mPollingRatePeriod = builder.pollingRatePeriod;
        this.mDefaultDisc = builder.defaultDisc;
        this.mIsAvailable = builder.isAvailable;
        this.mLastSeenActive = builder.isLastseenAvailable;
    }

    public String toString() {
        return "CapabilityConfig [mContext=" + this.mContext + ", mPhoneId=" + this.mPhoneId + ", mCapInfoExpiry=" + this.mCapInfoExpiry + ", mNonRCScapInfoExpiry=" + this.mNonRCScapInfoExpiry + ", mPollingPeriod=" + this.mPollingPeriod + ", mCapCacheExpiry=" + this.mCapCacheExpiry + ", mPollingRate=" + this.mPollingRate + ", mPollListSubExpiry=" + this.mPollListSubExpiry + ", mPollingRatePeriod=" + this.mPollingRatePeriod + ", mServiceAvailabilityInfoExpiry=" + this.mServiceAvailabilityInfoExpiry + ", mDefaultDisc=" + this.mDefaultDisc + ", mIsLocalConfigUsed=" + this.mIsLocalConfigUsed + ", mIsPollingPeriodUpdated=" + this.mIsPollingPeriodUpdated + ", mDisableInitialScan=" + this.mDisableInitialScan + ", mDefaultDisableInitialScan=" + this.mDefaultDisableInitialScan + ", mForceDisableInitialScan=" + this.mForceDisableInitialScan + ", mAllowedPrefixes=" + this.mAllowedPrefixes + ", mIsSupportExpCapInfoExpiry=" + this.mIsSupportExpCapInfoExpiry + ", mCapDiscCommonStack=" + this.mCapDiscCommonStack + "]";
    }
}
