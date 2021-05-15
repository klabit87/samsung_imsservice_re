package com.sec.internal.interfaces.ims.config;

public interface IDialogAdapter {
    void cleanup();

    boolean getAcceptReject(String str, String str2, String str3, String str4);

    boolean getAcceptReject(String str, String str2, String str3, String str4, int i);

    String getMsisdn(String str);

    String getMsisdn(String str, String str2);

    boolean getNextCancel();
}
