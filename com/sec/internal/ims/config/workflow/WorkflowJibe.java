package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Map;

public class WorkflowJibe extends WorkflowUpBase {
    protected static final int HTTPERR_RETRY_AFTER_TIME = 10;
    protected static final int HTTPERR_TRY_MAX_COUNT = 2;
    protected static final String LOG_TAG = WorkflowJibe.class.getSimpleName();
    protected static final int MSISDN_TRY_MAX_COUNT = 1;
    protected static final int MSISDN_TRY_MAX_COUNT_NON_GC = 3;
    protected static final String OTP_SMS_BINARY_TYPE = "binary";
    protected static final String OTP_SMS_TEXT_TYPE = "text";
    protected static final int OTP_SMS_TIME_OUT = 700;
    protected int m503ErrCount = 0;
    protected int m511ErrCount = 0;
    protected int mAuthHiddenTryCount = 0;
    protected int mAuthTryCount = 0;
    protected int mHttpResponse = 0;
    protected boolean mIsEnrichedHeaderFailed = false;
    protected boolean mIsMobileConfigCompleted = false;
    protected boolean mIsMobileConfigNeeded = false;
    protected boolean mIsMobileConnected = false;
    protected boolean mIsMobileRequested = false;
    protected boolean mIsWifiConnected = false;
    private final ConnectivityManager.NetworkCallback mMobileStateCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            WorkflowJibe.this.onMobileConnectionChanged(network, true);
        }

        public void onLost(Network network) {
            WorkflowJibe.this.onMobileConnectionChanged(network, false);
        }
    };
    protected int mMsisdnTryCount = 0;
    protected int mNewVersion = 0;
    protected int mOldVersion = 0;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowJibe(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, int r20) {
        /*
            r15 = this;
            r11 = r15
            r12 = r17
            r13 = r18
            r14 = r20
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceJibe r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceJibe
            r5.<init>(r12, r13, r14)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec r7 = new com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec
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
            r11.mOldVersion = r0
            r11.mNewVersion = r0
            r11.mIsMobileRequested = r0
            r11.mIsMobileConnected = r0
            r11.mIsWifiConnected = r0
            r11.mIsMobileConfigNeeded = r0
            r11.mIsMobileConfigCompleted = r0
            r11.mIsEnrichedHeaderFailed = r0
            r11.mHttpResponse = r0
            r11.mAuthTryCount = r0
            r11.mAuthHiddenTryCount = r0
            r11.mMsisdnTryCount = r0
            r11.m511ErrCount = r0
            r11.m503ErrCount = r0
            com.sec.internal.ims.config.workflow.WorkflowJibe$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowJibe$1
            r0.<init>()
            r11.mMobileStateCallback = r0
            android.content.Context r0 = r11.mContext
            java.lang.String r1 = "connectivity"
            java.lang.Object r0 = r0.getSystemService(r1)
            android.net.ConnectivityManager r0 = (android.net.ConnectivityManager) r0
            r11.mConnectivityManager = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowJibe.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message msg) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + msg.what);
        int i2 = msg.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 3) {
                removeMessages(4);
                if (this.mIsMobileConfigCompleted) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "mIsMobileConfigCompleted: " + this.mIsMobileConfigCompleted);
                    return;
                }
                this.mPowerController.lock();
                executeAutoConfig();
                this.mIsMobileConfigCompleted = true;
                unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                this.mNewVersion = getVersion();
                endAutoConfig(true);
                String str3 = LOG_TAG;
                int i3 = this.mPhoneId;
                IMSLog.i(str3, i3, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
                this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                this.mPowerController.release();
                return;
            } else if (i2 != 4) {
                if (i2 != 5) {
                    super.handleMessage(msg);
                    return;
                } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "sms default application is changed to samsung");
                    setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                    setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
                    setRcsDisabledState("");
                    setValidity(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                    cancelValidityTimer();
                    setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
                    return;
                } else {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "sms default application is changed to non-samsung");
                    setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                    removeMessages(1);
                    sendEmptyMessage(1);
                    return;
                }
            } else if (this.mIsMobileConfigCompleted) {
                String str4 = LOG_TAG;
                Log.i(str4, "mIsMobileConfigCompleted: " + this.mIsMobileConfigCompleted);
                return;
            } else {
                this.mPowerController.lock();
                changeOpMode(true);
                unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                this.mNewVersion = getVersion();
                endAutoConfig(false);
                String str5 = LOG_TAG;
                int i4 = this.mPhoneId;
                IMSLog.i(str5, i4, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
                this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                this.mPowerController.release();
                return;
            }
        }
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "AutoConfig: ongoing");
            return;
        }
        this.sIsConfigOngoing = true;
        String str6 = LOG_TAG;
        int i5 = this.mPhoneId;
        IMSLog.i(str6, i5, "AutoConfig: start, mStartForce: " + this.mStartForce);
        this.mModuleHandler.removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mPowerController.lock();
        initAutoConfig();
        int version = getVersion();
        this.mOldVersion = version;
        if (scheduleAutoconfigForJibe(version)) {
            String str7 = LOG_TAG;
            Log.i(str7, "mIsWifiConnected: " + this.mIsWifiConnected + " mIsMobileConfigNeeded: " + this.mIsMobileConfigNeeded);
            if (!this.mIsWifiConnected || !this.mIsMobileConfigNeeded) {
                executeAutoConfig();
            } else {
                Log.i(LOG_TAG, "use mobile network");
                this.mIsMobileRequested = true;
                this.mNetworkRequest = new NetworkRequest.Builder().addTransportType(0).addCapability(12).build();
                registerMobileNetwork(this.mConnectivityManager, this.mNetworkRequest, this.mMobileStateCallback);
                removeMessages(4);
                sendMessageDelayed(obtainMessage(4), 60000);
                this.mPowerController.release();
                return;
            }
        }
        this.mNewVersion = getVersion();
        endAutoConfig(true);
        String str8 = LOG_TAG;
        int i6 = this.mPhoneId;
        IMSLog.i(str8, i6, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig: finish");
        this.mModuleHandler.sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mPowerController.release();
    }

    public void onMobileConnectionChanged(Network network, boolean isAvailable) {
        if (isAvailable) {
            if (this.mIsMobileRequested && !this.mIsMobileConnected) {
                Log.i(LOG_TAG, "onMobileConnectionChanged: onAvailable");
                if (network != null) {
                    Log.i(LOG_TAG, "mobile connection is successful");
                    this.mNetwork = network;
                    this.mIsMobileConnected = true;
                    sendEmptyMessage(3);
                    return;
                }
                Log.i(LOG_TAG, "mobile connection info is empty");
            }
        } else if (this.mIsMobileRequested) {
            Log.i(LOG_TAG, "onMobileConnectionChanged: onLost");
            this.mIsMobileConnected = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfigForJibe(int currentVersion) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfigForJibe");
        if (!needScheduleAutoconfig(this.mPhoneId)) {
            Log.i(LOG_TAG, "needScheduleAutoconfig: false");
            return false;
        } else if (this.mStartForce) {
            cancelValidityTimer();
            Log.i(LOG_TAG, "force autoconfig");
            return true;
        } else if (currentVersion == -1 || currentVersion == -2) {
            String str = LOG_TAG;
            Log.i(str, "currentVersion: " + currentVersion + " skip autoconfig");
            return false;
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            String str2 = LOG_TAG;
            Log.i(str2, "nextAutoconfigTime: " + nextAutoconfigTime);
            int remainValidity = (int) ((nextAutoconfigTime - new Date().getTime()) / 1000);
            String str3 = LOG_TAG;
            Log.i(str3, "remainValidity: " + remainValidity);
            if (remainValidity <= 0) {
                Log.i(LOG_TAG, "need autoconfig");
                return true;
            }
            if (nextAutoconfigTime > 0) {
                String str4 = LOG_TAG;
                Log.i(str4, "autoconfig schedule: after " + remainValidity + " seconds");
                IMSLog.c(LogClass.WFJ_VALIDITY_NON_EXPIRED, this.mPhoneId + ",VNE:" + remainValidity);
                addEventLog(LOG_TAG + ": autoconfig schedule: after " + remainValidity + " seconds");
                setValidityTimer(remainValidity);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void initAutoConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "initAutoConfig");
        this.mNetwork = null;
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsWifiConnected = checkWifiConnection(this.mConnectivityManager);
        if (this.mMno == Mno.SPRINT) {
            this.mIsMobileConfigNeeded = true;
        }
        this.mIsMobileConfigCompleted = false;
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mMsisdnTryCount = 0;
    }

    /* access modifiers changed from: protected */
    public void executeAutoConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "executeAutoConfig");
        work();
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig(boolean result) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "endAutoConfig: result: " + result);
        boolean validRcsDisabledState = isValidRcsDisabledState(getRcsDisabledState());
        if (result && this.mOldVersion >= 0 && !validRcsDisabledState) {
            this.mTelephony.notifyAutoConfigurationListener(52, this.mNewVersion > 0);
        } else if (!result && this.mOldVersion >= 0 && !validRcsDisabledState) {
            this.mTelephony.notifyAutoConfigurationListener(52, this.mOldVersion > 0);
        }
        if (this.mSharedInfo.getHttpResponse() != null) {
            setLastErrorCode(this.mSharedInfo.getHttpResponse().getStatusCode());
        }
        setCompleted(true);
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsMobileConfigNeeded = false;
        this.mStartForce = false;
        this.sIsConfigOngoing = false;
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
                Log.i(LOG_TAG, "SQLiteFullException occur: " + e3.getMessage());
                next = getNextWorkflow(8);
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur: " + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                next = getNextWorkflow(1);
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "getHttpResponse");
        this.mHttp.close();
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setContext(this.mContext);
        String str = LOG_TAG;
        Log.i(str, "mIsMobileConfigNeeded: " + this.mIsMobileConfigNeeded + ", mIsMobileRequested: " + this.mIsMobileRequested + ", mIsMobileConnected: " + this.mIsMobileConnected);
        if (!this.mIsMobileConfigNeeded || !this.mIsMobileRequested || !this.mIsMobileConnected || !checkMobileConnection(this.mConnectivityManager)) {
            Log.i(LOG_TAG, "set network to default network");
            this.mHttp.setNetwork((Network) null);
        } else {
            Log.i(LOG_TAG, "set network to mobile network");
            this.mHttp.setNetwork(this.mNetwork);
        }
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response response = this.mHttp.request();
        this.mHttp.close();
        return response;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow current, int errorCode) throws InvalidHeaderException, UnknownStatusException {
        String msisdn;
        IMSLog.i(LOG_TAG, this.mPhoneId, "handleResponse: " + errorCode);
        addEventLog(LOG_TAG + "handleResponse: " + errorCode);
        setLastErrorCode(errorCode);
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode != 0) {
            if (lastErrorCode == 200) {
                this.m511ErrCount = 0;
                this.m503ErrCount = 0;
            } else if (lastErrorCode != 403) {
                if (lastErrorCode == 503) {
                    long retryAfter = getretryAfterTime();
                    Log.i(LOG_TAG, "m503ErrCount: " + this.m503ErrCount + " retryAfterTime: " + retryAfter);
                    int i = this.m503ErrCount;
                    if (i < 2) {
                        this.m503ErrCount = i + 1;
                        Log.i(LOG_TAG, "retry after " + retryAfter + " sec");
                        setValidityTimer((int) retryAfter);
                        setNextAutoconfigTime((long) ((int) retryAfter));
                    }
                    return getNextWorkflow(8);
                } else if (lastErrorCode == 511) {
                    if (current instanceof WorkflowBase.FetchHttp) {
                        return getNextWorkflow(3);
                    }
                    Log.i(LOG_TAG, "The token isn't valid: m511ErrCount: " + this.m511ErrCount);
                    setToken("");
                    int i2 = this.m511ErrCount;
                    if (i2 < 2) {
                        this.m511ErrCount = i2 + 1;
                        Log.i(LOG_TAG, "retry after 10 sec");
                        setValidityTimer(10);
                        setNextAutoconfigTime(10);
                    }
                    return getNextWorkflow(8);
                }
            } else if (!(current instanceof WorkflowBase.FetchHttps)) {
                return getNextWorkflow(8);
            } else {
                Log.i(LOG_TAG, "403 is received, mMsisdnTryCount:" + this.mMsisdnTryCount);
                if (isMsisdnForGcNeeded()) {
                    this.mMsisdnTryCount++;
                    if (this.mMobileNetwork || this.mMsisdnHandler.getMsisdnSkipCount() != 3) {
                        msisdn = getMsisdnForGc();
                        if ("skip".equals(msisdn)) {
                            this.mMsisdnHandler.setMsisdnSkipCount(this.mMsisdnHandler.getMsisdnSkipCount() + 1);
                            Log.i(LOG_TAG, "user enter skip msisdn.");
                            this.mMsisdnHandler.setMsisdnMsguiDisplay(CloudMessageProviderContract.JsonData.TRUE);
                            Intent sendIntent = new Intent();
                            WorkflowMsisdnHandler workflowMsisdnHandler = this.mMsisdnHandler;
                            sendIntent.setAction("com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG");
                            WorkflowMsisdnHandler workflowMsisdnHandler2 = this.mMsisdnHandler;
                            sendIntent.putExtra("isNeeded", this.mMsisdnHandler.getIsNeeded());
                            ContextExt.sendBroadcastAsUser(this.mContext, sendIntent, ContextExt.ALL);
                            return getNextWorkflow(8);
                        }
                    } else {
                        Log.i(LOG_TAG, "Retry counter for msisdn reached. Abort.");
                        return getNextWorkflow(8);
                    }
                } else {
                    msisdn = getMsisdnForJibe();
                }
                this.mSharedInfo.setUserMsisdn(msisdn);
                if (TextUtils.isEmpty(msisdn)) {
                    Log.i(LOG_TAG, "msisdn doesn't exist");
                    return getNextWorkflow(8);
                }
                Log.i(LOG_TAG, "msisdn exists");
                return getNextWorkflow(1);
            }
            return super.handleResponse(current, errorCode);
        } else if (this.mMno != Mno.SPRINT || !(current instanceof WorkflowBase.FetchHttp) || this.mIsEnrichedHeaderFailed) {
            return super.handleResponse(current, errorCode);
        } else {
            Log.i(LOG_TAG, "http enriched header is failed, retry it with default header");
            this.mIsEnrichedHeaderFailed = true;
            return getNextWorkflow(2);
        }
    }

    private String getMsisdnForJibe() {
        int i = this.mMsisdnTryCount;
        if (i >= 1) {
            return null;
        }
        this.mMsisdnTryCount = i + 1;
        Log.i(LOG_TAG, "need msisdn from telephony/application");
        this.mPowerController.release();
        String msisdn = this.mTelephony.getMsisdnNumber();
        Log.i(LOG_TAG, "receive msisdn from telephony/application");
        String str = LOG_TAG;
        IMSLog.s(str, "msisdn: " + msisdn);
        this.mPowerController.lock();
        return msisdn;
    }

    private boolean isMsisdnForGcNeeded() {
        return this.mMsisdnTryCount < 3 && !ConfigUtil.isRcsPreConsent(this.mContext, this.mPhoneId) && (this.mMno.isEur() || this.mMno.isSea() || this.mMno.isOce() || this.mMno.isMea() || this.mMno.isSwa() || this.mMno == Mno.CLARO_DOMINICAN);
    }

    private String getMsisdnForGc() {
        String msisdn;
        if (this.mMsisdnHandler.getMsisdnSkipCount() == -1) {
            this.mMsisdnHandler.setMsisdnSkipCount(0);
        }
        this.mPowerController.release();
        if (!TextUtils.isEmpty(this.mMsisdnHandler.getLastMsisdnValue())) {
            msisdn = this.mDialog.getMsisdn(this.mTelephony.getSimCountryCode(), this.mMsisdnHandler.getLastMsisdnValue());
        } else {
            msisdn = this.mDialog.getMsisdn(this.mTelephony.getSimCountryCode());
        }
        String str = LOG_TAG;
        IMSLog.s(str, "msisdn: " + msisdn);
        this.mPowerController.lock();
        return msisdn;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        switch (type) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Initialize:");
                        WorkflowBase.Workflow next = super.run();
                        if (!(next instanceof WorkflowBase.FetchHttp) || WorkflowJibe.this.mMobileNetwork || WorkflowJibe.this.mIsMobileConfigNeeded) {
                            return next;
                        }
                        Log.i(WorkflowJibe.LOG_TAG, "mMobileNetwork: false, try FetchHttps step");
                        return WorkflowJibe.this.getNextWorkflow(3);
                    }

                    /* access modifiers changed from: protected */
                    public void init() throws NoInitialDataException {
                        WorkflowJibe.this.mIsEnrichedHeaderFailed = false;
                        WorkflowJibe.this.mHttpResponse = 0;
                        super.init();
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "FetchHttp:");
                        WorkflowBase.Workflow next = super.run();
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        workflowJibe.mHttpResponse = workflowJibe.mSharedInfo.getHttpResponse().getStatusCode();
                        return next;
                    }

                    /* access modifiers changed from: protected */
                    public void setHttpHeader() {
                        if (WorkflowJibe.this.mMno == Mno.SPRINT) {
                            String str = WorkflowJibe.LOG_TAG;
                            Log.i(str, "mIsEnrichedHeaderFailed: " + WorkflowJibe.this.mIsEnrichedHeaderFailed);
                            if (!WorkflowJibe.this.mIsEnrichedHeaderFailed) {
                                WorkflowJibe.this.mSharedInfo.setHttpSPR();
                            } else {
                                WorkflowJibe.this.mSharedInfo.resetHttpSPR();
                            }
                        } else {
                            super.setHttpHeader();
                        }
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        String str;
                        if (WorkflowJibe.this.mMno == Mno.SPRINT) {
                            WorkflowJibe.this.mSharedInfo.setHttpsSPR();
                        } else {
                            WorkflowJibe.this.mSharedInfo.setHttpsDefault();
                        }
                        WorkflowJibe.this.mSharedInfo.addHttpParam("IMSI", WorkflowJibe.this.mTelephony.getSubscriberId(SimUtil.getSubId(WorkflowJibe.this.mPhoneId)));
                        WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowJibe.this.mTelephony.getDeviceId(WorkflowJibe.this.mPhoneId));
                        SharedInfo sharedInfo = WorkflowJibe.this.mSharedInfo;
                        if (WorkflowJibe.this.isSmsAppDefault()) {
                            str = "1";
                        } else {
                            str = "2";
                        }
                        sharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, str);
                        if ((!WorkflowJibe.this.mMobileNetwork && !WorkflowJibe.this.mIsMobileConfigNeeded) || WorkflowJibe.this.mHttpResponse == 511) {
                            String msisdn = WorkflowJibe.this.mTelephony.getMsisdn();
                            if (!TextUtils.isEmpty(msisdn)) {
                                Log.i(WorkflowJibe.LOG_TAG, "use msisdn from telephony");
                                WorkflowJibe.this.mSharedInfo.addHttpParam("msisdn", WorkflowJibe.this.mParamHandler.encodeRFC3986(msisdn));
                            }
                            String usermsisdn = WorkflowJibe.this.mSharedInfo.getUserMsisdn();
                            if (!TextUtils.isEmpty(usermsisdn)) {
                                Log.i(WorkflowJibe.LOG_TAG, "use msisdn from sharedInfo");
                                WorkflowJibe.this.mSharedInfo.addHttpParam("msisdn", WorkflowJibe.this.mParamHandler.encodeRFC3986(usermsisdn));
                            }
                            WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ConfigUtil.getSmsPort(WorkflowJibe.this.mPhoneId));
                            IMSLog.c(LogClass.WFJ_OTP_SMS_PORT, WorkflowJibe.this.mPhoneId + ",OSP:" + ConfigUtil.getSmsPort(WorkflowJibe.this.mPhoneId));
                            WorkflowJibe.this.mSharedInfo.addHttpParam("token", WorkflowJibe.this.getToken());
                        }
                        WorkflowJibe.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
                        WorkflowJibe.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.PVALUE.TERMINAL_MODEL);
                        WorkflowJibe.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowJibe.this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(WorkflowJibe.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
                        WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VENDOR, ConfigConstants.PVALUE.CLIENT_VENDOR);
                        SharedInfo sharedInfo2 = WorkflowJibe.this.mSharedInfo;
                        sharedInfo2.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, ConfigConstants.PVALUE.CLIENT_VERSION_NAME + WorkflowJibe.this.mClientVersion);
                        WorkflowJibe.this.mSharedInfo.addHttpParam("rcs_version", WorkflowJibe.this.mRcsVersion);
                        Log.i(WorkflowJibe.LOG_TAG, "set rcs_profile to UP_T-b1 for A2P");
                        WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_T-b1");
                        WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, WorkflowJibe.this.mRcsProvisioningVersion);
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        workflowJibe.setRcsState(workflowJibe.convertRcsStateWithSpecificParam());
                        WorkflowJibe.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, WorkflowJibe.this.getRcsState());
                        if (WorkflowJibe.this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowJibe.this.getRcsState())) {
                            Log.i(WorkflowJibe.LOG_TAG, "mStartForce: true, vers: 0");
                            WorkflowJibe.this.mSharedInfo.addHttpParam("vers", "0");
                        }
                        if (WorkflowJibe.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            String str2 = WorkflowJibe.LOG_TAG;
                            Log.i(str2, "use backup version in case of dormant, vers: " + WorkflowJibe.this.getVersionBackup());
                            WorkflowJibe.this.mSharedInfo.addHttpParam("vers", WorkflowJibe.this.getVersionBackup());
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Authorize:");
                        WorkflowBase.Workflow next = super.run();
                        if (next instanceof WorkflowBase.Finish) {
                            WorkflowJibe.this.mSharedInfo.getHttpResponse().setStatusCode(700);
                        }
                        return next;
                    }

                    /* access modifiers changed from: protected */
                    public String getOtp() {
                        String otpSmsType = ConfigUtil.getSmsType(WorkflowJibe.this.mPhoneId);
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "otpSmsType: " + otpSmsType + " mAuthTryCount: " + WorkflowJibe.this.mAuthTryCount + " mAuthHiddenTryCount: " + WorkflowJibe.this.mAuthHiddenTryCount);
                        StringBuilder sb = new StringBuilder();
                        sb.append(WorkflowJibe.this.mPhoneId);
                        sb.append(",OST:");
                        sb.append(otpSmsType);
                        IMSLog.c(LogClass.WFJ_OTP_SMS_TYPE, sb.toString());
                        if ("text".equals(otpSmsType) && WorkflowJibe.this.mAuthTryCount < 1) {
                            WorkflowJibe.this.mAuthTryCount++;
                            return WorkflowJibe.this.mTelephony.getOtp();
                        } else if (!WorkflowJibe.OTP_SMS_BINARY_TYPE.equals(otpSmsType) || WorkflowJibe.this.mAuthHiddenTryCount >= 3) {
                            return null;
                        } else {
                            WorkflowJibe.this.mAuthHiddenTryCount++;
                            return WorkflowJibe.this.mTelephony.getPortOtp();
                        }
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        if (WorkflowJibe.this.mMno.isEur() || WorkflowJibe.this.mMno.isSea() || WorkflowJibe.this.mMno.isOce() || WorkflowJibe.this.mMno.isMea() || WorkflowJibe.this.mMno.isSwa()) {
                            WorkflowJibe.this.mCookieHandler.handleCookie(WorkflowJibe.this.mSharedInfo.getHttpResponse());
                        } else {
                            WorkflowJibe.this.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, WorkflowJibe.this.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie"));
                        }
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Parse:");
                        return super.run();
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Store:");
                        Map<String, String> parsedXml = WorkflowJibe.this.mSharedInfo.getParsedXml();
                        boolean versionChange = WorkflowJibe.this.getVersion() != WorkflowJibe.this.getVersion(parsedXml);
                        String str = WorkflowJibe.LOG_TAG;
                        Log.i(str, "versionChange: " + versionChange);
                        WorkflowBase.OpMode rcsDisabledState = WorkflowJibe.this.getRcsDisabledState(parsedXml);
                        if (WorkflowJibe.this.isValidRcsDisabledState(rcsDisabledState)) {
                            WorkflowJibe.this.setOpMode(rcsDisabledState, parsedXml);
                            return WorkflowJibe.this.getNextWorkflow(8);
                        }
                        boolean userAccept = true;
                        if (!ConfigUtil.isRcsPreConsent(WorkflowJibe.this.mContext, WorkflowJibe.this.mPhoneId) && (WorkflowJibe.this.mMno.isEur() || WorkflowJibe.this.mMno.isSea() || WorkflowJibe.this.mMno.isOce() || WorkflowJibe.this.mMno.isMea() || WorkflowJibe.this.mMno.isSwa())) {
                            userAccept = WorkflowJibe.this.mParamHandler.getUserAccept(parsedXml);
                        }
                        WorkflowJibe.this.mParamHandler.setOpModeWithUserAccept(userAccept, parsedXml, WorkflowBase.OpMode.DISABLE);
                        if (WorkflowJibe.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                            WorkflowJibe workflowJibe = WorkflowJibe.this;
                            workflowJibe.setValidityTimer(workflowJibe.getValidity());
                        }
                        WorkflowJibe.this.mMsisdnHandler.setMsisdnSkipCount(0);
                        WorkflowJibe.this.setTcUserAccept(userAccept);
                        return WorkflowJibe.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Finish:");
                        return null;
                    }
                };
            default:
                String str = LOG_TAG;
                Log.i(str, "getNextWorkflow: Unexpected type [" + type + "] !!!");
                return null;
        }
    }
}
