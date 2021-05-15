package com.sec.internal.ims.servicemodules.ss;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.ims.servicemodules.ss.CallBarringData;
import com.sec.internal.ims.servicemodules.ss.CallForwardingData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UtXmlParse {
    public static final String LOG_TAG = "UtXmlParse";
    private XPathExpression mDefaultBehavior;
    private DocumentBuilder mDocumenetbuilder;
    private XPathExpression mErrorPath;
    private XPathExpression mReplyTimer;
    private XPathExpression mRootActiviationPath;
    private XPathExpression mRootBarringElement;
    private XPathExpression mRulePath;
    private XPath mXpath;

    public UtXmlParse() {
        try {
            this.mDocumenetbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        this.mXpath = XPathFactory.newInstance().newXPath();
        this.mReplyTimer = createXPathNode(UtElement.ELEMENT_NO_REPLY_TIMER);
        this.mRootActiviationPath = createXPathNode(UtElement.ELEMENT_ROOT_ACTIVATION);
        this.mRulePath = createXPathNode(UtElement.ELEMENT_RULE);
        this.mRootBarringElement = createXPathNode(UtElement.ELEMENT_ROOT_BARRING);
        this.mErrorPath = createXPathNode(UtElement.ELEMENT_ERROR);
        this.mDefaultBehavior = createXPathNode(UtElement.ELEMENT_BEHAVIOUR);
    }

    public boolean parseCallWaitingOrClip(String xml) {
        try {
            return extractBoolean(this.mRootActiviationPath, this.mDocumenetbuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8"))));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (SAXException e2) {
            e2.printStackTrace();
            return false;
        } catch (IOException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public CallBarringData parseCallBarring(String xml) {
        InputStream inputStream;
        Document document;
        InputStream inputStream2;
        Document document2;
        InputStream inputStream3;
        CallBarringData cbData = new CallBarringData();
        try {
            try {
                InputStream inputStream4 = new ByteArrayInputStream(xml.getBytes("utf-8"));
                try {
                    Document document3 = this.mDocumenetbuilder.parse(inputStream4);
                    int cbType = extractCbType(document3);
                    cbData.active = extractBoolean(this.mRootActiviationPath, document3);
                    NodeList nodes = extractNodeList(this.mRulePath, document3);
                    if (nodes != null) {
                        int i = 0;
                        while (i < nodes.getLength()) {
                            CallBarringData.Rule rules = new CallBarringData.Rule();
                            rules.ruleId = nodes.item(i).getAttributes().getNamedItem("id").getTextContent();
                            int j = 0;
                            while (j < nodes.item(i).getChildNodes().getLength()) {
                                NodeList child = nodes.item(i).getChildNodes();
                                if (child.item(j).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.CONDITIONS)) {
                                    rules.conditions = getConditions(child.item(j).getChildNodes());
                                    if (rules.conditions.condition == 16) {
                                        NodeList child_identity = child.item(j).getChildNodes();
                                        int k = 0;
                                        while (k < child_identity.getLength()) {
                                            if (child_identity.item(k).getNodeName().contains(UtElement.ELEMENT_IDENTITY)) {
                                                NodeList child_one_id = child_identity.item(k).getChildNodes();
                                                int l = 0;
                                                while (true) {
                                                    inputStream3 = inputStream4;
                                                    try {
                                                        if (l >= child_one_id.getLength()) {
                                                            break;
                                                        }
                                                        Document document4 = document3;
                                                        if (child_one_id.item(l).getNodeName().contains("one")) {
                                                            rules.target.add(child_one_id.item(l).getAttributes().getNamedItem("id").getTextContent());
                                                        }
                                                        l++;
                                                        inputStream4 = inputStream3;
                                                        document3 = document4;
                                                    } catch (UnsupportedEncodingException e) {
                                                        e = e;
                                                        InputStream inputStream5 = inputStream3;
                                                        e.printStackTrace();
                                                        return cbData;
                                                    } catch (SAXException e2) {
                                                        e = e2;
                                                        InputStream inputStream6 = inputStream3;
                                                        e.printStackTrace();
                                                        return cbData;
                                                    } catch (IOException e3) {
                                                        e = e3;
                                                        InputStream inputStream7 = inputStream3;
                                                        e.printStackTrace();
                                                        return cbData;
                                                    }
                                                }
                                                document2 = document3;
                                            } else {
                                                inputStream3 = inputStream4;
                                                document2 = document3;
                                            }
                                            k++;
                                            inputStream4 = inputStream3;
                                            document3 = document2;
                                        }
                                        inputStream2 = inputStream4;
                                        document = document3;
                                    } else {
                                        inputStream2 = inputStream4;
                                        document = document3;
                                    }
                                } else {
                                    inputStream2 = inputStream4;
                                    document = document3;
                                    if (child.item(j).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS)) {
                                        for (int k2 = 0; k2 < child.item(j).getChildNodes().getLength(); k2++) {
                                            NodeList actionChild = child.item(j).getChildNodes();
                                            if (actionChild.item(k2).getNodeName().contains("allow")) {
                                                rules.allow = Boolean.parseBoolean(child.item(j).getTextContent());
                                            } else if (actionChild.item(k2).getNodeType() == 1) {
                                                ActionElm elm = new ActionElm();
                                                elm.name = actionChild.item(k2).getNodeName();
                                                elm.value = actionChild.item(k2).getTextContent();
                                                rules.actions.add(elm);
                                            }
                                        }
                                    }
                                }
                                j++;
                                inputStream4 = inputStream2;
                                document3 = document;
                            }
                            InputStream inputStream8 = inputStream4;
                            Document document5 = document3;
                            if (rules.conditions.condition == -1) {
                                List<MEDIA> media = new ArrayList<>();
                                media.add(MEDIA.ALL);
                                rules.conditions.media = media;
                            }
                            combineCbType(rules, cbType);
                            cbData.rules.add(rules);
                            Log.i(LOG_TAG, "ruleId = " + rules.ruleId + " conditions = " + rules.conditions + " allow = " + rules.allow);
                            i++;
                            inputStream4 = inputStream8;
                            document3 = document5;
                        }
                        inputStream = inputStream4;
                        Document document6 = document3;
                    } else {
                        inputStream = inputStream4;
                        Document document7 = document3;
                    }
                } catch (UnsupportedEncodingException e4) {
                    e = e4;
                    InputStream inputStream9 = inputStream4;
                    e.printStackTrace();
                    return cbData;
                } catch (SAXException e5) {
                    e = e5;
                    InputStream inputStream10 = inputStream4;
                    e.printStackTrace();
                    return cbData;
                } catch (IOException e6) {
                    e = e6;
                    InputStream inputStream11 = inputStream4;
                    e.printStackTrace();
                    return cbData;
                }
            } catch (UnsupportedEncodingException e7) {
                e = e7;
                e.printStackTrace();
                return cbData;
            } catch (SAXException e8) {
                e = e8;
                e.printStackTrace();
                return cbData;
            } catch (IOException e9) {
                e = e9;
                e.printStackTrace();
                return cbData;
            }
        } catch (UnsupportedEncodingException e10) {
            e = e10;
            String str = xml;
            e.printStackTrace();
            return cbData;
        } catch (SAXException e11) {
            e = e11;
            String str2 = xml;
            e.printStackTrace();
            return cbData;
        } catch (IOException e12) {
            e = e12;
            String str3 = xml;
            e.printStackTrace();
            return cbData;
        }
        return cbData;
    }

    public int parseClir(String xml) {
        try {
            Document document = this.mDocumenetbuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            if (!extractBoolean(this.mRootActiviationPath, document)) {
                return 0;
            }
            String extractStr = extractString(this.mDefaultBehavior, document);
            if (TextUtils.isEmpty(extractStr)) {
                return 2;
            }
            if (extractStr.contains(UtElement.ELEMENT_CLI_RESTRICTED)) {
                return 1;
            }
            return extractStr.contains(UtElement.ELEMENT_CLI_NOT_RESTRICTED) ? 2 : 2;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }

    private void combineCbType(CallBarringData.Rule rules, int cbType) {
        if (rules.conditions.condition == 0) {
            if (cbType == 102) {
                rules.conditions.condition = 1;
            } else {
                rules.conditions.condition = 2;
            }
        } else if (rules.conditions.condition != 12) {
            if (rules.conditions.condition == 14) {
                rules.conditions.condition = 5;
            } else if (rules.conditions.condition == 10) {
                rules.conditions.condition = 3;
            } else if (rules.conditions.condition == 11) {
                rules.conditions.condition = 4;
            } else if (rules.conditions.condition != 13) {
                if (rules.conditions.condition == 15) {
                    rules.conditions.condition = 6;
                } else if (rules.conditions.condition == 16) {
                    rules.conditions.condition = 10;
                }
            }
        }
    }

    public CallForwardingData parseCallForwarding(String xml) {
        CallForwardingData cfData = new CallForwardingData();
        try {
            Document document = this.mDocumenetbuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            cfData.active = extractBoolean(this.mRootActiviationPath, document);
            cfData.replyTimer = extractInt(this.mReplyTimer, document);
            NodeList nodes = extractNodeList(this.mRulePath, document);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    String ruleid = nodes.item(i).getAttributes().getNamedItem("id").getTextContent();
                    if (!ruleid.contains("rule2") && !ruleid.contains("rule3") && !ruleid.contains("-vm")) {
                        if (!ruleid.contains("-default")) {
                            CallForwardingData.Rule rules = new CallForwardingData.Rule();
                            rules.ruleId = ruleid;
                            for (int j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {
                                NodeList child = nodes.item(i).getChildNodes();
                                if (child.item(j).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.CONDITIONS)) {
                                    rules.conditions = getConditions(child.item(j).getChildNodes());
                                } else if (child.item(j).getNodeName().contains(SoftphoneNamespaces.SoftphoneCallHandling.ACTIONS)) {
                                    rules.fwdElm = getForwardData(child.item(j).getChildNodes());
                                }
                            }
                            if (rules.conditions.condition == -1) {
                                rules.conditions = new Condition();
                                rules.conditions.condition = 0;
                                List<MEDIA> media = new ArrayList<>();
                                media.add(MEDIA.ALL);
                                rules.conditions.media = media;
                            }
                            cfData.rules.add(rules);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return cfData;
    }

    public String parseError(String xml) {
        String errorMsg = null;
        try {
            NodeList nodes = extractNodeList(this.mErrorPath, this.mDocumenetbuilder.parse(new ByteArrayInputStream(xml.getBytes("utf-8"))));
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    if ("constraint-failure".equals(nodes.item(i).getNodeName())) {
                        if (nodes.item(i).getAttributes().getNamedItem("phrase") != null) {
                            errorMsg = nodes.item(i).getAttributes().getNamedItem("phrase").getTextContent();
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SAXException e2) {
            e2.printStackTrace();
        } catch (IOException e3) {
            e3.printStackTrace();
        } catch (DOMException e4) {
            e4.printStackTrace();
        }
        return errorMsg;
    }

    private XPathExpression createXPathNode(String xpath) {
        try {
            return this.mXpath.compile(xpath);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractNodeName(XPathExpression expression, Document context) {
        try {
            Node node = (Node) expression.evaluate(context, XPathConstants.NODE);
            if (node != null) {
                return node.getNodeName();
            }
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractString(XPathExpression expression, Document context) {
        try {
            Node node = (Node) expression.evaluate(context, XPathConstants.NODE);
            if (node != null) {
                return node.getTextContent();
            }
            return null;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean extractBoolean(XPathExpression expression, Document context) {
        try {
            if (CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase((String) expression.evaluate(context, XPathConstants.STRING))) {
                return true;
            }
            return false;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int extractInt(XPathExpression expression, Document context) {
        try {
            String value = (String) expression.evaluate(context, XPathConstants.STRING);
            if (!TextUtils.isEmpty(value)) {
                return Integer.parseInt(value.trim());
            }
            return 20;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return 20;
        } catch (NumberFormatException e2) {
            Log.e(LOG_TAG, "Invalid integer");
            return 20;
        }
    }

    private static NodeList extractNodeList(XPathExpression expression, Document context) {
        try {
            return (NodeList) expression.evaluate(context, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ForwardTo getForwardData(NodeList nodes) {
        ForwardTo fwtList = new ForwardTo();
        int size = nodes.getLength();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {
                String name = nodes.item(i).getChildNodes().item(j).getNodeName();
                String value = nodes.item(i).getChildNodes().item(j).getTextContent();
                if (!name.equals("#text")) {
                    if (name.contains("to-target") || !name.contains(SoftphoneNamespaces.SoftphoneCallHandling.TARGET)) {
                        ForwardElm fwd = new ForwardElm();
                        fwd.id = name;
                        fwd.status = Boolean.parseBoolean(value);
                        fwtList.fwdElm.add(fwd);
                    } else {
                        fwtList.target = value;
                    }
                }
            }
        }
        return fwtList;
    }

    private List<MEDIA> getMediaTypes(NodeList nodes) {
        int size = nodes.getLength();
        List<MEDIA> media = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (nodes.item(i).getTextContent().contains(TMOConstants.CallLogTypes.AUDIO)) {
                media.add(MEDIA.AUDIO);
            } else if (nodes.item(i).getTextContent().contains(TMOConstants.CallLogTypes.VIDEO)) {
                media.add(MEDIA.VIDEO);
            }
        }
        if (media.isEmpty() != 0) {
            media.add(MEDIA.ALL);
        }
        return media;
    }

    private int getConditionfromName(String name) {
        if (name.contains("busy")) {
            return 1;
        }
        if (name.contains("no-answer")) {
            return 2;
        }
        if (name.contains("not-reachable")) {
            return 3;
        }
        if (name.contains("not-logged") || name.contains("not-registered")) {
            return 6;
        }
        if (name.contains("international-exHC")) {
            return 11;
        }
        if (name.contains("international")) {
            return 10;
        }
        if (name.contains("roaming")) {
            return 14;
        }
        if (name.contains("external-list")) {
            return 12;
        }
        if (name.contains("other-identity")) {
            return 13;
        }
        if (name.contains("anonymous")) {
            return 15;
        }
        if (name.contains(UtElement.ELEMENT_IDENTITY)) {
            return 16;
        }
        return 0;
    }

    private Condition getConditions(NodeList nodes) {
        int size = nodes.getLength();
        Condition condition = new Condition();
        condition.media = new ArrayList();
        for (int i = 0; i < size; i++) {
            String name = nodes.item(i).getNodeName();
            if (!name.contains("text")) {
                if (name.contains("rule-deactivated")) {
                    condition.state = false;
                    condition.action = 0;
                } else if (!name.contains("media")) {
                    condition.condition = getConditionfromName(name);
                } else if (nodes.item(i).getChildNodes().getLength() > 0 && condition.media.size() == 0) {
                    condition.media = getMediaTypes(nodes.item(i).getChildNodes());
                }
            }
        }
        if (size == 0 || condition.condition == -1) {
            condition.condition = 0;
        }
        if (condition.media.size() == 0) {
            condition.media.add(MEDIA.ALL);
        }
        return condition;
    }

    private int extractCbType(Document document) {
        String extractedNode = extractNodeName(this.mRootBarringElement, document);
        if (extractedNode == null || extractedNode.isEmpty()) {
            return 0;
        }
        if (extractedNode.contains(UtElement.ELEMENT_ICB)) {
            return 102;
        }
        if (extractedNode.contains(UtElement.ELEMENT_OCB)) {
            return 104;
        }
        return 0;
    }
}
