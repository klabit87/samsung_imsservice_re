package com.sec.internal.ims.config.workflow;

import android.database.sqlite.SQLiteFullException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorkflowVzw extends WorkflowUpBase {
    protected static final int BACKOFF_MAX_RETRY = 5;
    protected static final String LOCAL_CONFIG_BASE = "base";
    protected static final String LOCAL_CONFIG_FILE = "localconfig";
    protected static final int LOCAL_CONFIG_MAX_COUNT = 5;
    protected static final String LOCAL_CONFIG_TARGET = "vzw_up";
    protected static final int LOCAL_CONFIG_VERS = 59;
    protected static final String LOG_TAG = WorkflowVzw.class.getSimpleName();
    private static final String STANDARD_IMSI_NAI_TEMPLATE = "0<imsi>@nai.epc.mnc<mnc>.mcc<mcc>.3gppnetwork.org";
    protected static final int WORKFLOW_MAX_COUNT = 50;
    protected static final long[] backOffRetryTime = {UtStateMachine.HTTP_READ_TIMEOUT_GCF, 4000, 6000, 8000, 10000};
    protected int m511BackOffRetryCount = 0;
    protected String mAkaResponse = null;
    protected int mBackOffRetryCount = 0;
    protected int mBackupVersion = 0;
    protected int mHttpResponse = 0;
    protected boolean mIsClearedConfigInfo = false;
    protected boolean mIsCurConfigOngoing = false;
    protected boolean mIsMobileConfigCompleted = false;
    protected boolean mIsMobileConnected = false;
    protected boolean mIsMobileRequested = false;
    protected boolean mIsReceived403Err = false;
    protected boolean mIsReceived500Err = false;
    protected boolean mIsReceivedMinusVerison = false;
    protected boolean mIsSmsDefaultApp = false;
    protected int mMinusVersion = 0;
    private final ConnectivityManager.NetworkCallback mMobileStateCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            WorkflowVzw.this.onMobileConnectionChanged(network, true);
        }

        public void onLost(Network network) {
            WorkflowVzw.this.onMobileConnectionChanged(network, false);
        }
    };
    protected int mNewVersion = 0;
    protected int mOldVersion = 0;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowVzw(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceVzw
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterVzw r7 = new com.sec.internal.ims.config.adapters.HttpAdapterVzw
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
            r11.mNewVersion = r0
            r11.mBackupVersion = r0
            r11.mMinusVersion = r0
            r11.mIsMobileRequested = r0
            r11.mIsMobileConnected = r0
            r11.mIsMobileConfigCompleted = r0
            r11.mIsClearedConfigInfo = r0
            r11.mIsCurConfigOngoing = r0
            r11.mIsReceivedMinusVerison = r0
            r11.mIsReceived403Err = r0
            r11.mIsReceived500Err = r0
            r11.mIsSmsDefaultApp = r0
            r11.mHttpResponse = r0
            r1 = 0
            r11.mAkaResponse = r1
            r11.mBackOffRetryCount = r0
            r11.m511BackOffRetryCount = r0
            com.sec.internal.ims.config.workflow.WorkflowVzw$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowVzw$1
            r0.<init>()
            r11.mMobileStateCallback = r0
            android.content.Context r0 = r11.mContext
            java.lang.String r1 = "connectivity"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.net.ConnectivityManager r0 = (android.net.ConnectivityManager) r0
            r11.mConnectivityManager = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzw.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "handleMessage: " + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 2) {
                IMSLog.d(LOG_TAG, this.mPhoneId, "clear storage, force config is needed");
                clearStorageforVZW("");
                resetConfigInfo();
                this.mIsClearedConfigInfo = true;
                return;
            } else if (i2 == 3) {
                removeMessages(4);
                if (!this.mIsMobileConfigCompleted) {
                    this.mPowerController.lock();
                    executeAutoConfig();
                    this.mIsMobileConfigCompleted = true;
                    unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                    endAutoConfig();
                    String str2 = LOG_TAG;
                    int i3 = this.mPhoneId;
                    IMSLog.d(str2, i3, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                    IMSLog.d(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
                    this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                    this.mPowerController.release();
                    return;
                }
                return;
            } else if (i2 != 4) {
                if (i2 != 5) {
                    if (i2 != 11) {
                        Log.e(LOG_TAG, "unknown message");
                        return;
                    } else if (!this.mIsCurConfigOngoing) {
                        this.mIsCurConfigOngoing = true;
                        Log.d(LOG_TAG, "start cur config");
                        this.mPowerController.lock();
                        executeCurConfig();
                        setCompleted(true);
                        setLastSwVersion(ConfigConstants.BUILD.TERMINAL_SW_VERSION);
                        this.mIsCurConfigOngoing = false;
                        Log.d(LOG_TAG, "end cur config");
                        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                        Log.d(LOG_TAG, "send HANDLE_AUTO_CONFIG_RESTART after cur config");
                        this.mModuleHandler.removeMessages(19, Integer.valueOf(this.mPhoneId));
                        this.mModuleHandler.sendMessage(obtainMessage(19, this.mPhoneId, 0, (Object) null));
                        this.mPowerController.release();
                        return;
                    } else {
                        return;
                    }
                } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                    IMSLog.d(LOG_TAG, this.mPhoneId, "sms default application is changed to samsung");
                    setOpMode(WorkflowBase.OpMode.ENABLE_RCS_BY_USER, (Map<String, String>) null);
                    return;
                } else {
                    IMSLog.d(LOG_TAG, this.mPhoneId, "sms default application is changed to non-samsung");
                    setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                    return;
                }
            } else if (!this.mIsMobileConfigCompleted) {
                this.mPowerController.lock();
                unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                endAutoConfig();
                String str3 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.d(str3, i4, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                IMSLog.d(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
                this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                this.mPowerController.release();
                return;
            } else {
                return;
            }
        }
        if (this.sIsConfigOngoing) {
            Log.d(LOG_TAG, "AutoConfig: ongoing");
            return;
        }
        this.sIsConfigOngoing = true;
        String str4 = LOG_TAG;
        int i5 = this.mPhoneId;
        IMSLog.d(str4, i5, "AutoConfig: start, mStartForce: " + this.mStartForce);
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mPowerController.lock();
        initAutoConfig();
        if (scheduleAutoconfigForVZW()) {
            Log.d(LOG_TAG, "use mobile network with mms type");
            this.mIsMobileRequested = true;
            this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(0).build();
            registerMobileNetwork(this.mConnectivityManager, this.mNetworkRequest, this.mMobileStateCallback);
            removeMessages(4);
            sendMessageDelayed(obtainMessage(4), 60000);
            this.mPowerController.release();
            return;
        }
        endAutoConfig();
        String str5 = LOG_TAG;
        int i6 = this.mPhoneId;
        IMSLog.d(str5, i6, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.d(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mPowerController.release();
    }

    public void onMobileConnectionChanged(Network network, boolean isAvailable) {
        if (isAvailable) {
            if (this.mIsMobileRequested && !this.mIsMobileConnected) {
                Log.d(LOG_TAG, "onMobileConnectionChanged: onAvailable");
                if (network != null) {
                    Log.d(LOG_TAG, "mobile connection is successful");
                    this.mNetwork = network;
                    this.mIsMobileConnected = true;
                    sendEmptyMessage(3);
                    return;
                }
                Log.d(LOG_TAG, "mobile connection info is empty");
            }
        } else if (this.mIsMobileRequested) {
            Log.d(LOG_TAG, "onMobileConnectionChanged: onLost");
            this.mIsMobileConnected = false;
        }
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String lastSwVersion) {
        Log.d(LOG_TAG, "handleSwVersionChange");
        String newSwVersion = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        String str = LOG_TAG;
        Log.d(str, "oldSwVersion: " + lastSwVersion + " newSwVersion: " + newSwVersion);
        if (lastSwVersion != null && !lastSwVersion.equals(newSwVersion)) {
            int curVersion = getVersion();
            String str2 = LOG_TAG;
            Log.d(str2, "curVersion: " + curVersion);
            if (curVersion == 59) {
                curVersion = Integer.parseInt(getVersionBackup());
                String str3 = LOG_TAG;
                Log.d(str3, "set curVersion as backupVersion: " + curVersion);
            }
            if (curVersion != WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() && curVersion != WorkflowBase.OpMode.DISABLE.value()) {
                Log.d(LOG_TAG, "FOTA upgrade happened: force autoconfig");
                resetConfigInfo();
            }
        }
    }

    public void startCurrConfig() {
        if (this.mIsCurConfigOngoing) {
            Log.d(LOG_TAG, "startCurrConfig: ongoing");
        } else {
            sendEmptyMessage(11);
        }
    }

    /* access modifiers changed from: protected */
    public void clearStorageforVZW(String defaultVersion) {
        this.mStorage.deleteAll();
        checkStorageforVZW(defaultVersion);
    }

    /* access modifiers changed from: protected */
    public void checkStorageforVZW(String defaultVersion) {
        String str = LOG_TAG;
        Log.d(str, "checkStorageforVZW: default version:" + defaultVersion);
        List<String> defaultList = new ArrayList<>();
        for (Map.Entry<String, String> item : ConfigContract.STORAGE_DEFAULT.entrySet()) {
            if (this.mStorage.read(item.getKey()) == null) {
                defaultList.add(item.getKey());
                if (!"root/vers/version".equals(item.getKey()) || TextUtils.isEmpty(defaultVersion)) {
                    this.mStorage.write(item.getKey(), item.getValue());
                } else {
                    this.mStorage.write(item.getKey(), defaultVersion);
                }
            }
        }
        int size = defaultList.size();
        if (size > 0) {
            String str2 = LOG_TAG;
            Log.d(str2, "checkStorage: Default set(" + size + "): " + defaultList);
        }
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfigForVZW() {
        int curVersion = this.mOldVersion;
        if (curVersion == 59) {
            curVersion = this.mBackupVersion;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "scheduleAutoconfigForVZW: curVersion: " + curVersion);
        if (!needScheduleAutoconfig(this.mPhoneId)) {
            Log.d(LOG_TAG, "needScheduleAutoconfig: false");
            return false;
        } else if (this.mStartForce) {
            cancelValidityTimer();
            setAkaResponse("");
            Log.d(LOG_TAG, "force autoconfig");
            return true;
        } else if (curVersion == WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() || ((curVersion == WorkflowBase.OpMode.DISABLE.value() && !String.valueOf(WorkflowBase.OpMode.ENABLE_RCS_BY_USER.value()).equals(getRcsState())) || (curVersion == WorkflowBase.OpMode.DORMANT.value() && String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState())))) {
            String str2 = LOG_TAG;
            Log.d(str2, "curVersion: " + curVersion + " skip autoconfig");
            return false;
        } else if (this.mIsReceived403Err || this.mIsReceived500Err) {
            String str3 = LOG_TAG;
            Log.d(str3, "mIsReceived403Err: " + this.mIsReceived403Err + " mIsReceived500Err: " + this.mIsReceived500Err + " skip autoconfig");
            return false;
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            String str4 = LOG_TAG;
            Log.d(str4, "nextAutoconfigTime: " + nextAutoconfigTime);
            int remainValidity = (int) ((nextAutoconfigTime - new Date().getTime()) / 1000);
            String str5 = LOG_TAG;
            Log.d(str5, "remainValidity: " + remainValidity);
            if (remainValidity <= 0) {
                Log.d(LOG_TAG, "need autoconfig");
                if (curVersion == WorkflowBase.OpMode.DISABLE.value() || curVersion == WorkflowBase.OpMode.DORMANT.value()) {
                    String str6 = LOG_TAG;
                    Log.d(str6, "reset config info, curVersion: " + curVersion);
                    resetConfigInfo();
                }
                return true;
            }
            if (nextAutoconfigTime > 0) {
                String str7 = LOG_TAG;
                Log.d(str7, "autoconfig schedule: after " + remainValidity + " seconds");
                setValidityTimer(remainValidity);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void resetConfigInfo() {
        IMSLog.d(LOG_TAG, this.mPhoneId, "resetConfigInfo");
        this.mIsSmsDefaultApp = DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1;
        String str = LOG_TAG;
        Log.d(str, "resetConfigInfo: mIsSmsDefaultApp: " + this.mIsSmsDefaultApp);
        if (this.mIsSmsDefaultApp) {
            this.mStartForce = true;
            setVersionBackup(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
            setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
            setRcsDisabledState("");
            setAkaResponse("");
            cancelValidityTimer();
            setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        }
    }

    /* access modifiers changed from: protected */
    public void initAutoConfig() {
        IMSLog.d(LOG_TAG, this.mPhoneId, "initAutoConfig");
        this.mNetwork = null;
        this.mHttp.setNetwork((Network) null);
        boolean z = false;
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsMobileConfigCompleted = false;
        this.mBackOffRetryCount = 0;
        this.m511BackOffRetryCount = 0;
        this.mMinusVersion = 0;
        this.mIsReceivedMinusVerison = false;
        this.mIsClearedConfigInfo = false;
        this.mOldVersion = getVersion();
        this.mBackupVersion = Integer.parseInt(getVersionBackup());
        if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
            z = true;
        }
        this.mIsSmsDefaultApp = z;
        if (!z) {
            if (Settings.Secure.getString(this.mContext.getContentResolver(), "sms_default_application") == null) {
                Log.d(LOG_TAG, "smsApplication is null from Settings, reset rcsState");
                setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
            } else {
                Log.d(LOG_TAG, "smsApplication is non-samsung from Settings, set rcsState as non-samsung");
                setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
            }
        } else if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState())) {
            Log.d(LOG_TAG, "smsApplication is samsung but rcsState is invalid, reset rcsState");
            setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
        }
        Log.d(LOG_TAG, "curOldVersion: " + this.mOldVersion + " curBackupVersion: " + this.mBackupVersion + " mIsReceived403Err: " + this.mIsReceived403Err + " mIsReceived500Err: " + this.mIsReceived500Err + " mIsSmsDefaultApp: " + this.mIsSmsDefaultApp + " rcsState: " + getRcsState());
        if (this.mOldVersion == 59) {
            Log.d(LOG_TAG, "local config used, use backupversion");
            if (((this.mBackupVersion == WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() || this.mBackupVersion == WorkflowBase.OpMode.DISABLE.value() || this.mBackupVersion == WorkflowBase.OpMode.DORMANT.value() || isValidRcsDisabledState(getRcsDisabledState()) || this.mIsReceived403Err || this.mIsReceived500Err) && !this.mStartForce) || String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState())) {
                Log.d(LOG_TAG, "maintain config info");
                return;
            }
            Log.d(LOG_TAG, "reset config info");
            resetConfigInfo();
        }
    }

    /* access modifiers changed from: protected */
    public void executeAutoConfig() {
        Log.d(LOG_TAG, "executeAutoConfig");
        work();
    }

    /* access modifiers changed from: protected */
    public void executeCurConfig() {
        Log.d(LOG_TAG, "executeCurConfig");
        this.mOldVersion = getVersion();
        String str = LOG_TAG;
        Log.d(str, "oldVersion: " + this.mOldVersion);
        if (this.mOldVersion >= WorkflowBase.OpMode.ACTIVE.value()) {
            Log.d(LOG_TAG, "maintain cur config info and need to send complete msg");
            return;
        }
        if (this.mOldVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
            Log.d(LOG_TAG, "need force local config info");
            startLocalConfig(true);
            resetConfigInfo();
        } else if (this.mOldVersion == 59 || !isValidRcsDisabledState(getRcsDisabledState())) {
            Log.d(LOG_TAG, "maintain cur config info and need to send complete msg");
        } else {
            Log.d(LOG_TAG, "need local config info for rcsDisabledState");
            this.mNewVersion = this.mOldVersion;
            startLocalConfig(false);
            setVersionBackup(this.mNewVersion);
        }
        this.mNewVersion = getVersion();
        this.mIsClearedConfigInfo = false;
        String str2 = LOG_TAG;
        Log.d(str2, "newVersion: " + this.mNewVersion + " backupVersion: " + getVersionBackup());
    }

    /* access modifiers changed from: protected */
    public boolean startLocalConfig(boolean isForceLocalConfig) {
        IMSLog.d(LOG_TAG, this.mPhoneId, "startLocalConfig: isForceLocalConfig: " + isForceLocalConfig + " mIsClearedConfigInfo: " + this.mIsClearedConfigInfo);
        boolean result = false;
        int count = 5;
        Map<String, String> parsedXml = new TreeMap<>();
        while (true) {
            if (count <= 0) {
                break;
            }
            parsedXml = loadLocalConfig();
            if (parsedXml != null) {
                Log.d(LOG_TAG, "load localconfig data successfully");
                this.mSharedInfo.setParsedXml(parsedXml);
                if (this.mSharedInfo.getHttpResponse() != null) {
                    this.mSharedInfo.getHttpResponse().setStatusCode(200);
                }
                if (isForceLocalConfig) {
                    if (this.mIsClearedConfigInfo) {
                        setVersion(59);
                    } else {
                        clearStorageforVZW(String.valueOf(59));
                    }
                    this.mStorage.writeAll(parsedXml);
                } else {
                    checkAndKeepData(parsedXml);
                }
                setNextAutoconfigTimeFromValidity(getValidity());
                setValidityTimer(getValidity());
                result = true;
            } else {
                count--;
            }
        }
        if (parsedXml == null) {
            Log.d(LOG_TAG, "cannot load localconfig data");
            resetConfigInfo();
        }
        return result;
    }

    /* Debug info: failed to restart local var, previous not found, register: 14 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0096, code lost:
        if (r1 != null) goto L_0x009a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.Map<java.lang.String, java.lang.String> loadLocalConfig() {
        /*
            r14 = this;
            r0 = 0
            r1 = 0
            r2 = 0
            android.content.Context r3 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x013c }
            android.content.res.Resources r3 = r3.getResources()     // Catch:{ JsonParseException | IOException -> 0x013c }
            android.content.Context r4 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x013c }
            android.content.res.Resources r4 = r4.getResources()     // Catch:{ JsonParseException | IOException -> 0x013c }
            java.lang.String r5 = "localconfig"
            java.lang.String r6 = "raw"
            android.content.Context r7 = r14.mContext     // Catch:{ JsonParseException | IOException -> 0x013c }
            java.lang.String r7 = r7.getPackageName()     // Catch:{ JsonParseException | IOException -> 0x013c }
            int r4 = r4.getIdentifier(r5, r6, r7)     // Catch:{ JsonParseException | IOException -> 0x013c }
            java.io.InputStream r3 = r3.openRawResource(r4)     // Catch:{ JsonParseException | IOException -> 0x013c }
            com.google.gson.stream.JsonReader r4 = new com.google.gson.stream.JsonReader     // Catch:{ all -> 0x0130 }
            java.io.BufferedReader r5 = new java.io.BufferedReader     // Catch:{ all -> 0x0130 }
            java.io.InputStreamReader r6 = new java.io.InputStreamReader     // Catch:{ all -> 0x0130 }
            r6.<init>(r3)     // Catch:{ all -> 0x0130 }
            r5.<init>(r6)     // Catch:{ all -> 0x0130 }
            r4.<init>(r5)     // Catch:{ all -> 0x0130 }
            java.lang.String r5 = LOG_TAG     // Catch:{ all -> 0x0126 }
            java.lang.String r6 = "loadLocalConfig: open/parse localconfig"
            android.util.Log.d(r5, r6)     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonParser r5 = new com.google.gson.JsonParser     // Catch:{ all -> 0x0126 }
            r5.<init>()     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonElement r6 = r5.parse(r4)     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonObject r7 = r6.getAsJsonObject()     // Catch:{ all -> 0x0126 }
            java.lang.String r8 = "base"
            com.google.gson.JsonElement r7 = r7.get(r8)     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonObject r7 = r7.getAsJsonObject()     // Catch:{ all -> 0x0126 }
            r0 = r7
            com.google.gson.JsonObject r7 = r6.getAsJsonObject()     // Catch:{ all -> 0x0126 }
            java.util.Set r7 = r7.entrySet()     // Catch:{ all -> 0x0126 }
            java.util.Iterator r7 = r7.iterator()     // Catch:{ all -> 0x0126 }
        L_0x005c:
            boolean r8 = r7.hasNext()     // Catch:{ all -> 0x0126 }
            if (r8 == 0) goto L_0x009a
            java.lang.Object r8 = r7.next()     // Catch:{ all -> 0x0126 }
            java.util.Map$Entry r8 = (java.util.Map.Entry) r8     // Catch:{ all -> 0x0126 }
            java.lang.Object r9 = r8.getKey()     // Catch:{ all -> 0x0126 }
            java.lang.String r9 = (java.lang.String) r9     // Catch:{ all -> 0x0126 }
            java.lang.String r9 = r9.trim()     // Catch:{ all -> 0x0126 }
            java.lang.String r10 = ","
            java.lang.String[] r9 = r9.split(r10)     // Catch:{ all -> 0x0126 }
            int r10 = r9.length     // Catch:{ all -> 0x0126 }
            r11 = 0
        L_0x007a:
            if (r11 >= r10) goto L_0x0096
            r12 = r9[r11]     // Catch:{ all -> 0x0126 }
            java.lang.String r13 = "vzw_up"
            boolean r13 = r13.equals(r12)     // Catch:{ all -> 0x0126 }
            if (r13 == 0) goto L_0x0093
            java.lang.Object r9 = r8.getValue()     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonElement r9 = (com.google.gson.JsonElement) r9     // Catch:{ all -> 0x0126 }
            com.google.gson.JsonObject r9 = r9.getAsJsonObject()     // Catch:{ all -> 0x0126 }
            r1 = r9
            goto L_0x0096
        L_0x0093:
            int r11 = r11 + 1
            goto L_0x007a
        L_0x0096:
            if (r1 == 0) goto L_0x0099
            goto L_0x009a
        L_0x0099:
            goto L_0x005c
        L_0x009a:
            r4.close()     // Catch:{ all -> 0x0130 }
            if (r3 == 0) goto L_0x00a2
            r3.close()     // Catch:{ JsonParseException | IOException -> 0x013c }
        L_0x00a2:
            if (r0 == 0) goto L_0x011e
            if (r1 != 0) goto L_0x00a8
            goto L_0x011e
        L_0x00a8:
            java.util.TreeMap r2 = new java.util.TreeMap
            r2.<init>()
            java.util.Set r3 = r0.entrySet()
            java.util.Iterator r3 = r3.iterator()
        L_0x00b5:
            boolean r4 = r3.hasNext()
            java.lang.String r5 = "root/"
            if (r4 == 0) goto L_0x00e3
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
            goto L_0x00b5
        L_0x00e3:
            java.util.Set r3 = r1.entrySet()
            java.util.Iterator r3 = r3.iterator()
        L_0x00eb:
            boolean r4 = r3.hasNext()
            if (r4 == 0) goto L_0x0116
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
            goto L_0x00eb
        L_0x0116:
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "loadLocalConfig: return localconfig data"
            android.util.Log.d(r3, r4)
            return r2
        L_0x011e:
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "loadLocalConfig: base/target object is empty"
            android.util.Log.e(r3, r4)
            return r2
        L_0x0126:
            r5 = move-exception
            r4.close()     // Catch:{ all -> 0x012b }
            goto L_0x012f
        L_0x012b:
            r6 = move-exception
            r5.addSuppressed(r6)     // Catch:{ all -> 0x0130 }
        L_0x012f:
            throw r5     // Catch:{ all -> 0x0130 }
        L_0x0130:
            r4 = move-exception
            if (r3 == 0) goto L_0x013b
            r3.close()     // Catch:{ all -> 0x0137 }
            goto L_0x013b
        L_0x0137:
            r5 = move-exception
            r4.addSuppressed(r5)     // Catch:{ JsonParseException | IOException -> 0x013c }
        L_0x013b:
            throw r4     // Catch:{ JsonParseException | IOException -> 0x013c }
        L_0x013c:
            r3 = move-exception
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "loadLocalConfig: can't open/parse localconfig"
            android.util.Log.e(r4, r5)
            r3.printStackTrace()
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowVzw.loadLocalConfig():java.util.Map");
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
    public void checkAndKeepData(Map<String, String> data) {
        if (this.mNewVersion == WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || this.mNewVersion == WorkflowBase.OpMode.DISABLE_PERMANENTLY.value() || this.mNewVersion == WorkflowBase.OpMode.DISABLE.value()) {
            Log.d(LOG_TAG, "checkAndKeepData: use local config data");
            this.mStorage.writeAll(data);
            return;
        }
        String curToken = getToken();
        String curValidity = String.valueOf(getValidity());
        String str = LOG_TAG;
        IMSLog.s(str, "checkAndKeepData: curToken: " + curToken + " curValidity: " + curValidity);
        if (!TextUtils.isEmpty(curToken)) {
            Log.d(LOG_TAG, "checkAndKeepData: keep the token from the network");
            data.put("root/token/token", curToken);
        }
        if (!TextUtils.isEmpty(curValidity)) {
            Log.d(LOG_TAG, "checkAndKeepData: keep the validity from the network");
            data.put("root/vers/validity", curValidity);
        }
        String curSpgUrl = getSpgUrl();
        String curSpgParamsUrl = getSpgParamsUrl();
        String str2 = LOG_TAG;
        IMSLog.s(str2, "checkAndKeepData: curSpgUrl: " + curSpgUrl + " curSpgParamsUrl: " + curSpgParamsUrl);
        if (!TextUtils.isEmpty(curSpgUrl)) {
            Log.d(LOG_TAG, "checkAndKeepData: keep the spgUrl from the network");
            data.put(ConfigConstants.PATH.SPG_URL, curSpgUrl);
        }
        if (!TextUtils.isEmpty(curSpgParamsUrl)) {
            Log.d(LOG_TAG, "checkAndKeepData: keep the spgParamsUrl from the network");
            data.put(ConfigConstants.PATH.SPG_PARAMS_URL, curSpgParamsUrl);
        }
        this.mStorage.writeAll(data);
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig() {
        IMSLog.d(LOG_TAG, this.mPhoneId, "endAutoConfig");
        boolean result = true;
        this.mNewVersion = getVersion();
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        String rcsstate = getRcsState();
        String str = LOG_TAG;
        Log.d(str, "curOldVersion: " + this.mOldVersion + " curNewVersion: " + this.mNewVersion + " rcsState: " + rcsstate + " mIsReceivedMinusVerison: " + this.mIsReceivedMinusVerison + " mMinusVersion: " + this.mMinusVersion + " backupVersion: " + getVersionBackup());
        if (this.mIsReceivedMinusVerison) {
            this.mNewVersion = this.mMinusVersion;
            String str2 = LOG_TAG;
            Log.d(str2, "set curNewVersion as mMinusVersion: " + this.mNewVersion);
        }
        IMSLog.c(LogClass.WFV_VERSION_INFO, this.mPhoneId + ",OV:" + this.mOldVersion + ",RV:" + this.mNewVersion + ",BV:" + getVersionBackup() + ",DV:" + convertRcsDisabledStateToValue(rcsDisabledState));
        if (this.mNewVersion <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || isValidRcsDisabledState(rcsDisabledState)) {
            Log.d(LOG_TAG, "need to use local config info");
            result = startLocalConfig(false);
            if (this.mNewVersion != 59) {
                String str3 = LOG_TAG;
                Log.d(str3, "set versionbackup as mNewVersion: " + this.mNewVersion);
                setVersionBackup(this.mNewVersion);
            }
            this.mNewVersion = getVersion();
            String str4 = LOG_TAG;
            Log.d(str4, "set mNewVersion after local config: " + this.mNewVersion);
        }
        IMSLog.c(LogClass.WFV_LAST_VERSION_INFO, this.mPhoneId + ",OV:" + this.mOldVersion + ",NV:" + this.mNewVersion + ",BV:" + getVersionBackup());
        IMSLog.c(LogClass.WFV_PARAM_INFO, this.mPhoneId + "RDS:" + convertRcsDisabledStateToValue(rcsDisabledState) + ",SU:" + getSpgUrl() + ",SPU:" + getSpgParamsUrl());
        addEventLog(LOG_TAG + ": RcsDisabledState:" + convertRcsDisabledStateToValue(rcsDisabledState) + ",SpgUrl:" + getSpgUrl() + ",SpgParamsUrl:" + getSpgParamsUrl());
        setCompleted(true);
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsReceivedMinusVerison = false;
        this.mIsClearedConfigInfo = false;
        this.mStartForce = result ^ true;
        this.sIsConfigOngoing = false;
        setLastSwVersion(ConfigConstants.BUILD.TERMINAL_SW_VERSION);
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize();
        int count = getFlowCount();
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.e(LOG_TAG, "NoInitialDataException: " + e.getMessage());
                Log.e(LOG_TAG, "wait 10 sec and retry");
                sleep(10000);
                next = new Initialize();
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                String message = e2.getMessage();
                Log.e(LOG_TAG, "UnknownStatusException: " + message);
                Log.e(LOG_TAG, "wait 2 sec and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = new Initialize();
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.e(LOG_TAG, "SQLiteFullException occur: " + e3.getMessage());
                next = new Finish();
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.e(LOG_TAG, "unknown exception occur: " + e4.getMessage());
                }
                Log.e(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                next = new Initialize();
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    public int getFlowCount() {
        return 50;
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        Log.d(LOG_TAG, "getHttpResponse");
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setContext(this.mContext);
        this.mHttp.setNetwork(this.mNetwork);
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response response = this.mHttp.request();
        this.mHttp.close();
        return response;
    }

    /* access modifiers changed from: private */
    public synchronized String getAkaResponse() {
        return this.mAkaResponse;
    }

    /* access modifiers changed from: private */
    public synchronized void setAkaResponse(String response) {
        this.mAkaResponse = response;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState() {
        return getRcsDisabledState(ConfigConstants.CONFIGTYPE.STORAGE_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(Map<String, String> data) {
        return getRcsDisabledState(ConfigConstants.CONFIGTYPE.PARSEDXML_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, data);
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
    public void setRcsDisabledState(String rcsDisabledState) {
        this.mStorage.write(ConfigConstants.PATH.RCS_DISABLED_STATE_FOR_VZW, rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public boolean isValidRcsDisabledState(WorkflowBase.OpMode rcsDisabledState) {
        if (rcsDisabledState == WorkflowBase.OpMode.TURNEDOFF_BY_RCS_DISABLED_STATE) {
            return true;
        }
        return super.isValidRcsDisabledState(rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public boolean isActiveVersion(int version) {
        return version >= WorkflowBase.OpMode.ACTIVE.value() && version != 59;
    }

    /* access modifiers changed from: protected */
    public String getRcsState() {
        return this.mStorage.read(ConfigConstants.PATH.RCS_STATE_FOR_VZW);
    }

    /* access modifiers changed from: protected */
    public void setRcsState(String rcsState) {
        this.mStorage.write(ConfigConstants.PATH.RCS_STATE_FOR_VZW, rcsState);
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "setOpMode: mode: " + mode.name());
        switch (AnonymousClass2.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()]) {
            case 1:
                if (data != null) {
                    String str2 = LOG_TAG;
                    IMSLog.s(str2, "data: " + data);
                    if (getVersion() < getVersion(data) || (this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()))) {
                        clearStorageforVZW(String.valueOf(getVersion(data)));
                        this.mStorage.writeAll(data);
                        setVersionBackup(getVersion(data));
                    } else {
                        Log.d(LOG_TAG, "the same or lower version, remain previous data");
                        checkAndUpdateData(data);
                    }
                    setNextAutoconfigTimeFromValidity(getValidity());
                } else {
                    int backupVersion = Integer.parseInt(getVersionBackup());
                    if (backupVersion < WorkflowBase.OpMode.ACTIVE.value() || backupVersion == 59) {
                        Log.d(LOG_TAG, "data is empty, remain previous data and mode");
                    } else {
                        Log.d(LOG_TAG, "retreive backup version of configuration");
                        setVersion(backupVersion);
                    }
                }
                this.mIsReceivedMinusVerison = false;
                return;
            case 2:
            case 3:
            case 4:
                clearStorageforVZW(String.valueOf(59));
                if (data != null) {
                    this.mStorage.writeAll(data);
                    setVersion(59);
                }
                this.mMinusVersion = mode.value();
                this.mIsReceivedMinusVerison = true;
                return;
            case 5:
                setVersion(59);
                this.mMinusVersion = mode.value();
                this.mIsReceivedMinusVerison = true;
                return;
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
                if (data != null) {
                    this.mStorage.writeAll(data);
                }
                if (getOpMode() == WorkflowBase.OpMode.ACTIVE && getVersion() != 59) {
                    setVersionBackup(getVersion());
                }
                setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
                cancelValidityTimer();
                setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                this.mIsReceivedMinusVerison = false;
                return;
            case 11:
                if (getOpMode() == WorkflowBase.OpMode.ACTIVE && getVersion() != 59) {
                    setVersionBackup(getVersion());
                }
                setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
                String str3 = LOG_TAG;
                Log.d(str3, "rcsState: " + getRcsState());
                cancelValidityTimer();
                setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                this.mIsReceivedMinusVerison = false;
                return;
            case 12:
                if (getOpMode() == WorkflowBase.OpMode.ACTIVE && getVersion() != 59) {
                    setRcsState(String.valueOf(getVersion()));
                } else if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                    int backupVersion2 = Integer.parseInt(getVersionBackup());
                    String str4 = LOG_TAG;
                    Log.d(str4, "backupVersion: " + backupVersion2);
                    if (backupVersion2 == WorkflowBase.OpMode.DISABLE.value()) {
                        setRcsState(String.valueOf(WorkflowBase.OpMode.ENABLE_RCS_BY_USER.value()));
                    } else {
                        setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
                    }
                } else {
                    setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
                }
                String str5 = LOG_TAG;
                Log.d(str5, "rcsState: " + getRcsState());
                cancelValidityTimer();
                setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                this.mIsReceivedMinusVerison = false;
                return;
            default:
                Log.e(LOG_TAG, "unknown mode");
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowVzw$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
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
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_RCS_BY_USER.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.ENABLE_RCS_BY_USER.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    /* access modifiers changed from: private */
    public String getStandardImsiNai() {
        Log.d(LOG_TAG, "getStandardImsiNai");
        if (this.mTelephony.getImsi() == null || this.mTelephony.getMcc() == null || this.mTelephony.getMnc() == null) {
            return STANDARD_IMSI_NAI_TEMPLATE;
        }
        String imsi = STANDARD_IMSI_NAI_TEMPLATE.replace("<imsi>", this.mTelephony.getImsi()).replace("<mcc>", this.mTelephony.getMcc()).replace("<mnc>", this.mTelephony.getMnc());
        String str = LOG_TAG;
        IMSLog.s(str, "imsi: " + imsi);
        return imsi;
    }

    /* access modifiers changed from: private */
    public String generateChallengeResponse(String encodedval) {
        String akaChallenge = StrUtil.bytesToHexString(Base64.decode(encodedval.getBytes(), 2));
        return AKAEapAuthHelper.generateChallengeResponse(akaChallenge, this.mSm != null ? this.mSm.getIsimAuthentication(AKAEapAuthHelper.getNonce(akaChallenge)) : null, getStandardImsiNai());
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    private class Initialize implements WorkflowBase.Workflow {
        private Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw.this.mHttpResponse = 0;
            WorkflowVzw.this.mSharedInfo.setUrl(WorkflowVzw.this.mParamHandler.initUrl());
            WorkflowVzw.this.mCookieHandler.clearCookie();
            if (WorkflowVzw.this.mStartForce) {
                return new FetchHttpsForEapAka();
            }
            int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowVzw.this.getOpMode().ordinal()];
            return (i == 1 || i == 2 || i == 5) ? new FetchHttpsForEapAka() : new Finish();
        }
    }

    private class FetchHttpsForEapAka implements WorkflowBase.Workflow {
        private FetchHttpsForEapAka() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowVzw.this.mSharedInfo.setUrl(WorkflowVzw.this.mParamHandler.initUrl());
            WorkflowVzw.this.mSharedInfo.setHttpClean();
            WorkflowVzw.this.mSharedInfo.setHttpsDefault();
            String str = WorkflowVzw.LOG_TAG;
            int i = WorkflowVzw.this.mPhoneId;
            IMSLog.d(str, i, "FetchHttpsForEapAka: mAkaResponse: " + WorkflowVzw.this.getAkaResponse());
            if (WorkflowVzw.this.getAkaResponse() == null || WorkflowVzw.this.getAkaResponse().isEmpty()) {
                int vers = WorkflowVzw.this.getVersion();
                int versbackup = Integer.parseInt(WorkflowVzw.this.getVersionBackup());
                String str2 = WorkflowVzw.LOG_TAG;
                Log.d(str2, "vers: " + vers + " versbackup: " + versbackup);
                if (vers <= WorkflowBase.OpMode.DISABLE_TEMPORARY.value() || vers == 59) {
                    vers = (versbackup < WorkflowBase.OpMode.ACTIVE.value() || versbackup == 59) ? WorkflowBase.OpMode.DISABLE_TEMPORARY.value() : versbackup;
                }
                String str3 = WorkflowVzw.LOG_TAG;
                Log.d(str3, "set vers to " + vers);
                WorkflowVzw.this.mSharedInfo.addHttpParam("vers", String.valueOf(vers));
                String token = WorkflowVzw.this.getToken();
                WorkflowVzw.this.mSharedInfo.addHttpParam("token", token);
                String str4 = WorkflowVzw.LOG_TAG;
                IMSLog.s(str4, "token: " + token);
                if (token == null || token.isEmpty()) {
                    WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMSI_EAP, WorkflowVzw.this.getStandardImsiNai());
                } else {
                    WorkflowVzw.this.mSharedInfo.addHttpParam("IMSI", WorkflowVzw.this.mTelephony.getImsi());
                }
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowVzw.this.mTelephony.getImei());
                WorkflowVzw.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
                WorkflowVzw.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
                WorkflowVzw.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowVzw.this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(WorkflowVzw.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
                SharedInfo sharedInfo = WorkflowVzw.this.mSharedInfo;
                sharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, ConfigConstants.PVALUE.CLIENT_VERSION_NAME + WorkflowVzw.this.mClientVersion);
                WorkflowVzw.this.mSharedInfo.addHttpParam("rcs_version", WorkflowVzw.this.mRcsVersion);
                Log.d(WorkflowVzw.LOG_TAG, "set rcs_profile to UP_1.0");
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_1.0");
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, WorkflowVzw.this.mIsSmsDefaultApp ? "1" : "2");
                WorkflowVzw workflowVzw = WorkflowVzw.this;
                workflowVzw.setRcsState(workflowVzw.mIsSmsDefaultApp ? WorkflowVzw.this.convertRcsStateWithSpecificParam() : String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, WorkflowVzw.this.getRcsState());
                if (WorkflowVzw.this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowVzw.this.getRcsState())) {
                    Log.d(WorkflowVzw.LOG_TAG, "mStartForce: true, set vers and rcsState to 0");
                    WorkflowVzw.this.mSharedInfo.addHttpParam("vers", "0");
                    WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, "0");
                    WorkflowVzw.this.setRcsState("0");
                }
            } else {
                Log.d(WorkflowVzw.LOG_TAG, "set only eap_payld param");
                WorkflowVzw.this.mSharedInfo.setHttpClean();
                WorkflowVzw.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.EAP_PAYLD, WorkflowVzw.this.getAkaResponse());
                WorkflowVzw.this.setAkaResponse("");
            }
            WorkflowVzw.this.mSharedInfo.setHttpResponse(WorkflowVzw.this.getHttpResponse());
            WorkflowVzw workflowVzw2 = WorkflowVzw.this;
            workflowVzw2.mHttpResponse = workflowVzw2.mSharedInfo.getHttpResponse().getStatusCode();
            String str5 = WorkflowVzw.LOG_TAG;
            Log.d(str5, "FetchHttpsForEapAka: mHttpResponse: " + WorkflowVzw.this.mHttpResponse);
            if (WorkflowVzw.this.mHttpResponse == 200) {
                if (WorkflowVzw.this.mSharedInfo.getHttpResponse().getBody() != null) {
                    Log.d(WorkflowVzw.LOG_TAG, "response's body exists");
                    return new ParseForEapAka();
                }
                throw new UnknownStatusException("no body, something wrong");
            } else if (WorkflowVzw.this.mHttpResponse == 511) {
                Log.d(WorkflowVzw.LOG_TAG, "511 is received, reset EapAka response and config info");
                WorkflowVzw.this.setAkaResponse("");
                WorkflowVzw.this.setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                String str6 = WorkflowVzw.LOG_TAG;
                Log.d(str6, "m511BackOffRetryCount: " + WorkflowVzw.this.m511BackOffRetryCount);
                if (WorkflowVzw.this.m511BackOffRetryCount >= 5 || WorkflowVzw.backOffRetryTime[WorkflowVzw.this.m511BackOffRetryCount] <= 0) {
                    Log.d(WorkflowVzw.LOG_TAG, "no need to retry for 511 error, go to finish");
                    return new Finish();
                }
                String str7 = WorkflowVzw.LOG_TAG;
                Log.d(str7, "m511BackOffRetryTime: " + WorkflowVzw.backOffRetryTime[WorkflowVzw.this.m511BackOffRetryCount]);
                WorkflowVzw.this.sleep(WorkflowVzw.backOffRetryTime[WorkflowVzw.this.m511BackOffRetryCount]);
                WorkflowVzw workflowVzw3 = WorkflowVzw.this;
                workflowVzw3.m511BackOffRetryCount = workflowVzw3.m511BackOffRetryCount + 1;
                return new FetchHttpsForEapAka();
            } else if (WorkflowVzw.this.mHttpResponse == 403) {
                Log.d(WorkflowVzw.LOG_TAG, "403 is received, go to finish");
                WorkflowVzw.this.mIsReceived403Err = true;
                WorkflowVzw.this.setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                return new Finish();
            } else if (WorkflowVzw.this.mHttpResponse == 500) {
                Log.d(WorkflowVzw.LOG_TAG, "500 is received, go to finish");
                WorkflowVzw.this.mIsReceived500Err = true;
                return new Finish();
            } else {
                WorkflowVzw workflowVzw4 = WorkflowVzw.this;
                return workflowVzw4.handleResponseForUp(new Initialize(), new FetchHttpsForEapAka(), new Finish());
            }
        }
    }

    private class ParseForEapAka implements WorkflowBase.Workflow {
        private ParseForEapAka() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Map<String, String> parsedXml = WorkflowVzw.this.mXmlParser.parse(new String(WorkflowVzw.this.mSharedInfo.getHttpResponse().getBody(), "utf-8"));
            if (parsedXml != null) {
                WorkflowVzw.this.mSharedInfo.setParsedXml(parsedXml);
                WorkflowVzw.this.mParamHandler.checkSetToGS(parsedXml);
                if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                    if (parsedXml.get(ConfigConstants.PATH.EAP_AKA_CHALLENGE) != null) {
                        String nonce = WorkflowVzw.this.mSharedInfo.getParsedXml().get(ConfigConstants.PATH.EAP_AKA_CHALLENGE);
                        String str = WorkflowVzw.LOG_TAG;
                        IMSLog.s(str, "EapAka challenge: " + nonce);
                        String result = WorkflowVzw.this.generateChallengeResponse(nonce);
                        String str2 = WorkflowVzw.LOG_TAG;
                        IMSLog.s(str2, "EapAka response: " + result);
                        if (result != null) {
                            Log.d(WorkflowVzw.LOG_TAG, "EapAka response isn't empty");
                            WorkflowVzw.this.setAkaResponse(result);
                            return new FetchHttpsForEapAka();
                        }
                    }
                    throw new InvalidXmlException("parsedXmlForEapAka is something wrong");
                }
                Log.d(WorkflowVzw.LOG_TAG, "version/validity exists");
                return new Store();
            }
            throw new InvalidXmlException("parsedXmlForEapAka is null");
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            int version = WorkflowVzw.this.getVersion();
            WorkflowVzw workflowVzw = WorkflowVzw.this;
            boolean versionChange = version != workflowVzw.getVersion(workflowVzw.mSharedInfo.getParsedXml());
            String str = WorkflowVzw.LOG_TAG;
            int i = WorkflowVzw.this.mPhoneId;
            IMSLog.d(str, i, "versionChange: " + versionChange);
            WorkflowVzw workflowVzw2 = WorkflowVzw.this;
            WorkflowBase.OpMode rcsDisabledState = workflowVzw2.getRcsDisabledState(workflowVzw2.mSharedInfo.getParsedXml());
            if (WorkflowVzw.this.isValidRcsDisabledState(rcsDisabledState)) {
                Log.d(WorkflowVzw.LOG_TAG, "set opMode for rcsDisabledState");
                WorkflowVzw workflowVzw3 = WorkflowVzw.this;
                workflowVzw3.setOpMode(rcsDisabledState, workflowVzw3.mSharedInfo.getParsedXml());
                IMSLog.c(LogClass.WFV_SELFPROV_INFO, WorkflowVzw.this.mPhoneId + ",RDS:" + WorkflowVzw.this.convertRcsDisabledStateToValue(rcsDisabledState));
                if (rcsDisabledState == WorkflowBase.OpMode.DISABLE && ImsConstants.SystemSettings.getRcsUserSetting(WorkflowVzw.this.mContext, -1, WorkflowVzw.this.mPhoneId) == -1) {
                    Log.d(WorkflowVzw.LOG_TAG, "self provisioning procedure isn't completed");
                    return new Finish();
                }
                String str2 = WorkflowVzw.LOG_TAG;
                Log.d(str2, "mBackOffRetryCount: " + WorkflowVzw.this.mBackOffRetryCount);
                if (WorkflowVzw.this.mBackOffRetryCount >= 5 || WorkflowVzw.backOffRetryTime[WorkflowVzw.this.mBackOffRetryCount] <= 0) {
                    Log.d(WorkflowVzw.LOG_TAG, "no need to retry for rcsDisabledState");
                    return new Finish();
                }
                String str3 = WorkflowVzw.LOG_TAG;
                Log.d(str3, "backOffRetryTime: " + WorkflowVzw.backOffRetryTime[WorkflowVzw.this.mBackOffRetryCount]);
                IMSLog.c(LogClass.WFV_BACKOFF_RETRY_TIME, WorkflowVzw.this.mPhoneId + ",BRT:" + WorkflowVzw.backOffRetryTime[WorkflowVzw.this.mBackOffRetryCount]);
                WorkflowVzw workflowVzw4 = WorkflowVzw.this;
                workflowVzw4.addEventLog(WorkflowVzw.LOG_TAG + ": backOffRetryTime: " + WorkflowVzw.backOffRetryTime[WorkflowVzw.this.mBackOffRetryCount]);
                WorkflowVzw.this.sleep(WorkflowVzw.backOffRetryTime[WorkflowVzw.this.mBackOffRetryCount]);
                WorkflowVzw workflowVzw5 = WorkflowVzw.this;
                workflowVzw5.mBackOffRetryCount = workflowVzw5.mBackOffRetryCount + 1;
                WorkflowVzw.this.mStartForce = false;
                return new FetchHttpsForEapAka();
            }
            Log.d(WorkflowVzw.LOG_TAG, "set opMode for rcsVersion");
            WorkflowVzw workflowVzw6 = WorkflowVzw.this;
            workflowVzw6.setOpMode(workflowVzw6.getOpMode(workflowVzw6.mSharedInfo.getParsedXml()), WorkflowVzw.this.mSharedInfo.getParsedXml());
            String str4 = WorkflowVzw.LOG_TAG;
            Log.d(str4, "mIsReceivedMinusVerison: " + WorkflowVzw.this.mIsReceivedMinusVerison);
            if (WorkflowVzw.this.getOpMode() == WorkflowBase.OpMode.ACTIVE && !WorkflowVzw.this.mIsReceivedMinusVerison) {
                WorkflowVzw workflowVzw7 = WorkflowVzw.this;
                workflowVzw7.setValidityTimer(workflowVzw7.getValidity());
            }
            return new Finish();
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowVzw.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowVzw workflowVzw = WorkflowVzw.this;
                workflowVzw.setLastErrorCode(workflowVzw.mSharedInfo.getHttpResponse().getStatusCode());
            }
            IMSLog.d(WorkflowVzw.LOG_TAG, WorkflowVzw.this.mPhoneId, "workflow is finished");
            return null;
        }
    }
}
