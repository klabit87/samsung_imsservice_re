package com.sec.internal.ims.servicemodules.tapi.service.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharingLog;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;

public class GeolocProvider extends ContentProvider {
    private static final String AUTHORITY;
    private static final String LOG_TAG = GeolocProvider.class.getSimpleName();
    private static final int RCSAPI = 1;
    private static final int RCSAPI_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(-1);
    private final String[] MESSAGE_COLUMNS = {"_id", "sharing_id", ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, "content", "mime_type", "direction", "timestamp", "state", "reason_code"};
    private ImCache mCache;

    static {
        String authority = GeolocSharingLog.CONTENT_URI.getAuthority();
        AUTHORITY = authority;
        sUriMatcher.addURI(authority, "geolocshare", 1);
        sUriMatcher.addURI(AUTHORITY, "geolocshare/#", 2);
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
        if (!this.mCache.isLoaded()) {
            Log.e(LOG_TAG, "ImCache is not ready yet.");
            return null;
        }
        int uriKind = sUriMatcher.match(uri);
        if (uriKind == 1) {
            return buildMessagesCursor();
        }
        if (uriKind == 2) {
            return buildMessagesCursor(uri);
        }
        Log.d(LOG_TAG, "return null");
        return null;
    }

    private Cursor buildMessagesCursor() {
        MatrixCursor cursor = new MatrixCursor(this.MESSAGE_COLUMNS);
        fillMessageCursor(cursor, (String) null);
        return cursor;
    }

    private Cursor buildMessagesCursor(Uri uri) {
        String id = uri.getLastPathSegment();
        if (id == null) {
            Log.e(LOG_TAG, "buildMessageCursor: No last segment.");
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(this.MESSAGE_COLUMNS);
        fillMessageCursor(cursor, id);
        if (cursor.getCount() == 0) {
            Log.e(LOG_TAG, "buildMessageCursor: Message not found.");
        }
        return cursor;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0133  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void fillMessageCursor(android.database.MatrixCursor r17, java.lang.String r18) {
        /*
            r16 = this;
            r1 = r16
            java.lang.String r2 = "_id"
            java.lang.String r3 = "chat_id"
            java.lang.String r4 = "remote_uri"
            java.lang.String r5 = "content_type"
            java.lang.String r6 = "direction"
            java.lang.String r7 = "ext_info"
            java.lang.String r8 = "reason"
            java.lang.String r9 = "delivered_timestamp"
            java.lang.String r10 = "state"
            java.lang.String[] r0 = new java.lang.String[]{r2, r3, r4, r5, r6, r7, r8, r9, r10}
            r2 = r0
            java.lang.String r3 = "_id= ? "
            r0 = 1
            java.lang.String[] r4 = new java.lang.String[r0]
            r5 = 0
            r4[r5] = r18
            r6 = 0
            r7 = 0
            if (r18 != 0) goto L_0x0030
            com.sec.internal.ims.servicemodules.im.ImCache r8 = r1.mCache     // Catch:{ all -> 0x012d }
            android.database.Cursor r8 = r8.queryMessages(r2, r7, r7, r7)     // Catch:{ all -> 0x012d }
            r6 = r8
            goto L_0x0037
        L_0x0030:
            com.sec.internal.ims.servicemodules.im.ImCache r8 = r1.mCache     // Catch:{ all -> 0x012d }
            android.database.Cursor r8 = r8.queryMessages(r2, r3, r4, r7)     // Catch:{ all -> 0x012d }
            r6 = r8
        L_0x0037:
            if (r6 == 0) goto L_0x011b
            int r8 = r6.getCount()     // Catch:{ all -> 0x012d }
            if (r8 != 0) goto L_0x0044
            r15 = r1
            r1 = r17
            goto L_0x011e
        L_0x0044:
            r8 = 0
        L_0x0045:
            boolean r9 = r6.moveToNext()     // Catch:{ all -> 0x012d }
            if (r9 == 0) goto L_0x0112
            java.lang.String r9 = "content_type"
            int r9 = r6.getColumnIndexOrThrow(r9)     // Catch:{ all -> 0x012d }
            java.lang.String r9 = r6.getString(r9)     // Catch:{ all -> 0x012d }
            java.lang.String r10 = "ext_info"
            int r10 = r6.getColumnIndexOrThrow(r10)     // Catch:{ all -> 0x012d }
            java.lang.String r10 = r6.getString(r10)     // Catch:{ all -> 0x012d }
            java.lang.String r11 = "remote_uri"
            int r11 = r6.getColumnIndexOrThrow(r11)     // Catch:{ all -> 0x012d }
            java.lang.String r11 = r6.getString(r11)     // Catch:{ all -> 0x012d }
            java.lang.String r12 = "direction"
            int r12 = r6.getColumnIndexOrThrow(r12)     // Catch:{ all -> 0x012d }
            int r12 = r6.getInt(r12)     // Catch:{ all -> 0x012d }
            if (r9 == 0) goto L_0x010b
            java.lang.String r13 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r13 = r9.equals(r13)     // Catch:{ all -> 0x012d }
            if (r13 == 0) goto L_0x0107
            if (r10 == 0) goto L_0x0103
            r13 = 9
            java.lang.Object[] r13 = new java.lang.Object[r13]     // Catch:{ all -> 0x012d }
            int r14 = r8 + 1
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x012d }
            r13[r5] = r8     // Catch:{ all -> 0x012d }
            java.lang.String r8 = "_id"
            int r8 = r6.getColumnIndexOrThrow(r8)     // Catch:{ all -> 0x012d }
            java.lang.String r8 = r6.getString(r8)     // Catch:{ all -> 0x012d }
            java.lang.String r8 = java.lang.String.valueOf(r8)     // Catch:{ all -> 0x012d }
            r13[r0] = r8     // Catch:{ all -> 0x012d }
            r8 = 2
            if (r11 == 0) goto L_0x00a1
            r15 = r11
            goto L_0x00a2
        L_0x00a1:
            r15 = r7
        L_0x00a2:
            java.lang.String r15 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r15)     // Catch:{ all -> 0x012d }
            r13[r8] = r15     // Catch:{ all -> 0x012d }
            r8 = 3
            r13[r8] = r9     // Catch:{ all -> 0x012d }
            r8 = 4
            java.lang.Integer r15 = java.lang.Integer.valueOf(r12)     // Catch:{ all -> 0x012d }
            r13[r8] = r15     // Catch:{ all -> 0x012d }
            r8 = 5
            r13[r8] = r10     // Catch:{ all -> 0x012d }
            r8 = 6
            java.lang.String r15 = "reason"
            int r15 = r6.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x012d }
            int r15 = r6.getInt(r15)     // Catch:{ all -> 0x012d }
            int r15 = r1.transReason(r15)     // Catch:{ all -> 0x012d }
            java.lang.Integer r15 = java.lang.Integer.valueOf(r15)     // Catch:{ all -> 0x012d }
            r13[r8] = r15     // Catch:{ all -> 0x012d }
            r8 = 7
            java.lang.String r15 = "delivered_timestamp"
            int r15 = r6.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x012d }
            int r15 = r6.getInt(r15)     // Catch:{ all -> 0x012d }
            long r0 = (long) r15
            java.lang.Long r0 = java.lang.Long.valueOf(r0)     // Catch:{ all -> 0x00ff }
            r13[r8] = r0     // Catch:{ all -> 0x00ff }
            r0 = 8
            java.lang.String r1 = "state"
            int r1 = r6.getColumnIndexOrThrow(r1)     // Catch:{ all -> 0x00ff }
            int r1 = r6.getInt(r1)     // Catch:{ all -> 0x00ff }
            r15 = r16
            int r1 = r15.transState(r1, r12)     // Catch:{ all -> 0x00fd }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x00fd }
            r13[r0] = r1     // Catch:{ all -> 0x00fd }
            r1 = r17
            r1.addRow(r13)     // Catch:{ all -> 0x012b }
            r8 = r14
            goto L_0x010e
        L_0x00fd:
            r0 = move-exception
            goto L_0x012f
        L_0x00ff:
            r0 = move-exception
            r15 = r16
            goto L_0x012f
        L_0x0103:
            r15 = r1
            r1 = r17
            goto L_0x010e
        L_0x0107:
            r15 = r1
            r1 = r17
            goto L_0x010e
        L_0x010b:
            r15 = r1
            r1 = r17
        L_0x010e:
            r1 = r15
            r0 = 1
            goto L_0x0045
        L_0x0112:
            r15 = r1
            r1 = r17
            if (r6 == 0) goto L_0x011a
            r6.close()
        L_0x011a:
            return
        L_0x011b:
            r15 = r1
            r1 = r17
        L_0x011e:
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x012b }
            java.lang.String r5 = "buildMessageCursor: Message not found."
            android.util.Log.e(r0, r5)     // Catch:{ all -> 0x012b }
            if (r6 == 0) goto L_0x012a
            r6.close()
        L_0x012a:
            return
        L_0x012b:
            r0 = move-exception
            goto L_0x0131
        L_0x012d:
            r0 = move-exception
            r15 = r1
        L_0x012f:
            r1 = r17
        L_0x0131:
            if (r6 == 0) goto L_0x0136
            r6.close()
        L_0x0136:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.provider.GeolocProvider.fillMessageCursor(android.database.MatrixCursor, java.lang.String):void");
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private int transState(int stateId, int direction) {
        int state = GeolocSharing.State.INVITED.ordinal();
        if (!(stateId == 0 || stateId == 1)) {
            if (stateId == 2) {
                return GeolocSharing.State.STARTED.ordinal();
            }
            if (stateId == 3) {
                return GeolocSharing.State.RINGING.ordinal();
            }
            if (stateId != 4) {
                if (stateId != 6) {
                    if (stateId != 7) {
                        return state;
                    }
                }
            }
            return GeolocSharing.State.ABORTED.ordinal();
        }
        if (ImDirection.INCOMING.getId() == direction) {
            return GeolocSharing.State.INVITED.ordinal();
        }
        return ImDirection.OUTGOING.getId() == direction ? GeolocSharing.State.INITIATING.ordinal() : state;
    }

    private int transReason(int reasonId) {
        CancelReason cancelReason = CancelReason.valueOf(reasonId);
        if (cancelReason == null) {
            return GeolocSharing.ReasonCode.UNSPECIFIED.ordinal();
        }
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[cancelReason.ordinal()]) {
            case 1:
                return GeolocSharing.ReasonCode.ABORTED_BY_USER.ordinal();
            case 2:
                return GeolocSharing.ReasonCode.ABORTED_BY_REMOTE.ordinal();
            case 3:
                return GeolocSharing.ReasonCode.ABORTED_BY_SYSTEM.ordinal();
            case 4:
                return GeolocSharing.ReasonCode.REJECTED_BY_REMOTE.ordinal();
            case 5:
                return GeolocSharing.ReasonCode.FAILED_SHARING.ordinal();
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                return GeolocSharing.ReasonCode.FAILED_INITIATION.ordinal();
            case 17:
            case 18:
            case 19:
            case 20:
                return GeolocSharing.ReasonCode.FAILED_SHARING.ordinal();
            default:
                return 0;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.provider.GeolocProvider$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;

        static {
            int[] iArr = new int[CancelReason.values().length];
            $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = iArr;
            try {
                iArr[CancelReason.CANCELED_BY_USER.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_REMOTE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CANCELED_BY_SYSTEM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REJECTED_BY_REMOTE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.TIME_OUT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.TOO_LARGE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NOT_AUTHORIZED.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.ERROR.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONNECTION_RELEASED.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.CONTENT_REACHED_DOWNSIZE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.DEVICE_UNREGISTERED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.FORBIDDEN_NO_RETRY_FALLBACK.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.INVALID_REQUEST.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.LOCALLY_ABORTED.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.LOW_MEMORY.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.NO_RESPONSE.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_BLOCKED.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.REMOTE_USER_INVALID.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[CancelReason.VALIDITY_EXPIRED.ordinal()] = 20;
            } catch (NoSuchFieldError e20) {
            }
        }
    }
}
