package com.sec.internal.ims.entitlement.storagehelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LineDetail;
import com.sec.internal.constants.ims.entitilement.data.ResponseGetMSISDN;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageLocationAndTC;
import com.sec.internal.constants.ims.entitilement.data.ServiceInstanceDetail;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.entitlement.util.DeviceNameHelper;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.entitlement.util.SimSwapNSDSConfigHelper;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NSDSDatabaseHelper {
    private static final String LOG_TAG = NSDSDatabaseHelper.class.getSimpleName();
    protected Context mContext;
    protected ContentResolver mResolver;

    public NSDSDatabaseHelper(Context context) {
        Context createCredentialProtectedStorageContext = context.createCredentialProtectedStorageContext();
        this.mContext = createCredentialProtectedStorageContext;
        this.mResolver = createCredentialProtectedStorageContext.getContentResolver();
    }

    public void insertOrUpdateGcmPushToken(String senderId, String token, String protocolToServer, String deviceUid) {
        String str = LOG_TAG;
        IMSLog.s(str, "insertOrUpdateForGcmToken: token " + token + ", senderId " + senderId);
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(senderId)) {
            IMSLog.e(LOG_TAG, "insertFcmToken: empty or null input");
        } else if (isGcmTokenAvailable(senderId, deviceUid)) {
            updateGcmPushToken(token, senderId, deviceUid);
        } else {
            insertGcmPushToken(token, senderId, protocolToServer, deviceUid);
        }
    }

    private void insertGcmPushToken(String token, String senderId, String protocolToServer, String deviceUid) {
        IMSLog.i(LOG_TAG, "insertGcmPushToken()");
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.GcmTokensColumns.GCM_TOKEN, token);
        values.put(NSDSContractExt.GcmTokensColumns.SENDER_ID, senderId);
        values.put(NSDSContractExt.GcmTokensColumns.PROTOCOL_TO_SERVER, protocolToServer);
        values.put("device_uid", deviceUid);
        if (this.mResolver.insert(NSDSContractExt.GcmTokens.CONTENT_URI, values) != null) {
            IMSLog.i(LOG_TAG, "inserted GCM token successfully");
        }
    }

    private void updateGcmPushToken(String token, String senderId, String deviceUid) {
        IMSLog.i(LOG_TAG, "updateGcmPushToken()");
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.GcmTokensColumns.GCM_TOKEN, token);
        if (this.mResolver.update(NSDSContractExt.GcmTokens.CONTENT_URI, values, "sender_id = ? AND device_uid = ?", new String[]{senderId, deviceUid}) > 0) {
            String str = LOG_TAG;
            IMSLog.s(str, "update GCM token for sender ID: " + senderId + " for deviceId:" + deviceUid);
        }
    }

    public boolean isGcmTokenAvailable(String senderId, String deviceUid) {
        if (senderId != null && getGcmToken(senderId, deviceUid) != null) {
            return true;
        }
        IMSLog.i(LOG_TAG, "isGcmTokenAvailable: no GCM token");
        return false;
    }

    public String getGcmToken(String senderId, String deviceUid) {
        Cursor cursor = this.mResolver.query(NSDSContractExt.GcmTokens.CONTENT_URI, new String[]{NSDSContractExt.GcmTokensColumns.GCM_TOKEN}, "sender_id = ? AND device_uid = ?", new String[]{senderId, deviceUid}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && cursor.getString(0) != null) {
                    String string = cursor.getString(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
        throw th;
    }

    public boolean isDeviceConfigAvailable(String imsi) {
        if (getDeviceConfig(imsi) != null) {
            return true;
        }
        IMSLog.i(LOG_TAG, "isDeviceConfigAvailable: no config");
        return false;
    }

    public static String getConfigVersion(Context context, String imsi) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = EntitlementConfigContract.DeviceConfig.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, new String[]{"version"}, "imsi = ?", new String[]{imsi}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && cursor.getString(0) != null) {
                    String string = cursor.getString(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return "0";
        }
        cursor.close();
        return "0";
        throw th;
    }

    public String getDeviceConfig(String imsi) {
        Uri uriDeviceConfig = EntitlementConfigContract.DeviceConfig.CONTENT_URI;
        ContentResolver contentResolver = this.mResolver;
        Uri uri = uriDeviceConfig;
        Cursor cursor = contentResolver.query(uri, new String[]{"device_config"}, "imsi = ?", new String[]{imsi}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String string = cursor.getString(0);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return string;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
        throw th;
    }

    public void insertDeviceConfig(ResponseManageConnectivity resManageConn, String version, String deviceId) {
        ContentValues values = null;
        if (!TextUtils.isEmpty(resManageConn.deviceConfig)) {
            values = new ContentValues();
            values.put("device_id", deviceId);
            if (version != null) {
                values.put("version", version);
            }
            if (resManageConn.deviceConfig != null) {
                values.put("device_config", resManageConn.deviceConfig);
            }
        }
        if (values != null && values.size() != 0 && this.mResolver.insert(NSDSContractExt.DeviceConfig.CONTENT_URI, values) != null) {
            IMSLog.i(LOG_TAG, "inserted device config in device config successfully");
        }
    }

    public void updateDeviceConfig(ResponseManageConnectivity resManageConn, String version, String deviceId) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateDeviceConfig: version:" + version);
        ContentValues values = null;
        if (!TextUtils.isEmpty(resManageConn.deviceConfig)) {
            values = new ContentValues();
            if (version != null) {
                values.put("version", version);
            }
            if (resManageConn.deviceConfig != null) {
                values.put("device_config", resManageConn.deviceConfig);
            }
        }
        if (values == null || values.size() == 0) {
            IMSLog.i(LOG_TAG, "No update on the config");
            return;
        }
        int noDeletes = this.mResolver.delete(NSDSContractExt.NsdsConfigs.CONTENT_URI, (String) null, (String[]) null);
        String str2 = LOG_TAG;
        IMSLog.i(str2, "No of entries deleted from nsds_config :" + noDeletes);
        if (this.mResolver.update(NSDSContractExt.DeviceConfig.CONTENT_URI, values, "device_id = ?", new String[]{deviceId}) > 0) {
            String str3 = LOG_TAG;
            IMSLog.i(str3, "updated device config in device config successfully with version:" + version + " for deviceId:" + deviceId);
        }
    }

    public void deleteNsdsConfigs(String imsi) {
        if (this.mResolver.delete(NSDSContractExt.NsdsConfigs.CONTENT_URI, "imsi = ?", new String[]{imsi}) > 0) {
            IMSLog.i(LOG_TAG, "Deleted NSDS configs: successfully");
        }
    }

    public void copyConfigEntriesForSimSwap(String deviceId, String prevImsi, int slotId) {
        try {
            IMSLog.i(LOG_TAG, "Copying config entries for sim swap");
            Map<String, String> data = new HashMap<>();
            data.put(SimSwapNSDSConfigHelper.KEY_NATIVE_MSISDN, getNativeMsisdn(deviceId));
            data.put(NSDSNamespaces.NSDSSharedPref.PREF_AKA_TOKEN, NSDSSharedPrefHelper.getAkaToken(this.mContext, prevImsi));
            data.put(NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN, NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN));
            data.put("access_token", NSDSSharedPrefHelper.get(this.mContext, deviceId, "access_token"));
            data.put("imsi", NSDSSharedPrefHelper.getPrefForSlot(this.mContext, slotId, NSDSNamespaces.NSDSSharedPref.PREF_PREV_IMSI));
            data.put(NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP, NSDSSharedPrefHelper.getPrefForSlot(this.mContext, slotId, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP));
            data.put("device_id", NSDSSharedPrefHelper.getPrefForSlot(this.mContext, slotId, "device_id"));
            data.put(NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSSharedPrefHelper.get(this.mContext, deviceId, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE));
            ContentValues[] contentValuesArr = new ContentValues[data.size()];
            int ind = 0;
            for (String key : data.keySet()) {
                contentValuesArr[ind] = new ContentValues();
                contentValuesArr[ind].put(NSDSContractExt.NsdsConfigColumns.PNAME, key);
                contentValuesArr[ind].put(NSDSContractExt.NsdsConfigColumns.PVALUE, data.get(key));
                contentValuesArr[ind].put("imsi", prevImsi);
                ind++;
            }
            Uri.Builder builder = NSDSContractExt.SimSwapNsdsConfigs.CONTENT_URI.buildUpon();
            builder.appendQueryParameter("imsi", prevImsi);
            int noRows = this.mResolver.bulkInsert(builder.build(), contentValuesArr);
            String str = LOG_TAG;
            IMSLog.i(str, "copied shared pref and nsds config entries for sim swap:" + noRows);
        } finally {
            deleteConfigAndResetDeviceAndAccountStatus(deviceId, prevImsi, slotId);
        }
    }

    public void deleteConfigAndResetDeviceAndAccountStatus(String deviceId, String imsi, int slotId) {
        String str = LOG_TAG;
        IMSLog.s(str, "deleteConfigAndResetDeviceAndAccountStatus: imsi " + imsi);
        resetAccountStatus(deviceId);
        resetDeviceStatus(deviceId, imsi, slotId);
        deleteNsdsConfigs(imsi);
        NSDSConfigHelper.clear();
    }

    public void resetAccountStatus(String deviceUid) {
        IMSLog.i(LOG_TAG, "resetAccountStatus()");
        setLocalDevicePrimary(deviceUid, false);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, "access_token");
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ACCESS_TOKEN_EXPIRY);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ACCESS_TOKEN_TYPE);
    }

    public void resetDeviceStatus(String deviceUid, String imsi, int slotId) {
        NSDSSharedPrefHelper.save(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_DEVICE_STATE, NSDSNamespaces.NSDSDeviceState.DEACTIVATED);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_ENTITLEMENT_STATE);
        NSDSSharedPrefHelper.removePrefForSlot(this.mContext, slotId, NSDSNamespaces.NSDSSharedPref.PREF_IMSI_EAP);
        NSDSSharedPrefHelper.removeAkaToken(this.mContext, imsi);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        NSDSSharedPrefHelper.remove(this.mContext, deviceUid, NSDSNamespaces.NSDSSharedPref.PREF_SENT_TOKEN_TO_SERVER);
        NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, deviceUid);
        resetE911AidInfoForNativeLine(deviceUid);
    }

    public boolean isE911InfoAvailForNativeLine(String localDeviceUid) {
        LineDetail detail = getNativeLineDetail(localDeviceUid, false);
        if (detail == null) {
            IMSLog.e(LOG_TAG, "isE911InfoAvailForNativeLine: line info missing");
            return false;
        } else if (detail.e911AddressId == null) {
            IMSLog.e(LOG_TAG, "isE911InfoAvailForNativeLine: e911 aid missing");
            return false;
        } else if (detail.locationStatus == 1) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, "isE911InfoAvailForNativeLine: loc status false");
            return false;
        }
    }

    public void resetE911AidInfoForNativeLine(String deviceUid) {
        ContentValues values = new ContentValues();
        values.putNull(NSDSContractExt.LineColumns.E911_ADDRESS_ID);
        values.putNull("e911_aid_expiration");
        values.putNull(NSDSContractExt.LineColumns.E911_SERVER_DATA);
        values.putNull(NSDSContractExt.LineColumns.E911_SERVER_URL);
        int noUpdates = this.mResolver.update(NSDSContractExt.Lines.CONTENT_URI, values, "_id = ?", new String[]{String.valueOf(getNativeLineId(deviceUid))});
        if (noUpdates > 0) {
            String str = LOG_TAG;
            IMSLog.i(str, "resetE911AidInfoForNativeLine: success " + noUpdates);
        }
    }

    public void updateRegistationStatusForLines(List<String> msisdns, int status, int fromRegStatus, int toRegStatus) {
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.LineColumns.REG_STATUS, Integer.valueOf(toRegStatus));
        for (String msisdn : msisdns) {
            this.mResolver.update(NSDSContractExt.Lines.CONTENT_URI, values, "msisdn = ? AND status = ? AND reg_status = ?", new String[]{msisdn, String.valueOf(status), String.valueOf(fromRegStatus)});
        }
    }

    public void updateRegistationStatusForLines(int fromStatus, int toStatus) {
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.LineColumns.REG_STATUS, Integer.valueOf(toStatus));
        if (this.mResolver.update(NSDSContractExt.Lines.CONTENT_URI, values, "reg_status = ?", new String[]{String.valueOf(fromStatus)}) > 0) {
            String str = LOG_TAG;
            IMSLog.i(str, "updateStatusForLines fromStatus:" + fromStatus + " toStatus:" + toStatus);
            return;
        }
        IMSLog.e(LOG_TAG, "Updating lines failed");
    }

    public void updateLocationAndTcStatus(long lineId, ResponseManageLocationAndTC responseLocation, String deviceUid, int slotid) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateLocationAndTcStatus: lineId " + lineId);
        ContentValues values = new ContentValues();
        if (responseLocation != null) {
            int locationStatus = 0;
            if (responseLocation.locationStatus == null) {
                locationStatus = -1;
            } else if (responseLocation.locationStatus.booleanValue()) {
                locationStatus = 1;
            }
            int tcStatus = 0;
            if (responseLocation.tcStatus == null) {
                tcStatus = -1;
            } else if (responseLocation.tcStatus.booleanValue()) {
                tcStatus = 1;
            }
            values.put(NSDSContractExt.LineColumns.LOCATION_STATUS, Integer.valueOf(locationStatus));
            values.put("tc_status", Integer.valueOf(tcStatus));
            values.put(NSDSContractExt.LineColumns.E911_ADDRESS_ID, responseLocation.addressId);
            values.put("e911_aid_expiration", responseLocation.aidExpiration);
            values.put(NSDSContractExt.LineColumns.E911_SERVER_DATA, responseLocation.serverData);
            values.put(NSDSContractExt.LineColumns.E911_SERVER_URL, responseLocation.serverUrl);
            if (this.mResolver.update(NSDSContractExt.Lines.CONTENT_URI, values, "_id = ?", new String[]{String.valueOf(lineId)}) > 0) {
                IMSLog.i(LOG_TAG, "updateLocationAndTcStatus: success");
            }
            LineDetail lineDetail = getLineDetail(lineId, deviceUid, false);
            if (lineDetail == null) {
                IMSLog.e(LOG_TAG, "updateLocationAndTcStatus Line detail is NULL");
                return;
            }
            String str2 = LOG_TAG;
            IMSLog.i(str2, "updateLocationAndTcStatus location status: " + lineDetail.locationStatus + ", tc status: " + lineDetail.tcStatus + ", e911 AID Expirsation: " + lineDetail.e911AidExpiration);
            String str3 = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append(", e911 AID: ");
            sb.append(lineDetail.e911AddressId);
            IMSLog.s(str3, sb.toString());
            broadcastE911AID(responseLocation, slotid);
        }
    }

    private void broadcastE911AID(ResponseManageLocationAndTC responseLocation, int slotid) {
        if (responseLocation.addressId == null || responseLocation.aidExpiration == null) {
            IMSLog.e(LOG_TAG, "broadcastE911AID: invalid e911 AID info, vail");
            return;
        }
        Intent intent = new Intent(NSDSNamespaces.NSDSActions.E911_AID_INFO_RECEIVED);
        intent.putExtra(NSDSNamespaces.NSDSExtras.E911_AID, responseLocation.addressId);
        intent.putExtra("e911_aid_expiration", responseLocation.aidExpiration);
        intent.putExtra(NSDSNamespaces.NSDSExtras.SIM_SLOT_IDX, slotid);
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public long getLineIdOnDevice(String msisdn, String deviceUid, int status) {
        String selection = "msisdn = ? AND status = ?";
        String[] selectionArgs = {msisdn, String.valueOf(status)};
        if (status == -1) {
            selection = "msisdn = ?";
            selectionArgs = new String[]{msisdn};
        }
        Uri.Builder builder = NSDSContractExt.Lines.CONTENT_URI.buildUpon();
        builder.appendQueryParameter("device_uid", deviceUid);
        long lineId = -1;
        Cursor cursor = this.mResolver.query(builder.build(), new String[]{"_id"}, selection, selectionArgs, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    lineId = cursor.getLong(0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getLineIdOnDevice(): lineId: " + lineId);
        return lineId;
        throw th;
    }

    private long insertLineWithServiceDetail(long accountId, long deviceId, String msisdn, String fiendlyName, Boolean isOwner, String serviceName, String serviceFingerprint) {
        String str = msisdn;
        ContentValues values = new ContentValues();
        values.put("account_id", Long.valueOf(accountId));
        values.put("msisdn", str);
        values.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, fiendlyName);
        values.put("is_owner", isOwner);
        values.put("status", 0);
        Uri lineUri = this.mResolver.insert(NSDSContractExt.Lines.CONTENT_URI, values);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "inserted lineUri:" + lineUri);
        if (lineUri == null || lineUri.getPathSegments() == null) {
            String str3 = LOG_TAG;
            IMSLog.s(str3, "insertLineWithServiceDetail: failed for msisdn:" + str);
            return -1;
        }
        long lineId = Long.valueOf(lineUri.getPathSegments().get(1)).longValue();
        Uri serviceUri = insertServiceNameAndFingerPrint(lineId, deviceId, serviceName, serviceFingerprint, (String) null, msisdn);
        String str4 = LOG_TAG;
        IMSLog.s(str4, "insertLineWithServiceDetail: inserted line service Uri:" + serviceUri);
        return lineId;
    }

    private Uri insertServiceNameAndFingerPrint(long lineId, long deviceId, String serviceName, String serviceFingerprint, String serviceInstanceId, String serviceMsisdn) {
        ContentValues values = new ContentValues();
        values.put("service_name", serviceName);
        values.put(NSDSContractExt.ServiceColumns.SERVICE_MSISDN, serviceMsisdn);
        values.put(NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT, serviceFingerprint);
        values.put(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID, serviceInstanceId);
        return this.mResolver.insert(NSDSContractExt.Lines.buildServicesUri(deviceId, lineId), values);
    }

    public long insertOrUpdateNativeLine(long accountId, String deviceUId, ResponseGetMSISDN reponseGetMsisdn) {
        ResponseGetMSISDN responseGetMSISDN = reponseGetMsisdn;
        long deviceRowId = insertDeviceIfNotExists(accountId, deviceUId, false, true);
        long lineId = getLineIdFromAllLinesIf(responseGetMSISDN.msisdn);
        if (lineId == -1) {
            IMSLog.s(LOG_TAG, "native msisdn does not exist in db, creating one");
            String str = responseGetMSISDN.msisdn;
            String str2 = responseGetMSISDN.msisdn;
            String str3 = responseGetMSISDN.serviceFingerprint;
            long j = lineId;
            String str4 = " and lineId:";
            String str5 = "insertOrUpdateNativeLine: Updated service.is_native successfully for device:";
            long lineId2 = insertLineWithServiceDetail(accountId, deviceRowId, str, str2, 1, "vowifi", str3);
            ContentValues values = new ContentValues();
            values.put("is_native", 1);
            if (this.mResolver.update(NSDSContractExt.Lines.buildServicesUri(deviceRowId, lineId2), values, (String) null, (String[]) null) > 0) {
                String str6 = LOG_TAG;
                IMSLog.s(str6, str5 + deviceRowId + str4 + lineId2);
            }
            ResponseGetMSISDN responseGetMSISDN2 = reponseGetMsisdn;
            return lineId2;
        }
        String str7 = " and lineId:";
        IMSLog.s(LOG_TAG, "native msisdn does exist in db, add service fingerprint and is_native attribute");
        ContentValues values2 = new ContentValues();
        values2.put("is_native", 1);
        values2.put("service_name", "vowifi");
        ResponseGetMSISDN responseGetMSISDN3 = reponseGetMsisdn;
        String str8 = "insertOrUpdateNativeLine: Updated service.is_native successfully for device:";
        values2.put(NSDSContractExt.ServiceColumns.SERVICE_MSISDN, responseGetMSISDN3.msisdn);
        values2.put(NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT, responseGetMSISDN3.serviceFingerprint);
        long lineId3 = lineId;
        if (!doesServiceExists(deviceRowId, lineId3)) {
            if (this.mResolver.insert(NSDSContractExt.Lines.buildServicesUri(deviceRowId, lineId3), values2) != null) {
                String str9 = LOG_TAG;
                IMSLog.s(str9, "insertOrUpdateNativeLine: created service entry for:" + deviceRowId + str7 + lineId3);
            }
        } else if (this.mResolver.update(NSDSContractExt.Lines.buildServicesUri(deviceRowId, lineId3), values2, (String) null, (String[]) null) > 0) {
            String str10 = LOG_TAG;
            IMSLog.s(str10, str8 + deviceRowId + str7 + lineId3);
        }
        return lineId3;
    }

    private long insertDeviceIfNotExists(long accountId, String deviceUId, boolean isPrimary, boolean isLocal) {
        String deviceName;
        String str = deviceUId;
        long deviceRowId = (long) getDeviceId(str);
        if (deviceRowId != -1) {
            return deviceRowId;
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "device does not exist with :" + str + " creating one");
        String deviceName2 = DeviceNameHelper.getDeviceName(this.mContext);
        if (TextUtils.isEmpty(deviceName2)) {
            deviceName = deviceUId;
        } else {
            deviceName = deviceName2;
        }
        return insertDevice(accountId, deviceUId, deviceName, isPrimary, 0, isLocal);
    }

    private long getLineIdFromAllLinesIf(String msisdn) {
        long lineId = -1;
        ContentResolver contentResolver = this.mResolver;
        Uri buildAllLinesInternalUri = NSDSContractExt.Lines.buildAllLinesInternalUri();
        Cursor cursor = contentResolver.query(buildAllLinesInternalUri, new String[]{"_id"}, "msisdn = ? AND account_id = 0", new String[]{msisdn}, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    lineId = cursor.getLong(0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return lineId;
        throw th;
    }

    private long insertDevice(long accountId, String deviceUId, String deviceName, boolean isPrimary, int deviceType, boolean isLocal) {
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.DeviceColumns.ACCOUNT_ID, Long.valueOf(accountId));
        values.put("device_uid", deviceUId);
        values.put("device_name", deviceName);
        values.put("is_primary", Boolean.valueOf(isPrimary));
        values.put("device_type", Integer.valueOf(deviceType));
        values.put(NSDSContractExt.DeviceColumns.DEVICE_IS_LOCAL, Integer.valueOf(isLocal));
        Uri deviceUri = this.mResolver.insert(NSDSContractExt.Devices.CONTENT_URI, values);
        String str = LOG_TAG;
        IMSLog.s(str, "inserted deviceUri:" + deviceUri);
        if (deviceUri != null) {
            return Long.valueOf(deviceUri.getPathSegments().get(1)).longValue();
        }
        return -1;
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public String getNativeMsisdn(String deviceUid) {
        Cursor cursor;
        String nativeMsisdn = null;
        try {
            cursor = this.mResolver.query(NSDSContractExt.Lines.buildLinesUri(deviceUid), new String[]{"msisdn"}, "is_native = ?", new String[]{"1"}, (String) null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    nativeMsisdn = cursor.getString(0);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "getNativeMsisdn failed with:" + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return nativeMsisdn;
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public int getNativeLineId(String deviceUid) {
        Cursor cursor;
        int nativeLineId = -1;
        try {
            cursor = this.mResolver.query(NSDSContractExt.Lines.buildLinesUri(deviceUid), new String[]{"_id"}, "is_native = ?", new String[]{"1"}, (String) null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    nativeLineId = cursor.getInt(0);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.i(str, "getNativeLineId failed with:" + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return nativeLineId;
        throw th;
    }

    public LineDetail getNativeLineDetail(String localDeviceUid, boolean includeServiceDetails) {
        long nativeLineId = (long) getNativeLineId(localDeviceUid);
        if (nativeLineId != -1) {
            return getLineDetail(nativeLineId, localDeviceUid, includeServiceDetails);
        }
        IMSLog.e(LOG_TAG, "getNativeLineDetail: native line id not found");
        return null;
    }

    public LineDetail getLineDetail(long lineId, String localDeviceUid, boolean includeServiceDetails) {
        ServiceInstanceDetail serviceInstanceDetail;
        if (lineId <= 0) {
            IMSLog.e(LOG_TAG, "getLineDetail: lineId is zero/negative");
            return null;
        }
        LineDetail lineDetail = getLineDetail("lines._id = ?", new String[]{String.valueOf(lineId)}, localDeviceUid);
        if (includeServiceDetails && (serviceInstanceDetail = getServiceInstanceForLocalDevice(lineId, localDeviceUid)) != null) {
            lineDetail.serviceFingerPrint = serviceInstanceDetail.serviceFingerPrint;
            lineDetail.serviceInstanceId = serviceInstanceDetail.serviceInstanceId;
            lineDetail.serviceTokenExpiryTime = serviceInstanceDetail.serviceTokenExpiryTime;
        }
        return lineDetail;
    }

    public String getNativeLineE911AidExp(String localDeviceUid) {
        LineDetail nativeLineDetail = getNativeLineDetail(localDeviceUid, false);
        if (nativeLineDetail != null) {
            return nativeLineDetail.e911AidExpiration;
        }
        return null;
    }

    private LineDetail getLineDetail(String selection, String[] selectionArgs, String deviceUid) {
        Throwable th;
        LineDetail lineDetail = new LineDetail();
        Cursor cursor = this.mResolver.query(NSDSContractExt.Lines.buildLinesUri(deviceUid), new String[]{"_id", "msisdn", NSDSContractExt.LineColumns.LOCATION_STATUS, "tc_status", NSDSContractExt.LineColumns.E911_ADDRESS_ID, "e911_aid_expiration"}, selection, selectionArgs, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    lineDetail.lineId = (long) cursor.getInt(0);
                    lineDetail.msisdn = cursor.getString(1);
                    lineDetail.locationStatus = cursor.getInt(2);
                    lineDetail.tcStatus = cursor.getInt(3);
                    lineDetail.e911AddressId = cursor.getString(4);
                    lineDetail.e911AidExpiration = cursor.getString(5);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return lineDetail;
        throw th;
    }

    public ServiceInstanceDetail getServiceInstanceForLocalDevice(long lineId, String deviceUid) {
        Cursor cursor = this.mResolver.query(NSDSContractExt.Lines.buildServicesUri((long) getDeviceId(deviceUid), lineId), new String[]{"service_name", NSDSContractExt.ServiceColumns.SERVICE_FINGERPRINT, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_ID, NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN, NSDSContractExt.ServiceColumns.SERVICE_TOKEN_EXPIRE_TIME}, (String) null, (String[]) null, (String) null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    ServiceInstanceDetail serviceInstanceDetail = new ServiceInstanceDetail();
                    serviceInstanceDetail.serviceName = cursor.getString(0);
                    serviceInstanceDetail.serviceFingerPrint = cursor.getString(1);
                    serviceInstanceDetail.serviceInstanceId = cursor.getString(2);
                    serviceInstanceDetail.serviceInstanceToken = cursor.getString(3);
                    serviceInstanceDetail.serviceTokenExpiryTime = cursor.getString(4);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return serviceInstanceDetail;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
        throw th;
    }

    private boolean doesServiceExists(long deviceId, long lineId) {
        Cursor cursor = this.mResolver.query(NSDSContractExt.Lines.buildServicesUri(deviceId, lineId), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
        try {
            boolean moveToFirst = cursor.moveToFirst();
            if (cursor != null) {
                cursor.close();
            }
            return moveToFirst;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void setLocalDevicePrimary(String deviceUid, boolean isPrimary) {
        String str = LOG_TAG;
        IMSLog.s(str, "setLocalDevicePrimary: isPrimary " + isPrimary);
        ContentValues values = new ContentValues();
        values.put("is_primary", Integer.valueOf(isPrimary));
        if (this.mResolver.update(NSDSContractExt.Devices.CONTENT_URI, values, "is_local = ? AND device_uid = ?", new String[]{"1", deviceUid}) > 0) {
            IMSLog.s(LOG_TAG, "setLocalDevicePrimary: update success");
        }
    }

    public void updateDeviceName(String deviceUId, String deviceName) {
        String str = LOG_TAG;
        IMSLog.s(str, "Updating device name for deviceUID: " + deviceUId);
        updateDeviceName((long) getDeviceId(deviceUId), deviceName);
    }

    public void updateDeviceName(long deviceRowId, String deviceName) {
        ContentValues values = new ContentValues();
        values.put("device_name", deviceName);
        if (this.mResolver.update(NSDSContractExt.Devices.CONTENT_URI, values, "_id = ?", new String[]{String.valueOf(deviceRowId)}) > 0) {
            String str = LOG_TAG;
            IMSLog.s(str, "Updated device name successsfully to: " + deviceName);
        }
    }

    public void updateLineName(long lineId, String friendlyLineName) {
        ContentValues values = new ContentValues();
        values.put(NSDSContractExt.LineColumns.FRIENDLY_NAME, friendlyLineName);
        if (this.mResolver.update(NSDSContractExt.Lines.CONTENT_URI, values, "_id = ?", new String[]{String.valueOf(lineId)}) > 0) {
            String str = LOG_TAG;
            IMSLog.s(str, "UpdateLineName Successful. Line name: " + friendlyLineName);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public int getDeviceId(String deviceUId) {
        Cursor cursor;
        String str = LOG_TAG;
        IMSLog.s(str, "getDeviceId: for deviceId :" + deviceUId);
        try {
            ContentResolver contentResolver = this.mResolver;
            Uri uri = NSDSContractExt.Devices.CONTENT_URI;
            cursor = contentResolver.query(uri, new String[]{"_id"}, "device_uid = ?", new String[]{deviceUId}, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int rowId = cursor.getInt(0);
                    String str2 = LOG_TAG;
                    IMSLog.i(str2, "getDeviceId: returned :" + rowId);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return rowId;
                }
            }
            String str3 = LOG_TAG;
            IMSLog.s(str3, "getDeviceId: Could not find deviceUID :" + deviceUId);
            if (cursor == null) {
                return -1;
            }
            cursor.close();
            return -1;
        } catch (SQLiteException sqe) {
            String str4 = LOG_TAG;
            IMSLog.s(str4, "SQL exception while getDeviceId " + sqe.getMessage());
            return -1;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    public Map<String, Long> getActiveMsisdns(String deviceUid) {
        Cursor cursor;
        Map<String, Long> msisdnsIdMap = new HashMap<>();
        try {
            cursor = this.mResolver.query(NSDSContractExt.Lines.buildActiveLinesWithServicveUri(deviceUid), new String[]{"msisdn", "_id"}, "service_instance_id IS NOT NULL", (String[]) null, (String) null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    msisdnsIdMap.put(cursor.getString(0), Long.valueOf(cursor.getLong(1)));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "getActiveLines failed with:" + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return msisdnsIdMap;
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public List<String> getReadyForUseMsisdns(String deviceUid) {
        Cursor cursor;
        List<String> msisdns = new ArrayList<>();
        try {
            String[] selectionArgs = {String.valueOf(2)};
            cursor = this.mResolver.query(NSDSContractExt.Lines.buildLinesUri(deviceUid), new String[]{"msisdn"}, "reg_status = ?", selectionArgs, (String) null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    msisdns.add(cursor.getString(0));
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException sqe) {
            String str = LOG_TAG;
            IMSLog.s(str, "getActiveLines failed with:" + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return msisdns;
        throw th;
    }

    public static boolean migrationToCe(Context context, String db) {
        if (!context.createCredentialProtectedStorageContext().moveDatabaseFrom(context, db)) {
            IMSLog.e(LOG_TAG, "Failed to maigrate DB.");
            return false;
        } else if (!context.deleteDatabase(db)) {
            IMSLog.e(LOG_TAG, "Failed delete DB on DE.");
            return false;
        } else {
            IMSLog.i(LOG_TAG, "migration is done");
            return true;
        }
    }
}
