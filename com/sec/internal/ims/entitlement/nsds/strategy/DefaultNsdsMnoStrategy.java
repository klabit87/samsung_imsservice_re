package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.AttSimSwapFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.EntitlementAndE911AidCheckFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SIMDeviceDeactivationFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SIMDeviceImplicitActivation;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SimSwapFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.o2uentitlement.O2UEntitlementCheckFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaEntitlementCheckFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaSimDeviceImplicitActivation;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.ATTWfcEntitlementOperation;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.DefaultNsdsOperation;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.O2UEntitlementOperation;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.XAAEntitlementOperation;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceDeactivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultNsdsMnoStrategy implements IMnoNsdsStrategy {
    public static final long ATT_NETWORK_ERROR_RETRY_INTERVAL = 30000;
    private static final String DEFAULT_URL_ENTITLEMENT_SERVER = "http://ses.ericsson-magic.net:10080/generic_devices";
    private static final String LOG_TAG = DefaultNsdsMnoStrategy.class.getSimpleName();
    public static final long XAA_NETWORK_ERROR_RETRY_INTERVAL = 30000;
    protected Context mContext;
    protected NsdsStrategyType mStrategyType = NsdsStrategyType.DEFAULT;
    protected final Map<String, Integer> sMapEntitlementServices = new HashMap();

    public DefaultNsdsMnoStrategy(Context ctx) {
        this.mContext = ctx;
        this.sMapEntitlementServices.put("vowifi", 1);
    }

    public Map<String, Integer> getEntitlementServicesMap() {
        return this.sMapEntitlementServices;
    }

    public final String getEntitlementServerUrl(String imsi, String deviceUid) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            String subset = imsi.substring(5, 8);
            if (subset.equals("995") || subset.equals("997") || subset.equals("259")) {
                return "https://ses-test.o2.co.uk:443/generic_devices";
            }
            return "https://ses.o2.co.uk:443/generic_devices";
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            String url = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, "https://sentitlement2.mobile.att.net/WFC");
            if (TextUtils.isEmpty(url) || !url.contains("t-mobile")) {
                return url;
            }
            NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceUid);
            return "https://sentitlement2.mobile.att.net/WFC";
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            String entitlementUrl = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, (String) null);
            if (!TextUtils.isEmpty(entitlementUrl) && entitlementUrl.contains("att")) {
                NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceUid);
                entitlementUrl = null;
            }
            String str = LOG_TAG;
            IMSLog.i(str, "getEntitlementServerUrl: url in sp " + entitlementUrl);
            if (entitlementUrl == null) {
                return NSDSConfigHelper.getConfigValue(this.mContext, imsi, NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER, "https://eas3.msg.t-mobile.com/generic_devices");
            }
            return entitlementUrl;
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, "https://ses.epdg.gci.net/generic_devices");
        }
        return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, deviceUid, DEFAULT_URL_ENTITLEMENT_SERVER);
    }

    public final boolean supportEntitlementCheck() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.O2U, NsdsStrategyType.ATT, NsdsStrategyType.XAA);
    }

    public final boolean needGetMSISDNForEntitlement() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.O2U, NsdsStrategyType.XAA);
    }

    public final boolean needCheckEntitlementPollInterval() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.O2U);
    }

    public final boolean isNsdsUIAppSwitchOn(String deviceUid) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U, NsdsStrategyType.TMOUS)) {
            return true;
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.XAA, NsdsStrategyType.ATT)) {
            return false;
        }
        String autoActivate = NSDSSharedPrefHelper.get(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
        if (!EntFeatureDetector.checkWFCAutoOnEnabled(0) || autoActivate == null || "completed".equals(autoActivate)) {
            boolean isVoWiFiEnabled = VowifiConfig.isEnabled(this.mContext, 0);
            String str = LOG_TAG;
            IMSLog.i(str, "isNsdsUIAppSwitchOn: WFC switch [" + isVoWiFiEnabled + "]");
            return isVoWiFiEnabled;
        }
        IMSLog.i(LOG_TAG, "[ATT_AutoOn] isNsdsUIAppSwitchOn: In process autoOn ");
        return true;
    }

    public final long calEntitlementCheckExpRetryTime(int retryCount) {
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return 0;
        }
        long[] ExpBackOffRetrySlots_30Min = {1800, 1800};
        if (retryCount <= 2) {
            return ExpBackOffRetrySlots_30Min[retryCount - 1] * 1000;
        }
        IMSLog.i(LOG_TAG, "calEntitlementCheckExpRetryTime: retry exceeded max tries");
        return 0;
    }

    public final long getEntitlementCheckExpirationTime() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA)) {
            return 86400000;
        }
        return 0;
    }

    public final boolean shouldChangedUriTriggerNsdsService(Uri uri) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return NSDSContractExt.NsdsConfigs.CONTENT_URI.equals(uri);
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA)) {
            return false;
        }
        if (NSDSContractExt.DeviceConfig.CONTENT_URI.equals(uri) || NSDSContractExt.NsdsConfigs.CONTENT_URI.equals(uri)) {
            return true;
        }
        return false;
    }

    public final boolean shouldIgnoreDeviceConfigValidity() {
        return !this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS);
    }

    public final long getRetryInterval() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return 30000;
        }
        return 0;
    }

    public final boolean isNsdsServiceEnabled() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA, NsdsStrategyType.O2U);
    }

    public final String getSimAuthenticationType() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return NSDSNamespaces.NSDSSimAuthType.USIM;
        }
        return NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
    }

    public final String getGcmSenderId(String deviceUid, String imsi) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return NSDSConfigHelper.getConfigValue(this.mContext, imsi, NSDSConfigHelper.KEY_GCM_PUSH_MSG_SENDER_ID);
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return null;
        }
        String senderId = NSDSConfigHelper.getConfigValue(this.mContext, imsi, NSDSConfigHelper.KEY_GCM_EVT_LST_MSG_SENDER_ID);
        if (senderId == null) {
            return NSDSSharedPrefHelper.getGcmSenderId(this.mContext, deviceUid, "418816648224");
        }
        return senderId;
    }

    public final String getDeviceGroup(int slotId) {
        String deviceGroup;
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20;
        }
        boolean isDeviceGroup = ImsRegistry.getBoolean(slotId, GlobalSettingsConstants.Entitlement.DEVICE_GROUP, false);
        String salesCode = OmcCode.get();
        if (DeviceUtil.isTablet() && DeviceUtil.isSupport5G(this.mContext)) {
            isDeviceGroup = true;
        }
        if (!isDeviceGroup || (!"TMB".equals(salesCode) && !"TMK".equals(salesCode) && !"DSH".equals(salesCode))) {
            deviceGroup = NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20;
        } else {
            String model = Build.MODEL;
            String channel = getChannelName(slotId, salesCode, model);
            String[] separated = model.split("-");
            deviceGroup = NSDSNamespaces.NSDSSettings.DEVICE_GROUP + '-' + separated[1] + '-' + channel + '-' + Build.VERSION.INCREMENTAL;
        }
        IMSLog.s(LOG_TAG, "getDeviceGroup: " + deviceGroup);
        return deviceGroup;
    }

    private String getChannelName(int slotId, String salesCode, String model) {
        String channel = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMO;
        if (SemSystemProperties.get("ro.simbased.changetype", "").contains("SED") && !model.contains("G98")) {
            channel = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_SE_DEVICE;
        } else if ("TMK".equals(salesCode)) {
            channel = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMK;
            String channelFromGlobalSettings = ImsRegistry.getString(slotId, GlobalSettingsConstants.Entitlement.CHANNEL_NAME, "");
            if (!TextUtils.isEmpty(channelFromGlobalSettings)) {
                channel = channelFromGlobalSettings;
            }
        } else if ("DSH".equals(salesCode)) {
            channel = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_DISH;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getChannelName : " + channel);
        return channel;
    }

    public final int getNextOperation(int deviceEventType, int prevNsdsBaseOperation, int responseCode, Bundle dataMap) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            return O2UEntitlementOperation.getOperation(deviceEventType, prevNsdsBaseOperation);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return XAAEntitlementOperation.getOperation(deviceEventType, prevNsdsBaseOperation, responseCode, dataMap);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return ATTWfcEntitlementOperation.getOperation(deviceEventType, prevNsdsBaseOperation, responseCode, dataMap);
        }
        return DefaultNsdsOperation.getOperation(deviceEventType, prevNsdsBaseOperation, responseCode, dataMap);
    }

    public final List<String> getServiceListForPushToken() {
        List<String> serviceList = new ArrayList<>();
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            serviceList.add("vowifi");
        } else {
            if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
                serviceList.add(NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM);
            } else {
                serviceList.add(NSDSNamespaces.NSDSServices.SERVICE_CONNECTIVITY_MANAGER);
                serviceList.add("vowifi");
            }
        }
        return serviceList;
    }

    public final int getEntitlementCheckMaxRetry() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA) ? 2 : 0;
    }

    public final LocAndTcWebSheetData getLocAndTcWebSheetData(String url, String data) {
        String str = LOG_TAG;
        IMSLog.i(str, "LocAndTcWebSheetData: url-" + url + ", data-" + data);
        if (url == null || data == null) {
            return null;
        }
        String websheetClient = "NsdsWebSheetController";
        String websheetTitle = "Location and TC";
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            websheetClient = "WiFiCallingWebViewController";
            websheetTitle = "Wi-Fi Calling";
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            websheetTitle = "Wi-Fi Calling";
        }
        return new LocAndTcWebSheetData(url, data, websheetTitle, websheetClient);
    }

    public final boolean isSimSupportedForNsds(ISimManager simManager) {
        Mno mno = simManager.getSimMno();
        boolean isSimSupported = true;
        boolean z = true;
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            if (!(Mno.TELEFONICA_UK == mno || Mno.TELEFONICA_UK_LAB == mno)) {
                z = false;
            }
            isSimSupported = z;
        } else {
            if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
                if (Mno.GCI != mno) {
                    z = false;
                }
                isSimSupported = z;
            } else {
                if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
                    if (!(Mno.TMOBILE == mno || Mno.TMOUS == mno)) {
                        z = false;
                    }
                    isSimSupported = z;
                } else {
                    if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
                        if (Mno.ATT != mno) {
                            z = false;
                        }
                        isSimSupported = z;
                    }
                }
            }
        }
        IMSLog.i(LOG_TAG, "isSimSupportedForNsds: " + isSimSupported);
        return isSimSupported;
    }

    public final String[] getNSDSServices() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            return new String[]{NSDSNamespaces.NSDSRequestServices.REQ_SERVICE_VOWIFI, NSDSNamespaces.NSDSRequestServices.REQ_SERVICE_VOLTE};
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new String[]{"vowifi"};
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new String[]{NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM};
        }
        return null;
    }

    public final String getNSDSApiVersion() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            return NSDSNamespaces.NSDSApiVersion.MDSP30;
        }
        return "1.0";
    }

    public final String getUserAgent() {
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return null;
        }
        String channel = "T-Mobile";
        if ("TMK".equals(OmcCode.get())) {
            channel = "Metro";
        }
        return channel + ' ' + "UP2 VVM" + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public final IEntitlementCheck getEntitlementCheckImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new XaaEntitlementCheckFlow(looper, context, baseFlowImpl, databaseHelper);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new EntitlementAndE911AidCheckFlow(looper, context, baseFlowImpl, databaseHelper);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            return new O2UEntitlementCheckFlow(looper, context, baseFlowImpl, databaseHelper);
        }
        return null;
    }

    public final ISIMDeviceImplicitActivation getSimDeviceActivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new XaaSimDeviceImplicitActivation(looper, context, baseFlowImpl, databaseHelper);
        }
        return new SIMDeviceImplicitActivation(looper, context, baseFlowImpl, databaseHelper);
    }

    public final ISIMDeviceDeactivation getSimDeviceDeactivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        return new SIMDeviceDeactivationFlow(looper, context, baseFlowImpl, databaseHelper);
    }

    public final ISimSwapFlow getSimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            return null;
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new AttSimSwapFlow(looper, context, baseFlowImpl, databaseHelper);
        }
        return new SimSwapFlow(looper, context, baseFlowImpl, databaseHelper);
    }

    public final boolean isSIMDeviceActivationRequired() {
        return !this.mStrategyType.isOneOf(NsdsStrategyType.O2U);
    }

    public final int getBaseOperationMaxRetry() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA)) {
            return 0;
        }
        return 4;
    }

    public final boolean requireRetryBootupProcedure() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.O2U, NsdsStrategyType.TMOUS);
    }

    public final boolean isGcmTokenRequired() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.TMOUS);
    }

    public final boolean isNsdsServiceViaXcap() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.O2U);
    }

    public final long getWaitTimeForForcedSimSwap() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.TMOUS)) {
            return 10000;
        }
        return 0;
    }

    public final List<Integer> getOperationsForBootupInit(String deviceUid) {
        List<Integer> operations = new ArrayList<>();
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            operations.add(15);
        } else {
            if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
                operations.add(43);
                String autoActivate = NSDSSharedPrefHelper.get(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
                if (!EntFeatureDetector.checkWFCAutoOnEnabled(0) || (!TextUtils.isEmpty(autoActivate) && TextUtils.equals("completed", autoActivate))) {
                    IMSLog.i(LOG_TAG, "[ATT_AutoOn] getOperationsForBootupInit: already started");
                    if (isNsdsUIAppSwitchOn(deviceUid) && NSDSSharedPrefHelper.isDeviceActivated(this.mContext, deviceUid)) {
                        operations.add(44);
                    }
                } else {
                    NSDSSharedPrefHelper.save(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS);
                    IMSLog.i(LOG_TAG, "[ATT_AutoOn] getOperationsForBootupInit: add EVT_REFRESH_ENTITLEMENT_AND_911_AID");
                    operations.add(51);
                }
            }
        }
        return operations;
    }

    public final void handleSimNotAvailable(String deviceUid, int slotId) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.O2U)) {
            IMSLog.i(LOG_TAG, "reset NSDSSharedPrefHelper");
            NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
            NSDSSharedPrefHelper.removePrefForSlot(this.mContext, slotId, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
            Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, true);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOWIFI, false);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SERVICE_VOLTE, false);
            intent.putExtra(NSDSNamespaces.NSDSExtras.POLL_INTERVAL, 0);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotId);
            intent.putExtra("phoneId", slotId);
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
    }

    public final boolean isDeviceProvisioned() {
        boolean z = true;
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return true;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            z = false;
        }
        boolean isProvisioned = z;
        IMSLog.i(LOG_TAG, "isDeviceProvisioned: " + isProvisioned);
        return isProvisioned;
    }

    public final long getDeviceInfoTime() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return 172800000;
        }
        return 0;
    }

    protected enum NsdsStrategyType {
        DEFAULT,
        O2U,
        ATT,
        TMOUS,
        XAA,
        END_OF_NSDSSTRATEGY;

        /* access modifiers changed from: protected */
        public boolean isOneOf(NsdsStrategyType... types) {
            for (NsdsStrategyType type : types) {
                if (this == type) {
                    return true;
                }
            }
            return false;
        }
    }
}
