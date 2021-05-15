package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.log.IMSLog;

public class DeviceIMFTUpdateParam {
    public String mChatId;
    public String mImdnId;
    public String mLine;
    public CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public long mRowId;
    public int mTableindex;
    public CloudMessageBufferDBConstants.ActionStatusFlag mUpdateType;

    public DeviceIMFTUpdateParam(int index, CloudMessageBufferDBConstants.ActionStatusFlag type, CloudMessageBufferDBConstants.MsgOperationFlag operation, long rowId, String chatId, String imdnId, String line) {
        this.mTableindex = index;
        this.mUpdateType = type;
        this.mOperation = operation;
        this.mRowId = rowId;
        this.mChatId = chatId;
        this.mImdnId = imdnId;
        this.mLine = line;
    }

    public String toString() {
        return "DeviceIMFTUpdateParam [mTableindex=" + this.mTableindex + ", mUpdateType= " + this.mUpdateType + ", mOperation=" + this.mOperation + ", mRowId=" + this.mRowId + ", mChatId=" + this.mChatId + ", mImdnId=" + IMSLog.checker(this.mImdnId) + ", mLine=" + IMSLog.checker(this.mLine) + "]";
    }
}
