package com.sec.internal.helper;

import android.net.Uri;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;

public final class UriUtil {
    private static final String LOG_TAG = "UriUtil";

    public static ImsUri parseNumber(String number) {
        return parseNumber(number, (String) null);
    }

    public static ImsUri parseNumber(String number, String cc) {
        String e164;
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        if (cc == null) {
            cc = "ZZ";
        }
        try {
            Phonenumber.PhoneNumber phoneNumber = util.parse(number, cc.toUpperCase());
            if (isShortCode(number, cc)) {
                e164 = number;
            } else {
                e164 = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
            return ImsUri.parse("tel:" + e164);
        } catch (NumberParseException e) {
            Log.e(LOG_TAG, e.getClass().getSimpleName() + "!! " + e.getMessage());
            return null;
        }
    }

    public static boolean equals(ImsUri uri1, ImsUri uri2) {
        String msdn1 = null;
        String msdn2 = null;
        if (uri1 == null || uri2 == null) {
            return false;
        }
        if (!"sip".equalsIgnoreCase(uri1.getScheme())) {
            msdn1 = getMsisdnNumber(uri1);
        } else if (uri1.toString().contains("user=phone")) {
            msdn1 = uri1.getUser();
        }
        if (!"sip".equalsIgnoreCase(uri2.getScheme())) {
            msdn2 = getMsisdnNumber(uri2);
        } else if (uri2.toString().contains("user=phone")) {
            msdn2 = uri2.getUser();
        }
        if ((msdn1 == null && msdn2 != null) || (msdn1 != null && msdn2 == null)) {
            return false;
        }
        if (msdn1 != null) {
            return msdn1.equals(msdn2);
        }
        return uri1.equals(uri2);
    }

    public static boolean hasMsisdnNumber(ImsUri uri) {
        if (uri == null) {
            return false;
        }
        if ("tel".equalsIgnoreCase(uri.getScheme())) {
            return true;
        }
        String user = uri.getUser();
        if (uri.toString().contains("user=phone") || (user != null && user.matches("[\\+\\d]+"))) {
            return true;
        }
        return false;
    }

    public static String getMsisdnNumber(ImsUri uri) {
        if (uri == null) {
            return null;
        }
        if ("tel".equalsIgnoreCase(uri.getScheme())) {
            String uriStr = uri.toString();
            int end = uriStr.indexOf(59);
            if (end > 0) {
                return uriStr.substring(4, end);
            }
            return uriStr.substring(4);
        }
        String user = uri.getUser();
        if (uri.toString().contains("user=phone")) {
            return user;
        }
        if (user == null) {
            Log.d(LOG_TAG, "user is null. uri: " + uri.toString());
            return null;
        } else if (user.matches("[\\+\\d]+")) {
            return user;
        } else {
            return null;
        }
    }

    public static boolean isValidNumber(String number, String countryCode) {
        if (number == null || countryCode == null) {
            return false;
        }
        if (number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            Log.e(LOG_TAG, "isValidNumber: invalid special character in number");
            return false;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            PhoneNumberUtil.ValidationResult result = util.isPossibleNumberWithReason(util.parse(number, countryCode.toUpperCase()));
            if (result == PhoneNumberUtil.ValidationResult.IS_POSSIBLE) {
                return true;
            }
            if (result != PhoneNumberUtil.ValidationResult.TOO_LONG || !"US".equalsIgnoreCase(countryCode) || number.length() <= 9) {
                return false;
            }
            return true;
        } catch (NumberParseException e) {
            return false;
        } catch (NullPointerException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public static boolean isShortCode(String number, String cc) {
        if ("US".equalsIgnoreCase(cc)) {
            if (number.length() < 10) {
                return true;
            }
            if (number.length() == 10 && (number.charAt(0) == '0' || number.charAt(0) == '1')) {
                return true;
            }
            if (number.length() == 11 && number.charAt(0) == '1' && (number.charAt(1) == '0' || number.charAt(1) == '1')) {
                return true;
            }
            if (number.startsWith("+1") && number.length() == 12 && (number.charAt(2) == '0' || number.charAt(2) == '1')) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static Uri buildUri(String configUri, int simSlot) {
        Uri.Builder buildUpon = Uri.parse(configUri).buildUpon();
        return buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + simSlot).build();
    }

    public static int getSimSlotFromUri(Uri uri) {
        if (uri.getFragment() == null) {
            Log.i(LOG_TAG, "fragment is null. get simSlot from priority policy.");
            if (uri.toString().contains("#simslot")) {
                Log.d(LOG_TAG, "this should not happen: " + uri.toString());
            }
            return SimUtil.getSimSlotPriority();
        } else if (uri.getFragment().contains("subid")) {
            int subid = Character.getNumericValue(uri.getFragment().charAt(5));
            if (subid >= 0) {
                return Extensions.SubscriptionManager.getSlotId(subid);
            }
            Log.i(LOG_TAG, "Invalid subId:" + subid + ". get simSlot from priority policy");
            return SimUtil.getSimSlotPriority();
        } else if (uri.getFragment().contains(ImsConstants.Uris.FRAGMENT_SIM_SLOT)) {
            int simslot = Character.getNumericValue(uri.getFragment().charAt(7));
            if (simslot >= 0) {
                return simslot;
            }
            Log.i(LOG_TAG, "Invalid simslot:" + simslot + ". get it from priority policy");
            return SimUtil.getSimSlotPriority();
        } else {
            Log.i(LOG_TAG, "Invalid fragment:" + uri.getFragment() + ". get simSlot from priority policy");
            return SimUtil.getSimSlotPriority();
        }
    }
}
