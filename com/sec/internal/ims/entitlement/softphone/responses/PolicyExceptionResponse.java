package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class PolicyExceptionResponse {
    @SerializedName("RequestError")
    public RequestError mRequestError;

    public static class RequestError {
        @SerializedName("PolicyException")
        public ExceptionResponse mException;

        public String toString() {
            return "RequestError [mException = " + this.mException + "]";
        }
    }

    public String toString() {
        return "PolicyExceptionResponse [mRequestError = " + this.mRequestError + "]";
    }
}
