package com.sec.internal.ims.cmstore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.ims.cmstore.adapters.CloudMessageBufferDBPersister;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CloudMessageProvider extends ContentProvider {
    private static final String LOG_TAG = CloudMessageProvider.class.getSimpleName();
    private static final String PROVIDER_NAME = "com.samsung.rcs.cmstore";
    private static final UriMatcher sUriMatcher;
    private CloudMessageBufferDBPersister mBufferDB;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.samsung.rcs.cmstore", "smsmessages/#", 3);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "mmspdumessage/#", 4);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "mmsaddrmessages/#", 5);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "mmspartmessages/#", 6);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "mmspartmessages_partid/#", 8);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcschatmessage/#", 1);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcsftmessage/#", 1);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcsmessages/#", 1);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcsparticipants/*", 2);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcssession/*", 10);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "notification/#", 13);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "summarytable/#", 7);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "rcsmessageimdn/*", 15);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "vvmmessages/*", 17);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "vvmprofile/*", 20);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "vvmgreeting/*", 18);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "vvmpin/*", 19);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "calllog/*", 16);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "faxmessages/*", 21);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "multilinestatus/*", 23);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_SMSMESSAGES, 24);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_MMSPDUMESSAGE, 25);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_RCSCHATMESSAGE, 26);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_RCSFTMESSAGE, 27);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_VVMMESSAGES, 28);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_FAX, 29);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_PENDING_CALLLOG, 30);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", "latestmessage/#", 33);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_ALL_SMSMESSAGES, 31);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_ALL_MMSPDUMESSAGE, 32);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_USER_DEBUG_FLAG, 99);
        sUriMatcher.addURI("com.samsung.rcs.cmstore", CloudMessageProviderContract.CONTENTPRDR_MIGRATE_SUCCESS, 35);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "delete " + uri);
        switch (sUriMatcher.match(uri)) {
            case 1:
                return this.mBufferDB.deleteTable(1, selection, selectionArgs);
            case 2:
                return this.mBufferDB.deleteTable(2, selection, selectionArgs);
            case 3:
                return this.mBufferDB.deleteTable(3, selection, selectionArgs);
            case 4:
                return this.mBufferDB.deleteTable(4, selection, selectionArgs);
            case 5:
                return this.mBufferDB.deleteTable(5, selection, selectionArgs);
            case 6:
                return this.mBufferDB.deleteTable(6, selection, selectionArgs);
            case 7:
                return this.mBufferDB.deleteTable(7, selection, selectionArgs);
            default:
                return 0;
        }
    }

    public String getType(Uri arg0) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues arg1) {
        String str = LOG_TAG;
        IMSLog.s(str, "insert " + uri);
        return null;
    }

    public boolean onCreate() {
        IMSLog.s(LOG_TAG, "onCreate()");
        CloudMessagePreferenceManager.init(getContext().getApplicationContext());
        this.mBufferDB = CloudMessageBufferDBPersister.getInstance(getContext());
        return true;
    }

    private Cursor updateUritoCursor(Cursor cs, boolean isMMS) {
        int colLength;
        Uri thumb_contenturi;
        Uri file_contenturi;
        Uri content_uri;
        Cursor cursor = cs;
        List<String> lst = new ArrayList<>(Arrays.asList(cs.getColumnNames()));
        lst.add("content_uri");
        if (!isMMS) {
            lst.add("thumbnail_uri");
        }
        String[] colNames = (String[]) lst.toArray(new String[0]);
        MatrixCursor matrixCursor = new MatrixCursor(colNames);
        int colLength2 = colNames.length;
        int i = 2;
        if (isMMS) {
            colLength = colLength2 - 1;
        } else {
            colLength = colLength2 - 2;
        }
        if (cursor != null && cs.moveToFirst()) {
            String filepath = null;
            String thumbpath = null;
            String datapath = null;
            while (true) {
                MatrixCursor.RowBuilder builder = matrixCursor.newRow();
                int i2 = 0;
                while (i2 < colLength) {
                    String colName = colNames[i2];
                    int columnIndex = cursor.getColumnIndex(colName);
                    int type = cursor.getType(columnIndex);
                    if (type == 0) {
                        builder.add((Object) null);
                    } else if (type == 1) {
                        builder.add(Long.valueOf(cursor.getLong(columnIndex)));
                    } else if (type == i) {
                        builder.add(Float.valueOf(cursor.getFloat(columnIndex)));
                    } else if (type == 3) {
                        builder.add(cursor.getString(columnIndex));
                        if (colName.equalsIgnoreCase("file_path")) {
                            filepath = cursor.getString(columnIndex);
                        } else if (colName.equalsIgnoreCase(ImContract.CsSession.THUMBNAIL_PATH)) {
                            thumbpath = cursor.getString(columnIndex);
                        } else if (colName.equalsIgnoreCase(CloudMessageProviderContract.BufferDBMMSpart._DATA)) {
                            datapath = cursor.getString(columnIndex);
                        }
                    } else if (type != 4) {
                        builder.add((Object) null);
                        Log.i(LOG_TAG, "Type default: " + type);
                    } else {
                        builder.add(cursor.getBlob(columnIndex));
                    }
                    i2++;
                    i = 2;
                }
                IMSLog.s(LOG_TAG, "updateUritoCursor datapath: " + datapath + " filepath: " + filepath + " thumbnailpath: " + thumbpath);
                if (!isMMS) {
                    boolean updated = false;
                    if (!(filepath == null || !filepath.contains("/RCS_files/") || (file_contenturi = FileUtils.getUriForFile(getContext().getApplicationContext(), filepath)) == null)) {
                        builder.add(file_contenturi.toString());
                        updated = true;
                    }
                    if (!updated) {
                        builder.add((Object) null);
                    }
                    boolean updated2 = false;
                    if (!(thumbpath == null || !thumbpath.contains("/RCS_files/") || (thumb_contenturi = FileUtils.getUriForFile(getContext().getApplicationContext(), thumbpath)) == null)) {
                        builder.add(thumb_contenturi.toString());
                        updated2 = true;
                    }
                    if (!updated2) {
                        builder.add((Object) null);
                    }
                } else if (!(datapath == null || !datapath.contains("/MMS_files/") || (content_uri = FileUtils.getUriForFile(getContext().getApplicationContext(), datapath)) == null)) {
                    builder.add(content_uri.toString());
                }
                if (cursor == null || !cs.moveToNext()) {
                    break;
                }
                i = 2;
            }
        }
        return matrixCursor;
    }

    public String[] updateProjection(String[] projection) {
        if (projection == null) {
            return null;
        }
        List<String> newProjection = new ArrayList<>();
        for (int i = 0; i < projection.length; i++) {
            if (!projection[i].equalsIgnoreCase("content_uri") && !projection[i].equalsIgnoreCase("thumbnail_uri")) {
                newProjection.add(projection[i]);
            }
        }
        String str = LOG_TAG;
        Log.i(str, "Projection updated, old projection len: " + projection.length + " new projection len:" + newProjection.size());
        return (String[]) newProjection.toArray(new String[0]);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] projection2;
        String[] projection3;
        Uri uri2 = uri;
        String[] projection4 = projection;
        String str = LOG_TAG;
        IMSLog.s(str, "query " + uri2);
        int match = sUriMatcher.match(uri2);
        if (match == 13) {
            String rowId = uri.getPathSegments().get(1);
            String str2 = LOG_TAG;
            Log.d(str2, "RCS_MESSAGES_IMDN bufferDB = " + rowId);
            String str3 = selection;
            String[] strArr = selectionArgs;
            return this.mBufferDB.queryTablewithBufferDbId(13, (long) Integer.parseInt(rowId));
        } else if (match == 35) {
            IMSLog.s(LOG_TAG, "DATABASE MIGRATE FLAG");
            boolean flag = CloudMessagePreferenceManager.getInstance().getMigrateSuccessFlag();
            MatrixCursor returnCursor = new MatrixCursor(new String[]{CloudMessageProviderContract.CONTENTPRDR_MIGRATE_SUCCESS});
            returnCursor.newRow().add(Integer.valueOf(flag));
            String str4 = selection;
            String[] strArr2 = selectionArgs;
            return returnCursor;
        } else if (match != 99) {
            switch (match) {
                case 1:
                    if (projection4 != null) {
                        projection4 = updateProjection(projection4);
                    }
                    String[] strArr3 = selectionArgs;
                    return updateUritoCursor(this.mBufferDB.queryTable(uri, 1, projection4, "_bufferdbid= ?", sortOrder), false);
                case 2:
                    String[] strArr4 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 2, projection, "chat_id= ?", sortOrder);
                case 3:
                    String[] strArr5 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 3, projection, "_bufferdbid= ?", sortOrder);
                case 4:
                    String[] strArr6 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 4, projection, "_bufferdbid= ?", sortOrder);
                case 5:
                    String[] strArr7 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 5, projection, "msg_id= ?", sortOrder);
                case 6:
                    if (projection4 != null) {
                        projection4 = updateProjection(projection4);
                    }
                    String[] strArr8 = selectionArgs;
                    Object obj = "mid= ?";
                    return updateUritoCursor(this.mBufferDB.queryTable(uri, 6, projection4, "mid= ?", sortOrder), true);
                case 7:
                    String[] strArr9 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 7, projection, "_bufferdbid= ?", sortOrder);
                case 8:
                    if (projection4 != null) {
                        projection4 = updateProjection(projection4);
                    }
                    String[] strArr10 = selectionArgs;
                    Object obj2 = "_bufferdbid= ?";
                    return updateUritoCursor(this.mBufferDB.queryTable(uri, 6, projection4, "_bufferdbid= ?", sortOrder), true);
                case 9:
                    String rowId2 = uri.getPathSegments().get(1);
                    String str5 = LOG_TAG;
                    Log.d(str5, "RCS_MESSAGE_ID bufferDB = " + rowId2);
                    String str6 = selection;
                    String[] strArr11 = selectionArgs;
                    return updateUritoCursor(this.mBufferDB.queryTablewithBufferDbId(1, (long) Integer.parseInt(rowId2)), false);
                case 10:
                    String[] strArr12 = selectionArgs;
                    return this.mBufferDB.queryTable(uri, 10, projection, "chat_id= ?", sortOrder);
                default:
                    switch (match) {
                        case 15:
                            if (projection4 != null) {
                                projection4 = updateProjection(projection4);
                            }
                            String[] strArr13 = selectionArgs;
                            return updateUritoCursor(this.mBufferDB.queryTable(uri, 1, projection4, "imdn_message_id= ?", sortOrder), false);
                        case 16:
                            String[] strArr14 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 16, projection, "_bufferdbid= ?", sortOrder);
                        case 17:
                            String[] strArr15 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 17, projection, "_bufferdbid= ?", sortOrder);
                        case 18:
                            String[] strArr16 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 18, projection, "_bufferdbid= ?", sortOrder);
                        case 19:
                            String[] strArr17 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 19, projection, "_bufferdbid= ?", sortOrder);
                        case 20:
                            String[] strArr18 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 20, projection, "_bufferdbid= ?", sortOrder);
                        case 21:
                            String[] strArr19 = selectionArgs;
                            return this.mBufferDB.queryTable(uri, 21, projection, "_bufferdbid= ?", sortOrder);
                        default:
                            switch (match) {
                                case 23:
                                    String[] strArr20 = selectionArgs;
                                    return this.mBufferDB.queryTable(uri, 23, projection, "linenum= ?", sortOrder);
                                case 24:
                                    String[] selectionArgsPending = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())};
                                    String[] strArr21 = selectionArgs;
                                    String[] strArr22 = selectionArgsPending;
                                    Object obj3 = "syncdirection=? OR syncdirection=?";
                                    return this.mBufferDB.queryTable(3, projection, "syncdirection=? OR syncdirection=?", selectionArgsPending, sortOrder);
                                case 25:
                                    String[] selectionArgsPending2 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())};
                                    String[] strArr23 = selectionArgs;
                                    String[] strArr24 = selectionArgsPending2;
                                    Object obj4 = "syncdirection=? OR syncdirection=?";
                                    return this.mBufferDB.queryTable(4, projection, "syncdirection=? OR syncdirection=?", selectionArgsPending2, sortOrder);
                                case 26:
                                    String[] selectionArgsPending3 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), String.valueOf(0)};
                                    if (projection4 != null) {
                                        projection2 = updateProjection(projection4);
                                    } else {
                                        projection2 = projection4;
                                    }
                                    String[] strArr25 = selectionArgs;
                                    Object obj5 = "syncdirection=? OR syncdirection=? OR is_filetransfer=?";
                                    String[] strArr26 = projection2;
                                    String[] strArr27 = selectionArgsPending3;
                                    return updateUritoCursor(this.mBufferDB.queryTable(1, projection2, "syncdirection=? OR syncdirection=? OR is_filetransfer=?", selectionArgsPending3, sortOrder), false);
                                case 27:
                                    String[] selectionArgsPending4 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId()), String.valueOf(1)};
                                    if (projection4 != null) {
                                        projection3 = updateProjection(projection4);
                                    } else {
                                        projection3 = projection4;
                                    }
                                    String[] strArr28 = selectionArgs;
                                    Object obj6 = "syncdirection=? OR syncdirection=? OR is_filetransfer=?";
                                    String[] strArr29 = projection3;
                                    String[] strArr30 = selectionArgsPending4;
                                    return updateUritoCursor(this.mBufferDB.queryTable(1, projection3, "syncdirection=? OR syncdirection=? OR is_filetransfer=?", selectionArgsPending4, sortOrder), false);
                                case 28:
                                    String[] selectionArgsPending5 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())};
                                    String[] strArr31 = selectionArgs;
                                    String[] strArr32 = selectionArgsPending5;
                                    Object obj7 = "syncdirection=? OR syncdirection=?";
                                    return this.mBufferDB.queryTable(17, projection, "syncdirection=? OR syncdirection=?", selectionArgsPending5, sortOrder);
                                case 29:
                                    String[] selectionArgsPending6 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())};
                                    String[] strArr33 = selectionArgs;
                                    String[] strArr34 = selectionArgsPending6;
                                    Object obj8 = "syncdirection=? OR syncdirection=?";
                                    return this.mBufferDB.queryTable(21, projection, "syncdirection=? OR syncdirection=?", selectionArgsPending6, sortOrder);
                                case 30:
                                    String[] selectionArgsPending7 = {String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.ToSendDevice.getId()), String.valueOf(CloudMessageBufferDBConstants.DirectionFlag.UpdatingDevice.getId())};
                                    String[] strArr35 = selectionArgs;
                                    String[] strArr36 = selectionArgsPending7;
                                    Object obj9 = "syncdirection=? OR syncdirection=?";
                                    return this.mBufferDB.queryTable(16, projection, "syncdirection=? OR syncdirection=?", selectionArgsPending7, sortOrder);
                                case 31:
                                    String str7 = selection;
                                    String[] strArr37 = selectionArgs;
                                    return this.mBufferDB.queryTable(31, (String[]) null, (String) null, (String[]) null, (String) null);
                                case 32:
                                    String str8 = selection;
                                    String[] strArr38 = selectionArgs;
                                    return this.mBufferDB.queryTable(32, (String[]) null, (String) null, (String[]) null, (String) null);
                                case 33:
                                    int dbindex = Integer.parseInt(uri.getPathSegments().get(1));
                                    String str9 = LOG_TAG;
                                    Log.d(str9, "LASTEST Message DB index = " + dbindex);
                                    String[] selectionArgs2 = {"MAX(_bufferdbid)"};
                                    if (dbindex == 1 || dbindex == 3 || dbindex == 4 || dbindex == 16 || dbindex == 21 || dbindex == 17) {
                                        Cursor cs = this.mBufferDB.queryTable(dbindex, (String[]) null, (String) null, selectionArgs2, (String) null);
                                        if (dbindex == 1) {
                                            String str10 = selection;
                                            return updateUritoCursor(cs, false);
                                        }
                                        String str11 = selection;
                                        return cs;
                                    }
                                    String str12 = selection;
                                    return null;
                                default:
                                    String str13 = selection;
                                    String[] strArr39 = selectionArgs;
                                    return null;
                            }
                    }
            }
        } else {
            IMSLog.s(LOG_TAG, "USER_DEBUG_FLAG");
            CloudMessagePreferenceManager.initUserDebug();
            MatrixCursor matrixCursor = new MatrixCursor(new String[]{DebugFlag.DEBUG_FLAG, "app_id", DebugFlag.CPS_HOST_NAME, DebugFlag.AUTH_HOST_NAME, DebugFlag.RETRY_TIME, DebugFlag.NC_HOST_NAME});
            MatrixCursor.RowBuilder rowBuilder = matrixCursor.newRow();
            rowBuilder.add(Boolean.valueOf(DebugFlag.DEBUG_RETRY_TIMELINE_FLAG));
            rowBuilder.add(ATTGlobalVariables.APP_ID);
            rowBuilder.add(ATTGlobalVariables.CPS_HOST_NAME);
            rowBuilder.add(ATTGlobalVariables.ACMS_HOST_NAME);
            rowBuilder.add(DebugFlag.debugRetryTimeLine);
            rowBuilder.add(ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST);
            String str14 = selection;
            String[] strArr40 = selectionArgs;
            return matrixCursor;
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String str = LOG_TAG;
        IMSLog.s(str, "query " + uri);
        int match = sUriMatcher.match(uri);
        if (match != 99) {
            switch (match) {
                case 1:
                    return this.mBufferDB.updateTable(1, values, selection, selectionArgs);
                case 2:
                    return this.mBufferDB.updateTable(2, values, selection, selectionArgs);
                case 3:
                    return this.mBufferDB.updateTable(3, values, selection, selectionArgs);
                case 4:
                    return this.mBufferDB.updateTable(4, values, selection, selectionArgs);
                case 5:
                    return this.mBufferDB.updateTable(5, values, selection, selectionArgs);
                case 6:
                    return this.mBufferDB.updateTable(6, values, selection, selectionArgs);
                case 7:
                    return this.mBufferDB.updateTable(7, values, selection, selectionArgs);
                default:
                    return 0;
            }
        } else {
            if (values.getAsBoolean(DebugFlag.DEBUG_FLAG) == null ? false : values.getAsBoolean(DebugFlag.DEBUG_FLAG).booleanValue()) {
                String appId = values.getAsString("app_id");
                String cpsHostName = values.getAsString(DebugFlag.CPS_HOST_NAME);
                String authHostName = values.getAsString(DebugFlag.AUTH_HOST_NAME);
                String timeLine = values.getAsString(DebugFlag.RETRY_TIME);
                ATTGlobalVariables.setValue(appId, authHostName, cpsHostName, values.getAsString(DebugFlag.NC_HOST_NAME));
                ATTGlobalVariables.setDebugHttps(true);
                DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = true;
                if (timeLine != null) {
                    DebugFlag.setRetryTimeLine(timeLine);
                }
            } else {
                ATTGlobalVariables.initDefault();
                DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = false;
                DebugFlag.initRetryTimeLine();
                ATTGlobalVariables.setDebugHttps(false);
            }
            CloudMessagePreferenceManager.saveUserDebug();
            return 0;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 10 */
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        int match = sUriMatcher.match(uri);
        if (match == 8 || match == 9) {
            Cursor c = query(uri, (String[]) null, (String) null, (String[]) null, (String) null);
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        int i = -1;
                        if (match == 8) {
                            i = c.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart._DATA);
                        } else if (match == 9) {
                            i = c.getColumnIndex("file_path");
                        }
                        String path = i >= 0 ? c.getString(i) : null;
                        if (c.moveToNext()) {
                            throw new FileNotFoundException("Multiple items at " + uri);
                        } else if (path != null) {
                            if (c != null) {
                                c.close();
                            }
                            int imode = 0;
                            File file = new File(path);
                            if (mode.contains("w")) {
                                imode = 0 | 536870912;
                                if (!file.exists()) {
                                    try {
                                        file.createNewFile();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (mode.contains("r")) {
                                imode |= LogClass.SIM_EVENT;
                            }
                            if (mode.contains("+")) {
                                imode |= 33554432;
                            }
                            return ParcelFileDescriptor.open(file, imode);
                        } else {
                            throw new FileNotFoundException("File path is null");
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            throw new FileNotFoundException("No entry for " + uri);
        }
        throw new IllegalArgumentException("URI invalid. Use an id-based URI only.");
        throw th;
    }
}
