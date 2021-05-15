package com.sec.internal.ims.core.imslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;

public class ExternalPackage implements ISignallingNotifier {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ExternalPackage.class.getSimpleName();
    private Context mContext;
    /* access modifiers changed from: private */
    public String mPackageName;
    /* access modifiers changed from: private */
    public ISignallingNotifier.PackageStatus mPackageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;

    public ExternalPackage(Context context, String name) {
        this.mContext = context;
        this.mPackageName = name;
        this.mPackageStatus = checkPackageStatus();
        String str = LOG_TAG;
        Log.i(str, "name: " + this.mPackageName + " status: " + this.mPackageStatus);
        registerPackageAction();
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus() {
        ISignallingNotifier.PackageStatus status;
        ISignallingNotifier.PackageStatus packageStatus = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        try {
            int componentState = this.mContext.getPackageManager().getApplicationEnabledSetting(this.mContext.getPackageManager().getPackageInfo(this.mPackageName, 1).packageName);
            if (SemEmergencyManager.isEmergencyMode(this.mContext) || componentState >= 2) {
                status = ISignallingNotifier.PackageStatus.EMERGENCY_MODE;
                String str = LOG_TAG;
                Log.i(str, "checkPackageStatus(): " + status);
                return status;
            }
            status = ISignallingNotifier.PackageStatus.INSTALLED;
            String str2 = LOG_TAG;
            Log.i(str2, "checkPackageStatus(): " + status);
            return status;
        } catch (PackageManager.NameNotFoundException e) {
            status = ISignallingNotifier.PackageStatus.NOT_INSTALLED;
        }
    }

    private void registerPackageAction() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (("package:" + ExternalPackage.this.mPackageName).equalsIgnoreCase(intent.getData().toString())) {
                    ISignallingNotifier.PackageStatus unused = ExternalPackage.this.mPackageStatus = "android.intent.action.PACKAGE_ADDED".equalsIgnoreCase(intent.getAction()) ? ISignallingNotifier.PackageStatus.INSTALLED : ISignallingNotifier.PackageStatus.NOT_INSTALLED;
                    String access$200 = ExternalPackage.LOG_TAG;
                    Log.i(access$200, "name: " + ExternalPackage.this.mPackageName + " status: " + ExternalPackage.this.mPackageStatus);
                }
            }
        }, intentFilter);
    }

    private boolean isAllow() {
        return this.mPackageStatus == ISignallingNotifier.PackageStatus.INSTALLED;
    }

    public boolean send(Object o) {
        if (!Bundle.class.getSimpleName().equals(o.getClass().getSimpleName())) {
            return true;
        }
        Bundle b = (Bundle) o;
        if (!isAllow() || b.getInt("notifyType") != 0) {
            return true;
        }
        Intent i = new Intent(ISignallingNotifier.ACTION_SIP_MESSAGE);
        i.setPackage(this.mPackageName);
        i.putExtras(b);
        this.mContext.sendBroadcast(i, ISignallingNotifier.PERMISSION);
        return true;
    }
}
