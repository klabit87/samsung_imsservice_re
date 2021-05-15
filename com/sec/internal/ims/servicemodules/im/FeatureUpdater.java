package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;

public class FeatureUpdater {
    private static final String LOG_TAG = FeatureUpdater.class.getSimpleName();
    private long features;
    private final Context mContext;
    private ImConfig mImConfig;
    private final ImModule mImModule;
    private IMnoStrategy mMnoStrategy;
    private int mPhoneId;

    public FeatureUpdater(Context context, ImModule imModule) {
        this.mContext = context;
        this.mImModule = imModule;
    }

    public long updateFeatures(int phoneId, ImConfig imConfig) {
        if (!(DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, phoneId) == 1) || imConfig == null) {
            Log.i(LOG_TAG, "RCS is disabled.");
            ImsUtil.listToDumpFormat(LogClass.IM_SWITCH_OFF, phoneId, MessageContextValues.none);
            return 0;
        }
        this.features = 0;
        this.mPhoneId = phoneId;
        this.mImConfig = imConfig;
        this.mMnoStrategy = this.mImModule.getRcsStrategy(phoneId);
        updateImFeatures();
        updateFtFeatures();
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "slm", phoneId) == 1 && imConfig.getSlmAuth() != ImConstants.SlmAuth.DISABLED) {
            this.features |= (long) Capabilities.FEATURE_STANDALONE_MSG;
        }
        updateGlsFeatures();
        updateChatBotFeatures();
        IMnoStrategy iMnoStrategy = this.mMnoStrategy;
        if (iMnoStrategy != null && iMnoStrategy.isCustomizedFeature((long) Capabilities.FEATURE_FT_VIA_SMS)) {
            this.features |= (long) Capabilities.FEATURE_FT_VIA_SMS;
        }
        IMnoStrategy iMnoStrategy2 = this.mMnoStrategy;
        if (iMnoStrategy2 != null && iMnoStrategy2.isCustomizedFeature(Capabilities.FEATURE_PUBLIC_MSG)) {
            this.features |= Capabilities.FEATURE_PUBLIC_MSG;
        }
        Log.i(LOG_TAG, "updateFeatures: " + Capabilities.dumpFeature(this.features));
        return this.features;
    }

    private boolean isGlsEnabled(int phoneId) {
        Boolean isEnableGls = false;
        if (RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, phoneId)).booleanValue()) {
            isEnableGls = true;
        }
        String str = LOG_TAG;
        Log.i(str, "isEnableGls: " + isEnableGls);
        return isEnableGls.booleanValue();
    }

    private void updateImFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "im", this.mPhoneId) != 1) {
            return;
        }
        if (this.mImConfig.getChatEnabled() || this.mImConfig.getGroupChatEnabled()) {
            if (this.mImConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                this.features |= (long) Capabilities.FEATURE_CHAT_SIMPLE_IM;
            } else {
                this.features |= (long) Capabilities.FEATURE_CHAT_CPM;
            }
            if (this.mImConfig.isGroupChatFullStandFwd() || this.mImConfig.isFullSFGroupChat()) {
                this.features |= (long) Capabilities.FEATURE_SF_GROUP_CHAT;
            }
            if (this.mImConfig.isJoynIntegratedMessaging() && this.mImModule.isDefaultMessageAppInUse()) {
                this.features |= (long) Capabilities.FEATURE_INTEGRATED_MSG;
            }
        }
    }

    private void updateFtFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "ft", this.mPhoneId) == 1 && this.mImConfig.getFtEnabled()) {
            IMnoStrategy iMnoStrategy = this.mMnoStrategy;
            if (iMnoStrategy == null || !iMnoStrategy.isFtHttpOnlySupported(false)) {
                this.features |= (long) Capabilities.FEATURE_FT;
            }
            if (this.mImConfig.isFtThumb()) {
                this.features |= (long) Capabilities.FEATURE_FT_THUMBNAIL;
            }
        }
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "ft_http", this.mPhoneId) == 1 && this.mImConfig.getFtHttpEnabled()) {
            this.features |= (long) Capabilities.FEATURE_FT_HTTP;
        }
    }

    private void updateGlsFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, "gls", this.mPhoneId) == 1 && isGlsEnabled(this.mPhoneId)) {
            if (this.mImConfig.getGlsPushEnabled()) {
                this.features |= (long) Capabilities.FEATURE_GEOLOCATION_PUSH;
                IMnoStrategy iMnoStrategy = this.mMnoStrategy;
                if (iMnoStrategy != null && iMnoStrategy.isCustomizedFeature((long) Capabilities.FEATURE_GEO_VIA_SMS)) {
                    this.features |= (long) Capabilities.FEATURE_GEO_VIA_SMS;
                }
            }
            if (this.mImConfig.getGlsPullEnabled()) {
                this.features |= (long) Capabilities.FEATURE_GEOLOCATION_PULL;
            }
        }
    }

    private void updateChatBotFeatures() {
        if (DmConfigHelper.getImsSwitchValue(this.mContext, ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, this.mPhoneId) == 1) {
            if (this.mImConfig.getChatEnabled() && (this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM || this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.SESSION_ONLY)) {
                this.features |= Capabilities.FEATURE_CHATBOT_CHAT_SESSION;
            }
            if (this.mImConfig.getSlmAuth() == ImConstants.SlmAuth.DISABLED) {
                return;
            }
            if (this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM || this.mImConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.SLM_ONLY) {
                this.features |= Capabilities.FEATURE_CHATBOT_STANDALONE_MSG;
            }
        }
    }
}
