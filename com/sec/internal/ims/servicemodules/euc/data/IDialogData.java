package com.sec.internal.ims.servicemodules.euc.data;

public interface IDialogData {
    String getAcceptButton();

    EucMessageKey getKey();

    String getLanguage();

    String getRejectButton();

    String getSubject();

    String getText();
}
