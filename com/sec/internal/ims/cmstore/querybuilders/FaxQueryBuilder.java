package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import java.io.File;

public class FaxQueryBuilder extends QueryBuilderBase {
    private static final String TAG = FaxQueryBuilder.class.getSimpleName();
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public FaxQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(context);
    }

    public long insertNewFaxUsingChangedObject(ChangedObject object, String line) {
        ContentValues cv = new ContentValues();
        cv.put("sender", Util.getMsisdn(object.extendedMessage.sender));
        String receivers = Util.getMsisdn(object.extendedMessage.recipients[0].uri);
        for (int i = 1; i < object.extendedMessage.recipients.length; i++) {
            receivers = ";" + Util.getMsisdn(object.extendedMessage.recipients[i].uri);
        }
        cv.put("recipients", receivers);
        cv.put(CloudMessageProviderContract.FAXMessages.FAXID, object.extendedMessage.client_correlator);
        cv.put("date", Long.valueOf(getDateFromDateString(object.extendedMessage.message_time)));
        cv.put("flagRead", Integer.valueOf(getIfSeenValueUsingFlag(object.flags)));
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.FAXMessages.DELIVER_STATUS, Integer.valueOf(CloudMessageBufferDBConstants.FaxDeliveryStatus.PENDING.getId()));
        if ("In".equalsIgnoreCase(object.extendedMessage.direction)) {
            cv.put("direction", Integer.valueOf(ImDirection.INCOMING.getId()));
        } else {
            cv.put("direction", Integer.valueOf(ImDirection.OUTGOING.getId()));
        }
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, object.correlationId);
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(object.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        return this.mBufferDB.insertTable(21, cv);
    }

    public Cursor queryFaxMessageBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(21, url);
    }

    public Cursor queryFaxMessageBufferDBwithClientCorrelator(String correlator) {
        String str = TAG;
        Log.i(str, "queryFaxMessageBufferDBwithClientCorrelator: " + correlator);
        return this.mBufferDB.queryTable(21, (String[]) null, "transaction_id=?", new String[]{correlator}, (String) null);
    }

    public long insertFaxMessageUsingObject(ParamOMAObject objt, String line) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, objt.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, objt.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(objt.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(objt.parentFolder.toString()));
        if (!CloudMessageStrategyManager.getStrategy().isNmsEventHasMessageDetail()) {
            cv.put("path", Util.decodeUrlFromServer(objt.path.toString()));
        }
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, objt.payloadURL.toString());
        cv.put("linenum", line);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
        cv.put("sender", Util.getMsisdn(objt.FROM));
        String receivers = Util.getMsisdn(objt.TO.get(0));
        for (int i = 1; i < objt.TO.size(); i++) {
            receivers = ";" + Util.getMsisdn(objt.TO.get(i));
        }
        cv.put("recipients", receivers);
        cv.put("date", Long.valueOf(getDateFromDateString(objt.DATE)));
        cv.put("flagRead", Integer.valueOf(getIfSeenValueUsingFlag(objt.mFlagList)));
        cv.put("uploadstatus", Integer.valueOf(CloudMessageBufferDBConstants.UploadStatusFlag.SUCCESS.getId()));
        cv.put(CloudMessageProviderContract.FAXMessages.DELIVER_STATUS, Integer.valueOf(CloudMessageBufferDBConstants.FaxDeliveryStatus.PENDING.getId()));
        if ("In".equalsIgnoreCase(objt.DIRECTION)) {
            cv.put("direction", Integer.valueOf(ImDirection.INCOMING.getId()));
        } else {
            cv.put("direction", Integer.valueOf(ImDirection.OUTGOING.getId()));
        }
        return this.mBufferDB.insertTable(21, cv);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.ArrayList<android.content.ContentValues> getFaxDataFromTelephony(long r5) {
        /*
            r4 = this;
            r0 = 0
            com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter r1 = r4.mTelephonyStorage
            android.database.Cursor r1 = r1.queryFAXUseRowId(r5)
            if (r1 == 0) goto L_0x0015
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0022 }
            if (r2 == 0) goto L_0x0015
            java.util.ArrayList r2 = com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator.convertFaxtoCV(r1)     // Catch:{ all -> 0x0022 }
            r0 = r2
            goto L_0x001c
        L_0x0015:
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0022 }
            java.lang.String r3 = "getFaxDataFromTelephony: error, no FAX found"
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x0022 }
        L_0x001c:
            if (r1 == 0) goto L_0x0021
            r1.close()
        L_0x0021:
            return r0
        L_0x0022:
            r2 = move-exception
            if (r1 == 0) goto L_0x002d
            r1.close()     // Catch:{ all -> 0x0029 }
            goto L_0x002d
        L_0x0029:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x002d:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.FaxQueryBuilder.getFaxDataFromTelephony(long):java.util.ArrayList");
    }

    public long insertFaxtoBufferDB(ContentValues cv) {
        return this.mBufferDB.insertTable(21, cv);
    }

    public Cursor queryFaxMessageBufferDBwithAppId(long id) {
        String str = TAG;
        Log.d(str, "queryFaxMessageBufferDBwithAppId: " + id);
        return this.mBufferDB.queryTable(21, (String[]) null, "_id=?", new String[]{String.valueOf(id)}, (String) null);
    }

    public Cursor queryToDeviceUnDownloadedFax(String linenum) {
        return this.mBufferDB.queryTable(21, (String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()), linenum}, (String) null);
    }

    public void wipeOutIncomingFaxAttachement(int tableindex, String line) {
        String str = TAG;
        Log.i(str, "wipeOutIncomingFaxAttachement: " + tableindex + " , line = " + IMSLog.checker(line));
        Cursor cs = this.mBufferDB.queryTable(tableindex, (String[]) null, "linenum=? AND direction=?", new String[]{line, String.valueOf(ImDirection.INCOMING.getId())}, (String) null);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    do {
                        String path = cs.getString(cs.getColumnIndexOrThrow("file_path"));
                        if (!TextUtils.isEmpty(path)) {
                            File file = new File(path);
                            if (file.exists() && file.delete()) {
                                String str2 = TAG;
                                Log.d(str2, "wipeOutIncomingFaxAttachement(): " + path);
                            }
                        }
                    } while (cs.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
            return;
        }
        return;
        throw th;
    }
}
