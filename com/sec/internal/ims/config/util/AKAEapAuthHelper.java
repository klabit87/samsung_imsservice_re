package com.sec.internal.ims.config.util;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Locale;

public class AKAEapAuthHelper {
    private static final String AT_AUTS_HEADER = "0404";
    private static final String AT_MAC_HEADER = "0B050000";
    private static final int AT_MAC_LENGTH = 20;
    private static final String AT_MAC_WITH_ZEROED_VALUE = "00000000000000000000000000000000";
    private static final int AT_RES = 3;
    private static final int AT_RES_LENGTH_INDEX = 1;
    private static final int AT_RES_RESLENGTH_INDEX = 3;
    private static final int EAP_AUTN_HEADER_LENGTH = 8;
    private static final int EAP_AUTN_LENGTH = 32;
    private static final String EAP_FRAME_HEADER_CHALLENGE = "0201002817010000";
    private static final String EAP_FRAME_HEADER_SYNC_FAILURE = "0201002817040000";
    private static final int EAP_FRAME_IDENTIFIER_INDEX = 1;
    private static final int EAP_FRAME_LEN_INDEX = 3;
    public static final String EAP_JSON_TYPE = "application/vnd.gsma.eap-relay.v1.0+json";
    private static final int EAP_RAND_LENGTH = 32;
    public static final String EAP_RELAY_PACKET = "eap-relay-packet";
    private static final String LOG_TAG = AKAEapAuthHelper.class.getSimpleName();
    private static final String NONCE_SEPARATOR = "10";
    public static final String PARAM_EAP_ID = "EAP_ID";
    private static final int START_INDEX_AUTN_IN_AKA_CHALLENGE = 64;
    private static final int START_INDEX_RAND_IN_AKA_CHALLENGE = 24;

    public static String decodeChallenge(String akaChallenge) {
        return StrUtil.bytesToHexString(Base64.decode(akaChallenge.getBytes(), 2)).toUpperCase(Locale.US);
    }

    public static String generateChallengeResponse(String akaChallenge, String isimResponse, String imsiEap) {
        Log.d(LOG_TAG, "generateChallengeResponse()");
        String[] parts = {akaChallenge.substring(24, 56), akaChallenge.substring(64, 96)};
        IMSLog.d(LOG_TAG, "generateChallengeResponse: _org " + akaChallenge);
        String str = LOG_TAG;
        IMSLog.d(str, "generateChallengeResponse: _all " + (NONCE_SEPARATOR + parts[0] + NONCE_SEPARATOR + parts[1]));
        IMSLog.d(LOG_TAG, "generateChallengeResponse: rand " + parts[0]);
        IMSLog.d(LOG_TAG, "generateChallengeResponse: autn " + parts[1]);
        String eapFinalFrame = buildFinalEapFrame(isimResponse, imsiEap, StrUtil.hexStringToBytes(akaChallenge)[1]);
        if (isimResponse != null && eapFinalFrame != null) {
            return Base64.encodeToString(StrUtil.hexStringToBytes(eapFinalFrame), 2);
        }
        Log.e(LOG_TAG, "generateChallengeResponse: ISIM/USIM Auth failed");
        return null;
    }

    public static String getNonce(String akaChallenge) {
        String[] parts = {akaChallenge.substring(24, 56), akaChallenge.substring(64, 96)};
        return NONCE_SEPARATOR + parts[0] + NONCE_SEPARATOR + parts[1];
    }

    private static String getResFrameHeader(byte[] atResFrame, byte identifier) {
        byte[] eapFrameHeader = StrUtil.hexStringToBytes(EAP_FRAME_HEADER_CHALLENGE);
        if (eapFrameHeader == null) {
            return null;
        }
        int length = eapFrameHeader.length;
        int i = 20;
        if (atResFrame != null) {
            i = 20 + atResFrame.length;
        }
        eapFrameHeader[3] = (byte) (length + i);
        eapFrameHeader[1] = identifier;
        return StrUtil.bytesToHexString(eapFrameHeader);
    }

    private static String getAutSFrameHeader(byte[] atAutSFrame, byte identifier) {
        byte[] eapFrameHeader = StrUtil.hexStringToBytes(EAP_FRAME_HEADER_SYNC_FAILURE);
        if (eapFrameHeader == null) {
            return null;
        }
        eapFrameHeader[3] = (byte) (eapFrameHeader.length + (atAutSFrame != null ? atAutSFrame.length : 0));
        eapFrameHeader[1] = identifier;
        return StrUtil.bytesToHexString(eapFrameHeader);
    }

    private static String buildFinalEapFrame(String isimResponse, String imsiEap, byte identifier) {
        if (isimResponse == null) {
            Log.e(LOG_TAG, "buildFinalEapFrame: cannot build final frame");
            return null;
        }
        String atResFrame = buildAtResFrame(isimResponse);
        if (atResFrame == null) {
            Log.e(LOG_TAG, "buildFinalEapFrame: cannot build final frame, atResFrame is" + atResFrame);
            return null;
        } else if (StrUtil.hexStringToBytes(atResFrame) == null || StrUtil.hexStringToBytes(atResFrame)[0] != 3) {
            String eapFrameHeader = getAutSFrameHeader(StrUtil.hexStringToBytes(atResFrame), identifier);
            IMSLog.d(LOG_TAG, "buildFinalEapFrame calling for ISIM/USIM: EAP finalFrame " + eapFrameHeader + atResFrame);
            return eapFrameHeader + atResFrame;
        } else {
            String kAutn = buildK_AutnForAtMac(isimResponse, imsiEap);
            IMSLog.d(LOG_TAG, "buildFinalEapFrame: K_AUT " + kAutn);
            if (kAutn == null) {
                Log.e(LOG_TAG, "buildFinalEapFrame: K_AUT is null. Can not calculate final EAP frame");
                return null;
            }
            String eapFrameHeader2 = getResFrameHeader(StrUtil.hexStringToBytes(atResFrame), identifier);
            String eapFrameWithZeroedMac = eapFrameHeader2 + atResFrame + "0B05000000000000000000000000000000000000";
            IMSLog.d(LOG_TAG, "buildFinalEapFrame: resultWithZeroedMac " + eapFrameWithZeroedMac);
            String atMacValue = "";
            try {
                atMacValue = HmacSha1Signature.calculateRFC2104HMAC(StrUtil.hexStringToBytes(eapFrameWithZeroedMac), StrUtil.hexStringToBytes(kAutn));
                IMSLog.d(LOG_TAG, "buildFinalEapFrame calling for ISIM/USIM: AT_MAC " + atMacValue);
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                e.printStackTrace();
            }
            String eapFinalFrame = eapFrameHeader2 + atResFrame + AT_MAC_HEADER + atMacValue;
            IMSLog.d(LOG_TAG, "buildFinalEapFrame calling for ISIM/USIM: EAP finalFrame " + eapFinalFrame);
            return eapFinalFrame;
        }
    }

    private static String generateAtResHeader(int resLen) {
        byte[] atResHeader = {3, 0, 0, 0};
        atResHeader[1] = (byte) ((atResHeader.length + resLen) / 4);
        atResHeader[3] = (byte) (resLen * 8);
        return StrUtil.bytesToHexString(atResHeader);
    }

    private static String buildAtResFrame(String isimResponse) {
        AkaResponse akaResp = TelephonySupport.buildAkaResponse(isimResponse);
        if (akaResp == null) {
            Log.e(LOG_TAG, "buildAtResFrame: failed ISimAuthentication");
            return null;
        }
        byte[] res = akaResp.getRes();
        if (res != null) {
            String atResFrame = generateAtResHeader(res.length) + StrUtil.bytesToHexString(res);
            IMSLog.d(LOG_TAG, "buildAtResFrame: AT_RES Frame" + atResFrame);
            return atResFrame;
        }
        return AT_AUTS_HEADER + StrUtil.bytesToHexString(akaResp.getAuts());
    }

    private static String buildK_AutnForAtMac(String isimResponse, String imsiEap) {
        byte[] mainKeyBytes = TelephonySupport.buildMainKey(imsiEap, isimResponse);
        if (mainKeyBytes == null) {
            Log.d(LOG_TAG, "buildK_AutnForAtMac: key null, vail");
            return null;
        }
        SHA1 sha1 = new SHA1();
        byte[] mainKeyBytesWithSha1 = new byte[20];
        sha1.update(mainKeyBytes);
        sha1.digest(mainKeyBytesWithSha1);
        String str = LOG_TAG;
        IMSLog.d(str, "Main Key:" + StrUtil.bytesToHexString(mainKeyBytesWithSha1));
        byte[] out = new byte[MNO.UMOBILE];
        Fips186_2.fips186_2_prf2(mainKeyBytesWithSha1, out);
        String str2 = LOG_TAG;
        IMSLog.d(str2, "PRF OUTPUT with main key:" + StrUtil.bytesToHexString(out));
        return StrUtil.bytesToHexString(out).substring(32, 64);
    }

    public static String composeRootNai(int phoneId) {
        String mnc;
        String mcc;
        StringBuilder nai = new StringBuilder();
        ISimManager sm = SimManagerFactory.getSimManagerFromSimSlot(phoneId);
        if (sm == null) {
            return "";
        }
        String imsi = sm.getImsi();
        String operator = sm.getSimOperator();
        if (TextUtils.isEmpty(operator)) {
            IMSLog.d(LOG_TAG, "composeRootNai, operator empty");
            return "";
        }
        if (operator.length() == 5) {
            mcc = operator.substring(0, 3);
            mnc = "0" + operator.substring(3, 5);
        } else if (operator.length() == 6) {
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3, 6);
        } else {
            IMSLog.d(LOG_TAG, "composeRootNai, wrong operator");
            return "";
        }
        if (imsi != null) {
            nai.append(imsi);
            nai.append("@nai.epc.mnc");
            nai.append(mnc);
            nai.append(".mcc");
            nai.append(mcc);
            nai = nai.append(UtUtils.DOMAIN_NAME);
        }
        return nai.toString();
    }
}
