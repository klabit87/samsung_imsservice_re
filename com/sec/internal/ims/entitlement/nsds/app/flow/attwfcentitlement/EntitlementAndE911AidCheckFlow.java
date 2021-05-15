package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseManagePushToken;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.constants.ims.entitilement.data.ServiceEntitlement;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.LocationRegistrationAndTCAcceptanceCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.OperationUsingManagePushToken;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist.PushTokenHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntitlementAndE911AidCheckFlow extends NSDSAppFlowBase implements IEntitlementCheck {
    private static final int FAIL_ENTITLEMENT_AUTO_ON = 14;
    private static final int INIT_E911_ADDRESS_UPDATE = 10;
    private static final int INIT_ENTITLEMENT_CHECK = 8;
    private static final int INIT_PUSH_TOKEN_REMOVAL = 9;
    private static final int LOCATION_AND_TC_CHECK = 0;
    private static final String LOG_TAG = EntitlementAndE911AidCheckFlow.class.getSimpleName();
    private static final int OPEN_E911_ADDRESS_UPDATE_WEBSHEET = 5;
    private static final int OPEN_LOC_AND_TC_WEBSHEET = 4;
    private static final int REGISTER_PUSH_TOKEN = 2;
    private static final int REMOVE_PUSH_TOKEN = 3;
    private static final int REMOVE_PUSH_TOKEN_AUTO_ON = 12;
    private static final int REMOVE_PUSH_TOKEN_AUTO_ON_AFTER = 13;
    private static final int RESULT_SVC_PROV_LOC_AND_TC_WEBSHEET = 6;
    private static final int RESULT_UPDATE_LOC_AND_TC_WEBSHEET = 7;
    private static final int RETRY_ENTITLEMENT_AUTO_ON = 11;
    private static final int VOWIFI_ENTITLEMENT_CHECK = 1;
    private final AtomicBoolean mOnSvcProv = new AtomicBoolean(false);
    private String mServerData;
    private String mServerUrl;
    private int simSlot = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();

    public EntitlementAndE911AidCheckFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
        super(looper, context, baseFlowImpl, databaseHelper);
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleEntitlementCheckResponse(Bundle bundleNSDSResponses) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS, -1);
        if (bundleNSDSResponses == null) {
            return nsdsResponseStatus;
        }
        int errorResponseCode = getHttpErrRespCode(bundleNSDSResponses);
        String errorResponseReason = getHttpErrRespReason(bundleNSDSResponses);
        if (errorResponseCode > 0 || errorResponseReason != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "handleEntitlementCheckResponse: http error code = " + errorResponseCode + ", reason = " + errorResponseReason);
            nsdsResponseStatus.responseCode = NSDSNamespaces.NSDSDefinedResponseCode.HTTP_TRANSACTION_ERROR_CODE;
            return nsdsResponseStatus;
        }
        ResponseServiceEntitlementStatus entitlementStatus = (ResponseServiceEntitlementStatus) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS);
        ResponseGetMSISDN responseGetMsisdn = (ResponseGetMSISDN) bundleNSDSResponses.getParcelable(NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN);
        if (entitlementStatus != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "ResponseServiceEntitlementStatus : messageId:" + entitlementStatus.messageId + ", responseCode:" + entitlementStatus.responseCode);
            if (entitlementStatus.responseCode == 1000) {
                Iterator<T> it = emptyIfNull(entitlementStatus.serviceEntitlementList).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ServiceEntitlement se = (ServiceEntitlement) it.next();
                    if ("vowifi".equals(se.serviceName)) {
                        String str3 = LOG_TAG;
                        IMSLog.i(str3, "service responseCode: " + se.entitlementStatus + ", onDemandProv: " + se.onDemandProv);
                        nsdsResponseStatus.responseCode = se.entitlementStatus;
                        if (se.entitlementStatus == 1048) {
                            if (!EntFeatureDetector.checkWFCAutoOnEnabled(this.simSlot)) {
                                this.mOnSvcProv.set(true);
                            } else if (se.onDemandProv.booleanValue()) {
                                this.mOnSvcProv.set(true);
                            }
                        }
                    }
                }
            } else {
                nsdsResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.SERVICE_ENTITLEMENT_STATUS;
                nsdsResponseStatus.responseCode = entitlementStatus.responseCode;
            }
        }
        if (responseGetMsisdn != null) {
            if (responseGetMsisdn.responseCode == 1000) {
                LineDetail mNativeLineDetail = new LineDetail();
                String str4 = LOG_TAG;
                IMSLog.i(str4, "responseGetMsisdn content : messageId:" + responseGetMsisdn.messageId + ", responseCode:" + responseGetMsisdn.responseCode + ", msisdn:" + responseGetMsisdn.msisdn);
                String str5 = LOG_TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("service_fingerprint:");
                sb.append(responseGetMsisdn.serviceFingerprint);
                IMSLog.s(str5, sb.toString());
                if (!(responseGetMsisdn.responseCode != 1000 || responseGetMsisdn.msisdn == null || responseGetMsisdn.serviceFingerprint == null)) {
                    mNativeLineDetail.lineId = this.mNSDSDatabaseHelper.insertOrUpdateNativeLine(0, this.mBaseFlowImpl.getDeviceId(), responseGetMsisdn);
                    mNativeLineDetail.msisdn = responseGetMsisdn.msisdn;
                    mNativeLineDetail.serviceFingerPrint = responseGetMsisdn.serviceFingerprint;
                }
            } else {
                nsdsResponseStatus.methodName = NSDSNamespaces.NSDSMethodNamespace.GET_MSISDN;
                nsdsResponseStatus.responseCode = responseGetMsisdn.responseCode;
            }
        }
        return nsdsResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRegisterPushTokenResponse(Bundle bundleNSDSResponses) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, (String) null, -1);
        if (bundleNSDSResponses == null) {
            return nsdsResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundleNSDSResponses.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responseManagePushToken for token registration : messageId:" + responseManagePushToken.messageId + ", responseCode:" + responseManagePushToken.responseCode);
            nsdsResponseStatus.responseCode = responseManagePushToken.responseCode;
            if (responseManagePushToken.responseCode != 1000) {
                IMSLog.i(LOG_TAG, "responseManagePushToken failed");
                nsdsResponseStatus.failedOperation = 0;
            }
        } else {
            IMSLog.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        return nsdsResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleRemovePushTokenResponse(Bundle bundleNSDSResponses) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(1000, (String) null, -1);
        if (bundleNSDSResponses == null) {
            return nsdsResponseStatus;
        }
        ResponseManagePushToken responseManagePushToken = (ResponseManagePushToken) bundleNSDSResponses.getParcelable("managePushToken");
        if (responseManagePushToken != null) {
            String str = LOG_TAG;
            IMSLog.i(str, "responseManagePushToken for token removal : messageId:" + responseManagePushToken.messageId + ", responseCode:" + responseManagePushToken.responseCode);
            nsdsResponseStatus.responseCode = responseManagePushToken.responseCode;
            if (responseManagePushToken.responseCode != 1000) {
                IMSLog.i(LOG_TAG, "responseManagePushToken failed");
                nsdsResponseStatus.failedOperation = 1;
            }
        } else {
            IMSLog.e(LOG_TAG, "responseManagePushToken is NULL");
        }
        if (this.mDeviceEventType == 3) {
            nsdsResponseStatus.responseCode = 1000;
        }
        return nsdsResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleManageLocationAndTcResponse(ResponseManageLocationAndTC responseLocation) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, -1);
        if (responseLocation != null) {
            if (responseLocation.responseCode == 1000) {
                this.mServerData = responseLocation.serverData;
                this.mServerUrl = responseLocation.serverUrl;
                String str = LOG_TAG;
                IMSLog.i(str, "onResponseAvailable: update location and tc status in db. E911 AID received: " + responseLocation.addressId);
                this.mNSDSDatabaseHelper.updateLocationAndTcStatus((long) this.mNSDSDatabaseHelper.getNativeLineId(this.mBaseFlowImpl.getDeviceId()), responseLocation, this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            }
            nsdsResponseStatus.responseCode = responseLocation.responseCode;
        }
        return nsdsResponseStatus;
    }

    private void handleLocAndTcWebsheetResult(Bundle bundle, boolean isSvcProv) {
        int i;
        int result = 0;
        if (bundle != null) {
            result = bundle.getInt(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_RESULT_CODE);
        }
        notifyCallbackForNsdsEvent(5);
        String str = LOG_TAG;
        IMSLog.i(str, "handleLocAndTcWebsheetResult: result " + result);
        IMSLog.c(LogClass.ES_WEBSHEET_RESULT, "WBSHT RESULT:" + result);
        if (isSvcProv) {
            i = 7;
        } else {
            i = 12;
        }
        this.mDeviceEventType = i;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(getLocAndTcWebsheetRespCode(result), (String) null, -1), (Bundle) null);
    }

    private void retryEntitlementAutoOn(int operation) {
        IMSLog.i(LOG_TAG, "[ATT_AutoOn] EntitlementAutoOn");
        String autoActivate = NSDSSharedPrefHelper.get(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
        if (TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY, autoActivate) || TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS, autoActivate)) {
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
            if (operation == 0 || !TextUtils.equals(NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY, autoActivate)) {
                IMSLog.i(LOG_TAG, "[ATT_AutoOn] retry EntitlementAutoOn");
                NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_RETRY);
                return;
            }
            IMSLog.i(LOG_TAG, "[ATT_AutoOn] EntitlementAutoOn - fail : remove token");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS);
            performRemovePushToken(3);
        }
    }

    private void failEntitlementAutoOn() {
        IMSLog.i(LOG_TAG, "[ATT_AutoOn] failEntitlementAutoOn - reset token in device");
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER);
        notifyNSDSFlowResponse(true, (String) null, -1, 1000);
    }

    public void performEntitlementCheck(int deviceEventType, int retryCount) {
        String str = LOG_TAG;
        IMSLog.i(str, "performEntitlementCheck: deviceEventType " + deviceEventType + " retryCount " + retryCount + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "performEntitlementCheck: entitlement in progress");
            deferMessage(obtainMessage(8, deviceEventType, retryCount));
            return;
        }
        sendMessage(obtainMessage(8, deviceEventType, retryCount));
    }

    public void performRemovePushToken(int deviceEventType) {
        String str = LOG_TAG;
        IMSLog.i(str, "performRemovePushToken: deviceEventType " + deviceEventType + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "performRemovePushToken: entitlement in progress");
            deferMessage(obtainMessage(9, deviceEventType, 0));
            return;
        }
        sendMessage(obtainMessage(9, deviceEventType, 0));
        clearDeferredMessage();
    }

    public void performE911AddressUpdate(int deviceEventType) {
        String str = LOG_TAG;
        IMSLog.i(str, "performE911AddressUpdate: deviceEventType " + deviceEventType + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "performE911AddressUpdate: entitlement in progress");
            deferMessage(obtainMessage(10, deviceEventType, 0));
            return;
        }
        sendMessage(obtainMessage(10, deviceEventType, 0));
    }

    private void performNextOperation(int deviceEventType, int retryCount, String nsdsMethod) {
        String str = LOG_TAG;
        IMSLog.i(str, "performNextOperation: deviceEventType " + deviceEventType + " nsdsMethod " + nsdsMethod);
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE, NSDSNamespaces.NSDSDeviceState.ENTITLMENT_IN_PROGRESS);
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(1000, nsdsMethod, -1), getE911AidValidationBundle());
    }

    private void checkLocationAndTC() {
        IMSLog.i(LOG_TAG, "checkLocationAndTC()");
        LineDetail nativeLineDetail = this.mNSDSDatabaseHelper.getNativeLineDetail(this.mBaseFlowImpl.getDeviceId(), true);
        if (nativeLineDetail == null) {
            IMSLog.e(LOG_TAG, "checkLocationAndTC: native line detail is null");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE);
            notifyNSDSFlowResponse(false, (String) null, -1, -1);
            return;
        }
        new LocationRegistrationAndTCAcceptanceCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkLocationAndTC(nativeLineDetail.serviceFingerPrint, false, 30000);
    }

    private void checkVoWifiEntitlement() {
        IMSLog.i(LOG_TAG, "checkVoWifiEntitlement: requesting entitlement check");
        List<String> serviceList = new ArrayList<>();
        serviceList.add("vowifi");
        new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkBulkEntitlement(serviceList, false, 30000);
    }

    private void registerPushToken() {
        IMSLog.i(LOG_TAG, "registerPushToken: requesting push token registration");
        new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").registerVoWiFiPushToken(this.mNSDSDatabaseHelper.getNativeMsisdn(this.mBaseFlowImpl.getDeviceId()), (String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, false, 30000);
    }

    private void removePushToken() {
        IMSLog.i(LOG_TAG, "removePushToken: requesting push token de-registration");
        new OperationUsingManagePushToken(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").removeVoWiFiPushToken(this.mNSDSDatabaseHelper.getNativeMsisdn(this.mBaseFlowImpl.getDeviceId()), (String) null, PushTokenHelper.getPushToken(this.mContext, this.mBaseFlowImpl.getDeviceId()), NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM, false, 30000);
    }

    private void openLocAndTCWebsheet(boolean isSvcProv) {
        int message;
        Intent intent;
        LocAndTcWebSheetData webSheetData = getLocAndTcWebSheetData();
        if (webSheetData != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLocAndTCWebsheet: url " + webSheetData.url + ", serverData " + webSheetData.token + ", clientName " + webSheetData.clientName + ", title " + webSheetData.title);
            Bundle bundle = new Bundle();
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_URL, webSheetData.url);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_DATA, webSheetData.token);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_CLIENT_NAME, webSheetData.clientName);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_TITLE, webSheetData.title);
            if (isSvcProv) {
                message = 6;
            } else {
                message = 7;
            }
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOC_AND_TC_WEBSHEET_RESULT_MESSAGE, obtainMessage(message));
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOCATION_AND_TC_MESSENGER, new Messenger(this));
            Intent intent2 = new Intent();
            if (checkSntMode()) {
                intent = intent2.setAction(NSDSNamespaces.NSDSActions.SNT_MODE_LOCATIONANDTC_OPEN_WEBSHEET);
            } else {
                intent = intent2.setAction(NSDSNamespaces.NSDSActions.UNIFIED_WFC_LOCATIONANDTC_OPEN_WEBSHEET);
            }
            intent.putExtras(bundle);
            intent.setFlags(LogClass.SIM_EVENT);
            this.mContext.startActivity(intent);
            notifyCallbackForNsdsEvent(4);
            return;
        }
        IMSLog.e(LOG_TAG, "openLocAndTCWebsheet: missing server info, failed");
        notifyNSDSFlowResponse(false, (String) null, -1, -1);
    }

    private boolean checkSntMode() {
        if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0)) {
            return true;
        }
        return false;
    }

    private Bundle getLocationAndTCStatusBundle(ResponseManageLocationAndTC responseLocation) {
        Bundle bundle = new Bundle();
        if (responseLocation != null) {
            boolean locAndTcStatus = false;
            if ((responseLocation.locationStatus == null || responseLocation.locationStatus.booleanValue()) && (responseLocation.tcStatus == null || responseLocation.tcStatus.booleanValue())) {
                locAndTcStatus = true;
            }
            bundle.putBoolean(NSDSNamespaces.NSDSDataMapKey.SVC_PROV_STATUS, this.mOnSvcProv.get());
            bundle.putBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS, locAndTcStatus);
            bundle.putString(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_SERVER_URL, responseLocation.serverUrl);
            bundle.putString(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_SERVER_DATA, responseLocation.serverData);
            String str = LOG_TAG;
            IMSLog.i(str, "getLocationAndTCStatusBundle: " + locAndTcStatus);
        }
        return bundle;
    }

    private Bundle getE911AidValidationBundle() {
        String e911AidExp = this.mNSDSDatabaseHelper.getNativeLineE911AidExp(this.mBaseFlowImpl.getDeviceId());
        Bundle bundle = new Bundle();
        bundle.putString(NSDSNamespaces.NSDSDataMapKey.E911_AID_EXP, e911AidExp);
        bundle.putBoolean(NSDSNamespaces.NSDSDataMapKey.SVC_PROV_STATUS, this.mOnSvcProv.get());
        String str = LOG_TAG;
        IMSLog.s(str, "getE911AidValidationBundle: " + e911AidExp + ", OnSvcProv:" + this.mOnSvcProv.get());
        return bundle;
    }

    private int getLocAndTcWebsheetRespCode(int result) {
        if (result == 0) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE;
        }
        if (result == 1) {
            return 1000;
        }
        if (result == 2) {
            return NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_CANCEL_CODE;
        }
        if (result != 3) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
        }
        return NSDSNamespaces.NSDSDefinedResponseCode.VOID_WEBSHEET_TRANSACTION;
    }

    private LocAndTcWebSheetData getLocAndTcWebSheetData() {
        if (getMnoNsdsStrategy() != null) {
            return getMnoNsdsStrategy().getLocAndTcWebSheetData(this.mServerUrl, this.mServerData);
        }
        return null;
    }

    private void updateEntitlementStatus(int errorCode) {
        this.mDeviceEventType = 0;
        this.mOnSvcProv.set(false);
        this.mRetryCount = 0;
        NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        if (errorCode == 2303) {
            this.mNSDSDatabaseHelper.resetE911AidInfoForNativeLine(this.mBaseFlowImpl.getDeviceId());
            IMSLog.i(LOG_TAG, "updateEntitlementStatus: svc de-provision success");
            NSDSSharedPrefHelper.remove(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE);
        }
        if (errorCode == 2302 || errorCode == 2502) {
            clearDeferredMessage();
            IMSLog.i(LOG_TAG, "updateEntitlementStatus: svc provision success");
            NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_SVC_PROV_STATE, NSDSNamespaces.NSDSDeviceState.SERVICE_PROVISIONED);
            return;
        }
        moveDeferredMessageAtFrontOfQueue();
    }

    private boolean updateResponseResult(boolean success, int errorCode) {
        if (!success) {
            return false;
        }
        if (errorCode == 1000 || errorCode == 2303 || errorCode == 2501 || errorCode == 2502 || errorCode == 2302) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
        int msgId = -1;
        if (nextOperation == 2) {
            msgId = 1;
        } else if (nextOperation == 3) {
            msgId = 0;
        } else if (nextOperation == 4) {
            msgId = 2;
        } else if (nextOperation == 5) {
            msgId = 3;
        } else if (nextOperation == 8) {
            msgId = 4;
        } else if (nextOperation != 13) {
            switch (nextOperation) {
                case 17:
                    msgId = 11;
                    break;
                case 18:
                    msgId = 12;
                    break;
                case 19:
                    msgId = 13;
                    break;
                case 20:
                    msgId = 14;
                    break;
                default:
                    IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
                    break;
            }
        } else {
            msgId = 5;
        }
        if (msgId != -1) {
            Message message = obtainMessage(msgId);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    /* JADX WARNING: type inference failed for: r1v11, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r6) {
        /*
            r5 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "msg:"
            r1.append(r2)
            int r2 = r6.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r1)
            int r0 = r6.what
            r1 = 101(0x65, float:1.42E-43)
            r2 = 2
            if (r0 == r1) goto L_0x00ff
            r1 = 104(0x68, float:1.46E-43)
            java.lang.String r3 = "manageLocationAndTC"
            if (r0 == r1) goto L_0x00b7
            r1 = 112(0x70, float:1.57E-43)
            if (r0 == r1) goto L_0x00a9
            r1 = 113(0x71, float:1.58E-43)
            if (r0 == r1) goto L_0x0098
            r1 = 0
            r4 = 1
            switch(r0) {
                case 0: goto L_0x0093;
                case 1: goto L_0x008e;
                case 2: goto L_0x0089;
                case 3: goto L_0x0084;
                case 4: goto L_0x007f;
                case 5: goto L_0x007a;
                case 6: goto L_0x0071;
                case 7: goto L_0x0068;
                case 8: goto L_0x005c;
                case 9: goto L_0x0051;
                case 10: goto L_0x0048;
                case 11: goto L_0x0043;
                case 12: goto L_0x003e;
                case 13: goto L_0x0039;
                case 14: goto L_0x0034;
                default: goto L_0x0032;
            }
        L_0x0032:
            goto L_0x010f
        L_0x0034:
            r5.failEntitlementAutoOn()
            goto L_0x010f
        L_0x0039:
            r5.retryEntitlementAutoOn(r2)
            goto L_0x010f
        L_0x003e:
            r5.retryEntitlementAutoOn(r4)
            goto L_0x010f
        L_0x0043:
            r5.retryEntitlementAutoOn(r1)
            goto L_0x010f
        L_0x0048:
            int r0 = r6.arg1
            int r1 = r6.arg2
            r5.performNextOperation(r0, r1, r3)
            goto L_0x010f
        L_0x0051:
            int r0 = r6.arg1
            int r1 = r6.arg2
            java.lang.String r2 = "managePushToken"
            r5.performNextOperation(r0, r1, r2)
            goto L_0x010f
        L_0x005c:
            int r0 = r6.arg1
            int r1 = r6.arg2
            java.lang.String r2 = "serviceEntitlementStatus"
            r5.performNextOperation(r0, r1, r2)
            goto L_0x010f
        L_0x0068:
            android.os.Bundle r0 = r6.getData()
            r5.handleLocAndTcWebsheetResult(r0, r1)
            goto L_0x010f
        L_0x0071:
            android.os.Bundle r0 = r6.getData()
            r5.handleLocAndTcWebsheetResult(r0, r4)
            goto L_0x010f
        L_0x007a:
            r5.openLocAndTCWebsheet(r1)
            goto L_0x010f
        L_0x007f:
            r5.openLocAndTCWebsheet(r4)
            goto L_0x010f
        L_0x0084:
            r5.removePushToken()
            goto L_0x010f
        L_0x0089:
            r5.registerPushToken()
            goto L_0x010f
        L_0x008e:
            r5.checkVoWifiEntitlement()
            goto L_0x010f
        L_0x0093:
            r5.checkLocationAndTC()
            goto L_0x010f
        L_0x0098:
            r0 = 5
            android.os.Bundle r1 = r6.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r1 = r5.handleRemovePushTokenResponse(r1)
            android.os.Bundle r2 = r6.getData()
            r5.performNextOperationIf(r0, r1, r2)
            goto L_0x010f
        L_0x00a9:
            r0 = 4
            android.os.Bundle r1 = r6.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r1 = r5.handleRegisterPushTokenResponse(r1)
            r2 = 0
            r5.performNextOperationIf(r0, r1, r2)
            goto L_0x010f
        L_0x00b7:
            r0 = 0
            android.os.Bundle r1 = r6.getData()
            if (r1 == 0) goto L_0x00c9
            android.os.Bundle r1 = r6.getData()
            android.os.Parcelable r1 = r1.getParcelable(r3)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC) r0
        L_0x00c9:
            r1 = 3
            android.content.Context r2 = r5.mContext
            com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl r3 = r5.mBaseFlowImpl
            java.lang.String r3 = r3.getDeviceId()
            java.lang.String r4 = "activate_after_oos"
            java.lang.String r2 = com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.get(r2, r3, r4)
            int r3 = r5.simSlot
            boolean r3 = com.sec.internal.ims.entitlement.util.EntFeatureDetector.checkWFCAutoOnEnabled(r3)
            if (r3 == 0) goto L_0x00f3
            if (r2 == 0) goto L_0x00f3
            java.lang.String r3 = "completed"
            boolean r3 = r3.equals(r2)
            if (r3 != 0) goto L_0x00f3
            java.lang.String r3 = LOG_TAG
            java.lang.String r4 = "[ATT_AutoOn] InProgress - CHECK_LOC_AND_TC_AUTO_ON"
            com.sec.internal.log.IMSLog.i(r3, r4)
            r1 = 16
        L_0x00f3:
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r3 = r5.handleManageLocationAndTcResponse(r0)
            android.os.Bundle r4 = r5.getLocationAndTCStatusBundle(r0)
            r5.performNextOperationIf(r1, r3, r4)
            goto L_0x010f
        L_0x00ff:
            android.os.Bundle r0 = r5.getE911AidValidationBundle()
            android.os.Bundle r1 = r6.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r1 = r5.handleEntitlementCheckResponse(r1)
            r5.performNextOperationIf(r2, r1, r0)
        L_0x010f:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.EntitlementAndE911AidCheckFlow.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        ArrayList<Integer> translatedErrorCodes = new ArrayList<>();
        int errorCode = ATTWfcErrorCodeTranslator.translateErrorCode(this.mNSDSDatabaseHelper, this.mDeviceEventType, success, nsdsErrorCode, this.mRetryCount, this.mBaseFlowImpl.getDeviceId());
        translatedErrorCodes.add(Integer.valueOf(errorCode));
        boolean success2 = updateResponseResult(success, errorCode);
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: success " + success2);
        IMSLog.c(LogClass.ES_NSDS_RESULT, "SUCS:" + success2 + ", ERRC:" + translatedErrorCodes);
        if (2304 != errorCode) {
            Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_AND_LOCTC_CHECK_COMPLETED);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success2);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQ_TOGGLE_OFF_OP, false);
            intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErrorCodes);
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
        updateEntitlementStatus(errorCode);
    }
}
