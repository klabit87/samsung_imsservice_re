package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;

public class MultiLineStatusBuilder extends QueryBuilderBase {
    private static final String TAG = MultiLineStatusBuilder.class.getSimpleName();

    public MultiLineStatusBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
    }

    public int updateLineInitsyncStatus(String line, SyncMsgType type, String searchcursor, int status) {
        String str = TAG;
        Log.d(str, "updateLineStatusTableSearchCursor(): " + IMSLog.checker(line) + "  type: " + type + " cursor: " + searchcursor + " " + OMASyncEventType.valueOf(status));
        String[] selectionArgs = {line, String.valueOf(type.getId())};
        ContentValues cv = new ContentValues();
        if (CloudMessageStrategyManager.getStrategy().shouldClearCursorUponInitSyncDone()) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, searchcursor);
        } else if (TextUtils.isEmpty(searchcursor)) {
            IMSLog.d(TAG, "for certain carriers, we should save last cursor with value and not overwrite with empty cursor");
        } else {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, searchcursor);
        }
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS, Integer.valueOf(status));
        return this.mBufferDB.updateTable(23, cv, "linenum=? AND messagetype=?", selectionArgs);
    }

    public Cursor queryUsingLineAndSyncMsgType(String line, SyncMsgType type) {
        String str = TAG;
        Log.i(str, "queryUsingLineAndSyncMsgType(): " + IMSLog.checker(line) + " type: " + type);
        return this.mBufferDB.queryTable(23, (String[]) null, "linenum=? AND messagetype=?", new String[]{line, String.valueOf(type.getId())}, (String) null);
    }

    public void insertNewLine(String line, SyncMsgType type) {
        String str = TAG;
        Log.i(str, "insertNewLine(): " + IMSLog.checker(line) + " type: " + type);
        ContentValues cv = new ContentValues();
        cv.put("linenum", line);
        cv.put("messagetype", Integer.valueOf(type.getId()));
        this.mBufferDB.insertTable(23, cv);
    }

    public void deleteLine(String line, SyncMsgType type) {
        String str = TAG;
        Log.i(str, "deleteLine(): " + IMSLog.checker(line) + " type: " + type);
        String[] selectionArgs = {line, String.valueOf(type.getId())};
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR, "");
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS, Integer.valueOf(OMASyncEventType.DEFAULT.getId()));
        this.mBufferDB.updateTable(23, cv, "linenum=? AND messagetype=?", selectionArgs);
    }
}
