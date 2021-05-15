package com.sec.internal.ims.util;

import android.content.Context;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;

public class UriGeneratorKr extends UriGeneratorImpl {
    private static final String LOG_TAG = "UriGeneratorKr";
    private String mMnoName = Mno.DEFAULT.getName();

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UriGeneratorKr(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        super(preferredUri, countryCode, domain, tm, subId, phoneId, profile);
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri uri, String cc) {
        IMSLog.s(LOG_TAG, "kr convert input: " + uri + " cc: " + cc);
        PublicAccountUri.setCountryCode(cc);
        if (uri == null) {
            return null;
        }
        if (uri.getUriType() == ImsUri.UriType.TEL_URI) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(uri), cc);
        }
        if (!UriUtil.hasMsisdnNumber(uri)) {
            IMSLog.s(LOG_TAG, "non Tel-URI convertible uri " + uri);
            return null;
        } else if (!PublicAccountUri.isPublicAccountUri(uri)) {
            return UriUtil.parseNumber(UriUtil.getMsisdnNumber(uri), cc);
        } else {
            return ImsUri.parse("tel:" + UriUtil.getMsisdnNumber(uri));
        }
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, String number, String deviceId) {
        ImsUri.UriType uriType;
        Mno mno = Mno.fromName(this.mMnoName);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain " + this.mDomain);
        if ((mno != Mno.KT || !isRoaming() || serviceType == UriGenerator.URIServiceType.RCS_URI) && (mno != Mno.SKT || !isSipNumber(number))) {
            if (serviceType == UriGenerator.URIServiceType.VOLTE_URI) {
                uriType = this.mVoLTEUriType;
            } else if (serviceType == UriGenerator.URIServiceType.RCS_URI) {
                uriType = this.mRcsUriType;
            } else {
                uriType = this.mUriType;
            }
            return super.getNetworkPreferredUriInternal(number, deviceId, uriType, serviceType);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: KOR SIP URI");
        if (mno == Mno.KT && isLocalNumber(number)) {
            number = number + ";phone-context=geo-local." + this.mDomain;
        }
        ImsUri uri = getSipUri(number, this.mDomain + ";user=phone", deviceId);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    public ImsUri getNetworkPreferredUri(String number, String deviceId) {
        Mno mno = Mno.fromName(this.mMnoName);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain " + this.mDomain);
        if ((mno != Mno.KT || !isRoaming()) && (mno != Mno.SKT || !isSipNumber(number))) {
            return super.getNetworkPreferredUri(number, deviceId);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: KOR SIP URI");
        if (mno == Mno.KT && isLocalNumber(number)) {
            number = number + ";phone-context=geo-local." + this.mDomain;
        }
        ImsUri uri = getSipUri(number, this.mDomain + ";user=phone", deviceId);
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    public void extractOwnAreaCode(String msisdn) {
        IMSLog.d(LOG_TAG, "extractOwnAreaCode: KOR operator not use OwnAreaCode");
        this.mOwnAreaCode = "";
    }

    /* access modifiers changed from: protected */
    public void setMnoName(String mnoName) {
        this.mMnoName = mnoName;
    }
}
