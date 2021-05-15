package com.sec.internal.helper.httpclient;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class DigestAuth {
    private static final String AKAV1_MD5 = "AKAv1-MD5";
    private static final String AKAV2_MD5 = "AKAv2-MD5";
    private static final String AKAV2_PASSWORD_KEY = "http-digest-akav2-password";
    private static final String AUTH = "auth";
    private static final String AUTH_INT = "auth-int";
    private static final char[] HEXADECIMAL = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String HMACMD5 = "HmacMD5";
    private static final String LOG_TAG = DigestAuth.class.getSimpleName();
    private static final String MD5 = "MD5";
    private static final String MD5_SESSION = "MD5-sess";
    private static final String md5 = "md5";
    private Algo mAlgorithm;
    private String mCnonce;
    private String mDigestURI;
    private String mEntity;
    private String mMethod;
    private int mNC = 0;
    private String mNonce;
    private String mPassword;
    private String mQOP;
    private String mRealm;
    private String mUsername;

    public enum Algo {
        UNKNOWN,
        MD5,
        MD5_SESSION,
        AKAV1_MD5,
        AKAV2_MD5,
        md5;

        public static Algo getAlgoType(String algorithm) {
            if (TextUtils.isEmpty(algorithm)) {
                return UNKNOWN;
            }
            char c = 65535;
            switch (algorithm.hashCode()) {
                case -1459419359:
                    if (algorithm.equals(DigestAuth.MD5_SESSION)) {
                        c = 2;
                        break;
                    }
                    break;
                case 76158:
                    if (algorithm.equals(DigestAuth.MD5)) {
                        c = 0;
                        break;
                    }
                    break;
                case 107902:
                    if (algorithm.equals(DigestAuth.md5)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1324439363:
                    if (algorithm.equals(DigestAuth.AKAV1_MD5)) {
                        c = 3;
                        break;
                    }
                    break;
                case 1325362884:
                    if (algorithm.equals(DigestAuth.AKAV2_MD5)) {
                        c = 4;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return MD5;
            }
            if (c == 1) {
                return md5;
            }
            if (c == 2) {
                return MD5_SESSION;
            }
            if (c == 3) {
                return AKAV1_MD5;
            }
            if (c != 4) {
                return UNKNOWN;
            }
            return AKAV2_MD5;
        }

        public String toString() {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[ordinal()];
            if (i == 1) {
                return DigestAuth.MD5;
            }
            if (i == 2) {
                return DigestAuth.md5;
            }
            if (i == 3) {
                return DigestAuth.MD5_SESSION;
            }
            if (i == 4) {
                return DigestAuth.AKAV1_MD5;
            }
            if (i != 5) {
                return "";
            }
            return DigestAuth.AKAV2_MD5;
        }
    }

    /* renamed from: com.sec.internal.helper.httpclient.DigestAuth$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo;

        static {
            int[] iArr = new int[Algo.values().length];
            $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo = iArr;
            try {
                iArr[Algo.MD5.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[Algo.md5.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[Algo.MD5_SESSION.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[Algo.AKAV1_MD5.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$helper$httpclient$DigestAuth$Algo[Algo.AKAV2_MD5.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public DigestAuth() {
    }

    public DigestAuth(String userName, String passwd, String realm, String nonce, String method, String digestURI, String algorithm, String qop) {
        this.mUsername = userName;
        this.mPassword = passwd;
        this.mRealm = realm;
        this.mNonce = nonce;
        this.mMethod = method;
        this.mDigestURI = digestURI;
        this.mAlgorithm = Algo.getAlgoType(algorithm);
        this.mQOP = qop;
        this.mEntity = "";
    }

    public void setDigestAuth(String userName, String passwd, String realm, String nonce, String method, String digestURI, String algorithm, String qop) {
        this.mUsername = userName;
        this.mPassword = passwd;
        this.mRealm = realm;
        this.mNonce = nonce;
        this.mMethod = method;
        this.mDigestURI = digestURI;
        this.mAlgorithm = Algo.getAlgoType(algorithm);
        this.mQOP = qop;
        this.mEntity = "";
    }

    public void setDigestAuth(String userName, String passwd, String realm, String nonce, String method, String digestURI, String algorithm, String qop, String body) {
        this.mUsername = userName;
        this.mPassword = passwd;
        this.mRealm = realm;
        this.mNonce = nonce;
        this.mMethod = method;
        this.mDigestURI = digestURI;
        this.mAlgorithm = Algo.getAlgoType(algorithm);
        this.mQOP = qop;
        this.mEntity = body;
    }

    public static String createCnonce() {
        byte[] tmp = new byte[8];
        new SecureRandom().nextBytes(tmp);
        return encode(tmp);
    }

    public static String encode(byte[] binaryData) {
        int n = binaryData.length;
        char[] buffer = new char[(n * 2)];
        for (int i = 0; i < n; i++) {
            char[] cArr = HEXADECIMAL;
            buffer[i * 2] = cArr[(binaryData[i] & 240) >> 4];
            buffer[(i * 2) + 1] = cArr[binaryData[i] & 15];
        }
        return new String(buffer);
    }

    public String getNC() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%08x", new Object[]{Integer.valueOf(this.mNC)});
        formatter.close();
        String str = LOG_TAG;
        IMSLog.d(str, "getNC(): " + sb.toString());
        return sb.toString();
    }

    public String getCnonce() {
        if (AUTH.equalsIgnoreCase(this.mQOP) || AUTH_INT.equalsIgnoreCase(this.mQOP)) {
            return this.mCnonce;
        }
        String str = LOG_TAG;
        IMSLog.d(str, "not auth: " + this.mQOP);
        return "";
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getRealm() {
        return this.mRealm;
    }

    public String getNonce() {
        return this.mNonce;
    }

    public String getQop() {
        return this.mQOP;
    }

    public String getAlgorithm() {
        return this.mAlgorithm.toString();
    }

    public String getDigestUri() {
        return this.mDigestURI;
    }

    public String getResp() {
        if (this.mAlgorithm == Algo.AKAV2_MD5) {
            this.mPassword = calculatePasswordForAkav2();
        }
        return calcResponseForMD5();
    }

    private String calcResponseForMD5() {
        try {
            MessageDigest digester = MessageDigest.getInstance(MD5);
            this.mNC++;
            this.mCnonce = createCnonce();
            StringBuilder sb = new StringBuilder();
            sb.append(getHexHA1(digester));
            sb.append(":");
            sb.append(this.mNonce);
            sb.append(":");
            if (AUTH.equalsIgnoreCase(this.mQOP) || AUTH_INT.equalsIgnoreCase(this.mQOP)) {
                sb.append(getNC());
                sb.append(":");
                sb.append(this.mCnonce);
                sb.append(":");
                sb.append(this.mQOP);
                sb.append(":");
            }
            sb.append(getHexHA2(digester));
            String result = encode(digester.digest(sb.toString().getBytes()));
            IMSLog.d(LOG_TAG, "calcResponseForMD5(): contents: " + sb.toString() + ", HEX RESP: " + result);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getHexHA1(MessageDigest digester) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mUsername);
        sb.append(":");
        sb.append(this.mRealm);
        sb.append(":");
        sb.append(this.mPassword);
        if (this.mAlgorithm == Algo.MD5_SESSION) {
            sb.append(":");
            sb.append(this.mNonce);
            sb.append(":");
            sb.append(this.mCnonce);
        }
        String result = encode(digester.digest(sb.toString().getBytes(Charset.forName("CP1252"))));
        String str = LOG_TAG;
        IMSLog.d(str, "getHexHA1(): contents: " + sb.toString() + ", HEX HA1: " + result);
        return result;
    }

    private String getHexHA2(MessageDigest digester) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mMethod);
        sb.append(":");
        sb.append(this.mDigestURI);
        if (AUTH_INT.equalsIgnoreCase(this.mQOP)) {
            sb.append(":");
            sb.append(getEntityHash(digester));
        }
        String result = encode(digester.digest(sb.toString().getBytes()));
        String str = LOG_TAG;
        IMSLog.d(str, "getHexHA2(): : contents: " + sb.toString() + ", HEX HA2: " + result);
        return result;
    }

    private String getEntityHash(MessageDigest digester) {
        String result = encode(digester.digest(this.mEntity.getBytes()));
        String str = LOG_TAG;
        IMSLog.d(str, "getEntityHash(): contents: " + this.mEntity + ", HEX entityHash: " + result);
        return result;
    }

    private String calculatePasswordForAkav2() {
        try {
            return encode(hmacMD5(AKAV2_PASSWORD_KEY.getBytes(), this.mPassword.getBytes()));
        } catch (Exception e) {
            Log.e(LOG_TAG, "Hmac encryption failed");
            return "";
        }
    }

    private byte[] hmacMD5(byte[] key, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMACMD5);
        mac.init(new SecretKeySpec(key, HMACMD5));
        return mac.doFinal(message);
    }
}
