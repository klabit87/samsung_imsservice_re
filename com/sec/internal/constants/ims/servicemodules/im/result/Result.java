package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.MsrpResponse;
import com.sec.internal.constants.ims.servicemodules.im.SipResponse;

public class Result {
    private ImError mImError;
    private MsrpResponse mMsrpResponse;
    private ReasonHeader mReasonHdr;
    private SipResponse mSipResponse;
    private Type mType;
    private WarningHeader mWarningHdr;

    public enum Type {
        NONE,
        SUCCESS,
        SIP_ERROR,
        MSRP_ERROR,
        ENGINE_ERROR,
        SESSION_RELEASE,
        NETWORK_ERROR,
        SESSION_RSRC_UNAVAILABLE,
        REMOTE_PARTY_CANCELED,
        DEVICE_UNREGISTERED,
        SIP_PROVISIONAL,
        UNKNOWN_ERROR,
        DEDICATED_BEARER_ERROR
    }

    public Result(ImError imError, Type type) {
        init(imError, type, (SipResponse) null, (MsrpResponse) null, (WarningHeader) null, (ReasonHeader) null);
    }

    public Result(ImError imError, Result base) {
        init(imError, base.mType, base.mSipResponse, base.mMsrpResponse, base.mWarningHdr, base.mReasonHdr);
    }

    public Result(ImError imError, Type type, SipResponse sipResponse, MsrpResponse msrpResponse, WarningHeader warningHeader, ReasonHeader reasonHeader) {
        init(imError, type, sipResponse, msrpResponse, warningHeader, reasonHeader);
    }

    private void init(ImError imError, Type type, SipResponse sipResponse, MsrpResponse msrpResponse, WarningHeader warningHeader, ReasonHeader reasonHeader) {
        this.mImError = (ImError) Preconditions.checkNotNull(imError);
        this.mType = (Type) Preconditions.checkNotNull(type);
        this.mSipResponse = sipResponse;
        this.mMsrpResponse = msrpResponse;
        this.mWarningHdr = warningHeader;
        this.mReasonHdr = reasonHeader;
    }

    public static class WarningHeader {
        private final int mCode;
        private final String mText;

        public WarningHeader(int code, String text) {
            this.mCode = code;
            this.mText = text;
        }

        public int getCode() {
            return this.mCode;
        }

        public String getText() {
            return this.mText;
        }
    }

    public static class ReasonHeader {
        private final int mCode;
        private final String mText;

        public ReasonHeader(int code, String text) {
            this.mCode = code;
            this.mText = text;
        }

        public int getCode() {
            return this.mCode;
        }

        public String getText() {
            return this.mText;
        }
    }

    public ImError getImError() {
        return this.mImError;
    }

    public Type getType() {
        return this.mType;
    }

    public SipResponse getSipResponse() {
        return this.mSipResponse;
    }

    public MsrpResponse getMsrpResponse() {
        return this.mMsrpResponse;
    }

    public WarningHeader getWarningHdr() {
        return this.mWarningHdr;
    }

    public ReasonHeader getReasonHdr() {
        return this.mReasonHdr;
    }

    public String toString() {
        String str = "Result [mType=" + this.mType;
        if (this.mImError != null) {
            str = str + ", mImError=" + this.mImError;
        }
        if (this.mSipResponse != null) {
            str = str + ", mSipResponse=" + this.mSipResponse;
        }
        if (this.mMsrpResponse != null) {
            str = str + ", mMsrpResponse=" + this.mMsrpResponse;
        }
        if (this.mWarningHdr != null) {
            str = str + ", mWarningHdr=" + this.mWarningHdr;
        }
        if (this.mReasonHdr != null) {
            str = str + ", mReasonHdr=" + this.mReasonHdr;
        }
        return str + "]";
    }

    public String toCriticalLog() {
        String str = "T=" + this.mType;
        if (this.mImError != null) {
            str = str + ",i=" + this.mImError;
        }
        if (this.mSipResponse != null) {
            str = str + ",s=" + this.mSipResponse;
        }
        if (this.mMsrpResponse != null) {
            str = str + ",m==" + this.mMsrpResponse;
        }
        if (this.mWarningHdr != null) {
            str = str + ",w=" + this.mWarningHdr;
        }
        if (this.mReasonHdr == null) {
            return str;
        }
        return str + ",r=" + this.mReasonHdr;
    }
}
