package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteFullException;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.DigestAuth;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class WorkflowPrimaryDevice extends WorkflowBase {
    private static final String BODY = "";
    private static final String DIGEST_URI = "/";
    private static final String IMS_SWITCH = "imsswitch";
    private static final String LOG_TAG_BASE = WorkflowPrimaryDevice.class.getSimpleName();
    private static final String PASSWD = "";
    private static final String USER_NAME = "";
    /* access modifiers changed from: private */
    public String LOG_TAG = LOG_TAG_BASE;
    protected boolean mIsheaderEnrichment = false;
    private SharedPreferences.OnSharedPreferenceChangeListener mRcsSwitchListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "mRcsSwitchListener onChange");
            if (ImsConstants.SystemSettings.RCS_USER_SETTING1.getName().equals(key)) {
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice.sendMessage(workflowPrimaryDevice.obtainMessage(10, Boolean.valueOf(sharedPreferences.getBoolean(key, false))));
            }
        }
    };
    private boolean mRescheduleValidityTimer = false;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowPrimaryDevice(android.os.Looper r16, android.content.Context r17, android.os.Handler r18, com.sec.internal.constants.Mno r19, com.sec.internal.interfaces.ims.config.ITelephonyAdapter r20, int r21) {
        /*
            r15 = this;
            r11 = r15
            r12 = r21
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
            r7.<init>(r12)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r13 = r17
            r14 = r18
            r9.<init>(r13, r14)
            r0 = r15
            r1 = r16
            r2 = r17
            r3 = r18
            r4 = r19
            r5 = r20
            r10 = r21
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            java.lang.String r0 = LOG_TAG_BASE
            r11.LOG_TAG = r0
            r0 = 0
            r11.mIsheaderEnrichment = r0
            r11.mRescheduleValidityTimer = r0
            com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$1
            r0.<init>()
            r11.mRcsSwitchListener = r0
            com.sec.internal.constants.Mno r0 = r11.mMno
            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r0)
            if (r0 == 0) goto L_0x0046
            r15.registerListenersAndObservers()
        L_0x0046:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = LOG_TAG_BASE
            r0.append(r1)
            java.lang.String r1 = "["
            r0.append(r1)
            r0.append(r12)
            java.lang.String r1 = "]"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            r11.LOG_TAG = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, com.sec.internal.interfaces.ims.config.ITelephonyAdapter, int):void");
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i != 5) {
            if (i != 10) {
                super.handleMessage(msg);
            } else if (!((Boolean) msg.obj).booleanValue() || !this.mRescheduleValidityTimer) {
                cancelValidityTimer();
                this.mRescheduleValidityTimer = true;
            } else {
                Log.i(this.LOG_TAG, "Rescheduling validity timer due to RCS switch change");
                this.mRescheduleValidityTimer = false;
                scheduleAutoconfig(getVersion());
            }
        } else if (isSmsAppDefault()) {
            if (ImsRegistry.isRcsEnabledByPhoneId(this.mPhoneId)) {
                Log.i(this.LOG_TAG, "sms default application is changed to samsung, schedule autoconf");
                scheduleAutoconfig(getVersion());
                return;
            }
            this.mRescheduleValidityTimer = true;
        } else if (!isSmsAppDefault()) {
            Log.i(this.LOG_TAG, "sms default application is changed to non-samsung, cancel validity timer");
            cancelValidityTimer();
        }
    }

    private void registerListenersAndObservers() {
        Context context = this.mContext;
        context.getSharedPreferences("imsswitch_" + this.mPhoneId, 0).registerOnSharedPreferenceChangeListener(this.mRcsSwitchListener);
    }

    private void unregisterListenersAndObservers() {
        Context context = this.mContext;
        context.getSharedPreferences("imsswitch_" + this.mPhoneId, 0).unregisterOnSharedPreferenceChangeListener(this.mRcsSwitchListener);
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize();
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(this.LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(this.LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                next = new Initialize();
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                Log.i(this.LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(this.LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = new Initialize();
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.i(this.LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(this.LOG_TAG, "finish workflow");
                next = new Finish();
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(this.LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(this.LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                next = new Initialize();
                e4.printStackTrace();
            }
            count--;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    private class Initialize implements WorkflowBase.Workflow {
        private Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow next = null;
            WorkflowPrimaryDevice.this.mSharedInfo.setUrl(WorkflowPrimaryDevice.this.mParamHandler.initUrl());
            WorkflowPrimaryDevice.this.mCookieHandler.clearCookie();
            if (WorkflowPrimaryDevice.this.mStartForce) {
                WorkflowPrimaryDevice.this.setToken("");
                next = new FetchHttp();
            } else {
                int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowPrimaryDevice.this.getOpMode().ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    next = new FetchHttp();
                } else if (i == 4 || i == 5) {
                    next = new Finish();
                }
            }
            if (!(next instanceof FetchHttp) || WorkflowPrimaryDevice.this.mMobileNetwork) {
                return next;
            }
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$2  reason: invalid class name */
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
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.ENABLE_RCS_BY_USER.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_RCS_BY_USER.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private class FetchHttp implements WorkflowBase.Workflow {
        private FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpDefault();
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpResponse(WorkflowPrimaryDevice.this.getHttpResponse());
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 200 || WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                    WorkflowPrimaryDevice.this.mIsheaderEnrichment = true;
                }
                return new FetchHttps();
            }
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    private class FetchHttps implements WorkflowBase.Workflow {
        private FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.setSharedInfoWithParam();
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                if (ArrayUtils.isEmpty(WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getBody())) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "200 OK received. Body empty or null. Start Parsing.");
                    return new Parse();
                }
                String json = new String(WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getBody());
                try {
                    new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                } catch (Exception e) {
                    Log.d(WorkflowPrimaryDevice.this.LOG_TAG, "200 OK received. Body non-empty, but not Json either. Start Parsing");
                    return new Parse();
                }
            }
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                if (SimUtil.getMno() == Mno.BELL || (WorkflowPrimaryDevice.this.mMobileNetwork && !WorkflowPrimaryDevice.this.mIsheaderEnrichment)) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "403 received. Finish");
                    return new Finish();
                } else if (!WorkflowPrimaryDevice.this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "no msisdn. try to get user");
                    WorkflowPrimaryDevice.this.mPowerController.release();
                    String msisdn = WorkflowPrimaryDevice.this.mDialog.getMsisdn(WorkflowPrimaryDevice.this.mTelephony.getSimCountryCode());
                    WorkflowPrimaryDevice.this.mPowerController.lock();
                    if (TextUtils.isEmpty(msisdn)) {
                        Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "user didn't enter msisdn finish process");
                        return new Finish();
                    }
                    WorkflowPrimaryDevice.this.mSharedInfo.setUserMsisdn(msisdn);
                    return new Initialize();
                } else if (!TextUtils.isEmpty(WorkflowPrimaryDevice.this.mSharedInfo.getUserMsisdn())) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "wrong MSISDN from USER. try again after 300");
                    WorkflowPrimaryDevice.this.setValidityTimer(300);
                    WorkflowPrimaryDevice.this.mMsisdnHandler.setMsisdnValue("");
                    return new Finish();
                }
            }
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithParam() {
        this.mSharedInfo.setHttpsDefault();
        this.mCookieHandler.clearCookie();
        if (this.mParamHandler.isConfigProxy()) {
            this.mSharedInfo.changeConfigProxyUriForHttp();
            this.mSharedInfo.setHttpProxyDefault();
        }
        if (this.mMobileNetwork && this.mSharedInfo.getHttpResponse().getHeader().get("Set-Cookie") != null) {
            this.mCookieHandler.handleCookie(this.mSharedInfo.getHttpResponse());
        }
        this.mSharedInfo.addHttpParam("vers", String.valueOf(getVersion()));
        this.mSharedInfo.addHttpParam("IMSI", this.mTelephony.getSubscriberId(SimUtil.getSubId(this.mPhoneId)));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, this.mTelephony.getDeviceId(this.mPhoneId));
        this.mSharedInfo.addHttpParam("terminal_model", ConfigContract.BUILD.getTerminalModel());
        this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
        this.mSharedInfo.addHttpParam("terminal_sw_version", this.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, isSmsAppDefault() ? "1" : "2");
        setSharedInfoWithAuthParam();
        setOpenIdAuthParams();
        if (this.mStartForce) {
            this.mSharedInfo.addHttpParam("vers", "0");
        }
        if (getOpMode() == WorkflowBase.OpMode.DORMANT) {
            String str = this.LOG_TAG;
            Log.i(str, "DORMANT mode. use backup version :" + getVersionBackup());
            this.mSharedInfo.addHttpParam("vers", getVersionBackup());
        }
        this.mSharedInfo.setHttpResponse(getHttpResponse());
    }

    /* access modifiers changed from: protected */
    public void setOpenIdAuthParams() {
        byte[] jsonBytes;
        if (this.mSharedInfo.getHttpResponse() != null) {
            if (this.mSharedInfo.getHttpResponse().getStatusCode() == 401) {
                List<String> headerlist = new ArrayList<>();
                if (!(this.mSharedInfo.getHttpResponse().getHeader().get("WWW-Authenticate") == null || this.mSharedInfo.getHttpResponse().getHeader().get("WWW-Authenticate").get(0) == null)) {
                    this.mSharedInfo.parseAkaParams((String) this.mSharedInfo.getHttpResponse().getHeader().get("WWW-Authenticate").get(0));
                    HashMap<String, String> authParams = this.mSharedInfo.getAKAParams();
                    DigestAuth digestauth = new DigestAuth();
                    digestauth.setDigestAuth("", "", authParams.get("realm"), authParams.get(WwwAuthenticateHeader.HEADER_PARAM_NONCE), "POST", DIGEST_URI, authParams.get(WwwAuthenticateHeader.HEADER_PARAM_ALGORITHM), authParams.get(AuthenticationHeaders.HEADER_PARAM_QOP), "");
                    String cnonce = DigestAuth.createCnonce();
                    String response = digestauth.getResp();
                    headerlist.add(((String) this.mSharedInfo.getHttpResponse().getHeader().get("WWW-Authenticate").get(0)) + ",cnonce=" + cnonce + ",response=" + response);
                    this.mSharedInfo.addHttpHeader("Authorization", headerlist);
                }
            }
            if (this.mSharedInfo.getHttpResponse().getStatusCode() == 302) {
                Log.d(this.LOG_TAG, "302 Recieved");
                if (this.mSharedInfo.getHttpResponse().getHeader().get("Location") != null) {
                    Log.d("LOCATION HEADER: ", (String) this.mSharedInfo.getHttpResponse().getHeader().get("Location").get(0));
                    if (this.mSharedInfo.getHttpResponse().getHeader().get("access_token") != null) {
                        Log.d(this.LOG_TAG, "prepare for configuration request");
                    } else if (this.mSharedInfo.getHttpResponse().getHeader().get(AuthenticationHeaders.HEADER_PARAM_CODE) != null) {
                        Log.d(this.LOG_TAG, "should reach out token end point for access token");
                    } else {
                        this.mSharedInfo.parseOidcParams((String) this.mSharedInfo.getHttpResponse().getHeader().get("Location").get(0));
                        for (Map.Entry<String, String> entry : this.mSharedInfo.getOidcParams().entrySet()) {
                            this.mSharedInfo.addHttpParam(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            if (this.mSharedInfo.getHttpResponse().getStatusCode() == 200 && this.mSharedInfo.getHttpResponse().getBody() != null && (jsonBytes = this.mSharedInfo.getHttpResponse().getBody()) != null) {
                String json = new String(jsonBytes);
                try {
                    if (json.indexOf("{") == -1 || json.lastIndexOf("}") == -1) {
                        Log.d(this.LOG_TAG, "Not a JSON Body");
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                    if (jsonObject.has("access_token")) {
                        this.mSharedInfo.addHttpParam("access_token", jsonObject.getString("access_token"));
                        if (jsonObject.has(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN)) {
                            this.mSharedInfo.addHttpParam(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN, jsonObject.getString(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN));
                        }
                        this.mSharedInfo.setUrl(this.mParamHandler.initUrl());
                    }
                } catch (Exception e) {
                    Log.d(this.LOG_TAG, "Not a JSON Body");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithAuthParam() {
        if (!this.mMobileNetwork || this.mIsheaderEnrichment || this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
            if (!TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(this.mSharedInfo.getUserMsisdn()));
            } else if (!TextUtils.isEmpty(this.mTelephony.getMsisdn())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(this.mTelephony.getMsisdn()));
            } else if (!TextUtils.isEmpty(this.mMsisdnHandler.getLastMsisdnValue())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mMsisdnHandler.getLastMsisdnValue());
            }
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, this.mTelephony.getSmsDestPort());
            this.mSharedInfo.addHttpParam("token", getToken());
        }
    }

    private class FetchOtp implements WorkflowBase.Workflow {
        private FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpClean();
            WorkflowPrimaryDevice.this.mCookieHandler.clearCookie();
            WorkflowPrimaryDevice.this.mCookieHandler.handleCookie(WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse());
            WorkflowPrimaryDevice.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowPrimaryDevice.this.mSharedInfo.getOtp());
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpResponse(WorkflowPrimaryDevice.this.getHttpResponse());
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    private class Authorize implements WorkflowBase.Workflow {
        private Authorize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "get OTP & save it to shared info");
            WorkflowPrimaryDevice.this.mPowerController.release();
            String otp = WorkflowPrimaryDevice.this.mTelephony.getOtp();
            if (otp == null) {
                WorkflowPrimaryDevice.this.setValidityTimer(0);
                return new Finish();
            }
            WorkflowPrimaryDevice.this.mSharedInfo.setOtp(otp);
            WorkflowPrimaryDevice.this.mPowerController.lock();
            return new FetchOtp();
        }
    }

    private class Parse implements WorkflowBase.Workflow {
        private Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parsedXml = WorkflowPrimaryDevice.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parsedXml == null) {
                throw new InvalidXmlException("no parsed xml data.");
            } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "config xml must contain atleast 2 items(version & validity).");
                if (WorkflowPrimaryDevice.this.mCookieHandler.isCookie(WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse())) {
                    return new Authorize();
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                parsedXml.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, new String(body, "utf-8"));
                WorkflowPrimaryDevice.this.mSharedInfo.setParsedXml(parsedXml);
                WorkflowPrimaryDevice.this.mMsisdnHandler.setMsisdnValue(WorkflowPrimaryDevice.this.mSharedInfo.getUserMsisdn());
                return new Store();
            }
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            boolean userAccept = true;
            Map<String, String> msg = WorkflowPrimaryDevice.this.mParamHandler.getUserMessage(WorkflowPrimaryDevice.this.mSharedInfo.getParsedXml());
            if (msg.size() == 4) {
                boolean workingConfigurationAvailable = false;
                int i = AnonymousClass2.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowPrimaryDevice.this.getOpMode().ordinal()];
                boolean isDisablingConfigResponse = true;
                if (i == 1 || i == 3 || i == 6 || i == 7) {
                    workingConfigurationAvailable = true;
                }
                int version = WorkflowPrimaryDevice.this.getVersion();
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                boolean versionChange = version != workflowPrimaryDevice.getVersion(workflowPrimaryDevice.mSharedInfo.getParsedXml());
                WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
                if (workflowPrimaryDevice2.getVersion(workflowPrimaryDevice2.mSharedInfo.getParsedXml()) >= 1) {
                    isDisablingConfigResponse = false;
                }
                if ((!versionChange || workingConfigurationAvailable) && !isDisablingConfigResponse) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "Previously working configuration available for this IMSI. Don't display T&C.");
                } else {
                    userAccept = WorkflowPrimaryDevice.this.mParamHandler.getUserAcceptWithDialog(msg);
                }
            }
            boolean workingConfigurationAvailable2 = userAccept;
            WorkflowPrimaryDevice.this.mParamHandler.setOpModeWithUserAccept(userAccept, WorkflowPrimaryDevice.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowPrimaryDevice.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowPrimaryDevice workflowPrimaryDevice3 = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice3.setValidityTimer(workflowPrimaryDevice3.getValidity());
            }
            WorkflowPrimaryDevice.this.setTcUserAccept(workingConfigurationAvailable2 ? 1 : 0);
            return new Finish();
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice.setLastErrorCode(workflowPrimaryDevice.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "all workflow finished");
            WorkflowPrimaryDevice.this.createSharedInfo();
            return null;
        }
    }

    public void cleanup() {
        super.cleanup();
        unregisterListenersAndObservers();
    }
}
