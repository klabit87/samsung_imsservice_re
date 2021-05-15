package com.sec.internal.ims.config.util;

import android.text.TextUtils;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.log.IMSLog;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1Signature {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String calculateRFC2104HMAC(byte[] data, byte[] key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        String hMac = StrUtil.bytesToHexString(mac.doFinal(data));
        IMSLog.s("StrUtil", "calculateRFC2104HMAC: " + hMac);
        if (TextUtils.isEmpty(hMac)) {
            return "";
        }
        return hMac.substring(0, 32);
    }
}
