package com.sec.internal.ims.config.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Fips186_2 {
    public static int fips186_2_prf2(byte[] xKeyBytes, byte[] out) {
        BigInteger xKey = fromByteArray(xKeyBytes);
        ByteBuffer outBuf = ByteBuffer.wrap(out);
        int m = out.length / 40;
        SHA1 digester = new SHA1();
        BigInteger mod = new BigInteger("2").pow(xKeyBytes.length * 8);
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < 2; i++) {
                digester.update(Arrays.copyOf(toByteArray(xKey, 20), 64));
                ByteBuffer wBuf = ByteBuffer.allocate(20);
                wBuf.putInt(digester.H0);
                wBuf.putInt(digester.H1);
                wBuf.putInt(digester.H2);
                wBuf.putInt(digester.H3);
                wBuf.putInt(digester.H4);
                BigInteger wi = fromByteArray(wBuf.array());
                digester = new SHA1();
                xKey = xKey.add(BigInteger.ONE).add(wi).mod(mod);
                outBuf.put(toByteArray(wi, 20));
            }
        }
        return 0;
    }

    static byte[] toByteArray(BigInteger bi, int length) {
        byte[] bs = bi.toByteArray();
        if (bs.length == length) {
            return bs;
        }
        if (bs.length > length) {
            return Arrays.copyOfRange(bs, bs.length - length, bs.length);
        }
        byte[] rv = new byte[length];
        System.arraycopy(bs, 0, rv, length - bs.length, bs.length);
        return rv;
    }

    static BigInteger fromByteArray(byte[] arr) {
        byte[] src = new byte[(arr.length + 1)];
        System.arraycopy(arr, 0, src, 1, arr.length);
        src[0] = 0;
        return new BigInteger(src);
    }
}
