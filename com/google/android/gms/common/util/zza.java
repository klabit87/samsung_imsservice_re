package com.google.android.gms.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class zza {
    public static MessageDigest zzeq(String str) {
        int i = 0;
        while (i < 2) {
            try {
                MessageDigest instance = MessageDigest.getInstance(str);
                if (instance != null) {
                    return instance;
                }
                i++;
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return null;
    }
}
