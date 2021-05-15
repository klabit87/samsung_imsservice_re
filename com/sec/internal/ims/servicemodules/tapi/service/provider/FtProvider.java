package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.ims.servicemodules.im.ImCache;

public class FtProvider extends ContentProvider {
    public static final String AUTHORITY;
    private static final String LOG_TAG = FtProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private ImCache mCache;

    static {
        String authority = FileTransferLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        sUriMatcher.addURI(authority, "filetransfer", 1);
        sUriMatcher.addURI(AUTHORITY, "filetransfer/#", 2);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        this.mCache = ImCache.getInstance();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriKind = sUriMatcher.match(uri);
        if (uriKind == 1) {
            return buildMessagesCursor((Uri) null, projection, selection, selectionArgs, sortOrder);
        }
        if (uriKind == 2) {
            return buildMessagesCursor(uri, projection, selection, selectionArgs, sortOrder);
        }
        Log.d(LOG_TAG, "return null");
        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private Cursor buildMessagesCursor(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String idString = null;
        if (uri == null || (idString = uri.getLastPathSegment()) != null) {
            return fillMessageCursor(idString, projection, selection, selectionArgs, sortOrder);
        }
        Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
        return null;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:83:0x0247=Splitter:B:83:0x0247, B:97:0x02a7=Splitter:B:97:0x02a7} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.database.MatrixCursor fillMessageCursor(java.lang.String r23, java.lang.String[] r24, java.lang.String r25, java.lang.String[] r26, java.lang.String r27) {
        /*
            r22 = this;
            r1 = r22
            r2 = r23
            java.lang.String r3 = "ft_id"
            if (r2 == 0) goto L_0x0046
            boolean r0 = android.text.TextUtils.isEmpty(r25)
            if (r0 == 0) goto L_0x0021
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r4 = "ft_id = "
            r0.append(r4)
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            r4 = r0
            goto L_0x0048
        L_0x0021:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r4 = "("
            r0.append(r4)
            r4 = r25
            r0.append(r4)
            java.lang.String r5 = ") AND "
            r0.append(r5)
            r0.append(r3)
            java.lang.String r5 = " = "
            r0.append(r5)
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            r4 = r0
            goto L_0x0048
        L_0x0046:
            r4 = r25
        L_0x0048:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = " soyeon : selection : "
            r5.append(r6)
            r5.append(r4)
            java.lang.String r6 = " idString:"
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r0, r5)
            com.sec.internal.ims.servicemodules.im.ImCache r0 = r1.mCache
            r5 = r24
            r6 = r26
            r7 = r27
            android.database.Cursor r8 = r0.queryFtMessagesForTapi(r5, r4, r6, r7)
            if (r8 == 0) goto L_0x02a5
            int r0 = r8.getCount()     // Catch:{ all -> 0x02a0 }
            if (r0 != 0) goto L_0x007e
            r17 = r4
            goto L_0x02a7
        L_0x007e:
            java.lang.String[] r0 = r8.getColumnNames()     // Catch:{ all -> 0x02a0 }
            r10 = r0
            android.database.MatrixCursor r0 = new android.database.MatrixCursor     // Catch:{ all -> 0x02a0 }
            r0.<init>(r10)     // Catch:{ all -> 0x02a0 }
            r11 = r0
        L_0x0089:
            boolean r0 = r8.moveToNext()     // Catch:{ all -> 0x02a0 }
            if (r0 == 0) goto L_0x0298
            android.database.MatrixCursor$RowBuilder r0 = r11.newRow()     // Catch:{ all -> 0x02a0 }
            r12 = r0
            int r13 = r10.length     // Catch:{ all -> 0x02a0 }
            r15 = 0
        L_0x0096:
            if (r15 >= r13) goto L_0x028e
            r0 = r10[r15]     // Catch:{ all -> 0x02a0 }
            r25 = r0
            r14 = r25
            int r0 = r8.getColumnIndex(r14)     // Catch:{ all -> 0x02a0 }
            r25 = r0
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x02a0 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x02a0 }
            r9.<init>()     // Catch:{ all -> 0x02a0 }
            r17 = r4
            java.lang.String r4 = "columnName : "
            r9.append(r4)     // Catch:{ all -> 0x02b6 }
            r9.append(r14)     // Catch:{ all -> 0x02b6 }
            java.lang.String r4 = "columnType : "
            r9.append(r4)     // Catch:{ all -> 0x02b6 }
            r4 = r25
            int r5 = r8.getType(r4)     // Catch:{ all -> 0x02b6 }
            r9.append(r5)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = ", columnValue : "
            r9.append(r5)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = r8.getString(r4)     // Catch:{ all -> 0x02b6 }
            r9.append(r5)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = r9.toString()     // Catch:{ all -> 0x02b6 }
            android.util.Log.d(r0, r5)     // Catch:{ all -> 0x02b6 }
            java.lang.String r0 = "state"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x00f2
            java.lang.String r0 = r8.getString(r4)     // Catch:{ all -> 0x02b6 }
            int r5 = r1.transState(r0)     // Catch:{ all -> 0x02b6 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x02b6 }
            r12.add(r9)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x00f2:
            java.lang.String r0 = "reason_code"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x0117
            int r0 = r8.getInt(r4)     // Catch:{ all -> 0x02b6 }
            com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.valueOf((int) r0)     // Catch:{ all -> 0x02b6 }
            com.gsma.services.rcs.filetransfer.FileTransfer$ReasonCode r5 = com.sec.internal.ims.servicemodules.tapi.service.api.FileTransferingServiceImpl.ftCancelReasonTranslator(r5)     // Catch:{ all -> 0x02b6 }
            int r9 = r5.ordinal()     // Catch:{ all -> 0x02b6 }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x02b6 }
            r12.add(r9)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x0117:
            java.lang.String r0 = "read_status"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x014d
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.values()     // Catch:{ all -> 0x02b6 }
            int r5 = r8.getInt(r4)     // Catch:{ all -> 0x02b6 }
            r0 = r0[r5]     // Catch:{ all -> 0x02b6 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r5 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x02b6 }
            if (r5 != r0) goto L_0x013c
            com.gsma.services.rcs.RcsService$ReadStatus r5 = com.gsma.services.rcs.RcsService.ReadStatus.READ     // Catch:{ all -> 0x02b6 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x02b6 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0149
        L_0x013c:
            com.gsma.services.rcs.RcsService$ReadStatus r5 = com.gsma.services.rcs.RcsService.ReadStatus.UNREAD     // Catch:{ all -> 0x02b6 }
            int r5 = r5.ordinal()     // Catch:{ all -> 0x02b6 }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
        L_0x0149:
            r16 = 0
            goto L_0x0282
        L_0x014d:
            java.lang.String r0 = "fileicon_mime_type"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x0164
            java.lang.String r0 = r8.getString(r4)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = com.sec.internal.ims.servicemodules.tapi.service.utils.FileUtils.getContentTypeFromFileName(r0)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x0164:
            java.lang.String r0 = "file_expiration"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 != 0) goto L_0x0227
            java.lang.String r0 = "fileicon_expiration"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x0178
            r16 = 0
            goto L_0x0229
        L_0x0178:
            java.lang.String r0 = "expired_delivery"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            r5 = 1
            if (r0 == 0) goto L_0x0199
            long r18 = r8.getLong(r4)     // Catch:{ all -> 0x02b6 }
            r20 = 0
            int r0 = (r18 > r20 ? 1 : (r18 == r20 ? 0 : -1))
            if (r0 <= 0) goto L_0x018c
            goto L_0x018d
        L_0x018c:
            r5 = 0
        L_0x018d:
            r0 = r5
            java.lang.Integer r5 = java.lang.Integer.valueOf(r0)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x0199:
            java.lang.String r0 = "fileicon"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            if (r0 == 0) goto L_0x01a9
            r5 = 0
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x01a9:
            java.lang.String r0 = "file"
            boolean r0 = r0.equals(r14)     // Catch:{ all -> 0x02b6 }
            r9 = 4
            if (r0 == 0) goto L_0x01e3
            java.lang.String r0 = r8.getString(r4)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = ".tmp"
            boolean r5 = r0.endsWith(r5)     // Catch:{ all -> 0x02b6 }
            if (r5 == 0) goto L_0x01ca
            int r5 = r0.length()     // Catch:{ all -> 0x02b6 }
            int r5 = r5 - r9
            r9 = 0
            java.lang.String r5 = r0.substring(r9, r5)     // Catch:{ all -> 0x02b6 }
            r0 = r5
            goto L_0x01cb
        L_0x01ca:
            r9 = 0
        L_0x01cb:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b6 }
            r5.<init>()     // Catch:{ all -> 0x02b6 }
            java.lang.String r9 = "file://"
            r5.append(r9)     // Catch:{ all -> 0x02b6 }
            r5.append(r0)     // Catch:{ all -> 0x02b6 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            r16 = 0
            goto L_0x0282
        L_0x01e3:
            r16 = 0
            int r0 = r8.getType(r4)     // Catch:{ all -> 0x02b6 }
            if (r0 == r5) goto L_0x021b
            r5 = 2
            if (r0 == r5) goto L_0x020f
            r5 = 3
            if (r0 == r5) goto L_0x0206
            if (r0 == r9) goto L_0x01f9
            r5 = 0
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0282
        L_0x01f9:
            float r5 = r8.getFloat(r4)     // Catch:{ all -> 0x02b6 }
            java.lang.Float r5 = java.lang.Float.valueOf(r5)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0282
        L_0x0206:
            java.lang.String r5 = r8.getString(r4)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0282
        L_0x020f:
            float r5 = r8.getFloat(r4)     // Catch:{ all -> 0x02b6 }
            java.lang.Float r5 = java.lang.Float.valueOf(r5)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0282
        L_0x021b:
            long r18 = r8.getLong(r4)     // Catch:{ all -> 0x02b6 }
            java.lang.Long r5 = java.lang.Long.valueOf(r18)     // Catch:{ all -> 0x02b6 }
            r12.add(r5)     // Catch:{ all -> 0x02b6 }
            goto L_0x0282
        L_0x0227:
            r16 = 0
        L_0x0229:
            com.sec.internal.ims.servicemodules.im.ImCache r0 = com.sec.internal.ims.servicemodules.im.ImCache.getInstance()     // Catch:{ all -> 0x02b6 }
            r5 = r0
            r9 = 0
            r18 = -1
            if (r2 == 0) goto L_0x023c
            int r0 = java.lang.Integer.parseInt(r23)     // Catch:{ NumberFormatException -> 0x023a }
            r18 = r0
            goto L_0x0242
        L_0x023a:
            r0 = move-exception
            goto L_0x0245
        L_0x023c:
            int r0 = java.lang.Integer.parseInt(r3)     // Catch:{ NumberFormatException -> 0x023a }
            r18 = r0
        L_0x0242:
            r0 = r18
            goto L_0x0247
        L_0x0245:
            r0 = r18
        L_0x0247:
            com.sec.internal.ims.servicemodules.im.FtMessage r18 = r5.getFtMessage(r0)     // Catch:{ all -> 0x02b6 }
            r9 = r18
            if (r9 == 0) goto L_0x0273
            r18 = r0
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x02b6 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x02b6 }
            r1.<init>()     // Catch:{ all -> 0x02b6 }
            java.lang.String r2 = "FILE_EXPIRATION:"
            r1.append(r2)     // Catch:{ all -> 0x02b6 }
            java.lang.String r2 = r9.getFileExpire()     // Catch:{ all -> 0x02b6 }
            r1.append(r2)     // Catch:{ all -> 0x02b6 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x02b6 }
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x02b6 }
            java.lang.String r0 = r9.getFileExpire()     // Catch:{ all -> 0x02b6 }
            r12.add(r0)     // Catch:{ all -> 0x02b6 }
            goto L_0x0281
        L_0x0273:
            r18 = r0
            java.lang.String r0 = ""
            r12.add(r0)     // Catch:{ all -> 0x02b6 }
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x02b6 }
            java.lang.String r1 = "FILE_EXPIRATION:null"
            android.util.Log.d(r0, r1)     // Catch:{ all -> 0x02b6 }
        L_0x0281:
        L_0x0282:
            int r15 = r15 + 1
            r1 = r22
            r2 = r23
            r5 = r24
            r4 = r17
            goto L_0x0096
        L_0x028e:
            r17 = r4
            r1 = r22
            r2 = r23
            r5 = r24
            goto L_0x0089
        L_0x0298:
            r17 = r4
            if (r8 == 0) goto L_0x029f
            r8.close()
        L_0x029f:
            return r11
        L_0x02a0:
            r0 = move-exception
            r17 = r4
            r1 = r0
            goto L_0x02b8
        L_0x02a5:
            r17 = r4
        L_0x02a7:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x02b6 }
            java.lang.String r1 = "buildMessageCursor: Message not found."
            android.util.Log.e(r0, r1)     // Catch:{ all -> 0x02b6 }
            if (r8 == 0) goto L_0x02b4
            r8.close()
        L_0x02b4:
            r1 = 0
            return r1
        L_0x02b6:
            r0 = move-exception
            r1 = r0
        L_0x02b8:
            if (r8 == 0) goto L_0x02c3
            r8.close()     // Catch:{ all -> 0x02be }
            goto L_0x02c3
        L_0x02be:
            r0 = move-exception
            r2 = r0
            r1.addSuppressed(r2)
        L_0x02c3:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.FtProvider.fillMessageCursor(java.lang.String, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String):android.database.MatrixCursor");
    }

    public int transState(String statusAndDirection) {
        int imDirectionIndex;
        String[] strArr = statusAndDirection.split(";");
        if (strArr.length != 2 || (imDirectionIndex = Integer.parseInt(strArr[1])) < 0 || imDirectionIndex >= ImDirection.values().length) {
            return -1;
        }
        ImDirection direction = ImDirection.values()[imDirectionIndex];
        int stateId = Integer.parseInt(strArr[0]);
        if (!(stateId == 0 || stateId == 1)) {
            if (stateId == 2) {
                return FileTransfer.State.STARTED.ordinal();
            }
            if (stateId == 3) {
                return FileTransfer.State.TRANSFERRED.ordinal();
            }
            if (stateId != 4) {
                if (stateId != 6) {
                    if (stateId != 7) {
                        if (stateId != 9) {
                            return FileTransfer.State.FAILED.ordinal();
                        }
                    }
                }
            }
            return FileTransfer.State.ABORTED.ordinal();
        }
        if (ImDirection.INCOMING == direction) {
            return FileTransfer.State.INVITED.ordinal();
        }
        if (ImDirection.OUTGOING == direction) {
            return FileTransfer.State.INITIATING.ordinal();
        }
        return -1;
    }
}
