package com.sec.internal.interfaces.ims.config;

import android.net.Network;
import android.os.Bundle;
import android.os.Message;
import android.util.Pair;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.ims.config.params.ACSConfig;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.List;

public interface IConfigModule extends ISequentialInitializable {
    public static final int HANDLE_AUTO_CONFIG_CHANGE_OPMODE = 8;
    public static final int HANDLE_AUTO_CONFIG_COMPLETE = 3;
    public static final int HANDLE_AUTO_CONFIG_DUALSIM = 9;
    public static final int HANDLE_AUTO_CONFIG_FORCE = 1;
    public static final int HANDLE_AUTO_CONFIG_FORCE_SMS_PUSH = 4;
    public static final int HANDLE_AUTO_CONFIG_INIT = 0;
    public static final int HANDLE_AUTO_CONFIG_REGISTER_LISTENER = 5;
    public static final int HANDLE_AUTO_CONFIG_RESTART = 19;
    public static final int HANDLE_AUTO_CONFIG_SEND_MSISDN_NUMBER = 20;
    public static final int HANDLE_AUTO_CONFIG_SEND_VERIFICATION_CODE = 7;
    public static final int HANDLE_AUTO_CONFIG_SHOW_MSIDN_DIALOG = 16;
    public static final int HANDLE_AUTO_CONFIG_SMS_PUSH = 21;
    public static final int HANDLE_AUTO_CONFIG_START = 2;
    public static final int HANDLE_AUTO_CONFIG_START_WITH_SUITABLE_NETWORK = 17;
    public static final int HANDLE_AUTO_CONFIG_UNREGISTER_LISTENER = 6;
    public static final int HANDLE_EVENT_BOOT_COMPLETED = 23;
    public static final int HANDLE_EVENT_CONFIGURATION_COMPLETED = 13;
    public static final int HANDLE_EVENT_DDS_CHANGED = 10;
    public static final int HANDLE_EVENT_DEFAULT_MSG_APP_CHANGED = 18;
    public static final int HANDLE_EVENT_FORBIDDEN_ERROR_OCCURRED = 22;
    public static final int HANDLE_EVENT_NETWORK_AVAILABLE = 24;
    public static final int HANDLE_EVENT_NETWORK_LOST = 25;
    public static final int HANDLE_EVENT_NEW_CONFIGURATION_REQUIRED = 15;
    public static final int HANDLE_EVENT_SIM_READY = 11;
    public static final int HANDLE_EVENT_SIM_REFRESH = 12;
    public static final int HANDLE_EVENT_TELEPHONY_CALL_STATUS_CHANGED = 14;
    public static final String MSISDN_FROM_PAU = "msisdn_from_pau";
    public static final int RCS_CONFIG_SOURCE_LOCAL_FILE = 2;
    public static final int RCS_CONFIG_SOURCE_LOCAL_FILE_FROM_SDCARD = 3;
    public static final int RCS_CONFIG_SOURCE_LOCAL_SERVER = 1;
    public static final int RCS_CONFIG_SOURCE_REMOTE_SERVER = 0;
    public static final int RECONFIGURE_REQUEST_ACCEPTED = 1;
    public static final int RECONFIGURE_REQUEST_REJECTED = 0;

    void changeOpMode(boolean z, int i, int i2);

    ACSConfig getAcsConfig(int i);

    DiagnosisConstants.RCSA_ATRE getAcsTryReason(int i);

    Pair<Network, Integer> getAvailableNetwork(int i);

    Network getAvailableNetworkForNetworkType(int i, int i2);

    Integer getRcsConfVersion(int i);

    String getRcsConfigMark(int i);

    String getRcsProfile(int i);

    IStorageAdapter getStorage(int i);

    boolean isRcsEnabled(int i);

    boolean isSimMoActivatedAndRcsEurSupported(int i, ISimManager iSimManager, IRegistrationManager iRegistrationManager);

    boolean isValidAcsVersion(int i);

    boolean isValidConfigDb(int i);

    boolean isWaitAutoconfig(IRegisterTask iRegisterTask);

    Message obtainConfigMessage(int i, Bundle bundle);

    void onNewRcsConfigurationNeeded(String str, String str2, Message message);

    void onRegistrationStatusChanged(boolean z, int i, ImsRegistration imsRegistration);

    void resetAcsTryReason(int i);

    void resetReadyStateCommand(int i);

    void sendConfigMessage(int i, int i2);

    void setAcsTryReason(int i, DiagnosisConstants.RCSA_ATRE rcsa_atre);

    void setDualSimRcsAutoConfig(boolean z);

    void setMsisdnFromPau(ImsRegistration imsRegistration);

    void setRegisterFromApp(boolean z, int i);

    void showMSIDSNDialog();

    void startAcs(int i);

    void startAutoConfig(boolean z, Message message, int i);

    void startAutoConfigDualsim(int i, Message message);

    boolean triggerAutoConfig(boolean z, int i, List<IRegisterTask> list);

    boolean tryAutoconfiguration(IRegisterTask iRegisterTask);

    boolean updateMobileNetworkforDualRcs(int i);

    void updateTelephonyCallStatus(int i, int i2);
}
