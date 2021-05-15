package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDefaultConst;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.RcsConfigurationHelper;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.TapiServiceUtil;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class ImConfig {
    private static final String LOG_TAG = "ImConfig";
    public static final long UNDEFINED_MAX_SIZE_FILE_TR_INCOMING = -1;
    private static Map<Integer, ImConfig> sInstances = new HashMap();
    private int m1ToManySelectedTech;
    private boolean mAutAccept;
    private boolean mAutAcceptGroupChat;
    private boolean mBotPrivacyDisable;
    private int mCallComposerTimerIdle;
    private Uri mCbftHTTPCSURI;
    private boolean mCfsTrigger;
    private boolean mChatEnabled;
    private int mChatRevokeTimer;
    private ImConstants.ChatbotMsgTechConfig mChatbotMsgTech;
    private ImsUri mConfFctyUri;
    private ImsUri mDeferredMsgFuncUri;
    private int mDisplayNotificationSwitch;
    private boolean mEnableFtAutoResumable;
    private boolean mEnableGroupChatListRetrieve;
    private ImsUri mExploderUri;
    protected long mExtAttImMSRPFtMaxSize;
    protected int mExtAttImSlmMaxRecipients;
    private boolean mFirstMsgInvite;
    private boolean mFtAutAccept;
    private boolean mFtAutAcceptOriginalConfig;
    private boolean mFtCancelMemoryFull;
    private boolean mFtCapAlwaysOn;
    private ImConstants.FtMech mFtDefaultMech;
    private boolean mFtEnabled;
    private boolean mFtFallbackAllFail;
    private int mFtFbDefault;
    private Uri mFtHTTPExtraCSURI;
    private boolean mFtHttpCapAlwaysOn;
    private String mFtHttpCsPwd;
    private Uri mFtHttpCsUri;
    private String mFtHttpCsUser;
    private String mFtHttpDLUrl;
    private boolean mFtHttpEnabled;
    private int mFtHttpFallback;
    private boolean mFtHttpTrustAllCerts;
    private int mFtMax1ToManyRecipients;
    private boolean mFtStAndFwEnabled;
    private boolean mFtThumb;
    private long mFtWarnSize;
    private boolean mGlsPullEnabled;
    private boolean mGlsPushEnabled;
    private boolean mGroupChatEnabled;
    private boolean mGroupChatFullStandFwd;
    private boolean mGroupChatOnlyFStandFwd;
    private boolean mImCapAlwaysOn;
    private boolean mImCapNonRcs;
    private ImConstants.ImMsgTech mImMsgTech;
    private ImConstants.ImSessionStart mImSessionStart;
    private boolean mImWarnIw;
    private boolean mImWarnSf;
    private boolean mIsAggrImdnSupported;
    private boolean mIsFullSFGroupChat;
    private boolean mJoynIntegratedMessaging;
    private boolean mLegacyLatching;
    private int mMax1ToManyRecipients;
    private int mMaxAdhocGroupSize;
    private int mMaxConcurrentSession;
    private long mMaxSize;
    private long mMaxSize1To1;
    private long mMaxSize1ToM;
    private long mMaxSizeExtraFileTr;
    private long mMaxSizeFileTr;
    private long mMaxSizeFileTrIncoming;
    private ImConstants.MessagingUX mMessagingUX;
    private int mMsgCapValidityTime;
    private int mMsgFbDefault;
    private boolean mMultiMediaChat;
    private int mPagerModeLimit;
    private final int mPhoneId;
    private boolean mPresSrvCap;
    private String mPublicAccountAddr;
    private String mRcsProfile = "";
    private int mReconnectGuardTimer;
    private boolean mRespondDisplay;
    private int mServiceAvailabilityInfoExpiry;
    private ImConstants.SlmAuth mSlmAuth;
    private long mSlmMaxMsgSize;
    private int mSlmSwitchOverSize;
    private boolean mSmsFallbackAuth;
    private ITelephonyManager mTelephony;
    private int mTimerIdle;
    private String mUserAgent;
    private String mUserAlias;
    private boolean mUserAliasEnabled;
    private boolean mfThttpDefaultPdn;

    private ImConfig(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public static synchronized ImConfig getInstance(int phoneId) {
        ImConfig config;
        synchronized (ImConfig.class) {
            config = sInstances.get(Integer.valueOf(phoneId));
            if (config == null) {
                config = new ImConfig(phoneId);
                sInstances.put(Integer.valueOf(phoneId), config);
            }
        }
        return config;
    }

    public void load(Context context, String rcsProfile, boolean isRoaming) {
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(context, "root/*", this.mPhoneId);
        this.mRcsProfile = rcsProfile;
        initRcsConfiguration(context);
        loadGlobalSettings(context);
        loadRcsConfiguration(context, configData);
        updateRcsConfiguration(context, configData);
        loadUserAlias(context);
        setFtAutAccept(context, ImUserPreference.getInstance().getFtAutAccept(context, this.mPhoneId), isRoaming);
        this.mRespondDisplay = ImDefaultConst.DEFAULT_CHAT_RESPOND_TO_DISPLAY_REPORTS.booleanValue();
        this.mTelephony = TelephonyManagerWrapper.getInstance(context);
    }

    private void initRcsConfiguration(Context context) {
        if (ImsProfile.isRcsUpProfile(this.mRcsProfile)) {
            this.mMaxAdhocGroupSize = 100;
            this.mServiceAvailabilityInfoExpiry = 60;
            return;
        }
        this.mMaxAdhocGroupSize = ImsRegistry.getInt(this.mPhoneId, "max_adhoc_group_size", 10);
        this.mServiceAvailabilityInfoExpiry = 0;
    }

    private void loadGlobalSettings(Context context) {
        this.mUserAgent = getSipUserAgent(context);
        this.mIsFullSFGroupChat = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FULL_SF_GROUP_CHAT, false);
        this.mIsAggrImdnSupported = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.AGGR_IMDN_SUPPORTED, false);
        this.mEnableGroupChatListRetrieve = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.ENABLE_GROUP_CHAT_LIST_RETRIEVE, false);
        this.mFtHttpTrustAllCerts = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FTHTTP_TRUST_ALL_CERTS, false);
        this.mFtCancelMemoryFull = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FT_CANCEL_MEMORY_FULL, false);
        this.mFtFallbackAllFail = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FT_FALLBACK_ALL_FAIL, false);
        this.mEnableFtAutoResumable = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.ENABLE_FT_AUTO_RESUMABLE, false);
        this.mfThttpDefaultPdn = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FTHTTP_OVER_DEFAULT_PDN, false);
        this.mPagerModeLimit = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.RCS.PAGER_MODE_SIZE_LIMIT, 0);
    }

    private void loadRcsConfiguration(Context context, RcsConfigurationHelper.ConfigData data) {
        ImConstants.MessagingUX messagingUX;
        boolean z = false;
        boolean booleanValue = data.readBool(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, false).booleanValue();
        this.mChatEnabled = booleanValue;
        this.mGroupChatEnabled = data.readBool(ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH, Boolean.valueOf(booleanValue)).booleanValue();
        this.mFtEnabled = data.readBool(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, false).booleanValue();
        this.mFtHttpCsUri = data.readUri(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, (Uri) null);
        this.mFtDefaultMech = RcsConfigurationHelper.getFtDefaultTech(data, this.mRcsProfile, this.mPhoneId);
        Uri uri = this.mFtHttpCsUri;
        this.mFtHttpEnabled = uri != null && !"".equals(uri.toString().trim()) && this.mFtDefaultMech == ImConstants.FtMech.HTTP;
        this.mSlmAuth = ImConstants.SlmAuth.values()[data.readIntWithinRange(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, 0, 0, Integer.valueOf(ImConstants.SlmAuth.values().length - 1)).intValue()];
        this.mSmsFallbackAuth = data.readBool(ConfigConstants.ConfigTable.IM_SMS_FALLBACK_AUTH, false).booleanValue();
        this.mGlsPushEnabled = data.readBool(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, false).booleanValue();
        if (data.readInt(ConfigConstants.ConfigTable.SERVICES_GEOPULL_AUTH, 0).intValue() != 0) {
            z = true;
        }
        this.mGlsPullEnabled = z;
        this.mChatbotMsgTech = ImConstants.ChatbotMsgTechConfig.values()[data.readIntWithinRange(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, 1, 0, Integer.valueOf(ImConstants.ChatbotMsgTechConfig.values().length - 1)).intValue()];
        this.mPresSrvCap = data.readBool(ConfigConstants.ConfigTable.IM_PRES_SRV_CAP, false).booleanValue();
        this.mMaxAdhocGroupSize = data.readInt("max_adhoc_group_size", Integer.valueOf(this.mMaxAdhocGroupSize)).intValue();
        this.mConfFctyUri = data.readImsUri(ConfigConstants.ConfigTable.IM_CONF_FCTY_URI, (ImsUri) null);
        this.mExploderUri = data.readImsUri(ConfigConstants.ConfigTable.IM_EXPLODER_URI, (ImsUri) null);
        this.mDeferredMsgFuncUri = data.readImsUri(ConfigConstants.ConfigTable.IM_DEFERRED_MSG_FUNC_URI, (ImsUri) null);
        this.mImCapAlwaysOn = getImCapAlwaysOn(context, data);
        this.mImWarnSf = data.readBool(ConfigConstants.ConfigTable.IM_IM_WARN_SF, false).booleanValue();
        this.mGroupChatFullStandFwd = data.readBool(ConfigConstants.ConfigTable.IM_GROUP_CHAT_FULL_STAND_FWD, false).booleanValue();
        this.mGroupChatOnlyFStandFwd = data.readBool(ConfigConstants.ConfigTable.IM_GROUP_CHAT_ONLY_F_STAND_FWD, false).booleanValue();
        this.mImCapNonRcs = data.readBool(ConfigConstants.ConfigTable.IM_IM_CAP_NON_RCS, false).booleanValue();
        this.mImWarnIw = data.readBool(ConfigConstants.ConfigTable.IM_IM_WARN_IW, false).booleanValue();
        this.mAutAccept = data.readBool(ConfigConstants.ConfigTable.IM_AUT_ACCEPT, false).booleanValue();
        this.mImSessionStart = ImConstants.ImSessionStart.values()[data.readIntWithinRange(ConfigConstants.ConfigTable.IM_IM_SESSION_START, 0, 0, Integer.valueOf(ImConstants.ImSessionStart.values().length - 1)).intValue()];
        this.mAutAcceptGroupChat = data.readBool(ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT, false).booleanValue();
        this.mFirstMsgInvite = data.readBool(ConfigConstants.ConfigTable.IM_FIRST_MSG_INVITE, false).booleanValue();
        this.mTimerIdle = data.readInt(ConfigConstants.ConfigTable.IM_TIMER_IDLE, 0).intValue();
        this.mMaxConcurrentSession = data.readInt(ConfigConstants.ConfigTable.IM_MAX_CONCURRENT_SESSION, 0).intValue();
        this.mMultiMediaChat = data.readBool(ConfigConstants.ConfigTable.IM_MULTIMEDIA_CHAT, false).booleanValue();
        this.mMaxSize1To1 = data.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_1, 0L).longValue();
        this.mMaxSize1ToM = data.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_M, 0L).longValue();
        this.mSlmMaxMsgSize = data.readLong(ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE, 0L).longValue();
        this.mImMsgTech = RcsConfigurationHelper.getImMsgTech(data, this.mRcsProfile, this.mPhoneId);
        this.mFtHttpCsUser = data.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER, (String) null);
        this.mFtHttpCsPwd = data.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD, (String) null);
        this.mMaxSizeFileTr = data.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR, 0L).longValue();
        this.mFtWarnSize = data.readLong(ConfigConstants.ConfigTable.IM_FT_WARN_SIZE, 0L).longValue();
        this.mFtThumb = data.readBool(ConfigConstants.ConfigTable.IM_FT_THUMB, false).booleanValue();
        this.mFtStAndFwEnabled = data.readBool(ConfigConstants.ConfigTable.IM_FT_ST_AND_FW_ENABLED, false).booleanValue();
        this.mFtCapAlwaysOn = data.readBool(ConfigConstants.ConfigTable.IM_FT_CAP_ALWAYS_ON, false).booleanValue();
        boolean booleanValue2 = data.readBool(ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT, false).booleanValue();
        this.mFtAutAccept = booleanValue2;
        this.mFtAutAcceptOriginalConfig = booleanValue2;
        this.mFtHttpDLUrl = data.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI, (String) null);
        this.mCallComposerTimerIdle = data.readInt(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, Integer.valueOf(MNO.EVR_ESN)).intValue();
        this.mJoynIntegratedMessaging = RcsConfigurationHelper.readBoolParamWithPath(context, ConfigConstants.ConfigPath.JOYN_UX_MESSAGING_UX).booleanValue();
        this.mMsgCapValidityTime = data.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, 30).intValue();
        this.mFtHttpCapAlwaysOn = data.readBool(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON, Boolean.valueOf(this.mImCapAlwaysOn)).booleanValue();
        this.mChatRevokeTimer = data.readInt(ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER, 0).intValue();
        long longValue = data.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR_INCOMING, -1L).longValue();
        this.mMaxSizeFileTrIncoming = longValue;
        if (longValue == -1) {
            this.mMaxSizeFileTrIncoming = this.mMaxSizeFileTr;
        }
        this.mMaxSize = data.readLong("MaxSize", 0L).longValue();
        this.mFtHttpFallback = data.readInt(ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK, 0).intValue();
        this.mPublicAccountAddr = data.readString(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDR, (String) null);
        this.mMaxSizeExtraFileTr = (long) data.readInt(ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR, 0).intValue();
        this.mFtHTTPExtraCSURI = data.readUri(ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI, (Uri) null);
        this.mCbftHTTPCSURI = data.readUri(ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI, (Uri) null);
        if (data.readInt(ConfigConstants.ConfigTable.UX_MESSAGING_UX, 0).intValue() == 0) {
            messagingUX = ImConstants.MessagingUX.SEAMLESS;
        } else {
            messagingUX = ImConstants.MessagingUX.INTEGRATED;
        }
        this.mMessagingUX = messagingUX;
        this.mUserAliasEnabled = data.readBool(ConfigConstants.ConfigTable.UX_USER_ALIAS_AUTH, true).booleanValue();
        this.mMsgFbDefault = data.readInt(ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT, 0).intValue();
        this.mReconnectGuardTimer = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER, 0).intValue();
        this.mCfsTrigger = data.readBool(ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER, false).booleanValue();
        this.mMax1ToManyRecipients = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS, 0).intValue();
        this.m1ToManySelectedTech = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH, 0).intValue();
        this.mDisplayNotificationSwitch = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH, 0).intValue();
        this.mFtMax1ToManyRecipients = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS, 0).intValue();
        this.mFtFbDefault = data.readInt(ConfigConstants.ConfigTable.UX_FT_FB_DEFAULT, 0).intValue();
        this.mServiceAvailabilityInfoExpiry = data.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, Integer.valueOf(this.mServiceAvailabilityInfoExpiry)).intValue();
        this.mBotPrivacyDisable = data.readBool(ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE, false).booleanValue();
        this.mSlmSwitchOverSize = data.readInt(ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE, 1300).intValue();
        String extAttImSlmMaxRecipients = RcsConfigurationHelper.readStringParamWithPath(context, "root/application/1/im/ext/att/slmMaxRecipients");
        if (!TextUtils.isEmpty(extAttImSlmMaxRecipients)) {
            try {
                this.mExtAttImSlmMaxRecipients = Integer.parseInt(extAttImSlmMaxRecipients);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String extAttImMSRPFtMaxSize = RcsConfigurationHelper.readStringParamWithPath(context, "root/application/1/im/ext/att/MSRPFtMaxSize");
        if (!TextUtils.isEmpty(extAttImMSRPFtMaxSize)) {
            try {
                this.mExtAttImMSRPFtMaxSize = Long.parseLong(extAttImMSRPFtMaxSize);
            } catch (NumberFormatException e2) {
                e2.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0029, code lost:
        r0 = r10.mFtHttpCsUri;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRcsConfiguration(android.content.Context r11, com.sec.internal.ims.config.RcsConfigurationHelper.ConfigData r12) {
        /*
            r10 = this;
            java.lang.String r0 = r10.mRcsProfile
            boolean r0 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r0)
            r1 = 120(0x78, float:1.68E-43)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.String r2 = "reconnectGuardTimer"
            java.lang.String r3 = "ftHTTPCapAlwaysOn"
            java.lang.String r4 = ""
            r5 = 1
            java.lang.Boolean r6 = java.lang.Boolean.valueOf(r5)
            r7 = 0
            if (r0 == 0) goto L_0x0097
            java.lang.String r0 = r10.mRcsProfile
            int r8 = r10.mPhoneId
            boolean r0 = com.sec.internal.ims.config.RcsConfigurationHelper.isUp2NonTransitional(r0, r8)
            if (r0 == 0) goto L_0x0043
            boolean r0 = r10.mFtEnabled
            if (r0 == 0) goto L_0x003d
            android.net.Uri r0 = r10.mFtHttpCsUri
            if (r0 == 0) goto L_0x003d
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = r0.trim()
            boolean r0 = r4.equals(r0)
            if (r0 != 0) goto L_0x003d
            r0 = r5
            goto L_0x003e
        L_0x003d:
            r0 = r7
        L_0x003e:
            r10.mFtHttpEnabled = r0
            r10.mFtEnabled = r7
            goto L_0x0060
        L_0x0043:
            android.net.Uri r0 = r10.mFtHttpCsUri
            if (r0 == 0) goto L_0x005d
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = r0.trim()
            boolean r0 = r4.equals(r0)
            if (r0 != 0) goto L_0x005d
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$FtMech r0 = r10.mFtDefaultMech
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$FtMech r8 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.FtMech.HTTP
            if (r0 != r8) goto L_0x005d
            r0 = r5
            goto L_0x005e
        L_0x005d:
            r0 = r7
        L_0x005e:
            r10.mFtHttpEnabled = r0
        L_0x0060:
            long r8 = r10.mMaxSize
            r10.mMaxSize1To1 = r8
            r10.mMaxSize1ToM = r8
            java.lang.Integer r0 = r12.readInt(r2, r1)
            int r0 = r0.intValue()
            r10.mReconnectGuardTimer = r0
            java.lang.String r0 = "legacy_latching"
            boolean r0 = com.sec.internal.ims.registry.ImsRegistry.getBoolean(r7, r0, r7)
            r10.mLegacyLatching = r0
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r7)
            java.lang.String r8 = "firstMessageInvite"
            java.lang.Boolean r0 = r12.readBool(r8, r0)
            boolean r0 = r0.booleanValue()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r8 = r10.mImMsgTech
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.SIMPLE_IM
            if (r8 == r9) goto L_0x0090
            if (r0 == 0) goto L_0x008f
            goto L_0x0090
        L_0x008f:
            r5 = r7
        L_0x0090:
            r10.mFirstMsgInvite = r5
            r10.mFtEnabled = r7
            r10.mFtThumb = r7
            goto L_0x00ac
        L_0x0097:
            java.lang.String r0 = r10.mRcsProfile
            java.lang.String r5 = "joyn_cpr"
            boolean r0 = r5.equals(r0)
            if (r0 == 0) goto L_0x00ac
            java.lang.Boolean r0 = r12.readBool(r3, r6)
            boolean r0 = r0.booleanValue()
            r10.mFtHttpCapAlwaysOn = r0
            goto L_0x00ad
        L_0x00ac:
        L_0x00ad:
            int r0 = r10.mPhoneId
            java.lang.String r5 = "mnoname"
            java.lang.String r0 = com.sec.internal.ims.registry.ImsRegistry.getString(r0, r5, r4)
            int r4 = r10.mPhoneId
            java.lang.String r4 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r11, r4)
            int r5 = r10.mPhoneId
            int r5 = com.sec.internal.ims.util.ConfigUtil.getAutoconfigSourceWithFeature(r11, r5, r7)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "name:"
            r7.append(r8)
            r7.append(r0)
            java.lang.String r8 = ", rcs_local_config_server:"
            r7.append(r8)
            r7.append(r5)
            java.lang.String r7 = r7.toString()
            java.lang.String r8 = "ImConfig"
            android.util.Log.i(r8, r7)
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.SPRINT
            java.lang.String r7 = r7.getName()
            boolean r7 = r7.equalsIgnoreCase(r0)
            if (r7 == 0) goto L_0x00f6
            java.lang.Integer r1 = r12.readInt(r2, r1)
            int r1 = r1.intValue()
            r10.mReconnectGuardTimer = r1
            goto L_0x015c
        L_0x00f6:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.RJIL
            java.lang.String r1 = r1.getName()
            boolean r1 = r1.equalsIgnoreCase(r0)
            if (r1 == 0) goto L_0x010d
            java.lang.Boolean r1 = r12.readBool(r3, r6)
            boolean r1 = r1.booleanValue()
            r10.mFtHttpCapAlwaysOn = r1
            goto L_0x015c
        L_0x010d:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.ATT
            java.lang.String r1 = r1.getName()
            boolean r1 = r1.equalsIgnoreCase(r0)
            if (r1 == 0) goto L_0x014a
            java.lang.String r1 = "jibe"
            boolean r1 = r1.equalsIgnoreCase(r4)
            if (r1 != 0) goto L_0x014a
            r1 = 2
            if (r5 == r1) goto L_0x015c
            r1 = 3
            if (r5 == r1) goto L_0x015c
            java.lang.String r1 = r10.mFtHttpCsUser
            java.lang.String r1 = r10.decrypt(r1)
            r10.mFtHttpCsUser = r1
            java.lang.String r1 = r10.mFtHttpCsPwd
            java.lang.String r1 = r10.decrypt(r1)
            r10.mFtHttpCsPwd = r1
            android.net.Uri r1 = r10.mFtHttpCsUri
            if (r1 == 0) goto L_0x015c
            java.lang.String r1 = r1.toString()
            java.lang.String r1 = r10.decrypt(r1)
            android.net.Uri r1 = android.net.Uri.parse(r1)
            r10.mFtHttpCsUri = r1
            goto L_0x015c
        L_0x014a:
            com.sec.internal.constants.Mno r1 = com.sec.internal.constants.Mno.CMCC
            java.lang.String r1 = r1.getName()
            boolean r1 = r1.equalsIgnoreCase(r0)
            if (r1 == 0) goto L_0x015c
            if (r5 != 0) goto L_0x015c
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.CPM
            r10.mImMsgTech = r1
        L_0x015c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImConfig.updateRcsConfiguration(android.content.Context, com.sec.internal.ims.config.RcsConfigurationHelper$ConfigData):void");
    }

    private String decrypt(String data) {
        if (data == null) {
            return null;
        }
        try {
            return new String(Base64.decode(data, 0));
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Failed to decrypt the data");
            return data;
        }
    }

    public void loadUserAlias(Context context) {
        if (this.mUserAliasEnabled) {
            this.mUserAlias = getUserAliasFromPreference(context);
        } else {
            this.mUserAlias = "";
        }
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public boolean getChatEnabled() {
        return this.mChatEnabled;
    }

    public boolean getGroupChatEnabled() {
        return this.mGroupChatEnabled;
    }

    public boolean getFtEnabled() {
        return this.mFtEnabled;
    }

    public boolean getFtHttpEnabled() {
        return this.mFtHttpEnabled;
    }

    public boolean getGlsPushEnabled() {
        return this.mGlsPushEnabled;
    }

    public boolean getGlsPullEnabled() {
        return this.mGlsPullEnabled;
    }

    public ImConstants.SlmAuth getSlmAuth() {
        return this.mSlmAuth;
    }

    public ImConstants.ImMsgTech getImMsgTech() {
        return this.mImMsgTech;
    }

    public boolean isImCapAlwaysOn() {
        return this.mImCapAlwaysOn;
    }

    public boolean isImWarnSf() {
        return this.mImWarnSf;
    }

    public boolean isGroupChatFullStandFwd() {
        return this.mGroupChatFullStandFwd;
    }

    public boolean isSmsFallbackAuth() {
        return this.mSmsFallbackAuth;
    }

    public boolean isAutAccept() {
        return this.mAutAccept;
    }

    public ImConstants.ImSessionStart getImSessionStart() {
        return this.mImSessionStart;
    }

    public boolean isAutAcceptGroupChat() {
        return this.mAutAcceptGroupChat;
    }

    public boolean isFirstMsgInvite() {
        return this.mFirstMsgInvite;
    }

    public int getTimerIdle() {
        return this.mTimerIdle;
    }

    public int getCallComposerTimerIdle() {
        return this.mCallComposerTimerIdle;
    }

    public int getMaxConcurrentSession() {
        return this.mMaxConcurrentSession;
    }

    public long getMaxSize1To1() {
        return this.mMaxSize1To1;
    }

    public long getMaxSize1ToM() {
        return this.mMaxSize1ToM;
    }

    public long getFtWarnSize() {
        return this.mFtWarnSize * 1024;
    }

    public long getMaxSizeFileTr() {
        return this.mMaxSizeFileTr * 1024;
    }

    public long getMaxSizeFileTrIncoming() {
        return this.mMaxSizeFileTrIncoming * 1024;
    }

    public boolean isFtThumb() {
        return this.mFtThumb;
    }

    public boolean isFtStAndFwEnabled() {
        return this.mFtStAndFwEnabled;
    }

    public boolean isFtAutAccept() {
        return this.mFtAutAccept;
    }

    public Uri getFtHttpCsUri() {
        return this.mFtHttpCsUri;
    }

    public String getFtHttpDLUrl() {
        return this.mFtHttpDLUrl;
    }

    public String getFtHttpCsUser() {
        ITelephonyManager iTelephonyManager;
        if (!"VZW".equals(OmcCode.get()) || !TextUtils.isEmpty(this.mFtHttpCsUser) || (iTelephonyManager = this.mTelephony) == null) {
            return this.mFtHttpCsUser;
        }
        String msisdn = iTelephonyManager.getMsisdn();
        if (TextUtils.isEmpty(msisdn)) {
            return this.mTelephony.getLine1Number();
        }
        return msisdn;
    }

    public String getFtHttpCsPwd() {
        return this.mFtHttpCsPwd;
    }

    public ImConstants.FtMech getFtDefaultMech() {
        return this.mFtDefaultMech;
    }

    public boolean isFtHttpCapAlwaysOn() {
        return this.mFtHttpCapAlwaysOn;
    }

    public int getMaxAdhocGroupSize() {
        return this.mMaxAdhocGroupSize;
    }

    public long getSlmMaxMsgSize() {
        return this.mSlmMaxMsgSize;
    }

    public boolean isFullSFGroupChat() {
        return this.mIsFullSFGroupChat;
    }

    public boolean isAggrImdnSupported() {
        return this.mIsAggrImdnSupported;
    }

    public boolean isEnableGroupChatListRetrieve() {
        return this.mEnableGroupChatListRetrieve && this.mConfFctyUri != null;
    }

    public void setFtAutAccept(Context context, int ftAutAccept, boolean isRoaming) {
        ImUserPreference userPreference = ImUserPreference.getInstance();
        if (userPreference.getFtAutAccept(context, getPhoneId()) != ftAutAccept) {
            userPreference.setFtAutAccept(context, getPhoneId(), ftAutAccept);
        }
        boolean z = false;
        if (ftAutAccept == -1) {
            if (!isRoaming && this.mFtAutAcceptOriginalConfig) {
                z = true;
            }
            this.mFtAutAccept = z;
            return;
        }
        if (ftAutAccept == 2 || (ftAutAccept == 1 && !isRoaming)) {
            z = true;
        }
        this.mFtAutAccept = z;
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    public synchronized String getUserAlias() {
        if (!this.mUserAliasEnabled || this.mUserAlias == null) {
            return "";
        }
        return this.mUserAlias;
    }

    public synchronized String getUserAliasFromPreference(Context context) {
        return ImUserPreference.getInstance().getUserAlias(context);
    }

    public synchronized void setUserAlias(Context context, String alias) {
        if (!this.mUserAliasEnabled) {
            Log.i(LOG_TAG, "alias disabled");
        } else if (alias == null) {
            this.mUserAlias = "";
        } else {
            this.mUserAlias = alias;
        }
        ImUserPreference.getInstance().setUserAlias(context, alias == null ? "" : alias);
    }

    public boolean isJoynIntegratedMessaging() {
        return this.mJoynIntegratedMessaging;
    }

    public int getMsgCapValidityTime() {
        return this.mMsgCapValidityTime;
    }

    private String getSipUserAgent(Context context) {
        String version;
        String userAgent = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.USER_AGENT, "");
        if (!TextUtils.isEmpty(userAgent)) {
            return userAgent;
        }
        String model = ConfigContract.BUILD.getTerminalModel();
        String version2 = ConfigContract.BUILD.getTerminalSwVersion();
        String clientVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        if ("VOD".equals(OmcCode.get())) {
            clientVersion = "4.1";
        }
        if ("DTM".equals(OmcCode.get()) || "DTR".equals(OmcCode.get()) || "SFR".equals(OmcCode.get()) || "TMZ".equals(OmcCode.get())) {
            version = version2.length() > 8 ? version2.substring(version2.length() - 8) : version2;
        } else {
            version = version2.length() > 3 ? version2.substring(version2.length() - 3) : version2;
        }
        return ConfigUtil.getFormattedUserAgent(SimUtil.getSimMno(this.mPhoneId), model, version, clientVersion);
    }

    public long getMaxSizeExtraFileTr() {
        return this.mMaxSizeExtraFileTr * 1024;
    }

    public Uri getFtHTTPExtraCSURI() {
        return this.mFtHTTPExtraCSURI;
    }

    public Uri getCbftHTTPCSURI() {
        return this.mCbftHTTPCSURI;
    }

    public boolean isFtHttpTrustAllCerts() {
        return this.mFtHttpTrustAllCerts;
    }

    public boolean getFtCancelMemoryFull() {
        return this.mFtCancelMemoryFull;
    }

    public boolean getFtFallbackAllFail() {
        return this.mFtFallbackAllFail;
    }

    public boolean getRespondDisplay() {
        boolean z = true;
        if (TapiServiceUtil.isSupportTapi()) {
            RcsSettingsUtils rcsSetting = RcsSettingsUtils.getInstance();
            if (rcsSetting != null) {
                this.mRespondDisplay = Boolean.parseBoolean(rcsSetting.readParameter(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS));
            }
        } else {
            this.mRespondDisplay = true;
        }
        if (ImsProfile.isRcsUpProfile(this.mRcsProfile)) {
            if (!this.mRespondDisplay || this.mDisplayNotificationSwitch != 0) {
                z = false;
            }
            this.mRespondDisplay = z;
        }
        return this.mRespondDisplay;
    }

    public boolean getEnableFtAutoResumable() {
        return this.mEnableFtAutoResumable;
    }

    public boolean isFtHttpOverDefaultPdn() {
        return this.mfThttpDefaultPdn;
    }

    public boolean getUserAliasEnabled() {
        return this.mUserAliasEnabled;
    }

    public int getReconnectGuardTimer() {
        return this.mReconnectGuardTimer;
    }

    public boolean isCfsTrigger() {
        return this.mCfsTrigger;
    }

    public int getChatRevokeTimer() {
        return this.mChatRevokeTimer;
    }

    public boolean getLegacyLatching() {
        return this.mLegacyLatching;
    }

    public int getPagerModeLimit() {
        if (ImsProfile.isRcsUp23AndUp24Profile(this.mRcsProfile)) {
            return this.mSlmSwitchOverSize;
        }
        return this.mPagerModeLimit;
    }

    public ImConstants.ChatbotMsgTechConfig getChatbotMsgTech() {
        return this.mChatbotMsgTech;
    }

    public boolean getBotPrivacyDisable() {
        return this.mBotPrivacyDisable;
    }

    private boolean getImCapAlwaysOn(Context context, RcsConfigurationHelper.ConfigData data) {
        if (RcsConfigurationHelper.isUp2NonTransitional(this.mRcsProfile, this.mPhoneId)) {
            return true;
        }
        return data.readBool(ConfigConstants.ConfigTable.IM_IM_CAP_ALWAYS_ON, false).booleanValue();
    }

    public String toString() {
        return "ImConfig(phoneId: " + this.mPhoneId + ")[mRcsProfile=" + this.mRcsProfile + ", mChatEnabled=" + this.mChatEnabled + ", mGroupChatEnabled=" + this.mGroupChatEnabled + ", mFtEnabled=" + this.mFtEnabled + ", mFtHttpCsUri=" + this.mFtHttpCsUri + ", mFtHttpCsUser=" + IMSLog.checker(this.mFtHttpCsUser) + ", mFtHttpCsPwd=" + IMSLog.checker(this.mFtHttpCsPwd) + ", mFtHttpDLUrl=" + this.mFtHttpDLUrl + ", mFtHttpEnabled=" + this.mFtHttpEnabled + ", mSlmAuth=" + this.mSlmAuth + ", mSmsFallbackAuth=" + this.mSmsFallbackAuth + ", mGlsPushEnabled=" + this.mGlsPushEnabled + ", mGlsPullEnabled=" + this.mGlsPullEnabled + ", mPresSrvCap=" + this.mPresSrvCap + ", mMaxAdhocGroupSize=" + this.mMaxAdhocGroupSize + ", mConfFctyUri=" + this.mConfFctyUri + ", mExploderUri=" + this.mExploderUri + ", mDeferredMsgFuncUri=" + this.mDeferredMsgFuncUri + ", mImCapAlwaysOn=" + this.mImCapAlwaysOn + ", mImWarnSf=" + this.mImWarnSf + ", mGroupChatFullStandFwd=" + this.mGroupChatFullStandFwd + ", mGroupChatOnlyFStandFwd=" + this.mGroupChatOnlyFStandFwd + ", mImCapNonRcs=" + this.mImCapNonRcs + ", mImWarnIw=" + this.mImWarnIw + ", mAutAccept=" + this.mAutAccept + ", mImSessionStart=" + this.mImSessionStart + ", mAutAcceptGroupChat=" + this.mAutAcceptGroupChat + ", mFirstMsgInvite=" + this.mFirstMsgInvite + ", mTimerIdle=" + this.mTimerIdle + ", mMaxConcurrentSession=" + this.mMaxConcurrentSession + ", mMultiMediaChat=" + this.mMultiMediaChat + ", mMaxSize1To1=" + this.mMaxSize1To1 + ", mMaxSize1ToM=" + this.mMaxSize1ToM + ", mSlmMaxMsgSize=" + this.mSlmMaxMsgSize + ", mImMsgTech=" + this.mImMsgTech + ", mChatbotMsgTech=" + this.mChatbotMsgTech + ", mMaxSizeFileTr=" + this.mMaxSizeFileTr + ", mFtWarnSize=" + this.mFtWarnSize + ", mFtThumb=" + this.mFtThumb + ", mFtStAndFwEnabled=" + this.mFtStAndFwEnabled + ", mFtCapAlwaysOn=" + this.mFtCapAlwaysOn + ", mFtAutAccept=" + this.mFtAutAccept + ", mFtDefaultMech=" + this.mFtDefaultMech + ", mJoynIntegratedMessaging=" + this.mJoynIntegratedMessaging + ", mMsgCapValidityTime=" + this.mMsgCapValidityTime + ", mFtHttpCapAlwaysOn=" + this.mFtHttpCapAlwaysOn + ", mChatRevokeTimer=" + this.mChatRevokeTimer + ", mMaxSizeFileTrIncoming=" + this.mMaxSizeFileTrIncoming + ", mMaxSize=" + this.mMaxSize + ", mFtHttpFallback=" + this.mFtHttpFallback + ", mMessagingUX=" + this.mMessagingUX + ", mUserAliasEnabled=" + this.mUserAliasEnabled + ", mMsgFbDefault=" + this.mMsgFbDefault + ", mReconnectGuardTimer=" + this.mReconnectGuardTimer + ", mCfsTrigger=" + this.mCfsTrigger + ", mMax1ToManyRecipients=" + this.mMax1ToManyRecipients + ", m1ToManySelectedTech=" + this.m1ToManySelectedTech + ", mDisplayNotificationSwitch=" + this.mDisplayNotificationSwitch + ", mFtMax1ToManyRecipients=" + this.mFtMax1ToManyRecipients + ", mFtFbDefault=" + this.mFtFbDefault + ", mServiceAvailabilityInfoExpiry=" + this.mServiceAvailabilityInfoExpiry + ", mPublicAccountAddr=" + this.mPublicAccountAddr + ", mMaxSizeExtraFileTr=" + this.mMaxSizeExtraFileTr + ", mFtHTTPExtraCSURI=" + this.mFtHTTPExtraCSURI + ", mCbftHTTPCSURI=" + this.mCbftHTTPCSURI + ", mIsFullSFGroupChat=" + this.mIsFullSFGroupChat + ", mIsAggrImdnSupported=" + this.mIsAggrImdnSupported + ", mUserAgent=" + this.mUserAgent + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFtHttpTrustAllCerts=" + this.mFtHttpTrustAllCerts + ", mFtCancelMemoryFull=" + this.mFtCancelMemoryFull + ", mFtFallbackAllFail=" + this.mFtFallbackAllFail + ", mRespondDisplay=" + this.mRespondDisplay + ", mEnableGroupChatListRetrieve=" + this.mEnableGroupChatListRetrieve + ", mEnableFtAutoResumable=" + this.mEnableFtAutoResumable + ", mLegacyLatching=" + this.mLegacyLatching + ", mExtAttImSlmMaxRecipients=" + this.mExtAttImSlmMaxRecipients + ", mExtAttImMSRPFtMaxSize=" + this.mExtAttImMSRPFtMaxSize + ", mfThttpDefaultPdn=" + this.mfThttpDefaultPdn + ", mPagerModeLimit=" + this.mPagerModeLimit + ", mBotPrivacyDisable=" + this.mBotPrivacyDisable + ", mSwitchOverSize=" + this.mSlmSwitchOverSize + "]";
    }
}
