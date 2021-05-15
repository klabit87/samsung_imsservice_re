package com.sec.internal.ims.cmstore.syncschedulers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.DeviceLegacyUpdateParam;
import com.sec.internal.ims.cmstore.params.DeviceMsgAppFetchUpdateParam;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamNmsNotificationList;
import com.sec.internal.ims.cmstore.params.ParamOMAObject;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet;
import com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.ChangedObject;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import com.sec.internal.omanetapi.nms.data.NmsEvent;
import java.util.ArrayList;

public class MmsScheduler extends BaseMessagingScheduler {
    private static final String TAG = MmsScheduler.class.getSimpleName();
    private final MmsQueryBuilder mBufferDbQuery;
    private final MultiLineScheduler mMultiLineScheduler;

    public MmsScheduler(Context context, CloudMessageBufferDBEventSchedulingRule rule, SummaryQueryBuilder builder, MultiLineScheduler lineStatus, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback, Looper looper) {
        super(context, rule, deviceDataListener, callback, looper, builder);
        this.mBufferDbQuery = new MmsQueryBuilder(context, callback);
        this.mMultiLineScheduler = lineStatus;
        this.mDbTableContractIndex = 4;
    }

    /* Debug info: failed to restart local var, previous not found, register: 31 */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0164 A[SYNTHETIC, Splitter:B:29:0x0164] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0170 A[SYNTHETIC, Splitter:B:36:0x0170] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleObjectMMSCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject r32) {
        /*
            r31 = this;
            r10 = r31
            r11 = r32
            java.lang.String r0 = "read"
            java.lang.String r1 = "syncdirection"
            java.lang.String r2 = "syncaction"
            java.lang.String r3 = TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleObjectMMSCloudSearch: "
            r4.append(r5)
            java.lang.String r5 = r11.correlationId
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r3, r4)
            r12 = -1
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r3 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x017a }
            java.lang.String r4 = r11.correlationId     // Catch:{ NullPointerException -> 0x017a }
            android.database.Cursor r3 = r3.searchMMsPduBufferUsingCorrelationId(r4)     // Catch:{ NullPointerException -> 0x017a }
            r14 = r3
            r3 = 4
            r4 = 0
            if (r14 == 0) goto L_0x014c
            boolean r5 = r14.moveToFirst()     // Catch:{ all -> 0x016c }
            if (r5 == 0) goto L_0x014c
            java.lang.String r5 = "_bufferdbid"
            int r5 = r14.getColumnIndexOrThrow(r5)     // Catch:{ all -> 0x016c }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x016c }
            long r8 = (long) r5     // Catch:{ all -> 0x016c }
            int r5 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x016c }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r20 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r5)     // Catch:{ all -> 0x016c }
            int r5 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x016c }
            int r5 = r14.getInt(r5)     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r19 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r5)     // Catch:{ all -> 0x016c }
            android.content.ContentValues r5 = new android.content.ContentValues     // Catch:{ all -> 0x016c }
            r5.<init>()     // Catch:{ all -> 0x016c }
            r7 = r5
            java.lang.String r5 = "lastmodseq"
            java.lang.Long r6 = r11.lastModSeq     // Catch:{ all -> 0x016c }
            r7.put(r5, r6)     // Catch:{ all -> 0x016c }
            java.lang.String r5 = "res_url"
            java.net.URL r6 = r11.resourceURL     // Catch:{ all -> 0x016c }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x016c }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x016c }
            r7.put(r5, r6)     // Catch:{ all -> 0x016c }
            java.lang.String r5 = "parentfolder"
            java.net.URL r6 = r11.parentFolder     // Catch:{ all -> 0x016c }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x016c }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x016c }
            r7.put(r5, r6)     // Catch:{ all -> 0x016c }
            java.lang.String r5 = "path"
            java.lang.String r6 = r11.path     // Catch:{ all -> 0x016c }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x016c }
            java.lang.String r6 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r6)     // Catch:{ all -> 0x016c }
            r7.put(r5, r6)     // Catch:{ all -> 0x016c }
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r5 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.Done     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.None     // Catch:{ all -> 0x016c }
            r5.<init>(r6, r15)     // Catch:{ all -> 0x016c }
            r5.mIsChanged = r4     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r6 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x016c }
            boolean r4 = r4.equals(r6)     // Catch:{ all -> 0x016c }
            r6 = 1
            if (r4 == 0) goto L_0x00bb
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice     // Catch:{ all -> 0x016c }
            r5.setIsChangedActionAndDirection(r6, r0, r4)     // Catch:{ all -> 0x016c }
            r0 = r5
            goto L_0x00fb
        L_0x00bb:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = r11.mFlag     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x016c }
            boolean r4 = r4.equals(r15)     // Catch:{ all -> 0x016c }
            if (r4 == 0) goto L_0x00e6
            int r4 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x016c }
            int r4 = r14.getInt(r4)     // Catch:{ all -> 0x016c }
            if (r4 != 0) goto L_0x00d6
            java.lang.Integer r4 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x016c }
            r7.put(r0, r4)     // Catch:{ all -> 0x016c }
        L_0x00d6:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r15 = r10.mScheduleRule     // Catch:{ all -> 0x016c }
            int r0 = r10.mDbTableContractIndex     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r21 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x016c }
            r16 = r0
            r17 = r8
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r15.getSetFlagsForCldOperation(r16, r17, r19, r20, r21)     // Catch:{ all -> 0x016c }
            r5 = r0
            goto L_0x00fb
        L_0x00e6:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r0 = r10.mScheduleRule     // Catch:{ all -> 0x016c }
            int r4 = r10.mDbTableContractIndex     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r27 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x016c }
            r21 = r0
            r22 = r4
            r23 = r8
            r25 = r19
            r26 = r20
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r0 = r21.getSetFlagsForCldOperation(r22, r23, r25, r26, r27)     // Catch:{ all -> 0x016c }
            r5 = r0
        L_0x00fb:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r4 = r10.mSummaryDB     // Catch:{ all -> 0x016c }
            r4.insertSummaryDbUsingObjectIfNonExist(r11, r3)     // Catch:{ all -> 0x016c }
            boolean r3 = r0.mIsChanged     // Catch:{ all -> 0x016c }
            if (r3 == 0) goto L_0x013e
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r0.mAction     // Catch:{ all -> 0x016c }
            int r3 = r3.getId()     // Catch:{ all -> 0x016c }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x016c }
            r7.put(r2, r3)     // Catch:{ all -> 0x016c }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r0.mDirection     // Catch:{ all -> 0x016c }
            int r2 = r2.getId()     // Catch:{ all -> 0x016c }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x016c }
            r7.put(r1, r2)     // Catch:{ all -> 0x016c }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x016c }
            r10.updateQueryTable(r7, r8, r1)     // Catch:{ all -> 0x016c }
            r5 = 4
            r6 = 0
            boolean r15 = r11.mIsGoforwardSync     // Catch:{ all -> 0x016c }
            java.lang.String r3 = r11.mLine     // Catch:{ all -> 0x016c }
            r16 = 0
            r1 = r31
            r2 = r0
            r17 = r3
            r3 = r8
            r28 = r7
            r7 = r15
            r29 = r8
            r8 = r17
            r9 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x016c }
            goto L_0x014b
        L_0x013e:
            r28 = r7
            r29 = r8
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x016c }
            r4 = r28
            r2 = r29
            r10.updateQueryTable(r4, r2, r1)     // Catch:{ all -> 0x016c }
        L_0x014b:
            goto L_0x0162
        L_0x014c:
            java.lang.String r0 = TAG     // Catch:{ all -> 0x016c }
            java.lang.String r1 = "handleObjectMMSCloudSearch: MMS not found"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x016c }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x016c }
            r1 = 0
            long r0 = r0.insertMMSUsingObject(r11, r4, r1)     // Catch:{ all -> 0x016c }
            r1 = r0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r10.mSummaryDB     // Catch:{ all -> 0x0168 }
            r0.insertSummaryDbUsingObjectIfNonExist(r11, r3)     // Catch:{ all -> 0x0168 }
            r12 = r1
        L_0x0162:
            if (r14 == 0) goto L_0x0167
            r14.close()     // Catch:{ NullPointerException -> 0x017a }
        L_0x0167:
            goto L_0x017e
        L_0x0168:
            r0 = move-exception
            r12 = r1
            r1 = r0
            goto L_0x016e
        L_0x016c:
            r0 = move-exception
            r1 = r0
        L_0x016e:
            if (r14 == 0) goto L_0x0179
            r14.close()     // Catch:{ all -> 0x0174 }
            goto L_0x0179
        L_0x0174:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x017a }
        L_0x0179:
            throw r1     // Catch:{ NullPointerException -> 0x017a }
        L_0x017a:
            r0 = move-exception
            r0.printStackTrace()
        L_0x017e:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.handleObjectMMSCloudSearch(com.sec.internal.ims.cmstore.params.ParamOMAObject):long");
    }

    /* Debug info: failed to restart local var, previous not found, register: 24 */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x018e A[SYNTHETIC, Splitter:B:46:0x018e] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long handleNormalSyncObjectMmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject r25, boolean r26) {
        /*
            r24 = this;
            r10 = r24
            r11 = r25
            java.lang.String r0 = "syncdirection"
            java.lang.String r1 = "syncaction"
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleNormalSyncObjectMmsDownload: "
            r3.append(r4)
            r3.append(r11)
            java.lang.String r3 = r3.toString()
            android.util.Log.d(r2, r3)
            r12 = -1
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r2 = r10.mBufferDbQuery     // Catch:{ NullPointerException -> 0x0198 }
            java.lang.String r3 = r11.correlationId     // Catch:{ NullPointerException -> 0x0198 }
            android.database.Cursor r2 = r2.searchMMsPduBufferUsingCorrelationId(r3)     // Catch:{ NullPointerException -> 0x0198 }
            r14 = r2
            java.net.URL r2 = r11.resourceURL     // Catch:{ all -> 0x018a }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r8 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r2)     // Catch:{ all -> 0x018a }
            if (r14 == 0) goto L_0x0128
            boolean r4 = r14.moveToFirst()     // Catch:{ all -> 0x018a }
            if (r4 == 0) goto L_0x0128
            java.lang.String r4 = "_bufferdbid"
            int r4 = r14.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x018a }
            int r4 = r14.getInt(r4)     // Catch:{ all -> 0x018a }
            long r6 = (long) r4     // Catch:{ all -> 0x018a }
            java.lang.String r4 = "_id"
            int r4 = r14.getColumnIndexOrThrow(r4)     // Catch:{ all -> 0x018a }
            int r4 = r14.getInt(r4)     // Catch:{ all -> 0x018a }
            long r4 = (long) r4     // Catch:{ all -> 0x018a }
            int r9 = r14.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x018a }
            int r9 = r14.getInt(r9)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r9 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.valueOf((int) r9)     // Catch:{ all -> 0x018a }
            int r15 = r14.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x018a }
            int r15 = r14.getInt(r15)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r15 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.valueOf((int) r15)     // Catch:{ all -> 0x018a }
            android.content.ContentValues r16 = new android.content.ContentValues     // Catch:{ all -> 0x018a }
            r16.<init>()     // Catch:{ all -> 0x018a }
            r22 = r16
            java.lang.String r2 = "lastmodseq"
            java.lang.Long r3 = r11.lastModSeq     // Catch:{ all -> 0x018a }
            r16 = r9
            r9 = r22
            r9.put(r2, r3)     // Catch:{ all -> 0x018a }
            java.lang.String r2 = "res_url"
            java.net.URL r3 = r11.resourceURL     // Catch:{ all -> 0x018a }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r3)     // Catch:{ all -> 0x018a }
            r9.put(r2, r3)     // Catch:{ all -> 0x018a }
            java.lang.String r2 = "parentfolder"
            java.net.URL r3 = r11.parentFolder     // Catch:{ all -> 0x018a }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r3)     // Catch:{ all -> 0x018a }
            r9.put(r2, r3)     // Catch:{ all -> 0x018a }
            java.lang.String r2 = "path"
            java.lang.String r3 = r11.path     // Catch:{ all -> 0x018a }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x018a }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.decodeUrlFromServer(r3)     // Catch:{ all -> 0x018a }
            r9.put(r2, r3)     // Catch:{ all -> 0x018a }
            java.lang.String r2 = "read"
            int r2 = r14.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x018a }
            int r2 = r14.getInt(r2)     // Catch:{ all -> 0x018a }
            r3 = 1
            if (r2 != r3) goto L_0x00c7
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud     // Catch:{ all -> 0x018a }
            r15 = r3
            r22 = r2
            r23 = r15
            goto L_0x00cb
        L_0x00c7:
            r23 = r15
            r22 = r16
        L_0x00cb:
            com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule r15 = r10.mScheduleRule     // Catch:{ all -> 0x018a }
            int r2 = r10.mDbTableContractIndex     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = r11.mFlag     // Catch:{ all -> 0x018a }
            r16 = r2
            r17 = r6
            r19 = r23
            r20 = r22
            r21 = r3
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = r15.getSetFlagsForCldOperation(r16, r17, r19, r20, r21)     // Catch:{ all -> 0x018a }
            r15 = r2
            boolean r2 = r15.mIsChanged     // Catch:{ all -> 0x018a }
            if (r2 == 0) goto L_0x00fe
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r2 = r15.mDirection     // Catch:{ all -> 0x018a }
            int r2 = r2.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ all -> 0x018a }
            r9.put(r0, r2)     // Catch:{ all -> 0x018a }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = r15.mAction     // Catch:{ all -> 0x018a }
            int r0 = r0.getId()     // Catch:{ all -> 0x018a }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x018a }
            r9.put(r1, r0)     // Catch:{ all -> 0x018a }
        L_0x00fe:
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r10.updateQueryTable(r9, r6, r0)     // Catch:{ all -> 0x018a }
            r0 = 0
            int r0 = (r4 > r0 ? 1 : (r4 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0122
            r0 = 4
            r16 = 0
            r17 = 0
            r1 = r24
            r2 = r15
            r18 = r4
            r3 = r6
            r5 = r0
            r20 = r6
            r6 = r16
            r7 = r26
            r0 = r9
            r9 = r17
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x018a }
            goto L_0x0127
        L_0x0122:
            r18 = r4
            r20 = r6
            r0 = r9
        L_0x0127:
            goto L_0x0180
        L_0x0128:
            java.lang.String r0 = TAG     // Catch:{ all -> 0x018a }
            java.lang.String r1 = "handleObjectMMSCloudSearch: MMS not found"
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ all -> 0x018a }
            r1 = 0
            r2 = 0
            long r3 = r0.insertMMSUsingObject(r11, r1, r2)     // Catch:{ all -> 0x018a }
            java.lang.String r0 = "OUT"
            java.lang.String r1 = r11.DIRECTION     // Catch:{ all -> 0x0186 }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ all -> 0x0186 }
            if (r0 != 0) goto L_0x015e
            java.lang.String r0 = "IN"
            java.lang.String r1 = r11.DIRECTION     // Catch:{ all -> 0x0186 }
            boolean r0 = r0.equalsIgnoreCase(r1)     // Catch:{ all -> 0x0186 }
            if (r0 == 0) goto L_0x017f
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r0 = com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager.getStrategy()     // Catch:{ all -> 0x0186 }
            boolean r0 = r0.isSupportAtt72HoursRule()     // Catch:{ all -> 0x0186 }
            if (r0 == 0) goto L_0x017f
            java.lang.String r0 = r11.DATE     // Catch:{ all -> 0x0186 }
            boolean r0 = com.sec.internal.ims.cmstore.utils.Util.isOver72Hours(r0)     // Catch:{ all -> 0x0186 }
            if (r0 == 0) goto L_0x017f
        L_0x015e:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r0 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x0186 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = r11.mFlag     // Catch:{ all -> 0x0186 }
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x0186 }
            if (r0 != 0) goto L_0x017f
            boolean r0 = r11.mIsGoforwardSync     // Catch:{ all -> 0x0186 }
            if (r0 != 0) goto L_0x017f
            com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r0 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParamList     // Catch:{ all -> 0x0186 }
            r0.<init>()     // Catch:{ all -> 0x0186 }
            boolean r6 = r11.mIsGoforwardSync     // Catch:{ all -> 0x0186 }
            r1 = r24
            r2 = r0
            r5 = r8
            r1.addMmsPartDownloadList(r2, r3, r5, r6)     // Catch:{ all -> 0x0186 }
            com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener r1 = r10.mDeviceDataChangeListener     // Catch:{ all -> 0x0186 }
            r1.sendDeviceNormalSyncDownload(r0)     // Catch:{ all -> 0x0186 }
        L_0x017f:
            r12 = r3
        L_0x0180:
            if (r14 == 0) goto L_0x0185
            r14.close()     // Catch:{ NullPointerException -> 0x0198 }
        L_0x0185:
            goto L_0x01a2
        L_0x0186:
            r0 = move-exception
            r1 = r0
            r12 = r3
            goto L_0x018c
        L_0x018a:
            r0 = move-exception
            r1 = r0
        L_0x018c:
            if (r14 == 0) goto L_0x0197
            r14.close()     // Catch:{ all -> 0x0192 }
            goto L_0x0197
        L_0x0192:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ NullPointerException -> 0x0198 }
        L_0x0197:
            throw r1     // Catch:{ NullPointerException -> 0x0198 }
        L_0x0198:
            r0 = move-exception
            java.lang.String r1 = TAG
            java.lang.String r2 = r0.toString()
            android.util.Log.e(r1, r2)
        L_0x01a2:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.handleNormalSyncObjectMmsDownload(com.sec.internal.ims.cmstore.params.ParamOMAObject, boolean):long");
    }

    public void addMmsPartDownloadList(BufferDBChangeParamList list, long mmsPduBufferId, String linenum, boolean isGoforwardSync) {
        Cursor part = queryOneMmsUndownloadedParts(mmsPduBufferId);
        if (part != null) {
            try {
                if (part.moveToFirst()) {
                    do {
                        list.mChangelst.add(new BufferDBChangeParam(6, part.getLong(part.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), isGoforwardSync, linenum));
                    } while (part.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (part != null) {
            part.close();
            return;
        }
        return;
        throw th;
    }

    public void onNmsEventDeletedObjMmsBufferDbAvailableUsingCorrId(Cursor cs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjMMSBufferDbAvailable(cs, objt, mIsGoforwardSync, true);
    }

    public void onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(Cursor cs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjMMSBufferDbAvailable(cs, objt, mIsGoforwardSync);
    }

    public void onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(Cursor mmsCs, DeletedObject objt, boolean mIsGoforwardSync) {
        onNmsEventDeletedObjMMSBufferDbAvailable(mmsCs, objt, mIsGoforwardSync, false);
    }

    public void onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(Cursor mmsCs, ChangedObject objt, boolean mIsGoforwardSync) {
        onNmsEventChangedObjMMSBufferDbAvailable(mmsCs, objt, mIsGoforwardSync);
    }

    private void onNmsEventChangedObjMMSBufferDbAvailable(Cursor cs, ChangedObject objt, boolean mIsGoforwardSync) {
        Cursor cursor = cs;
        ChangedObject changedObject = objt;
        long bufferDbId = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, changedObject.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(changedObject.resourceURL.toString()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(changedObject.parentFolder.toString()));
        CloudMessageBufferDBConstants.ActionStatusFlag cldAction = this.mBufferDbQuery.getCloudActionPerFlag(changedObject.flags);
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(cldAction)) {
            cv.put("read", 1);
        }
        ContentValues cv2 = cv;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbId, direction, action, cldAction);
        if (flagSet.mIsChanged) {
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv2, bufferDbId, this.mBufferDbQuery);
        if (_id > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 4, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
    }

    private void onNmsEventDeletedObjMMSBufferDbAvailable(Cursor cs, DeletedObject objt, boolean mIsGoforwardSync, boolean isUrl) {
        Cursor cursor = cs;
        DeletedObject deletedObject = objt;
        long bufferDbId = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long _id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, Long.valueOf(deletedObject.lastModSeq));
        if (isUrl) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(deletedObject.resourceURL.toString()));
        }
        ContentValues cv2 = cv;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbId, direction, action, CloudMessageBufferDBConstants.ActionStatusFlag.Delete);
        if (flagSet.mIsChanged) {
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv2, bufferDbId, this.mBufferDbQuery);
        if (_id > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 4, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
    }

    public Cursor queryOneMmsUndownloadedParts(long bufferDbId) {
        String str = TAG;
        Log.i(str, "queryIfMmsPartsDownloadComplete: " + bufferDbId);
        return this.mBufferDbQuery.queryUndownloadedPart(bufferDbId);
    }

    /* Debug info: failed to restart local var, previous not found, register: 19 */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00ed A[Catch:{ all -> 0x0114, all -> 0x011c, IOException -> 0x0128 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x010f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMmsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r20, boolean r21) {
        /*
            r19 = this;
            r10 = r19
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "MMS PayLoad downloaded: "
            r1.append(r2)
            r11 = r20
            r1.append(r11)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r0 = r10.mBufferDbQuery     // Catch:{ IOException -> 0x0128 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r20.getBufferDBChangeParam()     // Catch:{ IOException -> 0x0128 }
            long r1 = r1.mRowId     // Catch:{ IOException -> 0x0128 }
            r3 = 6
            android.database.Cursor r0 = r0.queryTablewithBufferDbId(r3, r1)     // Catch:{ IOException -> 0x0128 }
            r12 = r0
            if (r12 == 0) goto L_0x0122
            boolean r0 = r12.moveToFirst()     // Catch:{ all -> 0x0114 }
            if (r0 == 0) goto L_0x0122
            java.lang.String r0 = "cl"
            int r0 = r12.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0114 }
            java.lang.String r0 = r12.getString(r0)     // Catch:{ all -> 0x0114 }
            java.lang.String r1 = "mid"
            int r1 = r12.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x0114 }
            int r1 = r12.getInt(r1)     // Catch:{ all -> 0x0114 }
            r13 = r1
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r1 = r20.getBufferDBChangeParam()     // Catch:{ all -> 0x0114 }
            java.lang.String r8 = r1.mLine     // Catch:{ all -> 0x0114 }
            android.content.ContentValues r1 = new android.content.ContentValues     // Catch:{ all -> 0x0114 }
            r1.<init>()     // Catch:{ all -> 0x0114 }
            r14 = r1
            java.lang.String r1 = "_bufferdbid= ?"
            r15 = r1
            r1 = 1
            java.lang.String[] r2 = new java.lang.String[r1]     // Catch:{ all -> 0x0114 }
            r4 = 0
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r5 = r20.getBufferDBChangeParam()     // Catch:{ all -> 0x0114 }
            long r5 = r5.mRowId     // Catch:{ all -> 0x0114 }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x0114 }
            r2[r4] = r5     // Catch:{ all -> 0x0114 }
            r9 = r2
            java.lang.String r2 = "ct"
            int r2 = r12.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x0114 }
            java.lang.String r2 = r12.getString(r2)     // Catch:{ all -> 0x0114 }
            r7 = r2
            java.lang.String r2 = "application/smil"
            boolean r2 = r2.equalsIgnoreCase(r7)     // Catch:{ all -> 0x0114 }
            if (r2 != 0) goto L_0x00ab
            java.lang.String r2 = "text/plain"
            boolean r2 = r2.equalsIgnoreCase(r7)     // Catch:{ all -> 0x0114 }
            if (r2 == 0) goto L_0x0082
            goto L_0x00ab
        L_0x0082:
            android.content.Context r2 = r10.mContext     // Catch:{ all -> 0x0114 }
            java.lang.String r1 = com.sec.internal.ims.cmstore.utils.Util.generateUniqueFilePath(r2, r0, r1)     // Catch:{ all -> 0x0114 }
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0114 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0114 }
            r4.<init>()     // Catch:{ all -> 0x0114 }
            java.lang.String r5 = "generated file path: "
            r4.append(r5)     // Catch:{ all -> 0x0114 }
            r4.append(r1)     // Catch:{ all -> 0x0114 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0114 }
            android.util.Log.d(r2, r4)     // Catch:{ all -> 0x0114 }
            byte[] r2 = r20.getData()     // Catch:{ all -> 0x0114 }
            com.sec.internal.ims.cmstore.utils.Util.saveFiletoPath(r2, r1)     // Catch:{ all -> 0x0114 }
            java.lang.String r2 = "_data"
            r14.put(r2, r1)     // Catch:{ all -> 0x0114 }
            goto L_0x00df
        L_0x00ab:
            byte[] r1 = r20.getData()     // Catch:{ all -> 0x0114 }
            java.lang.String r2 = "text"
            if (r1 == 0) goto L_0x00da
            java.lang.String r1 = new java.lang.String     // Catch:{ all -> 0x0114 }
            byte[] r4 = r20.getData()     // Catch:{ all -> 0x0114 }
            java.nio.charset.Charset r5 = java.nio.charset.StandardCharsets.UTF_8     // Catch:{ all -> 0x0114 }
            r1.<init>(r4, r5)     // Catch:{ all -> 0x0114 }
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0114 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0114 }
            r5.<init>()     // Catch:{ all -> 0x0114 }
            java.lang.String r6 = "part UTF8 text data to write: "
            r5.append(r6)     // Catch:{ all -> 0x0114 }
            r5.append(r1)     // Catch:{ all -> 0x0114 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0114 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x0114 }
            r14.put(r2, r1)     // Catch:{ all -> 0x0114 }
            goto L_0x00df
        L_0x00da:
            java.lang.String r1 = ""
            r14.put(r2, r1)     // Catch:{ all -> 0x0114 }
        L_0x00df:
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x0114 }
            r1.updateTable(r3, r14, r15, r9)     // Catch:{ all -> 0x0114 }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x0114 }
            long r2 = (long) r13     // Catch:{ all -> 0x0114 }
            boolean r1 = r1.queryIfMmsPartsDownloadComplete(r2)     // Catch:{ all -> 0x0114 }
            if (r1 == 0) goto L_0x010f
            com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet r2 = new com.sec.internal.ims.cmstore.params.ParamSyncFlagsSet     // Catch:{ all -> 0x0114 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$DirectionFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice     // Catch:{ all -> 0x0114 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Insert     // Catch:{ all -> 0x0114 }
            r2.<init>(r1, r3)     // Catch:{ all -> 0x0114 }
            com.sec.internal.ims.cmstore.querybuilders.MmsQueryBuilder r1 = r10.mBufferDbQuery     // Catch:{ all -> 0x0114 }
            long r3 = (long) r13     // Catch:{ all -> 0x0114 }
            r1.updateMMSUpdateingDevice(r3)     // Catch:{ all -> 0x0114 }
            long r3 = (long) r13     // Catch:{ all -> 0x0114 }
            r5 = 4
            r6 = 0
            r16 = 0
            r1 = r19
            r17 = r7
            r7 = r21
            r18 = r9
            r9 = r16
            r1.handleOutPutParamSyncFlagSet(r2, r3, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0114 }
            goto L_0x0122
        L_0x010f:
            r17 = r7
            r18 = r9
            goto L_0x0122
        L_0x0114:
            r0 = move-exception
            r1 = r0
            if (r12 == 0) goto L_0x0121
            r12.close()     // Catch:{ all -> 0x011c }
            goto L_0x0121
        L_0x011c:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)     // Catch:{ IOException -> 0x0128 }
        L_0x0121:
            throw r1     // Catch:{ IOException -> 0x0128 }
        L_0x0122:
            if (r12 == 0) goto L_0x0127
            r12.close()     // Catch:{ IOException -> 0x0128 }
        L_0x0127:
            goto L_0x012c
        L_0x0128:
            r0 = move-exception
            r0.printStackTrace()
        L_0x012c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.onMmsPayloadDownloaded(com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB, boolean):void");
    }

    public void handleExistingBufferForDeviceLegacyUpdate(Cursor cs, DeviceLegacyUpdateParam para, boolean mIsGoforwardSync, BufferDBChangeParamList changelist) {
        Cursor cursor = cs;
        DeviceLegacyUpdateParam deviceLegacyUpdateParam = para;
        String str = TAG;
        Log.i(str, "handleExistingBufferForDeviceLegacyUpdate: " + deviceLegacyUpdateParam);
        ContentValues cv = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        String line = cursor.getString(cursor.getColumnIndexOrThrow("linenum"));
        long bufferDbId = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForMsgOperation(this.mDbTableContractIndex, bufferDbId, direction, action, deviceLegacyUpdateParam.mOperation);
        if (flagSet.mIsChanged) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(flagSet.mDirection.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Read.equals(deviceLegacyUpdateParam.mOperation)) {
            cv.put("read", 1);
        }
        String[] selectionArgs = {String.valueOf(bufferDbId)};
        int _id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        if (deviceLegacyUpdateParam.mRowId != ((long) _id)) {
            cv.put("_id", Long.valueOf(deviceLegacyUpdateParam.mRowId));
        }
        this.mBufferDbQuery.updateTable(deviceLegacyUpdateParam.mTableindex, cv, "_bufferdbid=?", selectionArgs);
        if (flagSet.mIsChanged) {
            int i = _id;
            String[] strArr = selectionArgs;
            Object obj = "_bufferdbid=?";
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, deviceLegacyUpdateParam.mTableindex, false, mIsGoforwardSync, line, changelist);
            return;
        }
        String[] strArr2 = selectionArgs;
        Object obj2 = "_bufferdbid=?";
        ParamSyncFlagsSet paramSyncFlagsSet2 = flagSet;
    }

    public void handleNonExistingBufferForDeviceLegacyUpdate(DeviceLegacyUpdateParam para) {
        String str = TAG;
        Log.i(str, "handleNonExistingBufferForDeviceLegacyUpdate: " + para);
        ContentValues cv = new ContentValues();
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        cv.put("linenum", para.mLine);
        if (para.mTableindex == 4) {
            Cursor mmsCs = this.mBufferDbQuery.queryMMSPduFromTelephonyDbUseID(Long.valueOf(para.mRowId).longValue());
            if (mmsCs != null) {
                try {
                    if (mmsCs.moveToFirst()) {
                        this.mBufferDbQuery.insertToMMSPDUBufferDB(mmsCs, cv, false);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (mmsCs != null) {
                mmsCs.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppFetchBuffer(Cursor cs, int type) {
        String str = TAG;
        Log.i(str, "notifyMsgAppFetchBuffer: " + type);
        if (type == 4) {
            JsonArray jsonArrayRowIdsMMS = new JsonArray();
            do {
                int bufferDBid = cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                JsonObject jsobjct = new JsonObject();
                jsobjct.addProperty("id", String.valueOf(bufferDBid));
                jsonArrayRowIdsMMS.add(jsobjct);
                if (jsonArrayRowIdsMMS.size() == this.mMaxNumMsgsNotifyAppInIntent) {
                    String str2 = TAG;
                    Log.i(str2, "notify message app: MMS: " + jsonArrayRowIdsMMS.toString());
                    this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "MMS", jsonArrayRowIdsMMS.toString());
                    jsonArrayRowIdsMMS = new JsonArray();
                }
            } while (cs.moveToNext() != 0);
            if (jsonArrayRowIdsMMS.size() > 0) {
                String str3 = TAG;
                Log.d(str3, "notify message app: MMS: " + jsonArrayRowIdsMMS.toString());
                this.mCallbackMsgApp.notifyCloudMessageUpdate(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "MMS", jsonArrayRowIdsMMS.toString());
            }
        }
    }

    public Cursor queryToDeviceUnDownloadedMms(String linenum) {
        return this.mBufferDbQuery.queryToDeviceUnDownloadedMms(linenum);
    }

    public Cursor queryToCloudUnsyncedMms() {
        return this.mBufferDbQuery.queryToCloudUnsyncedMms();
    }

    public Cursor queryToDeviceUnsyncedMms() {
        return this.mBufferDbQuery.queryToDeviceUnsyncedMms();
    }

    public Cursor queryMMSMessagesToUpload() {
        return this.mBufferDbQuery.queryMMSMessagesToUpload();
    }

    public Cursor queryMMSBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.queryMMSBufferDBwithResUrl(url);
    }

    public int deleteMMSBufferDBwithResUrl(String url) {
        return this.mBufferDbQuery.deleteMMSBufferDBwithResUrl(url);
    }

    public Cursor searchMMsPduBufferUsingCorrelationId(String corrId) {
        return this.mBufferDbQuery.searchMMsPduBufferUsingCorrelationId(corrId);
    }

    public Cursor queryMMSMessagesBySycnDirection(int tableIndex, String syncDirection) {
        return this.mBufferDbQuery.queryMessageBySyncDirection(tableIndex, syncDirection);
    }

    public Cursor queryAllMMSPduFromTelephonyDb() {
        return this.mBufferDbQuery.queryAllMMSPduFromTelephonyDb();
    }

    public Cursor queryDeltaMMSPduFromTelephonyDb() {
        return this.mBufferDbQuery.queryDeltaMMSPduFromTelephonyDb();
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    public void syncReadMmsFromTelephony() {
        Cursor cssms;
        ArrayList<String> updatelist = new ArrayList<>();
        try {
            cssms = this.mBufferDbQuery.queryReadMmsfromTelephony();
            if (cssms != null) {
                if (cssms.moveToFirst()) {
                    updatelist.add(cssms.getString(cssms.getColumnIndex("_id")));
                }
            }
            if (cssms != null) {
                cssms.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        for (int i = 0; i < updatelist.size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud.getId()));
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Update.getId()));
            cv.put("read", 1);
            this.mBufferDbQuery.updateTable(this.mDbTableContractIndex, cv, "_id=? AND read=? AND syncaction<>? AND syncaction<>?", new String[]{updatelist.get(i), String.valueOf(0), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Delete.getId()), String.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId())});
        }
        return;
        throw th;
    }

    public void insertToMMSPDUBufferDB(Cursor cursor, ContentValues cvFlags, boolean isGoForwardSync) {
        this.mBufferDbQuery.insertToMMSPDUBufferDB(cursor, cvFlags, isGoForwardSync);
    }

    public Cursor queryMMSPduFromTelephonyDbUseID(long mmsId) {
        return this.mBufferDbQuery.queryMMSPduFromTelephonyDbUseID(mmsId);
    }

    private void handleDeviceLegacyUpdateParam(DeviceLegacyUpdateParam para, boolean mIsGoforwardSync, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "handleDeviceLegacyUpdateParam: " + para);
        Cursor cs = null;
        if (para.mTableindex == 4 && !CloudMessageBufferDBConstants.MsgOperationFlag.Sending.equals(para.mOperation) && !CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.equals(para.mOperation) && para.mMId != null) {
            try {
                switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[para.mOperation.ordinal()]) {
                    case 1:
                        cs = this.mBufferDbQuery.searchMMSPduBufferUsingMidorTrId(para.mMId, para.mTRId);
                        break;
                    case 2:
                        cs = this.mBufferDbQuery.searchMMSPduBufferUsingMidorTrId(para.mMId, para.mTRId);
                        break;
                    case 3:
                        cs = this.mBufferDbQuery.searchMMSPduBufferUsingRowId(para.mRowId);
                        break;
                    case 4:
                        cs = this.mBufferDbQuery.searchMMSPduBufferUsingRowId(para.mRowId);
                        break;
                    case 5:
                    case 6:
                    case 7:
                        if (cs == null) {
                            return;
                        }
                        return;
                }
                if (cs == null || !cs.moveToFirst()) {
                    handleNonExistingBufferForDeviceLegacyUpdate(para);
                } else {
                    handleExistingBufferForDeviceLegacyUpdate(cs, para, mIsGoforwardSync, changelist);
                }
                if (cs != null) {
                    cs.close();
                }
            } finally {
                if (cs != null) {
                    cs.close();
                }
            }
        }
    }

    /* renamed from: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag;

        static {
            int[] iArr = new int[CloudMessageBufferDBConstants.MsgOperationFlag.values().length];
            $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag = iArr;
            try {
                iArr[CloudMessageBufferDBConstants.MsgOperationFlag.Received.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sent.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Read.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Delete.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.SendFail.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Receiving.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$cmstore$CloudMessageBufferDBConstants$MsgOperationFlag[CloudMessageBufferDBConstants.MsgOperationFlag.Sending.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    public void onUpdateFromDeviceMsgAppFetch(DeviceMsgAppFetchUpdateParam para, boolean mIsGoforwardSync) {
        onUpdateFromDeviceMsgAppFetch(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void onUpdateFromDeviceMsgAppFetchFailed(DeviceMsgAppFetchUpdateParam para) {
        this.mBufferDbQuery.updateAppFetchingFailed(para.mTableindex, para.mBufferRowId);
    }

    public void onCloudUpdateFlagSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        onCloudUpdateFlagSuccess(para, mIsGoforwardSync, this.mBufferDbQuery);
    }

    public void onCloudUploadSuccess(ParamOMAresponseforBufDB para, boolean mIsGoforwardSync) {
        if (para.getReference() != null) {
            handleCloudUploadSuccess(para, mIsGoforwardSync, this.mBufferDbQuery, 4);
        }
    }

    public void cleanAllBufferDB() {
        this.mBufferDbQuery.cleanAllBufferDB();
    }

    public void onAppOperationReceived(ParamAppJsonValue param, BufferDBChangeParamList changelist) {
        String str = TAG;
        Log.i(str, "onAppOperationReceived: " + param);
        if (CloudMessageBufferDBConstants.MsgOperationFlag.Delete.equals(param.mOperation)) {
            handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(param.mDataContractType, param.mOperation, param.mRowId, (String) null, param.mCorrelationId, param.mCorrelationId, param.mLine), false, changelist);
            return;
        }
        Cursor csmmspdu = queryMMSPduFromTelephonyDbUseID((long) param.mRowId);
        if (csmmspdu != null) {
            try {
                if (csmmspdu.moveToFirst()) {
                    String tr_id = csmmspdu.getString(csmmspdu.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID));
                    String m_id = param.mCorrelationId;
                    if (TextUtils.isEmpty(param.mCorrelationId)) {
                        m_id = csmmspdu.getString(csmmspdu.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
                    }
                    handleDeviceLegacyUpdateParam(new DeviceLegacyUpdateParam(param.mDataContractType, param.mOperation, param.mRowId, (String) null, m_id, tr_id, param.mLine), false, changelist);
                    Log.d(TAG, "onAppOperationReceived: no mms pdu exists");
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (csmmspdu != null) {
            csmmspdu.close();
            return;
        }
        return;
        throw th;
    }

    public void onNotificationReceived(ParamNmsNotificationList notification) {
        Log.i(TAG, "onNotificationReceived: " + notification);
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        if (!(notification.mNmsEventList == null || notification.mNmsEventList.nmsEvent == null)) {
            for (NmsEvent event : notification.mNmsEventList.nmsEvent) {
                if (event.changedObject != null) {
                    handleCloudNotifyChangedObj(event.changedObject, downloadlist);
                }
                if (event.deletedObject != null) {
                    handleCloudNotifyDeletedObj(event.deletedObject);
                }
            }
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 13 */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00a2 A[SYNTHETIC, Splitter:B:25:0x00a2] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:48:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r14, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r15) {
        /*
            r13 = this;
            r6 = 0
            java.net.URL r0 = r14.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r7 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r13.mSummaryDB
            java.net.URL r1 = r14.resourceURL
            java.lang.String r1 = r1.toString()
            android.database.Cursor r8 = r0.querySummaryDBwithResUrl(r1)
            if (r8 == 0) goto L_0x0045
            boolean r0 = r8.moveToFirst()     // Catch:{ all -> 0x00bf }
            if (r0 == 0) goto L_0x0045
            java.lang.String r0 = "syncaction"
            int r0 = r8.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x00bf }
            int r0 = r8.getInt(r0)     // Catch:{ all -> 0x00bf }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x00bf }
            int r1 = r1.getId()     // Catch:{ all -> 0x00bf }
            if (r0 != r1) goto L_0x0040
            java.lang.String r1 = TAG     // Catch:{ all -> 0x00bf }
            java.lang.String r2 = "this is a deleted object"
            android.util.Log.d(r1, r2)     // Catch:{ all -> 0x00bf }
            if (r8 == 0) goto L_0x003f
            r8.close()
        L_0x003f:
            return
        L_0x0040:
            r13.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r8, r14)     // Catch:{ all -> 0x00bf }
            goto L_0x00b9
        L_0x0045:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r13.mSummaryDB     // Catch:{ all -> 0x00bf }
            r1 = 4
            long r2 = r0.insertNmsEventChangedObjToSummaryDB(r14, r1)     // Catch:{ all -> 0x00bf }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00bf }
            int r0 = r0.getId()     // Catch:{ all -> 0x00bf }
            r9 = r0
            java.lang.String r0 = TAG     // Catch:{ all -> 0x00bf }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00bf }
            r1.<init>()     // Catch:{ all -> 0x00bf }
            java.lang.String r4 = "check initial sync status ::"
            r1.append(r4)     // Catch:{ all -> 0x00bf }
            r1.append(r9)     // Catch:{ all -> 0x00bf }
            java.lang.String r4 = ", correlationId:"
            r1.append(r4)     // Catch:{ all -> 0x00bf }
            java.lang.String r4 = r14.correlationId     // Catch:{ all -> 0x00bf }
            r1.append(r4)     // Catch:{ all -> 0x00bf }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00bf }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00bf }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00bf }
            int r0 = r0.getId()     // Catch:{ all -> 0x00bf }
            if (r9 != r0) goto L_0x00b2
            java.lang.String r0 = r14.correlationId     // Catch:{ all -> 0x00bf }
            if (r0 == 0) goto L_0x00b9
            java.lang.String r0 = r14.correlationId     // Catch:{ all -> 0x00bf }
            android.database.Cursor r0 = r13.searchMMsPduBufferUsingCorrelationId(r0)     // Catch:{ all -> 0x00bf }
            r10 = r0
            if (r10 == 0) goto L_0x0092
            boolean r0 = r10.moveToFirst()     // Catch:{ all -> 0x00a6 }
            if (r0 == 0) goto L_0x0092
            r13.onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(r10, r14, r6)     // Catch:{ all -> 0x00a6 }
            goto L_0x00a0
        L_0x0092:
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r11 = r15.mChangelst     // Catch:{ all -> 0x00a6 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00a6 }
            r1 = 7
            r0 = r12
            r4 = r6
            r5 = r7
            r0.<init>(r1, r2, r4, r5)     // Catch:{ all -> 0x00a6 }
            r11.add(r12)     // Catch:{ all -> 0x00a6 }
        L_0x00a0:
            if (r10 == 0) goto L_0x00a5
            r10.close()     // Catch:{ all -> 0x00bf }
        L_0x00a5:
            goto L_0x00b9
        L_0x00a6:
            r0 = move-exception
            if (r10 == 0) goto L_0x00b1
            r10.close()     // Catch:{ all -> 0x00ad }
            goto L_0x00b1
        L_0x00ad:
            r1 = move-exception
            r0.addSuppressed(r1)     // Catch:{ all -> 0x00bf }
        L_0x00b1:
            throw r0     // Catch:{ all -> 0x00bf }
        L_0x00b2:
            java.lang.String r0 = TAG     // Catch:{ all -> 0x00bf }
            java.lang.String r1 = "initial sync not complete yet, buffer the NMS events until initial sync is finished"
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x00bf }
        L_0x00b9:
            if (r8 == 0) goto L_0x00be
            r8.close()
        L_0x00be:
            return
        L_0x00bf:
            r0 = move-exception
            if (r8 == 0) goto L_0x00ca
            r8.close()     // Catch:{ all -> 0x00c6 }
            goto L_0x00ca
        L_0x00c6:
            r1 = move-exception
            r0.addSuppressed(r1)
        L_0x00ca:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList):void");
    }

    public boolean handleCrossSearchChangedObj(ChangedObject objt, boolean mIsGoforwardSync) {
        String corrID;
        String str = TAG;
        Log.i(str, "message_id: " + objt.extendedMessage.message_id + ", correlationId: " + objt.correlationId);
        if (objt.extendedMessage.message_id != null) {
            corrID = objt.extendedMessage.message_id;
        } else if (objt.correlationId == null) {
            return false;
        } else {
            corrID = objt.correlationId;
        }
        Cursor csMms = searchMMsPduBufferUsingCorrelationId(corrID);
        if (csMms != null) {
            try {
                if (csMms.moveToFirst()) {
                    onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(csMms, objt, mIsGoforwardSync);
                    if (csMms != null) {
                        csMms.close();
                    }
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (csMms != null) {
            csMms.close();
        }
        return false;
        throw th;
    }

    public boolean handleCrossSearchObj(ParamOMAObject objt, String line, boolean mIsGoforwardSync) {
        String str = TAG;
        Log.i(str, "handleCrossSearchObj():  line: " + IMSLog.checker(line) + " objt: " + objt);
        Cursor cs = searchMMsPduBufferUsingCorrelationId(objt.correlationId);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    onCrossObjectSearchMmsAvailableUsingCorrelationId(cs, objt, line, mIsGoforwardSync);
                    if (cs != null) {
                        cs.close();
                    }
                    return true;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs == null) {
            return false;
        }
        cs.close();
        return false;
        throw th;
    }

    private void onCrossObjectSearchMmsAvailableUsingCorrelationId(Cursor mmsCs, ParamOMAObject objt, String line, boolean mIsGoforwardSync) {
        Cursor cursor = mmsCs;
        ParamOMAObject paramOMAObject = objt;
        long bufferDbId = (long) cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
        long time = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
        long id = (long) cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        long m_id = cursor.getLong(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
        String str = TAG;
        Log.d(str, "handleCrossSearchObj find bufferDB: " + paramOMAObject.correlationId + " id: " + bufferDbId + " time: " + time + " m_id: " + m_id);
        CloudMessageBufferDBConstants.ActionStatusFlag action = CloudMessageBufferDBConstants.ActionStatusFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION)));
        CloudMessageBufferDBConstants.DirectionFlag direction = CloudMessageBufferDBConstants.DirectionFlag.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION)));
        ContentValues cv = new ContentValues();
        CloudMessageBufferDBConstants.ActionStatusFlag action2 = action;
        CloudMessageBufferDBConstants.DirectionFlag direction2 = direction;
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.LASTMODSEQ, paramOMAObject.lastModSeq);
        cv.put(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL, Util.decodeUrlFromServer(paramOMAObject.resourceURL.toString()));
        if (paramOMAObject.parentFolder != null) {
            cv.put(CloudMessageProviderContract.BufferDBExtensionBase.PARENTFOLDER, Util.decodeUrlFromServer(paramOMAObject.parentFolder.toString()));
        }
        if (paramOMAObject.path != null) {
            cv.put("path", Util.decodeUrlFromServer(paramOMAObject.path.toString()));
        }
        int i = cursor.getInt(cursor.getColumnIndexOrThrow("read"));
        String str2 = CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION;
        if (i == 1) {
            CloudMessageBufferDBConstants.ActionStatusFlag action3 = CloudMessageBufferDBConstants.ActionStatusFlag.Update;
            direction2 = CloudMessageBufferDBConstants.DirectionFlag.UpdatingCloud;
            action2 = action3;
        }
        if (CloudMessageBufferDBConstants.ActionStatusFlag.Update.equals(paramOMAObject.mFlag)) {
            cv.put("read", 1);
        }
        long id2 = id;
        String str3 = str2;
        ContentValues cv2 = cv;
        long j = m_id;
        long id3 = id2;
        ParamSyncFlagsSet flagSet = this.mScheduleRule.getSetFlagsForCldOperation(this.mDbTableContractIndex, bufferDbId, direction2, action2, paramOMAObject.mFlag);
        if (flagSet.mIsChanged) {
            cv2.put(str3, Integer.valueOf(flagSet.mDirection.getId()));
            cv2.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(flagSet.mAction.getId()));
        }
        updateQueryTable(cv2, bufferDbId, this.mBufferDbQuery);
        if (id3 > 0) {
            ParamSyncFlagsSet paramSyncFlagsSet = flagSet;
            ContentValues contentValues = cv2;
            handleOutPutParamSyncFlagSet(flagSet, bufferDbId, 4, false, mIsGoforwardSync, line, (BufferDBChangeParamList) null);
            return;
        }
        ContentValues contentValues2 = cv2;
    }

    /* Debug info: failed to restart local var, previous not found, register: 6 */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:44:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r7) {
        /*
            r6 = this;
            java.net.URL r0 = r7.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r1 = r6.mSummaryDB
            java.net.URL r2 = r7.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySummaryDBwithResUrl(r2)
            if (r1 == 0) goto L_0x0043
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x00a7 }
            if (r2 == 0) goto L_0x0043
            java.lang.String r2 = "syncaction"
            int r2 = r1.getColumnIndexOrThrow(r2)     // Catch:{ all -> 0x00a7 }
            int r2 = r1.getInt(r2)     // Catch:{ all -> 0x00a7 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r3 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x00a7 }
            int r3 = r3.getId()     // Catch:{ all -> 0x00a7 }
            if (r2 != r3) goto L_0x003f
            java.lang.String r3 = TAG     // Catch:{ all -> 0x00a7 }
            java.lang.String r4 = "this is a deleted object"
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x00a7 }
            if (r1 == 0) goto L_0x003e
            r1.close()
        L_0x003e:
            return
        L_0x003f:
            r6.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r1, r7)     // Catch:{ all -> 0x00a7 }
            goto L_0x00a1
        L_0x0043:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r6.mSummaryDB     // Catch:{ all -> 0x00a7 }
            r3 = 4
            r2.insertNmsEventDeletedObjToSummaryDB(r7, r3)     // Catch:{ all -> 0x00a7 }
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r2 = r6.mMultiLineScheduler     // Catch:{ all -> 0x00a7 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.MESSAGE     // Catch:{ all -> 0x00a7 }
            int r2 = r2.getLineInitSyncStatus(r0, r3)     // Catch:{ all -> 0x00a7 }
            java.lang.String r3 = TAG     // Catch:{ all -> 0x00a7 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a7 }
            r4.<init>()     // Catch:{ all -> 0x00a7 }
            java.lang.String r5 = "check initial sync status ::"
            r4.append(r5)     // Catch:{ all -> 0x00a7 }
            r4.append(r2)     // Catch:{ all -> 0x00a7 }
            java.lang.String r5 = ", correlationId: "
            r4.append(r5)     // Catch:{ all -> 0x00a7 }
            java.lang.String r5 = r7.correlationId     // Catch:{ all -> 0x00a7 }
            r4.append(r5)     // Catch:{ all -> 0x00a7 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00a7 }
            android.util.Log.i(r3, r4)     // Catch:{ all -> 0x00a7 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r3 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x00a7 }
            int r3 = r3.getId()     // Catch:{ all -> 0x00a7 }
            if (r2 != r3) goto L_0x00a1
            java.lang.String r3 = r7.correlationId     // Catch:{ all -> 0x00a7 }
            if (r3 == 0) goto L_0x00a1
            java.lang.String r3 = r7.correlationId     // Catch:{ all -> 0x00a7 }
            android.database.Cursor r3 = r6.searchMMsPduBufferUsingCorrelationId(r3)     // Catch:{ all -> 0x00a7 }
            if (r3 == 0) goto L_0x009c
            boolean r4 = r3.moveToFirst()     // Catch:{ all -> 0x0090 }
            if (r4 == 0) goto L_0x009c
            r4 = 0
            r6.onNmsEventDeletedObjMmsBufferDbAvailableUsingCorrId(r3, r7, r4)     // Catch:{ all -> 0x0090 }
            goto L_0x009c
        L_0x0090:
            r4 = move-exception
            if (r3 == 0) goto L_0x009b
            r3.close()     // Catch:{ all -> 0x0097 }
            goto L_0x009b
        L_0x0097:
            r5 = move-exception
            r4.addSuppressed(r5)     // Catch:{ all -> 0x00a7 }
        L_0x009b:
            throw r4     // Catch:{ all -> 0x00a7 }
        L_0x009c:
            if (r3 == 0) goto L_0x00a1
            r3.close()     // Catch:{ all -> 0x00a7 }
        L_0x00a1:
            if (r1 == 0) goto L_0x00a6
            r1.close()
        L_0x00a6:
            return
        L_0x00a7:
            r2 = move-exception
            if (r1 == 0) goto L_0x00b2
            r1.close()     // Catch:{ all -> 0x00ae }
            goto L_0x00b2
        L_0x00ae:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x00b2:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject):void");
    }

    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, ChangedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor mmsCs = queryMMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (mmsCs != null) {
            try {
                if (mmsCs.moveToFirst()) {
                    onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(mmsCs, objt, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (mmsCs != null) {
            mmsCs.close();
            return;
        }
        return;
        throw th;
    }

    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.i(str, "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: " + type);
        Cursor mmsCs = queryMMSBufferDBwithResUrl(objt.resourceURL.toString());
        if (mmsCs != null) {
            try {
                if (mmsCs.moveToFirst()) {
                    onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(mmsCs, objt, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (mmsCs != null) {
            mmsCs.close();
            return;
        }
        return;
        throw th;
    }

    public void notifyMsgAppDeleteFail(int dbIndex, long bufferDbId, String line) {
        String str = TAG;
        Log.i(str, "notifyMsgAppDeleteFail, dbIndex: " + dbIndex + " bufferDbId: " + bufferDbId + " line: " + IMSLog.checker(line));
        if (dbIndex == 4) {
            JsonArray jsonArrayRowIds = new JsonArray();
            JsonObject jsobjct = new JsonObject();
            jsobjct.addProperty("id", String.valueOf(bufferDbId));
            jsonArrayRowIds.add(jsobjct);
            this.mCallbackMsgApp.notifyAppCloudDeleteFail(CloudMessageProviderContract.ApplicationTypes.MSGDATA, "MMS", jsonArrayRowIds.toString());
        }
    }
}
