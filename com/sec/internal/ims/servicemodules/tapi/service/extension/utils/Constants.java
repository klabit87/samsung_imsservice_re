package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

public interface Constants {
    public static final String[] C14N_ALGORITHMS = {C14N_ALGORITHM_XML11, C14N_ALGORITHM_XML_LOCAL};
    public static final String C14N_ALGORITHM_XML11 = "http://www.w3.org/2006/12/xml-c14n11";
    public static final String C14N_ALGORITHM_XML_LOCAL = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public static final String DIGEST_ALGORITHM_SHA1 = "SHA-1";
    public static final String DIGEST_ALGORITHM_SHA224 = "SHA-224";
    public static final String DIGEST_ALGORITHM_SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String DIGITAL_SIGN_NS = "http://www.w3.org/2000/09/xmldsig#";
    public static final String IARI_AUTH_ELT = "iari-authorisation";
    public static final String IARI_AUTH_NS = "http://gsma.com/ns/iari-authorisation#";
    public static final String IARI_ELT = "iari";
    public static final String ID = "Id";
    public static final String PACKAGE_NAME_ELT = "package-name";
    public static final String PACKAGE_SIGNER_ELT = "package-signer";
    public static final String SELF_SIGNED_IARI_PREFIX = "urn:urn-7:3gpp-application.ims.iari.rcs.ext.ss.";
    public static final String SIGNATURE_ELT = "Signature";
    public static final String SIG_ALGORITHM_RSAwithSHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String SIG_PROPERTY_IDENTIFIER_NAME = "Identifier";
    public static final String SIG_PROPERTY_NS = "http://www.w3.org/2009/xmldsig-properties";
    public static final String SIG_PROPERTY_PROFILE_NAME = "Profile";
    public static final String SIG_PROPERTY_PROFILE_URI = "http://gsma.com/ns/iari-authorisation-profile";
    public static final String SIG_PROPERTY_ROLE_NAME = "Role";
    public static final String SIG_PROPERTY_ROLE_SELF_SIGNED = "http://gsma.com/ns/iari-authorisation-role-standalone";
    public static final String SIG_PROPERTY_URI_NAME = "URI";
    public static final String UTF8 = "UTF-8";
}
