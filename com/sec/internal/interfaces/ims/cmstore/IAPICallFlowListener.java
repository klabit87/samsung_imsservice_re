package com.sec.internal.interfaces.ims.cmstore;

import android.os.Message;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;

public interface IAPICallFlowListener {
    void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface);

    void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, BufferDBChangeParam bufferDBChangeParam);

    void onFailedCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str);

    void onFailedEvent(int i, Object obj);

    void onFixedFlow(int i);

    void onFixedFlowWithMessage(Message message);

    void onGoToEvent(int i, Object obj);

    void onMoveOnToNext(IHttpAPICommonInterface iHttpAPICommonInterface, Object obj);

    void onOverRequest(IHttpAPICommonInterface iHttpAPICommonInterface, String str, int i);

    void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface);

    void onSuccessfulCall(IHttpAPICommonInterface iHttpAPICommonInterface, String str);

    void onSuccessfulEvent(IHttpAPICommonInterface iHttpAPICommonInterface, int i, Object obj);
}
