package com.sec.internal.ims.servicemodules.im.util;

import java.util.Random;
import java.util.UUID;

public class TidGenerator {
    private static final int CLOCK_SEQ_LIMIT = 16384;
    private static final int CLOCK_SEQ_MASK = 16383;
    private static final long INTERVAL = 10000;
    private static final long MULTICAST = 1099511627776L;
    private static final long NODE_LIMIT = 281474976710656L;
    private static final long NODE_MASK = 281474976710655L;
    private static final long OFFSET = 12219292800000L;
    private static final long RESERVED = Long.MIN_VALUE;
    private static final long VERSION_NUMBER = 4096;
    private long mClockSeq = ((long) this.mRandom.nextInt(16384));
    private final long mNode;
    private long mPrevSysTime;
    private final Random mRandom;

    public TidGenerator() {
        Random random = new Random();
        this.mRandom = random;
        this.mNode = (random.nextLong() & NODE_MASK) | MULTICAST;
    }

    public UUID generate() {
        long sysTime = System.currentTimeMillis();
        if (sysTime <= this.mPrevSysTime) {
            this.mClockSeq = (this.mClockSeq + 1) & 16383;
        }
        long time = (OFFSET + sysTime) * 10000;
        long timeLo = -1 & time;
        long j = (timeLo << 32) | (((time >> 32) & 65535) << 16);
        long j2 = time;
        long j3 = timeLo;
        this.mPrevSysTime = sysTime;
        return new UUID(j | ((time >> 48) & 4095) | VERSION_NUMBER, (this.mClockSeq << 48) | RESERVED | this.mNode);
    }
}
