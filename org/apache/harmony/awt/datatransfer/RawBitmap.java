package org.apache.harmony.awt.datatransfer;

public final class RawBitmap {
    public final int bMask;
    public final int bits;
    public final Object buffer;
    public final int gMask;
    public final int height;
    public final int rMask;
    public final int stride;
    public final int width;

    public RawBitmap(int w, int h, int stride2, int bits2, int rMask2, int gMask2, int bMask2, Object buffer2) {
        this.width = w;
        this.height = h;
        this.stride = stride2;
        this.bits = bits2;
        this.rMask = rMask2;
        this.gMask = gMask2;
        this.bMask = bMask2;
        this.buffer = buffer2;
    }

    public RawBitmap(int[] header, Object buffer2) {
        this.width = header[0];
        this.height = header[1];
        this.stride = header[2];
        this.bits = header[3];
        this.rMask = header[4];
        this.gMask = header[5];
        this.bMask = header[6];
        this.buffer = buffer2;
    }

    public int[] getHeader() {
        return new int[]{this.width, this.height, this.stride, this.bits, this.rMask, this.gMask, this.bMask};
    }
}
