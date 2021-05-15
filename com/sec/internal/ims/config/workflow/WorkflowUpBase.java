package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IAutoConfigurationListener;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;

public abstract class WorkflowUpBase extends WorkflowBase {
    protected static final int AUTH_HIDDENTRY_MAX_COUNT = 3;
    protected static final int AUTH_TRY_MAX_COUNT = 1;
    protected static final int INTERNALERR_RETRY_MAX_COUNT = 5;
    protected static final String LOG_TAG = WorkflowUpBase.class.getSimpleName();
    protected String[] mAlternateVersions = ImsRegistry.getStringArray(this.mPhoneId, GlobalSettingsConstants.RCS.ALT_PROVISIONING_VERSION, (String[]) null);

    public WorkflowUpBase(Looper looper, Context context, Handler moduleHandler, Mno mno, ITelephonyAdapter telephonyAdapter, IStorageAdapter storageAdapter, IHttpAdapter httpAdapter, IXmlParserAdapter xmlParserAdapter, IDialogAdapter dialogAdapter, int phoneId) {
        super(looper, context, moduleHandler, mno, telephonyAdapter, storageAdapter, httpAdapter, xmlParserAdapter, dialogAdapter, phoneId);
    }

    public void startAutoConfig(boolean mobile) {
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "startAutoConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "startAutoConfig mobile: " + mobile);
        this.mMobileNetwork = mobile;
        sendEmptyMessage(1);
    }

    public void forceAutoConfig(boolean mobile) {
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "forceAutoConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceAutoConfig mobile:" + mobile);
        this.mMobileNetwork = mobile;
        sendEmptyMessage(0);
    }

    public void forceAutoConfigNeedResetConfig(boolean mobile) {
        if (this.sIsConfigOngoing) {
            Log.i(LOG_TAG, "forceAutoConfigNeedResetConfig ongoing");
            return;
        }
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceAutoConfigNeedResetConfig mobile:" + mobile);
        this.mMobileNetwork = mobile;
        IConfigModule cm = ImsRegistry.getConfigModule();
        if (getVersion() == -2 && cm.getAcsConfig(this.mPhoneId).isTriggeredByNrcr() && this.mMno == Mno.SWISSCOM) {
            setOpMode(WorkflowBase.OpMode.DISABLE, (Map<String, String>) null);
            cm.getAcsConfig(this.mPhoneId).setAcsVersion(-2);
        } else {
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
        }
        sendEmptyMessage(0);
    }

    /* access modifiers changed from: package-private */
    public void work() {
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUp(WorkflowBase.Workflow init, WorkflowBase.Workflow fetchHttps, WorkflowBase.Workflow finish) throws InvalidHeaderException, UnknownStatusException {
        setLastErrorCode(this.mSharedInfo.getHttpResponse().getStatusCode());
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleResponseForUp: mLastErrorCode: " + getLastErrorCode());
        addEventLog(LOG_TAG + "handleResponseForUp: mLastErrorCode: " + getLastErrorCode());
        String supportedVersions = null;
        int i2 = 0;
        if (this.mAlternateVersions != null) {
            Map<String, List<String>> headers = this.mSharedInfo.getHttpResponse().getHeader();
            supportedVersions = headers.get(HttpRequest.HEADER_SUPPORTED_VERSIONS) != null ? (String) headers.get(HttpRequest.HEADER_SUPPORTED_VERSIONS).get(0) : null;
            if (supportedVersions != null) {
                setAcsSeverSupportedVersions(supportedVersions.trim());
            }
        }
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode == 0) {
            Log.i(LOG_TAG, "RCS configuration server is unreachable");
            return finish;
        } else if (lastErrorCode == 200) {
            Log.i(LOG_TAG, "normal case");
            return fetchHttps;
        } else if (lastErrorCode == 403) {
            Log.i(LOG_TAG, "set version to zero");
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            return finish;
        } else if (lastErrorCode == 406) {
            String commonVersion = null;
            if (this.mAlternateVersions != null) {
                String provVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PROVISIONING_VERSION, "2.0");
                if (supportedVersions != null) {
                    if (!supportedVersions.contains(provVersion)) {
                        String[] strArr = this.mAlternateVersions;
                        int length = strArr.length;
                        while (true) {
                            if (i2 >= length) {
                                break;
                            }
                            String str2 = strArr[i2];
                            if (supportedVersions.contains(str2)) {
                                commonVersion = str2;
                                break;
                            }
                            i2++;
                        }
                    } else {
                        commonVersion = provVersion;
                    }
                }
            }
            if (commonVersion == null) {
                return finish;
            }
            this.mRcsProvisioningVersion = commonVersion;
            return fetchHttps;
        } else if (lastErrorCode == 500) {
            Log.i(LOG_TAG, "internal server error");
            int retryCount = this.mSharedInfo.getInternalErrRetryCount();
            String str3 = LOG_TAG;
            Log.i(str3, "retryCount: " + retryCount);
            if (retryCount > 5) {
                return finish;
            }
            this.mSharedInfo.setInternalErrRetryCount(retryCount + 1);
            return init;
        } else if (lastErrorCode == 503) {
            sleep(1000 * getretryAfterTime());
            return fetchHttps;
        } else if (lastErrorCode == 511) {
            Log.i(LOG_TAG, "The token is no longer valid");
            setToken("");
            removeValidToken();
            return init;
        } else if (lastErrorCode == 800) {
            Log.i(LOG_TAG, "SSL handshake is failed");
            return finish;
        } else {
            throw new UnknownStatusException("unknown http status code");
        }
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> data) {
        String oldToken = getToken();
        String newToken = getToken(data);
        String str = LOG_TAG;
        IMSLog.s(str, "checkAndUpdateData: oldToken: " + oldToken + " newToken: " + newToken);
        if (!TextUtils.isEmpty(newToken) && !newToken.equals(oldToken)) {
            Log.i(LOG_TAG, "checkAndUpdateData: token is changed, update it");
            setToken(newToken);
        }
        String newValidity = "";
        String oldValidity = getVersion() > 0 ? String.valueOf(getValidity()) : newValidity;
        if (getVersion(data) > 0) {
            newValidity = String.valueOf(getValidity(data));
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "checkAndUpdateData: oldValidity: " + oldValidity + " newValidity: " + newValidity);
        if (!TextUtils.isEmpty(newValidity) && !newValidity.equals(oldValidity)) {
            Log.i(LOG_TAG, "checkAndUpdateData: validity is changed, update it");
            try {
                setValidity(Integer.parseInt(newValidity));
            } catch (NumberFormatException e) {
                String str3 = LOG_TAG;
                Log.i(str3, "checkAndUpdateData: cannot update validity: " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setOpMode: mode: " + mode.name());
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[mode.ordinal()]) {
            case 1:
                if (data == null) {
                    setActiveOpModeWithEmptyData();
                    return;
                } else {
                    setActiveOpMode(data);
                    return;
                }
            case 2:
            case 3:
            case 4:
                setDisableOpMode(mode, data);
                return;
            case 5:
                setDormantOpMode(mode);
                return;
            case 6:
            case 7:
            case 8:
            case 9:
                setDisabledStateOpMode(mode, data);
                return;
            case 10:
                setDisableRcsByUserOpMode();
                return;
            case 11:
                setEnableRcsByUserOpMode();
                return;
            default:
                Log.i(LOG_TAG, "unknown mode");
                return;
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowUpBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        static {
            int[] iArr = new int[WorkflowBase.OpMode.values().length];
            $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = iArr;
            try {
                iArr[WorkflowBase.OpMode.ACTIVE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.DISABLE_RCS_BY_USER.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowBase.OpMode.ENABLE_RCS_BY_USER.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDataFullUpdateNeeded(Map<String, String> data) {
        return getVersion() < getVersion(data) || (this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()));
    }

    /* access modifiers changed from: protected */
    public void setActiveOpMode(Map<String, String> data) {
        IMSLog.s(LOG_TAG, "data: " + data);
        if (isDataFullUpdateNeeded(data)) {
            writeDataToStorage(data);
            setVersionBackup(getVersion(data));
            String token = getToken(data);
            if (!TextUtils.isEmpty(token)) {
                Log.i(LOG_TAG, "save valid token");
                ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(this.mPhoneId), token);
            }
        } else {
            Log.i(LOG_TAG, "the same or lower version, remain previous data");
            checkAndUpdateData(data);
        }
        setNextAutoconfigTimeFromValidity(getValidity());
    }

    /* access modifiers changed from: protected */
    public void setActiveOpModeWithEmptyData() {
        int backupVersion = getParsedIntVersionBackup();
        if (backupVersion >= WorkflowBase.OpMode.ACTIVE.value()) {
            Log.i(LOG_TAG, "retreive backup version of configuration");
            setVersion(backupVersion);
            return;
        }
        Log.i(LOG_TAG, "data is empty, remain previous data and mode");
    }

    /* access modifiers changed from: protected */
    public void setDisableOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        clearStorage();
        if (data != null) {
            this.mStorage.writeAll(data);
        }
        setVersion(mode.value());
        setValidity(mode.value());
    }

    /* access modifiers changed from: protected */
    public void setDormantOpMode(WorkflowBase.OpMode mode) {
        if (getVersion() != WorkflowBase.OpMode.DORMANT.value() && getParsedIntVersionBackup() < WorkflowBase.OpMode.ACTIVE.value()) {
            setVersionBackup(getVersion());
        }
        setVersion(mode.value());
    }

    /* access modifiers changed from: protected */
    public void setDisabledStateOpMode(WorkflowBase.OpMode mode, Map<String, String> data) {
        if (data != null) {
            this.mStorage.writeAll(data);
        }
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            setVersionBackup(getVersion());
        }
        if (mode == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        } else if (mode == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE_PERMANENTLY.value());
        } else if (mode == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE) {
            setVersion(WorkflowBase.OpMode.DISABLE.value());
        } else {
            setVersion(WorkflowBase.OpMode.DORMANT.value());
        }
        setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
        cancelValidityTimer();
        setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
    }

    /* access modifiers changed from: protected */
    public void setDisableRcsByUserOpMode() {
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            setVersionBackup(getVersion());
        }
        setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()));
        String str = LOG_TAG;
        Log.i(str, "rcsState: " + getRcsState());
        cancelValidityTimer();
        setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
    }

    /* access modifiers changed from: protected */
    public void setEnableRcsByUserOpMode() {
        int backupVersion = getParsedIntVersionBackup();
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            setRcsState(String.valueOf(getVersion()));
        } else if (backupVersion >= WorkflowBase.OpMode.ACTIVE.value()) {
            setVersion(backupVersion);
            setRcsState(getVersionBackup());
        } else {
            clearStorage();
            setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
            setRcsState(String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()));
        }
        setRcsDisabledState("");
        cancelValidityTimer();
        setNextAutoconfigTime((long) WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        ImsRegistry.getConfigModule().getAcsConfig(this.mPhoneId).disableRcsByAcs(false);
    }

    public void changeOpMode(boolean isRcsEnabled) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "changeOpMode: isRcsEnabled: " + isRcsEnabled);
        if (isRcsEnabled) {
            setOpMode(WorkflowBase.OpMode.ENABLE_RCS_BY_USER, (Map<String, String>) null);
        } else {
            setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
        }
    }

    public void registerAutoConfigurationListener(IAutoConfigurationListener listener) {
        this.mTelephony.registerAutoConfigurationListener(listener);
    }

    public void unregisterAutoConfigurationListener(IAutoConfigurationListener listener) {
        this.mTelephony.unregisterAutoConfigurationListener(listener);
    }

    public void sendVerificationCode(String value) {
        IMSLog.c(LogClass.WFB_OTP_CODE, this.mPhoneId + ",VC:" + value);
        this.mTelephony.sendVerificationCode(value);
    }

    public void sendMsisdnNumber(String value) {
        this.mTelephony.sendMsisdnNumber(value);
    }

    /* access modifiers changed from: protected */
    public void registerMobileNetwork(ConnectivityManager cm, NetworkRequest networkRequest, ConnectivityManager.NetworkCallback networkCallback) {
        try {
            Log.i(LOG_TAG, "register mobile network");
            cm.registerNetworkCallback(networkRequest, networkCallback);
            cm.requestNetwork(networkRequest, networkCallback);
        } catch (ConnectivityManager.TooManyRequestsException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterMobileNetwork(ConnectivityManager cm, ConnectivityManager.NetworkCallback networkCallback) {
        try {
            Log.i(LOG_TAG, "unregister mobile network");
            cm.unregisterNetworkCallback(networkCallback);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public boolean checkWifiConnection(ConnectivityManager cm) {
        NetworkInfo ni;
        Network[] allNetworks = cm.getAllNetworks();
        int length = allNetworks.length;
        int i = 0;
        while (i < length) {
            Network network = allNetworks[i];
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc == null || !nc.hasTransport(1) || !nc.hasCapability(12) || (ni = cm.getNetworkInfo(network)) == null) {
                i++;
            } else {
                Log.i(LOG_TAG, "checkWifiConnection: " + ni);
                return ni.isAvailable() && ni.isConnected();
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkMobileConnection(ConnectivityManager cm) {
        NetworkInfo ni;
        Network[] allNetworks = cm.getAllNetworks();
        int length = allNetworks.length;
        int i = 0;
        while (i < length) {
            Network network = allNetworks[i];
            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
            if (nc == null || !nc.hasTransport(0) || !nc.hasCapability(12) || (ni = cm.getNetworkInfo(network)) == null) {
                i++;
            } else {
                Log.i(LOG_TAG, "checkMobileConnection: " + ni);
                return ni.isAvailable() && ni.isConnected();
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState() {
        return getRcsDisabledState(ConfigConstants.CONFIGTYPE.STORAGE_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE, (Map<String, String>) null);
    }

    /* access modifiers changed from: protected */
    public void setAcsSeverSupportedVersions(String vesions) {
        this.mStorage.write(ConfigConstants.PATH.SERVER_SUPPORTED_VESIONS, vesions);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(Map<String, String> data) {
        return getRcsDisabledState(ConfigConstants.CONFIGTYPE.PARSEDXML_DATA, ConfigConstants.PATH.RCS_DISABLED_STATE, data);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode getRcsDisabledState(String configType, String configPath, Map<String, String> configData) {
        WorkflowBase.OpMode mode = WorkflowBase.OpMode.NONE;
        String rcsDisabledState = "";
        if (ConfigConstants.CONFIGTYPE.STORAGE_DATA.equals(configType)) {
            rcsDisabledState = this.mStorage.read(configPath);
        } else if (ConfigConstants.CONFIGTYPE.PARSEDXML_DATA.equals(configType)) {
            rcsDisabledState = configData != null ? configData.get(configPath) : "";
        }
        if (TextUtils.isEmpty(rcsDisabledState)) {
            Log.i(LOG_TAG, "getRcsDisabledState: empty");
            return mode;
        }
        WorkflowBase.OpMode mode2 = convertRcsDisabledStateToOpMode(rcsDisabledState);
        String str = LOG_TAG;
        Log.i(str, "getRcsDisabledState: mode: " + mode2.name());
        return mode2;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.OpMode convertRcsDisabledStateToOpMode(String rcsDisabledState) {
        WorkflowBase.OpMode mode = WorkflowBase.OpMode.NONE;
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value()).equals(rcsDisabledState)) {
            return WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE;
        }
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_PERMANENTLY.value()).equals(rcsDisabledState)) {
            return WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE;
        }
        if (String.valueOf(WorkflowBase.OpMode.DISABLE.value()).equals(rcsDisabledState)) {
            return WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE;
        }
        if (String.valueOf(WorkflowBase.OpMode.DORMANT.value()).equals(rcsDisabledState)) {
            return WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE;
        }
        return mode;
    }

    /* access modifiers changed from: protected */
    public int convertRcsDisabledStateToValue(WorkflowBase.OpMode rcsDisabledState) {
        int value = WorkflowBase.OpMode.NONE.value();
        if (rcsDisabledState == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_TEMPORARY.value();
        }
        if (rcsDisabledState == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE_PERMANENTLY.value();
        }
        if (rcsDisabledState == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DISABLE.value();
        }
        if (rcsDisabledState == WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE) {
            return WorkflowBase.OpMode.DORMANT.value();
        }
        return value;
    }

    /* access modifiers changed from: protected */
    public void setRcsDisabledState(String rcsDisabledState) {
        this.mStorage.write(ConfigConstants.PATH.RCS_DISABLED_STATE, rcsDisabledState);
    }

    /* access modifiers changed from: protected */
    public boolean isValidRcsDisabledState(WorkflowBase.OpMode rcsDisabledState) {
        return rcsDisabledState == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE || rcsDisabledState == WorkflowBase.OpMode.DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE || rcsDisabledState == WorkflowBase.OpMode.DISABLE_BY_RCS_DISABLED_STATE || rcsDisabledState == WorkflowBase.OpMode.DORMANT_BY_RCS_DISABLED_STATE;
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam() {
        return convertRcsStateWithSpecificParam(getRcsState(), getRcsDisabledState());
    }

    /* access modifiers changed from: protected */
    public String convertRcsStateWithSpecificParam(String rcsState, WorkflowBase.OpMode rcsDisabledState) {
        if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(rcsState)) {
            return rcsState;
        }
        if (isValidRcsDisabledState(rcsDisabledState)) {
            return String.valueOf(rcsDisabledState.value());
        }
        int version = getVersion();
        return isActiveVersion(version) ? String.valueOf(version) : String.valueOf(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
    }

    /* access modifiers changed from: protected */
    public boolean isActiveVersion(int version) {
        return version >= WorkflowBase.OpMode.ACTIVE.value();
    }

    /* access modifiers changed from: protected */
    public String getRcsState() {
        return this.mStorage.read(ConfigConstants.PATH.RCS_STATE);
    }

    /* access modifiers changed from: protected */
    public void setRcsState(String rcsState) {
        this.mStorage.write(ConfigConstants.PATH.RCS_STATE, rcsState);
    }
}
