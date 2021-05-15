package com.sec.internal.ims.servicemodules.options;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.csh.CshModuleBase;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapabilityServiceEventListener {
    private static final String LOG_TAG = "CapabilityServiceEventListener";
    private Map<Integer, RemoteCallbackList<ICapabilityServiceEventListener>> mListenersList = new HashMap();

    public CapabilityServiceEventListener() {
        int phoneCount = SimUtil.getPhoneCount();
        for (int phoneId = 0; phoneId < phoneCount; phoneId++) {
            this.mListenersList.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
    }

    public void registerListener(ICapabilityServiceEventListener listener, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "registerListener: " + listener);
        RemoteCallbackList<ICapabilityServiceEventListener> eventListener = this.mListenersList.get(Integer.valueOf(phoneId));
        if (eventListener != null) {
            synchronized (eventListener) {
                if (listener != null) {
                    eventListener.register(listener);
                    try {
                        listener.onOwnCapabilitiesChanged();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    this.mListenersList.put(Integer.valueOf(phoneId), eventListener);
                }
            }
        }
    }

    public void unregisterListener(ICapabilityServiceEventListener listener, int phoneId) {
        IMSLog.i(LOG_TAG, phoneId, "unregisterListener: " + listener);
        RemoteCallbackList<ICapabilityServiceEventListener> eventListener = this.mListenersList.get(Integer.valueOf(phoneId));
        if (eventListener != null) {
            synchronized (eventListener) {
                if (listener != null) {
                    eventListener.unregister(listener);
                    this.mListenersList.put(Integer.valueOf(phoneId), eventListener);
                }
            }
        }
    }

    public void notifyOwnCapabilitiesChanged(int phoneId) {
        RemoteCallbackList<ICapabilityServiceEventListener> eventListener = this.mListenersList.get(Integer.valueOf(phoneId));
        try {
            int length = eventListener.beginBroadcast();
            IMSLog.i(LOG_TAG, phoneId, "notifyOwnCapabilitiesChanged: eventListener length: " + length);
            for (int index = 0; index < length; index++) {
                ICapabilityServiceEventListener listener = eventListener.getBroadcastItem(index);
                IMSLog.s(LOG_TAG, phoneId, "No. " + index + " notifyOwnCapabilitiesChanged: listener: " + listener);
                listener.onOwnCapabilitiesChanged();
            }
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            IMSLog.i(LOG_TAG, phoneId, "notifyOwnCapabilitiesChanged: finishBroadcast()");
            eventListener.finishBroadcast();
        } catch (IllegalStateException | NullPointerException e2) {
            e2.printStackTrace();
        }
    }

    public void notifyCapabilitiesChanged(List<ImsUri> uris, Capabilities capex, ImsUri activeCallRemoteUri, int phoneId) {
        RemoteCallbackList<ICapabilityServiceEventListener> eventListener = this.mListenersList.get(Integer.valueOf(phoneId));
        try {
            int length = eventListener.beginBroadcast();
            IMSLog.i(LOG_TAG, phoneId, "notifyCapabilitiesChanged: eventListener length: " + length);
            for (int index = 0; index < length; index++) {
                eventListener.getBroadcastItem(index).onCapabilitiesChanged(uris, capex);
            }
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
        try {
            eventListener.finishBroadcast();
        } catch (IllegalStateException | NullPointerException e2) {
            e2.printStackTrace();
        }
        if (activeCallRemoteUri != null) {
            for (ImsUri uri : uris) {
                if (TextUtils.equals(uri.getMsisdn(), activeCallRemoteUri.getMsisdn())) {
                    for (ServiceModuleBase module : ImsRegistry.getAllServiceModules()) {
                        if (module instanceof CshModuleBase) {
                            ((CshModuleBase) module).onRemoteCapabilitiesChanged(capex);
                        }
                    }
                }
            }
        }
    }

    public void notifyEABServiceAdvertiseResult(int errorCode, int phoneId) {
        RemoteCallbackList<ICapabilityServiceEventListener> eventListener = this.mListenersList.get(Integer.valueOf(phoneId));
        try {
            int length = eventListener.beginBroadcast();
            IMSLog.i(LOG_TAG, phoneId, "notifyEABServiceAdvertiseResult: eventListener length: " + length);
            for (int index = 0; index < length; index++) {
                eventListener.getBroadcastItem(index).onCapabilityAndAvailabilityPublished(errorCode);
            }
            eventListener.finishBroadcast();
        } catch (RemoteException | IllegalStateException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
