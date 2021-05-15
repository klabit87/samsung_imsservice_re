package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.capability.CapabilitiesLog;
import com.gsma.services.rcs.capability.ICapabilitiesListener;
import com.gsma.services.rcs.capability.ICapabilityService;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapabilityServiceImpl extends ICapabilityService.Stub {
    private static final String LOG_TAG = CapabilityServiceImpl.class.getSimpleName();
    private static final String SERVICE_ID_CALL_COMPOSER = "gsma.callcomposer";
    private static final String SERVICE_ID_POST_CALL = "gsma.callunanswered";
    private static final String SERVICE_ID_SHARED_MAP = "gsma.sharedmap";
    private static final String SERVICE_ID_SHARED_SKETCH = "gsma.sharedsketch";
    private RemoteCallbackList<ICapabilitiesListener> mCapabilitiesListeners = new RemoteCallbackList<>();
    private CapabilityDiscoveryService mCapabilityDiscoveryService = null;
    private Hashtable<String, RemoteCallbackList<ICapabilitiesListener>> mContactCapalitiesListeners = new Hashtable<>();
    Context mContext = null;
    private Object mLock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();
    private ICapabilityServiceEventListener.Stub serviceEventListener = null;

    public CapabilityServiceImpl(Context context) {
        this.mContext = context;
        this.serviceEventListener = new ICapabilityServiceEventListener.Stub() {
            public void onOwnCapabilitiesChanged() throws RemoteException {
                CapabilityServiceImpl.this.notifyOwnCapabilityChange();
            }

            public void onCapabilitiesChanged(List<ImsUri> uris, Capabilities capa) throws RemoteException {
                for (ImsUri uri : uris) {
                    CapabilityServiceImpl.this.receiveCapabilities(uri.toString(), capa);
                }
            }

            public void onMultipleCapabilitiesChanged(List<ImsUri> list, List<Capabilities> list2) throws RemoteException {
            }

            public void onCapabilityAndAvailabilityPublished(int errorCode) throws RemoteException {
            }
        };
        this.mCapabilityDiscoveryService = (CapabilityDiscoveryService) ImsRegistry.getBinder("options", (String) null);
        try {
            int phoneCount = SimUtil.getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                this.mCapabilityDiscoveryService.registerListener(this.serviceEventListener, i);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager manager = ImsRegistry.getRegistrationManager();
        if (manager == null) {
            return false;
        }
        for (ImsRegistration reg : manager.getRegistrationInfo()) {
            if (reg.hasService("options") || reg.hasService("presence")) {
                return true;
            }
        }
        return false;
    }

    public void addEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        this.mServiceListeners.register(listener);
    }

    public void removeEventListener(IRcsServiceRegistrationListener listener) throws ServerApiException {
        this.mServiceListeners.unregister(listener);
    }

    public void notifyRegistrationEvent(boolean registered, RcsServiceRegistration.ReasonCode code) {
        Log.d(LOG_TAG, "start : notifyRegistrationEvent()");
        synchronized (this.mLock) {
            int N = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                if (registered) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (Exception e) {
                        String str = LOG_TAG;
                        Log.d(str, "Can't notify listener : " + e.getMessage());
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(code);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public com.gsma.services.rcs.capability.Capabilities getMyCapabilities() throws ServerApiException {
        com.gsma.services.rcs.capability.Capabilities ret = null;
        try {
            Capabilities capabilities = this.mCapabilityDiscoveryService.getOwnCapabilities(SimUtil.getDefaultPhoneId());
            if (capabilities != null) {
                ret = transferCapabilities(capabilities);
            }
            String str = LOG_TAG;
            Log.d(str, "getMyCapabilities: " + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public com.gsma.services.rcs.capability.Capabilities getContactCapabilities(ContactId contact) throws ServerApiException {
        com.gsma.services.rcs.capability.Capabilities ret = null;
        try {
            Capabilities capabilities = this.mCapabilityDiscoveryService.getCapabilities(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contact.toString())), CapabilityRefreshType.DISABLED.ordinal(), SimUtil.getDefaultPhoneId());
            if (capabilities != null) {
                ret = transferCapabilities(capabilities);
            }
            String str = LOG_TAG;
            Log.d(str, "getContactCapabilities: contact = " + contact + ", ret = " + ret);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, com.gsma.services.rcs.capability.Capabilities> getAllContactCapabilities() {
        Log.d(LOG_TAG, "start : getAllContactCapabilities()");
        Map<String, com.gsma.services.rcs.capability.Capabilities> retMap = null;
        try {
            Capabilities[] capabilitiesArray = this.mCapabilityDiscoveryService.getAllCapabilities(SimUtil.getDefaultPhoneId());
            if (capabilitiesArray == null) {
                return null;
            }
            retMap = new HashMap<>();
            for (Capabilities capabilities : capabilitiesArray) {
                retMap.put(capabilities.getNumber(), transferCapabilities(capabilities));
            }
            return retMap;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void requestContactCapabilities(ContactId contact) throws ServerApiException {
        Log.d(LOG_TAG, "start : requestContactCapabilities(String contact)");
        try {
            this.mCapabilityDiscoveryService.getCapabilities(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contact.toString())), CapabilityRefreshType.ALWAYS_FORCE_REFRESH.ordinal(), SimUtil.getDefaultPhoneId());
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public void receiveCapabilities(String contact, Capabilities capabilities) {
        String str = LOG_TAG;
        IMSLog.s(str, "receiveCapabilities() contact = " + contact + " capabilities = " + capabilities);
        synchronized (this.mLock) {
            com.gsma.services.rcs.capability.Capabilities c = transferCapabilities(capabilities);
            String number = PhoneUtils.extractNumberFromUri(contact);
            notifyListeners(number, c, this.mCapabilitiesListeners);
            RemoteCallbackList<ICapabilitiesListener> listeners = this.mContactCapalitiesListeners.get(number);
            if (listeners != null) {
                notifyListeners(number, c, listeners);
            }
        }
    }

    private void notifyListeners(String contact, com.gsma.services.rcs.capability.Capabilities capabilities, RemoteCallbackList<ICapabilitiesListener> listeners) {
        String str = LOG_TAG;
        IMSLog.s(str, "start : notifyListeners() contact = " + contact + " capabilities = " + capabilities);
        ContactId contactId = new ContactId(contact);
        try {
            int N = listeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                listeners.getBroadcastItem(i).onCapabilitiesReceived(contactId, capabilities);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            e3.printStackTrace();
        }
        try {
            listeners.finishBroadcast();
        } catch (NullPointerException e4) {
            e4.printStackTrace();
        } catch (IllegalStateException e5) {
            e5.printStackTrace();
        }
    }

    public void requestAllContactsCapabilities() throws ServerApiException {
        Log.i(LOG_TAG, "start : requestAllContactsCapabilities()");
        this.mContext.sendBroadcast(new Intent("com.sec.internal.ims.servicemodules.options.poll_timeout"));
    }

    public void addCapabilitiesListener(ICapabilitiesListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mCapabilitiesListeners.register(listener);
        }
    }

    public void removeCapabilitiesListener(ICapabilitiesListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mCapabilitiesListeners.unregister(listener);
        }
    }

    public void addContactCapabilitiesListener(ContactId contact, ICapabilitiesListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "start : addContactCapabilitiesListener()");
            String number = PhoneUtils.extractNumberFromUri(contact.toString());
            RemoteCallbackList<ICapabilitiesListener> listeners = this.mContactCapalitiesListeners.get(number);
            if (listeners == null) {
                listeners = new RemoteCallbackList<>();
                this.mContactCapalitiesListeners.put(number, listeners);
            }
            listeners.register(listener);
        }
    }

    public void removeContactCapabilitiesListener(ContactId contact, ICapabilitiesListener listener) throws ServerApiException {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "start : removeContactCapabilitiesListener()");
            RemoteCallbackList<ICapabilitiesListener> listeners = this.mContactCapalitiesListeners.get(PhoneUtils.extractNumberFromUri(contact.toString()));
            if (listeners != null) {
                listeners.unregister(listener);
            }
        }
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public static com.gsma.services.rcs.capability.Capabilities transferCapabilities(Capabilities capabilities) {
        Set<String> extSet;
        int newFeatures;
        Capabilities capabilities2 = capabilities;
        long timestamp = 0;
        long feathres = capabilities.getFeature();
        List<String> extList = capabilities.getExtFeature();
        boolean bValid = capabilities.isAvailable();
        if (bValid) {
            extSet = new HashSet<>(extList);
        } else {
            extSet = new HashSet<>();
        }
        if (capabilities.getTimestamp() != null) {
            timestamp = capabilities.getTimestamp().getTime();
        }
        String str = LOG_TAG;
        Log.d(str, "transferCapabilities, bValid : " + bValid + ", bAutomata:" + false);
        int newFeatures2 = 0;
        if ((((long) Capabilities.FEATURE_ISH) & feathres) == ((long) Capabilities.FEATURE_ISH)) {
            newFeatures2 = 0 | 8;
        }
        if ((((long) Capabilities.FEATURE_VSH) & feathres) == ((long) Capabilities.FEATURE_VSH)) {
            newFeatures2 |= 16;
        }
        if ((((long) Capabilities.FEATURE_CHAT_CPM) & feathres) == ((long) Capabilities.FEATURE_CHAT_CPM) || (((long) Capabilities.FEATURE_CHAT_SIMPLE_IM) & feathres) == ((long) Capabilities.FEATURE_CHAT_SIMPLE_IM)) {
            newFeatures2 |= 2;
        }
        if ((((long) Capabilities.FEATURE_FT) & feathres) == ((long) Capabilities.FEATURE_FT)) {
            newFeatures2 |= 1;
        }
        if ((((long) Capabilities.FEATURE_GEOLOCATION_PUSH) & feathres) == ((long) Capabilities.FEATURE_GEOLOCATION_PUSH)) {
            newFeatures = newFeatures2 | 4;
        } else {
            newFeatures = newFeatures2;
        }
        if (capabilities2.hasFeature(Capabilities.FEATURE_ENRICHED_CALL_COMPOSER)) {
            extSet.add(SERVICE_ID_CALL_COMPOSER);
        }
        if (capabilities2.hasFeature(Capabilities.FEATURE_ENRICHED_SHARED_MAP)) {
            extSet.add(SERVICE_ID_SHARED_MAP);
        }
        if (capabilities2.hasFeature(Capabilities.FEATURE_ENRICHED_SHARED_SKETCH)) {
            extSet.add(SERVICE_ID_SHARED_SKETCH);
        }
        if (capabilities2.hasFeature(Capabilities.FEATURE_ENRICHED_POST_CALL)) {
            extSet.add(SERVICE_ID_POST_CALL);
        }
        return new com.gsma.services.rcs.capability.Capabilities(newFeatures, extSet, false, timestamp, bValid);
    }

    /* access modifiers changed from: private */
    public void notifyOwnCapabilityChange() {
        Log.d(LOG_TAG, "notifyOwnCapabilityChange");
        this.mContext.getContentResolver().notifyChange(Uri.withAppendedPath(CapabilitiesLog.CONTENT_URI, "own"), (ContentObserver) null);
    }
}
