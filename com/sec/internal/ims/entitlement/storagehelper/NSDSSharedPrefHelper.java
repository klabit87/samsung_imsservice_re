package com.sec.internal.ims.entitlement.storagehelper;

import android.content.Context;
import android.content.SharedPreferences;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.concurrent.atomic.AtomicBoolean;

public class NSDSSharedPrefHelper {
    private static final String LOG_TAG = NSDSSharedPrefHelper.class.getSimpleName();
    private static AtomicBoolean[] mIsVolteEntitled = {new AtomicBoolean(false), new AtomicBoolean(false)};
    private static AtomicBoolean[] mIsVowifiEntitled = {new AtomicBoolean(false), new AtomicBoolean(false)};

    public static SharedPreferences getSharedPref(Context context, String prefName, int mode) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        return context.createCredentialProtectedStorageContext().getSharedPreferences(prefName, mode);
    }

    public static String getInDe(Context context, String prefName, String deviceId, String key) {
        return context.createDeviceProtectedStorageContext().getSharedPreferences(prefName, 0).getString(getKey(deviceId, key), (String) null);
    }

    public static String get(Context context, String deviceId, String key) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        return context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(getKey(deviceId, key), (String) null);
    }

    private static String getKey(String deviceId, String key) {
        return deviceId + ":" + key;
    }

    public static void saveInDe(Context context, String prefName, String deviceId, String key, String value) {
        SharedPreferences.Editor editor = context.createDeviceProtectedStorageContext().getSharedPreferences(prefName, 0).edit();
        editor.putString(getKey(deviceId, key), value);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.s(str, "saved preference with key:" + key + " Value:" + value);
    }

    public static void save(Context context, String deviceId, String key, String value) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.putString(getKey(deviceId, key), value);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "saved preference with key:" + key + " Value:" + value);
        }
    }

    public static void save(Context context, String deviceId, String key, boolean value) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.putBoolean(getKey(deviceId, key), value);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "saved preference with key:" + key + " Value:" + value);
        }
    }

    public static void remove(Context context, String deviceId, String key) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.remove(getKey(deviceId, key));
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "removed preference with key:" + key);
        }
    }

    public static boolean isDeviceInActivationProgress(Context context, String devcieUid) {
        String isInProgress;
        if (NSDSConfigHelper.isUserUnlocked(context) && (isInProgress = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(getKey(devcieUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE), (String) null)) != null && isInProgress.equalsIgnoreCase(NSDSNamespaces.NSDSDeviceState.ACTIVATION_IN_PROGRESS)) {
            return true;
        }
        return false;
    }

    public static boolean isDeviceInEntitlementProgress(Context context, String deviceUid) {
        String isInProgress;
        if (NSDSConfigHelper.isUserUnlocked(context) && (isInProgress = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE), (String) null)) != null && isInProgress.equalsIgnoreCase(NSDSNamespaces.NSDSDeviceState.ENTITLMENT_IN_PROGRESS)) {
            return true;
        }
        return false;
    }

    public static boolean isVoWifiServiceProvisioned(Context context, String deviceUid) {
        String isServiceProvisioned;
        if (NSDSConfigHelper.isUserUnlocked(context) && (isServiceProvisioned = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE), (String) null)) != null && isServiceProvisioned.equalsIgnoreCase(NSDSNamespaces.NSDSDeviceState.SERVICE_PROVISIONED)) {
            return true;
        }
        return false;
    }

    public static boolean isDeviceActivated(Context context, String deviceUid) {
        String isActivated;
        if (NSDSConfigHelper.isUserUnlocked(context) && (isActivated = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE), (String) null)) != null && isActivated.equalsIgnoreCase(NSDSNamespaces.NSDSDeviceState.ACTIVATED)) {
            return true;
        }
        return false;
    }

    public static boolean isGcmTokenSentToServer(Context context, String deviceUid) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return false;
        }
        return context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getBoolean(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER), false);
    }

    public static boolean isSimSwapPending(Context context, String deviceUid) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return false;
        }
        return context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getBoolean(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PEDNING_SIM_SWAP), false);
    }

    public static void clearSimSwapPending(Context context, String deviceUid) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.remove(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PEDNING_SIM_SWAP));
            editor.commit();
            IMSLog.s(LOG_TAG, "cleared pending_sim_swap form shared pref ");
        }
    }

    public static void clearEntitlementServerUrl(Context context, String deviceUid) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            Context ceContext = context.createCredentialProtectedStorageContext();
            String key = getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL);
            SharedPreferences.Editor editor = ceContext.getSharedPreferences(NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL, 0).edit();
            editor.remove(key);
            editor.commit();
            IMSLog.s(LOG_TAG, "cleared entitlement server Url form shared pref ");
        }
    }

    public static void setEntitlementServerUrl(Context context, String deviceUid, String url) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            Context ceContext = context.createCredentialProtectedStorageContext();
            String key = getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL);
            SharedPreferences.Editor editor = ceContext.getSharedPreferences(NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL, 0).edit();
            editor.putString(key, url);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "setEntitlementServerUrl: " + url);
        }
    }

    public static String getEntitlementServerUrl(Context context, String deviceUid, String defaultEntitlementServerUrl) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        Context ceContext = context.createCredentialProtectedStorageContext();
        String entitlmentUrl = ceContext.getSharedPreferences(NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL, 0).getString(getKey(deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_SERVER_URL), (String) null);
        String str = LOG_TAG;
        IMSLog.s(str, "getEntitlementServerUrl: " + entitlmentUrl);
        if (entitlmentUrl == null) {
            return defaultEntitlementServerUrl;
        }
        return entitlmentUrl;
    }

    public static boolean getVoWiFiEntitlement(int slotId) {
        boolean value = mIsVowifiEntitled[slotId].get();
        String str = LOG_TAG;
        IMSLog.i(str, slotId, "getVoWiFiEntitlement: " + value);
        return value;
    }

    public static boolean getVoWiFiEntitlement(Context context, int slotIdx) {
        boolean value = context.getSharedPreferences("entitlement_completed_" + slotIdx, 0).getBoolean(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, false);
        String str = LOG_TAG;
        IMSLog.i(str, slotIdx, "getVoWiFiEntitlement: " + value);
        return value;
    }

    public static boolean getVoLteEntitlement(Context context, int slotIdx) {
        boolean value = context.getSharedPreferences("entitlement_completed_" + slotIdx, 0).getBoolean(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, false);
        String str = LOG_TAG;
        IMSLog.i(str, slotIdx, "getVoLteEntitlement: " + value);
        return value;
    }

    public static void setVoWiFiEntitlement(Context context, boolean value, int slotIdx) {
        mIsVowifiEntitled[slotIdx].set(value);
        SharedPreferences.Editor editor = context.getSharedPreferences("entitlement_completed_" + slotIdx, 0).edit();
        editor.putBoolean(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, value);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.i(str, slotIdx, "setVoWiFiEntitlement: " + value);
    }

    public static void setVoLteEntitlement(Context context, boolean value, int slotIdx) {
        mIsVolteEntitled[slotIdx].set(value);
        SharedPreferences.Editor editor = context.getSharedPreferences("entitlement_completed_" + slotIdx, 0).edit();
        editor.putBoolean(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, value);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.i(str, slotIdx, "setVoLteEntitlement: " + value);
    }

    public static String getGcmSenderId(Context context, String deviceUid, String defaultSenderId) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        SharedPreferences sp = context.createCredentialProtectedStorageContext().getSharedPreferences("gcm_sender_id", 0);
        String senderId = sp.getString("gcm_sender_id", (String) null);
        if (senderId != null) {
            return senderId;
        }
        String senderId2 = defaultSenderId;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(getKey(deviceUid, "gcm_sender_id"), senderId2);
        editor.commit();
        String str = LOG_TAG;
        IMSLog.s(str, "saved preference with key:" + "gcm_sender_id" + " Value:" + senderId2);
        return senderId2;
    }

    public static void savePrefForSlot(Context context, int slotId, String prefName, String imsi) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.putString(slotId + ":" + prefName, imsi);
            editor.commit();
        }
    }

    public static String getPrefForSlot(Context context, int slotId, String prefName) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        Context ceContext = context.createCredentialProtectedStorageContext();
        return ceContext.getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(slotId + ":" + prefName, "");
    }

    public static void removePrefForSlot(Context context, int slotId, String key) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.remove(slotId + ":" + key);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "removed preference with key: " + slotId + ":" + key);
        }
    }

    public static void saveAkaToken(Context context, String imsi, String akaToken) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.putString(imsi, akaToken);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "saved aka token, " + akaToken + ", with imsi: " + imsi);
        }
    }

    public static String getAkaToken(Context context, String imsi) {
        if (!NSDSConfigHelper.isUserUnlocked(context)) {
            return null;
        }
        return context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).getString(imsi, (String) null);
    }

    public static void removeAkaToken(Context context, String imsi) {
        if (NSDSConfigHelper.isUserUnlocked(context)) {
            SharedPreferences.Editor editor = context.createCredentialProtectedStorageContext().getSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0).edit();
            editor.remove(imsi);
            editor.commit();
            String str = LOG_TAG;
            IMSLog.s(str, "removed aka token with imsi: " + imsi);
        }
    }

    public static boolean migrationToCe(Context context) {
        if (!context.createCredentialProtectedStorageContext().moveSharedPreferencesFrom(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF)) {
            IMSLog.e(LOG_TAG, "Failed to maigrate shared preferences.");
            return false;
        } else if (context.deleteSharedPreferences(NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF)) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, "Failed delete shared preferences on DE.");
            return false;
        }
    }
}
