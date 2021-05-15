package com.sec.internal.ims.cmstore.params;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class FailedAPICallResponseParam {
    private String mApiName = null;
    public String mErrorCode;
    public IHttpAPICommonInterface mRequest;

    public FailedAPICallResponseParam(IHttpAPICommonInterface request, String errorCode) {
        this.mRequest = request;
        if (request != null) {
            this.mApiName = request.getClass().getSimpleName();
        }
        this.mErrorCode = errorCode;
    }

    public String toString() {
        return "FailedAPICallResponseParam [mApiName=" + this.mApiName + ", mErrorCode=" + this.mErrorCode + "]";
    }
}
