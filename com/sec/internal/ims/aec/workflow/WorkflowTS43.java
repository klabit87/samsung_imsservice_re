package com.sec.internal.ims.aec.workflow;

import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.aec.util.ContentParser;
import com.sec.internal.ims.aec.util.HttpStore;
import com.sec.internal.ims.aec.util.PowerController;
import com.sec.internal.ims.aec.workflow.WorkflowImpl;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.log.AECLog;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

class WorkflowTS43 extends WorkflowImpl {
    private final String LOG_TAG;
    private final int MAX_TRY_COUNT = 3;
    /* access modifiers changed from: private */
    public int mTryCnt = 3;
    /* access modifiers changed from: private */
    public int mTryCntNoAnswer = 1;

    static /* synthetic */ int access$710(WorkflowTS43 x0) {
        int i = x0.mTryCnt;
        x0.mTryCnt = i - 1;
        return i;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    WorkflowTS43(android.content.Context r3, android.os.Looper r4, android.os.Handler r5) {
        /*
            r2 = this;
            java.lang.Class<com.sec.internal.ims.aec.workflow.WorkflowTS43> r0 = com.sec.internal.ims.aec.workflow.WorkflowTS43.class
            java.lang.String r1 = r0.getSimpleName()
            r2.<init>(r3, r4, r5, r1)
            java.lang.String r0 = r0.getSimpleName()
            r2.LOG_TAG = r0
            r0 = 3
            r2.MAX_TRY_COUNT = r0
            r2.mTryCnt = r0
            r0 = 1
            r2.mTryCntNoAnswer = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.workflow.WorkflowTS43.<init>(android.content.Context, android.os.Looper, android.os.Handler):void");
    }

    /* access modifiers changed from: package-private */
    public void doWorkflow() {
        WorkflowImpl.Workflow workflow;
        WorkflowImpl.Workflow next = new Initialize();
        while (next != null) {
            int i = this.mTryCnt;
            if (i >= 0) {
                if (i == 0) {
                    try {
                        next = new Finish();
                    } catch (IOException e) {
                        if (this.mTryCnt == 1) {
                            workflow = new Stop();
                        } else {
                            clearAkaToken();
                            this.mHttpJar.setEapChallengeResp("");
                            PowerController powerController = this.mPowerCtrl;
                            int i2 = this.mTryCnt - 1;
                            this.mTryCnt = i2;
                            powerController.sleep(((long) (3 - i2)) * UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                            workflow = new Initialize();
                        }
                        next = workflow;
                        String str = this.LOG_TAG;
                        AECLog.e(str, "doWorkflow: " + e.getMessage(), this.mPhoneId);
                    } catch (Exception e2) {
                        next = new Stop();
                        String str2 = this.LOG_TAG;
                        AECLog.e(str2, "doWorkflow: " + e2.getMessage(), this.mPhoneId);
                    }
                }
                next = next.run();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public WorkflowImpl.Workflow handleNotOkResponse(int responseCode) {
        long raTime;
        String str = this.LOG_TAG;
        AECLog.i(str, "handleNotOkResponse: " + responseCode, this.mPhoneId);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.add("handleNotOkResponse: " + responseCode);
        WorkflowImpl.Workflow next = new Initialize();
        setValidEntitlement(false);
        if (responseCode != 0) {
            if (!(responseCode == 400 || responseCode == 415 || responseCode == 500)) {
                if (responseCode != 503) {
                    if (responseCode != 511) {
                        switch (responseCode) {
                            case 403:
                            case 404:
                                break;
                            case AECNamespace.HttpResponseCode.METHOD_NOT_ALLOWED:
                                break;
                            default:
                                return next;
                        }
                    }
                    clearAkaToken();
                    return new FetchEapId();
                }
                try {
                    String ra = (String) this.mHttpJar.getHttpResponse().getHeader().get(HttpRequest.HEADER_RETRY_AFTER).get(0);
                    if (ra.matches("[0-9]+")) {
                        raTime = (long) Integer.parseInt(ra);
                    } else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ", Locale.ENGLISH);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(ra));
                        long raTime2 = cal.getTimeInMillis();
                        cal.setTime(dateFormat.parse((String) this.mHttpJar.getHttpResponse().getHeader().get("Date").get(0)));
                        raTime = raTime2 - cal.getTimeInMillis();
                    }
                    if (raTime <= 0) {
                        raTime = 10;
                    }
                    String str2 = this.LOG_TAG;
                    AECLog.i(str2, "Retry-After: " + raTime + " sec", this.mPhoneId);
                    this.mPowerCtrl.sleep(1000 * raTime);
                    return next;
                } catch (Exception e) {
                    String str3 = this.LOG_TAG;
                    AECLog.e(str3, "Invalid Retry-After Header: " + e.getMessage(), this.mPhoneId);
                    return new Finish();
                }
            }
            clearAkaToken();
            return new Finish();
        }
        clearAkaToken();
        this.mHttpJar.setEapChallengeResp("");
        int i = this.mTryCntNoAnswer;
        if (i < 3) {
            this.mTryCntNoAnswer = i + 1;
            return next;
        }
        this.mTryCntNoAnswer = 1;
        this.mHttpJar.setHttpUrl((String) this.mHttpJar.getHttpUrls().poll());
        if (TextUtils.isEmpty(this.mHttpJar.getHttpUrl())) {
            return new Finish();
        }
        return next;
    }

    private class Initialize implements WorkflowImpl.Workflow {
        private Initialize() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            if (TextUtils.isEmpty(WorkflowTS43.this.mHttpJar.getHttpUrl())) {
                return new Stop();
            }
            if (!TextUtils.isEmpty(WorkflowTS43.this.getAkaToken())) {
                return new FetchToKen();
            }
            if (TextUtils.isEmpty(WorkflowTS43.this.mHttpJar.getEapChallengeResp())) {
                return new FetchEapId();
            }
            return new FetchEapChallengeResp();
        }
    }

    private class FetchEapId implements WorkflowImpl.Workflow {
        private FetchEapId() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            WorkflowTS43.this.mHttpJar.initHttpGetInfo(WorkflowTS43.this.mAECJar.getVersion(), WorkflowTS43.this.mAECJar.getEntitlementVersion());
            WorkflowTS43.this.mHttpJar.setHttpPushParam(WorkflowTS43.this.mAECJar.getNotifAction(), WorkflowTS43.this.mAECJar.getNotifToken());
            WorkflowTS43.this.mHttpJar.setHttpParam("EAP_ID", WorkflowTS43.this.mCalcEapAka.getImsiEap());
            HttpStore httpStore = WorkflowTS43.this.mHttpJar;
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            httpStore.setHttpResponse(workflowTS43.getHttpGetResponse(workflowTS43.mHttpJar.getHttpUrl()));
            WorkflowTS43.this.mAECJar.setHttpResponse(WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode());
            int statusCode = WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode();
            if (statusCode != 0) {
                if (statusCode == 200) {
                    return new ParseContent();
                }
                if (!(statusCode == 400 || statusCode == 403 || statusCode == 500)) {
                    WorkflowTS43.access$710(WorkflowTS43.this);
                }
            }
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            return workflowTS432.handleNotOkResponse(workflowTS432.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    private class FetchEapChallengeResp implements WorkflowImpl.Workflow {
        private FetchEapChallengeResp() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            WorkflowTS43.this.mHttpJar.initHttpPostInfo(WorkflowTS43.this.mHttpJar.getEapChallengeResp(), WorkflowTS43.this.mHttpJar.getHttpResponse().getHeader().get("Set-Cookie"));
            HttpStore httpStore = WorkflowTS43.this.mHttpJar;
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            httpStore.setHttpResponse(workflowTS43.getHttpPostResponse(workflowTS43.mHttpJar.getHttpUrl()));
            WorkflowTS43.this.mAECJar.setHttpResponse(WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode());
            int statusCode = WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode();
            if (statusCode != 0) {
                if (statusCode == 200) {
                    return new ParseContent();
                }
                if (!(statusCode == 400 || statusCode == 403 || statusCode == 500)) {
                    WorkflowTS43.access$710(WorkflowTS43.this);
                }
            }
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            return workflowTS432.handleNotOkResponse(workflowTS432.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    private class FetchToKen implements WorkflowImpl.Workflow {
        private FetchToKen() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            WorkflowTS43.this.mHttpJar.initHttpGetInfo(WorkflowTS43.this.mAECJar.getVersion(), WorkflowTS43.this.mAECJar.getEntitlementVersion());
            WorkflowTS43.this.mHttpJar.setHttpPushParam(WorkflowTS43.this.mAECJar.getNotifAction(), WorkflowTS43.this.mAECJar.getNotifToken());
            WorkflowTS43.this.mHttpJar.setHttpParam("token", WorkflowTS43.this.mAECJar.getAkaToken());
            WorkflowTS43.this.mHttpJar.setHttpParam("IMSI", WorkflowTS43.this.mAECJar.getImsi());
            HttpStore httpStore = WorkflowTS43.this.mHttpJar;
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            httpStore.setHttpResponse(workflowTS43.getHttpGetResponse(workflowTS43.mHttpJar.getHttpUrl()));
            WorkflowTS43.this.mAECJar.setHttpResponse(WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode());
            int statusCode = WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode();
            if (statusCode != 0) {
                if (statusCode == 200) {
                    return new ParseContent();
                }
                if (!(statusCode == 400 || statusCode == 403 || statusCode == 500)) {
                    WorkflowTS43.access$710(WorkflowTS43.this);
                }
            }
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            return workflowTS432.handleNotOkResponse(workflowTS432.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    private class CalcEapChallenge implements WorkflowImpl.Workflow {
        private CalcEapChallenge() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            Message replyTo = WorkflowTS43.this.obtainMessage();
            replyTo.what = 1005;
            WorkflowTS43.this.mCalcEapAka.requestEapChallengeResp(replyTo, WorkflowTS43.this.mHttpJar.getEapChallenge());
            return new WaitingEapChallengeResp();
        }
    }

    private class WaitingEapChallengeResp implements WorkflowImpl.Workflow {
        private WaitingEapChallengeResp() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            Message msg = WorkflowTS43.this.obtainMessage();
            msg.what = 1004;
            WorkflowTS43.this.sendMessage(msg);
            return null;
        }
    }

    private class ParseContent implements WorkflowImpl.Workflow {
        private ParseContent() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            String body = new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8);
            if (ContentParser.isJSONValid(WorkflowTS43.this.mHttpJar.getHttpResponse().getHeader().get("Content-Type")) || ContentParser.isJSONValid(body)) {
                return new ParseEapChallenge();
            }
            return new ParseConfiguration();
        }
    }

    private class ParseEapChallenge implements WorkflowImpl.Workflow {
        private ParseEapChallenge() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            Map<String, String> parsedJson = ContentParser.parseJson(new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8));
            if (parsedJson.isEmpty()) {
                return new Stop();
            }
            ContentParser.debugPrint(WorkflowTS43.this.mPhoneId, parsedJson);
            WorkflowTS43.this.mHttpJar.setEapChallenge(WorkflowTS43.this.mCalcEapAka.decodeChallenge(parsedJson.get(AECNamespace.Path.EAP_RELAY_PACKET)));
            return new CalcEapChallenge();
        }
    }

    private class ParseConfiguration implements WorkflowImpl.Workflow {
        private ParseConfiguration() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            Map<String, String> parsedXml = ContentParser.parseXml(new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8));
            if (parsedXml.isEmpty()) {
                return new Stop();
            }
            WorkflowTS43.this.mHttpJar.setParsedXml(parsedXml);
            return new StoreConfiguration();
        }
    }

    private class StoreConfiguration implements WorkflowImpl.Workflow {
        private StoreConfiguration() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            Map<String, String> parsedXml = WorkflowTS43.this.mHttpJar.getParsedXml();
            Date currDate = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
            parsedXml.put(AECNamespace.Path.TIMESTAMP, sdf.format(currDate));
            ContentParser.debugPrint(WorkflowTS43.this.mPhoneId, parsedXml);
            WorkflowTS43.this.mAECJar.setConfiguration(parsedXml);
            SimpleEventLog simpleEventLog = WorkflowTS43.this.mEventLog;
            simpleEventLog.add("StoreConfiguration: " + sdf.format(currDate));
            return new Finish();
        }
    }

    private class Stop implements WorkflowImpl.Workflow {
        private Stop() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            int unused = WorkflowTS43.this.mTryCnt = 3;
            int unused2 = WorkflowTS43.this.mTryCntNoAnswer = 1;
            WorkflowTS43.this.mHttpJar.clearHttpStore();
            Message msg = WorkflowTS43.this.obtainMessage();
            msg.what = 1002;
            msg.arg1 = WorkflowTS43.this.mPhoneId;
            msg.arg2 = 0;
            WorkflowTS43.this.sendMessage(msg);
            return null;
        }
    }

    private class Finish implements WorkflowImpl.Workflow {
        private Finish() {
        }

        public WorkflowImpl.Workflow run() throws Exception {
            int unused = WorkflowTS43.this.mTryCnt = 3;
            int unused2 = WorkflowTS43.this.mTryCntNoAnswer = 1;
            WorkflowTS43.this.mHttpJar.clearHttpStore();
            Message msg = WorkflowTS43.this.obtainMessage();
            msg.what = 1003;
            WorkflowTS43.this.sendMessage(msg);
            return null;
        }
    }
}
