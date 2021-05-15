package com.sec.internal.ims.config;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import java.util.Arrays;
import java.util.List;

public class PowerController {
    /* access modifiers changed from: private */
    public final String LOG_TAG = PowerController.class.getSimpleName();
    protected AlarmManager mAlarmManager = null;
    private final Context mContext;
    protected PendingIntent mPendingIntent = null;
    protected final Receiver mReceiver = new Receiver();
    protected State mState = null;
    /* access modifiers changed from: private */
    public long mTimeout = 0;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    interface State {
        void lock();

        void release();

        void sleep(long j);
    }

    public PowerController(Context context, long timeout) {
        Log.i(this.LOG_TAG, "PowerController");
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "PowerController");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(this.mReceiver.ACTION_SLEEP_ALARM_EXPIRED), 0);
        Context context2 = this.mContext;
        Receiver receiver = this.mReceiver;
        context2.registerReceiver(receiver, receiver.getIntentFilter());
        this.mTimeout = timeout;
        this.mState = new ReleaseState();
    }

    public void lock() {
        this.mState.lock();
    }

    public void release() {
        this.mState.release();
    }

    public void sleep(long time) {
        this.mState.sleep(time);
    }

    public void cleanup() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    class ReleaseState implements State {
        public ReleaseState() {
            Log.i(PowerController.this.LOG_TAG, "ReleaseState");
            if (PowerController.this.mWakeLock.isHeld()) {
                PowerController.this.mWakeLock.release();
            }
        }

        public synchronized void lock() {
            PowerController.this.mState = new LockState();
        }

        public synchronized void release() {
            Log.i(PowerController.this.LOG_TAG, "already released");
        }

        public synchronized void sleep(long time) {
            String access$000 = PowerController.this.LOG_TAG;
            Log.i(access$000, "+++ sleep start:" + time);
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(PowerController.this.LOG_TAG, "--- sleep end");
        }
    }

    class LockState implements State {
        public LockState() {
            Log.i(PowerController.this.LOG_TAG, "LockState");
            PowerController.this.mWakeLock.acquire(PowerController.this.mTimeout);
        }

        public synchronized void lock() {
            Log.i(PowerController.this.LOG_TAG, "already locked");
        }

        public synchronized void release() {
            PowerController.this.mState = new ReleaseState();
        }

        public synchronized void sleep(long time) {
            String access$000 = PowerController.this.LOG_TAG;
            Log.i(access$000, "+++ sleep start:" + time);
            PowerController.this.mAlarmManager.setExact(0, System.currentTimeMillis() + (time - (time > 1000 ? 100 : time / 10)), PowerController.this.mPendingIntent);
            release();
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PowerController.this.mState.lock();
            PowerController.this.mAlarmManager.cancel(PowerController.this.mPendingIntent);
            Log.i(PowerController.this.LOG_TAG, "--- sleep end");
        }
    }

    protected class Receiver extends BroadcastReceiver {
        public final String ACTION_SLEEP_ALARM_EXPIRED;
        private IntentFilter mIntentFilter = null;
        private final List<String> mIntentFilterAction;

        public Receiver() {
            String str = Receiver.class.getName() + ".SLEEP_ALARM_EXPIRED";
            this.ACTION_SLEEP_ALARM_EXPIRED = str;
            this.mIntentFilterAction = Arrays.asList(new String[]{str});
            Log.i(PowerController.this.LOG_TAG, "Receiver");
            this.mIntentFilter = new IntentFilter();
            for (String action : this.mIntentFilterAction) {
                this.mIntentFilter.addAction(action);
            }
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(PowerController.this.LOG_TAG, intent.getAction());
            if (intent.getAction().equals(this.ACTION_SLEEP_ALARM_EXPIRED)) {
                Log.i(PowerController.this.LOG_TAG, "received alarm expired. acquire wake lock");
                PowerController.this.mState.lock();
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }
}
