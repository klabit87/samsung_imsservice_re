package org.xbill.DNS;

public class DNSOutput {
    private byte[] array;
    private int pos;
    private int saved_pos;

    public DNSOutput(int size) {
        this.array = new byte[size];
        this.pos = 0;
        this.saved_pos = -1;
    }

    public DNSOutput() {
        this(32);
    }

    public int current() {
        return this.pos;
    }

    private void check(long val, int bits) {
        long max = 1 << bits;
        if (val < 0 || val > max) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(val);
            stringBuffer.append(" out of range for ");
            stringBuffer.append(bits);
            stringBuffer.append(" bit value");
            throw new IllegalArgumentException(stringBuffer.toString());
        }
    }

    private void need(int n) {
        byte[] bArr = this.array;
        int length = bArr.length;
        int i = this.pos;
        if (length - i < n) {
            int newsize = bArr.length * 2;
            if (newsize < i + n) {
                newsize = i + n;
            }
            byte[] newarray = new byte[newsize];
            System.arraycopy(this.array, 0, newarray, 0, this.pos);
            this.array = newarray;
        }
    }

    public void jump(int index) {
        if (index <= this.pos) {
            this.pos = index;
            return;
        }
        throw new IllegalArgumentException("cannot jump past end of data");
    }

    public void save() {
        this.saved_pos = this.pos;
    }

    public void restore() {
        int i = this.saved_pos;
        if (i >= 0) {
            this.pos = i;
            this.saved_pos = -1;
            return;
        }
        throw new IllegalStateException("no previous state");
    }

    public void writeU8(int val) {
        check((long) val, 8);
        need(1);
        byte[] bArr = this.array;
        int i = this.pos;
        this.pos = i + 1;
        bArr[i] = (byte) (val & 255);
    }

    public void writeU16(int val) {
        check((long) val, 16);
        need(2);
        byte[] bArr = this.array;
        int i = this.pos;
        int i2 = i + 1;
        this.pos = i2;
        bArr[i] = (byte) ((val >>> 8) & 255);
        this.pos = i2 + 1;
        bArr[i2] = (byte) (val & 255);
    }

    public void writeU16At(int val, int where) {
        check((long) val, 16);
        if (where <= this.pos - 2) {
            byte[] bArr = this.array;
            int where2 = where + 1;
            bArr[where] = (byte) ((val >>> 8) & 255);
            int i = where2 + 1;
            bArr[where2] = (byte) (val & 255);
            return;
        }
        throw new IllegalArgumentException("cannot write past end of data");
    }

    public void writeU32(long val) {
        check(val, 32);
        need(4);
        byte[] bArr = this.array;
        int i = this.pos;
        int i2 = i + 1;
        this.pos = i2;
        bArr[i] = (byte) ((int) ((val >>> 24) & 255));
        int i3 = i2 + 1;
        this.pos = i3;
        bArr[i2] = (byte) ((int) ((val >>> 16) & 255));
        int i4 = i3 + 1;
        this.pos = i4;
        bArr[i3] = (byte) ((int) ((val >>> 8) & 255));
        this.pos = i4 + 1;
        bArr[i4] = (byte) ((int) (val & 255));
    }

    public void writeByteArray(byte[] b, int off, int len) {
        need(len);
        System.arraycopy(b, off, this.array, this.pos, len);
        this.pos += len;
    }

    public void writeByteArray(byte[] b) {
        writeByteArray(b, 0, b.length);
    }

    public void writeCountedString(byte[] s) {
        if (s.length <= 255) {
            need(s.length + 1);
            byte[] bArr = this.array;
            int i = this.pos;
            this.pos = i + 1;
            bArr[i] = (byte) (255 & s.length);
            writeByteArray(s, 0, s.length);
            return;
        }
        throw new IllegalArgumentException("Invalid counted string");
    }

    public byte[] toByteArray() {
        int i = this.pos;
        byte[] out = new byte[i];
        System.arraycopy(this.array, 0, out, 0, i);
        return out;
    }
}
