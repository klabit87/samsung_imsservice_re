package com.sec.internal.ims.servicemodules.presence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactItem;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialPresenceStorage {
    private static final String LOG_TAG = "SocialPresenceStorage";
    private static final String[] PRESENCE_PROJECTION = {"_id", CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, "raw_contact_id", "tel_uri", "uri", "timestamp", PresenceTable.DISPLAY_NAME, BlockContactItem.BlockDataItem.PHONE_NUMBER, "avatar_uri", PresenceTable.BIRTHDAY, "email", "activities", PresenceTable.MOOD, "hyper", PresenceTable.FACEBOOK, PresenceTable.TWITTER, PresenceTable.CYWORLD, "phone_id"};
    private SocialPresenceCache mCache;
    private Context mContext;
    private DatabaseHelper mDbHelper;
    private int mPhoneId = 0;

    public static class PresenceTable {
        static final String ACTIVITIES = "activities";
        static final String AVATAR_URI = "avatar_uri";
        public static final String BIRTHDAY = "birthday";
        static final String CONTACT_ID = "contact_id";
        public static final String CYWORLD = "cyworld";
        public static final String DISPLAY_NAME = "display_name";
        public static final String EMAIL = "email";
        public static final String FACEBOOK = "facebook";
        public static final String HOMEPAGE = "homepage";
        static final String HYPER = "hyper";
        public static final String MOOD = "mood_text";
        static final String PHONE_ID = "phone_id";
        static final String PHONE_NUMBER = "phone_number";
        static final String RAW_CONTACT_ID = "raw_contact_id";
        static final String TABLE_NAME = "presence";
        static final String TEL_URI = "tel_uri";
        static final String TIMESTAMP = "timestamp";
        public static final String TWITTER = "twitter";
        static final String URI = "uri";
        static final String _ID = "_id";
    }

    public SocialPresenceStorage(Context context, SocialPresenceCache cache, int phoneId) {
        this.mContext = context;
        this.mCache = cache;
        this.mDbHelper = new DatabaseHelper(this.mContext);
        this.mPhoneId = phoneId;
    }

    public void persist() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "persist:");
        List<ImsUri> urisToUpdate = this.mCache.getUpdatedUriList();
        List<ImsUri> urisToDelete = this.mCache.getTrashedUriList();
        try {
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            db.beginTransaction();
            if (urisToUpdate != null) {
                try {
                    if (urisToUpdate.size() > 0) {
                        List<PresenceInfo> piListToUpdate = new ArrayList<>();
                        for (ImsUri uri : urisToUpdate) {
                            PresenceInfo pi = this.mCache.get(uri);
                            if (pi != null) {
                                piListToUpdate.add(pi);
                            } else {
                                Log.e(LOG_TAG, "persist: not found in cache.");
                            }
                        }
                        update(db, piListToUpdate);
                    }
                } catch (SQLiteFullException e) {
                    Log.e(LOG_TAG, "persist: SQLiteFullException: " + e.toString());
                } catch (SQLiteDiskIOException e2) {
                    Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e2.toString());
                } catch (SQLException e3) {
                    Log.e(LOG_TAG, "persist: SQLException: " + e3.toString());
                } catch (Throwable th) {
                    endTransaction(db);
                    throw th;
                }
            }
            if (urisToDelete != null && urisToDelete.size() > 0) {
                remove(db, urisToDelete);
            }
            db.setTransactionSuccessful();
            endTransaction(db);
        } catch (SQLiteDiskIOException e4) {
            Log.e(LOG_TAG, "persist: SQLiteDiskIOException: " + e4.toString());
        }
    }

    public void reset() {
        Log.i(LOG_TAG, "reset:");
        try {
            SQLiteDatabase db = this.mDbHelper.getWritableDatabase();
            db.delete("presence", "phone_id = ?", new String[]{String.valueOf(this.mPhoneId)});
        } catch (SQLiteDiskIOException e) {
            Log.e(LOG_TAG, "reset: SQLiteDiskIOException: " + e.toString());
        }
    }

    private void remove(SQLiteDatabase db, List<ImsUri> list) {
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "remove: " + list.size() + " uris");
        for (ImsUri uri : list) {
            db.delete("presence", "tel_uri = ? AND phone_id = ?", new String[]{uri.toString(), String.valueOf(this.mPhoneId)});
        }
    }

    private void update(SQLiteDatabase db, Collection<PresenceInfo> list) {
        ContentValues values = new ContentValues();
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "update: " + list.size() + " PresenceInfo");
        for (PresenceInfo pi : list) {
            values.clear();
            fillPresenceInfo(values, pi);
            if (pi.getId() < 0 || pi.getPhoneId() != this.mPhoneId) {
                pi.setId(db.insert("presence", (String) null, values));
            } else {
                db.update("presence", values, "_id=" + pi.getId(), (String[]) null);
            }
        }
    }

    private void fillPresenceInfo(ContentValues values, PresenceInfo pi) {
        if (pi.getContactId() != null) {
            values.put(CloudMessageProviderContract.BufferDBMMSaddr.CONTACT_ID, pi.getContactId());
        }
        if (pi.getRawContactId() != null) {
            values.put("raw_contact_id", pi.getRawContactId());
        }
        values.put("tel_uri", pi.getTelUri());
        values.put("uri", pi.getUri());
        values.put("timestamp", Long.valueOf(pi.getTimestamp()));
        values.put(PresenceTable.DISPLAY_NAME, pi.getDisplayName());
        values.put("avatar_uri", pi.getAvatarUri());
        values.put(PresenceTable.BIRTHDAY, pi.getBirthday());
        values.put("email", pi.getEmail());
        values.put("activities", pi.getActivities());
        values.put("hyper", Integer.valueOf(pi.getHyper()));
        values.put(PresenceTable.FACEBOOK, pi.getFacebook());
        values.put(PresenceTable.TWITTER, pi.getTwitter());
        values.put(PresenceTable.CYWORLD, pi.getCyworld());
        values.put("phone_id", Integer.valueOf(pi.getPhoneId()));
    }

    private PresenceInfo fillPresenceInfo(Cursor cursor) {
        PresenceInfo presenceInfo = new PresenceInfo.Builder().tel_uri(cursor.getString(cursor.getColumnIndex("tel_uri"))).uri(cursor.getString(cursor.getColumnIndex("uri"))).activities(cursor.getString(cursor.getColumnIndex("activities"))).avatar_uri(cursor.getString(cursor.getColumnIndex("avatar_uri"))).birthday(cursor.getString(cursor.getColumnIndex(PresenceTable.BIRTHDAY))).cyworld(cursor.getString(cursor.getColumnIndex(PresenceTable.CYWORLD))).display_name(cursor.getString(cursor.getColumnIndex(PresenceTable.DISPLAY_NAME))).email(cursor.getString(cursor.getColumnIndex("email"))).facebook(cursor.getString(cursor.getColumnIndex(PresenceTable.FACEBOOK))).hyper(cursor.getInt(cursor.getColumnIndex("hyper"))).mood_text(cursor.getString(cursor.getColumnIndex(PresenceTable.MOOD))).phone_number(cursor.getString(cursor.getColumnIndex(BlockContactItem.BlockDataItem.PHONE_NUMBER))).timestamp(cursor.getLong(cursor.getColumnIndex("timestamp"))).twitter(cursor.getString(cursor.getColumnIndex(PresenceTable.TWITTER))).state(1).phoneId(cursor.getInt(cursor.getColumnIndex("phone_id"))).build();
        presenceInfo.setId((long) cursor.getInt(cursor.getColumnIndex("_id")));
        return presenceInfo;
    }

    public PresenceInfo get(ImsUri teluri) {
        if (teluri == null) {
            return null;
        }
        int i = this.mPhoneId;
        IMSLog.s(LOG_TAG, i, "get: teluri-" + teluri);
        String[] selectionArgs = {teluri.toString(), String.valueOf(this.mPhoneId)};
        PresenceInfo presenceInfo = null;
        Cursor cursor = this.mDbHelper.getReadableDatabase().query("presence", PRESENCE_PROJECTION, "tel_uri = ? AND phone_id = ?", selectionArgs, (String) null, (String) null, (String) null);
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                presenceInfo = fillPresenceInfo(cursor);
            }
            if (cursor != null) {
                cursor.close();
            }
            return presenceInfo;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public Map<ImsUri, PresenceInfo> get(List<ImsUri> telUriList) {
        if (telUriList == null) {
            return null;
        }
        int size = telUriList.size();
        IMSLog.s(LOG_TAG, this.mPhoneId, "get: querying " + size + " telUris");
        if (size == 0) {
            return null;
        }
        StringBuilder sbSelection = new StringBuilder();
        sbSelection.append("tel_uri");
        sbSelection.append(" in (");
        for (ImsUri teluri : telUriList) {
            sbSelection.append("'" + teluri.toString() + "'");
            size += -1;
            if (size > 0) {
                sbSelection.append(",");
            }
        }
        sbSelection.append(")");
        String selection = sbSelection.toString() + " AND " + "phone_id" + " = ?";
        String[] selectionArgs = {String.valueOf(this.mPhoneId)};
        IMSLog.s(LOG_TAG, this.mPhoneId, "get: selection = " + selection);
        SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
        Map<ImsUri, PresenceInfo> piMap = new HashMap<>();
        Cursor cursor = db.query("presence", PRESENCE_PROJECTION, selection, selectionArgs, (String) null, (String) null, (String) null);
        if (cursor != null) {
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "get: presenceInfo " + cursor.getCount() + " from DB");
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        PresenceInfo presenceInfo = fillPresenceInfo(cursor);
                        piMap.put(ImsUri.parse(presenceInfo.getTelUri()), presenceInfo);
                    } while (cursor.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return piMap;
        throw th;
    }

    private void endTransaction(SQLiteDatabase db) {
        if (db == null) {
            Log.e(LOG_TAG, "endTransaction: db is null");
            return;
        }
        try {
            db.endTransaction();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "presence.db";
        private static final int DATABASE_VERSION = 5;
        private static final String SQL_CREATE_CAPABILTIES_TABLE = "CREATE TABLE presence( _id INTEGER PRIMARY KEY, contact_id TEXT, raw_contact_id TEXT, tel_uri TEXT, uri TEXT, timestamp BIGINT DEFAULT 0, display_name TEXT, phone_number TEXT, avatar_uri TEXT, birthday TEXT, email TEXT, homepage TEXT, activities TEXT, hyper INT, mood_text TEXT, facebook TEXT, twitter TEXT, cyworld TEXT, phone_id INT );";
        private static final String SQL_CREATE_INDEX_TEL_URI = "CREATE INDEX idx_tel_uri ON presence (tel_uri);";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 5);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(SocialPresenceStorage.LOG_TAG, "onCreate: Creating DB.");
            db.execSQL("DROP TABLE IF EXISTS presence");
            db.execSQL(SQL_CREATE_CAPABILTIES_TABLE);
            db.execSQL(SQL_CREATE_INDEX_TEL_URI);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(SocialPresenceStorage.LOG_TAG, "onUpgrade() oldVersion [" + oldVersion + "] , newVersion [" + newVersion + "]");
            if (db.getVersion() != newVersion) {
                onCreate(db);
                db.setVersion(5);
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.v(SocialPresenceStorage.LOG_TAG, "onDowngrade() oldVersion [" + oldVersion + "] , newVersion [" + newVersion + "]");
            if (db.getVersion() != newVersion) {
                onCreate(db);
                db.setVersion(5);
            }
        }
    }
}
