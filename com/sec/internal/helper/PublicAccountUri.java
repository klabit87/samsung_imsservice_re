package com.sec.internal.helper;

import com.sec.ims.util.ImsUri;

public class PublicAccountUri {
    private static String mCountryCode;
    private static String publicAccountDomain;

    public static void setPublicAccountDomain(String domain) {
        publicAccountDomain = domain;
    }

    public static void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    public static ImsUri convertToPublicAccountUri(String uri) {
        if (uri == null) {
            return null;
        }
        String domain = publicAccountDomain;
        String[] tempUri = uri.split(":");
        if (tempUri.length <= 1) {
            return null;
        }
        String[] tempUriPart = tempUri[1].split("@");
        if (tempUriPart.length > 1) {
            if (tempUriPart[0].startsWith("+86")) {
                return ImsUri.parse(tempUri[0] + ":" + tempUriPart[0].substring(3) + "@" + domain);
            }
            return ImsUri.parse(tempUri[0] + ":" + tempUriPart[0] + "@" + domain);
        } else if (tempUri[1].startsWith("+86")) {
            return ImsUri.parse("sip:" + tempUri[1].substring(3) + "@" + domain);
        } else {
            return ImsUri.parse("sip:" + tempUri[1] + "@" + domain);
        }
    }

    public static boolean isPublicAccountUri(ImsUri uri) {
        String str;
        if (uri.toString().startsWith("sip:+8612520")) {
            return true;
        }
        if (!uri.toString().startsWith("sip:12520") || (str = mCountryCode) == null || !"cn".equalsIgnoreCase(str)) {
            return false;
        }
        return true;
    }
}
