package com.sec.imsservice;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.os.Process;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.SoftphoneContract;
import com.sec.internal.constants.ims.os.CscFeatureTagIMS;
import com.sec.internal.ims.imsservice.ImsService;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import java.util.List;

public class ImsFrameworkApp extends Application {
    private static final String TAG = "ImsFrameworkApp";

    public void onCreate() {
        super.onCreate();
        if (Extensions.UserHandle.myUserId() != 0) {
            Log.e(TAG, "Do not initialize on non-system user");
            return;
        }
        String currentProcName = "";
        int pid = Process.myPid();
        ActivityManager manager = (ActivityManager) getSystemService("activity");
        if (manager == null) {
            Log.e(TAG, "Do not initalize IMS when AM is null");
            return;
        }
        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (processInfoList != null && processInfoList.size() > 0) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                if (processInfo != null && processInfo.pid == pid) {
                    currentProcName = processInfo.processName;
                }
            }
        }
        Log.i(TAG, "current process :" + currentProcName);
        if (currentProcName.endsWith(":ConfigService")) {
            Log.i(TAG, "this is rcs config process. stop init");
        } else if (currentProcName.endsWith(":CloudMessageService")) {
            Log.i(TAG, "this is CloudMessage process.");
            try {
                startService(new Intent(this, Class.forName("com.sec.internal.ims.cmstore.CloudMessageService")));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (currentProcName.endsWith(":CABService")) {
            Log.i(TAG, "this is CABService process. stop init");
        } else {
            Log.i(TAG, "onCreate()");
            try {
                ImsServiceStub.makeImsService(this);
                startService(new Intent(this, ImsService.class));
            } catch (SQLiteFullException e2) {
                Log.e(TAG, "makeImsService " + e2.getMessage());
            }
            if (SemCscFeature.getInstance().getString(CscFeatureTagIMS.TAG_CSCFEATURE_IMS_CONFIGMDMNTYPE).toUpperCase().contains("Softphone".toUpperCase())) {
                try {
                    startService(new Intent(this, Class.forName(SoftphoneContract.SERVICE_CLASS_NAME)));
                } catch (ClassNotFoundException e3) {
                    e3.printStackTrace();
                }
            }
        }
    }
}
