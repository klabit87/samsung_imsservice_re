package com.sec.internal.helper.os;

import android.os.Parcel;
import android.telephony.ServiceState;
import android.util.Log;
import com.sec.ims.extensions.ReflectionUtils;
import java.lang.reflect.InvocationTargetException;

public class ServiceStateWrapper {
    public static final int NR_5G_BEARER_STATUS_ALLOCATED = 1;
    public static final int NR_5G_BEARER_STATUS_MMW_ALLOCATED = 2;
    public static final int NR_5G_BEARER_STATUS_NOT_ALLOCATED = 0;
    public static final int ROAMING_TYPE_DOMESTIC = 2;
    public static final int ROAMING_TYPE_INTERNATIONAL = 3;
    public static final int ROAMING_TYPE_NOT_ROAMING = 0;
    public static final int ROAMING_TYPE_UNKNOWN = 1;
    private static final String TAG = ServiceStateWrapper.class.getSimpleName();
    private final ServiceState mServiceState;

    public ServiceStateWrapper(ServiceState serviceState) {
        this.mServiceState = serviceState;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mServiceState.writeToParcel(out, flags);
    }

    public int describeContents() {
        return this.mServiceState.describeContents();
    }

    public int getState() {
        return this.mServiceState.getState();
    }

    public void setState(int state) {
        this.mServiceState.setState(state);
    }

    public void setStateOutOfService() {
        this.mServiceState.setStateOutOfService();
    }

    public void setStateOff() {
        this.mServiceState.setStateOff();
    }

    public String getOperatorAlphaLong() {
        return this.mServiceState.getOperatorAlphaLong();
    }

    public boolean getRoaming() {
        return this.mServiceState.getRoaming();
    }

    public void setRoaming(boolean roaming) {
        this.mServiceState.setRoaming(roaming);
    }

    public void setOperatorName(String longName, String shortName, String numeric) {
        this.mServiceState.setOperatorName(longName, shortName, numeric);
    }

    public String getOperatorNumeric() {
        return this.mServiceState.getOperatorNumeric();
    }

    public boolean getIsManualSelection() {
        return this.mServiceState.getIsManualSelection();
    }

    public void setIsManualSelection(boolean isManual) {
        this.mServiceState.setIsManualSelection(isManual);
    }

    public String getOperatorAlphaShort() {
        return this.mServiceState.getOperatorAlphaShort();
    }

    public String toString() {
        return this.mServiceState.toString();
    }

    public int getDataRegState() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getDataRegState", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getVoiceRegState() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getVoiceRegState", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getLteImsVoiceAvail() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getLteImsVoiceAvail", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getLteIsEbSupported() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getLteIsEbSupported", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            String str = TAG;
            Log.e(str, e.getClass().getSimpleName() + "!! " + e.getMessage());
            return 1;
        }
    }

    public boolean getDataRoaming() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(ServiceState.class.getMethod("getDataRoaming", new Class[0]), this.mServiceState, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean getVoiceRoaming() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(ServiceState.class.getMethod("getVoiceRoaming", new Class[0]), this.mServiceState, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getDataNetworkType() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getDataNetworkType", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getMobileDataRegState() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getMobileDataRegState", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public int getRilVoiceRadioTechnology() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getRilVoiceRadioTechnology", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getRilMobileDataRadioTechnology() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getRilMobileDataRadioTechnology", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int rilRadioTechnologyToNetworkType(int rt) {
        Class<ServiceState> cls = ServiceState.class;
        try {
            return ((Integer) cls.getDeclaredMethod("rilRadioTechnologyToNetworkType", new Class[]{Integer.TYPE}).invoke((Object) null, new Object[]{Integer.valueOf(rt)})).intValue();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getSnapshotStatus() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getSnapshotStatus", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getVoiceNetworkType() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getVoiceNetworkType", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean isPsOnlyReg() {
        try {
            return ((Boolean) ReflectionUtils.invoke2(ServiceState.class.getMethod("isPsOnlyReg", new Class[0]), this.mServiceState, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getVoiceRoamingType() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getVoiceRoamingType", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getNrBearerStatus() {
        try {
            return ((Integer) ReflectionUtils.invoke2(ServiceState.class.getMethod("getNrBearerStatus", new Class[0]), this.mServiceState, new Object[0])).intValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
