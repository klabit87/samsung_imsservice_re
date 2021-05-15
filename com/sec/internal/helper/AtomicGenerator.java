package com.sec.internal.helper;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicGenerator {
    private static final AtomicLong mAtomicLong = new AtomicLong(0);

    public static long generateUniqueLong() {
        long ret = mAtomicLong.incrementAndGet();
        if (ret >= 0) {
            return ret;
        }
        mAtomicLong.set(0);
        return mAtomicLong.incrementAndGet();
    }
}
