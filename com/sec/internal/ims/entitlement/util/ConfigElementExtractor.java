package com.sec.internal.ims.entitlement.util;

import android.text.TextUtils;
import com.sec.internal.log.IMSLog;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConfigElementExtractor {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = ConfigElementExtractor.class.getSimpleName();

    public static Map<String, String> getAllElements(String deviceConfigXml, String xPathExpression) {
        Map<String, String> allElementsMap = new HashMap<>();
        Map<String, Integer> mapNameCount = new HashMap<>();
        try {
            NodeList nl = (NodeList) XPathFactory.newInstance().newXPath().evaluate(xPathExpression, new InputSource(new ByteArrayInputStream(deviceConfigXml.getBytes())), XPathConstants.NODESET);
            int length = nl.getLength();
            for (int i = 0; i < length; i++) {
                addChildNodes("", nl.item(i), allElementsMap, mapNameCount);
            }
        } catch (IllegalArgumentException | XPathExpressionException e) {
            String str = LOG_TAG;
            IMSLog.s(str, "XPath expression failed or Source became null" + e.getMessage());
        }
        return allElementsMap;
    }

    private static void addChildNodes(String parentName, Node node, Map<String, String> allElementsMap, Map<String, Integer> mapNameCount) {
        String keyName = deriveKeyName(parentName, node, mapNameCount);
        if (node.getChildNodes() != null && node.getChildNodes().getLength() == 1) {
            allElementsMap.put(keyName, node.getTextContent().trim());
        }
        if (!node.hasAttributes() && !node.hasChildNodes() && node.getNodeType() == 2 && node.getNodeValue() != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "addChildNodes: keyName = " + keyName + " value = " + node.getNodeValue());
            allElementsMap.put(keyName, node.getNodeValue().trim());
        }
        addAllAttributeNodes(keyName, node, allElementsMap);
        addAllChildsToMap(node, keyName, allElementsMap, mapNameCount);
    }

    private static String deriveKeyName(String parentName, Node node, Map<String, Integer> mapNameCount) {
        String keyName = node.getNodeName();
        if (!TextUtils.isEmpty(parentName)) {
            keyName = parentName + "/" + node.getNodeName();
        }
        if (mapNameCount.get(keyName) == null) {
            mapNameCount.put(keyName, 1);
            return keyName;
        }
        int count = mapNameCount.get(keyName).intValue();
        mapNameCount.put(keyName, Integer.valueOf(count + 1));
        return keyName + "[" + (count + 1) + "]";
    }

    private static void addAllChildsToMap(Node node, String parentName, Map<String, String> allElementsMap, Map<String, Integer> mapNameCount) {
        NodeList nl = node.getChildNodes();
        int length = nl.getLength();
        for (int i = 0; i < length; i++) {
            Node childNode = nl.item(i);
            if (childNode.getNodeType() == 1) {
                String keyName = deriveKeyName(parentName, childNode, mapNameCount);
                if (childNode.getChildNodes() != null && childNode.getChildNodes().getLength() == 1) {
                    allElementsMap.put(keyName, childNode.getTextContent().trim());
                }
                addAllAttributeNodes(keyName, childNode, allElementsMap);
                addAllChildsToMap(childNode, keyName, allElementsMap, mapNameCount);
            }
        }
    }

    private static void addAllAttributeNodes(String keyName, Node node, Map<String, String> allElementsMap) {
        NamedNodeMap namedNodeMap = node.getAttributes();
        if (namedNodeMap != null && namedNodeMap.getLength() > 0) {
            Node attrNode = namedNodeMap.item(0);
            String attrName = attrNode.getNodeName();
            String attrValue = attrNode.getTextContent();
            allElementsMap.put(keyName + "." + attrName, attrValue);
            for (int ind = 0; ind < namedNodeMap.getLength(); ind++) {
                Node attrNode2 = namedNodeMap.item(ind);
                String attrName2 = attrNode2.getNodeName();
                String attrValue2 = attrNode2.getTextContent();
                allElementsMap.put(keyName + "." + attrName2, attrValue2);
            }
        }
    }
}
