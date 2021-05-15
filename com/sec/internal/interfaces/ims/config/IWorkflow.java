package com.sec.internal.interfaces.ims.config;

import com.sec.ims.IAutoConfigurationListener;
import java.util.Map;

public interface IWorkflow {
    public static final int ACTIVE_AUTOCONFIG_VERSION = 1;
    public static final int DEFAULT_ERROR_CODE = 987;
    public static final int DISABLE_AUTOCONFIG_VERSION = -2;
    public static final int DISABLE_PERMANENTLY_AUTOCONFIG_VERSION = -1;
    public static final int DISABLE_TEMPORARY_AUTOCONFIG_VERSION = 0;
    public static final int DORMANT_AUTOCONFIG_VERSION = -3;

    void cleanup();

    void closeStorage();

    void forceAutoConfig(boolean z);

    void forceAutoConfigNeedResetConfig(boolean z);

    IStorageAdapter getStorage();

    void handleMSISDNDialog();

    void init();

    void onDefaultSmsPackageChanged();

    Map<String, String> read(String str);

    void startAutoConfig(boolean z);

    void startAutoConfigDualsim(boolean z);

    void clearAutoConfigStorage() {
    }

    void reInitIfNeeded() {
    }

    void clearToken() {
    }

    void removeValidToken() {
    }

    boolean checkNetworkConnectivity() {
        return false;
    }

    int getLastErrorCode() {
        return DEFAULT_ERROR_CODE;
    }

    void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
    }

    void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
    }

    void changeOpMode(boolean isRcsEnabled) {
    }

    void sendVerificationCode(String value) {
    }

    void sendMsisdnNumber(String value) {
    }

    void startCurrConfig() {
    }

    boolean isConfigOngoing() {
        return false;
    }

    void stopWorkFlow() {
    }

    void onBootCompleted() {
    }

    void dump() {
    }
}
