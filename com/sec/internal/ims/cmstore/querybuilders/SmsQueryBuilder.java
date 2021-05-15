package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SmsQueryBuilder extends QueryBuilderBase {
    private static final String TAG = SmsQueryBuilder.class.getSimpleName();
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public SmsQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(context);
    }

    public long insertSMSUsingObject(ParamOMAObject objt, boolean isUpdate, long bufferId) {
        int i;
        long rowId;
        Log.i(TAG, "insertSMSUsingObject: " + isUpdate + " bufferId:" + bufferId);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, objt.correlationTag);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(objt.parentFolder.toString()));
        cv.put("path", Util.decodeUrlFromServer(objt.path.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
        cv.put("linenum", Util.getLineTelUriFromObjUrl(objt.resourceURL.toString()));
        if ("IN".equalsIgnoreCase(objt.DIRECTION)) {
            i = 1;
        } else {
            i = 2;
        }
        cv.put("type", Integer.valueOf(i));
        String remoteAddr = "IN".equalsIgnoreCase(objt.DIRECTION) ? objt.FROM : objt.TO.size() > 0 ? objt.TO.get(0) : null;
        if (remoteAddr != null && remoteAddr.contains("tel:")) {
            remoteAddr = remoteAddr.replace("tel:", "");
        }
        cv.put("address", remoteAddr);
        cv.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        cv.put("date", Long.valueOf(getDateFromDateString(objt.DATE)));
        cv.put("body", objt.TEXT_CONTENT);
        if (objt.mIsGoforwardSync) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (objt.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (objt.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            cv.put("read", 1);
        } else {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()));
            cv.put("read", 0);
        }
        if (isUpdate) {
            rowId = (long) updateTable(3, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferId)});
        } else {
            rowId = insertTable(3, cv);
        }
        Log.d(TAG, "insert SMS: " + rowId + " body: " + objt.TEXT_CONTENT + " res url: " + IMSLog.checker(objt.resourceURL.toString()) + " lastmdf: " + objt.lastModSeq);
        return rowId;
    }

    private Cursor insertSmsUsingRcsBufferDbCursor(Cursor rcs) {
        int i;
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, rcs.getString(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(rcs.getLong(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ))));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, rcs.getString(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL)));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, rcs.getString(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER)));
        cv.put("path", rcs.getString(rcs.getColumnIndex("path")));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(rcs.getInt(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION))));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(rcs.getInt(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION))));
        cv.put("linenum", rcs.getString(rcs.getColumnIndex("linenum")));
        if (ImDirection.INCOMING.getId() == rcs.getInt(rcs.getColumnIndex("direction"))) {
            i = 1;
        } else {
            i = 2;
        }
        cv.put("type", Integer.valueOf(i));
        String remoteAddr = rcs.getString(rcs.getColumnIndex("remote_uri"));
        if (remoteAddr != null && remoteAddr.contains("tel:")) {
            remoteAddr = remoteAddr.replace("tel:", "");
        }
        cv.put("address", remoteAddr);
        cv.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        cv.put("date", Long.valueOf(rcs.getLong(rcs.getColumnIndex(ImContract.ChatItem.INSERTED_TIMESTAMP))));
        cv.put("body", rcs.getString(rcs.getColumnIndex("body")));
        cv.put("read", Integer.valueOf(ImConstants.Status.READ.getId() == rcs.getInt(rcs.getColumnIndex("status")) ? 1 : 0));
        long rowId = insertTable(3, cv);
        this.mBufferDB.deleteTablewithBufferDbId(1, rcs.getLong(rcs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)));
        updateSummaryTableMsgType(rcs.getString(rcs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL)), 3);
        return this.mBufferDB.queryTablewithBufferDbId(3, rowId);
    }

    public Cursor searchSMSBufferUsingCorrelationTagForEarlierNmsEvent(String correlationTag, String lineNum) {
        String str = TAG;
        Log.i(str, "searchSMSBufferUsingCorrelationTagForEarlierNmsEvent: " + correlationTag + " line: " + IMSLog.checker(lineNum));
        Cursor cs = this.mBufferDB.querySMSMessages((String[]) null, "correlation_tag=? AND _id=?", new String[]{correlationTag, String.valueOf(this.VALUE_ID_UNFETCHED)}, "date DESC LIMIT 1");
        if (!CloudMessageStrategyManager.getStrategy().requiresInterworkingCrossSearch()) {
            return cs;
        }
        if (cs == null || !cs.moveToFirst()) {
            return handleCrossSearchRcs(cs, correlationTag, lineNum);
        }
        return cs;
    }

    public Cursor handleCrossSearchRcs(Cursor sms, String correlationTag, String lineNum) {
        Cursor cs = this.mBufferDB.queryTable(1, (String[]) null, "correlation_tag=? AND (_id IS NULL OR _id = '')", new String[]{correlationTag}, "inserted_timestamp DESC LIMIT 1");
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    Cursor insertSmsUsingRcsBufferDbCursor = insertSmsUsingRcsBufferDbCursor(cs);
                    if (cs != null) {
                        cs.close();
                    }
                    return insertSmsUsingRcsBufferDbCursor;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        return sms;
        throw th;
    }

    public Cursor searchSMSBufferUsingRowId(long rowId) {
        return this.mBufferDB.querySMSMessages((String[]) null, "_id=?", new String[]{String.valueOf(rowId)}, (String) null);
    }

    public Cursor querySMSMessagesToUpload() {
        Log.d(TAG, "querySMSMessagesToUpload()");
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND date > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2184))}, (String) null);
    }

    public Cursor querySMSUseRowId(long rowId) {
        return this.mTelephonyStorage.querySMSUseRowId(rowId);
    }

    public Cursor queryAllSMSfromTelephony() {
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, "type=? OR type=?", new String[]{String.valueOf(1), String.valueOf(2)}, (String) null);
    }

    public Cursor queryDeltaSMSfromTelephony() {
        int largestInt = querySmsBufferDBLargestTelephonyId();
        String str = TAG;
        Log.i(str, "queryDeltaSMSfromTelephony largest SMS _id: " + largestInt);
        return this.mTelephonyStorage.querySMSfromTelephony((String[]) null, "_id>?", new String[]{String.valueOf(largestInt)}, (String) null);
    }

    public Cursor queryReadSMSfromTelephony() {
        String[] selectionArgs = {String.valueOf(1)};
        return this.mTelephonyStorage.querySMSfromTelephony(new String[]{"_id"}, "read=?", selectionArgs, (String) null);
    }

    public long insertToSMSBufferDB(Cursor cursor, ContentValues cvFlags, boolean isGoForwardSync) {
        ContentValues contentValues = cvFlags;
        boolean z = isGoForwardSync;
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertSMStoCV(cursor);
        String line = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
        String str = TAG;
        Log.d(str, "insertToSMSBufferDB size: " + cvs.size() + " isGoForwardSync: " + z);
        long row = 0;
        for (int i = 0; i < cvs.size(); i++) {
            ContentValues cv = cvs.get(i);
            Integer type = cv.getAsInteger("type");
            if (type != null) {
                if (!CloudMessageStrategyManager.getStrategy().shouldSkipCmasSMS(cv.getAsString("address"))) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_TAG, CloudMessageStrategyManager.getStrategy().getSmsHashTagOrCorrelationTag(cv.getAsString("address"), type.intValue(), cv.getAsString("body")));
                    Integer localReadCv = cv.getAsInteger("read");
                    int localRead = localReadCv == null ? 0 : localReadCv.intValue();
                    if (!z || localRead != 1) {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    } else {
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                    }
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, 0);
                    cv.put("linenum", line);
                    row = insertDeviceMsgToBuffer(3, cv);
                }
            }
        }
        if (cvs.size() == 1) {
            return row;
        }
        return 0;
    }

    public Cursor querySMSBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(3, url);
    }

    public int deleteSMSBufferDBwithResUrl(String url) {
        return this.mBufferDB.deleteTablewithResUrl(3, url);
    }

    public Cursor queryToCloudUnsyncedSms() {
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND date > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10))}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedSms() {
        return this.mBufferDB.querySMSMessages((String[]) null, "syncdirection=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, (String) null);
    }

    public int querySmsBufferDBLargestTelephonyId() {
        int value = 0;
        Cursor cs = this.mBufferDB.querySMSMessages(new String[]{"MAX(_id)", "_id"}, (String) null, (String[]) null, (String) null);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    value = cs.getInt(cs.getColumnIndexOrThrow("_id"));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        return value;
        throw th;
    }

    public Cursor searchUnSyncedSMSBufferUsingCorrelationTag(String correlationTag) {
        if (correlationTag == null) {
            return null;
        }
        return this.mBufferDB.querySMSMessages((String[]) null, "correlation_tag=? AND (res_url IS NULL OR res_url = '')", new String[]{correlationTag}, "date DESC LIMIT 1");
    }
}
