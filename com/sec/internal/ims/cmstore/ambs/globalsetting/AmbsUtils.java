package com.sec.internal.ims.cmstore.ambs.globalsetting;

import android.content.Context;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class AmbsUtils {
    private static final String TAG = AmbsUtils.class.getSimpleName();

    public static String generateRandomString(int length, boolean isNumber) {
        String base = "0123456789abcdefghijklmnopqrstuvwxyz";
        if (isNumber) {
            base = "0123456789";
        }
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            sb.append(base.charAt(rand.nextInt(base.length())));
        }
        return sb.toString();
    }

    public static String findErrorCode(String errorMsg, String key, char endChar) {
        int start;
        if (TextUtils.isEmpty(errorMsg) || TextUtils.isEmpty(key) || (start = errorMsg.indexOf(key)) < 0) {
            return null;
        }
        int start2 = start + key.length();
        int end = errorMsg.length();
        if (start2 >= 0) {
            int end2 = findEnd(errorMsg, endChar, start2, end);
            String str = TAG;
            Log.d(str, "findErrorCode:" + errorMsg.substring(start2, end2));
            return errorMsg.substring(start2, end2);
        }
        return null;
    }

    private static int findEnd(String errorMsg, char endChar, int start, int end) {
        if (endChar == 0) {
            return end;
        }
        int end2 = errorMsg.indexOf(endChar, start);
        if (end2 == -1) {
            return errorMsg.length();
        }
        return end2;
    }

    public static boolean isDefaultMessageAppInUse(Context context) {
        String currentDefaultMsgApp = null;
        String msgAppPkgName = PackageUtils.getMsgAppPkgName(context);
        try {
            currentDefaultMsgApp = Telephony.Sms.getDefaultSmsPackage(context);
        } catch (Exception e) {
            String str = TAG;
            Log.e(str, "Failed to currentDefaultMsgApp: " + e);
        }
        if (currentDefaultMsgApp == null) {
            currentDefaultMsgApp = Settings.Secure.getString(context.getContentResolver(), "sms_default_application");
        }
        boolean result = TextUtils.equals(currentDefaultMsgApp, msgAppPkgName);
        String str2 = TAG;
        Log.d(str2, "isDefaultMessageAppInUse : " + result + " msgAppPkgName: " + msgAppPkgName + " , current default Messaging App: " + currentDefaultMsgApp);
        return result;
    }

    public static String convertPhoneNumberToUserAct(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber;
        }
        if (phoneNumber.trim().length() > 10) {
            return phoneNumber.trim().substring(phoneNumber.trim().length() - 10);
        }
        if (phoneNumber.charAt(0) == '1') {
            return phoneNumber.substring(1);
        }
        if (phoneNumber.startsWith("+1")) {
            return phoneNumber.substring(2);
        }
        return phoneNumber;
    }

    public static String generateSmsHashCode(String phoneNum, int type, String body) {
        String str = TAG;
        IMSLog.s(str, "generateSmsHashCode: phoneNum: " + IMSLog.checker(phoneNum) + " type: " + type + " body: " + body);
        String number = makeE164Format(phoneNum);
        StringBuffer sb = new StringBuffer();
        if (type == 1) {
            sb.append(":::");
            sb.append(number);
            sb.append("::");
        } else {
            sb.append(number);
            sb.append(":::::");
        }
        sb.append(body);
        try {
            return hash(sb.toString());
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            String str2 = TAG;
            Log.e(str2, "generateSmsHashCode, Exception : " + e.toString());
            return null;
        }
    }

    private static String hash(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        byte[] stringInUTF8 = string.getBytes("UTF-8");
        m.update(stringInUTF8, 0, stringInUTF8.length);
        return new BigInteger(1, Arrays.copyOfRange(m.digest(), 0, 8)).toString(16);
    }

    private static String makeE164Format(String number) {
        if (number == null || number.length() == 0) {
            return null;
        }
        StringBuilder e164Number = new StringBuilder();
        if (number.charAt(0) != '+') {
            if (number.length() == 10) {
                e164Number.append("+1");
            }
            if (number.length() > 10) {
                e164Number.append("+");
            }
        }
        e164Number.append(number);
        return e164Number.toString();
    }

    public static boolean isInvalidShortCode(String number) {
        if (number == null || number.length() == 0 || number.length() >= 10 || number.charAt(0) != '+') {
            return false;
        }
        return true;
    }
}
