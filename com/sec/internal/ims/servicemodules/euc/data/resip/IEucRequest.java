package com.sec.internal.ims.servicemodules.euc.data.resip;

public interface IEucRequest extends IEuc<IEucMessageData> {

    public enum EucRequestType {
        VOLATILE,
        PERSISTENT
    }

    public interface IEucMessageData {
        String getAcceptButton();

        String getRejectButton();

        String getSubject();

        String getText();
    }

    Long getTimeOut();

    EucRequestType getType();

    boolean isExternal();

    boolean isPinRequested();
}
