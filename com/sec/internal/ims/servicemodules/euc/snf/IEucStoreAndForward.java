package com.sec.internal.ims.servicemodules.euc.snf;

import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;

public interface IEucStoreAndForward {

    public interface IResponseCallback {
        void onStatus(EucSendResponseStatus eucSendResponseStatus);
    }

    public interface IResponseHandle {
        void invalidate();
    }

    void forward(String str);

    IResponseHandle sendResponse(IEucData iEucData, EucResponseData.Response response, IResponseCallback iResponseCallback);

    IResponseHandle sendResponse(IEucData iEucData, EucResponseData.Response response, String str, IResponseCallback iResponseCallback);

    void store(String str);
}
