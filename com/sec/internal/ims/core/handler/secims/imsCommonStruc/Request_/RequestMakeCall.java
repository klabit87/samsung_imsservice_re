package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AdditionalContents;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ComposerData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ExtraHeader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class RequestMakeCall extends Table {
    public static RequestMakeCall getRootAsRequestMakeCall(ByteBuffer _bb) {
        return getRootAsRequestMakeCall(_bb, new RequestMakeCall());
    }

    public static RequestMakeCall getRootAsRequestMakeCall(ByteBuffer _bb, RequestMakeCall obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public RequestMakeCall __assign(int _i, ByteBuffer _bb) {
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

    public String peeruri() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer peeruriAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public int callType() {
        int o = __offset(8);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int codec() {
        int o = __offset(10);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int mode() {
        int o = __offset(12);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public int direction() {
        int o = __offset(14);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public String letteringText() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer letteringTextAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String typeOfEmergencyService() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer typeOfEmergencyServiceAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String ecscfList(int j) {
        int o = __offset(20);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int ecscfListLength() {
        int o = __offset(20);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public int ecscfPort() {
        int o = __offset(22);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public AdditionalContents additionalContents() {
        return additionalContents(new AdditionalContents());
    }

    public AdditionalContents additionalContents(AdditionalContents obj) {
        int o = __offset(24);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String origUri() {
        int o = __offset(26);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer origUriAsByteBuffer() {
        return __vector_as_bytebuffer(26, 1);
    }

    public String dispName() {
        int o = __offset(28);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dispNameAsByteBuffer() {
        return __vector_as_bytebuffer(28, 1);
    }

    public String dialedNumber() {
        int o = __offset(30);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer dialedNumberAsByteBuffer() {
        return __vector_as_bytebuffer(30, 1);
    }

    public String cli() {
        int o = __offset(32);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer cliAsByteBuffer() {
        return __vector_as_bytebuffer(32, 1);
    }

    public String pEmergencyInfoOfAtt() {
        int o = __offset(34);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer pEmergencyInfoOfAttAsByteBuffer() {
        return __vector_as_bytebuffer(34, 1);
    }

    public ExtraHeader additionalSipHeaders() {
        return additionalSipHeaders(new ExtraHeader());
    }

    public ExtraHeader additionalSipHeaders(ExtraHeader obj) {
        int o = __offset(36);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String alertInfo() {
        int o = __offset(38);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer alertInfoAsByteBuffer() {
        return __vector_as_bytebuffer(38, 1);
    }

    public String photoRing() {
        int o = __offset(40);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer photoRingAsByteBuffer() {
        return __vector_as_bytebuffer(40, 1);
    }

    public boolean isLteEpsOnlyAttached() {
        int o = __offset(42);
        return (o == 0 || this.bb.get(this.bb_pos + o) == 0) ? false : true;
    }

    public String p2pList(int j) {
        int o = __offset(44);
        if (o != 0) {
            return __string(__vector(o) + (j * 4));
        }
        return null;
    }

    public int p2pListLength() {
        int o = __offset(44);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public int cmcBoundSessionId() {
        int o = __offset(46);
        if (o != 0) {
            return this.bb.getInt(this.bb_pos + o);
        }
        return 0;
    }

    public ComposerData composerData() {
        return composerData(new ComposerData());
    }

    public ComposerData composerData(ComposerData obj) {
        int o = __offset(48);
        if (o != 0) {
            return obj.__assign(__indirect(this.bb_pos + o), this.bb);
        }
        return null;
    }

    public String replaceCallId() {
        int o = __offset(50);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer replaceCallIdAsByteBuffer() {
        return __vector_as_bytebuffer(50, 1);
    }

    public static int createRequestMakeCall(FlatBufferBuilder builder, long handle, int peeruriOffset, int call_type, int codec, int mode, int direction, int lettering_textOffset, int type_of_emergency_serviceOffset, int ecscf_listOffset, int ecscf_port, int additional_contentsOffset, int orig_uriOffset, int disp_nameOffset, int dialed_numberOffset, int cliOffset, int p_emergency_info_of_attOffset, int additional_sip_headersOffset, int alert_infoOffset, int photo_ringOffset, boolean is_lte_eps_only_attached, int p2p_listOffset, int cmc_bound_session_id, int composer_dataOffset, int replace_call_idOffset) {
        FlatBufferBuilder flatBufferBuilder = builder;
        flatBufferBuilder.startObject(24);
        addReplaceCallId(flatBufferBuilder, replace_call_idOffset);
        addComposerData(flatBufferBuilder, composer_dataOffset);
        addCmcBoundSessionId(flatBufferBuilder, cmc_bound_session_id);
        addP2pList(flatBufferBuilder, p2p_listOffset);
        addPhotoRing(flatBufferBuilder, photo_ringOffset);
        addAlertInfo(flatBufferBuilder, alert_infoOffset);
        addAdditionalSipHeaders(flatBufferBuilder, additional_sip_headersOffset);
        addPEmergencyInfoOfAtt(flatBufferBuilder, p_emergency_info_of_attOffset);
        addCli(flatBufferBuilder, cliOffset);
        addDialedNumber(flatBufferBuilder, dialed_numberOffset);
        addDispName(flatBufferBuilder, disp_nameOffset);
        addOrigUri(flatBufferBuilder, orig_uriOffset);
        addAdditionalContents(flatBufferBuilder, additional_contentsOffset);
        addEcscfPort(flatBufferBuilder, ecscf_port);
        addEcscfList(flatBufferBuilder, ecscf_listOffset);
        addTypeOfEmergencyService(flatBufferBuilder, type_of_emergency_serviceOffset);
        addLetteringText(flatBufferBuilder, lettering_textOffset);
        addDirection(flatBufferBuilder, direction);
        addMode(flatBufferBuilder, mode);
        addCodec(flatBufferBuilder, codec);
        addCallType(flatBufferBuilder, call_type);
        addPeeruri(flatBufferBuilder, peeruriOffset);
        addHandle(builder, handle);
        addIsLteEpsOnlyAttached(flatBufferBuilder, is_lte_eps_only_attached);
        return endRequestMakeCall(builder);
    }

    public static void startRequestMakeCall(FlatBufferBuilder builder) {
        builder.startObject(24);
    }

    public static void addHandle(FlatBufferBuilder builder, long handle) {
        builder.addInt(0, (int) handle, 0);
    }

    public static void addPeeruri(FlatBufferBuilder builder, int peeruriOffset) {
        builder.addOffset(1, peeruriOffset, 0);
    }

    public static void addCallType(FlatBufferBuilder builder, int callType) {
        builder.addInt(2, callType, 0);
    }

    public static void addCodec(FlatBufferBuilder builder, int codec) {
        builder.addInt(3, codec, 0);
    }

    public static void addMode(FlatBufferBuilder builder, int mode) {
        builder.addInt(4, mode, 0);
    }

    public static void addDirection(FlatBufferBuilder builder, int direction) {
        builder.addInt(5, direction, 0);
    }

    public static void addLetteringText(FlatBufferBuilder builder, int letteringTextOffset) {
        builder.addOffset(6, letteringTextOffset, 0);
    }

    public static void addTypeOfEmergencyService(FlatBufferBuilder builder, int typeOfEmergencyServiceOffset) {
        builder.addOffset(7, typeOfEmergencyServiceOffset, 0);
    }

    public static void addEcscfList(FlatBufferBuilder builder, int ecscfListOffset) {
        builder.addOffset(8, ecscfListOffset, 0);
    }

    public static int createEcscfListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startEcscfListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addEcscfPort(FlatBufferBuilder builder, int ecscfPort) {
        builder.addInt(9, ecscfPort, 0);
    }

    public static void addAdditionalContents(FlatBufferBuilder builder, int additionalContentsOffset) {
        builder.addOffset(10, additionalContentsOffset, 0);
    }

    public static void addOrigUri(FlatBufferBuilder builder, int origUriOffset) {
        builder.addOffset(11, origUriOffset, 0);
    }

    public static void addDispName(FlatBufferBuilder builder, int dispNameOffset) {
        builder.addOffset(12, dispNameOffset, 0);
    }

    public static void addDialedNumber(FlatBufferBuilder builder, int dialedNumberOffset) {
        builder.addOffset(13, dialedNumberOffset, 0);
    }

    public static void addCli(FlatBufferBuilder builder, int cliOffset) {
        builder.addOffset(14, cliOffset, 0);
    }

    public static void addPEmergencyInfoOfAtt(FlatBufferBuilder builder, int pEmergencyInfoOfAttOffset) {
        builder.addOffset(15, pEmergencyInfoOfAttOffset, 0);
    }

    public static void addAdditionalSipHeaders(FlatBufferBuilder builder, int additionalSipHeadersOffset) {
        builder.addOffset(16, additionalSipHeadersOffset, 0);
    }

    public static void addAlertInfo(FlatBufferBuilder builder, int alertInfoOffset) {
        builder.addOffset(17, alertInfoOffset, 0);
    }

    public static void addPhotoRing(FlatBufferBuilder builder, int photoRingOffset) {
        builder.addOffset(18, photoRingOffset, 0);
    }

    public static void addIsLteEpsOnlyAttached(FlatBufferBuilder builder, boolean isLteEpsOnlyAttached) {
        builder.addBoolean(19, isLteEpsOnlyAttached, false);
    }

    public static void addP2pList(FlatBufferBuilder builder, int p2pListOffset) {
        builder.addOffset(20, p2pListOffset, 0);
    }

    public static int createP2pListVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startP2pListVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addCmcBoundSessionId(FlatBufferBuilder builder, int cmcBoundSessionId) {
        builder.addInt(21, cmcBoundSessionId, 0);
    }

    public static void addComposerData(FlatBufferBuilder builder, int composerDataOffset) {
        builder.addOffset(22, composerDataOffset, 0);
    }

    public static void addReplaceCallId(FlatBufferBuilder builder, int replaceCallIdOffset) {
        builder.addOffset(23, replaceCallIdOffset, 0);
    }

    public static int endRequestMakeCall(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 6);
        return o;
    }
}
