package com.sec.internal.ims.core.sim;

import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.internal.helper.MccTable;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorVzw extends SimDataAdaptor {
    private static final String LOG_TAG = SimDataAdaptorVzw.class.getSimpleName();

    SimDataAdaptorVzw(SimManager simManager) {
        super(simManager);
    }

    private boolean checkAvailableImpu(String impu) {
        Log.i(LOG_TAG, "checkAvailableImpu:");
        String operator = this.mSimManager.getSimOperator();
        if (this.mSimManager.isLabSimCard()) {
            Log.i(LOG_TAG, "LAB SIM inserted. return true..");
            return true;
        }
        String msisdn = this.mSimManager.getMsisdn();
        if (!TextUtils.isEmpty(msisdn) && !TextUtils.isEmpty(impu)) {
            List<String> impuNumbers = Arrays.asList(impu.replaceAll("[^+?0-9]+", " ").trim().split(" "));
            if (impuNumbers.size() > 0) {
                String str = LOG_TAG;
                IMSLog.s(str, "impuNumber: " + impuNumbers.get(0) + ", msisdn: " + msisdn);
                if (TextUtils.isEmpty(operator) || operator.length() < 3) {
                    Log.e(LOG_TAG, "SimController : refresh: SIM operator is unknown.");
                    return false;
                }
                PhoneNumberUtil util = PhoneNumberUtil.getInstance();
                try {
                    String countryCode = MccTable.countryCodeForMcc(Integer.parseInt(operator.substring(0, 3))).toUpperCase();
                    Phonenumber.PhoneNumber impuPhoneNumber = util.parse(impuNumbers.get(0), countryCode);
                    Phonenumber.PhoneNumber msisdnPhoneNumber = util.parse(msisdn, countryCode);
                    String str2 = LOG_TAG;
                    IMSLog.s(str2, "impu: " + impuPhoneNumber + ", msisdn: " + msisdnPhoneNumber);
                    PhoneNumberUtil.MatchType match = util.isNumberMatch(impuPhoneNumber, msisdnPhoneNumber);
                    String str3 = LOG_TAG;
                    IMSLog.s(str3, "checkAvailableImpu: " + match);
                    if (match != PhoneNumberUtil.MatchType.NO_MATCH) {
                        return true;
                    }
                    return false;
                } catch (NumberParseException | NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public String getEmergencyImpu(List<String> impus) {
        Log.i(LOG_TAG, "getEmergencyImpu:");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        for (String impu : impus) {
            if (checkAvailableImpu(impu)) {
                return impu;
            }
        }
        return null;
    }

    public String getImpuFromList(List<String> impus) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        String impu = super.getImpuFromList(impus);
        if (impus.size() > 1 && !TextUtils.isEmpty(impus.get(1)) && impus.get(1).equals(impu) && checkAvailableImpu(impu)) {
            return impu;
        }
        if (!TextUtils.isEmpty(impus.get(0))) {
            return impus.get(0);
        }
        return null;
    }
}
