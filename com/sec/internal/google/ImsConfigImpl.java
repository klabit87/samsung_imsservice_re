package com.sec.internal.google;

import android.content.ContentValues;
import android.os.RemoteException;
import com.android.ims.ImsConfigListener;
import com.android.ims.internal.IImsConfig;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.ims.registry.ImsRegistry;

public class ImsConfigImpl extends IImsConfig.Stub {
    private int mPhoneId = 0;

    public ImsConfigImpl(int phoneId) {
        this.mPhoneId = phoneId;
    }

    public int getProvisionedValue(int item) throws RemoteException {
        if (item == 10) {
            return getConfigValue("93");
        }
        if (item != 11) {
            return 0;
        }
        return getConfigValue("94");
    }

    public String getProvisionedStringValue(int item) throws RemoteException {
        return null;
    }

    public int setProvisionedValue(int item, int value) throws RemoteException {
        int value2;
        if (item != 66) {
            return 0;
        }
        if (value == 1) {
            value2 = Extensions.TelecomManager.RTT_MODE;
        } else {
            value2 = Extensions.TelecomManager.RTT_MODE_OFF;
        }
        ImsRegistry.setRttMode(this.mPhoneId, value2);
        return 0;
    }

    public int setProvisionedStringValue(int item, String value) throws RemoteException {
        return 0;
    }

    public void getFeatureValue(int feature, int network, ImsConfigListener listener) throws RemoteException {
        String imsService = "";
        if (feature == 0 || feature == 2) {
            imsService = "mmtel";
        } else if (feature == 1 || feature == 3) {
            imsService = "mmtel-video";
        } else if (feature == 6 || feature == 7) {
            imsService = "smsip";
        }
        boolean ret = ImsRegistry.isServiceAvailable(imsService, network, this.mPhoneId);
        if (listener != null) {
            listener.onGetFeatureResponse(feature, network, ret, 0);
        }
    }

    public void setFeatureValue(int feature, int network, int value, ImsConfigListener listener) throws RemoteException {
    }

    public boolean getVolteProvisioned() throws RemoteException {
        return getConfigValue("93") == 1;
    }

    public void getVideoQuality(ImsConfigListener listener) throws RemoteException {
    }

    public void setVideoQuality(int quality, ImsConfigListener listener) throws RemoteException {
    }

    private int getConfigValue(String item) {
        ContentValues value = ImsRegistry.getConfigValues(new String[]{item}, this.mPhoneId);
        Integer readValue = null;
        if (value != null) {
            readValue = value.getAsInteger(item);
        }
        if (readValue == null) {
            return 0;
        }
        return readValue.intValue();
    }

    private String getGlobalSettingsValueToString(String item, String defVal) {
        return ImsRegistry.getString(this.mPhoneId, item, defVal);
    }
}
