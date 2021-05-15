package com.sec.internal.ims.core.imslogger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ImsLoggerPlus implements ISignallingNotifier {
    private static final String CLS_SERVICE_NAME = "com.sec.imslogger.services.IntentServiceForEvents";
    private static final String DM_SERVICE_NAME = "com.sec.imslogger.services.ImsDmService";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ImsLoggerPlus.class.getSimpleName();
    private static final int MAX_PENDING_QUEUE = 10;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(ImsLoggerPlus.LOG_TAG, "onServiceConnected()");
            IBinder unused = ImsLoggerPlus.this.mDmBinder = service;
            ISignallingNotifier.PackageStatus unused2 = ImsLoggerPlus.this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_CONNECTED;
            boolean unused3 = ImsLoggerPlus.this.mIsBound = true;
            ImsLoggerPlus.this.sendPendingObject();
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(ImsLoggerPlus.LOG_TAG, "onServiceDisconnected()");
            ISignallingNotifier.PackageStatus unused = ImsLoggerPlus.this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_DISCONNECTED;
            IBinder unused2 = ImsLoggerPlus.this.mDmBinder = null;
            boolean unused3 = ImsLoggerPlus.this.mIsBound = false;
            ImsLoggerPlus.this.mPendingQueue.clear();
        }
    };
    private final Context mContext;
    /* access modifiers changed from: private */
    public IBinder mDmBinder;
    /* access modifiers changed from: private */
    public boolean mIsBound = false;
    private final String mPackageName;
    /* access modifiers changed from: private */
    public ISignallingNotifier.PackageStatus mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
    /* access modifiers changed from: private */
    public final BlockingQueue<Object> mPendingQueue = new LinkedBlockingQueue();

    public ImsLoggerPlus(Context context, String name) {
        this.mContext = context;
        this.mPackageName = name;
    }

    public void initialize() {
        String str = LOG_TAG;
        Log.i(str, "name: " + this.mPackageName + " status: " + this.mPackageStatus);
        if (!(this.mPackageStatus == ISignallingNotifier.PackageStatus.NOT_INSTALLED || this.mPackageStatus == ISignallingNotifier.PackageStatus.EMERGENCY_MODE)) {
            startPackage();
        }
        if (allowedDmEvent()) {
            connect();
        }
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus() {
        this.mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        Context context = this.mContext;
        if (context != null) {
            try {
                context.getPackageManager().getServiceInfo(new ComponentName(this.mPackageName, DM_SERVICE_NAME), 128);
                this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_DISCONNECTED;
                if (checkBinderAvailable()) {
                    this.mPackageStatus = ISignallingNotifier.PackageStatus.DM_CONNECTED;
                }
            } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                String str = LOG_TAG;
                Log.i(str, "checkPackageStatus() : " + e);
                return this.mPackageStatus;
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "checkPackageStatus(): " + this.mPackageStatus);
        return this.mPackageStatus;
    }

    private boolean checkBinderAvailable() {
        return this.mDmBinder != null && this.mIsBound;
    }

    @Deprecated
    private boolean allowedDmEvent() {
        if (DEBUG || !SHIPBUILD || DeviceUtil.isOtpAuthorized()) {
            return true;
        }
        disconnectService();
        return false;
    }

    private void startPackage() {
        Intent i = new Intent();
        i.setComponent(new ComponentName(this.mPackageName, CLS_SERVICE_NAME));
        i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_DEBUG_INFO_TYPE, 13);
        this.mContext.startService(i);
        Log.i(LOG_TAG, "Starting ImsLogger+");
    }

    private void connect() {
        if (this.mPackageStatus == ISignallingNotifier.PackageStatus.DM_DISCONNECTED && !this.mIsBound) {
            connectService();
        }
    }

    private void connectService() {
        this.mContext.bindService(new Intent().setClassName(this.mPackageName, DM_SERVICE_NAME), this.mConnection, 1);
    }

    private void disconnectService() {
        if (checkBinderAvailable()) {
            try {
                this.mContext.unbindService(this.mConnection);
            } catch (IllegalArgumentException e) {
            }
            this.mDmBinder = null;
            this.mIsBound = false;
        }
    }

    private void addPendingObject(Object o) {
        String str = LOG_TAG;
        Log.i(str, "addPendingObject size:" + this.mPendingQueue.size());
        if (this.mPendingQueue.size() > 10) {
            this.mPendingQueue.poll();
        }
        this.mPendingQueue.add(o);
    }

    /* access modifiers changed from: private */
    public void sendPendingObject() {
        while (!this.mPendingQueue.isEmpty()) {
            Object o = this.mPendingQueue.peek();
            if (o != null && send(o)) {
                this.mPendingQueue.poll();
                Log.i(LOG_TAG, "succeed send pending requests");
            }
        }
    }

    @Deprecated
    private void sendViaIntentService(Intent i) {
        new Thread(new Runnable(i) {
            public final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ImsLoggerPlus.this.lambda$sendViaIntentService$0$ImsLoggerPlus(this.f$1);
            }
        }).start();
    }

    public /* synthetic */ void lambda$sendViaIntentService$0$ImsLoggerPlus(Intent i) {
        for (ComponentName componentName : new ComponentName[]{new ComponentName(this.mPackageName, CLS_SERVICE_NAME)}) {
            i.setComponent(componentName);
            this.mContext.startService(i);
        }
    }

    public boolean send(Object o) {
        boolean isSent = false;
        if (!allowedDmEvent() || this.mPackageStatus == ISignallingNotifier.PackageStatus.NOT_INSTALLED || this.mPackageStatus == ISignallingNotifier.PackageStatus.EMERGENCY_MODE) {
            return false;
        }
        String str = LOG_TAG;
        Log.i(str, "send() with " + o.getClass().getSimpleName() + " status: " + this.mPackageStatus);
        if (this.mPackageStatus == ISignallingNotifier.PackageStatus.DM_CONNECTED) {
            Parcel p = Parcel.obtain();
            int code = 1;
            try {
                if (Bundle.class.getSimpleName().equals(o.getClass().getSimpleName())) {
                    code = 0;
                }
                p.writeValue(o);
                if (this.mDmBinder != null) {
                    this.mDmBinder.transact(code, p, (Parcel) null, 0);
                    isSent = true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                this.mDmBinder = null;
                this.mIsBound = false;
            } catch (Throwable th) {
                p.recycle();
                throw th;
            }
            p.recycle();
            return isSent;
        } else if (this.mPackageStatus == ISignallingNotifier.PackageStatus.DM_DISCONNECTED) {
            String str2 = LOG_TAG;
            Log.i(str2, "adding request to PendingQueue: " + o.getClass().getSimpleName());
            addPendingObject(o);
            connect();
            return false;
        } else {
            if (Intent.class.getSimpleName().equals(o.getClass().getSimpleName())) {
                sendViaIntentService((Intent) o);
            } else {
                Bundle b = (Bundle) o;
                int notifyType = b.getInt("notifyType");
                if (notifyType == 0 || notifyType == 1) {
                    Intent i = new Intent();
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_DEBUG_INFO_TYPE, 1);
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_SIP_TYPE, b.getInt("msgType"));
                    i.putExtra("Direction", b.getInt("direction"));
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_DEBUG_INFO_TIMESTAMP, b.getString("timestamp"));
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_LOCAL_ADDRESS, b.getString("localIp"));
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_REMOTE_ADDRESS, b.getString("remoteIp"));
                    i.putExtra(ImsDiagnosticMonitorNotificationManager.IMS_SIP_MESSAGE, b.getString("message"));
                    sendViaIntentService(i);
                }
            }
            return true;
        }
    }
}
