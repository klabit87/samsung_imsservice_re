package com.sec.internal.constants.ims.config;

import com.sec.internal.log.IMSLog;

public final class RcsConfig {
    private int mCbMsgTech;
    private String mConfUri;
    private String mDownloadsPath;
    private String mEndUserConfReqId;
    private String mExploderUri;
    private int mFtChunkSize;
    private boolean mIsAggrImdnSupported;
    private boolean mIsConfSubscribeEnabled;
    private boolean mIsMsrpCema;
    private boolean mIsPrivacyDisable;
    private int mIshChunkSize;
    private int mPagerModeLimit;
    private String mSuspendUser;
    private boolean mUseMsrpDiscardPort;

    public RcsConfig(String suspendUser) {
        this.mSuspendUser = suspendUser;
    }

    public RcsConfig(int ftChunkSize, int ishChunkSize, String confUri, boolean isMsrpCema, String downloadsPath, boolean isConfSubscribeEnabled, String exploderUri, int pagerModeLimit, boolean useMsrpDiscardPort, boolean isAggrImdnSupported, boolean isPrivacyDisable, int cbMsgTech, String endUserConfReqId) {
        this.mFtChunkSize = ftChunkSize;
        this.mIshChunkSize = ishChunkSize;
        this.mConfUri = confUri;
        this.mIsMsrpCema = isMsrpCema;
        this.mDownloadsPath = downloadsPath;
        this.mIsConfSubscribeEnabled = isConfSubscribeEnabled;
        this.mExploderUri = exploderUri;
        this.mPagerModeLimit = pagerModeLimit;
        this.mUseMsrpDiscardPort = useMsrpDiscardPort;
        this.mIsAggrImdnSupported = isAggrImdnSupported;
        this.mIsPrivacyDisable = isPrivacyDisable;
        this.mCbMsgTech = cbMsgTech;
        this.mEndUserConfReqId = endUserConfReqId;
    }

    public int getFtChunkSize() {
        return this.mFtChunkSize;
    }

    public int getIshChunkSize() {
        return this.mIshChunkSize;
    }

    public String getConfUri() {
        return this.mConfUri;
    }

    public boolean isMsrpCema() {
        return this.mIsMsrpCema;
    }

    public String getDownloadsPath() {
        return this.mDownloadsPath;
    }

    public boolean isConfSubscribeEnabled() {
        return this.mIsConfSubscribeEnabled;
    }

    public String getExploderUri() {
        return this.mExploderUri;
    }

    public int getPagerModeLimit() {
        return this.mPagerModeLimit;
    }

    public boolean isUseMsrpDiscardPort() {
        return this.mUseMsrpDiscardPort;
    }

    public boolean isAggrImdnSupported() {
        return this.mIsAggrImdnSupported;
    }

    public boolean isPrivacyDisable() {
        return this.mIsPrivacyDisable;
    }

    public int getCbMsgTech() {
        return this.mCbMsgTech;
    }

    public String getEndUserConfReqId() {
        return this.mEndUserConfReqId;
    }

    public String getSuspendUser() {
        return this.mSuspendUser;
    }

    public String toString() {
        return "RcsConfig[chunksize = " + this.mFtChunkSize + " / " + this.mIshChunkSize + ", confuri = " + IMSLog.checker(this.mConfUri) + ", is msrp cema = " + this.mIsMsrpCema + ", downloads path = " + this.mDownloadsPath + ", conf.subscribe enabled = " + this.mIsConfSubscribeEnabled + ", exploderUri = " + this.mExploderUri + ", pagerModeLimit = " + this.mPagerModeLimit + ", useMsrpDiscardPort = " + this.mUseMsrpDiscardPort + ", aggr.imdn supported = " + this.mIsAggrImdnSupported + ", privacyDisable = " + this.mIsPrivacyDisable + ", cbMsgTech = " + this.mCbMsgTech + ", endUserConfReqId = " + IMSLog.checker(this.mEndUserConfReqId) + "]";
    }
}
