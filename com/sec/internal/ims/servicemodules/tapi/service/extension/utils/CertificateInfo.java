package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

public class CertificateInfo {
    private final X509Certificate mCert;
    private String mUriIdentity;

    public X509Certificate getX509Certificate() {
        return this.mCert;
    }

    public String getURIIdentity() {
        return this.mUriIdentity;
    }

    public CertificateInfo(X509Certificate cert) {
        this.mCert = cert;
        loadSanData();
    }

    private void loadSanData() {
        try {
            Collection<List<?>> subjectAltNames = this.mCert.getSubjectAlternativeNames();
            if (subjectAltNames != null && !subjectAltNames.isEmpty() && subjectAltNames.iterator().next().get(0) != null && subjectAltNames.iterator().next().get(1) != null) {
                this.mUriIdentity = subjectAltNames.iterator().next().get(1).toString();
            }
        } catch (CertificateParsingException e) {
            e.printStackTrace();
        }
    }
}
