package com.sec.internal.ims.entitlement.storagehelper;

import android.content.Context;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.log.IMSLog;

public class NSDSHelper {
    private static final String LOG_TAG = NSDSHelper.class.getSimpleName();

    public static String getRealm(String mccmnc) {
        StringBuilder builder = new StringBuilder();
        int mcc = 310;
        int mnc = 310;
        try {
            mcc = Integer.parseInt(mccmnc.substring(0, 3));
            mnc = Integer.parseInt(mccmnc.substring(3));
        } catch (NumberFormatException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "exception " + e.getMessage());
        }
        builder.append("nai.epc.mnc");
        if (mnc < 100) {
            builder.append("0");
        }
        builder.append(mnc);
        builder.append(".mcc");
        builder.append(mcc);
        builder.append(UtUtils.DOMAIN_NAME);
        String realm = builder.toString();
        String str2 = LOG_TAG;
        IMSLog.s(str2, "getRealm: " + realm);
        return realm;
    }

    public static String getImsiEap(Context context, int slotId, String imsi, String mccmnc) {
        IMSLog.s(LOG_TAG, "getImsiEap: imsi " + imsi + " mccmnc " + mccmnc);
        if (TextUtils.isEmpty(imsi) || TextUtils.isEmpty(mccmnc)) {
            IMSLog.e(LOG_TAG, "getImsiEap: mccmnc null");
            return null;
        }
        String imsiEap = "0" + imsi + "@" + getRealm(mccmnc);
        String imsiEapFromPref = NSDSSharedPrefHelper.getPrefForSlot(context, slotId, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        if (imsiEapFromPref == null || !imsiEapFromPref.equals(imsiEap)) {
            IMSLog.i(LOG_TAG, "getImsiEap: imsi eap updated for slotId " + slotId);
            NSDSSharedPrefHelper.savePrefForSlot(context, slotId, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP, imsiEap);
        }
        IMSLog.s(LOG_TAG, "getImsiEap: " + imsiEap);
        return imsiEap;
    }

    public static String getVIMSIforSIMDevice(Context context, String imsi) {
        String isimDomain = TelephonyManagerWrapper.getInstance(context).getIsimDomain();
        String str = LOG_TAG;
        IMSLog.s(str, "getVIMSIforSIMDevice: IsimDomain " + isimDomain);
        if (TextUtils.isEmpty(isimDomain)) {
            return null;
        }
        return imsi + "@" + isimDomain;
    }

    public static String getMSISDNFromSIM(Context context, int slotId) {
        String msisdn = TelephonyManagerWrapper.getInstance(context).getMsisdn(slotId);
        if (TextUtils.isEmpty(msisdn)) {
            return null;
        }
        return msisdn;
    }

    public static String getAccessToken(Context context, String deviceUid) {
        return NSDSSharedPrefHelper.get(context, deviceUid, "access_token");
    }

    public static String getAccessTokenType(Context context, String deviceUid) {
        return NSDSSharedPrefHelper.get(context, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ACCESS_TOKEN_TYPE);
    }
}
