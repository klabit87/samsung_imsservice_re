package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.PowerManager;
import android.os.SemSystemProperties;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class KeepAliveSender {
    private static final int KEEPALIVE_INTERVAL = 2000;
    private static final int KEEPALIVE_INTERVAL_CMCC = 8000;
    private String LOG_TAG = KeepAliveSender.class.getSimpleName();
    private Context mContext = null;
    String mIpAddr;
    private volatile boolean mIsRunning = false;
    private final Object mLock = new Object();
    private Mno mMno = Mno.DEFAULT;
    int mPort;
    private ImsRegistration mRegistration = null;
    private Thread mTask = null;
    private PowerManager.WakeLock mWakeLock = null;

    public KeepAliveSender(Context context, ImsRegistration regi, String ipAddr, int port, Mno mno) {
        this.mContext = context;
        this.mRegistration = regi;
        this.mMno = mno;
        this.mIpAddr = ipAddr;
        this.mPort = port;
        if (0 == 0) {
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, this.LOG_TAG + "KeepAlive");
        }
    }

    public void start() {
        Log.i(this.LOG_TAG, "KeepAliveSender: start: ");
        if (this.mTask != null) {
            Log.i(this.LOG_TAG, "KeepAliveSender: start() - already running.");
        } else if (SemSystemProperties.getBoolean("persist.sys.ims.blockvzwka", false)) {
            Log.i(this.LOG_TAG, "KeepAliveSender: blocked by system properties!");
        } else {
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                wakeLock.acquire();
                Log.i(this.LOG_TAG, "KeepAliveSender: acquire WakeLock");
            }
            this.mIsRunning = true;
            Thread thread = new Thread(new Runnable() {
                public final void run() {
                    KeepAliveSender.this.lambda$start$0$KeepAliveSender();
                }
            });
            this.mTask = thread;
            thread.start();
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 12 */
    public /* synthetic */ void lambda$start$0$KeepAliveSender() {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket(45016);
            InetAddress addr = InetAddress.getByName(this.mIpAddr);
            byte[] dummy = {13, 10, 13, 10};
            if (this.mRegistration != null) {
                this.mRegistration.getNetwork().bindSocket(s);
            }
            boolean mNeedSend = false;
            while (true) {
                if (!(this.mMno == Mno.CMCC || this.mMno == Mno.VIVA_BAHRAIN || this.mMno == Mno.ETISALAT_UAE) || mNeedSend) {
                    String str = this.LOG_TAG;
                    Log.i(str, "KeepAliveSender: send dummy.txt.txt UDP to [" + this.mIpAddr + "]:" + this.mPort + " ...");
                    s.send(new DatagramPacket(dummy, dummy.length, addr, this.mPort));
                }
                if (this.mMno.isOneOf(Mno.CMCC, Mno.VIVA_BAHRAIN, Mno.ETISALAT_UAE)) {
                    Thread.sleep(8000);
                } else {
                    Thread.sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                }
                mNeedSend = true;
                synchronized (this.mLock) {
                    if (!this.mIsRunning) {
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (s != null) {
            s.close();
        }
        this.mIsRunning = false;
    }

    public void stop() {
        if (this.mTask != null) {
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null && wakeLock.isHeld()) {
                this.mWakeLock.release();
                Log.i(this.LOG_TAG, "KeepAliveSender: release WakeLock");
            }
            Log.i(this.LOG_TAG, "KeepAliveSender: stop");
            synchronized (this.mLock) {
                this.mIsRunning = false;
            }
            this.mTask.interrupt();
            try {
                this.mTask.join(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.mTask = null;
        }
    }
}
