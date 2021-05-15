package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.Object;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BufferDBTranslation extends BufferDBSupportTranslation {
    private static final String LOG_TAG = BufferDBTranslation.class.getSimpleName();

    public BufferDBTranslation(Context context, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(context, iCloudMessageManagerHelper);
    }

    public String getSearchCursorByLine(String line, SyncMsgType type) {
        String str = LOG_TAG;
        Log.d(str, "getSearchCursorByLine: line " + IMSLog.checker(line) + " type: " + type);
        Cursor cs = this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/" + line), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    do {
                        String searchCursor = cs.getString(cs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCCURSOR));
                        if (type.equals(SyncMsgType.valueOf(cs.getInt(cs.getColumnIndex("messagetype"))))) {
                            CloudMessagePreferenceManager.getInstance().saveObjectSearchCursor(searchCursor);
                            if (cs != null) {
                                cs.close();
                            }
                            return searchCursor;
                        }
                    } while (cs.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs == null) {
            return "";
        }
        cs.close();
        return "";
        throw th;
    }

    public OMASyncEventType getInitialSyncStatusByLine(String line, SyncMsgType type) {
        String str = LOG_TAG;
        Log.d(str, "getInitialSyncStatusByLine: line " + IMSLog.checker(line) + " type: " + type);
        Cursor cs = this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MULTILINESTATUS + "/" + line), (String[]) null, (String) null, (String[]) null, (String) null);
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    do {
                        int initStatus = cs.getInt(cs.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.INITSYNCSTATUS));
                        if (type.equals(SyncMsgType.valueOf(cs.getInt(cs.getColumnIndex("messagetype"))))) {
                            CloudMessagePreferenceManager.getInstance().saveInitialSyncStatus(initStatus);
                            OMASyncEventType valueOf = OMASyncEventType.valueOf(initStatus);
                            if (cs != null) {
                                cs.close();
                            }
                            return valueOf;
                        }
                    } while (cs.moveToNext() != 0);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        return OMASyncEventType.DEFAULT;
        throw th;
    }

    public Pair<String, String> getObjectIdFlagNamePairFromBufDb(BufferDBChangeParam param) {
        return getFlagNamePairFromBufDb(param, false);
    }

    public Pair<String, String> getResourceUrlFlagNamePairFromBufDb(BufferDBChangeParam param) {
        return getFlagNamePairFromBufDb(param, true);
    }

    /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:146)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:71)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:35)
        */
    public android.util.Pair<java.lang.String, java.lang.String> getFlagNamePairFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam r12, boolean r13) {
        /*
            r11 = this;
            r0 = 0
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "message type: "
            r2.append(r3)
            int r3 = r12.mDBIndex
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            int r1 = r12.mDBIndex
            r2 = 17
            r3 = 1
            r4 = 3
            if (r1 != r4) goto L_0x0028
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.querySMSBufferDB(r4)
            goto L_0x0070
        L_0x0028:
            int r1 = r12.mDBIndex
            r4 = 4
            if (r1 != r4) goto L_0x0034
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.querymmsPduBufferDB(r4)
            goto L_0x0070
        L_0x0034:
            int r1 = r12.mDBIndex
            if (r1 != r3) goto L_0x003f
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.queryRCSMessageDBUsingRowId(r4)
            goto L_0x0070
        L_0x003f:
            int r1 = r12.mDBIndex
            if (r1 != r2) goto L_0x004a
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.queryVvmDataBufferDB(r4)
            goto L_0x0070
        L_0x004a:
            int r1 = r12.mDBIndex
            r4 = 21
            if (r1 != r4) goto L_0x0057
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.queryFaxBufferDB(r4)
            goto L_0x0070
        L_0x0057:
            int r1 = r12.mDBIndex
            r4 = 16
            if (r1 != r4) goto L_0x0064
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.queryCallLogDataBufferDB(r4)
            goto L_0x0070
        L_0x0064:
            int r1 = r12.mDBIndex
            r4 = 18
            if (r1 != r4) goto L_0x0070
            long r4 = r12.mRowId
            android.database.Cursor r0 = r11.queryVvmGreetingBufferDB(r4)
        L_0x0070:
            java.lang.String r1 = ""
            java.lang.String r4 = ""
            java.lang.String r5 = ""
            r6 = r0
            if (r6 == 0) goto L_0x011d
            boolean r7 = r6.moveToFirst()     // Catch:{ all -> 0x0111 }
            if (r7 == 0) goto L_0x011d
            java.lang.String r7 = "res_url"
            int r7 = r6.getColumnIndex(r7)     // Catch:{ all -> 0x0111 }
            java.lang.String r7 = r6.getString(r7)     // Catch:{ all -> 0x0111 }
            r1 = r7
            java.lang.String r7 = "syncaction"
            int r7 = r6.getColumnIndex(r7)     // Catch:{ all -> 0x0111 }
            int r7 = r6.getInt(r7)     // Catch:{ all -> 0x0111 }
            java.lang.String r8 = LOG_TAG     // Catch:{ all -> 0x0111 }
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ all -> 0x0111 }
            r9.<init>()     // Catch:{ all -> 0x0111 }
            java.lang.String r10 = "resUrl : "
            r9.append(r10)     // Catch:{ all -> 0x0111 }
            java.lang.String r10 = com.sec.internal.log.IMSLog.checker(r1)     // Catch:{ all -> 0x0111 }
            r9.append(r10)     // Catch:{ all -> 0x0111 }
            java.lang.String r10 = " action: "
            r9.append(r10)     // Catch:{ all -> 0x0111 }
            r9.append(r7)     // Catch:{ all -> 0x0111 }
            java.lang.String r9 = r9.toString()     // Catch:{ all -> 0x0111 }
            android.util.Log.i(r8, r9)     // Catch:{ all -> 0x0111 }
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r8 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Update     // Catch:{ all -> 0x0111 }
            int r8 = r8.getId()     // Catch:{ all -> 0x0111 }
            if (r7 != r8) goto L_0x00f2
            int r8 = r12.mDBIndex     // Catch:{ all -> 0x0111 }
            java.lang.String r9 = "\\Seen"
            if (r8 != r2) goto L_0x00d9
            java.lang.String r2 = "flagRead"
            int r2 = r6.getColumnIndex(r2)     // Catch:{ all -> 0x0111 }
            int r2 = r6.getInt(r2)     // Catch:{ all -> 0x0111 }
            if (r2 != 0) goto L_0x00d7
            java.lang.String r8 = "\\Flagged"
            r5 = r8
            goto L_0x00d8
        L_0x00d7:
            r5 = r9
        L_0x00d8:
            goto L_0x00db
        L_0x00d9:
            r2 = r9
            r5 = r2
        L_0x00db:
            java.lang.String r2 = LOG_TAG     // Catch:{ all -> 0x0111 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ all -> 0x0111 }
            r8.<init>()     // Catch:{ all -> 0x0111 }
            java.lang.String r9 = "FlagNames: "
            r8.append(r9)     // Catch:{ all -> 0x0111 }
            r8.append(r5)     // Catch:{ all -> 0x0111 }
            java.lang.String r8 = r8.toString()     // Catch:{ all -> 0x0111 }
            android.util.Log.i(r2, r8)     // Catch:{ all -> 0x0111 }
            goto L_0x00fd
        L_0x00f2:
            com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants$ActionStatusFlag r2 = com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants.ActionStatusFlag.Delete     // Catch:{ all -> 0x0111 }
            int r2 = r2.getId()     // Catch:{ all -> 0x0111 }
            if (r7 != r2) goto L_0x00fd
            java.lang.String r2 = "\\Deleted"
            r5 = r2
        L_0x00fd:
            if (r1 != 0) goto L_0x0102
            java.lang.String r2 = ""
            r1 = r2
        L_0x0102:
            if (r13 != 0) goto L_0x011d
            r2 = 47
            int r2 = r1.lastIndexOf(r2)     // Catch:{ all -> 0x0111 }
            int r2 = r2 + r3
            java.lang.String r2 = r1.substring(r2)     // Catch:{ all -> 0x0111 }
            r4 = r2
            goto L_0x011d
        L_0x0111:
            r2 = move-exception
            if (r6 == 0) goto L_0x011c
            r6.close()     // Catch:{ all -> 0x0118 }
            goto L_0x011c
        L_0x0118:
            r3 = move-exception
            r2.addSuppressed(r3)
        L_0x011c:
            throw r2
        L_0x011d:
            if (r6 == 0) goto L_0x0122
            r6.close()
        L_0x0122:
            if (r13 == 0) goto L_0x012a
            android.util.Pair r2 = new android.util.Pair
            r2.<init>(r1, r5)
            return r2
        L_0x012a:
            android.util.Pair r2 = new android.util.Pair
            r2.<init>(r4, r5)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBTranslation.getFlagNamePairFromBufDb(com.sec.internal.ims.cmstore.params.BufferDBChangeParam, boolean):android.util.Pair");
    }

    public String getSummaryObjectIdFromBufDb(BufferDBChangeParam param) {
        Cursor cursor = querySummaryDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (id == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return "";
                    }
                    String substring = id.substring(id.lastIndexOf(47) + 1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
        throw th;
    }

    public String getSmsObjectIdFromBufDb(BufferDBChangeParam param) {
        Cursor cursor = querySMSBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (id == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return "";
                    }
                    String substring = id.substring(id.lastIndexOf(47) + 1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
        throw th;
    }

    public String getVVMObjectIdFromBufDb(BufferDBChangeParam param) {
        Cursor cursor = queryVvmDataBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (id == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return "";
                    }
                    String substring = id.substring(id.lastIndexOf(47) + 1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
        throw th;
    }

    public String getVVMpayLoadUrlFromBufDb(BufferDBChangeParam param) {
        String payloadUrl = null;
        Cursor cursor = queryVvmDataBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && (payloadUrl = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL))) == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return "";
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return payloadUrl;
        throw th;
    }

    public String getVVMGreetingpayLoadUrlFromBufDb(BufferDBChangeParam param) {
        String payloadUrl = null;
        Cursor cursor = queryVvmGreetingBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && (payloadUrl = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL))) == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return "";
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return payloadUrl;
        throw th;
    }

    public ParamVvmUpdate.VvmTypeChange getVVMServiceProfileFromBufDb(BufferDBChangeParam param, VvmServiceProfile profile) {
        if (param == null || profile == null) {
            return null;
        }
        ParamVvmUpdate.VvmTypeChange profiletype = null;
        int type = param.mDBIndex;
        long rowId = param.mRowId;
        if (type == 19) {
            Cursor pincursor = queryVvmPinBufferDB(rowId);
            if (pincursor != null) {
                try {
                    if (pincursor.moveToFirst()) {
                        String oldPwd = pincursor.getString(pincursor.getColumnIndex(CloudMessageProviderContract.VVMPin.OLDPWD));
                        String newPwd = pincursor.getString(pincursor.getColumnIndex(CloudMessageProviderContract.VVMPin.NEWPWD));
                        AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
                        trans.setOldPwd(new String[]{oldPwd});
                        trans.setPwd(new String[]{newPwd});
                        profile.attributes = trans.getAttributeList();
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (pincursor != null) {
                pincursor.close();
            }
        } else if (type == 20) {
            Cursor profilecursor = queryVvmProfileBufferDB(rowId);
            if (profilecursor != null) {
                try {
                    if (profilecursor.moveToFirst()) {
                        int changeType = profilecursor.getInt(profilecursor.getColumnIndex(CloudMessageProviderContract.VVMAccountInfoColumns.PROFILE_CHANGETYPE));
                        AttributeTranslator trans2 = new AttributeTranslator(this.mCloudMessageManagerHelper);
                        if (ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT.getId() == changeType) {
                            trans2.setEmailAddress(new String[]{profilecursor.getString(profilecursor.getColumnIndex(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR1)), profilecursor.getString(profilecursor.getColumnIndex(CloudMessageProviderContract.VVMAccountInfoColumns.EMAIL_ADDR2))});
                            profiletype = ParamVvmUpdate.VvmTypeChange.VOICEMAILTOTEXT;
                        } else if (ParamVvmUpdate.VvmTypeChange.ACTIVATE.getId() == changeType) {
                            trans2.setVVMOn(new String[]{CloudMessageProviderContract.JsonData.TRUE});
                            profiletype = ParamVvmUpdate.VvmTypeChange.ACTIVATE;
                        } else if (ParamVvmUpdate.VvmTypeChange.DEACTIVATE.getId() == changeType) {
                            trans2.setVVMOn(new String[]{ConfigConstants.VALUE.INFO_COMPLETED});
                            profiletype = ParamVvmUpdate.VvmTypeChange.DEACTIVATE;
                        } else if (ParamVvmUpdate.VvmTypeChange.FULLPROFILE.getId() == changeType) {
                            profiletype = ParamVvmUpdate.VvmTypeChange.FULLPROFILE;
                        }
                        profile.attributes = trans2.getAttributeList();
                    }
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            if (profilecursor != null) {
                profilecursor.close();
            }
        }
        return profiletype;
        throw th;
        throw th;
    }

    public Pair<Object, HttpPostBody> getVVMGreetingObjectPairFromBufDb(BufferDBChangeParam param) {
        return new Pair<>(getVvmObjectFromDB(param), getVvmGreetingBodyFromDB(param));
    }

    public String getFaxObjectIdFromBufDb(BufferDBChangeParam param) {
        Cursor cursor = queryFaxBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (id == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return "";
                    }
                    String substring = id.substring(id.lastIndexOf(47) + 1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
        throw th;
    }

    public String getFaxpayLoadUrlFromBufDb(BufferDBChangeParam param) {
        String payloadUrl = null;
        Cursor cursor = queryFaxBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst() && (payloadUrl = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL))) == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return "";
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return payloadUrl;
        throw th;
    }

    public String getCallLogObjectIdFromBufDb(BufferDBChangeParam param) {
        Cursor cursor = queryCallLogDataBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    String id = cursor.getString(cursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    if (id == null) {
                        if (cursor != null) {
                            cursor.close();
                        }
                        return "";
                    }
                    String substring = id.substring(id.lastIndexOf(47) + 1);
                    if (cursor != null) {
                        cursor.close();
                    }
                    return substring;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return "";
        throw th;
    }

    public Pair<String, List<String>> getObjectIdPartIdFromRCSBufDb(BufferDBChangeParam param) {
        String ObjectId = "";
        List<String> partId = new ArrayList<>();
        Cursor rcsCursor = queryRCSMessageDBUsingRowId(param.mRowId);
        if (rcsCursor != null) {
            try {
                if (rcsCursor.moveToFirst()) {
                    String resUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    String str = LOG_TAG;
                    Log.i(str, "resUrl: " + IMSLog.checker(resUrl));
                    if (resUrl == null) {
                        ObjectId = "";
                    } else {
                        ObjectId = resUrl.substring(resUrl.lastIndexOf(47) + 1);
                    }
                    String payloadUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL));
                    if (payloadUrl != null) {
                        partId.add(payloadUrl.substring(payloadUrl.lastIndexOf(47) + 1));
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            ObjectId = "";
        }
        if (rcsCursor != null) {
            rcsCursor.close();
        }
        return new Pair<>(ObjectId, partId);
        throw th;
    }

    public Pair<String, String> getPayloadPartandAllPayloadUrlFromRCSBufDb(BufferDBChangeParam param) {
        String payloadpartUrl = "";
        String payloadUrl = "";
        Cursor rcsCursor = queryRCSMessageDBUsingRowId(param.mRowId);
        if (rcsCursor != null) {
            try {
                if (rcsCursor.moveToFirst()) {
                    payloadpartUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL));
                    payloadUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (rcsCursor != null) {
            rcsCursor.close();
        }
        return new Pair<>(payloadpartUrl, payloadUrl);
        throw th;
    }

    public Pair<String, String> getAllPayloadUrlFromRCSBufDb(BufferDBChangeParam param) {
        String payloadpartUrl = "";
        String payloadUrl = "";
        Cursor rcsCursor = queryRCSMessageDBUsingRowId(param.mRowId);
        if (rcsCursor != null) {
            try {
                if (rcsCursor.moveToFirst()) {
                    payloadpartUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTFULL));
                    payloadUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                    param.mPayloadThumbnailUrl = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB));
                    param.mFTThumbnailFileName = rcsCursor.getString(rcsCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADPARTTHUMB_FILENAME));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (rcsCursor != null) {
            rcsCursor.close();
        }
        if (!TextUtils.isEmpty(param.mPayloadThumbnailUrl)) {
            param.mIsFTThumbnail = true;
        }
        return new Pair<>(payloadpartUrl, payloadUrl);
        throw th;
    }

    public Pair<String, List<String>> getObjectIdPartIdFromMmsBufDb(BufferDBChangeParam param) {
        String id;
        String ObjectId = "";
        List<String> partId = new ArrayList<>();
        long rowId = param.mRowId;
        Cursor pduCursor = querymmsPduBufferDB(rowId);
        if (pduCursor != null) {
            try {
                if (pduCursor.moveToFirst()) {
                    String resUrl = pduCursor.getString(pduCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.RES_URL));
                    String str = LOG_TAG;
                    Log.i(str, "resUrl: " + IMSLog.checker(resUrl));
                    ObjectId = resUrl == null ? "" : resUrl.substring(resUrl.lastIndexOf(47) + 1);
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        } else {
            ObjectId = "";
        }
        if (pduCursor != null) {
            pduCursor.close();
        }
        Cursor pduCursor2 = queryPartsBufferDBUsingPduBufferId(rowId);
        if (pduCursor2 != null) {
            try {
                if (pduCursor2.moveToFirst()) {
                    do {
                        String url = pduCursor2.getString(pduCursor2.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                        if (TextUtils.isEmpty(url)) {
                            id = "";
                        } else {
                            id = url.substring(url.lastIndexOf(47) + 1);
                        }
                        String str2 = LOG_TAG;
                        Log.i(str2, "payLoadurl: " + IMSLog.checker(url) + "partId: " + id);
                        if (!TextUtils.isEmpty(id)) {
                            partId.add(id);
                        }
                    } while (pduCursor2.moveToNext());
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (pduCursor2 != null) {
            pduCursor2.close();
        }
        return new Pair<>(ObjectId, partId);
        throw th;
        throw th;
    }

    public List<String> getPayloadPartUrlFromMmsBufDb(BufferDBChangeParam param) {
        List<String> partUrl = new ArrayList<>();
        Cursor partCursor = queryPartsBufferDBUsingPduBufferId(param.mRowId);
        if (partCursor != null) {
            try {
                if (partCursor.moveToFirst()) {
                    do {
                        String url = partCursor.getString(partCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                        String str = LOG_TAG;
                        Log.i(str, "payLoadurl: " + IMSLog.checker(url));
                        if (!TextUtils.isEmpty(url)) {
                            partUrl.add(url);
                        }
                    } while (partCursor.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (partCursor != null) {
            partCursor.close();
        }
        return partUrl;
        throw th;
    }

    public String getPayloadPartUrlFromMmsPartUsingPartBufferId(BufferDBChangeParam param) {
        String partUrl = null;
        Cursor partCursor = queryPartsBufferDBUsingPartBufferId(param.mRowId);
        if (partCursor != null) {
            try {
                if (partCursor.moveToFirst()) {
                    partUrl = partCursor.getString(partCursor.getColumnIndex(CloudMessageProviderContract.BufferDBExtensionBase.PAYLOADURL));
                    String str = LOG_TAG;
                    Log.d(str, "payLoadurl: " + IMSLog.checker(partUrl));
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (partCursor != null) {
            partCursor.close();
        }
        return partUrl;
        throw th;
    }

    public ParamVvmUpdate.VvmGreetingType getVVMGreetingTypeFromBufDb(BufferDBChangeParam param) {
        Cursor vvmCursor = queryVvmGreetingBufferDB(param.mRowId);
        if (vvmCursor != null) {
            try {
                if (vvmCursor.moveToFirst()) {
                    int type = vvmCursor.getInt(vvmCursor.getColumnIndex(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE));
                    String str = LOG_TAG;
                    Log.i(str, "getVVMGreetingTypeFromBufDb : type " + type);
                    ParamVvmUpdate.VvmGreetingType valueOf = ParamVvmUpdate.VvmGreetingType.valueOf(type);
                    if (vvmCursor != null) {
                        vvmCursor.close();
                    }
                    return valueOf;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (vvmCursor != null) {
            vvmCursor.close();
        }
        return ParamVvmUpdate.VvmGreetingType.Default;
        throw th;
    }

    public Pair<Object, HttpPostBody> getRCSObjectPairFromCursor(BufferDBChangeParam param) {
        Log.d(LOG_TAG, param.toString());
        Pair<Object, HttpPostBody> pair = null;
        if (param.mDBIndex == 1) {
            Cursor cursor = queryrcsMessageBufferDB(param.mRowId);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int isSlm = cursor.getInt(cursor.getColumnIndex(ImContract.Message.MESSAGE_ISSLM));
                        int isFt = cursor.getInt(cursor.getColumnIndex(ImContract.ChatItem.IS_FILE_TRANSFER));
                        String str = LOG_TAG;
                        Log.i(str, "getRCSObjectPairFromCursor :: isSlm: " + isSlm + " isFt: " + isFt);
                        pair = isSlm == 1 ? getSlmObjectPairFromCursor(cursor) : isFt == 1 ? getFtObjectPairFromCursor(cursor) : getChatObjectPairFromCursor(cursor);
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return pair;
        throw th;
    }

    public Pair<Object, HttpPostBody> getSmsObjectPairFromCursor(BufferDBChangeParam param) {
        Throwable th;
        Pair<Object, HttpPostBody> pair = null;
        Cursor cursor = querySMSBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    Object object = new Object();
                    object.flags = new FlagList();
                    int read = cursor.getInt(cursor.getColumnIndex("read"));
                    long direction = cursor.getLong(cursor.getColumnIndex("type"));
                    if (read == 1 || direction == 2) {
                        object.flags.flag = new String[]{FlagNames.Flagged, FlagNames.Seen};
                    } else {
                        object.flags.flag = new String[]{FlagNames.Flagged};
                    }
                    AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
                    trans.setDate(new String[]{this.sFormatOfName.format(new Date(cursor.getLong(cursor.getColumnIndex("date"))))});
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    if (direction == 1) {
                        trans.setDirection(new String[]{"IN"});
                        trans.setFrom(new String[]{getE164FormatNumber(address)});
                        trans.setTo(new String[]{getE164FormatNumber(CloudMessagePreferenceManager.getInstance().getUserCtn())});
                    } else if (direction == 2) {
                        trans.setDirection(new String[]{"OUT"});
                        trans.setTo(new String[]{getE164FormatNumber(address)});
                        trans.setFrom(new String[]{getE164FormatNumber(CloudMessagePreferenceManager.getInstance().getUserCtn())});
                    }
                    trans.setCpmGroup(new String[]{"no"});
                    trans.setMessageContext(new String[]{MessageContextValues.pagerMessage});
                    object.attributes = trans.getAttributeList();
                    pair = new Pair<>(object, new HttpPostBody("form-data;name=\"attachments\";filename=\"sms.txt\"", "text/plain", cursor.getString(cursor.getColumnIndex("body"))));
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return pair;
        throw th;
    }

    public Pair<Object, HttpPostBody> getMmsObjectPairFromCursor(BufferDBChangeParam param) {
        return new Pair<>(getMmsObjectFromPduAndAddress(param), getMmsPartHttpPayloadFromCursor(queryPartsBufferDBUsingPduBufferId(param.mRowId)));
    }

    public Pair<Object, HttpPostBody> getFaxObjectPairFromCursor(BufferDBChangeParam param) {
        return new Pair<>(getFaxObjectFromBufferDB(param), getFaxHttpPayloadFromCursor(queryFaxBufferDB(param.mRowId)));
    }

    public Pair<Object, HttpPostBody> getImdnObjectPair(BufferDBChangeParam param) {
        Throwable th;
        Throwable th2;
        boolean hasError = false;
        String imdnId = null;
        String opUri = null;
        Object object = new Object();
        AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
        trans.setMessageContext(new String[]{"imdn-message"});
        Cursor notificationCursor = queryRCSNotificationDB(param.mRowId);
        if (notificationCursor != null) {
            try {
                if (notificationCursor.moveToFirst()) {
                    imdnId = notificationCursor.getString(notificationCursor.getColumnIndex("imdn_id"));
                    object.correlationId = imdnId;
                    trans.setDispositionOriginalMessageID(new String[]{imdnId});
                    opUri = ImsUri.parse(notificationCursor.getString(notificationCursor.getColumnIndex(ImContract.MessageNotification.SENDER_URI))).getMsisdn();
                    String str = LOG_TAG;
                    Log.i(str, "getImdnObjectPairFromCursor :: ImdnID : " + IMSLog.checker(imdnId) + " parsed opUri : " + IMSLog.checker(opUri));
                }
            } catch (Throwable th3) {
                th2.addSuppressed(th3);
            }
        }
        if (notificationCursor != null) {
            notificationCursor.close();
        }
        Cursor notificationCursor2 = queryRCSMessageDBUsingImdn(imdnId);
        if (notificationCursor2 != null) {
            try {
                if (notificationCursor2.moveToFirst()) {
                    int notificationStatus = notificationCursor2.getInt(notificationCursor2.getColumnIndex("notification_status"));
                    if (NotificationStatus.DELIVERED.getId() == notificationStatus) {
                        trans.setDispositionType(new String[]{ATTConstants.ATTDispositionType.DELIVERY});
                        trans.setDispositionStatus(new String[]{ATTConstants.ATTDispositionStatus.DELIVERED});
                    } else if (NotificationStatus.DISPLAYED.getId() == notificationStatus) {
                        trans.setDispositionType(new String[]{ATTConstants.ATTDispositionType.DISPLAY});
                        trans.setDispositionStatus(new String[]{ATTConstants.ATTDispositionStatus.DISPLAYED});
                    } else {
                        hasError = true;
                    }
                    object.flags = new FlagList();
                    object.flags.flag = new String[]{FlagNames.Flagged, FlagNames.Seen};
                    long direction = (long) notificationCursor2.getInt(notificationCursor2.getColumnIndex("direction"));
                    String str2 = LOG_TAG;
                    Log.i(str2, "getImdnObjectPairFromCursor :: direction : " + direction + " notificationStatus: " + notificationStatus);
                    if (direction == ((long) ImDirection.INCOMING.getId())) {
                        hasError = true;
                    } else if (direction == ((long) ImDirection.OUTGOING.getId())) {
                        trans.setDirection(new String[]{"IN"});
                        trans.setDispositionOriginalTo(new String[]{opUri});
                    }
                }
            } catch (Throwable th4) {
                th.addSuppressed(th4);
            }
        }
        if (notificationCursor2 != null) {
            notificationCursor2.close();
        }
        object.attributes = trans.getAttributeList();
        if (hasError) {
            return null;
        }
        return new Pair<>(object, (Object) null);
        throw th;
        throw th2;
    }
}
