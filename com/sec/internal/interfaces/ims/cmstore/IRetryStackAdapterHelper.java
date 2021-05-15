package com.sec.internal.interfaces.ims.cmstore;

public interface IRetryStackAdapterHelper {
    boolean checkRequestRetried(IHttpAPICommonInterface iHttpAPICommonInterface);

    void clearRetryHistory();

    IHttpAPICommonInterface getLastFailedRequest();

    boolean isEmpty();

    boolean isRetryTimesFinished();

    IHttpAPICommonInterface pop();

    void retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper);

    boolean retryLastApi(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper);

    void saveRetryLastFailedTime(long j);

    boolean searchAndPush(IHttpAPICommonInterface iHttpAPICommonInterface);
}
