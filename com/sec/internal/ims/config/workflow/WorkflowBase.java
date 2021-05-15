package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.ConfigProvider;
import com.sec.internal.ims.config.PowerController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.adapters.DialogAdapterConsentDecorator;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.interfaces.IRcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class WorkflowBase extends Handler implements IWorkflow {
    protected static final int AUTO_CONFIG_MAX_FLOWCOUNT = 20;
    protected static final long AUTO_CONFIG_MAX_TIMEOUT = 60000;
    protected static final int AUTO_CONFIG_RETRY_INTERVAL = 300;
    protected static final String CHARSET = "utf-8";
    protected static final int HANDLE_AUTO_CONFIG_CLEAN_UP = 14;
    protected static final int HANDLE_AUTO_CONFIG_CLEAR_DB = 2;
    protected static final int HANDLE_AUTO_CONFIG_DUALSIM = 6;
    protected static final int HANDLE_AUTO_CONFIG_FORCE = 0;
    protected static final int HANDLE_AUTO_CONFIG_GENERAL_ERROR_RETRY_TIMER_EXPIRED = 13;
    protected static final int HANDLE_AUTO_CONFIG_IMS_REGI_STATUS_CHANGED = 12;
    protected static final int HANDLE_AUTO_CONFIG_MOBILE_CONNECTION_FAILURE = 4;
    protected static final int HANDLE_AUTO_CONFIG_MOBILE_CONNECTION_SUCCESSFUL = 3;
    protected static final int HANDLE_AUTO_CONFIG_RESET = 8;
    protected static final int HANDLE_AUTO_CONFIG_SHARED_PREFERENCE_CHANGED = 10;
    protected static final int HANDLE_AUTO_CONFIG_SMS_DEFAULT_APPLICATION_CHANGED = 5;
    protected static final int HANDLE_AUTO_CONFIG_START = 1;
    protected static final int HANDLE_CURR_CONFIG_START = 11;
    protected static final int HANDLE_SHOW_MSISDN_DIALOG = 7;
    private static final String INTENT_VALIDITY_TIMEOUT = "com.sec.internal.ims.config.workflow.validity_timeout";
    protected static final int INVALID_SUBSCRIPTION_ID = -1;
    private static final String LAST_RCS_PROFILE = "lastRcsProfile";
    private static final String LAST_SW_VERSION = "lastSwVersion";
    /* access modifiers changed from: private */
    public static String LOG_TAG = WorkflowBase.class.getSimpleName();
    protected static final int NOTIFY_AUTO_CONFIGURATION_COMPLETED = 52;
    private static final String PREFERENCE_NAME = "workflowbase";
    private static final String RCS_PROFILE = "rcsprofile";
    protected static final String TIMESTAMP_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";
    private boolean isGlobalsettingsObserverRegisted;
    protected String mClientVersion;
    protected ConnectivityManager mConnectivityManager;
    protected final Context mContext;
    protected WorkflowCookieHandler mCookieHandler;
    protected IDialogAdapter mDialog;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    protected IHttpAdapter mHttp;
    protected boolean mHttpRedirect;
    protected String mIdentity;
    private BroadcastReceiver mIntentReceiver;
    protected boolean mIsUsingcheckSetToGS;
    protected int mLastErrorCode;
    protected int mLastErrorCodeNonRemote;
    protected final Mno mMno;
    protected boolean mMobileNetwork;
    protected final Handler mModuleHandler;
    protected WorkflowMsisdnHandler mMsisdnHandler;
    protected boolean mNeedToStopWork;
    protected Network mNetwork;
    protected NetworkRequest mNetworkRequest;
    protected WorkflowParamHandler mParamHandler;
    protected int mPhoneId;
    protected PowerController mPowerController;
    protected List<String> mRcsAppList;
    /* access modifiers changed from: private */
    public int mRcsAutoconfigSource;
    protected String mRcsCustomServerUrl;
    private final ContentObserver mRcsCustomServerUrlObserver;
    protected String mRcsProfile;
    protected String mRcsProvisioningVersion;
    protected String mRcsUPProfile;
    protected String mRcsVersion;
    protected final IRegistrationManager mRm;
    protected SharedInfo mSharedInfo;
    protected SharedPreferences mSharedPreferences;
    protected ISimManager mSm;
    protected boolean mStartForce;
    /* access modifiers changed from: private */
    public State mState;
    protected IStorageAdapter mStorage;
    protected int mSubId;
    protected ITelephonyAdapter mTelephony;
    /* access modifiers changed from: private */
    public PendingIntent mValidityIntent;
    protected IXmlParserAdapter mXmlParser;
    protected boolean sIsConfigOngoing;

    public interface Workflow {
        public static final int AUTHORIZE = 4;
        public static final int FETCH_HTTP = 2;
        public static final int FETCH_HTTPS = 3;
        public static final int FETCH_OTP = 5;
        public static final int FINISH = 8;
        public static final int INITIALIZE = 1;
        public static final int PARSE = 6;
        public static final int STORE = 7;

        Workflow run() throws Exception;
    }

    /* access modifiers changed from: protected */
    public abstract Workflow getNextWorkflow(int i);

    /* access modifiers changed from: package-private */
    public abstract void work();

    protected abstract class Initialize implements Workflow {
        protected Initialize() {
        }

        public Workflow run() throws Exception {
            init();
            if (WorkflowBase.this.mStartForce) {
                return WorkflowBase.this.getNextWorkflow(2);
            }
            int i = AnonymousClass3.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.this.getOpMode().ordinal()];
            if (i == 1 || i == 2 || i == 3) {
                return WorkflowBase.this.getNextWorkflow(2);
            }
            return WorkflowBase.this.getNextWorkflow(8);
        }

        /* access modifiers changed from: protected */
        public void init() throws NoInitialDataException {
            WorkflowBase.this.mSharedInfo.setUrl(WorkflowBase.this.mParamHandler.initUrl());
            WorkflowBase.this.mCookieHandler.clearCookie();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowBase$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        static {
            int[] iArr = new int[OpMode.values().length];
            $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = iArr;
            try {
                iArr[OpMode.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[OpMode.DISABLE_TEMPORARY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[OpMode.DORMANT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[OpMode.DISABLE_PERMANENTLY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    protected abstract class FetchHttp implements Workflow {
        protected FetchHttp() {
        }

        public Workflow run() throws Exception {
            setHttpHeader();
            WorkflowBase.this.mSharedInfo.setHttpResponse(WorkflowBase.this.getHttpResponse());
            WorkflowBase workflowBase = WorkflowBase.this;
            return workflowBase.handleResponse(this, workflowBase.mSharedInfo.getHttpResponse().getStatusCode());
        }

        /* access modifiers changed from: protected */
        public void setHttpHeader() {
            WorkflowBase.this.mSharedInfo.setHttpDefault();
        }
    }

    protected abstract class FetchHttps implements Workflow {
        /* access modifiers changed from: protected */
        public abstract void setHttps();

        protected FetchHttps() {
        }

        public Workflow run() throws Exception {
            setHttps();
            WorkflowBase.this.mSharedInfo.setHttpResponse(WorkflowBase.this.getHttpResponse());
            WorkflowBase workflowBase = WorkflowBase.this;
            return workflowBase.handleResponse(this, workflowBase.mSharedInfo.getHttpResponse().getStatusCode());
        }
    }

    protected abstract class Parse implements Workflow {
        protected Parse() {
        }

        public Workflow run() throws Exception {
            Map<String, String> parsedXml = WorkflowBase.this.mParamHandler.getParsedXmlFromBody();
            if (WorkflowBase.this.mParamHandler.isRequiredAuthentication(parsedXml)) {
                return WorkflowBase.this.getNextWorkflow(4);
            }
            parseParam(parsedXml);
            WorkflowBase.this.mSharedInfo.setParsedXml(parsedXml);
            return WorkflowBase.this.getNextWorkflow(7);
        }

        /* access modifiers changed from: protected */
        public void parseParam(Map<String, String> parsedXml) {
            WorkflowBase.this.mParamHandler.parseParam(parsedXml);
        }
    }

    protected abstract class Authorize implements Workflow {
        protected Authorize() {
        }

        public Workflow run() throws Exception {
            WorkflowBase.this.mPowerController.release();
            String otp = getOtp();
            String access$000 = WorkflowBase.LOG_TAG;
            Log.i(access$000, "otp: " + IMSLog.checker(otp));
            if (otp == null) {
                return WorkflowBase.this.getNextWorkflow(8);
            }
            WorkflowBase.this.mSharedInfo.setOtp(otp);
            WorkflowBase.this.mPowerController.lock();
            return WorkflowBase.this.getNextWorkflow(5);
        }

        /* access modifiers changed from: protected */
        public String getOtp() {
            return WorkflowBase.this.mTelephony.getOtp();
        }
    }

    protected abstract class FetchOtp implements Workflow {
        protected FetchOtp() {
        }

        public Workflow run() throws Exception {
            WorkflowBase.this.mSharedInfo.setHttpClean();
            setHttp();
            WorkflowBase.this.mSharedInfo.setHttpResponse(WorkflowBase.this.getHttpResponse());
            WorkflowBase workflowBase = WorkflowBase.this;
            return workflowBase.handleResponse(this, workflowBase.mSharedInfo.getHttpResponse().getStatusCode());
        }

        /* access modifiers changed from: protected */
        public void setHttp() {
            WorkflowBase.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowBase.this.mSharedInfo.getOtp());
        }
    }

    protected abstract class Store implements Workflow {
        public abstract Workflow run() throws Exception;

        protected Store() {
        }
    }

    protected abstract class Finish implements Workflow {
        protected Finish() {
        }

        public Workflow run() throws Exception {
            if (WorkflowBase.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowBase workflowBase = WorkflowBase.this;
                workflowBase.setLastErrorCode(workflowBase.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowBase.LOG_TAG, "workflow is finished");
            return null;
        }
    }

    public enum OpMode {
        ACTIVE(1),
        DISABLE_TEMPORARY(0),
        DISABLE_PERMANENTLY(-1),
        DISABLE(-2),
        DORMANT(-3),
        DISABLE_RCS_BY_USER(-4),
        ENABLE_RCS_BY_USER(-5),
        DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE(-6),
        DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE(-7),
        DISABLE_BY_RCS_DISABLED_STATE(-8),
        DORMANT_BY_RCS_DISABLED_STATE(-9),
        TURNEDOFF_BY_RCS_DISABLED_STATE(-10),
        DISABLED_TERMS_AND_CONDIDIONTS_REJECTED(-11),
        NONE(-12);
        
        int mValue;

        private OpMode(int value) {
            this.mValue = value;
        }

        /* access modifiers changed from: package-private */
        public int value() {
            return this.mValue;
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public WorkflowBase(Looper looper, Context context, Handler moduleHandler, Mno mno, ITelephonyAdapter telephonyAdapter, IStorageAdapter storageAdapter, IHttpAdapter httpAdapter, IXmlParserAdapter xmlParserAdapter, IDialogAdapter dialogAdapter, int phoneId) {
        super(looper);
        Context context2 = context;
        IDialogAdapter iDialogAdapter = dialogAdapter;
        int i = phoneId;
        this.mLastErrorCode = IWorkflow.DEFAULT_ERROR_CODE;
        this.mLastErrorCodeNonRemote = 200;
        this.mStartForce = false;
        this.mMobileNetwork = false;
        this.mHttpRedirect = false;
        this.sIsConfigOngoing = false;
        this.mIsUsingcheckSetToGS = false;
        this.mRcsCustomServerUrl = null;
        this.mRcsUPProfile = null;
        this.mNeedToStopWork = false;
        int i2 = -1;
        this.mRcsAutoconfigSource = -1;
        this.mIdentity = null;
        this.isGlobalsettingsObserverRegisted = false;
        this.mNetwork = null;
        this.mNetworkRequest = null;
        this.mConnectivityManager = null;
        this.mValidityIntent = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!WorkflowBase.this.checkRcsSwitchEur()) {
                    Log.i(WorkflowBase.LOG_TAG, "onReceive: validity period expired. but RCS is switch off. it should perform when switch on.");
                } else if (WorkflowBase.INTENT_VALIDITY_TIMEOUT.equals(intent.getAction())) {
                    PendingIntent unused = WorkflowBase.this.mValidityIntent = null;
                    ((IConfigModule) WorkflowBase.this.mModuleHandler).setAcsTryReason(WorkflowBase.this.mPhoneId, DiagnosisConstants.RCSA_ATRE.EXPIRE_VALIDITY);
                    if (WorkflowBase.this.isNetworkAvailable()) {
                        WorkflowBase.this.sendEmptyMessage(1);
                    } else {
                        Handler handler = WorkflowBase.this.mModuleHandler;
                        WorkflowBase workflowBase = WorkflowBase.this;
                        handler.sendMessage(workflowBase.obtainMessage(17, Integer.valueOf(workflowBase.mPhoneId)));
                    }
                    String access$000 = WorkflowBase.LOG_TAG;
                    Log.i(access$000, "onReceive: validity period expired. start config, mMobileNetwork = " + WorkflowBase.this.mMobileNetwork);
                }
            }
        };
        this.mRcsCustomServerUrlObserver = new ContentObserver(this) {
            public void onChange(boolean selfChange, Uri uri) {
                if (TextUtils.equals(WorkflowBase.this.mIdentity, WorkflowBase.this.mTelephony.getIdentityByPhoneId(WorkflowBase.this.mPhoneId)) && uri != null && uri.getPath().startsWith(GlobalSettingsConstants.CONTENT_URI.getPath())) {
                    String serverUrl = ImsRegistry.getString(WorkflowBase.this.mPhoneId, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
                    int autoConfigSource = ConfigUtil.getAutoconfigSourceWithFeature(WorkflowBase.this.mContext, WorkflowBase.this.mPhoneId, 0);
                    String rcsUPProfile = ImsRegistry.getString(WorkflowBase.this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "");
                    if (!TextUtils.equals(WorkflowBase.this.mRcsCustomServerUrl, serverUrl) || WorkflowBase.this.mRcsAutoconfigSource != autoConfigSource || !TextUtils.equals(WorkflowBase.this.mRcsUPProfile, rcsUPProfile)) {
                        String access$000 = WorkflowBase.LOG_TAG;
                        Log.i(access$000, "new rcs_custom_config_server_url=" + serverUrl + ", new rcs_autoconfig_source=" + autoConfigSource + ", new rcs_up_profile=" + rcsUPProfile);
                        WorkflowBase.this.mRcsCustomServerUrl = serverUrl;
                        int unused = WorkflowBase.this.mRcsAutoconfigSource = autoConfigSource;
                        WorkflowBase.this.removeMessages(2);
                        WorkflowBase.this.sendEmptyMessage(2);
                    }
                }
                if (WorkflowBase.this.mIsUsingcheckSetToGS) {
                    WorkflowBase.this.mParamHandler.checkSetToGS((Map<String, String>) null);
                }
                WorkflowBase workflowBase = WorkflowBase.this;
                workflowBase.mIdentity = workflowBase.mTelephony.getIdentityByPhoneId(WorkflowBase.this.mPhoneId);
            }
        };
        this.mPhoneId = i;
        Log.i(LOG_TAG, "WorkflowBase is created");
        this.mEventLog = new SimpleEventLog(context2, "Workflow", 500);
        this.mContext = context2;
        this.mModuleHandler = moduleHandler;
        this.mTelephony = telephonyAdapter;
        this.mStorage = storageAdapter;
        this.mHttp = httpAdapter;
        this.mXmlParser = xmlParserAdapter;
        this.mRcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context2, mno.getName(), this.mPhoneId);
        this.mRcsVersion = ImsRegistry.getString(this.mPhoneId, "rcs_version", "6.0");
        this.mClientVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        this.mRcsProvisioningVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PROVISIONING_VERSION, "2.0");
        this.mRcsAppList = Arrays.asList(ImsRegistry.getStringArray(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_APP_LIST, new String[0]));
        if (iDialogAdapter != null) {
            this.mDialog = new DialogAdapterConsentDecorator(iDialogAdapter, i);
        }
        this.mSharedPreferences = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0);
        this.mPowerController = new PowerController(context2, 60000);
        this.mState = new IdleState();
        this.mSm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        this.mRm = ImsRegistry.getRegistrationManager();
        ISimManager iSimManager = this.mSm;
        this.mSubId = iSimManager != null ? iSimManager.getSubscriptionId() : i2;
        this.mMno = mno;
        this.mCookieHandler = new WorkflowCookieHandler(this, this.mPhoneId);
        this.mParamHandler = new WorkflowParamHandler(this, this.mPhoneId, this.mTelephony);
        this.mMsisdnHandler = new WorkflowMsisdnHandler(this);
        createSharedInfo();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_VALIDITY_TIMEOUT);
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        registerGlobalSettingsObserver();
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowBase(android.os.Looper r15, android.content.Context r16, android.os.Handler r17, com.sec.internal.constants.Mno r18, int r19) {
        /*
            r14 = this;
            r11 = r16
            r12 = r17
            r13 = r19
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice
            r5.<init>(r11, r12, r13)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
            r7.<init>(r13)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r11, r12)
            r0 = r14
            r1 = r15
            r2 = r16
            r3 = r17
            r4 = r18
            r10 = r19
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBase.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        Log.i(str, "message: " + msg.what);
        addEventLog(LOG_TAG + "message: " + msg.what);
        int i = msg.what;
        if (i == 0) {
            Log.i(LOG_TAG, "forced startAutoConfig");
            removeMessages(0);
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            if (mno == Mno.CMCC || ConfigUtil.isRcsEur(mno)) {
                resetStorage();
            }
            this.mStartForce = true;
        } else if (i != 1) {
            if (i == 2) {
                Log.i(LOG_TAG, "clearStorage");
                clearStorage();
                return;
            } else if (i == 6) {
                Log.i(LOG_TAG, "autoconfig dualsim");
                resetStorage();
                sendEmptyMessage(1);
                return;
            } else if (i != 7) {
                Log.i(LOG_TAG, "unknown message!!!");
                return;
            } else {
                return;
            }
        }
        removeMessages(1);
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "AutoConfig:Already started");
            return;
        }
        this.sIsConfigOngoing = true;
        Log.i(LOG_TAG, "AutoConfig:START");
        this.mPowerController.lock();
        int oldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(oldVersion);
        }
        int newVersion = getVersion();
        String str2 = LOG_TAG;
        Log.i(str2, "oldVersion : " + oldVersion + " newVersion : " + newVersion);
        Log.i(LOG_TAG, "AutoConfig:FINISH");
        setCompleted(true);
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mModuleHandler.sendMessage(obtainMessage(3, oldVersion, newVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.sIsConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public boolean isNetworkAvailable() {
        String networkType = ConfigUtil.getNetworkType(this.mContext, this.mPhoneId);
        if (networkType.contains("internet") && ((IConfigModule) this.mModuleHandler).getAvailableNetworkForNetworkType(this.mPhoneId, 1) != null) {
            return true;
        }
        if (networkType.contains(DeviceConfigManager.IMS) && ((IConfigModule) this.mModuleHandler).getAvailableNetworkForNetworkType(this.mPhoneId, 2) != null) {
            return true;
        }
        if (!networkType.contains("wifi") || ((IConfigModule) this.mModuleHandler).getAvailableNetworkForNetworkType(this.mPhoneId, 3) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsSwitchEur() {
        boolean z = true;
        if (!ConfigUtil.isRcsEur(SimUtil.getSimMno(this.mPhoneId))) {
            return true;
        }
        if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, this.mPhoneId) != 1) {
            z = false;
        }
        boolean rcsSwitch = z;
        IMSLog.i(LOG_TAG, this.mPhoneId, "RCS switch : " + rcsSwitch);
        return rcsSwitch;
    }

    /* access modifiers changed from: protected */
    public void scheduleAutoconfig(int currentVersion) {
        Log.i(LOG_TAG, "scheduleAutoconfig enter");
        int versionFromServer = getVersionFromServer();
        String str = LOG_TAG;
        Log.i(str, "scheduleAutoconfig: getOpMode(): " + getOpMode() + " currentVersion: " + currentVersion + " getVersionBackup(): " + getVersionBackup() + " versionFromServer: " + versionFromServer);
        addEventLog(LOG_TAG + "scheduleAutoconfig: getOpMode(): " + getOpMode() + " currentVersion: " + currentVersion + " getVersionBackup(): " + getVersionBackup() + " versionFromServer: " + versionFromServer);
        IMSLog.c(LogClass.WFB_VERS, this.mPhoneId + ",OP:" + getOpMode() + ",CV:" + currentVersion + ",BV:" + getVersionBackup() + ",SV:" + versionFromServer);
        if (this.mStartForce) {
            cancelValidityTimer();
            Log.i(LOG_TAG, "Query autoconfig server now: force");
            work();
        } else if (currentVersion == -1 || versionFromServer == -1) {
            Log.i(LOG_TAG, "Skip querying autoconfig server since current version is -1");
        } else if (currentVersion == -2 || versionFromServer == -2) {
            this.mStartForce = true;
            Log.i(LOG_TAG, "Autoconfig version is -2. If scheduleAutoconfig was called, it means that user enabled RCS in settings. Force autoconfig.");
            scheduleAutoconfig(currentVersion);
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            String str2 = LOG_TAG;
            Log.i(str2, "nextAutoconfigTime=" + nextAutoconfigTime);
            int remainValidity = (int) ((nextAutoconfigTime - new Date().getTime()) / 1000);
            String str3 = LOG_TAG;
            Log.i(str3, "remainValidity=" + remainValidity);
            if (remainValidity <= 0) {
                boolean needQuery = true;
                if (SimUtil.getSimMno(this.mPhoneId) == Mno.TCE) {
                    Log.i(LOG_TAG, "waiting for query autoconfig");
                    needQuery = this.mDialog.getNextCancel();
                }
                if (needQuery) {
                    Log.i(LOG_TAG, "Query autoconfig server now");
                    work();
                    return;
                }
                Log.i(LOG_TAG, "Query autoconfig server - cancel by user");
                if (getVersion() > 0) {
                    setVersion(OpMode.DISABLE_TEMPORARY.value());
                }
            } else if (nextAutoconfigTime > 0) {
                String str4 = LOG_TAG;
                Log.i(str4, "Query autoconfig server after " + remainValidity + " seconds");
                IMSLog.c(LogClass.WFB_REMAIN_VALIDITY, this.mPhoneId + ",RVAL:" + remainValidity);
                addEventLog(LOG_TAG + ": Query autoconfig server after " + remainValidity + " seconds");
                setValidityTimer(remainValidity);
            }
        }
    }

    public void init() {
        this.mState.init();
    }

    public void cleanup() {
        this.mTelephony.cleanup();
        this.mState.cleanup();
        this.mDialog.cleanup();
        this.mPowerController.cleanup();
        this.mHttp.close();
        unregisterGlobalSettingsObserver();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    public void clearAutoConfigStorage() {
        Log.i(LOG_TAG, "clearAutoConfigStorage");
        removeMessages(2);
        sendEmptyMessage(2);
    }

    public void clearToken() {
        Log.i(LOG_TAG, "clearToken");
        setToken("");
    }

    public void removeValidToken() {
        Log.i(LOG_TAG, "remove valid token");
        ImsSharedPrefHelper.remove(this.mPhoneId, this.mContext, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(this.mPhoneId));
    }

    public void startAutoConfig(boolean mobile) {
        this.mState.startAutoConfig(mobile);
    }

    public void startAutoConfigDualsim(boolean mobile) {
        this.mState.startAutoConfigDualsim(mobile);
    }

    public void forceAutoConfig(boolean mobile) {
        this.mState.forceAutoConfig(mobile);
    }

    public Map<String, String> read(String path) {
        return this.mState.read(path);
    }

    public int getLastErrorCode() {
        String str = LOG_TAG;
        Log.i(str, "mLastErrorCode " + this.mLastErrorCode);
        return this.mLastErrorCode;
    }

    public void forceAutoConfigNeedResetConfig(boolean mobile) {
        setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
        this.mState.forceAutoConfig(mobile);
    }

    public boolean isConfigOngoing() {
        return this.sIsConfigOngoing;
    }

    public void stopWorkFlow() {
        Log.i(LOG_TAG, "Stop work flow in workflow state");
        this.mNeedToStopWork = true;
        this.sIsConfigOngoing = false;
        this.mHttp.close();
    }

    public void handleMSISDNDialog() {
        this.mState.handleMSISDNDialog();
    }

    public void onDefaultSmsPackageChanged() {
        this.mState.onDefaultSmsPackageChanged();
    }

    private static abstract class State implements IWorkflow {
        private State() {
        }

        public void cleanup() {
        }

        public void startAutoConfig(boolean mobile) {
        }

        public void startAutoConfigDualsim(boolean mobile) {
        }

        public void forceAutoConfig(boolean mobile) {
        }

        public Map<String, String> read(String path) {
            return null;
        }

        public void forceAutoConfigNeedResetConfig(boolean mobile) {
        }

        public void closeStorage() {
        }

        public void handleMSISDNDialog() {
        }

        public void onDefaultSmsPackageChanged() {
        }

        public IStorageAdapter getStorage() {
            return null;
        }
    }

    private class IdleState extends State {
        private IdleState() {
            super();
        }

        public void init() {
            if (WorkflowBase.this.initStorage()) {
                WorkflowBase workflowBase = WorkflowBase.this;
                workflowBase.handleSwVersionChange(workflowBase.getLastSwVersion());
                WorkflowBase workflowBase2 = WorkflowBase.this;
                workflowBase2.handleRcsProfileChange(workflowBase2.getLastRcsProfile());
                WorkflowBase workflowBase3 = WorkflowBase.this;
                State unused = workflowBase3.mState = new ReadyState();
            } else if (SimUtil.getSimMno(WorkflowBase.this.mPhoneId).isRjil()) {
                WorkflowBase.this.setCompleted(true);
                WorkflowBase.this.mModuleHandler.removeMessages(3);
                Handler handler = WorkflowBase.this.mModuleHandler;
                WorkflowBase workflowBase4 = WorkflowBase.this;
                handler.sendMessage(workflowBase4.obtainMessage(3, 0, 0, Integer.valueOf(workflowBase4.mPhoneId)));
            }
            SimpleEventLog access$600 = WorkflowBase.this.mEventLog;
            int i = WorkflowBase.this.mPhoneId;
            access$600.logAndAdd(i, "init: " + WorkflowBase.this.mState.getClass().getSimpleName());
        }
    }

    private class ReadyState extends State {
        private ReadyState() {
            super();
        }

        public void init() {
            Log.i(WorkflowBase.LOG_TAG, "already initialized");
        }

        public void cleanup() {
            WorkflowBase.this.sIsConfigOngoing = false;
        }

        public void startAutoConfig(boolean mobile) {
            String access$000 = WorkflowBase.LOG_TAG;
            Log.i(access$000, "startAutoConfig mobile:" + mobile + " Config status =" + WorkflowBase.this.sIsConfigOngoing);
            WorkflowBase.this.mMobileNetwork = mobile;
            if (!WorkflowBase.this.sIsConfigOngoing && !WorkflowBase.this.hasMessages(1)) {
                WorkflowBase.this.sendEmptyMessage(1);
            }
        }

        public void startAutoConfigDualsim(boolean mobile) {
            String access$000 = WorkflowBase.LOG_TAG;
            Log.i(access$000, "startAutoConfigDualsim mobile:" + mobile);
            WorkflowBase.this.mMobileNetwork = mobile;
            WorkflowBase.this.sendEmptyMessage(6);
        }

        public void forceAutoConfig(boolean mobile) {
            String access$000 = WorkflowBase.LOG_TAG;
            Log.i(access$000, "forceAutoConfig mobile:" + mobile);
            WorkflowBase.this.mMobileNetwork = mobile;
            WorkflowBase.this.sendEmptyMessage(0);
        }

        public Map<String, String> read(String path) {
            return WorkflowBase.this.mStorage.readAll(path);
        }

        public void forceAutoConfigNeedResetConfig(boolean mobile) {
            String access$000 = WorkflowBase.LOG_TAG;
            Log.i(access$000, "forceAutoConfigNeedResetConfig mobile:" + mobile);
            WorkflowBase.this.mMobileNetwork = mobile;
            WorkflowBase.this.setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            WorkflowBase.this.sendEmptyMessage(0);
        }

        public void handleMSISDNDialog() {
            Log.i(WorkflowBase.LOG_TAG, "handleMSISDNDialog()");
            WorkflowBase.this.sendEmptyMessage(7);
        }

        public void closeStorage() {
            WorkflowBase.this.mStorage.close();
        }

        public void onDefaultSmsPackageChanged() {
            Log.i(WorkflowBase.LOG_TAG, "onDefaultSmsPackageChanged");
            WorkflowBase.this.sendEmptyMessage(5);
        }
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        TrafficStats.setThreadStatsTag(Process.myTid());
        this.mHttp.close();
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setMethod(this.mSharedInfo.getUserMethod());
        this.mSharedInfo.setUserMethod("GET");
        this.mHttp.setContext(this.mContext);
        this.mHttp.open(this.mSharedInfo.getUrl());
        String str = LOG_TAG;
        IMSLog.s(str, "request starts " + this.mSharedInfo.getUrl());
        IHttpAdapter.Response response = this.mHttp.request();
        this.mHttp.close();
        return response;
    }

    /* access modifiers changed from: protected */
    public boolean isSmsAppDefault() {
        Log.i(LOG_TAG, "get default sms app.");
        String defaultSmsApp = null;
        try {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            String str = LOG_TAG;
            Log.i(str, "Failed to getDefaultSmsPackage: " + e);
        }
        if (defaultSmsApp == null) {
            Log.i(LOG_TAG, "default sms app is null");
            return false;
        }
        String samsungPackage = PackageUtils.getMsgAppPkgName(this.mContext);
        boolean result = TextUtils.equals(defaultSmsApp, samsungPackage);
        String str2 = LOG_TAG;
        Log.i(str2, "default sms app:" + defaultSmsApp + " samsungPackage:" + samsungPackage);
        String str3 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("isDefaultMessageAppInUse : ");
        sb.append(result);
        Log.i(str3, sb.toString());
        return result;
    }

    /* access modifiers changed from: protected */
    public void setLastErrorCode(int error) {
        IMSLog.c(LogClass.WFB_LAST_ERROR_CODE, this.mPhoneId + ",LEC:" + error);
        this.mLastErrorCode = error;
    }

    /* access modifiers changed from: protected */
    public Workflow handleResponse(Workflow current, int errorCode) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        Log.i(str, "handleResponse: " + errorCode);
        addEventLog(LOG_TAG + "handleResponse: " + errorCode);
        this.mLastErrorCode = errorCode;
        if (errorCode == 0) {
            Log.i(LOG_TAG, "RCS configuration server is unreachable");
            return getNextWorkflow(8);
        } else if (errorCode == 200) {
            Log.i(LOG_TAG, "200 ok received and it's normal case");
            if (current instanceof FetchHttp) {
                return getNextWorkflow(3);
            }
            if ((current instanceof FetchHttps) || (current instanceof FetchOtp)) {
                return getNextWorkflow(6);
            }
            return null;
        } else if (errorCode == 403) {
            Log.i(LOG_TAG, "set version to zero");
            setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            return getNextWorkflow(8);
        } else if (errorCode == 500) {
            Log.i(LOG_TAG, "internal server error");
            return getNextWorkflow(8);
        } else if (errorCode == 503) {
            long retryAfterTime = getretryAfterTime();
            String str2 = LOG_TAG;
            Log.i(str2, "retry after " + retryAfterTime + " sec");
            sleep(1000 * retryAfterTime);
            return getNextWorkflow(3);
        } else if (errorCode != 511) {
            switch (errorCode) {
                case 800:
                case 801:
                    Log.i(LOG_TAG, "SSL error happened");
                    return getNextWorkflow(8);
                case 802:
                case 803:
                case 804:
                    Log.i(LOG_TAG, "Socket error happened");
                    return getNextWorkflow(8);
                case 805:
                    Log.i(LOG_TAG, "Unknown Host error happened");
                    return getNextWorkflow(8);
                default:
                    throw new UnknownStatusException("unknown http status code");
            }
        } else {
            Log.i(LOG_TAG, "The token isn't valid");
            setToken("");
            removeValidToken();
            return getNextWorkflow(1);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v27, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v6, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.config.workflow.WorkflowBase.Workflow handleResponse2(com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r8, com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r9, com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r10) throws com.sec.internal.ims.config.exception.InvalidHeaderException, com.sec.internal.ims.config.exception.UnknownStatusException, java.net.ConnectException {
        /*
            r7 = this;
            r0 = 0
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            int r1 = r1.getStatusCode()
            r7.setLastErrorCode(r1)
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "handleResponse2: mLastErrorCode: "
            r2.append(r3)
            int r3 = r7.getLastErrorCode()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            int r1 = r7.getLastErrorCode()
            if (r1 == 0) goto L_0x01af
            r2 = 200(0xc8, float:2.8E-43)
            if (r1 == r2) goto L_0x01a6
            r2 = 401(0x191, float:5.62E-43)
            java.lang.String r3 = "POST"
            if (r1 == r2) goto L_0x0198
            r2 = 403(0x193, float:5.65E-43)
            if (r1 == r2) goto L_0x0188
            r2 = 500(0x1f4, float:7.0E-43)
            if (r1 == r2) goto L_0x017f
            r2 = 503(0x1f7, float:7.05E-43)
            if (r1 == r2) goto L_0x0162
            r2 = 511(0x1ff, float:7.16E-43)
            java.lang.String r4 = ""
            r5 = 0
            if (r1 == r2) goto L_0x012f
            r2 = 301(0x12d, float:4.22E-43)
            java.lang.String r6 = "Location"
            if (r1 == r2) goto L_0x00f3
            r2 = 302(0x12e, float:4.23E-43)
            if (r1 == r2) goto L_0x007f
            r2 = 800(0x320, float:1.121E-42)
            if (r1 == r2) goto L_0x0075
            r2 = 801(0x321, float:1.122E-42)
            if (r1 == r2) goto L_0x0066
            com.sec.internal.ims.config.exception.UnknownStatusException r1 = new com.sec.internal.ims.config.exception.UnknownStatusException
            java.lang.String r2 = "unknown http status code"
            r1.<init>(r2)
            throw r1
        L_0x0066:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "Connect exception, please retry"
            android.util.Log.i(r1, r2)
            java.net.ConnectException r1 = new java.net.ConnectException
            java.lang.String r2 = "Connection failed"
            r1.<init>(r2)
            throw r1
        L_0x0075:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "SSL handshake failed"
            android.util.Log.i(r1, r2)
            r0 = r10
            goto L_0x01b8
        L_0x007f:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "oidc redirects"
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            java.util.Map r1 = r1.getHeader()
            java.lang.String r2 = "Authentication-Info"
            java.lang.Object r1 = r1.get(r2)
            if (r1 == 0) goto L_0x009d
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            r1.setUserMethod(r3)
        L_0x009d:
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            java.util.Map r1 = r1.getHeader()
            java.lang.Object r1 = r1.get(r6)
            if (r1 == 0) goto L_0x01b8
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            java.util.Map r1 = r1.getHeader()
            java.lang.Object r1 = r1.get(r6)
            java.util.List r1 = (java.util.List) r1
            java.lang.Object r1 = r1.get(r5)
            java.lang.String r1 = (java.lang.String) r1
            if (r1 == 0) goto L_0x00f1
            java.lang.String r2 = "https"
            boolean r2 = r1.startsWith(r2)
            if (r2 == 0) goto L_0x00e1
            com.sec.internal.ims.config.SharedInfo r2 = r7.mSharedInfo
            java.lang.String r3 = "\\?"
            java.lang.String[] r3 = r1.split(r3)
            r3 = r3[r5]
            r2.setUrl(r3)
            com.sec.internal.ims.config.workflow.WorkflowCookieHandler r2 = r7.mCookieHandler
            r2.clearCookie()
            r0 = r9
            goto L_0x00f1
        L_0x00e1:
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "https redirect not found"
            android.util.Log.i(r2, r3)
            com.sec.internal.ims.config.exception.InvalidHeaderException r2 = new com.sec.internal.ims.config.exception.InvalidHeaderException
            java.lang.String r3 = "redirect location should be https instead of http"
            r2.<init>(r3)
            throw r2
        L_0x00f1:
            goto L_0x01b8
        L_0x00f3:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "http redirects"
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            java.util.Map r1 = r1.getHeader()
            java.lang.Object r1 = r1.get(r6)
            if (r1 == 0) goto L_0x0122
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r1 = r1.getHttpResponse()
            java.util.Map r1 = r1.getHeader()
            java.lang.Object r1 = r1.get(r6)
            java.util.List r1 = (java.util.List) r1
            java.lang.Object r1 = r1.get(r5)
            r4 = r1
            java.lang.String r4 = (java.lang.String) r4
            goto L_0x0123
        L_0x0122:
        L_0x0123:
            r1 = r4
            com.sec.internal.ims.config.SharedInfo r2 = r7.mSharedInfo
            r2.setUrl(r1)
            r2 = 1
            r7.mHttpRedirect = r2
            r0 = r8
            goto L_0x01b8
        L_0x012f:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "RCC07_RCS 5.1 Specification 2.3.3.4.2.1 - The token is no longer valid"
            android.util.Log.i(r1, r2)
            r7.setToken(r4)
            r7.removeValidToken()
            int r1 = r7.mPhoneId
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r1)
            java.lang.String r2 = "ps_only_network"
            boolean r2 = r1.boolSetting(r2)
            if (r2 == 0) goto L_0x0160
            android.os.Handler r2 = r7.mModuleHandler
            r3 = 3
            r2.removeMessages(r3)
            android.os.Handler r2 = r7.mModuleHandler
            int r4 = r7.mPhoneId
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            android.os.Message r3 = r7.obtainMessage(r3, r5, r5, r4)
            r2.sendMessage(r3)
        L_0x0160:
            r0 = r8
            goto L_0x01b8
        L_0x0162:
            long r1 = r7.getretryAfterTime()
            r3 = 1000(0x3e8, double:4.94E-321)
            long r3 = r3 * r1
            r7.sleep(r3)
            com.sec.internal.constants.Mno r3 = com.sec.internal.helper.SimUtil.getMno()
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.CMCC
            if (r3 != r4) goto L_0x017d
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "CMCC - next init"
            android.util.Log.i(r3, r4)
            r0 = r8
            goto L_0x01b8
        L_0x017d:
            r0 = r9
            goto L_0x01b8
        L_0x017f:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "fail. retry next boot"
            android.util.Log.i(r1, r2)
            r0 = r10
            goto L_0x01b8
        L_0x0188:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "set version to 0. retry next boot"
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_TEMPORARY
            r2 = 0
            r7.setOpMode(r1, r2)
            r0 = r10
            goto L_0x01b8
        L_0x0198:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "401"
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.config.SharedInfo r1 = r7.mSharedInfo
            r1.setUserMethod(r3)
            r0 = r9
            goto L_0x01b8
        L_0x01a6:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "normal case"
            android.util.Log.i(r1, r2)
            r0 = r9
            goto L_0x01b8
        L_0x01af:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "RCS configuration server is unreachable. retry next boot"
            android.util.Log.i(r1, r2)
            r0 = r10
        L_0x01b8:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBase.handleResponse2(com.sec.internal.ims.config.workflow.WorkflowBase$Workflow, com.sec.internal.ims.config.workflow.WorkflowBase$Workflow, com.sec.internal.ims.config.workflow.WorkflowBase$Workflow):com.sec.internal.ims.config.workflow.WorkflowBase$Workflow");
    }

    /* access modifiers changed from: protected */
    public OpMode getOpMode(Map<String, String> data) {
        OpMode mode = OpMode.ACTIVE;
        int version = getVersion(data);
        if (OpMode.ACTIVE.value() <= version) {
            String str = LOG_TAG;
            Log.i(str, "version :" + version);
            return OpMode.ACTIVE;
        }
        OpMode[] values = OpMode.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            OpMode currentMode = values[i];
            if (currentMode.value() == version) {
                mode = currentMode;
                break;
            }
            i++;
        }
        String str2 = LOG_TAG;
        Log.i(str2, "operation mode :" + mode.name());
        return mode;
    }

    /* access modifiers changed from: protected */
    public OpMode getOpMode() {
        OpMode mode = OpMode.ACTIVE;
        int version = getVersion();
        String str = LOG_TAG;
        Log.i(str, "getOpMode :" + version);
        if (OpMode.ACTIVE.value() <= version) {
            String str2 = LOG_TAG;
            Log.i(str2, "OpMode.ACTIVE.value(): " + OpMode.ACTIVE.value());
            return OpMode.ACTIVE;
        }
        OpMode[] values = OpMode.values();
        int length = values.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            OpMode currentMode = values[i];
            if (currentMode.value() == version) {
                mode = currentMode;
                break;
            }
            i++;
        }
        String str3 = LOG_TAG;
        Log.i(str3, "operation mode :" + mode.name());
        return mode;
    }

    /* access modifiers changed from: protected */
    public void writeDataToStorage(Map<String, String> data) {
        synchronized (IRcsPolicyManager.class) {
            clearStorage();
            this.mStorage.writeAll(data);
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        Log.i(str, "new operation mode :" + mode.name());
        IMSLog.c(LogClass.WFB_OP_MODE_NAME, this.mPhoneId + ",NOP:" + mode.name());
        addEventLog(LOG_TAG + ": new operation mode :" + mode.name());
        int i = AnonymousClass3.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    if (getVersion() != OpMode.DORMANT.value()) {
                        setVersionBackup(getVersion());
                    }
                    setVersion(mode.value());
                    return;
                } else if (!(i == 4 || i == 5)) {
                    return;
                }
            }
            clearStorage();
            setVersion(mode.value());
            setValidity(mode.value());
            return;
        }
        if (data != null) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "data :" + data);
            Mno mno = SimUtil.getSimMno(this.mPhoneId);
            if (mno.isKor() || mno == Mno.USCC || getVersion() < getVersion(data) || this.mStartForce || (getVersion() != getVersion(data) && mno == Mno.RJIL)) {
                writeDataToStorage(data);
            } else {
                Log.i(LOG_TAG, "the same or lower version and not RJIL. remain to previous data");
                String oldToken = getToken();
                String newToken = getToken(data);
                setValidity(getValidity(data));
                if (newToken != null && (oldToken == null || !newToken.equals(oldToken))) {
                    String str3 = LOG_TAG;
                    Log.i(str3, "token is changed. setToken : " + oldToken + " -> " + newToken);
                    setToken(newToken);
                }
            }
        } else {
            Log.i(LOG_TAG, "null data. remain previous mode & data");
        }
        setNextAutoconfigTimeFromValidity(getValidity());
    }

    /* access modifiers changed from: protected */
    public boolean initStorage() {
        Log.i(LOG_TAG, "initStorage()");
        if (this.mStorage.getState() != 1) {
            int retryCount = 60;
            if (!this.mTelephony.isReady()) {
                this.mEventLog.logAndAdd("initStorage: Telephony readiness check start.");
            }
            while (!this.mTelephony.isReady() && retryCount > 0) {
                sleep(1000);
                retryCount--;
            }
            this.mEventLog.logAndAdd("initStorage: Telephony readiness check done. Now check identity.");
            this.mIdentity = "";
            while (true) {
                if (retryCount <= 0) {
                    break;
                }
                String identityByPhoneId = this.mTelephony.getIdentityByPhoneId(this.mPhoneId);
                this.mIdentity = identityByPhoneId;
                if (identityByPhoneId != null && !identityByPhoneId.isEmpty()) {
                    Log.i(LOG_TAG, "initStorage. getIdentityByPhoneId is valid");
                    break;
                }
                sleep(1000);
                retryCount--;
            }
            if (retryCount <= 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "initStorage: failed");
                IMSLog.c(LogClass.WFB_STORAGE_INIT_FAIL, this.mPhoneId + ",STOR_IF");
                addEventLog(LOG_TAG + " initStorage: failed");
                return false;
            }
            String hashedIdentity = HashManager.generateMD5(this.mIdentity);
            this.mEventLog.logAndAdd("Open storage: " + IMSLog.checker(this.mIdentity));
            this.mStorage.open(this.mContext, ConfigProvider.CONFIG_DB_NAME_PREFIX + hashedIdentity, this.mPhoneId);
        }
        checkStorage();
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        String rcsAs = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
        if (mno == Mno.TCE || mno == Mno.VZW || mno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(rcsAs) || ImsConstants.RCS_AS.SEC.equals(rcsAs)) {
            this.mParamHandler.checkSetToGS((Map<String, String>) null);
            this.mIsUsingcheckSetToGS = true;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int getStorageState() {
        return this.mStorage.getState();
    }

    /* access modifiers changed from: protected */
    public void resetStorage() {
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",reset ACS Config");
        this.mStorage.close();
        initStorage();
    }

    /* access modifiers changed from: protected */
    public void clearStorage() {
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",clearStorage");
        this.mStorage.deleteAll();
        removeValidToken();
        checkStorage();
    }

    /* access modifiers changed from: protected */
    public void checkStorage() {
        List<String> defaultList = new ArrayList<>();
        for (Map.Entry<String, String> item : ConfigContract.STORAGE_DEFAULT.entrySet()) {
            if (this.mStorage.read(item.getKey()) == null) {
                defaultList.add(item.getKey());
                this.mStorage.write(item.getKey(), item.getValue());
            }
        }
        int size = defaultList.size();
        if (size > 0) {
            String str = LOG_TAG;
            Log.i(str, "checkStorage: Default set(" + size + "): " + defaultList);
        }
    }

    public void closeStorage() {
        this.mState.closeStorage();
    }

    /* access modifiers changed from: protected */
    public void setCompleted(boolean value) {
        ContentValues data = new ContentValues();
        data.put(ConfigConstants.PATH.INFO_COMPLETED, String.valueOf(value));
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri.Builder buildUpon = ConfigConstants.CONTENT_URI.buildUpon();
        contentResolver.insert(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + this.mPhoneId).build(), data);
    }

    /* access modifiers changed from: protected */
    public int getVersion(Map<String, String> data) {
        try {
            return Integer.parseInt(data.get("root/vers/version"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return getVersion();
        }
    }

    /* access modifiers changed from: protected */
    public int getVersion() {
        try {
            return Integer.parseInt(this.mStorage.read("root/vers/version"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersion(int version) {
        IMSLog.c(LogClass.WFB_SET_VERSION, this.mPhoneId + ",VER:" + version);
        this.mStorage.write("root/vers/version", String.valueOf(version));
    }

    /* access modifiers changed from: protected */
    public int getVersionFromServer() {
        try {
            return Integer.parseInt(this.mStorage.read(ConfigConstants.PATH.VERS_VERSION_FROM_SERVER));
        } catch (NullPointerException | NumberFormatException e) {
            String str = LOG_TAG;
            IMSLog.i(str, "getVersionFromServer: " + e.getMessage());
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersionFromServer(int version) {
        IMSLog.c(LogClass.WFB_VER_FROM_SERVER, this.mPhoneId + ",VERFS:" + version);
        this.mStorage.write(ConfigConstants.PATH.VERS_VERSION_FROM_SERVER, String.valueOf(version));
    }

    /* access modifiers changed from: protected */
    public String getVersionBackup() {
        String result = this.mStorage.read(ConfigConstants.PATH.VERS_VERSION_BACKUP);
        return TextUtils.isEmpty(result) ? "0" : result;
    }

    /* access modifiers changed from: protected */
    public int getParsedIntVersionBackup() {
        try {
            return Integer.parseInt(getVersionBackup());
        } catch (NullPointerException | NumberFormatException e) {
            String str = LOG_TAG;
            Log.i(str, "getParsedIntVersionBackup: cannot get backupVersion: " + e.getMessage());
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersionBackup(int version) {
        this.mStorage.write(ConfigConstants.PATH.VERS_VERSION_BACKUP, String.valueOf(version));
    }

    /* access modifiers changed from: protected */
    public int getValidity(Map<String, String> data) {
        int result = 0;
        try {
            result = Integer.parseInt(data.get("root/vers/validity"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        String str = LOG_TAG;
        Log.i(str, "getValidity from config.xml :" + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public int getValidity() {
        int result = 0;
        try {
            result = Integer.parseInt(this.mStorage.read("root/vers/validity"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
        }
        String str = LOG_TAG;
        Log.i(str, "getValidity from config DB :" + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void setValidity(int validity) {
        IMSLog.c(LogClass.WFB_VALIDITY, this.mPhoneId + ",VAL:" + validity);
        this.mStorage.write("root/vers/validity", String.valueOf(validity));
    }

    /* access modifiers changed from: protected */
    public int removeToken() {
        return this.mStorage.delete("root/token/token");
    }

    /* access modifiers changed from: protected */
    public String getToken(Map<String, String> data) {
        return data.get("root/token/token");
    }

    /* access modifiers changed from: protected */
    public String getToken() {
        return this.mStorage.read("root/token/token");
    }

    /* access modifiers changed from: protected */
    public void setToken(String token) {
        if ("".equals(token)) {
            IMSLog.c(LogClass.WFB_RESET_TOKEN, this.mPhoneId + ",reset ACS token");
        }
        this.mStorage.write("root/token/token", token);
    }

    /* access modifiers changed from: protected */
    public void sleep(long time) {
        this.mPowerController.sleep(time);
    }

    /* access modifiers changed from: protected */
    public void setValidityTimer(int validityPeriod) {
        if (this.mValidityIntent != null) {
            Log.i(LOG_TAG, "setValidityTimer: validityTimer is already running. Stopping it.");
            cancelValidityTimer();
        }
        String str = LOG_TAG;
        Log.i(str, "setValidityTimer: start validity period timer (" + validityPeriod + " sec)");
        if (validityPeriod == 0) {
            sendEmptyMessage(1);
        } else if (validityPeriod > 0) {
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_VALIDITY_TIMEOUT), 134217728);
            this.mValidityIntent = broadcast;
            AlarmTimer.start(this.mContext, broadcast, ((long) validityPeriod) * 1000);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelValidityTimer() {
        if (this.mValidityIntent == null) {
            Log.i(LOG_TAG, "cancelValidityTimer: validityTimer is not running.");
            return;
        }
        Log.i(LOG_TAG, "cancelValidityTimer:");
        AlarmTimer.stop(this.mContext, this.mValidityIntent);
        this.mValidityIntent = null;
    }

    public boolean checkNetworkConnectivity() {
        return true;
    }

    /* access modifiers changed from: protected */
    public long getNextAutoconfigTime() {
        long result = 0;
        String nextTime = this.mStorage.read(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME);
        if (!TextUtils.isEmpty(nextTime)) {
            try {
                result = Long.parseLong(nextTime);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String str = LOG_TAG;
        Log.i(str, "getNextAutoconfigTime = " + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void setNextAutoconfigTime(long timestamp) {
        this.mStorage.write(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME, String.valueOf(timestamp));
    }

    /* access modifiers changed from: protected */
    public void setNextAutoconfigTimeFromValidity(int validity) {
        String str = LOG_TAG;
        Log.i(str, "setNextAutoconfigTimeFromValidity:" + validity);
        if (validity > 0) {
            setNextAutoconfigTime(new Date().getTime() + (((long) validity) * 1000));
        }
    }

    /* access modifiers changed from: protected */
    public void setTcUserAccept(int userAccept) {
        String str = LOG_TAG;
        Log.i(str, "setTcUserAccept:" + userAccept);
        this.mStorage.write(ConfigConstants.PATH.TC_POPUP_USER_ACCEPT, String.valueOf(userAccept));
    }

    /* access modifiers changed from: protected */
    public String getLastSwVersion() {
        String mLastSwVersion = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0).getString(LAST_SW_VERSION, "");
        String str = LOG_TAG;
        Log.i(str, "getLastSwVersion:" + mLastSwVersion);
        return mLastSwVersion;
    }

    /* access modifiers changed from: protected */
    public void setLastSwVersion(String swVersion) {
        SharedPreferences sp = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0);
        String str = LOG_TAG;
        Log.i(str, "setLastSwVersion:" + swVersion);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(LAST_SW_VERSION, swVersion);
        editor.apply();
    }

    /* access modifiers changed from: protected */
    public String getLastRcsProfile() {
        Context context = this.mContext;
        String lastRcsProfile = context.getSharedPreferences("rcsprofile_" + this.mPhoneId, 0).getString(LAST_RCS_PROFILE, "");
        String str = LOG_TAG;
        Log.i(str, "getLastRcsProfile:" + lastRcsProfile);
        return lastRcsProfile;
    }

    /* access modifiers changed from: protected */
    public void setLastRcsProfile(String rcsProfile) {
        Context context = this.mContext;
        SharedPreferences sp = context.getSharedPreferences("rcsprofile_" + this.mPhoneId, 0);
        String str = LOG_TAG;
        Log.i(str, "setLastRcsProfile:" + rcsProfile);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(LAST_RCS_PROFILE, rcsProfile);
        editor.apply();
    }

    private void registerGlobalSettingsObserver() {
        this.mContext.getContentResolver().registerContentObserver(GlobalSettingsConstants.CONTENT_URI, false, this.mRcsCustomServerUrlObserver);
        this.isGlobalsettingsObserverRegisted = true;
        this.mRcsCustomServerUrl = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
        this.mRcsAutoconfigSource = ConfigUtil.getAutoconfigSourceWithFeature(this.mContext, this.mPhoneId, 0);
        this.mRcsUPProfile = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "");
        String mGlobalSettingsValues = "mRcsCustomConfigServerUrl= " + this.mRcsCustomServerUrl + ", mRcsAutoconfigSource=" + this.mRcsAutoconfigSource + ", mRcsUPProfile=" + this.mRcsUPProfile;
        Log.i(LOG_TAG, "registerGlobalSettingsObserver: " + mGlobalSettingsValues);
        addEventLog(LOG_TAG + ": registerGlobalSettingsObserver : " + mGlobalSettingsValues);
    }

    private void unregisterGlobalSettingsObserver() {
        Log.i(LOG_TAG, "unregisterGlobalSettingsObserver");
        if (this.isGlobalsettingsObserverRegisted) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mRcsCustomServerUrlObserver);
            this.isGlobalsettingsObserverRegisted = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean needScheduleAutoconfig(int simslot) {
        if (!OmcCode.isTmpSimSwap(simslot)) {
            return true;
        }
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if (!this.mParamHandler.isSupportCarrierVersion() || mno.isVodafone() || mno.isOrange() || mno.isTmobile()) {
            return true;
        }
        Log.i(LOG_TAG, "needScheduleAutoconfig: Temporal SIM swapped, skip autoconfiguration");
        setVersion(OpMode.DISABLE_TEMPORARY.value());
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String lastSwVersion) {
        Log.i(LOG_TAG, "handleSwVersionChange");
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        if (lastSwVersion.equals(ConfigConstants.BUILD.TERMINAL_SW_VERSION)) {
            return;
        }
        if (mno == Mno.BELL || mno.isKor()) {
            Log.i(LOG_TAG, "there is fota upgrade found");
            setNextAutoconfigTime(new Date().getTime());
            setLastSwVersion(ConfigConstants.BUILD.TERMINAL_SW_VERSION);
            ((IConfigModule) this.mModuleHandler).setAcsTryReason(this.mPhoneId, DiagnosisConstants.RCSA_ATRE.CHANGE_SWVERSION);
        }
    }

    /* access modifiers changed from: protected */
    public void handleRcsProfileChange(String lastRcsProfile) {
        Log.i(LOG_TAG, "handleRcsProfileChange");
        String rcsProfile = ImsRegistry.getRcsProfileType(this.mPhoneId);
        Mno mno = SimUtil.getSimMno(this.mPhoneId);
        String rcsAs = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
        String str = LOG_TAG;
        Log.i(str, "handleRcsProfileChange: now: " + rcsProfile + " last: " + lastRcsProfile);
        if (lastRcsProfile.equals(rcsProfile)) {
            return;
        }
        if ((mno.isVodafone() && ("UP_1.0".equals(rcsProfile) || "UP_2.0".equals(rcsProfile))) || mno == Mno.CMCC || ImsConstants.RCS_AS.JIBE.equals(rcsAs) || ImsConstants.RCS_AS.SEC.equals(rcsAs)) {
            Log.i(LOG_TAG, "There is RCS profile update found");
            setVersion(OpMode.DISABLE_TEMPORARY.value());
            setNextAutoconfigTime(new Date().getTime());
            setLastRcsProfile(rcsProfile);
        }
    }

    /* access modifiers changed from: protected */
    public long getretryAfterTime() throws InvalidHeaderException {
        long retryAfterTime;
        try {
            String retryAfter = (String) this.mSharedInfo.getHttpResponse().getHeader().get(HttpRequest.HEADER_RETRY_AFTER).get(0);
            if (retryAfter.matches("[0-9]+")) {
                retryAfterTime = (long) Integer.parseInt(retryAfter);
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.ENGLISH);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateFormat.parse(retryAfter));
                long retryAfterTime2 = cal.getTimeInMillis();
                cal.setTime(dateFormat.parse((String) this.mSharedInfo.getHttpResponse().getHeader().get("Date").get(0)));
                retryAfterTime = retryAfterTime2 - cal.getTimeInMillis();
            }
            if (retryAfterTime <= 0) {
                retryAfterTime = 10;
            }
            String str = LOG_TAG;
            Log.i(str, "retry after " + retryAfterTime + " sec");
            return retryAfterTime;
        } catch (IndexOutOfBoundsException e) {
            if (e.getMessage() != null) {
                String str2 = LOG_TAG;
                Log.i(str2, "retry after related header do not exist: " + e.getMessage());
            }
            throw new InvalidHeaderException("retry after related header do not exist");
        } catch (ParseException e2) {
            if (e2.getMessage() != null) {
                String str3 = LOG_TAG;
                Log.i(str3, "retry after related header is invalid: " + e2.getMessage());
            }
            throw new InvalidHeaderException("retry after related header is invalid");
        }
    }

    public void addEventLog(String eventLog) {
        this.mEventLog.add(this.mPhoneId, eventLog);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of Workflow:");
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void createSharedInfo() {
        this.mSharedInfo = new SharedInfo(this.mContext, this.mSm, this.mRcsProfile, this.mRcsVersion, this.mClientVersion);
    }

    public IStorageAdapter getStorage() {
        return null;
    }
}
