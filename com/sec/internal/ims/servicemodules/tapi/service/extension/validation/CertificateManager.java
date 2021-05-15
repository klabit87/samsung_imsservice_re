package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

class CertificateManager {
    private final List<X509Certificate> certificates = new ArrayList();
    private final List<X509CRL> crls = new ArrayList();

    public synchronized void addCert(X509Certificate cert) {
        this.certificates.add(cert);
    }

    public synchronized List<X509Certificate> getCertificates() {
        return this.certificates;
    }

    public synchronized void addCRL(X509CRL crl) {
        this.crls.add(crl);
    }
}
