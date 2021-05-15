package com.sec.internal.ims.servicemodules.euc.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.servicemodules.euc.locale.IDeviceLocale;
import java.util.Locale;

public class DeviceLocale extends BroadcastReceiver implements IDeviceLocale {
    public static final String DEFAULT_LANG_VALUE = "def";
    private static final IntentFilter LOCALE_SET_INTENT_FILTER = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
    private static final String LOG_TAG = DeviceLocale.class.getSimpleName();
    private final Context mContext;
    /* access modifiers changed from: private */
    public IDeviceLocale.IDeviceLocaleListener mDeviceLocaleListener;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private boolean mStarted = false;

    public DeviceLocale(Context context, Handler handler) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mHandler = (Handler) Preconditions.checkNotNull(handler);
    }

    public void start(IDeviceLocale.IDeviceLocaleListener listener) throws IllegalStateException {
        Log.d(LOG_TAG, "start");
        Preconditions.checkState(!this.mStarted, "Already started!");
        this.mStarted = true;
        this.mDeviceLocaleListener = (IDeviceLocale.IDeviceLocaleListener) Preconditions.checkNotNull(listener);
        this.mContext.registerReceiver(this, LOCALE_SET_INTENT_FILTER);
        getDeviceLocale(new IDeviceLocale.ICallback() {
            public void onResult(Locale locale) {
                DeviceLocale.this.mDeviceLocaleListener.onLocaleChanged(locale);
            }
        });
    }

    public void stop() throws IllegalStateException {
        Log.d(LOG_TAG, "stop");
        Preconditions.checkState(this.mStarted, "Not started!");
        this.mStarted = false;
        this.mContext.unregisterReceiver(this);
    }

    public void getDeviceLocale(final IDeviceLocale.ICallback callback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                final Locale locale = DeviceLocale.this.getLocale();
                DeviceLocale.this.mHandler.post(new Runnable() {
                    public void run() {
                        callback.onResult(locale);
                    }
                });
            }
        });
    }

    public String getLanguageCode(Locale locale) {
        return locale.toString().substring(0, 2);
    }

    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        final Locale locale = getLocale();
        this.mHandler.post(new Runnable() {
            public void run() {
                DeviceLocale.this.mDeviceLocaleListener.onLocaleChanged(locale);
            }
        });
    }

    /* access modifiers changed from: private */
    public Locale getLocale() {
        return this.mContext.getResources().getConfiguration().getLocales().get(0);
    }
}
