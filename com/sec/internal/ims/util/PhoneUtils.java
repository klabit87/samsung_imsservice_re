package com.sec.internal.ims.util;

import android.telephony.PhoneNumberUtils;

public class PhoneUtils {
    private static String COUNTRY_AREA_CODE = "0";
    private static String COUNTRY_CODE = "+1";

    public static synchronized void initialize() {
        synchronized (PhoneUtils.class) {
            if (RcsSettingsUtils.getInstance() != null) {
                COUNTRY_CODE = RcsSettingsUtils.getInstance().getCountryCode();
                COUNTRY_AREA_CODE = RcsSettingsUtils.getInstance().getCountryAreaCode();
            }
        }
    }

    public static String formatNumberToInternational(String number) {
        if (number == null) {
            return null;
        }
        String phoneNumber = PhoneNumberUtils.stripSeparators(number.trim());
        if (phoneNumber.startsWith("00" + COUNTRY_CODE.substring(1))) {
            return COUNTRY_CODE + phoneNumber.substring(4);
        }
        String str = COUNTRY_AREA_CODE;
        if (str != null && str.length() > 0 && phoneNumber.startsWith(COUNTRY_AREA_CODE)) {
            return COUNTRY_CODE + phoneNumber.substring(COUNTRY_AREA_CODE.length());
        } else if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        } else {
            return COUNTRY_CODE + phoneNumber;
        }
    }

    public static String extractNumberFromUri(String uri) {
        if (uri == null || "".equals(uri)) {
            return null;
        }
        try {
            int index0 = uri.indexOf("<");
            if (index0 != -1) {
                uri = uri.substring(index0 + 1, uri.indexOf(">", index0));
            }
            int index1 = uri.indexOf("tel:");
            if (index1 != -1) {
                uri = uri.substring(index1 + 4);
            }
            int index12 = uri.indexOf("sip:");
            if (index12 != -1) {
                uri = uri.substring(index12 + 4, uri.indexOf("@", index12));
            }
            int index2 = uri.indexOf(";");
            if (index2 != -1) {
                uri = uri.substring(0, index2);
            }
            return formatNumberToInternational(uri);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }
}
