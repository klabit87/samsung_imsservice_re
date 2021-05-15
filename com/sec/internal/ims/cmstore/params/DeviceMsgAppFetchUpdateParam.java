package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;

public class DeviceMsgAppFetchUpdateParam {
    public long mBufferRowId;
    public boolean mIsFT;
    public int mTableindex;
    public long mTelephonyRowId;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceMsgAppFetchUpdateParam(int index, CloudMessageBufferDBConstants.ActionStatusFlag type, long bufferrowId, long telephonyRowId, boolean isFt) {
        this.mTableindex = index;
        this.mUpdateType = type;
        this.mBufferRowId = bufferrowId;
        this.mTelephonyRowId = telephonyRowId;
        this.mIsFT = isFt;
    }

    public String toString() {
        return "DeviceMsgAppFetchUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mBufferRowId=" + this.mBufferRowId + ", mTelephonyRowId=" + this.mTelephonyRowId + ", mIsFT=" + this.mIsFT + "]";
    }
}
