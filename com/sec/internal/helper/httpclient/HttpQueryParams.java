package com.sec.internal.helper.httpclient;

import java.util.LinkedHashMap;

public class HttpQueryParams {
    private LinkedHashMap<String, String> mParams = new LinkedHashMap<>();
    private boolean mParamsEncoded;

    public HttpQueryParams(LinkedHashMap<String, String> params, boolean encoded) {
        this.mParams = params;
        this.mParamsEncoded = encoded;
    }

    public LinkedHashMap<String, String> getParams() {
        return this.mParams;
    }

    public boolean isEncoded() {
        return this.mParamsEncoded;
    }

    public String toString() {
        return this.mParams.toString();
    }
}
