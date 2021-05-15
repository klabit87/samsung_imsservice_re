package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class IncomingCall extends Table {
    public static IncomingCall getRootAsIncomingCall(ByteBuffer _bb) {
        return getRootAsIncomingCall(_bb, new IncomingCall());
    }

    public static IncomingCall getRootAsIncomingCall(ByteBuffer _bb, IncomingCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public IncomingCall __assign(int _i, ByteBuffer _bb) {
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

    public long session() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public int callType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String peeruri() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer peeruriAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public String displayName() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer displayNameAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public String referredBy() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer referredByAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public long replacingSession() {
        int o = __offset(16);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String sipCallId() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer sipCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String terminatingId() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer terminatingIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String numberPlus() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer numberPlusAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String rawSipmsg() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer rawSipmsgAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public String replaces() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer replacesAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String alertInfo() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String photoRing() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer photoRingAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String historyInfo() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer historyInfoAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public boolean cvoEnabled() {
        int o = __offset(34);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String verstat() {
        int o = __offset(36);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer verstatAsByteBuffer() {
        return __vector_as_bytebuffer(36, 1);
    }

    public String cmcDeviceId() {
        int o = __offset(38);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cmcDeviceIdAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public ComposerData composerData() {
        return composerData(new ComposerData());
    }

    public ComposerData composerData(ComposerData obj) {
        int o = __offset(40);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public boolean hasDiversion() {
        int o = __offset(42);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public static int createIncomingCall(FlatBufferBuilder builder, long handle, long session, int call_type, int peeruriOffset, int display_nameOffset, int referred_byOffset, long replacing_session, int sip_call_idOffset, int terminating_idOffset, int number_plusOffset, int raw_sipmsgOffset, int replacesOffset, int alert_infoOffset, int photo_ringOffset, int history_infoOffset, boolean cvo_enabled, int verstatOffset, int cmc_device_idOffset, int composer_dataOffset, boolean has_diversion) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(20);
        addComposerData(flatBufferBuilder, composer_dataOffset);
        addCmcDeviceId(flatBufferBuilder, cmc_device_idOffset);
        addVerstat(flatBufferBuilder, verstatOffset);
        addHistoryInfo(flatBufferBuilder, history_infoOffset);
        addPhotoRing(flatBufferBuilder, photo_ringOffset);
        addAlertInfo(flatBufferBuilder, alert_infoOffset);
        addReplaces(flatBufferBuilder, replacesOffset);
        addRawSipmsg(flatBufferBuilder, raw_sipmsgOffset);
        addNumberPlus(flatBufferBuilder, number_plusOffset);
        addTerminatingId(flatBufferBuilder, terminating_idOffset);
        addSipCallId(flatBufferBuilder, sip_call_idOffset);
        addReplacingSession(flatBufferBuilder, replacing_session);
        addReferredBy(flatBufferBuilder, referred_byOffset);
        addDisplayName(flatBufferBuilder, display_nameOffset);
        addPeeruri(flatBufferBuilder, peeruriOffset);
        addCallType(flatBufferBuilder, call_type);
        addSession(flatBufferBuilder, session);
        addHandle(builder, handle);
        addHasDiversion(flatBufferBuilder, has_diversion);
        addCvoEnabled(flatBufferBuilder, cvo_enabled);
        return endIncomingCall(builder);
    }

    public static void startIncomingCall(FlatBufferBuilder builder) {
        builder.startObject(20);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addSession(FlatBufferBuilder builder, long session) {
        builder.addInt(1, (int) session, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(2, callType, 0);
    }

    public static void addPeeruri(FlatBufferBuilder builder, int peeruriOffset) {
        builder.addOffset(3, peeruriOffset, 0);
    }

    public static void addDisplayName(FlatBufferBuilder builder, int displayNameOffset) {
        builder.addOffset(4, displayNameOffset, 0);
    }

    public static void addReferredBy(FlatBufferBuilder builder, int referredByOffset) {
        builder.addOffset(5, referredByOffset, 0);
    }

    public static void addReplacingSession(FlatBufferBuilder builder, long replacingSession) {
        builder.addInt(6, (int) replacingSession, 0);
    }

    public static void addSipCallId(FlatBufferBuilder builder, int sipCallIdOffset) {
        builder.addOffset(7, sipCallIdOffset, 0);
    }

    public static void addTerminatingId(FlatBufferBuilder builder, int terminatingIdOffset) {
        builder.addOffset(8, terminatingIdOffset, 0);
    }

    public static void addNumberPlus(FlatBufferBuilder builder, int numberPlusOffset) {
        builder.addOffset(9, numberPlusOffset, 0);
    }

    public static void addRawSipmsg(FlatBufferBuilder builder, int rawSipmsgOffset) {
        builder.addOffset(10, rawSipmsgOffset, 0);
    }

    public static void addReplaces(FlatBufferBuilder builder, int replacesOffset) {
        builder.addOffset(11, replacesOffset, 0);
    }

    public static void addAlertInfo(FlatBufferBuilder builder, int alertInfoOffset) {
        builder.addOffset(12, alertInfoOffset, 0);
    }

    public static void addPhotoRing(FlatBufferBuilder builder, int photoRingOffset) {
        builder.addOffset(13, photoRingOffset, 0);
    }

    public static void addHistoryInfo(FlatBufferBuilder builder, int historyInfoOffset) {
        builder.addOffset(14, historyInfoOffset, 0);
    }

    public static void addCvoEnabled(FlatBufferBuilder builder, boolean cvoEnabled) {
        builder.addBoolean(15, cvoEnabled, false);
    }

    public static void addVerstat(FlatBufferBuilder builder, int verstatOffset) {
        builder.addOffset(16, verstatOffset, 0);
    }

    public static void addCmcDeviceId(FlatBufferBuilder builder, int cmcDeviceIdOffset) {
        builder.addOffset(17, cmcDeviceIdOffset, 0);
    }

    public static void addComposerData(FlatBufferBuilder builder, int composerDataOffset) {
        builder.addOffset(18, composerDataOffset, 0);
    }

    public static void addHasDiversion(FlatBufferBuilder builder, boolean hasDiversion) {
        builder.addBoolean(19, hasDiversion, false);
    }

    public static int endIncomingCall(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
