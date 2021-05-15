package com.sec.internal.ims.servicemodules.csh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.servicemodules.csh.IshIntents;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;

public class IshTranslation {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = IshTranslation.class.getSimpleName();
    private static final IntentFilter sIntentFilter;
    private Context mContext;
    private BroadcastReceiver mIntentReceiver;
    private ImageShareModule mServiceModule;

    static {
        IntentFilter intentFilter = new IntentFilter();
        sIntentFilter = intentFilter;
        intentFilter.addAction(IshIntents.IshIntent.ACTION_SHARE_CONTENT);
        sIntentFilter.addAction(IshIntents.IshIntent.ACTION_SHARE_ACCEPT);
        sIntentFilter.addAction(IshIntents.IshIntent.ACTION_SHARE_CANCEL);
    }

    public IshTranslation(Context context, ImageShareModule ishServiceModule) {
        AnonymousClass1 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String access$000 = IshTranslation.LOG_TAG;
                Log.i(access$000, "Received intent: " + action);
                if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_CONTENT)) {
                    IshTranslation.this.requestNewShare(intent);
                } else if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_ACCEPT)) {
                    IshTranslation.this.requestAcceptShare(intent);
                } else if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_CANCEL)) {
                    IshTranslation.this.requestCancelShare(intent);
                }
            }
        };
        this.mIntentReceiver = r0;
        this.mContext = context;
        this.mServiceModule = ishServiceModule;
        context.registerReceiver(r0, sIntentFilter);
    }

    /* access modifiers changed from: private */
    public void requestNewShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestNewShare: extras " + extras);
        this.mServiceModule.createShare(ImsUri.parse(((Uri) extras.getParcelable(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI)).toString()), extras.getString(ICshConstants.ExtraInformation.EXTRA_FILE_PATH));
    }

    /* access modifiers changed from: private */
    public void requestAcceptShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestAcceptShare: extras " + extras);
        this.mServiceModule.acceptShare(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1));
    }

    /* access modifiers changed from: private */
    public void requestCancelShare(Intent intent) {
        Bundle extras = intent.getExtras();
        String str = LOG_TAG;
        Log.d(str, "requestCancelShare: extras " + extras);
        this.mServiceModule.cancelShare(extras.getLong(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, -1));
    }

    public void broadcastOutgoingSucceeded(long sharedId, ImsUri contactUri, String filePath) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_CREATED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_FILE_PATH, filePath);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        broadcastIntent(intent);
    }

    public void broadcastIncomming(long sharedId, ImsUri contactUri, String filePath, long fileSize) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_INCOMING);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_TYPE, 1);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_FILE_PATH, filePath);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_BYTES_TOTAL, Long.valueOf(fileSize).intValue());
        broadcastIntent(intent);
    }

    public void broadcastProgress(long sharedId, ImsUri contactUri, long bytesDone, long fileSize) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_PROGRESS);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_BYTES_DONE, Long.valueOf(bytesDone).intValue());
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_BYTES_TOTAL, Long.valueOf(fileSize).intValue());
        broadcastIntent(intent);
    }

    public void broadcastCompleted(long sharedId, ImsUri contactUri) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_COMPLETED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        broadcastIntent(intent);
    }

    public void broadcastCanceled(long sharedId, ImsUri contactUri, int shareDirection, int notificationReason) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_CANCELED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_REASON, notificationReason);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_DIRECTION, shareDirection);
        broadcastIntent(intent);
    }

    public void broadcastConnected(long sharedId, ImsUri contactUri) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_CONNECTED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        broadcastIntent(intent);
    }

    public void broadcastLimitExceeded(long sharedId, ImsUri contactUri) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_LIMIT_EXCEEDED);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_SHARE_ID, sharedId);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_CONTACT_URI, Uri.parse(contactUri.toString()));
        broadcastIntent(intent);
    }

    public void broadcastCommunicationError() {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_COMMUNICATION_ERROR);
        broadcastIntent(intent);
    }

    public void broadcastInvalidDataPath(String filePath) {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_FILE_PATH_ERROR);
        intent.putExtra(ICshConstants.ExtraInformation.EXTRA_FILE_PATH, filePath);
        broadcastIntent(intent);
    }

    public void broadcastServiceReady() {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_SERVICE_READY);
        broadcastIntent(intent);
    }

    public void broadcastServiceNotReady() {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_SHARE_SERVICE_NOT_READY);
        broadcastIntent(intent);
    }

    public void broadcastCshServiceNotReady() {
        Intent intent = new Intent();
        intent.addCategory(IshIntents.IshNotificationIntent.CATEGORY_NOTIFICATION);
        intent.setAction(IshIntents.IshNotificationIntent.NOTIFICATION_CSH_SERVICE_NOT_READY);
        broadcastIntent(intent);
    }

    public void broadcastSystemRefresh(String dataPath) {
        Context context = this.mContext;
        MediaScannerConnection.scanFile(context, new String[]{"file://" + dataPath}, (String[]) null, (MediaScannerConnection.OnScanCompletedListener) null);
    }

    public void broadcastIntent(Intent intent) throws NullPointerException {
        String str = LOG_TAG;
        Log.d(str, intent.toString() + intent.getExtras());
        IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        Log.i(str, "Received intent: " + action);
        if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_CONTENT)) {
            requestNewShare(intent);
        } else if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_ACCEPT)) {
            requestAcceptShare(intent);
        } else if (TextUtils.equals(action, IshIntents.IshIntent.ACTION_SHARE_CANCEL)) {
            requestCancelShare(intent);
        }
    }
}
