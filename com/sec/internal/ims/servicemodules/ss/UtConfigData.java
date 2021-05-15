package com.sec.internal.ims.servicemodules.ss;

public class UtConfigData {
    String apnSelection;
    int bsfPort;
    String bsfServer;
    String impu;
    int nafPort;
    String nafServer;
    String passwd;
    String userAgent;
    String username;
    String xcapRootUri;
    String xdmUserAgent;

    public String toString() {
        return "xcapRootUri : " + this.xcapRootUri + " nafServer : " + this.nafServer + " bsfServer : " + this.bsfServer + " nafPort :" + this.nafPort + " bsfPort : " + this.bsfPort;
    }
}
