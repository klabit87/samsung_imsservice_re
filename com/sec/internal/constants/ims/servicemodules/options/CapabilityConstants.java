package com.sec.internal.constants.ims.servicemodules.options;

public class CapabilityConstants {

    public enum RequestType {
        REQUEST_TYPE_NONE,
        REQUEST_TYPE_LAZY,
        REQUEST_TYPE_PERIODIC,
        REQUEST_TYPE_CONTACT_CHANGE
    }

    public enum CapExResult {
        SUCCESS,
        POLLING_SUCCESS,
        FAILURE,
        USER_NOT_FOUND,
        DOES_NOT_EXIST_ANYWHERE,
        USER_UNAVAILABLE,
        FORBIDDEN_403,
        REQUEST_TIMED_OUT,
        INVALID_DATA,
        NETWORK_ERROR,
        USER_AVAILABLE_OFFLINE,
        UNCLASSIFIED_ERROR,
        USER_NOT_REGISTERED;

        public boolean isOneOf(CapExResult... results) {
            for (CapExResult result : results) {
                if (this == result) {
                    return true;
                }
            }
            return false;
        }
    }
}
