package com.sun.mail.iap;

import java.io.ByteArrayInputStream;

public class ByteArray {
    private byte[] bytes;
    private int count;
    private int start;

    public ByteArray(byte[] b, int start2, int count2) {
        this.bytes = b;
        this.start = start2;
        this.count = count2;
    }

    public ByteArray(int size) {
        this(new byte[size], 0, size);
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public byte[] getNewBytes() {
        int i = this.count;
        byte[] b = new byte[i];
        System.arraycopy(this.bytes, this.start, b, 0, i);
        return b;
    }

    public int getStart() {
        return this.start;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count2) {
        this.count = count2;
    }

    public ByteArrayInputStream toByteArrayInputStream() {
        return new ByteArrayInputStream(this.bytes, this.start, this.count);
    }

    public void grow(int incr) {
        byte[] bArr = this.bytes;
        byte[] nbuf = new byte[(bArr.length + incr)];
        System.arraycopy(bArr, 0, nbuf, 0, bArr.length);
        this.bytes = nbuf;
    }
}
