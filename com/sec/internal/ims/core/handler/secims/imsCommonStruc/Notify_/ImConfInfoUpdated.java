package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ImConfInfoUpdated extends Table {
    public static ImConfInfoUpdated getRootAsImConfInfoUpdated(ByteBuffer _bb) {
        return getRootAsImConfInfoUpdated(_bb, new ImConfInfoUpdated());
    }

    public static ImConfInfoUpdated getRootAsImConfInfoUpdated(ByteBuffer _bb, ImConfInfoUpdated obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ImConfInfoUpdated __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long sessionId() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String state() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer stateAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public SubjectExt subjectData() {
        return subjectData(new SubjectExt());
    }

    public SubjectExt subjectData(SubjectExt obj) {
        int o = __offset(8);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public long maxUserCnt() {
        int o = __offset(10);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public ImConfUser users(int j) {
        return users(new ImConfUser(), j);
    }

    public ImConfUser users(ImConfUser obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int usersLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public Icon iconData() {
        return iconData(new Icon());
    }

    public Icon iconData(Icon obj) {
        int o = __offset(14);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public static int createImConfInfoUpdated(FlatBufferBuilder builder, long session_id, int stateOffset, int subject_dataOffset, long max_user_cnt, int usersOffset, int icon_dataOffset) {
        builder.startObject(6);
        addIconData(builder, icon_dataOffset);
        addUsers(builder, usersOffset);
        addMaxUserCnt(builder, max_user_cnt);
        addSubjectData(builder, subject_dataOffset);
        addState(builder, stateOffset);
        addSessionId(builder, session_id);
        return endImConfInfoUpdated(builder);
    }

    public static void startImConfInfoUpdated(FlatBufferBuilder builder) {
        builder.startObject(6);
    }

    public static void addSessionId(FlatBufferBuilder builder, long sessionId) {
        builder.addInt(0, (int) sessionId, 0);
    }

    public static void addState(FlatBufferBuilder builder, int stateOffset) {
        builder.addOffset(1, stateOffset, 0);
    }

    public static void addSubjectData(FlatBufferBuilder builder, int subjectDataOffset) {
        builder.addOffset(2, subjectDataOffset, 0);
    }

    public static void addMaxUserCnt(FlatBufferBuilder builder, long maxUserCnt) {
        builder.addInt(3, (int) maxUserCnt, 0);
    }

    public static void addUsers(FlatBufferBuilder builder, int usersOffset) {
        builder.addOffset(4, usersOffset, 0);
    }

    public static int createUsersVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startUsersVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addIconData(FlatBufferBuilder builder, int iconDataOffset) {
        builder.addOffset(5, iconDataOffset, 0);
    }

    public static int endImConfInfoUpdated(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
