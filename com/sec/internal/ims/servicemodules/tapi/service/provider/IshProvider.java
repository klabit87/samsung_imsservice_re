package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.sharing.image.ImageSharingLog;
import com.sec.internal.ims.servicemodules.csh.CshCache;
import com.sec.internal.ims.servicemodules.csh.ImageShare;
import com.sec.internal.ims.servicemodules.csh.event.CshInfo;
import com.sec.internal.ims.servicemodules.csh.event.IContentShare;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.util.PhoneUtils;

public class IshProvider extends ContentProvider {
    public static final String AUTHORITY;
    private static final String CONTENT_TYPE = "placeholder";
    private static final String LOG_TAG = IshProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private CshCache mCache;
    private final String[] session_columns = {"_id", "sharingId", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "filename", "mime_type", "direction", "filesize", "transferred", "state"};

    static {
        String authority = ImageSharingLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        sUriMatcher.addURI(authority, "ish", 1);
        sUriMatcher.addURI(AUTHORITY, "ish/#", 2);
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
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriKind = sUriMatcher.match(uri);
        MatrixCursor cm = new MatrixCursor(this.session_columns);
        int rowId = 0;
        Log.d(LOG_TAG, "mCache.getSize() = " + this.mCache.getSize());
        char c = 0;
        if (uriKind == 1) {
            int i = 0;
            while (i < this.mCache.getSize()) {
                IContentShare share = this.mCache.getSessionAt(i);
                if (share instanceof ImageShare) {
                    CshInfo info = share.getContent();
                    Log.d(LOG_TAG, info.toString());
                    Object[] objArr = new Object[9];
                    objArr[c] = Integer.valueOf(rowId);
                    objArr[1] = String.valueOf(info.shareId);
                    objArr[2] = PhoneUtils.extractNumberFromUri(info.shareContactUri.toString());
                    objArr[3] = String.valueOf(info.dataPath);
                    objArr[4] = Integer.valueOf(info.shareType);
                    objArr[5] = Integer.valueOf(info.shareDirection);
                    objArr[6] = Long.valueOf(info.dataSize);
                    objArr[7] = Long.valueOf(info.dataProgress);
                    objArr[8] = Integer.valueOf(info.shareState);
                    cm.addRow(objArr);
                    rowId++;
                }
                i++;
                c = 0;
            }
        } else if (uriKind == 2) {
            String shareid = uri.getPathSegments().get(1);
            int i2 = 0;
            while (true) {
                if (i2 >= this.mCache.getSize()) {
                    break;
                }
                CshInfo info2 = this.mCache.getSessionAt(i2).getContent();
                if (shareid != null && info2 != null && shareid.equals(String.valueOf(info2.shareId))) {
                    Log.d(LOG_TAG, info2.toString());
                    cm.addRow(new Object[]{0, String.valueOf(info2.shareId), PhoneUtils.extractNumberFromUri(info2.shareContactUri.toString()), String.valueOf(info2.dataPath), CONTENT_TYPE, Integer.valueOf(info2.shareType), Integer.valueOf(info2.shareDirection), Long.valueOf(info2.dataSize), Long.valueOf(info2.dataProgress), Integer.valueOf(info2.shareState)});
                    int i3 = 0 + 1;
                    break;
                }
                i2++;
            }
        }
        Log.d(LOG_TAG, "cm.getCount() = " + cm.getCount());
        return cm;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
