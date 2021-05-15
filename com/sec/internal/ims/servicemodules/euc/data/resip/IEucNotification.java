package com.sec.internal.ims.servicemodules.euc.data.resip;

public interface IEucNotification extends IEuc<IEucMessageData> {

    public interface IEucMessageData {
        String getOkButton();

        String getSubject();

        String getText();
    }
}
