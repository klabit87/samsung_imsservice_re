package com.sec.internal.ims.servicemodules.im.strategy;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.log.IMSLog;
import java.io.PrintStream;

class StrategyUtils {
    private static final String LOG_TAG = StrategyUtils.class.getSimpleName();

    StrategyUtils() {
    }

    static boolean isCapabilityValidUriForUS(ImsUri uri, int phoneId) {
        if (uri == null) {
            return false;
        }
        String msdn = UriUtil.getMsisdnNumber(uri);
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phonenumber = phoneUtil.parse(msdn, "US");
            PhoneNumberUtil.ValidationResult result = phoneUtil.isPossibleNumberWithReason(phonenumber);
            if (result != PhoneNumberUtil.ValidationResult.IS_POSSIBLE) {
                String str = LOG_TAG;
                IMSLog.s(str, "isCapabilityValidUri: msdn " + msdn);
                if (result != PhoneNumberUtil.ValidationResult.TOO_LONG) {
                    IMSLog.i(LOG_TAG, phoneId, "isCapabilityValidUri: Impossible phone number");
                    return false;
                } else if (msdn == null || !msdn.startsWith("+1") || msdn.length() < 12) {
                    IMSLog.i(LOG_TAG, phoneId, "isCapabilityValidUri: Impossible too long phone number");
                    return false;
                }
            }
            phonenumber.clearCountryCode();
            String number = String.valueOf(phonenumber.getNationalNumber());
            if (number.length() <= 3) {
                return false;
            }
            String areaCode = number.substring(0, 3);
            if ("900".equals(areaCode) || (areaCode.charAt(0) == '8' && areaCode.charAt(1) == areaCode.charAt(2))) {
                IMSLog.i(LOG_TAG, phoneId, "isCapabilityValidUri: 900 8YY contact. invalid request");
                return false;
            }
            try {
                if (phoneUtil.parse(msdn, "US").getCountryCode() == 1 && UriUtil.isShortCode(msdn, "US")) {
                    String str2 = LOG_TAG;
                    IMSLog.i(str2, phoneId, "isCapabilityValidUri: ShortCode. invalid request. msdn " + IMSLog.numberChecker(msdn));
                    return false;
                }
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
            return true;
        } catch (NumberParseException e2) {
            PrintStream printStream = System.err;
            printStream.println("Not a valid number. NumberParseException was thrown: " + e2);
            return false;
        }
    }
}
