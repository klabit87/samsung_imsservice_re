package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class SipMessage extends Table {
    public static SipMessage getRootAsSipMessage(ByteBuffer _bb) {
        return getRootAsSipMessage(_bb, new SipMessage());
    }

    public static SipMessage getRootAsSipMessage(ByteBuffer _bb, SipMessage obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public SipMessage __assign(int _i, ByteBuffer _bb) {
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

    public int direction() {
        int o = __offset(6);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int origin() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String sipMessage() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipMessageAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String hexContents() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer hexContentsAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public long phoneId() {
        int o = __offset(14);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int mno() {
        int o = __offset(16);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public boolean isRcsProfile() {
        int o = __offset(18);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createSipMessage(FlatBufferBuilder builder, long handle, int direction, int origin, int sip_messageOffset, int hex_contentsOffset, long phone_id, int mno, boolean is_rcs_profile) {
        builder.startObject(8);
        addMno(builder, mno);
        addPhoneId(builder, phone_id);
        addHexContents(builder, hex_contentsOffset);
        addSipMessage(builder, sip_messageOffset);
        addOrigin(builder, origin);
        addDirection(builder, direction);
        addHandle(builder, handle);
        addIsRcsProfile(builder, is_rcs_profile);
        return endSipMessage(builder);
    }

    public static void startSipMessage(FlatBufferBuilder builder) {
        builder.startObject(8);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addDirection(FlatBufferBuilder builder, int direction) {
        builder.addInt(1, direction, 0);
    }

    public static void addOrigin(FlatBufferBuilder builder, int origin) {
        builder.addInt(2, origin, 0);
    }

    public static void addSipMessage(FlatBufferBuilder builder, int sipMessageOffset) {
        builder.addOffset(3, sipMessageOffset, 0);
    }

    public static void addHexContents(FlatBufferBuilder builder, int hexContentsOffset) {
        builder.addOffset(4, hexContentsOffset, 0);
    }

    public static void addPhoneId(FlatBufferBuilder builder, long phoneId) {
        builder.addInt(5, (int) phoneId, 0);
    }

    public static void addMno(FlatBufferBuilder builder, int mno) {
        builder.addInt(6, mno, 0);
    }

    public static void addIsRcsProfile(FlatBufferBuilder builder, boolean isRcsProfile) {
        builder.addBoolean(7, isRcsProfile, false);
    }

    public static int endSipMessage(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 10);
        return o;
    }
}
