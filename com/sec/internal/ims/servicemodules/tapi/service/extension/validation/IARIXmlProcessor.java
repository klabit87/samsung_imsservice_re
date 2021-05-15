package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.CertificateInfo;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.ValidationHelper;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.w3c.dom.Element;

class IARIXmlProcessor {
    private static final String LOG_TAG = IARIXmlProcessor.class.getSimpleName();
    private IARIXmlParser authDocument;
    private int status = -1;

    IARIXmlProcessor() {
    }

    public void parseAuthDoc(InputStream is) {
        IARIXmlParser iARIXmlParser = new IARIXmlParser();
        this.authDocument = iARIXmlParser;
        if (iARIXmlParser.parse(is)) {
            this.status = 0;
        }
    }

    public void process() {
        if (this.authDocument == null) {
            Log.d(LOG_TAG, "process: auth doc is null");
            this.status = -1;
            return;
        }
        Set<String> expectedRefs = new HashSet<>();
        expectedRefs.add(this.authDocument.getIariNode().getAttribute(Constants.ID));
        expectedRefs.add(this.authDocument.getPackageSignerNode().getAttribute(Constants.ID));
        Element packageNameNode = this.authDocument.getPackageNameNode();
        if (packageNameNode != null) {
            expectedRefs.add(packageNameNode.getAttribute(Constants.ID));
        }
        int validateCertificateOtherProperties = validateCertificateOtherProperties();
        this.status = validateCertificateOtherProperties;
        if (validateCertificateOtherProperties == 0) {
            this.status = 0;
        }
    }

    public int getStatus() {
        return this.status;
    }

    public IARIXmlParser getAuthDocument() {
        return this.authDocument;
    }

    private int validateCertificateOtherProperties() {
        Log.d(LOG_TAG, "validateCertificateOtherProperties");
        CertificateInfo entityCert = this.authDocument.getSignature().getEntityCertificate();
        if (!this.authDocument.getIari().equals(entityCert.getURIIdentity())) {
            this.status = 1;
            return 1;
        } else if (!this.authDocument.getIari().startsWith(Constants.SELF_SIGNED_IARI_PREFIX)) {
            this.status = 1;
            return 1;
        } else {
            if (!this.authDocument.getIari().substring(Constants.SELF_SIGNED_IARI_PREFIX.length()).equals(ValidationHelper.hash(entityCert.getX509Certificate().getPublicKey().getEncoded()))) {
                Log.d(LOG_TAG, "Requested IARI key-specific part does not match signing key");
                this.status = 1;
                return 1;
            }
            this.status = 0;
            return 0;
        }
    }
}
