package com.sec.internal.ims.servicemodules.tapi.service.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactItem;

public final class BlockContactPersisit {
    public static final String BLOCKED_CONTACT_TABLE = "blockedContacts";
    private static final String LOG_TAG = BlockContactPersisit.class.getSimpleName();
    private static final String SELECTION_BLOCKED = "select * from blockedContacts where phone_number=?";
    public static BlockContactPersisit mInstance;
    private SQLiteDatabase db;
    private Context mContext;
    private DatabaseHelper mDatabaseHelper;

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String CREATE_BLOCK_TABLE = "CREATE TABLE blockedContacts(id INTEGER PRIMARY KEY AUTOINCREMENT,phone_number TEXT,key_blocked TEXT, key_blocking_timestamp LONG);";
        public static final String DATABASE_NAME = "blockContact.db";
        private static final int DATABASE_VERSION = 28;

        private void createDb(SQLiteDatabase db) {
            db.execSQL(CREATE_BLOCK_TABLE);
        }

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 28);
        }

        public void onCreate(SQLiteDatabase db) {
            createDb(db);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    public static synchronized BlockContactPersisit getInstance(Context context) {
        BlockContactPersisit blockContactPersisit;
        synchronized (BlockContactPersisit.class) {
            if (mInstance == null) {
                mInstance = new BlockContactPersisit(context);
            }
            blockContactPersisit = mInstance;
        }
        return blockContactPersisit;
    }

    private BlockContactPersisit(Context context) {
        this.mContext = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(this.mContext);
        this.mDatabaseHelper = databaseHelper;
        try {
            this.db = databaseHelper.getWritableDatabase();
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when getting writableDatabase");
            e.printStackTrace();
        }
    }

    public long insertBlockContactInfo(ContactInfo info, boolean isNotify) {
        Log.d(LOG_TAG, "insertBlockContactInfo");
        if (info == null || info.getContact() == null) {
            Log.d(LOG_TAG, "ContactInfo phone num is null");
            return -1;
        }
        ContentValues cv = new ContentValues();
        cv.put(BlockContactItem.BlockDataItem.PHONE_NUMBER, info.getContact().toString());
        cv.put(BlockContactItem.BlockDataItem.KEY_BLOCKED, info.getBlockingState().toString());
        cv.put(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP, Long.valueOf(info.getBlockingTimestamp()));
        long id = -1;
        try {
            id = this.db.insert(BLOCKED_CONTACT_TABLE, (String) null, cv);
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when inserting block contact info");
            e.printStackTrace();
        }
        if (isNotify) {
            notifyChanged(info.getContact().toString());
        }
        return id;
    }

    private void notifyChanged(String phoneNumber) {
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://com.gsma.services.rcs.provider.blockedcontact").buildUpon().appendPath(phoneNumber).build(), (ContentObserver) null);
    }

    public Cursor query(String phoneNumber) {
        try {
            return this.db.rawQuery(SELECTION_BLOCKED, new String[]{phoneNumber});
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when querying block contact info");
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateBlockContactInfo(ContactInfo info) {
        Log.e(LOG_TAG, "updateBlockContactInfo");
        if (info == null || info.getContact() == null) {
            Log.e(LOG_TAG, "Info or contact is null");
            return false;
        }
        String phoneNum = info.getContact().toString();
        ContentValues cv = new ContentValues();
        cv.put(BlockContactItem.BlockDataItem.PHONE_NUMBER, info.getContact().toString());
        cv.put(BlockContactItem.BlockDataItem.KEY_BLOCKED, info.getBlockingState().toString());
        cv.put(BlockContactItem.BlockDataItem.KEY_BLOCKING_TIMESTAMP, Long.valueOf(info.getBlockingTimestamp()));
        int count = 0;
        try {
            count = this.db.update(BLOCKED_CONTACT_TABLE, cv, "phone_number=?", new String[]{phoneNum});
        } catch (SQLException e) {
            Log.d(LOG_TAG, "got exception when updating block contact info");
            e.printStackTrace();
        }
        if (count > 0) {
            return true;
        }
        return false;
    }

    public static void changeContactInfo(Context context, ContactInfo info) {
        getInstance(context);
        Cursor cursor = mInstance.query(info.getContact().toString());
        if (cursor != null) {
            try {
                if (cursor.getCount() == 0) {
                    mInstance.insertBlockContactInfo(info, false);
                } else {
                    mInstance.updateBlockContactInfo(info);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
            return;
        }
        return;
        throw th;
    }
}
