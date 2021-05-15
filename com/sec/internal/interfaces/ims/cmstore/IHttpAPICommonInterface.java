package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.helper.httpclient.HttpRequestParams;
import java.io.Serializable;

public interface IHttpAPICommonInterface extends Serializable, Cloneable {
    HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener);

    HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper);

    void updateServerRoot(String str);
}
