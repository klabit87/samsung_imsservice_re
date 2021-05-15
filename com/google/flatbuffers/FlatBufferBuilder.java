package com.google.flatbuffers;

import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

public class FlatBufferBuilder {
    static final Charset utf8charset = Charset.forName("UTF-8");
    ByteBuffer bb;
    ByteBufferFactory bb_factory;
    ByteBuffer dst;
    CharsetEncoder encoder;
    boolean finished;
    boolean force_defaults;
    int minalign;
    boolean nested;
    int num_vtables;
    int object_start;
    int space;
    int vector_num_elems;
    int[] vtable;
    int vtable_in_use;
    int[] vtables;

    public interface ByteBufferFactory {
        ByteBuffer newByteBuffer(int i);
    }

    public FlatBufferBuilder(int initial_size, ByteBufferFactory bb_factory2) {
        this.minalign = 1;
        this.vtable = null;
        this.vtable_in_use = 0;
        this.nested = false;
        this.finished = false;
        this.vtables = new int[16];
        this.num_vtables = 0;
        this.vector_num_elems = 0;
        this.force_defaults = false;
        this.encoder = utf8charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        initial_size = initial_size <= 0 ? 1 : initial_size;
        this.space = initial_size;
        this.bb_factory = bb_factory2;
        this.bb = bb_factory2.newByteBuffer(initial_size);
    }

    public FlatBufferBuilder(int initial_size) {
        this(initial_size, (ByteBufferFactory) new HeapByteBufferFactory());
    }

    public FlatBufferBuilder() {
        this(1024);
    }

    public FlatBufferBuilder(ByteBuffer existing_bb, ByteBufferFactory bb_factory2) {
        this.minalign = 1;
        this.vtable = null;
        this.vtable_in_use = 0;
        this.nested = false;
        this.finished = false;
        this.vtables = new int[16];
        this.num_vtables = 0;
        this.vector_num_elems = 0;
        this.force_defaults = false;
        this.encoder = utf8charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        init(existing_bb, bb_factory2);
    }

    public FlatBufferBuilder(ByteBuffer existing_bb) {
        this.minalign = 1;
        this.vtable = null;
        this.vtable_in_use = 0;
        this.nested = false;
        this.finished = false;
        this.vtables = new int[16];
        this.num_vtables = 0;
        this.vector_num_elems = 0;
        this.force_defaults = false;
        this.encoder = utf8charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
        init(existing_bb, new HeapByteBufferFactory());
    }

    public FlatBufferBuilder init(ByteBuffer existing_bb, ByteBufferFactory bb_factory2) {
        this.bb_factory = bb_factory2;
        this.bb = existing_bb;
        existing_bb.clear();
        this.bb.order(ByteOrder.LITTLE_ENDIAN);
        this.minalign = 1;
        this.space = this.bb.capacity();
        this.vtable_in_use = 0;
        this.nested = false;
        this.finished = false;
        this.object_start = 0;
        this.num_vtables = 0;
        this.vector_num_elems = 0;
        return this;
    }

    public static final class HeapByteBufferFactory implements ByteBufferFactory {
        public ByteBuffer newByteBuffer(int capacity) {
            return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    public void clear() {
        this.space = this.bb.capacity();
        this.bb.clear();
        this.minalign = 1;
        while (true) {
            int i = this.vtable_in_use;
            if (i > 0) {
                int[] iArr = this.vtable;
                int i2 = i - 1;
                this.vtable_in_use = i2;
                iArr[i2] = 0;
            } else {
                this.vtable_in_use = 0;
                this.nested = false;
                this.finished = false;
                this.object_start = 0;
                this.num_vtables = 0;
                this.vector_num_elems = 0;
                return;
            }
        }
    }

    static ByteBuffer growByteBuffer(ByteBuffer bb2, ByteBufferFactory bb_factory2) {
        int old_buf_size = bb2.capacity();
        if ((-1073741824 & old_buf_size) == 0) {
            int new_buf_size = old_buf_size << 1;
            bb2.position(0);
            ByteBuffer nbb = bb_factory2.newByteBuffer(new_buf_size);
            nbb.position(new_buf_size - old_buf_size);
            nbb.put(bb2);
            return nbb;
        }
        throw new AssertionError("FlatBuffers: cannot grow buffer beyond 2 gigabytes.");
    }

    public int offset() {
        return this.bb.capacity() - this.space;
    }

    public void pad(int byte_size) {
        for (int i = 0; i < byte_size; i++) {
            ByteBuffer byteBuffer = this.bb;
            int i2 = this.space - 1;
            this.space = i2;
            byteBuffer.put(i2, (byte) 0);
        }
    }

    public void prep(int size, int additional_bytes) {
        if (size > this.minalign) {
            this.minalign = size;
        }
        int align_size = ((~((this.bb.capacity() - this.space) + additional_bytes)) + 1) & (size - 1);
        while (this.space < align_size + size + additional_bytes) {
            int old_buf_size = this.bb.capacity();
            ByteBuffer growByteBuffer = growByteBuffer(this.bb, this.bb_factory);
            this.bb = growByteBuffer;
            this.space += growByteBuffer.capacity() - old_buf_size;
        }
        pad(align_size);
    }

    public void putBoolean(boolean x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 1;
        this.space = i;
        byteBuffer.put(i, x ? (byte) 1 : 0);
    }

    public void putByte(byte x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 1;
        this.space = i;
        byteBuffer.put(i, x);
    }

    public void putShort(short x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 2;
        this.space = i;
        byteBuffer.putShort(i, x);
    }

    public void putInt(int x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 4;
        this.space = i;
        byteBuffer.putInt(i, x);
    }

    public void putLong(long x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 8;
        this.space = i;
        byteBuffer.putLong(i, x);
    }

    public void putFloat(float x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 4;
        this.space = i;
        byteBuffer.putFloat(i, x);
    }

    public void putDouble(double x) {
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - 8;
        this.space = i;
        byteBuffer.putDouble(i, x);
    }

    public void addBoolean(boolean x) {
        prep(1, 0);
        putBoolean(x);
    }

    public void addByte(byte x) {
        prep(1, 0);
        putByte(x);
    }

    public void addShort(short x) {
        prep(2, 0);
        putShort(x);
    }

    public void addInt(int x) {
        prep(4, 0);
        putInt(x);
    }

    public void addLong(long x) {
        prep(8, 0);
        putLong(x);
    }

    public void addFloat(float x) {
        prep(4, 0);
        putFloat(x);
    }

    public void addDouble(double x) {
        prep(8, 0);
        putDouble(x);
    }

    public void addOffset(int off) {
        prep(4, 0);
        if (off <= offset()) {
            putInt((offset() - off) + 4);
            return;
        }
        throw new AssertionError("Given offset: " + off + " is higher than value relative to the end of the buffer: " + offset());
    }

    public void startVector(int elem_size, int num_elems, int alignment) {
        notNested();
        this.vector_num_elems = num_elems;
        prep(4, elem_size * num_elems);
        prep(alignment, elem_size * num_elems);
        this.nested = true;
    }

    public int endVector() {
        if (this.nested) {
            this.nested = false;
            putInt(this.vector_num_elems);
            return offset();
        }
        throw new AssertionError("FlatBuffers: endVector called without startVector");
    }

    public ByteBuffer createUnintializedVector(int elem_size, int num_elems, int alignment) {
        int length = elem_size * num_elems;
        startVector(elem_size, num_elems, alignment);
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - length;
        this.space = i;
        byteBuffer.position(i);
        ByteBuffer copy = this.bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        copy.limit(length);
        return copy;
    }

    public int createVectorOfTables(int[] offsets) {
        notNested();
        startVector(4, offsets.length, 4);
        for (int i = offsets.length - 1; i >= 0; i--) {
            addOffset(offsets[i]);
        }
        return endVector();
    }

    public <T extends Table> int createSortedVectorOfTables(T obj, int[] offsets) {
        obj.sortTables(offsets, this.bb);
        return createVectorOfTables(offsets);
    }

    public int createString(CharSequence s) {
        CharBuffer src;
        int estimatedDstCapacity = (int) (((float) s.length()) * this.encoder.maxBytesPerChar());
        ByteBuffer byteBuffer = this.dst;
        if (byteBuffer == null || byteBuffer.capacity() < estimatedDstCapacity) {
            this.dst = ByteBuffer.allocate(Math.max(128, estimatedDstCapacity));
        }
        this.dst.clear();
        if (s instanceof CharBuffer) {
            src = (CharBuffer) s;
        } else {
            src = CharBuffer.wrap(s);
        }
        CoderResult result = this.encoder.encode(src, this.dst, true);
        if (result.isError()) {
            try {
                result.throwException();
            } catch (CharacterCodingException x) {
                throw new Error(x);
            }
        }
        this.dst.flip();
        return createString(this.dst);
    }

    public int createString(ByteBuffer s) {
        int length = s.remaining();
        addByte((byte) 0);
        startVector(1, length, 1);
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - length;
        this.space = i;
        byteBuffer.position(i);
        this.bb.put(s);
        return endVector();
    }

    public int createByteVector(byte[] arr) {
        int length = arr.length;
        startVector(1, length, 1);
        ByteBuffer byteBuffer = this.bb;
        int i = this.space - length;
        this.space = i;
        byteBuffer.position(i);
        this.bb.put(arr);
        return endVector();
    }

    public void finished() {
        if (!this.finished) {
            throw new AssertionError("FlatBuffers: you can only access the serialized buffer after it has been finished by FlatBufferBuilder.finish().");
        }
    }

    public void notNested() {
        if (this.nested) {
            throw new AssertionError("FlatBuffers: object serialization must not be nested.");
        }
    }

    public void nested(int obj) {
        if (obj != offset()) {
            throw new AssertionError("FlatBuffers: struct must be serialized inline.");
        }
    }

    public void startObject(int numfields) {
        notNested();
        int[] iArr = this.vtable;
        if (iArr == null || iArr.length < numfields) {
            this.vtable = new int[numfields];
        }
        this.vtable_in_use = numfields;
        Arrays.fill(this.vtable, 0, numfields, 0);
        this.nested = true;
        this.object_start = offset();
    }

    public void addBoolean(int o, boolean x, boolean d) {
        if (this.force_defaults || x != d) {
            addBoolean(x);
            slot(o);
        }
    }

    public void addByte(int o, byte x, int d) {
        if (this.force_defaults || x != d) {
            addByte(x);
            slot(o);
        }
    }

    public void addShort(int o, short x, int d) {
        if (this.force_defaults || x != d) {
            addShort(x);
            slot(o);
        }
    }

    public void addInt(int o, int x, int d) {
        if (this.force_defaults || x != d) {
            addInt(x);
            slot(o);
        }
    }

    public void addLong(int o, long x, long d) {
        if (this.force_defaults || x != d) {
            addLong(x);
            slot(o);
        }
    }

    public void addFloat(int o, float x, double d) {
        if (this.force_defaults || ((double) x) != d) {
            addFloat(x);
            slot(o);
        }
    }

    public void addDouble(int o, double x, double d) {
        if (this.force_defaults || x != d) {
            addDouble(x);
            slot(o);
        }
    }

    public void addOffset(int o, int x, int d) {
        if (this.force_defaults || x != d) {
            addOffset(x);
            slot(o);
        }
    }

    public void addStruct(int voffset, int x, int d) {
        if (x != d) {
            nested(x);
            slot(voffset);
        }
    }

    public void slot(int voffset) {
        this.vtable[voffset] = offset();
    }

    public int endObject() {
        if (this.vtable == null || !this.nested) {
            throw new AssertionError("FlatBuffers: endObject called without startObject");
        }
        addInt(0);
        int vtableloc = offset();
        int i = this.vtable_in_use - 1;
        while (i >= 0 && this.vtable[i] == 0) {
            i--;
        }
        int trimmed_size = i + 1;
        while (i >= 0) {
            int[] iArr = this.vtable;
            addShort((short) (iArr[i] != 0 ? vtableloc - iArr[i] : 0));
            i--;
        }
        addShort((short) (vtableloc - this.object_start));
        addShort((short) ((trimmed_size + 2) * 2));
        int existing_vtable = 0;
        int i2 = 0;
        loop2:
        while (true) {
            if (i2 >= this.num_vtables) {
                break;
            }
            int vt1 = this.bb.capacity() - this.vtables[i2];
            int vt2 = this.space;
            short len = this.bb.getShort(vt1);
            if (len == this.bb.getShort(vt2)) {
                int j = 2;
                while (j < len) {
                    if (this.bb.getShort(vt1 + j) == this.bb.getShort(vt2 + j)) {
                        j += 2;
                    }
                }
                existing_vtable = this.vtables[i2];
                break loop2;
            }
            i2++;
        }
        if (existing_vtable != 0) {
            int capacity = this.bb.capacity() - vtableloc;
            this.space = capacity;
            this.bb.putInt(capacity, existing_vtable - vtableloc);
        } else {
            int i3 = this.num_vtables;
            int[] iArr2 = this.vtables;
            if (i3 == iArr2.length) {
                this.vtables = Arrays.copyOf(iArr2, i3 * 2);
            }
            int[] iArr3 = this.vtables;
            int i4 = this.num_vtables;
            this.num_vtables = i4 + 1;
            iArr3[i4] = offset();
            ByteBuffer byteBuffer = this.bb;
            byteBuffer.putInt(byteBuffer.capacity() - vtableloc, offset() - vtableloc);
        }
        this.nested = false;
        return vtableloc;
    }

    public void required(int table, int field) {
        int table_start = this.bb.capacity() - table;
        if (!(this.bb.getShort((table_start - this.bb.getInt(table_start)) + field) != 0)) {
            throw new AssertionError("FlatBuffers: field " + field + " must be set");
        }
    }

    public void finish(int root_table) {
        prep(this.minalign, 4);
        addOffset(root_table);
        this.bb.position(this.space);
        this.finished = true;
    }

    public void finish(int root_table, String file_identifier) {
        prep(this.minalign, 8);
        if (file_identifier.length() == 4) {
            for (int i = 3; i >= 0; i--) {
                addByte((byte) file_identifier.charAt(i));
            }
            finish(root_table);
            return;
        }
        throw new AssertionError("FlatBuffers: file identifier must be length 4");
    }

    public FlatBufferBuilder forceDefaults(boolean forceDefaults) {
        this.force_defaults = forceDefaults;
        return this;
    }

    public ByteBuffer dataBuffer() {
        finished();
        return this.bb;
    }

    @Deprecated
    private int dataStart() {
        finished();
        return this.space;
    }

    public byte[] sizedByteArray(int start, int length) {
        finished();
        byte[] array = new byte[length];
        this.bb.position(start);
        this.bb.get(array);
        return array;
    }

    public byte[] sizedByteArray() {
        return sizedByteArray(this.space, this.bb.capacity() - this.space);
    }

    public InputStream sizedInputStream() {
        finished();
        ByteBuffer duplicate = this.bb.duplicate();
        duplicate.position(this.space);
        duplicate.limit(this.bb.capacity());
        return new ByteBufferBackedInputStream(duplicate);
    }

    static class ByteBufferBackedInputStream extends InputStream {
        ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf2) {
            this.buf = buf2;
        }

        public int read() {
            try {
                return this.buf.get() & 255;
            } catch (BufferUnderflowException e) {
                return -1;
            }
        }
    }
}
