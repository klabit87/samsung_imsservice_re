package com.sec.internal.ims.config.adapters;

import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;

public class TelephonyAdapterState implements ITelephonyAdapter {
    protected static String ABSENT_STATE = "AbsentState";
    protected static String IDLE_STATE = "IdleState";
    protected static String READY_STATE = "ReadyState";
    protected static String SMS_DEST_PORT = "37273";
    protected static String SMS_ORIG_PORT = "0";

    public boolean isReady() {
        return false;
    }

    public String getPrimaryIdentity() {
        return null;
    }

    public String getMcc() {
        return null;
    }

    public String getMnc() {
        return null;
    }

    public String getImsi() {
        return null;
    }

    public String getImei() {
        return null;
    }

    public String getMsisdn() {
        return null;
    }

    public String getSimCountryCode() {
        return null;
    }

    public String getSipUri() {
        return null;
    }

    public String getNetType() {
        return null;
    }

    public String getSmsDestPort() {
        return SMS_DEST_PORT;
    }

    public String getSmsOrigPort() {
        return SMS_ORIG_PORT;
    }

    public String getExistingOtp() {
        return null;
    }

    public String getExistingPortOtp() {
        return null;
    }

    public String getOtp() {
        return null;
    }

    public String getPortOtp() {
        return null;
    }

    public String getMsisdnNumber() {
        return null;
    }

    public String getAppToken(boolean isRetry) {
        return null;
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
    }

    public void notifyAutoConfigurationListener(int type, boolean result) {
    }

    public void sendVerificationCode(String value) {
    }

    public void sendMsisdnNumber(String value) {
    }

    public void cleanup() {
    }

    public void registerUneregisterForOTP(boolean val) {
    }

    public String getIdentityByPhoneId(int phoneId) {
        return null;
    }

    public String getSubscriberId(int subscriptionId) {
        return null;
    }

    public String getMsisdn(int subscriptionId) {
        return null;
    }

    public String getDeviceId(int slotId) {
        return null;
    }
}
