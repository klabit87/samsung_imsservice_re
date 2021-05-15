package com.sec.internal.ims.util;

import android.content.Context;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorRjil extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorRjil";

    public UriGeneratorRjil(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        super(preferredUri, countryCode, domain, tm, subId, phoneId, profile);
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, String number, String deviceId) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: URIServiceType : " + serviceType);
        if (serviceType == UriGenerator.URIServiceType.VOLTE_URI) {
            uriType = ImsUri.UriType.TEL_URI;
        } else if (serviceType == UriGenerator.URIServiceType.RCS_URI) {
            uriType = this.mRcsUriType;
        } else {
            uriType = this.mUriType;
        }
        if (!(uriType == ImsUri.UriType.SIP_URI || uriType == ImsUri.UriType.TEL_URI)) {
            uriType = this.mUriType;
        }
        return getNetworkPreferredUriInternal(number, deviceId, uriType, serviceType);
    }

    public ImsUri getNetworkPreferredUri(ImsUri.UriType uriType, String number) {
        ImsUri uri;
        if (uriType == ImsUri.UriType.SIP_URI) {
            uri = ImsUri.parse("sip:" + number + "@" + this.mDomain);
        } else {
            uri = ImsUri.parse("tel:" + number);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri with URI type: " + uri);
        return uri;
    }

    public ImsUri getNormalizedUri(String number, boolean ignoreRoaming) {
        String phoneNumber = number;
        if (number == null) {
            return null;
        }
        if (number.contains(";phone-context")) {
            phoneNumber = number.substring(0, number.indexOf(";phone-context"));
        }
        if (!this.mCountryCode.equalsIgnoreCase("in") || phoneNumber.length() != 12 || phoneNumber.startsWith("+") || !phoneNumber.startsWith("91")) {
            return super.getNormalizedUri(number, ignoreRoaming);
        }
        return ImsUri.parse("tel:" + phoneNumber + ";phone-context=" + this.mDomain);
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri uri, String cc) {
        IMSLog.s(LOG_TAG, "convert input: " + uri + " cc: " + cc);
        if (uri.getUriType() != ImsUri.UriType.TEL_URI && UriUtil.hasMsisdnNumber(uri)) {
            String msisdn = UriUtil.getMsisdnNumber(uri);
            String phoneNumber = msisdn;
            if (msisdn != null && msisdn.contains("phone-context")) {
                phoneNumber = msisdn.substring(0, msisdn.indexOf(";phone-context"));
            }
            if (this.mCountryCode.equalsIgnoreCase("in") && phoneNumber.length() == 12 && !phoneNumber.startsWith("+") && phoneNumber.startsWith("91")) {
                return ImsUri.parse("tel:" + phoneNumber + ";phone-context=" + this.mDomain);
            }
        }
        return super.convertToTelUri(uri, cc);
    }
}
