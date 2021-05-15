package com.sec.internal.ims.core.handler.secims;

import android.os.Message;
import com.google.flatbuffers.FlatBufferBuilder;

public class ResipStackRequest {
    public Message mCallback;
    public int mId;
    public int mOffset;
    public FlatBufferBuilder mRequest;

    public ResipStackRequest(int id, FlatBufferBuilder request, int offset, Message callback) {
        this.mId = id;
        this.mRequest = request;
        this.mOffset = offset;
        this.mCallback = callback;
    }
}
