package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.DeviceTuple;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Element;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.PersonTuple;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ServiceTuple;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestPresencePublish extends Table {
    public static RequestPresencePublish getRootAsRequestPresencePublish(ByteBuffer _bb) {
        return getRootAsRequestPresencePublish(_bb, new RequestPresencePublish());
    }

    public static RequestPresencePublish getRootAsRequestPresencePublish(ByteBuffer _bb, RequestPresencePublish obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestPresencePublish __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long handle() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String uri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public PersonTuple personTuples(int j) {
        return personTuples(new PersonTuple(), j);
    }

    public PersonTuple personTuples(PersonTuple obj, int j) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int personTuplesLength() {
        int o = __offset(8);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public ServiceTuple serviceTuples(int j) {
        return serviceTuples(new ServiceTuple(), j);
    }

    public ServiceTuple serviceTuples(ServiceTuple obj, int j) {
        int o = __offset(10);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int serviceTuplesLength() {
        int o = __offset(10);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public DeviceTuple deviceTuples(int j) {
        return deviceTuples(new DeviceTuple(), j);
    }

    public DeviceTuple deviceTuples(DeviceTuple obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int deviceTuplesLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Element notes(int j) {
        return notes(new Element(), j);
    }

    public Element notes(Element obj, int j) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int notesLength() {
        int o = __offset(14);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String timestamp() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer timestampAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public long expireTime() {
        int o = __offset(18);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String eTag() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer eTagAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public boolean gzipEnable() {
        int o = __offset(22);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createRequestPresencePublish(FlatBufferBuilder builder, long handle, int uriOffset, int person_tuplesOffset, int service_tuplesOffset, int device_tuplesOffset, int notesOffset, int timestampOffset, long expire_time, int e_tagOffset, boolean gzip_enable) {
        builder.startObject(10);
        addETag(builder, e_tagOffset);
        addExpireTime(builder, expire_time);
        addTimestamp(builder, timestampOffset);
        addNotes(builder, notesOffset);
        addDeviceTuples(builder, device_tuplesOffset);
        addServiceTuples(builder, service_tuplesOffset);
        addPersonTuples(builder, person_tuplesOffset);
        addUri(builder, uriOffset);
        addHandle(builder, handle);
        addGzipEnable(builder, gzip_enable);
        return endRequestPresencePublish(builder);
    }

    public static void startRequestPresencePublish(FlatBufferBuilder builder) {
        builder.startObject(10);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(1, uriOffset, 0);
    }

    public static void addPersonTuples(FlatBufferBuilder builder, int personTuplesOffset) {
        builder.addOffset(2, personTuplesOffset, 0);
    }

    public static int createPersonTuplesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startPersonTuplesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addServiceTuples(FlatBufferBuilder builder, int serviceTuplesOffset) {
        builder.addOffset(3, serviceTuplesOffset, 0);
    }

    public static int createServiceTuplesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startServiceTuplesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addDeviceTuples(FlatBufferBuilder builder, int deviceTuplesOffset) {
        builder.addOffset(4, deviceTuplesOffset, 0);
    }

    public static int createDeviceTuplesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startDeviceTuplesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addNotes(FlatBufferBuilder builder, int notesOffset) {
        builder.addOffset(5, notesOffset, 0);
    }

    public static int createNotesVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startNotesVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addTimestamp(FlatBufferBuilder builder, int timestampOffset) {
        builder.addOffset(6, timestampOffset, 0);
    }

    public static void addExpireTime(FlatBufferBuilder builder, long expireTime) {
        builder.addInt(7, (int) expireTime, 0);
    }

    public static void addETag(FlatBufferBuilder builder, int eTagOffset) {
        builder.addOffset(8, eTagOffset, 0);
    }

    public static void addGzipEnable(FlatBufferBuilder builder, boolean gzipEnable) {
        builder.addBoolean(9, gzipEnable, false);
    }

    public static int endRequestPresencePublish(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
