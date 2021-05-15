package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ContactInfo extends Table {
    public static ContactInfo getRootAsContactInfo(ByteBuffer _bb) {
        return getRootAsContactInfo(_bb, new ContactInfo());
    }

    public static ContactInfo getRootAsContactInfo(ByteBuffer _bb, ContactInfo obj) {
        _bb.order(ByteOrder.LITTLE_ENDIAN);
        return obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb);
    }

    public void __init(int _i, ByteBuffer _bb) {
        this.bb_pos = _i;
        this.bb = _bb;
    }

    public ContactInfo __assign(int _i, ByteBuffer _bb) {
        __init(_i, _bb);
        return this;
    }

    public String uri() {
        int o = __offset(4);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer uriAsByteBuffer() {
        return __vector_as_bytebuffer(4, 1);
    }

    public String number() {
        int o = __offset(6);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer numberAsByteBuffer() {
        return __vector_as_bytebuffer(6, 1);
    }

    public String subscriptionState() {
        int o = __offset(8);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subscriptionStateAsByteBuffer() {
        return __vector_as_bytebuffer(8, 1);
    }

    public String subscriptionFailureReason() {
        int o = __offset(10);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer subscriptionFailureReasonAsByteBuffer() {
        return __vector_as_bytebuffer(10, 1);
    }

    public PresenceServiceStatus serviceStatus(int j) {
        return serviceStatus(new PresenceServiceStatus(), j);
    }

    public PresenceServiceStatus serviceStatus(PresenceServiceStatus obj, int j) {
        int o = __offset(12);
        if (o != 0) {
            return obj.__assign(__indirect(__vector(o) + (j * 4)), this.bb);
        }
        return null;
    }

    public int serviceStatusLength() {
        int o = __offset(12);
        if (o != 0) {
            return __vector_len(o);
        }
        return 0;
    }

    public String note() {
        int o = __offset(14);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer noteAsByteBuffer() {
        return __vector_as_bytebuffer(14, 1);
    }

    public String iconUri() {
        int o = __offset(16);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer iconUriAsByteBuffer() {
        return __vector_as_bytebuffer(16, 1);
    }

    public String email() {
        int o = __offset(18);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer emailAsByteBuffer() {
        return __vector_as_bytebuffer(18, 1);
    }

    public String homepage() {
        int o = __offset(20);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer homepageAsByteBuffer() {
        return __vector_as_bytebuffer(20, 1);
    }

    public String className() {
        int o = __offset(22);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer classNameAsByteBuffer() {
        return __vector_as_bytebuffer(22, 1);
    }

    public String rawPidf() {
        int o = __offset(24);
        if (o != 0) {
            return __string(this.bb_pos + o);
        }
        return null;
    }

    public ByteBuffer rawPidfAsByteBuffer() {
        return __vector_as_bytebuffer(24, 1);
    }

    public static int createContactInfo(FlatBufferBuilder builder, int uriOffset, int numberOffset, int subscription_stateOffset, int subscription_failure_reasonOffset, int service_statusOffset, int noteOffset, int icon_uriOffset, int emailOffset, int homepageOffset, int class_nameOffset, int raw_pidfOffset) {
        builder.startObject(11);
        addRawPidf(builder, raw_pidfOffset);
        addClassName(builder, class_nameOffset);
        addHomepage(builder, homepageOffset);
        addEmail(builder, emailOffset);
        addIconUri(builder, icon_uriOffset);
        addNote(builder, noteOffset);
        addServiceStatus(builder, service_statusOffset);
        addSubscriptionFailureReason(builder, subscription_failure_reasonOffset);
        addSubscriptionState(builder, subscription_stateOffset);
        addNumber(builder, numberOffset);
        addUri(builder, uriOffset);
        return endContactInfo(builder);
    }

    public static void startContactInfo(FlatBufferBuilder builder) {
        builder.startObject(11);
    }

    public static void addUri(FlatBufferBuilder builder, int uriOffset) {
        builder.addOffset(0, uriOffset, 0);
    }

    public static void addNumber(FlatBufferBuilder builder, int numberOffset) {
        builder.addOffset(1, numberOffset, 0);
    }

    public static void addSubscriptionState(FlatBufferBuilder builder, int subscriptionStateOffset) {
        builder.addOffset(2, subscriptionStateOffset, 0);
    }

    public static void addSubscriptionFailureReason(FlatBufferBuilder builder, int subscriptionFailureReasonOffset) {
        builder.addOffset(3, subscriptionFailureReasonOffset, 0);
    }

    public static void addServiceStatus(FlatBufferBuilder builder, int serviceStatusOffset) {
        builder.addOffset(4, serviceStatusOffset, 0);
    }

    public static int createServiceStatusVector(FlatBufferBuilder builder, int[] data) {
        builder.startVector(4, data.length, 4);
        for (int i = data.length - 1; i >= 0; i--) {
            builder.addOffset(data[i]);
        }
        return builder.endVector();
    }

    public static void startServiceStatusVector(FlatBufferBuilder builder, int numElems) {
        builder.startVector(4, numElems, 4);
    }

    public static void addNote(FlatBufferBuilder builder, int noteOffset) {
        builder.addOffset(5, noteOffset, 0);
    }

    public static void addIconUri(FlatBufferBuilder builder, int iconUriOffset) {
        builder.addOffset(6, iconUriOffset, 0);
    }

    public static void addEmail(FlatBufferBuilder builder, int emailOffset) {
        builder.addOffset(7, emailOffset, 0);
    }

    public static void addHomepage(FlatBufferBuilder builder, int homepageOffset) {
        builder.addOffset(8, homepageOffset, 0);
    }

    public static void addClassName(FlatBufferBuilder builder, int classNameOffset) {
        builder.addOffset(9, classNameOffset, 0);
    }

    public static void addRawPidf(FlatBufferBuilder builder, int rawPidfOffset) {
        builder.addOffset(10, rawPidfOffset, 0);
    }

    public static int endContactInfo(FlatBufferBuilder builder) {
        int o = builder.endObject();
        builder.required(o, 4);
        builder.required(o, 8);
        return o;
    }
}
