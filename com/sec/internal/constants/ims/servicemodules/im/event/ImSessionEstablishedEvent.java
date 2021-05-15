package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import java.util.EnumSet;
import java.util.List;

public class ImSessionEstablishedEvent {
    public List<String> mAcceptTypes;
    public List<String> mAcceptWrappedTypes;
    public String mChatId;
    public EnumSet<SupportedFeature> mFeatures;
    public Object mRawHandle;
    public ImsUri mSessionUri;

    public ImSessionEstablishedEvent(Object rawHandle, String chatId, ImsUri sessionUri, EnumSet<SupportedFeature> features, List<String> acceptTypes, List<String> acceptWrappedTypes) {
        this.mRawHandle = rawHandle;
        this.mChatId = chatId;
        this.mSessionUri = sessionUri;
        this.mFeatures = features;
        this.mAcceptTypes = acceptTypes;
        this.mAcceptWrappedTypes = acceptWrappedTypes;
    }

    public String toString() {
        return "ImSessionEstablishedEvent [mRawHandle=" + this.mRawHandle + ", mChatId=" + this.mChatId + ", mSessionUri=" + this.mSessionUri + ", mFeatures=" + this.mFeatures + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + "]";
    }
}
