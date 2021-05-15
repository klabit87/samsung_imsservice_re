package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;

public class QualityStatistics {
    public static final String ACTIONQUALITYSTATISTICS = "com.sec.android.statistics.VZW_QUALITY_STATISTICS";
    public static final String EVENTNAME = "H015";
    public static final String EVENTTYPE = "event_type";
    private static final String LOG_TAG = QualityStatistics.class.getSimpleName();
    private final Context mContext;

    public QualityStatistics(Context context) {
        Log.i(LOG_TAG, "QualityStatistics");
        this.mContext = context;
    }

    public void sendQualityStatisticsEvent() {
        Log.i(LOG_TAG, "sendQualityStatisticsEvent");
        Intent intent = new Intent();
        intent.setAction(ACTIONQUALITYSTATISTICS);
        intent.putExtra(EVENTTYPE, EVENTNAME);
        intent.setPackage(ImsConstants.Packages.PACKAGE_QUALITY_DATALOG);
        this.mContext.sendBroadcast(intent);
    }
}
