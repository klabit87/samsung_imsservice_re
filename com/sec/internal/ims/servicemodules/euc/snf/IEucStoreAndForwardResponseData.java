package com.sec.internal.ims.servicemodules.euc.snf;

import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;

public interface IEucStoreAndForwardResponseData {
    IEucStoreAndForward.IResponseCallback getCallback();

    IEucData getEUCData();

    String getPIN();

    EucResponseData.Response getResponse();

    IEucStoreAndForward.IResponseHandle getResponseToWorkflowHandle();
}
