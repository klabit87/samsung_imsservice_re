package com.sun.mail.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ASCIIUtility {
    private ASCIIUtility() {
    }

    public static int parseInt(byte[] b, int start, int end, int radix) throws NumberFormatException {
        int limit;
        if (b != null) {
            int result = 0;
            boolean negative = false;
            int i = start;
            if (end > start) {
                if (b[i] == 45) {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                    i++;
                } else {
                    limit = -2147483647;
                }
                int multmin = limit / radix;
                if (i < end) {
                    int i2 = i + 1;
                    int digit = Character.digit((char) b[i], radix);
                    if (digit >= 0) {
                        result = -digit;
                        i = i2;
                    } else {
                        throw new NumberFormatException("illegal number: " + toString(b, start, end));
                    }
                }
                while (i < end) {
                    int i3 = i + 1;
                    int digit2 = Character.digit((char) b[i], radix);
                    if (digit2 < 0) {
                        throw new NumberFormatException("illegal number");
                    } else if (result >= multmin) {
                        int result2 = result * radix;
                        if (result2 >= limit + digit2) {
                            result = result2 - digit2;
                            i = i3;
                        } else {
                            throw new NumberFormatException("illegal number");
                        }
                    } else {
                        throw new NumberFormatException("illegal number");
                    }
                }
                if (!negative) {
                    return -result;
                }
                if (i > start + 1) {
                    return result;
                }
                throw new NumberFormatException("illegal number");
            }
            throw new NumberFormatException("illegal number");
        }
        throw new NumberFormatException("null");
    }

    public static int parseInt(byte[] b, int start, int end) throws NumberFormatException {
        return parseInt(b, start, end, 10);
    }

    public static long parseLong(byte[] b, int start, int end, int radix) throws NumberFormatException {
        long limit;
        if (b != null) {
            long result = 0;
            boolean negative = false;
            int i = start;
            if (end > start) {
                if (b[i] == 45) {
                    negative = true;
                    limit = Long.MIN_VALUE;
                    i++;
                } else {
                    limit = -9223372036854775807L;
                }
                long multmin = limit / ((long) radix);
                if (i < end) {
                    int i2 = i + 1;
                    int digit = Character.digit((char) b[i], radix);
                    if (digit >= 0) {
                        result = (long) (-digit);
                        i = i2;
                    } else {
                        throw new NumberFormatException("illegal number: " + toString(b, start, end));
                    }
                }
                while (i < end) {
                    int i3 = i + 1;
                    int digit2 = Character.digit((char) b[i], radix);
                    if (digit2 < 0) {
                        throw new NumberFormatException("illegal number");
                    } else if (result >= multmin) {
                        long result2 = result * ((long) radix);
                        if (result2 >= ((long) digit2) + limit) {
                            result = result2 - ((long) digit2);
                            i = i3;
                        } else {
                            throw new NumberFormatException("illegal number");
                        }
                    } else {
                        throw new NumberFormatException("illegal number");
                    }
                }
                if (!negative) {
                    return -result;
                }
                if (i > start + 1) {
                    return result;
                }
                throw new NumberFormatException("illegal number");
            }
            throw new NumberFormatException("illegal number");
        }
        throw new NumberFormatException("null");
    }

    public static long parseLong(byte[] b, int start, int end) throws NumberFormatException {
        return parseLong(b, start, end, 10);
    }

    public static String toString(byte[] b, int start, int end) {
        int size = end - start;
        char[] theChars = new char[size];
        int i = 0;
        int j = start;
        while (i < size) {
            theChars[i] = (char) (b[j] & 255);
            i++;
            j++;
        }
        return new String(theChars);
    }

    public static String toString(ByteArrayInputStream is) {
        int size = is.available();
        char[] theChars = new char[size];
        byte[] bytes = new byte[size];
        is.read(bytes, 0, size);
        for (int i = 0; i < size; i++) {
            theChars[i] = (char) (bytes[i] & 255);
        }
        return new String(theChars);
    }

    public static byte[] getBytes(String s) {
        char[] chars = s.toCharArray();
        int size = chars.length;
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        if (is instanceof ByteArrayInputStream) {
            int size = is.available();
            byte[] buf = new byte[size];
            int read = is.read(buf, 0, size);
            return buf;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf2 = new byte[1024];
        while (true) {
            int read2 = is.read(buf2, 0, 1024);
            int len = read2;
            if (read2 == -1) {
                int i = len;
                return bos.toByteArray();
            }
            bos.write(buf2, 0, len);
        }
    }
}
