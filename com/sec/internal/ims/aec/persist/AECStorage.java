package com.sec.internal.ims.aec.persist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.aec.util.ExternalStorage;
import java.util.Map;

public class AECStorage {
    private final Context mContext;
    private final int mPhoneId;
    private final Map<String, String> mProviderSettings;
    private final String mSharedPreference;

    public AECStorage(Context context, int phoneId, String mno) {
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mSharedPreference = String.format(AECNamespace.Template.AEC_RESULT, new Object[]{Integer.valueOf(phoneId)});
        this.mProviderSettings = ProviderSettings.getSettingMap(this.mContext, phoneId, mno);
    }

    private SharedPreferences getSharedPreferences() {
        return this.mContext.getSharedPreferences(this.mSharedPreference, 0);
    }

    private synchronized int getIntValue(String key) {
        try {
        } catch (NumberFormatException e) {
            return 0;
        }
        return Integer.parseInt(getSharedPreferences().getString(key, "0"));
    }

    private synchronized String getStringValue(String key) {
        return getSharedPreferences().getString(key, "");
    }

    private synchronized void setStringValue(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private synchronized void setMap(Map<String, String> map) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();
    }

    public void setConfiguration(Map<String, String> map) {
        setMap(map);
    }

    public void setDefaultValues(String version) {
        setStringValue("root/vers/version", version);
        setStringValue("root/vers/validity", "0");
        setStringValue("root/token/token", "");
        setStringValue(AECNamespace.Path.TOKEN_VALIDITY, "0");
        setStringValue(AECNamespace.Path.VOLTE_ENTITLEMENT_STATUS, "0");
        setStringValue(AECNamespace.Path.VOWIFI_ENTITLEMENT_STATUS, "0");
        setStringValue(AECNamespace.Path.SMSOIP_ENTITLEMENT_STATUS, "0");
        setStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN, "");
    }

    public void setImsi(String imsi) {
        setStringValue(AECNamespace.Path.IMSI, imsi);
    }

    public void setAkaToken(String token) {
        setStringValue("root/token/token", token);
    }

    public void setHttpResponse(int response) {
        setStringValue(AECNamespace.Path.RESPONSE, Integer.toString(response));
    }

    public void setVersion(String version) {
        setStringValue("root/vers/version", version);
    }

    public void setNotifToken(String notifToken) {
        setStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN, notifToken);
        ExternalStorage.setNotifToken(this.mPhoneId, notifToken);
    }

    public Bundle getStoredConfiguration() {
        Bundle bundle = new Bundle();
        bundle.putInt("phoneId", this.mPhoneId);
        bundle.putInt("version", getVersion());
        bundle.putInt(AECNamespace.BundleData.HTTP_RESPONSE, getHttpResponse());
        bundle.putInt(AECNamespace.BundleData.VOLTE_ENTITLEMENT_STATUS, getVoLTEEntitlementStatus());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ACTIVATION_MODE, getVoWiFiActivationMode());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ENTITLEMENT_STATUS, getVoWiFiEntitlementStatus());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_PROV_STATUS, getVoWiFiProvStatus());
        bundle.putInt("tc_status", getVoWiFiTcStatus());
        bundle.putInt(AECNamespace.BundleData.VOWIFI_ADDR_STATUS, getVoWiFiAddrStatus());
        bundle.putString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_URL, getServiceFlowURL());
        bundle.putString(AECNamespace.BundleData.VOWFIFI_SERVICEFLOW_USERDATA, getServiceFlowUserData());
        bundle.putString(AECNamespace.BundleData.VOWIFI_MESSAGE_FOR_INCOMPATIBLE, getMessageForIncompatible());
        bundle.putInt(AECNamespace.BundleData.SMSOIP_ENTITLEMENT_STATUS, getSMSoIPEntitlementStatus());
        bundle.putInt(AECNamespace.BundleData.VOLTE_AUTO_ON, getVoLTEAutoOn() ? 1 : 0);
        bundle.putInt(AECNamespace.BundleData.VOWIFI_AUTO_ON, getVoWiFiAutoOn() ? 1 : 0);
        return bundle;
    }

    public String getImsi() {
        return getStringValue(AECNamespace.Path.IMSI);
    }

    public String getAkaToken() {
        return getStringValue("root/token/token");
    }

    public int getHttpResponse() {
        String response = getStringValue(AECNamespace.Path.RESPONSE);
        if (TextUtils.isEmpty(response)) {
            return 0;
        }
        return Integer.parseInt(response);
    }

    public int getVersion() {
        String version = getStringValue("root/vers/version");
        if (TextUtils.isEmpty(version)) {
            version = "0";
        }
        return Integer.parseInt(version);
    }

    public String getNotifToken() {
        return getStringValue(AECNamespace.Path.PUSH_NOTIF_TOKEN);
    }

    public String getTimeStamp() {
        return getStringValue(AECNamespace.Path.TIMESTAMP);
    }

    public int getVersionValidity() {
        return getIntValue("root/vers/validity");
    }

    public int getTokenValidity() {
        return getIntValue(AECNamespace.Path.TOKEN_VALIDITY);
    }

    public int getVoLTEEntitlementStatus() {
        return getIntValue(AECNamespace.Path.VOLTE_ENTITLEMENT_STATUS);
    }

    public int getSMSoIPEntitlementStatus() {
        return getIntValue(AECNamespace.Path.SMSOIP_ENTITLEMENT_STATUS);
    }

    public int getVoWiFiActivationMode() {
        return getVoWiFiActivationMode(getVoWiFiEntitlementStatus(), getVoWiFiProvStatus(), getVoWiFiTcStatus(), getVoWiFiAddrStatus());
    }

    private int getVoWiFiEntitlementStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_ENTITLEMENT_STATUS);
    }

    private int getVoWiFiProvStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_PROV_STATUS);
    }

    private int getVoWiFiTcStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_TC_STATUS);
    }

    private int getVoWiFiAddrStatus() {
        return getIntValue(AECNamespace.Path.VOWIFI_ADDR_STATUS);
    }

    private int getVoWiFiActivationMode(int entitle, int prov, int tc, int addr) {
        if (getVersion() < 0 || entitle == 2) {
            return 0;
        }
        if (entitle == 0) {
            if (prov == 0 || tc == 0 || addr == 0) {
                return 2;
            }
            if (prov == 3 || tc == 3 || addr == 3) {
                return 1;
            }
        }
        if (entitle == 1 && ((prov == 1 || prov == 2) && ((tc == 1 || tc == 2) && (addr == 1 || addr == 2)))) {
            return 3;
        }
        return 0;
    }

    private String getServiceFlowURL() {
        return getStringValue(AECNamespace.Path.VOWIFI_SERVICEFLOW_URL);
    }

    private String getServiceFlowUserData() {
        return getStringValue(AECNamespace.Path.VOWIFI_SERVICEFLOW_USERDATA);
    }

    private String getMessageForIncompatible() {
        return getStringValue(AECNamespace.Path.VOWIFI_MESSAGE_FOR_INCOMPATIBLE);
    }

    public String getEntitlementVersion() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "1.0";
        }
        return map.getOrDefault("entitlement_version", "1.0");
    }

    public String getAppId() {
        StringBuilder appId = new StringBuilder();
        if (getEntitlementForVoLte()) {
            appId.append(AECNamespace.ApplicationId.APP_ID_VOLTE);
        }
        if (getEntitlementForVoWiFi()) {
            if (appId.length() > 0) {
                appId.append(",");
            }
            appId.append(AECNamespace.ApplicationId.APP_ID_VOWIFI);
        }
        if (getEntitlementForSMSoIp()) {
            if (appId.length() > 0) {
                appId.append(",");
            }
            appId.append(AECNamespace.ApplicationId.APP_ID_SMSOIP);
        }
        return appId.toString();
    }

    public String getNotifSenderId() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault(AECNamespace.ProviderSettings.NOTIF_SENDER_ID, "");
    }

    public String getNotifAction() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return "";
        }
        return map.getOrDefault("notif_action", "");
    }

    public boolean getPsDataOff() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.PS_DATA_OFF));
    }

    public boolean getPsDataRoaming() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.PS_DATA_ROAMING));
    }

    public boolean getEntitlementInitFromApp() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_INIT_FROM_APP));
    }

    public boolean getEntitlementForVoLte() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_VOLTE));
    }

    public boolean getEntitlementForVoWiFi() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_VOWIFI));
    }

    public boolean getEntitlementForSMSoIp() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.ENTITLEMENT_FOR_SMSOIP));
    }

    private boolean getVoLTEAutoOn() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.VOLTE_AUTO_ON));
    }

    private boolean getVoWiFiAutoOn() {
        Map<String, String> map = this.mProviderSettings;
        if (map == null) {
            return false;
        }
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(map.get(AECNamespace.ProviderSettings.VOWIFI_AUTO_ON));
    }
}
