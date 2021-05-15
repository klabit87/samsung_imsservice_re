package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.gsma.services.rcs.history.HistoryLog;
import com.gsma.services.rcs.sharing.image.ImageSharingLog;
import com.gsma.services.rcs.sharing.video.VideoSharingLog;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.tapi.service.api.HistoryLogMember;
import com.sec.internal.ims.servicemodules.tapi.service.api.HistoryLogServiceImpl;
import com.sec.internal.ims.servicemodules.tapi.service.api.TapiServiceManager;
import java.util.Map;

public class HistoryLogProvider extends ContentProvider {
    private static final String AUTHORITY = HistoryLog.CONTENT_URI.getAuthority();
    private static final String[] COLUMS = {"provider_id", "id", "mime_type", "direction", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "timestamp", "timestamp_sent", "timestamp_delivered", "timestamp_displayed", "status", "reason_code", CloudMessageProviderContract.BufferDBMMSpdu.READ_STATUS, "chat_id", "content", "fileicon", "fileicon_mime_type", "filename", "filesize", "transferred", "duration"};
    private static final int HISTORY_ID = 2;
    private static final int HISTORY_PARAMLESS = 3;
    private static final int HISTORY_PARAMLESS_ID = 4;
    private static final int HISTROY = 1;
    private static final String LOG_TAG = HistoryLogProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher;
    private Context mContext;
    private HistoryLogServiceImpl mHistorySvcApi = null;
    private ContentResolver mResolver;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI(AUTHORITY, "history", 1);
        sUriMatcher.addURI(AUTHORITY, "history/*", 2);
        sUriMatcher.addURI(AUTHORITY, "history_paramless", 3);
        sUriMatcher.addURI(AUTHORITY, "history_paramless/#", 4);
    }

    public boolean onCreate() {
        Context context = getContext();
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String str = LOG_TAG;
        Log.d(str, "Query: uri = " + uri);
        if (this.mHistorySvcApi == null) {
            this.mHistorySvcApi = TapiServiceManager.getHistoryService();
        }
        if (this.mHistorySvcApi == null) {
            Log.d(LOG_TAG, "Query:  HistoryLogProvider is not available");
            return null;
        }
        int type = sUriMatcher.match(uri);
        if (type != 1) {
            if (type != 2) {
                if (type != 3) {
                    if (type != 4) {
                        return null;
                    }
                }
            }
            String ids = uri.getLastPathSegment();
            if (ids != null) {
                return mergeAll(projection, ids.split("_"));
            }
            return mergeAll(projection, (String[]) null);
        }
        return mergeAll(projection, (String[]) null);
    }

    private MatrixCursor mergeAll(String[] projection, String[] providerIds) {
        String[] cursorColumn;
        Map<Integer, HistoryLogMember> extraProviderMap = this.mHistorySvcApi.getExternalProviderMap();
        if (projection == null) {
            Log.d(LOG_TAG, "mergeAll, projection is null");
            cursorColumn = COLUMS;
        } else {
            cursorColumn = projection;
        }
        MatrixCursor cursor = new MatrixCursor(cursorColumn);
        mergeDefaultProviderCursor(cursor, ChatLog.Message.CONTENT_URI, cursorColumn, 1);
        mergeDefaultProviderCursor(cursor, FileTransferLog.CONTENT_URI, cursorColumn, 2);
        mergeDefaultProviderCursor(cursor, ImageSharingLog.CONTENT_URI, cursorColumn, 3);
        mergeDefaultProviderCursor(cursor, VideoSharingLog.CONTENT_URI, cursorColumn, 4);
        if (providerIds == null) {
            for (Integer id : extraProviderMap.keySet()) {
                mergeAdditionalProviderCursor(cursor, cursorColumn, id.intValue());
            }
            return cursor;
        }
        int length = providerIds.length;
        int i = 0;
        while (i < length) {
            try {
                mergeAdditionalProviderCursor(cursor, cursorColumn, Integer.valueOf(providerIds[i]).intValue());
                i++;
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "NumberFormatException, close cursor");
                cursor.close();
                return null;
            }
        }
        return cursor;
    }

    private void mergeDefaultProviderCursor(MatrixCursor matrixCursor, Uri uri, String[] cursorColums, int providerId) {
        if (cursorColums != null) {
            Cursor cursor = this.mResolver.query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (cursor == null) {
                try {
                    Log.i(LOG_TAG, "No data exsit in " + uri);
                    if (cursor != null) {
                        cursor.close();
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } else {
                Log.d(LOG_TAG, "cursor getCount = " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        MatrixCursor.RowBuilder rowBuilder = matrixCursor.newRow();
                        for (String column : cursorColums) {
                            if (column.equals("provider_id")) {
                                rowBuilder.add(Integer.valueOf(providerId));
                            } else {
                                rowBuilder.add(getColumnValueToInsert(column, providerId, cursor));
                            }
                        }
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            }
        } else {
            return;
        }
        throw th;
    }

    private String getColumnValueToInsert(String column, int providerId, Cursor cursor) {
        if (column.equals("id")) {
            if (providerId == 1) {
                return cursor.getString(cursor.getColumnIndex("msg_id"));
            }
            if (providerId == 0) {
                return cursor.getString(cursor.getColumnIndex("chat_id"));
            }
            if (providerId == 2) {
                return cursor.getString(cursor.getColumnIndex("ft_id"));
            }
        } else if (column.equals("content") && providerId == 2) {
            return cursor.getString(cursor.getColumnIndex("file"));
        }
        int index = cursor.getColumnIndex(column);
        if (index < 0) {
            return null;
        }
        return cursor.getString(index);
    }

    private void mergeAdditionalProviderCursor(MatrixCursor matrixCursor, String[] cursorColums, int providerId) {
        HistoryLogMember member = this.mHistorySvcApi.getExternalProviderMap().get(Integer.valueOf(providerId));
        if (member == null) {
            Log.i(LOG_TAG, "Not registered provider, id = " + providerId);
            return;
        }
        Map<String, String> columnMap = member.getColumnMapping();
        Cursor cursor = this.mResolver.query(member.getUri(), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cursor == null) {
            try {
                Log.i(LOG_TAG, "No data exsit in " + member.getUri());
                if (cursor != null) {
                    cursor.close();
                    return;
                }
                return;
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                MatrixCursor.RowBuilder rowBuilder = matrixCursor.newRow();
                for (String column : cursorColums) {
                    if (column.equals("provider_id")) {
                        rowBuilder.add(Integer.valueOf(providerId));
                    }
                    String cs = columnMap.get(column);
                    if (cs == null) {
                        rowBuilder.add((Object) null);
                    } else {
                        int index = cursor.getColumnIndex(cs);
                        if (index < 0) {
                            rowBuilder.add((Object) null);
                        } else {
                            rowBuilder.add(cursor.getString(index));
                        }
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
                return;
            }
            return;
        }
        throw th;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri arg0, ContentValues arg1) {
        return null;
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        return 0;
    }
}
