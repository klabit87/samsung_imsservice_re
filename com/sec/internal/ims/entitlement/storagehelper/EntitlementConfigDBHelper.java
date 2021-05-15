package com.sec.internal.ims.entitlement.storagehelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.internal.constants.ims.entitilement.EntitlementConfigContract;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.log.IMSLog;

public class EntitlementConfigDBHelper extends NSDSDatabaseHelper {
    private static final String LOG_TAG = EntitlementConfigDBHelper.class.getSimpleName();

    public EntitlementConfigDBHelper(Context context) {
        super(context);
    }

    public boolean isDeviceConfigAvailable(String imsi) {
        if (getDeviceConfig(imsi) != null) {
            return true;
        }
        IMSLog.i(LOG_TAG, "isDeviceConfigAvailable: no config");
        return false;
    }

    public String getDeviceConfig(String imsi) {
        ContentResolver contentResolver = this.mResolver;
        Uri uri = EntitlementConfigContract.DeviceConfig.CONTENT_URI;
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

    public void insertDeviceConfig(ResponseManageConnectivity resManageConn, String version, String imsi) {
        insertDeviceConfig(this.mContext, resManageConn.deviceConfig, version, imsi);
    }

    public static void insertDeviceConfig(Context context, String xmlDeviceConfig, String version, String imsi) {
        Context ceContext = context.createCredentialProtectedStorageContext();
        ContentValues values = null;
        if (!TextUtils.isEmpty(xmlDeviceConfig)) {
            values = new ContentValues();
            if (version != null) {
                values.put("version", version);
            }
            values.put("imsi", imsi);
            values.put("device_config", xmlDeviceConfig);
        }
        if (values != null && values.size() != 0 && ceContext.getContentResolver().insert(EntitlementConfigContract.DeviceConfig.CONTENT_URI, values) != null) {
            IMSLog.i(LOG_TAG, "inserted device config in device config successfully");
        }
    }

    public void updateDeviceConfig(ResponseManageConnectivity resManageConn, String version, String imsi) {
        updateDeviceConfig(this.mContext, resManageConn.deviceConfig, version, imsi);
    }

    public static void updateDeviceConfig(Context context, String xmlDeviceConfig, String version, String imsi) {
        String str = LOG_TAG;
        IMSLog.i(str, "updateDeviceConfig: version:" + version);
        if (!TextUtils.isEmpty(xmlDeviceConfig)) {
            Context ceContext = context.createCredentialProtectedStorageContext();
            ContentValues values = new ContentValues();
            if (version != null) {
                values.put("version", version);
            }
            values.put("device_config", xmlDeviceConfig);
            if (ceContext.getContentResolver().update(EntitlementConfigContract.DeviceConfig.CONTENT_URI, values, "imsi = ?", new String[]{imsi}) > 0) {
                String str2 = LOG_TAG;
                IMSLog.i(str2, "updated device config in device config successfully with version:" + version);
            }
        }
    }

    public static void deleteConfig(Context context, String imsi) {
        if (context.createCredentialProtectedStorageContext().getContentResolver().delete(EntitlementConfigContract.DeviceConfig.CONTENT_URI, "imsi = ?", new String[]{imsi}) > 0) {
            String str = LOG_TAG;
            IMSLog.s(str, "Deleted device config: successfully for imsi:" + imsi);
        }
    }

    public static String getNsdsUrlFromDeviceConfig(Context context, String defaultUrl) {
        Cursor cursor;
        String entitlementUrl = null;
        try {
            cursor = context.createCredentialProtectedStorageContext().getContentResolver().query(EntitlementConfigContract.DeviceConfig.buildXPathExprUri("//janskyConfig/entitlement_server_FQDN"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    entitlementUrl = cursor.getString(1);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLException sqe) {
            IMSLog.s(LOG_TAG, "getNsdsUrlFromDeviceConfig: " + sqe.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        if (entitlementUrl == null) {
            return defaultUrl;
        }
        if (!entitlementUrl.endsWith("generic_devices")) {
            entitlementUrl = entitlementUrl + "/generic_devices";
        }
        IMSLog.i(LOG_TAG, "getNsdsUrlFromDeviceConfig: " + entitlementUrl);
        return entitlementUrl;
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
