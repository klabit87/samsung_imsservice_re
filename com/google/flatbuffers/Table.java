package com.google.flatbuffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Comparator;

public class Table {
    private static final ThreadLocal<CharBuffer> CHAR_BUFFER = new ThreadLocal<>();
    public static final ThreadLocal<Charset> UTF8_CHARSET = new ThreadLocal<Charset>() {
        /* access modifiers changed from: protected */
        public Charset initialValue() {
            return Charset.forName("UTF-8");
        }
    };
    private static final ThreadLocal<CharsetDecoder> UTF8_DECODER = new ThreadLocal<CharsetDecoder>() {
        /* access modifiers changed from: protected */
        public CharsetDecoder initialValue() {
            return Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        }
    };
    protected ByteBuffer bb;
    protected int bb_pos;

    public ByteBuffer getByteBuffer() {
        return this.bb;
    }

    /* access modifiers changed from: protected */
    public int __offset(int vtable_offset) {
        int i = this.bb_pos;
        int vtable = i - this.bb.getInt(i);
        if (vtable_offset < this.bb.getShort(vtable)) {
            return this.bb.getShort(vtable + vtable_offset);
        }
        return 0;
    }

    protected static int __offset(int vtable_offset, int offset, ByteBuffer bb2) {
        int vtable = bb2.capacity() - offset;
        return bb2.getShort((vtable + vtable_offset) - bb2.getInt(vtable)) + vtable;
    }

    /* access modifiers changed from: protected */
    public int __indirect(int offset) {
        return this.bb.getInt(offset) + offset;
    }

    protected static int __indirect(int offset, ByteBuffer bb2) {
        return bb2.getInt(offset) + offset;
    }

    /* access modifiers changed from: protected */
    public String __string(int offset) {
        CharsetDecoder decoder = UTF8_DECODER.get();
        decoder.reset();
        int offset2 = offset + this.bb.getInt(offset);
        ByteBuffer src = this.bb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int length = src.getInt(offset2);
        src.position(offset2 + 4);
        src.limit(offset2 + 4 + length);
        int required = (int) (((float) length) * decoder.maxCharsPerByte());
        CharBuffer dst = CHAR_BUFFER.get();
        if (dst == null || dst.capacity() < required) {
            dst = CharBuffer.allocate(required);
            CHAR_BUFFER.set(dst);
        }
        dst.clear();
        try {
            CoderResult cr = decoder.decode(src, dst, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            return dst.flip().toString();
        } catch (CharacterCodingException x) {
            throw new Error(x);
        }
    }

    /* access modifiers changed from: protected */
    public int __vector_len(int offset) {
        int offset2 = offset + this.bb_pos;
        return this.bb.getInt(offset2 + this.bb.getInt(offset2));
    }

    /* access modifiers changed from: protected */
    public int __vector(int offset) {
        int offset2 = offset + this.bb_pos;
        return this.bb.getInt(offset2) + offset2 + 4;
    }

    /* access modifiers changed from: protected */
    public ByteBuffer __vector_as_bytebuffer(int vector_offset, int elem_size) {
        int o = __offset(vector_offset);
        if (o == 0) {
            return null;
        }
        ByteBuffer bb2 = this.bb.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int vectorstart = __vector(o);
        bb2.position(vectorstart);
        bb2.limit((__vector_len(o) * elem_size) + vectorstart);
        return bb2;
    }

    /* access modifiers changed from: protected */
    public Table __union(Table t, int offset) {
        int offset2 = offset + this.bb_pos;
        t.bb_pos = this.bb.getInt(offset2) + offset2;
        t.bb = this.bb;
        return t;
    }

    protected static boolean __has_identifier(ByteBuffer bb2, String ident) {
        if (ident.length() == 4) {
            for (int i = 0; i < 4; i++) {
                if (ident.charAt(i) != ((char) bb2.get(bb2.position() + 4 + i))) {
                    return false;
                }
            }
            return true;
        }
        throw new AssertionError("FlatBuffers: file identifier must be length 4");
    }

    /* access modifiers changed from: protected */
    public void sortTables(int[] offsets, final ByteBuffer bb2) {
        Integer[] off = new Integer[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            off[i] = Integer.valueOf(offsets[i]);
        }
        Arrays.sort(off, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return Table.this.keysCompare(o1, o2, bb2);
            }
        });
        for (int i2 = 0; i2 < offsets.length; i2++) {
            offsets[i2] = off[i2].intValue();
        }
    }

    /* access modifiers changed from: protected */
    public int keysCompare(Integer o1, Integer o2, ByteBuffer bb2) {
        return 0;
    }

    protected static int compareStrings(int offset_1, int offset_2, ByteBuffer bb2) {
        int offset_12 = offset_1 + bb2.getInt(offset_1);
        int offset_22 = offset_2 + bb2.getInt(offset_2);
        int len_1 = bb2.getInt(offset_12);
        int len_2 = bb2.getInt(offset_22);
        int startPos_1 = offset_12 + 4;
        int startPos_2 = offset_22 + 4;
        int len = Math.min(len_1, len_2);
        for (int i = 0; i < len; i++) {
            if (bb2.get(i + startPos_1) != bb2.get(i + startPos_2)) {
                return bb2.get(i + startPos_1) - bb2.get(i + startPos_2);
            }
        }
        return len_1 - len_2;
    }

    protected static int compareStrings(int offset_1, byte[] key, ByteBuffer bb2) {
        int offset_12 = offset_1 + bb2.getInt(offset_1);
        int len_1 = bb2.getInt(offset_12);
        int len_2 = key.length;
        int startPos_1 = offset_12 + 4;
        int len = Math.min(len_1, len_2);
        for (int i = 0; i < len; i++) {
            if (bb2.get(i + startPos_1) != key[i]) {
                return bb2.get(i + startPos_1) - key[i];
            }
        }
        return len_1 - len_2;
    }
}
