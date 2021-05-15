package com.sec.internal.helper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class PreciseAlarmManager {
    protected static final String INTENT_ALARM_TIMEOUT = "com.sec.internal.ims.imsservice.alarmmanager";
    private static final String LOG_TAG = "PreciseAlarmManager";
    private static final int PRECISION = 250;
    private static final int WAKE_LOCK_TIMEOUT = 10000;
    private static volatile PreciseAlarmManager sInstance = null;
    Context mContext = null;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(PreciseAlarmManager.LOG_TAG, "sendMessageDelayed: get intent, get wake lock for 10secs.");
            PreciseAlarmManager.this.mWakeLock.acquire(10000);
        }
    };
    SimpleEventLog mLog = null;
    Thread mThread = null;
    PriorityBlockingQueue<DelayedMessage> mTimers = new PriorityBlockingQueue<>();
    PowerManager.WakeLock mWakeLock;

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:598)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    public static synchronized com.sec.internal.helper.PreciseAlarmManager getInstance(android.content.Context r2) {
        /*
            java.lang.Class<com.sec.internal.helper.PreciseAlarmManager> r0 = com.sec.internal.helper.PreciseAlarmManager.class
            monitor-enter(r0)
            com.sec.internal.helper.PreciseAlarmManager r1 = sInstance     // Catch:{ all -> 0x0027 }
            if (r1 != 0) goto L_0x0023
            monitor-enter(r0)     // Catch:{ all -> 0x0027 }
            com.sec.internal.helper.PreciseAlarmManager r1 = sInstance     // Catch:{ all -> 0x0020 }
            if (r1 != 0) goto L_0x001e
            com.sec.internal.helper.PreciseAlarmManager r1 = new com.sec.internal.helper.PreciseAlarmManager     // Catch:{ all -> 0x0020 }
            r1.<init>(r2)     // Catch:{ all -> 0x0020 }
            sInstance = r1     // Catch:{ all -> 0x0020 }
            boolean r1 = isRoboUnitTest()     // Catch:{ all -> 0x0020 }
            if (r1 != 0) goto L_0x001e
            com.sec.internal.helper.PreciseAlarmManager r1 = sInstance     // Catch:{ all -> 0x0020 }
            r1.start()     // Catch:{ all -> 0x0020 }
        L_0x001e:
            monitor-exit(r0)     // Catch:{ all -> 0x0020 }
            goto L_0x0023
        L_0x0020:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0020 }
            throw r1     // Catch:{ all -> 0x0027 }
        L_0x0023:
            com.sec.internal.helper.PreciseAlarmManager r1 = sInstance     // Catch:{ all -> 0x0027 }
            monitor-exit(r0)
            return r1
        L_0x0027:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.PreciseAlarmManager.getInstance(android.content.Context):com.sec.internal.helper.PreciseAlarmManager");
    }

    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }

    private PreciseAlarmManager(Context context) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ALARM_TIMEOUT);
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        createWakelock();
        this.mLog = new SimpleEventLog(context, LOG_TAG, 500);
    }

    /* access modifiers changed from: private */
    public void registerAlarmManager() {
        synchronized (this.mTimers) {
            if (this.mTimers.size() > 0) {
                Iterator<DelayedMessage> it = this.mTimers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    DelayedMessage msg = it.next();
                    if (!(msg == null || msg.msg == null)) {
                        if (msg.msg.getTarget() != null) {
                            Log.d(LOG_TAG, "next the soonest timer: " + msg.msg.what + " from " + msg.msg.getTarget() + " timeout=" + msg.timeout + " after msec=" + (msg.timeout - SystemClock.elapsedRealtime()));
                            PendingIntent pi = PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_ALARM_TIMEOUT), LogClass.IM_SWITCH_OFF);
                            long msec = msg.timeout - SystemClock.elapsedRealtime();
                            Context context = this.mContext;
                            long j = 0;
                            if (msec > 0) {
                                j = msec;
                            }
                            AlarmTimer.start(context, pi, j);
                        }
                    }
                    Log.e(LOG_TAG, "message is wrong do not handle");
                }
            } else {
                Log.d(LOG_TAG, "No pended alarm Timer. remove the registered timer from alarmManager.");
                AlarmTimer.stop(this.mContext, PendingIntent.getBroadcast(this.mContext, 0, new Intent(INTENT_ALARM_TIMEOUT), LogClass.IM_SWITCH_OFF));
            }
        }
    }

    public synchronized void sendMessageDelayed(String tag, Message msg, long millis) {
        synchronized (this.mTimers) {
            this.mTimers.put(new DelayedMessage(msg, SystemClock.elapsedRealtime() + millis));
            Log.d(LOG_TAG, "sendMessageDelayed: " + millis + ", remaining timers:" + this.mTimers.size());
        }
        wakeLockInfo(tag, msg, millis);
        registerAlarmManager();
    }

    public synchronized void removeMessage(Message msg) {
        Log.d(LOG_TAG, "removeMessage: " + msg.what);
        this.mTimers.remove(new DelayedMessage(msg, 0));
        registerAlarmManager();
    }

    private void start() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    int remaining = PreciseAlarmManager.this.mTimers.size();
                    if (remaining > 0) {
                        long current = SystemClock.elapsedRealtime();
                        Iterator<DelayedMessage> it = PreciseAlarmManager.this.mTimers.iterator();
                        while (it.hasNext()) {
                            DelayedMessage msg = it.next();
                            if (msg != null && msg.msg != null && msg.msg.getTarget() != null) {
                                if (msg.timeout >= current) {
                                    break;
                                }
                                Log.d(PreciseAlarmManager.LOG_TAG, "expiring message " + msg.msg.what + " from " + msg.msg.getTarget() + " timeout=" + msg.timeout);
                                PreciseAlarmManager.this.mWakeLock.acquire(10000);
                                msg.msg.sendToTarget();
                                it.remove();
                                StringBuilder sb = new StringBuilder();
                                sb.append("remaining timers ");
                                sb.append(PreciseAlarmManager.this.mTimers.size());
                                Log.d(PreciseAlarmManager.LOG_TAG, sb.toString());
                            } else {
                                Log.e(PreciseAlarmManager.LOG_TAG, "message is wrong do not handle");
                                it.remove();
                            }
                        }
                        if (PreciseAlarmManager.this.mTimers.size() != remaining) {
                            PreciseAlarmManager.this.registerAlarmManager();
                        }
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.mThread = thread;
        thread.start();
    }

    private static class DelayedMessage implements Comparable<DelayedMessage> {
        /* access modifiers changed from: private */
        public Message msg;
        /* access modifiers changed from: private */
        public long timeout;

        public DelayedMessage(Message msg2, long timeout2) {
            this.msg = msg2;
            this.timeout = timeout2;
        }

        public int compareTo(DelayedMessage another) {
            return (int) (this.timeout - another.timeout);
        }

        public int hashCode() {
            Message message = this.msg;
            if (message != null) {
                return (1 * 7) + message.hashCode();
            }
            return 1;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            DelayedMessage other = (DelayedMessage) obj;
            Message message = this.msg;
            if (message == null) {
                if (other.msg == null) {
                    return true;
                }
                return false;
            } else if (message.what == other.msg.what && this.msg.getTarget() == other.msg.getTarget() && this.msg.arg1 == other.msg.arg1) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void createWakelock() {
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "ImsService");
    }

    private void wakeLockInfo(String tag, Message msg, long after) {
        if (msg != null) {
            SimpleEventLog simpleEventLog = this.mLog;
            simpleEventLog.add(tag + "(" + msg.what + ") : " + after);
            return;
        }
        SimpleEventLog simpleEventLog2 = this.mLog;
        simpleEventLog2.add(tag + " : " + after);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        this.mLog.dump();
    }
}
