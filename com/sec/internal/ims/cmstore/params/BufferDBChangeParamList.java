package com.sec.internal.ims.cmstore.params;

import java.util.ArrayList;
import java.util.Iterator;

public class BufferDBChangeParamList {
    public ArrayList<BufferDBChangeParam> mChangelst = new ArrayList<>();

    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("BufferDBChangeParamList: ");
        Iterator<BufferDBChangeParam> it = this.mChangelst.iterator();
        while (it.hasNext()) {
            strBuilder.append(it.next());
        }
        return strBuilder.toString();
    }
}
