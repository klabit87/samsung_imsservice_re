package com.sec.internal.ims.servicemodules.options;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class CapabilityEvent {
    public static final int EVT_ATTACH_SERVICE_AVAILABILITY_LISTENER = 50;
    public static final int EVT_BOOT_COMPLETED = 12;
    public static final int EVT_CONTACT_CHANGED = 2;
    public static final int EVT_DDS_CHANGED = 15;
    public static final int EVT_DELAYED_SET_OWN_CAPABILITIES = 53;
    public static final int EVT_DETACH_SERVICE_AVAILABILITY_LISTENER = 51;
    public static final int EVT_EXCHANGE_CAPABILITIES_FOR_VSH = 14;
    public static final int EVT_INITIAL_CAPABILITIES_QUERY = 3;
    public static final int EVT_LAZY_CAPABILITIES_QUERY = 8;
    public static final int EVT_MESSAGEAPP_UPDATED = 40;
    public static final int EVT_NEW_CAPABILITIES_RECEIVED = 4;
    public static final int EVT_NEW_IMS_SETTINGS_AVAILABLE = 7;
    public static final int EVT_OWN_CAPABILITIES_CHANGED = 9;
    public static final int EVT_PERIODIC_POLL_TIMEOUT = 17;
    public static final int EVT_POLL = 1;
    public static final int EVT_REQUEST_CAPABILITIES = 6;
    public static final int EVT_REQUEST_LIST_CAPABILITIES = 33;
    public static final int EVT_RETRY_SYNC_CONTACT = 13;
    public static final int EVT_SEND_RCSC_INFO_TO_HQM = 16;
    public static final int EVT_SET_OWN_CAPABILITIES = 5;
    public static final int EVT_SYNC_CONTACT = 10;
    public static final int EVT_UPDATE_SERVICE_AVAILABILITY_LISTENER = 52;
    public static final int EVT_USER_SWITCHED = 11;
    private static final String LOG_TAG = "CapabilityEvent";

    static boolean handleEvent(Message msg, CapabilityDiscoveryModule capabilityDiscovery, CapabilityUtil capabilityUtil, ServiceAvailabilityEventListenerWrapper serviceAvailabilityEventListenerWrapper, int availablePhoneId) {
        Message message = msg;
        CapabilityDiscoveryModule capabilityDiscoveryModule = capabilityDiscovery;
        ServiceAvailabilityEventListenerWrapper serviceAvailabilityEventListenerWrapper2 = serviceAvailabilityEventListenerWrapper;
        int i = availablePhoneId;
        Log.i(LOG_TAG, "handleEvent: evt " + message.what);
        int i2 = message.what;
        if (i2 != 33) {
            boolean isAlwaysForce = false;
            if (i2 == 40) {
                capabilityDiscoveryModule.updateMsgAppInfo(false);
                return true;
            } else if (i2 == 8001 || i2 == 8002) {
                return true;
            } else {
                switch (i2) {
                    case 1:
                        capabilityDiscoveryModule.poll(((Boolean) message.obj).booleanValue(), i);
                        return true;
                    case 2:
                        capabilityDiscoveryModule.onContactChanged(false);
                        return true;
                    case 3:
                        capabilityDiscoveryModule.requestInitialCapabilitiesQuery(((Integer) message.obj).intValue());
                        return true;
                    case 4:
                        CapabilityUtil capabilityUtil2 = capabilityUtil;
                        Bundle b = (Bundle) message.obj;
                        ArrayList parcelableArrayList = b.getParcelableArrayList("URIS");
                        String pidf = b.getString("PIDF");
                        long features = b.getLong("FEATURES");
                        int phoneId = b.getInt("PHONEID");
                        int lastSeen = b.getInt("LASTSEEN");
                        String extFeature = b.getString("EXTFEATURE");
                        boolean isTokenUsed = b.getBoolean("ISTOKENUSED");
                        ArrayList parcelableArrayList2 = b.getParcelableArrayList("PAID");
                        Log.i(LOG_TAG, "handleEvent: lastSeen " + lastSeen);
                        int i3 = lastSeen;
                        Bundle bundle = b;
                        capabilityDiscovery.onUpdateCapabilities(parcelableArrayList, features, CapabilityConstants.CapExResult.values()[message.arg1], pidf, lastSeen, parcelableArrayList2, phoneId, isTokenUsed, extFeature);
                        return true;
                    case 5:
                        CapabilityUtil capabilityUtil3 = capabilityUtil;
                        int intValue = ((Integer) message.obj).intValue();
                        if (message.arg1 == 1) {
                            isAlwaysForce = true;
                        }
                        capabilityDiscoveryModule.setOwnCapabilities(intValue, isAlwaysForce);
                        return true;
                    case 6:
                        CapabilityUtil capabilityUtil4 = capabilityUtil;
                        IMSLog.i(LOG_TAG, message.arg2, "EVT_REQUEST_CAPABILITIES: refreshtype = " + message.arg1);
                        if (CapabilityRefreshType.values()[message.arg1] == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
                            isAlwaysForce = true;
                        }
                        capabilityDiscoveryModule.requestCapabilityExchange((ImsUri) message.obj, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, isAlwaysForce, message.arg2);
                        return true;
                    case 7:
                        CapabilityUtil capabilityUtil5 = capabilityUtil;
                        int pId = ((Integer) message.obj).intValue();
                        capabilityDiscoveryModule.loadConfig(pId);
                        capabilityDiscoveryModule.onImsSettingsUpdate(pId);
                        return true;
                    case 8:
                        CapabilityUtil capabilityUtil6 = capabilityUtil;
                        IMSLog.i(LOG_TAG, message.arg2, "EVT_LAZY_CAPABILITIES_QUERY: refreshtype = " + message.arg1);
                        if (CapabilityRefreshType.values()[message.arg1] == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
                            isAlwaysForce = true;
                        }
                        capabilityDiscoveryModule.requestCapabilityExchange((ImsUri) message.obj, CapabilityConstants.RequestType.REQUEST_TYPE_LAZY, isAlwaysForce, message.arg2);
                        return true;
                    case 9:
                        CapabilityUtil capabilityUtil7 = capabilityUtil;
                        capabilityDiscoveryModule.onOwnCapabilitiesChanged(message.arg1);
                        return true;
                    case 10:
                        CapabilityUtil capabilityUtil8 = capabilityUtil;
                        capabilityDiscoveryModule._syncContact((Mno) message.obj);
                        return true;
                    case 11:
                        CapabilityUtil capabilityUtil9 = capabilityUtil;
                        capabilityUtil.onUserSwitched();
                        return true;
                    case 12:
                        CapabilityUtil capabilityUtil10 = capabilityUtil;
                        capabilityDiscovery.onBootCompleted();
                        return true;
                    case 13:
                        CapabilityUtil capabilityUtil11 = capabilityUtil;
                        capabilityDiscovery.onRetrySyncContact();
                        return true;
                    case 14:
                        CapabilityUtil capabilityUtil12 = capabilityUtil;
                        capabilityDiscoveryModule.exchangeCapabilitiesForVSH(message.arg1, ((Boolean) message.obj).booleanValue());
                        return true;
                    case 15:
                        CapabilityUtil capabilityUtil13 = capabilityUtil;
                        capabilityDiscovery.ddsChangedCheckRcsSwitch();
                        return true;
                    case 16:
                        capabilityUtil.sendRCSCInfoToHQM(message.arg1);
                        return true;
                    case 17:
                        capabilityDiscoveryModule.deleteNonRcsDataFromContactDB(i);
                        return true;
                    default:
                        switch (i2) {
                            case 50:
                                serviceAvailabilityEventListenerWrapper2.attachServiceAvailabilityEventListener(message.arg1, (String) message.obj);
                                return true;
                            case 51:
                                serviceAvailabilityEventListenerWrapper2.detachServiceAvailabilityEventListener(message.arg1);
                                return true;
                            case 52:
                                serviceAvailabilityEventListenerWrapper2.updateServiceAvailabilityEventListener(message.arg1);
                                return true;
                            case 53:
                                capabilityDiscoveryModule.handleDelayedSetOwnCapabilities(((Integer) message.obj).intValue());
                                return true;
                            default:
                                Log.e(LOG_TAG, "handleEvent: Undefined message.");
                                return false;
                        }
                }
            }
        } else {
            capabilityDiscoveryModule.requestCapabilityExchange((List) message.obj, CapabilityConstants.RequestType.REQUEST_TYPE_NONE, message.arg1);
            return true;
        }
    }
}
