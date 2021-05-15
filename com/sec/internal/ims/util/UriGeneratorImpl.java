package com.sec.internal.ims.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UriGeneratorImpl extends UriGenerator {
    private static final String LOG_TAG = "UriGenerator";
    protected String mCountryCode;
    protected String mDomain;
    protected String mOwnAreaCode;
    protected int mPhoneId;
    protected ImsProfile mProfile;
    protected int mRat;
    protected ImsUri.UriType mRcsUriType;
    protected int mSubscriptionId;
    protected ITelephonyManager mTelephonyManager;
    protected ImsUri.UriType mUriType;
    protected ImsUri.UriType mVoLTEUriType;

    public UriGeneratorImpl(ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId, ImsProfile profile) {
        this.mTelephonyManager = null;
        this.mSubscriptionId = 0;
        this.mPhoneId = 0;
        this.mRat = 0;
        this.mTelephonyManager = tm;
        this.mSubscriptionId = subId;
        this.mUriType = preferredUri;
        this.mCountryCode = countryCode;
        this.mDomain = domain;
        this.mPhoneId = phoneId;
        this.mProfile = profile;
        Log.d(LOG_TAG, "mDomain " + this.mDomain);
    }

    public UriGeneratorImpl(Context context, ImsUri.UriType preferredUri, String countryCode, String domain, ITelephonyManager tm, int subId, int phoneId) {
        this(preferredUri, countryCode, domain, tm, subId, phoneId, (ImsProfile) null);
    }

    public ImsUri normalize(ImsUri uri) {
        IMSLog.s(LOG_TAG, "normalize " + uri);
        if (uri == null) {
            Log.d(LOG_TAG, "normalize: uri is null");
            return null;
        } else if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
            Log.d(LOG_TAG, "Do not normalize chatbot service ID");
            return uri;
        } else if (uri.getUriType() != ImsUri.UriType.SIP_URI || uri.toString().contains("user=phone") || !ChatbotUriUtil.isKnownBotServiceId(uri)) {
            ImsUri normalized = convertToTelUri(uri, this.mCountryCode);
            if (normalized == null) {
                return uri;
            }
            normalized.setUserParam(PhoneConstants.PHONE_KEY);
            return normalized;
        } else {
            IMSLog.s(LOG_TAG, "Service Id exists in mBotServiceIdMap, so don't normalize it.");
            return uri;
        }
    }

    public ImsUri getNormalizedUri(String number) {
        return getNormalizedUri(number, false);
    }

    public ImsUri getNormalizedUri(String number, boolean ignoreRoaming) {
        String countryCode = this.mCountryCode;
        if (number == null) {
            return null;
        }
        if (number.contains("#") || number.contains("*") || number.contains(",") || number.contains("N")) {
            Log.d(LOG_TAG, "getNormalizedUri: invalid special character in number");
            return null;
        } else if (isRoaming() && !ignoreRoaming) {
            return UriUtil.parseNumber(number, getLocalCountryCode());
        } else {
            if (number.startsWith("+")) {
                return ImsUri.parse("tel:" + number);
            }
            if (isRoaming() && !ignoreRoaming) {
                countryCode = getLocalCountryCode();
            }
            if (number.length() == 7) {
                if (this.mOwnAreaCode == null) {
                    extractOwnAreaCode(this.mTelephonyManager.getMsisdn(this.mSubscriptionId));
                }
                if (this.mOwnAreaCode != null) {
                    number = this.mOwnAreaCode + number;
                    Log.d(LOG_TAG, "local number format, adding own area code " + IMSLog.checker(number));
                }
            }
            return UriUtil.parseNumber(number, countryCode);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRoaming() {
        if (!this.mTelephonyManager.isNetworkRoaming()) {
            return false;
        }
        return !this.mCountryCode.equals(getLocalCountryCode());
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    /* access modifiers changed from: protected */
    public String getLocalCountryCode() {
        return this.mTelephonyManager.getNetworkCountryIso();
    }

    public ImsUri swapUriType(ImsUri uri) {
        IMSLog.s(LOG_TAG, "swapUriType: [input: " + uri + " network preferred type: " + this.mUriType + "]");
        if (uri.getUriType() == ImsUri.UriType.SIP_URI) {
            return convertToTelUri(uri, this.mCountryCode);
        }
        return convertToSipUri(uri, this.mDomain);
    }

    public Set<ImsUri> swapUriType(List<ImsUri> uris) {
        Set<ImsUri> ret = new HashSet<>();
        for (ImsUri uri : uris) {
            ret.add(swapUriType(uri));
        }
        return ret;
    }

    public ImsUri getNetworkPreferredUri(ImsUri uri) {
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: [input: " + uri + " network preferred type: " + this.mUriType + "]");
        if (uri == null) {
            Log.d(LOG_TAG, "uri is null");
            return null;
        } else if (this.mUriType == uri.getUriType()) {
            return uri;
        } else {
            return getNetworkPreferredUri(uri.getMsisdn(), (String) null);
        }
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, ImsUri uri) {
        return getNetworkPreferredUri(serviceType, uri, this.mDomain);
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, ImsUri uri, String domain) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: input URI: " + uri);
        if (uri == null) {
            Log.d(LOG_TAG, "uri is null");
            return null;
        } else if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
            Log.d(LOG_TAG, "Do not normalize chatbot service ID");
            return uri;
        } else {
            if (serviceType == UriGenerator.URIServiceType.VOLTE_URI) {
                uriType = this.mVoLTEUriType;
            } else if (serviceType == UriGenerator.URIServiceType.RCS_URI) {
                uriType = this.mRcsUriType;
            } else {
                uriType = this.mUriType;
            }
            Log.d(LOG_TAG, "URI type: " + uriType);
            if (uriType == uri.getUriType()) {
                return uri;
            }
            return getNetworkPreferredUriInternal(uri.getMsisdn(), (String) null, uriType, domain, serviceType);
        }
    }

    public Set<ImsUri> getNetworkPreferredUri(Set<ImsUri> uris) {
        Set<ImsUri> ret = new HashSet<>();
        for (ImsUri uri : uris) {
            ret.add(getNetworkPreferredUri(uri));
        }
        return ret;
    }

    public Set<ImsUri> getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, Set<ImsUri> uris) {
        Set<ImsUri> ret = new HashSet<>();
        for (ImsUri uri : uris) {
            if (ChatbotUriUtil.hasUriBotPlatform(uri)) {
                ret.add(uri);
            } else {
                ret.add(getNetworkPreferredUri(serviceType, uri.getMsisdn(), (String) null));
            }
        }
        return ret;
    }

    public ImsUri getNetworkPreferredUri(UriGenerator.URIServiceType serviceType, String number, String deviceId) {
        ImsUri.UriType uriType;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: URIServiceType : " + serviceType);
        if (serviceType == UriGenerator.URIServiceType.VOLTE_URI) {
            uriType = this.mVoLTEUriType;
        } else if (serviceType == UriGenerator.URIServiceType.RCS_URI) {
            uriType = this.mRcsUriType;
        } else {
            uriType = this.mUriType;
        }
        return getNetworkPreferredUriInternal(number, deviceId, uriType, serviceType);
    }

    public ImsUri getNetworkPreferredUri(String number, String deviceId) {
        return getNetowkrPreferredUri(number, deviceId, this.mDomain);
    }

    public ImsUri getNetowkrPreferredUri(String number, String deviceId, String domain) {
        return getNetworkPreferredUriInternal(number, deviceId, this.mUriType, domain);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String number, String deviceId, ImsUri.UriType uriType, UriGenerator.URIServiceType serviceType) {
        return getNetworkPreferredUriInternal(number, deviceId, uriType, this.mDomain, serviceType);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String number, String deviceId, ImsUri.UriType uriType, String domain) {
        return getNetworkPreferredUriInternal(number, deviceId, uriType, domain, (UriGenerator.URIServiceType) null);
    }

    /* access modifiers changed from: protected */
    public ImsUri getNetworkPreferredUriInternal(String number, String deviceId, ImsUri.UriType uriType, String domain, UriGenerator.URIServiceType serviceType) {
        ImsUri uri;
        String domainToUse = (domain == null || domain.isEmpty()) ? this.mDomain : domain;
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: mDomain : " + domainToUse + ", uriType : " + uriType);
        if (isLocalNumber(number) && !DeviceUtil.getGcfMode()) {
            String plmn = null;
            if (this.mProfile != null && serviceType == UriGenerator.URIServiceType.VOLTE_URI && this.mRat == 13 && isRoaming() && "geo-local".equals(this.mProfile.getPolicyOnLocalNumbers())) {
                plmn = this.mTelephonyManager.getNetworkOperator(this.mSubscriptionId);
            }
            if (TextUtils.isEmpty(plmn) || plmn.length() <= 4) {
                number = number + ";phone-context=" + domainToUse;
            } else {
                number = number + ";phone-context=" + plmn.substring(0, 3) + "." + plmn.substring(3) + ".eps." + domainToUse;
            }
        }
        if (uriType == ImsUri.UriType.TEL_URI) {
            uri = ImsUri.parse("tel:" + number);
        } else {
            uri = getSipUri(number, domainToUse, deviceId);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    /* access modifiers changed from: protected */
    public ImsUri getSipUri(String number, String domain, String deviceId) {
        ImsUri uri = ImsUri.parse("sip:" + number + "@" + domain);
        if (uri != null) {
            uri.setUserParam(PhoneConstants.PHONE_KEY);
            if (!TextUtils.isEmpty(deviceId)) {
                if (deviceId.startsWith("urn:")) {
                    uri.setParam("gr", deviceId);
                } else if (deviceId.length() == 15) {
                    String tac = deviceId.substring(0, 8);
                    String snr = deviceId.substring(8, 14);
                    String spare = deviceId.substring(14);
                    uri.setParam("gr", "urn:gsma:imei:" + tac + "-" + snr + "-" + spare);
                } else {
                    uri.setParam("gr", "urn:gsma:imei:" + deviceId);
                }
            }
        }
        return uri;
    }

    public ImsUri getNetworkPreferredUri(String number) {
        ImsUri uri;
        if (this.mUriType == ImsUri.UriType.TEL_URI) {
            uri = ImsUri.parse("tel:" + number);
        } else {
            uri = ImsUri.parse("sip:" + number + "@" + this.mDomain);
            if (uri != null) {
                uri.setUserParam(PhoneConstants.PHONE_KEY);
            }
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri: " + uri);
        return uri;
    }

    public ImsUri getNetworkPreferredUri(ImsUri.UriType uriType, String number) {
        ImsUri uri;
        if (uriType == ImsUri.UriType.TEL_URI) {
            uri = ImsUri.parse("tel:" + number);
        } else {
            uri = ImsUri.parse("sip:" + number + "@" + this.mDomain);
        }
        IMSLog.s(LOG_TAG, "getNetworkPreferredUri with URI type: " + uri);
        return uri;
    }

    public void extractOwnAreaCode(String msisdn) {
        IMSLog.d(LOG_TAG, "Area code available for US operator only");
    }

    public ImsUri getUssdRuri(String number) {
        ImsUri uri;
        if (this.mVoLTEUriType == ImsUri.UriType.TEL_URI) {
            uri = ImsUri.parse("tel:" + number + ";phone-context=" + this.mDomain);
        } else {
            uri = ImsUri.parse("sip:" + number + ";phone-context=" + this.mDomain + "@" + this.mDomain);
        }
        if (uri != null) {
            uri.setUserParam("dialstring");
        }
        return uri;
    }

    public void updateNetworkPreferredUriType(UriGenerator.URIServiceType serviceType, ImsUri.UriType uriType) {
        if (serviceType == UriGenerator.URIServiceType.VOLTE_URI) {
            this.mVoLTEUriType = uriType;
        } else if (serviceType == UriGenerator.URIServiceType.RCS_URI) {
            this.mRcsUriType = uriType;
        }
    }

    public void updateRat(int rat) {
        this.mRat = rat;
    }

    /* access modifiers changed from: protected */
    public ImsUri convertToTelUri(ImsUri uri, String cc) {
        IMSLog.s(LOG_TAG, "convert input: " + uri + " cc: " + cc);
        PublicAccountUri.setCountryCode(cc);
        if (uri == null) {
            return null;
        }
        if (uri.getUriType() == ImsUri.UriType.TEL_URI) {
            return uri;
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

    /* access modifiers changed from: protected */
    public ImsUri convertToSipUri(ImsUri uri, String domain) {
        Log.d(LOG_TAG, "convertToSipUri input: " + uri + " domain: " + domain);
        if ("sip".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        String number = uri.getMsisdn();
        if (number == null) {
            return null;
        }
        if (isLocalNumber(number)) {
            number = number + ";phone-context=" + domain;
        }
        return ImsUri.parse("sip:" + number + "@" + domain + ";user=phone");
    }

    /* access modifiers changed from: protected */
    public boolean isLocalNumber(String number) {
        return !number.startsWith("+");
    }

    /* access modifiers changed from: protected */
    public boolean isSipNumber(String number) {
        if (number.lastIndexOf("+") > 0) {
            return true;
        }
        return false;
    }
}
