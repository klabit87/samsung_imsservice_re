package com.sec.internal.ims.servicemodules.options;

import android.util.Log;
import android.util.SparseArray;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.IServiceAvailabilityEventListener;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public class ServiceAvailabilityEventListenerWrapper implements IServiceAvailabilityEventListener {
    private static final int EXPECTED_NUMBER_OF_SIM_SLOTS = 2;
    private static final String LOG_TAG = "ServiceAvailabilityEventListenerWrapper";
    CapabilityDiscoveryModule mCapabilityDiscovery;
    IImModule mImModule;
    private SparseArray<String> mProfileList;
    private SparseArray<IServiceAvailabilityEventListener> mServiceAvailabilityEventListenerList;

    public ServiceAvailabilityEventListenerWrapper(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mImModule = null;
        this.mServiceAvailabilityEventListenerList = new SparseArray<>(2);
        this.mProfileList = new SparseArray<>(2);
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    public void attachServiceAvailabilityEventListener(int phoneId, String rcsProfile) {
        if (this.mImModule != null) {
            if (this.mServiceAvailabilityEventListenerList.size() == 0) {
                this.mImModule.registerServiceAvailabilityEventListener(this);
            }
            attach(phoneId, rcsProfile);
            this.mProfileList.put(phoneId, rcsProfile);
        }
    }

    private void attach(int phoneId, String rcsProfile) {
        IServiceAvailabilityEventListener listener;
        if (!ImsProfile.isRcsUpProfile(rcsProfile) || this.mCapabilityDiscovery.getCapabilityConfig(phoneId) == null || this.mCapabilityDiscovery.getCapabilityConfig(phoneId).getDefaultDisc(phoneId) == 2) {
            Log.i(LOG_TAG, "attaching ServiceAvailabilityEventListenerBasic phoneId: " + phoneId);
            listener = new ServiceAvailabilityEventListenerBasic();
        } else {
            Log.i(LOG_TAG, "attaching ServiceAvailabilityEventListenerUp phoneId: " + phoneId);
            listener = new ServiceAvailabilityEventListenerUp(this.mCapabilityDiscovery.getLooper(), this.mCapabilityDiscovery.getCapabilitiesCache(phoneId), this.mCapabilityDiscovery.getUriGenerator());
        }
        this.mServiceAvailabilityEventListenerList.put(phoneId, listener);
    }

    public void detachServiceAvailabilityEventListener(int phoneId) {
        if (this.mImModule != null && this.mServiceAvailabilityEventListenerList.size() > 0) {
            this.mServiceAvailabilityEventListenerList.remove(phoneId);
            this.mProfileList.remove(phoneId);
            if (this.mServiceAvailabilityEventListenerList.size() == 0) {
                this.mImModule.unregisterServiceAvailabilityEventListener(this);
            }
        }
    }

    public void updateServiceAvailabilityEventListener(int phoneId) {
        if (this.mImModule != null && this.mServiceAvailabilityEventListenerList.get(phoneId) != null && this.mProfileList.get(phoneId) != null) {
            attach(phoneId, this.mProfileList.get(phoneId));
        }
    }

    public void onServiceAvailabilityUpdate(String ownIdentity, ImsUri uri, Date timestamp) {
        int phoneId = SimManagerFactory.getPhoneId(ownIdentity);
        if (phoneId == -1) {
            Log.e(LOG_TAG, "onServiceAvailabilityUpdate: failed to find phoneId for ownIdentity: " + IMSLog.checker(ownIdentity) + "!");
        } else if (this.mServiceAvailabilityEventListenerList.get(phoneId) != null) {
            this.mServiceAvailabilityEventListenerList.get(phoneId).onServiceAvailabilityUpdate(ownIdentity, uri, timestamp);
        } else {
            Log.e(LOG_TAG, "onServiceAvailabilityUpdate: ServiceAvailability listener is not attached for ownIdentity: " + IMSLog.checker(ownIdentity) + "!");
        }
    }
}
