package com.sec.internal.ims.servicemodules.tapi.service.extension.validation;

import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.Constants;
import com.sec.internal.ims.servicemodules.tapi.service.extension.utils.SignatureInfo;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IARIXmlParser {
    private static final String LOG_TAG = IARIXmlParser.class.getSimpleName();
    private Element iariNode;
    private final AuthType mAuthType = AuthType.SELF_SIGNED;
    private String mIari;
    private String mPackageName;
    private String mPackageSigner;
    private SignatureInfo mSignature;
    private Element packageNameNode;
    private Element packageSignerNode;
    private Element signatureNode;

    public enum AuthType {
        SELF_SIGNED
    }

    public Element getIariNode() {
        return this.iariNode;
    }

    public Element getPackageNameNode() {
        return this.packageNameNode;
    }

    public Element getPackageSignerNode() {
        return this.packageSignerNode;
    }

    public Element getSignatureNode() {
        return this.signatureNode;
    }

    public String getIari() {
        return this.mIari;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getPackageSigner() {
        return this.mPackageSigner;
    }

    public SignatureInfo getSignature() {
        return this.mSignature;
    }

    public void setSignature(SignatureInfo signature) {
        this.mSignature = signature;
    }

    public boolean parse(InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            Document doc = dbf.newDocumentBuilder().parse(is);
            String encoding = doc.getXmlEncoding();
            if (encoding != null && !encoding.equalsIgnoreCase("UTF-8")) {
                return printErrorMessage("Invalid IARI xml: iari-authorization is not encoded with UTF-8");
            }
            NodeList authElements = doc.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.IARI_AUTH_ELT);
            if (authElements.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of iari-authorization elements");
            }
            Element authElement = (Element) authElements.item(0);
            if (authElement != doc.getDocumentElement()) {
                return printErrorMessage("Invalid IARI xml: iari-authorization is not the root element");
            }
            NodeList iariElements = doc.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.IARI_ELT);
            if (iariElements.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of iari elements");
            }
            Element element = (Element) iariElements.item(0);
            this.iariNode = element;
            if (element.getParentNode() != authElement) {
                return printErrorMessage("Invalid IARI xml: iari must be a child of iari-authorization");
            }
            this.iariNode.setIdAttribute(Constants.ID, true);
            this.mIari = this.iariNode.getTextContent();
            NodeList nameElements = doc.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.PACKAGE_NAME_ELT);
            int nameElementCount = nameElements.getLength();
            if (nameElementCount > 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of package-name elements");
            }
            if (nameElementCount == 1) {
                Element element2 = (Element) nameElements.item(0);
                this.packageNameNode = element2;
                if (element2.getParentNode() != authElement) {
                    return printErrorMessage("Invalid IARI xml: package-name must be a child of iari-authorization");
                }
                this.packageNameNode.setIdAttribute(Constants.ID, true);
                this.mPackageName = this.packageNameNode.getTextContent();
            }
            NodeList signerElements = doc.getElementsByTagNameNS(Constants.IARI_AUTH_NS, Constants.PACKAGE_SIGNER_ELT);
            if (signerElements.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of package-signer elements");
            }
            Element element3 = (Element) signerElements.item(0);
            this.packageSignerNode = element3;
            if (element3.getParentNode() != authElement) {
                return printErrorMessage("Invalid IARI xml: package-signer must be a child of iari-authorization");
            }
            this.packageSignerNode.setIdAttribute(Constants.ID, true);
            this.mPackageSigner = this.packageSignerNode.getTextContent();
            NodeList signatureElements = doc.getElementsByTagNameNS(Constants.DIGITAL_SIGN_NS, Constants.SIGNATURE_ELT);
            if (signatureElements.getLength() != 1) {
                return printErrorMessage("Invalid IARI xml: invalid number of ds:Signature elements");
            }
            Element element4 = (Element) signatureElements.item(0);
            this.signatureNode = element4;
            if (element4.getParentNode() == authElement || printErrorMessage("Invalid IARI xml: signature node must be a child of iari-authorization")) {
                return true;
            }
            return false;
        } catch (ParserConfigurationException | SAXException e) {
            return printErrorMessage("Unexpected exception parsing IARI xml:" + e.getLocalizedMessage());
        } catch (IOException e2) {
            return printErrorMessage("Unexpected exception reading IARI xml: " + e2.getLocalizedMessage());
        }
    }

    private boolean printErrorMessage(String errorMessage) {
        String str = LOG_TAG;
        Log.d(str, "iari xml parse error : " + errorMessage);
        return false;
    }

    public String toString() {
        StringBuilder details = new StringBuilder();
        details.append("authType=");
        details.append(this.mAuthType.name());
        details.append(10);
        if (this.mIari != null) {
            details.append("iari=");
            details.append(this.mIari);
            details.append(10);
        }
        if (this.mPackageName != null) {
            details.append("packageName=");
            details.append(this.mPackageName);
            details.append(10);
        }
        if (this.mPackageSigner != null) {
            details.append("packageSigner=");
            details.append(this.mPackageSigner);
            details.append(10);
        }
        SignatureInfo signatureInfo = this.mSignature;
        if (signatureInfo != null) {
            details.append(signatureInfo);
            details.append(10);
        }
        return details.toString();
    }
}
