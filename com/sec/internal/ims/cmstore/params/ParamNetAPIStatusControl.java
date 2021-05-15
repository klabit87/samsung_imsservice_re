package com.sec.internal.ims.cmstore.params;

public class ParamNetAPIStatusControl {
    public final boolean mIsDefaultMsgAppNative;
    public final boolean mIsMsgAppForeground;
    public final boolean mIsNetworkValid;
    public final boolean mIsOMANetAPIRunning;
    public final boolean mIsProvisionSuccess;
    public final boolean mIsUserDeleteAccount;

    public ParamNetAPIStatusControl(boolean isMsgAppForeground, boolean isNetworkValid, boolean isAmbsRunning, boolean isDefaultMsgAppNative, boolean isUserDeleteAccount, boolean isProvisionSuccess) {
        this.mIsMsgAppForeground = isMsgAppForeground;
        this.mIsNetworkValid = isNetworkValid;
        this.mIsOMANetAPIRunning = isAmbsRunning;
        this.mIsDefaultMsgAppNative = isDefaultMsgAppNative;
        this.mIsUserDeleteAccount = isUserDeleteAccount;
        this.mIsProvisionSuccess = isProvisionSuccess;
    }

    public String toString() {
        return "ParamNetAPIStatusControl [mIsMsgAppForeground= " + this.mIsMsgAppForeground + " mIsNetworkValid = " + this.mIsNetworkValid + " mIsOMANetAPIRunning = " + this.mIsOMANetAPIRunning + "mIsDefaultMsgAppNative = " + this.mIsDefaultMsgAppNative + "mIsUserDeleteAccount = " + this.mIsUserDeleteAccount + " mIsProvisionSuccess = " + this.mIsProvisionSuccess + "]";
    }
}
