package com.sec.internal.ims.cmstore.params;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class SuccessfullAPICallResponseParam {
    private String mApiName = null;
    public String mCallFlow;
    public IHttpAPICommonInterface mRequest;

    public SuccessfullAPICallResponseParam(IHttpAPICommonInterface request, String callFlow) {
        this.mRequest = request;
        if (request != null) {
            this.mApiName = request.getClass().getSimpleName();
        }
        this.mCallFlow = callFlow;
    }

    public String getApiName() {
        return this.mApiName;
    }

    public String toString() {
        return "SuccessfullAPICallResponseParam [mApiName=" + this.mApiName + ", mCallFlow=" + this.mCallFlow + "]";
    }
}
