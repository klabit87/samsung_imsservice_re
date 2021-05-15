package com.sec.internal.ims.gba;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.helper.ByteArrayWriter;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class GbaUtility {
    private static final String ALGORITHM_HMAC_SHA_256 = "HmacSHA256";
    private static final String TAG = "GbaUtility";
    private static final int TRANSFER_BASE = 256;

    private static byte[] getByteArrayForLength(int value) {
        byte[] content = new byte[2];
        content[1] = (byte) (value % 256);
        content[0] = (byte) (value / 256);
        return content;
    }

    public static byte[] convertCipherSuite(String cipher) {
        Log.d(TAG, "ConvertCipherSuite Cipher Suite: " + cipher);
        if (TextUtils.isEmpty(cipher)) {
            return new byte[]{0, 47};
        }
        char c = 65535;
        switch (cipher.hashCode()) {
            case -2099629145:
                if (cipher.equals("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384")) {
                    c = 13;
                    break;
                }
                break;
            case -1819217787:
                if (cipher.equals("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256")) {
                    c = 2;
                    break;
                }
                break;
            case -1685240195:
                if (cipher.equals("TLS_DHE_RSA_WITH_AES_256_CBC_SHA")) {
                    c = 8;
                    break;
                }
                break;
            case -847673263:
                if (cipher.equals("TLS_AES_256_GCM_SHA384")) {
                    c = 14;
                    break;
                }
                break;
            case -778197189:
                if (cipher.equals("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256")) {
                    c = 0;
                    break;
                }
                break;
            case -605277298:
                if (cipher.equals("TLS_RSA_WITH_3DES_EDE_CBC_SHA")) {
                    c = 12;
                    break;
                }
                break;
            case -455697113:
                if (cipher.equals("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")) {
                    c = 1;
                    break;
                }
                break;
            case -432745621:
                if (cipher.equals("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA")) {
                    c = 4;
                    break;
                }
                break;
            case -339225213:
                if (cipher.equals("TLS_RSA_WITH_AES_128_GCM_SHA256")) {
                    c = 9;
                    break;
                }
                break;
            case -319124957:
                if (cipher.equals("TLS_RSA_WITH_AES_128_CBC_SHA")) {
                    c = 10;
                    break;
                }
                break;
            case 795671431:
                if (cipher.equals("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA")) {
                    c = 3;
                    break;
                }
                break;
            case 796258769:
                if (cipher.equals("TLS_AES_128_GCM_SHA256")) {
                    c = 15;
                    break;
                }
                break;
            case 867534079:
                if (cipher.equals("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA")) {
                    c = 5;
                    break;
                }
                break;
            case 909292095:
                if (cipher.equals("TLS_RSA_WITH_AES_256_CBC_SHA")) {
                    c = 11;
                    break;
                }
                break;
            case 1381310049:
                if (cipher.equals("TLS_DHE_RSA_WITH_AES_128_CBC_SHA")) {
                    c = 7;
                    break;
                }
                break;
            case 2095951131:
                if (cipher.equals("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA")) {
                    c = 6;
                    break;
                }
                break;
            case 2102337526:
                if (cipher.equals("TLS_CHACHA20_POLY1305_SHA256")) {
                    c = 16;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return new byte[]{-64, 43};
            case 1:
                return new byte[]{-64, 47};
            case 2:
                return new byte[]{0, -98};
            case 3:
                return new byte[]{-64, 10};
            case 4:
                return new byte[]{-64, 9};
            case 5:
                return new byte[]{-64, 19};
            case 6:
                return new byte[]{-64, 20};
            case 7:
                return new byte[]{0, 51};
            case 8:
                return new byte[]{0, 57};
            case 9:
                return new byte[]{0, -100};
            case 10:
                return new byte[]{0, 47};
            case 11:
                return new byte[]{0, 53};
            case 12:
                return new byte[]{0, 10};
            case 13:
                return new byte[]{-64, 48};
            case 14:
                return new byte[]{19, 2};
            case 15:
                return new byte[]{19, 1};
            case 16:
                return new byte[]{19, 3};
            default:
                return new byte[]{0, 47};
        }
    }

    public static synchronized String igenerateGbaMEKey(byte[] gbaType, byte[] ck, byte[] ik, byte[] rand, byte[] impi, byte[] fqdn, String lifeTime, String btid, boolean isTLS, byte[] cipherSuite) {
        String base64EncodedGbaKey;
        byte[] bArr = fqdn;
        synchronized (GbaUtility.class) {
            if (gbaType == null || ck == null || ik == null || rand == null || impi == null || bArr == null) {
                throw new IllegalArgumentException("GBA ME KEY Calculation - input cannot be null");
            }
            Log.i(TAG, "gbatype = " + Arrays.toString(gbaType));
            Log.i(TAG, "ck = " + Arrays.toString(ck));
            Log.i(TAG, "ik = " + Arrays.toString(ik));
            Log.i(TAG, "rand = " + Arrays.toString(rand));
            Log.i(TAG, "fqdn for nafid = " + Arrays.toString(fqdn));
            byte[] fc = {1};
            byte[] UaSecurityProtocolId = {1, 0, 0, 0, 2};
            byte[] UaSecurityProtocolId_tls = {1, 0, 1, cipherSuite[0], cipherSuite[1]};
            byte[] p0 = {ReqMsg.request_open_sip_dialog, 98, 97, 45, ReqMsg.request_silent_log_enabled, 101};
            byte[] l0 = getByteArrayForLength(p0.length);
            byte[] p1 = rand;
            byte[] l1 = getByteArrayForLength(p1.length);
            byte[] p2 = impi;
            byte[] l2 = getByteArrayForLength(p2.length);
            byte[] p3 = calculateNafId(bArr, UaSecurityProtocolId);
            byte[] l3 = getByteArrayForLength(p3.length);
            byte[] p3_tls = calculateNafId(bArr, UaSecurityProtocolId_tls);
            byte[] l3_tls = getByteArrayForLength(p3_tls.length);
            byte[] ks = calculateKs(ck, ik);
            byte[] p3_tls2 = p3_tls;
            byte[] s = calculateS(fc, p0, l0, p1, l1, p2, l2, p3, l3);
            byte[] s_tls = calculateS(fc, p0, l0, p1, l1, p2, l2, p3_tls2, l3_tls);
            byte[] kdfResult = calculate(ks, s);
            byte[] kdfResult_tls = calculate(ks, s_tls);
            if (isTLS) {
                base64EncodedGbaKey = Base64.encodeToString(kdfResult_tls, 2);
            } else {
                base64EncodedGbaKey = Base64.encodeToString(kdfResult, 2);
            }
            byte[] bArr2 = p0;
            StringBuilder sb = new StringBuilder();
            byte[] bArr3 = fc;
            sb.append("returning base64EncodedGbaKey [ ");
            sb.append(base64EncodedGbaKey);
            sb.append(" ]");
            Log.i(TAG, sb.toString());
        }
        return base64EncodedGbaKey;
    }

    private static byte[] calculateNafId(byte[] fqdn, byte[] uaSecurityProtocolId) {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter(fqdn.length + uaSecurityProtocolId.length);
        byteArrayWriter.write(fqdn);
        byteArrayWriter.write(uaSecurityProtocolId);
        return byteArrayWriter.getResult();
    }

    private static byte[] calculateKs(byte[] ck, byte[] ik) {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter(ck.length + ik.length);
        byteArrayWriter.write(ck);
        byteArrayWriter.write(ik);
        return byteArrayWriter.getResult();
    }

    private static byte[] calculateS(byte[] Fc, byte[] p0, byte[] l0, byte[] p1, byte[] l1, byte[] p2, byte[] l2, byte[] p3, byte[] l3) {
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter(0 + Fc.length + p0.length + l0.length + p1.length + l1.length + p2.length + l2.length + p3.length + l3.length);
        byteArrayWriter.write(Fc);
        byteArrayWriter.write(p0);
        byteArrayWriter.write(l0);
        byteArrayWriter.write(p1);
        byteArrayWriter.write(l1);
        byteArrayWriter.write(p2);
        byteArrayWriter.write(l2);
        byteArrayWriter.write(p3);
        byteArrayWriter.write(l3);
        return byteArrayWriter.getResult();
    }

    public static byte[] calculate(byte[] paramKey, byte[] paramS) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA_256);
            mac.init(new SecretKeySpec(paramKey, mac.getAlgorithm()));
            mac.update(paramS);
            return mac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("GBA ME KEY Algo Calculation encountered an error");
        } catch (InvalidKeyException e2) {
            e2.printStackTrace();
            throw new IllegalArgumentException("GBA ME KEY Algo Calculation encountered an error");
        }
    }

    public static String getNafUrl(String url) {
        String host = null;
        try {
            host = new URI(url).getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getNafUrl " + host);
        return host;
    }

    public static String getNafPath(String url) {
        try {
            URI nafUri = new URI(url);
            if (!nafUri.getPath().isEmpty()) {
                return nafUri.getPath();
            }
            return "/";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "/";
        }
    }

    public static byte[] getNafId(String fqdn) {
        String[] realms = fqdn.split("@");
        if (realms[1].contains(";")) {
            return realms[1].split(";")[0].getBytes(StandardCharsets.UTF_8);
        }
        return realms[1].getBytes(StandardCharsets.UTF_8);
    }

    public static boolean isTls(String url) {
        try {
            if (OMAGlobalVariables.HTTPS.equals(new URI(url).getScheme())) {
                return true;
            }
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getSecurityProtocolId(byte[] nafId, byte[] cipherSuite, boolean isTls) {
        byte[] UaSecurityProtocolId_tls = {1, 0, 1, cipherSuite[0], cipherSuite[1]};
        byte[] UaSecurityProtocolId = {1, 0, 1, 0, 47};
        if (isTls) {
            byte[] bNafid = new byte[(nafId.length + UaSecurityProtocolId_tls.length)];
            System.arraycopy(nafId, 0, bNafid, 0, nafId.length);
            System.arraycopy(UaSecurityProtocolId_tls, 0, bNafid, nafId.length, UaSecurityProtocolId_tls.length);
            return bNafid;
        }
        byte[] bNafid2 = new byte[(nafId.length + UaSecurityProtocolId.length)];
        System.arraycopy(nafId, 0, bNafid2, 0, nafId.length);
        System.arraycopy(UaSecurityProtocolId, 0, bNafid2, nafId.length, UaSecurityProtocolId.length);
        return bNafid2;
    }
}
