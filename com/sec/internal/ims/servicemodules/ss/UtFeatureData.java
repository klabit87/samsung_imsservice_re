package com.sec.internal.ims.servicemodules.ss;

public class UtFeatureData {
    String cbbaic;
    String cbbicwr;
    String cfUriType;
    String cfb;
    String cfni;
    String cfnr;
    String cfnrc;
    String cfu;
    int delay_disconnect_pdn;
    boolean insertNewRule;
    int ip_version;
    boolean isBlockUntilReboot;
    boolean isCBSingleElement;
    boolean isCFSingleElement;
    boolean isCsfbWithImserror;
    boolean isDisconnectXcapPdn;
    boolean isErrorMsgDisplay;
    boolean isNeedFirstGet;
    boolean isNeedSeparateCFA;
    boolean isNeedSeparateCFNL;
    boolean isNeedSeparateCFNRY;
    boolean noMediaForCB;
    boolean setAllMediaCF;
    boolean supportAlternativeMediaForCb;
    boolean supportSimservsRetry;
    boolean support_media;
    boolean support_ss;
    boolean support_tls;

    public String toString() {
        return "support_tls : " + this.support_tls + " support_media : " + this.support_media + " isCFSingleElement : " + this.isCFSingleElement + " isCBSingleElement : " + this.isCBSingleElement + " isBlockUntilReboot : " + this.isBlockUntilReboot + " isNeedSeparateCFNL : " + this.isNeedSeparateCFNL + " isNeedSeparateCFNRY : " + this.isNeedSeparateCFNRY + " isNeedFirstGet : " + this.isNeedFirstGet + " isErrorMsgDisplay : " + this.isErrorMsgDisplay + " support_ss : " + this.support_ss + " isDisconnectXcapPdn : " + this.isDisconnectXcapPdn + " isCsfbWithImserror : " + this.isCsfbWithImserror;
    }
}
