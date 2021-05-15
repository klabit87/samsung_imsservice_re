package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.gsma.services.rcs.CommonServiceConfiguration;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDefaultConst;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.servicemodules.tapi.service.defaultconst.FileTransferDefaultConst;
import java.util.ArrayList;

public class RcsSettingsProvider extends ContentProvider {
    public static final String DATABASE_NAME = "rcs_settings.db";
    /* access modifiers changed from: private */
    public static final String FALSE = Boolean.toString(false);
    private static final String LOG_TAG = "RcsSettingsProvider";
    private static final int RCSAPI_SETTINGS = 1;
    private static final int RCSAPI_SETTINGS_KEY = 2;
    private static final String TABLE = "settings";
    /* access modifiers changed from: private */
    public static final String TRUE = Boolean.toString(true);
    private static final UriMatcher sUriMatcher;
    private SQLiteOpenHelper mOpenHelper;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.gsma.services.rcs.provider.settings", TABLE, 1);
        sUriMatcher.addURI("com.gsma.services.rcs.provider.settings", "settings/*", 2);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 103;

        public DatabaseHelper(Context ctx) {
            super(ctx, RcsSettingsProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 103);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE settings (id integer primary key autoincrement,key TEXT,value TEXT);");
            addParameter(db, "ServiceActivated", RcsSettingsProvider.FALSE);
            addParameter(db, "ConfigurationValidity", RcsSettingsProvider.FALSE);
            addParameter(db, "ServiceAvailable", RcsSettingsProvider.FALSE);
            addParameter(db, "ModeChangeable", RcsSettingsProvider.TRUE);
            addParameter(db, "MinimumBatteryLevel", String.valueOf(CommonServiceConfiguration.MinimumBatteryLevel.NONE.toString()));
            addParameter(db, "DefaultMessagingMethod", CommonServiceConfiguration.MessagingMethod.NON_RCS.toString());
            addParameter(db, "MessagingMode", CommonServiceConfiguration.MessagingMode.NONE.toString());
            addParameter(db, "MyCountryCode", "+1");
            addParameter(db, "CountryAreaCode", "0");
            addParameter(db, "MyContactId", "");
            addParameter(db, "MyDisplayName", "");
            addParameter(db, ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS, Boolean.toString(ImDefaultConst.DEFAULT_CHAT_RESPOND_TO_DISPLAY_REPORTS.booleanValue()));
            addParameter(db, ImSettings.AUTO_ACCEPT_FT_CHANGEABLE, Boolean.toString(false));
            addParameter(db, ImSettings.AUTO_ACCEPT_FILE_TRANSFER, Boolean.toString(false));
            addParameter(db, ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, Boolean.toString(false));
            addParameter(db, ImSettings.KEY_IMAGE_RESIZE_OPTION, FileTransferDefaultConst.DEFALUT_IMAGERESIZEOPTION.toString());
        }

        private void addParameter(SQLiteDatabase db, String key, String value) {
            db.execSQL("INSERT INTO settings (key,value) VALUES ('" + key + "','" + value + "');");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            Cursor oldDataCursor = db.query(RcsSettingsProvider.TABLE, (String[]) null, (String) null, (String[]) null, (String) null, (String) null, (String) null);
            try {
                ArrayList arrayList = new ArrayList();
                if (oldDataCursor != null) {
                    while (oldDataCursor.moveToNext()) {
                        String key = null;
                        String value = null;
                        int index = oldDataCursor.getColumnIndex("key");
                        if (index != -1) {
                            key = oldDataCursor.getString(index);
                        }
                        int index2 = oldDataCursor.getColumnIndex(ImsConstants.Intents.EXTRA_UPDATED_VALUE);
                        if (index2 != -1) {
                            value = oldDataCursor.getString(index2);
                        }
                        if (!(key == null || value == null)) {
                            ContentValues values = new ContentValues();
                            values.put("key", key);
                            values.put(ImsConstants.Intents.EXTRA_UPDATED_VALUE, value);
                            arrayList.add(values);
                        }
                    }
                }
                if (oldDataCursor != null) {
                    oldDataCursor.close();
                }
                db.execSQL("DROP TABLE IF EXISTS settings");
                onCreate(db);
                for (int i = 0; i < arrayList.size(); i++) {
                    ContentValues values2 = (ContentValues) arrayList.get(i);
                    db.update(RcsSettingsProvider.TABLE, values2, "key=\"" + values2.getAsString("key") + "\"", (String[]) null);
                }
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
            throw th;
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public boolean onCreate() {
        this.mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        if (match == 1 || match == 2) {
            return uri.toString();
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    private StringBuilder buildKeyedSelection(String selectionKey, String selectionValue, String selection) {
        StringBuilder sb = new StringBuilder("(");
        sb.append(selectionKey);
        sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
        sb.append(selectionValue);
        StringBuilder selectionKeyBuilder = sb.append(")");
        if (TextUtils.isEmpty(selection)) {
            return selectionKeyBuilder;
        }
        selectionKeyBuilder.append(" AND (");
        selectionKeyBuilder.append(selection);
        selectionKeyBuilder.append(")");
        return selectionKeyBuilder;
    }

    public Cursor query(Uri uri, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        Uri uri2 = uri;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE);
        int match = sUriMatcher.match(uri);
        if (match != 1) {
            if (match == 2) {
                qb.appendWhere(buildKeyedSelection("key", "'" + uri.getLastPathSegment() + "'", (String) null).toString());
            } else {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }
        Cursor cursor = null;
        try {
            cursor = qb.query(this.mOpenHelper.getReadableDatabase(), projectionIn, selection, selectionArgs, (String) null, (String) null, sort);
            if (cursor != null) {
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            }
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "SQL exception while query: " + e);
            if (cursor != null) {
                cursor.close();
            }
        }
        return cursor;
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        int count = 0;
        try {
            SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
            int match = sUriMatcher.match(uri);
            if (match == 1) {
                db.beginTransaction();
                try {
                    count = db.update(TABLE, values, where, whereArgs);
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    endTransaction(db);
                    throw th;
                }
                endTransaction(db);
            } else if (match == 2) {
                String where2 = buildKeyedSelection("key", "'" + uri.getLastPathSegment() + "'", where).toString();
                db.beginTransaction();
                try {
                    count = db.update(TABLE, values, where2, whereArgs);
                    db.setTransactionSuccessful();
                } catch (SQLException e2) {
                    e2.printStackTrace();
                } catch (Throwable th2) {
                    endTransaction(db);
                    throw th2;
                }
                endTransaction(db);
            } else {
                throw new UnsupportedOperationException("Cannot update URI " + uri);
            }
            if (count != 0) {
                getContext().getContentResolver().notifyChange(uri, (ContentObserver) null);
            }
            return count;
        } catch (SQLException e3) {
            Log.d(LOG_TAG, "update: SQLException: " + e3.toString());
            return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new UnsupportedOperationException("Cannot insert URI " + uri);
    }

    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new UnsupportedOperationException();
    }

    private void endTransaction(SQLiteDatabase db) {
        if (db == null) {
            Log.e(LOG_TAG, "endTransaction: db is null");
            return;
        }
        try {
            db.endTransaction();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "SQLException while endTransaction:" + e);
        }
    }
}
