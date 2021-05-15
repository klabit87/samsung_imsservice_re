package com.sec.internal.ims.xdm.event;

import android.os.Message;

public class XdmFetchDocumentParams extends XdmBaseParams {
    public final String mAccessToken;
    public final String mName;
    public final NodeSelector mNodeSelector;
    public final DocType mType;

    public enum DocType {
        OMA_CAB_AB
    }

    public static class NodeSelector {
        public String mAttr;
        public String mAttrVal;
        public final String mNode;
        public int mPosition;

        public NodeSelector(String node, int pos, String attr, String attrVal) {
            this.mNode = node;
            this.mPosition = pos;
            this.mAttr = attr;
            this.mAttrVal = attrVal;
        }

        public String toString() {
            return "NodeSelector [mNode = " + this.mNode + ", mPosition = " + this.mPosition + ", mAttr = " + this.mAttr + ", mAttrVal = " + this.mAttrVal + "]";
        }
    }

    public XdmFetchDocumentParams(String xui, DocType type, String dir_or_doc_name, Message callback, NodeSelector selector, String accessToken) {
        super(xui, callback);
        this.mType = type;
        this.mName = dir_or_doc_name;
        this.mNodeSelector = selector;
        this.mAccessToken = accessToken;
    }

    public String toString() {
        return "XdmFetchDocumentParams [mXui = " + this.mXui + ", mType = " + this.mType + ", mName = " + this.mName + ", mCallback = " + this.mCallback + ", mNodeSelector = " + this.mNodeSelector + ", mAccessToken = " + this.mAccessToken + "]";
    }
}
