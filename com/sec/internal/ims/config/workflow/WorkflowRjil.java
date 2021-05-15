package com.sec.internal.ims.config.workflow;

import android.database.sqlite.SQLiteFullException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import java.net.ConnectException;
import java.util.Map;

public class WorkflowRjil extends WorkflowBase {
    public static final String LOG_TAG = WorkflowRjil.class.getSimpleName();

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowRjil(android.os.Looper r15, android.content.Context r16, android.os.Handler r17, com.sec.internal.constants.Mno r18, int r19) {
        /*
            r14 = this;
            r11 = r16
            r12 = r17
            r13 = r19
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceRjil r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceRjil
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowRjil.<init>(android.os.Looper, android.content.Context, android.os.Handler, com.sec.internal.constants.Mno, int):void");
    }

    /* access modifiers changed from: package-private */
    public void work() {
        String str;
        String str2;
        WorkflowBase.Workflow next = new Initialize();
        int count = 20;
        while (!this.mNeedToStopWork && next != null && count > 0) {
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
            } catch (ConnectException e3) {
                String str3 = LOG_TAG;
                if (("ConnectException occur:" + e3.getMessage()) == null) {
                    str = "";
                } else {
                    str = e3.getMessage();
                }
                Log.i(str3, str);
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                next = new Initialize();
                e3.printStackTrace();
            } catch (SQLiteFullException e4) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e4.getMessage());
                Log.i(LOG_TAG, "finish");
                e4.printStackTrace();
            } catch (Exception e5) {
                String str4 = LOG_TAG;
                if (("unknown exception occur:" + e5.getMessage()) == null) {
                    str2 = "";
                } else {
                    str2 = e5.getMessage();
                }
                Log.i(str4, str2);
                Log.i(LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                next = new Initialize();
                e5.printStackTrace();
            }
            count--;
        }
        if (this.mNeedToStopWork) {
            Log.i(LOG_TAG, "work interrupted");
            this.mNeedToStopWork = false;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int type) {
        return null;
    }

    public IStorageAdapter getStorage() {
        if (this.mStorage == null || this.mStorage.getState() != 1) {
            return null;
        }
        return this.mStorage;
    }

    protected class Initialize implements WorkflowBase.Workflow {
        protected Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow next = null;
            WorkflowRjil.this.mSharedInfo.setUrl(WorkflowRjil.this.mParamHandler.initUrl());
            WorkflowRjil.this.mCookieHandler.clearCookie();
            if (WorkflowRjil.this.mStartForce) {
                next = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowRjil.this.getOpMode().ordinal()];
                if (i == 1 || i == 2 || i == 3) {
                    next = new FetchHttp();
                } else if (i == 4 || i == 5) {
                    next = new Finish(WorkflowRjil.this, (AnonymousClass1) null);
                }
            }
            if (!(next instanceof FetchHttp) || WorkflowRjil.this.mMobileNetwork) {
                return next;
            }
            Log.i(WorkflowRjil.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowRjil$1  reason: invalid class name */
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
            WorkflowRjil.this.mSharedInfo.setHttpDefault();
            WorkflowRjil.this.mSharedInfo.setHttpResponse(WorkflowRjil.this.getHttpResponse());
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                return new FetchHttps();
            }
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            return workflowRjil.handleResponse2(new Initialize(), new FetchHttps(), new Finish(WorkflowRjil.this, (AnonymousClass1) null));
        }
    }

    protected class FetchHttps implements WorkflowBase.Workflow {
        protected FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil.this.mSharedInfo.setHttpsDefault();
            WorkflowRjil.this.mCookieHandler.handleCookie(WorkflowRjil.this.mSharedInfo.getHttpResponse());
            WorkflowRjil.this.mSharedInfo.addHttpParam("vers", String.valueOf(WorkflowRjil.this.getVersion()));
            WorkflowRjil.this.mSharedInfo.addHttpParam("IMSI", WorkflowRjil.this.mTelephony.getImsi());
            WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, WorkflowRjil.this.mTelephony.getImei());
            WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RJIL_TOKEN, WorkflowRjil.this.mParamHandler.encodeRFC7254(WorkflowRjil.this.mTelephony.getImei()));
            WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SIM_MODE, WorkflowRjil.this.mTelephony.getMnc());
            WorkflowRjil.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
            SharedInfo sharedInfo = WorkflowRjil.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.CLIENT_VERSION, "RCSAndJIO-" + WorkflowRjil.this.mClientVersion);
            WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.DEFAULT_SMS_APP, "1");
            if (!WorkflowRjil.this.mMobileNetwork || WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                String msisdn = TextUtils.isEmpty(WorkflowRjil.this.mSharedInfo.getUserMsisdn()) ? WorkflowRjil.this.mTelephony.getMsisdn() : WorkflowRjil.this.mSharedInfo.getUserMsisdn();
                if (!TextUtils.isEmpty(msisdn)) {
                    WorkflowRjil.this.mSharedInfo.addHttpParam("msisdn", WorkflowRjil.this.mParamHandler.encodeRFC3986(msisdn));
                }
                WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, WorkflowRjil.this.mTelephony.getSmsDestPort());
                WorkflowRjil.this.mSharedInfo.addHttpParam("token", WorkflowRjil.this.getToken());
            }
            WorkflowRjil.this.mSharedInfo.addHttpParam("terminal_vendor", ConfigConstants.PVALUE.CLIENT_VENDOR);
            WorkflowRjil.this.mSharedInfo.addHttpParam("terminal_sw_version", WorkflowRjil.this.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(WorkflowRjil.this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
            if (WorkflowRjil.this.mStartForce) {
                WorkflowRjil.this.mSharedInfo.addHttpParam("vers", "0");
            }
            if (WorkflowRjil.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                String str = WorkflowRjil.LOG_TAG;
                Log.i(str, "DORMANT mode. use backup version :" + WorkflowRjil.this.getVersionBackup());
                WorkflowRjil.this.mSharedInfo.addHttpParam("vers", WorkflowRjil.this.getVersionBackup());
            }
            WorkflowRjil.this.mSharedInfo.setHttpResponse(WorkflowRjil.this.getHttpResponse());
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                Log.i(WorkflowRjil.LOG_TAG, "200 OK received. try parsing");
                return new Parse();
            } else if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                Log.i(WorkflowRjil.LOG_TAG, "403 received. Finish");
                return new Finish(WorkflowRjil.this, (AnonymousClass1) null);
            } else {
                String str2 = WorkflowRjil.LOG_TAG;
                Log.i(str2, " http status : " + WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode());
                WorkflowRjil workflowRjil = WorkflowRjil.this;
                return workflowRjil.handleResponse2(new Initialize(), new FetchHttps(), new Finish(WorkflowRjil.this, (AnonymousClass1) null));
            }
        }
    }

    private class FetchOtp implements WorkflowBase.Workflow {
        private FetchOtp() {
        }

        /* synthetic */ FetchOtp(WorkflowRjil x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil.this.mSharedInfo.setHttpClean();
            WorkflowRjil.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, WorkflowRjil.this.mSharedInfo.getOtp());
            WorkflowRjil.this.mSharedInfo.setHttpResponse(WorkflowRjil.this.getHttpResponse());
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            return workflowRjil.handleResponse2(new Initialize(), new FetchHttps(), new Finish(WorkflowRjil.this, (AnonymousClass1) null));
        }
    }

    private class Authorize implements WorkflowBase.Workflow {
        private Authorize() {
        }

        /* synthetic */ Authorize(WorkflowRjil x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            Log.i(WorkflowRjil.LOG_TAG, "get OTP & save it to shared info");
            WorkflowRjil.this.mPowerController.release();
            WorkflowRjil.this.mTelephony.registerUneregisterForOTP(true);
            String otp = WorkflowRjil.this.mTelephony.getOtp();
            if (otp == null) {
                WorkflowRjil.this.mTelephony.registerUneregisterForOTP(false);
                return new Finish(WorkflowRjil.this, (AnonymousClass1) null);
            }
            WorkflowRjil.this.mTelephony.registerUneregisterForOTP(false);
            WorkflowRjil.this.mSharedInfo.setOtp(otp);
            WorkflowRjil.this.mPowerController.lock();
            return new FetchOtp(WorkflowRjil.this, (AnonymousClass1) null);
        }
    }

    protected class Parse implements WorkflowBase.Workflow {
        protected Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowRjil.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parsedXml = WorkflowRjil.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parsedXml == null) {
                throw new InvalidXmlException("no parsed xml ConfigContract.");
            } else if (parsedXml.get("root/vers/version") == null || parsedXml.get("root/vers/validity") == null) {
                Log.i(WorkflowRjil.LOG_TAG, "config xml must contain atleast 2 items(version & validity).");
                if (WorkflowRjil.this.mCookieHandler.isCookie(WorkflowRjil.this.mSharedInfo.getHttpResponse())) {
                    return new Authorize(WorkflowRjil.this, (AnonymousClass1) null);
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                WorkflowRjil.this.mSharedInfo.setParsedXml(parsedXml);
                return new Store(WorkflowRjil.this, (AnonymousClass1) null);
            }
        }
    }

    private class Store implements WorkflowBase.Workflow {
        private Store() {
        }

        /* synthetic */ Store(WorkflowRjil x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            boolean userAccept = WorkflowRjil.this.mParamHandler.getUserAccept(WorkflowRjil.this.mSharedInfo.getParsedXml());
            WorkflowRjil.this.mParamHandler.setOpModeWithUserAccept(userAccept, WorkflowRjil.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowRjil.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowRjil workflowRjil = WorkflowRjil.this;
                workflowRjil.setValidityTimer(workflowRjil.getValidity());
            }
            WorkflowRjil.this.setTcUserAccept(userAccept);
            return new Finish(WorkflowRjil.this, (AnonymousClass1) null);
        }
    }

    private class Finish implements WorkflowBase.Workflow {
        private Finish() {
        }

        /* synthetic */ Finish(WorkflowRjil x0, AnonymousClass1 x1) {
            this();
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowRjil workflowRjil = WorkflowRjil.this;
                workflowRjil.setLastErrorCode(workflowRjil.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowRjil.LOG_TAG, "all workflow finished");
            WorkflowRjil.this.createSharedInfo();
            return null;
        }
    }
}
