package com.sec.internal.ims.entitlement.storagehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.log.IMSLog;
import java.util.UUID;

public class DeviceIdHelper {
    private static final int INTERVAL_BETWEEN_RETRY = 500;
    private static final String LOG_TAG = DeviceIdHelper.class.getSimpleName();
    private static final int RETRY_COUNT = 5;

    public static String getDeviceIdIfExists(Context context, int slotId) {
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp == null) {
            return null;
        }
        return sp.getString(slotId + ":" + "device_id", (String) null);
    }

    public static void makeDeviceId(Context context, int slotId) {
        String deviceId = generateDeviceId(context, slotId);
        String str = LOG_TAG;
        IMSLog.s(str, slotId, "makeDeviceId: " + deviceId);
        saveDeviceId(context, slotId, deviceId);
    }

    public static String getDeviceId(Context context, int slotId) {
        String deviceUid = null;
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp != null) {
            deviceUid = sp.getString(slotId + ":" + "device_id", (String) null);
        }
        if (deviceUid == null) {
            IMSLog.e(LOG_TAG, slotId, "getDeviceId is null");
            deviceUid = generateDeviceId(context, slotId);
            saveDeviceId(context, slotId, deviceUid);
        }
        if (deviceUid != null) {
            String str = LOG_TAG;
            IMSLog.s(str, slotId, "getDeviceId: " + deviceUid);
        }
        return deviceUid;
    }

    public static String getEncodedDeviceId(String deviceUid) {
        return Base64.encodeToString(deviceUid.getBytes(), 2);
    }

    private static void saveDeviceId(Context context, int slotId, String deviceId) {
        SharedPreferences sp = NSDSSharedPrefHelper.getSharedPref(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        if (sp == null) {
            IMSLog.e(LOG_TAG, slotId, "saveDeviceId: save is failed");
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(slotId + ":" + "device_id", deviceId);
        editor.commit();
    }

    private static String generateDeviceId(Context context, int slotId) {
        String deviceIdFromTm = queryDeviceIdFromTelephonyManager(context, slotId);
        for (int indRetry = 0; indRetry < 5; indRetry++) {
            deviceIdFromTm = queryDeviceIdFromTelephonyManager(context, slotId);
            if (!TextUtils.isEmpty(deviceIdFromTm)) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                String str = LOG_TAG;
                IMSLog.s(str, "generateDeviceId wait interrrupted:" + ie.getMessage());
            }
        }
        String deviceId = deviceIdFromTm;
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        return String.format("urn:uuid:%s", new Object[]{UUID.randomUUID().toString()});
    }

    private static String queryDeviceIdFromTelephonyManager(Context context, int slotId) {
        ITelephonyManager manager = TelephonyManagerWrapper.getInstance(context);
        try {
            if (TextUtils.isEmpty(manager.getDeviceId(slotId))) {
                return "";
            }
            String imei = manager.getImei(slotId);
            String meid = manager.getMeid(slotId);
            if (!TextUtils.isEmpty(imei)) {
                return String.format("urn:gsma:imei:%s-%s-%s", new Object[]{imei.substring(0, 8), imei.substring(8, 14), "0"});
            } else if (TextUtils.isEmpty(meid)) {
                return "";
            } else {
                return String.format("urn:device-id:meid:%s-%s-%s", new Object[]{meid.substring(0, 8), meid.substring(8, 14), "0"});
            }
        } catch (Exception ex) {
            String str = LOG_TAG;
            IMSLog.i(str, "getting deviceId failed:" + ex.getMessage());
            return "";
        }
    }
}
