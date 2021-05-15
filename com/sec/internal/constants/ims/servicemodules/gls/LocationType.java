package com.sec.internal.constants.ims.servicemodules.gls;

public enum LocationType {
    OWN_LOCATION(1),
    OTHER_LOCATION(2);
    
    private final int mValue;

    public String toString() {
        return Integer.toString(this.mValue);
    }

    private LocationType(int value) {
        this.mValue = value;
    }
}
