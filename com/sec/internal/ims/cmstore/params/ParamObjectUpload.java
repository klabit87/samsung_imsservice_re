package com.sec.internal.ims.cmstore.params;

import android.util.Pair;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.omanetapi.nms.data.Object;

public class ParamObjectUpload {
    public final BufferDBChangeParam bufferDbParam;
    public final Pair<Object, HttpPostBody> uploadObjectInfo;

    public ParamObjectUpload(Pair<Object, HttpPostBody> objectinfo, BufferDBChangeParam param) {
        this.uploadObjectInfo = objectinfo;
        this.bufferDbParam = param;
    }
}
