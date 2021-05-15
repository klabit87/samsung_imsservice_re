package com.sec.internal.ims.entitlement.nsds;

import android.os.Handler;
import android.os.Looper;
import android.os.Messenger;
import com.sec.internal.helper.State;

public abstract class NSDSModuleBase extends Handler {
    protected NSDSModuleBase(Looper looper) {
        super(looper);
    }

    public void onSimReady(boolean isSwapped) {
    }

    public void onSimNotAvailable() {
    }

    public void initForDeviceReady() {
    }

    public void onDeviceReady() {
    }

    protected static class InitialState extends State {
        protected InitialState() {
        }
    }

    public void queueRefreshDeviceConfig(int retyCount) {
    }

    public void queueGcmTokenRetrieval() {
    }

    public void queuePushTokenUpdateInEntitlementServer() {
    }

    public void registerEventMessenger(Messenger messenger) {
    }

    public void unregisterEventMessenger(Messenger messenger) {
    }

    public void queueRefreshDeviceAndServiceInfo(int deviceEventType, int retryCount) {
    }

    public void activateSimDevice(int deviceEventType, int retryCount) {
    }

    public void deactivateSimDevice(int deactivationCause) {
    }

    public void updateE911Address() {
    }

    public void handleVoWifToggleOnEvent() {
    }

    public void handleVoWifToggleOffEvent() {
    }

    public void updateEntitlementUrl(String url) {
    }

    public void retrieveAkaToken(int deviceEventType, int retryCount) {
    }

    public void dump() {
    }
}
