package com.sec.internal.ims.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.interfaces.ims.core.INtpTimeChangedListener;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NtpTimeController extends Handler implements INtpTimeController {
    private static final String LOG_TAG = NtpTimeController.class.getSimpleName();
    private boolean isForceRefreshed = false;
    private Context mContext;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ArrayList<INtpTimeChangedListener> mNtpTimeChangedListnerList = new ArrayList<>();
    private long mNtpTimeOffset = 0;
    private NtpTrustedTime mNtpTrustedTime;

    public NtpTimeController(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        this.mNtpTrustedTime = NtpTrustedTime.getInstance(context);
    }

    public void registerNtpTimeChangedListener(INtpTimeChangedListener listener) {
        boolean alreadyRegistered = this.mNtpTimeChangedListnerList.contains(listener);
        String str = LOG_TAG;
        IMSLog.s(str, "registerNtpTimeChangedListener: alreadyRegistered=" + alreadyRegistered);
        if (!alreadyRegistered && listener != null) {
            try {
                this.mNtpTimeChangedListnerList.add(listener);
                listener.onNtpTimeOffsetChanged(this.mNtpTimeOffset);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public void unregisterNtpTimeChangedListener(INtpTimeChangedListener listener) {
        IMSLog.s(LOG_TAG, "unregisterNtpTimeChangedListener:");
        if (listener != null) {
            try {
                this.mNtpTimeChangedListnerList.remove(listener);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public void refreshNtpTime() {
        requestNtpTime(true);
    }

    private void updateNtpTimeOffset(long ntpTimeOffset, int duration) {
        String str = LOG_TAG;
        Log.i(str, "updateNtpTimeOffset (" + duration + ") : " + ntpTimeOffset);
        this.mNtpTimeOffset = ntpTimeOffset;
        StackIF.getInstance().updateNtpTimeOffset(ntpTimeOffset);
        sendNtpTimeOffsetChanged(ntpTimeOffset);
    }

    private void sendNtpTimeOffsetChanged(long ntpTimeOffset) {
        Iterator<INtpTimeChangedListener> it = this.mNtpTimeChangedListnerList.iterator();
        while (it.hasNext()) {
            try {
                it.next().onNtpTimeOffsetChanged(ntpTimeOffset);
            } catch (Exception e) {
                Log.e(LOG_TAG, "sendNtpTimeOffsetChanged failed", e);
            }
        }
    }

    public void initSequentially() {
        requestNtpTime(false);
    }

    private synchronized void requestNtpTime(boolean forceRefresh) {
        boolean isAutomaticTimeRequested = isAutomaticTimeRequested(this.mContext);
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("requestNtpTime : forceRefresh=");
        sb.append(forceRefresh);
        sb.append(" isForceRefreshed=");
        sb.append(this.isForceRefreshed);
        sb.append(" isAutomaticTimeRequested=");
        sb.append(isAutomaticTimeRequested);
        sb.append(" hasCache=");
        sb.append(this.mNtpTrustedTime != null ? Boolean.valueOf(this.mNtpTrustedTime.hasCache()) : "null");
        Log.i(str, sb.toString());
        try {
            if (this.isForceRefreshed) {
                sendNtpTimeOffsetChanged(this.mNtpTimeOffset);
            } else if (forceRefresh) {
                this.mExecutorService.submit(new Runnable() {
                    public final void run() {
                        NtpTimeController.this.lambda$requestNtpTime$0$NtpTimeController();
                    }
                });
            } else if (isAutomaticTimeRequested) {
                updateNtpTimeOffset(System.currentTimeMillis() - SystemClock.elapsedRealtime(), 0);
            } else {
                updateNtpTimeOffset(-1, 0);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return;
    }

    public /* synthetic */ void lambda$requestNtpTime$0$NtpTimeController() {
        try {
            if (this.mNtpTrustedTime != null && !this.mNtpTrustedTime.hasCache()) {
                long start = System.currentTimeMillis();
                if (this.mNtpTrustedTime.forceRefresh()) {
                    updateNtpTimeOffset(this.mNtpTrustedTime.currentTimeMillis() - this.mNtpTrustedTime.getCachedNtpTimeReference(), (int) (System.currentTimeMillis() - start));
                    this.isForceRefreshed = true;
                    return;
                }
                IMSLog.s(LOG_TAG, "forceRefresh failed");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private static boolean isAutomaticTimeRequested(Context ctx) {
        boolean z = false;
        if (Settings.Global.getInt(ctx.getContentResolver(), "auto_time", 0) != 0) {
            z = true;
        }
        boolean value = z;
        String str = LOG_TAG;
        IMSLog.s(str, "isAutomaticTimeRequested : " + value);
        return value;
    }
}
