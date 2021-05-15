package com.sec.internal.ims.cmstore.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TelephonyDbHelper {
    public static final String TAG = TelephonyDbHelper.class.getSimpleName();
    private ContentResolver mResolver = null;

    public TelephonyDbHelper(Context context) {
        this.mResolver = context.getContentResolver();
    }

    public Cursor query(Uri uri, String[] projection, String whereClaus, String[] selectionArgs, String sortOrder) {
        String str = TAG;
        Log.i(str, "query uri=" + IMSLog.checker(uri.toString()) + " whereClaus: " + whereClaus + " sortOrder: " + sortOrder);
        try {
            if (ITelephonyDBColumns.CONTENT_SMS.equals(uri)) {
                return this.mResolver.query(uri, projection, whereClaus, selectionArgs, sortOrder);
            }
            if (ITelephonyDBColumns.SPAM_SMS_CONTENT_URI.equals(uri)) {
                return this.mResolver.query(ITelephonyDBColumns.SPAM_MMSSMS_CONTENT_URI, (String[]) null, makeWhereForSpam(whereClaus, "sms"), selectionArgs, sortOrder);
            } else if (ITelephonyDBColumns.CONTENT_MMS.equals(uri)) {
                return this.mResolver.query(uri, projection, whereClaus, (String[]) null, (String) null);
            } else {
                if (!ITelephonyDBColumns.SPAM_MMS_CONTENT_URI.equals(uri)) {
                    return this.mResolver.query(uri, projection, whereClaus, selectionArgs, sortOrder);
                }
                return this.mResolver.query(ITelephonyDBColumns.SPAM_MMSSMS_CONTENT_URI, projection, makeWhereForSpam(whereClaus, "mms"), selectionArgs, sortOrder);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when query: ", e);
            return null;
        }
    }

    private String makeWhereForSpam(String whereClaus, String type) {
        if (whereClaus == null || whereClaus.length() == 0) {
            return "transport_type='" + type + "'";
        }
        return whereClaus + " and (" + ITelephonyDBColumns.TYPE_DISCRIMINATOR_COLUMN + "= '" + type + "')";
    }

    public InputStream getInputStream(Uri uri) throws FileNotFoundException {
        try {
            return this.mResolver.openInputStream(uri);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when getinput stream: ", e);
            return null;
        }
    }
}
