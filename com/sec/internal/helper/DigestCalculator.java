package com.sec.internal.helper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

public class DigestCalculator {
    private static final String ALGORITHM_AKAV1_MD5 = "AKAv1-MD5";
    private static final String ALGORITHM_MD5 = "MD5";
    private static final String QOP_AUTH_INT = "auth-int";
    private static final String SEPARATOR = ":";
    private String algorithm;
    private String cnonce;
    private String digestUri = null;
    private String httpMethod = null;
    private String nonce;
    private String nonceCount;
    private byte[] passwd;
    private String qop;
    private String realm;
    private String userName;

    public DigestCalculator(String userName2, String alg, String cnonce2, String nonce2, String nc, String qop2, String realm2, byte[] pwd, String method, String digestUri2, byte[] entityBody) {
        this.userName = userName2;
        this.algorithm = alg;
        this.cnonce = cnonce2;
        this.nonce = nonce2;
        this.nonceCount = nc;
        this.qop = qop2;
        this.realm = realm2;
        this.passwd = pwd;
        this.httpMethod = method;
        this.digestUri = digestUri2;
    }

    public String calculateDigest() {
        if (isInputDataValid()) {
            return calculateAuthDigest();
        }
        return null;
    }

    private boolean isInputDataValid() {
        if (this.httpMethod == null || this.algorithm == null || this.cnonce == null || this.qop == null || this.nonce == null || this.nonceCount == null || this.passwd == null || this.realm == null || this.userName == null || this.digestUri == null) {
            return false;
        }
        return true;
    }

    private String calculateAuthDigest() {
        String HA1 = calcDigestHA1();
        String data = getData();
        return new String(Hex.encodeHex(calcMD5((HA1 + SEPARATOR + data).getBytes())));
    }

    private String getData() {
        String hA2 = calcDigestHA2();
        return this.nonce + SEPARATOR + this.nonceCount + SEPARATOR + this.cnonce + SEPARATOR + this.qop + SEPARATOR + hA2;
    }

    private String calcDigestHA2() {
        String emptyentityBody;
        if (this.qop.equalsIgnoreCase(QOP_AUTH_INT)) {
            emptyentityBody = this.httpMethod + SEPARATOR + this.digestUri + SEPARATOR + new String(Hex.encodeHex(calcMD5("".getBytes())));
        } else {
            emptyentityBody = this.httpMethod + SEPARATOR + this.digestUri;
        }
        return new String(Hex.encodeHex(calcMD5(emptyentityBody.getBytes())));
    }

    private String calcDigestHA1() {
        if (!this.algorithm.equalsIgnoreCase(ALGORITHM_MD5) && !this.algorithm.equalsIgnoreCase(ALGORITHM_AKAV1_MD5)) {
            return null;
        }
        byte[] b = (this.userName + SEPARATOR + this.realm + SEPARATOR).getBytes();
        byte[] a1 = new byte[(b.length + this.passwd.length)];
        System.arraycopy(b, 0, a1, 0, b.length);
        byte[] bArr = this.passwd;
        System.arraycopy(bArr, 0, a1, b.length, bArr.length);
        return new String(Hex.encodeHex(calcMD5(a1)));
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
