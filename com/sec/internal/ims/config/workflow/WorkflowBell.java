package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowBell extends WorkflowBase {
    public static final String LOG_TAG = WorkflowBell.class.getSimpleName();

    public WorkflowBell(Looper looper, Context context, Handler handler, Mno mno, int phoneId) {
        super(looper, context, handler, mno, phoneId);
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow next = new Initialize();
        int count = 20;
        while (next != null && count > 0) {
            try {
                next = next.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                next = new Initialize();
                e.printStackTrace();
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = new Initialize();
                e2.printStackTrace();
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "finish workflow");
                next = new Finish();
                e3.printStackTrace();
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec. and retry");
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

    protected class Initialize implements WorkflowBase.Workflow {
        protected Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "Initialize:");
            WorkflowBase.Workflow next = null;
            WorkflowBell.this.mSharedInfo.setUrl(WorkflowBell.this.mParamHandler.initUrl());
            WorkflowBell.this.mCookieHandler.clearCookie();
            if (WorkflowBell.this.mStartForce) {
                next = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBell.this.getOpMode().ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    next = new FetchHttp();
                } else if (i == 4 || i == 5) {
                    next = new Finish();
                }
            }
            if (!(next instanceof FetchHttp) || WorkflowBell.this.mMobileNetwork) {
                return next;
            }
            Log.i(WorkflowBell.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowBell$1  reason: invalid class name */
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

    protected class FetchHttp implements WorkflowBase.Workflow {
        protected FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "FetchHttp:");
            WorkflowBell.this.mSharedInfo.setHttpDefault();
            WorkflowBell.this.mSharedInfo.setHttpResponse(WorkflowBell.this.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200 || WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                return new FetchHttps();
            }
            WorkflowBell workflowBell = WorkflowBell.this;
            return workflowBell.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class FetchHttps implements WorkflowBase.Workflow {
        protected FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "FetchHttps:");
            WorkflowBell.this.mSharedInfo.setHttpsDefault();
            WorkflowBell.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowBell.this.getVersion()));
            WorkflowBell.this.mSharedInfo.addHttpParam("IMSI", WorkflowBell.this.mTelephony.getImsi());
            WorkflowBell.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowBell.this.mTelephony.getImei());
            WorkflowBell.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
            WorkflowBell.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, WorkflowBell.this.isSmsAppDefault() ? "1" : "2");
            if (!WorkflowBell.this.mMobileNetwork || WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                if (!TextUtils.isEmpty(WorkflowBell.this.mTelephony.getMsisdn())) {
                    WorkflowBell.this.mSharedInfo.addHttpParam("msisdn", WorkflowBell.this.mParamHandler.encodeRFC3986(WorkflowBell.this.mTelephony.getMsisdn()));
                }
                if (!TextUtils.isEmpty(WorkflowBell.this.mSharedInfo.getUserMsisdn())) {
                    WorkflowBell.this.mSharedInfo.addHttpParam("msisdn", WorkflowBell.this.mParamHandler.encodeRFC3986(WorkflowBell.this.mSharedInfo.getUserMsisdn()));
                }
                WorkflowBell.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, WorkflowBell.this.mTelephony.getSmsDestPort());
                WorkflowBell.this.mSharedInfo.addHttpParam("token", WorkflowBell.this.getToken());
            }
            WorkflowBell.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
            WorkflowBell.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowBell.this.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(WorkflowBell.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
            if (WorkflowBell.this.mStartForce) {
                WorkflowBell.this.mSharedInfo.addHttpParam("vers", "0");
            }
            if (WorkflowBell.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                String str = WorkflowBell.LOG_TAG;
                Log.i(str, "DORMANT mode. use backup version :" + WorkflowBell.this.getVersionBackup());
                WorkflowBell workflowBell = WorkflowBell.this;
                workflowBell.addEventLog(WorkflowBell.LOG_TAG + "DORMANT mode. use backup version :" + WorkflowBell.this.getVersionBackup());
                WorkflowBell.this.mSharedInfo.addHttpParam("vers", WorkflowBell.this.getVersionBackup());
            }
            WorkflowBell.this.mSharedInfo.setHttpResponse(WorkflowBell.this.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                Log.i(WorkflowBell.LOG_TAG, "200 OK received. try parsing");
                return new Parse();
            }
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                if (!WorkflowBell.this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    Log.i(WorkflowBell.LOG_TAG, "no msisdn. try to get user");
                    WorkflowBell workflowBell2 = WorkflowBell.this;
                    workflowBell2.addEventLog(WorkflowBell.LOG_TAG + "no msisdn. try to get user");
                    WorkflowBell.this.mPowerController.release();
                    String msisdn = WorkflowBell.this.mDialog.getMsisdn(WorkflowBell.this.mTelephony.getSimCountryCode());
                    WorkflowBell.this.mPowerController.lock();
                    if (TextUtils.isEmpty(msisdn)) {
                        Log.i(WorkflowBell.LOG_TAG, "user didn't enter msisdn finish process");
                        return new Finish();
                    }
                    WorkflowBell.this.mSharedInfo.setUserMsisdn(msisdn);
                    return new Initialize();
                } else if (!TextUtils.isEmpty(WorkflowBell.this.mSharedInfo.getUserMsisdn())) {
                    Log.i(WorkflowBell.LOG_TAG, "wrong MSISDN from USER. try again after AUTO_CONFIG_RETRY_INTERVAL.");
                    WorkflowBell.this.setValidityTimer(300);
                    return new Finish();
                }
            }
            WorkflowBell workflowBell3 = WorkflowBell.this;
            return workflowBell3.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class FetchOtp implements WorkflowBase.Workflow {
        protected FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "FetchOtp:");
            WorkflowBell.this.mSharedInfo.setHttpClean();
            WorkflowBell.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowBell.this.mSharedInfo.getOtp());
            WorkflowBell.this.mSharedInfo.setHttpResponse(WorkflowBell.this.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowBell workflowBell = WorkflowBell.this;
            return workflowBell.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class Authorize implements WorkflowBase.Workflow {
        protected Authorize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "get OTP & save it to shared info");
            WorkflowBell.this.mPowerController.release();
            String otp = WorkflowBell.this.mTelephony.getOtp();
            if (otp == null) {
                WorkflowBell.this.setValidityTimer(0);
                return new Finish();
            }
            WorkflowBell.this.mSharedInfo.setOtp(otp);
            WorkflowBell.this.mPowerController.lock();
            return new FetchOtp();
        }
    }

    protected class Parse implements WorkflowBase.Workflow {
        protected Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "Parse:");
            byte[] body = WorkflowBell.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parsedXml = WorkflowBell.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parsedXml == null) {
                throw new InvalidXmlException("no parsed xml data.");
            } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "config xml must contain atleast 2 items(version & validity).");
                if (WorkflowBell.this.mCookieHandler.isCookie(WorkflowBell.this.mSharedInfo.getHttpResponse())) {
                    return new Authorize();
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                WorkflowBell.this.mSharedInfo.setParsedXml(parsedXml);
                return new Store();
            }
        }
    }

    protected class Store implements WorkflowBase.Workflow {
        protected Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "Store:");
            WorkflowBell.this.mParamHandler.setOpModeWithUserAccept(WorkflowBell.this.mParamHandler.getUserAccept(WorkflowBell.this.mSharedInfo.getParsedXml()), WorkflowBell.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowBell.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowBell workflowBell = WorkflowBell.this;
                workflowBell.setValidityTimer(workflowBell.getValidity());
            }
            return new Finish();
        }
    }

    protected class Finish implements WorkflowBase.Workflow {
        protected Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowBell.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowBell workflowBell = WorkflowBell.this;
                workflowBell.setLastErrorCode(workflowBell.mSharedInfo.getHttpResponse().getStatusCode());
            }
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "all workflow finished");
            WorkflowBell.this.createSharedInfo();
            return null;
        }
    }
}
