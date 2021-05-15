package com.sec.internal.ims.servicemodules.ss;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.NetworkUtils;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.log.IMSLog;

public class ApnSettings {
    private static final String[] APN_PROJECTION = {"type", "mmsc", "mmsproxy", "mmsport", "name", NSDSContractExt.ProvisioningParametersColumns.APN, "bearer_bitmask", CloudMessageProviderContract.BufferDBSMS.PROTOCOL, "roaming_protocol", "authtype", "mvno_type", "mvno_match_data", "proxy", "port", "server", "user", CloudMessageProviderContract.VVMAccountInfoColumns.PASSWORD};
    private static final int COLUMN_PORT = 13;
    private static final int COLUMN_PROXY = 12;
    private static final int COLUMN_TYPE = 0;
    private static final String LOG_TAG = "ApnSettings";
    private final String mDebugText;
    private final String mProxyAddress;
    private final int mProxyPort;

    public static ApnSettings load(Context context, String apnName, String apnType, int subId) {
        String portString;
        IMSLog.i(LOG_TAG, "Loading APN using name " + apnName);
        String selection = null;
        String[] selectionArgs = null;
        String apnName2 = trimWithNullCheck(apnName);
        if (!TextUtils.isEmpty(apnName2)) {
            selection = "apn=?";
            selectionArgs = new String[]{apnName2};
        }
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Telephony.Carriers.CONTENT_URI;
        Cursor cursor = SqliteWrapper.query(context, contentResolver, Uri.withAppendedPath(uri, "/subId/" + subId), APN_PROJECTION, selection, selectionArgs, (String) null);
        if (cursor != null) {
            int proxyPort = 80;
            do {
                try {
                    if (cursor.moveToNext()) {
                    }
                } catch (NumberFormatException e) {
                    IMSLog.e(LOG_TAG, "Invalid port " + portString + ", use 80");
                } catch (Throwable th) {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } while (!isValidApnType(cursor.getString(0), apnType));
            String proxyAddress = NetworkUtils.trimV4AddrZeros(trimWithNullCheck(cursor.getString(12)));
            portString = trimWithNullCheck(cursor.getString(13));
            proxyPort = Integer.parseInt(portString);
            ApnSettings apnSettings = new ApnSettings(proxyAddress, proxyPort, getDebugText(cursor));
            if (cursor != null) {
                cursor.close();
            }
            return apnSettings;
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
    }

    private static String getDebugText(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        sb.append("APN [");
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            String name = cursor.getColumnName(i);
            String value = cursor.getString(i);
            if (!TextUtils.isEmpty(value)) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(name);
                sb.append('=');
                sb.append(value);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String trimWithNullCheck(String value) {
        if (value != null) {
            return value.trim();
        }
        return null;
    }

    public ApnSettings(String proxyAddr, int proxyPort, String debugText) {
        this.mProxyAddress = proxyAddr;
        this.mProxyPort = proxyPort;
        this.mDebugText = debugText;
    }

    public String getProxyAddress() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    private static boolean isValidApnType(String types, String apnType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }
        for (String type : types.split(",")) {
            String type2 = type.trim();
            if (type2.equals(apnType) || type2.equals("*")) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.mDebugText;
    }
}
