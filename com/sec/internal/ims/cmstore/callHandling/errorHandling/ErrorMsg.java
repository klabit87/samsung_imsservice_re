package com.sec.internal.ims.cmstore.callHandling.errorHandling;

public class ErrorMsg {
    ErrorType mType;
    public int mTypeResId;

    public ErrorMsg(ErrorType type, int typeResId) {
        this.mType = type;
        this.mTypeResId = typeResId;
    }

    public String toString() {
        return "ErrorMsg [mType=" + this.mType + ", mTypeResId=" + this.mTypeResId + "]";
    }
}
