package com.sec.internal.omanetapi.nms.data;

import java.net.URL;
import java.util.Arrays;

public class FlagList {
    public String[] flag;
    public URL resourceURL;

    public String toString() {
        return "FlagList{flag=" + Arrays.toString(this.flag) + ", resourceURL=" + this.resourceURL + '}';
    }
}
