package com.sec.internal.ims.entitlement.util;

import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class CompleteXMLBlockExtractor {
    private static final String LOG_TAG = CompleteXMLBlockExtractor.class.getSimpleName();

    public static String getXmlBlockForElement(String deviceConfigXml, String xpathExpr) {
        try {
            return nodeToString((Node) XPathFactory.newInstance().newXPath().evaluate(xpathExpr, new InputSource(new ByteArrayInputStream(deviceConfigXml.getBytes())), XPathConstants.NODE));
        } catch (XPathExpressionException xppe) {
            String str = LOG_TAG;
            IMSLog.s(str, "XPath expression failed :" + xppe.getMessage());
            return null;
        }
    }

    private static String nodeToString(Node node) {
        StringWriter buf = new StringWriter();
        try {
            Transformer xform = TransformerFactory.newInstance().newTransformer();
            xform.setOutputProperty("omit-xml-declaration", "yes");
            xform.transform(new DOMSource(node), new StreamResult(buf));
        } catch (TransformerException te) {
            String str = LOG_TAG;
            IMSLog.s(str, "TransformerException: could not transform to string:" + te.getMessage());
        }
        return buf.toString();
    }
}
