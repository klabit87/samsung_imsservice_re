package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.cmstore.helper.TelephonyDbHelper;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import java.text.MessageFormat;
import java.util.Arrays;

public class CloudMessageTelephonyStorageAdapter {
    public static final String LOG_TAG = CloudMessageTelephonyStorageAdapter.class.getSimpleName();
    private final Context mContext;
    private final TelephonyDbHelper mTeleDBHelper = new TelephonyDbHelper(this.mContext);

    public CloudMessageTelephonyStorageAdapter(Context context) {
        this.mContext = context;
    }

    public Cursor getTelephonyAddr(long id) {
        return this.mTeleDBHelper.query(Uri.parse(MessageFormat.format("content://mms/{0}/addr", new Object[]{String.valueOf(id)})), (String[]) null, "msg_id=" + id, (String[]) null, (String) null);
    }

    public Cursor getTelephonyPart(long id) {
        return this.mTeleDBHelper.query(Uri.parse("content://mms/part"), (String[]) null, "mid=" + id, (String[]) null, (String) null);
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long mmsId) {
        Uri uri = ITelephonyDBColumns.CONTENT_MMS;
        return this.mTeleDBHelper.query(uri, (String[]) null, "_id = " + mmsId, (String[]) null, (String) null);
    }

    public Cursor querySMSfromTelephony(String[] projection, String whereClaus, String[] selectionArgs, String sortOrder) {
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_SMS, projection, whereClaus, selectionArgs, sortOrder);
    }

    public Cursor querySMSUseRowId(long rowId) {
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_SMS, (String[]) null, "_id=?", new String[]{Long.toString(rowId)}, (String) null);
    }

    public Cursor queryFAXUseRowId(long rowId) {
        String str = LOG_TAG;
        Log.d(str, "queryFAXUseRowId: " + rowId);
        return this.mTeleDBHelper.query(ContentUris.withAppendedId(ITelephonyDBColumns.CONTENT_FAX, rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryMMSPduFromTelephonyDb(String[] selection, String whereClaus, String[] selectionArgs, String sortOrder) {
        String str = LOG_TAG;
        Log.d(str, "queryMMSPduFromTelephonyDb,  whereClaus: " + whereClaus + " selectionArgs: " + Arrays.toString(selectionArgs));
        return this.mTeleDBHelper.query(ITelephonyDBColumns.CONTENT_MMS, selection, whereClaus, selectionArgs, sortOrder);
    }
}
