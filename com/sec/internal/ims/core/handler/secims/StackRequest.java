package com.sec.internal.ims.core.handler.secims;

import com.google.flatbuffers.FlatBufferBuilder;

public class StackRequest {
    private final FlatBufferBuilder mRequestBuilder;
    private final int mRequestOffSet;

    private StackRequest() {
        this.mRequestBuilder = null;
        this.mRequestOffSet = -1;
    }

    StackRequest(FlatBufferBuilder builder, int offset) {
        this.mRequestBuilder = builder;
        this.mRequestOffSet = offset;
    }

    public FlatBufferBuilder getBuilder() {
        return this.mRequestBuilder;
    }

    public int getOffset() {
        return this.mRequestOffSet;
    }
}
