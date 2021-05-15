package com.sec.internal.constants.ims.servicemodules.im;

import java.io.ByteArrayInputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ChatbotXmlUtils {
    private static ChatbotXmlUtils sInstance;
    private XPathExpression mCommandIdPath;
    private DocumentBuilder mDocumentBuilder;
    private XPath mXpath;

    private ChatbotXmlUtils() {
    }

    public static synchronized ChatbotXmlUtils getInstance() {
        ChatbotXmlUtils chatbotXmlUtils;
        synchronized (ChatbotXmlUtils.class) {
            if (sInstance == null) {
                sInstance = new ChatbotXmlUtils();
            }
            chatbotXmlUtils = sInstance;
        }
        return chatbotXmlUtils;
    }

    public String composeAnonymizeXml(String action, String commandId) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<AM" + " xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:aliasmgmt\"" + ">\n" + "\t<Command-ID>" + commandId + "</Command-ID>\n" + "\t<action>" + action + "</action>\n" + "</AM>\n";
    }

    public String composeSpamXml(String uri, List<String> messageIds, String spamType, String freeText) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("\t<SR");
        sb.append(" xmlns=\"urn:gsma:params:xml:ns:rcs:rcs:spamreport\"");
        sb.append(">\n");
        sb.append("\t\t<Chatbot>");
        sb.append(uri);
        sb.append("</Chatbot>\n");
        int count = 0;
        for (String messageId : messageIds) {
            if (messageId != null && !messageId.isEmpty()) {
                if (count >= 10) {
                    break;
                }
                sb.append("\t\t<Message-ID>");
                sb.append(messageId);
                sb.append("</Message-ID>\n");
                count++;
            }
        }
        if (spamType != null) {
            sb.append("\t\t<spam-type>");
            sb.append(spamType);
            sb.append("</spam-type>\n");
        }
        if (freeText != null) {
            String escapedFreeText = freeText.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;").replace("'", "&apos;");
            sb.append("\t\t<free-text>");
            sb.append(escapedFreeText);
            sb.append("</free-text>\n");
        }
        sb.append("</SR>\n");
        return sb.toString();
    }

    public String parseXml(String resultXml, String id) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        this.mXpath = XPathFactory.newInstance().newXPath();
        try {
            this.mDocumentBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mCommandIdPath = createXpathLazy(id);
        return extractString(this.mCommandIdPath, this.mDocumentBuilder.parse(new ByteArrayInputStream(resultXml.getBytes("utf-8"))));
    }

    private XPathExpression createXpathLazy(String xpath) {
        try {
            return this.mXpath.compile(xpath);
        } catch (XPathExpressionException e) {
            throw new Error(e);
        }
    }

    private static String extractString(XPathExpression expressionLazy, Document context) throws XPathExpressionException {
        Node node = (Node) expressionLazy.evaluate(context, XPathConstants.NODE);
        if (node != null) {
            return node.getTextContent();
        }
        return null;
    }
}
