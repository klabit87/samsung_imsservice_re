package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.IOException;

public class ReqRetireSession extends BaseProvisionAPIRequest {
    /* access modifiers changed from: private */
    public static final String TAG = ReqRetireSession.class.getSimpleName();
    private static final long serialVersionUID = 2492635640514901L;

    public ReqRetireSession(IAPICallFlowListener callFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(callFlowListener, iCloudMessageManagerHelper);
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                String access$000 = ReqRetireSession.TAG;
                Log.d(access$000, "StatusCode: " + result.getStatusCode());
                if (result.getStatusCode() != 200) {
                }
            }

            public void onFail(IOException arg1) {
                Log.d(ReqRetireSession.TAG, "call was failed");
                ReqRetireSession.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + getMsDomainAndSessionHelper() + "/logout");
    }

    private String getMsDomainAndSessionHelper() {
        return CloudMessagePreferenceManager.getInstance().getRedirectDomain() + "/handset/session" + CloudMessagePreferenceManager.getInstance().getMsgStoreSessionId();
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelperr) {
        return new ReqRetireSession(callback, cloudMessageManagerHelper);
    }
}
