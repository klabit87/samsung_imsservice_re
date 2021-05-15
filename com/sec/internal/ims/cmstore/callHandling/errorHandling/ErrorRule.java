package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class ErrorRule {
    String mErrorCode;
    ErrorMsg mErrorMsg;
    int mFailEvent;
    RetryAttribute mRetryAttr;
    int mRetryEvent;

    public enum RetryAttribute {
        RETRY_ALLOW,
        RETRY_FORBIDDEN,
        RETRY_USE_HEADER_VALUE
    }

    public ErrorRule(String errType, RetryAttribute retryAttribute, int retryEvent, int failEvent, ErrorMsg errorMsg) {
        this.mErrorCode = errType;
        this.mRetryAttr = retryAttribute;
        this.mRetryEvent = retryEvent;
        this.mFailEvent = failEvent;
        this.mErrorMsg = errorMsg;
    }

    public String toString() {
        return "ErrorRule [mErrorCode=" + this.mErrorCode + ", mRetryAttr=" + this.mRetryAttr + ", mRetryEvent=" + this.mRetryEvent + ", mFailEvent=" + this.mFailEvent + ", mErrorMsg=" + this.mErrorMsg + "]";
    }
}
