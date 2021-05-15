package com.sec.internal.ims.cmstore.params;

import java.util.ArrayList;
import java.util.Iterator;

public class ParamAppJsonValueList {
    public ArrayList<ParamAppJsonValue> mOperationList = new ArrayList<>();

    public String toString() {
        StringBuffer mString = new StringBuffer();
        mString.append("mOperationList: ");
        Iterator<ParamAppJsonValue> it = this.mOperationList.iterator();
        while (it.hasNext()) {
            mString.append(it.next());
        }
        return mString.toString();
    }
}
