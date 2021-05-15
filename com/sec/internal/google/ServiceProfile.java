package com.sec.internal.google;

import com.android.ims.internal.IImsRegistrationListener;

public class ServiceProfile {
    private int mPhoneId;
    private IImsRegistrationListener mRegistrationListener;
    private int mServiceClass;

    public ServiceProfile(int phoneId, int serviceClass, IImsRegistrationListener listener) {
        this.mPhoneId = phoneId;
        this.mServiceClass = serviceClass;
        this.mRegistrationListener = listener;
    }

    public void setRegistrationListener(IImsRegistrationListener listener) {
        this.mRegistrationListener = listener;
    }

    public int getServiceClass() {
        return this.mServiceClass;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public IImsRegistrationListener getRegistrationListener() {
        return this.mRegistrationListener;
    }
}
