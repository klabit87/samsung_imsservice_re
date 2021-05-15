package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.cmstore.TMOConstants;
import com.sec.internal.constants.ims.cmstore.data.FlagNames;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.httpclient.HttpPostBody;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.TelephonyDbHelper;
import com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamVvmUpdate;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.ITelephonyDBColumns;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.FlagList;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class BufferDBSupportTranslation extends BufferQueryDBTranslation {
    private static final String LOG_TAG = BufferDBSupportTranslation.class.getSimpleName();
    public static final String MSGAPP_FTCONTENT_URI = "content://im/ft/";
    protected ICloudMessageManagerHelper mCloudMessageManagerHelper;
    private Context mContext = null;
    private final String mDelimiter = ";";
    private final TelephonyDbHelper mTeleDBHelper;
    protected final SimpleDateFormat sFormatOfName;

    public static class MessageStatus {
        public static final int MESSAGE_TYPE_INBOX = 1;
        public static final int MESSAGE_TYPE_SENT = 2;
    }

    public BufferDBSupportTranslation(Context context, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(context);
        this.mCloudMessageManagerHelper = iCloudMessageManagerHelper;
        this.mTeleDBHelper = new TelephonyDbHelper(context);
        this.mContext = context;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        this.sFormatOfName = simpleDateFormat;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /* access modifiers changed from: protected */
    public Object getVvmObjectFromDB(BufferDBChangeParam param) {
        Object object = new Object();
        Cursor vvmCursor = queryVvmGreetingBufferDB(param.mRowId);
        if (vvmCursor != null) {
            try {
                if (vvmCursor.moveToFirst()) {
                    object.flags = new FlagList();
                    object.flags.flag = new String[]{FlagNames.Cns_Greeting_on};
                    AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
                    trans.setDate(new String[]{this.sFormatOfName.format(Long.valueOf(System.currentTimeMillis()))});
                    trans.setMessageId(new String[]{Util.generateHash()});
                    trans.setMimeVersion(new String[]{"1.0"});
                    int greetingType = vvmCursor.getInt(vvmCursor.getColumnIndex(CloudMessageProviderContract.VVMGreetingColumns.GREETINGTYPE));
                    String str = LOG_TAG;
                    Log.i(str, "getVvmObjectFromDB greetingType: " + greetingType);
                    if (ParamVvmUpdate.VvmGreetingType.Name.getId() == greetingType) {
                        trans.setGreetingType(new String[]{"voice-signature"});
                    } else if (ParamVvmUpdate.VvmGreetingType.Custom.getId() == greetingType) {
                        trans.setGreetingType(new String[]{"normal-greeting"});
                    } else if (ParamVvmUpdate.VvmGreetingType.Busy.getId() == greetingType) {
                        trans.setGreetingType(new String[]{"busy-greeting"});
                    } else if (ParamVvmUpdate.VvmGreetingType.ExtendAbsence.getId() == greetingType) {
                        trans.setGreetingType(new String[]{"extended-absence-greeting"});
                    } else if (ParamVvmUpdate.VvmGreetingType.Fun.getId() == greetingType) {
                        trans.setGreetingType(new String[]{"fun-greeting"});
                    } else if (ParamVvmUpdate.VvmGreetingType.Default.getId() == greetingType) {
                        trans.setGreetingType(new String[]{""});
                    }
                    trans.setContentDuration(new String[]{String.valueOf(vvmCursor.getInt(vvmCursor.getColumnIndex("duration")))});
                    object.attributes = trans.getAttributeList();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (vvmCursor != null) {
            vvmCursor.close();
        }
        return object;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getVvmGreetingBodyFromDB(BufferDBChangeParam param) {
        List<HttpPostBody> multipart = new ArrayList<>();
        Cursor cursor = queryVvmGreetingBufferDB(param.mRowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
                        if (filePath != null) {
                            byte[] data = getFileContentInBytes(filePath, CloudMessageBufferDBConstants.PayloadEncoding.Base64);
                            if (data != null) {
                                String filename = cursor.getString(cursor.getColumnIndex("fileName"));
                                if (filename == null) {
                                    filename = filePath.substring(filePath.lastIndexOf(47) + 1);
                                }
                                String contentType = cursor.getString(cursor.getColumnIndex("mimeType"));
                                if (!TextUtils.isEmpty(contentType)) {
                                    HttpPostBody body = new HttpPostBody("attachment;filename=\"" + filename + "\"", contentType, data);
                                    body.setContentTransferEncoding(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64);
                                    String str = LOG_TAG;
                                    Log.i(str, "getVvmGreetingBodyFromDB data size: " + data.length + " filename: " + filename + " contentType: " + contentType + " HttpPostBody one attachment: " + body.toString());
                                    multipart.add(body);
                                }
                            }
                        }
                    } while (cursor.moveToNext());
                    HttpPostBody rel = new HttpPostBody("form-data;name=\"attachments\"", "multipart/mixed", multipart);
                    String str2 = LOG_TAG;
                    Log.i(str2, "getVvmGreetingBodyFromDB HttpPostBody: " + rel.toString());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return rel;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor == null) {
            return null;
        }
        cursor.close();
        return null;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getFaxHttpPayloadFromCursor(Cursor cursor) {
        List<HttpPostBody> multipart = new ArrayList<>();
        Cursor cs = cursor;
        if (cs != null) {
            try {
                if (cs.moveToFirst()) {
                    String filePath = cs.getString(cs.getColumnIndex("file_path"));
                    byte[] data = null;
                    if (!TextUtils.isEmpty(filePath)) {
                        data = getFileContentInBytes(filePath, CloudMessageBufferDBConstants.PayloadEncoding.Base64);
                    }
                    if (data == null) {
                        if (cs != null) {
                            cs.close();
                        }
                        return null;
                    }
                    String filename = cs.getString(cs.getColumnIndex("file_name"));
                    HttpPostBody body = new HttpPostBody("attachment;filename=\"" + filename + "\"", cs.getString(cs.getColumnIndex("content_type")), data);
                    body.setContentTransferEncoding(HttpPostBody.CONTENT_TRANSFER_ENCODING_BASE64);
                    multipart.add(body);
                    HttpPostBody rel = new HttpPostBody("form-data;name=\"attachments\"", "multipart/mixed", multipart);
                    String str = LOG_TAG;
                    Log.i(str, "getFaxHttpPayloadFromCursor data size: " + data.length + " filename: " + filename + " HttpPostBody one attachment: " + body.toString() + " HttpPostBody: " + rel.toString());
                    if (cs != null) {
                        cs.close();
                    }
                    return rel;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cs != null) {
            cs.close();
        }
        return null;
        throw th;
    }

    /* access modifiers changed from: protected */
    public Object getFaxObjectFromBufferDB(BufferDBChangeParam param) {
        Object object = new Object();
        Cursor faxCursor = queryFaxBufferDB(param.mRowId);
        if (faxCursor != null) {
            try {
                if (faxCursor.moveToFirst()) {
                    AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
                    trans.setDate(new String[]{this.sFormatOfName.format(new Date(faxCursor.getLong(faxCursor.getColumnIndex("date"))))});
                    trans.setMessageContext(new String[]{MessageContextValues.faxMessage});
                    String to = faxCursor.getString(faxCursor.getColumnIndex("recipients"));
                    trans.setDirection(new String[]{"OUT"});
                    if (to.indexOf(";") >= 0) {
                        Log.d(LOG_TAG, "multiple recipient for Fax");
                        String[] result = to.split(";");
                        for (int i = 0; i < result.length; i++) {
                            result[i] = Util.getTelUri(result[i]);
                        }
                        trans.setTo(result);
                    } else {
                        trans.setTo(new String[]{Util.getTelUri(to)});
                    }
                    trans.setFrom(new String[]{param.mLine});
                    String filename = faxCursor.getString(faxCursor.getColumnIndex("file_name"));
                    trans.setContentType(new String[]{ContentTypeTranslator.translate(filename.substring(filename.lastIndexOf(".") + 1).toUpperCase(Locale.ENGLISH))});
                    trans.setMimeVersion(new String[]{"1.0"});
                    trans.setClientCorrelator(new String[]{faxCursor.getString(faxCursor.getColumnIndex(CloudMessageProviderContract.FAXMessages.FAXID))});
                    trans.setReportRequested(new String[]{CloudMessageProviderContract.JsonData.TRUE});
                    trans.setMessageId(new String[]{Util.generateHash()});
                    object.attributes = trans.getAttributeList();
                    object.parentFolder = new URL(TMOConstants.TMOCmStrategy.FAX_FOLDER);
                }
            } catch (MalformedURLException e) {
                object.parentFolder = null;
                e.printStackTrace();
            } catch (Throwable th) {
                if (faxCursor != null) {
                    try {
                        faxCursor.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        if (faxCursor != null) {
            faxCursor.close();
        }
        return object;
    }

    private HttpPostBody getThumbnailPayloadPart(Cursor cursor, File thumbfile, File file, String thumbfilepath) {
        Cursor cursor2 = cursor;
        if (thumbfile == null || !thumbfile.exists()) {
            String str = thumbfilepath;
            return null;
        }
        String filename = cursor2.getString(cursor2.getColumnIndex("file_name"));
        if (TextUtils.isEmpty(filename)) {
            filename = Util.getRandomFileName("jpg");
        }
        String contentDisposition = "attachment;filename=\"" + filename + "\"";
        String contentType = cursor2.getString(cursor2.getColumnIndex("content_type"));
        if (!TextUtils.isEmpty(contentType) && MIMEContentType.FT_HTTP.equals(contentType)) {
            contentType = (file == null || !file.exists()) ? ImCache.getContentType(thumbfile) : ImCache.getContentType(file);
        }
        byte[] data = getFileContentInBytes(thumbfilepath, CloudMessageBufferDBConstants.PayloadEncoding.None);
        if (data == null || data.length == 0 || TextUtils.isEmpty(contentType)) {
            return null;
        }
        if (file == null || !file.exists()) {
            return new HttpPostBody(contentDisposition, contentType, data);
        }
        return new HttpPostBody("icon;filename=\"thumbnail_" + filename + "\"", contentType, data, (String) null, "thumbnail");
    }

    private HttpPostBody getFilePayloadPart(Cursor cursor, File file, File thumbfile, String filepath) {
        Cursor cursor2 = cursor;
        if (file == null || !file.exists()) {
            String str = filepath;
            return null;
        }
        String contentDisposition = "attachment;filename=\"" + cursor2.getString(cursor2.getColumnIndex("file_name")) + "\"";
        String contentType = cursor2.getString(cursor2.getColumnIndex("content_type"));
        if (!TextUtils.isEmpty(contentType) && MIMEContentType.FT_HTTP.equals(contentType)) {
            contentType = ImCache.getContentType(file);
        }
        byte[] data = getFileContentInBytes(filepath, CloudMessageBufferDBConstants.PayloadEncoding.None);
        if (data == null || data.length == 0 || TextUtils.isEmpty(contentType)) {
            return null;
        }
        if (thumbfile == null || !thumbfile.exists()) {
            return new HttpPostBody(contentDisposition, contentType, data);
        }
        return new HttpPostBody(contentDisposition, contentType, data, "cid:thumbnail", (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00bc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getLocalFilePathForFt(android.database.Cursor r22) {
        /*
            r21 = this;
            r1 = r21
            r2 = r22
            java.lang.String r0 = "direction"
            int r0 = r2.getColumnIndexOrThrow(r0)
            int r0 = r2.getInt(r0)
            long r3 = (long) r0
            java.lang.String r0 = "is_filetransfer"
            int r0 = r2.getColumnIndexOrThrow(r0)
            int r5 = r2.getInt(r0)
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "direction: "
            r6.append(r7)
            r6.append(r3)
            java.lang.String r7 = " isFt: "
            r6.append(r7)
            r6.append(r5)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r0, r6)
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r0 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            int r0 = r0.getId()
            long r6 = (long) r0
            int r0 = (r3 > r6 ? 1 : (r3 == r6 ? 0 : -1))
            if (r0 != 0) goto L_0x00fb
            r0 = 1
            if (r5 == r0) goto L_0x0048
            goto L_0x00fb
        L_0x0048:
            java.lang.String r7 = "content://im/ft/"
            android.net.Uri r7 = android.net.Uri.parse(r7)
            java.lang.String r8 = "imdn_message_id"
            int r8 = r2.getColumnIndex(r8)
            java.lang.String r14 = r2.getString(r8)
            java.lang.String r15 = "_id"
            java.lang.String[] r10 = new java.lang.String[]{r15}
            java.lang.String r16 = "imdn_message_id = ?"
            java.lang.String[] r12 = new java.lang.String[r0]
            r0 = 0
            r12[r0] = r14
            r17 = -1
            com.sec.internal.ims.cmstore.helper.TelephonyDbHelper r8 = r1.mTeleDBHelper
            r13 = 0
            r9 = r7
            r11 = r16
            android.database.Cursor r8 = r8.query(r9, r10, r11, r12, r13)
            if (r8 == 0) goto L_0x0096
            boolean r0 = r8.moveToFirst()     // Catch:{ all -> 0x0088 }
            if (r0 == 0) goto L_0x0096
            int r0 = r8.getColumnIndexOrThrow(r15)     // Catch:{ all -> 0x0088 }
            long r19 = r8.getLong(r0)     // Catch:{ all -> 0x0088 }
            r17 = r19
            r9 = r7
            r6 = r17
            goto L_0x0099
        L_0x0088:
            r0 = move-exception
            r6 = r0
            if (r8 == 0) goto L_0x0095
            r8.close()     // Catch:{ all -> 0x0090 }
            goto L_0x0095
        L_0x0090:
            r0 = move-exception
            r9 = r0
            r6.addSuppressed(r9)
        L_0x0095:
            throw r6
        L_0x0096:
            r9 = r7
            r6 = r17
        L_0x0099:
            if (r8 == 0) goto L_0x009e
            r8.close()
        L_0x009e:
            r17 = -1
            int r8 = (r6 > r17 ? 1 : (r6 == r17 ? 0 : -1))
            if (r8 != 0) goto L_0x00bc
            java.lang.String r8 = LOG_TAG
            java.lang.StringBuilder r11 = new java.lang.StringBuilder
            r11.<init>()
            java.lang.String r13 = "Invalid rowId received for imdn id: "
            r11.append(r13)
            r11.append(r14)
            java.lang.String r11 = r11.toString()
            android.util.Log.e(r8, r11)
            r0 = 0
            return r0
        L_0x00bc:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r11 = "row id : "
            r8.append(r11)
            r8.append(r6)
            java.lang.String r11 = " for imdn id:"
            r8.append(r11)
            r8.append(r14)
            java.lang.String r8 = r8.toString()
            android.util.Log.i(r0, r8)
            java.lang.String r0 = "content://im/ft_original/"
            android.net.Uri r0 = android.net.Uri.parse(r0)
            android.net.Uri r8 = android.content.ContentUris.withAppendedId(r0, r6)
            java.lang.String r8 = r8.toString()
            java.lang.String r11 = "file_name"
            int r11 = r2.getColumnIndex(r11)
            java.lang.String r11 = r2.getString(r11)
            android.content.Context r13 = r1.mContext
            java.lang.String r13 = com.sec.internal.helper.FileUtils.copyFileFromUri(r13, r8, r11)
            return r13
        L_0x00fb:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBSupportTranslation.getLocalFilePathForFt(android.database.Cursor):java.lang.String");
    }

    private List<HttpPostBody> getFtMultiBody(Cursor cursor, String filepath) {
        String localFilePath = getLocalFilePathForFt(cursor);
        Log.i(LOG_TAG, "getFtMultiBody localFilePath : " + localFilePath + " filePath: " + filepath);
        String thumbfilepath = cursor.getString(cursor.getColumnIndex(ImContract.CsSession.THUMBNAIL_PATH));
        File file = null;
        if (!TextUtils.isEmpty(localFilePath)) {
            file = new File(localFilePath);
        } else if (!TextUtils.isEmpty(filepath)) {
            file = new File(filepath);
        }
        File thumbfile = null;
        if (!TextUtils.isEmpty(thumbfilepath)) {
            thumbfile = new File(thumbfilepath);
        }
        List<HttpPostBody> multibody = new ArrayList<>();
        if (ATTGlobalVariables.isAmbsPhaseIV()) {
            HttpPostBody part = getFilePayloadPart(cursor, file, thumbfile, filepath);
            if (part != null) {
                multibody.add(part);
            }
            HttpPostBody part2 = getThumbnailPayloadPart(cursor, thumbfile, file, thumbfilepath);
            if (part2 != null) {
                multibody.add(part2);
            }
            Log.d(LOG_TAG, "Filepath: " + file + " File payload size: " + multibody.size() + " thumbnailpath: " + thumbfilepath + " Thumbnail payload size: " + multibody.size());
        } else {
            if (file != null && file.exists()) {
                String contentDisposition = "attachment;filename=\"" + cursor.getString(cursor.getColumnIndex("file_name")) + "\"";
                String contentType = cursor.getString(cursor.getColumnIndex("content_type"));
                byte[] data = getFileContentInBytes(filepath, CloudMessageBufferDBConstants.PayloadEncoding.None);
                if (data == null || data.length == 0 || TextUtils.isEmpty(contentType)) {
                    return multibody;
                }
                multibody.add(new HttpPostBody(contentDisposition, contentType, data));
            }
            Log.d(LOG_TAG, "thumbnail filepath : " + thumbfilepath + " ,body size: " + multibody.size());
        }
        FileUtils.removeFile(localFilePath);
        return multibody;
    }

    /* access modifiers changed from: protected */
    public List<HttpPostBody> getChatSlmMultibody(Cursor cs, String body, BufferQueryDBTranslation.MessageType type, String filepath) {
        String localFilePath = getLocalFilePathForFt(cs);
        Log.i(LOG_TAG, "getChatSlmMultibody localFilePath : " + localFilePath + " filePath: " + filepath);
        if (!TextUtils.isEmpty(localFilePath)) {
            filepath = localFilePath;
        }
        List<HttpPostBody> multibody = new ArrayList<>();
        if (!TextUtils.isEmpty(body)) {
            multibody.add(new HttpPostBody("form-data;name=\"attachments\";filename=\"sms.txt\"", "text/plain", body));
        } else if (!TextUtils.isEmpty(filepath)) {
            String filename = cs.getString(cs.getColumnIndex("file_name"));
            String contentDisposition = "attachment;name=file;filename=\"" + filename + "\"";
            if (type == BufferQueryDBTranslation.MessageType.MESSAGE_CHAT) {
                contentDisposition = "attachment;filename=\"" + filename + "\"";
            }
            String contentType = cs.getString(cs.getColumnIndex("content_type"));
            byte[] data = getFileContentInBytes(filepath, CloudMessageBufferDBConstants.PayloadEncoding.None);
            if (data == null || data.length == 0 || TextUtils.isEmpty(contentType)) {
                return multibody;
            }
            multibody.add(new HttpPostBody(contentDisposition, contentType, data));
        }
        FileUtils.removeFile(localFilePath);
        return multibody;
    }

    /* access modifiers changed from: protected */
    public boolean setCpmTransMessage(AttributeTranslator trans, Set<String> participants, BufferQueryDBTranslation.MessageType type) {
        boolean isGroupChat = false;
        if (participants.size() > 1) {
            trans.setCpmGroup(new String[]{"yes"});
            isGroupChat = true;
        } else {
            trans.setCpmGroup(new String[]{"no"});
        }
        String str = LOG_TAG;
        Log.i(str, "setCpmTransMessage  type" + type);
        if (type == BufferQueryDBTranslation.MessageType.MESSAGE_CHAT) {
            trans.setMessageContext(new String[]{ATTConstants.ATTMessageContextValues.chatMessage});
        } else if (type == BufferQueryDBTranslation.MessageType.MESSAGE_SLM) {
            trans.setMessageContext(new String[]{ATTConstants.ATTMessageContextValues.standaloneMessage});
        } else if (type == BufferQueryDBTranslation.MessageType.MESSAGE_FT) {
            trans.setMessageContext(new String[]{ATTConstants.ATTMessageContextValues.fileMessage});
        }
        return isGroupChat;
    }

    /* access modifiers changed from: protected */
    public void setConversationId(AttributeTranslator trans, String chatId) {
        if (!TextUtils.isEmpty(chatId)) {
            Cursor sessionCursor = queryRCSSessionDB(chatId);
            if (sessionCursor != null) {
                try {
                    if (sessionCursor.moveToFirst()) {
                        String conversationId = sessionCursor.getString(sessionCursor.getColumnIndex("conversation_id"));
                        String str = LOG_TAG;
                        Log.i(str, "getObjectPairFromCursor :: conversationId : " + conversationId);
                        trans.setConversationId(new String[]{conversationId});
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (sessionCursor != null) {
                sessionCursor.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getFtObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_FT);
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getChatObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_CHAT);
    }

    /* access modifiers changed from: protected */
    public Pair<Object, HttpPostBody> getSlmObjectPairFromCursor(Cursor cursor) {
        return getObjectPairFromCursor(cursor, BufferQueryDBTranslation.MessageType.MESSAGE_SLM);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0252 A[SYNTHETIC, Splitter:B:106:0x0252] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x010d  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x018b A[SYNTHETIC, Splitter:B:57:0x018b] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01e5  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x01ec A[SYNTHETIC, Splitter:B:77:0x01ec] */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01f8  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.util.Pair<com.sec.internal.omanetapi.nms.data.Object, com.sec.internal.helper.httpclient.HttpPostBody> getObjectPairFromCursor(android.database.Cursor r23, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType r24) {
        /*
            r22 = this;
            r1 = r22
            r2 = r23
            r3 = r24
            r0 = 0
            if (r2 != 0) goto L_0x000a
            return r0
        L_0x000a:
            r4 = 0
            com.sec.internal.omanetapi.nms.data.Object r5 = new com.sec.internal.omanetapi.nms.data.Object
            r5.<init>()
            r6 = 0
            com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator r7 = new com.sec.internal.ims.cmstore.omanetapi.nms.data.AttributeTranslator
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r8 = r1.mCloudMessageManagerHelper
            r7.<init>(r8)
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            r9 = 0
            r10 = r23
            boolean r11 = r10.moveToFirst()     // Catch:{ all -> 0x0248 }
            if (r11 != 0) goto L_0x002d
            if (r10 == 0) goto L_0x002c
            r10.close()
        L_0x002c:
            return r0
        L_0x002d:
            java.lang.String r11 = "chat_id"
            int r11 = r10.getColumnIndex(r11)     // Catch:{ all -> 0x0248 }
            java.lang.String r11 = r10.getString(r11)     // Catch:{ all -> 0x0248 }
            r4 = r11
            java.lang.String r11 = "direction"
            int r11 = r10.getColumnIndex(r11)     // Catch:{ all -> 0x0248 }
            int r11 = r10.getInt(r11)     // Catch:{ all -> 0x0248 }
            long r11 = (long) r11     // Catch:{ all -> 0x0248 }
            com.sec.internal.omanetapi.nms.data.FlagList r13 = new com.sec.internal.omanetapi.nms.data.FlagList     // Catch:{ all -> 0x0248 }
            r13.<init>()     // Catch:{ all -> 0x0248 }
            r5.flags = r13     // Catch:{ all -> 0x0248 }
            java.lang.String r13 = "status"
            int r13 = r10.getColumnIndex(r13)     // Catch:{ all -> 0x0248 }
            int r13 = r10.getInt(r13)     // Catch:{ all -> 0x0248 }
            java.lang.String r14 = "ft_status"
            int r14 = r10.getColumnIndex(r14)     // Catch:{ all -> 0x0248 }
            int r14 = r10.getInt(r14)     // Catch:{ all -> 0x0248 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r15 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0248 }
            int r15 = r15.getId()     // Catch:{ all -> 0x0248 }
            java.lang.String r0 = "\\Flagged"
            if (r13 == r15) goto L_0x00a3
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Status r15 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Status.READ     // Catch:{ all -> 0x0099 }
            int r15 = r15.getId()     // Catch:{ all -> 0x0099 }
            if (r14 == r15) goto L_0x0094
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r15 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x0099 }
            int r15 = r15.getId()     // Catch:{ all -> 0x0099 }
            r16 = r8
            r17 = r9
            long r8 = (long) r15
            int r8 = (r11 > r8 ? 1 : (r11 == r8 ? 0 : -1))
            if (r8 != 0) goto L_0x0081
            goto L_0x00a7
        L_0x0081:
            com.sec.internal.omanetapi.nms.data.FlagList r8 = r5.flags     // Catch:{ all -> 0x008a }
            java.lang.String[] r0 = new java.lang.String[]{r0}     // Catch:{ all -> 0x008a }
            r8.flag = r0     // Catch:{ all -> 0x008a }
            goto L_0x00b1
        L_0x008a:
            r0 = move-exception
            r2 = r0
            r20 = r6
            r8 = r16
            r9 = r17
            goto L_0x0250
        L_0x0094:
            r16 = r8
            r17 = r9
            goto L_0x00a7
        L_0x0099:
            r0 = move-exception
            r16 = r8
            r17 = r9
            r2 = r0
            r20 = r6
            goto L_0x0250
        L_0x00a3:
            r16 = r8
            r17 = r9
        L_0x00a7:
            com.sec.internal.omanetapi.nms.data.FlagList r8 = r5.flags     // Catch:{ all -> 0x023f }
            java.lang.String r9 = "\\Seen"
            java.lang.String[] r0 = new java.lang.String[]{r0, r9}     // Catch:{ all -> 0x023f }
            r8.flag = r0     // Catch:{ all -> 0x023f }
        L_0x00b1:
            java.text.SimpleDateFormat r0 = r1.sFormatOfName     // Catch:{ all -> 0x023f }
            java.util.Date r8 = new java.util.Date     // Catch:{ all -> 0x023f }
            java.lang.String r9 = "inserted_timestamp"
            int r9 = r10.getColumnIndex(r9)     // Catch:{ all -> 0x023f }
            r15 = r13
            r18 = r14
            long r13 = r10.getLong(r9)     // Catch:{ all -> 0x023f }
            r8.<init>(r13)     // Catch:{ all -> 0x023f }
            java.lang.String r0 = r0.format(r8)     // Catch:{ all -> 0x023f }
            r8 = 1
            java.lang.String[] r9 = new java.lang.String[r8]     // Catch:{ all -> 0x023f }
            r13 = 0
            r9[r13] = r0     // Catch:{ all -> 0x023f }
            r7.setDate(r9)     // Catch:{ all -> 0x023f }
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x023f }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x023f }
            r14.<init>()     // Catch:{ all -> 0x023f }
            java.lang.String r13 = "getObjectPairFromCursor :: direction : "
            r14.append(r13)     // Catch:{ all -> 0x023f }
            r14.append(r11)     // Catch:{ all -> 0x023f }
            java.lang.String r13 = " messagetype : "
            r14.append(r13)     // Catch:{ all -> 0x023f }
            r14.append(r3)     // Catch:{ all -> 0x023f }
            java.lang.String r13 = " date : "
            r14.append(r13)     // Catch:{ all -> 0x023f }
            r14.append(r0)     // Catch:{ all -> 0x023f }
            java.lang.String r13 = r14.toString()     // Catch:{ all -> 0x023f }
            android.util.Log.i(r9, r13)     // Catch:{ all -> 0x023f }
            java.util.Set r9 = r1.getAddrFromParticipantTable(r4)     // Catch:{ all -> 0x023f }
            boolean r13 = r1.setCpmTransMessage(r7, r9, r3)     // Catch:{ all -> 0x023f }
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r14 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING     // Catch:{ all -> 0x0237 }
            int r14 = r14.getId()     // Catch:{ all -> 0x0237 }
            r21 = r9
            long r8 = (long) r14
            int r8 = (r11 > r8 ? 1 : (r11 == r8 ? 0 : -1))
            if (r8 != 0) goto L_0x0137
            java.lang.String r8 = "remote_uri"
            int r8 = r10.getColumnIndex(r8)     // Catch:{ all -> 0x012e }
            java.lang.String r8 = r10.getString(r8)     // Catch:{ all -> 0x012e }
            if (r8 != 0) goto L_0x0126
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r9 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_CHAT     // Catch:{ all -> 0x012e }
            if (r3 != r9) goto L_0x0126
            if (r10 == 0) goto L_0x0124
            r10.close()
        L_0x0124:
            r9 = 0
            return r9
        L_0x0126:
            r9 = r21
            r1.setTransToFrom(r10, r7, r9, r8)     // Catch:{ all -> 0x012e }
            r21 = r15
            goto L_0x0171
        L_0x012e:
            r0 = move-exception
            r2 = r0
            r20 = r6
            r9 = r13
            r8 = r16
            goto L_0x0250
        L_0x0137:
            r9 = r21
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r8 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ all -> 0x0237 }
            int r8 = r8.getId()     // Catch:{ all -> 0x0237 }
            r21 = r15
            long r14 = (long) r8     // Catch:{ all -> 0x0237 }
            int r8 = (r11 > r14 ? 1 : (r11 == r14 ? 0 : -1))
            if (r8 != 0) goto L_0x022c
            java.lang.String r8 = "OUT"
            java.lang.String[] r8 = new java.lang.String[]{r8}     // Catch:{ all -> 0x0237 }
            r7.setDirection(r8)     // Catch:{ all -> 0x0237 }
            int r8 = r9.size()     // Catch:{ all -> 0x0237 }
            java.lang.String[] r8 = new java.lang.String[r8]     // Catch:{ all -> 0x0237 }
            java.lang.Object[] r8 = r9.toArray(r8)     // Catch:{ all -> 0x0237 }
            java.lang.String[] r8 = (java.lang.String[]) r8     // Catch:{ all -> 0x0237 }
            r7.setTo(r8)     // Catch:{ all -> 0x0237 }
            r8 = 1
            java.lang.String[] r8 = new java.lang.String[r8]     // Catch:{ all -> 0x0237 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r14 = r1.mCloudMessageManagerHelper     // Catch:{ all -> 0x0237 }
            java.lang.String r14 = r14.getUserCtn()     // Catch:{ all -> 0x0237 }
            java.lang.String r14 = r1.getE164FormatNumber(r14)     // Catch:{ all -> 0x0237 }
            r15 = 0
            r8[r15] = r14     // Catch:{ all -> 0x0237 }
            r7.setFrom(r8)     // Catch:{ all -> 0x0237 }
        L_0x0171:
            java.lang.String r8 = "imdn_message_id"
            int r8 = r10.getColumnIndex(r8)     // Catch:{ all -> 0x0237 }
            java.lang.String r8 = r10.getString(r8)     // Catch:{ all -> 0x0237 }
            r5.correlationId = r8     // Catch:{ all -> 0x0237 }
            java.lang.String r14 = "file_path"
            int r14 = r10.getColumnIndex(r14)     // Catch:{ all -> 0x0237 }
            java.lang.String r14 = r10.getString(r14)     // Catch:{ all -> 0x0237 }
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r15 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_CHAT     // Catch:{ all -> 0x0237 }
            if (r3 == r15) goto L_0x01a4
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r15 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_SLM     // Catch:{ all -> 0x012e }
            if (r3 != r15) goto L_0x0190
            goto L_0x01a4
        L_0x0190:
            com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType r15 = com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation.MessageType.MESSAGE_FT     // Catch:{ all -> 0x012e }
            if (r3 != r15) goto L_0x019d
            java.util.List r15 = r1.getFtMultiBody(r2, r14)     // Catch:{ all -> 0x012e }
            r19 = r0
            r20 = r6
            goto L_0x01f1
        L_0x019d:
            r19 = r0
            r20 = r6
            r15 = r16
            goto L_0x01f1
        L_0x01a4:
            java.lang.String r15 = "body"
            int r15 = r10.getColumnIndex(r15)     // Catch:{ all -> 0x0237 }
            java.lang.String r15 = r10.getString(r15)     // Catch:{ all -> 0x0237 }
            r19 = r0
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0237 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0237 }
            r2.<init>()     // Catch:{ all -> 0x0237 }
            r20 = r6
            java.lang.String r6 = "getObjectPairFromCursor :: filepath : "
            r2.append(r6)     // Catch:{ all -> 0x0226 }
            r2.append(r14)     // Catch:{ all -> 0x0226 }
            java.lang.String r6 = " correlationId : "
            r2.append(r6)     // Catch:{ all -> 0x0226 }
            r2.append(r8)     // Catch:{ all -> 0x0226 }
            java.lang.String r6 = " body : "
            r2.append(r6)     // Catch:{ all -> 0x0226 }
            r2.append(r15)     // Catch:{ all -> 0x0226 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0226 }
            android.util.Log.i(r0, r2)     // Catch:{ all -> 0x0226 }
            boolean r0 = android.text.TextUtils.isEmpty(r14)     // Catch:{ all -> 0x0226 }
            if (r0 == 0) goto L_0x01ec
            boolean r0 = android.text.TextUtils.isEmpty(r15)     // Catch:{ all -> 0x0226 }
            if (r0 == 0) goto L_0x01ec
            if (r10 == 0) goto L_0x01ea
            r10.close()
        L_0x01ea:
            r0 = 0
            return r0
        L_0x01ec:
            java.util.List r0 = r1.getChatSlmMultibody(r10, r15, r3, r14)     // Catch:{ all -> 0x0226 }
            r15 = r0
        L_0x01f1:
            int r0 = r15.size()     // Catch:{ all -> 0x0221 }
            if (r0 != 0) goto L_0x01ff
            if (r10 == 0) goto L_0x01fd
            r10.close()
        L_0x01fd:
            r0 = 0
            return r0
        L_0x01ff:
            if (r10 == 0) goto L_0x0204
            r10.close()
        L_0x0204:
            r1.setConversationId(r7, r4)
            if (r13 == 0) goto L_0x020c
            r1.setSubjectAndGroup(r4, r7)
        L_0x020c:
            com.sec.internal.omanetapi.nms.data.AttributeList r0 = r7.getAttributeList()
            r5.attributes = r0
            com.sec.internal.helper.httpclient.HttpPostBody r2 = new com.sec.internal.helper.httpclient.HttpPostBody
            java.lang.String r6 = "form-data;name=\"attachments\""
            java.lang.String r8 = "multipart/mixed"
            r2.<init>((java.lang.String) r6, (java.lang.String) r8, (java.util.List<com.sec.internal.helper.httpclient.HttpPostBody>) r15)
            android.util.Pair r6 = new android.util.Pair
            r6.<init>(r5, r2)
            return r6
        L_0x0221:
            r0 = move-exception
            r2 = r0
            r9 = r13
            r8 = r15
            goto L_0x0250
        L_0x0226:
            r0 = move-exception
            r2 = r0
            r9 = r13
            r8 = r16
            goto L_0x0250
        L_0x022c:
            r19 = r0
            r20 = r6
            if (r10 == 0) goto L_0x0235
            r10.close()
        L_0x0235:
            r0 = 0
            return r0
        L_0x0237:
            r0 = move-exception
            r20 = r6
            r2 = r0
            r9 = r13
            r8 = r16
            goto L_0x0250
        L_0x023f:
            r0 = move-exception
            r20 = r6
            r2 = r0
            r8 = r16
            r9 = r17
            goto L_0x0250
        L_0x0248:
            r0 = move-exception
            r20 = r6
            r16 = r8
            r17 = r9
            r2 = r0
        L_0x0250:
            if (r10 == 0) goto L_0x025b
            r10.close()     // Catch:{ all -> 0x0256 }
            goto L_0x025b
        L_0x0256:
            r0 = move-exception
            r6 = r0
            r2.addSuppressed(r6)
        L_0x025b:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferDBSupportTranslation.getObjectPairFromCursor(android.database.Cursor, com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation.BufferQueryDBTranslation$MessageType):android.util.Pair");
    }

    private Set<String> getAddrFromParticipantTable(String chatId) {
        Set<String> address = new HashSet<>();
        Cursor participantCursor = queryRCSParticipantDB(chatId);
        if (participantCursor != null) {
            try {
                if (participantCursor.moveToFirst()) {
                    do {
                        String telUri = participantCursor.getString(participantCursor.getColumnIndex("uri"));
                        if (!TextUtils.isEmpty(telUri)) {
                            address.add(ImsUri.parse(telUri).getMsisdn());
                        }
                    } while (participantCursor.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (participantCursor != null) {
            participantCursor.close();
        }
        String str = LOG_TAG;
        Log.i(str, "getAddrFromParticipantTable : " + IMSLog.checker(address));
        return address;
        throw th;
    }

    private void setSubjectAndGroup(String chatId, AttributeTranslator trans) {
        if (!TextUtils.isEmpty(chatId)) {
            Cursor sessionCursor = queryRCSSessionDB(chatId);
            if (sessionCursor != null) {
                try {
                    if (sessionCursor.moveToFirst()) {
                        String subject = sessionCursor.getString(sessionCursor.getColumnIndex("subject"));
                        if (subject == null) {
                            subject = "";
                        }
                        trans.setSubject(new String[]{subject});
                        ChatData.ChatType chatType = ChatData.ChatType.fromId(sessionCursor.getInt(sessionCursor.getColumnIndexOrThrow(ImContract.ImSession.CHAT_TYPE)));
                        String str = LOG_TAG;
                        Log.i(str, "getChatObjectPairFromCursor :: subject : " + subject + " chatType : " + chatType);
                        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
                            trans.setOpenGroup(new String[]{"yes"});
                        } else {
                            trans.setOpenGroup(new String[]{"no"});
                        }
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (sessionCursor != null) {
                sessionCursor.close();
                return;
            }
            return;
        }
        return;
        throw th;
    }

    /* access modifiers changed from: protected */
    public HttpPostBody getMmsPartHttpPayloadFromCursor(Cursor cursor) {
        List<HttpPostBody> multipart = new ArrayList<>();
        if (cursor != null) {
            Cursor cs = cursor;
            try {
                if (cs.moveToFirst()) {
                    do {
                        String id = cs.getString(cs.getColumnIndex("_id"));
                        String contentType = cs.getString(cs.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CT));
                        if (!TextUtils.isEmpty(contentType)) {
                            byte[] data = null;
                            if (TextUtils.isEmpty(cs.getString(cs.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart._DATA)))) {
                                String text = cs.getString(cs.getColumnIndex("text"));
                                if (text != null) {
                                    data = text.getBytes();
                                }
                            } else {
                                data = getDataFromPartFile(Long.parseLong(id));
                            }
                            if (data != null) {
                                String filename = cs.getString(cs.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpart.CL));
                                HttpPostBody body = new HttpPostBody("attachment;filename=\"" + filename + "\"", contentType, data);
                                String str = LOG_TAG;
                                Log.i(str, "getMmsPartHttpPayloadFromCursor id: " + id + ", contentType: " + contentType + "data size: " + data.length + " filename: " + filename + " HttpPostBody one attachment: " + body.toString());
                                multipart.add(body);
                            }
                        }
                    } while (cs.moveToNext());
                    if (multipart.isEmpty()) {
                        if (cs != null) {
                            cs.close();
                        }
                        return null;
                    }
                    HttpPostBody rel = new HttpPostBody("form-data;name=\"attachments\"", "multipart/mixed", multipart);
                    String str2 = LOG_TAG;
                    Log.i(str2, "getMmsPartHttpPayloadFromCursor HttpPostBody: " + rel.toString());
                    if (cs != null) {
                        cs.close();
                    }
                    return rel;
                } else if (cs != null) {
                    cs.close();
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        return null;
        throw th;
    }

    /* access modifiers changed from: protected */
    public Object getMmsObjectFromPduAndAddress(BufferDBChangeParam param) {
        Throwable th;
        BufferDBChangeParam bufferDBChangeParam = param;
        Object object = new Object();
        Cursor pduCursor = querymmsPduBufferDB(bufferDBChangeParam.mRowId);
        if (pduCursor != null) {
            try {
                if (pduCursor.moveToFirst()) {
                    object.flags = new FlagList();
                    int read = pduCursor.getInt(pduCursor.getColumnIndex("read"));
                    long direction = pduCursor.getLong(pduCursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.MSG_BOX));
                    if (read == 1 || direction == 2) {
                        object.flags.flag = new String[]{FlagNames.Flagged, FlagNames.Seen};
                    } else {
                        object.flags.flag = new String[]{FlagNames.Flagged};
                    }
                    if (this.mCloudMessageManagerHelper.isMidPrimaryIdForMmsCorrelationId()) {
                        object.correlationId = pduCursor.getString(pduCursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
                    } else {
                        String trid = pduCursor.getString(pduCursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.TR_ID));
                        String str = LOG_TAG;
                        Log.d(str, "getMmsObjectFromPduAndAddress: " + bufferDBChangeParam.mRowId + ", trid : " + trid);
                        if (trid == null || trid.length() <= 2) {
                            object.correlationId = pduCursor.getString(pduCursor.getColumnIndex(CloudMessageProviderContract.BufferDBMMSpdu.M_ID));
                        } else {
                            object.correlationId = trid.substring(2);
                        }
                    }
                    AttributeTranslator trans = new AttributeTranslator(this.mCloudMessageManagerHelper);
                    trans.setDate(new String[]{this.sFormatOfName.format(new Date(pduCursor.getLong(pduCursor.getColumnIndex("date"))))});
                    MmsParticipant participants = getAddrFromPduId(bufferDBChangeParam.mRowId);
                    if (direction == 1) {
                        trans.setDirection(new String[]{"IN"});
                        trans.setFrom((String[]) participants.mFrom.toArray(new String[participants.mFrom.size()]));
                        participants.mTo.add(getE164FormatNumber(this.mCloudMessageManagerHelper.getUserCtn()));
                        trans.setTo((String[]) participants.mTo.toArray(new String[participants.mTo.size()]));
                    } else if (direction == 2) {
                        trans.setDirection(new String[]{"OUT"});
                        if (participants.mTo.size() != 0) {
                            trans.setTo((String[]) participants.mTo.toArray(new String[participants.mTo.size()]));
                        }
                        if (participants.mBcc.size() != 0) {
                            trans.setBCC((String[]) participants.mBcc.toArray(new String[participants.mBcc.size()]));
                        }
                        if (participants.mCc.size() != 0) {
                            trans.setCC((String[]) participants.mCc.toArray(new String[participants.mCc.size()]));
                        }
                        trans.setFrom(new String[]{getE164FormatNumber(this.mCloudMessageManagerHelper.getUserCtn())});
                    }
                    trans.setCpmGroup(new String[]{"no"});
                    trans.setMessageContext(new String[]{MessageContextValues.multiMediaMessage});
                    object.attributes = trans.getAttributeList();
                }
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        if (pduCursor != null) {
            pduCursor.close();
        }
        if (!TextUtils.isEmpty(object.correlationId)) {
            return object;
        }
        Log.e(LOG_TAG, "getMmsObjectFromPduAndAddress: correlation id is empty!!!");
        return null;
        throw th;
    }

    private MmsParticipant getAddrFromPduId(long rowId) {
        Set<String> temp;
        String str = LOG_TAG;
        Log.d(str, "getAddrFromPduId: " + rowId);
        Set<String> from = new HashSet<>();
        Set<String> to = new HashSet<>();
        Set<String> bcc = new HashSet<>();
        Set<String> cc = new HashSet<>();
        Cursor cursor = queryAddrBufferDB(rowId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        int direction = cursor.getInt(cursor.getColumnIndex("type"));
                        String str2 = LOG_TAG;
                        Log.d(str2, " direction: " + direction + "address is: " + IMSLog.checker(address));
                        if (direction == 137) {
                            temp = from;
                        } else if (direction == 151) {
                            temp = to;
                        } else if (direction == 129) {
                            temp = bcc;
                        } else if (direction == 130) {
                            temp = cc;
                        }
                        if (!TextUtils.isEmpty(address)) {
                            if (address.equals(ITelephonyDBColumns.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                                temp.add(getE164FormatNumber(this.mCloudMessageManagerHelper.getUserCtn()));
                            } else {
                                temp.add(getE164FormatNumber(address));
                            }
                        }
                    } while (cursor.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return new MmsParticipant(from, to, bcc, cc);
        throw th;
    }

    private byte[] getDataFromPartFile(long partID) {
        Uri partURI = Uri.parse("content://mms/part/" + partID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            InputStream is2 = this.mTeleDBHelper.getInputStream(partURI);
            if (is2 != null) {
                byte[] buffer = new byte[256];
                for (int len = is2.read(buffer); len >= 0; len = is2.read(buffer)) {
                    baos.write(buffer, 0, len);
                }
            }
            if (is2 != null) {
                try {
                    is2.close();
                } catch (IOException e) {
                    String str = LOG_TAG;
                    Log.e(str, "getDataFromPartFile is.close() error: " + e);
                }
            }
            return baos.toByteArray();
        } catch (IOException e2) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "getDataFromPartFile is.close() error: " + e3);
                }
            }
            return null;
        } catch (Throwable th) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e4) {
                    String str3 = LOG_TAG;
                    Log.e(str3, "getDataFromPartFile is.close() error: " + e4);
                }
            }
            throw th;
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 8 */
    private byte[] getFileContentInBytes(String filePath, CloudMessageBufferDBConstants.PayloadEncoding encoding) {
        FileInputStream is;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                is = new FileInputStream(filePath);
                byte[] buffer = new byte[256];
                int read = is.read(buffer);
                while (read >= 0) {
                    baos.write(buffer, 0, read);
                    read = is.read(buffer);
                }
                String str = LOG_TAG;
                Log.i(str, "getFileContentInBytes: " + filePath + " " + encoding + " bytes " + read + " getRcsFilePayloadFromPath, all bytes: " + baos.size());
                if (CloudMessageBufferDBConstants.PayloadEncoding.Base64.equals(encoding)) {
                    byte[] encode = Base64.encode(baos.toByteArray(), 0);
                    is.close();
                    baos.close();
                    return encode;
                }
                byte[] byteArray = baos.toByteArray();
                is.close();
                baos.close();
                return byteArray;
            } catch (Throwable th) {
                baos.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "getRcsFilePayloadFromPath :: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    private void setTransToFrom(Cursor cs, AttributeTranslator trans, Set<String> participants, String remoteURI) {
        String address = ImsUri.parse(remoteURI).getMsisdn();
        trans.setDirection(new String[]{"IN"});
        trans.setFrom(new String[]{address});
        String str = LOG_TAG;
        Log.i(str, "parsed address : " + IMSLog.checker(address) + " participants size: " + participants.size());
        if (participants.size() <= 1) {
            participants.clear();
        } else {
            participants.remove(address);
        }
        participants.add(getE164FormatNumber(this.mCloudMessageManagerHelper.getUserCtn()));
        trans.setTo((String[]) participants.toArray(new String[participants.size()]));
    }

    /* access modifiers changed from: protected */
    public String getE164FormatNumber(String number) {
        String str = LOG_TAG;
        Log.d(str, "getE164FormatNumber: old[" + IMSLog.checker(number) + "]");
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber usNumberProto = phoneUtil.parse(number, "US");
            if (usNumberProto != null && phoneUtil.isValidNumber(usNumberProto)) {
                String e164 = phoneUtil.format(usNumberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
                String str2 = LOG_TAG;
                Log.d(str2, "getE164FormatNumber: E164[" + IMSLog.checker(e164) + "]");
                return e164;
            }
        } catch (NumberParseException e) {
            PrintStream printStream = System.err;
            printStream.println("NumberParseException was thrown: " + e.toString());
        }
        return number;
    }

    private static class MmsParticipant {
        Set<String> mBcc;
        Set<String> mCc;
        Set<String> mFrom;
        Set<String> mTo;

        MmsParticipant(Set<String> from, Set<String> to, Set<String> bcc, Set<String> cc) {
            this.mFrom = from;
            this.mTo = to;
            this.mBcc = bcc;
            this.mCc = cc;
        }
    }
}
