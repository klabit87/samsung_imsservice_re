package com.sec.internal.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class FingerPrintGenerator {
    public static String getFileMD5(File file, String algorithm) {
        int i;
        if (!file.isFile()) {
            return null;
        }
        FileInputStream in = null;
        byte[] buffer = new byte[1024];
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            FileInputStream in2 = new FileInputStream(file);
            while (true) {
                int read = in2.read(buffer, 0, 1024);
                int len = read;
                if (read == -1) {
                    break;
                }
                md.update(buffer, 0, len);
            }
            in2.close();
            try {
                in2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] digest = md.digest();
            StringBuilder hashBuilder = new StringBuilder(digest.length);
            for (byte aDigest : digest) {
                hashBuilder.append(Integer.toString((aDigest & 255) + 256, 16).substring(1));
            }
            return splitWithColon(hashBuilder.toString().toUpperCase());
        } catch (Exception e2) {
            e2.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            return null;
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    public static String splitWithColon(String in) {
        String temp = in;
        String out = temp.substring(0, 2);
        String temp2 = temp.substring(2);
        String out2 = out + ':';
        while (temp2 != null && temp2.length() >= 2) {
            out2 = out2 + temp2.substring(0, 2);
            temp2 = temp2.substring(2);
            if (temp2 != null && temp2.length() >= 2) {
                out2 = out2 + ':';
            }
        }
        return out2;
    }
}
