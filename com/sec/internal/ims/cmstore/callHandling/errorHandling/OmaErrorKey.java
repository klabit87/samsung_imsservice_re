package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class OmaErrorKey {
    String mApiClass;
    int mErrorCode;
    String mHandlerClass;

    public OmaErrorKey(int code, String apiClass, String handlerClass) {
        this.mErrorCode = code;
        this.mApiClass = apiClass;
        this.mHandlerClass = handlerClass;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof OmaErrorKey) || ((OmaErrorKey) o).mErrorCode != this.mErrorCode || !((OmaErrorKey) o).mApiClass.equals(this.mApiClass) || !((OmaErrorKey) o).mHandlerClass.equals(this.mHandlerClass)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mErrorCode + this.mApiClass.hashCode() + this.mHandlerClass.hashCode();
    }

    public String toString() {
        return "OmaErrorKey = [ mErrorCode = " + this.mErrorCode + " ], [ mApiClass = " + this.mApiClass + " ], [ mHandlerClass = " + this.mHandlerClass + " ].";
    }
}
