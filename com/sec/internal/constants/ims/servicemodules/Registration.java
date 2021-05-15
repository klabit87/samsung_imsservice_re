package com.sec.internal.constants.ims.servicemodules;

import com.sec.ims.ImsRegistration;

public class Registration {
    private ImsRegistration mImsRegistration;
    private Boolean mIsReRegi;

    public Registration(ImsRegistration registration, boolean isReRegi) {
        this.mImsRegistration = registration;
        this.mIsReRegi = Boolean.valueOf(isReRegi);
    }

    public ImsRegistration getImsRegi() {
        return this.mImsRegistration;
    }

    public boolean isReRegi() {
        return this.mIsReRegi.booleanValue();
    }
}
