package com.sec.internal.constants.ims.servicemodules.im;

import android.util.SparseArray;

public enum FileDisposition {
    ATTACH(0),
    RENDER(1);
    
    private static final SparseArray<FileDisposition> mValueToEnum = null;
    int mCode;

    static {
        int i;
        mValueToEnum = new SparseArray<>();
        for (FileDisposition entry : values()) {
            mValueToEnum.put(entry.toInt(), entry);
        }
    }

    private FileDisposition(int disposition) {
        this.mCode = disposition;
    }

    public final int toInt() {
        return this.mCode;
    }

    public static FileDisposition valueOf(int value) {
        FileDisposition entry = mValueToEnum.get(value);
        if (entry != null) {
            return entry;
        }
        throw new IllegalArgumentException("No enum const class " + FileDisposition.class.getName() + "." + value + "!");
    }
}
