package com.sec.internal.ims.entitlement.softphone;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoftphoneSettingsProvider extends ContentProvider {
    private static final int ACCOUNT = 1;
    private static final int ACCOUNT_ADDRESS = 10;
    private static final int ACCOUNT_FOR_ID = 7;
    private static final int ACCOUNT_FOR_ID_MUM = 25;
    private static final int ACCOUNT_FOR_IMPI = 6;
    private static final int ACCOUNT_LABEL = 16;
    private static final int ACCOUNT_LABEL_MUM = 26;
    private static final int ACTIVATE_ACCOUNT = 4;
    private static final int ACTIVE_ACCOUNT = 2;
    private static final int ACTIVE_ACCOUNT_MUM = 23;
    private static final int ACTIVE_ADDRESS = 11;
    private static final int ADDRESS = 8;
    private static final int ADDRESS_ID = 9;
    public static final String AUTHORITY = "com.sec.vsim.attsoftphone.settings";
    public static final String DATABASE_NAME = "softphone.db";
    public static final int DATABASE_VERSION = 3;
    private static final int DEACTIVATE_ACCOUNT = 5;
    private static final int FULL_FUNCTIONAL_ACCOUNT = 21;
    private static final int GET_CURRENT_ADDRESS_BY_ACCOUNT = 17;
    private static final int GET_CURRENT_ADDRESS_BY_IMPI = 18;
    private static final int GET_DEFAULT_ADDRESS_BY_ACCOUNT = 19;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = SoftphoneSettingsProvider.class.getSimpleName();
    private static final int MARK_ADDRESS_DELETED = 24;
    private static final int PENDING_ACCOUNT = 3;
    private static final int REGISTERED_ACCOUNT = 22;
    private static final int REGISTER_ACCOUNT = 20;
    private static final int SET_CURRENT_ADDRESS = 12;
    private static final int SET_DEFAULT_ADDRESS = 14;
    private static final String SQL_WHERE_ACCOUNT_ID = "account_id= ?";
    private static final String SQL_WHERE_ACTIVE_ACCOUNT = "status >= 2";
    private static final String SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT = "status > 3";
    private static final String SQL_WHERE_PENDING_ACCOUNT = "status = 1";
    private static final String SQL_WHERE_REGISTERED_ACCOUNT = "status = 5";
    private static final int UNSET_CURRENT_ADDRESS = 13;
    private static final int UNSET_DEFAULT_ADDRESS = 15;
    private static final UriMatcher sUriMatcher;
    private DatabaseHelper mDbHelper;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/#", 1);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/active_account", 2);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/active_account/#", 23);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/pending_account/#", 3);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/full_functional_account", 21);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/registered_account", 22);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/activate/*", 4);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/deactivate/*", 5);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/register/*", 20);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/impi/*", 6);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/account_id/*", 7);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/account_id/*/#", 25);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/label/*", 16);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "account/label/*/#", 26);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address", 8);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/#", 9);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/account_address/*", 10);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/active_address/*", 11);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/current_address/set/*/#", 12);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/current_address/unset/*/#", 13);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/default_address/set/*/#", 14);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/default_address/unset/*/#", 15);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/saved_address/delete/*/#", 24);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_current_address/by_account/*", 17);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_current_address/by_impi/*", 18);
        sUriMatcher.addURI("com.sec.vsim.attsoftphone.settings", "address/get_default_address/by_account/*", 19);
    }

    private String getAccountId(String impi) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
        qb.appendWhere("impi=\"" + impi + "\"");
        Cursor cursor = qb.query(this.mDbHelper.getReadableDatabase(), new String[]{"account_id"}, (String) null, (String[]) null, (String) null, (String) null, (String) null);
        String accountId = null;
        if (cursor != null) {
            String str = LOG_TAG;
            Log.i(str, "found " + cursor.getCount() + " active users");
            if (cursor.getCount() == 0) {
                accountId = null;
            } else if (cursor.moveToFirst()) {
                accountId = cursor.getString(cursor.getColumnIndex("account_id"));
            }
            cursor.close();
        }
        return accountId;
    }

    private int updateAccountStatus(SQLiteDatabase db, Uri uri, ContentValues values) {
        return db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, values, "account_id= ? AND userid = ?", new String[]{uri.getLastPathSegment(), String.valueOf(Extensions.ActivityManager.getCurrentUser())});
    }

    private long insertAccount(SQLiteDatabase db, Uri uri, ContentValues values) {
        values.put("account_id", uri.getLastPathSegment());
        return db.insert(SoftphoneContract.SoftphoneAccount.TABLE_NAME, (String) null, values);
    }

    private int updateAddressStatus(SQLiteDatabase db, Uri uri, int status) {
        List<String> pathFragments = uri.getPathSegments();
        String accountId = pathFragments.get(pathFragments.size() - 2);
        String addressId = uri.getLastPathSegment();
        ContentValues values = new ContentValues();
        if ("0".equals(addressId) || status > 0) {
            values.put("status", 0);
            db.update("address", values, "account_id= ? AND status > ?", new String[]{accountId, String.valueOf(0)});
        }
        values.clear();
        values.put("status", Integer.valueOf(status));
        int count = db.update("address", values, "_id=" + addressId, (String[]) null);
        if (count == 1) {
            String str = LOG_TAG;
            Log.i(str, "Update address [" + addressId + "] status successfully: status:" + status);
        }
        return count;
    }

    private int updateDefaultAddressStatus(SQLiteDatabase db, Uri uri, int status) {
        List<String> pathFragments = uri.getPathSegments();
        String acountId = pathFragments.get(pathFragments.size() - 2);
        String addressId = uri.getLastPathSegment();
        ContentValues values = new ContentValues();
        if (status != 0) {
            values.put(SoftphoneContract.AddressColumns.DEFAULT_STATUS, 0);
            db.update("address", values, "account_id= ? AND default_status > ?", new String[]{acountId, String.valueOf(0)});
        }
        values.clear();
        values.put(SoftphoneContract.AddressColumns.DEFAULT_STATUS, Integer.valueOf(status));
        int count = db.update("address", values, "_id=" + addressId, (String[]) null);
        if (count == 1) {
            String str = LOG_TAG;
            Log.i(str, "Update address [" + addressId + "] default status successfully: status:" + status);
        }
        return count;
    }

    public boolean onCreate() {
        Log.i(LOG_TAG, "Starting SoftphoneSettingsProvider");
        this.mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uri2 = uri;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String str = LOG_TAG;
        Log.i(str, "query Uri: " + uri + "match" + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            case 1:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere("userid=" + uri.getLastPathSegment());
                break;
            case 2:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                break;
            case 3:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere(SQL_WHERE_PENDING_ACCOUNT);
                qb.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 6:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere("impi=\"" + uri.getLastPathSegment() + "\"");
                break;
            case 7:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                break;
            case 8:
                qb.setTables("address");
                break;
            case 9:
                qb.setTables("address");
                qb.appendWhere("_id=" + uri.getLastPathSegment());
                break;
            case 10:
                qb.setTables("address");
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                break;
            case 11:
                qb.setTables("address");
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                qb.appendWhere(" AND (status>-1)");
                break;
            case 16:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                break;
            case 17:
                qb.setTables("address");
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                qb.appendWhere(" AND (status=1)");
                break;
            case 18:
                String accountId = getAccountId(uri.getLastPathSegment());
                if (accountId != null) {
                    qb.setTables("address");
                    qb.appendWhere("account_id=\"" + accountId + "\"");
                    qb.appendWhere(" AND (status=1)");
                    break;
                } else {
                    return null;
                }
            case 19:
                qb.setTables("address");
                qb.appendWhere("account_id=\"" + uri.getLastPathSegment() + "\"");
                qb.appendWhere(" AND (default_status=2)");
                qb.appendWhere(" AND (status>-1)");
                break;
            case 21:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere(SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT);
                break;
            case 22:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere(SQL_WHERE_REGISTERED_ACCOUNT);
                break;
            case 23:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                qb.appendWhere(SQL_WHERE_ACTIVE_ACCOUNT);
                qb.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 25:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                List<String> pathFragments = uri.getPathSegments();
                qb.appendWhere("account_id=\"" + pathFragments.get(pathFragments.size() - 2) + "\"");
                qb.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            case 26:
                qb.setTables(SoftphoneContract.SoftphoneAccount.TABLE_NAME);
                List<String> pathFragments2 = uri.getPathSegments();
                qb.appendWhere("account_id=\"" + pathFragments2.get(pathFragments2.size() - 2) + "\"");
                qb.appendWhere(" AND (userid=" + uri.getLastPathSegment() + ")");
                break;
            default:
                String str2 = selection;
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        String str3 = LOG_TAG;
        IMSLog.s(str3, "selection: [" + selection + "], selectionArgs: [" + Arrays.toString(selectionArgs) + "], projection: " + Arrays.toString(projection));
        Cursor c = qb.query(db, projection, selection, selectionArgs, (String) null, (String) null, sortOrder);
        String str4 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("found : ");
        sb.append(c.getCount());
        IMSLog.s(str4, sb.toString());
        return c;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        long id;
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        String str = LOG_TAG;
        Log.i(str, "insert at " + uri + "match " + sUriMatcher.match(uri));
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            id = db.insert(SoftphoneContract.SoftphoneAccount.TABLE_NAME, (String) null, values);
        } else if (match == 8) {
            id = db.insert("address", (String) null, values);
        } else if (match == 10) {
            values.put("account_id", uri.getLastPathSegment());
            id = db.insert("address", (String) null, values);
        } else if (match == 20) {
            values.put("status", 5);
            id = insertAccount(db, uri, values);
        } else if (match == 4) {
            values.put("status", 2);
            id = insertAccount(db, uri, values);
        } else if (match == 5) {
            values.put("status", 0);
            id = insertAccount(db, uri, values);
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
        return Uri.withAppendedPath(uri, Long.toString(id));
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        Log.i(LOG_TAG, "delete " + uri + "match " + sUriMatcher.match(uri));
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return db.delete(SoftphoneContract.SoftphoneAccount.TABLE_NAME, selection, selectionArgs);
        }
        if (match != 25) {
            switch (match) {
                case 7:
                    String where = "account_id=\"" + uri.getLastPathSegment() + "\"";
                    if (selection != null) {
                        where = where + " AND (" + selection + ")";
                    }
                    int count = db.delete("address", where, selectionArgs);
                    return db.delete(SoftphoneContract.SoftphoneAccount.TABLE_NAME, where, selectionArgs);
                case 8:
                    return db.delete("address", selection, selectionArgs);
                case 9:
                    return db.delete("address", "_id=" + uri.getLastPathSegment(), (String[]) null);
                case 10:
                    return db.delete("address", "account_id=\"" + uri.getLastPathSegment() + "\"", (String[]) null);
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } else {
            List<String> pathFragments = uri.getPathSegments();
            String where2 = ("account_id=\"" + pathFragments.get(pathFragments.size() - 2) + "\"") + " AND (" + SoftphoneContract.AccountColumns.USERID + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + uri.getLastPathSegment() + ")";
            if (selection != null) {
                where2 = where2 + " AND (" + selection + ")";
            }
            int count2 = db.delete("address", where2, selectionArgs);
            return db.delete(SoftphoneContract.SoftphoneAccount.TABLE_NAME, where2, selectionArgs);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        Uri uri2 = uri;
        ContentValues contentValues = values;
        String str = selection;
        String[] strArr = selectionArgs;
        SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
        Log.i(LOG_TAG, "update " + uri2 + "match " + sUriMatcher.match(uri2));
        switch (sUriMatcher.match(uri2)) {
            case 1:
                count = db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, contentValues, str, strArr);
                break;
            case 2:
                String where = SQL_WHERE_ACTIVE_ACCOUNT;
                if (str != null) {
                    where = where + " AND (" + str + ")";
                }
                count = db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, contentValues, where, strArr);
                break;
            case 4:
                contentValues.put("status", 2);
                count = updateAccountStatus(db, uri2, contentValues);
                break;
            case 5:
                contentValues.put("status", 0);
                count = updateAccountStatus(db, uri2, contentValues);
                break;
            case 7:
                String where2 = "account_id=\"" + uri.getLastPathSegment() + "\"";
                if (str != null) {
                    where2 = where2 + " AND (" + str + ")";
                }
                count = db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, contentValues, where2, strArr);
                break;
            case 8:
                count = db.update("address", contentValues, str, strArr);
                break;
            case 9:
                String where3 = "_id=" + uri.getLastPathSegment();
                if (str != null) {
                    where3 = where3 + " AND (" + str + ")";
                }
                count = db.update("address", contentValues, where3, strArr);
                break;
            case 10:
            case 16:
                String where4 = "account_id=\"" + uri.getLastPathSegment() + "\"";
                if (str != null) {
                    where4 = where4 + " AND (" + str + ")";
                }
                count = db.update("address", contentValues, where4, strArr);
                break;
            case 12:
                count = updateAddressStatus(db, uri2, 1);
                break;
            case 13:
                count = updateAddressStatus(db, uri2, 0);
                break;
            case 14:
                count = updateDefaultAddressStatus(db, uri2, 2);
                break;
            case 15:
                count = updateDefaultAddressStatus(db, uri2, 0);
                break;
            case 20:
                contentValues.put("status", 5);
                count = updateAccountStatus(db, uri2, contentValues);
                break;
            case 21:
                String where5 = SQL_WHERE_FULL_FUNCTIONAL_ACCOUNT;
                if (str != null) {
                    where5 = where5 + " AND (" + str + ")";
                }
                count = db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, contentValues, where5, strArr);
                break;
            case 24:
                count = updateAddressStatus(db, uri2, -1);
                break;
            case 25:
            case 26:
                List<String> pathFragments = uri.getPathSegments();
                String where6 = ("account_id=\"" + pathFragments.get(pathFragments.size() - 2) + "\"") + " AND (" + SoftphoneContract.AccountColumns.USERID + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + uri.getLastPathSegment() + ")";
                if (str != null) {
                    where6 = where6 + " AND (" + str + ")";
                }
                count = db.update(SoftphoneContract.SoftphoneAccount.TABLE_NAME, contentValues, where6, strArr);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri2);
        }
        getContext().getContentResolver().notifyChange(uri2, (ContentObserver) null);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String SQL_CREATE_ACCOUNT = "CREATE TABLE account( account_id TEXT, impi TEXT, msisdn TEXT, access_token TEXT, token_type TEXT, secret_key TEXT, label TEXT, status INTEGER, environment INT DEFAULT -1, userid INT DEFAULT 0 );";
        private static final String SQL_CREATE_ADDRESS = "CREATE TABLE address( _id INTEGER PRIMARY KEY AUTOINCREMENT, account_id TEXT, name TEXT, houseNumber TEXT, houseNumExt TEXT, streetDir TEXT, street TEXT, streetNameSuffix TEXT, streetDirSuffix TEXT, city TEXT, state TEXT, zip TEXT, addressAdditional TEXT, formattedAddress TEXT, E911AID TEXT, expire_date TEXT, status INT DEFAULT 0, default_status INT DEFAULT 0 );";

        public DatabaseHelper(Context context) {
            super(context, SoftphoneSettingsProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 3);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(SoftphoneSettingsProvider.LOG_TAG, "Creating DB.");
            db.execSQL(SQL_CREATE_ACCOUNT);
            db.execSQL(SQL_CREATE_ADDRESS);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String access$000 = SoftphoneSettingsProvider.LOG_TAG;
            Log.v(access$000, "onUpgrade() oldVersion [" + oldVersion + "] , newVersion [" + newVersion + "]");
            List<ContentValues> accountValues = migrateTable(db, SoftphoneContract.SoftphoneAccount.TABLE_NAME);
            List<ContentValues> addressValues = migrateTable(db, "address");
            db.execSQL("DROP TABLE IF EXISTS account");
            db.execSQL("DROP TABLE IF EXISTS address");
            onCreate(db);
            upgradeTable(db, SoftphoneContract.SoftphoneAccount.TABLE_NAME, accountValues);
            upgradeTable(db, "address", addressValues);
        }

        private List<ContentValues> migrateTable(SQLiteDatabase db, String table) {
            List<ContentValues> valuesList = new ArrayList<>();
            Cursor oldDataCursor = db.query(table, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
            if (oldDataCursor != null) {
                while (oldDataCursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    int count = oldDataCursor.getColumnCount();
                    for (int i = 0; i < count; i++) {
                        String key = oldDataCursor.getColumnName(i);
                        String value = oldDataCursor.getString(i);
                        if (!(key == null || value == null)) {
                            values.put(key, value);
                        }
                    }
                    valuesList.add(values);
                }
                oldDataCursor.close();
            }
            return valuesList;
        }

        private void upgradeTable(SQLiteDatabase db, String table, List<ContentValues> valuesList) {
            db.beginTransaction();
            int i = 0;
            while (i < valuesList.size()) {
                try {
                    db.insert(table, (String) null, valuesList.get(i));
                    i++;
                } finally {
                    db.endTransaction();
                }
            }
            db.setTransactionSuccessful();
        }
    }
}
