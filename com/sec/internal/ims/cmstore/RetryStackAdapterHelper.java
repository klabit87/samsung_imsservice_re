package com.sec.internal.ims.cmstore;

import com.sec.internal.ims.cmstore.adapters.RetryStackAdapter;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;

public class RetryStackAdapterHelper implements IRetryStackAdapterHelper {
    public IHttpAPICommonInterface getLastFailedRequest() {
        return RetryStackAdapter.getInstance().getLastFailedRequest();
    }

    public IHttpAPICommonInterface pop() {
        return RetryStackAdapter.getInstance().pop();
    }

    public boolean isEmpty() {
        return RetryStackAdapter.getInstance().isEmpty();
    }

    public void retryApi(IHttpAPICommonInterface retry, IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        RetryStackAdapter.getInstance().retryApi(retry, callback, cloudMessageManagerHelper, retryStackAdapterHelper);
    }

    public boolean checkRequestRetried(IHttpAPICommonInterface request) {
        return RetryStackAdapter.getInstance().checkRequestRetried(request);
    }

    public boolean isRetryTimesFinished() {
        return RetryStackAdapter.getInstance().isRetryTimesFinished(new CloudMessageManagerHelper());
    }

    public void clearRetryHistory() {
        RetryStackAdapter.getInstance().clearRetryHistory();
    }

    public boolean searchAndPush(IHttpAPICommonInterface request) {
        return RetryStackAdapter.getInstance().searchAndPush(request);
    }

    public void saveRetryLastFailedTime(long time) {
        RetryStackAdapter.getInstance().saveRetryLastFailedTime(System.currentTimeMillis());
    }

    public boolean retryLastApi(IAPICallFlowListener callback, ICloudMessageManagerHelper cloudMessageManagerHelper, IRetryStackAdapterHelper retryStackAdapterHelper) {
        return RetryStackAdapter.getInstance().retryLastApi(callback, cloudMessageManagerHelper, retryStackAdapterHelper);
    }
}
