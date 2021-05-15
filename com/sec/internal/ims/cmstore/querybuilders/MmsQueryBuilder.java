package com.sec.internal.ims.cmstore.querybuilders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.adapters.CloudMessageTelephonyStorageAdapter;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CursorContentValueTranslator;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MmsQueryBuilder extends QueryBuilderBase {
    private static final String TAG = MmsQueryBuilder.class.getSimpleName();
    private final CloudMessageTelephonyStorageAdapter mTelephonyStorage;

    public MmsQueryBuilder(Context context, IBufferDBEventListener callbackapp) {
        super(context, callbackapp);
        this.mTelephonyStorage = new CloudMessageTelephonyStorageAdapter(context);
    }

    public Cursor searchMMSPduBufferUsingMidorTrId(String mid, String tr_id) {
        String subtrId = "invalid string";
        if (tr_id == null || tr_id.length() <= 2) {
            tr_id = "invalid string";
        } else {
            subtrId = tr_id.substring(2);
        }
        String[] selectionArgs = {mid, tr_id, mid, subtrId};
        String str = TAG;
        Log.d(str, "searchMMSPduBufferUsingMidorTrId, mid: " + mid + " tr_id: " + tr_id + " subtrid:" + subtrId);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "m_id=? OR tr_id=? OR correlation_id=? OR correlation_id=?", selectionArgs, (String) null);
    }

    public Cursor searchMMsPduBufferUsingCorrelationId(String corrId) {
        String str = TAG;
        Log.d(str, "searchMMsPduBufferUsingCorrelationId: " + corrId);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "m_id=? OR tr_id GLOB ?", new String[]{corrId, "*" + corrId + "*"}, (String) null);
    }

    public Cursor searchMMSPduBufferUsingRowId(long rowId) {
        String str = TAG;
        Log.d(str, "searchMMSPduBufferUsingRowId: " + rowId);
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "_id=?", new String[]{String.valueOf(rowId)}, (String) null);
    }

    public Cursor queryMMSMessagesToUpload() {
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=? AND (res_url IS NULL OR res_url = '') AND date > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2184))}, (String) null);
    }

    public void updateMMSUpdateingDevice(long bufferDBid) {
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        updateTable(4, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferDBid)});
    }

    public long insertToMMSPDUBufferDB(Cursor cursor, ContentValues cvFlags, boolean isGoForwardSync) {
        Throwable th;
        Throwable th2;
        ContentValues contentValues = cvFlags;
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertPDUtoCV(cursor);
        long j = 0;
        if (cvs == null) {
            return 0;
        }
        String line = CloudMessagePreferenceManager.getInstance().getUserTelCtn();
        String str = TAG;
        Log.d(str, "insertToPDUBufferDB size: " + cvs.size());
        long row = 0;
        int i = 0;
        while (i < cvs.size()) {
            ContentValues cv = cvs.get(i);
            if (cv != null) {
                Integer localReadCv = cv.getAsInteger("read");
                int localRead = localReadCv == null ? 0 : localReadCv.intValue();
                if (!isGoForwardSync || localRead != 1) {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, contentValues.getAsInteger(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                } else {
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
                    cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
                }
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, 0);
                cv.put("linenum", line);
                row = insertDeviceMsgToBuffer(4, cv);
                Integer mmsIdInteger = cv.getAsInteger("_id");
                long mmsId = mmsIdInteger == null ? j : mmsIdInteger.longValue();
                Cursor cursorAddr = this.mTelephonyStorage.getTelephonyAddr(mmsId);
                if (cursorAddr != null) {
                    try {
                        if (cursorAddr.moveToFirst()) {
                            insertToMMSAddrBufferDB(cursorAddr, row);
                        }
                    } catch (Throwable th3) {
                        th2.addSuppressed(th3);
                    }
                }
                if (cursorAddr != null) {
                    cursorAddr.close();
                }
                Cursor cursorPart = this.mTelephonyStorage.getTelephonyPart(mmsId);
                if (cursorPart != null) {
                    try {
                        if (cursorPart.moveToFirst()) {
                            insertToMMSPartBufferDB(cursorPart, row);
                        }
                    } catch (Throwable th4) {
                        th.addSuppressed(th4);
                    }
                }
                if (cursorPart != null) {
                    cursorPart.close();
                }
            }
            i++;
            contentValues = cvFlags;
            j = 0;
        }
        if (cvs.size() == 1) {
            return row;
        }
        return 0;
        throw th;
        throw th2;
    }

    private void insertToMMSPartBufferDB(Cursor cursor, long pdurowId) {
        String str = TAG;
        Log.d(str, "we do get something from telephony MMS Part: " + cursor.getCount() + ", row=" + pdurowId);
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertPARTtoCV(cursor);
        if (cvs != null) {
            for (int i = 0; i < cvs.size(); i++) {
                ContentValues cv = cvs.get(i);
                cv.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Long.valueOf(pdurowId));
                insertDeviceMsgToBuffer(6, cv);
            }
        }
    }

    private void insertToMMSAddrBufferDB(Cursor cursor, long pdurowId) {
        String str = TAG;
        Log.d(str, "insertToAddrBufferDB: " + pdurowId + "we do get something from telephony MMS Addr: " + cursor.getCount());
        ArrayList<ContentValues> cvs = CursorContentValueTranslator.convertADDRtoCV(cursor);
        if (cvs != null) {
            for (int i = 0; i < cvs.size(); i++) {
                ContentValues cv = cvs.get(i);
                cv.put("msg_id", Long.valueOf(pdurowId));
                insertDeviceMsgToBuffer(5, cv);
            }
        }
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long mmsId) {
        String str = TAG;
        Log.d(str, "queryMMSPduFromTelephonyDbUseID: " + mmsId);
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDbUseID(mmsId);
    }

    public Cursor queryAllMMSPduFromTelephonyDb() {
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, (String) null, (String[]) null, (String) null);
    }

    public Cursor queryDeltaMMSPduFromTelephonyDb() {
        int largestInt = queryMmsPduBufferDBLargestTelephonyId();
        String str = TAG;
        Log.i(str, "queryDeltaMMSPduFromTelephonyDb largest MMS _id: " + largestInt);
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, "_id>" + largestInt, (String[]) null, (String) null);
    }

    public Cursor queryReadMmsfromTelephony() {
        return this.mTelephonyStorage.queryMMSPduFromTelephonyDb((String[]) null, "read=?", new String[]{String.valueOf(1)}, (String) null);
    }

    public int queryMmsPduBufferDBLargestTelephonyId() {
        int value = 0;
        Cursor cs = this.mBufferDB.queryMMSPDUMessages(new String[]{"MAX(_id)", "_id"}, (String) null, (String[]) null, (String) null);
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

    public long insertMMSUsingObject(ParamOMAObject objt, boolean isUpdate, long bufferId) {
        long rowId;
        String remoteAddr;
        ParamOMAObject paramOMAObject = objt;
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.CORRELATION_ID, paramOMAObject.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.M_ID, paramOMAObject.correlationId);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
        cv.put("path", Util.decodeUrlFromServer(paramOMAObject.path.toString()));
        cv.put("linenum", Util.getLineTelUriFromObjUrl(paramOMAObject.resourceURL.toString()));
        cv.put("date", Long.valueOf(getDateFromDateString(paramOMAObject.DATE)));
        cv.put("_id", Integer.valueOf(this.VALUE_ID_UNFETCHED));
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.CT_T, paramOMAObject.MULTIPARTCONTENTTYPE);
        if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
            cv.put(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX, 1);
            cv.put(CloudMessageProviderContract.BufferDBMMSpdu.M_TYPE, 132);
        } else {
            cv.put(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX, 2);
            cv.put(CloudMessageProviderContract.BufferDBMMSpdu.M_TYPE, 128);
        }
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.SUB, paramOMAObject.SUBJECT);
        if (paramOMAObject.mIsGoforwardSync) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Delete)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        } else if (paramOMAObject.mFlag.equals(CloudMessageBufferDBConstants.ActionStatusFlag.Update)) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            cv.put("read", 1);
            cv.put("seen", 1);
        } else {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Downloading.getId()));
            cv.put("read", 0);
            cv.put("seen", 0);
        }
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.M_CLS, "personal");
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.V, 18);
        cv.put("pri", 129);
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.RR, 129);
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.D_RPT, 129);
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.RETR_ST, 128);
        cv.put(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID, "D4" + paramOMAObject.correlationId);
        if (isUpdate) {
            rowId = (long) updateTable(4, cv, "_bufferdbid=?", new String[]{String.valueOf(bufferId)});
        } else {
            rowId = insertTable(4, cv);
        }
        String str = TAG;
        Log.d(str, "insert MMS: " + rowId + " res url: " + IMSLog.checker(paramOMAObject.resourceURL.toString()) + " lastmdf: " + paramOMAObject.lastModSeq + " objt size: " + IMSLog.checker(Integer.valueOf(paramOMAObject.TO.size())) + " payloadPart: " + IMSLog.checker(paramOMAObject.payloadPart));
        cv.clear();
        cv.put("msg_id", Long.valueOf(rowId));
        if ("IN".equalsIgnoreCase(paramOMAObject.DIRECTION)) {
            remoteAddr = paramOMAObject.FROM;
        } else {
            remoteAddr = ITelephonyDBColumns.FROM_INSERT_ADDRESS_TOKEN_STR;
        }
        if (remoteAddr != null && remoteAddr.contains("tel:")) {
            remoteAddr = remoteAddr.replace("tel:", "");
        }
        cv.put("address", remoteAddr);
        cv.put("type", 137);
        cv.put("charset", 106);
        insertTable(5, cv);
        for (int i = 0; i < paramOMAObject.TO.size(); i++) {
            cv.clear();
            cv.put("msg_id", Long.valueOf(rowId));
            String remoteAddr2 = paramOMAObject.TO.get(i);
            if (remoteAddr2 != null && remoteAddr2.contains("tel:")) {
                remoteAddr2 = remoteAddr2.replace("tel:", "");
            }
            cv.put("address", remoteAddr2);
            cv.put("type", 151);
            cv.put("charset", 106);
            insertTable(5, cv);
        }
        if (paramOMAObject.payloadPart != null) {
            cv.clear();
            for (int i2 = 0; i2 < paramOMAObject.payloadPart.length; i2++) {
                cv.put(CloudMessageProviderContract.BufferDBMMSpart.MID, Long.valueOf(rowId));
                cv.put(CloudMessageProviderContract.BufferDBMMSpart.CT, paramOMAObject.payloadPart[i2].contentType.split(";")[0]);
                cv.put(CloudMessageProviderContract.BufferDBMMSpart.CID, paramOMAObject.payloadPart[i2].contentId);
                cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL, paramOMAObject.payloadPart[i2].href.toString());
                cv.put(CloudMessageProviderContract.BufferDBMMSpart.CL, Util.generateLocation(paramOMAObject.payloadPart[i2]));
                insertTable(6, cv);
            }
        }
        return rowId;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0044  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryIfMmsPartsDownloadComplete(long r7) {
        /*
            r6 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "queryIfMmsPartsDownloadComplete: "
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            java.lang.String r0 = "mid= ? AND (_data IS NULL OR _data = '') AND (text IS NULL OR text = '')"
            r1 = 1
            java.lang.String[] r1 = new java.lang.String[r1]
            java.lang.String r2 = java.lang.String.valueOf(r7)
            r3 = 0
            r1[r3] = r2
            r2 = 1
            com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister r3 = r6.mBufferDB
            r4 = 0
            android.database.Cursor r3 = r3.queryMMSPARTMessages(r4, r0, r1, r4)
            if (r3 == 0) goto L_0x0041
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0035 }
            if (r4 == 0) goto L_0x0041
            r2 = 0
            goto L_0x0042
        L_0x0035:
            r4 = move-exception
            if (r3 == 0) goto L_0x0040
            r3.close()     // Catch:{ all -> 0x003c }
            goto L_0x0040
        L_0x003c:
            r5 = move-exception
            r4.addSuppressed(r5)
        L_0x0040:
            throw r4
        L_0x0041:
            r2 = 1
        L_0x0042:
            if (r3 == 0) goto L_0x0047
            r3.close()
        L_0x0047:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder.queryIfMmsPartsDownloadComplete(long):boolean");
    }

    public Cursor queryUndownloadedPart(long bufferDbId) {
        return this.mBufferDB.queryMMSPARTMessages((String[]) null, "mid= ? AND (_data IS NULL OR _data = '') AND (text IS NULL OR text = '')", new String[]{String.valueOf(bufferDbId)}, (String) null);
    }

    public Cursor queryMMSBufferDBwithResUrl(String url) {
        return this.mBufferDB.queryTablewithResUrl(4, url);
    }

    public int deleteMMSBufferDBwithResUrl(String url) {
        return this.mBufferDB.deleteTablewithResUrl(4, url);
    }

    public Cursor queryToDeviceUnDownloadedMms(String linenum) {
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncaction=? AND linenum=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.DownLoad.getId()), linenum}, (String) null);
    }

    public Cursor queryToCloudUnsyncedMms() {
        Log.d(TAG, "queryToCloudUnsyncedMms: ");
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=? AND res_url IS NOT NULL AND date > ?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()), String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(10))}, (String) null);
    }

    public Cursor queryToDeviceUnsyncedMms() {
        Log.d(TAG, "queryToDeviceUnsyncedMms: ");
        return this.mBufferDB.queryMMSPDUMessages((String[]) null, "syncdirection=?", new String[]{String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())}, (String) null);
    }
}
