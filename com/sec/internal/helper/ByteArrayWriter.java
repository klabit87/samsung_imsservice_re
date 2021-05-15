package com.sec.internal.helper;

import android.util.Log;
import java.util.Arrays;

public class ByteArrayWriter {
    private final byte[] buffer;
    private int curPosition = 0;

    public ByteArrayWriter(int length) {
        if (length > 0) {
            this.buffer = new byte[length];
            return;
        }
        throw new IllegalArgumentException("The length must be greater then 0.");
    }

    public void write(byte[] item) {
        if (item != null) {
            int itemLen = item.length;
            int i = this.curPosition;
            int i2 = i + itemLen;
            byte[] bArr = this.buffer;
            if (i2 > bArr.length) {
                throw new IllegalStateException("The buffer is overflowed.");
            } else if (itemLen > 0) {
                System.arraycopy(item, 0, bArr, i, itemLen);
                this.curPosition += itemLen;
            }
        } else {
            throw new IllegalArgumentException("The item must be not null.");
        }
    }

    public byte[] getResult() {
        Log.v("ByteArrayWriter", toString());
        int i = this.curPosition;
        byte[] bArr = this.buffer;
        if (i == bArr.length) {
            return bArr;
        }
        throw new IllegalStateException("The result is not completed yet.");
    }

    public String toString() {
        return "ByteArrayWriter [buffer=" + Arrays.toString(this.buffer) + ", curPosition=" + this.curPosition + "]";
    }
}
