package com.sec.internal.ims.core.imslogger;

import android.content.Context;
import android.util.Log;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.SystemUtil;
import com.sec.internal.interfaces.ims.core.imslogger.ISignallingNotifier;
import java.util.ArrayList;
import java.util.Iterator;

public class ExternalSupporter {
    private static final String LOG_TAG = ExternalSupporter.class.getSimpleName();
    private static final ArrayList<ISignallingNotifier> mPackages = new ArrayList<>();
    private ImsLoggerPlus mImsLogger = null;

    public ExternalSupporter(Context context) {
        SemEmergencyManager emergencyManager = SemEmergencyManager.getInstance(context);
        if (SemEmergencyManager.isEmergencyMode(context)) {
            if (SystemUtil.checkUltraPowerSavingMode(emergencyManager)) {
                Log.i(LOG_TAG, "UPSM mode skip package add");
            } else if (emergencyManager.checkModeType(16)) {
                Log.i(LOG_TAG, "EMERGENCY mode skip package add");
            }
        } else if (ISignallingNotifier.ENG || !ISignallingNotifier.SHIPBUILD || DeviceUtil.isOtpAuthorized()) {
            Log.i(LOG_TAG, "package add");
            mPackages.add(new ExternalPackage(context, "com.hugeland.cdsplus"));
            ImsLoggerPlus imsLoggerPlus = new ImsLoggerPlus(context, "com.sec.imslogger");
            this.mImsLogger = imsLoggerPlus;
            mPackages.add(imsLoggerPlus);
        }
    }

    public boolean send(Object o) {
        Iterator<ISignallingNotifier> it = mPackages.iterator();
        while (it.hasNext()) {
            it.next().send(o);
        }
        return true;
    }

    public ISignallingNotifier.PackageStatus checkPackageStatus() {
        ImsLoggerPlus imsLoggerPlus = this.mImsLogger;
        if (imsLoggerPlus != null) {
            return imsLoggerPlus.checkPackageStatus();
        }
        return ISignallingNotifier.PackageStatus.NOT_INSTALLED;
    }

    public void initialize() {
        ImsLoggerPlus imsLoggerPlus = this.mImsLogger;
        if (imsLoggerPlus != null) {
            imsLoggerPlus.initialize();
        }
    }
}
