package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.ims.cmstore.CloudMessageIntent;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReqSession extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = ReqSession.class.getSimpleName();
    private static final long serialVersionUID = 890056112766767377L;

    public ReqSession(final IAPICallFlowListener callFlowListener, final IRetryStackAdapterHelper retryStackAdapterHelper, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.POST);
        setPostParams(makePostData());
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: java.lang.Object} */
            /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: java.lang.String} */
            /* JADX WARNING: Multi-variable type inference failed */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onComplete(com.sec.internal.helper.httpclient.HttpResponseParams r11) {
                /*
                    r10 = this;
                    int r0 = r11.getStatusCode()
                    java.lang.String r1 = "default_error_type"
                    r2 = 302(0x12e, float:4.23E-43)
                    if (r0 != r2) goto L_0x0157
                    java.util.Map r0 = r11.getHeaders()
                    java.lang.String r2 = "location"
                    java.lang.Object r0 = r0.get(r2)
                    java.util.List r0 = (java.util.List) r0
                    r2 = 0
                    if (r0 == 0) goto L_0x0027
                    int r3 = r0.size()
                    if (r3 <= 0) goto L_0x0027
                    r3 = 0
                    java.lang.Object r3 = r0.get(r3)
                    r2 = r3
                    java.lang.String r2 = (java.lang.String) r2
                L_0x0027:
                    java.lang.String r3 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.TAG
                    java.lang.StringBuilder r4 = new java.lang.StringBuilder
                    r4.<init>()
                    java.lang.String r5 = "location: "
                    r4.append(r5)
                    r4.append(r2)
                    java.lang.String r4 = r4.toString()
                    android.util.Log.d(r3, r4)
                    boolean r3 = android.text.TextUtils.isEmpty(r2)
                    if (r3 == 0) goto L_0x004b
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r3 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r3.goFailedCall(r1)
                    return
                L_0x004b:
                    r3 = -1
                    r4 = -1
                    if (r2 == 0) goto L_0x005b
                    java.lang.String r5 = com.sec.internal.ims.cmstore.helper.ATTGlobalVariables.ACMS_TARGET_URL
                    int r3 = r2.indexOf(r5)
                    java.lang.String r5 = "errorCode"
                    int r4 = r2.indexOf(r5)
                L_0x005b:
                    if (r4 < 0) goto L_0x0083
                    r1 = 38
                    java.lang.String r5 = "errorCode="
                    java.lang.String r1 = com.sec.internal.ims.cmstore.ambs.globalsetting.AmbsUtils.findErrorCode(r2, r5, r1)
                    java.lang.String r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.TAG
                    java.lang.StringBuilder r6 = new java.lang.StringBuilder
                    r6.<init>()
                    java.lang.String r7 = "errorCode: "
                    r6.append(r7)
                    r6.append(r1)
                    java.lang.String r6 = r6.toString()
                    android.util.Log.d(r5, r6)
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r5.goFailedCall(r1)
                    return
                L_0x0083:
                    if (r3 < 0) goto L_0x0156
                    com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper r1 = r4
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r1 = r1.getLastFailedRequest()
                    java.lang.String r5 = "HAP.STEADY.STATE.REQ.HUIMSTOKEN"
                    if (r1 != 0) goto L_0x00ae
                    com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r6 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
                    boolean r6 = r6.ifSteadyState()
                    if (r6 == 0) goto L_0x009f
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r6 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r6.goSuccessfulCall(r5)
                    goto L_0x00ad
                L_0x009f:
                    java.lang.String r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.TAG
                    java.lang.String r6 = "not steady state"
                    android.util.Log.d(r5, r6)
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r5.goSuccessfulCall()
                L_0x00ad:
                    return
                L_0x00ae:
                    java.lang.Class r6 = r1.getClass()
                    java.lang.String r6 = r6.getSimpleName()
                    java.lang.String r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.TAG
                    java.lang.StringBuilder r8 = new java.lang.StringBuilder
                    r8.<init>()
                    java.lang.String r9 = "lastFailedApiName: "
                    r8.append(r9)
                    r8.append(r6)
                    java.lang.String r9 = "SteadyState: "
                    r8.append(r9)
                    com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r9 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
                    boolean r9 = r9.ifSteadyState()
                    r8.append(r9)
                    java.lang.String r8 = r8.toString()
                    android.util.Log.d(r7, r8)
                    com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r7 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()
                    boolean r7 = r7.ifSteadyState()
                    java.lang.String r8 = "HAP.DELETE.ACCOUNT"
                    if (r7 == 0) goto L_0x013e
                    java.lang.Class<com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount> r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestCreateAccount.class
                    java.lang.String r7 = r7.getSimpleName()
                    boolean r7 = r6.equals(r7)
                    if (r7 == 0) goto L_0x00fe
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    java.lang.String r7 = "HAP.CREATE.ACCOUNT"
                    r5.goSuccessfulCall(r7)
                    goto L_0x0155
                L_0x00fe:
                    java.lang.Class<com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount> r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestAccount.class
                    java.lang.String r7 = r7.getSimpleName()
                    boolean r7 = r6.equals(r7)
                    if (r7 == 0) goto L_0x0112
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    java.lang.String r7 = "HAP.GET.SVC.ACCOUNT"
                    r5.goSuccessfulCall(r7)
                    goto L_0x0155
                L_0x0112:
                    java.lang.Class<com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount> r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount.class
                    java.lang.String r7 = r7.getSimpleName()
                    boolean r7 = r6.equals(r7)
                    if (r7 == 0) goto L_0x0124
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r5.goSuccessfulCall(r8)
                    goto L_0x0155
                L_0x0124:
                    java.lang.Class<com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC> r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestTC.class
                    java.lang.String r7 = r7.getSimpleName()
                    boolean r7 = r6.equals(r7)
                    if (r7 == 0) goto L_0x0138
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    java.lang.String r7 = "HAP.GET.TC"
                    r5.goSuccessfulCall(r7)
                    goto L_0x0155
                L_0x0138:
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r7 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r7.goSuccessfulCall(r5)
                    goto L_0x0155
                L_0x013e:
                    java.lang.Class<com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount> r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.RequestDeleteAccount.class
                    java.lang.String r5 = r5.getSimpleName()
                    boolean r5 = r6.equals(r5)
                    if (r5 == 0) goto L_0x0150
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r5.goSuccessfulCall(r8)
                    goto L_0x0155
                L_0x0150:
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r5 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r5.goSuccessfulCall()
                L_0x0155:
                    return
                L_0x0156:
                    goto L_0x0179
                L_0x0157:
                    int r0 = r11.getStatusCode()
                    r2 = 503(0x1f7, float:7.05E-43)
                    if (r0 == r2) goto L_0x0167
                    int r0 = r11.getStatusCode()
                    r2 = 429(0x1ad, float:6.01E-43)
                    if (r0 != r2) goto L_0x0179
                L_0x0167:
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r0 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    int r0 = r0.checkRetryAfter(r11)
                    if (r0 <= 0) goto L_0x0179
                    com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r1 = r3
                    com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2 = r0
                    java.lang.String r3 = "RetryAfterRule"
                    r1.onOverRequest(r2, r3, r0)
                    return
                L_0x0179:
                    java.lang.String r0 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.TAG
                    java.lang.String r2 = "all other responses"
                    android.util.Log.d(r0, r2)
                    com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession r0 = com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.this
                    r0.goFailedCall(r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.ambs.cloudrequest.ReqSession.AnonymousClass1.onComplete(com.sec.internal.helper.httpclient.HttpResponseParams):void");
            }

            public void onFail(IOException arg1) {
                String access$000 = ReqSession.TAG;
                Log.e(access$000, "Http request onFail: " + arg1.getMessage());
                ReqSession.this.goFailedCall();
            }
        });
    }

    private Map<String, String> makePostData() {
        Map<String, String> postData = new HashMap<>();
        postData.put("TG_OP", SoftphoneNamespaces.SoftphoneSettings.TGUARD_MSIP_OPERATION);
        postData.put("appID", ATTGlobalVariables.APP_ID);
        String atsToken = CloudMessagePreferenceManager.getInstance().getAtsToken();
        if (TextUtils.isEmpty(atsToken)) {
            atsToken = "null";
        }
        postData.put("atsToken", atsToken);
        String ctn = CloudMessagePreferenceManager.getInstance().getUserCtn();
        String str = TAG;
        Log.d(str, "ctnID: " + IMSLog.checker(ctn));
        postData.put("ctnID", ctn);
        postData.put(CloudMessageIntent.ExtrasAMBSUI.STYLE, ATTGlobalVariables.URL_PARAM_STYLE);
        postData.put("targetURL", ATTGlobalVariables.ACMS_TARGET_URL);
        postData.put("returnErrorCode", CloudMessageProviderContract.JsonData.TRUE);
        return postData;
    }

    public void updateUrl() {
        setUrl("https://" + ATTGlobalVariables.ACMS_HOST_NAME + SoftphoneNamespaces.SoftphoneSettings.MSIP_TOKEN_PATH);
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        return new ReqSession(callback, retryStackAdapterHelper, cloudMessageManagerHelper);
    }
}
