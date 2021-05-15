package com.sec.internal.ims.cmstore.omanetapi.bufferdbtranslation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;

public class BufferQueryDBTranslation {
    protected static final Uri CONTENT_URI_BUFFERDB = Uri.parse("content://com.samsung.rcs.cmstore");
    private static final String LOG_TAG = BufferQueryDBTranslation.class.getSimpleName();
    public static final String PROVIDER_NAME_BUFFERDB = "com.samsung.rcs.cmstore";
    private Context mContext;
    protected final ContentResolver mResolver;

    public enum MessageType {
        MESSAGE_CHAT,
        MESSAGE_SLM,
        MESSAGE_FT
    }

    public BufferQueryDBTranslation(Context context) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmGreetingBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMGREETING + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryFaxBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_FAX + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSParticipantDB(String chatId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSPARTICIPANTS + "/" + chatId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSSessionDB(String chatId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSSESSION + "/" + chatId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querySMSBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_SMSMESSAGES + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querySummaryDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_SUMMARYTABLE + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSNotificationDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + "notification" + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSMessageDBUsingRowId(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSMESSAGES + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryRCSMessageDBUsingImdn(String imdnId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSMESSAGEIMDN + "/" + imdnId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmDataBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMMESSAGES + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryCallLogDataBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_CALLLOG + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor querymmsPduBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPDUMESSAGE + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryAddrBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSADDRMESSAGES + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryPartsBufferDBUsingPduBufferId(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPARTMESSAGES_PDUID + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryPartsBufferDBUsingPartBufferId(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_MMSPARTMESSAGES_PARTID + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryrcsMessageBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_RCSCHATMESSAGE + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmPinBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMPIN + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* access modifiers changed from: protected */
    public Cursor queryVvmProfileBufferDB(long rowId) {
        return this.mResolver.query(Uri.parse(CONTENT_URI_BUFFERDB + "/" + CloudMessageProviderContract.CONTENTPRDR_VVMPROFILE + "/" + rowId), (String[]) null, (String) null, (String[]) null, (String) null);
    }
}
