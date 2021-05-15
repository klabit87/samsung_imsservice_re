package com.sec.internal.ims.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.ims.ImsConstants;

public class MessagingAppInfoReceiver extends BroadcastReceiver {
    private static final String ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
    private static final String DATA_SCHEME_PACKAGE = "package";
    private static final String DEFAULT_PACKAGE_NAME = "com.samsung.android.messaging";
    private static final String LOG_TAG = MessagingAppInfoReceiver.class.getSimpleName();
    private final Context mContext;
    private final IntentFilter mFilter = new IntentFilter();
    private boolean mIsRegistered;
    private final IMessagingAppInfoListener mListener;
    private final String mPackageName;

    public MessagingAppInfoReceiver(Context context, IMessagingAppInfoListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.mPackageName = SemFloatingFeature.getInstance().getString(ImsConstants.SecFloatingFeatures.CONFIG_PACKAGE_NAME, "com.samsung.android.messaging");
        this.mFilter.addAction(ACTION_PACKAGE_REPLACED);
        this.mFilter.addDataScheme(DATA_SCHEME_PACKAGE);
        this.mFilter.addDataSchemeSpecificPart(this.mPackageName, 0);
    }

    public void onReceive(Context context, Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onReceive(): intent - " + intent);
        this.mListener.onMessagingAppPackageReplaced();
    }

    public void registerReceiver() {
        String str = LOG_TAG;
        Log.d(str, "registerReceiver(): IsRegistered = " + this.mIsRegistered + ", PackageName = " + this.mPackageName);
        if (!this.mIsRegistered) {
            this.mContext.registerReceiver(this, this.mFilter);
            this.mIsRegistered = true;
        }
    }

    public void unregisterReceiver() {
        String str = LOG_TAG;
        Log.d(str, "unregisterReceiver(): IsRegistered = " + this.mIsRegistered);
        if (this.mIsRegistered) {
            this.mContext.unregisterReceiver(this);
            this.mIsRegistered = false;
        }
    }

    public String getMessagingAppVersion() {
        String version = "";
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(this.mPackageName, 0);
            if (packageInfo != null) {
                version = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "getMessagingAppVersion(): Cannot find the package.");
        }
        String str = LOG_TAG;
        Log.d(str, "getMessagingAppVersion(): " + this.mPackageName + " - " + version);
        return version;
    }
}
