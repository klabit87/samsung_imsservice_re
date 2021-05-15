package com.sec.internal.ims.core.sim;

import android.util.Log;
import java.util.List;

/* compiled from: SimDataAdaptor */
class SimDataAdaptorTmoUs extends SimDataAdaptor {
    private static final String LOG_TAG = SimDataAdaptorTmoUs.class.getSimpleName();

    SimDataAdaptorTmoUs(SimManager simManager) {
        super(simManager);
    }

    public String getImpuFromList(List<String> impus) {
        Log.i(LOG_TAG, "getImpuFromList:");
        if (impus == null || impus.size() == 0) {
            return null;
        }
        return impus.get(0);
    }
}
