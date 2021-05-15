package com.sec.internal.ims.cmstore.omanetapi.nms;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.omanetapi.nms.IndividualSubscription;
import java.io.IOException;

public class CloudMessageDeleteIndividualSubscription extends IndividualSubscription {
    private static final long serialVersionUID = 1;

    public CloudMessageDeleteIndividualSubscription(final IAPICallFlowListener callFlowListener, String url, final ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(url);
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, iCloudMessageManagerHelper);
        initCommonRequestHeaders(iCloudMessageManagerHelper.getContentType(), iCloudMessageManagerHelper.getValidTokenByLine(iCloudMessageManagerHelper.getUserTelCtn()));
        initCommonDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams result) {
                if (CloudMessageDeleteIndividualSubscription.this.shouldCareAfterResponsePreProcess(callFlowListener, result, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iCloudMessageManagerHelper.saveOMASubscriptionTime(0);
                    iCloudMessageManagerHelper.saveOMASubscriptionChannelDuration(0);
                    if (result.getStatusCode() == 200 || result.getStatusCode() == 204) {
                        callFlowListener.onSuccessfulCall(this);
                    } else {
                        callFlowListener.onMoveOnToNext(CloudMessageDeleteIndividualSubscription.this, (Object) null);
                    }
                }
            }

            public void onFail(IOException arg1) {
            }
        });
    }
}
