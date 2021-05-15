package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Map;

public class WorkflowAtt extends WorkflowBase {
    private static final String INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY = "com.sec.internal.ims.config.workflow.token_expired_after_max_retry";
    public static final String LOG_TAG = WorkflowAtt.class.getSimpleName();
    private static final long RESET_TOKEN_TIMEOUT = 86400000;
    private static final int[] RETRY_INTERVAL = {Id.REQUEST_SIP_DIALOG_SEND_SIP, 3600, 7200, 14400, 28800};
    private long expirationTime = 0;
    /* access modifiers changed from: private */
    public boolean isACSsuccessful = false;
    protected boolean isAirplaneModeObserverRegistered = false;
    private boolean isFailedToConnect = false;
    /* access modifiers changed from: private */
    public boolean isFirstImsRegistrationDone;
    protected boolean isImsRegiListenerRegistered;
    protected boolean isMainSwitchToggled = false;
    protected boolean isRcsUserSettingObserverRegistered = false;
    private boolean isRetry = false;
    private final ContentObserver mAirplaneModeObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange) {
            IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "onChange: Airplane Mode Observer");
            int airPlaneModeOn = Settings.Global.getInt(WorkflowAtt.this.mContext.getContentResolver(), "airplane_mode_on", 1);
            String str = WorkflowAtt.LOG_TAG;
            int i = WorkflowAtt.this.mPhoneId;
            IMSLog.i(str, i, "Airplane Mode On: " + airPlaneModeOn);
            if (airPlaneModeOn == 0) {
                Handler handler = WorkflowAtt.this.mModuleHandler;
                WorkflowAtt workflowAtt = WorkflowAtt.this;
                handler.sendMessage(workflowAtt.obtainMessage(17, Integer.valueOf(workflowAtt.mPhoneId)));
            }
        }
    };
    private final IImsRegistrationListener mImsRegistrationListener = new IImsRegistrationListener.Stub() {
        public void onRegistered(ImsRegistration reg) {
            IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "onRegistered()");
            if (!WorkflowAtt.this.isFirstImsRegistrationDone) {
                boolean unused = WorkflowAtt.this.isFirstImsRegistrationDone = true;
                String unused2 = WorkflowAtt.this.mMsisdn = reg.getOwnNumber();
                if (WorkflowAtt.this.mMsisdn != null) {
                    Log.i(WorkflowAtt.LOG_TAG, "MSISDN is registered.");
                }
                WorkflowAtt workflowAtt = WorkflowAtt.this;
                workflowAtt.addEventLog(WorkflowAtt.LOG_TAG + ": IMS registered, start autoconfig");
                WorkflowAtt.this.sendEmptyMessage(1);
                WorkflowAtt.this.unregisterImsRegistrationListener();
            }
        }

        public void onDeregistered(ImsRegistration reg, ImsRegistrationError errorCode) {
            Log.i(WorkflowAtt.LOG_TAG, "onDeregistered()");
        }
    };
    /* access modifiers changed from: private */
    public String mMsisdn;
    private final ContentObserver mRcsUserSettingObserver = new ContentObserver(this) {
        public void onChange(boolean selfChange) {
            WorkflowAtt workflowAtt = WorkflowAtt.this;
            workflowAtt.addEventLog(WorkflowAtt.LOG_TAG + ": RCS user switch is toggled, start autoconfig");
            WorkflowAtt.this.isMainSwitchToggled = true;
            WorkflowAtt.this.sendEmptyMessage(0);
        }
    };
    private PendingIntent mResetTokenIntent = null;
    private int mTrialCount = 0;

    private void registerRcsUserSettingObserver(int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "registerRcsUserSettingObserver");
        Uri uri = ImsConstants.SystemSettings.RCS_USER_SETTING1.getUri();
        if (!this.isRcsUserSettingObserverRegistered) {
            this.mContext.getContentResolver().registerContentObserver(uri, false, this.mRcsUserSettingObserver);
            this.isRcsUserSettingObserverRegistered = true;
        }
    }

    private void unregisterRcsUserSettingObserver() {
        Log.i(LOG_TAG, "unregisterRcsUserSettingObserver");
        if (this.isRcsUserSettingObserverRegistered) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mRcsUserSettingObserver);
            this.isRcsUserSettingObserverRegistered = false;
        }
    }

    public void setIsFirstImsRegistrationDone(boolean isDone) {
        this.isFirstImsRegistrationDone = isDone;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowAtt(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceAtt r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceAtt
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
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
            r11.expirationTime = r0
            r0 = 0
            r11.isMainSwitchToggled = r0
            r1 = 0
            r11.mResetTokenIntent = r1
            com.sec.internal.ims.config.workflow.WorkflowAtt$1 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$1
            r1.<init>(r15)
            r11.mRcsUserSettingObserver = r1
            com.sec.internal.ims.config.workflow.WorkflowAtt$2 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$2
            r1.<init>()
            r11.mImsRegistrationListener = r1
            com.sec.internal.ims.config.workflow.WorkflowAtt$11 r1 = new com.sec.internal.ims.config.workflow.WorkflowAtt$11
            r1.<init>(r15)
            r11.mAirplaneModeObserver = r1
            r11.mTrialCount = r0
            r11.isAirplaneModeObserverRegistered = r0
            r11.isRcsUserSettingObserverRegistered = r0
            r11.isACSsuccessful = r0
            r11.isRetry = r0
            r11.isFailedToConnect = r0
            r15.registerAirplaneModeObserver()
            r15.registerRcsUserSettingObserver(r14)
            r11.isImsRegiListenerRegistered = r0
            r11.isFirstImsRegistrationDone = r0
            r15.registerImsRegistrationListener()
            r15.registerResetTokenIntentReceiver()
            boolean r0 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isDualRcsReg()
            if (r0 != 0) goto L_0x0075
            com.sec.internal.interfaces.ims.config.IStorageAdapter r0 = r11.mStorage
            r1 = 1
            r0.setDBTableMax(r1)
        L_0x0075:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowAtt.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message: " + msg.what);
        if (msg.what != 5) {
            super.handleMessage(msg);
            return;
        }
        addEventLog(LOG_TAG + ": sms default application is changed");
        this.isMainSwitchToggled = true;
        sendEmptyMessage(0);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        switch (type) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Initialize:");
                        return super.run();
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchHttp:");
                        return super.run();
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        if (WorkflowAtt.this.mParamHandler.isConfigProxy()) {
                            WorkflowAtt.this.mSharedInfo.setHttpDefault();
                        } else {
                            WorkflowAtt.this.mSharedInfo.setHttpsDefault();
                        }
                        boolean z = true;
                        if (ImsConstants.SystemSettings.getRcsUserSetting(WorkflowAtt.this.mContext, -1, WorkflowAtt.this.mPhoneId) != 1) {
                            z = false;
                        }
                        boolean isIpmeSwitchOn = z;
                        String str = WorkflowAtt.LOG_TAG;
                        Log.i(str, "isIpmeSwitchOn: " + isIpmeSwitchOn);
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(WorkflowAtt.this.getVersionFromServer()));
                        WorkflowAtt.this.mSharedInfo.addHttpParam("rcs_version", WorkflowAtt.this.mRcsVersion);
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
                        SharedInfo sharedInfo = WorkflowAtt.this.mSharedInfo;
                        sharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, WorkflowAtt.this.mClientPlatform + WorkflowAtt.this.mClientVersion);
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, WorkflowAtt.this.isSmsAppDefault() ? "1" : "2");
                        WorkflowAtt.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowAtt.this.getVersionFromServer()));
                        WorkflowAtt.this.mSharedInfo.addHttpParam("IMSI", WorkflowAtt.this.mTelephony.getImsi());
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.TERMINAL_VENDOR);
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
                        WorkflowAtt.this.mSharedInfo.addHttpParam("terminal_sw_version", ConfigConstants.PVALUE.TERMINAL_SW_VERSION);
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowAtt.this.mTelephony.getImei());
                        WorkflowAtt.this.mSharedInfo.addHttpParam("token", decrypt(WorkflowAtt.this.getToken()));
                        if (!TextUtils.isEmpty(WorkflowAtt.this.getMsisdn())) {
                            WorkflowAtt.this.mSharedInfo.addHttpParam("msisdn", WorkflowAtt.this.mParamHandler.encodeRFC3986(WorkflowAtt.this.getMsisdn()));
                        }
                        if (WorkflowAtt.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                            WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, WorkflowAtt.this.mTelephony.getSmsDestPort());
                        }
                        if (WorkflowAtt.this.mStartForce && !isIpmeSwitchOn) {
                            WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
                        }
                        if (WorkflowAtt.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            String str2 = WorkflowAtt.LOG_TAG;
                            Log.i(str2, "DORMANT mode. use backup version :" + WorkflowAtt.this.getVersionBackup());
                            WorkflowAtt.this.mSharedInfo.addHttpParam("vers", WorkflowAtt.this.getVersionBackup());
                        }
                    }

                    private String decrypt(String data) {
                        if (data == null) {
                            return null;
                        }
                        try {
                            return new String(Base64.decode(data, 0));
                        } catch (IllegalArgumentException e) {
                            Log.i(WorkflowAtt.LOG_TAG, "Failed to decrypt the data");
                            return data;
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Authorize:");
                        return super.run();
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        WorkflowAtt.this.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, WorkflowAtt.this.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie"));
                        if (!TextUtils.isEmpty(WorkflowAtt.this.getMsisdn())) {
                            WorkflowAtt.this.mSharedInfo.addHttpParam("msisdn", WorkflowAtt.this.mParamHandler.encodeRFC3986(WorkflowAtt.this.getMsisdn()));
                        }
                        WorkflowAtt.this.mSharedInfo.addHttpParam("IMSI", WorkflowAtt.this.mTelephony.getImsi());
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowAtt.this.mTelephony.getImei());
                        WorkflowAtt.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, String.valueOf(0));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "Parse:");
                        try {
                            return super.run();
                        } catch (UnknownStatusException e) {
                            return WorkflowAtt.this.getNextWorkflow(8);
                        }
                    }

                    /* access modifiers changed from: protected */
                    public void parseParam(Map<String, String> parsedXml) {
                        WorkflowAtt.this.mParamHandler.parseParamForAtt(parsedXml);
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        Map<String, String> parsedXml = WorkflowAtt.this.mSharedInfo.getParsedXml();
                        WorkflowAtt.this.mParamHandler.setOpModeWithUserAccept(WorkflowAtt.this.mParamHandler.getUserAccept(parsedXml), parsedXml, WorkflowBase.OpMode.DISABLE);
                        if (WorkflowAtt.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                            WorkflowAtt workflowAtt = WorkflowAtt.this;
                            workflowAtt.setValidityTimer((int) (((double) workflowAtt.getValidity()) * 0.8d));
                        }
                        WorkflowAtt workflowAtt2 = WorkflowAtt.this;
                        workflowAtt2.setVersionFromServer(workflowAtt2.getVersion());
                        boolean unused = WorkflowAtt.this.isACSsuccessful = true;
                        return WorkflowAtt.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() throws Exception {
                        int currentVersion = WorkflowAtt.this.getVersion();
                        String str = WorkflowAtt.LOG_TAG;
                        int i = WorkflowAtt.this.mPhoneId;
                        IMSLog.i(str, i, "Finish: currentVersion=" + currentVersion);
                        if (WorkflowAtt.this.mSharedInfo.getHttpResponse() != null) {
                            WorkflowAtt workflowAtt = WorkflowAtt.this;
                            workflowAtt.setLastErrorCode(workflowAtt.mSharedInfo.getHttpResponse().getStatusCode());
                        }
                        WorkflowAtt.this.createSharedInfo();
                        if (currentVersion <= 0) {
                            WorkflowAtt.this.loadPreConfig();
                        }
                        WorkflowAtt.this.setLastSwVersion(ConfigConstants.BUILD.TERMINAL_SW_VERSION);
                        return null;
                    }
                };
            default:
                return null;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow current, int errorCode) throws InvalidHeaderException, UnknownStatusException {
        this.mLastErrorCode = errorCode;
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleResponse: mLastErrorCode: " + this.mLastErrorCode);
        addEventLog(LOG_TAG + ": handleResponse: mLastErrorCode: " + this.mLastErrorCode);
        if (errorCode != 0) {
            if (errorCode == 301) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "http redirects");
                this.mSharedInfo.setUrl((String) this.mSharedInfo.getHttpResponse().getHeader().get("Location").get(0));
                this.mHttpRedirect = true;
                return getNextWorkflow(1);
            } else if (errorCode == 401 || errorCode == 403) {
                return getNextWorkflow(8);
            } else {
                if (errorCode != 511) {
                    if (!(errorCode == 800 || errorCode == 801)) {
                        return super.handleResponse(current, errorCode);
                    }
                } else if (current instanceof WorkflowBase.FetchHttp) {
                    return getNextWorkflow(3);
                } else {
                    setToken("");
                    return getNextWorkflow(8);
                }
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "Failed to reach ACS");
        this.isFailedToConnect = true;
        return getNextWorkflow(8);
    }

    /* access modifiers changed from: private */
    public String getMsisdn() {
        if (this.mMsisdn != null) {
            return this.mMsisdn;
        }
        String registeredMsisdn = this.mTelephony.getMsisdn();
        Log.i(LOG_TAG, "getMsisdn: use telephony msisdn");
        return registeredMsisdn;
    }

    private void registerAirplaneModeObserver() {
        Log.i(LOG_TAG, "registerAirplaneModeObserver");
        if (!this.isAirplaneModeObserverRegistered) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), false, this.mAirplaneModeObserver);
            this.isAirplaneModeObserverRegistered = true;
        }
    }

    private void unregisterAirplaneModeObserver() {
        Log.i(LOG_TAG, "unregisterAirplaneModeObserver");
        if (this.isAirplaneModeObserverRegistered) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mAirplaneModeObserver);
            this.isAirplaneModeObserverRegistered = false;
        }
    }

    private boolean isBinarySMSForcedEvent() {
        return this.mStartForce && !this.isMainSwitchToggled;
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next;
        String str;
        if (isBinarySMSForcedEvent()) {
            setNextAutoconfigTimeFromValidity(0);
            cancelValidityTimer();
        } else if (((int) ((this.expirationTime - new Date().getTime()) / 1000)) > 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "30 sec has not passed after previous Autoconfig");
            return;
        } else if (this.isFirstImsRegistrationDone && !isNetworkAvailable()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "No network connection, try when connected");
            addEventLog(LOG_TAG + ": no network connection, try when connected");
            this.mModuleHandler.sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
            return;
        }
        this.isMainSwitchToggled = false;
        stopResetTokenTimer();
        if (this.isRetry) {
            this.isRetry = false;
        } else {
            this.mTrialCount = 0;
        }
        WorkflowBase.Workflow next2 = getNextWorkflow(1);
        if (!this.isFirstImsRegistrationDone) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "The first IMS registration didn't happen yet: skip autoconfig");
            addEventLog(LOG_TAG + ": IMS is not yet registered, skip autoconfig");
            IMSLog.c(LogClass.WFA_NO_FIRST_REGI, this.mPhoneId + ",NOFR");
            next2 = getNextWorkflow(8);
            this.isACSsuccessful = true;
        } else {
            this.expirationTime = new Date().getTime() + 30000;
        }
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                this.isFailedToConnect = true;
                e.printStackTrace();
                next = getNextWorkflow(8);
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                e2.printStackTrace();
                next = getNextWorkflow(8);
            } catch (Exception e3) {
                String str2 = LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("unknown exception occur:");
                if (e3.getMessage() == null) {
                    str = "";
                } else {
                    str = e3.getMessage();
                }
                sb.append(str);
                Log.i(str2, sb.toString());
                e3.printStackTrace();
                next = getNextWorkflow(8);
            }
            count--;
        }
        if (this.isACSsuccessful) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Autoconfig is done");
            this.isACSsuccessful = false;
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Autoconfig failed: isFailedToConnect=" + this.isFailedToConnect);
            addEventLog(LOG_TAG + ": Autoconfig failed: isFailedToConnect=" + this.isFailedToConnect);
            IMSLog.c(LogClass.WFA_CONNECT_FAILED, this.mPhoneId + ",FAIL,CON:" + this.isFailedToConnect);
            int interval = getTrialInterval();
            if (this.mLastErrorCode == 401 || this.mLastErrorCode == 403) {
                this.isRetry = false;
                cancelValidityTimer();
            } else if (interval < 0) {
                cancelValidityTimer();
                startResetTokenTimer();
                this.mModuleHandler.sendMessage(obtainMessage(17, Integer.valueOf(this.mPhoneId)));
            } else {
                setValidityTimer(interval);
                this.mTrialCount++;
            }
        }
        this.isFailedToConnect = false;
    }

    private int getTrialInterval() {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "mTrialCount=" + this.mTrialCount);
        int i2 = this.mTrialCount;
        int[] iArr = RETRY_INTERVAL;
        if (i2 < iArr.length) {
            this.isRetry = true;
            return iArr[i2];
        }
        Log.i(LOG_TAG, "Trial Count is bigger than retry count. No more retry!");
        this.isRetry = false;
        return -1;
    }

    private void startResetTokenTimer() {
        if (getToken() == null) {
            Log.i(LOG_TAG, "startResetTokenTimer: token doesn't exist, vail.");
            return;
        }
        if (this.mResetTokenIntent != null) {
            stopResetTokenTimer();
        }
        Log.i(LOG_TAG, "startResetTokenTimer()");
        this.mResetTokenIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY), 134217728);
        AlarmTimer.start(this.mContext, this.mResetTokenIntent, RESET_TOKEN_TIMEOUT);
    }

    /* access modifiers changed from: private */
    public void stopResetTokenTimer() {
        if (this.mResetTokenIntent == null) {
            Log.w(LOG_TAG, "stopResetTokenTimer: timer is not running.");
            return;
        }
        Log.i(LOG_TAG, "stopResetTokenTimer()");
        AlarmTimer.stop(this.mContext, this.mResetTokenIntent);
        this.mResetTokenIntent = null;
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup()");
        super.cleanup();
        unregisterAirplaneModeObserver();
        unregisterRcsUserSettingObserver();
        unregisterImsRegistrationListener();
    }

    public boolean checkNetworkConnectivity() {
        return false;
    }

    /* access modifiers changed from: private */
    public void loadPreConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "loadPreConfig");
        addEventLog(LOG_TAG + ": loadPreConfig");
        IMSLog.c(LogClass.WFA_PRE_CONFIG, this.mPhoneId + ",LPC");
        int versionFromServer = getVersion();
        String xml = ConfigUtil.getResourcesFromFile(this.mContext, this.mPhoneId, ConfigUtil.LOCAL_CONFIG_FILE, "utf-8");
        Map<String, String> parsedXml = WorkflowLocalFile.parseJson(xml, "att_preconfig");
        if (parsedXml != null) {
            parsedXml.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, xml);
            setOpMode(getOpMode(parsedXml), parsedXml);
            setVersionFromServer(versionFromServer);
            setLastErrorCode(this.mLastErrorCodeNonRemote);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String lastSwVersion) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleSwVersionChange: getLastSwVersion()=" + lastSwVersion + ", currentSwVersion=" + ConfigConstants.BUILD.TERMINAL_SW_VERSION);
        if (!lastSwVersion.equals(ConfigConstants.BUILD.TERMINAL_SW_VERSION)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "FOTA upgrade happened: clear previous RCS DB");
            clearStorage();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowAtt$13  reason: invalid class name */
    static /* synthetic */ class AnonymousClass13 {
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
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "new operation mode :" + mode.name());
        int i2 = AnonymousClass13.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()];
        if (i2 == 1) {
            if (data != null) {
                String str2 = LOG_TAG;
                IMSLog.s(str2, "data :" + data);
                int oldVerion = getVersion();
                if (true ^ TextUtils.isEmpty(data.get("root/application/1/presence/PublishTimer".toLowerCase()))) {
                    Log.i(LOG_TAG, "Received XML has full config");
                    writeDataToStorage(data);
                } else if (getVersion(data) == oldVerion) {
                    Log.i(LOG_TAG, "Received XML does NOT have full config with the same version");
                    int newValidity = getValidity(data);
                    Log.i(LOG_TAG, "Update validity");
                    setValidity(newValidity);
                    String newToken = getToken(data);
                    if (!TextUtils.isEmpty(newToken) && !TextUtils.equals(newToken, getToken())) {
                        Log.i(LOG_TAG, "Token is changed so update it");
                        setToken(newToken);
                    }
                } else {
                    Log.i(LOG_TAG, "Received non-full XML and version is different: Ignore");
                }
            } else {
                Log.i(LOG_TAG, "null data. remain previous mode & data");
            }
            setNextAutoconfigTimeFromValidity((int) (((double) getValidity()) * 0.8d));
        } else if (i2 == 2 || i2 == 3 || i2 == 4) {
            clearStorage();
            setVersion(mode.value());
            setValidity(mode.value());
        } else if (i2 == 5) {
            if (getVersion() != WorkflowBase.OpMode.DORMANT.value()) {
                setVersionBackup(getVersion());
            }
            setVersion(mode.value());
        }
    }

    private void registerResetTokenIntentReceiver() {
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (WorkflowAtt.INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY.equals(intent.getAction())) {
                    IMSLog.i(WorkflowAtt.LOG_TAG, WorkflowAtt.this.mPhoneId, "onReceive: token expired 24hrs after no_more_retry state");
                    WorkflowAtt.this.stopResetTokenTimer();
                    WorkflowAtt.this.removeToken();
                }
            }
        }, new IntentFilter(INTENT_TOKEN_EXPIRED_AFTER_MAX_RETRY));
    }

    private void registerImsRegistrationListener() {
        Log.i(LOG_TAG, "registerImsRegistrationListener");
        if (!this.isImsRegiListenerRegistered) {
            try {
                ImsRegistry.getRegistrationManager().registerListener(this.mImsRegistrationListener, this.mPhoneId);
                this.isImsRegiListenerRegistered = true;
            } catch (Exception e) {
                Log.i(LOG_TAG, "registerImsRegistrationListener failed");
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void unregisterImsRegistrationListener() {
        Log.i(LOG_TAG, "unregisterImsRegistrationListener");
        if (this.isImsRegiListenerRegistered) {
            try {
                ImsRegistry.getRegistrationManager().unregisterListener(this.mImsRegistrationListener, this.mPhoneId);
                Log.i(LOG_TAG, "mImsRegistrationListener was unregistered");
                this.isImsRegiListenerRegistered = false;
            } catch (Exception e) {
                Log.i(LOG_TAG, "unregisterImsRegistrationListener failed");
                e.printStackTrace();
            }
        }
    }
}
