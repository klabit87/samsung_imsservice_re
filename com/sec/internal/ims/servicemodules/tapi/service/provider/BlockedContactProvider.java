package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.google.android.gms.actions.SearchIntents;
import com.sec.internal.ims.servicemodules.tapi.service.utils.BlockContactPersisit;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.regex.Pattern;

public class BlockedContactProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.gsma.services.rcs.provider.blockedcontact");
    private static final String LOG_TAG = BlockedContactProvider.class.getSimpleName();
    private static final Pattern OPTIONS_PATTERN = Pattern.compile("\\?");
    private Context mContext = null;

    public int delete(Uri arg0, String arg1, String[] arg2) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    public boolean onCreate() {
        this.mContext = getContext();
        return false;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Uri uri2 = Uri.parse(OPTIONS_PATTERN.split(uri.toString())[0]);
        List<String> pathList = uri2.getPathSegments();
        if (pathList.size() != 0) {
            String phoneNum = pathList.get(0);
            String str = LOG_TAG;
            Log.d(str, SearchIntents.EXTRA_QUERY + IMSLog.checker(phoneNum));
            return BlockContactPersisit.getInstance(this.mContext).query(phoneNum);
        }
        throw new UnsupportedOperationException("Operation not supported for uri: ".concat(uri2.toString()).concat(", need parmeter!"));
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        throw new UnsupportedOperationException();
    }
}
