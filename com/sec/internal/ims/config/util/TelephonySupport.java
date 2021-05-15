package com.sec.internal.ims.config.util;

import android.util.Log;
import com.sec.internal.helper.ByteArrayWriter;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.log.IMSLog;

public class TelephonySupport {
    private static final String LOG_TAG = TelephonySupport.class.getSimpleName();

    public static AkaResponse buildAkaResponse(String strResult) {
        byte[] res = null;
        byte[] Ck = null;
        byte[] Ik = null;
        byte[] auts = null;
        byte[] result = StrUtil.hexStringToBytes(strResult);
        if (result != null) {
            try {
                if (result[0] == -37) {
                    Log.d(LOG_TAG, "calculateAkaResponse: in");
                    byte reslen = result[1];
                    if (reslen > 0) {
                        res = new byte[reslen];
                        System.arraycopy(result, 2, res, 0, reslen);
                    }
                    byte cklen = result[reslen + 2];
                    if (cklen > 0) {
                        Ck = new byte[cklen];
                        System.arraycopy(result, reslen + 3, Ck, 0, cklen);
                    }
                    byte iklen = result[reslen + 3 + cklen];
                    if (iklen > 0) {
                        Ik = new byte[iklen];
                        System.arraycopy(result, reslen + 4 + cklen, Ik, 0, iklen);
                    }
                } else if (result[0] == -36) {
                    byte autslen = result[1];
                    if (autslen > 0) {
                        auts = new byte[autslen];
                        System.arraycopy(result, 2, auts, 0, autslen);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
                String str = LOG_TAG;
                Log.d(str, "error2:" + e2);
            }
        }
        if (res == null && auts == null) {
            return null;
        }
        return new AkaResponse(Ck, Ik, auts, res);
    }

    public static byte[] buildMainKey(String strIdentity, String result) {
        AkaResponse akaResponse = buildAkaResponse(result);
        if (akaResponse == null) {
            return null;
        }
        byte[] identity = strIdentity.getBytes();
        byte[] ik = akaResponse.getIk();
        String str = LOG_TAG;
        IMSLog.s(str, "IK :" + StrUtil.bytesToHexString(ik));
        byte[] ck = akaResponse.getCk();
        String str2 = LOG_TAG;
        IMSLog.s(str2, "CK :" + StrUtil.bytesToHexString(ck));
        if (ik == null || ck == null) {
            return null;
        }
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter(identity.length + ik.length + ck.length);
        byteArrayWriter.write(identity);
        byteArrayWriter.write(ik);
        byteArrayWriter.write(ck);
        return byteArrayWriter.getResult();
    }
}
