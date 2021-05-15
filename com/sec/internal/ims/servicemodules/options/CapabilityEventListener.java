package com.sec.internal.ims.servicemodules.options;

import android.os.Bundle;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.options.OptionsEvent;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityEventListener;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityExchangeControl;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class CapabilityEventListener implements ICapabilityEventListener {
    private static final String LOG_TAG = "CapabilityEventListener";
    private CapabilityDiscoveryModule mCapabilityDiscovery;

    CapabilityEventListener(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    public void onCapabilityUpdate(List<ImsUri> uris, long features, CapabilityConstants.CapExResult result, String pidf, int phoneId) {
        Bundle b = new Bundle();
        b.putParcelableArrayList("URIS", new ArrayList(uris));
        b.putString("PIDF", pidf);
        b.putLong("FEATURES", features);
        b.putInt("PHONEID", phoneId);
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(4, result.ordinal(), -1, b));
    }

    public void onCapabilityUpdate(List<ImsUri> uris, CapabilityConstants.CapExResult result, String pidf, OptionsEvent event) {
        ArrayList<ImsUri> paidList = new ArrayList<>(2);
        paidList.addAll(event.getPAssertedIdSet());
        Bundle b = new Bundle();
        b.putParcelableArrayList("URIS", new ArrayList(uris));
        b.putString("PIDF", pidf);
        b.putLong("FEATURES", event.getFeatures());
        b.putInt("LASTSEEN", event.getLastSeen());
        b.putInt("PHONEID", event.getPhoneId());
        b.putString("EXTFEATURE", event.getExtFeature());
        b.putBoolean("ISTOKENUSED", event.getIsTokenUsed());
        b.putParcelableArrayList("PAID", paidList);
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(4, result.ordinal(), -1, b));
        if (!event.isResponse() && event.getTxId() != null) {
            this.mCapabilityDiscovery.prepareResponse(uris, event.getFeatures(), event.getTxId(), event.getPhoneId(), event.getExtFeature());
        }
    }

    public void onMediaReady(boolean ready, boolean isPresence, int phoneId) {
        ICapabilityExchangeControl mCapabilityControl;
        IMSLog.i(LOG_TAG, phoneId, "onMediaReady: ready " + ready + ", isPresence " + isPresence);
        CapabilityConfig mCapabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(phoneId);
        if ((mCapabilityConfig == null || mCapabilityConfig.usePresence() == isPresence) && (mCapabilityControl = this.mCapabilityDiscovery.getCapabilityControl(phoneId)) != null && mCapabilityControl.isReadyToRequest(phoneId)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(3, Integer.valueOf(phoneId)));
        }
    }

    public void onPollingRequested(boolean success, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "onPollingRequested: success " + success);
        if (!success) {
            this.mCapabilityDiscovery.stopPollingTimer();
        } else if (this.mCapabilityDiscovery.mPollingIntent == null && this.mCapabilityDiscovery.getCapabilityControl(phoneId).isReadyToRequest(phoneId)) {
            this.mCapabilityDiscovery.startPollingTimer(phoneId);
        }
    }

    public void onCapabilityAndAvailabilityPublished(int errorCode, int phoneId) {
        this.mCapabilityDiscovery.notifyEABServiceAdvertiseResult(errorCode, phoneId);
    }
}
