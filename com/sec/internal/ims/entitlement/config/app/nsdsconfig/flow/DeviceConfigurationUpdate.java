package com.sec.internal.ims.entitlement.config.app.nsdsconfig.flow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSErrorTranslator;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.DeviceConfiguration;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.MnoNsdsConfigStrategyCreator;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.ConfigurationRetrievalWithSIM;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.ConfigurationUpdate;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.Base64Decoder;
import com.sec.internal.ims.entitlement.util.DeviceConfigParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.config.IMnoNsdsConfigStrategy;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class DeviceConfigurationUpdate extends NSDSAppFlowBase {
    public static final int FORCE_CONFIG_UPDATE = 2;
    private static final String LOG_TAG = DeviceConfigurationUpdate.class.getSimpleName();
    public static final int RETRIEVE_DEVICE_CONFIG = 0;
    public static final int UPDATE_DEVICE_CONFIG = 1;
    private boolean mIsConfigUpdated = false;
    private boolean mIsForced = false;

    public DeviceConfigurationUpdate(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    public void performDeviceConfigRetrieval(int deviceEventType, int retryCount) {
        String str = LOG_TAG;
        IMSLog.i(str, "performDeviceConfigRetrieval: eventType " + deviceEventType + " retryCount " + retryCount);
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        int nsdsBaseOperation = -1;
        IMnoNsdsConfigStrategy mnoNsdsStrategy = getMnoNsdsConfigStrategy();
        if (mnoNsdsStrategy != null) {
            nsdsBaseOperation = mnoNsdsStrategy.getNextOperation(this.mDeviceEventType, -1);
        }
        if (nsdsBaseOperation == -1) {
            IMSLog.i(LOG_TAG, "performDeviceConfigRetrieval: next operation is empty.");
        } else {
            queueOperation(nsdsBaseOperation, (Bundle) null);
        }
    }

    private void retrieveDeviceConfiguration() {
        IMnoNsdsConfigStrategy mnoNsdsStrategy = getMnoNsdsConfigStrategy();
        ConfigurationRetrievalWithSIM configurationRetrievalWithSIM = new ConfigurationRetrievalWithSIM(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA);
        String deviceUid = this.mBaseFlowImpl.getDeviceId();
        configurationRetrievalWithSIM.retriveDeviceConfiguration(mnoNsdsStrategy != null ? mnoNsdsStrategy.getEntitlementServerUrl(deviceUid) : null, this.mDeviceGroup, NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi()));
    }

    private void updateDeviceConfiguration(boolean isForced) {
        String vimsiEap = NSDSHelper.getVIMSIforSIMDevice(this.mContext, this.mBaseFlowImpl.getSimManager().getImsi());
        this.mIsForced = isForced;
        new ConfigurationUpdate(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0", this.mUserAgent, this.mImeiForUA).updateDeviceConfiguration(this.mDeviceGroup, vimsiEap);
    }

    private void handleResponseGetDeviceConfig(Bundle bundleNSDSResponses, boolean refresh) {
        int errorResponseCode = getHttpErrRespCode(bundleNSDSResponses);
        String errorResponseReason = getHttpErrRespReason(bundleNSDSResponses);
        String str = LOG_TAG;
        IMSLog.i(str, "handleResponseGetDeviceConfig: refresh " + refresh + ", http error code = " + errorResponseCode + ", reason = " + errorResponseReason);
        boolean isSuccess = false;
        if (bundleNSDSResponses != null && errorResponseCode <= 0 && errorResponseReason == null) {
            ResponseManageConnectivity responseManageConnectivity = (ResponseManageConnectivity) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
            if (responseManageConnectivity == null) {
                IMSLog.e(LOG_TAG, "ResponseManageConnectivity is NULL");
                errorResponseCode = 1400;
            } else if (responseManageConnectivity.responseCode != 1000 || responseManageConnectivity.deviceConfig == null) {
                errorResponseCode = responseManageConnectivity.responseCode;
            } else {
                responseManageConnectivity.deviceConfig = Base64Decoder.decode(responseManageConnectivity.deviceConfig);
                persistDeviceConfig(responseManageConnectivity, DeviceConfigParser.parseDeviceConfig(responseManageConnectivity.deviceConfig), this.mBaseFlowImpl.getSimManager().getImsi(), this.mIsForced);
                isSuccess = true;
            }
        }
        if (!refresh && !isSuccess) {
            IMSLog.i(LOG_TAG, "!!!Device config retrieval failed. report it!!!");
            notifyBootupDeviceActivationFailure(errorResponseCode);
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "handleResponseGetDeviceConfig - response code = " + errorResponseCode);
        String deviceUid = this.mBaseFlowImpl.getDeviceId();
        if (NSDSNamespaces.NSDSDeviceState.DEVICECONFIG_IN_PROGRESS.equals(NSDSSharedPrefHelper.get(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE))) {
            IMSLog.i(LOG_TAG, "handleResponseGetDeviceConfig... reset device_config_state");
            NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICECONIFG_STATE);
        }
        notifyNSDSFlowResponse(isSuccess, NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, refresh ? 1 : 3, errorResponseCode);
    }

    private void persistDeviceConfig(ResponseManageConnectivity responseManageConnectivity, DeviceConfiguration deviceConfiguration, String imsi, boolean isForced) {
        if (responseManageConnectivity != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "ResponseManageConnectivity : messageId:" + responseManageConnectivity.messageId + "responseCode:" + responseManageConnectivity.responseCode + "servicenames:" + responseManageConnectivity.serviceNames + "deviceConfig:" + responseManageConnectivity.deviceConfig);
            if (responseManageConnectivity.responseCode == 1000) {
                String nwVersion = getVersionInfo(deviceConfiguration);
                String dbVersion = NSDSDatabaseHelper.getConfigVersion(this.mContext, imsi);
                this.mIsConfigUpdated = true;
                if (!this.mNSDSDatabaseHelper.isDeviceConfigAvailable(imsi)) {
                    NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME, this.mDeviceGroup);
                    this.mNSDSDatabaseHelper.insertDeviceConfig(responseManageConnectivity, nwVersion, imsi);
                } else if (isForced || isConfigVersionUpdated(nwVersion, dbVersion) || changedGorupName(this.mDeviceGroup)) {
                    this.mNSDSDatabaseHelper.updateDeviceConfig(responseManageConnectivity, nwVersion, imsi);
                } else {
                    IMnoNsdsConfigStrategy mnoNsdsStrategy = getMnoNsdsConfigStrategy();
                    if (mnoNsdsStrategy != null) {
                        mnoNsdsStrategy.scheduleRefreshDeviceConfig(this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
                    }
                }
            }
        } else {
            IMSLog.e(LOG_TAG, "ResponseManageConnectivity is NULL");
        }
    }

    private boolean isConfigVersionUpdated(String nwVersion, String dbVersion) {
        String str = LOG_TAG;
        IMSLog.i(str, "isConfigVersionUpdated: nwVersion-" + nwVersion + " dbVersion-" + dbVersion);
        IMSLog.c(LogClass.ES_DC_VERSION, "NWV:" + nwVersion + ",DBV:" + dbVersion);
        if (nwVersion == null || dbVersion == null) {
            IMSLog.e(LOG_TAG, "isConfigVersionUpdated: invalid version info");
            return false;
        }
        if (Double.compare(Double.valueOf(dbVersion).doubleValue(), Double.valueOf(nwVersion).doubleValue()) >= 0) {
            return false;
        }
        IMSLog.i(LOG_TAG, "isConfigVersionUpdated: config is updated");
        return true;
    }

    private boolean changedGorupName(String groupname) {
        String deviceUid = this.mBaseFlowImpl.getDeviceId();
        String preGroupName = NSDSSharedPrefHelper.get(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME);
        if (TextUtils.isEmpty(preGroupName) || !preGroupName.equalsIgnoreCase(groupname)) {
            NSDSSharedPrefHelper.save(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_GROUP_NAME, groupname);
            String str = LOG_TAG;
            IMSLog.i(str, "changedGorupName : changed to " + groupname);
            IMSLog.c(LogClass.ES_DC_GROUP_NAME, "CHANGED:" + groupname);
            return true;
        }
        IMSLog.i(LOG_TAG, "changedGorupName: not changed");
        return false;
    }

    private String getVersionInfo(DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration != null && deviceConfiguration.mConfigInfo != null) {
            return deviceConfiguration.mConfigInfo.mVersion;
        }
        IMSLog.e(LOG_TAG, "getVersionInfo: configuration info is null, vail");
        return null;
    }

    private void notifyBootupDeviceActivationFailure(int nsdsErrorCode) {
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        translatedErroCodes.add(Integer.valueOf(NSDSErrorTranslator.translate(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY, 3, nsdsErrorCode)));
        translatedErroCodes.add(1400);
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, false);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        notifyCallbackForNsdsEvent(0);
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsConfigStrategy getMnoNsdsConfigStrategy() {
        return MnoNsdsConfigStrategyCreator.getMnoStrategy(this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 0) {
            retrieveDeviceConfiguration();
        } else if (i == 1) {
            updateDeviceConfiguration(false);
        } else if (i == 2) {
            updateDeviceConfiguration(true);
        } else if (i == 102) {
            handleResponseGetDeviceConfig(msg.getData(), false);
        } else if (i != 109) {
            String str = LOG_TAG;
            IMSLog.i(str, "Unknown flow request: " + msg.what);
        } else {
            handleResponseGetDeviceConfig(msg.getData(), true);
        }
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nsdsBaseOperation, Bundle dataMap) {
        int msgId = -1;
        if (nsdsBaseOperation == 10) {
            msgId = 0;
        } else if (nsdsBaseOperation == 11) {
            msgId = 1;
        } else if (nsdsBaseOperation != 14) {
            IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
        } else {
            msgId = 2;
        }
        if (msgId != -1) {
            Message message = obtainMessage(msgId);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: success " + success + " isForced " + this.mIsForced);
        StringBuilder sb = new StringBuilder();
        sb.append("SUCS:");
        sb.append(success);
        IMSLog.c(LogClass.ES_DC_RESULT, sb.toString());
        ArrayList<Integer> translatedErroCodes = new ArrayList<>();
        if (!(success || nsdsMethodName == null || nsdsErrorCode == -1)) {
            int errorCode = NSDSErrorTranslator.translate(nsdsMethodName, operation, nsdsErrorCode);
            translatedErroCodes.add(Integer.valueOf(errorCode));
            String str2 = LOG_TAG;
            IMSLog.i(str2, "notifyNSDSFlowResponse: errorCode " + errorCode);
        }
        if (this.mIsConfigUpdated != 0) {
            translatedErroCodes.add(Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.MANAGE_CONNECTIVITY_NEW_CONFIG_UPDATED));
            this.mIsConfigUpdated = false;
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.DEVICE_CONFIG_UPDATED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success);
        intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
        intent.putExtra("retry_count", this.mRetryCount);
        intent.putExtra(NSDSNamespaces.NSDSExtras.FORCED_CONFIG_UPDATE, this.mIsForced);
        intent.putExtra(NSDSNamespaces.NSDSExtras.ORIG_ERROR_CODE, nsdsErrorCode);
        intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErroCodes);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        this.mIsForced = false;
    }
}
