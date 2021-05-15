package com.sec.internal.ims.cmstore.cloudmessagebuffer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.ims.cmstore.CloudMessageBufferDBEventSchedulingRule;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParamList;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValue;
import com.sec.internal.ims.cmstore.params.ParamAppJsonValueList;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler;
import com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IDeviceDataChangeListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.DeletedObject;
import java.util.Iterator;

public class CloudMessageBufferDBHelper extends Handler {
    private static final String TAG = CloudMessageBufferDBHelper.class.getSimpleName();
    protected BufferDBChangeParamList mBufferDBChangeNetAPI = null;
    protected boolean mBufferDBloaded = CloudMessagePreferenceManager.getInstance().getBufferDbLoaded();
    protected final CallLogScheduler mCallLogScheduler;
    protected final IBufferDBEventListener mCallbackMsgApp;
    protected final Context mContext;
    protected final IDeviceDataChangeListener mDeviceDataChangeListener;
    protected final FaxScheduler mFaxScheduler;
    protected boolean mIsGoforwardSync = false;
    protected final MmsScheduler mMmsScheduler;
    protected final MultiLineScheduler mMultiLnScheduler;
    protected boolean mProvisionSuccess = false;
    protected boolean mRCSDbReady = false;
    protected final RcsScheduler mRcsScheduler;
    protected final CloudMessageBufferDBEventSchedulingRule mScheduleRule;
    protected final SmsScheduler mSmsScheduler;
    protected final SummaryQueryBuilder mSummaryQuery;
    protected final VVMScheduler mVVMScheduler;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public CloudMessageBufferDBHelper(Looper looper, Context context, IDeviceDataChangeListener deviceDataListener, IBufferDBEventListener callback) {
        super(looper);
        Context context2 = context;
        Log.d(TAG, "onCreate");
        this.mContext = context2;
        this.mScheduleRule = new CloudMessageBufferDBEventSchedulingRule();
        this.mDeviceDataChangeListener = deviceDataListener;
        this.mCallbackMsgApp = callback;
        this.mBufferDBChangeNetAPI = new BufferDBChangeParamList();
        this.mSummaryQuery = new SummaryQueryBuilder(context2, this.mCallbackMsgApp);
        this.mMultiLnScheduler = new MultiLineScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper);
        this.mSmsScheduler = new SmsScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mMultiLnScheduler, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper);
        this.mMmsScheduler = new MmsScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mMultiLnScheduler, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper);
        this.mRcsScheduler = new RcsScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mDeviceDataChangeListener, this.mCallbackMsgApp, this.mMmsScheduler, this.mSmsScheduler, looper);
        Looper looper2 = looper;
        this.mVVMScheduler = new VVMScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper2);
        this.mFaxScheduler = new FaxScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper);
        this.mCallLogScheduler = new CallLogScheduler(this.mContext, this.mScheduleRule, this.mSummaryQuery, this.mDeviceDataChangeListener, this.mCallbackMsgApp, looper2);
    }

    /* access modifiers changed from: protected */
    public void buildBufferList(BufferDBChangeParamList list, Cursor cs, int type, boolean isGoforwardSync, boolean isUpload) {
        BufferDBChangeParamList bufferDBChangeParamList = list;
        Cursor cursor = cs;
        if (isUpload) {
            if (cursor != null && cs.moveToFirst()) {
                do {
                    int i = type;
                    bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(i, (long) cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), isGoforwardSync, (String) null));
                } while (cs.moveToNext() != 0);
            }
        } else if (cursor != null && cs.moveToFirst()) {
            do {
                int id = cs.getInt(cs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                int i2 = type;
                bufferDBChangeParamList.mChangelst.add(new BufferDBChangeParam(i2, (long) id, isGoforwardSync, cs.getString(cs.getColumnIndexOrThrow("linenum"))));
            } while (cs.moveToNext() != 0);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendUnDownloadedMessage(String linenum, SyncMsgType syncType, boolean isGoforwardSync) {
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4;
        Throwable th5;
        String str = linenum;
        SyncMsgType syncMsgType = syncType;
        BufferDBChangeParamList list = new BufferDBChangeParamList();
        int messageType = getMessageType(syncType);
        if (SyncMsgType.MESSAGE.equals(syncType) || SyncMsgType.DEFAULT.equals(syncType)) {
            Cursor mms = this.mMmsScheduler.queryToDeviceUnDownloadedMms(linenum);
            if (mms != null) {
                try {
                    if (mms.moveToFirst()) {
                        do {
                            int id = mms.getInt(mms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID));
                            BufferDBChangeParamList bufferDBChangeParamList = list;
                            this.mMmsScheduler.addMmsPartDownloadList(bufferDBChangeParamList, (long) id, mms.getString(mms.getColumnIndexOrThrow("linenum")), isGoforwardSync);
                        } while (mms.moveToNext() != 0);
                    }
                } catch (Throwable th6) {
                    th2.addSuppressed(th6);
                }
            }
            if (mms != null) {
                mms.close();
            }
            Cursor rcs = this.mRcsScheduler.queryToDeviceUnDownloadedRcs(linenum);
            try {
                buildBufferList(list, rcs, 1, isGoforwardSync, false);
                if (rcs != null) {
                    rcs.close();
                }
            } catch (Throwable th7) {
                th.addSuppressed(th7);
            }
        } else if (SyncMsgType.VM.equals(syncType)) {
            Cursor vvm = this.mVVMScheduler.queryToDeviceUnDownloadedVvm(linenum);
            try {
                buildBufferList(list, vvm, 17, isGoforwardSync, false);
                if (vvm != null) {
                    vvm.close();
                }
            } catch (Throwable th8) {
                th5.addSuppressed(th8);
            }
        } else if (SyncMsgType.FAX.equals(syncType)) {
            Cursor fax = this.mFaxScheduler.queryToDeviceUnDownloadedFax(linenum);
            try {
                buildBufferList(list, fax, 21, isGoforwardSync, false);
                if (fax != null) {
                    fax.close();
                }
            } catch (Throwable th9) {
                th4.addSuppressed(th9);
            }
        } else if (SyncMsgType.VM_GREETINGS.equals(syncType)) {
            Cursor grtcs = this.mVVMScheduler.queryToDeviceUnDownloadedGreeting(linenum);
            try {
                buildBufferList(list, grtcs, 18, isGoforwardSync, false);
                if (grtcs != null) {
                    grtcs.close();
                }
            } catch (Throwable th10) {
                th3.addSuppressed(th10);
            }
        } else if (SyncMsgType.CALLLOG.equals(syncType)) {
            list.mChangelst.add(new BufferDBChangeParam(16, 0, isGoforwardSync, linenum));
        }
        if (messageType >= 0 && messageType != 16 && list.mChangelst.size() < 1) {
            list.mChangelst.add(new BufferDBChangeParam(messageType, 0, isGoforwardSync, linenum));
        }
        if (list.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceInitialSyncDownload(list);
            return;
        }
        return;
        throw th2;
        throw th3;
        throw th4;
        throw th;
        throw th5;
    }

    /* access modifiers changed from: protected */
    public void onSendCloudUnSyncedUpdate() {
        BufferDBChangeParamList list = new BufferDBChangeParamList();
        Cursor sms = this.mSmsScheduler.queryToCloudUnsyncedSms();
        try {
            buildBufferList(list, sms, 3, false, false);
            if (sms != null) {
                sms.close();
            }
            Cursor mms = this.mMmsScheduler.queryToCloudUnsyncedMms();
            try {
                buildBufferList(list, mms, 4, false, false);
                if (mms != null) {
                    mms.close();
                }
                Cursor rcs = this.mRcsScheduler.queryToCloudUnsyncedRcs();
                try {
                    buildBufferList(list, rcs, 1, false, false);
                    if (rcs != null) {
                        rcs.close();
                    }
                    if (list.mChangelst.size() > 0) {
                        this.mDeviceDataChangeListener.sendDeviceUpdate(list);
                        return;
                    }
                    return;
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
            throw th;
            throw th;
        } catch (Throwable th3) {
            th.addSuppressed(th3);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendDeviceUnSyncedUpdate() {
        Cursor sms = this.mSmsScheduler.queryToDeviceUnsyncedSms();
        if (sms != null) {
            try {
                if (sms.moveToFirst()) {
                    do {
                        this.mSmsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, this.mSmsScheduler.getMessageTypeString(3, false), (long) sms.getInt(sms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)));
                    } while (sms.moveToNext() != 0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (sms != null) {
            sms.close();
        }
        Cursor mms = this.mMmsScheduler.queryToDeviceUnsyncedMms();
        if (mms != null) {
            try {
                if (mms.moveToFirst()) {
                    do {
                        this.mMmsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, this.mMmsScheduler.getMessageTypeString(4, false), (long) mms.getInt(mms.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)));
                    } while (mms.moveToNext() != 0);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (mms != null) {
            mms.close();
        }
        Cursor rcs = this.mRcsScheduler.queryToDeviceUnsyncedRcs();
        if (rcs != null) {
            try {
                if (rcs.moveToFirst()) {
                    do {
                        this.mRcsScheduler.notifyMsgAppCldNotification(CloudMessageProviderContract.ApplicationTypes.MSGDATA, this.mRcsScheduler.getMessageTypeString(1, rcs.getInt(rcs.getColumnIndexOrThrow(ImContract.ChatItem.IS_FILE_TRANSFER)) == 1), (long) rcs.getInt(rcs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)));
                    } while (rcs.moveToNext() != 0);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (rcs != null) {
            rcs.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }

    private int getMessageType(SyncMsgType syncType) {
        if (SyncMsgType.MESSAGE.equals(syncType) || SyncMsgType.DEFAULT.equals(syncType)) {
            return 1;
        }
        if (SyncMsgType.FAX.equals(syncType)) {
            return 21;
        }
        if (SyncMsgType.VM.equals(syncType)) {
            return 17;
        }
        if (SyncMsgType.CALLLOG.equals(syncType)) {
            return 16;
        }
        if (SyncMsgType.VM_GREETINGS.equals(syncType)) {
            return 18;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void notifyNetAPIUploadMessages(String linenum, SyncMsgType syncType, boolean isGoforwardSync) {
        BufferDBChangeParamList list = new BufferDBChangeParamList();
        if (!CloudMessageStrategyManager.getStrategy().requiresMsgUploadInInitSync()) {
            int messageType = getMessageType(syncType);
            if (messageType >= 0) {
                list.mChangelst.add(new BufferDBChangeParam(messageType, 0, isGoforwardSync, linenum));
            }
            this.mDeviceDataChangeListener.sendDeviceUpload(list);
            return;
        }
        Cursor cs = this.mSmsScheduler.querySMSMessagesToUpload();
        try {
            buildBufferList(list, cs, 3, isGoforwardSync, true);
            if (cs != null) {
                cs.close();
            }
            Cursor cs2 = this.mMmsScheduler.queryMMSMessagesToUpload();
            try {
                buildBufferList(list, cs2, 4, isGoforwardSync, true);
                if (cs2 != null) {
                    cs2.close();
                }
                Cursor cs3 = this.mRcsScheduler.queryRCSMessagesToUpload();
                try {
                    buildBufferList(list, cs3, 1, isGoforwardSync, true);
                    if (cs3 != null) {
                        cs3.close();
                    }
                    Cursor cs4 = this.mRcsScheduler.queryImdnMessagesToUpload();
                    try {
                        buildBufferList(list, cs4, 13, isGoforwardSync, true);
                        if (cs4 != null) {
                            cs4.close();
                        }
                        if (list.mChangelst.size() > 0) {
                            this.mDeviceDataChangeListener.sendDeviceUpload(list);
                            return;
                        }
                        list.mChangelst.add(new BufferDBChangeParam(1, 0, isGoforwardSync, linenum));
                        this.mDeviceDataChangeListener.sendDeviceUpload(list);
                        return;
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        } catch (Throwable th4) {
            th.addSuppressed(th4);
        }
        throw th;
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void handleBulkOpSingleUrlSuccess(String url) {
        String str = TAG;
        Log.d(str, "handleBulkDeleteSingleUrlSuccess: " + IMSLog.checker(url));
        if (url != null) {
            Cursor summaryCs = this.mSummaryQuery.querySummaryDBwithResUrl(url);
            if (summaryCs != null) {
                try {
                    if (summaryCs.moveToFirst()) {
                        onUpdateBufferDBBulkUpdateSuccess(summaryCs, url);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (summaryCs != null) {
                summaryCs.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x0193 A[SYNTHETIC, Splitter:B:100:0x0193] */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01a1  */
    /* JADX WARNING: Removed duplicated region for block: B:132:0x0200 A[SYNTHETIC, Splitter:B:132:0x0200] */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x020e  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x026d A[SYNTHETIC, Splitter:B:164:0x026d] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x027b  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x02da A[SYNTHETIC, Splitter:B:196:0x02da] */
    /* JADX WARNING: Removed duplicated region for block: B:203:0x02e8  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b9 A[SYNTHETIC, Splitter:B:36:0x00b9] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00c7  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0126 A[SYNTHETIC, Splitter:B:68:0x0126] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0134  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onUpdateBufferDBBulkUpdateSuccess(android.database.Cursor r20, java.lang.String r21) {
        /*
            r19 = this;
            r1 = r19
            r2 = r20
            r3 = r21
            java.lang.String r0 = "syncaction"
            int r0 = r2.getColumnIndexOrThrow(r0)
            int r4 = r2.getInt(r0)
            java.lang.String r0 = "messagetype"
            int r0 = r2.getColumnIndexOrThrow(r0)
            int r11 = r2.getInt(r0)
            java.lang.String r0 = TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onUpdateBufferDBBulkUpdateSuccess,  status: "
            r5.append(r6)
            r5.append(r4)
            java.lang.String r6 = " msgtype: "
            r5.append(r6)
            r5.append(r11)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r0, r5)
            r5 = 0
            r7 = 0
            r12 = 0
            r13 = 0
            r0 = 1
            java.lang.String r8 = "linenum"
            java.lang.String r9 = "_bufferdbid"
            if (r11 == r0) goto L_0x0280
            r0 = 21
            if (r11 == r0) goto L_0x0213
            r0 = 3
            if (r11 == r0) goto L_0x01a6
            r0 = 4
            if (r11 == r0) goto L_0x0139
            r0 = 16
            if (r11 == r0) goto L_0x00cc
            r0 = 17
            if (r11 == r0) goto L_0x005b
            goto L_0x02ec
        L_0x005b:
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r0 = r1.mVVMScheduler
            android.database.Cursor r15 = r0.queryVVMwithResUrl(r3)
            if (r15 == 0) goto L_0x00c3
            boolean r0 = r15.moveToFirst()     // Catch:{ all -> 0x00b5 }
            if (r0 == 0) goto L_0x00c3
            int r0 = r15.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x00b5 }
            int r0 = r15.getInt(r0)     // Catch:{ all -> 0x00b5 }
            long r5 = (long) r0
            int r0 = r15.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x00b0 }
            java.lang.String r10 = r15.getString(r0)     // Catch:{ all -> 0x00b0 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x00aa }
            r0.<init>()     // Catch:{ all -> 0x00aa }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r9 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x00aa }
            r16 = 0
            r17 = r5
            r5 = r9
            r6 = r11
            r7 = r17
            r14 = r9
            r9 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x00a4 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r14)     // Catch:{ all -> 0x00a4 }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.VVMScheduler r0 = r1.mVVMScheduler     // Catch:{ all -> 0x00a4 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x00a4 }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x00a4 }
            r7 = r10
            r5 = r17
            goto L_0x00c5
        L_0x00a4:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r17
            goto L_0x00b7
        L_0x00aa:
            r0 = move-exception
            r17 = r5
            r8 = r0
            r7 = r10
            goto L_0x00b7
        L_0x00b0:
            r0 = move-exception
            r17 = r5
            r8 = r0
            goto L_0x00b7
        L_0x00b5:
            r0 = move-exception
            r8 = r0
        L_0x00b7:
            if (r15 == 0) goto L_0x00c2
            r15.close()     // Catch:{ all -> 0x00bd }
            goto L_0x00c2
        L_0x00bd:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x00c2:
            throw r8
        L_0x00c3:
            r0 = 1
            r13 = r0
        L_0x00c5:
            if (r15 == 0) goto L_0x00ca
            r15.close()
        L_0x00ca:
            goto L_0x02ec
        L_0x00cc:
            com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler r0 = r1.mCallLogScheduler
            android.database.Cursor r14 = r0.queryCallLogMessageBufferDBwithResUrl(r3)
            if (r14 == 0) goto L_0x0130
            boolean r0 = r14.moveToFirst()     // Catch:{ all -> 0x0122 }
            if (r0 == 0) goto L_0x0130
            int r0 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x0122 }
            int r0 = r14.getInt(r0)     // Catch:{ all -> 0x0122 }
            long r5 = (long) r0
            int r0 = r14.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x011d }
            java.lang.String r10 = r14.getString(r0)     // Catch:{ all -> 0x011d }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x0117 }
            r0.<init>()     // Catch:{ all -> 0x0117 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0117 }
            r9 = 0
            r16 = r5
            r5 = r15
            r6 = r11
            r7 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x0111 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r15)     // Catch:{ all -> 0x0111 }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.CallLogScheduler r0 = r1.mCallLogScheduler     // Catch:{ all -> 0x0111 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x0111 }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x0111 }
            r7 = r10
            r5 = r16
            goto L_0x0132
        L_0x0111:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r16
            goto L_0x0124
        L_0x0117:
            r0 = move-exception
            r16 = r5
            r8 = r0
            r7 = r10
            goto L_0x0124
        L_0x011d:
            r0 = move-exception
            r16 = r5
            r8 = r0
            goto L_0x0124
        L_0x0122:
            r0 = move-exception
            r8 = r0
        L_0x0124:
            if (r14 == 0) goto L_0x012f
            r14.close()     // Catch:{ all -> 0x012a }
            goto L_0x012f
        L_0x012a:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x012f:
            throw r8
        L_0x0130:
            r0 = 1
            r13 = r0
        L_0x0132:
            if (r14 == 0) goto L_0x0137
            r14.close()
        L_0x0137:
            goto L_0x02ec
        L_0x0139:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r1.mMmsScheduler
            android.database.Cursor r14 = r0.queryMMSBufferDBwithResUrl(r3)
            if (r14 == 0) goto L_0x019d
            boolean r0 = r14.moveToFirst()     // Catch:{ all -> 0x018f }
            if (r0 == 0) goto L_0x019d
            int r0 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x018f }
            int r0 = r14.getInt(r0)     // Catch:{ all -> 0x018f }
            long r5 = (long) r0
            int r0 = r14.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x018a }
            java.lang.String r10 = r14.getString(r0)     // Catch:{ all -> 0x018a }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x0184 }
            r0.<init>()     // Catch:{ all -> 0x0184 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0184 }
            r9 = 0
            r16 = r5
            r5 = r15
            r6 = r11
            r7 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x017e }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r15)     // Catch:{ all -> 0x017e }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r1.mMmsScheduler     // Catch:{ all -> 0x017e }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x017e }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x017e }
            r7 = r10
            r5 = r16
            goto L_0x019f
        L_0x017e:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r16
            goto L_0x0191
        L_0x0184:
            r0 = move-exception
            r16 = r5
            r8 = r0
            r7 = r10
            goto L_0x0191
        L_0x018a:
            r0 = move-exception
            r16 = r5
            r8 = r0
            goto L_0x0191
        L_0x018f:
            r0 = move-exception
            r8 = r0
        L_0x0191:
            if (r14 == 0) goto L_0x019c
            r14.close()     // Catch:{ all -> 0x0197 }
            goto L_0x019c
        L_0x0197:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x019c:
            throw r8
        L_0x019d:
            r0 = 1
            r13 = r0
        L_0x019f:
            if (r14 == 0) goto L_0x01a4
            r14.close()
        L_0x01a4:
            goto L_0x02ec
        L_0x01a6:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r1.mSmsScheduler
            android.database.Cursor r14 = r0.querySMSBufferDBwithResUrl(r3)
            if (r14 == 0) goto L_0x020a
            boolean r0 = r14.moveToFirst()     // Catch:{ all -> 0x01fc }
            if (r0 == 0) goto L_0x020a
            int r0 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x01fc }
            int r0 = r14.getInt(r0)     // Catch:{ all -> 0x01fc }
            long r5 = (long) r0
            int r0 = r14.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x01f7 }
            java.lang.String r10 = r14.getString(r0)     // Catch:{ all -> 0x01f7 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x01f1 }
            r0.<init>()     // Catch:{ all -> 0x01f1 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x01f1 }
            r9 = 0
            r16 = r5
            r5 = r15
            r6 = r11
            r7 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x01eb }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r15)     // Catch:{ all -> 0x01eb }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r1.mSmsScheduler     // Catch:{ all -> 0x01eb }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x01eb }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x01eb }
            r7 = r10
            r5 = r16
            goto L_0x020c
        L_0x01eb:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r16
            goto L_0x01fe
        L_0x01f1:
            r0 = move-exception
            r16 = r5
            r8 = r0
            r7 = r10
            goto L_0x01fe
        L_0x01f7:
            r0 = move-exception
            r16 = r5
            r8 = r0
            goto L_0x01fe
        L_0x01fc:
            r0 = move-exception
            r8 = r0
        L_0x01fe:
            if (r14 == 0) goto L_0x0209
            r14.close()     // Catch:{ all -> 0x0204 }
            goto L_0x0209
        L_0x0204:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x0209:
            throw r8
        L_0x020a:
            r0 = 1
            r13 = r0
        L_0x020c:
            if (r14 == 0) goto L_0x0211
            r14.close()
        L_0x0211:
            goto L_0x02ec
        L_0x0213:
            com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler r0 = r1.mFaxScheduler
            android.database.Cursor r14 = r0.queryFaxMessageBufferDBwithResUrl(r3)
            if (r14 == 0) goto L_0x0277
            boolean r0 = r14.moveToFirst()     // Catch:{ all -> 0x0269 }
            if (r0 == 0) goto L_0x0277
            int r0 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x0269 }
            int r0 = r14.getInt(r0)     // Catch:{ all -> 0x0269 }
            long r5 = (long) r0
            int r0 = r14.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x0264 }
            java.lang.String r10 = r14.getString(r0)     // Catch:{ all -> 0x0264 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x025e }
            r0.<init>()     // Catch:{ all -> 0x025e }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x025e }
            r9 = 0
            r16 = r5
            r5 = r15
            r6 = r11
            r7 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x0258 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r15)     // Catch:{ all -> 0x0258 }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.FaxScheduler r0 = r1.mFaxScheduler     // Catch:{ all -> 0x0258 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x0258 }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x0258 }
            r7 = r10
            r5 = r16
            goto L_0x0279
        L_0x0258:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r16
            goto L_0x026b
        L_0x025e:
            r0 = move-exception
            r16 = r5
            r8 = r0
            r7 = r10
            goto L_0x026b
        L_0x0264:
            r0 = move-exception
            r16 = r5
            r8 = r0
            goto L_0x026b
        L_0x0269:
            r0 = move-exception
            r8 = r0
        L_0x026b:
            if (r14 == 0) goto L_0x0276
            r14.close()     // Catch:{ all -> 0x0271 }
            goto L_0x0276
        L_0x0271:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x0276:
            throw r8
        L_0x0277:
            r0 = 1
            r13 = r0
        L_0x0279:
            if (r14 == 0) goto L_0x027e
            r14.close()
        L_0x027e:
            goto L_0x02ec
        L_0x0280:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r1.mRcsScheduler
            android.database.Cursor r14 = r0.queryRCSBufferDBwithResUrl(r3)
            if (r14 == 0) goto L_0x02e4
            boolean r0 = r14.moveToFirst()     // Catch:{ all -> 0x02d6 }
            if (r0 == 0) goto L_0x02e4
            int r0 = r14.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x02d6 }
            int r0 = r14.getInt(r0)     // Catch:{ all -> 0x02d6 }
            long r5 = (long) r0
            int r0 = r14.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x02d1 }
            java.lang.String r10 = r14.getString(r0)     // Catch:{ all -> 0x02d1 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder     // Catch:{ all -> 0x02cb }
            r0.<init>()     // Catch:{ all -> 0x02cb }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r15 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x02cb }
            r9 = 0
            r16 = r5
            r5 = r15
            r6 = r11
            r7 = r16
            r5.<init>(r6, r7, r9, r10)     // Catch:{ all -> 0x02c5 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r0 = r0.setBufferDBChangeParam((com.sec.internal.ims.cmstore.params.BufferDBChangeParam) r15)     // Catch:{ all -> 0x02c5 }
            r12 = r0
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r1.mRcsScheduler     // Catch:{ all -> 0x02c5 }
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r5 = r12.build()     // Catch:{ all -> 0x02c5 }
            r6 = 0
            r0.onCloudUpdateFlagSuccess(r5, r6)     // Catch:{ all -> 0x02c5 }
            r7 = r10
            r5 = r16
            goto L_0x02e6
        L_0x02c5:
            r0 = move-exception
            r8 = r0
            r7 = r10
            r5 = r16
            goto L_0x02d8
        L_0x02cb:
            r0 = move-exception
            r16 = r5
            r8 = r0
            r7 = r10
            goto L_0x02d8
        L_0x02d1:
            r0 = move-exception
            r16 = r5
            r8 = r0
            goto L_0x02d8
        L_0x02d6:
            r0 = move-exception
            r8 = r0
        L_0x02d8:
            if (r14 == 0) goto L_0x02e3
            r14.close()     // Catch:{ all -> 0x02de }
            goto L_0x02e3
        L_0x02de:
            r0 = move-exception
            r9 = r0
            r8.addSuppressed(r9)
        L_0x02e3:
            throw r8
        L_0x02e4:
            r0 = 1
            r13 = r0
        L_0x02e6:
            if (r14 == 0) goto L_0x02eb
            r14.close()
        L_0x02eb:
        L_0x02ec:
            if (r13 == 0) goto L_0x02f5
            java.lang.String r0 = TAG
            java.lang.String r8 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r0, r8)
        L_0x02f5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onUpdateBufferDBBulkUpdateSuccess(android.database.Cursor, java.lang.String):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNmsEventChangedObjSummaryDbAvailableUsingUrl(android.database.Cursor r5, com.sec.internal.omanetapi.nms.data.ChangedObject r6, boolean r7) {
        /*
            r4 = this;
            java.lang.String r0 = "messagetype"
            int r0 = r5.getColumnIndexOrThrow(r0)
            int r0 = r5.getInt(r0)
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onNmsEventChangedObjSummaryDbAvailableUsingUrl(), type: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r3 = " isgoforwardSync: "
            r2.append(r3)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            r1 = 1
            if (r0 == r1) goto L_0x00a0
            r1 = 14
            if (r0 == r1) goto L_0x00a0
            r1 = 3
            if (r0 == r1) goto L_0x006d
            r1 = 4
            if (r0 == r1) goto L_0x0041
            r1 = 11
            if (r0 == r1) goto L_0x00a0
            r1 = 12
            if (r0 == r1) goto L_0x00a0
            goto L_0x00cc
        L_0x0041:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r1 = r4.mMmsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.queryMMSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x0067
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x005b }
            if (r2 == 0) goto L_0x0067
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r4.mMmsScheduler     // Catch:{ all -> 0x005b }
            r2.onNmsEventChangedObjBufferDbMmsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x005b }
            goto L_0x0067
        L_0x005b:
            r2 = move-exception
            if (r1 == 0) goto L_0x0066
            r1.close()     // Catch:{ all -> 0x0062 }
            goto L_0x0066
        L_0x0062:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0066:
            throw r2
        L_0x0067:
            if (r1 == 0) goto L_0x006c
            r1.close()
        L_0x006c:
            goto L_0x00cc
        L_0x006d:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r1 = r4.mSmsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySMSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x0087
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0094 }
            if (r2 == 0) goto L_0x0087
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r2 = r4.mSmsScheduler     // Catch:{ all -> 0x0094 }
            r2.onNmsEventChangedObjBufferDbSmsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x0094 }
            goto L_0x008e
        L_0x0087:
            java.lang.String r2 = TAG     // Catch:{ all -> 0x0094 }
            java.lang.String r3 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x0094 }
        L_0x008e:
            if (r1 == 0) goto L_0x0093
            r1.close()
        L_0x0093:
            goto L_0x00cc
        L_0x0094:
            r2 = move-exception
            if (r1 == 0) goto L_0x009f
            r1.close()     // Catch:{ all -> 0x009b }
            goto L_0x009f
        L_0x009b:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x009f:
            throw r2
        L_0x00a0:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r1 = r4.mRcsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.queryRCSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x00c6
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x00ba }
            if (r2 == 0) goto L_0x00c6
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r4.mRcsScheduler     // Catch:{ all -> 0x00ba }
            r2.onNmsEventChangedObjBufferDbRcsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x00ba }
            goto L_0x00c6
        L_0x00ba:
            r2 = move-exception
            if (r1 == 0) goto L_0x00c5
            r1.close()     // Catch:{ all -> 0x00c1 }
            goto L_0x00c5
        L_0x00c1:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x00c5:
            throw r2
        L_0x00c6:
            if (r1 == 0) goto L_0x00cb
            r1.close()
        L_0x00cb:
        L_0x00cc:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onNmsEventChangedObjSummaryDbAvailableUsingUrl(android.database.Cursor, com.sec.internal.omanetapi.nms.data.ChangedObject, boolean):void");
    }

    /* Debug info: failed to restart local var, previous not found, register: 17 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:109:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0190  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject r18, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList r19, boolean r20) {
        /*
            r17 = this;
            r1 = r17
            r2 = r18
            r9 = r20
            java.net.URL r0 = r2.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r10 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            r3 = 0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryQuery
            java.net.URL r4 = r2.resourceURL
            java.lang.String r4 = r4.toString()
            android.database.Cursor r11 = r0.querySummaryDBwithResUrl(r4)
            if (r11 == 0) goto L_0x0059
            boolean r0 = r11.moveToFirst()     // Catch:{ all -> 0x0194 }
            if (r0 == 0) goto L_0x0059
            java.lang.String r0 = "syncaction"
            int r0 = r11.getColumnIndexOrThrow(r0)     // Catch:{ all -> 0x0194 }
            int r0 = r11.getInt(r0)     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0194 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0194 }
            r5.<init>()     // Catch:{ all -> 0x0194 }
            java.lang.String r6 = "handleCloudNotifyChangedObj, Status: "
            r5.append(r6)     // Catch:{ all -> 0x0194 }
            r5.append(r0)     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0194 }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0194 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0194 }
            int r4 = r4.getId()     // Catch:{ all -> 0x0194 }
            if (r0 != r4) goto L_0x0054
            if (r11 == 0) goto L_0x0053
            r11.close()
        L_0x0053:
            return
        L_0x0054:
            r1.onNmsEventChangedObjSummaryDbAvailableUsingUrl(r11, r2, r9)     // Catch:{ all -> 0x0194 }
            goto L_0x018e
        L_0x0059:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryQuery     // Catch:{ all -> 0x0194 }
            r4 = 0
            long r4 = r0.insertNmsEventChangedObjToSummaryDB(r2, r4)     // Catch:{ all -> 0x0194 }
            r12 = r4
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r0 = r1.mMultiLnScheduler     // Catch:{ all -> 0x0194 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT     // Catch:{ all -> 0x0194 }
            int r0 = r0.getLineInitSyncStatus(r10, r4)     // Catch:{ all -> 0x0194 }
            r14 = r0
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r0 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x0194 }
            int r0 = r0.getId()     // Catch:{ all -> 0x0194 }
            if (r14 == r0) goto L_0x007f
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = "initial sync not complete yet, buffer the NMS events until initial sync is finished"
            android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0194 }
            if (r11 == 0) goto L_0x007e
            r11.close()
        L_0x007e:
            return
        L_0x007f:
            java.lang.String r0 = r2.correlationId     // Catch:{ all -> 0x0194 }
            if (r0 == 0) goto L_0x00cd
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0194 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0194 }
            r4.<init>()     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = "handleCloudNotifyChangedObj RCS CloudUpdate: "
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = r2.correlationId     // Catch:{ all -> 0x0194 }
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0194 }
            android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0194 }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r1.mRcsScheduler     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r2.correlationId     // Catch:{ all -> 0x0194 }
            android.database.Cursor r0 = r0.searchIMFTBufferUsingImdn(r4, r10)     // Catch:{ all -> 0x0194 }
            r4 = r0
            if (r4 == 0) goto L_0x00c8
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x00ba }
            if (r0 == 0) goto L_0x00c8
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryQuery     // Catch:{ all -> 0x00ba }
            r5 = 1
            r0.updateSummaryDbUsingMessageType(r12, r5)     // Catch:{ all -> 0x00ba }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r0 = r1.mRcsScheduler     // Catch:{ all -> 0x00ba }
            r0.onNmsEventChangedObjRcsBufferDbAvailableUsingImdnId(r4, r2, r9)     // Catch:{ all -> 0x00ba }
            r0 = 1
            r3 = r0
            goto L_0x00c8
        L_0x00ba:
            r0 = move-exception
            r5 = r0
            if (r4 == 0) goto L_0x00c7
            r4.close()     // Catch:{ all -> 0x00c2 }
            goto L_0x00c7
        L_0x00c2:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ all -> 0x0194 }
        L_0x00c7:
            throw r5     // Catch:{ all -> 0x0194 }
        L_0x00c8:
            if (r4 == 0) goto L_0x00cd
            r4.close()     // Catch:{ all -> 0x0194 }
        L_0x00cd:
            if (r3 != 0) goto L_0x011d
            java.lang.String r0 = r2.correlationId     // Catch:{ all -> 0x0194 }
            if (r0 == 0) goto L_0x011d
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0194 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0194 }
            r4.<init>()     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = "handleCloudNotifyChangedObj MMS CloudUpdate: "
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = r2.correlationId     // Catch:{ all -> 0x0194 }
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0194 }
            android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0194 }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r1.mMmsScheduler     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r2.correlationId     // Catch:{ all -> 0x0194 }
            android.database.Cursor r0 = r0.searchMMsPduBufferUsingCorrelationId(r4)     // Catch:{ all -> 0x0194 }
            r4 = r0
            if (r4 == 0) goto L_0x0118
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x010a }
            if (r0 == 0) goto L_0x0118
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryQuery     // Catch:{ all -> 0x010a }
            r5 = 4
            r0.updateSummaryDbUsingMessageType(r12, r5)     // Catch:{ all -> 0x010a }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r0 = r1.mMmsScheduler     // Catch:{ all -> 0x010a }
            r0.onNmsEventChangedObjMmsBufferDbAvailableUsingCorrId(r4, r2, r9)     // Catch:{ all -> 0x010a }
            r0 = 1
            r3 = r0
            goto L_0x0118
        L_0x010a:
            r0 = move-exception
            r5 = r0
            if (r4 == 0) goto L_0x0117
            r4.close()     // Catch:{ all -> 0x0112 }
            goto L_0x0117
        L_0x0112:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ all -> 0x0194 }
        L_0x0117:
            throw r5     // Catch:{ all -> 0x0194 }
        L_0x0118:
            if (r4 == 0) goto L_0x011d
            r4.close()     // Catch:{ all -> 0x0194 }
        L_0x011d:
            if (r3 != 0) goto L_0x016d
            java.lang.String r0 = r2.correlationTag     // Catch:{ all -> 0x0194 }
            if (r0 == 0) goto L_0x016d
            java.lang.String r0 = TAG     // Catch:{ all -> 0x0194 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ all -> 0x0194 }
            r4.<init>()     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = "handleCloudNotifyChangedObj: "
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r5 = r2.correlationTag     // Catch:{ all -> 0x0194 }
            r4.append(r5)     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x0194 }
            android.util.Log.d(r0, r4)     // Catch:{ all -> 0x0194 }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r1.mSmsScheduler     // Catch:{ all -> 0x0194 }
            java.lang.String r4 = r2.correlationTag     // Catch:{ all -> 0x0194 }
            android.database.Cursor r0 = r0.searchUnSyncedSMSBufferUsingCorrelationTag(r4)     // Catch:{ all -> 0x0194 }
            r4 = r0
            if (r4 == 0) goto L_0x0168
            boolean r0 = r4.moveToFirst()     // Catch:{ all -> 0x015a }
            if (r0 == 0) goto L_0x0168
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r0 = r1.mSummaryQuery     // Catch:{ all -> 0x015a }
            r5 = 3
            r0.updateSummaryDbUsingMessageType(r12, r5)     // Catch:{ all -> 0x015a }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r0 = r1.mSmsScheduler     // Catch:{ all -> 0x015a }
            r0.onNmsEventChangedObjSmsBufferDbAvailableUsingCorrTag(r4, r2, r9)     // Catch:{ all -> 0x015a }
            r0 = 1
            r3 = r0
            goto L_0x0168
        L_0x015a:
            r0 = move-exception
            r5 = r0
            if (r4 == 0) goto L_0x0167
            r4.close()     // Catch:{ all -> 0x0162 }
            goto L_0x0167
        L_0x0162:
            r0 = move-exception
            r6 = r0
            r5.addSuppressed(r6)     // Catch:{ all -> 0x0194 }
        L_0x0167:
            throw r5     // Catch:{ all -> 0x0194 }
        L_0x0168:
            if (r4 == 0) goto L_0x016d
            r4.close()     // Catch:{ all -> 0x0194 }
        L_0x016d:
            r15 = r3
            if (r9 != 0) goto L_0x018d
            if (r15 != 0) goto L_0x018d
            r8 = r19
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.BufferDBChangeParam> r0 = r8.mChangelst     // Catch:{ all -> 0x0189 }
            com.sec.internal.ims.cmstore.params.BufferDBChangeParam r7 = new com.sec.internal.ims.cmstore.params.BufferDBChangeParam     // Catch:{ all -> 0x0189 }
            r4 = 7
            r3 = r7
            r5 = r12
            r16 = r7
            r7 = r20
            r8 = r10
            r3.<init>(r4, r5, r7, r8)     // Catch:{ all -> 0x0189 }
            r3 = r16
            r0.add(r3)     // Catch:{ all -> 0x0189 }
            goto L_0x018d
        L_0x0189:
            r0 = move-exception
            r4 = r0
            r3 = r15
            goto L_0x0196
        L_0x018d:
            r3 = r15
        L_0x018e:
            if (r11 == 0) goto L_0x0193
            r11.close()
        L_0x0193:
            return
        L_0x0194:
            r0 = move-exception
            r4 = r0
        L_0x0196:
            if (r11 == 0) goto L_0x01a1
            r11.close()     // Catch:{ all -> 0x019c }
            goto L_0x01a1
        L_0x019c:
            r0 = move-exception
            r5 = r0
            r4.addSuppressed(r5)
        L_0x01a1:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.handleCloudNotifyChangedObj(com.sec.internal.omanetapi.nms.data.ChangedObject, com.sec.internal.ims.cmstore.params.BufferDBChangeParamList, boolean):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:62:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNmsEventDeletedObjSummaryDbAvailableUsingUrl(android.database.Cursor r5, com.sec.internal.omanetapi.nms.data.DeletedObject r6, boolean r7) {
        /*
            r4 = this;
            java.lang.String r0 = "messagetype"
            int r0 = r5.getColumnIndexOrThrow(r0)
            int r0 = r5.getInt(r0)
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onNmsEventDeletedObjSummaryDbAvailableUsingUrl(), type: "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            r1 = 1
            if (r0 == r1) goto L_0x0098
            r1 = 14
            if (r0 == r1) goto L_0x0098
            r1 = 3
            if (r0 == r1) goto L_0x0065
            r1 = 4
            if (r0 == r1) goto L_0x0039
            r1 = 11
            if (r0 == r1) goto L_0x0098
            r1 = 12
            if (r0 == r1) goto L_0x0098
            goto L_0x00c4
        L_0x0039:
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r1 = r4.mMmsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.queryMMSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x005f
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x0053 }
            if (r2 == 0) goto L_0x005f
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r2 = r4.mMmsScheduler     // Catch:{ all -> 0x0053 }
            r2.onNmsEventDeletedObjBufferDbMmsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x0053 }
            goto L_0x005f
        L_0x0053:
            r2 = move-exception
            if (r1 == 0) goto L_0x005e
            r1.close()     // Catch:{ all -> 0x005a }
            goto L_0x005e
        L_0x005a:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x005e:
            throw r2
        L_0x005f:
            if (r1 == 0) goto L_0x0064
            r1.close()
        L_0x0064:
            goto L_0x00c4
        L_0x0065:
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r1 = r4.mSmsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.querySMSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x007f
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x008c }
            if (r2 == 0) goto L_0x007f
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r2 = r4.mSmsScheduler     // Catch:{ all -> 0x008c }
            r2.onNmsEventDeletedObjBufferDbSmsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x008c }
            goto L_0x0086
        L_0x007f:
            java.lang.String r2 = TAG     // Catch:{ all -> 0x008c }
            java.lang.String r3 = "inconsistency between buffer or duplicated nms event"
            android.util.Log.e(r2, r3)     // Catch:{ all -> 0x008c }
        L_0x0086:
            if (r1 == 0) goto L_0x008b
            r1.close()
        L_0x008b:
            goto L_0x00c4
        L_0x008c:
            r2 = move-exception
            if (r1 == 0) goto L_0x0097
            r1.close()     // Catch:{ all -> 0x0093 }
            goto L_0x0097
        L_0x0093:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x0097:
            throw r2
        L_0x0098:
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r1 = r4.mRcsScheduler
            java.net.URL r2 = r6.resourceURL
            java.lang.String r2 = r2.toString()
            android.database.Cursor r1 = r1.queryRCSBufferDBwithResUrl(r2)
            if (r1 == 0) goto L_0x00be
            boolean r2 = r1.moveToFirst()     // Catch:{ all -> 0x00b2 }
            if (r2 == 0) goto L_0x00be
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r2 = r4.mRcsScheduler     // Catch:{ all -> 0x00b2 }
            r2.onNmsEventDeletedObjBufferDbRcsAvailableUsingUrl(r1, r6, r7)     // Catch:{ all -> 0x00b2 }
            goto L_0x00be
        L_0x00b2:
            r2 = move-exception
            if (r1 == 0) goto L_0x00bd
            r1.close()     // Catch:{ all -> 0x00b9 }
            goto L_0x00bd
        L_0x00b9:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x00bd:
            throw r2
        L_0x00be:
            if (r1 == 0) goto L_0x00c3
            r1.close()
        L_0x00c3:
        L_0x00c4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(android.database.Cursor, com.sec.internal.omanetapi.nms.data.DeletedObject, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void handleExpiredObject(DeletedObject objt) {
        Cursor summaryCs = this.mSummaryQuery.querySummaryDBwithResUrl(objt.resourceURL.toString());
        if (summaryCs != null) {
            try {
                if (summaryCs.moveToFirst()) {
                    int status = summaryCs.getInt(summaryCs.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION));
                    String str = TAG;
                    Log.d(str, "handleExpiredObject, Status:" + status);
                    if (status != CloudMessageBufferDBConstants.ActionStatusFlag.Deleted.getId()) {
                        onNmsEventExpiredObjSummaryDbAvailableUsingUrl(summaryCs, objt);
                        this.mSummaryQuery.deleteSummaryDBwithResUrl(objt.resourceURL.toString());
                    } else if (summaryCs != null) {
                        summaryCs.close();
                        return;
                    } else {
                        return;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (summaryCs != null) {
            summaryCs.close();
            return;
        }
        return;
        throw th;
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ba A[SYNTHETIC, Splitter:B:29:0x00ba] */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x010e A[SYNTHETIC, Splitter:B:50:0x010e] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0161 A[SYNTHETIC, Splitter:B:71:0x0161] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0173  */
    /* JADX WARNING: Removed duplicated region for block: B:95:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject r11, boolean r12) {
        /*
            r10 = this;
            java.net.URL r0 = r11.resourceURL
            java.lang.String r0 = r0.toString()
            java.lang.String r0 = com.sec.internal.ims.cmstore.utils.Util.getLineTelUriFromObjUrl(r0)
            r1 = 0
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r2 = r10.mSummaryQuery
            java.net.URL r3 = r11.resourceURL
            java.lang.String r3 = r3.toString()
            android.database.Cursor r2 = r2.querySummaryDBwithResUrl(r3)
            if (r2 == 0) goto L_0x0053
            boolean r3 = r2.moveToFirst()     // Catch:{ all -> 0x0177 }
            if (r3 == 0) goto L_0x0053
            java.lang.String r3 = "syncaction"
            int r3 = r2.getColumnIndexOrThrow(r3)     // Catch:{ all -> 0x0177 }
            int r3 = r2.getInt(r3)     // Catch:{ all -> 0x0177 }
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0177 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x0177 }
            r5.<init>()     // Catch:{ all -> 0x0177 }
            java.lang.String r6 = "handleCloudNotifyDeletedObj, Status:"
            r5.append(r6)     // Catch:{ all -> 0x0177 }
            r5.append(r3)     // Catch:{ all -> 0x0177 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x0177 }
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0177 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r4 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Deleted     // Catch:{ all -> 0x0177 }
            int r4 = r4.getId()     // Catch:{ all -> 0x0177 }
            if (r3 != r4) goto L_0x004e
            if (r2 == 0) goto L_0x004d
            r2.close()
        L_0x004d:
            return
        L_0x004e:
            r10.onNmsEventDeletedObjSummaryDbAvailableUsingUrl(r2, r11, r12)     // Catch:{ all -> 0x0177 }
            goto L_0x0171
        L_0x0053:
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r3 = r10.mSummaryQuery     // Catch:{ all -> 0x0177 }
            r4 = 0
            r3.insertNmsEventDeletedObjToSummaryDB(r11, r4)     // Catch:{ all -> 0x0177 }
            com.sec.internal.ims.cmstore.syncschedulers.MultiLineScheduler r3 = r10.mMultiLnScheduler     // Catch:{ all -> 0x0177 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.DEFAULT     // Catch:{ all -> 0x0177 }
            int r3 = r3.getLineInitSyncStatus(r0, r4)     // Catch:{ all -> 0x0177 }
            com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType r4 = com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType.INITIAL_SYNC_COMPLETE     // Catch:{ all -> 0x0177 }
            int r4 = r4.getId()     // Catch:{ all -> 0x0177 }
            if (r3 == r4) goto L_0x0076
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0177 }
            java.lang.String r5 = "initial sync not complete yet, buffer the NMS events until initial sync is finished"
            android.util.Log.d(r4, r5)     // Catch:{ all -> 0x0177 }
            if (r2 == 0) goto L_0x0075
            r2.close()
        L_0x0075:
            return
        L_0x0076:
            java.lang.String r4 = r11.correlationId     // Catch:{ all -> 0x0177 }
            java.lang.String r5 = "did not find buffer item to delete"
            if (r4 == 0) goto L_0x00ca
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0177 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0177 }
            r6.<init>()     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = "handleCloudNotifyDeletedObj RCS CloudUpdate: "
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = r11.correlationId     // Catch:{ all -> 0x0177 }
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0177 }
            android.util.Log.d(r4, r6)     // Catch:{ all -> 0x0177 }
            r6 = -1
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r4 = r10.mRcsScheduler     // Catch:{ all -> 0x0177 }
            java.lang.String r8 = r11.correlationId     // Catch:{ all -> 0x0177 }
            android.database.Cursor r4 = r4.searchIMFTBufferUsingImdn(r8, r0)     // Catch:{ all -> 0x0177 }
            if (r4 == 0) goto L_0x00b3
            boolean r8 = r4.moveToFirst()     // Catch:{ all -> 0x00be }
            if (r8 == 0) goto L_0x00b3
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r8 = r10.mSummaryQuery     // Catch:{ all -> 0x00be }
            r9 = 1
            r8.updateSummaryDbUsingMessageType(r6, r9)     // Catch:{ all -> 0x00be }
            com.sec.internal.ims.cmstore.syncschedulers.RcsScheduler r8 = r10.mRcsScheduler     // Catch:{ all -> 0x00be }
            r8.onNmsEventDeletedObjBufferDbRcsAvailableUsingImdnId(r4, r11, r12)     // Catch:{ all -> 0x00be }
            r1 = 1
            goto L_0x00b8
        L_0x00b3:
            java.lang.String r8 = TAG     // Catch:{ all -> 0x00be }
            android.util.Log.d(r8, r5)     // Catch:{ all -> 0x00be }
        L_0x00b8:
            if (r4 == 0) goto L_0x00ca
            r4.close()     // Catch:{ all -> 0x0177 }
            goto L_0x00ca
        L_0x00be:
            r5 = move-exception
            if (r4 == 0) goto L_0x00c9
            r4.close()     // Catch:{ all -> 0x00c5 }
            goto L_0x00c9
        L_0x00c5:
            r8 = move-exception
            r5.addSuppressed(r8)     // Catch:{ all -> 0x0177 }
        L_0x00c9:
            throw r5     // Catch:{ all -> 0x0177 }
        L_0x00ca:
            if (r1 != 0) goto L_0x011e
            java.lang.String r4 = r11.correlationId     // Catch:{ all -> 0x0177 }
            if (r4 == 0) goto L_0x011e
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0177 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0177 }
            r6.<init>()     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = "handleCloudNotifyDeletedObj MMS CloudUpdate: "
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = r11.correlationId     // Catch:{ all -> 0x0177 }
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0177 }
            android.util.Log.d(r4, r6)     // Catch:{ all -> 0x0177 }
            r6 = -1
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r4 = r10.mMmsScheduler     // Catch:{ all -> 0x0177 }
            java.lang.String r8 = r11.correlationId     // Catch:{ all -> 0x0177 }
            android.database.Cursor r4 = r4.searchMMsPduBufferUsingCorrelationId(r8)     // Catch:{ all -> 0x0177 }
            if (r4 == 0) goto L_0x0107
            boolean r8 = r4.moveToFirst()     // Catch:{ all -> 0x0112 }
            if (r8 == 0) goto L_0x0107
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r8 = r10.mSummaryQuery     // Catch:{ all -> 0x0112 }
            r9 = 4
            r8.updateSummaryDbUsingMessageType(r6, r9)     // Catch:{ all -> 0x0112 }
            com.sec.internal.ims.cmstore.syncschedulers.MmsScheduler r8 = r10.mMmsScheduler     // Catch:{ all -> 0x0112 }
            r8.onNmsEventDeletedObjMmsBufferDbAvailableUsingCorrId(r4, r11, r12)     // Catch:{ all -> 0x0112 }
            r1 = 1
            goto L_0x010c
        L_0x0107:
            java.lang.String r8 = TAG     // Catch:{ all -> 0x0112 }
            android.util.Log.d(r8, r5)     // Catch:{ all -> 0x0112 }
        L_0x010c:
            if (r4 == 0) goto L_0x011e
            r4.close()     // Catch:{ all -> 0x0177 }
            goto L_0x011e
        L_0x0112:
            r5 = move-exception
            if (r4 == 0) goto L_0x011d
            r4.close()     // Catch:{ all -> 0x0119 }
            goto L_0x011d
        L_0x0119:
            r8 = move-exception
            r5.addSuppressed(r8)     // Catch:{ all -> 0x0177 }
        L_0x011d:
            throw r5     // Catch:{ all -> 0x0177 }
        L_0x011e:
            if (r1 != 0) goto L_0x0171
            java.lang.String r4 = r11.correlationTag     // Catch:{ all -> 0x0177 }
            if (r4 == 0) goto L_0x0171
            java.lang.String r4 = TAG     // Catch:{ all -> 0x0177 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x0177 }
            r6.<init>()     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = "handleCloudNotifyChangedObj: "
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r7 = r11.correlationTag     // Catch:{ all -> 0x0177 }
            r6.append(r7)     // Catch:{ all -> 0x0177 }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x0177 }
            android.util.Log.d(r4, r6)     // Catch:{ all -> 0x0177 }
            r6 = -1
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r4 = r10.mSmsScheduler     // Catch:{ all -> 0x0177 }
            java.lang.String r8 = r11.correlationTag     // Catch:{ all -> 0x0177 }
            android.database.Cursor r4 = r4.searchUnSyncedSMSBufferUsingCorrelationTag(r8)     // Catch:{ all -> 0x0177 }
            if (r4 == 0) goto L_0x015a
            boolean r8 = r4.moveToFirst()     // Catch:{ all -> 0x0165 }
            if (r8 == 0) goto L_0x015a
            com.sec.internal.ims.cmstore.querybuilders.SummaryQueryBuilder r5 = r10.mSummaryQuery     // Catch:{ all -> 0x0165 }
            r8 = 3
            r5.updateSummaryDbUsingMessageType(r6, r8)     // Catch:{ all -> 0x0165 }
            com.sec.internal.ims.cmstore.syncschedulers.SmsScheduler r5 = r10.mSmsScheduler     // Catch:{ all -> 0x0165 }
            r5.onNmsEventDeletedObjSmsBufferDbAvailableUsingCorrTag(r4, r11, r12)     // Catch:{ all -> 0x0165 }
            goto L_0x015f
        L_0x015a:
            java.lang.String r8 = TAG     // Catch:{ all -> 0x0165 }
            android.util.Log.d(r8, r5)     // Catch:{ all -> 0x0165 }
        L_0x015f:
            if (r4 == 0) goto L_0x0171
            r4.close()     // Catch:{ all -> 0x0177 }
            goto L_0x0171
        L_0x0165:
            r5 = move-exception
            if (r4 == 0) goto L_0x0170
            r4.close()     // Catch:{ all -> 0x016c }
            goto L_0x0170
        L_0x016c:
            r8 = move-exception
            r5.addSuppressed(r8)     // Catch:{ all -> 0x0177 }
        L_0x0170:
            throw r5     // Catch:{ all -> 0x0177 }
        L_0x0171:
            if (r2 == 0) goto L_0x0176
            r2.close()
        L_0x0176:
            return
        L_0x0177:
            r3 = move-exception
            if (r2 == 0) goto L_0x0182
            r2.close()     // Catch:{ all -> 0x017e }
            goto L_0x0182
        L_0x017e:
            r4 = move-exception
            r3.addSuppressed(r4)
        L_0x0182:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.handleCloudNotifyDeletedObj(com.sec.internal.omanetapi.nms.data.DeletedObject, boolean):void");
    }

    /* access modifiers changed from: protected */
    public void onHandlePendingNmsEvent() {
        BufferDBChangeParamList downloadlist = new BufferDBChangeParamList();
        Cursor nmsEventCusor = this.mSummaryQuery.queryAllPendingNmsEventInSummaryDB();
        if (nmsEventCusor != null) {
            try {
                if (nmsEventCusor.moveToFirst()) {
                    Log.d(TAG, "NmsEvent sync");
                    do {
                        downloadlist.mChangelst.add(new BufferDBChangeParam(7, (long) nmsEventCusor.getInt(nmsEventCusor.getColumnIndexOrThrow(CloudMessageProviderContract.BufferDBExtensionBase.BUFFERDBID)), false, (String) null));
                    } while (nmsEventCusor.moveToNext() != 0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (nmsEventCusor != null) {
            nmsEventCusor.close();
        }
        if (downloadlist.mChangelst.size() > 0) {
            this.mDeviceDataChangeListener.sendDeviceNormalSyncDownload(downloadlist);
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void startGoForwardSyncDbCopyTask() {
        ContentValues cvFlags = new ContentValues();
        cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.None.getId()));
        cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.Done.getId()));
        Cursor smsCursor = this.mSmsScheduler.queryDeltaSMSfromTelephony();
        if (smsCursor != null) {
            try {
                if (smsCursor.moveToFirst()) {
                    Log.d(TAG, "SMS DB loading");
                    this.mSmsScheduler.insertToSMSBufferDB(smsCursor, cvFlags, true);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (smsCursor != null) {
            smsCursor.close();
        }
        Cursor pduCursor = this.mMmsScheduler.queryDeltaMMSPduFromTelephonyDb();
        if (pduCursor != null) {
            try {
                if (pduCursor.moveToFirst()) {
                    Log.d(TAG, "MMS DB loading");
                    this.mMmsScheduler.insertToMMSPDUBufferDB(pduCursor, cvFlags, true);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (pduCursor != null) {
            pduCursor.close();
        }
        this.mSmsScheduler.syncReadSmsFromTelephony();
        this.mMmsScheduler.syncReadMmsFromTelephony();
        setBufferDBLoaded(true);
        return;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void cleanAllBufferDB() {
        this.mSmsScheduler.cleanAllBufferDB();
        this.mMmsScheduler.cleanAllBufferDB();
        this.mRcsScheduler.cleanAllBufferDB();
        setBufferDBLoaded(false);
    }

    /* access modifiers changed from: protected */
    public void startInitialSyncDBCopyTask() {
        cleanAllBufferDB();
        ContentValues cvFlags = new ContentValues();
        this.mMultiLnScheduler.insertNewLine(CloudMessagePreferenceManager.getInstance().getUserTelCtn(), SyncMsgType.DEFAULT);
        cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCDIRECTION, Integer.valueOf(CloudMessageBufferDBConstants.ActionStatusFlag.Insert.getId()));
        cvFlags.put(CloudMessageProviderContract.BufferDBExtensionBase.SYNCACTION, Integer.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendCloud.getId()));
        Cursor smsCursor = this.mSmsScheduler.queryAllSMSfromTelephony();
        if (smsCursor != null) {
            try {
                if (smsCursor.moveToFirst()) {
                    Log.d(TAG, "SMS DB loading");
                    this.mSmsScheduler.insertToSMSBufferDB(smsCursor, cvFlags, false);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (smsCursor != null) {
            smsCursor.close();
        }
        Cursor pduCursor = this.mMmsScheduler.queryAllMMSPduFromTelephonyDb();
        if (pduCursor != null) {
            try {
                if (pduCursor.moveToFirst()) {
                    Log.d(TAG, "MMS DB loading");
                    this.mMmsScheduler.insertToMMSPDUBufferDB(pduCursor, cvFlags, false);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (pduCursor != null) {
            pduCursor.close();
        }
        Cursor rcsCursor = this.mRcsScheduler.queryAllSession();
        if (rcsCursor != null) {
            try {
                if (rcsCursor.moveToFirst()) {
                    Log.d(TAG, "RCS DB loading");
                    this.mRcsScheduler.insertAllSessionToRCSSessionBufferDB(rcsCursor);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (rcsCursor != null) {
            rcsCursor.close();
        }
        setBufferDBLoaded(true);
        return;
        throw th;
        throw th;
        throw th;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getTableIndex(java.lang.String r4, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag r5) {
        /*
            r3 = this;
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getTableIndex, Type: "
            r1.append(r2)
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            r0 = -1
            java.lang.String r1 = r4.toUpperCase()
            int r2 = r1.hashCode()
            switch(r2) {
                case -1511670668: goto L_0x00af;
                case -873347853: goto L_0x00a4;
                case 2254: goto L_0x009a;
                case 69373: goto L_0x0090;
                case 76467: goto L_0x0086;
                case 79221: goto L_0x007b;
                case 82233: goto L_0x0071;
                case 2067288: goto L_0x0067;
                case 310666545: goto L_0x005c;
                case 408556937: goto L_0x0051;
                case 417047728: goto L_0x0046;
                case 988049465: goto L_0x003a;
                case 1551214263: goto L_0x002f;
                case 2062991267: goto L_0x0024;
                default: goto L_0x0022;
            }
        L_0x0022:
            goto L_0x00ba
        L_0x0024:
            java.lang.String r2 = "MSG_ALL"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 5
            goto L_0x00bb
        L_0x002f:
            java.lang.String r2 = "VVMDATA"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 7
            goto L_0x00bb
        L_0x003a:
            java.lang.String r2 = "GREETING"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 13
            goto L_0x00bb
        L_0x0046:
            java.lang.String r2 = "CALLLOGDATA"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 6
            goto L_0x00bb
        L_0x0051:
            java.lang.String r2 = "PROFILE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 9
            goto L_0x00bb
        L_0x005c:
            java.lang.String r2 = "VOICEMAILTOTEXT"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 10
            goto L_0x00bb
        L_0x0067:
            java.lang.String r2 = "CHAT"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 2
            goto L_0x00bb
        L_0x0071:
            java.lang.String r2 = "SMS"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 0
            goto L_0x00bb
        L_0x007b:
            java.lang.String r2 = "PIN"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 8
            goto L_0x00bb
        L_0x0086:
            java.lang.String r2 = "MMS"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 1
            goto L_0x00bb
        L_0x0090:
            java.lang.String r2 = "FAX"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 4
            goto L_0x00bb
        L_0x009a:
            java.lang.String r2 = "FT"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 3
            goto L_0x00bb
        L_0x00a4:
            java.lang.String r2 = "ACTIVATE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 11
            goto L_0x00bb
        L_0x00af:
            java.lang.String r2 = "DEACTIVATE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0022
            r1 = 12
            goto L_0x00bb
        L_0x00ba:
            r1 = -1
        L_0x00bb:
            switch(r1) {
                case 0: goto L_0x00e7;
                case 1: goto L_0x00e5;
                case 2: goto L_0x00e3;
                case 3: goto L_0x00e3;
                case 4: goto L_0x00e0;
                case 5: goto L_0x00de;
                case 6: goto L_0x00db;
                case 7: goto L_0x00d8;
                case 8: goto L_0x00d5;
                case 9: goto L_0x00d2;
                case 10: goto L_0x00d2;
                case 11: goto L_0x00d2;
                case 12: goto L_0x00d2;
                case 13: goto L_0x00cf;
                default: goto L_0x00be;
            }
        L_0x00be:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.StartFullSync
            boolean r1 = r1.equals(r5)
            if (r1 != 0) goto L_0x00e9
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag r1 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag.StopSync
            boolean r1 = r1.equals(r5)
            if (r1 == 0) goto L_0x00ea
            goto L_0x00e9
        L_0x00cf:
            r0 = 18
            goto L_0x00ea
        L_0x00d2:
            r0 = 20
            goto L_0x00ea
        L_0x00d5:
            r0 = 19
            goto L_0x00ea
        L_0x00d8:
            r0 = 17
            goto L_0x00ea
        L_0x00db:
            r0 = 16
            goto L_0x00ea
        L_0x00de:
            r0 = 0
            goto L_0x00ea
        L_0x00e0:
            r0 = 21
            goto L_0x00ea
        L_0x00e3:
            r0 = 1
            goto L_0x00ea
        L_0x00e5:
            r0 = 4
            goto L_0x00ea
        L_0x00e7:
            r0 = 3
            goto L_0x00ea
        L_0x00e9:
            r0 = 0
        L_0x00ea:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.getTableIndex(java.lang.String, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag):int");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.sec.internal.ims.cmstore.params.ParamVvmUpdate getVvmParam(com.google.gson.JsonElement r9, java.lang.String r10, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag r11, int r12) {
        /*
            r8 = this;
            r0 = 0
            java.lang.String r1 = r10.toUpperCase()
            int r2 = r1.hashCode()
            r3 = 5
            r4 = 4
            r5 = 3
            r6 = 2
            r7 = 1
            switch(r2) {
                case -1511670668: goto L_0x0044;
                case -873347853: goto L_0x003a;
                case 79221: goto L_0x0030;
                case 310666545: goto L_0x0026;
                case 408556937: goto L_0x001c;
                case 988049465: goto L_0x0012;
                default: goto L_0x0011;
            }
        L_0x0011:
            goto L_0x004e
        L_0x0012:
            java.lang.String r2 = "GREETING"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = r5
            goto L_0x004f
        L_0x001c:
            java.lang.String r2 = "PROFILE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = r7
            goto L_0x004f
        L_0x0026:
            java.lang.String r2 = "VOICEMAILTOTEXT"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = r6
            goto L_0x004f
        L_0x0030:
            java.lang.String r2 = "PIN"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = 0
            goto L_0x004f
        L_0x003a:
            java.lang.String r2 = "ACTIVATE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = r4
            goto L_0x004f
        L_0x0044:
            java.lang.String r2 = "DEACTIVATE"
            boolean r1 = r1.equals(r2)
            if (r1 == 0) goto L_0x0011
            r1 = r3
            goto L_0x004f
        L_0x004e:
            r1 = -1
        L_0x004f:
            if (r1 == 0) goto L_0x0093
            if (r1 == r7) goto L_0x0088
            if (r1 == r6) goto L_0x007d
            if (r1 == r5) goto L_0x0072
            if (r1 == r4) goto L_0x0067
            if (r1 == r3) goto L_0x005c
            goto L_0x009e
        L_0x005c:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.DEACTIVATE
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
            goto L_0x009e
        L_0x0067:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.ACTIVATE
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
            goto L_0x009e
        L_0x0072:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.GREETING
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
            goto L_0x009e
        L_0x007d:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
            goto L_0x009e
        L_0x0088:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.FULLPROFILE
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
            goto L_0x009e
        L_0x0093:
            java.lang.String r1 = r9.toString()
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate$VvmTypeChange r2 = com.sec.internal.ims.cmstore.params.ParamVvmUpdate.VvmTypeChange.PIN
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r0 = r8.getVvmChangeParam(r1, r12, r2)
        L_0x009e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.getVvmParam(com.google.gson.JsonElement, java.lang.String, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag, int):com.sec.internal.ims.cmstore.params.ParamVvmUpdate");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d1 A[SYNTHETIC, Splitter:B:34:0x00d1] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fc A[SYNTHETIC, Splitter:B:44:0x00fc] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0127 A[SYNTHETIC, Splitter:B:54:0x0127] */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x015d A[SYNTHETIC, Splitter:B:64:0x015d] */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0188 A[SYNTHETIC, Splitter:B:74:0x0188] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01b0 A[SYNTHETIC, Splitter:B:85:0x01b0] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01b8 A[SYNTHETIC, Splitter:B:88:0x01b8] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.cmstore.params.ParamAppJsonValueList decodeJson(java.lang.String r36, java.lang.String r37, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.MsgOperationFlag r38) {
        /*
            r35 = this;
            r1 = r35
            r13 = r38
            java.lang.String r0 = "correlationId"
            java.lang.String r14 = "correlationTag"
            java.lang.String r15 = "preferred_line"
            java.lang.String r11 = "chatid"
            java.lang.String r10 = "id"
            java.lang.String r9 = "type"
            java.lang.String r8 = "islocalonly"
            com.google.gson.JsonParser r2 = new com.google.gson.JsonParser
            r2.<init>()
            r7 = r2
            com.sec.internal.ims.cmstore.params.ParamAppJsonValueList r2 = new com.sec.internal.ims.cmstore.params.ParamAppJsonValueList
            r2.<init>()
            r6 = r2
            r2 = 0
            r16 = 0
            r5 = r37
            com.google.gson.JsonElement r3 = r7.parse(r5)     // Catch:{ Exception -> 0x021e }
            r17 = r3
            boolean r2 = r17.isJsonArray()     // Catch:{ Exception -> 0x0216 }
            if (r2 == 0) goto L_0x0210
            com.google.gson.JsonArray r2 = r17.getAsJsonArray()     // Catch:{ Exception -> 0x0216 }
            r4 = r2
            r2 = 0
            r3 = r2
        L_0x0038:
            int r2 = r4.size()     // Catch:{ Exception -> 0x0216 }
            if (r3 >= r2) goto L_0x0207
            com.google.gson.JsonElement r2 = r4.get(r3)     // Catch:{ Exception -> 0x0216 }
            java.lang.String r12 = ""
            r18 = 0
            r19 = 0
            r20 = 0
            r21 = 0
            r22 = 0
            r23 = 0
            r24 = r3
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r8)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x009d
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r8)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 != 0) goto L_0x0091
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r8)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            r25 = r4
            java.lang.String r4 = "true"
            boolean r4 = r4.equalsIgnoreCase(r3)     // Catch:{ Exception -> 0x0094 }
            if (r4 == 0) goto L_0x009f
            r23 = r0
            r29 = r6
            r30 = r7
            r31 = r8
            r32 = r9
            r33 = r10
            r34 = r11
            goto L_0x01eb
        L_0x0091:
            r25 = r4
            goto L_0x009f
        L_0x0094:
            r0 = move-exception
            r29 = r6
            r30 = r7
            r2 = r17
            goto L_0x0223
        L_0x009d:
            r25 = r4
        L_0x009f:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r9)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x00c6
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r9)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 != 0) goto L_0x00c6
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r9)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            r12 = r3
            r4 = r12
            goto L_0x00c7
        L_0x00c6:
            r4 = r12
        L_0x00c7:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r10)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x00ef
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r10)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 == 0) goto L_0x00e0
            goto L_0x00ef
        L_0x00e0:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r10)     // Catch:{ Exception -> 0x0094 }
            int r3 = r3.getAsInt()     // Catch:{ Exception -> 0x0094 }
            r20 = r3
            goto L_0x00f2
        L_0x00ef:
            r3 = 0
            r20 = r3
        L_0x00f2:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r11)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x011a
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r11)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 == 0) goto L_0x010b
            goto L_0x011a
        L_0x010b:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r11)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            r22 = r3
            goto L_0x011d
        L_0x011a:
            r3 = 0
            r22 = r3
        L_0x011d:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r15)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x0149
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r15)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 == 0) goto L_0x0136
            goto L_0x0149
        L_0x0136:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r15)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = com.sec.internal.ims.cmstore.utils.Util.getTelUri(r3)     // Catch:{ Exception -> 0x0094 }
            r21 = r3
            goto L_0x0153
        L_0x0149:
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getInstance()     // Catch:{ Exception -> 0x0216 }
            java.lang.String r3 = r3.getUserTelCtn()     // Catch:{ Exception -> 0x0216 }
            r21 = r3
        L_0x0153:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r14)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x017b
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r14)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 == 0) goto L_0x016c
            goto L_0x017b
        L_0x016c:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r14)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            r18 = r3
            goto L_0x017e
        L_0x017b:
            r3 = 0
            r18 = r3
        L_0x017e:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0216 }
            com.google.gson.JsonElement r3 = r3.get(r0)     // Catch:{ Exception -> 0x0216 }
            if (r3 == 0) goto L_0x01a6
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r0)     // Catch:{ Exception -> 0x0094 }
            boolean r3 = r3.isJsonNull()     // Catch:{ Exception -> 0x0094 }
            if (r3 == 0) goto L_0x0197
            goto L_0x01a6
        L_0x0197:
            com.google.gson.JsonObject r3 = r2.getAsJsonObject()     // Catch:{ Exception -> 0x0094 }
            com.google.gson.JsonElement r3 = r3.get(r0)     // Catch:{ Exception -> 0x0094 }
            java.lang.String r3 = r3.getAsString()     // Catch:{ Exception -> 0x0094 }
            r19 = r3
            goto L_0x01a9
        L_0x01a6:
            r3 = 0
            r19 = r3
        L_0x01a9:
            int r3 = r1.getTableIndex(r4, r13)     // Catch:{ Exception -> 0x0216 }
            r12 = -1
            if (r3 != r12) goto L_0x01b8
            java.lang.String r0 = TAG     // Catch:{ Exception -> 0x0094 }
            java.lang.String r8 = "decodeJson: Invalid tableindex"
            android.util.Log.e(r0, r8)     // Catch:{ Exception -> 0x0094 }
            return r16
        L_0x01b8:
            com.sec.internal.ims.cmstore.params.ParamVvmUpdate r12 = r1.getVvmParam(r2, r4, r13, r3)     // Catch:{ Exception -> 0x0216 }
            r23 = r0
            java.util.ArrayList<com.sec.internal.ims.cmstore.params.ParamAppJsonValue> r0 = r6.mOperationList     // Catch:{ Exception -> 0x0216 }
            com.sec.internal.ims.cmstore.params.ParamAppJsonValue r1 = new com.sec.internal.ims.cmstore.params.ParamAppJsonValue     // Catch:{ Exception -> 0x0216 }
            r26 = r2
            r2 = r1
            r27 = r3
            r3 = r36
            r28 = r4
            r5 = r27
            r29 = r6
            r6 = r20
            r30 = r7
            r7 = r22
            r31 = r8
            r8 = r18
            r32 = r9
            r9 = r19
            r33 = r10
            r10 = r38
            r34 = r11
            r11 = r21
            r2.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11, r12)     // Catch:{ Exception -> 0x0203 }
            r0.add(r1)     // Catch:{ Exception -> 0x0203 }
        L_0x01eb:
            int r3 = r24 + 1
            r1 = r35
            r5 = r37
            r0 = r23
            r4 = r25
            r6 = r29
            r7 = r30
            r8 = r31
            r9 = r32
            r10 = r33
            r11 = r34
            goto L_0x0038
        L_0x0203:
            r0 = move-exception
            r2 = r17
            goto L_0x0223
        L_0x0207:
            r24 = r3
            r25 = r4
            r29 = r6
            r30 = r7
            goto L_0x0214
        L_0x0210:
            r29 = r6
            r30 = r7
        L_0x0214:
            return r29
        L_0x0216:
            r0 = move-exception
            r29 = r6
            r30 = r7
            r2 = r17
            goto L_0x0223
        L_0x021e:
            r0 = move-exception
            r29 = r6
            r30 = r7
        L_0x0223:
            java.lang.String r1 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "JsonSyntaxException: "
            r3.append(r4)
            java.lang.String r4 = r0.toString()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r1, r3)
            return r16
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferDBHelper.decodeJson(java.lang.String, java.lang.String, com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$MsgOperationFlag):com.sec.internal.ims.cmstore.params.ParamAppJsonValueList");
    }

    private ParamVvmUpdate getVvmChangeParam(String elemt, int tableIndex, ParamVvmUpdate.VvmTypeChange vvmchange) {
        String str = TAG;
        Log.d(str, "getVvmChangeParam: " + elemt + " tableindex: " + tableIndex + " VvmTypeChange: " + vvmchange);
        try {
            ParamVvmUpdate update = (ParamVvmUpdate) new Gson().fromJson(elemt, ParamVvmUpdate.class);
            update.mVvmChange = vvmchange;
            update.mLine = Util.getTelUri(update.mLine);
            return update;
        } catch (Exception e) {
            String str2 = TAG;
            Log.e(str2, "getVvmChangeParam: " + e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void handleReceivedMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Received));
    }

    /* access modifiers changed from: protected */
    public void handleSentMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Sent));
    }

    /* access modifiers changed from: protected */
    public void handleReadMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Read));
    }

    /* access modifiers changed from: protected */
    public void handleUnReadMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.UnRead));
    }

    /* access modifiers changed from: protected */
    public void handleDeleteMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Delete));
    }

    /* access modifiers changed from: protected */
    public void handleUploadMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Upload));
    }

    /* access modifiers changed from: protected */
    public void handleDownloadMessageJson(String json) {
        processParamAppJsonList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.Download));
    }

    /* access modifiers changed from: protected */
    public void handleBufferDbReadMessageJson(String json) {
    }

    /* access modifiers changed from: protected */
    public void handleWipeOutMessageJson(String json) {
        processWipeOutList(decodeJson((String) null, json, CloudMessageBufferDBConstants.MsgOperationFlag.WipeOut));
    }

    private void processParamAppJsonList(ParamAppJsonValueList list) {
        String str = TAG;
        IMSLog.s(str, "processParamAppJsonList: " + list);
        if (list != null && list.mOperationList != null && list.mOperationList.size() >= 1) {
            BufferDBChangeParamList changelist = new BufferDBChangeParamList();
            Iterator<ParamAppJsonValue> it = list.mOperationList.iterator();
            while (it.hasNext()) {
                ParamAppJsonValue param = it.next();
                int i = param.mDataContractType;
                if (i == 1) {
                    this.mRcsScheduler.onAppOperationReceived(param, changelist);
                } else if (i == 3) {
                    this.mSmsScheduler.onAppOperationReceived(param, changelist);
                } else if (i != 4) {
                    switch (i) {
                        case 16:
                            this.mCallLogScheduler.onAppOperationReceived(param, changelist);
                            break;
                        case 17:
                        case 18:
                        case 19:
                        case 20:
                            this.mVVMScheduler.onAppOperationReceived(param, changelist);
                            break;
                        case 21:
                            this.mFaxScheduler.onAppOperationReceived(param, changelist);
                            break;
                    }
                } else {
                    this.mMmsScheduler.onAppOperationReceived(param, changelist);
                }
            }
            if (changelist.mChangelst.size() > 0) {
                this.mDeviceDataChangeListener.sendDeviceUpdate(changelist);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setBufferDBLoaded(boolean isLoaded) {
        this.mBufferDBloaded = isLoaded;
        CloudMessagePreferenceManager.getInstance().saveBufferDbLoaded(this.mBufferDBloaded);
    }

    private void processWipeOutList(ParamAppJsonValueList list) {
        if (list != null && list.mOperationList != null && list.mOperationList.size() >= 1) {
            String str = TAG;
            Log.d(str, "processWipeOutList: " + list);
            Iterator<ParamAppJsonValue> it = list.mOperationList.iterator();
            while (it.hasNext()) {
                String line = it.next().mLine;
                if (CloudMessageProviderContract.DataTypes.MSGAPP_ALL.equalsIgnoreCase(list.mOperationList.get(0).mDataType)) {
                    this.mSmsScheduler.wipeOutData(3, line);
                    this.mMmsScheduler.wipeOutData(4, line);
                    this.mRcsScheduler.wipeOutData(1, line);
                    this.mFaxScheduler.wipeOutData(21, line);
                } else if ("CALLLOGDATA".equalsIgnoreCase(list.mOperationList.get(0).mDataType)) {
                    this.mCallLogScheduler.wipeOutData(16, line);
                } else if ("VVMDATA".equalsIgnoreCase(list.mOperationList.get(0).mDataType)) {
                    this.mVVMScheduler.wipeOutData(17, line);
                    this.mVVMScheduler.wipeOutData(18, line);
                    this.mVVMScheduler.wipeOutData(19, line);
                    this.mVVMScheduler.wipeOutData(20, line);
                }
            }
        }
    }

    private void onNmsEventExpiredObjSummaryDbAvailableUsingUrl(Cursor summaryCs, DeletedObject objt) {
        int type = summaryCs.getInt(summaryCs.getColumnIndexOrThrow("messagetype"));
        String str = TAG;
        Log.d(str, "onNmsEventExpiredObjSummaryDbAvailableUsingUrl(), type: " + type);
        if (!(type == 1 || type == 14)) {
            if (type == 3) {
                this.mSmsScheduler.deleteSMSBufferDBwithResUrl(objt.resourceURL.toString());
                return;
            } else if (type == 4) {
                this.mMmsScheduler.deleteMMSBufferDBwithResUrl(objt.resourceURL.toString());
                return;
            } else if (!(type == 11 || type == 12)) {
                return;
            }
        }
        this.mRcsScheduler.deleteRCSBufferDBwithResUrl(objt.resourceURL.toString());
    }

    /* access modifiers changed from: protected */
    public void appFetchingFailedMsg(String syncDirection) {
        Cursor cs = this.mSmsScheduler.querySMSMessagesBySycnDirection(3, syncDirection);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    this.mSmsScheduler.notifyMsgAppFetchBuffer(cs, 3);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        Cursor cs2 = this.mMmsScheduler.queryMMSMessagesBySycnDirection(4, syncDirection);
        if (cs2 != null) {
            try {
                if (cs2.moveToFirst()) {
                    this.mMmsScheduler.notifyMsgAppFetchBuffer(cs2, 4);
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cs2 != null) {
            cs2.close();
        }
        Cursor cs3 = this.mRcsScheduler.queryRCSMessagesBySycnDirection(1, syncDirection);
        if (cs3 != null) {
            try {
                if (cs3.moveToFirst()) {
                    this.mRcsScheduler.notifyMsgAppFetchBuffer(cs3, 1);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (cs3 != null) {
            cs3.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }

    /* access modifiers changed from: protected */
    public void fetchingPendingMsg() {
        String syncDirection = String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId());
        Cursor cs = this.mSmsScheduler.querySMSMessagesBySycnDirection(3, syncDirection);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    this.mSmsScheduler.msgAppFetchBuffer(cs, CloudMessageProviderContract.ApplicationTypes.MSGDATA, "SMS");
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        Cursor cs2 = this.mMmsScheduler.queryMMSMessagesBySycnDirection(4, syncDirection);
        if (cs2 != null) {
            try {
                if (cs2.moveToFirst()) {
                    this.mMmsScheduler.msgAppFetchBuffer(cs2, CloudMessageProviderContract.ApplicationTypes.MSGDATA, "MMS");
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cs2 != null) {
            cs2.close();
        }
        Cursor cs3 = this.mRcsScheduler.queryRCSMessagesBySycnDirection(1, syncDirection);
        if (cs3 != null) {
            try {
                if (cs3.moveToFirst()) {
                    this.mRcsScheduler.msgAppFetchBuffer(cs3, CloudMessageProviderContract.ApplicationTypes.MSGDATA, CloudMessageProviderContract.DataTypes.CHAT);
                }
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        }
        if (cs3 != null) {
            cs3.close();
            return;
        }
        return;
        throw th;
        throw th;
        throw th;
    }
}
