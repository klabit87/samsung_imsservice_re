package com.sec.internal.ims.servicemodules.csh;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;

public class CshProvider extends ContentProvider {
    private static final String LOG_TAG = CshProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.rcs.cs";
    private static final UriMatcher sUriMatcher;
    private CshCache mCache;
    private final String[] session_columns = {"id", "state", ICshConstants.ShareDatabase.KEY_SHARE_DIRECTION, "type", "size", "path", ICshConstants.ShareDatabase.KEY_PROGRESS, ICshConstants.ShareDatabase.KEY_RESOLUTION_HEIGHT, ICshConstants.ShareDatabase.KEY_RESOLUTION_WIDTH, ICshConstants.ShareDatabase.KEY_TARGET_CONTACT};

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.cs", "active_sessions", 5);
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
        this.mCache = CshCache.getInstance();
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (sUriMatcher.match(uri) != 5) {
            return null;
        }
        MatrixCursor cm = new MatrixCursor(this.session_columns);
        for (int i = 0; i < this.mCache.getSize(); i++) {
            CshInfo info = this.mCache.getSessionAt(i).getContent();
            Log.d(LOG_TAG, info.toString());
            cm.addRow(new String[]{String.valueOf(info.shareId), String.valueOf(info.shareState), String.valueOf(info.shareDirection), String.valueOf(info.shareType), String.valueOf(info.dataSize), String.valueOf(info.dataPath), String.valueOf(info.dataProgress), String.valueOf(info.videoWidth), String.valueOf(info.videoHeight), String.valueOf(info.shareContactUri)});
        }
        return cm;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
