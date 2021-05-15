package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

public class SignatureInfo {
    private final CertificateInfo entityCert;
    private final CertificateInfo rootCert;

    public SignatureInfo(CertificateInfo rootCert2, CertificateInfo entityCert2) {
        this.rootCert = rootCert2;
        this.entityCert = entityCert2;
    }

    public CertificateInfo getEntityCertificate() {
        return this.entityCert;
    }

    public String toString() {
        return "\n" + this.entityCert.toString() + this.rootCert.toString();
    }
}
