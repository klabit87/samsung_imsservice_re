package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class WorkflowVzwMvs extends WorkflowUpBase {
    protected static final int GENERAL_ERROR_MAX_RETRY = 3;
    protected static final long[] GeneralErrorRetryTime = {0, 120000, 240000};
    protected static final String INTENT_GENERAL_ERROR_MAX_RETRY = "com.sec.internal.ims.config.workflow.general_error_max_retry";
    protected static final String LOCAL_CONFIG_BASE = "base";
    protected static final String LOCAL_CONFIG_FILE = "localconfig";
    protected static final int LOCAL_CONFIG_MAX_RETRY = 5;
    protected static final String LOCAL_CONFIG_TARGET = "vzw_up";
    protected static final int LOCAL_CONFIG_VERS = 59;
    protected static final String LOG_TAG = WorkflowVzwMvs.class.getSimpleName();
    protected static final int NO_INITIAL_DATA_MAX_RETRY = 5;
    protected int m511ResponseRetryCount = 0;
    protected String mAppToken = null;
    protected int mBackupVersion = 0;
    protected int mCurrVersion = 0;
    protected PendingIntent mGeneralErrorRetryIntent = null;
    protected final BroadcastReceiver mGeneralErrorRetryIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (WorkflowVzwMvs.INTENT_GENERAL_ERROR_MAX_RETRY.equals(intent.getAction())) {
                WorkflowVzwMvs.this.onGeneralErrorRetryTimerExpired();
            }
        }
    };
    protected WorkflowBase.Workflow mGeneralErrorRetryNextWorkflow = null;
    protected int mHttpResponse = 0;
    protected final IImsRegistrationListener mImsRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) throws RemoteException {
            WorkflowVzwMvs.this.onImsRegistrationStatusChanged(true);
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errorCode) throws RemoteException {
            WorkflowVzwMvs.this.onImsRegistrationStatusChanged(false);
        }
    };
    protected IntentFilter mIntentFilter;
    protected boolean mIs403ResponseReceived = false;
    protected boolean mIs500ResponseReceived = false;
    protected boolean mIsCleanUpOngoing = false;
    protected boolean mIsCurrConfigOngoing = false;
    protected boolean mIsDefaultSmsAppInuse = false;
    protected boolean mIsGeneralErrorRetryFailed = false;
    protected boolean mIsImsRegiNotifyReceived = false;
    protected boolean mIsImsRegiNotifyWaiting = false;
    protected boolean mIsMobileAutoConfigOngoing = false;
    protected boolean mIsSwVersionChanged = false;
    protected boolean mIsUserSwitchToNonRcsApp = false;
    protected boolean mIsUserSwitchToRcsApp = false;
    protected final ConnectivityManager.NetworkCallback mMobileStateCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            WorkflowVzwMvs.this.onMobileConnectionChanged(network, true);
        }

        public void onLost(Network network) {
            WorkflowVzwMvs.this.onMobileConnectionChanged(network, false);
        }
    };
    protected int mNewVersion = 0;
    protected int mNoAppTokenRetryCount = 0;
    protected int mNoInitialDataRetryCount = 0;
    protected int mNoResponseRetryCount = 0;
    protected int mOldVersion = 0;
    protected int mRcsDisabledStateRetryCount = 0;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowVzwMvs(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzwMvs
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterVzwMvs r7 = new com.sec.internal.ims.config.adapters.HttpAdapterVzwMvs
            r7.<init>(r14)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r12, r13)
            r0 = r15
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r10 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mOldVersion = r0
            r11.mCurrVersion = r0
            r11.mNewVersion = r0
            r11.mBackupVersion = r0
            r11.mIsCurrConfigOngoing = r0
            r11.mIsImsRegiNotifyWaiting = r0
            r11.mIsMobileAutoConfigOngoing = r0
            r11.mIsCleanUpOngoing = r0
            r1 = 0
            r11.mAppToken = r1
            r11.mGeneralErrorRetryIntent = r1
            r11.mGeneralErrorRetryNextWorkflow = r1
            r11.mIsSwVersionChanged = r0
            r11.mIsDefaultSmsAppInuse = r0
            r11.mIsUserSwitchToRcsApp = r0
            r11.mIsUserSwitchToNonRcsApp = r0
            r11.mIsImsRegiNotifyReceived = r0
            r11.mHttpResponse = r0
            r11.mNoInitialDataRetryCount = r0
            r11.mNoAppTokenRetryCount = r0
            r11.m511ResponseRetryCount = r0
            r11.mNoResponseRetryCount = r0
            r11.mRcsDisabledStateRetryCount = r0
            r11.mIs403ResponseReceived = r0
            r11.mIs500ResponseReceived = r0
            r11.mIsGeneralErrorRetryFailed = r0
            com.sec.internal.ims.config.workflow.WorkflowVzwMvs$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowVzwMvs$1
            r0.<init>()
            r11.mImsRegistrationListener = r0
            com.sec.internal.ims.config.workflow.WorkflowVzwMvs$2 r0 = new com.sec.internal.ims.config.workflow.WorkflowVzwMvs$2
            r0.<init>()
            r11.mMobileStateCallback = r0
            com.sec.internal.ims.config.workflow.WorkflowVzwMvs$3 r0 = new com.sec.internal.ims.config.workflow.WorkflowVzwMvs$3
            r0.<init>()
            r11.mGeneralErrorRetryIntentReceiver = r0
            r15.registerImsRegistrationListener()
            android.content.Context r0 = r11.mContext
            java.lang.String r1 = "connectivity"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.net.ConnectivityManager r0 = (android.net.ConnectivityManager) r0
            r11.mConnectivityManager = r0
            android.content.IntentFilter r0 = new android.content.IntentFilter
            r0.<init>()
            r11.mIntentFilter = r0
            java.lang.String r1 = "com.sec.internal.ims.config.workflow.general_error_max_retry"
            r0.addAction(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzwMvs.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    /* access modifiers changed from: protected */
    public void registerImsRegistrationListener() {
        this.mRm.registerListener(this.mImsRegistrationListener, this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public void unregisterImsRegistrationListener() {
        this.mRm.unregisterListener(this.mImsRegistrationListener, this.mPhoneId);
    }

    /* access modifiers changed from: protected */
    public void onImsRegistrationStatusChanged(boolean registered) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "onImsRegistrationStatusChanged: registered: " + registered);
        sendEmptyMessage(12);
    }

    /* access modifiers changed from: protected */
    public boolean registerMobileNetwork() {
        try {
            this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(0).setNetworkSpecifier(Integer.toString(this.mSubId)).build();
            this.mConnectivityManager.requestNetwork(this.mNetworkRequest, this.mMobileStateCallback);
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "registerMobileNetwork: registered with subId: " + this.mSubId);
            return true;
        } catch (ConnectivityManager.TooManyRequestsException | IllegalArgumentException e) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "registerMobileNetwork: can't register: " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMobileNetwork() {
        try {
            this.mConnectivityManager.unregisterNetworkCallback(this.mMobileStateCallback);
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterMobileNetwork: unregistered");
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "unregisterMobileNetwork: can't unregister: " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void onMobileConnectionChanged(Network network, boolean isAvailable) {
        if (this.mIsMobileAutoConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "onMobileConnectionChanged: ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "onMobileConnectionChanged: isAvailable: " + isAvailable);
        this.mNetwork = isAvailable ? network : null;
        if (isAvailable) {
            sendEmptyMessage(3);
        } else {
            sendEmptyMessage(4);
        }
    }

    /* access modifiers changed from: protected */
    public void registerGeneralErrorRetryIntentReceiver() {
        this.mContext.registerReceiver(this.mGeneralErrorRetryIntentReceiver, this.mIntentFilter);
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerGeneralErrorRetryIntentReceiver: registered");
    }

    /* access modifiers changed from: protected */
    public void unregisterGeneralErrorRetryIntentReceiver() {
        try {
            this.mContext.unregisterReceiver(this.mGeneralErrorRetryIntentReceiver);
            IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterGeneralErrorRetryIntentReceiver: uregistered");
        } catch (IllegalArgumentException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "unregisterGeneralErrorRetryIntentReceiver: can't unregister: " + e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    public void onGeneralErrorRetryTimerExpired() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "onGeneralErrorRetryTimerExpired");
        sendEmptyMessage(13);
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String lastSwVersion) {
        String newSwVersion = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleSwVersionChange: lastSwVersion: " + lastSwVersion + " newSwVersion: " + newSwVersion);
        if (!lastSwVersion.equals(newSwVersion)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleSwVersionChange: software version is changed");
            setLastSwVersion(newSwVersion);
            this.mIsSwVersionChanged = true;
        }
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: message: " + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 2) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleMessage: clear config info");
                clearStorage();
                this.mStartForce = true;
                return;
            } else if (i2 == 3) {
                removeMessages(4);
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: mobile connection is available");
                if (this.mIsMobileAutoConfigOngoing) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: mobile autoconfig ongoing");
                    return;
                }
                this.mPowerController.lock();
                this.mIsMobileAutoConfigOngoing = true;
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: start mobile autoconfig");
                executeAutoConfig(new Initialize());
                if (isGeneralErrorRetryTimerRunning()) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: generalErrorRetryTimer is running");
                    this.mPowerController.release();
                    return;
                }
                unregisterMobileNetwork();
                endAutoConfig();
                this.mPowerController.release();
                return;
            } else if (i2 == 4) {
                removeMessages(4);
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: mobile connection is not available");
                if (this.mIsMobileAutoConfigOngoing) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: mobile autoconfig ongoing");
                    return;
                }
                this.mPowerController.lock();
                this.mIsMobileAutoConfigOngoing = true;
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: mobile autoconfig is failed");
                unregisterMobileNetwork();
                endFailureAutoConfig();
                this.mPowerController.release();
                return;
            } else if (i2 != 5) {
                switch (i2) {
                    case 11:
                        if (this.mIsCurrConfigOngoing) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "CurrConfig: ongoing");
                            return;
                        }
                        this.mPowerController.lock();
                        executeCurrConfig();
                        endCurrConfig();
                        this.mPowerController.release();
                        return;
                    case 12:
                        this.mIsImsRegiNotifyReceived = true;
                        String str2 = LOG_TAG;
                        int i3 = this.mPhoneId;
                        IMSLog.i(str2, i3, "handleMessage: ims regi status is changed mIsImsRegiNotifyWaiting: " + this.mIsImsRegiNotifyWaiting);
                        if (this.mIsImsRegiNotifyWaiting) {
                            sendRestartAutoConfigMsg();
                            return;
                        }
                        return;
                    case 13:
                        this.mPowerController.lock();
                        removeMessages(13);
                        IMSLog.i(LOG_TAG, this.mPhoneId, "ReAutoConfig: generalErrorRetryTimer is expired");
                        stopGeneralErrorRetryTimer();
                        unregisterGeneralErrorRetryIntentReceiver();
                        IMSLog.i(LOG_TAG, this.mPhoneId, "ReAutoConfig: start generalErrorRetry");
                        executeAutoConfig(this.mGeneralErrorRetryNextWorkflow);
                        if (isGeneralErrorRetryTimerRunning()) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "ReAutoConfig: generalErrorRetryTimer is running");
                            this.mPowerController.release();
                            return;
                        }
                        IMSLog.i(LOG_TAG, this.mPhoneId, "ReAutoConfig: end generalErrorRetry");
                        unregisterMobileNetwork();
                        endAutoConfig();
                        this.mPowerController.release();
                        return;
                    case 14:
                        IMSLog.i(LOG_TAG, this.mPhoneId, "handleMessage: start cleanup");
                        unregisterImsRegistrationListener();
                        unregisterMobileNetwork();
                        stopGeneralErrorRetryTimer();
                        unregisterGeneralErrorRetryIntentReceiver();
                        setCleanUpStatus(false);
                        IMSLog.i(LOG_TAG, this.mPhoneId, "handleMessage: end cleanup");
                        return;
                    default:
                        IMSLog.i(LOG_TAG, this.mPhoneId, "handleMessage: unknown message");
                        return;
                }
            } else {
                this.mIsDefaultSmsAppInuse = isDefaultSmsAppInuse();
                String str3 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.i(str3, i4, "handleMessage: defaultSmsApp is changed mIsDefaultSmsAppInuse: " + this.mIsDefaultSmsAppInuse);
                boolean z = this.mIsDefaultSmsAppInuse;
                this.mIsUserSwitchToRcsApp = z;
                this.mIsUserSwitchToNonRcsApp = z ^ true;
                cancelValidityTimer();
                return;
            }
        }
        if (this.sIsConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: ongoing");
            return;
        }
        this.mPowerController.lock();
        String str4 = LOG_TAG;
        int i5 = this.mPhoneId;
        IMSLog.i(str4, i5, "AutoConfig: start autoconfig, mStartForce: " + this.mStartForce);
        initAutoConfig();
        if (!scheduleAutoconfig()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: scheduleAutoconfig: false");
            endAutoConfig();
        } else {
            checkAutoConfigAvailable();
        }
        this.mPowerController.release();
    }

    public void startCurrConfig() {
        if (this.mIsCurrConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "startCurrConfig: ongoing");
        } else {
            sendEmptyMessage(11);
        }
    }

    /* access modifiers changed from: protected */
    public void executeCurrConfig() {
        this.mIsCurrConfigOngoing = true;
        this.mIsImsRegiNotifyWaiting = false;
        this.mOldVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        boolean validRcsDisabledState = isValidRcsDisabledState(rcsDisabledState);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "CurrConfig: mOldVersion: " + this.mOldVersion + " rcsDisabledState: " + displayRcsDisabledStateInfo(rcsDisabledState));
        if (this.mOldVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || (isActiveVersion(this.mOldVersion) && validRcsDisabledState)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "CurrConfig: need localconfig info");
            startLocalConfig(this.mOldVersion, rcsDisabledState);
        }
        this.mNewVersion = getVersion();
    }

    /* access modifiers changed from: protected */
    public void startLocalConfig(int version, WorkflowBase.OpMode rcsDisabledState) {
        Map<String, String> parsedXml = new TreeMap<>();
        int count = 0;
        while (true) {
            if (count >= 5) {
                break;
            }
            parsedXml = loadLocalConfig();
            if (parsedXml == null) {
                count++;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "startLocalConfig: load localconfig");
                if (isValidRcsDisabledState(rcsDisabledState)) {
                    checkAndKeepRcsDisabledStateData(parsedXml, rcsDisabledState);
                }
                this.mSharedInfo.setParsedXml(parsedXml);
                clearStorage();
                this.mStorage.writeAll(parsedXml);
                if (isValidRcsDisabledState(rcsDisabledState)) {
                    setNextAutoconfigTimeFromValidity(getValidity());
                    setValidityTimer(getValidity());
                } else {
                    cancelValidityTimer();
                    setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                }
                setVersionBackup(version);
                setLastErrorCode(this.mLastErrorCodeNonRemote);
            }
        }
        if (parsedXml == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "startLocalConfig: can't load localconfig");
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x008f, code lost:
        if (r1 != null) goto L_0x0093;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.Map<java.lang.String, java.lang.String> loadLocalConfig() {
        /*
            r14 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            android.content.Context r3 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x0130 }
            android.content.res.Resources r3 = r3.getResources()     // Catch:{ JsonParseException | IOException -> 0x0130 }
            android.content.Context r4 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x0130 }
            android.content.res.Resources r4 = r4.getResources()     // Catch:{ JsonParseException | IOException -> 0x0130 }
            java.lang.String r5 = "localconfig"
            java.lang.String r6 = "raw"
            android.content.Context r7 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x0130 }
            java.lang.String r7 = r7.getPackageName()     // Catch:{ JsonParseException | IOException -> 0x0130 }
            int r4 = r4.getIdentifier(r5, r6, r7)     // Catch:{ JsonParseException | IOException -> 0x0130 }
            java.io.InputStream r3 = r3.openRawResource(r4)     // Catch:{ JsonParseException | IOException -> 0x0130 }
            com.google.gson.stream.JsonReader r4 = new com.google.gson.stream.JsonReader     // Catch:{ all -> 0x0124 }
            java.io.BufferedReader r5 = new java.io.BufferedReader     // Catch:{ all -> 0x0124 }
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ all -> 0x0124 }
            r6.<init>(r3)     // Catch:{ all -> 0x0124 }
            r5.<init>(r6)     // Catch:{ all -> 0x0124 }
            r4.<init>(r5)     // Catch:{ all -> 0x0124 }
            com.google.gson.JsonParser r5 = new com.google.gson.JsonParser     // Catch:{ all -> 0x011a }
            r5.<init>()     // Catch:{ all -> 0x011a }
            com.google.gson.JsonElement r6 = r5.parse(r4)     // Catch:{ all -> 0x011a }
            com.google.gson.JsonObject r7 = r6.getAsJsonObject()     // Catch:{ all -> 0x011a }
            java.lang.String r8 = "base"
            com.google.gson.JsonElement r7 = r7.get(r8)     // Catch:{ all -> 0x011a }
            com.google.gson.JsonObject r7 = r7.getAsJsonObject()     // Catch:{ all -> 0x011a }
            r0 = r7
            com.google.gson.JsonObject r7 = r6.getAsJsonObject()     // Catch:{ all -> 0x011a }
            java.util.Set r7 = r7.entrySet()     // Catch:{ all -> 0x011a }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ all -> 0x011a }
        L_0x0055:
            boolean r8 = r7.hasNext()     // Catch:{ all -> 0x011a }
            if (r8 == 0) goto L_0x0093
            java.lang.Object r8 = r7.next()     // Catch:{ all -> 0x011a }
            java.util.Map$Entry r8 = (java.util.Map.Entry) r8     // Catch:{ all -> 0x011a }
            java.lang.Object r9 = r8.getKey()     // Catch:{ all -> 0x011a }
            java.lang.String r9 = (java.lang.String) r9     // Catch:{ all -> 0x011a }
            java.lang.String r9 = r9.trim()     // Catch:{ all -> 0x011a }
            java.lang.String r10 = ","
            java.lang.String[] r9 = r9.split(r10)     // Catch:{ all -> 0x011a }
            int r10 = r9.length     // Catch:{ all -> 0x011a }
            r11 = 0
        L_0x0073:
            if (r11 >= r10) goto L_0x008f
            r12 = r9[r11]     // Catch:{ all -> 0x011a }
            java.lang.String r13 = "vzw_up"
            boolean r13 = r13.equals(r12)     // Catch:{ all -> 0x011a }
            if (r13 == 0) goto L_0x008c
            java.lang.Object r9 = r8.getValue()     // Catch:{ all -> 0x011a }
            com.google.gson.JsonElement r9 = (com.google.gson.JsonElement) r9     // Catch:{ all -> 0x011a }
            com.google.gson.JsonObject r9 = r9.getAsJsonObject()     // Catch:{ all -> 0x011a }
            r1 = r9
            goto L_0x008f
        L_0x008c:
            int r11 = r11 + 1
            goto L_0x0073
        L_0x008f:
            if (r1 == 0) goto L_0x0092
            goto L_0x0093
        L_0x0092:
            goto L_0x0055
        L_0x0093:
            r4.close()     // Catch:{ all -> 0x0124 }
            if (r3 == 0) goto L_0x009b
            r3.close()     // Catch:{ JsonParseException | IOException -> 0x0130 }
        L_0x009b:
            if (r0 == 0) goto L_0x0110
            if (r1 != 0) goto L_0x00a1
            goto L_0x0110
        L_0x00a1:
            java.util.TreeMap r2 = new java.util.TreeMap
            r2.<init>()
            java.util.Set r3 = r0.entrySet()
            java.util.Iterator r3 = r3.iterator()
        L_0x00ae:
            boolean r4 = r3.hasNext()
            java.lang.String r5 = "root/"
            if (r4 == 0) goto L_0x00dc
            java.lang.Object r4 = r3.next()
            java.util.Map$Entry r4 = (java.util.Map.Entry) r4
            java.lang.Object r6 = r4.getValue()
            com.google.gson.JsonElement r6 = (com.google.gson.JsonElement) r6
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r5)
            java.lang.Object r5 = r4.getKey()
            java.lang.String r5 = (java.lang.String) r5
            r7.append(r5)
            java.lang.String r5 = r7.toString()
            com.sec.internal.ims.config.workflow.WorkflowLocalFile.path(r6, r5, r2)
            goto L_0x00ae
        L_0x00dc:
            java.util.Set r3 = r1.entrySet()
            java.util.Iterator r3 = r3.iterator()
        L_0x00e4:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x010f
            java.lang.Object r4 = r3.next()
            java.util.Map$Entry r4 = (java.util.Map.Entry) r4
            java.lang.Object r6 = r4.getValue()
            com.google.gson.JsonElement r6 = (com.google.gson.JsonElement) r6
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            r7.append(r5)
            java.lang.Object r8 = r4.getKey()
            java.lang.String r8 = (java.lang.String) r8
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            com.sec.internal.ims.config.workflow.WorkflowLocalFile.path(r6, r7, r2)
            goto L_0x00e4
        L_0x010f:
            return r2
        L_0x0110:
            java.lang.String r3 = LOG_TAG
            int r4 = r14.mPhoneId
            java.lang.String r5 = "loadLocalConfig: base/target object is empty"
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            return r2
        L_0x011a:
            r5 = move-exception
            r4.close()     // Catch:{ all -> 0x011f }
            goto L_0x0123
        L_0x011f:
            r6 = move-exception
            r5.addSuppressed(r6)     // Catch:{ all -> 0x0124 }
        L_0x0123:
            throw r5     // Catch:{ all -> 0x0124 }
        L_0x0124:
            r4 = move-exception
            if (r3 == 0) goto L_0x012f
            r3.close()     // Catch:{ all -> 0x012b }
            goto L_0x012f
        L_0x012b:
            r5 = move-exception
            r4.addSuppressed(r5)     // Catch:{ JsonParseException | IOException -> 0x0130 }
        L_0x012f:
            throw r4     // Catch:{ JsonParseException | IOException -> 0x0130 }
        L_0x0130:
            r3 = move-exception
            java.lang.String r4 = LOG_TAG
            int r5 = r14.mPhoneId
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "loadLocalConfig: can't open/parse localconfig: "
            r6.append(r7)
            java.lang.String r7 = r3.getMessage()
            r6.append(r7)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r4, r5, r6)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzwMvs.loadLocalConfig():java.util.Map");
    }

    /* access modifiers changed from: protected */
    public void endCurrConfig() {
        setCompleted(true);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "CurrConfig: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "CurrConfig: mIsImsRegiNotifyReceived: " + this.mIsImsRegiNotifyReceived);
        if (this.mIsImsRegiNotifyReceived) {
            sendRestartAutoConfigMsg();
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "CurrConfig: wait until ims is registered");
            this.mIsImsRegiNotifyWaiting = true;
        }
        this.mIsCurrConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public void sendRestartAutoConfigMsg() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "CurrConfig: send complete and restart message");
        this.mModuleHandler.removeMessages(19, Integer.valueOf(this.mPhoneId));
        this.mModuleHandler.sendMessage(obtainMessage(19, this.mPhoneId, 0, (Object) null));
        unregisterImsRegistrationListener();
        this.mIsImsRegiNotifyWaiting = false;
    }

    /* access modifiers changed from: protected */
    public void startGeneralErrorRetryTimer(long time) {
        if (isGeneralErrorRetryTimerRunning()) {
            stopGeneralErrorRetryTimer();
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "startGeneralErrorRetryTimer: set retryTimer to " + time);
        this.mGeneralErrorRetryIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_GENERAL_ERROR_MAX_RETRY), 134217728);
        AlarmTimer.start(this.mContext, this.mGeneralErrorRetryIntent, time);
    }

    /* access modifiers changed from: protected */
    public void stopGeneralErrorRetryTimer() {
        if (this.mGeneralErrorRetryIntent == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "stopGeneralErrorRetryTimer: retryTimer is not running");
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "stopGeneralErrorRetryTimer: stop retryTimer");
        AlarmTimer.stop(this.mContext, this.mGeneralErrorRetryIntent);
        this.mGeneralErrorRetryIntent = null;
    }

    /* access modifiers changed from: protected */
    public boolean isGeneralErrorRetryTimerRunning() {
        return this.mGeneralErrorRetryIntent != null;
    }

    /* access modifiers changed from: protected */
    public void initAutoConfig() {
        this.sIsConfigOngoing = true;
        this.mIsMobileAutoConfigOngoing = false;
        this.mNetwork = null;
        this.mHttpResponse = 0;
        this.mGeneralErrorRetryNextWorkflow = null;
        this.mNoInitialDataRetryCount = 0;
        this.mNoAppTokenRetryCount = 0;
        this.m511ResponseRetryCount = 0;
        this.mNoResponseRetryCount = 0;
        this.mRcsDisabledStateRetryCount = 0;
        setAppToken("");
        this.mOldVersion = getVersion();
        int parsedIntVersionBackup = getParsedIntVersionBackup();
        this.mBackupVersion = parsedIntVersionBackup;
        int i = this.mOldVersion;
        if (i != 59) {
            parsedIntVersionBackup = i;
        }
        this.mCurrVersion = parsedIntVersionBackup;
        this.mIsDefaultSmsAppInuse = isDefaultSmsAppInuse();
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "initAutoConfig: mOldVersion: " + this.mOldVersion + " mBackupOldVersion: " + this.mBackupVersion + " mCurrVersion: " + this.mCurrVersion + " mIsDefaultSmsAppInuse: " + this.mIsDefaultSmsAppInuse);
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: mCurrVersion: " + this.mCurrVersion);
        boolean z = false;
        if (!needScheduleAutoconfig(this.mPhoneId)) {
            return false;
        }
        if (this.mIsSwVersionChanged && this.mIsDefaultSmsAppInuse) {
            this.mStartForce = true;
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: force autoconfig because software version is changed");
        }
        if (this.mStartForce) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: force autoconfig");
            cancelValidityTimer();
            return true;
        } else if (this.mIsUserSwitchToRcsApp || this.mIsUserSwitchToNonRcsApp) {
            if (this.mCurrVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
                z = true;
            }
            this.mStartForce = z;
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: defaultSmsApp is switched by the user need autoconfig");
            return true;
        } else if (this.mCurrVersion == WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() || this.mCurrVersion == WorkflowBase.OpMode.DISABLE.value()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: DISABLE_PERMANENTLY/DISABLE opMode skip autoconfig");
            return false;
        } else if (this.mIs403ResponseReceived || this.mIs500ResponseReceived || this.mIsGeneralErrorRetryFailed) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: mIs403ResponseReceived: " + this.mIs403ResponseReceived + " mIs500ResponseReceived: " + this.mIs500ResponseReceived + " mIsGeneralErrorRetryFailed: " + this.mIsGeneralErrorRetryFailed + " skip autoconfig");
            return false;
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            int remainValidity = (int) ((nextAutoconfigTime - new Date().getTime()) / 1000);
            IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: nextAutoconfigTime: " + nextAutoconfigTime + " remainValidity: " + remainValidity);
            if (remainValidity <= 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: validity is expired");
                if (this.mCurrVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || this.mStartForce) {
                    z = true;
                }
                this.mStartForce = z;
                return true;
            }
            if (nextAutoconfigTime > 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig: autoconfig schedule: after " + remainValidity + " seconds");
                setValidityTimer(remainValidity);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void checkAutoConfigAvailable() {
        String smsApp = Settings.Secure.getString(this.mContext.getContentResolver(), "sms_default_application");
        this.mSubId = this.mSm != null ? this.mSm.getSubscriptionId() : -1;
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkAutoConfigAvailable: isSmsAppAvailable: " + smsApp + " mSubId: " + this.mSubId);
        if (TextUtils.isEmpty(smsApp) || this.mSubId == -1 || !registerMobileNetwork()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkAutoConfigAvailable: mobile autoconfig isn't available");
            sendEmptyMessage(4);
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "checkAutoConfigAvailable: mobile autoconfig available");
        removeMessages(4);
        sendMessageDelayed(obtainMessage(4), 60000);
    }

    /* access modifiers changed from: protected */
    public void executeAutoConfig(WorkflowBase.Workflow next) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "executeAutoConfig: next: " + next);
        while (next != null) {
            try {
                if (getCleanUpStatus()) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "executeAutoConfig: cleanup is ongoing, finish");
                    next = new Finish();
                }
                next = next.run();
            } catch (NoInitialDataException e) {
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "executeAutoConfig: NoInitialDataException: " + e.getMessage());
                if (this.mNoInitialDataRetryCount < 5) {
                    String str3 = LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.i(str3, i3, "executeAutoConfig: mNoInitialDataRetryCount: " + this.mNoInitialDataRetryCount + " wait 10 seconds and retry");
                    this.mNoInitialDataRetryCount = this.mNoInitialDataRetryCount + 1;
                    sleep(10000);
                    next = new Initialize();
                } else {
                    next = new Finish();
                }
            } catch (Exception e2) {
                String str4 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.i(str4, i4, "executeAutoConfig: Exception: " + e2.getMessage());
                next = new Finish();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig() {
        this.mNewVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        boolean validRcsDisabledState = isValidRcsDisabledState(rcsDisabledState);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "endAutoConfig: end autoconfig of newVersion: " + this.mNewVersion + " validRcsDisabledState: " + validRcsDisabledState);
        if (this.mNewVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || (isActiveVersion(this.mNewVersion) && validRcsDisabledState)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "endAutoConfig: need localconfig info");
            startLocalConfig(this.mNewVersion, rcsDisabledState);
            this.mNewVersion = getVersion();
        }
        this.mStartForce = false;
        this.mIsSwVersionChanged = false;
        this.mIsUserSwitchToRcsApp = false;
        this.mIsUserSwitchToNonRcsApp = false;
        setCompleted(true);
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "endAutoConfig: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.c(LogClass.WFVM_LAST_VERSION_INFO, this.mPhoneId + "OV:" + this.mOldVersion + ",NV:" + this.mNewVersion);
        addEventLog(LOG_TAG + ": OV: " + this.mOldVersion + " NV: " + this.mNewVersion);
        String str3 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str3, i3, "endAutoConfig: rcsDisabledState: " + displayRcsDisabledStateInfo(rcsDisabledState) + " spgUrl: " + getSpgUrl() + " spgParamsUrl: " + getSpgParamsUrl());
        IMSLog.c(LogClass.WFVM_PARAM_INFO, this.mPhoneId + "DV:" + displayRcsDisabledStateInfo(rcsDisabledState) + ",SU:" + getSpgUrl() + ",SPU:" + getSpgParamsUrl());
        addEventLog(LOG_TAG + ": rcsDisabledState: " + displayRcsDisabledStateInfo(rcsDisabledState) + " spgUrl: " + getSpgUrl() + " spgParamsUrl: " + getSpgParamsUrl());
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.sIsConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public void endFailureAutoConfig() {
        this.mNewVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        boolean validRcsDisabledState = isValidRcsDisabledState(rcsDisabledState);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "endFailureAutoConfig: mNewVersion: " + this.mNewVersion + " rcsDisabledState: " + displayRcsDisabledStateInfo(rcsDisabledState));
        if (this.mNewVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || (isActiveVersion(this.mNewVersion) && validRcsDisabledState)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "endFailureAutoConfig: need localconfig info");
            startLocalConfig(this.mNewVersion, rcsDisabledState);
            this.mNewVersion = getVersion();
        }
        setCompleted(true);
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "endFailureAutoConfig: oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        ((IConfigModule) this.mModuleHandler).resetReadyStateCommand(this.mPhoneId);
        this.sIsConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public boolean isActiveVersion(int version) {
        return version >= WorkflowBase.OpMode.ACTIVE.value() && version != 59;
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultSmsAppInuse() {
        return DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1;
    }

    /* access modifiers changed from: protected */
    public boolean isValidRcsDisabledState(WorkflowBase.OpMode rcsDisabledState) {
        if (rcsDisabledState == WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE) {
            return true;
        }
        return super.isValidRcsDisabledState(rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState() {
        return super.getRcsDisabledState(ConfigConstants.CONFIGTYPE.STORAGE_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(Map<String, String> data) {
        return super.getRcsDisabledState(ConfigConstants.CONFIGTYPE.PARSEDXML_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, data);
    }

    /* access modifiers changed from: protected */
    public void setRcsDisabledState(String rcsDisabledState) {
        this.mStorage.write(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, rcsDisabledState);
    }

    /* access modifiers changed from: private */
    public synchronized String getAppToken() {
        return this.mAppToken;
    }

    /* access modifiers changed from: private */
    public synchronized void setAppToken(String appToken) {
        this.mAppToken = appToken;
    }

    /* access modifiers changed from: protected */
    public String getSpgUrl() {
        return this.mStorage.read(ConfigConstants.PATH.SPG_URL);
    }

    /* access modifiers changed from: protected */
    public String getSpgParamsUrl() {
        return this.mStorage.read(ConfigConstants.PATH.SPG_PARAMS_URL);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode convertRcsDisabledStateToOpMode(String rcsDisabledState) {
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(rcsDisabledState)) {
            return WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE;
        }
        return super.convertRcsDisabledStateToOpMode(rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public int convertRcsDisabledStateToValue(WorkflowBase.OpMode rcsDisabledState) {
        if (rcsDisabledState == WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value();
        }
        return super.convertRcsDisabledStateToValue(rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam(int version) {
        if (!this.mIsDefaultSmsAppInuse) {
            return String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value());
        }
        if (this.mStartForce) {
            return String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        }
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        if (isValidRcsDisabledState(rcsDisabledState)) {
            return String.valueOf(convertRcsDisabledStateToValue(rcsDisabledState));
        }
        return String.valueOf(version);
    }

    /* access modifiers changed from: protected */
    public String displayRcsDisabledStateInfo(WorkflowBase.OpMode rcsDisabledState) {
        int value = convertRcsDisabledStateToValue(rcsDisabledState);
        return value == WorkflowBase.OpMode.NONE.value() ? "" : String.valueOf(value);
    }

    /* access modifiers changed from: protected */
    public void checkAndKeepRcsDisabledStateData(Map<String, String> data, WorkflowBase.OpMode rcsDisabledState) {
        String curValidity = String.valueOf(getValidity());
        if (!TextUtils.isEmpty(curValidity)) {
            data.put("root/vers/validity", curValidity);
        }
        String curToken = getToken();
        if (!TextUtils.isEmpty(curToken)) {
            data.put("root/token/token", curToken);
        }
        data.put(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, String.valueOf(convertRcsDisabledStateToValue(rcsDisabledState)));
        String curSpgUrl = getSpgUrl();
        if (!TextUtils.isEmpty(curSpgUrl)) {
            data.put(ConfigConstants.PATH.SPG_URL, curSpgUrl);
        }
        String curSpgParamsUrl = getSpgParamsUrl();
        if (!TextUtils.isEmpty(curSpgParamsUrl)) {
            data.put(ConfigConstants.PATH.SPG_PARAMS_URL, curSpgParamsUrl);
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUp(WorkflowBase.Workflow init, WorkflowBase.Workflow fetchHttps, WorkflowBase.Workflow finish) throws InvalidHeaderException, UnknownStatusException {
        IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: error code: " + getLastErrorCode());
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: The server is not responding");
            int i = this.mNoResponseRetryCount;
            if (i == 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: immediately perform retry");
                this.mNoResponseRetryCount++;
                return fetchHttps;
            } else if (i < 3) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: mNoResponseRetryCount: " + this.mNoResponseRetryCount + " mNoResponseRetryTime: " + GeneralErrorRetryTime[this.mNoResponseRetryCount]);
                registerGeneralErrorRetryIntentReceiver();
                startGeneralErrorRetryTimer(GeneralErrorRetryTime[this.mNoResponseRetryCount]);
                this.mNoResponseRetryCount = this.mNoResponseRetryCount + 1;
                this.mGeneralErrorRetryNextWorkflow = fetchHttps;
                return finish;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp:  no need to retry anymore for no response");
                this.mGeneralErrorRetryNextWorkflow = null;
                this.mIsGeneralErrorRetryFailed = true;
                cancelValidityTimer();
                return finish;
            }
        } else if (lastErrorCode == 403) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: 403 response is received reset config info");
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            this.mIs403ResponseReceived = true;
            cancelValidityTimer();
            return finish;
        } else if (lastErrorCode == 500) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: internal server error");
            this.mIs500ResponseReceived = true;
            cancelValidityTimer();
            return finish;
        } else if (lastErrorCode == 503) {
            long retryAfterTime = getretryAfterTime();
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: 503 response is received retry after " + retryAfterTime + " seconds");
            sleep(1000 * retryAfterTime);
            return fetchHttps;
        } else if (lastErrorCode == 511) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: 511 response is received reset token and apptoken");
            setToken("");
            setAppToken("");
            int i2 = this.m511ResponseRetryCount;
            if (i2 == 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: immediately perform retry");
                this.m511ResponseRetryCount++;
                return fetchHttps;
            } else if (i2 < 3) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: m511ResponseRetryCount: " + this.m511ResponseRetryCount + " m511ResponseRetryTime: " + GeneralErrorRetryTime[this.m511ResponseRetryCount]);
                registerGeneralErrorRetryIntentReceiver();
                startGeneralErrorRetryTimer(GeneralErrorRetryTime[this.m511ResponseRetryCount]);
                this.m511ResponseRetryCount = this.m511ResponseRetryCount + 1;
                this.mGeneralErrorRetryNextWorkflow = fetchHttps;
                return finish;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponseForUp: no need to retry anymore for 511 response");
                this.mGeneralErrorRetryNextWorkflow = null;
                this.mIsGeneralErrorRetryFailed = true;
                cancelValidityTimer();
                return finish;
            }
        } else {
            throw new UnknownStatusException("handleResponseForUp: unknown https status code");
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowVzwMvs$4  reason: invalid class name */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        static {
            int[] iArr = new int[WorkflowBase.OpMode.values().length];
            $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = iArr;
            try {
                iArr[WorkflowBase.OpMode.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setOpMode: " + mode.name());
        switch (AnonymousClass4.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()]) {
            case 1:
                if (data != null) {
                    String str2 = LOG_TAG;
                    int i2 = this.mPhoneId;
                    IMSLog.s(str2, i2, "setOpMode: data: " + data);
                    if ((this.mCurrVersion < getVersion(data) || this.mStartForce) && this.mIsDefaultSmsAppInuse) {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "setOpMode: update the new config info");
                        clearStorage();
                        this.mStorage.writeAll(data);
                        setVersionBackup(getVersion(data));
                    } else {
                        IMSLog.i(LOG_TAG, this.mPhoneId, "setOpMode: version is not changed, maintain previous config info");
                        checkAndUpdateData(data);
                    }
                    setNextAutoconfigTimeFromValidity(getValidity());
                    return;
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "setOpMode: data is empty, remain previous data");
                return;
            case 2:
            case 3:
            case 4:
                clearStorage();
                if (data != null) {
                    this.mStorage.writeAll(data);
                }
                setVersion(mode.value());
                setValidity(mode.value());
                return;
            case 5:
                setVersion(mode.value());
                return;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                clearStorage();
                if (data != null) {
                    this.mStorage.writeAll(data);
                    return;
                }
                return;
            default:
                IMSLog.i(LOG_TAG, this.mPhoneId, "setOpMode: unknown opMode");
                return;
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> data) {
        String oldToken = getToken();
        String newToken = getToken(data);
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "checkAndUpdateData: oldToken: " + oldToken + " newToken: " + newToken);
        if (!TextUtils.isEmpty(newToken) && !newToken.equals(oldToken)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkAndUpdateData: token is changed, update it");
            setToken(newToken);
        }
        String newValidity = "";
        String oldValidity = getVersion() > 0 ? String.valueOf(getValidity()) : newValidity;
        if (getVersion(data) > 0) {
            newValidity = String.valueOf(getValidity(data));
        }
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "checkAndUpdateData: oldValidity: " + oldValidity + " newValidity: " + newValidity);
        if (!TextUtils.isEmpty(newValidity) && !newValidity.equals(oldValidity)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "checkAndUpdateData: validity is changed, update it");
            try {
                setValidity(Integer.parseInt(newValidity));
            } catch (NumberFormatException e) {
                String str3 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str3, i3, "checkAndUpdateData: skip setValidity: " + e.getMessage());
            }
        }
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup");
        setCleanUpStatus(true);
        super.cleanup();
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup: send cleanup message");
        sendEmptyMessage(14);
    }

    /* access modifiers changed from: protected */
    public synchronized boolean getCleanUpStatus() {
        return this.mIsCleanUpOngoing;
    }

    /* access modifiers changed from: protected */
    public synchronized void setCleanUpStatus(boolean status) {
        this.mIsCleanUpOngoing = status;
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setContext(this.mContext);
        this.mHttp.setNetwork(this.mNetwork);
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response response = this.mHttp.request();
        this.mHttp.close();
        return response;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    private class Initialize implements WorkflowBase.Workflow {
        private Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzwMvs.this.mHttpResponse = 0;
            WorkflowVzwMvs.this.mSharedInfo.setUrl(WorkflowVzwMvs.this.mParamHandler.initUrl());
            WorkflowVzwMvs.this.mCookieHandler.clearCookie();
            return new FetchToken();
        }
    }

    private class FetchToken implements WorkflowBase.Workflow {
        private FetchToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (TextUtils.isEmpty(WorkflowVzwMvs.this.getToken())) {
                return new FetchAppToken();
            }
            IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchToken: rcstoken is existed");
            return new FetchHttps();
        }
    }

    private class FetchAppToken implements WorkflowBase.Workflow {
        private FetchAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchAppToken: apptoken is needed");
            WorkflowVzwMvs.this.mPowerController.release();
            WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
            workflowVzwMvs.setAppToken(workflowVzwMvs.mTelephony.getAppToken(false));
            WorkflowVzwMvs.this.mPowerController.lock();
            return new AuthorizeAppToken();
        }
    }

    private class AuthorizeAppToken implements WorkflowBase.Workflow {
        private AuthorizeAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "AuthorizeAppToken: apptoken is received");
            if (TextUtils.isEmpty(WorkflowVzwMvs.this.getAppToken())) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "AuthorizeAppToken: apptoken is empty");
                return new ReFetchAppToken();
            }
            IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "AuthorizeAppToken: apptoken is existed");
            return new FetchHttps();
        }
    }

    private class ReFetchAppToken implements WorkflowBase.Workflow {
        private ReFetchAppToken() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowVzwMvs.this.mNoAppTokenRetryCount == 0) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "ReFetchAppToken: immediately perform retry");
                WorkflowVzwMvs.this.mNoAppTokenRetryCount++;
                return new FetchAppToken();
            } else if (WorkflowVzwMvs.this.mNoAppTokenRetryCount < 3) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "ReFetchAppToken: mNoAppTokenRetryCount: " + WorkflowVzwMvs.this.mNoAppTokenRetryCount + " mNoAppTokenRetryTime: " + WorkflowVzwMvs.GeneralErrorRetryTime[WorkflowVzwMvs.this.mNoAppTokenRetryCount]);
                WorkflowVzwMvs.this.registerGeneralErrorRetryIntentReceiver();
                WorkflowVzwMvs.this.startGeneralErrorRetryTimer(WorkflowVzwMvs.GeneralErrorRetryTime[WorkflowVzwMvs.this.mNoAppTokenRetryCount]);
                WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
                workflowVzwMvs.mNoAppTokenRetryCount = workflowVzwMvs.mNoAppTokenRetryCount + 1;
                WorkflowVzwMvs workflowVzwMvs2 = WorkflowVzwMvs.this;
                workflowVzwMvs2.mGeneralErrorRetryNextWorkflow = new FetchAppToken();
                return new Finish();
            } else {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "ReFetchAppToken: no need to retry anymore for no apptoken");
                WorkflowVzwMvs.this.mGeneralErrorRetryNextWorkflow = null;
                WorkflowVzwMvs.this.mIsGeneralErrorRetryFailed = true;
                WorkflowVzwMvs.this.cancelValidityTimer();
                return new Finish();
            }
        }
    }

    private class ReFetchAppTokenFor511Response implements WorkflowBase.Workflow {
        private ReFetchAppTokenFor511Response() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "ReFetchAppTokenFor511Response: apptoken is needed");
            WorkflowVzwMvs.this.mPowerController.release();
            WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
            workflowVzwMvs.setAppToken(workflowVzwMvs.mTelephony.getAppToken(WorkflowVzwMvs.this.m511ResponseRetryCount != 0));
            WorkflowVzwMvs.this.mPowerController.lock();
            return new AuthorizeAppToken();
        }
    }

    private class FetchHttps implements WorkflowBase.Workflow {
        private FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            String str;
            WorkflowVzwMvs.this.mSharedInfo.setUrl(WorkflowVzwMvs.this.mParamHandler.initUrl());
            WorkflowVzwMvs.this.mSharedInfo.setHttpClean();
            WorkflowVzwMvs.this.mSharedInfo.setHttpsDefault();
            String str2 = WorkflowVzwMvs.LOG_TAG;
            int i = WorkflowVzwMvs.this.mPhoneId;
            IMSLog.i(str2, i, "FetchHttps: mCurrVersion: " + WorkflowVzwMvs.this.mCurrVersion + " mStartForce: " + WorkflowVzwMvs.this.mStartForce);
            if (WorkflowVzwMvs.this.mStartForce) {
                WorkflowVzwMvs.this.mCurrVersion = 0;
            } else {
                WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
                workflowVzwMvs.mCurrVersion = (workflowVzwMvs.mCurrVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || WorkflowVzwMvs.this.mCurrVersion == 59) ? WorkflowBase.OpMode.DISABLE_TEMPORARY.value() : WorkflowVzwMvs.this.mCurrVersion;
            }
            String str3 = WorkflowVzwMvs.LOG_TAG;
            int i2 = WorkflowVzwMvs.this.mPhoneId;
            IMSLog.i(str3, i2, "FetchHttps: update mCurrVersion: " + WorkflowVzwMvs.this.mCurrVersion);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowVzwMvs.this.mCurrVersion));
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("IMSI", WorkflowVzwMvs.this.mTelephony.getImsi());
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowVzwMvs.this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(WorkflowVzwMvs.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowVzwMvs.this.mTelephony.getImei());
            String msisdn = WorkflowVzwMvs.this.mTelephony.getMsisdn(WorkflowVzwMvs.this.mSubId);
            if (!TextUtils.isEmpty(msisdn)) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchHttps: use msisdn from telephony");
                WorkflowVzwMvs.this.mSharedInfo.addHttpParam("msisdn", WorkflowVzwMvs.this.mParamHandler.encodeRFC3986(msisdn));
            }
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, WorkflowVzwMvs.this.mTelephony.getSmsDestPort());
            if (TextUtils.isEmpty(WorkflowVzwMvs.this.getToken())) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchHttps: rcstoken is empty");
                String str4 = WorkflowVzwMvs.LOG_TAG;
                int i3 = WorkflowVzwMvs.this.mPhoneId;
                IMSLog.s(str4, i3, "FetchHttps: use apptoken: " + WorkflowVzwMvs.this.getAppToken());
                WorkflowVzwMvs.this.mSharedInfo.addHttpParam("token", WorkflowVzwMvs.this.getAppToken());
            } else {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchHttps: rcstoken is existed");
                String str5 = WorkflowVzwMvs.LOG_TAG;
                int i4 = WorkflowVzwMvs.this.mPhoneId;
                IMSLog.s(str5, i4, "FetchHttps: use rcstoken: " + WorkflowVzwMvs.this.getToken());
                WorkflowVzwMvs.this.mSharedInfo.addHttpParam("token", WorkflowVzwMvs.this.getToken());
            }
            WorkflowVzwMvs workflowVzwMvs2 = WorkflowVzwMvs.this;
            String rcsState = workflowVzwMvs2.convertRcsStateWithSpecificParam(workflowVzwMvs2.mCurrVersion);
            String str6 = WorkflowVzwMvs.LOG_TAG;
            int i5 = WorkflowVzwMvs.this.mPhoneId;
            IMSLog.i(str6, i5, "FetchHttps: rcsState: " + rcsState);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, rcsState);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam("rcs_version", WorkflowVzwMvs.this.mRcsVersion);
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_1.0");
            WorkflowVzwMvs.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
            SharedInfo sharedInfo = WorkflowVzwMvs.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, WorkflowVzwMvs.this.mClientPlatform + WorkflowVzwMvs.this.mClientVersion);
            SharedInfo sharedInfo2 = WorkflowVzwMvs.this.mSharedInfo;
            if (WorkflowVzwMvs.this.mIsDefaultSmsAppInuse) {
                str = "1";
            } else {
                str = "2";
            }
            sharedInfo2.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, str);
            WorkflowVzwMvs.this.mSharedInfo.setHttpResponse(WorkflowVzwMvs.this.getHttpResponse());
            WorkflowVzwMvs workflowVzwMvs3 = WorkflowVzwMvs.this;
            workflowVzwMvs3.mHttpResponse = workflowVzwMvs3.mSharedInfo.getHttpResponse().getStatusCode();
            WorkflowVzwMvs workflowVzwMvs4 = WorkflowVzwMvs.this;
            workflowVzwMvs4.setLastErrorCode(workflowVzwMvs4.mSharedInfo.getHttpResponse().getStatusCode());
            String str7 = WorkflowVzwMvs.LOG_TAG;
            int i6 = WorkflowVzwMvs.this.mPhoneId;
            IMSLog.i(str7, i6, "FetchHttps: https response: " + WorkflowVzwMvs.this.mHttpResponse);
            if (WorkflowVzwMvs.this.mHttpResponse == 200) {
                if (WorkflowVzwMvs.this.mSharedInfo.getHttpResponse().getBody() != null) {
                    IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "FetchHttps: https response's body is existed");
                    return new Parse();
                }
                throw new UnknownStatusException("FetchHttps: there is no https response's body");
            } else if (WorkflowVzwMvs.this.mHttpResponse == 511) {
                WorkflowVzwMvs workflowVzwMvs5 = WorkflowVzwMvs.this;
                return workflowVzwMvs5.handleResponseForUp(new Initialize(), new ReFetchAppTokenFor511Response(), new Finish());
            } else {
                WorkflowVzwMvs workflowVzwMvs6 = WorkflowVzwMvs.this;
                return workflowVzwMvs6.handleResponseForUp(new Initialize(), new FetchHttps(), new Finish());
            }
        }
    }

    private class Parse implements WorkflowBase.Workflow {
        private Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Map<String, String> parsedXml = WorkflowVzwMvs.this.mXmlParser.parse(new String(WorkflowVzwMvs.this.mSharedInfo.getHttpResponse().getBody(), "utf-8"));
            if (parsedXml == null) {
                throw new InvalidXmlException("Parse: parsedXml is null");
            } else if (TextUtils.isEmpty(parsedXml.get("root/vers/version")) || TextUtils.isEmpty(parsedXml.get("root/vers/validity"))) {
                throw new InvalidXmlException("Parse: parsedXml is something wrong");
            } else {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Parse: parsedXml info received from the network server");
                String str = WorkflowVzwMvs.LOG_TAG;
                int i = WorkflowVzwMvs.this.mPhoneId;
                IMSLog.i(str, i, "Parse: version: " + parsedXml.get("root/vers/version") + " validity: " + parsedXml.get("root/vers/validity") + " rcsDisabledState: " + WorkflowVzwMvs.this.getRcsDisabledState(parsedXml));
                WorkflowVzwMvs.this.mParamHandler.checkSetToGS(parsedXml);
                WorkflowVzwMvs.this.mSharedInfo.setParsedXml(parsedXml);
                return new Store();
            }
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
            WorkflowBase.OpMode rcsDisabledState = workflowVzwMvs.getRcsDisabledState(workflowVzwMvs.mSharedInfo.getParsedXml());
            if (WorkflowVzwMvs.this.isValidRcsDisabledState(rcsDisabledState)) {
                IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Store: rcsDisabledState is valid, need to set the opmode");
                WorkflowVzwMvs workflowVzwMvs2 = WorkflowVzwMvs.this;
                workflowVzwMvs2.setOpMode(rcsDisabledState, workflowVzwMvs2.mSharedInfo.getParsedXml());
                if (rcsDisabledState != WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
                    IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Store: no need to retry for rcsDisabledState");
                    return new Finish();
                } else if (WorkflowVzwMvs.this.mRcsDisabledStateRetryCount == 0) {
                    IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Store: immediately perform retry");
                    WorkflowVzwMvs.this.mRcsDisabledStateRetryCount++;
                    return new FetchHttps();
                } else if (WorkflowVzwMvs.this.mRcsDisabledStateRetryCount < 3) {
                    IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Store: mRcsDisabledStateRetryCount: " + WorkflowVzwMvs.this.mRcsDisabledStateRetryCount + " mRcsDisabledStateRetryTime: " + WorkflowVzwMvs.GeneralErrorRetryTime[WorkflowVzwMvs.this.mRcsDisabledStateRetryCount]);
                    WorkflowVzwMvs.this.registerGeneralErrorRetryIntentReceiver();
                    WorkflowVzwMvs.this.startGeneralErrorRetryTimer(WorkflowVzwMvs.GeneralErrorRetryTime[WorkflowVzwMvs.this.mRcsDisabledStateRetryCount]);
                    WorkflowVzwMvs workflowVzwMvs3 = WorkflowVzwMvs.this;
                    workflowVzwMvs3.mRcsDisabledStateRetryCount = workflowVzwMvs3.mRcsDisabledStateRetryCount + 1;
                    WorkflowVzwMvs workflowVzwMvs4 = WorkflowVzwMvs.this;
                    workflowVzwMvs4.mGeneralErrorRetryNextWorkflow = new FetchHttps();
                    return new Finish();
                } else {
                    IMSLog.i(WorkflowVzwMvs.LOG_TAG, WorkflowVzwMvs.this.mPhoneId, "Store: no need to retry anymore for rcsDisabledState");
                    WorkflowVzwMvs.this.mGeneralErrorRetryNextWorkflow = null;
                    return new Finish();
                }
            } else {
                WorkflowVzwMvs workflowVzwMvs5 = WorkflowVzwMvs.this;
                workflowVzwMvs5.setOpMode(workflowVzwMvs5.getOpMode(workflowVzwMvs5.mSharedInfo.getParsedXml()), WorkflowVzwMvs.this.mSharedInfo.getParsedXml());
                if (WorkflowVzwMvs.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                    WorkflowVzwMvs workflowVzwMvs6 = WorkflowVzwMvs.this;
                    workflowVzwMvs6.setValidityTimer(workflowVzwMvs6.getValidity());
                }
                return new Finish();
            }
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzwMvs workflowVzwMvs = WorkflowVzwMvs.this;
            workflowVzwMvs.setLastErrorCode(workflowVzwMvs.mSharedInfo.getHttpResponse() != null ? WorkflowVzwMvs.this.mSharedInfo.getHttpResponse().getStatusCode() : IWorkflow.DEFAULT_ERROR_CODE);
            String str = WorkflowVzwMvs.LOG_TAG;
            int i = WorkflowVzwMvs.this.mPhoneId;
            IMSLog.i(str, i, "Finish: lastErrorCode: " + WorkflowVzwMvs.this.getLastErrorCode());
            return null;
        }
    }
}
