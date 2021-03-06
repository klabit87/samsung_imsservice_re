package org.xbill.DNS;

public final class Serial {
    private static final long MAX32 = 4294967295L;

    private Serial() {
    }

    public static int compare(long serial1, long serial2) {
        if (serial1 < 0 || serial1 > MAX32) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(serial1);
            stringBuffer.append(" out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        } else if (serial2 < 0 || serial2 > MAX32) {
            StringBuffer stringBuffer2 = new StringBuffer();
            stringBuffer2.append(serial2);
            stringBuffer2.append(" out of range");
            throw new IllegalArgumentException(stringBuffer2.toString());
        } else {
            long diff = serial1 - serial2;
            if (diff >= MAX32) {
                diff -= 4294967296L;
            } else if (diff < -4294967295L) {
                diff += 4294967296L;
            }
            return (int) diff;
        }
    }

    public static long increment(long serial) {
        if (serial < 0 || serial > MAX32) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(serial);
            stringBuffer.append(" out of range");
            throw new IllegalArgumentException(stringBuffer.toString());
        } else if (serial == MAX32) {
            return 0;
        } else {
            return 1 + serial;
        }
    }
}
