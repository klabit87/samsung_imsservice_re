package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;

public class MultiLineScheduler extends BaseMessagingScheduler {
    private static final String TAG = MultiLineScheduler.class.getSimpleName();
    private final MultiLineStatusBuilder mBufferDbQuery;

    public MultiLineScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new MultiLineStatusBuilder(context, callback);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0023  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void insertNewLine(java.lang.String r5, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r6) {
        /*
            r4 = this;
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r0 = r4.mBufferDbQuery
            android.database.Cursor r0 = r0.queryUsingLineAndSyncMsgType(r5, r6)
            if (r0 == 0) goto L_0x001c
            boolean r1 = r0.moveToFirst()     // Catch:{ all -> 0x0027 }
            if (r1 == 0) goto L_0x001c
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x0027 }
            java.lang.String r2 = ""
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.DEFAULT     // Catch:{ all -> 0x0027 }
            int r3 = r3.getId()     // Catch:{ all -> 0x0027 }
            r1.updateLineInitsyncStatus(r5, r6, r2, r3)     // Catch:{ all -> 0x0027 }
            goto L_0x0021
        L_0x001c:
            com.sec.internal.ims.cmstore.querybuilders.MultiLineStatusBuilder r1 = r4.mBufferDbQuery     // Catch:{ all -> 0x0027 }
            r1.insertNewLine(r5, r6)     // Catch:{ all -> 0x0027 }
        L_0x0021:
            if (r0 == 0) goto L_0x0026
            r0.close()
        L_0x0026:
            return
        L_0x0027:
            r1 = move-exception
            if (r0 == 0) goto L_0x0032
            r0.close()     // Catch:{ all -> 0x002e }
            goto L_0x0032
        L_0x002e:
            r2 = move-exception
            r1.addSuppressed(r2)
        L_0x0032:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler.insertNewLine(java.lang.String, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType):void");
    }

    public void updateLineInitsyncStatus(String line, SyncMsgType type, String cursor, int status) {
        this.mBufferDbQuery.updateLineInitsyncStatus(line, type, cursor, status);
    }

    public void deleteLine(String line, SyncMsgType type) {
        this.mBufferDbQuery.deleteLine(line, type);
    }

    public int getLineInitSyncStatus(String line, SyncMsgType type) {
        OMASyncEventType initSyncType = OMASyncEventType.DEFAULT;
        Cursor cs = this.mBufferDbQuery.queryUsingLineAndSyncMsgType(line, type);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    initSyncType = OMASyncEventType.valueOf(cs.getInt(cs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS)));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        String str = TAG;
        Log.i(str, "getLineInitSyncStatus(): " + initSyncType);
        return initSyncType == null ? OMASyncEventType.DEFAULT.getId() : initSyncType.getId();
        throw th;
    }
}
