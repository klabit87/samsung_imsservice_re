package com.sec.internal.ims.cmstore.params;

import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;

public class HttpResParamsWrapper {
    public IHttpAPICommonInterface mApi;
    public Object mBufDbParams;

    public HttpResParamsWrapper(IHttpAPICommonInterface api, Object bufDbparams) {
        this.mApi = api;
        this.mBufDbParams = bufDbparams;
    }
}
