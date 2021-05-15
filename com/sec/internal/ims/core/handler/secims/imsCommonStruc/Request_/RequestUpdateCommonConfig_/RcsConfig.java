package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateCommonConfig_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RcsConfig extends Table {
    public static RcsConfig getRootAsRcsConfig(ByteBuffer _bb) {
        return getRootAsRcsConfig(_bb, new RcsConfig());
    }

    public static RcsConfig getRootAsRcsConfig(ByteBuffer _bb, RcsConfig obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RcsConfig __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public long rcsFtChunkSize() {
        int o = __offset(4);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public long rcsIshChunkSize() {
        int o = __offset(6);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String confUri() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer confUriAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public boolean isMsrpCema() {
        int o = __offset(10);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String downloadsPath() {
        int o = __offset(12);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer downloadsPathAsByteBuffer() {
        return __vector_as_bytebuffer(12, 1);
    }

    public boolean isConfSubscribeEnabled() {
        int o = __offset(14);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String exploderUri() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer exploderUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public long pagerModeSizeLimit() {
        int o = __offset(18);
        if (o != 0) {
            return ((long) this.bb.getInt(this.bb_pos + o)) & 4294967295L;
        }
        return 0;
    }

    public String endUserConfReqId() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer endUserConfReqIdAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String suspendUser() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer suspendUserAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public boolean useMsrpDiscardPort() {
        int o = __offset(24);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isAggrImdnSupported() {
        int o = __offset(26);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public boolean isCbPrivacyDisable() {
        int o = __offset(28);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public int cbMsgTech() {
        int o = __offset(30);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public static int createRcsConfig(FlatBufferBuilder builder, long rcs_ft_chunk_size, long rcs_ish_chunk_size, int conf_uriOffset, boolean is_msrp_cema, int downloads_pathOffset, boolean is_conf_subscribe_enabled, int exploder_uriOffset, long pager_mode_size_limit, int end_user_conf_req_idOffset, int suspend_userOffset, boolean use_msrp_discard_port, boolean is_aggr_imdn_supported, boolean is_cb_privacy_disable, int cb_msg_tech) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(14);
        addCbMsgTech(flatBufferBuilder, cb_msg_tech);
        addSuspendUser(flatBufferBuilder, suspend_userOffset);
        addEndUserConfReqId(flatBufferBuilder, end_user_conf_req_idOffset);
        addPagerModeSizeLimit(flatBufferBuilder, pager_mode_size_limit);
        addExploderUri(flatBufferBuilder, exploder_uriOffset);
        addDownloadsPath(flatBufferBuilder, downloads_pathOffset);
        addConfUri(flatBufferBuilder, conf_uriOffset);
        addRcsIshChunkSize(flatBufferBuilder, rcs_ish_chunk_size);
        addRcsFtChunkSize(builder, rcs_ft_chunk_size);
        addIsCbPrivacyDisable(flatBufferBuilder, is_cb_privacy_disable);
        addIsAggrImdnSupported(flatBufferBuilder, is_aggr_imdn_supported);
        addUseMsrpDiscardPort(flatBufferBuilder, use_msrp_discard_port);
        addIsConfSubscribeEnabled(flatBufferBuilder, is_conf_subscribe_enabled);
        addIsMsrpCema(flatBufferBuilder, is_msrp_cema);
        return endRcsConfig(builder);
    }

    public static void startRcsConfig(FlatBufferBuilder builder) {
        builder.startObject(14);
    }

    public static void addRcsFtChunkSize(FlatBufferBuilder builder, long rcsFtChunkSize) {
        builder.addInt(0, (int) rcsFtChunkSize, 0);
    }

    public static void addRcsIshChunkSize(FlatBufferBuilder builder, long rcsIshChunkSize) {
        builder.addInt(1, (int) rcsIshChunkSize, 0);
    }

    public static void addConfUri(FlatBufferBuilder builder, int confUriOffset) {
        builder.addOffset(2, confUriOffset, 0);
    }

    public static void addIsMsrpCema(FlatBufferBuilder builder, boolean isMsrpCema) {
        builder.addBoolean(3, isMsrpCema, false);
    }

    public static void addDownloadsPath(FlatBufferBuilder builder, int downloadsPathOffset) {
        builder.addOffset(4, downloadsPathOffset, 0);
    }

    public static void addIsConfSubscribeEnabled(FlatBufferBuilder builder, boolean isConfSubscribeEnabled) {
        builder.addBoolean(5, isConfSubscribeEnabled, false);
    }

    public static void addExploderUri(FlatBufferBuilder builder, int exploderUriOffset) {
        builder.addOffset(6, exploderUriOffset, 0);
    }

    public static void addPagerModeSizeLimit(FlatBufferBuilder builder, long pagerModeSizeLimit) {
        builder.addInt(7, (int) pagerModeSizeLimit, 0);
    }

    public static void addEndUserConfReqId(FlatBufferBuilder builder, int endUserConfReqIdOffset) {
        builder.addOffset(8, endUserConfReqIdOffset, 0);
    }

    public static void addSuspendUser(FlatBufferBuilder builder, int suspendUserOffset) {
        builder.addOffset(9, suspendUserOffset, 0);
    }

    public static void addUseMsrpDiscardPort(FlatBufferBuilder builder, boolean useMsrpDiscardPort) {
        builder.addBoolean(10, useMsrpDiscardPort, false);
    }

    public static void addIsAggrImdnSupported(FlatBufferBuilder builder, boolean isAggrImdnSupported) {
        builder.addBoolean(11, isAggrImdnSupported, false);
    }

    public static void addIsCbPrivacyDisable(FlatBufferBuilder builder, boolean isCbPrivacyDisable) {
        builder.addBoolean(12, isCbPrivacyDisable, false);
    }

    public static void addCbMsgTech(FlatBufferBuilder builder, int cbMsgTech) {
        builder.addInt(13, cbMsgTech, 0);
    }

    public static int endRcsConfig(FlatBufferBuilder builder) {
        return builder.endObject();
    }
}
