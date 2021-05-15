package com.sec.internal.constants.ims.servicemodules.im.result;

import com.android.internal.util.Preconditions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.log.IMSLog;

public class StartImSessionResult {
    public String mAllowedMethods;
    public boolean mIsChatbotRole;
    public boolean mIsMsgFallbackSupported;
    public boolean mIsMsgRevokeSupported;
    public boolean mIsProvisional;
    public Object mRawHandle;
    public String mRemoteUserDisplayName;
    public final Result mResult;
    public int mRetryTimer;
    public ImsUri mSessionUri;

    public StartImSessionResult(Result result, ImsUri uri, Object rawHandle) {
        this(result, uri, rawHandle, 0, (String) null, false, false, false, "");
    }

    public StartImSessionResult(Result result, ImsUri uri, Object rawHandle, boolean isProvisional) {
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mSessionUri = uri;
        this.mRawHandle = rawHandle;
        this.mIsProvisional = isProvisional;
        this.mRetryTimer = 0;
        this.mAllowedMethods = null;
        this.mRemoteUserDisplayName = "";
    }

    public StartImSessionResult(Result result, ImsUri uri, Object rawHandle, int retryTimer, String allowedMethods, boolean isMsgRevokeSupported, boolean isMsgFallbackSupported, boolean isChatbotRole, String displayName) {
        this.mResult = (Result) Preconditions.checkNotNull(result);
        this.mRemoteUserDisplayName = (String) Preconditions.checkNotNull(displayName);
        this.mSessionUri = uri;
        this.mRawHandle = rawHandle;
        this.mRetryTimer = retryTimer;
        this.mAllowedMethods = allowedMethods;
        this.mIsMsgRevokeSupported = isMsgRevokeSupported;
        this.mIsMsgFallbackSupported = isMsgFallbackSupported;
        this.mIsChatbotRole = isChatbotRole;
    }

    public String toString() {
        return "StartImSessionResult [mResult=" + this.mResult + ", mSessionUri=" + this.mSessionUri + ", mRawHandle=" + this.mRawHandle + ", mRetryTimer=" + this.mRetryTimer + ", mAllowedMethods=" + this.mAllowedMethods + ", mIsProvisional=" + this.mIsProvisional + ", mIsMsgRevokeSupported=" + this.mIsMsgRevokeSupported + ", mIsChatbotRole=" + this.mIsChatbotRole + ", mRemoteUserDisplayName=" + IMSLog.checker(this.mRemoteUserDisplayName) + "]";
    }

    public String toCriticalLog() {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append("r=");
        sb.append(this.mRawHandle);
        sb.append(",t=");
        sb.append(this.mRetryTimer);
        sb.append(",p=");
        String str2 = "1";
        sb.append(this.mIsProvisional ? str2 : "0");
        sb.append(",v=");
        if (this.mIsMsgRevokeSupported) {
            str = str2;
        } else {
            str = "0";
        }
        sb.append(str);
        sb.append(",b=");
        if (!this.mIsChatbotRole) {
            str2 = "0";
        }
        sb.append(str2);
        sb.append(",u=");
        sb.append(this.mSessionUri);
        return sb.toString();
    }
}
