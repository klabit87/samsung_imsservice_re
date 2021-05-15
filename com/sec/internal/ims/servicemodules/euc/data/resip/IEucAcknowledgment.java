package com.sec.internal.ims.servicemodules.euc.data.resip;

public interface IEucAcknowledgment extends IEuc<IEUCMessageData> {

    public interface IEUCMessageData {
        String getSubject();

        String getText();
    }

    IEUCMessageData getDefaultData();
}
