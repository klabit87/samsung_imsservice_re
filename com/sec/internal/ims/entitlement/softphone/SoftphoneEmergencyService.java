package com.sec.internal.ims.entitlement.softphone;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import java.util.List;

public class SoftphoneEmergencyService {
    private static final int E911AID_REVERSE_INDEX = 2;
    private static final String LOG_TAG = SoftphoneEmergencyService.class.getSimpleName();
    private final Context mContext;

    public SoftphoneEmergencyService(Context context) {
        this.mContext = context;
    }

    private static ContentValues getContentValues(String[] fields, String accountId) {
        String field;
        ContentValues values = new ContentValues();
        values.put("account_id", accountId);
        values.put("name", fields[0]);
        values.put(SoftphoneContract.AddressColumns.HOUSE_NUMBER, fields[1]);
        values.put(SoftphoneContract.AddressColumns.HOUSE_NUMBER_EXTENSION, fields[2]);
        values.put(SoftphoneContract.AddressColumns.STREET_DIRECTION_PREFIX, fields[3]);
        values.put(SoftphoneContract.AddressColumns.STREET_NAME, fields[4]);
        values.put(SoftphoneContract.AddressColumns.STREET_NAME_SUFFIX, fields[5]);
        values.put(SoftphoneContract.AddressColumns.STREET_DIRECTION_SUFFIX, fields[6]);
        values.put(SoftphoneContract.AddressColumns.CITY, fields[7]);
        values.put("state", fields[8]);
        values.put(SoftphoneContract.AddressColumns.ZIP, fields[9]);
        values.put(SoftphoneContract.AddressColumns.ADDITIONAL_ADDRESS_INFO, fields[10]);
        values.put(SoftphoneContract.AddressColumns.E911AID, fields[11]);
        values.put(SoftphoneContract.AddressColumns.EXPIRE_DATE, fields[12]);
        StringBuilder formattedAddressBuilder = new StringBuilder();
        for (int i = 1; i < 11; i++) {
            if (fields[i] == null || fields[i].isEmpty() || fields[i].equalsIgnoreCase("null")) {
                field = "";
            } else {
                field = fields[i];
            }
            formattedAddressBuilder.append(field);
            formattedAddressBuilder.append(";");
        }
        values.put(SoftphoneContract.AddressColumns.FORMATTED_ADDRESS, formattedAddressBuilder.toString());
        return values;
    }

    public void compareAndSaveE911Address(List<String> locations, String accountId) {
        String str = LOG_TAG;
        Log.i(str, "networkLocations size: " + locations.size());
        Uri uri = SoftphoneContract.SoftphoneAddress.buildAddressUri(accountId);
        for (String location : locations) {
            String[] locationFields = location.split(";");
            if (locationFields.length >= 13) {
                String e911AID = locationFields[locationFields.length - 2];
                String str2 = LOG_TAG;
                Log.i(str2, "networkLocation: " + locationFields[0] + " " + e911AID);
                String[] selectionArgs = {e911AID};
                ContentValues values = getContentValues(locationFields, accountId);
                if (this.mContext.getContentResolver().update(uri, values, "E911AID=?", selectionArgs) == 0) {
                    this.mContext.getContentResolver().insert(uri, values);
                }
            }
        }
    }
}
