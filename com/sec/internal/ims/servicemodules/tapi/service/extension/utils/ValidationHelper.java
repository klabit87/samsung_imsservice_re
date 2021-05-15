package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

import android.content.pm.Signature;
import android.util.Base64;
import com.sec.internal.helper.header.AuthenticationHeaders;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

public class ValidationHelper {
    private static char[] byte2hex(byte b) {
        return new char[]{"0123456789ABCDEF".charAt((b & 240) >> 4), "0123456789ABCDEF".charAt(b & 15)};
    }

    public static String hash(byte[] key) {
        try {
            return new String(Base64.encode(MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA224).digest(key), 2)).replace('+', '-').replace('/', '_').replace(AuthenticationHeaders.HEADER_PRARAM_SPERATOR, "");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String getFingerPrint(Signature sig) {
        X509Certificate x509Cert = null;
        try {
            x509Cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(sig.toByteArray()));
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(x509Cert);
        return loadFingerprint(x509Cert);
    }

    private static String loadFingerprint(X509Certificate cert) {
        try {
            byte[] digest = new byte[0];
            try {
                digest = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM_SHA1).digest(cert.getEncoded());
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
            StringBuilder result = new StringBuilder();
            int len = digest.length - 1;
            for (int i = 0; i < len; i++) {
                result.append(byte2hex(digest[i]));
                result.append(':');
            }
            result.append(byte2hex(digest[len]));
            return result.toString();
        } catch (NoSuchAlgorithmException e2) {
            return null;
        }
    }

    public static boolean isContained(String[] c14nAlgorithms, String c14nAlgorithm) {
        for (String s : c14nAlgorithms) {
            if (s.equalsIgnoreCase(c14nAlgorithm)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkKeyLength(PublicKey key) {
        if (!(key instanceof RSAPublicKey) || ((RSAPublicKey) key).getModulus().bitLength() < 2048) {
            return false;
        }
        return true;
    }

    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), 0);
    }

    public static String decrypt(String input) {
        return new String(Base64.decode(input, 0));
    }

    public static boolean isTapiAuthorisationSupports() {
        return false;
    }
}
