package com.sec.internal.helper;

import android.util.Log;

public class StrUtil {
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String LOG_TAG = StrUtil.class.getSimpleName();

    public static String bytesToHexString(byte[] data) {
        if (data != null) {
            int l = data.length;
            char[] out = new char[(l << 1)];
            int j = 0;
            for (int i = 0; i < l; i++) {
                int j2 = j + 1;
                char[] cArr = DIGITS;
                out[j] = cArr[(data[i] & 240) >>> 4];
                j = j2 + 1;
                out[j2] = cArr[data[i] & 15];
            }
            return new String(out);
        }
        throw new IllegalArgumentException("Byte data cannot be null");
    }

    public static byte[] hexStringToBytes(String s) {
        if (s == null) {
            return null;
        }
        int sz = s.length();
        if (sz % 2 == 0) {
            byte[] ret = new byte[(sz / 2)];
            for (int i = 0; i < sz; i += 2) {
                ret[i / 2] = (byte) ((hexCharToInt(s.charAt(i)) << 4) | hexCharToInt(s.charAt(i + 1)));
            }
            return ret;
        }
        throw new IllegalArgumentException("String length shall be even");
    }

    private static int hexCharToInt(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 'A') + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return (c - 'a') + 10;
        }
        throw new RuntimeException("invalid hex char '" + c + "'");
    }

    public static String convertByteToHexWithLength(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%02X", new Object[]{Integer.valueOf(bytes.length)}));
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            builder.append(String.format("%02X", new Object[]{Byte.valueOf(bytes[i])}));
        }
        String hexStr = builder.toString();
        Log.d(LOG_TAG, "Byte to Hex: " + hexStr);
        return hexStr;
    }

    public static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < hex.length() - 1; i += 2) {
            int decimal = Integer.parseInt(hex.substring(i, i + 2), 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }
}
