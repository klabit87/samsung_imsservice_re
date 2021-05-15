package com.sec.internal.ims.cmstore;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.sec.ims.settings.ImsSettings;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageBufferDBConstants;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.servicecontainer.CentralMsgStoreInterface;
import com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IBufferDBEventListener;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.GCMPushNotification;

public class CloudMessageService extends Service implements IBufferDBEventListener, IUIEventCallback {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CloudMessageService.class.getSimpleName();
    private HandlerThread mBufferDBHandlingThread;
    private CentralMsgStoreInterface mCentralMsgStoreWrapper;
    private CloudMessageBufferSchedulingHandler mCloudMessageScheduler = null;
    BroadcastReceiver mJanskyIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String access$000 = CloudMessageService.LOG_TAG;
            Log.d(access$000, "mJanskyIntentReceiver: onReceive " + intent);
            if (CloudMessageService.this.mNetAPIWorkingController.getCmsProfileEnabled()) {
                CloudMessageService.this.mJanskyTranslation.handleIntent(intent);
            } else {
                Log.d(CloudMessageService.LOG_TAG, "mJanskyIntentReceiver: CmsProfileEnabled false");
            }
        }
    };
    /* access modifiers changed from: private */
    public JanskyIntentTranslation mJanskyTranslation;
    private HandlerThread mNetAPIHandlingThread;
    /* access modifiers changed from: private */
    public NetAPIWorkingStatusController mNetAPIWorkingController = null;

    public IBinder onBind(Intent arg0) {
        Log.d(LOG_TAG, "onBind():");
        return this.mCentralMsgStoreWrapper.getBinder();
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
        ATTGlobalVariables.setAmbsPhaseVersion(getAMBSPhaseVersion(0));
        this.mBufferDBHandlingThread = new HandlerThread("cloud message service buffer DB thread");
        this.mNetAPIHandlingThread = new HandlerThread("cloud message service NetAPI thread");
        this.mBufferDBHandlingThread.start();
        this.mNetAPIHandlingThread.start();
        Looper bufferDBLooper = this.mBufferDBHandlingThread.getLooper();
        Looper netAPILooper = this.mNetAPIHandlingThread.getLooper();
        CloudMessagePreferenceManager.init(getApplicationContext());
        this.mNetAPIWorkingController = new NetAPIWorkingStatusController(netAPILooper, getApplicationContext(), this, new RetryStackAdapterHelper());
        this.mCloudMessageScheduler = new CloudMessageBufferSchedulingHandler(bufferDBLooper, getApplicationContext(), this.mNetAPIWorkingController, this, new CloudMessageManagerHelper());
        this.mJanskyTranslation = new JanskyIntentTranslation(getApplicationContext(), this);
        this.mCentralMsgStoreWrapper = new CentralMsgStoreInterface(this.mCloudMessageScheduler, this.mNetAPIWorkingController, this.mJanskyTranslation, new CloudMessageManagerHelper());
        registerJanskyIntentReceiver(getApplicationContext());
        this.mCloudMessageScheduler.resyncPendingMsg();
    }

    private void registerJanskyIntentReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.samsung.nsds.action.LINE_ACTIVATED");
        intentFilter.addAction("com.samsung.nsds.action.LINE_DEACTIVATED");
        intentFilter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_ACTIVATED);
        intentFilter.addAction(NSDSNamespaces.NSDSActions.SIM_DEVICE_DEACTIVATED);
        intentFilter.addAction(NSDSNamespaces.NSDSActions.RECEIVED_PUSH_NOTIFICATION);
        intentFilter.addAction(NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION);
        intentFilter.addAction("com.samsung.nsds.action.SIT_REFRESHED");
        context.registerReceiver(this.mJanskyIntentReceiver, intentFilter);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        String str = LOG_TAG;
        Log.i(str, "onStartCommand(): Received start id " + startId + ": " + intent);
        return 1;
    }

    public void notifyCloudMessageUpdate(String apptype, String msgtype, String rowIDs) {
        this.mCentralMsgStoreWrapper.notifyCloudMessageUpdate(apptype, msgtype, rowIDs);
    }

    public void notifyAppInitialSyncStatus(String apptype, String msgtype, String line, CloudMessageBufferDBConstants.InitialSyncStatusFlag SyncStatus) {
        this.mCentralMsgStoreWrapper.notifyAppInitialSyncStatus(apptype, msgtype, line, SyncStatus);
    }

    public void notifyAppCloudDeleteFail(String apptype, String msgtype, String rowIds) {
        this.mCentralMsgStoreWrapper.notifyAppCloudDeleteFail(apptype, msgtype, rowIds);
    }

    public void notifyUIScreen(int screenName, String message, int param) {
        this.mCentralMsgStoreWrapper.notifyUIScreen(screenName, message, param);
    }

    public void onJanskySITTokenUpdated(String line) {
        this.mNetAPIWorkingController.onDeviceSITRefreshed(line);
    }

    public void onMStoreEnabled(String msisdn) {
        String str = LOG_TAG;
        Log.d(str, "onMStoreEnabled: " + IMSLog.checker(msisdn) + ", cmsenabled: " + this.mNetAPIWorkingController.getCmsProfileEnabled());
        if (this.mNetAPIWorkingController.getCmsProfileEnabled()) {
            this.mCloudMessageScheduler.activateLine(msisdn);
        }
    }

    public void onMStoreDisabled(String msisdn) {
        String str = LOG_TAG;
        Log.d(str, "onMStoreDisabled: " + IMSLog.checker(msisdn) + ", cmsenabled: " + this.mNetAPIWorkingController.getCmsProfileEnabled());
        if (this.mNetAPIWorkingController.getCmsProfileEnabled()) {
            this.mCloudMessageScheduler.deActivateLine(msisdn);
        }
    }

    public void onGcmReceived(GCMPushNotification pushnotification) {
        String str = LOG_TAG;
        Log.d(str, "onGcmReceived: " + pushnotification + ", cmsenabled: " + this.mNetAPIWorkingController.getCmsProfileEnabled());
        if (this.mNetAPIWorkingController.getCmsProfileEnabled()) {
            this.mCloudMessageScheduler.onGcmReceived(pushnotification);
        }
    }

    public void onNativeChannelReceived(ParamOMAresponseforBufDB para) {
        String str = LOG_TAG;
        Log.d(str, "onNativeChannelReceived: " + para + ", cmsenabled: " + this.mNetAPIWorkingController.getCmsProfileEnabled());
        if (this.mNetAPIWorkingController.getCmsProfileEnabled()) {
            this.mCloudMessageScheduler.onNativeChannelReceived(para);
        }
    }

    public void updateSubscriptionChannel() {
        this.mNetAPIWorkingController.updateSubscriptionChannel();
    }

    public void removeUpdateSubscriptionChannelEvent() {
        this.mNetAPIWorkingController.removeUpdateSubscriptionChannelEvent();
    }

    public void handleLargeDataPolling() {
        this.mNetAPIWorkingController.handleLargeDataPolling();
    }

    public void showInitsyncIndicator(boolean isShow) {
        if (CloudMessageStrategyManager.getStrategy().getIsInitSyncIndicatorRequired()) {
            String str = LOG_TAG;
            Log.v(str, "showInitsyncIndicator: " + isShow);
            NotificationManager notifyManager = (NotificationManager) getApplicationContext().getSystemService("notification");
            if (notifyManager == null) {
                Log.d(LOG_TAG, "mNotifyManager is null");
                return;
            }
            NotificationChannel channel = new NotificationChannel("ambs_channel", "ambs_channel_ni", 2);
            channel.setLockscreenVisibility(0);
            notifyManager.createNotificationChannel(channel);
            Notification.Builder notiBuilder = new Notification.Builder(getApplicationContext(), "ambs_channel");
            if (isShow) {
                Log.d(LOG_TAG, "init sync will be displayed");
                notiBuilder.setContentTitle("Messages Backup & Sync").setContentText("Messages syncing...").setSmallIcon(17301599).setOngoing(true).setAutoCancel(false);
                notifyManager.notify(1, notiBuilder.build());
                return;
            }
            notifyManager.cancel(1);
            return;
        }
        Log.v(LOG_TAG, "showInitsyncIndicator: not supported");
    }

    /* Debug info: failed to restart local var, previous not found, register: 9 */
    private int getAMBSPhaseVersion(int phoneid) {
        Cursor cursor;
        int phase = 3;
        Uri.Builder buildUpon = ImsSettings.GLOBAL_CONTENT_URI.buildUpon();
        try {
            cursor = getApplicationContext().getContentResolver().query(buildUpon.fragment(ImsConstants.Uris.FRAGMENT_SIM_SLOT + phoneid).build(), new String[]{GlobalSettingsConstants.RCS.RCS_PHASE_VERSION}, (String) null, (String[]) null, (String) null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return 3;
            } else if (cursor.getCount() == 0) {
                if (cursor != null) {
                    cursor.close();
                }
                return 3;
            } else {
                if (cursor.moveToFirst() && "RCS_ATT_PHASE2".equals(cursor.getString(0))) {
                    phase = 4;
                }
                if (cursor != null) {
                    cursor.close();
                }
                String str = LOG_TAG;
                Log.d(str, "get RCS_PHASE_VERSION: " + phase);
                return phase;
            }
        } catch (SQLiteException | IllegalArgumentException e) {
            String str2 = LOG_TAG;
            Log.d(str2, "!!!Could not get RCS_PHASE_VERSION from db " + e.toString());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mBufferDBHandlingThread.getLooper().quitSafely();
        this.mNetAPIHandlingThread.getLooper().quitSafely();
    }
}
