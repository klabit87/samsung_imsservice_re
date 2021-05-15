package com.sec.internal.helper.os;

import android.content.ContentResolver;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import java.util.List;

public interface ITelephonyManager {
    void clearCache();

    String getAidForAppType(int i);

    String getAidForAppType(int i, int i2);

    List<CellInfo> getAllCellInfo();

    String getBtid();

    int getCallState();

    int getCallState(int i);

    CellLocation getCellLocationBySubId(int i);

    int getCurrentPhoneTypeForSlot(int i);

    int getDataNetworkType();

    int getDataNetworkType(int i);

    int getDataServiceState();

    int getDataServiceState(int i);

    String getDeviceId();

    String getDeviceId(int i);

    String getGid2(int i);

    String getGroupIdLevel1();

    String getGroupIdLevel1(int i);

    String getIccAuthentication(int i, int i2, int i3, String str);

    String getImei();

    String getImei(int i);

    int getIntAtIndex(ContentResolver contentResolver, String str, int i);

    String getIsimDomain();

    String getIsimDomain(int i);

    String getIsimImpi();

    String getIsimImpi(int i);

    String[] getIsimImpu();

    String[] getIsimImpu(int i);

    String[] getIsimPcscf();

    String getKeyLifetime();

    String getLine1Number();

    String getMeid();

    String getMeid(int i);

    String getMsisdn();

    String getMsisdn(int i);

    String getNetworkCountryIso();

    String getNetworkCountryIso(int i);

    String getNetworkOperator(int i);

    String getNetworkOperatorForPhone(int i);

    int getNetworkType();

    int getPhoneCount();

    byte[] getRand();

    int getServiceState();

    int getServiceStateForSubscriber(int i);

    String getSimCountryIso();

    String getSimCountryIsoForPhone(int i);

    String getSimOperator();

    String getSimOperator(int i);

    String getSimOperatorName(int i);

    String getSimSerialNumber();

    String getSimSerialNumber(int i);

    int getSimState();

    int getSimState(int i);

    String getSubscriberId();

    String getSubscriberId(int i);

    String getSubscriberIdForUiccAppType(int i, int i2);

    String getTelephonyProperty(int i, String str, String str2);

    int getVoiceNetworkType();

    int getVoiceNetworkType(int i);

    boolean hasCall(String str);

    boolean iccCloseLogicalChannel(int i, int i2);

    int iccOpenLogicalChannelAndGetChannel(int i, String str);

    String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str);

    boolean isGbaSupported();

    boolean isGbaSupported(int i);

    boolean isNetworkRoaming();

    boolean isNetworkRoaming(int i);

    boolean isVoiceCapable();

    void setCallState(int i);

    void setGbaBootstrappingParams(byte[] bArr, String str, String str2);

    void setImsRegistrationState(int i, boolean z);

    boolean setPreferredNetworkType(int i, int i2);

    void setRadioPower(boolean z);

    void setTelephonyProperty(int i, String str, String str2);

    boolean validateMsisdn(int i);
}
