package com.sec.internal.ims.util;

import android.content.Context;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorUs extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorUs";

    public UriGeneratorUs(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        super(preferredUri, countryCode, domain, tm, subId, phoneId, profile);
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    public void extractOwnAreaCode(String msisdn) {
        if (msisdn != null) {
            try {
                if (msisdn.startsWith("+1")) {
                    this.mOwnAreaCode = msisdn.substring(2, 5);
                } else if (msisdn.length() == 11) {
                    this.mOwnAreaCode = msisdn.substring(1, 4);
                } else if (msisdn.length() == 10) {
                    this.mOwnAreaCode = msisdn.substring(0, 3);
                }
            } catch (StringIndexOutOfBoundsException e) {
                this.mOwnAreaCode = "";
            }
        }
        Log.d(LOG_TAG, "extractOwnAreaCode: mOwnAreaCode=" + this.mOwnAreaCode);
    }

    public ImsUri getNormalizedUri(String number, boolean ignoreRoaming) {
        if (number == null) {
            return null;
        }
        if (number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            Log.d(LOG_TAG, "getNormalizedUri: invalid special character in number");
            return null;
        } else if (!ignoreRoaming && isRoaming() && !number.contains("*") && !number.contains("#")) {
            return UriUtil.parseNumber(number, getLocalCountryCode());
        } else {
            if (this.mOwnAreaCode == null) {
                extractOwnAreaCode(this.mTelephonyManager.getMsisdn(this.mSubscriptionId));
            }
            if (number.length() == 7 && this.mOwnAreaCode != null) {
                number = this.mOwnAreaCode + number;
                Log.d(LOG_TAG, "local number format, adding own area code " + IMSLog.checker(number));
            }
            if (this.mCountryCode != null && "mx".equalsIgnoreCase(this.mCountryCode) && !number.startsWith("+")) {
                number = "1" + number;
                Log.d(LOG_TAG, "getNormalizedUri: Added 1 for Mexico " + IMSLog.checker(number));
            }
            return UriUtil.parseNumber(number, this.mCountryCode);
        }
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, String number, String deviceId) {
        ImsUri uri = null;
        if (number != null) {
            uri = super.getNetworkPreferredUri(serviceType, number, deviceId);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    public ImsUri getNetworkPreferredUri(String number, String deviceId) {
        ImsUri uri = null;
        if (number != null) {
            uri = super.getNetworkPreferredUri(number, deviceId);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }
}
