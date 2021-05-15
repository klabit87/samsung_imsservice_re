package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfoLog;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;

public class GroupDeliveryInfoProvider extends ContentProvider {
    public static final String AUTHORITY;
    private static final String LOG_TAG = GroupDeliveryInfoProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private final String[] MESSAGES_COLUMS = {"_id", "id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "chat_id", "timestamp_delivered", "timestamp_displayed", "status", "reason_code"};
    private ImCache mCache;

    static {
        String authority = GroupDeliveryInfoLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        sUriMatcher.addURI(authority, "GroupDeliveryInfoLog", 1);
        sUriMatcher.addURI(AUTHORITY, "GroupDeliveryInfoLog/#", 2);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = ImCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String str = LOG_TAG;
        Log.d(str, "query " + uri);
        int uriKind = sUriMatcher.match(uri);
        if (uriKind == 1) {
            return buildMessagesCursor();
        }
        if (uriKind != 2) {
            return new MatrixCursor(this.MESSAGES_COLUMS);
        }
        return buildMessagesCursor(uri);
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private Cursor buildMessagesCursor(Uri uri) {
        String idString = uri.getLastPathSegment();
        if (idString == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(this.MESSAGES_COLUMS);
        fillMessageCursor(cursor, idString);
        return cursor;
    }

    private Cursor buildMessagesCursor() {
        MatrixCursor cursor = new MatrixCursor(this.MESSAGES_COLUMS);
        fillMessageCursor(cursor, (String) null);
        return cursor;
    }

    private void fillMessageCursor(MatrixCursor cursor, String idString) {
        String[] MESSAGES_COLUMS2 = {"_id", "remote_uri", "chat_id", ImContract.ChatItem.DELIVERED_TIMESTAMP, ImContract.Message.DISPLAYED_TIMESTAMP, "notification_status"};
        String[] selectionArgs = {idString};
        Cursor cursorDb = null;
        if (idString == null) {
            try {
                cursorDb = this.mCache.queryMessages(MESSAGES_COLUMS2, (String) null, (String[]) null, (String) null);
            } catch (Throwable th) {
                if (cursorDb != null) {
                    cursorDb.close();
                }
                throw th;
            }
        } else {
            cursorDb = this.mCache.queryMessages(MESSAGES_COLUMS2, "_id= ?", selectionArgs, (String) null);
        }
        if (cursorDb != null) {
            if (cursorDb.getCount() != 0) {
                while (cursorDb.moveToNext()) {
                    NotificationStatus status = NotificationStatus.fromId(cursorDb.getInt(cursorDb.getColumnIndexOrThrow("notification_status")));
                    int state = GroupDeliveryInfo.Status.NOT_DELIVERED.ordinal();
                    if (NotificationStatus.NONE == status) {
                        state = GroupDeliveryInfo.Status.NOT_DELIVERED.ordinal();
                    } else if (NotificationStatus.DELIVERED == status) {
                        state = GroupDeliveryInfo.Status.DELIVERED.ordinal();
                    } else if (NotificationStatus.DISPLAYED == status) {
                        state = GroupDeliveryInfo.Status.DISPLAYED.ordinal();
                    }
                    cursor.newRow().add(Long.valueOf((long) cursorDb.getInt(cursorDb.getColumnIndexOrThrow("_id")))).add(cursorDb.getString(cursorDb.getColumnIndexOrThrow("remote_uri"))).add(String.valueOf(cursorDb.getInt(cursorDb.getColumnIndexOrThrow("chat_id")))).add(Long.valueOf(cursorDb.getLong(cursorDb.getColumnIndexOrThrow(ImContract.ChatItem.DELIVERED_TIMESTAMP)))).add(Long.valueOf(cursorDb.getLong(cursorDb.getColumnIndexOrThrow(ImContract.Message.DISPLAYED_TIMESTAMP)))).add(Integer.valueOf(state)).add(Integer.valueOf(GroupDeliveryInfo.ReasonCode.UNSPECIFIED.ordinal()));
                }
                if (cursorDb != null) {
                    cursorDb.close();
                    return;
                }
                return;
            }
        }
        Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        if (cursorDb != null) {
            cursorDb.close();
        }
    }
}
