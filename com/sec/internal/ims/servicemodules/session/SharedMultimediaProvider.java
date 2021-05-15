package com.sec.internal.ims.servicemodules.session;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.google.android.gms.actions.SearchIntents;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;

public class SharedMultimediaProvider extends ContentProvider {
    private static final String AUTHORITY = "com.samsung.rcs.sharedmultimedia";
    private static final String LOG_TAG = SharedMultimediaProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher;
    private Object mContentType = null;
    private Object mDirection = null;
    private Object mFilePath = null;
    private Object mInsertedTimestamp = null;
    private Object mRemoteUri = null;
    private final String[] session_columns = {"remote_uri", "file_path", "direction", ImContract.ChatItem.INSERTED_TIMESTAMP, "content_type"};

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.sharedmultimedia", "shared_multimedia", 1);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != 1) {
            return null;
        }
        Log.d(LOG_TAG, "insert");
        this.mRemoteUri = values.get("remote_uri");
        this.mFilePath = values.get("file_path");
        this.mDirection = values.get("direction");
        this.mInsertedTimestamp = values.get(ImContract.ChatItem.INSERTED_TIMESTAMP);
        this.mContentType = values.get("content_type");
        if (this.mFilePath != null) {
            String str = LOG_TAG;
            Log.d(str, "mFilePath : " + this.mFilePath.toString());
        }
        if (this.mDirection != null) {
            String str2 = LOG_TAG;
            Log.d(str2, "mDirection : " + this.mDirection.toString());
        }
        if (this.mInsertedTimestamp != null) {
            String str3 = LOG_TAG;
            Log.d(str3, "mInsertedTimestamp : " + this.mInsertedTimestamp.toString());
        }
        if (this.mContentType != null) {
            String str4 = LOG_TAG;
            Log.d(str4, "mContentType : " + this.mContentType.toString());
        }
        onContentsInserted();
        return uri;
    }

    public boolean onCreate() {
        Log.d(LOG_TAG, "SharedMultimediaProvider : onCreate()");
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (sUriMatcher.match(uri) != 1) {
            return null;
        }
        Log.d(LOG_TAG, SearchIntents.EXTRA_QUERY);
        MatrixCursor cm = new MatrixCursor(this.session_columns);
        cm.addRow(new Object[]{this.mRemoteUri, this.mFilePath, this.mDirection, this.mInsertedTimestamp, this.mContentType});
        return cm;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private void onContentsInserted() {
        Intent intent = new Intent();
        intent.addCategory(ISharedMultimediaConstants.CATEGORY_NOTIFICATION);
        intent.setAction(ISharedMultimediaConstants.NOTIFICATION_MULTI_DATA_INSERTION);
        String str = LOG_TAG;
        Log.d(str, "broadcastIntent: " + intent.toString());
        getContext().sendBroadcastAsUser(intent, ContextExt.CURRENT_OR_SELF);
    }
}
