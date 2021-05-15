package com.sec.internal.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.apache.commons.codec.binary.Hex;

public class HashManager {
    private static final String ALGORITHM_MD5 = "MD5";
    public static final String HASH_IMSUPDATE = "imsupdate";
    private static final String POSTFIX_FOR_MEMO = "_memo";
    private static final HashMap<Integer, HashManager> sInstances = new HashMap<>();
    private SharedPreferences mSharedPrefs = null;

    private HashManager(Context ctx, int phoneId) {
        this.mSharedPrefs = ImsSharedPrefHelper.getSharedPref(phoneId, ctx, ImsSharedPrefHelper.IMS_CONFIG, 0, false);
    }

    public static HashManager getInstance(Context ctx, int phoneId) {
        synchronized (sInstances) {
            if (sInstances.containsKey(Integer.valueOf(phoneId))) {
                HashManager hashManager = sInstances.get(Integer.valueOf(phoneId));
                return hashManager;
            }
            sInstances.put(Integer.valueOf(phoneId), new HashManager(ctx, phoneId));
            return sInstances.get(Integer.valueOf(phoneId));
        }
    }

    public static String generateMD5(String input) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(ALGORITHM_MD5);
            md5.update(input.getBytes("utf-8"));
            byte[] md5bytes = md5.digest();
            StringBuffer s1 = new StringBuffer();
            for (byte md5byte : md5bytes) {
                s1.append(Integer.toHexString((md5byte & 255) + 256).substring(1));
            }
            return s1.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return "";
        }
    }

    public static String generateHash(String source) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = new byte[0];
        try {
            buffer = source.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        md.update(buffer);
        byte[] digest = md.digest();
        StringBuilder hashBuilder = new StringBuilder(digest.length);
        for (byte aDigest : digest) {
            hashBuilder.append(Integer.toString((aDigest & 255) + 256, 16).substring(1));
        }
        return hashBuilder.toString();
    }

    public String getHash(byte[] content) {
        byte[] bytes = calcMD5(content);
        if (bytes != null) {
            return new String(Hex.encodeHex(bytes));
        }
        return null;
    }

    public boolean isHashChanged(String key, String hash) {
        return !TextUtils.equals(hash, getOldHash(key));
    }

    private String getOldHash(String key) {
        return this.mSharedPrefs.getString(key, "");
    }

    public void saveHash(String key, String hash) {
        SharedPreferences.Editor editor = this.mSharedPrefs.edit();
        editor.putString(key, hash);
        editor.apply();
    }

    public void saveMemo(String key, String memo) {
        SharedPreferences.Editor editor = this.mSharedPrefs.edit();
        editor.putString(key + POSTFIX_FOR_MEMO, memo);
        editor.apply();
    }

    private byte[] calcMD5(byte[] a1) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM_MD5);
            digest.reset();
            digest.update(a1);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
