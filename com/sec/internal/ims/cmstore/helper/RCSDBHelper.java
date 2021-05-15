package com.sec.internal.ims.cmstore.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;

public class RCSDBHelper {
    public static final String TAG = RCSDBHelper.class.getSimpleName();
    private ContentResolver mResolver = null;

    public RCSDBHelper(Context context) {
        this.mResolver = context.getContentResolver();
    }

    public Uri insert(Uri uri, ContentValues value) {
        try {
            return this.mResolver.insert(uri, value);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when insert: ", e);
            return null;
        }
    }

    public int insertSingleSessionPartsToDB(Uri uri, ContentValues[] values) {
        if (values == null || values.length < 1) {
            return 0;
        }
        try {
            return this.mResolver.bulkInsert(uri, values);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when insert: ", e);
            return 0;
        }
    }

    public Cursor query(Uri uri, String[] projection, String whereClaus, String[] selectionArgs, String sortOrder) {
        String str = TAG;
        Log.d(str, "query uri=" + IMSLog.checker(uri.toString()) + " whereClaus: " + whereClaus + " selectionArgs: " + Arrays.toString(selectionArgs) + " sortOrder: " + sortOrder + " projections:" + Arrays.toString(projection));
        try {
            return this.mResolver.query(uri, (String[]) null, whereClaus, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when query: ", e);
            return null;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            int rowsupdated = this.mResolver.update(uri, values, selection, selectionArgs);
            String str = TAG;
            Log.i(str, "update success rowsupdated: " + rowsupdated);
            return rowsupdated;
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when update: ", e);
            return 0;
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        try {
            int rowsdeleted = this.mResolver.delete(uri, selection, selectionArgs);
            String str = TAG;
            Log.i(str, "update success rowsupdated: " + rowsdeleted);
            return rowsdeleted;
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when delete: ", e);
            return 0;
        }
    }
}
