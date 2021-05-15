package com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ResponseServiceEntitlementStatus;
import com.sec.internal.constants.ims.entitilement.data.ServiceEntitlement;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BulkEntitlementCheck;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.LocationRegistrationAndTCAcceptanceCheck;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XaaEntitlementCheckFlow extends NSDSAppFlowBase implements IEntitlementCheck {
    private static final int INIT_E911_ADDRESS_UPDATE = 7;
    private static final int INIT_ENTITLEMENT_CHECK = 6;
    protected static final int LOCATION_AND_TC_CHECK = 0;
    private static final String LOG_TAG = XaaEntitlementCheckFlow.class.getSimpleName();
    protected static final int OPEN_E911_ADDRESS_UPDATE_WEBSHEET = 3;
    protected static final int OPEN_LOC_AND_TC_WEBSHEET = 2;
    protected static final int RESULT_SVC_PROV_LOC_AND_TC_WEBSHEET = 4;
    protected static final int RESULT_UPDATE_LOC_AND_TC_WEBSHEET = 5;
    protected static final int VOWIFI_ENTITLEMENT_CHECK = 1;
    protected String mServerData;
    protected String mServerUrl;

    public XaaEntitlementCheckFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper databaseHelper) {
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
        if (entitlementStatus != null) {
            String str2 = LOG_TAG;
            IMSLog.i(str2, "ResponseServiceEntitlementStatus :messageId:" + entitlementStatus.messageId + "responseCode:" + entitlementStatus.responseCode);
            if (entitlementStatus.responseCode == 1000) {
                Iterator<T> it = emptyIfNull(entitlementStatus.serviceEntitlementList).iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ServiceEntitlement se = (ServiceEntitlement) it.next();
                    if ("vowifi".equals(se.serviceName)) {
                        String str3 = LOG_TAG;
                        IMSLog.i(str3, "service responseCode:" + se.entitlementStatus);
                        nsdsResponseStatus.responseCode = se.entitlementStatus;
                        break;
                    }
                }
            } else {
                nsdsResponseStatus.responseCode = entitlementStatus.responseCode;
            }
        }
        return nsdsResponseStatus;
    }

    private NSDSAppFlowBase.NSDSResponseStatus handleManageLocationAndTcResponse(ResponseManageLocationAndTC responseLocation) {
        NSDSAppFlowBase.NSDSResponseStatus nsdsResponseStatus = new NSDSAppFlowBase.NSDSResponseStatus(-1, NSDSNamespaces.NSDSMethodNamespace.MANAGE_LOC_AND_TC, -1);
        if (responseLocation != null) {
            if (responseLocation.responseCode == 1000) {
                this.mServerData = responseLocation.serverData;
                this.mServerUrl = responseLocation.serverUrl;
                IMSLog.i(LOG_TAG, "onResponseAvailable: update location and tc status in db");
                this.mNSDSDatabaseHelper.updateLocationAndTcStatus((long) this.mNSDSDatabaseHelper.getNativeLineId(this.mBaseFlowImpl.getDeviceId()), responseLocation, this.mBaseFlowImpl.getDeviceId(), this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            }
            nsdsResponseStatus.responseCode = responseLocation.responseCode;
        }
        return nsdsResponseStatus;
    }

    private void handleLocAndTcWebsheetResult(Bundle bundle, boolean isSvcProv) {
        int result = 0;
        if (bundle != null) {
            result = bundle.getInt(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_RESULT_CODE);
        }
        notifyCallbackForNsdsEvent(5);
        String str = LOG_TAG;
        IMSLog.i(str, "handleLocAndTcWebsheetResult: result " + result);
        this.mDeviceEventType = isSvcProv ? 7 : 12;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(getLocAndTcWebsheetRespCode(result), (String) null, -1), (Bundle) null);
    }

    public void performEntitlementCheck(int deviceEventType, int retryCount) {
        String str = LOG_TAG;
        IMSLog.i(str, "performEntitlementCheck: deviceEventType " + deviceEventType + " retryCount " + retryCount + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "performEntitlementCheck: entitlement in progress");
            deferMessage(obtainMessage(6, deviceEventType, retryCount));
            return;
        }
        sendMessage(obtainMessage(6, deviceEventType, retryCount));
    }

    public void performRemovePushToken(int deviceEventType) {
        IMSLog.e(LOG_TAG, "performRemovePushToken: not supported");
        notifyNSDSFlowResponse(true, (String) null, -1, -1);
    }

    public void performE911AddressUpdate(int deviceEventType) {
        String str = LOG_TAG;
        IMSLog.i(str, "performE911AddressUpdate: deviceEventType " + deviceEventType + " ongoingEvent " + this.mDeviceEventType);
        if (NSDSSharedPrefHelper.isDeviceInEntitlementProgress(this.mContext, this.mBaseFlowImpl.getDeviceId())) {
            IMSLog.i(LOG_TAG, "performE911AddressUpdate: entitlement in progress");
            deferMessage(obtainMessage(7, deviceEventType, 0));
            return;
        }
        sendMessage(obtainMessage(7, deviceEventType, 0));
    }

    private void performNextOperation(int deviceEventType, int retryCount, String nsdsMethod) {
        String str = LOG_TAG;
        IMSLog.i(str, "performNextOperation: deviceEventType " + deviceEventType + " nsdsMethod " + nsdsMethod);
        NSDSSharedPrefHelper.save(this.mContext, this.mBaseFlowImpl.getDeviceId(), NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE, NSDSNamespaces.NSDSDeviceState.ENTITLMENT_IN_PROGRESS);
        this.mDeviceEventType = deviceEventType;
        this.mRetryCount = retryCount;
        performNextOperationIf(-1, new NSDSAppFlowBase.NSDSResponseStatus(1000, nsdsMethod, -1), (Bundle) null);
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
        new BulkEntitlementCheck(getLooper(), this.mContext, this.mBaseFlowImpl, new Messenger(this), "1.0").checkBulkEntitlement(serviceList, true, 30000);
    }

    private void openLocAndTCWebsheet(boolean isSvcProv) {
        Intent intent;
        LocAndTcWebSheetData webSheetData = getLocAndTcWebSheetData();
        if (webSheetData != null) {
            String str = LOG_TAG;
            IMSLog.s(str, "openLocAndTCWebsheet: url " + webSheetData.url + "serverData " + webSheetData.token + "clientName " + webSheetData.clientName + "title " + webSheetData.title);
            Bundle bundle = new Bundle();
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_URL, webSheetData.url);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_DATA, webSheetData.token);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_CLIENT_NAME, webSheetData.clientName);
            bundle.putString(NSDSNamespaces.NSDSExtras.LOCATIONANDTC_TITLE, webSheetData.title);
            bundle.putParcelable(NSDSNamespaces.NSDSExtras.LOC_AND_TC_WEBSHEET_RESULT_MESSAGE, obtainMessage(isSvcProv ? 4 : 5));
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
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0;
    }

    private Bundle getLocationAndTCStatusBundle(ResponseManageLocationAndTC responseLocation) {
        Bundle bundle = new Bundle();
        if (responseLocation != null) {
            boolean locAndTcStatus = false;
            if ((responseLocation.locationStatus == null || responseLocation.locationStatus.booleanValue()) && (responseLocation.tcStatus == null || responseLocation.tcStatus.booleanValue())) {
                locAndTcStatus = true;
            }
            bundle.putBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS, locAndTcStatus);
            bundle.putString(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_SERVER_URL, responseLocation.serverUrl);
            bundle.putString(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_SERVER_DATA, responseLocation.serverData);
            String str = LOG_TAG;
            IMSLog.i(str, "getLocationAndTCStatusBundle: " + locAndTcStatus);
        }
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
        return success && (errorCode == 1000 || errorCode == 2303 || errorCode == 2501 || errorCode == 2502 || errorCode == 2302);
    }

    /* access modifiers changed from: protected */
    public void queueOperation(int nextOperation, Bundle dataMap) {
        int msgId = -1;
        if (nextOperation == 2) {
            msgId = 1;
        } else if (nextOperation == 3) {
            msgId = 0;
        } else if (nextOperation == 8) {
            msgId = 2;
        } else if (nextOperation != 13) {
            IMSLog.i(LOG_TAG, "queueOperation: did not match any nsds base operations");
        } else {
            msgId = 3;
        }
        if (msgId != -1) {
            Message message = obtainMessage(msgId);
            message.setData(dataMap);
            sendMessage(message);
        }
    }

    /* JADX WARNING: type inference failed for: r1v9, types: [android.os.Parcelable] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(android.os.Message r5) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "msg:"
            r1.append(r2)
            int r2 = r5.what
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r0, r1)
            int r0 = r5.what
            r1 = 101(0x65, float:1.42E-43)
            if (r0 == r1) goto L_0x007c
            r1 = 104(0x68, float:1.46E-43)
            java.lang.String r2 = "manageLocationAndTC"
            if (r0 == r1) goto L_0x005d
            r1 = 0
            r3 = 1
            switch(r0) {
                case 0: goto L_0x0059;
                case 1: goto L_0x0055;
                case 2: goto L_0x0051;
                case 3: goto L_0x004d;
                case 4: goto L_0x0045;
                case 5: goto L_0x003d;
                case 6: goto L_0x0032;
                case 7: goto L_0x002a;
                default: goto L_0x0029;
            }
        L_0x0029:
            goto L_0x008a
        L_0x002a:
            int r0 = r5.arg1
            int r1 = r5.arg2
            r4.performNextOperation(r0, r1, r2)
            goto L_0x008a
        L_0x0032:
            int r0 = r5.arg1
            int r1 = r5.arg2
            java.lang.String r2 = "serviceEntitlementStatus"
            r4.performNextOperation(r0, r1, r2)
            goto L_0x008a
        L_0x003d:
            android.os.Bundle r0 = r5.getData()
            r4.handleLocAndTcWebsheetResult(r0, r1)
            goto L_0x008a
        L_0x0045:
            android.os.Bundle r0 = r5.getData()
            r4.handleLocAndTcWebsheetResult(r0, r3)
            goto L_0x008a
        L_0x004d:
            r4.openLocAndTCWebsheet(r1)
            goto L_0x008a
        L_0x0051:
            r4.openLocAndTCWebsheet(r3)
            goto L_0x008a
        L_0x0055:
            r4.checkVoWifiEntitlement()
            goto L_0x008a
        L_0x0059:
            r4.checkLocationAndTC()
            goto L_0x008a
        L_0x005d:
            r0 = 0
            android.os.Bundle r1 = r5.getData()
            if (r1 == 0) goto L_0x006f
            android.os.Bundle r1 = r5.getData()
            android.os.Parcelable r1 = r1.getParcelable(r2)
            r0 = r1
            com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC r0 = (com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC) r0
        L_0x006f:
            r1 = 3
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r2 = r4.handleManageLocationAndTcResponse(r0)
            android.os.Bundle r3 = r4.getLocationAndTCStatusBundle(r0)
            r4.performNextOperationIf(r1, r2, r3)
            goto L_0x008a
        L_0x007c:
            r0 = 2
            android.os.Bundle r1 = r5.getData()
            com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.NSDSAppFlowBase$NSDSResponseStatus r1 = r4.handleEntitlementCheckResponse(r1)
            r2 = 0
            r4.performNextOperationIf(r0, r1, r2)
        L_0x008a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaEntitlementCheckFlow.handleMessage(android.os.Message):void");
    }

    /* access modifiers changed from: protected */
    public void notifyNSDSFlowResponse(boolean success, String nsdsMethodName, int operation, int nsdsErrorCode) {
        ArrayList<Integer> translatedErrorCodes = new ArrayList<>();
        int errorCode = XaaWfcErrorCodeTranslator.translateErrorCode(this.mDeviceEventType, success, nsdsErrorCode);
        translatedErrorCodes.add(Integer.valueOf(errorCode));
        boolean success2 = updateResponseResult(success, errorCode);
        String str = LOG_TAG;
        IMSLog.i(str, "notifyNSDSFlowResponse: success " + success2);
        if (2304 != errorCode) {
            Intent intent = new Intent(NSDSNamespaces.NSDSActions.ENTITLEMENT_CHECK_COMPLETED);
            intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, this.mBaseFlowImpl.getSimManager().getSimSlotIndex());
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQUEST_STATUS, success2);
            intent.putExtra(NSDSNamespaces.NSDSExtras.DEVICE_EVENT_TYPE, this.mDeviceEventType);
            intent.putExtra(NSDSNamespaces.NSDSExtras.REQ_TOGGLE_OFF_OP, true);
            intent.putIntegerArrayListExtra(NSDSNamespaces.NSDSExtras.ERROR_CODES, translatedErrorCodes);
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
        }
        updateEntitlementStatus(errorCode);
    }
}
