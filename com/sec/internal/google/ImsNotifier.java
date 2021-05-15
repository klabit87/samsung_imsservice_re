package com.sec.internal.google;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.aidl.IImsRegistrationCallback;
import android.util.Log;
import com.android.ims.internal.IImsRegistrationListener;
import com.sec.ims.DialogEvent;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.google.IImsNotifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImsNotifier implements IImsNotifier {
    private static final String LOG_TAG = "ImsNotifier";
    /* access modifiers changed from: private */
    public GoogleImsService mGoogleImsService;
    private final Map<Integer, RemoteCallbackList<IImsRegistrationCallback>> mRegistrationCallbacks = new ConcurrentHashMap();

    public ImsNotifier(GoogleImsService googleImsService) {
        this.mGoogleImsService = googleImsService;
    }

    public void addCallback(int phoneId, IImsRegistrationCallback callback) {
        ImsRegistration[] registrationList = ImsRegistry.getRegistrationManager().getRegistrationInfo();
        if (!CollectionUtils.isNullOrEmpty((Object[]) registrationList)) {
            for (ImsRegistration reg : registrationList) {
                if (reg.getPhoneId() == phoneId && reg.hasVolteService()) {
                    try {
                        callback.onRegistered(GoogleImsService.getRegistrationTech(reg.getCurrentRat()));
                        if (this.mGoogleImsService.isOwnUrisChanged(phoneId, reg)) {
                            callback.onSubscriberAssociatedUriChanged(this.mGoogleImsService.mOwnUris.get(Integer.valueOf(phoneId)));
                        }
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        if (!this.mRegistrationCallbacks.containsKey(Integer.valueOf(phoneId))) {
            this.mRegistrationCallbacks.put(Integer.valueOf(phoneId), new RemoteCallbackList());
        }
        this.mRegistrationCallbacks.get(Integer.valueOf(phoneId)).register(callback);
    }

    public void removeCallback(int phoneId, IImsRegistrationCallback callback) {
        if (this.mRegistrationCallbacks.containsKey(Integer.valueOf(phoneId))) {
            this.mRegistrationCallbacks.get(Integer.valueOf(phoneId)).unregister(callback);
        }
    }

    public void notifyImsRegistration(ImsRegistration reg, boolean registered, ImsRegistrationError error) {
        if (reg.hasVolteService() && !reg.getImsProfile().hasEmergencySupport()) {
            notifyRegistrationCallback(reg, registered, error);
            notifyRegistrationListener(reg, registered, error);
        }
    }

    private void notifyRegistrationCallback(ImsRegistration reg, boolean registered, ImsRegistrationError error) {
        RemoteCallbackList<IImsRegistrationCallback> registrationCallback = this.mRegistrationCallbacks.get(Integer.valueOf(reg.getPhoneId()));
        if (registrationCallback != null) {
            int callbackSize = registrationCallback.beginBroadcast();
            for (int i = 0; i < callbackSize; i++) {
                try {
                    IImsRegistrationCallback callback = registrationCallback.getBroadcastItem(i);
                    if (callback != null) {
                        if (registered) {
                            callback.onRegistered(GoogleImsService.getRegistrationTech(reg.getCurrentRat()));
                            if (this.mGoogleImsService.isOwnUrisChanged(reg.getPhoneId(), reg)) {
                                callback.onSubscriberAssociatedUriChanged(this.mGoogleImsService.mOwnUris.get(Integer.valueOf(reg.getPhoneId())));
                            }
                        } else {
                            callback.onDeregistered(new ImsReasonInfo(error.getSipErrorCode(), error.getDeregistrationReason(), error.getSipErrorReason()));
                            callback.onSubscriberAssociatedUriChanged(new Uri[0]);
                        }
                    }
                } catch (RemoteException e) {
                }
            }
            registrationCallback.finishBroadcast();
        }
    }

    private void notifyRegistrationListener(ImsRegistration reg, boolean registered, ImsRegistrationError error) {
        int phoneId = reg.getPhoneId();
        for (Integer key : GoogleImsService.mServiceList.keySet()) {
            ServiceProfile service = GoogleImsService.mServiceList.get(key);
            IImsRegistrationListener listener = service.getRegistrationListener();
            if (phoneId == service.getPhoneId()) {
                if (registered) {
                    try {
                        listener.registrationConnectedWithRadioTech(GoogleImsService.getRegistrationTech(reg.getCurrentRat()));
                        if (this.mGoogleImsService.isOwnUrisChanged(phoneId, reg)) {
                            listener.registrationAssociatedUriChanged(this.mGoogleImsService.mOwnUris.get(Integer.valueOf(phoneId)));
                        }
                    } catch (RemoteException e) {
                    }
                } else {
                    listener.registrationDisconnected(DataTypeConvertor.convertToGoogleImsReason(error.getSipErrorCode()));
                    listener.registrationAssociatedUriChanged(new Uri[0]);
                }
            }
        }
    }

    public void onIncomingCall(int phoneId, int callId) {
        if (!SemSystemProperties.getBoolean("net.sip.vzthirdpartyapi", false)) {
            for (Integer key : GoogleImsService.mServiceList.keySet()) {
                ServiceProfile service = GoogleImsService.mServiceList.get(key);
                if (service != null && service.getPhoneId() == phoneId) {
                    Intent fillIn = new Intent();
                    fillIn.putExtra("android:imsCallID", Integer.toString(callId));
                    fillIn.putExtra("android:imsServiceId", key);
                    try {
                        IImsCallSession secCallSession = this.mGoogleImsService.mVolteServiceModule.getSessionByCallId(callId);
                        if (secCallSession != null) {
                            CallProfile cp = secCallSession.getCallProfile();
                            if (cp.getHistoryInfo() != null) {
                                fillIn.putExtra("com.samsung.telephony.extra.SEM_EXTRA_FORWARDED_CALL", cp.getHistoryInfo());
                            }
                            if (cp.getCallType() == 12) {
                                fillIn.putExtra("android:ussd", true);
                            }
                            this.mGoogleImsService.getCmcImsServiceUtil().onIncomingCall(phoneId, fillIn, service, secCallSession);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    try {
                        this.mGoogleImsService.mSecMmtelListener.get(Integer.valueOf(phoneId)).onIncomingCall(callId, fillIn.getExtras());
                    } catch (RemoteException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }

    public void onIncomingPreAlerting(ImsCallInfo callInfo, String remoteUri) {
        int callId = callInfo.getCallId();
        IImsCallSession secPendingSession = this.mGoogleImsService.mVolteServiceModule.getPendingSession(Integer.toString(callId));
        if (secPendingSession != null) {
            try {
                onIncomingCall(secPendingSession.getPhoneId(), callId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCdpnInfo(int phoneId, String calledPartyNumber, int timeout) {
        try {
            this.mGoogleImsService.mSecMmtelListener.get(Integer.valueOf(phoneId)).onCdpnInfo(calledPartyNumber.replaceAll("[^+?0-9]+", " "), timeout);
        } catch (RemoteException e) {
        }
    }

    public void onDialogEvent(DialogEvent de) {
        if (GoogleImsService.mMultEndPoints.get(Integer.valueOf(de.getPhoneId())) != null) {
            GoogleImsService.mMultEndPoints.get(Integer.valueOf(de.getPhoneId())).setDialogInfo(de, this.mGoogleImsService.getCmcTypeFromRegHandle(de.getRegId()));
            try {
                GoogleImsService.mMultEndPoints.get(Integer.valueOf(de.getPhoneId())).getImsExternalCallStateListener().onImsExternalCallStateUpdate(GoogleImsService.mMultEndPoints.get(Integer.valueOf(de.getPhoneId())).getExternalCallStateList());
            } catch (RemoteException e) {
            }
        }
    }

    public void notifyFeatureCapableChanged(int phoneId) {
        Log.i(LOG_TAG, "notifyFeatureCapableChanged, phoneId: " + phoneId);
        for (Integer key : GoogleImsService.mServiceList.keySet()) {
            ServiceProfile service = GoogleImsService.mServiceList.get(key);
            if (service != null && phoneId == service.getPhoneId()) {
                try {
                    service.getRegistrationListener().registrationFeatureCapabilityChanged(service.getServiceClass(), this.mGoogleImsService.convertCapaToFeature(this.mGoogleImsService.mCapabilities[phoneId]), (int[]) null);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void onP2pRegCompleteEvent() {
        Log.d(LOG_TAG, "onP2pRegCompleteEvent");
        if (this.mGoogleImsService.isEnabledWifiDirectFeature()) {
            new Handler().post(new Runnable() {
                public void run() {
                    try {
                        ImsNotifier.this.mGoogleImsService.getCmcImsServiceUtil().createCmcCallSession();
                    } catch (RemoteException e) {
                    }
                }
            });
        }
    }

    public void onP2pPushCallEvent(final DialogEvent de) {
        Log.d(LOG_TAG, "onP2pPushCallEvent");
        new Handler().post(new Runnable() {
            public void run() {
                try {
                    ImsNotifier.this.mGoogleImsService.preparePushCall(de);
                } catch (RemoteException e) {
                }
            }
        });
    }
}
