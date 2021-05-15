package com.sec.internal.ims.entitlement.softphone.responses;

import com.google.gson.annotations.SerializedName;

public class ExceptionResponse {
    @SerializedName("MessageId")
    public String mMessageId;
    @SerializedName("Text")
    public String mText;
    @SerializedName("Values")
    public String mValues;
    @SerializedName("Variables")
    public String mVariables;

    public String toString() {
        return "ExceptionResponse [mMessageId = " + this.mMessageId + ", mText = " + this.mText + ", mVariables = " + this.mVariables + ", mValues = " + this.mValues + "]";
    }
}
