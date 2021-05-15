package com.sec.internal.ims.core.handler.secims;

import android.os.Message;
import com.google.flatbuffers.FlatBufferBuilder;

/* compiled from: StackIF */
class ImsRequest {
    private FlatBufferBuilder mReqBuffer;
    Message mResult;
    int mTid;

    ImsRequest() {
    }

    static ImsRequest obtain(FlatBufferBuilder requestBuffer, Message result) {
        ImsRequest ir = new ImsRequest();
        ir.mReqBuffer = requestBuffer;
        ir.mResult = result;
        if (result == null || result.getTarget() != null) {
            return ir;
        }
        throw new NullPointerException("Message target must not be null");
    }

    public FlatBufferBuilder getReqBuffer() {
        return this.mReqBuffer;
    }
}
