package com.sec.internal.omanetapi.nms.data;

import com.sec.internal.omanetapi.common.data.Link;

public class NmsEventList {
    public String callbackData;
    public Long index = 0L;
    public Link[] link;
    public NmsEvent[] nmsEvent;
    public String restartToken = "";
}
