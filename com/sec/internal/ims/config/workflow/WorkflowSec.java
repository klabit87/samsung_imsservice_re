package com.sec.internal.ims.config.workflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.net.ConnectivityManager;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowSec extends WorkflowUpBase {
    protected static final String INTENT_EXTRA_CAUSE = "CAUSE";
    protected static final String INTENT_LTE_REJECT = "android.intent.action.LTE_REJECT";
    protected static final String INTENT_REGIST_REJECT = "com.android.server.status.regist_reject";
    protected static final int INTERNALERR_RETRY_MAX_COUNT = 1;
    protected static final int INTERNAL_503_ERR_RETRY_MAX_COUNT = 1;
    protected static final int INTERNAL_511_ERR_RETRY_MAX_COUNT = 1;
    protected static final String LOG_TAG = WorkflowSec.class.getSimpleName();
    protected static final String OTP_SMS_BINARY_TYPE = "binary";
    protected static final String OTP_SMS_TEXT_TYPE = "text";
    protected static final int OTP_SMS_TIME_OUT = 700;
    protected static final int RESET_RETRY_MAX_COUNT = 3;
    private static final int[] RETRY_INTERVAL = {Id.REQUEST_SIP_DIALOG_SEND_SIP, 3600, 7200, 14400, 28800};
    private static final int RETRY_INTERVAL_DAILY = 86400;
    protected static final int STORAGE_STATE_READY = 1;
    private static int mAcsCnt = 0;
    /* access modifiers changed from: private */
    public int mAuthHiddenTryCount = 0;
    /* access modifiers changed from: private */
    public int mAuthTryCount = 0;
    protected int mHttpResponse = 0;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String str = WorkflowSec.LOG_TAG;
            int i = WorkflowSec.this.mPhoneId;
            IMSLog.i(str, i, "onReceive: " + intent.getAction());
            if (("com.android.server.status.regist_reject".equals(intent.getAction()) || "android.intent.action.LTE_REJECT".equals(intent.getAction())) && WorkflowSec.this.needToProcessRejectCode(intent.getStringExtra("CAUSE"))) {
                int unused = WorkflowSec.this.mResetRetryCount = 0;
                ((IConfigModule) WorkflowSec.this.mModuleHandler).setAcsTryReason(WorkflowSec.this.mPhoneId, DiagnosisConstants.RCSA_ATRE.REJECT_LTE);
                if (WorkflowSec.this.getStorageState() != 1) {
                    Log.i(WorkflowSec.LOG_TAG, "StorageAdapter's state is idle");
                    WorkflowSec.this.removeMessages(8);
                    WorkflowSec workflowSec = WorkflowSec.this;
                    workflowSec.sendMessageDelayed(workflowSec.obtainMessage(8), 10000);
                    return;
                }
                WorkflowSec.this.resetAutoConfigInfo(true);
            }
        }
    };
    private int mNewVersion = 0;
    private int mOldVersion = 0;
    /* access modifiers changed from: private */
    public int mResetRetryCount = 0;
    private int mTrialCount;

    static /* synthetic */ int access$308(WorkflowSec x0) {
        int i = x0.mAuthTryCount;
        x0.mAuthTryCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$408(WorkflowSec x0) {
        int i = x0.mAuthHiddenTryCount;
        x0.mAuthHiddenTryCount = i + 1;
        return i;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowSec(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceSec r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceSec
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec r7 = new com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec
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
            r11.mHttpResponse = r0
            r11.mAuthTryCount = r0
            r11.mAuthHiddenTryCount = r0
            r11.mResetRetryCount = r0
            com.sec.internal.ims.config.workflow.WorkflowSec$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowSec$1
            r0.<init>()
            r11.mIntentReceiver = r0
            android.content.IntentFilter r0 = new android.content.IntentFilter
            r0.<init>()
            java.lang.String r1 = "com.android.server.status.regist_reject"
            r0.addAction(r1)
            java.lang.String r1 = "android.intent.action.LTE_REJECT"
            r0.addAction(r1)
            android.content.Context r1 = r11.mContext
            android.content.BroadcastReceiver r2 = r11.mIntentReceiver
            r1.registerReceiver(r2, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowSec.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "handleMessage: " + msg.what);
        addEventLog(LOG_TAG + "handleMessage: " + msg.what);
        int i = msg.what;
        if (i == 0) {
            this.mStartForce = true;
        } else if (i != 1) {
            if (i != 5) {
                if (i != 8) {
                    super.handleMessage(msg);
                    return;
                } else if (checkMobileConnection((ConnectivityManager) this.mContext.getSystemService("connectivity"))) {
                    Log.i(LOG_TAG, "ignore auto config reset in mobile connection state");
                    return;
                } else {
                    this.mResetRetryCount++;
                    if (getStorageState() == 1 || this.mResetRetryCount >= 3) {
                        resetAutoConfigInfo(true);
                        return;
                    }
                    Log.i(LOG_TAG, "StorageAdapter's state is idle");
                    removeMessages(8);
                    sendMessageDelayed(obtainMessage(8), 10000);
                    return;
                }
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "sms default application is changed to samsung");
                resetAutoConfigInfo(false);
                ((IConfigModule) this.mModuleHandler).setAcsTryReason(this.mPhoneId, DiagnosisConstants.RCSA_ATRE.CHANGE_MSG_APP);
                return;
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "sms default application is changed to non-samsung");
                setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                return;
            }
        }
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "AutoConfig: ongoing");
            return;
        }
        mAcsCnt++;
        this.sIsConfigOngoing = true;
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: start : " + mAcsCnt);
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mSharedInfo.setInternalErrRetryCount(0);
        this.mSharedInfo.setInternal503ErrRetryCount(0);
        this.mSharedInfo.setInternal511ErrRetryCount(0);
        this.mPowerController.lock();
        this.mOldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(this.mOldVersion);
        }
        this.mNewVersion = getVersion();
        IMSLog.i(LOG_TAG, this.mPhoneId, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
        if (this.mOldVersion >= 0 && !isValidRcsDisabledState(getRcsDisabledState())) {
            this.mTelephony.notifyAutoConfigurationListener(52, this.mNewVersion > 0);
        }
        setCompleted(true);
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.sIsConfigOngoing = false;
    }

    public void reInitIfNeeded() {
        if (this.mTelephony.isReady()) {
            String identity = this.mTelephony.getIdentityByPhoneId(this.mPhoneId);
            if (!TextUtils.isEmpty(identity) && !TextUtils.equals(this.mIdentity, identity)) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "reInitIfNeeded: identity changed, re-init storage");
                IMSLog.c(LogClass.WFS_STORAGE_RE_INIT, this.mPhoneId + ",STOR_RI");
                addEventLog(LOG_TAG + ": reInitIfNeeded: identity changed, re-init storage");
                resetStorage();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean needToProcessRejectCode(String rejectCodeString) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "rejectCode: " + rejectCodeString);
        if (rejectCodeString == null) {
            return false;
        }
        int rejectCode = 0;
        try {
            rejectCode = Integer.parseInt(rejectCodeString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        IMSLog.c(LogClass.WFS_LTE_REJECT, this.mPhoneId + ",LTE reject by cause " + rejectCode);
        if (rejectCode == 2 || rejectCode == 3 || rejectCode == 6 || rejectCode == 8) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void resetAutoConfigInfo(Boolean isNeedSetVersion) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "resetAutoConfigInfo");
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",resetAutoConfigInfo");
        if (isNeedSetVersion.booleanValue()) {
            setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        }
        setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
        setRcsDisabledState("");
        setValidity(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        cancelValidityTimer();
        setNextAutoconfigTime(0);
        if (this.mModuleHandler != null) {
            ((IConfigModule) this.mModuleHandler).getAcsConfig(this.mPhoneId).setAcsCompleteStatus(false);
            ((IConfigModule) this.mModuleHandler).getAcsConfig(this.mPhoneId).setForceAcs(true);
        }
    }

    /* access modifiers changed from: private */
    public void setInfoLastErrorCode(int errorCode) {
        this.mStorage.write(ConfigConstants.PATH.INFO_LAST_ERROR_CODE, String.valueOf(errorCode));
    }

    private int getTrialInterval() {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "mTrialCount=" + this.mTrialCount);
        int i2 = this.mTrialCount;
        int[] iArr = RETRY_INTERVAL;
        if (i2 < iArr.length) {
            return iArr[i2];
        }
        Log.i(LOG_TAG, "Trial Count is bigger than retry count. So retry once a day");
        return -1;
    }

    private void retryExpBackoff() {
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        int version = getVersion();
        if (rcsDisabledState == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE || version == WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "retryExpBackoff: rcsDisabledState: " + convertRcsDisabledStateToValue(rcsDisabledState) + " ,Currnet version: " + version);
            int interval = getTrialInterval();
            if (this.mLastErrorCode == 403) {
                Log.i(LOG_TAG, "mLastErrorCode is 403, No retry");
                cancelValidityTimer();
            } else if (interval < 0) {
                Log.i(LOG_TAG, "retryExpBackoff: Once a day");
                IMSLog.c(LogClass.WFS_RETRY_DAILY, this.mPhoneId + ",RID");
                addEventLog(LOG_TAG + ": retryExpBackoff: Once a day");
                setValidityTimer(86400);
                setNextAutoconfigTime(86400);
            } else {
                String str2 = LOG_TAG;
                Log.i(str2, "retryExpBackoff: interval: " + interval + ImsConstants.RCS_AS.SEC);
                IMSLog.c(LogClass.WFS_RETRY_DAILY, this.mPhoneId + ",RBOI:" + interval);
                addEventLog(LOG_TAG + ": retryExpBackoff: interval: " + interval + ImsConstants.RCS_AS.SEC);
                setValidityTimer(interval);
                setNextAutoconfigTime((long) interval);
                this.mTrialCount = this.mTrialCount + 1;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = getNextWorkflow(1);
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException: " + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec and retry");
                sleep(10000);
                next = getNextWorkflow(1);
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                String message = e2.getMessage();
                Log.i(LOG_TAG, "UnknownStatusException: " + message);
                if ("body and cookie are null".equals(message)) {
                    next = getNextWorkflow(8);
                } else {
                    Log.i(LOG_TAG, "wait 2 sec and retry");
                    sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    next = getNextWorkflow(1);
                }
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                next = getNextWorkflow(8);
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                next = getNextWorkflow(1);
                e4.printStackTrace();
            }
            count--;
        }
        if (Boolean.valueOf(ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_SUPPORT_EXPONENTIAL_RETRY_ACS, false)).booleanValue()) {
            retryExpBackoff();
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow current, int errorCode) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleResponse: " + errorCode);
        addEventLog(LOG_TAG + "handleResponse: " + errorCode);
        this.mLastErrorCode = errorCode;
        if (errorCode == 511) {
            if (current instanceof WorkflowBase.FetchHttp) {
                return getNextWorkflow(3);
            }
            setToken("");
            removeValidToken();
            int retry511Cnt = this.mSharedInfo.getInternal511ErrRetryCount() + 1;
            if (retry511Cnt <= 1) {
                String str2 = LOG_TAG;
                int i2 = this.mPhoneId;
                IMSLog.i(str2, i2, "The token is no longer valid, retry511Cnt: " + retry511Cnt);
                this.mSharedInfo.setInternal511ErrRetryCount(retry511Cnt);
                return getNextWorkflow(1);
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "The token is no longer valid, finish");
            return getNextWorkflow(8);
        } else if (errorCode != 403 || !(current instanceof WorkflowBase.FetchHttps)) {
            if (errorCode == 500) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "internal server error");
                int retryCnt = this.mSharedInfo.getInternalErrRetryCount() + 1;
                if (retryCnt > 1) {
                    return getNextWorkflow(8);
                }
                String str3 = LOG_TAG;
                Log.i(str3, "retryCnt: " + retryCnt);
                this.mSharedInfo.setInternalErrRetryCount(retryCnt);
                return getNextWorkflow(1);
            } else if (errorCode != 503) {
                return super.handleResponse(current, errorCode);
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "service unavailable");
                int retry503Cnt = this.mSharedInfo.getInternal503ErrRetryCount() + 1;
                if (retry503Cnt > 1) {
                    return getNextWorkflow(8);
                }
                String str4 = LOG_TAG;
                Log.i(str4, "retry503Cnt: " + retry503Cnt);
                this.mSharedInfo.setInternal503ErrRetryCount(retry503Cnt);
                sleep(1000 * getretryAfterTime());
                return getNextWorkflow(3);
            }
        } else if (this.mMobileNetwork && this.mHttpResponse != 511) {
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            IMSLog.i(LOG_TAG, this.mPhoneId, "403 received. Set version to 0. Finish");
            return getNextWorkflow(8);
        } else if (!this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "no msisdn. try to get user");
            this.mPowerController.release();
            String msisdn = this.mDialog.getMsisdn(this.mTelephony.getSimCountryCode());
            this.mPowerController.lock();
            if (TextUtils.isEmpty(msisdn)) {
                Log.i(LOG_TAG, "user didn't enter msisdn finish process");
                return getNextWorkflow(8);
            }
            this.mSharedInfo.setUserMsisdn(msisdn);
            return getNextWorkflow(1);
        } else if (TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
            return super.handleResponse(current, errorCode);
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "msisdn is wrong from user, try it again after 300 sec");
            setValidityTimer(300);
            return getNextWorkflow(8);
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        switch (type) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Initialize:");
                        WorkflowSec.this.mHttpResponse = 0;
                        WorkflowBase.Workflow next = super.run();
                        if (!(next instanceof WorkflowBase.FetchHttp)) {
                            return next;
                        }
                        if (WorkflowSec.this.mStorage.getState() != 1) {
                            IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "getNextWorkflow: mStorage is not ready");
                            IMSLog.c(LogClass.WFS_STORAGE_NOT_READY, WorkflowSec.this.mPhoneId + ",STOR_NR");
                            WorkflowSec workflowSec = WorkflowSec.this;
                            workflowSec.addEventLog(WorkflowSec.LOG_TAG + ": getNextWorkflow: mStorage is not ready");
                            return WorkflowSec.this.getNextWorkflow(8);
                        } else if (WorkflowSec.this.mMobileNetwork) {
                            return next;
                        } else {
                            Log.i(WorkflowSec.LOG_TAG, "mMobileNetwork: false, try FetchHttps step");
                            return WorkflowSec.this.getNextWorkflow(3);
                        }
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchHttp:");
                        WorkflowBase.Workflow next = super.run();
                        WorkflowSec workflowSec = WorkflowSec.this;
                        workflowSec.mHttpResponse = workflowSec.mSharedInfo.getHttpResponse().getStatusCode();
                        return next;
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        String str;
                        WorkflowSec.this.mSharedInfo.setHttpsDefault();
                        if (WorkflowSec.this.mParamHandler.isConfigProxy()) {
                            WorkflowSec.this.mSharedInfo.changeConfigProxyUriForHttp();
                            WorkflowSec.this.mSharedInfo.setHttpProxyDefault();
                        }
                        String str2 = WorkflowSec.LOG_TAG;
                        IMSLog.s(str2, "FetchHttps: NetType = " + WorkflowSec.this.mTelephony.getNetType() + ", Identity = " + WorkflowSec.this.mTelephony.getIdentityByPhoneId(WorkflowSec.this.mPhoneId) + ", SipUri = " + WorkflowSec.this.mTelephony.getSipUri());
                        WorkflowSec.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowSec.this.getVersion()));
                        WorkflowSec.this.mSharedInfo.addHttpParam("IMSI", WorkflowSec.this.mTelephony.getImsi());
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowSec.this.mTelephony.getImei());
                        WorkflowSec.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
                        SharedInfo sharedInfo = WorkflowSec.this.mSharedInfo;
                        if (WorkflowSec.this.isSmsAppDefault()) {
                            str = "1";
                        } else {
                            str = "2";
                        }
                        sharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, str);
                        if (!WorkflowSec.this.mMobileNetwork || WorkflowSec.this.mHttpResponse == 511) {
                            if (!TextUtils.isEmpty(WorkflowSec.this.mTelephony.getMsisdn())) {
                                WorkflowSec.this.mSharedInfo.addHttpParam("msisdn", WorkflowSec.this.mParamHandler.encodeRFC3986(WorkflowSec.this.mTelephony.getMsisdn()));
                            } else {
                                IMSLog.i(WorkflowSec.LOG_TAG, "FetchHttps: MSISDN is null, using the PAU");
                                String pauFromSP = ImsSharedPrefHelper.getString(WorkflowSec.this.mPhoneId, WorkflowSec.this.mContext, IConfigModule.MSISDN_FROM_PAU, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(WorkflowSec.this.mPhoneId), "");
                                WorkflowSec workflowSec = WorkflowSec.this;
                                workflowSec.addEventLog(WorkflowSec.LOG_TAG + ": pauFromSP");
                                String str3 = WorkflowSec.LOG_TAG;
                                IMSLog.s(str3, "pauFromSP: " + pauFromSP);
                                IMSLog.c(LogClass.WFS_PAU_FROM_SP, WorkflowSec.this.mPhoneId + "PAU_FROM_SP");
                                if (!TextUtils.isEmpty(pauFromSP)) {
                                    WorkflowSec.this.mSharedInfo.addHttpParam("msisdn", WorkflowSec.this.mParamHandler.encodeRFC3986(pauFromSP));
                                }
                            }
                            if (!TextUtils.isEmpty(WorkflowSec.this.mSharedInfo.getUserMsisdn())) {
                                WorkflowSec.this.mSharedInfo.addHttpParam("msisdn", WorkflowSec.this.mParamHandler.encodeRFC3986(WorkflowSec.this.mSharedInfo.getUserMsisdn()));
                            }
                            WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ConfigUtil.getSmsPort(WorkflowSec.this.mPhoneId));
                            String token = WorkflowSec.this.getToken();
                            if (TextUtils.isEmpty(token)) {
                                String lasttoken = ImsSharedPrefHelper.getString(WorkflowSec.this.mPhoneId, WorkflowSec.this.mContext, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(WorkflowSec.this.mPhoneId), "");
                                if (!TextUtils.isEmpty(lasttoken)) {
                                    Log.i(WorkflowSec.LOG_TAG, "use last valid token");
                                    token = lasttoken;
                                }
                            }
                            WorkflowSec.this.mSharedInfo.addHttpParam("token", token);
                        }
                        WorkflowSec.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
                        WorkflowSec.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowSec.this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(WorkflowSec.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
                        WorkflowSec.this.mSharedInfo.addHttpParam("rcs_version", WorkflowSec.this.mRcsVersion);
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_1.0");
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
                        WorkflowSec workflowSec2 = WorkflowSec.this;
                        workflowSec2.setRcsState(workflowSec2.convertRcsStateWithSpecificParam());
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, WorkflowSec.this.getRcsState());
                        if (WorkflowSec.this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowSec.this.getRcsState())) {
                            Log.i(WorkflowSec.LOG_TAG, "mStartForce: true, vers: 0");
                            WorkflowSec.this.mSharedInfo.addHttpParam("vers", "0");
                        }
                        if (WorkflowSec.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            String str4 = WorkflowSec.LOG_TAG;
                            Log.i(str4, "use backup version in case of dormant, vers: " + WorkflowSec.this.getVersionBackup());
                            WorkflowSec.this.mSharedInfo.addHttpParam("vers", WorkflowSec.this.getVersionBackup());
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Authorize:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public String getOtp() {
                        String otp = null;
                        String otpSmsType = ConfigUtil.getSmsType(WorkflowSec.this.mPhoneId);
                        if ("text".equals(otpSmsType)) {
                            if (WorkflowSec.this.mAuthTryCount < 1) {
                                WorkflowSec.access$308(WorkflowSec.this);
                                otp = WorkflowSec.this.mTelephony.getOtp();
                            }
                        } else if (WorkflowSec.OTP_SMS_BINARY_TYPE.equals(otpSmsType) && WorkflowSec.this.mAuthHiddenTryCount < 3) {
                            WorkflowSec.access$408(WorkflowSec.this);
                            otp = WorkflowSec.this.mTelephony.getPortOtp();
                        }
                        String str = WorkflowSec.LOG_TAG;
                        Log.i(str, "otp: " + IMSLog.checker(otp));
                        return otp;
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchOtp:");
                        WorkflowBase.Workflow next = super.run();
                        if (next instanceof WorkflowBase.Finish) {
                            WorkflowSec.this.mSharedInfo.getHttpResponse().setStatusCode(700);
                        }
                        return next;
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        WorkflowSec.this.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, WorkflowSec.this.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie"));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Parse:");
                        return super.run();
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Store:");
                        Map<String, String> parsedXml = WorkflowSec.this.mSharedInfo.getParsedXml();
                        int newVersion = WorkflowSec.this.getVersion(parsedXml);
                        boolean versionChange = WorkflowSec.this.getVersion() != newVersion;
                        String str = WorkflowSec.LOG_TAG;
                        Log.i(str, "versionChange: " + versionChange);
                        if (newVersion == 0) {
                            ((IConfigModule) WorkflowSec.this.mModuleHandler).setAcsTryReason(WorkflowSec.this.mPhoneId, DiagnosisConstants.RCSA_ATRE.VERSION_ZERO);
                        }
                        WorkflowBase.OpMode rcsDisabledState = WorkflowSec.this.getRcsDisabledState(parsedXml);
                        if (WorkflowSec.this.isValidRcsDisabledState(rcsDisabledState)) {
                            WorkflowSec.this.setOpMode(rcsDisabledState, parsedXml);
                            return WorkflowSec.this.getNextWorkflow(8);
                        }
                        WorkflowSec workflowSec = WorkflowSec.this;
                        workflowSec.setOpMode(workflowSec.getOpMode(parsedXml), parsedXml);
                        if (WorkflowSec.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                            WorkflowSec workflowSec2 = WorkflowSec.this;
                            workflowSec2.setValidityTimer(workflowSec2.getValidity());
                        }
                        return WorkflowSec.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() throws Exception {
                        if (WorkflowSec.this.mSharedInfo.getHttpResponse() != null) {
                            int errorCode = WorkflowSec.this.mSharedInfo.getHttpResponse().getStatusCode();
                            WorkflowSec.this.setLastErrorCode(errorCode);
                            WorkflowSec.this.setInfoLastErrorCode(errorCode);
                        }
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "workflow is finished");
                        return null;
                    }
                };
            default:
                return null;
        }
    }

    public void cleanup() {
        super.cleanup();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }

    public static int getAcsTriggerCount() {
        return mAcsCnt;
    }
}
