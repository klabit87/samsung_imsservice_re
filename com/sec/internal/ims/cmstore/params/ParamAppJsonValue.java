package com.sec.internal.ims.cmstore.params;

import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.log.IMSLog;

public class ParamAppJsonValue {
    public final String mAppType;
    public final String mChatId;
    public final String mCorrelationId;
    public final String mCorrelationTag;
    public final int mDataContractType;
    public final String mDataType;
    public final String mLine;
    public final CloudMessageBufferDBConstants.MsgOperationFlag mOperation;
    public final int mRowId;
    public final ParamVvmUpdate mVvmUpdate;

    public ParamAppJsonValue(String appType, String dataType, int contractType, int rowid, String chatId, String correlationTag, String correlationId, CloudMessageBufferDBConstants.MsgOperationFlag operation, String line, ParamVvmUpdate vvmparam) {
        this.mAppType = appType;
        this.mDataType = dataType;
        this.mDataContractType = contractType;
        this.mRowId = rowid;
        this.mChatId = chatId;
        if (line != null) {
            this.mLine = line;
        } else {
            this.mLine = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
        }
        this.mOperation = operation;
        this.mCorrelationTag = correlationTag;
        this.mCorrelationId = correlationId;
        this.mVvmUpdate = vvmparam;
    }

    public String toString() {
        return "ParamAppJsonValue [mAppType= " + this.mAppType + " mDataType = " + this.mDataType + " mDataContractType = " + this.mDataContractType + " mRowId = " + this.mRowId + " mChatId = " + this.mChatId + " mOperation = " + this.mOperation + " mLine = " + IMSLog.checker(this.mLine) + " mCorrelationTag = " + this.mCorrelationTag + " mCorrelationId = " + this.mCorrelationId + " mVvmUpdate = " + this.mVvmUpdate + "]";
    }
}
