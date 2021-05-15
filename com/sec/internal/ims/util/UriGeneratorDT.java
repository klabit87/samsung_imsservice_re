package com.sec.internal.ims.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorDT extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorDT";
    protected String mPhoneContext;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UriGeneratorDT(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, String phonecontext, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        super(preferredUri, countryCode, domain, tm, subId, phoneId, profile);
        this.mPhoneContext = phonecontext;
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String number, String deviceId, ImsUri.UriType uriType, String domain) {
        return getNetworkPreferredUriInternal(number, deviceId, uriType, domain, (UriGenerator.URIServiceType) null);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String number, String deviceId, ImsUri.UriType uriType, UriGenerator.URIServiceType serviceType) {
        ImsUri uri;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain " + this.mDomain);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mPhoneContext " + this.mPhoneContext);
        if (isLocalNumber(number) && !DeviceUtil.getGcfMode()) {
            String plmn = null;
            if (this.mProfile != null && serviceType == UriGenerator.URIServiceType.VOLTE_URI && this.mRat == 13 && isRoaming() && "geo-local".equals(this.mProfile.getPolicyOnLocalNumbers())) {
                plmn = this.mTelephonyManager.getNetworkOperator(this.mSubscriptionId);
            }
            if (TextUtils.isEmpty(plmn) || plmn.length() <= 4) {
                number = number + ";phone-context=" + this.mPhoneContext;
            } else {
                number = number + ";phone-context=" + plmn.substring(0, 3) + "." + plmn.substring(3) + ".eps." + this.mPhoneContext;
            }
        }
        if (uriType == ImsUri.UriType.TEL_URI) {
            uri = ImsUri.parse("tel:" + number);
        } else {
            uri = getSipUri(number, this.mDomain, deviceId);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    /* access modifiers changed from: protected */
    public boolean isLocalNumber(String number) {
        return !number.startsWith("+");
    }
}
