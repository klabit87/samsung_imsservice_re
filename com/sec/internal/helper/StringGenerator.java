package com.sec.internal.helper;

import java.util.Random;

public class StringGenerator {
    private static final int CHAR_ARRAY_SIZE = 62;
    private static final char[] charArray = new char[62];

    static {
        for (int i = 0; i < 10; i++) {
            charArray[i] = (char) (i + 48);
        }
        for (int i2 = 0; i2 < 26; i2++) {
            char[] cArr = charArray;
            cArr[i2 + 10] = (char) (i2 + 97);
            cArr[i2 + 36] = (char) (i2 + 65);
        }
    }

    private static char getChar(int index) {
        return charArray[index];
    }

    public static String generateString(int minSize, int maxSize) throws IllegalArgumentException {
        if (minSize <= 0 || minSize > maxSize) {
            throw new IllegalArgumentException();
        }
        Random rand = new Random();
        StringBuilder word = new StringBuilder();
        int length = maxSize > minSize ? rand.nextInt((maxSize - minSize) + 1) + minSize : maxSize;
        for (int i = 0; i < length; i++) {
            word.append(getChar(rand.nextInt(62)));
        }
        return word.toString();
    }
}
