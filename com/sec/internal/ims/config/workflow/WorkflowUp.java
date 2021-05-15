package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowUp extends WorkflowUpBase {
    public static final String LOG_TAG = WorkflowUp.class.getSimpleName();
    protected int mAuthHiddenTryCount = 0;
    protected int mAuthTryCount = 0;
    protected boolean mIsReceicedXml = false;
    protected boolean mIsheaderEnrichment = false;
    protected CountDownTimer mMsisdnTimer = null;
    protected int mNewVersion = 0;
    protected int mOldVersion = 0;
    protected String mSmsPort = null;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowUp(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceUp r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceUp
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterUp r7 = new com.sec.internal.ims.config.adapters.HttpAdapterUp
            r7.<init>(r14)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r12, r13, r14)
            r0 = r15
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r10 = r20
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mMsisdnTimer = r0
            r1 = 0
            r11.mIsReceicedXml = r1
            r11.mOldVersion = r1
            r11.mNewVersion = r1
            r11.mSmsPort = r0
            r11.mAuthTryCount = r1
            r11.mAuthHiddenTryCount = r1
            r11.mIsheaderEnrichment = r1
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUp.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + msg.what);
        addEventLog(LOG_TAG + "handleMessage: " + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 != 5) {
                if (i2 != 7) {
                    super.handleMessage(msg);
                    return;
                }
                Log.i(LOG_TAG, "show MSISDN dialog,");
                sendEmptyMessage(1);
                return;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                Log.i(LOG_TAG, "sms default application is changed to samsung");
                setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
                setRcsDisabledState("");
                setValidity(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                cancelValidityTimer();
                setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                return;
            } else {
                Log.i(LOG_TAG, "sms default application is changed to non-samsung");
                setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                sendEmptyMessage(1);
                return;
            }
        }
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "AutoConfig: ongoing");
            return;
        }
        this.sIsConfigOngoing = true;
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: start");
        this.mIsReceicedXml = false;
        this.mMsisdnHandler.setMsisdnTimer(this.mMsisdnTimer);
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mSmsPort = null;
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mSharedInfo.setInternalErrRetryCount(0);
        this.mPowerController.lock();
        this.mOldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(this.mOldVersion);
        }
        this.mNewVersion = getVersion();
        String str2 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str2, i3, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
        setCompleted(true);
        String str3 = LOG_TAG;
        Log.i(str3, "mIsReceicedXml: " + this.mIsReceicedXml);
        if (this.mIsReceicedXml) {
            this.mMsisdnHandler.cancelMsisdnTimer(this.mMsisdnTimer, true);
            this.mIsReceicedXml = false;
        }
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.sIsConfigOngoing = false;
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize(this, (AnonymousClass1) null);
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException: " + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec and retry");
                sleep(10000);
                next = new Initialize(this, (AnonymousClass1) null);
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                String message = e2.getMessage();
                Log.i(LOG_TAG, "UnknownStatusException: " + message);
                if ("body and cookie are null".equals(message)) {
                    next = new Finish(this, (AnonymousClass1) null);
                    e2.printStackTrace();
                } else {
                    Log.i(LOG_TAG, "wait 2 sec and retry");
                    sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    next = new Initialize(this, (AnonymousClass1) null);
                    e2.printStackTrace();
                }
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "finish workflow");
                next = new Finish(this, (AnonymousClass1) null);
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                next = new Initialize(this, (AnonymousClass1) null);
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0075, code lost:
        if (getVersion(r9) > 0) goto L_0x0077;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isDataFullUpdateNeeded(java.util.Map<java.lang.String, java.lang.String> r9) {
        /*
            r8 = this;
            int r0 = r8.mPhoneId
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)
            java.lang.String r1 = "root/application/1/services/ChatAuth"
            java.util.Locale r2 = java.util.Locale.US
            java.lang.String r2 = r1.toLowerCase(r2)
            java.lang.Object r2 = r9.get(r2)
            java.lang.String r2 = (java.lang.String) r2
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "startForce = "
            r4.append(r5)
            boolean r5 = r8.mStartForce
            r4.append(r5)
            java.lang.String r5 = ", isRcsByUser = "
            r4.append(r5)
            com.sec.internal.ims.config.SharedInfo r5 = r8.mSharedInfo
            boolean r5 = r5.isRcsByUser()
            r4.append(r5)
            java.lang.String r5 = ", rcsState = "
            r4.append(r5)
            java.lang.String r5 = r8.getRcsState()
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r3, r4)
            int r3 = r8.getVersion()
            int r4 = r8.getVersion(r9)
            r5 = 2
            r6 = 1
            r7 = 0
            if (r3 < r4) goto L_0x0077
            boolean r3 = r8.mStartForce
            if (r3 == 0) goto L_0x0061
            com.sec.internal.ims.config.SharedInfo r3 = r8.mSharedInfo
            boolean r3 = r3.isRcsByUser()
            if (r3 == 0) goto L_0x0077
        L_0x0061:
            com.sec.internal.constants.Mno[] r3 = new com.sec.internal.constants.Mno[r5]
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.SWISSCOM
            r3[r7] = r4
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.MTS_RUSSIA
            r3[r6] = r4
            boolean r3 = r0.isOneOf(r3)
            if (r3 == 0) goto L_0x00a2
            int r3 = r8.getVersion(r9)
            if (r3 <= 0) goto L_0x00a2
        L_0x0077:
            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r3 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_RCS_BY_USER
            int r3 = r3.value()
            java.lang.String r3 = java.lang.String.valueOf(r3)
            java.lang.String r4 = r8.getRcsState()
            boolean r3 = r3.equals(r4)
            if (r3 != 0) goto L_0x00a2
            com.sec.internal.constants.Mno[] r3 = new com.sec.internal.constants.Mno[r5]
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.SWISSCOM
            r3[r7] = r4
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.MTS_RUSSIA
            r3[r6] = r4
            boolean r3 = r0.isOneOf(r3)
            if (r3 == 0) goto L_0x00a1
            boolean r3 = android.text.TextUtils.isEmpty(r2)
            if (r3 != 0) goto L_0x00a2
        L_0x00a1:
            return r6
        L_0x00a2:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUp.isDataFullUpdateNeeded(java.util.Map):boolean");
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> data) {
        IMSLog.i(LOG_TAG, "Update of client configuration control parameters");
        setValidity(getValidity(data));
        String token = getToken(data);
        if (!TextUtils.isEmpty(token)) {
            setToken(token);
        }
    }

    /* access modifiers changed from: protected */
    public void setDisableRcsByUserOpMode() {
        super.setDisableRcsByUserOpMode();
        this.mSharedInfo.setRcsByUser(true);
    }

    /* access modifiers changed from: protected */
    public void setEnableRcsByUserOpMode() {
        super.setEnableRcsByUserOpMode();
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            this.mSharedInfo.setRcsByUser(true);
        }
        ImsRegistry.getConfigModule().getAcsConfig(this.mPhoneId).disableRcsByAcs(false);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    private class Initialize implements WorkflowBase.Workflow {
        private Initialize() {
        }

        /* synthetic */ Initialize(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow next = null;
            WorkflowUp.this.mSharedInfo.setUrl(WorkflowUp.this.mParamHandler.initUrl());
            WorkflowUp.this.mCookieHandler.clearCookie();
            if (WorkflowUp.this.mStartForce) {
                next = new FetchHttp(WorkflowUp.this, (AnonymousClass1) null);
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowUp.this.getOpMode().ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    next = new FetchHttp(WorkflowUp.this, (AnonymousClass1) null);
                } else if (i == 4 || i == 5) {
                    next = new Finish(WorkflowUp.this, (AnonymousClass1) null);
                }
            }
            if (!(next instanceof FetchHttp) || WorkflowUp.this.mMobileNetwork) {
                return next;
            }
            Log.i(WorkflowUp.LOG_TAG, "mMobileNetwork: false, try FetchHttps step");
            return new FetchHttps(WorkflowUp.this, (AnonymousClass1) null);
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowUp$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
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
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private class FetchHttp implements WorkflowBase.Workflow {
        private FetchHttp() {
        }

        /* synthetic */ FetchHttp(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.mSharedInfo.setHttpDefault();
            WorkflowUp.this.mSharedInfo.setHttpResponse(WorkflowUp.this.getHttpResponse());
            if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 200 || WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                    WorkflowUp.this.mIsheaderEnrichment = true;
                }
                return new FetchHttps(WorkflowUp.this, (AnonymousClass1) null);
            }
            WorkflowUp workflowUp = WorkflowUp.this;
            return workflowUp.handleResponseForUp(new Initialize(workflowUp, (AnonymousClass1) null), new FetchHttps(WorkflowUp.this, (AnonymousClass1) null), new Finish(WorkflowUp.this, (AnonymousClass1) null));
        }
    }

    private class FetchHttps implements WorkflowBase.Workflow {
        private FetchHttps() {
        }

        /* synthetic */ FetchHttps(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.setSharedInfoWithParamForUp();
            if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                Log.i(WorkflowUp.LOG_TAG, "200 OK is received, try to parse");
                return new Parse(WorkflowUp.this, (AnonymousClass1) null);
            }
            if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowUp.this.getRcsState())) {
                    return new Finish(WorkflowUp.this, (AnonymousClass1) null);
                }
                if (WorkflowUp.this.mMobileNetwork && !WorkflowUp.this.mIsheaderEnrichment) {
                    WorkflowUp.this.setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                    Log.i(WorkflowUp.LOG_TAG, "403 is received, set version to zero");
                    return new Finish(WorkflowUp.this, (AnonymousClass1) null);
                } else if (!WorkflowUp.this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    return WorkflowUp.this.getMsisdnWithDialog();
                } else {
                    if (!TextUtils.isEmpty(WorkflowUp.this.mSharedInfo.getUserMsisdn())) {
                        Log.i(WorkflowUp.LOG_TAG, "msisdn is wrong from user, try it again after 300 sec");
                        WorkflowUp.this.setValidityTimer(300);
                        return new Finish(WorkflowUp.this, (AnonymousClass1) null);
                    }
                }
            }
            WorkflowUp workflowUp = WorkflowUp.this;
            return workflowUp.handleResponseForUp(new Initialize(workflowUp, (AnonymousClass1) null), new FetchHttps(), new Finish(WorkflowUp.this, (AnonymousClass1) null));
        }
    }

    /* access modifiers changed from: private */
    public WorkflowBase.Workflow getMsisdnWithDialog() {
        String msisdn;
        if (this.mMobileNetwork || this.mMsisdnHandler.getMsisdnSkipCount() != 3) {
            Log.i(LOG_TAG, "no msisdn, try to get user");
            if (this.mMsisdnHandler.getMsisdnSkipCount() == -1) {
                this.mMsisdnHandler.setMsisdnSkipCount(0);
            }
            this.mPowerController.release();
            if (!TextUtils.isEmpty(this.mMsisdnHandler.getLastMsisdnValue())) {
                msisdn = this.mDialog.getMsisdn(this.mTelephony.getSimCountryCode(), this.mMsisdnHandler.getLastMsisdnValue());
            } else {
                msisdn = this.mDialog.getMsisdn(this.mTelephony.getSimCountryCode());
            }
            this.mPowerController.lock();
            if (TextUtils.isEmpty(msisdn)) {
                Log.i(LOG_TAG, "user didn't enter msisdn finish process");
                return new Finish(this, (AnonymousClass1) null);
            } else if ("skip".equals(msisdn)) {
                this.mMsisdnHandler.setMsisdnSkipCount(this.mMsisdnHandler.getMsisdnSkipCount() + 1);
                Log.i(LOG_TAG, "user enter skip msisdn.");
                this.mMsisdnHandler.setMsisdnMsguiDisplay(CloudMessageProviderContract.JsonData.TRUE);
                Intent sendIntent = new Intent();
                WorkflowMsisdnHandler workflowMsisdnHandler = this.mMsisdnHandler;
                sendIntent.setAction("com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG");
                WorkflowMsisdnHandler workflowMsisdnHandler2 = this.mMsisdnHandler;
                sendIntent.putExtra("isNeeded", this.mMsisdnHandler.getIsNeeded());
                ContextExt.sendBroadcastAsUser(this.mContext, sendIntent, ContextExt.ALL);
                return new Finish(this, (AnonymousClass1) null);
            } else {
                this.mSharedInfo.setUserMsisdn(msisdn);
                return new Initialize(this, (AnonymousClass1) null);
            }
        } else {
            Log.i(LOG_TAG, "Retry counter for msisdn reached. Abort.");
            return new Finish(this, (AnonymousClass1) null);
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithParamForUp() {
        String str;
        Mno mno = SimUtil.getMno(this.mPhoneId);
        if (ConfigUtil.shallUsePreviousCookie(this.mLastErrorCode, mno)) {
            this.mSharedInfo.setHttpsWithPreviousCookies();
        } else {
            this.mSharedInfo.setHttpsDefault();
        }
        if (this.mParamHandler.isConfigProxy()) {
            this.mSharedInfo.changeConfigProxyUriForHttp();
            this.mSharedInfo.setHttpProxyDefault();
        }
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (cm.getAcsConfig(this.mPhoneId).isTriggeredByNrcr() && mno == Mno.SWISSCOM) {
            setRcsState(String.valueOf(getVersion()));
            setRcsDisabledState("");
        }
        this.mCookieHandler.handleCookie(this.mSharedInfo.getHttpResponse());
        this.mSharedInfo.addHttpParam("vers", String.valueOf(getVersion()));
        this.mSharedInfo.addHttpParam("terminal_model", ConfigContract.BUILD.getTerminalModel());
        this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
        this.mSharedInfo.addHttpParam("terminal_sw_version", this.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
        this.mSharedInfo.addHttpParam("IMSI", this.mTelephony.getSubscriberId(SimUtil.getSubId(this.mPhoneId)));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, this.mTelephony.getDeviceId(this.mPhoneId));
        SharedInfo sharedInfo = this.mSharedInfo;
        if (isSmsAppDefault()) {
            str = "1";
        } else {
            str = "2";
        }
        sharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, str);
        this.mSharedInfo.addHttpParam("rcs_version", this.mRcsVersion);
        String rcsProfile = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, ImsRegistry.getRcsProfileType(this.mPhoneId));
        String str2 = LOG_TAG;
        Log.i(str2, "rcsProfile read and used : " + rcsProfile);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, rcsProfile);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, this.mRcsProvisioningVersion);
        if (!ConfigUtil.doesUpRcsProfileMatchProvisioningVersion(rcsProfile, this.mRcsProvisioningVersion)) {
            String str3 = LOG_TAG;
            Log.w(str3, "Provisioning version <-> RCS profile mismatch. Rcs profile is: " + rcsProfile + " Provisioning version is: " + this.mRcsProvisioningVersion);
        }
        if (ImsProfile.isRcsUp2Profile(rcsProfile) && !this.mRcsAppList.isEmpty()) {
            this.mSharedInfo.addHttpParam("app", String.join("||", this.mRcsAppList));
        }
        setRcsState(convertRcsStateWithSpecificParam());
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, getRcsState());
        setSharedInfoWithAuthParamForUp();
        if (this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()) && (!cm.getAcsConfig(this.mPhoneId).isTriggeredByNrcr() || mno != Mno.MTS_RUSSIA)) {
            Log.i(LOG_TAG, "mStartForce: true, vers: 0");
            this.mSharedInfo.addHttpParam("vers", "0");
        }
        if (getOpMode() == WorkflowBase.OpMode.DORMANT) {
            String str4 = LOG_TAG;
            Log.i(str4, "use backup version in case of dormant, vers: " + getVersionBackup());
            this.mSharedInfo.addHttpParam("vers", getVersionBackup());
        }
        this.mSharedInfo.setHttpResponse(getHttpResponse());
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithAuthParamForUp() {
        if (!this.mMobileNetwork || this.mIsheaderEnrichment || this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
            if (!TextUtils.isEmpty(this.mTelephony.getMsisdn(SimUtil.getSubId(this.mPhoneId)))) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(ImsCallUtil.validatePhoneNumber(this.mTelephony.getMsisdn(SimUtil.getSubId(this.mPhoneId)), this.mTelephony.getSimCountryCode())));
            }
            if (!TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(this.mSharedInfo.getUserMsisdn()));
            }
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.SMS_DEST_PORT, this.mTelephony.getSmsDestPort()));
            this.mSharedInfo.addHttpParam("token", getToken());
        }
    }

    private class FetchOtp implements WorkflowBase.Workflow {
        private FetchOtp() {
        }

        /* synthetic */ FetchOtp(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.mSharedInfo.setHttpClean();
            if (WorkflowUp.this.mMno.isEur() || WorkflowUp.this.mMno.isSea() || WorkflowUp.this.mMno.isOce() || WorkflowUp.this.mMno.isMea() || WorkflowUp.this.mMno.isSwa()) {
                WorkflowUp.this.mCookieHandler.handleCookie(WorkflowUp.this.mSharedInfo.getHttpResponse());
            } else {
                WorkflowUp.this.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, WorkflowUp.this.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie"));
            }
            WorkflowUp.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowUp.this.mSharedInfo.getOtp());
            WorkflowUp.this.mSharedInfo.setHttpResponse(WorkflowUp.this.getHttpResponse());
            if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse(WorkflowUp.this, (AnonymousClass1) null);
            }
            WorkflowUp workflowUp = WorkflowUp.this;
            return workflowUp.handleResponseForUp(new Initialize(workflowUp, (AnonymousClass1) null), new FetchHttps(WorkflowUp.this, (AnonymousClass1) null), new Finish(WorkflowUp.this, (AnonymousClass1) null));
        }
    }

    private class Authorize implements WorkflowBase.Workflow {
        private Authorize() {
        }

        /* synthetic */ Authorize(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            String otp;
            WorkflowUp.this.mPowerController.release();
            String prevOtp = WorkflowUp.this.mSharedInfo.getOtp();
            if ("0".equals(WorkflowUp.this.mSmsPort)) {
                otp = WorkflowUp.this.mTelephony.getExistingOtp();
                if ((otp == null || TextUtils.equals(otp, prevOtp)) && WorkflowUp.this.mAuthTryCount < 1) {
                    WorkflowUp.this.mAuthTryCount++;
                    otp = WorkflowUp.this.mTelephony.getOtp();
                }
            } else {
                otp = WorkflowUp.this.mTelephony.getExistingPortOtp();
                if ((otp == null || TextUtils.equals(otp, prevOtp)) && WorkflowUp.this.mAuthHiddenTryCount < 3) {
                    WorkflowUp.this.mAuthHiddenTryCount++;
                    otp = WorkflowUp.this.mTelephony.getPortOtp();
                    if (otp == null) {
                        WorkflowUp.this.setValidityTimer(0);
                    }
                }
            }
            if (otp != null) {
                Log.i(WorkflowUp.LOG_TAG, "otp: " + IMSLog.checker(otp));
                WorkflowUp.this.mSharedInfo.setOtp(otp);
                WorkflowUp.this.mPowerController.lock();
                return new FetchOtp(WorkflowUp.this, (AnonymousClass1) null);
            }
            Log.i(WorkflowUp.LOG_TAG, "otp: null, go to finish state");
            return new Finish(WorkflowUp.this, (AnonymousClass1) null);
        }
    }

    private class Parse implements WorkflowBase.Workflow {
        private Parse() {
        }

        /* synthetic */ Parse(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowUp.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parsedXml = WorkflowUp.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parsedXml == null) {
                throw new InvalidXmlException("parsedXml is null");
            } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                Log.i(WorkflowUp.LOG_TAG, "parsedXml need to contain version, validity items");
                if (WorkflowUp.this.mCookieHandler.isCookie(WorkflowUp.this.mSharedInfo.getHttpResponse())) {
                    WorkflowUp.this.mSmsPort = parsedXml.get(ConfigConstants.PATH.POLICY_SMS_PORT);
                    return new Authorize(WorkflowUp.this, (AnonymousClass1) null);
                }
                throw new UnknownStatusException("body and cookie are null");
            } else {
                WorkflowUp.this.mIsReceicedXml = true;
                WorkflowUp.this.mSharedInfo.setParsedXml(parsedXml);
                WorkflowUp.this.mMsisdnHandler.setMsisdnValue(WorkflowUp.this.mSharedInfo.getUserMsisdn());
                return new Store(WorkflowUp.this, (AnonymousClass1) null);
            }
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        /* synthetic */ Store(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            Mno mno = SimUtil.getSimMno(WorkflowUp.this.mPhoneId);
            if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowUp.this.getRcsState()) && mno != Mno.TELEFONICA_GERMANY && mno != Mno.TELEFONICA_SPAIN && mno != Mno.TELEFONICA_UK) {
                return new Finish(WorkflowUp.this, (AnonymousClass1) null);
            }
            WorkflowUp workflowUp = WorkflowUp.this;
            WorkflowBase.OpMode rcsDisabledState = workflowUp.getRcsDisabledState(workflowUp.mSharedInfo.getParsedXml());
            if (WorkflowUp.this.isValidRcsDisabledState(rcsDisabledState)) {
                WorkflowUp workflowUp2 = WorkflowUp.this;
                workflowUp2.setOpMode(rcsDisabledState, workflowUp2.mSharedInfo.getParsedXml());
                return new Finish(WorkflowUp.this, (AnonymousClass1) null);
            }
            boolean userAccept = WorkflowUp.this.mParamHandler.getUserAccept(WorkflowUp.this.mSharedInfo.getParsedXml());
            WorkflowUp.this.mParamHandler.setOpModeWithUserAccept(userAccept, WorkflowUp.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowUp.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowUp workflowUp3 = WorkflowUp.this;
                workflowUp3.setValidityTimer(workflowUp3.getValidity());
            }
            WorkflowUp.this.mMsisdnHandler.setMsisdnSkipCount(0);
            WorkflowUp.this.setTcUserAccept(userAccept);
            return new Finish(WorkflowUp.this, (AnonymousClass1) null);
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        /* synthetic */ Finish(WorkflowUp x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowUp.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowUp workflowUp = WorkflowUp.this;
                workflowUp.setLastErrorCode(workflowUp.mSharedInfo.getHttpResponse().getStatusCode());
            }
            WorkflowUp.this.mSharedInfo.setRcsByUser(false);
            Log.i(WorkflowUp.LOG_TAG, "all workflow is finished");
            return null;
        }
    }
}
