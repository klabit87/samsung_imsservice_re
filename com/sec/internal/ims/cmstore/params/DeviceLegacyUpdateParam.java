package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.log.IMSLog;

public class DeviceLegacyUpdateParam {
    public String mCorrelationTag;
    public String mLine;
    public String mMId;
    public CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public long mRowId;
    public String mTRId;
    public int mTableindex;

    public DeviceLegacyUpdateParam(int index, CloudMessageBufferDBConstants.MsgOperationFlag operation, int rowid, String correlationTag, String mid, String trid, String line) {
        this.mTableindex = index;
        this.mOperation = operation;
        this.mRowId = (long) rowid;
        this.mCorrelationTag = correlationTag;
        this.mMId = mid;
        this.mTRId = trid;
        this.mLine = line;
    }

    public String toString() {
        return "DeviceLegacyUpdateParam [mTableindex=" + this.mTableindex + ", mOperation=" + this.mOperation + ", mRowId=" + this.mRowId + ", mCorrelationTag=" + this.mCorrelationTag + ", mMId=" + this.mMId + ", mTRId=" + this.mTRId + ", mLine=" + IMSLog.checker(this.mLine) + "]";
    }
}
