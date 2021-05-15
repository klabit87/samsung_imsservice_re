package com.sec.internal.ims.servicemodules.presence;

import android.content.Context;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;

public class PresenceConfig {
    private static final String LOG_TAG = "PresenceConfig";
    private static final int MAX_NUMBER_LIMIT = 150;
    private long mBadEventExpiry;
    private Context mContext;
    private int mDefaultDisc = 0;
    private boolean mIsLocalConfigUsed;
    private boolean mIsSocialPresenceSupport;
    private int mMaxUri = 100;
    private int mPhoneId = 0;
    private long mPublishErrRetry;
    private long mPublishTimer = 1200;
    private long mPublishTimerExtended = 86400;
    private String mRcsProfile = "";
    private long mRetryPublishTimer;
    private ImsUri mRlsUri;
    private long mSourceThrottlePublish;
    private long mSourceThrottleSubscribe;
    private long mTdelayPublish;
    private boolean mUseAnonymousFetch;
    private boolean mUseSipUri;

    public static class Builder {
        long badEventExpiry = 259200;
        boolean isLocalConfigUsed = false;
        int maxUri = 100;
        long publishErrRetry = 21600;
        long publishTimer = 1200;
        long publishTimerExtended = 86400;
        long retryPublishTimer = 1200;
        long sourceThrottlePublish = 0;
        long sourceThrottleSubscribe = 0;
        long tDelayPublish = 5;
        boolean useAnonymousFetch = false;
    }

    PresenceConfig(Context context, int phoneId) {
        this.mContext = context;
        this.mPhoneId = phoneId;
    }

    public void load() {
        IRegistrationManager rm = ImsRegistry.getRegistrationManager();
        if (rm == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: registrationManager is null");
            return;
        }
        ImsProfile profile = rm.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.RCS);
        if (profile == null || !profile.hasService("presence")) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: profile is null");
            return;
        }
        this.mRcsProfile = ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, profile);
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(this.mContext, "root/application/*", this.mPhoneId);
        if (configData == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: configData is not found");
            return;
        }
        this.mIsSocialPresenceSupport = configData.readBool(ConfigConstants.ConfigTable.SERVICES_PRESENCE_PRFL, false).booleanValue();
        long publishTimer = (long) profile.getPublishTimer();
        this.mPublishTimer = publishTimer;
        if (publishTimer <= 0) {
            this.mPublishTimer = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_PUBLISH_TIMER, 1200L).longValue();
        }
        long longValue = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, 0L).longValue();
        this.mSourceThrottlePublish = longValue;
        this.mSourceThrottleSubscribe = longValue;
        Mno mMno = SimUtil.getSimMno(this.mPhoneId);
        if (profile.getSubscribeMaxEntry() == 0) {
            this.mMaxUri = configData.readInt(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, 100).intValue();
            if (mMno.isKor() && this.mMaxUri > 150) {
                this.mMaxUri = 150;
            }
        } else {
            this.mMaxUri = profile.getSubscribeMaxEntry();
        }
        String uri = configData.readString(ConfigConstants.ConfigTable.PRESENCE_RLS_URI, "");
        if (!TextUtils.isEmpty(uri)) {
            this.mRlsUri = ImsUri.parse(uri);
        }
        this.mUseAnonymousFetch = profile.isAnonymousFetch();
        this.mDefaultDisc = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue();
        if (profile.getBadEventExpiry() == 259200) {
            this.mBadEventExpiry = configData.readLong(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, 259200L).longValue();
        } else {
            this.mBadEventExpiry = (long) profile.getBadEventExpiry();
        }
        this.mPublishErrRetry = (long) profile.getPublishErrRetryTimer();
        Context context = ImsRegistry.getContext();
        this.mTdelayPublish = (long) DmConfigHelper.readInt(context, "t_delay", 5, this.mPhoneId).intValue();
        this.mPublishTimerExtended = (long) profile.getExtendedPublishTimer();
        this.mUseSipUri = false;
        if (mMno == Mno.VZW) {
            this.mPublishTimer = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER, 1200L, this.mPhoneId).longValue();
            this.mPublishTimerExtended = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER_EXTEND, 86400L, this.mPhoneId).longValue();
            this.mPublishErrRetry = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_ERR_RETRY_TIMER, 21600L, this.mPhoneId).longValue();
            long longValue2 = DmConfigHelper.readLong(this.mContext, ConfigConstants.ConfigPath.OMADM_SRC_THROTTLE_PUBLISH, 60L, this.mPhoneId).longValue();
            this.mSourceThrottlePublish = longValue2;
            this.mSourceThrottleSubscribe = longValue2;
            this.mMaxUri = DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_SUBSCRIBE_MAX_ENTRY, 100, this.mPhoneId).intValue();
            int i = this.mPhoneId;
            StringBuilder sb = new StringBuilder();
            sb.append("load: mSourceThrottlePublishFromDM: ");
            Mno mno = mMno;
            sb.append(this.mSourceThrottlePublish);
            sb.append(", mSourceThrottleSubscribeFromDM: ");
            sb.append(this.mSourceThrottleSubscribe);
            sb.append(", mMaxUriFromDM: ");
            sb.append(this.mMaxUri);
            IMSLog.s(LOG_TAG, i, sb.toString());
            IMnoStrategy mnoStrategy = RcsPolicyManager.getRcsStrategy(this.mPhoneId);
            boolean z = mnoStrategy != null && mnoStrategy.isLocalConfigUsed();
            this.mIsLocalConfigUsed = z;
            if (!z) {
                long sourceThrottlePublish = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, 0L).longValue();
                if (sourceThrottlePublish > 0) {
                    int i2 = this.mPhoneId;
                    IMSLog.s(LOG_TAG, i2, "load: change mSourceThrottlePublish to " + sourceThrottlePublish);
                    this.mSourceThrottlePublish = sourceThrottlePublish;
                }
                int maxUri = configData.readInt(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, 0).intValue();
                if (maxUri > 0) {
                    int i3 = this.mPhoneId;
                    IMSLog.s(LOG_TAG, i3, "load: change mMaxUri to " + maxUri);
                    this.mMaxUri = maxUri;
                }
            }
        }
        long j = this.mPublishTimer;
        this.mRetryPublishTimer = j;
        if (j == 0) {
            this.mRetryPublishTimer = (long) profile.getPublishExpiry();
        }
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "load: " + toString());
        IMSLog.c(LogClass.PM_READ_CONF, this.mPhoneId + "," + this.mPublishTimer + "," + this.mRetryPublishTimer + "," + this.mSourceThrottlePublish + "," + this.mSourceThrottleSubscribe + "," + this.mMaxUri + "," + this.mUseAnonymousFetch + "," + this.mBadEventExpiry);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.mPhoneId);
        sb2.append(",");
        sb2.append(this.mRlsUri);
        sb2.append(",");
        sb2.append(this.mPublishErrRetry);
        sb2.append(",");
        sb2.append(this.mTdelayPublish);
        sb2.append(",");
        sb2.append(this.mPublishTimerExtended);
        sb2.append(",");
        sb2.append(this.mUseSipUri);
        sb2.append(",");
        sb2.append(this.mDefaultDisc);
        IMSLog.c(LogClass.PM_READ_CONF, sb2.toString());
    }

    public boolean isSocialPresenceSupport() {
        return this.mIsSocialPresenceSupport;
    }

    public boolean useAnonymousFetch() {
        return this.mUseAnonymousFetch;
    }

    public boolean useSipUri() {
        return this.mUseSipUri;
    }

    public boolean isLocalConfigUsed() {
        return this.mIsLocalConfigUsed;
    }

    public long getSourceThrottlePublish() {
        return this.mSourceThrottlePublish;
    }

    public long getSourceThrottleSubscribe() {
        return this.mSourceThrottleSubscribe;
    }

    public long getTdelayPublish() {
        return this.mTdelayPublish;
    }

    public long getPublishTimer() {
        return this.mPublishTimer;
    }

    public long getPublishErrRetry() {
        return this.mPublishErrRetry;
    }

    public void setPublishErrRetry(long publishErrRetry) {
        this.mPublishErrRetry = publishErrRetry;
    }

    public long getPublishTimerExtended() {
        return this.mPublishTimerExtended;
    }

    public long getRetryPublishTimer() {
        return this.mRetryPublishTimer;
    }

    public long getBadEventExpiry() {
        return this.mBadEventExpiry;
    }

    public ImsUri getRlsUri() {
        return this.mRlsUri;
    }

    public int getMaxUri() {
        return this.mMaxUri;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public int getDefaultDisc() {
        return this.mDefaultDisc;
    }

    public PresenceConfig(Builder builder) {
        this.mUseAnonymousFetch = builder.useAnonymousFetch;
        this.mIsLocalConfigUsed = builder.isLocalConfigUsed;
        this.mSourceThrottlePublish = builder.sourceThrottlePublish;
        this.mSourceThrottleSubscribe = builder.sourceThrottleSubscribe;
        this.mTdelayPublish = builder.tDelayPublish;
        this.mPublishTimer = builder.publishTimer;
        this.mRetryPublishTimer = builder.retryPublishTimer;
        this.mPublishTimerExtended = builder.publishTimerExtended;
        this.mPublishErrRetry = builder.publishErrRetry;
        this.mBadEventExpiry = builder.badEventExpiry;
        this.mMaxUri = builder.maxUri;
    }

    public String toString() {
        return "PresenceConfig [mPhoneId=" + this.mPhoneId + ", mUseAnonymousFetch=" + this.mUseAnonymousFetch + ", mIsLocalConfigUsed=" + this.mIsLocalConfigUsed + ", mSourceThrottlePublish=" + this.mSourceThrottlePublish + ", mSourceThrottleSubscribe=" + this.mSourceThrottleSubscribe + ", mTdelayPublish=" + this.mTdelayPublish + ", mPublishTimer=" + this.mPublishTimer + ", mRetryPublishTimer=" + this.mRetryPublishTimer + ", mPublishTimerExtended=" + this.mPublishTimerExtended + ", mPublishErrRetry=" + this.mPublishErrRetry + ", mBadEventExpiry=" + this.mBadEventExpiry + ", mMaxUri=" + this.mMaxUri + ", mDefaultDisc=" + this.mDefaultDisc + "]";
    }
}
