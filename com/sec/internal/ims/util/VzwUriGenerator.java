package com.sec.internal.ims.util;

import android.content.Context;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.log.IMSLog;

public class VzwUriGenerator extends UriGeneratorUs {
    private static final String LOG_TAG = "VzwUriGenerator";

    public VzwUriGenerator(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        super(context, preferredUri, countryCode, domain, tm, subId, phoneId, profile);
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    public ImsUri normalize(ImsUri uri) {
        return super.normalize(uri);
    }

    public ImsUri getNormalizedUri(String number, boolean ignoreRoaming) {
        if (number == null) {
            return null;
        }
        if (number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            Log.d(LOG_TAG, "getNormalizedUri: invalid special character in number");
            return null;
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

    public ImsUri getNetworkPreferredUri(ImsUri uri) {
        if (this.mUriType == uri.getUriType()) {
            return uri;
        }
        if (this.mUriType != ImsUri.UriType.SIP_URI) {
            return convertToTelUri(uri, this.mCountryCode);
        }
        if ("sip".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        String number = uri.getMsisdn();
        if (number == null) {
            return null;
        }
        return ImsUri.parse("sip:" + number + "@" + this.mDomain + ";user=phone");
    }
}
