package com.sec.internal.ims.cmstore.strategy;

import android.content.Context;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.os.CscFeatureTagMessage;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy;

public class CloudMessageStrategyManager {
    private static final String TAG = CloudMessageStrategyManager.class.getSimpleName();
    private static ICloudMessageStrategy mCloudMessageStrategy;
    private static final Object mLock = new Object();

    public static void createStrategy(Context context) {
        synchronized (mLock) {
            Mno mno = SimUtil.getMno();
            if (mno != null) {
                String str = TAG;
                Log.d(str, "Carrier: " + mno.toString());
            }
            if (Mno.ATT.equals(mno)) {
                if (getEnableATTCloudService()) {
                    mCloudMessageStrategy = new ATTCmStrategy();
                    ATTGlobalVariables.initVersionName();
                } else {
                    mCloudMessageStrategy = new DefaultCloudMessageStrategy();
                }
            } else if (Mno.TMOUS.equals(mno)) {
                mCloudMessageStrategy = new TMOCmStrategy(context);
            } else {
                mCloudMessageStrategy = new DefaultCloudMessageStrategy();
                Log.e(TAG, "Unsupported Carrier");
            }
        }
    }

    public static ICloudMessageStrategy getStrategy() {
        ICloudMessageStrategy iCloudMessageStrategy;
        synchronized (mLock) {
            if (mCloudMessageStrategy == null) {
                mCloudMessageStrategy = new DefaultCloudMessageStrategy();
            }
            iCloudMessageStrategy = mCloudMessageStrategy;
        }
        return iCloudMessageStrategy;
    }

    public static boolean getEnableATTCloudService() {
        if (ATTGlobalVariables.PHASE_AMBS_SERVICE.contains(SemCscFeature.getInstance().getString(CscFeatureTagMessage.TAG_CSCFEATURE_MESSAGE_CONFIGOPBACKUPSYNC))) {
            return true;
        }
        Log.d(TAG, "Temp sim swap or CSC not enable");
        return false;
    }
}
