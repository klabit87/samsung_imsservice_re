package com.sec.internal.ims.core.handler.secims.imsCommonStruc;

public final class XdmDocType {
    public static final int OMA_CAB_AB = 2;
    public static final int OMA_CAB_PCC = 1;
    public static final int OMA_XML_DOC_DIR = 0;
    public static final int UNKNOWN_XDM_DOC_TYPE = 3;
    public static final String[] names = {"OMA_XML_DOC_DIR", "OMA_CAB_PCC", "OMA_CAB_AB", "UNKNOWN_XDM_DOC_TYPE"};

    private XdmDocType() {
    }

    public static String name(int e) {
        return names[e];
    }
}
