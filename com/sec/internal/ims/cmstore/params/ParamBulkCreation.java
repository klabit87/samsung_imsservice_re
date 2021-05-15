package com.sec.internal.ims.cmstore.params;

import android.util.Pair;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import java.util.List;

public class ParamBulkCreation {
    public final BufferDBChangeParamList bufferDbParamList;
    public final String mLine;
    public final Pair<ObjectList, List<HttpPostBody>> uploadObjectInfo;

    public ParamBulkCreation(Pair<ObjectList, List<HttpPostBody>> objectinfo, BufferDBChangeParamList param, String line) {
        this.uploadObjectInfo = objectinfo;
        this.bufferDbParamList = param;
        this.mLine = line;
    }
}
