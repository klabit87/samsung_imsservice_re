package com.sec.internal.constants.ims;

import android.util.Log;
import com.sec.internal.constants.ims.XmlElement;

public final class XmlCreator {
    private static final String LOG_TAG = XmlCreator.class.getSimpleName();

    private XmlCreator() {
    }

    public static String toXml(XmlElement element) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + getElementDfs(element, (String) null);
    }

    public static String toXml(XmlElement element, String version, String charset) {
        return ("<?xml version=\"" + version + "\" encoding=\"" + charset + "\"?>") + getElementDfs(element, (String) null);
    }

    public static String toXcapXml(XmlElement element) {
        return "" + getElementDfs(element, (String) null);
    }

    public static String getElementDfs(XmlElement element, String parentNs) {
        String tag;
        StringBuilder xml = new StringBuilder();
        String ns = element.mNamespace;
        if (ns == null) {
            ns = parentNs;
        }
        if (element.mName != null) {
            if (ns != null) {
                tag = ns + ":" + element.mName;
            } else {
                tag = element.mName;
            }
            xml.append("<");
            xml.append(tag);
            for (XmlElement.Attribute attr : element.mAttributes) {
                xml.append(" ");
                if (attr.mNamespace != null) {
                    xml.append(attr.mNamespace);
                    xml.append(":");
                }
                xml.append(attr.mName);
                xml.append("=\"");
                xml.append(attr.mValue);
                xml.append("\"");
            }
            if (element.mValue == null && element.mChildElements.size() == 0) {
                xml.append("/>");
                return xml.toString();
            }
            xml.append(">");
            if (element.mValue != null) {
                xml.append(element.mValue);
            }
            if (element.mChildElements.size() != 0) {
                for (XmlElement childElement : element.mChildElements) {
                    xml.append(getElementDfs(childElement, ns));
                }
            }
            xml.append("</");
            xml.append(tag);
            xml.append(">");
            return xml.toString();
        }
        Log.e(LOG_TAG, "getElementDfs: element name is required ");
        return xml.toString();
    }
}
