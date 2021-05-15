package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;

public class SummaryQueryBuilder extends QueryBuilderBase {
    private static final String TAG = SummaryQueryBuilder.class.getSimpleName();

    public SummaryQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
    }

    public Cursor querySummaryDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(7, url);
    }

    public int deleteSummaryDBwithResUrl(String url) {
        return this.mBufferDB.deleteTablewithResUrl(7, url);
    }

    public Cursor queryAllPendingNmsEventInSummaryDB() {
        Log.d(TAG, "queryAllPendingNmsEventInSummaryDB()");
        return this.mBufferDB.querySummaryTable((String[]) null, "messagetype=?", new String[]{String.valueOf(0)}, (String) null);
    }

    public int updateSummaryDbUsingObject(ParamOMAObject obj, int msgType) {
        String str = TAG;
        Log.i(str, "updateSummaryDbUsingObject(): " + obj);
        String resUrl = Util.decodeUrlFromServer(obj.resourceURL.toString());
        String objId = Util.extractObjIdFromResUrl(obj.resourceURL.toString());
        String line = Util.getLineTelUriFromObjUrl(obj.resourceURL.toString());
        String[] selectionArgs = {"*" + objId, line};
        ContentValues cv = new ContentValues();
        cv.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(msgType)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, obj.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, obj.correlationTag);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, resUrl);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, obj.lastModSeq);
        cv.put("path", obj.path);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, obj.parentFolder.toString());
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH, obj.parentFolderPath);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(obj.mFlag.getId()));
        return this.mBufferDB.updateTable(7, cv, "res_url GLOB ? AND linenum=?", selectionArgs);
    }

    public long insertSummaryDbUsingObjectIfNonExist(ParamOMAObject obj, int msgType) {
        String str = TAG;
        Log.i(str, "insertSummaryDbUsingObjectIfNonExist(): " + obj);
        Cursor cs = this.mBufferDB.queryTablewithResUrl(7, obj.resourceURL.toString());
        if (cs != null) {
            try {
                if (!cs.moveToFirst()) {
                    long insertObjectToSummaryDB = insertObjectToSummaryDB(obj, msgType);
                    if (cs != null) {
                        cs.close();
                    }
                    return insertObjectToSummaryDB;
                } else if (cs.getInt(cs.getColumnIndexOrThrow("messagetype")) != msgType) {
                    long updateSummaryDbUsingObject = (long) updateSummaryDbUsingObject(obj, msgType);
                    if (cs != null) {
                        cs.close();
                    }
                    return updateSummaryDbUsingObject;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            Log.e(TAG, "insertSummaryDbUsingObjectIfNonExist search error");
        }
        if (cs == null) {
            return 0;
        }
        cs.close();
        return 0;
        throw th;
    }

    public long updateSummaryDbUsingMessageType(long rowId, int msgType) {
        String str = TAG;
        Log.i(str, "updateSummaryDbUsingMessageType(): msgtype: " + msgType);
        String[] selectionArgs = {String.valueOf(rowId)};
        ContentValues cv = new ContentValues();
        cv.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(msgType)));
        return (long) this.mBufferDB.updateTable(7, cv, "_bufferdbid=?", selectionArgs);
    }

    public long insertObjectToSummaryDB(ParamOMAObject obj, int type) {
        ContentValues cv = new ContentValues();
        cv.put("linenum", Util.getLineTelUriFromObjUrl(obj.resourceURL.toString()));
        cv.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(type)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, obj.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, obj.correlationTag);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(obj.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, obj.lastModSeq);
        cv.put("path", obj.path);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, obj.parentFolder.toString());
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDERPATH, obj.parentFolderPath);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        return this.mBufferDB.insertTable(7, cv);
    }

    public long insertNmsEventChangedObjToSummaryDB(ChangedObject objt, int msgType) {
        Log.d(TAG, "insertNmsEventChangedObjToSummaryDB()");
        ContentValues cv = new ContentValues();
        cv.put("linenum", Util.getLineTelUriFromObjUrl(objt.resourceURL.toString()));
        cv.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(msgType)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, objt.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, objt.correlationTag);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, objt.parentFolder.toString());
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        return this.mBufferDB.insertTable(7, cv);
    }

    public long insertNmsEventDeletedObjToSummaryDB(DeletedObject objt, int msgType) {
        ContentValues cv = new ContentValues();
        cv.put("linenum", Util.getLineTelUriFromObjUrl(objt.resourceURL.toString()));
        cv.put("messagetype", Integer.valueOf(convergeRcsMsgTypes(msgType)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, objt.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, objt.correlationTag);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(objt.lastModSeq));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
        return this.mBufferDB.insertTable(7, cv);
    }

    public void deleteAllUsingLineAndTableIndex(int tableindex, String line) {
        this.mBufferDB.deleteTable(7, "linenum=? AND messagetype=?", new String[]{line, String.valueOf(tableindex)});
    }

    private int convergeRcsMsgTypes(int msgType) {
        if (11 == msgType || 12 == msgType || 14 == msgType) {
            return 1;
        }
        return msgType;
    }

    public long insertResUrlinSummaryIfNonExist(String resUrl, int msgType) {
        String str = TAG;
        Log.d(str, "insertResUrlinSummaryIfNonExist(): " + IMSLog.checker(resUrl) + " msgType: " + msgType);
        Cursor cs = this.mBufferDB.queryTablewithResUrl(7, resUrl);
        if (cs != null) {
            try {
                if (!cs.moveToFirst()) {
                    insertResUrlinSummary(resUrl, msgType);
                } else if (cs.getInt(cs.getColumnIndexOrThrow("messagetype")) != msgType) {
                    long updateSummaryDbUsingMessageType = updateSummaryDbUsingMessageType(cs.getLong(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), msgType);
                    if (cs != null) {
                        cs.close();
                    }
                    return updateSummaryDbUsingMessageType;
                }
            } catch (NullPointerException e) {
                String str2 = TAG;
                Log.e(str2, "Nullpointer exception: " + e.getMessage());
            } catch (Throwable th) {
                if (cs != null) {
                    try {
                        cs.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } else {
            Log.e(TAG, "insertResUrlinSummaryIfNonExist search error");
        }
        if (cs == null) {
            return 0;
        }
        cs.close();
        return 0;
    }
}
