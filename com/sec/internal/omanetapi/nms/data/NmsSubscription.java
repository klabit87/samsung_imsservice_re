package com.sec.internal.omanetapi.nms.data;

import com.sec.internal.omanetapi.common.data.CallbackReference;
import java.net.URL;

public class NmsSubscription {
    public CallbackReference callbackReference;
    public String clientCorrelator;
    public Integer duration;
    public SearchCriteria filter;
    public Long index;
    public URL resourceURL;
    public String restartToken;
}
